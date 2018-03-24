package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapCompleteListener;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapFailListener;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class DataSynchronizeEngineWorkThread extends Thread {
    private static final int MAX_UPLOAD_THREAD_COUNT = 3;
    private static final String TAG = "DataSynchronizeEngineWorkThread";
    private String mClientID;
    private Context mContext;
    private DataSynchronizeEngineDataBase mDataBase;
    protected ArrayList<DataSynchronizeInterface> mDataSynchronizeModules = new ArrayList();
    protected Integer mDownloadTickCount = Integer.valueOf(-1);
    protected boolean mNoNewData = false;
    protected StatusBarDisplayer mStatusBarDisplayer;
    protected boolean mUploading = false;
    private DataSynchronizeWorkRunnable mWorkRunnable;
    private Handler mWorkThreadHandler;

    private class DataSynchronizeWorkRunnable implements Runnable {
        private DataSynchronizeWorkRunnable() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            if (VirtualNetworkObject.getOfflineMode()) {
                DataSynchronizeEngineWorkThread.this.mWorkThreadHandler.postDelayed(DataSynchronizeEngineWorkThread.this.mWorkRunnable, 100);
            } else if (DataSynchronizeEngineWorkThread.this.mDataSynchronizeModules != null) {
                synchronized (DataSynchronizeEngineWorkThread.this.mDataSynchronizeModules) {
                    DataSynchronizeInterface Module;
                    DataSynchronizeEngineWorkThread.this.mUploading = false;
                    Iterator it = DataSynchronizeEngineWorkThread.this.mDataSynchronizeModules.iterator();
                    while (it.hasNext()) {
                        Module = (DataSynchronizeInterface) it.next();
                        if (Module.isUploadInProgress()) {
                            DataSynchronizeEngineWorkThread.this.mUploading = true;
                        }
                        if (!Module.isUploadInProgress()) {
                            if (Module.isDownloadInProgress()) {
                            }
                        }
                        DataSynchronizeEngineWorkThread.this.mWorkThreadHandler.postDelayed(DataSynchronizeEngineWorkThread.this.mWorkRunnable, 100);
                        return;
                    }
                    it = DataSynchronizeEngineWorkThread.this.mDataSynchronizeModules.iterator();
                    while (it.hasNext()) {
                        if (((DataSynchronizeInterface) it.next()).startUploadProcess()) {
                            DataSynchronizeEngineWorkThread.this.mWorkThreadHandler.postDelayed(DataSynchronizeEngineWorkThread.this.mWorkRunnable, 100);
                            return;
                        }
                    }
                    it = DataSynchronizeEngineWorkThread.this.mDataSynchronizeModules.iterator();
                    while (it.hasNext()) {
                        Module = (DataSynchronizeInterface) it.next();
                        if (Module.hasNewData()) {
                            Module.mLastCheckTime = System.currentTimeMillis();
                            Module.startDownloadProcess();
                            break;
                        } else if (System.currentTimeMillis() - Module.mLastCheckTime > 10000) {
                            boolean bAllowDownload = false;
                            synchronized (DataSynchronizeEngineWorkThread.this.mDownloadTickCount) {
                                if (DataSynchronizeEngineWorkThread.this.mDownloadTickCount.intValue() == -1) {
                                    bAllowDownload = true;
                                } else if (DataSynchronizeEngineWorkThread.this.mDownloadTickCount.intValue() > 0) {
                                    DataSynchronizeEngineWorkThread dataSynchronizeEngineWorkThread = DataSynchronizeEngineWorkThread.this;
                                    dataSynchronizeEngineWorkThread.mDownloadTickCount = Integer.valueOf(dataSynchronizeEngineWorkThread.mDownloadTickCount.intValue() - 1);
                                    if (DataSynchronizeEngineWorkThread.this.mDownloadTickCount.intValue() > 0) {
                                        bAllowDownload = true;
                                    }
                                    if (DataSynchronizeEngineWorkThread.this.mDownloadTickCount.intValue() == 0) {
                                        Log.w(DataSynchronizeEngineWorkThread.TAG, "Data download paused due to mDownloadTickCount is zero.");
                                    }
                                }
                            }
                            if (bAllowDownload) {
                                Module.mLastCheckTime = System.currentTimeMillis();
                                Module.startDownloadProcess();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    protected class BasicSynchronizeModule extends DataSynchronizeInterface {
        private int mDownloadWorkCount = 0;
        private LoadExamDataThread3 mGetThread = null;
        private boolean mHasNewData = false;
        private LoadExamDataThread3 mPutThread = null;
        private StatusBarDisplayer mStatusBarDisplayer;
        private int mUploadWorkCount = 0;
        private boolean mbFullDataDownloaded = false;

        protected BasicSynchronizeModule() {
        }

        public void setDisplayer(StatusBarDisplayer Displayer) {
            this.mStatusBarDisplayer = Displayer;
        }

        public boolean isUploadInProgress() {
            return this.mUploadWorkCount > 0;
        }

        public boolean isDownloadInProgress() {
            return this.mDownloadWorkCount > 0;
        }

        public void resetFullDownLoadFlag() {
            this.mbFullDataDownloaded = false;
        }

        public boolean startUploadProcess() {
            ArrayList<String> arrPackageIDs = new ArrayList();
            ArrayList<String> arrClientIDs = new ArrayList();
            if (!DataSynchronizeEngineWorkThread.this.mDataBase.getSynchronizePackage(arrPackageIDs, arrClientIDs)) {
                return false;
            }
            this.mStatusBarDisplayer.setTitle("数据同步");
            this.mStatusBarDisplayer.setText("正在上传数据...");
            this.mStatusBarDisplayer.showProgressBox(null);
            for (int i = 0; i < Math.min(arrPackageIDs.size(), 3); i++) {
                final String szPackageID = (String) arrPackageIDs.get(i);
                final Object szClientID = (String) arrClientIDs.get(i);
                Object szOneXML = DataSynchronizeEngineWorkThread.this.mDataBase.generateSynchronizeXML(szPackageID, szClientID);
                this.mStatusBarDisplayer.smartRemainProgressHandle(arrPackageIDs.size());
                LoadExamDataThread3 SubmitDataThread = new LoadExamDataThread3(DataSynchronizeEngineWorkThread.this.mContext, "DataSynchronizePut", new OnSoapCompleteListener() {
                    public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                        BasicSynchronizeModule basicSynchronizeModule = BasicSynchronizeModule.this;
                        basicSynchronizeModule.mUploadWorkCount = basicSynchronizeModule.mUploadWorkCount - 1;
                        if (BasicSynchronizeModule.this.mUploadWorkCount == 0) {
                            BasicSynchronizeModule.this.mStatusBarDisplayer.smartRemainProgressHandle(0);
                        }
                        DataSynchronizeEngineWorkThread.this.mDataBase.deleteSynchronizePackage(szPackageID, szClientID);
                        BasicSynchronizeModule.this.mPutThread = null;
                    }
                });
                SubmitDataThread.setOnSoapFailListener(new OnSoapFailListener() {
                    public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                        BasicSynchronizeModule basicSynchronizeModule = BasicSynchronizeModule.this;
                        basicSynchronizeModule.mUploadWorkCount = basicSynchronizeModule.mUploadWorkCount - 1;
                        if (BasicSynchronizeModule.this.mUploadWorkCount == 0) {
                            BasicSynchronizeModule.this.mStatusBarDisplayer.smartRemainProgressHandle(0);
                        }
                        BasicSynchronizeModule.this.mPutThread = null;
                        return false;
                    }
                });
                SubmitDataThread.addParam("lpszClientID", szClientID);
                SubmitDataThread.addParam("szInputOutputXML", szOneXML);
                SubmitDataThread.start();
                this.mPutThread = SubmitDataThread;
                this.mUploadWorkCount++;
            }
            return true;
        }

        public boolean startDownloadProcess() {
            if (!this.mbFullDataDownloaded) {
                LoadExamDataThread3 DownloadDataThread = new LoadExamDataThread3(DataSynchronizeEngineWorkThread.this.mContext, "DataSynchronizeGet", new OnSoapCompleteListener() {
                    public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                        BasicSynchronizeModule.this.mGetThread = null;
                        String szXML = ThreadObject.getStringParam(arrSoapData, 0);
                        if (szXML.length() > 100) {
                            BasicSynchronizeModule.this.mStatusBarDisplayer.setTitle("数据同步");
                            BasicSynchronizeModule.this.mStatusBarDisplayer.setText("正在从服务器端下载数据...");
                            BasicSynchronizeModule.this.mStatusBarDisplayer.showProgressBox(null);
                        }
                        if (DataSynchronizeEngineWorkThread.this.mDataBase.handleSynchronizeXML(szXML, false, BasicSynchronizeModule.this.mStatusBarDisplayer) == 0) {
                            BasicSynchronizeModule.this.mStatusBarDisplayer.hideProgressBox();
                            DataSynchronizeEngineWorkThread.this.mNoNewData = true;
                        } else {
                            DataSynchronizeEngineWorkThread.this.mNoNewData = false;
                        }
                        BasicSynchronizeModule.this.mDownloadWorkCount = 0;
                    }
                });
                DownloadDataThread.setOnSoapFailListener(new OnSoapFailListener() {
                    public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                        BasicSynchronizeModule.this.mGetThread = null;
                        BasicSynchronizeModule.this.mDownloadWorkCount = 0;
                        return false;
                    }
                });
                DownloadDataThread.addParam("lpszClientID", DataSynchronizeEngineWorkThread.this.mClientID);
                DownloadDataThread.addParam("szInputOutputXML", DataSynchronizeEngineWorkThread.this.mDataBase.generateSynchronizeXML(null, DataSynchronizeEngineWorkThread.this.mClientID));
                DownloadDataThread.start();
                this.mGetThread = DownloadDataThread;
                this.mDownloadWorkCount = 1;
                this.mbFullDataDownloaded = true;
            }
            return true;
        }

        public boolean hasNewData() {
            return this.mHasNewData;
        }

        public void stopAllProcess() {
            if (this.mGetThread != null) {
                this.mGetThread.setCancel(true);
            }
            if (this.mPutThread != null) {
                this.mPutThread.setCancel(true);
            }
        }
    }

    public DataSynchronizeEngineWorkThread(Context Context, String szClientID) {
        this.mDataBase = new DataSynchronizeEngineDataBase(Context);
        this.mContext = Context;
        this.mClientID = szClientID;
        this.mWorkRunnable = new DataSynchronizeWorkRunnable();
        this.mStatusBarDisplayer = new StatusBarDisplayer(Context);
        this.mStatusBarDisplayer.setIcon(UI.mSynchronizeIcon);
        registerModule(new BasicSynchronizeModule());
    }

    public void registerModule(DataSynchronizeInterface Module) {
        synchronized (this.mDataSynchronizeModules) {
            this.mDataSynchronizeModules.add(Module);
        }
        Module.setDisplayer(this.mStatusBarDisplayer);
    }

    public void resetTimer() {
        synchronized (this.mDataSynchronizeModules) {
            Iterator it = this.mDataSynchronizeModules.iterator();
            while (it.hasNext()) {
                ((DataSynchronizeInterface) it.next()).mLastCheckTime = 0;
            }
            this.mDownloadTickCount = Integer.valueOf(-1);
        }
    }

    public void resetFullDownLoadFlag() {
        synchronized (this.mDataSynchronizeModules) {
            Iterator it = this.mDataSynchronizeModules.iterator();
            while (it.hasNext()) {
                ((DataSynchronizeInterface) it.next()).resetFullDownLoadFlag();
            }
        }
    }

    public boolean isUploading() {
        return this.mUploading;
    }

    public void setDownloadTickCount(int nTickCount) {
        synchronized (this.mDownloadTickCount) {
            if (nTickCount != -1) {
                this.mDownloadTickCount = Integer.valueOf(this.mDataSynchronizeModules.size() * nTickCount);
            } else {
                this.mDownloadTickCount = Integer.valueOf(-1);
            }
        }
    }

    public void stopThread() {
        if (this.mWorkThreadHandler != null) {
            this.mWorkThreadHandler.removeCallbacksAndMessages(null);
            synchronized (this.mDataSynchronizeModules) {
                Iterator it = this.mDataSynchronizeModules.iterator();
                while (it.hasNext()) {
                    ((DataSynchronizeInterface) it.next()).stopAllProcess();
                }
                this.mDataSynchronizeModules.clear();
            }
            Log.d(TAG, "stopThread called. All message in queue removed.");
            Utilities.putACRAData(getName() + " stopThread called. All message in queue removed.");
            this.mWorkThreadHandler.post(new Runnable() {
                public void run() {
                    Looper.myLooper().quit();
                }
            });
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.mStatusBarDisplayer.shutDown();
        this.mWorkThreadHandler = null;
    }

    public void run() {
        setName("DataSynchronizeEngine WorkThread");
        super.run();
        Looper.prepare();
        this.mWorkThreadHandler = new Handler();
        this.mWorkThreadHandler.postDelayed(this.mWorkRunnable, 100);
        Looper.loop();
        Log.d(TAG, "DataSynchronizeEngine WorkThread exit.");
    }
}
