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
package com.netscape.cms.profile.output;


import java.security.cert.*;
import java.io.*;
import java.util.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.profile.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.ca.*;

import netscape.security.x509.*;
import netscape.security.util.*;
import netscape.security.pkcs.*;

import org.mozilla.jss.asn1.*;
import org.mozilla.jss.pkix.crmf.*;
import org.mozilla.jss.pkix.cmmf.*;
import org.mozilla.jss.pkix.primitive.*;

import com.netscape.cms.profile.common.*;


/**
 * This class implements the output plugin that outputs
 * PKCS7 for the issued certificate.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class PKCS7Output extends EnrollOutput implements IProfileOutput { 

    public static final String VAL_PRETTY_CERT = "pretty_cert";
    public static final String VAL_PKCS7 = "pkcs7";

    public PKCS7Output() {
        addValueName(VAL_PRETTY_CERT);
        addValueName(VAL_PKCS7);
    }

    /**
     * Initializes this default policy.
     */
    public void init(IProfile profile, IConfigStore config)
        throws EProfileException {
        super.init(profile, config);
    }

    /**
     * Retrieves the localizable name of this policy.
     */
    public String getName(Locale locale) {
        return CMS.getUserMessage(locale, "CMS_PROFILE_OUTPUT_CERT_NAME");
    }

    /**
     * Retrieves the localizable description of this policy.
     */
    public String getText(Locale locale) {
        return CMS.getUserMessage(locale, "CMS_PROFILE_OUTPUT_CERT_TEXT");
    }

    /**
     * Populates the request with this policy default.
     */
    public void populate(IProfileContext ctx, IRequest request)
        throws EProfileException {
    }

    /**
     * Retrieves the descriptor of the given value
     * parameter by name.
     */
    public IDescriptor getValueDescriptor(Locale locale, String name) {
        if (name.equals(VAL_PRETTY_CERT)) {
            return new Descriptor(IDescriptor.PRETTY_PRINT, null,
                    null,
                    CMS.getUserMessage(locale, 
                        "CMS_PROFILE_OUTPUT_CERT_PP"));
        } else if (name.equals(VAL_PKCS7)) {
            return new Descriptor(IDescriptor.PRETTY_PRINT, null,
                    null,
                    CMS.getUserMessage(locale, 
                        "CMS_PROFILE_OUTPUT_PKCS7_B64"));
        }
        return null;
    }

    public String getValue(String name, Locale locale, IRequest request)
        throws EProfileException {
        if (name.equals(VAL_PRETTY_CERT)) {
            X509CertImpl cert = request.getExtDataInCert(
                    EnrollProfile.REQUEST_ISSUED_CERT);
            if (cert == null)
                  return null;
            ICertPrettyPrint prettyCert = CMS.getCertPrettyPrint(cert); 

            return prettyCert.toString(locale);
        } else if (name.equals(VAL_PKCS7)) {

            try {
              X509CertImpl cert = request.getExtDataInCert(
                    EnrollProfile.REQUEST_ISSUED_CERT);
              if (cert == null)
                  return null;
            
              ICertificateAuthority ca = (ICertificateAuthority)
                   CMS.getSubsystem("ca");
              CertificateChain cachain = ca.getCACertChain(); 
              X509Certificate[] cacerts = cachain.getChain();

              X509CertImpl[] userChain = new X509CertImpl[cacerts.length + 1]; 
              int m = 1, n = 0;

              for (; n < cacerts.length; m++, n++) {
                userChain[m] = (X509CertImpl) cacerts[n];
              }

              userChain[0] = cert;
              PKCS7 p7 = new PKCS7(new AlgorithmId[0], 
                  new ContentInfo(new byte[0]), 
                  userChain, 
                  new SignerInfo[0]); 
              ByteArrayOutputStream bos = new ByteArrayOutputStream();

              p7.encodeSignedData(bos); 
              byte[] p7Bytes = bos.toByteArray(); 
              String p7Str = CMS.BtoA(p7Bytes);

              return p7Str;
            } catch (Exception e) {
              return "";
            }
        } else {
            return null;
        }
    }

}
