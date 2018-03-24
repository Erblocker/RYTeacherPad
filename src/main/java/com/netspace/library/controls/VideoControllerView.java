package com.netspace.library.controls;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.TransportMediator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class VideoControllerView extends FrameLayout {
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final String TAG = "VideoControllerView";
    private static final int sDefaultTimeout = 3000;
    private ViewGroup mAnchor;
    private Context mContext;
    private TextView mCurrentTime;
    private boolean mDragging;
    private TextView mEndTime;
    private ImageButton mFfwdButton;
    private OnClickListener mFfwdListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private boolean mFromXml;
    private ImageButton mFullscreenButton;
    private OnClickListener mFullscreenListener;
    private Handler mHandler;
    private boolean mListenersSet;
    private ImageButton mNextButton;
    private OnClickListener mNextListener;
    private ImageButton mPauseButton;
    private OnClickListener mPauseListener;
    private MediaPlayerControl mPlayer;
    private ImageButton mPrevButton;
    private OnClickListener mPrevListener;
    private ProgressBar mProgress;
    private ImageButton mRewButton;
    private OnClickListener mRewListener;
    private View mRoot;
    private OnSeekBarChangeListener mSeekListener;
    private boolean mShowing;
    private boolean mUseFastForward;

    public interface MediaPlayerControl {
        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        int getBufferPercentage();

        int getCurrentPosition();

        int getDuration();

        boolean isFullScreen();

        boolean isPlaying();

        void pause();

        void seekTo(int i);

        void start();

        void toggleFullScreen();
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        MessageHandler(VideoControllerView view) {
            this.mView = new WeakReference(view);
        }

        public void handleMessage(Message msg) {
            VideoControllerView view = (VideoControllerView) this.mView.get();
            if (view != null && view.mPlayer != null) {
                switch (msg.what) {
                    case 1:
                        view.hide();
                        return;
                    case 2:
                        int pos = view.setProgress();
                        if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                            sendMessageDelayed(obtainMessage(2), (long) (1000 - (pos % 1000)));
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHandler = new MessageHandler(this);
        this.mPauseListener = new OnClickListener() {
            public void onClick(View v) {
                if (VideoControllerView.this.mPlayer != null) {
                    Utilities.logClick(v, String.valueOf(VideoControllerView.this.mPlayer.getCurrentPosition()));
                }
                VideoControllerView.this.doPauseResume();
                VideoControllerView.this.show(VideoControllerView.sDefaultTimeout);
            }
        };
        this.mFullscreenListener = new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                VideoControllerView.this.doToggleFullscreen();
                VideoControllerView.this.show(VideoControllerView.sDefaultTimeout);
            }
        };
        this.mSeekListener = new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {
                VideoControllerView.this.show(3600000);
                VideoControllerView.this.mDragging = true;
                VideoControllerView.this.mHandler.removeMessages(2);
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (VideoControllerView.this.mPlayer != null && fromuser) {
                    long newposition = (((long) progress) * ((long) VideoControllerView.this.mPlayer.getDuration())) / 1000;
                    VideoControllerView.this.mPlayer.seekTo((int) newposition);
                    if (VideoControllerView.this.mCurrentTime != null) {
                        VideoControllerView.this.mCurrentTime.setText(VideoControllerView.this.stringForTime((int) newposition));
                    }
                    Utilities.logClick(bar, String.valueOf(newposition));
                }
            }

            public void onStopTrackingTouch(SeekBar bar) {
                VideoControllerView.this.mDragging = false;
                VideoControllerView.this.setProgress();
                VideoControllerView.this.updatePausePlay();
                VideoControllerView.this.show(VideoControllerView.sDefaultTimeout);
                VideoControllerView.this.mHandler.sendEmptyMessage(2);
            }
        };
        this.mRewListener = new OnClickListener() {
            public void onClick(View v) {
                if (VideoControllerView.this.mPlayer != null) {
                    Utilities.logClick(v, String.valueOf(VideoControllerView.this.mPlayer.getCurrentPosition()));
                    VideoControllerView.this.mPlayer.seekTo(VideoControllerView.this.mPlayer.getCurrentPosition() - 5000);
                    VideoControllerView.this.setProgress();
                    VideoControllerView.this.show(VideoControllerView.sDefaultTimeout);
                }
            }
        };
        this.mFfwdListener = new OnClickListener() {
            public void onClick(View v) {
                if (VideoControllerView.this.mPlayer != null) {
                    Utilities.logClick(v, String.valueOf(VideoControllerView.this.mPlayer.getCurrentPosition()));
                    VideoControllerView.this.mPlayer.seekTo(VideoControllerView.this.mPlayer.getCurrentPosition() + 15000);
                    VideoControllerView.this.setProgress();
                    VideoControllerView.this.show(VideoControllerView.sDefaultTimeout);
                }
            }
        };
        this.mRoot = null;
        this.mContext = context;
        this.mUseFastForward = true;
        this.mFromXml = true;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context, boolean useFastForward) {
        super(context);
        this.mHandler = new MessageHandler(this);
        this.mPauseListener = /* anonymous class already generated */;
        this.mFullscreenListener = /* anonymous class already generated */;
        this.mSeekListener = /* anonymous class already generated */;
        this.mRewListener = /* anonymous class already generated */;
        this.mFfwdListener = /* anonymous class already generated */;
        this.mContext = context;
        this.mUseFastForward = useFastForward;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context) {
        this(context, true);
        Log.i(TAG, TAG);
    }

    public void onFinishInflate() {
        if (this.mRoot != null) {
            initControllerView(this.mRoot);
        }
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        this.mPlayer = player;
        updatePausePlay();
        updateFullScreen();
    }

    public void setAnchorView(ViewGroup view) {
        this.mAnchor = view;
        LayoutParams frameParams = new LayoutParams(-1, -1);
        removeAllViews();
        addView(makeControllerView(), frameParams);
    }

    protected View makeControllerView() {
        this.mRoot = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.media_controller, null);
        initControllerView(this.mRoot);
        return this.mRoot;
    }

    private void initControllerView(View v) {
        int i = 0;
        this.mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (this.mPauseButton != null) {
            this.mPauseButton.requestFocus();
            this.mPauseButton.setOnClickListener(this.mPauseListener);
        }
        this.mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        if (this.mFullscreenButton != null) {
            this.mFullscreenButton.requestFocus();
            this.mFullscreenButton.setOnClickListener(this.mFullscreenListener);
        }
        this.mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
        if (this.mFfwdButton != null) {
            this.mFfwdButton.setOnClickListener(this.mFfwdListener);
            if (!this.mFromXml) {
                this.mFfwdButton.setVisibility(this.mUseFastForward ? 0 : 8);
            }
        }
        this.mRewButton = (ImageButton) v.findViewById(R.id.rew);
        if (this.mRewButton != null) {
            this.mRewButton.setOnClickListener(this.mRewListener);
            if (!this.mFromXml) {
                ImageButton imageButton = this.mRewButton;
                if (!this.mUseFastForward) {
                    i = 8;
                }
                imageButton.setVisibility(i);
            }
        }
        this.mNextButton = (ImageButton) v.findViewById(R.id.next);
        if (!(this.mNextButton == null || this.mFromXml || this.mListenersSet)) {
            this.mNextButton.setVisibility(8);
        }
        this.mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        if (!(this.mPrevButton == null || this.mFromXml || this.mListenersSet)) {
            this.mPrevButton.setVisibility(8);
        }
        this.mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_progress);
        if (this.mProgress != null) {
            if (this.mProgress instanceof SeekBar) {
                SeekBar seeker = this.mProgress;
                seeker.getProgressDrawable().setColorFilter(Utilities.getThemeCustomColor(R.attr.float_button_border_color), Mode.SRC_IN);
                seeker.getThumb().setColorFilter(Utilities.getThemeCustomColor(R.attr.float_button_border_color), Mode.SRC_IN);
                seeker.setOnSeekBarChangeListener(this.mSeekListener);
            }
            this.mProgress.setMax(1000);
        }
        this.mEndTime = (TextView) v.findViewById(R.id.time);
        this.mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        this.mFormatBuilder = new StringBuilder();
        this.mFormatter = new Formatter(this.mFormatBuilder, Locale.getDefault());
        installPrevNextListeners();
    }

    public void show() {
        show(sDefaultTimeout);
    }

    private void disableUnsupportedButtons() {
        if (this.mPlayer != null) {
            try {
                if (!(this.mPauseButton == null || this.mPlayer.canPause())) {
                    this.mPauseButton.setEnabled(false);
                }
                if (!(this.mRewButton == null || this.mPlayer.canSeekBackward())) {
                    this.mRewButton.setEnabled(false);
                    this.mRewButton.setVisibility(4);
                }
                if (this.mFfwdButton != null && !this.mPlayer.canSeekForward()) {
                    this.mFfwdButton.setEnabled(false);
                    this.mFfwdButton.setVisibility(4);
                }
            } catch (IncompatibleClassChangeError e) {
            }
        }
    }

    public void show(int timeout) {
        if (!(this.mShowing || this.mAnchor == null)) {
            setProgress();
            if (this.mPauseButton != null) {
                this.mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            this.mAnchor.addView(this, new LayoutParams(-1, -2, 80));
            this.mShowing = true;
        }
        updatePausePlay();
        updateFullScreen();
        this.mHandler.sendEmptyMessage(2);
        Message msg = this.mHandler.obtainMessage(1);
        if (timeout != 0) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(msg, (long) timeout);
        }
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public void hide() {
        if (this.mAnchor != null) {
            try {
                this.mAnchor.removeView(this);
                this.mHandler.removeMessages(2);
            } catch (IllegalArgumentException e) {
                Log.w("MediaController", "already removed");
            }
            this.mShowing = false;
        }
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        this.mFormatBuilder.setLength(0);
        if (hours > 0) {
            return this.mFormatter.format("%d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
        }
        return this.mFormatter.format("%02d:%02d", new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
    }

    private int setProgress() {
        if (this.mPlayer == null || this.mDragging) {
            return 0;
        }
        int position = this.mPlayer.getCurrentPosition();
        int duration = this.mPlayer.getDuration();
        if (this.mProgress != null) {
            if (duration > 0) {
                this.mProgress.setProgress((int) ((1000 * ((long) position)) / ((long) duration)));
            }
            this.mProgress.setSecondaryProgress(this.mPlayer.getBufferPercentage() * 10);
        }
        if (this.mEndTime != null) {
            this.mEndTime.setText(stringForTime(duration));
        }
        if (this.mCurrentTime == null) {
            return position;
        }
        this.mCurrentTime.setText(stringForTime(position));
        return position;
    }

    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mPlayer == null) {
            return true;
        }
        int keyCode = event.getKeyCode();
        boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == 0;
        if (keyCode == 79 || keyCode == 85 || keyCode == 62) {
            if (!uniqueDown) {
                return true;
            }
            doPauseResume();
            show(sDefaultTimeout);
            if (this.mPauseButton == null) {
                return true;
            }
            this.mPauseButton.requestFocus();
            return true;
        } else if (keyCode == TransportMediator.KEYCODE_MEDIA_PLAY) {
            if (!uniqueDown || this.mPlayer.isPlaying()) {
                return true;
            }
            this.mPlayer.start();
            updatePausePlay();
            show(sDefaultTimeout);
            return true;
        } else if (keyCode == 86 || keyCode == TransportMediator.KEYCODE_MEDIA_PAUSE) {
            if (!uniqueDown || !this.mPlayer.isPlaying()) {
                return true;
            }
            this.mPlayer.pause();
            updatePausePlay();
            show(sDefaultTimeout);
            return true;
        } else if (keyCode == 25 || keyCode == 24 || keyCode == 164) {
            return super.dispatchKeyEvent(event);
        } else {
            if (keyCode != 4 && keyCode != 82) {
                show(sDefaultTimeout);
                return super.dispatchKeyEvent(event);
            } else if (!uniqueDown) {
                return true;
            } else {
                hide();
                return true;
            }
        }
    }

    public void updatePausePlay() {
        if (this.mRoot != null && this.mPauseButton != null && this.mPlayer != null) {
            if (this.mPlayer.isPlaying()) {
                this.mPauseButton.setImageResource(R.drawable.ic_media_pause);
            } else {
                this.mPauseButton.setImageResource(R.drawable.ic_media_play);
            }
        }
    }

    public void updateFullScreen() {
        if (this.mRoot != null && this.mFullscreenButton != null && this.mPlayer != null) {
            if (this.mPlayer.isFullScreen()) {
                this.mFullscreenButton.setVisibility(8);
                return;
            }
            this.mFullscreenButton.setVisibility(0);
            this.mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_stretch);
        }
    }

    private void doPauseResume() {
        if (this.mPlayer != null) {
            if (this.mPlayer.isPlaying()) {
                this.mPlayer.pause();
            } else {
                this.mPlayer.start();
            }
            updatePausePlay();
        }
    }

    private void doToggleFullscreen() {
        if (this.mPlayer != null) {
            this.mPlayer.toggleFullScreen();
        }
    }

    public void setEnabled(boolean enabled) {
        boolean z = true;
        if (this.mPauseButton != null) {
            this.mPauseButton.setEnabled(enabled);
        }
        if (this.mFfwdButton != null) {
            this.mFfwdButton.setEnabled(enabled);
        }
        if (this.mRewButton != null) {
            this.mRewButton.setEnabled(enabled);
        }
        if (this.mNextButton != null) {
            ImageButton imageButton = this.mNextButton;
            boolean z2 = enabled && this.mNextListener != null;
            imageButton.setEnabled(z2);
        }
        if (this.mPrevButton != null) {
            ImageButton imageButton2 = this.mPrevButton;
            if (!enabled || this.mPrevListener == null) {
                z = false;
            }
            imageButton2.setEnabled(z);
        }
        if (this.mProgress != null) {
            this.mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private void installPrevNextListeners() {
        boolean z = true;
        if (this.mNextButton != null) {
            this.mNextButton.setOnClickListener(this.mNextListener);
            this.mNextButton.setEnabled(this.mNextListener != null);
        }
        if (this.mPrevButton != null) {
            this.mPrevButton.setOnClickListener(this.mPrevListener);
            ImageButton imageButton = this.mPrevButton;
            if (this.mPrevListener == null) {
                z = false;
            }
            imageButton.setEnabled(z);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
        this.mNextListener = next;
        this.mPrevListener = prev;
        this.mListenersSet = true;
        if (this.mRoot != null) {
            installPrevNextListeners();
            if (!(this.mNextButton == null || this.mFromXml)) {
                this.mNextButton.setVisibility(0);
            }
            if (this.mPrevButton != null && !this.mFromXml) {
                this.mPrevButton.setVisibility(0);
            }
        }
    }
}
