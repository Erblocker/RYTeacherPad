package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.pdf.PDFPage;

public abstract class AnnotIconProvider {
    public abstract boolean canChangeColor(int i, String str);

    public abstract float getDisplayHeight(int i, String str);

    public abstract float getDisplayWidth(int i, String str);

    public abstract PDFPage getIcon(int i, String str, long j);

    public abstract String getProviderID();

    public abstract String getProviderVersion();

    public abstract ShadingColor getShadingColor(int i, String str, long j, int i2);

    public abstract boolean hasIcon(int i, String str);

    public abstract void release();
}
