package com.eclipsesource.v8.utils.typedarrays;

import java.nio.ByteBuffer;

public class Float32Array extends TypedArray {
    public Float32Array(ByteBuffer buffer) {
        super(buffer);
    }

    public Float32Array(ArrayBuffer arrayBuffer) {
        this(arrayBuffer.getByteBuffer());
    }

    public float get(int index) {
        return this.buffer.asFloatBuffer().get(index);
    }

    public int length() {
        return this.buffer.asFloatBuffer().limit();
    }

    public void put(int index, float value) {
        this.buffer.asFloatBuffer().put(index, value);
    }

    public int getType() {
        return 16;
    }
}
