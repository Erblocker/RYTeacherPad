package com.netspace.library.activity.plugins;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.AnswerSheetV3OtherQuestionCorrectActivity.AnswerSheetV3OtherQuestion;
import com.netspace.library.activity.FingerDrawActivity2;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.controls.MoveableObject;
import com.netspace.library.controls.plugin.DrawViewTextPlugin;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import java.util.Date;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

public class ActivityPlugin_AnswerSheetOtherQuestions extends ActivityPluginBase implements OnClickListener, DrawViewActionInterface {
    private static JSONObject mAnswerSheetJsonData;
    private static ArrayList<AnswerSheetV3OtherQuestion> marrData;
    private static int mnCurrentQuestionID = 0;
    private ImageView mButtonLayers;
    private AnswerSheetV3OtherQuestion mCurrentQuestion;
    private DrawView mDrawView;
    private DrawViewTextPlugin mDrawViewTextPlugin;
    private View mFrameView;
    private Target mImageDownloadTarget = new Target() {
        public void onBitmapFailed(Drawable arg0) {
            Utilities.showAlertMessage(UI.getCurrentActivity(), "获取图片数据失败", "获取图片数据失败。");
        }

        public void onBitmapLoaded(Bitmap Bitmap, LoadedFrom arg1) {
            ActivityPlugin_AnswerSheetOtherQuestions.this.mDrawView.setBackgroundBitmap(Bitmap);
            if (ActivityPlugin_AnswerSheetOtherQuestions.this.mActivity instanceof FingerDrawActivity2) {
                ActivityPlugin_AnswerSheetOtherQuestions.this.mActivity.setImageFullSize(Bitmap.getWidth(), Bitmap.getHeight());
            }
        }

        public void onPrepareLoad(Drawable arg0) {
        }
    };
    private ImageView mImageViewCorrect;
    private ImageView mImageViewHalfCorrect;
    private ImageView mImageViewNext;
    private ImageView mImageViewPrev;
    private ImageView mImageViewWrong;
    private LinearLayout mLayoutMinusScore;
    private LinearLayout mLayoutPlusScore;
    private View mRootView;
    private float mScore = 0.0f;
    private HorizontalScrollView mScrollViewMinusScore;
    private HorizontalScrollView mScrollViewPlusScore;
    private boolean mbChanged = false;
    private Boolean mbEnableCameraAnswer = Boolean.valueOf(true);
    private Boolean mbEnableDrawAnswer = Boolean.valueOf(true);
    private Boolean mbEnableTextAnswer = Boolean.valueOf(true);
    private boolean mbUserDrawDisplayed = false;
    private float mfFullScore = 0.0f;
    private int mnDrawViewOldDataPointIndex = 0;
    private int mnIndex = 0;
    private String mszUrlBase;

    public static class AnswerSheetCorrectImageChange {
    }

    public ActivityPlugin_AnswerSheetOtherQuestions(Activity activity, RelativeLayout relativeLayout, TextView textView, DrawView drawView, ViewGroup tools) {
        super(activity);
        this.mDrawView = drawView;
        this.mDrawViewTextPlugin = new DrawViewTextPlugin();
        this.mDrawView.addPlugin(this.mDrawViewTextPlugin);
        this.mDrawView.setCallback(this);
        addButtonWithTooltip(R.id.buttonLayers, "选择学生的作答层", tools);
        this.mButtonLayers = (ImageView) tools.findViewById(R.id.buttonLayers);
        this.mButtonLayers.setImageDrawable(new IconDrawable((Context) activity, FontAwesomeIcons.fa_clone).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mButtonLayers.setOnClickListener(this);
        this.mRootView = ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate(R.layout.layout_activityplugin_answersheetotherquestions, null);
        activity.addContentView(this.mRootView, new LayoutParams(-1, -1));
        this.mFrameView = this.mRootView.findViewById(R.id.frame3);
        this.mImageViewPrev = (ImageView) this.mRootView.findViewById(R.id.imageViewPrev);
        this.mImageViewPrev.setOnClickListener(this);
        this.mImageViewNext = (ImageView) this.mRootView.findViewById(R.id.imageViewNext);
        this.mImageViewNext.setOnClickListener(this);
        this.mImageViewCorrect = (ImageView) this.mRootView.findViewById(R.id.imageViewCorrect);
        this.mImageViewCorrect.setOnClickListener(this);
        this.mImageViewWrong = (ImageView) this.mRootView.findViewById(R.id.imageViewWrong);
        this.mImageViewWrong.setOnClickListener(this);
        this.mImageViewHalfCorrect = (ImageView) this.mRootView.findViewById(R.id.imageViewHalfCorrect);
        this.mImageViewHalfCorrect.setOnClickListener(this);
        this.mLayoutPlusScore = (LinearLayout) this.mRootView.findViewById(R.id.layoutPlusScore);
        this.mLayoutMinusScore = (LinearLayout) this.mRootView.findViewById(R.id.layoutMinusScore);
        this.mScrollViewPlusScore = (HorizontalScrollView) this.mLayoutPlusScore.getParent();
        this.mScrollViewMinusScore = (HorizontalScrollView) this.mLayoutMinusScore.getParent();
        MoveableObject moveable = new MoveableObject(activity, null, false, true);
        moveable.setUseMoveDelay(false);
        this.mFrameView.setOnTouchListener(moveable);
        if (activity.getIntent() != null && activity.getIntent().getExtras() != null) {
            this.mnIndex = activity.getIntent().getExtras().getInt("id", -1);
            this.mfFullScore = activity.getIntent().getExtras().getFloat("fullscore", 0.0f);
            String szTitle = activity.getIntent().getExtras().getString("title", "");
            setFullScore(this.mfFullScore);
            if (this.mnIndex >= 0 && this.mnIndex < marrData.size()) {
                this.mCurrentQuestion = (AnswerSheetV3OtherQuestion) marrData.get(this.mnIndex);
                this.mszUrlBase = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/DataSynchronizeGetSingleData?clientid=" + this.mCurrentQuestion.studentAnswer.getClientid() + "&packageid=";
                displayStudentAnswers();
                activity.getIntent().putExtra("displayText", new StringBuilder(String.valueOf(szTitle)).append("-").append(this.mCurrentQuestion.studentAnswer.getStudentname()).toString());
                this.mScore = this.mCurrentQuestion.answerResult.getAnswerscore().floatValue();
                if (this.mCurrentQuestion.answerResult.getAnswerresult().intValue() != 0) {
                    updateDisplay();
                }
            }
        }
    }

    private void displayStudentAnswers() {
        if (!this.mbEnableTextAnswer.booleanValue() || this.mCurrentQuestion.studentAnswer.getAnswertext().isEmpty()) {
            this.mDrawViewTextPlugin.setText("");
        } else {
            this.mDrawViewTextPlugin.setText(this.mCurrentQuestion.studentAnswer.getAnswertext());
        }
        if (!this.mbEnableCameraAnswer.booleanValue() || this.mCurrentQuestion.studentAnswer.getAnswercamera().isEmpty()) {
            this.mDrawView.setBackgroundBitmap(null);
        } else {
            Picasso.with(this.mActivity).load(this.mszUrlBase + this.mCurrentQuestion.studentAnswer.getAnswercamera()).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(this.mImageDownloadTarget);
        }
        this.mDrawView.clear();
        if (!this.mbEnableDrawAnswer.booleanValue() || this.mCurrentQuestion.studentAnswer.getAnswerhandwritedata().isEmpty()) {
            this.mbUserDrawDisplayed = false;
        } else {
            this.mDrawView.fromString(this.mCurrentQuestion.studentAnswer.getAnswerhandwritedata());
            if (this.mnDrawViewOldDataPointIndex == 0) {
                this.mnDrawViewOldDataPointIndex = this.mDrawView.getDataPointsCount();
            }
            this.mbUserDrawDisplayed = true;
        }
        if (!(this.mCurrentQuestion.answerResult.getAnswercorrecthandwrite() == null || this.mCurrentQuestion.answerResult.getAnswercorrecthandwrite().isEmpty())) {
            this.mDrawView.fromString(this.mCurrentQuestion.answerResult.getAnswercorrecthandwrite());
        }
        this.mDrawView.invalidate();
    }

    public static void setAnswerData(ArrayList<AnswerSheetV3OtherQuestion> arrData) {
        marrData = arrData;
    }

    public void setFullScore(float fFullScore) {
        this.mfFullScore = fFullScore;
        generateScoreList(fFullScore);
    }

    private void generateScoreList(float fFullScore) {
        this.mLayoutPlusScore.removeAllViews();
        this.mLayoutMinusScore.removeAllViews();
        float fScore = fFullScore;
        while (fScore >= 0.0f) {
            TextView textView = getNewScoreTextView();
            textView.setBackgroundResource(R.drawable.background_oval_selected_with_green_background);
            if (fScore == fFullScore) {
                textView.setText("+" + String.valueOf(fFullScore) + "分");
            } else if (fScore == 0.0f) {
                textView.setText("+0分");
            } else {
                textView.setText("+" + String.valueOf(fScore) + "分");
            }
            textView.setTag(Float.valueOf(fScore));
            fScore = (float) (((double) fScore) - 0.5d);
            this.mLayoutPlusScore.addView(textView);
            ((LinearLayout.LayoutParams) textView.getLayoutParams()).leftMargin = (int) Utilities.dpToPixel(5);
        }
        fScore = 0.0f;
        while (fScore <= fFullScore) {
            textView = getNewScoreTextView();
            textView.setBackgroundResource(R.drawable.background_oval_selected_with_red_background);
            if (fScore == fFullScore) {
                textView.setText("-" + String.valueOf(fFullScore) + "分");
            } else if (fScore == 0.0f) {
                textView.setText("-0分");
            } else {
                textView.setText("-" + String.valueOf(fScore) + "分");
            }
            textView.setTag(Float.valueOf(fFullScore - fScore));
            fScore = (float) (((double) fScore) + 0.5d);
            this.mLayoutMinusScore.addView(textView);
            ((LinearLayout.LayoutParams) textView.getLayoutParams()).leftMargin = (int) Utilities.dpToPixel(5);
        }
    }

    private TextView getNewScoreTextView() {
        TextView textView = new TextView(this.mActivity);
        textView.setOnClickListener(this);
        textView.setTextSize(15.0f);
        return textView;
    }

    private boolean gotoNext() {
        if (this.mnIndex == marrData.size() - 1) {
            Utilities.showToastMessage("当前已经是最后一个了", 0);
            return false;
        }
        this.mnIndex++;
        Intent intent = this.mActivity.getIntent();
        intent.putExtra("id", this.mnIndex);
        this.mActivity.overridePendingTransition(R.anim.anim_enter_right, R.anim.anim_leave_left);
        this.mActivity.startActivity(intent);
        return true;
    }

    public void onClick(View v) {
        boolean bJumpToNext = false;
        if (v.getId() == R.id.buttonLayers) {
            final ArrayList<String> arrOptions = new ArrayList();
            ArrayList<Boolean> arrValues = new ArrayList();
            if (!this.mCurrentQuestion.studentAnswer.getAnswertext().isEmpty()) {
                arrOptions.add("文字作答");
                arrValues.add(this.mbEnableTextAnswer);
            }
            if (!this.mCurrentQuestion.studentAnswer.getAnswercamera().isEmpty()) {
                arrOptions.add("拍照作答");
                arrValues.add(this.mbEnableCameraAnswer);
            }
            if (!this.mCurrentQuestion.studentAnswer.getAnswerhandwritedata().isEmpty()) {
                arrOptions.add("手写作答");
                arrValues.add(this.mbEnableDrawAnswer);
            }
            boolean[] arrCheckState = new boolean[arrValues.size()];
            String[] arrNames = (String[]) arrOptions.toArray(new String[0]);
            for (int i = 0; i < arrValues.size(); i++) {
                arrCheckState[i] = ((Boolean) arrValues.get(i)).booleanValue();
            }
            new Builder(new ContextThemeWrapper(this.mActivity, 16974130)).setMultiChoiceItems(arrNames, arrCheckState, new OnMultiChoiceClickListener() {
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (((String) arrOptions.get(which)).contentEquals("文字作答")) {
                        ActivityPlugin_AnswerSheetOtherQuestions.this.mbEnableTextAnswer = Boolean.valueOf(isChecked);
                    } else if (((String) arrOptions.get(which)).contentEquals("拍照作答")) {
                        ActivityPlugin_AnswerSheetOtherQuestions.this.mbEnableCameraAnswer = Boolean.valueOf(isChecked);
                    } else if (((String) arrOptions.get(which)).contentEquals("手写作答")) {
                        ActivityPlugin_AnswerSheetOtherQuestions.this.mbEnableDrawAnswer = Boolean.valueOf(isChecked);
                    }
                }
            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (!ActivityPlugin_AnswerSheetOtherQuestions.this.mbUserDrawDisplayed) {
                        ActivityPlugin_AnswerSheetOtherQuestions.this.mCurrentQuestion.answerResult.setAnswercorrecthandwrite(ActivityPlugin_AnswerSheetOtherQuestions.this.mDrawView.getDataAsString(0));
                    } else if (ActivityPlugin_AnswerSheetOtherQuestions.this.mDrawView.getDataPointsCount() > ActivityPlugin_AnswerSheetOtherQuestions.this.mnDrawViewOldDataPointIndex) {
                        ActivityPlugin_AnswerSheetOtherQuestions.this.mCurrentQuestion.answerResult.setAnswercorrecthandwrite(ActivityPlugin_AnswerSheetOtherQuestions.this.mDrawView.getDataAsString(ActivityPlugin_AnswerSheetOtherQuestions.this.mnDrawViewOldDataPointIndex));
                    }
                    ActivityPlugin_AnswerSheetOtherQuestions.this.displayStudentAnswers();
                }
            }).setTitle("选择显示作答方式").create().show();
            return;
        }
        if (v.getId() == R.id.imageViewPrev) {
            if (this.mnIndex == 0) {
                Utilities.showToastMessage("当前已经是第一个了", 0);
                return;
            }
            this.mnIndex--;
            Intent intent = this.mActivity.getIntent();
            intent.putExtra("id", this.mnIndex);
            this.mActivity.overridePendingTransition(R.anim.anim_enter_left, R.anim.anim_leave_right);
            this.mActivity.startActivity(intent);
            this.mActivity.finish();
        } else if (v.getId() == R.id.imageViewNext) {
            if (gotoNext()) {
                this.mActivity.overridePendingTransition(R.anim.anim_enter_right, R.anim.anim_leave_left);
                this.mActivity.finish();
                return;
            }
        } else if (v.getId() == R.id.imageViewCorrect) {
            this.mScore = this.mfFullScore;
            bJumpToNext = true;
        } else if (v.getId() == R.id.imageViewHalfCorrect) {
            this.mScore = this.mfFullScore / 2.0f;
            bJumpToNext = false;
        } else if (v.getId() == R.id.imageViewWrong) {
            this.mScore = 0.0f;
            bJumpToNext = true;
        } else {
            this.mScore = ((Float) v.getTag()).floatValue();
            bJumpToNext = true;
        }
        if (this.mCurrentQuestion.answerResult.getAnswerresult().intValue() != 0) {
            bJumpToNext = false;
        }
        if (this.mScore == this.mfFullScore) {
            this.mCurrentQuestion.answerResult.setAnswerresult(Integer.valueOf(2));
        } else if (this.mScore == 0.0f) {
            this.mCurrentQuestion.answerResult.setAnswerresult(Integer.valueOf(-1));
        } else {
            this.mCurrentQuestion.answerResult.setAnswerresult(Integer.valueOf(1));
        }
        save();
        updateDisplay();
        if (bJumpToNext && gotoNext()) {
            this.mActivity.overridePendingTransition(R.anim.anim_enter_right, R.anim.anim_leave_left);
            this.mActivity.finish();
        }
    }

    private void save() {
        Log.d("ActivityPlugin_AnswerSheetOtherQuestion", "save");
        this.mCurrentQuestion.answerResult.setAnswercorrecthandwrite(this.mDrawView.getDataAsString(this.mnDrawViewOldDataPointIndex));
        this.mCurrentQuestion.answerResult.setAnswerscore(Float.valueOf(this.mScore));
        if (this.mCurrentQuestion.answerResult.getAnswercorrecthandwritepreview() == null || this.mCurrentQuestion.answerResult.getAnswercorrecthandwritepreview().isEmpty()) {
            this.mCurrentQuestion.answerResult.setAnswercorrecthandwritepreview(new StringBuilder(String.valueOf(this.mCurrentQuestion.studentAnswer.getGuid())).append("_CorrectingPreview.jpg").toString());
        }
        this.mCurrentQuestion.answerResult.setSyn_timestamp(new Date());
        DataSynchronizeItemObject CallItem = new DataSynchronizeItemObject(this.mCurrentQuestion.answerResult.getAnswercorrecthandwritepreview(), null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                Log.d("ActivityPlugin_AnswerSheetOtherQuestion", "save image complete. Event send");
                EventBus.getDefault().post(new AnswerSheetCorrectImageChange());
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showToastMessage("批改笔记缩略图上传失败", 0);
            }
        });
        Bitmap bitmap = this.mDrawView.saveToBitmap();
        String szBase64 = Utilities.saveBitmapToBase64String(bitmap);
        bitmap.recycle();
        CallItem.writeTextData(szBase64);
        CallItem.setReadOperation(false);
        CallItem.setClientID(this.mCurrentQuestion.studentAnswer.getClientid());
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
        EventBus.getDefault().post(this.mCurrentQuestion.answerResult);
        this.mbChanged = false;
    }

    private void updateDisplay() {
        int i;
        this.mImageViewCorrect.setSelected(false);
        this.mImageViewWrong.setSelected(false);
        this.mImageViewHalfCorrect.setSelected(false);
        if (this.mScore == this.mfFullScore) {
            this.mImageViewCorrect.setSelected(true);
        } else if (this.mScore == 0.0f) {
            this.mImageViewWrong.setSelected(true);
        } else {
            this.mImageViewHalfCorrect.setSelected(true);
        }
        for (i = 0; i < this.mLayoutPlusScore.getChildCount(); i++) {
            this.mLayoutPlusScore.getChildAt(i).setSelected(false);
        }
        for (i = 0; i < this.mLayoutMinusScore.getChildCount(); i++) {
            this.mLayoutMinusScore.getChildAt(i).setSelected(false);
        }
        for (i = 0; i < this.mLayoutPlusScore.getChildCount(); i++) {
            View view = this.mLayoutPlusScore.getChildAt(i);
            if (((Float) view.getTag()).floatValue() == this.mScore) {
                view.setSelected(true);
                if (!isViewVisible(this.mScrollViewPlusScore, view)) {
                    this.mScrollViewPlusScore.scrollTo(view.getLeft(), 0);
                }
            }
        }
        for (i = 0; i < this.mLayoutMinusScore.getChildCount(); i++) {
            view = this.mLayoutMinusScore.getChildAt(i);
            if (((Float) view.getTag()).floatValue() == this.mScore) {
                view.setSelected(true);
                if (!isViewVisible(this.mScrollViewMinusScore, view)) {
                    this.mScrollViewMinusScore.scrollTo(view.getLeft(), 0);
                }
            }
        }
    }

    private boolean isViewVisible(HorizontalScrollView scrollView, View view) {
        Rect scrollBounds = new Rect();
        scrollBounds.left = scrollView.getScrollX();
        scrollBounds.right = scrollBounds.left + scrollView.getWidth();
        if (view.getLeft() < scrollBounds.left || view.getRight() > scrollBounds.right) {
            return false;
        }
        return true;
    }

    public void OnTouchDown() {
    }

    public void OnTouchUp() {
        this.mbChanged = true;
    }

    public void OnTouchMove() {
    }

    public void OnPenButtonDown() {
    }

    public void OnPenButtonUp() {
    }

    public void OnTouchPen() {
    }

    public void OnTouchFinger() {
    }

    public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
    }

    public void onPause() {
        if (this.mbChanged) {
            save();
        }
        super.onPause();
    }
}
