package com.foxit.home.local;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import com.foxit.app.App;
import com.foxit.home.R;

class HM_LocalView extends RelativeLayout {
    private RelativeLayout mBottomLayout;
    private View mFileView;
    private RelativeLayout mTopLayout;
    private View mTopLayoutDivider = new View(getContext());

    public HM_LocalView(Context context) {
        super(context);
        setLayoutParams(new LayoutParams(-1, -1));
        initTopLayout();
        initBottomLayout();
        this.mTopLayoutDivider.setBackgroundColor(getResources().getColor(R.color.ux_color_seperator_gray));
        RelativeLayout.LayoutParams dividerParams = new RelativeLayout.LayoutParams(-1, App.instance().getDisplay().dp2px(1.0f));
        dividerParams.addRule(12);
        this.mTopLayoutDivider.setLayoutParams(dividerParams);
    }

    private void initTopLayout() {
        RelativeLayout.LayoutParams params;
        this.mTopLayout = new RelativeLayout(getContext());
        this.mTopLayout.setId(R.id.fb_local_view_top);
        this.mTopLayout.setBackgroundColor(getResources().getColor(R.color.ux_color_white));
        if (App.instance().getDisplay().isPad()) {
            params = new RelativeLayout.LayoutParams(-1, (int) getResources().getDimension(R.dimen.ux_list_item_height_1l_phone));
        } else {
            params = new RelativeLayout.LayoutParams(-1, (int) getResources().getDimension(R.dimen.ux_list_item_height_1l_phone));
        }
        addView(this.mTopLayout, params);
        this.mTopLayout.setGravity(15);
    }

    private void initBottomLayout() {
        this.mBottomLayout = new RelativeLayout(getContext());
        this.mBottomLayout.setId(R.id.fb_local_view_bottom);
        this.mBottomLayout.setVisibility(0);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
        params.addRule(12);
        addView(this.mBottomLayout, params);
        this.mBottomLayout.setGravity(15);
    }

    void setTopLayoutVisible(boolean visible) {
        this.mTopLayout.setVisibility(visible ? 0 : 8);
    }

    void addPathView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
        params.addRule(9);
        params.addRule(15);
        this.mTopLayout.addView(view, params);
    }

    void removeAllTopView() {
        this.mTopLayout.removeAllViews();
        this.mTopLayout.addView(this.mTopLayoutDivider);
    }

    void setBottomLayoutVisible(boolean visible) {
        this.mBottomLayout.setVisibility(visible ? 0 : 8);
    }

    void addFileView(View view) {
        if (!(this.mFileView == null || this.mFileView.getParent() == null)) {
            removeView(this.mFileView);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
        params.addRule(3, this.mTopLayout.getId());
        params.addRule(2, this.mBottomLayout.getId());
        addView(view, params);
        this.mFileView = view;
    }
}
