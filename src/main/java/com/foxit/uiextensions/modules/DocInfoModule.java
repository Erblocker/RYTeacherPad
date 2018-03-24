package com.foxit.uiextensions.modules;

import android.content.Context;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.Module;

public class DocInfoModule implements Module {
    private Context mContext;
    private DocInfoView mDocInfo = null;
    private String mFilePath = null;
    private ViewGroup mParent = null;
    private PDFViewCtrl mPdfViewCtrl;

    public DocInfoModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, String filePath) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mFilePath = filePath;
        this.mParent = parent;
    }

    public DocInfoView getView() {
        return this.mDocInfo;
    }

    public String getName() {
        return Module.MODULE_NAME_DOCINFO;
    }

    public boolean loadModule() {
        this.mDocInfo = new DocInfoView(this.mContext, this.mPdfViewCtrl);
        if (this.mDocInfo == null) {
            return false;
        }
        this.mDocInfo.init(this.mFilePath);
        return true;
    }

    public boolean unloadModule() {
        return true;
    }

    public String getFilePath() {
        return this.mFilePath;
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
        if (this.mDocInfo != null) {
            this.mDocInfo.init(this.mFilePath);
        }
    }
}
