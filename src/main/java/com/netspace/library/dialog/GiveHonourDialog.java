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
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.fragment.UserHonourFragment.UserHonourCallBack;
import com.netspace.library.struct.UserHonourData;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class GiveHonourDialog extends DialogFragment implements OnTabSelectedListener, UserHonourCallBack {
    private FragmentsPageAdapter mAdapter;
    private UserHonourFragment mAllHonourFragment;
    private AllUserFragment mAllUserFragment;
    private GiveHonourDialogCallBack mCallBack;
    private CustomUserGroupFragment mCustomUserGroupFragment;
    private GiveHonourDialogDismissCallBack mDismissCallBack;
    private RecentUserFragment mRecentUserFragment;
    private View mRootView;
    private CustomViewPager mViewPager;
    private ArrayList<UserHonourData> marrSelectedHonour = new ArrayList();
    private ArrayList<String> marrSelectedUserJIDs = new ArrayList();
    private String mszUserClassGUID;

    public interface GiveHonourDialogCallBack {
        void onGiveHonour(ArrayList<UserHonourData> arrayList, ArrayList<String> arrayList2);
    }

    public interface GiveHonourDialogDismissCallBack {
        void onDismiss();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_common, container, false);
        Toolbar toolbar = (Toolbar) this.mRootView.findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "奖励");
        ((ImageView) this.mRootView.findViewById(R.id.imageViewLogo)).setImageResource(R.drawable.ic_medal);
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                Utilities.logMenuClick(arg0, "GiveHonourDialog");
                if (arg0.getItemId() == R.id.action_yes) {
                    ArrayList<String> arrUserNames = new ArrayList();
                    ArrayList<UserHonourData> arrHonourData = GiveHonourDialog.this.marrSelectedHonour;
                    GiveHonourDialog.this.marrSelectedUserJIDs.clear();
                    if (GiveHonourDialog.this.mViewPager.getCurrentItem() == 1) {
                        GiveHonourDialog.this.marrSelectedUserJIDs.addAll(GiveHonourDialog.this.mRecentUserFragment.getSelectedUsersJIDs());
                        arrUserNames.addAll(GiveHonourDialog.this.mRecentUserFragment.getSelectedUsersNames());
                    } else if (GiveHonourDialog.this.mViewPager.getCurrentItem() == 2) {
                        GiveHonourDialog.this.marrSelectedUserJIDs.addAll(GiveHonourDialog.this.mAllUserFragment.getSelectedUsersJIDs());
                        arrUserNames.addAll(GiveHonourDialog.this.mAllUserFragment.getSelectedUsersNames());
                    } else if (GiveHonourDialog.this.mViewPager.getCurrentItem() == 3) {
                        GiveHonourDialog.this.marrSelectedUserJIDs.addAll(GiveHonourDialog.this.mCustomUserGroupFragment.getSelectedUsersJIDs());
                        arrUserNames.addAll(GiveHonourDialog.this.mCustomUserGroupFragment.getSelectedUsersNames());
                    }
                    if (arrHonourData.size() == 0) {
                        Utilities.showAlertMessage(GiveHonourDialog.this.getActivity(), "没有选择奖励", "请先选择至少一个需要给予的奖励。");
                        return false;
                    } else if (GiveHonourDialog.this.marrSelectedUserJIDs.size() == 0) {
                        Utilities.showAlertMessage(GiveHonourDialog.this.getActivity(), "没有选择内容", "请选择至少一个奖励对象，如果您已经选择了，请切换到对应的学生标签后再点击提交按钮。");
                        return false;
                    } else {
                        if (GiveHonourDialog.this.marrSelectedUserJIDs.size() < 10) {
                            for (int i = 0; i < GiveHonourDialog.this.marrSelectedUserJIDs.size(); i++) {
                                MyiBaseApplication.getRecentUser().addUser((String) arrUserNames.get(i), (String) GiveHonourDialog.this.marrSelectedUserJIDs.get(i), 0);
                            }
                        }
                        if (GiveHonourDialog.this.mCallBack != null) {
                            GiveHonourDialog.this.mCallBack.onGiveHonour(arrHonourData, GiveHonourDialog.this.marrSelectedUserJIDs);
                        }
                        GiveHonourDialog.this.dismiss();
                        return true;
                    }
                }
                if (GiveHonourDialog.this.mViewPager.getCurrentItem() == 0) {
                    if (arg0.getItemId() == R.id.action_selectall) {
                        GiveHonourDialog.this.mAllHonourFragment.select(true);
                    } else if (arg0.getItemId() == R.id.action_selectnone) {
                        GiveHonourDialog.this.mAllHonourFragment.select(false);
                    }
                } else if (GiveHonourDialog.this.mViewPager.getCurrentItem() == 1) {
                    if (arg0.getItemId() == R.id.action_selectall) {
                        GiveHonourDialog.this.mRecentUserFragment.select(true);
                    } else if (arg0.getItemId() == R.id.action_selectnone) {
                        GiveHonourDialog.this.mRecentUserFragment.select(false);
                    }
                } else if (GiveHonourDialog.this.mViewPager.getCurrentItem() == 2) {
                    if (arg0.getItemId() == R.id.action_selectall) {
                        GiveHonourDialog.this.mAllUserFragment.select(true);
                    } else if (arg0.getItemId() == R.id.action_selectnone) {
                        GiveHonourDialog.this.mAllUserFragment.select(false);
                    }
                } else if (GiveHonourDialog.this.mViewPager.getCurrentItem() == 3) {
                    if (arg0.getItemId() == R.id.action_selectall) {
                        GiveHonourDialog.this.mCustomUserGroupFragment.select(true);
                    } else if (arg0.getItemId() == R.id.action_selectnone) {
                        GiveHonourDialog.this.mCustomUserGroupFragment.select(false);
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
        this.mCustomUserGroupFragment = new CustomUserGroupFragment();
        this.mRecentUserFragment.setNoMessageGroup(true);
        this.mAllUserFragment.setNoMessageGroup(true);
        this.mRecentUserFragment.setNoMessageGroup(true);
        this.mAllHonourFragment = new UserHonourFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(UserHonourFragment.ALLOWMULTISELECT, true);
        bundle.putString(UserHonourFragment.USERCLASSGUID, this.mszUserClassGUID);
        this.mAllHonourFragment.setArguments(bundle);
        this.mAllHonourFragment.setCallBack(this);
        this.mAdapter.addPage(this.mAllHonourFragment, "可用的奖励");
        this.mAdapter.addPage(this.mRecentUserFragment, "最近联系人");
        this.mAdapter.addPage(this.mAllUserFragment, "全部学生");
        this.mAdapter.addPage(this.mCustomUserGroupFragment, "我的分组");
        this.mViewPager = (CustomViewPager) this.mRootView.findViewById(R.id.pager);
        this.mViewPager.setOffscreenPageLimit(6);
        this.mViewPager.setAdapter(this.mAdapter);
        TabLayout tabLayout = (TabLayout) this.mRootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(this.mViewPager);
        tabLayout.setOnTabSelectedListener(this);
        setHasOptionsMenu(true);
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(0).toString());
        return this.mRootView;
    }

    public void setCallBack(GiveHonourDialogCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void setDismissCallBack(GiveHonourDialogDismissCallBack CallBack) {
        this.mDismissCallBack = CallBack;
    }

    public void setUserClassGUID(String szUserClassGUID) {
        this.mszUserClassGUID = szUserClassGUID;
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

    public void onHonourSelected(UserHonourData honourData) {
        int i = 0;
        while (i < this.marrSelectedHonour.size()) {
            if (!((UserHonourData) this.marrSelectedHonour.get(i)).szImageDataGUID.equalsIgnoreCase(honourData.szImageDataGUID)) {
                i++;
            } else {
                return;
            }
        }
        this.marrSelectedHonour.add(honourData);
    }

    public void onHonourDeselected(UserHonourData honourData) {
        int i = 0;
        while (i < this.marrSelectedHonour.size()) {
            if (((UserHonourData) this.marrSelectedHonour.get(i)).szImageDataGUID.equalsIgnoreCase(honourData.szImageDataGUID)) {
                this.marrSelectedHonour.remove(i);
                i--;
            }
            i++;
        }
    }
}
