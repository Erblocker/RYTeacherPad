package com.foxit.view.menu;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.TopBarImpl;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppResource;
import java.util.ArrayList;
import java.util.Iterator;

public class MenuViewImpl implements MenuView {
    private MenuCallback mCallback;
    private Context mContext;
    private ArrayList<MenuGroupImpl> mMenuGroups = new ArrayList();
    private LinearLayout mMenuList_ly = ((LinearLayout) this.mView.findViewById(R.id.menu_more_content_ly));
    private BaseBar mMenuTitleBar;
    private RelativeLayout mMenuTitleLayout;
    private View mView = View.inflate(this.mContext, R.layout.view_menu_more, null);

    public interface MenuCallback {
        void onClosed();
    }

    public MenuViewImpl(Context context, MenuCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        initMenuTitleView();
        initBasicFileMenuItem();
    }

    private void initBasicFileMenuItem() {
        if (getMenuGroup(0) == null) {
            addMenuGroup(new MenuGroupImpl(this.mContext, 0, AppResource.getString(this.mContext, R.string.rd_menu_file)));
        }
    }

    private void initMenuTitleView() {
        this.mMenuTitleBar = new TopBarImpl(this.mContext);
        this.mMenuTitleBar.setBackgroundResource(R.color.ux_text_color_subhead_colour);
        BaseItemImpl mTitleTextItem = new BaseItemImpl(this.mContext);
        mTitleTextItem.setText(AppResource.getString(this.mContext, R.string.action_more));
        mTitleTextItem.setTextSize(AppDisplay.getInstance(this.mContext).px2dp(this.mContext.getResources().getDimension(R.dimen.ux_text_height_title)));
        mTitleTextItem.setTextColorResource(R.color.ux_text_color_menu_light);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            this.mMenuTitleBar.addView(mTitleTextItem, TB_Position.Position_LT);
        } else {
            BaseItemImpl mMenuCloseItem = new BaseItemImpl(this.mContext);
            mMenuCloseItem.setImageResource(R.drawable.cloud_back);
            mMenuCloseItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (MenuViewImpl.this.mCallback != null) {
                        MenuViewImpl.this.mCallback.onClosed();
                    }
                }
            });
            this.mMenuTitleBar.addView(mMenuCloseItem, TB_Position.Position_LT);
            this.mMenuTitleBar.addView(mTitleTextItem, TB_Position.Position_LT);
        }
        this.mMenuTitleLayout = (RelativeLayout) this.mView.findViewById(R.id.menu_more_title_ly);
        this.mMenuTitleLayout.removeAllViews();
        this.mMenuTitleLayout.addView(this.mMenuTitleBar.getContentView());
    }

    public void addMenuGroup(MenuGroupImpl menuGroup) {
        int tag = menuGroup.getTag();
        if (this.mMenuGroups.size() == 0) {
            this.mMenuGroups.add(menuGroup);
        } else {
            int size = this.mMenuGroups.size();
            int i = 0;
            while (i < size) {
                if (tag > ((MenuGroupImpl) this.mMenuGroups.get(i)).getTag()) {
                    if (i != size - 1) {
                        i++;
                    } else if (!this.mMenuGroups.contains(menuGroup)) {
                        this.mMenuGroups.add(menuGroup);
                    }
                } else if (!this.mMenuGroups.contains(menuGroup)) {
                    this.mMenuGroups.add(i, menuGroup);
                }
            }
        }
        resetView();
    }

    public void removeMenuGroup(int tag) {
        if (this.mMenuGroups.size() > 0) {
            Iterator it = this.mMenuGroups.iterator();
            while (it.hasNext()) {
                MenuGroupImpl group = (MenuGroupImpl) it.next();
                if (group.getTag() == tag) {
                    this.mMenuGroups.remove(group);
                    this.mMenuList_ly.removeView(group.getView());
                    return;
                }
            }
        }
    }

    public MenuGroupImpl getMenuGroup(int tag) {
        if (this.mMenuGroups.size() > 0) {
            Iterator it = this.mMenuGroups.iterator();
            while (it.hasNext()) {
                MenuGroupImpl group = (MenuGroupImpl) it.next();
                if (group.getTag() == tag) {
                    return group;
                }
            }
        }
        return null;
    }

    public void addMenuItem(int groupTag, MenuItemImpl item) {
        if (this.mMenuGroups.size() > 0) {
            Iterator it = this.mMenuGroups.iterator();
            while (it.hasNext()) {
                MenuGroupImpl group = (MenuGroupImpl) it.next();
                if (group.getTag() == groupTag) {
                    group.addItem(item);
                    return;
                }
            }
        }
    }

    public void removeMenuItem(int groupTag, int itemTag) {
        if (this.mMenuGroups.size() > 0) {
            Iterator it = this.mMenuGroups.iterator();
            while (it.hasNext()) {
                MenuGroupImpl group = (MenuGroupImpl) it.next();
                if (group.getTag() == groupTag) {
                    group.removeItem(itemTag);
                }
            }
        }
    }

    public View getContentView() {
        return this.mView;
    }

    private void resetView() {
        this.mMenuList_ly.removeAllViews();
        Iterator it = this.mMenuGroups.iterator();
        while (it.hasNext()) {
            this.mMenuList_ly.addView(((MenuGroupImpl) it.next()).getView());
        }
    }
}
