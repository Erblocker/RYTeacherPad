package com.netspace.teacherpad.theads;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.servers.MP3RecordThread;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import eu.janmuller.android.simplecropimage.CropImage;
import java.util.ArrayList;
import java.util.HashSet;
import net.sourceforge.opencamera.MainActivity;

public class MuteControlService extends Service implements OnClickListener, OnLongClickListener, OnHoverListener {
    private static final int ATTACH_MARGIN_LEFT = 50;
    private static final int ATTACH_MARGIN_RIGHT = 120;
    private static final int HIDE_WINDOW = 300;
    public static final String OPERATION = "operation";
    public static final int OPERATION_HIDE = 101;
    public static final int OPERATION_SHOW = 100;
    private static final int SHOW_WINDOW = 200;
    private static final String TAG = "MuteControlService";
    private static LayoutParams mParams;
    private static WindowManager mWindowManager;
    private View mActiveTooltipView;
    private boolean mAdded = false;
    private LinearLayout mButtonsView;
    private Runnable mCheckCurrentActivityRunnable = new Runnable() {
        public void run() {
            Activity activity = UI.getCurrentActivity();
            if (activity != null) {
                if (activity instanceof MainActivity) {
                    if (MuteControlService.this.mAdded) {
                        MuteControlService.this.mHandler.sendEmptyMessage(300);
                    }
                    MuteControlService.this.mHandler.postDelayed(this, 1000);
                    return;
                } else if (activity instanceof CropImage) {
                    if (MuteControlService.this.mAdded) {
                        MuteControlService.this.mHandler.sendEmptyMessage(300);
                    }
                    MuteControlService.this.mHandler.postDelayed(this, 1000);
                    return;
                } else if (!MuteControlService.this.mAdded) {
                    MuteControlService.this.mHandler.sendEmptyMessage(200);
                }
            } else if (MuteControlService.this.mAdded) {
                MuteControlService.this.mHandler.sendEmptyMessage(300);
            }
            MuteControlService.this.mHandler.postDelayed(this, 1000);
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (!MuteControlService.this.mAdded) {
                        MuteControlService.mWindowManager.addView(MuteControlService.this.mButtonsView, MuteControlService.mParams);
                        MuteControlService.this.mAdded = true;
                        return;
                    }
                    return;
                case 300:
                    if (MuteControlService.this.mAdded) {
                        MuteControlService.mWindowManager.removeView(MuteControlService.this.mButtonsView);
                        MuteControlService.this.mAdded = false;
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
    private int mLastHoverX = 0;
    private int mLastHoverY = 0;
    private Toast mLastToast;
    private View mLastTooltipView;
    private ImageButton mMicButton;
    private ImageButton mMoreButton;
    private int mScreenWidth = 0;
    private Runnable mShowTipRunnable = new Runnable() {
        public void run() {
            if (MuteControlService.this.mActiveTooltipView != MuteControlService.this.mLastTooltipView) {
                if (MuteControlService.this.mLastToast != null) {
                    MuteControlService.this.mLastToast.cancel();
                }
                MuteControlService.this.mLastTooltipView = null;
                MuteControlService.this.mLastToast = null;
                int[] location = new int[2];
                String szText = "";
                MuteControlService.this.mActiveTooltipView.getLocationOnScreen(location);
                if (MuteControlService.this.mLastHoverX <= location[0] || MuteControlService.this.mLastHoverX >= location[0] + MuteControlService.this.mActiveTooltipView.getWidth() || MuteControlService.this.mLastHoverY <= location[1] || MuteControlService.this.mLastHoverY >= location[1] + MuteControlService.this.mActiveTooltipView.getHeight()) {
                    MuteControlService.this.mActiveTooltipView = null;
                    return;
                }
                location[1] = location[1] + 20;
                if (MuteControlService.this.mActiveTooltipView.getTag() != null) {
                    szText = (String) MuteControlService.this.mActiveTooltipView.getTag();
                }
                if (!szText.isEmpty()) {
                    MuteControlService.this.mLastTooltipView = MuteControlService.this.mActiveTooltipView;
                    Toast toast = Toast.makeText(MuteControlService.this.getApplicationContext(), szText, 0);
                    toast.setGravity(51, location[0], location[1]);
                    toast.show();
                    MuteControlService.this.mLastToast = toast;
                }
            }
        }
    };
    private int mVisibleButtonCount = 0;
    private String[] marrBlockedButtons;
    private ArrayList<Integer> marrVisibleButtons = new ArrayList();
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

    private void addButtonToGroup(ImageButton button, int nIndex) {
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
        button.setBackgroundResource(R.drawable.background_screen_startclass);
        button.setClickable(true);
        button.setVisibility(0);
        this.mButtonsView.addView(button, nIndex);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        int dpToPixel = Utilities.dpToPixel(2, (Context) this);
        params.topMargin = dpToPixel;
        params.leftMargin = dpToPixel;
        params.bottomMargin = dpToPixel;
        params.width = -2;
        params.height = -2;
        button.setLayoutParams(params);
    }

    private void addButtons() {
        this.mMicButton = new ImageButton(getApplicationContext());
        this.mMicButton.setId(R.id.buttonMute);
        this.mMicButton.setOnClickListener(this);
        this.mMicButton.setTag("麦克风启用/禁用");
        addButtonToGroup(this.mMicButton, 0);
        if (MP3RecordThread.getMute()) {
            this.mMicButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_microphone_slash).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        } else {
            this.mMicButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_microphone).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        }
        ImageButton lockButton = new ImageButton(this);
        lockButton.setId(R.id.buttonLockScreen);
        lockButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_lock).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        lockButton.setOnClickListener(this);
        lockButton.setTag("锁定学生平板");
        addButtonToGroup(lockButton, 1);
        ImageButton imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonUnLockScreen);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_unlock).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("解锁学生平板");
        addButtonToGroup(imageButton, 2);
        ImageButton listButton = new ImageButton(this);
        listButton.setId(R.id.buttonResourceList);
        listButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_list_ol).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        listButton.setOnClickListener(this);
        listButton.setTag("资源列表");
        addButtonToGroup(listButton, 3);
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonReturn);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_reply).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("返回正在资源");
        addButtonToGroup(imageButton, 4);
        ImageButton prevButton = new ImageButton(this);
        prevButton.setId(R.id.buttonPrev);
        prevButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_step_backward).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        prevButton.setOnClickListener(this);
        prevButton.setTag("上一页");
        addButtonToGroup(prevButton, 5);
        ImageButton playButton = new ImageButton(this);
        playButton.setId(R.id.buttonPlay);
        playButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_play).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        playButton.setOnClickListener(this);
        playButton.setTag("播放");
        addButtonToGroup(playButton, 6);
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonSeekBar);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_sliders).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("快速跳转PPT页面或视频");
        addButtonToGroup(imageButton, 7);
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonStop);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_stop).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("暂停视频播放");
        addButtonToGroup(imageButton, 8);
        ImageButton nextButton = new ImageButton(this);
        nextButton.setId(R.id.buttonNext);
        nextButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_step_forward).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        nextButton.setOnClickListener(this);
        nextButton.setTag("下一页");
        addButtonToGroup(nextButton, 9);
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonZoomin);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_search_plus).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("放大");
        addButtonToGroup(imageButton, 10);
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonZoomout);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_search_minus).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("缩小");
        addButtonToGroup(imageButton, 11);
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonStartQuestion);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_question).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("发起做题");
        addButtonToGroup(imageButton, 12);
        ImageButton reportButton = new ImageButton(this);
        reportButton.setId(R.id.buttonReport);
        reportButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_line_chart).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        reportButton.setOnClickListener(this);
        reportButton.setTag("试题报表");
        addButtonToGroup(reportButton, 13);
        imageButton = new ImageButton(this);
        imageButton.setId(R.id.buttonWhiteBoard);
        imageButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_pencil_square_o).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageButton.setOnClickListener(this);
        imageButton.setTag("切换到手写笔模式");
        addButtonToGroup(imageButton, 14);
        ImageButton mouseButton = new ImageButton(this);
        mouseButton.setId(R.id.buttonMouse);
        mouseButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_mouse_pointer).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        mouseButton.setOnClickListener(this);
        mouseButton.setTag("切换到鼠标模式");
        addButtonToGroup(mouseButton, 15);
        ImageButton desktopButton = new ImageButton(this);
        desktopButton.setId(R.id.buttonDesktop);
        desktopButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_desktop).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        desktopButton.setOnClickListener(this);
        desktopButton.setTag("显示桌面");
        addButtonToGroup(desktopButton, 16);
        ImageButton cameraButton = new ImageButton(this);
        cameraButton.setId(R.id.buttonCamera);
        cameraButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_camera).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        cameraButton.setOnClickListener(this);
        cameraButton.setTag("拍照");
        addButtonToGroup(cameraButton, 17);
        ImageButton broadcastButton = new ImageButton(this);
        broadcastButton.setId(R.id.buttonBroadcast);
        broadcastButton.setImageDrawable(new IconDrawable((Context) this, (Icon) FontAwesomeIcons.fa_sitemap).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        broadcastButton.setOnClickListener(this);
        broadcastButton.setTag("广播当前计算机屏幕到学生平板");
        addButtonToGroup(broadcastButton, 18);
        for (int i = 0; i < this.mButtonsView.getChildCount(); i++) {
            View oneView = this.mButtonsView.getChildAt(i);
            if (oneView instanceof ImageButton) {
                ImageButton oneButton = (ImageButton) oneView;
                oneButton.setOnHoverListener(this);
                if (oneButton.getId() != R.id.imageButtonMore) {
                    oneButton.setLongClickable(true);
                    oneButton.setOnLongClickListener(this);
                }
            }
        }
    }

    private void showHideButtons(boolean bShowAll) {
        this.mVisibleButtonCount = 0;
        for (int i = 0; i < this.mButtonsView.getChildCount(); i++) {
            View oneView = this.mButtonsView.getChildAt(i);
            if (oneView instanceof ImageButton) {
                ImageButton oneButton = (ImageButton) oneView;
                int nID = oneButton.getId();
                boolean bFound = false;
                if (nID == R.id.imageButtonMore) {
                    bFound = true;
                }
                for (int j = 0; j < this.marrVisibleButtons.size(); j++) {
                    if (((Integer) this.marrVisibleButtons.get(j)).intValue() == nID) {
                        bFound = true;
                        break;
                    }
                }
                if (bShowAll) {
                    bFound = true;
                }
                if (bFound) {
                    oneButton.setVisibility(0);
                    this.mVisibleButtonCount++;
                } else {
                    oneButton.setVisibility(8);
                }
            }
        }
        this.mButtonsView.post(new Runnable() {
            public void run() {
                MuteControlService.this.autoSetToolbarDirection();
            }
        });
    }

    private void createFloatView() {
        this.mInflater = (LayoutInflater) getSystemService("layout_inflater");
        this.mButtonsView = (LinearLayout) this.mInflater.inflate(R.layout.layout_startclassbuttons, null);
        this.mButtonsView.setOnHoverListener(new OnHoverListener() {
            public boolean onHover(View v, MotionEvent event) {
                MuteControlService.this.mLastHoverX = (int) event.getX();
                MuteControlService.this.mLastHoverY = (int) event.getY();
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                MuteControlService muteControlService = MuteControlService.this;
                muteControlService.mLastHoverX = muteControlService.mLastHoverX + location[0];
                muteControlService = MuteControlService.this;
                muteControlService.mLastHoverY = muteControlService.mLastHoverY + location[1];
                return false;
            }
        });
        this.mMoreButton = (ImageButton) this.mButtonsView.findViewById(R.id.imageButtonMore);
        this.mMoreButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_ellipsis_h).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        this.mMoreButton.setTag("显示/隐藏按钮，拖动此按钮可以移动工具栏");
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.mMoreButton.getLayoutParams();
        int dpToPixel = Utilities.dpToPixel(2, (Context) this);
        params.topMargin = dpToPixel;
        params.leftMargin = dpToPixel;
        params.bottomMargin = dpToPixel;
        params.width = -2;
        params.height = -2;
        this.mMoreButton.setLayoutParams(params);
        mWindowManager = (WindowManager) getApplicationContext().getSystemService("window");
        mParams = new LayoutParams();
        mParams.type = 2003;
        mParams.format = 1;
        mParams.flags = 40;
        this.mScreenWidth = Utilities.getScreenWidth((Context) this);
        mParams.width = -2;
        mParams.height = -2;
        this.mButtonsView.setOnTouchListener(new OnTouchListener() {
            int lastX;
            int lastY;
            int paramX;
            int paramY;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case 0:
                        this.lastX = (int) event.getRawX();
                        this.lastY = (int) event.getRawY();
                        this.paramX = MuteControlService.mParams.x;
                        this.paramY = MuteControlService.mParams.y;
                        MuteControlService.this.mItemMove = false;
                        break;
                    case 1:
                        if (MuteControlService.this.mItemMove) {
                            MuteControlService.this.autoSetToolbarDirection();
                            MuteControlService.this.saveData();
                            return true;
                        }
                        break;
                    case 2:
                        int dx = ((int) event.getRawX()) - this.lastX;
                        int dy = ((int) event.getRawY()) - this.lastY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            MuteControlService.mParams.x = this.paramX + dx;
                            MuteControlService.mParams.y = this.paramY + dy;
                            MuteControlService.this.mItemMove = true;
                            MuteControlService.mWindowManager.updateViewLayout(MuteControlService.this.mButtonsView, MuteControlService.mParams);
                            break;
                        }
                }
                return false;
            }
        });
        this.mButtonsView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!MuteControlService.this.mItemMove) {
                    MuteControlService.this.mbAllShow = !MuteControlService.this.mbAllShow;
                    MuteControlService.this.showHideButtons(MuteControlService.this.mbAllShow);
                }
            }
        });
        mWindowManager.addView(this.mButtonsView, mParams);
        this.mAdded = true;
        mWindowManager.getDefaultDisplay().getMetrics(new DisplayMetrics());
        addButtons();
        loadData();
        showHideButtons(false);
        mWindowManager.updateViewLayout(this.mButtonsView, mParams);
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
        if (this.mVisibleButtonCount >= 10 || (nLeftPos <= this.mScreenWidth - 120 && nLeftPos2 >= 50)) {
            this.mButtonsView.setOrientation(0);
        } else {
            this.mButtonsView.setOrientation(1);
        }
    }

    public void onClick(View v) {
        boolean bProcessed = false;
        if (v.getId() == R.id.buttonMute) {
            boolean bMute = !MP3RecordThread.getMute();
            MP3RecordThread.setMute(bMute);
            if (bMute) {
                this.mMicButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_microphone_slash).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
            } else {
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
        ArrayList<String> arrVisibleButtons = new ArrayList();
        for (int i = 0; i < this.marrVisibleButtons.size(); i++) {
            arrVisibleButtons.add(String.valueOf(this.marrVisibleButtons.get(i)));
        }
        editor.putStringSet("Buttons", new HashSet(arrVisibleButtons));
        editor.putInt("buttonX", mParams.x);
        editor.putInt("buttonY", mParams.y);
        editor.putInt("orientation", this.mButtonsView.getOrientation());
        editor.commit();
    }

    private void loadData() {
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<String> arrVisibleButtons = new ArrayList(Settings.getStringSet("Buttons", new HashSet()));
        this.marrVisibleButtons.clear();
        for (int i = 0; i < arrVisibleButtons.size(); i++) {
            this.marrVisibleButtons.add(Integer.valueOf((String) arrVisibleButtons.get(i)));
        }
        Display display = mWindowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mParams.x = ((metrics.widthPixels - mParams.width) / 2) - 50;
        mParams.y = (-((metrics.heightPixels - mParams.height) / 2)) + (metrics.heightPixels - 130);
        mParams.x = Settings.getInt("buttonX", mParams.x);
        mParams.y = Settings.getInt("buttonY", mParams.y);
        this.mButtonsView.setOrientation(Settings.getInt("orientation", 0));
        if (this.marrVisibleButtons.size() == 0) {
            this.marrVisibleButtons.add(Integer.valueOf(R.id.buttonMute));
            this.marrVisibleButtons.add(Integer.valueOf(R.id.buttonResourceList));
            this.marrVisibleButtons.add(Integer.valueOf(R.id.buttonLockScreen));
            this.marrVisibleButtons.add(Integer.valueOf(R.id.buttonUnLockScreen));
            this.marrVisibleButtons.add(Integer.valueOf(R.id.buttonNext));
            this.marrVisibleButtons.add(Integer.valueOf(R.id.buttonPrev));
            this.marrVisibleButtons.add(Integer.valueOf(R.id.buttonStartQuestion));
            this.marrVisibleButtons.add(Integer.valueOf(R.id.buttonCamera));
        }
    }

    public boolean onLongClick(View v) {
        final View TargetView = v;
        new Builder(UI.getCurrentActivity()).setItems(new String[]{"始终显示", "始终隐藏"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int nID = TargetView.getId();
                Integer[] nIndex = new Integer[]{Integer.valueOf(0)};
                if (which == 0) {
                    if (!Utilities.isInArray(MuteControlService.this.marrVisibleButtons, nID)) {
                        MuteControlService.this.marrVisibleButtons.add(Integer.valueOf(nID));
                    }
                    MuteControlService.this.saveData();
                } else if (which == 1 && Utilities.isInArray(MuteControlService.this.marrVisibleButtons, nID, nIndex)) {
                    MuteControlService.this.marrVisibleButtons.remove(nIndex[0].intValue());
                    TargetView.setVisibility(8);
                    MuteControlService.this.saveData();
                }
            }
        }).setTitle("选择按钮的显示或隐藏").setCancelable(true).show();
        return true;
    }

    public boolean onHover(View v, MotionEvent event) {
        this.mLastHoverX = (int) event.getX();
        this.mLastHoverY = (int) event.getY();
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        this.mLastHoverX += location[0];
        this.mLastHoverY += location[1];
        if (this.mActiveTooltipView != v) {
            Log.d(TAG, "mButtonsView on Buttons");
            this.mActiveTooltipView = v;
            this.mButtonsView.removeCallbacks(this.mShowTipRunnable);
            this.mButtonsView.postDelayed(this.mShowTipRunnable, 1000);
        }
        return false;
    }
}
