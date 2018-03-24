package com.foxit.uiextensions;

import java.io.Serializable;

public interface IUndoItem extends Serializable {
    boolean redo();

    boolean undo();
}
