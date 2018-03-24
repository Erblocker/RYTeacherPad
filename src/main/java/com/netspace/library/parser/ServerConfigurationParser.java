package com.netspace.library.parser;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.gson.annotations.Expose;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ServerConfigurationParser {
    private static String mLastWebAccessConfigMD5 = "";
    private static ExecuteScriptCallBack mScriptCallBack;
    @Expose
    private HashMap<String, UIImage> mMapUIImage = new HashMap();
    @Expose
    private ArrayList<Achievement> marrAchievements = new ArrayList();
    @Expose
    private ArrayList<String> marrAppBlackList = new ArrayList();
    @Expose
    private ArrayList<String> marrAppWhiteList = new ArrayList();
    @Expose
    private ArrayList<String> marrBlackList = new ArrayList();
    @Expose
    private ArrayList<BuildInAppInfo> marrBuildApps = new ArrayList();
    @Expose
    private ArrayList<Feature> marrFeatures = new ArrayList();
    @Expose
    private ArrayList<Level> marrLevels = new ArrayList();
    @Expose
    private ArrayList<Module> marrModules = new ArrayList();
    @Expose
    private ArrayList<UsageTime> marrPhoneUsageTime = new ArrayList();
    @Expose
    private ArrayList<PluginModule> marrPlugins = new ArrayList();
    @Expose
    private ArrayList<Score> marrScores = new ArrayList();
    @Expose
    private ArrayList<String> marrScript = new ArrayList();
    @Expose
    private ArrayList<Subject> marrSubjects = new ArrayList();
    @Expose
    private ArrayList<UsageTime> marrUsageTime = new ArrayList();
    @Expose
    private ArrayList<String> marrWhiteList = new ArrayList();
    @Expose
    private boolean mbWebBrowserUnlimit = false;
    @Expose
    private String mszPromptMessage;

    public static class Achievement {
        @Expose
        public String szComment;
        @Expose
        public String szConfig;
        @Expose
        public String szIconUrl;
        @Expose
        public String szKey;
        @Expose
        public String szName;
    }

    public static class BuildInAppInfo {
        @Expose
        public int nIconResID;
        @Expose
        public String szActionName;
        @Expose
        public String szGroupName;
        @Expose
        public String szIntent;
        @Expose
        public String szMD5;
        @Expose
        public String szPackageName;
        @Expose
        public String szTitle;
        @Expose
        public String szURL;
        @Expose
        public String szVersion;
    }

    public interface ExecuteScriptCallBack {
        boolean onExecuteScript(String str);
    }

    public static class Feature {
        @Expose
        public int nMinLevel;
        @Expose
        public String szName;
        @Expose
        public String szValue;
    }

    public static class Level {
        @Expose
        public int nID;
        @Expose
        public int nMinScore;
        @Expose
        public String szName;
    }

    public static class Module {
        @Expose
        public boolean bEnable;
        @Expose
        public String szGroupName;
        @Expose
        public String szName;
        @Expose
        public String szParams;
        @Expose
        public String szType;
    }

    public static class PluginModule {
        @Expose
        public String szClassName;
        @Expose
        public String szURL;
    }

    public static class Score {
        @Expose
        public int nScoreValue;
        @Expose
        public String szAction;
        @Expose
        public String szName;
    }

    private class ScriptlaunchThread extends Thread {
        private ScriptlaunchThread() {
        }

        public void run() {
            if (ServerConfigurationParser.mScriptCallBack != null) {
                ServerConfigurationParser.this.executeScripts(ServerConfigurationParser.mScriptCallBack);
            }
        }
    }

    public static class Subject {
        @Expose
        public int nID;
        @Expose
        public String szName;
    }

    public static class UIImage {
        @Expose
        public int nHeight;
        @Expose
        public int nLeft;
        @Expose
        public int nTop;
        @Expose
        public int nWidth;
        @Expose
        public String szIntent;
        @Expose
        public String szURL;
    }

    public static class UsageTime {
        @Expose
        public String szFrom;
        @Expose
        public String szTo;
    }

    public boolean initialize(Context Context, String szXMLData) {
        try {
            int i;
            Element OneElement;
            UsageTime OneTime;
            String szFrom;
            String szTo;
            Element OneNode;
            String szName;
            String szValue;
            String szURL;
            String szPackageName;
            String szID;
            String szKey;
            Document RootDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXMLData.getBytes(HTTP.UTF_8)));
            NodeList AppNodeList = RootDocument.getElementsByTagName("Application");
            String szConfigMD5 = Utilities.md5(szXMLData);
            for (i = 0; i < AppNodeList.getLength(); i++) {
                OneElement = (Element) AppNodeList.item(i);
                BuildInAppInfo OneApp = new BuildInAppInfo();
                OneApp.szPackageName = OneElement.getAttribute("package");
                OneApp.szActionName = OneElement.getAttribute(TestHandler.ACTION);
                OneApp.szTitle = OneElement.getAttribute("title");
                OneApp.szURL = OneElement.getAttribute(StudentAnswerImageService.LISTURL);
                OneApp.szIntent = OneElement.getAttribute("intent");
                OneApp.szMD5 = OneElement.getAttribute("md5");
                OneApp.szGroupName = OneElement.getAttribute("group");
                if (OneApp.szIntent.isEmpty()) {
                    OneApp.szIntent = null;
                }
                OneApp.szVersion = OneElement.getAttribute(ClientCookie.VERSION_ATTR);
                if (OneApp.szIntent != null) {
                    OneApp.szIntent = OneApp.szIntent.replace("%USERNAME%", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                    OneApp.szIntent = OneApp.szIntent.replace("%USERGUID%", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    OneApp.szIntent = OneApp.szIntent.replace("%PASSWORD%", MyiBaseApplication.getCommonVariables().Session.getPasswordMD5());
                }
                String szIcon = OneElement.getAttribute("icon");
                if (!(szIcon == null || szIcon.isEmpty())) {
                    OneApp.nIconResID = Context.getResources().getIdentifier(szIcon, "drawable", Context.getPackageName());
                }
                this.marrBuildApps.add(OneApp);
            }
            NodeList UsageTimeNodeList = RootDocument.getElementsByTagName("UsageTime");
            for (i = 0; i < UsageTimeNodeList.getLength(); i++) {
                OneElement = (Element) UsageTimeNodeList.item(i);
                OneTime = new UsageTime();
                szFrom = OneElement.getAttribute("from");
                szTo = OneElement.getAttribute("to");
                if (!(szFrom == null || szTo == null || szFrom.isEmpty() || szTo.isEmpty())) {
                    OneTime.szFrom = szFrom;
                    OneTime.szTo = szTo;
                    this.marrUsageTime.add(OneTime);
                }
            }
            NodeList PhoneUsageTimeNodeList = RootDocument.getElementsByTagName("PhoneUsageTime");
            for (i = 0; i < PhoneUsageTimeNodeList.getLength(); i++) {
                OneElement = (Element) PhoneUsageTimeNodeList.item(i);
                OneTime = new UsageTime();
                szFrom = OneElement.getAttribute("from");
                szTo = OneElement.getAttribute("to");
                if (!(szFrom == null || szTo == null || szFrom.isEmpty() || szTo.isEmpty())) {
                    OneTime.szFrom = szFrom;
                    OneTime.szTo = szTo;
                    this.marrPhoneUsageTime.add(OneTime);
                }
            }
            Element WebBrowserNode = getXMLNode("/Configuration/Webbrowser", RootDocument.getDocumentElement());
            if (WebBrowserNode != null) {
                if (WebBrowserNode.hasAttribute("unlimit") && WebBrowserNode.getAttribute("unlimit").equalsIgnoreCase("true")) {
                    this.mbWebBrowserUnlimit = true;
                }
                NodeList NodeList = WebBrowserNode.getElementsByTagName("Whitelist");
                for (i = 0; i < NodeList.getLength(); i++) {
                    OneNode = (Element) NodeList.item(i);
                    if (OneNode.hasAttribute(StudentAnswerImageService.LISTURL)) {
                        this.marrWhiteList.add(OneNode.getAttribute(StudentAnswerImageService.LISTURL));
                    }
                }
                NodeList = WebBrowserNode.getElementsByTagName("Blacklist");
                for (i = 0; i < NodeList.getLength(); i++) {
                    OneNode = (Element) NodeList.item(i);
                    if (OneNode.hasAttribute(StudentAnswerImageService.LISTURL)) {
                        this.marrBlackList.add(OneNode.getAttribute(StudentAnswerImageService.LISTURL));
                    }
                }
                if (mLastWebAccessConfigMD5.equalsIgnoreCase(szConfigMD5)) {
                    Log.i("ServerConfiguration", "Ignore host limit script generate. Config file not change.");
                } else {
                    generateHostLimitScript();
                }
                mLastWebAccessConfigMD5 = szConfigMD5;
            }
            Element MessageNode = getXMLNode("/Configuration/Messages/Message", RootDocument.getDocumentElement());
            if (MessageNode != null) {
                this.mszPromptMessage = MessageNode.getTextContent();
            }
            NodeList ScriptNodes = getXMLNodes("/Configuration/Scripts/Script", RootDocument.getDocumentElement());
            for (i = 0; i < ScriptNodes.getLength(); i++) {
                this.marrScript.add(((Element) ScriptNodes.item(i)).getTextContent());
            }
            NodeList SettingsNodes = getXMLNodes("/Configuration/Settings/Setting", RootDocument.getDocumentElement());
            for (i = 0; i < SettingsNodes.getLength(); i++) {
                OneNode = (Element) SettingsNodes.item(i);
                szName = OneNode.getAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                szValue = OneNode.getAttribute("value");
                Editor Editor = PreferenceManager.getDefaultSharedPreferences(Context).edit();
                if (szName.equalsIgnoreCase("AutoLogin") || szName.equalsIgnoreCase("RememberPassword") || szName.equalsIgnoreCase("UseRootCapture") || szName.equalsIgnoreCase("SkipRootCheck")) {
                    Editor.putBoolean(szName, szValue.equalsIgnoreCase("true"));
                } else if (szName.equalsIgnoreCase("password")) {
                    Editor.putString(szName, szValue);
                }
                Editor.commit();
            }
            NodeList PluginsNodes = getXMLNodes("/Configuration/Plugins/Plugin", RootDocument.getDocumentElement());
            for (i = 0; i < PluginsNodes.getLength(); i++) {
                OneNode = (Element) PluginsNodes.item(i);
                szURL = OneNode.getAttribute(StudentAnswerImageService.LISTURL);
                String szClassName = OneNode.getAttribute("class");
                if (!szURL.isEmpty() || szClassName.isEmpty()) {
                    PluginModule module = new PluginModule();
                    module.szClassName = szClassName;
                    module.szURL = szURL;
                    this.marrPlugins.add(module);
                }
            }
            NodeList ModuleNodes = getXMLNodes("/Configuration/Modules/Module", RootDocument.getDocumentElement());
            for (i = 0; i < ModuleNodes.getLength(); i++) {
                OneNode = (Element) ModuleNodes.item(i);
                Module module2 = new Module();
                module2.szGroupName = OneNode.getAttribute("group");
                module2.szType = OneNode.getAttribute("type");
                module2.szName = OneNode.getAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                module2.szParams = OneNode.getAttribute("params");
                module2.bEnable = OneNode.getAttribute("enable").equalsIgnoreCase("on");
                module2.szParams = module2.szParams.replace("%USERNAME%", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                module2.szParams = module2.szParams.replace("%USERGUID%", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                module2.szParams = module2.szParams.replace("%PASSWORD%", MyiBaseApplication.getCommonVariables().Session.getPasswordMD5());
                module2.szParams = module2.szParams.replace("%PASSWORDMD5%", MyiBaseApplication.getCommonVariables().Session.getPasswordMD5());
                this.marrModules.add(module2);
            }
            NodeList AllowAppNodes = getXMLNodes("/Configuration/Apps/Allow", RootDocument.getDocumentElement());
            for (i = 0; i < AllowAppNodes.getLength(); i++) {
                szPackageName = ((Element) AllowAppNodes.item(i)).getAttribute("package");
                if (!szPackageName.isEmpty()) {
                    this.marrAppWhiteList.add(szPackageName);
                }
            }
            NodeList RemoveAppNodes = getXMLNodes("/Configuration/Apps/Remove", RootDocument.getDocumentElement());
            for (i = 0; i < RemoveAppNodes.getLength(); i++) {
                OneNode = (Element) RemoveAppNodes.item(i);
                String szVersionName = OneNode.getAttribute(ClientCookie.VERSION_ATTR);
                szPackageName = OneNode.getAttribute("package");
                if (!szPackageName.isEmpty()) {
                    boolean bRemove;
                    this.marrAppBlackList.add(szPackageName);
                    try {
                        PackageInfo packInfo = Context.getPackageManager().getPackageInfo(szPackageName, 0);
                        bRemove = true;
                        if (!szVersionName.isEmpty()) {
                            if (packInfo.versionName.equalsIgnoreCase(szVersionName)) {
                                bRemove = true;
                            } else {
                                bRemove = false;
                            }
                        }
                    } catch (NameNotFoundException e) {
                        bRemove = false;
                    }
                    if (bRemove) {
                        this.marrScript.add("su -c \"pm uninstall " + szPackageName + "\"");
                    } else {
                        continue;
                    }
                }
            }
            NodeList FeaturesNodes = getXMLNodes("/Configuration/Features/Item", RootDocument.getDocumentElement());
            for (i = 0; i < FeaturesNodes.getLength(); i++) {
                OneNode = (Element) FeaturesNodes.item(i);
                szName = OneNode.getAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                szValue = OneNode.getAttribute("value");
                String szMinLevel = OneNode.getAttribute("minLevel");
                Feature oneFeature = new Feature();
                oneFeature.szName = szName;
                oneFeature.szValue = szValue;
                oneFeature.nMinLevel = Integer.valueOf(szMinLevel).intValue();
                this.marrFeatures.add(oneFeature);
            }
            NodeList LevelNodes = getXMLNodes("/Configuration/Levels/Level", RootDocument.getDocumentElement());
            for (i = 0; i < LevelNodes.getLength(); i++) {
                OneNode = (Element) LevelNodes.item(i);
                szName = OneNode.getAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                szID = OneNode.getAttribute("id");
                String szMinScore = OneNode.getAttribute("minScore");
                Level oneLevel = new Level();
                oneLevel.szName = szName;
                oneLevel.nID = Integer.valueOf(szID).intValue();
                oneLevel.nMinScore = Integer.valueOf(szMinScore).intValue();
                this.marrLevels.add(oneLevel);
            }
            NodeList ScoreNodes = getXMLNodes("/Configuration/Scores/Score", RootDocument.getDocumentElement());
            for (i = 0; i < ScoreNodes.getLength(); i++) {
                OneNode = (Element) ScoreNodes.item(i);
                szName = OneNode.getAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                String szAction = OneNode.getAttribute(TestHandler.ACTION);
                String szScore = OneNode.getAttribute("scoreValue");
                Score oneScore = new Score();
                oneScore.szName = szName;
                oneScore.szAction = szAction;
                oneScore.nScoreValue = Integer.valueOf(szScore).intValue();
                this.marrScores.add(oneScore);
            }
            NodeList AchievementsNodes = getXMLNodes("/Configuration/Achievements/Achievement", RootDocument.getDocumentElement());
            for (i = 0; i < AchievementsNodes.getLength(); i++) {
                OneNode = (Element) AchievementsNodes.item(i);
                szName = OneNode.getAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                szKey = OneNode.getAttribute("key");
                String szComment = OneNode.getAttribute("comment");
                String szIconURL = OneNode.getAttribute("iconUrl");
                String szConfig = OneNode.getAttribute("config");
                if (OneNode.getAttribute("disable").equalsIgnoreCase("true")) {
                    for (int j = 0; j < this.marrAchievements.size(); j++) {
                        if (((Achievement) this.marrAchievements.get(j)).szName.equalsIgnoreCase(szName)) {
                            this.marrAchievements.remove(j);
                            break;
                        }
                    }
                } else {
                    szIconURL = szIconURL.replace("%RESOURCEBASEURL%", MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL);
                    Achievement oneAchievement = new Achievement();
                    oneAchievement.szName = szName;
                    oneAchievement.szKey = szKey;
                    oneAchievement.szComment = szComment;
                    oneAchievement.szIconUrl = szIconURL;
                    oneAchievement.szConfig = szConfig;
                    this.marrAchievements.add(oneAchievement);
                }
            }
            NodeList SubjectNodes = getXMLNodes("/Configuration/Subjects/Subject", RootDocument.getDocumentElement());
            for (i = 0; i < SubjectNodes.getLength(); i++) {
                OneNode = (Element) SubjectNodes.item(i);
                szName = OneNode.getAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                szID = OneNode.getAttribute("id");
                Subject subject = new Subject();
                subject.szName = szName;
                subject.nID = Integer.valueOf(szID).intValue();
                this.marrSubjects.add(subject);
            }
            NodeList UINodes = getXMLNodes("/Configuration/UI/Item", RootDocument.getDocumentElement());
            for (i = 0; i < UINodes.getLength(); i++) {
                OneNode = (Element) UINodes.item(i);
                UIImage image = new UIImage();
                szURL = OneNode.getAttribute(StudentAnswerImageService.LISTURL);
                szKey = OneNode.getAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                String szWidth = OneNode.getAttribute("width");
                String szHeight = OneNode.getAttribute("height");
                String szLeft = OneNode.getAttribute("left");
                String szTop = OneNode.getAttribute("top");
                image.szURL = szURL.replace("%RESOURCEBASEURL%", MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL);
                image.szIntent = OneNode.getAttribute("intent");
                if (image.szIntent != null) {
                    image.szIntent = image.szIntent.replace("%USERNAME%", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                    image.szIntent = image.szIntent.replace("%USERGUID%", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    image.szIntent = image.szIntent.replace("%PASSWORD%", MyiBaseApplication.getCommonVariables().Session.getPasswordMD5());
                }
                if (!szWidth.isEmpty()) {
                    if (szWidth == "100%") {
                        image.nWidth = -1;
                    } else {
                        image.nWidth = Utilities.toInt(szWidth);
                    }
                }
                if (!szHeight.isEmpty()) {
                    if (szHeight == "100%") {
                        image.nHeight = -1;
                    } else {
                        image.nHeight = Utilities.toInt(szHeight);
                    }
                }
                if (!szLeft.isEmpty()) {
                    image.nLeft = Utilities.toInt(szLeft);
                }
                if (!szTop.isEmpty()) {
                    image.nTop = Utilities.toInt(szTop);
                }
                this.mMapUIImage.put(szKey, image);
            }
            return true;
        } catch (SAXException e2) {
            e2.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        } catch (ParserConfigurationException e4) {
            e4.printStackTrace();
        }
        return false;
    }

    public void clear() {
        this.marrBuildApps.clear();
        this.marrUsageTime.clear();
        this.marrPhoneUsageTime.clear();
        this.marrWhiteList.clear();
        this.marrBlackList.clear();
        this.marrScript.clear();
        this.marrFeatures.clear();
        this.marrLevels.clear();
        this.marrScores.clear();
        this.marrAppBlackList.clear();
        this.marrAppWhiteList.clear();
        this.marrAchievements.clear();
        this.marrSubjects.clear();
        this.marrPlugins.clear();
        this.marrModules.clear();
    }

    public ArrayList<PluginModule> getPluginModules() {
        return this.marrPlugins;
    }

    public ArrayList<String> getScripts() {
        return this.marrScript;
    }

    public String getLevelName(int nScore) {
        String szResult = "";
        for (int i = 0; i < this.marrLevels.size(); i++) {
            Level oneLevel = (Level) this.marrLevels.get(i);
            if (oneLevel.nMinScore < nScore) {
                szResult = oneLevel.szName;
            }
        }
        return szResult;
    }

    public String getUIGraphicUrl(String szKey) {
        if (this.mMapUIImage.containsKey(szKey)) {
            return ((UIImage) this.mMapUIImage.get(szKey)).szURL;
        }
        return null;
    }

    public HashMap<String, UIImage> getAllUIGraphics() {
        return this.mMapUIImage;
    }

    public UIImage getUIGraphic(String szKey) {
        return (UIImage) this.mMapUIImage.get(szKey);
    }

    public Feature getFeature(String szFeatureName) {
        for (int i = 0; i < this.marrFeatures.size(); i++) {
            if (((Feature) this.marrFeatures.get(i)).szName.equalsIgnoreCase(szFeatureName)) {
                return (Feature) this.marrFeatures.get(i);
            }
        }
        return null;
    }

    public Score getScore(String szAction) {
        for (int i = 0; i < this.marrScores.size(); i++) {
            Score oneScore = (Score) this.marrScores.get(i);
            if (oneScore.szAction.equalsIgnoreCase(szAction)) {
                return oneScore;
            }
        }
        return null;
    }

    public ArrayList<BuildInAppInfo> getApplications() {
        return (ArrayList) this.marrBuildApps.clone();
    }

    public ArrayList<Achievement> getAchievements() {
        return this.marrAchievements;
    }

    public ArrayList<String> getAppBlackList() {
        return this.marrAppBlackList;
    }

    public ArrayList<String> getAppWhiteList() {
        return this.marrAppWhiteList;
    }

    public Achievement getAchievement(String szKey) {
        for (int i = 0; i < this.marrAchievements.size(); i++) {
            Achievement oneAchievement = (Achievement) this.marrAchievements.get(i);
            if (oneAchievement.szKey.equalsIgnoreCase(szKey)) {
                return oneAchievement;
            }
        }
        return null;
    }

    public ArrayList<UsageTime> getUsageTime() {
        return this.marrUsageTime;
    }

    public ArrayList<UsageTime> getPhoneUsageTime() {
        return this.marrPhoneUsageTime;
    }

    public ArrayList<Subject> getSubjects() {
        return this.marrSubjects;
    }

    public ArrayList<Module> getModules() {
        return this.marrModules;
    }

    public String getMessage() {
        return this.mszPromptMessage;
    }

    public boolean isWebBrowserUnLimit() {
        return this.mbWebBrowserUnlimit;
    }

    public ArrayList<String> getUrlWhiteLists() {
        return this.marrWhiteList;
    }

    public ArrayList<String> getUrlBlackLists() {
        return this.marrBlackList;
    }

    public boolean isUrlInWhiteList(String szUrl) {
        for (int i = 0; i < this.marrWhiteList.size(); i++) {
            String szList = (String) this.marrWhiteList.get(i);
            if (szList.indexOf("*") == -1) {
                if (szList.equalsIgnoreCase(szUrl)) {
                    return true;
                }
            } else if (szUrl.matches(szList.replaceAll(".", "[$0]").replace("[*]", ".*"))) {
                return true;
            }
        }
        return false;
    }

    public boolean isUrlInBlackList(String szUrl) {
        for (int i = 0; i < this.marrBlackList.size(); i++) {
            String szList = (String) this.marrBlackList.get(i);
            if (szList.indexOf("*") == -1) {
                if (szList.equalsIgnoreCase(szUrl)) {
                    return true;
                }
            } else if (szUrl.matches(szList.replaceAll(".", "[$0]").replace("[*]", ".*"))) {
                return true;
            }
        }
        return false;
    }

    public void addScript(String szScriptText) {
        this.marrScript.add(szScriptText);
    }

    public void generateRemoveLimitScript() {
        this.marrScript.add("iptables -F");
        this.marrScript.add("iptables -X");
        this.marrScript.add("iptables -t nat -F");
        this.marrScript.add("iptables -t nat -X");
        this.marrScript.add("iptables -t mangle -F");
        this.marrScript.add("iptables -t mangle -X");
        this.marrScript.add("iptables -P INPUT ACCEPT");
        this.marrScript.add("iptables -P FORWARD ACCEPT");
        this.marrScript.add("iptables -P OUTPUT ACCEPT");
    }

    public void generateHostLimitScript() {
        Log.i("ServerConfiguration", "generateHostLimitScript()");
        this.marrScript.add("iptables -F");
        this.marrScript.add("iptables -X");
        this.marrScript.add("iptables -t nat -F");
        this.marrScript.add("iptables -t nat -X");
        this.marrScript.add("iptables -t mangle -F");
        this.marrScript.add("iptables -t mangle -X");
        this.marrScript.add("iptables -P INPUT ACCEPT");
        this.marrScript.add("iptables -P FORWARD ACCEPT");
        this.marrScript.add("iptables -P OUTPUT ACCEPT");
        int i;
        if (this.mbWebBrowserUnlimit) {
            for (i = 0; i < this.marrBlackList.size(); i++) {
                this.marrScript.add("iptables -A INPUT -i eth0 -m string --algo bm --string \"" + ((String) this.marrBlackList.get(i)) + "\" -j DROP");
                this.marrScript.add("iptables -A OUTPUT -m string --algo bm --string \"" + ((String) this.marrBlackList.get(i)) + "\" -j DROP");
                this.marrScript.add("iptables -A FORWARD -i eth0 -m string --algo bm --string \"" + ((String) this.marrBlackList.get(i)) + "\" -j DROP");
            }
            return;
        }
        this.marrScript.add("iptables -A OUTPUT -p udp --dport 53 -j ACCEPT");
        this.marrScript.add("iptables -A INPUT -m pkttype --pkt-type multicast -j ACCEPT");
        this.marrScript.add("iptables -A INPUT -m pkttype --pkt-type broadcast -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -p udp --dport 123 -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -p tcp --sport 8080 -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -p tcp --sport 8081 -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -p tcp --sport 8011 -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT --dport 5554 -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT --dport 5555 -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -d 121.14.30.153 -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -d rp.crasheye.cn -j ACCEPT");
        if (!MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress.isEmpty()) {
            this.marrScript.add("iptables -A OUTPUT -d " + MyiBaseApplication.getCommonVariables().ServerInfo.getServerHost() + " -j ACCEPT");
            if (!MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL.isEmpty()) {
                try {
                    this.marrScript.add("iptables -A OUTPUT -d " + new URL(MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL).getHost() + " -j ACCEPT");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        this.marrScript.add("iptables -A OUTPUT -d webservice.myi.cn -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -d updates.myi.cn -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -p icmp -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -p tcp --dport 50007 -j ACCEPT");
        this.marrScript.add("iptables -A OUTPUT -p tcp --dport 50008 -j ACCEPT");
        for (i = 0; i < this.marrWhiteList.size(); i++) {
            this.marrScript.add("iptables -A OUTPUT -d " + ((String) this.marrWhiteList.get(i)) + " -j ACCEPT");
            this.marrScript.add("iptables -A OUTPUT -m string --algo bm --string \"" + ((String) this.marrWhiteList.get(i)) + "\" -j ACCEPT");
            this.marrScript.add("iptables -A FORWARD -m string --algo bm --string \"" + ((String) this.marrWhiteList.get(i)) + "\" -j ACCEPT");
        }
        this.marrScript.add("iptables -P INPUT ACCEPT");
        this.marrScript.add("iptables -P OUTPUT DROP");
    }

    public static void setExecuteScriptCallBack(ExecuteScriptCallBack CallBack) {
        mScriptCallBack = CallBack;
    }

    public boolean executeScripts(ExecuteScriptCallBack CallBack) {
        for (int i = 0; i < this.marrScript.size(); i++) {
            if (!CallBack.onExecuteScript((String) this.marrScript.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean executeScripts() {
        if (mScriptCallBack == null) {
            return false;
        }
        if (this.marrScript.size() > 10) {
            new ScriptlaunchThread().start();
        } else {
            executeScripts(mScriptCallBack);
        }
        return true;
    }

    public boolean executeScripts(boolean bForceSychronize) {
        if (mScriptCallBack == null) {
            return false;
        }
        if (bForceSychronize) {
            executeScripts(mScriptCallBack);
        } else {
            new ScriptlaunchThread().start();
        }
        return true;
    }

    public void clearScripts() {
        this.marrScript.clear();
    }

    private Element getXMLNode(String szXPath, Element RootElement) {
        try {
            NodeList nodes = (NodeList) XPathFactory.newInstance().newXPath().evaluate(szXPath, RootElement, XPathConstants.NODESET);
            if (nodes.getLength() >= 1) {
                return (Element) nodes.item(0);
            }
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private NodeList getXMLNodes(String szXPath, Element RootElement) {
        try {
            return (NodeList) XPathFactory.newInstance().newXPath().evaluate(szXPath, RootElement, XPathConstants.NODESET);
        } catch (XPathExpressionException e1) {
            e1.printStackTrace();
            return null;
        }
    }
}
