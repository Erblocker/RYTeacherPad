package com.touchmenotapps.widget.radialmenu.menu.v1;

import android.graphics.drawable.Drawable;
import java.util.List;

public interface RadialMenuInterface {
    List<RadialMenuItem> getChildren();

    Drawable getIcon();

    String getLabel();

    String getName();

    void menuActiviated();
}
