package com.netspace.library.virtualnetworkobject.helper;

import android.database.Cursor;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.database.DaoSession;
import com.netspace.library.database.RESTSynchronizePackage;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.interfaces.RESTBasicData;
import com.netspace.library.struct.CRestDataBase;
import com.netspace.library.struct.RESTSynchronizeComplete;
import com.netspace.library.struct.RESTSynchronizeError;
import com.netspace.library.struct.RESTSynchronizeReport;
import com.netspace.library.utilities.SimpleBackgroundTask;
import com.netspace.library.utilities.SimpleBackgroundTask.BackgroundTaskExecuteCallBack;
import com.netspace.library.utilities.Utilities;
import com.xsj.crasheye.CrasheyeFileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.WhereCondition;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RESTDataEngineHelper<S, D> {
    protected static final String TAG = "RESTDataEngineHelper";
    private static final boolean VERBOSE = false;
    private WhereCondition mAllDataWhere1;
    private WhereCondition[] mAllDataWheres;
    private Class<D> mClassForDataItem;
    private DaoSession mDaoSession;
    private Method mGetItemMethod;
    private Method mGetItemsMethod;
    private Property mGuidProperty;
    private Method mPutItemMethod;
    private Method mPutItemsMethod;
    private S mService;
    private RESTSynchronizeComplete mSynchronizeComplete = new RESTSynchronizeComplete();
    private Method mSynchronizeDataMethod;
    private RESTSynchronizeError mSynchronizeError = new RESTSynchronizeError();
    private SimpleBackgroundTask mSynchronizeTask;
    private boolean mbEventSaveToLocalOnly = false;
    private boolean mbReadOnly = false;
    private boolean mbSynchronizeComplete = false;
    private String mszField;
    private String mszOverrideFieldNames = null;
    private String mszOverrideFieldValues = null;
    private String mszTableName;

    public static class RESTDataChanged {
        public String szGUID;
        public String szTableName;
    }

    public static class RESTDataPutSuccess {
        public String szGUID;
        public String szTableName;

        public RESTDataPutSuccess(String szTableName, String szGUID) {
            this.szTableName = szTableName;
            this.szGUID = szGUID;
        }
    }

    public RESTDataEngineHelper(Retrofit Retrofit, String szField, DaoSession daoSession, Class<S> retrofitService, Class<D> clazzForDataItem, Property GuidProperty) {
        this.mService = Retrofit.create(retrofitService);
        this.mszField = szField;
        this.mDaoSession = daoSession;
        this.mClassForDataItem = clazzForDataItem;
        this.mGuidProperty = GuidProperty;
        this.mSynchronizeComplete.szFieldName = szField;
        this.mSynchronizeError.szFieldName = szField;
        bindMethods();
    }

    public RESTDataEngineHelper(Retrofit Retrofit, String szField, DaoSession daoSession, Class<S> retrofitService, Class<D> clazzForDataItem, Property GuidProperty, String szOverrideFieldNames, String szOverrideFieldValues) {
        this.mService = Retrofit.create(retrofitService);
        this.mszField = szField;
        this.mDaoSession = daoSession;
        this.mClassForDataItem = clazzForDataItem;
        this.mGuidProperty = GuidProperty;
        this.mSynchronizeComplete.szFieldName = szField;
        this.mSynchronizeError.szFieldName = szField;
        this.mszOverrideFieldNames = szOverrideFieldNames;
        this.mszOverrideFieldValues = szOverrideFieldValues;
        bindMethods();
    }

    private void bindMethods() {
        try {
            this.mPutItemMethod = this.mService.getClass().getMethod("putItem", new Class[]{String.class, String.class, this.mClassForDataItem, String.class, String.class});
            this.mPutItemsMethod = this.mService.getClass().getMethod("putItems", new Class[]{String.class, String.class, List.class, String.class, String.class});
            this.mGetItemMethod = this.mService.getClass().getMethod("getItem", new Class[]{String.class, String.class, String.class, String.class});
            this.mGetItemsMethod = this.mService.getClass().getMethod("getItems", new Class[]{String.class, String.class, List.class, String.class, String.class});
            this.mSynchronizeDataMethod = this.mService.getClass().getMethod("synchronizeData", new Class[]{String.class, List.class, String.class, String.class});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public RESTDataEngineHelper<S, D> setOverrideData(String szFieldNames, String szFieldValues) {
        this.mszOverrideFieldNames = szFieldNames;
        this.mszOverrideFieldValues = szFieldValues;
        return this;
    }

    public RESTDataEngineHelper<S, D> setTableName(String szTableName) {
        this.mszTableName = szTableName;
        return this;
    }

    public RESTDataEngineHelper<S, D> setReadOnly(boolean bReadOnly) {
        this.mbReadOnly = bReadOnly;
        return this;
    }

    public RESTDataEngineHelper<S, D> setEventSaveToLocalOnly(boolean bLocalOnly) {
        this.mbEventSaveToLocalOnly = bLocalOnly;
        return this;
    }

    public boolean getEventSaveToLocalOnly() {
        return this.mbEventSaveToLocalOnly;
    }

    public void saveData(final D data, final String szGUID) {
        Exception e;
        this.mDaoSession.insertOrReplace(data);
        if (!this.mbEventSaveToLocalOnly && Utilities.isNetworkConnected()) {
            try {
                ((Call) this.mPutItemMethod.invoke(this.mService, new Object[]{this.mszField, szGUID, data, this.mszOverrideFieldNames, this.mszOverrideFieldValues})).enqueue(new Callback<Void>() {
                    public void onFailure(final Call<Void> arg0, final Throwable arg1) {
                        Log.e(RESTDataEngineHelper.TAG, "saveAndPushData failed with error " + arg1.getMessage());
                        arg1.printStackTrace();
                        if (data instanceof CRestDataBase) {
                            final CRestDataBase DataBase = data;
                            if (DataBase.getFailureListener() != null) {
                                Utilities.runOnUIThread(MyiBaseApplication.getBaseAppContext(), new Runnable() {
                                    public void run() {
                                        DataBase.getFailureListener().OnDataFailure(arg0, arg1);
                                    }
                                });
                            }
                        }
                    }

                    public void onResponse(final Call<Void> arg0, final Response<Void> arg1) {
                        EventBus.getDefault().post(new RESTDataPutSuccess(RESTDataEngineHelper.this.mszTableName, szGUID));
                        if (data instanceof CRestDataBase) {
                            final CRestDataBase DataBase = data;
                            if (DataBase.getSuccessListener() != null) {
                                Utilities.runOnUIThread(MyiBaseApplication.getBaseAppContext(), new Runnable() {
                                    public void run() {
                                        DataBase.getSuccessListener().OnDataSuccess(arg0, arg1);
                                    }
                                });
                            }
                        }
                    }
                });
            } catch (IllegalAccessException e2) {
                e = e2;
                e.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e = e3;
                e.printStackTrace();
            } catch (InvocationTargetException e4) {
                e = e4;
                e.printStackTrace();
            }
        } else if (data instanceof CRestDataBase) {
            final CRestDataBase DataBase = (CRestDataBase) data;
            if (DataBase.getSuccessListener() != null) {
                Utilities.runOnUIThread(MyiBaseApplication.getBaseAppContext(), new Runnable() {
                    public void run() {
                        DataBase.getSuccessListener().OnDataSuccess(null, null);
                    }
                });
            }
        }
    }

    public void getAndSaveItem(final String szGUID) {
        Exception e;
        try {
            ((Call) this.mGetItemMethod.invoke(this.mService, new Object[]{this.mszField, szGUID, this.mszOverrideFieldNames, this.mszOverrideFieldValues})).enqueue(new Callback<D>() {
                public void onFailure(Call<D> call, Throwable arg1) {
                    Log.e(RESTDataEngineHelper.TAG, "getAndSaveItem failed with error " + arg1.getMessage());
                    arg1.printStackTrace();
                }

                public void onResponse(Call<D> call, Response<D> arg1) {
                    if (arg1.code() == 200 && arg1.body() != null) {
                        RESTDataEngineHelper.this.mDaoSession.insertOrReplace(arg1.body());
                        RESTDataChanged notify = new RESTDataChanged();
                        notify.szGUID = szGUID;
                        notify.szTableName = RESTDataEngineHelper.this.mszTableName;
                        EventBus.getDefault().post(notify);
                    }
                }
            });
            return;
        } catch (IllegalAccessException e2) {
            e = e2;
        } catch (IllegalArgumentException e3) {
            e = e3;
        } catch (InvocationTargetException e4) {
            e = e4;
        }
        e.printStackTrace();
    }

    public void setAllDataQuery(WhereCondition cond1, WhereCondition... condMore) {
        this.mAllDataWhere1 = cond1;
        this.mAllDataWheres = condMore;
    }

    public void doSychronize() {
        doSychronize(false);
    }

    public void doSychronize(final boolean bSkipRealProcess) {
        if (this.mSynchronizeTask == null || !this.mSynchronizeTask.isRunning()) {
            this.mSynchronizeTask = new SimpleBackgroundTask(MyiBaseApplication.getBaseAppContext(), SimpleBackgroundTask.getNotifyBarDisplayer(), false, new BackgroundTaskExecuteCallBack() {
                public void onExecute(SimpleBackgroundTask taskObject, String... params) {
                    RESTDataEngineHelper.this.doSychronizeReal(bSkipRealProcess, taskObject);
                }

                public void onNewInstance(SimpleBackgroundTask newInstance) {
                }

                public void onComplete() {
                    RESTDataEngineHelper.this.mSynchronizeTask = null;
                }

                public void onCancel() {
                    RESTDataEngineHelper.this.mSynchronizeTask = null;
                }
            });
            this.mSynchronizeTask.execute(new String[0]);
        }
    }

    private void doSychronizeReal(boolean bSkipRealProcess, SimpleBackgroundTask taskObject) {
        Cursor cursor;
        int i;
        Exception e;
        String[] arrFields = this.mDaoSession.getDao(this.mClassForDataItem).getAllColumns();
        List<RESTBasicData> oldData = new ArrayList();
        if (this.mAllDataWhere1 != null) {
            cursor = this.mDaoSession.getDao(this.mClassForDataItem).queryBuilder().where(this.mAllDataWhere1, this.mAllDataWheres).buildCursor().forCurrentThread().query();
        } else {
            cursor = this.mDaoSession.getDao(this.mClassForDataItem).queryBuilder().buildCursor().forCurrentThread().query();
        }
        RESTSynchronizeComplete rESTSynchronizeComplete = this.mSynchronizeComplete;
        this.mSynchronizeComplete.nFailureCount = 0;
        rESTSynchronizeComplete.nSuccessCount = 0;
        this.mbSynchronizeComplete = false;
        taskObject.setProgressTitle("数据同步");
        taskObject.setProgressMessage("正在准备本地数据...");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                RESTBasicData newitem = new RESTBasicData();
                for (i = 0; i < arrFields.length; i++) {
                    if (arrFields[i].equalsIgnoreCase("GUID")) {
                        newitem.setGuid(cursor.getString(i));
                    }
                    if (arrFields[i].equalsIgnoreCase("UserName")) {
                        newitem.setUsername(cursor.getString(i));
                    } else if (arrFields[i].equalsIgnoreCase("SYN_TimeStamp")) {
                        newitem.setSyn_timestamp(new Date(cursor.getLong(i)));
                    } else if (arrFields[i].equalsIgnoreCase("SYN_IsDelete")) {
                        newitem.setSyn_isdelete(Integer.valueOf(cursor.getInt(i)));
                    }
                }
                if (taskObject.isCancelled()) {
                    break;
                }
                oldData.add(newitem);
                cursor.moveToNext();
            }
        }
        cursor.close();
        if (taskObject.isCancelled()) {
            setSynchronizeComplete();
            this.mSynchronizeError.nStage = 0;
            this.mSynchronizeError.nCode = ErrorCode.ERROR_CANCELLED;
            EventBus.getDefault().post(this.mSynchronizeError);
            return;
        }
        try {
            taskObject.setProgressMessage("正在发送数据到服务器端...");
            try {
                Response<RESTSynchronizePackage> response = ((Call) this.mSynchronizeDataMethod.invoke(this.mService, new Object[]{this.mszField, oldData, this.mszOverrideFieldNames, this.mszOverrideFieldValues})).execute();
                int statusCode = response.code();
                RESTSynchronizePackage sychresult = (RESTSynchronizePackage) response.body();
                if (!response.isSuccessful()) {
                    this.mSynchronizeError.nStage = 0;
                    this.mSynchronizeError.nCode = statusCode;
                    setSynchronizeComplete();
                    EventBus.getDefault().post(this.mSynchronizeError);
                } else if (sychresult == null) {
                    Log.d(TAG, "no Sychronize need.");
                    setSynchronizeComplete();
                    EventBus.getDefault().post(this.mSynchronizeComplete);
                } else {
                    Iterator it;
                    this.mSynchronizeComplete.nUploadCount = sychresult.upload == null ? 0 : sychresult.upload.size();
                    this.mSynchronizeComplete.nDownloadCount = sychresult.download == null ? 0 : sychresult.download.size();
                    this.mSynchronizeComplete.nModifyCount = 0;
                    RESTSynchronizeReport report = new RESTSynchronizeReport(this.mszField, this.mSynchronizeComplete.nDownloadCount, this.mSynchronizeComplete.nUploadCount, this.mSynchronizeComplete.nModifyCount);
                    if (sychresult.upload != null) {
                        it = sychresult.upload.iterator();
                        while (it.hasNext()) {
                            report.arrUploadGUIDs.add(((RESTBasicData) it.next()).getGuid());
                        }
                    }
                    if (sychresult.download != null) {
                        it = sychresult.download.iterator();
                        while (it.hasNext()) {
                            report.arrDownloadGUIDs.add(((RESTBasicData) it.next()).getGuid());
                        }
                    }
                    EventBus.getDefault().post(report);
                    this.mSynchronizeComplete.nRemainTaskCount = (this.mSynchronizeComplete.nUploadCount + this.mSynchronizeComplete.nDownloadCount) + this.mSynchronizeComplete.nModifyCount;
                    if (bSkipRealProcess) {
                        Log.i(TAG, "Set remain task count to zero because bSkipRealProcess is set.");
                        this.mSynchronizeComplete.nRemainTaskCount = 0;
                    }
                    taskObject.setProgressMessage("正在下载和上传数据...");
                    taskObject.setProgressMax(this.mSynchronizeComplete.nRemainTaskCount);
                    taskObject.setProgress(0);
                    if (this.mSynchronizeComplete.nRemainTaskCount == 0) {
                        Log.d(TAG, "no Sychronize need.");
                        setSynchronizeComplete();
                        EventBus.getDefault().post(this.mSynchronizeComplete);
                    } else {
                        ArrayList<String> arrList;
                        if (!(this.mbReadOnly || sychresult.upload == null)) {
                            arrList = new ArrayList();
                            for (i = 0; i < sychresult.upload.size(); i++) {
                                if (taskObject.isCancelled()) {
                                    setSynchronizeComplete();
                                    this.mSynchronizeError.nStage = 2;
                                    this.mSynchronizeError.nCode = ErrorCode.ERROR_CANCELLED;
                                    EventBus.getDefault().post(this.mSynchronizeError);
                                    return;
                                }
                                arrList.add(((RESTBasicData) sychresult.upload.get(i)).getGuid());
                                if (arrList.size() > 50) {
                                    sychronizePutItems(arrList, taskObject);
                                    arrList.clear();
                                }
                            }
                            if (arrList.size() > 0) {
                                sychronizePutItems(arrList, taskObject);
                            }
                        }
                        if (sychresult.download != null) {
                            arrList = new ArrayList();
                            for (i = 0; i < sychresult.download.size(); i++) {
                                if (taskObject.isCancelled()) {
                                    setSynchronizeComplete();
                                    this.mSynchronizeError.nStage = 1;
                                    this.mSynchronizeError.nCode = ErrorCode.ERROR_CANCELLED;
                                    EventBus.getDefault().post(this.mSynchronizeError);
                                    return;
                                }
                                arrList.add(((RESTBasicData) sychresult.download.get(i)).getGuid());
                                if (arrList.size() > 200) {
                                    synchronizeGetAndSaveItems("dummy", arrList, taskObject);
                                    arrList.clear();
                                }
                            }
                            if (arrList.size() > 0) {
                                synchronizeGetAndSaveItems("dummy", arrList, taskObject);
                            }
                        }
                        ArrayList arrayList = sychresult.modified;
                    }
                    EventBus.getDefault().post(this.mSynchronizeComplete);
                    setSynchronizeComplete();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                if (!taskObject.isCancelled()) {
                    Utilities.showAlertMessage(null, "数据同步出现错误", "数据同步出现错误，错误信息：" + e2.getMessage());
                }
                Log.e(TAG, "doSychronize failed with error " + e2.getMessage());
                this.mSynchronizeError.nStage = 0;
                this.mSynchronizeError.error = e2;
                EventBus.getDefault().post(this.mSynchronizeError);
            }
        } catch (IllegalAccessException e3) {
            e = e3;
            e.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e = e4;
            e.printStackTrace();
        } catch (InvocationTargetException e5) {
            e = e5;
            e.printStackTrace();
        }
    }

    private void decreaseSynchconizeCount(SimpleBackgroundTask taskObject) {
        taskObject.increaseProgress();
        RESTSynchronizeComplete rESTSynchronizeComplete = this.mSynchronizeComplete;
        rESTSynchronizeComplete.nRemainTaskCount--;
    }

    private void setSynchronizeComplete() {
        this.mbSynchronizeComplete = true;
    }

    public boolean isSynchronizing() {
        if (this.mSynchronizeTask == null || !this.mSynchronizeTask.isRunning()) {
            return false;
        }
        return true;
    }

    private void synchronizeGetAndSaveItem(String szGUID, SimpleBackgroundTask taskObject) {
        Exception e;
        try {
            RESTSynchronizeComplete rESTSynchronizeComplete;
            try {
                Response<D> arg1 = ((Call) this.mGetItemMethod.invoke(this.mService, new Object[]{this.mszField, szGUID, this.mszOverrideFieldNames, this.mszOverrideFieldValues})).execute();
                if (arg1.code() == 200 && arg1.body() != null) {
                    this.mDaoSession.insertOrReplace(arg1.body());
                }
                rESTSynchronizeComplete = this.mSynchronizeComplete;
                rESTSynchronizeComplete.nSuccessCount++;
                decreaseSynchconizeCount(taskObject);
            } catch (IOException e2) {
                e2.printStackTrace();
                Log.e(TAG, "sychronizeGetAndSaveItem failed with error " + e2.getMessage());
                this.mSynchronizeError.nStage = 1;
                this.mSynchronizeError.error = e2;
                EventBus.getDefault().post(this.mSynchronizeError);
                rESTSynchronizeComplete = this.mSynchronizeComplete;
                rESTSynchronizeComplete.nFailureCount++;
                decreaseSynchconizeCount(taskObject);
            }
        } catch (IllegalAccessException e3) {
            e = e3;
            e.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e = e4;
            e.printStackTrace();
        } catch (InvocationTargetException e5) {
            e = e5;
            e.printStackTrace();
        }
    }

    private int synchronizeGetAndSaveItems(String szGUIDs, List<String> arrGUIDs, SimpleBackgroundTask taskObject) {
        Exception e;
        int nCount = 0;
        try {
            try {
                Response<List<D>> arg1 = ((Call) this.mGetItemsMethod.invoke(this.mService, new Object[]{this.mszField, new StringBuilder(String.valueOf(szGUIDs)).append(CrasheyeFileFilter.POSTFIX).toString(), arrGUIDs, this.mszOverrideFieldNames, this.mszOverrideFieldValues})).execute();
                if (!(arg1.code() != 200 || arg1.body() == null || arg1.body() == null)) {
                    Iterable arrBatchData = new ArrayList();
                    for (int i = 0; i < ((List) arg1.body()).size(); i++) {
                        arrBatchData.add(((List) arg1.body()).get(i));
                        RESTSynchronizeComplete rESTSynchronizeComplete = this.mSynchronizeComplete;
                        rESTSynchronizeComplete.nSuccessCount++;
                        decreaseSynchconizeCount(taskObject);
                        nCount++;
                    }
                    this.mDaoSession.getDao(this.mClassForDataItem).insertOrReplaceInTx(arrBatchData);
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                Log.e(TAG, "sychronizeGetAndSaveItem failed with error " + e2.getMessage());
                this.mSynchronizeError.nStage = 1;
                this.mSynchronizeError.error = e2;
                EventBus.getDefault().post(this.mSynchronizeError);
                rESTSynchronizeComplete = this.mSynchronizeComplete;
                rESTSynchronizeComplete.nFailureCount++;
                decreaseSynchconizeCount(taskObject);
            }
        } catch (IllegalAccessException e3) {
            e = e3;
            e.printStackTrace();
            return nCount;
        } catch (IllegalArgumentException e4) {
            e = e4;
            e.printStackTrace();
            return nCount;
        } catch (InvocationTargetException e5) {
            e = e5;
            e.printStackTrace();
            return nCount;
        }
        return nCount;
    }

    private void sychronizePutItem(String szGUID, SimpleBackgroundTask taskObject) {
        RESTSynchronizeComplete rESTSynchronizeComplete;
        Exception e;
        try {
            D data = this.mDaoSession.queryBuilder(this.mClassForDataItem).where(this.mGuidProperty.eq(szGUID), new WhereCondition[0]).limit(1).unique();
            try {
                ((Call) this.mPutItemMethod.invoke(this.mService, new Object[]{this.mszField, szGUID, data, this.mszOverrideFieldNames, this.mszOverrideFieldValues})).execute();
                EventBus.getDefault().post(new RESTDataPutSuccess(this.mszTableName, szGUID));
                rESTSynchronizeComplete = this.mSynchronizeComplete;
                rESTSynchronizeComplete.nSuccessCount++;
                decreaseSynchconizeCount(taskObject);
            } catch (IOException e2) {
                e2.printStackTrace();
                Log.e(TAG, "sychronizePutItem(" + szGUID + ") failed with error " + e2.getMessage());
                this.mSynchronizeError.nStage = 2;
                this.mSynchronizeError.error = e2;
                EventBus.getDefault().post(this.mSynchronizeError);
                rESTSynchronizeComplete = this.mSynchronizeComplete;
                rESTSynchronizeComplete.nFailureCount++;
                decreaseSynchconizeCount(taskObject);
            }
        } catch (IllegalAccessException e3) {
            e = e3;
            e.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e = e4;
            e.printStackTrace();
        } catch (InvocationTargetException e5) {
            e = e5;
            e.printStackTrace();
        }
    }

    private void sychronizePutItems(List<String> arrGUIDs, SimpleBackgroundTask taskObject) {
        RESTSynchronizeComplete rESTSynchronizeComplete;
        Exception e;
        try {
            int i;
            List<D> arrData = new ArrayList();
            for (i = 0; i < arrGUIDs.size(); i++) {
                arrData.add(this.mDaoSession.queryBuilder(this.mClassForDataItem).where(this.mGuidProperty.eq(arrGUIDs.get(i)), new WhereCondition[0]).limit(1).unique());
            }
            try {
                ((Call) this.mPutItemsMethod.invoke(this.mService, new Object[]{this.mszField, "dummy.json", arrData, this.mszOverrideFieldNames, this.mszOverrideFieldValues})).execute();
                for (i = 0; i < arrGUIDs.size(); i++) {
                    EventBus.getDefault().post(new RESTDataPutSuccess(this.mszTableName, (String) arrGUIDs.get(i)));
                    rESTSynchronizeComplete = this.mSynchronizeComplete;
                    rESTSynchronizeComplete.nSuccessCount++;
                    decreaseSynchconizeCount(taskObject);
                }
                return;
            } catch (IOException e2) {
                e2.printStackTrace();
                Log.e(TAG, "sychronizePutItems() failed with error " + e2.getMessage());
                this.mSynchronizeError.nStage = 2;
                this.mSynchronizeError.error = e2;
                EventBus.getDefault().post(this.mSynchronizeError);
                rESTSynchronizeComplete = this.mSynchronizeComplete;
                rESTSynchronizeComplete.nFailureCount++;
                decreaseSynchconizeCount(taskObject);
                return;
            }
        } catch (IllegalAccessException e3) {
            e = e3;
        } catch (IllegalArgumentException e4) {
            e = e4;
        } catch (InvocationTargetException e5) {
            e = e5;
        }
        e.printStackTrace();
    }

    public boolean cancelSynchronize() {
        if (this.mSynchronizeTask == null || !this.mSynchronizeTask.isRunning()) {
            return false;
        }
        this.mSynchronizeTask.cancel(true);
        return true;
    }

    public boolean isSynchronizeComplete() {
        return this.mbSynchronizeComplete;
    }
}
