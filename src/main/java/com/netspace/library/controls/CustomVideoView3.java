package com.netspace.library.controls;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import io.vov.vitamio.widget.VideoView;

public class CustomVideoView3 extends VideoView {
    private int _overrideHeight = -1;
    private int _overrideWidth = -1;
    private boolean mBuffering = true;
    private int mMeasureHeight = 0;
    private int mMeasureWidth = 0;
    private MediaPlayer mMediaPlayer;
    private int mPreparePercent = 0;
    private boolean mReadyToPlay = false;
    private boolean mbPrepared = false;

    public CustomVideoView3(Context context) {
        super(context);
    }

    public CustomVideoView3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView3(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void resizeVideo(int width, int height) {
        this._overrideHeight = height;
        this._overrideWidth = width;
        if (getHolder() != null) {
            getHolder().setFixedSize(width, height);
            requestLayout();
            invalidate();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this._overrideWidth != -1 && this._overrideHeight != -1) {
            setMeasuredDimension(this._overrideWidth, this._overrideHeight);
        }
    }
}
