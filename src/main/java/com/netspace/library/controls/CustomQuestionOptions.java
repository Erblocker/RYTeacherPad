package com.netspace.library.controls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class CustomQuestionOptions extends LinearLayout implements OnClickListener {
    private Button mButtonOptions;
    private Context mContext;
    private ImageButton mImageButtonDelete;
    private OnClickListener mOnDeleteListener;
    private OnClickListener mOnSelectedListener;
    private boolean mbSelected = false;
    private String mszOptions;

    public CustomQuestionOptions(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customquestionoptions, this);
        this.mContext = context;
        this.mImageButtonDelete = (ImageButton) findViewById(R.id.imageButtonDelete);
        this.mButtonOptions = (Button) findViewById(R.id.buttonOptions);
        this.mButtonOptions.setBackgroundResource(R.drawable.button_selector);
        this.mImageButtonDelete.setOnClickListener(this);
        this.mButtonOptions.setOnClickListener(this);
        this.mImageButtonDelete.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_times).colorRes(R.color.toolbar).actionBarSize());
    }

    public void setCanDelete(boolean bCanDelete) {
        LayoutParams params = (LayoutParams) this.mButtonOptions.getLayoutParams();
        if (bCanDelete) {
            this.mImageButtonDelete.setVisibility(0);
            params.rightMargin = 0;
        } else {
            this.mImageButtonDelete.setVisibility(8);
            params.rightMargin = Utilities.dpToPixel(5, this.mContext);
        }
        this.mButtonOptions.setLayoutParams(params);
    }

    public void setOnSelectedListener(OnClickListener OnClickListener) {
        this.mOnSelectedListener = OnClickListener;
    }

    public void setOnDeleteClickListener(OnClickListener OnClickListener) {
        if (OnClickListener != null) {
            this.mOnDeleteListener = OnClickListener;
            this.mImageButtonDelete.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    CustomQuestionOptions.this.mOnDeleteListener.onClick(CustomQuestionOptions.this);
                }
            });
        }
    }

    public void setOptions(String szValue) {
        this.mszOptions = szValue;
        this.mButtonOptions.setText(this.mszOptions);
    }

    public void setSelected(boolean bSelected) {
        this.mbSelected = bSelected;
        this.mButtonOptions.setSelected(this.mbSelected);
    }

    public void onClick(View v) {
        boolean z = false;
        if (v.getId() == R.id.buttonOptions) {
            if (this.mbSelected) {
                this.mButtonOptions.setSelected(false);
            } else {
                this.mButtonOptions.setSelected(true);
                if (this.mOnSelectedListener != null) {
                    this.mOnSelectedListener.onClick(this);
                }
            }
            if (!this.mbSelected) {
                z = true;
            }
            this.mbSelected = z;
        }
    }

    public String getValue() {
        return this.mszOptions;
    }

    public boolean isSelected() {
        return this.mbSelected;
    }
}
