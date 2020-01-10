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
import java.io.*;
import java.math.*;
import netscape.ldap.*;
import netscape.security.x509.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.dbs.*;


/**
 * A class represents a collection of schema information
 * for CRL.
 * <P>
 *
 * @author thomask
 * @version $Revision: 1677 $, $Date: 2010-12-23 17:08:17 -0800 (Thu, 23 Dec 2010) $
 */
public class CRLDBSchema {

    public static final String LDAP_OC_TOP = "top";
    public static final String LDAP_OC_CRL_RECORD = "crlIssuingPointRecord";
    public static final String LDAP_ATTR_CRL_ID = "cn";
    public static final String LDAP_ATTR_CRL_NUMBER = "crlNumber";
    public static final String LDAP_ATTR_DELTA_NUMBER = "deltaNumber";
    public static final String LDAP_ATTR_CRL_SIZE = "crlSize";
    public static final String LDAP_ATTR_DELTA_SIZE = "deltaSize";
    public static final String LDAP_ATTR_THIS_UPDATE = "thisUpdate";
    public static final String LDAP_ATTR_NEXT_UPDATE = "nextUpdate";
    public static final String LDAP_ATTR_FIRST_UNSAVED = "firstUnsaved";
    public static final String LDAP_ATTR_CRL = "certificateRevocationList";
    public static final String LDAP_ATTR_CA_CERT = "cACertificate";
    public static final String LDAP_ATTR_CRL_CACHE = "crlCache";
    public static final String LDAP_ATTR_REVOKED_CERTS = "revokedCerts";
    public static final String LDAP_ATTR_UNREVOKED_CERTS = "unrevokedCerts";
    public static final String LDAP_ATTR_EXPIRED_CERTS = "expiredCerts";
    public static final String LDAP_ATTR_DELTA_CRL = "deltaRevocationList";
}
