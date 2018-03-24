package com.netspace.library.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.LinedEditText;
import com.netspace.library.struct.UserAnswerSheetImage;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class UserAnswerSheetImageAdapter extends ArrayAdapter<UserAnswerSheetImage> {
    private OnLongClickListener mLongClickListener;
    private OnClickListener mOnClickListener;
    private String mScheduleGUID = "";
    private LayoutInflater m_LayoutInflater;
    private int mnCallCount = 0;

    public UserAnswerSheetImageAdapter(Context context, ArrayList<UserAnswerSheetImage> images, OnLongClickListener OnLongClickListener, OnClickListener OnClickListener) {
        super(context, 0, images);
        this.mLongClickListener = OnLongClickListener;
        this.mOnClickListener = OnClickListener;
        this.m_LayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public void setScheduleGUID(String szScheduleGUID) {
        this.mScheduleGUID = szScheduleGUID;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final UserAnswerSheetImage info = (UserAnswerSheetImage) getItem(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_useranswersheet, parent, false);
        }
        this.mnCallCount++;
        Log.d("UserAnswerSheetImageAdapter", "getView Call Count=" + this.mnCallCount);
        TextView textView = (TextView) convertView.findViewById(R.id.textViewUserName);
        ImageView answerImage = (ImageView) convertView.findViewById(R.id.imageViewThumbnil);
        LinedEditText editText = (LinedEditText) convertView.findViewById(R.id.editText1);
        final Spinner spinner = (Spinner) convertView.findViewById(R.id.spinner1);
        String szURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/DataSynchronizeGetSingleData?clientid=" + info.szClientID + "&packageid=";
        answerImage.setVisibility(4);
        editText.setVisibility(4);
        textView.setText(info.szRealName);
        editText.setFocusable(false);
        final ArrayList<String> arrScores = new ArrayList();
        int nSelectedIndex = 0;
        for (float fScore = 0.0f; fScore <= info.nFullScore; fScore += 1.0f) {
            arrScores.add("得" + String.valueOf((int) fScore) + "分");
            if (fScore == info.nAnswerScore) {
                nSelectedIndex = arrScores.size() - 1;
            }
        }
        String[] arrStringObjects = new String[arrScores.size()];
        arrScores.toArray(arrStringObjects);
        spinner.setAdapter(new ArrayAdapter(getContext(), R.layout.simple_list_item_smallpad, 16908308, arrStringObjects));
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String szScoreText = (String) arrScores.get(position);
                szScoreText = szScoreText.substring(1, szScoreText.length() - 1);
                info.nAnswerScore = Float.valueOf(szScoreText).floatValue();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinner.setSelection(nSelectedIndex);
        if (!info.szAnswer0.isEmpty()) {
            editText.setVisibility(0);
            editText.setText(info.szAnswer0);
            editText.setTextSize(9.0f);
        } else if (!info.szAnswer1Preview.isEmpty()) {
            szURL = new StringBuilder(String.valueOf(szURL)).append(info.szAnswer1Preview).toString();
            answerImage.setVisibility(0);
            Picasso.with(getContext()).load(szURL).resize(0, 500).into(answerImage);
        } else if (!info.szAnswer2.isEmpty()) {
            szURL = new StringBuilder(String.valueOf(szURL)).append(info.szAnswer2).toString();
            answerImage.setVisibility(0);
            Picasso.with(getContext()).load(szURL).resize(0, 500).into(answerImage);
        }
        answerImage.setTag(info);
        answerImage.setOnClickListener(this.mOnClickListener);
        answerImage.setOnLongClickListener(this.mLongClickListener);
        final ImageView correctButton = (ImageView) convertView.findViewById(R.id.imageViewCorrect);
        final ImageView halfcorrectButton = (ImageView) convertView.findViewById(R.id.imageViewHalfCorrect);
        final ImageView wrongButton = (ImageView) convertView.findViewById(R.id.imageViewWrong);
        OnClickListener clickListener = new OnClickListener() {
            public void onClick(View v) {
                correctButton.setBackgroundColor(0);
                halfcorrectButton.setBackgroundColor(0);
                wrongButton.setBackgroundColor(0);
                v.setBackgroundColor(-16776978);
                if (v.getId() == R.id.imageViewCorrect) {
                    info.nAnswerResult = 2;
                    spinner.setSelection(arrScores.size() - 1);
                } else if (v.getId() == R.id.imageViewHalfCorrect) {
                    info.nAnswerResult = 1;
                } else if (v.getId() == R.id.imageViewWrong) {
                    info.nAnswerResult = -1;
                    spinner.setSelection(0);
                }
            }
        };
        correctButton.setOnClickListener(clickListener);
        halfcorrectButton.setOnClickListener(clickListener);
        wrongButton.setOnClickListener(clickListener);
        if (convertView.getTag() == null) {
            WebServiceCallItemObject webServiceCallItemObject = new WebServiceCallItemObject("LessonsScheduleGetQuestionAnswer", null);
            final ImageView imageView = correctButton;
            final ImageView imageView2 = halfcorrectButton;
            final ImageView imageView3 = wrongButton;
            final ArrayList<String> arrayList = arrScores;
            final Spinner spinner2 = spinner;
            webServiceCallItemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    String szAnswerResult = (String) ItemObject.getParam("0");
                    int nScore = Utilities.toInt((String) ItemObject.getParam("2"));
                    int nAnswerResult = Utilities.toInt(szAnswerResult);
                    if (nAnswerResult == 2) {
                        imageView.setBackgroundColor(-16776978);
                    } else if (nAnswerResult == 1) {
                        imageView2.setBackgroundColor(-16776978);
                    } else if (nAnswerResult == -1) {
                        imageView3.setBackgroundColor(-16776978);
                    }
                    for (int i = 0; i < arrayList.size(); i++) {
                        String szScoreText = (String) arrayList.get(i);
                        if (Utilities.toInt(szScoreText.substring(1, szScoreText.length() - 1)) == nScore) {
                            spinner2.setSelection(i);
                        }
                    }
                }
            });
            webServiceCallItemObject = webServiceCallItemObject;
            webServiceCallItemObject.setParam("lpszLessonsScheduleGUID", this.mScheduleGUID);
            webServiceCallItemObject.setParam("lpszStudentID", info.szClientID.replace("myipad_", ""));
            webServiceCallItemObject.setParam("lpszObjectGUID", info.szQuestionGUID);
            webServiceCallItemObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(webServiceCallItemObject);
            convertView.setTag("Loaded");
        }
        return convertView;
    }
}
