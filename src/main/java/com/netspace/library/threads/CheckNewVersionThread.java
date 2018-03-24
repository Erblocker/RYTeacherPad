package com.netspace.library.threads;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.widget.TextView;
import com.netspace.library.service.StudentAnswerImageService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

public class CheckNewVersionThread extends Thread {
    public static final int MSG_AUTHFAIL = 258;
    public static final int MSG_AUTHSUCCESS = 257;
    public static final int MSG_DOWNLOADFAIL = 260;
    public static final int MSG_DOWNLOADPROGRESS = 256;
    public static final int MSG_DOWNLOADSUCCESS = 261;
    public static final int MSG_NONEWVERSION = 259;
    public static final int MSG_VERSIONCHECKFAIL = 262;
    private int REQUEST_TIMEOUT = 20000;
    private int SO_TIMEOUT = 20000;
    private Context m_Context;
    private WeakReference<Handler> m_MessageHandler;
    private WeakReference<TextView> m_ViewProgress;
    private boolean m_bUseOnlyFileName = false;
    private String m_szURL;
    private String m_szVersion;

    private class ViewProgressText implements Runnable {
        private String m_szPromptText = "";

        public ViewProgressText(String szText) {
            this.m_szPromptText = szText;
        }

        public void run() {
            TextView ViewProgress = (TextView) CheckNewVersionThread.this.m_ViewProgress.get();
            if (ViewProgress != null) {
                ViewProgress.setText(this.m_szPromptText);
            }
        }
    }

    public CheckNewVersionThread(Context Context, String szCurrentVersion, String szURL, TextView viewProgress, Handler MessageHandler, boolean bUseOnlyFileName) {
        this.m_Context = Context;
        this.m_szVersion = szCurrentVersion;
        this.m_szURL = szURL;
        this.m_ViewProgress = new WeakReference(viewProgress);
        this.m_MessageHandler = new WeakReference(MessageHandler);
        this.m_bUseOnlyFileName = bUseOnlyFileName;
    }

    public void SetMessageHandler(Handler Handler) {
        this.m_MessageHandler = new WeakReference(Handler);
    }

    public void SetViewProgress(TextView ViewProgress) {
        this.m_ViewProgress = new WeakReference(ViewProgress);
    }

    public void SetContext(Context Context) {
        this.m_Context = Context;
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
            HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(httpGet);
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
                float fPercent = (((float) nCurrentPos) / ((float) nTotalLength)) * 100.0f;
                Handler Handler = (Handler) this.m_MessageHandler.get();
                if (Handler != null) {
                    Handler.obtainMessage(256, Float.valueOf(fPercent)).sendToTarget();
                }
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
        String szTargetFile;
        final TextView TempViewProgress;
        ConnectivityManager cm = (ConnectivityManager) this.m_Context.getSystemService("connectivity");
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e2) {
        }
        TextView ViewProgress = (TextView) this.m_ViewProgress.get();
        Handler MessageHandler = (Handler) this.m_MessageHandler.get();
        if (ViewProgress != null) {
            ViewProgress.post(new ViewProgressText("正在等待网络就绪..."));
        }
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
        if (ViewProgress != null) {
            ViewProgress.post(new ViewProgressText("开始检查更新..."));
        }
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
            if (ViewProgress != null) {
                ViewProgress.post(new ViewProgressText("版本更新检查失败。"));
            }
            if (MessageHandler != null) {
                MessageHandler.obtainMessage(262, Integer.valueOf(0)).sendToTarget();
            }
        } else {
            if (CompareVersion(szNewVersion, this.m_szVersion) > 0) {
                if (ViewProgress != null) {
                    ViewProgress.post(new ViewProgressText("正在下载新版本..."));
                }
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
                            if (ViewProgress != null) {
                                ViewProgress.post(new ViewProgressText("正在安装新版本..."));
                            }
                            InstallAPK(szTargetFile);
                            if (ViewProgress != null) {
                                TempViewProgress = ViewProgress;
                                ViewProgress.post(new Runnable() {
                                    public void run() {
                                        TempViewProgress.setText("");
                                    }
                                });
                            }
                            if (MessageHandler != null) {
                                MessageHandler.obtainMessage(261, Integer.valueOf(0)).sendToTarget();
                            }
                        } else {
                            if (ViewProgress != null) {
                                ViewProgress.post(new ViewProgressText("新版本下载出现错误。"));
                            }
                            if (MessageHandler != null) {
                                MessageHandler.obtainMessage(260, Integer.valueOf(0)).sendToTarget();
                            }
                        }
                    }
                } catch (MalformedURLException e6) {
                    e = e6;
                    e.printStackTrace();
                    if (this.m_bUseOnlyFileName) {
                        szNewVersionURL = "http://" + UpdateFileURL.getAuthority() + "/updates" + TargetFileURL.getFile();
                    }
                    szTargetFile = DoDownloadFile(szNewVersionURL);
                    if (szTargetFile != null) {
                        if (ViewProgress != null) {
                            ViewProgress.post(new ViewProgressText("新版本下载出现错误。"));
                        }
                        if (MessageHandler != null) {
                            MessageHandler.obtainMessage(260, Integer.valueOf(0)).sendToTarget();
                        }
                    } else {
                        if (ViewProgress != null) {
                            ViewProgress.post(new ViewProgressText("正在安装新版本..."));
                        }
                        InstallAPK(szTargetFile);
                        if (ViewProgress != null) {
                            TempViewProgress = ViewProgress;
                            ViewProgress.post(/* anonymous class already generated */);
                        }
                        if (MessageHandler != null) {
                            MessageHandler.obtainMessage(261, Integer.valueOf(0)).sendToTarget();
                        }
                    }
                }
                if (this.m_bUseOnlyFileName) {
                    szNewVersionURL = "http://" + UpdateFileURL.getAuthority() + "/updates" + TargetFileURL.getFile();
                }
                szTargetFile = DoDownloadFile(szNewVersionURL);
                if (szTargetFile != null) {
                    if (ViewProgress != null) {
                        ViewProgress.post(new ViewProgressText("正在安装新版本..."));
                    }
                    InstallAPK(szTargetFile);
                    if (ViewProgress != null) {
                        TempViewProgress = ViewProgress;
                        ViewProgress.post(/* anonymous class already generated */);
                    }
                    if (MessageHandler != null) {
                        MessageHandler.obtainMessage(261, Integer.valueOf(0)).sendToTarget();
                    }
                } else {
                    if (ViewProgress != null) {
                        ViewProgress.post(new ViewProgressText("新版本下载出现错误。"));
                    }
                    if (MessageHandler != null) {
                        MessageHandler.obtainMessage(260, Integer.valueOf(0)).sendToTarget();
                    }
                }
            } else {
                if (ViewProgress != null) {
                    ViewProgress.post(new ViewProgressText("当前使用的是最新版本，无需更新。"));
                }
                if (MessageHandler != null) {
                    MessageHandler.obtainMessage(259, Integer.valueOf(0)).sendToTarget();
                }
            }
        }
    }
}
