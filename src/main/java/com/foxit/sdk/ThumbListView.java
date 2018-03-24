package com.foxit.sdk;

import android.content.Context;
import android.widget.ListView;

public class ThumbListView extends ListView {
    protected IPageClickListener mClickListener;

    public interface IPageClickListener {
        void onClick(int i);
    }

    protected ThumbListView(Context context) {
        super(context);
    }

    public void setPageClickedListener(IPageClickListener iPageClickListener) {
        this.mClickListener = iPageClickListener;
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        t tVar = (t) getAdapter();
        tVar.a(i, i2);
        tVar.notifyDataSetChanged();
    }
}
