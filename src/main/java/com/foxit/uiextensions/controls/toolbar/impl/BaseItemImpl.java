package com.foxit.uiextensions.controls.toolbar.impl;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.internal.view.SupportMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.BaseItem.ItemType;
import com.foxit.uiextensions.utils.AppDisplay;

public class BaseItemImpl implements BaseItem {
    protected int mDefinedTextSize;
    protected ImageView mImage;
    protected LayoutParams mImgParams;
    protected ItemType mItemType;
    protected int mNoSelectTextColor;
    protected int mRelation;
    protected LinearLayout mRootLayout;
    protected int mSelectTextColor;
    private int mTag;
    protected LayoutParams mTextParams;
    protected TextView mTextView;
    protected boolean mUseTextColorRes;

    public BaseItemImpl(Context context) {
        this(context, null, 0, null, 10, ItemType.Item_Text_Image);
    }

    protected BaseItemImpl(Context context, String text) {
        this(context, text, 0, null, 10, ItemType.Item_Text);
    }

    protected BaseItemImpl(Context context, int imgRes) {
        this(context, null, imgRes, null, 10, ItemType.Item_Image);
    }

    protected BaseItemImpl(Context context, View customView) {
        this(context, null, 0, customView, 10, ItemType.Item_custom);
    }

    protected BaseItemImpl(Context context, String text, int imgRes, int relation) {
        this(context, text, imgRes, null, relation, ItemType.Item_Text_Image);
    }

    private BaseItemImpl(Context context, String text, int imgRes, View customView, int relation, ItemType type) {
        this.mSelectTextColor = SupportMenu.CATEGORY_MASK;
        this.mNoSelectTextColor = -16777216;
        this.mUseTextColorRes = false;
        this.mDefinedTextSize = 12;
        initDimens(context);
        this.mRelation = relation;
        this.mItemType = type;
        this.mRootLayout = new LinearLayout(context) {
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                BaseItemImpl.this.onItemLayout(l, t, r, b);
                super.onLayout(changed, l, t, r, b);
            }
        };
        this.mRootLayout.setGravity(17);
        if (ItemType.Item_Text.equals(type) || ItemType.Item_Text_Image.equals(type)) {
            this.mTextView = new TextView(context);
            this.mTextParams = new LayoutParams(-2, -2);
            this.mTextParams.gravity = 17;
            if (text != null) {
                this.mTextView.setText(text);
            }
        }
        if (ItemType.Item_Image.equals(type) || ItemType.Item_Text_Image.equals(type)) {
            this.mImage = new ImageView(context);
            this.mImgParams = new LayoutParams(-2, -2);
            this.mImgParams.gravity = 17;
            if (imgRes != 0) {
                this.mImage.setImageResource(imgRes);
            }
        }
        if (customView != null) {
            this.mRootLayout.setOrientation(1);
            this.mRootLayout.addView(customView);
        } else {
            setTextImgRelation(relation);
        }
        if (this.mTextView != null) {
            setTextSize((float) this.mDefinedTextSize);
            setTextColorResource(R.color.tb_item_text_color_selector);
        }
    }

    public View getContentView() {
        return this.mRootLayout;
    }

    public void setText(String text) {
        if (this.mTextView != null) {
            this.mTextView.setText(text);
        }
    }

    public String getText() {
        if (this.mTextView == null || this.mTextView.getText() == null) {
            return null;
        }
        return this.mTextView.getText().toString();
    }

    public void setTextColor(int selectedTextColor, int disSelectedTextColor) {
        this.mUseTextColorRes = false;
        this.mSelectTextColor = selectedTextColor;
        this.mNoSelectTextColor = disSelectedTextColor;
        if (this.mTextView != null) {
            this.mTextView.setTextColor(this.mNoSelectTextColor);
        }
    }

    public void setTextColor(int color) {
        setTextColor(color, color);
    }

    public void setTypeface(Typeface typeface) {
        if (this.mTextView != null) {
            this.mTextView.setTypeface(typeface);
        }
    }

    public void setTextSize(float dp) {
        if (this.mTextView != null) {
            this.mTextView.setTextSize(1, dp);
        }
    }

    public void setText(int res) {
        this.mTextView.setText(res);
    }

    public void setTextColorResource(int res) {
        this.mTextView.setTextColor(this.mTextView.getContext().getResources().getColorStateList(res));
        this.mUseTextColorRes = true;
    }

    public boolean setImageResource(int res) {
        if (this.mImage == null) {
            return false;
        }
        this.mImage.setImageResource(res);
        return true;
    }

    public void setContentView(View view) {
        if (view != null && this.mRootLayout != null) {
            this.mRootLayout.removeAllViews();
            this.mRootLayout.addView(view);
        }
    }

    public void setBackgroundResource(int res) {
        this.mRootLayout.setBackgroundResource(res);
    }

    public void setRelation(int relation) {
        if (this.mRelation != relation && this.mItemType == ItemType.Item_Text_Image) {
            this.mRelation = relation;
            this.mRootLayout.removeAllViews();
            setTextImgRelation(relation);
        }
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
            if (this.mImage == null) {
                return;
            }
            if (this.mImgParams != null) {
                this.mRootLayout.addView(this.mImage, this.mImgParams);
                return;
            } else {
                this.mRootLayout.addView(this.mImage);
                return;
            }
        }
        if (this.mImage != null) {
            if (this.mImgParams != null) {
                this.mRootLayout.addView(this.mImage, this.mImgParams);
            } else {
                this.mRootLayout.addView(this.mImage);
            }
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
        this.mRootLayout.setEnabled(enable);
        if (this.mTextView != null) {
            this.mTextView.setEnabled(enable);
        }
        if (this.mImage != null) {
            this.mImage.setEnabled(enable);
        }
    }

    public void setSelected(boolean selected) {
        this.mRootLayout.setSelected(selected);
        if (this.mTextView != null) {
            this.mTextView.setSelected(selected);
            if (!this.mUseTextColorRes) {
                if (selected) {
                    this.mTextView.setTextColor(this.mSelectTextColor);
                } else {
                    this.mTextView.setTextColor(this.mNoSelectTextColor);
                }
            }
        }
        if (this.mImage != null) {
            this.mImage.setSelected(selected);
        }
    }

    public void setOnClickListener(OnClickListener l) {
        this.mRootLayout.setOnClickListener(l);
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.mRootLayout.setOnLongClickListener(l);
    }

    public void setTag(int tag) {
        this.mTag = tag;
    }

    public int getTag() {
        return this.mTag;
    }

    public void setId(int id) {
        this.mRootLayout.setId(id);
    }

    public int getId() {
        if (this.mRootLayout != null) {
            return this.mRootLayout.getId();
        }
        return 0;
    }

    public void setInterval(int interval) {
        if (this.mTextView != null && this.mTextView.getLayoutParams() != null && (this.mTextView.getLayoutParams() instanceof LayoutParams)) {
            ((LayoutParams) this.mTextView.getLayoutParams()).setMargins(0, 0, 0, 0);
            if (this.mRelation == 10) {
                ((LayoutParams) this.mTextView.getLayoutParams()).rightMargin = interval;
            } else if (this.mRelation == 12) {
                ((LayoutParams) this.mTextView.getLayoutParams()).leftMargin = interval;
            } else if (this.mRelation == 11) {
                ((LayoutParams) this.mTextView.getLayoutParams()).bottomMargin = interval;
            } else {
                ((LayoutParams) this.mTextView.getLayoutParams()).topMargin = interval;
            }
        }
    }

    public void setDisplayStyle(ItemType type) {
        if (ItemType.Item_Image.equals(type)) {
            if (this.mImage != null) {
                this.mImage.setVisibility(0);
            }
            if (this.mTextView != null) {
                this.mTextView.setVisibility(8);
            }
        } else if (ItemType.Item_Text.equals(type)) {
            if (this.mImage != null) {
                this.mImage.setVisibility(8);
            }
            if (this.mTextView != null) {
                this.mTextView.setVisibility(0);
            }
        } else if (ItemType.Item_Text_Image.equals(type)) {
            if (this.mImage != null) {
                this.mImage.setVisibility(0);
            }
            if (this.mTextView != null) {
                this.mTextView.setVisibility(0);
            }
        } else {
            if (this.mImage != null) {
                this.mImage.setVisibility(8);
            }
            if (this.mTextView != null) {
                this.mTextView.setVisibility(8);
            }
        }
    }

    public void onItemLayout(int l, int t, int r, int b) {
    }

    private void initDimens(Context context) {
        this.mDefinedTextSize = (int) context.getResources().getDimension(R.dimen.ux_text_height_toolbar);
        this.mDefinedTextSize = (int) AppDisplay.getInstance(context).px2dp((float) this.mDefinedTextSize);
    }
}
