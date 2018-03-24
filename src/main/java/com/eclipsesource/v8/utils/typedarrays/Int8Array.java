package com.eclipsesource.v8.utils.typedarrays;

import java.nio.ByteBuffer;

public class Int8Array extends TypedArray {
    public Int8Array(ByteBuffer buffer) {
        super(buffer);
    }

    public Int8Array(ArrayBuffer arrayBuffer) {
        this(arrayBuffer.getByteBuffer());
    }

    public byte get(int index) {
        return this.buffer.get(index);
    }

    public int length() {
        return this.buffer.limit();
    }

    public void put(int index, byte value) {
        this.buffer.put(index, value);
    }

    public int getType() {
        return 9;
    }
}
