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
package com.netscape.cms.servlet.csadmin;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import java.security.cert.*;
import javax.servlet.http.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.CMS;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;
import com.netscape.cms.servlet.*;
import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;
import com.netscape.cmsutil.xml.*;
import com.netscape.cmsutil.password.*;
import org.w3c.dom.*;

public class GetTokenInfo extends CMSServlet {

    private final static String SUCCESS = "0";
    private final static String FAILED = "1";

    public GetTokenInfo() {
        super();
    }

    /**
     * initialize the servlet.
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        CMS.debug("GetTokenInfo init");
    }

    /**
     * Process the HTTP request. 
     * <ul>
     * <li>http.param op 'downloadBIN' - return the binary certificate chain
     * <li>http.param op 'displayIND' - display pretty-print of certificate chain components
     * </ul>
     * @param cmsReq the object holding the request and response information
     */
    protected void process(CMSRequest cmsReq) throws EBaseException {
        HttpServletRequest httpReq = cmsReq.getHttpReq();
        HttpServletResponse httpResp = cmsReq.getHttpResp();

        // Construct an ArgBlock
        IArgBlock args = cmsReq.getHttpParams();

        XMLObject xmlObj = null;
        try {
            xmlObj = new XMLObject();
        } catch (Exception e) {
            CMS.debug("GetTokenInfo process: Exception: "+e.toString());
            throw new EBaseException( e.toString() );
        }

        Node root = xmlObj.createRoot("XMLResponse");

        IConfigStore config = CMS.getConfigStore();

        String certlist = "";
        try {
            certlist = config.getString("cloning.list");
        } catch (Exception e) {
        }

        StringTokenizer t1 = new StringTokenizer(certlist, ",");
        while (t1.hasMoreTokens()) {
            String name = t1.nextToken();
            if (name.equals("sslserver"))
                continue;
            name = "cloning."+name+".nickname";
            String value = "";

            try {
                value = config.getString(name);
            } catch (Exception ee) {
                continue;
            }
             
            Node container = xmlObj.createContainer(root, "Config");
            xmlObj.addItemToContainer(container, "name", name);
            xmlObj.addItemToContainer(container, "value", value);
        }

        String value = "";
        String name = "cloning.module.token";
        try {
            value = config.getString(name);
        } catch (Exception e) {
        }

        Node container = xmlObj.createContainer(root, "Config");
        xmlObj.addItemToContainer(container, "name", name);
        xmlObj.addItemToContainer(container, "value", value);

        try {
            xmlObj.addItemToContainer(root, "Status", SUCCESS);
            byte[] cb = xmlObj.toByteArray();

            outputResult(httpResp, "application/xml", cb);
        } catch (Exception e) {
            CMS.debug("Failed to send the XML output");
        }
    }

    /**
     * Retrieves locale based on the request.
     */
    protected Locale getLocale(HttpServletRequest req) {
        Locale locale = null;
        String lang = req.getHeader("accept-language");

        if (lang == null) {
            // use server locale
            locale = Locale.getDefault();
        } else {
            locale = new Locale(UserInfo.getUserLanguage(lang),
                    UserInfo.getUserCountry(lang));
        }
        return locale;
    }

    protected void renderResult(CMSRequest cmsReq) throws IOException {// do nothing, ie, it will not return the default javascript.
    }
}
