package com.foxit.uiextensions.annots.freetext.typewriter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.DefaultAppearance;
import com.foxit.sdk.common.Font;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FreeText;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.freetext.FtTextUtil;
import com.foxit.uiextensions.annots.freetext.FtTextUtil.OnTextValuesChangedListener;
import com.foxit.uiextensions.annots.freetext.FtUtil;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;
import org.achartengine.chart.TimeChart;

public class TypewriterAnnotHandler implements AnnotHandler {
    private float deltaHeight = 0.0f;
    private float deltaWidth = 0.0f;
    private boolean isDeleteAnnot = false;
    private AnnotMenu mAnnotMenu;
    private float mBBoxHeight;
    private int mBBoxSpace;
    private float mBBoxWidth;
    private Annot mBitmapAnnot;
    private Context mContext;
    private RectF mDocViewBBox = new RectF();
    private PointF mDownPoint;
    private PointF mEditPoint = new PointF(0.0f, 0.0f);
    private boolean mEditState;
    private EditText mEditView;
    private boolean mEditingProperty;
    private boolean mIsSelcetEndText = false;
    private PointF mLastPoint;
    private ArrayList<Integer> mMenuText;
    private boolean mModifyed;
    private int mOffset;
    private Paint mPaintOut;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyBar mPropertyBar;
    private PropertyChangeListener mPropertyChangeListener;
    private RectF mTempLastBBox;
    private int mTempLastColor;
    private ArrayList<String> mTempLastComposedText = new ArrayList();
    private String mTempLastContent;
    private String mTempLastFont;
    private float mTempLastFontSize;
    private int mTempLastOpacity;
    private FtTextUtil mTextUtil;
    private boolean mTouchCaptured = false;

    TypewriterAnnotHandler(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mDownPoint = new PointF();
        this.mLastPoint = new PointF();
        this.mPaintOut = new Paint();
        this.mPaintOut.setAntiAlias(true);
        this.mPaintOut.setStyle(Style.STROKE);
        this.mPaintOut.setPathEffect(AppAnnotUtil.getAnnotBBoxPathEffect());
        this.mPaintOut.setStrokeWidth(AppAnnotUtil.getInstance(context).getAnnotBBoxStrokeWidth());
        this.mMenuText = new ArrayList();
        this.mBBoxSpace = AppAnnotUtil.getAnnotBBoxSpace();
        this.mBitmapAnnot = null;
    }

    public void setAnnotMenu(AnnotMenu annotMenu) {
        this.mAnnotMenu = annotMenu;
    }

    public AnnotMenu getAnnotMenu() {
        return this.mAnnotMenu;
    }

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mPropertyBar = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mPropertyBar;
    }

    public int getType() {
        return 3;
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
        RectF bbox = getAnnotBBox(annot);
        if (bbox == null) {
            return false;
        }
        try {
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, annot.getPage().getIndex());
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return bbox.contains(point.x, point.y);
    }

    public void onAnnotSelected(Annot annot, boolean needInvalid) {
        this.deltaWidth = 0.0f;
        this.deltaHeight = 0.0f;
        this.mTextUtil = new FtTextUtil(this.mContext, this.mPdfViewCtrl);
        this.mEditView = new EditText(this.mContext);
        this.mEditView.setLayoutParams(new LayoutParams(1, 1));
        try {
            this.mEditView.setText(annot.getContent());
            Font oldFont = ((FreeText) annot).getDefaultAppearance().getFont();
            String fontName = "";
            if (oldFont != null) {
                fontName = this.mTextUtil.getSupportFontName(oldFont.getName());
            }
            DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
            if (oldFont == null) {
                da.setFlags(7);
            }
            da.setFont(this.mTextUtil.getSupportFont(fontName));
            ((FreeText) annot).setDefaultAppearance(da);
            DefaultAppearance defaultAppearance = ((FreeText) annot).getDefaultAppearance();
            this.mTempLastColor = (int) defaultAppearance.getTextColor();
            this.mTempLastOpacity = (int) ((((FreeText) annot).getOpacity() * 255.0f) + 0.5f);
            this.mTempLastBBox = annot.getRect();
            this.mTempLastFont = this.mTextUtil.getSupportFontName(defaultAppearance.getFont().getName());
            this.mTempLastFontSize = defaultAppearance.getFontSize();
            this.mTempLastContent = annot.getContent();
            if (this.mTempLastContent == null) {
                this.mTempLastContent = "";
            }
            int pageIndex = annot.getPage().getIndex();
            RectF _rect = annot.getRect();
            RectF mPageViewRect = new RectF(_rect.left, _rect.top, _rect.right, _rect.bottom);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(mPageViewRect, mPageViewRect, pageIndex);
            RectF menuRect = new RectF(mPageViewRect);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(menuRect, menuRect, pageIndex);
            prepareAnnotMenu(annot);
            this.mAnnotMenu.show(menuRect);
            preparePropertyBar();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        final Annot annot2 = annot;
        this.mTextUtil.setOnWidthChanged(new OnTextValuesChangedListener() {
            public void onMaxWidthChanged(float maxWidth) {
                if (TypewriterAnnotHandler.this.mBBoxWidth != maxWidth) {
                    TypewriterAnnotHandler.this.mBBoxWidth = maxWidth;
                    try {
                        RectF textRect = ((FreeText) annot2).getRect();
                        int pageIndex = annot2.getPage().getIndex();
                        TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(textRect, textRect, pageIndex);
                        if (TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex) && TypewriterAnnotHandler.this.mBBoxWidth > textRect.width()) {
                            textRect.set(textRect.left, textRect.top, textRect.left + TypewriterAnnotHandler.this.mBBoxWidth, textRect.bottom);
                            RectF rectChanged = new RectF(textRect);
                            TypewriterAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToPdfRect(textRect, textRect, pageIndex);
                            ((FreeText) annot2).move(textRect);
                            annot2.resetAppearanceStream();
                            rectChanged.inset((float) ((-TypewriterAnnotHandler.this.mBBoxSpace) - TypewriterAnnotHandler.this.mOffset), (float) ((-TypewriterAnnotHandler.this.mBBoxSpace) - TypewriterAnnotHandler.this.mOffset));
                            TypewriterAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectChanged, rectChanged, pageIndex);
                            TypewriterAnnotHandler.this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rectChanged));
                        }
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onMaxHeightChanged(float maxHeight) {
                if (TypewriterAnnotHandler.this.mBBoxHeight != maxHeight) {
                    TypewriterAnnotHandler.this.mBBoxHeight = maxHeight;
                    try {
                        RectF textRect = ((FreeText) annot2).getRect();
                        int pageIndex = annot2.getPage().getIndex();
                        TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(textRect, textRect, pageIndex);
                        if (TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            textRect.set(textRect.left, textRect.top, textRect.right, textRect.top + TypewriterAnnotHandler.this.mBBoxHeight);
                            RectF rectChanged = new RectF(textRect);
                            TypewriterAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToPdfRect(textRect, textRect, pageIndex);
                            ((FreeText) annot2).move(textRect);
                            annot2.resetAppearanceStream();
                            rectChanged.inset((float) ((-TypewriterAnnotHandler.this.mBBoxSpace) - TypewriterAnnotHandler.this.mOffset), (float) ((-TypewriterAnnotHandler.this.mBBoxSpace) - TypewriterAnnotHandler.this.mOffset));
                            TypewriterAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectChanged, rectChanged, pageIndex);
                            TypewriterAnnotHandler.this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rectChanged));
                        }
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onCurrentSelectIndex(int selectIndex) {
                System.err.println("selectindex  " + selectIndex);
                if (selectIndex >= TypewriterAnnotHandler.this.mEditView.getText().length()) {
                    selectIndex = TypewriterAnnotHandler.this.mEditView.getText().length();
                    TypewriterAnnotHandler.this.mIsSelcetEndText = true;
                } else {
                    TypewriterAnnotHandler.this.mIsSelcetEndText = false;
                }
                TypewriterAnnotHandler.this.mEditView.setSelection(selectIndex);
            }

            public void onEditPointChanged(float editPointX, float editPointY) {
                try {
                    int pageIndex = annot2.getPage().getIndex();
                    PointF point = new PointF(editPointX, editPointY);
                    TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfPtToPageViewPt(point, point, pageIndex);
                    TypewriterAnnotHandler.this.mEditPoint.set(point.x, point.y);
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        });
        annot2 = annot;
        this.mEditView.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    annot2.setContent(String.valueOf(s));
                    annot2.resetAppearanceStream();
                    RectF pageViewRect = annot2.getRect();
                    int pageIndex = annot2.getPage().getIndex();
                    TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRect, pageViewRect, pageIndex);
                    RectF pdfRectF = new RectF(pageViewRect.left, pageViewRect.top, pageViewRect.left + TypewriterAnnotHandler.this.mBBoxWidth, pageViewRect.top + TypewriterAnnotHandler.this.mBBoxHeight);
                    RectF rect = new RectF(pdfRectF.left, pdfRectF.top, pdfRectF.left + TypewriterAnnotHandler.this.mBBoxWidth, pdfRectF.top + TypewriterAnnotHandler.this.mBBoxHeight);
                    Rect mRect = new Rect((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom);
                    mRect.inset(-AppDisplay.getInstance(TypewriterAnnotHandler.this.mContext).dp2px(200.0f), -AppDisplay.getInstance(TypewriterAnnotHandler.this.mContext).dp2px(200.0f));
                    if (TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        TypewriterAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(AppDmUtil.rectToRectF(mRect), AppDmUtil.rectToRectF(mRect), pageIndex);
                        TypewriterAnnotHandler.this.mPdfViewCtrl.invalidate(mRect);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        try {
            _rect = annot.getRect();
            RectF viewRect = new RectF(_rect.left, _rect.top, _rect.right, _rect.bottom);
            pageIndex = annot.getPage().getIndex();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                    this.mBitmapAnnot = annot;
                    return;
                }
                return;
            }
            this.mBitmapAnnot = annot;
        } catch (PDFException e2) {
            e2.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean needInvalid) {
        this.mAnnotMenu.dismiss();
        if (this.mEditingProperty) {
            this.mEditingProperty = false;
            this.mPropertyBar.dismiss();
        }
        if (this.isDeleteAnnot) {
            needInvalid = false;
        }
        try {
            PDFPage page = annot.getPage();
            if (page != null) {
                RectF pdfRect = annot.getRect();
                RectF rectF = new RectF(pdfRect.left, pdfRect.top, pdfRect.right, pdfRect.bottom);
                final int pageIndex = page.getIndex();
                DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
                if (!(this.mEditView == null || this.mEditView.getText().toString().equals(this.mTempLastContent))) {
                    RectF pageViewRect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRect, pageViewRect, pageIndex);
                    rectF = new RectF(pageViewRect.left, pageViewRect.top, pageViewRect.left + this.mBBoxWidth, pageViewRect.top + this.mBBoxHeight);
                    this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, rectF, pageIndex);
                    annot.move(new RectF(rectF.left, rectF.top, rectF.right, rectF.bottom));
                    RectF rectF2 = new RectF(rectF);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF2, rectF2, pageIndex);
                    String content = this.mEditView.getText().toString();
                    String font = this.mTextUtil.getSupportFontName(da);
                    float fontSize = da.getFontSize();
                    String annotContent = "";
                    ArrayList<String> composeText = this.mTextUtil.getComposedText(this.mPdfViewCtrl, pageIndex, rectF2, content, font, fontSize);
                    for (int i = 0; i < composeText.size(); i++) {
                        annotContent = new StringBuilder(String.valueOf(annotContent)).append((String) composeText.get(i)).toString();
                        if (i != composeText.size() - 1) {
                            if (annotContent.charAt(annotContent.length() - 1) != '\n') {
                                if (annotContent.charAt(annotContent.length() - 1) != '\r') {
                                    annotContent = new StringBuilder(String.valueOf(annotContent)).append("\r").toString();
                                }
                            }
                        }
                    }
                    modifyAnnot(pageIndex, annot, annot.getRect(), (int) da.getTextColor(), (int) (((FreeText) annot).getOpacity() * 255.0f), font, fontSize, annotContent, false);
                }
                if (!needInvalid || !this.mModifyed) {
                    da.setTextColor((long) this.mTempLastColor);
                    ((FreeText) annot).setOpacity(((float) this.mTempLastOpacity) / 255.0f);
                    annot.move(this.mTempLastBBox);
                    da.setFontSize(this.mTempLastFontSize);
                    da.setFont(this.mTextUtil.getSupportFont(this.mTempLastFont));
                    ((FreeText) annot).setDefaultAppearance(da);
                    annot.setContent(this.mTempLastContent);
                    annot.resetAppearanceStream();
                } else if (((long) this.mTempLastColor) == da.getTextColor() && this.mTempLastOpacity == ((int) (((FreeText) annot).getOpacity() * 255.0f)) && this.mTempLastBBox.equals(Boolean.valueOf(annot.resetAppearanceStream())) && this.mTempLastContent.equals(annot.getContent()) && this.mTempLastFontSize == da.getFontSize() && this.mTempLastFont == this.mTextUtil.getSupportFontName(da)) {
                    modifyAnnot(pageIndex, annot, annot.getRect(), (int) da.getTextColor(), (int) ((((FreeText) annot).getOpacity() * 255.0f) + 0.5f), this.mTextUtil.getSupportFontName(da), da.getFontSize(), annot.getContent(), false);
                } else {
                    modifyAnnot(pageIndex, annot, annot.getRect(), (int) da.getTextColor(), (int) ((((FreeText) annot).getOpacity() * 255.0f) + 0.5f), this.mTextUtil.getSupportFontName(da), da.getFontSize(), annot.getContent(), true);
                }
                if (this.mPdfViewCtrl.isPageVisible(pageIndex) && needInvalid) {
                    final RectF rect = annot.getRect();
                    CallBack anonymousClass3 = new CallBack() {
                        public void result(Task task) {
                            if (TypewriterAnnotHandler.this.mBitmapAnnot != DocumentManager.getInstance(TypewriterAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                                TypewriterAnnotHandler.this.mBitmapAnnot = null;
                                AppUtil.dismissInputSoft(TypewriterAnnotHandler.this.mEditView);
                                TypewriterAnnotHandler.this.mParent.removeView(TypewriterAnnotHandler.this.mEditView);
                                TypewriterAnnotHandler.this.mEditState = false;
                                TypewriterAnnotHandler.this.mTextUtil.getBlink().removeCallbacks((Runnable) TypewriterAnnotHandler.this.mTextUtil.getBlink());
                                TypewriterAnnotHandler.this.mBBoxWidth = 0.0f;
                                TypewriterAnnotHandler.this.mBBoxHeight = 0.0f;
                                TypewriterAnnotHandler.this.mEditPoint.set(0.0f, 0.0f);
                                TypewriterAnnotHandler.this.mPdfViewCtrl.layout(0, 0, TypewriterAnnotHandler.this.mPdfViewCtrl.getWidth(), TypewriterAnnotHandler.this.mPdfViewCtrl.getHeight());
                                if (!TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                                    return;
                                }
                                if ((pageIndex == TypewriterAnnotHandler.this.mPdfViewCtrl.getPageCount() - 1 || TypewriterAnnotHandler.this.mPdfViewCtrl.getPageLayoutMode() == 1) && pageIndex == TypewriterAnnotHandler.this.mPdfViewCtrl.getCurrentPage()) {
                                    PointF endPoint = new PointF((float) TypewriterAnnotHandler.this.mPdfViewCtrl.getPageViewWidth(pageIndex), (float) TypewriterAnnotHandler.this.mPdfViewCtrl.getPageViewHeight(pageIndex));
                                    TypewriterAnnotHandler.this.mPdfViewCtrl.convertPageViewPtToDisplayViewPt(endPoint, endPoint, pageIndex);
                                    if (((float) AppDisplay.getInstance(TypewriterAnnotHandler.this.mContext).getRawScreenHeight()) - (endPoint.y - TypewriterAnnotHandler.this.mTextUtil.getKeyboardOffset()) > 0.0f) {
                                        TypewriterAnnotHandler.this.mPdfViewCtrl.layout(0, 0, TypewriterAnnotHandler.this.mPdfViewCtrl.getWidth(), TypewriterAnnotHandler.this.mPdfViewCtrl.getHeight());
                                        TypewriterAnnotHandler.this.mTextUtil.setKeyboardOffset(0);
                                        TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, pageIndex);
                                        PointF startPoint = new PointF(rect.left, rect.top);
                                        TypewriterAnnotHandler.this.mPdfViewCtrl.gotoPage(pageIndex, TypewriterAnnotHandler.this.mTextUtil.getPageViewOrigin(TypewriterAnnotHandler.this.mPdfViewCtrl, pageIndex, startPoint.x, startPoint.y).x, TypewriterAnnotHandler.this.mTextUtil.getPageViewOrigin(TypewriterAnnotHandler.this.mPdfViewCtrl, pageIndex, startPoint.x, startPoint.y).y);
                                    }
                                }
                            }
                        }
                    };
                    rect = rectF;
                    this.mPdfViewCtrl.addTask(new Task(anonymousClass3) {
                        protected void execute() {
                            TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, pageIndex);
                            TypewriterAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(rect));
                        }
                    });
                } else {
                    this.mBitmapAnnot = null;
                    AppUtil.dismissInputSoft(this.mEditView);
                    this.mParent.removeView(this.mEditView);
                    this.mEditState = false;
                    this.mTextUtil.getBlink().removeCallbacks((Runnable) this.mTextUtil.getBlink());
                    this.mBBoxWidth = 0.0f;
                    this.mBBoxHeight = 0.0f;
                    this.mEditPoint.set(0.0f, 0.0f);
                }
            } else {
                this.mBitmapAnnot = null;
                AppUtil.dismissInputSoft(this.mEditView);
                this.mParent.removeView(this.mEditView);
                this.mEditState = false;
                this.mTextUtil.getBlink().removeCallbacks((Runnable) this.mTextUtil.getBlink());
                this.mBBoxWidth = 0.0f;
                this.mBBoxHeight = 0.0f;
                this.mEditPoint.set(0.0f, 0.0f);
            }
            this.mModifyed = false;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void preparePropertyBar() {
        FreeText curAnnot = null;
        if (DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() instanceof FreeText) {
            curAnnot = (FreeText) DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        }
        if (curAnnot == null) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            return;
        }
        int[] colors = new int[PropertyBar.PB_COLORS_TYPEWRITER.length];
        System.arraycopy(PropertyBar.PB_COLORS_TYPEWRITER, 0, colors, 0, colors.length);
        colors[0] = PropertyBar.PB_COLORS_TYPEWRITER[0];
        this.mPropertyBar.setColors(colors);
        try {
            DefaultAppearance da = curAnnot.getDefaultAppearance();
            this.mPropertyBar.setProperty(1, (float) da.getTextColor());
            this.mPropertyBar.setProperty(2, AppDmUtil.opacity255To100((int) ((curAnnot.getOpacity() * 255.0f) + 0.5f)));
            this.mPropertyBar.setProperty(8, this.mTextUtil.getSupportFontName(da));
            this.mPropertyBar.setProperty(16, da.getFontSize());
        } catch (PDFException e) {
            e.printStackTrace();
        }
        this.mPropertyBar.setArrowVisible(false);
        this.mPropertyBar.reset(getSupportedProperties());
        this.mPropertyBar.setPropertyChangeListener(this.mPropertyChangeListener);
    }

    private long getSupportedProperties() {
        return 27;
    }

    private void prepareAnnotMenu(final Annot annot) {
        resetAnnotationMenuResource(annot);
        this.mAnnotMenu.setMenuItems(this.mMenuText);
        this.mAnnotMenu.setListener(new ClickListener() {
            public void onAMClick(int btType) {
                if (btType == 2) {
                    if (annot == DocumentManager.getInstance(TypewriterAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                        TypewriterAnnotHandler.this.deleteAnnot(annot, true, null);
                    }
                } else if (btType == 5) {
                    TypewriterAnnotHandler.this.mAnnotMenu.dismiss();
                    TypewriterAnnotHandler.this.mParent.addView(TypewriterAnnotHandler.this.mEditView);
                    TypewriterAnnotHandler.this.mTextUtil.getBlink().postDelayed((Runnable) TypewriterAnnotHandler.this.mTextUtil.getBlink(), 500);
                    TypewriterAnnotHandler.this.mEditView.setSelection(TypewriterAnnotHandler.this.mEditView.getText().length());
                    AppUtil.showSoftInput(TypewriterAnnotHandler.this.mEditView);
                    TypewriterAnnotHandler.this.mEditState = true;
                    try {
                        int pageIndex = annot.getPage().getIndex();
                        RectF rectF = annot.getRect();
                        RectF viewRect = new RectF(rectF.left, rectF.top, rectF.right, rectF.bottom);
                        if (TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                            TypewriterAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(viewRect, viewRect, pageIndex);
                            TypewriterAnnotHandler.this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(viewRect));
                        }
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                } else if (btType == 6) {
                    TypewriterAnnotHandler.this.mPropertyBar.show(TypewriterAnnotHandler.this.mDocViewBBox, false);
                    TypewriterAnnotHandler.this.mAnnotMenu.dismiss();
                }
            }
        });
    }

    private void resetAnnotationMenuResource(Annot annot) {
        this.mMenuText.clear();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mMenuText.add(Integer.valueOf(5));
            this.mMenuText.add(Integer.valueOf(6));
            this.mMenuText.add(Integer.valueOf(2));
        }
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        TypewriterAnnotContent lContent = (TypewriterAnnotContent) content;
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            final FreeText annot = (FreeText) page.addAnnot(3, content.getBBox());
            final TypewriterAddUndoItem undoItem = new TypewriterAddUndoItem(this.mPdfViewCtrl);
            undoItem.setCurrentValue((AnnotContent) lContent);
            undoItem.mPageIndex = pageIndex;
            undoItem.mFont = this.mTextUtil.getSupportFont(lContent.getFontName());
            undoItem.mFontSize = lContent.getFontSize();
            undoItem.mTextColor = (long) lContent.getColor();
            undoItem.mDaFlags = 7;
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mFlags = 4;
            undoItem.mIntent = "FreeTextTypewriter";
            final boolean z = addUndo;
            final int i = pageIndex;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new TypewriterEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(TypewriterAnnotHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (z) {
                            DocumentManager.getInstance(TypewriterAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        }
                        try {
                            if (TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                                RectF viewRect = annot.getRect();
                                TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, i);
                                Rect rect = new Rect();
                                viewRect.roundOut(rect);
                                rect.inset(-10, -10);
                                TypewriterAnnotHandler.this.mPdfViewCtrl.refresh(i, rect);
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
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        deleteAnnot(annot, addUndo, result);
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e, Annot annot) {
        if (!DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            return false;
        }
        PointF pointF = new PointF(e.getX(), e.getY());
        PointF point = new PointF();
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pointF, point, pageIndex);
        float evX = point.x;
        float evY = point.y;
        RectF pageViewRect;
        RectF rectChanged;
        RectF rectF;
        switch (e.getAction()) {
            case 0:
                try {
                    if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() || pageIndex != annot.getPage().getIndex() || !isHitAnnot(annot, point) || this.mEditState) {
                        return false;
                    }
                    this.mDownPoint.set(evX, evY);
                    this.mLastPoint.set(evX, evY);
                    this.mTouchCaptured = true;
                    return true;
                } catch (PDFException e1) {
                    e1.printStackTrace();
                    break;
                }
            case 1:
                if (this.mTouchCaptured && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && pageIndex == annot.getPage().getIndex()) {
                    pageViewRect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRect, pageViewRect, pageIndex);
                    rectChanged = new RectF(pageViewRect);
                    rectChanged.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    rectF = new RectF(rectChanged);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                    if (!this.mEditingProperty) {
                        if (this.mAnnotMenu.isShowing()) {
                            this.mAnnotMenu.update(rectF);
                        } else {
                            this.mAnnotMenu.show(rectF);
                        }
                    }
                    this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectChanged, rectChanged, pageIndex);
                    DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
                    if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                        RectF _rect = new RectF(rectChanged);
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(_rect, _rect, pageIndex);
                        _rect.right += this.deltaWidth;
                        _rect.bottom -= this.deltaHeight;
                        String font = this.mTextUtil.getSupportFontName(da);
                        float fontSize = da.getFontSize();
                        ArrayList<String> composeText = this.mTextUtil.getComposedText(this.mPdfViewCtrl, pageIndex, _rect, annot.getContent(), font, fontSize);
                        String annotContent = "";
                        for (int i = 0; i < composeText.size(); i++) {
                            annotContent = new StringBuilder(String.valueOf(annotContent)).append((String) composeText.get(i)).toString();
                            char ch = annotContent.charAt(annotContent.length() - 1);
                            if (!(i == composeText.size() - 1 || ch == '\n' || ch == '\r')) {
                                annotContent = new StringBuilder(String.valueOf(annotContent)).append("\r").toString();
                            }
                        }
                        this.mEditView.setText(annotContent);
                        modifyAnnot(pageIndex, annot, rectChanged, (int) da.getTextColor(), (int) (((FreeText) annot).getOpacity() * 255.0f), font, fontSize, annotContent, false);
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
                if (!this.mTouchCaptured || pageIndex != annot.getPage().getIndex() || annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() || this.mEditState) {
                    return false;
                }
                if (!(evX == this.mLastPoint.x && evY == this.mLastPoint.y)) {
                    pageViewRect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRect, pageViewRect, pageIndex);
                    pageViewRect.set(pageViewRect.left - ((float) this.mOffset), pageViewRect.top, (pageViewRect.left + this.mBBoxWidth) + ((float) this.mOffset), pageViewRect.top + this.mBBoxHeight);
                    rectF = new RectF(pageViewRect);
                    rectChanged = new RectF(pageViewRect);
                    rectF.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    rectChanged.offset(evX - this.mDownPoint.x, evY - this.mDownPoint.y);
                    float deltaXY = FtUtil.widthOnPageView(this.mPdfViewCtrl, annot.getPage().getIndex(), 2.0f);
                    float adjustx = 0.0f;
                    float adjusty = 0.0f;
                    if (rectChanged.left < deltaXY) {
                        adjustx = (-rectChanged.left) + deltaXY;
                    }
                    if (rectChanged.top < deltaXY) {
                        adjusty = (-rectChanged.top) + deltaXY;
                    }
                    if (rectChanged.right > ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - deltaXY) {
                        adjustx = (((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - rectChanged.right) - deltaXY;
                    }
                    if (rectChanged.bottom > ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - deltaXY) {
                        adjusty = (((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - rectChanged.bottom) - deltaXY;
                    }
                    if (rectChanged.top < deltaXY && rectChanged.bottom > ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - deltaXY) {
                        adjusty = (-rectChanged.top) + deltaXY;
                    }
                    rectChanged.offset(adjustx, adjusty);
                    rectF.union(rectChanged);
                    rectF.inset((float) ((-this.mBBoxSpace) - this.mOffset), (float) ((-this.mBBoxSpace) - this.mOffset));
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                    this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rectF));
                    rectF = new RectF(rectChanged);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                    if (this.mAnnotMenu.isShowing()) {
                        this.mAnnotMenu.dismiss();
                        this.mAnnotMenu.update(rectF);
                    }
                    if (this.mEditingProperty) {
                        this.mPropertyBar.dismiss();
                    }
                    this.mLastPoint.set(evX, evY);
                    this.mLastPoint.offset(adjustx, adjusty);
                }
                return true;
            case 3:
                this.mTouchCaptured = false;
                this.mDownPoint.set(0.0f, 0.0f);
                this.mLastPoint.set(0.0f, 0.0f);
                this.mEditPoint.set(0.0f, 0.0f);
                return false;
            default:
                return false;
        }
        e1.printStackTrace();
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        PointF point = new PointF(motionEvent.getX(), motionEvent.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        return onSingleTapOrLongPress(pageIndex, point, annot);
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        PointF point = new PointF(motionEvent.getX(), motionEvent.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        return onSingleTapOrLongPress(pageIndex, point, annot);
    }

    private boolean onSingleTapOrLongPress(int pageIndex, PointF point, Annot annot) {
        try {
            if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
                return true;
            } else if (pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, point) && this.mEditState) {
                PointF pointF = new PointF(point.x, point.y);
                this.mPdfViewCtrl.convertPageViewPtToPdfPt(pointF, pointF, pageIndex);
                this.mEditPoint.set(pointF.x, pointF.y);
                this.mTextUtil.resetEditState();
                pageViewRect = annot.getRect();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRect, pageViewRect, pageIndex);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(pageViewRect, pageViewRect, pageIndex);
                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(pageViewRect));
                AppUtil.showSoftInput(this.mEditView);
                return true;
            } else if (pageIndex != annot.getPage().getIndex() || isHitAnnot(annot, point) || this.mEditView == null || this.mEditView.getText().toString().equals(annot.getContent())) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                return true;
            } else {
                pageViewRect = annot.getRect();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRect, pageViewRect, pageIndex);
                RectF rectF = new RectF(pageViewRect.left, pageViewRect.top, pageViewRect.left + this.mBBoxWidth, pageViewRect.top + this.mBBoxHeight);
                this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, rectF, pageIndex);
                annot.move(new RectF(rectF.left, rectF.top, rectF.right, rectF.bottom));
                DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
                modifyAnnot(pageIndex, annot, annot.getRect(), (int) da.getTextColor(), (int) (((FreeText) annot).getOpacity() * 255.0f), this.mTextUtil.getSupportFontName(da), da.getFontSize(), this.mEditView.getText().toString(), false);
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
        if (annot != null && (annot instanceof FreeText) && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                if (annot.getType() == 3 && this.mBitmapAnnot == annot && annot.getPage().getIndex() == pageIndex) {
                    canvas.save();
                    Rect rect1 = new Rect(0, 0, 10, 0);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(AppDmUtil.rectToRectF(rect1), AppDmUtil.rectToRectF(rect1), pageIndex);
                    this.mOffset = rect1.width();
                    RectF frameRectF = new RectF();
                    RectF rect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, pageIndex);
                    rect.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    PointF editPoint = new PointF(this.mEditPoint.x, this.mEditPoint.y);
                    if (!(editPoint.x == 0.0f && editPoint.y == 0.0f)) {
                        this.mPdfViewCtrl.convertPdfPtToPageViewPt(editPoint, editPoint, pageIndex);
                    }
                    this.mTextUtil.setTextString(pageIndex, annot.getContent(), this.mEditState);
                    this.mTextUtil.setStartPoint(new PointF(rect.left, rect.top));
                    this.mTextUtil.setEditPoint(editPoint);
                    if (this.mEditState) {
                        this.mTextUtil.setMaxRect(((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - rect.left, ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - rect.top);
                    } else {
                        this.mTextUtil.setMaxRect(rect.width() + this.deltaWidth, ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - rect.top);
                    }
                    DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
                    this.mTextUtil.setTextColor((int) da.getTextColor(), AppDmUtil.opacity100To255((int) (((FreeText) annot).getOpacity() * 100.0f)));
                    this.mTextUtil.setFont(this.mTextUtil.getSupportFontName(da), da.getFontSize());
                    if (this.mIsSelcetEndText) {
                        this.mTextUtil.setEndSelection(this.mEditView.getSelectionEnd() + 1);
                    } else {
                        this.mTextUtil.setEndSelection(this.mEditView.getSelectionEnd());
                    }
                    this.mTextUtil.loadText();
                    this.mTextUtil.DrawText(canvas);
                    if (!this.mEditState) {
                        frameRectF.set(rect.left - ((float) this.mOffset), rect.top, (rect.left + this.mBBoxWidth) + ((float) this.mOffset), rect.top + this.mBBoxHeight);
                        this.mPaintOut.setColor(((int) da.getTextColor()) | -16777216);
                        canvas.drawRect(frameRectF, this.mPaintOut);
                    }
                    canvas.restore();
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this && !this.mEditState) {
            try {
                this.mDocViewBBox = curAnnot.getRect();
                int pageIndex = curAnnot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mDocViewBBox, this.mDocViewBBox, pageIndex);
                    this.mDocViewBBox.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mDocViewBBox, this.mDocViewBBox, pageIndex);
                    this.mAnnotMenu.update(this.mDocViewBBox);
                    if (this.mPropertyBar.isShowing()) {
                        this.mPropertyBar.update(this.mDocViewBBox);
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onColorValueChanged(int color) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        try {
            DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
            if (annot != null && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this && color != ((int) da.getTextColor())) {
                modifyAnnot(annot.getPage().getIndex(), annot, annot.getRect(), color, (int) (((FreeText) annot).getOpacity() * 255.0f), this.mTextUtil.getSupportFontName(da), da.getFontSize(), annot.getContent(), false);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onOpacityValueChanged(int opacity) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this && AppDmUtil.opacity100To255(opacity) != ((int) (((FreeText) annot).getOpacity() * 255.0f))) {
                    int pageIndex = annot.getPage().getIndex();
                    DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
                    modifyAnnot(pageIndex, annot, annot.getRect(), (int) da.getTextColor(), AppDmUtil.opacity100To255(opacity), this.mTextUtil.getSupportFontName(da), da.getFontSize(), annot.getContent(), false);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onFontValueChanged(String font) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        try {
            DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
            if (annot != null && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this && font != this.mTextUtil.getSupportFontName(da)) {
                int pageIndex = annot.getPage().getIndex();
                RectF rectF = annot.getRect();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
                if (rectF.width() < this.mTextUtil.getFontWidth(this.mPdfViewCtrl, pageIndex, font, da.getFontSize())) {
                    rectF.set(rectF.left, rectF.top, rectF.left + this.mTextUtil.getFontWidth(this.mPdfViewCtrl, pageIndex, font, da.getFontSize()), rectF.bottom);
                }
                RectF rectChanged = new RectF(rectF);
                rectF.inset((float) ((-this.mBBoxSpace) - this.mOffset), (float) ((-this.mBBoxSpace) - this.mOffset));
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rectF));
                this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectChanged, rectChanged, pageIndex);
                modifyAnnot(pageIndex, annot, rectChanged, (int) da.getTextColor(), (int) (((FreeText) annot).getOpacity() * 255.0f), font, da.getFontSize(), annot.getContent(), false);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onFontSizeValueChanged(float fontSize) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        try {
            DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
            if (annot != null && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this && fontSize != da.getFontSize()) {
                int pageIndex = annot.getPage().getIndex();
                RectF rectF = annot.getRect();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
                if (rectF.width() < this.mTextUtil.getFontWidth(this.mPdfViewCtrl, pageIndex, this.mTextUtil.getSupportFontName(da), fontSize)) {
                    rectF.set(rectF.left, rectF.top, rectF.left + this.mTextUtil.getFontWidth(this.mPdfViewCtrl, pageIndex, this.mTextUtil.getSupportFontName(da), fontSize), rectF.bottom);
                }
                RectF rectChanged = new RectF(rectF);
                rectF.inset((float) ((-this.mBBoxSpace) - this.mOffset), (float) ((-this.mBBoxSpace) - this.mOffset));
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rectF));
                this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectChanged, rectChanged, pageIndex);
                modifyAnnot(pageIndex, annot, rectChanged, (int) da.getTextColor(), (int) (((FreeText) annot).getOpacity() * 255.0f), this.mTextUtil.getSupportFontName(da), fontSize, annot.getContent(), false);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void modifyAnnot(int pageIndex, Annot annot, RectF bbox, int color, int opacity, String font, float fontSize, String content, boolean isModifyJni) {
        if (this.mTextUtil == null) {
            this.mTextUtil = new FtTextUtil(this.mContext, this.mPdfViewCtrl);
        }
        modifyAnnot(pageIndex, (FreeText) annot, bbox, color, opacity, font, fontSize, content, isModifyJni, true, "FreeTextTypewriter", null);
    }

    private void deleteAnnot(Annot annot, boolean addUndo, Callback result) {
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            this.isDeleteAnnot = true;
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            AppUtil.dismissInputSoft(this.mEditView);
            this.mParent.removeView(this.mEditView);
            this.mTextUtil.getBlink().removeCallbacks((Runnable) this.mTextUtil.getBlink());
        }
        try {
            PDFPage page = annot.getPage();
            final int pageIndex = page.getIndex();
            if (this.mTextUtil == null) {
                this.mTextUtil = new FtTextUtil(this.mContext, this.mPdfViewCtrl);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF viewRect = annot.getRect();
            final TypewriterDeleteUndoItem undoItem = new TypewriterDeleteUndoItem(this.mPdfViewCtrl);
            undoItem.setCurrentValue(annot);
            DefaultAppearance da = ((FreeText) annot).getDefaultAppearance();
            undoItem.mFont = da.getFont();
            String fontName = "";
            if (undoItem.mFont != null) {
                fontName = this.mTextUtil.getSupportFontName(undoItem.mFont.getName());
            }
            undoItem.mFont = this.mTextUtil.getSupportFont(fontName);
            undoItem.mFontSize = da.getFontSize();
            undoItem.mTextColor = da.getTextColor();
            undoItem.mDaFlags = da.getFlags();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mIntent = ((FreeText) annot).getIntent();
            final boolean z = addUndo;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new TypewriterEvent(3, undoItem, (FreeText) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    TypewriterAnnotHandler.this.isDeleteAnnot = false;
                    if (success) {
                        if (z) {
                            DocumentManager.getInstance(TypewriterAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        }
                        if (TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                            TypewriterAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
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

    protected void deleteAnnot(Annot annot, TypewriterDeleteUndoItem undoItem, final Callback result) {
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            this.isDeleteAnnot = true;
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        try {
            PDFPage page = annot.getPage();
            final int pageIndex = page.getIndex();
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF viewRect = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new TypewriterEvent(3, undoItem, (FreeText) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    TypewriterAnnotHandler.this.isDeleteAnnot = false;
                    if (success && TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                        TypewriterAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                    }
                    if (result != null) {
                        result.result(null, success);
                    }
                }
            }));
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        if (content != null) {
            modifyAnnot(annot, (TypewriterAnnotContent) content, addUndo, result);
        } else if (result != null) {
            result.result(null, false);
        }
    }

    private void modifyAnnot(Annot annot, TypewriterAnnotContent content, boolean addUndo, Callback result) {
        FreeText lAnnot = (FreeText) annot;
        if (this.mTextUtil == null) {
            this.mTextUtil = new FtTextUtil(this.mContext, this.mPdfViewCtrl);
        }
        try {
            String contents;
            String fontName;
            float fontSize;
            int pageIndex = annot.getPage().getIndex();
            if (content.getContents() == null || content.getContents().equals("")) {
                contents = " ";
            } else {
                contents = content.getContents();
            }
            contents = FtTextUtil.filterEmoji(contents);
            if (content.getFontName() == null || content.getFontName().equals("")) {
                fontName = "Courier";
            } else if (content.getFontName().startsWith("Cour") || content.getFontName().equalsIgnoreCase("Courier") || content.getFontName().startsWith("Helv") || !content.getFontName().equalsIgnoreCase("Helvetica") || content.getFontName().startsWith(TimeChart.TYPE) || content.getFontName().equalsIgnoreCase("Times")) {
                fontName = content.getFontName();
            } else {
                fontName = "Courier";
            }
            if (content.getFontSize() == 0.0f) {
                fontSize = 24.0f;
            } else {
                fontSize = content.getFontSize();
            }
            boolean z = addUndo;
            modifyAnnot(pageIndex, lAnnot, annot.getRect(), (int) lAnnot.getDefaultAppearance().getTextColor(), (int) (lAnnot.getOpacity() * 255.0f), fontName, fontSize, contents, true, z, "", result);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected void modifyAnnot(int pageIndex, Annot annot, RectF bbox, int color, int opacity, String fontName, float fontSize, String content, boolean isModifyJni, boolean addUndo, String fromType, Callback result) {
        final TypewriterModifyUndoItem undoItem = new TypewriterModifyUndoItem(this.mPdfViewCtrl);
        undoItem.setCurrentValue(annot);
        undoItem.mPageIndex = pageIndex;
        undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
        undoItem.mColor = (long) color;
        undoItem.mOpacity = ((float) opacity) / 255.0f;
        undoItem.mBBox = new RectF(bbox);
        if (content == null) {
            content = "";
        }
        undoItem.mContents = content;
        undoItem.mFont = this.mTextUtil.getSupportFont(fontName);
        undoItem.mFontSize = fontSize;
        undoItem.mTextColor = (long) color;
        undoItem.mOldBBox = new RectF(this.mTempLastBBox);
        undoItem.mOldColor = (long) this.mTempLastColor;
        undoItem.mOldOpacity = ((float) this.mTempLastOpacity) / 255.0f;
        undoItem.mOldFont = this.mTextUtil.getSupportFont(this.mTempLastFont);
        undoItem.mOldFontSize = this.mTempLastFontSize;
        undoItem.mOldTextColor = (long) this.mTempLastColor;
        undoItem.mOldContents = this.mTempLastContent;
        try {
            final RectF tempRectF = annot.getRect();
            if (isModifyJni) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(true);
                final boolean z = addUndo;
                final String str = fromType;
                final Annot annot2 = annot;
                final int i = pageIndex;
                final RectF rectF = bbox;
                final Callback callback = result;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new TypewriterEvent(2, undoItem, (FreeText) annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            if (z) {
                                DocumentManager.getInstance(TypewriterAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            }
                            DocumentManager.getInstance(TypewriterAnnotHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                            if (str.equals("")) {
                                TypewriterAnnotHandler.this.mModifyed = true;
                            }
                            try {
                                RectF newViewRect = annot2.getRect();
                                TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(newViewRect, newViewRect, i);
                                RectF oldViewRect = new RectF(rectF);
                                TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(oldViewRect, oldViewRect, i);
                                float tmp = oldViewRect.width() - newViewRect.width();
                                if (tmp >= 1.0f && TypewriterAnnotHandler.this.deltaWidth < tmp + 10.0f) {
                                    TypewriterAnnotHandler.this.deltaWidth = tmp + 10.0f;
                                }
                                tmp = newViewRect.height() - oldViewRect.height();
                                if (tmp >= 1.0f) {
                                    TypewriterAnnotHandler.this.deltaHeight = tmp;
                                }
                                DocumentManager.getInstance(TypewriterAnnotHandler.this.mPdfViewCtrl).onAnnotModified(annot2.getPage(), annot2);
                                if (TypewriterAnnotHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                                    RectF viewRect = annot2.getRect();
                                    TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, i);
                                    TypewriterAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(tempRectF, tempRectF, i);
                                    viewRect.union(tempRectF);
                                    viewRect.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 10), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 10));
                                    TypewriterAnnotHandler.this.mPdfViewCtrl.refresh(i, AppDmUtil.rectFToRect(viewRect));
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
                this.mModifyed = true;
                if (isModifyJni) {
                    DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotModified(annot.getPage(), annot);
                }
                if (!isModifyJni) {
                    FreeText ft_Annot = (FreeText) annot;
                    DefaultAppearance da = ft_Annot.getDefaultAppearance();
                    da.setTextColor((long) color);
                    da.setFont(this.mTextUtil.getSupportFont(fontName));
                    da.setFontSize(fontSize);
                    ft_Annot.setDefaultAppearance(da);
                    ft_Annot.setOpacity(((float) opacity) / 255.0f);
                    ft_Annot.move(bbox);
                    ft_Annot.setModifiedDateTime(AppDmUtil.currentDateToDocumentDate());
                    if (content == null) {
                        content = "";
                    }
                    ft_Annot.setContent(content);
                    ft_Annot.resetAppearanceStream();
                    RectF newViewRect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(newViewRect, newViewRect, pageIndex);
                    RectF rectF2 = new RectF(bbox);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF2, rectF2, pageIndex);
                    float tmp = rectF2.width() - newViewRect.width();
                    if (tmp >= 1.0f && this.deltaWidth < 10.0f + tmp) {
                        this.deltaWidth = 10.0f + tmp;
                    }
                    tmp = newViewRect.height() - rectF2.height();
                    if (tmp >= 1.0f) {
                        this.deltaHeight = tmp;
                    }
                    RectF viewRect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(tempRectF, tempRectF, pageIndex);
                    viewRect.union(tempRectF);
                    viewRect.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 10), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 10));
                    this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                }
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void removePropertyBarListener() {
        this.mPropertyChangeListener = null;
    }
}
