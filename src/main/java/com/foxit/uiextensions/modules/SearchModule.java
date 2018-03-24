package com.foxit.uiextensions.modules;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;

public class SearchModule implements Module {
    private Context mContext = null;
    IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc pdfDoc, int i) {
        }

        public void onDocWillClose(PDFDoc pdfDoc) {
        }

        public void onDocClosed(PDFDoc pdfDoc, int i) {
            SearchModule.this.mSearchView.onDocumentClosed();
        }

        public void onDocWillSave(PDFDoc pdfDoc) {
        }

        public void onDocSaved(PDFDoc pdfDoc, int i) {
        }
    };
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            if (!SearchModule.this.mSearchView.mIsCancel && SearchModule.this.mSearchView.mRect != null && SearchModule.this.mSearchView.mPageIndex != -1 && SearchModule.this.mSearchView.mPageIndex == pageIndex && SearchModule.this.mSearchView.mRect.size() > 0) {
                Paint paint = new Paint();
                paint.setARGB(150, 23, 156, 216);
                for (int i = 0; i < SearchModule.this.mSearchView.mRect.size(); i++) {
                    RectF rectF = new RectF((RectF) SearchModule.this.mSearchView.mRect.get(i));
                    RectF deviceRect = new RectF();
                    if (SearchModule.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, deviceRect, SearchModule.this.mSearchView.mPageIndex)) {
                        canvas.drawRect(deviceRect, paint);
                    }
                }
            }
        }
    };
    private ViewGroup mParent = null;
    private PDFViewCtrl mPdfViewCtrl = null;
    private SearchView mSearchView = null;

    public SearchModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        if (context == null || parent == null || pdfViewCtrl == null) {
            throw new NullPointerException();
        }
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean loadModule() {
        this.mSearchView = new SearchView(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public String getName() {
        return Module.MODULE_NAME_SEARCH;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public SearchView getSearchView() {
        return this.mSearchView;
    }

    public boolean onKeyBack() {
        if (this.mSearchView.getView().getVisibility() != 0) {
            return false;
        }
        this.mSearchView.cancel();
        return true;
    }
}
