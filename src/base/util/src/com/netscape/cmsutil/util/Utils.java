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
package com.netscape.cmsutil.util;


import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class Utils {
	/**
	 * Checks if this is NT.
	 */
	public static boolean isNT() {
		return ((File.separator).equals("\\"));
	}

	public static boolean exec(String cmd) {
		try {
			String cmds[] = null;
			if (isNT()) {
				// NT
				cmds = new String[3];
				cmds[0] = "cmd";
				cmds[1] = "/c";
				cmds[2] = cmd;
			} else {
				// UNIX
				cmds = new String[3];
				cmds[0] = "/bin/sh";
				cmds[1] = "-c";
				cmds[2] = cmd;
			}
			Process process = Runtime.getRuntime().exec(cmds);
			process.waitFor();
			BufferedReader pOut = null;
			String l = null;

			if (process.exitValue() == 0) {
				/**
				pOut = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				while ((l = pOut.readLine()) != null) {
					System.out.println(l);
				}
				**/
				return true;
			} else {
				/**
				pOut = new BufferedReader(
						new InputStreamReader(process.getErrorStream()));
				l = null;
				while ((l = pOut.readLine()) != null) {
					System.out.println(l);
				}
				**/
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

     public static String SpecialURLDecode(String s) {
        if (s == null)
            return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream(s.length());
        
        for (int i = 0; i < s.length(); i++) {
            int c = (int) s.charAt(i);

            if (c == '+') {
                out.write(' ');
            } else if (c == '#') {
                int c1 = Character.digit(s.charAt(++i), 16);
                int c2 = Character.digit(s.charAt(++i), 16);

                out.write((char) (c1 * 16 + c2));
            } else {
                out.write(c);
            }
        } // end for
        return out.toString();
    }

     public static byte[] SpecialDecode(String s) {
        if (s == null)
            return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream(s.length());
        
        for (int i = 0; i < s.length(); i++) {
            int c = (int) s.charAt(i);

            if (c == '+') {
                out.write(' ');
            } else if (c == '#') {
                int c1 = Character.digit(s.charAt(++i), 16);
                int c2 = Character.digit(s.charAt(++i), 16);

                out.write((char) (c1 * 16 + c2));
            } else {
                out.write(c);
            }
        } // end for
        return out.toByteArray();
    }

     public static String SpecialEncode(byte data[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
          sb.append("%");
          if ((data[i] & 0xff) < 16) {
              sb.append("0");
          }
          sb.append(Integer.toHexString((data[i] & 0xff)));
        }
        return sb.toString().toUpperCase();
     }

    public static void checkHost(String hostname) throws UnknownHostException {
        InetAddress addr = InetAddress.getByName(hostname);
    }

    public static void copy(String orig, String dest) {
        try {
        BufferedReader in = new BufferedReader(new FileReader(orig));
        PrintWriter out = new PrintWriter(
          new BufferedWriter(new FileWriter(dest)));
        String line = "";
        while (in.ready()) { 
            line = in.readLine(); 
            if (line != null)
                out.println(line);
        }
        in.close();
        out.close();
        } catch (Exception ee) {
        }
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int len;

        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    public static void copyStream(BufferedReader in, OutputStreamWriter out) throws IOException {
        char[] buf = new char[4096];
        int len;

        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    /// Sorts an array of Strings.
    // Java currently has no general sort function.  Sorting Strings is
    // common enough that it's worth making a special case.
    public static void sortStrings(String[] strings) {
        // Just does a bubblesort.
        for (int i = 0; i < strings.length - 1; ++i) {
            for (int j = i + 1; j < strings.length; ++j) {
                if (strings[i].compareTo(strings[j]) > 0) {
                    String t = strings[i];

                    strings[i] = strings[j];
                    strings[j] = t;
                }
            }
        }
    }

    /// Returns a date string formatted in Unix ls style - if it's within
    // six months of now, Mmm dd hh:ss, else Mmm dd  yyyy.
    public static String lsDateStr(Date date) {
        long dateTime = date.getTime();

        if (dateTime == -1L)
            return "------------";
        long nowTime = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat();

        if (Math.abs(nowTime - dateTime) < 183L * 24L * 60L * 60L * 1000L)
            formatter.applyPattern("MMM dd hh:ss");
        else
            formatter.applyPattern("MMM dd yyyy");
        return formatter.format(date);
    }

    /**
     * compares contents two byte arrays returning true if exactly same.
     */
    static public boolean byteArraysAreEqual(byte[] a, byte[] b) {
        if (a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }
    
    /**
     * strips out double quotes around String parameter
     * @param s the string potentially bracketed with double quotes
     * @return string stripped of surrounding double quotes
     */
    public static String stripQuotes(String s) {
        if (s == null) {
            return s;
        }

        if ((s.startsWith("\"")) && (s.endsWith("\""))) {
            return (s.substring(1, (s.length() - 1)));
        }

        return s;
    }

    /**
     * returns an array of strings from a vector of Strings
     * there'll be trouble if the Vector contains something other
     * than just Strings
     */
    public static String[] getStringArrayFromVector(Vector v) {
        String s[] = new String[v.size()];

        v.copyInto(s);
        return s;
    }

}
