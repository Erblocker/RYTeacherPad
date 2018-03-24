package com.foxit.uiextensions.annots.textmarkup.underline;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.PDFTextSelect;
import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.sdk.pdf.annots.Underline;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupUtil;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.util.ArrayList;
import java.util.Iterator;

public class UnderlineToolHandler implements ToolHandler {
    private int mColor;
    private Context mContext;
    private int mCurrentIndex;
    private boolean mIsContinuousCreate = false;
    private int mOpacity;
    private int[] mPBColors = new int[PropertyBar.PB_COLORS_UNDERLINE.length];
    private Paint mPaint;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyBar mPropertyBar;
    private PropertyChangeListener mPropertyChangeListener;
    public SelectInfo mSelectInfo;
    private RectF mTmpDesRect;
    private RectF mTmpRect;
    private boolean misFromSelector = false;

    public class SelectInfo {
        public RectF mBBox = new RectF();
        public int mEndChar;
        public boolean mIsFromTS;
        public ArrayList<RectF> mRectArray = new ArrayList();
        public ArrayList<Boolean> mRectVert = new ArrayList();
        public ArrayList<Integer> mRotaton = new ArrayList();
        public int mStartChar;

        public void clear() {
            this.mIsFromTS = false;
            this.mEndChar = -1;
            this.mStartChar = -1;
            this.mBBox.setEmpty();
            this.mRectArray.clear();
        }
    }

    public UnderlineToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mSelectInfo = new SelectInfo();
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mTmpRect = new RectF();
        this.mTmpDesRect = new RectF();
        init();
    }

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
    }

    private void init() {
        this.mPropertyBar = new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent);
    }

    public void unInit() {
    }

    public String getType() {
        return ToolHandler.TH_TYPE_UNDERLINE;
    }

    public void onActivate() {
        resetLineData();
    }

    public void onDeactivate() {
    }

    private boolean OnSelectDown(int pageIndex, PointF point, SelectInfo selectInfo) {
        if (selectInfo == null) {
            return false;
        }
        try {
            this.mCurrentIndex = pageIndex;
            selectInfo.mRectArray.clear();
            selectInfo.mEndChar = -1;
            selectInfo.mStartChar = -1;
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mCurrentIndex);
            if (!page.isParsed()) {
                for (int ret = page.startParse(0, null, false); ret == 1; ret = page.continueParse()) {
                }
            }
            PDFTextSelect textPage = PDFTextSelect.create(page);
            PointF pagePt = new PointF();
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(point, pagePt, this.mCurrentIndex);
            int index = textPage.getIndexAtPos(pagePt.x, pagePt.y, 30.0f);
            if (index >= 0) {
                selectInfo.mEndChar = index;
                selectInfo.mStartChar = index;
            }
            this.mPdfViewCtrl.getDoc().closePage(this.mCurrentIndex);
            return true;
        } catch (PDFException e) {
            if (e.getLastError() != PDFError.OOM.getCode()) {
                return false;
            }
            this.mPdfViewCtrl.recoverForOOM();
            return false;
        }
    }

    private boolean OnSelectMove(int pageIndex, PointF point, SelectInfo selectInfo) {
        if (selectInfo == null || this.mCurrentIndex != pageIndex) {
            return false;
        }
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mCurrentIndex);
            if (!page.isParsed()) {
                for (int ret = page.startParse(0, null, false); ret == 1; ret = page.continueParse()) {
                }
            }
            PDFTextSelect textPage = PDFTextSelect.create(page);
            PointF pagePt = new PointF();
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(point, pagePt, this.mCurrentIndex);
            int index = textPage.getIndexAtPos(pagePt.x, pagePt.y, 30.0f);
            if (index >= 0) {
                if (selectInfo.mStartChar < 0) {
                    selectInfo.mStartChar = index;
                }
                selectInfo.mEndChar = index;
            }
            this.mPdfViewCtrl.getDoc().closePage(this.mCurrentIndex);
            return true;
        } catch (PDFException e) {
            if (e.getLastError() != PDFError.OOM.getCode()) {
                return false;
            }
            this.mPdfViewCtrl.recoverForOOM();
            return false;
        }
    }

    public boolean OnSelectRelease(int pageIndex, SelectInfo selectInfo, Callback result) {
        if (selectInfo == null) {
            return false;
        }
        if (this.mSelectInfo.mRectArray.size() == 0) {
            return false;
        }
        RectF rectF = new RectF();
        rectF.set(this.mSelectInfo.mBBox);
        rectF.bottom += 2.0f;
        rectF.left -= 2.0f;
        rectF.right += 2.0f;
        rectF.top -= 2.0f;
        RectF pageRt = new RectF();
        this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, pageRt, pageIndex);
        addAnnot(pageIndex, true, this.mSelectInfo.mRectArray, pageRt, selectInfo, result);
        return true;
    }

    public void SelectCountRect(int pageIndex, SelectInfo selectInfo) {
        if (selectInfo != null) {
            int start = selectInfo.mStartChar;
            int end = selectInfo.mEndChar;
            if (start != end || start != -1) {
                if (end < start) {
                    int tmp = end;
                    end = start;
                    start = tmp;
                }
                selectInfo.mRectArray.clear();
                selectInfo.mRectVert.clear();
                try {
                    PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
                    if (!page.isParsed()) {
                        for (int ret = page.startParse(0, null, false); ret == 1; ret = page.continueParse()) {
                        }
                    }
                    PDFTextSelect textPage = PDFTextSelect.create(page);
                    int count = textPage.getTextRectCount(start, (end - start) + 1);
                    for (int i = 0; i < count; i++) {
                        RectF crect = new RectF();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(textPage.getTextRect(i), crect, pageIndex);
                        int rotate = textPage.getBaselineRotation(i);
                        boolean vert = rotate == 1 || rotate == 3;
                        this.mSelectInfo.mRectArray.add(crect);
                        this.mSelectInfo.mRectVert.add(Boolean.valueOf(vert));
                        this.mSelectInfo.mRotaton.add(Integer.valueOf(rotate));
                        if (i == 0) {
                            selectInfo.mBBox = new RectF(crect);
                        } else {
                            reSizeRect(selectInfo.mBBox, crect);
                        }
                    }
                    textPage.release();
                    this.mPdfViewCtrl.getDoc().closePage(pageIndex);
                } catch (PDFException e) {
                    if (e.getLastError() == PDFError.OOM.getCode()) {
                        this.mPdfViewCtrl.recoverForOOM();
                    }
                }
            }
        }
    }

    private void reSizeRect(RectF MainRt, RectF rect) {
        if (rect.left < MainRt.left) {
            MainRt.left = rect.left;
        }
        if (rect.right > MainRt.right) {
            MainRt.right = rect.right;
        }
        if (rect.bottom > MainRt.bottom) {
            MainRt.bottom = rect.bottom;
        }
        if (rect.top < MainRt.top) {
            MainRt.top = rect.top;
        }
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        PointF point = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        switch (action) {
            case 0:
                OnSelectDown(pageIndex, point, this.mSelectInfo);
                break;
            case 1:
            case 3:
                OnSelectRelease(pageIndex, this.mSelectInfo, null);
                break;
            case 2:
                OnSelectMove(pageIndex, point, this.mSelectInfo);
                SelectCountRect(pageIndex, this.mSelectInfo);
                invalidateTouch(this.mSelectInfo, pageIndex);
                break;
        }
        return true;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    private void invalidateTouch(SelectInfo selectInfo, int pageIndex) {
        if (selectInfo != null) {
            RectF rectF = new RectF();
            rectF.set(this.mSelectInfo.mBBox);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
            RectF rF = calculate(rectF, this.mTmpRect);
            Rect rect = new Rect();
            rF.roundOut(rect);
            rect.bottom += 4;
            rect.top -= 4;
            rect.left -= 4;
            rect.right += 4;
            this.mPdfViewCtrl.invalidate(rect);
            this.mTmpRect.set(rectF);
        }
    }

    private RectF calculate(RectF desRectF, RectF srcRectF) {
        if (srcRectF.isEmpty()) {
            return desRectF;
        }
        int count = 0;
        if (desRectF.left == srcRectF.left && desRectF.top == srcRectF.top) {
            count = 0 + 1;
        }
        if (desRectF.right == srcRectF.right && desRectF.top == srcRectF.top) {
            count++;
        }
        if (desRectF.left == srcRectF.left && desRectF.bottom == srcRectF.bottom) {
            count++;
        }
        if (desRectF.right == srcRectF.right && desRectF.bottom == srcRectF.bottom) {
            count++;
        }
        this.mTmpDesRect.set(desRectF);
        if (count == 2) {
            this.mTmpDesRect.union(srcRectF);
            RectF rectF = new RectF();
            rectF.set(this.mTmpDesRect);
            this.mTmpDesRect.intersect(srcRectF);
            rectF.intersect(this.mTmpDesRect);
            return rectF;
        } else if (count == 3 || count == 4) {
            return this.mTmpDesRect;
        } else {
            this.mTmpDesRect.union(srcRectF);
            return this.mTmpDesRect;
        }
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (this.mCurrentIndex == pageIndex) {
            Rect clipRect = canvas.getClipBounds();
            int i = 0;
            PointF startPointF = new PointF();
            PointF endPointF = new PointF();
            RectF widthRect = new RectF();
            Iterator it = this.mSelectInfo.mRectArray.iterator();
            while (it.hasNext()) {
                RectF rect = (RectF) it.next();
                Rect r = new Rect();
                rect.round(r);
                if (r.intersect(clipRect)) {
                    RectF tmpF = new RectF();
                    tmpF.set(rect);
                    if (i < this.mSelectInfo.mRectVert.size()) {
                        boolean vert = ((Boolean) this.mSelectInfo.mRectVert.get(i)).booleanValue();
                        this.mPdfViewCtrl.convertPageViewRectToPdfRect(rect, widthRect, pageIndex);
                        if (widthRect.top - widthRect.bottom > widthRect.right - widthRect.left) {
                            TextMarkupUtil.resetDrawLineWidth(this.mPdfViewCtrl, pageIndex, this.mPaint, widthRect.right, widthRect.left);
                        } else {
                            TextMarkupUtil.resetDrawLineWidth(this.mPdfViewCtrl, pageIndex, this.mPaint, widthRect.top, widthRect.bottom);
                        }
                        if (vert) {
                            if (((Integer) this.mSelectInfo.mRotaton.get(i)).intValue() == 3) {
                                startPointF.x = tmpF.right - ((tmpF.right - tmpF.left) / 8.0f);
                            } else {
                                startPointF.x = tmpF.left + ((tmpF.right - tmpF.left) / 8.0f);
                            }
                            startPointF.y = tmpF.top;
                            endPointF.x = startPointF.x;
                            endPointF.y = tmpF.bottom;
                        } else {
                            startPointF.x = tmpF.left;
                            startPointF.y = tmpF.bottom + ((tmpF.top - tmpF.bottom) / 8.0f);
                            endPointF.x = tmpF.right;
                            endPointF.y = startPointF.y;
                        }
                        canvas.save();
                        canvas.drawLine(startPointF.x, startPointF.y, endPointF.x, endPointF.y, this.mPaint);
                        canvas.restore();
                    }
                }
                i++;
            }
        }
    }

    private void addAnnot(int pageIndex, boolean addUndo, ArrayList<RectF> rectArray, RectF rectF, SelectInfo selectInfo, Callback result) {
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            Underline annot = (Underline) page.addAnnot(10, rectF);
            if (annot == null) {
                if (!(this.misFromSelector || this.mIsContinuousCreate)) {
                    ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
                this.misFromSelector = false;
                return;
            }
            Object quadPoint = new QuadPoints[rectArray.size()];
            for (int i = 0; i < rectArray.size(); i++) {
                if (i < selectInfo.mRectVert.size()) {
                    RectF rF = new RectF();
                    this.mPdfViewCtrl.convertPageViewRectToPdfRect((RectF) rectArray.get(i), rF, pageIndex);
                    if (((Boolean) selectInfo.mRectVert.get(i)).booleanValue()) {
                        quadPoint[i] = new QuadPoints();
                        quadPoint[i].setFirst(new PointF(rF.left, rF.top));
                        quadPoint[i].setSecond(new PointF(rF.left, rF.bottom));
                        quadPoint[i].setThird(new PointF(rF.right, rF.top));
                        quadPoint[i].setFourth(new PointF(rF.right, rF.bottom));
                    } else {
                        quadPoint[i] = new QuadPoints();
                        quadPoint[i].setFirst(new PointF(rF.left, rF.top));
                        quadPoint[i].setSecond(new PointF(rF.right, rF.top));
                        quadPoint[i].setThird(new PointF(rF.left, rF.bottom));
                        quadPoint[i].setFourth(new PointF(rF.right, rF.bottom));
                    }
                }
            }
            final UnderlineAddUndoItem undoItem = new UnderlineAddUndoItem(this.mPdfViewCtrl);
            undoItem.mType = 10;
            undoItem.mColor = (long) this.mColor;
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mQuadPoints = new QuadPoints[quadPoint.length];
            System.arraycopy(quadPoint, 0, undoItem.mQuadPoints, 0, quadPoint.length);
            undoItem.mContents = getContent(page, selectInfo);
            undoItem.mNM = AppDmUtil.randomUUID(null);
            undoItem.mSubject = "Underline";
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mFlags = 4;
            undoItem.mOpacity = ((float) this.mOpacity) / 255.0f;
            undoItem.mPageIndex = pageIndex;
            final PDFPage finalPage = page;
            final Underline finalAnnot = annot;
            final boolean z = addUndo;
            final int i2 = pageIndex;
            final RectF rectF2 = rectF;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new UnderlineEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(UnderlineToolHandler.this.mPdfViewCtrl).onAnnotAdded(finalPage, finalAnnot);
                        if (z) {
                            DocumentManager.getInstance(UnderlineToolHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        }
                        if (UnderlineToolHandler.this.mPdfViewCtrl.isPageVisible(i2)) {
                            UnderlineToolHandler.this.invalidate(i2, rectF2, callback);
                        }
                        UnderlineToolHandler.this.resetLineData();
                        if (!(UnderlineToolHandler.this.misFromSelector || UnderlineToolHandler.this.mIsContinuousCreate)) {
                            ((UIExtensionsManager) UnderlineToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                        }
                        UnderlineToolHandler.this.misFromSelector = false;
                    }
                }
            }));
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    private String getContent(PDFPage page, SelectInfo selectInfo) {
        int start = selectInfo.mStartChar;
        int end = selectInfo.mEndChar;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        try {
            if (!page.isParsed()) {
                int ret = page.startParse(0, null, false);
                while (ret == 1) {
                    ret = page.continueParse();
                }
            }
            return PDFTextSelect.create(page).getChars(start, (end - start) + 1);
        } catch (PDFException e) {
            if (e.getLastError() != PDFError.OOM.getCode()) {
                return null;
            }
            this.mPdfViewCtrl.recoverForOOM();
            return null;
        }
    }

    private void invalidate(int pageIndex, RectF dmrectf, Callback result) {
        if (dmrectf != null) {
            RectF rectF = new RectF();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(dmrectf, rectF, pageIndex);
            Rect rect = new Rect();
            rectF.roundOut(rect);
            this.mPdfViewCtrl.refresh(pageIndex, rect);
            if (result != null) {
                result.result(null, false);
            }
        } else if (result != null) {
            result.result(null, true);
        }
    }

    public void setPaint(int color, int opacity) {
        this.mColor = color;
        this.mOpacity = opacity;
        this.mPaint.setColor(this.mColor);
        this.mPaint.setAlpha(this.mOpacity);
    }

    public int getPBCustomColor() {
        return PropertyBar.PB_COLORS_UNDERLINE[0];
    }

    public int getColor() {
        return this.mColor;
    }

    public int getOpacity() {
        return this.mOpacity;
    }

    private void resetLineData() {
        SelectInfo selectInfo = this.mSelectInfo;
        this.mSelectInfo.mEndChar = -1;
        selectInfo.mStartChar = -1;
        this.mSelectInfo.mRectArray.clear();
        this.mSelectInfo.mBBox.setEmpty();
        this.mTmpRect.setEmpty();
    }

    public void AddAnnot(int pageIndex, boolean addUndo, AnnotContent contentSupplier, ArrayList<RectF> arrayList, RectF dmRectf, SelectInfo selectInfo, Callback result) {
        try {
            final Underline annot = (Underline) this.mPdfViewCtrl.getDoc().getPage(pageIndex).addAnnot(10, dmRectf);
            final UnderlineAddUndoItem undoItem = new UnderlineAddUndoItem(this.mPdfViewCtrl);
            undoItem.mPageIndex = pageIndex;
            int count = annot.getQuadPointsCount();
            undoItem.mQuadPoints = new QuadPoints[count];
            for (int i = 0; i < count; i++) {
                undoItem.mQuadPoints[i] = annot.getQuadPoints(i);
            }
            undoItem.mColor = (long) contentSupplier.getColor();
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mNM = AppDmUtil.randomUUID(null);
            undoItem.mSubject = "Underline";
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mFlags = 4;
            UnderlineEvent event = new UnderlineEvent(1, undoItem, annot, this.mPdfViewCtrl);
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            final boolean z = addUndo;
            final int i2 = pageIndex;
            final RectF rectF = dmRectf;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(event, new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(UnderlineToolHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (z) {
                            DocumentManager.getInstance(UnderlineToolHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        }
                        if (UnderlineToolHandler.this.mPdfViewCtrl.isPageVisible(i2)) {
                            UnderlineToolHandler.this.invalidate(i2, rectF, callback);
                        } else if (callback != null) {
                            callback.result(event, success);
                        }
                    } else if (callback != null) {
                        callback.result(event, success);
                    }
                }
            }));
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != this || keyCode != 4) {
            return false;
        }
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
        return true;
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public boolean getIsContinuousCreate() {
        return this.mIsContinuousCreate;
    }

    public void setIsContinuousCreate(boolean isContinuousCreate) {
        this.mIsContinuousCreate = isContinuousCreate;
    }

    public void removeProbarListener() {
        this.mPropertyChangeListener = null;
    }

    public void setFromSelector(boolean b) {
        this.misFromSelector = b;
    }
}
