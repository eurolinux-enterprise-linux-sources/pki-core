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
package com.netscape.cms.servlet.ocsp;


import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;

import java.io.*;
import java.util.*;
import java.math.*;
import javax.servlet.*;
import java.security.cert.*;
import javax.servlet.http.*;
import netscape.ldap.*;
import netscape.security.x509.*;
import netscape.security.pkcs.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.ocsp.*;
import com.netscape.certsrv.policy.*;
import com.netscape.certsrv.request.IRequest;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.certdb.*;
import com.netscape.certsrv.ldap.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.ca.*;

import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.crldb.*;
import com.netscape.cms.servlet.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;


/**
 * Retrieve information about the number of OCSP requests the OCSP 
 * has serviced
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class GetOCSPInfo extends CMSServlet {
    private final static String TPL_FILE = "getOCSPInfo.template";
    private String mFormPath = null;

    public GetOCSPInfo() {
        super();
    }

    /**
     * initialize the servlet. This servlet uses the template
     * file "getOCSPInfo.template" to render the result page.
     *
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        // override success to display own output.

        // coming from agent
        mFormPath = "/" + mAuthority.getId() + "/" + TPL_FILE;

        mTemplates.remove(CMSRequest.SUCCESS);
        if (mOutputTemplatePath != null)
            mFormPath = mOutputTemplatePath;

    }


    /**
	 * Process the HTTP request. 
     *
     * @param cmsReq the object holding the request and response information
     */
    protected void process(CMSRequest cmsReq)
        throws EBaseException {
        HttpServletRequest httpReq = cmsReq.getHttpReq();
        HttpServletResponse httpResp = cmsReq.getHttpResp();

        IAuthToken authToken = authenticate(cmsReq);
        AuthzToken authzToken = null;

        try {
            authzToken = authorize(mAclMethod, authToken,
                        mAuthzResourceName, "read");
        } catch (EAuthzAccessDenied e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
        } catch (Exception e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
        }

        if (authzToken == null) {
            cmsReq.setStatus(CMSRequest.UNAUTHORIZED);
            return;
        }

        // Construct an ArgBlock
        IArgBlock args = cmsReq.getHttpParams();

        if (!(mAuthority instanceof IOCSPService)) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_CA_FROM_RA_NOT_IMP"));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_NOT_YET_IMPLEMENTED")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }

        CMSTemplate form = null;
        Locale[] locale = new Locale[1];

        try {
            form = getTemplate(mFormPath, httpReq, locale);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_ERR_GET_TEMPLATE", mFormPath, e.toString()));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }

        IArgBlock header = CMS.createArgBlock();
        IArgBlock fixed = CMS.createArgBlock();
        CMSTemplateParams argSet = new CMSTemplateParams(header, fixed);

        IOCSPService ca = (IOCSPService) mAuthority;

        header.addLongValue("numReq", ca.getNumOCSPRequest());
        header.addLongValue("totalSec", ca.getOCSPRequestTotalTime());
        header.addLongValue("totalSignSec", ca.getOCSPTotalSignTime());
        header.addLongValue("totalLookupSec", ca.getOCSPTotalLookupTime());
        header.addLongValue("totalData", ca.getOCSPTotalData());
        long secs = 0;
        if (ca.getOCSPRequestTotalTime() != 0) {
             secs = (ca.getNumOCSPRequest() * 1000) / ca.getOCSPRequestTotalTime();
        } 
        header.addLongValue("ReqSec", secs);
        try {
            ServletOutputStream out = httpResp.getOutputStream();

            httpResp.setContentType("text/html");
            form.renderOutput(out, argSet);
            cmsReq.setStatus(CMSRequest.SUCCESS);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_ERR_STREAM_TEMPLATE", e.toString()));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR")));
            cmsReq.setStatus(CMSRequest.ERROR);
        }
        cmsReq.setStatus(CMSRequest.SUCCESS);
        return;
    }

}
