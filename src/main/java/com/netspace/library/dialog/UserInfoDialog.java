package com.netspace.library.dialog;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.library.adapter.FragmentsPageAdapter;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.fragment.BasicUserInfoFragment;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.fragment.UserRightsFragment;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;

public class UserInfoDialog extends DialogFragment implements OnTabSelectedListener {
    private FragmentsPageAdapter mAdapter;
    private BasicUserInfoFragment mBasicInfoFragment;
    private LinearLayout mLayoutUserInfo;
    private View mRootView;
    private String mUserGUID;
    private UserHonourFragment mUserHonourFragment;
    private String mUserName;
    private UserRightsFragment mUserRightsFragment;
    private CustomViewPager mViewPager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_userinfo, container, false);
        this.mLayoutUserInfo = (LinearLayout) this.mRootView.findViewById(R.id.layoutUserInfo);
        ((Toolbar) this.mRootView.findViewById(R.id.toolbar)).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                UserInfoDialog.this.dismiss();
                return true;
            }
        });
        this.mAdapter = new FragmentsPageAdapter(getChildFragmentManager());
        this.mBasicInfoFragment = new BasicUserInfoFragment();
        this.mBasicInfoFragment.setParent(this);
        this.mUserRightsFragment = new UserRightsFragment();
        this.mUserHonourFragment = new UserHonourFragment();
        this.mUserHonourFragment.setUserName(this.mUserName);
        this.mAdapter.addPage(this.mBasicInfoFragment, "基本信息");
        this.mAdapter.addPage(this.mUserRightsFragment, "权限信息");
        this.mAdapter.addPage(this.mUserHonourFragment, "荣誉信息");
        this.mViewPager = (CustomViewPager) this.mRootView.findViewById(R.id.pager);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOffscreenPageLimit(4);
        TabLayout tabLayout = (TabLayout) this.mRootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(this.mViewPager);
        tabLayout.setOnTabSelectedListener(this);
        setHasOptionsMenu(true);
        if (this.mUserGUID != null) {
            this.mBasicInfoFragment.setData(this.mUserGUID);
            this.mUserRightsFragment.setData(this.mUserGUID);
            this.mUserHonourFragment.setData(this.mUserGUID);
        } else if (this.mUserName != null) {
            WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("UsersGetUserGUID", null);
            ItemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szGUID = ItemObject.readTextData();
                    if (szGUID != null) {
                        UserInfoDialog.this.mUserGUID = szGUID;
                        UserInfoDialog.this.mBasicInfoFragment.setData(UserInfoDialog.this.mUserGUID);
                        UserInfoDialog.this.mUserRightsFragment.setData(UserInfoDialog.this.mUserGUID);
                        UserInfoDialog.this.mUserHonourFragment.setData(UserInfoDialog.this.mUserGUID);
                    }
                }
            });
            ItemObject.setParam("lpszUserName", this.mUserName);
            ItemObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ItemObject);
        }
        return this.mRootView;
    }

    public void setUserGUID(String szUserGUID) {
        this.mUserGUID = szUserGUID;
    }

    public void setUserName(String szUserName) {
        this.mUserName = szUserName;
    }

    public void setUserNameAndType(String szName, String szUserType) {
        ((TextView) this.mLayoutUserInfo.findViewById(R.id.textViewRealName)).setText(szName);
        ((TextView) this.mLayoutUserInfo.findViewById(R.id.textViewUserType)).setText(szUserType);
    }

    public static void showDialog(AppCompatActivity activity, String szUserName) {
        if (activity.getSupportFragmentManager().findFragmentByTag("userinfoDialog") == null) {
            UserInfoDialog infoDialog = new UserInfoDialog();
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            infoDialog.setCancelable(true);
            infoDialog.setUserName(szUserName);
            infoDialog.show(ft, "userinfoDialog");
        }
    }

    public void onTabReselected(Tab arg0) {
    }

    public void onTabSelected(Tab arg0) {
        this.mViewPager.setCurrentItem(arg0.getPosition());
    }

    public void onTabUnselected(Tab arg0) {
    }
}
