package com.foxit.uiextensions.annots.note;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.Note;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;

class NoteAnnotHandler implements AnnotHandler {
    private AnnotMenu mAnnotMenu;
    private int mBBoxSpace;
    private Annot mBitmapAnnot;
    private Button mCancel;
    private Context mContext;
    private TextView mDialog_title;
    private AppDisplay mDisplay;
    private RectF mDocViewerRectF = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private PointF mDownPoint;
    private EditText mET_Content;
    private boolean mIsEditProperty;
    private boolean mIsModify;
    private PointF mLastPoint;
    private ArrayList<Integer> mMenuItems;
    private Paint mPaint;
    private Paint mPaintOut;
    private ViewGroup mParentView;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyBar mPropertyBar;
    private PropertyChangeListener mPropertyChangeListener;
    private Button mSave;
    private NoteToolHandler mToolHandler;
    private boolean mTouchCaptured = false;
    private RectF tempUndoBBox;
    private int tempUndoColor;
    private String tempUndoContents;
    private String tempUndoIconType;
    private float tempUndoOpacity;

    NoteAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
        this.mContext = context;
        this.mParentView = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mDisplay = new AppDisplay(context);
        AppAnnotUtil annotUtil = new AppAnnotUtil(this.mContext);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mPaintOut = new Paint();
        this.mPaintOut.setAntiAlias(true);
        this.mPaintOut.setStyle(Style.STROKE);
        this.mPaintOut.setPathEffect(AppAnnotUtil.getAnnotBBoxPathEffect());
        this.mPaintOut.setStrokeWidth(annotUtil.getAnnotBBoxStrokeWidth());
        this.mDownPoint = new PointF();
        this.mLastPoint = new PointF();
        this.mMenuItems = new ArrayList();
        this.mPropertyBar = new PropertyBarImpl(context, pdfViewCtrl, this.mParentView);
        this.mAnnotMenu = new AnnotMenuImpl(this.mContext, this.mParentView);
        this.mBBoxSpace = AppAnnotUtil.getAnnotBBoxSpace();
        this.mBitmapAnnot = null;
    }

    public PropertyBar getPropertyBar() {
        return this.mPropertyBar;
    }

    public AnnotMenu getAnnotMenu() {
        return this.mAnnotMenu;
    }

    public int getType() {
        return 1;
    }

    public boolean annotCanAnswer(Annot annot) {
        return true;
    }

    public RectF getAnnotBBox(Annot annot) {
        try {
            return annot.getRect();
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isHitAnnot(Annot annot, PointF point) {
        RectF rectF = getAnnotBBox(annot);
        if (this.mPdfViewCtrl != null) {
            try {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, annot.getPage().getIndex());
            } catch (Exception e) {
                e.printStackTrace();
            }
            rectF.inset(-10.0f, -10.0f);
        }
        return rectF.contains(point.x, point.y);
    }

    @SuppressLint({"NewApi"})
    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent, Annot annot) {
        int action = motionEvent.getActionMasked();
        PointF pointF = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF point = new PointF();
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pointF, point, pageIndex);
        pointF = new PointF(point.x, point.y);
        try {
            float envX = point.x;
            float envY = point.y;
            RectF pageViewRectF;
            RectF rectF;
            float f;
            float f2;
            switch (action) {
                case 0:
                    if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        try {
                            if (pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pointF)) {
                                this.mDownPoint.set(envX, envY);
                                this.mLastPoint.set(envX, envY);
                                this.mTouchCaptured = true;
                                return true;
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    return false;
                case 1:
                    if (this.mTouchCaptured && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && annot.getPage().getIndex() == pageIndex && DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                        RectF pageRectF = annot.getRect();
                        pageViewRectF = new RectF();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageRectF, pageViewRectF, pageIndex);
                        rectF = new RectF(pageViewRectF);
                        rectF = new RectF(pageViewRectF);
                        rectF.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                        rectF.offset(envX - this.mDownPoint.x, envY - this.mDownPoint.y);
                        f = 0.0f;
                        f2 = 0.0f;
                        if (rectF.left < 0.0f) {
                            f = -rectF.left;
                        }
                        if (rectF.top < 0.0f) {
                            f2 = -rectF.top;
                        }
                        if (rectF.right > ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex))) {
                            f = ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - rectF.right;
                        }
                        if (rectF.bottom > ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex))) {
                            f2 = ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - rectF.bottom;
                        }
                        rectF.offset(f, f2);
                        rectF.union(rectF);
                        rectF.inset((float) ((-this.mBBoxSpace) - 3), (float) ((-this.mBBoxSpace) - 3));
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                        this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rectF));
                        rectF = new RectF(rectF);
                        RectF canvasRectF = new RectF();
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, canvasRectF, pageIndex);
                        if (this.mIsEditProperty) {
                            if (this.mPropertyBar.isShowing()) {
                                this.mPropertyBar.update(canvasRectF);
                            } else {
                                this.mPropertyBar.show(canvasRectF, false);
                            }
                        } else if (this.mAnnotMenu.isShowing()) {
                            this.mAnnotMenu.update(canvasRectF);
                        } else {
                            this.mAnnotMenu.show(canvasRectF);
                        }
                        RectF pageRect = new RectF();
                        this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, pageRect, pageIndex);
                        if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                            modifyAnnot(annot, (int) annot.getBorderColor(), ((Note) annot).getOpacity(), ((Note) annot).getIconName(), pageRect, annot.getContent(), false);
                        }
                        this.mTouchCaptured = false;
                        this.mDownPoint.set(0.0f, 0.0f);
                        this.mLastPoint.set(0.0f, 0.0f);
                        return true;
                    }
                    this.mTouchCaptured = false;
                    this.mDownPoint.set(0.0f, 0.0f);
                    this.mLastPoint.set(0.0f, 0.0f);
                    return false;
                case 2:
                    try {
                        if (this.mTouchCaptured && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && pageIndex == annot.getPage().getIndex() && DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                            if (!(envX == this.mLastPoint.x && envY == this.mLastPoint.y)) {
                                pageViewRectF = annot.getRect();
                                this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRectF, pageViewRectF, pageIndex);
                                rectF = new RectF(pageViewRectF);
                                rectF = new RectF(pageViewRectF);
                                rectF.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                                rectF.offset(envX - this.mDownPoint.x, envY - this.mDownPoint.y);
                                f = 0.0f;
                                f2 = 0.0f;
                                if (rectF.left < 0.0f) {
                                    f = -rectF.left;
                                }
                                if (rectF.top < 0.0f) {
                                    f2 = -rectF.top;
                                }
                                if (rectF.right > ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex))) {
                                    f = ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - rectF.right;
                                }
                                if (rectF.bottom > ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex))) {
                                    f2 = ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - rectF.bottom;
                                }
                                rectF.offset(f, f2);
                                rectF.union(rectF);
                                rectF.inset((float) ((-this.mBBoxSpace) - 3), (float) ((-this.mBBoxSpace) - 3));
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rectF));
                                rectF = new RectF(rectF);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(rectF);
                                }
                                if (this.mIsEditProperty) {
                                    this.mPropertyBar.dismiss();
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(f, f2);
                            }
                            return true;
                        }
                    } catch (Exception e12) {
                        e12.printStackTrace();
                    }
                    return false;
                case 3:
                    this.mTouchCaptured = false;
                    this.mDownPoint.set(0.0f, 0.0f);
                    this.mLastPoint.set(0.0f, 0.0f);
                    return false;
                default:
                    return false;
            }
        } catch (PDFException e13) {
            if (e13.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
            return true;
        }
        if (e13.getLastError() == PDFError.OOM.getCode()) {
            this.mPdfViewCtrl.recoverForOOM();
        }
        return true;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && (annot instanceof Note) && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                int index = annot.getPage().getIndex();
                if (this.mBitmapAnnot == annot && index == pageIndex) {
                    canvas.save();
                    RectF frameRectF = new RectF();
                    RectF rect2 = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect2, rect2, annot.getPage().getIndex());
                    rect2.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    this.mPaint.setStyle(Style.FILL);
                    this.mPaint.setColor(AppDmUtil.calColorByMultiply((int) annot.getBorderColor(), (int) ((((Note) annot).getOpacity() * 255.0f) + 0.5f)));
                    canvas.drawPath(NoteUtil.GetPathStringByType(((Note) annot).getIconName(), rect2), this.mPaint);
                    this.mPaint.setStyle(Style.STROKE);
                    this.mPaint.setStrokeWidth(LineWidth2PageView(pageIndex, 0.6f));
                    this.mPaint.setARGB((int) ((((Note) annot).getOpacity() * 255.0f) + 0.5f), 91, 91, 163);
                    canvas.drawPath(NoteUtil.GetPathStringByType(((Note) annot).getIconName(), rect2), this.mPaint);
                    frameRectF.set(rect2.left - ((float) this.mBBoxSpace), rect2.top - ((float) this.mBBoxSpace), rect2.right + ((float) this.mBBoxSpace), rect2.bottom + ((float) this.mBBoxSpace));
                    this.mPaintOut.setColor((int) (annot.getBorderColor() | -16777216));
                    canvas.drawRect(frameRectF, this.mPaintOut);
                    canvas.restore();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private float LineWidth2PageView(int pageIndex, float linewidth) {
        RectF rectF = new RectF(0.0f, 0.0f, linewidth, linewidth);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
        return Math.abs(rectF.width());
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        PointF pageViewPt = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        try {
            if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
            } else if (!(pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt))) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
        } catch (PDFException e1) {
            e1.printStackTrace();
        }
        return true;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        if (AppUtil.isFastDoubleClick()) {
            return true;
        }
        PointF pageViewPt = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        try {
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                if (!(pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt))) {
                    DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                }
            } else if (annot == null) {
                return false;
            } else {
                this.tempUndoColor = (int) annot.getBorderColor();
                this.tempUndoOpacity = ((Note) annot).getOpacity();
                this.tempUndoIconType = ((Note) annot).getIconName();
                this.tempUndoBBox = annot.getRect();
                this.tempUndoContents = annot.getContent();
                showDialog(annot);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return true;
    }

    public void onAnnotSelected(final Annot annot, boolean needInvalid) {
        try {
            this.tempUndoColor = (int) annot.getBorderColor();
            this.tempUndoOpacity = ((Note) annot).getOpacity();
            this.tempUndoIconType = ((Note) annot).getIconName();
            this.tempUndoBBox = annot.getRect();
            this.tempUndoContents = annot.getContent();
            this.mBitmapAnnot = annot;
            this.mAnnotMenu.dismiss();
            this.mMenuItems.clear();
            if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                this.mMenuItems.add(Integer.valueOf(6));
                this.mMenuItems.add(Integer.valueOf(3));
                this.mMenuItems.add(Integer.valueOf(4));
                this.mMenuItems.add(Integer.valueOf(2));
            } else {
                this.mMenuItems.add(Integer.valueOf(3));
            }
            this.mAnnotMenu.setMenuItems(this.mMenuItems);
            this.mAnnotMenu.setListener(new ClickListener() {
                public void onAMClick(int btType) {
                    NoteAnnotHandler.this.mAnnotMenu.dismiss();
                    if (btType == 3) {
                        DocumentManager.getInstance(NoteAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.showComments(NoteAnnotHandler.this.mContext, NoteAnnotHandler.this.mPdfViewCtrl, NoteAnnotHandler.this.mParentView, annot);
                    } else if (btType == 4) {
                        DocumentManager.getInstance(NoteAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.replyToAnnot(NoteAnnotHandler.this.mContext, NoteAnnotHandler.this.mPdfViewCtrl, NoteAnnotHandler.this.mParentView, annot);
                    } else if (btType == 2) {
                        NoteAnnotHandler.this.delAnnot(NoteAnnotHandler.this.mPdfViewCtrl, annot, true, null);
                    } else if (btType == 6) {
                        NoteAnnotHandler.this.mIsEditProperty = true;
                        int[] colors = new int[PropertyBar.PB_COLORS_TEXT.length];
                        System.arraycopy(PropertyBar.PB_COLORS_TEXT, 0, colors, 0, colors.length);
                        colors[0] = PropertyBar.PB_COLORS_TEXT[0];
                        NoteAnnotHandler.this.mPropertyBar.setColors(colors);
                        try {
                            NoteAnnotHandler.this.mPropertyBar.setProperty(1, (int) annot.getBorderColor());
                            NoteAnnotHandler.this.mPropertyBar.setProperty(2, AppDmUtil.opacity255To100((int) ((((Note) annot).getOpacity() * 255.0f) + 0.5f)));
                            NoteAnnotHandler.this.mPropertyBar.setProperty(64, ((Note) annot).getIconName());
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                        NoteAnnotHandler.this.mPropertyBar.setPropertyChangeListener(NoteAnnotHandler.this.mPropertyChangeListener);
                        NoteAnnotHandler.this.mPropertyBar.setArrowVisible(false);
                        NoteAnnotHandler.this.mPropertyBar.reset(67);
                        NoteAnnotHandler.this.mPropertyBar.show(NoteAnnotHandler.this.mDocViewerRectF, false);
                    }
                }
            });
            RectF viewRect = new RectF(annot.getRect());
            RectF modifyRectF = new RectF(viewRect);
            int pageIndex = annot.getPage().getIndex();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(viewRect, viewRect, pageIndex);
            this.mAnnotMenu.show(viewRect);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(modifyRectF, modifyRectF, pageIndex);
            this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(modifyRectF));
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                this.mBitmapAnnot = annot;
            }
            this.mIsModify = false;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean needInvalid) {
        this.mAnnotMenu.dismiss();
        if (this.mIsEditProperty) {
            this.mIsEditProperty = false;
        }
        this.mPropertyBar.dismiss();
        try {
            PDFPage page = annot.getPage();
            RectF pdfRect = annot.getRect();
            RectF viewRect = new RectF(pdfRect.left, pdfRect.top, pdfRect.right, pdfRect.bottom);
            if (this.mIsModify && needInvalid) {
                if (((long) this.tempUndoColor) == annot.getBorderColor() && this.tempUndoOpacity == ((Note) annot).getOpacity() && this.tempUndoBBox.equals(annot.getRect()) && this.tempUndoIconType == ((Note) annot).getIconName()) {
                    modifyAnnot(annot, (int) annot.getBorderColor(), ((Note) annot).getOpacity(), ((Note) annot).getIconName(), annot.getRect(), annot.getContent(), false);
                } else {
                    modifyAnnot(annot, (int) annot.getBorderColor(), ((Note) annot).getOpacity(), ((Note) annot).getIconName(), annot.getRect(), annot.getContent(), true);
                }
            } else if (this.mIsModify) {
                annot.setBorderColor((long) this.tempUndoColor);
                ((Note) annot).setOpacity(this.tempUndoOpacity);
                ((Note) annot).setIconName(this.tempUndoIconType);
                annot.move(this.tempUndoBBox);
                annot.setContent(this.tempUndoContents);
            }
            this.mIsModify = false;
            if (needInvalid) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, page.getIndex());
                this.mPdfViewCtrl.refresh(page.getIndex(), AppDmUtil.rectFToRect(viewRect));
                this.mBitmapAnnot = null;
                return;
            }
            this.mBitmapAnnot = null;
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null) {
            try {
                if (ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), curAnnot.getType()) == this) {
                    if (this.mPdfViewCtrl.isPageVisible(curAnnot.getPage().getIndex())) {
                        RectF bboxRect = curAnnot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(bboxRect, bboxRect, curAnnot.getPage().getIndex());
                        bboxRect.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bboxRect, bboxRect, curAnnot.getPage().getIndex());
                        this.mAnnotMenu.update(bboxRect);
                        this.mDocViewerRectF.set(bboxRect);
                        if (this.mIsEditProperty) {
                            ((PropertyBarImpl) this.mPropertyBar).onConfigurationChanged(this.mDocViewerRectF);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void delAnnot(PDFViewCtrl docView, Annot annot, boolean addUndo, Callback result) {
        DocumentManager documentManager = DocumentManager.getInstance(docView);
        if (annot == documentManager.getCurrentAnnot()) {
            documentManager.setCurrentAnnot(null);
        }
        try {
            PDFPage page = annot.getPage();
            if (page != null) {
                final RectF annotRectF = annot.getRect();
                final int pageIndex = page.getIndex();
                DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
                final NoteDeleteUndoItem undoItem = new NoteDeleteUndoItem(docView);
                undoItem.setCurrentValue(annot);
                undoItem.mIconName = ((Note) annot).getIconName();
                Markup markup = ((Note) annot).getReplyTo();
                if (markup != null) {
                    undoItem.mIsFromReplyModule = true;
                    undoItem.mParentNM = markup.getUniqueID();
                }
                final boolean z = addUndo;
                final PDFViewCtrl pDFViewCtrl = docView;
                final Callback callback = result;
                docView.addTask(new EditAnnotTask(new NoteEvent(3, undoItem, (Note) annot, docView), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            if (z) {
                                DocumentManager.getInstance(pDFViewCtrl).addUndoItem(undoItem);
                            }
                            if (pDFViewCtrl.isPageVisible(pageIndex)) {
                                pDFViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
                                pDFViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(annotRectF));
                            }
                        }
                        if (callback != null) {
                            callback.result(event, success);
                        }
                    }
                }));
            } else if (result != null) {
                result.result(null, false);
            }
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                docView.recoverForOOM();
            }
        }
    }

    public void modifyAnnot(Annot annot, int color, float opacity, String iconType, RectF bbox, String content, boolean isModify) {
        modifyAnnot(annot, color, opacity, iconType, content, bbox, isModify, true, "Note", null);
    }

    @TargetApi(11)
    private void showDialog(final Annot annot) {
        Context context = this.mContext;
        View mView = View.inflate(context, R.layout.rd_note_dialog_edit, null);
        this.mDialog_title = (TextView) mView.findViewById(R.id.rd_note_dialog_edit_title);
        this.mET_Content = (EditText) mView.findViewById(R.id.rd_note_dialog_edit);
        this.mCancel = (Button) mView.findViewById(R.id.rd_note_dialog_edit_cancel);
        this.mSave = (Button) mView.findViewById(R.id.rd_note_dialog_edit_ok);
        mView.setLayoutParams(new LayoutParams(-1, -2));
        final Dialog mDialog = new Dialog(context, R.style.rv_dialog_style);
        mDialog.setContentView(mView, new LayoutParams(this.mDisplay.getUITextEditDialogWidth(), -2));
        this.mET_Content.setMaxLines(10);
        mDialog.getWindow().setFlags(1024, 1024);
        mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dlg_title_bg_4circle_corner_white);
        this.mDialog_title.setText(this.mContext.getResources().getString(R.string.fx_string_note));
        this.mET_Content.setEnabled(true);
        try {
            String content = annot.getContent() != null ? annot.getContent() : "";
            this.mET_Content.setText(content);
            this.mET_Content.setSelection(content.length());
        } catch (PDFException e) {
            e.printStackTrace();
        }
        this.mSave.setEnabled(false);
        this.mSave.setTextColor(this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
        this.mET_Content.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                try {
                    if (NoteAnnotHandler.this.mET_Content.getText().toString().equals(annot.getContent())) {
                        NoteAnnotHandler.this.mSave.setEnabled(false);
                        NoteAnnotHandler.this.mSave.setTextColor(NoteAnnotHandler.this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
                        return;
                    }
                    NoteAnnotHandler.this.mSave.setEnabled(true);
                    NoteAnnotHandler.this.mSave.setTextColor(NoteAnnotHandler.this.mContext.getResources().getColor(R.color.dlg_bt_text_selector));
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
            }
        });
        this.mCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        this.mSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    if (!NoteAnnotHandler.this.mET_Content.getText().toString().equals(annot.getContent())) {
                        NoteAnnotHandler.this.modifyAnnot(annot, (int) annot.getBorderColor(), ((Note) annot).getOpacity(), ((Note) annot).getIconName(), annot.getRect(), NoteAnnotHandler.this.mET_Content.getText().toString(), true);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
                mDialog.dismiss();
            }
        });
        mDialog.show();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            AppUtil.showSoftInput(this.mET_Content);
            return;
        }
        this.mET_Content.setFocusable(false);
        this.mET_Content.setLongClickable(false);
        if (VERSION.SDK_INT > 11) {
            this.mET_Content.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                }
            });
        } else {
            this.mET_Content.setEnabled(false);
        }
    }

    public void onColorValueChanged(int color) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && annot != null) {
            try {
                if (((long) color) != annot.getBorderColor()) {
                    modifyAnnot(annot, color, ((Note) annot).getOpacity(), ((Note) annot).getIconName(), annot.getRect(), ((Note) annot).getContent(), false);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onOpacityValueChanged(int opacity) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (((float) AppDmUtil.opacity100To255(opacity)) != ((Note) annot).getOpacity()) {
                    modifyAnnot(annot, (int) annot.getBorderColor(), ((float) AppDmUtil.opacity100To255(opacity)) / 255.0f, ((Note) annot).getIconName(), annot.getRect(), ((Note) annot).getContent(), false);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onIconTypeChanged(String iconType) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (iconType != ((Note) annot).getIconName()) {
                    modifyAnnot(annot, (int) annot.getBorderColor(), ((Note) annot).getOpacity(), iconType, annot.getRect(), annot.getContent(), false);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        if (this.mToolHandler != null) {
            this.mToolHandler.addAnnot(pageIndex, (NoteAnnotContent) content, addUndo, result);
        } else if (result != null) {
            result.result(null, false);
        }
    }

    public void setToolHandler(NoteToolHandler toolHandler) {
        this.mToolHandler = toolHandler;
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        Note lAnnot = (Note) annot;
        try {
            int pageIndex = annot.getPage().getIndex();
            NoteModifyUndoItem undoItem = new NoteModifyUndoItem(this.mPdfViewCtrl);
            undoItem.setCurrentValue(content);
            undoItem.mPageIndex = pageIndex;
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mIconName = lAnnot.getIconName();
            undoItem.mRedoColor = content.getColor();
            undoItem.mRedoOpacity = ((float) content.getOpacity()) / 255.0f;
            undoItem.mRedoBbox = new RectF(content.getBBox());
            undoItem.mRedoIconName = lAnnot.getIconName();
            undoItem.mRedoContent = content.getContents();
            undoItem.mUndoColor = (int) lAnnot.getBorderColor();
            undoItem.mUndoOpacity = lAnnot.getOpacity();
            undoItem.mUndoBbox = new RectF(lAnnot.getRect());
            undoItem.mUndoContent = lAnnot.getContent();
            undoItem.mUndoIconName = lAnnot.getIconName();
            Markup markup = ((Note) annot).getReplyTo();
            if (markup != null) {
                undoItem.mIsFromReplyModule = true;
                undoItem.mParentNM = markup.getUniqueID();
            }
            modifyAnnot(lAnnot, undoItem, true, addUndo, "", result);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        delAnnot(this.mPdfViewCtrl, annot, addUndo, result);
    }

    protected void modifyAnnot(Annot annot, int color, float opacity, String iconName, String content, RectF rect, boolean isModifyJni, boolean addUndo, String fromType, Callback result) {
        try {
            int pageIndex = annot.getPage().getIndex();
            NoteModifyUndoItem undoItem = new NoteModifyUndoItem(this.mPdfViewCtrl);
            undoItem.setCurrentValue(annot);
            undoItem.mPageIndex = pageIndex;
            undoItem.mBBox = new RectF(rect);
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mColor = (long) color;
            undoItem.mOpacity = opacity;
            undoItem.mIconName = iconName;
            undoItem.mContents = content;
            undoItem.mRedoColor = color;
            undoItem.mRedoOpacity = opacity;
            undoItem.mRedoBbox = new RectF(rect);
            undoItem.mRedoIconName = iconName;
            undoItem.mRedoContent = content;
            undoItem.mUndoColor = this.tempUndoColor;
            undoItem.mUndoOpacity = this.tempUndoOpacity;
            undoItem.mUndoBbox = new RectF(this.tempUndoBBox);
            undoItem.mUndoContent = this.tempUndoContents;
            undoItem.mUndoIconName = this.tempUndoIconType;
            Markup markup = ((Note) annot).getReplyTo();
            if (markup != null) {
                undoItem.mIsFromReplyModule = true;
                undoItem.mParentNM = markup.getUniqueID();
            }
            modifyAnnot(annot, undoItem, isModifyJni, addUndo, fromType, result);
        } catch (PDFException e) {
        }
    }

    protected void modifyAnnot(Annot annot, NoteModifyUndoItem undoItem, boolean isModifyJni, boolean addUndo, String fromType, Callback result) {
        try {
            final int pageIndex = annot.getPage().getIndex();
            if (isModifyJni) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(addUndo);
                NoteEvent event = new NoteEvent(2, undoItem, (Note) annot, this.mPdfViewCtrl);
                final boolean z = addUndo;
                final NoteModifyUndoItem noteModifyUndoItem = undoItem;
                final Annot annot2 = annot;
                final String str = fromType;
                final Callback callback = result;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(event, new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            if (z) {
                                DocumentManager.getInstance(NoteAnnotHandler.this.mPdfViewCtrl).addUndoItem(noteModifyUndoItem);
                            }
                            DocumentManager.getInstance(NoteAnnotHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                            try {
                                RectF tempRectF = annot2.getRect();
                                if (str.equals("")) {
                                    DocumentManager.getInstance(NoteAnnotHandler.this.mPdfViewCtrl).onAnnotModified(annot2.getPage(), annot2);
                                    NoteAnnotHandler.this.mIsModify = true;
                                }
                                if (NoteAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex) && !z) {
                                    RectF annotRectF = annot2.getRect();
                                    NoteAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
                                    NoteAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(tempRectF, tempRectF, pageIndex);
                                    annotRectF.union(tempRectF);
                                    annotRectF.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                    NoteAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(annotRectF));
                                }
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                        }
                        if (callback != null) {
                            callback.result(event, success);
                        }
                    }
                }));
            }
            if (!fromType.equals("")) {
                if (isModifyJni) {
                    DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotModified(annot.getPage(), annot);
                }
                this.mIsModify = true;
                if (!isModifyJni) {
                    Note ta_Annot = (Note) annot;
                    RectF tempRectF = ta_Annot.getRect();
                    ta_Annot.setModifiedDateTime(AppDmUtil.currentDateToDocumentDate());
                    ta_Annot.setBorderColor(undoItem.mColor);
                    ta_Annot.setOpacity(undoItem.mOpacity);
                    ta_Annot.setIconName(undoItem.mIconName);
                    if (undoItem.mContents != null) {
                        ta_Annot.setContent(undoItem.mContents);
                    }
                    ta_Annot.move(undoItem.mBBox);
                    ta_Annot.setModifiedDateTime(AppDmUtil.currentDateToDocumentDate());
                    RectF annotRectF = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(tempRectF, tempRectF, pageIndex);
                    annotRectF.union(tempRectF);
                    annotRectF.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                    this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(annotRectF));
                }
            }
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }
}
