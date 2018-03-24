package com.eclipsesource.v8.utils.typedarrays;

import java.nio.ByteBuffer;

public class UInt8ClampedArray extends TypedArray {
    public UInt8ClampedArray(ByteBuffer buffer) {
        super(buffer);
    }

    public UInt8ClampedArray(ArrayBuffer arrayBuffer) {
        this(arrayBuffer.getByteBuffer());
    }

    public short get(int index) {
        return (short) (this.buffer.get(index) & 255);
    }

    public int length() {
        return this.buffer.limit();
    }

    public void put(int index, short value) {
        if (value > (short) 255) {
            this.buffer.put(index, (byte) -1);
        } else if (value < (short) 0) {
            this.buffer.put(index, (byte) 0);
        } else {
            this.buffer.put(index, (byte) value);
        }
    }

    public int getType() {
        return 12;
    }
}
