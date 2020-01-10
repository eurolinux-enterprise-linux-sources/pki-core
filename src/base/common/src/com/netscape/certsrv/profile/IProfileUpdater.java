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

import java.util.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.request.*;

/**
 * This interface represents an updater that will be
 * called when the request's state changes.
 * 
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public interface IProfileUpdater extends IConfigTemplate {

    /**
     * Initializes this default policy.
     *
     * @param profile owner of this policy
     * @param config configuration store
     * @exception EProfileException failed to initialize
     */
    public void init(IProfile profile, IConfigStore config)
        throws EProfileException;

    /**
     * Retrieves configuration store.
     *
     * @return configuration store
     */
    public IConfigStore getConfigStore();

    /**
     * Notifies of state change.
     *
     * @param req request
     * @param status The status to check for.
     * @exception EProfileException failed to populate
     */
    public void update(IRequest req, RequestStatus status) 
         throws EProfileException;

    /**
     * Retrieves the localizable name of this policy.
     *
     * @param locale user locale
     * @return output policy name
     */
    public String getName(Locale locale);

    /**
     * Retrieves the localizable description of this policy.
     *
     * @param locale user locale
     * @return output policy description
     */
    public String getText(Locale locale);
}
