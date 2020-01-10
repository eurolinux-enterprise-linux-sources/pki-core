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
package com.netscape.cms.servlet.cert;


import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.Locale;
import java.io.IOException;
import java.math.BigInteger;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import netscape.security.x509.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.base.*;

import com.netscape.certsrv.authorization.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.ca.*;
import com.netscape.certsrv.ra.*;
import com.netscape.certsrv.request.*;

import com.netscape.certsrv.dbs.certdb.*;

import com.netscape.certsrv.authentication.*;

import com.netscape.certsrv.logging.*;

import com.netscape.certsrv.publish.*;


/**
 * Revoke a certificate with a CMC-formatted revocation request
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class CMCRevReqServlet extends CMSServlet {
    public final static String GETCERTS_FOR_CHALLENGE_REQUEST = "getCertsForChallenge";
    public static final String TOKEN_CERT_SERIAL = "certSerialToRevoke";
    // revocation templates.
    private final static String TPL_FILE = "revocationResult.template";
    public static final String CRED_CMC = "cmcRequest";
    
    private ICertificateRepository mCertDB = null;
    private String mFormPath = null;
    private IRequestQueue mQueue = null;
    private IPublisherProcessor mPublisherProcessor = null;
    private String mRequestID = null;
    private final static String REVOKE = "revoke";
    private final static String ON_HOLD = "on-hold";
    private final static int ON_HOLD_REASON = 6;
    private final static String
        LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST =
        "LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_5";
    private final static String
        LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_PROCESSED =
        "LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_PROCESSED_7";

    // http params 
    public static final String SERIAL_NO = TOKEN_CERT_SERIAL;
    public static final String REASON_CODE = "reasonCode";
    public static final String CHALLENGE_PHRASE = "challengePhrase";

    // request attributes
    public static final String SERIALNO_ARRAY = "serialNoArray";
    
    public CMCRevReqServlet() {
        super();
    }

	/**
     * initialize the servlet.
	 * @param sc servlet configuration, read from the web.xml file
	 */
    public void init(ServletConfig sc) throws ServletException {

        super.init(sc);

        String authorityId = mAuthority.getId();

        mFormPath = "/" + authorityId + "/" + TPL_FILE;

        mTemplates.remove(CMSRequest.SUCCESS);
        if (mAuthority instanceof ICertificateAuthority) {
            mCertDB = ((ICertificateAuthority) mAuthority).getCertificateRepository();
        }

        if (mAuthority instanceof ICertAuthority) {
            mPublisherProcessor = ((ICertAuthority) mAuthority).getPublisherProcessor();
        }
        mQueue = mAuthority.getRequestQueue();
        if (mOutputTemplatePath != null)
            mFormPath = mOutputTemplatePath;
    }


    /** 
	 * Process the HTTP request. 
	 *
	 * <ul>
	 * <li>http.param cmcRequest the base-64 encoded CMC request
	 * </ul>
	 * @param cmsReq the object holding the request and response information
     */
    protected void process(CMSRequest cmsReq) throws EBaseException {

        String cmcAgentSerialNumber = null;
        IArgBlock httpParams = cmsReq.getHttpParams();
        HttpServletRequest req = cmsReq.getHttpReq();
        HttpServletResponse resp = cmsReq.getHttpResp();        
        
        CMSTemplate form = null;
        Locale[] locale = new Locale[1];

CMS.debug("**** mFormPath = "+mFormPath);
        try {
            form = getTemplate(mFormPath, req, locale);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_ERROR_DISPLAY_TEMPLATE"));
            throw new ECMSGWException(CMS.getLogMessage("CMSGW_ERROR_DISPLAY_TEMPLATE"));
        }
        EBaseException error = null;

        IArgBlock header = CMS.createArgBlock();
        IArgBlock ctx = CMS.createArgBlock();
        CMSTemplateParams argSet = new CMSTemplateParams(header, ctx);
        

        String cmc = (String) httpParams.get(CRED_CMC);
        if (cmc == null) {
            throw new EMissingCredential(
				CMS.getUserMessage("CMS_AUTHENTICATION_NULL_CREDENTIAL", CRED_CMC));
        }

        IAuthToken authToken = authenticate(cmsReq);

        AuthzToken authzToken = null;
        try {
            authzToken = authorize(mAclMethod, authToken, mAuthzResourceName, "revoke");
        } catch (Exception e) {
            // do nothing for now
        }

        if (authzToken == null) {
            cmsReq.setStatus(CMSRequest.UNAUTHORIZED);
            return;
        }

        //IAuthToken authToken = getAuthToken(cmsReq);
        //Object subject = authToken.get(CMCAuth.TOKEN_CERT_SERIAL);
        //Object uid = authToken.get("uid");
        //===========================
        String authMgr = AuditFormat.NOAUTH;
        BigInteger[] serialNoArray = null;

        if (authToken != null) {
            serialNoArray = authToken.getInBigIntegerArray(TOKEN_CERT_SERIAL);
        }

        Integer reasonCode = Integer.valueOf(0); 
         if (authToken != null) {
            reasonCode = authToken.getInInteger(REASON_CODE);
        }
        RevocationReason reason = RevocationReason.fromInt(reasonCode.intValue());

        String comments = "";
        Date invalidityDate = null;
        String revokeAll = null;
        int verifiedRecordCount = 0;
        int totalRecordCount = 0;
  
        if (serialNoArray != null) {
            totalRecordCount = serialNoArray.length;
            verifiedRecordCount = serialNoArray.length;
        }
        
        X509CertImpl[] certs = null;

        //for audit log.
        String initiative = null;

        if (mAuthMgr != null && mAuthMgr.equals("CMCAuth")) {
            // request is from agent
            if (authToken != null) {
                authMgr = authToken.getInString(AuthToken.TOKEN_AUTHMGR_INST_NAME);
                String agentID = authToken.getInString("userid");

                initiative = AuditFormat.FROMAGENT + " agentID: " + agentID +
                        " authenticated by " + authMgr;
            }
        } else {
            initiative = AuditFormat.FROMUSER;
        }

        if ((serialNoArray != null) && (serialNoArray.length > 0)) {
            if (mAuthority instanceof ICertificateAuthority) {
                certs = new X509CertImpl[serialNoArray.length];

                for (int i = 0; i < serialNoArray.length; i++) {
                    certs[i] = ((ICertificateAuthority) mAuthority).getCertificateRepository().getX509Certificate(serialNoArray[i]);
                }

            } else if (mAuthority instanceof IRegistrationAuthority) {
                IRequest getCertsChallengeReq = null;

                getCertsChallengeReq = mQueue.newRequest(
                            GETCERTS_FOR_CHALLENGE_REQUEST); 
                getCertsChallengeReq.setExtData(SERIALNO_ARRAY, serialNoArray);
                mQueue.processRequest(getCertsChallengeReq);
                RequestStatus status = getCertsChallengeReq.getRequestStatus();

                if (status == RequestStatus.COMPLETE) {
                    certs = getCertsChallengeReq.getExtDataInCertArray(IRequest.OLD_CERTS);
                    header.addStringValue("request", getCertsChallengeReq.getRequestId().toString());
                    mRequestID = getCertsChallengeReq.getRequestId().toString();
                } else {
                    log(ILogger.LL_FAILURE, CMS.getLogMessage("ADMIN_SRVLT_FAIL_GET_CERT_CHALL_PWRD")); 
                }
            }

            header.addIntegerValue("totalRecordCount", serialNoArray.length);
            header.addIntegerValue("verifiedRecordCount", serialNoArray.length);

            for (int i = 0; i < serialNoArray.length; i++) {
                IArgBlock rarg = CMS.createArgBlock();

                rarg.addBigIntegerValue("serialNumber",
                    serialNoArray[i], 16);
                rarg.addStringValue("subject",
                    certs[i].getSubjectDN().toString());
                rarg.addLongValue("validNotBefore",
                    certs[i].getNotBefore().getTime() / 1000);
                rarg.addLongValue("validNotAfter",
                    certs[i].getNotAfter().getTime() / 1000);
                //argSet.addRepeatRecord(rarg);
            }

            revokeAll = "(|(certRecordId=" + serialNoArray[0].toString() + "))";
            cmcAgentSerialNumber=  authToken.getInString(IAuthManager.CRED_SSL_CLIENT_CERT);
            process(argSet, header, reasonCode.intValue(), invalidityDate, initiative, req, resp,
                verifiedRecordCount, revokeAll, totalRecordCount,
                comments, locale[0],cmcAgentSerialNumber);
            
        } else {
            header.addIntegerValue("totalRecordCount", 0);
            header.addIntegerValue("verifiedRecordCount", 0);
        }

        try {
            ServletOutputStream out = resp.getOutputStream();

            if ((serialNoArray== null) || (serialNoArray.length == 0)) {
                cmsReq.setStatus(CMSRequest.ERROR);
                EBaseException ee = new EBaseException("No matched certificate is found");

                cmsReq.setError(ee);
            } else if (error == null) {
                String xmlOutput = req.getParameter("xml");
                if (xmlOutput != null && xmlOutput.equals("true")) {
                  outputXML(resp, argSet);
                } else {
                  resp.setContentType("text/html");
                  form.renderOutput(out, argSet);
                  cmsReq.setStatus(CMSRequest.SUCCESS);
                }
            } else {
                cmsReq.setStatus(CMSRequest.ERROR);
                cmsReq.setError(error);
            }
        } catch (IOException e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_ERR_STREAM_TEMPLATE", e.toString()));
            throw new ECMSGWException(CMS.getLogMessage("CMSGW_ERROR_DISPLAY_TEMPLATE"));
        }
    }

    /**
     * get cert to revoke from agent.
     */
    private BigInteger getCertFromAgent(
        IArgBlock httpParams, X509Certificate[] certContainer)
        throws EBaseException {
        BigInteger serialno = null;
        X509Certificate cert = null;

        // get serial no
        serialno = httpParams.getValueAsBigInteger(SERIAL_NO, null);
        if (serialno == null) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_MISSING_SERIALNO_FOR_REVOKE"));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_MISSING_SERIALNO_FOR_REVOKE"));
        }

        // get cert from db if we're cert authority.
        if (mAuthority instanceof ICertificateAuthority) {
            cert = getX509Certificate(serialno);
            if (cert == null) {
                log(ILogger.LL_FAILURE, 
                    CMS.getLogMessage("CMSGW_INVALID_CERT_FOR_REVOCATION"));
                throw new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_INVALID_CERT_FOR_REVOCATION"));
            }
        }
        certContainer[0] = cert;
        return serialno;
    }

    /**
     * Revoke the specified certificate
     */
    private BigInteger getCertFromAuthMgr(
        AuthToken authToken, X509Certificate[] certContainer) 
        throws EBaseException {
        X509CertImpl cert =
            authToken.getInCert(AuthToken.TOKEN_CERT);

        if (cert == null) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_MISSING_CERTS_REVOKE_FROM_AUTHMGR"));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_MISSING_CERTS_REVOKE_FROM_AUTHMGR"));
        }
        if (mAuthority instanceof ICertificateAuthority && 
            !isCertFromCA(cert)) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_INVALID_CERT_FOR_REVOCATION"));
            throw new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_INVALID_CERT_FOR_REVOCATION"));
        }
        certContainer[0] = cert;
        BigInteger serialno = ((X509Certificate) cert).getSerialNumber();

        return serialno;
    }

    /**
     * get cert to revoke from ssl 
     */
    private BigInteger getCertFromSSL(
        HttpServletRequest req, X509CertImpl[] certContainer) 
        throws EBaseException {
        X509Certificate cert = getSSLClientCertificate(req);

        if (cert == null) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_MISSING_CERTS_REVOKE_FROM_SSL"));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_MISSING_CERTS_REVOKE_FROM_SSL"));
        }
        if (mAuthority instanceof ICertificateAuthority && 
            !isCertFromCA(cert)) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_INVALID_CERT_FOR_REVOCATION", ""));
            throw new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_INVALID_CERT_FOR_REVOCATION"));
        }
        BigInteger serialno = ((X509Certificate) cert).getSerialNumber();

        certContainer[0] = (X509CertImpl) cert;

        return serialno;
    }

    /**
     * Process cert status change request using the Certificate Management
     * protocol using CMS (CMC)
     * <P>
     *
     * (Certificate Request - an "EE" cert status change request)
     * <P>
     *
     * (Certificate Request Processed - an "EE" cert status change request)
     * <P>
     *
     * <ul>
     * <li>signed.audit LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST used when
     * a cert status change request (e. g. - "revocation") is made (before
     * approval process)
     * <li>signed.audit LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_PROCESSED
     * used when a certificate status is changed (revoked, expired, on-hold,
     * off-hold)
     * </ul>
     * @param argSet CMS template parameters
     * @param header argument block
     * @param reason revocation reason (0 - Unspecified, 1 - Key compromised,
     * 2 - CA key compromised; should not be used, 3 - Affiliation changed,
     * 4 - Certificate superceded, 5 - Cessation of operation, or
     * 6 - Certificate is on hold)
     * @param invalidityDate certificate validity date
     * @param initiative string containing the audit format
     * @param req HTTP servlet request
     * @param resp HTTP servlet response
     * @param verifiedRecordCount number of verified records
     * @param revokeAll string containing information on all of the
     * certificates to be revoked
     * @param totalRecordCount total number of records (verified and unverified)
     * @param comments string containing certificate comments
     * @param locale the system locale
     * @exception EBaseException an error has occurred
     */
    private void process(CMSTemplateParams argSet, IArgBlock header,
        int reason, Date invalidityDate,
        String initiative,
        HttpServletRequest req,
        HttpServletResponse resp,
        int verifiedRecordCount,
        String revokeAll,
        int totalRecordCount,
        String comments,
        Locale locale,String cmcAgentSerialNumber)
        throws EBaseException {
        String eeSerialNumber = null;
        if(cmcAgentSerialNumber!=null) {
            eeSerialNumber = cmcAgentSerialNumber;
        }else{
            X509CertImpl sslCert = ( X509CertImpl ) getSSLClientCertificate( req );
            if( sslCert != null ) {
                eeSerialNumber = sslCert.getSerialNumber().toString();
            }
        }

        boolean auditRequest = true;
        String auditMessage = null;
        String auditSubjectID = auditSubjectID();
        String auditRequesterID = auditRequesterID( req );
        String auditSerialNumber = auditSerialNumber( eeSerialNumber );
        String auditRequestType = auditRequestType( reason );
        String auditApprovalStatus = ILogger.SIGNED_AUDIT_EMPTY_VALUE;
        String auditReasonNum = String.valueOf( reason );

        try {
            int count = 0;
            Vector oldCertsV = new Vector();
            Vector revCertImplsV = new Vector();

            // Construct a CRL reason code extension.
            RevocationReason revReason = RevocationReason.fromInt(reason);
            CRLReasonExtension crlReasonExtn = new CRLReasonExtension(revReason);

            // Construct a CRL invalidity date extension.
            InvalidityDateExtension invalidityDateExtn = null;

            if (invalidityDate != null) {
                invalidityDateExtn = new InvalidityDateExtension(invalidityDate);
            }

            // Construct a CRL extension for this request.
            CRLExtensions entryExtn = new CRLExtensions();

            if (crlReasonExtn != null) {
                entryExtn.set(crlReasonExtn.getName(), crlReasonExtn);
            }
            if (invalidityDateExtn != null) {
                entryExtn.set(invalidityDateExtn.getName(), invalidityDateExtn);
            }

            if (mAuthority instanceof ICertificateAuthority) {
                ICertRecordList list = (ICertRecordList) mCertDB.findCertRecordsInList(
                        revokeAll, null, totalRecordCount);
                Enumeration e = list.getCertRecords(0, totalRecordCount - 1);

                while (e != null && e.hasMoreElements()) {
                    ICertRecord rec = (ICertRecord) e.nextElement();
                    X509CertImpl cert = rec.getCertificate();
                    IArgBlock rarg = CMS.createArgBlock();

                    rarg.addBigIntegerValue("serialNumber",
                        cert.getSerialNumber(), 16);

                    if (rec.getStatus().equals(ICertRecord.STATUS_REVOKED)) {
                        rarg.addStringValue("error", "Certificate " +
                            cert.getSerialNumber().toString() +
                            " is already revoked.");
                    } else {
                        oldCertsV.addElement(cert);

                        RevokedCertImpl revCertImpl =
                            new RevokedCertImpl(cert.getSerialNumber(),
                                CMS.getCurrentDate(), entryExtn);

                        revCertImplsV.addElement(revCertImpl);
                        count++;
                        rarg.addStringValue("error", null);
                    }
                    argSet.addRepeatRecord(rarg);
                }

            } else if (mAuthority instanceof IRegistrationAuthority) {
                String reqIdStr = null;

                if (mRequestID != null && mRequestID.length() > 0) 
                    reqIdStr = mRequestID;
                Vector serialNumbers = new Vector();

                if (revokeAll != null && revokeAll.length() > 0) {
                    for (int i = revokeAll.indexOf('=');
                        i < revokeAll.length() && i > -1;
                        i = revokeAll.indexOf('=', i)) {
                        if (i > -1) {
                            i++;
                            while (i < revokeAll.length() && revokeAll.charAt(i) == ' ') {
                                i++;
                            }
                            String legalDigits = "0123456789";
                            int j = i;

                            while (j < revokeAll.length() && 
                                legalDigits.indexOf(revokeAll.charAt(j)) != -1) {
                                j++;
                            }
                            if (j > i) {
                                serialNumbers.addElement(revokeAll.substring(i, j));
                            }
                        }
                    }
                }
                if (reqIdStr != null && reqIdStr.length() > 0 && serialNumbers.size() > 0) {
                    IRequest certReq = mRequestQueue.findRequest(new RequestId(reqIdStr));
                    X509CertImpl[] certs = certReq.getExtDataInCertArray(IRequest.OLD_CERTS);

                    for (int i = 0; i < certs.length; i++) {
                        boolean addToList = false;

                        for (int j = 0; j < serialNumbers.size(); j++) {
                            if (certs[i].getSerialNumber().toString().equals(
                                    (String) serialNumbers.elementAt(j))) {
                                addToList = true;
                                break;
                            }
                        }
                        if (addToList) {
                            IArgBlock rarg = CMS.createArgBlock();

                            rarg.addBigIntegerValue("serialNumber",
                                certs[i].getSerialNumber(), 16);
                            oldCertsV.addElement(certs[i]);

                            RevokedCertImpl revCertImpl =
                                new RevokedCertImpl(certs[i].getSerialNumber(),
                                    CMS.getCurrentDate(), entryExtn);

                            revCertImplsV.addElement(revCertImpl);
                            count++;
                            rarg.addStringValue("error", null);
                            argSet.addRepeatRecord(rarg);
                        }
                    }
                } else {
                    String b64eCert = req.getParameter("b64eCertificate");

                    if (b64eCert != null) {
                        byte[] certBytes = com.netscape.osutil.OSUtil.AtoB(b64eCert);
                        X509CertImpl cert = new X509CertImpl(certBytes);
                        IArgBlock rarg = CMS.createArgBlock();

                        rarg.addBigIntegerValue("serialNumber",
                            cert.getSerialNumber(), 16);
                        oldCertsV.addElement(cert);

                        RevokedCertImpl revCertImpl =
                            new RevokedCertImpl(cert.getSerialNumber(),
                                CMS.getCurrentDate(), entryExtn);

                        revCertImplsV.addElement(revCertImpl);
                        count++;
                        rarg.addStringValue("error", null);
                        argSet.addRepeatRecord(rarg);
                    }
                }
            }

            header.addIntegerValue("totalRecordCount", count);

            X509CertImpl[] oldCerts = new X509CertImpl[count];
            RevokedCertImpl[] revCertImpls = new RevokedCertImpl[count];

            for (int i = 0; i < count; i++) {
                oldCerts[i] = (X509CertImpl) oldCertsV.elementAt(i);
                revCertImpls[i] = (RevokedCertImpl) revCertImplsV.elementAt(i);
            }

            IRequest revReq =
                mQueue.newRequest(IRequest.REVOCATION_REQUEST);

            // store a message in the signed audit log file
            auditMessage = CMS.getLogMessage(
                        LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST,
                        auditSubjectID,
                        ILogger.SUCCESS,
                        auditRequesterID,
                        auditSerialNumber,
                        auditRequestType);

            audit(auditMessage);

            revReq.setExtData(IRequest.CERT_INFO, revCertImpls);
            revReq.setExtData(IRequest.REQ_TYPE, IRequest.REVOCATION_REQUEST);
            revReq.setExtData(IRequest.REQUESTOR_TYPE, IRequest.REQUESTOR_AGENT);
            revReq.setExtData(IRequest.REVOKED_REASON, reason);
            revReq.setExtData(IRequest.OLD_CERTS, oldCerts);
            if (comments != null) {
                revReq.setExtData(IRequest.REQUESTOR_COMMENTS, comments);
            }

            // change audit processing from "REQUEST" to "REQUEST_PROCESSED"
            // to distinguish which type of signed audit log message to save
            // as a failure outcome in case an exception occurs
            auditRequest = false;

            mQueue.processRequest(revReq);

            // retrieve the request status
            auditApprovalStatus = revReq.getRequestStatus().toString();

            RequestStatus stat = revReq.getRequestStatus();

            if (stat == RequestStatus.COMPLETE) {
                // audit log the error
                Integer result = revReq.getExtDataInInteger(IRequest.RESULT);

                if (result.equals(IRequest.RES_ERROR)) {
                    String[] svcErrors =
                        revReq.getExtDataInStringArray(IRequest.SVCERRORS);

                    if (svcErrors != null && svcErrors.length > 0) {
                        for (int i = 0; i < svcErrors.length; i++) {
                            String err = svcErrors[i];

                            if (err != null) {
                                //cmsReq.setErrorDescription(err);
                                for (int j = 0; j < count; j++) {
                                    if (oldCerts[j] != null) {
                                        mLogger.log(ILogger.EV_AUDIT,
                                            ILogger.S_OTHER,
                                            AuditFormat.LEVEL,
                                            AuditFormat.DOREVOKEFORMAT,
                                            new Object[] {
                                                revReq.getRequestId(),
                                                initiative,
                                                "completed with error: " +
                                                err,
                                                oldCerts[j].getSubjectDN(),
                                                oldCerts[j].getSerialNumber().toString(16),
                                                RevocationReason.fromInt(reason).toString()}
                                        );
                                    }
                                }
                            }
                        }
                    }
                    return;
                }

                // audit log the success.
                for (int j = 0; j < count; j++) {
                    if (oldCerts[j] != null) {
                        mLogger.log(ILogger.EV_AUDIT, ILogger.S_OTHER,
                            AuditFormat.LEVEL,
                            AuditFormat.DOREVOKEFORMAT,
                            new Object[] {
                                revReq.getRequestId(),
                                initiative,
                                "completed",
                                oldCerts[j].getSubjectDN(),
                                oldCerts[j].getSerialNumber().toString(16),
                                RevocationReason.fromInt(reason).toString()}
                        );
                    }
                }

                header.addStringValue("revoked", "yes");

                Integer updateCRLResult =
                    revReq.getExtDataInInteger(IRequest.CRL_UPDATE_STATUS);

                if (updateCRLResult != null) {
                    header.addStringValue("updateCRL", "yes");
                    if (updateCRLResult.equals(IRequest.RES_SUCCESS)) {
                        header.addStringValue("updateCRLSuccess", "yes");
                    } else {
                        header.addStringValue("updateCRLSuccess", "no");
                        String crlError =
                            revReq.getExtDataInString(IRequest.CRL_UPDATE_ERROR);

                        if (crlError != null)
                            header.addStringValue("updateCRLError",
                                crlError);
                    }
                    // let known crl publishing status too.
                    Integer publishCRLResult =
                        revReq.getExtDataInInteger(IRequest.CRL_PUBLISH_STATUS);

                    if (publishCRLResult != null) {
                        if (publishCRLResult.equals(IRequest.RES_SUCCESS)) {
                            header.addStringValue("publishCRLSuccess", "yes");
                        } else {
                            header.addStringValue("publishCRLSuccess", "no");
                            String publError =
                                revReq.getExtDataInString(IRequest.CRL_PUBLISH_ERROR);

                            if (publError != null)
                                header.addStringValue("publishCRLError",
                                    publError);
                        }
                    }
                }
                if (mAuthority instanceof ICertificateAuthority) {
                    // let known update and publish status of all crls.
                    Enumeration otherCRLs =
                        ((ICertificateAuthority) mAuthority).getCRLIssuingPoints();

                    while (otherCRLs.hasMoreElements()) {
                        ICRLIssuingPoint crl = (ICRLIssuingPoint)
                            otherCRLs.nextElement();
                        String crlId = crl.getId();

                        if (crlId.equals(ICertificateAuthority.PROP_MASTER_CRL))
                            continue;
                        String updateStatusStr = crl.getCrlUpdateStatusStr();
                        Integer updateResult = revReq.getExtDataInInteger(updateStatusStr);

                        if (updateResult != null) {
                            if (updateResult.equals(IRequest.RES_SUCCESS)) {
                                CMS.debug("CMCRevReqServlet: " + CMS.getLogMessage("ADMIN_SRVLT_ADDING_HEADER", 
                                        updateStatusStr));
                                header.addStringValue(updateStatusStr, "yes");
                            } else {
                                String updateErrorStr = crl.getCrlUpdateErrorStr();

                                CMS.debug("CMCRevReqServlet: " + CMS.getLogMessage("ADMIN_SRVLT_ADDING_HEADER_NO", 
                                        updateStatusStr));
                                header.addStringValue(updateStatusStr, "no");
                                String error =
                                    revReq.getExtDataInString(updateErrorStr);

                                if (error != null)
                                    header.addStringValue(updateErrorStr,
                                        error);
                            }
                            String publishStatusStr = crl.getCrlPublishStatusStr();
                            Integer publishResult =
                                revReq.getExtDataInInteger(publishStatusStr);

                            if (publishResult == null)
                                continue;
                            if (publishResult.equals(IRequest.RES_SUCCESS)) {
                                header.addStringValue(publishStatusStr, "yes");
                            } else {
                                String publishErrorStr =
                                    crl.getCrlPublishErrorStr();

                                header.addStringValue(publishStatusStr, "no");
                                String error =
                                    revReq.getExtDataInString(publishErrorStr);

                                if (error != null)
                                    header.addStringValue(
                                        publishErrorStr, error);
                            }
                        }
                    }
                }

                if (mPublisherProcessor != null && mPublisherProcessor.ldapEnabled()) {
                    header.addStringValue("dirEnabled", "yes");
                    Integer[] ldapPublishStatus =
                        revReq.getExtDataInIntegerArray("ldapPublishStatus");
                    int certsToUpdate = 0;
                    int certsUpdated = 0;

                    if (ldapPublishStatus != null) {
                        certsToUpdate = ldapPublishStatus.length;
                        for (int i = 0; i < certsToUpdate; i++) {
                            if (ldapPublishStatus[i] == IRequest.RES_SUCCESS) {
                                certsUpdated++;
                            }
                        }
                    }
                    header.addIntegerValue("certsUpdated", certsUpdated);
                    header.addIntegerValue("certsToUpdate", certsToUpdate);

                    // add crl publishing status.
                    String publError =
                        revReq.getExtDataInString(IRequest.CRL_PUBLISH_ERROR);

                    if (publError != null) {
                        header.addStringValue("crlPublishError",
                            publError);
                    }
                } else {
                    header.addStringValue("dirEnabled", "no");
                }
                header.addStringValue("error", null);

            } else if (stat == RequestStatus.PENDING) {
                header.addStringValue("error", "Request Pending");
                header.addStringValue("revoked", "pending");
                // audit log the pending
                for (int j = 0; j < count; j++) {
                    if (oldCerts[j] != null) {
                        mLogger.log(ILogger.EV_AUDIT, ILogger.S_OTHER,
                            AuditFormat.LEVEL,
                            AuditFormat.DOREVOKEFORMAT,
                            new Object[] {
                                revReq.getRequestId(),
                                initiative,
                                "pending",
                                oldCerts[j].getSubjectDN(),
                                oldCerts[j].getSerialNumber().toString(16),
                                RevocationReason.fromInt(reason).toString()}
                        );
                    }
                }

            } else {
                Vector errors = revReq.getExtDataInStringVector(IRequest.ERRORS);
                StringBuffer errorStr = new StringBuffer();

                if (errors != null && errors.size() > 0) {
                    for (int ii = 0; ii < errors.size(); ii++) {
                        errorStr.append(errors.elementAt(ii));;
                    }
                }
                header.addStringValue("error", errorStr.toString());
                header.addStringValue("revoked", "no");
                // audit log the error
                for (int j = 0; j < count; j++) {
                    if (oldCerts[j] != null) {
                        mLogger.log(ILogger.EV_AUDIT, ILogger.S_OTHER,
                            AuditFormat.LEVEL,
                            AuditFormat.DOREVOKEFORMAT,
                            new Object[] {
                                revReq.getRequestId(),
                                initiative,
                                stat.toString(),
                                oldCerts[j].getSubjectDN(),
                                oldCerts[j].getSerialNumber().toString(16),
                                RevocationReason.fromInt(reason).toString()}
                        );
                    }
                }
            }

            // store a message in the signed audit log file
            // if and only if "auditApprovalStatus" is
            // "complete", "revoked", or "canceled"
            if ((auditApprovalStatus.equals(RequestStatus.COMPLETE_STRING))
            ||  (auditApprovalStatus.equals(RequestStatus.REJECTED_STRING))
            ||  (auditApprovalStatus.equals(RequestStatus.CANCELED_STRING))) {
                auditMessage = CMS.getLogMessage(
                    LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_PROCESSED,
                    auditSubjectID,
                    ILogger.SUCCESS,
                    auditRequesterID,
                    auditSerialNumber,
                    auditRequestType,
                    auditReasonNum,
                    auditApprovalStatus);

                audit(auditMessage);
            }

        } catch (CertificateException e) {
            if (auditRequest) {
                // store a "CERT_STATUS_CHANGE_REQUEST" failure
                // message in the signed audit log file
                auditMessage = CMS.getLogMessage(
                    LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST,
                    auditSubjectID,
                    ILogger.FAILURE,
                    auditRequesterID,
                    auditSerialNumber,
                    auditRequestType);

                audit(auditMessage);
            } else {
                // store a "CERT_STATUS_CHANGE_REQUEST_PROCESSED" failure
                // message in the signed audit log file
                // if and only if "auditApprovalStatus" is
                // "complete", "revoked", or "canceled"
                if ((auditApprovalStatus.equals(RequestStatus.COMPLETE_STRING))
                ||  (auditApprovalStatus.equals(RequestStatus.REJECTED_STRING))
                ||  (auditApprovalStatus.equals(RequestStatus.CANCELED_STRING)))
                {
                    auditMessage = CMS.getLogMessage(
                        LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_PROCESSED,
                            auditSubjectID,
                            ILogger.FAILURE,
                            auditRequesterID,
                            auditSerialNumber,
                            auditRequestType,
                            auditReasonNum,
                            auditApprovalStatus);

                    audit(auditMessage);
                }
            }

            log(ILogger.LL_FAILURE, "error " + e);
        } catch (EBaseException e) {
            log(ILogger.LL_FAILURE, "error " + e);

            if (auditRequest) {
                // store a "CERT_STATUS_CHANGE_REQUEST" failure
                // message in the signed audit log file
                auditMessage = CMS.getLogMessage(
                    LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST,
                    auditSubjectID,
                    ILogger.FAILURE,
                    auditRequesterID,
                    auditSerialNumber,
                    auditRequestType);

                audit(auditMessage);
            } else {
                // store a "CERT_STATUS_CHANGE_REQUEST_PROCESSED" failure
                // message in the signed audit log file
                // if and only if "auditApprovalStatus" is
                // "complete", "revoked", or "canceled"
                if ((auditApprovalStatus.equals(RequestStatus.COMPLETE_STRING))
                ||  (auditApprovalStatus.equals(RequestStatus.REJECTED_STRING))
                ||  (auditApprovalStatus.equals(RequestStatus.CANCELED_STRING)))
                {
                    auditMessage = CMS.getLogMessage(
                        LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_PROCESSED,
                        auditSubjectID,
                        ILogger.FAILURE,
                        auditRequesterID,
                        auditSerialNumber,
                        auditRequestType,
                        auditReasonNum,
                        auditApprovalStatus);

                    audit(auditMessage);
                }
            }

            throw e;
        } catch (IOException e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("CMSGW_ERROR_MARKING_CERT_REVOKED", e.toString()));

            if (auditRequest) {
                // store a "CERT_STATUS_CHANGE_REQUEST" failure
                // message in the signed audit log file
                auditMessage = CMS.getLogMessage(
                    LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST,
                    auditSubjectID,
                    ILogger.FAILURE,
                    auditRequesterID,
                    auditSerialNumber,
                    auditRequestType);

                audit(auditMessage);
            } else {
                // store a "CERT_STATUS_CHANGE_REQUEST_PROCESSED" failure
                // message in the signed audit log file
                // if and only if "auditApprovalStatus" is
                // "complete", "revoked", or "canceled"
                if ((auditApprovalStatus.equals(RequestStatus.COMPLETE_STRING))
                ||  (auditApprovalStatus.equals(RequestStatus.REJECTED_STRING))
                ||  (auditApprovalStatus.equals(RequestStatus.CANCELED_STRING)))
                {
                    auditMessage = CMS.getLogMessage(
                        LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_PROCESSED,
                        auditSubjectID,
                        ILogger.FAILURE,
                        auditRequesterID,
                        auditSerialNumber,
                        auditRequestType,
                        auditReasonNum,
                        auditApprovalStatus);

                    audit(auditMessage);
                }
            }

            throw new ECMSGWException(CMS.getLogMessage("CMSGW_ERROR_MARKING_CERT_REVOKED"));
        } catch (Exception e) {
            if (auditRequest) {
                // store a "CERT_STATUS_CHANGE_REQUEST" failure
                // message in the signed audit log file
                auditMessage = CMS.getLogMessage(
                    LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST,
                    auditSubjectID,
                    ILogger.FAILURE,
                    auditRequesterID,
                    auditSerialNumber,
                    auditRequestType);

                audit(auditMessage);
            } else {
                // store a "CERT_STATUS_CHANGE_REQUEST_PROCESSED" failure
                // message in the signed audit log file
                // if and only if "auditApprovalStatus" is
                // "complete", "revoked", or "canceled"
                if ((auditApprovalStatus.equals(RequestStatus.COMPLETE_STRING))
                ||  (auditApprovalStatus.equals(RequestStatus.REJECTED_STRING))
                ||  (auditApprovalStatus.equals(RequestStatus.CANCELED_STRING)))
                {
                    auditMessage = CMS.getLogMessage(
                        LOGGING_SIGNED_AUDIT_CERT_STATUS_CHANGE_REQUEST_PROCESSED,
                        auditSubjectID,
                        ILogger.FAILURE,
                        auditRequesterID,
                        auditSerialNumber,
                        auditRequestType,
                        auditReasonNum,
                        auditApprovalStatus);

                    audit(auditMessage);
                }
            }

            e.printStackTrace();
        }

        return;
    }

    /**
     * Signed Audit Log Requester ID
     *
     * This method is called to obtain the "RequesterID" for
     * a signed audit log message.
     * <P>
     *
     * @param req HTTP request
     * @return id string containing the signed audit log message RequesterID
     */
    private String auditRequesterID(HttpServletRequest req) {
        // if no signed audit object exists, bail
        if (mSignedAuditLogger == null) {
            return null;
        }

        String requesterID = null;

        // Obtain the requesterID
        requesterID = req.getParameter("requestId");

        if (requesterID != null) {
            requesterID = requesterID.trim();
        } else {
            requesterID = ILogger.UNIDENTIFIED;
        }

        return requesterID;
    }

    /**
     * Signed Audit Log Serial Number
     *
     * This method is called to obtain the serial number of the certificate
     * whose status is to be changed for a signed audit log message.
     * <P>
     *
     * @param eeSerialNumber a string containing the un-normalized serialNumber
     * @return id string containing the signed audit log message RequesterID
     */
    private String auditSerialNumber(String eeSerialNumber) {
        // if no signed audit object exists, bail
        if (mSignedAuditLogger == null) {
            return null;
        }

        String serialNumber = null;

        // Normalize the serialNumber
        if (eeSerialNumber != null) {
            serialNumber = eeSerialNumber.trim();

            // convert it to hexadecimal
            serialNumber = "0x"
                    + Integer.toHexString(
                        Integer.valueOf(serialNumber).intValue());
        } else {
            serialNumber = ILogger.SIGNED_AUDIT_EMPTY_VALUE;
        }

        return serialNumber;
    }

    /**
     * Signed Audit Log Request Type
     *
     * This method is called to obtain the "Request Type" for
     * a signed audit log message.
     * <P>
     *
     * @param reason an integer denoting the revocation reason
     * @return string containing REVOKE or ON_HOLD
     */
    private String auditRequestType(int reason) {
        // if no signed audit object exists, bail
        if (mSignedAuditLogger == null) {
            return null;
        }

        String requestType = null;

        // Determine the revocation type based upon the revocation reason
        if (reason == ON_HOLD_REASON) {
            requestType = ON_HOLD;
        } else {
            requestType = REVOKE;
        }

        return requestType;
    }
}

