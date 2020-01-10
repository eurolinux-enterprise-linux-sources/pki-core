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
package com.netscape.cms.servlet.key;


import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.security.*;
import javax.servlet.*;
import javax.servlet.http.*;
import netscape.security.x509.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.base.*;
 
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.dbs.keydb.*;

import com.netscape.cms.servlet.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.kra.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.authentication.*;


/**
 * A class representing a recoverKey servlet. This servlet
 * shows key information and presents a list of text boxes
 * so that recovery agents can type in their identifiers
 * and passwords.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class ConfirmRecoverBySerial extends CMSServlet {

    private final static String INFO = "recoverBySerial";
    private final static String TPL_FILE = 
        "confirmRecoverBySerial.template";

    private final static String IN_SERIALNO = "serialNumber";
    private final static String OUT_SERIALNO = IN_SERIALNO;
    private final static String OUT_OP = "op";
    private final static String OUT_SERVICE_URL = "serviceURL";
    private final static String OUT_M = "noOfRequiredAgents";
    private final static String OUT_ERROR = "errorDetails";

    private IKeyRepository mKeyDB = null;
    private IKeyService mRecoveryService = null;
    private String mFormPath = null;

    /**
     * Constructs ConfirmRecoverBySerial servlet.
     */
    public ConfirmRecoverBySerial() {
        super();
    }

    /**
     * Initializes the servlet.
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        mFormPath = "/" + mAuthority.getId() + "/" + TPL_FILE;
        mRecoveryService = (IKeyService) mAuthority;
        mKeyDB = ((IKeyRecoveryAuthority) mAuthority).getKeyRepository();

        mTemplates.remove(CMSRequest.SUCCESS);
    }

    /**
     * Returns serlvet information.
     */
    public String getServletInfo() { 
        return INFO; 
    }

    /**
     * Serves HTTP request. The format of this request is 
     * as follows:
     *   confirmRecoverBySerial?
     *     [serialNumber=<serialno>]
     */
    public void process(CMSRequest cmsReq) throws EBaseException {

        // Note that we should try to handle all the exceptions
        // instead of passing it up back to the servlet 
        // framework.
		
        HttpServletRequest req = cmsReq.getHttpReq();
        HttpServletResponse resp = cmsReq.getHttpResp();

        IAuthToken authToken = authenticate(cmsReq);

        CMSTemplate form = null;
        Locale[] locale = new Locale[1];

        try {
            form = getTemplate(mFormPath, req, locale);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("CMSGW_ERR_GET_TEMPLATE", mFormPath, e.toString()));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
        }

        IArgBlock header = CMS.createArgBlock();
        IArgBlock fixed = CMS.createArgBlock();
        CMSTemplateParams argSet = new CMSTemplateParams(header, fixed);

        int seqNum = -1;

        try {
            if (req.getParameter(IN_SERIALNO) != null) {
                seqNum = Integer.parseInt(
                            req.getParameter(IN_SERIALNO));
            }

            // make sure this page, which contains password
            // information, is not cache. Too bad, this is
            // only good for NS browser, not IE specifically.
            resp.setHeader("pragma", "no-cache");

            process(argSet, header, seqNum, req, resp, locale[0]);
        } catch (NumberFormatException e) {
            header.addStringValue(OUT_ERROR, 
                CMS.getUserMessage(locale[0], "CMS_BASE_INTERNAL_ERROR", e.toString()));
        }

        try {
            ServletOutputStream out = resp.getOutputStream();

            resp.setContentType("text/html");
            form.renderOutput(out, argSet);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_ERR_STREAM_TEMPLATE", e.toString()));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
        }
        cmsReq.setStatus(CMSRequest.SUCCESS);
    }

    /**
     * Requests for a list of agent passwords.
     */
    private void process(CMSTemplateParams argSet,
        IArgBlock header, int seq, 
        HttpServletRequest req, HttpServletResponse resp,
        Locale locale) {
        try {
            header.addIntegerValue(OUT_SERIALNO, seq);
            header.addIntegerValue(OUT_M,
                mRecoveryService.getNoOfRequiredAgents());
            header.addStringValue(OUT_OP,
                req.getParameter(OUT_OP));
            header.addStringValue(OUT_SERVICE_URL,
                req.getRequestURI());

            IKeyRecord rec = (IKeyRecord) mKeyDB.readKeyRecord(new BigInteger(
                        Integer.toString(seq)));

            KeyRecordParser.fillRecordIntoArg(rec, header);
        } catch (EBaseException e) {
            header.addStringValue(OUT_ERROR, e.toString(locale));
        }
    }
}
