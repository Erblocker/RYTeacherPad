package com.foxit.view.menu;

import android.view.View;

public interface MenuView {
    void addMenuGroup(MenuGroupImpl menuGroupImpl);

    void addMenuItem(int i, MenuItemImpl menuItemImpl);

    View getContentView();

    MenuGroupImpl getMenuGroup(int i);

    void removeMenuGroup(int i);

    void removeMenuItem(int i, int i2);
}
