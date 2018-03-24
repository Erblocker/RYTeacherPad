package com.foxit.uiextensions.modules;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.controls.dialog.AppDialogManager;

public class ThumbnailModule implements Module {
    private final Context mContext;
    private final PDFViewCtrl mPdfView;
    private boolean mSinglePage = true;

    public ThumbnailModule(Context context, PDFViewCtrl pdfView) {
        this.mContext = context;
        this.mPdfView = pdfView;
    }

    public void show() {
        initApplyValue();
        applyValue();
        showThumbnailDialog();
    }

    public String getName() {
        return Module.MODULE_NAME_THUMBNAIL;
    }

    public boolean loadModule() {
        return true;
    }

    public boolean unloadModule() {
        return true;
    }

    private void initApplyValue() {
        boolean z = true;
        if (getViewModePosition() != 1) {
            z = false;
        }
        this.mSinglePage = z;
    }

    private void applyValue() {
        if (this.mSinglePage) {
            this.mPdfView.setPageLayoutMode(1);
        } else {
            this.mPdfView.setPageLayoutMode(2);
        }
        PageNavigationModule pageNumberJump = (PageNavigationModule) ((UIExtensionsManager) this.mPdfView.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PAGENAV);
        if (pageNumberJump != null) {
            pageNumberJump.resetJumpView();
        }
    }

    private int getViewModePosition() {
        switch (this.mPdfView.getPageLayoutMode()) {
            case 2:
                return 2;
            default:
                return 1;
        }
    }

    private void showThumbnailDialog() {
        if (DocumentManager.getInstance(this.mPdfView).getCurrentAnnot() != null) {
            DocumentManager.getInstance(this.mPdfView).setCurrentAnnot(null);
        }
        FragmentActivity act = this.mContext;
        ThumbnailSupport support = (ThumbnailSupport) act.getSupportFragmentManager().findFragmentByTag("ThumbnailSupport");
        if (support == null) {
            support = new ThumbnailSupport();
        }
        support.init(this.mPdfView);
        AppDialogManager.getInstance().showAllowManager(support, act.getSupportFragmentManager(), "ThumbnailSupport", null);
    }
}
