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
package com.netscape.cmscore.usrgrp;


import netscape.ldap.*;
import java.util.*;
import java.security.*;
import java.security.cert.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.ldap.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.usrgrp.*;


/**
 * This interface defines a strategy on how to match
 * the incoming certificate(s) with the certificate(s)
 * in the scope.  It matches the "certdn" field which contains
 * the subject dn of the certificate
 *
 * @author cfu
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class CertDNCertUserLocator implements ICertUserLocator {
    private IUGSubsystem mUG = null;
    protected static final String LDAP_ATTR_CERTDN = "seeAlso";

    /**
     * Constructs certificate matching agent.
     */
    public CertDNCertUserLocator() {
    }

    /**
     * Retrieves description.
     */
    public String getDescription() {
        return "A subject is authenticated if its first" + 
            " certificate can be matched with one of the" +
            " certificate in the scope";
    }

    /**
     * Do the cert-user mapping
     */
    public IUser locateUser(Certificates certs) throws
            EUsrGrpException, LDAPException, ELdapException {
        mUG = (IUGSubsystem) CMS.getSubsystem(CMS.SUBSYSTEM_UG);

        X509Certificate certificates[] = certs.getCertificates();

        if (certificates == null)
            return null;

        String filter = LDAP_ATTR_CERTDN + "=" +
            certificates[0].getSubjectDN();

        return mUG.findUsersByCert(filter);
    }
}
