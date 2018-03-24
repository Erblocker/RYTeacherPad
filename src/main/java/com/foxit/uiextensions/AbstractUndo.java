package com.foxit.uiextensions;

import com.foxit.uiextensions.annots.AnnotUndoItem;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Iterator;
import javazoom.jl.decoder.BitstreamErrors;

public abstract class AbstractUndo {
    public static final String HISTORY_CACHE_FILE = "history.dat";
    public static final int HISTORY_COUNT = 512;
    protected ArrayList<IUndoItem> mRedoItemStack = new ArrayList();
    ArrayList<IUndoEventListener> mUndoEventListeners = new ArrayList();
    protected ArrayList<IUndoItem> mUndoItemStack = new ArrayList();

    public interface IUndoEventListener {
        void clearUndoFinished(DocumentManager documentManager);

        void itemAdded(DocumentManager documentManager, IUndoItem iUndoItem);

        void itemWillAdd(DocumentManager documentManager, IUndoItem iUndoItem);

        void redoFinished(DocumentManager documentManager, IUndoItem iUndoItem);

        void undoFinished(DocumentManager documentManager, IUndoItem iUndoItem);

        void willClearUndo(DocumentManager documentManager);

        void willRedo(DocumentManager documentManager, IUndoItem iUndoItem);

        void willUndo(DocumentManager documentManager, IUndoItem iUndoItem);
    }

    protected abstract String getDiskCacheFolder();

    protected abstract boolean haveModifyTasks();

    public void onPageRemoved(boolean success, int index) {
        if (success) {
            removeInvalidItems(this.mRedoItemStack, index);
            removeInvalidItems(this.mUndoItemStack, index);
        }
    }

    public void onPagesInsert(boolean success, int dstIndex, int[] range) {
        if (success) {
            int offsetIndex = 0;
            for (int i = 0; i < range.length / 2; i++) {
                offsetIndex += range[(i * 2) + 1];
            }
            updateItemsWithOffset(this.mRedoItemStack, dstIndex, offsetIndex);
            updateItemsWithOffset(this.mUndoItemStack, dstIndex, offsetIndex);
        }
    }

    private void removeInvalidItems(ArrayList<IUndoItem> list, int index) {
        ArrayList<IUndoItem> invalidList = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            IUndoItem undoItem = (IUndoItem) it.next();
            if (undoItem instanceof AnnotUndoItem) {
                AnnotUndoItem item = (AnnotUndoItem) undoItem;
                if (item.mPageIndex == index) {
                    invalidList.add(item);
                } else if (item.mPageIndex > index) {
                    item.mPageIndex--;
                }
            }
        }
        it = invalidList.iterator();
        while (it.hasNext()) {
            list.remove((IUndoItem) it.next());
        }
        invalidList.clear();
    }

    public void onPageMoved(boolean success, int index, int dstIndex) {
        if (success) {
            updateItems(this.mRedoItemStack, index, dstIndex);
            updateItems(this.mUndoItemStack, index, dstIndex);
        }
    }

    private void updateItemsWithOffset(ArrayList<IUndoItem> list, int index, int offset) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            IUndoItem undoItem = (IUndoItem) it.next();
            if ((undoItem instanceof AnnotUndoItem) && ((AnnotUndoItem) undoItem).mPageIndex >= index) {
                AnnotUndoItem annotUndoItem = (AnnotUndoItem) undoItem;
                annotUndoItem.mPageIndex += offset;
            }
        }
    }

    private void updateItems(ArrayList<IUndoItem> list, int index, int dstIndex) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            IUndoItem undoItem = (IUndoItem) it.next();
            if (undoItem instanceof AnnotUndoItem) {
                AnnotUndoItem annotUndoItem;
                if (index < dstIndex) {
                    if (((AnnotUndoItem) undoItem).mPageIndex <= dstIndex && ((AnnotUndoItem) undoItem).mPageIndex > index) {
                        annotUndoItem = (AnnotUndoItem) undoItem;
                        annotUndoItem.mPageIndex--;
                    } else if (((AnnotUndoItem) undoItem).mPageIndex == index) {
                        ((AnnotUndoItem) undoItem).mPageIndex = dstIndex;
                    }
                } else if (((AnnotUndoItem) undoItem).mPageIndex >= dstIndex && ((AnnotUndoItem) undoItem).mPageIndex < index) {
                    annotUndoItem = (AnnotUndoItem) undoItem;
                    annotUndoItem.mPageIndex++;
                } else if (((AnnotUndoItem) undoItem).mPageIndex == index) {
                    ((AnnotUndoItem) undoItem).mPageIndex = dstIndex;
                }
            }
        }
    }

    public void addUndoItem(IUndoItem undoItem) {
        undoItemWillAdd(undoItem);
        this.mUndoItemStack.add(undoItem);
        this.mRedoItemStack.clear();
        writeHistoryCache();
        undoItemAdded(undoItem);
    }

    public boolean canUndo() {
        return this.mUndoItemStack.size() > 0;
    }

    public boolean canRedo() {
        return this.mRedoItemStack.size() > 0;
    }

    public void undo() {
        if (this.mUndoItemStack.size() != 0 && !haveModifyTasks()) {
            IUndoItem item = (IUndoItem) this.mUndoItemStack.get(this.mUndoItemStack.size() - 1);
            willUndo(item);
            item.undo();
            this.mUndoItemStack.remove(item);
            this.mRedoItemStack.add(item);
            undoFinished(item);
        }
    }

    public void redo() {
        if (this.mRedoItemStack.size() != 0 && !haveModifyTasks()) {
            IUndoItem item = (IUndoItem) this.mRedoItemStack.get(this.mRedoItemStack.size() - 1);
            willRedo(item);
            item.redo();
            this.mRedoItemStack.remove(item);
            this.mUndoItemStack.add(item);
            redoFinished(item);
        }
    }

    public void clearUndoRedo() {
        willClearUndo();
        this.mUndoItemStack.clear();
        this.mRedoItemStack.clear();
        deleteHistoryCacheFile();
        clearUndoFinished();
    }

    private void readHistoryCache(File file, ArrayList<IUndoItem> historyStack) {
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                while (true) {
                    try {
                        IUndoItem item = (IUndoItem) ois.readObject();
                        if (item == null) {
                            break;
                        }
                        historyStack.add(item);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (EOFException e2) {
                        e2.printStackTrace();
                    }
                }
                ois.close();
            } catch (StreamCorruptedException e3) {
                e3.printStackTrace();
            } catch (FileNotFoundException e4) {
                e4.printStackTrace();
            } catch (IOException e5) {
                e5.printStackTrace();
            }
        }
    }

    protected boolean writeHistoryCache() {
        if (this.mUndoItemStack.size() >= 1024) {
            int i;
            ArrayList<IUndoItem> historyStack = new ArrayList();
            File file = new File(getHistoryCachePath());
            readHistoryCache(file, historyStack);
            if (file.exists()) {
                file.delete();
            }
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file, true));
                int count = historyStack.size();
                for (i = 0; i < count; i++) {
                    oos.writeObject((IUndoItem) historyStack.get(i));
                }
                for (i = 0; i < 512; i++) {
                    oos.writeObject((IUndoItem) this.mUndoItemStack.get(i));
                }
                oos.writeObject(null);
                oos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            for (i = BitstreamErrors.BITSTREAM_LAST; i >= 0; i--) {
                this.mUndoItemStack.remove(i);
            }
        }
        return true;
    }

    protected void onDocumentOpened() {
        deleteHistoryCacheFile();
    }

    protected void onDocumentClosed() {
        deleteHistoryCacheFile();
    }

    protected void deleteHistoryCacheFile() {
        new File(getHistoryCachePath()).delete();
    }

    protected String getHistoryCachePath() {
        return getDiskCacheFolder().concat("/history.dat");
    }

    public void registerUndoEventListener(IUndoEventListener listener) {
        this.mUndoEventListeners.add(listener);
    }

    public void unregisterUndoEventListener(IUndoEventListener listener) {
        this.mUndoEventListeners.remove(listener);
    }

    protected void undoItemWillAdd(IUndoItem item) {
        Iterator it = this.mUndoEventListeners.iterator();
        while (it.hasNext()) {
            ((IUndoEventListener) it.next()).itemWillAdd((DocumentManager) this, item);
        }
    }

    protected void undoItemAdded(IUndoItem item) {
        Iterator it = this.mUndoEventListeners.iterator();
        while (it.hasNext()) {
            ((IUndoEventListener) it.next()).itemAdded((DocumentManager) this, item);
        }
    }

    protected void willUndo(IUndoItem item) {
        Iterator it = this.mUndoEventListeners.iterator();
        while (it.hasNext()) {
            ((IUndoEventListener) it.next()).willUndo((DocumentManager) this, item);
        }
    }

    protected void undoFinished(IUndoItem item) {
        Iterator it = this.mUndoEventListeners.iterator();
        while (it.hasNext()) {
            ((IUndoEventListener) it.next()).undoFinished((DocumentManager) this, item);
        }
    }

    protected void willRedo(IUndoItem item) {
        Iterator it = this.mUndoEventListeners.iterator();
        while (it.hasNext()) {
            ((IUndoEventListener) it.next()).willRedo((DocumentManager) this, item);
        }
    }

    protected void redoFinished(IUndoItem item) {
        Iterator it = this.mUndoEventListeners.iterator();
        while (it.hasNext()) {
            ((IUndoEventListener) it.next()).redoFinished((DocumentManager) this, item);
        }
    }

    protected void willClearUndo() {
        Iterator it = this.mUndoEventListeners.iterator();
        while (it.hasNext()) {
            ((IUndoEventListener) it.next()).willClearUndo((DocumentManager) this);
        }
    }

    protected void clearUndoFinished() {
        Iterator it = this.mUndoEventListeners.iterator();
        while (it.hasNext()) {
            ((IUndoEventListener) it.next()).clearUndoFinished((DocumentManager) this);
        }
    }
}
