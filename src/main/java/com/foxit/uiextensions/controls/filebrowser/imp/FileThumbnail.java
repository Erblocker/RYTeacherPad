package com.foxit.uiextensions.controls.filebrowser.imp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.Renderer;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppStorageManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class FileThumbnail {
    private static final int MSG_EXECUTE = 1;
    private static FileThumbnail mFileThumbnail = null;
    private Context mContext;
    private Set<String> mErrorSet = new HashSet(10);
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                FileThumbnail.this.mRunning = false;
                FileThumbnail.this.executeTask();
            }
        }
    };
    private boolean mRunning;
    private Cache mThumbnailCache = new Cache();
    private Map<String, WeakReference<Bitmap>> mThumbnailMap = new ConcurrentHashMap();
    private Vector<ThumbnailTask> mThumbnailTasks = new Vector();

    class Cache {
        private static final String CACHE_DIR = "FMThumbnailCache";
        private static final int CACHE_SIZE = 2097152;
        private static final String SUFFIX = ".cache";

        Cache() {
        }

        public Bitmap getThumbnail(String filePath) {
            if (filePath == null || filePath.equals("")) {
                return null;
            }
            File cacheFile = new File(getCacheDir(), convertPathToFileName(filePath));
            if (cacheFile.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(cacheFile.getPath());
                if (bmp == null) {
                    cacheFile.delete();
                } else {
                    updateFileTime(cacheFile);
                    return bmp;
                }
            }
            return null;
        }

        public void saveThumbnail(Bitmap bitmap, String filePath) {
            if (bitmap != null && filePath != null && !filePath.equals("")) {
                File dir = getCacheDir();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (AppFileUtil.getFolderSize(dir.getPath()) > 2097152) {
                    removeCache(dir);
                }
                File file = new File(dir, convertPathToFileName(filePath));
                try {
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean removeCache(File dir) {
            File[] files = dir.listFiles();
            if (files != null && AppFileUtil.getFolderSize(dir.getPath()) > 2097152) {
                int factor = (int) ((0.4d * ((double) files.length)) + 1.0d);
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File lhs, File rhs) {
                        if (lhs.lastModified() > rhs.lastModified()) {
                            return 1;
                        }
                        if (lhs.lastModified() < rhs.lastModified()) {
                            return -1;
                        }
                        return 0;
                    }
                });
                for (int i = 0; i < factor; i++) {
                    if (files[i].getName().contains(SUFFIX)) {
                        files[i].delete();
                    }
                }
            }
            return true;
        }

        private void updateFileTime(File file) {
            file.setLastModified(System.currentTimeMillis());
        }

        private String convertPathToFileName(String filePath) {
            return filePath.replace(File.separator, "") + SUFFIX;
        }

        private File getCacheDir() {
            return new File(AppStorageManager.getInstance(FileThumbnail.this.mContext).getCacheDir(), CACHE_DIR);
        }

        public void removeFile(String filePath) {
            File delFile = new File(getCacheDir(), convertPathToFileName(filePath));
            if (delFile.exists()) {
                delFile.delete();
            }
        }
    }

    public interface ThumbnailCallback {
        void result(boolean z, String str);
    }

    class ThumbnailTask implements Runnable {
        private ThumbnailCallback mCallback;
        private String mFilePath;

        public ThumbnailTask(String filePath, ThumbnailCallback callback) {
            this.mFilePath = filePath;
            this.mCallback = callback;
        }

        public void run() {
            Bitmap bitmap = null;
            boolean succeed = false;
            try {
                bitmap = Bitmap.createBitmap(FileThumbnail.this.dip2px(38), FileThumbnail.this.dip2px(44), Config.ARGB_8888);
                if (drawPageEx(this.mFilePath, 0, bitmap, new Point(0, 0), new Point(FileThumbnail.this.dip2px(38), FileThumbnail.this.dip2px(44)), 0) == 0) {
                    succeed = true;
                } else {
                    succeed = false;
                }
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
            if (succeed) {
                FileThumbnail.this.mThumbnailCache.saveThumbnail(bitmap, this.mFilePath);
                FileThumbnail.this.mThumbnailMap.put(this.mFilePath, new WeakReference(bitmap));
            } else {
                FileThumbnail.this.mErrorSet.add(this.mFilePath);
                FileThumbnail.this.mThumbnailCache.removeFile(this.mFilePath);
                FileThumbnail.this.mThumbnailMap.remove(this.mFilePath);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
            if (this.mCallback != null) {
                this.mCallback.result(succeed, this.mFilePath);
            }
            FileThumbnail.this.mHandler.sendEmptyMessage(1);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof ThumbnailTask)) {
                return false;
            }
            if (((ThumbnailTask) o).mFilePath == null || ((ThumbnailTask) o).mCallback == null) {
                return false;
            }
            if (((ThumbnailTask) o).mCallback != this.mCallback) {
                return false;
            }
            return ((ThumbnailTask) o).mFilePath.equalsIgnoreCase(this.mFilePath);
        }

        private synchronized int drawPageEx(String filePath, int pageIndex, Bitmap bitmap, Point position, Point viewSize, int rotate) {
            int i;
            synchronized (AppFileUtil.getInstance().isOOMHappened) {
                if (AppFileUtil.getInstance().isOOMHappened.booleanValue()) {
                    i = -1;
                } else {
                    PDFDoc pdfDoc = PDFDoc.createFromFilePath(filePath);
                    pdfDoc.load(null);
                    if (pageIndex < 0 || pageIndex >= pdfDoc.getPageCount()) {
                        i = -1;
                    } else {
                        int ret;
                        PDFPage page = pdfDoc.getPage(pageIndex);
                        if (!page.isParsed()) {
                            ret = page.startParse(0, null, false);
                            while (ret == 1) {
                                try {
                                    ret = page.continueParse();
                                } catch (PDFException e) {
                                    i = -1;
                                }
                            }
                        }
                        Matrix matrix = page.getDisplayMatrix(-position.x, -position.y, viewSize.x, viewSize.y, rotate);
                        if (page.hasTransparency()) {
                            bitmap.eraseColor(0);
                        } else {
                            bitmap.eraseColor(-1);
                        }
                        Renderer renderer = Renderer.create(bitmap);
                        for (ret = renderer.startRender(page, matrix, null); ret == 1; ret = renderer.continueRender()) {
                        }
                        renderer.release();
                        pdfDoc.closePage(pageIndex);
                        pdfDoc.release();
                        i = 0;
                    }
                }
            }
            return i;
        }
    }

    private FileThumbnail(Context context) {
        this.mContext = context;
    }

    public static boolean isSupportThumbnail(String pathname) {
        if (pathname.toLowerCase().endsWith("pdf")) {
            return true;
        }
        return false;
    }

    public synchronized Bitmap getThumbnail(String filePath, ThumbnailCallback callback) {
        Bitmap bitmap = null;
        synchronized (this) {
            if (filePath != null) {
                if (filePath.length() != 0 && isSupportThumbnail(filePath)) {
                    if (!this.mErrorSet.contains(filePath)) {
                        WeakReference<Bitmap> reference = (WeakReference) this.mThumbnailMap.get(filePath);
                        bitmap = null;
                        if (reference != null) {
                            bitmap = (Bitmap) reference.get();
                        }
                        if (bitmap == null) {
                            bitmap = this.mThumbnailCache.getThumbnail(filePath);
                            if (bitmap != null) {
                                this.mThumbnailMap.put(filePath, new WeakReference(bitmap));
                            } else {
                                addTask(filePath, callback);
                                executeTask();
                            }
                        }
                    }
                }
            }
        }
        return bitmap;
    }

    private void addTask(String filePath, ThumbnailCallback callback) {
        ThumbnailTask task = new ThumbnailTask(filePath, callback);
        if (!this.mThumbnailTasks.contains(task)) {
            this.mThumbnailTasks.add(task);
        }
    }

    public synchronized void executeTask() {
        if (!this.mRunning) {
            if (this.mThumbnailTasks.size() > 0) {
                this.mRunning = true;
                ThumbnailTask task = (ThumbnailTask) this.mThumbnailTasks.remove(0);
                if (task != null) {
                    new Thread(task).start();
                }
            }
        }
    }

    private int dip2px(int dip) {
        return AppDisplay.getInstance(this.mContext).dp2px((float) dip);
    }

    public static FileThumbnail getInstance(Context context) {
        if (mFileThumbnail == null) {
            mFileThumbnail = new FileThumbnail(context);
        }
        return mFileThumbnail;
    }
}
