package com.foxit.uiextensions.annots.form;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.utils.AppDisplay;

public class FormNavigationModule implements Module {
    private TextView mClearView;
    private Context mContext;
    private TextView mFinishView;
    private RelativeLayout mFormNavigationLayout;
    private ImageView mNextView;
    private ViewGroup mParent;
    private ImageView mPreView;

    public FormNavigationModule(Context context, ViewGroup parent) {
        this.mContext = context;
        this.mParent = parent;
    }

    public String getName() {
        return Module.MODULE_NAME_FORM_NAVIGATION;
    }

    public boolean loadModule() {
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            this.mFormNavigationLayout = (RelativeLayout) View.inflate(this.mContext, R.layout.rv_form_navigation_pad, null);
        } else {
            this.mFormNavigationLayout = (RelativeLayout) View.inflate(this.mContext, R.layout.rv_form_navigation_phone, null);
        }
        this.mPreView = (ImageView) this.mFormNavigationLayout.findViewById(R.id.rv_form_pre);
        this.mNextView = (ImageView) this.mFormNavigationLayout.findViewById(R.id.rv_form_next);
        this.mClearView = (TextView) this.mFormNavigationLayout.findViewById(R.id.rv_form_clear);
        this.mFinishView = (TextView) this.mFormNavigationLayout.findViewById(R.id.rv_form_finish);
        LayoutParams lp = new LayoutParams(-2, -2);
        lp.addRule(12);
        this.mFormNavigationLayout.setPadding(0, 0, 0, 0);
        this.mFormNavigationLayout.setVisibility(4);
        this.mFormNavigationLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
        this.mParent.addView(this.mFormNavigationLayout, lp);
        return true;
    }

    public boolean unloadModule() {
        this.mParent.removeView(this.mFormNavigationLayout);
        return true;
    }

    public RelativeLayout getLayout() {
        return this.mFormNavigationLayout;
    }

    public ImageView getPreView() {
        return this.mPreView;
    }

    public ImageView getNextView() {
        return this.mNextView;
    }

    public TextView getClearView() {
        return this.mClearView;
    }

    public TextView getFinishView() {
        return this.mFinishView;
    }

    public void setClearEnable(boolean enable) {
        if (enable) {
            this.mClearView.setEnabled(true);
        } else {
            this.mClearView.setEnabled(false);
        }
    }
}
