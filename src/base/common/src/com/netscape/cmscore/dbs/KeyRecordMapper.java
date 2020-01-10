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


import java.math.*;
import java.io.*;
import java.util.*;
import java.security.cert.*;
import netscape.ldap.*;
import netscape.security.x509.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.keydb.*;
import com.netscape.certsrv.kra.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.*;
 

/**
 * A class represents a mapper to serialize 
 * key record into database.
 * <P>
 *
 * @author  thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class KeyRecordMapper implements IDBAttrMapper {

    private IKeyRepository mDB = null;
    private ILogger mLogger = CMS.getLogger();

    public KeyRecordMapper(IKeyRepository db) {
        mDB = db;
    }

    public Enumeration getSupportedLDAPAttributeNames() {
        Vector v = new Vector();

        v.addElement(KeyDBSchema.LDAP_ATTR_KEY_RECORD_ID);
        return v.elements();
    }

    public void mapObjectToLDAPAttributeSet(IDBObj parent, String name, 
        Object obj, LDAPAttributeSet attrs) throws EBaseException {
        try {
            KeyRecord rec = (KeyRecord) obj;

            attrs.add(new LDAPAttribute(KeyDBSchema.LDAP_ATTR_KEY_RECORD_ID,
                    rec.getSerialNumber().toString()));
        } catch (Exception e) {

            /*LogDoc
             *
             * @phase  Maps object to ldap attribute set
             * @message KeyRecordMapper: <exception thrown>
             */
            mLogger.log(ILogger.EV_SYSTEM, ILogger.S_DB, ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSCORE_DBS_KEYRECORD_MAPPER_ERROR", e.toString()));
            throw new EDBException(
                    CMS.getUserMessage("CMS_DBS_SERIALIZE_FAILED", name));
        }
    }

    public void mapLDAPAttributeSetToObject(LDAPAttributeSet attrs, 
        String name, IDBObj parent) throws EBaseException {
        try {	
            LDAPAttribute attr = attrs.getAttribute(
                    KeyDBSchema.LDAP_ATTR_KEY_RECORD_ID);

            if (attr == null)
                return;
            String serialno = (String) attr.getStringValues().nextElement();
            IKeyRecord rec = mDB.readKeyRecord(new 
                    BigInteger(serialno));

            parent.set(name, rec);
        } catch (Exception e) {

            /*LogDoc
             *
             * @phase  Maps ldap attribute set to object
             * @message KeyRecordMapper: <exception thrown>
             */
            mLogger.log(ILogger.EV_SYSTEM, ILogger.S_DB, ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSCORE_DBS_KEYRECORD_MAPPER_ERROR", e.toString()));
            throw new EDBException(
                    CMS.getUserMessage("CMS_DBS_DESERIALIZE_FAILED", name));
        }
    }

    public String mapSearchFilter(String name, String op, String value)
        throws EBaseException {
        return name + op + value;
    }
}
