package com.foxit.uiextensions.controls.toolbar.impl;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import com.foxit.uiextensions.R;

public class BottomBarImpl extends BaseBarImpl {
    private LinearLayout mBottomBarRealRootLayout = null;
    protected int mShadowLineHeight = 3;
    protected int mSidesInterval = 16;
    protected int mSolidLineHeight = 1;

    public BottomBarImpl(Context context) {
        super(context, 0);
        initDimens();
    }

    public View getContentView() {
        if (this.mOrientation == 0 && this.mBottomBarRealRootLayout == null) {
            this.mBottomBarRealRootLayout = new LinearLayout(this.mRootLayout.getContext());
            this.mBottomBarRealRootLayout.setOrientation(1);
            View shadowLine = new View(this.mRootLayout.getContext());
            shadowLine.setBackgroundResource(R.drawable.toolbar_shadow_bottom);
            this.mBottomBarRealRootLayout.addView(shadowLine);
            shadowLine.getLayoutParams().width = -1;
            shadowLine.getLayoutParams().height = dip2px_fromDimens(this.mShadowLineHeight);
            View solidLine = new View(this.mRootLayout.getContext());
            solidLine.setBackgroundColor(this.mRootLayout.getContext().getResources().getColor(R.color.ux_color_shadow_solid_line));
            this.mBottomBarRealRootLayout.addView(solidLine);
            solidLine.getLayoutParams().width = -1;
            solidLine.getLayoutParams().height = dip2px_fromDimens(this.mSolidLineHeight);
            this.mBottomBarRealRootLayout.addView(this.mRootLayout);
        }
        if (!this.mInterval) {
            if (this.mOrientation == 0) {
                this.mRootLayout.setPadding(dip2px_fromDimens(this.mSidesInterval), 0, dip2px_fromDimens(this.mSidesInterval), 0);
            } else {
                this.mRootLayout.setPadding(0, dip2px_fromDimens(this.mSidesInterval), 0, dip2px_fromDimens(this.mSidesInterval));
            }
        }
        if (this.mOrientation != 0 || this.mBottomBarRealRootLayout == null) {
            return this.mRootLayout;
        }
        return this.mBottomBarRealRootLayout;
    }

    private void initDimens() {
        try {
            this.mSolidLineHeight = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_solidLine_height);
        } catch (Exception e) {
            this.mSolidLineHeight = dip2px(this.mSolidLineHeight);
        }
        try {
            this.mShadowLineHeight = (int) this.mContext.getResources().getDimension(R.dimen.ux_shadow_height);
        } catch (Exception e2) {
            this.mShadowLineHeight = dip2px(this.mShadowLineHeight);
        }
        try {
            this.mSidesInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_text_icon_distance_phone);
        } catch (Exception e3) {
            this.mSidesInterval = dip2px(this.mSidesInterval);
        }
    }
}
