package com.netspace.library.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

public class InlineViewPageAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<View> mPages = new ArrayList();
    private ArrayList<String> mPagesTitle = new ArrayList();

    public InlineViewPageAdapter(Context context) {
        this.mContext = context;
    }

    public void addPage(View view, String szTitle) {
        this.mPages.add(view);
        this.mPagesTitle.add(szTitle);
    }

    public Object instantiateItem(ViewGroup collection, int position) {
        return (View) this.mPages.get(position);
    }

    public void destroyItem(ViewGroup collection, int position, Object view) {
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
