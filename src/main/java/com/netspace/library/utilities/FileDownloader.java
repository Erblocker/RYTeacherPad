package com.netspace.library.utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloader extends AsyncTask<String, Integer, String> {
    private final int MSG_DOWNLOAD_FAIL = 2;
    private final int MSG_DOWNLOAD_SUCCESS = 1;
    private Context m_Context;
    private OnDownloadEventListener m_FailListener = null;
    private ProgressDialog m_ProgressDialog;
    private OnDownloadEventListener m_SuccessListener = null;
    private Handler m_ThreadMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    FileDownloader.this.m_ProgressDialog.hide();
                    if (FileDownloader.this.m_SuccessListener != null) {
                        FileDownloader.this.m_SuccessListener.onDownloadEvent(true, FileDownloader.this.m_szURL, FileDownloader.this.m_szLocalFilePath, null);
                        return;
                    }
                    return;
                case 2:
                    FileDownloader.this.m_ProgressDialog.hide();
                    if (FileDownloader.this.m_FailListener != null) {
                        FileDownloader.this.m_FailListener.onDownloadEvent(false, FileDownloader.this.m_szURL, FileDownloader.this.m_szLocalFilePath, (String) msg.obj);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private String m_szLocalFilePath;
    private String m_szProgressText;
    private String m_szURL;

    public interface OnDownloadEventListener {
        void onDownloadEvent(boolean z, String str, String str2, String str3);
    }

    public FileDownloader(Context Context, String szProgressText) {
        this.m_Context = Context;
        this.m_szProgressText = szProgressText;
    }

    public void setSuccessListener(OnDownloadEventListener successListener) {
        this.m_SuccessListener = successListener;
    }

    public void setFailListener(OnDownloadEventListener failListener) {
        this.m_FailListener = failListener;
    }

    protected String doInBackground(String... sUrl) {
        try {
            this.m_szURL = sUrl[0];
            this.m_szLocalFilePath = sUrl[1];
            URL url = new URL(sUrl[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            int fileLength = connection.getContentLength();
            if (isCancelled()) {
                return null;
            }
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(sUrl[1]);
            byte[] data = new byte[1024];
            long total = 0;
            while (true) {
                int count = input.read(data);
                if (!(count == -1 || isCancelled())) {
                    total += (long) count;
                    publishProgress(new Integer[]{Integer.valueOf((int) ((100 * total) / ((long) fileLength)))});
                    output.write(data, 0, count);
                }
            }
            output.flush();
            output.close();
            input.close();
            if (isCancelled()) {
                new File(this.m_szLocalFilePath).delete();
            } else {
                this.m_ThreadMessageHandler.obtainMessage(1).sendToTarget();
            }
            return null;
        } catch (Exception e) {
            new File(this.m_szLocalFilePath).delete();
            if (!isCancelled()) {
                this.m_ThreadMessageHandler.obtainMessage(2, e.getMessage()).sendToTarget();
            }
            e.printStackTrace();
        }
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this.m_ProgressDialog = new ProgressDialog(this.m_Context);
        this.m_ProgressDialog.setMessage(this.m_szProgressText);
        this.m_ProgressDialog.setTitle("正在下载");
        this.m_ProgressDialog.setIndeterminate(false);
        this.m_ProgressDialog.setMax(100);
        this.m_ProgressDialog.setProgressStyle(1);
        this.m_ProgressDialog.setCancelable(false);
        this.m_ProgressDialog.setButton(-2, "取消", new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FileDownloader.this.cancel(true);
            }
        });
        this.m_ProgressDialog.show();
    }

    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        this.m_ProgressDialog.setProgress(progress[0].intValue());
    }
}
