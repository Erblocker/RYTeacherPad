package com.netspace.library.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.pad.library.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;

public class StudentAnswerImageAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    private ArrayList<StudentAnswer> marrData;
    private HashMap<String, Boolean> mmapSelecetedAnswers = new HashMap();
    private String mszSelectedItem;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public View mCardView;
        public TextView mQuestionAnswer;
        public TextView mStudentName;
        public ImageView mThumbnail;

        public ViewHolder(StudentAnswerImageAdapter adapter, View itemView, Context context, OnClickListener OnClickListener, OnLongClickListener OnLongClickListener) {
            super(itemView);
            this.mCardView = itemView.findViewById(R.id.cardViewStudentAnswer);
            this.mCardView.setOnClickListener(OnClickListener);
            this.mCardView.setOnLongClickListener(OnLongClickListener);
            this.mQuestionAnswer = (TextView) itemView.findViewById(R.id.textViewAnswer);
            this.mStudentName = (TextView) itemView.findViewById(R.id.textViewStudentName);
            this.mThumbnail = (ImageView) itemView.findViewById(R.id.imageViewThumbnail);
        }
    }

    public StudentAnswerImageAdapter(Context context, ArrayList<StudentAnswer> arrData) {
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

    public void setItemSelected(String szGUID, boolean bSelected) {
        this.mmapSelecetedAnswers.put(szGUID, Boolean.valueOf(bSelected));
    }

    public void setCurrentSelectedItem(String szItemKey) {
        this.mszSelectedItem = szItemKey;
    }

    public int getItemCount() {
        return this.marrData.size();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_studentanswerimage_item, viewGroup, false), this.mContext, this.mOnClickListener, this.mOnLongClickListener);
    }

    public void onBindViewHolder(ViewHolder ViewHolder, int arg1) {
        final StudentAnswer answerData = (StudentAnswer) this.marrData.get(arg1);
        if (this.mszSelectedItem != null && this.mszSelectedItem.equalsIgnoreCase(answerData.szStudentJID)) {
            ViewHolder.mCardView.setSelected(true);
        } else if (this.mmapSelecetedAnswers.get(answerData.szStudentJID) == null || ((Boolean) this.mmapSelecetedAnswers.get(answerData.szStudentJID)).equals(Boolean.FALSE)) {
            ViewHolder.mCardView.setSelected(false);
        } else {
            ViewHolder.mCardView.setSelected(true);
        }
        ViewHolder.mCardView.setTag(answerData);
        if (answerData.bIsHandWrite) {
            ViewHolder.mQuestionAnswer.setVisibility(8);
            final ImageView imageView = ViewHolder.mThumbnail;
            if (answerData.szFinalImageURL == null) {
                answerData.szFinalImageURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + answerData.szAnswerOrPictureKey;
            }
            Picasso.with(this.mContext).load(answerData.szFinalImageURL).resize(0, 500).into(ViewHolder.mThumbnail, new Callback() {
                public void onError() {
                    String szOldImage = answerData.szFinalImageURL;
                    answerData.szFinalImageURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + answerData.szAnswerOrPictureKey;
                    if (!szOldImage.equalsIgnoreCase(answerData.szFinalImageURL)) {
                        Picasso.with(StudentAnswerImageAdapter.this.mContext).load(answerData.szFinalImageURL).resize(0, 500).into(imageView);
                    }
                }

                public void onSuccess() {
                }
            });
        } else {
            ViewHolder.mQuestionAnswer.setVisibility(0);
            ViewHolder.mThumbnail.setImageDrawable(null);
            ViewHolder.mQuestionAnswer.setText(answerData.szAnswerOrPictureKey);
            Picasso.with(this.mContext).cancelRequest(ViewHolder.mThumbnail);
        }
        ViewHolder.mStudentName.setText(answerData.szStudentName);
        ViewHolder.mStudentName.setVisibility(0);
    }
}
