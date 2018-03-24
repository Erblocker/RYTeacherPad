package com.foxit.uiextensions.annots.textmarkup.underline;

import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class UnderlineUndoItem extends AnnotUndoItem {
    QuadPoints[] mQuadPoints;
}
