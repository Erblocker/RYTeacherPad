package com.netspace.library.service;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.AnswerSheetV2QuestionListAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.AnswerSheetV2OthersComponent;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.im.IMService;
import com.netspace.library.im.IMService.OnIMServiceArrivedListener;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.struct.AnswerSheetV2QuestionItem;
import com.netspace.library.struct.CRestDataBase.OnRestFailureListener;
import com.netspace.library.struct.CRestDataBase.OnRestSuccessListener;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.library.window.AnswerSheetV2OthersWindow;
import com.netspace.library.wrapper.CameraCaptureActivity;
import com.netspace.pad.library.R;
import com.xsj.crasheye.Properties;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.cookie.ClientCookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import retrofit2.Call;
import retrofit2.Response;
import wei.mark.standout.StandOutWindow;

public class AnswerSheetV2MenuService extends Service implements OnItemClickListener, OnClickListener, OnIMServiceArrivedListener, OnRestSuccessListener, OnRestFailureListener {
    private static final int CAPTURE_WINDOW = 400;
    private static final int HIDE_WINDOW = 300;
    public static final String OPERATION = "operation";
    public static final int OPERATION_HIDE = 101;
    public static final int OPERATION_SHOW = 100;
    private static final int SHOW_WINDOW = 200;
    public static final String TAG = "AnswerSheetV2MenuService";
    private static IWmExamDBOpenHelper mDataBase = null;
    private static LayoutParams mParams;
    private static WindowManager mWindowManager;
    private AnswerSheetV2QuestionListAdapter mAdapter;
    private boolean mAdded = false;
    private View mAnswerSheetView;
    private boolean mButtonsShow = true;
    private Button mCancelButton;
    private String mClientID = null;
    private Context mContext;
    protected String mData = null;
    private String mDataSynchronizedGUID = "";
    private ImageButton mExpandButton;
    private String mGUID = "";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (!AnswerSheetV2MenuService.this.mAdded) {
                        AnswerSheetV2MenuService.mWindowManager.addView(AnswerSheetV2MenuService.this.mAnswerSheetView, AnswerSheetV2MenuService.mParams);
                        AnswerSheetV2MenuService.this.mAdded = true;
                        return;
                    }
                    return;
                case 300:
                    if (AnswerSheetV2MenuService.this.mAdded) {
                        AnswerSheetV2MenuService.mWindowManager.removeView(AnswerSheetV2MenuService.this.mAnswerSheetView);
                        AnswerSheetV2MenuService.this.mAdded = false;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private LayoutInflater mInflater;
    private boolean mItemMove = false;
    protected JSONObject mJsonData = null;
    private ListView mListView;
    private String mResourceGUID = "";
    private String mScheduleGUID = "";
    private Date mStartTime;
    private Button mSubmitButton;
    private TextView mTextViewStatus;
    private final Runnable mUpdateTimerRunnable = new Runnable() {
        public void run() {
            if (AnswerSheetV2MenuService.this.mTextViewStatus != null) {
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - AnswerSheetV2MenuService.this.mStartTime.getTime());
                int seconds = (int) (diffInSec % 60);
                diffInSec /= 60;
                int minutes = (int) (diffInSec % 60);
                diffInSec /= 60;
                int hours = (int) (diffInSec % 24);
                diffInSec /= 24;
                int nFinishCount = 0;
                int nTotalCount = 0;
                float fPercent = 0.0f;
                for (int i = 0; i < AnswerSheetV2MenuService.this.marrData.size(); i++) {
                    AnswerSheetV2QuestionItem Item = (AnswerSheetV2QuestionItem) AnswerSheetV2MenuService.this.marrData.get(i);
                    if (!Item.szGuid.isEmpty()) {
                        nTotalCount++;
                    }
                    if (!Item.szAnswer.isEmpty() || !Item.szAnswer0.isEmpty() || !Item.szAnswer1.isEmpty() || !Item.szAnswer2.isEmpty()) {
                        nFinishCount++;
                    }
                }
                if (nTotalCount > 0) {
                    fPercent = (((float) nFinishCount) / ((float) nTotalCount)) * 100.0f;
                }
                final String szResult = String.format("%d%%已完成\n%02d:%02d:%02d", new Object[]{Integer.valueOf((int) fPercent), Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
                AnswerSheetV2MenuService.this.mTextViewStatus.post(new Runnable() {
                    public void run() {
                        AnswerSheetV2MenuService.this.mTextViewStatus.setText(szResult);
                    }
                });
                AnswerSheetV2MenuService.this.mHandler.postDelayed(AnswerSheetV2MenuService.this.mUpdateTimerRunnable, 500);
            }
        }
    };
    private String mUserClassGUID;
    private String mUserClassName;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    private ArrayList<AnswerSheetV2QuestionItem> marrData;
    private boolean mbCancel = false;
    private boolean mbDataLoaded = false;
    private boolean mbLocked = false;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        createFloatView();
    }

    public void onDestroy() {
        this.mHandler.sendEmptyMessage(300);
        IMService.getIMService().unregisterCallBack(this);
        super.onDestroy();
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("operation")) {
            switch (intent.getIntExtra("operation", 100)) {
                case 100:
                    this.mHandler.sendEmptyMessage(200);
                    break;
                case 101:
                    this.mHandler.sendEmptyMessage(300);
                    break;
            }
            if (intent.getExtras().containsKey("data")) {
                setData(intent.getStringExtra("data"));
            }
        }
    }

    private void createFloatView() {
        this.mContext = new ContextThemeWrapper(this, R.style.ComponentTheme);
        this.marrData = new ArrayList();
        this.mAdapter = new AnswerSheetV2QuestionListAdapter(this.mContext, this.marrData);
        AnswerSheetV2OthersComponent.setData(this.marrData, this.mAdapter);
        IMService.getIMService().registerCallBack(this);
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mAnswerSheetView = this.mInflater.inflate(R.layout.layout_answersheetv2, null);
        this.mExpandButton = (ImageButton) this.mAnswerSheetView.findViewById(R.id.imageButtonExpand);
        this.mSubmitButton = (Button) this.mAnswerSheetView.findViewById(R.id.buttonSubmit);
        this.mCancelButton = (Button) this.mAnswerSheetView.findViewById(R.id.buttonCancel);
        this.mTextViewStatus = (TextView) this.mAnswerSheetView.findViewById(R.id.textViewStates);
        this.mListView = (ListView) this.mAnswerSheetView.findViewById(R.id.listViewQuestions);
        this.mListView.setDivider(null);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(this);
        this.mSubmitButton.setVisibility(4);
        this.mSubmitButton.setOnClickListener(this);
        this.mCancelButton.setOnClickListener(this);
        mWindowManager = (WindowManager) getApplicationContext().getSystemService("window");
        mParams = new LayoutParams();
        mParams.type = 2003;
        mParams.format = 1;
        mParams.flags = 40;
        updateUI();
        this.mAnswerSheetView.setOnTouchListener(new OnTouchListener() {
            int lastX;
            int lastY;
            int paramX;
            int paramY;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case 0:
                        this.lastX = (int) event.getRawX();
                        this.lastY = (int) event.getRawY();
                        this.paramX = AnswerSheetV2MenuService.mParams.x;
                        this.paramY = AnswerSheetV2MenuService.mParams.y;
                        AnswerSheetV2MenuService.this.mItemMove = false;
                        break;
                    case 1:
                        if (AnswerSheetV2MenuService.this.mItemMove) {
                            return true;
                        }
                        break;
                    case 2:
                        int dx = ((int) event.getRawX()) - this.lastX;
                        int dy = ((int) event.getRawY()) - this.lastY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            AnswerSheetV2MenuService.mParams.x = this.paramX + dx;
                            AnswerSheetV2MenuService.mParams.y = this.paramY + dy;
                            AnswerSheetV2MenuService.this.mItemMove = true;
                            AnswerSheetV2MenuService.mWindowManager.updateViewLayout(AnswerSheetV2MenuService.this.mAnswerSheetView, AnswerSheetV2MenuService.mParams);
                            break;
                        }
                }
                return false;
            }
        });
        this.mExpandButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AnswerSheetV2MenuService.this.mButtonsShow = !AnswerSheetV2MenuService.this.mButtonsShow;
                AnswerSheetV2MenuService.this.updateUI();
                AnswerSheetV2MenuService.mWindowManager.updateViewLayout(AnswerSheetV2MenuService.this.mAnswerSheetView, AnswerSheetV2MenuService.mParams);
            }
        });
        mWindowManager.addView(this.mAnswerSheetView, mParams);
        this.mAdded = true;
        Display display = mWindowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mParams.x = ((metrics.widthPixels - mParams.width) / 2) - 50;
        mParams.y = (-((metrics.heightPixels - mParams.height) / 2)) + 130;
        mWindowManager.updateViewLayout(this.mAnswerSheetView, mParams);
    }

    private void updateUI() {
        if (this.mButtonsShow) {
            this.mListView.setVisibility(0);
            if (!this.mbLocked) {
                this.mSubmitButton.setVisibility(0);
            }
            this.mCancelButton.setVisibility(0);
            mParams.height = Utilities.dpToPixel(500, this.mContext);
            mParams.width = Utilities.dpToPixel(300, this.mContext);
            this.mExpandButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_toggle_up).color(-13421773).actionBarSize());
            return;
        }
        this.mListView.setVisibility(8);
        this.mSubmitButton.setVisibility(8);
        this.mCancelButton.setVisibility(8);
        mParams.height = Utilities.dpToPixel(100, this.mContext);
        mParams.width = Utilities.dpToPixel(200, this.mContext);
        this.mExpandButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_toggle_down).color(-13421773).actionBarSize());
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (!CameraCaptureActivity.isCaptureInProgress()) {
            AnswerSheetV2QuestionItem Data = (AnswerSheetV2QuestionItem) this.marrData.get(position);
            if (Data.nType == 3 || Data.nType == 4) {
                AnswerSheetV2OthersComponent.setClientID(this.mClientID);
                AnswerSheetV2OthersComponent.setGlobalLock(this.mbLocked);
                startService(StandOutWindow.getShowIntent(this, AnswerSheetV2OthersWindow.class, position));
            }
        }
    }

    private void buildUI() {
        try {
            JSONArray jsonArray = this.mJsonData.getJSONArray("category");
            float fFullScore = 0.0f;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject oneCategory = jsonArray.getJSONObject(i);
                String szTitle = oneCategory.getString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                AnswerSheetV2QuestionItem categoryItem = new AnswerSheetV2QuestionItem();
                categoryItem.szTitle = szTitle;
                this.marrData.add(categoryItem);
                JSONArray arrQuestions = oneCategory.getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    AnswerSheetV2QuestionItem questionItem = new AnswerSheetV2QuestionItem();
                    if (oneQuestion.has("answerguid")) {
                        questionItem.szAnswerGUID = oneQuestion.getString("answerguid");
                    }
                    questionItem.szIndex = oneQuestion.getString("index");
                    if (oneQuestion.has("options")) {
                        questionItem.szOptions = oneQuestion.getString("options");
                    }
                    if (oneQuestion.has("score")) {
                        questionItem.fFullScore = (float) oneQuestion.getDouble("score");
                    }
                    if (oneQuestion.has("correctanswer")) {
                        questionItem.szCorrectAnswer = oneQuestion.getString("correctanswer");
                    }
                    if (oneQuestion.has("answer")) {
                        questionItem.szAnswer = oneQuestion.getString("answer");
                    }
                    if (oneQuestion.has("guid")) {
                        questionItem.szGuid = oneQuestion.getString("guid");
                        int nAnswerResult = mDataBase.GetQuestionAnswerResult(this.mScheduleGUID, oneQuestion.getString("guid"));
                        if (nAnswerResult != 0) {
                            questionItem.nAnswerResult = nAnswerResult;
                            float nAnswerScore = mDataBase.GetQuestionAnswerScore(this.mScheduleGUID, oneQuestion.getString("guid"));
                            questionItem.fAnswerScore = nAnswerScore;
                            fFullScore += nAnswerScore;
                            this.mbLocked = true;
                        }
                    }
                    int nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                    questionItem.nType = nQuestionType;
                    if (nQuestionType == 0) {
                        questionItem.szOptions = "对错";
                    } else if (!(nQuestionType == 1 || nQuestionType == 2 || (nQuestionType != 3 && nQuestionType != 4))) {
                        if (oneQuestion.has("answer0")) {
                            questionItem.szAnswer0 = oneQuestion.getString("answer0");
                        }
                        if (oneQuestion.has("answer1")) {
                            questionItem.szAnswer1 = oneQuestion.getString("answer1");
                        }
                        if (oneQuestion.has("answer1source")) {
                            questionItem.szAnswer1 = oneQuestion.getString("answer1source");
                        }
                        if (oneQuestion.has("answer2")) {
                            questionItem.szAnswer2 = oneQuestion.getString("answer2");
                        }
                    }
                    this.marrData.add(questionItem);
                }
            }
            SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            if (MyiBaseApplication.ReleaseBuild && Settings.getBoolean(this.mScheduleGUID + "_" + this.mResourceGUID + "_lock", false)) {
                this.mAdapter.setLocked(true);
                this.mAdapter.notifyDataSetChanged();
                this.mbLocked = true;
                this.mSubmitButton.setVisibility(4);
                this.mCancelButton.setText("关闭");
                this.mTextViewStatus.setText("作答已停止");
            }
            if (this.mbLocked) {
                this.mTextViewStatus.setText("您的成绩为：" + String.valueOf(fFullScore) + "分");
                this.mSubmitButton.setVisibility(4);
                this.mCancelButton.setText("关闭");
                return;
            }
            this.mStartTime = new Date();
            this.mHandler.post(this.mUpdateTimerRunnable);
            this.mSubmitButton.setVisibility(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setDataBase(IWmExamDBOpenHelper dataBase) {
        if (dataBase == null) {
            throw new IllegalArgumentException("dataBase can not be null.");
        }
        mDataBase = dataBase;
    }

    public void setData(String szData) {
        if (!this.mbDataLoaded) {
            this.mData = szData;
            try {
                JSONObject JsonData = new JSONObject(this.mData);
                this.mbLocked = JsonData.getBoolean("lock");
                this.mResourceGUID = JsonData.getString(CommentComponent.RESOURCEGUID);
                this.mScheduleGUID = JsonData.getString("scheduleguid");
                this.mClientID = JsonData.getString(DeviceOperationRESTServiceProvider.CLIENTID);
                this.mGUID = JsonData.getString("guid");
                this.mUserClassName = JsonData.getString("userclassname");
                this.mUserClassGUID = JsonData.getString(UserHonourFragment.USERCLASSGUID);
                this.mDataSynchronizedGUID = "AnswerSheet_" + this.mGUID;
                DataSynchronizeItemObject DataObject = new DataSynchronizeItemObject(this.mDataSynchronizedGUID, null);
                DataObject.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        if (ItemObject.readTextData() != null) {
                            AnswerSheetV2MenuService.this.loadJsonData(ItemObject.readTextData());
                        } else {
                            AnswerSheetV2MenuService.this.loadOriginalTemplate();
                        }
                    }
                });
                DataObject.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        AnswerSheetV2MenuService.this.loadOriginalTemplate();
                    }
                });
                DataObject.setReadOperation(true);
                DataObject.setNoDeleteOnFinish(true);
                DataObject.setClientID(this.mClientID);
                DataObject.setAlwaysActiveCallbacks(true);
                VirtualNetworkObject.addToQueue(DataObject);
                this.mVirtualNetworkObjectManager.add(DataObject);
                this.mbDataLoaded = true;
            } catch (JSONException e) {
                this.mTextViewStatus.setText("数据加载错误，错误信息：" + e.getMessage());
                this.mTextViewStatus.setVisibility(0);
                e.printStackTrace();
            }
            if (this.mGUID == null || this.mGUID.isEmpty()) {
                throw new InvalidParameterException("GUID must be set.");
            } else if (this.mClientID == null || this.mClientID.isEmpty()) {
                throw new InvalidParameterException("clientid must be set.");
            }
        }
    }

    private void loadOriginalTemplate() {
        PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject("AnswerSheet_" + this.mResourceGUID, null, new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                AnswerSheetV2MenuService.this.loadJsonData(ItemObject.readTextData());
            }
        });
        PrivateDataObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                AnswerSheetV2MenuService.this.mTextViewStatus.setText("获取数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                AnswerSheetV2MenuService.this.mTextViewStatus.setVisibility(0);
            }
        });
        PrivateDataObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(PrivateDataObject);
        this.mVirtualNetworkObjectManager.add(PrivateDataObject);
    }

    private void loadJsonData(String szJsonData) {
        if (szJsonData != null) {
            try {
                this.mJsonData = new JSONObject(szJsonData);
                if (this.mJsonData != null) {
                    buildUI();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        if (mDataBase == null) {
            throw new NullPointerException("must set database before save operation. Call setDataBase before create this control.");
        }
        DataSynchronizeItemObject DataObject;
        AnswerSheetV2OthersComponent.saveAllPreview();
        try {
            JSONArray jsonArray = this.mJsonData.getJSONArray("category");
            int nIndex = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    String szQuestionGUID = oneQuestion.getString("guid");
                    boolean bTrulyAnswered = false;
                    String szGUID = mDataBase.CheckItemExsit(this.mScheduleGUID, szQuestionGUID);
                    AnswerSheetV2QuestionItem item = null;
                    for (int k = 0; k < this.marrData.size(); k++) {
                        if (((AnswerSheetV2QuestionItem) this.marrData.get(k)).szGuid.equalsIgnoreCase(szQuestionGUID)) {
                            item = (AnswerSheetV2QuestionItem) this.marrData.get(k);
                            break;
                        }
                    }
                    if (item != null) {
                        if (szGUID == null || szGUID.isEmpty()) {
                            szGUID = Utilities.createGUID();
                        }
                        oneQuestion.put("answerguid", szGUID);
                        if (!(item.szAnswer.isEmpty() && item.szAnswer0.isEmpty() && item.szAnswer1.isEmpty() && item.szAnswer2.isEmpty())) {
                            bTrulyAnswered = true;
                        }
                        oneQuestion.put("answer", item.szAnswer);
                        oneQuestion.put("answer0", item.szAnswer0);
                        oneQuestion.put("answer1", item.szAnswer1);
                        oneQuestion.put("answer2", item.szAnswer2);
                        oneQuestion.put("answer1preview", item.szAnswer1Preview);
                        UserInfo.UserScore("SaveAnswerSheet", this.mScheduleGUID);
                        if (bTrulyAnswered) {
                            mDataBase.SetItemRead(this.mScheduleGUID, szQuestionGUID, szGUID, this.mUserClassGUID, this.mUserClassName, 0, 2);
                            if (item.nType == 0 || item.nType == 1 || item.nType == 2) {
                                DataObject = new DataSynchronizeItemObject(szGUID, null);
                                DataObject.setSuccessListener(new OnSuccessListener() {
                                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                    }
                                });
                                DataObject.writeTextData(getSelectorControlXML(item.szAnswer, szQuestionGUID));
                                DataObject.setReadOperation(false);
                                DataObject.setNoDeleteOnFinish(true);
                                DataObject.setClientID(this.mClientID);
                                DataObject.setAlwaysActiveCallbacks(true);
                                VirtualNetworkObject.addToQueue(DataObject);
                            }
                        }
                        nIndex++;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataObject = new DataSynchronizeItemObject(this.mDataSynchronizedGUID, null);
        DataObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                Toast.makeText(AnswerSheetV2MenuService.this.mContext, "数据已成功保存", 0).show();
            }
        });
        DataObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Toast.makeText(AnswerSheetV2MenuService.this.mContext, "数据保存出现错误，" + ItemObject.getErrorText(), 0).show();
            }
        });
        DataObject.writeTextData(this.mJsonData.toString());
        DataObject.setReadOperation(false);
        DataObject.setNoDeleteOnFinish(true);
        DataObject.setClientID(this.mClientID);
        DataObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(DataObject);
    }

    private String getSelectorControlXML(String szValue, String szResourceGUID) {
        try {
            boolean bHasData = false;
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element mRootElement = document.createElement("LessonsScheduleUserAnswer");
            document.setXmlVersion(Properties.REST_VERSION);
            document.appendChild(mRootElement);
            mRootElement.setAttribute(ClientCookie.VERSION_ATTR, Properties.REST_VERSION);
            mRootElement.setAttribute("resourceGUID", szResourceGUID);
            Element OneElement = document.createElement("SelectorView");
            OneElement.setAttribute("height", "30");
            OneElement.setTextContent(szValue);
            mRootElement.appendChild(OneElement);
            if (!OneElement.getTextContent().isEmpty()) {
                bHasData = true;
            }
            if (bHasData) {
                return Utilities.XMLToString(document);
            }
            return "";
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonSubmit) {
            save();
        } else if (v.getId() != R.id.buttonCancel) {
        } else {
            if (this.mbLocked) {
                close();
                return;
            }
            AlertDialog alert = new Builder(this).setTitle("关闭提示").setMessage("确实要取消吗？关闭答题卡后所有未保存的作答都将丢失，请确认是否继续").setPositiveButton("继续关闭", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    AnswerSheetV2MenuService.this.close();
                }
            }).setNegativeButton("取消", null).create();
            alert.getWindow().setType(2003);
            alert.show();
        }
    }

    public void close() {
        this.mHandler.removeCallbacks(this.mUpdateTimerRunnable);
        StandOutWindow.closeAll(this.mContext, AnswerSheetV2OthersWindow.class);
        AnswerSheetV2OthersComponent.setData(null, null);
        this.marrData = null;
        this.mAdapter = null;
        this.mHandler.obtainMessage(300).sendToTarget();
        stopSelf();
    }

    public void OnMessageArrived(String szMessage) {
        int nPos = szMessage.indexOf("StopSubmit");
        if (nPos != -1 && szMessage.substring(nPos + 11).equalsIgnoreCase(this.mScheduleGUID)) {
            this.mHandler.removeCallbacks(this.mUpdateTimerRunnable);
            this.mHandler.post(new Runnable() {
                public void run() {
                    StandOutWindow.closeAll(AnswerSheetV2MenuService.this.mContext, AnswerSheetV2OthersWindow.class);
                    AnswerSheetV2MenuService.this.save();
                    AnswerSheetV2MenuService.this.mAdapter.setLocked(true);
                    AnswerSheetV2MenuService.this.mAdapter.notifyDataSetChanged();
                    AnswerSheetV2MenuService.this.mbLocked = true;
                    AnswerSheetV2MenuService.this.mSubmitButton.setVisibility(4);
                    AnswerSheetV2MenuService.this.mCancelButton.setText("关闭");
                    AnswerSheetV2MenuService.this.mTextViewStatus.setText("作答已停止");
                    Editor edit = PreferenceManager.getDefaultSharedPreferences(AnswerSheetV2MenuService.this.mContext).edit();
                    edit.putBoolean(new StringBuilder(String.valueOf(AnswerSheetV2MenuService.this.mScheduleGUID)).append("_").append(AnswerSheetV2MenuService.this.mResourceGUID).append("_lock").toString(), true);
                    edit.apply();
                }
            });
        }
    }

    public void OnDataFailure(Call<?> call, Throwable arg1) {
        Utilities.showAlertMessage(null, "数据保存失败", "作答数据保存失败，请稍候重试。\n错误信息：" + arg1.getMessage());
    }

    public void OnDataSuccess(Call<?> call, Response<?> response) {
    }
}
