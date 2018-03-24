package com.foxit.uiextensions.annots.line;

import android.graphics.PointF;
import com.foxit.uiextensions.annots.AnnotContent;
import java.util.ArrayList;

public interface LineAnnotContent extends AnnotContent {
    ArrayList<PointF> getEndingPoints();

    ArrayList<String> getEndingStyles();

    boolean isCaption();
}
