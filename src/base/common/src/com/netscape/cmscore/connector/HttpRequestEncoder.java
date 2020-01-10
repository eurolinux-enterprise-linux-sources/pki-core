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
package com.netscape.cmscore.connector;


import com.netscape.certsrv.request.*;
import com.netscape.certsrv.connector.*;
import com.netscape.cmscore.util.Debug;

import java.io.*;


/**
 * encodes a request by serializing it. 
 */
public class HttpRequestEncoder implements IRequestEncoder {
    public String encode(Object r)
        throws IOException {
        String s = null;
        byte[] serial;
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(ba);

        os.writeObject(r);
        serial = ba.toByteArray();
        s = com.netscape.osutil.OSUtil.BtoA(serial);
        return s;
    }

    public Object decode(String s)
        throws IOException {
        Object result = null;
        byte[] serial = null;

        try {

            serial = com.netscape.osutil.OSUtil.AtoB(s);
            ByteArrayInputStream ba = new ByteArrayInputStream(serial);
            ObjectInputStream is = new ObjectInputStream(ba);

            result = is.readObject();
        } catch (ClassNotFoundException e) {
            // XXX hack: change this 
            if (Debug.ON)
                Debug.trace("class not found ex " + e + e.getMessage());
            throw new IOException("Class Not Found " + e.getMessage());
        } catch (OptionalDataException e) {
            if (e.eof == true) {
                if (Debug.ON)
                    Debug.trace("done reading input stream " + result);
            } else {
                if (Debug.ON)
                    Debug.trace(e.length + " more bytes of primitive data");
            }
        }
        return result;
    }
}
