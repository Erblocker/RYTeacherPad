package com.touchmenotapps.widget.radialmenu.menu.v1;

import android.graphics.drawable.Drawable;
import android.util.Log;
import java.util.List;

public class RadialMenuItem implements RadialMenuInterface {
    private Drawable mMenuIcon;
    private List<RadialMenuItem> menuChildren = null;
    private String menuLabel = null;
    private RadialMenuItemClickListener menuListener = null;
    private String menuName = "Empty";

    public interface RadialMenuItemClickListener {
        void execute();
    }

    public RadialMenuItem(String name, String displayName) {
        if (name != null) {
            this.menuName = name;
        }
        this.menuLabel = displayName;
    }

    public void setDisplayIcon(Drawable icon) {
        this.mMenuIcon = icon;
    }

    public void setOnMenuItemPressed(RadialMenuItemClickListener listener) {
        this.menuListener = listener;
    }

    public void setMenuChildren(List<RadialMenuItem> childItems) {
        this.menuChildren = childItems;
    }

    public String getName() {
        return this.menuName;
    }

    public String getLabel() {
        return this.menuLabel;
    }

    public Drawable getIcon() {
        return this.mMenuIcon;
    }

    public List<RadialMenuItem> getChildren() {
        return this.menuChildren;
    }

    public void menuActiviated() {
        Log.i(getClass().getName(), this.menuName + " menu pressed.");
        if (this.menuListener != null) {
            this.menuListener.execute();
        }
    }
}
