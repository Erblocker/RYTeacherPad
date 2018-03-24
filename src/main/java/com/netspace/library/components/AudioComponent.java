package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.library.controls.CustomAudioRecord;
import com.netspace.library.controls.CustomAudioRecord.OnRecordSendListener;
import com.netspace.library.controls.CustomAudioView;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.virtualnetworkobject.GetTemporaryStorageItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.pad.library.R;

public class AudioComponent extends CustomFrameLayout implements IComponents {
    private CustomAudioRecord mAudioRecord;
    private ComponentCallBack mCallBack;
    private ContextThemeWrapper mContextThemeWrapper;
    private String mFileName;
    private LinearLayout mLoadingLayout;
    private LinearLayout mPlayLayout;
    private LinearLayout mRecordLayout;
    private View mRootView;
    private TextView mTextViewMessage;
    private String mTotalTime;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();

    public AudioComponent(Context context) {
        super(context);
        initView();
    }

    public AudioComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AudioComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_audio, this, true);
        this.mPlayLayout = (LinearLayout) this.mRootView.findViewById(R.id.playLayout);
        this.mRecordLayout = (LinearLayout) this.mRootView.findViewById(R.id.recordLayout);
        this.mLoadingLayout = (LinearLayout) this.mRootView.findViewById(R.id.loadingLayout);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mPlayLayout.setVisibility(4);
        this.mLoadingLayout.setVisibility(4);
        this.mTextViewMessage.setVisibility(4);
        this.mRootView.findViewById(R.id.buttonRecord).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AudioComponent.this.showRecordUI();
            }
        });
    }

    public String getTotalTime() {
        return this.mTotalTime;
    }

    public void showRecordUI() {
        this.mPlayLayout.setVisibility(4);
        this.mLoadingLayout.setVisibility(4);
        this.mAudioRecord = new CustomAudioRecord(this.mContextThemeWrapper);
        this.mAudioRecord.setSendCallBack(new OnRecordSendListener() {
            public void OnRecordSend(String szKey, String szTotalTime) {
                AudioComponent.this.mFileName = szKey;
                AudioComponent.this.mTotalTime = szTotalTime;
                if (AudioComponent.this.mCallBack != null) {
                    AudioComponent.this.mCallBack.OnDataLoaded(AudioComponent.this.mFileName, AudioComponent.this);
                }
                AudioComponent.this.showPlayUI();
            }

            public void OnRecordCancel() {
                AudioComponent.this.mRecordLayout.removeView(AudioComponent.this.mAudioRecord);
                AudioComponent.this.mRecordLayout.findViewById(R.id.textView1).setVisibility(0);
                AudioComponent.this.mRecordLayout.findViewById(R.id.buttonRecord).setVisibility(0);
                AudioComponent.this.mAudioRecord = null;
            }
        });
        this.mRecordLayout.findViewById(R.id.textView1).setVisibility(8);
        this.mRecordLayout.findViewById(R.id.buttonRecord).setVisibility(8);
        this.mRecordLayout.addView(this.mAudioRecord, -1, -1);
        this.mAudioRecord.start();
    }

    public void showPlayUI() {
        GetTemporaryStorageItemObject ResourceObject = new GetTemporaryStorageItemObject(this.mFileName, null);
        final String szLocalFileName = new StringBuilder(String.valueOf(getContext().getExternalCacheDir().getAbsolutePath())).append("/").append(this.mFileName).toString();
        this.mRecordLayout.removeAllViews();
        this.mRecordLayout.setVisibility(4);
        this.mLoadingLayout.setVisibility(0);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                CustomAudioView AudioView = new CustomAudioView(AudioComponent.this.mContextThemeWrapper);
                AudioComponent.this.mPlayLayout.addView(AudioView, new LayoutParams(-1, -1));
                AudioView.setMainFileURL(szLocalFileName);
                AudioComponent.this.mPlayLayout.setVisibility(0);
                AudioComponent.this.mRecordLayout.setVisibility(4);
                AudioComponent.this.mLoadingLayout.setVisibility(4);
                if (AudioComponent.this.mCallBack != null) {
                    AudioComponent.this.mCallBack.OnDataLoaded(AudioComponent.this.mFileName, AudioComponent.this);
                }
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                AudioComponent.this.mRecordLayout.setVisibility(4);
                AudioComponent.this.mLoadingLayout.setVisibility(4);
                TextView textView = (TextView) AudioComponent.this.mRootView.findViewById(R.id.textViewMessage);
                textView.setVisibility(0);
                if (VirtualNetworkObject.getOfflineMode()) {
                    textView.setText("离线模式下内容获取失败。");
                } else {
                    textView.setText("内容获取失败，错误原因：" + ItemObject.getErrorText());
                }
            }
        });
        ResourceObject.setSaveToFile(true);
        ResourceObject.setTargetFileName(szLocalFileName);
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
        this.mVirtualNetworkObjectManager.add(ResourceObject);
    }

    public void setData(String szData) {
        this.mFileName = szData;
        if (this.mFileName == null) {
            this.mFileName = "";
        }
        if (!this.mFileName.isEmpty()) {
            showPlayUI();
        }
    }

    public String getData() {
        return this.mFileName;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
        if (this.mRecordLayout.findViewById(R.id.buttonRecord) != null) {
            this.mRecordLayout.findViewById(R.id.buttonRecord).setEnabled(!bLock);
        }
    }

    protected void onDetachedFromWindow() {
        this.mVirtualNetworkObjectManager.cancelAll();
        super.onDetachedFromWindow();
    }
}
