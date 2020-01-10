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

import javax.servlet.*;
import javax.servlet.http.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.base.*;
import com.netscape.cmsutil.util.Utils;


/**
 * This servlet is started by the web server at startup, and
 * it starts the CMS framework.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class CMSStartServlet extends HttpServlet {
    public final static String PROP_CMS_CFG = "cfgPath";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String path = config.getInitParameter(PROP_CMS_CFG);

        File f = new File(path);
        String old_path = "";
        if (!f.exists()) {
            int index = path.lastIndexOf("CS.cfg");
            if (index != -1) {
                old_path = path.substring(0, index)+"CMS.cfg";
            }
            File f1 = new File(old_path);
            if (f1.exists()) {
                // The following block of code moves "CMS.cfg" to "CS.cfg".
                try {
                    if( Utils.isNT() ) {
                        // NT is very picky on the path
                        Utils.exec( "copy " +
                                    f1.getAbsolutePath().replace( '/', '\\' ) +
                                    " " +
                                    f.getAbsolutePath().replace( '/', '\\' ) );
                    } else {
                        // Create a copy of the original file which
                        // preserves the original file permissions.
                        Utils.exec( "cp -p " + f1.getAbsolutePath() + " " +
                                    f.getAbsolutePath() );
                    }

                    // Remove the original file if and only if
                    // the backup copy was successful.
                    if( f.exists() ) {
                        f1.delete();

                        // Make certain that the new file has
                        // the correct permissions.
                        if( !Utils.isNT() ) {
                            Utils.exec( "chmod 00660 " + f.getAbsolutePath() );
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        try {
            CMS.start(path);
        } catch (EBaseException e) {
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        res.setContentType("text/html");

        PrintWriter out = res.getWriter();

        out.print("<html>");
        out.print("<head><title>CMS is started!</title></head>");
        out.print("<body>");
        out.print("<h1>CMS is started!</h1>");
        out.print("</body></html>");
    }

    public String getServletInfo() {
        return "CMS startup servlet";
    }

    public void destroy() {
        CMS.shutdown();
        super.destroy();
    }
}
