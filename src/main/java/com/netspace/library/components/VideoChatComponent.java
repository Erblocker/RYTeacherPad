package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.VideoQuality;

public class VideoChatComponent extends FrameLayout implements IComponents, Callback, Session.Callback, OnClickListener {
    private static VideoChatCallBack mVideoChatCallBack;
    private ComponentCallBack mCallBack;
    private ContextThemeWrapper mContextThemeWrapper;
    private ImageButton mImageButtonSwitch;
    private View mRootView;
    private Session mSession;
    private SurfaceView mSurfaceView;
    private TextView mTextView;
    private TextView mTextViewSpeed;

    public interface VideoChatCallBack {
        void OnSessionReady(String str);
    }

    public VideoChatComponent(Context context) {
        super(context);
        initView();
    }

    public VideoChatComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public VideoChatComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_videochat, this, true);
        this.mSurfaceView = (SurfaceView) this.mRootView.findViewById(R.id.videoView2);
        this.mTextView = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextView.setVisibility(4);
        this.mTextViewSpeed = (TextView) this.mRootView.findViewById(R.id.textViewSpeed);
        this.mTextViewSpeed.setVisibility(4);
        this.mImageButtonSwitch = (ImageButton) this.mRootView.findViewById(R.id.imageButtonSwap);
        this.mImageButtonSwitch.setOnClickListener(this);
        this.mImageButtonSwitch.setVisibility(4);
        this.mSession = SessionBuilder.getInstance().setCallback(this).setSurfaceView(this.mSurfaceView).setPreviewOrientation(0).setContext(MyiBaseApplication.getBaseAppContext()).setAudioEncoder(5).setAudioQuality(new AudioQuality(16000, 32000)).setVideoEncoder(1).setCamera(1).setVideoQuality(new VideoQuality(640, 480, 15, 15000)).build();
        this.mSurfaceView.getHolder().addCallback(this);
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("BroadcastGetChatPort", null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szPort = ItemObject.readTextData();
                if (szPort != null && !szPort.isEmpty()) {
                    VideoChatComponent.this.setData(szPort);
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                VideoChatComponent.this.mTextView.setText("获取信息时出现错误，错误原因：" + ErrorCode.getErrorMessage(nReturnCode));
                VideoChatComponent.this.mTextView.setVisibility(0);
            }
        });
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    protected void onDetachedFromWindow() {
        this.mSession.release();
        super.onDetachedFromWindow();
    }

    public void setData(String szData) {
        int nStartPort = Integer.valueOf(szData).intValue();
        this.mSession.setDestination(MyiBaseApplication.getCommonVariables().ServerInfo.getServerHost());
        this.mSession.getVideoTrack().setDestinationPorts(nStartPort);
        this.mSession.getAudioTrack().setDestinationPorts(nStartPort + 2);
        if (this.mSession.isStreaming()) {
            this.mSession.stop();
        } else {
            this.mSession.configure();
        }
    }

    public String getData() {
        return null;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void onBitrateUpdate(long bitrate) {
        this.mTextViewSpeed.setText(new StringBuilder(String.valueOf((bitrate / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) / 8)).append("KB/s").toString());
        if (this.mTextViewSpeed.getVisibility() != 0) {
            this.mTextViewSpeed.setVisibility(0);
        }
    }

    public void onSessionError(int reason, int streamType, Exception e) {
    }

    public void onPreviewStarted() {
    }

    public static void setSessionCallBack(VideoChatCallBack CallBack) {
        mVideoChatCallBack = CallBack;
    }

    public void onSessionConfigured() {
        String szSDPContent = this.mSession.getSessionDescription();
        final String szSDPFileName = "chat_" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + ".sdp";
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("BroadcastUploadSDPFile", null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                VideoChatComponent.this.mSession.start();
                if (VideoChatComponent.mVideoChatCallBack != null) {
                    VideoChatComponent.mVideoChatCallBack.OnSessionReady(szSDPFileName);
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                VideoChatComponent.this.mTextView.setText("提交信息时出现错误，错误原因：" + ErrorCode.getErrorMessage(nReturnCode));
                VideoChatComponent.this.mTextView.setVisibility(0);
            }
        });
        CallItem.setAlwaysActiveCallbacks(true);
        CallItem.setParam("lpszFileName", szSDPFileName);
        CallItem.setParam("lpszFileContent", szSDPContent);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public void onSessionStarted() {
    }

    public void onSessionStopped() {
    }

    public void onClick(View v) {
        if (this.mSession.isStreaming()) {
            this.mSession.switchCamera();
        }
    }
}
