package com.foxit.view.menu;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import java.util.ArrayList;
import java.util.Iterator;

public class MenuGroupImpl {
    private LinearLayout mContentList_ly = ((LinearLayout) this.mView.findViewById(R.id.menu_more_group_content_ly));
    private Context mContext;
    private ArrayList<MenuItemImpl> mMenuItems = new ArrayList();
    private View mView = View.inflate(this.mContext, R.layout.view_menu_more_group, null);
    private int tag;
    public String title;

    public MenuGroupImpl(Context context, int tag, String title) {
        this.tag = tag;
        this.title = title;
        this.mContext = context;
        TextView titleTV = (TextView) this.mView.findViewById(R.id.menu_more_group_title);
        if (title == null) {
            title = "";
        }
        titleTV.setText(title);
    }

    public void addItem(MenuItemImpl item) {
        if (item != null) {
            int tag = item.getTag();
            if (!this.mMenuItems.contains(item)) {
                if (this.mMenuItems.size() != 0) {
                    int size = this.mMenuItems.size();
                    int i = 0;
                    while (i < size) {
                        if (tag <= ((MenuItemImpl) this.mMenuItems.get(i)).getTag()) {
                            this.mMenuItems.add(i, item);
                            break;
                        } else if (i == size - 1) {
                            this.mMenuItems.add(item);
                            break;
                        } else {
                            i++;
                        }
                    }
                } else {
                    this.mMenuItems.add(item);
                }
                resetItems();
            }
        }
    }

    private void resetItems() {
        this.mContentList_ly.removeAllViews();
        Iterator it = this.mMenuItems.iterator();
        while (it.hasNext()) {
            addItemToMenu((MenuItemImpl) it.next());
        }
    }

    public void removeItem(MenuItemImpl item) {
        if (this.mMenuItems.size() > 0) {
            this.mMenuItems.remove(item);
            this.mContentList_ly.removeView(item.getView());
        }
    }

    public void removeItem(int tag) {
        if (this.mMenuItems.size() > 0) {
            Iterator it = this.mMenuItems.iterator();
            while (it.hasNext()) {
                MenuItemImpl item = (MenuItemImpl) it.next();
                if (item.getTag() == tag) {
                    this.mContentList_ly.removeView(item.getView());
                    this.mMenuItems.remove(item);
                    return;
                }
            }
        }
    }

    private void addItemToMenu(MenuItemImpl item) {
        if (item.getView().getParent() != null) {
            ((ViewGroup) item.getView().getParent()).removeView(item.getView());
        }
        this.mContentList_ly.addView(item.getView(), new LayoutParams(-1, -2));
        item.setDividerVisible(true);
    }

    public int getTag() {
        return this.tag;
    }

    public View getView() {
        return this.mView;
    }
}
