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
 * Maps a X509 certificate to a LDAP entry using AVAs in the certificate's 
 * subject name to form the ldap search dn and filter.
 * Takes a optional root search dn.
 * The DN comps are used to form a LDAP entry to begin a subtree search.
 * The filter comps are used to form a search filter for the subtree.
 * If none of the DN comps matched, baseDN is used for the subtree.
 * If the baseDN is null and none of the DN comps matched, it is an error.
 * If none of the DN comps and filter comps matched, it is an error.
 * If just the filter comps is null, a base search is performed.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class LdapCertCompsMap 
    extends LdapDNCompsMap implements ILdapMapper {
    ILogger mLogger = CMS.getLogger();

    public LdapCertCompsMap() {
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
    public LdapCertCompsMap(String baseDN, ObjectIdentifier[] dnComps,
        ObjectIdentifier[] filterComps) {
        init(baseDN, dnComps, filterComps);
    }

    public String getImplName() {
        return "LdapCertCompsMap";
    }

    public String getDescription() {
        return "LdapCertCompsMap";
    }

    public Vector getDefaultParams() {
        Vector v = super.getDefaultParams();

        return v;
    }

    public Vector getInstanceParams() {
        Vector v = super.getInstanceParams();

        return v;
    }

    /**
     * constructor using non-standard certificate attribute.
     */
    public LdapCertCompsMap(String certAttr, String baseDN, 
        ObjectIdentifier[] dnComps,
        ObjectIdentifier[] filterComps) {
        super(certAttr, baseDN, dnComps, filterComps);
    }

    protected void init(String baseDN, ObjectIdentifier[] dnComps,
        ObjectIdentifier[] filterComps) {
        super.init(baseDN, dnComps, filterComps);
    }

    /**
     * Maps a certificate to LDAP entry.
     * Uses DN components and filter components to form a DN and 
     * filter for a LDAP search.
     * If the formed DN is null the baseDN will be used.
     * If the formed DN is null and baseDN is null an error is thrown.
     * If the filter is null a base search is performed.
     * If both are null an error is thrown.
     * 
     * @param conn - the LDAP connection.
     * @param obj - the X509Certificate.
     */
    public String
    map(LDAPConnection conn, Object obj)
        throws ELdapException {
        if (conn == null)
            return null;
        try {
            X509Certificate cert = (X509Certificate) obj;
            String result = null;
            // form dn and filter for search.
            X500Name subjectDN = 
                (X500Name) ((X509Certificate) cert).getSubjectDN();

            CMS.debug("LdapCertCompsMap: " + subjectDN.toString());

            byte[] certbytes = cert.getEncoded();

            result = super.map(conn, subjectDN, certbytes);
            return result;
        } catch (CertificateEncodingException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("PUBLISH_CANT_DECODE_CERT", e.toString()));
            throw new ELdapException(
                    CMS.getUserMessage("CMS_LDAP_GET_DER_ENCODED_CERT_FAILED", e.toString()));
        } catch (ClassCastException e) {
            try {
                X509CRLImpl crl = (X509CRLImpl) obj;
                String result = null;
                X500Name issuerDN = 
                    (X500Name) ((X509CRLImpl) crl).getIssuerDN();

                CMS.debug("LdapCertCompsMap: " + issuerDN.toString());

                byte[] crlbytes = crl.getEncoded();

                result = super.map(conn, issuerDN, crlbytes);
                return result;
            } catch (CRLException ex) {
                log(ILogger.LL_FAILURE, CMS.getLogMessage("PUBLISH_CANT_DECODE_CRL", ex.toString()));
                throw new ELdapException(CMS.getUserMessage("CMS_LDAP_GET_DER_ENCODED_CRL_FAILED", ex.toString()));
            } catch (ClassCastException ex) {
                log(ILogger.LL_FAILURE, CMS.getLogMessage("PUBLISH_NOT_SUPPORTED_OBJECT"));
                return null;
            }
        }
    }

    public String map(LDAPConnection conn, IRequest req, Object obj)
        throws ELdapException {
        return map(conn, obj);
    }

    private void log(int level, String msg) {
        mLogger.log(ILogger.EV_SYSTEM, ILogger.S_LDAP, level,
            "LdapCertCompsMap: " + msg);
    }

}

