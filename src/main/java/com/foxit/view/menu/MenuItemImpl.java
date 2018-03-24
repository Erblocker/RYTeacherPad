package com.foxit.view.menu;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.foxit.uiextensions.R;

public class MenuItemImpl {
    public static final int EVENT_BUTTON_CLICKED = 1;
    public static final int EVENT_ITEM_CLICKED = 2;
    public MenuViewCallback callback;
    public View customView;
    public boolean enable;
    public int iconId;
    private MenuViewCallback mCallback;
    private Context mContext;
    private int mEventType;
    private ImageView mImage;
    private int mTag;
    private TextView mText;
    private View mView;
    public String text;

    public MenuItemImpl(Context context, int tag, String item_text, int imageID, MenuViewCallback callback) {
        this.mTag = tag;
        this.text = item_text;
        this.iconId = imageID;
        this.enable = true;
        this.callback = callback;
        this.mContext = context;
        this.mView = View.inflate(context, R.layout.view_menu_more_item, null);
        this.mText = (TextView) this.mView.findViewById(R.id.menu_more_item_tv);
        if (item_text == null) {
            this.mText.setVisibility(4);
        } else {
            this.mText.setText(item_text);
        }
        this.mImage = (ImageView) this.mView.findViewById(R.id.menu_more_item_bt);
        if (imageID == 0) {
            this.mImage.setVisibility(8);
        } else {
            this.mImage.setImageResource(imageID);
        }
        this.mCallback = callback;
        final MenuItemImpl itemSelf = this;
        this.mView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (MenuItemImpl.this.mCallback != null) {
                    MenuItemImpl.this.mCallback.onClick(itemSelf);
                }
            }
        });
    }

    public MenuItemImpl(Context context, int tag, View customView) {
        this.mTag = tag;
        this.customView = customView;
        this.mContext = context;
        this.mView = View.inflate(context, R.layout.view_menu_more_item, null);
        LinearLayout ly = (LinearLayout) this.mView.getRootView();
        ly.removeAllViews();
        ly.addView(customView);
    }

    public void setDividerVisible(boolean visibly) {
        View divider = this.mView.findViewById(R.id.menu_more_item_divider);
        if (divider != null) {
            if (visibly) {
                divider.setVisibility(0);
            } else {
                divider.setVisibility(8);
            }
        }
    }

    public boolean isCustomView() {
        if (this.customView != null) {
            return true;
        }
        return false;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public View getView() {
        return this.mView;
    }

    public int getTag() {
        return this.mTag;
    }

    public void setEnable(boolean enable) {
        this.mView.setEnabled(enable);
        this.mText.setEnabled(enable);
        this.mImage.setEnabled(enable);
    }
}
