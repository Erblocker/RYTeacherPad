package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.library.adapter.AnswerSheetV2QuestionListAdapter;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.controls.DrawView;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.struct.AnswerSheetV2QuestionItem;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.library.wrapper.CameraCaptureActivity;
import com.netspace.library.wrapper.CameraCaptureActivity.CameraCaptureCallBack;
import com.netspace.pad.library.R;
import java.io.File;
import java.util.ArrayList;
import net.sqlcipher.database.SQLiteDatabase;

public class AnswerSheetV2OthersComponent extends CustomFrameLayout implements IComponents, OnClickListener, ComponentCallBack {
    private static AnswerSheetV2QuestionListAdapter mAdapter;
    private static String mClientID;
    private static ArrayList<AnswerSheetV2QuestionItem> marrData;
    private static ArrayList<AnswerSheetV2OthersComponent> marrOpenedComponents = new ArrayList();
    private static boolean mbGlobalLock = false;
    private ImageButton mBackButton;
    private LinearLayout mContentLayout;
    private ContextThemeWrapper mContextThemeWrapper;
    private String[] mData;
    private ImageButton mImageButtonBrush;
    private ImageButton mImageButtonCamera;
    private ImageButton mImageButtonText;
    private ImageButton mNextButton;
    private View mRootView;
    private View[] mView;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager;
    private boolean mbLocked;
    private int mnCurrentIndex;

    public AnswerSheetV2OthersComponent(Context context) {
        super(context);
        this.mData = new String[3];
        this.mView = new View[3];
        this.mbLocked = false;
        this.mnCurrentIndex = 0;
        this.mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
        initView();
    }

    public AnswerSheetV2OthersComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mData = new String[3];
        this.mView = new View[3];
        this.mbLocked = false;
        this.mnCurrentIndex = 0;
        this.mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
        initView();
    }

    public AnswerSheetV2OthersComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mData = new String[3];
        this.mView = new View[3];
        this.mbLocked = false;
        this.mnCurrentIndex = 0;
        this.mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
        initView();
    }

    public AnswerSheetV2OthersComponent(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mData = new String[3];
        this.mView = new View[3];
        this.mbLocked = false;
        this.mnCurrentIndex = 0;
        this.mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
        initView();
    }

    public static void setData(ArrayList<AnswerSheetV2QuestionItem> arrData, AnswerSheetV2QuestionListAdapter Adapter) {
        marrData = arrData;
        mAdapter = Adapter;
    }

    public static void saveAllPreview() {
        for (int i = 0; i < marrOpenedComponents.size(); i++) {
            ((AnswerSheetV2OthersComponent) marrOpenedComponents.get(i)).saveDrawPreview();
        }
    }

    public void setIndex(int nIndex) {
        this.mnCurrentIndex = nIndex;
    }

    public static void setClientID(String szClientID) {
        mClientID = szClientID;
    }

    public static void setGlobalLock(boolean bLocked) {
        mbGlobalLock = bLocked;
    }

    public void setData(int nDataIndex, String szValue) {
        this.mData[nDataIndex] = szValue;
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        marrOpenedComponents.add(this);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.layout_answersheetv2_other, this, true);
        this.mContentLayout = (LinearLayout) this.mRootView.findViewById(R.id.linearLayoutContent);
        this.mImageButtonCamera = (ImageButton) this.mRootView.findViewById(R.id.imageButtonCamera);
        this.mImageButtonBrush = (ImageButton) this.mRootView.findViewById(R.id.imageButtonBrush);
        this.mImageButtonText = (ImageButton) this.mRootView.findViewById(R.id.imageButtonText);
        this.mNextButton = (ImageButton) this.mRootView.findViewById(R.id.imageButtonNext);
        this.mNextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int nIndex = AnswerSheetV2OthersComponent.this.mnCurrentIndex;
                while (nIndex < AnswerSheetV2OthersComponent.marrData.size() - 1) {
                    nIndex++;
                    AnswerSheetV2QuestionItem item = (AnswerSheetV2QuestionItem) AnswerSheetV2OthersComponent.marrData.get(nIndex);
                    if (item.nType != 3) {
                        if (item.nType == 4) {
                        }
                    }
                    AnswerSheetV2OthersComponent.this.saveDrawPreview();
                    AnswerSheetV2OthersComponent.this.mnCurrentIndex = nIndex;
                    AnswerSheetV2OthersComponent.this.updateDisplay();
                    return;
                }
            }
        });
        this.mBackButton = (ImageButton) this.mRootView.findViewById(R.id.imageButtonPrev);
        this.mBackButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int nIndex = AnswerSheetV2OthersComponent.this.mnCurrentIndex;
                while (nIndex > 0) {
                    nIndex--;
                    AnswerSheetV2QuestionItem item = (AnswerSheetV2QuestionItem) AnswerSheetV2OthersComponent.marrData.get(nIndex);
                    if (item.nType != 3) {
                        if (item.nType == 4) {
                        }
                    }
                    AnswerSheetV2OthersComponent.this.saveDrawPreview();
                    AnswerSheetV2OthersComponent.this.mnCurrentIndex = nIndex;
                    AnswerSheetV2OthersComponent.this.updateDisplay();
                    return;
                }
            }
        });
        TextComponent RootView0 = new TextComponent(getContext());
        RootView0.setCallBack(this);
        RootView0.setAutoHeight(false);
        this.mView[0] = RootView0;
        DrawComponent RootView1 = new DrawComponent(getContext());
        RootView1.setCallBack(this);
        this.mView[1] = RootView1;
        CameraComponent RootView2 = new CameraComponent(getContext());
        RootView2.setCallBack(this);
        this.mView[2] = RootView2;
        this.mImageButtonCamera.setTag(Integer.valueOf(2));
        this.mImageButtonBrush.setTag(Integer.valueOf(1));
        this.mImageButtonText.setTag(Integer.valueOf(0));
        this.mImageButtonCamera.setOnClickListener(this);
        this.mImageButtonBrush.setOnClickListener(this);
        this.mImageButtonText.setOnClickListener(this);
    }

    public void onClick(View v) {
        int nID = ((Integer) v.getTag()).intValue();
        ImageButton button = (ImageButton) v;
        this.mImageButtonCamera.setSelected(false);
        this.mImageButtonBrush.setSelected(false);
        this.mImageButtonText.setSelected(false);
        this.mContentLayout.removeAllViews();
        this.mContentLayout.addView(this.mView[nID], -1, -1);
        button.setSelected(true);
    }

    public void updateDisplay() {
        this.mView[0] = null;
        this.mView[1] = null;
        this.mView[2] = null;
        TextComponent RootView0 = new TextComponent(getContext());
        RootView0.setCallBack(this);
        RootView0.setAutoHeight(false);
        this.mView[0] = RootView0;
        DrawComponent RootView1 = new DrawComponent(getContext());
        RootView1.setCallBack(this);
        this.mView[1] = RootView1;
        CameraComponent RootView2 = new CameraComponent(getContext());
        RootView2.setCallBack(this);
        this.mView[2] = RootView2;
        AnswerSheetV2QuestionItem ItemData = (AnswerSheetV2QuestionItem) marrData.get(this.mnCurrentIndex);
        this.mData[0] = ItemData.szAnswer0;
        this.mData[1] = ItemData.szAnswer1;
        if (ItemData.nAnswerResult != 0) {
            this.mbLocked = true;
        }
        if (mbGlobalLock) {
            this.mbLocked = true;
        }
        View parentView = (View) getParent();
        if (parentView != null) {
            parentView = (View) parentView.getParent();
        }
        if (parentView != null) {
            View titleView = parentView.findViewById(R.id.title);
            if (titleView != null && (titleView instanceof TextView)) {
                ((TextView) titleView).setText(ItemData.szIndex);
            }
        }
        this.mData[1] = ItemData.szAnswer1;
        this.mData[2] = ItemData.szAnswer2;
        RootView0.setData(this.mData[0]);
        RootView0.setLocked(this.mbLocked);
        RootView1.setData(this.mData[1]);
        RootView1.setLocked(this.mbLocked);
        RootView2.setClientID(mClientID);
        RootView2.setData(this.mData[2]);
        RootView2.setLocked(this.mbLocked);
        if (!this.mData[0].isEmpty()) {
            this.mImageButtonText.performClick();
        } else if (!this.mData[1].isEmpty()) {
            this.mImageButtonBrush.performClick();
        } else if (this.mData[2].isEmpty()) {
            this.mImageButtonCamera.performClick();
        } else {
            this.mImageButtonCamera.performClick();
        }
        if (ItemData.nAnswerResult != 0) {
            DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(ItemData.szGuid + "_CorrectResult", null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szData = ItemObject.readTextData();
                    if (szData != null && !szData.isEmpty()) {
                        DrawComponent RootView1 = AnswerSheetV2OthersComponent.this.mView[1];
                        if (RootView1 != null) {
                            RootView1.mDrawView.fromString(ItemObject.readTextData());
                            RootView1.setLocked(true);
                        }
                    }
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                }
            });
            ResourceObject.setClientID(mClientID);
            ResourceObject.setReadOperation(true);
            ResourceObject.setAlwaysActiveCallbacks(true);
            ResourceObject.setIgnoreActivityFinishCheck(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
            this.mVirtualNetworkObjectManager.add(ResourceObject);
        }
    }

    public void setData(String szData) {
    }

    public String getData() {
        return null;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }

    public void OnDataLoaded(String szFileName, IComponents Component) {
    }

    public void OnDataUploaded(String szData, IComponents Component) {
        AnswerSheetV2QuestionItem ItemData = (AnswerSheetV2QuestionItem) marrData.get(this.mnCurrentIndex);
        if (Component.equals(this.mView[0])) {
            if (ItemData.szAnswer0.isEmpty()) {
            }
            ItemData.szAnswer0 = szData;
        } else if (Component.equals(this.mView[1])) {
            if (ItemData.szAnswer1.isEmpty()) {
            }
            if (szData.isEmpty()) {
                szData = ";";
            }
            ItemData.szAnswer1 = szData;
        } else if (Component.equals(this.mView[2])) {
            if (ItemData.szAnswer2.isEmpty()) {
            }
            ItemData.szAnswer2 = szData;
        }
        mAdapter.notifyDataSetChanged();
    }

    private void saveDrawPreview() {
        if (marrData != null && !this.mbLocked) {
            AnswerSheetV2QuestionItem ItemData = (AnswerSheetV2QuestionItem) marrData.get(this.mnCurrentIndex);
            DrawComponent drawComponent = this.mView[1];
            if (ItemData != null && drawComponent != null && !ItemData.szAnswer1.isEmpty()) {
                DrawView drawView = drawComponent.getDrawView();
                if (drawView.getWidth() > 0 && drawView.getHeight() > 0) {
                    drawView.setSize(drawView.getWidth(), drawView.getHeight());
                    Bitmap bitmap = drawView.saveToBitmap();
                    if (ItemData.szAnswer1Preview == null || ItemData.szAnswer1Preview.isEmpty()) {
                        ItemData.szAnswer1Preview = "DrawViewPreview_" + Utilities.createGUID();
                    }
                    DataSynchronizeItemObject CallItem = new DataSynchronizeItemObject(ItemData.szAnswer1Preview, null);
                    CallItem.setSuccessListener(new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        }
                    });
                    CallItem.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        }
                    });
                    CallItem.writeTextData(Utilities.saveBitmapToBase64String(bitmap));
                    CallItem.setReadOperation(false);
                    CallItem.setClientID(mClientID);
                    CallItem.setAlwaysActiveCallbacks(true);
                    VirtualNetworkObject.addToQueue(CallItem);
                }
            }
        }
    }

    public void OnRequestIntent(Intent intent, IComponents Component) {
        final CameraComponent RootView = (CameraComponent) Component;
        Intent intent2 = new Intent(getContext(), CameraCaptureActivity.class);
        CameraCaptureActivity.setCallBack(new CameraCaptureCallBack() {
            public void onCaptureComplete(String szFileName) {
                Intent intent = new Intent();
                intent.setData(Uri.fromFile(new File(szFileName)));
                RootView.intentComplete(intent);
            }
        });
        intent2.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        getContext().startActivity(intent2);
    }

    protected void onDetachedFromWindow() {
        saveDrawPreview();
        this.mVirtualNetworkObjectManager.cancelAll();
        for (int i = 0; i < marrOpenedComponents.size(); i++) {
            if (((AnswerSheetV2OthersComponent) marrOpenedComponents.get(i)).equals(this)) {
                marrOpenedComponents.remove(i);
                break;
            }
        }
        super.onDetachedFromWindow();
    }
}
