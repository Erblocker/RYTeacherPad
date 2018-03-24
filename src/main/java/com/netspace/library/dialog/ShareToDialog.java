package com.netspace.library.dialog;

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
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.FragmentsPageAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.fragment.AllUserFragment;
import com.netspace.library.fragment.CustomUserGroupFragment;
import com.netspace.library.fragment.RecentUserFragment;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class ShareToDialog extends DialogFragment implements OnTabSelectedListener {
    private FragmentsPageAdapter mAdapter;
    private AllUserFragment mAllUserFragment;
    private ShareDialogCallBack mCallBack;
    private CustomUserGroupFragment mCustomUserGroupFragment;
    private ShareDialogDismissCallBack mDismissCallBack;
    private RecentUserFragment mRecentUserFragment;
    private View mRootView;
    private CustomViewPager mViewPager;
    private ArrayList<String> marrSelectedUserJIDs = new ArrayList();

    public interface ShareDialogCallBack {
        void onShare(ArrayList<String> arrayList);
    }

    public interface ShareDialogDismissCallBack {
        void onDismiss();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_common, container, false);
        Toolbar toolbar = (Toolbar) this.mRootView.findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "分享");
        ((ImageView) this.mRootView.findViewById(R.id.imageViewLogo)).setImageResource(R.drawable.ic_share2);
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                Utilities.logMenuClick(arg0, "ShareToDialog");
                if (arg0.getItemId() == R.id.action_yes) {
                    ArrayList<String> arrUserNames = new ArrayList();
                    if (ShareToDialog.this.mViewPager.getCurrentItem() == 0) {
                        ShareToDialog.this.marrSelectedUserJIDs.addAll(ShareToDialog.this.mRecentUserFragment.getSelectedUsersJIDs());
                        arrUserNames.addAll(ShareToDialog.this.mRecentUserFragment.getSelectedUsersNames());
                    } else if (ShareToDialog.this.mViewPager.getCurrentItem() == 1) {
                        ShareToDialog.this.marrSelectedUserJIDs.addAll(ShareToDialog.this.mAllUserFragment.getSelectedUsersJIDs());
                        arrUserNames.addAll(ShareToDialog.this.mAllUserFragment.getSelectedUsersNames());
                    } else if (ShareToDialog.this.mViewPager.getCurrentItem() == 2) {
                        ShareToDialog.this.marrSelectedUserJIDs.addAll(ShareToDialog.this.mCustomUserGroupFragment.getSelectedUsersJIDs());
                        arrUserNames.addAll(ShareToDialog.this.mCustomUserGroupFragment.getSelectedUsersNames());
                    }
                    if (ShareToDialog.this.marrSelectedUserJIDs.size() == 0) {
                        Utilities.showAlertMessage(ShareToDialog.this.getActivity(), "没有选择内容", "请选择至少一个分享对象。");
                        return false;
                    }
                    if (ShareToDialog.this.marrSelectedUserJIDs.size() < 10) {
                        for (int i = 0; i < ShareToDialog.this.marrSelectedUserJIDs.size(); i++) {
                            MyiBaseApplication.getRecentUser().addUser((String) arrUserNames.get(i), (String) ShareToDialog.this.marrSelectedUserJIDs.get(i), 0);
                        }
                    }
                    if (ShareToDialog.this.mCallBack != null) {
                        ShareToDialog.this.mCallBack.onShare(ShareToDialog.this.marrSelectedUserJIDs);
                    }
                    ShareToDialog.this.dismiss();
                    return true;
                }
                if (ShareToDialog.this.mViewPager.getCurrentItem() == 0) {
                    if (arg0.getItemId() == R.id.action_selectall) {
                        ShareToDialog.this.mRecentUserFragment.select(true);
                    } else if (arg0.getItemId() == R.id.action_selectnone) {
                        ShareToDialog.this.mRecentUserFragment.select(false);
                    }
                } else if (ShareToDialog.this.mViewPager.getCurrentItem() == 1) {
                    if (arg0.getItemId() == R.id.action_selectall) {
                        ShareToDialog.this.mAllUserFragment.select(true);
                    } else if (arg0.getItemId() == R.id.action_selectnone) {
                        ShareToDialog.this.mAllUserFragment.select(false);
                    }
                } else if (ShareToDialog.this.mViewPager.getCurrentItem() == 2) {
                    if (arg0.getItemId() == R.id.action_selectall) {
                        ShareToDialog.this.mCustomUserGroupFragment.select(true);
                    } else if (arg0.getItemId() == R.id.action_selectnone) {
                        ShareToDialog.this.mCustomUserGroupFragment.select(false);
                    }
                }
                return true;
            }
        });
        toolbar.inflateMenu(R.menu.menu_selectok_withtext);
        toolbar.getMenu().findItem(R.id.action_selectall).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_check_square_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        toolbar.getMenu().findItem(R.id.action_selectnone).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_square_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mAdapter = new FragmentsPageAdapter(getChildFragmentManager());
        this.mRecentUserFragment = new RecentUserFragment();
        this.mAllUserFragment = new AllUserFragment();
        this.mAdapter.addPage(this.mRecentUserFragment, "最近联系人");
        if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
            this.mAdapter.addPage(this.mAllUserFragment, "全部老师");
        } else {
            this.mCustomUserGroupFragment = new CustomUserGroupFragment();
            this.mAdapter.addPage(this.mAllUserFragment, "全部学生");
            this.mAdapter.addPage(this.mCustomUserGroupFragment, "我的分组");
        }
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

    public void setCallBack(ShareDialogCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void setDismissCallBack(ShareDialogDismissCallBack CallBack) {
        this.mDismissCallBack = CallBack;
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
}
