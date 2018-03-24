package com.foxit.uiextensions.annots.textmarkup.strikeout;

import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class StrikeoutUndoItem extends AnnotUndoItem {
    QuadPoints[] mQuadPoints;
}
