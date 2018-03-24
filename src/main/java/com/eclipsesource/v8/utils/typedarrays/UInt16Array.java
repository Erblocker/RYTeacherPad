package com.eclipsesource.v8.utils.typedarrays;

import android.support.v4.internal.view.SupportMenu;
import java.nio.ByteBuffer;

public class UInt16Array extends TypedArray {
    public UInt16Array(ByteBuffer buffer) {
        super(buffer);
    }

    public UInt16Array(ArrayBuffer arrayBuffer) {
        this(arrayBuffer.getByteBuffer());
    }

    public int get(int index) {
        return SupportMenu.USER_MASK & this.buffer.asShortBuffer().get(index);
    }

    public int length() {
        return this.buffer.asShortBuffer().limit();
    }

    public void put(int index, int value) {
        this.buffer.asShortBuffer().put(index, (short) (SupportMenu.USER_MASK & value));
    }

    public int getType() {
        return 14;
    }
}
