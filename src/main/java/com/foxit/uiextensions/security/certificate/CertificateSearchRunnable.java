package com.foxit.uiextensions.security.certificate;

import android.content.Context;
import android.os.Handler;
import com.foxit.uiextensions.utils.AppStorageManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class CertificateSearchRunnable implements Runnable {
    private Context mContext;
    private Handler mHandler;
    private boolean mShouldStop;
    private boolean mbOnlyPfx;

    public CertificateSearchRunnable(Context context) {
        this.mContext = context;
    }

    public void init(Handler handler, boolean isOnlyPfxFile) {
        this.mHandler = handler;
        this.mShouldStop = false;
        this.mbOnlyPfx = isOnlyPfxFile;
    }

    public void stopSearch() {
        this.mShouldStop = true;
    }

    public boolean isStoped() {
        return this.mShouldStop;
    }

    private boolean filterFile(String filename) {
        if (filename.toLowerCase().endsWith(".pfx") || filename.toLowerCase().endsWith(".p12")) {
            return true;
        }
        if (this.mbOnlyPfx || !filename.toLowerCase().endsWith(".cer")) {
            return false;
        }
        return true;
    }

    public void run() {
        if (this.mHandler != null) {
            int i;
            List<File> folderList = new ArrayList();
            List<String> listFiles = AppStorageManager.getInstance(this.mContext).getVolumePaths();
            if (listFiles != null) {
                i = 0;
                while (i < listFiles.size()) {
                    if (!this.mShouldStop) {
                        File file = new File((String) listFiles.get(i));
                        if (file.isDirectory() && !file.isHidden()) {
                            folderList.add(file);
                        } else if (file.isFile() && !file.isHidden()) {
                            if (!this.mShouldStop) {
                                if (filterFile(file.getName())) {
                                    this.mHandler.obtainMessage(17, file).sendToTarget();
                                }
                            } else {
                                return;
                            }
                        }
                        i++;
                    } else {
                        return;
                    }
                }
            }
            while (folderList.size() > 0) {
                if (!this.mShouldStop) {
                    File[] tempFolderFileList = ((File) folderList.remove(0)).listFiles();
                    if (tempFolderFileList != null) {
                        i = 0;
                        while (i < tempFolderFileList.length) {
                            if (!this.mShouldStop) {
                                if (tempFolderFileList[i].isDirectory() && !tempFolderFileList[i].isHidden()) {
                                    folderList.add(tempFolderFileList[i]);
                                } else if (tempFolderFileList[i].isFile() && !tempFolderFileList[i].isHidden()) {
                                    if (!this.mShouldStop) {
                                        if (filterFile(tempFolderFileList[i].getName())) {
                                            this.mHandler.obtainMessage(17, tempFolderFileList[i]).sendToTarget();
                                        }
                                    } else {
                                        return;
                                    }
                                }
                                i++;
                            } else {
                                return;
                            }
                        }
                        continue;
                    }
                } else {
                    return;
                }
            }
            this.mHandler.obtainMessage(18).sendToTarget();
        }
    }
}
