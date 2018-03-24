package com.netspace.teacherpad.im;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMService;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.servers.AudioServer;
import com.netspace.library.service.CountDownService;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.teacherpad.QuestionCaptureActivity;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.ScreenDisplayActivity;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.fragments.ResourcesListFragment;
import com.netspace.teacherpad.modules.startclass.ReportActivity2;
import com.netspace.teacherpad.structure.MultiScreen;
import com.netspace.teacherpad.structure.PlayPos;
import java.util.ArrayList;
import java.util.Iterator;
import net.sqlcipher.database.SQLiteDatabase;

public class IMMessageProcess {
    private static String Tag = "IMMessageProcess";
    private Context m_Context;
    private boolean mbLastHasWindowMaxmized = false;
    private String mszLastGetScreenLayoutResult;

    public IMMessageProcess(Context Context) {
        this.m_Context = Context;
    }

    public void ProcessIMGroupChatMessage(String szMessageBody, String szFrom) {
        if (TeacherPadApplication.IMThread != null) {
            if (szFrom.lastIndexOf("/") != -1) {
                szFrom = szFrom.substring(szFrom.lastIndexOf("/") + 1);
            }
            if (szFrom.indexOf("myipad") != -1) {
                szFrom = szFrom.substring(szFrom.indexOf("_") + 1);
            }
            if (!szFrom.isEmpty()) {
                boolean bFromPC = false;
                if (szFrom.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                    bFromPC = true;
                }
                szMessageBody = szMessageBody.replace("\r", "").replace("\n", "");
                Activity CurrentActivity = UI.getCurrentActivity();
                String[] arrData = szMessageBody.split(" ");
                String szVerb = arrData[0].replaceAll(":", "");
                if (!((szVerb.equalsIgnoreCase("ScreenCaptureResult") || szVerb.equalsIgnoreCase("ScreenCaptureKeyResult")) && arrData.length == 2)) {
                    if (szVerb.equalsIgnoreCase("StatusResult")) {
                        if (szMessageBody.length() > szVerb.length() + 1) {
                            String szData = szMessageBody.substring(szVerb.length() + 1);
                            synchronized (TeacherPadApplication.mapStatus) {
                                TeacherPadApplication.mapStatus.put(szFrom, szData);
                            }
                            TeacherPadApplication.mapStatusUpdateTime.put(szFrom, Integer.valueOf((int) System.currentTimeMillis()));
                        }
                    } else if (szVerb.equalsIgnoreCase("GetPlayPosResult") && bFromPC) {
                        TeacherPadApplication.mapIMCommandsResponse.put(szVerb, szMessageBody);
                        arrData2 = szMessageBody.split(" ");
                        nPlayPos = Integer.valueOf(arrData2[arrData2.length - 2]).intValue();
                        nTotalPos = Integer.valueOf(arrData2[arrData2.length - 1]).intValue();
                        if (!(TeacherPadApplication.szCurrentPlayingGUID == null || TeacherPadApplication.szCurrentPlayingGUID.isEmpty() || nTotalPos == -1 || nPlayPos == -1)) {
                            TeacherPadApplication.mapResourcePlayPos.put(TeacherPadApplication.szCurrentPlayingGUID, Integer.valueOf(nPlayPos));
                        }
                    } else if (szVerb.equalsIgnoreCase("PlayStatsResult") && bFromPC && !TeacherPadApplication.szCurrentClassGUID.isEmpty()) {
                        arrData2 = szMessageBody.split(" ");
                        nPlayPos = Integer.valueOf(arrData2[2]).intValue();
                        nTotalPos = Integer.valueOf(arrData2[3]).intValue();
                        String szWhiteboardStates = arrData2[1];
                        if (arrData2.length >= 6) {
                            TeacherPadApplication.szPCScheduleGUID = arrData2[5];
                        }
                        if (szWhiteboardStates.equalsIgnoreCase("whiteboardvisible")) {
                            TeacherPadApplication.bWhiteBoardOn = true;
                        } else {
                            TeacherPadApplication.bWhiteBoardOn = false;
                        }
                        if (arrData2.length > 4) {
                            if (!(TeacherPadApplication.szCurrentPlayingGUID == null || TeacherPadApplication.szCurrentPlayingGUID.isEmpty() || TeacherPadApplication.szCurrentPlayingGUID.indexOf(":") != -1 || TeacherPadApplication.szLastResourceGUID.equalsIgnoreCase(TeacherPadApplication.szCurrentPlayingGUID))) {
                                TeacherPadApplication.szLastResourceGUID = TeacherPadApplication.szCurrentPlayingGUID;
                            }
                            TeacherPadApplication.szCurrentPlayingGUID = arrData[4];
                            TeacherPadApplication.mapIMCommandsResponse.put("Playing", "Playing " + TeacherPadApplication.szCurrentPlayingGUID);
                        }
                        if (!(TeacherPadApplication.szCurrentPlayingGUID == null || TeacherPadApplication.szCurrentPlayingGUID.isEmpty() || nTotalPos == -1 || nPlayPos == -1)) {
                            TeacherPadApplication.mapResourcePlayPos.put(TeacherPadApplication.szCurrentPlayingGUID, Integer.valueOf(nPlayPos));
                        }
                        TeacherPadApplication.mapIMCommandsResponse.put("GetPlayPosResult", "GetPlayPosResult " + String.valueOf(nPlayPos) + " " + String.valueOf(nTotalPos));
                    } else if (szVerb.equalsIgnoreCase("GetScreenPlayStackResult") && arrData.length == 2 && bFromPC) {
                        String[] arrScreenData = arrData[1].split(";", -1);
                        boolean bPlayStackModified = false;
                        boolean bPlayPosChanged = false;
                        i = 0;
                        while (i < arrScreenData.length && i < TeacherPadApplication.marrMonitors.size()) {
                            int j;
                            String[] arrPlayGUIDAndFlags = arrScreenData[i].split(",");
                            MultiScreen screen = (MultiScreen) TeacherPadApplication.marrMonitors.get(i);
                            ArrayList<String> arrNewPlayStack = new ArrayList();
                            ArrayList<Integer> arrNewPlayStackFlags = new ArrayList();
                            ArrayList<PlayPos> arrNewPlayPos = new ArrayList();
                            if (arrPlayGUIDAndFlags.length >= 2) {
                                for (j = 0; j < arrPlayGUIDAndFlags.length; j += 4) {
                                    String szResourceGUID = arrPlayGUIDAndFlags[j];
                                    arrNewPlayStack.add(arrPlayGUIDAndFlags[j]);
                                    arrNewPlayStackFlags.add(Integer.valueOf(Utilities.toInt(arrPlayGUIDAndFlags[j + 1])));
                                    PlayPos playpos = new PlayPos();
                                    if (arrPlayGUIDAndFlags.length > j + 3) {
                                        playpos.nPos = Utilities.toInt(arrPlayGUIDAndFlags[j + 2]);
                                        playpos.nLength = Utilities.toInt(arrPlayGUIDAndFlags[j + 3]);
                                    }
                                    arrNewPlayPos.add(playpos);
                                    Iterator it = TeacherPadApplication.arrResourceData.iterator();
                                    while (it.hasNext()) {
                                        ResourceItemData oneItem = (ResourceItemData) it.next();
                                        if (oneItem.szGUID.equalsIgnoreCase(szResourceGUID) && oneItem.bLoaded) {
                                            break;
                                        } else if (!(!oneItem.szGUID.equalsIgnoreCase(szResourceGUID) || oneItem.bLoaded || oneItem.nType == 0)) {
                                            final ResourceItemData Item = oneItem;
                                            ResourceItemObject ResourceObject = new ResourceItemObject(oneItem.szGUID, null);
                                            ResourceObject.setSuccessListener(new OnSuccessListener() {
                                                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                                    Item.bLoaded = true;
                                                    ResourceParser ResourceParser = new ResourceParser();
                                                    if (ResourceParser.initialize(IMMessageProcess.this.m_Context, ItemObject.readTextData())) {
                                                        ArrayList<String> arrThumbnails = ResourceParser.getPreviewThumbnailURLs();
                                                        if (arrThumbnails != null) {
                                                            Item.arrThumbnailUrls = arrThumbnails;
                                                            ResourcesListFragment.refrehResourceAdapter();
                                                        }
                                                    }
                                                }
                                            });
                                            ResourceObject.setFailureListener(new OnFailureListener() {
                                                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                                }
                                            });
                                            ResourceObject.setAllowCache(false);
                                            ResourceObject.setAlwaysActiveCallbacks(true);
                                            VirtualNetworkObject.addToQueue(ResourceObject);
                                        }
                                    }
                                }
                            }
                            if (arrNewPlayStack.size() == screen.arrPlayStack.size()) {
                                for (j = 0; j < arrNewPlayStack.size(); j++) {
                                    if (((String) arrNewPlayStack.get(j)).equalsIgnoreCase((String) screen.arrPlayStack.get(j))) {
                                        PlayPos pos1 = (PlayPos) arrNewPlayPos.get(j);
                                        PlayPos pos2 = (PlayPos) screen.arrPlayPos.get(j);
                                        if (!(pos1.nLength == pos2.nLength && pos1.nPos == pos2.nPos)) {
                                            bPlayPosChanged = true;
                                        }
                                    } else {
                                        bPlayStackModified = true;
                                    }
                                    if (bPlayPosChanged && bPlayStackModified) {
                                        break;
                                    }
                                }
                            } else {
                                bPlayStackModified = true;
                            }
                            screen.arrPlayStack.clear();
                            screen.arrPlayStack.addAll(arrNewPlayStack);
                            screen.arrPlayStackFlags.clear();
                            screen.arrPlayStackFlags.addAll(arrNewPlayStackFlags);
                            screen.arrPlayPos.clear();
                            screen.arrPlayPos.addAll(arrNewPlayPos);
                            i++;
                        }
                        if (bPlayPosChanged || bPlayStackModified) {
                            ResourcesListFragment.refrehResourceAdapter();
                        }
                        if ((bPlayPosChanged || bPlayStackModified) && (CurrentActivity instanceof ScreenDisplayActivity)) {
                            ((ScreenDisplayActivity) CurrentActivity).updateDisplay();
                        }
                    } else if (szVerb.equalsIgnoreCase("GetScreenLayoutResult") && arrData.length >= 4 && bFromPC) {
                        String[] arrSourceCoordinates = arrData[1].split(";");
                        String[] arrMaxStates = arrData[2].split(";");
                        String[] arrWindowCoordinate = arrData[3].split(";");
                        boolean bHasWindowMaxmized = false;
                        int nMaxmizedWindowButtonID = 0;
                        boolean bMenuNeedChange = false;
                        if (arrData.length == 5) {
                            TeacherPadApplication.mszActiveMonitorLayoutTags = arrData[4];
                        }
                        if (arrSourceCoordinates.length == arrMaxStates.length && arrWindowCoordinate.length == arrMaxStates.length) {
                            if (TeacherPadApplication.marrMonitors.size() != arrWindowCoordinate.length) {
                                bMenuNeedChange = true;
                            }
                            for (i = 0; i < arrSourceCoordinates.length; i++) {
                                Rect rect;
                                MultiScreen oneScreen = new MultiScreen();
                                String[] szCoordinates = arrSourceCoordinates[i].split(",");
                                if (TeacherPadApplication.marrMonitors.size() > i) {
                                    oneScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(i);
                                } else {
                                    TeacherPadApplication.marrMonitors.add(oneScreen);
                                    oneScreen.nCurrentFunctionButtonID = R.id.buttonWhiteBoard;
                                }
                                if (szCoordinates.length == 4) {
                                    rect = new Rect();
                                    rect.left = Integer.valueOf(szCoordinates[0]).intValue();
                                    rect.top = Integer.valueOf(szCoordinates[1]).intValue();
                                    rect.right = Integer.valueOf(szCoordinates[2]).intValue();
                                    rect.bottom = Integer.valueOf(szCoordinates[3]).intValue();
                                    oneScreen.rectScreen = rect;
                                }
                                if (Integer.valueOf(arrMaxStates[i]).intValue() == 1) {
                                    if (!oneScreen.bMaximized) {
                                        bMenuNeedChange = true;
                                    }
                                    oneScreen.bMaximized = true;
                                    bHasWindowMaxmized = true;
                                    nMaxmizedWindowButtonID = oneScreen.nCurrentFunctionButtonID;
                                    szCoordinates = arrWindowCoordinate[i].split(",");
                                    if (szCoordinates.length == 4) {
                                        rect = new Rect();
                                        rect.left = Integer.valueOf(szCoordinates[0]).intValue();
                                        rect.top = Integer.valueOf(szCoordinates[1]).intValue();
                                        rect.right = Integer.valueOf(szCoordinates[2]).intValue();
                                        rect.bottom = Integer.valueOf(szCoordinates[3]).intValue();
                                        if (rect.left > 0 || rect.top > 0 || rect.right > 0 || rect.bottom > 0) {
                                            oneScreen.rectScreen = rect;
                                        }
                                    }
                                } else {
                                    if (oneScreen.bMaximized) {
                                        bMenuNeedChange = true;
                                    }
                                    oneScreen.bMaximized = false;
                                }
                                TeacherPadApplication.marrMonitors.set(i, oneScreen);
                            }
                            while (arrSourceCoordinates.length < TeacherPadApplication.marrMonitors.size()) {
                                TeacherPadApplication.marrMonitors.remove(arrSourceCoordinates.length);
                            }
                            if (TeacherPadApplication.mActiveScreenID > TeacherPadApplication.marrMonitors.size() - 1) {
                                TeacherPadApplication.mActiveScreenID = TeacherPadApplication.marrMonitors.size() - 1;
                            }
                            if (CurrentActivity != null && (CurrentActivity instanceof ScreenDisplayActivity)) {
                                ScreenDisplayActivity ScreenDisplayActivity = (ScreenDisplayActivity) CurrentActivity;
                                if (!szMessageBody.equalsIgnoreCase(this.mszLastGetScreenLayoutResult)) {
                                    ScreenDisplayActivity.updateDisplay();
                                }
                                if (bMenuNeedChange) {
                                    ScreenDisplayActivity.updateSideMenu();
                                }
                                if (TeacherPadApplication.marrMonitors.size() == 1) {
                                    bHasWindowMaxmized = true;
                                    nMaxmizedWindowButtonID = ((MultiScreen) TeacherPadApplication.marrMonitors.get(0)).nCurrentFunctionButtonID;
                                }
                                if (bHasWindowMaxmized) {
                                    if (nMaxmizedWindowButtonID == R.id.buttonWhiteBoard || nMaxmizedWindowButtonID == R.id.buttonErase) {
                                        ScreenDisplayActivity.setCursorMode(false);
                                    } else {
                                        ScreenDisplayActivity.setCursorMode(true);
                                    }
                                    if (this.mbLastHasWindowMaxmized != bHasWindowMaxmized && PreferenceManager.getDefaultSharedPreferences(this.m_Context).getBoolean("SetToWriteModeOnMaxmize", true)) {
                                        ScreenDisplayActivity.setCursorMode(false);
                                    }
                                } else {
                                    ScreenDisplayActivity.setCursorMode(true);
                                }
                                this.mbLastHasWindowMaxmized = bHasWindowMaxmized;
                            }
                            this.mszLastGetScreenLayoutResult = szMessageBody;
                        } else {
                            Log.e(Tag, "IM Data incorrect while processing " + szMessageBody);
                        }
                    } else if (szVerb.equalsIgnoreCase("ActiveVoice") && arrData.length == 4 && bFromPC) {
                        if (arrData[3].equalsIgnoreCase(new StringBuilder(String.valueOf(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)).append("_teacherpad").toString())) {
                            if (TeacherPadApplication.AudioServer != null) {
                                TeacherPadApplication.AudioServer.StopServer();
                                TeacherPadApplication.AudioServer = null;
                            }
                            TeacherPadApplication.AudioServer = new AudioServer();
                            TeacherPadApplication.AudioServer.InitServer(arrData[1], Integer.valueOf(arrData[2]).intValue());
                        }
                    } else if (szVerb.equalsIgnoreCase("StopVoice") && bFromPC) {
                        if (TeacherPadApplication.AudioServer != null) {
                            TeacherPadApplication.AudioServer.StopServer();
                            TeacherPadApplication.AudioServer = null;
                        }
                    } else if (szVerb.equalsIgnoreCase("Version")) {
                        TeacherPadApplication.IMThread.SendMessage(TeacherPadApplication.szAppVersionName);
                    } else if (szVerb.equalsIgnoreCase("MessageReceived") && arrData.length >= 2) {
                        String szClientID = arrData[1];
                        synchronized (TeacherPadApplication.mapStudentQuestionReceived) {
                            TeacherPadApplication.mapStudentQuestionReceived.put(szClientID, Boolean.TRUE);
                        }
                    } else if (szVerb.equalsIgnoreCase("CountDown")) {
                        if (TeacherPadApplication.bAllowCountDownMessage) {
                            Intent CountDownServiceIntent = new Intent(this.m_Context, CountDownService.class);
                            CountDownServiceIntent.putExtra("operation", 100);
                            CountDownServiceIntent.putExtra("seconds", Utilities.toInt(arrData[1]));
                            CountDownServiceIntent.putExtra("nobackground", true);
                            CountDownServiceIntent.putExtra("timetoend", System.currentTimeMillis() + ((long) (Utilities.toInt(arrData[1]) * 1000)));
                            this.m_Context.startService(CountDownServiceIntent);
                            TeacherPadApplication.bAllowCountDownMessage = false;
                        }
                    } else if (szVerb.equalsIgnoreCase("Playing") && arrData.length == 2 && bFromPC) {
                        TeacherPadApplication.mapIMCommandsResponse.put(szVerb, szMessageBody);
                        TeacherPadApplication.szCurrentPlayingGUID = arrData[1];
                    } else if (szVerb.equalsIgnoreCase("ReportIPResult") && arrData.length == 2) {
                        if (!arrData[1].isEmpty() && Utilities.checkIPValid(arrData[1])) {
                            TeacherPadApplication.mapStudentIP.put(szFrom, arrData[1]);
                            if (szFrom.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                                IMService.addDirectIMClient(arrData[1], szFrom);
                                TeacherPadApplication.ClassMultiQuestions.setLocalPCAddress(arrData[1]);
                            }
                        }
                    } else if (szVerb.equalsIgnoreCase("RateAnswer")) {
                        if (arrData.length >= 2) {
                            if (TeacherPadApplication.ClassMultiQuestions.rateStudentAnswer(arrData[1], arrData[2], szFrom, true)) {
                                activity = UI.getCurrentActivity();
                                if (activity != null && (activity instanceof ReportActivity2)) {
                                    ((ReportActivity2) activity).refreshStudentAnswer();
                                }
                            }
                        }
                    } else if (szVerb.equalsIgnoreCase("UnRateAnswer")) {
                        if (arrData.length >= 2) {
                            if (TeacherPadApplication.ClassMultiQuestions.rateStudentAnswer(arrData[1], arrData[2], szFrom, false)) {
                                activity = UI.getCurrentActivity();
                                if (activity != null && (activity instanceof ReportActivity2)) {
                                    ((ReportActivity2) activity).refreshStudentAnswer();
                                }
                            }
                        }
                    } else if (szVerb.equalsIgnoreCase("ReportIP")) {
                        TeacherPadApplication.IMThread.SendMessage("ReportIPResult: " + Utilities.getWifiIP(this.m_Context));
                        Log.d("IMMessageProcess", "Receive ReportIP Request.");
                    } else if (szVerb.equalsIgnoreCase("ReportWANIP")) {
                        TeacherPadApplication.IMThread.SendMessage("ReportWANIPResult: " + Utilities.getWifiIP(this.m_Context));
                    } else if (szVerb.equalsIgnoreCase("StudentHandsUp")) {
                        if (UI.getCurrentActivity() != null) {
                            String szStudentName = (String) TeacherPadApplication.mapStudentName.get(szFrom);
                            if (szStudentName != null) {
                                final String str = szFrom;
                                new Builder(UI.getCurrentActivity()).setTitle("学生举手").setMessage("学生“" + szStudentName + "”举手了。").setPositiveButton("确定", null).setNegativeButton("打开语音", new OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        TeacherPadApplication.IMThread.SendMessage("ActiveVoice", str);
                                        TeacherPadApplication.switchToStudentAudio(str);
                                    }
                                }).show();
                            }
                        }
                    } else if (szVerb.equalsIgnoreCase("startcamera") && bFromPC) {
                        if (!QuestionCaptureActivity.getIsActivity()) {
                            Intent intent = new Intent(this.m_Context, QuestionCaptureActivity.class);
                            intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                            this.m_Context.startActivity(intent);
                        }
                    } else if (szVerb.equalsIgnoreCase("VResult") && arrData.length == 2) {
                        if (Utilities.isInArray(TeacherPadApplication.arrStudentIDs, szFrom)) {
                            String szQuestionGroupGUID = arrData[1];
                            if (szQuestionGroupGUID.indexOf(":") != -1) {
                                szQuestionGroupGUID = szQuestionGroupGUID.substring(0, szQuestionGroupGUID.indexOf(":"));
                            }
                            if (TeacherPadApplication.bAllowQuestionAnswer) {
                                szStudentAnswer = arrData[1];
                                TeacherPadApplication.ClassMultiQuestions.addStudentAnswer(szFrom, szStudentAnswer, false, (String) TeacherPadApplication.mapStudentName.get(szFrom));
                            }
                            if (TeacherPadApplication.bLockStudentPadAfterAnswer && TeacherPadApplication.ClassMultiQuestions.isLastQuestion(szQuestionGroupGUID)) {
                                TeacherPadApplication.IMThread.SendLockRequest(szFrom);
                            }
                        }
                    } else if (szVerb.equalsIgnoreCase("VAutoResult") && arrData.length == 2) {
                        if (Utilities.isInArray(TeacherPadApplication.arrStudentIDs, szFrom)) {
                            szStudentAnswer = arrData[1];
                            TeacherPadApplication.ClassMultiQuestions.addStudentAnswer(szFrom, szStudentAnswer, true, (String) TeacherPadApplication.mapStudentName.get(szFrom));
                            if (TeacherPadApplication.bLockStudentPadAfterAnswer) {
                                TeacherPadApplication.IMThread.SendLockRequest(szFrom);
                            }
                        }
                    } else if (szVerb.equalsIgnoreCase("CorrectAnswer") && bFromPC) {
                        if (arrData.length == 2) {
                            TeacherPadApplication.szCorrectAnswer = arrData[1];
                        } else {
                            TeacherPadApplication.szCorrectAnswer = "";
                        }
                    } else if (szVerb.equalsIgnoreCase("WhiteBoardStateResult") && bFromPC) {
                        if (arrData.length >= 1) {
                            if (arrData[1].equalsIgnoreCase("visible")) {
                                TeacherPadApplication.bWhiteBoardOn = true;
                            } else {
                                TeacherPadApplication.bWhiteBoardOn = false;
                            }
                        }
                    } else if (szVerb.equalsIgnoreCase("QuestionRequest") && arrData.length >= 2 && bFromPC) {
                        String[] QuestionData = arrData;
                        if (QuestionData[1].equalsIgnoreCase("Vote") || QuestionData[1].equalsIgnoreCase("VoteSingle")) {
                            TeacherPadApplication.szCurrentQuestionIMMessage = QuestionData[1] + " " + QuestionData[2];
                        } else if (QuestionData[1].equalsIgnoreCase("AnswerSheetQuestion")) {
                            TeacherPadApplication.szCurrentQuestionIMMessage = szMessageBody.substring(szMessageBody.indexOf("AnswerSheetQuestion"));
                        } else {
                            TeacherPadApplication.szCurrentQuestionIMMessage = "ScreenCopyAndSend HandWrite";
                        }
                    }
                }
                if (CurrentActivity != null && (CurrentActivity instanceof BaseActivity)) {
                    ((BaseActivity) CurrentActivity).onIMMessage(szFrom, szMessageBody);
                }
            }
        }
    }

    public void forgetData() {
        this.mszLastGetScreenLayoutResult = "";
    }
}
