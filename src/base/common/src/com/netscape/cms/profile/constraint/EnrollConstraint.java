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
package com.netscape.cms.profile.constraint;


import java.io.*;
import java.util.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.profile.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.apps.*;

import com.netscape.cms.profile.common.*;
import com.netscape.cms.profile.def.*;
import netscape.security.x509.*;


/**
 * This class implements the generic enrollment constraint.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public abstract class EnrollConstraint implements IPolicyConstraint {
    public static final String CONFIG_NAME = "name";

    protected IConfigStore mConfig = null;
    protected Vector mConfigNames = new Vector();

    public EnrollConstraint() {
    }

    public Enumeration getConfigNames() {
        return mConfigNames.elements();
    }

    public void addConfigName(String name) {
        mConfigNames.addElement(name);
    }

    public IDescriptor getConfigDescriptor(Locale locale, String name) {
        return null;
    }

    public IDescriptor getValueDescriptor(Locale locale, String name) {
        return null;
    }

    public Locale getLocale(IRequest request) {
        Locale locale = null;
        String language = request.getExtDataInString(
                EnrollProfile.REQUEST_LOCALE);
        if (language != null) {
            locale = new Locale(language);
        }
        return locale;
    }

    public void setConfig(String name, String value)
        throws EPropertyException {
        if (mConfig.getSubStore("params") == null) {
            //
        } else {
            mConfig.getSubStore("params").putString(name, value);
        }
    }

    public String getConfig(String name) {
        try {
            if (mConfig == null)
                return null;
            if (mConfig.getSubStore("params") != null) {
                String val = mConfig.getSubStore("params").getString(name);

                return val;
            }
        } catch (EBaseException e) {
            CMS.debug(e.toString());
        }
        return "";
    }

    public void init(IProfile profile, IConfigStore config)
        throws EProfileException {
        mConfig = config;
    }

    public IConfigStore getConfigStore() {
        return mConfig;
    } 

    /**
     * Validates the request. The request is not modified
     * during the validation.
     *
     * @param request enrollment request
     * @param info certificate template
     * @exception ERejectException request is rejected due
     *        to violation of constraint
     */
    public abstract void validate(IRequest request, X509CertInfo info)
        throws ERejectException;

    /**
     * Validates the request. The request is not modified
     * during the validation.
     *
     * The current implementation of this method calls
     * into the subclass's validate(request, info)
     * method for validation checking.
     *
     * @param request request
     * @exception ERejectException request is rejected due
     *        to violation of constraint
     */
    public void validate(IRequest request)
        throws ERejectException {
        String name = getClass().getName();

        name = name.substring(name.lastIndexOf('.') + 1);
        CMS.debug(name + ": validate start");
        X509CertInfo info =
            request.getExtDataInCertInfo(EnrollProfile.REQUEST_CERTINFO);

        validate(request, info);

        request.setExtData(EnrollProfile.REQUEST_CERTINFO, info);
        CMS.debug(name + ": validate end");
    }

    public String getText(Locale locale) {
        return "Enroll Constraint";
    }

    public String getName(Locale locale) {
        try {
            return mConfig.getString(CONFIG_NAME);
        } catch (EBaseException e) {
            return null;
        }
    }

    protected Extension getExtension(String name, X509CertInfo info) {
        CertificateExtensions exts = null;

        try {
            exts = (CertificateExtensions)
                    info.get(X509CertInfo.EXTENSIONS);
        } catch (Exception e) {
            CMS.debug("EnrollConstraint: getExtension " + e.toString());
        }
        if (exts == null)
            return null;
        Enumeration e = exts.getElements();

        while (e.hasMoreElements()) {
            Extension ext = (Extension) e.nextElement();

            if (ext.getExtensionId().toString().equals(name)) {
                return ext;
            }
        }
        return null;
    }

    protected boolean isOptional(String value) {
        if (value.equals("") || value.equals("-"))
            return true;
        else
            return false;
    }

    protected boolean getBoolean(String value) {
        return Boolean.valueOf(value).booleanValue();
    }

    protected int getInt(String value) {
        return Integer.valueOf(value).intValue();
    }

    protected boolean getConfigBoolean(String value) {
        return getBoolean(getConfig(value));
    }

    protected int getConfigInt(String value) {
        return getInt(getConfig(value));
    }

    public boolean isApplicable(IPolicyDefault def) {
        return true;
    }
}
