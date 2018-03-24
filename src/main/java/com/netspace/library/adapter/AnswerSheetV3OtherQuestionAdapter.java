package com.netspace.library.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import com.netspace.library.activity.AnswerSheetV3OtherQuestionCorrectActivity.AnswerSheetV3OtherQuestion;
import com.netspace.library.activity.FingerDrawActivity2;
import com.netspace.library.activity.plugins.ActivityPlugin_AnswerSheetOtherQuestions;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.LinedEditText;
import com.netspace.library.database.DaoSession;
import com.netspace.library.dialog.AnswerSheetV2OneAnswerDialog;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Date;
import org.greenrobot.eventbus.EventBus;

public class AnswerSheetV3OtherQuestionAdapter extends Adapter<ViewHolder> {
    private static final String TAG = "AnswerSheetV3OtherQuestionAdapter";
    private Context mContext;
    private DaoSession mDaoSession;
    private AnswerSheetV2OneAnswerDialog mDirectOpenPictureDialog;
    private int mMenuStartID = R.id.action_a;
    private OnClickListener mOnClickListener;
    private ArrayList<AnswerSheetV3OtherQuestion> marrData;
    private ArrayList<Float> marrScores = new ArrayList();
    private float mfFullScore = 0.0f;
    private String mszTitle;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        private ImageView mAnswerImage;
        private LinearLayout mContentLayout;
        private LinearLayout mCorrectLinearLayout;
        private LinedEditText mEditText;
        private ImageView mImageCorrectResult;
        private TextView mTextViewRealName;
        private TextView mTextViewScore;

        public ViewHolder(AnswerSheetV3OtherQuestionAdapter adapter, View itemView, Context context, OnClickListener OnClickListener) {
            super(itemView);
            this.mImageCorrectResult = (ImageView) itemView.findViewById(R.id.imageViewResult);
            this.mAnswerImage = (ImageView) itemView.findViewById(R.id.imageViewThumbnil);
            this.mEditText = (LinedEditText) itemView.findViewById(R.id.editText1);
            this.mTextViewRealName = (TextView) itemView.findViewById(R.id.textViewUserName);
            this.mTextViewScore = (TextView) itemView.findViewById(R.id.textViewScore);
            this.mContentLayout = (LinearLayout) itemView.findViewById(R.id.linearLayoutContent);
            this.mCorrectLinearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayoutCorrectMenu);
            this.mCorrectLinearLayout.setClickable(true);
        }
    }

    public AnswerSheetV3OtherQuestionAdapter(Context context, ArrayList<AnswerSheetV3OtherQuestion> arrData, DaoSession daoSession) {
        this.mContext = context;
        this.marrData = arrData;
        this.mDaoSession = daoSession;
        this.mDirectOpenPictureDialog = new AnswerSheetV2OneAnswerDialog();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public void setTitle(String szTitle) {
        this.mszTitle = szTitle;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemCount() {
        return this.marrData.size();
    }

    public void setFullScore(float fFullScore) {
        this.mfFullScore = fFullScore;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_answersheetv2otherquestion, viewGroup, false), this.mContext, this.mOnClickListener);
    }

    public void onBindViewHolder(final ViewHolder ViewHolder, final int arg1) {
        final AnswerSheetV3OtherQuestion answerData = (AnswerSheetV3OtherQuestion) this.marrData.get(arg1);
        String szGUID = answerData.studentAnswer.getQuestionguid();
        String szURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/DataSynchronizeGetSingleData?clientid=" + answerData.studentAnswer.getClientid() + "&packageid=";
        ViewHolder.mCorrectLinearLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(AnswerSheetV3OtherQuestionAdapter.this.mContext, v);
                Menu menu = popup.getMenu();
                int nIndex = 0;
                AnswerSheetV3OtherQuestionAdapter.this.marrScores.clear();
                for (float fScore = AnswerSheetV3OtherQuestionAdapter.this.mfFullScore; fScore >= 0.0f; fScore = (float) (((double) fScore) - 0.5d)) {
                    if (fScore == AnswerSheetV3OtherQuestionAdapter.this.mfFullScore) {
                        menu.add(0, AnswerSheetV3OtherQuestionAdapter.this.mMenuStartID + nIndex, 0, "全对(+" + String.valueOf(AnswerSheetV3OtherQuestionAdapter.this.mfFullScore) + "分)");
                    } else if (fScore == 0.0f) {
                        menu.add(0, AnswerSheetV3OtherQuestionAdapter.this.mMenuStartID + nIndex, 0, "全错(+0分)");
                    } else {
                        menu.add(0, AnswerSheetV3OtherQuestionAdapter.this.mMenuStartID + nIndex, 0, "半对(+" + String.valueOf(fScore) + "分)");
                    }
                    nIndex++;
                    AnswerSheetV3OtherQuestionAdapter.this.marrScores.add(Float.valueOf(fScore));
                }
                final AnswerSheetV3OtherQuestion answerSheetV3OtherQuestion = answerData;
                final ViewHolder viewHolder = ViewHolder;
                popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        float fScore = ((Float) AnswerSheetV3OtherQuestionAdapter.this.marrScores.get(item.getItemId() - AnswerSheetV3OtherQuestionAdapter.this.mMenuStartID)).floatValue();
                        answerSheetV3OtherQuestion.answerResult.setAnswerscore(Float.valueOf(fScore));
                        if (fScore == AnswerSheetV3OtherQuestionAdapter.this.mfFullScore) {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_correct);
                            answerSheetV3OtherQuestion.answerResult.setAnswerresult(Integer.valueOf(2));
                        } else if (fScore == 0.0f) {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_wrong);
                            answerSheetV3OtherQuestion.answerResult.setAnswerresult(Integer.valueOf(-1));
                        } else {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_halfcorrect);
                            answerSheetV3OtherQuestion.answerResult.setAnswerresult(Integer.valueOf(1));
                        }
                        viewHolder.mTextViewScore.setText("+" + String.valueOf(fScore) + "分");
                        answerSheetV3OtherQuestion.answerResult.setSyn_timestamp(new Date());
                        EventBus.getDefault().post(answerSheetV3OtherQuestion.answerResult);
                        return false;
                    }
                });
                popup.show();
            }
        });
        ViewHolder.mAnswerImage.setVisibility(4);
        ViewHolder.mEditText.setVisibility(4);
        ViewHolder.mTextViewRealName.setText(answerData.studentAnswer.getStudentname());
        ViewHolder.mEditText.setFocusable(false);
        String szLoadURL = szURL;
        if (answerData.answerResult == null || answerData.answerResult.getAnswerresult().intValue() == 0) {
            ViewHolder.mImageCorrectResult.setImageDrawable(null);
            ViewHolder.mTextViewScore.setText("?");
        } else {
            float fScore = answerData.answerResult.getAnswerscore().floatValue();
            if (fScore == this.mfFullScore) {
                ViewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_correct);
            } else if (fScore == 0.0f) {
                ViewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_wrong);
            } else {
                ViewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_halfcorrect);
            }
            ViewHolder.mTextViewScore.setText("+" + String.valueOf(fScore) + "分");
        }
        if (answerData.answerResult.getAnswercorrecthandwritepreview() != null && !answerData.answerResult.getAnswercorrecthandwritepreview().isEmpty()) {
            szLoadURL = new StringBuilder(String.valueOf(szLoadURL)).append(answerData.answerResult.getAnswercorrecthandwritepreview()).toString();
            Picasso.with(this.mContext).load(szLoadURL).resize(0, 500).into(ViewHolder.mAnswerImage);
            ViewHolder.mAnswerImage.setVisibility(0);
            Log.d(TAG, "load correct url=" + szLoadURL);
        } else if (!answerData.studentAnswer.getAnswertext().isEmpty()) {
            ViewHolder.mEditText.setVisibility(0);
            ViewHolder.mEditText.setText(answerData.studentAnswer.getAnswertext());
            ViewHolder.mEditText.setTextSize(9.0f);
        } else if (!answerData.studentAnswer.getAnswerhandwritedata().isEmpty()) {
            szLoadURL = new StringBuilder(String.valueOf(szLoadURL)).append(answerData.studentAnswer.getAnswerhandwritepreview()).toString();
            ViewHolder.mAnswerImage.setVisibility(0);
            Picasso.with(this.mContext).load(szLoadURL).resize(0, 500).into(ViewHolder.mAnswerImage);
        } else if (!answerData.studentAnswer.getAnswercamera().isEmpty()) {
            szLoadURL = new StringBuilder(String.valueOf(szLoadURL)).append(answerData.studentAnswer.getAnswercamera()).toString();
            ViewHolder.mAnswerImage.setVisibility(0);
            Picasso.with(this.mContext).load(szLoadURL).resize(0, 500).into(ViewHolder.mAnswerImage);
        }
        String szImageURL = szLoadURL;
        ViewHolder.mContentLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ActivityPlugin_AnswerSheetOtherQuestions.setAnswerData(AnswerSheetV3OtherQuestionAdapter.this.marrData);
                Intent intent = new Intent(AnswerSheetV3OtherQuestionAdapter.this.mContext, FingerDrawActivity2.class);
                intent.putExtra("answersheet", true);
                intent.putExtra("id", arg1);
                intent.putExtra("fullscore", AnswerSheetV3OtherQuestionAdapter.this.mfFullScore);
                intent.putExtra("title", AnswerSheetV3OtherQuestionAdapter.this.mszTitle);
                AnswerSheetV3OtherQuestionAdapter.this.mContext.startActivity(intent);
            }
        });
    }
}
