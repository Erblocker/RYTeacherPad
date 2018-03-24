package com.netspace.library.utilities;

import android.util.Log;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SSLConnection {
    private static X509TrustManager mOriginalTrustmanager;
    private static SSLSocketFactory mSSLFactory;
    private static TrustManager[] trustManagers;

    public static class _FakeX509TrustManager implements X509TrustManager {
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[0];

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            SSLConnection.mOriginalTrustmanager.checkClientTrusted(arg0, arg1);
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            SSLConnection.mOriginalTrustmanager.checkServerTrusted(arg0, arg1);
            String szSubjectDN = arg0[0].getSubjectDN().getName();
            if (!szSubjectDN.contains("O=宁波睿易教育科技股份有限公司") || !szSubjectDN.contains("CN=*.lexuewang.cn")) {
                throw new CertificateException("服务器证书无效");
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return SSLConnection.mOriginalTrustmanager.getAcceptedIssuers();
        }
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        return mSSLFactory;
    }

    public static X509TrustManager getTrustManager() {
        return mOriginalTrustmanager;
    }

    public static synchronized void allowAllSSL() {
        synchronized (SSLConnection.class) {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            if (trustManagers == null) {
                trustManagers = new TrustManager[]{new _FakeX509TrustManager()};
                try {
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(null);
                    mOriginalTrustmanager = (X509TrustManager) tmf.getTrustManagers()[0];
                } catch (Exception e) {
                }
            }
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustManagers, new SecureRandom());
                mSSLFactory = context.getSocketFactory();
                HttpsURLConnection.setDefaultSSLSocketFactory(mSSLFactory);
            } catch (NoSuchAlgorithmException e2) {
                Log.e("allowAllSSL", e2.toString());
            } catch (KeyManagementException e3) {
                Log.e("allowAllSSL", e3.toString());
            }
        }
    }
}
