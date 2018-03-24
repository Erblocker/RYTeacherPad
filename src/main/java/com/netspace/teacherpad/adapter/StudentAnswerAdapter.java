package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class StudentAnswerAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    private ArrayList<StudentAnswer> marrData;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        private ImageButton mButtonSendBack;
        private ImageButton mButtonVote;
        private CardView mCardView;
        private TextView mQuestionAnswer;
        private TextView mStudentName;
        private ImageView mThumbnail;
        private TextView mTime;
        private TextView mTitle;
        private TextView mTopMessage;
        private TextView mVoteCount;

        public ViewHolder(StudentAnswerAdapter adapter, View itemView, Context context, OnClickListener OnClickListener, OnLongClickListener OnLongClickListener) {
            super(itemView);
            this.mCardView = (CardView) itemView.findViewById(R.id.cardViewStudentAnswer);
            this.mCardView.setOnClickListener(OnClickListener);
            this.mTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.mButtonVote = (ImageButton) itemView.findViewById(R.id.buttonVoteCount);
            this.mButtonVote.setOnClickListener(OnClickListener);
            this.mButtonSendBack = (ImageButton) itemView.findViewById(R.id.buttonSendBack);
            this.mButtonSendBack.setOnClickListener(OnClickListener);
            this.mButtonSendBack.setOnLongClickListener(OnLongClickListener);
            this.mQuestionAnswer = (TextView) itemView.findViewById(R.id.textViewAnswer);
            this.mTopMessage = (TextView) itemView.findViewById(R.id.textViewTopMessage);
            this.mStudentName = (TextView) itemView.findViewById(R.id.textViewStudentName);
            this.mTime = (TextView) itemView.findViewById(R.id.textViewTime);
            this.mVoteCount = (TextView) itemView.findViewById(R.id.textViewVoteCount);
            this.mThumbnail = (ImageView) itemView.findViewById(R.id.imageViewThumbnail);
        }
    }

    public StudentAnswerAdapter(Context context, ArrayList<StudentAnswer> arrData) {
        this.mContext = context;
        this.marrData = arrData;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.mOnLongClickListener = onLongClickListener;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemCount() {
        return this.marrData.size();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_studentanswer, viewGroup, false), this.mContext, this.mOnClickListener, this.mOnLongClickListener);
    }

    public void onBindViewHolder(ViewHolder ViewHolder, int arg1) {
        final StudentAnswer answerData = (StudentAnswer) this.marrData.get(arg1);
        ViewHolder.mCardView.setTag(answerData);
        ViewHolder.mTime.setText(Utilities.getTimeOffsetInHourMinutesSeconds((long) answerData.nTimeInMS));
        ViewHolder.mTitle.setText(answerData.szStudentName);
        ViewHolder.mButtonVote.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_thumbs_o_up).colorRes(17170444).actionBarSize());
        ViewHolder.mButtonVote.setTag(answerData);
        ViewHolder.mButtonSendBack.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_tv).colorRes(17170444).actionBarSize());
        ViewHolder.mButtonSendBack.setTag(answerData);
        ViewHolder.mVoteCount.setText(String.valueOf(answerData.arrVoteUsers.size()));
        if (answerData.bIsHandWrite) {
            ViewHolder.mQuestionAnswer.setVisibility(8);
            ViewHolder.mButtonSendBack.setVisibility(0);
            ViewHolder.mButtonVote.setVisibility(0);
            ViewHolder.mVoteCount.setVisibility(0);
            final ImageView imageView = ViewHolder.mThumbnail;
            if (answerData.szFinalImageURL == null) {
                answerData.szFinalImageURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + answerData.szAnswerOrPictureKey;
            }
            Picasso.with(this.mContext).load(answerData.szFinalImageURL).resize(0, 500).into(ViewHolder.mThumbnail, new Callback() {
                public void onError() {
                    String szOldImage = answerData.szFinalImageURL;
                    answerData.szFinalImageURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + answerData.szAnswerOrPictureKey;
                    if (!szOldImage.equalsIgnoreCase(answerData.szFinalImageURL)) {
                        Picasso.with(StudentAnswerAdapter.this.mContext).load(answerData.szFinalImageURL).resize(0, 500).into(imageView);
                    }
                }

                public void onSuccess() {
                }
            });
        } else {
            ViewHolder.mQuestionAnswer.setVisibility(0);
            ViewHolder.mButtonSendBack.setVisibility(8);
            ViewHolder.mButtonVote.setVisibility(8);
            ViewHolder.mVoteCount.setVisibility(8);
            ViewHolder.mThumbnail.setImageResource(R.drawable.material_background_2);
            ViewHolder.mQuestionAnswer.setText(answerData.szAnswerOrPictureKey);
            Picasso.with(this.mContext).cancelRequest(ViewHolder.mThumbnail);
        }
        ViewHolder.mStudentName.setVisibility(8);
        String szTopMessage = "";
        String szMessage = "";
        szTopMessage = "第" + String.valueOf(arg1 + 1) + "名";
        if (szTopMessage.isEmpty()) {
            ViewHolder.mTopMessage.setVisibility(4);
            return;
        }
        ViewHolder.mTopMessage.setText(szTopMessage);
        ViewHolder.mTopMessage.setVisibility(0);
    }
}
