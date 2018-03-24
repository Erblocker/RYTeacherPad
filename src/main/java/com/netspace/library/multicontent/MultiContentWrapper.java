package com.netspace.library.multicontent;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import com.netspace.library.controls.CustomCameraView;
import com.netspace.library.controls.CustomDrawView;
import com.netspace.library.controls.CustomSelectorView;
import com.netspace.library.controls.CustomTextView;
import com.netspace.library.controls.CustomViewBase;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.controls.LockableScrollView.ScrollViewListener;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.parser.UserResourceParser;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import eu.janmuller.android.simplecropimage.CropImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import net.sourceforge.opencamera.MainActivity;

public class MultiContentWrapper implements OnClickListener {
    private static final int CROP_BIG_PICTURE = 2;
    public static final int LEFTMARGIN = 20;
    public static final int RIGHTMARGIN = 20;
    private static final String TAG = "MultiContentWrapper";
    private static final int TAKE_BIG_PICTURE = 1;
    public static final int TOPMARGIN = 20;
    private boolean mAllowSave;
    private boolean mAllowUnlock;
    private boolean mAllowVisibleCheck;
    private Runnable mCheckVisibleRunnable;
    private String mClientID;
    private int mItemCount;
    private int mItemPos;
    private LockableScrollView mScrollView;
    private Activity m_Activity;
    private MultiContentWrapperEventInterface m_CallBackInterface;
    private Context m_Context;
    private CustomCameraView m_CurrentCameraView;
    private ResourceItemData m_CurrentItem;
    private IWmExamDBOpenHelper m_DBHelper;
    private Uri m_ImageUri;
    private LinearLayout m_RootLayout;
    private boolean m_bChanged;
    private int m_nMinTopIndex;
    private String m_szCameraFileName;
    private String m_szResourceGUID;
    private String m_szScheduleGUID;

    public interface MultiContentWrapperEventInterface {
        void OnFavorite(boolean z);

        void OnGoBack();

        void OnGoNext();

        void OnSave();
    }

    public MultiContentWrapper(Activity Activity, int nRootLayoutID, MultiContentWrapperEventInterface CallBack) {
        this(Activity, nRootLayoutID, CallBack, null, null, null);
    }

    public MultiContentWrapper(Activity Activity, int nRootLayoutID, MultiContentWrapperEventInterface CallBack, IWmExamDBOpenHelper DBHelper, String szScheduleGUID, String szResourceGUID) {
        this.m_CurrentItem = new ResourceItemData();
        this.m_bChanged = false;
        this.m_nMinTopIndex = 0;
        this.m_CallBackInterface = null;
        this.m_DBHelper = null;
        this.m_szScheduleGUID = null;
        this.m_szResourceGUID = null;
        this.mItemPos = -1;
        this.mItemCount = -1;
        this.mAllowVisibleCheck = true;
        this.mAllowUnlock = true;
        this.mAllowSave = true;
        this.mCheckVisibleRunnable = new Runnable() {
            public void run() {
                if (MultiContentWrapper.this.mAllowVisibleCheck) {
                    MultiContentWrapper.this.checkComponentsVisible(MultiContentWrapper.this.mScrollView);
                }
            }
        };
        this.m_Activity = Activity;
        this.m_Context = Activity;
        this.m_RootLayout = (LinearLayout) this.m_Activity.findViewById(nRootLayoutID);
        this.m_CallBackInterface = CallBack;
        this.m_nMinTopIndex = this.m_RootLayout.getChildCount() - 1;
        this.m_DBHelper = DBHelper;
        this.m_szScheduleGUID = szScheduleGUID;
        this.m_szResourceGUID = szResourceGUID;
        View ParentView = (View) this.m_RootLayout.getParent();
        while (ParentView != null && !(ParentView instanceof LockableScrollView)) {
            ParentView = (View) ParentView.getParent();
        }
        if (ParentView != null) {
            this.mScrollView = (LockableScrollView) ParentView;
            this.mScrollView.setScrollViewListener(new ScrollViewListener() {
                public void onScrollChanged(LockableScrollView scrollView, int x, int y, int oldx, int oldy) {
                    MultiContentWrapper.this.mScrollView.removeCallbacks(MultiContentWrapper.this.mCheckVisibleRunnable);
                    MultiContentWrapper.this.mScrollView.postDelayed(MultiContentWrapper.this.mCheckVisibleRunnable, 100);
                }
            });
        }
        InitControls();
    }

    public void setCurrentItem(ResourceItemData CurrentItem) {
        this.m_CurrentItem = CurrentItem;
    }

    public void setAllowUnlock(boolean bAllow) {
        this.mAllowUnlock = bAllow;
    }

    public void setClientID(String szClientID) {
        this.mClientID = szClientID;
    }

    public boolean isAllowVisibleCheck() {
        return this.mAllowVisibleCheck;
    }

    public void setAllowVisibleCheck(boolean bAllowVisibleCheck) {
        this.mAllowVisibleCheck = bAllowVisibleCheck;
    }

    public void setAllowSave(boolean bAllowSave) {
        this.mAllowSave = bAllowSave;
        UpdateIcons();
    }

    public boolean getAllowSave() {
        return this.mAllowSave;
    }

    private boolean InitButtons(int nResID) {
        View OneButton = this.m_Activity.findViewById(nResID);
        if (OneButton == null) {
            return false;
        }
        OneButton.setOnClickListener(this);
        return true;
    }

    private boolean InitButton(String szButtonID, String szImageBaseName) {
        int nButtonID = this.m_Activity.getResources().getIdentifier(szButtonID, "id", this.m_Context.getPackageName());
        if (!InitButtons(nButtonID)) {
            return false;
        }
        int nLightImageID = this.m_Activity.getResources().getIdentifier(new StringBuilder(String.valueOf(szImageBaseName)).append("_light").toString(), "drawable", this.m_Context.getPackageName());
        int nNormalImageID = this.m_Activity.getResources().getIdentifier(szImageBaseName, "drawable", this.m_Context.getPackageName());
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{16842919}, this.m_Activity.getResources().getDrawable(nLightImageID));
        states.addState(new int[]{16842908}, this.m_Activity.getResources().getDrawable(nLightImageID));
        states.addState(new int[0], this.m_Activity.getResources().getDrawable(nNormalImageID));
        ((ImageButton) this.m_Activity.findViewById(nButtonID)).setImageDrawable(states);
        return true;
    }

    public void InitControls() {
        InitButton("imageButtonFav", "ic_star");
        InitButton("ImageButtonNext", "ic_right");
        InitButton("ImageButtonPrev", "ic_left");
        InitButton("ImageButtonThumbUp", "ic_thumbup");
        InitButton("ImageButtonThumbDown", "ic_thumbdown");
        InitButton("ImageButtonAdd", "ic_plus");
        InitButton("ImageButtonLock", "ic_locked");
        InitButton("ImageButtonSave", "ic_save");
        setChanged(this.m_bChanged);
    }

    private int getId(String szIDName) {
        return this.m_Activity.getResources().getIdentifier(szIDName, "id", this.m_Context.getPackageName());
    }

    private int getDrawableId(String szIDName) {
        return this.m_Activity.getResources().getIdentifier(szIDName, "drawable", this.m_Context.getPackageName());
    }

    public void setChanged(boolean bChanged) {
        if (this.m_bChanged && !bChanged) {
            resetChangeFlag();
        }
        this.m_bChanged = bChanged;
        UpdateIcons();
    }

    public void LockComponents(boolean bLock) {
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            View OneObject = this.m_RootLayout.getChildAt(i);
            if (OneObject instanceof CustomCameraView) {
                ((CustomCameraView) OneObject).setLocked(bLock);
                setChanged(true);
            } else if (OneObject instanceof CustomDrawView) {
                ((CustomDrawView) OneObject).setLocked(bLock);
                setChanged(true);
            } else if (OneObject instanceof CustomTextView) {
                ((CustomTextView) OneObject).setLocked(bLock);
                setChanged(true);
            } else if (OneObject instanceof CustomSelectorView) {
                ((CustomSelectorView) OneObject).setLocked(bLock);
                setChanged(true);
            }
        }
    }

    public void LockComponents(boolean bLock, LinearLayout RootLayout) {
        for (int i = 0; i < RootLayout.getChildCount(); i++) {
            View OneObject = RootLayout.getChildAt(i);
            if (OneObject instanceof CustomCameraView) {
                ((CustomCameraView) OneObject).setLocked(bLock);
                setChanged(true);
            } else if (OneObject instanceof CustomDrawView) {
                ((CustomDrawView) OneObject).setLocked(bLock);
                setChanged(true);
            } else if (OneObject instanceof CustomTextView) {
                ((CustomTextView) OneObject).setLocked(bLock);
                setChanged(true);
            } else if (OneObject instanceof CustomSelectorView) {
                ((CustomSelectorView) OneObject).setLocked(bLock);
                setChanged(true);
            }
        }
    }

    public void onClick(View v) {
        boolean z = false;
        if (v.getId() == getId("ImageButtonNext")) {
            if (this.m_CallBackInterface != null) {
                if (this.m_bChanged) {
                    this.m_CallBackInterface.OnSave();
                }
                this.m_CallBackInterface.OnGoNext();
            }
        } else if (v.getId() == getId("ImageButtonPrev") && this.m_CallBackInterface != null) {
            if (this.m_bChanged) {
                this.m_CallBackInterface.OnSave();
            }
            this.m_CallBackInterface.OnGoBack();
        }
        if (this.mAllowSave) {
            ImageButton ImageButton = (ImageButton) v;
            IWmExamDBOpenHelper iWmExamDBOpenHelper;
            String str;
            String str2;
            int i;
            if (v.getId() == getId("imageButtonFav")) {
                this.m_CurrentItem.bFav = !this.m_CurrentItem.bFav;
                UpdateIcons();
                if (this.m_DBHelper != null) {
                    iWmExamDBOpenHelper = this.m_DBHelper;
                    str = this.m_szScheduleGUID;
                    str2 = this.m_CurrentItem.szResourceGUID;
                    if (this.m_CurrentItem.bFav) {
                        i = 1;
                    }
                    iWmExamDBOpenHelper.SetItemResult(str, str2, 2, i);
                }
                if (this.m_CallBackInterface != null) {
                    this.m_CallBackInterface.OnFavorite(this.m_CurrentItem.bFav);
                }
                setChanged(true);
            } else if (v.getId() == getId("ImageButtonThumbDown")) {
                this.m_CurrentItem.bThumbDown = !this.m_CurrentItem.bThumbDown;
                if (this.m_CurrentItem.bThumbDown) {
                    this.m_CurrentItem.bThumbUp = false;
                }
                UpdateIcons();
                if (this.m_DBHelper != null) {
                    iWmExamDBOpenHelper = this.m_DBHelper;
                    str = this.m_szScheduleGUID;
                    str2 = this.m_CurrentItem.szResourceGUID;
                    if (this.m_CurrentItem.bThumbDown) {
                        i = -1;
                    }
                    iWmExamDBOpenHelper.SetItemResult(str, str2, 1, i);
                }
                setChanged(true);
            } else if (v.getId() == getId("ImageButtonThumbUp")) {
                this.m_CurrentItem.bThumbUp = !this.m_CurrentItem.bThumbUp;
                if (this.m_CurrentItem.bThumbUp) {
                    this.m_CurrentItem.bThumbDown = false;
                }
                UpdateIcons();
                if (this.m_DBHelper != null) {
                    iWmExamDBOpenHelper = this.m_DBHelper;
                    str = this.m_szScheduleGUID;
                    str2 = this.m_CurrentItem.szResourceGUID;
                    if (this.m_CurrentItem.bThumbUp) {
                        i = 1;
                    }
                    iWmExamDBOpenHelper.SetItemResult(str, str2, 1, i);
                }
                setChanged(true);
            } else if (v.getId() == getId("ImageButtonLock")) {
                if (this.mAllowUnlock || !this.m_CurrentItem.bLocked) {
                    ResourceItemData resourceItemData = this.m_CurrentItem;
                    if (!this.m_CurrentItem.bLocked) {
                        z = true;
                    }
                    resourceItemData.bLocked = z;
                    LockComponents(this.m_CurrentItem.bLocked);
                    UpdateIcons();
                }
            } else if (v.getId() == getId("ImageButtonSave")) {
                if (this.m_CallBackInterface != null) {
                    this.m_CallBackInterface.OnSave();
                }
            } else if (v.getId() == getId("ImageButtonAdd") && !this.m_CurrentItem.bLocked) {
                new Builder(new ContextThemeWrapper(this.m_Context, 16974130)).setItems(new String[]{"拍照", "绘画板", "文本"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean bScrollToBotton = false;
                        LayoutParams LayoutParam;
                        if (which == 0) {
                            CustomCameraView CameraView = new CustomCameraView(MultiContentWrapper.this.m_Activity);
                            MultiContentWrapper.this.m_RootLayout.addView(CameraView);
                            LayoutParam = (LayoutParams) CameraView.getLayoutParams();
                            LayoutParam.leftMargin = 20;
                            LayoutParam.rightMargin = 20;
                            LayoutParam.topMargin = 20;
                            LayoutParam.height = 165;
                            CameraView.setLayoutParams(LayoutParam);
                            CameraView.startDefaultAction();
                            MultiContentWrapper.this.setChanged(true);
                            bScrollToBotton = true;
                        } else if (which == 2) {
                            CustomTextView TextView = new CustomTextView(MultiContentWrapper.this.m_Activity);
                            MultiContentWrapper.this.m_RootLayout.addView(TextView);
                            LayoutParam = (LayoutParams) TextView.getLayoutParams();
                            LayoutParam.leftMargin = 20;
                            LayoutParam.rightMargin = 20;
                            LayoutParam.topMargin = 20;
                            LayoutParam.height = 250;
                            TextView.setLayoutParams(LayoutParam);
                            TextView.startDefaultAction();
                            MultiContentWrapper.this.setChanged(true);
                            bScrollToBotton = true;
                        } else if (which == 1) {
                            CustomDrawView DrawView = new CustomDrawView(MultiContentWrapper.this.m_Activity);
                            MultiContentWrapper.this.m_RootLayout.addView(DrawView);
                            LayoutParam = (LayoutParams) DrawView.getLayoutParams();
                            LayoutParam.leftMargin = 20;
                            LayoutParam.rightMargin = 20;
                            LayoutParam.topMargin = 20;
                            LayoutParam.height = 500;
                            DrawView.setLayoutParams(LayoutParam);
                            DrawView.startDefaultAction();
                            MultiContentWrapper.this.setChanged(true);
                            bScrollToBotton = true;
                        }
                        if (bScrollToBotton) {
                            View ParentView = (View) MultiContentWrapper.this.m_RootLayout.getParent();
                            while (ParentView != null && !(ParentView instanceof ScrollView)) {
                                ParentView = (View) ParentView.getParent();
                            }
                            if (ParentView != null) {
                                final ScrollView ScrollView = (ScrollView) ParentView;
                                ScrollView.post(new Runnable() {
                                    public void run() {
                                        ScrollView.fullScroll(130);
                                        if (MultiContentWrapper.this.m_RootLayout.getChildCount() > 0) {
                                            MultiContentWrapper.this.m_RootLayout.getChildAt(MultiContentWrapper.this.m_RootLayout.getChildCount() - 1).requestFocus();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }).setTitle("选择要增加的内容").create().show();
            }
        }
    }

    public void setItemPos(int nItemPos, int nCount) {
        this.mItemPos = nItemPos;
        this.mItemCount = nCount;
    }

    public void disableButtons() {
        DisableControls(((ViewGroup) this.m_Activity.findViewById(16908290)).getChildAt(0));
    }

    protected void DisableControls(View Child) {
        if (Child instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) Child;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if ((child instanceof ImageButton) && child.isEnabled()) {
                    ((ImageButton) child).setAlpha(76);
                    child.setEnabled(false);
                }
                if (child instanceof ViewGroup) {
                    DisableControls((ViewGroup) child);
                }
            }
        } else if ((Child instanceof ImageButton) && Child.isEnabled()) {
            ((ImageButton) Child).setAlpha(76);
            Child.setEnabled(false);
        }
    }

    public void disableButton(int nButtonID, boolean bDisable) {
        ImageButton Button = (ImageButton) this.m_Activity.findViewById(nButtonID);
        if (bDisable) {
            Button.setAlpha(76);
            Button.setEnabled(false);
            return;
        }
        Button.setAlpha(255);
        Button.setEnabled(true);
    }

    public void UpdateIcons() {
        ImageButton ImageButton = (ImageButton) this.m_Activity.findViewById(getId("imageButtonFav"));
        if (ImageButton != null) {
            if (this.m_CurrentItem.bFav) {
                ImageButton.setImageResource(getDrawableId("ic_star_light"));
            } else {
                ImageButton.setImageResource(getDrawableId("ic_star"));
            }
        }
        ImageButton = (ImageButton) this.m_Activity.findViewById(getId("ImageButtonThumbDown"));
        if (ImageButton != null) {
            if (this.m_CurrentItem.bThumbDown) {
                ImageButton.setImageResource(getDrawableId("ic_thumbdown_light"));
            } else {
                ImageButton.setImageResource(getDrawableId("ic_thumbdown"));
            }
        }
        ImageButton = (ImageButton) this.m_Activity.findViewById(getId("ImageButtonThumbUp"));
        if (ImageButton != null) {
            if (this.m_CurrentItem.bThumbUp) {
                ImageButton.setImageResource(getDrawableId("ic_thumbup_light"));
            } else {
                ImageButton.setImageResource(getDrawableId("ic_thumbup"));
            }
        }
        ImageButton = (ImageButton) this.m_Activity.findViewById(getId("ImageButtonLock"));
        if (ImageButton != null) {
            ImageButton AddButton = (ImageButton) this.m_Activity.findViewById(getId("ImageButtonAdd"));
            if (this.m_CurrentItem.bLocked) {
                if (AddButton != null) {
                    AddButton.setAlpha(76);
                    AddButton.setEnabled(false);
                }
                ImageButton.setImageResource(getDrawableId("ic_locked_light"));
            } else {
                if (AddButton != null) {
                    AddButton.setAlpha(255);
                    AddButton.setEnabled(true);
                }
                ImageButton.setImageResource(getDrawableId("ic_locked"));
            }
        }
        ImageButton = (ImageButton) this.m_Activity.findViewById(getId("ImageButtonPrev"));
        if (!(ImageButton == null || this.mItemPos == -1)) {
            if (this.mItemPos == 0) {
                ImageButton.setAlpha(76);
                ImageButton.setEnabled(false);
            } else {
                ImageButton.setAlpha(255);
                ImageButton.setEnabled(true);
            }
        }
        ImageButton = (ImageButton) this.m_Activity.findViewById(getId("ImageButtonNext"));
        if (!(ImageButton == null || this.mItemPos == -1)) {
            if (this.mItemPos >= this.mItemCount - 1) {
                ImageButton.setAlpha(76);
                ImageButton.setEnabled(false);
            } else {
                ImageButton.setAlpha(255);
                ImageButton.setEnabled(true);
            }
        }
        int nButtonID = getId("ImageButtonSave");
        int nLightImageID = this.m_Activity.getResources().getIdentifier("ic_save_light", "drawable", this.m_Context.getPackageName());
        int nNormalImageID = this.m_Activity.getResources().getIdentifier("ic_save", "drawable", this.m_Context.getPackageName());
        ImageButton = (ImageButton) this.m_Activity.findViewById(nButtonID);
        if (ImageButton == null) {
            return;
        }
        if (!this.mAllowSave) {
            ImageButton.setImageResource(nNormalImageID);
            ImageButton.setAlpha(76);
            ImageButton.setEnabled(false);
        } else if (this.m_bChanged) {
            ImageButton.setImageResource(nLightImageID);
            ImageButton.setAlpha(255);
            ImageButton.setEnabled(true);
        } else {
            ImageButton.setImageResource(nNormalImageID);
            ImageButton.setAlpha(76);
            ImageButton.setEnabled(false);
        }
    }

    public void LoadData() {
        DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(this.m_CurrentItem.szGUID, this.m_Activity);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szData = ItemObject.readTextData();
                if (szData != null && !szData.isEmpty()) {
                    new UserResourceParser(MultiContentWrapper.this.m_Context).Load(szData, MultiContentWrapper.this.m_CurrentItem, MultiContentWrapper.this.m_RootLayout);
                    View ParentView = (View) MultiContentWrapper.this.m_RootLayout.getParent();
                    while (ParentView != null && !(ParentView instanceof ScrollView)) {
                        ParentView = (View) ParentView.getParent();
                    }
                    if (ParentView != null) {
                        final ScrollView ScrollView = (ScrollView) ParentView;
                        ScrollView.requestFocus();
                        ScrollView.postDelayed(new Runnable() {
                            public void run() {
                                ScrollView.scrollTo(0, 0);
                            }
                        }, 100);
                    }
                    MultiContentWrapper.this.resetChangeFlag();
                    MultiContentWrapper.this.setChanged(false);
                    MultiContentWrapper.this.UpdateIcons();
                }
            }
        });
        ResourceObject.setClientID(this.mClientID);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    public boolean SaveData(CustomSelectorView SelectorView, String szGUID) {
        if (this.mClientID == null) {
            Log.e(TAG, "No clientID assigned. Can not save.");
            throw new IllegalArgumentException("No clientID assigned. Can not save.");
        }
        String szXMLData = new UserResourceParser(this.m_Context).SaveToString(SelectorView);
        if (szXMLData != null) {
            DataSynchronizeItemObject DataObject = new DataSynchronizeItemObject(szGUID, this.m_Activity);
            DataObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    MultiContentWrapper.this.setChanged(false);
                }
            });
            DataObject.writeTextData(szXMLData);
            DataObject.setReadOperation(false);
            DataObject.setNoDeleteOnFinish(true);
            DataObject.setClientID(this.mClientID);
            VirtualNetworkObject.addToQueue(DataObject);
            setChanged(false);
            return true;
        }
        Log.d(TAG, "Save data failed.");
        return false;
    }

    public boolean SaveData() {
        if (this.mClientID == null) {
            Log.e(TAG, "No clientID assigned. Can not save.");
            throw new IllegalArgumentException("No clientID assigned. Can not save.");
        }
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            View OneView = this.m_RootLayout.getChildAt(i);
            LayoutParams LayoutParam = (LayoutParams) OneView.getLayoutParams();
            if (OneView instanceof CustomCameraView) {
                CustomCameraView TempView = (CustomCameraView) OneView;
                final String szFileName = TempView.getImageFileName(false);
                String szKey = "";
                boolean bSaveAndUpload = false;
                if (szFileName != null) {
                    szKey = Utilities.getFileName(szFileName);
                }
                if (!szKey.isEmpty()) {
                    if (TempView.isChanged()) {
                        bSaveAndUpload = true;
                    } else if (!VirtualNetworkObject.getDataSynchronizeEngine().hasPackage(szKey, this.mClientID)) {
                        bSaveAndUpload = true;
                    }
                }
                if (bSaveAndUpload) {
                    DataSynchronizeItemObject DataObject = new DataSynchronizeItemObject(szKey, this.m_Activity);
                    DataObject.setSuccessListener(new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            Utilities.deleteFile(szFileName);
                        }
                    });
                    DataObject.writeTextData(Utilities.getBase64FileContent(szFileName));
                    DataObject.setReadOperation(false);
                    DataObject.setNoDeleteOnFinish(true);
                    DataObject.setClientID(this.mClientID);
                    VirtualNetworkObject.addToQueue(DataObject);
                } else {
                    Utilities.deleteFile(szFileName);
                }
            }
        }
        String szXMLData = new UserResourceParser(this.m_Context).SaveToString(this.m_CurrentItem, this.m_RootLayout);
        if (szXMLData != null) {
            DataObject = new DataSynchronizeItemObject(this.m_CurrentItem.szGUID, this.m_Activity);
            DataObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    MultiContentWrapper.this.setChanged(false);
                }
            });
            DataObject.writeTextData(szXMLData);
            DataObject.setReadOperation(false);
            DataObject.setNoDeleteOnFinish(true);
            DataObject.setClientID(this.mClientID);
            VirtualNetworkObject.addToQueue(DataObject);
            setChanged(false);
            return true;
        }
        Log.d(TAG, "Save data failed.");
        return false;
    }

    public boolean IsChanged() {
        if (this.m_bChanged) {
            return true;
        }
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            View OneObject = this.m_RootLayout.getChildAt(i);
            if ((OneObject instanceof CustomViewBase) && ((CustomViewBase) OneObject).isChanged()) {
                return true;
            }
        }
        return false;
    }

    private void resetChangeFlag() {
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            View OneObject = this.m_RootLayout.getChildAt(i);
            if (OneObject instanceof CustomViewBase) {
                CustomViewBase OneView = (CustomViewBase) OneObject;
                if (OneView.isChanged()) {
                    OneView.resetChangeFlag();
                }
            }
        }
    }

    public static String CreateCameraFileName(Context Context) {
        return Context.getExternalCacheDir() + "/data/camera_" + Utilities.createGUID() + ".jpg";
    }

    public static String CreateCameraFileName(Context Context, String szKey) {
        if (szKey == null || szKey.isEmpty()) {
            return CreateCameraFileName(Context);
        }
        return Context.getExternalCacheDir() + "/data/" + szKey;
    }

    public void StartTakePicture(CustomCameraView CameraView) {
        this.m_CurrentCameraView = CameraView;
        Intent it = new Intent(this.m_Context, MainActivity.class);
        it.setAction("android.media.action.IMAGE_CAPTURE");
        try {
            this.m_szCameraFileName = CreateCameraFileName(this.m_Context);
            this.m_ImageUri = Uri.fromFile(new File(this.m_szCameraFileName));
            it.putExtra("output", this.m_ImageUri);
            this.m_Activity.startActivityForResult(it, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent(this.m_Context, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, this.m_szCameraFileName);
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 0);
        intent.putExtra(CropImage.ASPECT_Y, 0);
        this.m_Activity.startActivityForResult(intent, requestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            new File(this.m_szCameraFileName).delete();
            this.m_szCameraFileName = "";
            this.m_CurrentCameraView = null;
        } else if (requestCode == 1) {
            Log.d("TakePicture", "m_ImageUri:" + this.m_ImageUri.toString());
            cropImageUri(this.m_ImageUri, 640, 480, 2);
        } else if (requestCode == 2) {
            Bitmap SourceBitmap = BitmapFactory.decodeFile(this.m_szCameraFileName);
            if (SourceBitmap != null) {
                int nWidth = SourceBitmap.getWidth();
                int nHeight = SourceBitmap.getHeight();
                float fScale = 1.0f;
                if (nWidth > nHeight) {
                    if (nWidth > 1280) {
                        fScale = 1280.0f / ((float) nWidth);
                        nWidth = 1280;
                        nHeight = (int) (((float) nHeight) * fScale);
                    }
                } else if (nHeight > 1280) {
                    fScale = 1280.0f / ((float) nHeight);
                    nHeight = 1280;
                    nWidth = (int) (((float) nWidth) * fScale);
                }
                if (fScale != 1.0f) {
                    Bitmap TargetBitmap = Bitmap.createScaledBitmap(SourceBitmap, nWidth, nHeight, true);
                    if (TargetBitmap != null) {
                        try {
                            TargetBitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(this.m_szCameraFileName));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        TargetBitmap.recycle();
                    }
                }
                SourceBitmap.recycle();
            }
            String szOldFileName = this.m_CurrentCameraView.getImageFileName(true);
            if (szOldFileName == null || szOldFileName.isEmpty()) {
                this.m_CurrentCameraView.setImageFileName(this.m_szCameraFileName);
            } else {
                Utilities.copyFile(new File(this.m_szCameraFileName), new File(szOldFileName));
                this.m_CurrentCameraView.setImageFileName(szOldFileName);
            }
            this.m_CurrentCameraView = null;
        }
    }

    public void DeleteComponent(View Object) {
        if (Object instanceof CustomDrawView) {
            ((CustomDrawView) Object).clear();
        }
        this.m_RootLayout.removeView(Object);
        setChanged(true);
    }

    public void MoveComponentUp(View Object) {
        int nIndex = this.m_RootLayout.indexOfChild(Object);
        if (nIndex != -1 && nIndex > this.m_nMinTopIndex + 1) {
            nIndex--;
            this.m_RootLayout.removeView(Object);
            this.m_RootLayout.addView(Object, nIndex);
            setChanged(true);
        }
    }

    public void MoveComponentDown(View Object) {
        int nIndex = this.m_RootLayout.indexOfChild(Object);
        if (nIndex != -1 && nIndex < this.m_RootLayout.getChildCount() - 1) {
            nIndex++;
            this.m_RootLayout.removeView(Object);
            this.m_RootLayout.addView(Object, nIndex);
            setChanged(true);
        }
    }

    public void onResume() {
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            this.m_RootLayout.getChildAt(i);
        }
    }

    public void onPause() {
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            this.m_RootLayout.getChildAt(i);
        }
    }

    private void checkComponentsVisible(ViewGroup ViewGroup) {
        Log.d(TAG, "checkComponentsVisible");
        for (int i = 0; i < ViewGroup.getChildCount(); i++) {
            View OneView = ViewGroup.getChildAt(i);
            if (OneView instanceof DrawView) {
                DrawView TempView = (DrawView) OneView;
                Rect scrollBounds = new Rect();
                TempView.getHitRect(scrollBounds);
                if (TempView.getLocalVisibleRect(scrollBounds)) {
                    TempView.setPausePaint(false);
                } else {
                    TempView.setPausePaint(true);
                    if (TempView.getEnableCache()) {
                        TempView.cleanCache();
                    }
                }
            } else if (OneView instanceof CustomViewBase) {
                ((CustomViewBase) OneView).startVisibleCheck();
            } else if (OneView instanceof ViewGroup) {
                checkComponentsVisible((ViewGroup) OneView);
            }
        }
    }
}
