package com.eclipsesource.v8.utils;

import com.eclipsesource.v8.ReferenceHandler;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Value;
import java.util.ArrayList;
import java.util.Iterator;

public class MemoryManager {
    private MemoryManagerReferenceHandler memoryManagerReferenceHandler;
    private ArrayList<V8Value> references = new ArrayList();
    private boolean released = false;
    private boolean releasing = false;
    private V8 v8;

    private class MemoryManagerReferenceHandler implements ReferenceHandler {
        private MemoryManagerReferenceHandler() {
        }

        public void v8HandleCreated(V8Value object) {
            MemoryManager.this.references.add(object);
        }

        public void v8HandleDisposed(V8Value object) {
            if (!MemoryManager.this.releasing) {
                Iterator<V8Value> iterator = MemoryManager.this.references.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next() == object) {
                        iterator.remove();
                        return;
                    }
                }
            }
        }
    }

    public MemoryManager(V8 v8) {
        this.v8 = v8;
        this.memoryManagerReferenceHandler = new MemoryManagerReferenceHandler();
        v8.addReferenceHandler(this.memoryManagerReferenceHandler);
    }

    public int getObjectReferenceCount() {
        checkReleased();
        return this.references.size();
    }

    public void persist(V8Value object) {
        this.v8.getLocker().checkThread();
        checkReleased();
        this.references.remove(object);
    }

    public boolean isReleased() {
        return this.released;
    }

    public void release() {
        this.v8.getLocker().checkThread();
        if (!this.released) {
            this.releasing = true;
            try {
                Iterator it = this.references.iterator();
                while (it.hasNext()) {
                    ((V8Value) it.next()).release();
                }
                this.v8.removeReferenceHandler(this.memoryManagerReferenceHandler);
                this.references.clear();
                this.released = true;
            } finally {
                this.releasing = false;
            }
        }
    }

    private void checkReleased() {
        if (this.released) {
            throw new IllegalStateException("Memory manager released");
        }
    }
}
