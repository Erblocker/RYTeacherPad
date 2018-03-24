package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SimpleListAdapterWrapper extends BaseAdapter {
    private ListAdapterWrapperCallBack mCallBack;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mnViewLayoutResID;

    public interface ListAdapterWrapperCallBack {
        int getCount();

        Object getItem(int i);

        void getView(int i, View view);
    }

    public SimpleListAdapterWrapper(Context context, ListAdapterWrapperCallBack CallBack, int nViewResID) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mCallBack = CallBack;
        this.mnViewLayoutResID = nViewResID;
        if (this.mCallBack == null) {
            throw new NullPointerException("CallBack can't be null.");
        }
    }

    public int getCount() {
        return this.mCallBack.getCount();
    }

    public Object getItem(int position) {
        return this.mCallBack.getItem(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(this.mnViewLayoutResID, null);
        }
        this.mCallBack.getView(position, convertView);
        return convertView;
    }
}
