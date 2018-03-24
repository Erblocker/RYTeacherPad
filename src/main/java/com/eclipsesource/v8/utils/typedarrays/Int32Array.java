package com.eclipsesource.v8.utils.typedarrays;

import java.nio.ByteBuffer;

public class Int32Array extends TypedArray {
    public Int32Array(ByteBuffer buffer) {
        super(buffer);
    }

    public Int32Array(ArrayBuffer arrayBuffer) {
        this(arrayBuffer.getByteBuffer());
    }

    public int get(int index) {
        return this.buffer.asIntBuffer().get(index);
    }

    public int length() {
        return this.buffer.asIntBuffer().limit();
    }

    public void put(int index, int value) {
        this.buffer.asIntBuffer().put(index, value);
    }

    public int getType() {
        return 1;
    }
}
