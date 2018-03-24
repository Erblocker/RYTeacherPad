package com.joanzapata.iconify.fonts;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

public class FontAwesomeModule implements IconFontDescriptor {
    public String ttfFileName() {
        return "iconify/android-iconify-fontawesome.ttf";
    }

    public Icon[] characters() {
        return FontAwesomeIcons.values();
    }
}
