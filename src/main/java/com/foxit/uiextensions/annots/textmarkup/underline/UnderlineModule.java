package com.foxit.uiextensions.annots.textmarkup.underline;

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
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.ToolUtil;

public class UnderlineModule implements Module, PropertyChangeListener {
    private UnderlineAnnotHandler mAnnotHandler;
    private ColorChangeListener mColorChangeListener = null;
    private Context mContext;
    private int mCurrentColor;
    private int mCurrentOpacity;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            UnderlineModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewer;
    private UnderlineToolHandler mToolHandler;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (UnderlineModule.this.mAnnotHandler.getAnnotMenu() != null && UnderlineModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                UnderlineModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (UnderlineModule.this.mAnnotHandler.getPropertyBar() != null && UnderlineModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                UnderlineModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public interface ColorChangeListener {
        void onColorChange(int i);
    }

    public UnderlineModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewer) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewer = pdfViewer;
    }

    public String getName() {
        return Module.MODULE_NAME_UNDERLINE;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public boolean loadModule() {
        this.mAnnotHandler = new UnderlineAnnotHandler(this.mContext, this.mPdfViewer, this.mParent);
        this.mToolHandler = new UnderlineToolHandler(this.mContext, this.mParent, this.mPdfViewer);
        this.mAnnotHandler.setToolHandler(this.mToolHandler);
        this.mAnnotHandler.setAnnotMenu(new AnnotMenuImpl(this.mContext, this.mParent));
        this.mAnnotHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewer, this.mParent));
        this.mAnnotHandler.setPropertyChangeListener(this);
        this.mToolHandler.setPropertyChangeListener(this);
        this.mPdfViewer.registerRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewer.registerDrawEventListener(this.mDrawEventListener);
        this.mCurrentColor = PropertyBar.PB_COLORS_UNDERLINE[0];
        this.mCurrentOpacity = 255;
        this.mToolHandler.setPaint(this.mCurrentColor, this.mCurrentOpacity);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewer.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewer.unregisterDrawEventListener(this.mDrawEventListener);
        this.mToolHandler.unInit();
        this.mAnnotHandler.removeProbarListener();
        this.mToolHandler.removeProbarListener();
        return true;
    }

    public void setColorChangeListener(ColorChangeListener listener) {
        this.mColorChangeListener = listener;
    }

    public void onValueChanged(long property, int value) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewer.getUIExtensionsManager();
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
