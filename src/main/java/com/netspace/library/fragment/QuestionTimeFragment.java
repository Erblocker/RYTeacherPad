package com.netspace.library.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import org.apache.http.HttpStatus;

public class QuestionTimeFragment extends Fragment implements OnClickListener {
    private View mLastRadioButton;
    private RelativeLayout mRootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_questiontime, null);
        for (int i = 0; i < this.mRootView.getChildCount(); i++) {
            View oneView = this.mRootView.getChildAt(i);
            if (oneView instanceof RadioButton) {
                ((RadioButton) oneView).setOnClickListener(this);
            }
        }
        onClick(this.mRootView.getChildAt(0));
        return this.mRootView;
    }

    public String repackIMCommand(String szOldCommand) {
        if (this.mLastRadioButton.getId() == R.id.radioButtonFast) {
            return "CountDown 5\n" + szOldCommand;
        }
        return szOldCommand;
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
}
