package com.netspace.library.controls;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

public class CustomVideoView2 extends VideoView implements OnPreparedListener, OnBufferingUpdateListener, OnInfoListener {
    private int _overrideHeight = 360;
    private int _overrideWidth = 480;
    private boolean mBuffering = true;
    private PlayPauseListener mListener;
    private MediaPlayer mMediaPlayer;
    private int mPreparePercent = 0;
    private boolean mReadyToPlay = false;
    private boolean mbPrepared = false;

    public interface PlayPauseListener {
        void onBufferUpdate(CustomVideoView2 customVideoView2, int i);

        void onPause();

        void onPlay();

        void onSeekTo(int i);
    }

    public CustomVideoView2(Context context) {
        super(context);
        setOnPreparedListener(this);
    }

    public CustomVideoView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreparedListener(this);
    }

    public CustomVideoView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnPreparedListener(this);
    }

    public void setPlayPauseListener(PlayPauseListener listener) {
        this.mListener = listener;
    }

    public void rawPause() {
        super.pause();
    }

    public void rawResume() {
        super.resume();
    }

    public void rawStart() {
        super.start();
    }

    public void rawSeekTo(int msec) {
        super.seekTo(msec);
    }

    public int getPreparedPercent() {
        return this.mPreparePercent;
    }

    public void pause() {
        if (this.mListener != null) {
            this.mListener.onPause();
        }
    }

    public void start() {
        if (this.mListener != null) {
            this.mListener.onPlay();
        }
    }

    public void seekTo(int msec) {
        if (this.mListener != null) {
            this.mListener.onSeekTo(msec);
        }
    }

    public boolean getPrepared() {
        return this.mbPrepared;
    }

    public boolean getReadyToPlay() {
        return this.mReadyToPlay;
    }

    public void onPrepared(MediaPlayer mp) {
        this.mMediaPlayer = mp;
        mp.setOnInfoListener(this);
    }

    public void setMediaController(CustomMediaController controller) {
        super.setMediaController(controller);
        setOnPreparedListener(this);
    }

    public void resizeVideo(int width, int height) {
        this._overrideHeight = height;
        this._overrideWidth = width;
        getHolder().setFixedSize(width, height);
        requestLayout();
        invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this._overrideWidth, this._overrideHeight);
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (this.mPreparePercent != percent) {
            this.mPreparePercent = percent;
            if (this.mListener != null) {
                this.mListener.onBufferUpdate(this, percent);
            }
        }
    }

    public boolean getBuffering() {
        return this.mBuffering;
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == 702) {
            Log.d("CustomVideoView2", "MEDIA_INFO_BUFFERING_END");
            this.mReadyToPlay = true;
            this.mBuffering = false;
        }
        if (what == 701) {
            Log.d("CustomVideoView2", "MEDIA_INFO_BUFFERING_START");
            this.mBuffering = true;
        }
        if (what == 3) {
            this.mReadyToPlay = true;
            post(new Runnable() {
                public void run() {
                    Log.d("CustomVideoView2", "Quest raw pause.");
                }
            });
            Log.d("CustomVideoView2", "MEDIA_INFO_VIDEO_RENDERING_START. mp is paused.");
        }
        return false;
    }
}
