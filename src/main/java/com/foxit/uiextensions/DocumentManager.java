package com.foxit.uiextensions;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.ActionHandler;
import com.foxit.sdk.common.Library;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FreeText;
import com.foxit.sdk.pdf.annots.Ink;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.form.FormControl;
import com.foxit.sdk.pdf.form.FormField;
import com.foxit.sdk.pdf.objects.PDFArray;
import com.foxit.sdk.pdf.objects.PDFDictionary;
import com.foxit.sdk.pdf.objects.PDFObject;
import com.foxit.sdk.pdf.signature.Signature;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.line.LineConstants;
import com.foxit.uiextensions.textselect.TextSelectToolHandler;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DocumentManager extends AbstractUndo {
    private static DocumentManager mAnnotManager = null;
    private Boolean isSetActionHandler = new Boolean(false);
    private ActionHandler mActionHandler = null;
    ArrayList<AnnotEventListener> mAnnotEventListenerList = new ArrayList();
    protected Annot mCurAnnot = null;
    protected List<Ink> mEraseAnnotList = new ArrayList();
    private boolean mHasModifyTask = false;
    private boolean mIsOwner = false;
    private boolean mIsSign = false;
    private int mMDPPermission = 0;
    private PDFViewCtrl mPdfViewCtrl;
    private long mUserPermission = 0;

    public interface AnnotEventListener {
        void onAnnotAdded(PDFPage pDFPage, Annot annot);

        void onAnnotChanged(Annot annot, Annot annot2);

        void onAnnotDeleted(PDFPage pDFPage, Annot annot);

        void onAnnotModified(PDFPage pDFPage, Annot annot);
    }

    public static DocumentManager getInstance(PDFViewCtrl pdfViewCtrl) {
        if (mAnnotManager == null) {
            mAnnotManager = new DocumentManager(pdfViewCtrl);
        }
        return mAnnotManager;
    }

    public static void release() {
        mAnnotManager = null;
    }

    private DocumentManager(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public void setActionHandler(ActionHandler handler) {
        synchronized (this.isSetActionHandler) {
            if (this.isSetActionHandler.booleanValue()) {
                return;
            }
            this.mActionHandler = handler;
            try {
                Library.setActionHandler(this.mActionHandler);
                this.isSetActionHandler = Boolean.valueOf(true);
            } catch (PDFException e) {
                this.isSetActionHandler = Boolean.valueOf(false);
            }
        }
    }

    public void reInit() {
        synchronized (this.isSetActionHandler) {
            this.isSetActionHandler = Boolean.valueOf(false);
        }
    }

    public ActionHandler getActionHandler() {
        return this.mActionHandler;
    }

    public void setCurrentAnnot(Annot annot) {
        if (this.mCurAnnot != annot) {
            Annot lastAnnot = this.mCurAnnot;
            try {
                if (this.mCurAnnot != null) {
                    int type = getAnnotHandlerType(lastAnnot);
                    if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(type) != null) {
                        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(type).onAnnotDeselected(lastAnnot, true);
                    }
                }
                this.mCurAnnot = annot;
                if (!(annot == null || ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot)) == null)) {
                    if (annot.getUniqueID() == null) {
                        annot.setUniqueID(AppDmUtil.randomUUID(null));
                    }
                    ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot)).onAnnotSelected(annot, true);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
            onAnnotChanged(lastAnnot, this.mCurAnnot);
        }
    }

    protected static int getAnnotHandlerType(Annot annot) {
        if (annot == null) {
            return 0;
        }
        try {
            int type = annot.getType();
            if (type == 3) {
                if (((FreeText) annot).getIntent() == null) {
                    type = 0;
                } else if (((FreeText) annot).getIntent().equalsIgnoreCase("FreeTextCallout")) {
                    type += 100;
                }
            }
            if (type != 20) {
                return type;
            }
            FormField field = ((FormControl) annot).getField();
            if (field == null || field.getType() != 7) {
                return type;
            }
            return type + 101;
        } catch (PDFException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Annot getCurrentAnnot() {
        return this.mCurAnnot;
    }

    protected Annot getFocusAnnot() {
        if (this.mCurAnnot != null) {
            return this.mCurAnnot;
        }
        if (this.mEraseAnnotList.size() > 0) {
            return (Annot) this.mEraseAnnotList.get(0);
        }
        return null;
    }

    public boolean shouldViewCtrlDraw(Annot annot) {
        try {
            if (this.mCurAnnot != null && this.mCurAnnot.getPage().getIndex() == annot.getPage().getIndex()) {
                int type = this.mCurAnnot.getType();
                if (type == 3 || type == 5 || type == 6 || type == 15 || type == 4 || type == 1) {
                    if (type == 4) {
                        String intent = ((Line) this.mCurAnnot).getIntent();
                        if (intent != null && intent.equals(LineConstants.INTENT_LINE_DIMENSION)) {
                            return true;
                        }
                    }
                    if (this.mCurAnnot.getIndex() == annot.getIndex()) {
                        return false;
                    }
                }
            }
            if (annot.getType() != 15) {
                return true;
            }
            for (int i = 0; i < this.mEraseAnnotList.size(); i++) {
                Ink ink = (Ink) this.mEraseAnnotList.get(i);
                if (ink.getPage().getIndex() == annot.getPage().getIndex() && ink.getIndex() == annot.getIndex()) {
                    return false;
                }
            }
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private int getMDPDigitalSignPermissionInDocument(PDFDoc document) throws PDFException {
        PDFDictionary catalog = document.getCatalog();
        if (catalog == null) {
            return 0;
        }
        PDFObject object = catalog.getElement("Perms");
        if (object == null) {
            return 0;
        }
        PDFDictionary perms = (PDFDictionary) object.getDirectObject();
        if (perms == null) {
            return 0;
        }
        object = perms.getElement("DocMDP");
        if (object == null) {
            return 0;
        }
        PDFDictionary docMDP = (PDFDictionary) object.getDirectObject();
        if (docMDP == null) {
            return 0;
        }
        object = docMDP.getElement("Reference");
        if (object == null) {
            return 0;
        }
        PDFArray reference = (PDFArray) object.getDirectObject();
        if (reference == null) {
            return 0;
        }
        for (int i = 0; i < reference.getElementCount(); i++) {
            object = reference.getElement(i);
            if (object == null) {
                return 0;
            }
            PDFDictionary tmpDict = (PDFDictionary) object.getDirectObject();
            if (tmpDict == null) {
                return 0;
            }
            object = tmpDict.getElement("TransformMethod");
            if (object == null) {
                return 0;
            }
            if (object.getDirectObject().getString().contentEquals("DocMDP")) {
                object = tmpDict.getElement("TransformParams");
                if (object == null) {
                    return 0;
                }
                PDFDictionary transformParams = (PDFDictionary) object.getDirectObject();
                if (transformParams == null || transformParams == tmpDict) {
                    return 0;
                }
                object = transformParams.getElement("P");
                if (object != null) {
                    return object.getDirectObject().getInteger();
                }
                return 0;
            }
        }
        return 0;
    }

    public boolean canCopy() {
        if (this.mPdfViewCtrl.getDoc() == null || (this.mUserPermission & 16) == 0) {
            return false;
        }
        return true;
    }

    public boolean canCopyForAssess() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        if ((this.mUserPermission & 512) == 0 && (this.mUserPermission & 16) == 0) {
            return false;
        }
        return true;
    }

    public boolean canAssemble() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        if (!canModifyFile() && !canSaveAsFile()) {
            return false;
        }
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if (uiExtensionsManager != null && uiExtensionsManager.getModuleByName(Module.MODULE_NAME_DIGITALSIGNATURE) != null && (this.mMDPPermission != 0 || this.mIsSign)) {
            return false;
        }
        if ((this.mUserPermission & PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) == 0 && (this.mUserPermission & 8) == 0) {
            return false;
        }
        return true;
    }

    public boolean canModifyContents() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        if (!canModifyFile() && !canSaveAsFile()) {
            return false;
        }
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if ((uiExtensionsManager == null || uiExtensionsManager.getModuleByName(Module.MODULE_NAME_DIGITALSIGNATURE) == null || (this.mMDPPermission == 0 && !this.mIsSign)) && (this.mUserPermission & 8) != 0) {
            return true;
        }
        return false;
    }

    public boolean canFillForm() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        if (!canModifyFile() && !canSaveAsFile()) {
            return false;
        }
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if (uiExtensionsManager != null && uiExtensionsManager.getModuleByName(Module.MODULE_NAME_DIGITALSIGNATURE) != null && this.mMDPPermission == 1) {
            return false;
        }
        if ((this.mUserPermission & 256) == 0 && (this.mUserPermission & 32) == 0 && (this.mUserPermission & 8) == 0) {
            return false;
        }
        return true;
    }

    public boolean canAddAnnot() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        if (!canModifyFile() && !canSaveAsFile()) {
            return false;
        }
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if ((uiExtensionsManager == null || uiExtensionsManager.getModuleByName(Module.MODULE_NAME_DIGITALSIGNATURE) == null || (this.mMDPPermission != 1 && this.mMDPPermission != 2)) && (this.mUserPermission & 32) != 0) {
            return true;
        }
        return false;
    }

    public boolean canSigning() {
        if (canAddAnnot() || canFillForm() || canModifyContents()) {
            return true;
        }
        return false;
    }

    public boolean isOwner() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        return this.mIsOwner;
    }

    public boolean canModifyFile() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        return true;
    }

    public boolean canSaveAsFile() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        return true;
    }

    public boolean isSign() {
        if (this.mPdfViewCtrl.getDoc() == null) {
            return false;
        }
        return this.mIsSign;
    }

    public void registerAnnotEventListener(AnnotEventListener listener) {
        this.mAnnotEventListenerList.add(listener);
    }

    public void unregisterAnnotEventListener(AnnotEventListener listener) {
        this.mAnnotEventListenerList.remove(listener);
    }

    public void onAnnotAdded(PDFPage page, Annot annot) {
        Iterator it = this.mAnnotEventListenerList.iterator();
        while (it.hasNext()) {
            ((AnnotEventListener) it.next()).onAnnotAdded(page, annot);
        }
    }

    public void onAnnotDeleted(PDFPage page, Annot annot) {
        Iterator it = this.mAnnotEventListenerList.iterator();
        while (it.hasNext()) {
            ((AnnotEventListener) it.next()).onAnnotDeleted(page, annot);
        }
    }

    public void onAnnotModified(PDFPage page, Annot annot) {
        Iterator it = this.mAnnotEventListenerList.iterator();
        while (it.hasNext()) {
            ((AnnotEventListener) it.next()).onAnnotModified(page, annot);
        }
    }

    public void onAnnotChanged(Annot lastAnnot, Annot currentAnnot) {
        Iterator it = this.mAnnotEventListenerList.iterator();
        while (it.hasNext()) {
            ((AnnotEventListener) it.next()).onAnnotChanged(lastAnnot, currentAnnot);
        }
    }

    public void onAnnotStartEraser(Ink annot) {
        this.mEraseAnnotList.add(annot);
    }

    public void onAnnotEndEraser() {
        this.mEraseAnnotList.clear();
    }

    public static boolean intersects(RectF a, RectF b) {
        return a.left < b.right && b.left < a.right && a.top > b.bottom && b.top > a.bottom;
    }

    public ArrayList<Annot> getAnnotsInteractRect(PDFPage page, RectF rect, int type) {
        ArrayList<Annot> annotList = new ArrayList(4);
        try {
            int count = page.getAnnotCount();
            for (int i = 0; i < count; i++) {
                Annot annot = page.getAnnot(i);
                if ((annot.getFlags() & 2) == 0) {
                    AnnotHandler annotHandler = ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), getAnnotHandlerType(annot));
                    if (annotHandler != null && intersects(annotHandler.getAnnotBBox(annot), rect) && annot.getType() == type) {
                        annotList.add(annot);
                    }
                }
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return annotList;
    }

    public Annot getAnnot(PDFPage page, String nm) {
        if (page == null) {
            return null;
        }
        try {
            int count = page.getAnnotCount();
            for (int i = 0; i < count; i++) {
                Annot annot = page.getAnnot(i);
                if (annot.getUniqueID() != null && annot.getUniqueID().equals(nm)) {
                    return annot;
                }
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addAnnot(PDFPage page, AnnotContent content, boolean addUndo, Callback result) {
        if (page != null) {
            Annot annot = getAnnot(page, content.getNM());
            if (annot != null) {
                modifyAnnot(annot, content, addUndo, result);
                return;
            }
            AnnotHandler annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(content.getType());
            if (annotHandler != null) {
                try {
                    annotHandler.addAnnot(page.getIndex(), content, addUndo, result);
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            } else if (result != null) {
                result.result(null, false);
            }
        }
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        try {
            if (annot.getModifiedDateTime() == null || content.getModifiedDate() == null || !annot.getModifiedDateTime().equals(content.getModifiedDate())) {
                AnnotHandler annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
                if (annotHandler != null) {
                    annotHandler.modifyAnnot(annot, content, addUndo, result);
                } else if (result != null) {
                    result.result(null, false);
                }
            } else if (result != null) {
                result.result(null, true);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        if (annot == getCurrentAnnot()) {
            setCurrentAnnot(null);
        }
        AnnotHandler annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
        if (annotHandler != null) {
            annotHandler.removeAnnot(annot, addUndo, result);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        Annot annot;
        AnnotHandler annotHandler;
        switch (motionEvent.getActionMasked()) {
            case 0:
                try {
                    annot = getInstance(this.mPdfViewCtrl).getCurrentAnnot();
                    if (annot != null) {
                        annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
                        if (annotHandler == null) {
                            return false;
                        }
                        if (annotHandler.onTouchEvent(pageIndex, motionEvent, annot)) {
                            hideSelectorAnnotMenu(this.mPdfViewCtrl);
                            return true;
                        }
                    }
                    PointF pdfPoint = AppAnnotUtil.getPdfPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
                    PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
                    if (page != null) {
                        annot = page.getAnnotAtPos(pdfPoint, AppAnnotUtil.ANNOT_SELECT_TOLERANCE);
                    }
                } catch (PDFException e1) {
                    e1.printStackTrace();
                    break;
                }
            case 1:
            case 2:
            case 3:
                annot = getInstance(this.mPdfViewCtrl).getCurrentAnnot();
                if (annot != null) {
                    annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
                    if (annotHandler != null && annotHandler.annotCanAnswer(annot)) {
                        hideSelectorAnnotMenu(this.mPdfViewCtrl);
                        return annotHandler.onTouchEvent(pageIndex, motionEvent, annot);
                    }
                }
                break;
            default:
                return false;
        }
        if (annot != null) {
            annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
            hideSelectorAnnotMenu(this.mPdfViewCtrl);
            return annotHandler.onTouchEvent(pageIndex, motionEvent, annot);
        }
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        boolean annotCanceled = false;
        try {
            AnnotHandler annotHandler;
            Annot annot = getInstance(this.mPdfViewCtrl).getCurrentAnnot();
            if (annot != null) {
                annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
                if (annotHandler != null && annotHandler.onLongPress(pageIndex, motionEvent, annot)) {
                    hideSelectorAnnotMenu(this.mPdfViewCtrl);
                    return true;
                } else if (getInstance(this.mPdfViewCtrl).getCurrentAnnot() == null) {
                    annotCanceled = true;
                }
            }
            PointF pdfPoint = AppAnnotUtil.getPdfPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            if (page != null) {
                annot = page.getAnnotAtPos(pdfPoint, AppAnnotUtil.ANNOT_SELECT_TOLERANCE);
            }
            if (annot != null && AppAnnotUtil.isSupportGroup(annot)) {
                annot = ((Markup) annot).getGroupHeader();
            }
            if (annot != null) {
                annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
                if (annotHandler != null && annotHandler.annotCanAnswer(annot) && annotHandler.onLongPress(pageIndex, motionEvent, annot)) {
                    hideSelectorAnnotMenu(this.mPdfViewCtrl);
                    return true;
                }
            }
            return annotCanceled;
        } catch (PDFException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        boolean annotCanceled = false;
        try {
            AnnotHandler annotHandler;
            Annot annot = getInstance(this.mPdfViewCtrl).getCurrentAnnot();
            if (annot != null) {
                annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
                if (annotHandler != null && annotHandler.onSingleTapConfirmed(pageIndex, motionEvent, annot)) {
                    hideSelectorAnnotMenu(this.mPdfViewCtrl);
                    return true;
                } else if (getInstance(this.mPdfViewCtrl).getCurrentAnnot() == null) {
                    annotCanceled = true;
                }
            }
            PointF pdfPoint = AppAnnotUtil.getPdfPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            if (page != null) {
                annot = page.getAnnotAtPos(pdfPoint, AppAnnotUtil.ANNOT_SELECT_TOLERANCE);
            }
            if (annot != null && AppAnnotUtil.isSupportGroup(annot)) {
                annot = ((Markup) annot).getGroupHeader();
            }
            if (annot != null) {
                annotHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getAnnotHandlerByType(getAnnotHandlerType(annot));
                if (annotHandler != null && annotHandler.annotCanAnswer(annot) && annotHandler.onSingleTapConfirmed(pageIndex, motionEvent, annot)) {
                    hideSelectorAnnotMenu(this.mPdfViewCtrl);
                    return true;
                }
            }
            if (annotCanceled) {
                return true;
            }
            return false;
        } catch (PDFException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    private void hideSelectorAnnotMenu(PDFViewCtrl pdfViewCtrl) {
        TextSelectToolHandler selectionTool = (TextSelectToolHandler) ((UIExtensionsManager) pdfViewCtrl.getUIExtensionsManager()).getToolHandlerByType(ToolHandler.TH_TYPE_TEXTSELECT);
        if (selectionTool != null) {
            selectionTool.mAnnotationMenu.dismiss();
        }
    }

    public void undo() {
        if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != null) {
            ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
        }
        if (getInstance(this.mPdfViewCtrl).getCurrentAnnot() != null) {
            getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        if (haveModifyTasks()) {
            this.mPdfViewCtrl.post(new Runnable() {
                public void run() {
                    DocumentManager.this.undo();
                }
            });
            return;
        }
        super.undo();
    }

    public void redo() {
        if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != null) {
            ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
        }
        if (getInstance(this.mPdfViewCtrl).getCurrentAnnot() != null) {
            getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        if (haveModifyTasks()) {
            this.mPdfViewCtrl.post(new Runnable() {
                public void run() {
                    DocumentManager.this.redo();
                }
            });
            return;
        }
        super.redo();
    }

    protected String getDiskCacheFolder() {
        return this.mPdfViewCtrl.getContext().getCacheDir().getParent();
    }

    protected boolean haveModifyTasks() {
        return this.mHasModifyTask;
    }

    public void setHasModifyTask(boolean hasModifyTask) {
        this.mHasModifyTask = hasModifyTask;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mCurAnnot == null || keyCode != 4) {
            return false;
        }
        setCurrentAnnot(null);
        return true;
    }

    private boolean isOwner(PDFDoc doc) {
        try {
            if (!doc.isEncrypted() || 3 == doc.getPasswordType()) {
                return true;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isSign(PDFDoc doc) {
        try {
            int count = doc.getSignatureCount();
            for (int i = 0; i < count; i++) {
                Signature signature = doc.getSignature(i);
                if (signature != null && signature.isSigned()) {
                    return true;
                }
            }
        } catch (PDFException e) {
        }
        return false;
    }

    protected void initDocProperties(PDFDoc doc) {
        if (doc != null) {
            try {
                this.mUserPermission = doc.getUserPermissions();
                this.mIsOwner = isOwner(doc);
                UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
                if (!(uiExtensionsManager == null || uiExtensionsManager.getModuleByName(Module.MODULE_NAME_DIGITALSIGNATURE) == null)) {
                    this.mMDPPermission = getMDPDigitalSignPermissionInDocument(doc);
                }
                this.mIsSign = isSign(doc);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }
}
