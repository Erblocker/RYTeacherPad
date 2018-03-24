package com.netspace.library.parser;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.controls.CustomVideoPlayerWrapper;
import com.netspace.library.struct.LessonClassItemData;
import com.netspace.library.utilities.FileDownloader;
import com.netspace.library.utilities.FileDownloader.OnDownloadEventListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import io.vov.vitamio.MediaMetadataRetriever;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LessonClassParser extends ResourceBase {
    private LinearLayout mRootLayout;
    private String mszNewVersionBackCameraFileName;
    private String mszNewVersionForewardCameraFileName;
    private String mszNewVersionMainCameraFileName;

    public LessonClassParser() {
        this.mObjectName = "LessonClass";
    }

    public LessonClassParser(String szResourceXMLContent) {
        this.mObjectName = "LessonClass";
        try {
            this.mRootDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szResourceXMLContent.getBytes(HTTP.UTF_8)));
            this.mRootElement = this.mRootDocument.getDocumentElement();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (ParserConfigurationException e3) {
            e3.printStackTrace();
        }
    }

    public boolean Initialize(String szResourceXMLContent) {
        try {
            this.mRootDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szResourceXMLContent.getBytes(HTTP.UTF_8)));
            this.mRootElement = this.mRootDocument.getDocumentElement();
            Element rootElement = getXMLNode("/wmStudy/LessonClass");
            if (rootElement == null) {
                return false;
            }
            if (rootElement.hasAttribute("cameraConfig")) {
                try {
                    int i;
                    JSONArray json = new JSONArray(rootElement.getAttribute("cameraConfig"));
                    String szFileNameBase = rootElement.getAttribute("guid") + "_All_";
                    ArrayList<String> arrCameraIndex = new ArrayList();
                    String[] arrFinalFileNames = new String[3];
                    for (i = 0; i < json.length(); i++) {
                        JSONObject oneCamera = json.getJSONObject(i);
                        if (oneCamera.getInt("isvideo") == 1 && arrCameraIndex.size() < 3) {
                            arrCameraIndex.add(String.valueOf(oneCamera.getInt("index")));
                        }
                    }
                    if (arrCameraIndex.size() > 1) {
                        for (i = 0; i < arrCameraIndex.size(); i++) {
                            arrFinalFileNames[i] = new StringBuilder(String.valueOf(szFileNameBase)).append((String) arrCameraIndex.get(i)).append("_").toString();
                            for (int j = 0; j < arrCameraIndex.size(); j++) {
                                if (i != j) {
                                    arrFinalFileNames[i] = arrFinalFileNames[i] + ((String) arrCameraIndex.get(j));
                                }
                            }
                            if (arrFinalFileNames[i].endsWith("_")) {
                                arrFinalFileNames[i] = arrFinalFileNames[i].substring(0, arrFinalFileNames[i].length() - 1);
                            }
                            arrFinalFileNames[i] = arrFinalFileNames[i] + ".m3u8";
                        }
                    } else {
                        arrFinalFileNames[0] = rootElement.getAttribute("guid") + "_All" + ".m3u8";
                    }
                    this.mszNewVersionMainCameraFileName = arrFinalFileNames[0];
                    this.mszNewVersionBackCameraFileName = arrFinalFileNames[1];
                    this.mszNewVersionForewardCameraFileName = arrFinalFileNames[2];
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return true;
        } catch (SAXException e2) {
            e2.printStackTrace();
            return false;
        } catch (IOException e3) {
            e3.printStackTrace();
            return false;
        } catch (ParserConfigurationException e4) {
            e4.printStackTrace();
            return false;
        }
    }

    public boolean initialize(Context Context, String szXMLContent) {
        if (!super.initialize(Context, szXMLContent)) {
            return false;
        }
        if (szXMLContent.contains("<Error")) {
            szXMLContent.replace("<", "&lt;");
            szXMLContent.replace(">", "&gt;");
            this.mErrorText = "数据无效，返回内容为：" + szXMLContent;
            return false;
        } else if (testFormat()) {
            return Initialize(szXMLContent);
        } else {
            this.mErrorText = "数据格式无效。";
            return false;
        }
    }

    public boolean readTimeLines(ArrayList<LessonClassItemData> arrData) {
        boolean bResult = false;
        NodeList TimeLineNodes = getXMLNodes("/wmStudy/LessonClass/TimeLine/Action");
        for (int i = 0; i < TimeLineNodes.getLength(); i++) {
            Element OneNode = (Element) TimeLineNodes.item(i);
            LessonClassItemData OneData = new LessonClassItemData();
            OneData.szTitle = OneNode.getAttribute("title");
            OneData.szMethod = OneNode.getAttribute("method");
            OneData.szTime = OneNode.getAttribute("timeOffset");
            OneData.szObjectGUID = OneNode.getAttribute("objectGUID");
            OneData.szResult = OneNode.getAttribute("result");
            OneData.szAnswerResult = OneNode.getAttribute("answerResult");
            Date date = new Date();
            try {
                date = new SimpleDateFormat("HH:mm:ss").parse(OneData.szTime);
                OneData.nTimeOffsetInSeconds = ((date.getHours() * 3600) + (date.getMinutes() * 60)) + date.getSeconds();
            } catch (Exception e) {
                e.printStackTrace();
            }
            arrData.add(OneData);
            bResult = true;
        }
        return bResult;
    }

    public String getVideoURL() {
        NodeList MediaNodes = getXMLNodes("/wmStudy/LessonClass/Multimedia/Media");
        String szResult = "";
        for (int i = 0; i < MediaNodes.getLength(); i++) {
            Element OneNode = (Element) MediaNodes.item(i);
            String szType = OneNode.getAttribute("type");
            if (szResult.isEmpty() || szType.equalsIgnoreCase("Screen")) {
                szResult = OneNode.getAttribute("fileName");
            }
            if (szType.equalsIgnoreCase("MixScreen")) {
                szResult = OneNode.getAttribute("fileName");
            }
            if (szType.equalsIgnoreCase("All") && !this.mszNewVersionMainCameraFileName.isEmpty()) {
                szResult = OneNode.getAttribute("fileName");
                szResult = szResult.substring(0, szResult.lastIndexOf("/")) + "/" + this.mszNewVersionMainCameraFileName;
            }
        }
        return szResult;
    }

    public String getCameraURL() {
        NodeList MediaNodes = getXMLNodes("/wmStudy/LessonClass/Multimedia/Media");
        String szResult = "";
        String szCameraURL = "";
        for (int i = 0; i < MediaNodes.getLength(); i++) {
            Element OneNode = (Element) MediaNodes.item(i);
            String szType = OneNode.getAttribute("type");
            if (szType.equalsIgnoreCase("Camera") && szCameraURL.isEmpty()) {
                szCameraURL = OneNode.getAttribute("fileName");
            }
            if (szType.equalsIgnoreCase("MixCamera")) {
                szCameraURL = OneNode.getAttribute("fileName");
            }
            if (szType.equalsIgnoreCase("All") && !this.mszNewVersionForewardCameraFileName.isEmpty()) {
                szResult = OneNode.getAttribute("fileName");
                szCameraURL = szResult.substring(0, szResult.lastIndexOf("/")) + "/" + this.mszNewVersionForewardCameraFileName;
            }
        }
        return szCameraURL;
    }

    public String getBackCameraURL() {
        NodeList MediaNodes = getXMLNodes("/wmStudy/LessonClass/Multimedia/Media");
        String szResult = "";
        String szCameraURL = "";
        String szBackCameraURL = "";
        for (int i = 0; i < MediaNodes.getLength(); i++) {
            Element OneNode = (Element) MediaNodes.item(i);
            String szType = OneNode.getAttribute("type");
            if (szType.equalsIgnoreCase("BackCamera") && szBackCameraURL.isEmpty()) {
                szBackCameraURL = OneNode.getAttribute("fileName");
            }
            if (szType.equalsIgnoreCase("MixBackCamera")) {
                szBackCameraURL = OneNode.getAttribute("fileName");
            }
            if (szType.equalsIgnoreCase("All") && !this.mszNewVersionBackCameraFileName.isEmpty()) {
                szResult = OneNode.getAttribute("fileName");
                szBackCameraURL = szResult.substring(0, szResult.lastIndexOf("/")) + "/" + this.mszNewVersionBackCameraFileName;
            }
        }
        return szBackCameraURL;
    }

    public String getMixVideo() {
        NodeList MediaNodes = getXMLNodes("/wmStudy/LessonClass/Multimedia/Media");
        String szResult = "";
        for (int i = 0; i < MediaNodes.getLength(); i++) {
            Element OneNode = (Element) MediaNodes.item(i);
            if (OneNode.getAttribute("type").equalsIgnoreCase("Mix")) {
                return OneNode.getAttribute("fileName");
            }
        }
        return "";
    }

    public String getTitle() {
        Element RootElement = getXMLNode("/wmStudy/LessonClass");
        if (RootElement != null) {
            return RootElement.getAttribute("title");
        }
        return null;
    }

    public String getSummery() {
        Element RootElement = getXMLNode("/wmStudy/LessonClass");
        String szAuthor = RootElement.getAttribute(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
        String szDate = RootElement.getAttribute(MediaMetadataRetriever.METADATA_KEY_DATE);
        return "由" + szAuthor + "老师在" + szDate + "给" + RootElement.getAttribute("userClassName") + "上课时记录。";
    }

    public boolean display(LinearLayout RootLayout) {
        super.display(RootLayout);
        this.mRootLayout = RootLayout;
        displayTitlebar(RootLayout, getTitle(), getSummery());
        CustomVideoPlayerWrapper PlayView = new CustomVideoPlayerWrapper(this.mContext);
        this.mRootLayout.addView(PlayView);
        int nVideoViewPos = this.mRootLayout.getChildCount() - 1;
        LayoutParams Params = (LayoutParams) PlayView.getLayoutParams();
        if (RootLayout.getResources().getConfiguration().orientation == 2) {
            Params.width = (int) (((float) Utilities.getScreenWidth(this.mContext)) * 0.78125f);
            Params.height = (int) (((float) Utilities.getScreenHeight(this.mContext)) * 0.6510417f);
        } else {
            Params.width = ((int) ((float) Utilities.getScreenWidth(this.mContext))) - Utilities.dpToPixel(80, this.mContext);
            Params.height = (((int) ((float) Params.width)) * 10) / 16;
        }
        Params.gravity = 1;
        PlayView.setLayoutParams(Params);
        if (VirtualNetworkObject.getOfflineMode()) {
            String szLocalFilePath = VirtualNetworkObject.getOfflineURL(getVideoURL());
            if (szLocalFilePath != null) {
                PlayView.setMediaURL("file://" + szLocalFilePath);
            } else {
                PlayView.setMediaURL(getVideoURL());
            }
        } else {
            PlayView.setMediaURL(getVideoURL());
        }
        LinearLayout LinearLayout = new LinearLayout(this.mContext);
        LinearLayout.setGravity(17);
        this.mRootLayout.addView(LinearLayout);
        LayoutParams Params2 = (LayoutParams) LinearLayout.getLayoutParams();
        Params2.topMargin = 10;
        LinearLayout.setLayoutParams(Params2);
        Button DownloadButton = Utilities.createThemedButton(this.mContext);
        DownloadButton.setText("下载当前视频");
        final int i = nVideoViewPos;
        DownloadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String szURL = ((CustomVideoPlayerWrapper) LessonClassParser.this.mRootLayout.getChildAt(i)).getMediaURL();
                String szFileName = szURL.substring(szURL.lastIndexOf("/") + 1);
                if (szURL.startsWith("http://")) {
                    szFileName = new StringBuilder(String.valueOf(LessonClassParser.this.mContext.getExternalCacheDir().getAbsolutePath())).append("/").append(szFileName).toString();
                    FileDownloader FileDownloader = new FileDownloader(LessonClassParser.this.mContext, "正在下载视频文件");
                    FileDownloader.setSuccessListener(new OnDownloadEventListener() {
                        public void onDownloadEvent(boolean bDownloadSuccess, String szURL, String szLocalFileName, String szErrorMessage) {
                            Utilities.showAlertMessage(LessonClassParser.this.mContext, "下载完成", "所需的视频已下载到本地，下次打开播放时将使用本地文件。");
                        }
                    });
                    FileDownloader.setFailListener(new OnDownloadEventListener() {
                        public void onDownloadEvent(boolean bDownloadSuccess, String szURL, String szLocalFileName, String szErrorMessage) {
                            Utilities.showAlertMessage(LessonClassParser.this.mContext, "下载错误", "下载文件出现错误。");
                            new File(szLocalFileName).delete();
                        }
                    });
                    FileDownloader.execute(new String[]{szURL, szFileName});
                    return;
                }
                Utilities.showAlertMessage(LessonClassParser.this.mContext, "下载错误", "当前播放的视频已经在本地了。");
            }
        });
        LinearLayout.addView(DownloadButton);
        LayoutParams Param2 = (LayoutParams) DownloadButton.getLayoutParams();
        Param2.rightMargin = 10;
        DownloadButton.setLayoutParams(Param2);
        String szURL = getVideoURL();
        ArrayList<String> arrOtherURLs = new ArrayList();
        String szCameraURL = "";
        String szBackCameraURL = "";
        szCameraURL = getCameraURL();
        szBackCameraURL = getBackCameraURL();
        if (!(szCameraURL.isEmpty() && szBackCameraURL.isEmpty())) {
            Button MainButton;
            LayoutParams Param;
            final int i2 = nVideoViewPos;
            OnClickListener SwitchButtonClickListener = new OnClickListener() {
                public void onClick(View v) {
                    CustomVideoPlayerWrapper PlayView = (CustomVideoPlayerWrapper) LessonClassParser.this.mRootLayout.getChildAt(i2);
                    String szURL = (String) v.getTag();
                    long nPlayPos = PlayView.getPos();
                    if (!PlayView.getMediaURL().equalsIgnoreCase(szURL)) {
                        LessonClassParser.this.mRootLayout.removeView(PlayView);
                        CustomVideoPlayerWrapper PlayView2 = new CustomVideoPlayerWrapper(LessonClassParser.this.mContext);
                        LessonClassParser.this.mRootLayout.addView(PlayView2, i2);
                        LayoutParams Params2 = (LayoutParams) PlayView.getLayoutParams();
                        Params2.width = (int) (((float) Utilities.getScreenWidth(LessonClassParser.this.mContext)) * 0.78125f);
                        Params2.height = (int) (((float) Utilities.getScreenHeight(LessonClassParser.this.mContext)) * 0.6510417f);
                        Params2.gravity = 1;
                        PlayView2.setLayoutParams(Params2);
                        PlayView2.setMediaURL(szURL, nPlayPos);
                        PlayView2.startPlay();
                    }
                }
            };
            String szMixURL = getMixVideo();
            if (!szMixURL.isEmpty()) {
                MainButton = Utilities.createThemedButton(this.mContext);
                MainButton.setText("黑板大屏幕混合");
                MainButton.setTag(szMixURL);
                MainButton.setOnClickListener(SwitchButtonClickListener);
                LinearLayout.addView(MainButton);
                Param = (LayoutParams) MainButton.getLayoutParams();
                Param.rightMargin = 10;
                MainButton.setLayoutParams(Param);
            }
            MainButton = Utilities.createThemedButton(this.mContext);
            MainButton.setText("大屏幕");
            MainButton.setTag(getVideoURL());
            MainButton.setOnClickListener(SwitchButtonClickListener);
            LinearLayout.addView(MainButton);
            if (!szCameraURL.isEmpty()) {
                Button MainButton2 = Utilities.createThemedButton(this.mContext);
                MainButton2.setText("学生");
                MainButton2.setTag(szCameraURL);
                MainButton2.setOnClickListener(SwitchButtonClickListener);
                LinearLayout.addView(MainButton2);
                Param = (LayoutParams) MainButton2.getLayoutParams();
                Param.leftMargin = 10;
                Param.rightMargin = 10;
                MainButton2.setLayoutParams(Param);
            }
            if (!szBackCameraURL.isEmpty()) {
                Button MainButton3 = Utilities.createThemedButton(this.mContext);
                MainButton3.setText("黑板");
                MainButton3.setTag(szBackCameraURL);
                MainButton3.setOnClickListener(SwitchButtonClickListener);
                LinearLayout.addView(MainButton3);
            }
        }
        return true;
    }
}
