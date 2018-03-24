package com.netspace.library.restful.provider.screencapture;

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
import com.netspace.library.restful.RESTEvent;
import com.netspace.library.restful.provider.screencapture.ScreenCaptureRESTServiceProvider.ScreenRESTCaptureEvent;
import com.netspace.library.restful.provider.screencapture.ScreenCaptureRESTServiceProvider.ScreenRESTCaptureResultEvent;
import com.netspace.library.utilities.Utilities;
import java.io.File;
import java.nio.ByteBuffer;
import org.greenrobot.eventbus.EventBus;

public class ScreenCaptureRESTServiceActivity extends Activity {
    private static final int REQUEST_CODE_CAPTURE_PERMISSION = 1234;
    private static final String TAG = "ScreenCaptureRESTServiceActivity";
    private Runnable mGetImageRunnable = new Runnable() {
        public void run() {
            Display display = ((WindowManager) ScreenCaptureRESTServiceActivity.this.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            ScreenCaptureRESTServiceActivity.this.mVirtualDisplay = ScreenCaptureRESTServiceActivity.this.mMediaProjection.createVirtualDisplay("ScreenCapture", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, 16, ScreenCaptureRESTServiceActivity.this.mImageReader.getSurface(), null, null);
            try {
                Image image = ScreenCaptureRESTServiceActivity.this.mImageReader.acquireLatestImage();
                while (image == null) {
                    image = ScreenCaptureRESTServiceActivity.this.mImageReader.acquireLatestImage();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
                int width = image.getWidth();
                int height = image.getHeight();
                Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowPadding = planes[0].getRowStride() - (pixelStride * width);
                ScreenCaptureRESTServiceActivity.this.mWidth = width;
                ScreenCaptureRESTServiceActivity.this.mHeight = height;
                Bitmap bitmap = Bitmap.createBitmap((rowPadding / pixelStride) + width, height, Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                image.close();
                String szExtName = Utilities.getFileExtName(ScreenCaptureRESTServiceActivity.this.mszTargetFileName);
                if (szExtName.equalsIgnoreCase("jpg")) {
                    Utilities.saveBitmapToJpeg(ScreenCaptureRESTServiceActivity.this.mszTargetFileName, bitmap);
                    ScreenCaptureRESTServiceActivity.this.sendResultEvent(-1);
                } else {
                    if (szExtName.equalsIgnoreCase("png")) {
                        Utilities.saveBitmapToPng(ScreenCaptureRESTServiceActivity.this.mszTargetFileName, bitmap);
                        ScreenCaptureRESTServiceActivity.this.sendResultEvent(-1);
                    } else {
                        Log.e(ScreenCaptureRESTServiceActivity.TAG, "FileName " + ScreenCaptureRESTServiceActivity.this.mszTargetFileName + " is not recognized. No file is saved.");
                        ScreenCaptureRESTServiceActivity.this.sendResultEvent(0);
                    }
                }
            } catch (UnsupportedOperationException e2) {
                ScreenCaptureRESTServiceActivity.this.sendResultEvent(RESTEvent.RESULT_FAILURE);
            }
            ScreenCaptureRESTServiceActivity.this.mVirtualDisplay.release();
            ScreenCaptureRESTServiceActivity.this.mMediaProjection.stop();
        }
    };
    private Handler mHandler;
    private int mHeight;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;
    private ScreenRESTCaptureEvent mSourceEvent = new ScreenRESTCaptureEvent();
    private VirtualDisplay mVirtualDisplay;
    private int mWidth;
    private boolean mbNoDeleteOnCancel;
    private String mszTargetFileName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSourceEvent = (ScreenRESTCaptureEvent) Utilities.readObjectFromIntent(this.mSourceEvent, getIntent());
        this.mHandler = new Handler();
        try {
            if (this.mSourceEvent.szTargetFileName != null) {
                this.mszTargetFileName = this.mSourceEvent.szTargetFileName;
                this.mbNoDeleteOnCancel = true;
            } else {
                this.mszTargetFileName = File.createTempFile("tmpscreen", ".jpg", getExternalCacheDir()).getAbsolutePath();
            }
        } catch (Exception e) {
            Utilities.showToastMessage("无法创建临时文件，屏幕捕获操作取消");
            sendResultEvent(0);
            e.printStackTrace();
            finish();
        }
        this.mMediaProjectionManager = (MediaProjectionManager) getSystemService("media_projection");
        Intent permissionIntent = this.mMediaProjectionManager.createScreenCaptureIntent();
        permissionIntent.setFlags(67108864);
        startActivityForResult(permissionIntent, REQUEST_CODE_CAPTURE_PERMISSION);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult, result=" + resultCode);
        if (REQUEST_CODE_CAPTURE_PERMISSION == requestCode) {
            if (resultCode == -1) {
                this.mMediaProjection = this.mMediaProjectionManager.getMediaProjection(resultCode, intent);
                Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                this.mImageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, 1, 5);
                if (this.mImageReader != null) {
                    Log.d(TAG, "imageReader Successful");
                }
                Utilities.showToastMessage("3秒后开始捕获当前屏幕");
                this.mHandler.postDelayed(this.mGetImageRunnable, 3000);
            } else {
                if (!this.mbNoDeleteOnCancel) {
                    new File(this.mszTargetFileName).delete();
                    this.mszTargetFileName = "";
                }
                sendResultEvent(0);
            }
            finish();
        }
    }

    private void sendResultEvent(int nResultCode) {
        ScreenRESTCaptureResultEvent event = new ScreenRESTCaptureResultEvent();
        event.nResult = nResultCode;
        event.szTargetFileName = this.mszTargetFileName;
        event.szRequestGUID = this.mSourceEvent.szRequestGUID;
        EventBus.getDefault().post(event);
    }
}
