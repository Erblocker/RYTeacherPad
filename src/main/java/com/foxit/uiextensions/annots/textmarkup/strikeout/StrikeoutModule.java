package com.foxit.uiextensions.annots.textmarkup.strikeout;

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

public class StrikeoutModule implements Module, PropertyChangeListener {
    private StrikeoutAnnotHandler mAnnotHandler;
    private ColorChangeListener mColorChangeListener = null;
    private Context mContext;
    private int mCurrentColor;
    private int mCurrentOpacity;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            StrikeoutModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private ToolHandlerChangedListener mHandlerChangedListener = new ToolHandlerChangedListener() {
        public void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
            StrikeoutModule.this.mToolHandler.onToolHandlerChanged(lastTool, currentTool);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private StrikeoutToolHandler mToolHandler;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (StrikeoutModule.this.mAnnotHandler.getAnnotMenu() != null && StrikeoutModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                StrikeoutModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (StrikeoutModule.this.mAnnotHandler.getPropertyBar() != null && StrikeoutModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                StrikeoutModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public StrikeoutModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewer) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewer;
    }

    public String getName() {
        return Module.MODULE_NAME_STRIKEOUT;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public boolean loadModule() {
        this.mAnnotHandler = new StrikeoutAnnotHandler(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mToolHandler = new StrikeoutToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
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
                this.mColorChangeListener.onColorChange(value);
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
