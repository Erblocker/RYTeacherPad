package com.foxit.uiextensions.annots;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.BorderInfo;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.IUndoItem;

public abstract class AnnotUndoItem implements IUndoItem {
    public String mAuthor;
    public RectF mBBox;
    public int mBorderStyle;
    public long mColor;
    public String mContents;
    public DateTime mCreationDate;
    public float[] mDashes;
    public long mFlags;
    public String mIntent;
    public float mLineWidth;
    public DateTime mModifiedDate;
    public String mNM;
    public String mOldAuthor;
    public RectF mOldBBox;
    public int mOldBorderStyle;
    public long mOldColor;
    public String mOldContents;
    public DateTime mOldCreationDate;
    public float[] mOldDashes;
    public long mOldFlags;
    public String mOldIntent;
    public float mOldLineWidth;
    public DateTime mOldModifiedDate;
    public float mOldOpacity;
    public String mOldSubject;
    public float mOpacity;
    public int mPageIndex;
    protected PDFViewCtrl mPdfViewCtrl;
    public String mSubject;
    public int mType;

    public void setCurrentValue(Annot annot) {
        if (annot != null) {
            try {
                if (annot.getPage() != null) {
                    this.mPageIndex = annot.getPage().getIndex();
                }
                this.mType = annot.getType();
                this.mNM = annot.getUniqueID();
                this.mBBox = new RectF(annot.getRect());
                this.mColor = annot.getBorderColor();
                this.mFlags = annot.getFlags();
                this.mModifiedDate = annot.getModifiedDateTime();
                this.mContents = annot.getContent();
                if (annot.isMarkup()) {
                    this.mOpacity = ((Markup) annot).getOpacity();
                    this.mSubject = ((Markup) annot).getSubject();
                    this.mAuthor = ((Markup) annot).getTitle();
                    this.mCreationDate = ((Markup) annot).getCreationDateTime();
                    this.mIntent = ((Markup) annot).getIntent();
                }
                BorderInfo borderInfo = annot.getBorderInfo();
                if (borderInfo != null) {
                    this.mLineWidth = borderInfo.getWidth();
                    this.mBorderStyle = borderInfo.getStyle();
                    float[] dashes = borderInfo.getDashes();
                    if (dashes != null) {
                        this.mDashes = new float[dashes.length];
                        System.arraycopy(borderInfo.getDashes(), 0, this.mDashes, 0, dashes.length);
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOldValue(Annot annot) {
        if (annot != null) {
            try {
                if (annot.getPage() != null) {
                    this.mPageIndex = annot.getPage().getIndex();
                }
                this.mType = annot.getType();
                this.mNM = annot.getUniqueID();
                this.mOldBBox = new RectF(annot.getRect());
                this.mOldColor = annot.getBorderColor();
                this.mOldFlags = annot.getFlags();
                this.mOldModifiedDate = annot.getModifiedDateTime();
                this.mOldContents = annot.getContent();
                if (annot.isMarkup()) {
                    this.mOldOpacity = ((Markup) annot).getOpacity();
                    this.mOldSubject = ((Markup) annot).getSubject();
                    this.mOldAuthor = ((Markup) annot).getTitle();
                    this.mOldCreationDate = ((Markup) annot).getCreationDateTime();
                    this.mOldIntent = ((Markup) annot).getIntent();
                }
                BorderInfo borderInfo = annot.getBorderInfo();
                if (borderInfo != null) {
                    this.mOldLineWidth = borderInfo.getWidth();
                    this.mOldBorderStyle = borderInfo.getStyle();
                    float[] dashes = borderInfo.getDashes();
                    if (dashes != null) {
                        this.mOldDashes = new float[dashes.length];
                        System.arraycopy(borderInfo.getDashes(), 0, this.mOldDashes, 0, dashes.length);
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCurrentValue(AnnotContent content) {
        this.mPageIndex = content.getPageIndex();
        this.mType = content.getType();
        this.mNM = content.getNM();
        if (content.getBBox() != null) {
            this.mBBox = new RectF(content.getBBox());
        }
        this.mColor = (long) content.getColor();
        this.mOpacity = ((float) content.getOpacity()) / 255.0f;
        if (content.getLineWidth() != 0.0f) {
            this.mLineWidth = content.getLineWidth();
        }
        if (content.getSubject() != null) {
            this.mSubject = content.getSubject();
        }
        if (content.getModifiedDate() != null) {
            this.mModifiedDate = content.getModifiedDate();
        }
        if (content.getContents() != null) {
            this.mContents = content.getContents();
        }
        if (content.getIntent() != null) {
            this.mIntent = content.getIntent();
        }
    }

    public void setOldValue(AnnotContent content) {
        this.mPageIndex = content.getPageIndex();
        this.mType = content.getType();
        this.mNM = content.getNM();
        if (content.getBBox() != null) {
            this.mOldBBox = new RectF(content.getBBox());
        }
        this.mOldColor = (long) content.getColor();
        this.mOldOpacity = ((float) content.getOpacity()) / 255.0f;
        if (content.getLineWidth() != 0.0f) {
            this.mOldLineWidth = content.getLineWidth();
        }
        if (content.getSubject() != null) {
            this.mOldSubject = content.getSubject();
        }
        if (content.getModifiedDate() != null) {
            this.mOldModifiedDate = content.getModifiedDate();
        }
        if (content.getContents() != null) {
            this.mOldContents = content.getContents();
        }
        if (content.getIntent() != null) {
            this.mOldIntent = content.getIntent();
        }
    }
}
