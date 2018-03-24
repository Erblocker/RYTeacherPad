package com.netspace.library.controls;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;

public class CustomAudioView extends LinearLayout implements OnClickListener, OnBufferingUpdateListener, OnCompletionListener, OnTouchListener {
    private Context m_Context;
    private final Handler m_Handler = new Handler();
    private ImageView m_ImageButtonPause;
    private ImageView m_ImageButtonPlay;
    private MediaPlayer m_MediaPlayer;
    private SeekBar m_SeekBar;
    private boolean m_bIsPrepared = false;
    private boolean m_bIsReleased = false;
    private int m_nFileLengthInMilliseconds;
    private String m_szMainFileURL;

    public CustomAudioView(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customaudioview, this);
        this.m_Context = context;
        this.m_ImageButtonPlay = (ImageButton) findViewById(R.id.ImageButtonPlay);
        this.m_ImageButtonPause = (ImageButton) findViewById(R.id.ImageButtonPause);
        this.m_ImageButtonPlay.setOnClickListener(this);
        this.m_ImageButtonPause.setOnClickListener(this);
        this.m_SeekBar = (SeekBar) findViewById(R.id.SeekBarPosition);
        this.m_SeekBar.setOnTouchListener(this);
        this.m_MediaPlayer = new MediaPlayer();
        this.m_MediaPlayer.setOnBufferingUpdateListener(this);
        this.m_MediaPlayer.setOnCompletionListener(this);
    }

    public void setMainFileURL(String szURL) {
        this.m_szMainFileURL = szURL;
        if (VirtualNetworkObject.getOfflineMode()) {
            String szLocalFileName = VirtualNetworkObject.getOfflineURL(this.m_szMainFileURL);
            if (szLocalFileName != null) {
                this.m_szMainFileURL = szLocalFileName;
            }
        }
    }

    public void onClick(View v) {
        Utilities.logClick(v, this.m_szMainFileURL);
        if (v.getId() == R.id.ImageButtonPlay) {
            if (!this.m_bIsPrepared) {
                try {
                    this.m_MediaPlayer.setDataSource(this.m_szMainFileURL);
                    this.m_MediaPlayer.prepare();
                    this.m_bIsPrepared = true;
                    this.m_nFileLengthInMilliseconds = this.m_MediaPlayer.getDuration();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (!this.m_MediaPlayer.isPlaying()) {
                this.m_MediaPlayer.start();
                primarySeekBarProgressUpdater();
            }
        } else if (v.getId() == R.id.ImageButtonPause && this.m_MediaPlayer.isPlaying()) {
            this.m_MediaPlayer.pause();
            primarySeekBarProgressUpdater();
        }
    }

    private void primarySeekBarProgressUpdater() {
        if (!this.m_bIsReleased) {
            this.m_SeekBar.setProgress((int) ((((float) this.m_MediaPlayer.getCurrentPosition()) / ((float) this.m_nFileLengthInMilliseconds)) * 100.0f));
            if (this.m_MediaPlayer.isPlaying()) {
                this.m_Handler.postDelayed(new Runnable() {
                    public void run() {
                        CustomAudioView.this.primarySeekBarProgressUpdater();
                    }
                }, 1000);
            }
        }
    }

    public void onCompletion(MediaPlayer mp) {
    }

    public void stopPlay() {
        if (!this.m_bIsReleased && this.m_bIsPrepared && this.m_MediaPlayer.isPlaying()) {
            this.m_MediaPlayer.stop();
        }
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        this.m_SeekBar.setSecondaryProgress(percent);
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.SeekBarPosition && this.m_MediaPlayer.isPlaying()) {
            int playPositionInMillisecconds = (this.m_nFileLengthInMilliseconds / 100) * ((SeekBar) v).getProgress();
            this.m_MediaPlayer.seekTo(playPositionInMillisecconds);
            Utilities.logClick(v, String.valueOf(playPositionInMillisecconds));
        }
        return false;
    }

    protected void onDetachedFromWindow() {
        if (!this.m_bIsReleased) {
            if (this.m_MediaPlayer.isPlaying()) {
                this.m_MediaPlayer.stop();
            }
            this.m_bIsReleased = true;
            this.m_MediaPlayer.release();
        }
        super.onDetachedFromWindow();
    }

    public void release() {
        this.m_bIsReleased = true;
        this.m_bIsPrepared = false;
        this.m_MediaPlayer.release();
    }
}
