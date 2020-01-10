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
package com.netscape.certsrv.profile;

import com.netscape.certsrv.base.*;

/**
 * This represents a profile specific exception. This
 * exception is raised when a request is rejected.
 * <p>
 * A rejected request cannot be reprocessed. Rejected
 * request is considered as a request in its terminal
 * state.
 * <p>
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class ERejectException extends EProfileException {

    /**
     * Creates a rejection exception. 
     *
     * @param msg localized message that indicates
     *            the reason why a request is 
     *            rejected.
     */
    public ERejectException(String msg) {
        super(msg);
    }
}
