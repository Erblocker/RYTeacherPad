package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map.Entry;

public class ItemObject {
    private static final String TAG = "ItemObject";
    protected static final int UI_MSG_FAILURE = 2;
    protected static final int UI_MSG_SUCCESS = 1;
    private final String ENGINENAME;
    protected WeakReference<Activity> mActivity;
    protected boolean mAllowCache;
    protected boolean mAlwaysCallCallbacks;
    protected String mBusyText;
    protected boolean mCalledFromUIThread;
    protected long mExpireTimeInMS;
    protected OnFailureListener mFailureListener;
    protected boolean mIgnoreActivityFinishCheck;
    protected boolean mNoCallOnActivityFinish;
    protected boolean mNoDeleteOnFinish;
    protected String mObjectURI;
    protected HashMap<String, Object> mParams;
    protected Object mPrivateObject;
    private ProgressDialog mProgressDialog;
    protected boolean mReadOperation;
    protected int mReturnCode;
    protected String mReturnMessage;
    protected OnSuccessListener mSuccessListener;
    protected String mTextContent;
    protected UIThreadMessageHandler mUIHandler;
    protected boolean mbCancelled;
    private int mnRetryCount;

    public interface OnFailureListener {
        void OnDataFailure(ItemObject itemObject, int i);
    }

    public interface OnSuccessListener {
        void OnDataSuccess(ItemObject itemObject, int i);
    }

    protected class UIThreadMessageHandler extends Handler {
        protected UIThreadMessageHandler() {
        }

        public void handleMessage(Message msg) {
            if (ItemObject.this.isActivityAlive() || ItemObject.this.mAlwaysCallCallbacks) {
                if (msg.what == 1) {
                    if (ItemObject.this.mSuccessListener == null || !(msg.obj instanceof ItemObject)) {
                        Log.d(ItemObject.TAG, "Operation successful, but no callback is executed.");
                    } else {
                        try {
                            ItemObject.this.mSuccessListener.OnDataSuccess((ItemObject) msg.obj, msg.arg1);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(ItemObject.TAG, "Execute OnDataSuccess failed with exception. " + msg.obj.toString());
                        }
                    }
                } else if (msg.what == 2) {
                    if (ItemObject.this.mFailureListener == null || !(msg.obj instanceof ItemObject)) {
                        Log.d(ItemObject.TAG, "Operation failed, but no callback is executed.");
                    } else {
                        try {
                            ItemObject.this.mFailureListener.OnDataFailure((ItemObject) msg.obj, msg.arg1);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            Log.e(ItemObject.TAG, "Execute OnDataFailure failed with exception. " + msg.obj.toString());
                        }
                    }
                }
                ItemObject.this.clearCallbacks();
                return;
            }
            Log.d(ItemObject.TAG, "Operation returned, but called activity is not alive. Ignore UI callbacks. Calling URI = " + ItemObject.this.mObjectURI);
            ItemObject.this.clearCallbacks();
        }
    }

    public ItemObject() {
        this(null, null, null);
    }

    public ItemObject(String szObjectURI) {
        this(szObjectURI, null, null);
    }

    public ItemObject(String szObjectURI, Activity Activity) {
        this(szObjectURI, Activity, null);
    }

    public ItemObject(String szObjectURI, Activity Activity, OnSuccessListener SuccessListener) {
        this.ENGINENAME = "NullEngine";
        this.mParams = new HashMap();
        this.mReadOperation = true;
        this.mAllowCache = false;
        this.mNoDeleteOnFinish = false;
        this.mNoCallOnActivityFinish = false;
        this.mReturnCode = 0;
        this.mBusyText = "正在获取数据...";
        this.mCalledFromUIThread = false;
        this.mUIHandler = null;
        this.mIgnoreActivityFinishCheck = false;
        this.mAlwaysCallCallbacks = false;
        this.mbCancelled = false;
        this.mnRetryCount = 1;
        this.mObjectURI = szObjectURI;
        this.mSuccessListener = SuccessListener;
        setActivity(Activity);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.mCalledFromUIThread = true;
            this.mUIHandler = new UIThreadMessageHandler();
        }
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.mProgressDialog = progressDialog;
    }

    public String readTextData() {
        return this.mTextContent;
    }

    public boolean writeTextData(String szData) {
        this.mTextContent = szData;
        return true;
    }

    public String getBusyText() {
        return this.mBusyText;
    }

    public void setBusyText(String szText) {
        this.mBusyText = szText;
    }

    public void setIgnoreActivityFinishCheck(boolean bIgnore) {
        this.mIgnoreActivityFinishCheck = bIgnore;
    }

    public void setAlwaysActiveCallbacks(boolean bOn) {
        this.mAlwaysCallCallbacks = bOn;
    }

    public String getObjectURI() {
        return this.mObjectURI;
    }

    public void setObjectURI(String szObjectURI) {
        this.mObjectURI = szObjectURI;
    }

    public void setPrivateObject(Object PrivateObject) {
        this.mPrivateObject = PrivateObject;
    }

    public Object getPrivateObject() {
        return this.mPrivateObject;
    }

    public boolean getNoDeleteOnFinish() {
        return this.mNoDeleteOnFinish;
    }

    public void setNoDeleteOnFinish(boolean bNoDeleteOnFinish) {
        this.mNoDeleteOnFinish = bNoDeleteOnFinish;
    }

    public boolean getAllowCache() {
        return this.mAllowCache;
    }

    public void setAllowCache(boolean bAllowCache) {
        this.mAllowCache = bAllowCache;
    }

    public void setExpireTime(long nMSFromNowOn) {
        this.mExpireTimeInMS = System.currentTimeMillis() + nMSFromNowOn;
    }

    public boolean isExpire() {
        if (this.mExpireTimeInMS == 0 || System.currentTimeMillis() <= this.mExpireTimeInMS) {
            return false;
        }
        return true;
    }

    public void setSuccessListener(OnSuccessListener SuccessListener) {
        this.mSuccessListener = SuccessListener;
    }

    public OnSuccessListener getSuccessListener() {
        return this.mSuccessListener;
    }

    public void setFailureListener(OnFailureListener FailureListener) {
        this.mFailureListener = FailureListener;
    }

    public OnFailureListener getFailureListener() {
        return this.mFailureListener;
    }

    public void clearCallbacks() {
        this.mSuccessListener = null;
        this.mFailureListener = null;
    }

    public void setCancelled() {
        this.mbCancelled = true;
        this.mSuccessListener = null;
        this.mFailureListener = null;
    }

    public void setActivity(Activity Activity) {
        if (Activity != null) {
            this.mActivity = new WeakReference(Activity);
            if (!Activity.isFinishing()) {
                this.mNoCallOnActivityFinish = true;
                return;
            }
            return;
        }
        this.mActivity = null;
        this.mNoCallOnActivityFinish = false;
    }

    public Activity getActivity() {
        if (this.mActivity == null) {
            return null;
        }
        return (Activity) this.mActivity.get();
    }

    public void setParam(String szParamName, Object ParamObject) {
        this.mParams.put(szParamName, ParamObject);
    }

    public Object getParam(String szParamName) {
        return this.mParams.get(szParamName);
    }

    public void clearParams() {
        this.mParams.clear();
    }

    public boolean callCallbacks(boolean bSuccess, int nReturnCode) {
        return callCallbacks(bSuccess, nReturnCode, this.mReturnMessage);
    }

    public boolean callCallbacks(boolean bSuccess, int nReturnCode, String szReturnMessage) {
        this.mReturnCode = nReturnCode;
        this.mReturnMessage = szReturnMessage;
        if (this.mProgressDialog != null) {
            Utilities.runOnUIThread(MyiBaseApplication.getBaseAppContext(), new Runnable() {
                public void run() {
                    if (ItemObject.this.mProgressDialog != null) {
                        try {
                            if (ItemObject.this.mProgressDialog.isShowing()) {
                                ItemObject.this.mProgressDialog.dismiss();
                            }
                        } catch (Exception e) {
                        }
                        ItemObject.this.mProgressDialog = null;
                    }
                }
            });
        }
        if (VirtualNetworkObject.mShutdown || this.mbCancelled) {
            return false;
        }
        if (bSuccess && (this.mAllowCache || VirtualNetworkObject.getOfflineMode())) {
            VirtualNetworkObject.writeToCache(this);
        }
        if (this.mActivity != null && !isActivityAlive()) {
            return false;
        }
        if (!bSuccess && this.mActivity != null && (this.mActivity.get() instanceof BaseActivity) && this.mFailureListener == null) {
            ((BaseActivity) this.mActivity.get()).reportError(this);
        }
        if (bSuccess && this.mSuccessListener != null) {
            if (this.mUIHandler == null) {
                try {
                    this.mSuccessListener.OnDataSuccess(this, nReturnCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Execute OnDataSuccess failed with exception. " + toString());
                }
                clearCallbacks();
            } else {
                this.mUIHandler.obtainMessage(1, nReturnCode, 0, this).sendToTarget();
            }
            return true;
        } else if (bSuccess || this.mFailureListener == null) {
            return false;
        } else {
            if (this.mUIHandler == null) {
                try {
                    this.mFailureListener.OnDataFailure(this, nReturnCode);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    Log.e(TAG, "Execute OnDataFailure failed with exception. " + toString());
                }
                clearCallbacks();
            } else {
                this.mUIHandler.obtainMessage(2, nReturnCode, 0, this).sendToTarget();
            }
            return true;
        }
    }

    protected boolean isActivityAlive() {
        if (this.mActivity == null) {
            return false;
        }
        Activity TempActivity = (Activity) this.mActivity.get();
        if (TempActivity == null) {
            return false;
        }
        if (!this.mNoCallOnActivityFinish || this.mIgnoreActivityFinishCheck) {
            return true;
        }
        if (TempActivity.isFinishing()) {
            return false;
        }
        return true;
    }

    public String getRequiredEngineName() {
        return "NullEngine";
    }

    public boolean getReadOperation() {
        return this.mReadOperation;
    }

    public void setReadOperation(boolean bReadOperation) {
        this.mReadOperation = bReadOperation;
    }

    public int getReturnCode() {
        return this.mReturnCode;
    }

    public void setRetryCount(int nRetryCount) {
        this.mnRetryCount = nRetryCount;
    }

    public int getRetryCount() {
        return this.mnRetryCount;
    }

    public String toString() {
        StringBuilder szResult = new StringBuilder();
        szResult.append(getClass().getName());
        szResult.append("@");
        if (this.mObjectURI != null) {
            szResult.append(this.mObjectURI);
        } else {
            szResult.append("mObjectURI=null");
        }
        szResult.append(";ReadOperation=" + this.mReadOperation + ";");
        for (Entry<String, Object> entry : this.mParams.entrySet()) {
            szResult.append((String) entry.getKey());
            szResult.append("=");
            if (entry.getValue() != null) {
                szResult.append(entry.getValue().toString());
            } else {
                szResult.append("null");
            }
            szResult.append(";");
        }
        return szResult.toString();
    }

    public String getReturnMessage() {
        return this.mReturnMessage;
    }

    public boolean buildErrorDialog(Builder Builder) {
        Builder.setTitle("通讯错误");
        Builder.setMessage("获取数据时出现错误，错误代码：" + String.valueOf(this.mReturnCode) + "，错误原因：" + this.mReturnMessage);
        return true;
    }

    public String getErrorText() {
        return "获取数据时出现错误(" + String.valueOf(this.mReturnCode) + ")";
    }
}
