package com.netspace.library.dialog;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.netspace.library.adapter.FragmentsPageAdapter;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.fragment.MyiLibraryFragment;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class SelectFolderDialog extends DialogFragment implements OnTabSelectedListener {
    private FragmentsPageAdapter mAdapter;
    private SelectFolderDialogCallBack mCallBack;
    private MyiLibraryFragment mMyiLibraryFragment;
    private View mRootView;
    private String mTargetClassKPGUID;
    private CustomViewPager mViewPager;

    public interface SelectFolderDialogCallBack {
        void onSelected(String str, String str2);
    }

    public void setTargetClassKPGUID(String szClassKPGUID) {
        this.mTargetClassKPGUID = szClassKPGUID;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_common, container, false);
        Toolbar toolbar = (Toolbar) this.mRootView.findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "选择目录");
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                Utilities.logMenuClick(arg0, "ShareToDialog");
                if (arg0.getItemId() != R.id.action_yes) {
                    return false;
                }
                if (SelectFolderDialog.this.mCallBack != null) {
                    SelectFolderDialog.this.mCallBack.onSelected(SelectFolderDialog.this.mMyiLibraryFragment.getCurrentKPGUID(), SelectFolderDialog.this.mMyiLibraryFragment.getCurrentKPPath());
                }
                SelectFolderDialog.this.dismiss();
                return true;
            }
        });
        toolbar.inflateMenu(R.menu.menu_okonly);
        this.mAdapter = new FragmentsPageAdapter(getChildFragmentManager());
        this.mMyiLibraryFragment = new MyiLibraryFragment();
        this.mMyiLibraryFragment.setShareFolderOnly(true, this.mTargetClassKPGUID);
        this.mAdapter.addPage(this.mMyiLibraryFragment, "班级分享区目录");
        this.mViewPager = (CustomViewPager) this.mRootView.findViewById(R.id.pager);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOffscreenPageLimit(4);
        TabLayout tabLayout = (TabLayout) this.mRootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(this.mViewPager);
        tabLayout.setOnTabSelectedListener(this);
        setHasOptionsMenu(true);
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(0).toString());
        return this.mRootView;
    }

    public void setCallBack(SelectFolderDialogCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void onTabReselected(Tab arg0) {
    }

    public void onTabSelected(Tab arg0) {
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(arg0.getPosition()).toString());
        this.mViewPager.setCurrentItem(arg0.getPosition());
    }

    public void onTabUnselected(Tab arg0) {
    }
}
