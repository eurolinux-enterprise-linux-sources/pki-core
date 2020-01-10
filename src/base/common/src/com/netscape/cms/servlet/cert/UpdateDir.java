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
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.security.*;
import javax.servlet.*;
import javax.servlet.http.*;
import netscape.security.x509.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.publish.*;
import com.netscape.certsrv.ca.*;

import com.netscape.cms.servlet.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.ldap.*;
import com.netscape.certsrv.dbs.certdb.*;
import com.netscape.certsrv.dbs.crldb.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;


/**
 * Update the configured LDAP server with specified objects
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class UpdateDir extends CMSServlet {

    private final static String INFO = "UpdateDir";
    private final static String TPL_FILE = "updateDir.template";
    private final static int UPDATE_ALL = 0;
    private final static int UPDATE_CRL = 1;
    private final static int UPDATE_CA = 2;
    private final static int UPDATE_VALID = 3;
    private final static int VALID_FROM = 4;
    private final static int VALID_TO = 5;
    private final static int UPDATE_EXPIRED = 6;
    private final static int EXPIRED_FROM = 7;
    private final static int EXPIRED_TO = 8;
    private final static int UPDATE_REVOKED = 9;
    private final static int REVOKED_FROM = 10;
    private final static int REVOKED_TO = 11;
    private final static int CHECK_FLAG = 12;
    private final static String[] updateName = 
        {"updateAll", "updateCRL", "updateCA",
            "updateValid", "validFrom", "validTo",
            "updateExpired", "expiredFrom", "expiredTo",
            "updateRevoked", "revokedFrom", "revokedTo",
            "checkFlag"};

    private String mFormPath = null;
    private ICertificateAuthority mCA = null;
    private IPublisherProcessor mPublisherProcessor = null;
    private ICRLRepository mCRLRepository = null;
    private boolean mClonedCA = false;

    /**
     * Constructs UpdateDir servlet.
     */
    public UpdateDir() {
        super();
    }

    /**
     * Initialize the servlet. This servlet uses the template
     * 'updateDir.template' to render the response
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);

        if( mAuthority != null ) {
            mFormPath = "/" + mAuthority.getId() + "/" + TPL_FILE;
            if (mAuthority instanceof ICertificateAuthority) {
                mCA = (ICertificateAuthority) mAuthority;
                mPublisherProcessor = mCA.getPublisherProcessor();
                mCRLRepository = mCA.getCRLRepository();
            }

            // override success to do output orw own template.
            mTemplates.remove(CMSRequest.SUCCESS);
            if (mOutputTemplatePath != null) {
                mFormPath = mOutputTemplatePath;
            }
        }
    }

    /**
     * Process the HTTP request. 
     *
     * @param cmsReq the object holding the request and response information
     */
    public void process(CMSRequest cmsReq) throws EBaseException {
        HttpServletRequest req = cmsReq.getHttpReq();
        HttpServletResponse resp = cmsReq.getHttpResp();

        IAuthToken authToken = authenticate(cmsReq);

        AuthzToken authzToken = null;

        try {
            authzToken = authorize(mAclMethod, authToken,
                        mAuthzResourceName, "update");
        } catch (EAuthzAccessDenied e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
        } catch (Exception e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
        }

        if (authzToken == null) {
            cmsReq.setStatus(CMSRequest.UNAUTHORIZED);
            return;
        }

        EBaseException error = null;

        IArgBlock header = CMS.createArgBlock();
        IArgBlock fixed = CMS.createArgBlock();
        CMSTemplateParams argSet = new CMSTemplateParams(header, fixed);

        CMSTemplate form = null;
        Locale[] locale = new Locale[1];

        try {
            form = getTemplate(mFormPath, req, locale);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_ERR_GET_TEMPLATE", mFormPath, e.toString()));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
        }

        try {
            String crlIssuingPointId = req.getParameter("crlIssuingPoint");

            if (mPublisherProcessor == null ||
                !mPublisherProcessor.enabled())
                throw new ECMSGWException(CMS.getUserMessage("CMS_GW_NO_PUB_MODULE"));

            String[] updateValue = new String[updateName.length];

            for (int i = 0; i < updateName.length; i++) {
                updateValue[i] = req.getParameter(updateName[i]);
            }

            String masterHost = CMS.getConfigStore().getString("master.ca.agent.host", "");
            String masterPort = CMS.getConfigStore().getString("master.ca.agent.port", "");
            if (masterHost != null && masterHost.length() > 0 &&
                masterPort != null && masterPort.length() > 0) {
                mClonedCA = true;
            }

            process(argSet, header, req, resp, crlIssuingPointId, updateValue, locale[0]);
        } catch (EBaseException e) {
            error = e;
        }

        try {
            ServletOutputStream out = resp.getOutputStream();

            if (error == null) {
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
                CMS.getLogMessage("CMSGW_ERR_OUT_STREAM_TEMPLATE", e.toString()));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
        }
    }

    private void updateCRLIssuingPoint(
        IArgBlock header, 
        String crlIssuingPointId,
        ICRLIssuingPoint crlIssuingPoint,
        Locale locale) {
        SessionContext sc = SessionContext.getContext();

        sc.put(ICRLIssuingPoint.SC_ISSUING_POINT_ID, crlIssuingPointId);
        sc.put(ICRLIssuingPoint.SC_IS_DELTA_CRL, "false");
        ICRLIssuingPointRecord crlRecord = null;

        try {
            if (mCRLRepository != null) {
                crlRecord = (ICRLIssuingPointRecord)mCRLRepository.readCRLIssuingPointRecord(crlIssuingPointId);
            }
        } catch (EBaseException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_ERR_GET_CRL_RECORD", e.toString()));
        }

        if (crlRecord == null) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_CRL_NOT_YET_UPDATED_1", crlIssuingPointId));
            header.addStringValue("crlPublished", "Failure");
            header.addStringValue("crlError",
                new ECMSGWException(CMS.getUserMessage(locale, "CMS_GW_CRL_NOT_YET_UPDATED")).toString());
        } else {
            String publishDN = (crlIssuingPoint != null)? crlIssuingPoint.getPublishDN(): null;
            byte[] crlbytes = crlRecord.getCRL();

            if (crlbytes == null) {
                log(ILogger.LL_FAILURE, 
                    CMS.getLogMessage("CMSGW_CRL_NOT_YET_UPDATED_1", ""));
                header.addStringValue("crlPublished", "Failure");
                header.addStringValue("crlError",
                    new ECMSGWException(CMS.getUserMessage(locale, "CMS_GW_CRL_NOT_YET_UPDATED")).toString());
            } else {
                X509CRLImpl crl = null;

                try {
                    crl = new X509CRLImpl(crlbytes);
                } catch (Exception e) {
                    log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_ERR_DECODE_CRL", e.toString()));
                }

                if (crl == null) {
                    header.addStringValue("crlPublished", "Failure");
                    header.addStringValue("crlError",
                        new ECMSGWException(CMS.getUserMessage(locale,"CMS_GW_DECODE_CRL_FAILED")).toString());
                } else {
                    try {
                        if (publishDN != null) {
                            mPublisherProcessor.publishCRL(publishDN, crl);
                        } else {
                            mPublisherProcessor.publishCRL(crl,crlIssuingPointId);
                        }
                        header.addStringValue("crlPublished", "Success");
                    } catch (ELdapException e) {
                        header.addStringValue("crlPublished", "Failure");
                        header.addStringValue("crlError", e.toString(locale));
                        log(ILogger.LL_FAILURE, CMS.getLogMessage("LDAP_ERROR_PUBLISH_CRL", e.toString()));
                    }
                }
            }

            sc.put(ICRLIssuingPoint.SC_IS_DELTA_CRL, "true");
            // handle delta CRL if any
            byte[] deltaCrlBytes = crlRecord.getDeltaCRL();

            if (deltaCrlBytes != null) {
                X509CRLImpl deltaCrl = null;

                try {
                    deltaCrl = new X509CRLImpl(deltaCrlBytes);
                } catch (Exception e) {
                    log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_ERR_DECODE_DELTA_CRL", e.toString()));
                }

                boolean goodDelta = false;
                if (mClonedCA) {
                    BigInteger crlNumber = crlRecord.getCRLNumber();
                    BigInteger deltaNumber = crlRecord.getDeltaCRLNumber();
                    Long deltaCRLSize = crlRecord.getDeltaCRLSize();
                    if (deltaCRLSize != null && deltaCRLSize.longValue() > -1 &&
                        crlNumber != null && deltaNumber != null &&
                        deltaNumber.compareTo(crlNumber) >= 0) {
                        goodDelta = true;
                    }
                }

                if (deltaCrl != null && ((mClonedCA && goodDelta) ||
                    (crlIssuingPoint != null &&
                     crlIssuingPoint.isThisCurrentDeltaCRL(deltaCrl)))) {
                    try {
                        if (publishDN != null) {
                            mPublisherProcessor.publishCRL(publishDN, deltaCrl);
                        } else {
                            mPublisherProcessor.publishCRL(deltaCrl,crlIssuingPointId);
                        }
                    } catch (ELdapException e) {
                        log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_ERR_PUBLISH_DELTA_CRL", e.toString()));
                    }
                }
            }
        } // if
    }

    private void process(CMSTemplateParams argSet, IArgBlock header,
        HttpServletRequest req,
        HttpServletResponse resp,
        String crlIssuingPointId,
        String[] updateValue,
        Locale locale)
        throws EBaseException {
        // all or crl
        if ((updateValue[UPDATE_ALL] != null &&
                updateValue[UPDATE_ALL].equalsIgnoreCase("yes")) ||
            (updateValue[UPDATE_CRL] != null &&
                updateValue[UPDATE_CRL].equalsIgnoreCase("yes"))) {
            // check if received issuing point ID is known to the server
            if (crlIssuingPointId != null) {
                Enumeration ips = mCA.getCRLIssuingPoints();

                while (ips.hasMoreElements()) {
                    ICRLIssuingPoint ip = (ICRLIssuingPoint) ips.nextElement();

                    if (crlIssuingPointId.equals(ip.getId())) {
                        break;
                    }
                    if (!ips.hasMoreElements()) crlIssuingPointId = null;
                }
            }
            if (crlIssuingPointId == null) {
                // publish all issuing points
                if (mClonedCA && mCRLRepository != null) {
                    Vector ipNames = mCRLRepository.getIssuingPointsNames();
                    if (ipNames != null && ipNames.size() > 0) {
                        for (int i = 0; i < ipNames.size(); i++) {
                            String ipName = (String)ipNames.elementAt(i);

                            updateCRLIssuingPoint(header, ipName, null, locale);
                        }
                    }
                } else {
                    Enumeration oips = mCA.getCRLIssuingPoints();

                    while (oips.hasMoreElements()) {
                        ICRLIssuingPoint oip = (ICRLIssuingPoint) oips.nextElement();

                        updateCRLIssuingPoint(header, oip.getId(), oip, locale);
                    }
                }
            } else {
                ICRLIssuingPoint crlIssuingPoint =
                    mCA.getCRLIssuingPoint(crlIssuingPointId);
                ICRLIssuingPointRecord crlRecord = null;

                updateCRLIssuingPoint(header, crlIssuingPointId, 
                    crlIssuingPoint, locale);
            }
        }

        ICertificateRepository certificateRepository = (ICertificateRepository) mCA.getCertificateRepository();

        // all or ca
        if ((updateValue[UPDATE_ALL] != null &&
                updateValue[UPDATE_ALL].equalsIgnoreCase("yes")) ||
            (updateValue[UPDATE_CA] != null &&
                updateValue[UPDATE_CA].equalsIgnoreCase("yes"))) {
            X509CertImpl caCert = mCA.getSigningUnit().getCertImpl();

            try {
                mPublisherProcessor.publishCACert(caCert);
                header.addStringValue("caCertPublished", "Success");
            } catch (ELdapException e) {
                log(ILogger.LL_FAILURE, CMS.getLogMessage("LDAP_ERROR_PUBLISH_CACERT_1",
                        caCert.getSerialNumber().toString(16), e.toString()));
                header.addStringValue("caCertPublished", "Failure");
                header.addStringValue("caCertError", e.toString(locale));
            }
        }

        // all or valid
        if ((updateValue[UPDATE_ALL] != null &&
                updateValue[UPDATE_ALL].equalsIgnoreCase("yes")) ||
            (updateValue[UPDATE_VALID] != null &&
                updateValue[UPDATE_VALID].equalsIgnoreCase("yes"))) {
            if (certificateRepository != null) {
                if (updateValue[VALID_FROM].startsWith("0x")) {
                    updateValue[VALID_FROM] = hexToDecimal(updateValue[VALID_FROM]);
                }
                if (updateValue[VALID_TO].startsWith("0x")) {
                    updateValue[VALID_TO] = hexToDecimal(updateValue[VALID_TO]);
                }
                Enumeration validCerts = null;

                if (updateValue[CHECK_FLAG] != null &&
                    updateValue[CHECK_FLAG].equalsIgnoreCase("yes")) {
                    validCerts = 
                            certificateRepository.getValidNotPublishedCertificates(
                                updateValue[VALID_FROM],
                                updateValue[VALID_TO]);
                } else {
                    validCerts = 
                            certificateRepository.getValidCertificates(
                                updateValue[VALID_FROM],
                                updateValue[VALID_TO]);
                }
                int i = 0;
                int l = 0;
                String validCertsError = "";

                if (validCerts != null) {
                    while (validCerts.hasMoreElements()) {
                        ICertRecord certRecord =
                            (ICertRecord) validCerts.nextElement();
                        //X509CertImpl cert = certRecord.getCertificate();
                        X509CertImpl cert = null;
                        Object o = certRecord.getCertificate();

                        if (o instanceof X509CertImpl)
                            cert = (X509CertImpl) o;

                        MetaInfo metaInfo = null;
                        String ridString = null;

                        metaInfo = (MetaInfo) certRecord.get(ICertRecord.ATTR_META_INFO);
                        if (metaInfo == null) {
                            // ca's self signed signing cert and
                            // server cert has no related request and
                            // have no metaInfo
                            log(ILogger.LL_FAILURE,					
                                CMS.getLogMessage("CMSGW_FAIL_GET_ICERT_RECORD",
                                    cert.getSerialNumber().toString(16)));
                        } else {
                            ridString = (String) metaInfo.get(ICertRecord.META_REQUEST_ID);
                        }

                        IRequest r = null;

                        if (ridString != null) {
                            RequestId rid = new RequestId(ridString);
						
                            r = mCA.getRequestQueue().findRequest(rid);
                        } 

                        try {
                            l++;
                            SessionContext sc = SessionContext.getContext();                                

                            if (r == null) {
                                if (CMS.isEncryptionCert(cert))
                                    sc.put((Object) "isEncryptionCert", (Object) "true");
                                else 
                                    sc.put((Object) "isEncryptionCert", (Object) "false");
                                mPublisherProcessor.publishCert(cert, null);
                            } else {
                                if (CMS.isEncryptionCert(cert))
                                    r.setExtData("isEncryptionCert", "true");
                                else 
                                    r.setExtData("isEncryptionCert", "false");
                                mPublisherProcessor.publishCert(cert, r);
                            }
                            i++;
                        } catch (Exception e) {
                            log(ILogger.LL_FAILURE, 
                                CMS.getLogMessage("CMSGW_FAIL_PUBLISH_CERT", certRecord.getSerialNumber().toString(16),
                                    e.toString()));
                            validCertsError +=
                                    "Failed to publish certificate: 0x" +
                                    certRecord.getSerialNumber().toString(16) +
                                    ".\n <BR> &nbsp;&nbsp;&nbsp;&nbsp;";
                        }
                    }
                }
                if (i > 0 && i == l) {
                    header.addStringValue("validCertsPublished",
                        "Success");
                    if (i == 1)
                        header.addStringValue("validCertsError", i + 
                            " valid certificate is published in the directory.");
                    else
                        header.addStringValue("validCertsError", i + 
                            " valid certificates are published in the directory.");
                } else {
                    if (l == 0) {
                        header.addStringValue("validCertsPublished", "No");
                    } else {
                        header.addStringValue("validCertsPublished", "Failure");
                        header.addStringValue("validCertsError", 
                            validCertsError);
                    }
                }
            } else {
                header.addStringValue("validCertsPublished", "Failure");
                header.addStringValue("validCertsError", "Certificate repository is unavailable.");
            }
        }

        // all or expired
        if ((updateValue[UPDATE_ALL] != null &&
                updateValue[UPDATE_ALL].equalsIgnoreCase("yes")) ||
            (updateValue[UPDATE_EXPIRED] != null &&
                updateValue[UPDATE_EXPIRED].equalsIgnoreCase("yes"))) {
            if (certificateRepository != null) {
                if (updateValue[EXPIRED_FROM].startsWith("0x")) {
                    updateValue[EXPIRED_FROM] = hexToDecimal(updateValue[EXPIRED_FROM]);
                }
                if (updateValue[EXPIRED_TO].startsWith("0x")) {
                    updateValue[EXPIRED_TO] = hexToDecimal(updateValue[EXPIRED_TO]);
                }
                Enumeration expiredCerts = null;

                if (updateValue[CHECK_FLAG] != null &&
                    updateValue[CHECK_FLAG].equalsIgnoreCase("yes")) {
                    expiredCerts =
                            certificateRepository.getExpiredPublishedCertificates(
                                updateValue[EXPIRED_FROM],
                                updateValue[EXPIRED_TO]);
                } else {
                    expiredCerts =
                            certificateRepository.getExpiredCertificates(
                                updateValue[EXPIRED_FROM],
                                updateValue[EXPIRED_TO]);
                }
                int i = 0;
                int l = 0;
                StringBuffer expiredCertsError = new StringBuffer();

                if (expiredCerts != null) { 
                    while (expiredCerts.hasMoreElements()) {
                        ICertRecord certRecord =
                            (ICertRecord) expiredCerts.nextElement();
                        //X509CertImpl cert = certRecord.getCertificate();
                        X509CertImpl cert = null;
                        Object o = certRecord.getCertificate();

                        if (o instanceof X509CertImpl)
                            cert = (X509CertImpl) o;

                        MetaInfo metaInfo = null;
                        String ridString = null;

                        metaInfo = (MetaInfo) certRecord.get(ICertRecord.ATTR_META_INFO);
                        if (metaInfo == null) {
                            // ca's self signed signing cert and
                            // server cert has no related request and
                            // have no metaInfo
                            log(ILogger.LL_FAILURE,					
                                CMS.getLogMessage("CMSGW_FAIL_GET_ICERT_RECORD",
                                    cert.getSerialNumber().toString(16)));
                        } else {
                            ridString = (String) metaInfo.get(ICertRecord.META_REQUEST_ID);
                        }

                        IRequest r = null;

                        if (ridString != null) {
                            RequestId rid = new RequestId(ridString);
						
                            r = mCA.getRequestQueue().findRequest(rid);
                        } 

                        try {
                            l++;
                            if (r == null) {
                                mPublisherProcessor.unpublishCert(cert, null);
                            } else {
                                mPublisherProcessor.unpublishCert(cert, r);
                            }
                            i++;
                        } catch (Exception e) {
                            log(ILogger.LL_FAILURE, 
                                CMS.getLogMessage("LDAP_ERROR_UNPUBLISH_CERT",
                                    certRecord.getSerialNumber().toString(16),
                                    e.toString()));
                            expiredCertsError.append(
                                    "Failed to unpublish certificate: 0x");
                            expiredCertsError.append(
                                    certRecord.getSerialNumber().toString(16));
                            expiredCertsError.append(
                                    ".\n <BR> &nbsp;&nbsp;&nbsp;&nbsp;");
                        }
                    }
                }
                if (i > 0 && i == l) {
                    header.addStringValue("expiredCertsUnpublished", "Success");
                    if (i == 1)
                        header.addStringValue("expiredCertsError", i + 
                            " expired certificate is unpublished in the directory.");
                    else
                        header.addStringValue("expiredCertsError", i + 
                            " expired certificates are unpublished in the directory.");
                } else {
                    if (l == 0) {
                        header.addStringValue("expiredCertsUnpublished", "No");
                    } else {
                        header.addStringValue("expiredCertsUnpublished", "Failure");
                        header.addStringValue("expiredCertsError", 
                            expiredCertsError.toString());
                    }
                }
            } else {
                header.addStringValue("expiredCertsUnpublished", "Failure");
                header.addStringValue("expiredCertsError", "Certificate repository is unavailable.");
            }
        }

        // all or revoked
        if ((updateValue[UPDATE_ALL] != null &&
                updateValue[UPDATE_ALL].equalsIgnoreCase("yes")) ||
            (updateValue[UPDATE_REVOKED] != null &&
                updateValue[UPDATE_REVOKED].equalsIgnoreCase("yes"))) {
            if (certificateRepository != null) {
                if (updateValue[REVOKED_FROM].startsWith("0x")) {
                    updateValue[REVOKED_FROM] = hexToDecimal(updateValue[REVOKED_FROM]);
                }
                if (updateValue[REVOKED_TO].startsWith("0x")) {
                    updateValue[REVOKED_TO] = hexToDecimal(updateValue[REVOKED_TO]);
                }
                Enumeration revokedCerts = null;

                if (updateValue[CHECK_FLAG] != null &&
                    updateValue[CHECK_FLAG].equalsIgnoreCase("yes")) {
                    revokedCerts =
                            certificateRepository.getRevokedPublishedCertificates(
                                updateValue[REVOKED_FROM],
                                updateValue[REVOKED_TO]);
                } else {
                    revokedCerts =
                            certificateRepository.getRevokedCertificates(
                                updateValue[REVOKED_FROM],
                                updateValue[REVOKED_TO]);
                }
                int i = 0;
                int l = 0;
                String revokedCertsError = "";

                if (revokedCerts != null) { 
                    while (revokedCerts.hasMoreElements()) {
                        ICertRecord certRecord =
                            (ICertRecord) revokedCerts.nextElement();
                        //X509CertImpl cert = certRecord.getCertificate();
                        X509CertImpl cert = null;
                        Object o = certRecord.getCertificate();

                        if (o instanceof X509CertImpl)
                            cert = (X509CertImpl) o;

                        MetaInfo metaInfo = null;
                        String ridString = null;

                        metaInfo = (MetaInfo) certRecord.get(ICertRecord.ATTR_META_INFO);
                        if (metaInfo == null) {
                            // ca's self signed signing cert and
                            // server cert has no related request and
                            // have no metaInfo
                            log(ILogger.LL_FAILURE,					
                                CMS.getLogMessage("CMSGW_FAIL_GET_ICERT_RECORD",
                                    cert.getSerialNumber().toString(16)));
                        } else {
                            ridString = (String) metaInfo.get(ICertRecord.META_REQUEST_ID);
                        }

                        IRequest r = null;

                        if (ridString != null) {
                            RequestId rid = new RequestId(ridString);
						
                            r = mCA.getRequestQueue().findRequest(rid);
                        } 

                        try {
                            l++;
                            if (r == null) {
                                mPublisherProcessor.unpublishCert(cert, null);
                            } else {
                                mPublisherProcessor.unpublishCert(cert, r);
                            }
                            i++;
                        } catch (Exception e) {
                            log(ILogger.LL_FAILURE, 
                                CMS.getLogMessage("LDAP_ERROR_UNPUBLISH_CERT",
                                    certRecord.getSerialNumber().toString(16),
                                    e.toString()));
                            revokedCertsError +=
                                    "Failed to unpublish certificate: 0x" +
                                    certRecord.getSerialNumber().toString(16) +
                                    ".\n <BR> &nbsp;&nbsp;&nbsp;&nbsp;";
                        }
                    }
                }
                if (i > 0 && i == l) {
                    header.addStringValue("revokedCertsUnpublished", "Success");
                    if (i == 1)
                        header.addStringValue("revokedCertsError", i + 
                            " revoked certificate is unpublished in the directory.");
                    else
                        header.addStringValue("revokedCertsError", i + 
                            " revoked certificates are unpublished in the directory.");
                } else {
                    if (l == 0) {
                        header.addStringValue("revokedCertsUnpublished", "No");
                    } else {
                        header.addStringValue("revokedCertsUnpublished", "Failure");
                        header.addStringValue("revokedCertsError", 
                            revokedCertsError);
                    }
                }
            } else {
                header.addStringValue("revokedCertsUnpublished", "Failure");
                header.addStringValue("revokedCertsError", "Certificate repository is unavailable.");
            }
        }

        return;
    }

    private String hexToDecimal(String hex) {
        String newHex = hex.substring(2);
        BigInteger bi = new BigInteger(newHex, 16);

        return bi.toString();
    }
}
