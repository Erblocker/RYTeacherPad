package com.netspace.library.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.adapter.StudentAnswerImageAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.ClassMultiQuestions;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;

public class StudentAnswerImageService extends Service {
    public static final String ANSWERGROUP = "answergroup";
    private static final int HIDE_WINDOW = 300;
    public static final String LISTURL = "url";
    public static final String OPERATION = "operation";
    public static final int OPERATION_HIDE = 101;
    public static final int OPERATION_LOCK = 102;
    public static final int OPERATION_SHOW = 100;
    public static final int OPERATION_UNLOCK = 103;
    public static final String SHOWEXPANDED = "showExpanded";
    private static final int SHOW_WINDOW = 200;
    public static final String TAG = "StudentAnswerImageService";
    private static final int UPDATE_WINDOW = 400;
    private static StudentAnswerImageService mInstance;
    private static OnClickListener mOnClickListener;
    private StudentAnswerImageAdapter mAdapter;
    private boolean mAdded = false;
    private View mButtonsView;
    private Context mContext;
    private ContextThemeWrapper mContextThemeWrapper;
    private StudentAnswer mCurrentStudentAnswer;
    private OnClickListener mDefaultOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            StudentAnswer oneAnswer = (StudentAnswer) v.getTag();
            if (UI.getCurrentActivity() != null) {
                StudentAnswerImageService.this.mCurrentStudentAnswer = oneAnswer;
                if (oneAnswer.bIsHandWrite) {
                    Picasso.with(StudentAnswerImageService.this).load(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + StudentAnswerImageService.this.mCurrentStudentAnswer.szAnswerOrPictureKey).into(StudentAnswerImageService.this.mImageClickTarget);
                }
                StudentAnswerImageService.this.mAdapter.setCurrentSelectedItem(oneAnswer.szStudentJID);
                StudentAnswerImageService.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (!StudentAnswerImageService.this.mAdded) {
                        StudentAnswerImageService.this.mWindowManager.addView(StudentAnswerImageService.this.mButtonsView, StudentAnswerImageService.this.mParams);
                        StudentAnswerImageService.this.mAdded = true;
                        return;
                    }
                    return;
                case 300:
                    if (StudentAnswerImageService.this.mAdded) {
                        StudentAnswerImageService.this.mWindowManager.removeView(StudentAnswerImageService.this.mButtonsView);
                        StudentAnswerImageService.this.mAdded = false;
                        return;
                    }
                    return;
                case 400:
                    if (StudentAnswerImageService.this.mAdded) {
                        StudentAnswerImageService.this.showHideButtons(StudentAnswerImageService.this.mbAllShow);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private Target mImageClickTarget = new Target() {
        public void onBitmapFailed(Drawable arg0) {
            Utilities.showAlertMessage(null, "获取用户作答图片失败", "获取图片数据失败。");
        }

        public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
            if (Utilities.saveBitmapToJpeg(new StringBuilder(String.valueOf(StudentAnswerImageService.this.getExternalCacheDir().getAbsolutePath())).append("/").append(StudentAnswerImageService.this.mCurrentStudentAnswer.szAnswerOrPictureKey).append(".jpg").toString(), arg0)) {
                Activity activity = UI.getCurrentActivity();
                if (activity == null || !(activity instanceof FingerDrawActivity)) {
                    FingerDrawActivity.SetCallbackInterface(null);
                    Intent DrawActivity = new Intent(activity, FingerDrawActivity.class);
                    DrawActivity.putExtra("imageKey", StudentAnswerImageService.this.mCurrentStudentAnswer.szAnswerOrPictureKey);
                    DrawActivity.putExtra("imageWidth", -1);
                    DrawActivity.putExtra("imageHeight", -1);
                    DrawActivity.putExtra("allowProject", false);
                    DrawActivity.putExtra("allowBroadcast", false);
                    DrawActivity.putExtra("allowUpload", false);
                    DrawActivity.putExtra("allowvote", true);
                    DrawActivity.putExtra("displayText", StudentAnswerImageService.this.mCurrentStudentAnswer.szStudentName);
                    if (activity == null) {
                        DrawActivity.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                        StudentAnswerImageService.this.startActivity(DrawActivity);
                        return;
                    }
                    activity.startActivity(DrawActivity);
                    return;
                }
                ((FingerDrawActivity) activity).switchImage(StudentAnswerImageService.this.mCurrentStudentAnswer.szAnswerOrPictureKey, StudentAnswerImageService.this.mCurrentStudentAnswer.szStudentName);
                return;
            }
            Utilities.showAlertMessage(null, "保存用户作答图片失败", "保存用户作答图片数据到本地失败。");
        }

        public void onPrepareLoad(Drawable arg0) {
        }
    };
    private ImageView mImageView;
    private LayoutInflater mInflater;
    private boolean mItemMove = false;
    private ImageButton mMoreButton;
    private LayoutParams mParams;
    private RecyclerView mRecycleView;
    private RelativeLayout mRelativeLayout;
    private Runnable mUpdateStudentAnswerImageRunnable = new Runnable() {
        public void run() {
            if (ClassMultiQuestions.getInstance() != null) {
                ClassMultiQuestions.getInstance().refreshCurrentQuestionByReloadingURL();
            }
            StudentAnswerImageService.this.mHandler.postDelayed(StudentAnswerImageService.this.mUpdateStudentAnswerImageRunnable, 2000);
        }
    };
    private WindowManager mWindowManager;
    private ArrayList<StudentAnswer> marrStudentAnswers = new ArrayList();
    private boolean mbAllShow = true;
    private boolean mbLocked = false;
    private int mnLastWidth = 0;
    private String mszAnswerGroup;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void setStudentAnswerData(ArrayList<StudentAnswer> arrData) {
        this.marrStudentAnswers.clear();
        this.marrStudentAnswers.addAll(arrData);
        for (int i = 0; i < arrData.size(); i++) {
            StudentAnswer oneAnswer = (StudentAnswer) arrData.get(i);
            if (oneAnswer.szStudentID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                this.marrStudentAnswers.remove(i);
                this.marrStudentAnswers.add(0, oneAnswer);
                FingerDrawActivity.SetSkipVoteKey(oneAnswer.szAnswerOrPictureKey);
                break;
            }
        }
        this.mAdapter.notifyDataSetChanged();
    }

    public void setJsonData(String szJsonData) {
        try {
            JSONArray jsonArray = new JSONArray(szJsonData);
            this.marrStudentAnswers.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                StudentAnswer oneAnswer = new StudentAnswer();
                JSONObject oneObject = jsonArray.getJSONObject(i);
                oneAnswer.szStudentName = oneObject.getString("studentName");
                oneAnswer.szStudentID = oneObject.getString("studentID");
                oneAnswer.szStudentJID = oneObject.getString("studentJID");
                oneAnswer.szAnswerOrPictureKey = oneObject.getString("answer");
                oneAnswer.bAutoSubmit = oneObject.getBoolean("isAutosubmit");
                oneAnswer.nTimeInMS = oneObject.getInt("timeInMS");
                oneAnswer.bCorrect = oneObject.getBoolean("isCorrect");
                oneAnswer.bIsHandWrite = oneObject.getBoolean("isHandWrite");
                if (oneAnswer.szStudentID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                    FingerDrawActivity.SetSkipVoteKey(oneAnswer.szAnswerOrPictureKey);
                    this.marrStudentAnswers.add(0, oneAnswer);
                } else {
                    this.marrStudentAnswers.add(oneAnswer);
                }
            }
            this.mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        this.mHandler.removeCallbacks(this.mUpdateStudentAnswerImageRunnable);
        this.mHandler.sendEmptyMessage(300);
        mInstance = null;
        super.onDestroy();
    }

    public static boolean isActive() {
        if (mInstance != null) {
            return true;
        }
        return false;
    }

    public static void setOnItemClickListener(OnClickListener OnClickListener) {
        mOnClickListener = OnClickListener;
        if (mInstance != null) {
            mInstance.mAdapter.setOnClickListener(mOnClickListener);
        }
    }

    public static StudentAnswerImageService getInstance() {
        return mInstance;
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("operation")) {
            mInstance = this;
            this.mszAnswerGroup = intent.getStringExtra(ANSWERGROUP);
            if (intent.getExtras().getBoolean(SHOWEXPANDED, true)) {
                this.mbAllShow = true;
            } else {
                this.mbAllShow = false;
            }
            createFloatView();
            switch (intent.getIntExtra("operation", 100)) {
                case 100:
                    this.mHandler.sendEmptyMessage(200);
                    return;
                case 101:
                    this.mHandler.sendEmptyMessage(300);
                    return;
                case 102:
                    this.mbLocked = true;
                    this.mbAllShow = false;
                    this.mHandler.sendEmptyMessage(400);
                    return;
                case 103:
                    this.mbLocked = false;
                    return;
                default:
                    return;
            }
        }
    }

    public void reload() {
    }

    public void setLock(boolean bLock) {
        this.mbLocked = bLock;
        if (this.mbLocked) {
            this.mbAllShow = false;
            showHideButtons(this.mbAllShow);
        }
    }

    public void hide() {
        this.mbAllShow = false;
        showHideButtons(this.mbAllShow);
    }

    public void show() {
        this.mbAllShow = true;
        showHideButtons(this.mbAllShow);
    }

    private void createFloatView() {
        if (!this.mAdded) {
            this.mContext = getApplicationContext();
            this.mContextThemeWrapper = new ContextThemeWrapper(this, R.style.ComponentTheme);
            this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            this.mButtonsView = this.mInflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.layout_studentanswerimage, null);
            this.mImageView = (ImageView) this.mButtonsView.findViewById(R.id.imageHandler);
            this.mMoreButton = (ImageButton) this.mButtonsView.findViewById(R.id.imageButtonSelectQuestion);
            this.mMoreButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).colorRes(17170443).actionBarSize());
            this.mMoreButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Activity activity = UI.getCurrentActivity();
                    if (activity != null) {
                        ClassMultiQuestions.getInstance().showQuestionSelector(activity);
                    }
                }
            });
            this.mRelativeLayout = (RelativeLayout) this.mButtonsView.findViewById(R.id.relative1);
            this.mRecycleView = (RecyclerView) this.mButtonsView.findViewById(R.id.studentAnswerView);
            LinearLayoutManager Manager = new LinearLayoutManager(this);
            Manager.setOrientation(0);
            this.mRecycleView.setLayoutManager(Manager);
            this.mRecycleView.setItemAnimator(new DefaultItemAnimator());
            this.mAdapter = new StudentAnswerImageAdapter(this, this.marrStudentAnswers);
            if (mOnClickListener != null) {
                this.mAdapter.setOnClickListener(mOnClickListener);
            } else {
                this.mAdapter.setOnClickListener(this.mDefaultOnClickListener);
            }
            this.mAdapter.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    StudentAnswer data = (StudentAnswer) v.getTag();
                    if (v.isSelected()) {
                        v.setSelected(false);
                        StudentAnswerImageService.this.mAdapter.setItemSelected(data.szStudentJID, false);
                    } else {
                        v.setSelected(true);
                        StudentAnswerImageService.this.mAdapter.setItemSelected(data.szStudentJID, true);
                    }
                    return false;
                }
            });
            this.mRecycleView.setAdapter(this.mAdapter);
            this.mWindowManager = (WindowManager) getApplicationContext().getSystemService("window");
            this.mParams = new LayoutParams();
            this.mParams.type = 2003;
            this.mParams.format = 1;
            this.mParams.flags = 40;
            this.mParams.height = -2;
            this.mWindowManager.addView(this.mButtonsView, this.mParams);
            this.mAdded = true;
            Display display = this.mWindowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            this.mnLastWidth = metrics.widthPixels - Utilities.dpToPixel((int) SoapEnvelope.VER12, (Context) this);
            this.mParams.x = (-metrics.widthPixels) / 2;
            this.mParams.y = (metrics.heightPixels / 2) - Utilities.dpToPixel(80, (Context) this);
            this.mParams.width = this.mnLastWidth;
            this.mImageView.setImageResource(R.drawable.ic_sideclose);
            this.mWindowManager.updateViewLayout(this.mButtonsView, this.mParams);
            showHideButtons(this.mbAllShow);
            this.mButtonsView.setOnTouchListener(new OnTouchListener() {
                int lastX;
                int lastY;
                int paramX;
                int paramY;

                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case 0:
                            this.lastX = (int) event.getRawX();
                            this.lastY = (int) event.getRawY();
                            this.paramX = StudentAnswerImageService.this.mParams.x;
                            this.paramY = StudentAnswerImageService.this.mParams.y;
                            StudentAnswerImageService.this.mItemMove = false;
                            break;
                        case 1:
                            if (StudentAnswerImageService.this.mItemMove) {
                                StudentAnswerImageService.this.mItemMove = false;
                                return true;
                            }
                            break;
                        case 2:
                            int dy = ((int) event.getRawY()) - this.lastY;
                            boolean bNeedUpdate = false;
                            if (Math.abs(((int) event.getRawX()) - this.lastX) > 10) {
                                StudentAnswerImageService.this.mParams.width = Math.max((int) event.getRawX(), 200);
                                StudentAnswerImageService.this.mItemMove = true;
                                bNeedUpdate = true;
                                Log.d(StudentAnswerImageService.TAG, "width=" + StudentAnswerImageService.this.mParams.width);
                            } else if (Math.abs(dy) > 10) {
                                StudentAnswerImageService.this.mParams.y = this.paramY + dy;
                                bNeedUpdate = true;
                                StudentAnswerImageService.this.mItemMove = true;
                            }
                            if (bNeedUpdate) {
                                StudentAnswerImageService.this.mWindowManager.updateViewLayout(StudentAnswerImageService.this.mButtonsView, StudentAnswerImageService.this.mParams);
                                break;
                            }
                            break;
                    }
                    return false;
                }
            });
            this.mButtonsView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Log.d(StudentAnswerImageService.TAG, "onClick");
                    if (!StudentAnswerImageService.this.mbLocked && !StudentAnswerImageService.this.mItemMove) {
                        StudentAnswerImageService.this.mbAllShow = !StudentAnswerImageService.this.mbAllShow;
                        StudentAnswerImageService.this.showHideButtons(StudentAnswerImageService.this.mbAllShow);
                        if (StudentAnswerImageService.this.mbAllShow) {
                            UserInfo.UserScore("ShowStudentAnswers");
                        } else {
                            UserInfo.UserScore("HideStudentAnswers");
                        }
                    }
                }
            });
        }
    }

    private void showHideButtons(boolean bAllShow) {
        if (bAllShow) {
            this.mRelativeLayout.setVisibility(0);
            this.mRecycleView.setVisibility(0);
            this.mMoreButton.setVisibility(0);
            this.mImageView.setImageResource(R.drawable.ic_sideclose);
            this.mParams.width = this.mnLastWidth;
            this.mHandler.postDelayed(this.mUpdateStudentAnswerImageRunnable, 2000);
        } else {
            this.mRecycleView.setVisibility(4);
            this.mRelativeLayout.setVisibility(8);
            this.mMoreButton.setVisibility(8);
            this.mImageView.setImageResource(R.drawable.ic_sideopen);
            this.mParams.x = (-Utilities.getScreenWidth((Context) this)) / 2;
            this.mnLastWidth = this.mParams.width;
            this.mParams.width = Utilities.dpToPixel(30, (Context) this);
            this.mHandler.removeCallbacks(this.mUpdateStudentAnswerImageRunnable);
        }
        this.mWindowManager.updateViewLayout(this.mButtonsView, this.mParams);
    }
}
