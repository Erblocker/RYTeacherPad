package com.eclipsesource.v8.debug.mirror;

import com.eclipsesource.v8.V8Object;
import com.netspace.library.fragment.RESTLibraryFragment;

public class PropertyMirror extends Mirror {
    PropertyMirror(V8Object v8Object) {
        super(v8Object);
    }

    public String getName() {
        return this.v8Object.executeStringFunction(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX, null);
    }

    public Mirror getValue() {
        V8Object mirror = this.v8Object.executeObjectFunction("value", null);
        try {
            Mirror createMirror = Mirror.createMirror(mirror);
            return createMirror;
        } finally {
            mirror.release();
        }
    }

    public boolean isProperty() {
        return true;
    }
}
