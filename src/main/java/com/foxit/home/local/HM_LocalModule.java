package com.foxit.home.local;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import com.foxit.app.App;
import com.foxit.app.thread.AppAsyncTask;
import com.foxit.home.IHM_HomeModule;
import com.foxit.home.R;
import com.foxit.home.view.HM_PathView;
import com.foxit.home.view.HM_PathView.pathChangedListener;
import com.foxit.read.RD_ReadActivity;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.controls.filebrowser.FileBrowser;
import com.foxit.uiextensions.controls.filebrowser.FileDelegate;
import com.foxit.uiextensions.controls.filebrowser.imp.FileBrowserImpl;
import com.foxit.uiextensions.controls.filebrowser.imp.FileItem;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.TopBarImpl;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppStorageManager;
import com.foxit.uiextensions.utils.AppUtil;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HM_LocalModule implements Module, IHM_HomeModule {
    protected static final int MSG_PDFs_STOP = 11008;
    protected static final int MSG_UPDATE_PDFs = 11002;
    public static final int STATE_ALL_PDF = 1;
    public static final int STATE_NORMAL = 0;
    private boolean isSortUp = true;
    private final Context mContext;
    private String mCurrentPath;
    private int mCurrentState = 1;
    private BaseItemImpl mDocumentItem;
    private FileBrowser mFileBrowser;
    private FileDelegate mFileBrowserDelegate = new FileDelegate() {
        public List<FileItem> getDataSource() {
            return HM_LocalModule.this.mFileItems;
        }

        public void onPathChanged(String path) {
            if (HM_LocalModule.this.mCurrentState == 0) {
                File f;
                FileItem item;
                File[] fs;
                if (AppUtil.isEmpty(path)) {
                    HM_LocalModule.this.mPathView.setPath(null);
                    HM_LocalModule.this.mLocalView.setTopLayoutVisible(false);
                    HM_LocalModule.this.mFileItems.clear();
                    for (String p : AppStorageManager.getInstance(HM_LocalModule.this.mContext).getVolumePaths()) {
                        f = new File(p);
                        item = new FileItem();
                        item.parentPath = path;
                        item.path = f.getPath();
                        item.name = f.getName();
                        item.date = AppDmUtil.getLocalDateString(AppDmUtil.javaDateToDocumentDate(f.lastModified()));
                        item.lastModifyTime = f.lastModified();
                        item.type = 0;
                        fs = f.listFiles(new FileFilter() {
                            public boolean accept(File pathname) {
                                if (pathname.isHidden() || !pathname.canRead()) {
                                    return false;
                                }
                                return true;
                            }
                        });
                        if (fs != null) {
                            item.fileCount = fs.length;
                        } else {
                            item.fileCount = 0;
                        }
                        HM_LocalModule.this.mFileItems.add(item);
                    }
                    return;
                }
                File file = new File(path);
                if (file.exists()) {
                    try {
                        File[] files = file.listFiles(HM_LocalModule.this.mFileFilter);
                        HM_LocalModule.this.mCurrentPath = path;
                        HM_LocalModule.this.mLocalView.setTopLayoutVisible(true);
                        HM_LocalModule.this.mFileItems.clear();
                        HM_LocalModule.this.mPathView.setPath(HM_LocalModule.this.mCurrentPath);
                        if (files != null) {
                            for (File f2 : files) {
                                item = new FileItem();
                                item.parentPath = file.getPath();
                                item.path = f2.getPath();
                                item.name = f2.getName();
                                item.date = AppDmUtil.getLocalDateString(AppDmUtil.javaDateToDocumentDate(f2.lastModified()));
                                item.lastModifyTime = f2.lastModified();
                                if (f2.isDirectory()) {
                                    item.type = 16;
                                    fs = f2.listFiles(new FileFilter() {
                                        public boolean accept(File pathname) {
                                            if (pathname.isHidden() || !pathname.canRead()) {
                                                return false;
                                            }
                                            if (pathname.isDirectory()) {
                                                return true;
                                            }
                                            if (pathname.isFile() && pathname.getName().toLowerCase().endsWith(".pdf")) {
                                                return true;
                                            }
                                            return false;
                                        }
                                    });
                                    if (fs != null) {
                                        item.fileCount = fs.length;
                                    } else {
                                        item.fileCount = 0;
                                    }
                                    HM_LocalModule.this.mFileItems.add(item);
                                } else {
                                    item.type = 1;
                                    item.size = AppFileUtil.formatFileSize(f2.length());
                                    item.length = f2.length();
                                    HM_LocalModule.this.mFileItems.add(item);
                                }
                            }
                            Collections.sort(HM_LocalModule.this.mFileItems, HM_LocalModule.this.mFileBrowser.getComparator());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void onItemClicked(View view, FileItem item) {
            if (item.type == 16 || item.type == 0) {
                HM_LocalModule.this.mFileBrowser.setPath(item.path);
            } else if ((item.type & 1) != 0) {
                Intent intent = new Intent();
                intent.putExtra("filePath", item.path);
                intent.setClass(HM_LocalModule.this.mContext, RD_ReadActivity.class);
                HM_LocalModule.this.mContext.startActivity(intent);
            }
        }

        public void onItemsCheckedChanged(boolean isAllSelected, int folderCount, int fileCount) {
        }
    };
    private FileFilter mFileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            if (pathname.isHidden() || !pathname.canRead()) {
                return false;
            }
            if (HM_LocalModule.this.mCurrentState == 0 && pathname.isDirectory()) {
                return true;
            }
            String name = pathname.getName().toLowerCase();
            if (pathname.isFile() && name.endsWith(".pdf")) {
                return true;
            }
            return false;
        }
    };
    private final List<FileItem> mFileItems = new ArrayList();
    private FileObserver mFileObserver;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HM_LocalModule.MSG_UPDATE_PDFs /*11002*/:
                    if (msg.obj instanceof FileItem[]) {
                        FileItem[] items = msg.obj;
                        if (HM_LocalModule.this.mCurrentState == 1) {
                            Collections.addAll(HM_LocalModule.this.mFileItems, items);
                        }
                        HM_LocalModule.this.mFileBrowser.updateDataSource(true);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private HM_LocalView mLocalView;
    private HM_PathView mPathView;
    private int mSortMode = 1;
    private BaseBar mTopBar;

    class SDCardFileObserver extends FileObserver {
        public SDCardFileObserver(String path, int mask) {
            super(path, mask);
        }

        public SDCardFileObserver(String path) {
            super(path);
        }

        public void onEvent(int event, String path) {
            switch (event & 4095) {
                case 2:
                case 512:
                    ((Activity) HM_LocalModule.this.mContext).runOnUiThread(new Runnable() {
                        public void run() {
                            HM_LocalModule.this.mFileItems.clear();
                            AllPDFs.start(HM_LocalModule.this.mContext, HM_LocalModule.this.mHandler);
                            HM_LocalModule.this.mFileBrowser.updateDataSource(false);
                        }
                    });
                    return;
                default:
                    return;
            }
        }
    }

    class CopyAsy extends AppAsyncTask {
        CopyAsy() {
        }

        public String doInBackground(Object... params) {
            if (Environment.getExternalStorageState().equals("mounted")) {
                if (HM_LocalModule.this.mergeFiles(new File[]{new File(params[0] + File.separator + "Sample.pdf"), new File(params[0] + File.separator + "complete_pdf_viewer_guide_android.pdf")}, new String[]{"Sample.pdf", "complete_pdf_viewer_guide_android.pdf"})) {
                    return params[0] + File.separator + "Sample.pdf";
                }
            }
            return null;
        }

        public void onPostExecute(Object result) {
            if (result != null) {
                HM_LocalModule.this.mFileBrowser.setPath(HM_LocalModule.this.mCurrentPath);
                HM_LocalModule.this.mFileBrowser.updateDataSource(true);
            }
        }
    }

    public String getName() {
        return "HOME_MODULE_LOCAL";
    }

    public HM_LocalModule(Context context) {
        this.mContext = context;
        this.mFileObserver = new SDCardFileObserver(Environment.getExternalStorageDirectory().getPath());
    }

    public boolean loadModule() {
        this.mFileObserver.startWatching();
        return true;
    }

    public boolean unloadModule() {
        this.mFileObserver.stopWatching();
        return true;
    }

    public String getTag() {
        return IHM_HomeModule.HOME_MODULE_TAG_LOCAL;
    }

    public void loadHomeModule(Context context) {
        initItems(context);
        if (this.mTopBar == null) {
            this.mTopBar = new TopBarImpl(context);
            this.mTopBar.setBackgroundColor(context.getResources().getColor(R.color.ux_text_color_subhead_colour));
        }
        if (this.mLocalView == null) {
            this.mLocalView = new HM_LocalView(context);
            this.mPathView = new HM_PathView(context);
            this.mFileBrowser = new FileBrowserImpl(context, this.mFileBrowserDelegate);
            this.mLocalView.addFileView(this.mFileBrowser.getContentView());
            this.mPathView.setPathChangedListener(new pathChangedListener() {
                public void onPathChanged(String newPath) {
                    HM_LocalModule.this.mFileBrowser.setPath(newPath);
                }
            });
        }
        if (AppFileUtil.isSDAvailable()) {
            CopyAsy task;
            File file = new File(AppFileUtil.getSDPath() + File.separator + "FoxitSDK");
            if (!file.exists()) {
                file.mkdirs();
            }
            if (file.exists()) {
                this.mCurrentPath = file.getPath();
            } else {
                this.mCurrentPath = AppFileUtil.getSDPath();
            }
            this.mPathView.setPath(this.mCurrentPath);
            this.mFileBrowser.setPath(this.mCurrentPath);
            if (!new File(file.getPath() + File.separator + "Sample.pdf").exists()) {
                task = new CopyAsy();
                App.instance().getThreadManager().startThread(task, file.getPath());
            }
            if (!new File(file.getPath() + File.separator + "complete_pdf_viewer_guide_android.pdf").exists()) {
                task = new CopyAsy();
                App.instance().getThreadManager().startThread(task, file.getPath());
            }
        }
        resetSortMode();
        switchState(0);
    }

    public void unloadHomeModule(Context context) {
    }

    public View getTopToolbar(Context context) {
        return this.mTopBar.getContentView();
    }

    public View getContentView(Context context) {
        return this.mLocalView;
    }

    public boolean isNewVersion() {
        return false;
    }

    private void setStateAllPDFs() {
        this.mTopBar.removeAllItems();
        this.mTopBar.addView(this.mDocumentItem, TB_Position.Position_LT);
        this.mLocalView.removeAllTopView();
        this.mLocalView.setTopLayoutVisible(false);
        this.mLocalView.setBottomLayoutVisible(false);
    }

    private void setStateNormal() {
        this.mTopBar.removeAllItems();
        this.mTopBar.addView(this.mDocumentItem, TB_Position.Position_LT);
        this.mLocalView.removeAllTopView();
        this.mLocalView.setTopLayoutVisible(!AppUtil.isEmpty(this.mFileBrowser.getDisplayPath()));
        this.mLocalView.addPathView(this.mPathView.getContentView());
        this.mLocalView.setBottomLayoutVisible(false);
    }

    public void onActivated() {
        if (this.mCurrentState == 1) {
            setStateAllPDFs();
            AllPDFs.stop();
            this.mFileItems.clear();
            AllPDFs.start(this.mContext, this.mHandler);
            this.mFileBrowser.updateDataSource(true);
        }
    }

    public void onDeactivated() {
    }

    public boolean onWillDestroy() {
        return false;
    }

    private void switchState(int state) {
        if (this.mCurrentState != state) {
            if (this.mCurrentState == 1) {
                AllPDFs.stop();
            }
            if (state == 0) {
                this.mCurrentState = state;
                setStateNormal();
                this.mFileBrowser.setPath(this.mFileBrowser.getDisplayPath());
            } else if (state == 1) {
                this.mCurrentState = state;
                setStateAllPDFs();
                this.mFileItems.clear();
                AllPDFs.start(this.mContext, this.mHandler);
                this.mFileBrowser.updateDataSource(true);
            }
        }
    }

    private void resetSortMode() {
        if (this.mSortMode == 0) {
            if (this.isSortUp) {
                this.mFileBrowser.getComparator().setOrderBy(2);
            } else {
                this.mFileBrowser.getComparator().setOrderBy(3);
            }
        } else if (this.mSortMode == 1) {
            if (this.isSortUp) {
                this.mFileBrowser.getComparator().setOrderBy(0);
            } else {
                this.mFileBrowser.getComparator().setOrderBy(1);
            }
        } else if (this.mSortMode == 2) {
            if (this.isSortUp) {
                this.mFileBrowser.getComparator().setOrderBy(4);
            } else {
                this.mFileBrowser.getComparator().setOrderBy(5);
            }
        }
        if (!this.mFileItems.isEmpty()) {
            Collections.sort(this.mFileItems, this.mFileBrowser.getComparator());
            this.mFileBrowser.updateDataSource(true);
        }
    }

    private void initItems(Context context) {
        this.mDocumentItem = new BaseItemImpl(context);
        this.mDocumentItem.setText(R.string.hm_document);
        this.mDocumentItem.setTextSize(App.instance().getDisplay().px2dp((float) App.instance().getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.ux_text_height_title)));
        this.mDocumentItem.setTextColor(context.getResources().getColor(R.color.ux_color_white));
    }

    private boolean mergeFiles(File[] outFile, String[] files) {
        IOException e;
        Throwable th;
        boolean success = false;
        for (int i = 0; i < outFile.length; i++) {
            OutputStream os = null;
            try {
                OutputStream os2 = new FileOutputStream(outFile[i]);
                try {
                    byte[] buffer = new byte[8192];
                    InputStream is = App.instance().getApplicationContext().getAssets().open(files[i]);
                    for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                        os2.write(buffer, 0, len);
                    }
                    is.close();
                    if (os2 != null) {
                        try {
                            os2.flush();
                            os2.close();
                            success = true;
                            os = os2;
                        } catch (IOException e2) {
                            os = os2;
                        }
                    }
                } catch (IOException e3) {
                    e = e3;
                    os = os2;
                } catch (Throwable th2) {
                    th = th2;
                    os = os2;
                }
            } catch (IOException e4) {
                e = e4;
                try {
                    e.printStackTrace();
                    if (os != null) {
                        try {
                            os.flush();
                            os.close();
                            success = true;
                        } catch (IOException e5) {
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        }
        return success;
        if (os != null) {
            try {
                os.flush();
                os.close();
            } catch (IOException e6) {
            }
        }
        throw th;
        throw th;
    }
}
