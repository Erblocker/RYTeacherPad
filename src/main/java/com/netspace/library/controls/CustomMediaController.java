package com.netspace.library.controls;

import android.content.Context;
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
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.Formatter;
import java.util.Locale;

public class CustomMediaController extends MediaController {
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int sDefaultTimeout = 3000;
    private ViewGroup mAnchor;
    private Context mContext;
    private TextView mCurrentTime;
    private View mDecor;
    private LayoutParams mDecorLayoutParams;
    private boolean mDragging;
    private TextView mEndTime;
    private ImageButton mFfwdButton;
    private OnClickListener mFfwdListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private boolean mFromXml;
    private FullScreenControl mFullScreen;
    private ImageButton mFullscreenButton;
    private OnClickListener mFullscreenListener;
    private Handler mHandler;
    private OnLayoutChangeListener mLayoutChangeListener;
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
    private OnTouchListener mTouchListener;
    private boolean mUseFastForward;
    private Window mWindow;
    private WindowManager mWindowManager;

    public interface FullScreenControl {
        boolean isFullScreen();

        void toggleFullScreen();
    }

    public CustomMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLayoutChangeListener = new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                CustomMediaController.this.updateFloatingWindowLayout();
                if (CustomMediaController.this.mShowing) {
                    CustomMediaController.this.mWindowManager.updateViewLayout(CustomMediaController.this.mDecor, CustomMediaController.this.mDecorLayoutParams);
                }
            }
        };
        this.mTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0 && CustomMediaController.this.mShowing) {
                    CustomMediaController.this.hide();
                }
                return false;
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        CustomMediaController.this.hide();
                        return;
                    case 2:
                        int pos = CustomMediaController.this.setProgress();
                        if (!CustomMediaController.this.mDragging && CustomMediaController.this.mShowing && CustomMediaController.this.mPlayer.isPlaying()) {
                            sendMessageDelayed(obtainMessage(2), (long) (1000 - (pos % 1000)));
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPauseListener = new OnClickListener() {
            public void onClick(View v) {
                CustomMediaController.this.doPauseResume();
                CustomMediaController.this.show(CustomMediaController.sDefaultTimeout);
            }
        };
        this.mFullscreenListener = new OnClickListener() {
            public void onClick(View v) {
                CustomMediaController.this.doToggleFullscreen();
                CustomMediaController.this.show(CustomMediaController.sDefaultTimeout);
            }
        };
        this.mSeekListener = new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {
                Utilities.logClick(bar, "onStartTrackingTouch");
                CustomMediaController.this.show(3600000);
                CustomMediaController.this.mDragging = true;
                CustomMediaController.this.mHandler.removeMessages(2);
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (fromuser) {
                    long newposition = (((long) progress) * ((long) CustomMediaController.this.mPlayer.getDuration())) / 1000;
                    CustomMediaController.this.mPlayer.seekTo((int) newposition);
                    if (CustomMediaController.this.mCurrentTime != null) {
                        CustomMediaController.this.mCurrentTime.setText(CustomMediaController.this.stringForTime((int) newposition));
                    }
                }
            }

            public void onStopTrackingTouch(SeekBar bar) {
                CustomMediaController.this.mDragging = false;
                Utilities.logClick(bar, "onStopTrackingTouch");
                CustomMediaController.this.setProgress();
                CustomMediaController.this.updatePausePlay();
                CustomMediaController.this.show(CustomMediaController.sDefaultTimeout);
                CustomMediaController.this.mHandler.sendEmptyMessage(2);
            }
        };
        this.mRewListener = new OnClickListener() {
            public void onClick(View v) {
                CustomMediaController.this.mPlayer.seekTo(CustomMediaController.this.mPlayer.getCurrentPosition() - 5000);
                CustomMediaController.this.setProgress();
                CustomMediaController.this.show(CustomMediaController.sDefaultTimeout);
            }
        };
        this.mFfwdListener = new OnClickListener() {
            public void onClick(View v) {
                CustomMediaController.this.mPlayer.seekTo(CustomMediaController.this.mPlayer.getCurrentPosition() + 15000);
                CustomMediaController.this.setProgress();
                CustomMediaController.this.show(CustomMediaController.sDefaultTimeout);
            }
        };
        this.mRoot = this;
        this.mContext = context;
        this.mUseFastForward = true;
        this.mFromXml = true;
    }

    public void onFinishInflate() {
        if (this.mRoot != null) {
            initControllerView(this.mRoot);
        }
    }

    public CustomMediaController(Context context, boolean useFastForward) {
        super(context);
        this.mLayoutChangeListener = /* anonymous class already generated */;
        this.mTouchListener = /* anonymous class already generated */;
        this.mHandler = /* anonymous class already generated */;
        this.mPauseListener = /* anonymous class already generated */;
        this.mFullscreenListener = /* anonymous class already generated */;
        this.mSeekListener = /* anonymous class already generated */;
        this.mRewListener = /* anonymous class already generated */;
        this.mFfwdListener = /* anonymous class already generated */;
        this.mContext = context;
        this.mUseFastForward = useFastForward;
    }

    public CustomMediaController(Context context) {
        this(context, true);
    }

    private void initFloatingWindow() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWindow.setWindowManager(this.mWindowManager, null, null);
        this.mWindow.requestFeature(1);
        this.mDecor = this.mWindow.getDecorView();
        this.mDecor.setOnTouchListener(this.mTouchListener);
        this.mWindow.setContentView(this);
        this.mWindow.setBackgroundDrawableResource(17170445);
        this.mWindow.setVolumeControlStream(3);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(262144);
        requestFocus();
    }

    private void initFloatingWindowLayout() {
        this.mDecorLayoutParams = new LayoutParams();
        LayoutParams p = this.mDecorLayoutParams;
        p.gravity = 48;
        p.height = -2;
        p.x = 0;
        p.format = -3;
        p.type = 1000;
        p.flags |= 8519712;
        p.token = null;
        p.windowAnimations = 0;
    }

    private void updateFloatingWindowLayout() {
        int[] anchorPos = new int[2];
        this.mAnchor.getLocationOnScreen(anchorPos);
        LayoutParams p = this.mDecorLayoutParams;
        p.width = this.mAnchor.getWidth();
        p.y = anchorPos[1] + this.mAnchor.getHeight();
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        this.mPlayer = player;
        updatePausePlay();
    }

    public void setAnchorView(ViewGroup view) {
        if (view == null) {
            hide();
            this.mAnchor = null;
            removeAllViews();
            return;
        }
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
        this.mAnchor = view;
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(-1, -1);
        removeAllViews();
        addView(makeControllerView(), frameParams);
    }

    public void setFullScreen(FullScreenControl FullScreen) {
        this.mFullScreen = FullScreen;
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
            if (this.mFullScreen == null) {
                this.mFullscreenButton.setVisibility(8);
            } else {
                this.mFullscreenButton.requestFocus();
                this.mFullscreenButton.setOnClickListener(this.mFullscreenListener);
            }
        }
        this.mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
        if (this.mFfwdButton != null) {
            this.mFfwdButton.setOnClickListener(this.mFfwdListener);
            if (!this.mFromXml) {
                int i2;
                ImageButton imageButton = this.mFfwdButton;
                if (this.mUseFastForward) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                imageButton.setVisibility(i2);
            }
        }
        this.mRewButton = (ImageButton) v.findViewById(R.id.rew);
        if (this.mRewButton != null) {
            this.mRewButton.setOnClickListener(this.mRewListener);
            if (!this.mFromXml) {
                ImageButton imageButton2 = this.mRewButton;
                if (!this.mUseFastForward) {
                    i = 8;
                }
                imageButton2.setVisibility(i);
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
                this.mProgress.setOnSeekBarChangeListener(this.mSeekListener);
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
        try {
            if (!(this.mPauseButton == null || this.mPlayer.canPause())) {
                this.mPauseButton.setEnabled(false);
            }
            if (!(this.mRewButton == null || this.mPlayer.canSeekBackward())) {
                this.mRewButton.setEnabled(false);
            }
            if (this.mFfwdButton != null && !this.mPlayer.canSeekForward()) {
                this.mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError e) {
        }
    }

    public void show(int timeout) {
        if (!(this.mShowing || this.mAnchor == null)) {
            setProgress();
            if (this.mPauseButton != null) {
                this.mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(-1, -2);
            tlp.addRule(12);
            this.mAnchor.addView(this, tlp);
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
        if (this.mAnchor != null && this.mShowing) {
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
        } else if (keyCode == 25 || keyCode == 24 || keyCode == 164 || keyCode == 27) {
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

    private void updatePausePlay() {
        if (this.mRoot != null && this.mPauseButton != null) {
            if (this.mPlayer.isPlaying()) {
                this.mPauseButton.setImageResource(R.drawable.ic_media_pause);
            } else {
                this.mPauseButton.setImageResource(R.drawable.ic_media_play);
            }
        }
    }

    public void updateFullScreen() {
        if (this.mRoot != null && this.mFullscreenButton != null && this.mPlayer != null && this.mFullScreen != null) {
            if (this.mFullScreen.isFullScreen()) {
                this.mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_shrink);
            } else {
                this.mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_stretch);
            }
        }
    }

    private void doPauseResume() {
        if (this.mPlayer.isPlaying()) {
            this.mPlayer.pause();
        } else {
            this.mPlayer.start();
        }
        updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (this.mPlayer != null) {
            this.mFullScreen.toggleFullScreen();
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

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(MediaController.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(MediaController.class.getName());
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
