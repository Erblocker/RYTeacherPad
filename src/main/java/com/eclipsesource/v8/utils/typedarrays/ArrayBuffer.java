package com.eclipsesource.v8.utils.typedarrays;

import java.nio.ByteBuffer;

public class ArrayBuffer {
    private ByteBuffer byteBuffer;

    public ArrayBuffer(int capacity) {
        this.byteBuffer = ByteBuffer.allocateDirect(capacity);
    }

    public ArrayBuffer(byte[] src) {
        this.byteBuffer = ByteBuffer.allocateDirect(src.length);
        this.byteBuffer.put(src, 0, src.length);
    }

    public ArrayBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = validateByteBuffer(byteBuffer);
    }

    private ByteBuffer validateByteBuffer(ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect()) {
            return byteBuffer;
        }
        throw new IllegalArgumentException("ByteBuffer must be a allocated as a direct ByteBuffer");
    }

    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    public byte getByte(int index) {
        return this.byteBuffer.get(index);
    }

    public short getUnsignedByte(int index) {
        return (short) (this.byteBuffer.get(index) & 255);
    }

    public void put(int index, byte value) {
        this.byteBuffer.put(index, value);
    }

    public int limit() {
        return this.byteBuffer.limit();
    }

    public String toString() {
        return "[object ArrayBuffer]";
    }
}
