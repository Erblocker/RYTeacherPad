package com.foxit.uiextensions.annots.stamp;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.Stamp;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StampEvent extends EditAnnotEvent {
    public StampEvent(int eventType, StampUndoItem undoItem, Stamp stamp, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = stamp;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        PDFException e;
        IOException e2;
        Throwable th;
        if (this.mAnnot == null || !(this.mAnnot instanceof Stamp)) {
            return false;
        }
        Stamp annot = this.mAnnot;
        StampAddUndoItem undoItem = this.mUndoItem;
        ByteArrayOutputStream baos = null;
        try {
            annot.setUniqueID(this.mUndoItem.mNM);
            annot.setFlags(4);
            annot.setFlags(this.mUndoItem.mFlags);
            if (this.mUndoItem.mCreationDate != null) {
                annot.setCreationDateTime(this.mUndoItem.mCreationDate);
            }
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mAuthor != null) {
                annot.setTitle(this.mUndoItem.mAuthor);
            }
            if (this.mUndoItem.mSubject != null) {
                annot.setSubject(this.mUndoItem.mSubject);
            }
            if (undoItem.mIconName != null) {
                annot.setIconName(undoItem.mIconName);
            }
            if (this.mUndoItem.mContents == null) {
                this.mUndoItem.mContents = "";
            }
            annot.setContent(this.mUndoItem.mContents);
            if (undoItem.mStampType < 17 || undoItem.mStampType > 21) {
                annot.setBitmap(undoItem.mBitmap);
            } else {
                InputStream is = this.mPdfViewCtrl.getContext().getAssets().open("DynamicStamps/" + undoItem.mSubject.substring(4, undoItem.mSubject.length()) + ".pdf");
                if (is != null) {
                    byte[] buffer = new byte[8192];
                    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                    while (true) {
                        try {
                            int n = is.read(buffer);
                            if (-1 == n) {
                                break;
                            }
                            baos2.write(buffer, 0, n);
                        } catch (PDFException e3) {
                            e = e3;
                            baos = baos2;
                        } catch (IOException e4) {
                            e2 = e4;
                            baos = baos2;
                        } catch (Throwable th2) {
                            th = th2;
                            baos = baos2;
                        }
                    }
                    PDFDoc pdfDoc = PDFDoc.createFromMemory(baos2.toByteArray());
                    pdfDoc.load(null);
                    undoItem.mDsip.addDocMap(undoItem.mSubject + 13, pdfDoc);
                    is.close();
                    baos = baos2;
                } else if (baos == null) {
                    return false;
                } else {
                    try {
                        baos.flush();
                        baos.close();
                        return false;
                    } catch (IOException e5) {
                        return false;
                    }
                }
            }
            annot.resetAppearanceStream();
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (IOException e6) {
                }
            }
            return true;
        } catch (PDFException e7) {
            e = e7;
            try {
                if (e.getLastError() == PDFError.OOM.getCode()) {
                    this.mPdfViewCtrl.recoverForOOM();
                }
                if (baos == null) {
                    return false;
                }
                try {
                    baos.flush();
                    baos.close();
                    return false;
                } catch (IOException e8) {
                    return false;
                }
            } catch (Throwable th3) {
                th = th3;
                if (baos != null) {
                    try {
                        baos.flush();
                        baos.close();
                    } catch (IOException e9) {
                    }
                }
                throw th;
            }
        } catch (IOException e10) {
            e2 = e10;
            e2.printStackTrace();
            if (baos == null) {
                return false;
            }
            try {
                baos.flush();
                baos.close();
                return false;
            } catch (IOException e11) {
                return false;
            }
        }
    }

    public boolean modify() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Stamp)) {
            return false;
        }
        Stamp annot = this.mAnnot;
        try {
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mContents == null) {
                this.mUndoItem.mContents = "";
            }
            annot.setContent(this.mUndoItem.mContents);
            annot.move(this.mUndoItem.mBBox);
            annot.resetAppearanceStream();
            return true;
        } catch (PDFException e) {
            if (e.getLastError() != PDFError.OOM.getCode()) {
                return false;
            }
            this.mPdfViewCtrl.recoverForOOM();
            return false;
        }
    }

    public boolean delete() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Stamp)) {
            return false;
        }
        try {
            ((Markup) this.mAnnot).removeAllReplies();
            this.mAnnot.getPage().removeAnnot(this.mAnnot);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
