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
import java.security.cert.X509Certificate;
import javax.servlet.*;
import javax.servlet.http.*;
import netscape.security.x509.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.base.*;
 
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.keydb.*;

import com.netscape.cms.servlet.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.request.IRequest;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.security.*;
import com.netscape.certsrv.kra.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;


/**
 * Check to see if a Key Recovery Request has been approved
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class GetApprovalStatus extends CMSServlet {

    private final static String INFO = "getApprovalStatus";
    private final static String TPL_FILE = "getApprovalStatus.template";
    private final static String TPL_FINISH = "finishRecovery.template";

    private final static String IN_DELIVERY = "p12Delivery";

    private final static String OUT_RECOVERY_SUCCESS = "recoverySuccess";
    private final static String OUT_ERROR = "errorDetails";
    private final static String OUT_STATUS = "status";

    private com.netscape.certsrv.kra.IKeyService mService = null;
    private String mFormPath = null;

    /**
     * Constructs getApprovalStatus servlet.
     */
    public GetApprovalStatus() {
        super();
    }

    /**
     * initialize the servlet. This servlet uses the template files
     * "getApprovalStatus.template" and "finishRecovery.template"
     * to process the response.
     *
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        // mFormPath = "/"+authority.getId()+"/"+TPL_FILE;
        mService = (com.netscape.certsrv.kra.IKeyService) mAuthority;

        mTemplates.remove(CMSRequest.SUCCESS);
    }

    /**
     * Returns serlvet information.
     */
    public String getServletInfo() { 
        return INFO; 
    }

    /**
     * Process the HTTP request.
     * <ul>
     * <li>http.param recoveryID request ID to check
     * </ul>
     *
     * @param cmsReq the object holding the request and response information
     */
    public void process(CMSRequest cmsReq) throws EBaseException {

        HttpServletRequest req = cmsReq.getHttpReq();
        HttpServletResponse resp = cmsReq.getHttpResp();

        IAuthToken authToken = authenticate(cmsReq);

        AuthzToken authzToken = null;

        try {
            authzToken = authorize(mAclMethod, authToken,
                        mAuthzResourceName, "read");
        } catch (Exception e) {
            // do nothing for now
        }

        if (authzToken == null) {
            cmsReq.setStatus(CMSRequest.UNAUTHORIZED);
            return;
        }

        CMSTemplate form = null;
        Locale[] locale = new Locale[1];

        cmsReq.setStatus(CMSRequest.SUCCESS);
        IArgBlock header = CMS.createArgBlock();
        IArgBlock fixed = CMS.createArgBlock();
        CMSTemplateParams argSet = new CMSTemplateParams(header, fixed);
        int seq = -1;
        int rComplete = 0;

        // get status and populate argSet
        try {
            String recoveryID = req.getParameter("recoveryID");

            header.addStringValue("recoveryID", recoveryID);

            Hashtable params = mService.getRecoveryParams(recoveryID);

            if (params == null) {
                log(ILogger.LL_FAILURE,
                    CMS.getLogMessage("CMSGW_NO_RECOVERY_TOKEN_FOUND_1", recoveryID));
                throw new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_NO_RECOVERY_TOKEN_FOUND", recoveryID));
            }
            header.addStringValue("serialNumber",
                (String) params.get("keyID"));

            int requiredNumber = mService.getNoOfRequiredAgents();

            header.addIntegerValue("noOfRequiredAgents", requiredNumber);

            Vector dc = ((IKeyRecoveryAuthority) mService).getAppAgents(recoveryID);
            Enumeration agents = dc.elements();

            while (agents.hasMoreElements()) {
                IArgBlock rarg = CMS.createArgBlock();

                rarg.addStringValue("agentName", ((Credential) agents.nextElement()).getIdentifier());
                argSet.addRepeatRecord(rarg);
            }
            if (dc.size() >= requiredNumber) {
                // got all approval, return pk12
                byte pkcs12[] = ((IKeyRecoveryAuthority) mService).getPk12(recoveryID);

                if (pkcs12 != null) {
                    rComplete = 1;
                    header.addStringValue(OUT_STATUS, "complete");        

                    /*
                     mService.destroyRecoveryParams(recoveryID);
                     try {
                     resp.setContentType("application/x-pkcs12");
                     resp.getOutputStream().write(pkcs12);
                     return;
                     } catch (IOException e) {
                     header.addStringValue(OUT_ERROR,
                     MessageFormatter.getLocalizedString(
                     locale[0],
                     BaseResources.class.getName(),
                     BaseResources.INTERNAL_ERROR_1,
                     e.toString()));
                     }
                     */
                } else if (((IKeyRecoveryAuthority) mService).getError(recoveryID) != null) {
                    // error in recovery process 
                    header.addStringValue(OUT_ERROR, 
                        ((IKeyRecoveryAuthority) mService).getError(recoveryID));
                    rComplete = 1;
                } else {
                    // pk12 hasn't been created yet.
                }
            }
        } catch (EBaseException e) {
            header.addStringValue(OUT_ERROR, e.toString(locale[0]));
            rComplete = 1;
        }

        try {
            if (rComplete == 1) {
                mFormPath = "/" + ((IAuthority) mService).getId() + "/" + TPL_FINISH;
            } else {
                mFormPath = "/" + ((IAuthority) mService).getId() + "/" + TPL_FILE;
            }    
            if (mOutputTemplatePath != null)
                mFormPath = mOutputTemplatePath;
            try {
                form = getTemplate(mFormPath, req, locale);
            } catch (IOException e) {
                log(ILogger.LL_FAILURE,
                    CMS.getLogMessage("CMSGW_ERR_GET_TEMPLATE", mFormPath, e.toString()));
                throw new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
            }

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
}
