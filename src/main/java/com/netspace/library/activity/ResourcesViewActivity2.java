package com.netspace.library.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.ResourceListAdapter;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.components.LessonPrepareDisplayComponent;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.service.AnswerSheetV2MenuService;
import com.netspace.library.service.AnswerSheetV3MenuService;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.AutoHideNavBar;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class ResourcesViewActivity2 extends BaseActivity implements OnItemSelectedListener, OnItemClickListener {
    protected static String mClientID = "";
    protected static IWmExamDBOpenHelper mDataBase;
    private ResourceListAdapter mAdapter;
    private AutoHideNavBar mAutoHideNavBar;
    private LinearLayout mContentLayout;
    private LessonPrepareDisplayComponent mDisplayer;
    private DrawerLayout mDrawer;
    private Handler mHandler = new Handler();
    private LinearLayout mLayoutNav;
    private ListView mListView;
    private Runnable mReloadCorrectResultRunnable = new Runnable() {
        public void run() {
            ResourcesViewActivity2.this.mDisplayer.reloadCorrectResult();
            if (ResourcesViewActivity2.this.mAdapter != null) {
                ResourcesViewActivity2.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    private LockableScrollView mScrollView;
    private Spinner mSpinner;
    private String mTitle;
    private boolean mbDisplayAnswer = false;
    private int mnAnswerFilterIndex = 2;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_resourcesview2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (VERSION.SDK_INT >= 21) {
            findViewById(R.id.shadow).setVisibility(8);
        }
        setDoublePressReturn(true);
        this.mLayoutNav = (LinearLayout) findViewById(R.id.layoutNav);
        this.mContentLayout = (LinearLayout) findViewById(R.id.layoutContent);
        this.mDisplayer = new LessonPrepareDisplayComponent(this);
        if (mDataBase != null) {
            this.mDisplayer.setDataBase(mDataBase);
        }
        this.mContentLayout.addView(this.mDisplayer, new LayoutParams(-1, -1));
        this.mScrollView = this.mDisplayer.getScrollView();
        this.mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        this.mDrawer.setDrawerShadow(R.drawable.drawer_shadow, 8388611);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, this.mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerStateChanged(int newState) {
                if (newState == 1 && !ResourcesViewActivity2.this.mDrawer.isDrawerOpen(3) && ResourcesViewActivity2.this.mAdapter != null) {
                    ResourcesViewActivity2.this.mAdapter.notifyDataSetChanged();
                }
            }
        };
        this.mDrawer.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        this.mDisplayer.setCallBack(new ComponentCallBack() {
            public void OnDataLoaded(String szFileName, IComponents Component) {
                if (ResourcesViewActivity2.this.mTitle == null) {
                    ResourcesViewActivity2.this.getSupportActionBar().setTitle(ResourcesViewActivity2.this.mDisplayer.getTitle());
                }
                ResourcesViewActivity2.this.initResourcesList();
            }

            public void OnDataUploaded(String szData, IComponents Component) {
            }

            public void OnRequestIntent(Intent intent, IComponents Component) {
            }
        });
        if (getIntent() != null) {
            Intent callIntent = getIntent();
            String szLessonScheduleGUID = callIntent.getStringExtra("scheduleguid");
            String szLessonPrepareResourceGUID = callIntent.getStringExtra(CommentComponent.RESOURCEGUID);
            String szObjectGUID = callIntent.getStringExtra("objectguid");
            String szDisplayName = callIntent.getStringExtra("displayname");
            String szUserClassName = callIntent.getStringExtra("userclassname");
            String szUserClassGUID = callIntent.getStringExtra(UserHonourFragment.USERCLASSGUID);
            if (szUserClassName == null || szUserClassGUID == null) {
                throw new NullPointerException("Must set userclassname and userclassguid in calling intent.");
            }
            if (szDisplayName != null) {
                this.mTitle = szDisplayName;
                getSupportActionBar().setTitle(this.mTitle);
            }
            if (mClientID == null) {
                throw new IllegalArgumentException("ClientID can not be null. Use setClientID method to set it at least once.");
            } else if (szLessonScheduleGUID == null || szLessonPrepareResourceGUID == null) {
                throw new IllegalArgumentException("resourceguid and scheduleguid can not be null. ");
            } else {
                this.mDisplayer.setData(szLessonPrepareResourceGUID, szLessonScheduleGUID, mClientID);
                this.mDisplayer.setUserClassInfo(szUserClassName, szUserClassGUID);
                int nFlags = 0;
                if (callIntent.getBooleanExtra("displayanswers", false)) {
                    nFlags = 0 | 64;
                    this.mbDisplayAnswer = true;
                }
                if (!callIntent.getBooleanExtra("enableunlock", true)) {
                    nFlags |= 32;
                }
                this.mDisplayer.setDisplayOptions(nFlags);
                if (szObjectGUID != null && !szObjectGUID.isEmpty()) {
                    this.mDisplayer.setTargetResourceGUID(szObjectGUID);
                    return;
                }
                return;
            }
        }
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_resourcesview2, menu);
        menu.findItem(R.id.action_save).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_floppy_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_answerfilter).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_filter).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        if (this.mbDisplayAnswer) {
            menu.findItem(R.id.action_save).setVisible(false);
            if (this.mnAnswerFilterIndex == 0) {
                menu.findItem(R.id.action_noneanswer).setChecked(true);
            } else if (this.mnAnswerFilterIndex == 1) {
                menu.findItem(R.id.action_wronganswer).setChecked(true);
            } else if (this.mnAnswerFilterIndex == 2) {
                menu.findItem(R.id.action_allanswer).setChecked(true);
            }
        } else {
            menu.findItem(R.id.action_noneanswer).setVisible(false);
            menu.findItem(R.id.action_wronganswer).setVisible(false);
            menu.findItem(R.id.action_allanswer).setVisible(false);
            menu.findItem(R.id.action_answerfilter).setVisible(false);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            this.mDisplayer.save();
            Toast.makeText(this, "数据已保存到本地，会自动提交到服务器端", 0).show();
        } else if (item.getItemId() == R.id.action_noneanswer) {
            this.mnAnswerFilterIndex = 0;
            this.mDisplayer.showCorrectAnswer(this.mnAnswerFilterIndex);
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.action_wronganswer) {
            this.mnAnswerFilterIndex = 1;
            this.mDisplayer.showCorrectAnswer(this.mnAnswerFilterIndex);
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.action_allanswer) {
            this.mnAnswerFilterIndex = 2;
            this.mDisplayer.showCorrectAnswer(this.mnAnswerFilterIndex);
            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initResourcesList() {
        this.mAdapter = new ResourceListAdapter(this, this.mDisplayer.getResourceItemData());
        this.mListView = (ListView) findViewById(R.id.listViewResource);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setChoiceMode(1);
        this.mListView.setOnItemClickListener(this);
        this.mSpinner = (Spinner) findViewById(R.id.spinner1);
        try {
            this.mSpinner.setAdapter(new ArrayAdapter(this, 17367043, 16908308, new String[]{"全部内容", "仅试题", "仅资源", "仅收藏过的"}));
        } catch (Exception e) {
        }
        this.mSpinner.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 0) {
            this.mAdapter = new ResourceListAdapter(this, this.mDisplayer.getResourceItemData());
            this.mListView.setAdapter(this.mAdapter);
            return;
        }
        ArrayList<ResourceItemData> arrData = new ArrayList();
        for (int i = 0; i < this.mDisplayer.getResourceItemData().size(); i++) {
            ResourceItemData OneData = (ResourceItemData) this.mDisplayer.getResourceItemData().get(i);
            if (position == 1) {
                if (OneData.nType == 0) {
                    arrData.add(OneData);
                }
            } else if (position == 2) {
                if (OneData.nType != 0) {
                    arrData.add(OneData);
                }
            } else if (position == 3 && OneData.bFav) {
                arrData.add(OneData);
            }
        }
        this.mAdapter = new ResourceListAdapter(this, arrData);
        this.mListView.setAdapter(this.mAdapter);
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mDisplayer.jumpToResource(((ResourceItemData) this.mAdapter.getItem(position)).szResourceGUID);
        this.mDrawer.closeDrawers();
    }

    protected void onResume() {
        if (this.mAdapter != null) {
            this.mHandler.removeCallbacks(this.mReloadCorrectResultRunnable);
            this.mHandler.postDelayed(this.mReloadCorrectResultRunnable, 1000);
        }
        QuestionWidgetsUtilities.restoreAllWindow(this);
        super.onResume();
    }

    protected void onPause() {
        QuestionWidgetsUtilities.hideAllWindow(this);
        super.onPause();
    }

    protected void onDestroy() {
        this.mHandler.removeCallbacks(this.mReloadCorrectResultRunnable);
        QuestionWidgetsUtilities.closeAllWindow(this);
        stopService(new Intent(this, AnswerSheetV3MenuService.class));
        stopService(new Intent(this, AnswerSheetV2MenuService.class));
        this.mDisplayer.save();
        super.onDestroy();
    }

    public static void setDataBase(IWmExamDBOpenHelper dataBase) {
        mDataBase = dataBase;
    }

    public static void setClientID(String szClientID) {
        mClientID = szClientID;
    }

    public void onIMMessage(String szFrom, String szMessage) {
        if (szMessage.indexOf("TableDataChanged lessonsscheduleresult") != -1 && this.mAdapter != null) {
            this.mHandler.removeCallbacks(this.mReloadCorrectResultRunnable);
            this.mHandler.postDelayed(this.mReloadCorrectResultRunnable, 1000);
        }
    }
}
