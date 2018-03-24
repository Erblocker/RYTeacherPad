package com.netspace.teacherpad.modules.paper;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomViewBase;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.controls.LockableScrollView.ScrollViewListener;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.controls.CustomQuestionBlock;
import com.netspace.teacherpad.controls.CustomQuestionBlock.onScoreChangeListener;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PaperViewActivity extends BaseActivity implements OnClickListener, DrawViewActionInterface, onScoreChangeListener {
    protected static final int CONTENT_PADDING = 200;
    private static final String TAG = "PaperViewActivity";
    private static boolean mAirplayOn = false;
    private static ArrayList<QuestionCategory> mCategoryData = new ArrayList();
    private Runnable AddDataRunnable = new Runnable() {
        public void run() {
            if (PaperViewActivity.this.mAddQuestionIndex < PaperViewActivity.this.marrData.size()) {
                QuestionData OneQuestionData = (QuestionData) PaperViewActivity.this.marrData.get(PaperViewActivity.this.mAddQuestionIndex);
                CustomQuestionBlock OneBlock = new CustomQuestionBlock(PaperViewActivity.this);
                PaperViewActivity.this.mRootLayout.addView(OneBlock);
                OneQuestionData.Object = OneBlock;
                OneBlock.setImageInfo(PaperViewActivity.this.mPaperID, OneQuestionData.szUserClassGUID, OneQuestionData.szStudentID, PaperViewActivity.this.mCurrentCategory.nPageID, PaperViewActivity.this.mCurrentCategory.nQuestionIndex);
                OneBlock.setScore((float) PaperViewActivity.this.mCurrentCategory.dFullScore, PaperViewActivity.this.mCurrentCategory.bAllowHalfScore);
                if (OneQuestionData.bSubmited) {
                    OneBlock.setMinusScore((float) OneQuestionData.dMinusScore);
                }
                OneBlock.setScoreChangeListener(PaperViewActivity.this);
                LayoutParams LayoutParam = (LayoutParams) OneBlock.getLayoutParams();
                LayoutParam.leftMargin = 20;
                LayoutParam.rightMargin = 20;
                LayoutParam.topMargin = 20;
                OneBlock.setLayoutParams(LayoutParam);
                if (PaperViewActivity.this.mbErasing) {
                    OneBlock.setEraseMode(PaperViewActivity.this.mbErasing);
                }
                if (PaperViewActivity.this.mbCorrecting) {
                    OneBlock.setBrushMode(PaperViewActivity.this.mbCorrecting);
                }
                synchronized (PaperViewActivity.this.mPaperBlocks) {
                    PaperViewActivity.this.mPaperBlocks.add(OneBlock);
                    Log.d(PaperViewActivity.TAG, "Block Count is " + PaperViewActivity.this.mPaperBlocks.size());
                }
                PaperViewActivity paperViewActivity = PaperViewActivity.this;
                paperViewActivity.mAddQuestionIndex = paperViewActivity.mAddQuestionIndex + 1;
                PaperViewActivity.this.mHandler.postDelayed(PaperViewActivity.this.AddDataRunnable, 100);
            }
        }
    };
    private Runnable AdjustContentRunnable = new Runnable() {
        public void run() {
            Log.d(PaperViewActivity.TAG, "AdjustContentRunnable");
            int nScreenHeight = Utilities.getScreenHeight(PaperViewActivity.this.mContext);
            synchronized (PaperViewActivity.this.mPaperBlocks) {
                for (int i = 0; i < PaperViewActivity.this.mPaperBlocks.size(); i++) {
                    CustomQuestionBlock OneBlock = (CustomQuestionBlock) PaperViewActivity.this.mPaperBlocks.get(i);
                    if (OneBlock.getBottom() < PaperViewActivity.this.mScrollView.getCurrentYPos() - 200) {
                        if (OneBlock.getVisibility() == 0) {
                            if (!OneBlock.getChanged()) {
                                OneBlock.cleanImage();
                            }
                            OneBlock.release();
                        }
                    } else if (OneBlock.getTop() <= (PaperViewActivity.this.mScrollView.getCurrentYPos() + nScreenHeight) + 200) {
                        OneBlock.setVisibility(0);
                        OneBlock.loadImage();
                        Log.d(PaperViewActivity.TAG, "OneBlock.loadImage();");
                    } else if (OneBlock.getVisibility() == 0) {
                        if (!OneBlock.getChanged()) {
                            OneBlock.cleanImage();
                        }
                        OneBlock.release();
                    }
                }
            }
        }
    };
    private final Runnable GetScreenCopyRunnable = new Runnable() {
        public void run() {
            long nStartTime = System.currentTimeMillis();
            if (UI.ScreenJpegServer != null && UI.ScreenJpegServer.HasClients() && UI.ScreenJpegServer.needFeedImage()) {
                PaperViewActivity.mAirplayOn = true;
                View v = PaperViewActivity.this.getWindow().getDecorView();
                boolean bLastCacheEnabled = v.isDrawingCacheEnabled();
                v.setDrawingCacheEnabled(true);
                UI.ScreenJpegServer.PostNewImageData(v.getDrawingCache());
                v.setDrawingCacheEnabled(bLastCacheEnabled);
                Log.d("ScreenCopy", "time cost " + String.valueOf(System.currentTimeMillis() - nStartTime));
            } else {
                PaperViewActivity.mAirplayOn = false;
            }
            PaperViewActivity.this.mHandler.postDelayed(PaperViewActivity.this.GetScreenCopyRunnable, 500);
        }
    };
    private final Runnable PadIPUpdateRunnable = new Runnable() {
        public void run() {
            if (TeacherPadApplication.IMThread != null) {
                TeacherPadApplication.IMThread.SendIPRequest();
            }
            PaperViewActivity.this.mHandler.postDelayed(PaperViewActivity.this.PadIPUpdateRunnable, 6000);
        }
    };
    private int mAddQuestionIndex = 0;
    private int mAnswerAreaStartID = 100000;
    private boolean mChanged;
    private String mClientID = "";
    private Context mContext;
    private ArrayList<DrawView> mCorrectingDrawViews;
    private QuestionCategory mCurrentCategory;
    private Runnable mDisplayScrollViewRunnable = new Runnable() {
        public void run() {
            if (PaperViewActivity.this.mScrollView.getVisibility() == 4) {
                PaperViewActivity.this.mScrollView.fullScroll(33);
                Utilities.fadeOutView(PaperViewActivity.this.mScrollView, 500);
                PaperViewActivity.this.checkComponentsVisible(PaperViewActivity.this.mScrollView);
            }
        }
    };
    private Handler mHandler = new Handler();
    private ImageButton mImageButtonCheckMark;
    private ImageButton mImageButtonDown;
    private ImageButton mImageButtonErase;
    private ImageButton mImageButtonNext;
    private ImageButton mImageButtonPrev;
    private ImageButton mImageButtonSave;
    private ImageButton mImageButtonUp;
    private Button mLabelButton;
    private ArrayList<CustomQuestionBlock> mPaperBlocks = new ArrayList();
    private String mPaperID;
    private String mQuestionContent;
    private String mQuestionGUID;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mRootLayout;
    private LockableScrollView mScrollView;
    private TextView mStatusTextView;
    private boolean mStudentNameUpdateExecuted = false;
    private PaperViewActivity mThis;
    private LinearLayout mToolsLayout;
    private ArrayList<QuestionData> marrData = new ArrayList();
    private boolean mbCorrectChanged = false;
    private boolean mbCorrecting = false;
    private boolean mbDisplayCorrectButton = false;
    private boolean mbErasing;
    private boolean mbPenMode = true;
    private int mnCurrentItem = 0;

    public static class QuestionCategory {
        public String QuestionDisplayOrder;
        public String QuestionGUID;
        public boolean bAllowHalfScore;
        public double dFullScore;
        public int nPageID;
        public int nQuestionIndex;
    }

    public static class QuestionData {
        public String GUID;
        public CustomQuestionBlock Object;
        public boolean bSubmited;
        public double dMinusScore;
        public String szCorrectString;
        public String szQueueTime;
        public String szStudentID;
        public String szUserClassGUID;
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(16974064);
        super.onCreate(savedInstanceState);
        this.mThis = this;
        this.mContext = this;
        this.mbDisableScreenCopyRunnable = true;
        setContentView((int) R.layout.activity_paperview);
        this.mRootLayout = (LinearLayout) findViewById(R.id.LayoutContent);
        this.mStatusTextView = (TextView) findViewById(R.id.textViewPos);
        this.mStatusTextView.setText("");
        this.mScrollView = (LockableScrollView) findViewById(R.id.scrollViewContent);
        this.mScrollView.setSpeedRate(2.5d);
        this.mScrollView.setScrollViewListener(new ScrollViewListener() {
            public void onScrollChanged(LockableScrollView scrollView, int x, int y, int oldx, int oldy) {
                PaperViewActivity.this.mHandler.removeCallbacks(PaperViewActivity.this.AdjustContentRunnable);
                PaperViewActivity.this.mHandler.postDelayed(PaperViewActivity.this.AdjustContentRunnable, 200);
            }
        });
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("paperid")) {
                this.mPaperID = getIntent().getExtras().getString("paperid");
            }
            if (getIntent().getExtras().containsKey("pos")) {
                this.mnCurrentItem = getIntent().getExtras().getInt("pos");
                if (this.mnCurrentItem < mCategoryData.size()) {
                    this.mCurrentCategory = (QuestionCategory) mCategoryData.get(this.mnCurrentItem);
                }
            }
        }
        if (mCategoryData.size() == 0) {
            getQuestionTypes();
        } else {
            getQuestions();
        }
        this.mbDisplayCorrectButton = true;
        findViewById(R.id.imageButtonList).setOnClickListener(this);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{16842919}, getResources().getDrawable(R.drawable.ic_list_light));
        states.addState(new int[]{16842908}, getResources().getDrawable(R.drawable.ic_list_light));
        states.addState(new int[0], getResources().getDrawable(R.drawable.ic_list));
        ((ImageButton) findViewById(R.id.imageButtonList)).setImageDrawable(states);
        states = new StateListDrawable();
        states.addState(new int[]{16842919}, getResources().getDrawable(R.drawable.ic_correcting_light));
        states.addState(new int[]{16842908}, getResources().getDrawable(R.drawable.ic_correcting_light));
        states.addState(new int[0], getResources().getDrawable(R.drawable.ic_correcting));
        this.mImageButtonCheckMark = (ImageButton) findViewById(R.id.ImageButtonCheckMark);
        this.mImageButtonCheckMark.setImageDrawable(states);
        this.mImageButtonCheckMark.setOnClickListener(this);
        states = new StateListDrawable();
        states.addState(new int[]{16842919}, getResources().getDrawable(R.drawable.ic_erase_light));
        states.addState(new int[]{16842908}, getResources().getDrawable(R.drawable.ic_erase_light));
        states.addState(new int[0], getResources().getDrawable(R.drawable.ic_erase));
        this.mImageButtonErase = (ImageButton) findViewById(R.id.ImageButtonErase);
        this.mImageButtonErase.setImageDrawable(states);
        this.mImageButtonErase.setOnClickListener(this);
        states = new StateListDrawable();
        states.addState(new int[]{16842919}, getResources().getDrawable(R.drawable.ic_left_light));
        states.addState(new int[]{16842908}, getResources().getDrawable(R.drawable.ic_left_light));
        states.addState(new int[0], getResources().getDrawable(R.drawable.ic_left));
        this.mImageButtonPrev = (ImageButton) findViewById(R.id.ImageButtonPrev);
        this.mImageButtonPrev.setImageDrawable(states);
        this.mImageButtonPrev.setOnClickListener(this);
        states = new StateListDrawable();
        states.addState(new int[]{16842919}, getResources().getDrawable(R.drawable.ic_right_light));
        states.addState(new int[]{16842908}, getResources().getDrawable(R.drawable.ic_right_light));
        states.addState(new int[0], getResources().getDrawable(R.drawable.ic_right));
        this.mImageButtonNext = (ImageButton) findViewById(R.id.ImageButtonNext);
        this.mImageButtonNext.setImageDrawable(states);
        this.mImageButtonNext.setOnClickListener(this);
        states = new StateListDrawable();
        states.addState(new int[]{16842919}, getResources().getDrawable(R.drawable.ic_moveup_light));
        states.addState(new int[]{16842908}, getResources().getDrawable(R.drawable.ic_moveup_light));
        states.addState(new int[0], getResources().getDrawable(R.drawable.ic_moveup));
        this.mImageButtonUp = (ImageButton) findViewById(R.id.ImageButtonUp);
        this.mImageButtonUp.setImageDrawable(states);
        this.mImageButtonUp.setOnClickListener(this);
        states = new StateListDrawable();
        states.addState(new int[]{16842919}, getResources().getDrawable(R.drawable.ic_movedown_light));
        states.addState(new int[]{16842908}, getResources().getDrawable(R.drawable.ic_movedown_light));
        states.addState(new int[0], getResources().getDrawable(R.drawable.ic_movedown));
        this.mImageButtonDown = (ImageButton) findViewById(R.id.ImageButtonDown);
        this.mImageButtonDown.setImageDrawable(states);
        this.mImageButtonDown.setOnClickListener(this);
        this.mImageButtonSave = (ImageButton) findViewById(R.id.ImageButtonSave);
        this.mImageButtonSave.setOnClickListener(this);
        setChanged(false);
    }

    public static void clearData() {
        mCategoryData.clear();
    }

    private boolean isHasUnsubmitItems() {
        synchronized (this.mPaperBlocks) {
            int i = 0;
            while (i < this.mPaperBlocks.size()) {
                if (((CustomQuestionBlock) this.mPaperBlocks.get(i)).getScoreSet()) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    private void scrollUp() {
        synchronized (this.mPaperBlocks) {
            CustomQuestionBlock LastBlock = null;
            int nScreenHeight = Utilities.getScreenHeight(this.mContext);
            for (int i = 0; i < this.mPaperBlocks.size(); i++) {
                CustomQuestionBlock OneBlock = (CustomQuestionBlock) this.mPaperBlocks.get(i);
                if (OneBlock.getBottom() < this.mScrollView.getCurrentYPos()) {
                    LastBlock = OneBlock;
                } else if (OneBlock.getTop() > this.mScrollView.getCurrentYPos() + nScreenHeight) {
                    break;
                }
            }
            if (LastBlock != null) {
                this.mScrollView.scrollTo(0, LastBlock.getTop());
            }
        }
    }

    private void scrollDown() {
        synchronized (this.mPaperBlocks) {
            CustomQuestionBlock LastBlock = null;
            int nScreenHeight = Utilities.getScreenHeight(this.mContext);
            for (int i = 0; i < this.mPaperBlocks.size(); i++) {
                CustomQuestionBlock OneBlock = (CustomQuestionBlock) this.mPaperBlocks.get(i);
                if (OneBlock.getTop() > this.mScrollView.getCurrentYPos() + nScreenHeight) {
                    LastBlock = OneBlock;
                    break;
                }
            }
            if (LastBlock != null) {
                this.mScrollView.scrollTo(0, LastBlock.getBottom() - nScreenHeight);
            }
        }
    }

    private void checkComponentsVisible(ViewGroup ViewGroup) {
        Log.d(TAG, "checkComponentsVisible");
        for (int i = 0; i < ViewGroup.getChildCount(); i++) {
            View OneView = ViewGroup.getChildAt(i);
            if (OneView instanceof DrawView) {
                DrawView TempView = (DrawView) OneView;
                Rect scrollBounds = new Rect();
                TempView.getHitRect(scrollBounds);
                if (TempView.getLocalVisibleRect(scrollBounds)) {
                    TempView.setPausePaint(false);
                    TempView.invalidate();
                } else {
                    TempView.setPausePaint(true);
                    if (TempView.getEnableCache()) {
                        TempView.cleanCache();
                    }
                }
            } else if (OneView instanceof CustomViewBase) {
                ((CustomViewBase) OneView).startVisibleCheck();
            } else if (OneView instanceof ViewGroup) {
                checkComponentsVisible((ViewGroup) OneView);
            }
        }
    }

    private void getQuestions() {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", this);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                try {
                    JSONArray jArray = new JSONArray((String) ItemObject.getParam("2"));
                    for (int i = 0; i < jArray.length(); i++) {
                        QuestionData OneData = new QuestionData();
                        JSONObject OneObj = (JSONObject) jArray.get(i);
                        OneData.bSubmited = OneObj.getBoolean("submited");
                        OneData.dMinusScore = (double) Integer.valueOf(OneObj.getString("minusscore")).intValue();
                        OneData.GUID = OneObj.getString("guid");
                        OneData.szStudentID = OneObj.getString("studentid");
                        OneData.szUserClassGUID = OneObj.getString(UserHonourFragment.USERCLASSGUID);
                        PaperViewActivity.this.marrData.add(OneData);
                    }
                    PaperViewActivity.this.updateInfo();
                    PaperViewActivity.this.mHandler.postDelayed(PaperViewActivity.this.AddDataRunnable, 100);
                    PaperViewActivity.this.mHandler.postDelayed(PaperViewActivity.this.AdjustContentRunnable, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    PaperViewActivity.this.reportError("数据解析错误", "解析数据时出现错误，" + e.getMessage());
                }
            }
        });
        String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage(this, "json2.js") + "%>\r\n";
        ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage(this, "getQuestionData.js")).toString());
        ArrayList<String> arrParam = new ArrayList();
        ArrayList<String> arrValue = new ArrayList();
        arrParam.add("userguid");
        arrValue.add(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        arrParam.add(UserHonourFragment.USERNAME);
        arrValue.add(MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        arrParam.add("paperid");
        arrValue.add(this.mPaperID);
        arrParam.add("questionguid");
        arrValue.add(this.mCurrentCategory.QuestionGUID);
        ItemObject.setParam("arrInputParamName", arrParam);
        ItemObject.setParam("arrInputParamValue", arrValue);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    private void getQuestionTypes() {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", this);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                try {
                    JSONArray jArray = new JSONArray((String) ItemObject.getParam("2"));
                    for (int i = 0; i < jArray.length(); i++) {
                        QuestionCategory OneData = new QuestionCategory();
                        JSONObject OneObj = (JSONObject) jArray.get(i);
                        OneData.QuestionGUID = OneObj.getString("questionguid");
                        OneData.nPageID = Integer.valueOf(OneObj.getString("pageid")).intValue();
                        OneData.QuestionDisplayOrder = OneObj.getString("paperdisplayorder");
                        PaperViewActivity.mCategoryData.add(OneData);
                    }
                    PaperViewActivity.this.finishQuestionType();
                } catch (Exception e) {
                    e.printStackTrace();
                    PaperViewActivity.this.reportError("数据解析错误", "解析数据时出现错误，" + e.getMessage());
                }
            }
        });
        String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage(this, "json2.js") + "%>\r\n";
        ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage(this, "getQuestionType.js")).toString());
        ArrayList<String> arrParam = new ArrayList();
        ArrayList<String> arrValue = new ArrayList();
        arrParam.add("userguid");
        arrValue.add(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        arrParam.add(UserHonourFragment.USERNAME);
        arrValue.add(MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        arrParam.add("paperid");
        arrValue.add(this.mPaperID);
        ItemObject.setParam("arrInputParamName", arrParam);
        ItemObject.setParam("arrInputParamValue", arrValue);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    private void finishQuestionType() {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("GetPaperLayoutXML", this);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                try {
                    Element RootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(ItemObject.readTextData().getBytes(HTTP.UTF_8))).getDocumentElement();
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    Iterator it = PaperViewActivity.mCategoryData.iterator();
                    while (it.hasNext()) {
                        QuestionCategory OneData = (QuestionCategory) it.next();
                        NodeList nodes = (NodeList) xPath.evaluate("/wmStudy/ExamPaper/Paper/Question[@guid='" + OneData.QuestionGUID + "']", RootElement, XPathConstants.NODESET);
                        if (nodes.getLength() >= 1) {
                            Element e = (Element) nodes.item(0);
                            OneData.dFullScore = Double.valueOf(e.getAttribute("score")).doubleValue();
                            OneData.nQuestionIndex = Integer.valueOf(e.getAttribute("index")).intValue();
                            if (e.getAttribute("halfScore").equalsIgnoreCase("true")) {
                                OneData.bAllowHalfScore = true;
                            }
                        }
                    }
                    PaperViewActivity.this.mnCurrentItem = 0;
                    PaperViewActivity.this.mCurrentCategory = (QuestionCategory) PaperViewActivity.mCategoryData.get(0);
                    PaperViewActivity.this.getQuestions();
                } catch (Exception e2) {
                    e2.printStackTrace();
                    PaperViewActivity.this.reportError("数据解析错误", "解析数据时出现错误，" + e2.getMessage());
                }
            }
        });
        ItemObject.setParam("lpszPaperID", this.mPaperID);
        ItemObject.setParam("lpszIP", "");
        ItemObject.setUseSuperAdmin(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    public void enterCorrectMode(boolean bEnter) {
        synchronized (this.mPaperBlocks) {
            for (int i = 0; i < this.mPaperBlocks.size(); i++) {
                ((CustomQuestionBlock) this.mPaperBlocks.get(i)).setBrushMode(bEnter);
            }
        }
    }

    public void enterEraseMode(boolean bEnter) {
        synchronized (this.mPaperBlocks) {
            for (int i = 0; i < this.mPaperBlocks.size(); i++) {
                ((CustomQuestionBlock) this.mPaperBlocks.get(i)).setEraseMode(bEnter);
            }
        }
    }

    private void jumpToQuestionType(int which) {
        Intent newIntent = new Intent(this, PaperViewActivity.class);
        newIntent.putExtra("pos", which);
        newIntent.putExtra("paperid", this.mPaperID);
        newIntent.setFlags(67108864);
        startActivity(newIntent);
        finish();
    }

    public void updateInfo() {
        String szPosInfo = String.format("%d/%d", new Object[]{Integer.valueOf(this.mnCurrentItem + 1), Integer.valueOf(mCategoryData.size())});
        int nUnsubmitCount = 0;
        for (int i = 0; i < this.marrData.size(); i++) {
            if (((QuestionData) this.marrData.get(i)).Object != null) {
                if (!((QuestionData) this.marrData.get(i)).Object.getScoreSet()) {
                    nUnsubmitCount++;
                }
            } else if (!((QuestionData) this.marrData.get(i)).bSubmited) {
                nUnsubmitCount++;
            }
        }
        if (this.mChanged) {
            szPosInfo = new StringBuilder(String.valueOf(szPosInfo)).append("，有改动尚未保存").toString();
        }
        if (nUnsubmitCount > 0) {
            szPosInfo = new StringBuilder(String.valueOf(szPosInfo)).append("，当前还有").append(String.valueOf(nUnsubmitCount)).append("个尚未批改。").toString();
        }
        this.mStatusTextView.setText(szPosInfo);
        if (this.mnCurrentItem == 0) {
            this.mImageButtonPrev.setEnabled(false);
            this.mImageButtonPrev.setAlpha(76);
        }
        if (this.mnCurrentItem == mCategoryData.size() - 1) {
            this.mImageButtonNext.setEnabled(false);
            this.mImageButtonNext.setAlpha(76);
        }
    }

    public void onClick(View v) {
        ImageButton ImageButton = (ImageButton) v;
        int i;
        if (v.getId() == R.id.imageButtonList) {
            ArrayList<String> arrCategorys = new ArrayList();
            for (i = 0; i < mCategoryData.size(); i++) {
                arrCategorys.add(((QuestionCategory) mCategoryData.get(i)).QuestionDisplayOrder);
            }
            new Builder(new ContextThemeWrapper(this, 16974130)).setItems((String[]) arrCategorys.toArray(new String[arrCategorys.size()]), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    final int nQuestionType = which;
                    if (PaperViewActivity.this.mChanged) {
                        new Builder(PaperViewActivity.this).setTitle("数据保存").setIcon(17301543).setMessage("当前有尚未保存的数据，如果现在离开则这些数据将会丢失，是否继续？").setPositiveButton("是，放弃未保存的数据", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                PaperViewActivity.this.jumpToQuestionType(nQuestionType);
                            }
                        }).setNegativeButton("否，我要手动保存数据", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                    } else {
                        PaperViewActivity.this.jumpToQuestionType(nQuestionType);
                    }
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                }
            }).setTitle("选择题目").create().show();
        } else if (v.getId() == R.id.ImageButtonCheckMark) {
            if (this.mbCorrecting) {
                this.mImageButtonCheckMark.setImageResource(R.drawable.ic_correcting);
                this.mbCorrecting = false;
                this.mScrollView.setScrollingEnabled(true);
            } else {
                this.mbCorrecting = true;
                this.mImageButtonCheckMark.setImageResource(R.drawable.ic_correcting_light);
                this.mbCorrectChanged = true;
                if (!DrawView.getIsSPenSupported()) {
                    this.mScrollView.setScrollingEnabled(false);
                }
            }
            this.mImageButtonErase.setImageResource(R.drawable.ic_erase);
            this.mbErasing = false;
            enterCorrectMode(this.mbCorrecting);
        } else if (v.getId() == R.id.ImageButtonErase) {
            if (this.mbErasing) {
                this.mImageButtonErase.setImageResource(R.drawable.ic_erase);
                this.mbErasing = false;
                this.mScrollView.setScrollingEnabled(true);
            } else {
                this.mbErasing = true;
                this.mImageButtonErase.setImageResource(R.drawable.ic_erase_light);
                this.mbCorrectChanged = true;
                this.mScrollView.setScrollingEnabled(false);
            }
            this.mImageButtonCheckMark.setImageResource(R.drawable.ic_correcting);
            this.mbCorrecting = false;
            enterEraseMode(this.mbErasing);
        } else if (v.getId() == R.id.ImageButtonNext) {
            if (this.mChanged) {
                new Builder(this).setTitle("数据保存").setIcon(17301543).setMessage("当前有尚未保存的数据，如果现在离开则这些数据将会丢失，是否继续？").setPositiveButton("是，放弃未保存的数据", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PaperViewActivity.this.goNext();
                    }
                }).setNegativeButton("否，我要手动保存数据", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            } else {
                goNext();
            }
        } else if (v.getId() == R.id.ImageButtonPrev) {
            if (this.mChanged) {
                new Builder(this).setTitle("数据保存").setIcon(17301543).setMessage("当前有尚未保存的数据，如果现在离开则这些数据将会丢失，是否继续？").setPositiveButton("是，放弃未保存的数据", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PaperViewActivity.this.goPrev();
                    }
                }).setNegativeButton("否，我要手动保存数据", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            } else {
                goPrev();
            }
        } else if (v.getId() == R.id.ImageButtonSave) {
            save();
        } else if (v.getId() == R.id.ImageButtonUp) {
            if (isHasUnsubmitItems()) {
                synchronized (this.mPaperBlocks) {
                    LastBlock = null;
                    nScreenHeight = Utilities.getScreenHeight(this.mContext);
                    for (i = 0; i < this.mPaperBlocks.size(); i++) {
                        OneBlock = (CustomQuestionBlock) this.mPaperBlocks.get(i);
                        if (OneBlock.getBottom() < this.mScrollView.getCurrentYPos()) {
                            if (!OneBlock.getScoreSet()) {
                                LastBlock = OneBlock;
                            }
                        } else if (OneBlock.getTop() > this.mScrollView.getCurrentYPos() + nScreenHeight) {
                            break;
                        }
                    }
                    if (LastBlock != null) {
                        this.mScrollView.scrollTo(0, LastBlock.getTop());
                    }
                }
                return;
            }
            scrollUp();
        } else if (v.getId() != R.id.ImageButtonDown) {
        } else {
            if (isHasUnsubmitItems()) {
                synchronized (this.mPaperBlocks) {
                    LastBlock = null;
                    nScreenHeight = Utilities.getScreenHeight(this.mContext);
                    for (i = 0; i < this.mPaperBlocks.size(); i++) {
                        OneBlock = (CustomQuestionBlock) this.mPaperBlocks.get(i);
                        if (OneBlock.getTop() > this.mScrollView.getCurrentYPos() + nScreenHeight && !OneBlock.getScoreSet()) {
                            LastBlock = OneBlock;
                            break;
                        }
                    }
                    if (LastBlock != null) {
                        this.mScrollView.scrollTo(0, LastBlock.getBottom() - nScreenHeight);
                    }
                }
                return;
            }
            scrollDown();
        }
    }

    private void save() {
        Iterator it = this.marrData.iterator();
        while (it.hasNext()) {
            QuestionData QuestionData = (QuestionData) it.next();
            if (QuestionData.Object != null) {
                if (QuestionData.Object.getChanged()) {
                    submitResult(QuestionData);
                }
                if (QuestionData.Object.getImageChanged()) {
                    QuestionData.Object.uploadImage();
                }
            }
        }
        setChanged(false);
    }

    private void goPrev() {
        Intent newIntent = new Intent(this, PaperViewActivity.class);
        this.mnCurrentItem--;
        newIntent.putExtra("pos", this.mnCurrentItem);
        newIntent.putExtra("paperid", this.mPaperID);
        newIntent.setFlags(67108864);
        startActivity(newIntent);
        overridePendingTransition(R.anim.anim_enter_left, R.anim.anim_leave_right);
        finish();
    }

    private void goNext() {
        Intent newIntent = new Intent(this, PaperViewActivity.class);
        this.mnCurrentItem++;
        newIntent.putExtra("pos", this.mnCurrentItem);
        newIntent.putExtra("paperid", this.mPaperID);
        newIntent.setFlags(67108864);
        startActivity(newIntent);
        overridePendingTransition(R.anim.anim_enter_right, R.anim.anim_leave_left);
        finish();
    }

    public void submitResult(final QuestionData QuestionData) {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("DPCAssignResult", this);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                QuestionData.bSubmited = true;
                QuestionData.Object.setChanged(false);
                PaperViewActivity.this.updateInfo();
            }
        });
        ItemObject.setParam("lpszGUID", QuestionData.GUID);
        ItemObject.setParam("dMinusScore", Double.valueOf(QuestionData.dMinusScore));
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    protected void onResume() {
        this.mHandler.postDelayed(this.PadIPUpdateRunnable, 1000);
        this.mHandler.postDelayed(this.GetScreenCopyRunnable, 500);
        super.onResume();
    }

    protected void onPause() {
        this.mHandler.removeCallbacks(this.PadIPUpdateRunnable);
        this.mHandler.removeCallbacks(this.GetScreenCopyRunnable);
        super.onPause();
    }

    public void onBackPressed() {
        if (this.mChanged) {
            new Builder(this).setTitle("数据保存").setIcon(17301543).setMessage("当前有尚未保存的数据，如果现在离开则这些数据将会丢失，是否继续？").setPositiveButton("是，放弃未保存的数据", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PaperViewActivity.this.marrData.clear();
                    PaperViewActivity.this.finish();
                    PaperViewActivity.this.onBackPressed();
                }
            }).setNegativeButton("否，我要手动保存数据", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
            return;
        }
        this.marrData.clear();
        finish();
        super.onBackPressed();
    }

    public void OnTouchDown() {
        if ((this.mbCorrecting || this.mbErasing) && DrawView.getIsSPenSupported()) {
            this.mScrollView.setScrollingEnabled(false);
        }
        this.mbCorrectChanged = true;
        setChanged(true);
    }

    public void OnTouchUp() {
        if ((this.mbCorrecting || this.mbErasing) && DrawView.getIsSPenSupported()) {
            this.mScrollView.setScrollingEnabled(true);
        }
    }

    public void OnPenButtonDown() {
    }

    public void OnPenButtonUp() {
    }

    public void setChanged(boolean bChanged) {
        this.mChanged = bChanged;
        int nLightImageID = getResources().getIdentifier("ic_save_light", "drawable", getPackageName());
        int nNormalImageID = getResources().getIdentifier("ic_save", "drawable", getPackageName());
        ImageButton ImageButton = (ImageButton) findViewById(R.id.ImageButtonSave);
        if (ImageButton != null) {
            if (this.mChanged) {
                ImageButton.setImageResource(nLightImageID);
                ImageButton.setAlpha(255);
                ImageButton.setEnabled(true);
            } else {
                ImageButton.setImageResource(nNormalImageID);
                ImageButton.setAlpha(76);
                ImageButton.setEnabled(false);
            }
        }
        updateInfo();
    }

    public void onScoreChanged(CustomQuestionBlock Block, float fNewMinusScore) {
        for (int i = 0; i < this.marrData.size(); i++) {
            if (((QuestionData) this.marrData.get(i)).Object.equals(Block)) {
                ((QuestionData) this.marrData.get(i)).dMinusScore = (double) fNewMinusScore;
                setChanged(true);
                return;
            }
        }
    }

    public void onDrawViewChanged(CustomQuestionBlock Block) {
        setChanged(true);
    }

    public void onImageLoaded(CustomQuestionBlock Block) {
        this.mHandler.removeCallbacks(this.AdjustContentRunnable);
        this.mHandler.postDelayed(this.AdjustContentRunnable, 200);
    }

    public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
    }

    public void OnTouchPen() {
    }

    public void OnTouchFinger() {
    }

    public void OnTouchMove() {
    }
}
