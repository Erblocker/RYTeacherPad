package com.netspace.library.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.controls.CustomAudioView;
import com.netspace.library.controls.CustomDocumentView;
import com.netspace.library.controls.CustomDocumentView.OnOpenDocumentListener;
import com.netspace.library.controls.CustomImageView;
import com.netspace.library.controls.CustomVideoPlayerWrapper;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ResourceParser extends ResourceBase {
    protected static final String TAG = "ResourceParser";
    protected static OnOpenDocumentListener mFlashOpenListener;
    private static String[] maudioextensions = new String[]{".3ga", ".a52", ".aac", ".ac3", ".adt", ".adts", ".aif", ".aifc", ".aiff", ".amr", ".aob", ".ape", ".awb", ".caf", ".dts", ".flac", ".it", ".m4a", ".m4p", ".mid", ".mka", ".mlp", ".mod", ".mpa", ".mp1", ".mp2", ".mp3", ".mpc", ".oga", ".ogg", ".oma", ".opus", ".ra", ".ram", ".rmi", ".s3m", ".spx", ".tta", ".voc", ".vqf", ".w64", ".wav", ".wma", ".wv", ".xa", ".xm"};
    private static String[] mdocumentextensions = new String[]{".pdf", ".doc", ".docx", ".ppt", ".pptx"};
    public static String mszEmptyResourceXML = "<wmStudy><Resource><KnowledgePoints/><Summery/><Content /><Attachments/><ContentRelationMap/><Refrences/><Logs/></Resource></wmStudy>";
    private static String mszHTMLExtName = "htm;html;shtml;";
    private static String mszPictureExtName = "bmp;jpg;jpeg;gif;tif;png;";
    private static String[] mvideoextensions = new String[]{".3g2", ".3gp", ".3gp2", ".3gpp", ".amv", ".asf", ".avi", ".divx", "drc", ".dv", ".f4v", ".flv", ".gvi", ".gxf", ".iso", ".m1v", ".m2v", ".m2t", ".m2ts", ".m4v", ".mkv", ".mov", ".mp2", ".mp2v", ".mp4", ".mp4v", ".mpe", ".mpeg", ".mpeg1", ".mpeg2", ".mpeg4", ".mpg", ".mpv2", ".mts", ".mtv", ".mxf", ".mxg", ".nsv", ".nuv", ".ogm", ".ogv", ".ogx", ".ps", ".rec", ".rm", ".rmvb", ".tod", ".ts", ".tts", ".vob", ".vro", ".webm", ".wm", ".wmv", ".wtv", ".xesc"};
    private int mLastLayoutHeight = 0;
    private String mMainFileURL;
    private String mPreviewFileURL;
    protected LinearLayout mRootLayout;

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
            String szPreviewFile = getContentPreviewFileURL();
            String szMainFile = getContentMainFileURL();
            String szFileToUse = "";
            this.mPreviewFileURL = szPreviewFile;
            if (szMainFile != null && !szMainFile.isEmpty()) {
                szFileToUse = szMainFile;
            } else if (!(szPreviewFile == null || szPreviewFile.isEmpty())) {
                szFileToUse = szPreviewFile;
            }
            this.mMainFileURL = szFileToUse;
            return true;
        } else {
            this.mErrorText = "数据格式无效。";
            return false;
        }
    }

    public boolean display(LinearLayout RootLayout) {
        String szPreviewData;
        super.display(RootLayout);
        this.mRootLayout = RootLayout;
        this.mLastLayoutHeight = this.mRootLayout.getMeasuredHeight();
        displayTitlebar(RootLayout, getTitle(), getSummeryNoBR());
        if (isPictureResource()) {
            szPreviewData = getPreviewThumbnailData();
            CustomImageView ImageView = new CustomImageView(this.mContext);
            if (!szPreviewData.isEmpty()) {
                ImageView.setImage(szPreviewData);
            }
            ImageView.setMainFileURL(getMainFileURL());
            RootLayout.addView(ImageView);
        }
        if (isVideoResource()) {
            CustomVideoPlayerWrapper PlayView = new CustomVideoPlayerWrapper(this.mContext);
            RootLayout.addView(PlayView);
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
                String szLocalFilePath = VirtualNetworkObject.getOfflineURL(getMainFileURL());
                if (szLocalFilePath != null) {
                    PlayView.setMediaURL("file://" + szLocalFilePath);
                } else {
                    PlayView.setMediaURL(getMainFileURL());
                }
            } else {
                PlayView.setMediaURL(getMainFileURL());
            }
            szPreviewData = getPreviewThumbnailData();
            if (!(szPreviewData == null || szPreviewData.isEmpty())) {
                PlayView.setThumbnail(szPreviewData);
            }
        }
        if (isAudioResource()) {
            CustomAudioView AudioView = new CustomAudioView(this.mContext);
            AudioView.setMainFileURL(getMainFileURL());
            RootLayout.addView(AudioView);
            LayoutParams LayoutParam = (LayoutParams) AudioView.getLayoutParams();
            LayoutParam.leftMargin = 20;
            LayoutParam.rightMargin = 20;
            AudioView.setLayoutParams(LayoutParam);
        }
        if (isDocumentResource()) {
            CustomDocumentView DocumentView = new CustomDocumentView(this.mContext);
            DocumentView.setMainFileURL(getMainFileURL());
            DocumentView.setPreviewFileURL(getPreviewFileURL());
            DocumentView.setOpenListener(mFlashOpenListener);
            RootLayout.addView(DocumentView);
            LayoutParam = (LayoutParams) DocumentView.getLayoutParams();
            LayoutParam.leftMargin = 20;
            LayoutParam.rightMargin = 20;
            DocumentView.setLayoutParams(LayoutParam);
        }
        if (isSWFResource()) {
            DocumentView = new CustomDocumentView(this.mContext);
            DocumentView.setFlashFileURL(getMainFileURL());
            DocumentView.setOpenListener(mFlashOpenListener);
            RootLayout.addView(DocumentView);
            LayoutParam = (LayoutParams) DocumentView.getLayoutParams();
            LayoutParam.leftMargin = 20;
            LayoutParam.rightMargin = 20;
            DocumentView.setLayoutParams(LayoutParam);
        }
        displayContent(RootLayout, getMainContentNoBR());
        listenLayoutComplete();
        return true;
    }

    protected void listenLayoutComplete() {
        this.mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (ResourceParser.this.mRootLayout.getMeasuredHeight() <= ResourceParser.this.mLastLayoutHeight) {
                    Log.d(ResourceParser.TAG, "Skip onGlobalLayout.");
                } else if (ResourceParser.this.mCallBack != null) {
                    if (!ResourceParser.this.mCallBack.onAfterLayoutComplete(ResourceParser.this)) {
                        return;
                    }
                    if (VERSION.SDK_INT > 15) {
                        ResourceParser.this.mRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        ResourceParser.this.mRootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                } else if (VERSION.SDK_INT > 15) {
                    ResourceParser.this.mRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    ResourceParser.this.mRootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    public String getMainFileURL() {
        return this.mMainFileURL;
    }

    public String getPreviewFileURL() {
        return this.mPreviewFileURL;
    }

    public static void setFlashOpenListener(OnOpenDocumentListener OnFlashOpenListener) {
        mFlashOpenListener = OnFlashOpenListener;
    }

    public String getSummery() {
        Element SummeryNode = getXMLNode("/wmStudy/Resource/Summery");
        String szResult = "";
        if (SummeryNode != null) {
            szResult = SummeryNode.getTextContent();
        }
        return szResult.replace("\n", "<br>\n");
    }

    public boolean setSummery(String szSummery) {
        Element SummeryNode = getXMLNode("/wmStudy/Resource/Summery");
        String szResult = "";
        if (SummeryNode == null) {
            return false;
        }
        if (SummeryNode.getChildNodes().getLength() == 0) {
            SummeryNode.appendChild(this.mRootDocument.createCDATASection(szSummery));
        } else {
            SummeryNode.getFirstChild().setTextContent(szSummery);
        }
        return true;
    }

    public String getSummeryNoBR() {
        Element SummeryNode = getXMLNode("/wmStudy/Resource/Summery");
        String szResult = "";
        if (SummeryNode != null) {
            return SummeryNode.getTextContent();
        }
        return szResult;
    }

    public boolean setThumbnail(Bitmap bitmap) {
        Element AttachmentsNode = getXMLNode("/wmStudy/Resource/Attachments");
        if (AttachmentsNode == null) {
            return false;
        }
        Element AttachmentNode = this.mRootDocument.createElement("Attachment");
        AttachmentNode.setAttribute("thumbnail", "true");
        AttachmentNode.setAttribute("encoding", "base64");
        AttachmentNode.setAttribute("contentType", "image/jpeg");
        AttachmentNode.appendChild(this.mRootDocument.createCDATASection(Utilities.saveBitmapToBase64String(bitmap)));
        AttachmentsNode.appendChild(AttachmentNode);
        return true;
    }

    public boolean addKnowledgePoints(String szKPPath, String szKPGUID) {
        if (szKPPath == null || szKPGUID == null) {
            return false;
        }
        Element KnowledgePointsNode = getXMLNode("/wmStudy/Resource/KnowledgePoints");
        if (KnowledgePointsNode == null) {
            return false;
        }
        Element KnowledgePointNode = this.mRootDocument.createElement("KnowledgePoint");
        KnowledgePointNode.setAttribute("path", szKPPath);
        KnowledgePointNode.setAttribute("guid", szKPGUID);
        KnowledgePointsNode.appendChild(KnowledgePointNode);
        return true;
    }

    public boolean getKnowledgePoints(ArrayList<String> arrKPPath, ArrayList<String> arrKPGUID) {
        Element KnowledgePointsNode = getXMLNode("/wmStudy/Resource/KnowledgePoints");
        if (KnowledgePointsNode == null) {
            return false;
        }
        for (int i = 0; i < KnowledgePointsNode.getChildNodes().getLength(); i++) {
            Element OneChild = (Element) KnowledgePointsNode.getChildNodes().item(i);
            arrKPPath.add(OneChild.getAttribute("path"));
            arrKPGUID.add(OneChild.getAttribute("guid"));
        }
        return true;
    }

    public String getMainFileExtName() {
        return getXMLNode("/wmStudy/Resource").getAttribute("mainFileExtName").toString().toLowerCase();
    }

    public boolean setAttribute(String szName, String szValue) {
        Element Node = getXMLNode("/wmStudy/Resource");
        if (Node == null) {
            return false;
        }
        Node.setAttribute(szName, szValue);
        return true;
    }

    public boolean setMainFileExtName(String szExtName) {
        return setAttribute("mainFileExtName", szExtName);
    }

    public boolean isPictureResource() {
        return mszPictureExtName.indexOf(getMainFileExtName()) != -1;
    }

    public boolean isHTMLResource() {
        return mszHTMLExtName.indexOf(getMainFileExtName()) != -1;
    }

    public boolean isSWFResource() {
        return getMainFileExtName().equalsIgnoreCase("swf");
    }

    public boolean isAudioVideoResource() {
        String szExtName = "." + getMainFileExtName().toLowerCase();
        for (String equals : mvideoextensions) {
            if (equals.equals(szExtName)) {
                return true;
            }
        }
        for (String equals2 : maudioextensions) {
            if (equals2.equals(szExtName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAudioResource() {
        String szExtName = "." + getMainFileExtName().toLowerCase();
        for (String equals : maudioextensions) {
            if (equals.equals(szExtName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isVideoResource() {
        String szExtName = "." + getMainFileExtName().toLowerCase();
        for (String equals : mvideoextensions) {
            if (equals.equals(szExtName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDocumentResource() {
        String szExtName = "." + getMainFileExtName().toLowerCase();
        for (String equals : mdocumentextensions) {
            if (equals.equals(szExtName)) {
                return true;
            }
        }
        return false;
    }

    public String getPreviewThumbnailData() {
        Element AttachmentNode = getXMLNode("/wmStudy/Resource/Attachments/Attachment[@thumbnail='true']");
        String szResult = "";
        if (AttachmentNode != null) {
            return AttachmentNode.getTextContent();
        }
        return szResult;
    }

    public String getMainContent() {
        Element Node = getXMLNode("/wmStudy/Resource/Content");
        String szResult = "";
        if (Node != null) {
            szResult = Node.getTextContent();
        }
        return szResult.replace("\n", "<br>\n");
    }

    public String getMainContentNoBR() {
        Element Node = getXMLNode("/wmStudy/Resource/Content");
        String szResult = "";
        if (Node != null) {
            return Node.getTextContent();
        }
        return szResult;
    }

    public ArrayList<String> getPreviewThumbnailURLs() {
        NodeList AttachmentNodes = getXMLNodes("/wmStudy/Resource/Attachments/Attachment[@thumbnail='true']");
        ArrayList<String> arrResult = new ArrayList();
        if (AttachmentNodes != null) {
            for (int i = 0; i < AttachmentNodes.getLength(); i++) {
                String szURL = ((Element) AttachmentNodes.item(i)).getAttribute("src");
                if (!szURL.isEmpty()) {
                    arrResult.add(szURL);
                }
            }
        }
        return arrResult.size() > 0 ? arrResult : null;
    }

    public String getContentMainFileURL() {
        Element Node = getXMLNode("/wmStudy/Resource/Content");
        String szResult = "";
        if (Node != null) {
            return Node.getAttribute("fileURI").toString();
        }
        return szResult;
    }

    public boolean setContentMainFileURL(String szURL) {
        Element Node = getXMLNode("/wmStudy/Resource/Content");
        if (Node == null) {
            return false;
        }
        Node.setAttribute("fileURI", szURL);
        return true;
    }

    public String getContentPreviewFileURL() {
        Element Node = getXMLNode("/wmStudy/Resource/Content");
        String szResult = "";
        if (Node != null) {
            return Node.getAttribute("previewFileURI").toString();
        }
        return szResult;
    }

    public String getXML() {
        return Utilities.XMLToString(this.mRootDocument, true);
    }
}
