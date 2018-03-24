package com.foxit.uiextensions.annots.textmarkup.highlight;

import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class HighlightUndoItem extends AnnotUndoItem {
    QuadPoints[] quadPointsArray;
}
