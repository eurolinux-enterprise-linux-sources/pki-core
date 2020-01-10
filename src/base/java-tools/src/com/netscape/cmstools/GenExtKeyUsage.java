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
package com.netscape.cmstools;


import java.io.*;
import java.util.Vector;
import netscape.security.x509.*;
import netscape.security.util.*;


/**
 * Generates a DER-encoded Extended Key Usage extension.
 * The first parameter is the criticality of the extension, true or false.
 * The OIDs to be included in the extension are passed as command-line
 * arguments.  The OIDs are described in RFC 2459.  For example,
 * the OID for code signing is 1.3.6.1.5.5.7.3.3.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class GenExtKeyUsage {

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Usage:  GenExtKeyUsage [true|false] <OID> ...");
                System.exit(-1);
            } 

            boolean critical = false;

            if (args[0].equalsIgnoreCase("true")) {
                critical = true;
            } else if (args[0].equalsIgnoreCase("false")) {
                critical = false;
            } else {
                System.out.println("Usage: GenExtKeyUsage [true|false] <OID> ...");
                System.exit(-1);
            }

            // Generate vector of object identifiers from command line
            Vector oids = new Vector();

            for (int i = 1; i < args.length; i++) {
                ObjectIdentifier oid = new ObjectIdentifier(args[i]);

                oids.addElement(oid);
            }

            // encode all the object identifiers to the DerOutputStream
            DerOutputStream contents = new DerOutputStream();

            for (int i = 0; i < oids.size(); i++) {
                contents.putOID((ObjectIdentifier) oids.elementAt(i));
            }

            // stuff the object identifiers into a SEQUENCE
            DerOutputStream seq = new DerOutputStream();

            seq.write(DerValue.tag_Sequence, contents);

            // encode the SEQUENCE in an octet string
            DerOutputStream octetString = new DerOutputStream();

            octetString.putOctetString(seq.toByteArray());

            // Construct an extension
            ObjectIdentifier extKeyUsageOID = new ObjectIdentifier("2.5.29.37");
            Extension extn = new Extension(extKeyUsageOID, critical,
                    octetString.toByteArray());
            DerOutputStream extdos = new DerOutputStream();

            extn.encode(extdos);

            // BASE64 encode the whole thing and write it to stdout

            System.out.println(com.netscape.osutil.OSUtil.BtoA(extdos.toByteArray()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
