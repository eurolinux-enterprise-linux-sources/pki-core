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
package com.netscape.cms.publish.mappers;


import netscape.ldap.*;
import java.io.*;
import java.util.*;
import java.security.*;
import java.security.cert.*;
import netscape.security.x509.*;
import netscape.security.util.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.request.IRequest;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.ldap.*;
import com.netscape.certsrv.publish.*;


/** 
 * Default crl mapper. 
 * maps the crl to a ldap entry by using components in the issuer name
 * to find the CA's entry.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class LdapCrlIssuerCompsMap 
    extends LdapDNCompsMap implements ILdapMapper {
    ILogger mLogger = CMS.getLogger();

    public LdapCrlIssuerCompsMap() {
        // need to support baseDN, dnComps, and filterComps
        // via configuration
    }

    /** 
     * Constructor.
     *
     * The DN comps are used to form a LDAP entry to begin a subtree search.
     * The filter comps are used to form a search filter for the subtree.
     * If none of the DN comps matched, baseDN is used for the subtree.
     * If the baseDN is null and none of the DN comps matched, it is an error.
     * If none of the DN comps and filter comps matched, it is an error.
     * If just the filter comps is null, a base search is performed.
     * 
     * @param baseDN The base DN. 
     * @param dnComps Components to form the LDAP base dn for search.
     * @param filterComps Components to form the LDAP search filter.
     */
    public LdapCrlIssuerCompsMap(String baseDN, ObjectIdentifier[] dnComps,
        ObjectIdentifier[] filterComps) {
        init(baseDN, dnComps, filterComps);
    }

    /**
     * constructor using non-standard certificate attribute.
     */
    public LdapCrlIssuerCompsMap(String crlAttr, String baseDN, 
        ObjectIdentifier[] dnComps,
        ObjectIdentifier[] filterComps) {
        super(crlAttr, baseDN, dnComps, filterComps);
    }

    public String getImplName() {
        return "LdapCrlIssuerCompsMap";
    }

    public String getDescription() {
        return "LdapCrlIssuerCompsMap";
    }

    public Vector getDefaultParams() {
        Vector v = super.getDefaultParams();

        //v.addElement("crlAttr=" + LdapCrlPublisher.LDAP_CRL_ATTR);
        return v;
    }

    public Vector getInstanceParams() {
        Vector v = super.getInstanceParams();

        return v;
    }

    protected void init(String baseDN, ObjectIdentifier[] dnComps,
        ObjectIdentifier[] filterComps) {
        //mLdapAttr = LdapCrlPublisher.LDAP_CRL_ATTR;
        super.init(baseDN, dnComps, filterComps);
    }

    /**
     * Maps a crl to LDAP entry.
     * Uses issuer DN components and filter components to form a DN and 
     * filter for a LDAP search.
     * If the formed DN is null the baseDN will be used.
     * If the formed DN is null and baseDN is null an error is thrown.
     * If the filter is null a base search is performed.
     * If both are null an error is thrown.
     * 
     * @param conn - the LDAP connection.
     * @param obj - the X509Certificate.
     * @return the result. LdapCertMapResult is also used for CRL.
     */ 
    public String
    map(LDAPConnection conn, Object obj)
        throws ELdapException {
        if (conn == null)
            return null;
        X509CRLImpl crl = (X509CRLImpl) obj;

        try {
            String result = null;
            X500Name issuerDN = 
                (X500Name) ((X509CRLImpl) crl).getIssuerDN();

            CMS.debug("LdapCrlIssuerCompsMap: " + issuerDN.toString());

            byte[] crlbytes = crl.getEncoded();

            result = super.map(conn, issuerDN, crlbytes);
            return result;
        } catch (CRLException e) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("PUBLISH_CANT_DECODE_CRL", e.toString()));
            throw new ELdapException(CMS.getUserMessage("CMS_LDAP_GET_DER_ENCODED_CRL_FAILED", e.toString()));
        }
    }

    public String map(LDAPConnection conn, IRequest req, Object obj)
        throws ELdapException {
        return map(conn, obj);
    }

    /**
     * overrides super's log().
     */
    private void log(int level, String msg) {
        mLogger.log(ILogger.EV_SYSTEM, ILogger.S_LDAP, level,
            "LdapCrlCompsMap: " + msg);
    }

}

