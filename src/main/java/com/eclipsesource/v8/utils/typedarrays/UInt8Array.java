package com.eclipsesource.v8.utils.typedarrays;

import java.nio.ByteBuffer;

public class UInt8Array extends TypedArray {
    public UInt8Array(ByteBuffer buffer) {
        super(buffer);
    }

    public UInt8Array(ArrayBuffer arrayBuffer) {
        this(arrayBuffer.getByteBuffer());
    }

    public short get(int index) {
        return (short) (this.buffer.get(index) & 255);
    }

    public int length() {
        return this.buffer.limit();
    }

    public void put(int index, short value) {
        this.buffer.put(index, (byte) (value & 255));
    }

    public int getType() {
        return 11;
    }
}
