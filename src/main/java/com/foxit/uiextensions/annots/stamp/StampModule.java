package com.foxit.uiextensions.annots.stamp;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;

public class StampModule implements Module, PropertyChangeListener {
    private StampAnnotHandler mAnnotHandlerSTP;
    private Context mContext;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            StampModule.this.mAnnotHandlerSTP.onDrawForControls(canvas);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyBar mPropertyBar;
    private StampToolHandler mToolHandlerSTP;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (StampModule.this.mAnnotHandlerSTP.getAnnotMenu() != null && StampModule.this.mAnnotHandlerSTP.getAnnotMenu().isShowing()) {
                StampModule.this.mAnnotHandlerSTP.getAnnotMenu().dismiss();
            }
            if (StampModule.this.mToolHandlerSTP.getPropertyBar() != null && StampModule.this.mToolHandlerSTP.getPropertyBar().isShowing()) {
                StampModule.this.mToolHandlerSTP.getPropertyBar().dismiss();
            }
            DocumentManager.getInstance(StampModule.this.mPdfViewCtrl).reInit();
        }

        public void onRecovered() {
            StampModule.this.mToolHandlerSTP.initAnnotIconProvider();
        }
    };

    public StampModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_STAMP;
    }

    public boolean loadModule() {
        this.mPropertyBar = new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mPropertyBar.setPropertyChangeListener(this);
        this.mToolHandlerSTP = new StampToolHandler(this.mContext, this.mPdfViewCtrl);
        this.mToolHandlerSTP.setPropertyBar(this.mPropertyBar);
        this.mAnnotHandlerSTP = new StampAnnotHandler(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mAnnotHandlerSTP.setAnnotMenu(new AnnotMenuImpl(this.mContext, this.mParent));
        this.mAnnotHandlerSTP.setToolHandler(this.mToolHandlerSTP);
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        return false;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandlerSTP;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandlerSTP;
    }

    public void onValueChanged(long property, int value) {
    }

    public void onValueChanged(long property, float value) {
    }

    public void onValueChanged(long property, String value) {
    }
}
