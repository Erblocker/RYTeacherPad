package com.netspace.library.service;

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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.AnswerSheetV2QuestionListAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.AnswerSheetV3OthersComponent;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.database.AnswerSheetResult;
import com.netspace.library.database.AnswerSheetResultDao.Properties;
import com.netspace.library.database.AnswerSheetStudentAnswer;
import com.netspace.library.database.AnswerSheetStudentAnswerDao;
import com.netspace.library.database.DaoSession;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.im.IMService;
import com.netspace.library.im.IMService.OnIMServiceArrivedListener;
import com.netspace.library.receiver.WifiReceiver.WifiConnect;
import com.netspace.library.receiver.WifiReceiver.WifiDisconnect;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.struct.AnswerSheetV2QuestionItem;
import com.netspace.library.struct.CRestDataBase.OnRestFailureListener;
import com.netspace.library.struct.CRestDataBase.OnRestSuccessListener;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.RESTEngine;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.library.virtualnetworkobject.helper.RESTDataEngineHelper.RESTDataChanged;
import com.netspace.library.window.AnswerSheetV3OthersWindow;
import com.netspace.library.wrapper.CameraCaptureActivity;
import com.netspace.pad.library.R;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.query.WhereCondition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Response;
import wei.mark.standout.StandOutWindow;

public class AnswerSheetV3MenuService extends Service implements OnItemClickListener, OnClickListener, OnIMServiceArrivedListener, OnRestSuccessListener, OnRestFailureListener {
    private static final int CAPTURE_WINDOW = 400;
    private static final int HIDE_WINDOW = 300;
    public static final String OPERATION = "operation";
    public static final int OPERATION_HIDE = 101;
    public static final int OPERATION_SHOW = 100;
    private static final int SHOW_WINDOW = 200;
    public static final String TAG = "AnswerSheetV3MenuService";
    private static AnswerSheetV3MenuService mInstance;
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
    private ImageButton mExpandButton;
    private int mFailureSaveCount = 0;
    private FrameLayout mFrame;
    private String mGUID = "";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (!AnswerSheetV3MenuService.this.mAdded) {
                        AnswerSheetV3MenuService.mWindowManager.addView(AnswerSheetV3MenuService.this.mAnswerSheetView, AnswerSheetV3MenuService.mParams);
                        AnswerSheetV3MenuService.this.mAdded = true;
                        return;
                    }
                    return;
                case 300:
                    if (AnswerSheetV3MenuService.this.mAdded) {
                        AnswerSheetV3MenuService.mWindowManager.removeView(AnswerSheetV3MenuService.this.mAnswerSheetView);
                        AnswerSheetV3MenuService.this.mAdded = false;
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
    private int mSuccessSaveCount = 0;
    private TextView mTextViewStatus;
    private final Runnable mUpdateTimerRunnable = new Runnable() {
        public void run() {
            if (AnswerSheetV3MenuService.this.mTextViewStatus != null) {
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - AnswerSheetV3MenuService.this.mStartTime.getTime());
                int seconds = (int) (diffInSec % 60);
                diffInSec /= 60;
                int minutes = (int) (diffInSec % 60);
                diffInSec /= 60;
                int hours = (int) (diffInSec % 24);
                diffInSec /= 24;
                int nFinishCount = 0;
                int nTotalCount = 0;
                float fPercent = 0.0f;
                for (int i = 0; i < AnswerSheetV3MenuService.this.marrData.size(); i++) {
                    AnswerSheetV2QuestionItem Item = (AnswerSheetV2QuestionItem) AnswerSheetV3MenuService.this.marrData.get(i);
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
                AnswerSheetV3MenuService.this.mTextViewStatus.post(new Runnable() {
                    public void run() {
                        AnswerSheetV3MenuService.this.mTextViewStatus.setText(szResult);
                    }
                });
                AnswerSheetV3MenuService.this.mHandler.postDelayed(AnswerSheetV3MenuService.this.mUpdateTimerRunnable, 500);
            }
        }
    };
    private String mUserClassGUID;
    private String mUserClassName;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    private ArrayList<AnswerSheetV2QuestionItem> marrData;
    private boolean mbCancel = false;
    private boolean mbChanged = false;
    private boolean mbDataLoaded = false;
    private boolean mbLocked = false;
    private int mnNeedSaveCount = 0;
    private String mszAuthorNotifyClientID;

    public static class AnswerSheetCorrectingImageChanged {
        public String szQuestionGUID;
    }

    public static class AnswerSheetDataChanged {
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public static AnswerSheetV3MenuService getInstance() {
        return mInstance;
    }

    public void onCreate() {
        super.onCreate();
        mInstance = this;
        createFloatView();
    }

    public void onDestroy() {
        mInstance = null;
        this.mHandler.sendEmptyMessage(300);
        IMService.getIMService().unregisterCallBack(this);
        EventBus.getDefault().unregister(this);
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
        AnswerSheetV3OthersComponent.setData(this.marrData, this.mAdapter);
        IMService.getIMService().registerCallBack(this);
        EventBus.getDefault().register(this);
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mAnswerSheetView = this.mInflater.inflate(R.layout.layout_answersheetv3, null);
        this.mExpandButton = (ImageButton) this.mAnswerSheetView.findViewById(R.id.imageButtonExpand);
        this.mSubmitButton = (Button) this.mAnswerSheetView.findViewById(R.id.buttonSubmit);
        this.mCancelButton = (Button) this.mAnswerSheetView.findViewById(R.id.buttonCancel);
        this.mTextViewStatus = (TextView) this.mAnswerSheetView.findViewById(R.id.textViewStates);
        this.mListView = (ListView) this.mAnswerSheetView.findViewById(R.id.listViewQuestions);
        this.mListView.setDivider(null);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(this);
        this.mFrame = (FrameLayout) this.mAnswerSheetView.findViewById(R.id.frame1);
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
                        this.paramX = AnswerSheetV3MenuService.mParams.x;
                        this.paramY = AnswerSheetV3MenuService.mParams.y;
                        AnswerSheetV3MenuService.this.mItemMove = false;
                        break;
                    case 1:
                        if (AnswerSheetV3MenuService.this.mItemMove) {
                            return true;
                        }
                        break;
                    case 2:
                        int dx = ((int) event.getRawX()) - this.lastX;
                        int dy = ((int) event.getRawY()) - this.lastY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            AnswerSheetV3MenuService.mParams.x = this.paramX + dx;
                            AnswerSheetV3MenuService.mParams.y = this.paramY + dy;
                            AnswerSheetV3MenuService.this.mItemMove = true;
                            AnswerSheetV3MenuService.mWindowManager.updateViewLayout(AnswerSheetV3MenuService.this.mAnswerSheetView, AnswerSheetV3MenuService.mParams);
                            break;
                        }
                }
                return false;
            }
        });
        this.mExpandButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AnswerSheetV3MenuService.this.mButtonsShow = !AnswerSheetV3MenuService.this.mButtonsShow;
                AnswerSheetV3MenuService.this.updateUI();
                AnswerSheetV3MenuService.mWindowManager.updateViewLayout(AnswerSheetV3MenuService.this.mAnswerSheetView, AnswerSheetV3MenuService.mParams);
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
            this.mFrame.setVisibility(0);
            if (!this.mbLocked) {
                this.mSubmitButton.setVisibility(0);
            }
            this.mCancelButton.setVisibility(0);
            mParams.height = Utilities.dpToPixel(500, this.mContext);
            mParams.width = Utilities.dpToPixel(380, this.mContext);
            this.mExpandButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_chevron_up).color(-13421773).actionBarSize());
        } else {
            this.mListView.setVisibility(8);
            this.mFrame.setVisibility(8);
            this.mSubmitButton.setVisibility(8);
            this.mCancelButton.setVisibility(8);
            mParams.height = Utilities.dpToPixel(100, this.mContext);
            mParams.width = Utilities.dpToPixel(200, this.mContext);
            this.mExpandButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_chevron_down).color(-13421773).actionBarSize());
        }
        if (!this.mbDataLoaded) {
            return;
        }
        if (this.mbLocked) {
            this.mHandler.removeCallbacks(this.mUpdateTimerRunnable);
            this.mTextViewStatus.setText("您的成绩为：" + String.valueOf(calcScore()) + "分");
            this.mSubmitButton.setVisibility(8);
            this.mCancelButton.setText("关闭");
        } else if (this.mStartTime == null) {
            this.mStartTime = new Date();
            this.mHandler.post(this.mUpdateTimerRunnable);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (!CameraCaptureActivity.isCaptureInProgress()) {
            AnswerSheetV2QuestionItem Data = (AnswerSheetV2QuestionItem) this.marrData.get(position);
            if (Data.nType == 3 || Data.nType == 4) {
                AnswerSheetV3OthersComponent.setClientID(this.mClientID);
                AnswerSheetV3OthersComponent.setGlobalLock(this.mbLocked);
                startService(StandOutWindow.getShowIntent(this, AnswerSheetV3OthersWindow.class, position));
            }
        }
    }

    private void buildUI() {
        DaoSession daoSession = RESTEngine.getDefault().getDaoSession();
        try {
            if (this.mJsonData.has("authorusername")) {
                this.mszAuthorNotifyClientID = new StringBuilder(String.valueOf(this.mJsonData.getString("authorusername"))).append("_teacherpad").toString();
            }
            JSONArray jsonArray = this.mJsonData.getJSONArray("category");
            float fFullScore = 0.0f;
            boolean bHasAnswers = false;
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
                    if (oneQuestion.has("guid")) {
                        questionItem.szGuid = oneQuestion.getString("guid");
                        AnswerSheetResult answerResult = (AnswerSheetResult) daoSession.queryBuilder(AnswerSheetResult.class).where(Properties.Scheduleguid.eq(this.mScheduleGUID), Properties.Questionguid.eq(questionItem.szGuid)).limit(1).unique();
                        if (!(answerResult == null || answerResult.getAnswerresult().intValue() == 0)) {
                            questionItem.nAnswerResult = answerResult.getAnswerresult().intValue();
                            float nAnswerScore = answerResult.getAnswerscore().floatValue();
                            questionItem.fAnswerScore = nAnswerScore;
                            fFullScore += nAnswerScore;
                            questionItem.szCorrectingPreview = answerResult.getAnswercorrecthandwritepreview();
                            this.mbLocked = true;
                        }
                    }
                    AnswerSheetStudentAnswer studentAnswer = (AnswerSheetStudentAnswer) daoSession.queryBuilder(AnswerSheetStudentAnswer.class).where(AnswerSheetStudentAnswerDao.Properties.Scheduleguid.eq(this.mScheduleGUID), AnswerSheetStudentAnswerDao.Properties.Questionguid.eq(questionItem.szGuid)).limit(1).unique();
                    if (studentAnswer != null) {
                        questionItem.szAnswer = studentAnswer.getAnswerchoice();
                        questionItem.szAnswer0 = studentAnswer.getAnswertext();
                        questionItem.szClientID = studentAnswer.getClientid();
                        questionItem.szAnswer1 = studentAnswer.getAnswerhandwritedata();
                        questionItem.szAnswer1Preview = studentAnswer.getAnswerhandwritepreview();
                        questionItem.szAnswer2 = studentAnswer.getAnswercamera();
                        bHasAnswers = true;
                    }
                    int nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                    questionItem.nType = nQuestionType;
                    if (nQuestionType == 0) {
                        questionItem.szOptions = "对错";
                    } else if (!(nQuestionType == 1 || nQuestionType == 2 || nQuestionType == 3)) {
                    }
                    this.marrData.add(questionItem);
                }
            }
            this.mAdapter.setLocked(this.mbLocked);
            SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            if (MyiBaseApplication.ReleaseBuild && Settings.getBoolean(this.mScheduleGUID + "_" + this.mResourceGUID + "_lock", false)) {
                this.mAdapter.setLocked(true);
                this.mAdapter.notifyDataSetChanged();
                this.mbLocked = true;
                this.mSubmitButton.setVisibility(4);
                this.mCancelButton.setText("关闭");
                this.mTextViewStatus.setText("作答已停止");
            }
            if (bHasAnswers) {
                this.mSubmitButton.setText("已全部提交");
            }
            updateUI();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                this.mTextViewStatus.setText("正在加载数据...");
                this.mSubmitButton.setVisibility(4);
                this.mCancelButton.setText("关闭");
                loadOriginalTemplate();
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
                AnswerSheetV3MenuService.this.loadJsonData(ItemObject.readTextData());
            }
        });
        PrivateDataObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                AnswerSheetV3MenuService.this.mTextViewStatus.setText("获取数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                AnswerSheetV3MenuService.this.mTextViewStatus.setVisibility(0);
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
        AnswerSheetV3OthersComponent.saveAllPreview();
        DaoSession daoSession = RESTEngine.getDefault().getDaoSession();
        this.mFailureSaveCount = 0;
        this.mSuccessSaveCount = 0;
        this.mnNeedSaveCount = 0;
        this.mSubmitButton.setText("正在提交");
        try {
            JSONArray jsonArray = this.mJsonData.getJSONArray("category");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    String szQuestionGUID = arrQuestions.getJSONObject(j).getString("guid");
                    String szGUID = null;
                    AnswerSheetV2QuestionItem item = null;
                    AnswerSheetStudentAnswer oneAnswer = null;
                    List<AnswerSheetStudentAnswer> lists = daoSession.queryBuilder(AnswerSheetStudentAnswer.class).where(AnswerSheetStudentAnswerDao.Properties.Scheduleguid.eq(this.mScheduleGUID), AnswerSheetStudentAnswerDao.Properties.Questionguid.eq(szQuestionGUID)).list();
                    if (lists.size() > 0) {
                        oneAnswer = (AnswerSheetStudentAnswer) lists.get(0);
                    }
                    if (oneAnswer == null) {
                        oneAnswer = new AnswerSheetStudentAnswer();
                        oneAnswer.setGuid(Utilities.createGUID());
                    } else {
                        szGUID = oneAnswer.getGuid();
                    }
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
                        oneAnswer.setAnswerdate(new Date());
                        oneAnswer.setAnswerchoice(item.szAnswer);
                        oneAnswer.setAnswertext(item.szAnswer0);
                        oneAnswer.setAnswercamera(item.szAnswer2);
                        oneAnswer.setAnswerhandwritedata(item.szAnswer1);
                        oneAnswer.setAnswerhandwritepreview(item.szAnswer1Preview);
                        if (this.mszAuthorNotifyClientID != null) {
                            oneAnswer.setNotifyclientid(this.mszAuthorNotifyClientID);
                        }
                        oneAnswer.setQuestionguid(szQuestionGUID);
                        oneAnswer.setAnswersheetresourceguid(this.mResourceGUID);
                        oneAnswer.setScheduleguid(this.mScheduleGUID);
                        oneAnswer.setSyn_isdelete(Integer.valueOf(0));
                        oneAnswer.setFailureListener(this).setSuccessListener(this);
                        EventBus.getDefault().post(oneAnswer);
                        this.mnNeedSaveCount++;
                        UserInfo.UserScore("SaveAnswerSheet", this.mScheduleGUID);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonSubmit) {
            save();
        } else if (v.getId() != R.id.buttonCancel) {
        } else {
            if (this.mbLocked) {
                close();
            } else if (this.mbChanged) {
                Utilities.showAlertMessage(this, "关闭提示", "当前有作答尚未保存，关闭答题卡后所有未保存的作答都将丢失，请确认是否继续", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AnswerSheetV3MenuService.this.close();
                    }
                }, null);
            } else {
                close();
            }
        }
    }

    public void close() {
        this.mHandler.removeCallbacks(this.mUpdateTimerRunnable);
        StandOutWindow.closeAll(this.mContext, AnswerSheetV3OthersWindow.class);
        AnswerSheetV3OthersComponent.setData(null, null);
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
                    StandOutWindow.closeAll(AnswerSheetV3MenuService.this.mContext, AnswerSheetV3OthersWindow.class);
                    AnswerSheetV3MenuService.this.save();
                    AnswerSheetV3MenuService.this.mAdapter.setLocked(true);
                    AnswerSheetV3MenuService.this.mAdapter.notifyDataSetChanged();
                    AnswerSheetV3MenuService.this.mbLocked = true;
                    AnswerSheetV3MenuService.this.mSubmitButton.setVisibility(4);
                    AnswerSheetV3MenuService.this.mCancelButton.setText("关闭");
                    AnswerSheetV3MenuService.this.mTextViewStatus.setText("作答已停止");
                    Editor edit = PreferenceManager.getDefaultSharedPreferences(AnswerSheetV3MenuService.this.mContext).edit();
                    edit.putBoolean(new StringBuilder(String.valueOf(AnswerSheetV3MenuService.this.mScheduleGUID)).append("_").append(AnswerSheetV3MenuService.this.mResourceGUID).append("_lock").toString(), true);
                    edit.apply();
                }
            });
        }
    }

    public void OnDataFailure(Call<?> call, Throwable arg1) {
        if (this.mFailureSaveCount == 0) {
            this.mSubmitButton.setText("提交");
            Utilities.showAlertMessage(null, "数据提交失败", "作答数据提交到服务端失败，请稍候重试。错误信息：" + arg1.getMessage());
        }
        this.mFailureSaveCount++;
    }

    public void OnDataSuccess(Call<?> call, Response<?> response) {
        this.mSuccessSaveCount++;
        if (this.mSuccessSaveCount == this.mnNeedSaveCount && this.mFailureSaveCount == 0) {
            this.mSubmitButton.setText("已全部提交");
            this.mbChanged = false;
        }
    }

    @Subscribe
    public void onAnswerSheetDataChanged(AnswerSheetDataChanged data) {
        this.mSubmitButton.setText("提交");
        this.mbChanged = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRESTDataChanged(RESTDataChanged data) {
        if (data.szTableName.equalsIgnoreCase("answersheetresult")) {
            AnswerSheetResult answerResult = (AnswerSheetResult) RESTEngine.getDefault().getDaoSession().queryBuilder(AnswerSheetResult.class).where(Properties.Guid.eq(data.szGUID), new WhereCondition[0]).limit(1).unique();
            if (answerResult != null) {
                Iterator it = this.marrData.iterator();
                while (it.hasNext()) {
                    AnswerSheetV2QuestionItem oneItem = (AnswerSheetV2QuestionItem) it.next();
                    if (oneItem.szGuid.equalsIgnoreCase(answerResult.getQuestionguid())) {
                        int nAnswerResult = answerResult.getAnswerresult().intValue();
                        if (nAnswerResult != 0) {
                            oneItem.nAnswerResult = nAnswerResult;
                            oneItem.fAnswerScore = answerResult.getAnswerscore().floatValue();
                            oneItem.szCorrectingPreview = answerResult.getAnswercorrecthandwritepreview();
                            this.mbLocked = true;
                            this.mAdapter.setLocked(this.mbLocked);
                            this.mAdapter.notifyDataSetChanged();
                            updateUI();
                            AnswerSheetCorrectingImageChanged changeNotify = new AnswerSheetCorrectingImageChanged();
                            changeNotify.szQuestionGUID = oneItem.szGuid;
                            EventBus.getDefault().post(changeNotify);
                            return;
                        }
                    }
                }
            }
        }
    }

    private float calcScore() {
        try {
            JSONArray jsonArray = this.mJsonData.getJSONArray("category");
            float fFullScore = 0.0f;
            DaoSession daoSession = RESTEngine.getDefault().getDaoSession();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    if (arrQuestions.getJSONObject(j).has("guid")) {
                        AnswerSheetResult answerResult = (AnswerSheetResult) daoSession.queryBuilder(AnswerSheetResult.class).where(Properties.Scheduleguid.eq(this.mScheduleGUID), Properties.Questionguid.eq(oneQuestion.getString("guid"))).limit(1).unique();
                        if (!(answerResult == null || answerResult.getAnswerresult().intValue() == 0)) {
                            fFullScore += answerResult.getAnswerscore().floatValue();
                        }
                    }
                }
            }
            return fFullScore;
        } catch (JSONException e) {
            return 0.0f;
        }
    }

    @Subscribe
    public void onWifiConnect(WifiConnect data) {
    }

    @Subscribe
    public void onWifiDisconnect(WifiDisconnect data) {
    }
}
