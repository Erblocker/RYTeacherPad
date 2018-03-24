package com.netspace.teacherpad.theads;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.NovaIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.servers.MP3RecordThread;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.ScreenDisplayActivity;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import com.netspace.teacherpad.structure.MultiScreen;
import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuItem;
import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuItem.RadialMenuItemClickListener;
import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuWidget;
import eu.janmuller.android.simplecropimage.CropImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.opencamera.MainActivity;

public class ClassOnScreenControlsService extends Service implements OnClickListener {
    private static final int ATTACH_MARGIN_LEFT = 50;
    private static final int ATTACH_MARGIN_RIGHT = 120;
    private static final int HIDE_WINDOW = 300;
    public static final String OPERATION = "operation";
    public static final int OPERATION_HIDE = 101;
    public static final int OPERATION_SHOW = 100;
    private static final int SHOW_WINDOW = 200;
    private static final String TAG = "ClassOnScreenControlsService";
    private static LayoutParams mParams;
    private static WindowManager mWindowManager;
    private boolean mAdded = false;
    private LinearLayout mButtonsView;
    private Runnable mCheckCurrentActivityRunnable = new Runnable() {
        public void run() {
            Activity activity = UI.getCurrentActivity();
            if (activity != null) {
                if (activity instanceof MainActivity) {
                    if (ClassOnScreenControlsService.this.mAdded) {
                        ClassOnScreenControlsService.this.mHandler.sendEmptyMessage(300);
                    }
                    ClassOnScreenControlsService.this.mHandler.postDelayed(this, 1000);
                    return;
                } else if (activity instanceof CropImage) {
                    if (ClassOnScreenControlsService.this.mAdded) {
                        ClassOnScreenControlsService.this.mHandler.sendEmptyMessage(300);
                    }
                    ClassOnScreenControlsService.this.mHandler.postDelayed(this, 1000);
                    return;
                } else if (ClassOnScreenControlsService.this.mAdded) {
                    ClassOnScreenControlsService.this.updateButtonDisplay();
                } else {
                    ClassOnScreenControlsService.this.mHandler.sendEmptyMessage(200);
                }
            } else if (ClassOnScreenControlsService.this.mAdded) {
                ClassOnScreenControlsService.this.mHandler.sendEmptyMessage(300);
            }
            ClassOnScreenControlsService.this.mHandler.postDelayed(this, 1000);
        }
    };
    private IconDrawable mEraseDrawable;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (!ClassOnScreenControlsService.this.mAdded) {
                        ClassOnScreenControlsService.this.mMoreButton.setVisibility(0);
                        if (ClassOnScreenControlsService.this.mbAllShow) {
                            ClassOnScreenControlsService.this.mPieMenu.dismiss();
                            ClassOnScreenControlsService.this.mbAllShow = false;
                        }
                        ClassOnScreenControlsService.mWindowManager.addView(ClassOnScreenControlsService.this.mRootView, ClassOnScreenControlsService.mParams);
                        ClassOnScreenControlsService.this.mAdded = true;
                    }
                    ClassOnScreenControlsService.this.updateButtonsOnPlayState();
                    return;
                case 300:
                    if (ClassOnScreenControlsService.this.mAdded) {
                        ClassOnScreenControlsService.mWindowManager.removeView(ClassOnScreenControlsService.this.mRootView);
                        ClassOnScreenControlsService.this.mAdded = false;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private LayoutInflater mInflater;
    private boolean mItemMove = false;
    private ImageButton mMicButton;
    private ImageButton mMoreButton;
    private IconDrawable mMouseDrawable;
    private LinearLayout mParentButtonsView;
    private IconDrawable mPencialDrawable;
    private RadialMenuWidget mPieMenu;
    private View mRootView;
    private int mScreenWidth = 0;
    private int mVisibleButtonCount = 0;
    private String[] marrBlockedButtons;
    private ArrayList<String> marrVisibleButtons = new ArrayList();
    private boolean mbAllButtonsShow = false;
    private boolean mbAllShow = false;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        this.marrBlockedButtons = getResources().getStringArray(R.array.blockedButtons);
        createFloatView();
    }

    public void onDestroy() {
        this.mHandler.removeCallbacks(this.mCheckCurrentActivityRunnable);
        this.mHandler.removeMessages(200);
        this.mHandler.sendEmptyMessage(300);
        super.onDestroy();
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (intent != null) {
            switch (intent.getIntExtra("operation", 100)) {
                case 100:
                    this.mHandler.sendEmptyMessage(200);
                    return;
                case 101:
                    this.mHandler.sendEmptyMessage(300);
                    return;
                default:
                    return;
            }
        }
    }

    private void addButtonToGroup(ImageButton button, boolean bPopupWindow, String szShortDescription) {
        if (this.marrBlockedButtons != null) {
            String szName;
            try {
                szName = MyiBaseApplication.getBaseAppContext().getResources().getResourceEntryName(button.getId());
            } catch (NotFoundException e) {
                szName = "Invalid ID";
            }
            if (Utilities.isInArray(this.marrBlockedButtons, szName)) {
                Log.d(TAG, "Button " + szName + " is blocked by config.");
                return;
            }
        }
        RelativeLayout relativeLayout = new RelativeLayout(this);
        TextView textViewTooltip = new TextView(this);
        relativeLayout.addView(button);
        textViewTooltip.setMaxLines(1);
        textViewTooltip.setSingleLine(true);
        textViewTooltip.setEllipsize(TruncateAt.END);
        textViewTooltip.setTextColor(0);
        textViewTooltip.setTextSize(64.0f);
        textViewTooltip.setText((String) button.getTag());
        relativeLayout.addView(textViewTooltip, (int) Utilities.dpToPixel(32), (int) Utilities.dpToPixel(32));
        if (bPopupWindow) {
            button.setBackgroundResource(R.drawable.background_screen_startclass_with_more);
        } else {
            button.setBackgroundResource(R.drawable.background_screen_startclass);
        }
        button.setClickable(true);
        button.setVisibility(0);
        this.mButtonsView.addView(relativeLayout, this.mButtonsView.getChildCount() - 1);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) relativeLayout.getLayoutParams();
        int dpToPixel = Utilities.dpToPixel(2, (Context) this);
        params.topMargin = dpToPixel;
        params.leftMargin = dpToPixel;
        params.bottomMargin = dpToPixel;
        params.width = (int) Utilities.dpToPixel(48);
        params.height = (int) Utilities.dpToPixel(48);
        relativeLayout.setLayoutParams(params);
        TextView textViewTitle = new TextView(this);
        textViewTitle.setTextSize(3, 6.0f);
        textViewTitle.setGravity(17);
        textViewTitle.setTextColor(-16750849);
        textViewTitle.setText(szShortDescription);
        textViewTitle.setPadding(0, 0, (int) Utilities.dpToPixel(3), (int) Utilities.dpToPixel(1));
        relativeLayout.addView(textViewTitle, -2, -2);
        RelativeLayout.LayoutParams LayoutParams = (RelativeLayout.LayoutParams) textViewTitle.getLayoutParams();
        LayoutParams.addRule(12);
        LayoutParams.addRule(9);
        LayoutParams.addRule(11);
        textViewTitle.setLayoutParams(LayoutParams);
    }

    private void triggerButton(int nID) {
        View TempView = new View(getBaseContext());
        TempView.setId(nID);
        onClick(TempView);
    }

    private void addButton(RadialMenuItem parentMenuItem, final int nID, String szName, Drawable icon) {
        RadialMenuItem Item = new RadialMenuItem(szName, szName);
        Item.setDisplayIcon(icon);
        Item.setOnMenuItemPressed(new RadialMenuItemClickListener() {
            public void execute() {
                ClassOnScreenControlsService.this.triggerButton(nID);
                ClassOnScreenControlsService.this.mPieMenu.invalidate();
            }
        });
        if (parentMenuItem.getChildren() == null) {
            parentMenuItem.setMenuChildren(new ArrayList());
        }
        parentMenuItem.getChildren().add(Item);
    }

    private void addButtons() {
        List<RadialMenuItem> firstLevelchildren = new ArrayList();
        this.mPieMenu = new RadialMenuWidget(this);
        RadialMenuItem radialMenuItem = new RadialMenuItem(getString(R.string.close), null);
        radialMenuItem.setDisplayIcon(getResources().getDrawable(17301560));
        RadialMenuItem ListItem = new RadialMenuItem("List", null);
        firstLevelchildren.add(ListItem);
        ListItem.setDisplayIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_list_ol).color(-1));
        ListItem.setOnMenuItemPressed(new RadialMenuItemClickListener() {
            public void execute() {
                ClassOnScreenControlsService.this.triggerButton(R.id.buttonResourceList);
                ClassOnScreenControlsService.this.mPieMenu.invalidate();
            }
        });
        RadialMenuItem LockAndUnLockItem = new RadialMenuItem("LockAndUnLock", "锁定解锁");
        firstLevelchildren.add(LockAndUnLockItem);
        addButton(LockAndUnLockItem, R.id.buttonLockScreen, "锁定", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_lock).color(-1).actionBarSize());
        addButton(LockAndUnLockItem, R.id.buttonUnLockScreen, "解锁", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_unlock).color(-1).actionBarSize());
        RadialMenuItem NextItem = new RadialMenuItem("Next", null);
        firstLevelchildren.add(NextItem);
        NextItem.setDisplayIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_step_forward).color(-1));
        NextItem.setOnMenuItemPressed(new RadialMenuItemClickListener() {
            public void execute() {
                ClassOnScreenControlsService.this.triggerButton(R.id.buttonNext);
                ClassOnScreenControlsService.this.mPieMenu.invalidate();
            }
        });
        RadialMenuItem DeviceItem = new RadialMenuItem("Device", "设备");
        firstLevelchildren.add(DeviceItem);
        addButton(DeviceItem, R.id.buttonMute, "麦克风", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_microphone).color(-1).actionBarSize());
        addButton(DeviceItem, R.id.buttonCamera, "拍照", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_camera).color(-1).actionBarSize());
        addButton(DeviceItem, R.id.buttonProjectToScreen, "投影教师平板到大屏幕", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_tv).color(-1).actionBarSize());
        RadialMenuItem ScreenItem = new RadialMenuItem("Screen", "大屏幕");
        firstLevelchildren.add(ScreenItem);
        addButton(ScreenItem, R.id.buttonDesktop, "显示桌面", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_desktop).color(-1).actionBarSize());
        addButton(ScreenItem, R.id.buttonMouse, "切换到鼠标模式", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_mouse_pointer).color(-1).actionBarSize());
        addButton(ScreenItem, R.id.buttonWhiteBoard, "切换到白板模式", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_pencil_square_o).color(-1).actionBarSize());
        addButton(ScreenItem, R.id.buttonZoomin, "放大", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_search_plus).color(-1).actionBarSize());
        addButton(ScreenItem, R.id.buttonZoomout, "缩小", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_search_minus).color(-1).actionBarSize());
        addButton(ScreenItem, R.id.buttonBroadcast, "广播截屏给学生", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_sitemap).color(-1).actionBarSize());
        if (VERSION.SDK_INT >= 21 && (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_H264) || MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_H264_RECEIVEONLY))) {
            addButton(ScreenItem, R.id.buttonBroadcastReal, "实时广播给学生", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_film).color(-1).actionBarSize());
        }
        RadialMenuItem QuestionItem = new RadialMenuItem("Question", "做题");
        firstLevelchildren.add(QuestionItem);
        addButton(QuestionItem, R.id.buttonStartQuestion, "发起做题", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_question).color(-1).actionBarSize());
        addButton(QuestionItem, R.id.buttonReport, "试题报表", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_line_chart).color(-1).actionBarSize());
        RadialMenuItem PrevItem = new RadialMenuItem("Prev", null);
        firstLevelchildren.add(PrevItem);
        PrevItem.setDisplayIcon(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_step_backward).color(-1));
        PrevItem.setOnMenuItemPressed(new RadialMenuItemClickListener() {
            public void execute() {
                ClassOnScreenControlsService.this.triggerButton(R.id.buttonPrev);
                ClassOnScreenControlsService.this.mPieMenu.invalidate();
            }
        });
        RadialMenuItem PlayItem = new RadialMenuItem("Play", "播放控制");
        firstLevelchildren.add(PlayItem);
        addButton(PlayItem, R.id.buttonPlay, "开始", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_play).color(-1).actionBarSize());
        addButton(PlayItem, R.id.buttonSeekBar, "跳转", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_sliders).color(-1).actionBarSize());
        addButton(PlayItem, R.id.buttonStop, "停止", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_stop).color(-1).actionBarSize());
        addButton(PlayItem, R.id.buttonReturn, "返回正在播放的资源", new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_reply).color(-1).actionBarSize());
        radialMenuItem.setOnMenuItemPressed(new RadialMenuItemClickListener() {
            public void execute() {
                ClassOnScreenControlsService.this.mPieMenu.dismiss();
                ClassOnScreenControlsService.this.mbAllShow = false;
                ClassOnScreenControlsService.this.mMoreButton.setVisibility(0);
            }
        });
        this.mPieMenu.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                ClassOnScreenControlsService.this.mbAllShow = false;
                ClassOnScreenControlsService.this.mMoreButton.setVisibility(0);
            }
        });
        this.mPieMenu.addMenuEntry((List) firstLevelchildren);
        this.mPieMenu.setAnimationSpeed(0);
        this.mPieMenu.setTextColor(-1, 255);
        this.mPieMenu.setIconSize(15, 30);
        this.mPieMenu.setTextSize(15);
        this.mPieMenu.setDisabledColor(-7829368, 255);
        this.mPieMenu.setOutlineColor(-16777216, 225);
        this.mPieMenu.setInnerRingColor(Color.rgb(34, 96, 120), 255);
        this.mPieMenu.setOuterRingColor(-16738596, 255);
        this.mPieMenu.setSelectedColor(-16283188, 255);
        this.mPieMenu.setCenterCircle(radialMenuItem);
        this.mMicButton = new ImageButton(getApplicationContext());
        this.mMicButton.setId(R.id.buttonMute);
        this.mMicButton.setOnClickListener(this);
        this.mMicButton.setTag("麦克风启用/禁用");
        addButtonToGroup(this.mMicButton, false, "话筒");
        if (MP3RecordThread.getMute()) {
            this.mMicButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_microphone_slash).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        } else {
            this.mMicButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_microphone).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        }
        ImageButton imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonLockScreen);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_lock).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("锁定学生平板");
        addButtonToGroup(imageButton, false, "锁定");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonUnLockScreen);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_unlock).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("解锁学生平板");
        addButtonToGroup(imageButton, false, "解锁");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonLockUnlockMerge);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_lock).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("锁定/解锁学生平板");
        addButtonToGroup(imageButton, true, "锁/解");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonResourceList);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_list_ol).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("资源列表");
        addButtonToGroup(imageButton, false, "资源");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonReturn);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_reply).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("返回正在播放的资源");
        addButtonToGroup(imageButton, false, "返回");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonPrev);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_step_backward).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("上一页");
        addButtonToGroup(imageButton, false, "前页");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonPlayMerge);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_play).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("播放");
        addButtonToGroup(imageButton, true, "播放");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonPlay);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_play).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("播放");
        addButtonToGroup(imageButton, false, "播放");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonSeekBar);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_sliders).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("快速跳转PPT页面或视频");
        addButtonToGroup(imageButton, false, "跳转");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonStop);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_stop).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("暂停视频播放");
        addButtonToGroup(imageButton, false, "暂停");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonNext);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_step_forward).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("下一页");
        addButtonToGroup(imageButton, false, "后页");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonZoomin);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_search_plus).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("放大");
        addButtonToGroup(imageButton, false, "放大");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonZoomout);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_search_minus).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("缩小");
        addButtonToGroup(imageButton, false, "缩小");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonStartQuestion);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_question).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("发起做题");
        addButtonToGroup(imageButton, false, "发题");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonReport);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_line_chart).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("试题报表");
        addButtonToGroup(imageButton, false, "报表");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonWhiteBoard);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_pencil_square_o).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("切换到手写笔模式");
        addButtonToGroup(imageButton, false, "手写");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonMouse);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_mouse_pointer).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("切换到鼠标模式");
        addButtonToGroup(imageButton, false, "鼠标");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonErase);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) NovaIcons.nova_icon_eraser).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("切换到擦除模式");
        addButtonToGroup(imageButton, false, "擦除");
        ImageButton clearWhiteBoardButton = new ImageButton(this);
        clearWhiteBoardButton.setId(R.id.buttonClearWhiteBoard);
        clearWhiteBoardButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_trash_o).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        clearWhiteBoardButton.setOnClickListener(this);
        clearWhiteBoardButton.setTag("清除白板上的全部内容");
        addButtonToGroup(clearWhiteBoardButton, false, "清空");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonModeMerge);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_mouse_pointer).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("切换到绘画比/鼠标/橡皮擦/全部清除模式");
        addButtonToGroup(imageButton, true, "切换");
        ImageButton colorButton = new ImageButton(this);
        colorButton.setId(R.id.buttonColor);
        colorButton.setImageDrawable(new IconDrawable((Context) this, (Icon) NovaIcons.nova_icon_paint_palette).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        colorButton.setOnClickListener(this);
        colorButton.setTag("选择颜色和粗细");
        addButtonToGroup(colorButton, true, "颜色");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonDesktop);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_desktop).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("显示桌面");
        addButtonToGroup(imageButton, false, "桌面");
        ImageButton cameraButton = new ImageButton(this);
        cameraButton.setId(R.id.buttonCamera);
        cameraButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_camera).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        cameraButton.setOnClickListener(this);
        cameraButton.setTag("拍照");
        addButtonToGroup(cameraButton, false, "拍照");
        ImageButton broadcastButton = new ImageButton(this);
        broadcastButton.setId(R.id.buttonBroadcast);
        broadcastButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_sitemap).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        broadcastButton.setOnClickListener(this);
        broadcastButton.setTag("广播当前计算机屏幕到学生平板");
        addButtonToGroup(broadcastButton, false, "同屏");
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonProjectToScreen);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_tv).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("投影教师平板到大屏幕");
        addButtonToGroup(imageButton, false, "投影");
        for (int i = 0; i < this.mButtonsView.getChildCount(); i++) {
            View oneView = this.mButtonsView.getChildAt(i);
            if (oneView instanceof ImageButton) {
                ImageButton oneButton = (ImageButton) oneView;
                if (oneButton.getId() != R.id.imageButtonMore) {
                    oneButton.setLongClickable(true);
                }
            }
        }
    }

    private boolean isButtonVisible(int nButtonID) {
        View view = this.mButtonsView.findViewById(nButtonID);
        if (view != null && ((View) view.getParent()).getVisibility() == 0) {
            return true;
        }
        return false;
    }

    private void setButtonState(int nButtonID, int nState) {
        String szName = "";
        boolean bFound = false;
        View view = this.mButtonsView.findViewById(nButtonID);
        if (view != null) {
            view = (View) view.getParent();
            try {
                szName = MyiBaseApplication.getBaseAppContext().getResources().getResourceEntryName(nButtonID);
            } catch (NotFoundException e) {
                szName = "Invalid ID";
            }
            if (Utilities.isInArray(this.marrVisibleButtons, szName)) {
                bFound = true;
            }
            if (nState == 0) {
                if (bFound || this.mbAllButtonsShow) {
                    view.setVisibility(0);
                }
            } else if (nState == 8) {
                view.setVisibility(nState);
            }
        }
    }

    private void updateButtonOnSettings(boolean bShowVisibleButtons) {
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (Settings.getBoolean("ClassButton_PlayMerge", false)) {
            if (!bShowVisibleButtons) {
                setButtonState(R.id.buttonPlay, 8);
                setButtonState(R.id.buttonStop, 8);
                setButtonState(R.id.buttonNext, 8);
                setButtonState(R.id.buttonPrev, 8);
                setButtonState(R.id.buttonSeekBar, 8);
                setButtonState(R.id.buttonZoomin, 8);
                setButtonState(R.id.buttonZoomout, 8);
            }
            if (bShowVisibleButtons) {
                setButtonState(R.id.buttonPlayMerge, 0);
            }
        } else {
            if (bShowVisibleButtons) {
                setButtonState(R.id.buttonPlay, 0);
                setButtonState(R.id.buttonStop, 0);
                setButtonState(R.id.buttonNext, 0);
                setButtonState(R.id.buttonPrev, 0);
                setButtonState(R.id.buttonSeekBar, 0);
                setButtonState(R.id.buttonZoomin, 0);
                setButtonState(R.id.buttonZoomout, 0);
            }
            if (!bShowVisibleButtons) {
                setButtonState(R.id.buttonPlayMerge, 8);
            }
        }
        if (Settings.getBoolean("ClassButton_NextPrevHide", false) && !bShowVisibleButtons) {
            setButtonState(R.id.buttonNext, 8);
            setButtonState(R.id.buttonPrev, 8);
        }
        if (Settings.getBoolean("ClassButton_LockUnlockMerge", false)) {
            if (!bShowVisibleButtons) {
                setButtonState(R.id.buttonLockScreen, 8);
                setButtonState(R.id.buttonUnLockScreen, 8);
            }
            if (bShowVisibleButtons) {
                setButtonState(R.id.buttonLockUnlockMerge, 0);
            }
        } else {
            if (bShowVisibleButtons) {
                setButtonState(R.id.buttonLockScreen, 0);
                setButtonState(R.id.buttonUnLockScreen, 0);
            }
            if (!bShowVisibleButtons) {
                setButtonState(R.id.buttonLockUnlockMerge, 8);
            }
        }
        if (Settings.getBoolean("ClassButton_DrawMerge", false)) {
            if (!bShowVisibleButtons) {
                setButtonState(R.id.buttonWhiteBoard, 8);
                setButtonState(R.id.buttonMouse, 8);
                setButtonState(R.id.buttonErase, 8);
                setButtonState(R.id.buttonClearWhiteBoard, 8);
            }
            if (bShowVisibleButtons) {
                setButtonState(R.id.buttonModeMerge, 0);
                return;
            }
            return;
        }
        if (bShowVisibleButtons) {
            setButtonState(R.id.buttonWhiteBoard, 0);
            setButtonState(R.id.buttonMouse, 0);
            setButtonState(R.id.buttonErase, 0);
            setButtonState(R.id.buttonClearWhiteBoard, 0);
        }
        if (!bShowVisibleButtons) {
            setButtonState(R.id.buttonModeMerge, 8);
        }
    }

    private void updateButtonsOnPlayState() {
        Activity activity = UI.getCurrentActivity();
        if (activity == null || !(activity instanceof ScreenDisplayActivity)) {
            setButtonState(R.id.buttonColor, 8);
            setButtonState(R.id.buttonModeMerge, 8);
        } else {
            setButtonState(R.id.buttonColor, 0);
            setButtonState(R.id.buttonModeMerge, 0);
        }
        int nScreenID = TeacherPadApplication.mActiveScreenID;
        if (nScreenID >= 0 && nScreenID < TeacherPadApplication.marrMonitors.size()) {
            MultiScreen screen = (MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID);
            if (screen.arrPlayStackFlags.size() > 0) {
                int nTopResourceFlags = ((Integer) screen.arrPlayStackFlags.get(0)).intValue();
                if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTZOOM & nTopResourceFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTZOOM) {
                    setButtonState(R.id.buttonZoomin, 0);
                    setButtonState(R.id.buttonZoomout, 0);
                } else {
                    setButtonState(R.id.buttonZoomin, 8);
                    setButtonState(R.id.buttonZoomout, 8);
                }
                if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK & nTopResourceFlags) != StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK) {
                    setButtonState(R.id.buttonNext, 8);
                    setButtonState(R.id.buttonPrev, 8);
                    setButtonState(R.id.buttonSeekBar, 8);
                } else if (!isButtonVisible(R.id.buttonPlayMerge)) {
                    setButtonState(R.id.buttonNext, 0);
                    setButtonState(R.id.buttonPrev, 0);
                    setButtonState(R.id.buttonSeekBar, 0);
                }
                if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP & nTopResourceFlags) != StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP) {
                    setButtonState(R.id.buttonPlay, 8);
                    setButtonState(R.id.buttonStop, 8);
                } else if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_PLAYING & nTopResourceFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_PLAYING) {
                    setButtonState(R.id.buttonPlay, 8);
                    setButtonState(R.id.buttonStop, 0);
                } else {
                    setButtonState(R.id.buttonPlay, 0);
                    setButtonState(R.id.buttonStop, 8);
                }
                int i = StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPAN;
                i = StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPAN;
            } else {
                setButtonState(R.id.buttonZoomin, 8);
                setButtonState(R.id.buttonZoomout, 8);
                setButtonState(R.id.buttonPlay, 8);
                setButtonState(R.id.buttonStop, 8);
                setButtonState(R.id.buttonNext, 8);
                setButtonState(R.id.buttonPrev, 8);
                setButtonState(R.id.buttonSeekBar, 8);
            }
            if (activity != null && (activity instanceof ScreenDisplayActivity)) {
                ScreenDisplayActivity ScreenDisplayActivity = (ScreenDisplayActivity) activity;
                if (ScreenDisplayActivity.hasButton(R.id.buttonZoomin)) {
                    setButtonState(R.id.buttonZoomin, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonZoomout)) {
                    setButtonState(R.id.buttonZoomout, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonPlay)) {
                    setButtonState(R.id.buttonPlay, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonStop)) {
                    setButtonState(R.id.buttonStop, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonNext)) {
                    setButtonState(R.id.buttonNext, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonPrev)) {
                    setButtonState(R.id.buttonPrev, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonSeekBar)) {
                    setButtonState(R.id.buttonSeekBar, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonWhiteBoard)) {
                    setButtonState(R.id.buttonWhiteBoard, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonMouse)) {
                    setButtonState(R.id.buttonMouse, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonErase)) {
                    setButtonState(R.id.buttonErase, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonColor)) {
                    setButtonState(R.id.buttonColor, 8);
                }
                if (ScreenDisplayActivity.hasButton(R.id.buttonClearWhiteBoard)) {
                    setButtonState(R.id.buttonClearWhiteBoard, 8);
                }
            }
            ImageView drawMergeButton = (ImageView) this.mButtonsView.findViewById(R.id.buttonModeMerge);
            if (ScreenDisplayActivity.getCursorMode()) {
                drawMergeButton.setImageDrawable(this.mMouseDrawable);
            } else if (ScreenDisplayActivity.getPenMode()) {
                drawMergeButton.setImageDrawable(this.mPencialDrawable);
            } else {
                drawMergeButton.setImageDrawable(this.mEraseDrawable);
            }
        }
    }

    private void showHideButtons(boolean bShowAll) {
        if (!bShowAll) {
            this.mPieMenu.dismiss();
        }
        this.mbAllButtonsShow = bShowAll;
        updateButtonDisplay();
    }

    private void updateButtonDisplay() {
        int i;
        updateButtonOnSettings(true);
        for (i = 0; i < this.mButtonsView.getChildCount(); i++) {
            View oneView = this.mButtonsView.getChildAt(i);
            if (oneView instanceof RelativeLayout) {
                oneView = ((RelativeLayout) oneView).getChildAt(0);
            }
            if (oneView instanceof ImageButton) {
                ImageButton oneButton = (ImageButton) oneView;
                int nID = oneButton.getId();
                boolean bFound = false;
                String szName = "";
                try {
                    szName = MyiBaseApplication.getBaseAppContext().getResources().getResourceEntryName(nID);
                } catch (NotFoundException e) {
                    szName = "Invalid ID";
                }
                if (nID == R.id.imageButtonMore) {
                    bFound = true;
                }
                if (nID == R.id.buttonPlayMerge) {
                    bFound = true;
                }
                if (Utilities.isInArray(this.marrVisibleButtons, szName)) {
                    bFound = true;
                }
                if (bFound || this.mbAllButtonsShow) {
                    ((View) oneButton.getParent()).setVisibility(0);
                } else {
                    ((View) oneButton.getParent()).setVisibility(8);
                }
            }
        }
        updateButtonsOnPlayState();
        updateButtonOnSettings(false);
        this.mVisibleButtonCount = 0;
        for (i = 0; i < this.mButtonsView.getChildCount(); i++) {
            oneView = this.mButtonsView.getChildAt(i);
            if ((oneView instanceof RelativeLayout) && oneView.getVisibility() == 0) {
                this.mVisibleButtonCount++;
            }
        }
    }

    private void createFloatView() {
        this.mInflater = (LayoutInflater) getSystemService("layout_inflater");
        this.mRootView = this.mInflater.inflate(R.layout.layout_startclassbuttons, null);
        this.mButtonsView = (LinearLayout) this.mRootView.findViewById(R.id.layoutStartClassButtons);
        this.mParentButtonsView = (LinearLayout) this.mRootView.findViewById(R.id.layoutStartClassParent);
        this.mPencialDrawable = new IconDrawable((Context) this, FontAwesomeIcons.fa_pencil_square_o).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize();
        this.mMouseDrawable = new IconDrawable((Context) this, FontAwesomeIcons.fa_mouse_pointer).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize();
        this.mEraseDrawable = new IconDrawable((Context) this, NovaIcons.nova_icon_eraser).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize();
        this.mMoreButton = (ImageButton) this.mRootView.findViewById(R.id.imageButtonMore);
        this.mMoreButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_ellipsis_h).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        this.mMoreButton.setTag("操作按钮，拖动此按钮可以移动工具栏");
        mWindowManager = (WindowManager) getApplicationContext().getSystemService("window");
        mParams = new LayoutParams();
        mParams.type = 2003;
        mParams.format = 1;
        mParams.flags = 40;
        this.mScreenWidth = Utilities.getScreenWidth((Context) this);
        mParams.width = -2;
        mParams.height = -2;
        this.mMoreButton.setOnTouchListener(new OnTouchListener() {
            int lastX;
            int lastY;
            int paramX;
            int paramY;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case 0:
                        this.lastX = (int) event.getRawX();
                        this.lastY = (int) event.getRawY();
                        ClassOnScreenControlsService.mParams = (LayoutParams) ClassOnScreenControlsService.this.mRootView.getLayoutParams();
                        this.paramX = ClassOnScreenControlsService.mParams.x;
                        this.paramY = ClassOnScreenControlsService.mParams.y;
                        ClassOnScreenControlsService.this.mItemMove = false;
                        break;
                    case 1:
                        if (ClassOnScreenControlsService.this.mItemMove) {
                            ClassOnScreenControlsService.this.autoSetToolbarDirection();
                            ClassOnScreenControlsService.this.saveData();
                            return true;
                        }
                        break;
                    case 2:
                        int dx = ((int) event.getRawX()) - this.lastX;
                        int dy = ((int) event.getRawY()) - this.lastY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            ClassOnScreenControlsService.mParams.x = this.paramX + dx;
                            ClassOnScreenControlsService.mParams.y = this.paramY + dy;
                            ClassOnScreenControlsService.this.mItemMove = true;
                            Log.d("MuteControlService", "move toolbar x=" + ClassOnScreenControlsService.mParams.x + ",y=" + ClassOnScreenControlsService.mParams.y + ",width=" + ClassOnScreenControlsService.this.mRootView.getWidth() + ",height=" + ClassOnScreenControlsService.this.mRootView.getHeight());
                            ClassOnScreenControlsService.mWindowManager.updateViewLayout(ClassOnScreenControlsService.this.mRootView, ClassOnScreenControlsService.mParams);
                            break;
                        }
                }
                return false;
            }
        });
        this.mMoreButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!ClassOnScreenControlsService.this.mItemMove) {
                    ClassOnScreenControlsService.this.mbAllShow = !ClassOnScreenControlsService.this.mbAllShow;
                    ClassOnScreenControlsService.this.showHideButtons(ClassOnScreenControlsService.this.mbAllShow);
                }
            }
        });
        mWindowManager.addView(this.mRootView, mParams);
        this.mAdded = true;
        mWindowManager.getDefaultDisplay().getMetrics(new DisplayMetrics());
        addButtons();
        loadData();
        showHideButtons(false);
        mWindowManager.updateViewLayout(this.mRootView, mParams);
        this.mHandler.postDelayed(this.mCheckCurrentActivityRunnable, 500);
    }

    private void autoSetToolbarDirection() {
        int[] locations = new int[2];
        int[] locations2 = new int[2];
        this.mMoreButton.getLocationOnScreen(locations);
        this.mButtonsView.getLocationOnScreen(locations2);
        int nLeftPos = locations[0];
        int nLeftPos2 = locations2[0];
        if (nLeftPos == 0) {
            return;
        }
        if (nLeftPos > this.mScreenWidth - 120 || nLeftPos2 < 50) {
            this.mButtonsView.setOrientation(1);
            this.mParentButtonsView.setOrientation(1);
            return;
        }
        this.mButtonsView.setOrientation(0);
        this.mParentButtonsView.setOrientation(0);
    }

    public void onClick(View v) {
        boolean bProcessed = false;
        if (v.getId() == R.id.buttonMute) {
            boolean bMute = MP3RecordThread.getMute();
            RadialMenuItem Item = this.mPieMenu.findItem("麦克风");
            bMute = !bMute;
            MP3RecordThread.setMute(bMute);
            if (bMute) {
                Item.setDisplayIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_microphone_slash).color(-1).actionBarSize());
                this.mMicButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_microphone_slash).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            } else {
                Item.setDisplayIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_microphone).color(-1).actionBarSize());
                this.mMicButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_microphone).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            }
            bProcessed = true;
        }
        if (UI.getCurrentActivity() != null && !bProcessed) {
            new StartClassControlUnit(UI.getCurrentActivity()).onClick(v);
        }
    }

    private void saveData() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt("buttonX", mParams.x);
        editor.putInt("buttonY", mParams.y);
        editor.putInt("orientation", this.mButtonsView.getOrientation());
        editor.commit();
    }

    private void loadData() {
        int i;
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this);
        String[] arrDefaultButtons = getResources().getStringArray(R.array.startclassicons_default);
        Set<String> set = new HashSet();
        for (Object add : arrDefaultButtons) {
            set.add(add);
        }
        ArrayList<String> arrVisibleButtons = new ArrayList(Settings.getStringSet("ClassVisibleButtons", set));
        this.marrVisibleButtons.clear();
        for (i = 0; i < arrVisibleButtons.size(); i++) {
            this.marrVisibleButtons.add((String) arrVisibleButtons.get(i));
        }
        Display display = mWindowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mParams.x = ((metrics.widthPixels - mParams.width) / 2) - 50;
        mParams.y = (-((metrics.heightPixels - mParams.height) / 2)) + (metrics.heightPixels - 130);
        mParams.x = Settings.getInt("buttonX", mParams.x);
        mParams.y = Settings.getInt("buttonY", mParams.y);
        this.mButtonsView.setOrientation(Settings.getInt("orientation", 0));
        this.mParentButtonsView.setOrientation(Settings.getInt("orientation", 0));
    }
}
