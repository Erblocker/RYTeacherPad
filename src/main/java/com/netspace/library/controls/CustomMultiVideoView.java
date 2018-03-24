package com.netspace.library.controls;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.netspace.library.controls.CustomMediaController.FullScreenControl;
import com.netspace.library.controls.CustomVideoView2.PlayPauseListener;
import com.netspace.library.servers.HttpGetProxy;
import com.netspace.pad.library.R;

public class CustomMultiVideoView extends LinearLayout implements PlayPauseListener, OnTouchListener {
    private Runnable mCheckBufferStateRunnable = new Runnable() {
        public void run() {
            int i;
            CustomVideoView2[] arrViews = new CustomVideoView2[]{CustomMultiVideoView.this.m_VideoView1, CustomMultiVideoView.this.m_VideoView2, CustomMultiVideoView.this.m_VideoView3};
            HttpGetProxy[] arrProxy = new HttpGetProxy[]{CustomMultiVideoView.this.m_Proxy1, CustomMultiVideoView.this.m_Proxy2, CustomMultiVideoView.this.m_Proxy3};
            int[] minCache = new int[]{CustomMultiVideoView.this.m_MinCache1, CustomMultiVideoView.this.m_MinCache2, CustomMultiVideoView.this.m_MinCache3};
            boolean bCanPlay = true;
            for (i = 0; i < arrViews.length; i++) {
                CustomVideoView2 OneView = arrViews[i];
                HttpGetProxy Proxy = arrProxy[i];
                if (OneView.getVisibility() == 0) {
                    Log.d("CustomMultiView", "id " + i + ", percent " + OneView.getPreparedPercent() + ", mincache=" + minCache[i] + "," + (OneView.getBuffering() ? "Buffering" : " buffered."));
                    if (OneView.getBuffering() || !OneView.getReadyToPlay()) {
                        bCanPlay = false;
                    }
                }
            }
            Log.d("CustomMultiView", "Can play? " + bCanPlay);
            if (!CustomMultiVideoView.this.m_bAllPlayStarted) {
                for (i = 0; i < arrViews.length; i++) {
                    OneView = arrViews[i];
                    Proxy = arrProxy[i];
                    if (OneView.getVisibility() == 0 && OneView.isPlaying() && OneView.getCurrentPosition() > 0) {
                        OneView.rawPause();
                    }
                }
                if (bCanPlay) {
                    for (i = 0; i < arrViews.length; i++) {
                        OneView = arrViews[i];
                        Proxy = arrProxy[i];
                        if (OneView.getVisibility() == 0) {
                            OneView.rawStart();
                        }
                    }
                }
            }
            CustomMultiVideoView.this.postDelayed(CustomMultiVideoView.this.mCheckBufferStateRunnable, 100);
        }
    };
    private Context m_Context;
    private CustomMediaController m_Control1;
    private CustomMediaController m_Control2;
    private CustomMediaController m_Control3;
    private CustomMediaController m_CurrentControl;
    private CustomVideoView2 m_CurrentMaxView;
    private RelativeLayout m_LayoutControls;
    private int m_MinCache1 = 0;
    private int m_MinCache2 = 0;
    private int m_MinCache3 = 0;
    private HttpGetProxy m_Proxy1;
    private HttpGetProxy m_Proxy2;
    private HttpGetProxy m_Proxy3;
    private CustomVideoView2 m_VideoView1;
    private CustomVideoView2 m_VideoView2;
    private CustomVideoView2 m_VideoView3;
    private boolean m_bAllPlayStarted = false;
    private int m_nAllPlayCachePercent = 0;

    public CustomMultiVideoView(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_custommultivideoview, this);
        this.m_Context = context;
        this.m_LayoutControls = (RelativeLayout) findViewById(R.id.LayoutControls);
        this.m_VideoView1 = (CustomVideoView2) findViewById(R.id.videoView1);
        this.m_VideoView2 = (CustomVideoView2) findViewById(R.id.videoView2);
        this.m_VideoView3 = (CustomVideoView2) findViewById(R.id.videoView3);
        this.m_Control1 = new CustomMediaController(this.m_Context, false);
        this.m_Control2 = new CustomMediaController(this.m_Context, false);
        this.m_Control3 = new CustomMediaController(this.m_Context, false);
        this.m_VideoView1.setMediaController(this.m_Control1);
        this.m_VideoView2.setMediaController(this.m_Control2);
        this.m_VideoView3.setMediaController(this.m_Control3);
        this.m_VideoView1.setOnTouchListener(this);
        this.m_VideoView2.setOnTouchListener(this);
        this.m_VideoView3.setOnTouchListener(this);
        this.m_Control1.setMediaPlayer(this.m_VideoView1);
        this.m_Control2.setMediaPlayer(this.m_VideoView2);
        this.m_Control3.setMediaPlayer(this.m_VideoView3);
        this.m_VideoView1.setVisibility(4);
        this.m_VideoView2.setVisibility(4);
        this.m_VideoView3.setVisibility(4);
        this.m_VideoView1.setPlayPauseListener(this);
        this.m_VideoView2.setPlayPauseListener(this);
        this.m_VideoView3.setPlayPauseListener(this);
    }

    public void setFullScreen(FullScreenControl FullScreen) {
        this.m_Control1.setFullScreen(FullScreen);
        this.m_Control2.setFullScreen(FullScreen);
        this.m_Control3.setFullScreen(FullScreen);
    }

    public void seekTo(int nTimeInMS) {
        if (this.m_VideoView1.getVisibility() == 0) {
            this.m_VideoView1.seekTo(nTimeInMS);
        }
        if (this.m_VideoView2.getVisibility() == 0) {
            this.m_VideoView2.seekTo(nTimeInMS);
        }
        if (this.m_VideoView3.getVisibility() == 0) {
            this.m_VideoView3.seekTo(nTimeInMS);
        }
    }

    public void setFileURL(String szURL, int nIndex) {
        if (nIndex == 0) {
            this.m_Proxy1 = new HttpGetProxy();
            this.m_Proxy1.initProxy(szURL);
            this.m_VideoView1.setVideoURI(Uri.parse(this.m_Proxy1.getLocalAddress()));
            this.m_VideoView1.setVisibility(0);
        } else if (nIndex == 1) {
            this.m_Proxy2 = new HttpGetProxy();
            this.m_Proxy2.initProxy(szURL);
            this.m_VideoView2.setVideoURI(Uri.parse(this.m_Proxy2.getLocalAddress()));
            this.m_VideoView2.setVisibility(0);
        } else if (nIndex == 2) {
            this.m_Proxy3 = new HttpGetProxy();
            this.m_Proxy3.initProxy(szURL);
            this.m_VideoView3.setVideoURI(Uri.parse(this.m_Proxy3.getLocalAddress()));
            this.m_VideoView3.setVisibility(0);
        }
    }

    public void start() {
        if (this.m_VideoView1.getVisibility() == 0) {
            this.m_VideoView1.rawStart();
        }
        if (this.m_VideoView2.getVisibility() == 0) {
            this.m_VideoView2.rawStart();
        }
        if (this.m_VideoView3.getVisibility() == 0) {
            this.m_VideoView3.rawStart();
        }
        postDelayed(this.mCheckBufferStateRunnable, 100);
    }

    public void stop() {
        if (this.m_VideoView1.getVisibility() == 0) {
            this.m_VideoView1.stopPlayback();
        }
        if (this.m_VideoView2.getVisibility() == 0) {
            this.m_VideoView2.stopPlayback();
        }
        if (this.m_VideoView3.getVisibility() == 0) {
            this.m_VideoView3.stopPlayback();
        }
    }

    public void onPlay() {
        if (this.m_VideoView1.getVisibility() == 0) {
            this.m_VideoView1.rawStart();
        }
        if (this.m_VideoView2.getVisibility() == 0) {
            this.m_VideoView2.rawStart();
        }
        if (this.m_VideoView3.getVisibility() == 0) {
            this.m_VideoView3.rawStart();
        }
        maxView(this.m_VideoView1);
    }

    public void onPause() {
        if (this.m_VideoView1.getVisibility() == 0) {
            this.m_VideoView1.rawPause();
        }
        if (this.m_VideoView2.getVisibility() == 0) {
            this.m_VideoView2.rawPause();
        }
        if (this.m_VideoView3.getVisibility() == 0) {
            this.m_VideoView3.rawPause();
        }
    }

    public void onSeekTo(int msec) {
        if (this.m_VideoView1.getVisibility() == 0) {
            this.m_VideoView1.rawSeekTo(msec);
            this.m_MinCache1 = (int) ((((float) msec) / ((float) this.m_VideoView1.getDuration())) * 100.0f);
        }
        if (this.m_VideoView2.getVisibility() == 0) {
            this.m_VideoView2.rawSeekTo(msec);
            this.m_MinCache2 = (int) ((((float) msec) / ((float) this.m_VideoView2.getDuration())) * 100.0f);
        }
        if (this.m_VideoView3.getVisibility() == 0) {
            this.m_VideoView3.rawSeekTo(msec);
            this.m_MinCache3 = (int) ((((float) msec) / ((float) this.m_VideoView3.getDuration())) * 100.0f);
        }
        this.m_bAllPlayStarted = false;
    }

    public void maxView(View v) {
        int nWidth = getWidth();
        int nHeight = getHeight();
        CustomVideoView2 MaxView = (CustomVideoView2) v;
        CustomVideoView2[] arrViews = new CustomVideoView2[]{this.m_VideoView1, this.m_VideoView2, this.m_VideoView3};
        CustomMediaController[] arrControl = new CustomMediaController[]{this.m_Control1, this.m_Control2, this.m_Control3};
        LayoutParams Params = (LayoutParams) MaxView.getLayoutParams();
        Params.leftMargin = 0;
        Params.rightMargin = 0;
        Params.topMargin = 0;
        Params.width = nWidth;
        Params.height = nHeight - 138;
        MaxView.setLayoutParams(Params);
        MaxView.layout(0, 0, nWidth, nHeight - 138);
        MaxView.resizeVideo(nWidth, nHeight - 138);
        Params = (LayoutParams) this.m_LayoutControls.getLayoutParams();
        Params.leftMargin = 0;
        Params.rightMargin = 0;
        Params.topMargin = 0;
        Params.width = nWidth;
        Params.height = nHeight - 138;
        this.m_LayoutControls.setLayoutParams(Params);
        this.m_LayoutControls.layout(0, 0, nWidth, nHeight - 138);
        int nLeftWidth = 0;
        this.m_Control1.setAnchorView(null);
        this.m_Control2.setAnchorView(null);
        this.m_Control3.setAnchorView(null);
        for (int i = 0; i < arrViews.length; i++) {
            View OneView = arrViews[i];
            if (OneView.getVisibility() == 0) {
                if (OneView == v) {
                    CustomMediaController Control = arrControl[i];
                    Control.setEnabled(true);
                    Control.setVisibility(0);
                    Control.setAnchorView(this.m_LayoutControls);
                    if (this.m_CurrentMaxView != null) {
                        this.m_CurrentMaxView.equals(OneView);
                    }
                    this.m_CurrentMaxView = OneView;
                    this.m_CurrentControl = Control;
                } else {
                    Params = (LayoutParams) OneView.getLayoutParams();
                    Params.leftMargin = nLeftWidth;
                    Params.width = 128;
                    Params.height = 128;
                    Params.topMargin = nHeight - 128;
                    nLeftWidth += Params.width + 10;
                    OneView.setLayoutParams(Params);
                    OneView.resizeVideo(128, 128);
                }
            }
        }
    }

    public void onBufferUpdate(CustomVideoView2 VideoView, int percent) {
    }

    public void relayout() {
        if (this.m_CurrentMaxView != null) {
            maxView(this.m_CurrentMaxView);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == 0) {
            maxView(v);
        }
        return false;
    }
}
