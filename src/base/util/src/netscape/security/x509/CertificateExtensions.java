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
package netscape.security.x509;

import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import netscape.security.util.*;

/**
 * This class defines the Extensions attribute for the Certificate.
 *
 * @author Amit Kapoor
 * @author Hemma Prafullchandra
 * @version 1.11
 * @see CertAttrSet
 */
public class CertificateExtensions extends Vector
implements CertAttrSet, Serializable {
    /**
     * Identifier for this attribute, to be used with the
     * get, set, delete methods of Certificate, x509 type.
     */  
    public static final String IDENT = "x509.info.extensions";
    /**
     * name
     */
    public static final String NAME = "extensions";

    private Hashtable map;

    // Parse the encoded extension
    public void parseExtension(Extension ext) throws IOException {
        try {
            Class extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass == null) {   // Unsupported extension
                if (ext.isCritical()) {
                    throw new IOException("Unsupported CRITICAL extension: "
                            + ext.getExtensionId());
                } else {
                    map.put(ext.getExtensionId().toString(), ext);
                    addElement(ext);
                    return;
                }
            }
            Class[] params = {Boolean.class, Object.class};
            Constructor cons = extClass.getConstructor(params);

            byte[] extData = ext.getExtensionValue();
            int extLen = extData.length;
            Object value = Array.newInstance(byte.class, extLen);

            for (int i = 0; i < extLen; i++) {
                Array.setByte(value, i, extData[i]);
            }
            Object[] passed = new Object[]{new Boolean(ext.isCritical()),
                    value};
            CertAttrSet certExt = (CertAttrSet) cons.newInstance(passed);
            if (certExt != null && certExt.getName() != null) {
                map.put(certExt.getName(), certExt);
                addElement(certExt);
            }
        } catch (NoSuchMethodException nosuch) {
            throw new IOException(nosuch.toString());
        } catch (InvocationTargetException invk) {
            throw new IOException(invk.getTargetException().toString());
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }

    /**
     * Default constructor for the certificate attribute.
     */
    public CertificateExtensions() {
        map = new Hashtable();
    }

    /**
     * Create the object, decoding the values from the passed DER stream.
     *
     * @param in the DerInputStream to read the Extension from.
     * @exception IOException on decoding errors.
     */
    public CertificateExtensions(DerInputStream in)
        throws IOException {

        map = new Hashtable();
        DerValue[] exts = in.getSequence(5);

        for (int i = 0; i < exts.length; i++) {
            Extension ext = new Extension(exts[i]);
            parseExtension(ext);
        }
    }

    /**
     * Decode the extensions from the InputStream.
     *
     * @param in the InputStream to unmarshal the contents from.
     * @exception IOException on decoding or validity errors.
     */
    public void decode(InputStream in) throws IOException {
        DerValue val = new DerValue(in);
        DerInputStream str = val.toDerInputStream();

        map = new Hashtable();
        DerValue[] exts = str.getSequence(5);

        for (int i = 0; i < exts.length; i++) {
            Extension ext = new Extension(exts[i]);
            parseExtension(ext);
        }
    }

    /**
     * Decode the extensions from the InputStream.
     *
     * @param in the InputStream to unmarshal the contents from.
     * @exception IOException on decoding or validity errors.
     */
    public void decodeEx(InputStream in) throws IOException {
        DerValue val = new DerValue(in);
        DerInputStream str = null;
        if (val.isConstructed() && val.isContextSpecific((byte)3)) {
          str = val.data;
        } else {
	  str = val.toDerInputStream();
        }

        map = new Hashtable();
        DerValue[] exts = str.getSequence(5);

        for (int i = 0; i < exts.length; i++) {
            Extension ext = new Extension(exts[i]);
            parseExtension(ext);
        }
    }

    private synchronized void writeObject(ObjectOutputStream stream)
    throws CertificateException, IOException {
        encode(stream);
    }

    private synchronized void readObject(ObjectInputStream stream)
    throws CertificateException, IOException {
       decodeEx(stream);
    }

    /**
     * Encode the extensions in DER form to the stream.
     *
     * @param out the DerOutputStream to marshal the contents to.
     * @exception CertificateException on encoding errors.
     * @exception IOException on errors.
     */
    public void encode(OutputStream out)
    throws CertificateException, IOException {
        DerOutputStream extOut = new DerOutputStream();
        for (int i = 0; i < size(); i++) {
            Object thisOne = elementAt(i);
            if (thisOne instanceof CertAttrSet)
                ((CertAttrSet)thisOne).encode(extOut);
            else if (thisOne instanceof Extension)
                ((Extension)thisOne).encode(extOut);
            else
                throw new CertificateException("Invalid extension object");
        }

        DerOutputStream seq = new DerOutputStream();
        seq.write(DerValue.tag_Sequence,extOut);

        DerOutputStream tmp = new DerOutputStream();
        tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)3),
                  seq);

        out.write(tmp.toByteArray());
    }

    /**
     * Set the attribute value.
     * @param name the extension name used in the cache.
     * @param obj the object to set.
     * @exception IOException if the object could not be cached.
     */
    public void set(String name, Object obj) throws IOException {
        map.put(name,obj);
        addElement(obj);
    }

    /**
     * Get the attribute value.
     * @param name the extension name used in the lookup.
     * @exception IOException if named extension is not found.
     */
    public Object get(String name) throws IOException {
        Object obj = map.get(name);
        if (obj == null) {
            throw new IOException("No extension found with name " + name);
        }
        return (obj);
    }

    /**
     * Delete the attribute value.
     * @param name the extension name used in the lookup.
     * @exception IOException if named extension is not found.
     */
    public void delete(String name) throws IOException {
        Object obj = map.get(name);
        if (obj == null) {
            throw new IOException("No extension found with name " + name);
        }
        map.remove(name);
        removeElement(obj);
    }

    public Enumeration getNames()
    {
	return map.keys();
    }

    /**
     * Return an enumeration of names of attributes existing within this
     * attribute.
     */
    public Enumeration getElements () {
        return (map.elements());
    }

    /**
     * Return the name of this attribute.
     */
    public String getName () {
        return (NAME);
    }
}
