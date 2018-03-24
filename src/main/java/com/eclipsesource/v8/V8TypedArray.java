package com.eclipsesource.v8;

import java.nio.ByteBuffer;

public class V8TypedArray extends V8Array {

    private static class V8ArrayData {
        private V8ArrayBuffer buffer;
        private int offset;
        private int size;
        private int type;

        public V8ArrayData(V8ArrayBuffer buffer, int offset, int size, int type) {
            this.buffer = buffer;
            this.offset = offset;
            this.size = size;
            this.type = type;
        }
    }

    public V8TypedArray(V8 v8, V8ArrayBuffer buffer, int type, int offset, int size) {
        super(v8, new V8ArrayData(buffer, offset, size, type));
    }

    private V8TypedArray(V8 v8) {
        super(v8);
    }

    public V8ArrayBuffer getBuffer() {
        return (V8ArrayBuffer) get("buffer");
    }

    public ByteBuffer getByteBuffer() {
        V8ArrayBuffer buffer = getBuffer();
        try {
            ByteBuffer backingStore = buffer.getBackingStore();
            return backingStore;
        } finally {
            buffer.release();
        }
    }

    protected void initialize(long runtimePtr, Object data) {
        this.v8.checkThread();
        if (data == null) {
            super.initialize(runtimePtr, data);
            return;
        }
        V8ArrayData arrayData = (V8ArrayData) data;
        checkArrayProperties(arrayData);
        long handle = createTypedArray(runtimePtr, arrayData);
        this.released = false;
        addObjectReference(handle);
    }

    private long createTypedArray(long runtimePtr, V8ArrayData arrayData) {
        switch (arrayData.type) {
            case 1:
                return this.v8.initNewV8Int32Array(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            case 2:
                return this.v8.initNewV8Float64Array(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            case 9:
                return this.v8.initNewV8Int8Array(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            case 11:
                return this.v8.initNewV8UInt8Array(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            case 12:
                return this.v8.initNewV8UInt8ClampedArray(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            case 13:
                return this.v8.initNewV8Int16Array(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            case 14:
                return this.v8.initNewV8UInt16Array(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            case 15:
                return this.v8.initNewV8UInt32Array(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            case 16:
                return this.v8.initNewV8Float32Array(runtimePtr, arrayData.buffer.objectHandle, arrayData.offset, arrayData.size);
            default:
                throw new IllegalArgumentException("Cannot create a typed array of type " + V8Value.getStringRepresentation(arrayData.type));
        }
    }

    public static int getStructureSize(int type) {
        switch (type) {
            case 1:
            case 15:
            case 16:
                return 4;
            case 2:
                return 8;
            case 9:
            case 11:
            case 12:
                return 1;
            case 13:
            case 14:
                return 2;
            default:
                throw new IllegalArgumentException("Cannot create a typed array of type " + V8Value.getStringRepresentation(type));
        }
    }

    private void checkArrayProperties(V8ArrayData arrayData) {
        checkOffset(arrayData);
        checkSize(arrayData);
    }

    private void checkSize(V8ArrayData arrayData) {
        if (arrayData.size < 0) {
            throw new IllegalStateException("RangeError: Invalid typed array length");
        } else if ((arrayData.size * getStructureSize(arrayData.type)) + arrayData.offset > arrayData.buffer.getBackingStore().limit()) {
            throw new IllegalStateException("RangeError: Invalid typed array length");
        }
    }

    private void checkOffset(V8ArrayData arrayData) {
        if (arrayData.offset % getStructureSize(arrayData.type) != 0) {
            throw new IllegalStateException("RangeError: Start offset of Int32Array must be a multiple of " + getStructureSize(arrayData.type));
        }
    }

    protected V8Value createTwin() {
        return new V8TypedArray(this.v8);
    }
}
