package com.foxit.uiextensions.annots.caret;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.ToolUtil;

public class CaretModule implements Module, PropertyChangeListener {
    private CaretAnnotHandler mAnnotHandler;
    private ColorChangeListener mColorChangeListener = null;
    private final Context mContext;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            CaretModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private CaretToolHandler mIS_ToolHandler;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private CaretToolHandler mRP_ToolHandler;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (CaretModule.this.mAnnotHandler.getAnnotMenu() != null && CaretModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                CaretModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (CaretModule.this.mAnnotHandler.getPropertyBar() != null && CaretModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                CaretModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public CaretModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.mColorChangeListener = listener;
    }

    public boolean loadModule() {
        this.mAnnotHandler = new CaretAnnotHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mIS_ToolHandler = new CaretToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mRP_ToolHandler = new CaretToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mAnnotHandler.setPropertyChangeListener(this);
        this.mIS_ToolHandler.setPropertyChangeListener(this);
        this.mRP_ToolHandler.setPropertyChangeListener(this);
        this.mAnnotHandler.setAnnotMenu(new AnnotMenuImpl(this.mContext, this.mParent));
        this.mAnnotHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mIS_ToolHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mRP_ToolHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mIS_ToolHandler.init(true);
        this.mRP_ToolHandler.init(false);
        this.mAnnotHandler.setToolHandler("Replace", this.mRP_ToolHandler);
        this.mAnnotHandler.setToolHandler("Insert Text", this.mIS_ToolHandler);
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public boolean unloadModule() {
        this.mRP_ToolHandler.removePropertyBarListener();
        this.mIS_ToolHandler.removePropertyBarListener();
        this.mAnnotHandler.removePropertyBarListener();
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public String getName() {
        return Module.MODULE_NAME_CARET;
    }

    public void onValueChanged(long property, int value) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        AnnotHandler currentAnnotHandler = ToolUtil.getCurrentAnnotHandler(uiExtensionsManager);
        if (property == 1 || property == 128) {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mIS_ToolHandler) {
                this.mIS_ToolHandler.changeCurrentColor(value);
            } else if (uiExtensionsManager.getCurrentToolHandler() == this.mRP_ToolHandler) {
                this.mRP_ToolHandler.changeCurrentColor(value);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onColorValueChanged(value);
            }
            if (this.mColorChangeListener != null) {
                this.mColorChangeListener.onColorChange(value);
            }
        } else if (property != 2) {
        } else {
            if (uiExtensionsManager.getCurrentToolHandler() == this.mIS_ToolHandler) {
                this.mIS_ToolHandler.changeCurrentOpacity(value);
            } else if (uiExtensionsManager.getCurrentToolHandler() == this.mRP_ToolHandler) {
                this.mRP_ToolHandler.changeCurrentOpacity(value);
            } else if (currentAnnotHandler == this.mAnnotHandler) {
                this.mAnnotHandler.onOpacityValueChanged(value);
            }
        }
    }

    public ToolHandler getISToolHandler() {
        return this.mIS_ToolHandler;
    }

    public ToolHandler getRPToolHandler() {
        return this.mRP_ToolHandler;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public void onValueChanged(long property, float value) {
    }

    public void onValueChanged(long property, String value) {
    }
}
