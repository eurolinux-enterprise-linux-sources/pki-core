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
package com.netscape.cms.ocsp;


import netscape.ldap.*;
import java.math.*;
import java.io.*;
import java.util.*;
import com.netscape.certsrv.common.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.ocsp.*;
import com.netscape.certsrv.dbs.crldb.*;

import java.security.*;
import java.security.cert.*;
import netscape.security.x509.*;
import netscape.security.util.*;
import com.netscape.certsrv.util.*;
import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.dbs.repository.*;
import com.netscape.certsrv.dbs.crldb.ICRLRepository;
import com.netscape.cmsutil.ocsp.*;

import org.mozilla.jss.asn1.*;
import org.mozilla.jss.pkix.primitive.*;
import org.mozilla.jss.pkix.cert.Extension;


/**
 * This is the LDAP OCSP store. It reads CA certificate and
 * revocation list attributes from the CA entry.
 *
 * @version $Revision: 1304 $, $Date: 2010-09-20 16:38:55 -0700 (Mon, 20 Sep 2010) $
 */
public class LDAPStore implements IDefStore, IExtendedPluginInfo {
    private static final String PROP_NUM_CONNS = "numConns";
    private static final String PROP_REFRESH_IN_SEC = "refreshInSec";
    private static final int DEF_REFRESH_IN_SEC = 60 * 60 * 24;
    private static final String PROP_BASE_DN = "baseDN";
    private static final String PROP_BY_NAME = "byName";
    private static final String PROP_CONN_INFO = "connInfo";
    private static final String PROP_CRL_ATTR = "crlAttr";
    private static final String DEF_CRL_ATTR = "certificateRevocationList;binary";
    private static final String PROP_CA_CERT_ATTR = "caCertAttr";
    private static final String DEF_CA_CERT_ATTR = "cACertificate;binary";
    private static final String PROP_HOST = "host";
    private static final String PROP_PORT = "port";

    private final static String PROP_NOT_FOUND_GOOD = "notFoundAsGood";
    private final static String PROP_INCLUDE_NEXT_UPDATE = 
        "includeNextUpdate";

    private IOCSPAuthority mOCSPAuthority = null;
    private IConfigStore mConfig = null;
    private String mId = null;
    private String mCRLAttr = null;
    private boolean mByName = true;
    private String mCACertAttr = null;
    protected Hashtable mReqCounts = new Hashtable();
    private Hashtable mCRLs = new Hashtable();

    /**
     * Constructs the default store.
     */
    public LDAPStore() {
    }

    public String[] getExtendedPluginInfo(Locale locale) { 
        Vector v = new Vector(); 

        v.addElement(PROP_NOT_FOUND_GOOD + ";boolean; " + CMS.getUserMessage(locale, "CMS_OCSP_LDAPSTORE_PROP_NOT_FOUND_GOOD"));
        v.addElement(PROP_INCLUDE_NEXT_UPDATE + ";boolean; " + CMS.getUserMessage(locale, "CMS_OCSP_LDAPSTORE_PROP_INCLUDE_NEXT_UPDATE"));
        v.addElement(PROP_NUM_CONNS + ";number; " + CMS.getUserMessage(locale, "CMS_OCSP_LDAPSTORE_PROP_NUM_CONNS"));
        v.addElement(PROP_BY_NAME + ";boolean; " + CMS.getUserMessage(locale, "CMS_OCSP_LDAPSTORE_PROP_BY_NAME"));
        v.addElement(PROP_CRL_ATTR + ";string; " + CMS.getUserMessage(locale, "CMS_OCSP_LDAPSTORE_PROP_CRL_ATTR"));
        v.addElement(PROP_CA_CERT_ATTR + ";string; " + CMS.getUserMessage(locale, "CMS_OCSP_LDAPSTORE_PROP_CA_CERT_ATTR"));
        v.addElement(IExtendedPluginInfo.HELP_TEXT + "; " + CMS.getUserMessage(locale, "CMS_OCSP_LDAPSTORE_DESC"));
        v.addElement(IExtendedPluginInfo.HELP_TOKEN + ";configuration-ocspstores-ldapstore"); 
        return com.netscape.cmsutil.util.Utils.getStringArrayFromVector(v); 
    }

    /**
     * Fetch CA certificate and CRL from LDAP server.
     */
    public void init(ISubsystem owner, IConfigStore config) 	
        throws EBaseException {
        mOCSPAuthority = (IOCSPAuthority) owner;
        mConfig = config;

        mCRLAttr = mConfig.getString(PROP_CRL_ATTR, DEF_CRL_ATTR);
        mCACertAttr = mConfig.getString(PROP_CA_CERT_ATTR, 
                    DEF_CA_CERT_ATTR);
        mByName = mConfig.getBoolean(PROP_BY_NAME, true);
		
    }

    /**
     * Locates the CA certificate.
     */
    public X509CertImpl locateCACert(LDAPConnection conn, String baseDN) 
        throws EBaseException {
        try {
            LDAPSearchResults results = conn.search(baseDN, 
                    LDAPv2.SCOPE_SUB, mCACertAttr + "=*", 
                    null, false);

            if (!results.hasMoreElements()) {
                throw new EBaseException("error - no entry");
            }
            LDAPEntry entry = results.next();
            LDAPAttribute crls = entry.getAttribute(mCACertAttr);
            Enumeration vals = crls.getByteValues();

            if (!vals.hasMoreElements()) {
                throw new EBaseException("error - no values");
            }
            byte caCertData[] = (byte[]) vals.nextElement();
            X509CertImpl caCert = new X509CertImpl(caCertData);

            return caCert;
        } catch (Exception e) {
            CMS.debug("LDAPStore: locateCACert " + e.toString());
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("OCSP_LOCATE_CA", e.toString()));
        }
        return null;
    }

    /**
     * Locates the CRL.
     */
    public X509CRLImpl locateCRL(LDAPConnection conn, String baseDN) 
        throws EBaseException {
        try {
            LDAPSearchResults results = conn.search(baseDN, 
                    LDAPv2.SCOPE_SUB, mCRLAttr + "=*", 
                    null, false);

            if (!results.hasMoreElements()) {
                throw new EBaseException("error - no entry");
            }
            LDAPEntry entry = results.next();
            LDAPAttribute crls = entry.getAttribute(mCRLAttr);
            Enumeration vals = crls.getByteValues();

            if (!vals.hasMoreElements()) {
                throw new EBaseException("error - no values");
            }
            byte crlData[] = (byte[]) vals.nextElement();
            X509CRLImpl crl = new X509CRLImpl(crlData);

            return crl;
        } catch (Exception e) {
            CMS.debug("LDAPStore: locateCRL " + e.toString());
            log(ILogger.LL_FAILURE, 
                CMS.getLogMessage("OCSP_LOCATE_CRL", e.toString()));
        }
        return null;
    }

    public void updateCRLHash(X509CertImpl caCert, X509CRLImpl crl) 
        throws EBaseException {
        X509CRLImpl oldCRL = (X509CRLImpl) mCRLs.get(caCert);

        if (oldCRL != null) {
            if (oldCRL.getThisUpdate().getTime() >= 
                crl.getThisUpdate().getTime()) {
                log(ILogger.LL_INFO, 
                    "LDAPStore: no update, received CRL is older than current CRL");
                return; // no update
            }
        }
        CMS.debug("Added '" + caCert.getSubjectDN().toString() + "' into CRL hash");
        mCRLs.put(caCert, crl);
    }

    public void log(int level, String msg) {
        mOCSPAuthority.log(level, msg);
    }

    public void startup() throws EBaseException {
        int num = mConfig.getInteger(PROP_NUM_CONNS, 0);

        for (int i = 0; i < num; i++) {
            String host = mConfig.getString(PROP_HOST + Integer.toString(i), null);
            int port = mConfig.getInteger(PROP_PORT + Integer.toString(i), 0);
            LDAPConnection c = new LDAPConnection();

            try {
                c.connect(host, port);
            } catch (LDAPException e) {
                throw new EBaseException("LDAP " + e);
            }
            String baseDN = mConfig.getString(PROP_BASE_DN + Integer.toString(i), null);
            CRLUpdater updater = new CRLUpdater(
                    this, c, baseDN,
                    mConfig.getInteger(PROP_REFRESH_IN_SEC + Integer.toString(i), 
                        DEF_REFRESH_IN_SEC));

            updater.start();
        }
    }

    public void shutdown() {
    }

    public IConfigStore getConfigStore() {
        return mConfig;
    }

    public void setId(String id) throws EBaseException {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    /**
     * Validate an OCSP request.
     */
    public OCSPResponse validate(OCSPRequest request) 
        throws EBaseException {	

        IStatsSubsystem statsSub = (IStatsSubsystem)CMS.getSubsystem("stats");

        mOCSPAuthority.incNumOCSPRequest(1);
        long startTime = CMS.getCurrentDate().getTime();
        try {
            mOCSPAuthority.log(ILogger.LL_INFO, "start OCSP request");
            TBSRequest tbsReq = request.getTBSRequest();

            Vector singleResponses = new Vector();

            if (statsSub != null) {
              statsSub.startTiming("lookup");
            }

            long lookupStartTime = CMS.getCurrentDate().getTime();
            for (int i = 0; i < tbsReq.getRequestCount(); i++) {
                com.netscape.cmsutil.ocsp.Request req = 
                    tbsReq.getRequestAt(i);
                CertID cid = req.getCertID();
                SingleResponse sr = processRequest(cid);

                singleResponses.addElement(sr);
            }
            long lookupEndTime = CMS.getCurrentDate().getTime();
            if (statsSub != null) {
              statsSub.endTiming("lookup");
            }
            mOCSPAuthority.incLookupTime(lookupEndTime - lookupStartTime);

            if (statsSub != null) {
              statsSub.startTiming("build_response");
            }
            SingleResponse res[] = new SingleResponse[singleResponses.size()];

            singleResponses.copyInto(res);

            ResponderID rid = null;

            if (mByName) {
                rid = mOCSPAuthority.getResponderIDByName();
            } else {
                rid = mOCSPAuthority.getResponderIDByHash();
            }

            Extension nonce[] = null;

            for (int j = 0; j < tbsReq.getExtensionsCount(); j++) {
                Extension thisExt = tbsReq.getRequestExtensionAt(j);

                if (thisExt.getExtnId().equals(IOCSPAuthority.OCSP_NONCE)) {
                    nonce = new Extension[1];
                    nonce[0] = thisExt;
                }
            }

            ResponseData rd = new ResponseData(rid, 
                    new GeneralizedTime(CMS.getCurrentDate()), res, nonce);
            if (statsSub != null) {
              statsSub.endTiming("build_response");
            }

            if (statsSub != null) {
              statsSub.startTiming("signing");
            }

            long signStartTime = CMS.getCurrentDate().getTime();
            BasicOCSPResponse basicRes = mOCSPAuthority.sign(rd);
            long signEndTime = CMS.getCurrentDate().getTime();
            mOCSPAuthority.incSignTime(signEndTime - signStartTime);
            if (statsSub != null) {
              statsSub.endTiming("signing");
            }

            OCSPResponse response = new OCSPResponse(
                    OCSPResponseStatus.SUCCESSFUL, 
                    new ResponseBytes(ResponseBytes.OCSP_BASIC, 
                        new OCTET_STRING(ASN1Util.encode(basicRes))));

            log(ILogger.LL_INFO, "done OCSP request");
            long endTime = CMS.getCurrentDate().getTime();
            mOCSPAuthority.incTotalTime(endTime - startTime);
            return response;
        } catch (Exception e) {
            CMS.debug("LDAPStore: validation " + e.toString());
            log(ILogger.LL_FAILURE, CMS.getLogMessage("OCSP_REQUEST_FAILURE", e.toString()));
            return null;
        }
    }

    public int getStateCount() {
        return 0;
    }

    public long getReqCount(String id) {
        Long c = (Long) mReqCounts.get(id);

        if (c == null)
            return 0;
        else
            return c.longValue();
    }

    public IRepositoryRecord createRepositoryRecord() {
        return null;
    }

    public void addRepository(String name, String thisUpdate,
        IRepositoryRecord rec)
        throws EBaseException {
        throw new EBaseException("NOT SUPPORTED");
    }

    public boolean waitOnCRLUpdate() {
        return false;
    }

    public void updateCRL(X509CRL crl) throws EBaseException {
        throw new EBaseException("NOT SUPPORTED");
    }

    public ICRLIssuingPointRecord readCRLIssuingPoint(String name)
        throws EBaseException {
        throw new EBaseException("NOT SUPPORTED");
    }

    public Enumeration searchAllCRLIssuingPointRecord(int maxSize)
        throws EBaseException {
        Vector recs = new Vector();
        Enumeration keys = mCRLs.keys();

        while (keys.hasMoreElements()) {
            X509CertImpl caCert = (X509CertImpl) keys.nextElement();
            X509CRLImpl crl = (X509CRLImpl) mCRLs.get(caCert);

            recs.addElement(new TempCRLIssuingPointRecord(caCert, crl));
        }
        return recs.elements();
    }

    public Enumeration searchCRLIssuingPointRecord(String filter,
        int maxSize)
        throws EBaseException {
        return null;
    }

    public ICRLIssuingPointRecord createCRLIssuingPointRecord(
        String name, BigInteger crlNumber,
        Long crlSize, Date thisUpdate, Date nextUpdate) {
        return null;
    }

    public void addCRLIssuingPoint(String name, ICRLIssuingPointRecord rec)
        throws EBaseException {
        throw new EBaseException("NOT SUPPORTED");
    }

    public void deleteCRLIssuingPointRecord(String id)
       throws EBaseException  {
       throw new EBaseException("NOT SUPPORTED");
    }

    public boolean isNotFoundGood() {
        try {
            return isNotFoundGood1();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean includeNextUpdate() throws EBaseException {
        return mConfig.getBoolean(PROP_INCLUDE_NEXT_UPDATE, false);
    } 

    public boolean isNotFoundGood1() throws EBaseException {
        return mConfig.getBoolean(PROP_NOT_FOUND_GOOD, true);
    }

    public void incReqCount(String id) {
        mReqCounts.put(id, Long.valueOf(getReqCount(id) + 1));
    }

    /**
     * Check against the database for status.
     */
    private SingleResponse processRequest(CertID cid) throws EBaseException {
        // locate the right CRL
        X509CertImpl theCert = null;
        X509CRLImpl theCRL = null;

        Enumeration caCerts = mCRLs.keys();

        while (caCerts.hasMoreElements()) {
            X509CertImpl caCert = (X509CertImpl) caCerts.nextElement();
            MessageDigest md = null;

            try {
                md = MessageDigest.getInstance(
                            mOCSPAuthority.getDigestName(cid.getHashAlgorithm()));
            } catch (Exception e) {
            }
            X509Key key = (X509Key) caCert.getPublicKey();

            if( key == null ) {
                System.out.println("LDAPStore::processRequest - key is null!");
                return null;
            }

            byte digest[] = md.digest(key.getKey());
            byte keyhsh[] = cid.getIssuerKeyHash().toByteArray();

            if (mOCSPAuthority.arraysEqual(digest, keyhsh)) {
                theCert = caCert;
                incReqCount(caCert.getSubjectDN().toString());
                theCRL = (X509CRLImpl) mCRLs.get(caCert);
                break;
            }
        }

        if (theCert == null) {
            return null;
        }

        if (theCRL == null) {
            return null;
        }

        GeneralizedTime thisUpdate = new GeneralizedTime(
                theCRL.getThisUpdate());
        GeneralizedTime nextUpdate = null;

        if (includeNextUpdate()) {
            nextUpdate = new GeneralizedTime(
                        theCRL.getNextUpdate());
        }

        CertStatus certStatus = null;
        X509CRLEntry entry = theCRL.getRevokedCertificate(
                cid.getSerialNumber());

        if (entry == null) {
            if (isNotFoundGood1()) { 
                certStatus = new GoodInfo(); 
            } else { 
                certStatus = new UnknownInfo(); 
            }
        } else {
            certStatus = new RevokedInfo(new GeneralizedTime(
                            entry.getRevocationDate()));
        }
		
        return new SingleResponse(cid, certStatus, thisUpdate, nextUpdate);
    }

    /**
     * Provides configuration parameters.
     */
    public NameValuePairs getConfigParameters() { 
        try {
            NameValuePairs params = new NameValuePairs(); 

            params.add(Constants.PR_OCSPSTORE_IMPL_NAME, 
                mConfig.getString("class"));
            int num = mConfig.getInteger(PROP_NUM_CONNS, 0);

            params.add(PROP_NUM_CONNS, Integer.toString(num));
            for (int i = 0; i < num; i++) {
                params.add(PROP_HOST + Integer.toString(i), 
                    mConfig.getString(PROP_HOST + 
                        Integer.toString(i), ""));
                params.add(PROP_PORT + Integer.toString(i), 
                    mConfig.getString(PROP_PORT + 
                        Integer.toString(i), "389"));
                params.add(PROP_BASE_DN + Integer.toString(i), 
                    mConfig.getString(PROP_BASE_DN + 
                        Integer.toString(i), ""));
                params.add(PROP_REFRESH_IN_SEC + Integer.toString(i), 
                    mConfig.getString(PROP_REFRESH_IN_SEC + 
                        Integer.toString(i), Integer.toString(DEF_REFRESH_IN_SEC)));
            }
            params.add(PROP_BY_NAME, 
                mConfig.getString(PROP_BY_NAME, "true"));
            params.add(PROP_CA_CERT_ATTR, 
                mConfig.getString(PROP_CA_CERT_ATTR, DEF_CA_CERT_ATTR));
            params.add(PROP_CRL_ATTR,
                mConfig.getString(PROP_CRL_ATTR, DEF_CRL_ATTR));
            params.add(PROP_NOT_FOUND_GOOD,
                mConfig.getString(PROP_NOT_FOUND_GOOD, "true"));
            params.add(PROP_INCLUDE_NEXT_UPDATE,
                mConfig.getString(PROP_INCLUDE_NEXT_UPDATE, "false"));
            return params;
        } catch (Exception e) {
            return null;
        }
    }

    public void setConfigParameters(NameValuePairs pairs)
        throws EBaseException {
        Enumeration k = pairs.getNames();

        while (k.hasMoreElements()) {
            String key = (String) k.nextElement();

            mConfig.put(key, pairs.getValue(key));
        }
    }
}


class CRLUpdater extends Thread {
    private LDAPConnection mC = null;
    private String mBaseDN = null;
    private int mSec = 0;
    private LDAPStore mStore = null;

    public CRLUpdater(LDAPStore store, LDAPConnection c, 
        String baseDN, int sec) {
        mC = c;
        mSec = sec;
        mBaseDN = baseDN;
        mStore = store;
    }

    public void run() {
        while (true) {
            try {
                LDAPConnection conn = mC;
                CMS.debug("Started CRL Update '" + mBaseDN);
                X509CertImpl caCert = mStore.locateCACert(conn, mBaseDN);
                X509CRLImpl crl = mStore.locateCRL(conn, mBaseDN);

                mStore.updateCRLHash(caCert, crl);
                CMS.debug("Finished CRL Update - '" + mBaseDN);
                sleep(mSec * 1000); // turn sec into millis-sec
            } catch (Exception e) {
                // ignore
            }
        }
    }
}


class TempCRLIssuingPointRecord implements ICRLIssuingPointRecord {
    private X509CertImpl mCACert = null;
    private X509CRLImpl mCRL = null;

    TempCRLIssuingPointRecord(X509CertImpl caCert, X509CRLImpl crl) {
        mCACert = caCert;
        mCRL = crl;
    }

    public String getId() {
        return mCACert.getSubjectDN().toString();
    }

    /**
     * Retrieves CRL serial number.
     */
    public BigInteger getCRLNumber() {
        return null;
    }

    /**
     * Retrieves delta CRL serial number.
     */
    public BigInteger getDeltaCRLNumber() {
        return null;
    }

    /**
     * Retrieves CRL size.
     */
    public Long getCRLSize() {
        return Long.valueOf(mCRL.getNumberOfRevokedCertificates());
    }

    /**
     * Retrieves CRL size.
     */
    public Long getDeltaCRLSize() {
        return Long.valueOf(-1);
    }

    /**
     * Retrieves this update time.
     */
    public Date getThisUpdate() {
        return mCRL.getThisUpdate();
    }

    /**
     * Retrieves next update time.
     */
    public Date getNextUpdate() {
        return mCRL.getNextUpdate();
    }

    public String getFirstUnsaved() {
        return null;
    }

    public Hashtable getCRLCacheNoClone() {
        return null;
    }

    public Hashtable getCRLCache() {
        return null;
    }

    /**
     * Retrieves CRL encodings.
     */
    public byte[] getCRL() {
        try {
            return mCRL.getEncoded();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves CRL encodings.
     */
    public byte[] getDeltaCRL() {
        return null;
    }

    public Vector getSplitTimes() {
        return null;
    }

    public int isCRLIssuingPointInitialized() {
        return 1;
    }

    public byte[] getCACert() {
        try {
            return mCACert.getEncoded();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves cache info of revoked certificates.
     */
    public Hashtable getRevokedCerts() {
        return mCRL.getListOfRevokedCertificates();
    }

    /**
     * Retrieves cache info of unrevoked certificates.
     */
    public Hashtable getUnrevokedCerts() {
        return null;
    }

    /**
     * Retrieves cache info of expired certificates.
     */
    public Hashtable getExpiredCerts() {
        return null;
    }

    public Enumeration getSerializableAttrNames() {
        return null;
    }

    public void set(String name, Object obj)throws EBaseException {
    }

    public Object get(String name) throws EBaseException {
        return null;
    }

    public void delete(String name) throws EBaseException {
	
    }

    public Enumeration getElements() {
        return null;
    }
}
