package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomVideoView4;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.threads.LocalFileUploader;
import com.netspace.library.threads.LocalFileUploader.FileUploaderListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.pad.library.R;
import org.apache.http.HttpHost;

public class VideoComponent extends FrameLayout implements IComponents {
    private ComponentCallBack mCallBack;
    private ContextThemeWrapper mContextThemeWrapper;
    private String mFileName;
    private String mFileURL;
    private ImageView mImageViewPlay;
    private View mRootView;
    private CustomVideoView4 mVideoView;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();

    public VideoComponent(Context context) {
        super(context);
        initView();
    }

    public VideoComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public VideoComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_video, this, true);
        this.mVideoView = (CustomVideoView4) this.mRootView.findViewById(R.id.videoView2);
        this.mVideoView.switchToAndroidVideoView();
        this.mImageViewPlay = (ImageView) this.mRootView.findViewById(R.id.imageViewPlay);
        this.mImageViewPlay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                Utilities.checkAndWarnNetworkType(new Runnable() {
                    public void run() {
                        VideoComponent.this.mVideoView.setVisibility(0);
                        VideoComponent.this.mVideoView.setUrlToPlay(VideoComponent.this.mFileURL);
                        VideoComponent.this.mVideoView.start();
                        VideoComponent.this.mImageViewPlay.setVisibility(4);
                    }
                });
            }
        });
        this.mVideoView.setVisibility(4);
        this.mImageViewPlay.setVisibility(4);
        this.mVideoView.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (VideoComponent.this.mVideoView.getVisibility() != 0) {
                    return false;
                }
                VideoComponent.this.mRootView.findViewById(R.id.videoView2).setVisibility(4);
                VideoComponent.this.mRootView.findViewById(R.id.recordLayout).setVisibility(4);
                VideoComponent.this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                TextView textView = (TextView) VideoComponent.this.mRootView.findViewById(R.id.textViewMessage);
                textView.setVisibility(0);
                textView.setText("视频播放出现错误，错误代码：" + String.valueOf(extra));
                return true;
            }
        });
        this.mRootView.findViewById(R.id.recordLayout).setVisibility(0);
        this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
        this.mRootView.findViewById(R.id.textViewMessage).setVisibility(4);
        this.mRootView.findViewById(R.id.buttonRecord).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                Intent intent = new Intent();
                if (VideoComponent.this.mCallBack != null) {
                    for (int i = 0; i < VideoComponent.this.mVideoView.getNeighbourPlayers().size(); i++) {
                        View oneView = (View) ((View) VideoComponent.this.mVideoView.getNeighbourPlayers().get(i)).getParent();
                        if (oneView != null) {
                            oneView = (View) oneView.getParent();
                        }
                        if (oneView != null && (oneView instanceof VideoComponent)) {
                            ((VideoComponent) oneView).reset();
                        }
                    }
                    VideoComponent.this.mCallBack.OnRequestIntent(intent, VideoComponent.this);
                }
            }
        });
    }

    public void reset() {
        if (!this.mFileName.isEmpty() && this.mVideoView.getVisibility() == 0) {
            this.mVideoView.setVisibility(8);
            this.mVideoView.stop();
            this.mImageViewPlay.setVisibility(0);
        }
    }

    public void setEnableTools(boolean bEnable) {
        this.mVideoView.getControllerView().setEnabled(bEnable);
    }

    public void setData(String szData) {
        this.mFileName = szData;
        if (this.mFileName == null) {
            this.mFileName = "";
        }
        if (!this.mFileName.isEmpty()) {
            if (this.mFileName.startsWith("rtsp") || this.mFileName.startsWith(HttpHost.DEFAULT_SCHEME_NAME)) {
                this.mVideoView.switchToNormalVideoView();
                this.mVideoView.setVisibility(0);
                if (this.mFileName.startsWith("rtsp")) {
                    this.mVideoView.getNormalVideoView().setBufferSize(5120);
                }
                this.mVideoView.setUrlToPlay(this.mFileName);
                this.mRootView.findViewById(R.id.recordLayout).setVisibility(4);
                this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                this.mImageViewPlay.setVisibility(4);
                Log.d("VideoWindow", "RealPath=" + this.mFileName);
                if (this.mFileName.startsWith("rtsp")) {
                    this.mVideoView.getControllerView().setAnchorView(null);
                }
                if (this.mCallBack != null) {
                    this.mCallBack.OnDataLoaded(this.mFileName, this);
                    return;
                }
                return;
            }
            this.mFileURL = MyiBaseApplication.getProtocol() + "://" + VirtualNetworkObject.getServerAddress() + "/GetTemporaryStorage?binary=1&filename=" + szData;
            this.mVideoView.setVisibility(4);
            this.mRootView.findViewById(R.id.recordLayout).setVisibility(4);
            this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
            this.mImageViewPlay.setVisibility(0);
            if (this.mCallBack != null) {
                this.mCallBack.OnDataLoaded(this.mFileName, this);
            }
        }
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
        String szFileName = intent.getData().getPath();
        String szKey = "CameraVideo_" + Utilities.createGUID() + ".mp4";
        this.mFileName = szKey;
        this.mVideoView.setVisibility(0);
        this.mVideoView.setFileToPlay(szFileName);
        this.mRootView.findViewById(R.id.recordLayout).setVisibility(4);
        this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
        Log.d("VideoWindow", "RealPath=" + szFileName);
        String szURL = MyiBaseApplication.getProtocol() + "://" + VirtualNetworkObject.getServerAddress() + "/PutTemporaryStorage?binary=1&filename=" + szKey;
        this.mFileURL = MyiBaseApplication.getProtocol() + "://" + VirtualNetworkObject.getServerAddress() + "/GetTemporaryStorage?binary=1&filename=" + szKey;
        new LocalFileUploader(szURL, szFileName, false, new FileUploaderListener() {
            public void onFileUploadedSuccess(LocalFileUploader Uploader) {
                Utilities.runOnUIThread(VideoComponent.this.getContext(), new Runnable() {
                    public void run() {
                        VideoComponent.this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                        VideoComponent.this.mRootView.findViewById(R.id.textViewMessage).setVisibility(4);
                        if (VideoComponent.this.mCallBack != null) {
                            VideoComponent.this.mCallBack.OnDataUploaded(VideoComponent.this.mFileName, VideoComponent.this);
                        }
                    }
                });
            }

            public void onFileUploadedFail(LocalFileUploader Uploader) {
                final String szError = Uploader.getErrorText();
                Utilities.runOnUIThread(VideoComponent.this.getContext(), new Runnable() {
                    public void run() {
                        VideoComponent.this.mRootView.findViewById(R.id.recordLayout).setVisibility(4);
                        VideoComponent.this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                        TextView textView = (TextView) VideoComponent.this.mRootView.findViewById(R.id.textViewMessage);
                        textView.setVisibility(0);
                        textView.setText("数据上传失败，错误原因：" + szError);
                    }
                });
            }

            public void onFileUploadProgress(LocalFileUploader Uploader, int nCurrentPos, int nMaxPos) {
                final float fPercent = (((float) nCurrentPos) / ((float) nMaxPos)) * 100.0f;
                Utilities.runOnUIThread(VideoComponent.this.getContext(), new Runnable() {
                    public void run() {
                        TextView textView = (TextView) VideoComponent.this.mRootView.findViewById(R.id.textViewMessage);
                        textView.setVisibility(0);
                        textView.setText("正在上传数据(" + String.valueOf((int) fPercent) + "%)...");
                    }
                });
            }
        }).start();
        this.mRootView.findViewById(R.id.loadingLayout).setVisibility(0);
        TextView textView = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        textView.setVisibility(0);
        textView.setText("正在上传数据...");
    }

    public String getData() {
        return this.mFileName;
    }

    public void setLocked(boolean bLock) {
        this.mRootView.findViewById(R.id.buttonRecord).setEnabled(!bLock);
    }

    protected void onDetachedFromWindow() {
        this.mVirtualNetworkObjectManager.cancelAll();
        super.onDetachedFromWindow();
    }
}
