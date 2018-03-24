package com.netspace.library.parser;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.netspace.library.controls.CustomCameraView;
import com.netspace.library.controls.CustomDrawView;
import com.netspace.library.controls.CustomSelectorView;
import com.netspace.library.controls.CustomTextView;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import com.xsj.crasheye.Properties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class UserResourceParser {
    private static final String TAG = "UserResourceParser";
    private static boolean mUseScaledBitmap = false;
    private final int TAGID_CLASSNAME = R.id.textView1;
    private final int TAGID_DATA = R.id.textView3;
    private final int TAGID_ID = R.id.textView2;
    private int mLastSelectorViewIndex;
    private Context m_Context;
    private String m_szXMLContent = null;

    public UserResourceParser(Context Context) {
        this.m_Context = Context;
    }

    public static void setUseScaledBitmap(boolean bUse) {
        mUseScaledBitmap = bUse;
    }

    public String SaveToString(CustomSelectorView selectorView) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element RootElement = document.createElement("LessonsScheduleUserAnswer");
            document.setXmlVersion(Properties.REST_VERSION);
            document.appendChild(RootElement);
            RootElement.setAttribute(ClientCookie.VERSION_ATTR, Properties.REST_VERSION);
            if (selectorView instanceof CustomSelectorView) {
                LayoutParams LayoutParam = (LayoutParams) selectorView.getLayoutParams();
                CustomSelectorView TempView = selectorView;
                Element OneElement = document.createElement("SelectorView");
                OneElement.setAttribute("height", String.valueOf(LayoutParam.height));
                OneElement.setTextContent(TempView.getValue());
                RootElement.appendChild(OneElement);
            }
            return Utilities.XMLToString(document);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String SaveToString(ResourceItemData ItemData, LinearLayout RootLayout) {
        try {
            int i;
            Element OneElement;
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element RootElement = document.createElement("LessonsScheduleUserAnswer");
            document.setXmlVersion(Properties.REST_VERSION);
            document.appendChild(RootElement);
            RootElement.setAttribute(ClientCookie.VERSION_ATTR, Properties.REST_VERSION);
            RootElement.setAttribute("resourceGUID", ItemData.szResourceGUID);
            if (ItemData.bFav) {
                RootElement.setAttribute("isFav", "true");
            }
            if (ItemData.bLocked) {
                RootElement.setAttribute("isLocked", "true");
            }
            if (ItemData.bRead) {
                RootElement.setAttribute("isReaded", "true");
            }
            if (ItemData.bThumbDown) {
                RootElement.setAttribute("voteBad", "true");
            }
            if (ItemData.bThumbUp) {
                RootElement.setAttribute("voteGood", "true");
            }
            if (ItemData.bAnswered) {
                RootElement.setAttribute("answered", "true");
            }
            for (i = 0; i < RootLayout.getChildCount(); i++) {
                View OneView = RootLayout.getChildAt(i);
                LayoutParams LayoutParam = (LayoutParams) OneView.getLayoutParams();
                if (OneView instanceof CustomTextView) {
                    CustomTextView TempView = (CustomTextView) OneView;
                    OneElement = document.createElement("TextView");
                    OneElement.setAttribute("height", String.valueOf(LayoutParam.height));
                    OneElement.setTextContent(TempView.getData());
                    RootElement.appendChild(OneElement);
                } else if (OneView instanceof CustomDrawView) {
                    CustomDrawView TempView2 = (CustomDrawView) OneView;
                    OneElement = document.createElement("DrawView");
                    OneElement.setAttribute("height", String.valueOf(LayoutParam.height));
                    OneElement.setTextContent(TempView2.getData());
                    RootElement.appendChild(OneElement);
                } else if (OneView instanceof CustomSelectorView) {
                    CustomSelectorView TempView3 = (CustomSelectorView) OneView;
                    OneElement = document.createElement("SelectorView");
                    OneElement.setAttribute("height", String.valueOf(LayoutParam.height));
                    OneElement.setTextContent(TempView3.getValue());
                    RootElement.appendChild(OneElement);
                } else if (OneView instanceof CustomCameraView) {
                    CustomCameraView TempView4 = (CustomCameraView) OneView;
                    OneElement = document.createElement("CameraView");
                    String szFileName = TempView4.getImageFileName(true);
                    OneElement.setAttribute("height", String.valueOf(LayoutParam.height));
                    if (!(szFileName == null || szFileName.isEmpty())) {
                        OneElement.setAttribute("key", Utilities.getFileName(szFileName));
                    }
                    RootElement.appendChild(OneElement);
                }
            }
            View ParentView = (View) RootLayout.getParent();
            if (ParentView instanceof RelativeLayout) {
                RelativeLayout Layout = (RelativeLayout) ParentView;
                for (i = 0; i < Layout.getChildCount(); i++) {
                    View ChildView = Layout.getChildAt(i);
                    if (ChildView instanceof ImageView) {
                        String szClassName = (String) ChildView.getTag(this.TAGID_CLASSNAME);
                        int nID = Integer.valueOf((String) ChildView.getTag(this.TAGID_ID)).intValue();
                        String szData = (String) ChildView.getTag(this.TAGID_DATA);
                        RelativeLayout.LayoutParams Params = (RelativeLayout.LayoutParams) ChildView.getLayoutParams();
                        OneElement = document.createElement("WindowView");
                        OneElement.setAttribute("type", szClassName);
                        OneElement.setAttribute("id", String.valueOf(nID));
                        OneElement.setAttribute("left", String.valueOf(Params.leftMargin));
                        OneElement.setAttribute("top", String.valueOf(Params.topMargin));
                        OneElement.setTextContent(szData);
                        RootElement.appendChild(OneElement);
                    }
                }
            }
            return Utilities.XMLToString(document);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private CustomSelectorView getSelectorView(LinearLayout RootLayout) {
        for (int i = 0; i < RootLayout.getChildCount(); i++) {
            if (RootLayout.getChildAt(i) instanceof CustomSelectorView) {
                return (CustomSelectorView) RootLayout.getChildAt(i);
            }
        }
        return null;
    }

    private CustomSelectorView getSelectorViewInOrder(LinearLayout RootLayout) {
        for (int i = this.mLastSelectorViewIndex; i < RootLayout.getChildCount(); i++) {
            if (RootLayout.getChildAt(i) instanceof CustomSelectorView) {
                this.mLastSelectorViewIndex = i + 1;
                return (CustomSelectorView) RootLayout.getChildAt(i);
            }
        }
        return null;
    }

    public boolean Load(String szXMLContent, ResourceItemData ItemData, LinearLayout RootLayout) {
        return Load(szXMLContent, ItemData, RootLayout, "");
    }

    public boolean Load(String szXMLContent, ResourceItemData ItemData, LinearLayout RootLayout, String szClientID) {
        boolean bResult = false;
        try {
            Element RootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXMLContent.getBytes(HTTP.UTF_8))).getDocumentElement();
            if (!RootElement.getTagName().equalsIgnoreCase("LessonsScheduleUserAnswer")) {
                return false;
            }
            if (!RootElement.hasAttribute(ClientCookie.VERSION_ATTR)) {
                return false;
            }
            if (!RootElement.getAttribute(ClientCookie.VERSION_ATTR).equalsIgnoreCase(Properties.REST_VERSION)) {
                return false;
            }
            boolean z = RootElement.hasAttribute("isFav") && RootElement.getAttribute("isFav").equalsIgnoreCase("true");
            ItemData.bFav = z;
            z = RootElement.hasAttribute("isLocked") && RootElement.getAttribute("isLocked").equalsIgnoreCase("true");
            ItemData.bLocked = z;
            z = RootElement.hasAttribute("isReaded") && RootElement.getAttribute("isReaded").equalsIgnoreCase("true");
            ItemData.bRead = z;
            z = RootElement.hasAttribute("voteBad") && RootElement.getAttribute("voteBad").equalsIgnoreCase("true");
            ItemData.bThumbDown = z;
            z = RootElement.hasAttribute("voteGood") && RootElement.getAttribute("voteGood").equalsIgnoreCase("true");
            ItemData.bThumbUp = z;
            z = RootElement.hasAttribute("answered") && RootElement.getAttribute("answered").equalsIgnoreCase("true");
            ItemData.bAnswered = z;
            this.mLastSelectorViewIndex = 0;
            for (int i = 0; i < RootElement.getChildNodes().getLength(); i++) {
                Element OneElement = (Element) RootElement.getChildNodes().item(i);
                String szTagName = OneElement.getTagName();
                int nHeight = -2;
                bResult = true;
                if (OneElement.hasAttribute("height")) {
                    nHeight = Integer.valueOf(OneElement.getAttribute("height")).intValue();
                }
                if (szTagName.equalsIgnoreCase("SelectorView")) {
                    CustomSelectorView OneView = getSelectorViewInOrder(RootLayout);
                    if (OneView == null) {
                        Log.e(TAG, "Must add selectorview first before call ParserXML");
                    } else {
                        OneView.putValue(OneElement.getTextContent());
                    }
                    OneView.setLocked(ItemData.bLocked);
                } else if (szTagName.equalsIgnoreCase("TextView")) {
                    CustomTextView TextView = new CustomTextView(this.m_Context);
                    TextView.putData(OneElement.getTextContent());
                    RootLayout.addView(TextView);
                    LayoutParam = (LayoutParams) TextView.getLayoutParams();
                    LayoutParam.leftMargin = 20;
                    LayoutParam.rightMargin = 20;
                    LayoutParam.topMargin = 20;
                    LayoutParam.height = nHeight;
                    TextView.setLayoutParams(LayoutParam);
                    TextView.setLocked(ItemData.bLocked);
                } else if (szTagName.equalsIgnoreCase("DrawView")) {
                    CustomDrawView DrawView = new CustomDrawView(this.m_Context);
                    RootLayout.addView(DrawView);
                    DrawView.putData(OneElement.getTextContent());
                    LayoutParam = (LayoutParams) DrawView.getLayoutParams();
                    LayoutParam.leftMargin = 20;
                    LayoutParam.rightMargin = 20;
                    LayoutParam.topMargin = 20;
                    LayoutParam.height = nHeight;
                    DrawView.setLayoutParams(LayoutParam);
                    DrawView.setLocked(ItemData.bLocked);
                } else if (szTagName.equalsIgnoreCase("CameraView")) {
                    CustomCameraView CameraView = new CustomCameraView(this.m_Context);
                    RootLayout.addView(CameraView);
                    LayoutParam = (LayoutParams) CameraView.getLayoutParams();
                    LayoutParam.leftMargin = 20;
                    LayoutParam.rightMargin = 20;
                    LayoutParam.topMargin = 20;
                    LayoutParam.height = nHeight;
                    CameraView.setLayoutParams(LayoutParam);
                    CameraView.setLocked(ItemData.bLocked);
                    if (OneElement.hasAttribute("key")) {
                        String szKey = OneElement.getAttribute("key");
                        if (!(szKey == null || szKey.isEmpty())) {
                            CustomCameraView TargetCameraView = CameraView;
                            TargetCameraView.setImageKey(szKey, szClientID);
                            TargetCameraView.setUseDownScaleBitmap(mUseScaledBitmap);
                        }
                    }
                    CameraView.resetChangeFlag();
                } else if (szTagName.equalsIgnoreCase("WindowView")) {
                    View ParentView = (View) RootLayout.getParent();
                    if (ParentView instanceof RelativeLayout) {
                        String szClassName = OneElement.getAttribute("type");
                        int nID = Integer.valueOf(OneElement.getAttribute("id")).intValue();
                        String szData = OneElement.getTextContent();
                        int nLeft = Integer.valueOf(OneElement.getAttribute("left")).intValue();
                        int nTop = Integer.valueOf(OneElement.getAttribute("top")).intValue();
                        RelativeLayout RelativeLayout = (RelativeLayout) ParentView;
                        ImageView ImageView = new ImageView(this.m_Context);
                        ImageView.setTag(this.TAGID_CLASSNAME, szClassName);
                        ImageView.setTag(this.TAGID_ID, String.valueOf(nID));
                        ImageView.setTag(this.TAGID_DATA, szData);
                        RelativeLayout.addView(ImageView);
                        RelativeLayout.LayoutParams Params = (RelativeLayout.LayoutParams) ImageView.getLayoutParams();
                        Params.leftMargin = nLeft;
                        Params.topMargin = nTop;
                        ImageView.setLayoutParams(Params);
                    }
                }
            }
            return bResult;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (ParserConfigurationException e3) {
            e3.printStackTrace();
        }
    }

    public boolean getAttachments(String szXMLContent, ArrayList<String> arrAttachments) {
        boolean bResult = false;
        try {
            Element RootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXMLContent.getBytes(HTTP.UTF_8))).getDocumentElement();
            if (!RootElement.getTagName().equalsIgnoreCase("LessonsScheduleUserAnswer")) {
                return false;
            }
            if (!RootElement.hasAttribute(ClientCookie.VERSION_ATTR)) {
                return false;
            }
            if (!RootElement.getAttribute(ClientCookie.VERSION_ATTR).equalsIgnoreCase(Properties.REST_VERSION)) {
                return false;
            }
            for (int i = 0; i < RootElement.getChildNodes().getLength(); i++) {
                Element OneElement = (Element) RootElement.getChildNodes().item(i);
                bResult = true;
                if (OneElement.getTagName().equalsIgnoreCase("CameraView") && OneElement.hasAttribute("key")) {
                    String szKey = OneElement.getAttribute("key");
                    if (!szKey.isEmpty()) {
                        arrAttachments.add(szKey);
                    }
                }
            }
            return bResult;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (ParserConfigurationException e3) {
            e3.printStackTrace();
        }
    }

    public String getXMLContent() {
        return this.m_szXMLContent;
    }
}
