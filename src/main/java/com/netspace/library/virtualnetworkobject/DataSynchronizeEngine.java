package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import android.util.Log;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapCompleteListener;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapFailListener;
import java.util.Vector;

public class DataSynchronizeEngine extends Engine {
    private static final String TAG = "DataSynchronizeEngine";
    private static String mClientID;
    private DataSynchronizeEngineDataBase mDataBase;
    private String mEngineName = TAG;
    private boolean mNoAutoSaveOperation = false;
    private DataSynchronizeEngineWorkThread mWorkThread;

    public DataSynchronizeEngine(Context Context) {
        super(Context);
        this.mWorkThread = new DataSynchronizeEngineWorkThread(Context, mClientID);
        this.mDataBase = new DataSynchronizeEngineDataBase(Context);
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public static void setClientID(String szClientID) {
        mClientID = szClientID;
    }

    public boolean isUploading() {
        return this.mWorkThread.isUploading();
    }

    public void startEngine() {
        super.startEngine();
        this.mWorkThread.start();
    }

    public void stopEngine() {
        super.stopEngine();
        this.mWorkThread.stopThread();
    }

    public void resetTimer() {
        this.mWorkThread.resetTimer();
    }

    public void resetFullDownLoadFlag() {
        this.mWorkThread.resetFullDownLoadFlag();
    }

    public void setDownloadTickCount(int nTickCount) {
        this.mWorkThread.setDownloadTickCount(nTickCount);
    }

    public void registerModule(DataSynchronizeInterface Module) {
        this.mWorkThread.registerModule(Module);
    }

    public void setNoAutoSave(boolean bNoSave) {
        this.mNoAutoSaveOperation = bNoSave;
    }

    public void deletePackage(String szPackageID, String szClientID) {
        if (this.mDataBase.deletePackage(szPackageID, szClientID)) {
            this.mDataBase.addPackageToSynchronizeList(szPackageID, szClientID);
        }
    }

    public boolean hasPackage(String szPackageID, String szClientID) {
        return this.mDataBase.hasPackage(szPackageID, szClientID);
    }

    public boolean handlePackageRead(final ItemObject OneObject) {
        final Object szPackageID = OneObject.getObjectURI();
        DataSynchronizeItemObject OneObj = (DataSynchronizeItemObject) OneObject;
        String szTempClientID = mClientID;
        if (OneObject.getParam("ClientID") != null) {
            szTempClientID = (String) OneObject.getParam("ClientID");
        }
        final Object szClientID = szTempClientID;
        if (szClientID == null || szClientID.isEmpty()) {
            throw new IllegalArgumentException("ClientID can not be empty or null. Either set client in DataSynchronizeEngine or set ClientID in package.");
        }
        if (!VirtualNetworkObject.getOfflineMode()) {
            final long nNetworkStartTime = System.currentTimeMillis();
            final ItemObject itemObject = OneObject;
            LoadExamDataThread3 GetDataThread = new LoadExamDataThread3(this.mContext, "DataSynchronizeGetSingleData", new OnSoapCompleteListener() {
                public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                    Log.d(DataSynchronizeEngine.TAG, "DataSynchronizeGetSingleData cost " + String.valueOf(System.currentTimeMillis() - nNetworkStartTime) + " ms.");
                    String szTextContent = ThreadObject.getStringParam(arrSoapData, 0);
                    if (szTextContent != null) {
                        DataSynchronizeItemObject OneObj = itemObject;
                        if (!(DataSynchronizeEngine.this.mNoAutoSaveOperation || OneObj.isNoSave())) {
                            DataSynchronizeEngine.this.mDataBase.addPackage(szPackageID, szClientID, szTextContent, true);
                        }
                    }
                    itemObject.writeTextData(szTextContent);
                    itemObject.callCallbacks(true, 0);
                }
            });
            GetDataThread.setOnSoapFailListener(new OnSoapFailListener() {
                public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                    OneObject.mReturnMessage = ThreadObject.getErrorDescription();
                    OneObject.callCallbacks(false, nReturnCode);
                    return false;
                }
            });
            GetDataThread.setAllowReturnCode(ErrorCode.ERROR_NO_DATA);
            GetDataThread.addParam("lpszClientID", szClientID);
            GetDataThread.addParam("lpszPackageID", szPackageID);
            GetDataThread.start();
        } else if (this.mDataBase.hasPackage(szPackageID, szClientID)) {
            OneObject.writeTextData(this.mDataBase.getPackageContent(szPackageID, szClientID));
            OneObject.callCallbacks(true, 0);
        } else {
            Log.e(TAG, "Package " + szPackageID + " for client " + szClientID + " is not found in local database. Return null.");
            OneObject.writeTextData(null);
            OneObject.callCallbacks(true, 0);
        }
        return true;
    }

    public boolean handlePackageWrite(ItemObject OneObject) {
        String szPackageID = OneObject.getObjectURI();
        String szTempClientID = mClientID;
        if (OneObject.getParam("ClientID") != null) {
            szTempClientID = (String) OneObject.getParam("ClientID");
        }
        String szClientID = szTempClientID;
        if (szClientID == null || szClientID.isEmpty()) {
            throw new IllegalArgumentException("ClientID can not be empty or null. Either set client in DataSynchronizeEngine or set ClientID in package.");
        }
        if (OneObject.readTextData() != null) {
            this.mDataBase.addPackage(szPackageID, szClientID, OneObject.readTextData(), false);
            this.mDataBase.addPackageToSynchronizeList(szPackageID, szClientID);
        }
        OneObject.callCallbacks(true, 0);
        return true;
    }
}
