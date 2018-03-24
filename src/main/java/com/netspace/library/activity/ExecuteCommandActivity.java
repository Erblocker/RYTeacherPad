package com.netspace.library.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ExecuteCommandActivity extends Activity {
    private static ExecuteCommandInterface mCallBack;

    public interface ExecuteCommandInterface {
        void executeCommand(Intent intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mCallBack != null) {
            mCallBack.executeCommand(getIntent());
        }
        finish();
    }

    public static void setCallBack(ExecuteCommandInterface CallBack) {
        mCallBack = CallBack;
    }
}
