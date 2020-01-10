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
package com.netscape.cms.policy.constraints;


import java.io.*;

import java.util.*;
import com.netscape.certsrv.request.IRequest;
import com.netscape.certsrv.request.PolicyResult;
import com.netscape.certsrv.policy.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.common.*;
import netscape.security.x509.*;
import com.netscape.cms.policy.APolicyRule;


/**
 * Whether to allow renewal of an expired cert.
 * @version $Revision: 1226 $, $Date: 2010-08-19 14:16:41 -0700 (Thu, 19 Aug 2010) $
 * <P>
 * <PRE>
 * NOTE:  The Policy Framework has been replaced by the Profile Framework.
 * </PRE>
 * <P>
 *
 * @deprecated
 * @version $Revision: 1226 $, $Date: 2010-08-19 14:16:41 -0700 (Thu, 19 Aug 2010) $
 */
public class RenewalConstraints extends APolicyRule
    implements IRenewalPolicy, IExtendedPluginInfo {

    private static final String PROP_ALLOW_EXPIRED_CERTS = "allowExpiredCerts";
    private static final String PROP_RENEWAL_NOT_AFTER = "renewalNotAfter";

    private boolean mAllowExpiredCerts = true;
    private long mRenewalNotAfter = 0;

    public final static int DEF_RENEWAL_NOT_AFTER = 30;
    public final static long DAYS_TO_MS_FACTOR = 24L * 3600 * 1000;

    private final static Vector defConfParams = new Vector();
    static {
        defConfParams.addElement(PROP_ALLOW_EXPIRED_CERTS + "=" + true);
        defConfParams.addElement(PROP_RENEWAL_NOT_AFTER + "=" +
            DEF_RENEWAL_NOT_AFTER);
    }

    public RenewalConstraints() {
        NAME = "RenewalConstraints";
        DESC = "Whether to allow renewal of expired certs.";
    }

    public String[] getExtendedPluginInfo(Locale locale) {
        String[] params = {
                PROP_ALLOW_EXPIRED_CERTS + ";boolean;Allow a user to renew an already-expired certificate",
                PROP_RENEWAL_NOT_AFTER + ";number;Number of days since certificate expiry after which renewal request would be rejected",
                IExtendedPluginInfo.HELP_TOKEN +
                ";configuration-policyrules-renewalconstraints",
                IExtendedPluginInfo.HELP_TEXT +
                ";Permit administrator to decide policy on whether to " +
                "permit renewals for already-expired certificates"
            };

        return params;

    }

    /**
     * Initializes this policy rule.
     * <P>
     *
     * The entries probably are of the form:
     *
     *      ra.Policy.rule.<ruleName>.implName=ValidityConstraints
     *      ra.Policy.rule.<ruleName>.enable=true
     *      ra.Policy.rule.<ruleName>.allowExpiredCerts=true
     *
     * @param config	The config store reference
     */
    public void init(ISubsystem owner, IConfigStore config)
        throws EPolicyException {
        // Get min and max validity in days and configure them.
        try {
            mAllowExpiredCerts = 
                    config.getBoolean(PROP_ALLOW_EXPIRED_CERTS, true);
            String val = config.getString(PROP_RENEWAL_NOT_AFTER, null);

            if (val == null) 
                mRenewalNotAfter = DEF_RENEWAL_NOT_AFTER * DAYS_TO_MS_FACTOR;
            else {
                mRenewalNotAfter = Long.parseLong(val) * DAYS_TO_MS_FACTOR;
            }

        } catch (EBaseException e) {
            // never happen.
        }

        CMS.debug("RenewalConstraints: allow expired certs " + mAllowExpiredCerts);
    }

    /**
     * Applies the policy on the given Request.
     * <P>
     *
     * @param req	The request on which to apply policy.
     * @return The policy result object.
     */
    public PolicyResult apply(IRequest req) {
        PolicyResult result = PolicyResult.ACCEPTED;

        try {
            // Get the certificates being renwed.
            X509CertImpl[] oldCerts =
                req.getExtDataInCertArray(IRequest.OLD_CERTS);

            if (oldCerts == null) {
                setError(req, CMS.getUserMessage("CMS_POLICY_NO_OLD_CERT",
                        getInstanceName()), "");
                return PolicyResult.REJECTED;
            }
			
            if (mAllowExpiredCerts) {
                CMS.debug("checking validity of each cert");
                // check if each cert to be renewed is expired for more than 		    // allowed days.
                for (int i = 0; i < oldCerts.length; i++) {
                    X509CertInfo oldCertInfo = (X509CertInfo)
                        oldCerts[i].get(X509CertImpl.NAME + "." +
                            X509CertImpl.INFO);
                    CertificateValidity  oldValidity = (CertificateValidity)
                        oldCertInfo.get(X509CertInfo.VALIDITY);
                    Date notAfter = (Date)
                        oldValidity.get(CertificateValidity.NOT_AFTER);

                    // Is the Certificate eligible for renewal ?

                    Date now = CMS.getCurrentDate();

                    Date renewedNotAfter = new Date(notAfter.getTime() +
                            mRenewalNotAfter);

                    CMS.debug("RenewalConstraints: cert " + i + " renewedNotAfter " + renewedNotAfter + " now=" + now);

                    if (renewedNotAfter.before(now)) {
                        CMS.debug(
                            "One or more certificates is expired for more than " + (mRenewalNotAfter / DAYS_TO_MS_FACTOR) + " days");
                        String params[] = { getInstanceName(), Long.toString(mRenewalNotAfter / DAYS_TO_MS_FACTOR) };

                        setError(req, 
                            CMS.getUserMessage("CMS_POLICY_CANNOT_RENEW_EXPIRED_CERTS_AFTER_ALLOWED_PERIOD",
                                params), "");
                        return PolicyResult.REJECTED;
                    }
                }
                return PolicyResult.ACCEPTED;
            }

            CMS.debug("RenewalConstraints: checking validity of each cert");
            // check if each cert to be renewed is expired.
            for (int i = 0; i < oldCerts.length; i++) {
                X509CertInfo oldCertInfo = (X509CertInfo)
                    oldCerts[i].get(
                        X509CertImpl.NAME + "." + X509CertImpl.INFO);
                CertificateValidity  oldValidity = (CertificateValidity)
                    oldCertInfo.get(X509CertInfo.VALIDITY);
                Date notAfter = (Date)
                    oldValidity.get(CertificateValidity.NOT_AFTER);

                // Is the Certificate still valid?
                Date now = CMS.getCurrentDate();

                CMS.debug("RenewalConstraints: cert " + i + " notAfter " + notAfter + " now=" + now);
                if (notAfter.before(now)) {
                    CMS.debug(
                        "RenewalConstraints: One or more certificates is expired.");
                    String params[] = { getInstanceName() };

                    setError(req, 
                        CMS.getUserMessage("CMS_POLICY_CANNOT_RENEW_EXPIRED_CERTS",
                            params), "");
                    result = PolicyResult.REJECTED;
                    break;
                }
            }

        } catch (Exception e) {
            String params[] = {getInstanceName(), e.toString()};

            setError(req, CMS.getUserMessage("CMS_POLICY_UNEXPECTED_POLICY_ERROR", params), "");
            result = PolicyResult.REJECTED;
        }
        return result;
    }

    /**
     * Return configured parameters for a policy rule instance.
     *
     * @return nvPairs A Vector of name/value pairs.
     */
    public Vector getInstanceParams() {
        Vector confParams = new Vector();

        confParams.addElement(
            PROP_ALLOW_EXPIRED_CERTS + "=" + mAllowExpiredCerts);
        confParams.addElement(PROP_RENEWAL_NOT_AFTER + "=" +
            mRenewalNotAfter / DAYS_TO_MS_FACTOR);
        return confParams;
    }

    /**
     * Return default parameters for a policy implementation.
     *
     * @return nvPairs A Vector of name/value pairs.
     */
    public Vector getDefaultParams() {
        return defConfParams;
    }
}
