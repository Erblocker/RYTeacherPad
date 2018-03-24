package com.foxit.uiextensions.annots.ink;

import android.content.Context;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;

public class EraserModule implements Module {
    private EraserToolHandler mToolHandler;

    public EraserModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mToolHandler = new EraserToolHandler(context, parent, pdfViewCtrl);
    }

    public String getName() {
        return Module.MODULE_NAME_ERASER;
    }

    public boolean loadModule() {
        this.mToolHandler.initUiElements();
        return false;
    }

    public boolean unloadModule() {
        return true;
    }

    public ToolHandler getToolHandler() {
        return this.mToolHandler;
    }
}
