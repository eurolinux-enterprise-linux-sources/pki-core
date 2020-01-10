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
import java.security.*;
import java.security.cert.*;
//import netscape.security.provider.*;
import netscape.security.x509.*;
import netscape.security.pkcs.*;
import netscape.ldap.*;
import com.netscape.cmscore.util.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.dbs.keydb.*;


/**
 * A class represents a Key record. It maintains the key
 * life cycle as well as other information about an
 * archived key. Namely, whether a key is inactive because
 * of compromise.
 * <P>
 *
 * @author thomask
 * @version $Revision: 1796 $, $Date: 2011-01-31 09:17:21 -0800 (Mon, 31 Jan 2011) $
 */
public class KeyRecord implements IDBObj, IKeyRecord {

    private BigInteger mSerialNo = null;
    private KeyState mState = null;
    private MetaInfo mMetaInfo = null;
    private String mAlgorithm = null;
    private byte mPrivateKey[] = null;
    private byte mPublicKey[] = null;
    private Integer mSize = null;
    private String mOwnerName = null;
    private Date mDatesOfRecovery[] = null;
    private Date mCreateTime = null;
    private Date mModifyTime = null;
    private String mArchivedBy = null;

    protected static Vector mNames = new Vector();
    static {
        mNames.addElement(ATTR_STATE);
        mNames.addElement(ATTR_ID);
        mNames.addElement(ATTR_OWNER_NAME);
        mNames.addElement(ATTR_KEY_SIZE);
        mNames.addElement(ATTR_ALGORITHM);
        mNames.addElement(ATTR_PRIVATE_KEY_DATA);
        mNames.addElement(ATTR_PUBLIC_KEY_DATA);
        mNames.addElement(ATTR_DATE_OF_RECOVERY);
        mNames.addElement(ATTR_META_INFO);
        mNames.addElement(ATTR_CREATE_TIME);
        mNames.addElement(ATTR_MODIFY_TIME);
        mNames.addElement(ATTR_ARCHIVED_BY);
    }

    /**
     * Constructs empty key record.
     */
    public KeyRecord() {
    }

    /*
     *  Constructs key record.
     * 
     * @param key key to be archived
     */
    public KeyRecord(BigInteger serialNo, byte publicData[], 
        byte privateData[], String owner,
        String algorithm, String agentId) 
        throws EBaseException {
        mSerialNo = serialNo;
        mPublicKey = publicData;
        mPrivateKey = privateData;
        mOwnerName = owner;
        mAlgorithm = algorithm;
        mState = KeyState.VALID;
        mCreateTime = com.netscape.certsrv.apps.CMS.getCurrentDate();
        mModifyTime = com.netscape.certsrv.apps.CMS.getCurrentDate();
        mArchivedBy = agentId;
    }

    /**
     * Sets an attribute.
     * <P>
     */
    public void set(String name, Object object) throws EBaseException {
        if (name.equalsIgnoreCase(ATTR_STATE)) {
            mState = (KeyState) object;
        } else if (name.equalsIgnoreCase(ATTR_ID)) {
            mSerialNo = (BigInteger) object;
        } else if (name.equalsIgnoreCase(ATTR_KEY_SIZE)) {
            mSize = (Integer) object;
        } else if (name.equalsIgnoreCase(ATTR_OWNER_NAME)) {
            mOwnerName = (String) object;
        } else if (name.equalsIgnoreCase(ATTR_ALGORITHM)) {
            mAlgorithm = (String) object;
        } else if (name.equalsIgnoreCase(ATTR_PRIVATE_KEY_DATA)) {
            mPrivateKey = (byte[]) object;
        } else if (name.equalsIgnoreCase(ATTR_PUBLIC_KEY_DATA)) {
            mPublicKey = (byte[]) object;
        } else if (name.equalsIgnoreCase(ATTR_DATE_OF_RECOVERY)) {
            mDatesOfRecovery = (Date[]) object;
        } else if (name.equalsIgnoreCase(ATTR_META_INFO)) {
            mMetaInfo = (MetaInfo) object;
        } else if (name.equalsIgnoreCase(ATTR_CREATE_TIME)) {
            mCreateTime = (Date) object;
        } else if (name.equalsIgnoreCase(ATTR_MODIFY_TIME)) {
            mModifyTime = (Date) object;
        } else if (name.equalsIgnoreCase(ATTR_ARCHIVED_BY)) {
            mArchivedBy = (String) object;
        } else {
            throw new EBaseException(com.netscape.certsrv.apps.CMS.getUserMessage("CMS_BASE_INVALID_ATTRIBUTE", name));
        }
    }

    /**
     * Retrieves an attribute.
     * <P>
     */
    public Object get(String name) throws EBaseException {
        if (name.equalsIgnoreCase(ATTR_STATE)) {
            return mState;
        } else if (name.equalsIgnoreCase(ATTR_ID)) {
            return mSerialNo;
        } else if (name.equalsIgnoreCase(ATTR_KEY_SIZE)) {
            return mSize;
        } else if (name.equalsIgnoreCase(ATTR_OWNER_NAME)) {
            return mOwnerName;
        } else if (name.equalsIgnoreCase(ATTR_ALGORITHM)) {
            return mAlgorithm;
        } else if (name.equalsIgnoreCase(ATTR_PRIVATE_KEY_DATA)) {
            return mPrivateKey;
        } else if (name.equalsIgnoreCase(ATTR_PUBLIC_KEY_DATA)) {
            return mPublicKey;
        } else if (name.equalsIgnoreCase(ATTR_DATE_OF_RECOVERY)) {
            return mDatesOfRecovery;
        } else if (name.equalsIgnoreCase(ATTR_CREATE_TIME)) {
            return mCreateTime;
        } else if (name.equalsIgnoreCase(ATTR_MODIFY_TIME)) {
            return mModifyTime;
        } else if (name.equalsIgnoreCase(ATTR_META_INFO)) {
            return mMetaInfo;
        } else if (name.equalsIgnoreCase(ATTR_ARCHIVED_BY)) {
            return mArchivedBy;
        } else {
            throw new EBaseException(com.netscape.certsrv.apps.CMS.getUserMessage("CMS_BASE_INVALID_ATTRIBUTE", name));
        }
    }

    /**
     * Deletes an attribute.
     * <P>
     */
    public void delete(String name) throws EBaseException {
        throw new EBaseException(com.netscape.certsrv.apps.CMS.getUserMessage("CMS_BASE_INVALID_ATTRIBUTE", name));
    }

    /**
     * Retrieves an enumeration of attributes.
     * <P>
     */
    public Enumeration getElements() {
        return mNames.elements();
    }

    /**
     * Retrieves serializable attribute names.
     */
    public Enumeration getSerializableAttrNames() {
        return mNames.elements();
    }

    /**
     * Retrieves serial number of the key record. Each key record
     * is uniquely identified by serial number.
     * <P>
     *
     * @return serial number of this key record
     */
    public BigInteger getSerialNumber() throws EBaseException {
        return mSerialNo;
    }

    /**
     * Sets serial number.
     */
    public void setSerialNumber(BigInteger serialno) throws EBaseException {
        mSerialNo = serialno;
    }

    /**
     * Retrieves the key state. This gives key life cycle 
     * information.
     * <P>
     *
     * @return key state
     */
    public KeyState getState() throws EBaseException {
        return mState;
    }

    /**
     * Sets key state.
     * <P>
     */
    public void setState(KeyState state) throws EBaseException {
        mState = state;
    }

    /**
     * Retrieves the uid of person who archived this record.
     */
    public String getArchivedBy() {
        return mArchivedBy;
    }

    /**
     * Retrieves key.
     * <P>
     * 	
     * @return archived key
     */
    public byte[] getPrivateKeyData() throws EBaseException {
        return mPrivateKey;
    }

    /**
     * Sets key data.
     */
    public void setPrivateKeyData(byte keydata[]) throws EBaseException {
        mPrivateKey = keydata;
    }

    /**
     * Retrieves the key size.
     * <P>
     *
     * @return key size
     */
    public Integer getKeySize() throws EBaseException {
        return mSize;
    }

    /**
     * Sets key size.
     * <P>
     */
    public void setKeySize(Integer keySize) throws EBaseException {
        mSize = keySize;
    }

    /**
     * Retrieves owner name.
     * <P>
     */
    public String getOwnerName() throws EBaseException {
        return mOwnerName;
    }

    /**
     * Sets owner name.	
     * <P>
     */
    public void setOwnerName(String name) throws EBaseException {
        mOwnerName = name;
    }

    /**
     * Retrieves the public key.
     * <P>
     */
    public byte[] getPublicKeyData() throws EBaseException {
        return mPublicKey;
    }

    /**
     * Sets the public key.
     * <P>
     */
    public void setPublicKeyData(byte key[]) throws EBaseException {
        mPublicKey = key;
    }

    /**
     * Retrieves the date(s) of revocation.
     * <P>
     */
    public Date[] getDateOfRevocation() throws EBaseException {
        return mDatesOfRecovery;
    }

    /**
     * Sets the dateso of revocation.
     * <P>
     */
    public void setDateOfRevocation(Date dates[]) throws EBaseException {
        mDatesOfRecovery = dates;
    }

    /**
     * Retrieves algorithm of the key pair.
     */
    public String getAlgorithm() {
        return mAlgorithm;
    }

    public MetaInfo getMetaInfo() {
        return mMetaInfo;
    }

    /**
     * Retrieves the creation time of this record.
     */
    public Date getCreateTime() {
        return mCreateTime;
    }

    /**
     * Retrieves the last modification time of 
     * this record.
     */
    public Date getModifyTime() {
        return mModifyTime;
    }
}
