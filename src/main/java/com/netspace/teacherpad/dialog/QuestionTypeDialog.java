package com.netspace.teacherpad.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;

public class QuestionTypeDialog extends Dialog implements OnClickListener {
    private int mOptions = 0;
    private String mszCorrectAnswer = "";
    private String mszQuestionType = "";

    public QuestionTypeDialog(Context context) {
        super(context);
    }

    public int getOptions() {
        return this.mOptions;
    }

    public String getQuestionType() {
        return this.mszQuestionType;
    }

    public String getCorrectAnswer() {
        return this.mszCorrectAnswer;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("试题类型");
        setContentView(R.layout.dialog_startquestion);
        findViewById(R.id.buttonOk).setOnClickListener(this);
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        ((Button) findViewById(R.id.buttonOk)).setText("确定");
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker1);
        np.setMaxValue(6);
        np.setMinValue(2);
        np.setWrapSelectorWheel(false);
        np.setValue(4);
        RadioGroup radgrp = (RadioGroup) findViewById(R.id.radioGroup1);
        radgrp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                TextView AnswerView = (TextView) QuestionTypeDialog.this.findViewById(R.id.editTextCorrectAnswer);
                if (checkedId == R.id.radioButton2) {
                    AnswerView.setHint("正确答案：“是”输入T，“否”输入F");
                    AnswerView.setVisibility(0);
                } else if (checkedId == R.id.radioButton3 || checkedId == R.id.radioButton4) {
                    AnswerView.setHint("在这里输入正确答案");
                    AnswerView.setVisibility(0);
                } else {
                    AnswerView.setVisibility(8);
                }
            }
        });
        radgrp.check(R.id.radioButton1);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.buttonOk) {
            int nID = ((RadioGroup) findViewById(R.id.radioGroup1)).getCheckedRadioButtonId();
            if (nID == R.id.radioButton1) {
                this.mszQuestionType = "主观题";
            } else if (nID == R.id.radioButton2) {
                this.mszQuestionType = "判断题";
                szCorrectAnswer = ((TextView) findViewById(R.id.editTextCorrectAnswer)).getText().toString().toUpperCase();
                TeacherPadApplication.szCorrectAnswer = "";
                if (szCorrectAnswer.equalsIgnoreCase("T")) {
                    this.mszCorrectAnswer = "是";
                } else if (szCorrectAnswer.equalsIgnoreCase("F")) {
                    this.mszCorrectAnswer = "否";
                }
            } else if (nID == R.id.radioButton3 || nID == R.id.radioButton4) {
                int i;
                int nSelectValue = ((NumberPicker) findViewById(R.id.numberPicker1)).getValue();
                String szValue = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                String szVoteValue = "";
                this.mOptions = nSelectValue;
                for (i = 0; i < nSelectValue; i++) {
                    if (!szVoteValue.isEmpty()) {
                        szVoteValue = new StringBuilder(String.valueOf(szVoteValue)).append(",").toString();
                    }
                    szVoteValue = new StringBuilder(String.valueOf(szVoteValue)).append(szValue.substring(i, i + 1)).toString();
                }
                if (nID == R.id.radioButton3) {
                    this.mszQuestionType = "单选题";
                } else {
                    this.mszQuestionType = "不定项选择题";
                }
                szCorrectAnswer = ((TextView) findViewById(R.id.editTextCorrectAnswer)).getText().toString().toUpperCase();
                this.mszCorrectAnswer = "";
                for (i = 0; i < szValue.length(); i++) {
                    if (szCorrectAnswer.indexOf(szValue.charAt(i)) != -1) {
                        this.mszCorrectAnswer += szValue.charAt(i);
                    }
                }
            }
            dismiss();
        } else if (v.getId() == R.id.buttonCancel) {
            cancel();
        }
    }
}
