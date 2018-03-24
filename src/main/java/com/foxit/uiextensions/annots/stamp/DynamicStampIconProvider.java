package com.foxit.uiextensions.annots.stamp;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.AnnotIconProvider;
import com.foxit.sdk.pdf.annots.ShadingColor;
import java.util.HashMap;
import java.util.UUID;

public class DynamicStampIconProvider extends AnnotIconProvider {
    private String id = UUID.randomUUID().toString();
    HashMap<String, PDFDoc> mDocMap = new HashMap();
    HashMap<PDFDoc, PDFPage> mDocPagePair = new HashMap();
    private int pageIndex = 0;
    private String version = "Version 3.0";

    public void addDocMap(String key, PDFDoc pdfDoc) {
        if (key != null && key.trim().length() >= 1 && this.mDocMap.get(key) == null) {
            this.mDocMap.put(key, pdfDoc);
        }
    }

    public void release() {
        for (PDFDoc pdfDoc : this.mDocMap.values()) {
            if (this.mDocPagePair.get(pdfDoc) != null) {
                try {
                    pdfDoc.closePage(((PDFPage) this.mDocPagePair.get(pdfDoc)).getIndex());
                    pdfDoc.release();
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }
        this.mDocPagePair.clear();
        this.mDocMap.clear();
    }

    public String getProviderID() {
        return this.id;
    }

    public String getProviderVersion() {
        return this.version;
    }

    public boolean hasIcon(int annotType, String iconName) {
        return true;
    }

    public boolean canChangeColor(int annotType, String iconName) {
        return true;
    }

    public PDFPage getIcon(int annotType, String iconName, long color) {
        if (this.mDocMap == null || this.mDocMap.get(new StringBuilder(String.valueOf(iconName)).append(annotType).toString()) == null || annotType == 1) {
            return null;
        }
        try {
            PDFDoc pdfDoc = (PDFDoc) this.mDocMap.get(new StringBuilder(String.valueOf(iconName)).append(annotType).toString());
            if (pdfDoc == null) {
                return null;
            }
            if (this.mDocPagePair.get(pdfDoc) != null) {
                return (PDFPage) this.mDocPagePair.get(pdfDoc);
            }
            PDFPage page = pdfDoc.getPage(this.pageIndex);
            this.mDocPagePair.put(pdfDoc, page);
            return page;
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ShadingColor getShadingColor(int annotType, String iconName, long refColor, int shadingIndex) {
        return null;
    }

    public float getDisplayWidth(int annotType, String iconName) {
        return 0.0f;
    }

    public float getDisplayHeight(int annotType, String iconName) {
        return 0.0f;
    }
}
