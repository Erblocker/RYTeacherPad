package com.netspace.library.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.window.VideoWindow;
import net.sourceforge.opencamera.MainActivity;
import wei.mark.standout.StandOutWindow;

public class CameraRecordActivity extends Activity {
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static CameraRecordCallBack mCallBack;
    private int mCallID = -1;

    public interface CameraRecordCallBack {
        void onRecordComplete(String str);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mCallID = getIntent().getIntExtra("id", -1);
        Intent takeVideoIntent = new Intent(this, MainActivity.class);
        takeVideoIntent.setAction("android.media.action.VIDEO_CAPTURE");
        takeVideoIntent.putExtra("android.intent.extra.durationLimit", 10);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, 1);
        }
    }

    public static void setCallBack(CameraRecordCallBack CallBack) {
        mCallBack = CallBack;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == -1) {
            Uri videoUri = data.getData();
            Bundle data2 = new Bundle();
            data2.putString("uri", videoUri.toString());
            if (this.mCallID != -1) {
                StandOutWindow.sendData(getApplicationContext(), VideoWindow.class, this.mCallID, 1, data2, VideoWindow.class, this.mCallID);
            } else if (mCallBack != null) {
                mCallBack.onRecordComplete(videoUri.toString());
            } else {
                Intent intent = new Intent();
                intent.setData(videoUri);
                QuestionWidgetsUtilities.setIntentCallBack(intent);
            }
        }
        mCallBack = null;
        finish();
    }
}
