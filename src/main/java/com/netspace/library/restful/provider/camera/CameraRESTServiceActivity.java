package com.netspace.library.restful.provider.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.netspace.library.restful.provider.camera.CameraRESTServiceProvider.CameraRESTCaptureEvent;
import com.netspace.library.restful.provider.camera.CameraRESTServiceProvider.CameraRESTCaptureResultEvent;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.window.AnswerSheetV2OthersWindow;
import com.netspace.library.window.AnswerSheetV3OthersWindow;
import com.netspace.library.window.AnswerSheetWindow;
import com.netspace.library.window.ChatWindow;
import java.io.File;
import net.sourceforge.opencamera.MainActivity;
import org.greenrobot.eventbus.EventBus;
import wei.mark.standout.StandOutWindow;

public class CameraRESTServiceActivity extends Activity {
    private static final int TAKE_BIG_PICTURE = 1;
    private Uri mImageUri;
    private String mPictureFileName;
    private CameraRESTCaptureEvent mSourceEvent = new CameraRESTCaptureEvent();
    private boolean mbNoDeleteOnCancel = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSourceEvent = (CameraRESTCaptureEvent) Utilities.readObjectFromIntent(this.mSourceEvent, getIntent());
        Intent it = new Intent(this, MainActivity.class);
        it.setAction("android.media.action.IMAGE_CAPTURE");
        try {
            if (this.mSourceEvent.szTargetFileName != null) {
                this.mPictureFileName = this.mSourceEvent.szTargetFileName;
                this.mbNoDeleteOnCancel = true;
            } else {
                this.mPictureFileName = File.createTempFile("tmpcamera", ".jpg", getExternalCacheDir()).getAbsolutePath();
            }
            this.mImageUri = Uri.fromFile(new File(this.mPictureFileName));
            it.putExtra("output", this.mImageUri);
            startActivityForResult(it, 1);
        } catch (Exception e) {
            Utilities.showToastMessage("无法创建临时文件，拍照操作取消");
            sendResultEvent(0);
            e.printStackTrace();
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            sendResultEvent(-1);
        } else {
            if (!this.mbNoDeleteOnCancel) {
                new File(this.mPictureFileName).delete();
                this.mPictureFileName = "";
            }
            sendResultEvent(0);
        }
        finish();
    }

    private void sendResultEvent(int nResultCode) {
        CameraRESTCaptureResultEvent event = new CameraRESTCaptureResultEvent();
        event.nResult = nResultCode;
        event.szTargetFileName = this.mPictureFileName;
        event.szRequestGUID = this.mSourceEvent.szRequestGUID;
        EventBus.getDefault().post(event);
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
