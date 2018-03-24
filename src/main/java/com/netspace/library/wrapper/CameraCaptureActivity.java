package com.netspace.library.wrapper;

import android.app.Activity;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.service.ScreenRecorderService;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.window.AnswerSheetV2OthersWindow;
import com.netspace.library.window.AnswerSheetV3OthersWindow;
import com.netspace.library.window.AnswerSheetWindow;
import com.netspace.library.window.CameraWindow;
import com.netspace.library.window.ChatWindow;
import com.netspace.library.window.VideoWindow;
import com.netspace.library.wrapper.ScreenCaptureActivity.ScreenCaptureCallBack;
import eu.janmuller.android.simplecropimage.CropImage;
import io.vov.vitamio.MediaMetadataRetriever;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import net.sourceforge.opencamera.MainActivity;
import net.sqlcipher.database.SQLiteDatabase;
import wei.mark.standout.StandOutWindow;

public class CameraCaptureActivity extends Activity {
    private static final int CROP_BIG_PICTURE = 2;
    private static final int TAKE_BIG_PICTURE = 1;
    private static CameraCaptureCallBack mCallBack;
    private static boolean mbCaptureInProgress = false;
    private Context mContext;
    private int mHeight = 0;
    private Uri mImageUri;
    private String mPictureFileName;
    private int mWidth = 0;

    public interface CameraCaptureCallBack {
        void onCaptureComplete(String str);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        if (!getIntent().getBooleanExtra("startscreenrecord", false) || ScreenRecorderService.getAllowRecord()) {
            if (getIntent().getBooleanExtra("screencapture", false)) {
                try {
                    String szLocalTempFile = File.createTempFile("tmpcamera", ".jpg", this.mContext.getCacheDir()).getAbsolutePath();
                    this.mPictureFileName = File.createTempFile("tmpcamera", ".jpg", this.mContext.getExternalCacheDir()).getAbsolutePath();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Utilities.captureScreen(szLocalTempFile)) {
                        Utilities.copyFile(new File(szLocalTempFile), new File(this.mPictureFileName));
                        new File(szLocalTempFile).delete();
                        this.mImageUri = Uri.fromFile(new File(this.mPictureFileName));
                        cropImageUri(this.mImageUri, 640, 480, 2);
                        return;
                    } else if (VERSION.SDK_INT >= 21) {
                        ScreenCaptureActivity.setCallBack(new ScreenCaptureCallBack() {
                            public void onSuccess(String szFileName) {
                                CameraCaptureActivity.this.mImageUri = Uri.fromFile(new File(CameraCaptureActivity.this.mPictureFileName));
                                CameraCaptureActivity.this.cropImageUri(CameraCaptureActivity.this.mImageUri, 640, 480, 2);
                            }

                            public void onFailure() {
                                Toast.makeText(CameraCaptureActivity.this.mContext, "屏幕捕获失败，无法开始录制", 0).show();
                            }
                        });
                        Intent intent = new Intent(this.mContext, ScreenCaptureActivity.class);
                        intent.putExtra(MediaMetadataRetriever.METADATA_KEY_FILENAME, this.mPictureFileName);
                        intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                        this.mContext.startActivity(intent);
                        return;
                    } else {
                        Toast.makeText(this, "屏幕捕获失败，无法开始录制", 0).show();
                        finish();
                        return;
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (getIntent().getBooleanExtra("nocamera", false) && getIntent().getBooleanExtra("startscreenrecord", false)) {
                startScreenRecord(null);
                finish();
                return;
            }
            Intent it = new Intent(this.mContext, MainActivity.class);
            it.setAction("android.media.action.IMAGE_CAPTURE");
            try {
                this.mPictureFileName = File.createTempFile("tmpcamera", ".jpg", this.mContext.getExternalCacheDir()).getAbsolutePath();
                this.mImageUri = Uri.fromFile(new File(this.mPictureFileName));
                it.putExtra("output", this.mImageUri);
                startActivityForResult(it, 1);
                mbCaptureInProgress = true;
                return;
            } catch (Exception e3) {
                e3.printStackTrace();
                finish();
                return;
            }
        }
        Toast.makeText(this, "当前处于上课模式，不允许录屏", 0).show();
        finish();
    }

    public static void setCallBack(CameraCaptureCallBack CallBack) {
        mCallBack = CallBack;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mbCaptureInProgress = false;
        if (resultCode != -1) {
            new File(this.mPictureFileName).delete();
            this.mPictureFileName = "";
            finish();
        } else if (requestCode == 1) {
            Log.d("TakePicture", "m_ImageUri:" + this.mImageUri.toString());
            cropImageUri(this.mImageUri, 640, 480, 2);
        } else if (requestCode == 2) {
            Bitmap SourceBitmap = BitmapFactory.decodeFile(this.mPictureFileName);
            if (SourceBitmap != null) {
                int nWidth = SourceBitmap.getWidth();
                int nHeight = SourceBitmap.getHeight();
                float fScale = 1.0f;
                if (nWidth > nHeight) {
                    if (nWidth > 1280) {
                        fScale = 1280.0f / ((float) nWidth);
                        nWidth = 1280;
                        nHeight = (int) (((float) nHeight) * fScale);
                    }
                } else if (nHeight > 1280) {
                    fScale = 1280.0f / ((float) nHeight);
                    nHeight = 1280;
                    nWidth = (int) (((float) nWidth) * fScale);
                }
                if (fScale != 1.0f) {
                    Bitmap TargetBitmap = Bitmap.createScaledBitmap(SourceBitmap, nWidth, nHeight, true);
                    if (TargetBitmap != null) {
                        try {
                            TargetBitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(this.mPictureFileName));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        TargetBitmap.recycle();
                    }
                }
                this.mWidth = nWidth;
                this.mHeight = nHeight;
                SourceBitmap.recycle();
            }
            if (getIntent().hasExtra("id")) {
                int nID = getIntent().getIntExtra("id", 0);
                Bundle data2 = new Bundle();
                data2.putString("uri", this.mPictureFileName);
                StandOutWindow.sendData(this.mContext, CameraWindow.class, nID, 0, data2, VideoWindow.class, nID);
            } else if (getIntent().getBooleanExtra("startfullscreendrawpad", false)) {
                String szKey = this.mPictureFileName;
                szKey = Utilities.getFileName(this.mPictureFileName);
                startScreenRecord(szKey.substring(0, szKey.length() - 4));
            } else {
                Intent intent = new Intent();
                intent.setData(Uri.fromFile(new File(this.mPictureFileName)));
                QuestionWidgetsUtilities.setIntentCallBack(intent);
            }
            if (mCallBack != null) {
                mCallBack.onCaptureComplete(this.mPictureFileName);
                mCallBack = null;
            }
            finish();
        }
    }

    private void startScreenRecord(String szImageKey) {
        Intent DrawActivity = new Intent(this.mContext, FingerDrawActivity.class);
        if (szImageKey != null) {
            DrawActivity.putExtra("imageKey", szImageKey);
            DrawActivity.putExtra("imageWidth", this.mWidth);
            DrawActivity.putExtra("imageHeight", this.mHeight);
        }
        DrawActivity.putExtra("allowUpload", false);
        DrawActivity.putExtra("allowCamera", true);
        DrawActivity.putExtra("uploadName", "");
        DrawActivity.putExtra("enableBackButton", true);
        DrawActivity.putExtra("multipage", true);
        DrawActivity.setFlags(335544320);
        this.mContext.startActivity(DrawActivity);
        if (getIntent().getBooleanExtra("startscreenrecord", false)) {
            try {
                ScreenRecorderService.getScreenRecordIntent().send(this.mContext, 0, new Intent());
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent(this.mContext, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, this.mPictureFileName);
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 0);
        intent.putExtra(CropImage.ASPECT_Y, 0);
        startActivityForResult(intent, requestCode);
        mbCaptureInProgress = true;
    }

    public static boolean isCaptureInProgress() {
        return mbCaptureInProgress;
    }

    protected void onDestroy() {
        mCallBack = null;
        StandOutWindow.restoreAll(this.mContext, ChatWindow.class);
        StandOutWindow.restoreAll(this.mContext, AnswerSheetWindow.class);
        StandOutWindow.restoreAll(this.mContext, AnswerSheetV2OthersWindow.class);
        StandOutWindow.restoreAll(this.mContext, AnswerSheetV3OthersWindow.class);
        super.onDestroy();
    }

    protected void onResume() {
        StandOutWindow.hideAll(this.mContext, ChatWindow.class);
        StandOutWindow.hideAll(this.mContext, AnswerSheetWindow.class);
        StandOutWindow.hideAll(this.mContext, AnswerSheetV2OthersWindow.class);
        StandOutWindow.hideAll(this.mContext, AnswerSheetV3OthersWindow.class);
        super.onResume();
    }
}
