package com.netspace.library.parser;

import android.content.Context;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.controls.HTMLTextView;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.utilities.XMLParser;
import com.netspace.pad.library.R;
import io.vov.vitamio.MediaMetadataRetriever;
import org.w3c.dom.Element;

public class ResourceBase extends XMLParser {
    public static final int LEFTMARGIN = 20;
    public static final int RIGHTMARGIN = 20;
    public static final int TOPMARGIN = 20;
    protected static int mTitleLayoutID = R.layout.layout_resourcetitle;
    protected ResourceBaseCallBack mCallBack;
    protected Context mContext;
    protected String mErrorText;
    protected String mObjectName = "Resource";

    public interface ResourceBaseCallBack {
        boolean onAfterLayoutComplete(ResourceBase resourceBase);
    }

    public boolean initialize(Context Context, String szXMLContent) {
        this.mContext = Context;
        if (szXMLContent == null) {
            this.mErrorText = "输入数据为空。";
            return false;
        }
        String szReturnHTML = "";
        if (szXMLContent.contains("<Error")) {
            szXMLContent.replace("<", "&lt;");
            szXMLContent.replace(">", "&gt;");
            this.mErrorText = "数据无效，返回内容为：" + szXMLContent;
            return false;
        } else if (!super.parseXML(szXMLContent)) {
            this.mErrorText = "XML解析错误";
            return false;
        } else if (testFormat()) {
            return true;
        } else {
            this.mErrorText = "数据格式无效。";
            return false;
        }
    }

    public void setObjectName(String szObjectName) {
        this.mObjectName = szObjectName;
    }

    public void setCallBack(ResourceBaseCallBack ResourceBaseCallBack) {
        this.mCallBack = ResourceBaseCallBack;
    }

    public boolean testFormat() {
        if (getXMLNode("/wmStudy/" + this.mObjectName) == null) {
            return false;
        }
        return true;
    }

    public String getGUID() {
        Element Node = getXMLNode("/wmStudy/" + this.mObjectName);
        String szResult = "";
        if (Node != null) {
            return Node.getAttribute("guid").toString();
        }
        return szResult;
    }

    public boolean setGUID(String szGUID) {
        Element Node = getXMLNode("/wmStudy/" + this.mObjectName);
        if (Node == null) {
            return false;
        }
        Node.setAttribute("guid", szGUID);
        return true;
    }

    public String getTitle() {
        return getXMLNode("/wmStudy/" + this.mObjectName).getAttribute("title").toString();
    }

    public void setTitle(String szTitle) {
        getXMLNode("/wmStudy/" + this.mObjectName).setAttribute("title", szTitle);
    }

    public boolean display(LinearLayout RootLayout) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return false;
        }
        throw new IllegalStateException("This method should be called from the main/UI thread.");
    }

    public boolean addLog(String szText, String szName, String szGUID) {
        Element LogNode = getXMLNode("/wmStudy/" + this.mObjectName + "/Logs");
        if (LogNode == null) {
            return false;
        }
        Element NewLogNode = this.mRootDocument.createElement("Log");
        NewLogNode.setAttribute(MediaMetadataRetriever.METADATA_KEY_DATE, Utilities.getNow());
        NewLogNode.setAttribute("operatorName", szName);
        NewLogNode.setAttribute("operatorGUID", szGUID);
        NewLogNode.setTextContent(szText);
        LogNode.appendChild(NewLogNode);
        return true;
    }

    public static void setTitleLayoutID(int nLayoutID) {
        mTitleLayoutID = nLayoutID;
    }

    public void displayTitlebar(LinearLayout RootLayout, String szTitle, String szSummery) {
        View v = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(mTitleLayoutID, null);
        RootLayout.addView(v);
        LayoutParams LayoutParam = (LayoutParams) v.getLayoutParams();
        LayoutParam.bottomMargin = 20;
        v.setLayoutParams(LayoutParam);
        TextView textSubTitle = (TextView) v.findViewById(R.id.TextViewSubTitle);
        ((TextView) v.findViewById(R.id.TextViewTitle)).setText(szTitle);
        textSubTitle.setText(Html.fromHtml(szSummery));
    }

    public void displayContent(LinearLayout RootLayout, String szMainContent) {
        int nWidth = Utilities.getScreenWidth(this.mContext);
        HTMLTextView textView = new HTMLTextView(this.mContext);
        textView.setScreenHeight((Utilities.getScreenHeight(this.mContext) - 40) - 100);
        textView.setText(szMainContent);
        textView.setMargin(20, 0, 20, 0);
        RootLayout.addView(textView, new ViewGroup.LayoutParams(nWidth, textView.getEstHeight(nWidth)));
    }

    public String getErrorText() {
        return this.mErrorText;
    }

    public String getXML() {
        return Utilities.XMLToString(this.mRootDocument, true);
    }
}
