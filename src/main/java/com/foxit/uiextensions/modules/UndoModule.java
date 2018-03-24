package com.foxit.uiextensions.modules;

import android.content.Context;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IPageEventListener;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.DefaultAnnotHandler;
import com.foxit.uiextensions.utils.OnPageEventListener;

public class UndoModule implements Module {
    private Context mContext;
    private DefaultAnnotHandler mDefAnnotHandler;
    private IPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPageMoved(boolean success, int index, int dstIndex) {
            DocumentManager.getInstance(UndoModule.this.mPdfViewCtrl).onPageMoved(success, index, dstIndex);
        }

        public void onPagesRemoved(boolean success, int[] pageIndexes) {
            for (int i = 0; i < pageIndexes.length; i++) {
                DocumentManager.getInstance(UndoModule.this.mPdfViewCtrl).onPageRemoved(success, pageIndexes[i] - i);
            }
        }

        public void onPagesInserted(boolean success, int dstIndex, int[] range) {
            DocumentManager.getInstance(UndoModule.this.mPdfViewCtrl).onPagesInsert(success, dstIndex, range);
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;

    public UndoModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_UNDO;
    }

    public boolean loadModule() {
        this.mDefAnnotHandler = new DefaultAnnotHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mPdfViewCtrl.registerPageEventListener(this.mPageEventListener);
        return true;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mDefAnnotHandler;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterPageEventListener(this.mPageEventListener);
        return true;
    }

    public void undo() {
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canUndo()) {
            DocumentManager.getInstance(this.mPdfViewCtrl).undo();
        }
    }

    public void redo() {
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canRedo()) {
            DocumentManager.getInstance(this.mPdfViewCtrl).redo();
        }
    }
}
