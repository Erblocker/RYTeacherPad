package com.foxit.uiextensions.annots.note;

import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class NoteUndoItem extends AnnotUndoItem {
    String mIconName;
    boolean mIsFromReplyModule = false;
    boolean mOpenStatus;
    String mParentNM;
}
