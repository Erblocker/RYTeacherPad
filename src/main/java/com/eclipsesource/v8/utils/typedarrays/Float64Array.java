package com.eclipsesource.v8.utils.typedarrays;

import java.nio.ByteBuffer;

public class Float64Array extends TypedArray {
    public Float64Array(ByteBuffer buffer) {
        super(buffer);
    }

    public Float64Array(ArrayBuffer arrayBuffer) {
        this(arrayBuffer.getByteBuffer());
    }

    public double get(int index) {
        return this.buffer.asDoubleBuffer().get(index);
    }

    public int length() {
        return this.buffer.asDoubleBuffer().limit();
    }

    public void put(int index, double value) {
        this.buffer.asDoubleBuffer().put(index, value);
    }

    public int getType() {
        return 2;
    }
}
