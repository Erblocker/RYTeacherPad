package com.foxit.uiextensions.annots.textmarkup;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.PDFTextSelect;
import java.util.ArrayList;

public class TextSelector {
    private RectF mBBox = new RectF();
    private String mContents;
    private int mEndChar = -1;
    private PDFViewCtrl mPdfViewCtrl;
    private ArrayList<RectF> mRectFList = new ArrayList();
    private int mStartChar = -1;

    public TextSelector(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public void clear() {
        this.mEndChar = -1;
        this.mStartChar = -1;
        this.mBBox.setEmpty();
        this.mRectFList.clear();
    }

    public void setStart(int start) {
        this.mStartChar = start;
    }

    public void setEnd(int end) {
        this.mEndChar = end;
    }

    public int getStart() {
        return this.mStartChar;
    }

    public int getEnd() {
        return this.mEndChar;
    }

    public void start(PDFPage page, int start) {
        computeSelected(page, start, start);
    }

    public void update(PDFPage page, int update) {
        if (this.mStartChar < 0) {
            this.mStartChar = update;
        }
        computeSelected(page, this.mStartChar, update);
    }

    public String getText(PDFPage page) {
        int start = Math.min(this.mStartChar, this.mEndChar);
        int end = Math.max(this.mStartChar, this.mEndChar);
        try {
            if (!page.isParsed()) {
                int ret = page.startParse(0, null, false);
                while (ret == 1) {
                    ret = page.continueParse();
                }
            }
            PDFTextSelect textPage = PDFTextSelect.create(page);
            this.mContents = textPage.getChars(start, (end - start) + 1);
            textPage.release();
            return this.mContents;
        } catch (PDFException e) {
            if (e.getLastError() != 10) {
                return null;
            }
            this.mPdfViewCtrl.recoverForOOM();
            return null;
        }
    }

    public RectF getBbox() {
        return this.mBBox;
    }

    public ArrayList<RectF> getRectFList() {
        return this.mRectFList;
    }

    public void computeSelected(PDFPage page, int start, int end) {
        if (page != null) {
            if ((start != end || start != -1) && start != -1) {
                this.mStartChar = start;
                this.mEndChar = end;
                if (end < start) {
                    int tmp = end;
                    end = start;
                    start = tmp;
                }
                this.mRectFList.clear();
                try {
                    if (!page.isParsed()) {
                        int ret = page.startParse(0, null, false);
                        while (ret == 1) {
                            ret = page.continueParse();
                        }
                    }
                    PDFTextSelect textPage = PDFTextSelect.create(page);
                    int count = textPage.getTextRectCount(start, (end - start) + 1);
                    for (int i = 0; i < count; i++) {
                        RectF rectF = textPage.getTextRect(i);
                        this.mRectFList.add(rectF);
                        if (i == 0) {
                            this.mBBox = new RectF(rectF);
                        } else {
                            adjustBbox(this.mBBox, rectF);
                        }
                    }
                    textPage.release();
                } catch (PDFException e) {
                    if (e.getLastError() == 10) {
                        this.mPdfViewCtrl.recoverForOOM();
                    }
                }
            }
        }
    }

    private void adjustBbox(RectF dst, RectF rect) {
        if (rect.left < dst.left) {
            dst.left = rect.left;
        }
        if (rect.right > dst.right) {
            dst.right = rect.right;
        }
        if (rect.bottom < dst.bottom) {
            dst.bottom = rect.bottom;
        }
        if (rect.top > dst.top) {
            dst.top = rect.top;
        }
    }

    public String getContents() {
        return this.mContents;
    }

    public void setContents(String mContents) {
        this.mContents = mContents;
    }
}
