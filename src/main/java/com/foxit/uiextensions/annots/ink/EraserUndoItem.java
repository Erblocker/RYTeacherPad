package com.foxit.uiextensions.annots.ink;

import com.foxit.uiextensions.annots.AnnotUndoItem;
import java.util.ArrayList;
import java.util.Iterator;

public class EraserUndoItem extends AnnotUndoItem {
    private ArrayList<InkUndoItem> undoItems = new ArrayList();

    public void addUndoItem(InkUndoItem undoItem) {
        this.undoItems.add(undoItem);
    }

    public boolean undo() {
        for (int i = this.undoItems.size() - 1; i >= 0; i--) {
            InkUndoItem undoItem = (InkUndoItem) this.undoItems.get(i);
            if (undoItem instanceof InkModifyUndoItem) {
                ((InkModifyUndoItem) undoItem).undo();
            } else if (undoItem instanceof InkDeleteUndoItem) {
                ((InkDeleteUndoItem) undoItem).undo();
            }
        }
        return false;
    }

    public boolean redo() {
        Iterator it = this.undoItems.iterator();
        while (it.hasNext()) {
            InkUndoItem undoItem = (InkUndoItem) it.next();
            if (undoItem instanceof InkModifyUndoItem) {
                ((InkModifyUndoItem) undoItem).redo();
            } else if (undoItem instanceof InkDeleteUndoItem) {
                ((InkDeleteUndoItem) undoItem).redo();
            }
        }
        return false;
    }
}
