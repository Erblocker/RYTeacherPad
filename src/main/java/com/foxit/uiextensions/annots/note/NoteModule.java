package com.foxit.uiextensions.annots.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.InputDeviceCompat;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;

public class NoteModule implements Module, PropertyChangeListener {
    private int mAnnotColor;
    private NoteAnnotHandler mAnnotHandler;
    private String mAnnotIconType;
    private int mAnnotOpacity;
    private ArrayList<BitmapDrawable> mBitmapDrawables;
    private ColorChangeListener mColorChangeListener = null;
    private Context mContext;
    private int mCurrentColor;
    private String mCurrentIconType;
    private int mCurrentOpacity;
    private AppDisplay mDisplay;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            NoteModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private Paint mPaint;
    private ViewGroup mParentView;
    private PDFViewCtrl mPdfViewCtrl;
    private NoteToolHandler mToolHandler;
    private UIExtensionsManager mUIExtensionsManager;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (NoteModule.this.mAnnotHandler.getAnnotMenu() != null && NoteModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                NoteModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (NoteModule.this.mAnnotHandler.getPropertyBar() != null && NoteModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                NoteModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public NoteModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mDisplay = new AppDisplay(context);
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParentView = parent;
    }

    public String getName() {
        return Module.MODULE_NAME_NOTE;
    }

    public boolean loadModule() {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setDither(true);
        this.mAnnotHandler = new NoteAnnotHandler(this.mContext, this.mParentView, this.mPdfViewCtrl, this);
        this.mToolHandler = new NoteToolHandler(this.mContext, this.mPdfViewCtrl);
        this.mAnnotHandler.setToolHandler(this.mToolHandler);
        initVariable();
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        initViewBuilding();
        return true;
    }

    private void initVariable() {
        this.mCurrentColor = Color.argb(255, 255, 159, 64);
        this.mCurrentOpacity = 100;
        this.mCurrentIconType = "Comment";
        this.mToolHandler.setColor(this.mCurrentColor);
        this.mToolHandler.setOpacity(this.mCurrentOpacity);
        this.mToolHandler.setIconType(this.mCurrentIconType);
        Rect rect = new Rect(0, 0, dp2px(32), dp2px(32));
        this.mBitmapDrawables = new ArrayList();
        for (int i = 1; i < 8; i++) {
            Bitmap mBitmap = Bitmap.createBitmap(dp2px(32), dp2px(32), Config.ARGB_8888);
            Canvas canvas = new Canvas(mBitmap);
            BitmapDrawable bd = new BitmapDrawable(mBitmap);
            this.mPaint.setStyle(Style.FILL);
            this.mPaint.setColor(InputDeviceCompat.SOURCE_ANY);
            String iconName = NoteUtil.getIconNameByType(i);
            canvas.drawPath(NoteUtil.GetPathStringByType(iconName, AppDmUtil.rectToRectF(rect)), this.mPaint);
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setStrokeWidth((float) dp2px(1));
            this.mPaint.setARGB(255, 91, 91, 163);
            canvas.drawPath(NoteUtil.GetPathStringByType(iconName, AppDmUtil.rectToRectF(rect)), this.mPaint);
            canvas.save(31);
            canvas.restore();
            this.mBitmapDrawables.add(bd);
        }
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        return true;
    }

    private void initViewBuilding() {
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.mColorChangeListener = listener;
    }

    public void onValueChanged(long property, int value) {
        this.mUIExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(this.mUIExtensionsManager);
        if (property == 1 || property == 128) {
            if (this.mUIExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mCurrentColor = value;
                this.mToolHandler.setColor(value);
            }
            if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onColorValueChanged(value);
                this.mAnnotColor = value;
            }
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(this.mCurrentColor);
            }
        } else if (property == 2) {
            if (this.mUIExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mCurrentOpacity = value;
                this.mToolHandler.setOpacity(value);
            }
            if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onOpacityValueChanged(value);
                this.mAnnotOpacity = value;
            }
        } else if (property == 64) {
            String iconName = PropertyBar.ICONNAMES[value - 1];
            if (this.mUIExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mCurrentIconType = iconName;
                this.mToolHandler.setIconType(iconName);
            }
            if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onIconTypeChanged(iconName);
                this.mAnnotIconType = iconName;
            }
        }
    }

    public void onValueChanged(long property, float value) {
    }

    public void onValueChanged(long property, String iconName) {
    }

    private int dp2px(int dip) {
        return this.mDisplay.dp2px((float) dip);
    }
}
