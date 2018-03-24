package com.foxit.uiextensions.controls.toolbar.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.internal.view.SupportMenu;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.toolbar.PropertyCircleItem;
import com.foxit.uiextensions.utils.AppDisplay;

public class PropertyCircleItemImp extends CircleItemImpl implements PropertyCircleItem {
    private PropertyCircle mPropertyCircle;
    private LayoutParams mPropertyCircleLayoutParams = new LayoutParams(-2, -2);

    private class PropertyCircle extends View {
        private int mColor = SupportMenu.CATEGORY_MASK;
        private Paint mPaint = new Paint();
        private float mRadius;

        public PropertyCircle(Context context) {
            super(context);
            this.mRadius = (float) new AppDisplay(context).dp2px(30.0f);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStrokeWidth(1.0f);
        }

        public void setColor(int color) {
            this.mColor = color;
            invalidate();
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            PropertyCircleItemImp.this.mContentImageView.measure(0, 0);
            getLayoutParams().width = PropertyCircleItemImp.this.mContentImageView.getMeasuredWidth();
            getLayoutParams().height = PropertyCircleItemImp.this.mContentImageView.getMeasuredHeight();
        }

        protected void onDraw(Canvas canvas) {
            this.mPaint.setColor(this.mColor);
            this.mRadius = (float) ((int) ((((float) getWidth()) / 2.0f) - 0.1f));
            canvas.drawCircle(((float) getWidth()) / 2.0f, ((float) getHeight()) / 2.0f, this.mRadius, this.mPaint);
            super.onDraw(canvas);
        }
    }

    public PropertyCircleItemImp(Context context) {
        super(context);
        this.mPropertyCircle = new PropertyCircle(context);
        this.mPropertyCircleLayoutParams.addRule(13);
        this.mCircleLayout.addView(this.mPropertyCircle, this.mCircleLayout.getChildCount() - 1, this.mPropertyCircleLayoutParams);
        this.mContentImageView.setImageResource(R.drawable.annot_propertycircleitem_selector);
    }

    public void setCentreCircleColor(int color) {
        this.mPropertyCircle.setColor(color);
    }

    public boolean setImageResource(int res) {
        return false;
    }
}
