package io.vov.vitamio.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.utils.StringUtils;

public class MediaController extends FrameLayout {
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int sDefaultTimeout = 3000;
    private AudioManager mAM;
    private View mAnchor;
    private int mAnimStyle;
    private Context mContext;
    private TextView mCurrentTime;
    private boolean mDragging;
    private long mDuration;
    private TextView mEndTime;
    private TextView mFileName;
    private boolean mFromXml;
    @SuppressLint({"HandlerLeak"})
    private Handler mHandler;
    private OnHiddenListener mHiddenListener;
    private OutlineTextView mInfoView;
    private boolean mInstantSeeking;
    private ImageButton mPauseButton;
    private OnClickListener mPauseListener;
    private MediaPlayerControl mPlayer;
    private SeekBar mProgress;
    private View mRoot;
    private OnSeekBarChangeListener mSeekListener;
    private boolean mShowing;
    private OnShownListener mShownListener;
    private String mTitle;
    private PopupWindow mWindow;

    public interface MediaPlayerControl {
        int getBufferPercentage();

        long getCurrentPosition();

        long getDuration();

        boolean isPlaying();

        void pause();

        void seekTo(long j);

        void start();
    }

    public interface OnHiddenListener {
        void onHidden();
    }

    public interface OnShownListener {
        void onShown();
    }

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInstantSeeking = false;
        this.mFromXml = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        MediaController.this.hide();
                        return;
                    case 2:
                        long pos = MediaController.this.setProgress();
                        if (!MediaController.this.mDragging && MediaController.this.mShowing) {
                            sendMessageDelayed(obtainMessage(2), 1000 - (pos % 1000));
                            MediaController.this.updatePausePlay();
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
                MediaController.this.doPauseResume();
                MediaController.this.show(MediaController.sDefaultTimeout);
            }
        };
        this.mSeekListener = new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {
                MediaController.this.mDragging = true;
                MediaController.this.show(3600000);
                MediaController.this.mHandler.removeMessages(2);
                if (MediaController.this.mInstantSeeking) {
                    MediaController.this.mAM.setStreamMute(3, true);
                }
                if (MediaController.this.mInfoView != null) {
                    MediaController.this.mInfoView.setText("");
                    MediaController.this.mInfoView.setVisibility(0);
                }
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (fromuser) {
                    long newposition = (MediaController.this.mDuration * ((long) progress)) / 1000;
                    String time = StringUtils.generateTime(newposition);
                    if (MediaController.this.mInstantSeeking) {
                        MediaController.this.mPlayer.seekTo(newposition);
                    }
                    if (MediaController.this.mInfoView != null) {
                        MediaController.this.mInfoView.setText(time);
                    }
                    if (MediaController.this.mCurrentTime != null) {
                        MediaController.this.mCurrentTime.setText(time);
                    }
                }
            }

            public void onStopTrackingTouch(SeekBar bar) {
                if (!MediaController.this.mInstantSeeking) {
                    MediaController.this.mPlayer.seekTo((MediaController.this.mDuration * ((long) bar.getProgress())) / 1000);
                }
                if (MediaController.this.mInfoView != null) {
                    MediaController.this.mInfoView.setText("");
                    MediaController.this.mInfoView.setVisibility(8);
                }
                MediaController.this.show(MediaController.sDefaultTimeout);
                MediaController.this.mHandler.removeMessages(2);
                MediaController.this.mAM.setStreamMute(3, false);
                MediaController.this.mDragging = false;
                MediaController.this.mHandler.sendEmptyMessageDelayed(2, 1000);
            }
        };
        this.mRoot = this;
        this.mFromXml = true;
        initController(context);
    }

    public MediaController(Context context) {
        super(context);
        this.mInstantSeeking = false;
        this.mFromXml = false;
        this.mHandler = /* anonymous class already generated */;
        this.mPauseListener = /* anonymous class already generated */;
        this.mSeekListener = /* anonymous class already generated */;
        if (!this.mFromXml && initController(context)) {
            initFloatingWindow();
        }
    }

    private boolean initController(Context context) {
        this.mContext = context;
        this.mAM = (AudioManager) this.mContext.getSystemService("audio");
        return true;
    }

    public void onFinishInflate() {
        if (this.mRoot != null) {
            initControllerView(this.mRoot);
        }
    }

    private void initFloatingWindow() {
        this.mWindow = new PopupWindow(this.mContext);
        this.mWindow.setFocusable(false);
        this.mWindow.setBackgroundDrawable(null);
        this.mWindow.setOutsideTouchable(true);
        this.mAnimStyle = 16973824;
    }

    @TargetApi(16)
    public void setWindowLayoutType() {
        if (VERSION.SDK_INT >= 14) {
            try {
                this.mAnchor.setSystemUiVisibility(512);
                PopupWindow.class.getMethod("setWindowLayoutType", new Class[]{Integer.TYPE}).invoke(this.mWindow, new Object[]{Integer.valueOf(1003)});
            } catch (Throwable e) {
                Log.e("setWindowLayoutType", e);
            }
        }
    }

    public void setAnchorView(View view) {
        this.mAnchor = view;
        if (!this.mFromXml) {
            removeAllViews();
            this.mRoot = makeControllerView();
            this.mWindow.setContentView(this.mRoot);
            this.mWindow.setWidth(-1);
            this.mWindow.setHeight(-2);
        }
        initControllerView(this.mRoot);
    }

    protected View makeControllerView() {
        return ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(getResources().getIdentifier("mediacontroller", "layout", this.mContext.getPackageName()), this);
    }

    private void initControllerView(View v) {
        this.mPauseButton = (ImageButton) v.findViewById(getResources().getIdentifier("mediacontroller_play_pause", "id", this.mContext.getPackageName()));
        if (this.mPauseButton != null) {
            this.mPauseButton.requestFocus();
            this.mPauseButton.setOnClickListener(this.mPauseListener);
        }
        this.mProgress = (SeekBar) v.findViewById(getResources().getIdentifier("mediacontroller_seekbar", "id", this.mContext.getPackageName()));
        if (this.mProgress != null) {
            if (this.mProgress instanceof SeekBar) {
                this.mProgress.setOnSeekBarChangeListener(this.mSeekListener);
            }
            this.mProgress.setMax(1000);
        }
        this.mEndTime = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_time_total", "id", this.mContext.getPackageName()));
        this.mCurrentTime = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_time_current", "id", this.mContext.getPackageName()));
        this.mFileName = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_file_name", "id", this.mContext.getPackageName()));
        if (this.mFileName != null) {
            this.mFileName.setText(this.mTitle);
        }
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        this.mPlayer = player;
        updatePausePlay();
    }

    public void setInstantSeeking(boolean seekWhenDragging) {
        this.mInstantSeeking = seekWhenDragging;
    }

    public void show() {
        show(sDefaultTimeout);
    }

    public void setFileName(String name) {
        this.mTitle = name;
        if (this.mFileName != null) {
            this.mFileName.setText(this.mTitle);
        }
    }

    public void setInfoView(OutlineTextView v) {
        this.mInfoView = v;
    }

    public void setAnimationStyle(int animationStyle) {
        this.mAnimStyle = animationStyle;
    }

    public void show(int timeout) {
        if (!(this.mShowing || this.mAnchor == null || this.mAnchor.getWindowToken() == null)) {
            if (this.mPauseButton != null) {
                this.mPauseButton.requestFocus();
            }
            if (this.mFromXml) {
                setVisibility(0);
            } else {
                int[] location = new int[2];
                this.mAnchor.getLocationOnScreen(location);
                Rect anchorRect = new Rect(location[0], location[1], location[0] + this.mAnchor.getWidth(), location[1] + this.mAnchor.getHeight());
                this.mWindow.setAnimationStyle(this.mAnimStyle);
                setWindowLayoutType();
                this.mWindow.showAtLocation(this.mAnchor, 0, anchorRect.left, anchorRect.bottom);
            }
            this.mShowing = true;
            if (this.mShownListener != null) {
                this.mShownListener.onShown();
            }
        }
        updatePausePlay();
        this.mHandler.sendEmptyMessage(2);
        if (timeout != 0) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), (long) timeout);
        }
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public void hide() {
        if (this.mAnchor != null && this.mShowing) {
            try {
                this.mHandler.removeMessages(2);
                if (this.mFromXml) {
                    setVisibility(8);
                } else {
                    this.mWindow.dismiss();
                }
            } catch (IllegalArgumentException e) {
                Log.d("MediaController already removed", new Object[0]);
            }
            this.mShowing = false;
            if (this.mHiddenListener != null) {
                this.mHiddenListener.onHidden();
            }
        }
    }

    public void setOnShownListener(OnShownListener l) {
        this.mShownListener = l;
    }

    public void setOnHiddenListener(OnHiddenListener l) {
        this.mHiddenListener = l;
    }

    private long setProgress() {
        if (this.mPlayer == null || this.mDragging) {
            return 0;
        }
        long position = this.mPlayer.getCurrentPosition();
        long duration = this.mPlayer.getDuration();
        if (this.mProgress != null) {
            if (duration > 0) {
                this.mProgress.setProgress((int) ((1000 * position) / duration));
            }
            this.mProgress.setSecondaryProgress(this.mPlayer.getBufferPercentage() * 10);
        }
        this.mDuration = duration;
        if (this.mEndTime != null) {
            this.mEndTime.setText(StringUtils.generateTime(this.mDuration));
        }
        if (this.mCurrentTime == null) {
            return position;
        }
        this.mCurrentTime.setText(StringUtils.generateTime(position));
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
        if (event.getRepeatCount() == 0 && (keyCode == 79 || keyCode == 85 || keyCode == 62)) {
            doPauseResume();
            show(sDefaultTimeout);
            if (this.mPauseButton == null) {
                return true;
            }
            this.mPauseButton.requestFocus();
            return true;
        } else if (keyCode == 86) {
            if (!this.mPlayer.isPlaying()) {
                return true;
            }
            this.mPlayer.pause();
            updatePausePlay();
            return true;
        } else if (keyCode == 4 || keyCode == 82) {
            hide();
            return true;
        } else {
            show(sDefaultTimeout);
            return super.dispatchKeyEvent(event);
        }
    }

    private void updatePausePlay() {
        if (this.mRoot != null && this.mPauseButton != null) {
            if (this.mPlayer.isPlaying()) {
                this.mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_pause", "drawable", this.mContext.getPackageName()));
            } else {
                this.mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_play", "drawable", this.mContext.getPackageName()));
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

    public void setEnabled(boolean enabled) {
        if (this.mPauseButton != null) {
            this.mPauseButton.setEnabled(enabled);
        }
        if (this.mProgress != null) {
            this.mProgress.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }
}
