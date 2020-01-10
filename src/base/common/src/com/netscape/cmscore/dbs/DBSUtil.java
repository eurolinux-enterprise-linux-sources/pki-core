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
import java.math.*;
import netscape.ldap.*;
import com.netscape.certsrv.base.*;


/**
 * A class represents ann attribute mapper that maps
 * a Java BigInteger object into LDAP attribute,
 * and vice versa.
 *
 * @author thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $ 
 */
public class DBSUtil {

    public static String longToDB(long val) {
        String s = Long.toString(val);
        // prefix with 2 digits that represents
        // the length of the value
        int l = s.length();
        String dbVal = "";

        if (s.length() < 10) {
            dbVal = "0";
        }
        return dbVal + Integer.toString(l) + s;
    }

    public static long longFromDB(String dbLong) {
        // remove the first 2 digits
        String s = dbLong.substring(2);

        return Long.parseLong(s);
    }
}
