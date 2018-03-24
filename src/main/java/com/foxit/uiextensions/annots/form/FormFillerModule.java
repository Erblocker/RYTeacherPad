package com.foxit.uiextensions.annots.form;

import android.content.Context;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IPageEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.sdk.PDFViewCtrl.IScaleGestureEventListener;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.form.Form;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.utils.OnPageEventListener;

public class FormFillerModule implements Module, PropertyChangeListener {
    private FormFillerAnnotHandler mAnnotHandler;
    private Context mContext;
    private IDocEventListener mDocumentEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc document, int errCode) {
            if (errCode == 0 && document != null) {
                try {
                    if (document.hasForm()) {
                        FormFillerModule.this.mForm = document.getForm();
                        FormFillerModule.this.mAnnotHandler.init(FormFillerModule.this.mForm);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onDocWillClose(PDFDoc document) {
            FormFillerModule.this.mAnnotHandler.clear();
        }

        public void onDocClosed(PDFDoc document, int errCode) {
        }

        public void onDocWillSave(PDFDoc document) {
        }

        public void onDocSaved(PDFDoc document, int errCode) {
        }
    };
    private Form mForm = null;
    private IPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPagesInserted(boolean success, int dstIndex, int[] range) {
            if (success && !FormFillerModule.this.mAnnotHandler.hasInitialized()) {
                try {
                    if (FormFillerModule.this.mPdfViewCtrl.getDoc() != null && FormFillerModule.this.mPdfViewCtrl.getDoc().hasForm()) {
                        FormFillerModule.this.mForm = FormFillerModule.this.mPdfViewCtrl.getDoc().getForm();
                        FormFillerModule.this.mAnnotHandler.init(FormFillerModule.this.mForm);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private IScaleGestureEventListener mScaleGestureEventListener = new IScaleGestureEventListener() {
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (FormFillerModule.this.mAnnotHandler.getFormFillerAssist() != null) {
                FormFillerModule.this.mAnnotHandler.getFormFillerAssist().setScaling(true);
            }
            return false;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            if (FormFillerModule.this.mAnnotHandler.getFormFillerAssist() != null) {
                FormFillerModule.this.mAnnotHandler.getFormFillerAssist().setScaling(false);
            }
        }
    };
    private FormFillerToolHandler mToolHandler;
    IRecoveryEventListener memoryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
            DocumentManager.getInstance(FormFillerModule.this.mPdfViewCtrl).reInit();
        }

        public void onRecovered() {
            FormFillerModule.this.mToolHandler.initActionHandler();
        }
    };

    public FormFillerModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_FORMFILLER;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public boolean resetForm() {
        try {
            return this.mForm.reset();
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportFormToXML(String path) {
        try {
            return this.mForm.exportToXML(path);
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean importFormFromXML(String path) {
        try {
            return this.mForm.importFromXML(path);
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadModule() {
        this.mToolHandler = new FormFillerToolHandler(this.mPdfViewCtrl);
        this.mAnnotHandler = new FormFillerAnnotHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mPdfViewCtrl.registerDocEventListener(this.mDocumentEventListener);
        this.mPdfViewCtrl.registerPageEventListener(this.mPageEventListener);
        this.mPdfViewCtrl.registerScaleGestureEventListener(this.mScaleGestureEventListener);
        this.mPdfViewCtrl.registerRecoveryEventListener(this.memoryEventListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDocEventListener(this.mDocumentEventListener);
        this.mPdfViewCtrl.unregisterPageEventListener(this.mPageEventListener);
        this.mPdfViewCtrl.unregisterScaleGestureEventListener(this.mScaleGestureEventListener);
        this.mPdfViewCtrl.unregisterRecoveryEventListener(this.memoryEventListener);
        return true;
    }

    public void onValueChanged(long property, int value) {
    }

    public void onValueChanged(long property, float value) {
    }

    public void onValueChanged(long property, String value) {
    }

    public boolean onKeyBack() {
        return this.mAnnotHandler.onKeyBack();
    }
}
