package com.foxit.sdk.pdf;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.Pause;
import com.foxit.sdk.pdf.annots.Annot;

public class Renderer {
    public static final int e_colorModeMapping = 2;
    public static final int e_colorModeNormal = 0;
    public static final int e_renderAnnot = 2;
    public static final int e_renderPage = 1;
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected Renderer(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(Renderer renderer) {
        return renderer == null ? 0 : renderer.a;
    }

    private synchronized void a() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PDFJNI.Renderer_release(this.a, this);
            }
            this.a = 0;
        }
    }

    protected static Bitmap createBitmap(int i, int i2, int i3) {
        Config config = Config.ARGB_8888;
        switch (i3) {
            case 1:
                config = Config.ALPHA_8;
                break;
            case 3:
                config = Config.RGB_565;
                break;
            case 4:
                config = Config.ARGB_4444;
                break;
            case 5:
                config = Config.ARGB_8888;
                break;
        }
        return Bitmap.createBitmap(i, i2, config);
    }

    public static Renderer create(Bitmap bitmap) throws PDFException {
        if (bitmap == null) {
            throw new PDFException(8);
        } else if (bitmap.getConfig() != Config.ARGB_8888) {
            throw new PDFException(9);
        } else {
            long Renderer_create = PDFJNI.Renderer_create(bitmap, true);
            if (Renderer_create != 0) {
                return Renderer_create == 0 ? null : new Renderer(Renderer_create, true);
            } else {
                throw new PDFException(4);
            }
        }
    }

    public void release() throws PDFException {
        a();
    }

    public int startRender(PDFPage pDFPage, Matrix matrix, Pause pause) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage == null || matrix == null) {
            throw new PDFException(8);
        } else if (pDFPage.isParsed()) {
            return PDFJNI.Renderer_startRender(this.a, this, PDFPage.getCPtr(pDFPage), pDFPage, matrix, pause);
        } else {
            throw new PDFException(12);
        }
    }

    public int startRenderReflowPage(ReflowPage reflowPage, Matrix matrix, Pause pause) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (reflowPage == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (matrix != null) {
            return PDFJNI.Renderer_startRenderReflowPage(this.a, this, ReflowPage.getCPtr(reflowPage), reflowPage, matrix, pause);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public int continueRender() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.Renderer_continueRender(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean renderAnnot(Annot annot, Matrix matrix) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (annot == null || matrix == null) {
            throw new PDFException(8);
        } else {
            return PDFJNI.Renderer_renderAnnot(this.a, this, ((Long) a.a(annot.getClass(), "getCPtr", (Object) annot)).longValue(), annot, matrix);
        }
    }

    public void setRenderContent(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.Renderer_setRenderContent(this.a, this, j);
    }

    public void setTransformAnnotIcon(boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.Renderer_setTransformAnnotIcon(this.a, this, z);
    }

    public void setColorMode(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.Renderer_setColorMode(this.a, this, i);
    }

    public void setMappingModeColors(long j, long j2) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.Renderer_setMappingModeColors(this.a, this, j, j2);
    }

    public void setForceHalftone(boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.Renderer_setForceHalftone(this.a, this, z);
    }
}
