package com.netspace.library.parser;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.netspace.library.components.AudioComponent;
import com.netspace.library.components.CameraComponent;
import com.netspace.library.components.DrawComponent;
import com.netspace.library.components.TextComponent;
import com.netspace.library.components.VideoComponent;
import com.netspace.library.controls.CustomSelectorView;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import com.xsj.crasheye.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.cookie.ClientCookie;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LessonsScheduleUserAnswerParser extends ResourceBase {
    private static final String TAG = "LessonsScheduleUserAnswerParser";
    private static boolean mUseScaledBitmap = false;
    private final int TAGID_CLASSNAME;
    private final int TAGID_DATA;
    private final int TAGID_ID;
    private String mClientID;
    private int mFirstContentTop;
    private boolean mHasRealAnswer;
    protected ResourceItemData mItemData;
    private int mLastLayoutHeight;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mRootLayout;
    private boolean mbAlignTop;
    private boolean mbNoNewControls;

    public LessonsScheduleUserAnswerParser() {
        this.mClientID = "";
        this.mLastLayoutHeight = 0;
        this.TAGID_CLASSNAME = R.id.textView1;
        this.TAGID_ID = R.id.textView2;
        this.TAGID_DATA = R.id.textView3;
        this.mFirstContentTop = -1;
        this.mItemData = new ResourceItemData();
        this.mbNoNewControls = false;
        this.mbAlignTop = false;
        this.mHasRealAnswer = false;
        this.mObjectName = "LessonsScheduleUserAnswer";
    }

    public LessonsScheduleUserAnswerParser(Context Context) {
        this.mClientID = "";
        this.mLastLayoutHeight = 0;
        this.TAGID_CLASSNAME = R.id.textView1;
        this.TAGID_ID = R.id.textView2;
        this.TAGID_DATA = R.id.textView3;
        this.mFirstContentTop = -1;
        this.mItemData = new ResourceItemData();
        this.mbNoNewControls = false;
        this.mbAlignTop = false;
        this.mHasRealAnswer = false;
        this.mContext = Context;
        this.mObjectName = "LessonsScheduleUserAnswer";
    }

    public static void setUseScaledBitmap(boolean bUse) {
        mUseScaledBitmap = bUse;
    }

    public void setClientID(String szClientID) {
        this.mClientID = szClientID;
    }

    public int getFirstContentTop() {
        if (this.mFirstContentTop >= 0) {
            return this.mFirstContentTop;
        }
        return 0;
    }

    public boolean getRealAnswered() {
        return this.mHasRealAnswer;
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
        } else if (!testFormat()) {
            this.mErrorText = "数据格式无效。";
            return false;
        } else if (!this.mRootElement.hasAttribute(ClientCookie.VERSION_ATTR)) {
            this.mErrorText = "缺少文件版本信息";
            return false;
        } else if (this.mRootElement.getAttribute(ClientCookie.VERSION_ATTR).equalsIgnoreCase(Properties.REST_VERSION)) {
            return true;
        } else {
            this.mErrorText = "文件版本不正确";
            return false;
        }
    }

    public boolean testFormat() {
        if (getXMLNode("/" + this.mObjectName) == null) {
            return false;
        }
        return true;
    }

    public boolean display(LinearLayout RootLayout) {
        return display(RootLayout, this.mItemData, this.mClientID);
    }

    public void setRelativeLayout(RelativeLayout relativeLayout) {
        this.mRelativeLayout = relativeLayout;
    }

    public ResourceItemData getItemData() {
        return this.mItemData;
    }

    public void setAlignTop(boolean bAlignTop) {
        this.mbAlignTop = bAlignTop;
    }

    public boolean getAlignTop() {
        return this.mbAlignTop;
    }

    public boolean display(LinearLayout RootLayout, ResourceItemData ItemData, String szClientID) {
        boolean bResult = false;
        this.mRootLayout = RootLayout;
        this.mbNoNewControls = true;
        if (ItemData == null) {
            ItemData = new ResourceItemData();
        }
        if (this.mRelativeLayout == null) {
            throw new IllegalArgumentException("New version use relativelayout to store items. Please use setRelativeLayout first.");
        }
        int i;
        Element OneElement;
        int nTop;
        this.mLastLayoutHeight = this.mRelativeLayout.getMeasuredHeight();
        boolean z = this.mRootElement.hasAttribute("isFav") && this.mRootElement.getAttribute("isFav").equalsIgnoreCase("true");
        ItemData.bFav = z;
        z = this.mRootElement.hasAttribute("isLocked") && this.mRootElement.getAttribute("isLocked").equalsIgnoreCase("true");
        ItemData.bLocked = z;
        z = this.mRootElement.hasAttribute("isReaded") && this.mRootElement.getAttribute("isReaded").equalsIgnoreCase("true");
        ItemData.bRead = z;
        z = this.mRootElement.hasAttribute("voteBad") && this.mRootElement.getAttribute("voteBad").equalsIgnoreCase("true");
        ItemData.bThumbDown = z;
        z = this.mRootElement.hasAttribute("voteGood") && this.mRootElement.getAttribute("voteGood").equalsIgnoreCase("true");
        ItemData.bThumbUp = z;
        z = this.mRootElement.hasAttribute("answered") && this.mRootElement.getAttribute("answered").equalsIgnoreCase("true");
        ItemData.bAnswered = z;
        int nCurrentHeight = Math.max(RootLayout.getHeight(), this.mRelativeLayout.getHeight());
        int nTopOffset = 0;
        int nMinTop = -1;
        if (this.mbAlignTop) {
            for (i = 0; i < this.mRootElement.getChildNodes().getLength(); i++) {
                OneElement = (Element) this.mRootElement.getChildNodes().item(i);
                if (!OneElement.getTagName().equalsIgnoreCase("SelectorView")) {
                    bResult = true;
                    if (OneElement.hasAttribute("top")) {
                        nTop = Integer.valueOf(OneElement.getAttribute("top")).intValue();
                        if (nTop < nMinTop || nMinTop == -1) {
                            nMinTop = nTop;
                        }
                    }
                }
            }
            if (nMinTop != -1) {
                nTopOffset = -nMinTop;
            }
            for (i = 0; i < RootLayout.getChildCount(); i++) {
                View OneView = RootLayout.getChildAt(i);
                if (OneView instanceof CustomSelectorView) {
                    nTopOffset = (nTopOffset + OneView.getBottom()) + Utilities.dpToPixel(10, this.mContext);
                }
            }
        }
        for (i = 0; i < this.mRootElement.getChildNodes().getLength(); i++) {
            OneElement = (Element) this.mRootElement.getChildNodes().item(i);
            String szTagName = OneElement.getTagName();
            int nHeight = -2;
            int nWidth = -1;
            nTop = -1;
            int nLeft = 0;
            bResult = true;
            if (OneElement.hasAttribute("height")) {
                nHeight = Integer.valueOf(OneElement.getAttribute("height")).intValue();
            }
            if (OneElement.hasAttribute("width")) {
                nWidth = Integer.valueOf(OneElement.getAttribute("width")).intValue();
            }
            if (OneElement.hasAttribute("top")) {
                nTop = Integer.valueOf(OneElement.getAttribute("top")).intValue() + nTopOffset;
                if (this.mFirstContentTop == -1 || this.mFirstContentTop > nTop) {
                    this.mFirstContentTop = nTop;
                }
            }
            if (OneElement.hasAttribute("left")) {
                nLeft = Integer.valueOf(OneElement.getAttribute("left")).intValue();
            }
            if (nTop == -1) {
                nTop = nCurrentHeight + Utilities.dpToPixel(10, this.mContext);
                nCurrentHeight += nHeight;
            }
            if (szTagName.equalsIgnoreCase("SelectorView")) {
                CustomSelectorView OneView2 = getSelectorView(RootLayout);
                if (OneView2 == null) {
                    Log.e(TAG, "Must add selectorview first before call ParserXML");
                } else {
                    OneView2.putValue(OneElement.getTextContent());
                    OneView2.setLocked(ItemData.bLocked);
                    OneView2.resetChangeFlag();
                }
            } else if (szTagName.equalsIgnoreCase("TextView")) {
                TextComponent TextComponent = new TextComponent(this.mContext);
                TextComponent.setData(OneElement.getTextContent());
                this.mRelativeLayout.addView(TextComponent);
                this.mbNoNewControls = false;
                LayoutParam = (LayoutParams) TextComponent.getLayoutParams();
                LayoutParam.leftMargin = nLeft;
                LayoutParam.topMargin = nTop;
                LayoutParam.height = nHeight;
                LayoutParam.width = nWidth;
                TextComponent.setLayoutParams(LayoutParam);
            } else if (szTagName.equalsIgnoreCase("DrawView")) {
                DrawComponent DrawView = new DrawComponent(this.mContext);
                this.mRelativeLayout.addView(DrawView);
                this.mbNoNewControls = false;
                DrawView.setData(OneElement.getTextContent());
                LayoutParam = (LayoutParams) DrawView.getLayoutParams();
                LayoutParam.leftMargin = nLeft;
                LayoutParam.topMargin = nTop;
                LayoutParam.height = nHeight;
                LayoutParam.width = nWidth;
                DrawView.setLayoutParams(LayoutParam);
            } else if (szTagName.equalsIgnoreCase("CameraView")) {
                CameraComponent CameraView = new CameraComponent(this.mContext);
                this.mRelativeLayout.addView(CameraView);
                this.mbNoNewControls = false;
                LayoutParam = (LayoutParams) CameraView.getLayoutParams();
                LayoutParam.leftMargin = nLeft;
                LayoutParam.topMargin = nTop;
                LayoutParam.height = nHeight;
                LayoutParam.width = nWidth;
                CameraView.setLayoutParams(LayoutParam);
                CameraView.setClientID(this.mClientID);
                CameraView.setData(OneElement.getAttribute("key"));
            } else if (szTagName.equalsIgnoreCase("AudioView")) {
                AudioComponent AudioView = new AudioComponent(this.mContext);
                this.mRelativeLayout.addView(AudioView);
                this.mbNoNewControls = false;
                LayoutParam = (LayoutParams) AudioView.getLayoutParams();
                LayoutParam.leftMargin = nLeft;
                LayoutParam.topMargin = nTop;
                LayoutParam.height = nHeight;
                LayoutParam.width = nWidth;
                AudioView.setLayoutParams(LayoutParam);
                AudioView.setData(OneElement.getAttribute("key"));
            } else if (szTagName.equalsIgnoreCase("VideoView")) {
                VideoComponent VideoView = new VideoComponent(this.mContext);
                this.mRelativeLayout.addView(VideoView);
                this.mbNoNewControls = false;
                LayoutParam = (LayoutParams) VideoView.getLayoutParams();
                LayoutParam.leftMargin = nLeft;
                LayoutParam.topMargin = nTop;
                LayoutParam.height = nHeight;
                LayoutParam.width = nWidth;
                VideoView.setLayoutParams(LayoutParam);
                VideoView.setData(OneElement.getAttribute("key"));
            } else if (szTagName.equalsIgnoreCase("WindowView")) {
                String szClassName = OneElement.getAttribute("type");
                int nID = Integer.valueOf(OneElement.getAttribute("id")).intValue();
                String szData = OneElement.getTextContent();
                RelativeLayout RelativeLayout = this.mRelativeLayout;
                ImageView ImageView = new ImageView(this.mContext);
                ImageView.setTag(this.TAGID_CLASSNAME, szClassName);
                ImageView.setTag(this.TAGID_ID, String.valueOf(nID));
                ImageView.setTag(this.TAGID_DATA, szData);
                RelativeLayout.addView(ImageView);
                this.mbNoNewControls = false;
                LayoutParams Params = (LayoutParams) ImageView.getLayoutParams();
                Params.leftMargin = nLeft;
                Params.topMargin = nTop;
                ImageView.setLayoutParams(Params);
            }
        }
        listenLayoutComplete();
        return bResult;
    }

    public String save(ResourceItemData ItemData, LinearLayout RootLayout) {
        this.mHasRealAnswer = false;
        if (this.mRelativeLayout == null) {
            throw new IllegalArgumentException("New version use relativelayout to store items. Please use setRelativeLayout first.");
        }
        try {
            int i;
            View OneView;
            CustomSelectorView TempView;
            Element OneElement;
            boolean bHasData = false;
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element mRootElement = document.createElement("LessonsScheduleUserAnswer");
            document.setXmlVersion(Properties.REST_VERSION);
            document.appendChild(mRootElement);
            mRootElement.setAttribute(ClientCookie.VERSION_ATTR, Properties.REST_VERSION);
            mRootElement.setAttribute("resourceGUID", ItemData.szResourceGUID);
            if (ItemData.bFav) {
                bHasData = true;
                mRootElement.setAttribute("isFav", "true");
            }
            if (ItemData.bLocked) {
                bHasData = true;
                mRootElement.setAttribute("isLocked", "true");
            }
            if (ItemData.bRead) {
                mRootElement.setAttribute("isReaded", "true");
            }
            if (ItemData.bThumbDown) {
                bHasData = true;
                mRootElement.setAttribute("voteBad", "true");
            }
            if (ItemData.bThumbUp) {
                bHasData = true;
                mRootElement.setAttribute("voteGood", "true");
            }
            if (ItemData.bAnswered) {
                bHasData = true;
                mRootElement.setAttribute("answered", "true");
            }
            for (i = 0; i < RootLayout.getChildCount(); i++) {
                OneView = RootLayout.getChildAt(i);
                LinearLayout.LayoutParams LayoutParam = (LinearLayout.LayoutParams) OneView.getLayoutParams();
                if (OneView instanceof CustomSelectorView) {
                    TempView = (CustomSelectorView) OneView;
                    OneElement = document.createElement("SelectorView");
                    OneElement.setAttribute("height", String.valueOf(LayoutParam.height));
                    OneElement.setTextContent(TempView.getValue());
                    mRootElement.appendChild(OneElement);
                    if (!OneElement.getTextContent().isEmpty()) {
                        bHasData = true;
                        this.mHasRealAnswer = true;
                    }
                }
            }
            for (i = 0; i < this.mRelativeLayout.getChildCount(); i++) {
                OneView = this.mRelativeLayout.getChildAt(i);
                LayoutParams LayoutParam2 = (LayoutParams) OneView.getLayoutParams();
                if (OneView instanceof TextComponent) {
                    TextComponent TempView2 = (TextComponent) OneView;
                    OneElement = document.createElement("TextView");
                    OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                    OneElement.setAttribute("width", String.valueOf(LayoutParam2.width));
                    OneElement.setAttribute("top", String.valueOf(LayoutParam2.topMargin));
                    OneElement.setAttribute("left", String.valueOf(LayoutParam2.leftMargin));
                    OneElement.setTextContent(TempView2.getData());
                    mRootElement.appendChild(OneElement);
                    if (!OneElement.getTextContent().isEmpty()) {
                        bHasData = true;
                        this.mHasRealAnswer = true;
                    }
                } else if (OneView instanceof DrawComponent) {
                    DrawComponent TempView3 = (DrawComponent) OneView;
                    OneElement = document.createElement("DrawView");
                    OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                    OneElement.setAttribute("width", String.valueOf(LayoutParam2.width));
                    OneElement.setAttribute("top", String.valueOf(LayoutParam2.topMargin));
                    OneElement.setAttribute("left", String.valueOf(LayoutParam2.leftMargin));
                    OneElement.setTextContent(TempView3.getData());
                    mRootElement.appendChild(OneElement);
                    if (!OneElement.getTextContent().isEmpty()) {
                        bHasData = true;
                        this.mHasRealAnswer = true;
                    }
                } else if (OneView instanceof CustomSelectorView) {
                    TempView = (CustomSelectorView) OneView;
                    OneElement = document.createElement("SelectorView");
                    OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                    OneElement.setTextContent(TempView.getValue());
                    mRootElement.appendChild(OneElement);
                    if (!OneElement.getTextContent().isEmpty()) {
                        bHasData = true;
                        this.mHasRealAnswer = true;
                    }
                } else if (OneView instanceof CameraComponent) {
                    CameraComponent TempView4 = (CameraComponent) OneView;
                    OneElement = document.createElement("CameraView");
                    szFileName = TempView4.getData();
                    OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                    OneElement.setAttribute("width", String.valueOf(LayoutParam2.width));
                    OneElement.setAttribute("top", String.valueOf(LayoutParam2.topMargin));
                    OneElement.setAttribute("left", String.valueOf(LayoutParam2.leftMargin));
                    if (!(szFileName == null || szFileName.isEmpty())) {
                        OneElement.setAttribute("key", szFileName);
                        bHasData = true;
                        this.mHasRealAnswer = true;
                    }
                    mRootElement.appendChild(OneElement);
                } else if (OneView instanceof AudioComponent) {
                    AudioComponent TempView5 = (AudioComponent) OneView;
                    OneElement = document.createElement("AudioView");
                    szFileName = TempView5.getData();
                    OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                    OneElement.setAttribute("width", String.valueOf(LayoutParam2.width));
                    OneElement.setAttribute("top", String.valueOf(LayoutParam2.topMargin));
                    OneElement.setAttribute("left", String.valueOf(LayoutParam2.leftMargin));
                    if (!(szFileName == null || szFileName.isEmpty())) {
                        OneElement.setAttribute("key", szFileName);
                        bHasData = true;
                        this.mHasRealAnswer = true;
                    }
                    mRootElement.appendChild(OneElement);
                } else if (OneView instanceof VideoComponent) {
                    VideoComponent TempView6 = (VideoComponent) OneView;
                    OneElement = document.createElement("VideoView");
                    szFileName = TempView6.getData();
                    OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                    OneElement.setAttribute("width", String.valueOf(LayoutParam2.width));
                    OneElement.setAttribute("top", String.valueOf(LayoutParam2.topMargin));
                    OneElement.setAttribute("left", String.valueOf(LayoutParam2.leftMargin));
                    if (!(szFileName == null || szFileName.isEmpty())) {
                        OneElement.setAttribute("key", szFileName);
                        bHasData = true;
                        this.mHasRealAnswer = true;
                    }
                    mRootElement.appendChild(OneElement);
                } else if (OneView instanceof ImageView) {
                    String szClassName = (String) OneView.getTag(this.TAGID_CLASSNAME);
                    int nID = Integer.valueOf((String) OneView.getTag(this.TAGID_ID)).intValue();
                    String szData = (String) OneView.getTag(this.TAGID_DATA);
                    LayoutParams Params = (LayoutParams) OneView.getLayoutParams();
                    OneElement = document.createElement("WindowView");
                    OneElement.setAttribute("type", szClassName);
                    OneElement.setAttribute("id", String.valueOf(nID));
                    OneElement.setAttribute("left", String.valueOf(Params.leftMargin));
                    OneElement.setAttribute("top", String.valueOf(Params.topMargin));
                    OneElement.setTextContent(szData);
                    if (!szData.isEmpty()) {
                        bHasData = true;
                        this.mHasRealAnswer = true;
                    }
                    mRootElement.appendChild(OneElement);
                }
            }
            if (bHasData) {
                return Utilities.XMLToString(document);
            }
            return "";
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void listenLayoutComplete() {
        this.mRelativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (!LessonsScheduleUserAnswerParser.this.mbNoNewControls && LessonsScheduleUserAnswerParser.this.mRelativeLayout.getMeasuredHeight() <= LessonsScheduleUserAnswerParser.this.mLastLayoutHeight) {
                    Log.d(LessonsScheduleUserAnswerParser.TAG, "Skip onGlobalLayout.");
                } else if (LessonsScheduleUserAnswerParser.this.mCallBack != null) {
                    if (!LessonsScheduleUserAnswerParser.this.mCallBack.onAfterLayoutComplete(LessonsScheduleUserAnswerParser.this)) {
                        return;
                    }
                    if (VERSION.SDK_INT > 15) {
                        LessonsScheduleUserAnswerParser.this.mRelativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        LessonsScheduleUserAnswerParser.this.mRelativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                } else if (VERSION.SDK_INT > 15) {
                    LessonsScheduleUserAnswerParser.this.mRelativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    LessonsScheduleUserAnswerParser.this.mRelativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private CustomSelectorView getSelectorView(LinearLayout RootLayout) {
        for (int i = 0; i < RootLayout.getChildCount(); i++) {
            if (RootLayout.getChildAt(i) instanceof CustomSelectorView) {
                return (CustomSelectorView) RootLayout.getChildAt(i);
            }
        }
        return null;
    }
}
