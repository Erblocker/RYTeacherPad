package com.foxit.uiextensions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IDoubleTapEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.caret.CaretModule;
import com.foxit.uiextensions.annots.circle.CircleModule;
import com.foxit.uiextensions.annots.form.FormFillerModule;
import com.foxit.uiextensions.annots.form.FormNavigationModule;
import com.foxit.uiextensions.annots.freetext.typewriter.TypewriterModule;
import com.foxit.uiextensions.annots.ink.EraserModule;
import com.foxit.uiextensions.annots.ink.InkModule;
import com.foxit.uiextensions.annots.line.LineModule;
import com.foxit.uiextensions.annots.link.LinkModule;
import com.foxit.uiextensions.annots.note.NoteModule;
import com.foxit.uiextensions.annots.square.SquareModule;
import com.foxit.uiextensions.annots.stamp.StampModule;
import com.foxit.uiextensions.annots.textmarkup.highlight.HighlightModule;
import com.foxit.uiextensions.annots.textmarkup.squiggly.SquigglyModule;
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutModule;
import com.foxit.uiextensions.annots.textmarkup.underline.UnderlineModule;
import com.foxit.uiextensions.modules.PageNavigationModule;
import com.foxit.uiextensions.modules.UndoModule;
import com.foxit.uiextensions.modules.signature.SignatureModule;
import com.foxit.uiextensions.security.digitalsignature.DigitalSignatureModule;
import com.foxit.uiextensions.security.standard.PasswordModule;
import com.foxit.uiextensions.textselect.TextSelectModule;
import com.foxit.uiextensions.textselect.TextSelectToolHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UIExtensionsManager implements com.foxit.sdk.PDFViewCtrl.UIExtensionsManager {
    private SparseArray<AnnotHandler> mAnnotHandlerList;
    private ArrayList<ConfigurationChangedListener> mConfigurationChangedListeners;
    private ToolHandler mCurToolHandler = null;
    IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc document, int errCode) {
            if (errCode == PDFError.NO_ERROR.getCode() && document != null) {
                DocumentManager.getInstance(UIExtensionsManager.this.mPdfViewCtrl).initDocProperties(document);
            }
        }

        public void onDocWillClose(PDFDoc document) {
        }

        public void onDocClosed(PDFDoc document, int errCode) {
        }

        public void onDocWillSave(PDFDoc document) {
        }

        public void onDocSaved(PDFDoc document, int errCode) {
        }
    };
    IDoubleTapEventListener mDoubleTapEventListener = new IDoubleTapEventListener() {
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            if (DocumentManager.getInstance(UIExtensionsManager.this.mPdfViewCtrl).getCurrentAnnot() == null) {
                return false;
            }
            DocumentManager.getInstance(UIExtensionsManager.this.mPdfViewCtrl).setCurrentAnnot(null);
            return true;
        }

        public boolean onDoubleTap(MotionEvent motionEvent) {
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    };
    private boolean mEnableLinkAnnot = true;
    private ArrayList<ToolHandlerChangedListener> mHandlerChangedListeners;
    private final List<Module> mModules = new ArrayList();
    private PDFViewCtrl mPdfViewCtrl = null;
    private int mSelectHighlightColor = -5448979;
    private HashMap<String, ToolHandler> mToolHandlerList;

    public interface ConfigurationChangedListener {
        void onConfigurationChanged(Configuration configuration);
    }

    public interface ToolHandlerChangedListener {
        void onToolHandlerChanged(ToolHandler toolHandler, ToolHandler toolHandler2);
    }

    public UIExtensionsManager(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        if (pdfViewCtrl == null) {
            throw new NullPointerException("PDF view control can't be null");
        }
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mToolHandlerList = new HashMap(8);
        this.mAnnotHandlerList = new SparseArray(8);
        this.mHandlerChangedListeners = new ArrayList();
        this.mConfigurationChangedListeners = new ArrayList();
        TextSelectModule textSelectModule = new TextSelectModule(context, parent, pdfViewCtrl);
        registerModule(textSelectModule);
        textSelectModule.loadModule();
        registerToolHandler(textSelectModule.getToolHandler());
        SquigglyModule squigglyModule = new SquigglyModule(context, parent, pdfViewCtrl);
        registerModule(squigglyModule);
        squigglyModule.loadModule();
        registerAnnotHandler(squigglyModule.getAnnotHandler());
        registerToolHandler(squigglyModule.getToolHandler());
        StrikeoutModule strikeoutModule = new StrikeoutModule(context, parent, pdfViewCtrl);
        registerModule(strikeoutModule);
        strikeoutModule.loadModule();
        registerAnnotHandler(strikeoutModule.getAnnotHandler());
        registerToolHandler(strikeoutModule.getToolHandler());
        UnderlineModule underlineModule = new UnderlineModule(context, parent, pdfViewCtrl);
        registerModule(underlineModule);
        underlineModule.loadModule();
        registerAnnotHandler(underlineModule.getAnnotHandler());
        registerToolHandler(underlineModule.getToolHandler());
        HighlightModule hltModule = new HighlightModule(context, parent, pdfViewCtrl);
        registerModule(hltModule);
        hltModule.loadModule();
        registerAnnotHandler(hltModule.getAnnotHandler());
        registerToolHandler(hltModule.getToolHandler());
        NoteModule noteModule = new NoteModule(context, parent, pdfViewCtrl);
        registerModule(noteModule);
        noteModule.loadModule();
        registerAnnotHandler(noteModule.getAnnotHandler());
        registerToolHandler(noteModule.getToolHandler());
        LinkModule linkModule = new LinkModule(context, parent, pdfViewCtrl);
        linkModule.loadModule();
        registerAnnotHandler(linkModule.getAnnotHandler());
        CircleModule circleModule = new CircleModule(context, parent, pdfViewCtrl);
        registerModule(circleModule);
        circleModule.loadModule();
        registerAnnotHandler(circleModule.getAnnotHandler());
        registerToolHandler(circleModule.getToolHandler());
        SquareModule squareModule = new SquareModule(context, parent, pdfViewCtrl);
        registerModule(squareModule);
        squareModule.loadModule();
        registerAnnotHandler(squareModule.getAnnotHandler());
        registerToolHandler(squareModule.getToolHandler());
        TypewriterModule typewriterModule = new TypewriterModule(context, parent, pdfViewCtrl);
        registerModule(typewriterModule);
        typewriterModule.loadModule();
        registerAnnotHandler(typewriterModule.getAnnotHandler());
        registerToolHandler(typewriterModule.getToolHandler());
        StampModule stampModule = new StampModule(context, parent, pdfViewCtrl);
        registerModule(stampModule);
        stampModule.loadModule();
        registerAnnotHandler(stampModule.getAnnotHandler());
        registerToolHandler(stampModule.getToolHandler());
        CaretModule caretModule = new CaretModule(context, parent, pdfViewCtrl);
        registerModule(caretModule);
        caretModule.loadModule();
        registerAnnotHandler(caretModule.getAnnotHandler());
        registerToolHandler(caretModule.getISToolHandler());
        registerToolHandler(caretModule.getRPToolHandler());
        EraserModule eraserModule = new EraserModule(context, parent, pdfViewCtrl);
        registerModule(eraserModule);
        eraserModule.loadModule();
        registerToolHandler(eraserModule.getToolHandler());
        InkModule inkModule = new InkModule(context, parent, pdfViewCtrl);
        registerModule(inkModule);
        inkModule.loadModule();
        registerAnnotHandler(inkModule.getAnnotHandler());
        registerToolHandler(inkModule.getToolHandler());
        LineModule lineModule = new LineModule(context, parent, pdfViewCtrl);
        registerModule(lineModule);
        lineModule.loadModule();
        registerAnnotHandler(lineModule.getAnnotHandler());
        registerToolHandler(lineModule.getLineToolHandler());
        registerToolHandler(lineModule.getArrowToolHandler());
        PageNavigationModule pageNavigationModule = new PageNavigationModule(context, parent, pdfViewCtrl);
        registerModule(pageNavigationModule);
        pageNavigationModule.loadModule();
        FormNavigationModule formNavigationModule = new FormNavigationModule(context, parent);
        registerModule(formNavigationModule);
        formNavigationModule.loadModule();
        FormFillerModule formFillerModule = new FormFillerModule(context, parent, pdfViewCtrl);
        registerModule(formFillerModule);
        formFillerModule.loadModule();
        registerToolHandler(formFillerModule.getToolHandler());
        registerAnnotHandler(formFillerModule.getAnnotHandler());
        UndoModule undoModule = new UndoModule(context, parent, pdfViewCtrl);
        registerModule(undoModule);
        undoModule.loadModule();
        registerAnnotHandler(undoModule.getAnnotHandler());
        SignatureModule signatureModule = new SignatureModule(context, parent, pdfViewCtrl);
        registerModule(signatureModule);
        signatureModule.loadModule();
        registerToolHandler(signatureModule.getToolHandler());
        DigitalSignatureModule dsgModule = new DigitalSignatureModule(context, parent, pdfViewCtrl);
        registerModule(dsgModule);
        dsgModule.loadModule();
        registerAnnotHandler(dsgModule.getAnnotHandler());
        PasswordModule passwordModule = new PasswordModule(context, pdfViewCtrl);
        registerModule(passwordModule);
        passwordModule.loadModule();
        pdfViewCtrl.registerDocEventListener(this.mDocEventListener);
        final PDFViewCtrl pDFViewCtrl = pdfViewCtrl;
        pdfViewCtrl.registerRecoveryEventListener(new IRecoveryEventListener() {
            public void onWillRecover() {
                DocumentManager.getInstance(pDFViewCtrl).mCurAnnot = null;
            }

            public void onRecovered() {
                DocumentManager.getInstance(UIExtensionsManager.this.mPdfViewCtrl).initDocProperties(pDFViewCtrl.getDoc());
            }
        });
        pdfViewCtrl.registerDoubleTapEventListener(this.mDoubleTapEventListener);
    }

    public PDFViewCtrl getPDFViewCtrl() {
        return this.mPdfViewCtrl;
    }

    public void registerToolHandlerChangedListener(ToolHandlerChangedListener listener) {
        this.mHandlerChangedListeners.add(listener);
    }

    public void unregisterToolHandlerChangedListener(ToolHandlerChangedListener listener) {
        this.mHandlerChangedListeners.remove(listener);
    }

    private void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
        Iterator it = this.mHandlerChangedListeners.iterator();
        while (it.hasNext()) {
            ((ToolHandlerChangedListener) it.next()).onToolHandlerChanged(lastTool, currentTool);
        }
    }

    public void registerConfigurationChangedListener(ConfigurationChangedListener listener) {
        this.mConfigurationChangedListeners.add(listener);
    }

    public void unregisterConfigurationChangedListener(ConfigurationChangedListener listener) {
        this.mConfigurationChangedListeners.remove(listener);
    }

    public void onConfigurationChanged(Configuration config) {
        Iterator it = this.mConfigurationChangedListeners.iterator();
        while (it.hasNext()) {
            ((ConfigurationChangedListener) it.next()).onConfigurationChanged(config);
        }
    }

    public void setCurrentToolHandler(ToolHandler toolHandler) {
        if ((toolHandler == null && this.mCurToolHandler == null) || !DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            return;
        }
        if (toolHandler == null || this.mCurToolHandler == null || this.mCurToolHandler.getType() != toolHandler.getType()) {
            ToolHandler lastToolHandler = this.mCurToolHandler;
            if (lastToolHandler != null) {
                lastToolHandler.onDeactivate();
            }
            if (!(toolHandler == null || DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() == null)) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            this.mCurToolHandler = toolHandler;
            if (this.mCurToolHandler != null) {
                this.mCurToolHandler.onActivate();
            }
            onToolHandlerChanged(lastToolHandler, this.mCurToolHandler);
        }
    }

    public ToolHandler getCurrentToolHandler() {
        return this.mCurToolHandler;
    }

    public void registerToolHandler(ToolHandler handler) {
        this.mToolHandlerList.put(handler.getType(), handler);
    }

    public void unregisterToolHandler(ToolHandler handler) {
        this.mToolHandlerList.remove(handler.getType());
    }

    public ToolHandler getToolHandlerByType(String type) {
        return (ToolHandler) this.mToolHandlerList.get(type);
    }

    protected void registerAnnotHandler(AnnotHandler handler) {
        this.mAnnotHandlerList.put(handler.getType(), handler);
    }

    protected void unregisterAnnotHandler(AnnotHandler handler) {
        this.mAnnotHandlerList.remove(handler.getType());
    }

    protected AnnotHandler getCurrentAnnotHandler() {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot == null) {
            return null;
        }
        return getAnnotHandlerByType(DocumentManager.getAnnotHandlerType(curAnnot));
    }

    protected AnnotHandler getAnnotHandlerByType(int type) {
        return (AnnotHandler) this.mAnnotHandlerList.get(type);
    }

    public void registerModule(Module module) {
        this.mModules.add(module);
    }

    public void unregisterModule(Module module) {
        this.mModules.remove(module);
    }

    public Module getModuleByName(String name) {
        for (Module module : this.mModules) {
            String moduleName = module.getName();
            if (moduleName != null && moduleName.compareTo(name) == 0) {
                return module;
            }
        }
        return null;
    }

    public void enableLinks(boolean enable) {
        this.mEnableLinkAnnot = enable;
    }

    public boolean isLinksEnabled() {
        return this.mEnableLinkAnnot;
    }

    public void setSelectionHighlightColor(int color) {
        this.mSelectHighlightColor = color;
    }

    public int getSelectionHighlightColor() {
        return this.mSelectHighlightColor;
    }

    public String getCurrentSelectedText() {
        ToolHandler selectionTool = getToolHandlerByType(ToolHandler.TH_TYPE_TEXTSELECT);
        if (selectionTool != null) {
            return ((TextSelectToolHandler) selectionTool).getCurrentSelectedText();
        }
        return null;
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        if (this.mPdfViewCtrl.getPageLayoutMode() == 3 || motionEvent.getPointerCount() > 1) {
            return false;
        }
        if (this.mCurToolHandler != null) {
            if (this.mCurToolHandler.onTouchEvent(pageIndex, motionEvent)) {
                return true;
            }
            return false;
        } else if (DocumentManager.getInstance(this.mPdfViewCtrl).onTouchEvent(pageIndex, motionEvent)) {
            return true;
        } else {
            ToolHandler selectionTool = getToolHandlerByType(ToolHandler.TH_TYPE_TEXTSELECT);
            if (selectionTool == null || !selectionTool.onTouchEvent(pageIndex, motionEvent)) {
                return false;
            }
            return true;
        }
    }

    public boolean shouldViewCtrlDraw(Annot annot) {
        return DocumentManager.getInstance(this.mPdfViewCtrl).shouldViewCtrlDraw(annot);
    }

    public Annot getFocusAnnot() {
        return DocumentManager.getInstance(this.mPdfViewCtrl).getFocusAnnot();
    }

    @SuppressLint({"WrongCall"})
    public void onDraw(int pageIndex, Canvas canvas) {
        for (ToolHandler handler : this.mToolHandlerList.values()) {
            handler.onDraw(pageIndex, canvas);
        }
        for (int i = 0; i < this.mAnnotHandlerList.size(); i++) {
            AnnotHandler handler2 = (AnnotHandler) this.mAnnotHandlerList.get(this.mAnnotHandlerList.keyAt(i));
            if (handler2 != null) {
                handler2.onDraw(pageIndex, canvas);
            }
        }
    }

    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        if (this.mPdfViewCtrl.getPageLayoutMode() == 3 || motionEvent.getPointerCount() > 1) {
            return false;
        }
        int pageIndex = this.mPdfViewCtrl.getPageIndex(new PointF(motionEvent.getX(), motionEvent.getY()));
        if (this.mCurToolHandler != null) {
            if (this.mCurToolHandler.onSingleTapConfirmed(pageIndex, motionEvent)) {
                return true;
            }
            return false;
        } else if (DocumentManager.getInstance(this.mPdfViewCtrl).onSingleTapConfirmed(pageIndex, motionEvent)) {
            return true;
        } else {
            ToolHandler selectionTool = getToolHandlerByType(ToolHandler.TH_TYPE_TEXTSELECT);
            if (selectionTool != null && selectionTool.onSingleTapConfirmed(pageIndex, motionEvent)) {
                return true;
            }
            if (DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() == null) {
                return false;
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            return true;
        }
    }

    public boolean onDoubleTap(MotionEvent motionEvent) {
        return false;
    }

    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    public void onShowPress(MotionEvent motionEvent) {
    }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    public void onLongPress(MotionEvent motionEvent) {
        if (this.mPdfViewCtrl.getPageLayoutMode() != 3 && motionEvent.getPointerCount() <= 1) {
            int pageIndex = this.mPdfViewCtrl.getPageIndex(new PointF(motionEvent.getX(), motionEvent.getY()));
            if (this.mCurToolHandler != null) {
                if (!this.mCurToolHandler.onLongPress(pageIndex, motionEvent)) {
                }
            } else if (!DocumentManager.getInstance(this.mPdfViewCtrl).onLongPress(pageIndex, motionEvent)) {
                ToolHandler selectionTool = getToolHandlerByType(ToolHandler.TH_TYPE_TEXTSELECT);
                if (selectionTool != null && selectionTool.onLongPress(pageIndex, motionEvent)) {
                }
            }
        }
    }

    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
    }
}
