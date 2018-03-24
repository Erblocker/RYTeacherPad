package com.netspace.library.parser;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.controls.CustomSelectorView;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.utilities.Utilities;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QuestionParser extends ResourceBase {
    public static final String EMPTY_QUESTION_XML = "<wmStudy><Question difficulty=\"0\" estTime=\"0\" score=\"0\" subject=\"\" questionType=\"\" teachLevel=\"\" subType=\"\" grade=\"\" parserVersion=\"2.0\" guid=\"\" date=\"\" pdfQuestion=\"false\"><KnowledgePoints/><Answers/><Content type=\"questionContent\"/><Content type=\"questionAnalysis\"/><Attachments/><Logs/></Question></wmStudy>";
    private int mRequiredHeight;
    private LinearLayout mRootLayout;
    private WebView mWebView;
    private boolean m_bIsSelectQuestion;
    private boolean m_bMultiSelect;
    private String m_szOptions;
    private boolean mbDisplayAnswers;
    private boolean mbDisplayHints;
    private boolean mbDisplayMainContent;
    private boolean mbHasData;
    private boolean mbHideQuestionContent;
    private boolean mbNoAnswerArea;

    public QuestionParser() {
        this.m_szOptions = "";
        this.m_bMultiSelect = false;
        this.m_bIsSelectQuestion = false;
        this.mbHideQuestionContent = false;
        this.mbDisplayAnswers = false;
        this.mbDisplayHints = false;
        this.mbDisplayMainContent = true;
        this.mbHasData = false;
        this.mbNoAnswerArea = false;
        this.mObjectName = "Question";
    }

    public QuestionParser(String szResourceXMLContent) {
        this.m_szOptions = "";
        this.m_bMultiSelect = false;
        this.m_bIsSelectQuestion = false;
        this.mbHideQuestionContent = false;
        this.mbDisplayAnswers = false;
        this.mbDisplayHints = false;
        this.mbDisplayMainContent = true;
        this.mbHasData = false;
        this.mbNoAnswerArea = false;
        initialize(null, szResourceXMLContent);
        this.mObjectName = "Question";
    }

    public QuestionParser(Context context, String szResourceXMLContent) {
        this.m_szOptions = "";
        this.m_bMultiSelect = false;
        this.m_bIsSelectQuestion = false;
        this.mbHideQuestionContent = false;
        this.mbDisplayAnswers = false;
        this.mbDisplayHints = false;
        this.mbDisplayMainContent = true;
        this.mbHasData = false;
        this.mbNoAnswerArea = false;
        initialize(context, szResourceXMLContent);
        this.mObjectName = "Question";
    }

    public String getOptions() {
        return this.m_szOptions;
    }

    public boolean isDisplayAnswers() {
        return this.mbDisplayAnswers;
    }

    public void setDisplayAnswers(boolean bDisplayAnswers) {
        this.mbDisplayAnswers = bDisplayAnswers;
    }

    public void setNoAnswerArea(boolean bNoAnswerArea) {
        this.mbNoAnswerArea = bNoAnswerArea;
    }

    public void setDisplayMainContent(boolean bDisplayMainContent) {
        this.mbDisplayMainContent = bDisplayMainContent;
    }

    public boolean isDisplayHints() {
        return this.mbDisplayHints;
    }

    public void setDisplayHints(boolean bDisplayHints) {
        this.mbDisplayHints = bDisplayHints;
    }

    public boolean getMultiSelect() {
        return this.m_bMultiSelect;
    }

    public void setHideQuestionContent(boolean bHide) {
        this.mbHideQuestionContent = bHide;
    }

    public boolean getHideQuestionContent() {
        return this.mbHideQuestionContent;
    }

    public boolean getIsSelectQuestion() {
        return this.m_bIsSelectQuestion;
    }

    public String getQuestionType() {
        Element Node = getXMLNode("/wmStudy/Question");
        String szResult = "";
        if (Node != null) {
            return Node.getAttribute("questionType").toString();
        }
        return szResult;
    }

    public int getSubjectID() {
        Element Node = getXMLNode("/wmStudy/Question");
        String szResult = "";
        if (Node != null) {
            szResult = Node.getAttribute("subject").toString();
        }
        return Utilities.getSubjectID(szResult);
    }

    public void setQuestionType(String szQuestionType) {
        Element Node = getXMLNode("/wmStudy/Question");
        if (Node != null) {
            Node.setAttribute("questionType", szQuestionType);
        }
    }

    public void setQuestionContent(String szContent) {
        Element ContentNode = getXMLNode("/wmStudy/Question/Content[@type='questionContent']");
        if (ContentNode != null) {
            ContentNode.setTextContent(szContent);
        }
    }

    public void setMainNodeAttribute(String szAttributeName, String szValue) {
        Element Node = getXMLNode("/wmStudy/Question");
        if (Node != null) {
            Node.setAttribute(szAttributeName, szValue);
        }
    }

    public int getOptionsCount() {
        Element Node = getXMLNode("/wmStudy/Question");
        String szResult = "";
        int nResult = 0;
        if (Node != null) {
            try {
                nResult = Integer.valueOf(Node.getAttribute("options").toString()).intValue();
            } catch (NumberFormatException e) {
            }
        }
        return nResult;
    }

    public void setQuestionsCount(int nCount) {
        Element Node = getXMLNode("/wmStudy/Question");
        if (Node != null) {
            Node.setAttribute("options", String.valueOf(nCount));
        }
    }

    public void setQuestionAnswer(String szAnswer) {
        Element AnswerNodes = getXMLNode("/wmStudy/Question/Answers");
        Node answerNode = this.mRootDocument.createElement("Answer");
        answerNode.setTextContent(szAnswer);
        if (AnswerNodes != null) {
            AnswerNodes.appendChild(answerNode);
        }
    }

    public void addBase64File(String szFileName, String szContentType, String szBase64Content) {
        Element AttachmentsNodes = getXMLNode("/wmStudy/Question/Attachments");
        Element fileNode = this.mRootDocument.createElement("File");
        fileNode.appendChild(this.mRootDocument.createCDATASection(szBase64Content));
        fileNode.setAttribute(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX, szFileName);
        fileNode.setAttribute("encoding", "base64");
        fileNode.setAttribute("contentType", szContentType);
        if (AttachmentsNodes != null) {
            AttachmentsNodes.appendChild(fileNode);
        }
    }

    public String getCorrectAnswer() {
        NodeList AnswerNodes = getXMLNodes("/wmStudy/Question/Answers/Answer");
        String szCorrectAnswers = "";
        if (0 < AnswerNodes.getLength()) {
            szCorrectAnswers = ((Element) AnswerNodes.item(0)).getTextContent();
        }
        if (!getQuestionType().equals("判断题")) {
            return szCorrectAnswers;
        }
        if (szCorrectAnswers.equalsIgnoreCase("T")) {
            szCorrectAnswers = "对";
        }
        if (szCorrectAnswers.equalsIgnoreCase("F")) {
            return "错";
        }
        return szCorrectAnswers;
    }

    public String getContentHTML(boolean bDisplayCorrectAnswer) {
        Element ContentNode = getXMLNode("/wmStudy/Question/Content[@type='questionContent']");
        String szBase64Answers = "";
        String szUserAnswer = "";
        if (ContentNode == null) {
            return null;
        }
        int i;
        String szContent = ContentNode.getTextContent();
        String szPDFContent = getQuestionContentPicture();
        if (szPDFContent != null) {
            szContent = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("" + "<img width='100%' src=\"data:image/png;base64,")).append(szPDFContent).toString())).append("\"/><br/>").toString();
            this.mbHasData = true;
        }
        if (!szContent.isEmpty()) {
            this.mbHasData = true;
        }
        String szQuestionType = getQuestionType();
        ArrayList<String> arrOptions;
        ArrayList<String> arrOptionValues;
        if (szQuestionType.equals("单选题") || szQuestionType.equals("不定项选择题") || szQuestionType.equals("多选题") || szQuestionType.equals("不定项选则题")) {
            arrOptions = new ArrayList();
            arrOptionValues = new ArrayList();
            String szMainContent = "";
            this.m_bIsSelectQuestion = true;
            if (szQuestionType.equals("单选题")) {
                this.m_bMultiSelect = false;
            } else {
                this.m_bMultiSelect = true;
            }
            if (getOptionsCount() == 0) {
                SplitChooseQuestion(szContent, arrOptions, arrOptionValues);
            } else {
                String szChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                for (i = 0; i < getOptionsCount(); i++) {
                    arrOptions.add(szChars.substring(i, i + 1));
                    arrOptionValues.add(szChars.substring(i, i + 1));
                }
            }
            for (i = 0; i < arrOptions.size(); i++) {
                this.m_szOptions += ((String) arrOptionValues.get(i)) + ";";
            }
        } else if (szQuestionType.equals("判断题")) {
            this.m_bMultiSelect = false;
            this.m_bIsSelectQuestion = true;
            arrOptions = new ArrayList();
            arrOptionValues = new ArrayList();
            arrOptions.add("对");
            arrOptions.add("错");
            arrOptionValues.add("T");
            arrOptionValues.add("F");
            for (i = 0; i < arrOptions.size(); i++) {
                this.m_szOptions += ((String) arrOptions.get(i)) + ";";
            }
        } else if (szQuestionType.equals("填空题")) {
            szContent = ReplaceUnderline(szContent, szUserAnswer, false);
        }
        if (!this.mbDisplayMainContent) {
            szContent = "";
            this.mbHasData = false;
        }
        if (bDisplayCorrectAnswer) {
            NodeList AnswerNodes = getXMLNodes("/wmStudy/Question/Answers/Answer");
            String szCorrectAnswers = "";
            Element AnswerNode = getXMLNode("/wmStudy/Question/Attachments/File[@name='AnswerContent.png']");
            for (i = 0; i < AnswerNodes.getLength(); i++) {
                szCorrectAnswers = new StringBuilder(String.valueOf(szCorrectAnswers)).append("<LI>").append(((Element) AnswerNodes.item(i)).getTextContent()).append("\n").toString();
            }
            if (!(szCorrectAnswers.isEmpty() && AnswerNode == null)) {
                szContent = new StringBuilder(String.valueOf(szContent)).append("<h3>正确答案</h3>\n").toString();
                if (AnswerNode != null) {
                    szContent = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szContent)).append("<img width='100%' src=\"data:image/png;base64,").toString())).append(AnswerNode.getTextContent()).toString())).append("\"/>").toString();
                } else {
                    szContent = new StringBuilder(String.valueOf(szContent)).append(szCorrectAnswers).toString();
                }
                this.mbHasData = true;
            }
            Element ExamTopicNode = getXMLNode("/wmStudy/Question/Content[@type='questionExamTopic']");
            if (!(ExamTopicNode == null || ExamTopicNode.getTextContent().isEmpty())) {
                szContent = new StringBuilder(String.valueOf(szContent)).append("<h3>题目专题</h3>\n").toString();
                this.mbHasData = true;
                szContent = new StringBuilder(String.valueOf(szContent)).append(ExamTopicNode.getTextContent()).toString();
            }
            Element ExamPointNode = getXMLNode("/wmStudy/Question/Content[@type='questionExamPoint']");
            if (!(ExamPointNode == null || ExamPointNode.getTextContent().isEmpty())) {
                szContent = new StringBuilder(String.valueOf(szContent)).append("<h3>题目考点</h3>\n").toString();
                this.mbHasData = true;
                szContent = new StringBuilder(String.valueOf(szContent)).append(ExamPointNode.getTextContent()).toString();
            }
            Element AnalysisNode = getXMLNode("/wmStudy/Question/Content[@type='questionAnalysis']");
            Element AnalysisImageNode = getXMLNode("/wmStudy/Question/Attachments/File[@name='AnalysisContent.png']");
            if (!((AnalysisNode == null || AnalysisNode.getTextContent().isEmpty()) && AnalysisImageNode == null)) {
                szContent = new StringBuilder(String.valueOf(szContent)).append("<h3>题目分析</h3>\n").toString();
                this.mbHasData = true;
                if (AnalysisImageNode == null) {
                    szContent = new StringBuilder(String.valueOf(szContent)).append(AnalysisNode.getTextContent()).toString();
                } else {
                    szContent = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szContent)).append("<img width='100%' src=\"data:image/png;base64,").toString())).append(AnalysisImageNode.getTextContent()).toString())).append("\"/>").toString();
                }
            }
            Element SolutionNode = getXMLNode("/wmStudy/Question/Content[@type='questionSolution']");
            if (!(SolutionNode == null || SolutionNode.getTextContent().isEmpty())) {
                szContent = new StringBuilder(String.valueOf(szContent)).append("<h3>题目解答</h3>\n").toString();
                this.mbHasData = true;
                szContent = new StringBuilder(String.valueOf(szContent)).append(SolutionNode.getTextContent()).toString();
            }
            Element CommentNode = getXMLNode("/wmStudy/Question/Content[@type='questionComment']");
            if (!(CommentNode == null || CommentNode.getTextContent().isEmpty())) {
                szContent = new StringBuilder(String.valueOf(szContent)).append("<h3>题目点评</h3>\n").toString();
                this.mbHasData = true;
                szContent = new StringBuilder(String.valueOf(szContent)).append(CommentNode.getTextContent()).toString();
            }
        }
        NodeList Attachments = getXMLNodes("/wmStudy/Question/Attachments/File");
        for (i = 0; i < Attachments.getLength(); i++) {
            Node OneAttachment = Attachments.item(i);
            String szFileName = OneAttachment.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getTextContent().toString();
            String szContentType = null;
            String szNewSRC = "";
            int nFoundIndex = szContent.indexOf(szFileName);
            if (OneAttachment.getAttributes().getNamedItem("contentType") != null) {
                szContentType = OneAttachment.getAttributes().getNamedItem("contentType").getTextContent().toString();
            }
            if (nFoundIndex != -1) {
                if (szContentType == null) {
                    szContentType = "image/jpeg";
                }
                szNewSRC = new StringBuilder(String.valueOf("data:" + szContentType + ";base64,")).append(OneAttachment.getTextContent()).toString();
                boolean bNeedQuote = true;
                char szChar = szContent.charAt(nFoundIndex - 1);
                int nSearchCount = 5;
                while (nFoundIndex > 0 && szChar != '=' && szChar != '>' && nSearchCount > 0) {
                    if (szChar == '\'' || szChar == '\"') {
                        bNeedQuote = false;
                        break;
                    }
                    nFoundIndex--;
                    nSearchCount--;
                    szChar = szContent.charAt(nFoundIndex - 1);
                }
                if (bNeedQuote) {
                    szNewSRC = "\"" + szNewSRC + "\"";
                }
                szContent = szContent.replace(szFileName, szNewSRC);
            }
        }
        return szContent;
    }

    private String IsLineBeginWithOption(String szLine) {
        boolean bInHTMLCode = false;
        for (int i = 0; i < szLine.length(); i++) {
            char OneChar = szLine.charAt(i);
            if (OneChar == '<') {
                bInHTMLCode = true;
            } else if (OneChar == '>') {
                bInHTMLCode = false;
            } else if (!(bInHTMLCode || OneChar == ' ' || OneChar == '　' || OneChar == '\t' || OneChar == '\r' || OneChar == '\n')) {
                if (OneChar < 'A' || OneChar > 'Z') {
                    return null;
                }
                return String.valueOf(OneChar);
            }
        }
        return null;
    }

    private String ReplaceUnderline(String szContent, String szUserAnswers, boolean bReadOnly) {
        int nFindPos = szContent.indexOf("__");
        int nLastFoundPos = 0;
        int nBlankIndex = 0;
        String szResult = "";
        String[] arrUserAnswers = szUserAnswers.split("!#;#!");
        String szReadOnly = "";
        if (bReadOnly) {
            szReadOnly = " readonly='true' ";
        }
        while (nFindPos != -1) {
            int nStartPos = nFindPos;
            int nEndPos = nStartPos;
            while (szContent.charAt(nEndPos) == '_') {
                nEndPos++;
            }
            int nLineSize = nEndPos - nStartPos;
            szResult = new StringBuilder(String.valueOf(szResult)).append(szContent.substring(nLastFoundPos, nFindPos)).toString();
            if (arrUserAnswers.length > nBlankIndex) {
                szResult = new StringBuilder(String.valueOf(szResult)).append("<input type='text' size='").append(String.valueOf(nLineSize)).append("' id='Blank").append(String.valueOf(nBlankIndex)).append("' value='").append(arrUserAnswers[nBlankIndex]).append("' ").append(szReadOnly).append(">").toString();
            } else {
                szResult = new StringBuilder(String.valueOf(szResult)).append("<input type='text' size='").append(String.valueOf(nLineSize)).append("' id='Blank").append(String.valueOf(nBlankIndex)).append("' ").append(szReadOnly).append(">").toString();
            }
            szResult = new StringBuilder(String.valueOf(szResult)).append("<img src=ic_camera.png id='Camera").append(String.valueOf(nBlankIndex)).append("' onclick='TakePicture(this,Blank").append(String.valueOf(nBlankIndex)).append(");' >").toString();
            nLastFoundPos = nEndPos;
            nFindPos = szContent.indexOf("__", nEndPos);
            nBlankIndex++;
        }
        return new StringBuilder(String.valueOf(szResult)).append(szContent.substring(nLastFoundPos)).toString();
    }

    private String RemoveHTMLCodes(String szContent) {
        return szContent.replaceAll("<.*?>", "");
    }

    private String SplitChooseQuestion(String szContent, ArrayList<String> arrOptions, ArrayList<String> arrOptionValues) {
        int j;
        int nEndPos = -1;
        int nContentEndPos = -1;
        String szQuestionContent = "";
        int nStartPos = szContent.indexOf("[");
        while (nStartPos != -1) {
            nEndPos = szContent.indexOf("]", nStartPos);
            if (nEndPos != -1) {
                String szInnerContent = "";
                if (nEndPos - nStartPos > 2) {
                    szInnerContent = RemoveHTMLCodes(szContent.substring(nStartPos + 1, nEndPos)).replace("_", "").replace("\r", "").replace("\n", "").replace(" ", "").replace("　", "");
                }
                if (szInnerContent.isEmpty()) {
                    nContentEndPos = nStartPos;
                    break;
                }
            }
            nStartPos = szContent.indexOf("[", nStartPos + 1);
        }
        if (nContentEndPos != -1) {
            szQuestionContent = szContent.substring(0, nContentEndPos);
            szContent = szContent.substring(nEndPos + 1);
        }
        String[] arrLines = szContent.split("\n");
        int nLastOptionStart = -1;
        String szLastOptionText = "";
        for (int i = 0; i < arrLines.length; i++) {
            String szOptionText = IsLineBeginWithOption(arrLines[i]);
            if (szOptionText != null) {
                if (nLastOptionStart != -1) {
                    String szCurrentOptionText = "";
                    for (j = nLastOptionStart; j < i; j++) {
                        szCurrentOptionText = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szCurrentOptionText)).append(arrLines[j]).toString())).append("\n").toString();
                    }
                    arrOptions.add(szCurrentOptionText.replaceAll("(?i:</?(?!sub|sup|img|u|i)[^>/]*/?>)", ""));
                    arrOptionValues.add(szLastOptionText);
                }
                nLastOptionStart = i;
                szLastOptionText = szOptionText;
            }
        }
        if (nLastOptionStart != -1) {
            szOptionText = "";
            for (j = nLastOptionStart; j < arrLines.length; j++) {
                szOptionText = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szOptionText)).append(arrLines[j]).toString())).append("\n").toString();
            }
            arrOptions.add(szOptionText.replaceAll("(?i:</?(?!sub|sup|img|u|i)[^>/]*/?>)", ""));
            arrOptionValues.add(szLastOptionText);
        }
        return szQuestionContent;
    }

    public boolean getIsPDFQuestion() {
        Element Node = getXMLNode("/wmStudy/Question");
        String szResult = "";
        if (Node != null) {
            szResult = Node.getAttribute("pdfQuestion").toString();
        }
        return szResult.equalsIgnoreCase("true");
    }

    public String getQuestionContentPicture() {
        Element ContentNode = getXMLNode("/wmStudy/Question/Attachments/File[@name='QuestionContent.png']");
        if (ContentNode == null) {
            return null;
        }
        return ContentNode.getTextContent();
    }

    public String getNextRefrenceQuestionGUID() {
        String szGUID = getGUID();
        NodeList Refrences = getXMLNodes("/wmStudy/Question/Refrences/Refrence");
        boolean bReturnGUID = false;
        for (int i = 0; i < Refrences.getLength(); i++) {
            Element OneNode = (Element) Refrences.item(i);
            if (OneNode.getAttribute("guid").equalsIgnoreCase(szGUID)) {
                bReturnGUID = true;
            } else if (bReturnGUID) {
                return OneNode.getAttribute("guid");
            }
        }
        return null;
    }

    public ArrayList<String> getRefrencesQuestionGUIDs() {
        ArrayList<String> arrGUIDs = new ArrayList();
        NodeList Refrences = getXMLNodes("/wmStudy/Question/Refrences/Refrence");
        for (int i = 0; i < Refrences.getLength(); i++) {
            arrGUIDs.add(((Element) Refrences.item(i)).getAttribute("guid"));
        }
        return arrGUIDs;
    }

    public boolean display(LinearLayout RootLayout) {
        this.mbHasData = false;
        display(RootLayout, this.mbNoAnswerArea, this.mbDisplayAnswers);
        return this.mbHasData;
    }

    @JavascriptInterface
    public void resize(final float height) {
        this.mRootLayout.post(new Runnable() {
            public void run() {
                Log.d("WebView", "content height:" + height);
                LayoutParams Params = (LayoutParams) QuestionParser.this.mWebView.getLayoutParams();
                Params.height = (int) (height * QuestionParser.this.mRootLayout.getResources().getDisplayMetrics().density);
                QuestionParser.this.mRequiredHeight = Params.height;
                QuestionParser.this.mWebView.setLayoutParams(Params);
                QuestionParser.this.listenLayoutComplete();
            }
        });
    }

    protected void listenLayoutComplete() {
        this.mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (QuestionParser.this.mWebView != null) {
                    Log.d("QuestionParser", "onGlobalLayout. mWebView measured height=" + QuestionParser.this.mWebView.getMeasuredHeight() + ", require height is " + QuestionParser.this.mRequiredHeight);
                    if (QuestionParser.this.mWebView.getMeasuredHeight() != QuestionParser.this.mRequiredHeight) {
                        return;
                    }
                }
                if (QuestionParser.this.mCallBack != null) {
                    if (!QuestionParser.this.mCallBack.onAfterLayoutComplete(QuestionParser.this)) {
                        return;
                    }
                    if (VERSION.SDK_INT > 15) {
                        QuestionParser.this.mRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        QuestionParser.this.mRootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                } else if (VERSION.SDK_INT > 15) {
                    QuestionParser.this.mRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    QuestionParser.this.mRootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    public void display(LinearLayout RootLayout, boolean bNoAnswerArea, boolean bDisplayAnswers) {
        String szGUID = getGUID();
        this.mRootLayout = RootLayout;
        if (this.mbHideQuestionContent) {
            getContentHTML(bDisplayAnswers);
        } else if (!getIsPDFQuestion() || bDisplayAnswers) {
            szHTMLContent = getContentHTML(bDisplayAnswers);
            this.mWebView = new WebView(this.mContext);
            RootLayout.addView(this.mWebView, new LayoutParams(-1, 100));
            this.mWebView.getSettings().setJavaScriptEnabled(true);
            this.mWebView.getSettings().setPluginState(PluginState.ON);
            this.mWebView.getSettings().setDefaultTextEncodingName("gb2312");
            this.mWebView.getSettings().setBuiltInZoomControls(false);
            this.mWebView.getSettings().setSaveFormData(false);
            this.mWebView.getSettings().setSavePassword(false);
            this.mWebView.getSettings().setDisplayZoomControls(false);
            this.mWebView.getSettings().setLoadWithOverviewMode(true);
            this.mWebView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            this.mWebView.setLongClickable(false);
            this.mWebView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    QuestionParser.this.mWebView.loadUrl("javascript:MyApp.resize(document.body.scrollHeight)");
                    super.onPageFinished(view, url);
                }
            });
            this.mWebView.addJavascriptInterface(this, "MyApp");
            Utilities.showStaticTextInWebView(this.mWebView, szHTMLContent);
        } else {
            szHTMLContent = getContentHTML(bDisplayAnswers);
            String szImageBase64 = getQuestionContentPicture();
            if (szImageBase64 != null) {
                ImageView ImageView = new ImageView(this.mContext);
                ImageView.setImageBitmap(Utilities.getBase64Bitmap(szImageBase64));
                ImageView.setScaleType(ScaleType.FIT_START);
                ImageView.setAdjustViewBounds(true);
                ImageView.setTag(szGUID);
                RootLayout.addView(ImageView);
                LayoutParams Params = (LayoutParams) ImageView.getLayoutParams();
                Params.width = -2;
                Params.height = -2;
                Params.bottomMargin = 5;
                ImageView.setLayoutParams(Params);
            }
        }
        if (!bNoAnswerArea && getIsSelectQuestion()) {
            CustomSelectorView SelectorView = new CustomSelectorView(this.mContext);
            String szOptions = getOptions();
            if ((szOptions == null || szOptions.isEmpty()) && getIsPDFQuestion()) {
                szOptions = "A;B;C;D";
            }
            if (!(szOptions == null || szOptions.isEmpty())) {
                String[] arrOptions = szOptions.split(";");
                for (String addOptions : arrOptions) {
                    SelectorView.addOptions(addOptions);
                }
                SelectorView.setMultiableSelect(getMultiSelect());
                SelectorView.setTag(szGUID);
                RootLayout.addView(SelectorView);
                if (this.mbDisplayAnswers) {
                    SelectorView.putCorrectValue(getCorrectAnswer());
                    SelectorView.resetChangeFlag();
                }
            }
        }
        if (this.mWebView == null) {
            listenLayoutComplete();
        }
    }

    public boolean addKnowledgePoints(String szKPPath, String szKPGUID) {
        if (szKPPath == null || szKPGUID == null) {
            return false;
        }
        Element KnowledgePointsNode = getXMLNode("/wmStudy/Question/KnowledgePoints");
        if (KnowledgePointsNode == null) {
            return false;
        }
        Element KnowledgePointNode = this.mRootDocument.createElement("KnowledgePoint");
        KnowledgePointNode.setAttribute("path", szKPPath);
        KnowledgePointNode.setAttribute("guid", szKPGUID);
        KnowledgePointsNode.appendChild(KnowledgePointNode);
        return true;
    }
}
