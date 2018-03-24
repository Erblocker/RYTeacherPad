package com.netspace.library.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.netspace.library.interfaces.FragmentViewPagerCallBack;
import java.util.ArrayList;

public class FragmentsPageAdapter extends FragmentPagerAdapter {
    private static final String TAG = "FragmentsPageAdapter";
    private ArrayList<Fragment> mPages = new ArrayList();
    private ArrayList<FragmentViewPagerCallBack> mPagesCallBack = new ArrayList();
    private ArrayList<String> mPagesTitle = new ArrayList();
    private boolean mbPreventDestroyItem = true;

    public FragmentsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addPage(Fragment fragment, String szTitle) {
        this.mPages.add(fragment);
        this.mPagesTitle.add(szTitle);
        this.mPagesCallBack.add(null);
    }

    public void addPage(String szTitle, FragmentViewPagerCallBack newInstanceCallBack) {
        this.mPages.add(null);
        this.mPagesTitle.add(szTitle);
        this.mPagesCallBack.add(newInstanceCallBack);
    }

    public boolean setPageTitle(int nIndex, String szTitle) {
        if (((String) this.mPagesTitle.get(nIndex)).equalsIgnoreCase(szTitle)) {
            return false;
        }
        this.mPagesTitle.set(nIndex, szTitle);
        notifyDataSetChanged();
        return true;
    }

    public void setPreventDestroyItem(boolean bPrevent) {
        this.mbPreventDestroyItem = bPrevent;
    }

    public void clear() {
        this.mPages.clear();
        this.mPagesTitle.clear();
        notifyDataSetChanged();
    }

    public Fragment getItem(int position) {
        Log.d(TAG, "getItem(" + position + ")");
        if (position < 0 || position >= this.mPages.size()) {
            return null;
        }
        if (this.mPages.get(position) != null) {
            return (Fragment) this.mPages.get(position);
        }
        if (this.mPagesCallBack.get(position) != null) {
            return ((FragmentViewPagerCallBack) this.mPagesCallBack.get(position)).onNewInstance();
        }
        return null;
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

    public void destroyItem(ViewGroup container, int position, Object object) {
        if (this.mbPreventDestroyItem) {
            Log.i(TAG, "prevent real destroyItem ");
        } else {
            super.destroyItem(container, position, object);
        }
    }

    public void destroyItem(View container, int position, Object object) {
        if (this.mbPreventDestroyItem) {
            Log.i(TAG, "prevent real destroyItem");
        } else {
            super.destroyItem(container, position, object);
        }
    }
}
