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

import org.w3c.dom.*;
import org.apache.xml.serialize.*;

import javax.xml.parsers.*;
import java.util.*;
import java.io.*;
import java.net.*;


public class ParseXML {
    Document dom = null;

    public ParseXML() {// nothing
    }

    public void parse(java.io.InputStream is) {
        try {
            // get the factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = db.parse(is);
        } catch (Exception se) {
            System.out.println("ERROR: unable to parse xml");
            se.printStackTrace();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                br.close();
                System.out.println("ERROR XML = " + sb.toString());
            } catch (Exception se1) {
                System.out.println("ERROR: unable to print xml");
                se1.printStackTrace();
            }
        }
    }

    public String getvalue(String tag) {
        String temp = null;

        try {

            // get the root elememt
            Element docEle = dom.getDocumentElement();
				
            // get a nodelist of <employee> elements
            NodeList nl = docEle.getElementsByTagName(tag);

            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);

                if (el != null) {
                    temp = el.getFirstChild().getNodeValue();
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Tag=" + tag + "has no values");
            return null;
        }

        return temp;
    }

    public void prettyprintxml() {
        try {
            // Serialize the document
            OutputFormat format = new OutputFormat(dom);

            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(System.out, format);

            serializer.serialize(dom);
        } catch (Exception e) {}
    }
			
    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);

        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);

            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    // returns an arraylist of values for the corresponding tag

    public ArrayList constructvaluelist(String first, String second) {
        String temp = null;
        ArrayList al = new ArrayList();

        try {
            // get the root elememt
            Element docEle = dom.getDocumentElement();
		
            // get a nodelist of <employee> elements
            NodeList nl = docEle.getElementsByTagName(first);

            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    String value = getTextValue(el, second);

                    System.out.println("tag=" + second + " value=" + value);
                    if (value != null) {
                        al.add(value);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Tag=" + first + " has no values");
        }

        return al;
    }

    public static void main(String args[]) {
        try {

            ParseXML px = new ParseXML();
            FileInputStream fiscfg = new FileInputStream("/tmp/test.xml");

            px.parse(fiscfg);
            px.prettyprintxml();

        } catch (Exception e) {}
    }

}


; // end class
