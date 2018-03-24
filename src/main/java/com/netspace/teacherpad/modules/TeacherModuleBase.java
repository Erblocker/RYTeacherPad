package com.netspace.teacherpad.modules;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.lang.ref.WeakReference;

public class TeacherModuleBase {
    protected WeakReference<Activity> mActivity;
    protected String mCategoryName;
    protected int mIconID;
    protected LayoutInflater mLayoutInflater;
    protected String mMenuName;
    protected String mModuleName;
    protected ViewGroup mRootLayout;
    protected boolean mbPutInScrollView = true;

    public TeacherModuleBase(Activity Activity, ViewGroup RootLayout) {
        this.mActivity = new WeakReference(Activity);
        this.mRootLayout = RootLayout;
        this.mLayoutInflater = LayoutInflater.from(Activity);
    }

    public String getModuleName() {
        return this.mModuleName;
    }

    public int getModuleIcon() {
        return this.mIconID;
    }

    public String getCategoryName() {
        return this.mCategoryName;
    }

    public boolean getPutinScrollView() {
        return this.mbPutInScrollView;
    }

    public void startModule() {
    }

    public void stopModule() {
    }

    public void refreshModule() {
    }
}
