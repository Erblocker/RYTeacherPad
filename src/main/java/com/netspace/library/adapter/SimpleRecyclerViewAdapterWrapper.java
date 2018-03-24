package com.netspace.library.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;

public class SimpleRecyclerViewAdapterWrapper extends Adapter<ViewHolder> {
    private RecyclerAdapterWrapperCallBack mCallBack;
    private Context mContext;
    private int mLayoutResID = 0;

    public interface RecyclerAdapterWrapperCallBack {
        int getCount();

        void getView(int i, ViewHolder viewHolder);
    }

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        private View mRootView;
        private HashMap<Integer, View> mViewCache = new HashMap();

        public ViewHolder(View view) {
            super(view);
            this.mRootView = view;
        }

        public View findViewById(int nID) {
            if (this.mViewCache.containsKey(Integer.valueOf(nID))) {
                return (View) this.mViewCache.get(Integer.valueOf(nID));
            }
            View result = this.mRootView.findViewById(nID);
            if (result != null) {
                this.mViewCache.put(Integer.valueOf(nID), result);
            }
            return result;
        }
    }

    public SimpleRecyclerViewAdapterWrapper(Context context, int nLayoutResID, RecyclerAdapterWrapperCallBack CallBack) {
        this.mContext = context;
        this.mLayoutResID = nLayoutResID;
        this.mCallBack = CallBack;
    }

    public int getItemCount() {
        if (this.mCallBack != null) {
            return this.mCallBack.getCount();
        }
        return 0;
    }

    public void onBindViewHolder(ViewHolder arg0, int arg1) {
        if (this.mCallBack != null) {
            this.mCallBack.getView(arg1, arg0);
        }
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(this.mLayoutResID, viewGroup, false));
    }
}
