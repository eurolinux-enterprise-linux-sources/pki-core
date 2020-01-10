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
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.keydb.*;


/**
 * A class represents a key state mapper.
 * <P>
 *
 * @author thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $ 
 */
public class KeyStateMapper implements IDBAttrMapper {

    private String mLdapName = null;

    public KeyStateMapper(String ldapName) {
        mLdapName = ldapName;
    }

    public Enumeration getSupportedLDAPAttributeNames() {
        Vector v = new Vector();

        v.addElement(mLdapName);
        return v.elements();
    }

    public void mapObjectToLDAPAttributeSet(IDBObj parent, 
        String name, Object obj, LDAPAttributeSet attrs) 
        throws EBaseException {
        attrs.add(new LDAPAttribute(mLdapName, 
                ((KeyState) obj).toString()));
    }

    /**
     * Maps LDAP attributes into object, and put the object
     * into 'parent'.
     */
    public void mapLDAPAttributeSetToObject(LDAPAttributeSet attrs, 
        String name, IDBObj parent) throws EBaseException {
        LDAPAttribute attr = attrs.getAttribute(mLdapName);

        if (attr == null) {
            return;
        }
        parent.set(name, KeyState.toKeyState(
                ((String) attr.getStringValues().nextElement())));
    }

    /**
     * Maps search filters into LDAP search filter.
     */
    public String mapSearchFilter(String name, String op, 
        String value) throws EBaseException {
        return mLdapName + op + value;
    }
}
