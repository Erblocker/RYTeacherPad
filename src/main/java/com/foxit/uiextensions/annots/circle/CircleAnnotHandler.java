package com.foxit.uiextensions.annots.circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.BorderInfo;
import com.foxit.sdk.pdf.annots.Circle;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;

public class CircleAnnotHandler implements AnnotHandler {
    public static final int CTR_B = 6;
    public static final int CTR_L = 8;
    public static final int CTR_LB = 7;
    public static final int CTR_LT = 1;
    public static final int CTR_NONE = -1;
    public static final int CTR_R = 4;
    public static final int CTR_RB = 5;
    public static final int CTR_RT = 3;
    public static final int CTR_T = 2;
    public static final int OPER_DEFAULT = -1;
    public static final int OPER_SCALE_B = 6;
    public static final int OPER_SCALE_L = 8;
    public static final int OPER_SCALE_LB = 7;
    public static final int OPER_SCALE_LT = 1;
    public static final int OPER_SCALE_R = 4;
    public static final int OPER_SCALE_RB = 5;
    public static final int OPER_SCALE_RT = 3;
    public static final int OPER_SCALE_T = 2;
    public static final int OPER_TRANSLATE = 9;
    private static float mCtlPtDeltyXY = 20.0f;
    private static float mCtlPtLineWidth = 2.0f;
    private static float mCtlPtRadius = 5.0f;
    private static float mCtlPtTouchExt = 20.0f;
    private PointF mAdjustPointF = new PointF(0.0f, 0.0f);
    private RectF mAnnotMenuRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private AnnotMenu mAnnotationMenu;
    private PropertyBar mAnnotationProperty;
    private RectF mBBoxInOnDraw = new RectF();
    private Annot mBitmapAnnot;
    private Context mContext;
    private Paint mCtlPtPaint;
    private int mCurrentCtr = -1;
    private RectF mDocViewerBBox = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private PointF mDocViewerPt = new PointF(0.0f, 0.0f);
    private PointF mDownPoint;
    private DrawFilter mDrawFilter = new PaintFlagsDrawFilter(0, 3);
    private Paint mFrmPaint;
    Path mImaginaryPath = new Path();
    private RectF mInvalidateRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private boolean mIsEditProperty;
    private boolean mIsModify;
    private int mLastOper = -1;
    private PointF mLastPoint;
    RectF mMapBounds = new RectF();
    private ArrayList<Integer> mMenuText;
    private RectF mPageDrawRect = new RectF();
    private RectF mPageViewRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private ViewGroup mParent;
    private Paint mPathPaint;
    private PointF mPdfPointF = new PointF(0.0f, 0.0f);
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyChangeListener mPropertyChangeListener;
    private RectF mTempLastBBox = new RectF();
    private int mTempLastColor;
    private float mTempLastLineWidth;
    private int mTempLastOpacity;
    private float mThickness = 0.0f;
    private RectF mThicknessRectF = new RectF();
    private boolean mTouchCaptured = false;
    private RectF mViewDrawRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private RectF mViewDrawRectInOnDraw = new RectF();

    public CircleAnnotHandler(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mDownPoint = new PointF();
        this.mLastPoint = new PointF();
        this.mPathPaint = new Paint();
        this.mPathPaint.setStyle(Style.STROKE);
        this.mPathPaint.setAntiAlias(true);
        this.mPathPaint.setDither(true);
        PathEffect effect = AppAnnotUtil.getAnnotBBoxPathEffect();
        this.mFrmPaint = new Paint();
        this.mFrmPaint.setPathEffect(effect);
        this.mFrmPaint.setStyle(Style.STROKE);
        this.mFrmPaint.setAntiAlias(true);
        this.mCtlPtPaint = new Paint();
        this.mMenuText = new ArrayList();
    }

    public void setAnnotMenu(AnnotMenu annotMenu) {
        this.mAnnotationMenu = annotMenu;
    }

    public AnnotMenu getAnnotMenu() {
        return this.mAnnotationMenu;
    }

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mAnnotationProperty = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mAnnotationProperty;
    }

    public void onColorValueChanged(int color) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if (annot != null) {
            try {
                if (ToolUtil.getCurrentAnnotHandler(uiExtensionsManager) == this && ((long) color) != annot.getBorderColor()) {
                    modifyAnnot(annot.getPage().getIndex(), annot, annot.getRect(), color, (int) (((Circle) annot).getOpacity() * 255.0f), annot.getBorderInfo().getWidth(), annot.getContent(), false, false, null);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onOpacityValueChanged(int opacity) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if (annot != null) {
            try {
                if (ToolUtil.getCurrentAnnotHandler(uiExtensionsManager) == this && opacity != ((int) (((Circle) annot).getOpacity() * 255.0f))) {
                    modifyAnnot(annot.getPage().getIndex(), annot, annot.getRect(), (int) annot.getBorderColor(), AppDmUtil.opacity100To255(opacity), annot.getBorderInfo().getWidth(), annot.getContent(), false, false, null);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onLineWidthValueChanged(float lineWidth) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if (annot != null) {
            try {
                if (ToolUtil.getCurrentAnnotHandler(uiExtensionsManager) == this && lineWidth != annot.getBorderInfo().getWidth()) {
                    float deltLineWidth = annot.getBorderInfo().getWidth() - lineWidth;
                    modifyAnnot(annot.getPage().getIndex(), annot, annot.getRect(), (int) annot.getBorderColor(), (int) (((Circle) annot).getOpacity() * 255.0f), lineWidth, annot.getContent(), false, false, null);
                    if (this.mAnnotationMenu.isShowing()) {
                        RectF pageViewBBox = annot.getRect();
                        pageViewBBox.inset(0.5f * deltLineWidth, 0.5f * deltLineWidth);
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewBBox, pageViewBBox, annot.getPage().getIndex());
                        this.mAnnotationMenu.update(pageViewBBox);
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    private float thicknessOnPageView(int pageIndex, float thickness) {
        this.mThicknessRectF.set(0.0f, 0.0f, thickness, thickness);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mThicknessRectF, this.mThicknessRectF, pageIndex);
        return Math.abs(this.mThicknessRectF.width());
    }

    private void resetAnnotationMenuResource(Annot annot) {
        this.mMenuText.clear();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mMenuText.add(Integer.valueOf(6));
            this.mMenuText.add(Integer.valueOf(3));
            this.mMenuText.add(Integer.valueOf(4));
            this.mMenuText.add(Integer.valueOf(2));
            return;
        }
        this.mMenuText.add(Integer.valueOf(3));
    }

    private void modifyAnnot(int pageIndex, Annot annot, RectF bbox, int color, int opacity, float lineWidth, String contents, boolean isModifyJni, boolean addUndo, Callback result) {
        final CircleModifyUndoItem undoItem = new CircleModifyUndoItem(this.mPdfViewCtrl);
        undoItem.setCurrentValue(annot);
        undoItem.mPageIndex = pageIndex;
        undoItem.mBBox = new RectF(bbox);
        undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
        undoItem.mColor = (long) color;
        undoItem.mOpacity = ((float) opacity) / 255.0f;
        undoItem.mLineWidth = lineWidth;
        undoItem.mContents = contents;
        undoItem.mRedoColor = color;
        undoItem.mRedoOpacity = ((float) opacity) / 255.0f;
        undoItem.mRedoBbox = new RectF(bbox);
        undoItem.mRedoLineWidth = lineWidth;
        undoItem.mRedoContent = contents;
        undoItem.mUndoColor = this.mTempLastColor;
        undoItem.mUndoOpacity = ((float) this.mTempLastOpacity) / 255.0f;
        undoItem.mUndoBbox = new RectF(this.mTempLastBBox);
        undoItem.mUndoLineWidth = this.mTempLastLineWidth;
        try {
            undoItem.mUndoContent = annot.getContent();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        if (isModifyJni) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(addUndo);
            final boolean z = addUndo;
            final int i = pageIndex;
            final Annot annot2 = annot;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CircleEvent(2, undoItem, (Circle) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (z) {
                            DocumentManager.getInstance(CircleAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            DocumentManager.getInstance(CircleAnnotHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                        }
                        RectF tempRectF = CircleAnnotHandler.this.mTempLastBBox;
                        if (CircleAnnotHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                            RectF annotRectF = null;
                            try {
                                annotRectF = annot2.getRect();
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                            CircleAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, i);
                            CircleAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(tempRectF, tempRectF, i);
                            annotRectF.union(tempRectF);
                            annotRectF.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 10), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 10));
                            CircleAnnotHandler.this.mPdfViewCtrl.refresh(i, AppDmUtil.rectFToRect(annotRectF));
                        }
                    }
                    if (callback != null) {
                        callback.result(null, success);
                    }
                }
            }));
        }
        if (isModifyJni) {
            try {
                DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotModified(annot.getPage(), annot);
            } catch (PDFException e2) {
                if (e2.getLastError() == PDFError.OOM.getCode()) {
                    this.mPdfViewCtrl.recoverForOOM();
                    return;
                }
                return;
            }
        }
        this.mIsModify = true;
        if (!isModifyJni) {
            annot.setBorderColor((long) color);
            ((Circle) annot).setOpacity(((float) opacity) / 255.0f);
            BorderInfo borderInfo = new BorderInfo();
            borderInfo.setWidth(lineWidth);
            annot.setBorderInfo(borderInfo);
            if (contents != null) {
                annot.setContent(contents);
            } else {
                annot.setContent("");
            }
            annot.setFlags(annot.getFlags());
            annot.move(bbox);
            annot.setModifiedDateTime(AppDmUtil.currentDateToDocumentDate());
            RectF annotRectF = annot.getRect();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                float thickness = thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth());
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(annotRectF, annotRectF, pageIndex);
                annotRectF.inset(((-thickness) - mCtlPtRadius) - mCtlPtDeltyXY, ((-thickness) - mCtlPtRadius) - mCtlPtDeltyXY);
                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(annotRectF));
            }
        }
    }

    private void deleteAnnot(Annot annot, boolean addUndo, Callback result) {
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        try {
            PDFPage page = annot.getPage();
            final RectF viewRect = annot.getRect();
            final int pageIndex = page.getIndex();
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final CircleDeleteUndoItem undoItem = new CircleDeleteUndoItem(this.mPdfViewCtrl);
            undoItem.setCurrentValue(annot);
            undoItem.mPageIndex = pageIndex;
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            final boolean z = addUndo;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CircleEvent(3, undoItem, (Circle) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (z) {
                            DocumentManager.getInstance(CircleAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        }
                        if (CircleAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            CircleAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                            CircleAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                        }
                    }
                    if (callback != null) {
                        callback.result(null, success);
                    }
                }
            }));
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public int getType() {
        return 6;
    }

    public boolean annotCanAnswer(Annot annot) {
        return true;
    }

    public RectF getAnnotBBox(Annot annot) {
        RectF rectF = null;
        try {
            rectF = annot.getRect();
        } catch (PDFException e) {
        }
        return rectF;
    }

    public boolean isHitAnnot(Annot annot, PointF point) {
        try {
            RectF rectF = annot.getRect();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, annot.getPage().getIndex());
            return rectF.contains(point.x, point.y);
        } catch (PDFException e) {
            return false;
        }
    }

    public void onAnnotSelected(Annot annot, boolean needInvalid) {
        mCtlPtRadius = (float) AppDisplay.getInstance(this.mContext).dp2px(mCtlPtRadius);
        mCtlPtDeltyXY = (float) AppDisplay.getInstance(this.mContext).dp2px(mCtlPtDeltyXY);
        try {
            this.mTempLastColor = (int) annot.getBorderColor();
            this.mTempLastOpacity = (int) ((((Circle) annot).getOpacity() * 255.0f) + 0.5f);
            this.mTempLastBBox = annot.getRect();
            this.mTempLastLineWidth = annot.getBorderInfo().getWidth();
            RectF _rect = annot.getRect();
            this.mPageViewRect.set(_rect.left, _rect.top, _rect.right, _rect.bottom);
            int pageIndex = annot.getPage().getIndex();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mPageViewRect, this.mPageViewRect, pageIndex);
            prepareAnnotMenu(annot);
            RectF menuRect = new RectF(this.mPageViewRect);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(menuRect, menuRect, pageIndex);
            this.mAnnotationMenu.show(menuRect);
            preparePropertyBar();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(this.mPageViewRect));
                if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                    this.mBitmapAnnot = annot;
                    return;
                }
                return;
            }
            this.mBitmapAnnot = annot;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean needInvalid) {
        mCtlPtRadius = 5.0f;
        mCtlPtDeltyXY = 20.0f;
        this.mAnnotationMenu.setListener(null);
        this.mAnnotationMenu.dismiss();
        if (this.mIsEditProperty) {
            this.mIsEditProperty = false;
            this.mAnnotationProperty.dismiss();
        }
        if (this.mAnnotationProperty.isShowing()) {
            this.mAnnotationProperty.dismiss();
        }
        try {
            PDFPage page = annot.getPage();
            if (needInvalid && this.mIsModify) {
                if (this.mTempLastColor == ((int) annot.getBorderColor()) && this.mTempLastLineWidth == annot.getBorderInfo().getWidth() && this.mTempLastBBox.equals(annot.getRect()) && this.mTempLastOpacity == ((int) (((Circle) annot).getOpacity() * 255.0f))) {
                    modifyAnnot(page.getIndex(), annot, annot.getRect(), (int) annot.getBorderColor(), (int) (((Circle) annot).getOpacity() * 255.0f), annot.getBorderInfo().getWidth(), annot.getContent(), false, false, null);
                } else {
                    modifyAnnot(page.getIndex(), annot, annot.getRect(), (int) annot.getBorderColor(), (int) (((Circle) annot).getOpacity() * 255.0f), annot.getBorderInfo().getWidth(), annot.getContent(), true, true, null);
                }
            } else if (this.mIsModify) {
                annot.setBorderColor((long) this.mTempLastColor);
                BorderInfo borderInfo = new BorderInfo();
                borderInfo.setWidth(this.mTempLastLineWidth);
                annot.setBorderInfo(borderInfo);
                ((Circle) annot).setOpacity(((float) this.mTempLastOpacity) / 255.0f);
                annot.move(this.mTempLastBBox);
            }
            RectF pdfRect = annot.getRect();
            RectF rectF = new RectF(pdfRect.left, pdfRect.top, pdfRect.right, pdfRect.bottom);
            if (this.mPdfViewCtrl.isPageVisible(page.getIndex()) && needInvalid) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, page.getIndex());
                this.mPdfViewCtrl.refresh(page.getIndex(), AppDmUtil.rectFToRect(rectF));
            }
            this.mBitmapAnnot = null;
            this.mIsModify = false;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void prepareAnnotMenu(final Annot annot) {
        resetAnnotationMenuResource(annot);
        this.mAnnotationMenu.setMenuItems(this.mMenuText);
        this.mAnnotationMenu.setListener(new ClickListener() {
            public void onAMClick(int btType) {
                if (btType == 2) {
                    if (annot == DocumentManager.getInstance(CircleAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                        CircleAnnotHandler.this.deleteAnnot(annot, true, null);
                    }
                } else if (btType == 3) {
                    DocumentManager.getInstance(CircleAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                    UIAnnotReply.showComments(CircleAnnotHandler.this.mContext, CircleAnnotHandler.this.mPdfViewCtrl, CircleAnnotHandler.this.mParent, annot);
                } else if (btType == 4) {
                    DocumentManager.getInstance(CircleAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                    UIAnnotReply.replyToAnnot(CircleAnnotHandler.this.mContext, CircleAnnotHandler.this.mPdfViewCtrl, CircleAnnotHandler.this.mParent, annot);
                } else if (btType == 6) {
                    CircleAnnotHandler.this.mAnnotationProperty.show(CircleAnnotHandler.this.mDocViewerBBox, false);
                    CircleAnnotHandler.this.mAnnotationMenu.dismiss();
                }
            }
        });
    }

    private void preparePropertyBar() {
        int[] colors = new int[PropertyBar.PB_COLORS_CIRCLE.length];
        System.arraycopy(PropertyBar.PB_COLORS_CIRCLE, 0, colors, 0, colors.length);
        colors[0] = PropertyBar.PB_COLORS_CIRCLE[0];
        this.mAnnotationProperty.setColors(colors);
        try {
            this.mAnnotationProperty.setProperty(1, (int) DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot().getBorderColor());
            this.mAnnotationProperty.setProperty(2, AppDmUtil.opacity255To100((int) ((((Circle) DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()).getOpacity() * 255.0f) + 0.5f)));
            this.mAnnotationProperty.setProperty(4, DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot().getBorderInfo().getWidth());
        } catch (PDFException e) {
            e.printStackTrace();
        }
        this.mAnnotationProperty.setArrowVisible(false);
        this.mAnnotationProperty.reset(getSupportedProperties());
        this.mAnnotationProperty.setPropertyChangeListener(this.mPropertyChangeListener);
    }

    private long getSupportedProperties() {
        return 7;
    }

    public void addAnnot(int pageIndex, AnnotContent contentSupplier, boolean addUndo, Callback result) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            final Annot annot = page.addAnnot(5, contentSupplier.getBBox());
            final CircleAddUndoItem undoItem = new CircleAddUndoItem(this.mPdfViewCtrl);
            undoItem.mPageIndex = pageIndex;
            undoItem.mColor = (long) contentSupplier.getColor();
            undoItem.mNM = contentSupplier.getNM();
            undoItem.mOpacity = ((float) contentSupplier.getOpacity()) / 255.0f;
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mBorderStyle = 0;
            undoItem.mLineWidth = contentSupplier.getLineWidth();
            undoItem.mFlags = 4;
            undoItem.mSubject = "Oval";
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            final boolean z = addUndo;
            final int i = pageIndex;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CircleEvent(1, undoItem, (Circle) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(CircleAnnotHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (z) {
                            DocumentManager.getInstance(CircleAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        }
                        if (CircleAnnotHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                            try {
                                RectF viewRect = annot.getRect();
                                CircleAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, i);
                                Rect rect = new Rect();
                                viewRect.roundOut(rect);
                                rect.inset(-10, -10);
                                CircleAnnotHandler.this.mPdfViewCtrl.refresh(i, rect);
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (callback != null) {
                        callback.result(null, true);
                    }
                }
            }));
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        deleteAnnot(annot, addUndo, result);
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e, Annot annot) {
        PointF pointF = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pointF, pointF, pageIndex);
        float evX = pointF.x;
        float evY = pointF.y;
        RectF pageViewBBox;
        switch (e.getAction()) {
            case 0:
                try {
                    if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && pageIndex == annot.getPage().getIndex()) {
                        this.mThickness = thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth());
                        pageViewBBox = annot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewBBox, pageViewBBox, pageIndex);
                        RectF pdfRect = annot.getRect();
                        this.mPageViewRect.set(pdfRect.left, pdfRect.top, pdfRect.right, pdfRect.bottom);
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mPageViewRect, this.mPageViewRect, pageIndex);
                        this.mPageViewRect.inset(this.mThickness / 2.0f, this.mThickness / 2.0f);
                        this.mCurrentCtr = isTouchControlPoint(pageViewBBox, evX, evY);
                        this.mDownPoint.set(evX, evY);
                        this.mLastPoint.set(evX, evY);
                        this.mDocViewerPt.set(e.getX(), e.getY());
                        if (this.mCurrentCtr == 1) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 1;
                            return true;
                        } else if (this.mCurrentCtr == 2) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 2;
                            return true;
                        } else if (this.mCurrentCtr == 3) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 3;
                            return true;
                        } else if (this.mCurrentCtr == 4) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 4;
                            return true;
                        } else if (this.mCurrentCtr == 5) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 5;
                            return true;
                        } else if (this.mCurrentCtr == 6) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 6;
                            return true;
                        } else if (this.mCurrentCtr == 7) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 7;
                            return true;
                        } else if (this.mCurrentCtr == 8) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 8;
                            return true;
                        } else if (isHitAnnot(annot, pointF)) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 9;
                            return true;
                        }
                    }
                    return false;
                } catch (PDFException e1) {
                    if (e1.getLastError() == PDFError.OOM.getCode()) {
                        this.mPdfViewCtrl.recoverForOOM();
                    }
                    return false;
                }
            case 1:
            case 3:
                if (this.mTouchCaptured && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && pageIndex == annot.getPage().getIndex()) {
                    RectF pageViewRect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRect, pageViewRect, pageIndex);
                    pageViewRect.inset(this.mThickness / 2.0f, this.mThickness / 2.0f);
                    switch (this.mLastOper) {
                        case 1:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(this.mLastPoint.x, this.mLastPoint.y, pageViewRect.right, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 2:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, this.mLastPoint.y, pageViewRect.right, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 3:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, this.mLastPoint.y, this.mLastPoint.x, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 4:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, pageViewRect.top, this.mLastPoint.x, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 5:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, pageViewRect.top, this.mLastPoint.x, this.mLastPoint.y);
                                break;
                            }
                            break;
                        case 6:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, pageViewRect.top, pageViewRect.right, this.mLastPoint.y);
                                break;
                            }
                            break;
                        case 7:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(this.mLastPoint.x, pageViewRect.top, pageViewRect.right, this.mLastPoint.y);
                                break;
                            }
                            break;
                        case 8:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(this.mLastPoint.x, pageViewRect.top, pageViewRect.right, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 9:
                            this.mPageDrawRect.set(pageViewRect);
                            this.mPageDrawRect.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                            break;
                    }
                    RectF rectF;
                    float _lineWidth;
                    if (this.mLastOper == -1 || this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                        rectF = new RectF(this.mPageDrawRect.left, this.mPageDrawRect.top, this.mPageDrawRect.right, this.mPageDrawRect.bottom);
                        _lineWidth = annot.getBorderInfo().getWidth();
                        rectF.inset((-thicknessOnPageView(pageIndex, _lineWidth)) / 2.0f, (-thicknessOnPageView(pageIndex, _lineWidth)) / 2.0f);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                        if (this.mAnnotationMenu.isShowing()) {
                            this.mAnnotationMenu.update(rectF);
                        } else {
                            this.mAnnotationMenu.show(rectF);
                        }
                    } else {
                        rectF = new RectF(this.mPageDrawRect.left, this.mPageDrawRect.top, this.mPageDrawRect.right, this.mPageDrawRect.bottom);
                        _lineWidth = annot.getBorderInfo().getWidth();
                        rectF.inset((-thicknessOnPageView(pageIndex, _lineWidth)) / 2.0f, (-thicknessOnPageView(pageIndex, _lineWidth)) / 2.0f);
                        RectF bboxRect = new RectF();
                        this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, bboxRect, pageIndex);
                        modifyAnnot(pageIndex, annot, bboxRect, (int) annot.getBorderColor(), (int) (((Circle) annot).getOpacity() * 255.0f), _lineWidth, annot.getContent(), false, false, null);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                        if (!this.mIsEditProperty) {
                            if (this.mAnnotationMenu.isShowing()) {
                                this.mAnnotationMenu.update(rectF);
                            } else {
                                this.mAnnotationMenu.show(rectF);
                            }
                        }
                    }
                    this.mTouchCaptured = false;
                    this.mDownPoint.set(0.0f, 0.0f);
                    this.mLastPoint.set(0.0f, 0.0f);
                    this.mLastOper = -1;
                    this.mCurrentCtr = -1;
                    return true;
                }
                this.mTouchCaptured = false;
                this.mDownPoint.set(0.0f, 0.0f);
                this.mLastPoint.set(0.0f, 0.0f);
                this.mLastOper = -1;
                this.mCurrentCtr = -1;
                this.mTouchCaptured = false;
                return false;
            case 2:
                if (pageIndex != annot.getPage().getIndex() || !this.mTouchCaptured || annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                    return false;
                }
                if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                    pageViewBBox = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewBBox, pageViewBBox, pageIndex);
                    float deltaXY = (mCtlPtLineWidth + (mCtlPtRadius * 2.0f)) + 2.0f;
                    PointF adjustXY;
                    switch (this.mLastOper) {
                        case 1:
                            if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mLastPoint.x, this.mLastPoint.y, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(evX, evY, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotationMenu.isShowing()) {
                                    this.mAnnotationMenu.dismiss();
                                    this.mAnnotationMenu.update(this.mAnnotMenuRect);
                                }
                                if (this.mIsEditProperty) {
                                    this.mAnnotationProperty.dismiss();
                                }
                                this.mLastPoint.set(evX, evY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 2:
                            if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mLastPoint.y, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, evY, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotationMenu.isShowing()) {
                                    this.mAnnotationMenu.dismiss();
                                    this.mAnnotationMenu.update(this.mAnnotMenuRect);
                                }
                                if (this.mIsEditProperty) {
                                    this.mAnnotationProperty.dismiss();
                                }
                                this.mLastPoint.set(evX, evY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 3:
                            if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mLastPoint.y, this.mLastPoint.x, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, evY, evX, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotationMenu.isShowing()) {
                                    this.mAnnotationMenu.dismiss();
                                    this.mAnnotationMenu.update(this.mAnnotMenuRect);
                                }
                                if (this.mIsEditProperty) {
                                    this.mAnnotationProperty.dismiss();
                                }
                                this.mLastPoint.set(evX, evY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 4:
                            if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mPageViewRect.top, this.mLastPoint.x, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, this.mPageViewRect.top, evX, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotationMenu.isShowing()) {
                                    this.mAnnotationMenu.dismiss();
                                    this.mAnnotationMenu.update(this.mAnnotMenuRect);
                                }
                                if (this.mIsEditProperty) {
                                    this.mAnnotationProperty.dismiss();
                                }
                                this.mLastPoint.set(evX, evY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 5:
                            if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mPageViewRect.top, this.mLastPoint.x, this.mLastPoint.y);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, this.mPageViewRect.top, evX, evY);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotationMenu.isShowing()) {
                                    this.mAnnotationMenu.dismiss();
                                    this.mAnnotationMenu.update(this.mAnnotMenuRect);
                                }
                                if (this.mIsEditProperty) {
                                    this.mAnnotationProperty.dismiss();
                                }
                                this.mLastPoint.set(evX, evY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 6:
                            if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mPageViewRect.top, this.mPageViewRect.right, this.mLastPoint.y);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, this.mPageViewRect.top, this.mPageViewRect.right, evY);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotationMenu.isShowing()) {
                                    this.mAnnotationMenu.dismiss();
                                    this.mAnnotationMenu.update(this.mAnnotMenuRect);
                                }
                                if (this.mIsEditProperty) {
                                    this.mAnnotationProperty.dismiss();
                                }
                                this.mLastPoint.set(evX, evY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 7:
                            if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mLastPoint.x, this.mPageViewRect.top, this.mPageViewRect.right, this.mLastPoint.y);
                                this.mAnnotMenuRect.set(evX, this.mPageViewRect.top, this.mPageViewRect.right, evY);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotationMenu.isShowing()) {
                                    this.mAnnotationMenu.dismiss();
                                    this.mAnnotationMenu.update(this.mAnnotMenuRect);
                                }
                                if (this.mIsEditProperty) {
                                    this.mAnnotationProperty.dismiss();
                                }
                                this.mLastPoint.set(evX, evY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 8:
                            if (!(evX == this.mLastPoint.x || evY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mLastPoint.x, this.mPageViewRect.top, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(evX, this.mPageViewRect.top, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotationMenu.isShowing()) {
                                    this.mAnnotationMenu.dismiss();
                                    this.mAnnotationMenu.update(this.mAnnotMenuRect);
                                }
                                if (this.mIsEditProperty) {
                                    this.mAnnotationProperty.dismiss();
                                }
                                this.mLastPoint.set(evX, evY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 9:
                            this.mInvalidateRect.set(pageViewBBox);
                            this.mAnnotMenuRect.set(pageViewBBox);
                            this.mInvalidateRect.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                            this.mAnnotMenuRect.offset(evX - this.mDownPoint.x, evY - this.mDownPoint.y);
                            adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                            this.mInvalidateRect.union(this.mAnnotMenuRect);
                            this.mInvalidateRect.inset((-deltaXY) - mCtlPtDeltyXY, (-deltaXY) - mCtlPtDeltyXY);
                            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                            this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                            if (this.mAnnotationMenu.isShowing()) {
                                this.mAnnotationMenu.dismiss();
                                this.mAnnotationMenu.update(this.mAnnotMenuRect);
                            }
                            if (this.mIsEditProperty) {
                                this.mAnnotationProperty.dismiss();
                            }
                            this.mLastPoint.set(evX, evY);
                            this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                            break;
                    }
                }
                return true;
            default:
                return false;
        }
        if (e1.getLastError() == PDFError.OOM.getCode()) {
            this.mPdfViewCtrl.recoverForOOM();
        }
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return onSingleTapOrLongPress(pageIndex, motionEvent, annot);
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return onSingleTapOrLongPress(pageIndex, motionEvent, annot);
    }

    private boolean onSingleTapOrLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        try {
            this.mDocViewerPt.set(motionEvent.getX(), motionEvent.getY());
            PointF point = new PointF(motionEvent.getX(), motionEvent.getY());
            this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
            this.mThickness = thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth());
            RectF _rect = annot.getRect();
            this.mPageViewRect.set(_rect.left, _rect.top, _rect.right, _rect.bottom);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mPageViewRect, this.mPageViewRect, pageIndex);
            this.mPageViewRect.inset(this.mThickness / 2.0f, this.mThickness / 2.0f);
            if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
                return true;
            } else if (pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, point)) {
                return true;
            } else {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                return true;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && (annot instanceof Circle)) {
            try {
                int annotPageIndex = annot.getPage().getIndex();
                if (this.mBitmapAnnot == annot && annotPageIndex == pageIndex) {
                    canvas.save();
                    canvas.setDrawFilter(this.mDrawFilter);
                    float thickness = thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth());
                    this.mPathPaint.setColor((int) annot.getBorderColor());
                    this.mPathPaint.setAlpha((int) (((Circle) annot).getOpacity() * 255.0f));
                    this.mPathPaint.setStrokeWidth(thickness);
                    RectF _rect = annot.getRect();
                    this.mViewDrawRectInOnDraw.set(_rect.left, _rect.top, _rect.right, _rect.bottom);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mViewDrawRectInOnDraw, this.mViewDrawRectInOnDraw, pageIndex);
                    this.mViewDrawRectInOnDraw.inset(thickness / 2.0f, thickness / 2.0f);
                    if (this.mLastOper == 1) {
                        this.mBBoxInOnDraw.set(this.mLastPoint.x, this.mLastPoint.y, this.mViewDrawRectInOnDraw.right, this.mViewDrawRectInOnDraw.bottom);
                    } else if (this.mLastOper == 2) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mLastPoint.y, this.mViewDrawRectInOnDraw.right, this.mViewDrawRectInOnDraw.bottom);
                    } else if (this.mLastOper == 3) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mLastPoint.y, this.mLastPoint.x, this.mViewDrawRectInOnDraw.bottom);
                    } else if (this.mLastOper == 4) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mViewDrawRectInOnDraw.top, this.mLastPoint.x, this.mViewDrawRectInOnDraw.bottom);
                    } else if (this.mLastOper == 5) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mViewDrawRectInOnDraw.top, this.mLastPoint.x, this.mLastPoint.y);
                    } else if (this.mLastOper == 6) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mViewDrawRectInOnDraw.top, this.mViewDrawRectInOnDraw.right, this.mLastPoint.y);
                    } else if (this.mLastOper == 7) {
                        this.mBBoxInOnDraw.set(this.mLastPoint.x, this.mViewDrawRectInOnDraw.top, this.mViewDrawRectInOnDraw.right, this.mLastPoint.y);
                    } else if (this.mLastOper == 8) {
                        this.mBBoxInOnDraw.set(this.mLastPoint.x, this.mViewDrawRectInOnDraw.top, this.mViewDrawRectInOnDraw.right, this.mViewDrawRectInOnDraw.bottom);
                    }
                    this.mBBoxInOnDraw.inset((-thickness) / 2.0f, (-thickness) / 2.0f);
                    if (this.mLastOper == 9 || this.mLastOper == -1) {
                        this.mBBoxInOnDraw = annot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mBBoxInOnDraw, this.mBBoxInOnDraw, pageIndex);
                        this.mBBoxInOnDraw.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    }
                    if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        drawControlPoints(canvas, this.mBBoxInOnDraw, (int) annot.getBorderColor());
                        drawControlImaginary(canvas, this.mBBoxInOnDraw, (int) annot.getBorderColor());
                    }
                    this.mBBoxInOnDraw.inset(thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth()) / 2.0f, thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth()) / 2.0f);
                    canvas.drawArc(this.mBBoxInOnDraw, 0.0f, 360.0f, false, this.mPathPaint);
                    canvas.restore();
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                int annotPageIndex = curAnnot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(annotPageIndex)) {
                    float thickness = thicknessOnPageView(annotPageIndex, curAnnot.getBorderInfo().getWidth());
                    RectF _rect = curAnnot.getRect();
                    this.mViewDrawRect.set(_rect.left, _rect.top, _rect.right, _rect.bottom);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mViewDrawRect, this.mViewDrawRect, annotPageIndex);
                    this.mViewDrawRect.inset(thickness / 2.0f, thickness / 2.0f);
                    if (this.mLastOper == 1) {
                        this.mDocViewerBBox.left = this.mLastPoint.x;
                        this.mDocViewerBBox.top = this.mLastPoint.y;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    } else if (this.mLastOper == 2) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mLastPoint.y;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    } else if (this.mLastOper == 3) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mLastPoint.y;
                        this.mDocViewerBBox.right = this.mLastPoint.x;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    } else if (this.mLastOper == 4) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mLastPoint.x;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    } else if (this.mLastOper == 5) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mLastPoint.x;
                        this.mDocViewerBBox.bottom = this.mLastPoint.y;
                    } else if (this.mLastOper == 6) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mLastPoint.y;
                    } else if (this.mLastOper == 7) {
                        this.mDocViewerBBox.left = this.mLastPoint.x;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mLastPoint.y;
                    } else if (this.mLastOper == 8) {
                        this.mDocViewerBBox.left = this.mLastPoint.x;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    }
                    this.mDocViewerBBox.inset((-thickness) / 2.0f, (-thickness) / 2.0f);
                    if (this.mLastOper == 9 || this.mLastOper == -1) {
                        this.mDocViewerBBox = curAnnot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mDocViewerBBox, this.mDocViewerBBox, annotPageIndex);
                        this.mDocViewerBBox.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    }
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mDocViewerBBox, this.mDocViewerBBox, annotPageIndex);
                    this.mAnnotationMenu.update(this.mDocViewerBBox);
                    if (this.mAnnotationProperty.isShowing()) {
                        this.mAnnotationProperty.update(this.mDocViewerBBox);
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawControlImaginary(Canvas canvas, RectF rectBBox, int color) {
        PointF[] ctlPts = calculateControlPoints(rectBBox);
        this.mFrmPaint.setStrokeWidth(mCtlPtLineWidth);
        this.mFrmPaint.setColor(color);
        this.mImaginaryPath.reset();
        pathAddLine(this.mImaginaryPath, mCtlPtRadius + ctlPts[0].x, ctlPts[0].y, ctlPts[1].x - mCtlPtRadius, ctlPts[1].y);
        pathAddLine(this.mImaginaryPath, mCtlPtRadius + ctlPts[1].x, ctlPts[1].y, ctlPts[2].x - mCtlPtRadius, ctlPts[2].y);
        pathAddLine(this.mImaginaryPath, ctlPts[2].x, mCtlPtRadius + ctlPts[2].y, ctlPts[3].x, ctlPts[3].y - mCtlPtRadius);
        pathAddLine(this.mImaginaryPath, ctlPts[3].x, mCtlPtRadius + ctlPts[3].y, ctlPts[4].x, ctlPts[4].y - mCtlPtRadius);
        pathAddLine(this.mImaginaryPath, ctlPts[4].x - mCtlPtRadius, ctlPts[4].y, mCtlPtRadius + ctlPts[5].x, ctlPts[5].y);
        pathAddLine(this.mImaginaryPath, ctlPts[5].x - mCtlPtRadius, ctlPts[5].y, mCtlPtRadius + ctlPts[6].x, ctlPts[6].y);
        pathAddLine(this.mImaginaryPath, ctlPts[6].x, ctlPts[6].y - mCtlPtRadius, ctlPts[7].x, mCtlPtRadius + ctlPts[7].y);
        pathAddLine(this.mImaginaryPath, ctlPts[7].x, ctlPts[7].y - mCtlPtRadius, ctlPts[0].x, mCtlPtRadius + ctlPts[0].y);
        canvas.drawPath(this.mImaginaryPath, this.mFrmPaint);
    }

    private void pathAddLine(Path path, float start_x, float start_y, float end_x, float end_y) {
        path.moveTo(start_x, start_y);
        path.lineTo(end_x, end_y);
    }

    private void drawControlPoints(Canvas canvas, RectF rectBBox, int color) {
        PointF[] ctlPts = calculateControlPoints(rectBBox);
        this.mCtlPtPaint.setStrokeWidth(mCtlPtLineWidth);
        for (PointF ctlPt : ctlPts) {
            this.mCtlPtPaint.setColor(-1);
            this.mCtlPtPaint.setStyle(Style.FILL);
            canvas.drawCircle(ctlPt.x, ctlPt.y, mCtlPtRadius, this.mCtlPtPaint);
            this.mCtlPtPaint.setColor(color);
            this.mCtlPtPaint.setStyle(Style.STROKE);
            canvas.drawCircle(ctlPt.x, ctlPt.y, mCtlPtRadius, this.mCtlPtPaint);
        }
    }

    private PointF[] calculateControlPoints(RectF rect) {
        rect.sort();
        this.mMapBounds.set(rect);
        this.mMapBounds.inset((-mCtlPtRadius) - (mCtlPtLineWidth / 2.0f), (-mCtlPtRadius) - (mCtlPtLineWidth / 2.0f));
        PointF p1 = new PointF(this.mMapBounds.left, this.mMapBounds.top);
        PointF p2 = new PointF((this.mMapBounds.right + this.mMapBounds.left) / 2.0f, this.mMapBounds.top);
        PointF p3 = new PointF(this.mMapBounds.right, this.mMapBounds.top);
        PointF p4 = new PointF(this.mMapBounds.right, (this.mMapBounds.bottom + this.mMapBounds.top) / 2.0f);
        PointF p5 = new PointF(this.mMapBounds.right, this.mMapBounds.bottom);
        PointF p6 = new PointF((this.mMapBounds.right + this.mMapBounds.left) / 2.0f, this.mMapBounds.bottom);
        PointF p7 = new PointF(this.mMapBounds.left, this.mMapBounds.bottom);
        PointF p8 = new PointF(this.mMapBounds.left, (this.mMapBounds.bottom + this.mMapBounds.top) / 2.0f);
        return new PointF[]{p1, p2, p3, p4, p5, p6, p7, p8};
    }

    private int isTouchControlPoint(RectF rect, float x, float y) {
        PointF[] ctlPts = calculateControlPoints(rect);
        RectF area = new RectF();
        int ret = -1;
        for (int i = 0; i < ctlPts.length; i++) {
            area.set(ctlPts[i].x, ctlPts[i].y, ctlPts[i].x, ctlPts[i].y);
            area.inset(-mCtlPtTouchExt, -mCtlPtTouchExt);
            if (area.contains(x, y)) {
                ret = i + 1;
            }
        }
        return ret;
    }

    private PointF adjustScalePointF(int pageIndex, RectF rectF, float dxy) {
        float adjustx = 0.0f;
        float adjusty = 0.0f;
        if (this.mLastOper != 9) {
            rectF.inset((-this.mThickness) / 2.0f, (-this.mThickness) / 2.0f);
        }
        if (((float) ((int) rectF.left)) < dxy) {
            adjustx = (-rectF.left) + dxy;
            rectF.left = dxy;
        }
        if (((float) ((int) rectF.top)) < dxy) {
            adjusty = (-rectF.top) + dxy;
            rectF.top = dxy;
        }
        if (((float) ((int) rectF.right)) > ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - dxy) {
            adjustx = (((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - rectF.right) - dxy;
            rectF.right = ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - dxy;
        }
        if (((float) ((int) rectF.bottom)) > ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - dxy) {
            adjusty = (((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - rectF.bottom) - dxy;
            rectF.bottom = ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - dxy;
        }
        this.mAdjustPointF.set(adjustx, adjusty);
        return this.mAdjustPointF;
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        try {
            int pageIndex = annot.getPage().getIndex();
            RectF bbox = annot.getRect();
            int color = (int) annot.getBorderColor();
            float lineWidth = annot.getBorderInfo().getWidth();
            int opacity = (int) (((Circle) annot).getOpacity() * 255.0f);
            String contents = annot.getContent();
            this.mTempLastColor = (int) annot.getBorderColor();
            this.mTempLastOpacity = (int) (((Circle) annot).getOpacity() * 255.0f);
            this.mTempLastLineWidth = annot.getBorderInfo().getWidth();
            this.mTempLastBBox = annot.getRect();
            if (content.getBBox() != null) {
                bbox = content.getBBox();
            }
            if (content.getColor() != 0) {
                color = content.getColor();
            }
            if (content.getLineWidth() != 0.0f) {
                lineWidth = content.getLineWidth();
            }
            if (content.getOpacity() != 0) {
                opacity = content.getOpacity();
            }
            if (content.getContents() != null) {
                contents = content.getContents();
            }
            modifyAnnot(pageIndex, annot, bbox, color, opacity, lineWidth, contents, true, addUndo, result);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void removePropertyBarListener() {
        this.mPropertyChangeListener = null;
    }
}
