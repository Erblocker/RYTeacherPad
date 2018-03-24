package com.netspace.library.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import com.netspace.library.controls.CustomQuestionOptionsSelect;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import org.apache.http.HttpStatus;

public class QuestionTypeFragment extends Fragment implements OnClickListener {
    private CustomQuestionOptionsSelect mJudgement;
    private View mLastRadioButton;
    private CustomQuestionOptionsSelect mMulti;
    private int mQuestionType = 0;
    private RadioButton mRadioButtonAnswerSheet;
    private RelativeLayout mRootView;
    private CustomQuestionOptionsSelect mSingle;
    private String mszAnswerSheetCommand;
    private String mszCorrectAnswer = "";
    private String mszCurrentQuestionIMMessage = "";
    private String mszOldQuestionAnswer;
    private String mszOldQuestionIMCommand;
    private String mszQuestionGroupGUID;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        int i;
        this.mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_questiontype, null);
        this.mSingle = (CustomQuestionOptionsSelect) this.mRootView.findViewById(R.id.questionOptionsSelectSingle);
        this.mJudgement = (CustomQuestionOptionsSelect) this.mRootView.findViewById(R.id.questionOptionsSelectJudgement);
        this.mMulti = (CustomQuestionOptionsSelect) this.mRootView.findViewById(R.id.questionOptionsSelectMulti);
        this.mRadioButtonAnswerSheet = (RadioButton) this.mRootView.findViewById(R.id.radioButtonAnswerSheet);
        this.mRadioButtonAnswerSheet.setVisibility(8);
        String szDefaultOptions = "ABCD";
        int nDefaultRadioID = R.id.radioButtonHandWrite;
        if (!(this.mszOldQuestionIMCommand == null || this.mszOldQuestionIMCommand.isEmpty())) {
            this.mszOldQuestionIMCommand = this.mszOldQuestionIMCommand.replace("ScreenCopyAndSend", "");
            this.mszOldQuestionIMCommand = this.mszOldQuestionIMCommand.trim();
            String[] arrData = this.mszOldQuestionIMCommand.split(" ");
            if (arrData[0].equalsIgnoreCase("AnswerSheetQuestion")) {
                this.mRadioButtonAnswerSheet.setVisibility(0);
                nDefaultRadioID = R.id.radioButtonAnswerSheet;
                this.mszAnswerSheetCommand = this.mszOldQuestionIMCommand;
            } else if (arrData[0].equalsIgnoreCase("Vote")) {
                nDefaultRadioID = R.id.radioButtonMulti;
                szDefaultOptions = arrData[1].replace(",", "");
            } else if (arrData[0].equalsIgnoreCase("VoteSingle")) {
                if (arrData[1].indexOf("是") != -1) {
                    nDefaultRadioID = R.id.radioButtonJudgement;
                } else {
                    nDefaultRadioID = R.id.radioButtonSingleSelector;
                    szDefaultOptions = arrData[1].replace(",", "");
                }
            }
        }
        this.mSingle.setSingleSelect(true);
        if (!(this.mszOldQuestionAnswer == null || this.mszOldQuestionAnswer.isEmpty())) {
            if (this.mszOldQuestionAnswer.length() == 1) {
                this.mSingle.setCorrectAnswer(this.mszOldQuestionAnswer);
            }
            this.mMulti.setCorrectAnswer(this.mszOldQuestionAnswer);
            this.mJudgement.setCorrectAnswer(this.mszOldQuestionAnswer);
        }
        for (i = 0; i < szDefaultOptions.length(); i++) {
            this.mSingle.addOption(String.valueOf(szDefaultOptions.charAt(i)));
            this.mMulti.addOption(String.valueOf(szDefaultOptions.charAt(i)));
        }
        this.mJudgement.setSingleSelect(true);
        this.mJudgement.setCanModify(false);
        this.mJudgement.addOption("是");
        this.mJudgement.addOption("否");
        for (i = 0; i < this.mRootView.getChildCount(); i++) {
            View oneView = this.mRootView.getChildAt(i);
            if (oneView instanceof RadioButton) {
                RadioButton RadioButton = (RadioButton) oneView;
                RadioButton.setOnClickListener(this);
                if (nDefaultRadioID == RadioButton.getId()) {
                    onClick(RadioButton);
                }
            }
        }
        return this.mRootView;
    }

    public void prepareData() {
        if (this.mLastRadioButton.getId() == R.id.radioButtonHandWrite) {
            this.mszCurrentQuestionIMMessage = "ScreenCopyAndSend HandWrite";
            this.mszCorrectAnswer = "";
            this.mQuestionType = 4;
        } else if (this.mLastRadioButton.getId() == R.id.radioButtonJudgement) {
            this.mszCurrentQuestionIMMessage = "ScreenCopyAndSend VoteSingle " + this.mJudgement.getOptions(",");
            this.mszCorrectAnswer = this.mJudgement.getSelectedOptions("");
            this.mQuestionType = 0;
        } else if (this.mLastRadioButton.getId() == R.id.radioButtonSingleSelector) {
            this.mszCurrentQuestionIMMessage = "ScreenCopyAndSend VoteSingle " + this.mSingle.getOptions(",");
            this.mszCorrectAnswer = this.mSingle.getSelectedOptions("");
            this.mQuestionType = 1;
        } else if (this.mLastRadioButton.getId() == R.id.radioButtonMulti) {
            this.mszCurrentQuestionIMMessage = "ScreenCopyAndSend Vote " + this.mMulti.getOptions(",");
            this.mszCorrectAnswer = this.mMulti.getSelectedOptions("");
            this.mQuestionType = 2;
        } else if (this.mLastRadioButton.getId() == R.id.radioButtonAnswerSheet) {
            this.mszCurrentQuestionIMMessage = this.mszAnswerSheetCommand;
            this.mszCorrectAnswer = "";
            this.mQuestionType = 5;
        }
        if (this.mszQuestionGroupGUID != null) {
            this.mszCurrentQuestionIMMessage += " " + this.mszQuestionGroupGUID;
        }
    }

    public String getQuestionIMData() {
        return this.mszCurrentQuestionIMMessage;
    }

    public String getQuestionCorrectAnswer() {
        return this.mszCorrectAnswer;
    }

    public void setOldInfo(String szOldIMCommand, String szOldAnswer) {
        this.mszOldQuestionIMCommand = szOldIMCommand;
        this.mszOldQuestionAnswer = szOldAnswer;
    }

    public void onClick(View v) {
        if (this.mLastRadioButton == null || !this.mLastRadioButton.equals(v)) {
            this.mLastRadioButton = v;
            boolean bSkipNextControl = false;
            for (int i = 0; i < this.mRootView.getChildCount(); i++) {
                View oneView = this.mRootView.getChildAt(i);
                if (oneView instanceof RadioButton) {
                    RadioButton RadioButton = (RadioButton) oneView;
                    bSkipNextControl = false;
                    if (oneView.equals(v)) {
                        bSkipNextControl = true;
                        RadioButton.setChecked(true);
                    } else {
                        RadioButton.setChecked(false);
                    }
                } else if (bSkipNextControl) {
                    Utilities.fadeOutView(oneView, HttpStatus.SC_MULTIPLE_CHOICES);
                } else {
                    oneView.setVisibility(8);
                }
            }
        }
    }

    public int getQuestionType() {
        return this.mQuestionType;
    }

    public void setQuestionGroupGUID(String szQuestionGroupGUID) {
        this.mszQuestionGroupGUID = szQuestionGroupGUID;
    }
}
