package com.foxit.uiextensions.controls.panel;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class PanelContentViewAdapter extends PagerAdapter {
    private List<View> mViewPagerList;

    public PanelContentViewAdapter(List<View> viewPagerList) {
        this.mViewPagerList = viewPagerList;
    }

    public int getCount() {
        return this.mViewPagerList.size();
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        container.addView((View) this.mViewPagerList.get(position));
        return this.mViewPagerList.get(position);
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) this.mViewPagerList.get(position));
    }
}
