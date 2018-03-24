package com.foxit.uiextensions.annots.square;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Square;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.ToolbarItemConfig;
import com.foxit.uiextensions.controls.toolbar.impl.CircleItemImpl;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

public class SquareToolHandler implements ToolHandler {
    private static float mCtlPtLineWidth = 2.0f;
    private static float mCtlPtRadius = 5.0f;
    private int mColor;
    private Context mContext;
    private int mControlPtEx = 5;
    private PointF mDownPoint = new PointF(0.0f, 0.0f);
    private Rect mInvalidateRect = new Rect(0, 0, 0, 0);
    private boolean mIsContinuousCreate = false;
    private Paint mLastAnnotPaint;
    private int mLastPageIndex = -1;
    private RectF mNowRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private int mOpacity;
    private RectF mPageViewThickness = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private Paint mPaint;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyBar mPropertyBar;
    private PropertyChangeListener mPropertyChangeListener;
    private PointF mStartPoint = new PointF(0.0f, 0.0f);
    private PointF mStopPoint = new PointF(0.0f, 0.0f);
    RectF mTempRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private Rect mTempRectInTouch = new Rect(0, 0, 0, 0);
    private float mThickness;
    private BaseItem mToolBtn;
    private boolean mTouchCaptured = false;

    public SquareToolHandler(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent) {
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mContext = context;
        this.mParent = parent;
        this.mControlPtEx = AppDisplay.getInstance(context).dp2px((float) this.mControlPtEx);
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mLastAnnotPaint = new Paint();
        this.mLastAnnotPaint.setStyle(Style.STROKE);
        this.mLastAnnotPaint.setAntiAlias(true);
        this.mLastAnnotPaint.setDither(true);
    }

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
    }

    public void setPaint(int pageIndex) {
        this.mPaint.setColor(this.mColor);
        this.mPaint.setAlpha(AppDmUtil.opacity100To255(this.mOpacity));
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(new PointF(thicknessOnPageView(pageIndex, this.mThickness), thicknessOnPageView(pageIndex, this.mThickness)).x);
    }

    private float thicknessOnPageView(int pageIndex, float thickness) {
        this.mPageViewThickness.set(0.0f, 0.0f, thickness, thickness);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mPageViewThickness, this.mPageViewThickness, pageIndex);
        return Math.abs(this.mPageViewThickness.width());
    }

    public int getColor() {
        return this.mColor;
    }

    public int getOpacity() {
        return this.mOpacity;
    }

    public float getLineWidth() {
        return this.mThickness;
    }

    public String getType() {
        return ToolHandler.TH_TYPE_SQUARE;
    }

    public void onActivate() {
        this.mLastPageIndex = -1;
        mCtlPtRadius = 5.0f;
        mCtlPtRadius = (float) AppDisplay.getInstance(this.mContext).dp2px(mCtlPtRadius);
    }

    public void onDeactivate() {
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        PointF disPoint = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF pvPoint = new PointF();
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(disPoint, pvPoint, pageIndex);
        float x = pvPoint.x;
        float y = pvPoint.y;
        switch (motionEvent.getAction()) {
            case 0:
                if ((!this.mTouchCaptured && this.mLastPageIndex == -1) || this.mLastPageIndex == pageIndex) {
                    this.mTouchCaptured = true;
                    this.mStartPoint.x = x;
                    this.mStartPoint.y = y;
                    this.mStopPoint.x = x;
                    this.mStopPoint.y = y;
                    this.mDownPoint.set(x, y);
                    this.mTempRectInTouch.setEmpty();
                    if (this.mLastPageIndex == -1) {
                        this.mLastPageIndex = pageIndex;
                    }
                }
                return true;
            case 1:
            case 3:
                if (this.mTouchCaptured && this.mLastPageIndex == pageIndex) {
                    if (this.mStartPoint.equals(this.mStopPoint.x, this.mStopPoint.y)) {
                        this.mStartPoint.set(0.0f, 0.0f);
                        this.mStopPoint.set(0.0f, 0.0f);
                        this.mNowRect.setEmpty();
                        this.mDownPoint.set(0.0f, 0.0f);
                        this.mTouchCaptured = false;
                        this.mLastPageIndex = -1;
                        this.mDownPoint.set(0.0f, 0.0f);
                        if (!this.mIsContinuousCreate) {
                            ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                            this.mToolBtn.setSelected(false);
                        }
                    } else {
                        createAnnot();
                    }
                    return true;
                }
            case 2:
                if (this.mTouchCaptured && this.mLastPageIndex == pageIndex) {
                    if (!this.mDownPoint.equals(x, y)) {
                        this.mStopPoint.x = x;
                        this.mStopPoint.y = y;
                        float deltaXY = (((thicknessOnPageView(pageIndex, this.mThickness) / 2.0f) + mCtlPtLineWidth) + (mCtlPtRadius * 2.0f)) + 2.0f;
                        float line_k = (y - this.mStartPoint.y) / (x - this.mStartPoint.x);
                        float line_b = this.mStartPoint.y - (this.mStartPoint.x * line_k);
                        if (y <= deltaXY && line_k != 0.0f) {
                            this.mStopPoint.y = deltaXY;
                            this.mStopPoint.x = (this.mStopPoint.y - line_b) / line_k;
                        } else if (y >= ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - deltaXY && line_k != 0.0f) {
                            this.mStopPoint.y = ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - deltaXY;
                            this.mStopPoint.x = (this.mStopPoint.y - line_b) / line_k;
                        }
                        if (this.mStopPoint.x <= deltaXY) {
                            this.mStopPoint.x = deltaXY;
                        } else if (this.mStopPoint.x >= ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - deltaXY) {
                            this.mStopPoint.x = ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - deltaXY;
                        }
                        getDrawRect(this.mStartPoint.x, this.mStartPoint.y, this.mStopPoint.x, this.mStopPoint.y);
                        this.mInvalidateRect.set((int) this.mNowRect.left, (int) this.mNowRect.top, (int) this.mNowRect.right, (int) this.mNowRect.bottom);
                        this.mInvalidateRect.inset((int) (((-this.mThickness) * 12.0f) - ((float) this.mControlPtEx)), (int) (((-this.mThickness) * 12.0f) - ((float) this.mControlPtEx)));
                        if (!this.mTempRectInTouch.isEmpty()) {
                            this.mInvalidateRect.union(this.mTempRectInTouch);
                        }
                        this.mTempRectInTouch.set(this.mInvalidateRect);
                        RectF _rect = AppDmUtil.rectToRectF(this.mInvalidateRect);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(_rect, _rect, pageIndex);
                        this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(_rect));
                        this.mDownPoint.set(x, y);
                    }
                    return true;
                }
                break;
            default:
                return true;
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
        if (this.mStartPoint != null && this.mStopPoint != null && this.mLastPageIndex == pageIndex) {
            canvas.save();
            setPaint(pageIndex);
            canvas.drawRect(this.mNowRect, this.mPaint);
            canvas.restore();
        }
    }

    private RectF getBBox(int pageIndex) {
        RectF bboxRect = new RectF();
        this.mTempRect.set(this.mNowRect);
        this.mTempRect.inset((-thicknessOnPageView(pageIndex, this.mThickness)) / 2.0f, (-thicknessOnPageView(pageIndex, this.mThickness)) / 2.0f);
        this.mPdfViewCtrl.convertPageViewRectToPdfRect(this.mTempRect, this.mTempRect, pageIndex);
        bboxRect.left = this.mTempRect.left;
        bboxRect.right = this.mTempRect.right;
        bboxRect.top = this.mTempRect.top;
        bboxRect.bottom = this.mTempRect.bottom;
        return bboxRect;
    }

    private void createAnnot() {
        if (this.mPdfViewCtrl.isPageVisible(this.mLastPageIndex)) {
            RectF bboxRect = getBBox(this.mLastPageIndex);
            try {
                final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mLastPageIndex);
                final Square newAnnot = (Square) page.addAnnot(5, bboxRect);
                final SquareAddUndoItem undoItem = new SquareAddUndoItem(this.mPdfViewCtrl);
                undoItem.mPageIndex = this.mLastPageIndex;
                undoItem.mColor = (long) this.mColor;
                undoItem.mNM = AppDmUtil.randomUUID(null);
                undoItem.mOpacity = ((float) AppDmUtil.opacity100To255(this.mOpacity)) / 255.0f;
                undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
                undoItem.mBorderStyle = 0;
                undoItem.mLineWidth = this.mThickness;
                undoItem.mFlags = 4;
                undoItem.mSubject = "Rectangle";
                undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
                undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
                undoItem.mBBox = new RectF(bboxRect);
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new SquareEvent(1, undoItem, newAnnot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            DocumentManager.getInstance(SquareToolHandler.this.mPdfViewCtrl).onAnnotAdded(page, newAnnot);
                            DocumentManager.getInstance(SquareToolHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            if (SquareToolHandler.this.mPdfViewCtrl.isPageVisible(SquareToolHandler.this.mLastPageIndex)) {
                                RectF viewRect = null;
                                try {
                                    viewRect = newAnnot.getRect();
                                } catch (PDFException e) {
                                    e.printStackTrace();
                                }
                                SquareToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, SquareToolHandler.this.mLastPageIndex);
                                Rect rect = new Rect();
                                viewRect.roundOut(rect);
                                rect.inset(-10, -10);
                                SquareToolHandler.this.mPdfViewCtrl.refresh(SquareToolHandler.this.mLastPageIndex, rect);
                                SquareToolHandler.this.mTouchCaptured = false;
                                SquareToolHandler.this.mLastPageIndex = -1;
                                SquareToolHandler.this.mDownPoint.set(0.0f, 0.0f);
                                if (!SquareToolHandler.this.mIsContinuousCreate) {
                                    ((UIExtensionsManager) SquareToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                                    SquareToolHandler.this.mToolBtn.setSelected(false);
                                }
                            }
                        }
                    }
                }));
            } catch (PDFException e) {
            }
        }
    }

    public void getDrawRect(float x1, float y1, float x2, float y2) {
        float minx = Math.min(x1, x2);
        float miny = Math.min(y1, y2);
        float maxx = Math.max(x1, x2);
        float maxy = Math.max(y1, y2);
        this.mNowRect.left = minx;
        this.mNowRect.top = miny;
        this.mNowRect.right = maxx;
        this.mNowRect.bottom = maxy;
    }

    public void init() {
        this.mPropertyBar = new PropertyBarImpl(this.mContext, this.mPdfViewCtrl, this.mParent);
        this.mToolBtn = new CircleItemImpl(this.mContext);
        this.mToolBtn.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_CIR_TAG);
        this.mToolBtn.setImageResource(R.drawable.annot_circle_selector);
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mToolBtn.setEnable(false);
        }
        if (this.mColor == 0) {
            this.mColor = PropertyBar.PB_COLORS_SQUARE[0];
        }
        if (this.mOpacity == 0) {
            this.mOpacity = 100;
        }
        if (0.0f == this.mThickness) {
            this.mThickness = 5.0f;
        }
        this.mToolBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SquareToolHandler.this.changeStatusForClick();
            }
        });
        this.mToolBtn.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (DocumentManager.getInstance(SquareToolHandler.this.mPdfViewCtrl).canAddAnnot() && ((UIExtensionsManager) SquareToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != SquareToolHandler.this) {
                    ((UIExtensionsManager) SquareToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(SquareToolHandler.this);
                }
                return true;
            }
        });
    }

    private void changeStatusForClick() {
        if (!DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            return;
        }
        if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != this) {
            ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(this);
            this.mToolBtn.setSelected(true);
            return;
        }
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
        this.mToolBtn.setSelected(false);
    }

    public void changeCurrentColor(int currentColor) {
        this.mColor = currentColor;
    }

    public void changeCurrentOpacity(int currentOpacity) {
        this.mOpacity = currentOpacity;
    }

    public void changeCurrentThickness(float currentThickness) {
        this.mThickness = currentThickness;
    }

    public void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mToolBtn.setEnable(true);
        } else {
            this.mToolBtn.setEnable(false);
        }
        if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() == this) {
            this.mToolBtn.setSelected(true);
            preparePropertyBar();
            return;
        }
        this.mToolBtn.setSelected(false);
    }

    public void removePropertyListener() {
        this.mPropertyChangeListener = null;
    }

    private void preparePropertyBar() {
        int[] colors = new int[PropertyBar.PB_COLORS_SQUARE.length];
        System.arraycopy(PropertyBar.PB_COLORS_SQUARE, 0, colors, 0, colors.length);
        colors[0] = PropertyBar.PB_COLORS_SQUARE[0];
        this.mPropertyBar.setColors(colors);
        this.mPropertyBar.setProperty(1, this.mColor);
        this.mPropertyBar.setProperty(2, this.mOpacity);
        this.mPropertyBar.setProperty(4, this.mThickness);
        this.mPropertyBar.setArrowVisible(true);
        this.mPropertyBar.reset(getSupportedProperties());
        this.mPropertyBar.setPropertyChangeListener(this.mPropertyChangeListener);
    }

    private long getSupportedProperties() {
        return 7;
    }

    public boolean getIsContinuousCreate() {
        return this.mIsContinuousCreate;
    }

    public void setIsContinuousCreate(boolean isContinuousCreate) {
        this.mIsContinuousCreate = isContinuousCreate;
    }
}
