package com.foxit.uiextensions.annots.ink;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;

public class InkModule implements Module {
    protected InkAnnotHandler mAnnotHandler;
    private Context mContext;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            InkModule.this.mAnnotHandler.onDrawForControls(canvas);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    protected InkToolHandler mToolHandler;
    protected InkAnnotUtil mUtil;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (InkModule.this.mAnnotHandler.getAnnotMenu() != null && InkModule.this.mAnnotHandler.getAnnotMenu().isShowing()) {
                InkModule.this.mAnnotHandler.getAnnotMenu().dismiss();
            }
            if (InkModule.this.mAnnotHandler.getPropertyBar() != null && InkModule.this.mAnnotHandler.getPropertyBar().isShowing()) {
                InkModule.this.mAnnotHandler.getPropertyBar().dismiss();
            }
        }

        public void onRecovered() {
        }
    };

    public InkModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_INK;
    }

    public boolean loadModule() {
        this.mUtil = new InkAnnotUtil();
        this.mToolHandler = new InkToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl, this.mUtil);
        this.mAnnotHandler = new InkAnnotHandler(this.mContext, this.mParent, this.mPdfViewCtrl, this.mToolHandler, this.mUtil);
        this.mToolHandler.mAnnotHandler = this.mAnnotHandler;
        this.mAnnotHandler.setAnnotMenu(new AnnotMenuImpl(this.mContext, this.mParent));
        this.mAnnotHandler.setPropertyBar(new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        this.mToolHandler.uninitUiElements();
        return true;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }
}
