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
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.policy.*;
import com.netscape.certsrv.request.IRequest;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.certdb.*;
import com.netscape.certsrv.ldap.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.CMS;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;
import com.netscape.cms.servlet.*;
import com.netscape.cmsutil.xml.*;
import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class UpdateDomainXML extends CMSServlet {

    private final static String SUCCESS = "0";
    private final static String FAILED = "1";

    public UpdateDomainXML() {
        super();
    }

    /**
     * initialize the servlet.
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        CMS.debug("UpdateDomainXML: initializing...");
        super.init(sc);
        CMS.debug("UpdateDomainXML: done initializing...");
    }

    private String remove_from_ldap(String dn) {
        CMS.debug("UpdateDomainXML: delete_from_ldap: starting dn: " + dn);
        String status = SUCCESS;
        ILdapConnFactory connFactory = null;
        LDAPConnection conn = null;
        IConfigStore cs = CMS.getConfigStore();

        try {
            IConfigStore ldapConfig = cs.getSubStore("internaldb");
            connFactory = CMS.getLdapBoundConnFactory();
            connFactory.init(ldapConfig);
            conn = connFactory.getConn();
            conn.delete(dn);
        } catch (LDAPException e) {
            if (e.getLDAPResultCode() != LDAPException.NO_SUCH_OBJECT) {
                status = FAILED;
                CMS.debug("Failed to delete entry" + e.toString());
            }
       } catch (Exception e) {
            CMS.debug("Failed to delete entry" + e.toString()); 
       } finally { 
            try {
                if ((conn != null) && (connFactory!= null)) {
                    CMS.debug("Releasing ldap connection");
                    connFactory.returnConn(conn);
                }
            }
            catch (Exception e) {
                CMS.debug("Error releasing the ldap connection" + e.toString());
            }
       }
       return status;
    }

    private String modify_ldap(String dn, LDAPModification mod) {
        CMS.debug("UpdateDomainXML: modify_ldap: starting dn: " + dn);
        String status = SUCCESS;
        ILdapConnFactory connFactory = null;
        LDAPConnection conn = null;
        IConfigStore cs = CMS.getConfigStore();

        try {
            IConfigStore ldapConfig = cs.getSubStore("internaldb");
            connFactory = CMS.getLdapBoundConnFactory();
            connFactory.init(ldapConfig);
            conn = connFactory.getConn();
            conn.modify(dn, mod);
        } catch (LDAPException e) {
            if (e.getLDAPResultCode() != LDAPException.NO_SUCH_OBJECT) {
                status = FAILED;
                CMS.debug("Failed to modify entry" + e.toString());
            }
       } catch (Exception e) {
            CMS.debug("Failed to modify entry" + e.toString());
       } finally {
            try {
                if ((conn != null) && (connFactory!= null)) {
                    CMS.debug("Releasing ldap connection");
                    connFactory.returnConn(conn);
                }
            }
            catch (Exception e) {
                CMS.debug("Error releasing the ldap connection" + e.toString());
            }
       }
       return status;
    }


    private String add_to_ldap(LDAPEntry entry, String dn) {
        CMS.debug("UpdateDomainXML: add_to_ldap: starting");
        String status = SUCCESS;
        ILdapConnFactory connFactory = null;
        LDAPConnection conn = null;
        IConfigStore cs = CMS.getConfigStore();

        try {
            IConfigStore ldapConfig = cs.getSubStore("internaldb");
            connFactory = CMS.getLdapBoundConnFactory();
            connFactory.init(ldapConfig);
            conn = connFactory.getConn();
            conn.add(entry);
        } catch (LDAPException e) {
            if (e.getLDAPResultCode() == LDAPException.ENTRY_ALREADY_EXISTS) {
                CMS.debug("UpdateDomainXML: Entry already exists");
                try {
                    conn.delete(dn);
                    conn.add(entry);
                } catch (LDAPException ee) {
                    CMS.debug("UpdateDomainXML: Error when replacing existing entry "+ee.toString());
                    status = FAILED;
                }
            } else {
                CMS.debug("UpdateDomainXML: Failed to update ldap domain info. Exception: "+e.toString());
                status = FAILED;
            }
        } catch (Exception e) {
            CMS.debug("Failed to add entry" + e.toString());
        } finally {
            try {
                if ((conn != null) && (connFactory!= null)) {
                    CMS.debug("Releasing ldap connection");
                    connFactory.returnConn(conn);
                }
            }
            catch (Exception e) {
                CMS.debug("Error releasing the ldap connection" + e.toString());
            }
       }
       return status;
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
        CMS.debug("UpdateDomainXML: processing...");
        String status = SUCCESS;

        HttpServletRequest httpReq = cmsReq.getHttpReq();
        HttpServletResponse httpResp = cmsReq.getHttpResp();

        CMS.debug("UpdateDomainXML process: authentication starts");
        IAuthToken authToken = null;
        try {
            authToken = authenticate(cmsReq);
        } catch (Exception e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
            outputError(httpResp, AUTH_FAILURE, "Error: Not authenticated");
            return;
        }
        if (authToken == null) {
            CMS.debug("UpdateDomainXML process: authToken is null");
            outputError(httpResp, AUTH_FAILURE, "Error: not authenticated");
            return;
        }
        CMS.debug("UpdateDomainXML process: authentication done");

        AuthzToken authzToken = null;

        try {
            authzToken = authorize(mAclMethod, authToken, mAuthzResourceName, 
                "modify");
        } catch (EAuthzAccessDenied e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
            outputError(httpResp, AUTH_FAILURE, "Error: Not authorized");
            return;
        } catch (Exception e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
            outputError(httpResp,
                AUTH_FAILURE, 
                "Error: Encountered problem during authorization.");
            return;
        }
        if (authzToken == null) {
            CMS.debug("UpdateDomainXML process: authorization error");
            outputError(httpResp, AUTH_FAILURE, "Error: Not authorized");
            return;
        }

        String list = httpReq.getParameter("list");
        String type = httpReq.getParameter("type");
        String host = httpReq.getParameter("host");
        String name = httpReq.getParameter("name");
        String sport = httpReq.getParameter("sport");
        String agentsport = httpReq.getParameter("agentsport");
        String adminsport = httpReq.getParameter("adminsport");
        String eecaport = httpReq.getParameter("eeclientauthsport");
        String httpport = httpReq.getParameter("httpport");
        String domainmgr = httpReq.getParameter("dm");
        String clone = httpReq.getParameter("clone");
        String operation = httpReq.getParameter("operation");

        // ensure required parameters are present
        // especially important for DS syntax checking
        String missing = "";
        if ((host == null) || host.equals("")) {
            missing += " host ";
        } 
        if ((name == null) || name.equals("")) {
            missing += " name ";
        }
        if ((sport == null) || sport.equals("")) {
            missing += " sport ";
        }
        if ((clone == null) || clone.equals("")) {
            clone = "false";
        }

        if (! missing.equals("")) {
            CMS.debug("UpdateDomainXML process: required parameters:" + missing + "not provided in request");
            outputError(httpResp, "Error: required parameters: "  + missing + "not provided in request");
            return;
        }

        String basedn = null;
        String secstore = null;

        IConfigStore cs = CMS.getConfigStore();

        try {
            basedn = cs.getString("internaldb.basedn");
            secstore = cs.getString("securitydomain.store");
        }
        catch (Exception e)  {
            CMS.debug("Unable to determine security domain name or basedn. Please run the domaininfo migration script");
        }

        if ((basedn != null) && (secstore != null) && (secstore.equals("ldap"))) {
            // update in ldap

            LDAPEntry entry = null;
            ILdapConnFactory connFactory = null;
            LDAPConnection conn = null;
            String listName = type + "List";
            String cn = host + ":";

            if ((adminsport!= null) && (adminsport != "")) {
                cn += adminsport;
            } else {
                cn += sport;
            }

            String dn = "cn=" + cn + ",cn=" + listName + ",ou=Security Domain," + basedn;
            CMS.debug("UpdateDomainXML: updating LDAP entry: " + dn);

            LDAPAttributeSet attrs = null;
            attrs = new LDAPAttributeSet();
            attrs.add(new LDAPAttribute("objectclass", "top"));
            attrs.add(new LDAPAttribute("objectclass", "pkiSubsystem"));
            attrs.add(new LDAPAttribute("cn", cn));
            attrs.add(new LDAPAttribute("Host", host));
            attrs.add(new LDAPAttribute("SecurePort", sport));

            if ((agentsport != null) && (!agentsport.equals(""))) {
                attrs.add(new LDAPAttribute("SecureAgentPort", agentsport));
            }
            if ((adminsport != null) && (!adminsport.equals(""))) {
                attrs.add(new LDAPAttribute("SecureAdminPort", adminsport));
            }
            if ((httpport != null) && (!httpport.equals(""))) {
                attrs.add(new LDAPAttribute("UnSecurePort", httpport));
            }
            if ((eecaport != null) && (!eecaport.equals(""))) {
                attrs.add(new LDAPAttribute("SecureEEClientAuthPort", eecaport));
            }
            if ((domainmgr != null) && (!domainmgr.equals(""))) {
                attrs.add(new LDAPAttribute("DomainManager", domainmgr.toUpperCase()));
            }
            attrs.add(new LDAPAttribute("clone", clone.toUpperCase()));
            attrs.add(new LDAPAttribute("SubsystemName", name));
            entry = new LDAPEntry(dn, attrs);
 
            if ((operation !=  null) && (operation.equals("remove"))) {
                    status = remove_from_ldap(dn);
                    String adminUserDN = "uid=" + type + "-" + host + "-" + agentsport + ",ou=People," + basedn;
                    if (status.equals(SUCCESS)) {
                        // remove the client cert for this subsystem's admin
                        status = remove_from_ldap(adminUserDN);
                        if (status.equals(SUCCESS)) {
                            // remove this user from the subsystem group
                            dn = "cn=Subsystem Group, ou=groups," + basedn;
                            LDAPModification mod = new LDAPModification(LDAPModification.DELETE, 
                                new LDAPAttribute("uniqueMember", adminUserDN));
                            status = modify_ldap(dn, mod);
                        }
                    }
            } else {
                    status = add_to_ldap(entry, dn);
            }

        }
        else { 
            // update the domain.xml file
            String path = CMS.getConfigStore().getString("instanceRoot", "")
                    + "/conf/domain.xml";

            CMS.debug("UpdateDomainXML: got path=" + path);

            try {
                // using domain.xml file
                CMS.debug("UpdateDomainXML: Inserting new domain info");
                XMLObject parser = new XMLObject(new FileInputStream(path));
                Node n = parser.getContainer(list);
                int count =0;

                if ((operation != null) && (operation.equals("remove"))) {
                    // delete node
                    Document doc = parser.getDocument();
                    NodeList nodeList = doc.getElementsByTagName(type);
                    int len = nodeList.getLength();

                    for (int i = 0; i < len; i++) {
                        Node nn = (Node) nodeList.item(i);
                        Vector v_name = parser.getValuesFromContainer(nn, "SubsystemName");
                        Vector v_host = parser.getValuesFromContainer(nn, "Host");
                        Vector v_adminport = parser.getValuesFromContainer(nn, "SecureAdminPort");
                        if ((v_name.elementAt(0).equals(name)) && (v_host.elementAt(0).equals(host))
                            && (v_adminport.elementAt(0).equals(adminsport))) {
                                Node parent = nn.getParentNode();
                                Node remNode = parent.removeChild(nn);
                                count --;
                                break;
                        }
                    }
                } else {
                    // add node
                    Node parent = parser.createContainer(n, type);
                    parser.addItemToContainer(parent, "SubsystemName", name);
                    parser.addItemToContainer(parent, "Host", host);
                    parser.addItemToContainer(parent, "SecurePort", sport);
                    parser.addItemToContainer(parent, "SecureAgentPort", agentsport);
                    parser.addItemToContainer(parent, "SecureAdminPort", adminsport);
                    parser.addItemToContainer(parent, "SecureEEClientAuthPort", eecaport);
                    parser.addItemToContainer(parent, "UnSecurePort", httpport);
                    parser.addItemToContainer(parent, "DomainManager", domainmgr.toUpperCase());
                    parser.addItemToContainer(parent, "Clone", clone.toUpperCase());
                    count ++;
                }
                //update count 

                String countS = "";
                NodeList nlist = n.getChildNodes();
                Node countnode = null;
                for (int i=0; i<nlist.getLength(); i++) {
                    Element nn = (Element)nlist.item(i);
                    String tagname = nn.getTagName();
                    if (tagname.equals("SubsystemCount")) {
                        countnode = nn;
                        NodeList nlist1 = nn.getChildNodes();
                        Node nn1 = nlist1.item(0);
                        countS  = nn1.getNodeValue();
                        break;
                    }
                }

                CMS.debug("UpdateDomainXML process: SubsystemCount="+countS);
                try {
                        count += Integer.parseInt(countS);
                } catch (Exception ee) {
                }

                Node nn2 = n.removeChild(countnode);
                parser.addItemToContainer(n, "SubsystemCount", ""+count);

                // recreate domain.xml
                CMS.debug("UpdateDomainXML: Recreating domain.xml");
                byte[] b = parser.toByteArray();
                FileOutputStream fos = new FileOutputStream(path);
                fos.write(b);
                fos.close();
            } catch (Exception e) {
                CMS.debug("Failed to update domain.xml file" + e.toString());
                status = FAILED;
            }
        }
  
        try {
            // send success status back to the requestor
            CMS.debug("UpdateDomainXML: Sending response");
            XMLObject xmlObj = new XMLObject();
            Node root = xmlObj.createRoot("XMLResponse");

            xmlObj.addItemToContainer(root, "Status", status);
            byte[] cb = xmlObj.toByteArray();

            outputResult(httpResp, "application/xml", cb);
        } catch (Exception e) {
                CMS.debug("UpdateDomainXML: Failed to send the XML output" + e.toString());
        }
    }

    protected String securityDomainXMLtoLDAP(String xmltag) {
        if (xmltag.equals("Host")) return "host";
        else return xmltag;
    }


    protected void setDefaultTemplates(ServletConfig sc) {}

    protected void renderTemplate(
            CMSRequest cmsReq, String templateName, ICMSTemplateFiller filler)
        throws IOException {// do nothing
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
