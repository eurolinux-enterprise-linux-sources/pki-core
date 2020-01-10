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
package com.netscape.certsrv.pattern;


import java.util.*;
import com.netscape.certsrv.base.*;

/**
 * This class represents a collection of attribute
 * sets.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class AttrSetCollection extends Hashtable {

    /**
     * Constructs a collection.
     */
    public AttrSetCollection() {
        super();
    }

    /**
     * Retrieves a attribute set from this collection.
     *
     * @param name name of the attribute set
     * @return attribute set
     */
    public IAttrSet getAttrSet(String name) {
        return (IAttrSet) get(name);
    }

    /**
     * Sets attribute set in this collection.
     *
     * @param name set of the attribute set
     * @param set attribute set
     */
    public void putAttrSet(String name, IAttrSet set) {
        put(name, set);
    }
}
