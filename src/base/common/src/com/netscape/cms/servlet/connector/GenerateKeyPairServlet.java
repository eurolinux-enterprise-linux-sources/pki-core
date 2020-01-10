// --- BEGIN COPYRIGHT BLOCK ---
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
// (C) 2007 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---
package com.netscape.cms.servlet.connector;

import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;

import com.netscape.certsrv.common.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.CMS;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;



/**
 * GenerateKeyPairServlet
 *   handles "server-side key pair generation" requests from the
 * netkey RA. 
 *
 * @author Christina Fu (cfu)
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
//XXX add auditing later
public class GenerateKeyPairServlet extends CMSServlet {

    private final static String INFO = "GenerateKeyPairServlet";
    public final static String PROP_AUTHORITY = "authority";
    protected ServletConfig mConfig = null;
    protected IAuthority mAuthority = null;
    public static int ERROR = 1;
    IPrettyPrintFormat pp = CMS.getPrettyPrintFormat(":");
    protected IAuthSubsystem mAuthSubsystem = null;
    protected ILogger mLogger = CMS.getLogger();

    /**
     * Constructs GenerateKeyPair servlet.
     *
     */
    public GenerateKeyPairServlet() {
        super();
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        mConfig = config;
        String authority = config.getInitParameter(PROP_AUTHORITY);

        if (authority != null)
            mAuthority = (IAuthority)
                    CMS.getSubsystem(authority);
        
        mAuthSubsystem = (IAuthSubsystem) CMS.getSubsystem(CMS.SUBSYSTEM_AUTH);
    }

    /**
     * Returns serlvet information.
     *
     * @return name of this servlet
     */
    public String getServletInfo() { 
        return INFO; 
    }

    /*
     * processServerSideKeyGen -
     *   handles netkey DRM serverside keygen.
     * netkey operations: 
     *  1. generate keypair (archive user priv key)
     *  2. unwrap des key with transport key, then url decode it
     *  3. wrap user priv key with des key
     *  4. send the following to RA:
     *      * des key wrapped(user priv key)
     *      * user public key
     *     (note: RA should have kek-wrapped des key from TKS)
     *      * recovery blob (used for recovery)
     */
    private void processServerSideKeyGen(HttpServletRequest req,
      HttpServletResponse resp) throws EBaseException
    {
        IRequestQueue queue = mAuthority.getRequestQueue();
        IRequest thisreq = null;

        IConfigStore sconfig = CMS.getConfigStore();
        boolean missingParam = false;
        String status = "0";

        CMS.debug("processServerSideKeyGen begins:");

        String rCUID = req.getParameter("CUID");
        String rUserid = req.getParameter("userid");
        String rdesKeyString = req.getParameter("drm_trans_desKey");
	String rArchive = req.getParameter("archive");
	String rKeysize = req.getParameter("keysize");

        if ((rCUID == null) || (rCUID.equals(""))) {
            CMS.debug("GenerateKeyPairServlet: processServerSideKeygen(): missing request parameter: CUID");
            missingParam = true;
        }

        if ((rUserid == null) || (rUserid.equals(""))) {
            CMS.debug("GenerateKeyPairServlet: processServerSideKeygen(): missing request parameter: userid");
            missingParam = true;
        }

	if ((rKeysize == null) || (rKeysize.equals(""))) {
	    rKeysize = "1024"; // default to 1024
	}

        if ((rdesKeyString == null) ||
            (rdesKeyString.equals(""))) {
            CMS.debug("GenerateKeyPairServlet: processServerSideKeygen(): missing request parameter: DRM-transportKey-wrapped DES key");
            missingParam = true;
        }

        if ((rArchive == null) || (rArchive.equals(""))) {
            CMS.debug("GenerateKeyPairServlet: processServerSideKeygen(): missing key archival flag 'archive' ,default to true");
	    rArchive = "true";
        }

        String selectedToken = null;

        if (!missingParam) {
            thisreq = queue.newRequest(IRequest.NETKEY_KEYGEN_REQUEST);

            thisreq.setExtData(IRequest.REQUESTOR_TYPE, IRequest.REQUESTOR_NETKEY_RA);
            thisreq.setExtData(IRequest.NETKEY_ATTR_CUID, rCUID);
            thisreq.setExtData(IRequest.NETKEY_ATTR_USERID, rUserid);
            thisreq.setExtData(IRequest.NETKEY_ATTR_DRMTRANS_DES_KEY, rdesKeyString);
	    thisreq.setExtData(IRequest.NETKEY_ATTR_ARCHIVE_FLAG, rArchive);
	    thisreq.setExtData(IRequest.NETKEY_ATTR_KEY_SIZE, rKeysize);

            queue.processRequest( thisreq );
            Integer result = thisreq.getExtDataInInteger(IRequest.RESULT);
            if (result != null) {
		// sighs!  tps thinks 0 is good, and DRM thinks 1 is good
		if (result.intValue() == 1)
		    status = "0";
		else
		    status = result.toString();
            } else
                status = "7";

            CMS.debug("processServerSideKeygen finished");
        } // ! missingParam

        String value = "";

        resp.setContentType("text/html");

        String wrappedPrivKeyString = "";
        String publicKeyString = "";

        if( thisreq == null ) {
            CMS.debug( "GenerateKeyPairServlet::processServerSideKeyGen() - "
                     + "thisreq is null!" );
            throw new EBaseException( "thisreq is null" );
        }

        publicKeyString = thisreq.getExtDataInString("public_key");
        wrappedPrivKeyString = thisreq.getExtDataInString("wrappedUserPrivate");

	String ivString = thisreq.getExtDataInString("iv_s");

        /*
          if (selectedToken == null)
          status = "4";
        */
        if (!status.equals("0")) 
            value = "status="+status;
        else {
            StringBuffer sb = new StringBuffer();
            sb.append("status=0&");
			sb.append("wrapped_priv_key=");
			sb.append(wrappedPrivKeyString);
			sb.append("&iv_param=");
			sb.append(ivString);
            sb.append("&public_key=");
			sb.append(publicKeyString);
            value = sb.toString();

        }
        CMS.debug("processServerSideKeyGen:outputString.encode " +value);

        try{
            resp.setContentLength(value.length());
            CMS.debug("GenerateKeyPairServlet:outputString.length " +value.length());
            OutputStream ooss = resp.getOutputStream();
            ooss.write(value.getBytes());
            ooss.flush();
            mRenderResult = false;
        } catch (IOException e) {
            CMS.debug("GenerateKeyPairServlet: " + e.toString());
        }
    }


    /* 
    
     *   For GenerateKeyPair:
     *
     *   input:
     *   CUID=value0
     *   trans-wrapped-desKey=value1
     *
     *   output:
     *   status=value0
     *   publicKey=value1
     *   desKey-wrapped-userPrivateKey=value2
     *   proofOfArchival=value3
     */

    public void process(CMSRequest cmsReq) throws EBaseException {
        HttpServletRequest req = cmsReq.getHttpReq();
        HttpServletResponse resp = cmsReq.getHttpResp();

        IAuthToken authToken = authenticate(cmsReq);
        AuthzToken authzToken = null;

        try {
            authzToken = authorize(mAclMethod, authToken,
                        mAuthzResourceName, "execute");
        } catch (Exception e) {
        }

        if (authzToken == null) {

            try{
                resp.setContentType("text/html");
                String value = "unauthorized=";
                CMS.debug("GenerateKeyPairServlet: Unauthorized");

                resp.setContentLength(value.length());
                OutputStream ooss = resp.getOutputStream();
                ooss.write(value.getBytes());
                ooss.flush();
                mRenderResult = false;
            }catch (Exception e) {
                CMS.debug("GenerateKeyPairServlet: " + e.toString());
            }

            cmsReq.setStatus(CMSRequest.UNAUTHORIZED);
            return;
        }

        // begin Netkey serverSideKeyGen and archival
	CMS.debug("GenerateKeyPairServlet: processServerSideKeyGen would be called");
	processServerSideKeyGen(req, resp);
	return;
        // end Netkey functions

    }

    /** XXX remember tocheck peer SSL cert and get RA id later
     *
     * Serves HTTP admin request.
     *
     * @param req HTTP request
     * @param resp HTTP response
     */
    public void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String scope = req.getParameter(Constants.OP_SCOPE);
        String op = req.getParameter(Constants.OP_TYPE);

       super.service(req, resp);

 
    }

}
