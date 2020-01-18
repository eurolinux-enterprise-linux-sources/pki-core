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
// (C) 2013 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---
package org.dogtagpki.tps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.dogtagpki.tps.msg.TPSMessage;

import com.netscape.certsrv.apps.CMS;

/**
 * @author Endi S. Dewata <edewata@redhat.com>
 */
public class TPSConnection {

    public InputStream in;
    public PrintStream out;
    public boolean chunked;

    public TPSConnection(InputStream in, OutputStream out) {
        this(in, out, false);
    }

    public TPSConnection(InputStream in, OutputStream out, boolean chunked) {
        this.in = in;
        this.out = new PrintStream(out);
        this.chunked = chunked;
    }

    public TPSMessage read() throws IOException {
        CMS.debug("TPSConnection read()");

        StringBuilder sb = new StringBuilder();
        int b;

        // read the first parameter
        while ((b = in.read()) >= 0) {
            char c = (char) b;
            if (c == '&')
                break;
            sb.append(c);
        }

        if (b < 0)
            throw new IOException("Unexpected end of stream");

        // parse message size
        String nvp = sb.toString();
        String[] s = nvp.split("=");
        int size = Integer.parseInt(s[1]);

        sb.append('&');

        // read the rest of message
        for (int i = 0; i < size; i++) {

            b = in.read();
            if (b < 0)
                throw new IOException("Unexpected end of stream");

            char c = (char) b;
            sb.append(c);
        }

        if (size <= 38) // for pdu_data size is 2 and only contains status
            CMS.debug("TPSConnection.read: Reading:  " + sb.toString());
        else
            CMS.debug("TPSConnection.read: Reading...");

        // parse the entire message
        return TPSMessage.createMessage(sb.toString());
    }

    public void write(TPSMessage message) throws IOException {
        String s = message.encode();

        // don't print the pdu_data
        int idx =  s.lastIndexOf("pdu_data=");

        int debug = 0;
        String toDebug = null;
        if (idx == -1 || debug == 1)
            CMS.debug("TPSConnection.write: Writing: " + s);
        else {
            toDebug = s.substring(0, idx-1);
            CMS.debug("TPSConnection.write: Writing: " + toDebug + "pdu_data=<do not print>");
        }
        // send message
        out.print(s);

        // We don't have to send any specific chunk format here
        // The output stream detects chunked encoding and sends
        // the correct output to the other end.


        out.flush();
    }

}
