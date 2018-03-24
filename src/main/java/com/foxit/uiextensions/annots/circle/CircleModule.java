package com.foxit.uiextensions.annots.circle;

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
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.ToolUtil;

public class CircleModule implements Module, PropertyChangeListener {
    private CircleAnnotHandler mAnnotHandler;
    private ColorChangeListener mColorChangeListener = null;
    private Context mContext;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            CircleModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private ToolHandlerChangedListener mHandlerChangedListener = new ToolHandlerChangedListener() {
        public void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
            CircleModule.this.mToolHandler.onToolHandlerChanged(lastTool, currentTool);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private CircleToolHandler mToolHandler;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (CircleModule.this.mAnnotHandler.getAnnotMenu() != null && CircleModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                CircleModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (CircleModule.this.mAnnotHandler.getPropertyBar() != null && CircleModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                CircleModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public CircleModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.mColorChangeListener = listener;
    }

    public void onValueChanged(long property, int value) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
        if (property == 1 || property == 128) {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mToolHandler.changeCurrentColor(value);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onColorValueChanged(value);
            }
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(value);
            }
        } else if (property != 2) {
        } else {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
                this.mToolHandler.changeCurrentOpacity(value);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onOpacityValueChanged(value);
            }
        }
    }

    public void onValueChanged(long property, float value) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
        if (property != 4) {
            return;
        }
        if (uiExtensionsManager.getCurrentToolHandler() == this.mToolHandler) {
            this.mToolHandler.changeCurrentThickness(value);
        } else if (currentAnnotHandler == this.mAnnotHandler) {
            this.mAnnotHandler.onLineWidthValueChanged(value);
        }
    }

    public void onValueChanged(long property, String value) {
    }

    public String getName() {
        return Module.MODULE_NAME_CIRCLE;
    }

    public boolean loadModule() {
        this.mAnnotHandler = new CircleAnnotHandler(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mToolHandler = new CircleToolHandler(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mAnnotHandler.setPropertyChangeListener(this);
        this.mToolHandler.setPropertyChangeListener(this);
        this.mAnnotHandler.setAnnotMenu(new AnnotMenuImpl(this.mContext, this.mParent));
        this.mAnnotHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        this.mToolHandler.init();
        return true;
    }

    public boolean unloadModule() {
        this.mToolHandler.removePropertyBarListener();
        this.mAnnotHandler.removePropertyBarListener();
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }
}
