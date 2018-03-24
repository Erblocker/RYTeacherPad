package com.foxit.uiextensions.annots.textmarkup.highlight;

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

public class HighlightModule implements Module, PropertyChangeListener {
    private HighlightAnnotHandler mAnnotHandler;
    private ColorChangeListener mColorChangeListener = null;
    private Context mContext;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            HighlightModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private ToolHandlerChangedListener mHandlerChangedListener = new ToolHandlerChangedListener() {
        public void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
            HighlightModule.this.mToolHandler.onToolHandlerChanged(lastTool, currentTool);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private int mToolColor;
    private HighlightToolHandler mToolHandler;
    private int mToolOpacity;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (HighlightModule.this.mAnnotHandler.getAnnotMenu() != null && HighlightModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                HighlightModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (HighlightModule.this.mAnnotHandler.getPropertyBar() != null && HighlightModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                HighlightModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public HighlightModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_HIGHLIGHT;
    }

    public boolean loadModule() {
        this.mToolHandler = new HighlightToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mAnnotHandler = new HighlightAnnotHandler(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mAnnotHandler.setToolHandler(this.mToolHandler);
        this.mAnnotHandler.setAnnotMenu(new AnnotMenuImpl(this.mContext, this.mParent));
        this.mAnnotHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mAnnotHandler.setPropertyChangeListener(this);
        this.mToolHandler.setPropertyChangeListener(this);
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        this.mToolColor = PropertyBar.PB_COLORS_HIGHLIGHT[0];
        this.mToolOpacity = 255;
        this.mToolHandler.setPaint(this.mToolColor, this.mToolOpacity);
        return true;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
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
                this.mToolColor = value;
                this.mToolHandler.setPaint(this.mToolColor, this.mToolOpacity);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.modifyAnnotColor(value);
            }
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(this.mToolColor);
            }
        } else if (property != 2) {
        } else {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mToolOpacity = AppDmUtil.opacity100To255(value);
                this.mToolHandler.setPaint(this.mToolColor, this.mToolOpacity);
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
