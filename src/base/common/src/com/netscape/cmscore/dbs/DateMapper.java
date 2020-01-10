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
import java.text.*;
import netscape.ldap.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.dbs.*;


/**
 * A class represents ann attribute mapper that maps
 * a Java Date object into LDAP attribute,
 * and vice versa.
 *
 * @author thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class DateMapper implements IDBAttrMapper {

    private String mLdapName = null;
    private Vector v = new Vector();
    private static SimpleDateFormat formatter = new
        SimpleDateFormat("yyyyMMddHHmmss'Z'");

    /**
     * Constructs date mapper.
     */
    public DateMapper(String ldapName) {
        mLdapName = ldapName;
        v.addElement(mLdapName);
    }

    /**
     * Retrieves a list of ldap attribute names.
     */
    public Enumeration getSupportedLDAPAttributeNames() {
        return v.elements();
    }

    /**
     * Maps object to ldap attribute set.
     */
    public void mapObjectToLDAPAttributeSet(IDBObj parent,
        String name, Object obj, LDAPAttributeSet attrs)
        throws EBaseException {
        attrs.add(new LDAPAttribute(mLdapName, 
                dateToDB((Date) obj)));
    }

    /**
     * Maps LDAP attributes into object, and put the object
     * into 'parent'.
     */
    public void mapLDAPAttributeSetToObject(LDAPAttributeSet attrs,
        String name, IDBObj parent) throws EBaseException {
        LDAPAttribute attr = attrs.getAttribute(mLdapName);

        if (attr == null)
            return;
        parent.set(name, dateFromDB((String)
                attr.getStringValues().nextElement()));
    }

    /**
     * Maps search filters into LDAP search filter.
     */
    public String mapSearchFilter(String name, String op,
        String value) throws EBaseException {
        String val = null;

        try {
            val = dateToDB(new Date(Long.parseLong(value)));
        } catch (NumberFormatException e) {
            val = value;
        }
        return mLdapName + op + val;
    }

    public synchronized static String dateToDB(Date date) {
        return formatter.format(date);
    }

    public synchronized static Date dateFromDB(String dbDate) {
        try {
            return formatter.parse(dbDate);
        } catch (ParseException e) {
        }
        return null;
    }
}
