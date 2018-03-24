package com.netspace.library.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.FragmentsPageAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.fragment.SearchFragment;
import com.netspace.library.fragment.SubjectLearnFragment;
import com.netspace.library.fragment.TeacherMyiLibraryFragment;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class SubjectLearnActivity extends BaseActivity implements OnQueryTextListener, OnTabSelectedListener {
    private FragmentsPageAdapter mAdapter;
    private int mJyeooLibraryPos = -1;
    private RESTLibraryFragment mJyeooRESTFragment;
    private TeacherMyiLibraryFragment mOwnerLibraryFragment;
    private SearchFragment mSearchFragmentClassRecord;
    private SearchFragment mSearchFragmentLessonPrepare;
    private SearchFragment mSearchFragmentQuestion;
    private SearchFragment mSearchFragmentResource;
    private SubjectLearnFragment mSubjectLearnFragment;
    private CustomViewPager mViewPager;
    private int mZxxkLibraryPos = -1;
    private RESTLibraryFragment mZxxkRESTFragment;
    private ArrayList<ResourceItemData> marrData;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjectlearnview);
        setTitle("学习");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.mAdapter = new FragmentsPageAdapter(getSupportFragmentManager());
        this.mOwnerLibraryFragment = new TeacherMyiLibraryFragment();
        this.mSubjectLearnFragment = new SubjectLearnFragment();
        this.mSearchFragmentQuestion = new SearchFragment();
        this.mSearchFragmentResource = new SearchFragment();
        this.mAdapter.addPage(this.mOwnerLibraryFragment, "我的资源库");
        if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_JYEOOLIBRARY_TEACHERPADONLY)) {
            this.mJyeooLibraryPos = this.mAdapter.getCount();
            this.mJyeooRESTFragment = new RESTLibraryFragment();
            Bundle bundle = new Bundle();
            bundle.putString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX, "Jyeoo");
            bundle.putString(RESTLibraryFragment.ARGUMENT_PATH_SUFFIX, "jyeoo/");
            bundle.putString(RESTLibraryFragment.ARGUMENT_PARAM_SUFFIX, "translate=jyeootranslate.js&count=200&ps=100");
            bundle.putString(RESTLibraryFragment.ARGUMENT_QUESTION_SUFFIX, "question/");
            bundle.putString("", "pi");
            bundle.putBoolean(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_QUESTION_BOOK, false);
            bundle.putBoolean(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_RESOURCELIBRARY, true);
            this.mJyeooRESTFragment.setArguments(bundle);
            this.mAdapter.addPage(this.mJyeooRESTFragment, "试题库");
        }
        if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_ZXXKLIBRARY_TEACHERPADONLY)) {
            this.mZxxkLibraryPos = this.mAdapter.getCount();
            this.mZxxkRESTFragment = new RESTLibraryFragment();
            bundle = new Bundle();
            bundle.putString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX, "Zxxk");
            bundle.putString(RESTLibraryFragment.ARGUMENT_PATH_SUFFIX, "zxxk/");
            bundle.putString(RESTLibraryFragment.ARGUMENT_PARAM_SUFFIX, "translate=zxxktranslate.js&filterpackage=true");
            bundle.putString(RESTLibraryFragment.ARGUMENT_QUESTION_SUFFIX, "");
            bundle.putString("", "startPage");
            bundle.putBoolean(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_QUESTION_BOOK, false);
            bundle.putBoolean(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_RESOURCELIBRARY, true);
            this.mZxxkRESTFragment.setArguments(bundle);
            this.mAdapter.addPage(this.mZxxkRESTFragment, "资源库");
        }
        this.mAdapter.addPage(this.mSubjectLearnFragment, "专题学习");
        this.mAdapter.addPage(this.mSearchFragmentQuestion, "试题搜索结果");
        this.mAdapter.addPage(this.mSearchFragmentResource, "资源搜索结果");
        this.mSearchFragmentQuestion.setType(1, 2);
        this.mSearchFragmentResource.setType(4, 5);
        this.mViewPager = (CustomViewPager) findViewById(R.id.pager);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOffscreenPageLimit(4);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(this.mViewPager);
        tabLayout.setOnTabSelectedListener(this);
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(0).toString());
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subjectlearn, menu);
        menu.findItem(R.id.action_search).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_search).colorRes(R.color.toolbar).actionBarSize());
        if (menu.findItem(R.id.action_search) != null) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
            if (searchView != null) {
                searchView.setOnQueryTextListener(this);
                changeSearchViewTextColor(searchView);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void changeSearchViewTextColor(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof TextView) {
            ((TextView) view).setHint("在这里输入搜索内容...");
            ((TextView) view).setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                changeSearchViewTextColor(viewGroup.getChildAt(i));
            }
        }
    }

    public boolean onQueryTextChange(String arg0) {
        return false;
    }

    public boolean onQueryTextSubmit(String arg0) {
        if (this.mViewPager.getCurrentItem() == this.mJyeooLibraryPos) {
            if (!arg0.isEmpty()) {
                this.mJyeooRESTFragment.startSearch(arg0);
            }
        } else if (this.mViewPager.getCurrentItem() != this.mZxxkLibraryPos) {
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("SearchEverything", UI.getCurrentActivity());
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    ArrayList<String> arrTitle = (ArrayList) ItemObject.getParam("0");
                    ArrayList<String> arrGUID = (ArrayList) ItemObject.getParam("1");
                    ArrayList<String> arrObjectType = (ArrayList) ItemObject.getParam("2");
                    SubjectLearnActivity.this.marrData = new ArrayList();
                    for (int i = 0; i < arrTitle.size(); i++) {
                        ResourceItemData newItem = new ResourceItemData();
                        newItem.szTitle = (String) arrTitle.get(i);
                        newItem.szGUID = (String) arrGUID.get(i);
                        newItem.nType = Integer.parseInt((String) arrObjectType.get(i));
                        SubjectLearnActivity.this.marrData.add(newItem);
                    }
                    SubjectLearnActivity.this.mSearchFragmentQuestion.handleSearchResult(SubjectLearnActivity.this.marrData);
                    SubjectLearnActivity.this.mSearchFragmentResource.handleSearchResult(SubjectLearnActivity.this.marrData);
                    if (SubjectLearnActivity.this.marrData.size() > 0 && SubjectLearnActivity.this.mViewPager.getCurrentItem() == 0) {
                        SubjectLearnActivity.this.mViewPager.setCurrentItem(1);
                    }
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showAlertMessage(SubjectLearnActivity.this, "搜索错误", "搜索出现错误，错误信息：" + ItemObject.getErrorText());
                }
            });
            CallItem.setParam("lpszKeywords", arg0.replace("%", "").replace("'", "").replace("\"", "").replace("*", ""));
            VirtualNetworkObject.addToQueue(CallItem);
        } else if (!arg0.isEmpty()) {
            this.mZxxkRESTFragment.startSearch(arg0);
        }
        return true;
    }

    public void onBackPressed() {
        if (this.mViewPager.getCurrentItem() != 0 || !this.mOwnerLibraryFragment.goBack()) {
            if (this.mViewPager.getCurrentItem() != this.mJyeooLibraryPos || !this.mJyeooRESTFragment.goBack()) {
                if (this.mViewPager.getCurrentItem() != this.mZxxkLibraryPos || !this.mZxxkRESTFragment.goBack()) {
                    super.onBackPressed();
                }
            }
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
}
