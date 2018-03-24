package com.foxit.uiextensions.annots;

import android.widget.Toast;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.ActionHandler;
import com.foxit.sdk.common.IdentityProperties;

public class AnnotActionHandler extends ActionHandler {
    private PDFViewCtrl mPDFViewCtrl;

    public AnnotActionHandler(PDFViewCtrl pdfViewCtrl) {
        this.mPDFViewCtrl = pdfViewCtrl;
    }

    public int alert(String msg, String title, int type, int icon) {
        Toast.makeText(this.mPDFViewCtrl.getContext(), "alert...." + msg, 0).show();
        return 0;
    }

    public IdentityProperties getIdentityProperties() {
        IdentityProperties identityProperties = new IdentityProperties();
        identityProperties.setName("Foxit");
        return identityProperties;
    }
}
