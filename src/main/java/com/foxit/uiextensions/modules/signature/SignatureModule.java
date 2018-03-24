package com.foxit.uiextensions.modules.signature;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;

public class SignatureModule implements Module {
    private Context mContext;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            if (SignatureModule.this.mToolHandler != null) {
                SignatureModule.this.mToolHandler.onDrawForControls(canvas);
            }
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private SignatureToolHandler mToolHandler;

    public SignatureModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public String getName() {
        return Module.MODULE_NAME_PSISIGNATURE;
    }

    public boolean loadModule() {
        this.mToolHandler = new SignatureToolHandler(this.mContext, this.mParent, this.mPdfViewCtrl);
        this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public boolean onKeyBack() {
        return this.mToolHandler.onKeyBack();
    }
}
