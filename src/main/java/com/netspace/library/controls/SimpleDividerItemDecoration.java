package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;
import com.netspace.pad.library.R;

public class SimpleDividerItemDecoration extends ItemDecoration {
    private Drawable mDivider;

    public SimpleDividerItemDecoration(Context context) {
        this.mDivider = context.getResources().getDrawable(R.drawable.line_divider);
    }

    public void onDrawOver(Canvas c, RecyclerView parent, State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int top = child.getBottom() + ((LayoutParams) child.getLayoutParams()).bottomMargin;
            this.mDivider.setBounds(left, top, right, top + this.mDivider.getIntrinsicHeight());
            this.mDivider.draw(c);
        }
    }
}
