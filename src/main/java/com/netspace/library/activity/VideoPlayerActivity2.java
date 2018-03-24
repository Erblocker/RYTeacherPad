package com.netspace.library.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import com.netspace.library.controls.CustomVideoView4FullScreen;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class VideoPlayerActivity2 extends BaseActivity {
    private static FullScreenVideoPlayCallBack mCallBack;
    private AudioManager mAudioManager;
    private float mBrightness = -1.0f;
    private Handler mDismissHandler = new Handler() {
        public void handleMessage(Message msg) {
            VideoPlayerActivity2.this.mVolumeBrightnessLayout.setVisibility(8);
        }
    };
    private GestureDetector mGestureDetector;
    private int mLayout = 3;
    private int mMaxVolume;
    private ImageView mOperationBg;
    private ImageView mOperationPercent;
    private long mPosition = -1;
    private CustomVideoView4FullScreen mVideoView;
    private int mVolume = -1;
    private View mVolumeBrightnessLayout;

    public interface FullScreenVideoPlayCallBack {
        void onFullScreenPlayActivityComplete(long j);
    }

    private class MyGestureListener extends SimpleOnGestureListener {
        private MyGestureListener() {
        }

        public boolean onDoubleTap(MotionEvent e) {
            if (VideoPlayerActivity2.this.mLayout == 3) {
                VideoPlayerActivity2.this.mLayout = 0;
            } else {
                VideoPlayerActivity2 videoPlayerActivity2 = VideoPlayerActivity2.this;
                videoPlayerActivity2.mLayout = videoPlayerActivity2.mLayout + 1;
            }
            if (VideoPlayerActivity2.this.mVideoView != null) {
                VideoPlayerActivity2.this.mVideoView.setVideoLayout(VideoPlayerActivity2.this.mLayout, 0);
            }
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX();
            float mOldY = e1.getY();
            int y = (int) e2.getRawY();
            Display disp = VideoPlayerActivity2.this.getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();
            if (((double) mOldX) > (((double) windowWidth) * 4.0d) / 5.0d) {
                VideoPlayerActivity2.this.onVolumeSlide((mOldY - ((float) y)) / ((float) windowHeight));
            } else if (((double) mOldX) < ((double) windowWidth) / 5.0d) {
                VideoPlayerActivity2.this.onBrightnessSlide((mOldY - ((float) y)) / ((float) windowHeight));
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.setKeepScreenOn(getWindow());
        Utilities.setFullScreenWindow(getWindow());
        setContentView(R.layout.activity_videoplayer2);
        String szPath = "";
        if (getIntent().hasExtra("itemLocation")) {
            szPath = getIntent().getExtras().getString("itemLocation");
        }
        if (getIntent().hasExtra("position")) {
            this.mPosition = getIntent().getLongExtra("position", -1);
        }
        if (szPath.isEmpty()) {
            finish();
            return;
        }
        this.mVideoView = (CustomVideoView4FullScreen) findViewById(R.id.surface_view);
        this.mVideoView.setFullScreen(true);
        this.mVideoView.setUrlToPlay(szPath);
        if (this.mPosition != -1) {
            this.mVideoView.seekTo((int) this.mPosition);
        }
        this.mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
        this.mOperationBg = (ImageView) findViewById(R.id.operation_bg);
        this.mOperationPercent = (ImageView) findViewById(R.id.operation_percent);
        this.mAudioManager = (AudioManager) getSystemService("audio");
        this.mMaxVolume = this.mAudioManager.getStreamMaxVolume(3);
        this.mGestureDetector = new GestureDetector(this, new MyGestureListener());
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        switch (event.getAction() & 255) {
            case 1:
                endGesture();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void endGesture() {
        this.mVolume = -1;
        this.mBrightness = -1.0f;
        this.mDismissHandler.removeMessages(0);
        this.mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }

    public static void setCallBack(FullScreenVideoPlayCallBack CallBack) {
        mCallBack = CallBack;
    }

    private void onVolumeSlide(float percent) {
        if (this.mVolume == -1) {
            this.mVolume = this.mAudioManager.getStreamVolume(3);
            if (this.mVolume < 0) {
                this.mVolume = 0;
            }
            this.mOperationBg.setImageResource(R.drawable.video_volumn_bg);
            this.mVolumeBrightnessLayout.setVisibility(0);
        }
        int index = ((int) (((float) this.mMaxVolume) * percent)) + this.mVolume;
        if (index > this.mMaxVolume) {
            index = this.mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }
        this.mAudioManager.setStreamVolume(3, index, 0);
        LayoutParams lp = this.mOperationPercent.getLayoutParams();
        lp.width = (findViewById(R.id.operation_full).getLayoutParams().width * index) / this.mMaxVolume;
        this.mOperationPercent.setLayoutParams(lp);
    }

    private void onBrightnessSlide(float percent) {
        if (this.mBrightness < 0.0f) {
            this.mBrightness = getWindow().getAttributes().screenBrightness;
            if (this.mBrightness <= 0.0f) {
                this.mBrightness = 0.5f;
            }
            if (this.mBrightness < 0.01f) {
                this.mBrightness = 0.01f;
            }
            this.mOperationBg.setImageResource(R.drawable.video_brightness_bg);
            this.mVolumeBrightnessLayout.setVisibility(0);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = this.mBrightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        getWindow().setAttributes(lpa);
        LayoutParams lp = this.mOperationPercent.getLayoutParams();
        lp.width = (int) (((float) findViewById(R.id.operation_full).getLayoutParams().width) * lpa.screenBrightness);
        this.mOperationPercent.setLayoutParams(lp);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mVideoView != null) {
            this.mVideoView.setVideoLayout(this.mLayout, 0);
        }
        super.onConfigurationChanged(newConfig);
    }

    public void onBackPressed() {
        long nPos = this.mVideoView.getPos();
        this.mVideoView.stop();
        finish();
        if (mCallBack != null) {
            mCallBack.onFullScreenPlayActivityComplete(nPos);
            mCallBack = null;
        }
        super.onBackPressed();
    }
}
