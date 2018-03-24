package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.controls.StickyScrollView;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.dialog.UserInfoDialog;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.parser.LessonsPrepareResourceParser;
import com.netspace.library.parser.LessonsPrepareResourceParser.LessonPrepareResourceItem;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.HashMap;
import me.dm7.barcodescanner.zbar.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LessonPrepareCorrectComponent extends FrameLayout implements IComponents {
    public static int DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY = 8;
    public static int DISPLAY_OPTIONS_DISPLAY_NO_CORRECT = 2;
    public static int DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY = 16;
    public static int DISPLAY_OPTIONS_FILTER = 1;
    public static int DISPLAY_OPTIONS_HIDE_STUDENTTITLE = 4;
    public static int DISPLAY_OPTIONS_LOAD_ALL = 128;
    private static String mTitle;
    protected ComponentCallBack mCallBack;
    protected LinearLayout mContentLayout;
    private IWmExamDBOpenHelper mDataBase;
    private DataChangeCallBack mDataChangeCallBack;
    protected int mDisplayIndex = 0;
    protected String mLessonPrepareResourceGUID;
    protected String mLimitClientID;
    private LoadStudentAllAnswerCallBack mLoadStudentAllAnswerCallBack;
    private LinearLayout mLoadingLayout;
    protected HashMap<String, String> mMapAnsweredStudentsInfo = new HashMap();
    private View mRootView;
    protected String mScheduleGUID;
    protected StickyScrollView mScrollView;
    private Object mSubjectID;
    private TextView mTextViewMessage;
    private boolean mUsePagerContainer = false;
    private OnClickListener mUserOnClickListener;
    protected VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    protected ArrayList<ResourceItemData> marrData = new ArrayList();
    protected ArrayList<String> marrDisplayClientID = new ArrayList();
    private boolean mbReloadScoreMode = false;
    int mnDisplayOptions = 0;

    public interface DataChangeCallBack {
        void OnDataChanged();
    }

    public interface LoadStudentAllAnswerCallBack {
        void OnLoadStudentAllAnswer(String str, String str2);
    }

    public LessonPrepareCorrectComponent(Context context) {
        super(context);
        initView();
    }

    public LessonPrepareCorrectComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LessonPrepareCorrectComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        this.mRootView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.component_lessonpreparecorrect, this, true);
        this.mContentLayout = (LinearLayout) findViewById(R.id.layoutContent);
        this.mLoadingLayout = (LinearLayout) findViewById(R.id.loadingLayout);
        this.mTextViewMessage = (TextView) findViewById(R.id.textViewMessage);
        this.mScrollView = (StickyScrollView) findViewById(R.id.scrollView1);
        this.mScrollView.setDescendantFocusability(131072);
        this.mScrollView.setFocusable(true);
        this.mScrollView.setFocusableInTouchMode(true);
        this.mScrollView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });
        QuestionWidgetsUtilities.setScrollView(this.mScrollView);
    }

    public void setDataBase(IWmExamDBOpenHelper dataBase) {
        if (dataBase == null) {
            throw new IllegalArgumentException("dataBase can not be null.");
        }
        this.mDataBase = dataBase;
    }

    public void setDataChangeCallBack(DataChangeCallBack CallBack) {
        this.mDataChangeCallBack = CallBack;
    }

    public void setLoadStudentAllAnswerCallBack(LoadStudentAllAnswerCallBack CallBack) {
        this.mLoadStudentAllAnswerCallBack = CallBack;
    }

    protected void loadLessonPrepareData(String szLessonPrepareResourceGUID) {
        this.mLessonPrepareResourceGUID = szLessonPrepareResourceGUID;
        if (this.marrData.size() == 0) {
            displayLoading();
            ResourceItemObject ResourceObject = new ResourceItemObject(this.mLessonPrepareResourceGUID, null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    LessonPrepareCorrectComponent.this.hideLoading();
                    LessonPrepareCorrectComponent.this.parserResourceList(ItemObject.readTextData());
                    LessonPrepareCorrectComponent.this.loadUncorrectCount();
                    LessonPrepareCorrectComponent.this.displayContent(0);
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    LessonPrepareCorrectComponent.this.displayMessage("数据加载错误，错误原因：" + ItemObject.getErrorText());
                }
            });
            ResourceObject.setAllowCache(false);
            ResourceObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
            return;
        }
        hideLoading();
        setTitle(mTitle);
        displayContent(0);
    }

    public boolean checkUnCorrectAnswer(boolean bJumpTo) {
        RelativeLayout LastTextViewLayout = null;
        for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
            View OneView = this.mContentLayout.getChildAt(i);
            if (OneView instanceof CorrectFootComponent) {
                if (((CorrectFootComponent) OneView).getAnswerResult() == 0) {
                    if (bJumpTo && LastTextViewLayout != null) {
                        OneView = LastTextViewLayout;
                        int nTop = OneView.getTop();
                        View ParentView = (View) OneView.getParent();
                        while (ParentView != null && !ParentView.equals(this.mScrollView)) {
                            nTop += ParentView.getTop();
                            ParentView = (View) ParentView.getParent();
                        }
                        this.mScrollView.scrollTo(0, nTop);
                    }
                    return true;
                }
            } else if ((OneView instanceof RelativeLayout) && OneView.getId() == R.id.LayoutTitle) {
                LastTextViewLayout = (RelativeLayout) OneView;
            }
        }
        return false;
    }

    public void reloadCorrectResult() {
        this.mbReloadScoreMode = true;
        displayContent(this.mDisplayIndex);
    }

    public void setDisplayOptions(int nDisplayOptions) {
        this.mnDisplayOptions = nDisplayOptions;
    }

    public int getDisplayOptions() {
        return this.mnDisplayOptions;
    }

    protected void addOneResourceTitle(ResourceItemData OneItem) {
        LayoutInflater LayoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        JSONObject JSON = new JSONObject();
        try {
            JSON.put(CommentComponent.RESOURCEGUID, OneItem.szResourceGUID);
            JSON.put("isquestion", OneItem.nType == 0);
            JSON.put("guid", "");
        } catch (JSONException e) {
        }
        ContentDisplayComponent TitleDisplayer = new ContentDisplayComponent(getContext());
        this.mContentLayout.addView(TitleDisplayer, new LayoutParams(-1, -2));
        TitleDisplayer.setDisplayOptions(BuildConfig.VERSION_CODE);
        TitleDisplayer.setScheduleArrangeGUID(this.mScheduleGUID);
        TitleDisplayer.setResourceItem(OneItem);
        TitleDisplayer.setData(JSON.toString());
        setContentBlockMargin(TitleDisplayer);
    }

    protected void addOneResourceOneStudentAnswer(ResourceItemData OneItem, String szStudentName, String szClientID, String szGUID, int nAnswerResult, int nAnswerScore, String szReadTime, String szAnswerTime) {
        CorrectFootComponent FootComponent;
        if (this.mbReloadScoreMode) {
            for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
                View OneView = this.mContentLayout.getChildAt(i);
                if (OneView instanceof CorrectFootComponent) {
                    FootComponent = (CorrectFootComponent) OneView;
                    if (FootComponent.getGUID().equalsIgnoreCase(szGUID)) {
                        FootComponent.setAnswerResult(nAnswerResult);
                        FootComponent.setAnswerScore(nAnswerScore);
                        return;
                    }
                }
            }
            return;
        }
        LayoutInflater LayoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        JSONObject JSON = new JSONObject();
        ContentDisplayComponent DisplayComponent = null;
        try {
            JSON.put(CommentComponent.RESOURCEGUID, OneItem.szResourceGUID);
            JSON.put("isquestion", OneItem.nType == 0);
            JSON.put(DeviceOperationRESTServiceProvider.CLIENTID, szClientID);
            JSON.put("guid", szGUID);
        } catch (JSONException e) {
        }
        View v = LayoutInflater.inflate(R.layout.layout_studenttitle, null);
        TextView textTitle = (TextView) v.findViewById(R.id.TextViewTitle);
        TextView textAnswerTime = (TextView) v.findViewById(R.id.textViewAnswerTime);
        TextView textAllAnswers = (TextView) v.findViewById(R.id.textViewAll);
        final String str = szClientID;
        textTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String szUserName = str.replace("myipad_", "").replace("_teacherpad", "");
                if (UI.getCurrentActivity() != null && (UI.getCurrentActivity() instanceof AppCompatActivity)) {
                    UserInfoDialog.showDialog((AppCompatActivity) UI.getCurrentActivity(), szUserName);
                }
            }
        });
        this.mContentLayout.addView(v);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.topMargin = Utilities.dpToPixel(10, getContext());
        params.bottomMargin = Utilities.dpToPixel(10, getContext());
        if ((this.mnDisplayOptions & DISPLAY_OPTIONS_HIDE_STUDENTTITLE) == DISPLAY_OPTIONS_HIDE_STUDENTTITLE) {
            textTitle.setVisibility(8);
            textAllAnswers.setVisibility(8);
            ((RelativeLayout.LayoutParams) textAnswerTime.getLayoutParams()).topMargin = Utilities.dpToPixel(10, getContext());
        }
        textTitle.setText(Html.fromHtml("<u><font color=#0000ee>" + szStudentName + "</font></u>的作答"));
        textAnswerTime.setText("阅读时间：" + szReadTime + "，作答时间：" + szAnswerTime);
        str = szClientID;
        final String str2 = szStudentName;
        textAllAnswers.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (LessonPrepareCorrectComponent.this.mLoadStudentAllAnswerCallBack != null) {
                    LessonPrepareCorrectComponent.this.mLoadStudentAllAnswerCallBack.OnLoadStudentAllAnswer(str, str2);
                }
            }
        });
        if (!this.mUsePagerContainer) {
            ContentDisplayComponent Pager = new ContentDisplayComponent(getContext());
            this.mContentLayout.addView(Pager, new LayoutParams(-1, -2));
            Pager.setDisplayOptions(29);
            Pager.setScheduleArrangeGUID(this.mScheduleGUID);
            Pager.setClientID(szClientID);
            Pager.setResourceItem(OneItem);
            if (this.mDataBase != null) {
                Pager.setDataBase(this.mDataBase);
            }
            Pager.setData(JSON.toString());
            Pager.getDrawView().setBrushMode(true);
            Pager.getDrawView().changeWidth(Utilities.getIntSettings("PenWidth", 6));
            DisplayComponent = Pager;
            setContentBlockMargin(Pager);
        }
        FootComponent = new CorrectFootComponent(getContext());
        this.mContentLayout.addView(FootComponent, new LayoutParams(-1, -2));
        params = (LinearLayout.LayoutParams) FootComponent.getLayoutParams();
        params.topMargin = Utilities.dpToPixel(10, getContext());
        params.bottomMargin = Utilities.dpToPixel(10, getContext());
        FootComponent.setAnswerResult(nAnswerResult);
        FootComponent.setAnswerScore(nAnswerScore);
        FootComponent.setDisplayComponent(DisplayComponent);
        FootComponent.setData(JSON.toString());
        FootComponent.setChanged(false);
    }

    public int getDisplayIndex() {
        return this.mDisplayIndex;
    }

    public void clear() {
        this.mbReloadScoreMode = false;
        this.mContentLayout.removeAllViews();
    }

    public HashMap<String, String> getAnsweredStudentsInfo() {
        return this.mMapAnsweredStudentsInfo;
    }

    public ArrayList<String> getFilterStudentsClientID() {
        return this.marrDisplayClientID;
    }

    public void setLimitClientID(String szClientID) {
        this.mLimitClientID = szClientID;
    }

    protected void loadUncorrectCount() {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", null);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                try {
                    String szData = (String) ItemObject.getParam("2");
                    if (szData != null && !szData.isEmpty()) {
                        int i;
                        JSONArray jArray = new JSONArray(szData);
                        String szClientID = "";
                        for (i = 0; i < LessonPrepareCorrectComponent.this.marrData.size(); i++) {
                            ((ResourceItemData) LessonPrepareCorrectComponent.this.marrData.get(i)).nTipNumber = 0;
                        }
                        for (int k = 0; k < jArray.length(); k++) {
                            JSONObject OneObj = (JSONObject) jArray.get(k);
                            String szObjectGUID = OneObj.getString("objectguid");
                            int nCount = OneObj.getInt("count");
                            for (i = 0; i < LessonPrepareCorrectComponent.this.marrData.size(); i++) {
                                if (((ResourceItemData) LessonPrepareCorrectComponent.this.marrData.get(i)).szResourceGUID.equalsIgnoreCase(szObjectGUID)) {
                                    ((ResourceItemData) LessonPrepareCorrectComponent.this.marrData.get(i)).nTipNumber = nCount;
                                    break;
                                }
                            }
                        }
                        if (LessonPrepareCorrectComponent.this.mDataChangeCallBack != null) {
                            LessonPrepareCorrectComponent.this.mDataChangeCallBack.OnDataChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LessonPrepareCorrectComponent.this.displayMessage("解析数据时出现错误，" + e.getMessage());
                }
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage(getContext(), "json2.js") + "%>\r\n";
        ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage(getContext(), "getScheduleUnCorrectCount.js")).toString());
        ArrayList<String> arrParam = new ArrayList();
        ArrayList<String> arrValue = new ArrayList();
        arrParam.add("scheduleguid");
        arrValue.add(this.mScheduleGUID);
        if (!(this.mLimitClientID == null || this.mLimitClientID.isEmpty())) {
            arrParam.add(DeviceOperationRESTServiceProvider.CLIENTID);
            arrValue.add(this.mLimitClientID);
        }
        ItemObject.setParam("arrInputParamName", arrParam);
        ItemObject.setParam("arrInputParamValue", arrValue);
        ItemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    public void displayContent(final int i) {
        if (i < this.marrData.size()) {
            this.mDisplayIndex = i;
            final ResourceItemData OneItem = (ResourceItemData) this.marrData.get(i);
            if (!this.mbReloadScoreMode) {
                addOneResourceTitle(OneItem);
            }
            WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", null);
            ItemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    try {
                        JSONArray jArray = new JSONArray((String) ItemObject.getParam("2"));
                        String szClientID = "";
                        jArray.length();
                        if ((LessonPrepareCorrectComponent.this.mnDisplayOptions & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_LOAD_ALL) != LessonPrepareCorrectComponent.DISPLAY_OPTIONS_LOAD_ALL) {
                            LessonPrepareCorrectComponent.this.mMapAnsweredStudentsInfo.clear();
                        }
                        for (int k = 0; k < jArray.length(); k++) {
                            JSONObject OneObj = (JSONObject) jArray.get(k);
                            boolean bDisplay = true;
                            szClientID = OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                            int nAnswerResult = Integer.valueOf(OneObj.getString("answerresult")).intValue();
                            if (!OneObj.getString("answerdate").isEmpty()) {
                                LessonPrepareCorrectComponent.this.mMapAnsweredStudentsInfo.put(szClientID, OneObj.getString("studentname"));
                                if ((LessonPrepareCorrectComponent.this.mnDisplayOptions & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_FILTER) {
                                    if (Utilities.isInArray(LessonPrepareCorrectComponent.this.marrDisplayClientID, szClientID)) {
                                        bDisplay = true;
                                    } else {
                                        bDisplay = false;
                                    }
                                }
                                if ((LessonPrepareCorrectComponent.this.mnDisplayOptions & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_NO_CORRECT) {
                                    if (nAnswerResult == 0) {
                                        bDisplay = true;
                                    } else {
                                        bDisplay = false;
                                    }
                                }
                                if (LessonPrepareCorrectComponent.this.mLimitClientID != null) {
                                    if (LessonPrepareCorrectComponent.this.mLimitClientID.equalsIgnoreCase(OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID))) {
                                        bDisplay = true;
                                    } else {
                                        bDisplay = false;
                                    }
                                }
                                if (bDisplay) {
                                    if ((LessonPrepareCorrectComponent.this.mnDisplayOptions & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_CORRECT_ONLY) {
                                        if (nAnswerResult == 2) {
                                            bDisplay = true;
                                        } else {
                                            bDisplay = false;
                                        }
                                    } else if ((LessonPrepareCorrectComponent.this.mnDisplayOptions & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_DISPLAY_WRONG_ONLY) {
                                        if (nAnswerResult == 1 || nAnswerResult == -1) {
                                            bDisplay = true;
                                        } else {
                                            bDisplay = false;
                                        }
                                    }
                                }
                                if (bDisplay) {
                                    LessonPrepareCorrectComponent.this.addOneResourceOneStudentAnswer(OneItem, OneObj.getString("studentname"), OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID), OneObj.getString("guid"), Integer.valueOf(OneObj.getString("answerresult")).intValue(), Integer.valueOf(OneObj.getString("answerscore")).intValue(), OneObj.getString("viewdate"), OneObj.getString("answerdate"));
                                }
                            }
                        }
                        if ((LessonPrepareCorrectComponent.this.mnDisplayOptions & LessonPrepareCorrectComponent.DISPLAY_OPTIONS_LOAD_ALL) == LessonPrepareCorrectComponent.DISPLAY_OPTIONS_LOAD_ALL) {
                            LessonPrepareCorrectComponent.this.displayContent(i + 1);
                        } else if (LessonPrepareCorrectComponent.this.mCallBack != null) {
                            LessonPrepareCorrectComponent.this.mCallBack.OnDataLoaded(LessonPrepareCorrectComponent.this.mLessonPrepareResourceGUID, LessonPrepareCorrectComponent.this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LessonPrepareCorrectComponent.this.displayMessage("解析数据时出现错误，" + e.getMessage());
                    }
                }
            });
            ItemObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    LessonPrepareCorrectComponent.this.displayMessage("数据加载出现错误，错误原因：" + ItemObject.getErrorText());
                }
            });
            String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage(getContext(), "json2.js") + "%>\r\n";
            ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage(getContext(), "getScheduleAnswerStudentID.js")).toString());
            ArrayList<String> arrParam = new ArrayList();
            ArrayList<String> arrValue = new ArrayList();
            arrParam.add(CommentComponent.RESOURCEGUID);
            arrValue.add(OneItem.szResourceGUID);
            arrParam.add("scheduleguid");
            arrValue.add(this.mScheduleGUID);
            ItemObject.setParam("arrInputParamName", arrParam);
            ItemObject.setParam("arrInputParamValue", arrValue);
            ItemObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ItemObject);
            if ((this.mnDisplayOptions & DISPLAY_OPTIONS_LOAD_ALL) == DISPLAY_OPTIONS_LOAD_ALL && !this.mbReloadScoreMode && i != this.marrData.size() - 1) {
                insertLine();
            }
        } else if (this.mCallBack != null) {
            this.mCallBack.OnDataLoaded(this.mLessonPrepareResourceGUID, this);
        }
    }

    public void save() {
        for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
            View OneView = this.mContentLayout.getChildAt(i);
            if (OneView instanceof ContentDisplayComponent) {
                ContentDisplayComponent ContentDisplayComponent = (ContentDisplayComponent) OneView;
                if (ContentDisplayComponent.isChanged()) {
                    ContentDisplayComponent.save();
                }
            } else if (OneView instanceof CorrectFootComponent) {
                CorrectFootComponent CorrectFootComponent = (CorrectFootComponent) OneView;
                if (CorrectFootComponent.isChanged()) {
                    CorrectFootComponent.save();
                }
            }
        }
        loadUncorrectCount();
    }

    protected void setContentBlockMargin(View Components) {
        LinearLayout.LayoutParams Params = (LinearLayout.LayoutParams) Components.getLayoutParams();
        int dpToPixel = Utilities.dpToPixel(16, getContext());
        Params.leftMargin = dpToPixel;
        Params.rightMargin = dpToPixel;
        Params.topMargin = dpToPixel;
        Components.setLayoutParams(Params);
    }

    protected void insertLine() {
        View sepline = new View(getContext());
        this.mContentLayout.addView(sepline, new LayoutParams(-1, Utilities.dpToPixel(1, getContext())));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) sepline.getLayoutParams();
        params.topMargin = Utilities.dpToPixel(10, getContext());
        int dpToPixel = Utilities.dpToPixel(16, getContext());
        params.rightMargin = dpToPixel;
        params.leftMargin = dpToPixel;
        params.bottomMargin = Utilities.dpToPixel(10, getContext());
        sepline.setBackgroundColor(Utilities.getThemeCustomColor(R.attr.content_display_seperatelinecolor));
        sepline.setVisibility(0);
    }

    protected void setTitle(String szTitle) {
        mTitle = szTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getReportTitle() {
        return mTitle;
    }

    public ArrayList<ResourceItemData> getResourceItemData() {
        return this.marrData;
    }

    public LockableScrollView getScrollView() {
        return this.mScrollView;
    }

    public LinearLayout getContentLayout() {
        return this.mContentLayout;
    }

    public boolean jumpToResource(String szResourceGUID) {
        for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
            if (this.mContentLayout.getChildAt(i) instanceof ContentDisplayComponent) {
                ContentDisplayComponent OneView = (ContentDisplayComponent) this.mContentLayout.getChildAt(i);
                if (OneView.getResourceGUID() != null && OneView.getResourceGUID().equalsIgnoreCase(szResourceGUID)) {
                    this.mScrollView.scrollTo(0, OneView.getTop());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean jumpToClientID(String szClientID) {
        if (this.mLimitClientID == null || this.mLimitClientID.isEmpty()) {
            for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
                if (this.mContentLayout.getChildAt(i) instanceof ContentDisplayComponent) {
                    ContentDisplayComponent OneView = (ContentDisplayComponent) this.mContentLayout.getChildAt(i);
                    if (OneView.getClientID() != null && OneView.getClientID().equalsIgnoreCase(szClientID)) {
                        this.mScrollView.scrollTo(0, OneView.getTop() - Utilities.dpToPixel(100, getContext()));
                        return true;
                    }
                }
            }
            return false;
        } else if (this.mLoadStudentAllAnswerCallBack == null) {
            return true;
        } else {
            this.mLoadStudentAllAnswerCallBack.OnLoadStudentAllAnswer(szClientID, (String) this.mMapAnsweredStudentsInfo.get(szClientID));
            return true;
        }
    }

    protected void displayMessage(String szMessage) {
        this.mLoadingLayout.setVisibility(4);
        this.mContentLayout.setVisibility(4);
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
    }

    protected void displayLoading() {
        this.mLoadingLayout.setVisibility(0);
        this.mContentLayout.setVisibility(4);
        this.mTextViewMessage.setVisibility(4);
    }

    protected void hideLoading() {
        this.mLoadingLayout.setVisibility(4);
        this.mContentLayout.setVisibility(0);
        this.mTextViewMessage.setVisibility(4);
    }

    protected boolean parserResourceList(String szData) {
        LessonsPrepareResourceParser ResourceParser = new LessonsPrepareResourceParser();
        if (ResourceParser.initialize(getContext(), szData)) {
            this.mSubjectID = Integer.valueOf(ResourceParser.getSubjectID());
            setTitle(ResourceParser.getTitle());
            for (int i = 0; i < ResourceParser.getCount(); i++) {
                LessonPrepareResourceItem OneItem = ResourceParser.getItem(i);
                ResourceItemData NewItem = new ResourceItemData();
                NewItem.szTitle = OneItem.szTitle;
                NewItem.szResourceGUID = OneItem.szGUID;
                NewItem.szFileType = OneItem.szResourceType;
                NewItem.nType = OneItem.nType;
                NewItem.nUsageType = OneItem.nUsageType;
                NewItem.szScheduleGUID = this.mScheduleGUID;
                this.marrData.add(NewItem);
            }
            if (this.marrData.size() != 0) {
                return true;
            }
            displayMessage("当前备课资源文件当中没有包含任何内容。");
            return true;
        }
        displayMessage("解析备课资源文件时出现错误。");
        return false;
    }

    public void setOnClickListener(OnClickListener l) {
        this.mUserOnClickListener = l;
    }

    protected void onDetachedFromWindow() {
        this.mVirtualNetworkObjectManager.cancelAll();
        Utilities.unbindDrawables(this.mContentLayout);
        this.mContentLayout.removeAllViews();
        QuestionWidgetsUtilities.setScrollView(null);
        super.onDetachedFromWindow();
    }

    public void setData(String szData) {
        throw new IllegalArgumentException("Please use setData(String szLessonPrepareResourceGUID,String szLessonScheduleGUID) instead of setData(szData");
    }

    public void setData(String szLessonPrepareResourceGUID, String szLessonScheduleGUID) {
        this.mScheduleGUID = szLessonScheduleGUID;
        loadLessonPrepareData(szLessonPrepareResourceGUID);
    }

    public String getData() {
        return null;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }
}
