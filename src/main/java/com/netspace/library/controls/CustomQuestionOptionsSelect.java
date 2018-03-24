package com.netspace.library.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.pad.library.R;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class CustomQuestionOptionsSelect extends LinearLayout implements OnClickListener {
    private Context mContext;
    private ImageButton mImageButtonAdd;
    private LinearLayout mLayoutOptions;
    private ArrayList<CustomQuestionOptions> marrOptions = new ArrayList();
    private boolean mbCanModify = true;
    private boolean mbSingleSelect = false;
    private String mszCorrectAnswer;

    public CustomQuestionOptionsSelect(Context context) {
        super(context);
        initView();
    }

    public CustomQuestionOptionsSelect(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomQuestionOptionsSelect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public CustomQuestionOptionsSelect(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    public void initView() {
        this.mContext = getContext();
        ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.layout_customquestionoptionsselect, this);
        this.mImageButtonAdd = (ImageButton) findViewById(R.id.imageButtonAdd);
        this.mImageButtonAdd.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_plus).colorRes(R.color.toolbar).actionBarSize());
        this.mImageButtonAdd.setOnClickListener(this);
        this.mLayoutOptions = (LinearLayout) findViewById(R.id.layoutOptions);
    }

    public void setSingleSelect(boolean bSingleSelect) {
        this.mbSingleSelect = bSingleSelect;
    }

    public void setCanModify(boolean bCanModify) {
        if (this.mLayoutOptions.getChildCount() > 1) {
            throw new InvalidParameterException("setCanModify must be set before any options is add.");
        }
        this.mbCanModify = bCanModify;
        this.mImageButtonAdd.setVisibility(this.mbCanModify ? 0 : 8);
    }

    public void setCorrectAnswer(String szAnswer) {
        if (this.mLayoutOptions.getChildCount() > 1) {
            throw new InvalidParameterException("setCorrectAnswer must be set before any options is add.");
        }
        this.mszCorrectAnswer = szAnswer;
    }

    public void addOption(String szValue) {
        CustomQuestionOptions CustomQuestionOptions = new CustomQuestionOptions(this.mContext);
        CustomQuestionOptions.setOptions(szValue);
        CustomQuestionOptions.setCanDelete(this.mbCanModify);
        if (!(this.mszCorrectAnswer == null || this.mszCorrectAnswer.indexOf(szValue) == -1)) {
            CustomQuestionOptions.setSelected(true);
        }
        CustomQuestionOptions.setOnDeleteClickListener(new OnClickListener() {
            public void onClick(View v) {
                CustomQuestionOptionsSelect.this.mLayoutOptions.removeView(v);
                CustomQuestionOptionsSelect.this.marrOptions.remove(v);
            }
        });
        CustomQuestionOptions.setOnSelectedListener(new OnClickListener() {
            public void onClick(View v) {
                if (CustomQuestionOptionsSelect.this.mbSingleSelect) {
                    CustomQuestionOptionsSelect.this.unSelectOthers(v);
                }
            }
        });
        this.marrOptions.add(CustomQuestionOptions);
        this.mLayoutOptions.addView(CustomQuestionOptions, this.mLayoutOptions.getChildCount() - 1);
    }

    private void unSelectOthers(View viewSelected) {
        for (int i = 0; i < this.marrOptions.size(); i++) {
            CustomQuestionOptions oneOptions = (CustomQuestionOptions) this.marrOptions.get(i);
            if (!oneOptions.equals(viewSelected)) {
                oneOptions.setSelected(false);
            }
        }
    }

    public String getOptions(String szSepChar) {
        String szResult = "";
        for (int i = 0; i < this.marrOptions.size(); i++) {
            CustomQuestionOptions oneOptions = (CustomQuestionOptions) this.marrOptions.get(i);
            if (!szResult.isEmpty()) {
                szResult = new StringBuilder(String.valueOf(szResult)).append(szSepChar).toString();
            }
            szResult = new StringBuilder(String.valueOf(szResult)).append(oneOptions.getValue()).toString();
        }
        return szResult;
    }

    public String getSelectedOptions(String szSepChar) {
        String szResult = "";
        for (int i = 0; i < this.marrOptions.size(); i++) {
            CustomQuestionOptions oneOptions = (CustomQuestionOptions) this.marrOptions.get(i);
            if (oneOptions.isSelected()) {
                if (!szResult.isEmpty()) {
                    szResult = new StringBuilder(String.valueOf(szResult)).append(szSepChar).toString();
                }
                szResult = new StringBuilder(String.valueOf(szResult)).append(oneOptions.getValue()).toString();
            }
        }
        return szResult;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.imageButtonAdd) {
            String szCurrentOptions = getOptions("");
            String szAllChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            for (int i = 0; i < szAllChar.length(); i++) {
                if (szCurrentOptions.indexOf(szAllChar.charAt(i)) == -1) {
                    addOption(String.valueOf(szAllChar.charAt(i)));
                    return;
                }
            }
        }
    }
}
