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
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.apps.*;

import netscape.security.x509.*;

public class CertInfoProfile 
{
    private Vector mDefaults = new Vector();
    private String mName = null;
    private String mID = null;
    private String mDescription = null;
    private String mProfileIDMapping = null;
    private String mProfileSetIDMapping = null;

    public CertInfoProfile(String cfg) throws Exception
    {
        IConfigStore config = CMS.createFileConfigStore(cfg);
        mID = config.getString("id");
        mName = config.getString("name");
        mDescription = config.getString("description");
        mProfileIDMapping = config.getString("profileIDMapping");
        mProfileSetIDMapping = config.getString("profileSetIDMapping");
        StringTokenizer st = new StringTokenizer(config.getString("list"), ",");
        while (st.hasMoreTokens()) {
            String id = (String)st.nextToken();
            String c = config.getString(id + ".default.class");
            try {
              /* load defaults */
              ICertInfoPolicyDefault def = (ICertInfoPolicyDefault)
                 Class.forName(c).newInstance();
              init(config.getSubStore(id + ".default"), def);
              mDefaults.addElement(def);
            } catch (Exception e) {
              CMS.debug("CertInfoProfile: " + e.toString());
            }
        }
    }

    private void init(IConfigStore config, ICertInfoPolicyDefault def)
           throws Exception
    {
      try {
        def.init(null, config);
      } catch (Exception e) {
        CMS.debug("CertInfoProfile.init: " + e.toString());
      }
    }

    public String getID()
    {
        return mID;
    }

    public String getName()
    {
        return mName;
    }

    public String getDescription()
    {
        return mDescription;
    }

    public String getProfileIDMapping()
    {
        return mProfileIDMapping;
    }

    public String getProfileSetIDMapping()
    {
        return mProfileSetIDMapping;
    }

    public void populate(X509CertInfo info)
    {
        Enumeration e1 = mDefaults.elements();
        while (e1.hasMoreElements()) {
          ICertInfoPolicyDefault def = 
               (ICertInfoPolicyDefault)e1.nextElement();
          try {
            def.populate(null /* request */, info);
          } catch (Exception e) {
            CMS.debug(e);
            CMS.debug("CertInfoProfile.populate: " + e.toString());
          }
        }
    }
}
