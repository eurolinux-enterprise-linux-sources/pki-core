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
import java.security.cert.*;
import java.util.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.profile.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.apps.*;

import netscape.security.x509.*;
import netscape.security.extensions.*;
import netscape.security.util.*;
import netscape.security.x509.CRLDistributionPointsExtension.Reason;
import com.netscape.cms.profile.common.*;


/**
 * This class implements an enrollment default policy
 * that populates a CRL Distribution points extension
 * into the certificate template.
 *
 * @version $Revision: 1520 $, $Date: 2010-11-17 11:52:43 -0800 (Wed, 17 Nov 2010) $
 */
public class CRLDistributionPointsExtDefault extends EnrollExtDefault {

    public static final String CONFIG_CRITICAL = "crlDistPointsCritical";
    public static final String CONFIG_NUM_POINTS = "crlDistPointsNum";
    public static final String CONFIG_POINT_TYPE = "crlDistPointsPointType_";
    public static final String CONFIG_POINT_NAME = "crlDistPointsPointName_";
    public static final String CONFIG_REASONS = "crlDistPointsReasons_";
    public static final String CONFIG_ISSUER_TYPE = "crlDistPointsIssuerType_";
    public static final String CONFIG_ISSUER_NAME = "crlDistPointsIssuerName_";
    public static final String CONFIG_ENABLE = "crlDistPointsEnable_";

    public static final String VAL_CRITICAL = "crlDistPointsCritical";
    public static final String VAL_CRL_DISTRIBUTION_POINTS = "crlDistPointsValue";

    private static final String REASONS = "Reasons";
    private static final String POINT_TYPE = "Point Type";
    private static final String POINT_NAME = "Point Name";
    private static final String ISSUER_TYPE = "Issuer Type";
    private static final String ISSUER_NAME = "Issuer Name";
    private static final String ENABLE = "Enable";

    private static final String RELATIVETOISSUER = "RelativeToIssuer";

    private static final int DEF_NUM_POINTS = 1;
    private static final int MAX_NUM_POINTS = 100;

    public CRLDistributionPointsExtDefault() {
        super();
    }

    public void init(IProfile profile, IConfigStore config)
        throws EProfileException {
        super.init(profile, config);
        refreshConfigAndValueNames();
    }

     public void setConfig(String name, String value)
        throws EPropertyException {
        int num = 0;
        if (name.equals(CONFIG_NUM_POINTS)) {
          try {
            num = Integer.parseInt(value);

            if (num >= MAX_NUM_POINTS || num < 0) {
                throw new EPropertyException(CMS.getUserMessage(
                            "CMS_INVALID_PROPERTY", CONFIG_NUM_POINTS));
            }

          } catch (Exception e) {
                throw new EPropertyException(CMS.getUserMessage(
                            "CMS_INVALID_PROPERTY", CONFIG_NUM_POINTS));
          }
        }
        super.setConfig(name, value);
    }


    public Enumeration getConfigNames() {
        refreshConfigAndValueNames();
        return super.getConfigNames();
    }

    protected void refreshConfigAndValueNames() {
        super.refreshConfigAndValueNames();

        addValueName(VAL_CRITICAL);
        addValueName(VAL_CRL_DISTRIBUTION_POINTS);

        addConfigName(CONFIG_CRITICAL);
        int num = getNumPoints();

        addConfigName(CONFIG_NUM_POINTS);
        for (int i = 0; i < num; i++) {
            addConfigName(CONFIG_POINT_TYPE + i);
            addConfigName(CONFIG_POINT_NAME + i);
            addConfigName(CONFIG_REASONS + i);
            addConfigName(CONFIG_ISSUER_TYPE + i);
            addConfigName(CONFIG_ISSUER_NAME + i);
            addConfigName(CONFIG_ENABLE + i);
        }
    }

    protected int getNumPoints() {
        int num = DEF_NUM_POINTS;
        String val = getConfig(CONFIG_NUM_POINTS);

        if (val != null) {
            try {
                num = Integer.parseInt(val);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (num >= MAX_NUM_POINTS) 
            num = DEF_NUM_POINTS;

        return num;
    }

    public IDescriptor getConfigDescriptor(Locale locale, String name) { 
        if (name.equals(CONFIG_CRITICAL)) { 
            return new Descriptor(IDescriptor.BOOLEAN, null, 
                    "false",
                    CMS.getUserMessage(locale, "CMS_PROFILE_CRITICAL"));
        } else if (name.startsWith(CONFIG_POINT_TYPE)) {
            return new Descriptor(IDescriptor.STRING, null, 
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_POINT_TYPE"));
        } else if (name.startsWith(CONFIG_POINT_NAME)) {
            return new Descriptor(IDescriptor.STRING, null, 
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_POINT_NAME"));
        } else if (name.startsWith(CONFIG_REASONS)) {
            return new Descriptor(IDescriptor.STRING, null, 
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_REASONS"));
        } else if (name.startsWith(CONFIG_ISSUER_TYPE)) {
            return new Descriptor(IDescriptor.STRING, null, 
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_ISSUER_TYPE"));
        } else if (name.startsWith(CONFIG_ISSUER_NAME)) {
            return new Descriptor(IDescriptor.STRING, null, 
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_ISSUER_NAME"));
        } else if (name.startsWith(CONFIG_ENABLE)) {
            return new Descriptor(IDescriptor.BOOLEAN, null, 
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_ENABLE"));
        } else if (name.startsWith(CONFIG_NUM_POINTS)) {
            return new Descriptor(IDescriptor.INTEGER, null,
                    "1",
                    CMS.getUserMessage(locale, "CMS_PROFILE_NUM_DIST_POINTS"));

        } else {
            return null;
        }
    }

    public IDescriptor getValueDescriptor(Locale locale, String name) {
        if (name.equals(VAL_CRITICAL)) { 
            return new Descriptor(IDescriptor.BOOLEAN, null, 
                    "false",
                    CMS.getUserMessage(locale, "CMS_PROFILE_CRITICAL"));
        } else if (name.equals(VAL_CRL_DISTRIBUTION_POINTS)) {
            return new Descriptor(IDescriptor.STRING_LIST, null, 
                    null,
                    CMS.getUserMessage(locale, "CMS_PROFILE_CRL_DISTRIBUTION_POINTS"));
        } else {
            return null;
        }
    }

    public void setValue(String name, Locale locale,
        X509CertInfo info, String value)
        throws EPropertyException {
        try {
            CRLDistributionPointsExtension ext = null;

            if (name == null) { 
                throw new EPropertyException(CMS.getUserMessage( 
                            locale, "CMS_INVALID_PROPERTY", name));
            }

            ext = (CRLDistributionPointsExtension)
                        getExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(),
                            info);

            if(ext == null)  {
                populate(locale,info); 
            }

            if (name.equals(VAL_CRITICAL)) {
                ext = (CRLDistributionPointsExtension)
                        getExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(), 
                            info);
                boolean val = Boolean.valueOf(value).booleanValue();

                if(ext == null)
                {
                    return;
                }
                ext.setCritical(val); 
            } else if (name.equals(VAL_CRL_DISTRIBUTION_POINTS)) { 
                ext = (CRLDistributionPointsExtension)
                        getExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(), 
                            info);

                if(ext == null)
                {
                    return;
                }
                Vector v = parseRecords(value);
                int size = v.size();
              
                boolean critical = ext.isCritical();
                int i = 0;

                for (; i < size; i++) {
                    NameValuePairs nvps = (NameValuePairs) v.elementAt(i);
                    Enumeration names = nvps.getNames();
                    String pointType = null;
                    String pointValue = null;
                    String issuerType = null;
                    String issuerValue = null;
                    String enable = null;
                    CRLDistributionPoint cdp = new CRLDistributionPoint();

                    while (names.hasMoreElements()) {
                        String name1 = (String) names.nextElement();

                        if (name1.equals(REASONS)) {
                            addReasons(locale, cdp, REASONS, nvps.getValue(name1));
                        } else if (name1.equals(POINT_TYPE)) {
                            pointType = nvps.getValue(name1);
                        } else if (name1.equals(POINT_NAME)) {
                            pointValue = nvps.getValue(name1);
                        } else if (name1.equals(ISSUER_TYPE)) {
                            issuerType = nvps.getValue(name1);
                        } else if (name1.equals(ISSUER_NAME)) {
                            issuerValue = nvps.getValue(name1);
                        } else if (name1.equals(ENABLE)) {
                            enable = nvps.getValue(name1);
                        }
                    }

                    if (enable != null && enable.equals("true")) {
                        if (pointType != null)
                            addCRLPoint(locale, cdp, pointType, pointValue);
                        if (issuerType != null)
                            addIssuer(locale, cdp, issuerType, issuerValue);

                            // this is the first distribution point
                        if (i == 0) {
                            ext = new CRLDistributionPointsExtension(cdp);
                            ext.setCritical(critical);
                        } else {
                            ext.addPoint(cdp);
                        }
                    }
                }
            } else {
                throw new EPropertyException(CMS.getUserMessage( 
                            locale, "CMS_INVALID_PROPERTY", name));
            }

            replaceExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(),
                ext, info);
        } catch (EProfileException e) {
            CMS.debug("CRLDistributionPointsExtDefault: setValue " + 
                e.toString());
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", name));
        }
    }

    private void addCRLPoint(Locale locale, CRLDistributionPoint cdp, String type,
        String value) throws EPropertyException {
        try {
            if (value == null || value.length() == 0)
                return;
        
            if (type.equals(RELATIVETOISSUER)) {
                cdp.setRelativeName(new RDN(value));
            } else if (isGeneralNameType(type)) {
                GeneralNames gen = new GeneralNames();
                gen.addElement(parseGeneralName(type,value));
                cdp.setFullName(gen);
            } else {
                throw new EPropertyException(CMS.getUserMessage( 
                            locale, "CMS_INVALID_PROPERTY", type));
            }
        } catch (IOException e) {
            CMS.debug("CRLDistributionPointsExtDefault: addCRLPoint " + 
                e.toString());
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", type));
        } catch (GeneralNamesException e) {
            CMS.debug("CRLDistributionPointsExtDefault: addCRLPoint " + 
                e.toString());
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", type));
        }
    }

    private void addIssuer(Locale locale, CRLDistributionPoint cdp, String type,
        String value) throws EPropertyException {
        if (value == null || value.length() == 0)
            return;
        try {
            if (isGeneralNameType(type)) {
                GeneralNames gen = new GeneralNames();

                gen.addElement(parseGeneralName(type, value));
                cdp.setCRLIssuer(gen);
            } else {
                throw new EPropertyException(CMS.getUserMessage( 
                            locale, "CMS_INVALID_PROPERTY", type));
            }
        } catch (IOException e) {
            CMS.debug("CRLDistributionPointsExtDefault: addIssuer " + 
                e.toString());
        } catch (GeneralNamesException e) {
            CMS.debug("CRLDistributionPointsExtDefault: addIssuer " + 
                e.toString());
        }
    }

    private void addReasons(Locale locale, CRLDistributionPoint cdp, String type, 
        String value) throws EPropertyException {
        if (value == null || value.length() == 0)
            return;
        if (type.equals(REASONS)) {
            if (value != null && !value.equals("")) {
                StringTokenizer st = new StringTokenizer(value, ", \t");
                byte reasonBits = 0;

                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    Reason r = Reason.fromString(s);

                    if (r == null) {
                        CMS.debug("CRLDistributeionPointsExtDefault: addReasons Unknown reason: " + s);
                        throw new EPropertyException(CMS.getUserMessage( 
                                    locale, "CMS_INVALID_PROPERTY", s));
                    } else {
                        reasonBits |= r.getBitMask();
                    }
                }

                if (reasonBits != 0) {
                    BitArray ba = new BitArray(8, new byte[] {reasonBits}
                        );

                    cdp.setReasons(ba);
                }
            }
        } else {
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", type));
        }
    }

    public String getValue(String name, Locale locale,
        X509CertInfo info)
        throws EPropertyException {
        CRLDistributionPointsExtension ext = null;

        if (name == null) {
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", name));
        }

        ext = (CRLDistributionPointsExtension)
                    getExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(),
                        info);

        if(ext == null)
        {
            try {
                populate(locale,info);

            } catch (EProfileException e) {
                 throw new EPropertyException(CMS.getUserMessage(
                      locale, "CMS_INVALID_PROPERTY", name));
            }
        }

        if (name.equals(VAL_CRITICAL)) {
            ext = (CRLDistributionPointsExtension)
                    getExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(), 
                        info);

            if (ext == null) {
                return null;
            }
            if (ext.isCritical()) {
                return "true";
            } else {
                return "false";
            }
        } else if (name.equals(VAL_CRL_DISTRIBUTION_POINTS)) { 
            ext = (CRLDistributionPointsExtension)
                    getExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(), 
                        info);

            if (ext == null)
                return "";

            StringBuffer sb = new StringBuffer();

            Vector recs = new Vector();
            int num = getNumPoints();

            for (int i = 0; i < num; i++) {
                NameValuePairs pairs = null;

                if (i < ext.getNumPoints()) {
                    CRLDistributionPoint p = ext.getPointAt(i); 
                    GeneralNames gns = p.getFullName();

                    pairs = buildGeneralNames(gns, p);
                    recs.addElement(pairs);
                } else {
                    pairs = buildEmptyGeneralNames();
                    recs.addElement(pairs);
                }
            }
                
            return buildRecords(recs);
        } else {
            throw new EPropertyException(CMS.getUserMessage( 
                        locale, "CMS_INVALID_PROPERTY", name));
        }
    }

    protected NameValuePairs buildEmptyGeneralNames() {
        NameValuePairs pairs = new NameValuePairs();

        pairs.add(POINT_TYPE, "");
        pairs.add(POINT_NAME, "");
        pairs.add(REASONS, "");
        pairs.add(ISSUER_TYPE, "");
        pairs.add(ISSUER_NAME, "");
        pairs.add(ENABLE, "false");
        return pairs;
    }

    protected NameValuePairs buildGeneralNames(GeneralNames gns, CRLDistributionPoint p)
        throws EPropertyException {

        NameValuePairs pairs = new NameValuePairs();

        RDN rdn = null;
        boolean hasFullName = false;

        pairs.add(ENABLE, "true");
        if (gns == null) {
            rdn = p.getRelativeName();
            if (rdn != null) {
                hasFullName = true;
                pairs.add(POINT_TYPE, RELATIVETOISSUER);
                pairs.add(POINT_NAME, rdn.toString());
            } else {
                pairs.add(POINT_TYPE, "");
                pairs.add(POINT_NAME, "");
            }
        } else {
            GeneralName gn = (GeneralName) gns.elementAt(0);

            if (gn != null) {
                hasFullName = true;
                int type = gn.getType();

                pairs.add(POINT_TYPE, getGeneralNameType(gn));
                pairs.add(POINT_NAME, getGeneralNameValue(gn));
            }
        }

        if (!hasFullName) {
            pairs.add(POINT_TYPE, GN_DIRECTORY_NAME);
            pairs.add(POINT_NAME, "");
        }

        BitArray reasons = p.getReasons();
        String s = convertBitArrayToReasonNames(reasons);

        if (s.length() > 0) {
            pairs.add(REASONS, s);
        } else {
            pairs.add(REASONS, "");
        }

        gns = p.getCRLIssuer();

        if (gns == null) {
            pairs.add(ISSUER_TYPE, GN_DIRECTORY_NAME);
            pairs.add(ISSUER_NAME, "");
        } else {
            GeneralName gn = (GeneralName) gns.elementAt(0);

            if (gn != null) {
                hasFullName = true;
                int type = gn.getType();

                pairs.add(ISSUER_TYPE, getGeneralNameType(gn));
                pairs.add(ISSUER_NAME, getGeneralNameValue(gn));
            }
        }
        return pairs;
    }

    private String convertBitArrayToReasonNames(BitArray reasons) {
        StringBuffer sb = new StringBuffer();

        if (reasons != null) {
            byte[] b = reasons.toByteArray();
            Reason[] reasonArray = Reason.bitArrayToReasonArray(b);
    
            for (int i = 0; i < reasonArray.length; i++) {
                if (sb.length() > 0)
                    sb.append(",");
                sb.append(reasonArray[i].getName());
            }
        }
 
        return sb.toString();
    }

    public String getText(Locale locale) {
        StringBuffer sb = new StringBuffer();
        int num = getNumPoints();

        for (int i = 0; i < num; i++) {
            sb.append("Record #");
            sb.append(i);
            sb.append("{");
            sb.append(POINT_TYPE + ":");
            sb.append(getConfig(CONFIG_POINT_TYPE + i));
            sb.append(",");
            sb.append(POINT_NAME + ":");
            sb.append(getConfig(CONFIG_POINT_NAME + i));
            sb.append(",");
            sb.append(REASONS + ":");
            sb.append(getConfig(CONFIG_REASONS + i));
            sb.append(",");
            sb.append(ISSUER_TYPE + ":");
            sb.append(getConfig(CONFIG_ISSUER_TYPE + i));
            sb.append(",");
            sb.append(ISSUER_NAME + ":");
            sb.append(getConfig(CONFIG_ISSUER_NAME + i));
            sb.append(",");
            sb.append(ENABLE + ":");
            sb.append(getConfig(CONFIG_ENABLE + i));
            sb.append("}");
        }
        return CMS.getUserMessage(locale, 
                "CMS_PROFILE_DEF_CRL_DIST_POINTS_EXT", 
                getConfig(CONFIG_CRITICAL),
                sb.toString());
    }

    /**
     * Populates the request with this policy default.
     */
    private void populate(Locale locale, X509CertInfo info)
        throws EProfileException {
        CRLDistributionPointsExtension ext = createExtension(locale);

        if (ext == null)
            return;
        addExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(),
            ext, info);
    }
    /**
     * Populates the request with this policy default.
     */
    public void populate(IRequest request, X509CertInfo info)
        throws EProfileException {
        CRLDistributionPointsExtension ext = createExtension(request);

        if (ext == null)
            return;
        addExtension(PKIXExtensions.CRLDistributionPoints_Id.toString(), 
            ext, info);
    }

    public CRLDistributionPointsExtension createExtension(IRequest request) {
        CRLDistributionPointsExtension ext = null; 
        int num = 0;

        try {
            boolean critical = getConfigBoolean(CONFIG_CRITICAL);

            num = getNumPoints();
            for (int i = 0; i < num; i++) {
                CRLDistributionPoint cdp = new CRLDistributionPoint();

                String enable = getConfig(CONFIG_ENABLE + i); 
                String pointType = getConfig(CONFIG_POINT_TYPE + i); 
                String pointName = getConfig(CONFIG_POINT_NAME + i);
                String reasons = getConfig(CONFIG_REASONS + i);
                String issuerType = getConfig(CONFIG_ISSUER_TYPE + i);
                String issuerName = getConfig(CONFIG_ISSUER_NAME + i);

                if (enable != null && enable.equals("true")) {
                    if (pointType != null)
                        addCRLPoint(getLocale(request), cdp, pointType, pointName);
                    if (issuerType != null)
                        addIssuer(getLocale(request), cdp, issuerType, issuerName);
                    if (reasons != null)
                      addReasons(getLocale(request), cdp, REASONS, reasons);

                    if (i == 0) {
                        ext = new CRLDistributionPointsExtension(cdp);
                        ext.setCritical(critical);
                    } else {
                        ext.addPoint(cdp);
                    }
                }
            }
        } catch (Exception e) {
            CMS.debug("CRLDistribtionPointsExtDefault: createExtension " +
                e.toString());
            CMS.debug(e);
        }

        return ext;
    }

    private CRLDistributionPointsExtension createExtension(Locale locale) {
        CRLDistributionPointsExtension ext = null;
        int num = 0;

        try {
            boolean critical = getConfigBoolean(CONFIG_CRITICAL);

            num = getNumPoints();
            for (int i = 0; i < num; i++) {
                CRLDistributionPoint cdp = new CRLDistributionPoint();

                String enable = getConfig(CONFIG_ENABLE + i);
                String pointType = getConfig(CONFIG_POINT_TYPE + i);
                String pointName = getConfig(CONFIG_POINT_NAME + i);
                String reasons = getConfig(CONFIG_REASONS + i);
                String issuerType = getConfig(CONFIG_ISSUER_TYPE + i);
                String issuerName = getConfig(CONFIG_ISSUER_NAME + i);

                if (enable != null && enable.equals("true")) {
                    if (pointType != null)
                        addCRLPoint(locale, cdp, pointType, pointName);
                    if (issuerType != null)
                        addIssuer(locale, cdp, issuerType, issuerName);
                    addReasons(locale, cdp, REASONS, reasons);

                    if (i == 0) {
                        ext = new CRLDistributionPointsExtension(cdp);
                        ext.setCritical(critical);
                    } else {
                        ext.addPoint(cdp);
                    }
                }
            }
        } catch (Exception e) {
            CMS.debug("CRLDistribtionPointsExtDefault: createExtension " +
                e.toString());
            CMS.debug(e);
        }

        return ext;
    }
}
