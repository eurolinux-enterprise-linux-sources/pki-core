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
package com.netscape.cmscore.base;


import com.netscape.certsrv.apps.CMS;
import java.util.*;
import com.netscape.certsrv.base.*;


/**
 * A class represents a subsystem loader.
 * <P>
 * 
 * @author thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class SubsystemLoader {
	
    private static final String PROP_SUBSYSTEM = "subsystem";
    private static final String PROP_CLASSNAME = "class";
    private static final String PROP_ID = "id";

    public static Vector load(IConfigStore config) throws EBaseException {
        Vector v = new Vector();

        // load a list of installable subsystems (services)
        for (int i = 0;; i++) {
            IConfigStore c = config.getSubStore(PROP_SUBSYSTEM + i);

            if (c == null)
                break;
            String id = null;

            try {
                id = c.getString(PROP_ID, null);
                if (id == null)
                    break;
            } catch (EBaseException e) {
                break;
            }
            String className = c.getString(PROP_CLASSNAME, null);

            if (className == null)
                break;
            try {
                ISubsystem sub = (ISubsystem) Class.forName(
                        className).newInstance();

                sub.setId(id);
                v.addElement(sub);
            } catch (Exception e) {
                throw new EBaseException(
                        CMS.getUserMessage("CMS_BASE_LOAD_FAILED", className));
            }
        }
        return v;
    }
}
