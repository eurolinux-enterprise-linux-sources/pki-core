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


import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.apps.CMS;
import com.netscape.cms.servlet.common.*;


/**
 * This is the servlet that displays the html page for the corresponding input id.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class DisplayHtmlServlet extends CMSServlet {
    public final static String PROP_TEMPLATE = "template";
    public final static String PROP_HTML_PATH = "htmlPath";

    private String mHTMLPath = null;

    public DisplayHtmlServlet() {
        super();
    }

    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        mHTMLPath = sc.getInitParameter(PROP_HTML_PATH); 
        mTemplates.remove(CMSRequest.SUCCESS);
    }

    /**
     * Serves HTTP request.
     */
    public void process(CMSRequest cmsReq) throws EBaseException {
        CMS.debug("DisplayHtmlServlet about to service ");

        IAuthToken authToken = authenticate(cmsReq);

        try {
            String realpath = 
              mServletConfig.getServletContext().getRealPath("/" + mHTMLPath);

            if (realpath == null) {
                mLogger.log(
                  ILogger.EV_SYSTEM, ILogger.S_OTHER, ILogger.LL_FAILURE,
                  CMS.getLogMessage("CMSGW_NO_FIND_TEMPLATE", mHTMLPath));
                throw new ECMSGWException(CMS.getLogMessage("CMSGW_ERROR_DISPLAY_TEMPLATE")) ;
            }
            File file = new File(realpath);
            long flen = file.length();
            byte[] bin = new byte[(int)flen];
            FileInputStream ins = new FileInputStream(file);

            int len = 0;
            if (ins.available() > 0) {
                len = ins.read(bin);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(bin, 0, len);
            bos.writeTo(cmsReq.getHttpResp().getOutputStream());
            ins.close();
            bos.close();
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, 
              CMS.getLogMessage("CMSGW_ERR_OUT_TEMPLATE", mHTMLPath, e.toString()));
            throw new ECMSGWException(CMS.getLogMessage("CMSGW_ERROR_DISPLAY_TEMPLATE")); 
        }
    }
}
