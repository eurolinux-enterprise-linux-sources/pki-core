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


import java.util.*;

import org.mozilla.jss.pkix.crmf.*;
import org.mozilla.jss.CryptoManager;
import org.mozilla.jss.crypto.CryptoToken;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.profile.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.apps.*;

import com.netscape.cms.profile.common.*;


/**
 * This class implements the base enrollment input.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public abstract class EnrollInput implements IProfileInput { 

    private final static String LOGGING_SIGNED_AUDIT_PROOF_OF_POSSESSION =
        "LOGGING_SIGNED_AUDIT_PROOF_OF_POSSESSION_2";

    protected IConfigStore mConfig = null;
    protected Vector mValueNames = new Vector();
    protected Vector mConfigNames = new Vector();
    protected IProfile mProfile = null;

    protected ILogger mSignedAuditLogger = CMS.getSignedAuditLogger();
 
    /**
     * Initializes this default policy.
     */
    public void init(IProfile profile, IConfigStore config)
        throws EProfileException {
        mConfig = config;
        mProfile = profile;
    }

    public IConfigStore getConfigStore() {
        return mConfig;
    }

    /**
     * Populates the request with this policy default.
     *
     * @param ctx profile context
     * @param request request
     * @exception EProfileException failed to populate
     */
    public abstract void populate(IProfileContext ctx, IRequest request)
        throws EProfileException;

    /**
     * Retrieves the localizable name of this policy.
     *
     * @param locale user locale
     * @return localized input name
     */
    public abstract String getName(Locale locale);

    /**
     * Retrieves the localizable description of this policy.
     *
     * @param locale user locale
     * @return localized input description
     */
    public abstract String getText(Locale locale);

    /**
     * Retrieves the descriptor of the given value
     * property by name.
     *
     * @param locale user locale
     * @param name property name
     * @return descriptor of the property
     */
    public abstract IDescriptor getValueDescriptor(Locale locale, String name);


    public void addValueName(String name) {
        mValueNames.addElement(name);
    }

    /**
     * Retrieves a list of names of the value parameter.
     */
    public Enumeration getValueNames() {
        return mValueNames.elements();
    }

    public void addConfigName(String name) {
        mConfigNames.addElement(name);
    }

    public Enumeration getConfigNames() {
        return mConfigNames.elements();
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
            if (mConfig == null) {
                return null;
	    }
            if (mConfig.getSubStore("params") != null) {
                return mConfig.getSubStore("params").getString(name);
            }
        } catch (EBaseException e) {
        }
        return "";
    }

    public String getDefaultConfig(String name) {
        return null;
    }

    public String getValue(String name, Locale locale, IRequest request)
        throws EProfileException {
        return request.getExtDataInString(name);
    }

    /**
     * Sets the value of the given value parameter by name.
     */
    public void setValue(String name, Locale locale, IRequest request,
        String value) throws EPropertyException {
        request.setExtData(name, value);
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

    public IDescriptor getConfigDescriptor(Locale locale, String name) {
        return null;
    }

    public void verifyPOP(Locale locale, CertReqMsg certReqMsg) 
        throws EProfileException {
        CMS.debug("EnrollInput ::in verifyPOP"); 

        String auditMessage = null;
        String auditSubjectID = auditSubjectID();

        if (!certReqMsg.hasPop()) { 
            CMS.debug("CertReqMsg has not POP, return");
            return; 
        }
        ProofOfPossession pop = certReqMsg.getPop();
        ProofOfPossession.Type popType = pop.getType();

        if (popType != ProofOfPossession.SIGNATURE) {
            CMS.debug("not POP SIGNATURE, return");
            return;
        }

        try {
            if (CMS.getConfigStore().getBoolean("cms.skipPOPVerify", false)) {
              CMS.debug("skipPOPVerify on, return");
              return;
            }
            CMS.debug("POP verification begins:");
            CryptoManager cm = CryptoManager.getInstance();
            String tokenName = CMS.getConfigStore().getString("ca.requestVerify.token",
                   "Internal Key Storage Token");
            CryptoToken verifyToken = cm.getTokenByName(tokenName);
            if (tokenName.equals("Internal Key Storage Token")) {
                //use internal token
                CMS.debug("POP verification using internal token");
                certReqMsg.verify();
            } else {
                CMS.debug("POP verification using token:"+ tokenName);
                certReqMsg.verify(verifyToken);
            }

            // store a message in the signed audit log file
            auditMessage = CMS.getLogMessage(
              LOGGING_SIGNED_AUDIT_PROOF_OF_POSSESSION,
              auditSubjectID,
              ILogger.SUCCESS );
            audit( auditMessage );
        } catch (Exception e) {

            CMS.debug("Failed POP verify! "+e.toString());
            CMS.debug(e);

            // store a message in the signed audit log file
            auditMessage = CMS.getLogMessage(
              LOGGING_SIGNED_AUDIT_PROOF_OF_POSSESSION,
              auditSubjectID,
              ILogger.FAILURE );

            audit( auditMessage );

            throw new EProfileException(CMS.getUserMessage(locale, 
                        "CMS_POP_VERIFICATION_ERROR"));
        }
    }

    /**
     * Signed Audit Log
     *
     * This method is inherited by all extended "CMSServlet"s,
     * and is called to store messages to the signed audit log.
     * <P>
     *
     * @param msg signed audit log message
     */
    protected void audit(String msg) {
        // in this case, do NOT strip preceding/trailing whitespace
        // from passed-in String parameters

        if (mSignedAuditLogger == null) {
            return;
        }

        mSignedAuditLogger.log(ILogger.EV_SIGNED_AUDIT,
            null,
            ILogger.S_SIGNED_AUDIT,
            ILogger.LL_SECURITY,
            msg);
    }

    /**
     * Signed Audit Log Subject ID
     *
     * This method is inherited by all extended "CMSServlet"s,
     * and is called to obtain the "SubjectID" for
     * a signed audit log message.
     * <P>
     *
     * @return id string containing the signed audit log message SubjectID
     */
    protected String auditSubjectID() {
        // if no signed audit object exists, bail
        if (mSignedAuditLogger == null) {
            return null;
        }

        String subjectID = null;

        // Initialize subjectID
        SessionContext auditContext = SessionContext.getExistingContext();

        if (auditContext != null) {
            subjectID = (String)
                    auditContext.get(SessionContext.USER_ID);

            if (subjectID != null) {
                subjectID = subjectID.trim();
            } else {
                subjectID = ILogger.NONROLEUSER;
            }
        } else {
            subjectID = ILogger.UNIDENTIFIED;
        }

        return subjectID;
    }
}
