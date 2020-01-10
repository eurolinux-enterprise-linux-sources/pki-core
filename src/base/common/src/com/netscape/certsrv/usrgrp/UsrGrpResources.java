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

import java.util.ListResourceBundle;

/**
 * A class represents a resource bundle for the 
 * user/group manager
 *
 * @deprecated
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class UsrGrpResources extends ListResourceBundle {

    /**
     * Returns the content of this resource.
     * @return the content of this resource.
     */
    public Object[][] getContents() {
        return contents;
    }

    /**
     * Constants. The suffix represents the number of
     * possible parameters.
     */

    static final Object[][] contents = {};
}
