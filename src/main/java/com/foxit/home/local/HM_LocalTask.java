package com.foxit.home.local;

import android.content.Context;
import android.os.Handler;
import com.foxit.app.App;
import com.foxit.uiextensions.controls.filebrowser.imp.FileItem;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppStorageManager;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

class HM_LocalTask {

    static class AllPDFs implements Runnable {
        static AllPDFs sPDFTask = null;
        private Handler handler;
        private Context mContext;
        private FileFilter mFolderFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return !pathname.isHidden() && pathname.canRead() && pathname.isDirectory();
            }
        };
        private FileFilter mPDFFilter = new FileFilter() {
            public boolean accept(File pathname) {
                if (!pathname.isHidden() && pathname.canRead() && pathname.isFile()) {
                    return pathname.getName().toLowerCase().toLowerCase().endsWith(".pdf");
                }
                return false;
            }
        };
        private boolean stopped;

        public static void start(Context context, Handler handler) {
            if (sPDFTask != null) {
                sPDFTask.stopped = true;
                sPDFTask = null;
            }
            sPDFTask = new AllPDFs(context, handler);
            App.instance().getThreadManager().startThread(sPDFTask);
        }

        public static void stop() {
            if (sPDFTask != null) {
                sPDFTask.stopped = true;
            }
            sPDFTask = null;
        }

        public AllPDFs(Context context, Handler handler) {
            this.handler = handler;
            this.mContext = context;
        }

        public void run() {
            if (!this.stopped) {
                Thread.currentThread().setPriority(3);
                List<String> roots = AppStorageManager.getInstance(this.mContext).getVolumePaths();
                if (roots.size() != 0) {
                    for (String path : roots) {
                        if (!this.stopped) {
                            try {
                                scanPDF(new File(path));
                            } catch (StackOverflowError error) {
                                error.printStackTrace();
                            }
                        } else {
                            return;
                        }
                    }
                    this.handler.obtainMessage(11008, null).sendToTarget();
                }
            }
        }

        private void scanPDF(File file) {
            int length;
            int i = 0;
            File[] files = file.listFiles(this.mPDFFilter);
            if (files != null && files.length > 0) {
                FileItem[] items = new FileItem[(files.length + 1)];
                int index = 0;
                items[0] = new FileItem();
                items[0].type = 256;
                items[0].path = file.getPath();
                items[0].name = file.getPath();
                for (File f : files) {
                    File f2;
                    index++;
                    items[index] = new FileItem();
                    items[index].type = 257;
                    items[index].path = f2.getPath();
                    items[index].parentPath = f2.getParent();
                    items[index].name = f2.getName();
                    items[index].date = AppDmUtil.getLocalDateString(AppDmUtil.javaDateToDocumentDate(f2.lastModified()));
                    items[index].size = AppFileUtil.formatFileSize(f2.length());
                }
                if (!this.stopped) {
                    this.handler.obtainMessage(11002, items).sendToTarget();
                } else {
                    return;
                }
            }
            File[] folders = file.listFiles(this.mFolderFilter);
            if (folders != null && folders.length != 0) {
                length = folders.length;
                while (i < length) {
                    f2 = folders[i];
                    if (!this.stopped) {
                        scanPDF(f2);
                        i++;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    HM_LocalTask() {
    }
}
