package com.netspace.library.struct;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.gson.annotations.Expose;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Const;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.im.IMService;
import com.netspace.library.parser.ServerConfigurationParser.Feature;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import io.vov.vitamio.MediaMetadataRetriever;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.http.cookie.ClientCookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {
    private static final String TAG = "UserInfo";
    @Expose
    public ArrayList<UserAchievement> arrAchievements = new ArrayList();
    public ArrayList<UserClassInfo> arrClasses = new ArrayList();
    @Expose
    public ArrayList<String> arrGroupGUIDs = new ArrayList();
    @Expose
    public ArrayList<String> arrGroupNames = new ArrayList();
    @Expose
    public ArrayList<UserSchool> arrSchoolAdmins = new ArrayList();
    @Expose
    public boolean bServerIPChanged = false;
    @Expose
    public HashMap<String, Boolean> mapPermission = new HashMap();
    @Expose
    public int nGrade = -1;
    @Expose
    public int nLevel = 0;
    @Expose
    public int nScore = 0;
    @Expose
    public int nSubject = -1;
    @Expose
    public int nUserType = -1;
    @Expose
    public String szEMail = "";
    @Expose
    public String szFullAccount = "";
    @Expose
    public String szPhoneNumber = "";
    @Expose
    public String szRealName = "";
    @Expose
    public String szSchoolGUID = "";
    @Expose
    public String szSchoolName = "";
    @Expose
    public String szStudentID = "";
    @Expose
    public String szSubcenterGUID = "";
    @Expose
    public String szUserGUID = "";
    @Expose
    public String szUserJID = "";
    @Expose
    public String szUserName = "";

    public boolean isConfigured() {
        if (this.szFullAccount.isEmpty() || this.szUserName.isEmpty()) {
            return false;
        }
        return true;
    }

    public String getFirstClassName() {
        if (this.arrClasses.size() == 1) {
            return ((UserClassInfo) this.arrClasses.get(0)).szClassName;
        }
        for (int i = 0; i < this.arrClasses.size(); i++) {
            if (((UserClassInfo) this.arrClasses.get(i)).nVirtual == 0) {
                return ((UserClassInfo) this.arrClasses.get(i)).szClassName;
            }
        }
        return "";
    }

    public String getClassNames() {
        String szResult = "";
        for (int i = 0; i < this.arrClasses.size(); i++) {
            szResult = new StringBuilder(String.valueOf(szResult)).append(((UserClassInfo) this.arrClasses.get(i)).szClassName).toString();
            if (i != this.arrClasses.size() - 1) {
                szResult = new StringBuilder(String.valueOf(szResult)).append("、").toString();
            }
        }
        return szResult;
    }

    public String getFirstClassGUID() {
        if (this.arrClasses.size() == 1) {
            return ((UserClassInfo) this.arrClasses.get(0)).szClassGUID;
        }
        for (int i = 0; i < this.arrClasses.size(); i++) {
            if (((UserClassInfo) this.arrClasses.get(i)).nVirtual == 0) {
                return ((UserClassInfo) this.arrClasses.get(i)).szClassGUID;
            }
        }
        return "";
    }

    public String getClassesGUIDs() {
        String szResult = "";
        for (int i = 0; i < this.arrClasses.size(); i++) {
            szResult = new StringBuilder(String.valueOf(szResult)).append(((UserClassInfo) this.arrClasses.get(i)).szClassGUID).toString();
            if (i != this.arrClasses.size() - 1) {
                szResult = new StringBuilder(String.valueOf(szResult)).append(",").toString();
            }
        }
        return szResult;
    }

    public String getClassesGUIDs(String szSepChar) {
        String szResult = "";
        for (int i = 0; i < this.arrClasses.size(); i++) {
            szResult = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szResult)).append(szSepChar).toString())).append(((UserClassInfo) this.arrClasses.get(i)).szClassGUID).toString();
        }
        if (MyiBaseApplication.getCommonVariables().MyiApplication.getClientID().indexOf("_teacherpad") != -1) {
            return new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szResult)).append("_[").toString())).append(this.szUserName).toString())).append("_teacherpad").toString())).append("]").toString();
        }
        return new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szResult)).append("_[").toString())).append(this.szUserName).toString())).append("]").toString();
    }

    public int getClassesCount() {
        return this.arrClasses.size();
    }

    public void getAllClassesTeachers(ArrayList<String> arrTeacherRealName, ArrayList<String> arrTeacherUID) {
        for (int i = 0; i < this.arrClasses.size(); i++) {
            UserClassInfo oneClass = (UserClassInfo) this.arrClasses.get(i);
            for (int j = 0; j < oneClass.arrTeachers.size(); j++) {
                TeacherInfo oneTeacher = (TeacherInfo) oneClass.arrTeachers.get(j);
                String szUID = oneTeacher.szUserName + "_teacherpad";
                if (!Utilities.isInArray((ArrayList) arrTeacherUID, szUID)) {
                    arrTeacherRealName.add(oneTeacher.szRealName);
                    arrTeacherUID.add(szUID);
                }
            }
        }
    }

    public ArrayList<UserInfo> getClassStudents(String szClassGUID) {
        for (int i = 0; i < this.arrClasses.size(); i++) {
            UserClassInfo oneClass = (UserClassInfo) this.arrClasses.get(i);
            if (oneClass.szClassGUID.equalsIgnoreCase(szClassGUID)) {
                return oneClass.arrStudents;
            }
        }
        return null;
    }

    public UserInfo findUserByGUID(String szUserGUID) {
        for (int i = 0; i < this.arrClasses.size(); i++) {
            int j;
            UserClassInfo oneClass = (UserClassInfo) this.arrClasses.get(i);
            for (j = 0; j < oneClass.arrTeachers.size(); j++) {
                TeacherInfo oneTeacher = (TeacherInfo) oneClass.arrTeachers.get(j);
                if (oneTeacher.szUserGUID.equalsIgnoreCase(szUserGUID)) {
                    return oneTeacher;
                }
            }
            for (j = 0; j < oneClass.arrStudents.size(); j++) {
                UserInfo oneStudent = (UserInfo) oneClass.arrStudents.get(j);
                if (oneStudent.szUserGUID.equalsIgnoreCase(szUserGUID)) {
                    return oneStudent;
                }
            }
        }
        return null;
    }

    public String getLevelName() {
        return MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.getLevelName(this.nScore);
    }

    public boolean decodeLoginJson(String szJsonData) {
        try {
            String szACL;
            int i;
            int j;
            JSONObject jSONObject = new JSONObject(szJsonData);
            this.nUserType = jSONObject.getInt("usertype");
            this.szSubcenterGUID = jSONObject.getString("subcenterguid");
            this.szSchoolName = jSONObject.getString("schoolname");
            this.szSchoolGUID = jSONObject.getString("schoolguid");
            this.szRealName = jSONObject.getString("realname");
            this.szPhoneNumber = jSONObject.getString("phonenumber");
            this.szUserGUID = jSONObject.getString("userguid");
            this.nLevel = jSONObject.getInt("level");
            this.nScore = jSONObject.getInt("score");
            if (MyiBaseApplication.getCommonVariables().MyiApplication != null) {
                this.szUserJID = MyiBaseApplication.getCommonVariables().MyiApplication.getClientID();
            }
            if (!jSONObject.optString("resourcebaseurl").isEmpty()) {
                MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL = jSONObject.getString("resourcebaseurl");
                Editor Editor = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext()).edit();
                Editor.putString("BaseAddress", MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL);
                Editor.commit();
            }
            if (!jSONObject.isNull("acl")) {
                szACL = jSONObject.getString("acl");
                if (!szACL.isEmpty()) {
                    jSONObject = new JSONObject(szACL);
                    if (jSONObject.has("hosts") && jSONObject.has(ClientCookie.PORT_ATTR)) {
                        Log.i(TAG, "Found new host info. prepare to redirect...");
                        String[] arrHosts = jSONObject.getString("hosts").split(";");
                        int nPort = jSONObject.getInt(ClientCookie.PORT_ATTR);
                        MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress = arrHosts[0] + ":" + String.valueOf(nPort);
                        LoadExamDataThread3.setupServerAddress(MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress);
                        Log.i(TAG, "Redirect comm to " + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress);
                        try {
                            URL url = new URL(MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL);
                            MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL = MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL.replace(url.getHost(), arrHosts[0]);
                            Log.i(TAG, "Redirect resource to " + MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        this.bServerIPChanged = true;
                    }
                }
            }
            this.arrGroupGUIDs.clear();
            this.arrGroupNames.clear();
            if (!jSONObject.isNull("usergroup")) {
                JSONArray jsonGroup = jSONObject.getJSONArray("usergroup");
                for (i = 0; i < jsonGroup.length(); i++) {
                    JSONObject oneGroup = jsonGroup.getJSONObject(i);
                    this.arrGroupNames.add(oneGroup.getString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX));
                    this.arrGroupGUIDs.add(oneGroup.getString("guid"));
                }
            }
            this.arrClasses.clear();
            HashMap<String, UserInfo> studentGUIDMap = new HashMap();
            HashMap<String, UserInfo> teacherGUIDMap = new HashMap();
            HashMap<String, UserClassInfo> classGUIDMap = new HashMap();
            if (!jSONObject.isNull("classes")) {
                JSONArray jsonClass = jSONObject.getJSONArray("classes");
                for (i = 0; i < jsonClass.length(); i++) {
                    JSONObject oneClass = jsonClass.getJSONObject(i);
                    UserClassInfo UserClassInfo = new UserClassInfo();
                    UserClassInfo.szClassName = oneClass.getString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                    UserClassInfo.szClassGUID = oneClass.getString("guid");
                    UserClassInfo.nSubject = oneClass.getInt("subject");
                    UserClassInfo.Grade = new UserGrade(oneClass.getInt("grade"));
                    if (!oneClass.isNull("teachers")) {
                        JSONArray jsonTeachers = oneClass.getJSONArray("teachers");
                        for (j = 0; j < jsonTeachers.length(); j++) {
                            JSONObject oneTeacher = jsonTeachers.getJSONObject(j);
                            if (oneTeacher.getInt("type") == 0) {
                                StudentInfo StudentInfo = new StudentInfo();
                                String szStudentGUID = oneTeacher.getString("guid");
                                if (studentGUIDMap.containsKey(szStudentGUID)) {
                                    StudentInfo = (StudentInfo) studentGUIDMap.get(szStudentGUID);
                                } else {
                                    StudentInfo.szUserGUID = oneTeacher.getString("guid");
                                    StudentInfo.szRealName = oneTeacher.getString("realname");
                                    StudentInfo.szUserName = oneTeacher.getString(UserHonourFragment.USERNAME);
                                    StudentInfo.nUserType = oneTeacher.getInt("type");
                                    StudentInfo.nGrade = oneTeacher.getInt("grade");
                                    StudentInfo.nSubject = oneTeacher.getInt("subject");
                                    StudentInfo.szUserJID = "myipad_" + StudentInfo.szUserName;
                                    studentGUIDMap.put(szStudentGUID, StudentInfo);
                                }
                                StudentInfo.arrClasses.add(UserClassInfo);
                                UserClassInfo.arrStudents.add(StudentInfo);
                            } else {
                                TeacherInfo TeacherInfo = new TeacherInfo();
                                String szTeacherGUID = oneTeacher.getString("guid");
                                if (teacherGUIDMap.containsKey(szTeacherGUID)) {
                                    TeacherInfo = (TeacherInfo) teacherGUIDMap.get(szTeacherGUID);
                                } else {
                                    TeacherInfo.szUserGUID = oneTeacher.getString("guid");
                                    TeacherInfo.szRealName = oneTeacher.getString("realname");
                                    TeacherInfo.szUserName = oneTeacher.getString(UserHonourFragment.USERNAME);
                                    TeacherInfo.nUserType = oneTeacher.getInt("type");
                                    TeacherInfo.nGrade = oneTeacher.getInt("grade");
                                    TeacherInfo.nSubject = oneTeacher.getInt("subject");
                                    TeacherInfo.szUserJID = TeacherInfo.szUserName + "_teacherpad";
                                    teacherGUIDMap.put(szTeacherGUID, TeacherInfo);
                                }
                                TeacherInfo.arrClasses.add(UserClassInfo);
                                UserClassInfo.arrTeachers.add(TeacherInfo);
                            }
                        }
                        Collections.sort(UserClassInfo.arrStudents, new Comparator<UserInfo>() {
                            RuleBasedCollator collator = ((RuleBasedCollator) Collator.getInstance(Locale.CHINA));

                            public int compare(UserInfo lhs, UserInfo rhs) {
                                return this.collator.compare(lhs.szRealName, rhs.szRealName);
                            }
                        });
                        Collections.sort(UserClassInfo.arrTeachers, new Comparator<TeacherInfo>() {
                            RuleBasedCollator collator = ((RuleBasedCollator) Collator.getInstance(Locale.CHINA));

                            public int compare(TeacherInfo lhs, TeacherInfo rhs) {
                                return this.collator.compare(lhs.szRealName, rhs.szRealName);
                            }
                        });
                    }
                    classGUIDMap.put(UserClassInfo.szClassGUID, UserClassInfo);
                    this.arrClasses.add(UserClassInfo);
                }
                Collections.sort(this.arrClasses, new Comparator<UserClassInfo>() {
                    RuleBasedCollator collator = ((RuleBasedCollator) Collator.getInstance(Locale.CHINA));

                    public int compare(UserClassInfo lhs, UserClassInfo rhs) {
                        return this.collator.compare(lhs.szClassName, rhs.szClassName);
                    }
                });
            }
            if (!jSONObject.isNull("acl")) {
                szACL = jSONObject.getString("acl");
                if (!szACL.isEmpty()) {
                    jSONObject = new JSONObject(szACL);
                    for (i = 0; i < jSONObject.names().length(); i++) {
                        String szOneName = jSONObject.names().getString(i);
                        String szValue = jSONObject.getString(szOneName);
                        if (szValue.equalsIgnoreCase("on")) {
                            this.mapPermission.put(szOneName, Boolean.valueOf(true));
                        } else if (szValue.equalsIgnoreCase("off")) {
                            this.mapPermission.put(szOneName, Boolean.valueOf(false));
                        }
                    }
                    if (!jSONObject.isNull("schooladmin")) {
                        JSONArray arrSchoolAdmin = jSONObject.getJSONArray("schooladmin");
                        for (i = 0; i < arrSchoolAdmin.length(); i++) {
                            JSONObject oneSchool = arrSchoolAdmin.getJSONObject(i);
                            UserSchool userSchool = new UserSchool();
                            userSchool.szName = oneSchool.getString("schoolname");
                            userSchool.szGUID = oneSchool.getString("schoolguid");
                            userSchool.szSubcenterID = oneSchool.getString("subcenterid");
                            this.arrSchoolAdmins.add(userSchool);
                        }
                    }
                }
            }
            if (!jSONObject.isNull("achievements")) {
                JSONArray jsonAchievements = jSONObject.getJSONArray("achievements");
                for (i = 0; i < jsonAchievements.length(); i++) {
                    JSONObject oneAchievement = jsonAchievements.getJSONObject(i);
                    UserAchievement Info = new UserAchievement();
                    Info.szAchievementGUID = oneAchievement.getString("guid");
                    Info.szDate = oneAchievement.getString(MediaMetadataRetriever.METADATA_KEY_DATE);
                    Info.szComment = oneAchievement.getString("comment");
                    boolean bFound = false;
                    for (j = 0; j < this.arrAchievements.size(); j++) {
                        if (((UserAchievement) this.arrAchievements.get(j)).szAchievementGUID.equalsIgnoreCase(Info.szAchievementGUID)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        this.arrAchievements.add(Info);
                    }
                }
            }
            JSONObject savejson = new JSONObject();
            try {
                savejson.put("fullaccount", this.szFullAccount);
                savejson.put(UserHonourFragment.USERNAME, this.szUserName);
                savejson.put("serveraddress", MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress);
                savejson.put("baseaddress", MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL);
                savejson.put("schoolguid", this.szSchoolGUID);
                String szContent = Utilities.encryptString(savejson.toString(), Const.PASSWORDFORLOGINCONFIG);
                File externalFile = MyiBaseApplication.getBaseAppContext().getExternalCacheDir();
                if (externalFile != null) {
                    Utilities.writeTextToFile(externalFile.getAbsolutePath() + "/../../." + MyiBaseApplication.getBaseAppContext().getPackageName() + ".config", szContent);
                }
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            return true;
        } catch (JSONException e22) {
            e22.printStackTrace();
            return false;
        }
    }

    public void deleteSavedAccountConfig() {
        File externalFile = MyiBaseApplication.getBaseAppContext().getExternalCacheDir();
        if (externalFile != null) {
            Utilities.deleteFile(externalFile.getAbsolutePath() + "/../../." + MyiBaseApplication.getBaseAppContext().getPackageName() + ".config");
        }
    }

    public boolean hasAchievement(String szKey) {
        for (int i = 0; i < this.arrAchievements.size(); i++) {
            if (((UserAchievement) this.arrAchievements.get(i)).szAchievementGUID.equalsIgnoreCase(szKey)) {
                return true;
            }
        }
        return false;
    }

    public UserAchievement getAchievement(String szKey) {
        for (int i = 0; i < this.arrAchievements.size(); i++) {
            if (((UserAchievement) this.arrAchievements.get(i)).szAchievementGUID.equalsIgnoreCase(szKey)) {
                return (UserAchievement) this.arrAchievements.get(i);
            }
        }
        return null;
    }

    public boolean checkPermission(String szPermissionName) {
        boolean bResult = false;
        if (this.mapPermission.containsKey(szPermissionName)) {
            bResult = ((Boolean) this.mapPermission.get(szPermissionName)).booleanValue();
            if (!bResult) {
                return bResult;
            }
        }
        Feature Feature = MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.getFeature(szPermissionName);
        if (Feature != null) {
            if (Feature.szValue.equalsIgnoreCase("auto")) {
                if (this.nLevel >= Feature.nMinLevel) {
                    return true;
                }
                return false;
            } else if (Feature.szValue.equalsIgnoreCase("off")) {
                return false;
            } else {
                if (Feature.szValue.equalsIgnoreCase("on")) {
                    return true;
                }
            }
        }
        return bResult;
    }

    public static void UserScore(String szAction) {
        UserScore(szAction, null);
    }

    public static void UserScore(String szAction, String szReason) {
        if (!VirtualNetworkObject.getOfflineMode()) {
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("UsersAction", null);
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                }
            });
            CallItem.setAlwaysActiveCallbacks(true);
            CallItem.setParam("lpszUserName", IMService.getIMUserName());
            CallItem.setParam("lpszUserGUID", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
            CallItem.setParam("lpszActionName", szAction);
            if (szReason != null) {
                CallItem.setParam("lpszReason", szReason);
            }
            if (szAction.equalsIgnoreCase("StatusReport")) {
                JSONObject json = new JSONObject();
                JSONArray wifiArray = new JSONArray();
                json.put("battery", Utilities.getBatteryLevel());
                json.put("screen", Utilities.getScreenStatus());
                json.put("wifi", wifiArray);
                Activity activity = UI.getCurrentActivity();
                if (activity != null) {
                    json.put("activity", activity.getClass().getName());
                }
                WifiManager wifiManager = (WifiManager) MyiBaseApplication.getBaseAppContext().getSystemService("wifi");
                List<ScanResult> wifiList = wifiManager.getScanResults();
                WifiInfo currentWifi = wifiManager.getConnectionInfo();
                if (wifiList != null) {
                    for (ScanResult scanResult : wifiList) {
                        int level = WifiManager.calculateSignalLevel(scanResult.level, 100);
                        JSONObject oneWifi = new JSONObject();
                        oneWifi.put("ssid", scanResult.SSID);
                        oneWifi.put("mac", scanResult.BSSID);
                        oneWifi.put("level", level);
                        oneWifi.put("frequency", scanResult.frequency);
                        if (VERSION.SDK_INT >= 23) {
                            switch (scanResult.channelWidth) {
                                case 0:
                                    try {
                                        oneWifi.put("channelWidth", "20Mhz");
                                        break;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                case 1:
                                    oneWifi.put("channelWidth", "40Mhz");
                                    break;
                                case 2:
                                    oneWifi.put("channelWidth", "80Mhz");
                                    break;
                                case 3:
                                    oneWifi.put("channelWidth", "160Mhz");
                                    break;
                                case 4:
                                    oneWifi.put("channelWidth", "80Mhz+");
                                    break;
                            }
                        }
                        if (scanResult.BSSID.equalsIgnoreCase(currentWifi.getBSSID())) {
                            oneWifi.put("current", true);
                        }
                        wifiArray.put(oneWifi);
                    }
                }
                CallItem.setParam("lpszReason", json.toString());
            }
            VirtualNetworkObject.addToQueue(CallItem);
        }
    }

    public boolean isInClass(String szClassGUID) {
        for (int i = 0; i < this.arrClasses.size(); i++) {
            if (((UserClassInfo) this.arrClasses.get(i)).szClassGUID.equalsIgnoreCase(szClassGUID)) {
                return true;
            }
        }
        return false;
    }
}
