# Python
%{!?python_sitelib: %global python_sitelib %(%{__python} -c "from
distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%{!?python_sitearch: %global python_sitearch %(%{__python} -c "from
distutils.sysconfig import get_python_lib; print(get_python_lib(1))")}

Name:             pki-core
Version:          9.0.3
Release:          43%{?dist}
Summary:          Certificate System - PKI Core Components
URL:              http://pki.fedoraproject.org/
License:          GPLv2
Group:            System Environment/Daemons

BuildRoot:        %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

# jss requires versioning to meet both build and runtime requirements
# tomcatjss requires versioning since version 2.0.0 requires tomcat6
# pki-common-theme requires versioning to meet runtime requirements
# pki-ca-theme requires versioning to meet runtime requirements
%if 0%{?fedora} >= 14
BuildRequires:    apache-commons-io
BuildRequires:    apache-commons-lang
%endif
%if 0%{?rhel} || 0%{?fedora} < 14
BuildRequires:    jakarta-commons-io
BuildRequires:    jakarta-commons-lang
%endif
BuildRequires:    cmake >= 2.8.9-1
BuildRequires:    java-1.7.0-openjdk-devel
BuildRequires:    jpackage-utils
BuildRequires:    jss >= 4.2.6-35
BuildRequires:    ldapjdk
BuildRequires:    nspr-devel
BuildRequires:    nss-devel
BuildRequires:    openldap-devel
BuildRequires:    osutil
BuildRequires:    pkgconfig
BuildRequires:    policycoreutils
BuildRequires:    selinux-policy-devel >= 3.7.19-260
BuildRequires:    tomcatjss >= 2.1.0-4
BuildRequires:    velocity
BuildRequires:    xalan-j2
BuildRequires:    xerces-j2

Source0:          http://pki.fedoraproject.org/pki/sources/%{name}/%{name}-%{version}.tar.gz

Patch0:           %{name}-%{version}-r1846.patch
Patch1:           %{name}-%{version}-r1860.patch
Patch2:           %{name}-%{version}-r1862.patch
Patch3:           %{name}-%{version}-r1864.patch
Patch4:           %{name}-%{version}-r1875.patch
Patch5:           %{name}-%{version}-r1879.patch
Patch6:           %{name}-%{version}-r1886.patch
Patch7:           %{name}-%{version}-r1908.patch
Patch8:           %{name}-%{version}-r2074.patch
Patch9:           %{name}-%{version}-r2097.patch
Patch10:          %{name}-%{version}-r2103.patch
Patch11:          %{name}-%{version}-r2104.patch
Patch12:          %{name}-%{version}-r2106.patch
Patch13:          %{name}-%{version}-r2112.patch
Patch14:          %{name}-%{version}-r2118.patch
Patch15:          %{name}-%{version}-r2125.patch
Patch16:          %{name}-%{version}-r2126.patch
Patch17:          %{name}-%{version}-r2128.patch
Patch18:          %{name}-%{version}-r2149.patch
Patch19:          %{name}-%{version}-r2151.patch
Patch20:          %{name}-%{version}-r2153.patch
Patch21:          %{name}-%{version}-r2161.patch
Patch22:          %{name}-%{version}-r2163.patch
Patch23:          %{name}-%{version}-r2182.patch
Patch24:          %{name}-%{version}-r2249.patch
Patch25:          %{name}-%{version}-bz771790.patch
Patch26:          %{name}-%{version}-bz745677.patch
Patch27:          %{name}-%{version}-bz769388.patch
Patch28:          %{name}-%{version}-bz802396.patch
#Patch29:          %{name}-%{version}-bz819111.patch
Patch30:          %{name}-%{version}-bz844459.patch
Patch31:          %{name}-%{version}-bz841663.patch
Patch32:          %{name}-%{version}-bz858864.patch
Patch33:          %{name}-%{version}-bz885790.patch
Patch34:          %{name}-%{version}-bz891985.patch
Patch35:          %{name}-%{version}-bz902474.patch
Patch36:          %{name}-%{version}-bz895702.patch
Patch37:          %{name}-%{version}-bz999055.patch
Patch38:          %{name}-%{version}-bz1051382.patch
Patch39:          %{name}-%{version}-bz1083170.patch
Patch40:          %{name}-%{version}-bz1096142.patch
Patch41:          %{name}-%{version}-bz1061442.patch
Patch42:          %{name}-%{version}-bz1055080.patch
Patch43:          %{name}-%{version}-bz1024462.patch
Patch44:          %{name}-%{version}-bz1109181.patch
Patch45:          %{name}-%{version}-bz1123811.patch
Patch46:          %{name}-%{version}-bz1144188.patch
Patch47:          %{name}-%{version}-bz1144608.patch
Patch48:          %{name}-%{version}-bz1171848.patch
Patch49:          %{name}-%{version}-bz21220423.patch
Patch50:          %{name}-%{version}-bz1225589.patch


%if 0%{?rhel}
ExcludeArch:      ppc ppc64 s390 s390x
%endif

%global saveFileContext() \
if [ -s /etc/selinux/config ]; then \
     . %{_sysconfdir}/selinux/config; \
     FILE_CONTEXT=%{_sysconfdir}/selinux/%1/contexts/files/file_contexts; \
     if [ "${SELINUXTYPE}" == %1 -a -f ${FILE_CONTEXT} ]; then \
          cp -f ${FILE_CONTEXT} ${FILE_CONTEXT}.%{name}; \
     fi \
fi;

%global relabel() \
. %{_sysconfdir}/selinux/config; \
FILE_CONTEXT=%{_sysconfdir}/selinux/%1/contexts/files/file_contexts; \
selinuxenabled; \
if [ $? == 0  -a "${SELINUXTYPE}" == %1 -a -f ${FILE_CONTEXT}.%{name} ]; then \
     fixfiles -C ${FILE_CONTEXT}.%{name} restore > /dev/null 2>&1; \
     rm -f ${FILE_CONTEXT}.%name; \
fi;

%global overview                                                       \
==================================                                     \
||  ABOUT "CERTIFICATE SYSTEM"  ||                                     \
==================================                                     \
                                                                       \
Certificate System (CS) is an enterprise software system designed      \
to manage enterprise Public Key Infrastructure (PKI) deployments.      \
                                                                       \
PKI Core contains fundamental packages required by Certificate System, \
and consists of the following components:                              \
                                                                       \
  * pki-setup                                                          \
  * pki-symkey                                                         \
  * pki-native-tools                                                   \
  * pki-util                                                           \
  * pki-util-javadoc                                                   \
  * pki-java-tools                                                     \
  * pki-java-tools-javadoc                                             \
  * pki-common                                                         \
  * pki-common-javadoc                                                 \
  * pki-selinux                                                        \
  * pki-ca                                                             \
  * pki-silent                                                         \
                                                                       \
which comprise the following PKI subsystems:                           \
                                                                       \
  * Certificate Authority (CA)                                         \
                                                                       \
For deployment purposes, Certificate System requires ONE AND ONLY ONE  \
of the following "Mutually-Exclusive" PKI Theme packages:              \
                                                                       \
  * ipa-pki-theme    (IPA deployments)                                 \
  * dogtag-pki-theme (Dogtag Certificate System deployments)           \
  * redhat-pki-theme (Red Hat Certificate System deployments)          \
                                                                       \
%{nil}

%description %{overview}


%package -n       pki-setup
Summary:          Certificate System - PKI Instance Creation & Removal Scripts
Group:            System Environment/Base

BuildArch:        noarch

Requires:         perl-Crypt-SSLeay
Requires:         policycoreutils
Requires:         openldap-clients

%description -n   pki-setup
PKI setup scripts are used to create and remove instances from PKI deployments.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-symkey
Summary:          Symmetric Key JNI Package
Group:            System Environment/Libraries

Requires:         java-1.7.0-openjdk
Requires:         jpackage-utils
Requires:         jss >= 4.2.6-35
Requires:         nss

Provides:         symkey = %{version}-%{release}

Obsoletes:        symkey < %{version}-%{release}

%description -n   pki-symkey
The Symmetric Key Java Native Interface (JNI) package supplies various native
symmetric key operations to Java programs.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-native-tools
Summary:          Certificate System - Native Tools
Group:            System Environment/Base

Requires:         openldap-clients
Requires:         nss
Requires:         nss-tools

%description -n   pki-native-tools
These platform-dependent PKI executables are used to help make
Certificate System into a more complete and robust PKI solution.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-util
Summary:          Certificate System - PKI Utility Framework
Group:            System Environment/Base

BuildArch:        noarch

%if 0%{?fedora} >= 14
Requires:         apache-commons-lang
%endif
%if 0%{?rhel} || 0%{?fedora} < 14
Requires:         jakarta-commons-lang
%endif
Requires:         java-1.7.0-openjdk
Requires:         jpackage-utils
Requires:         jss >= 4.2.6-35
Requires:         ldapjdk

%description -n   pki-util
The PKI Utility Framework is required by the following four PKI subsystems:

    the Certificate Authority (CA),
    the Data Recovery Manager (DRM),
    the Online Certificate Status Protocol (OCSP) Manager, and
    the Token Key Service (TKS).

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-util-javadoc
Summary:          Certificate System - PKI Utility Framework Javadocs
Group:            Documentation

BuildArch:        noarch

Requires:         pki-util = %{version}-%{release}

%description -n   pki-util-javadoc
This documentation pertains exclusively to version %{version} of
the PKI Utility Framework.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-java-tools
Summary:          Certificate System - PKI Java-Based Tools
Group:            System Environment/Base

BuildArch:        noarch

Requires:         java-1.7.0-openjdk
Requires:         pki-native-tools = %{version}-%{release}
Requires:         pki-util = %{version}-%{release}

%description -n   pki-java-tools
These platform-independent PKI executables are used to help make
Certificate System into a more complete and robust PKI solution.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-java-tools-javadoc
Summary:          Certificate System - PKI Java-Based Tools Javadocs
Group:            Documentation

BuildArch:        noarch

Requires:         pki-java-tools = %{version}-%{release}

%description -n   pki-java-tools-javadoc
This documentation pertains exclusively to version %{version} of
the PKI Java-Based Tools.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-common
Summary:          Certificate System - PKI Common Framework
Group:            System Environment/Base

BuildArch:        noarch

%if 0%{?fedora} >= 14
Requires:         apache-commons-lang
Requires:         apache-commons-logging
%endif
%if 0%{?rhel} || 0%{?fedora} < 14
Requires:         jakarta-commons-lang
Requires:         jakarta-commons-logging
%endif
Requires:         java-1.7.0-openjdk
Requires:         jss >= 4.2.6-35
Requires:         osutil
Requires:         pki-common-theme >= 9.0.0
Requires:         pki-java-tools = %{version}-%{release}
Requires:         pki-setup = %{version}-%{release}
Requires:         pki-symkey = %{version}-%{release}
Requires:         python-ldap
Requires:         tomcatjss >= 2.1.0-4
Requires:         %{_javadir}/ldapjdk.jar
Requires:         %{_javadir}/velocity.jar
Requires:         %{_javadir}/xalan-j2.jar
Requires:         %{_javadir}/xalan-j2-serializer.jar
Requires:         %{_javadir}/xerces-j2.jar
Requires:         %{_javadir}/xml-commons-apis.jar
Requires:         %{_javadir}/xml-commons-resolver.jar
Requires:         velocity

%description -n   pki-common
The PKI Common Framework is required by the following four PKI subsystems:

    the Certificate Authority (CA),
    the Data Recovery Manager (DRM),
    the Online Certificate Status Protocol (OCSP) Manager, and
    the Token Key Service (TKS).

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-common-javadoc
Summary:          Certificate System - PKI Common Framework Javadocs
Group:            Documentation

BuildArch:        noarch

Requires:         pki-common = %{version}-%{release}

%description -n   pki-common-javadoc
This documentation pertains exclusively to version %{version} of
the PKI Common Framework.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-selinux
Summary:          Certificate System - PKI Selinux Policies
Group:            System Environment/Base

BuildArch:        noarch

Requires:         policycoreutils
Requires:         selinux-policy-targeted >= 3.7.19-260

%description -n   pki-selinux
Selinux policies for the PKI components.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-ca
Summary:          Certificate System - Certificate Authority
Group:            System Environment/Daemons

BuildArch:        noarch

Requires:         java-1.7.0-openjdk
Requires:         pki-ca-theme >= 9.0.0
Requires:         pki-common = %{version}-%{release}
Requires:         pki-selinux = %{version}-%{release}
Requires(post):   chkconfig
Requires(preun):  chkconfig
Requires(preun):  initscripts
Requires(postun): initscripts
Requires(post):   policycoreutils

%if 0%{?fedora} >= 15
# Details:
#
#     * https://fedoraproject.org/wiki/Features/var-run-tmpfs
#     * https://fedoraproject.org/wiki/Tmpfiles.d_packaging_draft
#
Requires:         initscripts
%endif

%description -n   pki-ca
The Certificate Authority (CA) is a required PKI subsystem which issues,
renews, revokes, and publishes certificates as well as compiling and
publishing Certificate Revocation Lists (CRLs).

The Certificate Authority can be configured as a self-signing Certificate
Authority, where it is the root CA, or it can act as a subordinate CA,
where it obtains its own signing certificate from a public CA.

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%package -n       pki-silent
Summary:          Certificate System - Silent Installer
Group:            System Environment/Base

BuildArch:        noarch

%if 0%{?fedora} >= 14
Requires:         apache-commons-io
%endif
%if 0%{?rhel} || 0%{?fedora} < 14
Requires:         jakarta-commons-io
%endif
Requires:         java-1.7.0-openjdk
Requires:         pki-common = %{version}-%{release}

%description -n   pki-silent
The PKI Silent Installer may be used to "automatically" configure
the following PKI subsystems in a non-graphical (batch) fashion
including:

    the Certificate Authority (CA),
    the Data Recovery Manager (DRM),
    the Online Certificate Status Protocol (OCSP) Manager,
    the Registration Authority (RA),
    the Token Key Service (TKS), and/or
    the Token Processing System (TPS).

This package is a part of the PKI Core used by the Certificate System.

%{overview}


%prep


%setup -q


%patch0 -b .p0
%patch1 -b .p1
%patch2 -b .p2
%patch3 -b .p3
%patch4 -b .p4
%patch5 -b .p5
%patch6 -b .p6
%patch7 -b .p7
%patch8 -b .p8
%patch9 -b .p9
%patch10 -b .p10
%patch11 -b .p11
%patch12 -b .p12
%patch13 -b .p13
%patch14 -b .p14
%patch15 -b .p15
%patch16 -b .p16
%patch17 -b .p17
%patch18 -b .p18
%patch19 -b .p19
%patch20 -b .p20
%patch21 -b .p21
%patch22 -b .p22
%patch23 -b .p23
%patch24 -b .p24
%patch25 -p2 -b .p25
%patch26 -p2 -b .p26
%patch27 -p2 -b .p27
%patch28 -p2 -b .p28
#%patch29 -p1 -b .p29
%patch30 -p1 -b .p30
%patch31 -p1 -b .p31
%patch32 -p1 -b .p32
%patch33 -p1 -b .p33
%patch34 -p1 -b .p34
%patch35 -p1 -b .p35
%patch36 -p1 -b .p36
%patch37 -p1 -b .p37
%patch38 -p1 -b .p38
%patch39 -p1 -b .p39
%patch40 -p1 -b .p40
%patch41 -p1 -b .p41
%patch42 -p1 -b .p42
%patch43 -p1 -b .p43
%patch44 -p1 -b .p44
%patch45 -p1 -b .p45
%patch46 -p1 -b .p46
%patch47 -p1 -b .p47
%patch48 -p1 -b .p48
%patch49 -p1 -b .p49
%patch50 -p1 -b .p50

%clean
%{__rm} -rf %{buildroot}


%build
%{__mkdir_p} build
cd build
%cmake -DVAR_INSTALL_DIR:PATH=/var -DBUILD_PKI_CORE:BOOL=ON ..
%{__make} VERBOSE=1 %{?_smp_mflags}


%install
%{__rm} -rf %{buildroot}
cd build
%{__make} install DESTDIR=%{buildroot} INSTALL="install -p"

cd %{buildroot}%{_jnidir}
mv symkey.jar %{buildroot}%{_libdir}/symkey/symkey-%{version}.jar
%{__ln_s} %{_libdir}/symkey/symkey.jar symkey.jar

cd %{buildroot}%{_libdir}/symkey
%{__ln_s} symkey-%{version}.jar symkey.jar

cd %{buildroot}%{_javadir}/pki
mv pki-ca.jar pki-ca-%{version}.jar
%{__ln_s} pki-ca-%{version}.jar pki-ca.jar
mv pki-certsrv.jar pki-certsrv-%{version}.jar
%{__ln_s} pki-certsrv-%{version}.jar pki-certsrv.jar
mv pki-cms.jar pki-cms-%{version}.jar
%{__ln_s} pki-cms-%{version}.jar pki-cms.jar
mv pki-cmsbundle.jar pki-cmsbundle-%{version}.jar
%{__ln_s} pki-cmsbundle-%{version}.jar pki-cmsbundle.jar
mv pki-cmscore.jar pki-cmscore-%{version}.jar
%{__ln_s} pki-cmscore-%{version}.jar pki-cmscore.jar
mv pki-cmsutil.jar pki-cmsutil-%{version}.jar
%{__ln_s} pki-cmsutil-%{version}.jar pki-cmsutil.jar
mv pki-nsutil.jar pki-nsutil-%{version}.jar
%{__ln_s} pki-nsutil-%{version}.jar pki-nsutil.jar
mv pki-silent.jar pki-silent-%{version}.jar
%{__ln_s} pki-silent-%{version}.jar pki-silent.jar
mv pki-tools.jar pki-tools-%{version}.jar
%{__ln_s} pki-tools-%{version}.jar pki-tools.jar

%if 0%{?fedora} >= 15
# Details:
#
#     * https://fedoraproject.org/wiki/Features/var-run-tmpfs
#     * https://fedoraproject.org/wiki/Tmpfiles.d_packaging_draft
#
%{__mkdir_p} %{buildroot}%{_sysconfdir}/tmpfiles.d
# generate 'pki-ca.conf' under the 'tmpfiles.d' directory
echo "D /var/lock/pki 0755 root root -"    >  %{buildroot}%{_sysconfdir}/tmpfiles.d/pki-ca.conf
echo "D /var/lock/pki/ca 0755 root root -" >> %{buildroot}%{_sysconfdir}/tmpfiles.d/pki-ca.conf
echo "D /var/run/pki 0755 root root -"     >> %{buildroot}%{_sysconfdir}/tmpfiles.d/pki-ca.conf
echo "D /var/run/pki/ca 0755 root root -"  >> %{buildroot}%{_sysconfdir}/tmpfiles.d/pki-ca.conf
%endif

# tomcat6 has changed how TOMCAT_LOG is used.
# Need to adjust accordingly
# This macro will be executed in the postinstall scripts
%define fix_tomcat_log() (                                                   \
if [ -d /etc/sysconfig/pki/%i ]; then                                        \
  for F in `find /etc/sysconfig/pki/%1 -type f`; do                          \
    instance=`basename $F`                                                   \
    if [ -f /etc/sysconfig/$instance ]; then                                 \
        sed -i -e 's/catalina.out/tomcat-initd.log/' /etc/sysconfig/$instance \
    fi                                                                       \
  done                                                                       \
fi                                                                           \
)

%pre -n pki-selinux
%saveFileContext targeted


%post -n pki-selinux
semodule -s targeted -i %{_datadir}/selinux/modules/pki.pp
%relabel targeted


%preun -n pki-selinux
if [ $1 = 0 ]; then
     %saveFileContext targeted
fi


%postun -n pki-selinux
if [ $1 = 0 ]; then
     semodule -s targeted -r pki
     %relabel targeted
fi


%post -n pki-ca
# This adds the proper /etc/rc*.d links for the script
/sbin/chkconfig --add pki-cad || :
/sbin/restorecon -Fr %{_localstatedir}/run/pki/ca >/dev/null 2>&1 || :
%fix_tomcat_log ca

%preun -n pki-ca
if [ $1 = 0 ] ; then
    /sbin/service pki-cad stop >/dev/null 2>&1
    /sbin/chkconfig --del pki-cad || :
fi


%postun -n pki-ca
if [ "$1" -ge "1" ] ; then
    /sbin/service pki-cad condrestart >/dev/null 2>&1 || :
fi

 

%files -n pki-setup
%defattr(-,root,root,-)
%doc base/setup/LICENSE
%{_bindir}/pkicreate
%{_bindir}/pkiremove
%{_bindir}/pki-setup-proxy
%dir %{_datadir}/pki
%dir %{_datadir}/pki/scripts
%{_datadir}/pki/scripts/pkicommon.pm
%if 0%{?rhel} || 0%{?fedora} < 15
%dir %{_localstatedir}/lock/pki
%dir %{_localstatedir}/run/pki
%endif


%files -n pki-symkey
%defattr(-,root,root,-)
%doc base/symkey/LICENSE
%{_jnidir}/symkey.jar
%{_libdir}/symkey/


%files -n pki-native-tools
%defattr(-,root,root,-)
%doc base/native-tools/LICENSE base/native-tools/doc/README
%{_bindir}/bulkissuance
%{_bindir}/p7tool
%{_bindir}/revoker
%{_bindir}/setpin
%{_bindir}/sslget
%{_bindir}/tkstool
%dir %{_datadir}/pki
%{_datadir}/pki/native-tools/


%files -n pki-util
%defattr(-,root,root,-)
%doc base/util/LICENSE
%dir %{_javadir}/pki
%{_javadir}/pki/pki-cmsutil-%{version}.jar
%{_javadir}/pki/pki-cmsutil.jar
%{_javadir}/pki/pki-nsutil-%{version}.jar
%{_javadir}/pki/pki-nsutil.jar

%files -n pki-util-javadoc
%defattr(-,root,root,-)
%{_javadocdir}/pki-util-%{version}/


%files -n pki-java-tools
%defattr(-,root,root,-)
%doc base/java-tools/LICENSE
%{_bindir}/AtoB
%{_bindir}/AuditVerify
%{_bindir}/BtoA
%{_bindir}/CMCEnroll
%{_bindir}/CMCRequest
%{_bindir}/CMCResponse
%{_bindir}/CMCRevoke
%{_bindir}/CRMFPopClient
%{_bindir}/ExtJoiner
%{_bindir}/GenExtKeyUsage
%{_bindir}/GenIssuerAltNameExt
%{_bindir}/GenSubjectAltNameExt
%{_bindir}/HttpClient
%{_bindir}/OCSPClient
%{_bindir}/PKCS10Client
%{_bindir}/PKCS12Export
%{_bindir}/PrettyPrintCert
%{_bindir}/PrettyPrintCrl
%{_bindir}/TokenInfo
%{_javadir}/pki/pki-tools-%{version}.jar
%{_javadir}/pki/pki-tools.jar

%files -n pki-java-tools-javadoc
%defattr(-,root,root,-)
%{_javadocdir}/pki-java-tools-%{version}/


%files -n pki-common
%defattr(-,root,root,-)
%doc base/common/LICENSE
%{_javadir}/pki/pki-certsrv-%{version}.jar
%{_javadir}/pki/pki-certsrv.jar
%{_javadir}/pki/pki-cms-%{version}.jar
%{_javadir}/pki/pki-cms.jar
%{_javadir}/pki/pki-cmsbundle-%{version}.jar
%{_javadir}/pki/pki-cmsbundle.jar
%{_javadir}/pki/pki-cmscore-%{version}.jar
%{_javadir}/pki/pki-cmscore.jar
%{_datadir}/pki/scripts/functions
%{_datadir}/pki/scripts/pki_apache_initscript
%{_datadir}/pki/scripts/restore-subsystem-user.*
%{_datadir}/pki/setup/
%{python_sitelib}/pki/
%{_sbindir}/

%files -n pki-common-javadoc
%defattr(-,root,root,-)
%{_javadocdir}/pki-common-%{version}/


%files -n pki-selinux
%defattr(-,root,root,-)
%doc base/selinux/LICENSE
%{_datadir}/selinux/modules/pki.pp


%files -n pki-ca
%defattr(-,root,root,-)
%doc base/ca/LICENSE
%{_initrddir}/pki-cad
%{_javadir}/pki/pki-ca-%{version}.jar
%{_javadir}/pki/pki-ca.jar
%dir %{_datadir}/pki/ca
%{_datadir}/pki/ca/conf/
%{_datadir}/pki/ca/emails/
%dir %{_datadir}/pki/ca/profiles
%{_datadir}/pki/ca/profiles/ca/
%{_datadir}/pki/ca/webapps/
%{_datadir}/pki/ca/setup/
%dir %{_localstatedir}/lock/pki/ca
%dir %{_localstatedir}/run/pki/ca
%if 0%{?fedora} >= 15
# Details:
#
#     * https://fedoraproject.org/wiki/Features/var-run-tmpfs
#     * https://fedoraproject.org/wiki/Tmpfiles.d_packaging_draft
#
%config(noreplace) %{_sysconfdir}/tmpfiles.d/pki-ca.conf
%endif


%files -n pki-silent
%defattr(-,root,root,-)
%doc base/silent/LICENSE
%{_bindir}/pkisilent
%{_javadir}/pki/pki-silent-%{version}.jar
%{_javadir}/pki/pki-silent.jar
%{_datadir}/pki/silent/


%changelog
* Wed May 27 2015 Endi S. Dewata <edewata@redhat.com> 9.0.3-43
- Resolves #1225589 - unable to create rhel 7.1 replica from rhel 6 replica CA because subsystem user does not exist
	
* Mon May 18 2015 Jack Magne <jmagne@redhat.com> 9.0.3-42
- Resolves #1221900 - pki-core: cross-site scripting flaw in the dogtag administration page (port 9180, port 9444) [rhel-6.7]

* Mon Apr 20 2015 Endi S. Dewata <edewata@redhat.com> 9.0.3-41
- Resolves #1212557 - ipa-server-install fails when configuring CA

* Tue Feb 11 2015 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-40
- Resolves #1171848 - IPA - port 9443 (pki-core) is vulnerable to SSLv3 POODLE
  (based upon upstream changes provided by cfu and alee)

* Wed Feb 04 2015 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-39
- Resolves #1144608 - pki-core failed to build with cmake-2.8.12.2-4.el6
- Resolves #1037248 - pki-core FTBFS if "-Werror=format-security" flag is used
- Resolves #1243 - Outdated selinux-policy dependency in Dogtag 9

* Wed Sep 24 2014 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-38
- Resolves #1144188 - TPS tests: RPM rebuild failure due to wildcard imports

* Wed Jul 30 2014 Ade Lee <alee@redhat.com> 9.0.3-37
- Resolves #1123811 - IPA PKI clone certificate renewal produces AVC

* Thu Jun 26 2014 Ade Lee <alee@redhat.com> 9.0.3-36
- Resolves #1109181 - certmonger cannot start tracking PKI certificates due
  to AVC

* Fri Jun 20 2014 Ade Lee <alee@redhat.com> 9.0.3-35
- Resolves #1024462 - IPA admin cert is created with SHA1 signing algorithm,
  should be SHA256

* Fri Jun 20 2014 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-34
- Resolves #1096142 - Added 'jakarta-commons-io' build and runtime dependencies

* Tue May 20 2014 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-33
- Resolves #1051382 - IPA Replica installation fails when using an external CA
- Test patch to filter out invalid XML and provide additional debugging
  information
- Resolves #1083170 - Prevent LDAP Attributes from being affected by Locale
- Resolves #1096142 - IPA replica setup fails during CA setup with
                      "unable to parse xml"
- Resolves #1061442 - RFE - ipa-server should keep backup of CS.cfg
- Resolves #1055080 - Giant /var/log/pki-ca/debug

* Thu Aug 29 2013 Ade Lee <alee@redhat.com> 9.0.3-32
- Resolves #999055 - AVC denials during ipa server and replica installs
- Resolves #998715 - Package Sanity Test Failures

* Fri Aug 9 2013 Ade Lee <alee@redhat.com> 9.0.3-31
- Resolves #887305 - /var/run/pki/ca has wrong selinux context
- Resolves #895702 - pki-cad restart avc denial

* Tue Jan 22 2013 Ade Lee <alee@redhat.com> 9.0.3-30
- Resolves #902474 - upgrading IPA from 2.2 to 3.0 sees certmonger errors

* Mon Jan 7 2013 Ade Lee <alee@redhat.com> 9.0.3-29
- Resolves #891985 - Increase FreeIPA root CA validity

* Fri Dec 14 2012 Andrew Wnuk <awnuk@redhat.com> 9.0.3-28
- Resolves #885790 - Multiple cross-site scripting flaws
  by displaying CRL or processing profile

* Fri Oct 19 2012 Ade Lee <alee@redhat.com> 9.0.3-27
- Resolves #867640 - ipa-replica-install Configuration of CA failed
  by REVERTING #819111 - Non-existent container breaks replication

* Fri Sep 28 2012 Ade Lee <alee@redhat.com> 9.0.3-26
- Resolves #844459 - Increase audit cert renewal range to 2 years (mharmsen)
- Resolves #841663 - serial number incorrectly cast from BigInt to integer in
  installation wizard (mharmsen)
- Resolves #858864 - create/ identify a mechanism for clients to determine that
  the pki subsystem is up (alee)

* Tue May 8 2012 Ade Lee <alee@redhat.com> 9.0.3-25
- Resolves #819111 - Non-existent container breaks replication

* Fri Mar 16 2012 Ade Lee <alee@redhat.com> 9.0.3-24
- BZ 802396 - Change location of TOMCAT_LOG to match tomcat6 changes

* Mon Mar  5 2012 Ade Lee <alee@redhat.com> 9.0.3-23
- Resolves #769388 - pki-silent does not properly escape command-line arguments
  (fixed in Git repo)

* Mon Mar  5 2012 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-22
- Resolves #745677 - Firefox Launcher on Panel being modified for all users.
  (fixed in Git repo)

* Tue Jan 17 2012 Ade Lee <alee@redhat.com> 9.0.3-21
- Resolves #771790 - sslget does not work after FEDORA-2011-17400 update,
  breaking FreeIPA install (fixed in Git repo)

* Fri Aug 26 2011 Andrew Wnuk <awnuk@redhat.com> 9.0.3-20
- Resolves #737179 - Need script to upgrade proxy configuration, r2249

* Fri Aug 26 2011 Andrew Wnuk <awnuk@redhat.com> 9.0.3-19
- Resolves #730801 - Coverity issues in native-tools area, r2182

* Tue Aug 23 2011 Andrew Wnuk <awnuk@redhat.com> 9.0.3-18
- Resolves #730801 - Coverity issues in native-tools area, r2163

* Tue Aug 23 2011 Ade Lee <alee@redhat.com> 9.0.3-17
- Resolves #712931 - CS requires too many ports to be open in the FW, r2161

* Mon Aug 22 2011 Andrew Wnuk <awnuk@redhat.com> 9.0.3-16
- Resolves #717643 - Fopen without NULL check and other Coverity issues

* Mon Aug 22 2011 Andrew Wnuk <awnuk@redhat.com> 9.0.3-15
- Resolves #717643 - Fopen without NULL check and other Coverity issues

* Mon Aug 15 2011 Ade Lee <alee@redhat.com> 9.0.3-14
- Resolves #700522 - pki tomcat6 instances currently running unconfined, 
  allow server to come up when selinux disabled, r2149

* Thu Aug 4 2011 Ade Lee <alee@redhat.com> 9.0.3-13
- Resolves #698796: Race conditions during IPA installation, r2103 (alee)
- Resolves #708075 - Clone installation does not work over NAT, r2104 (alee)
- Resolves #726785 - If replication fails while setting up a clone it 
  will wait forever, r2106 (alee)
- Resolves #691076 - pkiremove removes the registry entry for all instances
  on a machine, r2112 (mharmsen)
- Resolves #693835 - /var/log/tomcat6/catalina.out owned by pkiuser, r2118
  (mharmsen)
- Resolves #729126 - Increase default validity from 6mo to 2yrs in IPA
  profile, r2125 (awnuk)
- Resolves #728651 - CS8 64 bit pkicreate script uses wrong library name
  for, r2126 (mharmsen)
- Resolves #700522 - pki tomcat6 instances currently running unconfined,
  r2128 (alee)

* Wed Aug 3 2011 Ade Lee <alee@redhat.com> 9.0.3-12
- Resolves #689909 - Dogtag installation under IPA takes too much
  time - remove the inefficient sleeps, r2097

* Fri Jul 22 2011 Andrew Wnuk <awnuk@redhat.com> 9.0.3-11
- Resolves #722634 - Add client usage flag to caIPAserviceCert, r2074

* Tue Mar 22 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-10
- Resolves #688251 - Dogtag installation under IPA takes too much
  time - SELinux policy compilation, r1908

* Fri Mar 9 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-9
- Resolves: bug 645097 
- update to the pki-core-9.0.3-r1886.patch file

* Wed Mar 9 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-8
- Resolves 645097 
- Resolves #683172 - pkisilent needs to provide option to set
  nsDS5ReplicaTransportInfo to TLS in replication agreements
  when creating a clone, r1886

* Fri Mar 4 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-7
- Resolves 645097 

* Fri Mar 4 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-6
- Resolves #682021 - pkisilent needs xml-commons-apis.jar in it's classpath

* Wed Mar 2 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-5
- Resolves 645097 

* Wed Mar 2 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-4
- Resolves #681367 - xml-commons-apis.jar dependency, r1875

* Mon Feb 21 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-3
- Resolves #676873 - Rebase pki-core again to pick the latest features and fixes
-     Resolves #676048 - Installation within IPA hangs, r1846
-     Resolves #679173 - uninitialized variable warnings from Perl, r1860
-     Resolves #679174 - netstat loop fixes needed, r1862
-     Resolves #679580 - Velocity fails to load all dependent classes, r1864

* Wed Feb 9 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-2
- 'pki-common'
-     Bugzilla Bug #676051 - IPA installation failing - Fails to create CA
      instance
-     Bugzilla Bug #676182 - IPA installation failing - Fails to create CA
      instance

* Fri Feb 4 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.3-1
- 'pki-common'
-     Bugzilla Bug #674894 - ipactl restart : an annoy output line
-     Bugzilla Bug #675179 - ipactl restart : an annoy output line

* Thu Feb 3 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.2-1
- Bugzilla Bug #673233 - Rebase pki-core to pick the latest features and fixes
- 'pki-setup'
-     Bugzilla Bug #673638 - Installation within IPA hangs
- 'pki-symkey'
- 'pki-native-tools'
- 'pki-util'
- 'pki-java-tools'
-     Bugzilla Bug #673614 - CC: Review of cryptographic algorithms provided
      by 'netscape.security.provider' package
- 'pki-common'
-     Bugzilla Bug #672291 - CA is not publishing certificates issued using
      "Manual User Dual-Use Certificate Enrollment"
-     Bugzilla Bug #670337 - CA Clone configuration throws TCP connection
      error.
-     Bugzilla Bug #504056 - Completed SCEP requests are assigned to the
      "begin" state instead of "complete".
-     Bugzilla Bug #504055 - SCEP requests are not properly populated
-     Bugzilla Bug #564207 - Searches for completed requests in the agent
      interface returns zero entries
-     Bugzilla Bug #672291 - CA is not publishing certificates issued using
      "Manual User Dual-Use Certificate Enrollment" -
-     Bugzilla Bug #673614 - CC: Review of cryptographic algorithms provided
      by 'netscape.security.provider' package
-     Bugzilla Bug #672920 - CA console: adding policy to a profile throws
      'Duplicate policy' error in some cases.
-     Bugzilla Bug #673199 - init script returns control before web apps have
      started
-     Bugzilla Bug #674917 - Restore identification of Tomcat-based PKI
      subsystem instances
- 'pki-selinux'
- 'pki-ca'
-     Bugzilla Bug #504013 - sscep request is rejected due to authentication
      error if submitted through one time pin router certificate enrollment.
-     Bugzilla Bug #672111 - CC doc: certServer.usrgrp.administration missing
      information
-     Bugzilla Bug #583825 - CC: Obsolete servlets to be removed from web.xml
      as part of CC interface review
-     Bugzilla Bug #672333 - Creation of RA agent fails in IPA installation
-     Bugzilla Bug #674917 - Restore identification of Tomcat-based PKI
      subsystem instances
- 'pki-silent'
-     Bugzilla Bug #673614 - CC: Review of cryptographic algorithms provided
      by 'netscape.security.provider' package

* Wed Feb 2 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.1-3
- Bugzilla Bug #656661 - Please Update Spec File to use 'ghost' on files
  in /var/run and /var/lock

* Thu Jan 20 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.1-2
- 'pki-symkey'
-     Bugzilla Bug #671265 - pki-symkey jar version incorrect
- 'pki-common'
-     Bugzilla Bug #564207 - Searches for completed requests in the agent
      interface returns zero entries

* Tue Jan 18 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.1-1
- Allow 'pki-native-tools' to be installed independently of 'pki-setup'
- Removed explicit 'pki-setup' requirement from 'pki-ca'
  (since it already requires 'pki-common')
- 'pki-setup'
-     Bugzilla Bug #223343 - pkicreate: should add 'pkiuser' to nfast group
-     Bugzilla Bug #629377 - Selinux errors during pkicreate CA, KRA, OCSP
      and TKS.
-     Bugzilla Bug #555927 - rhcs80 - AgentRequestFilter servlet and port
      fowarding for agent services
-     Bugzilla Bug #632425 - Port to tomcat6
-     Bugzilla Bug #606946 - Convert Native Tools to use ldapAPI from
      OpenLDAP instead of the Mozldap
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #658926 - org.apache.commons.lang class not found on F13
-     Bugzilla Bug #661514 - CMAKE build system requires rules to make
      javadocs
-     Bugzilla Bug #665388 - jakarta-* jars have been renamed to apache-*,
      pkicreate fails Fedora 14 and above
-     Bugzilla Bug #23346 - Two conflicting ACL list definitions in source
      repository
-     Bugzilla Bug #656733 - Standardize jar install location and jar names
- 'pki-symkey'
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #644056 - CS build contains warnings
- 'pki-native-tools'
-     template change
-     Bugzilla Bug #606946 - Convert Native Tools to use ldapAPI from
      OpenLDAP instead of the Mozldap
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #644056 - CS build contains warnings
- 'pki-util'
-     Bugzilla Bug #615814 - rhcs80 - profile policyConstraintsCritical
      cannot be set to true
-     Bugzilla Bug #224945 - javadocs has missing descriptions, contains
      empty packages
-     Bugzilla Bug #621337 - Limit the received senderNonce value to 16 bytes.
-     Bugzilla Bug #621338 - Include a server randomly-generated 16 byte
      senderNonce in all signed SCEP responses.
-     Bugzilla Bug #621327 - Provide switch disabling algorithm downgrade
      attack in SCEP
-     Bugzilla Bug #621334 - Provide an option to set default hash algorithm
      for signing SCEP response messages.
-     Bugzilla Bug #635033 - At installation wizard selecting key types other
      than CA's signing cert will fail
-     Bugzilla Bug #645874 - rfe ecc - add ecc curve name support in JSS and
      CS interface
-     Bugzilla Bug #488253 - com.netscape.cmsutil.ocsp.BasicOCSPResponse
      ASN.1 encoding/decoding is broken
-     Bugzilla Bug #551410 - com.netscape.cmsutil.ocsp.TBSRequest ASN.1
      encoding/decoding is incomplete
-     Bugzilla Bug #550331 - com.netscape.cmsutil.ocsp.ResponseData ASN.1
      encoding/decoding is incomplete
-     Bugzilla Bug #623452 - rhcs80 pkiconsole profile policy editor limit
      policy extension to 5 only
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #651977 - turn off ssl2 for java servers (server.xml)
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #661514 - CMAKE build system requires rules to make
      javadocs
-     Bugzilla Bug #658188 - remove remaining references to tomcat5
-     Bugzilla Bug #656733 - Standardize jar install location and jar names
-     Bugzilla Bug #223319 - Certificate Status inconsistency between token
      db and CA
-     Bugzilla Bug #531137 - RHCS 7.1 - Running out of Java Heap Memory
      During CRL Generation
- 'pki-java-tools'
-     Bugzilla Bug #224945 - javadocs has missing descriptions, contains
      empty packages
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #659004 - CC: AuditVerify hardcoded with SHA-1
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #661514 - CMAKE build system requires rules to make
      javadocs
-     Bugzilla Bug #662156 - HttpClient is hard-coded to handle only up to
      5000 bytes
-     Bugzilla Bug #656733 - Standardize jar install location and jar names
- 'pki-common'
-     Bugzilla Bug #583822 - CC: ACL issues from CA interface CC doc review
-     Bugzilla Bug #623745 - SessionTimer with LDAPSecurityDomainSessionTable
      started before configuration completed
-     Bugzilla Bug #620925 - CC: auditor needs to be able to download audit
      logs in the java subsystems
-     Bugzilla Bug #615827 - rhcs80 - profile policies need more than 5
      policy mappings (seem hardcoded)
-     Bugzilla Bug #224945 - javadocs has missing descriptions, contains
      empty packages
-     Bugzilla Bug #548699 - subCA's admin certificate should be generated by
      itself
-     Bugzilla Bug #621322 - Provide switch disabling SCEP support in CA
-     Bugzilla Bug #563386 - rhcs80 ca crash on invalid inputs to profile
      caAgentServerCert (null cert_request)
-     Bugzilla Bug #621339 - SCEP one-time PIN can be used an unlimited
      number of times
-     Bugzilla Bug #583825 - CC: Obsolete servlets to be removed from web.xml
      as part of CC interface review
-     Bugzilla Bug #629677 - TPS: token enrollment fails.
-     Bugzilla Bug #621350 - Unauthenticated user can decrypt a one-time PIN
      in a SCEP request
-     Bugzilla Bug #503838 - rhcs71-80 external publishing ldap connection
      pools not reliable - improve connections or discovery
-     Bugzilla Bug #629769 - password decryption logs plain text password
-     Bugzilla Bug #583823 - CC: Auditing issues found as result of
      CC - interface review
-     Bugzilla Bug #632425 - Port to tomcat6
-     Bugzilla Bug #586700 - OCSP Server throws fatal error while using
      OCSP console for renewing SSL Server certificate.
-     Bugzilla Bug #621337 - Limit the received senderNonce value to 16 bytes.
-     Bugzilla Bug #621338 - Include a server randomly-generated 16 byte
      senderNonce in all signed SCEP responses.
-     Bugzilla Bug #607380 - CC: Make sure Java Console can configure all
      security relevant config items
-     Bugzilla Bug #558100 - host challenge of the Secure Channel needs to be
      generated on TKS instead of TPS.
-     Bugzilla Bug #489342 -
      com.netscape.cms.servlet.common.CMCOutputTemplate.java
      doesn't support EC
-     Bugzilla Bug #630121 - OCSP responder lacking option to delete or
      disable a CA that it serves
-     Bugzilla Bug #634663 - CA CMC response default hard-coded to SHA1
-     Bugzilla Bug #621327 - Provide switch disabling algorithm downgrade
      attack in SCEP
-     Bugzilla Bug #621334 - Provide an option to set default hash algorithm
      for signing SCEP response messages.
-     Bugzilla Bug #635033 - At installation wizard selecting key types other
      than CA's signing cert will fail
-     Bugzilla Bug #621341 - Add CA support for new SCEP key pair dedicated
      for SCEP signing and encryption.
-     Bugzilla Bug #223336 - ECC: unable to clone a ECC CA
-     Bugzilla Bug #539781 - rhcs 71 - CRLs Partitioned
      by Reason Code - onlySomeReasons ?
-     Bugzilla Bug #637330 - CC feature: Key Management - provide signature
      verification functions (JAVA subsystems)
-     Bugzilla Bug #223313 - should do random generated IV param
      for symmetric keys
-     Bugzilla Bug #555927 - rhcs80 - AgentRequestFilter servlet and port
      fowarding for agent services
-     Bugzilla Bug #630176 - Improve reliability of the LdapAnonConnFactory
-     Bugzilla Bug #524916 - ECC key constraints plug-ins should be based on
      ECC curve names (not on key sizes).
-     Bugzilla Bug #516632 - RHCS 7.1 - CS Incorrectly Issuing Multiple
      Certificates from the Same Request
-     Bugzilla Bug #648757 - expose and use updated cert verification
      function in JSS
-     Bugzilla Bug #638242 - Installation Wizard: at SizePanel, fix selection
      of signature algorithm; and for ECC curves
-     Bugzilla Bug #451874 - RFE - Java console - Certificate Wizard missing
      e.c. support
-     Bugzilla Bug #651040 - cloning shoud not include sslserver
-     Bugzilla Bug #542863 - RHCS8: Default cert audit nickname written to
      CS.cfg files imcomplete when the cert is stored on a hsm
-     Bugzilla Bug #360721 - New Feature: Profile Integrity Check . . .
-     Bugzilla Bug #651916 - kra and ocsp are using incorrect ports
      to talk to CA and complete configuration in DonePanel
-     Bugzilla Bug #642359 - CC Feature - need to verify certificate when it
      is added
-     Bugzilla Bug #653713 - CC: setting trust on a CIMC cert requires
      auditing
-     Bugzilla Bug #489385 - references to rhpki
-     Bugzilla Bug #499494 - change CA defaults to SHA2
-     Bugzilla Bug #623452 - rhcs80 pkiconsole profile policy editor limit
      policy extension to 5 only
-     Bugzilla Bug #649910 - Console: an auditor or agent can be added to
      an administrator group.
-     Bugzilla Bug #632425 - Port to tomcat6
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #651977 - turn off ssl2 for java servers (server.xml)
-     Bugzilla Bug #653576 - tomcat5 does not always run filters on servlets
      as expected
-     Bugzilla Bug #642357 - CC Feature- Self-Test plugins only check for
      validity
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #659004 - CC: AuditVerify hardcoded with SHA-1
-     Bugzilla Bug #661196 - ECC(with nethsm) subca configuration fails with
      Key Type RSA Not Matched despite using ECC key pairs for rootCA & subCA.
-     Bugzilla Bug #661889 - The Servlet TPSRevokeCert of the CA returns an
      error to TPS even if certificate in question is already revoked.
-     Bugzilla Bug #663546 - Disable the functionalities that are not exposed
      in the console
-     Bugzilla Bug #661514 - CMAKE build system requires rules to make
      javadocs
-     Bugzilla Bug #658188 - remove remaining references to tomcat5
-     Bugzilla Bug #649343 - Publishing queue should recover from CA crash.
-     Bugzilla Bug #491183 - rhcs rfe - add rfc 4523 support for pkiUser and
      pkiCA, obsolete 2252 and 2256
-     Bugzilla Bug #640710 - Current SCEP implementation does not support HSMs
-     Bugzilla Bug #656733 - Standardize jar install location and jar names
-     Bugzilla Bug #661142 - Verification should fail when
      a revoked certificate is added
-     Bugzilla Bug #642741 - CS build uses deprecated functions
-     Bugzilla Bug #670337 - CA Clone configuration throws TCP connection error
-     Bugzilla Bug #662127 - CC doc Error: SignedAuditLog expiration time
      interface is no longer available through console
- 'pki-selinux'
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #667153 - store nuxwdog passwords in kernel ring buffer -
      selinux changes
- 'pki-ca'
-     Bugzilla Bug #583822 - CC: ACL issues from CA interface CC doc review
-     Bugzilla Bug #620925 - CC: auditor needs to be able to download audit
      logs in the java subsystems
-     Bugzilla Bug #621322 - Provide switch disabling SCEP support in CA
-     Bugzilla Bug #583824 - CC: Duplicate servlet mappings found as part of
      CC interface doc review
-     Bugzilla Bug #621602 - pkiconsole: Click on 'Publishing' option with
      admin privilege throws error "You are not authorized to perform this
      operation".
-     Bugzilla Bug #583825 - CC: Obsolete servlets to be removed from web.xml
      as part of CC interface review
-     Bugzilla Bug #583823 - CC: Auditing issues found as result of
      CC - interface review
-     Bugzilla Bug #519291 - Deleting a CRL Issuing Point after edits throws
      'Internal Server Error'.
-     Bugzilla Bug #586700 - OCSP Server throws fatal error while using
      OCSP console for renewing SSL Server certificate.
-     Bugzilla Bug #621337 - Limit the received senderNonce value to 16 bytes.
-     Bugzilla Bug #621338 - Include a server randomly-generated 16 byte
      senderNonce in all signed SCEP responses.
-     Bugzilla Bug #558100 - host challenge of the Secure Channel needs to be
      generated on TKS instead of TPS.
-     Bugzilla Bug #630121 - OCSP responder lacking option to delete or
      disable a CA that it serves
-     Bugzilla Bug #634663 - CA CMC response default hard-coded to SHA1
-     Bugzilla Bug #621327 - Provide switch disabling algorithm downgrade
      attack in SCEP
-     Bugzilla Bug #621334 - Provide an option to set default hash algorithm
      for signing SCEP response messages.
-     Bugzilla Bug #539781 - rhcs 71 - CRLs Partitioned
      by Reason Code - onlySomeReasons ?
-     Bugzilla Bug #637330 - CC feature: Key Management - provide signature
      verification functions (JAVA subsystems)
-     Bugzilla Bug #555927 - rhcs80 - AgentRequestFilter servlet and port
      fowarding for agent services
-     Bugzilla Bug #524916 - ECC key constraints plug-ins should be based on
      ECC curve names (not on key sizes).
-     Bugzilla Bug #516632 - RHCS 7.1 - CS Incorrectly Issuing Multiple
      Certificates from the Same Request
-     Bugzilla Bug #638242 - Installation Wizard: at SizePanel, fix selection
      of signature algorithm; and for ECC curves
-     Bugzilla Bug #529945 - (Instructions and sample only) CS 8.0 GA
      release -- DRM and TKS do not seem to have CRL checking enabled
-     Bugzilla Bug #609641 - CC: need procedure (and possibly tools) to help
      correctly set up CC environment
-     Bugzilla Bug #509481 - RFE: support sMIMECapabilities extensions in
      certificates (RFC 4262)
-     Bugzilla Bug #651916 - kra and ocsp are using incorrect ports
      to talk to CA and complete configuration in DonePanel
-     Bugzilla Bug #511990 - rhcs 7.3, 8.0 - re-activate missing object
      signing support in RHCS
-     Bugzilla Bug #651977 - turn off ssl2 for java servers (server.xml)
-     Bugzilla Bug #489385 - references to rhpki
-     Bugzilla Bug #499494 - change CA defaults to SHA2
-     Bugzilla Bug #623452 - rhcs80 pkiconsole profile policy editor limit
      policy extension to 5 only
-     Bugzilla Bug #649910 - Console: an auditor or agent can be added to
      an administrator group.
-     Bugzilla Bug #632425 - Port to tomcat6
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #653576 - tomcat5 does not always run filters on servlets
      as expected
-     Bugzilla Bug #642357 - CC Feature- Self-Test plugins only check for
      validity
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #661128 - incorrect CA ports used for revoke, unrevoke
      certs in TPS
-     Bugzilla Bug #512496 - RFE rhcs80 - crl updates and scheduling feature 
-     Bugzilla Bug #661196 - ECC(with nethsm) subca configuration fails with
      Key Type RSA Not Matched despite using ECC key pairs for rootCA & subCA.
-     Bugzilla Bug #649343 - Publishing queue should recover from CA crash.
-     Bugzilla Bug #491183 - rhcs rfe - add rfc 4523 support for pkiUser and
      pkiCA, obsolete 2252 and 2256
-     Bugzilla Bug #223346 - Two conflicting ACL list definitions in source
      repository
-     Bugzilla Bug #640710 - Current SCEP implementation does not support HSMs
-     Bugzilla Bug #656733 - Standardize jar install location and jar names
-     Bugzilla Bug #661142 - Verification should fail when
      a revoked certificate is added
-     Bugzilla Bug #668100 - DRM storage cert has OCSP signing extended key
      usage
-     Bugzilla Bug #662127 - CC doc Error: SignedAuditLog expiration time
      interface is no longer available through console
-     Bugzilla Bug #531137 - RHCS 7.1 - Running out of Java Heap Memory
      During CRL Generation
- 'pki-silent'
-     Bugzilla Bug #627309 - pkisilent subca configuration fails.
-     Bugzilla Bug #640091 - pkisilent panels need to match with changed java
      subsystems
-     Bugzilla Bug #527322 - pkisilent ConfigureDRM should configure DRM
      Clone.
-     Bugzilla Bug #643053 - pkisilent DRM configuration fails
-     Bugzilla Bug #583754 - pki-silent needs an option to configure signing
      algorithm for CA certificates
-     Bugzilla Bug #489385 - references to rhpki
-     Bugzilla Bug #638377 - Generate PKI UI components which exclude a GUI
      interface
-     Bugzilla Bug #651977 - turn off ssl2 for java servers (server.xml)
-     Bugzilla Bug #640042 - TPS Installlation Wizard: need to move Module
      Panel up to before Security Domain Panel
-     Bugzilla Bug #643206 - New CMake based build system for Dogtag
-     Bugzilla Bug #588323 - Failed to enable cipher 0xc001
-     Bugzilla Bug #656733 - Standardize jar install location and jar names
-     Bugzilla Bug #645895 - pkisilent: add ability to select ECC curves,
      signing algorithm
-     Bugzilla Bug #658641 - pkisilent doesn't not properly handle passwords
      with special characters
-     Bugzilla Bug #642741 - CS build uses deprecated functions

* Thu Jan 13 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.0-3
- Bugzilla Bug #668839 - Review Request: pki-core
-   Removed empty "pre" from "pki-ca"
-   Consolidated directory ownership
-   Corrected file ownership within subpackages
-   Removed all versioning from NSS and NSPR packages

* Thu Jan 13 2011 Matthew Harmsen <mharmsen@redhat.com> 9.0.0-2
- Bugzilla Bug #668839 - Review Request: pki-core
-   Added component versioning comments
-   Updated JSS from "4.2.6-10" to "4.2.6-12"
-   Modified installation section to preserve timestamps
-   Removed sectional comments

* Wed Dec 1 2010 Matthew Harmsen <mharmsen@redhat.com> 9.0.0-1
- Initial revision. (kwright@redhat.com & mharmsen@redhat.com)

