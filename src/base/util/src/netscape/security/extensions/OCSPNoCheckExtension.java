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
package netscape.security.extensions;


import netscape.security.x509.*;
import netscape.security.util.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.io.*;


/**
 * This represents the OCSPNoCheck extension.
 */
public class OCSPNoCheckExtension extends Extension implements CertAttrSet {

    public static final String OID = "1.3.6.1.5.5.7.48.1.5";
    public static final String NAME = "OCSPNoCheckExtension";

    private byte mCached[] = null;

    static {
        try {
            OIDMap.addAttribute(OCSPNoCheckExtension.class.getName(),
                OID, NAME);
        } catch (CertificateException e) {
        }
    }

    public OCSPNoCheckExtension() {
        this(Boolean.FALSE);
    }

    public OCSPNoCheckExtension(Boolean crit) {
        try {
            extensionId = ObjectIdentifier.getObjectIdentifier(OCSPNoCheckExtension.OID);
        } catch (IOException e) {
            // never here
        }
        critical = crit.booleanValue();
        DerOutputStream tmpD = new DerOutputStream();

        try {
            tmpD.putNull();
        } catch (IOException ex) {
        }
        extensionValue = tmpD.toByteArray();
    }

    public OCSPNoCheckExtension(Boolean crit, Object byteVal) {
        try {
            extensionId = ObjectIdentifier.getObjectIdentifier(OCSPNoCheckExtension.OID);
        } catch (IOException e) {
            // never here
        }
        critical = crit.booleanValue();
        extensionValue = (byte[]) ((byte[]) byteVal).clone();
    }
    
    public void setCritical(boolean newValue) {
        if (critical != newValue) {
            critical = newValue;
            mCached = null;
        }
    }

    public void encode(DerOutputStream out) throws IOException {
        if (mCached == null) {
            super.encode(out);
            mCached = out.toByteArray();
        }
    }
    
    private void encodeThis(DerOutputStream out) throws IOException {
        if (mCached == null) {
            super.encode(out);
            mCached = out.toByteArray();
        }
    }
    
    public String toString() {
        String presentation = "oid=" + OID + " ";

        if (critical) {
            presentation += "critical=true";
        }
        if (extensionValue != null) {
            String extByteValue = new String(" val=");

            for (int i = 0; i < extensionValue.length; i++) {
                extByteValue += (extensionValue[i] + " ");
            }
            presentation += extByteValue;    
        }
        return presentation;
    }

    public void decode(InputStream in) 
        throws CertificateException, IOException {
        // NOT USED
    }

    public void encode(OutputStream out) 
        throws CertificateException, IOException {
        if (mCached == null) {
            DerOutputStream temp = new DerOutputStream();

            encode(temp);
        }
        out.write(mCached);
    }

    public void set(String name, Object obj) 
        throws CertificateException, IOException {
        // NOT USED
    }

    public Object get(String name) throws CertificateException, IOException {
        // NOT USED
        return null;
    }

    public Enumeration getElements() {
        // NOT USED
        return null;
    }

    public String getName() {
        return NAME;   
    }

    public void delete(String name) 
        throws CertificateException, IOException {
        // NOT USED
    }
}
