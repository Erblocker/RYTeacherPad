package com.eclipsesource.v8.utils.typedarrays;

import java.nio.ByteBuffer;

public class Int16Array extends TypedArray {
    public Int16Array(ByteBuffer buffer) {
        super(buffer);
    }

    public Int16Array(ArrayBuffer arrayBuffer) {
        this(arrayBuffer.getByteBuffer());
    }

    public short get(int index) {
        return this.buffer.asShortBuffer().get(index);
    }

    public int length() {
        return this.buffer.asShortBuffer().limit();
    }

    public void put(int index, short value) {
        this.buffer.asShortBuffer().put(index, value);
    }

    public int getType() {
        return 13;
    }
}
