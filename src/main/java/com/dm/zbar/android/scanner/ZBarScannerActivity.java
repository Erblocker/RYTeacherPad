package com.dm.zbar.android.scanner;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import java.util.Iterator;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;

public class ZBarScannerActivity extends Activity implements PreviewCallback, ZBarConstants {
    private static final String TAG = "ZBarScannerActivity";
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            ZBarScannerActivity.this.mAutoFocusHandler.postDelayed(ZBarScannerActivity.this.doAutoFocus, 1000);
        }
    };
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (ZBarScannerActivity.this.mCamera != null && ZBarScannerActivity.this.mPreviewing) {
                ZBarScannerActivity.this.mCamera.autoFocus(ZBarScannerActivity.this.autoFocusCB);
            }
        }
    };
    private Handler mAutoFocusHandler;
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean mPreviewing = true;
    private ImageScanner mScanner;

    static {
        System.loadLibrary("iconv");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isCameraAvailable()) {
            requestWindowFeature(1);
            getWindow().addFlags(1024);
            this.mAutoFocusHandler = new Handler();
            setupScanner();
            this.mPreview = new CameraPreview(this, this, this.autoFocusCB);
            setContentView(this.mPreview);
            return;
        }
        cancelRequest();
    }

    public void setupScanner() {
        this.mScanner = new ImageScanner();
        this.mScanner.setConfig(0, 256, 3);
        this.mScanner.setConfig(0, 257, 3);
        this.mScanner.setConfig(64, 0, 1);
    }

    protected void onResume() {
        super.onResume();
        this.mCamera = Camera.open();
        if (this.mCamera == null) {
            cancelRequest();
            return;
        }
        this.mPreview.setCamera(this.mCamera);
        this.mPreview.showSurfaceView();
        this.mPreviewing = true;
    }

    protected void onPause() {
        super.onPause();
        if (this.mCamera != null) {
            this.mPreview.setCamera(null);
            this.mCamera.cancelAutoFocus();
            this.mCamera.setPreviewCallback(null);
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mPreview.hideSurfaceView();
            this.mPreviewing = false;
            this.mCamera = null;
        }
    }

    public boolean isCameraAvailable() {
        return getPackageManager().hasSystemFeature("android.hardware.camera");
    }

    public void cancelRequest() {
        Intent dataIntent = new Intent();
        dataIntent.putExtra(ZBarConstants.ERROR_INFO, "Camera unavailable");
        setResult(0, dataIntent);
        finish();
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        Size size = camera.getParameters().getPreviewSize();
        Image barcode = new Image(size.width, size.height, "Y800");
        barcode.setData(data);
        if (this.mScanner.scanImage(barcode) != 0) {
            this.mCamera.cancelAutoFocus();
            this.mCamera.setPreviewCallback(null);
            this.mCamera.stopPreview();
            this.mPreviewing = false;
            Iterator it = this.mScanner.getResults().iterator();
            while (it.hasNext()) {
                String symData = ((Symbol) it.next()).getData();
                if (!TextUtils.isEmpty(symData)) {
                    Log.d(TAG, "Get data " + symData);
                    return;
                }
            }
        }
    }
}
