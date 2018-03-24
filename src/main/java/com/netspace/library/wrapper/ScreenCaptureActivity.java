package com.netspace.library.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.netspace.library.utilities.Utilities;
import io.vov.vitamio.MediaMetadataRetriever;
import java.nio.ByteBuffer;

public class ScreenCaptureActivity extends Activity {
    private static final int REQUEST_CODE_CAPTURE_PERM = 1234;
    private static final String TAG = "ScreenCaptureActivity";
    private static ScreenCaptureCallBack mCallBack;
    private Handler mHandler;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;
    private VirtualDisplay mVirtualDisplay;
    private String mszTargetFileName;

    public interface ScreenCaptureCallBack {
        void onFailure();

        void onSuccess(String str);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null) {
            finish();
        } else if (getIntent().hasExtra(MediaMetadataRetriever.METADATA_KEY_FILENAME)) {
            this.mHandler = new Handler();
            this.mszTargetFileName = getIntent().getStringExtra(MediaMetadataRetriever.METADATA_KEY_FILENAME);
            this.mMediaProjectionManager = (MediaProjectionManager) getSystemService("media_projection");
            Intent permissionIntent = this.mMediaProjectionManager.createScreenCaptureIntent();
            permissionIntent.setFlags(67108864);
            startActivityForResult(permissionIntent, REQUEST_CODE_CAPTURE_PERM);
        } else {
            Log.e(TAG, "filename extra is not set.");
            finish();
        }
    }

    public static void setCallBack(ScreenCaptureCallBack CallBack) {
        mCallBack = CallBack;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult, result=" + resultCode);
        if (mCallBack != null) {
            Log.d(TAG, "onActivityResult, callback is set.");
        } else {
            Log.d(TAG, "onActivityResult, callback is null.");
        }
        if (REQUEST_CODE_CAPTURE_PERM == requestCode && mCallBack != null) {
            if (resultCode == -1) {
                this.mMediaProjection = this.mMediaProjectionManager.getMediaProjection(resultCode, intent);
                Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                this.mImageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, 1, 5);
                if (this.mImageReader != null) {
                    Log.d(TAG, "imageReader Successful");
                }
                this.mVirtualDisplay = this.mMediaProjection.createVirtualDisplay("ScreenCapture", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, 16, this.mImageReader.getSurface(), null, null);
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        Image image = ScreenCaptureActivity.this.mImageReader.acquireLatestImage();
                        int width = image.getWidth();
                        int height = image.getHeight();
                        Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        Bitmap bitmap = Bitmap.createBitmap(((planes[0].getRowStride() - (pixelStride * width)) / pixelStride) + width, height, Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);
                        image.close();
                        String szExtName = Utilities.getFileExtName(ScreenCaptureActivity.this.mszTargetFileName);
                        if (szExtName.equalsIgnoreCase("jpg")) {
                            Utilities.saveBitmapToJpeg(ScreenCaptureActivity.this.mszTargetFileName, bitmap);
                            ScreenCaptureActivity.mCallBack.onSuccess(ScreenCaptureActivity.this.mszTargetFileName);
                        } else if (szExtName.equalsIgnoreCase("png")) {
                            Utilities.saveBitmapToPng(ScreenCaptureActivity.this.mszTargetFileName, bitmap);
                            ScreenCaptureActivity.mCallBack.onSuccess(ScreenCaptureActivity.this.mszTargetFileName);
                        } else {
                            Log.e(ScreenCaptureActivity.TAG, "FileName " + ScreenCaptureActivity.this.mszTargetFileName + " is not recognized. No file is saved.");
                        }
                        ScreenCaptureActivity.this.mMediaProjection.stop();
                        ScreenCaptureActivity.mCallBack = null;
                    }
                }, 1500);
            } else {
                mCallBack.onFailure();
            }
        }
        finish();
    }

    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (this.mVirtualDisplay != null) {
            this.mVirtualDisplay.release();
        }
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
