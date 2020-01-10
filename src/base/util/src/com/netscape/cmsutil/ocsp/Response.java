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
package com.netscape.cmsutil.ocsp;

import java.io.*;
import org.mozilla.jss.asn1.*;

/**
 * RFC 2560:
 *
 * <pre>
 *     response       OCTET STRING
 * </pre>
 *
 * @version $Revision: 1213 $ $Date: 2010-08-18 11:44:45 -0700 (Wed, 18 Aug 2010) $
 */
public interface Response extends ASN1Value
{
	public OCTET_STRING getBytes();
}
