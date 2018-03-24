package com.netspace.library.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.StudentClassAnswer;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Map.Entry;

public class StudentClassAnswerAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private OnClickListener mOnClickListener;
    private ArrayList<StudentClassAnswer> marrData;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public ImageView mAnswerResult;
        public CardView mCardView;
        public TextView mMessage;
        public TextView mQuestionAnswer;
        public TextView mScore;
        public ImageView mThumbnail;
        public TextView mTitle;
        public TextView mTopMessage;
        public TableRow mVotes;

        public ViewHolder(StudentClassAnswerAdapter adapter, View itemView, Context context, OnClickListener OnClickListener) {
            super(itemView);
            this.mCardView = (CardView) itemView.findViewById(R.id.cardViewStudentClassAnswer);
            this.mCardView.setOnClickListener(OnClickListener);
            this.mTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.mMessage = (TextView) itemView.findViewById(R.id.textViewMessage);
            this.mQuestionAnswer = (TextView) itemView.findViewById(R.id.textViewQuestionAnswer);
            this.mScore = (TextView) itemView.findViewById(R.id.textViewScore);
            this.mTopMessage = (TextView) itemView.findViewById(R.id.textViewTopMessage);
            this.mVotes = (TableRow) itemView.findViewById(R.id.tableRowVote);
            this.mThumbnail = (ImageView) itemView.findViewById(R.id.imageViewThumbnail);
            this.mAnswerResult = (ImageView) itemView.findViewById(R.id.imageViewAnswerResult);
        }
    }

    public StudentClassAnswerAdapter(Context context, ArrayList<StudentClassAnswer> arrData) {
        this.mContext = context;
        this.marrData = arrData;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemCount() {
        return this.marrData.size();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_studentclassanswer, viewGroup, false), this.mContext, this.mOnClickListener);
    }

    public void onBindViewHolder(ViewHolder ViewHolder, int arg1) {
        StudentClassAnswer answerData = (StudentClassAnswer) this.marrData.get(arg1);
        ViewHolder.mTitle.setText(Utilities.getTimePart(answerData.szTime));
        if (answerData.szAnswer.isEmpty() && answerData.szCorrectAnswer.isEmpty()) {
            ViewHolder.mQuestionAnswer.setVisibility(4);
        } else {
            String szAnswerText;
            if (answerData.szCorrectAnswer.isEmpty()) {
                szAnswerText = answerData.szAnswer;
            } else {
                szAnswerText = answerData.szAnswer + "(" + answerData.szCorrectAnswer + ")";
            }
            SpannableString szText = new SpannableString(szAnswerText);
            if (!(answerData.nAnswerResult == 2 || answerData.nAnswerResult == 0)) {
                szText.setSpan(new StrikethroughSpan(), 0, answerData.szAnswer.length(), 33);
            }
            ViewHolder.mQuestionAnswer.setText(szText);
        }
        if (answerData.nAnswerResult == 2) {
            ViewHolder.mAnswerResult.setImageResource(R.drawable.ic_resource_correct);
            ViewHolder.mAnswerResult.setVisibility(0);
        } else if (answerData.nAnswerResult == 1) {
            ViewHolder.mAnswerResult.setImageResource(R.drawable.ic_resource_halfcorrect);
            ViewHolder.mAnswerResult.setVisibility(0);
        } else if (answerData.nAnswerResult == -1) {
            ViewHolder.mAnswerResult.setImageResource(R.drawable.ic_resource_wrong);
            ViewHolder.mAnswerResult.setVisibility(0);
        } else {
            ViewHolder.mAnswerResult.setVisibility(4);
        }
        if (answerData.nScore != 0) {
            ViewHolder.mScore.setText("+" + String.valueOf(answerData.nScore) + "分");
            ViewHolder.mScore.setVisibility(0);
        } else {
            ViewHolder.mScore.setVisibility(4);
        }
        ViewHolder.mVotes.removeAllViews();
        for (Entry<String, Integer> entry : answerData.mapVoteCount.entrySet()) {
            String szKey = (String) entry.getKey();
            Integer nValue = (Integer) entry.getValue();
            if (nValue.intValue() > 0) {
                ImageView imageView = new ImageView(this.mContext);
                imageView.setImageResource(R.drawable.ic_rating_star);
                ViewHolder.mVotes.addView(imageView, Utilities.dpToPixel(20, this.mContext), Utilities.dpToPixel(20, this.mContext));
                View textView = new TextView(this.mContext);
                textView.setGravity(17);
                textView.setText(" " + String.valueOf(nValue) + " ");
                textView.setTextColor(-8355712);
                ViewHolder.mVotes.addView(textView, -2, -1);
            }
        }
        if (!answerData.szPicturePackageID.isEmpty()) {
            Picasso.with(this.mContext).load(new StringBuilder(String.valueOf(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/DataSynchronizeGetSingleData?clientid=" + answerData.szClientID + "&packageid=")).append(answerData.szPicturePackageID).toString()).resize(0, 500).into(ViewHolder.mThumbnail);
        }
        String szTopMessage = "";
        String szMessage = "";
        if (answerData.nSubmitIndex == 1) {
            szTopMessage = "全班你第一个提交";
        }
        if (szTopMessage.isEmpty()) {
            ViewHolder.mTopMessage.setVisibility(4);
        } else {
            ViewHolder.mTopMessage.setText(szTopMessage);
            ViewHolder.mTopMessage.setVisibility(0);
        }
        ViewHolder.mMessage.setText("花了" + Utilities.getTimeOffsetInSeconds(Utilities.getTimeDifference(answerData.szTime, answerData.szAnswerTime)) + "秒思考后提交答案。");
    }
}
