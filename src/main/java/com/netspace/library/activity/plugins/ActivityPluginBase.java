package com.netspace.library.activity.plugins;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.netspace.pad.library.R;

public class ActivityPluginBase {
    protected Activity mActivity;
    protected int mnFullScaleHeight = 0;
    protected int mnFullScaleWidth = 0;

    public ActivityPluginBase(Activity activity) {
        this.mActivity = activity;
    }

    public ViewGroup addButtonWithTooltip(int nButtonID, String szTooltip, ViewGroup root) {
        ViewGroup result = (ViewGroup) ((LayoutInflater) this.mActivity.getSystemService("layout_inflater")).inflate(R.layout.layout_imageviewwithtooltip, root);
        result = (ViewGroup) root.getChildAt(root.getChildCount() - 1);
        result.getChildAt(0).setId(nButtonID);
        ((TextView) result.getChildAt(1)).setText(szTooltip);
        return result;
    }

    public void setFullScaleSize(int nFullScaleWidth, int nFullScaleHeight) {
        this.mnFullScaleWidth = nFullScaleWidth;
        this.mnFullScaleHeight = nFullScaleHeight;
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void onDestroy() {
    }
}
