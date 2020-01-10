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
package com.netscape.certsrv.dbs.repository;

import java.util.*;
import java.io.*;
import java.math.*;
import netscape.ldap.*;
import netscape.security.x509.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.dbs.*;

/**
 * An interface represents a generic repository record. 
 * It maintains unique serial number within repository.
 * <P>
 *
 * @version $Revision: 1659 $, $Date: 2010-12-20 23:42:50 -0800 (Mon, 20 Dec 2010) $
 */
public interface IRepositoryRecord extends IDBObj {

	public final static String ATTR_SERIALNO = "serialNo";
	public final static String ATTR_PUB_STATUS = "publishingStatus";

	/**
	 * Retrieves serial number.
	 *
	 * @return serial number
	 */
	public BigInteger getSerialNumber();

	public String getPublishingStatus();
}
