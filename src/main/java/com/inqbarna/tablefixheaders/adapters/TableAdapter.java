package com.inqbarna.tablefixheaders.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

public interface TableAdapter {
    public static final int IGNORE_ITEM_VIEW_TYPE = -1;

    int getColumnCount();

    int getHeight(int i);

    int getItemViewType(int i, int i2);

    int getRowCount();

    View getView(int i, int i2, View view, ViewGroup viewGroup);

    int getViewTypeCount();

    int getWidth(int i);

    void onCellClick(int i, int i2);

    void registerDataSetObserver(DataSetObserver dataSetObserver);

    void unregisterDataSetObserver(DataSetObserver dataSetObserver);
}
