package com.netspace.library.wrapper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import com.netspace.library.service.ScreenRecorderService3;

@TargetApi(21)
public class ScreenRecordPermissionActivity extends Activity {
    private static final int REQUEST_CODE_CAPTURE_PERM = 1234;
    private static final String TAG = "ScreenRecordPermissionActivity";
    private static ScreenRecordPermissionCallBack mCallBack;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;

    public interface ScreenRecordPermissionCallBack {
        void onFailure();

        void onSuccess(MediaProjection mediaProjection);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMediaProjectionManager = (MediaProjectionManager) getSystemService("media_projection");
        Intent permissionIntent = this.mMediaProjectionManager.createScreenCaptureIntent();
        permissionIntent.setFlags(67108864);
        startActivityForResult(permissionIntent, REQUEST_CODE_CAPTURE_PERM);
    }

    public static void setCallBack(ScreenRecordPermissionCallBack CallBack) {
        mCallBack = CallBack;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult, result=" + resultCode);
        if (mCallBack != null) {
            Log.d(TAG, "onActivityResult, callback is set.");
        } else {
            Log.d(TAG, "onActivityResult, callback is null.");
        }
        if (REQUEST_CODE_CAPTURE_PERM == requestCode) {
            if (mCallBack != null) {
                if (resultCode == -1) {
                    this.mMediaProjection = this.mMediaProjectionManager.getMediaProjection(resultCode, intent);
                    mCallBack.onSuccess(this.mMediaProjection);
                } else {
                    mCallBack.onFailure();
                }
            } else if (resultCode == -1) {
                this.mMediaProjection = this.mMediaProjectionManager.getMediaProjection(resultCode, intent);
                ScreenRecorderService3.setMediaProjection(this.mMediaProjection);
            }
            mCallBack = null;
        }
        finish();
    }

    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }
}
