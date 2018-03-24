package com.netspace.library.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import java.util.ArrayList;
import java.util.Iterator;

public class FragmentBaseActivity extends FragmentActivity {
    private static final int BUSY_SCREEN_REFRESH_TIME = 300;
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
    private final Runnable progressShowRunnable = new Runnable() {
        public void run() {
            int nTaskCount = VirtualNetworkObject.getActivityRemainTasks(FragmentBaseActivity.this);
            if (nTaskCount > 0 || FragmentBaseActivity.this.mBusy) {
                FragmentBaseActivity.this.mBusyDisplayer.setProgressMax(nTaskCount);
                FragmentBaseActivity.this.mBusyDisplayer.showOverlappedLayout();
                FragmentBaseActivity.this.mBusyDisplayer.setText(FragmentBaseActivity.this.mBusyText);
            } else if (nTaskCount == 0 && !FragmentBaseActivity.this.mBusy) {
                FragmentBaseActivity.this.mBusyDisplayer.hideOverlappedLayout();
            }
            FragmentBaseActivity.this.mHandler.postDelayed(FragmentBaseActivity.this.progressShowRunnable, 300);
        }
    };

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    public void registerUIDisplayer(UIDisplayer Displayer) {
        this.mUIDisplayers.add(Displayer);
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
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.putACRAData("onCreate " + getLocalClassName() + "(" + toString() + ")");
        UI.setCurrentActivity(this);
        initDefaultDisplayer();
    }

    protected void onResume() {
        UI.setCurrentActivity(this);
        Utilities.putACRAData("onResume " + getLocalClassName() + "(" + toString() + ")");
        this.mHandler.postDelayed(this.progressShowRunnable, 300);
        super.onResume();
    }

    protected void onPause() {
        if (UI.getCurrentActivity() != null && UI.getCurrentActivity().equals(this)) {
            UI.setCurrentActivity(null);
        }
        Utilities.putACRAData("onPause " + getLocalClassName() + "(" + toString() + ")");
        this.mHandler.removeCallbacks(this.progressShowRunnable);
        super.onPause();
    }

    protected void onDestroy() {
        Log.d("BaseActivity", "onDestroy");
        Utilities.putACRAData("onDestroy " + getLocalClassName() + "(" + toString() + ")");
        Utilities.logContent("onDestroy " + getLocalClassName());
        if (!this.mNoRemoveQueueOnDestroy) {
            VirtualNetworkObject.removeFromQueue(this);
        }
        if (this.mRemoveReadQueueOnDestroy) {
            VirtualNetworkObject.removeReadOperationFromQueue(this);
        }
        Iterator it = this.mUIDisplayers.iterator();
        while (it.hasNext()) {
            ((UIDisplayer) it.next()).shutDown();
        }
        super.onDestroy();
    }

    public void reportError(final ItemObject ItemObject) {
        Log.d("BaseActivity", "reportError");
        if (isFinishing()) {
            Log.e("BaseActivity", "reportError failed because this activity is finished.");
        } else {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (!FragmentBaseActivity.this.isFinishing()) {
                        Builder builder = new Builder(FragmentBaseActivity.this);
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
        this.mMessageDisplayer.hideOverlappedLayout();
    }

    public void reportError(final String szTitle, final String szMessage) {
        if (isFinishing()) {
            Log.e("BaseActivity", "reportError failed because this activity is finished.");
        } else {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (!FragmentBaseActivity.this.isFinishing()) {
                        Builder builder = new Builder(FragmentBaseActivity.this, 3);
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
            switch (event.getKeyCode()) {
                case 4:
                    event.isCanceled();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
