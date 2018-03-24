package com.foxit.uiextensions.textselect;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;

public class TextSelectModule implements Module {
    private Context mContext;
    private IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc pdfDoc, int err) {
            if (err == 0) {
                TextSelectModule.this.mToolHandler.mIsEdit = false;
                TextSelectModule.this.mToolHandler.mIsMenuShow = false;
            }
        }

        public void onDocWillClose(PDFDoc pdfDoc) {
        }

        public void onDocClosed(PDFDoc pdfDoc, int err) {
            if (err == 0) {
                TextSelectModule.this.mToolHandler.mSelectInfo.clear();
                TextSelectModule.this.mToolHandler.mAnnotationMenu.dismiss();
            }
        }

        public void onDocWillSave(PDFDoc pdfDoc) {
        }

        public void onDocSaved(PDFDoc pdfDoc, int i) {
        }
    };
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            TextSelectModule.this.mToolHandler.onDrawForAnnotMenu(canvas);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    IRecoveryEventListener mRecoveryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            if (TextSelectModule.this.mToolHandler.getAnnotationMenu() != null && TextSelectModule.this.mToolHandler.getAnnotationMenu().isShowing()) {
                TextSelectModule.this.mToolHandler.getAnnotationMenu().dismiss();
            }
            if (TextSelectModule.this.mToolHandler.getSelectInfo() != null) {
                TextSelectModule.this.mToolHandler.getSelectInfo().clear();
            }
        }

        public void onRecovered() {
        }
    };
    private TextSelectToolHandler mToolHandler;

    public TextSelectModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
    }

    public String getName() {
        return Module.MODULE_NAME_SELECTION;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public boolean loadModule() {
        this.mToolHandler = new TextSelectToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mPdfViewCtrl.registerDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        this.mPdfViewCtrl.registerRecoveryEventListener(this.mRecoveryEventListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.mRecoveryEventListener);
        this.mToolHandler.uninit();
        return true;
    }

    public void triggerDismissMenu() {
        if (this.mToolHandler != null) {
            this.mToolHandler.dismissMenu();
        }
    }
}
