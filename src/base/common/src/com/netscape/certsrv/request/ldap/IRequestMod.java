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
package com.netscape.certsrv.request.ldap;

import java.util.Date;

import com.netscape.certsrv.request.RequestId;
import com.netscape.certsrv.request.RequestStatus;
import com.netscape.certsrv.request.IRequest;

/**
 * This interface defines how to update request record.
 * <p>
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public interface IRequestMod
{
	/**
     * Modifies request status.
	 *
     * @param r request
     * @param s request status
     */
	void modRequestStatus(IRequest r, RequestStatus s);

	/**
     * Modifies request creation time.
	 *
     * @param r request
     * @param d date
     */
	void modCreationTime(IRequest r, Date d);

	/**
     * Modifies request modification time.
	 *
     * @param r request
     * @param d date
     */
	void modModificationTime(IRequest r, Date d);
}
