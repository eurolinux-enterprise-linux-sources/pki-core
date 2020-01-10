package com.netscape.certsrv.app;

import com.netscape.certsrv.apps.ICMSEngine;
import com.netscape.certsrv.apps.ICommandQueue;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.logging.ILogger;
import com.netscape.certsrv.request.IRequest;
import com.netscape.certsrv.acls.IACL;
import com.netscape.certsrv.acls.EACLsException;
import com.netscape.certsrv.dbs.crldb.ICRLIssuingPointRecord;
import com.netscape.certsrv.dbs.repository.IRepositoryRecord;
import com.netscape.certsrv.connector.*;
import com.netscape.certsrv.ca.ICRLIssuingPoint;
import com.netscape.certsrv.ldap.ILdapConnInfo;
import com.netscape.certsrv.ldap.ELdapException;
import com.netscape.certsrv.ldap.ILdapAuthInfo;
import com.netscape.certsrv.ldap.ILdapConnFactory;
import com.netscape.certsrv.password.IPasswordCheck;
import com.netscape.certsrv.notification.*;
import com.netscape.certsrv.policy.IGeneralNamesConfig;
import com.netscape.certsrv.policy.IGeneralNameAsConstraintsConfig;
import com.netscape.certsrv.policy.IGeneralNamesAsConstraintsConfig;
import com.netscape.certsrv.policy.ISubjAltNameConfig;
import com.netscape.certsrv.authority.IAuthority;
import com.netscape.cmsutil.net.ISocketFactory;
import com.netscape.cmsutil.password.IPasswordStore;

import java.util.*;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.cert.X509CRL;
import java.security.NoSuchAlgorithmException;

import netscape.security.x509.Extension;
import netscape.security.x509.X509CertInfo;
import netscape.security.x509.GeneralName;
import netscape.security.util.ObjectIdentifier;
import netscape.ldap.LDAPSSLSocketFactoryExt;
import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPException;
import org.mozilla.jss.util.PasswordCallback;

/**
 * Default engine stub for testing.
 */
public class CMSEngineDefaultStub implements ICMSEngine {
    public String getId() {
        return null;
    }

    public void setId(String id) throws EBaseException {
    }

    public void init(ISubsystem owner, IConfigStore config) throws EBaseException {
    }

    public void startup() throws EBaseException {
    }

    public void shutdown() {
    }

    public IConfigStore getConfigStore() {
        return null;
    }

    public int getpid() {
        return 0;
    }

    public void reinit(String id) throws EBaseException {
    }

    public int getCSState() {
        return 0;
    }

    public void setCSState(int mode) {
    }

    public boolean isPreOpMode() {
        return false;
    }

    public boolean isRunningMode() {
        return false;
    }

    public String getInstanceDir() {
        return null;
    }

    public Date getCurrentDate() {
        return null;
    }

    public long getStartupTime() {
        return 0;
    }

    public boolean isInRunningState() {
        return false;
    }

    public Enumeration getSubsystemNames() {
        return null;
    }

    public Enumeration getSubsystems() {
        return null;
    }

    public ISubsystem getSubsystem(String name) {
        return null;
    }

    public ILogger getLogger() {
        return null;
    }

    public ILogger getSignedAuditLogger() {
        return null;
    }

    public void debug(byte data[]) {
    }

    public void debug(String msg) {
    }

    public void debug(int level, String msg) {
    }

    public void debug(Throwable e) {
    }

    public boolean debugOn() {
        return false;
    }

    public void debugStackTrace() {
    }

    public void traceHashKey(String type, String key) {
    }

    public void traceHashKey(String type, String key, String val) {
    }

    public void traceHashKey(String type, String key, String val, String def) {
    }

    public byte[] getPKCS7(Locale locale, IRequest req) {
        return new byte[0];
    }

    public String getUserMessage(Locale locale, String msgID) {
        return null;
    }

    public String getUserMessage(Locale locale, String msgID, String p[]) {
        return null;
    }

    public String getUserMessage(Locale locale, String msgID, String p1) {
        return null;
    }

    public String getUserMessage(Locale locale, String msgID, String p1, String p2) {
        return null;
    }

    public String getUserMessage(Locale locale, String msgID, String p1, String p2, String p3) {
        return null;
    }

    public String getLogMessage(String msgID) {
        return null;
    }

    public String getLogMessage(String msgID, String p[]) {
        return null;
    }

    public String getLogMessage(String msgID, String p1) {
        return null;
    }

    public String getLogMessage(String msgID, String p1, String p2) {
        return null;
    }

    public String getLogMessage(String msgID, String p1, String p2, String p3) {
        return null;
    }

    public String getLogMessage(String msgID, String p1, String p2, String p3, String p4) {
        return null;
    }

    public String getLogMessage(String msgID, String p1, String p2, String p3, String p4, String p5) {
        return null;
    }

    public String getLogMessage(String msgID, String p1, String p2, String p3, String p4, String p5, String p6) {
        return null;
    }

    public String getLogMessage(String msgID, String p1, String p2, String p3, String p4, String p5, String p6, String p7) {
        return null;
    }

    public String getLogMessage(String msgID, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8) {
        return null;
    }

    public String getLogMessage(String msgID, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9) {
        return null;
    }

    public IACL parseACL(String resACLs) throws EACLsException {
        return null;
    }

    public ICRLIssuingPointRecord createCRLIssuingPointRecord(String id, BigInteger crlNumber, Long crlSize, Date thisUpdate, Date nextUpdate) {
        return null;
    }

    public String getCRLIssuingPointRecordName() {
        return null;
    }

    public String getFingerPrint(Certificate cert) throws CertificateEncodingException, NoSuchAlgorithmException {
        return null;
    }

    public String getFingerPrints(Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        return null;
    }/*
    * Returns the finger print of the given certificate.
*
* @param certDer DER byte array of certificate
* @return finger print of certificate
*/
public String getFingerPrints(byte[] certDer) throws NoSuchAlgorithmException {
    return null;
}

    public IRepositoryRecord createRepositoryRecord() {
        return null;
    }

    public IPKIMessage getHttpPKIMessage() {
        return null;
    }

    public IRequestEncoder getHttpRequestEncoder() {
        return null;
    }

    public String BtoA(byte data[]) {
        return null;
    }

    public byte[] AtoB(String data) {
        return new byte[0];
    }

    public String getEncodedCert(X509Certificate cert) {
        return null;
    }

    public IPrettyPrintFormat getPrettyPrintFormat(String delimiter) {
        return null;
    }

    public IExtPrettyPrint getExtPrettyPrint(Extension e, int indent) {
        return null;
    }

    public ICertPrettyPrint getCertPrettyPrint(X509Certificate cert) {
        return null;
    }

    public ICRLPrettyPrint getCRLPrettyPrint(X509CRL crl) {
        return null;
    }

    public ICRLPrettyPrint getCRLCachePrettyPrint(ICRLIssuingPoint ip) {
        return null;
    }

    public ILdapConnInfo getLdapConnInfo(IConfigStore config) throws EBaseException, ELdapException {
        return null;
    }

    public LDAPSSLSocketFactoryExt getLdapJssSSLSocketFactory(String certNickname) {
        return null;
    }

    public LDAPSSLSocketFactoryExt getLdapJssSSLSocketFactory() {
        return null;
    }

    public ILdapAuthInfo getLdapAuthInfo() {
        return null;
    }

    public ILdapConnFactory getLdapBoundConnFactory() throws ELdapException {
        return null;
    }

    public LDAPConnection getBoundConnection(String host, int port, int version, LDAPSSLSocketFactoryExt fac, String bindDN, String bindPW) throws LDAPException {
        return null;
    }

    public ILdapConnFactory getLdapAnonConnFactory() throws ELdapException {
        return null;
    }

    public IPasswordCheck getPasswordChecker() {
        return null;
    }

    public void putPasswordCache(String tag, String pw) {
    }

    public PasswordCallback getPasswordCallback() {
        return null;
    }

    public String getServerCertNickname() {
        return null;
    }

    public void setServerCertNickname(String tokenName, String nickName) {
    }

    public void setServerCertNickname(String newName) {
    }

    public String getEEHost() {
        return null;
    }

    public String getEENonSSLHost() {
        return null;
    }

    public String getEENonSSLIP() {
        return null;
    }

    public String getEENonSSLPort() {
        return null;
    }

    public String getEESSLHost() {
        return null;
    }

    public String getEESSLIP() {
        return null;
    }

    public String getEESSLPort() {
        return null;
    }

    public String getAgentHost() {
        return null;
    }

    public String getAgentIP() {
        return null;
    }

    public String getAgentPort() {
        return null;
    }

    public String getAdminHost() {
        return null;
    }

    public String getAdminIP() {
        return null;
    }

    public String getAdminPort() {
        return null;
    }

    public boolean isSigningCert(X509Certificate cert) {
        return false;
    }

    public boolean isEncryptionCert(X509Certificate cert) {
        return false;
    }

    public X509CertInfo getDefaultX509CertInfo() {
        return null;
    }

    public IEmailFormProcessor getEmailFormProcessor() {
        return null;
    }

    public IEmailTemplate getEmailTemplate(String path) {
        return null;
    }

    public IMailNotification getMailNotification() {
        return null;
    }

    public IEmailResolverKeys getEmailResolverKeys() {
        return null;
    }

    public IEmailResolver getReqCertSANameEmailResolver() {
        return null;
    }

    public ObjectIdentifier checkOID(String attrName, String value) throws EBaseException {
        return null;
    }

    public GeneralName form_GeneralNameAsConstraints(String generalNameChoice, String value) throws EBaseException {
        return null;
    }

    public GeneralName form_GeneralName(String generalNameChoice, String value) throws EBaseException {
        return null;
    }

    public void getGeneralNameConfigDefaultParams(String name, boolean isValueConfigured, Vector params) {
    }

    public void getGeneralNamesConfigDefaultParams(String name, boolean isValueConfigured, Vector params) {
    }

    public void getGeneralNameConfigExtendedPluginInfo(String name, boolean isValueConfigured, Vector info) {
    }

    public void getGeneralNamesConfigExtendedPluginInfo(String name, boolean isValueConfigured, Vector info) {
    }

    public IGeneralNamesConfig createGeneralNamesConfig(String name, IConfigStore config, boolean isValueConfigured, boolean isPolicyEnabled) throws EBaseException {
        return null;
    }

    public IGeneralNameAsConstraintsConfig createGeneralNameAsConstraintsConfig(String name, IConfigStore config, boolean isValueConfigured, boolean isPolicyEnabled) throws EBaseException {
        return null;
    }

    public IGeneralNamesAsConstraintsConfig createGeneralNamesAsConstraintsConfig(String name, IConfigStore config, boolean isValueConfigured, boolean isPolicyEnabled) throws EBaseException {
        return null;
    }

    public void getSubjAltNameConfigDefaultParams(String name, Vector params) {
    }

    public void getSubjAltNameConfigExtendedPluginInfo(String name, Vector params) {
    }

    public ISubjAltNameConfig createSubjAltNameConfig(String name, IConfigStore config, boolean isValueConfigured) throws EBaseException {
        return null;
    }

    public IHttpConnection getHttpConnection(IRemoteAuthority authority, ISocketFactory factory) {
        return null;
    }

    public IHttpConnection getHttpConnection(IRemoteAuthority authority, ISocketFactory factory, int timeout) {
        return null;
    }

    public IResender getResender(IAuthority authority, String nickname, IRemoteAuthority remote, int interval) {
        return null;
    }

    public ICommandQueue getCommandQueue() {
        return null;
    }

    public void disableRequests() {
    }

    public void terminateRequests() {
    }

    public boolean areRequestsDisabled() {
        return false;
    }

    public IConfigStore createFileConfigStore(String path) throws EBaseException {
        return null;
    }

    public IArgBlock createArgBlock() {
        return null;
    }

    public IArgBlock createArgBlock(String realm, Hashtable httpReq) {
        return null;
    }

    public IArgBlock createArgBlock(Hashtable httpReq) {
        return null;
    }

    public boolean isRevoked(X509Certificate[] certificates) {
        return false;
    }

    public void setListOfVerifiedCerts(int size, long interval, long unknownStateInterval) {
    }

    public void forceShutdown() {
    }

    public IPasswordStore getPasswordStore() {
        return null;
    }

    public ISecurityDomainSessionTable getSecurityDomainSessionTable() {
        return null;
    }

    public void setConfigSDSessionId(String id) {
    }

    public String getConfigSDSessionId() {
        return null;
    }
}
