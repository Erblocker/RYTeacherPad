package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.controls.CircleIndicator;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.controls.CustomSelectorView;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.controls.DrawView;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.library.wrapper.CameraCaptureActivity;
import com.netspace.library.wrapper.CameraCaptureActivity.CameraCaptureCallBack;
import com.netspace.pad.library.R;
import com.xsj.crasheye.Properties;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.cookie.ClientCookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnswerSheetComponent extends CustomFrameLayout implements IComponents {
    private static final int ANSWER_SELECTOR_BUTTON_HEIGHT = 30;
    private static final int ANSWER_SELECTOR_BUTTON_WIDTH = 30;
    private static final float ANSWER_SELECTOR_TEXT_SIZE = 15.0f;
    private static final String TAG = "AnswerSheetComponent";
    private static IWmExamDBOpenHelper mDataBase = null;
    private static AnswerSheetCallBack mGlobalCallBack;
    private AnswerSheetCallBack mAnswerSheetCallBack;
    private ComponentCallBack mCallBack;
    private Button mCancelButton;
    private String mClientID = null;
    private LinearLayout mContentLayout;
    private ContextThemeWrapper mContextThemeWrapper;
    private String mData = null;
    private String mDataSynchronizedGUID = "";
    private String mFileName;
    private String mGUID = "";
    private Handler mHandler = new Handler();
    private LinearLayout mInfoLayout;
    private JSONObject mJsonData = null;
    private HashMap<String, View> mMapQuestionToView = new HashMap();
    private String mResourceGUID = "";
    private View mRootView;
    private String mScheduleGUID = "";
    private Date mStartTime;
    private boolean mStartTimeCount = true;
    private Button mSubmitButton;
    private TextView mTextViewInfo;
    private TextView mTextViewMessage;
    private final Runnable mUpdateTimerRunnable = new Runnable() {
        public void run() {
            if (AnswerSheetComponent.this.mTextViewInfo != null) {
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - AnswerSheetComponent.this.mStartTime.getTime());
                int seconds = (int) (diffInSec % 60);
                diffInSec /= 60;
                int minutes = (int) (diffInSec % 60);
                diffInSec /= 60;
                int hours = (int) (diffInSec % 24);
                diffInSec /= 24;
                final String szResult = String.format("当前耗时   %02d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
                AnswerSheetComponent.this.mTextViewInfo.post(new Runnable() {
                    public void run() {
                        AnswerSheetComponent.this.mTextViewInfo.setText(szResult);
                    }
                });
                AnswerSheetComponent.this.mHandler.postDelayed(AnswerSheetComponent.this.mUpdateTimerRunnable, 500);
            }
        }
    };
    private String mUserClassGUID;
    private String mUserClassName;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    private boolean mbCancel = false;
    private boolean mbDataLoaded = false;
    private boolean mbLocked = false;

    public interface AnswerSheetCallBack {
        void onCancel();

        void onSubmit();
    }

    public class AnswerModeAdapter extends PagerAdapter {
        private String[] mData = new String[3];
        private String mDrawViewPreviewImageName;
        private IComponents[] mView = new IComponents[3];
        private Context m_Context;
        private int m_nCount = 3;
        private String mszGUID;

        public AnswerModeAdapter(Context Context) {
            this.m_Context = Context;
            this.mData[0] = "";
            this.mData[1] = "";
            this.mData[2] = "";
        }

        public int getCount() {
            return this.m_nCount;
        }

        public void setGUID(String szGUID) {
            this.mszGUID = szGUID;
        }

        public void setData(int nIndex, String szData) {
            this.mData[nIndex] = szData;
        }

        public String getData(int nIndex) {
            if (this.mView[nIndex] != null) {
                return this.mView[nIndex].getData();
            }
            return this.mData[nIndex];
        }

        public boolean isExpanded() {
            return (this.mView[0] == null && this.mView[1] == null && this.mView[2] == null) ? false : true;
        }

        public String saveDrawView() {
            if (this.mView[1] == null || this.mView[1].getData().isEmpty()) {
                return "";
            }
            DrawView drawView = this.mView[1].getDrawView();
            drawView.setSize(drawView.getWidth(), drawView.getHeight());
            Bitmap bitmap = drawView.saveToBitmap();
            if (this.mDrawViewPreviewImageName == null) {
                this.mDrawViewPreviewImageName = "DrawViewPreview_" + Utilities.createGUID();
            }
            DataSynchronizeItemObject CallItem = new DataSynchronizeItemObject(this.mDrawViewPreviewImageName, null);
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                }
            });
            CallItem.writeTextData(Utilities.saveBitmapToBase64String(bitmap));
            CallItem.setReadOperation(false);
            CallItem.setClientID(AnswerSheetComponent.this.mClientID);
            CallItem.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(CallItem);
            return this.mDrawViewPreviewImageName;
        }

        public Object instantiateItem(View collection, int position) {
            View view = null;
            View RootView;
            if (position == 0) {
                RootView = new TextComponent(AnswerSheetComponent.this.getContext());
                RootView.setData(this.mData[position]);
                view = RootView;
                RootView.setLocked(AnswerSheetComponent.this.mbLocked);
                this.mView[position] = RootView;
            } else if (position == 1) {
                RootView = new DrawComponent(AnswerSheetComponent.this.getContext());
                RootView.setData(this.mData[position]);
                view = RootView;
                RootView.setLocked(AnswerSheetComponent.this.mbLocked);
                this.mView[position] = RootView;
                if (this.mszGUID != null) {
                    DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(this.mszGUID + "_CorrectResult", null);
                    ResourceObject.setSuccessListener(new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            String szData = ItemObject.readTextData();
                            if (szData != null && !szData.isEmpty()) {
                                RootView.mDrawView.fromString(ItemObject.readTextData());
                                RootView.setLocked(true);
                                AnswerSheetComponent.this.mbLocked = true;
                            }
                        }
                    });
                    ResourceObject.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        }
                    });
                    ResourceObject.setClientID(AnswerSheetComponent.this.mClientID);
                    ResourceObject.setReadOperation(true);
                    ResourceObject.setAlwaysActiveCallbacks(true);
                    ResourceObject.setIgnoreActivityFinishCheck(true);
                    VirtualNetworkObject.addToQueue(ResourceObject);
                }
            } else if (position == 2) {
                RootView = new CameraComponent(AnswerSheetComponent.this.getContext());
                RootView.setClientID(AnswerSheetComponent.this.mClientID);
                RootView.setData(this.mData[position]);
                view = RootView;
                RootView.setLocked(AnswerSheetComponent.this.mbLocked);
                this.mView[position] = RootView;
                RootView.setCallBack(new ComponentCallBack() {
                    public void OnDataLoaded(String szFileName, IComponents Component) {
                    }

                    public void OnDataUploaded(String szData, IComponents Component) {
                    }

                    public void OnRequestIntent(Intent intent, IComponents Component) {
                        Intent intent2 = new Intent(AnswerSheetComponent.this.getContext(), CameraCaptureActivity.class);
                        final CameraComponent cameraComponent = RootView;
                        CameraCaptureActivity.setCallBack(new CameraCaptureCallBack() {
                            public void onCaptureComplete(String szFileName) {
                                Intent intent = new Intent();
                                intent.setData(Uri.fromFile(new File(szFileName)));
                                cameraComponent.intentComplete(intent);
                            }
                        });
                        intent2.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                        AnswerSheetComponent.this.getContext().startActivity(intent2);
                    }
                });
            }
            ((ViewPager) collection).addView(view, 0);
            return view;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);
        }
    }

    public AnswerSheetComponent(Context context) {
        super(context);
        initView();
    }

    public AnswerSheetComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AnswerSheetComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_answersheet, this, true);
        this.mContentLayout = (LinearLayout) this.mRootView.findViewById(R.id.contentLayout);
        this.mInfoLayout = (LinearLayout) this.mRootView.findViewById(R.id.linearlayoutCountdown);
        this.mTextViewInfo = (TextView) this.mRootView.findViewById(R.id.textViewTimeCount);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mSubmitButton = (Button) this.mRootView.findViewById(R.id.buttonSubmit);
        this.mCancelButton = (Button) this.mRootView.findViewById(R.id.buttonCancel);
        this.mTextViewMessage.setVisibility(4);
        this.mSubmitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AnswerSheetComponent.this.save();
            }
        });
        this.mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AnswerSheetComponent.this.mbCancel = true;
                if (AnswerSheetComponent.this.mAnswerSheetCallBack != null) {
                    AnswerSheetComponent.this.mAnswerSheetCallBack.onCancel();
                }
                if (AnswerSheetComponent.mGlobalCallBack != null) {
                    AnswerSheetComponent.mGlobalCallBack.onCancel();
                }
            }
        });
    }

    public static void setDataBase(IWmExamDBOpenHelper dataBase) {
        if (dataBase == null) {
            throw new IllegalArgumentException("dataBase can not be null.");
        }
        mDataBase = dataBase;
    }

    public void setAnswerSheetCallBack(AnswerSheetCallBack CallBack) {
        this.mAnswerSheetCallBack = CallBack;
    }

    public static void setGlobalCallBack(AnswerSheetCallBack CallBack) {
        mGlobalCallBack = CallBack;
    }

    private void addCorrectResultIcon(int nAnswerResult, int nScore, int nFullScore, LinearLayout layout) {
        String szScore;
        if (nAnswerResult != 0) {
            szScore = "(+" + nScore + "分)";
        } else {
            szScore = "(" + nFullScore + "分)";
        }
        TextView textViewScore = new TextView(this.mContextThemeWrapper);
        textViewScore.setText(szScore);
        layout.addView(textViewScore, 1);
        if (nAnswerResult != 0) {
            ImageView imageView = new ImageView(this.mContextThemeWrapper);
            if (nAnswerResult == 2) {
                imageView.setImageResource(R.drawable.ic_resource_correct);
            } else if (nAnswerResult == 1) {
                imageView.setImageResource(R.drawable.ic_resource_halfcorrect);
            } else if (nAnswerResult == -1) {
                imageView.setImageResource(R.drawable.ic_resource_wrong);
            }
            layout.addView(imageView, 0);
            LayoutParams param = (LayoutParams) imageView.getLayoutParams();
            int dpToPixel = Utilities.dpToPixel(22, this.mContextThemeWrapper);
            param.height = dpToPixel;
            param.width = dpToPixel;
        }
    }

    public boolean isCancelled() {
        return this.mbCancel;
    }

    private void buildUI() {
        try {
            JSONArray jsonArray = this.mJsonData.getJSONArray("category");
            int nFullScore = 0;
            int nAnswerScore = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject oneCategory = jsonArray.getJSONObject(i);
                String szTitle = oneCategory.getString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                View textView = new TextView(this.mContextThemeWrapper);
                textView.setTextSize(20.0f);
                textView.setTextColor(-16777216);
                textView.setText(szTitle);
                this.mContentLayout.addView(textView);
                JSONArray arrQuestions = oneCategory.getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    String szAnswerGUID = null;
                    if (oneQuestion.has("answerguid")) {
                        szAnswerGUID = oneQuestion.getString("answerguid");
                    }
                    textView = new LinearLayout(this.mContextThemeWrapper);
                    textView.setOrientation(0);
                    this.mContentLayout.addView(textView);
                    ViewGroup.LayoutParams param = (LayoutParams) textView.getLayoutParams();
                    param.leftMargin = Utilities.dpToPixel(5, getContext());
                    param.topMargin = Utilities.dpToPixel(5, getContext());
                    textView.setLayoutParams(param);
                    textView = new TextView(this.mContextThemeWrapper);
                    textView.setText(oneQuestion.getString("index"));
                    textView.setTextSize(16.0f);
                    textView.setTextColor(-16777216);
                    textView.addView(textView);
                    int nQuestionType = Integer.valueOf(oneQuestion.getString("type")).intValue();
                    CustomSelectorView SelectorView = null;
                    int nAnswerResult;
                    int i2;
                    int i3;
                    if (nQuestionType == 0) {
                        SelectorView = new CustomSelectorView(this.mContextThemeWrapper);
                        SelectorView.setSize(Utilities.dpToPixel(30, getContext()), Utilities.dpToPixel(30, getContext()));
                        SelectorView.setTextSize(ANSWER_SELECTOR_TEXT_SIZE);
                        SelectorView.addOptions("对");
                        SelectorView.addOptions("错");
                        SelectorView.setMultiableSelect(false);
                        textView.addView(SelectorView);
                        if (oneQuestion.has("guid")) {
                            SelectorView.setTag(oneQuestion.get("guid"));
                            this.mMapQuestionToView.put(oneQuestion.getString("guid"), SelectorView);
                            nAnswerResult = mDataBase.GetQuestionAnswerResult(this.mScheduleGUID, oneQuestion.getString("guid"));
                            if (nAnswerResult != 0) {
                                this.mbLocked = true;
                                if (nAnswerResult != 2) {
                                    if (oneQuestion.has("correctanswer")) {
                                        SelectorView.putCorrectValue(oneQuestion.getString("correctanswer"));
                                    }
                                }
                                nAnswerScore = (int) mDataBase.GetQuestionAnswerScore(this.mScheduleGUID, oneQuestion.getString("guid"));
                                nFullScore += nAnswerScore;
                            }
                            i2 = nAnswerResult;
                            i3 = nAnswerScore;
                            addCorrectResultIcon(i2, i3, Utilities.toInt(oneQuestion.getString("score")), textView);
                        }
                    } else if (nQuestionType == 1) {
                        SelectorView = new CustomSelectorView(this.mContextThemeWrapper);
                        szOptions = oneQuestion.getString("options");
                        SelectorView.setSize(Utilities.dpToPixel(30, getContext()), Utilities.dpToPixel(30, getContext()));
                        SelectorView.setTextSize(ANSWER_SELECTOR_TEXT_SIZE);
                        for (k = 0; k < szOptions.length(); k++) {
                            SelectorView.addOptions(szOptions.substring(k, k + 1));
                        }
                        SelectorView.setMultiableSelect(false);
                        textView.addView(SelectorView);
                        if (oneQuestion.has("guid")) {
                            SelectorView.setTag(oneQuestion.get("guid"));
                            this.mMapQuestionToView.put(oneQuestion.getString("guid"), SelectorView);
                            nAnswerResult = mDataBase.GetQuestionAnswerResult(this.mScheduleGUID, oneQuestion.getString("guid"));
                            if (nAnswerResult != 0) {
                                this.mbLocked = true;
                                if (nAnswerResult != 2) {
                                    if (oneQuestion.has("correctanswer")) {
                                        SelectorView.putCorrectValue(oneQuestion.getString("correctanswer"));
                                    }
                                }
                                nAnswerScore = (int) mDataBase.GetQuestionAnswerScore(this.mScheduleGUID, oneQuestion.getString("guid"));
                                nFullScore += nAnswerScore;
                            }
                            i2 = nAnswerResult;
                            i3 = nAnswerScore;
                            addCorrectResultIcon(i2, i3, Utilities.toInt(oneQuestion.getString("score")), textView);
                        }
                    } else if (nQuestionType == 2) {
                        SelectorView = new CustomSelectorView(this.mContextThemeWrapper);
                        szOptions = oneQuestion.getString("options");
                        SelectorView.setSize(Utilities.dpToPixel(30, getContext()), Utilities.dpToPixel(30, getContext()));
                        SelectorView.setTextSize(ANSWER_SELECTOR_TEXT_SIZE);
                        for (k = 0; k < szOptions.length(); k++) {
                            SelectorView.addOptions(szOptions.substring(k, k + 1));
                        }
                        SelectorView.setMultiableSelect(true);
                        textView.addView(SelectorView);
                        if (oneQuestion.has("guid")) {
                            SelectorView.setTag(oneQuestion.get("guid"));
                            this.mMapQuestionToView.put(oneQuestion.getString("guid"), SelectorView);
                            nAnswerResult = mDataBase.GetQuestionAnswerResult(this.mScheduleGUID, oneQuestion.getString("guid"));
                            if (nAnswerResult != 0) {
                                this.mbLocked = true;
                                if (nAnswerResult != 2) {
                                    if (oneQuestion.has("correctanswer")) {
                                        SelectorView.putCorrectValue(oneQuestion.getString("correctanswer"));
                                    }
                                }
                                nAnswerScore = (int) mDataBase.GetQuestionAnswerScore(this.mScheduleGUID, oneQuestion.getString("guid"));
                                nFullScore += nAnswerScore;
                            }
                            i2 = nAnswerResult;
                            i3 = nAnswerScore;
                            addCorrectResultIcon(i2, i3, Utilities.toInt(oneQuestion.getString("score")), textView);
                        }
                    } else if (nQuestionType == 3 || nQuestionType == 4) {
                        textView = new LinearLayout(this.mContextThemeWrapper);
                        textView.setOrientation(1);
                        textView.addView(textView, new FrameLayout.LayoutParams(-1, -2));
                        ((LayoutParams) textView.getLayoutParams()).leftMargin = Utilities.dpToPixel(8, getContext());
                        textView = new Button(this.mContextThemeWrapper);
                        textView.setBackgroundResource(R.drawable.button_selector);
                        textView.setText("显示/隐藏作答区域");
                        textView.addView(textView, new FrameLayout.LayoutParams(Utilities.dpToPixel(220, getContext()), -2));
                        ((LayoutParams) textView.getLayoutParams()).height = Utilities.dpToPixel(30, getContext());
                        textView.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                Button button = (Button) v;
                                LinearLayout linearLayout = (LinearLayout) button.getParent();
                                button.setSelected(!button.isSelected());
                                if (button.isSelected()) {
                                    linearLayout.getChildAt(1).setVisibility(0);
                                    linearLayout.getChildAt(2).setVisibility(0);
                                    return;
                                }
                                linearLayout.getChildAt(1).setVisibility(8);
                                linearLayout.getChildAt(2).setVisibility(8);
                            }
                        });
                        AnswerModeAdapter adapter = new AnswerModeAdapter(this.mContextThemeWrapper);
                        textView = new CustomViewPager(this.mContextThemeWrapper);
                        adapter.setGUID(szAnswerGUID);
                        if (oneQuestion.has("answer0")) {
                            adapter.setData(0, oneQuestion.getString("answer0"));
                        }
                        if (oneQuestion.has("answer1")) {
                            adapter.setData(1, oneQuestion.getString("answer1"));
                        }
                        if (oneQuestion.has("answer1source")) {
                            adapter.setData(1, oneQuestion.getString("answer1source"));
                        }
                        if (oneQuestion.has("answer2")) {
                            adapter.setData(2, oneQuestion.getString("answer2"));
                        }
                        textView.setOffscreenPageLimit(3);
                        textView.setAdapter(adapter);
                        textView.setVisibility(8);
                        textView.addView(textView, new FrameLayout.LayoutParams(-1, 600));
                        CircleIndicator defaultIndicator = new CircleIndicator(this.mContextThemeWrapper);
                        defaultIndicator.configureIndicator(-1, -1, -1, R.animator.scale_with_alpha, 0, R.drawable.black_radius, R.drawable.black_radius);
                        defaultIndicator.setViewPager(textView);
                        defaultIndicator.setVisibility(8);
                        textView.addView(defaultIndicator, new FrameLayout.LayoutParams(-1, 30));
                        if (oneQuestion.has("guid")) {
                            textView.setTag(oneQuestion.get("guid"));
                            this.mMapQuestionToView.put(oneQuestion.getString("guid"), textView);
                            nAnswerResult = mDataBase.GetQuestionAnswerResult(this.mScheduleGUID, oneQuestion.getString("guid"));
                            if (nAnswerResult != 0) {
                                this.mbLocked = true;
                                nAnswerScore = (int) mDataBase.GetQuestionAnswerScore(this.mScheduleGUID, oneQuestion.getString("guid"));
                                nFullScore += nAnswerScore;
                            }
                            i2 = nAnswerResult;
                            i3 = nAnswerScore;
                            addCorrectResultIcon(i2, i3, Utilities.toInt(oneQuestion.getString("score")), textView);
                        }
                    }
                    if (SelectorView != null) {
                        if (oneQuestion.has("answer")) {
                            SelectorView.putValue(oneQuestion.getString("answer"));
                        }
                        SelectorView.setLocked(this.mbLocked);
                    }
                }
            }
            if (this.mbLocked) {
                this.mTextViewInfo.setText("您的成绩为：" + String.valueOf(nFullScore) + "分");
                this.mSubmitButton.setVisibility(4);
                this.mCancelButton.setText("关闭");
                return;
            }
            this.mStartTime = new Date();
            this.mHandler.post(this.mUpdateTimerRunnable);
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
                this.mDataSynchronizedGUID = "AnswerSheet_" + this.mGUID;
                DataSynchronizeItemObject DataObject = new DataSynchronizeItemObject(this.mDataSynchronizedGUID, null);
                DataObject.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        if (ItemObject.readTextData() != null) {
                            AnswerSheetComponent.this.loadJsonData(ItemObject.readTextData());
                        } else {
                            AnswerSheetComponent.this.loadOriginalTemplate();
                        }
                    }
                });
                DataObject.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        AnswerSheetComponent.this.loadOriginalTemplate();
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
                this.mTextViewMessage.setText("数据加载错误，错误信息：" + e.getMessage());
                this.mTextViewMessage.setVisibility(0);
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
                AnswerSheetComponent.this.loadJsonData(ItemObject.readTextData());
            }
        });
        PrivateDataObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                AnswerSheetComponent.this.mTextViewMessage.setText("获取数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                AnswerSheetComponent.this.mTextViewMessage.setVisibility(0);
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
        try {
            JSONArray jsonArray = this.mJsonData.getJSONArray("category");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    String szQuestionGUID = oneQuestion.getString("guid");
                    boolean bTrulyAnswered = false;
                    View view = null;
                    String szGUID = mDataBase.CheckItemExsit(this.mScheduleGUID, szQuestionGUID);
                    if (szGUID == null || szGUID.isEmpty()) {
                        szGUID = Utilities.createGUID();
                    }
                    oneQuestion.put("answerguid", szGUID);
                    if (this.mMapQuestionToView.containsKey(szQuestionGUID)) {
                        view = (View) this.mMapQuestionToView.get(szQuestionGUID);
                        if (view instanceof CustomSelectorView) {
                            if (!((CustomSelectorView) view).getValue().isEmpty()) {
                                bTrulyAnswered = true;
                            }
                        } else if (view instanceof CustomViewPager) {
                            AnswerModeAdapter adapter = (AnswerModeAdapter) ((CustomViewPager) view).getAdapter();
                            if (adapter.isExpanded() && !(adapter.getData(0).isEmpty() && adapter.getData(1).isEmpty() && adapter.getData(2).isEmpty())) {
                                bTrulyAnswered = true;
                            }
                        }
                    }
                    if (bTrulyAnswered) {
                        mDataBase.SetItemRead(this.mScheduleGUID, szQuestionGUID, szGUID, this.mUserClassGUID, this.mUserClassName, 0, 2);
                        DataObject = new DataSynchronizeItemObject(szGUID, null);
                        DataObject.setSuccessListener(new OnSuccessListener() {
                            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            }
                        });
                        DataObject.writeTextData(getControlXML(view, szQuestionGUID));
                        DataObject.setReadOperation(false);
                        DataObject.setNoDeleteOnFinish(true);
                        DataObject.setClientID(this.mClientID);
                        DataObject.setAlwaysActiveCallbacks(true);
                        VirtualNetworkObject.addToQueue(DataObject);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataObject = new DataSynchronizeItemObject(this.mDataSynchronizedGUID, null);
        DataObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (AnswerSheetComponent.this.mAnswerSheetCallBack != null) {
                    AnswerSheetComponent.this.mAnswerSheetCallBack.onSubmit();
                }
                if (AnswerSheetComponent.mGlobalCallBack != null) {
                    AnswerSheetComponent.mGlobalCallBack.onSubmit();
                }
            }
        });
        DataObject.writeTextData(getData());
        DataObject.setReadOperation(false);
        DataObject.setNoDeleteOnFinish(true);
        DataObject.setClientID(this.mClientID);
        DataObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(DataObject);
    }

    private String getControlXML(View view, String szResourceGUID) {
        try {
            boolean bHasData = false;
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element mRootElement = document.createElement("LessonsScheduleUserAnswer");
            document.setXmlVersion(Properties.REST_VERSION);
            document.appendChild(mRootElement);
            mRootElement.setAttribute(ClientCookie.VERSION_ATTR, Properties.REST_VERSION);
            mRootElement.setAttribute("resourceGUID", szResourceGUID);
            CustomSelectorView TempView;
            Element OneElement;
            if (view instanceof CustomSelectorView) {
                TempView = (CustomSelectorView) view;
                LayoutParams LayoutParam = (LayoutParams) TempView.getLayoutParams();
                OneElement = document.createElement("SelectorView");
                OneElement.setAttribute("height", String.valueOf(LayoutParam.height));
                OneElement.setTextContent(TempView.getValue());
                mRootElement.appendChild(OneElement);
                if (!OneElement.getTextContent().isEmpty()) {
                    bHasData = true;
                }
            } else if (view instanceof ViewPager) {
                ViewPager ViewPager = (ViewPager) view;
                for (int i = 0; i < ViewPager.getChildCount(); i++) {
                    View OneView = ViewPager.getChildAt(i);
                    ViewPager.LayoutParams LayoutParam2 = (ViewPager.LayoutParams) OneView.getLayoutParams();
                    if (OneView instanceof TextComponent) {
                        TextComponent TempView2 = (TextComponent) OneView;
                        OneElement = document.createElement("TextView");
                        OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                        OneElement.setAttribute("width", String.valueOf(LayoutParam2.width));
                        OneElement.setAttribute("top", "0");
                        OneElement.setAttribute("left", "0");
                        OneElement.setTextContent(TempView2.getData());
                        mRootElement.appendChild(OneElement);
                        if (!OneElement.getTextContent().isEmpty()) {
                            bHasData = true;
                        }
                    } else if (OneView instanceof DrawComponent) {
                        DrawComponent TempView3 = (DrawComponent) OneView;
                        OneElement = document.createElement("DrawView");
                        OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                        OneElement.setAttribute("width", String.valueOf(LayoutParam2.width));
                        OneElement.setAttribute("top", "0");
                        OneElement.setAttribute("left", "0");
                        OneElement.setTextContent(TempView3.getData());
                        mRootElement.appendChild(OneElement);
                        if (!OneElement.getTextContent().isEmpty()) {
                            bHasData = true;
                        }
                    } else if (OneView instanceof CustomSelectorView) {
                        TempView = (CustomSelectorView) OneView;
                        OneElement = document.createElement("SelectorView");
                        OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                        OneElement.setTextContent(TempView.getValue());
                        mRootElement.appendChild(OneElement);
                        if (!OneElement.getTextContent().isEmpty()) {
                            bHasData = true;
                        }
                    } else if (OneView instanceof CameraComponent) {
                        CameraComponent TempView4 = (CameraComponent) OneView;
                        OneElement = document.createElement("CameraView");
                        String szFileName = TempView4.getData();
                        OneElement.setAttribute("height", String.valueOf(LayoutParam2.height));
                        OneElement.setAttribute("width", String.valueOf(LayoutParam2.width));
                        OneElement.setAttribute("top", "0");
                        OneElement.setAttribute("left", "0");
                        if (!(szFileName == null || szFileName.isEmpty())) {
                            OneElement.setAttribute("key", szFileName);
                            bHasData = true;
                        }
                        mRootElement.appendChild(OneElement);
                    }
                }
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

    public String getData() {
        try {
            JSONArray jsonArray = this.mJsonData.getJSONArray("category");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray arrQuestions = jsonArray.getJSONObject(i).getJSONArray("questions");
                for (int j = 0; j < arrQuestions.length(); j++) {
                    JSONObject oneQuestion = arrQuestions.getJSONObject(j);
                    if (oneQuestion.has("guid")) {
                        String szGUID = oneQuestion.getString("guid");
                        if (this.mMapQuestionToView.containsKey(szGUID)) {
                            View view = (View) this.mMapQuestionToView.get(szGUID);
                            if (view instanceof CustomSelectorView) {
                                oneQuestion.put("answer", ((CustomSelectorView) view).getValue());
                            } else if (view instanceof CustomViewPager) {
                                AnswerModeAdapter adapter = (AnswerModeAdapter) ((CustomViewPager) view).getAdapter();
                                oneQuestion.put("answer0", adapter.getData(0));
                                oneQuestion.put("answer1", adapter.getData(1));
                                oneQuestion.put("answer2", adapter.getData(2));
                                if (adapter.isExpanded()) {
                                    oneQuestion.put("answer1preview", adapter.saveDrawView());
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.mJsonData.toString();
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
        this.mbLocked = bLock;
    }

    protected void onDetachedFromWindow() {
        this.mVirtualNetworkObjectManager.cancelAll();
        mDataBase = null;
        super.onDetachedFromWindow();
    }
}
