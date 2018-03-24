package com.foxit.uiextensions.annots.textmarkup;

import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.utils.AppDmUtil;

public abstract class TextMarkupContentAbs implements AnnotContent {
    public abstract TextSelector getTextSelector();

    public String getNM() {
        return AppDmUtil.randomUUID(null);
    }

    public RectF getBBox() {
        return getTextSelector().getBbox();
    }

    public int getColor() {
        return 0;
    }

    public int getOpacity() {
        return 0;
    }

    public float getLineWidth() {
        return 0.0f;
    }

    public String getSubject() {
        return null;
    }

    public DateTime getModifiedDate() {
        try {
            return new DateTime();
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getContents() {
        return getTextSelector().getContents();
    }
}
