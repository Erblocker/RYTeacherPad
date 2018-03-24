package com.foxit.uiextensions.annots.textmarkup.highlight;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.PDFTextSelect;
import com.foxit.sdk.pdf.annots.Highlight;
import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContent;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContentAbs;
import com.foxit.uiextensions.annots.textmarkup.TextSelector;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.ToolbarItemConfig;
import com.foxit.uiextensions.controls.toolbar.impl.CircleItemImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.util.ArrayList;
import java.util.Iterator;

public class HighlightToolHandler implements ToolHandler {
    private BaseItem mAnnotButton;
    private RectF mBBoxRect;
    private RectF mBbox = new RectF();
    private int mColor;
    private int mCurrentIndex;
    private Rect mInvalidateRect = new Rect();
    private boolean mIsContinuousCreate = false;
    private int mOpacity;
    private int[] mPBColors = new int[PropertyBar.PB_COLORS_HIGHLIGHT.length];
    private Paint mPaint;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyBar mPropertyBar;
    private PropertyChangeListener mPropertyChangeListener;
    private final TextSelector mTextSelector;
    private RectF mTmpRectF = new RectF();
    private Rect mTmpRoundRect = new Rect();
    private boolean misFromSelector = false;

    public HighlightToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mTextSelector = new TextSelector(pdfViewCtrl);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
        this.mBBoxRect = new RectF();
        this.mPropertyBar = new PropertyBarImpl(context, pdfViewCtrl, parent);
        this.mAnnotButton = new CircleItemImpl(context);
        this.mAnnotButton.setImageResource(R.drawable.annot_highlight_selector);
        this.mAnnotButton.setTag(ToolbarItemConfig.ITEM_HIGHLIGHT_TAG);
        this.mAnnotButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HighlightToolHandler.this.mTextSelector.clear();
                ((UIExtensionsManager) HighlightToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(HighlightToolHandler.this);
            }
        });
    }

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
    }

    public TextSelector getTextSelector() {
        return this.mTextSelector;
    }

    public int getPBCustomColor() {
        return PropertyBar.PB_COLORS_HIGHLIGHT[0];
    }

    private void resetPropertyBar() {
        System.arraycopy(PropertyBar.PB_COLORS_HIGHLIGHT, 0, this.mPBColors, 0, this.mPBColors.length);
        this.mPBColors[0] = getPBCustomColor();
        this.mPropertyBar.setColors(this.mPBColors);
        this.mPropertyBar.setProperty(1, this.mColor);
        this.mPropertyBar.setProperty(2, AppDmUtil.opacity255To100(this.mOpacity));
        this.mPropertyBar.reset(3);
        this.mPropertyBar.setPropertyChangeListener(this.mPropertyChangeListener);
    }

    public int getColor() {
        return this.mColor;
    }

    public int getOpacity() {
        return this.mOpacity;
    }

    public String getType() {
        return ToolHandler.TH_TYPE_HIGHLIGHT;
    }

    public void onActivate() {
        this.mTextSelector.clear();
        this.mBBoxRect.setEmpty();
        this.mAnnotButton.setSelected(true);
        resetPropertyBar();
    }

    public void onDeactivate() {
        this.mTextSelector.clear();
        this.mBBoxRect.setEmpty();
        this.mAnnotButton.setSelected(false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            if (!page.isParsed()) {
                for (int ret = page.startParse(0, null, false); ret == 1; ret = page.continueParse()) {
                }
            }
            PDFTextSelect textPage = PDFTextSelect.create(page);
            int action = motionEvent.getAction();
            PointF pagePt = AppAnnotUtil.getPdfPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
            int index;
            switch (action) {
                case 0:
                    this.mCurrentIndex = pageIndex;
                    index = textPage.getIndexAtPos(pagePt.x, pagePt.y, 30.0f);
                    if (index >= 0) {
                        this.mTextSelector.start(page, index);
                        break;
                    }
                    break;
                case 1:
                case 3:
                    if (this.mTextSelector.getRectFList().size() != 0) {
                        this.mTextSelector.setContents(this.mTextSelector.getText(page));
                        addAnnot(this.mCurrentIndex, true, null, null);
                        break;
                    }
                    break;
                case 2:
                    if (this.mCurrentIndex == pageIndex) {
                        index = textPage.getIndexAtPos(pagePt.x, pagePt.y, 30.0f);
                        if (index >= 0) {
                            this.mTextSelector.update(page, index);
                        }
                        invalidateTouch(this.mPdfViewCtrl, pageIndex, this.mTextSelector.getBbox());
                        break;
                    }
                    break;
            }
        } catch (PDFException e1) {
            e1.printStackTrace();
        }
        return true;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (this.mCurrentIndex == pageIndex) {
            Rect clipRect = canvas.getClipBounds();
            Iterator it = this.mTextSelector.getRectFList().iterator();
            while (it.hasNext()) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect((RectF) it.next(), this.mTmpRectF, this.mCurrentIndex);
                this.mTmpRectF.round(this.mTmpRoundRect);
                if (this.mTmpRoundRect.intersect(clipRect)) {
                    canvas.save();
                    canvas.drawRect(this.mTmpRoundRect, this.mPaint);
                    canvas.restore();
                }
            }
        }
    }

    public void setPaint(int color, int opacity) {
        this.mColor = color;
        this.mOpacity = opacity;
        this.mPaint.setColor(calColorByMultiply(this.mColor, this.mOpacity));
    }

    private int calColorByMultiply(int color, int opacity) {
        int rColor = color | -16777216;
        float rOpacity = ((float) opacity) / 255.0f;
        return (((rColor & -16777216) | (((int) ((((float) ((16711680 & rColor) >> 16)) * rOpacity) + ((1.0f - rOpacity) * 255.0f))) << 16)) | (((int) ((((float) ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & rColor) >> 8)) * rOpacity) + ((1.0f - rOpacity) * 255.0f))) << 8)) | ((int) ((((float) (rColor & 255)) * rOpacity) + ((1.0f - rOpacity) * 255.0f)));
    }

    protected void addAnnot(int pageIndex, boolean addUndo, AnnotContent contentSupplier, Callback result) {
        int color = this.mColor;
        try {
            final Highlight annot = (Highlight) this.mPdfViewCtrl.getDoc().getPage(pageIndex).addAnnot(9, new RectF(0.0f, 0.0f, 0.0f, 0.0f));
            if (annot != null) {
                final HighlightAddUndoItem undoItem = new HighlightAddUndoItem(this.mPdfViewCtrl);
                undoItem.mColor = (long) color;
                Object quadPoint = null;
                int i;
                if (contentSupplier != null && (contentSupplier instanceof TextMarkupContent)) {
                    ArrayList<PointF> pointsList = ((TextMarkupContent) TextMarkupContent.class.cast(contentSupplier)).getQuadPoints();
                    quadPoint = new QuadPoints[(pointsList.size() / 4)];
                    for (i = 0; i < pointsList.size() / 4; i++) {
                        quadPoint[i] = new QuadPoints((PointF) pointsList.get(i * 4), (PointF) pointsList.get((i * 4) + 1), (PointF) pointsList.get((i * 4) + 2), (PointF) pointsList.get((i * 4) + 3));
                    }
                    undoItem.mColor = (long) contentSupplier.getColor();
                    undoItem.mOpacity = ((float) contentSupplier.getOpacity()) / 255.0f;
                } else if (contentSupplier != null && (contentSupplier instanceof TextMarkupContentAbs)) {
                    TextMarkupContentAbs tmSelector = (TextMarkupContentAbs) TextMarkupContentAbs.class.cast(contentSupplier);
                    quadPoint = new QuadPoints[tmSelector.getTextSelector().getRectFList().size()];
                    for (i = 0; i < tmSelector.getTextSelector().getRectFList().size(); i++) {
                        rect = (RectF) tmSelector.getTextSelector().getRectFList().get(i);
                        quadPoint[i] = new QuadPoints();
                        quadPoint[i].setFirst(new PointF(rect.left, rect.top));
                        quadPoint[i].setSecond(new PointF(rect.right, rect.top));
                        quadPoint[i].setThird(new PointF(rect.left, rect.bottom));
                        quadPoint[i].setFourth(new PointF(rect.right, rect.bottom));
                    }
                    undoItem.mColor = (long) color;
                    undoItem.mOpacity = ((float) this.mOpacity) / 255.0f;
                    undoItem.mContents = tmSelector.getContents();
                } else if (this.mTextSelector != null) {
                    quadPoint = new QuadPoints[this.mTextSelector.getRectFList().size()];
                    for (i = 0; i < this.mTextSelector.getRectFList().size(); i++) {
                        rect = (RectF) this.mTextSelector.getRectFList().get(i);
                        quadPoint[i] = new QuadPoints();
                        quadPoint[i].setFirst(new PointF(rect.left, rect.top));
                        quadPoint[i].setSecond(new PointF(rect.right, rect.top));
                        quadPoint[i].setThird(new PointF(rect.left, rect.bottom));
                        quadPoint[i].setFourth(new PointF(rect.right, rect.bottom));
                    }
                    undoItem.mColor = (long) color;
                    undoItem.mOpacity = ((float) this.mOpacity) / 255.0f;
                    undoItem.mContents = this.mTextSelector.getContents();
                    undoItem.mFlags = 4;
                } else if (result != null) {
                    result.result(null, false);
                }
                undoItem.quadPointsArray = new QuadPoints[quadPoint.length];
                System.arraycopy(quadPoint, 0, undoItem.quadPointsArray, 0, quadPoint.length);
                undoItem.mNM = AppDmUtil.randomUUID(null);
                undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
                undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
                undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
                undoItem.mPageIndex = pageIndex;
                final int i2 = pageIndex;
                final boolean z = addUndo;
                final Callback callback = result;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new HighlightEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            try {
                                DocumentManager.getInstance(HighlightToolHandler.this.mPdfViewCtrl).onAnnotAdded(HighlightToolHandler.this.mPdfViewCtrl.getDoc().getPage(i2), annot);
                                if (z) {
                                    DocumentManager.getInstance(HighlightToolHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                                }
                                if (HighlightToolHandler.this.mPdfViewCtrl.isPageVisible(i2)) {
                                    HighlightToolHandler.this.invalidate(HighlightToolHandler.this.mPdfViewCtrl, i2, annot.getRect());
                                }
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                            HighlightToolHandler.this.mTextSelector.clear();
                            HighlightToolHandler.this.mBBoxRect.setEmpty();
                            if (!(HighlightToolHandler.this.misFromSelector || HighlightToolHandler.this.mIsContinuousCreate)) {
                                ((UIExtensionsManager) HighlightToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                                HighlightToolHandler.this.mAnnotButton.setSelected(false);
                            }
                            HighlightToolHandler.this.misFromSelector = false;
                        }
                        if (callback != null) {
                            callback.result(null, success);
                        }
                    }
                }));
            } else if (!this.misFromSelector && !this.mIsContinuousCreate) {
                ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                this.mAnnotButton.setSelected(false);
            }
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    private void invalidateTouch(PDFViewCtrl pdfViewCtrl, int pageIndex, RectF rectF) {
        if (rectF != null) {
            RectF rBBox = new RectF(rectF);
            pdfViewCtrl.convertPdfRectToPageViewRect(rBBox, rBBox, pageIndex);
            pdfViewCtrl.convertPageViewRectToDisplayViewRect(rBBox, rBBox, pageIndex);
            calculate(this.mBbox, this.mBBoxRect).roundOut(this.mInvalidateRect);
            pdfViewCtrl.invalidate(this.mInvalidateRect);
            this.mBBoxRect.set(rBBox);
        }
    }

    private RectF calculate(RectF newRectF, RectF oldRectF) {
        if (oldRectF.isEmpty()) {
            return newRectF;
        }
        int count = 0;
        if (newRectF.left == oldRectF.left && newRectF.top == oldRectF.top) {
            count = 0 + 1;
        }
        if (newRectF.right == oldRectF.right && newRectF.top == oldRectF.top) {
            count++;
        }
        if (newRectF.left == oldRectF.left && newRectF.bottom == oldRectF.bottom) {
            count++;
        }
        if (newRectF.right == oldRectF.right && newRectF.bottom == oldRectF.bottom) {
            count++;
        }
        if (count == 2) {
            newRectF.union(oldRectF);
            RectF rectF = new RectF(newRectF);
            newRectF.intersect(oldRectF);
            rectF.intersect(newRectF);
            return rectF;
        } else if (count == 3 || count == 4) {
            return newRectF;
        } else {
            newRectF.union(oldRectF);
            return newRectF;
        }
    }

    private void invalidate(PDFViewCtrl pdfViewCtrl, int pageIndex, RectF rectF) {
        if (rectF != null && pdfViewCtrl.isPageVisible(pageIndex)) {
            this.mBbox.set(rectF);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, this.mBbox, pageIndex);
            this.mBbox.roundOut(this.mInvalidateRect);
            pdfViewCtrl.refresh(pageIndex, this.mInvalidateRect);
        }
    }

    public void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mAnnotButton.setEnable(true);
        } else {
            this.mAnnotButton.setEnable(false);
        }
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
