package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

public abstract class FixHeadTableAdapter extends BaseTableAdapter {
    private final Context context;
    private final LayoutInflater inflater;

    public abstract String getCellString(int i, int i2);

    public abstract int getLayoutResource(int i, int i2);

    public FixHeadTableAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public Context getContext() {
        return this.context;
    }

    public LayoutInflater getInflater() {
        return this.inflater;
    }

    public View getView(int row, int column, View converView, ViewGroup parent) {
        if (converView == null) {
            converView = this.inflater.inflate(getLayoutResource(row, column), parent, false);
        }
        setText(converView, getCellString(row, column));
        return converView;
    }

    private void setText(View view, String text) {
        ((TextView) view.findViewById(16908308)).setText(text);
    }
}
