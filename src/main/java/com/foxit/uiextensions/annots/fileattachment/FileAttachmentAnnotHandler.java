package com.foxit.uiextensions.annots.fileattachment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FileAttachment;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.fileattachment.FileAttachmentModule.IAttachmentDocEvent;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppIntentUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class FileAttachmentAnnotHandler implements AnnotHandler {
    private String TMP_PATH;
    private AnnotMenu mAnnotMenu;
    private PropertyBar mAnnotPropertyBar;
    private ArrayList<IAttachmentDocEvent> mAttachDocEventListeners = new ArrayList();
    private PDFViewCtrl mAttachPdfViewCtrl;
    private int mBBoxSpace;
    private Annot mBitmapAnnot;
    private Context mContext;
    private PointF mDownPoint;
    private RectF mDrawLocal_tmpF;
    private FileAttachmentModule mFileAttachmentModule;
    private FileAttachmentToolHandler mFileAttachmentToolHandler;
    private LinearLayout mIconLayout;
    private boolean mIsAnnotModified;
    private boolean mIsEditProperty;
    private PointF mLastPoint;
    private ArrayList<Integer> mMenuItems;
    private int mModifyColor;
    private String mModifyIconName;
    private float mModifyOpacity;
    private View mOpenView;
    private ImageView mOpenView_backIV;
    private LinearLayout mOpenView_contentLy;
    private TextView mOpenView_filenameTV;
    private LinearLayout mOpenView_titleLy;
    private int[] mPBColors = new int[PropertyBar.PB_COLORS_FILEATTACHMENT.length];
    private Paint mPaintAnnot;
    private Paint mPaintBbox;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private FileAttachmentPBAdapter mPropertyListViewAdapter;
    private RectF mTmpUndoBbox;
    private int mTmpUndoColor;
    private String mTmpUndoIconName;
    private String mTmpUndoModifiedDate;
    private float mTmpUndoOpacity;
    private boolean mTouchCaptured = false;
    private boolean mbAttachengOpening = false;

    public FileAttachmentAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewer, FileAttachmentModule fileAttachmentModule) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewer;
        this.mFileAttachmentModule = fileAttachmentModule;
        this.mBBoxSpace = AppAnnotUtil.getAnnotBBoxSpace();
        this.mPaintBbox = new Paint();
        this.mPaintBbox.setAntiAlias(true);
        this.mPaintBbox.setStyle(Style.STROKE);
        this.mPaintBbox.setStrokeWidth(new AppAnnotUtil(this.mContext).getAnnotBBoxStrokeWidth());
        this.mPaintBbox.setPathEffect(AppAnnotUtil.getAnnotBBoxPathEffect());
        this.mPaintAnnot = new Paint();
        this.mPaintAnnot.setStyle(Style.STROKE);
        this.mPaintAnnot.setAntiAlias(true);
        this.mDrawLocal_tmpF = new RectF();
        this.mDownPoint = new PointF();
        this.mLastPoint = new PointF();
        this.mMenuItems = new ArrayList();
        this.mAnnotMenu = new AnnotMenuImpl(this.mContext, this.mParent);
        this.mAnnotMenu.setMenuItems(this.mMenuItems);
        this.TMP_PATH = Environment.getExternalStorageDirectory() + "/FoxitSDK/AttaTmp/";
        this.mAnnotPropertyBar = fileAttachmentModule.getPropertyBar();
        initOpenView();
    }

    public String getTMP_PATH() {
        return this.TMP_PATH;
    }

    public void deleteTMP_PATH() {
        deleteDir(new File(this.TMP_PATH));
    }

    private void deleteDir(File path) {
        if (!path.exists()) {
            return;
        }
        if (path.isFile()) {
            path.delete();
            return;
        }
        File[] files = path.listFiles();
        if (files != null) {
            for (File deleteDir : files) {
                deleteDir(deleteDir);
            }
            path.delete();
        }
    }

    public void setPropertyListViewAdapter(FileAttachmentPBAdapter adapter) {
        this.mPropertyListViewAdapter = adapter;
    }

    public int getType() {
        return 17;
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

    public void resetMenuItems() {
        this.mMenuItems.clear();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mMenuItems.add(Integer.valueOf(6));
            this.mMenuItems.add(Integer.valueOf(3));
            this.mMenuItems.add(Integer.valueOf(2));
            return;
        }
        this.mMenuItems.add(Integer.valueOf(3));
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mOpenView.getVisibility() == 0 && keyCode == 4) {
            closeAttachment();
            this.mOpenView.setVisibility(8);
            return true;
        } else if (ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) != this || keyCode != 4) {
            return false;
        } else {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            return true;
        }
    }

    public void setPropertyBarIconLayout(LinearLayout layout) {
        this.mIconLayout = layout;
    }

    public void onAnnotSelected(final Annot annotation, boolean reRender) {
        if (annotation != null && (annotation instanceof FileAttachment)) {
            final Annot annot = (FileAttachment) annotation;
            try {
                this.mTmpUndoColor = (int) annot.getBorderColor();
                this.mTmpUndoOpacity = annot.getOpacity();
                this.mTmpUndoIconName = annot.getIconName();
                this.mTmpUndoBbox = annot.getRect();
                this.mTmpUndoModifiedDate = AppDmUtil.getLocalDateString(annot.getModifiedDateTime());
                this.mModifyIconName = this.mTmpUndoIconName;
                this.mModifyOpacity = this.mTmpUndoOpacity;
                this.mModifyColor = this.mTmpUndoColor;
                this.mBitmapAnnot = annot;
                this.mAnnotPropertyBar.setArrowVisible(false);
                resetMenuItems();
                this.mAnnotMenu.setMenuItems(this.mMenuItems);
                this.mAnnotMenu.setListener(new ClickListener() {
                    public void onAMClick(int btType) {
                        try {
                            int pageIndex = annotation.getPage().getIndex();
                            if (btType == 1) {
                                ((ClipboardManager) FileAttachmentAnnotHandler.this.mContext.getSystemService("clipboard")).setText(annot.getContent());
                                AppAnnotUtil.toastAnnotCopy(FileAttachmentAnnotHandler.this.mContext);
                                DocumentManager.getInstance(FileAttachmentAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                            } else if (btType == 2) {
                                FileAttachmentAnnotHandler.this.DeleteAnnot(annot, Boolean.valueOf(true), null);
                            } else if (btType == 6) {
                                FileAttachmentAnnotHandler.this.mAnnotMenu.dismiss();
                                FileAttachmentAnnotHandler.this.mIsEditProperty = true;
                                System.arraycopy(PropertyBar.PB_COLORS_FILEATTACHMENT, 0, FileAttachmentAnnotHandler.this.mPBColors, 0, FileAttachmentAnnotHandler.this.mPBColors.length);
                                FileAttachmentAnnotHandler.this.mPBColors[0] = PropertyBar.PB_COLORS_FILEATTACHMENT[0];
                                FileAttachmentAnnotHandler.this.mAnnotPropertyBar.setColors(FileAttachmentAnnotHandler.this.mPBColors);
                                FileAttachmentAnnotHandler.this.mAnnotPropertyBar.setProperty(1, (float) annot.getBorderColor());
                                FileAttachmentAnnotHandler.this.mAnnotPropertyBar.setProperty(2, AppDmUtil.opacity255To100((int) ((annot.getOpacity() * 255.0f) + 0.5f)));
                                FileAttachmentAnnotHandler.this.mAnnotPropertyBar.reset(3);
                                FileAttachmentAnnotHandler.this.mAnnotPropertyBar.addTab("", 0, FileAttachmentAnnotHandler.this.mContext.getResources().getString(R.string.pb_type_tab), 0);
                                FileAttachmentAnnotHandler.this.mPropertyListViewAdapter.setNoteIconType(FileAttachmentUtil.getIconType(annot.getIconName()));
                                FileAttachmentAnnotHandler.this.mAnnotPropertyBar.addCustomItem(PropertyBar.PROPERTY_FILEATTACHMENT, FileAttachmentAnnotHandler.this.mIconLayout, 0, 0);
                                FileAttachmentAnnotHandler.this.mAnnotPropertyBar.setPropertyChangeListener(FileAttachmentAnnotHandler.this.mFileAttachmentModule);
                                RectF annotRectF = annot.getRect();
                                FileAttachmentAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
                                FileAttachmentAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(annotRectF, annotRectF, pageIndex);
                                FileAttachmentAnnotHandler.this.mAnnotPropertyBar.show(annotRectF, false);
                            } else if (btType == 3) {
                                DocumentManager.getInstance(FileAttachmentAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                FileAttachmentAnnotHandler.this._onOpenAttachment(annot);
                            }
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                    }
                });
                RectF annotRectF = annot.getRect();
                int pageIndex = annot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                    RectF modifyRectF = new RectF(annotRectF);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(annotRectF, annotRectF, pageIndex);
                    this.mAnnotMenu.show(annotRectF);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(modifyRectF, modifyRectF, pageIndex);
                    this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(modifyRectF));
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
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
        if (annot != null && (annot instanceof FileAttachment)) {
            this.mAnnotMenu.dismiss();
            if (this.mIsEditProperty) {
                this.mIsEditProperty = false;
            }
            try {
                PDFPage page = annot.getPage();
                if (page != null) {
                    int pageIndex = page.getIndex();
                    if (this.mIsAnnotModified && reRender) {
                        if (!(this.mTmpUndoColor == this.mModifyColor && this.mTmpUndoOpacity == this.mModifyOpacity && this.mTmpUndoIconName == this.mModifyIconName && this.mTmpUndoBbox.equals(annot.getRect()))) {
                            ModifyAnnot(annot, this.mModifyColor, this.mModifyOpacity, this.mModifyIconName, null, true, null);
                        }
                    } else if (this.mIsAnnotModified) {
                        annot.setBorderColor((long) this.mTmpUndoColor);
                        ((FileAttachment) annot).setOpacity(((float) AppDmUtil.opacity100To255((int) this.mTmpUndoOpacity)) / 255.0f);
                        ((FileAttachment) annot).setIconName(this.mTmpUndoIconName);
                        annot.move(this.mTmpUndoBbox);
                        annot.setModifiedDateTime(AppDmUtil.parseDocumentDate(this.mTmpUndoModifiedDate));
                        annot.resetAppearanceStream();
                    }
                    this.mIsAnnotModified = false;
                    RectF rect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, pageIndex);
                    this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(rect));
                    this.mBitmapAnnot = null;
                    this.mBitmapAnnot = null;
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        if (this.mFileAttachmentToolHandler != null) {
            this.mFileAttachmentToolHandler.addAnnot(pageIndex, content.getBBox(), result);
        } else if (result != null) {
            result.result(null, false);
        }
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        if (annot != null && (annot instanceof FileAttachment)) {
            try {
                this.mTmpUndoColor = (int) annot.getBorderColor();
                this.mTmpUndoOpacity = ((FileAttachment) annot).getOpacity();
                this.mTmpUndoIconName = ((FileAttachment) annot).getIconName();
                this.mTmpUndoBbox = annot.getRect();
                this.mTmpUndoModifiedDate = AppDmUtil.getLocalDateString(annot.getModifiedDateTime());
                this.mIsAnnotModified = true;
                if (content != null) {
                    IFileAttachmentAnnotContent annotContent = (IFileAttachmentAnnotContent) IFileAttachmentAnnotContent.class.cast(content);
                    ModifyAnnot(annot, annotContent.getColor(), (float) annotContent.getOpacity(), ((FileAttachment) annot).getIconName(), AppDmUtil.getLocalDateString(annotContent.getModifiedDate()), addUndo, result);
                    return;
                }
                ModifyAnnot(annot, (int) annot.getBorderColor(), ((FileAttachment) annot).getOpacity(), ((FileAttachment) annot).getIconName(), AppDmUtil.getLocalDateString(annot.getModifiedDateTime()), addUndo, result);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        DeleteAnnot(annot, Boolean.valueOf(addUndo), result);
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent, Annot annot) {
        int action = motionEvent.getActionMasked();
        PointF devPt = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF point = new PointF();
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(devPt, point, pageIndex);
        PointF pageViewPt = new PointF(point.x, point.y);
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
                            if (pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt)) {
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
                    if (this.mTouchCaptured && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && this.mPdfViewCtrl.getCurrentPage() == pageIndex && DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
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
                        this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(rectF));
                        rectF = new RectF(rectF);
                        RectF canvasRectF = new RectF();
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, canvasRectF, pageIndex);
                        if (this.mIsEditProperty) {
                            if (this.mAnnotPropertyBar.isShowing()) {
                                this.mAnnotPropertyBar.update(canvasRectF);
                            } else {
                                this.mAnnotPropertyBar.show(canvasRectF, false);
                            }
                        } else if (this.mAnnotMenu.isShowing()) {
                            this.mAnnotMenu.update(canvasRectF);
                        } else {
                            this.mAnnotMenu.show(canvasRectF);
                        }
                        RectF rect = new RectF();
                        this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, rect, pageIndex);
                        if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                            this.mIsAnnotModified = true;
                            annot.move(rect);
                            pageViewRectF.inset((float) ((-this.mBBoxSpace) - 3), (float) ((-this.mBBoxSpace) - 3));
                            this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(pageViewRectF));
                            this.mModifyColor = (int) annot.getBorderColor();
                            this.mModifyOpacity = ((FileAttachment) annot).getOpacity();
                            this.mModifyIconName = ((FileAttachment) annot).getIconName();
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
                                this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRectF, pageViewRectF, annot.getIndex());
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
                                    this.mAnnotPropertyBar.dismiss();
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
            if (e13.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
            }
            return true;
        }
        if (e13.getLastError() == 10) {
            this.mPdfViewCtrl.recoverForOOM();
        }
        return true;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        PointF pageViewPt = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            try {
                if (!(pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt))) {
                    DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        } else {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
        }
        return true;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        if (!AppUtil.isFastDoubleClick()) {
            PointF pageViewPt = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                try {
                    if (!(pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt))) {
                        DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            } else {
                _onOpenAttachment(annot);
            }
        }
        return true;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                int id = annot.getPage().getIndex();
                if (this.mBitmapAnnot == annot && id == pageIndex) {
                    canvas.save();
                    RectF frameRectF = new RectF();
                    RectF rect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, annot.getPage().getIndex());
                    rect.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    frameRectF.set(rect.left - ((float) this.mBBoxSpace), rect.top - ((float) this.mBBoxSpace), rect.right + ((float) this.mBBoxSpace), rect.bottom + ((float) this.mBBoxSpace));
                    this.mPaintBbox.setColor((int) (annot.getBorderColor() | -16777216));
                    canvas.drawRect(frameRectF, this.mPaintBbox);
                    canvas.restore();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null) {
            try {
                if (ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), curAnnot.getType()) == this) {
                    if (this.mPdfViewCtrl.isPageVisible(curAnnot.getPage().getIndex())) {
                        RectF rect = curAnnot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, curAnnot.getPage().getIndex());
                        rect.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rect, rect, curAnnot.getPage().getIndex());
                        this.mAnnotMenu.update(rect);
                        this.mDrawLocal_tmpF.set(rect);
                        if (this.mIsEditProperty) {
                            ((PropertyBarImpl) this.mAnnotPropertyBar).onConfigurationChanged(this.mDrawLocal_tmpF);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setToolHandler(FileAttachmentToolHandler toolHandler) {
        this.mFileAttachmentToolHandler = toolHandler;
    }

    public void modifyAnnotColor(int color) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            this.mModifyColor = color;
            try {
                if (((long) this.mModifyColor) != annot.getBorderColor()) {
                    this.mIsAnnotModified = true;
                    annot.setBorderColor((long) this.mModifyColor);
                    annot.resetAppearanceStream();
                    invalidateForToolModify(annot);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void modifyAnnotOpacity(int opacity) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && (annot instanceof FileAttachment)) {
            this.mModifyOpacity = ((float) AppDmUtil.opacity100To255(opacity)) / 255.0f;
            try {
                if (this.mModifyOpacity != ((FileAttachment) annot).getOpacity()) {
                    this.mIsAnnotModified = true;
                    ((FileAttachment) annot).setOpacity(this.mModifyOpacity);
                    annot.resetAppearanceStream();
                    invalidateForToolModify(annot);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void modifyIconType(int type) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            this.mModifyIconName = FileAttachmentUtil.getIconNames()[type];
            try {
                if (this.mModifyIconName != ((FileAttachment) annot).getIconName()) {
                    this.mIsAnnotModified = true;
                    ((FileAttachment) annot).setIconName(this.mModifyIconName);
                    annot.resetAppearanceStream();
                    invalidateForToolModify(annot);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    private void invalidateForToolModify(Annot annot) {
        if (annot != null) {
            try {
                int pageIndex = annot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                    RectF rectF = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
                    Rect rect = rectRoundOut(rectF, this.mBBoxSpace);
                    rect.inset(-1, -1);
                    this.mPdfViewCtrl.refresh(pageIndex, rect);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    private Rect rectRoundOut(RectF rectF, int roundSize) {
        Rect rect = new Rect();
        rectF.roundOut(rect);
        rect.inset(-roundSize, -roundSize);
        return rect;
    }

    public void DeleteAnnot(Annot annot, Boolean addUndo, Callback result) {
        DocumentManager dm_Doc = DocumentManager.getInstance(this.mPdfViewCtrl);
        if (annot == dm_Doc.getCurrentAnnot()) {
            dm_Doc.setCurrentAnnot(null);
        }
        try {
            PDFPage page = annot.getPage();
            if (page != null) {
                final RectF annotRectF = annot.getRect();
                final int pageIndex = page.getIndex();
                DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
                final FileAttachmentDeleteUndoItem undoItem = new FileAttachmentDeleteUndoItem(this.mPdfViewCtrl);
                undoItem.setCurrentValue(annot);
                undoItem.mIconName = ((FileAttachment) annot).getIconName();
                undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
                undoItem.mPath = this.mFileAttachmentToolHandler.getAttachmentPath(annot);
                final Boolean bool = addUndo;
                final Callback callback = result;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new FileAttachmentEvent(3, undoItem, (FileAttachment) annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            if (bool.booleanValue()) {
                                DocumentManager.getInstance(FileAttachmentAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            }
                            if (FileAttachmentAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                                FileAttachmentAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
                                FileAttachmentAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(annotRectF));
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
            if (e.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    public void ModifyAnnot(Annot annot, int color, float opacity, String iconName, String modifyDate, final boolean addUndo, final Callback callback) {
        try {
            if (annot.getPage() != null) {
                final FileAttachmentModifyUndoItem undoItem = new FileAttachmentModifyUndoItem(this.mPdfViewCtrl);
                undoItem.setCurrentValue(annot);
                undoItem.mIconName = iconName;
                undoItem.mRedoColor = color;
                undoItem.mRedoOpacity = opacity;
                undoItem.mRedoIconName = iconName;
                undoItem.mRedoBbox = annot.getRect();
                undoItem.mUndoColor = this.mTmpUndoColor;
                undoItem.mUndoOpacity = this.mTmpUndoOpacity;
                undoItem.mUndoIconName = this.mTmpUndoIconName;
                undoItem.mUndoBbox = this.mTmpUndoBbox;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new FileAttachmentEvent(2, undoItem, (FileAttachment) annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            if (addUndo) {
                                DocumentManager.getInstance(FileAttachmentAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            }
                            if (callback != null) {
                                callback.result(event, success);
                            }
                        }
                    }
                }));
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void initOpenView() {
        this.mOpenView = View.inflate(this.mContext, R.layout.attachment_view, null);
        this.mOpenView_titleLy = (LinearLayout) this.mOpenView.findViewById(R.id.attachment_view_topbar_ly);
        this.mOpenView_contentLy = (LinearLayout) this.mOpenView.findViewById(R.id.attachment_view_content_ly);
        this.mOpenView_backIV = (ImageView) this.mOpenView.findViewById(R.id.attachment_view_topbar_back);
        this.mOpenView_filenameTV = (TextView) this.mOpenView.findViewById(R.id.attachment_view_topbar_name);
        this.mParent.addView(this.mOpenView);
        this.mOpenView.setVisibility(8);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            ((LayoutParams) this.mOpenView_titleLy.getLayoutParams()).setMargins(AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_horz_left_margin_pad), 0, AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_horz_right_margin_pad), 0);
        }
        this.mOpenView_backIV.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FileAttachmentAnnotHandler.this.mOpenView.setVisibility(8);
                FileAttachmentAnnotHandler.this.onAttachmentDocWillClose();
                FileAttachmentAnnotHandler.this.mAttachPdfViewCtrl.closeDoc();
            }
        });
        this.mOpenView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public boolean isAttachmentOpening() {
        return this.mbAttachengOpening;
    }

    protected void registerAttachmentDocEventListener(IAttachmentDocEvent listener) {
        this.mAttachDocEventListeners.add(listener);
    }

    private void onAttachmentDocWillOpen() {
        Iterator it = this.mAttachDocEventListeners.iterator();
        while (it.hasNext()) {
            ((IAttachmentDocEvent) it.next()).onAttachmentDocWillOpen();
        }
    }

    private void onAttachmentDocOpened(PDFDoc document, int errCode) {
        Iterator it = this.mAttachDocEventListeners.iterator();
        while (it.hasNext()) {
            ((IAttachmentDocEvent) it.next()).onAttachmentDocOpened(document, errCode);
        }
    }

    private void onAttachmentDocWillClose() {
        Iterator it = this.mAttachDocEventListeners.iterator();
        while (it.hasNext()) {
            ((IAttachmentDocEvent) it.next()).onAttachmentDocWillClose();
        }
    }

    private void onAttachmentDocClosed() {
        Iterator it = this.mAttachDocEventListeners.iterator();
        while (it.hasNext()) {
            ((IAttachmentDocEvent) it.next()).onAttachmentDocClosed();
        }
    }

    public void openAttachment(String filePath) {
        onAttachmentDocWillOpen();
        this.mAttachPdfViewCtrl = new PDFViewCtrl(this.mContext);
        this.mOpenView_filenameTV.setText(filePath.substring(filePath.lastIndexOf(47) + 1));
        this.mOpenView_contentLy.removeAllViews();
        this.mOpenView_contentLy.addView(this.mAttachPdfViewCtrl);
        this.mOpenView.setVisibility(0);
        this.mAttachPdfViewCtrl.registerDocEventListener(new IDocEventListener() {
            public void onDocWillOpen() {
            }

            public void onDocOpened(PDFDoc document, int errCode) {
                if (errCode == PDFError.NO_ERROR.getCode()) {
                    FileAttachmentAnnotHandler.this.mAttachPdfViewCtrl.setPageLayoutMode(2);
                    FileAttachmentAnnotHandler.this.mbAttachengOpening = true;
                } else {
                    Toast.makeText(FileAttachmentAnnotHandler.this.mContext, R.string.rv_document_open_failed, 0).show();
                }
                FileAttachmentAnnotHandler.this.onAttachmentDocOpened(document, errCode);
            }

            public void onDocWillClose(PDFDoc document) {
            }

            public void onDocClosed(PDFDoc document, int errCode) {
                FileAttachmentAnnotHandler.this.onAttachmentDocClosed();
            }

            public void onDocWillSave(PDFDoc document) {
            }

            public void onDocSaved(PDFDoc document, int errCode) {
            }
        });
        this.mAttachPdfViewCtrl.openDoc(filePath, null);
    }

    private void closeAttachment() {
        if (this.mbAttachengOpening) {
            this.mbAttachengOpening = false;
            onAttachmentDocWillClose();
            this.mAttachPdfViewCtrl.closeDoc();
        }
    }

    private void _onOpenAttachment(Annot annot) {
        try {
            String fileName = ((FileAttachment) annot).getFileSpec().getFileName();
            String tmpPath = getTMP_PATH();
            String uuid = annot.getUniqueID();
            if (uuid == null) {
                uuid = AppDmUtil.randomUUID("");
            }
            tmpPath = new StringBuilder(String.valueOf(tmpPath)).append(uuid).append("/").toString();
            new File(tmpPath).mkdirs();
            final String newFilePath = new StringBuilder(String.valueOf(tmpPath)).append(fileName).toString();
            FileAttachmentUtil.saveAttachment(this.mPdfViewCtrl, newFilePath, annot, new Callback() {
                public void result(Event event, boolean success) {
                    if (!success) {
                        return;
                    }
                    if (newFilePath.substring(newFilePath.lastIndexOf(46) + 1).toLowerCase().equals("pdf")) {
                        FileAttachmentAnnotHandler.this.openAttachment(newFilePath);
                    } else {
                        AppIntentUtil.openFile((Activity) FileAttachmentAnnotHandler.this.mContext, newFilePath);
                    }
                }
            });
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }
}
