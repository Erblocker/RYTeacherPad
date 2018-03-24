package com.netspace.library.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

public class ViewPageAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<View> mPages = new ArrayList();
    private ArrayList<String> mPagesTitle = new ArrayList();

    public ViewPageAdapter(Context context) {
        this.mContext = context;
    }

    public void addPage(View view, String szTitle) {
        this.mPages.add(view);
        this.mPagesTitle.add(szTitle);
    }

    public Object instantiateItem(ViewGroup collection, int position) {
        View view = (View) this.mPages.get(position);
        collection.addView(view);
        return view;
    }

    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    public int getCount() {
        return this.mPages.size();
    }

    public CharSequence getPageTitle(int position) {
        if (position < 0 || position >= this.mPages.size()) {
            return null;
        }
        return (CharSequence) this.mPagesTitle.get(position);
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
}
