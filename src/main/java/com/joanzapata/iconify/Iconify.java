package com.joanzapata.iconify;

import android.content.Context;
import android.widget.TextView;
import com.joanzapata.iconify.internal.IconFontDescriptorWrapper;
import com.joanzapata.iconify.internal.ParsingUtil;
import java.util.ArrayList;
import java.util.List;

public class Iconify {
    private static List<IconFontDescriptorWrapper> iconFontDescriptors = new ArrayList();

    public static class IconifyInitializer {
        public IconifyInitializer(IconFontDescriptor iconFontDescriptor) {
            Iconify.addIconFontDescriptor(iconFontDescriptor);
        }

        public IconifyInitializer with(IconFontDescriptor iconFontDescriptor) {
            Iconify.addIconFontDescriptor(iconFontDescriptor);
            return this;
        }
    }

    public static IconifyInitializer with(IconFontDescriptor iconFontDescriptor) {
        return new IconifyInitializer(iconFontDescriptor);
    }

    public static void addIcons(TextView... textViews) {
        for (TextView textView : textViews) {
            if (textView != null) {
                textView.setText(compute(textView.getContext(), textView.getText(), textView));
            }
        }
    }

    private static void addIconFontDescriptor(IconFontDescriptor iconFontDescriptor) {
        for (IconFontDescriptorWrapper wrapper : iconFontDescriptors) {
            if (wrapper.getIconFontDescriptor().ttfFileName().equals(iconFontDescriptor.ttfFileName())) {
                return;
            }
        }
        iconFontDescriptors.add(new IconFontDescriptorWrapper(iconFontDescriptor));
    }

    public static CharSequence compute(Context context, CharSequence text) {
        return compute(context, text, null);
    }

    public static CharSequence compute(Context context, CharSequence text, TextView target) {
        return ParsingUtil.parse(context, iconFontDescriptors, text, target);
    }

    public static IconFontDescriptorWrapper findTypefaceOf(Icon icon) {
        for (IconFontDescriptorWrapper iconFontDescriptor : iconFontDescriptors) {
            if (iconFontDescriptor.hasIcon(icon)) {
                return iconFontDescriptor;
            }
        }
        return null;
    }

    static Icon findIconForKey(String iconKey) {
        int iconFontDescriptorsSize = iconFontDescriptors.size();
        for (int i = 0; i < iconFontDescriptorsSize; i++) {
            Icon icon = ((IconFontDescriptorWrapper) iconFontDescriptors.get(i)).getIcon(iconKey);
            if (icon != null) {
                return icon;
            }
        }
        return null;
    }
}
