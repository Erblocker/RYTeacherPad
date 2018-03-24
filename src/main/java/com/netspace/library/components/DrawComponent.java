package com.netspace.library.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.netspace.library.bluetooth.BlueToothPen;
import com.netspace.library.bluetooth.BlueToothPen.PenActionInterface;
import com.netspace.library.controls.ColorPickerView;
import com.netspace.library.controls.CustomGraphicCanvas;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.controls.FriendlyPoint;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.controls.Point;
import com.netspace.library.graphics.CircleGraphic;
import com.netspace.library.graphics.CoordinatesGraphic;
import com.netspace.library.graphics.MultiLineGraphic;
import com.netspace.library.graphics.OvalGraphic;
import com.netspace.library.graphics.ParallelogramGraphic;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.utilities.MoveableObject;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.io.File;
import java.util.ArrayList;

public class DrawComponent extends FrameLayout implements IComponents, OnScrollChangedListener, PenActionInterface {
    private static ArrayList<DrawComponentGraphic> mGraphics = new ArrayList();
    private BlueToothPen mBlueToothPen;
    private ImageView mBrushButton;
    private String mCacheFileName;
    private ComponentCallBack mCallBack;
    private ImageView mCameraButton;
    private ImageView mCancelButton;
    private ImageView mCapturePadButton;
    private ImageView mColorButton;
    private LinearLayout mColorPickerLayout;
    private ColorPickerView mColorPickerView;
    private Context mContextThemeWrapper;
    private DrawComponentCallBack mDrawComponentCallBack;
    DrawView mDrawView;
    private ImageView mEmptyButton;
    private ImageView mEraseButton;
    private CustomGraphicCanvas mGraphicCanvas;
    private ImageView mGraphicsButton;
    private Runnable mHidePointRunnable = new Runnable() {
        public void run() {
            DrawComponent.this.mPointer.setVisibility(4);
        }
    };
    private Point mLastPoint;
    private ImageView mOKButton;
    private Runnable mOnTouchDownRunnable = new Runnable() {
        public void run() {
            if (DrawComponent.this.mCallBack != null) {
                DrawComponent.this.mCallBack.OnDataUploaded(DrawComponent.this.getData(), DrawComponent.this);
                DrawComponent.this.mbChanged = true;
            }
        }
    };
    private ImageView mPencialButton;
    private ImageView mPointer;
    private RelativeLayout mRelativeLayout;
    private View mRootView;
    private ImageView mSaveButton;
    private Toolbar mToolbar;
    private ImageView mZoomButton;
    private boolean mbCacheToDisk;
    private boolean mbChanged = false;
    private boolean mbDrawPending = false;
    private boolean mbWindowMode = false;
    private String mszDrawData = null;

    public interface DrawComponentCallBack {
        void OnCamera(DrawView drawView);

        void OnCapturePad(DrawView drawView);

        void OnDrawViewTouchUp(DrawView drawView);

        void OnSave(DrawView drawView);

        void OnZoomout(DrawView drawView);
    }

    public interface DrawComponentGraphic {
        boolean addPoint(CustomGraphicCanvas customGraphicCanvas, float f, float f2);

        String getName();

        boolean init(CustomGraphicCanvas customGraphicCanvas);

        boolean measureToDrawView(CustomGraphicCanvas customGraphicCanvas, DrawView drawView, float f, float f2);

        void onDrawPreviewContent(CustomGraphicCanvas customGraphicCanvas, Canvas canvas);

        void onMoveObjectResize(CustomGraphicCanvas customGraphicCanvas);

        void onPrepareMoveObject(CustomGraphicCanvas customGraphicCanvas, MoveableObject moveableObject);
    }

    public DrawComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DrawComponent(Context context) {
        super(context);
        initView();
    }

    public DrawComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setWindowMode(boolean bOn) {
        this.mbWindowMode = bOn;
    }

    protected void cacheToDisk() {
        if (!this.mbCacheToDisk) {
            if (this.mCacheFileName == null) {
                this.mCacheFileName = new StringBuilder(String.valueOf(getContext().getCacheDir().getAbsolutePath())).append("/DrawComponent_Cache_").append(Utilities.createGUID()).append(".png").toString();
            }
            this.mDrawView.saveCacheToBitmap(this.mCacheFileName);
            this.mbCacheToDisk = true;
        }
    }

    protected void loadFromDisk() {
        if (this.mbCacheToDisk && this.mCacheFileName != null) {
            this.mDrawView.loadCacheFromDisk(this.mCacheFileName);
            this.mbCacheToDisk = false;
            new File(this.mCacheFileName).delete();
            this.mCacheFileName = null;
        }
    }

    protected boolean isCachedToDisk() {
        return this.mbCacheToDisk;
    }

    public static void registerGraphic(DrawComponentGraphic Graphic) {
        mGraphics.add(Graphic);
    }

    public static ArrayList<DrawComponentGraphic> getGraphics() {
        return mGraphics;
    }

    public static void registerAvailableGraphics() {
        mGraphics.clear();
        registerGraphic(new MultiLineGraphic());
        registerGraphic(new CircleGraphic());
        registerGraphic(new OvalGraphic());
        registerGraphic(new ParallelogramGraphic());
        registerGraphic(new CoordinatesGraphic());
    }

    public static void unregisterGraphics() {
        mGraphics.clear();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        LayoutInflater localInflater = inflater;
        if (getContext() instanceof Activity) {
            this.mContextThemeWrapper = getContext();
        } else {
            this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
            localInflater = inflater.cloneInContext(this.mContextThemeWrapper);
        }
        this.mRootView = localInflater.inflate(R.layout.component_draw, this, true);
        this.mRelativeLayout = (RelativeLayout) this.mRootView.findViewById(R.id.RelativeLayout1);
        this.mDrawView = (DrawView) this.mRootView.findViewById(R.id.drawPad);
        this.mColorPickerView = (ColorPickerView) this.mRootView.findViewById(R.id.color_picker_view);
        this.mPointer = (ImageView) this.mRootView.findViewById(R.id.imageViewPointer);
        this.mColorPickerLayout = (LinearLayout) this.mRootView.findViewById(R.id.layoutColorPickerView);
        this.mDrawView.setEnableCache(true);
        this.mDrawView.setFocusable(false);
        this.mDrawView.setBackgroundResource(R.drawable.background_drawpad);
        this.mDrawView.setCallback(new DrawViewActionInterface() {
            public void OnTouchDown() {
                DrawComponent.this.hideColorPicker();
                if (DrawComponent.this.mDrawView.getBrushMode()) {
                    DrawComponent.this.mDrawView.removeCallbacks(DrawComponent.this.mOnTouchDownRunnable);
                    if (DrawComponent.this.mCallBack != null) {
                        DrawComponent.this.mDrawView.postDelayed(DrawComponent.this.mOnTouchDownRunnable, 500);
                    }
                }
            }

            public void OnTouchMove() {
                DrawComponent.this.mDrawView.removeCallbacks(DrawComponent.this.mOnTouchDownRunnable);
                if (DrawComponent.this.mCallBack != null) {
                    DrawComponent.this.mDrawView.postDelayed(DrawComponent.this.mOnTouchDownRunnable, 500);
                }
            }

            public void OnTouchUp() {
                if (DrawComponent.this.mDrawComponentCallBack != null) {
                    DrawComponent.this.mDrawComponentCallBack.OnDrawViewTouchUp(DrawComponent.this.mDrawView);
                }
                DrawComponent.this.mDrawView.removeCallbacks(DrawComponent.this.mOnTouchDownRunnable);
                if (DrawComponent.this.mCallBack != null) {
                    DrawComponent.this.mDrawView.postDelayed(DrawComponent.this.mOnTouchDownRunnable, 50);
                }
            }

            public void OnPenButtonDown() {
            }

            public void OnPenButtonUp() {
            }

            public void OnTouchPen() {
                DrawComponent.this.hideColorPicker();
                if (DrawComponent.this.mDrawView.getBrushMode() && DrawComponent.this.mCallBack != null && !DrawComponent.this.mbChanged) {
                    DrawComponent.this.mCallBack.OnDataUploaded(DrawComponent.this.getData(), DrawComponent.this);
                    DrawComponent.this.mbChanged = true;
                }
            }

            public void OnTouchFinger() {
                DrawComponent.this.hideColorPicker();
            }

            public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
            }
        });
        this.mEmptyButton = (ImageView) findViewById(R.id.buttonEmpty);
        this.mEmptyButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                Utilities.showAlertMessage(new Builder(DrawComponent.this.getContext()).setTitle("清除全部内容").setMessage("确实清除全部内容吗？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DrawComponent.this.mDrawView.clearPoints();
                        if (DrawComponent.this.mCallBack != null) {
                            DrawComponent.this.mCallBack.OnDataUploaded(DrawComponent.this.getData(), DrawComponent.this);
                            DrawComponent.this.mbChanged = true;
                        }
                    }
                }).setNegativeButton("否", null));
            }
        });
        this.mPencialButton = (ImageView) findViewById(R.id.buttonPencial);
        this.mPencialButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (v.isSelected()) {
                    DrawComponent.this.mPencialButton.setSelected(false);
                    DrawComponent.this.mEraseButton.setSelected(false);
                    DrawComponent.this.mBrushButton.setSelected(false);
                    DrawComponent.this.mDrawView.setBrushMode(false);
                    DrawComponent.this.lockParents(false);
                    return;
                }
                DrawComponent.this.mPencialButton.setSelected(true);
                DrawComponent.this.mEraseButton.setSelected(false);
                DrawComponent.this.mBrushButton.setSelected(false);
                DrawComponent.this.mDrawView.setEraseMode(false);
                DrawComponent.this.mDrawView.setBrushMode(true);
                DrawComponent.this.mDrawView.changeWidth(3);
                DrawComponent.this.lockParents(true);
                DrawComponent.this.setFocusable(true);
                DrawComponent.this.setFocusableInTouchMode(true);
                DrawComponent.this.requestFocus();
                DrawComponent.this.enableGraphic(true);
            }
        });
        this.mEraseButton = (ImageView) findViewById(R.id.buttonEraser);
        this.mEraseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (v.isSelected()) {
                    DrawComponent.this.mPencialButton.setSelected(false);
                    DrawComponent.this.mEraseButton.setSelected(false);
                    DrawComponent.this.mBrushButton.setSelected(false);
                    DrawComponent.this.mDrawView.setEraseMode2(false, 0);
                    DrawComponent.this.lockParents(false);
                    return;
                }
                DrawComponent.this.mEraseButton.setSelected(true);
                DrawComponent.this.mBrushButton.setSelected(false);
                DrawComponent.this.mPencialButton.setSelected(false);
                DrawComponent.this.mDrawView.setEraseMode2(true, 0);
                DrawComponent.this.lockParents(true);
                DrawComponent.this.setFocusable(true);
                DrawComponent.this.setFocusableInTouchMode(true);
                DrawComponent.this.requestFocus();
                DrawComponent.this.enableGraphic(false);
            }
        });
        this.mBrushButton = (ImageView) findViewById(R.id.buttonBrush);
        this.mBrushButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (v.isSelected()) {
                    DrawComponent.this.mPencialButton.setSelected(false);
                    DrawComponent.this.mEraseButton.setSelected(false);
                    DrawComponent.this.mBrushButton.setSelected(false);
                    DrawComponent.this.mDrawView.setBrushMode(false);
                    DrawComponent.this.mDrawView.setEraseMode(false);
                    DrawComponent.this.lockParents(false);
                    return;
                }
                DrawComponent.this.mBrushButton.setSelected(true);
                DrawComponent.this.mEraseButton.setSelected(false);
                DrawComponent.this.mPencialButton.setSelected(false);
                DrawComponent.this.mDrawView.setEraseMode(false);
                DrawComponent.this.mDrawView.setBrushMode(true);
                DrawComponent.this.mDrawView.changeWidth(10);
                DrawComponent.this.lockParents(true);
                DrawComponent.this.setFocusable(true);
                DrawComponent.this.setFocusableInTouchMode(true);
                DrawComponent.this.requestFocus();
                DrawComponent.this.enableGraphic(true);
            }
        });
        this.mColorButton = (ImageView) findViewById(R.id.buttonColorize);
        this.mColorButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (v.isSelected()) {
                    DrawComponent.this.mDrawView.setColor(DrawComponent.this.mColorPickerView.getColor());
                    DrawComponent.this.mColorPickerLayout.setVisibility(4);
                    v.setSelected(false);
                    return;
                }
                DrawComponent.this.mColorPickerLayout.setVisibility(0);
                v.setSelected(true);
            }
        });
        this.mSaveButton = (ImageView) findViewById(R.id.buttonSave);
        this.mSaveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (DrawComponent.this.mDrawComponentCallBack != null) {
                    DrawComponent.this.mDrawComponentCallBack.OnSave(DrawComponent.this.mDrawView);
                }
            }
        });
        this.mZoomButton = (ImageView) findViewById(R.id.buttonZoomOut);
        this.mZoomButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (DrawComponent.this.mDrawComponentCallBack != null) {
                    DrawComponent.this.mDrawComponentCallBack.OnZoomout(DrawComponent.this.mDrawView);
                }
            }
        });
        this.mCapturePadButton = (ImageView) findViewById(R.id.buttonCapturePad);
        this.mCapturePadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (DrawComponent.this.mDrawComponentCallBack != null) {
                    DrawComponent.this.mDrawComponentCallBack.OnCapturePad(DrawComponent.this.mDrawView);
                }
            }
        });
        this.mCameraButton = (ImageView) findViewById(R.id.buttonCamera);
        this.mCameraButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (DrawComponent.this.mDrawComponentCallBack != null) {
                    DrawComponent.this.mDrawComponentCallBack.OnCamera(DrawComponent.this.mDrawView);
                }
            }
        });
        this.mGraphicsButton = (ImageView) findViewById(R.id.buttonGraphics);
        this.mGraphicsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                PopupMenu popup = new PopupMenu(DrawComponent.this.mContextThemeWrapper, v);
                for (int i = 0; i < DrawComponent.mGraphics.size(); i++) {
                    popup.getMenu().add(0, i, i, ((DrawComponentGraphic) DrawComponent.mGraphics.get(i)).getName());
                }
                popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        DrawComponentGraphic DrawComponentGraphic = (DrawComponentGraphic) DrawComponent.mGraphics.get(item.getItemId());
                        if (DrawComponent.this.mGraphicCanvas != null) {
                            DrawComponent.this.mRelativeLayout.removeView(DrawComponent.this.mGraphicCanvas);
                            DrawComponent.this.mGraphicCanvas = null;
                        }
                        DrawComponent.this.mGraphicCanvas = new CustomGraphicCanvas(DrawComponent.this.mContextThemeWrapper);
                        DrawComponent.this.mGraphicCanvas.setGraphic(DrawComponentGraphic);
                        DrawComponent.this.mRelativeLayout.addView(DrawComponent.this.mGraphicCanvas, 200, 100);
                        DrawComponent.this.mGraphicCanvas.setBackgroundResource(R.drawable.background_grahpiccanvas);
                        DrawComponent.this.mGraphicCanvas.setDrawView(DrawComponent.this.mDrawView);
                        DrawComponentGraphic.init(DrawComponent.this.mGraphicCanvas);
                        LayoutParams Params = (LayoutParams) DrawComponent.this.mGraphicCanvas.getLayoutParams();
                        Params.topMargin = DrawComponent.this.mDrawView.getTop();
                        Params.leftMargin = DrawComponent.this.mDrawView.getLeft();
                        Params.width = DrawComponent.this.mDrawView.getWidth();
                        Params.height = DrawComponent.this.mDrawView.getHeight();
                        DrawComponent.this.enableOtherButtons(false);
                        DrawComponent.this.mOKButton.setVisibility(0);
                        DrawComponent.this.mCancelButton.setVisibility(0);
                        return false;
                    }
                });
                popup.show();
            }
        });
        this.mOKButton = (ImageView) findViewById(R.id.buttonOK);
        this.mOKButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (DrawComponent.this.mGraphicCanvas != null) {
                    DrawComponent.this.mGraphicCanvas.measureDataToDrawView();
                    DrawComponent.this.mGraphicCanvas.setVisibility(8);
                    DrawComponent.this.mRelativeLayout.removeView(DrawComponent.this.mGraphicCanvas);
                    DrawComponent.this.mGraphicCanvas = null;
                    DrawComponent.this.mOKButton.setVisibility(8);
                    DrawComponent.this.mCancelButton.setVisibility(8);
                    if (DrawComponent.this.mCallBack != null) {
                        DrawComponent.this.mCallBack.OnDataUploaded(DrawComponent.this.getData(), DrawComponent.this);
                    }
                    DrawComponent.this.mbChanged = true;
                }
                DrawComponent.this.enableOtherButtons(true);
            }
        });
        this.mCancelButton = (ImageView) findViewById(R.id.buttonCancel);
        this.mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (DrawComponent.this.mGraphicCanvas != null) {
                    DrawComponent.this.mGraphicCanvas.setVisibility(8);
                    DrawComponent.this.mRelativeLayout.removeView(DrawComponent.this.mGraphicCanvas);
                    DrawComponent.this.mGraphicCanvas = null;
                }
                DrawComponent.this.mOKButton.setVisibility(8);
                DrawComponent.this.mCancelButton.setVisibility(8);
                DrawComponent.this.enableOtherButtons(true);
            }
        });
    }

    private void enableGraphic(boolean bEnable) {
        if (bEnable) {
            this.mGraphicsButton.setEnabled(true);
            this.mGraphicsButton.setAlpha(1.0f);
            return;
        }
        this.mGraphicsButton.setEnabled(false);
        this.mGraphicsButton.setAlpha(0.5f);
    }

    private void enableOtherButtons(boolean bEnable) {
        ArrayList<ImageView> arrButtons = new ArrayList();
        arrButtons.add(this.mBrushButton);
        arrButtons.add(this.mPencialButton);
        arrButtons.add(this.mColorButton);
        arrButtons.add(this.mGraphicsButton);
        arrButtons.add(this.mEraseButton);
        arrButtons.add(this.mSaveButton);
        arrButtons.add(this.mZoomButton);
        arrButtons.add(this.mEmptyButton);
        arrButtons.add(this.mCameraButton);
        arrButtons.add(this.mCapturePadButton);
        for (int i = 0; i < arrButtons.size(); i++) {
            ImageView oneButton = (ImageView) arrButtons.get(i);
            if (oneButton.getVisibility() == 0) {
                if (bEnable) {
                    oneButton.setEnabled(true);
                    oneButton.setAlpha(1.0f);
                } else {
                    oneButton.setEnabled(false);
                    oneButton.setAlpha(0.5f);
                }
            }
        }
    }

    public DrawView getDrawView() {
        return this.mDrawView;
    }

    public void setDrawComponentCallBack(DrawComponentCallBack DrawComponentCallBack) {
        this.mDrawComponentCallBack = DrawComponentCallBack;
        this.mSaveButton.setVisibility(0);
        this.mZoomButton.setVisibility(0);
        this.mCameraButton.setVisibility(0);
        this.mCapturePadButton.setVisibility(0);
        this.mEmptyButton.setVisibility(8);
    }

    public void setDrawComponentCallBack(DrawComponentCallBack DrawComponentCallBack, boolean bEnableOtherButton) {
        this.mDrawComponentCallBack = DrawComponentCallBack;
        if (bEnableOtherButton) {
            this.mSaveButton.setVisibility(0);
            this.mZoomButton.setVisibility(0);
            this.mCameraButton.setVisibility(0);
            this.mCapturePadButton.setVisibility(0);
            this.mEmptyButton.setVisibility(8);
        }
    }

    protected void hideColorPicker() {
        if (this.mColorPickerLayout.getVisibility() == 0) {
            this.mDrawView.setColor(this.mColorPickerView.getColor());
            this.mColorPickerLayout.setVisibility(4);
            this.mColorButton.setSelected(false);
        }
    }

    protected void lockParents(boolean bLock) {
        View parentView = (View) getParent();
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

    public void setData(String szData) {
        if (this.mbWindowMode) {
            this.mDrawView.fromString(szData);
        } else if (getLocalVisibleRect(new Rect())) {
            this.mDrawView.fromString(szData);
        } else {
            this.mbDrawPending = true;
            this.mszDrawData = szData;
        }
    }

    public String getData() {
        return this.mDrawView.getDataAsString();
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int nNewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int nNewHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (nNewWidth != getWidth() || nNewHeight != getHeight()) {
            this.mDrawView.cleanCache();
        }
    }

    public void setLocked(boolean bLock) {
        boolean z;
        boolean z2 = false;
        if (bLock) {
            if (this.mPencialButton.isSelected()) {
                this.mPencialButton.performClick();
            }
            if (this.mEraseButton.isSelected()) {
                this.mEraseButton.performClick();
            }
            if (this.mBrushButton.isSelected()) {
                this.mBrushButton.performClick();
            }
            if (this.mColorButton.isSelected()) {
                this.mColorButton.performClick();
            }
        }
        this.mPencialButton.setEnabled(!bLock);
        ImageView imageView = this.mEraseButton;
        if (bLock) {
            z = false;
        } else {
            z = true;
        }
        imageView.setEnabled(z);
        imageView = this.mBrushButton;
        if (bLock) {
            z = false;
        } else {
            z = true;
        }
        imageView.setEnabled(z);
        imageView = this.mColorButton;
        if (bLock) {
            z = false;
        } else {
            z = true;
        }
        imageView.setEnabled(z);
        imageView = this.mGraphicsButton;
        if (bLock) {
            z = false;
        } else {
            z = true;
        }
        imageView.setEnabled(z);
        ImageView imageView2 = this.mEmptyButton;
        if (!bLock) {
            z2 = true;
        }
        imageView2.setEnabled(z2);
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (!gainFocus) {
            this.mPointer.setVisibility(4);
            if (this.mPencialButton.isSelected()) {
                this.mPencialButton.performClick();
            }
            if (this.mEraseButton.isSelected()) {
                this.mEraseButton.performClick();
            }
            if (this.mBrushButton.isSelected()) {
                this.mBrushButton.performClick();
            }
            if (this.mColorButton.isSelected()) {
                this.mColorButton.performClick();
            }
            if (this.mBlueToothPen != null) {
                this.mBlueToothPen.stop();
            }
        } else if (this.mBlueToothPen != null) {
            this.mBlueToothPen.start();
        }
    }

    protected void onAttachedToWindow() {
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(this);
        }
        this.mBlueToothPen = new BlueToothPen();
        this.mBlueToothPen.setCallBack(this);
        super.onAttachedToWindow();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressLint({"NewApi"})
            public void onGlobalLayout() {
                DrawComponent.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (DrawComponent.this.getLocalVisibleRect(new Rect())) {
                    DrawComponent.this.onScrollChanged();
                }
            }
        });
    }

    protected void onDetachedFromWindow() {
        if (this.mCacheFileName != null) {
            new File(this.mCacheFileName).delete();
            this.mCacheFileName = null;
        }
        if (this.mBlueToothPen != null) {
            this.mBlueToothPen.stop();
            this.mBlueToothPen = null;
        }
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.removeOnScrollChangedListener(this);
        }
        super.onDetachedFromWindow();
    }

    public void onScrollChanged() {
        if (getLocalVisibleRect(new Rect())) {
            if (this.mbDrawPending) {
                this.mDrawView.fromString(this.mszDrawData);
                this.mszDrawData = null;
                this.mbDrawPending = false;
            }
            if (this.mbCacheToDisk) {
                loadFromDisk();
                return;
            }
            return;
        }
        Runtime info = Runtime.getRuntime();
        if (info.totalMemory() - info.freeMemory() > 100663296 && !this.mbCacheToDisk) {
            cacheToDisk();
        }
    }

    public void onPenAction(String szAction, int nX, int nY, float fPressure) {
        boolean bShowPointer = false;
        if (szAction.equalsIgnoreCase("write")) {
            if (this.mLastPoint != null) {
                final FriendlyPoint friendlyPoint = new FriendlyPoint((float) nX, (float) nY, this.mDrawView.getColor(), this.mLastPoint, (int) (3.0f * fPressure));
                getDrawView().post(new Runnable() {
                    public void run() {
                        if (!DrawComponent.this.mDrawView.getBrushMode()) {
                            DrawComponent.this.mDrawView.setEraseMode(false);
                            DrawComponent.this.mDrawView.setBrushMode(true);
                        }
                        DrawComponent.this.mDrawView.changeWidth(3);
                        DrawComponent.this.mDrawView.addPoint(friendlyPoint);
                        DrawComponent.this.mDrawView.invalidate();
                    }
                });
                this.mLastPoint = friendlyPoint;
            } else {
                final Point Point = new Point((float) nX, (float) nY, this.mDrawView.getColor(), (int) (3.0f * fPressure));
                getDrawView().post(new Runnable() {
                    public void run() {
                        if (!DrawComponent.this.mDrawView.getBrushMode()) {
                            DrawComponent.this.mDrawView.setEraseMode(false);
                            DrawComponent.this.mDrawView.setBrushMode(true);
                        }
                        DrawComponent.this.mDrawView.changeWidth(3);
                        DrawComponent.this.mDrawView.addPoint(Point);
                        DrawComponent.this.mDrawView.invalidate();
                    }
                });
                this.mLastPoint = Point;
            }
            bShowPointer = true;
        } else if (szAction.equalsIgnoreCase("move")) {
            bShowPointer = true;
            this.mLastPoint = null;
        } else if (szAction.equalsIgnoreCase("reset")) {
            bShowPointer = false;
            this.mLastPoint = null;
        }
        if (bShowPointer) {
            final int nPointerX = nX;
            final int nPointerY = nY;
            this.mPointer.post(new Runnable() {
                public void run() {
                    DrawComponent.this.mPointer.setVisibility(0);
                    int nWidth = DrawComponent.this.mPointer.getWidth();
                    int nHeight = DrawComponent.this.mPointer.getHeight();
                    LayoutParams param = (LayoutParams) DrawComponent.this.mPointer.getLayoutParams();
                    param.leftMargin = nPointerX - (nWidth / 2);
                    param.topMargin = (DrawComponent.this.mDrawView.getTop() + nPointerY) - (nHeight / 2);
                    DrawComponent.this.mPointer.setLayoutParams(param);
                }
            });
            this.mPointer.removeCallbacks(this.mHidePointRunnable);
            this.mPointer.postDelayed(this.mHidePointRunnable, 5000);
            return;
        }
        this.mPointer.post(new Runnable() {
            public void run() {
                DrawComponent.this.mPointer.setVisibility(4);
            }
        });
    }

    public void onPenConnected() {
    }

    public void onPenDisconnected() {
    }
}
