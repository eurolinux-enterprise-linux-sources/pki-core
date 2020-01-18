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
package com.netscape.certsrv.publish;

import com.netscape.certsrv.base.EBaseException;
import com.netscape.certsrv.base.IConfigStore;
import com.netscape.certsrv.base.IPluginImpl;
import com.netscape.certsrv.base.ISubsystem;
import com.netscape.certsrv.ldap.ELdapException;

/**
 * Interface for any ldap plugin. Plugin implementation is defined here.
 *
 * @version $Revision$ $Date$
 */
public interface ILdapPluginImpl extends IPluginImpl {

    /**
     * initialize from config store.
     *
     * @param config the configuration store to initialize from.
     * @exception ELdapException initializtion failed due to Ldap error.
     * @exception EBaseException initialization failed.
     */
    public void init(ISubsystem sys, IConfigStore config)
            throws EBaseException, ELdapException;

    /**
     * initialize from config store and Isubsystem.
     *
     * @param config the configuration store to initialize from.
     * @exception ELdapException initializtion failed due to Ldap error.
     * @exception EBaseException initialization failed.
     */
    public void init(IConfigStore config)
            throws EBaseException, ELdapException;

}
