package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.fragment.RESTLibraryFragment;
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
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LessonPrepareCorrectAnswerSheetComponent extends LessonPrepareCorrectComponent {
    private final String TAG = "LessonPrepareCorrectAnswerSheetComponent";
    private ArrayList<ResourceItemData> mAnswerCardQuestions = new ArrayList();
    private JSONObject mAnswerSheetJsonData;
    private Context mContextThemeWrapper;
    private int mLoadCount = 0;
    private ArrayList<LoadUserData> marrLoadUserData = new ArrayList();
    private ArrayList<UserAnswersComponent> marrUserAnswersComponent = new ArrayList();
    private boolean mbAnswerSheetMode;

    private class LoadUserData {
        JSONObject jsonData;
        String szClientID;
        String szRealName;
        String szScheduleResultGUID;
        String szUserClassGUID;
        String szUserClassName;
        String szViewDate;

        private LoadUserData() {
            this.szScheduleResultGUID = "";
            this.szClientID = "";
            this.szRealName = "";
            this.szUserClassGUID = "";
            this.szUserClassName = "";
            this.szViewDate = "";
        }
    }

    public LessonPrepareCorrectAnswerSheetComponent(Context context) {
        super(context);
        this.mContextThemeWrapper = context;
    }

    public LessonPrepareCorrectAnswerSheetComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContextThemeWrapper = context;
    }

    public LessonPrepareCorrectAnswerSheetComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContextThemeWrapper = context;
    }

    public String getReportTemplateName() {
        return "reportTemplate.jsp";
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
        if (!this.mbAnswerSheetMode) {
            return null;
        }
        Utilities.showToastMessage("如果当前批改记录没有保存，报表中的结果会不准确。");
        return this.mAnswerCardQuestions;
    }

    public void displayContent(final int i) {
        if (i < this.marrData.size()) {
            this.mbAnswerSheetMode = false;
            this.mLoadCount = 0;
            this.marrLoadUserData.clear();
            this.marrUserAnswersComponent.clear();
            this.mAnswerSheetJsonData = null;
            this.mAnswerCardQuestions.clear();
            final ResourceItemData OneItem = (ResourceItemData) this.marrData.get(i);
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
                                            oneResource.szResourceGUID = szQuestionGUID;
                                            oneResource.szFileType = "";
                                            oneResource.nType = 0;
                                            oneResource.nUsageType = Utilities.toInt(oneQuestion.getString("type"));
                                            oneResource.szScheduleGUID = LessonPrepareCorrectAnswerSheetComponent.this.mScheduleGUID;
                                            oneResource.szTitle = oneQuestion.getString("index");
                                            LessonPrepareCorrectAnswerSheetComponent.this.mAnswerCardQuestions.add(oneResource);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                LessonPrepareCorrectAnswerSheetComponent.this.mAnswerSheetJsonData = jsonData;
                                LessonPrepareCorrectAnswerSheetComponent.this.loadAnsweredStudentsList(i, OneItem);
                                LessonPrepareCorrectAnswerSheetComponent.this.mDisplayIndex = i;
                                LessonPrepareCorrectAnswerSheetComponent.this.mbAnswerSheetMode = true;
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

    protected void loadAnsweredStudentsList(int i, ResourceItemData OneItem) {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", UI.getCurrentActivity());
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                try {
                    JSONArray jArray = new JSONArray((String) ItemObject.getParam("2"));
                    String szClientID = "";
                    jArray.length();
                    if ((LessonPrepareCorrectAnswerSheetComponent.this.mnDisplayOptions & LessonPrepareCorrectAnswerSheetComponent.DISPLAY_OPTIONS_LOAD_ALL) != LessonPrepareCorrectAnswerSheetComponent.DISPLAY_OPTIONS_LOAD_ALL) {
                        LessonPrepareCorrectAnswerSheetComponent.this.mMapAnsweredStudentsInfo.clear();
                    }
                    for (int k = 0; k < jArray.length(); k++) {
                        JSONObject OneObj = (JSONObject) jArray.get(k);
                        boolean bDisplay = true;
                        szClientID = OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                        int nAnswerResult = Integer.valueOf(OneObj.getString("answerresult")).intValue();
                        LessonPrepareCorrectAnswerSheetComponent.this.mMapAnsweredStudentsInfo.put(szClientID, OneObj.getString("studentname"));
                        if ((LessonPrepareCorrectAnswerSheetComponent.this.mnDisplayOptions & LessonPrepareCorrectAnswerSheetComponent.DISPLAY_OPTIONS_FILTER) == LessonPrepareCorrectAnswerSheetComponent.DISPLAY_OPTIONS_FILTER) {
                            if (Utilities.isInArray(LessonPrepareCorrectAnswerSheetComponent.this.marrDisplayClientID, szClientID)) {
                                bDisplay = true;
                            } else {
                                bDisplay = false;
                            }
                        }
                        if (LessonPrepareCorrectAnswerSheetComponent.this.mLimitClientID != null) {
                            if (LessonPrepareCorrectAnswerSheetComponent.this.mLimitClientID.equalsIgnoreCase(OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID))) {
                                bDisplay = true;
                            } else {
                                bDisplay = false;
                            }
                        }
                        if (bDisplay) {
                            final LoadUserData oneData = new LoadUserData();
                            oneData.szClientID = OneObj.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                            oneData.szRealName = OneObj.getString("studentname");
                            oneData.szScheduleResultGUID = OneObj.getString("guid");
                            oneData.szViewDate = OneObj.getString("viewdate");
                            LessonPrepareCorrectAnswerSheetComponent.this.marrLoadUserData.add(oneData);
                            DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject("AnswerSheet_" + oneData.szScheduleResultGUID, UI.getCurrentActivity());
                            ResourceObject.setSuccessListener(new OnSuccessListener() {
                                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                    LessonPrepareCorrectAnswerSheetComponent access$0 = LessonPrepareCorrectAnswerSheetComponent.this;
                                    access$0.mLoadCount = access$0.mLoadCount + 1;
                                    if (ItemObject.readTextData() != null) {
                                        try {
                                            oneData.jsonData = new JSONObject(ItemObject.readTextData());
                                        } catch (JSONException e) {
                                            Log.e("LessonPrepareCorrectAnswerSheetComponent", "Student answersheet json parser error.");
                                            e.printStackTrace();
                                        }
                                    }
                                    if (LessonPrepareCorrectAnswerSheetComponent.this.mLoadCount == LessonPrepareCorrectAnswerSheetComponent.this.marrLoadUserData.size()) {
                                        LessonPrepareCorrectAnswerSheetComponent.this.buildAnswerSheetUI();
                                    }
                                }
                            });
                            ResourceObject.setFailureListener(new OnFailureListener() {
                                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                    LessonPrepareCorrectAnswerSheetComponent access$0 = LessonPrepareCorrectAnswerSheetComponent.this;
                                    access$0.mLoadCount = access$0.mLoadCount + 1;
                                    if (LessonPrepareCorrectAnswerSheetComponent.this.mLoadCount == LessonPrepareCorrectAnswerSheetComponent.this.marrLoadUserData.size()) {
                                        LessonPrepareCorrectAnswerSheetComponent.this.buildAnswerSheetUI();
                                    }
                                }
                            });
                            ResourceObject.setClientID(oneData.szClientID);
                            ResourceObject.setAlwaysActiveCallbacks(true);
                            VirtualNetworkObject.addToQueue(ResourceObject);
                            LessonPrepareCorrectAnswerSheetComponent.this.mVirtualNetworkObjectManager.add(ResourceObject);
                        }
                    }
                    if (LessonPrepareCorrectAnswerSheetComponent.this.mLoadCount == LessonPrepareCorrectAnswerSheetComponent.this.marrLoadUserData.size()) {
                        LessonPrepareCorrectAnswerSheetComponent.this.buildAnswerSheetUI();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LessonPrepareCorrectAnswerSheetComponent.this.displayMessage("解析数据时出现错误，" + e.getMessage());
                }
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                LessonPrepareCorrectAnswerSheetComponent.this.displayMessage("数据加载出现错误，错误原因：" + ItemObject.getErrorText());
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

    protected void buildAnswerSheetUI() {
        try {
            JSONArray jsonArray = this.mAnswerSheetJsonData.getJSONArray("category");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject oneCategory = jsonArray.getJSONObject(i);
                String szTitle = oneCategory.getString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                View textView = new TextView(this.mContextThemeWrapper);
                textView.setTextSize(20.0f);
                textView.setTextColor(-16777216);
                textView.setText(szTitle);
                this.mContentLayout.addView(textView);
                ((LayoutParams) textView.getLayoutParams()).leftMargin = Utilities.dpToPixel(16, this.mContextThemeWrapper);
                JSONArray arrQuestions = oneCategory.getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    String szQuestionGUID = oneQuestion.getString("guid");
                    float fFullScore = 0.0f;
                    try {
                        fFullScore = Float.valueOf(oneQuestion.getString("score")).floatValue();
                    } catch (NumberFormatException e) {
                    }
                    textView = new LinearLayout(this.mContextThemeWrapper);
                    textView.setOrientation(0);
                    this.mContentLayout.addView(textView);
                    ViewGroup.LayoutParams param = (LayoutParams) textView.getLayoutParams();
                    param.leftMargin = Utilities.dpToPixel(21, getContext());
                    param.topMargin = Utilities.dpToPixel(21, getContext());
                    textView.setLayoutParams(param);
                    textView = new TextView(this.mContextThemeWrapper);
                    textView.setText(oneQuestion.getString("index"));
                    textView.setTextSize(16.0f);
                    textView.setTextColor(-16777216);
                    textView.addView(textView);
                    int nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                    UserAnswersComponent UserAnswersComponent;
                    LoadUserData oneStudent;
                    JSONObject oneAnswer;
                    if (nQuestionType == 0 || nQuestionType == 1 || nQuestionType == 2) {
                        UserAnswersComponent = new UserAnswersComponent(this.mContextThemeWrapper);
                        UserAnswersComponent.setFullScore(fFullScore);
                        UserAnswersComponent.setScheduleGUID(this.mScheduleGUID);
                        this.marrUserAnswersComponent.add(UserAnswersComponent);
                        Iterator it = this.marrLoadUserData.iterator();
                        while (it.hasNext()) {
                            oneStudent = (LoadUserData) it.next();
                            if (oneStudent.jsonData != null) {
                                oneAnswer = findUserQuestion(oneStudent.jsonData, szQuestionGUID);
                                boolean bCorrectAnswer = false;
                                if (oneAnswer != null) {
                                    if (oneAnswer.has("correctanswer")) {
                                        if (oneAnswer.getString("correctanswer").equalsIgnoreCase(oneAnswer.getString("answer"))) {
                                            bCorrectAnswer = true;
                                        }
                                    }
                                    UserAnswersComponent.setCorrectAnswer(oneAnswer.getString("correctanswer"));
                                    UserAnswersComponent.addStudentAnswer(oneAnswer.getString("answerguid"), oneStudent.szRealName, oneAnswer.getString("answer"), bCorrectAnswer);
                                } else {
                                    Log.e("LessonPrepareCorrectAnswerSheetComponent", "student " + oneStudent.szRealName + " did not has question " + szQuestionGUID + " answer result. JSONData: " + oneStudent.jsonData.toString());
                                }
                            }
                        }
                        UserAnswersComponent.buildUI();
                        textView.addView(UserAnswersComponent);
                    } else if (nQuestionType == 3 || nQuestionType == 4) {
                        UserAnswersComponent = new UserAnswersComponent(this.mContextThemeWrapper);
                        UserAnswersComponent.setFullScore(fFullScore);
                        UserAnswersComponent.setScheduleGUID(this.mScheduleGUID);
                        this.marrUserAnswersComponent.add(UserAnswersComponent);
                        Iterator it2 = this.marrLoadUserData.iterator();
                        while (it2.hasNext()) {
                            oneStudent = (LoadUserData) it2.next();
                            if (oneStudent.jsonData != null) {
                                oneAnswer = findUserQuestion(oneStudent.jsonData, szQuestionGUID);
                                if (oneAnswer != null) {
                                    String string;
                                    String string2 = oneAnswer.getString("answerguid");
                                    String str = oneStudent.szRealName;
                                    String str2 = oneStudent.szClientID;
                                    String string3 = oneAnswer.getString("answer0");
                                    String string4 = oneAnswer.getString("answer1");
                                    String string5 = oneAnswer.getString("answer2");
                                    if (oneAnswer.has("answer1preview")) {
                                        string = oneAnswer.getString("answer1preview");
                                    } else {
                                        string = "";
                                    }
                                    UserAnswersComponent.addStudentAnswerImage(string2, str, str2, szQuestionGUID, string3, string4, string5, string, 0.0f);
                                } else {
                                    Log.e("LessonPrepareCorrectAnswerSheetComponent", "student " + oneStudent.szRealName + " did not has question " + szQuestionGUID + " answer result. JSONData:" + oneStudent.jsonData.toString());
                                }
                            }
                        }
                        UserAnswersComponent.buildUI();
                        textView.addView(UserAnswersComponent);
                    }
                }
            }
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        if (this.mCallBack != null) {
            this.mCallBack.OnDataLoaded(this.mLessonPrepareResourceGUID, this);
        }
    }

    public void save() {
        if (this.mbAnswerSheetMode) {
            Iterator it = this.marrUserAnswersComponent.iterator();
            while (it.hasNext()) {
                ((UserAnswersComponent) it.next()).save();
            }
            loadUncorrectCount();
            return;
        }
        super.save();
    }

    public boolean isAnswerSheetMode() {
        return this.mbAnswerSheetMode;
    }

    public void openSource() {
        ResourceItemData data = (ResourceItemData) this.marrData.get(this.mDisplayIndex);
        Intent Intent = new Intent(getContext(), ResourceDetailActivity.class);
        Intent.putExtra(CommentComponent.RESOURCEGUID, data.szResourceGUID);
        Intent.putExtra("title", data.szTitle);
        Intent.putExtra("isquestion", false);
        Intent.putExtra("resourcetype", data.nType);
        getContext().startActivity(Intent);
    }

    public int getUnsavedCount() {
        return 0;
    }
}
