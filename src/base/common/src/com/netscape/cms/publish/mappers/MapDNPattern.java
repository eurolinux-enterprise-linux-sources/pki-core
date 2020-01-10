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


import com.netscape.certsrv.logging.*;
import java.security.*;
import java.security.cert.*;
import netscape.security.x509.*;
import netscape.security.util.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.apps.*;
import netscape.ldap.*;
import java.io.*;
import java.util.*;
import com.netscape.certsrv.request.IRequest;
import com.netscape.certsrv.ldap.*;
import com.netscape.certsrv.publish.*;


/**
 * class for parsing a DN pattern used to construct a ldap dn from 
 * request attributes and cert subject name.<p>
 * 
 * dnpattern is a string representing a ldap dn pattern to formulate from 
 * the certificate subject name attributes and request attributes . 
 * If empty or not set, the certificate subject name 
 * will be used as the ldap dn. <p>
 * 
 * The syntax is 
 * <pre>
 *		dnPattern := rdnPattern *[ "," rdnPattern ]
 *		rdnPattern := avaPattern *[ "+" avaPattern ]
 * 		avaPattern := name "=" value | 
 *				      name "=" "$subj" "." attrName [ "." attrNumber ] | 
 *				      name "=" "$req" "." attrName [ "." attrNumber ] | 
 *	    		 	  "$rdn" "." number
 * </pre>
 * <pre>
 * Example1: <i>cn=Certificate Manager,ou=people,o=mcom.com</i>
 * cert subject name: dn:  CN=Certificate Manager, OU=people, O=mcom.com
 * request attributes: uid: cmanager 
 * <p>
 * The dn formulated will be : <br>
 *     CN=Certificate Manager, OU=people, O=mcom.com
 * <p>
 * note: Subordinate ca enrollment will use ca mapper. Use predicate
 * to distinguish the ca itself and the subordinates.
 *
 * Example2: <i>UID=$req.HTTP_PARAMS.uid, OU=$subj.ou, O=people, , O=mcom.com</i>
 * cert subject name: dn:  UID=jjames, OU=IS, O=people, , O=mcom.com
 * request attributes: uid: cmanager 
 * <p>
 * The dn formulated will be : <br>
 *     UID=jjames, OU=IS, OU=people, O=mcom.com
 * <p>	
 *     UID = the 'uid' attribute value in the request. <br>
 *     OU = the 'ou' value in the cert subject name.  <br>
 *     O = the string people, mcom.com. <br>
 * <p>
 * </pre>
 * If an request attribute or subject DN component does not exist,
 * the attribute is skipped. There is potential risk that a wrong dn
 * will be mapped into.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class MapDNPattern {

    /* the list of request attriubutes to retrieve*/
    protected String[] mReqAttrs = null;

    /* the list of cert attriubutes to retrieve*/
    protected String[] mCertAttrs = null;

    /* rdn patterns */
    protected MapRDNPattern[] mRDNPatterns = null;

    /* original pattern string */
    protected String mPatternString = null;

    protected String mTestDN = null;

    /** 
     * Construct a DN pattern by parsing a pattern string.
     * @param pattern the DN pattern
     * @exception EBaseException If parsing error occurs. 
     */
    public MapDNPattern(String pattern)
        throws ELdapException {
        if (pattern == null || pattern.equals("")) {
            CMS.debug(
                "MapDNPattern: null pattern");			
        } else {
            mPatternString = pattern;
            PushbackReader in = new PushbackReader(new StringReader(pattern));

            parse(in);
        }
    }

    public MapDNPattern(PushbackReader in) 
        throws ELdapException {
        parse(in);
    }

    private void parse(PushbackReader in)
        throws ELdapException {
        Vector rdnPatterns = new Vector();
        MapRDNPattern rdnPattern = null;
        int lastChar = -1;

        do {
            rdnPattern = new MapRDNPattern(in);
            rdnPatterns.addElement(rdnPattern);
            try {
                lastChar = in.read();
            } catch (IOException e) {
                throw new ELdapException(
                        CMS.getUserMessage("CMS_LDAP_INTERNAL_ERROR", e.toString()));
            }
        }
        while (lastChar == ',');

        mRDNPatterns = new MapRDNPattern[rdnPatterns.size()];
        rdnPatterns.copyInto(mRDNPatterns);

        Vector reqAttrs = new Vector();

        for (int i = 0; i < mRDNPatterns.length; i++) {
            String[] rdnAttrs = mRDNPatterns[i].getReqAttrs();

            if (rdnAttrs != null && rdnAttrs.length > 0) 
                for (int j = 0; j < rdnAttrs.length; j++) 
                    reqAttrs.addElement(rdnAttrs[j]);
        }
        mReqAttrs = new String[reqAttrs.size()];
        reqAttrs.copyInto(mReqAttrs);

        Vector certAttrs = new Vector();

        for (int i = 0; i < mRDNPatterns.length; i++) {
            String[] rdnAttrs = mRDNPatterns[i].getCertAttrs();

            if (rdnAttrs != null && rdnAttrs.length > 0) 
                for (int j = 0; j < rdnAttrs.length; j++) 
                    certAttrs.addElement(rdnAttrs[j]);
        }
        mCertAttrs = new String[certAttrs.size()];
        certAttrs.copyInto(mCertAttrs);
    }

    /**
     * Form a Ldap v3 DN string from a request and a cert subject name.
     * @param req the request for (un)publish
     * @param subject the subjectDN of the certificate
     * @return Ldap v3 DN string to use for base ldap search. 
     */
    public String formDN(IRequest req, X500Name subject, CertificateExtensions ext)
        throws ELdapException {
        StringBuffer formedDN = new StringBuffer();

        for (int i = 0; i < mRDNPatterns.length; i++) {
            if (mTestDN != null)
                mRDNPatterns[i].mTestDN = mTestDN;
            String rdn = mRDNPatterns[i].formRDN(req, subject, ext);

            if (rdn != null && rdn.length() != 0) {
                    if (formedDN.length() != 0) 
                        formedDN.append(",");
                    formedDN.append(rdn);
            } else {
		throw new ELdapException("pattern not matched");
            }
        }
        return formedDN.toString();
    }

    public String[] getReqAttrs() {
        return (String[]) mReqAttrs.clone();
    }

    public String[] getCertAttrs() {
        return (String[]) mCertAttrs.clone();
    }
}

