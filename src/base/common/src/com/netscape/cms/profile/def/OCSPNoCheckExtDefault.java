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
package com.netscape.cms.profile.def;


import java.io.*;
import java.util.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.profile.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.apps.*;

import netscape.security.x509.*;
import netscape.security.util.*;
import netscape.security.extensions.*;
import com.netscape.cms.profile.common.*;


/**
 * This class implements an enrollment default policy
 * that populates an OCSP No Check extension
 * into the certificate template.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class OCSPNoCheckExtDefault extends EnrollExtDefault {

    public static final String CONFIG_CRITICAL = "ocspNoCheckCritical";

    public static final String VAL_CRITICAL = "ocspNoCheckCritical";

    public OCSPNoCheckExtDefault() {
        super();
        addValueName(VAL_CRITICAL);
        addConfigName(CONFIG_CRITICAL);
    }

    public void init(IProfile profile, IConfigStore config)
        throws EProfileException {
        super.init(profile, config);
    }

    public IDescriptor getConfigDescriptor(Locale locale, String name) { 
        if (name.equals(CONFIG_CRITICAL)) {
            return new Descriptor(IDescriptor.BOOLEAN, null, 
                    "false",
                    CMS.getUserMessage(locale, "CMS_PROFILE_CRITICAL"));
        } else {
            return null;
        }
    }

    public IDescriptor getValueDescriptor(Locale locale, String name) {
        if (name.equals(VAL_CRITICAL)) {
            return new Descriptor(IDescriptor.BOOLEAN, null, 
                    "false",
                    CMS.getUserMessage(locale, "CMS_PROFILE_CRITICAL"));
        } else {
            return null;
        }
    }

    public void setValue(String name, Locale locale,
        X509CertInfo info, String value)
        throws EPropertyException {
        if (name == null) { 
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", name));
        }

        OCSPNoCheckExtension ext = (OCSPNoCheckExtension)
                getExtension(OCSPNoCheckExtension.OID, info);


        if(ext == null)
        {
            try {
                populate(null,info);

            } catch (EProfileException e) {
                 throw new EPropertyException(CMS.getUserMessage(
                      locale, "CMS_INVALID_PROPERTY", name));
            }

        }

        if (name.equals(VAL_CRITICAL)) {
            ext = (OCSPNoCheckExtension)
                getExtension(OCSPNoCheckExtension.OID, info);
            boolean val = Boolean.valueOf(value).booleanValue();

            if(ext == null)  {
               return;
            }
            ext.setCritical(val);
        } else {
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", name));
        }
    }

    public String getValue(String name, Locale locale,
        X509CertInfo info)
        throws EPropertyException {
        if (name == null) {
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", name));
        }

        OCSPNoCheckExtension ext = (OCSPNoCheckExtension)
                getExtension(OCSPNoCheckExtension.OID, info);

        if(ext == null)
        {
            try {
                populate(null,info);

            } catch (EProfileException e) {
                 throw new EPropertyException(CMS.getUserMessage(
                      locale, "CMS_INVALID_PROPERTY", name));
            }

        }

        if (name.equals(VAL_CRITICAL)) {
            ext = (OCSPNoCheckExtension)
                getExtension(OCSPNoCheckExtension.OID, info);

            if (ext == null) {
                return null;
            }
            if (ext.isCritical()) {
                return "true";
            } else {
                return "false";
            }
        } else {
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", name));
        }
    }

    public String getText(Locale locale) {
        return CMS.getUserMessage(locale, "CMS_PROFILE_DEF_OCSP_NO_CHECK_EXT",
                getConfig(CONFIG_CRITICAL));
    }

    /**
     * Populates the request with this policy default.
     */
    public void populate(IRequest request, X509CertInfo info)
        throws EProfileException {
        OCSPNoCheckExtension ext = createExtension();

        addExtension(OCSPNoCheckExtension.OID, ext, info);
    }

    public OCSPNoCheckExtension createExtension() {
        OCSPNoCheckExtension ext = null; 

        try {
            ext = new OCSPNoCheckExtension();
        } catch (Exception e) {
            CMS.debug("OCSPNoCheckExtDefault:  createExtension " +
                e.toString());
            return null;
        }
        boolean critical = getConfigBoolean(CONFIG_CRITICAL);

        ext.setCritical(critical);
        return ext;
    }
}
