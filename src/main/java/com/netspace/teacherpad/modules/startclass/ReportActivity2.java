package com.netspace.teacherpad.modules.startclass;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.HomeworkCorrectActivity;
import com.netspace.library.adapter.FragmentsPageAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.im.IMService;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.fragments.ReportUserAnswerFragment;
import com.netspace.teacherpad.fragments.ReportUserAnswerStatisticFragment;
import com.netspace.teacherpad.fragments.ReportUserSubmitFragment;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.PicassoTools;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.kxml2.wap.Wbxml;

public class ReportActivity2 extends BaseActivity implements OnTabSelectedListener {
    public static final int[] REPORT_COLORS = new int[]{-16750849, Color.rgb(255, 102, 0), -8604180, -12369080, -548004, -8354327, -959360, -1780908, -13922161, -763045, -7214879};
    public static final int[] REPORT_COLORS_BAR = new int[]{Color.rgb(0, Wbxml.EXT_0, 0), Color.rgb(255, 102, 0), -8604180, -12369080, -548004, -8354327, -959360, -1780908, -13922161, -763045, -7214879};
    public static final int[] REPORT_COLORS_BLUE = new int[]{-16750849};
    private static final String TAG = "ReportActivity2";
    private static int mnAutoSubmitTime = -1;
    private static int mnLastQuestionIndex = -1;
    private final Runnable AutosubmitTimerRunnable = new Runnable() {
        public void run() {
            if (ReportActivity2.mnAutoSubmitTime != -1) {
                long nTimeDiff;
                Date dateTimeNow = new Date();
                if (TeacherPadApplication.bAllowQuestionAnswer) {
                    nTimeDiff = System.currentTimeMillis() - TeacherPadApplication.nQuestionStartTime;
                } else {
                    nTimeDiff = TeacherPadApplication.nQuestionStopTime - TeacherPadApplication.nQuestionStartTime;
                }
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(nTimeDiff);
                int seconds = (int) (diffInSec % 60);
                diffInSec /= 60;
                int minutes = (int) (diffInSec % 60);
                diffInSec /= 60;
                int hours = (int) (diffInSec % 24);
                diffInSec /= 24;
                if (ReportActivity2.mnAutoSubmitTime == -1 || minutes < ReportActivity2.mnAutoSubmitTime) {
                    Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), ReportActivity2.this.AutosubmitTimerRunnable, 1000);
                    return;
                }
                TeacherPadApplication.bAllowQuestionAnswer = false;
                TeacherPadApplication.nQuestionStopTime = System.currentTimeMillis();
                TeacherPadApplication.IMThread.SendAllSubmit();
                ReportActivity2.mnAutoSubmitTime = -1;
                ReportActivity2.this.invalidateOptionsMenu();
                Toast.makeText(MyiBaseApplication.getBaseAppContext(), "已发出停止作答并将未完成作答启动提交", 0).show();
            }
        }
    };
    private final Runnable UpdateTimerRunnable = new Runnable() {
        public void run() {
            if (ReportActivity2.this.mTextViewAnswerTime != null) {
                long nTimeDiff;
                Date dateTimeNow = new Date();
                if (TeacherPadApplication.bAllowQuestionAnswer) {
                    nTimeDiff = System.currentTimeMillis() - TeacherPadApplication.nQuestionStartTime;
                } else {
                    nTimeDiff = TeacherPadApplication.nQuestionStopTime - TeacherPadApplication.nQuestionStartTime;
                }
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(nTimeDiff);
                int seconds = (int) (diffInSec % 60);
                diffInSec /= 60;
                int minutes = (int) (diffInSec % 60);
                diffInSec /= 60;
                int hours = (int) (diffInSec % 24);
                diffInSec /= 24;
                String szResult = String.format(" %02d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
                CharSequence newTitle = "学生答案(已提交" + String.valueOf(TeacherPadApplication.marrStudentAnswers.size()) + "个)";
                if (ReportActivity2.this.mAdapter.setPageTitle(1, newTitle)) {
                    Tab tab = ((TabLayout) ReportActivity2.this.findViewById(R.id.tabs)).getTabAt(1);
                    if (tab != null) {
                        tab.setText(newTitle);
                    }
                }
                if (TeacherPadApplication.bAllowQuestionAnswer) {
                    szResult = new StringBuilder(String.valueOf(szResult)).append("，作答进行中").toString();
                } else {
                    szResult = new StringBuilder(String.valueOf(szResult)).append("，已禁止提交作答").toString();
                }
                if (!ReportActivity2.this.mTextViewAnswerTime.getText().toString().equalsIgnoreCase(szResult)) {
                    ReportActivity2.this.mTextViewAnswerTime.setText(szResult);
                }
            }
            ReportActivity2.this.mUIHandler.postDelayed(ReportActivity2.this.UpdateTimerRunnable, 500);
        }
    };
    private final Runnable checkUserReceivedRunnable = new Runnable() {
        private ArrayList<String> mMissingStudentID = new ArrayList();
        private Snackbar mSnackbar;
        private boolean mbCancelCheck = false;

        public void run() {
            if (!this.mbCancelCheck || this.mSnackbar == null) {
                this.mMissingStudentID.clear();
                String szStudentNames = "";
                synchronized (TeacherPadApplication.mapStudentQuestionReceived) {
                    for (String szStudentID : TeacherPadApplication.mapStudentIDExists.keySet()) {
                        if (!TeacherPadApplication.mapStudentQuestionReceived.containsKey("myipad_" + szStudentID)) {
                            String szRealName = (String) TeacherPadApplication.mapStudentName.get(szStudentID);
                            this.mMissingStudentID.add(szStudentID);
                            if (!szStudentNames.isEmpty()) {
                                szStudentNames = new StringBuilder(String.valueOf(szStudentNames)).append("、").toString();
                            }
                            szStudentNames = new StringBuilder(String.valueOf(szStudentNames)).append(szRealName).toString();
                            TeacherPadApplication.IMThread.SendMessage("QuestionGroup " + TeacherPadApplication.szCurrentQuestionGroupGUID, "myipad_" + szStudentID + ";");
                            IMService.resendMissingMessage("myipad_" + szStudentID, 10);
                        }
                    }
                }
                if (this.mMissingStudentID.size() > 0) {
                    if (this.mSnackbar == null) {
                        this.mSnackbar = Snackbar.make(ReportActivity2.this.mTextViewAnswerTime, (CharSequence) "发现学生没有接收到题目", -2);
                        this.mSnackbar.setAction((CharSequence) "取消重发", new OnClickListener() {
                            public void onClick(View v) {
                                AnonymousClass4.this.mbCancelCheck = true;
                                AnonymousClass4.this.mSnackbar.dismiss();
                            }
                        });
                        this.mSnackbar.show();
                    }
                    if (this.mMissingStudentID.size() < 3) {
                        this.mSnackbar.setText("发现学生" + szStudentNames + "没有接收到题目，已自动重发");
                    } else {
                        this.mSnackbar.setText("发现" + this.mMissingStudentID.size() + "个学生没有接收到题目，已自动重发");
                    }
                    ReportActivity2.this.mUIHandler.postDelayed(ReportActivity2.this.checkUserReceivedRunnable, 1000);
                    return;
                } else if (this.mSnackbar != null && this.mSnackbar.isShown()) {
                    this.mSnackbar.setText((CharSequence) "全部学生都收到题目了");
                    this.mSnackbar.setDuration(0);
                    this.mSnackbar.show();
                    return;
                } else {
                    return;
                }
            }
            this.mSnackbar.dismiss();
        }
    };
    private FragmentsPageAdapter mAdapter;
    private boolean mAppBarExpanded = false;
    private DrawerLayout mDrawerLayout;
    private ImageView mImageViewQuestion;
    private NavigationView mNavigationView;
    private Target mQuestionImageTarget = new Target() {
        public void onBitmapFailed(Drawable arg0) {
            ReportActivity2.this.mImageViewQuestion.setImageResource(R.drawable.ic_placehold);
            ReportActivity2.this.mszLastImageURL = "";
        }

        public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
            ReportActivity2.this.mImageViewQuestion.setImageBitmap(arg0);
        }

        public void onPrepareLoad(Drawable arg0) {
        }
    };
    private String mQuestionMessage;
    private TextView mTextViewAnswerTime;
    private Handler mUIHandler = new Handler();
    private ReportUserAnswerFragment mUserAnswerFragment;
    private ReportUserAnswerStatisticFragment mUserCorrectRateFragment;
    private ReportUserSubmitFragment mUserSubmitFragment;
    private ViewPager mViewPager;
    private boolean mbSideMenuExpanded;
    private String mszLastImageURL = "";

    public abstract class AppBarStateChangeListener implements OnOffsetChangedListener {
        public final int COLLAPSED = 1;
        public final int EXPANDED = 0;
        public final int IDLE = 2;
        private int mCurrentState = 2;

        public abstract void onStateChanged(AppBarLayout appBarLayout, int i);

        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (this.mCurrentState != 0) {
                    onStateChanged(appBarLayout, 0);
                }
                this.mCurrentState = 0;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (this.mCurrentState != 1) {
                    onStateChanged(appBarLayout, 1);
                }
                this.mCurrentState = 1;
            } else {
                if (this.mCurrentState != 2) {
                    onStateChanged(appBarLayout, 2);
                }
                this.mCurrentState = 2;
            }
        }
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    public void updateTitle() {
        ((Toolbar) findViewById(R.id.toolbar)).setTitle("第" + String.valueOf(TeacherPadApplication.ClassMultiQuestions.getCurrentQuestionIndex() + 1) + "题报表");
        setTitle("第" + String.valueOf(TeacherPadApplication.ClassMultiQuestions.getCurrentQuestionIndex() + 1) + "题报表");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_reportview2);
        setTitle("报表");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).colorRes(R.color.actiontoolbartextcolor).actionBarSize());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).colorRes(R.color.actiontoolbartextcolor).actionBarSize());
        updateTitle();
        this.mQuestionMessage = TeacherPadApplication.szCurrentQuestionIMMessage;
        ((CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar)).setTitleEnabled(false);
        this.mImageViewQuestion = (ImageView) findViewById(R.id.imageViewQuestionImage);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbarlayout);
        appBarLayout.setExpanded(false);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            public void onStateChanged(AppBarLayout appBarLayout, int state) {
                if (state == 0 || state == 2) {
                    ReportActivity2.this.mAppBarExpanded = true;
                    ReportActivity2.this.loadImage();
                } else if (state == 1) {
                    ReportActivity2.this.mAppBarExpanded = false;
                    ReportActivity2.this.mImageViewQuestion.setImageDrawable(null);
                    ReportActivity2.this.mszLastImageURL = "";
                }
            }
        });
        this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (VERSION.SDK_INT < 21) {
            this.mDrawerLayout.setDrawerShadow((int) R.drawable.drawer_shadow, 3);
        }
        this.mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        this.mNavigationView.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(MenuItem arg0) {
                TeacherPadApplication.ClassMultiQuestions.processMenuClick(arg0.getItemId());
                ReportActivity2.mnLastQuestionIndex = arg0.getItemId();
                ReportActivity2.this.mDrawerLayout.closeDrawers();
                return true;
            }
        });
        this.mDrawerLayout.setDrawerListener(new DrawerListener() {
            public void onDrawerClosed(View arg0) {
                ReportActivity2.this.mbSideMenuExpanded = false;
            }

            public void onDrawerOpened(View arg0) {
                ReportActivity2.this.mbSideMenuExpanded = true;
            }

            public void onDrawerSlide(View arg0, float arg1) {
                loadDrawerData();
            }

            public void onDrawerStateChanged(int arg0) {
                if (arg0 == 1) {
                    loadDrawerData();
                }
            }

            private void loadDrawerData() {
                ReportActivity2.this.updateMenu();
                TextView textViewTitle = (TextView) ReportActivity2.this.findViewById(R.id.textViewClassName);
                if (!(textViewTitle == null || TeacherPadApplication.szScheduleResourceTitle == null)) {
                    textViewTitle.setText(TeacherPadApplication.szScheduleResourceTitle);
                }
                TextView textViewStudentInfo = (TextView) ReportActivity2.this.findViewById(R.id.textViewStudentInfo);
                if (textViewStudentInfo != null) {
                    textViewStudentInfo.setText("当前共发了" + String.valueOf(TeacherPadApplication.ClassMultiQuestions.getSize()) + "个题目");
                }
            }
        });
        if (VERSION.SDK_INT >= 21 && findViewById(R.id.shadow) != null) {
            findViewById(R.id.shadow).setVisibility(8);
        }
        setDoublePressReturn(true);
        this.mTextViewAnswerTime = (TextView) findViewById(R.id.textViewAnswerTimeCount);
        this.mAdapter = new FragmentsPageAdapter(getSupportFragmentManager());
        this.mUserSubmitFragment = new ReportUserSubmitFragment();
        this.mUserCorrectRateFragment = new ReportUserAnswerStatisticFragment();
        this.mUserAnswerFragment = new ReportUserAnswerFragment();
        this.mAdapter.addPage(this.mUserSubmitFragment, "学生提交情况");
        this.mAdapter.addPage(this.mUserAnswerFragment, "学生答案");
        this.mAdapter.addPage(this.mUserCorrectRateFragment, "客观题回答情况");
        this.mViewPager = (ViewPager) findViewById(R.id.pager);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOffscreenPageLimit(6);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(this.mViewPager);
        tabLayout.setOnTabSelectedListener(this);
        this.mUIHandler.postDelayed(this.checkUserReceivedRunnable, 2000);
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(0).toString());
    }

    private void loadImage() {
        String szURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + TeacherPadApplication.szCurrentQuestionGroupGUID;
        if (!this.mszLastImageURL.equalsIgnoreCase(szURL)) {
            this.mszLastImageURL = szURL;
            Picasso.with(this).load(szURL).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(this.mQuestionImageTarget);
        }
    }

    private void updateAutoStopMenu(Menu menu) {
        menu.findItem(R.id.action_autostop_none).setChecked(false);
        menu.findItem(R.id.action_autostop_1).setChecked(false);
        menu.findItem(R.id.action_autostop_3).setChecked(false);
        menu.findItem(R.id.action_autostop_5).setChecked(false);
        menu.findItem(R.id.action_autostop_10).setChecked(false);
        menu.findItem(R.id.action_autostop_15).setChecked(false);
        if (mnAutoSubmitTime == -1) {
            menu.findItem(R.id.action_autostop_none).setChecked(true);
        } else if (mnAutoSubmitTime == 1) {
            menu.findItem(R.id.action_autostop_1).setChecked(true);
        } else if (mnAutoSubmitTime == 3) {
            menu.findItem(R.id.action_autostop_3).setChecked(true);
        } else if (mnAutoSubmitTime == 5) {
            menu.findItem(R.id.action_autostop_5).setChecked(true);
        } else if (mnAutoSubmitTime == 10) {
            menu.findItem(R.id.action_autostop_10).setChecked(true);
        } else if (mnAutoSubmitTime == 15) {
            menu.findItem(R.id.action_autostop_15).setChecked(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Utilities.logMenuClick(menuItem);
        if (menuItem.getItemId() == 16908332) {
            this.mDrawerLayout.openDrawer(3);
        } else if (menuItem.getItemId() == R.id.action_autostop_none) {
            mnAutoSubmitTime = -1;
            invalidateOptionsMenu();
        } else if (menuItem.getItemId() == R.id.action_autostop_1) {
            mnAutoSubmitTime = 1;
            invalidateOptionsMenu();
            Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), this.AutosubmitTimerRunnable, 1000);
        } else if (menuItem.getItemId() == R.id.action_autostop_3) {
            mnAutoSubmitTime = 3;
            invalidateOptionsMenu();
            Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), this.AutosubmitTimerRunnable, 1000);
        } else if (menuItem.getItemId() == R.id.action_autostop_5) {
            mnAutoSubmitTime = 5;
            invalidateOptionsMenu();
            Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), this.AutosubmitTimerRunnable, 1000);
        } else if (menuItem.getItemId() == R.id.action_autostop_10) {
            mnAutoSubmitTime = 10;
            invalidateOptionsMenu();
            Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), this.AutosubmitTimerRunnable, 1000);
        } else if (menuItem.getItemId() == R.id.action_autostop_15) {
            mnAutoSubmitTime = 15;
            invalidateOptionsMenu();
            Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), this.AutosubmitTimerRunnable, 1000);
        } else if (menuItem.getItemId() == R.id.action_airplay) {
            TeacherPadApplication.projectToMonitor();
        } else if (menuItem.getItemId() == R.id.action_stop) {
            Utilities.showAlertMessage(this, "强制提交", "点击“是”后将强制所有学生提交当前作答并停止所有后续题目的作答，是否继续？", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    TeacherPadApplication.bAllowQuestionAnswer = false;
                    TeacherPadApplication.nQuestionStopTime = System.currentTimeMillis();
                    TeacherPadApplication.IMThread.SendAllSubmit();
                    ReportActivity2.mnAutoSubmitTime = -1;
                    ReportActivity2.this.invalidateOptionsMenu();
                    Toast.makeText(ReportActivity2.this, "已发出停止作答并将未完成作答启动提交", 0).show();
                }
            }, null);
        } else if (menuItem.getItemId() == R.id.action_correct && !this.mQuestionMessage.isEmpty()) {
            String[] arrParams = this.mQuestionMessage.split(" ");
            String szScheduleGUID = arrParams[2];
            String szScheduleResourceGUID = arrParams[3];
            String szResourceGUID = arrParams[4];
            Intent Intent = new Intent(this, HomeworkCorrectActivity.class);
            Intent.putExtra(CommentComponent.RESOURCEGUID, szScheduleResourceGUID);
            Intent.putExtra("scheduleguid", szScheduleGUID);
            Intent.putExtra(UserHonourFragment.USERCLASSGUID, TeacherPadApplication.szCurrentClassGUID);
            Intent.putExtra("objectguid", szResourceGUID);
            Intent.putExtra("serveraddress", MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress);
            startActivity(Intent);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void updateMenu() {
        TeacherPadApplication.ClassMultiQuestions.updateSideMenu(this.mNavigationView.getMenu());
    }

    public void update() {
        if (this.mAppBarExpanded) {
            loadImage();
        }
        TeacherPadApplication.ClassMultiQuestions.updateSideMenu(this.mNavigationView.getMenu());
        this.mUserSubmitFragment.refresh();
        this.mUserCorrectRateFragment.refresh();
        this.mUserAnswerFragment.refresh();
        updateTitle();
        mnLastQuestionIndex = TeacherPadApplication.ClassMultiQuestions.getCurrentQuestionIndex();
    }

    private boolean isAnswerSheetQuestion() {
        if (this.mQuestionMessage.isEmpty() || this.mQuestionMessage.indexOf("AnswerSheetQuestion") == -1) {
            return false;
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reportactivity2, menu);
        menu.findItem(R.id.action_airplay).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_tv).colorRes(R.color.actiontoolbartextcolor).actionBarSize());
        menu.findItem(R.id.action_stop).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_ban).colorRes(R.color.actiontoolbartextcolor).actionBarSize());
        menu.findItem(R.id.action_correct).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_pencil).colorRes(R.color.actiontoolbartextcolor).actionBarSize());
        menu.findItem(R.id.action_autostop).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_clock_o).colorRes(R.color.actiontoolbartextcolor).actionBarSize());
        updateAutoStopMenu(menu);
        if (!isAnswerSheetQuestion()) {
            menu.findItem(R.id.action_correct).setVisible(false);
        }
        return true;
    }

    public void onTabReselected(Tab arg0) {
    }

    public void refreshStudentAnswer() {
        this.mUserAnswerFragment.refresh();
    }

    public void onTabSelected(Tab arg0) {
        this.mViewPager.setCurrentItem(arg0.getPosition());
        Utilities.logClick(this.mViewPager, this.mAdapter.getPageTitle(arg0.getPosition()).toString());
    }

    public void onTabUnselected(Tab arg0) {
    }

    public void onIMMessage(String szFrom, String szMessage) {
        if (szMessage.indexOf("VResult") != -1 || szMessage.indexOf("VAutoResult") != -1) {
            TeacherPadApplication.ClassMultiQuestions.refreshCurrentQuestion();
            this.mUserSubmitFragment.refresh();
            this.mUserCorrectRateFragment.refresh();
            this.mUserAnswerFragment.refresh();
        }
    }

    protected void onResume() {
        this.mUIHandler.postDelayed(this.UpdateTimerRunnable, 500);
        super.onResume();
        this.mUserSubmitFragment.refresh();
        this.mUserCorrectRateFragment.refresh();
        this.mUserAnswerFragment.refresh();
    }

    protected void onDestroy() {
        this.mUIHandler.removeCallbacks(this.checkUserReceivedRunnable);
        Utilities.unbindDrawables(findViewById(R.id.pager));
        super.onDestroy();
    }

    protected void onPause() {
        this.mUIHandler.removeCallbacks(this.UpdateTimerRunnable);
        PicassoTools.clearCache(Picasso.with(this));
        super.onPause();
    }
}
