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
package com.netscape.cms.servlet.base;


import com.netscape.cms.servlet.common.*;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.usrgrp.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.ca.*;
import com.netscape.certsrv.kra.*;
import com.netscape.certsrv.apps.CMS;


/**
 * This is the servlet that builds the index page in
 * various ports.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class IndexServlet extends CMSServlet {
    public final static String PROP_TEMPLATE = "template";

    private final static String INFO = "indexServlet";

    // input parameters

    // output parameters
    private final static String OUT_TYPE = "type";
    private final static String OUT_ID = "id";
    private final static String OUT_TOTAL_COUNT = "totalCount";
    private final static String OUT_ERROR = "errorDetails";

    private String mTemplateName = null;

    public IndexServlet() {
        super();
    }

    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        mTemplateName = sc.getInitParameter(PROP_TEMPLATE);

        /*
         mTemplates.put(CMSRequest.SUCCESS,
         new CMSLoadTemplate(
         PROP_SUCCESS_TEMPLATE, PROP_SUCCESS_TEMPLATE_FILLER,
         mTemplateName, new IndexTemplateFiller()));
         */
        mTemplates.remove(CMSRequest.SUCCESS);
    }

    public CMSRequest newCMSRequest() {
        return new CMSRequest();
    }

    /**
     * Returns serlvet information.
     */
    public String getServletInfo() {
        return INFO;
    }

    /**
     * Serves HTTP request.
     */
    public void process(CMSRequest cmsReq) throws EBaseException {
        if (CMSGateway.getEnableAdminEnroll() && 
            mAuthority != null && 
            mAuthority instanceof ICertificateAuthority) {
            try {
                cmsReq.getHttpResp().sendRedirect("/ca/adminEnroll.html");
            } catch (IOException e) {
                log(ILogger.LL_FAILURE, 
                    CMS.getLogMessage("CMSGW_FAIL_REDIRECT_ADMIN_ENROLL", e.toString()));
                throw new ECMSGWException(
                        CMS.getLogMessage("CMSGW_ERROR_REDIRECTING_ADMINENROLL1",
                            e.toString()));
            }
            return;
        } else {
            try {
                renderTemplate(
                    cmsReq, mTemplateName, new IndexTemplateFiller());
            } catch (IOException e) {
                log(ILogger.LL_FAILURE, 
                    CMS.getLogMessage("CMSGW_FAIL_RENDER_TEMPLATE", mTemplateName, e.toString()));
                throw new ECMSGWException(
                        CMS.getLogMessage("CMSG_ERROR_DISPLAY_TEMPLATE"));
            }
        }
        cmsReq.setStatus(CMSRequest.SUCCESS);
    }
}
