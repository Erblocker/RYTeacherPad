package com.foxit.uiextensions.modules;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;

/* compiled from: ThumbnailSupport */
class ThumbnailItem implements Comparable<ThumbnailItem> {
    public static final int EDIT_LEFT_VIEW = 1;
    public static final int EDIT_NO_VIEW = 0;
    public static final int EDIT_RIGHT_VIEW = 2;
    public int editViewFlag = 0;
    private boolean isRendering = false;
    private boolean isSelected;
    private final Point mBackgroundSize;
    private Bitmap mBitmap;
    private PDFPage mPDFPage;
    private final PDFViewCtrl mPDFView;
    private Rect mThumbnailRect;
    private Point mThumbnailSize;
    private boolean mbNeedCompute;

    public ThumbnailItem(int pageIndex, Point backgroundSize, PDFViewCtrl pdfViewCtrl) {
        try {
            this.mPDFPage = pdfViewCtrl.getDoc().getPage(pageIndex);
        } catch (PDFException e) {
            e.printStackTrace();
        }
        this.mPDFView = pdfViewCtrl;
        this.mBackgroundSize = backgroundSize;
        this.isSelected = false;
        this.mbNeedCompute = true;
    }

    public int getIndex() {
        try {
            return this.mPDFPage.getIndex();
        } catch (PDFException e) {
            return -1;
        }
    }

    public boolean isRendering() {
        return this.isRendering;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public void resetRending(boolean rendering) {
        this.isRendering = rendering;
    }

    public boolean needRecompute() {
        return this.mbNeedCompute;
    }

    public PDFPage getPage() {
        return this.mPDFPage;
    }

    public void closePage() {
        try {
            if (this.mPDFPage != null) {
                this.mPDFPage.getDocument().closePage(getIndex());
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public boolean setRotation(int rotation) {
        this.mPDFView.rotatePages(new int[]{getIndex()}, rotation);
        this.mbNeedCompute = true;
        return true;
    }

    public int getRotation() {
        try {
            return getPage() != null ? getPage().getRotation() : 0;
        } catch (PDFException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void compute() {
        if (this.mThumbnailRect == null) {
            this.mThumbnailRect = new Rect();
        }
        if (this.mThumbnailSize == null) {
            this.mThumbnailSize = new Point();
        }
        try {
            PDFPage page = getPage();
            if (page != null) {
                float psWidth = page.getWidth();
                float psHeight = page.getHeight();
                float scale = Math.min(((float) this.mBackgroundSize.x) / psWidth, ((float) this.mBackgroundSize.y) / psHeight);
                psWidth *= scale;
                psHeight *= scale;
                int left = (int) ((((float) this.mBackgroundSize.x) / 2.0f) - (psWidth / 2.0f));
                int top = (int) ((((float) this.mBackgroundSize.y) / 2.0f) - (psHeight / 2.0f));
                this.mThumbnailRect.set(left, top, this.mBackgroundSize.x - left, this.mBackgroundSize.y - top);
                this.mThumbnailSize.set((int) psWidth, (int) psHeight);
                this.mbNeedCompute = false;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public Point getSize() {
        if (this.mbNeedCompute) {
            compute();
        }
        return new Point(this.mThumbnailSize);
    }

    public Rect getRect() {
        if (this.mbNeedCompute) {
            compute();
        }
        return new Rect(this.mThumbnailRect);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof ThumbnailItem)) {
            return false;
        }
        if (this == o || getIndex() == ((ThumbnailItem) o).getIndex()) {
            return true;
        }
        return false;
    }

    public int compareTo(@NonNull ThumbnailItem another) {
        return getIndex() - another.getIndex();
    }
}
