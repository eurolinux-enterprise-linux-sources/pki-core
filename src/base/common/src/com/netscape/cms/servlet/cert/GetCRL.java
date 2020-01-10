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
import java.math.*;
import javax.servlet.*;
import java.security.cert.*;
import javax.servlet.http.*;
import netscape.ldap.*;
import netscape.security.x509.*;
import netscape.security.pkcs.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.policy.*;
import com.netscape.certsrv.request.IRequest;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.certdb.*;
import com.netscape.certsrv.ldap.*;
import com.netscape.certsrv.authority.*;

import com.netscape.certsrv.ca.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.dbs.certdb.*;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.crldb.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;
import com.netscape.cms.servlet.*;


/**
 * Retrieve CRL for a Certificate Authority
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class GetCRL extends CMSServlet {
    private final static String TPL_FILE = "displayCRL.template";
    private String mFormPath = null;

    public GetCRL() {
        super();
    }

    /**
     * initialize the servlet.
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);

        mTemplates.remove(CMSRequest.SUCCESS);
        mFormPath = "/" + mAuthority.getId() + "/" + TPL_FILE;
        if (mOutputTemplatePath != null)
            mFormPath = mOutputTemplatePath;
    }


    /**
     * Process the HTTP request. 
     *
     * @param cmsReq the object holding the request and response information
	 * @see DisplayCRL#process
     */
    protected void process(CMSRequest cmsReq)
        throws EBaseException {
        HttpServletRequest httpReq = cmsReq.getHttpReq();
        HttpServletResponse httpResp = cmsReq.getHttpResp();

        IAuthToken authToken = authenticate(cmsReq);

        AuthzToken authzToken = null;

        try {
            authzToken = authorize(mAclMethod, authToken,
                        mAuthzResourceName, "read");
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

        // Construct an ArgBlock
        IArgBlock args = cmsReq.getHttpParams();

        if (!(mAuthority instanceof ICertificateAuthority)) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_CA_FROM_RA_NOT_IMP"));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_NOT_YET_IMPLEMENTED")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }

        CMSTemplate form = null;
        Locale[] locale = new Locale[1];

CMS.debug("**** mFormPath before getTemplate = "+mFormPath);
        try {
            form = getTemplate(mFormPath, httpReq, locale);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_ERR_GET_TEMPLATE", mFormPath, e.toString()));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }

        IArgBlock header = CMS.createArgBlock();
        IArgBlock fixed = CMS.createArgBlock();
        CMSTemplateParams argSet = new CMSTemplateParams(header, fixed);

        // Get the operation code
        String op = null;
        String crlId = null;

        op = args.getValueAsString("op", null);
        crlId = args.getValueAsString("crlIssuingPoint", null);
        if (op == null) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_NO_OPTIONS_SELECTED"));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_NO_OPTIONS_SELECTED")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }
        if (crlId == null) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_NO_CRL_ISSUING_POINT"));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_NO_CRL_SELECTED")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }

        ICRLIssuingPointRecord crlRecord = null;
        ICertificateAuthority ca = (ICertificateAuthority) mAuthority;
        ICRLIssuingPoint crlIP = null;
        if (ca != null) crlIP = ca.getCRLIssuingPoint(crlId);

        try {
            crlRecord = (ICRLIssuingPointRecord) ca.getCRLRepository().readCRLIssuingPointRecord(crlId);
        } catch (EBaseException e) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_NO_CRL_ISSUING_POINT_FOUND", crlId));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_CRL_NOT_FOUND")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }
        if (crlRecord == null) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_CRL_NOT_YET_UPDATED_1", crlId));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_CRL_NOT_UPDATED")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }

        header.addStringValue("crlIssuingPoint", crlId);
        header.addStringValue("crlNumber", crlRecord.getCRLNumber().toString());
        long lCRLSize = crlRecord.getCRLSize().longValue();

        header.addLongValue("crlSize", lCRLSize);
        if (crlIP != null) {
            header.addStringValue("crlDescription", crlIP.getDescription());
        }

        String crlDisplayType = args.getValueAsString("crlDisplayType", null);
        if (crlDisplayType != null) {
            header.addStringValue("crlDisplayType", crlDisplayType);
        }

        if ((op.equals("checkCRLcache") ||
             (op.equals("displayCRL") && crlDisplayType != null && crlDisplayType.equals("cachedCRL"))) &&
              (crlIP == null || (!crlIP.isCRLCacheEnabled()) || crlIP.isCRLCacheEmpty())) {
            cmsReq.setError(
                CMS.getUserMessage(
                    ((crlIP != null && crlIP.isCRLCacheEnabled() && crlIP.isCRLCacheEmpty())?
                        "CMS_GW_CRL_CACHE_IS_EMPTY":"CMS_GW_CRL_CACHE_IS_NOT_ENABLED"), crlId));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }

        byte[] crlbytes = null;

        if (op.equals("importDeltaCRL") || op.equals("getDeltaCRL") ||
            (op.equals("displayCRL") && crlDisplayType != null &&
             crlDisplayType.equals("deltaCRL"))) {
            crlbytes = crlRecord.getDeltaCRL();
        } else if (op.equals("importCRL") || op.equals("getCRL") ||
                   op.equals("checkCRL") ||
                   (op.equals("displayCRL") &&
                    crlDisplayType != null &&
                    (crlDisplayType.equals("entireCRL") ||
                     crlDisplayType.equals("crlHeader") ||
                     crlDisplayType.equals("base64Encoded")))) {
            crlbytes = crlRecord.getCRL();
        } 

        if (crlbytes == null && (!op.equals("checkCRLcache")) &&
            (!(op.equals("displayCRL") && crlDisplayType != null &&
               crlDisplayType.equals("cachedCRL")))) {
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("CMSGW_CRL_NOT_YET_UPDATED_1", crlId));
            cmsReq.setError(new ECMSGWException(
              CMS.getUserMessage("CMS_GW_CRL_NOT_UPDATED")));
            cmsReq.setStatus(CMSRequest.ERROR);
            return;
        }
        byte[] bytes = crlbytes;

        X509CRLImpl crl = null;

        if (op.equals("checkCRL") || op.equals("importCRL") ||
            op.equals("importDeltaCRL") ||
            (op.equals("displayCRL") && crlDisplayType != null &&
             (crlDisplayType.equals("entireCRL") ||
              crlDisplayType.equals("crlHeader") ||
              crlDisplayType.equals("base64Encoded") ||
              crlDisplayType.equals("deltaCRL")))) {
            try {
                if (op.equals("displayCRL") && crlDisplayType != null &&
                    crlDisplayType.equals("crlHeader")) {
                    crl = new X509CRLImpl(crlbytes, false);
                } else {
                    crl = new X509CRLImpl(crlbytes);
                }
            } catch (Exception e) {
                log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_FAILED_DECODE_CRL_1", e.toString()));
                cmsReq.setError(new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_DECODE_CRL_FAILED")));
                cmsReq.setStatus(CMSRequest.ERROR);
                return;
            }
            if (crl == null) {
                log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_FAILED_DECODE_CRL"));
                cmsReq.setError(new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_DECODE_CRL_FAILED")));
                cmsReq.setStatus(CMSRequest.ERROR);
                return;
            }
            if ((op.equals("importDeltaCRL") || (op.equals("displayCRL") &&
                 crlDisplayType != null && crlDisplayType.equals("deltaCRL"))) &&
                  ((!(crlIP != null && crlIP.isThisCurrentDeltaCRL(crl))) &&
                   (crlRecord.getCRLNumber() == null ||
                    crlRecord.getDeltaCRLNumber() == null || 
                    crlRecord.getDeltaCRLNumber().compareTo(crlRecord.getCRLNumber()) < 0 ||
                    crlRecord.getDeltaCRLSize() == null ||
                    crlRecord.getDeltaCRLSize().longValue() == -1))) {
                log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_ERR_NO_DELTA_CRL_1"));
                cmsReq.setError(new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_CRL_NOT_UPDATED")));
                cmsReq.setStatus(CMSRequest.ERROR);
                return;
            }
        } 

        String mimeType = "application/x-pkcs7-crl";

        if (op.equals("checkCRLcache") || op.equals("checkCRL") || op.equals("displayCRL")) {
            header.addStringValue("toDo", op);
            String certSerialNumber = args.getValueAsString("certSerialNumber", "");

            header.addStringValue("certSerialNumber", certSerialNumber);
            if (certSerialNumber.startsWith("0x")) {
                certSerialNumber = hexToDecimal(certSerialNumber);
            }

            if (op.equals("checkCRLcache")) {
                if (crlIP.getRevocationDateFromCache(
                        new BigInteger(certSerialNumber), false, false) != null) {
                    header.addBooleanValue("isOnCRL", true);
                } else {
                    header.addBooleanValue("isOnCRL", false);
                }
            }

            if (op.equals("checkCRL")) {
                header.addBooleanValue("isOnCRL",
                    crl.isRevoked(new BigInteger(certSerialNumber)));
            }

            if (op.equals("displayCRL")) {
                if (crlDisplayType.equals("entireCRL") || crlDisplayType.equals("cachedCRL")) {
                    ICRLPrettyPrint crlDetails = (crlDisplayType.equals("entireCRL"))?
                                                    CMS.getCRLPrettyPrint(crl):
                                                    CMS.getCRLCachePrettyPrint(crlIP);
                    String pageStart = args.getValueAsString("pageStart", null);
                    String pageSize = args.getValueAsString("pageSize", null);

                    if (pageStart != null && pageSize != null) {
                        long lPageStart = new Long(pageStart).longValue();
                        long lPageSize = new Long(pageSize).longValue();

                        if (lPageStart < 1) lPageStart = 1;

                        header.addStringValue("crlPrettyPrint",
                                 crlDetails.toString(locale[0],
                                 lCRLSize, lPageStart, lPageSize));
                        header.addLongValue("pageStart", lPageStart);
                        header.addLongValue("pageSize", lPageSize);
                    } else {
                        header.addStringValue(
                            "crlPrettyPrint", crlDetails.toString(locale[0]));
                    }
                } else if (crlDisplayType.equals("crlHeader")) {
                    ICRLPrettyPrint crlDetails = CMS.getCRLPrettyPrint(crl);

                    header.addStringValue(
                        "crlPrettyPrint", crlDetails.toString(locale[0], lCRLSize, 0, 0));
                } else if (crlDisplayType.equals("base64Encoded")) {
                    try {
                        byte[] ba = crl.getEncoded();
                        String crlBase64Encoded = com.netscape.osutil.OSUtil.BtoA(ba);
                        int length = crlBase64Encoded.length();
                        int i = 0;
                        int j = 0;
                        int n = 1;

                        while (i < length) {
                            int k = crlBase64Encoded.indexOf('\n', i);

                            if (n < 100 && k > -1) {
                                n++;
                                i = k + 1;
                            } else {
                                n = 1;
                                IArgBlock rarg = CMS.createArgBlock();

                                if (k > -1) {
                                    rarg.addStringValue("crlBase64Encoded", crlBase64Encoded.substring(j, k));
                                    i = k + 1;
                                    j = i;
                                } else {
                                    rarg.addStringValue("crlBase64Encoded", crlBase64Encoded.substring(j, length));
                                    i = length;
                                }
                                argSet.addRepeatRecord(rarg);
                            }
                        }
                    } catch (CRLException e) {
                    }
                } else if (crlDisplayType.equals("deltaCRL")) {
                    header.addIntegerValue("deltaCRLSize",
                        crl.getNumberOfRevokedCertificates());

                    ICRLPrettyPrint crlDetails = CMS.getCRLPrettyPrint(crl);

                    header.addStringValue(
                        "crlPrettyPrint", crlDetails.toString(locale[0], 0, 0, 0));

                    try {
                        byte[] ba = crl.getEncoded();
                        String crlBase64Encoded = com.netscape.osutil.OSUtil.BtoA(ba);
                        int length = crlBase64Encoded.length();
                        int i = 0;
                        int j = 0;
                        int n = 1;

                        while (i < length) {
                            int k = crlBase64Encoded.indexOf('\n', i);

                            if (n < 100 && k > -1) {
                                n++;
                                i = k + 1;
                            } else {
                                n = 1;
                                IArgBlock rarg = CMS.createArgBlock();

                                if (k > -1) {
                                    rarg.addStringValue("crlBase64Encoded", crlBase64Encoded.substring(j, k));
                                    i = k + 1;
                                    j = i;
                                } else {
                                    rarg.addStringValue("crlBase64Encoded", crlBase64Encoded.substring(j, length));
                                    i = length;
                                }
                                argSet.addRepeatRecord(rarg);
                            }
                        }
                    } catch (CRLException e) {
                    }
                }
            }

            try {
                ServletOutputStream out = httpResp.getOutputStream();

                httpResp.setContentType("text/html");
                form.renderOutput(out, argSet);
                cmsReq.setStatus(CMSRequest.SUCCESS);
            } catch (IOException e) {
                log(ILogger.LL_FAILURE, 
                    CMS.getLogMessage("CMSGW_ERR_OUT_STREAM_TEMPLATE", e.toString()));
                cmsReq.setError(new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR")));
                cmsReq.setStatus(CMSRequest.ERROR);
            }
            return;
        } else if (op.equals("importCRL") || op.equals("importDeltaCRL")) {
            if (clientIsMSIE(httpReq))
                mimeType = "application/pkix-crl";
            else
                mimeType = "application/x-pkcs7-crl";
        } else if (op.equals("getCRL")) {
            mimeType = "application/octet-stream";
            httpResp.setHeader("Content-disposition",
                "attachment; filename=" + crlId + ".crl");
        } else if (op.equals("getDeltaCRL")) {
            mimeType = "application/octet-stream";
            httpResp.setHeader("Content-disposition",
                "attachment; filename=delta-" + crlId + ".crl");
        } else {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_INVALID_OPTIONS_SELECTED"));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_INVALID_OPTIONS_SELECTED"));
        }

        try {
            //            if (clientIsMSIE(httpReq) &&  op.equals("getCRL"))
            //                httpResp.setHeader("Content-disposition",
            //                  "attachment; filename=getCRL.crl");
            httpResp.setContentType(mimeType);
            httpResp.setContentLength(bytes.length);
            httpResp.getOutputStream().write(bytes);
            httpResp.getOutputStream().flush();
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSGW_ERROR_DISPLAYING_CRLINFO"));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAYING_CRLINFO_ERROR"));
        }
        //		cmsReq.setResult(null);
        cmsReq.setStatus(CMSRequest.SUCCESS);
        return;
    }

    private String hexToDecimal(String hex) {
        String newHex = hex.substring(2);
        BigInteger bi = new BigInteger(newHex, 16);

        return bi.toString();
    }
}
