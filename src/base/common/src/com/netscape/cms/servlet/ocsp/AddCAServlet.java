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
package com.netscape.cms.servlet.ocsp;


import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;

import java.math.*;
import java.util.Vector;
import java.io.InputStream;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.mozilla.jss.asn1.INTEGER;
import org.mozilla.jss.pkix.cert.Certificate;
import org.mozilla.jss.pkix.primitive.AlgorithmIdentifier;
import org.mozilla.jss.asn1.BIT_STRING;

import com.netscape.certsrv.base.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.ocsp.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.dbs.crldb.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.authorization.*;
import com.netscape.cms.servlet.*;
import com.netscape.cmsutil.util.*;

import netscape.security.pkcs.*;
import netscape.security.x509.*;
import java.security.cert.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;


/**
 * Configure the CA to respond to OCSP requests for a CA
 *
 * @version $Revision: 1275 $ $Date: 2010-09-07 22:26:21 -0700 (Tue, 07 Sep 2010) $
 */
public class AddCAServlet extends CMSServlet {
	
    public static final String BEGIN_HEADER =
        "-----BEGIN CERTIFICATE-----";
    public static final String END_HEADER =
        "-----END CERTIFICATE-----";

    public static final BigInteger BIG_ZERO = new BigInteger("0");
    public static final Long MINUS_ONE = Long.valueOf(-1);

    private final static String TPL_FILE = "addCA.template";
    private String mFormPath = null;
    private IOCSPAuthority mOCSPAuthority = null;

    private final static String LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST =
        "LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_3";
    private final static String LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED =
        "LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED_3";

    public AddCAServlet() {
        super();
    }

    /**
     * initialize the servlet. This servlet uses the template file
     * "addCA.template" to process the response.
     *
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        // override success to display own output.

        mFormPath = "/" + mAuthority.getId() + "/" + TPL_FILE;
        mTemplates.remove(CMSRequest.SUCCESS);
        mOCSPAuthority = (IOCSPAuthority) mAuthority;

        if (mOutputTemplatePath != null)
            mFormPath = mOutputTemplatePath;
    }

    /**
     * Process the HTTP request.
     * <ul>
     * <li>http.param cert ca certificate. The format is base-64, DER
     *    encoded, wrapped with -----BEGIN CERTIFICATE-----, 
     *    -----END CERTIFICATE----- strings 
     * <li>signed.audit LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST used when
     * a CA is attempted to be added to the OCSP responder
     * <li>signed.audit LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED
     * used when an add CA request to the OCSP Responder is processed
     * </ul>
     *
     * @param cmsReq the object holding the request and response information
     */
    protected void process(CMSRequest cmsReq)
        throws EBaseException {
        HttpServletRequest req = cmsReq.getHttpReq();
        HttpServletResponse resp = cmsReq.getHttpResp();
        String auditMessage = null;
        String auditSubjectID = auditSubjectID();
        String auditCA = ILogger.SIGNED_AUDIT_EMPTY_VALUE;
        String auditCASubjectDN = ILogger.SIGNED_AUDIT_EMPTY_VALUE;

        IAuthToken authToken = authenticate(cmsReq);

        AuthzToken authzToken = null;

        try {
            authzToken = authorize(mAclMethod, authToken,
                        mAuthzResourceName, "add");
        } catch (Exception e) {
            // do nothing for now
        }

        if (authzToken == null) {
            cmsReq.setStatus(CMSRequest.UNAUTHORIZED);
            return;
        }

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

        IArgBlock header = CMS.createArgBlock();
        IArgBlock fixed = CMS.createArgBlock();
        CMSTemplateParams argSet = new CMSTemplateParams(header, fixed);

        if (auditSubjectID.equals(ILogger.NONROLEUSER) ||
               auditSubjectID.equals(ILogger.UNIDENTIFIED))  {
            String uid = authToken.getInString(IAuthToken.USER_ID);
            if (uid != null) {
                CMS.debug("AddCAServlet: auditSubjectID set to "+uid);
                auditSubjectID = uid;
            }
        }
        String b64 = cmsReq.getHttpReq().getParameter("cert");

        if (b64 == null) {
            auditMessage = CMS.getLogMessage(
                LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST,
                auditSubjectID,
                ILogger.FAILURE,
                ILogger.SIGNED_AUDIT_EMPTY_VALUE);

            audit( auditMessage );

            throw new ECMSGWException(CMS.getUserMessage(getLocale(req), "CMS_GW_MISSING_CA_CERT"));
        }

        auditCA = Cert.normalizeCertStr(Cert.stripCertBrackets(b64.trim()));
        // record the fact that a request to add CA is made
        auditMessage = CMS.getLogMessage(
            LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST,
            auditSubjectID,
            ILogger.SUCCESS,
            auditCA);

        audit( auditMessage );

        if (b64.indexOf(BEGIN_HEADER) == -1) {
            auditMessage = CMS.getLogMessage(
                LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED,
                auditSubjectID,
                ILogger.FAILURE,
                auditCASubjectDN);

            audit( auditMessage );

            throw new ECMSGWException(CMS.getUserMessage(getLocale(req), "CMS_GW_MISSING_CERT_HEADER"));
        }
        if (b64.indexOf(END_HEADER) == -1) {
            auditMessage = CMS.getLogMessage(
                LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED,
                auditSubjectID,
                ILogger.FAILURE,
                auditCASubjectDN);

            audit( auditMessage );

            throw new ECMSGWException(CMS.getUserMessage(getLocale(req), "CMS_GW_MISSING_CERT_FOOTER"));
        }

        IDefStore defStore = mOCSPAuthority.getDefaultStore();

        X509Certificate leafCert = null;
        X509Certificate certs[] = null;

        try {
            X509Certificate cert = Cert.mapCert(b64);

            if( cert == null ) {
                CMS.debug( "AddCAServlet::process() - cert is null!" );
                auditMessage = CMS.getLogMessage(
                    LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED,
                    auditSubjectID,
                    ILogger.FAILURE,
                    auditCASubjectDN);

                audit( auditMessage );

                throw new EBaseException( "cert is null" );
            } else {
                certs = new X509Certificate[1];
            }

            certs[0] = cert;
            leafCert = cert;
            auditCASubjectDN = leafCert.getSubjectDN().getName();
        } catch (Exception e) {
        }
        if (certs == null) {
            try {
                // this could be a chain
                certs = Cert.mapCertFromPKCS7(b64);
                if (certs[0].getSubjectDN().getName().equals(certs[0].getIssuerDN().getName())) {
                    leafCert = certs[certs.length - 1];
                } else {
                    leafCert = certs[0];
                }
                auditCASubjectDN = leafCert.getSubjectDN().getName();
            } catch (Exception e) {
                auditMessage = CMS.getLogMessage(
                    LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED,
                    auditSubjectID,
                    ILogger.FAILURE,
                    auditCASubjectDN);

                audit( auditMessage );

                throw new ECMSGWException(
                  CMS.getUserMessage("CMS_GW_ENCODING_CA_CHAIN_ERROR"));
            }
        }
        if (certs != null && certs.length > 0) {
            // (1) need to normalize (sort) the chain

            // (2) store certificate (and certificate chain) into
            // database
            ICRLIssuingPointRecord rec = defStore.createCRLIssuingPointRecord(
                    leafCert.getSubjectDN().getName(),  
                    BIG_ZERO, 
                    MINUS_ONE, null, null);

            try {
                rec.set(ICRLIssuingPointRecord.ATTR_CA_CERT, leafCert.getEncoded());
            } catch (Exception e) {
                auditMessage = CMS.getLogMessage(
                    LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED,
                    auditSubjectID,
                    ILogger.FAILURE,
                    auditCASubjectDN);

                audit( auditMessage );

                // error
            }
            defStore.addCRLIssuingPoint(leafCert.getSubjectDN().getName(), rec);
            log(ILogger.EV_AUDIT, AuditFormat.LEVEL, "Added CA certificate " + leafCert.getSubjectDN().getName());
            auditMessage = CMS.getLogMessage(
                LOGGING_SIGNED_AUDIT_OCSP_ADD_CA_REQUEST_PROCESSED,
                auditSubjectID,
                ILogger.SUCCESS,
                auditCASubjectDN);

            audit( auditMessage );
        }

        try {
            ServletOutputStream out = resp.getOutputStream();
            String error = null;

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
                //  cmsReq.setError(error);
            }
        } catch (IOException e) {
            log(ILogger.LL_FAILURE,
                CMS.getLogMessage("CMSGW_ERR_STREAM_TEMPLATE", e.toString()));
            throw new ECMSGWException(
              CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
        }
    }
}
