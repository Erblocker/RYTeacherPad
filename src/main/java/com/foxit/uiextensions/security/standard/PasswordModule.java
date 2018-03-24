package com.foxit.uiextensions.security.standard;

import android.content.Context;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.Module;

public class PasswordModule implements Module {
    private Context mContext;
    private PDFViewCtrl mPdfViewCtrl;
    private PasswordSecurityHandler mSecurityHandler = null;
    private PasswordStandardSupport mSupport = null;

    public PasswordModule(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_PASSWORD;
    }

    public boolean loadModule() {
        this.mSupport = new PasswordStandardSupport(this.mContext, this.mPdfViewCtrl);
        this.mSecurityHandler = new PasswordSecurityHandler(this.mSupport, this.mPdfViewCtrl);
        return true;
    }

    public boolean unloadModule() {
        this.mSupport = null;
        return true;
    }

    public PasswordStandardSupport getPasswordSupport() {
        return this.mSupport;
    }

    public PasswordSecurityHandler getSecurityHandler() {
        return this.mSecurityHandler;
    }
}
