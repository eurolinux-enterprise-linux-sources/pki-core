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
package com.netscape.cms.profile.common;


import java.security.cert.*;
import java.math.*;
import java.util.*;
import java.io.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.connector.*;
import com.netscape.certsrv.profile.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.ca.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.logging.*;

import netscape.security.x509.*;
import netscape.security.util.*;
import netscape.security.pkcs.*;

import java.security.*;
import org.mozilla.jss.asn1.*;
import org.mozilla.jss.pkix.primitive.*;
import org.mozilla.jss.pkix.crmf.*;


/**
 * This class implements a Certificate Manager enrollment
 * profile for Server Certificates.
 *
 * @version $Revision: 1604 $, $Date: 2010-12-03 09:30:10 -0800 (Fri, 03 Dec 2010) $
 */
public class ServerCertCAEnrollProfile extends CAEnrollProfile 
   implements IProfileEx {

    /**
     * Called after initialization. It populates default
     * policies, inputs, and outputs.
     */
    public void populate() throws EBaseException
    {
        // create inputs
        NameValuePairs inputParams1 = new NameValuePairs();
        IProfileInput input1 =
          createProfileInput("i1", "certReqInputImpl", inputParams1);
        NameValuePairs inputParams2 = new NameValuePairs();
        IProfileInput input2 =
          createProfileInput("i2", "submitterInfoInputImpl", inputParams2);

        // create outputs
        NameValuePairs outputParams1 = new NameValuePairs();
        IProfileOutput output1 =
          createProfileOutput("o1", "certOutputImpl", outputParams1);

        IProfilePolicy policy1 =
          createProfilePolicy("set1", "p1",
            "userSubjectNameDefaultImpl", "noConstraintImpl");
        IPolicyDefault def1 = policy1.getDefault();
        IConfigStore defConfig1 = def1.getConfigStore();
        IPolicyConstraint con1 = policy1.getConstraint();
        IConfigStore conConfig1 = con1.getConfigStore();

        IProfilePolicy policy2 =
          createProfilePolicy("set1", "p2",
            "validityDefaultImpl", "noConstraintImpl");
        IPolicyDefault def2 = policy2.getDefault();
        IConfigStore defConfig2 = def2.getConfigStore();
        defConfig2.putString("params.range","180");
        defConfig2.putString("params.startTime","0");
        IPolicyConstraint con2 = policy2.getConstraint();
        IConfigStore conConfig2 = con2.getConfigStore();

        IProfilePolicy policy3 =
          createProfilePolicy("set1", "p3",
            "userKeyDefaultImpl", "noConstraintImpl");
        IPolicyDefault def3 = policy3.getDefault();
        IConfigStore defConfig3 = def3.getConfigStore();
        defConfig3.putString("params.keyType","RSA");
        defConfig3.putString("params.keyMinLength","512");
        defConfig3.putString("params.keyMaxLength","4096");
        IPolicyConstraint con3 = policy3.getConstraint();
        IConfigStore conConfig3 = con3.getConfigStore();

        IProfilePolicy policy4 =
          createProfilePolicy("set1", "p4",
            "signingAlgDefaultImpl", "noConstraintImpl");
        IPolicyDefault def4 = policy4.getDefault();
        IConfigStore defConfig4 = def4.getConfigStore();
        defConfig4.putString("params.signingAlg","-");
        defConfig4.putString("params.signingAlgsAllowed",
          "SHA1withRSA,SHA256withRSA,SHA512withRSA,MD5withRSA,MD2withRSA,SHA1withEC,SHA256withEC,SHA384withEC,SHA512withEC");
        IPolicyConstraint con4 = policy4.getConstraint();
        IConfigStore conConfig4 = con4.getConfigStore();

        IProfilePolicy policy5 = 
          createProfilePolicy("set1", "p5", 
            "keyUsageExtDefaultImpl", "noConstraintImpl"); 
        IPolicyDefault def5 = policy5.getDefault(); 
        IConfigStore defConfig5 = def5.getConfigStore(); 
        defConfig5.putString("params.keyUsageCritical","true"); 
        defConfig5.putString("params.keyUsageCrlSign","false"); 
        defConfig5.putString("params.keyUsageDataEncipherment","true"); 
        defConfig5.putString("params.keyUsageDecipherOnly","false"); 
        defConfig5.putString("params.keyUsageDigitalSignature","true"); 
        defConfig5.putString("params.keyUsageEncipherOnly","false"); 
        defConfig5.putString("params.keyUsageKeyAgreement","false"); 
        defConfig5.putString("params.keyUsageKeyCertSign","false"); 
        defConfig5.putString("params.keyUsageKeyEncipherment","true"); 
        defConfig5.putString("params.keyUsageNonRepudiation","true"); 
        IPolicyConstraint con5 = policy5.getConstraint(); 
        IConfigStore conConfig5 = con5.getConfigStore();

    }

}
