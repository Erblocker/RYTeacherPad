package com.netspace.library.adapter;

import android.content.Context;
import android.support.v4.internal.view.SupportMenu;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.controls.CustomSelectorView;
import com.netspace.library.controls.CustomSelectorView.CustomSelectorViewCallBack;
import com.netspace.library.service.AnswerSheetV3MenuService.AnswerSheetDataChanged;
import com.netspace.library.struct.AnswerSheetV2QuestionItem;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import org.greenrobot.eventbus.EventBus;

public class AnswerSheetV2QuestionListAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater;
    private ArrayList<AnswerSheetV2QuestionItem> m_arrData;
    private boolean m_bLocked = false;

    public AnswerSheetV2QuestionListAdapter(Context context, ArrayList<AnswerSheetV2QuestionItem> arrData) {
        this.m_Context = context;
        this.m_arrData = arrData;
        this.m_LayoutInflater = (LayoutInflater) this.m_Context.getSystemService("layout_inflater");
    }

    public void setLocked(boolean bLocked) {
        this.m_bLocked = bLocked;
    }

    public int getCount() {
        return this.m_arrData.size();
    }

    public Object getItem(int position) {
        return this.m_arrData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final AnswerSheetV2QuestionItem Data = (AnswerSheetV2QuestionItem) this.m_arrData.get(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_answersheetv2_item, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
        TextView textQuestionIndex = (TextView) convertView.findViewById(R.id.textViewQuestionIndex);
        TextView textScore = (TextView) convertView.findViewById(R.id.textViewQuestionScore);
        ImageView ImageViewCorrectResult = (ImageView) convertView.findViewById(R.id.imageViewCorrectResult);
        ImageView ImageViewCamera = (ImageView) convertView.findViewById(R.id.imageViewCamera);
        ImageView ImageViewDraw = (ImageView) convertView.findViewById(R.id.imageViewDraw);
        ImageView ImageViewText = (ImageView) convertView.findViewById(R.id.imageViewText);
        LinearLayout LayoutContent = (LinearLayout) convertView.findViewById(R.id.layoutContent);
        HorizontalScrollView horizontalScrollView = (HorizontalScrollView) convertView.findViewById(R.id.horizontalScrollView1);
        ImageViewCamera.setVisibility(0);
        ImageViewDraw.setVisibility(0);
        ImageViewText.setVisibility(0);
        ImageViewCorrectResult.setVisibility(0);
        textTitle.setVisibility(0);
        final int nPosition = position;
        final ListView listView = (ListView) parent;
        LayoutContent.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                listView.performItemClick(v, nPosition, 0);
            }
        });
        if (LayoutContent.getChildCount() > 3) {
            LayoutContent.removeViewAt(3);
        }
        textQuestionIndex.setText(Data.szIndex);
        textQuestionIndex.setVisibility(0);
        if (Data.szGuid.isEmpty()) {
            ImageViewCamera.setVisibility(8);
            ImageViewDraw.setVisibility(8);
            ImageViewText.setVisibility(8);
            ImageViewCorrectResult.setVisibility(8);
            textTitle.setText(Data.szTitle);
            textTitle.setVisibility(0);
            textScore.setVisibility(8);
            ImageViewCorrectResult.setVisibility(8);
        } else {
            if (Data.nAnswerResult == 0) {
                textScore.setText("(" + String.valueOf(Data.fFullScore) + ")");
            } else {
                textScore.setText("(+" + String.valueOf(Data.fAnswerScore) + ")");
            }
            textScore.setVisibility(0);
            boolean bShowPlaceHolder = true;
            textTitle.setVisibility(8);
            ImageViewCamera.setVisibility(8);
            ImageViewDraw.setVisibility(8);
            ImageViewText.setVisibility(8);
            ImageViewCorrectResult.setVisibility(8);
            if (!Data.szAnswer0.isEmpty()) {
                ImageViewText.setVisibility(0);
                bShowPlaceHolder = false;
            }
            if (!Data.szAnswer1.isEmpty()) {
                ImageViewDraw.setVisibility(0);
                bShowPlaceHolder = false;
            }
            if (!Data.szAnswer2.isEmpty()) {
                ImageViewCamera.setVisibility(0);
                bShowPlaceHolder = false;
            }
            if (Data.nAnswerResult != 0) {
                bShowPlaceHolder = false;
                if (Data.nType == 0 || Data.nType == 1 || Data.nType == 2) {
                    String szText = Data.szAnswer;
                    SpannableString msp = new SpannableString(szText);
                    if (!(Data.nAnswerResult == 0 || Data.szAnswer.equalsIgnoreCase(Data.szCorrectAnswer))) {
                        szText = new StringBuilder(String.valueOf(szText)).append(" ").append(Data.szCorrectAnswer).toString();
                        msp = new SpannableString(szText);
                        msp.setSpan(new StrikethroughSpan(), 0, Data.szAnswer.length(), 33);
                        msp.setSpan(new ForegroundColorSpan(-16711936), Data.szAnswer.length() + 1, szText.length(), 33);
                        msp.setSpan(new ForegroundColorSpan(SupportMenu.CATEGORY_MASK), 0, Data.szAnswer.length(), 33);
                    }
                    textTitle.setText(msp);
                    textTitle.setVisibility(0);
                }
            } else if (Data.nType == 0 || Data.nType == 1 || Data.nType == 2) {
                bShowPlaceHolder = false;
                CustomSelectorView SelectorView = new CustomSelectorView(this.m_Context);
                SelectorView.setSize(Utilities.dpToPixel(30, this.m_Context), Utilities.dpToPixel(30, this.m_Context));
                SelectorView.setTextSize(15.0f);
                for (int i = 0; i < Data.szOptions.length(); i++) {
                    SelectorView.addOptions(String.valueOf(Data.szOptions.charAt(i)));
                }
                if (Data.nType == 2) {
                    SelectorView.setMultiableSelect(true);
                } else {
                    SelectorView.setMultiableSelect(false);
                }
                LayoutContent.addView(SelectorView, -2, Utilities.dpToPixel(30, this.m_Context));
                SelectorView.setVisibility(0);
                SelectorView.putValue(Data.szAnswer);
                SelectorView.setCallBack(new CustomSelectorViewCallBack() {
                    public void onChange(CustomSelectorView view) {
                        Data.szAnswer = view.getValue();
                        EventBus.getDefault().post(new AnswerSheetDataChanged());
                    }
                });
                if (this.m_bLocked) {
                    SelectorView.setLocked(this.m_bLocked);
                }
            }
            if (bShowPlaceHolder) {
                textTitle.setVisibility(0);
                textTitle.setText("<点击这里进行作答>");
            }
        }
        if (Data.nAnswerResult != 0) {
            if (Data.nAnswerResult == -1) {
                ImageViewCorrectResult.setImageResource(R.drawable.ic_resource_wrong);
            } else if (Data.nAnswerResult == 1) {
                ImageViewCorrectResult.setImageResource(R.drawable.ic_resource_halfcorrect);
            } else if (Data.nAnswerResult == 2) {
                ImageViewCorrectResult.setImageResource(R.drawable.ic_resource_correct);
            }
            ImageViewCorrectResult.setVisibility(0);
        } else {
            ImageViewCorrectResult.setVisibility(8);
        }
        return convertView;
    }
}
