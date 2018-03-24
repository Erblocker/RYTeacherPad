package com.eclipsesource.v8;

public class V8Function extends V8Object {
    public V8Function(V8 v8, JavaCallback javaCallback) {
        super(v8, javaCallback);
    }

    protected V8Function(V8 v8) {
        this(v8, null);
    }

    protected V8Value createTwin() {
        return new V8Function(this.v8);
    }

    public String toString() {
        if (this.released || this.v8.isReleased()) {
            return "[Function released]";
        }
        return super.toString();
    }

    protected void initialize(long runtimePtr, Object data) {
        if (data == null) {
            super.initialize(runtimePtr, null);
            return;
        }
        JavaCallback javaCallback = (JavaCallback) data;
        long[] pointers = this.v8.initNewV8Function(runtimePtr);
        this.v8.createAndRegisterMethodDescriptor(javaCallback, pointers[1]);
        this.released = false;
        addObjectReference(pointers[0]);
    }

    public V8Function twin() {
        return (V8Function) super.twin();
    }

    public Object call(V8Object receiver, V8Array parameters) {
        this.v8.checkThread();
        checkReleased();
        this.v8.checkRuntime(receiver);
        this.v8.checkRuntime(parameters);
        if (receiver == null) {
            receiver = this.v8;
        }
        return this.v8.executeFunction(this.v8.getV8RuntimePtr(), receiver.isUndefined() ? this.v8.getHandle() : receiver.getHandle(), this.objectHandle, parameters == null ? 0 : parameters.getHandle());
    }
}
