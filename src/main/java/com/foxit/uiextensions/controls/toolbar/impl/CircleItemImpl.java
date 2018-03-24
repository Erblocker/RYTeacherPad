package com.foxit.uiextensions.controls.toolbar.impl;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.toolbar.BaseItem.ItemType;
import com.foxit.uiextensions.controls.toolbar.CircleItem;

public class CircleItemImpl extends BaseItemImpl implements CircleItem {
    protected ImageView mBackgroundImageView;
    protected LayoutParams mBackgroundLayoutParams = new LayoutParams(-2, -2);
    protected RelativeLayout mCircleLayout;
    protected LinearLayout.LayoutParams mCircleLayoutParams;
    protected ImageView mContentImageView;
    protected LayoutParams mContentLayoutParams;

    public CircleItemImpl(Context context) {
        super(context);
        this.mCircleLayout = new RelativeLayout(context);
        this.mBackgroundLayoutParams.addRule(13);
        this.mContentLayoutParams = new LayoutParams(-2, -2);
        this.mContentLayoutParams.addRule(13);
        this.mBackgroundImageView = new ImageView(context);
        this.mBackgroundImageView.setImageResource(R.drawable.tb_circle_background);
        this.mContentImageView = new ImageView(context);
        this.mCircleLayout.addView(this.mBackgroundImageView, this.mBackgroundLayoutParams);
        this.mCircleLayout.addView(this.mContentImageView, this.mContentLayoutParams);
        this.mCircleLayoutParams = new LinearLayout.LayoutParams(-2, -2);
        this.mCircleLayoutParams.gravity = 17;
        this.mRootLayout.addView(this.mCircleLayout, this.mCircleLayoutParams);
        this.mImage.setVisibility(8);
    }

    public View getContentView() {
        return this.mRootLayout;
    }

    public boolean setImageResource(int res) {
        this.mContentImageView.setImageResource(res);
        return true;
    }

    public void setContentView(View view) {
        if (view != null && this.mCircleLayout != null) {
            this.mCircleLayout.removeAllViews();
            this.mCircleLayout.addView(this.mBackgroundImageView);
            this.mCircleLayout.addView(view);
        }
    }

    public void setBackgroundResource(int res) {
        this.mRootLayout.setBackgroundResource(res);
    }

    public void setRelation(int relation) {
        this.mRelation = relation;
        this.mRootLayout.removeAllViews();
        setTextImgRelation(relation);
    }

    private void setTextImgRelation(int relation) {
        if (relation == 10 || relation == 12) {
            this.mRootLayout.setOrientation(0);
        } else {
            this.mRootLayout.setOrientation(1);
        }
        if (relation == 10 || relation == 11) {
            if (this.mTextView != null) {
                if (this.mTextParams != null) {
                    this.mRootLayout.addView(this.mTextView, this.mTextParams);
                } else {
                    this.mRootLayout.addView(this.mTextView);
                }
            }
            if (this.mCircleLayout != null) {
                this.mRootLayout.addView(this.mCircleLayout);
                return;
            }
            return;
        }
        if (this.mCircleLayout != null) {
            this.mRootLayout.addView(this.mCircleLayout);
        }
        if (this.mTextView == null) {
            return;
        }
        if (this.mTextParams != null) {
            this.mRootLayout.addView(this.mTextView, this.mTextParams);
        } else {
            this.mRootLayout.addView(this.mTextView);
        }
    }

    public void setEnable(boolean enable) {
        super.setEnable(enable);
        this.mCircleLayout.setEnabled(enable);
        this.mBackgroundImageView.setEnabled(enable);
        this.mContentImageView.setEnabled(enable);
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        this.mCircleLayout.setSelected(selected);
        this.mContentImageView.setSelected(selected);
    }

    public void setOnClickListener(OnClickListener l) {
        this.mRootLayout.setOnClickListener(l);
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.mRootLayout.setOnLongClickListener(l);
    }

    public void setInterval(int interval) {
        super.setInterval(interval);
    }

    public void setDisplayStyle(ItemType type) {
        if (ItemType.Item_Image.equals(type)) {
            if (this.mCircleLayout != null) {
                this.mCircleLayout.setVisibility(0);
            }
            if (this.mTextView != null) {
                this.mTextView.setVisibility(8);
            }
        } else if (ItemType.Item_Text.equals(type)) {
            if (this.mCircleLayout != null) {
                this.mCircleLayout.setVisibility(8);
            }
            if (this.mTextView != null) {
                this.mTextView.setVisibility(0);
            }
        } else if (ItemType.Item_Text_Image.equals(type)) {
            if (this.mCircleLayout != null) {
                this.mCircleLayout.setVisibility(0);
            }
            if (this.mTextView != null) {
                this.mTextView.setVisibility(0);
            }
        } else {
            if (this.mCircleLayout != null) {
                this.mCircleLayout.setVisibility(8);
            }
            if (this.mTextView != null) {
                this.mTextView.setVisibility(8);
            }
        }
    }

    public void setCircleRes(int res) {
        this.mBackgroundImageView.setImageResource(res);
    }
}
