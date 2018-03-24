package com.netspace.library.restful.provider.imagecrop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import com.netspace.library.restful.provider.imagecrop.CropRESTServiceProvider.CropRESTCaptureEvent;
import com.netspace.library.restful.provider.imagecrop.CropRESTServiceProvider.CropRESTCaptureResultEvent;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.window.AnswerSheetV2OthersWindow;
import com.netspace.library.window.AnswerSheetV3OthersWindow;
import com.netspace.library.window.AnswerSheetWindow;
import com.netspace.library.window.ChatWindow;
import eu.janmuller.android.simplecropimage.CropImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.greenrobot.eventbus.EventBus;
import wei.mark.standout.StandOutWindow;

public class CropRESTServiceActivity extends Activity {
    private static final int CROP_BIG_PICTURE = 2;
    private String mPictureFileName;
    private CropRESTCaptureEvent mSourceEvent = new CropRESTCaptureEvent();
    private boolean mbNoDeleteOnCancel = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSourceEvent = (CropRESTCaptureEvent) Utilities.readObjectFromIntent(this.mSourceEvent, getIntent());
        try {
            if (this.mSourceEvent.szTargetFileName == null || !new File(this.mSourceEvent.szTargetFileName).exists()) {
                sendResultEvent(0);
                finish();
                return;
            }
            this.mPictureFileName = this.mSourceEvent.szTargetFileName;
            this.mbNoDeleteOnCancel = true;
            cropImageUri(2);
        } catch (Exception e) {
            Utilities.showToastMessage("无法创建临时文件，拍照操作取消");
            sendResultEvent(0);
            e.printStackTrace();
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            if (!this.mbNoDeleteOnCancel) {
                new File(this.mPictureFileName).delete();
                this.mPictureFileName = "";
            }
            sendResultEvent(0);
            finish();
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
                SourceBitmap.recycle();
            }
            sendResultEvent(-1);
            finish();
        }
    }

    private void sendResultEvent(int nResultCode) {
        CropRESTCaptureResultEvent event = new CropRESTCaptureResultEvent();
        event.nResult = nResultCode;
        event.szTargetFileName = this.mPictureFileName;
        event.szRequestGUID = this.mSourceEvent.szRequestGUID;
        EventBus.getDefault().post(event);
    }

    private void cropImageUri(int requestCode) {
        Intent intent = new Intent(this, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, this.mPictureFileName);
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 0);
        intent.putExtra(CropImage.ASPECT_Y, 0);
        startActivityForResult(intent, requestCode);
    }

    protected void onDestroy() {
        StandOutWindow.restoreAll(this, ChatWindow.class);
        StandOutWindow.restoreAll(this, AnswerSheetWindow.class);
        StandOutWindow.restoreAll(this, AnswerSheetV2OthersWindow.class);
        StandOutWindow.restoreAll(this, AnswerSheetV3OthersWindow.class);
        super.onDestroy();
    }

    protected void onResume() {
        StandOutWindow.hideAll(this, ChatWindow.class);
        StandOutWindow.hideAll(this, AnswerSheetWindow.class);
        StandOutWindow.hideAll(this, AnswerSheetV2OthersWindow.class);
        StandOutWindow.hideAll(this, AnswerSheetV3OthersWindow.class);
        super.onResume();
    }
}
