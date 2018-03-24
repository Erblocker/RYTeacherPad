package com.netspace.library.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.adapter.LessonClassExpandableListAdapter;
import com.netspace.library.controls.CustomMediaController.FullScreenControl;
import com.netspace.library.controls.CustomMultiVideoView;
import com.netspace.library.controls.CustomVideoView4;
import com.netspace.library.parser.LessonClassParser;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.struct.LessonClassItemData;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class LessonClassViewActivity extends BaseActivity implements OnClickListener, OnChildClickListener, FullScreenControl {
    private LessonClassExpandableListAdapter mAdapter;
    private Button mButtonBackCamera;
    private Button mButtonCamera;
    private Button mButtonMain;
    private Button mButtonMix;
    private Context mContext;
    private String mCurrentURL = "";
    private boolean mFullScreen = false;
    private ExpandableListView mListView;
    private LessonClassParser mParser;
    private CustomVideoView4 mPlayView;
    private CustomMultiVideoView mPlayView2;
    private ArrayList<LessonClassItemData> mTimelineData = new ArrayList();
    private String mVideoBackCameraURL = "";
    private String mVideoCameraURL = "";
    private LinearLayout mVideoLayout;
    private String mVideoMainURL = "";
    private String mVideoMixURL = "";
    private boolean mVideoOK = false;
    private View mViewSperator;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessonclassview);
        this.mContext = this;
        this.mVideoLayout = (LinearLayout) findViewById(R.id.LayoutVideo);
        this.mListView = (ExpandableListView) findViewById(R.id.ListViewActions);
        this.mViewSperator = findViewById(R.id.view1);
        this.mButtonMain = (Button) findViewById(R.id.buttonScreen);
        this.mButtonCamera = (Button) findViewById(R.id.buttonCamera);
        this.mButtonBackCamera = (Button) findViewById(R.id.buttonBackCamera);
        this.mButtonMix = (Button) findViewById(R.id.buttonMix);
        this.mButtonMain.setOnClickListener(this);
        this.mButtonCamera.setOnClickListener(this);
        this.mButtonBackCamera.setOnClickListener(this);
        this.mButtonMix.setOnClickListener(this);
        String szGUID = "";
        if (getIntent().getExtras() != null) {
            szGUID = getIntent().getExtras().getString("guid");
        }
        ResourceItemObject ResourceItem = new ResourceItemObject(szGUID, this);
        ResourceItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szResult = ItemObject.readTextData();
                if (szResult == null || szResult.isEmpty()) {
                    LessonClassViewActivity.this.reportError("无法获得数据", "无法打开这个课堂实录的资源。");
                    LessonClassViewActivity.this.mListView.setVisibility(8);
                    if (LessonClassViewActivity.this.mPlayView != null) {
                        LessonClassViewActivity.this.mPlayView.setVisibility(8);
                        return;
                    }
                    return;
                }
                LessonClassViewActivity.this.mParser = new LessonClassParser(ItemObject.readTextData());
                if (LessonClassViewActivity.this.mParser.readTimeLines(LessonClassViewActivity.this.mTimelineData)) {
                    LessonClassViewActivity.this.mAdapter = new LessonClassExpandableListAdapter(LessonClassViewActivity.this.mContext, LessonClassViewActivity.this.mTimelineData, LessonClassViewActivity.this);
                    LessonClassViewActivity.this.mListView.setAdapter(LessonClassViewActivity.this.mAdapter);
                    LessonClassViewActivity.this.mListView.setOnChildClickListener(LessonClassViewActivity.this);
                }
                String szURL = LessonClassViewActivity.this.mParser.getVideoURL();
                ArrayList<String> arrOtherURLs = new ArrayList();
                LessonClassViewActivity.this.mVideoCameraURL = LessonClassViewActivity.this.mParser.getCameraURL();
                LessonClassViewActivity.this.mVideoBackCameraURL = LessonClassViewActivity.this.mParser.getBackCameraURL();
                if (LessonClassViewActivity.this.mVideoCameraURL.isEmpty() && LessonClassViewActivity.this.mVideoBackCameraURL.isEmpty()) {
                    LessonClassViewActivity.this.mButtonMain.setVisibility(8);
                    LessonClassViewActivity.this.mButtonMix.setVisibility(8);
                    LessonClassViewActivity.this.mButtonCamera.setVisibility(8);
                    LessonClassViewActivity.this.mButtonBackCamera.setVisibility(8);
                } else {
                    LessonClassViewActivity.this.mVideoMainURL = szURL;
                    LessonClassViewActivity.this.mVideoMixURL = LessonClassViewActivity.this.mParser.getMixVideo();
                    if (LessonClassViewActivity.this.mVideoCameraURL.isEmpty()) {
                        LessonClassViewActivity.this.mButtonCamera.setVisibility(8);
                    }
                    if (LessonClassViewActivity.this.mVideoBackCameraURL.isEmpty()) {
                        LessonClassViewActivity.this.mButtonBackCamera.setVisibility(8);
                    }
                    if (LessonClassViewActivity.this.mVideoMixURL.isEmpty()) {
                        LessonClassViewActivity.this.mButtonMix.setVisibility(8);
                    }
                }
                LessonClassViewActivity.this.mPlayView = new CustomVideoView4(LessonClassViewActivity.this);
                LessonClassViewActivity.this.mVideoLayout.addView(LessonClassViewActivity.this.mPlayView, 0);
                LayoutParams Params = (LayoutParams) LessonClassViewActivity.this.mPlayView.getLayoutParams();
                Params.width = (int) (((float) Utilities.getScreenWidth(LessonClassViewActivity.this.mContext)) * 0.5859375f);
                Params.height = (int) (((float) Utilities.getScreenHeight(LessonClassViewActivity.this.mContext)) * 0.6510417f);
                Params.gravity = 1;
                LessonClassViewActivity.this.mPlayView.setLayoutParams(Params);
                if (szURL != null || !szURL.isEmpty()) {
                    LessonClassViewActivity.this.mPlayView.setMediaURL(szURL);
                    LessonClassViewActivity.this.mCurrentURL = szURL;
                    LessonClassViewActivity.this.mVideoOK = true;
                }
            }
        });
        VirtualNetworkObject.addToQueue(ResourceItem);
    }

    protected void onDestroy() {
        if (this.mVideoOK) {
            if (this.mPlayView != null) {
                this.mPlayView.stop();
            }
            if (this.mPlayView2 != null) {
                this.mPlayView2.stop();
            }
        }
        super.onDestroy();
    }

    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void onClick(View v) {
        String szTargetURL = "";
        long nCurrentTime = this.mPlayView.getPos();
        if (v.getId() == R.id.buttonScreen || v.getId() == R.id.buttonCamera || v.getId() == R.id.buttonBackCamera || v.getId() == R.id.buttonMix) {
            if (v.getId() == R.id.buttonScreen) {
                szTargetURL = this.mVideoMainURL;
            } else if (v.getId() == R.id.buttonCamera) {
                szTargetURL = this.mVideoCameraURL;
            } else if (v.getId() == R.id.buttonBackCamera) {
                szTargetURL = this.mVideoBackCameraURL;
            } else if (v.getId() == R.id.buttonMix) {
                szTargetURL = this.mVideoMixURL;
            }
            if (!this.mCurrentURL.equalsIgnoreCase(szTargetURL)) {
                this.mPlayView.stop();
                this.mVideoLayout.removeView(this.mPlayView);
                this.mPlayView = null;
                this.mPlayView = new CustomVideoView4(this);
                this.mVideoLayout.addView(this.mPlayView, 0);
                LayoutParams Params = (LayoutParams) this.mPlayView.getLayoutParams();
                Params.width = (int) (((float) Utilities.getScreenWidth(this.mContext)) * 0.5859375f);
                Params.height = (int) (((float) Utilities.getScreenHeight(this.mContext)) * 0.6510417f);
                Params.gravity = 1;
                this.mPlayView.setLayoutParams(Params);
                this.mPlayView.setMediaURL(szTargetURL, nCurrentTime);
                this.mCurrentURL = szTargetURL;
                return;
            }
            return;
        }
        int nTimeOffset = ((Integer) v.getTag()).intValue();
        if (this.mVideoOK) {
            if (this.mPlayView != null) {
                this.mPlayView.seekTo(nTimeOffset * 1000);
            }
            if (this.mPlayView2 != null) {
                this.mPlayView2.seekTo(nTimeOffset * 1000);
            }
        }
    }

    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        LessonClassItemData ItemData = (LessonClassItemData) this.mTimelineData.get((int) id);
        String szURL = ItemData.szObjectGUID;
        if (szURL.startsWith("http://")) {
            HttpItemObject HttpItemObject = new HttpItemObject(szURL, this);
            HttpItemObject.setSaveToFile(true);
            HttpItemObject.setTargetFileName(new StringBuilder(String.valueOf(getExternalCacheDir().getPath())).append("/lessonclasstemp.jpg").toString());
            HttpItemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    Intent intent = new Intent(LessonClassViewActivity.this.mContext, PictureActivity2.class);
                    intent.putExtra(StudentAnswerImageService.LISTURL, ItemObject.readTextData());
                    LessonClassViewActivity.this.mContext.startActivity(intent);
                }
            });
            VirtualNetworkObject.addToQueue(HttpItemObject);
        } else {
            if (this.mPlayView != null) {
                this.mPlayView.seekTo(ItemData.nTimeOffsetInSeconds * 1000);
            }
            if (this.mPlayView2 != null) {
                this.mPlayView2.seekTo(ItemData.nTimeOffsetInSeconds * 1000);
            }
        }
        return false;
    }

    public boolean isFullScreen() {
        return this.mFullScreen;
    }

    public void toggleFullScreen() {
        LayoutParams Params = (LayoutParams) this.mPlayView2.getLayoutParams();
        Params.gravity = 1;
        if (this.mFullScreen) {
            this.mListView.setVisibility(0);
            this.mViewSperator.setVisibility(0);
            this.mFullScreen = false;
            Params.width = (int) (((float) Utilities.getScreenWidth(this.mContext)) * 0.5859375f);
            Params.height = (int) (((float) Utilities.getScreenHeight(this.mContext)) * 0.6510417f);
        } else {
            this.mListView.setVisibility(8);
            this.mViewSperator.setVisibility(8);
            this.mFullScreen = true;
            Params.width = Utilities.getScreenWidth(this.mContext);
            Params.height = Utilities.getScreenHeight(this.mContext);
        }
        this.mPlayView2.post(new Runnable() {
            public void run() {
                LessonClassViewActivity.this.mPlayView2.relayout();
            }
        });
    }
}
