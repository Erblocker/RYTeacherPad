package com.netspace.library.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout.LayoutParams;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.netspace.library.activity.AnswerSheetV3OtherQuestionCorrectActivity;
import com.netspace.library.activity.AnswerSheetV3SelectQuestionCorrectActivity;
import com.netspace.library.activity.AnswerSheetV3SelectQuestionCorrectActivity.AnswerSheetCorrectAnswerChanged;
import com.netspace.library.adapter.FixHeadTableAdapter;
import com.netspace.library.database.AnswerSheetResult;
import com.netspace.library.database.AnswerSheetResultDao;
import com.netspace.library.database.AnswerSheetStudentAnswer;
import com.netspace.library.database.AnswerSheetStudentAnswerDao.Properties;
import com.netspace.library.database.DaoSession;
import com.netspace.library.interfaces.AnswerSheetDataService;
import com.netspace.library.interfaces.AnswerSheetStudentAnswerDataService;
import com.netspace.library.struct.RESTSynchronizeComplete;
import com.netspace.library.struct.RESTSynchronizeError;
import com.netspace.library.struct.RESTSynchronizeReport;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.SimpleBackgroundTask;
import com.netspace.library.utilities.SimpleBackgroundTask.BackgroundTaskExecuteCallBack;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.RESTEngine;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.helper.RESTDataEngineHelper;
import com.netspace.library.virtualnetworkobject.helper.RESTDataEngineHelper.RESTDataPutSuccess;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.http.cookie.ClientCookie;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.query.WhereCondition;
import org.greenrobot.greendao.query.WhereCondition.StringCondition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LessonPrepareCorrectAnswerSheetV3Component extends LessonPrepareCorrectAnswerSheetV2Component {
    private static final String TAG = "LessonPrepareCorrectAnswerSheetV3Component";
    private BaseActivity mActivity;
    private AnswerSheetFixTableAdapter mAdapter;
    private ArrayList<ResourceItemData> mAnswerCardQuestions = new ArrayList();
    private SimpleBackgroundTask mAutoCorrectBackgroundTask;
    private Context mContextThemeWrapper;
    private DaoSession mDaoSession;
    private Handler mHandler = new Handler();
    private HashMap<String, Integer> mMapUnsavedData = new HashMap();
    private ViewGroup mParentView;
    private ProgressBar mProgressBar;
    private JSONObject mQuestionJsonData;
    private RESTDataEngineHelper<AnswerSheetDataService, AnswerSheetResult> mRestEngine;
    private RESTDataEngineHelper<AnswerSheetStudentAnswerDataService, AnswerSheetStudentAnswer> mStudentAnswerRestEngine;
    private TableFixHeaders mTable;
    private Runnable mUpdateMenuIcon = new Runnable() {
        public void run() {
            if (LessonPrepareCorrectAnswerSheetV3Component.this.mCallBack != null) {
                LessonPrepareCorrectAnswerSheetV3Component.this.mCallBack.OnDataLoaded(null, LessonPrepareCorrectAnswerSheetV3Component.this);
                LessonPrepareCorrectAnswerSheetV3Component.this.mnUpdateCount = 0;
            }
        }
    };
    private ArrayList<LoadUserData> marrLoadUserData = new ArrayList();
    private boolean mbAnswerSheetMode;
    private int mnUpdateCount = 0;
    private String mszCurrentResourceGUID = "";
    private String mszCurrentScheduleGUID = "";

    public static class LoadUserData {
        public Date dtAnswerDate = null;
        public String szClientID = "";
        public String szRealName = "";
        public String szScheduleResultGUID = "";
        public String szUserClassGUID = "";
        public String szUserClassName = "";
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
            return LessonPrepareCorrectAnswerSheetV3Component.this.marrLoadUserData.size();
        }

        public int getColumnCount() {
            return LessonPrepareCorrectAnswerSheetV3Component.this.mAnswerCardQuestions.size();
        }

        public int getWidth(int column) {
            return this.width;
        }

        public int getHeight(int row) {
            return this.height;
        }

        public void onCellClick(int row, int column) {
            JSONObject question = LessonPrepareCorrectAnswerSheetV3Component.this.findQuestionByIndex(LessonPrepareCorrectAnswerSheetV3Component.this.mQuestionJsonData, column);
            if (question != null) {
                try {
                    Intent intent;
                    AnswerSheetV3SelectQuestionCorrectActivity.setAnswerData(LessonPrepareCorrectAnswerSheetV3Component.this.mQuestionJsonData);
                    AnswerSheetV3OtherQuestionCorrectActivity.setAnswerData(LessonPrepareCorrectAnswerSheetV3Component.this.mQuestionJsonData);
                    int nType = question.getInt("type");
                    if (nType == 0 || nType == 1 || nType == 2) {
                        intent = new Intent(getContext(), AnswerSheetV3SelectQuestionCorrectActivity.class);
                    } else {
                        intent = new Intent(getContext(), AnswerSheetV3OtherQuestionCorrectActivity.class);
                    }
                    intent.putExtra("scheduleguid", LessonPrepareCorrectAnswerSheetV3Component.this.mszCurrentScheduleGUID);
                    intent.putExtra("id", column);
                    if (row != -1) {
                        intent.putExtra("limitclientid", ((LoadUserData) LessonPrepareCorrectAnswerSheetV3Component.this.marrLoadUserData.get(row)).szClientID);
                    }
                    getContext().startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d(LessonPrepareCorrectAnswerSheetV3Component.TAG, "row=" + row + ",column=" + column);
        }

        public String getCellString(int row, int column) {
            if (row == -1) {
                if (column == -1) {
                    return "姓名";
                }
                return ((ResourceItemData) LessonPrepareCorrectAnswerSheetV3Component.this.mAnswerCardQuestions.get(column)).szTitle;
            } else if (column == -1) {
                return ((LoadUserData) LessonPrepareCorrectAnswerSheetV3Component.this.marrLoadUserData.get(row)).szRealName;
            } else {
                String szGUID = ((ResourceItemData) LessonPrepareCorrectAnswerSheetV3Component.this.mAnswerCardQuestions.get(column)).szResourceGUID;
                LoadUserData userData = (LoadUserData) LessonPrepareCorrectAnswerSheetV3Component.this.marrLoadUserData.get(row);
                AnswerSheetStudentAnswer oneAnswer = (AnswerSheetStudentAnswer) LessonPrepareCorrectAnswerSheetV3Component.this.mDaoSession.queryBuilder(AnswerSheetStudentAnswer.class).where(Properties.Scheduleguid.eq(LessonPrepareCorrectAnswerSheetV3Component.this.mScheduleGUID), Properties.Questionguid.eq(szGUID), Properties.Clientid.eq(userData.szClientID)).limit(1).unique();
                String szResult = "";
                if (oneAnswer == null) {
                    return "未作答";
                }
                AnswerSheetResult answerResult = (AnswerSheetResult) LessonPrepareCorrectAnswerSheetV3Component.this.mDaoSession.queryBuilder(AnswerSheetResult.class).where(AnswerSheetResultDao.Properties.Scheduleguid.eq(LessonPrepareCorrectAnswerSheetV3Component.this.mScheduleGUID), AnswerSheetResultDao.Properties.Questionguid.eq(szGUID), AnswerSheetResultDao.Properties.Clientid.eq(oneAnswer.getClientid())).limit(1).unique();
                szResult = oneAnswer.getAnswerchoice();
                if (!oneAnswer.getAnswertext().isEmpty()) {
                    if (!szResult.isEmpty()) {
                        szResult = new StringBuilder(String.valueOf(szResult)).append(",").toString();
                    }
                    szResult = new StringBuilder(String.valueOf(szResult)).append("文字作答").toString();
                }
                if (!oneAnswer.getAnswerhandwritedata().isEmpty()) {
                    if (!szResult.isEmpty()) {
                        szResult = new StringBuilder(String.valueOf(szResult)).append(",").toString();
                    }
                    szResult = new StringBuilder(String.valueOf(szResult)).append("绘画板作答").toString();
                }
                if (!oneAnswer.getAnswercamera().isEmpty()) {
                    if (!szResult.isEmpty()) {
                        szResult = new StringBuilder(String.valueOf(szResult)).append(",").toString();
                    }
                    szResult = new StringBuilder(String.valueOf(szResult)).append("拍照作答").toString();
                }
                if (answerResult == null || answerResult.getAnswerresult().intValue() == 0) {
                    szResult = new StringBuilder(String.valueOf(szResult)).append("(未批改)").toString();
                } else {
                    szResult = new StringBuilder(String.valueOf(szResult)).append("(").toString();
                    if (answerResult.getAnswerresult().intValue() == 2) {
                        szResult = new StringBuilder(String.valueOf(szResult)).append("正确").toString();
                    } else if (answerResult.getAnswerresult().intValue() == 1) {
                        szResult = new StringBuilder(String.valueOf(szResult)).append("半对").toString();
                    } else if (answerResult.getAnswerresult().intValue() == -1) {
                        szResult = new StringBuilder(String.valueOf(szResult)).append("错误").toString();
                    }
                    szResult = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szResult)).append("+").toString())).append(String.valueOf(answerResult.getAnswerscore())).toString())).append("分)").toString();
                }
                if (szResult.isEmpty()) {
                    return "未作答";
                }
                return szResult;
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

    public LessonPrepareCorrectAnswerSheetV3Component(Context context) {
        super(context);
        this.mContextThemeWrapper = context;
        if (context instanceof BaseActivity) {
            this.mActivity = (BaseActivity) context;
        }
    }

    public LessonPrepareCorrectAnswerSheetV3Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContextThemeWrapper = context;
        if (context instanceof BaseActivity) {
            this.mActivity = (BaseActivity) context;
        }
    }

    public LessonPrepareCorrectAnswerSheetV3Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContextThemeWrapper = context;
        if (context instanceof BaseActivity) {
            this.mActivity = (BaseActivity) context;
        }
    }

    public ArrayList<ResourceItemData> getReportResourceItemData() {
        if (this.mbAnswerSheetMode) {
            return this.mAnswerCardQuestions;
        }
        return super.getReportResourceItemData();
    }

    public String getReportTemplateName() {
        if (this.mbAnswerSheetMode) {
            return "reportTemplate3.jsp";
        }
        return super.getReportTemplateName();
    }

    public void reloadCorrectResult() {
        if (this.mbAnswerSheetMode) {
            if (this.mAutoCorrectBackgroundTask != null && this.mAutoCorrectBackgroundTask.isRunning()) {
                this.mAutoCorrectBackgroundTask.cancel(true);
            }
            this.mActivity.setBusy(true);
            this.mAnswerCardQuestions.clear();
            this.marrLoadUserData.clear();
            if (this.mAdapter != null) {
                this.mAdapter.notifyDataSetChanged();
            }
            removeAnswerSheet();
            if (!this.mStudentAnswerRestEngine.isSynchronizeComplete()) {
                this.mStudentAnswerRestEngine.cancelSynchronize();
            }
            this.mStudentAnswerRestEngine.doSychronize();
            return;
        }
        super.reloadCorrectResult();
    }

    public String getTitle() {
        if (this.mbAnswerSheetMode) {
            return super.getTitle() + "答题卡批改 - " + this.marrLoadUserData.size() + "个学生已提交";
        }
        return super.getTitle();
    }

    public String getReportTitle() {
        return super.getReportTitle();
    }

    public void initUI() {
        this.mAnswerCardQuestions.clear();
        this.marrLoadUserData.clear();
        removeAnswerSheet();
        try {
            if (this.mQuestionJsonData.getJSONArray("category").length() > 0) {
                JSONArray jsonArray = this.mQuestionJsonData.getJSONArray("category");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                    int j = 0;
                    while (j < arrQuestions.length()) {
                        JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                        String szQuestionGUID = oneQuestion.getString("guid");
                        ResourceItemData oneResource = new ResourceItemData();
                        int nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                        if (nQuestionType == 0 || nQuestionType != 1) {
                            oneResource.szResourceGUID = szQuestionGUID;
                            oneResource.szFileType = "";
                            oneResource.nType = 0;
                            oneResource.nUsageType = Utilities.toInt(oneQuestion.getString("type"));
                            oneResource.szScheduleGUID = this.mScheduleGUID;
                            oneResource.szTitle = oneQuestion.getString("index");
                            this.mAnswerCardQuestions.add(oneResource);
                            j++;
                        } else {
                            oneResource.szResourceGUID = szQuestionGUID;
                            oneResource.szFileType = "";
                            oneResource.nType = 0;
                            oneResource.nUsageType = Utilities.toInt(oneQuestion.getString("type"));
                            oneResource.szScheduleGUID = this.mScheduleGUID;
                            oneResource.szTitle = oneQuestion.getString("index");
                            this.mAnswerCardQuestions.add(oneResource);
                            j++;
                        }
                    }
                }
            }
            if (this.mAdapter == null) {
                this.mAdapter = new AnswerSheetFixTableAdapter(getContext());
                this.mTable = new TableFixHeaders(getContext());
                this.mParentView = (ViewGroup) this.mScrollView.getParent();
                LayoutParams params = (LayoutParams) ((View) this.mParentView.getParent().getParent()).getLayoutParams();
                this.mParentView.addView(this.mTable, -1, -1);
                this.mTable.setAdapter(this.mAdapter);
                ((AppBarLayout.LayoutParams) ((Toolbar) ((Activity) getContext()).findViewById(R.id.toolbar)).getLayoutParams()).setScrollFlags(20);
            } else {
                this.mAdapter.notifyDataSetChanged();
            }
            this.mScrollView.setVisibility(8);
            this.mScrollView.setScrollingEnabled(false);
            loadAnsweredStudentsList(this.mDisplayIndex, (ResourceItemData) this.marrData.get(this.mDisplayIndex));
            if (!this.mAutoCorrectBackgroundTask.isRunning()) {
                this.mAutoCorrectBackgroundTask.execute(new String[0]);
            }
            if (this.mCallBack != null) {
                this.mCallBack.OnDataLoaded(null, this);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    public static JSONObject findUserQuestionByIndexAndSetCorrectAnswer(JSONObject jsonData, int nIndex, String szCorrectAnswer) {
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
                        oneQuestion.put("correctanswer", szCorrectAnswer);
                        return oneQuestion;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void displayContent(final int i) {
        if (i < this.marrData.size()) {
            this.mbAnswerSheetMode = false;
            this.mAnswerCardQuestions.clear();
            this.marrLoadUserData.clear();
            removeAnswerSheet();
            this.mProgressBar = (ProgressBar) ((Activity) this.mContextThemeWrapper).findViewById(R.id.progress);
            if (this.mAutoCorrectBackgroundTask == null) {
                this.mAutoCorrectBackgroundTask = new SimpleBackgroundTask(getContext(), SimpleBackgroundTask.getNotifyBarDisplayer(), true, new BackgroundTaskExecuteCallBack() {
                    public void onExecute(SimpleBackgroundTask taskObject, String... params) {
                        LessonPrepareCorrectAnswerSheetV3Component.this.autoCorrectSelectQuestions(taskObject);
                    }

                    public void onNewInstance(SimpleBackgroundTask newInstance) {
                        LessonPrepareCorrectAnswerSheetV3Component.this.mAutoCorrectBackgroundTask = newInstance;
                    }

                    public void onComplete() {
                        if (LessonPrepareCorrectAnswerSheetV3Component.this.mAdapter != null) {
                            LessonPrepareCorrectAnswerSheetV3Component.this.mAdapter.notifyDataSetChanged();
                        }
                    }

                    public void onCancel() {
                    }
                });
            }
            ResourceItemData OneItem = (ResourceItemData) this.marrData.get(i);
            this.mszCurrentResourceGUID = OneItem.szResourceGUID;
            this.mszCurrentScheduleGUID = this.mScheduleGUID;
            PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject("AnswerSheet_" + OneItem.szResourceGUID, UI.getCurrentActivity(), new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szJsonData = ItemObject.readTextData();
                    if (!(szJsonData == null || szJsonData.isEmpty())) {
                        try {
                            JSONObject jsonData = new JSONObject(szJsonData);
                            String szVersion = "";
                            boolean bNewVersion = false;
                            if (jsonData.has(ClientCookie.VERSION_ATTR)) {
                                szVersion = jsonData.getString(ClientCookie.VERSION_ATTR);
                            }
                            if (szVersion.equalsIgnoreCase("3.0")) {
                                bNewVersion = true;
                            }
                            if (bNewVersion && jsonData.has("category")) {
                                LessonPrepareCorrectAnswerSheetV3Component.this.mQuestionJsonData = jsonData;
                                LessonPrepareCorrectAnswerSheetV3Component.this.mDisplayIndex = i;
                                LessonPrepareCorrectAnswerSheetV3Component.this.mbAnswerSheetMode = true;
                                LessonPrepareCorrectAnswerSheetV3Component.this.mDaoSession = RESTEngine.getDefault().getDaoSession();
                                if (!EventBus.getDefault().isRegistered(LessonPrepareCorrectAnswerSheetV3Component.this)) {
                                    EventBus.getDefault().register(LessonPrepareCorrectAnswerSheetV3Component.this);
                                }
                                LessonPrepareCorrectAnswerSheetV3Component.this.mActivity.setBusy(true);
                                LessonPrepareCorrectAnswerSheetV3Component.this.mRestEngine = RESTEngine.getDefault().getAnswerSheetHelper();
                                LessonPrepareCorrectAnswerSheetV3Component.this.mRestEngine.setOverrideData("ScheduleGUID;AnswerSheetResourceGUID", new StringBuilder(String.valueOf(LessonPrepareCorrectAnswerSheetV3Component.this.mszCurrentScheduleGUID)).append(";").append(LessonPrepareCorrectAnswerSheetV3Component.this.mszCurrentResourceGUID).toString());
                                LessonPrepareCorrectAnswerSheetV3Component.this.mRestEngine.setAllDataQuery(AnswerSheetResultDao.Properties.Scheduleguid.eq(LessonPrepareCorrectAnswerSheetV3Component.this.mScheduleGUID), AnswerSheetResultDao.Properties.Answersheetresourceguid.eq(LessonPrepareCorrectAnswerSheetV3Component.this.mszCurrentResourceGUID));
                                if (RESTEngine.getDefault().getDaoSession().queryBuilder(AnswerSheetResult.class).where(AnswerSheetResultDao.Properties.Scheduleguid.eq(LessonPrepareCorrectAnswerSheetV3Component.this.mScheduleGUID), new WhereCondition[0]).list().size() == 0) {
                                    LessonPrepareCorrectAnswerSheetV3Component.this.mRestEngine.doSychronize();
                                } else {
                                    LessonPrepareCorrectAnswerSheetV3Component.this.mRestEngine.doSychronize(true);
                                }
                                LessonPrepareCorrectAnswerSheetV3Component.this.mStudentAnswerRestEngine = RESTEngine.getDefault().getAnswerSheetStudentAnswerHelper();
                                LessonPrepareCorrectAnswerSheetV3Component.this.mStudentAnswerRestEngine.setOverrideData("ScheduleGUID;AnswerSheetResourceGUID", new StringBuilder(String.valueOf(LessonPrepareCorrectAnswerSheetV3Component.this.mszCurrentScheduleGUID)).append(";").append(LessonPrepareCorrectAnswerSheetV3Component.this.mszCurrentResourceGUID).toString());
                                LessonPrepareCorrectAnswerSheetV3Component.this.mStudentAnswerRestEngine.setAllDataQuery(Properties.Scheduleguid.eq(LessonPrepareCorrectAnswerSheetV3Component.this.mScheduleGUID), Properties.Answersheetresourceguid.eq(LessonPrepareCorrectAnswerSheetV3Component.this.mszCurrentResourceGUID));
                                LessonPrepareCorrectAnswerSheetV3Component.this.mStudentAnswerRestEngine.doSychronize();
                                LessonPrepareCorrectAnswerSheetV3Component.this.initUI();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
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
    }

    private void autoCorrectSelectQuestions(SimpleBackgroundTask taskObject) {
        try {
            int i;
            JSONArray arrQuestions;
            int j;
            int nQuestionType;
            taskObject.setProgressTitle("自动批改");
            taskObject.setProgressMessage("正在自动批改客观题，请稍候...");
            JSONArray jsonArray = this.mQuestionJsonData.getJSONArray("category");
            int nLength = jsonArray.length();
            int nTotalQuestionCount = 0;
            int nCurrentQuestionIndex = 0;
            ArrayList<LoadUserData> arrLoadUserData = (ArrayList) this.marrLoadUserData.clone();
            for (i = 0; i < nLength; i++) {
                arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (j = 0; j < arrQuestions.length(); j++) {
                    nQuestionType = Integer.valueOf(arrQuestions.getJSONObject(j).getString("type")).intValue();
                    if (nQuestionType == 0 || nQuestionType == 1 || nQuestionType == 2) {
                        nTotalQuestionCount++;
                    }
                }
            }
            taskObject.setProgressMax(nTotalQuestionCount);
            for (i = 0; i < nLength; i++) {
                arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    String szQuestionGUID = oneQuestion.getString("guid");
                    float fFullScore = 0.0f;
                    float fMissingScore = 0.0f;
                    if (oneQuestion.has("missingscore")) {
                        try {
                            fMissingScore = Float.valueOf(oneQuestion.getString("missingscore")).floatValue();
                        } catch (NumberFormatException e) {
                        }
                    }
                    try {
                        fFullScore = Float.valueOf(oneQuestion.getString("score")).floatValue();
                    } catch (NumberFormatException e2) {
                    }
                    nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                    if (nQuestionType == 0 || nQuestionType == 1 || nQuestionType == 2) {
                        String szCorrectAnswer = null;
                        if (oneQuestion.has("correctanswer")) {
                            szCorrectAnswer = oneQuestion.getString("correctanswer");
                        }
                        nCurrentQuestionIndex++;
                        taskObject.setProgress(nCurrentQuestionIndex);
                        if (!(szCorrectAnswer == null || szCorrectAnswer.isEmpty())) {
                            Iterator it = arrLoadUserData.iterator();
                            while (it.hasNext()) {
                                LoadUserData oneStudent = (LoadUserData) it.next();
                                AnswerSheetStudentAnswer oneAnswer = (AnswerSheetStudentAnswer) this.mDaoSession.queryBuilder(AnswerSheetStudentAnswer.class).where(Properties.Scheduleguid.eq(this.mScheduleGUID), Properties.Questionguid.eq(szQuestionGUID), Properties.Clientid.eq(oneStudent.szClientID)).limit(1).unique();
                                boolean bCorrect = false;
                                boolean bHalfCorrect = false;
                                boolean bChanged = false;
                                if (oneAnswer != null) {
                                    AnswerSheetResult answerResult = (AnswerSheetResult) this.mDaoSession.queryBuilder(AnswerSheetResult.class).where(AnswerSheetResultDao.Properties.Scheduleguid.eq(this.mScheduleGUID), AnswerSheetResultDao.Properties.Questionguid.eq(szQuestionGUID), AnswerSheetResultDao.Properties.Clientid.eq(oneAnswer.getClientid())).limit(1).unique();
                                    if (!oneAnswer.getAnswerchoice().isEmpty()) {
                                        if (oneAnswer.getAnswerchoice().equalsIgnoreCase(szCorrectAnswer)) {
                                            bCorrect = true;
                                        } else if (szCorrectAnswer.indexOf(oneAnswer.getAnswerchoice()) != -1 && fMissingScore > 0.0f) {
                                            bHalfCorrect = true;
                                        }
                                    }
                                    if (answerResult == null) {
                                        answerResult = new AnswerSheetResult();
                                        answerResult.setGuid(Utilities.createGUID());
                                        answerResult.setScheduleguid(this.mszCurrentScheduleGUID);
                                        answerResult.setQuestionguid(szQuestionGUID);
                                        answerResult.setClientid(oneAnswer.getClientid());
                                        answerResult.setStudentname(oneAnswer.getStudentname());
                                        answerResult.setUsername(oneAnswer.getUsername());
                                        answerResult.setAnswersheetresourceguid(this.mszCurrentResourceGUID);
                                        answerResult.setAnswerscore(Float.valueOf(0.0f));
                                        answerResult.setAnswerresult(Integer.valueOf(0));
                                        answerResult.setSyn_isdelete(Integer.valueOf(0));
                                        bChanged = true;
                                    }
                                    if (bCorrect) {
                                        if (answerResult.getAnswerresult().intValue() != 2) {
                                            bChanged = true;
                                            answerResult.setAnswerresult(Integer.valueOf(2));
                                            answerResult.setAnswerscore(Float.valueOf(fFullScore));
                                            answerResult.setSyn_timestamp(new Date());
                                        }
                                    } else if (bHalfCorrect) {
                                        if (answerResult.getAnswerresult().intValue() != 1) {
                                            bChanged = true;
                                            answerResult.setAnswerresult(Integer.valueOf(1));
                                            answerResult.setAnswerscore(Float.valueOf(fMissingScore));
                                            answerResult.setSyn_timestamp(new Date());
                                        }
                                    } else if (answerResult.getAnswerresult().intValue() != -1) {
                                        bChanged = true;
                                        answerResult.setAnswerresult(Integer.valueOf(-1));
                                        answerResult.setAnswerscore(Float.valueOf(0.0f));
                                        answerResult.setSyn_timestamp(new Date());
                                    }
                                    if (bChanged) {
                                        EventBus.getDefault().post(answerResult);
                                    }
                                    if (taskObject.isCancelled()) {
                                        return;
                                    }
                                }
                            }
                            continue;
                        }
                    } else if (nQuestionType != 3) {
                    }
                }
            }
        } catch (JSONException e3) {
        }
    }

    protected void loadAnsweredStudentsList(int i, ResourceItemData OneItem) {
        if (this.mbAnswerSheetMode) {
            List<AnswerSheetStudentAnswer> lists = this.mDaoSession.queryBuilder(AnswerSheetStudentAnswer.class).where(Properties.Scheduleguid.eq(this.mScheduleGUID), new StringCondition("1 GROUP BY UserName")).list();
            String szClientID = "";
            for (int k = 0; k < lists.size(); k++) {
                AnswerSheetStudentAnswer oneAnswer = (AnswerSheetStudentAnswer) lists.get(k);
                boolean bDisplay = true;
                szClientID = oneAnswer.getClientid();
                if ((this.mnDisplayOptions & DISPLAY_OPTIONS_FILTER) == DISPLAY_OPTIONS_FILTER) {
                    if (Utilities.isInArray(this.marrDisplayClientID, szClientID)) {
                        bDisplay = true;
                    } else {
                        bDisplay = false;
                    }
                }
                if (this.mLimitClientID != null) {
                    if (this.mLimitClientID.equalsIgnoreCase(szClientID)) {
                        bDisplay = true;
                    } else {
                        bDisplay = false;
                    }
                }
                if (bDisplay) {
                    LoadUserData oneData = new LoadUserData();
                    oneData.szClientID = oneAnswer.getClientid();
                    oneData.szRealName = oneAnswer.getStudentname();
                    oneData.szScheduleResultGUID = oneAnswer.getAnswersheetresourceguid();
                    oneData.dtAnswerDate = oneAnswer.getAnswerdate();
                    this.marrLoadUserData.add(oneData);
                }
            }
            return;
        }
        super.loadAnsweredStudentsList(i, OneItem);
    }

    public JSONObject findQuestionByIndex(JSONObject jsonData, int nIndex) {
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

    public void save() {
        if (this.mbAnswerSheetMode) {
            this.mRestEngine.doSychronize();
            Utilities.showToastMessage("程序已在后台开始数据同步操作");
            return;
        }
        super.save();
    }

    public int getUnsavedCount() {
        return this.mMapUnsavedData.size();
    }

    public boolean isAnswerSheetMode() {
        if (this.mbAnswerSheetMode) {
            return this.mbAnswerSheetMode;
        }
        return super.isAnswerSheetMode();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTSychronizeComplete(RESTSynchronizeComplete data) {
        Log.d(TAG, "onRESTSychronizeComplete");
        if (this.mStudentAnswerRestEngine.isSynchronizeComplete()) {
            this.mActivity.setBusy(true);
            initUI();
            this.mActivity.setBusy(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTSychronizeError(RESTSynchronizeError data) {
        Log.d(TAG, "onRESTSychronizeError");
        if (this.mStudentAnswerRestEngine.isSynchronizeComplete()) {
            this.mActivity.setBusy(true);
            initUI();
            this.mActivity.setBusy(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnswerSheetCorrectAnswerChanged(AnswerSheetCorrectAnswerChanged data) {
        if (!this.mAutoCorrectBackgroundTask.isRunning()) {
            this.mAutoCorrectBackgroundTask.execute(new String[0]);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnswerSheetResult(AnswerSheetResult data) {
        String szGUID = data.getGuid();
        if (!this.mMapUnsavedData.containsKey(szGUID)) {
            this.mMapUnsavedData.put(szGUID, Integer.valueOf(1));
            this.mnUpdateCount++;
            if (this.mnUpdateCount > 100) {
                this.mHandler.removeCallbacks(this.mUpdateMenuIcon);
                this.mUpdateMenuIcon.run();
            } else if (this.mnUpdateCount == 1) {
                this.mHandler.postDelayed(this.mUpdateMenuIcon, 1000);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTDataPutSuccess(RESTDataPutSuccess data) {
        if (data.szTableName.equalsIgnoreCase("answersheetresult")) {
            String szGUID = data.szGUID;
            if (this.mMapUnsavedData.containsKey(szGUID)) {
                this.mMapUnsavedData.remove(szGUID);
                if (this.mCallBack != null) {
                    this.mCallBack.OnDataLoaded(null, this);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTSynchronizeReport(RESTSynchronizeReport data) {
        if (data.szFieldName.equalsIgnoreCase("answersheetresult")) {
            for (int i = 0; i < data.arrUploadGUIDs.size(); i++) {
                this.mMapUnsavedData.put((String) data.arrUploadGUIDs.get(i), Integer.valueOf(1));
            }
            if (this.mCallBack != null) {
                this.mCallBack.OnDataLoaded(null, this);
            }
        }
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
        EventBus.getDefault().unregister(this);
        if (this.mAutoCorrectBackgroundTask != null && this.mAutoCorrectBackgroundTask.isRunning()) {
            this.mAutoCorrectBackgroundTask.cancel(true);
        }
        this.mAutoCorrectBackgroundTask = null;
        if (this.mStudentAnswerRestEngine != null && this.mStudentAnswerRestEngine.isSynchronizing()) {
            this.mStudentAnswerRestEngine.cancelSynchronize();
        }
        removeAnswerSheet();
        super.onDetachedFromWindow();
    }
}
