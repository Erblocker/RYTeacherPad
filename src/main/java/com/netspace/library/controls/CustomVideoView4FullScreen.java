package com.netspace.library.controls;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.VideoView;
import com.netspace.library.activity.VideoPlayerActivity2;
import com.netspace.library.controls.VideoControllerView.MediaPlayerControl;
import com.netspace.pad.library.R;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import java.io.File;
import java.util.ArrayList;
import net.sqlcipher.database.SQLiteDatabase;

public class CustomVideoView4FullScreen extends LinearLayout implements MediaPlayerControl {
    private static ArrayList<CustomVideoView4FullScreen> marrVideoPlayers = new ArrayList();
    private int mBufferPercent;
    private Context mContext;
    private VideoControllerView mControllerView;
    private String mFilePath;
    private Uri mFileURI;
    private String mFileURL;
    private FrameLayout mFrameLayout;
    private CustomVideoView3 mVideoView;
    private VideoView mVideoView2;
    private boolean mbCanResize = false;
    private boolean mbFileLoaded = false;
    private boolean mbFullScreen = false;
    private boolean mbNeedReload = false;

    public CustomVideoView4FullScreen(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public CustomVideoView4FullScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    public CustomVideoView4FullScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initView();
    }

    public void initView() {
        View RootView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.layout_customvideoview4fullscreen, this);
        this.mFrameLayout = (FrameLayout) RootView.findViewById(R.id.frameLayout1);
        this.mVideoView = (CustomVideoView3) RootView.findViewById(R.id.videoView1);
        this.mControllerView = new VideoControllerView(this.mContext);
        this.mControllerView.setAnchorView(this.mFrameLayout);
        this.mControllerView.setMediaPlayer(this);
    }

    public void setFullScreen(boolean bFullScreen) {
        this.mbFullScreen = bFullScreen;
    }

    public boolean setFileToPlay(String szFilePath) {
        this.mFilePath = szFilePath;
        this.mFileURI = Uri.fromFile(new File(szFilePath));
        if (this.mVideoView != null) {
            this.mVideoView.setVideoURI(this.mFileURI);
        }
        if (this.mVideoView2 != null) {
            this.mVideoView2.setVideoURI(this.mFileURI);
        }
        this.mbFileLoaded = true;
        return true;
    }

    public boolean setUrlToPlay(String szUrl) {
        this.mFileURL = szUrl;
        this.mFileURI = Uri.parse(szUrl);
        if (this.mVideoView != null) {
            this.mVideoView.setVideoURI(this.mFileURI);
        }
        if (this.mVideoView2 != null) {
            this.mVideoView2.setVideoURI(this.mFileURI);
        }
        this.mbFileLoaded = true;
        replaceURLWithLocalFile();
        return true;
    }

    public void setMediaURL(String videoURL) {
        setUrlToPlay(videoURL);
    }

    public void setMediaURL(String szURL, long nPlayPos) {
        setUrlToPlay(szURL);
        if (this.mVideoView != null) {
            this.mVideoView.seekTo(nPlayPos);
        }
        if (this.mVideoView2 != null) {
            this.mVideoView2.seekTo((int) nPlayPos);
        }
    }

    public String getMediaURL() {
        return this.mFileURL;
    }

    protected void replaceURLWithLocalFile() {
        String szURL = this.mFileURL;
        String szFileName = new StringBuilder(String.valueOf(getContext().getExternalCacheDir().getAbsolutePath())).append("/").append(szURL.substring(szURL.lastIndexOf("/") + 1)).toString();
        if (new File(szFileName).exists()) {
            this.mFileURI = Uri.fromFile(new File(szFileName));
            if (this.mVideoView != null) {
                this.mVideoView.setVideoURI(this.mFileURI);
            }
            if (this.mVideoView2 != null) {
                this.mVideoView2.setVideoURI(this.mFileURI);
            }
            this.mbFileLoaded = true;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        this.mControllerView.show();
        return false;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return false;
    }

    public boolean canSeekForward() {
        return false;
    }

    public int getBufferPercentage() {
        return this.mBufferPercent;
    }

    public int getCurrentPosition() {
        if (this.mVideoView != null) {
            return (int) this.mVideoView.getCurrentPosition();
        }
        if (this.mVideoView2 != null) {
            return this.mVideoView2.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (this.mVideoView != null) {
            return (int) this.mVideoView.getDuration();
        }
        if (this.mVideoView2 != null) {
            return this.mVideoView2.getDuration();
        }
        return 0;
    }

    public boolean isPlaying() {
        if (this.mVideoView != null) {
            return this.mVideoView.isPlaying();
        }
        if (this.mVideoView2 != null) {
            return this.mVideoView2.isPlaying();
        }
        return false;
    }

    public void pause() {
        if (this.mVideoView != null) {
            this.mVideoView.pause();
        }
        if (this.mVideoView2 != null) {
            this.mVideoView2.pause();
        }
    }

    public void seekTo(int i) {
        if (this.mVideoView != null) {
            this.mVideoView.seekTo((long) i);
        }
        if (this.mVideoView2 != null) {
            this.mVideoView2.seekTo(i);
        }
    }

    public void startPlay() {
        start();
    }

    public void start() {
        synchronized (marrVideoPlayers) {
            for (int i = 0; i < marrVideoPlayers.size(); i++) {
                CustomVideoView4FullScreen OnePlayer = (CustomVideoView4FullScreen) marrVideoPlayers.get(i);
                if (!(OnePlayer.equals(this) || OnePlayer.mFileURI == null)) {
                    if (OnePlayer.mVideoView != null) {
                        OnePlayer.mVideoView.stopPlayback();
                    }
                    if (OnePlayer.mVideoView2 != null) {
                        OnePlayer.mVideoView2.stopPlayback();
                    }
                    OnePlayer.mbNeedReload = true;
                }
            }
        }
        if (this.mFileURI != null && this.mbNeedReload) {
            if (this.mVideoView != null) {
                this.mVideoView.setVideoURI(this.mFileURI);
            }
            if (this.mVideoView2 != null) {
                this.mVideoView2.setVideoURI(this.mFileURI);
            }
            this.mbNeedReload = false;
        }
        if (this.mVideoView != null) {
            this.mVideoView.postDelayed(new Runnable() {
                public void run() {
                    if (CustomVideoView4FullScreen.this.mVideoView.getDuration() > 0 && CustomVideoView4FullScreen.this.mVideoView.getCurrentPosition() == CustomVideoView4FullScreen.this.mVideoView.getDuration()) {
                        CustomVideoView4FullScreen.this.mVideoView.seekTo(0);
                    }
                    CustomVideoView4FullScreen.this.mVideoView.setVideoLayout(2, 0.0f);
                    CustomVideoView4FullScreen.this.mVideoView.start();
                }
            }, 500);
        }
        if (this.mVideoView2 != null) {
            this.mVideoView2.postDelayed(new Runnable() {
                public void run() {
                    CustomVideoView4FullScreen.this.mVideoView2.start();
                }
            }, 500);
        }
    }

    public boolean isFullScreen() {
        return this.mbFullScreen;
    }

    public long getPos() {
        if (this.mVideoView != null) {
            return this.mVideoView.getCurrentPosition();
        }
        if (this.mVideoView2 != null) {
            return (long) this.mVideoView2.getCurrentPosition();
        }
        return -1;
    }

    public void stop() {
        if (this.mVideoView != null) {
            this.mVideoView.stopPlayback();
        }
        if (this.mVideoView2 != null) {
            this.mVideoView2.stopPlayback();
        }
    }

    public void setOnErrorListener(OnErrorListener OnErrorListener) {
        if (this.mVideoView != null) {
            this.mVideoView.setOnErrorListener(OnErrorListener);
        }
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener OnErrorListener) {
        if (this.mVideoView2 != null) {
            this.mVideoView2.setOnErrorListener(OnErrorListener);
        }
    }

    public void toggleFullScreen() {
        Intent intent = new Intent(this.mContext, VideoPlayerActivity2.class);
        if (this.mVideoView != null) {
            this.mVideoView.stopPlayback();
        }
        if (this.mVideoView2 != null) {
            this.mVideoView2.stopPlayback();
        }
        intent.setAction("org.videolan.vlc.gui.video.PLAY_FROM_VIDEOGRID");
        if (this.mFileURL == null) {
            intent.putExtra("itemLocation", Uri.fromFile(new File(this.mFilePath)).toString());
        } else {
            intent.putExtra("itemLocation", this.mFileURL);
        }
        intent.putExtra("itemTitle", "视频播放");
        if (this.mVideoView != null) {
            intent.putExtra("position", this.mVideoView.getCurrentPosition());
        }
        if (this.mVideoView2 != null) {
            intent.putExtra("position", (long) this.mVideoView2.getCurrentPosition());
        }
        intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        this.mContext.startActivity(intent);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getWidth() > 0 && getHeight() > 0) {
            if ((this.mbFileLoaded && this.mVideoView.isPlaying()) || this.mbCanResize) {
                this.mbCanResize = true;
                if (this.mVideoView == null) {
                }
            }
        }
    }

    protected void onAttachedToWindow() {
        synchronized (marrVideoPlayers) {
            boolean bFound = false;
            for (int i = 0; i < marrVideoPlayers.size(); i++) {
                if (((CustomVideoView4FullScreen) marrVideoPlayers.get(i)).equals(this)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                marrVideoPlayers.add(this);
            }
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        synchronized (marrVideoPlayers) {
            for (int i = 0; i < marrVideoPlayers.size(); i++) {
                if (((CustomVideoView4FullScreen) marrVideoPlayers.get(i)).equals(this)) {
                    marrVideoPlayers.remove(i);
                    break;
                }
            }
        }
        if (this.mVideoView != null) {
            this.mVideoView.stopPlayback();
        }
        super.onDetachedFromWindow();
    }

    public void setVideoLayout(int mLayout, int i) {
        if (this.mVideoView != null) {
            this.mVideoView.setVideoLayout(mLayout, (float) i);
        }
    }
}
