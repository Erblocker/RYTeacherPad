package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class CustomVideoPlayerWrapper extends LinearLayout implements OnClickListener {
    protected static ArrayList<CustomVideoPlayerWrapper> marrPlayers = new ArrayList();
    private Context mContext;
    private ImageView mImageButtonPlay = ((ImageView) findViewById(R.id.imageView1));
    private ImageView mImageThumbnail = ((ImageView) findViewById(R.id.imageViewThumbnail));
    private long mLastPos;
    private RelativeLayout mLayout = ((RelativeLayout) findViewById(R.id.RelativeLayout1));
    private String mMainFileURL;
    private CustomVideoView4 mPlayerView;
    private boolean mbHasThumbnail;

    public CustomVideoPlayerWrapper(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customvideoplayerwrapper, this);
        this.mContext = context;
        this.mImageThumbnail.setVisibility(4);
        this.mImageButtonPlay.setOnClickListener(this);
    }

    public void setThumbnail(String szImageBase64) {
        Bitmap bitmap = Utilities.getBase64Bitmap(szImageBase64);
        if (bitmap != null) {
            this.mImageThumbnail.setImageBitmap(bitmap);
            this.mImageThumbnail.setVisibility(0);
            this.mbHasThumbnail = true;
        }
    }

    public void setMediaURL(String szURL) {
        this.mMainFileURL = szURL;
    }

    public void setMediaURL(String szURL, long nPlayPos) {
        this.mMainFileURL = szURL;
        this.mLastPos = nPlayPos;
    }

    public long getPos() {
        if (this.mPlayerView != null) {
            return this.mPlayerView.getPos();
        }
        return 0;
    }

    public String getMediaURL() {
        if (this.mPlayerView != null) {
            return this.mPlayerView.getMediaURL();
        }
        return this.mMainFileURL;
    }

    public boolean isPlaying() {
        if (this.mPlayerView != null) {
            return this.mPlayerView.isPlaying();
        }
        return false;
    }

    public void pause() {
        if (this.mPlayerView != null) {
            this.mPlayerView.pause();
        }
    }

    public void play() {
        if (this.mPlayerView != null) {
            this.mPlayerView.startPlay();
        }
    }

    public void onClick(View v) {
        Utilities.logClick(this.mImageButtonPlay, this.mMainFileURL);
        startPlay();
    }

    public void startPlay() {
        synchronized (marrPlayers) {
            for (int i = 0; i < marrPlayers.size(); i++) {
                ((CustomVideoPlayerWrapper) marrPlayers.get(i)).closePlayer();
            }
        }
        this.mPlayerView = new CustomVideoView4(this.mContext);
        this.mLayout.addView(this.mPlayerView, new LayoutParams(-1, -1));
        this.mPlayerView.setMediaURL(this.mMainFileURL, this.mLastPos);
        String szAlterURL = this.mPlayerView.getMediaURL();
        if (!szAlterURL.equalsIgnoreCase(this.mMainFileURL) && szAlterURL.startsWith("file:///")) {
            this.mMainFileURL = szAlterURL.substring(7);
        }
        this.mImageThumbnail.setVisibility(4);
        this.mImageButtonPlay.setVisibility(4);
    }

    public void closePlayer() {
        if (this.mPlayerView != null) {
            this.mLastPos = this.mPlayerView.getPos();
            if (this.mPlayerView.isPlaying()) {
                this.mPlayerView.stop();
            }
            this.mLayout.removeView(this.mPlayerView);
            this.mPlayerView = null;
            this.mImageButtonPlay.setVisibility(0);
            if (this.mbHasThumbnail) {
                this.mImageThumbnail.setVisibility(0);
            }
        }
    }

    protected void onDetachedFromWindow() {
        synchronized (marrPlayers) {
            for (int i = 0; i < marrPlayers.size(); i++) {
                if (((CustomVideoPlayerWrapper) marrPlayers.get(i)).equals(this)) {
                    marrPlayers.remove(i);
                    break;
                }
            }
        }
        super.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        synchronized (marrPlayers) {
            marrPlayers.add(this);
        }
        super.onAttachedToWindow();
    }
}
