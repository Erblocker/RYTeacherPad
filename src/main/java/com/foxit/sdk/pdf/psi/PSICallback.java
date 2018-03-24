package com.foxit.sdk.pdf.psi;

import android.graphics.RectF;

public abstract class PSICallback {
    public abstract void refresh(PSI psi, RectF rectF);

    public void release() {
    }
}
