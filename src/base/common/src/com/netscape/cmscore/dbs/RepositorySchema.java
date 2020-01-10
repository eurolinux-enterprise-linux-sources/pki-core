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
 * A class represents a collection of repository-specific
 * schema information.
 * <P>
 *
 * @author thomask
 * @version $Revision: 1659 $, $Date: 2010-12-20 23:42:50 -0800 (Mon, 20 Dec 2010) $
 */
public class RepositorySchema {

    public static final String LDAP_OC_TOP = "top";
    public static final String LDAP_OC_REPOSITORY = "repository";
    public static final String LDAP_ATTR_SERIALNO = "serialno";
    public static final String LDAP_ATTR_PUB_STATUS = "publishingStatus";
}
