package com.foxit.uiextensions.annots.line;

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

public class LineModule implements Module {
    protected LineToolHandler mArrowToolHandler;
    Context mContext;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            LineModule.this.mLineAnnotHandler.onDrawForControls(canvas);
        }
    };
    protected LineAnnotHandler mLineAnnotHandler;
    protected LineToolHandler mLineToolHandler;
    ViewGroup mParent;
    PDFViewCtrl mPdfViewCtrl;
    protected LineUtil mUtil;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
        }

        public void onRecovered() {
        }
    };

    public LineModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_LINE;
    }

    public boolean loadModule() {
        this.mUtil = new LineUtil(this.mContext, this);
        this.mLineToolHandler = new LineToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl, this.mUtil, "");
        this.mArrowToolHandler = new LineToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl, this.mUtil, LineConstants.INTENT_LINE_ARROW);
        this.mLineAnnotHandler = new LineAnnotHandler(this.mContext, this.mParent, this.mPdfViewCtrl, this.mUtil);
        this.mLineToolHandler.mAnnotHandler = this.mLineAnnotHandler.mRealAnnotHandler;
        this.mArrowToolHandler.mAnnotHandler = this.mLineAnnotHandler.mRealAnnotHandler;
        this.mLineAnnotHandler.setAnnotMenu(LineConstants.INTENT_LINE_DIMENSION, new AnnotMenuImpl(this.mContext, this.mParent));
        this.mLineAnnotHandler.setPropertyBar(LineConstants.INTENT_LINE_DIMENSION, new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mLineAnnotHandler.setAnnotMenu(LineConstants.INTENT_LINE_ARROW, new AnnotMenuImpl(this.mContext, this.mParent));
        this.mLineAnnotHandler.setPropertyBar(LineConstants.INTENT_LINE_ARROW, new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent));
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        initUiElements();
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        uninitUiElements();
        return true;
    }

    void initUiElements() {
        this.mArrowToolHandler.initUiElements();
        this.mLineToolHandler.initUiElements();
    }

    void uninitUiElements() {
        this.mArrowToolHandler.uninitUiElements();
        this.mLineToolHandler.uninitUiElements();
    }

    public AnnotHandler getAnnotHandler() {
        return this.mLineAnnotHandler;
    }

    public ToolHandler getLineToolHandler() {
        return this.mLineToolHandler;
    }

    public ToolHandler getArrowToolHandler() {
        return this.mArrowToolHandler;
    }
}
