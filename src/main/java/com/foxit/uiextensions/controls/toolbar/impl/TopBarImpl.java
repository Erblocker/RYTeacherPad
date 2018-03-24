package com.foxit.uiextensions.controls.toolbar.impl;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;

public class TopBarImpl extends BaseBarImpl {
    protected int mLeftSideInterval = 16;
    protected int mRightSideInterval = 9;
    protected int mShadowLineHeight = 3;
    protected int mSolidLineHeight = 1;
    private LinearLayout mTopBarRealRootLayout = null;
    protected int mWide = 56;

    public TopBarImpl(Context context) {
        super(context, 0);
        initDimens();
        refreshLayout();
        initOrientation(0, -1, this.mWide);
    }

    public View getContentView() {
        if (this.mOrientation == 0 && this.mTopBarRealRootLayout == null) {
            this.mTopBarRealRootLayout = new LinearLayout(this.mRootLayout.getContext());
            this.mTopBarRealRootLayout.setOrientation(1);
            this.mTopBarRealRootLayout.addView(this.mRootLayout);
            View solidLine = new View(this.mRootLayout.getContext());
            solidLine.setBackgroundColor(this.mRootLayout.getContext().getResources().getColor(R.color.ux_color_shadow_solid_line));
            this.mTopBarRealRootLayout.addView(solidLine);
            solidLine.getLayoutParams().width = -1;
            solidLine.getLayoutParams().height = dip2px_fromDimens(this.mSolidLineHeight);
            View shadowLine = new View(this.mRootLayout.getContext());
            shadowLine.setBackgroundResource(R.drawable.toolbar_shadow_top);
            this.mTopBarRealRootLayout.addView(shadowLine);
            shadowLine.getLayoutParams().width = -1;
            shadowLine.getLayoutParams().height = dip2px_fromDimens(this.mShadowLineHeight);
        }
        if (!this.mInterval) {
            if (this.mOrientation == 0) {
                this.mRootLayout.setPadding(dip2px_fromDimens(this.mLeftSideInterval), 0, dip2px_fromDimens(this.mRightSideInterval), 0);
            } else {
                this.mRootLayout.setPadding(0, dip2px_fromDimens(this.mLeftSideInterval), 0, dip2px_fromDimens(this.mRightSideInterval));
            }
        }
        if (this.mOrientation != 0 || this.mTopBarRealRootLayout == null) {
            return this.mRootLayout;
        }
        return this.mTopBarRealRootLayout;
    }

    public void addView(BaseItem item, TB_Position position) {
        super.addView(item, position);
        if (!this.mInterval) {
            if (this.mOrientation == 0) {
                this.mRootLayout.setPadding(dip2px_fromDimens(this.mLeftSideInterval), 0, dip2px_fromDimens(this.mRightSideInterval), 0);
            } else {
                this.mRootLayout.setPadding(0, dip2px_fromDimens(this.mLeftSideInterval), 0, dip2px_fromDimens(this.mRightSideInterval));
            }
        }
    }

    private void initDimens() {
        try {
            if (this.mIsPad) {
                this.mWide = this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_toolbar_height_pad);
            } else {
                this.mWide = this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_toolbar_height_phone);
            }
        } catch (Exception e) {
            this.mWide = dip2px(this.mWide);
        }
        try {
            this.mSolidLineHeight = this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_toolbar_solidLine_height);
        } catch (Exception e2) {
            this.mSolidLineHeight = dip2px(this.mSolidLineHeight);
        }
        try {
            this.mShadowLineHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.ux_shadow_height);
        } catch (Exception e3) {
            this.mShadowLineHeight = dip2px(this.mShadowLineHeight);
        }
        try {
            if (this.mIsPad) {
                this.mLeftSideInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
            } else {
                this.mLeftSideInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
            }
        } catch (Exception e4) {
            this.mLeftSideInterval = dip2px(this.mLeftSideInterval);
        }
        try {
            if (this.mIsPad) {
                this.mRightSideInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_pad);
            } else {
                this.mRightSideInterval = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_phone);
            }
        } catch (Exception e5) {
            this.mRightSideInterval = dip2px(this.mRightSideInterval);
        }
    }
}
