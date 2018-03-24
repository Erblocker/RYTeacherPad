package com.netspace.library.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import io.vov.vitamio.Vitamio;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class BaseActivity extends AppCompatActivity {
    private static final int BUSY_SCREEN_REFRESH_TIME = 300;
    protected static ArrayList<WeakReference<BaseActivity>> mActivities = new ArrayList();
    private static ActivitySwitchCallBack mCallBack;
    private final Runnable GetScreenCopyRunnable = new Runnable() {
        public void run() {
            if (!BaseActivity.this.mbDisableScreenCopyRunnable) {
                long nStartTime = System.currentTimeMillis();
                int nRefreshTime = 500;
                if (UI.ScreenJpegServer != null && UI.ScreenJpegServer.HasClients() && UI.ScreenJpegServer.needFeedImage()) {
                    nRefreshTime = UI.ScreenJpegServer.getRefreshTime();
                    long nTime1 = System.currentTimeMillis();
                    View v = BaseActivity.this.getWindow().getDecorView();
                    boolean bLastCacheEnabled = v.isDrawingCacheEnabled();
                    if (!bLastCacheEnabled) {
                        v.buildDrawingCache();
                    }
                    Bitmap b = v.getDrawingCache();
                    nTime1 = System.currentTimeMillis() - nTime1;
                    long nTime2 = System.currentTimeMillis();
                    UI.ScreenJpegServer.PostNewImageData(b);
                    nTime2 = System.currentTimeMillis() - nTime2;
                    if (!bLastCacheEnabled) {
                        v.destroyDrawingCache();
                    }
                }
                BaseActivity.this.mHandler.postDelayed(BaseActivity.this.GetScreenCopyRunnable, (long) nRefreshTime);
            }
        }
    };
    protected boolean mBusy = false;
    protected UIDisplayer mBusyDisplayer;
    protected String mBusyText = "正在获取数据...";
    protected ArrayList<View> mDisabledControls = new ArrayList();
    protected UIDisplayer mErrorDisplayer;
    protected Handler mHandler = new Handler();
    protected boolean mHasCancelledError = false;
    protected UIDisplayer mMessageDisplayer;
    protected boolean mNoRemoveQueueOnDestroy = false;
    protected boolean mRemoveReadQueueOnDestroy = false;
    protected ArrayList<UIDisplayer> mUIDisplayers = new ArrayList();
    protected VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    private boolean mbDelayBackButton = false;
    protected boolean mbDisableScreenCopyRunnable = false;
    private boolean mbSecondBackExit = false;
    protected boolean mbSkipVitamioInit = false;
    private boolean mbUseFinishOnBackExit = false;
    private final Runnable progressShowRunnable = new Runnable() {
        public void run() {
            int nTaskCount = VirtualNetworkObject.getActivityRemainTasks(BaseActivity.this);
            if (nTaskCount > 0 || BaseActivity.this.mBusy) {
                BaseActivity.this.mBusyDisplayer.setProgressMax(nTaskCount);
                BaseActivity.this.mBusyDisplayer.showOverlappedLayout();
                BaseActivity.this.mBusyDisplayer.setText(BaseActivity.this.mBusyText);
            } else if (nTaskCount == 0 && !BaseActivity.this.mBusy) {
                BaseActivity.this.mBusyDisplayer.hideOverlappedLayout();
            }
            BaseActivity.this.mHandler.postDelayed(BaseActivity.this.progressShowRunnable, 300);
        }
    };

    public interface ActivitySwitchCallBack {
        void onActivityCreate(Activity activity);

        void onActivityDestroy(Activity activity);

        void onActivityResume(Activity activity);
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    public void registerUIDisplayer(UIDisplayer Displayer) {
        this.mUIDisplayers.add(Displayer);
    }

    public static void setCallBack(ActivitySwitchCallBack CallBack) {
        mCallBack = CallBack;
    }

    public void setDoublePressReturn(boolean bEnable) {
        this.mbDelayBackButton = bEnable;
    }

    public void setDoublePressReturn(boolean bEnable, boolean bUseFinishOnExit) {
        this.mbDelayBackButton = bEnable;
        this.mbUseFinishOnBackExit = bUseFinishOnExit;
    }

    public Bitmap capture() {
        View v = getWindow().getDecorView();
        boolean bLastCacheEnabled = v.isDrawingCacheEnabled();
        v.setDrawingCacheEnabled(true);
        Bitmap b = v.getDrawingCache();
        Bitmap result = Utilities.cloneBitmap(b, 0, 0, b.getWidth(), b.getHeight());
        v.setDrawingCacheEnabled(bLastCacheEnabled);
        return result;
    }

    protected void initDefaultDisplayer() {
        this.mBusyDisplayer = new BusyUIDisplayer(this);
        this.mErrorDisplayer = new ErrorUIDisplayer(this);
        this.mMessageDisplayer = new MessageUIDisplayer(this);
    }

    public void setBusyText(String szText) {
        this.mBusyText = szText;
    }

    public void resetCancelFlag() {
        this.mHasCancelledError = false;
    }

    public void setBusy(boolean bBusy) {
        this.mBusy = bBusy;
        Log.d("BaseActivity", "setBusy(" + bBusy + ")");
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.i("BaseActivity", "onCreate " + toString());
        Utilities.logActivity(this, "create");
        String szThemeName = PreferenceManager.getDefaultSharedPreferences(this).getString("MainTheme", "AppTheme");
        if (!szThemeName.equalsIgnoreCase("AppTheme")) {
            int nThemeID = getResources().getIdentifier(szThemeName, "style", getPackageName());
            if (nThemeID != 0) {
                setTheme(nThemeID);
            }
        }
        super.onCreate(savedInstanceState);
        if (!this.mbSkipVitamioInit) {
            Vitamio.isInitialized(getApplicationContext());
        }
        synchronized (mActivities) {
            mActivities.add(new WeakReference(this));
        }
        Utilities.putACRAData("onCreate " + getLocalClassName() + "(" + toString() + ")");
        UI.setCurrentActivity(this);
        initDefaultDisplayer();
        if (mCallBack != null) {
            mCallBack.onActivityCreate(this);
        }
    }

    private void removeFromActivityList() {
        synchronized (mActivities) {
            boolean bModified = true;
            while (bModified) {
                bModified = false;
                Iterator it = mActivities.iterator();
                while (it.hasNext()) {
                    WeakReference<BaseActivity> OneActivity = (WeakReference) it.next();
                    BaseActivity OneObject = (BaseActivity) OneActivity.get();
                    if (OneObject != null) {
                        if (OneObject.equals(this)) {
                            mActivities.remove(OneActivity);
                            bModified = true;
                            break;
                        }
                    }
                    mActivities.remove(OneActivity);
                    bModified = true;
                    break;
                }
            }
        }
    }

    protected void onResume() {
        Log.d("BaseActivity", "onResume " + toString());
        Utilities.logActivity(this, "resume");
        Utilities.putACRAData("onResume " + getLocalClassName() + "(" + toString() + ")");
        UI.setCurrentActivity(this);
        this.mHandler.postDelayed(this.progressShowRunnable, 300);
        if (!this.mbDisableScreenCopyRunnable) {
            this.mHandler.postDelayed(this.GetScreenCopyRunnable, 500);
        }
        super.onResume();
        if (mCallBack != null) {
            mCallBack.onActivityResume(this);
        }
    }

    protected void onPause() {
        Log.d("BaseActivity", "onPause " + toString());
        Utilities.logActivity(this, "pause");
        Utilities.putACRAData("onPause " + getLocalClassName() + "(" + toString() + ")");
        if (UI.getCurrentActivity() != null && UI.getCurrentActivity().equals(this)) {
            UI.setCurrentActivity(null);
        }
        this.mHandler.removeCallbacks(this.progressShowRunnable);
        if (!this.mbDisableScreenCopyRunnable) {
            this.mHandler.removeCallbacks(this.GetScreenCopyRunnable);
        }
        super.onPause();
    }

    protected void onDestroy() {
        Log.d("BaseActivity", "onDestroy " + toString());
        Utilities.logActivity(this, "destroy");
        Utilities.putACRAData("onDestroy " + getLocalClassName() + "(" + toString() + ")");
        removeFromActivityList();
        if (!this.mNoRemoveQueueOnDestroy) {
            VirtualNetworkObject.removeFromQueue(this);
        }
        if (this.mRemoveReadQueueOnDestroy) {
            VirtualNetworkObject.removeReadOperationFromQueue(this);
        }
        this.mVirtualNetworkObjectManager.cancelAll();
        Iterator it = this.mUIDisplayers.iterator();
        while (it.hasNext()) {
            ((UIDisplayer) it.next()).shutDown();
        }
        super.onDestroy();
        if (mCallBack != null) {
            mCallBack.onActivityDestroy(this);
        }
    }

    public void finishAllActivity() {
        synchronized (mActivities) {
            boolean bModified = true;
            while (bModified) {
                bModified = false;
                Iterator it = mActivities.iterator();
                while (it.hasNext()) {
                    WeakReference<BaseActivity> OneActivity = (WeakReference) it.next();
                    BaseActivity OneObject = (BaseActivity) OneActivity.get();
                    if (OneObject == null) {
                        mActivities.remove(OneActivity);
                        bModified = true;
                        break;
                    } else if (!OneObject.equals(this)) {
                        if (!OneObject.isFinishing()) {
                            OneObject.finish();
                        }
                        mActivities.remove(OneActivity);
                        bModified = true;
                    }
                }
            }
        }
    }

    public static void endAllActivity() {
        synchronized (mActivities) {
            boolean bModified = true;
            while (bModified) {
                bModified = false;
                Iterator it = mActivities.iterator();
                if (it.hasNext()) {
                    WeakReference<BaseActivity> OneActivity = (WeakReference) it.next();
                    BaseActivity OneObject = (BaseActivity) OneActivity.get();
                    if (OneObject == null) {
                        mActivities.remove(OneActivity);
                        bModified = true;
                    } else {
                        if (!OneObject.isFinishing()) {
                            OneObject.finish();
                        }
                        mActivities.remove(OneActivity);
                        bModified = true;
                    }
                }
            }
        }
    }

    public void reportError(final ItemObject ItemObject) {
        Log.d("BaseActivity", "reportError");
        if (isFinishing()) {
            Log.e("BaseActivity", "reportError failed because this activity is finished.");
        } else {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (!BaseActivity.this.isFinishing()) {
                        Builder builder = new Builder(BaseActivity.this);
                        if (ItemObject.buildErrorDialog(builder)) {
                            final ItemObject itemObject = ItemObject;
                            builder.setPositiveButton("重试", new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    VirtualNetworkObject.addToQueue(itemObject);
                                }
                            });
                            builder.setNegativeButton("取消", new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            builder.show();
                        }
                    }
                }
            });
        }
    }

    public void reportMessage(String szTitle, String szMessage) {
        this.mMessageDisplayer.setTitle(szTitle);
        this.mMessageDisplayer.setText(szMessage);
        this.mMessageDisplayer.showOverlappedLayout();
    }

    public void hideMessage() {
        if (this.mMessageDisplayer != null) {
            this.mMessageDisplayer.hideOverlappedLayout();
        }
    }

    public void reportError(final String szTitle, final String szMessage) {
        if (isFinishing()) {
            Log.e("BaseActivity", "reportError failed because this activity is finished.");
        } else {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (!BaseActivity.this.isFinishing()) {
                        Builder builder = new Builder(BaseActivity.this, 3);
                        builder.setTitle(szTitle);
                        builder.setMessage(szMessage);
                        builder.setIcon(17301543);
                        builder.setNegativeButton("确定", null);
                        builder.setCancelable(false);
                        builder.show();
                    }
                }
            });
        }
    }

    public void alertMessage(final String szTitle, final String szMessage) {
        if (isFinishing()) {
            Log.e("BaseActivity", "alertMessage failed because this activity is finished.");
        } else {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (!BaseActivity.this.isFinishing()) {
                        Builder builder = new Builder(BaseActivity.this, 3);
                        builder.setTitle(szTitle);
                        builder.setMessage(szMessage);
                        builder.setIcon(17301659);
                        builder.setNegativeButton("确定", null);
                        builder.setCancelable(false);
                        builder.show();
                    }
                }
            });
        }
    }

    protected void DisableControls(View Child) {
        if (Child instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) Child;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child.isEnabled()) {
                    child.setEnabled(false);
                    this.mDisabledControls.add(child);
                }
                if (child instanceof ViewGroup) {
                    DisableControls((ViewGroup) child);
                }
            }
        } else if (Child.isEnabled()) {
            Child.setEnabled(false);
            this.mDisabledControls.add(Child);
        }
    }

    protected void EnableControls() {
        for (int i = 0; i < this.mDisabledControls.size(); i++) {
            ((View) this.mDisabledControls.get(i)).setEnabled(true);
        }
        this.mDisabledControls.clear();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (UI.isScreenLocked() || UI.isTimeLocked()) {
            if (this instanceof FingerDrawActivity) {
                return super.dispatchKeyEvent(event);
            }
            switch (event.getKeyCode()) {
                case 4:
                    event.isCanceled();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void onIMMessage(String szFrom, String szMessage) {
    }

    public void onBackPressed() {
        if (!this.mbDelayBackButton) {
            super.onBackPressed();
        } else if (!this.mbSecondBackExit) {
            this.mbSecondBackExit = true;
            Toast.makeText(this, "再按一次返回键退出", 0).show();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    BaseActivity.this.mbSecondBackExit = false;
                }
            }, 2000);
        } else if (this.mbUseFinishOnBackExit) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
