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
import java.math.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import org.w3c.dom.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.CMS;
import com.netscape.cms.servlet.*;
import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;
import com.netscape.cmsutil.xml.*;

public class TokenAuthenticate extends CMSServlet {

    private final static String SUCCESS = "0";
    private final static String FAILED = "1";

    public TokenAuthenticate() {
        super();
    }

    /**
     * initialize the servlet.
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
    }

    /**
     * Process the HTTP request. 
     * @param cmsReq the object holding the request and response information
     */
    protected void process(CMSRequest cmsReq) throws EBaseException {
        HttpServletRequest httpReq = cmsReq.getHttpReq();
        HttpServletResponse httpResp = cmsReq.getHttpResp();
	IConfigStore config = CMS.getConfigStore();

        String sessionId = httpReq.getParameter("sessionID");
        CMS.debug("TokenAuthentication: sessionId=" + sessionId);
        String givenHost = httpReq.getParameter("hostname");
        CMS.debug("TokenAuthentication: givenHost=" + givenHost);

        ISecurityDomainSessionTable table = CMS.getSecurityDomainSessionTable();
        String uid = "";
        String gid = "";
        CMS.debug("TokenAuthentication: checking session in the session table");
        if (table.isSessionIdExist(sessionId)) {
            CMS.debug("TokenAuthentication: found session");
            String hostname = table.getIP(sessionId);
            if (hostname.equals(givenHost)) {
                CMS.debug("TokenAuthentication: hostname and givenHost matched");
                uid = table.getUID(sessionId);
                gid = table.getGroup(sessionId);
            } else {
                CMS.debug("TokenAuthentication: hostname=" + hostname + " and givenHost=" + givenHost + " is different");
                CMS.debug("TokenAuthenticate authenticate failed, wrong hostname.");
                outputError(httpResp, "Error: Failed Authentication");
                return;
            }
        } else {
            CMS.debug("TokenAuthentication: session not found");
            CMS.debug("TokenAuthentication authenticate failed, session id does not exist.");
            outputError(httpResp, "Error: Failed Authentication");
            return;
        }

        CMS.debug("TokenAuthenticate successfully authenticate");
        try {
            XMLObject xmlObj = null;

            xmlObj = new XMLObject();

            Node root = xmlObj.createRoot("XMLResponse");

            xmlObj.addItemToContainer(root, "Status", SUCCESS);
            xmlObj.addItemToContainer(root, "uid", uid);
            xmlObj.addItemToContainer(root, "gid", gid);
            byte[] cb = xmlObj.toByteArray();

            outputResult(httpResp, "application/xml", cb);
        } catch (Exception e) {
            CMS.debug("Failed to send the XML output");
        }
    }

    protected void renderResult(CMSRequest cmsReq) throws IOException {// do nothing, ie, it will not return the default javascript.
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
}
