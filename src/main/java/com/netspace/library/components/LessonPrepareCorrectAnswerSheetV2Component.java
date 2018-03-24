package com.netspace.library.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout.LayoutParams;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.netspace.library.activity.AnswerSheetV2OtherQuestionCorrectActivity;
import com.netspace.library.activity.AnswerSheetV2SelectQuestionCorrectActivity;
import com.netspace.library.adapter.FixHeadTableAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LessonPrepareCorrectAnswerSheetV2Component extends LessonPrepareCorrectAnswerSheetComponent {
    private static final String TAG = "LessonPrepareCorrectAnswerSheetV2Component";
    private static JSONObject mAnswerSheetJsonData;
    private static int mLoadCount = 0;
    private static HashMap<String, Integer> mMapAllQuestionGUIDs = new HashMap();
    private static ArrayList<LoadUserData> marrLoadUserData = new ArrayList();
    private static int mnRefreshRunCount = 0;
    private static String mszCurrentResourceGUID = "";
    private static String mszCurrentScheduleGUID = "";
    private AnswerSheetFixTableAdapter mAdapter;
    private ArrayList<ResourceItemData> mAnswerCardQuestions = new ArrayList();
    private Context mContextThemeWrapper;
    private ViewGroup mParentView;
    private ProgressBar mProgressBar;
    private TableFixHeaders mTable;
    private boolean mbAnswerSheetMode;

    public static class LoadUserData {
        private String mCacheFileName;
        public HashMap<String, Integer> mapAnswerResult = new HashMap();
        public HashMap<String, Float> mapQuestionScore = new HashMap();
        public HashMap<String, String> mapQuestionToAnswerGUID = new HashMap();
        private boolean mbAlreadySaved = false;
        private boolean mbDataCached = false;
        private boolean mbMiniJSONReady = false;
        private boolean mbNeedCache = false;
        private JSONObject mjsonData;
        public String szClientID = "";
        public String szRealName = "";
        public String szScheduleResultGUID = "";
        public String szUserClassGUID = "";
        public String szUserClassName = "";
        public String szViewDate = "";

        public boolean initJsonData(String szJsonData) {
            try {
                this.mjsonData = new JSONObject(szJsonData);
                Log.i(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Data initialized with size of " + String.valueOf(szJsonData.length()));
                if (szJsonData.length() <= 131072) {
                    return true;
                }
                this.mbNeedCache = true;
                cacheData();
                return true;
            } catch (JSONException e) {
                Log.e(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Student answersheet json parser error.");
                e.printStackTrace();
                return false;
            }
        }

        public void cacheData() {
            if (this.mjsonData != null) {
                if (this.mCacheFileName == null) {
                    this.mCacheFileName = new StringBuilder(String.valueOf(MyiBaseApplication.getBaseAppContext().getCacheDir().getAbsolutePath())).append("/").append(this.szScheduleResultGUID).append("_").append(this.szClientID).append(".txt").toString();
                }
                if (!this.mbAlreadySaved && Utilities.writeTextToFile(this.mCacheFileName, this.mjsonData.toString())) {
                    this.mbAlreadySaved = true;
                }
                try {
                    JSONArray jsonArray = this.mjsonData.getJSONArray("category");
                    String szFinalJSON = "";
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                        for (int j = 0; j < arrQuestions.length(); j++) {
                            JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                            if (oneQuestion.has("answer1")) {
                                String szAnswer1 = oneQuestion.getString("answer1");
                                if (szAnswer1.length() > 100) {
                                    oneQuestion.put("answer1", szAnswer1.subSequence(0, 100));
                                }
                            }
                        }
                    }
                    this.mjsonData = new JSONObject(this.mjsonData.toString());
                    this.mbMiniJSONReady = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Data cached.");
                this.mbDataCached = true;
            }
        }

        public JSONObject getFullJSON() {
            JSONObject jSONObject = null;
            if (!this.mbDataCached) {
                return this.mjsonData;
            }
            try {
                this.mjsonData = new JSONObject(Utilities.readTextFile(this.mCacheFileName));
                this.mCacheFileName = null;
                this.mbDataCached = false;
                Log.i(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Data loaded.");
                return this.mjsonData;
            } catch (JSONException e) {
                Log.e(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Student answersheet json parser error.");
                e.printStackTrace();
                return jSONObject;
            }
        }

        public JSONObject getJSON() {
            return this.mjsonData;
        }

        public void releaseJSON() {
            if (this.mbNeedCache && !this.mbDataCached) {
                cacheData();
            }
        }
    }

    /* renamed from: com.netspace.library.components.LessonPrepareCorrectAnswerSheetV2Component$12 */
    class AnonymousClass12 implements OnSuccessListener {
        private final /* synthetic */ Runnable val$onFinishRunnable;
        private final /* synthetic */ LoadUserData val$oneData;
        private final /* synthetic */ ProgressBar val$progressBar;

        AnonymousClass12(LoadUserData loadUserData, ProgressBar progressBar, Runnable runnable) {
            this.val$oneData = loadUserData;
            this.val$progressBar = progressBar;
            this.val$onFinishRunnable = runnable;
        }

        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            if (ItemObject.readTextData() == null) {
                Log.e(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Student " + this.val$oneData.szClientID + " don't have answersheet data." + this.val$oneData.toString());
            } else if (this.val$oneData.initJsonData(ItemObject.readTextData())) {
                Log.e(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Student " + this.val$oneData.szClientID + " answersheet data is invalid." + this.val$oneData.toString());
            }
            LessonPrepareCorrectAnswerSheetV2Component.mnRefreshRunCount = LessonPrepareCorrectAnswerSheetV2Component.mnRefreshRunCount + 1;
            if (this.val$progressBar != null) {
                this.val$progressBar.setProgress(LessonPrepareCorrectAnswerSheetV2Component.mnRefreshRunCount);
                this.val$progressBar.setVisibility(0);
            }
            if (LessonPrepareCorrectAnswerSheetV2Component.mnRefreshRunCount >= LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.size()) {
                if (this.val$progressBar != null) {
                    this.val$progressBar.setVisibility(8);
                }
                if (this.val$onFinishRunnable != null) {
                    this.val$onFinishRunnable.run();
                    return;
                }
                return;
            }
            LessonPrepareCorrectAnswerSheetV2Component.loadRealStudentAnswer2(this.val$onFinishRunnable, this.val$progressBar);
        }
    }

    /* renamed from: com.netspace.library.components.LessonPrepareCorrectAnswerSheetV2Component$13 */
    class AnonymousClass13 implements OnFailureListener {
        private final /* synthetic */ Runnable val$onFinishRunnable;
        private final /* synthetic */ ProgressBar val$progressBar;

        AnonymousClass13(ProgressBar progressBar, Runnable runnable) {
            this.val$progressBar = progressBar;
            this.val$onFinishRunnable = runnable;
        }

        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            LessonPrepareCorrectAnswerSheetV2Component.mnRefreshRunCount = LessonPrepareCorrectAnswerSheetV2Component.mnRefreshRunCount + 1;
            if (this.val$progressBar != null) {
                this.val$progressBar.setProgress(LessonPrepareCorrectAnswerSheetV2Component.mnRefreshRunCount);
                this.val$progressBar.setVisibility(0);
            }
            if (LessonPrepareCorrectAnswerSheetV2Component.mnRefreshRunCount == LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.size()) {
                if (this.val$progressBar != null) {
                    this.val$progressBar.setVisibility(8);
                }
                if (this.val$onFinishRunnable != null) {
                    this.val$onFinishRunnable.run();
                    return;
                }
                return;
            }
            LessonPrepareCorrectAnswerSheetV2Component.loadRealStudentAnswer2(this.val$onFinishRunnable, this.val$progressBar);
        }
    }

    /* renamed from: com.netspace.library.components.LessonPrepareCorrectAnswerSheetV2Component$4 */
    class AnonymousClass4 implements OnSuccessListener {
        private final /* synthetic */ Runnable val$onFinishRunnable;
        private final /* synthetic */ ProgressBar val$progressBar;

        AnonymousClass4(Runnable runnable, ProgressBar progressBar) {
            this.val$onFinishRunnable = runnable;
            this.val$progressBar = progressBar;
        }

        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            try {
                JSONArray jArray = new JSONArray((String) ItemObject.getParam("2"));
                String szClientID = "";
                jArray.length();
                LessonPrepareCorrectAnswerSheetV2Component.mLoadCount = 0;
                for (int k = 0; k < jArray.length(); k++) {
                    JSONObject OneObj = (JSONObject) jArray.get(k);
                    szClientID = OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                    int nAnswerResult = Integer.valueOf(OneObj.getString("answerresult")).intValue();
                    if (true) {
                        boolean bFound = false;
                        LoadUserData tempUserData = new LoadUserData();
                        tempUserData.mapQuestionScore = new HashMap();
                        tempUserData.mapAnswerResult = new HashMap();
                        szClientID = OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                        for (int i = 0; i < LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.size(); i++) {
                            if (((LoadUserData) LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.get(i)).szClientID.equalsIgnoreCase(szClientID)) {
                                tempUserData = (LoadUserData) LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.get(i);
                                bFound = true;
                                break;
                            }
                        }
                        LoadUserData oneData = tempUserData;
                        oneData.szClientID = OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                        oneData.szRealName = OneObj.getString("studentname");
                        oneData.szScheduleResultGUID = OneObj.getString("guid");
                        oneData.szViewDate = OneObj.getString("viewdate");
                        if (!bFound) {
                            LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.add(oneData);
                        }
                    }
                }
                LessonPrepareCorrectAnswerSheetV2Component.loadRealStudentAnswer2(this.val$onFinishRunnable, this.val$progressBar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class AnswerSheetFixTableAdapter extends FixHeadTableAdapter {
        private final int height;
        private final int width;

        public AnswerSheetFixTableAdapter(Context context) {
            super(context);
            this.width = Utilities.dpToPixel(100, context);
            this.height = Utilities.dpToPixel(40, context);
        }

        public int getRowCount() {
            return LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.size();
        }

        public int getColumnCount() {
            return LessonPrepareCorrectAnswerSheetV2Component.this.mAnswerCardQuestions.size();
        }

        public int getWidth(int column) {
            return this.width;
        }

        public int getHeight(int row) {
            return this.height;
        }

        public void onCellClick(int row, int column) {
            JSONObject question = LessonPrepareCorrectAnswerSheetV2Component.findUserQuestionByIndex(LessonPrepareCorrectAnswerSheetV2Component.mAnswerSheetJsonData, column);
            if (question != null) {
                try {
                    Intent intent;
                    AnswerSheetV2SelectQuestionCorrectActivity.setAnswerData(LessonPrepareCorrectAnswerSheetV2Component.mAnswerSheetJsonData, LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData);
                    AnswerSheetV2OtherQuestionCorrectActivity.setAnswerData(LessonPrepareCorrectAnswerSheetV2Component.mAnswerSheetJsonData, LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData);
                    int nType = question.getInt("type");
                    if (nType == 0 || nType == 1 || nType == 2) {
                        intent = new Intent(getContext(), AnswerSheetV2SelectQuestionCorrectActivity.class);
                    } else {
                        intent = new Intent(getContext(), AnswerSheetV2OtherQuestionCorrectActivity.class);
                    }
                    intent.putExtra("id", column);
                    getContext().startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d(LessonPrepareCorrectAnswerSheetV2Component.TAG, "row=" + row + ",column=" + column);
        }

        public String getCellString(int row, int column) {
            if (row == -1) {
                if (column == -1) {
                    return "姓名";
                }
                return ((ResourceItemData) LessonPrepareCorrectAnswerSheetV2Component.this.mAnswerCardQuestions.get(column)).szTitle;
            } else if (column == -1) {
                return ((LoadUserData) LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.get(row)).szRealName;
            } else {
                JSONObject jsonData = ((LoadUserData) LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.get(row)).getJSON();
                String szGUID = ((ResourceItemData) LessonPrepareCorrectAnswerSheetV2Component.this.mAnswerCardQuestions.get(column)).szResourceGUID;
                if (jsonData != null) {
                    if (jsonData.has("category")) {
                        try {
                            if (jsonData.getJSONArray("category").length() > 0) {
                                JSONArray jsonArray = jsonData.getJSONArray("category");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                                    for (int j = 0; j < arrQuestions.length(); j++) {
                                        JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                                        String szResult = "";
                                        String szAnswer0 = "";
                                        String szAnswer1 = "";
                                        String szAnswer2 = "";
                                        if (oneQuestion.getString("guid").equalsIgnoreCase(szGUID)) {
                                            if (oneQuestion.has("answer")) {
                                                szResult = oneQuestion.getString("answer");
                                            }
                                            if (oneQuestion.has("answer0")) {
                                                szAnswer0 = oneQuestion.getString("answer0");
                                                if (!szAnswer0.isEmpty()) {
                                                    szResult = "";
                                                }
                                            }
                                            if (oneQuestion.has("answer1")) {
                                                szAnswer1 = oneQuestion.getString("answer1");
                                                if (!szAnswer1.isEmpty()) {
                                                    szResult = "";
                                                }
                                            }
                                            if (oneQuestion.has("answer2")) {
                                                szAnswer2 = oneQuestion.getString("answer2");
                                                if (!szAnswer2.isEmpty()) {
                                                    szResult = "";
                                                }
                                            }
                                            if (!szAnswer0.isEmpty()) {
                                                szResult = new StringBuilder(String.valueOf(szResult)).append("文字作答").toString();
                                            }
                                            if (!szAnswer1.isEmpty()) {
                                                szResult = new StringBuilder(String.valueOf(szResult)).append("绘画板作答").toString();
                                            }
                                            if (!szAnswer2.isEmpty()) {
                                                szResult = new StringBuilder(String.valueOf(szResult)).append("拍照作答").toString();
                                            }
                                            if (!(szAnswer0.isEmpty() && szAnswer1.isEmpty() && szAnswer2.isEmpty()) && (((LoadUserData) LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.get(row)).mapAnswerResult.get(szGUID) == null || ((Integer) ((LoadUserData) LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.get(row)).mapAnswerResult.get(szGUID)).intValue() == 0)) {
                                                szResult = new StringBuilder(String.valueOf(szResult)).append("(未批改)").toString();
                                            }
                                            if (szResult.isEmpty()) {
                                                return "未作答";
                                            }
                                            return szResult;
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ((LoadUserData) LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.get(row)).releaseJSON();
                }
                return "N/A";
            }
        }

        public int getLayoutResource(int row, int column) {
            switch (getItemViewType(row, column)) {
                case 0:
                    return R.layout.item_table1_header;
                case 1:
                    return R.layout.item_table1;
                default:
                    throw new RuntimeException("wtf?");
            }
        }

        public int getItemViewType(int row, int column) {
            if (row < 0) {
                return 0;
            }
            return 1;
        }

        public int getViewTypeCount() {
            return 2;
        }
    }

    public LessonPrepareCorrectAnswerSheetV2Component(Context context) {
        super(context);
        this.mContextThemeWrapper = context;
    }

    public LessonPrepareCorrectAnswerSheetV2Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContextThemeWrapper = context;
    }

    public LessonPrepareCorrectAnswerSheetV2Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContextThemeWrapper = context;
    }

    private boolean isGUIDinResource(String szGUID) {
        for (int i = 0; i < this.marrData.size(); i++) {
            if (((ResourceItemData) this.marrData.get(i)).szResourceGUID.equalsIgnoreCase(szGUID)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<ResourceItemData> getReportResourceItemData() {
        if (this.mbAnswerSheetMode) {
            return this.mAnswerCardQuestions;
        }
        return null;
    }

    public void reloadCorrectResult() {
        if (this.mbAnswerSheetMode) {
            refreshAnsweredStudentList(new Runnable() {
                public void run() {
                    LessonPrepareCorrectAnswerSheetV2Component.this.buildAnswerSheetUI();
                    LessonPrepareCorrectAnswerSheetV2Component.this.mAdapter.notifyDataSetChanged();
                }
            }, this.mProgressBar);
        } else {
            super.reloadCorrectResult();
        }
    }

    public void displayContent(final int i) {
        if (i < this.marrData.size()) {
            this.mbAnswerSheetMode = false;
            mLoadCount = 0;
            mAnswerSheetJsonData = null;
            this.mAnswerCardQuestions.clear();
            marrLoadUserData.clear();
            mMapAllQuestionGUIDs.clear();
            removeAnswerSheet();
            this.mProgressBar = (ProgressBar) ((Activity) this.mContextThemeWrapper).findViewById(R.id.progress);
            final ResourceItemData OneItem = (ResourceItemData) this.marrData.get(i);
            mszCurrentResourceGUID = OneItem.szResourceGUID;
            mszCurrentScheduleGUID = this.mScheduleGUID;
            PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject("AnswerSheet_" + OneItem.szResourceGUID, UI.getCurrentActivity(), new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szJsonData = ItemObject.readTextData();
                    if (!(szJsonData == null || szJsonData.isEmpty())) {
                        try {
                            JSONObject jsonData = new JSONObject(szJsonData);
                            if (jsonData.has("category") && jsonData.getJSONArray("category").length() > 0) {
                                try {
                                    JSONArray jsonArray = jsonData.getJSONArray("category");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                                        for (int j = 0; j < arrQuestions.length(); j++) {
                                            JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                                            String szQuestionGUID = oneQuestion.getString("guid");
                                            ResourceItemData oneResource = new ResourceItemData();
                                            int nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                                            if (!(nQuestionType == 0 || nQuestionType == 1 || nQuestionType == 2)) {
                                                LessonPrepareCorrectAnswerSheetV2Component.mMapAllQuestionGUIDs.put(szQuestionGUID, Integer.valueOf(1));
                                            }
                                            oneResource.szResourceGUID = szQuestionGUID;
                                            oneResource.szFileType = "";
                                            oneResource.nType = 0;
                                            oneResource.nUsageType = Utilities.toInt(oneQuestion.getString("type"));
                                            oneResource.szScheduleGUID = LessonPrepareCorrectAnswerSheetV2Component.this.mScheduleGUID;
                                            oneResource.szTitle = oneQuestion.getString("index");
                                            LessonPrepareCorrectAnswerSheetV2Component.this.mAnswerCardQuestions.add(oneResource);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                LessonPrepareCorrectAnswerSheetV2Component.this.mAdapter = new AnswerSheetFixTableAdapter(LessonPrepareCorrectAnswerSheetV2Component.this.getContext());
                                LessonPrepareCorrectAnswerSheetV2Component.this.mTable = new TableFixHeaders(LessonPrepareCorrectAnswerSheetV2Component.this.getContext());
                                LessonPrepareCorrectAnswerSheetV2Component.this.mParentView = (ViewGroup) LessonPrepareCorrectAnswerSheetV2Component.this.mScrollView.getParent();
                                LayoutParams params = (LayoutParams) ((View) LessonPrepareCorrectAnswerSheetV2Component.this.mParentView.getParent().getParent()).getLayoutParams();
                                LessonPrepareCorrectAnswerSheetV2Component.this.mParentView.addView(LessonPrepareCorrectAnswerSheetV2Component.this.mTable, -1, -1);
                                LessonPrepareCorrectAnswerSheetV2Component.this.mTable.setAdapter(LessonPrepareCorrectAnswerSheetV2Component.this.mAdapter);
                                ((AppBarLayout.LayoutParams) ((Toolbar) ((Activity) LessonPrepareCorrectAnswerSheetV2Component.this.getContext()).findViewById(R.id.toolbar)).getLayoutParams()).setScrollFlags(20);
                                LessonPrepareCorrectAnswerSheetV2Component.this.mScrollView.setVisibility(8);
                                LessonPrepareCorrectAnswerSheetV2Component.this.mScrollView.setScrollingEnabled(false);
                                LessonPrepareCorrectAnswerSheetV2Component.mAnswerSheetJsonData = jsonData;
                                LessonPrepareCorrectAnswerSheetV2Component.this.loadAnsweredStudentsList(i, OneItem);
                                LessonPrepareCorrectAnswerSheetV2Component.this.mDisplayIndex = i;
                                LessonPrepareCorrectAnswerSheetV2Component.this.mbAnswerSheetMode = true;
                                return;
                            }
                        } catch (JSONException e2) {
                            e2.printStackTrace();
                        }
                    }
                    super.displayContent(i);
                }
            });
            PrivateDataObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    super.displayContent(i);
                }
            });
            PrivateDataObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(PrivateDataObject);
        } else if (this.mCallBack != null) {
            this.mCallBack.OnDataLoaded(this.mLessonPrepareResourceGUID, this);
        }
    }

    public static void refreshAnsweredStudentList(Runnable onFinishRunnable, ProgressBar progressBar) {
        mnRefreshRunCount = 0;
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", UI.getCurrentActivity());
        ItemObject.setSuccessListener(new AnonymousClass4(onFinishRunnable, progressBar));
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        String szJsonData = "<%" + Utilities.readTextFileFromAssertPackage(MyiBaseApplication.getBaseAppContext(), "json2.js") + "%>\r\n";
        ItemObject.setParam("lpszJSFileContent", new StringBuilder(String.valueOf(szJsonData)).append(Utilities.readTextFileFromAssertPackage(MyiBaseApplication.getBaseAppContext(), "getScheduleAnswerStudentID.js")).toString());
        ArrayList<String> arrParam = new ArrayList();
        ArrayList<String> arrValue = new ArrayList();
        arrParam.add(CommentComponent.RESOURCEGUID);
        arrValue.add(mszCurrentResourceGUID);
        arrParam.add("scheduleguid");
        arrValue.add(mszCurrentScheduleGUID);
        arrParam.add("ignoreanswered");
        arrValue.add("true");
        ItemObject.setParam("arrInputParamName", arrParam);
        ItemObject.setParam("arrInputParamValue", arrValue);
        ItemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    public static void saveCorrectData() {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("LessonsScheduleSetAnswerResultJson", null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                Toast.makeText(MyiBaseApplication.getBaseAppContext(), "批改数据已成功保存", 0).show();
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Toast.makeText(MyiBaseApplication.getBaseAppContext(), "批改数据保存失败，" + ItemObject.getErrorText(), 0).show();
            }
        });
        JSONArray jsonResult = new JSONArray();
        autoCorrectSelectQuestions(jsonResult);
        for (int i = 0; i < marrLoadUserData.size(); i++) {
            LoadUserData oneData = (LoadUserData) marrLoadUserData.get(i);
            for (String szGUID : oneData.mapAnswerResult.keySet()) {
                JSONObject oneQuestion = new JSONObject();
                try {
                    oneQuestion.put("guid", oneData.mapQuestionToAnswerGUID.get(szGUID));
                    oneQuestion.put("score", oneData.mapQuestionScore.get(szGUID));
                    oneQuestion.put("result", oneData.mapAnswerResult.get(szGUID));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonResult.put(oneQuestion);
            }
        }
        CallItem.setParam("lpszJsonData", jsonResult.toString());
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    private static void autoCorrectSelectQuestions(JSONArray jsonResult) {
        try {
            JSONArray jsonArray = mAnswerSheetJsonData.getJSONArray("category");
            int nLength = jsonArray.length();
            for (int i = 0; i < nLength; i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    String szQuestionGUID = oneQuestion.getString("guid");
                    float fFullScore = 0.0f;
                    try {
                        fFullScore = Float.valueOf(oneQuestion.getString("score")).floatValue();
                    } catch (NumberFormatException e) {
                    }
                    int nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                    if (nQuestionType == 0 || nQuestionType == 1 || nQuestionType == 2) {
                        Iterator it = marrLoadUserData.iterator();
                        while (it.hasNext()) {
                            LoadUserData oneStudent = (LoadUserData) it.next();
                            if (oneStudent.getJSON() != null) {
                                JSONObject oneAnswer = findUserQuestion2(oneStudent.getJSON(), szQuestionGUID);
                                boolean bCorrectAnswer = false;
                                if (oneAnswer != null) {
                                    if (oneQuestion.has("correctanswer") && oneQuestion.getString("correctanswer").equalsIgnoreCase(oneAnswer.getString("answer"))) {
                                        bCorrectAnswer = true;
                                    }
                                    JSONObject oneQuestionResult = new JSONObject();
                                    try {
                                        oneQuestionResult.put("guid", oneStudent.mapQuestionToAnswerGUID.get(szQuestionGUID));
                                        if (bCorrectAnswer) {
                                            oneQuestionResult.put("score", (double) fFullScore);
                                            oneQuestionResult.put("result", 2);
                                        } else {
                                            oneQuestionResult.put("score", 0);
                                            oneQuestionResult.put("result", -1);
                                        }
                                    } catch (JSONException e2) {
                                        e2.printStackTrace();
                                    }
                                    jsonResult.put(oneQuestionResult);
                                } else {
                                    Log.e(TAG, "student " + oneStudent.szRealName + " did not has question " + szQuestionGUID + " answer result. JSONData: " + oneStudent.getJSON().toString());
                                }
                                oneStudent.releaseJSON();
                            }
                        }
                        continue;
                    } else if (nQuestionType != 3) {
                    }
                }
            }
        } catch (JSONException e22) {
            e22.printStackTrace();
        }
    }

    protected void loadAnsweredStudentsList(int i, ResourceItemData OneItem) {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", UI.getCurrentActivity());
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                try {
                    JSONArray jArray = new JSONArray((String) ItemObject.getParam("2"));
                    String szClientID = "";
                    jArray.length();
                    if ((LessonPrepareCorrectAnswerSheetV2Component.this.mnDisplayOptions & LessonPrepareCorrectAnswerSheetV2Component.DISPLAY_OPTIONS_LOAD_ALL) != LessonPrepareCorrectAnswerSheetV2Component.DISPLAY_OPTIONS_LOAD_ALL) {
                        LessonPrepareCorrectAnswerSheetV2Component.this.mMapAnsweredStudentsInfo.clear();
                    }
                    for (int k = 0; k < jArray.length(); k++) {
                        JSONObject OneObj = (JSONObject) jArray.get(k);
                        boolean bDisplay = true;
                        szClientID = OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                        int nAnswerResult = Integer.valueOf(OneObj.getString("answerresult")).intValue();
                        LessonPrepareCorrectAnswerSheetV2Component.this.mMapAnsweredStudentsInfo.put(szClientID, OneObj.getString("studentname"));
                        if ((LessonPrepareCorrectAnswerSheetV2Component.this.mnDisplayOptions & LessonPrepareCorrectAnswerSheetV2Component.DISPLAY_OPTIONS_FILTER) == LessonPrepareCorrectAnswerSheetV2Component.DISPLAY_OPTIONS_FILTER) {
                            if (Utilities.isInArray(LessonPrepareCorrectAnswerSheetV2Component.this.marrDisplayClientID, szClientID)) {
                                bDisplay = true;
                            } else {
                                bDisplay = false;
                            }
                        }
                        if (LessonPrepareCorrectAnswerSheetV2Component.this.mLimitClientID != null) {
                            if (LessonPrepareCorrectAnswerSheetV2Component.this.mLimitClientID.equalsIgnoreCase(OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID))) {
                                bDisplay = true;
                            } else {
                                bDisplay = false;
                            }
                        }
                        if (bDisplay) {
                            LoadUserData oneData = new LoadUserData();
                            oneData.szClientID = OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                            oneData.szRealName = OneObj.getString("studentname");
                            oneData.szScheduleResultGUID = OneObj.getString("guid");
                            oneData.szViewDate = OneObj.getString("viewdate");
                            oneData.mapQuestionScore = new HashMap();
                            oneData.mapAnswerResult = new HashMap();
                            LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.add(oneData);
                        }
                    }
                    if (LessonPrepareCorrectAnswerSheetV2Component.mLoadCount == LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.size()) {
                        LessonPrepareCorrectAnswerSheetV2Component.this.buildAnswerSheetUI();
                    }
                    LessonPrepareCorrectAnswerSheetV2Component.this.loadRealStudentAnswer();
                } catch (Exception e) {
                    e.printStackTrace();
                    LessonPrepareCorrectAnswerSheetV2Component.this.displayMessage("解析数据时出现错误，" + e.getMessage());
                }
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                LessonPrepareCorrectAnswerSheetV2Component.this.displayMessage("数据加载出现错误，错误原因：" + ItemObject.getErrorText());
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
        arrParam.add("ignoreanswered");
        arrValue.add("true");
        ItemObject.setParam("arrInputParamName", arrParam);
        ItemObject.setParam("arrInputParamValue", arrValue);
        ItemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    private void loadRealStudentAnswer() {
        this.mProgressBar.setMax(marrLoadUserData.size());
        final LoadUserData oneData = (LoadUserData) marrLoadUserData.get(mLoadCount);
        DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject("AnswerSheet_" + oneData.szScheduleResultGUID, UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                LessonPrepareCorrectAnswerSheetV2Component.mLoadCount = LessonPrepareCorrectAnswerSheetV2Component.mLoadCount + 1;
                LessonPrepareCorrectAnswerSheetV2Component.this.mProgressBar.setProgress(LessonPrepareCorrectAnswerSheetV2Component.mLoadCount);
                LessonPrepareCorrectAnswerSheetV2Component.this.mProgressBar.setVisibility(0);
                if (ItemObject.readTextData() == null) {
                    Log.e(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Student " + oneData.szClientID + " don't have answersheet data." + oneData.toString());
                } else if (!oneData.initJsonData(ItemObject.readTextData())) {
                    Log.e(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Student answersheet json parser error.");
                }
                if (LessonPrepareCorrectAnswerSheetV2Component.mLoadCount >= LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.size()) {
                    LessonPrepareCorrectAnswerSheetV2Component.this.mProgressBar.setVisibility(8);
                    LessonPrepareCorrectAnswerSheetV2Component.this.buildAnswerSheetUI();
                    LessonPrepareCorrectAnswerSheetV2Component.this.mAdapter.notifyDataSetChanged();
                    return;
                }
                LessonPrepareCorrectAnswerSheetV2Component.this.loadRealStudentAnswer();
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                LessonPrepareCorrectAnswerSheetV2Component.mLoadCount = LessonPrepareCorrectAnswerSheetV2Component.mLoadCount + 1;
                LessonPrepareCorrectAnswerSheetV2Component.this.mProgressBar.setProgress(LessonPrepareCorrectAnswerSheetV2Component.mLoadCount);
                LessonPrepareCorrectAnswerSheetV2Component.this.mProgressBar.setVisibility(0);
                if (LessonPrepareCorrectAnswerSheetV2Component.mLoadCount >= LessonPrepareCorrectAnswerSheetV2Component.marrLoadUserData.size()) {
                    LessonPrepareCorrectAnswerSheetV2Component.this.mProgressBar.setVisibility(8);
                    LessonPrepareCorrectAnswerSheetV2Component.this.buildAnswerSheetUI();
                    LessonPrepareCorrectAnswerSheetV2Component.this.mAdapter.notifyDataSetChanged();
                    return;
                }
                LessonPrepareCorrectAnswerSheetV2Component.this.loadRealStudentAnswer();
            }
        });
        ResourceObject.setNoSave(true);
        ResourceObject.setClientID(oneData.szClientID);
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    private static void loadRealStudentAnswer2(Runnable onFinishRunnable, ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.setMax(marrLoadUserData.size());
        }
        LoadUserData oneData = (LoadUserData) marrLoadUserData.get(mnRefreshRunCount);
        DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject("AnswerSheet_" + oneData.szScheduleResultGUID, UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new AnonymousClass12(oneData, progressBar, onFinishRunnable));
        ResourceObject.setFailureListener(new AnonymousClass13(progressBar, onFinishRunnable));
        ResourceObject.setNoSave(true);
        ResourceObject.setClientID(oneData.szClientID);
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    protected JSONObject findUserQuestion(JSONObject jsonData, String szQuestionGUID) {
        try {
            JSONArray jsonArray = jsonData.getJSONArray("category");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    if (oneQuestion.getString("guid").equalsIgnoreCase(szQuestionGUID)) {
                        return oneQuestion;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static JSONObject findUserQuestion2(JSONObject jsonData, String szQuestionGUID) {
        try {
            JSONArray jsonArray = jsonData.getJSONArray("category");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    if (oneQuestion.getString("guid").equalsIgnoreCase(szQuestionGUID)) {
                        return oneQuestion;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject findUserQuestionByIndex(JSONObject jsonData, int nIndex) {
        if (nIndex < 0) {
            return null;
        }
        try {
            JSONArray jsonArray = jsonData.getJSONArray("category");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    nIndex--;
                    if (nIndex < 0) {
                        return oneQuestion;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void buildAnswerSheetUI() {
        int i = 0;
        while (i < marrLoadUserData.size()) {
            LoadUserData oneStudent2;
            try {
                oneStudent2 = (LoadUserData) marrLoadUserData.get(i);
                if (oneStudent2.getJSON() == null) {
                    marrLoadUserData.remove(i);
                    i--;
                } else {
                    oneStudent2.releaseJSON();
                }
                i++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONArray jsonArray = mAnswerSheetJsonData.getJSONArray("category");
        int nLength = jsonArray.length();
        HashMap<String, Integer> mapAskedStudentIDs = new HashMap();
        for (i = 0; i < nLength; i++) {
            JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
            for (int j = 0; j < arrQuestions.length(); j++) {
                JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                String szQuestionGUID = oneQuestion.getString("guid");
                try {
                    float fFullScore = Float.valueOf(oneQuestion.getString("score")).floatValue();
                } catch (NumberFormatException e2) {
                }
                Iterator it = marrLoadUserData.iterator();
                while (it.hasNext()) {
                    JSONObject oneAnswer;
                    LoadUserData oneStudent = (LoadUserData) it.next();
                    if (oneStudent.getJSON() != null) {
                        oneAnswer = findUserQuestion(oneStudent.getJSON(), szQuestionGUID);
                        if (oneAnswer != null && oneAnswer.has("answerguid")) {
                            oneStudent.mapQuestionToAnswerGUID.put(szQuestionGUID, oneAnswer.getString("answerguid"));
                        }
                        oneStudent.releaseJSON();
                    }
                }
                int nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                if (nQuestionType == 0 || nQuestionType == 1 || nQuestionType == 2) {
                    it = marrLoadUserData.iterator();
                    while (it.hasNext()) {
                        oneStudent = (LoadUserData) it.next();
                        if (oneStudent.getJSON() != null) {
                            oneAnswer = findUserQuestion(oneStudent.getJSON(), szQuestionGUID);
                            if (oneAnswer == null) {
                                Log.e(TAG, "student " + oneStudent.szRealName + " did not has question " + szQuestionGUID + " answer result. JSONData: " + oneStudent.getJSON().toString());
                            } else if (oneAnswer.has("correctanswer") && oneAnswer.getString("correctanswer").equalsIgnoreCase(oneAnswer.getString("answer"))) {
                            }
                            oneStudent.releaseJSON();
                        }
                    }
                } else if (nQuestionType == 3 || nQuestionType == 4) {
                    it = marrLoadUserData.iterator();
                    while (it.hasNext()) {
                        oneStudent = (LoadUserData) it.next();
                        oneStudent2 = oneStudent;
                        if (!mapAskedStudentIDs.containsKey(oneStudent.szClientID) && oneStudent.mapQuestionScore.size() == 0) {
                            WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("LessonsScheduleGetQuestionAnswer2", null);
                            final LoadUserData loadUserData = oneStudent2;
                            ItemObject.setSuccessListener(new OnSuccessListener() {
                                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                    String szJsonResult = ItemObject.readTextData();
                                    if (szJsonResult != null && !szJsonResult.isEmpty()) {
                                        try {
                                            JSONArray jsonArray = new JSONArray(szJsonResult);
                                            if (loadUserData.szClientID.equalsIgnoreCase("myipad_102378")) {
                                                Log.e(LessonPrepareCorrectAnswerSheetV2Component.TAG, "Found problem one.");
                                            }
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                if (jsonObject.has("objectguid") && jsonObject.has("answerresult") && jsonObject.has("score")) {
                                                    String szItemGUID = jsonObject.getString("objectguid");
                                                    if (jsonObject.getInt("answerresult") != 0 && LessonPrepareCorrectAnswerSheetV2Component.mMapAllQuestionGUIDs.containsKey(szItemGUID)) {
                                                        loadUserData.mapAnswerResult.put(szItemGUID, Integer.valueOf(jsonObject.getInt("answerresult")));
                                                        loadUserData.mapQuestionScore.put(szItemGUID, Float.valueOf((float) jsonObject.getDouble("score")));
                                                    }
                                                }
                                            }
                                            LessonPrepareCorrectAnswerSheetV2Component.this.mAdapter.notifyDataSetChanged();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            ItemObject.setParam("lpszLessonsScheduleGUID", this.mScheduleGUID);
                            ItemObject.setParam("lpszStudentID", oneStudent.szClientID.replace("myipad_", ""));
                            ItemObject.setAlwaysActiveCallbacks(true);
                            VirtualNetworkObject.addToQueue(ItemObject);
                            mapAskedStudentIDs.put(oneStudent.szClientID, Integer.valueOf(1));
                        }
                    }
                }
            }
        }
        if (this.mCallBack != null) {
            this.mCallBack.OnDataLoaded(this.mLessonPrepareResourceGUID, this);
        }
    }

    public void save() {
        if (this.mbAnswerSheetMode) {
            saveCorrectData();
        } else {
            super.save();
        }
    }

    public boolean isAnswerSheetMode() {
        return this.mbAnswerSheetMode;
    }

    private void removeAnswerSheet() {
        if (this.mTable != null) {
            this.mParentView.removeView(this.mTable);
            ((AppBarLayout.LayoutParams) ((Toolbar) ((Activity) getContext()).findViewById(R.id.toolbar)).getLayoutParams()).setScrollFlags(5);
            this.mScrollView.setVisibility(0);
            this.mScrollView.setScrollingEnabled(true);
            this.mTable = null;
            this.mAdapter = null;
        }
    }

    protected void onDetachedFromWindow() {
        removeAnswerSheet();
        super.onDetachedFromWindow();
    }
}
