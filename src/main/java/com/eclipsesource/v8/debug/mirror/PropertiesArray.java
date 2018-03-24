package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

public class PropertiesArray implements Releasable {
    private V8Array v8Array;

    PropertiesArray(V8Array v8Object) {
        this.v8Array = v8Object.twin();
    }

    public PropertyMirror getProperty(int index) {
        V8Object result = this.v8Array.getObject(index);
        try {
            PropertyMirror propertyMirror = new PropertyMirror(result);
            return propertyMirror;
        } finally {
            result.release();
        }
    }

    public void release() {
        if (!this.v8Array.isReleased()) {
            this.v8Array.release();
        }
    }

    public int length() {
        return this.v8Array.length();
    }
}
