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
package com.netscape.cmscore.dbs;


import java.util.*;
import netscape.ldap.*;
import java.security.*;
import java.security.cert.*;
import netscape.security.x509.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.*;
import com.netscape.cmscore.cert.*;
 

/**
 * A class represents an attribute mapper that maps
 * a public key data into LDAP attribute and
 * vice versa.
 * <P>
 *
 * @author thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $ 
 */
public class PublicKeyMapper implements IDBAttrMapper {

    private String mLdapName = null;
    private Vector v = new Vector();

    private ILogger mLogger = CMS.getLogger();

    /**
     * Constructs a byte array mapper.
     */
    public PublicKeyMapper(String ldapName) {
        mLdapName = ldapName;
        v.addElement(mLdapName);
    }

    /**
     * Lists a list of supported ldap attribute names.
     */
    public Enumeration getSupportedLDAPAttributeNames() {
        return v.elements();
    }

    /**
     * Maps object to ldap attribute set.
     */
    public void mapObjectToLDAPAttributeSet(IDBObj parent, 
        String name, Object obj, LDAPAttributeSet attrs) 
        throws EBaseException {
        attrs.add(new LDAPAttribute(mLdapName, (byte[]) obj));
    }

    /**
     * Maps LDAP attributes into object, and put the object
     * into 'parent'.
     */
    public void mapLDAPAttributeSetToObject(LDAPAttributeSet attrs, 
        String name, IDBObj parent) throws EBaseException {
        LDAPAttribute attr = attrs.getAttribute(mLdapName);

        if (attr == null) {
            return;
        }
        parent.set(name, (byte[]) attr.getByteValues().nextElement());
    }

    /**
     * Maps search filters into LDAP search filter. It knows
     * how to extract public key from the certificate.
     */
    public String mapSearchFilter(String name, String op, 
        String value) throws EBaseException {
        int i = value.indexOf("#");

        if (i != -1) {
            String tag = value.substring(0, i);
            String val = value.substring(i + 1);

            try {
                if (val.startsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                X509Certificate cert = CertUtils.mapCert(val);
                PublicKey key = cert.getPublicKey();
                byte pub[] = key.getEncoded();

                return mLdapName + op + escapeBinaryData(pub);
            } catch (Exception e) {

                /*LogDoc
                 *
                 * @phase Maps search filters into LDAP search filter
                 * @message PublicKeyMapper: <exception thrown>
                 */
                mLogger.log(ILogger.EV_SYSTEM, ILogger.S_DB, ILogger.LL_FAILURE, 
                    CMS.getLogMessage("CMSCORE_DBS_PUBLICKEY_MAPPER_ERROR",
                        e.toString()));
            }
        }
        return mLdapName + op + value;
    }

    private String normalize(String s) {
        String val = "";

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                continue;
            } else if (s.charAt(i) == '\r') {
                continue;
            } else if (s.charAt(i) == '"') {
                continue;
            }
            val += s.charAt(i);
        }
        return val;
    }

    public static String escapeBinaryData(byte data[]) {
        String result = "";

        for (int i = 0; i < data.length; i++) {
            int v = 0xff & data[i];

            result = result + "\\" + (v < 16 ? "0" : "") +
                    Integer.toHexString(v);
        }
        return result;
    }
}
