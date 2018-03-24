package com.eclipsesource.v8.utils;

public abstract class SingleTypeAdapter implements TypeAdapter {
    private int typeToAdapt;

    public abstract Object adapt(Object obj);

    public SingleTypeAdapter(int typeToAdapt) {
        this.typeToAdapt = typeToAdapt;
    }

    public Object adapt(int type, Object value) {
        if (type == this.typeToAdapt) {
            return adapt(value);
        }
        return TypeAdapter.DEFAULT;
    }
}
