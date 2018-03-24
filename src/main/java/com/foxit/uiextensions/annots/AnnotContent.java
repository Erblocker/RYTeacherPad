package com.foxit.uiextensions.annots;

import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;

public interface AnnotContent {
    RectF getBBox();

    int getColor();

    String getContents();

    String getIntent();

    float getLineWidth();

    DateTime getModifiedDate();

    String getNM();

    int getOpacity();

    int getPageIndex();

    String getSubject();

    int getType();
}
