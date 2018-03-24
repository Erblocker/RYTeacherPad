package com.foxit.uiextensions.security.certificate;

import android.content.Context;
import com.foxit.uiextensions.security.ICertificateSupport;
import com.foxit.uiextensions.security.ICertificateSupport.CertificateInfo;
import com.foxit.uiextensions.utils.AppUtil;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

public class CertificateSupport implements ICertificateSupport {
    public CertificateSupport(Context context) {
    }

    private static boolean matchUsage(boolean[] keyUsage, int usage) {
        if (usage == 0 || keyUsage == null) {
            return true;
        }
        int i = 0;
        while (i < Math.min(keyUsage.length, 32)) {
            if (((1 << i) & usage) != 0 && !keyUsage[i]) {
                return false;
            }
            i++;
        }
        return true;
    }

    private void generateCertificateInfo(String keyStorePath, String keyStorePassword, CertificateInfo info) throws Exception {
        KeyStore keyStore;
        Throwable th;
        if (keyStorePath.toLowerCase().endsWith(".pfx")) {
            keyStore = KeyStore.getInstance("PKCS12");
        } else {
            keyStore = KeyStore.getInstance("JKS");
        }
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(keyStorePath);
            try {
                keyStore.load(fis2, keyStorePassword.toCharArray());
                if (fis2 != null) {
                    fis2.close();
                }
                Enumeration aliases = keyStore.aliases();
                if (aliases != null) {
                    while (aliases.hasMoreElements()) {
                        Certificate[] certs = keyStore.getCertificateChain((String) aliases.nextElement());
                        if (!(certs == null || certs.length == 0)) {
                            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
                            X509Certificate cert = certs[0];
                            info.startDate = dateformat.format(cert.getNotBefore());
                            info.expiringDate = dateformat.format(cert.getNotAfter());
                            info.issuerUniqueID = "";
                            boolean[] uniqueid = cert.getIssuerUniqueID();
                            if (uniqueid != null) {
                                for (boolean z : uniqueid) {
                                    info.issuerUniqueID += z;
                                }
                            }
                            info.identity = "XXXX";
                            info.keyUsage = cert.getKeyUsage();
                            if (info.keyUsage != null) {
                                String str = "";
                                for (boolean z2 : info.keyUsage) {
                                    str = new StringBuilder(String.valueOf(str)).append(z2 ? 1 : 0).append(" ").toString();
                                }
                            }
                            String algName = cert.getSigAlgName();
                            info.serialNumber = cert.getSerialNumber().toString(16);
                            info.issuer = cert.getIssuerDN().getName();
                            info.publisher = AppUtil.getEntryName(cert.getSubjectDN().getName(), "CN=");
                            info.emailAddress = AppUtil.getEntryName(cert.getIssuerDN().getName(), "E=");
                            if (matchUsage(cert.getKeyUsage(), 1)) {
                                try {
                                    cert.checkValidity();
                                    return;
                                } catch (CertificateExpiredException e) {
                                    info.expired = true;
                                    return;
                                } catch (CertificateNotYetValidException e2) {
                                    info.expired = false;
                                    return;
                                }
                            }
                        }
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                fis = fis2;
                if (fis != null) {
                    fis.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (fis != null) {
                fis.close();
            }
            throw th;
        }
    }

    public CertificateInfo verifyPassword(String filePath, String password) {
        Exception e;
        CertificateInfo info = null;
        try {
            KeyStore var2 = KeyStore.getInstance("PKCS12", "BC");
            var2.load(new FileInputStream(filePath), password.toCharArray());
            Enumeration var3 = var2.aliases();
            CertificateInfo info2 = new CertificateInfo();
            try {
                generateCertificateInfo(filePath, password, info2);
                return info2;
            } catch (Exception e2) {
                e = e2;
                info = info2;
                e.printStackTrace();
                return info;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            return info;
        }
    }

    public CertificateInfo getCertificateInfo(String filePath) {
        Exception e;
        CertificateInfo info = null;
        InputStream is;
        try {
            is = new FileInputStream(filePath);
            X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
            CertificateInfo info2 = new CertificateInfo();
            try {
                is.close();
                return info2;
            } catch (Exception e2) {
                e = e2;
                info = info2;
                e.printStackTrace();
                return info;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            return info;
        } catch (Throwable th) {
            is.close();
        }
    }
}
