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
package com.netscape.cmscore.security;


import netscape.security.x509.*;
import netscape.security.util.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.PrivateKey;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.cert.*;
import java.security.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.security.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.apps.*;
import org.mozilla.jss.crypto.Signature;
import org.mozilla.jss.crypto.TokenException;
import org.mozilla.jss.crypto.*;
import org.mozilla.jss.CryptoManager.*;
import org.mozilla.jss.*;


/**
 * OCSP signing certificate.
 * 
 * @author Christine Ho
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class OCSPSigningCert extends CertificateInfo {
    public static final String SUBJECT_NAME = 
        "CN=Certificate Authority, O=Netscape Communications, C=US";

    public OCSPSigningCert(KeyCertData properties) {
        this(properties, null);
    }

    public OCSPSigningCert(KeyCertData properties, KeyPair pair) {
        super(properties, pair);
        /* included in console UI
        try {
            if (mProperties.get(Constants.PR_OCSP_SIGNING) == null) {
                mProperties.put(Constants.PR_OCSP_SIGNING, Constants.TRUE);
            }
            if (mProperties.get(Constants.PR_OCSP_NOCHECK) == null) {
                mProperties.put(Constants.PR_OCSP_NOCHECK, Constants.TRUE);
            }
        } catch (Exception e) {
            mProperties.put(Constants.PR_OCSP_SIGNING, Constants.TRUE);
            mProperties.put(Constants.PR_OCSP_NOCHECK, Constants.TRUE);
        }
        */
    }

    public String getSubjectName() {
        return (String) mProperties.get(Constants.PR_SUBJECT_NAME);
    }

    // get PQG params from the configuration file
    public static PQGParams getPQGParams() throws EBaseException, IOException,
            PQGParamGenException {

        byte[] p = mConfig.getByteArray("ca.dsaP", null);
        byte[] q = mConfig.getByteArray("ca.dsaQ", null);
        byte[] g = mConfig.getByteArray("ca.dsaG", null);
        byte[] seed = mConfig.getByteArray("ca.dsaSeed", null);
        byte[] H = mConfig.getByteArray("ca.dsaH", null);
        int counter = mConfig.getInteger("ca.dsaCounter", 0);

        if (p != null && q != null && g != null) {
            BigInteger P = new BigInteger(p);
            BigInteger Q = new BigInteger(q);
            BigInteger G = new BigInteger(g);
            BigInteger pqgSeed = new BigInteger(seed); 
            BigInteger pqgH = new BigInteger(H);

            return new PQGParams(P, Q, G, pqgSeed, counter, pqgH);
        }
        return null;
    }

    public void updateConfig(IConfigStore cmsFileTmp) throws EBaseException {
        String tokenname = (String) mProperties.get(Constants.PR_TOKEN_NAME);
        String nickname = getNickname();

        cmsFileTmp.putString("ca.signing.tokenname", tokenname);
        String keyType = (String) mProperties.get(Constants.PR_KEY_TYPE);
        String alg;

        if (keyType.equals("RSA"))
            alg = "SHA1withRSA";
        else if (keyType.equals("DSA"))
            alg = "SHA1withDSA";
        else
            throw new EBaseException(CMS.getUserMessage("CMS_BASE_ALG_NOT_SUPPORTED", keyType));

        cmsFileTmp.putString("ca.signing.defaultSigningAlgorithm", alg);
        if (tokenname.equals(Constants.PR_INTERNAL_TOKEN_NAME))
            cmsFileTmp.putString("ca.signing.cacertnickname", nickname);
        else
            cmsFileTmp.putString("ca.signing.cacertnickname",
                tokenname + ":" + nickname);
        cmsFileTmp.commit(false);
    }

    public String getNickname() {
        String name = (String) mProperties.get(Constants.PR_NICKNAME);
        String instanceName = (String) mProperties.get(ConfigConstants.PR_CERT_INSTANCE_NAME);

        if (name != null)
            return name;
        return "ocspSigningCert " + instanceName;
    }

    public String getKeyAlgorithm() {
        return (String) mProperties.get(Constants.PR_KEY_TYPE);
    }

    protected KeyUsageExtension getKeyUsageExtension() throws IOException {
        KeyUsageExtension extension = new KeyUsageExtension();

        extension.set(KeyUsageExtension.DIGITAL_SIGNATURE, new Boolean(true));
        extension.set(KeyUsageExtension.NON_REPUDIATION, new Boolean(true));
        extension.set(KeyUsageExtension.KEY_CERTSIGN, new Boolean(true));
        extension.set(KeyUsageExtension.CRL_SIGN, new Boolean(true));
        return extension;
    }
}

