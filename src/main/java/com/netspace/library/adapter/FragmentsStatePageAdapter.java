package com.netspace.library.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.netspace.library.interfaces.FragmentViewPagerCallBack;
import java.util.ArrayList;

public class FragmentsStatePageAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "FragmentsStatePageAdapter";
    private ArrayList<FragmentViewPagerCallBack> mPagesCallBack = new ArrayList();
    private ArrayList<String> mPagesTitle = new ArrayList();

    public FragmentsStatePageAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addPage(String szTitle, FragmentViewPagerCallBack newInstanceCallBack) {
        this.mPagesCallBack.add(newInstanceCallBack);
        this.mPagesTitle.add(szTitle);
    }

    public boolean setPageTitle(int nIndex, String szTitle) {
        if (((String) this.mPagesTitle.get(nIndex)).equalsIgnoreCase(szTitle)) {
            return false;
        }
        this.mPagesTitle.set(nIndex, szTitle);
        notifyDataSetChanged();
        return true;
    }

    public void clear() {
        this.mPagesCallBack.clear();
        this.mPagesTitle.clear();
        notifyDataSetChanged();
    }

    public Fragment getItem(int position) {
        Log.d(TAG, "getItem(" + position + ")");
        if (position < 0 || position >= this.mPagesCallBack.size()) {
            return null;
        }
        return ((FragmentViewPagerCallBack) this.mPagesCallBack.get(position)).onNewInstance();
    }

    public int getCount() {
        return this.mPagesCallBack.size();
    }

    public CharSequence getPageTitle(int position) {
        if (position < 0 || position >= this.mPagesTitle.size()) {
            return null;
        }
        return (CharSequence) this.mPagesTitle.get(position);
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    public void destroyItem(View container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
