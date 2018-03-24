package com.foxit.uiextensions.annots.link;

import android.content.Context;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;

public class LinkModule implements Module {
    private LinkAnnotHandler mAnnotHandler;
    private Context mContext;
    IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc pdfDoc, int i) {
            LinkModule.this.mAnnotHandler.isDocClosed = false;
        }

        public void onDocWillClose(PDFDoc pdfDoc) {
            LinkModule.this.mAnnotHandler.isDocClosed = true;
            LinkModule.this.mAnnotHandler.clear();
        }

        public void onDocClosed(PDFDoc pdfDoc, int i) {
        }

        public void onDocWillSave(PDFDoc pdfDoc) {
        }

        public void onDocSaved(PDFDoc pdfDoc, int i) {
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    IRecoveryEventListener mRecoveryListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            LinkModule.this.mAnnotHandler.isDocClosed = true;
            LinkModule.this.mAnnotHandler.clear();
        }

        public void onRecovered() {
        }
    };

    public LinkModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
    }

    public String getName() {
        return Module.MODULE_NAME_LINK;
    }

    public LinkAnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public boolean loadModule() {
        this.mAnnotHandler = new LinkAnnotHandler(this.mContext, this.mPdfViewCtrl);
        this.mPdfViewCtrl.registerDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.registerPageEventListener(this.mAnnotHandler.getPageEventListener());
        this.mPdfViewCtrl.registerRecoveryEventListener(this.mRecoveryListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.unregisterPageEventListener(this.mAnnotHandler.getPageEventListener());
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.mRecoveryListener);
        return true;
    }
}
