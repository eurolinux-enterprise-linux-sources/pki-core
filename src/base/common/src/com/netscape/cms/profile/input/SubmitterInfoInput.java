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
package com.netscape.cms.profile.input;


import java.security.cert.*;
import java.io.*;
import java.util.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.profile.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.apps.*;

import netscape.security.x509.*;
import netscape.security.util.*;
import netscape.security.pkcs.*;

import com.netscape.cms.profile.common.*;


/**
 * This class implements the submitter information
 * input that collects certificate requestor's 
 * information such as name, email and phone.
 * <p>
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class SubmitterInfoInput extends EnrollInput implements IProfileInput { 

    public static final String NAME = "requestor_name";
    public static final String EMAIL = "requestor_email";
    public static final String PHONE = "requestor_phone";

    public SubmitterInfoInput() {
        addValueName(NAME);
        addValueName(EMAIL);
        addValueName(PHONE);
    }

    /**
     * Initializes this default policy.
     */
    public void init(IProfile profile, IConfigStore config)
        throws EProfileException {
        super.init(profile, config);
    }

    /**
     * Retrieves the localizable name of this policy.
     */
    public String getName(Locale locale) {
        return CMS.getUserMessage(locale, "CMS_PROFILE_INPUT_SUBMITTER_NAME");
    }

    /**
     * Retrieves the localizable description of this policy.
     */
    public String getText(Locale locale) {
        return CMS.getUserMessage(locale, "CMS_PROFILE_INPUT_SUBMITTER_TEXT");
    }

    /**
     * Populates the request with this policy default.
     */
    public void populate(IProfileContext ctx, IRequest request)
        throws EProfileException {
        //
    }

    /**
     * Retrieves the descriptor of the given value
     * parameter by name.
     */
    public IDescriptor getValueDescriptor(Locale locale, String name) {
        if (name.equals(NAME)) {
            return new Descriptor(IDescriptor.STRING, null,
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_REQUESTOR_NAME"));
        } else if (name.equals(EMAIL)) {
            return new Descriptor(IDescriptor.STRING, null,
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_REQUESTOR_EMAIL"));
        } else if (name.equals(PHONE)) {
            return new Descriptor(IDescriptor.STRING, null,
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_REQUESTOR_PHONE"));
        }
        return null;
    }
}
