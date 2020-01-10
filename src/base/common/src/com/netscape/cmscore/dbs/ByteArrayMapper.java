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
import netscape.ldap.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.apps.*;


/**
 * A class represents ann attribute mapper that maps
 * a Java byte array object into LDAP attribute,
 * and vice versa.
 *
 * @author thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $ 
 */
public class ByteArrayMapper implements IDBAttrMapper {

    private String mLdapName = null;
    private Vector v = new Vector();

    /**
     * Constructs a byte array mapper.
     */
    public ByteArrayMapper(String ldapName) {
        mLdapName = ldapName;
        v.addElement(mLdapName);
    }

    /**
     * Lists a list of supported ldap attribute names.
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
        byte data[] = (byte[]) obj;
        if (data == null) {
            CMS.debug("ByteArrayMapper:mapObjectToLDAPAttributeSet " + name +
		" size=0");
        } else {
            CMS.debug("ByteArrayMapper:mapObjectToLDAPAttributeSet " + name +
		" size=" + data.length);
        }
        attrs.add(new LDAPAttribute(mLdapName, data));
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
        parent.set(name, (byte[]) attr.getByteValues().nextElement());
    }

    /**
     * Maps search filters into LDAP search filter.
     */
    public String mapSearchFilter(String name, String op, 
        String value) throws EBaseException {
        return mLdapName + op + value;
    }
}
