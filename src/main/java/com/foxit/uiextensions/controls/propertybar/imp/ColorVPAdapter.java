package com.foxit.uiextensions.controls.propertybar.imp;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

class ColorVPAdapter extends PagerAdapter {
    private List<View> mColorViewList;

    public ColorVPAdapter(List<View> viewList) {
        this.mColorViewList = viewList;
    }

    public int getCount() {
        return this.mColorViewList.size();
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        container.addView((View) this.mColorViewList.get(position));
        return this.mColorViewList.get(position);
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) this.mColorViewList.get(position));
    }

    public CharSequence getPageTitle(int position) {
        return position;
    }
}
