package com.joanzapata.iconify.fonts;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

public class NovaIconsModule implements IconFontDescriptor {
    public String ttfFileName() {
        return "iconify/android-iconify-nova.ttf";
    }

    public Icon[] characters() {
        return NovaIcons.values();
    }
}
