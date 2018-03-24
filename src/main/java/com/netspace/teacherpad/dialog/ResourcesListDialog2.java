package com.netspace.teacherpad.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.activity.ResourcesViewActivity2;
import com.netspace.library.adapter.FragmentsPageAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.fragment.MyiLibraryFragment.MyiLibraryCallBackListener;
import com.netspace.library.fragment.TeacherMyiLibraryFragment;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.fragments.ResourcesListFragment;
import java.util.ArrayList;

public class ResourcesListDialog2 extends DialogFragment implements OnTabSelectedListener, MyiLibraryCallBackListener {
    private FragmentsPageAdapter mAdapter;
    private ResourceDialogCallBack mCallBack;
    private StartClassControlUnit mControlUnit;
    private TeacherMyiLibraryFragment mMyiLibraryFragment;
    private ResourcesListFragment mResourcesListFragment;
    private View mRootView;
    private CustomViewPager mViewPager;

    public interface ResourceDialogCallBack {
        void onShare(ArrayList<String> arrayList);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_common, container, false);
        Toolbar toolbar = (Toolbar) this.mRootView.findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) "资源列表");
        ((ImageView) this.mRootView.findViewById(R.id.imageViewLogo)).setImageResource(R.drawable.ic_books);
        setRetainInstance(true);
        this.mControlUnit = new StartClassControlUnit(getContext());
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                int nID = arg0.getItemId();
                View view = new View(MyiBaseApplication.getBaseAppContext());
                view.setId(nID);
                ResourcesListDialog2.this.mControlUnit.onClick(view);
                if (nID != R.id.buttonBlank) {
                    ResourcesListDialog2.this.dismiss();
                }
                return false;
            }
        });
        toolbar.inflateMenu(R.menu.menu_resourcelist2);
        toolbar.getMenu().findItem(R.id.buttonCamera).setIcon(new IconDrawable(getContext(), FontAwesomeIcons.fa_camera_retro).colorRes(R.color.actiontoolbartextcolor).actionBarSize());
        toolbar.getMenu().findItem(R.id.buttonBlank).setIcon(new IconDrawable(getContext(), FontAwesomeIcons.fa_sticky_note_o).colorRes(R.color.actiontoolbartextcolor).actionBarSize());
        SubMenu whiteBoardSubMenu = toolbar.getMenu().findItem(R.id.buttonBlank).getSubMenu();
        TypedArray arrWhiteBoardIDs = getContext().getResources().obtainTypedArray(R.array.whiteboards_ids);
        String[] whiteBoardNames = getContext().getResources().getStringArray(R.array.whiteboards_names);
        int[] whiteBoardColors = getContext().getResources().getIntArray(R.array.whiteboards_colors);
        for (int i = 0; i < arrWhiteBoardIDs.length(); i++) {
            whiteBoardSubMenu.add(0, arrWhiteBoardIDs.getResourceId(i, 0), 0, whiteBoardNames[i]).setIcon(new IconDrawable(getContext(), FontAwesomeIcons.fa_square).color(whiteBoardColors[i]).actionBarSize());
        }
        this.mAdapter = new FragmentsPageAdapter(getChildFragmentManager());
        this.mResourcesListFragment = new ResourcesListFragment();
        this.mMyiLibraryFragment = new TeacherMyiLibraryFragment();
        this.mMyiLibraryFragment.setReadOnly(true);
        this.mMyiLibraryFragment.setCallBack(this);
        this.mAdapter.addPage(this.mResourcesListFragment, "当前备课所用资源");
        this.mAdapter.addPage(this.mMyiLibraryFragment, "我的资源库");
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

    public void setCallBack(ResourceDialogCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void onTabReselected(Tab arg0) {
    }

    public void onTabSelected(Tab arg0) {
        this.mViewPager.setCurrentItem(arg0.getPosition());
    }

    public void onTabUnselected(Tab arg0) {
    }

    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(Utilities.dpToPixel(600, getContext()), -1);
        }
    }

    private void openResource(ResourceItemData data) {
        Intent Intent;
        if (data.nType == 0) {
            Intent = new Intent(getActivity(), ResourceDetailActivity.class);
            Intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
            Intent.putExtra("title", data.szTitle);
            Intent.putExtra("isquestion", true);
            Intent.putExtra("resourcetype", data.nType);
            startActivity(Intent);
        } else if (data.nType < 1000 || data.nType >= 2000) {
            Intent = new Intent(getActivity(), ResourceDetailActivity.class);
            Intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
            Intent.putExtra("title", data.szTitle);
            Intent.putExtra("isquestion", false);
            Intent.putExtra("resourcetype", data.nType);
            startActivity(Intent);
        } else {
            Intent = new Intent(getActivity(), ResourcesViewActivity2.class);
            Intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
            Intent.putExtra("scheduleguid", data.szGUID);
            Intent.putExtra("displayanswers", true);
            Intent.putExtra("enableunlock", true);
            Intent.putExtra(UserHonourFragment.USERCLASSGUID, "");
            Intent.putExtra("userclassname", "");
            Intent.putExtra("resourcetype", data.nType);
            startActivity(Intent);
        }
    }

    public boolean OnResourceClick(final ResourceItemData itemData) {
        ArrayList<String> arrOptionTexts = new ArrayList();
        String[] arrNames = new String[]{"在平板上打开资源", "加入当前备课"};
        arrOptionTexts.add("在平板上打开资源");
        arrOptionTexts.add("加入当前备课");
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            arrOptionTexts.add("仅在大屏幕" + String.valueOf(i + 1) + "上打开资源");
        }
        new Builder(new ContextThemeWrapper(getActivity(), 16974130)).setItems((String[]) arrOptionTexts.toArray(arrNames), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    ResourcesListDialog2.this.openResource(itemData);
                } else if (which == 1) {
                    TeacherPadApplication.arrResourceData.add(0, itemData);
                    ResourcesListFragment.refrehResourceAdapter();
                    ResourcesListDialog2.this.mViewPager.setCurrentItem(0);
                } else {
                    TeacherPadApplication.mapResourceTitles.put(itemData.szGUID, itemData.szTitle);
                    int nScreenID = which - 2;
                    StartClassControlUnit.switchToResource(itemData.szGUID, itemData.nType, nScreenID);
                }
            }
        }).setTitle("选择动作").create().show();
        return true;
    }

    public boolean OnQuestionClick(ResourceItemData itemData) {
        OnResourceClick(itemData);
        return true;
    }

    public void onSaveInstanceState(Bundle arg0) {
        super.onSaveInstanceState(arg0);
    }

    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
