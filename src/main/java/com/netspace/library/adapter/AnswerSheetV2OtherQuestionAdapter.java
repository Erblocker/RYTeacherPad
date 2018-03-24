package com.netspace.library.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView.Adapter;
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
import com.netspace.library.activity.AnswerSheetV2OtherQuestionCorrectActivity.AnswerSheetV2OtherQuestion;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.LinedEditText;
import com.netspace.library.dialog.AnswerSheetV2OneAnswerDialog;
import com.netspace.library.dialog.AnswerSheetV2OneAnswerDialog.OnCorrectScoreSelectedListener;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.pad.library.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class AnswerSheetV2OtherQuestionAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private AnswerSheetV2OneAnswerDialog mDirectOpenPictureDialog;
    private int mMenuStartID = R.id.action_a;
    private OnClickListener mOnClickListener;
    private ArrayList<AnswerSheetV2OtherQuestion> marrData;
    private ArrayList<Float> marrScores = new ArrayList();
    private float mfFullScore = 0.0f;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        private ImageView mAnswerImage;
        private LinearLayout mContentLayout;
        private LinearLayout mCorrectLinearLayout;
        private LinedEditText mEditText;
        private ImageView mImageCorrectResult;
        private TextView mTextViewRealName;
        private TextView mTextViewScore;

        public ViewHolder(AnswerSheetV2OtherQuestionAdapter adapter, View itemView, Context context, OnClickListener OnClickListener) {
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

    public AnswerSheetV2OtherQuestionAdapter(Context context, ArrayList<AnswerSheetV2OtherQuestion> arrData) {
        this.mContext = context;
        this.marrData = arrData;
        this.mDirectOpenPictureDialog = new AnswerSheetV2OneAnswerDialog();
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

    public void setFullScore(float fFullScore) {
        this.mfFullScore = fFullScore;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_answersheetv2otherquestion, viewGroup, false), this.mContext, this.mOnClickListener);
    }

    public void onBindViewHolder(final ViewHolder ViewHolder, int arg1) {
        final AnswerSheetV2OtherQuestion answerData = (AnswerSheetV2OtherQuestion) this.marrData.get(arg1);
        final String szGUID = answerData.questionItem.szGuid;
        final String szURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/DataSynchronizeGetSingleData?clientid=" + answerData.loadUserData.szClientID + "&packageid=";
        ViewHolder.mCorrectLinearLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(AnswerSheetV2OtherQuestionAdapter.this.mContext, v);
                Menu menu = popup.getMenu();
                int nIndex = 0;
                AnswerSheetV2OtherQuestionAdapter.this.marrScores.clear();
                for (float fScore = AnswerSheetV2OtherQuestionAdapter.this.mfFullScore; fScore >= 0.0f; fScore = (float) (((double) fScore) - 0.5d)) {
                    if (fScore == AnswerSheetV2OtherQuestionAdapter.this.mfFullScore) {
                        menu.add(0, AnswerSheetV2OtherQuestionAdapter.this.mMenuStartID + nIndex, 0, "全对(+" + String.valueOf(AnswerSheetV2OtherQuestionAdapter.this.mfFullScore) + "分)");
                    } else if (fScore == 0.0f) {
                        menu.add(0, AnswerSheetV2OtherQuestionAdapter.this.mMenuStartID + nIndex, 0, "全错(+0分)");
                    } else {
                        menu.add(0, AnswerSheetV2OtherQuestionAdapter.this.mMenuStartID + nIndex, 0, "半对(+" + String.valueOf(fScore) + "分)");
                    }
                    nIndex++;
                    AnswerSheetV2OtherQuestionAdapter.this.marrScores.add(Float.valueOf(fScore));
                }
                final AnswerSheetV2OtherQuestion answerSheetV2OtherQuestion = answerData;
                final String str = szGUID;
                final ViewHolder viewHolder = ViewHolder;
                popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        float fScore = ((Float) AnswerSheetV2OtherQuestionAdapter.this.marrScores.get(item.getItemId() - AnswerSheetV2OtherQuestionAdapter.this.mMenuStartID)).floatValue();
                        answerSheetV2OtherQuestion.loadUserData.mapQuestionScore.put(str, Float.valueOf(fScore));
                        if (fScore == AnswerSheetV2OtherQuestionAdapter.this.mfFullScore) {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_correct);
                            answerSheetV2OtherQuestion.loadUserData.mapAnswerResult.put(str, Integer.valueOf(2));
                        } else if (fScore == 0.0f) {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_wrong);
                            answerSheetV2OtherQuestion.loadUserData.mapAnswerResult.put(str, Integer.valueOf(-1));
                        } else {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_halfcorrect);
                            answerSheetV2OtherQuestion.loadUserData.mapAnswerResult.put(str, Integer.valueOf(1));
                        }
                        viewHolder.mTextViewScore.setText("+" + String.valueOf(fScore) + "分");
                        return false;
                    }
                });
                popup.show();
            }
        });
        ViewHolder.mAnswerImage.setVisibility(4);
        ViewHolder.mEditText.setVisibility(4);
        ViewHolder.mTextViewRealName.setText(answerData.loadUserData.szRealName);
        ViewHolder.mEditText.setFocusable(false);
        String szLoadURL = szURL;
        if (answerData.loadUserData.mapQuestionScore.get(szGUID) == null || answerData.loadUserData.mapAnswerResult.get(szGUID) == null || ((Integer) answerData.loadUserData.mapAnswerResult.get(szGUID)).intValue() == 0) {
            ViewHolder.mImageCorrectResult.setImageDrawable(null);
            ViewHolder.mTextViewScore.setText("?");
        } else {
            float fScore = ((Float) answerData.loadUserData.mapQuestionScore.get(szGUID)).floatValue();
            if (fScore == this.mfFullScore) {
                ViewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_correct);
            } else if (fScore == 0.0f) {
                ViewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_wrong);
            } else {
                ViewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_halfcorrect);
            }
            ViewHolder.mTextViewScore.setText("+" + String.valueOf(fScore) + "分");
        }
        if (!answerData.questionItem.szAnswer0.isEmpty()) {
            ViewHolder.mEditText.setVisibility(0);
            ViewHolder.mEditText.setText(answerData.questionItem.szAnswer0);
            ViewHolder.mEditText.setTextSize(9.0f);
        } else if (!answerData.questionItem.szAnswer1Preview.isEmpty()) {
            szLoadURL = new StringBuilder(String.valueOf(szLoadURL)).append(answerData.questionItem.szAnswer1Preview).toString();
            ViewHolder.mAnswerImage.setVisibility(0);
            Picasso.with(this.mContext).load(szLoadURL).resize(0, 500).into(ViewHolder.mAnswerImage);
        } else if (!answerData.questionItem.szAnswer2.isEmpty()) {
            szLoadURL = new StringBuilder(String.valueOf(szLoadURL)).append(answerData.questionItem.szAnswer2).toString();
            ViewHolder.mAnswerImage.setVisibility(0);
            Picasso.with(this.mContext).load(szLoadURL).resize(0, 500).into(ViewHolder.mAnswerImage);
        }
        answerData.questionItem.fFullScore = this.mfFullScore;
        final String szImageURL = szLoadURL;
        final ViewHolder viewHolder = ViewHolder;
        ViewHolder.mContentLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (AnswerSheetV2OtherQuestionAdapter.this.mDirectOpenPictureDialog.isOnlyPicture(answerData)) {
                    AnswerSheetV2OneAnswerDialog access$6 = AnswerSheetV2OtherQuestionAdapter.this.mDirectOpenPictureDialog;
                    final ViewHolder viewHolder = viewHolder;
                    final String str = szImageURL;
                    access$6.setCallBack(new OnCorrectScoreSelectedListener() {
                        public void onScoreSelected(float fNewScore) {
                        }

                        public void onImageChanged(String szImageKey) {
                            ImageView access$3 = viewHolder.mAnswerImage;
                            final String str = str;
                            final ViewHolder viewHolder = viewHolder;
                            access$3.postDelayed(new Runnable() {
                                public void run() {
                                    Picasso.with(AnswerSheetV2OtherQuestionAdapter.this.mContext).invalidate(str);
                                    Picasso.with(AnswerSheetV2OtherQuestionAdapter.this.mContext).load(str).memoryPolicy(MemoryPolicy.NO_CACHE, new MemoryPolicy[0]).networkPolicy(NetworkPolicy.NO_CACHE, new NetworkPolicy[0]).resize(0, 500).into(viewHolder.mAnswerImage);
                                }
                            }, 500);
                        }
                    });
                    AnswerSheetV2OtherQuestionAdapter.this.mDirectOpenPictureDialog.setData(answerData);
                    return;
                }
                AnswerSheetV2OneAnswerDialog dialog = new AnswerSheetV2OneAnswerDialog();
                Activity activity = UI.getCurrentActivity();
                viewHolder = viewHolder;
                final AnswerSheetV2OtherQuestion answerSheetV2OtherQuestion = answerData;
                final String str2 = szGUID;
                final String str3 = szURL;
                dialog.setCallBack(new OnCorrectScoreSelectedListener() {
                    public void onScoreSelected(float fNewScore) {
                        float fScore = fNewScore;
                        if (fScore == AnswerSheetV2OtherQuestionAdapter.this.mfFullScore) {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_correct);
                            answerSheetV2OtherQuestion.loadUserData.mapAnswerResult.put(str2, Integer.valueOf(2));
                        } else if (fScore == 0.0f) {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_wrong);
                            answerSheetV2OtherQuestion.loadUserData.mapAnswerResult.put(str2, Integer.valueOf(-1));
                        } else {
                            viewHolder.mImageCorrectResult.setImageResource(R.drawable.ic_resource_halfcorrect);
                            answerSheetV2OtherQuestion.loadUserData.mapAnswerResult.put(str2, Integer.valueOf(1));
                        }
                        viewHolder.mTextViewScore.setText("+" + String.valueOf(fScore) + "分");
                        answerSheetV2OtherQuestion.loadUserData.mapQuestionScore.put(str2, Float.valueOf(fScore));
                    }

                    public void onImageChanged(String szImageKey) {
                        final String szImageURL = str3 + szImageKey;
                        ImageView access$3 = viewHolder.mAnswerImage;
                        final ViewHolder viewHolder = viewHolder;
                        access$3.postDelayed(new Runnable() {
                            public void run() {
                                Picasso.with(AnswerSheetV2OtherQuestionAdapter.this.mContext).invalidate(szImageURL);
                                Picasso.with(AnswerSheetV2OtherQuestionAdapter.this.mContext).load(szImageURL).memoryPolicy(MemoryPolicy.NO_CACHE, new MemoryPolicy[0]).networkPolicy(NetworkPolicy.NO_CACHE, new NetworkPolicy[0]).resize(0, 500).into(viewHolder.mAnswerImage);
                            }
                        }, 500);
                    }
                });
                if (activity instanceof BaseActivity) {
                    FragmentTransaction ft = ((BaseActivity) activity).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    dialog.setCancelable(true);
                    dialog.setData(answerData);
                    dialog.show(ft, "DetailDialog");
                }
            }
        });
    }
}
