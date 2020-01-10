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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.*;
import java.util.*;

import java.security.cert.CertificateException;
import netscape.security.x509.*;
import netscape.security.util.*;


/**
 * This represents the subject information access extension
 * as defined in RFC3280.
 *
 * @author thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class SubjectInfoAccessExtension extends Extension implements CertAttrSet {
    public static final String NAME = "SubjectInfoAccessExtension";

    public static final int OID_OCSP[] = { 1, 3, 6, 1, 5, 5, 7, 48, 1 };
    public static final ObjectIdentifier METHOD_OCSP = new 
        ObjectIdentifier(OID_OCSP);

    public static final int OID_CA_ISSUERS[] = { 1, 3, 6, 1, 5, 5, 7, 48, 2 };
    public static final ObjectIdentifier METHOD_CA_ISSUERS = new 
        ObjectIdentifier(OID_CA_ISSUERS);

    public static final int OID[] = { 1, 3, 6, 1, 5, 5, 7, 1, 11 };
    public static final ObjectIdentifier ID = new ObjectIdentifier(OID);

    private Vector mDesc = new Vector();

    /**
     * Create the extension from the passed DER encoded value of the same.
     *
     * @param critical true if the extension is to be treated as critical.
     * @param value Array of DER encoded bytes of the actual value.
     * @exception IOException on error.
     */
    public SubjectInfoAccessExtension(boolean critical) {
        this.extensionId = ID;
        this.critical = critical;
        this.extensionValue = null; // build this when encodeThis() is called
    }

    public SubjectInfoAccessExtension(Boolean critical, Object value) 
        throws IOException {
        this.extensionId = ID;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) ((byte[]) value).clone();
        decodeThis();
    }

    /**
     * Sets extension attribute.
     */
    public void set(String name, Object obj) throws CertificateException {
        // NOT USED
    }

    /**
     * Retrieves extension attribute.
     */
    public Object get(String name) throws CertificateException {
        // NOT USED
        return null;
    }

    /**
     * Deletes attribute.
     */
    public void delete(String name) throws CertificateException {
        // NOT USED
    }

    /**
     * Decodes this extension.
     */
    public void decode(InputStream in) throws IOException {
        // NOT USED
    }

    /**
     * Return an enumeration of names of attributes existing within this
     * attribute.
     */
    public Enumeration getElements() {
        // NOT USED
        return null;
    }

    /**
     * Return the name of this attribute.
     */
    public String getName() {
        return NAME;
    }

    /**
     * Adds Access Description.
     */
    public void addAccessDescription(
        ObjectIdentifier method, 
        GeneralName gn) {
	clearValue();
        mDesc.addElement(new AccessDescription(method, gn));
    }

    public AccessDescription getAccessDescription(int pos) {
        return (AccessDescription) mDesc.elementAt(pos);
    }

    /**
     * Returns the number of access description.
     */
    public int numberOfAccessDescription() {
        return mDesc.size();
    }

    private void decodeThis() throws IOException {
        DerValue val = new DerValue(this.extensionValue);

        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding of AuthInfoAccess extension");
        }
        while (val.data.available() != 0) {
            DerValue seq = val.data.getDerValue();
            ObjectIdentifier method = seq.data.getDerValue().getOID();
            GeneralName gn = new GeneralName(seq.data.getDerValue());

            addAccessDescription(method, gn);
        }
    }

    private void encodeThis() throws IOException {	
        DerOutputStream seq = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();

        for (int i = 0; i < mDesc.size(); i++) {
            DerOutputStream tmp0 = new DerOutputStream();
            AccessDescription ad = (AccessDescription) mDesc.elementAt(i);

            tmp0.putOID(ad.getMethod());
            ad.getLocation().encode(tmp0);
            tmp.write(DerValue.tag_Sequence, tmp0);
        }
        seq.write(DerValue.tag_Sequence, tmp);
        this.extensionValue = seq.toByteArray();
    }
 
    /**
     * Write the extension to the DerOutputStream.
     *
     * @param out the DerOutputStream to write the extension to.
     * @exception IOException on encoding errors.
     */
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();

        if (this.extensionValue == null) {
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    /**
     * Returns a printable representation of the AuthInfoAccess.
     */
    public String toString() {
        String s = super.toString() + "AuthInfoAccess [\n";

        for (int i = 0; i < mDesc.size(); i++) {
            AccessDescription ad = (AccessDescription) mDesc.elementAt(i);

            s += "(" + i + ")";
            s += " ";
            s += ad.getMethod().toString() + " " + ad.getLocation().toString();
        }
        return (s + "]\n");
    }

    public static void main(String[] argv) {
        AuthInfoAccessExtension aia = new AuthInfoAccessExtension(false);
        GeneralName ocspName = new GeneralName(new
                URIName("http://ocsp.netscape.com"));

        aia.addAccessDescription(METHOD_OCSP, ocspName);
        GeneralName caIssuersName = new GeneralName(new
                URIName("http://ocsp.netscape.com"));

        aia.addAccessDescription(METHOD_CA_ISSUERS, caIssuersName);	
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            aia.encode(os);

            System.out.println(com.netscape.osutil.OSUtil.BtoA(os.toByteArray()));
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        try {
            // test serialization
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(aia);

            ByteArrayInputStream bis = new ByteArrayInputStream(
                    bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            AuthInfoAccessExtension clone = (AuthInfoAccessExtension)
                ois.readObject();

            System.out.println(clone);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
