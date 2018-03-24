package com.netspace.library.threads;

import android.os.Build;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.threads.LocalFileUploader.FileUploaderListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import java.io.File;

public class UsageDataUploadThread extends Thread {
    private static final String TAG = "UsageDataUploadThread";
    private static String mszDestFile = "/sdcard/myiapp_usage.upload";
    private static String mszSourceFile = "/sdcard/myiapp_usage.txt";
    private LocalFileUploader mFileUploader;
    private FileUploaderListener mUploadCallBack = new FileUploaderListener() {
        public void onFileUploadProgress(LocalFileUploader Uploader, int nCurrentPos, int nMaxPos) {
        }

        public void onFileUploadedSuccess(LocalFileUploader Uploader) {
            UsageDataUploadThread.this.mbUploading = false;
            Log.i(UsageDataUploadThread.TAG, "Usage data uploaded success.");
            new File(UsageDataUploadThread.mszDestFile).delete();
            UsageDataUploadThread.this.mFileUploader = null;
        }

        public void onFileUploadedFail(LocalFileUploader Uploader) {
            UsageDataUploadThread.this.mbUploading = false;
            Log.i(UsageDataUploadThread.TAG, "Usage data uploaded failed.");
            UsageDataUploadThread.this.mFileUploader = null;
        }
    };
    private boolean mbStop = false;
    private boolean mbUploading = false;
    private int mnTimeCheck = 60;
    private String mszWifiMac;

    public void run() {
        setName(TAG);
        this.mszWifiMac = Utilities.getMacAddr();
        this.mszWifiMac = this.mszWifiMac.replace(":", "");
        this.mszWifiMac = this.mszWifiMac.toUpperCase();
        super.run();
        int nTimeCount = this.mnTimeCheck;
        while (!this.mbStop) {
            if (Utilities.isNetworkConnected(MyiBaseApplication.getBaseAppContext()) && !VirtualNetworkObject.getOfflineMode() && !this.mbUploading && (new File(mszSourceFile).exists() || new File(mszDestFile).exists())) {
                nTimeCount--;
                if (nTimeCount <= 0) {
                    startUpload();
                    nTimeCount = this.mnTimeCheck;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        this.mbStop = true;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startUpload() {
        String szUploadURL = MyiBaseApplication.getProtocol() + "://" + VirtualNetworkObject.getServerAddress() + "/PutFileChunk?filename=" + ("AppUsage_" + Build.MODEL + "_" + this.mszWifiMac + "_" + Utilities.getWifiIP(MyiBaseApplication.getBaseAppContext())) + "_" + Utilities.createGUID() + ".txt";
        Utilities.appendUTF8File(new File(mszSourceFile), new File(mszDestFile));
        new File(mszSourceFile).delete();
        if (new File(mszDestFile).exists()) {
            Log.i(TAG, "Start upload usage data.");
            this.mbUploading = true;
            this.mFileUploader = new LocalFileUploader(szUploadURL, mszDestFile, true, this.mUploadCallBack);
            this.mFileUploader.start();
        }
    }
}
