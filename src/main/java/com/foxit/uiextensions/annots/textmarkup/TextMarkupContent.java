package com.foxit.uiextensions.annots.textmarkup;

import android.graphics.PointF;
import com.foxit.uiextensions.annots.AnnotContent;
import java.util.ArrayList;

public interface TextMarkupContent extends AnnotContent {
    ArrayList<PointF> getQuadPoints();
}
