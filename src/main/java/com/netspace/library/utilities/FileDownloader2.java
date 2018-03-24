package com.netspace.library.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.pad.library.R;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class FileDownloader2 extends AsyncTask<String, Integer, String> {
    private final int MSG_DOWNLOAD_FAIL = 2;
    private final int MSG_DOWNLOAD_SUCCESS = 1;
    private final int TIMEOUT = 4000;
    private OnDownloadResultCallBack mCallBack;
    private Context mContext;
    private StatusBarDisplayer mStatusBarDisplayer;
    private Handler mThreadMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    FileDownloader2.this.mStatusBarDisplayer.hideProgressBox();
                    if (FileDownloader2.this.mCallBack != null) {
                        FileDownloader2.this.mCallBack.onDownloadSuccess();
                        return;
                    }
                    return;
                case 2:
                    FileDownloader2.this.mStatusBarDisplayer.hideProgressBox();
                    FileDownloader2.this.mStatusBarDisplayer.setText("下载出现错误，错误信息：" + ((String) msg.obj));
                    if (FileDownloader2.this.mCallBack != null) {
                        FileDownloader2.this.mCallBack.onDownloadFail((String) msg.obj);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private String mszLocalFilePath;
    private String mszProgressText;
    private String mszProgressTitle;
    private String mszURL;

    public interface OnDownloadResultCallBack {
        void onDownloadFail(String str);

        void onDownloadProgress(int i, int i2);

        void onDownloadSuccess();
    }

    public FileDownloader2(Context Context, String szProgressTitle, String szProgressText) {
        this.mContext = Context;
        this.mszProgressText = szProgressText;
        this.mszProgressTitle = szProgressTitle;
    }

    public void setCallBack(OnDownloadResultCallBack callBack) {
        this.mCallBack = callBack;
    }

    protected String doInBackground(String... sUrl) {
        boolean bisResume = false;
        boolean bForget = false;
        try {
            this.mszURL = sUrl[0];
            this.mszLocalFilePath = sUrl[1];
            if (sUrl.length == 3) {
                bForget = sUrl[2].equalsIgnoreCase("restart");
            }
            File TempFile = new File(this.mszLocalFilePath + "-" + Utilities.md5(this.mszURL) + ".tmp");
            if (bForget) {
                TempFile.delete();
            }
            OkHttpClient client = new Builder().readTimeout(4000, TimeUnit.MILLISECONDS).connectTimeout(4000, TimeUnit.MILLISECONDS).writeTimeout(4000, TimeUnit.MILLISECONDS).hostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).retryOnConnectionFailure(true).build();
            Request.Builder requestBuilder = new Request.Builder().url(this.mszURL);
            if (TempFile.exists()) {
                bisResume = true;
                requestBuilder.addHeader("Range", "bytes=" + String.valueOf(TempFile.length()) + "-");
            }
            Response response = client.newCall(requestBuilder.build()).execute();
            int nResponseCode = response.code();
            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            BufferedInputStream input = new BufferedInputStream(responseBody.byteStream());
            OutputStream fileOutputStream;
            if (bisResume) {
                fileOutputStream = new FileOutputStream(TempFile, true);
            } else {
                fileOutputStream = new FileOutputStream(TempFile, false);
            }
            long currentDownloadedSize = TempFile.length();
            long currentTotalByteSize = responseBody.contentLength() + currentDownloadedSize;
            byte[] data = new byte[1048576];
            long nLastRefreshTick = System.currentTimeMillis();
            do {
                int count = input.read(data);
                if (count == -1) {
                    break;
                }
                currentDownloadedSize += (long) count;
                output.write(data, 0, count);
                if (System.currentTimeMillis() - nLastRefreshTick > 1000) {
                    publishProgress(new Integer[]{Integer.valueOf((int) currentDownloadedSize), Integer.valueOf((int) currentTotalByteSize)});
                    if (this.mCallBack != null) {
                        this.mCallBack.onDownloadProgress((int) currentDownloadedSize, (int) currentTotalByteSize);
                    }
                    nLastRefreshTick = System.currentTimeMillis();
                }
            } while (!isCancelled());
            output.flush();
            output.close();
            input.close();
            if (isCancelled()) {
                this.mThreadMessageHandler.obtainMessage(2, "文件下载被取消").sendToTarget();
            } else if (currentDownloadedSize == currentTotalByteSize) {
                new File(this.mszLocalFilePath).delete();
                TempFile.renameTo(new File(this.mszLocalFilePath));
                this.mThreadMessageHandler.obtainMessage(1).sendToTarget();
            } else {
                this.mThreadMessageHandler.obtainMessage(2, "文件下载被异常终止").sendToTarget();
            }
            if (isCancelled()) {
                this.mStatusBarDisplayer.hideMessage();
            }
        } catch (Exception e) {
            if (!isCancelled()) {
                this.mThreadMessageHandler.obtainMessage(2, e.getMessage()).sendToTarget();
            }
            e.printStackTrace();
        }
        return null;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this.mStatusBarDisplayer = new StatusBarDisplayer(this.mContext);
        this.mStatusBarDisplayer.setTitle(this.mszProgressTitle);
        this.mStatusBarDisplayer.setText(this.mszProgressText);
        this.mStatusBarDisplayer.setIcon(R.drawable.ic_cloud_download);
        this.mStatusBarDisplayer.showProgressBox(null);
    }

    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        this.mStatusBarDisplayer.setProgress(progress[0].intValue());
        this.mStatusBarDisplayer.setProgressMax(progress[1].intValue());
    }
}
