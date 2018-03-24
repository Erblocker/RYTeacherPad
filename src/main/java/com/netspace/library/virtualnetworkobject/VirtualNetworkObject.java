package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.http.HttpHost;

public class VirtualNetworkObject {
    private static final String TAG = "VirtualNetworkObject";
    protected static ItemCache mCache;
    protected static String mCachePath;
    protected static DataSynchronizeEngine mDataSynchronizeEngine;
    protected static HashMap<String, Engine> mEngines = new HashMap();
    protected static boolean mPauseCurrentActivityQueue = false;
    protected static ArrayList<ItemObject> mQueue = new ArrayList();
    protected static boolean mShutdown = false;
    protected static WorkingThread mWorkingThread;
    protected static Handler mWorkingThreadHandler;

    /* renamed from: com.netspace.library.virtualnetworkobject.VirtualNetworkObject$1 */
    class AnonymousClass1 extends Thread {
        private final /* synthetic */ ItemObject val$ItemObject;

        AnonymousClass1(ItemObject itemObject) {
            this.val$ItemObject = itemObject;
        }

        public void run() {
            setName("VirtualnetworkObject executeNow thread");
            VirtualNetworkObject.execute(this.val$ItemObject);
        }
    }

    protected static class WorkingThread extends Thread {
        private WorkingRunnable mRunnable = new WorkingRunnable();

        private class WorkingRunnable implements Runnable {
            private WorkingRunnable() {
            }

            public void run() {
                ItemObject OneObject = null;
                boolean bHasTask = false;
                synchronized (VirtualNetworkObject.mQueue) {
                    if (VirtualNetworkObject.mQueue.size() > 0) {
                        if (VirtualNetworkObject.mPauseCurrentActivityQueue) {
                            Activity CurrentActivity = UI.getCurrentActivity();
                            if (CurrentActivity != null) {
                                Iterator it = VirtualNetworkObject.mQueue.iterator();
                                while (it.hasNext()) {
                                    ItemObject OneTaskObject = (ItemObject) it.next();
                                    if (OneTaskObject.getActivity() != null && !OneTaskObject.getActivity().equals(CurrentActivity)) {
                                        OneObject = OneTaskObject;
                                        VirtualNetworkObject.mQueue.remove(OneObject);
                                        bHasTask = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            OneObject = (ItemObject) VirtualNetworkObject.mQueue.get(0);
                            VirtualNetworkObject.mQueue.remove(0);
                            bHasTask = true;
                        }
                    }
                }
                if (bHasTask) {
                    VirtualNetworkObject.execute(OneObject);
                }
                if (VirtualNetworkObject.mWorkingThreadHandler != null) {
                    VirtualNetworkObject.mWorkingThreadHandler.postDelayed(WorkingThread.this.mRunnable, 100);
                }
            }
        }

        protected WorkingThread() {
        }

        public void stopThread() {
            if (VirtualNetworkObject.mWorkingThreadHandler != null) {
                VirtualNetworkObject.mWorkingThreadHandler.post(new Runnable() {
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
            VirtualNetworkObject.mWorkingThreadHandler = null;
        }

        public void run() {
            super.run();
            setName("VirtualNetworkObject Working Thread");
            Looper.prepare();
            VirtualNetworkObject.mWorkingThreadHandler = new Handler();
            VirtualNetworkObject.mWorkingThreadHandler.postDelayed(this.mRunnable, 100);
            Looper.loop();
        }
    }

    public static void initEngines(Context Context) {
        if (mWorkingThread == null) {
            mShutdown = false;
            mCachePath = new StringBuilder(String.valueOf(Context.getExternalCacheDir().getAbsolutePath())).append("/").toString();
            ResourceEngine ResourceEngine = new ResourceEngine(Context);
            mEngines.put(ResourceEngine.getEngineName(), ResourceEngine);
            ResourceOfflineEngine ResourceOfflineEngine = new ResourceOfflineEngine(Context);
            mEngines.put(ResourceOfflineEngine.getEngineName(), ResourceOfflineEngine);
            QuestionEngine QuestionEngine = new QuestionEngine(Context);
            mEngines.put(QuestionEngine.getEngineName(), QuestionEngine);
            QuestionOfflineEngine QuestionOfflineEngine = new QuestionOfflineEngine(Context);
            mEngines.put(QuestionOfflineEngine.getEngineName(), QuestionOfflineEngine);
            mDataSynchronizeEngine = new DataSynchronizeEngine(Context);
            mEngines.put(mDataSynchronizeEngine.getEngineName(), mDataSynchronizeEngine);
            PrivateDataEngine PrivateDataEngine = new PrivateDataEngine(Context);
            mEngines.put(PrivateDataEngine.getEngineName(), PrivateDataEngine);
            WebServiceCallEngine WebServiceCallEngine = new WebServiceCallEngine(Context);
            mEngines.put(WebServiceCallEngine.getEngineName(), WebServiceCallEngine);
            HttpCallEngine HttpCallEngine = new HttpCallEngine(Context);
            mEngines.put(HttpCallEngine.getEngineName(), HttpCallEngine);
            RESTEngine RestEngine = new RESTEngine(Context);
            mEngines.put(RestEngine.getEngineName(), RestEngine);
            mCache = new ItemCache(Context.getExternalCacheDir().getAbsolutePath());
            for (Entry<String, Engine> entry : mEngines.entrySet()) {
                ((Engine) entry.getValue()).startEngine();
            }
            mWorkingThread = new WorkingThread();
            mWorkingThread.start();
        }
    }

    public static String getServerAddress() {
        return LoadExamDataThread3.getServerAddress();
    }

    public static Engine getEngine(String szEngineName) {
        if (mEngines.containsKey(szEngineName)) {
            return (Engine) mEngines.get(szEngineName);
        }
        Log.e(TAG, "Engine " + szEngineName + " is not found.");
        return null;
    }

    public static DataSynchronizeEngine getDataSynchronizeEngine() {
        return mDataSynchronizeEngine;
    }

    public static ItemCache getCache() {
        return mCache;
    }

    public static boolean getOfflineMode() {
        return MyiBaseApplication.getCommonVariables().Session.getOfflineMode();
    }

    public static void setOfflineMode(boolean bOfflineMode) {
        MyiBaseApplication.getCommonVariables().Session.setOfflineMode(bOfflineMode);
    }

    public static String getOfflineObjectContent(String szGUID) {
        String szCacheFilePath = mCachePath + szGUID + ".cache";
        if (new File(szCacheFilePath).exists()) {
            return Utilities.getTextFromFile(szCacheFilePath);
        }
        return null;
    }

    public static String getOfflineURL(String szURL) {
        if (!szURL.startsWith(HttpHost.DEFAULT_SCHEME_NAME) && new File(szURL).exists()) {
            return szURL;
        }
        String szCacheFilePath = mCachePath + Utilities.md5(szURL) + ".cache";
        String szExtName = Utilities.getFileExtName(szURL);
        if (szExtName.equalsIgnoreCase("pdf")) {
            szCacheFilePath = new StringBuilder(String.valueOf(szCacheFilePath)).append(".").append(szExtName).toString();
        }
        if (new File(szCacheFilePath).exists()) {
            return szCacheFilePath;
        }
        return null;
    }

    public static void shutDown() {
        synchronized (mQueue) {
            mQueue.clear();
        }
        if (mWorkingThread != null) {
            mShutdown = true;
            mWorkingThread.stopThread();
            for (Entry<String, Engine> entry : mEngines.entrySet()) {
                ((Engine) entry.getValue()).stopEngine();
            }
            mEngines.clear();
            mWorkingThread = null;
            mCache = null;
        }
    }

    public static void addToQueue(ItemObject ItemObject) {
        synchronized (mQueue) {
            mQueue.add(ItemObject);
        }
    }

    public static void executeNow(ItemObject ItemObject) {
        if (mShutdown) {
            Log.e(TAG, "Calling " + ItemObject.toString() + " after shutdown VirtualNetworkObject.");
        } else {
            new AnonymousClass1(ItemObject).start();
        }
    }

    public static void removeFromQueue(Activity Activity) {
        Log.d(TAG, "removeFromQueue");
        synchronized (mQueue) {
            int i = 0;
            while (i < mQueue.size()) {
                ItemObject ItemObject = (ItemObject) mQueue.get(i);
                Activity OwnerActivity = ItemObject.getActivity();
                if (!(OwnerActivity == null || !OwnerActivity.equals(Activity) || ItemObject.getNoDeleteOnFinish())) {
                    mQueue.remove(i);
                    i--;
                }
                i++;
            }
        }
        if (mPauseCurrentActivityQueue) {
            pauseCurrentActivityExecution(false);
        }
    }

    public static void removeReadOperationFromQueue(Activity Activity) {
        Log.d(TAG, "removeReadOperationFromQueue");
        synchronized (mQueue) {
            int i = 0;
            while (i < mQueue.size()) {
                ItemObject ItemObject = (ItemObject) mQueue.get(i);
                if (ItemObject.mReadOperation) {
                    Activity OwnerActivity = ItemObject.getActivity();
                    if (!(OwnerActivity == null || !OwnerActivity.equals(Activity) || ItemObject.getNoDeleteOnFinish())) {
                        mQueue.remove(i);
                        i--;
                    }
                }
                i++;
            }
        }
        if (mPauseCurrentActivityQueue) {
            pauseCurrentActivityExecution(false);
        }
    }

    public static int getActivityRemainTasks(Activity Activity) {
        int nTaskCount = 0;
        synchronized (mQueue) {
            Iterator it = mQueue.iterator();
            while (it.hasNext()) {
                Activity OwnerActivity = ((ItemObject) it.next()).getActivity();
                if (OwnerActivity != null && OwnerActivity.equals(Activity)) {
                    nTaskCount++;
                }
            }
        }
        if (mPauseCurrentActivityQueue && Activity.equals(UI.getCurrentActivity())) {
            return 0;
        }
        return nTaskCount;
    }

    protected static void updateUI(ItemObject ItemObject) {
        Activity OwnerActivity = ItemObject.getActivity();
        if (OwnerActivity != null && (OwnerActivity instanceof BaseActivity)) {
            ((BaseActivity) OwnerActivity).setBusyText(ItemObject.getBusyText());
        }
    }

    protected static void execute(ItemObject ItemObject) {
        String szEngineName = ItemObject.getRequiredEngineName();
        boolean bReadOperation = ItemObject.getReadOperation();
        if (getOfflineMode()) {
            if (szEngineName.equalsIgnoreCase("QuestionEngine")) {
                szEngineName = "QuestionOfflineEngine";
            }
            if (szEngineName.equalsIgnoreCase("ResourceEngine")) {
                szEngineName = "ResourceOfflineEngine";
            }
        }
        Engine TargetEngine = getEngine(szEngineName);
        if (TargetEngine == null) {
            throw new IllegalArgumentException("Engine " + szEngineName + " is not found. Can not process ItemObject");
        } else if (!bReadOperation) {
            updateUI(ItemObject);
            TargetEngine.handlePackageWrite(ItemObject);
        } else if (ItemObject.getAllowCache() && mCache.hasCache(ItemObject) && getOfflineMode() && mCache.readFromCache(ItemObject)) {
            ItemObject.callCallbacks(true, 0);
        } else {
            updateUI(ItemObject);
            TargetEngine.handlePackageRead(ItemObject);
        }
    }

    public static boolean writeToCache(ItemObject ItemObject) {
        return mCache.writeToCache(ItemObject);
    }

    public static void pauseCurrentActivityExecution(boolean bPause) {
        Log.d(TAG, "pauseCurrentActivityExecution " + bPause);
        mPauseCurrentActivityQueue = bPause;
    }
}
