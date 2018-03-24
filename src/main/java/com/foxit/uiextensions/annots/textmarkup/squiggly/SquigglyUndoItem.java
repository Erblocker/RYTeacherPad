package com.foxit.uiextensions.annots.textmarkup.squiggly;

import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class SquigglyUndoItem extends AnnotUndoItem {
    QuadPoints[] mQuadPoints;
}
