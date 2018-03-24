package com.foxit.uiextensions.annots.caret;

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
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Caret;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.StrikeOut;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContent;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;

public class CaretAnnotHandler implements AnnotHandler {
    private AnnotMenu mAnnotationMenu;
    private PropertyBar mAnnotationProperty;
    private int mBBoxSpace;
    private Paint mBorderPaint;
    private final Context mContext;
    private Annot mCurrentAnnot;
    private RectF mDocViewerRectF;
    private CaretToolHandler mIST_ToolHandler;
    private boolean mIsEditProperty;
    private boolean mIsModify;
    private ArrayList<Integer> mMenuItems;
    private Paint mPaint = new Paint();
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyChangeListener mPropertyChangeListener;
    private CaretToolHandler mRPL_ToolHandler;
    private int mTempLastColor;
    private String mTempLastContent;
    private int mTempLastOpacity;

    public CaretAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mBorderPaint = new Paint();
        this.mBorderPaint.setAntiAlias(true);
        this.mBorderPaint.setStyle(Style.STROKE);
        AppAnnotUtil annotUtil = new AppAnnotUtil(this.mContext);
        this.mBorderPaint.setPathEffect(AppAnnotUtil.getAnnotBBoxPathEffect());
        this.mBorderPaint.setStrokeWidth(annotUtil.getAnnotBBoxStrokeWidth());
        this.mMenuItems = new ArrayList();
        this.mCurrentAnnot = null;
        this.mBBoxSpace = AppAnnotUtil.getAnnotBBoxSpace();
        this.mDocViewerRectF = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    }

    protected ToolHandler getToolHandler(String intent) {
        if (intent == null || !"Replace".equals(intent)) {
            return this.mIST_ToolHandler;
        }
        return this.mRPL_ToolHandler;
    }

    protected void setToolHandler(String intent, CaretToolHandler handler) {
        if (intent == null || !"Replace".equals(intent)) {
            this.mIST_ToolHandler = handler;
        } else {
            this.mRPL_ToolHandler = handler;
        }
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

    public void removePropertyBarListener() {
        this.mPropertyChangeListener = null;
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mAnnotationProperty = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mAnnotationProperty;
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent, Annot annot) {
        try {
            PointF pageViewPt = new PointF(motionEvent.getX(), motionEvent.getY());
            this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pageViewPt, pageViewPt, pageIndex);
            Annot caret = getCaretAnnot(annot);
            if (caret == null) {
                return false;
            }
            switch (motionEvent.getAction()) {
                case 0:
                    if (caret == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt)) {
                        return true;
                    }
                    return false;
                default:
                    return false;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        try {
            PointF pageViewPt = new PointF(motionEvent.getX(), motionEvent.getY());
            this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pageViewPt, pageViewPt, pageIndex);
            Annot caret = getCaretAnnot(annot);
            if (caret == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                if (pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt)) {
                    return true;
                }
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                return true;
            } else if (AppAnnotUtil.isReplaceCaret(caret)) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(caret);
                return true;
            } else {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
                return true;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        try {
            PointF pageViewPt = new PointF(motionEvent.getX(), motionEvent.getY());
            this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pageViewPt, pageViewPt, pageIndex);
            Annot caret = getCaretAnnot(annot);
            if (caret == null) {
                return false;
            }
            if (AppUtil.isFastDoubleClick()) {
                return true;
            }
            if (caret != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                this.mTempLastColor = (int) caret.getBorderColor();
                this.mTempLastOpacity = (int) ((((Markup) caret).getOpacity() * 255.0f) + 0.5f);
                this.mTempLastContent = caret.getContent();
                showDialog(caret);
                return true;
            } else if (pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt)) {
                return true;
            } else {
                return false;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getType() {
        return 14;
    }

    public boolean isHitAnnot(Annot annot, PointF pageViewPt) {
        RectF rectF = getAnnotBBox(annot);
        try {
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, annot.getPage().getIndex());
            rectF.inset(-10.0f, 10.0f);
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return rectF.contains(pageViewPt.x, pageViewPt.y);
    }

    public boolean annotCanAnswer(Annot annot) {
        return true;
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        if (content.getIntent() == null || !"Replace".equals(content.getIntent())) {
            if (this.mIST_ToolHandler != null) {
                this.mIST_ToolHandler.addAnnot(pageIndex, (CaretAnnotContent) content, addUndo, result);
            } else if (result != null) {
                result.result(null, false);
            }
        } else if (this.mRPL_ToolHandler != null) {
            this.mRPL_ToolHandler.addAnnot(pageIndex, (CaretAnnotContent) content, addUndo, result);
        } else if (result != null) {
            result.result(null, false);
        }
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        try {
            delAnnot(annot.getPage().getIndex(), annot, addUndo, result);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void delAnnot(int pageIndex, Annot annot, boolean addUndo, Callback result) {
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        try {
            if (annot.getPage() != null) {
                CaretDeleteUndoItem caretDeleteUndoItem = new CaretDeleteUndoItem(this.mPdfViewCtrl);
                caretDeleteUndoItem.mPageIndex = pageIndex;
                caretDeleteUndoItem.mNM = annot.getUniqueID();
                caretDeleteUndoItem.mColor = annot.getBorderColor();
                caretDeleteUndoItem.mOpacity = ((Caret) annot).getOpacity();
                caretDeleteUndoItem.mBBox = new RectF(annot.getRect());
                caretDeleteUndoItem.mAuthor = ((Caret) annot).getTitle();
                caretDeleteUndoItem.mContents = annot.getContent();
                caretDeleteUndoItem.mModifiedDate = annot.getModifiedDateTime();
                caretDeleteUndoItem.mCreationDate = ((Caret) annot).getCreationDateTime();
                caretDeleteUndoItem.mFlags = annot.getFlags();
                caretDeleteUndoItem.mSubject = ((Caret) annot).getSubject();
                caretDeleteUndoItem.mIntent = ((Caret) annot).getIntent();
                if (annot.getDict().getElement("Rotate") != null) {
                    caretDeleteUndoItem.mRotate = (360 - annot.getDict().getElement("Rotate").getInteger()) / 90;
                } else {
                    caretDeleteUndoItem.mRotate = 0;
                }
                caretDeleteUndoItem.mIsReplace = AppAnnotUtil.isReplaceCaret(annot);
                if (caretDeleteUndoItem.mIsReplace) {
                    StrikeOut strikeout = getStrikeOutFromCaret(annot);
                    if (strikeout != null) {
                        final ArrayList<PointF> quadPoints = new ArrayList();
                        final RectF bbox = new RectF(strikeout.getRect());
                        final int color = (int) strikeout.getBorderColor();
                        final int opacity = (int) ((strikeout.getOpacity() * 255.0f) + 0.5f);
                        final float linewidth = strikeout.getBorderInfo().getWidth();
                        final String subject = strikeout.getSubject();
                        final String content = strikeout.getContent();
                        final String nm = strikeout.getUniqueID();
                        final DateTime dateTime = strikeout.getModifiedDateTime();
                        try {
                            int count = strikeout.getQuadPointsCount();
                            for (int i = 0; i < count; i++) {
                                PointF pointF1 = new PointF();
                                pointF1.set(strikeout.getQuadPoints(i).first);
                                PointF pointF2 = new PointF();
                                pointF2.set(strikeout.getQuadPoints(i).second);
                                PointF pointF3 = new PointF();
                                pointF3.set(strikeout.getQuadPoints(i).third);
                                PointF pointF4 = new PointF();
                                pointF4.set(strikeout.getQuadPoints(i).fourth);
                                quadPoints.add(pointF1);
                                quadPoints.add(pointF2);
                                quadPoints.add(pointF3);
                                quadPoints.add(pointF4);
                            }
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                        final int i2 = pageIndex;
                        caretDeleteUndoItem.mTMContent = new TextMarkupContent() {
                            public ArrayList<PointF> getQuadPoints() {
                                return quadPoints;
                            }

                            public int getPageIndex() {
                                return i2;
                            }

                            public int getType() {
                                return 12;
                            }

                            public String getNM() {
                                return nm;
                            }

                            public RectF getBBox() {
                                return bbox;
                            }

                            public int getColor() {
                                return color;
                            }

                            public int getOpacity() {
                                return opacity;
                            }

                            public float getLineWidth() {
                                return linewidth;
                            }

                            public String getSubject() {
                                return subject != null ? subject : "Replace";
                            }

                            public DateTime getModifiedDate() {
                                return dateTime;
                            }

                            public String getContents() {
                                return content;
                            }

                            public String getIntent() {
                                return "StrikeOutTextEdit";
                            }
                        };
                    }
                }
                deleteAnnot(annot, caretDeleteUndoItem, addUndo, result);
            } else if (result != null) {
                result.result(null, false);
            }
        } catch (PDFException e2) {
            e2.printStackTrace();
        }
    }

    protected boolean deleteAnnot(Annot annot, CaretDeleteUndoItem undoItem, boolean addUndo, Callback result) {
        try {
            PDFPage page = annot.getPage();
            final int pageIndex = page.getIndex();
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF viewRect = annot.getRect();
            boolean bReplace = AppAnnotUtil.isReplaceCaret(annot);
            StrikeOut strikeOut = getStrikeOutFromCaret(annot);
            if (bReplace && strikeOut != null) {
                RectF sto_Rect = new RectF(strikeOut.getRect());
                sto_Rect.union(viewRect);
                viewRect.set(sto_Rect);
            }
            final boolean z = addUndo;
            final CaretDeleteUndoItem caretDeleteUndoItem = undoItem;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CaretEvent(3, undoItem, (Caret) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (z) {
                            DocumentManager.getInstance(CaretAnnotHandler.this.mPdfViewCtrl).addUndoItem(caretDeleteUndoItem);
                        }
                        CaretAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                        CaretAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                    }
                    if (callback != null) {
                        callback.result(null, success);
                    }
                }
            }));
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        if (content != null) {
            try {
                CaretModifyUndoItem undoItem = new CaretModifyUndoItem(this.mPdfViewCtrl);
                undoItem.setCurrentValue(content);
                undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
                undoItem.mLastColor = (int) annot.getBorderColor();
                undoItem.mLastOpacity = ((Caret) annot).getOpacity();
                undoItem.mLastContent = annot.getContent();
                modifyAnnot(annot.getPage().getIndex(), annot, undoItem, true, addUndo, "", result);
            } catch (PDFException e) {
                e.getLastError();
            }
        } else if (result != null) {
            result.result(null, false);
        }
    }

    private void modifyAnnot(int pageIndex, Annot annot, int color, int opacity, String content, boolean modifyJni, boolean addUndo) {
        CaretModifyUndoItem undoItem = new CaretModifyUndoItem(this.mPdfViewCtrl);
        undoItem.mPageIndex = pageIndex;
        undoItem.setCurrentValue(annot);
        undoItem.mColor = (long) color;
        undoItem.mOpacity = ((float) opacity) / 255.0f;
        if (content == null) {
            content = "";
        }
        undoItem.mContents = content;
        undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
        undoItem.mLastColor = this.mTempLastColor;
        undoItem.mLastOpacity = ((float) this.mTempLastOpacity) / 255.0f;
        undoItem.mLastContent = this.mTempLastContent;
        modifyAnnot(pageIndex, annot, undoItem, modifyJni, addUndo, Module.MODULE_NAME_CARET, null);
        if (AppAnnotUtil.isReplaceCaret(annot)) {
            final StrikeOut subAnnot = getStrikeOutFromCaret(annot);
            if (subAnnot != null) {
                final int i = pageIndex;
                final int i2 = color;
                final int i3 = opacity;
                final Annot annot2 = annot;
                ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), 12).modifyAnnot(subAnnot, new AnnotContent() {
                    public int getPageIndex() {
                        return i;
                    }

                    public int getType() {
                        return 12;
                    }

                    public String getNM() {
                        try {
                            return subAnnot.getUniqueID();
                        } catch (PDFException e) {
                            return null;
                        }
                    }

                    public RectF getBBox() {
                        try {
                            return subAnnot.getRect();
                        } catch (PDFException e) {
                            return null;
                        }
                    }

                    public int getColor() {
                        return i2;
                    }

                    public int getOpacity() {
                        return i3;
                    }

                    public float getLineWidth() {
                        try {
                            return subAnnot.getBorderInfo().getWidth();
                        } catch (PDFException e) {
                            return 0.0f;
                        }
                    }

                    public String getSubject() {
                        try {
                            return subAnnot.getSubject();
                        } catch (PDFException e) {
                            return null;
                        }
                    }

                    public DateTime getModifiedDate() {
                        try {
                            return annot2.getModifiedDateTime();
                        } catch (PDFException e) {
                            return null;
                        }
                    }

                    public String getContents() {
                        try {
                            return annot2.getContent();
                        } catch (PDFException e) {
                            return null;
                        }
                    }

                    public String getIntent() {
                        try {
                            return subAnnot.getIntent();
                        } catch (PDFException e) {
                            return null;
                        }
                    }
                }, false, null);
            }
        }
    }

    private void modifyAnnot(int pageIndex, Annot annot, CaretModifyUndoItem undoItem, boolean isModifyJni, boolean addUndo, String fromType, Callback result) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            if (isModifyJni) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(true);
                CaretEvent event = new CaretEvent(2, undoItem, (Caret) annot, this.mPdfViewCtrl);
                final boolean z = addUndo;
                final CaretModifyUndoItem caretModifyUndoItem = undoItem;
                final Annot annot2 = annot;
                final String str = fromType;
                final int i = pageIndex;
                final Callback callback = result;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(event, new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            if (z) {
                                DocumentManager.getInstance(CaretAnnotHandler.this.mPdfViewCtrl).addUndoItem(caretModifyUndoItem);
                            }
                            DocumentManager.getInstance(CaretAnnotHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                            try {
                                RectF tempRectF = annot2.getRect();
                                if (str.equals("")) {
                                    CaretAnnotHandler.this.mIsModify = true;
                                    DocumentManager.getInstance(CaretAnnotHandler.this.mPdfViewCtrl).onAnnotModified(page, annot2);
                                }
                                if (CaretAnnotHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                                    RectF viewRect = annot2.getRect();
                                    CaretAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, i);
                                    CaretAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(tempRectF, tempRectF, i);
                                    viewRect.union(tempRectF);
                                    viewRect.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                    CaretAnnotHandler.this.mPdfViewCtrl.refresh(i, AppDmUtil.rectFToRect(viewRect));
                                }
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                        }
                        if (callback != null) {
                            callback.result(null, success);
                        }
                    }
                }));
            }
            if (!fromType.equals("")) {
                DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotModified(page, annot);
                this.mIsModify = true;
                if (!isModifyJni) {
                    Caret caret = (Caret) annot;
                    RectF tempRectF = caret.getRect();
                    caret.setBorderColor(undoItem.mColor);
                    caret.setOpacity(undoItem.mOpacity);
                    caret.setContent(undoItem.mContents);
                    caret.setModifiedDateTime(AppDmUtil.currentDateToDocumentDate());
                    caret.resetAppearanceStream();
                    if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        RectF viewRect = annot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(tempRectF, tempRectF, pageIndex);
                        viewRect.union(tempRectF);
                        viewRect.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                        this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                    }
                }
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
        this.mAnnotationMenu.dismiss();
        if (this.mIsEditProperty) {
            this.mIsEditProperty = false;
        }
        this.mAnnotationProperty.dismiss();
        try {
            int pageIndex = annot.getPage().getIndex();
            RectF pdfRect = annot.getRect();
            RectF viewRect = new RectF(pdfRect.left, pdfRect.top, pdfRect.right, pdfRect.bottom);
            if (this.mIsModify && reRender) {
                if (((long) this.mTempLastColor) == annot.getBorderColor() && ((float) this.mTempLastOpacity) == ((float) ((int) ((Markup) annot).getOpacity())) * 255.0f) {
                    modifyAnnot(pageIndex, annot, (int) annot.getBorderColor(), (int) ((((Markup) annot).getOpacity() * 255.0f) + 0.5f), annot.getContent(), false, true);
                } else {
                    modifyAnnot(pageIndex, annot, (int) annot.getBorderColor(), (int) ((((Markup) annot).getOpacity() * 255.0f) + 0.5f), annot.getContent(), true, true);
                }
            } else if (this.mIsModify) {
                annot.setBorderColor((long) this.mTempLastColor);
                ((Markup) annot).setOpacity(((float) this.mTempLastOpacity) / 255.0f);
                annot.setContent(this.mTempLastContent);
            }
            this.mIsModify = false;
            if (this.mPdfViewCtrl.isPageVisible(pageIndex) && reRender) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                this.mCurrentAnnot = null;
                return;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        this.mCurrentAnnot = null;
    }

    public void onAnnotSelected(Annot annot, boolean reRender) {
        Caret caret = (Caret) annot;
        try {
            int pageIndex = annot.getPage().getIndex();
            if (caret.isGrouped()) {
                this.mTempLastColor = (int) caret.getBorderColor();
                this.mTempLastOpacity = (int) ((caret.getOpacity() * 255.0f) + 0.5f);
                this.mTempLastContent = caret.getContent();
            } else {
                this.mTempLastColor = (int) caret.getBorderColor();
                this.mTempLastOpacity = (int) ((caret.getOpacity() * 255.0f) + 0.5f);
                this.mTempLastContent = caret.getContent();
            }
            this.mCurrentAnnot = annot;
            this.mAnnotationMenu.dismiss();
            prepareAnnotMenu(caret);
            RectF caretRectF = this.mCurrentAnnot.getRect();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(caretRectF, caretRectF, pageIndex);
            if (AppAnnotUtil.isReplaceCaret(annot)) {
                RectF strikeoutRectF = getStrikeOutFromCaret(annot).getRect();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(strikeoutRectF, strikeoutRectF, pageIndex);
                caretRectF.union(strikeoutRectF);
            }
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(caretRectF, caretRectF, pageIndex);
            this.mAnnotationMenu.show(caretRectF);
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(caretRectF, caretRectF, pageIndex);
                this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(caretRectF));
                if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                    this.mCurrentAnnot = annot;
                }
            } else {
                this.mCurrentAnnot = annot;
            }
            this.mIsModify = false;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void prepareAnnotMenu(final Annot caret) {
        this.mMenuItems.clear();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mMenuItems.add(Integer.valueOf(6));
            this.mMenuItems.add(Integer.valueOf(3));
            this.mMenuItems.add(Integer.valueOf(4));
            this.mMenuItems.add(Integer.valueOf(2));
        } else {
            this.mMenuItems.add(Integer.valueOf(3));
        }
        this.mAnnotationMenu.setMenuItems(this.mMenuItems);
        this.mAnnotationMenu.setListener(new ClickListener() {
            public void onAMClick(int btType) {
                try {
                    int pageIndex = caret.getPage().getIndex();
                    if (btType == 3) {
                        DocumentManager.getInstance(CaretAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.showComments(CaretAnnotHandler.this.mContext, CaretAnnotHandler.this.mPdfViewCtrl, CaretAnnotHandler.this.mParent, caret);
                    } else if (btType == 4) {
                        DocumentManager.getInstance(CaretAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.replyToAnnot(CaretAnnotHandler.this.mContext, CaretAnnotHandler.this.mPdfViewCtrl, CaretAnnotHandler.this.mParent, caret);
                    } else if (btType == 2) {
                        CaretAnnotHandler.this.delAnnot(pageIndex, caret, true, null);
                    } else if (btType == 6) {
                        CaretAnnotHandler.this.mAnnotationMenu.dismiss();
                        CaretAnnotHandler.this.mIsEditProperty = true;
                        int[] colors = new int[PropertyBar.PB_COLORS_CARET.length];
                        System.arraycopy(PropertyBar.PB_COLORS_CARET, 0, colors, 0, colors.length);
                        colors[0] = PropertyBar.PB_COLORS_CARET[0];
                        CaretAnnotHandler.this.mAnnotationProperty.setColors(colors);
                        try {
                            CaretAnnotHandler.this.mAnnotationProperty.setProperty(1, (int) DocumentManager.getInstance(CaretAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot().getBorderColor());
                            CaretAnnotHandler.this.mAnnotationProperty.setProperty(2, AppDmUtil.opacity255To100((int) ((((Markup) DocumentManager.getInstance(CaretAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()).getOpacity() * 255.0f) + 0.5f)));
                            CaretAnnotHandler.this.mAnnotationProperty.reset(3);
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                        RectF annotRectF = new RectF();
                        if (CaretAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            CaretAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(caret.getRect(), annotRectF, pageIndex);
                            if (AppAnnotUtil.isReplaceCaret(caret)) {
                                RectF strikeoutRect = CaretAnnotHandler.getStrikeOutFromCaret(caret).getRect();
                                CaretAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(strikeoutRect, strikeoutRect, pageIndex);
                                annotRectF.union(strikeoutRect);
                            }
                            CaretAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(annotRectF, annotRectF, pageIndex);
                        }
                        CaretAnnotHandler.this.mAnnotationProperty.show(annotRectF, false);
                        CaretAnnotHandler.this.mAnnotationProperty.setPropertyChangeListener(CaretAnnotHandler.this.mPropertyChangeListener);
                    }
                } catch (PDFException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
            if (annot != null && (annot instanceof Caret) && this.mCurrentAnnot == annot && annot.getPage().getIndex() == pageIndex) {
                canvas.save();
                RectF bBoxRectF = annot.getRect();
                RectF bInnerRect = ((Caret) annot).getInnerRect();
                float lineWidth = (bBoxRectF.right - bBoxRectF.left) / 5.0f;
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(bBoxRectF, bBoxRectF, pageIndex);
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(bInnerRect, bInnerRect, pageIndex);
                this.mPaint.setStyle(Style.STROKE);
                this.mPaint.setStrokeWidth(LineWidth2PageView(pageIndex, lineWidth));
                this.mPaint.setColor(AppDmUtil.calColorByMultiply((int) annot.getBorderColor(), (int) ((((Markup) annot).getOpacity() * 255.0f) + 0.5f)));
                this.mBorderPaint.setColor((int) annot.getBorderColor());
                RectF borderRectF = new RectF();
                borderRectF.set(bBoxRectF.left - ((float) this.mBBoxSpace), bBoxRectF.top - ((float) this.mBBoxSpace), bBoxRectF.right + ((float) this.mBBoxSpace), bBoxRectF.bottom + ((float) this.mBBoxSpace));
                canvas.drawRect(borderRectF, this.mBorderPaint);
                if (AppAnnotUtil.isReplaceCaret(annot)) {
                    RectF subRectF = new RectF(getStrikeOutFromCaret(annot).getRect());
                    RectF parentRectF = new RectF(annot.getRect());
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(subRectF, subRectF, pageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(parentRectF, parentRectF, pageIndex);
                    subRectF.union(parentRectF);
                    borderRectF.set(subRectF.left - ((float) this.mBBoxSpace), subRectF.top - ((float) this.mBBoxSpace), subRectF.right + ((float) this.mBBoxSpace), subRectF.bottom + ((float) this.mBBoxSpace));
                    canvas.drawRect(borderRectF, this.mBorderPaint);
                }
                canvas.restore();
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null && (curAnnot instanceof Caret) && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                int pageIndex = curAnnot.getPage().getIndex();
                boolean isReplace = AppAnnotUtil.isReplaceCaret(curAnnot);
                if (curAnnot.getType() == 14) {
                    RectF caretRect = new RectF();
                    RectF strikeoutRect = new RectF();
                    if (isReplace) {
                        strikeoutRect.set(getStrikeOutFromCaret(curAnnot).getRect());
                        caretRect.set(curAnnot.getRect());
                    } else {
                        caretRect.set(curAnnot.getRect());
                    }
                    if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(caretRect, caretRect, pageIndex);
                        if (isReplace) {
                            this.mPdfViewCtrl.convertPdfRectToPageViewRect(strikeoutRect, strikeoutRect, pageIndex);
                            caretRect.union(strikeoutRect);
                        }
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(caretRect, caretRect, pageIndex);
                        this.mAnnotationMenu.update(caretRect);
                        this.mDocViewerRectF.set(caretRect);
                        if (this.mIsEditProperty) {
                            ((PropertyBarImpl) this.mAnnotationProperty).onConfigurationChanged(this.mDocViewerRectF);
                        }
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public RectF getAnnotBBox(Annot annot) {
        if (annot != null) {
            try {
                return annot.getRect();
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Annot getCaretAnnot(Annot annot) {
        try {
            if (annot.getType() == 14) {
                return annot;
            }
            Annot caret = null;
            if (annot.getType() == 12) {
                caret = ((Markup) annot).getGroupHeader();
            }
            return caret;
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static StrikeOut getStrikeOutFromCaret(Annot annot) {
        try {
            Caret caret = (Caret) annot;
            int nCount = caret.getGroupElementCount();
            for (int i = 0; i < nCount; i++) {
                Markup groupAnnot = caret.getGroupElement(i);
                if (groupAnnot.getType() == 12) {
                    return (StrikeOut) groupAnnot;
                }
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return null;
    }

    private float LineWidth2PageView(int pageIndex, float lineWidth) {
        RectF rectF = new RectF(0.0f, 0.0f, lineWidth, lineWidth);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
        return Math.abs(rectF.width());
    }

    public void onColorValueChanged(int color) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (((long) color) != annot.getBorderColor()) {
                    modifyAnnot(annot.getPage().getIndex(), annot, color, (int) ((((Markup) annot).getOpacity() * 255.0f) + 0.5f), annot.getContent(), false, true);
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
                if (AppDmUtil.opacity100To255(opacity) != ((int) (((Markup) annot).getOpacity() * 255.0f))) {
                    modifyAnnot(annot.getPage().getIndex(), annot, (int) annot.getBorderColor(), AppDmUtil.opacity100To255(opacity), annot.getContent(), false, true);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(11)
    private void showDialog(final Annot annot) {
        Context context = this.mContext;
        View contentView = View.inflate(context, R.layout.rd_note_dialog_edit, null);
        TextView contentTitle = (TextView) contentView.findViewById(R.id.rd_note_dialog_edit_title);
        final EditText contentEditText = (EditText) contentView.findViewById(R.id.rd_note_dialog_edit);
        Button cancelButton = (Button) contentView.findViewById(R.id.rd_note_dialog_edit_cancel);
        final Button applayButton = (Button) contentView.findViewById(R.id.rd_note_dialog_edit_ok);
        final Dialog contentDialog = new Dialog(context, R.style.rv_dialog_style);
        contentView.setLayoutParams(new LayoutParams(-1, -2));
        contentDialog.setContentView(contentView, new LayoutParams(AppDisplay.getInstance(this.mContext).getUITextEditDialogWidth(), -2));
        contentEditText.setMaxLines(10);
        contentDialog.getWindow().setFlags(1024, 1024);
        contentDialog.getWindow().setBackgroundDrawableResource(R.drawable.dlg_title_bg_4circle_corner_white);
        if (AppAnnotUtil.isReplaceCaret(annot)) {
            contentTitle.setText(this.mContext.getResources().getString(R.string.fx_string_replacetext));
        } else {
            contentTitle.setText(this.mContext.getResources().getString(R.string.fx_string_inserttext));
        }
        contentEditText.setEnabled(true);
        try {
            String content = annot.getContent();
            if (content == null) {
                content = "";
            }
            contentEditText.setText(content);
            contentEditText.setSelection(content.length());
            applayButton.setEnabled(false);
            applayButton.setTextColor(this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
        } catch (PDFException e) {
            e.printStackTrace();
        }
        contentEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                try {
                    if (contentEditText.getText().toString().equals(annot.getContent())) {
                        applayButton.setEnabled(false);
                        applayButton.setTextColor(CaretAnnotHandler.this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
                        return;
                    }
                    applayButton.setEnabled(true);
                    applayButton.setTextColor(CaretAnnotHandler.this.mContext.getResources().getColor(R.color.dlg_bt_text_selector));
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
            }
        });
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                contentDialog.dismiss();
            }
        });
        applayButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    int pageIndex = annot.getPage().getIndex();
                    if (!contentEditText.getText().toString().equals(annot.getContent())) {
                        CaretAnnotHandler.this.modifyAnnot(pageIndex, annot, (int) annot.getBorderColor(), (int) (((Markup) annot).getOpacity() * 255.0f), contentEditText.getText().toString(), true, true);
                    }
                    contentDialog.dismiss();
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        });
        contentDialog.show();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            AppUtil.showSoftInput(contentEditText);
            return;
        }
        contentEditText.setFocusable(false);
        contentEditText.setLongClickable(false);
        if (VERSION.SDK_INT > 11) {
            contentEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
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
            contentEditText.setEnabled(false);
        }
    }
}
