package com.foxit.uiextensions.textselect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.ClipboardManager;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.PDFTextSelect;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.DocumentManager.AnnotEventListener;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.UIExtensionsManager.ToolHandlerChangedListener;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContentAbs;
import com.foxit.uiextensions.annots.textmarkup.TextSelector;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.modules.signature.SignatureModule;
import com.foxit.uiextensions.modules.signature.SignatureToolHandler;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;
import java.util.Iterator;

public class TextSelectToolHandler implements ToolHandler {
    private static final int HANDLE_AREA = 10;
    int[] ChPassList;
    int[] EnSeparatorList;
    private int ctrlPoint;
    private Callback mAddResult;
    private AnnotEventListener mAnnotListener;
    public AnnotMenu mAnnotationMenu;
    private ArrayList<Integer> mBlank;
    private Context mContext;
    public int mCurrentIndex;
    public Bitmap mHandlerBitmap;
    private ToolHandlerChangedListener mHandlerChangedListener = new ToolHandlerChangedListener() {
        public void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
            if (currentTool != null && TextSelectToolHandler.this.mIsMenuShow) {
                TextSelectToolHandler.this.mAnnotationMenu.dismiss();
                TextSelectToolHandler.this.mIsMenuShow = false;
            }
            if (currentTool != null && TextSelectToolHandler.this.mIsEdit && TextSelectToolHandler.this.mIsEdit) {
                RectF rectF = new RectF(TextSelectToolHandler.this.mSelectInfo.getBbox());
                TextSelectToolHandler.this.mSelectInfo.clear();
                TextSelectToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(TextSelectToolHandler.this.mSelectInfo.getBbox(), rectF, TextSelectToolHandler.this.mCurrentIndex);
                TextSelectToolHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, TextSelectToolHandler.this.mCurrentIndex);
                RectF rF = TextSelectToolHandler.this.calculate(rectF, TextSelectToolHandler.this.mTmpRect);
                Rect rect = new Rect();
                rF.roundOut(rect);
                rect.top -= TextSelectToolHandler.this.mHandlerBitmap.getHeight();
                rect.bottom += TextSelectToolHandler.this.mHandlerBitmap.getHeight();
                rect.left -= TextSelectToolHandler.this.mHandlerBitmap.getWidth() / 2;
                rect.right += TextSelectToolHandler.this.mHandlerBitmap.getWidth() / 2;
                TextSelectToolHandler.this.mPdfViewCtrl.invalidate(rect);
                TextSelectToolHandler.this.mIsEdit = false;
                TextSelectToolHandler.this.mAnnotationMenu.dismiss();
            }
        }
    };
    public boolean mIsEdit;
    public boolean mIsMenuShow;
    private RectF mMenuBox;
    private PointF mMenuPdfPoint;
    private PointF mMenuPoint;
    private Paint mPaint;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    protected final TextSelector mSelectInfo;
    private ArrayList<Integer> mText;
    private RectF mTmpDesRect;
    public RectF mTmpRect;

    public TextSelectToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        int[] iArr = new int[26];
        iArr[1] = 10;
        iArr[2] = 13;
        iArr[3] = 32;
        iArr[4] = 33;
        iArr[5] = 34;
        iArr[6] = 35;
        iArr[7] = 36;
        iArr[8] = 37;
        iArr[9] = 38;
        iArr[10] = 39;
        iArr[11] = 40;
        iArr[12] = 41;
        iArr[13] = 42;
        iArr[14] = 43;
        iArr[15] = 44;
        iArr[16] = 45;
        iArr[17] = 46;
        iArr[18] = 47;
        iArr[19] = 58;
        iArr[20] = 59;
        iArr[21] = 60;
        iArr[22] = 61;
        iArr[23] = 62;
        iArr[24] = 63;
        iArr[25] = 64;
        this.EnSeparatorList = iArr;
        iArr = new int[4];
        iArr[1] = 10;
        iArr[2] = 13;
        iArr[3] = 32;
        this.ChPassList = iArr;
        this.ctrlPoint = 0;
        this.mAddResult = new Callback() {
            public void result(Event event, boolean success) {
                TextSelectToolHandler.this.mSelectInfo.clear();
            }
        };
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mContext = context;
        this.mParent = parent;
        this.mSelectInfo = new TextSelector(pdfViewCtrl);
        this.mMenuPoint = null;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
        this.mTmpRect = new RectF();
        this.mTmpDesRect = new RectF();
        this.mHandlerBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.rv_textselect_handler);
        this.mAnnotationMenu = new AnnotMenuImpl(context, this.mParent);
        this.mText = new ArrayList();
        this.mBlank = new ArrayList();
        this.mIsEdit = false;
        this.mIsMenuShow = false;
        this.mAnnotListener = new AnnotEventListener() {
            public void onAnnotAdded(PDFPage page, Annot annot) {
            }

            public void onAnnotDeleted(PDFPage page, Annot annot) {
            }

            public void onAnnotModified(PDFPage page, Annot annot) {
            }

            public void onAnnotChanged(Annot lastAnnot, Annot currentAnnot) {
                if (currentAnnot != null && TextSelectToolHandler.this.mIsMenuShow) {
                    TextSelectToolHandler.this.mIsMenuShow = false;
                    TextSelectToolHandler.this.mAnnotationMenu.dismiss();
                    TextSelectToolHandler.this.mAnnotationMenu.show(TextSelectToolHandler.this.mMenuBox);
                }
                if (currentAnnot != null && TextSelectToolHandler.this.mIsEdit && TextSelectToolHandler.this.mIsEdit) {
                    RectF rectF = new RectF(TextSelectToolHandler.this.mSelectInfo.getBbox());
                    TextSelectToolHandler.this.mSelectInfo.clear();
                    if (TextSelectToolHandler.this.mPdfViewCtrl.isPageVisible(TextSelectToolHandler.this.mCurrentIndex)) {
                        TextSelectToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, TextSelectToolHandler.this.mCurrentIndex);
                        TextSelectToolHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, TextSelectToolHandler.this.mCurrentIndex);
                        RectF rF = TextSelectToolHandler.this.calculate(rectF, TextSelectToolHandler.this.mTmpRect);
                        Rect rect = new Rect();
                        rF.roundOut(rect);
                        TextSelectToolHandler.this.getInvalidateRect(rect);
                        TextSelectToolHandler.this.mPdfViewCtrl.invalidate(rect);
                        TextSelectToolHandler.this.mIsEdit = false;
                        TextSelectToolHandler.this.mAnnotationMenu.dismiss();
                        TextSelectToolHandler.this.mAnnotationMenu.show(TextSelectToolHandler.this.mMenuBox);
                    }
                }
            }
        };
        DocumentManager.getInstance(this.mPdfViewCtrl).registerAnnotEventListener(this.mAnnotListener);
    }

    protected AnnotMenu getAnnotationMenu() {
        return this.mAnnotationMenu;
    }

    public String getType() {
        return ToolHandler.TH_TYPE_TEXTSELECT;
    }

    public String getCurrentSelectedText() {
        return this.mSelectInfo.getContents();
    }

    public void onActivate() {
    }

    public void onDeactivate() {
        RectF rectF = new RectF(this.mSelectInfo.getBbox());
        this.mSelectInfo.clear();
        if (this.mPdfViewCtrl.isPageVisible(this.mCurrentIndex)) {
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, this.mCurrentIndex);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, this.mCurrentIndex);
            RectF rF = calculate(rectF, this.mTmpRect);
            Rect rect = new Rect();
            rF.roundOut(rect);
            getInvalidateRect(rect);
            this.mPdfViewCtrl.invalidate(rect);
            this.mAnnotationMenu.dismiss();
            this.mIsEdit = false;
        }
    }

    public void uninit() {
        DocumentManager.getInstance(this.mPdfViewCtrl).unregisterAnnotEventListener(this.mAnnotListener);
    }

    public void reloadres() {
        this.mText.clear();
        this.mBlank.clear();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canCopy()) {
            this.mText.add(Integer.valueOf(1));
        }
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot() && ToolUtil.getAnnotHandlerByType(uiExtensionsManager, 9) != null) {
            this.mText.add(Integer.valueOf(7));
        }
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot() && ToolUtil.getAnnotHandlerByType(uiExtensionsManager, 10) != null) {
            this.mText.add(Integer.valueOf(8));
        }
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot() && ToolUtil.getAnnotHandlerByType(uiExtensionsManager, 12) != null) {
            this.mText.add(Integer.valueOf(9));
        }
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot() && ToolUtil.getAnnotHandlerByType(uiExtensionsManager, 11) != null) {
            this.mText.add(Integer.valueOf(10));
        }
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot() && ToolUtil.getAnnotHandlerByType(uiExtensionsManager, 1) != null) {
            this.mBlank.add(Integer.valueOf(11));
        }
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot() && uiExtensionsManager.getModuleByName(Module.MODULE_NAME_PSISIGNATURE) != null) {
            this.mBlank.add(Integer.valueOf(12));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void findEnWord(int pageIndex, TextSelector info, int index) {
        info.setStart(index);
        info.setEnd(index);
        try {
            String charInfo;
            int i;
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            if (!page.isParsed()) {
                for (int ret = page.startParse(0, null, false); ret == 1; ret = page.continueParse()) {
                }
            }
            PDFTextSelect textPage = PDFTextSelect.create(page);
            while (info.getStart() >= 0) {
                charInfo = textPage.getChars(info.getStart(), 1);
                if (charInfo == null) {
                    info.setStart(info.getStart() + 1);
                    break;
                }
                i = 0;
                while (i < this.EnSeparatorList.length && this.EnSeparatorList[i] != charInfo.charAt(0)) {
                    i++;
                }
                if (i != this.EnSeparatorList.length) {
                    info.setStart(info.getStart() + 1);
                    break;
                }
                info.setStart(info.getStart() - 1);
            }
            if (info.getStart() < 0) {
                info.setStart(0);
            }
            while (true) {
                charInfo = textPage.getChars(info.getEnd(), 1);
                if (charInfo == null) {
                    break;
                }
                i = 0;
                while (i < this.EnSeparatorList.length && this.EnSeparatorList[i] != charInfo.charAt(0)) {
                    i++;
                }
                if (i != this.EnSeparatorList.length) {
                    break;
                }
                info.setEnd(info.getEnd() + 1);
                if (charInfo == null) {
                    info.setEnd(info.getEnd() - 1);
                }
            }
            info.setEnd(info.getEnd() - 1);
            if (charInfo == null) {
                info.setEnd(info.getEnd() - 1);
            }
        } catch (PDFException e) {
            if (e.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    private void findChWord(int pageIndex, TextSelector info, int index) {
        info.setStart(index);
        info.setEnd(index);
        info.setStart(info.getStart() - 1);
        info.setEnd(info.getEnd() + 1);
        try {
            int i;
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            if (!page.isParsed()) {
                for (int ret = page.startParse(0, null, false); ret == 1; ret = page.continueParse()) {
                }
            }
            PDFTextSelect textPage = PDFTextSelect.create(page);
            String charinfo = textPage.getChars(index, 1);
            while (info.getStart() >= 0) {
                charinfo = textPage.getChars(info.getStart(), 1);
                if (charinfo == null) {
                    info.setStart(info.getStart() + 1);
                    break;
                }
                i = 0;
                while (i < this.ChPassList.length && this.ChPassList[i] != charinfo.charAt(0)) {
                    i++;
                }
                if (i != this.ChPassList.length) {
                    info.setStart(info.getStart() + 1);
                    break;
                }
                info.setStart(info.getStart() - 1);
            }
            if (info.getStart() < 0) {
                info.setStart(0);
            }
            while (info.getEnd() >= 0) {
                charinfo = textPage.getChars(info.getEnd(), 1);
                if (charinfo == null) {
                    info.setEnd(info.getEnd() - 1);
                    break;
                }
                i = 0;
                while (i < this.ChPassList.length && this.ChPassList[i] != charinfo.charAt(0)) {
                    i++;
                }
                if (i != this.ChPassList.length) {
                    info.setEnd(info.getEnd() - 1);
                    break;
                }
                info.setEnd(info.getEnd() + 1);
            }
            if (charinfo == null) {
                info.setEnd(info.getEnd() - 1);
            }
        } catch (PDFException e) {
            if (e.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        PointF devPt = new PointF(motionEvent.getX(), motionEvent.getY());
        PointF point = new PointF();
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(devPt, point, pageIndex);
        switch (motionEvent.getActionMasked()) {
            case 0:
                try {
                    this.ctrlPoint = isControlPoint(this.mCurrentIndex, point);
                    if (this.ctrlPoint == 0) {
                        return false;
                    }
                    return true;
                } catch (PDFException e1) {
                    if (e1.getLastError() == 10) {
                        this.mPdfViewCtrl.recoverForOOM();
                        return true;
                    }
                }
                break;
            case 1:
            case 3:
                if (this.mIsEdit) {
                    this.mText.clear();
                    reloadres();
                    if (this.mText.size() == 0) {
                        return false;
                    }
                    this.mAnnotationMenu.setMenuItems(this.mText);
                    this.mMenuBox = new RectF();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mSelectInfo.getBbox(), this.mMenuBox, this.mCurrentIndex);
                    this.mMenuBox.inset(-10.0f, -10.0f);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mMenuBox, this.mMenuBox, this.mCurrentIndex);
                    this.mAnnotationMenu.show(this.mMenuBox);
                    return false;
                }
                break;
            case 2:
                this.mAnnotationMenu.dismiss();
                if (this.ctrlPoint == 0) {
                    return false;
                }
                OnSelectMove(pageIndex, point, this.mSelectInfo);
                this.mSelectInfo.computeSelected(this.mPdfViewCtrl.getDoc().getPage(this.mCurrentIndex), this.mSelectInfo.getStart(), this.mSelectInfo.getEnd());
                invalidateTouch(this.mCurrentIndex, this.mSelectInfo);
                return true;
        }
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        PointF pointF = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        try {
            if (DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() != null) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                return false;
            } else if (this.mIsMenuShow) {
                this.mAnnotationMenu.dismiss();
                this.mIsMenuShow = false;
                return true;
            } else if (this.mIsEdit) {
                RectF rectF = new RectF(this.mSelectInfo.getBbox());
                this.mSelectInfo.clear();
                this.mCurrentIndex = pageIndex;
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mSelectInfo.getBbox(), rectF, this.mCurrentIndex);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, this.mCurrentIndex);
                RectF rF = calculate(rectF, this.mTmpRect);
                Rect rect = new Rect();
                rF.roundOut(rect);
                getInvalidateRect(rect);
                this.mPdfViewCtrl.invalidate(rect);
                this.mIsEdit = false;
                this.mAnnotationMenu.dismiss();
                return true;
            } else {
                if (this.mAnnotationMenu.isShowing()) {
                    this.mAnnotationMenu.dismiss();
                }
                this.mCurrentIndex = pageIndex;
                PointF pointPdfView = new PointF(pointF.x, pointF.y);
                this.mPdfViewCtrl.convertPageViewPtToPdfPt(pointF, pointPdfView, this.mCurrentIndex);
                final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mCurrentIndex);
                if (!page.isParsed()) {
                    for (int ret = page.startParse(0, null, false); ret == 1; ret = page.continueParse()) {
                    }
                }
                PDFTextSelect textPage = PDFTextSelect.create(page);
                int index = textPage.getIndexAtPos(pointPdfView.x, pointPdfView.y, 30.0f);
                if (index == -1 && (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot() || DocumentManager.getInstance(this.mPdfViewCtrl).canModifyContents())) {
                    this.mIsMenuShow = true;
                    this.mMenuPoint = new PointF(pointF.x, pointF.y);
                    this.mMenuPdfPoint = new PointF(this.mMenuPoint.x, this.mMenuPoint.y);
                    this.mPdfViewCtrl.convertPageViewPtToPdfPt(this.mMenuPdfPoint, this.mMenuPdfPoint, this.mCurrentIndex);
                    this.mMenuBox = new RectF(pointF.x, pointF.y, pointF.x + 1.0f, pointF.y + 1.0f);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mMenuBox, this.mMenuBox, this.mCurrentIndex);
                    reloadres();
                    this.mAnnotationMenu.setMenuItems(this.mBlank);
                    this.mAnnotationMenu.show(this.mMenuBox);
                    this.mAnnotationMenu.setListener(new ClickListener() {
                        public void onAMClick(int btType) {
                            if (btType == 11) {
                                PDFPage pdfPage = null;
                                try {
                                    pdfPage = TextSelectToolHandler.this.mPdfViewCtrl.getDoc().getPage(TextSelectToolHandler.this.mCurrentIndex);
                                } catch (PDFException e1) {
                                    e1.printStackTrace();
                                }
                                if (pdfPage != null) {
                                    DocumentManager.getInstance(TextSelectToolHandler.this.mPdfViewCtrl).addAnnot(page, new TextAnnotContent(new PointF(TextSelectToolHandler.this.mMenuPdfPoint.x, TextSelectToolHandler.this.mMenuPdfPoint.y), TextSelectToolHandler.this.mCurrentIndex), true, TextSelectToolHandler.this.mAddResult);
                                } else {
                                    return;
                                }
                            } else if (btType == 12) {
                                UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) TextSelectToolHandler.this.mPdfViewCtrl.getUIExtensionsManager();
                                Module module = uiExtensionsManager.getModuleByName(Module.MODULE_NAME_PSISIGNATURE);
                                if (module != null) {
                                    SignatureToolHandler toolHandler = (SignatureToolHandler) ((SignatureModule) module).getToolHandler();
                                    uiExtensionsManager.setCurrentToolHandler(toolHandler);
                                    PointF p = new PointF(TextSelectToolHandler.this.mMenuPdfPoint.x, TextSelectToolHandler.this.mMenuPdfPoint.y);
                                    TextSelectToolHandler.this.mPdfViewCtrl.convertPdfPtToPageViewPt(p, p, TextSelectToolHandler.this.mCurrentIndex);
                                    toolHandler.addSignature(TextSelectToolHandler.this.mCurrentIndex, p, true);
                                }
                            }
                            TextSelectToolHandler.this.mIsMenuShow = false;
                            TextSelectToolHandler.this.mAnnotationMenu.dismiss();
                            TextSelectToolHandler.this.mMenuPoint = null;
                        }
                    });
                    return true;
                } else if (index == -1) {
                    return true;
                } else {
                    String info = textPage.getChars(index, 1);
                    if (index >= 0) {
                        reloadres();
                        if (this.mText.size() == 0) {
                            return false;
                        }
                        if ((info.charAt(0) < 'A' || info.charAt(0) > 'Z') && (info.charAt(0) < 'a' || info.charAt(0) > 'z')) {
                            findChWord(this.mCurrentIndex, this.mSelectInfo, index);
                        } else {
                            findEnWord(this.mCurrentIndex, this.mSelectInfo, index);
                        }
                        this.mSelectInfo.computeSelected(page, this.mSelectInfo.getStart(), this.mSelectInfo.getEnd());
                        invalidateTouch(this.mCurrentIndex, this.mSelectInfo);
                        this.mIsEdit = true;
                    } else {
                        this.mIsEdit = false;
                    }
                    if (this.mSelectInfo.getRectFList().size() == 0) {
                        this.mIsEdit = false;
                    }
                    if (this.mIsEdit) {
                        this.mMenuBox = new RectF(this.mSelectInfo.getBbox());
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mMenuBox, this.mMenuBox, this.mCurrentIndex);
                        this.mMenuBox.inset(-10.0f, -10.0f);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mMenuBox, this.mMenuBox, this.mCurrentIndex);
                        this.mAnnotationMenu.setMenuItems(this.mText);
                        this.mMenuPoint = null;
                        this.mAnnotationMenu.show(this.mMenuBox);
                        this.mAnnotationMenu.setListener(new ClickListener() {
                            public void onAMClick(int btType) {
                                if (btType == 1) {
                                    ((ClipboardManager) TextSelectToolHandler.this.mContext.getSystemService("clipboard")).setText(TextSelectToolHandler.this.mSelectInfo.getText(page));
                                    AppAnnotUtil.toastAnnotCopy(TextSelectToolHandler.this.mContext);
                                    RectF rectF = new RectF(TextSelectToolHandler.this.mSelectInfo.getBbox());
                                    TextSelectToolHandler.this.mSelectInfo.clear();
                                    TextSelectToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, TextSelectToolHandler.this.mCurrentIndex);
                                    TextSelectToolHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, TextSelectToolHandler.this.mCurrentIndex);
                                    RectF rF = TextSelectToolHandler.this.calculate(rectF, TextSelectToolHandler.this.mTmpRect);
                                    Rect rect = new Rect();
                                    rF.roundOut(rect);
                                    TextSelectToolHandler.this.getInvalidateRect(rect);
                                    TextSelectToolHandler.this.mPdfViewCtrl.invalidate(rect);
                                    TextSelectToolHandler.this.mIsEdit = false;
                                    TextSelectToolHandler.this.mAnnotationMenu.dismiss();
                                    return;
                                }
                                DocumentManager instance;
                                PDFPage pDFPage;
                                final PDFPage pDFPage2;
                                if (btType == 7) {
                                    try {
                                        if (TextSelectToolHandler.this.mPdfViewCtrl.getDoc().getPage(TextSelectToolHandler.this.mCurrentIndex) != null) {
                                            instance = DocumentManager.getInstance(TextSelectToolHandler.this.mPdfViewCtrl);
                                            pDFPage = page;
                                            pDFPage2 = page;
                                            instance.addAnnot(pDFPage, new TextMarkupContentAbs() {
                                                public TextSelector getTextSelector() {
                                                    TextSelectToolHandler.this.mSelectInfo.setContents(TextSelectToolHandler.this.mSelectInfo.getText(pDFPage2));
                                                    return TextSelectToolHandler.this.mSelectInfo;
                                                }

                                                public int getPageIndex() {
                                                    return TextSelectToolHandler.this.mCurrentIndex;
                                                }

                                                public int getType() {
                                                    return 9;
                                                }

                                                public String getIntent() {
                                                    return null;
                                                }
                                            }, true, TextSelectToolHandler.this.mAddResult);
                                        } else {
                                            return;
                                        }
                                    } catch (PDFException e1) {
                                        e1.printStackTrace();
                                    }
                                } else if (btType == 8) {
                                    if (TextSelectToolHandler.this.mPdfViewCtrl.getDoc().getPage(TextSelectToolHandler.this.mCurrentIndex) != null) {
                                        instance = DocumentManager.getInstance(TextSelectToolHandler.this.mPdfViewCtrl);
                                        pDFPage = page;
                                        pDFPage2 = page;
                                        instance.addAnnot(pDFPage, new TextMarkupContentAbs() {
                                            public TextSelector getTextSelector() {
                                                TextSelectToolHandler.this.mSelectInfo.setContents(TextSelectToolHandler.this.mSelectInfo.getText(pDFPage2));
                                                return TextSelectToolHandler.this.mSelectInfo;
                                            }

                                            public int getPageIndex() {
                                                return TextSelectToolHandler.this.mCurrentIndex;
                                            }

                                            public int getType() {
                                                return 10;
                                            }

                                            public String getIntent() {
                                                return null;
                                            }
                                        }, true, TextSelectToolHandler.this.mAddResult);
                                    } else {
                                        return;
                                    }
                                } else if (btType == 9) {
                                    if (TextSelectToolHandler.this.mPdfViewCtrl.getDoc().getPage(TextSelectToolHandler.this.mCurrentIndex) != null) {
                                        instance = DocumentManager.getInstance(TextSelectToolHandler.this.mPdfViewCtrl);
                                        pDFPage = page;
                                        pDFPage2 = page;
                                        instance.addAnnot(pDFPage, new TextMarkupContentAbs() {
                                            public TextSelector getTextSelector() {
                                                TextSelectToolHandler.this.mSelectInfo.setContents(TextSelectToolHandler.this.mSelectInfo.getText(pDFPage2));
                                                return TextSelectToolHandler.this.mSelectInfo;
                                            }

                                            public int getPageIndex() {
                                                return TextSelectToolHandler.this.mCurrentIndex;
                                            }

                                            public int getType() {
                                                return 12;
                                            }

                                            public String getIntent() {
                                                return null;
                                            }
                                        }, true, TextSelectToolHandler.this.mAddResult);
                                    } else {
                                        return;
                                    }
                                } else if (btType == 10) {
                                    if (TextSelectToolHandler.this.mPdfViewCtrl.getDoc().getPage(TextSelectToolHandler.this.mCurrentIndex) != null) {
                                        instance = DocumentManager.getInstance(TextSelectToolHandler.this.mPdfViewCtrl);
                                        pDFPage = page;
                                        pDFPage2 = page;
                                        instance.addAnnot(pDFPage, new TextMarkupContentAbs() {
                                            public TextSelector getTextSelector() {
                                                TextSelectToolHandler.this.mSelectInfo.setContents(TextSelectToolHandler.this.mSelectInfo.getText(pDFPage2));
                                                return TextSelectToolHandler.this.mSelectInfo;
                                            }

                                            public int getPageIndex() {
                                                return TextSelectToolHandler.this.mCurrentIndex;
                                            }

                                            public int getType() {
                                                return 11;
                                            }

                                            public String getIntent() {
                                                return null;
                                            }
                                        }, true, TextSelectToolHandler.this.mAddResult);
                                    } else {
                                        return;
                                    }
                                }
                                TextSelectToolHandler.this.mIsEdit = false;
                                TextSelectToolHandler.this.mAnnotationMenu.dismiss();
                            }
                        });
                    }
                    return true;
                }
            }
        } catch (PDFException exception) {
            if (exception.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
                return true;
            }
        }
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        if (this.mIsMenuShow) {
            this.mAnnotationMenu.dismiss();
            this.mIsMenuShow = false;
            return true;
        } else if (!this.mIsEdit) {
            return false;
        } else {
            RectF rectF = new RectF(this.mSelectInfo.getBbox());
            this.mSelectInfo.clear();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mSelectInfo.getBbox(), rectF, this.mCurrentIndex);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, this.mCurrentIndex);
            RectF rF = calculate(rectF, this.mTmpRect);
            Rect rect = new Rect();
            rF.roundOut(rect);
            getInvalidateRect(rect);
            this.mPdfViewCtrl.invalidate(rect);
            this.mIsEdit = false;
            this.mAnnotationMenu.dismiss();
            return true;
        }
    }

    protected TextSelector getSelectInfo() {
        return this.mSelectInfo;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (this.mCurrentIndex == pageIndex && this.mSelectInfo != null) {
            this.mPaint.setColor(((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getSelectionHighlightColor());
            Rect clipRect = canvas.getClipBounds();
            Iterator it = this.mSelectInfo.getRectFList().iterator();
            while (it.hasNext()) {
                RectF rect = (RectF) it.next();
                RectF tmp = new RectF(rect);
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, tmp, this.mCurrentIndex);
                Rect r = new Rect();
                tmp.round(r);
                if (r.intersect(clipRect)) {
                    canvas.save();
                    canvas.drawRect(r, this.mPaint);
                    canvas.restore();
                }
            }
            if (this.mSelectInfo.getRectFList().size() > 0) {
                RectF start = new RectF((RectF) this.mSelectInfo.getRectFList().get(0));
                RectF end = new RectF((RectF) this.mSelectInfo.getRectFList().get(this.mSelectInfo.getRectFList().size() - 1));
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(start, start, this.mCurrentIndex);
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(end, end, this.mCurrentIndex);
                canvas.drawBitmap(this.mHandlerBitmap, start.left - ((float) this.mHandlerBitmap.getWidth()), start.top - ((float) this.mHandlerBitmap.getHeight()), null);
                canvas.drawBitmap(this.mHandlerBitmap, end.right, end.bottom, null);
                this.mPaint.setARGB(255, 76, 121, 164);
                canvas.drawLine(start.left, start.top - 1.0f, start.left, start.bottom + 1.0f, this.mPaint);
                canvas.drawLine(end.right, end.top - 1.0f, end.right, end.bottom + 1.0f, this.mPaint);
            }
        }
    }

    private boolean OnSelectMove(int pageIndex, PointF point, TextSelector selectInfo) {
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
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(point, point, this.mCurrentIndex);
            int index = textPage.getIndexAtPos(point.x, point.y, 30.0f);
            if (index < 0) {
                return false;
            }
            if (this.ctrlPoint == 1) {
                if (index <= selectInfo.getEnd()) {
                    selectInfo.setStart(index);
                }
            } else if (this.ctrlPoint == 2 && index >= selectInfo.getStart()) {
                selectInfo.setEnd(index);
            }
            textPage.release();
            this.mPdfViewCtrl.getDoc().closePage(this.mCurrentIndex);
            return true;
        } catch (PDFException e) {
            if (e.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
                return false;
            }
        }
    }

    private void invalidateTouch(int pageIndex, TextSelector selectInfo) {
        if (selectInfo != null) {
            RectF rectF = new RectF();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mSelectInfo.getBbox(), rectF, pageIndex);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
            RectF rF = calculate(rectF, this.mTmpRect);
            Rect rect = new Rect();
            rF.roundOut(rect);
            getInvalidateRect(rect);
            this.mPdfViewCtrl.invalidate(rect);
            this.mTmpRect.set(rectF);
        }
    }

    public RectF calculate(RectF desRectF, RectF srcRectF) {
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

    private int isControlPoint(int pageIndex, PointF point) {
        if (this.mSelectInfo != null && this.mSelectInfo.getRectFList().size() > 0) {
            RectF mStart = new RectF((RectF) this.mSelectInfo.getRectFList().get(0));
            RectF mEnd = new RectF((RectF) this.mSelectInfo.getRectFList().get(this.mSelectInfo.getRectFList().size() - 1));
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(mStart, mStart, pageIndex);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(mEnd, mEnd, pageIndex);
            RectF startHandler = new RectF(mStart.left - ((float) this.mHandlerBitmap.getWidth()), mStart.top - ((float) this.mHandlerBitmap.getHeight()), mStart.left, mStart.top);
            RectF endHandler = new RectF(mEnd.right, mEnd.bottom, mEnd.right + ((float) this.mHandlerBitmap.getWidth()), mEnd.bottom + ((float) this.mHandlerBitmap.getHeight()));
            startHandler.inset(-10.0f, -10.0f);
            endHandler.inset(-10.0f, -10.0f);
            if (mStart != null && startHandler.contains(point.x, point.y)) {
                return 1;
            }
            if (mEnd != null && endHandler.contains(point.x, point.y)) {
                return 2;
            }
        }
        return 0;
    }

    public void dismissMenu() {
        if (this.mIsMenuShow) {
            this.mAnnotationMenu.dismiss();
            this.mIsMenuShow = false;
        }
        if (this.mIsEdit) {
            RectF rectF = new RectF(this.mSelectInfo.getBbox());
            this.mSelectInfo.clear();
            int _pageIndex = this.mPdfViewCtrl.getCurrentPage();
            if (this.mPdfViewCtrl.isPageVisible(_pageIndex)) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, _pageIndex);
                RectF rF = calculate(rectF, this.mTmpRect);
                Rect rect = new Rect();
                rF.roundOut(rect);
                getInvalidateRect(rect);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, _pageIndex);
                this.mPdfViewCtrl.invalidate(rect);
            }
            this.mIsEdit = false;
            this.mAnnotationMenu.dismiss();
        }
    }

    public void getInvalidateRect(Rect rect) {
        rect.top -= this.mHandlerBitmap.getHeight();
        rect.bottom += this.mHandlerBitmap.getHeight();
        rect.left -= this.mHandlerBitmap.getWidth() / 2;
        rect.right += this.mHandlerBitmap.getWidth() / 2;
        rect.inset(-20, -20);
    }

    public void onDrawForAnnotMenu(Canvas canvas) {
        if (!this.mPdfViewCtrl.isPageVisible(this.mCurrentIndex)) {
            return;
        }
        if (this.mIsEdit || this.mIsMenuShow) {
            RectF bboxRect;
            if (this.mMenuPoint != null) {
                PointF temp = new PointF(this.mMenuPdfPoint.x, this.mMenuPdfPoint.y);
                this.mPdfViewCtrl.convertPdfPtToPageViewPt(this.mMenuPdfPoint, temp, this.mCurrentIndex);
                bboxRect = new RectF(temp.x, temp.y, temp.x + 1.0f, temp.y + 1.0f);
            } else {
                bboxRect = new RectF(this.mSelectInfo.getBbox());
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(bboxRect, bboxRect, this.mCurrentIndex);
                bboxRect.inset(-10.0f, -10.0f);
            }
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bboxRect, bboxRect, this.mCurrentIndex);
            this.mAnnotationMenu.update(bboxRect);
        }
    }
}
