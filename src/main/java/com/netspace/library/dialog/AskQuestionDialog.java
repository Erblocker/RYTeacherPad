package com.netspace.library.dialog;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.fragment.QuestionTimeFragment;
import com.netspace.library.fragment.QuestionTypeFragment;
import com.netspace.library.fragment.QuestionUserFragment;
import com.netspace.library.fragment.QuestionUserFragment.StudentInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class AskQuestionDialog extends DialogFragment implements OnTabSelectedListener {
    private FragmentsPageAdapter mAdapter;
    private OnStartAskQuestionCallBack mCallBack;
    private QuestionTimeFragment mQuestionTimeFragment;
    private QuestionTypeFragment mQuestionTypeFragment;
    private QuestionUserFragment mQuestionUserFragment;
    private View mRootView;
    private String mUserClassGUID;
    private CustomViewPager mViewPager;
    private String mszOldQuestionAnswer;
    private String mszOldQuestionIMCommand;
    private String mszQuestionGroupGUID;
    private String mszTitle;

    public interface OnStartAskQuestionCallBack {
        void OnStartQuestion(AskQuestionDialog askQuestionDialog, String str, String str2, String str3, int i);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_common, container, false);
        Toolbar toolbar = (Toolbar) this.mRootView.findViewById(R.id.toolbar);
        if (this.mszTitle != null) {
            toolbar.setTitle(this.mszTitle);
        } else {
            toolbar.setTitle((CharSequence) "发起做题");
        }
        ((ImageView) this.mRootView.findViewById(R.id.imageViewLogo)).setVisibility(8);
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                Utilities.logMenuClick(arg0, "AskQuestionDialog");
                if (arg0.getItemId() == R.id.action_yes || arg0.getItemId() == R.id.action_newsubmit) {
                    if (AskQuestionDialog.this.mCallBack != null) {
                        AskQuestionDialog.this.mQuestionTypeFragment.prepareData();
                        String szIMMessage = AskQuestionDialog.this.mQuestionTimeFragment.repackIMCommand(AskQuestionDialog.this.mQuestionTypeFragment.getQuestionIMData());
                        if (arg0.getItemId() == R.id.action_newsubmit) {
                            szIMMessage = "AllSubmitSkipRealSubmit\n" + szIMMessage;
                        }
                        AskQuestionDialog.this.mCallBack.OnStartQuestion(AskQuestionDialog.this, szIMMessage, AskQuestionDialog.this.mQuestionTypeFragment.getQuestionCorrectAnswer(), AskQuestionDialog.this.mQuestionUserFragment.getSelectedUserJIDs(";"), AskQuestionDialog.this.mQuestionTypeFragment.getQuestionType());
                        AskQuestionDialog.this.dismiss();
                    }
                } else if (arg0.getItemId() == R.id.action_selectall) {
                    AskQuestionDialog.this.mQuestionUserFragment.select(true);
                } else if (arg0.getItemId() == R.id.action_selectnone) {
                    AskQuestionDialog.this.mQuestionUserFragment.select(false);
                } else if (arg0.getItemId() == R.id.action_no) {
                    AskQuestionDialog.this.dismiss();
                }
                return true;
            }
        });
        toolbar.inflateMenu(R.menu.menu_askquestion);
        toolbar.getMenu().findItem(R.id.action_selectall).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_check_square_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        toolbar.getMenu().findItem(R.id.action_selectnone).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_square_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mAdapter = new FragmentsPageAdapter(getChildFragmentManager());
        this.mQuestionTypeFragment = new QuestionTypeFragment();
        this.mQuestionTypeFragment.setOldInfo(this.mszOldQuestionIMCommand, this.mszOldQuestionAnswer);
        this.mQuestionTypeFragment.setQuestionGroupGUID(this.mszQuestionGroupGUID);
        this.mQuestionUserFragment = new QuestionUserFragment();
        this.mQuestionUserFragment.setUserClassGUID(this.mUserClassGUID);
        this.mQuestionTimeFragment = new QuestionTimeFragment();
        this.mAdapter.addPage(this.mQuestionTypeFragment, "题型");
        this.mAdapter.addPage(this.mQuestionUserFragment, "学生");
        this.mAdapter.addPage(this.mQuestionTimeFragment, "时间");
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

    public void setTitle(String szTitle) {
        this.mszTitle = szTitle;
    }

    public void setUserClassGUID(String szGUID) {
        this.mUserClassGUID = szGUID;
    }

    public void setCallBack(OnStartAskQuestionCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public ArrayList<StudentInfo> getNeedAnswerStudents() {
        return this.mQuestionUserFragment.getNeedAnswerStudents();
    }

    public void setOldInfo(String szOldIMCommand, String szOldAnswer) {
        this.mszOldQuestionIMCommand = szOldIMCommand;
        this.mszOldQuestionAnswer = szOldAnswer;
    }

    public static void showDialog(FragmentActivity activity, String szUserClassGUID, OnStartAskQuestionCallBack CallBack, String szOldIMCommand, String szOldQuestionAnswer, String szQuestionGroupGUID, String szTitle) {
        if (activity.getSupportFragmentManager().findFragmentByTag("askQuestionDialog") == null) {
            AskQuestionDialog infoDialog = new AskQuestionDialog();
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            infoDialog.setUserClassGUID(szUserClassGUID);
            infoDialog.mszQuestionGroupGUID = szQuestionGroupGUID;
            infoDialog.setCancelable(false);
            infoDialog.setCallBack(CallBack);
            infoDialog.setTitle(szTitle);
            infoDialog.setOldInfo(szOldIMCommand, szOldQuestionAnswer);
            infoDialog.show(ft, "askQuestionDialog");
        }
    }

    public void onTabReselected(Tab arg0) {
    }

    public void onTabSelected(Tab arg0) {
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(arg0.getPosition()).toString());
        this.mViewPager.setCurrentItem(arg0.getPosition());
    }

    public void onTabUnselected(Tab arg0) {
    }

    public void onStart() {
        super.onStart();
        Utilities.setDialogToScreenSize((DialogFragment) this, 0.7f);
    }
}
