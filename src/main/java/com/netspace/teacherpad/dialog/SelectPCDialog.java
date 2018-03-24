package com.netspace.teacherpad.dialog;

import android.content.DialogInterface;
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
import android.widget.ImageView;
import com.netspace.library.adapter.FragmentsPageAdapter;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.fragments.SelectPCFragment;
import com.netspace.teacherpad.fragments.SelectPCFragment.PCData;

public class SelectPCDialog extends DialogFragment implements OnTabSelectedListener {
    private FragmentsPageAdapter mAdapter;
    private SelectPCDialogCallBack mCallBack;
    private SelectPCDialogDismissCallBack mDismissCallBack;
    private View mRootView;
    private SelectPCFragment mSelectPCFragment;
    private CustomViewPager mViewPager;
    private String mszScheduleGUID;

    public interface SelectPCDialogCallBack {
        void onSelectPC(String str, String str2, String str3);
    }

    public interface SelectPCDialogDismissCallBack {
        void onDismiss();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_common, container, false);
        Toolbar toolbar = (Toolbar) this.mRootView.findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "选择睿易通");
        ((ImageView) this.mRootView.findViewById(R.id.imageViewLogo)).setImageResource(R.drawable.ic_computer);
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                Utilities.logMenuClick(arg0, "GiveHonourDialog");
                if (arg0.getItemId() == R.id.action_yes) {
                    PCData pcdata = SelectPCDialog.this.mSelectPCFragment.getSelectedItem();
                    if (pcdata == null) {
                        Utilities.showAlertMessage(SelectPCDialog.this.getActivity(), "请选择一个睿易通", "请选择一个睿易通然后才可以开始上课。");
                    } else if (SelectPCDialog.this.mCallBack != null) {
                        SelectPCDialog.this.mCallBack.onSelectPC(pcdata.szSessionID, pcdata.szIP, pcdata.szStatus);
                        SelectPCDialog.this.dismiss();
                    }
                } else if (arg0.getItemId() == R.id.action_no) {
                    SelectPCDialog.this.dismiss();
                }
                return true;
            }
        });
        toolbar.inflateMenu(R.menu.menu_okcancel);
        this.mAdapter = new FragmentsPageAdapter(getChildFragmentManager());
        this.mSelectPCFragment = new SelectPCFragment();
        this.mSelectPCFragment.setScheduleGUID(this.mszScheduleGUID);
        this.mAdapter.addPage(this.mSelectPCFragment, "正在运行的睿易通");
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

    public void setCallBack(SelectPCDialogCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void setDismissCallBack(SelectPCDialogDismissCallBack CallBack) {
        this.mDismissCallBack = CallBack;
    }

    public void setScheduleGUID(String szScheduleGUID) {
        this.mszScheduleGUID = szScheduleGUID;
    }

    public void onTabReselected(Tab arg0) {
    }

    public void onTabSelected(Tab arg0) {
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(arg0.getPosition()).toString());
        this.mViewPager.setCurrentItem(arg0.getPosition());
    }

    public void onTabUnselected(Tab arg0) {
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mDismissCallBack != null) {
            this.mDismissCallBack.onDismiss();
        }
        super.onDismiss(dialog);
    }

    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(Utilities.dpToPixel(600, getContext()), -1);
        }
    }
}
