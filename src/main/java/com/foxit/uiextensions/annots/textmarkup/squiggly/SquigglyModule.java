package com.foxit.uiextensions.annots.textmarkup.squiggly;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.UIExtensionsManager.ToolHandlerChangedListener;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.ToolUtil;

public class SquigglyModule implements Module, PropertyChangeListener {
    private SquigglyAnnotHandler mAnnotHandler;
    private ColorChangeListener mColorChangeListener = null;
    private Context mContext;
    private int mCurrentColor;
    private int mCurrentOpacity;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            SquigglyModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private ToolHandlerChangedListener mHandlerChangedListener = new ToolHandlerChangedListener() {
        public void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
            SquigglyModule.this.mToolHandler.onToolHandlerChanged(lastTool, currentTool);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private SquigglyToolHandler mToolHandler;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (SquigglyModule.this.mAnnotHandler.getAnnotMenu() != null && SquigglyModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                SquigglyModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (SquigglyModule.this.mAnnotHandler.getPropertyBar() != null && SquigglyModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                SquigglyModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public SquigglyModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_SQUIGGLY;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public boolean loadModule() {
        this.mAnnotHandler = new SquigglyAnnotHandler(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mToolHandler = new SquigglyToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mAnnotHandler.setToolHandler(this.mToolHandler);
        this.mAnnotHandler.setAnnotMenu(new AnnotMenuImpl(this.mContext, this.mParent));
        this.mAnnotHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mAnnotHandler.setPropertyChangeListener(this);
        this.mToolHandler.setPropertyChangeListener(this);
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        this.mCurrentColor = PropertyBar.PB_COLORS_STRIKEOUT[0];
        this.mCurrentOpacity = 255;
        this.mToolHandler.setPaint(this.mCurrentColor, this.mCurrentOpacity);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        this.mToolHandler.unInit();
        this.mAnnotHandler.removeProbarListener();
        this.mToolHandler.removeProbarListener();
        return true;
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.mColorChangeListener = listener;
    }

    public void onValueChanged(long property, int value) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
        if (property == 1 || property == 128) {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mCurrentColor = value;
                this.mToolHandler.setPaint(this.mCurrentColor, this.mCurrentOpacity);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.modifyAnnotColor(value);
            }
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(this.mCurrentColor);
            }
        } else if (property != 2) {
        } else {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mCurrentOpacity = AppDmUtil.opacity100To255(value);
                this.mToolHandler.setPaint(this.mCurrentColor, this.mCurrentOpacity);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.modifyAnnotOpacity(AppDmUtil.opacity100To255(value));
            }
        }
    }

    public void onValueChanged(long property, float value) {
    }

    public void onValueChanged(long property, String value) {
    }
}
