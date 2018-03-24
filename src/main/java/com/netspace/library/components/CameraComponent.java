package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import com.netspace.library.activity.PictureActivity2;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.pad.library.R;
import java.io.File;

public class CameraComponent extends CustomFrameLayout implements IComponents, OnScrollChangedListener {
    private static final String TAG = "CameraComponent";
    private String mBase64ImageData;
    private Button mButtonCamera;
    private String mCacheFileName;
    private ComponentCallBack mCallBack;
    private LinearLayout mCaptureLayout;
    private String mClientID;
    private ContextThemeWrapper mContextThemeWrapper;
    private String mData;
    private ImageView mImageView;
    private View mRootView;
    private TextView mTextMessage;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    private boolean mbCacheToDisk = false;
    private boolean mbDataLoaded = false;
    private boolean mbDetachedFromWindow = false;
    private boolean mbLocked = false;

    public CameraComponent(Context context) {
        super(context);
        initView();
    }

    public CameraComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CameraComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_camera, this, true);
        this.mImageView = (ImageView) this.mRootView.findViewById(R.id.imageView1);
        this.mTextMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mButtonCamera = (Button) this.mRootView.findViewById(R.id.buttonCamera);
        this.mButtonCamera.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                Intent intent = new Intent();
                if (CameraComponent.this.mCallBack != null) {
                    CameraComponent.this.mCallBack.OnRequestIntent(intent, CameraComponent.this);
                }
            }
        });
        this.mCaptureLayout = (LinearLayout) this.mRootView.findViewById(R.id.recordLayout);
    }

    public void setData(String szData) {
        if (szData == null) {
            szData = "";
        }
        this.mData = szData;
        if (this.mData.isEmpty()) {
            this.mRootView.findViewById(R.id.recordLayout).setVisibility(0);
            this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
            this.mRootView.findViewById(R.id.textViewMessage).setVisibility(4);
            this.mButtonCamera.setVisibility(0);
            return;
        }
        DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(this.mData, null);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                CameraComponent.this.mbDataLoaded = true;
                if (!CameraComponent.this.mbDetachedFromWindow) {
                    String szData = ItemObject.readTextData();
                    Bitmap bitmap = null;
                    if (szData != null) {
                        bitmap = Utilities.getBase64Bitmap(szData);
                        CameraComponent.this.mBase64ImageData = szData;
                        if (CameraComponent.this.mCacheFileName == null) {
                            CameraComponent.this.mCacheFileName = new StringBuilder(String.valueOf(CameraComponent.this.getContext().getCacheDir().getAbsolutePath())).append("/CameraComponent_Cache_").append(Utilities.createGUID()).append(".jpg").toString();
                            Utilities.saveBitmapToJpeg(CameraComponent.this.mCacheFileName, bitmap);
                        }
                    }
                    if (bitmap == null) {
                        CameraComponent.this.mRootView.findViewById(R.id.recordLayout).setVisibility(4);
                        CameraComponent.this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                        CameraComponent.this.mRootView.findViewById(R.id.textViewMessage).setVisibility(0);
                        CameraComponent.this.mTextMessage.setText("读取数据时发生错误，错误原因：数据无效，服务器端无法获取所需数据。");
                        return;
                    }
                    CameraComponent.this.mImageView.setImageBitmap(bitmap);
                    CameraComponent.this.mImageView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            PictureActivity2.setBase64Bitmap(CameraComponent.this.mBase64ImageData);
                            Intent intent = new Intent(CameraComponent.this.getContext(), PictureActivity2.class);
                            intent.setFlags(805306368);
                            CameraComponent.this.getContext().startActivity(intent);
                        }
                    });
                    CameraComponent.this.mImageView.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            if (!CameraComponent.this.mbLocked) {
                                PopupMenu popup = new PopupMenu(CameraComponent.this.mContextThemeWrapper, v);
                                popup.getMenu().add(0, 1, 0, "重新拍照");
                                popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                                    public boolean onMenuItemClick(MenuItem item) {
                                        if (item.getItemId() != 1) {
                                            return false;
                                        }
                                        CameraComponent.this.setData("");
                                        CameraComponent.this.mImageView.setImageResource(R.drawable.ic_camera);
                                        return true;
                                    }
                                });
                                popup.show();
                            }
                            return false;
                        }
                    });
                    CameraComponent.this.mRootView.findViewById(R.id.recordLayout).setVisibility(0);
                    CameraComponent.this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                    CameraComponent.this.mRootView.findViewById(R.id.textViewMessage).setVisibility(4);
                    CameraComponent.this.mButtonCamera.setVisibility(8);
                }
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                CameraComponent.this.mRootView.findViewById(R.id.recordLayout).setVisibility(4);
                CameraComponent.this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                CameraComponent.this.mRootView.findViewById(R.id.textViewMessage).setVisibility(0);
                CameraComponent.this.mTextMessage.setText("读取数据时发生错误，错误原因：" + ItemObject.getErrorText());
            }
        });
        ResourceObject.setParam("lpszKey", szData);
        ResourceObject.setAlwaysActiveCallbacks(true);
        ResourceObject.setReadOperation(true);
        ResourceObject.setClientID(this.mClientID);
        VirtualNetworkObject.addToQueue(ResourceObject);
        this.mVirtualNetworkObjectManager.add(ResourceObject);
        this.mRootView.findViewById(R.id.recordLayout).setVisibility(4);
        this.mRootView.findViewById(R.id.loadingLayout).setVisibility(0);
        this.mRootView.findViewById(R.id.textViewMessage).setVisibility(4);
    }

    public String getData() {
        return this.mData;
    }

    public void setClientID(String szClientID) {
        this.mClientID = szClientID;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
        Uri pictureUri = intent.getData();
        String szFileName = pictureUri.getPath();
        String szKey = "Camera_" + Utilities.createGUID() + ".jpg";
        this.mData = szKey;
        this.mImageView.setImageURI(pictureUri);
        this.mButtonCamera.setVisibility(8);
        this.mRootView.findViewById(R.id.recordLayout).setVisibility(0);
        this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
        DataSynchronizeItemObject CallItem = new DataSynchronizeItemObject(szKey, null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                CameraComponent.this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                CameraComponent.this.mRootView.findViewById(R.id.textViewMessage).setVisibility(4);
                if (CameraComponent.this.mCallBack != null) {
                    CameraComponent.this.mCallBack.OnDataUploaded(CameraComponent.this.mData, CameraComponent.this);
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                CameraComponent.this.mRootView.findViewById(R.id.loadingLayout).setVisibility(4);
                TextView textView = (TextView) CameraComponent.this.mRootView.findViewById(R.id.textViewMessage);
                textView.setVisibility(0);
                textView.setText("数据上传失败，错误原因：" + ItemObject.getErrorText());
            }
        });
        CallItem.writeTextData(Utilities.getBase64FileContent(szFileName));
        CallItem.setReadOperation(false);
        CallItem.setClientID(this.mClientID);
        CallItem.setAlwaysActiveCallbacks(true);
        this.mRootView.findViewById(R.id.loadingLayout).setVisibility(0);
        TextView textView = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        textView.setVisibility(0);
        textView.setText("正在上传数据...");
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public void setLocked(boolean bLock) {
        this.mButtonCamera.setEnabled(!bLock);
        this.mbLocked = bLock;
    }

    protected void onAttachedToWindow() {
        boolean bisInViewport = getLocalVisibleRect(new Rect());
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(this);
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        if (this.mCacheFileName != null) {
            new File(this.mCacheFileName).delete();
            this.mCacheFileName = null;
        }
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.removeOnScrollChangedListener(this);
        }
        this.mbDetachedFromWindow = true;
        this.mVirtualNetworkObjectManager.cancelAll();
        super.onDetachedFromWindow();
    }

    public void onScrollChanged() {
        if (!this.mbDataLoaded) {
            return;
        }
        if (!getLocalVisibleRect(new Rect())) {
            Runtime info = Runtime.getRuntime();
            if (info.totalMemory() - info.freeMemory() > 100663296 && !this.mbCacheToDisk) {
                cacheToDisk();
            }
        } else if (this.mbCacheToDisk) {
            loadFromDisk();
        }
    }

    private void cacheToDisk() {
        Drawable Drawable = this.mImageView.getDrawable();
        if (Drawable != null && (Drawable instanceof BitmapDrawable)) {
            Bitmap bitmap = ((BitmapDrawable) Drawable).getBitmap();
            if (bitmap != null) {
                Log.d(TAG, "SaveCacheToDisk");
                if (this.mCacheFileName == null) {
                    this.mCacheFileName = new StringBuilder(String.valueOf(getContext().getCacheDir().getAbsolutePath())).append("/CameraComponent_Cache_").append(Utilities.createGUID()).append(".jpg").toString();
                    Utilities.saveBitmapToJpeg(this.mCacheFileName, bitmap);
                }
                this.mBase64ImageData = "";
                this.mImageView.setImageBitmap(null);
                this.mbCacheToDisk = true;
            }
        }
    }

    private void loadFromDisk() {
        if (this.mCacheFileName != null) {
            Log.d(TAG, "LoadCacheFromDisk");
            Bitmap bitmap = Utilities.loadBitmapFromFile(this.mCacheFileName);
            if (bitmap != null) {
                this.mImageView.setImageBitmap(bitmap);
                this.mBase64ImageData = Utilities.saveBitmapToBase64String(bitmap);
            }
            this.mbCacheToDisk = false;
        }
    }
}
