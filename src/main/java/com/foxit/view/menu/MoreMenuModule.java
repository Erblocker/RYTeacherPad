package com.foxit.view.menu;

import android.content.Context;
import android.content.res.Configuration;
import android.view.ViewGroup;
import com.foxit.read.IRD_StateChangeListener;
import com.foxit.read.RD_Read;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.annots.form.FormFillerModule;
import com.foxit.uiextensions.security.standard.PasswordModule;

public class MoreMenuModule implements Module {
    private Context mContext;
    private IDocEventListener mDocumentEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc document, int errCode) {
            if (errCode == PDFError.NO_ERROR.getCode()) {
                if (MoreMenuModule.this.mHasFormFillerModule) {
                    MoreMenuModule.this.moreMenuView.reloadFormItems();
                }
                if (MoreMenuModule.this.mHasPasswordModule) {
                    MoreMenuModule.this.moreMenuView.reloadPasswordItem(MoreMenuModule.this.mPasswordModule);
                    try {
                        if (MoreMenuModule.this.mPdfViewer.getDoc().getEncryptionType() == 1) {
                            MoreMenuModule.this.mPasswordModule.getPasswordSupport().isOwner();
                        }
                        RD_Read access$5 = MoreMenuModule.this.mRead;
                        MoreMenuModule moreMenuModule = MoreMenuModule.this;
                        IRD_StateChangeListener anonymousClass1 = new IRD_StateChangeListener() {
                            public void onStateChanged(int oldState, int newState) {
                                MoreMenuModule.this.moreMenuView.reloadPasswordItem(MoreMenuModule.this.mPasswordModule);
                            }
                        };
                        moreMenuModule.mPasswordStateChangeListner = anonymousClass1;
                        access$5.registerStateChangeListener(anonymousClass1);
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void onDocWillClose(PDFDoc document) {
        }

        public void onDocClosed(PDFDoc document, int errCode) {
            if (errCode == PDFError.NO_ERROR.getCode() && MoreMenuModule.this.mHasPasswordModule) {
                MoreMenuModule.this.mPasswordModule.getPasswordSupport().setDocOpenAuthEvent(true);
                MoreMenuModule.this.mPasswordModule.getPasswordSupport().setIsOwner(false);
                MoreMenuModule.this.mRead.unregisterStateChangeListener(MoreMenuModule.this.mPasswordStateChangeListner);
            }
        }

        public void onDocWillSave(PDFDoc document) {
        }

        public void onDocSaved(PDFDoc document, int errCode) {
        }
    };
    private String mFilePath = null;
    private FormFillerModule mFormFillerModule = null;
    private boolean mHasDocInfoModule = false;
    private boolean mHasFormFillerModule = false;
    private boolean mHasPasswordModule = false;
    private ViewGroup mParent = null;
    private PasswordModule mPasswordModule = null;
    private IRD_StateChangeListener mPasswordStateChangeListner;
    private PDFViewCtrl mPdfViewer;
    private RD_Read mRead;
    private MoreMenuView moreMenuView = null;

    public MoreMenuModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewer, RD_Read read, String filePath) {
        this.mContext = context;
        this.mPdfViewer = pdfViewer;
        this.mParent = parent;
        this.mRead = read;
        this.mFilePath = filePath;
    }

    public MoreMenuView getView() {
        return this.moreMenuView;
    }

    public String getName() {
        return Module.MODULE_MORE_MENU;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.moreMenuView != null) {
            this.moreMenuView.onConfigurationChanged(newConfig);
        }
    }

    public boolean loadModule() {
        if (this.moreMenuView == null) {
            this.moreMenuView = new MoreMenuView(this.mContext, this.mParent, this.mPdfViewer, this.mRead, this.mFilePath);
        }
        this.moreMenuView.initView();
        if (this.mHasDocInfoModule) {
            this.moreMenuView.addDocInfoItem();
        }
        if (this.mHasPasswordModule) {
            this.moreMenuView.addPasswordItems(this.mPasswordModule);
        }
        if (this.mHasFormFillerModule) {
            this.moreMenuView.addFormItem(this.mFormFillerModule);
        }
        this.mRead.getDocViewer().registerDocEventListener(this.mDocumentEventListener);
        return true;
    }

    public boolean unloadModule() {
        return true;
    }

    public void configFormFillerModule(Module module) {
        if (module != null) {
            this.mHasFormFillerModule = true;
            this.mFormFillerModule = (FormFillerModule) module;
        }
    }

    public void configDocInfoModule(Module module) {
        if (module != null) {
            this.mHasDocInfoModule = true;
        }
    }

    public void configPasswordModule(Module module) {
        if (module != null) {
            this.mHasPasswordModule = true;
            this.mPasswordModule = (PasswordModule) module;
        }
    }
}
