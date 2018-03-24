package com.netspace.library.activity;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.NovaIcons;
import com.netspace.library.adapter.ResourceListAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.components.LessonPrepareCorrectAnswerSheetComponent;
import com.netspace.library.components.LessonPrepareCorrectAnswerSheetV3Component;
import com.netspace.library.components.LessonPrepareCorrectComponent;
import com.netspace.library.components.LessonPrepareCorrectComponent.DataChangeCallBack;
import com.netspace.library.components.LessonPrepareCorrectComponent.LoadStudentAllAnswerCallBack;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.im.IMService;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;

public class HomeworkCorrectActivity extends BaseActivity implements OnItemSelectedListener, OnItemClickListener {
    private static final int USERS_JUMP_MENU_STARTID = 2000;
    private static final int USERS_MENU_STARTID = 1000;
    protected static IWmExamDBOpenHelper mDataBase;
    private ResourceListAdapter mAdapter;
    private String mClientID;
    private LinearLayout mContentLayout;
    private int mCurrentPos = 0;
    private LessonPrepareCorrectAnswerSheetComponent mDisplayer;
    private DrawerLayout mDrawer;
    private String mLessonPrepareResourceGUID;
    private String mLessonScheduleGUID;
    private ListView mListView;
    private LockableScrollView mScrollView;
    private String mServerAddress;
    private Spinner mSpinner;
    private String mStudentName;
    private TextView mTextViewUnsavedCount;
    private ArrayList<String> marrClientIDs;
    private boolean mbFirstCall = false;
    private boolean mbSkipSave = false;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_homeworkcorrect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.mContentLayout = (LinearLayout) findViewById(R.id.layoutContent);
        this.mDisplayer = new LessonPrepareCorrectAnswerSheetV3Component(this);
        this.mContentLayout.addView(this.mDisplayer, new LayoutParams(-1, -1));
        this.mScrollView = this.mDisplayer.getScrollView();
        this.mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        this.mDrawer.setDrawerShadow(R.drawable.drawer_shadow, 8388611);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, this.mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerStateChanged(int newState) {
                if (newState == 1 && !HomeworkCorrectActivity.this.mDrawer.isDrawerOpen(3) && HomeworkCorrectActivity.this.mAdapter != null) {
                    HomeworkCorrectActivity.this.mAdapter.notifyDataSetChanged();
                }
            }
        };
        this.mDrawer.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        this.mDisplayer.setCallBack(new ComponentCallBack() {
            public void OnDataLoaded(String szFileName, IComponents Component) {
                CharSequence szTitle = HomeworkCorrectActivity.this.mDisplayer.getTitle();
                if (HomeworkCorrectActivity.this.mStudentName != null) {
                    szTitle = new StringBuilder(String.valueOf(szTitle)).append(" ").append(HomeworkCorrectActivity.this.mStudentName).append("的作答").toString();
                }
                HomeworkCorrectActivity.this.getSupportActionBar().setTitle(szTitle);
                if (!HomeworkCorrectActivity.this.mbFirstCall) {
                    HomeworkCorrectActivity.this.initResourcesList();
                    HomeworkCorrectActivity.this.mbFirstCall = true;
                }
                HomeworkCorrectActivity.this.mListView.setItemChecked(HomeworkCorrectActivity.this.mDisplayer.getDisplayIndex(), true);
                HomeworkCorrectActivity.this.invalidateOptionsMenu();
            }

            public void OnDataUploaded(String szData, IComponents Component) {
            }

            public void OnRequestIntent(Intent intent, IComponents Component) {
            }
        });
        this.mDisplayer.setDataChangeCallBack(new DataChangeCallBack() {
            public void OnDataChanged() {
                if (HomeworkCorrectActivity.this.mAdapter != null) {
                    HomeworkCorrectActivity.this.mAdapter.notifyDataSetChanged();
                }
            }
        });
        if (getIntent() != null) {
            this.mLessonScheduleGUID = getIntent().getStringExtra("scheduleguid");
            this.mLessonPrepareResourceGUID = getIntent().getStringExtra(CommentComponent.RESOURCEGUID);
            this.mServerAddress = getIntent().getStringExtra("serveraddress");
            this.mClientID = getIntent().getStringExtra(DeviceOperationRESTServiceProvider.CLIENTID);
            this.mStudentName = getIntent().getStringExtra("studentname");
            String szObjectGUID = getIntent().getStringExtra("objectguid");
            if (this.mLessonScheduleGUID == null || this.mLessonPrepareResourceGUID == null) {
                throw new IllegalArgumentException("resourceguid and scheduleguid can not be null. ");
            }
            if (this.mClientID != null) {
                this.mDisplayer.setLimitClientID(this.mClientID);
                this.mDisplayer.setDisplayOptions((this.mDisplayer.getDisplayOptions() | LessonPrepareCorrectAnswerSheetComponent.DISPLAY_OPTIONS_LOAD_ALL) | LessonPrepareCorrectAnswerSheetComponent.DISPLAY_OPTIONS_HIDE_STUDENTTITLE);
            }
            this.mDisplayer.setData(this.mLessonPrepareResourceGUID, this.mLessonScheduleGUID);
            this.mDisplayer.setLoadStudentAllAnswerCallBack(new LoadStudentAllAnswerCallBack() {
                public void OnLoadStudentAllAnswer(String szClientID, String szStudentName) {
                    Intent intent = new Intent(HomeworkCorrectActivity.this, HomeworkCorrectActivity.class);
                    intent.putExtra("scheduleguid", HomeworkCorrectActivity.this.mLessonScheduleGUID);
                    intent.putExtra(CommentComponent.RESOURCEGUID, HomeworkCorrectActivity.this.mLessonPrepareResourceGUID);
                    intent.putExtra("serveraddress", HomeworkCorrectActivity.this.mServerAddress);
                    intent.putExtra(DeviceOperationRESTServiceProvider.CLIENTID, szClientID);
                    intent.putExtra("studentname", szStudentName);
                    HomeworkCorrectActivity.this.startActivity(intent);
                }
            });
            return;
        }
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        SubMenu userMenu;
        int i;
        String szClientID;
        MenuInflater inflater = getMenuInflater();
        if (this.mDisplayer.isAnswerSheetMode()) {
            inflater.inflate(R.menu.menu_homeworkcorrect_answersheet, menu);
        } else {
            inflater.inflate(R.menu.menu_homeworkcorrect, menu);
        }
        if (this.mClientID != null) {
            menu.findItem(R.id.action_userfilter).setVisible(false);
            menu.findItem(R.id.action_report).setVisible(false);
            menu.findItem(R.id.action_autocorrect).setVisible(false);
        }
        if (menu.findItem(R.id.action_opensource) != null) {
            menu.findItem(R.id.action_opensource).setIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_folder_open_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        }
        if (menu.findItem(R.id.action_save) != null) {
            final MenuItem saveMenu = menu.findItem(R.id.action_save);
            saveMenu.setIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_save).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            View unSavedNotificaitonsView = saveMenu.getActionView();
            if (unSavedNotificaitonsView != null) {
                ImageView imageView = (ImageView) unSavedNotificaitonsView.findViewById(R.id.imageViewSave);
                if (imageView != null) {
                    imageView.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_save).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
                    imageView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            HomeworkCorrectActivity.this.onOptionsItemSelected(saveMenu);
                        }
                    });
                }
                this.mTextViewUnsavedCount = (TextView) unSavedNotificaitonsView.findViewById(R.id.textViewCount);
                if (this.mTextViewUnsavedCount != null) {
                    if (this.mDisplayer.getUnsavedCount() == 0) {
                        this.mTextViewUnsavedCount.setVisibility(8);
                    } else {
                        this.mTextViewUnsavedCount.setVisibility(0);
                        this.mTextViewUnsavedCount.setText(String.valueOf(this.mDisplayer.getUnsavedCount()));
                    }
                }
            }
        }
        if (menu.findItem(R.id.action_refresh) != null) {
            menu.findItem(R.id.action_refresh).setIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_refresh).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        }
        if (menu.findItem(R.id.action_stopsubmit) != null) {
            menu.findItem(R.id.action_stopsubmit).setIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_ban).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        }
        menu.findItem(R.id.action_userfilter).setIcon(new IconDrawable((Context) this, (Icon) NovaIcons.nova_icon_content_filter).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_userjump).setIcon(new IconDrawable((Context) this, (Icon) NovaIcons.nova_icon_user_group_view).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_report).setIcon(new IconDrawable((Context) this, (Icon) NovaIcons.nova_icon_business_graph_line_2).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_airplay).setIcon(new IconDrawable((Context) this, (Icon) NovaIcons.nova_icon_projector_screen).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_autocorrect).setIcon(new IconDrawable((Context) this, (Icon) NovaIcons.nova_icon_spelling_check_1).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_prev).setIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_arrow_left).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        menu.findItem(R.id.action_next).setIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_arrow_right).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        if ((this.mDisplayer.getDisplayOptions() & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT) {
            menu.findItem(R.id.action_displayuncorrect).setChecked(true);
        } else {
            if ((this.mDisplayer.getDisplayOptions() & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER) {
                menu.findItem(R.id.action_select_user).setChecked(true);
            } else {
                if ((this.mDisplayer.getDisplayOptions() & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY) {
                    menu.findItem(R.id.action_displaycorrect).setChecked(true);
                } else {
                    if ((this.mDisplayer.getDisplayOptions() & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY) {
                        menu.findItem(R.id.action_displaywrong).setChecked(true);
                    } else {
                        menu.findItem(R.id.action_all_user).setChecked(true);
                    }
                }
            }
        }
        MenuItem menuUserFilter = menu.findItem(R.id.action_userfilter);
        if (menuUserFilter != null) {
            userMenu = menuUserFilter.getSubMenu();
            i = 0;
            this.marrClientIDs = new ArrayList();
            for (Entry<String, String> entry : this.mDisplayer.getAnsweredStudentsInfo().entrySet()) {
                szClientID = (String) entry.getKey();
                int i2 = i + USERS_MENU_STARTID;
                MenuItem newMenu = userMenu.add(R.id.group_users, i2, 0, (String) entry.getValue());
                newMenu.setCheckable(true);
                if (Utilities.isInArray(this.mDisplayer.getFilterStudentsClientID(), szClientID)) {
                    newMenu.setChecked(true);
                }
                this.marrClientIDs.add(szClientID);
                i++;
            }
            userMenu.setGroupCheckable(R.id.group_users, true, false);
        }
        MenuItem menuUserJump = menu.findItem(R.id.action_userjump);
        if (menuUserJump != null) {
            userMenu = menuUserJump.getSubMenu();
            i = 0;
            for (Entry<String, String> entry2 : this.mDisplayer.getAnsweredStudentsInfo().entrySet()) {
                szClientID = (String) entry2.getKey();
                String szName = (String) entry2.getValue();
                boolean bAdd = false;
                if (this.mDisplayer.getFilterStudentsClientID().size() == 0) {
                    bAdd = true;
                } else if (Utilities.isInArray(this.mDisplayer.getFilterStudentsClientID(), szClientID)) {
                    bAdd = true;
                }
                if (bAdd) {
                    userMenu.add(R.id.group_users, i + USERS_JUMP_MENU_STARTID, 0, szName);
                }
                i++;
            }
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Utilities.logMenuClick(item);
        if (item.getItemId() == R.id.action_report) {
            initReportContent(this.mServerAddress);
        } else if (item.getItemId() == R.id.action_opensource) {
            this.mDisplayer.openSource();
        } else if (item.getItemId() == R.id.action_save) {
            this.mDisplayer.save();
        } else if (item.getItemId() == R.id.action_refresh) {
            this.mDisplayer.reloadCorrectResult();
        } else if (item.getItemId() == R.id.action_airplay) {
            if (UI.mOnAirplayButton != null) {
                UI.mOnAirplayButton.onClick(null, 0);
            }
        } else if (item.getItemId() == R.id.action_stopsubmit) {
            if (this.mDisplayer.isAnswerSheetMode()) {
                this.mDisplayer.save();
            }
            IMService.getIMService().sendMessage(Utilities.getNow() + " " + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + ": " + ("StopSubmit " + this.mLessonScheduleGUID), "*");
            Toast.makeText(this, "停止作答消息已发出", 0).show();
        } else if (item.getItemId() == R.id.action_next) {
            if (this.mCurrentPos < this.mAdapter.getCount() - 1) {
                this.mCurrentPos++;
                onItemClick(this.mListView, this.mListView, this.mCurrentPos, (long) this.mCurrentPos);
            } else {
                Utilities.showAlertMessage(this, "没有下一题", "当前已经是最后一题了。");
            }
        } else if (item.getItemId() == R.id.action_prev) {
            if (this.mCurrentPos > 0) {
                this.mCurrentPos--;
                onItemClick(this.mListView, this.mListView, this.mCurrentPos, (long) this.mCurrentPos);
            } else {
                Utilities.showAlertMessage(this, "没有上一题", "当前已经是第一题了。");
            }
        } else if (item.getItemId() == R.id.action_autocorrect) {
            if (this.mDisplayer.isAnswerSheetMode()) {
                Utilities.showAlertMessage(this, "自动批改", "当前此功能不能在答题卡上使用，答题卡的客观题如果有答案的已经被自动批改了。");
                return false;
            }
            new Builder(this).setTitle("自动批改").setMessage("程序将自动批改所有作答了的客观题题目，如果您手动批改过这些题目，您的批改结果将被覆盖。所有手写痕迹都不会受到影响。\n是否继续？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("LessonsScheduleAutoCorrect", HomeworkCorrectActivity.this);
                    ItemObject.setSuccessListener(new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            HomeworkCorrectActivity.this.mDisplayer.reloadCorrectResult();
                            new Builder(HomeworkCorrectActivity.this).setTitle("批改完成").setMessage("服务器端已完成客观题的自动批改。当前结果已更新。").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setCancelable(false).show();
                        }
                    });
                    ItemObject.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                            new Builder(HomeworkCorrectActivity.this).setTitle("批改出现错误").setMessage("服务器端批改时出现错误，错误代码：" + nReturnCode).setPositiveButton("确定", null).show();
                        }
                    });
                    ItemObject.setParam("lpszScheduleGUID", HomeworkCorrectActivity.this.mLessonScheduleGUID);
                    VirtualNetworkObject.addToQueue(ItemObject);
                }
            }).setNegativeButton("否", null).show();
        } else if (item.getItemId() == R.id.action_all_user) {
            this.mDisplayer.setDisplayOptions(this.mDisplayer.getDisplayOptions() & ((LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER | LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT) ^ -1));
            this.mDisplayer.save();
            this.mDisplayer.clear();
            this.mDisplayer.displayContent(this.mDisplayer.getDisplayIndex());
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.action_select_user) {
            this.mDisplayer.setDisplayOptions((((this.mDisplayer.getDisplayOptions() | LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT ^ -1)) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY ^ -1)) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY ^ -1));
            this.mDisplayer.save();
            this.mDisplayer.clear();
            this.mDisplayer.displayContent(this.mDisplayer.getDisplayIndex());
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.action_displayuncorrect) {
            this.mDisplayer.setDisplayOptions((((this.mDisplayer.getDisplayOptions() & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY ^ -1)) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY ^ -1)) | LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER ^ -1));
            this.mDisplayer.save();
            this.mDisplayer.clear();
            this.mDisplayer.displayContent(this.mDisplayer.getDisplayIndex());
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.action_displaycorrect) {
            this.mDisplayer.setDisplayOptions((((this.mDisplayer.getDisplayOptions() | LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY ^ -1)) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT ^ -1)) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER ^ -1));
            this.mDisplayer.save();
            this.mDisplayer.clear();
            this.mDisplayer.displayContent(this.mDisplayer.getDisplayIndex());
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.action_displaywrong) {
            this.mDisplayer.setDisplayOptions((((this.mDisplayer.getDisplayOptions() & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY ^ -1)) | LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT ^ -1)) & (LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER ^ -1));
            this.mDisplayer.save();
            this.mDisplayer.clear();
            this.mDisplayer.displayContent(this.mDisplayer.getDisplayIndex());
            invalidateOptionsMenu();
        } else if (item.getItemId() >= USERS_MENU_STARTID && item.getItemId() < USERS_JUMP_MENU_STARTID) {
            this.mDisplayer.setDisplayOptions(this.mDisplayer.getDisplayOptions() | LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER);
            String szClientID = (String) this.marrClientIDs.get(item.getItemId() - 1000);
            Integer[] nIndex = new Integer[]{Integer.valueOf(0)};
            if (Utilities.isInArray(this.mDisplayer.getFilterStudentsClientID(), szClientID, nIndex)) {
                this.mDisplayer.getFilterStudentsClientID().remove(nIndex[0].intValue());
            } else {
                this.mDisplayer.getFilterStudentsClientID().add(szClientID);
            }
            invalidateOptionsMenu();
            this.mDisplayer.save();
            this.mDisplayer.clear();
            this.mDisplayer.displayContent(this.mDisplayer.getDisplayIndex());
        } else if (item.getItemId() >= USERS_JUMP_MENU_STARTID && item.getItemId() < 3000) {
            this.mDisplayer.jumpToClientID((String) this.marrClientIDs.get(item.getItemId() - 2000));
        }
        return super.onOptionsItemSelected(item);
    }

    private void initReportContent(String szServerAddress) {
        HttpItemObject ItemObject = new HttpItemObject(MyiBaseApplication.getProtocol() + "://" + szServerAddress + "/script", this);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    Intent WebBrowserIntent = new Intent(HomeworkCorrectActivity.this, ReportWebBrowserActivity.class);
                    WebBrowserIntent.setFlags(67108864);
                    WebBrowserIntent.putExtra("title", new StringBuilder(String.valueOf(HomeworkCorrectActivity.this.mDisplayer.getReportTitle())).append("作答情况报表").toString());
                    ReportWebBrowserActivity.setContent(ItemObject.readTextData());
                    HomeworkCorrectActivity.this.startActivity(WebBrowserIntent);
                    return;
                }
                new Builder(HomeworkCorrectActivity.this).setTitle("报表").setMessage("获取数据时出现错误，没有返回数据。").setPositiveButton("确定", null).show();
            }
        });
        String szTemplateFileName = this.mDisplayer.getReportTemplateName();
        if (szTemplateFileName == null || szTemplateFileName.isEmpty()) {
            szTemplateFileName = "reportTemplate.jsp";
        }
        String szFileContent = Utilities.readTextFileFromAssertPackage(this, szTemplateFileName);
        try {
            ArrayList<ResourceItemData> arrData;
            JSONObject jsonObject = new JSONObject();
            JSONArray array = new JSONArray();
            if (this.mDisplayer.isAnswerSheetMode()) {
                arrData = this.mDisplayer.getReportResourceItemData();
            } else {
                arrData = this.mDisplayer.getResourceItemData();
            }
            Iterator it = arrData.iterator();
            while (it.hasNext()) {
                ResourceItemData Data = (ResourceItemData) it.next();
                JSONObject JsonItemData = new JSONObject();
                if (!Data.szResourceGUID.equalsIgnoreCase("Report")) {
                    JsonItemData.put(CommentComponent.RESOURCEGUID, Data.szResourceGUID);
                    JsonItemData.put("title", Data.szTitle);
                    JsonItemData.put("type", Data.nType);
                    if (this.mDisplayer.isAnswerSheetMode()) {
                        JsonItemData.put("usagetype", Data.nUsageType);
                        JsonItemData.put("answersheetmode", true);
                    }
                    array.put(JsonItemData);
                }
            }
            jsonObject.put("data", array);
            jsonObject.put("scheduleguid", this.mLessonScheduleGUID);
            szFileContent = szFileContent.replace("%JSONDATA%", jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ItemObject.writeTextData(szFileContent);
        ItemObject.setReadOperation(false);
        ItemObject.setTimeout(600000);
        VirtualNetworkObject.addToQueue(ItemObject);
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

    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
        ResourceItemData itemData = (ResourceItemData) this.mAdapter.getItem(position);
        this.mCurrentPos = position;
        if (this.mClientID != null) {
            this.mDisplayer.jumpToResource(itemData.szResourceGUID);
            return;
        }
        if (this.mDisplayer.isAnswerSheetMode()) {
            new Builder(this).setTitle("是否保存当前批改").setCancelable(true).setMessage("是否保存当前答题卡的批改？选择“是”后学生端就会看到批改结果。").setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    HomeworkCorrectActivity.this.mDisplayer.save();
                    HomeworkCorrectActivity.this.mDisplayer.clear();
                    HomeworkCorrectActivity.this.mDisplayer.displayContent(position);
                    HomeworkCorrectActivity.this.invalidateOptionsMenu();
                }
            }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    HomeworkCorrectActivity.this.mDisplayer.clear();
                    HomeworkCorrectActivity.this.mDisplayer.displayContent(position);
                    HomeworkCorrectActivity.this.invalidateOptionsMenu();
                }
            }).show();
        } else {
            this.mDisplayer.save();
            this.mDisplayer.clear();
            this.mDisplayer.displayContent(position);
            invalidateOptionsMenu();
        }
        this.mDrawer.closeDrawers();
    }

    protected void onResume() {
        QuestionWidgetsUtilities.restoreAllWindow(this);
        super.onResume();
    }

    protected void onPause() {
        QuestionWidgetsUtilities.hideAllWindow(this);
        super.onPause();
    }

    protected void onDestroy() {
        QuestionWidgetsUtilities.closeAllWindow(this);
        if (!this.mbSkipSave) {
            this.mDisplayer.save();
        }
        super.onDestroy();
    }

    public static void setDataBase(IWmExamDBOpenHelper dataBase) {
        mDataBase = dataBase;
    }

    public void onBackPressed() {
        if (this.mDrawer.isDrawerOpen(3)) {
            this.mDrawer.closeDrawers();
        } else if (this.mDisplayer.isAnswerSheetMode()) {
            new Builder(this).setTitle("是否保存当前批改").setCancelable(true).setMessage("是否保存当前答题卡的批改？选择“是”后学生端就会看到批改结果。").setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    HomeworkCorrectActivity.this.finish();
                }
            }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    HomeworkCorrectActivity.this.mbSkipSave = true;
                    HomeworkCorrectActivity.this.finish();
                }
            }).show();
        } else if (this.mDisplayer.checkUnCorrectAnswer(true)) {
            new Builder(this).setTitle("有学生作业尚未批改").setCancelable(true).setMessage("发现有学生作业尚未批改，是否继续退出？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    HomeworkCorrectActivity.this.finish();
                }
            }).setNegativeButton("否", null).show();
        } else {
            super.onBackPressed();
        }
    }
}
