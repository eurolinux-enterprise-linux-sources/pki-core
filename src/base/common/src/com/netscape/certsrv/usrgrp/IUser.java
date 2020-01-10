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
package com.netscape.certsrv.usrgrp;


import com.netscape.certsrv.common.*;
import com.netscape.certsrv.base.*;
import java.security.cert.*;
import netscape.security.x509.*;


/**
 * This interface defines the basic interfaces for
 * a user identity. (get/set methods for a user entry attributes)
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public interface IUser extends IAttrSet, IUserConstants {

    /**
     * Retrieves name.
     * @return user name
     */
    public String getName();

    /**
     * Retrieves user identifier.
     * @return user id
     */
    public String getUserID();

    /**
     * Retrieves user full name.
     * @return user fullname
     */
    public String getFullName();

    /**
     * Retrieves user phonenumber.
     * @return user phonenumber
     */
    public String getPhone();

    /**
     * Retrieves user state
     * @return user state
     */
    public String getState();

    /**
     * Sets user full name.
     * @param name the given full name
     */
    public void setFullName(String name);

    /**
     * Sets user ldap DN.
     * @param userdn the given user DN
     */
    public void setUserDN(String userdn);

    /**
     * Gets user ldap dn
     * @return user DN
     */
    public String getUserDN();

    /**
     * Retrieves user password.
     * @return user password
     */
    public String getPassword();

    /**
     * Sets user password.
     * @param p the given password
     */
    public void setPassword(String p);

    /**
     * Sets user phonenumber
     * @param p user phonenumber 
     */
    public void setPhone(String p);

    /**
     * Sets user state
     * @param p the given user state
     */
    public void setState(String p);

    /**
     * Sets user type
     * @param userType the given user type
     */
    public void setUserType(String userType);

    /**
     * Gets user email address.
     * @return email address
     */
    public String getEmail();

    /**
     * Sets user email address.
     * @param email the given email address
     */
    public void setEmail(String email);

    /**
     * Gets list of certificates from this user
     * @return list of certificates
     */
    public X509Certificate[] getX509Certificates();

    /**
     * Sets list of certificates in this user
     * @param certs list of certificates
     */
    public void setX509Certificates(X509Certificate certs[]);

    /**
     * Get certificate DN
     * @return certificate DN
     */
    public String getCertDN();

    /**
     * Set certificate DN
     * @param userdn the given DN
     */
    public void setCertDN(String userdn);

    /**
     * Get user type
     * @return user type.
     */
    public String getUserType();
}
