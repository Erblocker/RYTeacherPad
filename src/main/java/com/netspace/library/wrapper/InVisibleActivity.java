package com.netspace.library.wrapper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import com.netspace.pad.library.R;

public class InVisibleActivity extends FragmentActivity {
    private static OnActivityReadyCallBack mCallBack;
    private static InVisibleActivity mLastInstance;

    public interface OnActivityReadyCallBack {
        void onActivityReady(Context context);
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastInstance = this;
    }

    public static void setCallBack(OnActivityReadyCallBack CallBack) {
        mCallBack = CallBack;
    }

    public static void finishLastActivity() {
        if (mLastInstance != null) {
            mLastInstance.finish();
            mLastInstance = null;
        }
    }

    protected void onDestroy() {
        mCallBack = null;
        super.onDestroy();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        if (mCallBack != null) {
            mCallBack.onActivityReady(new ContextThemeWrapper(this, R.style.AppDialogTheme));
        }
    }
}
