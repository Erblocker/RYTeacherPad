package com.netspace.teacherpad.dialog;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.dialog.AskQuestionDialog;
import com.netspace.library.dialog.AskQuestionDialog.OnStartAskQuestionCallBack;
import com.netspace.library.fragment.QuestionUserFragment.StudentInfo;
import com.netspace.library.im.IMService;
import com.netspace.library.servers.MJpegRelayToMulticastThread;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.MainActivity;
import com.netspace.teacherpad.MasterControlActivity;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.ScreenDisplayActivity;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.dialog.SelectPCDialog.SelectPCDialogCallBack;
import com.netspace.teacherpad.dialog.SelectPCDialog.SelectPCDialogDismissCallBack;
import com.netspace.teacherpad.modules.startclass.ReportActivity2;
import com.netspace.teacherpad.popup.ModePopupWindow;
import com.netspace.teacherpad.popup.PencialPopupWindow;
import com.netspace.teacherpad.popup.PencialPopupWindow.OnChangeCallBack;
import com.netspace.teacherpad.popup.PlayPopupWindow;
import com.netspace.teacherpad.structure.MultiScreen;
import com.netspace.teacherpad.structure.PlayPos;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.http.HttpStatus;

public class StartClassControlUnit implements OnClickListener, OnSeekBarChangeListener {
    private static final String TAG = "StartClassControlUnit";
    public static int WM_CLASSMEDIA_CONTROL_FLAG_PLAYING = 1;
    public static int WM_CLASSMEDIA_CONTROL_FLAG_STOPPED = 2;
    public static int WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPAN = 2048;
    public static int WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP = 1024;
    public static int WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK = 512;
    public static int WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTZOOM = 256;
    private static ArrayList<Dialog> marrPopupDialogs = new ArrayList();
    private static boolean mbLockState = false;
    private Context mContext;
    private boolean m_bNoUpdatePlayPos = false;
    private String m_szReturnIMMessage;

    /* renamed from: com.netspace.teacherpad.dialog.StartClassControlUnit$3 */
    class AnonymousClass3 implements SelectPCDialogCallBack {
        private final /* synthetic */ SelectPCDialog val$selectPCDialog;

        AnonymousClass3(SelectPCDialog selectPCDialog) {
            this.val$selectPCDialog = selectPCDialog;
        }

        public void onSelectPC(String szSessionID, String szIP, String szStatus) {
            TeacherPadApplication.szPCSessionID = szSessionID;
            TeacherPadApplication.szPCIP = szIP;
            TeacherPadApplication.szPCStatus = szStatus;
            StartClassControlUnit.launchPC();
            this.val$selectPCDialog.setDismissCallBack(null);
        }
    }

    public StartClassControlUnit(Context context) {
        this.mContext = context;
    }

    public void onClick(View v) {
        int i;
        Activity activity;
        Utilities.logClick(v);
        for (i = 0; i < marrPopupDialogs.size(); i++) {
            Dialog oneDialog = (Dialog) marrPopupDialogs.get(i);
            if (oneDialog.isShowing()) {
                oneDialog.dismiss();
            }
        }
        marrPopupDialogs.clear();
        ScreenDisplayActivity CurrentScreenDisplayActivity = null;
        if (UI.getCurrentActivity() != null) {
            activity = UI.getCurrentActivity();
            if (activity instanceof ScreenDisplayActivity) {
                CurrentScreenDisplayActivity = (ScreenDisplayActivity) activity;
            }
        }
        TypedArray arrWhiteBoardIDs = this.mContext.getResources().obtainTypedArray(R.array.whiteboards_ids);
        ArrayList arrBoardIDs = new ArrayList();
        for (i = 0; i < arrWhiteBoardIDs.length(); i++) {
            arrBoardIDs.add(Integer.valueOf(arrWhiteBoardIDs.getResourceId(i, 0)));
        }
        Integer[] nBoardIndex = new Integer[]{Integer.valueOf(0)};
        if (v.getId() == R.id.buttonMute) {
            TeacherPadApplication.IMThread.SendMessage("Mute");
        } else if (v.getId() == R.id.buttonLockScreen) {
            TeacherPadApplication.IMThread.SendLockRequest(null);
            TeacherPadApplication.mapStatusUpdateTime.clear();
        } else if (v.getId() == R.id.buttonUnLockScreen) {
            TeacherPadApplication.IMThread.SendUnLockRequest(null);
            TeacherPadApplication.mapStatusUpdateTime.clear();
        } else if (v.getId() == R.id.buttonLockUnlockMerge) {
            mbLockState = !mbLockState;
            if (mbLockState) {
                TeacherPadApplication.IMThread.SendLockRequest(null);
                TeacherPadApplication.mapStatusUpdateTime.clear();
            } else {
                TeacherPadApplication.IMThread.SendUnLockRequest(null);
                TeacherPadApplication.mapStatusUpdateTime.clear();
            }
            if (v instanceof ImageView) {
                ImageView button = (ImageView) v;
                if (mbLockState) {
                    button.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_unlock).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
                } else {
                    button.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_lock).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
                }
            }
        } else if (v.getId() == R.id.buttonVolumnDown) {
            TeacherPadApplication.IMThread.SendMessage("Volumn Down");
        } else if (v.getId() == R.id.buttonVolumnUp) {
            TeacherPadApplication.IMThread.SendMessage("Volumn Up");
        } else if (v.getId() == R.id.buttonPlayMerge || v.getId() == R.id.buttonSeekBar) {
            ResourceItemData currentItem = getTopResource(TeacherPadApplication.mActiveScreenID);
            if (currentItem != null) {
                new PlayPopupWindow(this.mContext, currentItem).show(v);
            }
        } else if (v.getId() == R.id.buttonPlay) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Play " + String.valueOf(TeacherPadApplication.mActiveScreenID));
        } else if (v.getId() == R.id.buttonBestFit) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Reset " + String.valueOf(TeacherPadApplication.mActiveScreenID));
        } else if (v.getId() == R.id.buttonStop) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Stop " + String.valueOf(TeacherPadApplication.mActiveScreenID));
        } else if (v.getId() == R.id.buttonPrev) {
            if (CurrentScreenDisplayActivity != null) {
                CurrentScreenDisplayActivity.cleanLocalDrawView();
            }
            TeacherPadApplication.IMThread.SendMessage("PlayControl Prev " + String.valueOf(TeacherPadApplication.mActiveScreenID));
        } else if (v.getId() == R.id.buttonNext) {
            if (CurrentScreenDisplayActivity != null) {
                CurrentScreenDisplayActivity.cleanLocalDrawView();
            }
            TeacherPadApplication.IMThread.SendMessage("PlayControl Next " + String.valueOf(TeacherPadApplication.mActiveScreenID));
        } else if (v.getId() == R.id.buttonZoomout) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Zoomout " + String.valueOf(TeacherPadApplication.mActiveScreenID));
        } else if (v.getId() == R.id.buttonZoomin) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Zoomin " + String.valueOf(TeacherPadApplication.mActiveScreenID));
        } else if (v.getId() == R.id.buttonStartQuestion) {
            startQuestionRequest();
        } else if (v.getId() == R.id.buttonModeMerge) {
            activity = UI.getCurrentActivity();
            if (activity instanceof ScreenDisplayActivity) {
                new ModePopupWindow((ScreenDisplayActivity) activity).show(v);
            }
        } else if (v.getId() == R.id.buttonColor) {
            if (CurrentScreenDisplayActivity != null) {
                final ScreenDisplayActivity ScreenDisplayActivity = CurrentScreenDisplayActivity;
                ScreenDisplayActivity.setCursorMode(false);
                ScreenDisplayActivity.setPenMode(true);
                PencialPopupWindow colorWindow = new PencialPopupWindow(ScreenDisplayActivity, ScreenDisplayActivity.getPenWidth(), ScreenDisplayActivity.getPenColor());
                colorWindow.setCallBack(new OnChangeCallBack() {
                    public void onDataChanged(int nWidth, int nColor) {
                        ScreenDisplayActivity.setCursorMode(false);
                        ScreenDisplayActivity.setPenMode(true);
                        ScreenDisplayActivity.setPenColor(nColor);
                        ScreenDisplayActivity.setPenWidth(nWidth);
                        ScreenDisplayActivity.sendWhiteBoardInfo();
                    }
                });
                colorWindow.show(v);
            }
        } else if (v.getId() == R.id.buttonProjectToScreen) {
            if (UI.ScreenJpegServer == null) {
                UI.ScreenJpegServer = MyiBaseApplication.createMJpegServer();
                UI.ScreenJpegServer.SetSendOnlyDiff(true);
                UI.ScreenJpegServer.start();
            }
            UI.ScreenJpegServer.CleanImageData();
            TeacherPadApplication.projectToMonitor(new StringBuilder(String.valueOf(Utilities.getWifiIP(MyiBaseApplication.getBaseAppContext()))).append(":8082").toString());
        } else if (v.getId() == R.id.buttonResourceList) {
            activity = UI.getCurrentActivity();
            if (activity != null && (activity instanceof BaseActivity)) {
                BaseActivity activity2 = (BaseActivity) activity;
                DialogFragment resourceListDialog = new ResourcesListDialog2();
                resourceListDialog.setCancelable(true);
                Utilities.showDialog(activity2, resourceListDialog, "resourceListDialog");
            }
        } else if (v.getId() == R.id.buttonReturn) {
            String szTopGUID = getTopResourceGUID(TeacherPadApplication.mActiveScreenID);
            if (!(szTopGUID == null || szTopGUID.indexOf(":") == -1)) {
                TeacherPadApplication.IMThread.SendMessage("CloseScreen " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                TeacherPadApplication.IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            }
            boolean bNeedLaunch = true;
            if (UI.getCurrentActivity() != null && ((UI.getCurrentActivity() instanceof ScreenDisplayActivity) || (UI.getCurrentActivity() instanceof MasterControlActivity))) {
                bNeedLaunch = false;
            }
            if (bNeedLaunch) {
                r0 = new Intent(this.mContext, ScreenDisplayActivity.class);
                r0.putExtra("MJpegServer", TeacherPadApplication.getMJpegURL());
                r0.setFlags(67108864);
                this.mContext.startActivity(r0);
            }
        } else if (v.getId() == R.id.buttonReport) {
            if (TeacherPadApplication.ClassMultiQuestions.getSize() == 0) {
                new Builder(this.mContext).setTitle("无法显示报表").setMessage("当前没有发起过做题，无法显示报表。").setPositiveButton("确定", null).show();
            } else if (UI.getCurrentActivity() == null || !(UI.getCurrentActivity() instanceof ReportActivity2)) {
                Utilities.launchIntent(new Intent(this.mContext, ReportActivity2.class));
            }
        } else if (v.getId() == R.id.buttonFullScreen) {
            r0 = new Intent(this.mContext, ScreenDisplayActivity.class);
            r0.putExtra("MJpegServer", TeacherPadApplication.getMJpegURL());
            this.mContext.startActivity(r0);
        } else if (v.getId() == R.id.buttonWhiteBoard) {
            if (CurrentScreenDisplayActivity != null) {
                CurrentScreenDisplayActivity.setCursorMode(false);
                CurrentScreenDisplayActivity.setPenMode(true);
                CurrentScreenDisplayActivity.sendWhiteBoardInfo();
            }
        } else if (v.getId() == R.id.buttonMouse) {
            if (CurrentScreenDisplayActivity != null) {
                CurrentScreenDisplayActivity.setCursorMode(true);
            }
        } else if (v.getId() == R.id.buttonErase) {
            if (CurrentScreenDisplayActivity != null) {
                CurrentScreenDisplayActivity.setCursorMode(false);
                CurrentScreenDisplayActivity.setPenMode(false);
            }
        } else if (v.getId() == R.id.buttonClearWhiteBoard) {
            if (CurrentScreenDisplayActivity != null) {
                CurrentScreenDisplayActivity.clearWhiteBoard();
                CurrentScreenDisplayActivity.sendWhiteBoardInfo();
                return;
            }
            TeacherPadApplication.IMThread.SendMessage("ClearWhiteBoard " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        } else if (v.getId() == R.id.buttonDesktop) {
            ScreenDisplayActivity.showDesktop();
        } else if (v.getId() == R.id.buttonCamera) {
            ScreenDisplayActivity.startCamera();
        } else if (v.getId() == R.id.buttonBroadcast) {
            ArrayList<String> arrOptionTexts = new ArrayList();
            String[] arrNames = new String[0];
            activity = UI.getCurrentActivity();
            if (activity != null) {
                arrOptionTexts.add("广播静态照片");
                if (VERSION.SDK_INT >= 21 && (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_H264) || MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_H264_RECEIVEONLY))) {
                    arrOptionTexts.add("实时广播大屏幕 - 自由模式，学生可以缩小或关闭直播窗口");
                    arrOptionTexts.add("实时广播大屏幕 - 锁定模式，学生不能关闭直播窗口");
                }
                if (arrOptionTexts.size() > 1) {
                    new Builder(new ContextThemeWrapper(activity, 16974130)).setItems((String[]) arrOptionTexts.toArray(arrNames), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                TeacherPadApplication.IMThread.SendMessage("MasterScreenCaptureURL: " + TeacherPadApplication.getMJpegURL() + "/");
                                TeacherPadApplication.IMThread.SendMessage("ScreenCopyAndSend HandWriteNoUpload");
                            } else if (which == 1) {
                                if (TeacherPadApplication.mMJpegRelayToMulticastThread == null || !TeacherPadApplication.mMJpegRelayToMulticastThread.isReady()) {
                                    TeacherPadApplication.IMThread.SendMessage("H264Stream " + TeacherPadApplication.getMJpegURL());
                                    return;
                                }
                                TeacherPadApplication.IMThread.SendMessage(String.format("H264MulticastStream %s %s %d %d %d", new Object[]{TeacherPadApplication.szCurrentMulticastAddress, Utilities.getWifiIP(StartClassControlUnit.this.mContext), Integer.valueOf(TeacherPadApplication.mMJpegRelayToMulticastThread.getPort()), Integer.valueOf(TeacherPadApplication.mMJpegRelayToMulticastThread.getWidth()), Integer.valueOf(TeacherPadApplication.mMJpegRelayToMulticastThread.getHeight())}));
                            } else if (which == 2) {
                                TeacherPadApplication.IMThread.SendMessage("H264StreamLocked " + TeacherPadApplication.getMJpegURL());
                            }
                        }
                    }).setTitle("选择动作").create().show();
                    return;
                }
                TeacherPadApplication.IMThread.SendMessage("MasterScreenCaptureURL: " + TeacherPadApplication.getMJpegURL() + "/");
                TeacherPadApplication.IMThread.SendMessage("ScreenCopyAndSend HandWriteNoUpload");
            }
        } else if (v.getId() == R.id.buttonBroadcastReal) {
            TeacherPadApplication.IMThread.SendMessage("MasterScreenCaptureKey: " + TeacherPadApplication.szPCScreenKey);
            TeacherPadApplication.IMThread.SendMessage("MasterScreenCaptureURL: " + TeacherPadApplication.getPCScreenCaptureURL());
            if (TeacherPadApplication.mMJpegRelayToMulticastThread == null || !TeacherPadApplication.mMJpegRelayToMulticastThread.isReady()) {
                TeacherPadApplication.IMThread.SendMessage("H264Stream " + TeacherPadApplication.getMJpegURL());
            } else {
                TeacherPadApplication.IMThread.SendMessage(String.format("H264MulticastStream %s %s %d %d %d", new Object[]{TeacherPadApplication.szCurrentMulticastAddress, Utilities.getWifiIP(this.mContext), Integer.valueOf(TeacherPadApplication.mMJpegRelayToMulticastThread.getPort()), Integer.valueOf(TeacherPadApplication.mMJpegRelayToMulticastThread.getWidth()), Integer.valueOf(TeacherPadApplication.mMJpegRelayToMulticastThread.getHeight())}));
            }
        } else if (v.getId() == R.id.buttonMaximize) {
            TeacherPadApplication.IMThread.SendMessage("MaxScreen " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        } else if (v.getId() == R.id.buttonRestore) {
            TeacherPadApplication.IMThread.SendMessage("RestoreScreen " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        } else if (v.getId() == R.id.buttonClose) {
            TeacherPadApplication.IMThread.SendMessage("CloseScreen " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            TeacherPadApplication.IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        } else if (Utilities.isInArray(arrBoardIDs, v.getId(), nBoardIndex)) {
            String szGUID = Utilities.createGUID();
            int nColor = this.mContext.getResources().getIntArray(R.array.whiteboards_colors)[nBoardIndex[0].intValue()];
            String szIP = Utilities.getWifiIP(this.mContext);
            String szMessage = String.format("OpenWhiteBoard: %s http://%s/%d.whiteboard", new Object[]{szGUID, szIP, Integer.valueOf(nColor)});
            TeacherPadApplication.mapResourceTitles.put(szGUID, "白板");
            TeacherPadApplication.IMThread.sendMessageToMonitor(szMessage, true);
        }
    }

    public static void processCommandWithScreen(View v) {
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            processCommandWithScreen(v, i);
        }
    }

    public static void processCommandWithScreen(View v, int nScreenID) {
        ScreenDisplayActivity CurrentScreenDisplayActivity = null;
        if (UI.getCurrentActivity() != null) {
            Activity activity = UI.getCurrentActivity();
            if (activity instanceof ScreenDisplayActivity) {
                CurrentScreenDisplayActivity = (ScreenDisplayActivity) activity;
            }
        }
        if (v.getId() == R.id.buttonPlay) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Play " + String.valueOf(nScreenID));
        } else if (v.getId() == R.id.buttonStop) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Stop " + String.valueOf(nScreenID));
        } else if (v.getId() == R.id.buttonPrev) {
            if (CurrentScreenDisplayActivity != null) {
                CurrentScreenDisplayActivity.cleanLocalDrawView();
            }
            TeacherPadApplication.IMThread.SendMessage("PlayControl Prev " + String.valueOf(nScreenID));
        } else if (v.getId() == R.id.buttonNext) {
            if (CurrentScreenDisplayActivity != null) {
                CurrentScreenDisplayActivity.cleanLocalDrawView();
            }
            TeacherPadApplication.IMThread.SendMessage("PlayControl Next " + String.valueOf(nScreenID));
        } else if (v.getId() == R.id.buttonZoomout) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Zoomout " + String.valueOf(nScreenID));
        } else if (v.getId() == R.id.buttonZoomin) {
            TeacherPadApplication.IMThread.SendMessage("PlayControl Zoomin " + String.valueOf(nScreenID));
        }
    }

    public static void processSeekToCommandWithScreen(String szResourceGUIDOrUrl, int nPos) {
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            if (isResourceOnTop(szResourceGUIDOrUrl, i)) {
                TeacherPadApplication.IMThread.SendMessage("SetPlayPos " + String.valueOf(nPos) + " " + String.valueOf(i));
            }
        }
    }

    public static boolean isResourceInScreen(String szGUIDOrUrl, int nScreenID) {
        if (nScreenID < 0 || nScreenID >= TeacherPadApplication.marrMonitors.size()) {
            return false;
        }
        return ((MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID)).isResourceExsit(szGUIDOrUrl);
    }

    public static boolean isResourceOnTop(String szGUIDOrUrl) {
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            if (isResourceOnTop(szGUIDOrUrl, i)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isResourceOnTop(String szGUIDOrUrl, int nScreenID) {
        if (nScreenID >= 0 && nScreenID < TeacherPadApplication.marrMonitors.size()) {
            MultiScreen oneScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID);
            if (oneScreen.arrPlayStack.size() > 0 && ((String) oneScreen.arrPlayStack.get(0)).equalsIgnoreCase(szGUIDOrUrl)) {
                return true;
            }
        }
        return false;
    }

    public static ResourceItemData getTopResource(int nScreenID) {
        if (nScreenID >= 0 && nScreenID < TeacherPadApplication.marrMonitors.size()) {
            MultiScreen oneScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID);
            if (oneScreen.arrPlayStack.size() > 0) {
                String szGUID = (String) oneScreen.arrPlayStack.get(0);
                for (int i = 0; i < TeacherPadApplication.arrResourceData.size(); i++) {
                    if (((ResourceItemData) TeacherPadApplication.arrResourceData.get(i)).szGUID.equalsIgnoreCase(szGUID)) {
                        return (ResourceItemData) TeacherPadApplication.arrResourceData.get(i);
                    }
                }
            }
        }
        return null;
    }

    public static String getTopResourceGUID(int nScreenID) {
        if (nScreenID >= 0 && nScreenID < TeacherPadApplication.marrMonitors.size()) {
            MultiScreen oneScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID);
            if (oneScreen.arrPlayStack.size() > 0) {
                return (String) oneScreen.arrPlayStack.get(0);
            }
        }
        return null;
    }

    public static int getResourcePlayFlags(String szGUIDOrUrl) {
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            MultiScreen oneScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(i);
            for (int j = 0; j < oneScreen.arrPlayStack.size(); j++) {
                if (((String) oneScreen.arrPlayStack.get(j)).equalsIgnoreCase(szGUIDOrUrl)) {
                    return ((Integer) oneScreen.arrPlayStackFlags.get(j)).intValue();
                }
            }
        }
        return 0;
    }

    public static PlayPos getResourcePlayPos(String szGUIDOrUrl) {
        PlayPos pos = null;
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            MultiScreen oneScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(i);
            for (int j = 0; j < oneScreen.arrPlayStack.size(); j++) {
                if (((String) oneScreen.arrPlayStack.get(j)).equalsIgnoreCase(szGUIDOrUrl)) {
                    if (pos != null) {
                        return null;
                    }
                    pos = (PlayPos) oneScreen.arrPlayPos.get(j);
                }
            }
        }
        return pos;
    }

    public static void closeResourceFromScreen(String szGUIDOrUrl, int nScreen) {
        TeacherPadApplication.IMThread.SendMessage("CloseResourceFromScreen " + szGUIDOrUrl + " " + String.valueOf(nScreen), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            TeacherPadApplication.IMThread.SendMessage("SetPlayPos " + String.valueOf(progress));
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.m_bNoUpdatePlayPos = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.m_bNoUpdatePlayPos = false;
    }

    private void startQuestion() {
        TeacherPadApplication.IMThread.SendMessage("MasterScreenCaptureURL: " + TeacherPadApplication.getPCScreenCaptureURL());
        TeacherPadApplication.IMThread.SendMessage(IMService.buildMessage(TeacherPadApplication.szCurrentQuestionIMMessage, Utilities.createGUID(), HttpStatus.SC_MULTIPLE_CHOICES));
        TeacherPadApplication.mapStudentsQuestionAnswers.clear();
        TeacherPadApplication.mapStudentsAnswerTime.clear();
        TeacherPadApplication.marrStudentAnswers.clear();
        TeacherPadApplication.MasterAdapter.CleanPadAnswer();
        TeacherPadApplication.bAllowQuestionAnswer = true;
        TeacherPadApplication.nQuestionStartTime = System.currentTimeMillis();
        PicassoTools.clearCache(Picasso.with(this.mContext));
        Intent intent = new Intent(this.mContext, ReportActivity2.class);
        intent.setFlags(67108864);
        Utilities.launchIntent(intent);
    }

    public static void launchPC() {
        IMService.getIMService().sendMessage(Utilities.getNow() + " " + IMService.getIMUserName() + ": " + String.format("StartClass " + TeacherPadApplication.szScheduleGUID, new Object[0]), TeacherPadApplication.szPCSessionID);
        IMService.addDirectIMClient(TeacherPadApplication.szPCIP, MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        TeacherPadApplication.ClassMultiQuestions.setLocalPCAddress(TeacherPadApplication.szPCIP);
        processPCStatus();
        if (!TeacherPadApplication.szCurrentMulticastAddress.isEmpty() && !TeacherPadApplication.szPCIP.isEmpty()) {
            if (TeacherPadApplication.mMJpegRelayToMulticastThread != null) {
                Log.d(TAG, "Shutdown previous PC screen multicast relay service.");
                TeacherPadApplication.mMJpegRelayToMulticastThread.stopThread();
                TeacherPadApplication.mMJpegRelayToMulticastThread = null;
            }
            Log.d(TAG, "Reactive PC screen multicast relay service.");
            TeacherPadApplication.mMJpegRelayToMulticastThread = new MJpegRelayToMulticastThread(MyiBaseApplication.getBaseAppContext(), TeacherPadApplication.getMJpegURL(), TeacherPadApplication.szCurrentMulticastAddress, 8910, TeacherPadApplication.mInstance);
            TeacherPadApplication.mMJpegRelayToMulticastThread.start();
        }
    }

    public static void processPCStatus() {
        String[] arrStatus = TeacherPadApplication.szPCStatus.split(" ");
        if (arrStatus.length >= 3) {
            TeacherPadApplication.szPCScreenKey = arrStatus[2];
        }
    }

    public static void showSelectPCDialog() {
        Activity activity = UI.getCurrentActivity();
        if (activity instanceof BaseActivity) {
            BaseActivity baseActivty = (BaseActivity) activity;
            SelectPCDialog selectPCDialog = new SelectPCDialog();
            FragmentTransaction ft = baseActivty.getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            selectPCDialog.setCancelable(false);
            selectPCDialog.setCallBack(new AnonymousClass3(selectPCDialog));
            selectPCDialog.setDismissCallBack(new SelectPCDialogDismissCallBack() {
                public void onDismiss() {
                    StartClassControlUnit.ReturnToClassSelectIntent();
                }
            });
            selectPCDialog.setScheduleGUID(TeacherPadApplication.szScheduleGUID);
            selectPCDialog.show(ft, "selectPCDialog");
            if (TeacherPadApplication.mMJpegRelayToMulticastThread != null) {
                Log.d(TAG, "Shutdown previous PC screen multicast relay service.");
                TeacherPadApplication.mMJpegRelayToMulticastThread.stopThread();
                TeacherPadApplication.mMJpegRelayToMulticastThread = null;
            }
        }
    }

    public static void ReturnToClassSelectIntent() {
        MasterControlActivity.stopClass();
        TeacherPadApplication.szCurrentClassGUID = "";
        TeacherPadApplication.szPCSessionID = "";
        TeacherPadApplication.szScheduleGUID = "";
        Intent intent = new Intent(MyiBaseApplication.getBaseAppContext(), MainActivity.class);
        intent.setFlags(67108864);
        Utilities.launchIntent(intent);
    }

    public static boolean switchToResource(String szGUID, int nScreenID) {
        TeacherPadApplication.szCorrectAnswer = "";
        TeacherPadApplication.szCurrentQuestionIMMessage = "";
        if (TeacherPadApplication.mapResourcePlayPos.containsKey(szGUID)) {
            TeacherPadApplication.IMThread.SendMessage("SwitchToResource " + szGUID + " " + String.valueOf(((Integer) TeacherPadApplication.mapResourcePlayPos.get(szGUID)).intValue()) + " " + String.valueOf(nScreenID));
        } else {
            TeacherPadApplication.IMThread.SendMessage("SwitchToResource " + szGUID + " " + String.valueOf(-1) + " " + String.valueOf(nScreenID));
        }
        return true;
    }

    public static boolean switchToResource(String szGUID, int nType, int nScreenID) {
        TeacherPadApplication.szCorrectAnswer = "";
        TeacherPadApplication.szCurrentQuestionIMMessage = "";
        TeacherPadApplication.IMThread.SendMessage("SwitchToResource " + szGUID + " " + String.valueOf(nType) + " " + String.valueOf(nScreenID));
        return true;
    }

    public void startQuestionRequest() {
        if (UI.getCurrentActivity() != null && (UI.getCurrentActivity() instanceof FragmentActivity)) {
            final String szCurrentQuestionGroupGUID = Utilities.createGUID() + "=" + String.valueOf(TeacherPadApplication.ClassMultiQuestions.getSize() + 1);
            AskQuestionDialog.showDialog((FragmentActivity) UI.getCurrentActivity(), TeacherPadApplication.szCurrentClassGUID, new OnStartAskQuestionCallBack() {
                public void OnStartQuestion(AskQuestionDialog dialog, String szIMMessage, String szCorrectAnswer, String szTargetJID, int nQuestionType) {
                    TeacherPadApplication.IMThread.SendMessage("CorrectAnswer " + szCurrentQuestionGroupGUID + " " + szCorrectAnswer);
                    TeacherPadApplication.mapStudentQuestionReceived.clear();
                    TeacherPadApplication.ClassMultiQuestions.addClassQuestion(szCurrentQuestionGroupGUID, nQuestionType, szCorrectAnswer);
                    TeacherPadApplication.ClassMultiQuestions.switchToLastQuestion();
                    String[] arrIMMessage = szIMMessage.split("\n");
                    if (arrIMMessage.length >= 2) {
                        for (int i = 0; i < arrIMMessage.length - 1; i++) {
                            if (arrIMMessage[i].contains("CountDown")) {
                                TeacherPadApplication.bAllowCountDownMessage = true;
                            }
                            if (szTargetJID.isEmpty()) {
                                TeacherPadApplication.IMThread.SendMessage(arrIMMessage[i]);
                            } else {
                                TeacherPadApplication.IMThread.SendMessage(arrIMMessage[i], new StringBuilder(String.valueOf(szTargetJID)).append(";").toString());
                            }
                        }
                        szIMMessage = arrIMMessage[arrIMMessage.length - 1];
                    }
                    if (!szTargetJID.isEmpty()) {
                        szTargetJID = " " + szTargetJID;
                    }
                    TeacherPadApplication.szCurrentQuestionIMMessage = new StringBuilder(String.valueOf(szIMMessage)).append(szTargetJID).toString();
                    TeacherPadApplication.szCorrectAnswer = szCorrectAnswer;
                    TeacherPadApplication.szLastStudentAnswerJSON = "";
                    TeacherPadApplication.marrRequiredStudentAnswer.clear();
                    Iterator it = dialog.getNeedAnswerStudents().iterator();
                    while (it.hasNext()) {
                        StudentInfo studentInfo = (StudentInfo) it.next();
                        StudentAnswer studentAnswer = new StudentAnswer();
                        studentAnswer.szStudentJID = studentInfo.szJID;
                        studentAnswer.szStudentName = studentInfo.szRealName;
                        TeacherPadApplication.marrRequiredStudentAnswer.add(studentAnswer);
                    }
                    StartClassControlUnit.this.startQuestion();
                }
            }, TeacherPadApplication.szCurrentQuestionIMMessage, TeacherPadApplication.szCorrectAnswer, szCurrentQuestionGroupGUID, "发起做题 - 第" + String.valueOf(TeacherPadApplication.ClassMultiQuestions.getSize() + 1) + "题");
        }
    }
}
