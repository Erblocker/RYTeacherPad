package com.netspace.library.threads;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.service.StudentAnswerImageService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public class CheckNewVersionThread2 extends Thread {
    public static final int MSG_AUTHFAIL = 258;
    public static final int MSG_AUTHSUCCESS = 257;
    public static final int MSG_CREATEPROGRESS = 153;
    public static final int MSG_DOWNLOADFAIL = 260;
    public static final int MSG_DOWNLOADPROGRESS = 256;
    public static final int MSG_DOWNLOADSUCCESS = 261;
    public static final int MSG_NONEWVERSION = 259;
    public static final int MSG_TEXT = 152;
    public static final int MSG_VERSIONCHECKFAIL = 262;
    private int REQUEST_TIMEOUT = 20000;
    private int SO_TIMEOUT = 20000;
    private Activity m_Activity;
    private Context m_Context;
    private Handler m_MessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CheckNewVersionThread2.MSG_TEXT /*152*/:
                    CheckNewVersionThread2.this.m_Progress.setMessage((String) msg.obj);
                    return;
                case CheckNewVersionThread2.MSG_CREATEPROGRESS /*153*/:
                    CheckNewVersionThread2.this.m_Progress = new ProgressDialog(CheckNewVersionThread2.this.m_Activity);
                    CheckNewVersionThread2.this.m_Progress.setTitle("版本更新检测");
                    CheckNewVersionThread2.this.m_Progress.setMessage("正在检查服务器上是否有新版本，请稍候...");
                    CheckNewVersionThread2.this.m_Progress.setCancelable(false);
                    CheckNewVersionThread2.this.m_Progress.setIndeterminate(true);
                    CheckNewVersionThread2.this.m_Progress.setProgressStyle(1);
                    CheckNewVersionThread2.this.m_Progress.show();
                    return;
                case 256:
                    int nMax = msg.arg1;
                    int nCurrent = msg.arg2;
                    CheckNewVersionThread2.this.m_Progress.setIndeterminate(false);
                    CheckNewVersionThread2.this.m_Progress.setMax(nMax);
                    CheckNewVersionThread2.this.m_Progress.setProgress(nCurrent);
                    return;
                case 259:
                    new Builder(CheckNewVersionThread2.this.m_Context).setTitle("版本更新检查").setMessage("您当前使用的是最新版，无需更新。").setPositiveButton("确定", null).show();
                    CheckNewVersionThread2.this.m_Progress.dismiss();
                    CheckNewVersionThread2.this.m_Progress = null;
                    return;
                case 260:
                    new Builder(CheckNewVersionThread2.this.m_Context).setTitle("版本更新检查").setMessage("新版本下载失败，请重试。").setPositiveButton("确定", null).show();
                    CheckNewVersionThread2.this.m_Progress.dismiss();
                    CheckNewVersionThread2.this.m_Progress = null;
                    return;
                case 261:
                    new Builder(CheckNewVersionThread2.this.m_Context).setTitle("版本更新检查").setMessage("新版本下载成功。").setPositiveButton("确定", null).show();
                    CheckNewVersionThread2.this.m_Progress.dismiss();
                    CheckNewVersionThread2.this.m_Progress = null;
                    return;
                case 262:
                    new Builder(CheckNewVersionThread2.this.m_Context).setTitle("版本更新检查").setMessage("版本更新检查失败。").setPositiveButton("确定", null).show();
                    CheckNewVersionThread2.this.m_Progress.dismiss();
                    CheckNewVersionThread2.this.m_Progress = null;
                    return;
                default:
                    return;
            }
        }
    };
    private ProgressDialog m_Progress;
    private boolean m_bUseOnlyFileName = false;
    private String m_szURL;
    private String m_szVersion;

    private class ViewProgressText implements Runnable {
        private String m_szPromptText = "";

        public ViewProgressText(String szText) {
            this.m_szPromptText = szText;
        }

        public void run() {
            if (CheckNewVersionThread2.this.m_Progress != null) {
                CheckNewVersionThread2.this.m_Progress.setMessage(this.m_szPromptText);
            }
        }
    }

    public CheckNewVersionThread2(Context Context, Activity Activity, String szCurrentVersion, String szURL, boolean bUseOnlyFileName) {
        this.m_Context = Context;
        this.m_Activity = Activity;
        this.m_szVersion = szCurrentVersion;
        this.m_szURL = szURL;
        this.m_bUseOnlyFileName = bUseOnlyFileName;
    }

    private int CompareVersion(String szVersion1, String szVersion2) {
        String[] arrVersion1 = szVersion1.split("\\.");
        String[] arrVersion2 = szVersion2.split("\\.");
        if (arrVersion1.length == arrVersion2.length) {
            for (int i = 0; i < arrVersion1.length; i++) {
                int a = Integer.parseInt(arrVersion1[i]);
                int b = Integer.parseInt(arrVersion2[i]);
                if (a > b) {
                    return 1;
                }
                if (a < b) {
                    return -1;
                }
            }
        }
        return 0;
    }

    private String DoDownloadFile(String szURL) {
        HttpGet httpGet = new HttpGet(szURL);
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
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            long nTotalLength = httpResponse.getEntity().getContentLength();
            InputStream inputStream = httpResponse.getEntity().getContent();
            File outputFile = File.createTempFile("upgrade", ".apk", this.m_Context.getCacheDir());
            byte[] Buffer = new byte[8192];
            int nCurrentPos = 0;
            FileOutputStream OutputStream = new FileOutputStream(outputFile.getPath());
            while (true) {
                int nReadCount = inputStream.read(Buffer);
                if (nReadCount == -1) {
                    break;
                }
                OutputStream.write(Buffer, 0, nReadCount);
                nCurrentPos += nReadCount;
                this.m_MessageHandler.obtainMessage(256, (int) nTotalLength, nCurrentPos).sendToTarget();
            }
            OutputStream.close();
            inputStream.close();
            if (((long) nCurrentPos) != nTotalLength) {
                return null;
            }
            String szFinalFileName = outputFile.getPath();
            outputFile.setExecutable(true, false);
            outputFile.setReadable(true, false);
            return szFinalFileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void InstallAPK(String szFileName) {
        File file = new File(szFileName);
        Intent intent = new Intent();
        intent.addFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        this.m_Context.startActivity(intent);
    }

    public void run() {
        boolean bDownloadFailed;
        MalformedURLException e;
        this.m_MessageHandler.obtainMessage(MSG_CREATEPROGRESS).sendToTarget();
        ConnectivityManager cm = (ConnectivityManager) this.m_Context.getSystemService("connectivity");
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e2) {
        }
        ViewProgressText viewProgressText = new ViewProgressText("正在等待网络就绪...");
        while (!isConnected) {
            if (activeNetwork != null) {
                isConnected = activeNetwork.isConnected();
            }
            activeNetwork = cm.getActiveNetworkInfo();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e3) {
                e3.printStackTrace();
            }
        }
        viewProgressText = new ViewProgressText("开始检查更新...");
        HttpGet httpGet = new HttpGet(this.m_szURL);
        String szNewVersion = "";
        String szNewVersionURL = "";
        boolean bFileReadError = false;
        try {
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, this.REQUEST_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, this.SO_TIMEOUT);
            HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                bDownloadFailed = false;
                BufferedReader textBuffer = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                for (String szOneLine = textBuffer.readLine(); szOneLine != null; szOneLine = textBuffer.readLine()) {
                    int nSepPos = szOneLine.indexOf("=");
                    String szFieldName = "";
                    String szFieldValue = "";
                    if (nSepPos != -1) {
                        szFieldName = szOneLine.substring(0, nSepPos);
                        szFieldValue = szOneLine.substring(nSepPos + 1).trim();
                        if (szFieldName.compareToIgnoreCase(StudentAnswerImageService.LISTURL) == 0) {
                            szNewVersionURL = szFieldValue;
                        }
                        if (szFieldName.compareToIgnoreCase(ClientCookie.VERSION_ATTR) == 0) {
                            szNewVersion = szFieldValue;
                        }
                    }
                }
                if (szNewVersion.isEmpty() || szNewVersionURL.isEmpty()) {
                    bFileReadError = true;
                }
            } else {
                bDownloadFailed = true;
            }
        } catch (Exception e4) {
            e4.printStackTrace();
            bDownloadFailed = true;
        }
        if (bDownloadFailed || bFileReadError) {
            viewProgressText = new ViewProgressText("版本更新检查失败。");
            this.m_MessageHandler.obtainMessage(262, Integer.valueOf(0)).sendToTarget();
            return;
        }
        if (CompareVersion(szNewVersion, this.m_szVersion) > 0) {
            String szTargetFile;
            viewProgressText = new ViewProgressText("正在下载新版本...");
            URL TargetFileURL = null;
            URL UpdateFileURL = null;
            try {
                URL TargetFileURL2 = new URL(szNewVersionURL);
                try {
                    UpdateFileURL = new URL(this.m_szURL);
                    TargetFileURL = TargetFileURL2;
                } catch (MalformedURLException e5) {
                    e = e5;
                    TargetFileURL = TargetFileURL2;
                    e.printStackTrace();
                    if (this.m_bUseOnlyFileName) {
                        szNewVersionURL = "http://" + UpdateFileURL.getAuthority() + "/updates" + TargetFileURL.getFile();
                    }
                    szTargetFile = DoDownloadFile(szNewVersionURL);
                    if (szTargetFile != null) {
                        viewProgressText = new ViewProgressText("正在安装新版本...");
                        InstallAPK(szTargetFile);
                        this.m_MessageHandler.obtainMessage(261, Integer.valueOf(0)).sendToTarget();
                        return;
                    }
                    viewProgressText = new ViewProgressText("新版本下载出现错误。");
                    this.m_MessageHandler.obtainMessage(260, Integer.valueOf(0)).sendToTarget();
                    return;
                }
            } catch (MalformedURLException e6) {
                e = e6;
                e.printStackTrace();
                if (this.m_bUseOnlyFileName) {
                    szNewVersionURL = "http://" + UpdateFileURL.getAuthority() + "/updates" + TargetFileURL.getFile();
                }
                szTargetFile = DoDownloadFile(szNewVersionURL);
                if (szTargetFile != null) {
                    viewProgressText = new ViewProgressText("新版本下载出现错误。");
                    this.m_MessageHandler.obtainMessage(260, Integer.valueOf(0)).sendToTarget();
                    return;
                }
                viewProgressText = new ViewProgressText("正在安装新版本...");
                InstallAPK(szTargetFile);
                this.m_MessageHandler.obtainMessage(261, Integer.valueOf(0)).sendToTarget();
                return;
            }
            if (this.m_bUseOnlyFileName) {
                szNewVersionURL = "http://" + UpdateFileURL.getAuthority() + "/updates" + TargetFileURL.getFile();
            }
            szTargetFile = DoDownloadFile(szNewVersionURL);
            if (szTargetFile != null) {
                viewProgressText = new ViewProgressText("正在安装新版本...");
                InstallAPK(szTargetFile);
                this.m_MessageHandler.obtainMessage(261, Integer.valueOf(0)).sendToTarget();
                return;
            }
            viewProgressText = new ViewProgressText("新版本下载出现错误。");
            this.m_MessageHandler.obtainMessage(260, Integer.valueOf(0)).sendToTarget();
            return;
        }
        viewProgressText = new ViewProgressText("当前使用的是最新版本，无需更新。");
        this.m_MessageHandler.obtainMessage(259, Integer.valueOf(0)).sendToTarget();
    }
}
