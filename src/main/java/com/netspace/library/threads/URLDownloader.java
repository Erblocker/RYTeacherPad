package com.netspace.library.threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.utilities.Utilities;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public class URLDownloader extends Thread {
    public static final int MSG_DOWNLOAD_FAIL = -1;
    public static final int MSG_DOWNLOAD_PROGRESS = 2;
    public static final int MSG_DOWNLOAD_SUCCESS = 1;
    private int REQUEST_TIMEOUT = 20000;
    private int SO_TIMEOUT = 20000;
    private OnURLDownloadEventListener m_CallBackInterface;
    private Context m_Context;
    private Handler m_ThreadMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (URLDownloader.this.m_CallBackInterface != null) {
                URLDownloader.this.m_CallBackInterface.onURLDownloadEvent(msg.what, URLDownloader.this.m_nProgress, URLDownloader.this.m_nProgressMax, URLDownloader.this.m_fPercent, URLDownloader.this.m_szURL, URLDownloader.this.m_szLocalFilePath, URLDownloader.this.m_szErrorMessage);
            }
        }
    };
    private boolean m_bCancelled = false;
    private boolean m_bSkipIfLocalFileExsit = false;
    private float m_fPercent = 0.0f;
    private int m_nProgress = 0;
    private int m_nProgressMax = 0;
    private String m_szErrorMessage;
    private String m_szLocalFilePath;
    private String m_szURL;

    public interface OnURLDownloadEventListener {
        void onURLDownloadEvent(int i, int i2, int i3, float f, String str, String str2, String str3);
    }

    public URLDownloader(Context Context, String szURL, OnURLDownloadEventListener CallBack) {
        this.m_szURL = szURL;
        this.m_CallBackInterface = CallBack;
        this.m_Context = Context;
        if (this.m_szURL.indexOf("%HOST%") != -1) {
            this.m_szURL = this.m_szURL.replace("%HOST%", MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    public void Cancel() {
        this.m_bCancelled = true;
    }

    public void setSkipIfLocalFileExsit(boolean bSkip) {
        this.m_bSkipIfLocalFileExsit = bSkip;
    }

    public static String GetLocalFileName(Context Context, String szURL) {
        String szFinalFileName = Utilities.md5(szURL);
        return new StringBuilder(String.valueOf(Context.getExternalCacheDir().getAbsolutePath())).append("/cache_").append(szFinalFileName).append(".").append(Utilities.getFileExtName(szURL)).toString();
    }

    public void run() {
        HttpGet httpGet = new HttpGet(this.m_szURL);
        String szFinalFileName = GetLocalFileName(this.m_Context, this.m_szURL);
        boolean bDownloadSuccess = true;
        if (this.m_bSkipIfLocalFileExsit && new File(szFinalFileName).exists()) {
            this.m_szLocalFilePath = szFinalFileName;
            this.m_ThreadMessageHandler.obtainMessage(1).sendToTarget();
            return;
        }
        try {
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, this.REQUEST_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, this.SO_TIMEOUT);
            HttpClient httpClient = null;
            if (MyiBaseApplication.isUseSSL()) {
                HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                DefaultHttpClient client2 = new DefaultHttpClient();
                SchemeRegistry registry = new SchemeRegistry();
                SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
                socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
                registry.register(new Scheme(HttpHost.DEFAULT_SCHEME_NAME, PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", socketFactory, 443));
                httpClient = new DefaultHttpClient(new SingleClientConnManager(client2.getParams(), registry), httpParams);
                HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            }
            if (httpClient == null) {
                httpClient = new DefaultHttpClient(httpParams);
            }
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                long nTotalLength = httpResponse.getEntity().getContentLength();
                InputStream inputStream = httpResponse.getEntity().getContent();
                File outputDir = this.m_Context.getExternalCacheDir();
                File file = new File(szFinalFileName);
                this.m_szLocalFilePath = file.getAbsolutePath();
                byte[] Buffer = new byte[16384];
                int nCurrentPos = 0;
                FileOutputStream OutputStream = new FileOutputStream(file.getPath());
                do {
                    int nReadCount = inputStream.read(Buffer);
                    if (nReadCount == -1) {
                        break;
                    }
                    OutputStream.write(Buffer, 0, nReadCount);
                    nCurrentPos += nReadCount;
                    this.m_nProgress = nCurrentPos;
                    this.m_nProgressMax = (int) nTotalLength;
                    this.m_fPercent = (((float) nCurrentPos) / ((float) nTotalLength)) * 100.0f;
                    this.m_ThreadMessageHandler.obtainMessage(2).sendToTarget();
                } while (!this.m_bCancelled);
                httpClient.getConnectionManager().shutdown();
                Buffer = null;
                OutputStream.close();
                inputStream.close();
                if (((long) nCurrentPos) == nTotalLength) {
                    file.setReadable(true, false);
                    this.m_ThreadMessageHandler.obtainMessage(1).sendToTarget();
                } else {
                    this.m_ThreadMessageHandler.obtainMessage(-1).sendToTarget();
                }
            } else {
                this.m_ThreadMessageHandler.obtainMessage(-1).sendToTarget();
            }
        } catch (Exception e) {
            e.printStackTrace();
            bDownloadSuccess = false;
            this.m_szErrorMessage = e.getMessage();
            this.m_ThreadMessageHandler.obtainMessage(-1).sendToTarget();
        }
        if ((this.m_bCancelled || !bDownloadSuccess) && this.m_szLocalFilePath != null) {
            new File(this.m_szLocalFilePath).delete();
        }
    }
}
