package com.foxit.uiextensions.annots.ink;

import android.graphics.PointF;
import com.foxit.uiextensions.annots.AnnotContent;
import java.util.ArrayList;

public interface InkAnnotContent extends AnnotContent {
    ArrayList<ArrayList<PointF>> getInkLisk();
}
