package com.netspace.library.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.components.ContentDisplayComponent;
import com.netspace.library.components.fragment.CommentComponentFragment;
import com.netspace.library.components.fragment.ContentDisplayComponentFragment;
import com.netspace.library.components.fragment.ContentDisplayComponentFragment.ContentDisplayExtensionCallBack;
import com.netspace.library.consts.Features;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.dialog.ShareToDialog;
import com.netspace.library.dialog.ShareToDialog.ShareDialogCallBack;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class ResourceDetailActivity extends BaseActivity implements OnTabSelectedListener {
    private static ContentDisplayExtensionCallBack mExtensionCallBack;
    private ResourceDetailPageAdapter mAdapter;
    private CommentComponentFragment mCommentFragment;
    private LinearLayout mContentLayout;
    private ContentDisplayComponentFragment mDisplayerFragment;
    private LinearLayout mLayoutNav;
    private CustomViewPager mViewPager;
    private ArrayList<String> marrFragmentTitles = new ArrayList();
    private ArrayList<Fragment> marrFragments = new ArrayList();

    public class ResourceDetailPageAdapter extends FragmentPagerAdapter {
        public ResourceDetailPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return (Fragment) ResourceDetailActivity.this.marrFragments.get(position);
        }

        public int getCount() {
            return ResourceDetailActivity.this.marrFragments.size();
        }

        public CharSequence getPageTitle(int position) {
            return (CharSequence) ResourceDetailActivity.this.marrFragmentTitles.get(position);
        }
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_resourcedetail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.mLayoutNav = (LinearLayout) findViewById(R.id.layoutNav);
        this.mContentLayout = (LinearLayout) findViewById(R.id.layoutContent);
        this.mViewPager = (CustomViewPager) findViewById(R.id.pager);
        this.mViewPager.setEnableSameSize(true);
        this.mAdapter = new ResourceDetailPageAdapter(getSupportFragmentManager());
        this.mViewPager.setAdapter(this.mAdapter);
        this.mDisplayerFragment = new ContentDisplayComponentFragment();
        this.mCommentFragment = new CommentComponentFragment();
        Bundle displayBundle = new Bundle();
        if (getIntent() != null) {
            String szResourceGUID = getIntent().getStringExtra(CommentComponent.RESOURCEGUID);
            boolean bIsQuestion = getIntent().getBooleanExtra("isquestion", true);
            String szTitle = getIntent().getStringExtra("title");
            if (szTitle != null) {
                setTitle(szTitle);
            }
            JSONObject JSON = new JSONObject();
            try {
                JSON.put(CommentComponent.RESOURCEGUID, szResourceGUID);
                JSON.put("isquestion", bIsQuestion);
                JSON.put("guid", "");
                JSON.put("title", szTitle);
            } catch (JSONException e) {
            }
            displayBundle.putString("data", JSON.toString());
            boolean bAllowSwitchAnswers = getIntent().getBooleanExtra("allowSwitchAnswers", false);
            boolean bAllowAddToResourceLibrary = getIntent().getBooleanExtra(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_RESOURCELIBRARY, false);
            if (bAllowSwitchAnswers) {
                displayBundle.putInt(ContentDisplayComponent.FLAGS, 262);
                displayBundle.putBoolean(ContentDisplayComponent.ANSWERSWITCHBUTTON, true);
            } else {
                displayBundle.putInt(ContentDisplayComponent.FLAGS, 326);
            }
            this.mDisplayerFragment.setExtensionCallBack(mExtensionCallBack);
            if (getIntent().getBooleanExtra(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_QUESTION_BOOK, false)) {
                displayBundle.putBoolean(ContentDisplayComponent.ADDTOQUESTIONBOOK, true);
            }
            if (bAllowAddToResourceLibrary) {
                displayBundle.putBoolean(ContentDisplayComponent.ADDTORESOURCELIBRARY, true);
            }
            this.mDisplayerFragment.setArguments(displayBundle);
            Bundle commentBundle = new Bundle();
            commentBundle.putString(CommentComponent.RESOURCEGUID, szResourceGUID);
            this.mCommentFragment.setArguments(commentBundle);
            this.marrFragments.add(this.mDisplayerFragment);
            this.marrFragmentTitles.add("内容");
            if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_DISCUSS_READ)) {
                this.marrFragments.add(this.mCommentFragment);
                this.marrFragmentTitles.add("讨论");
            }
            this.mAdapter.notifyDataSetChanged();
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(this.mViewPager);
            tabLayout.setOnTabSelectedListener(this);
        } else {
            finish();
        }
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(0).toString());
    }

    public static void setExtensionCallBack(ContentDisplayExtensionCallBack CallBack) {
        mExtensionCallBack = CallBack;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_resourcedetail, menu);
        menu.findItem(R.id.action_share).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_share_alt).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Utilities.logMenuClick(menuItem);
        if (menuItem.getItemId() == 16908332) {
            finish();
        } else if (menuItem.getItemId() == R.id.action_currentimtarget) {
            ChatComponent ActiveComponent = ChatComponent.getCurrentChatComponent();
            if (ActiveComponent != null) {
                if (!ActiveComponent.sendMessage("INTENT=" + getIntent().toUri(0))) {
                    Utilities.showAlertMessage(this, "无法发送", "请检查当前是否没有选择在线答疑对象。");
                }
            } else {
                Utilities.showAlertMessage(this, "无法发送", "没有检测到当前打开了聊天窗口。");
            }
        } else if (menuItem.getItemId() == R.id.action_select_user && getSupportFragmentManager().findFragmentByTag("shareDialog") == null) {
            ShareToDialog shareDialog = new ShareToDialog();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            shareDialog.setCancelable(true);
            shareDialog.setCallBack(new ShareDialogCallBack() {
                public void onShare(ArrayList<String> arrSelectedUserJIDs) {
                    String szTextToSend = "INTENT=" + ResourceDetailActivity.this.getIntent().toUri(0);
                    for (int i = 0; i < arrSelectedUserJIDs.size(); i++) {
                        ChatComponent.sendMessage((String) arrSelectedUserJIDs.get(i), szTextToSend);
                    }
                    Utilities.showAlertMessage(ResourceDetailActivity.this, "已发出", "该内容已成功分享。");
                }
            });
            shareDialog.show(ft, "shareDialog");
        }
        return super.onOptionsItemSelected(menuItem);
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
