package com.netspace.library.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.DialogFragment;
import android.support.v4.internal.view.SupportMenu;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.activity.FingerDrawActivity.FingerDrawCallbackInterface;
import com.netspace.library.adapter.ViewPageAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.controls.LinedEditText;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.struct.UserAnswerSheetImage;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class AnswerSheetOneAnswerDialog extends DialogFragment implements OnTabSelectedListener, FingerDrawCallbackInterface {
    private ViewPageAdapter mAdapter;
    private OnCorrectScoreSelectedListener mCallBack;
    private UserAnswerSheetImage mData;
    private DrawView mDrawView;
    private DrawView mDrawViewCorrecting;
    private LinedEditText mEditText;
    private boolean mHasCorrectDrawing = false;
    private ImageView mImageView;
    private View mRootView;
    private CustomViewPager mViewPager;
    private boolean mbDrawActivityOn = false;
    private Target mhandWriteCameraClickTarget = new Target() {
        public void onBitmapFailed(Drawable arg0) {
            Utilities.showAlertMessage(UI.getCurrentActivity(), "获取图片数据失败", "获取图片数据失败。");
            AnswerSheetOneAnswerDialog.this.mbDrawActivityOn = false;
        }

        public void onBitmapLoaded(Bitmap Bitmap, LoadedFrom arg1) {
            AnswerSheetOneAnswerDialog.this.launchDrawPad(Bitmap, AnswerSheetOneAnswerDialog.this.mData.szAnswer2);
        }

        public void onPrepareLoad(Drawable arg0) {
        }
    };

    public interface OnCorrectScoreSelectedListener {
        void onImageChanged();

        void onScoreSelected();
    }

    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            getDialog().getWindow().setLayout((width * 8) / 10, -2);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_common, container, false);
        this.mRootView.setLayerType(1, null);
        Toolbar toolbar = (Toolbar) this.mRootView.findViewById(R.id.toolbar);
        toolbar.setTitle(new StringBuilder(String.valueOf(this.mData.szRealName)).append("的作答").toString());
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                int nID = arg0.getItemId();
                if (nID == 0) {
                    return false;
                }
                nID -= 1000;
                AnswerSheetOneAnswerDialog.this.mData.nAnswerScore = (float) nID;
                if (nID == 0) {
                    AnswerSheetOneAnswerDialog.this.mData.nAnswerResult = -1;
                } else if (nID == ((int) AnswerSheetOneAnswerDialog.this.mData.nFullScore)) {
                    AnswerSheetOneAnswerDialog.this.mData.nAnswerResult = 2;
                } else {
                    AnswerSheetOneAnswerDialog.this.mData.nAnswerResult = 1;
                }
                if (AnswerSheetOneAnswerDialog.this.mCallBack != null) {
                    AnswerSheetOneAnswerDialog.this.mCallBack.onScoreSelected();
                }
                if (AnswerSheetOneAnswerDialog.this.mHasCorrectDrawing) {
                    String szCorrectResultData = AnswerSheetOneAnswerDialog.this.mDrawViewCorrecting.getDataAsString();
                    if (!szCorrectResultData.isEmpty()) {
                        DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(new StringBuilder(String.valueOf(AnswerSheetOneAnswerDialog.this.mData.szGUID)).append("_CorrectResult").toString(), null);
                        ResourceObject.setSuccessListener(new OnSuccessListener() {
                            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            }
                        });
                        ResourceObject.setFailureListener(new OnFailureListener() {
                            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                            }
                        });
                        ResourceObject.setClientID(AnswerSheetOneAnswerDialog.this.mData.szClientID);
                        ResourceObject.writeTextData(szCorrectResultData);
                        ResourceObject.setReadOperation(false);
                        ResourceObject.setAlwaysActiveCallbacks(true);
                        ResourceObject.setIgnoreActivityFinishCheck(true);
                        VirtualNetworkObject.addToQueue(ResourceObject);
                    }
                }
                AnswerSheetOneAnswerDialog.this.dismiss();
                return true;
            }
        });
        SubMenu submenu = toolbar.getMenu().addSubMenu("批改");
        submenu.getItem().setShowAsAction(2);
        for (int i = (int) this.mData.nFullScore; i >= 0; i--) {
            if (i == ((int) this.mData.nFullScore)) {
                submenu.add(0, i + 1000, 0, "全对（" + String.valueOf(i) + "分）");
            } else if (i == 0) {
                submenu.add(0, i + 1000, 0, "全错（" + String.valueOf(i) + "分）");
            } else {
                submenu.add(0, i + 1000, 0, "得" + String.valueOf(i) + "分");
            }
        }
        this.mAdapter = new ViewPageAdapter(getContext());
        FrameLayout frameLayout = new FrameLayout(getContext());
        this.mDrawView = new DrawView(getContext());
        this.mDrawViewCorrecting = new DrawView(getContext());
        frameLayout.setLayerType(1, null);
        frameLayout.setBackgroundResource(R.drawable.background_drawpad);
        frameLayout.addView(this.mDrawView, -1, -1);
        frameLayout.addView(this.mDrawViewCorrecting, -1, -1);
        this.mImageView = new ImageView(getContext());
        this.mEditText = new LinedEditText(getContext());
        if (!this.mData.szAnswer1.isEmpty()) {
            this.mAdapter.addPage(frameLayout, "绘画板");
            this.mDrawView.setLayerType(1, null);
            this.mDrawView.fromString(this.mData.szAnswer1);
            this.mDrawViewCorrecting.setBrushMode(true);
            this.mDrawViewCorrecting.setColor(SupportMenu.CATEGORY_MASK);
            this.mDrawViewCorrecting.setOnlyActivePenDraw(true);
            this.mDrawViewCorrecting.setLayerType(1, null);
            this.mDrawViewCorrecting.setEnableCache(true);
            this.mDrawViewCorrecting.changeWidth(Utilities.getIntSettings("PenWidth", 6));
            DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(new StringBuilder(String.valueOf(this.mData.szGUID)).append("_CorrectResult").toString(), null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    AnswerSheetOneAnswerDialog.this.mDrawViewCorrecting.fromString(ItemObject.readTextData());
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                }
            });
            ResourceObject.setClientID(this.mData.szClientID);
            ResourceObject.setReadOperation(true);
            ResourceObject.setAlwaysActiveCallbacks(true);
            ResourceObject.setIgnoreActivityFinishCheck(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
            this.mDrawViewCorrecting.setCallback(new DrawViewActionInterface() {
                private boolean mEraseMode = false;

                public void OnTouchDown() {
                    lockParents(true);
                }

                public void OnTouchUp() {
                    lockParents(false);
                }

                public void OnPenButtonDown() {
                    if (this.mEraseMode) {
                        Utilities.showToastMessage(AnswerSheetOneAnswerDialog.this.getContext(), "已切换为书写模式");
                        AnswerSheetOneAnswerDialog.this.mDrawViewCorrecting.setEraseMode2(false, 0);
                        AnswerSheetOneAnswerDialog.this.mDrawViewCorrecting.setBrushMode(true);
                        this.mEraseMode = false;
                        return;
                    }
                    Utilities.showToastMessage(AnswerSheetOneAnswerDialog.this.getContext(), "已切换为擦除模式");
                    AnswerSheetOneAnswerDialog.this.mDrawViewCorrecting.setBrushMode(false);
                    AnswerSheetOneAnswerDialog.this.mDrawViewCorrecting.setEraseMode2(true, 0);
                    this.mEraseMode = true;
                }

                public void OnPenButtonUp() {
                }

                public void OnTouchPen() {
                    AnswerSheetOneAnswerDialog.this.mHasCorrectDrawing = true;
                }

                public void OnTouchFinger() {
                }

                public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
                }

                private void lockParents(boolean bLock) {
                    View parentView = (View) AnswerSheetOneAnswerDialog.this.mDrawViewCorrecting.getParent();
                    while (parentView != null) {
                        boolean z;
                        if (parentView instanceof LockableScrollView) {
                            LockableScrollView LockableScrollView = (LockableScrollView) parentView;
                            if (bLock) {
                                z = false;
                            } else {
                                z = true;
                            }
                            LockableScrollView.setScrollingEnabled(z);
                        }
                        if (parentView instanceof CustomViewPager) {
                            CustomViewPager CustomViewPager = (CustomViewPager) parentView;
                            if (bLock) {
                                z = false;
                            } else {
                                z = true;
                            }
                            CustomViewPager.setPagingEnabled(z);
                        }
                        if (parentView.getParent() instanceof View) {
                            parentView = (View) parentView.getParent();
                        } else {
                            return;
                        }
                    }
                }

                public void OnTouchMove() {
                }
            });
        }
        if (!this.mData.szAnswer0.isEmpty()) {
            this.mAdapter.addPage(this.mEditText, "文本");
            this.mEditText.setText(this.mData.szAnswer0);
            this.mEditText.setBackgroundResource(R.drawable.background_textcomponent);
            this.mEditText.setFocusable(false);
            this.mEditText.setGravity(51);
        }
        if (!this.mData.szAnswer2.isEmpty()) {
            this.mAdapter.addPage(this.mImageView, "拍照");
            String szURL = new StringBuilder(String.valueOf(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/DataSynchronizeGetSingleData?clientid=" + this.mData.szClientID + "&packageid=")).append(this.mData.szAnswer2).toString();
            Picasso.with(getContext()).load(szURL).into(this.mImageView);
            final String szImageURL = szURL;
            this.mImageView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AnswerSheetOneAnswerDialog.this.mbDrawActivityOn) {
                        Picasso.with(AnswerSheetOneAnswerDialog.this.getContext()).cancelRequest(AnswerSheetOneAnswerDialog.this.mhandWriteCameraClickTarget);
                        Picasso.with(AnswerSheetOneAnswerDialog.this.getContext()).load(szImageURL).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(AnswerSheetOneAnswerDialog.this.mhandWriteCameraClickTarget);
                        AnswerSheetOneAnswerDialog.this.mbDrawActivityOn = true;
                    }
                }
            });
        }
        this.mViewPager = (CustomViewPager) this.mRootView.findViewById(R.id.pager);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOffscreenPageLimit(4);
        TabLayout tabLayout = (TabLayout) this.mRootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(this.mViewPager);
        tabLayout.setOnTabSelectedListener(this);
        setHasOptionsMenu(true);
        return this.mRootView;
    }

    public void setData(UserAnswerSheetImage data) {
        this.mData = data;
    }

    public void setCallBack(OnCorrectScoreSelectedListener callBack) {
        this.mCallBack = callBack;
    }

    public void onTabReselected(Tab arg0) {
    }

    public void onTabSelected(Tab arg0) {
        this.mViewPager.setCurrentItem(arg0.getPosition());
    }

    public void onTabUnselected(Tab arg0) {
    }

    private void launchDrawPad(Bitmap Bitmap, String szKey) {
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(new StringBuilder(String.valueOf(getActivity().getExternalCacheDir().getAbsolutePath())).append("/").append(szKey).append(".jpg").toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!(stream == null || Bitmap == null)) {
            Bitmap.compress(CompressFormat.JPEG, 100, stream);
        }
        if (Bitmap != null) {
            int nImageWidth = Bitmap.getWidth();
            int nImageHeight = Bitmap.getHeight();
            Intent DrawActivity = new Intent(getActivity(), FingerDrawActivity.class);
            FingerDrawActivity.SetCallbackInterface(this);
            DrawActivity.putExtra("imageKey", szKey);
            DrawActivity.putExtra("imageWidth", nImageWidth);
            DrawActivity.putExtra("imageHeight", nImageHeight);
            DrawActivity.putExtra("allowUpload", true);
            DrawActivity.putExtra("allowCamera", true);
            DrawActivity.putExtra("enableBackButton", true);
            DrawActivity.putExtra("uploadName", szKey);
            DrawActivity.putExtra("allowautoupload", false);
            DrawActivity.setFlags(67108864);
            if (UI.getCurrentActivity() != null) {
                UI.getCurrentActivity().startActivity(DrawActivity);
                return;
            }
            DrawActivity.setFlags(268468224);
            startActivity(DrawActivity);
        }
    }

    public void OnFingerDrawCreate(Activity Activity) {
    }

    public boolean HasMJpegClients() {
        return false;
    }

    public void OnUpdateMJpegImage(Bitmap bitmap, Activity Activity) {
    }

    public void OnOK(Bitmap bitmap, String szUploadName, final Activity Activity) {
        DataSynchronizeItemObject CallItem = new DataSynchronizeItemObject(szUploadName, null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                Activity.finish();
                if (AnswerSheetOneAnswerDialog.this.mCallBack != null) {
                    AnswerSheetOneAnswerDialog.this.mCallBack.onImageChanged();
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Toast.makeText(AnswerSheetOneAnswerDialog.this.getActivity(), "批改保存失败", 0).show();
            }
        });
        CallItem.writeTextData(Utilities.saveBitmapToBase64String(bitmap));
        CallItem.setReadOperation(false);
        CallItem.setClientID(this.mData.szClientID);
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public void OnProject(Activity Activity) {
    }

    public void OnDestroy(Activity Activity) {
        Picasso.with(getContext()).load(new StringBuilder(String.valueOf(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/DataSynchronizeGetSingleData?clientid=" + this.mData.szClientID + "&packageid=")).append(this.mData.szAnswer2).toString()).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(this.mImageView);
        this.mbDrawActivityOn = false;
    }

    public void OnBroadcast(Activity Activity) {
    }

    public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight, Activity Activity) {
    }
}
