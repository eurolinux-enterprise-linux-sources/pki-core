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
package com.netscape.cmscore.ldapconn;


import netscape.ldap.*;
import com.netscape.certsrv.ldap.*;


/**
 * A LDAP connection that is bound to a server host, port and secure type.
 * Makes a LDAP connection when instantiated.
 * Cannot establish another LDAP connection after construction. 
 * LDAPConnection connect methods are overridden to prevent this.
 */
public class LdapAnonConnection extends LDAPConnection {

    /**
     * instantiates a connection to a ldap server
     */
    public LdapAnonConnection(LdapConnInfo connInfo)
        throws LDAPException {
        super(connInfo.getSecure() ? new LdapJssSSLSocketFactory() : null);

        // Set option to automatically follow referrals. 
        // rebind info is also anonymous.
        boolean followReferrals = connInfo.getFollowReferrals();

        setOption(LDAPv2.REFERRALS, new Boolean(followReferrals));

        super.connect(connInfo.getVersion(), 
            connInfo.getHost(), connInfo.getPort(), null, null);
    }

    /**
     * instantiates a connection to a ldap server
     */
    public LdapAnonConnection(String host, int port, int version, 
        LDAPSocketFactory fac)
        throws LDAPException {
        super(fac);
        super.connect(version, host, port, null, null);
    }

    /**
     * instantiates a non-secure connection to a ldap server
     */
    public LdapAnonConnection(String host, int port, int version)
        throws LDAPException {
        super();
        super.connect(version, host, port, null, null);
    }

    /**
     * overrides superclass connect. 
     * does not allow reconnect.
     */
    public void connect(String host, int port) throws LDAPException {
        throw new RuntimeException(
                "this LdapAnonConnection already connected: connect(h,p)");
    }

    /**
     * overrides superclass connect.
     * does not allow reconnect.
     */
    public void connect(int version, String host, int port, 
        String dn, String pw) throws LDAPException {
        throw new RuntimeException(
                "this LdapAnonConnection already connected: connect(v,h,p)");
    }
}
