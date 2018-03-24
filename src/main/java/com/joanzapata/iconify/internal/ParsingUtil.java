package com.joanzapata.iconify.internal;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.internal.HasOnViewAttachListener.OnViewAttachListener;
import java.util.List;

public final class ParsingUtil {

    /* renamed from: com.joanzapata.iconify.internal.ParsingUtil$1 */
    class AnonymousClass1 implements OnViewAttachListener {
        boolean isAttached = false;
        private final /* synthetic */ TextView val$target;

        AnonymousClass1(TextView textView) {
            this.val$target = textView;
        }

        public void onAttach() {
            this.isAttached = true;
            View view = this.val$target;
            final TextView textView = this.val$target;
            ViewCompat.postOnAnimation(view, new Runnable() {
                public void run() {
                    if (AnonymousClass1.this.isAttached) {
                        textView.invalidate();
                        ViewCompat.postOnAnimation(textView, this);
                    }
                }
            });
        }

        public void onDetach() {
            this.isAttached = false;
        }
    }

    private ParsingUtil() {
    }

    public static CharSequence parse(Context context, List<IconFontDescriptorWrapper> iconFontDescriptors, CharSequence text, TextView target) {
        context = context.getApplicationContext();
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(text);
        recursivePrepareSpannableIndexes(context, text.toString(), spannableBuilder, iconFontDescriptors, 0);
        if (hasAnimatedSpans(spannableBuilder)) {
            if (target == null) {
                throw new IllegalArgumentException("You can't use \"spin\" without providing the target TextView.");
            } else if (target instanceof HasOnViewAttachListener) {
                ((HasOnViewAttachListener) target).setOnViewAttachListener(new AnonymousClass1(target));
            } else {
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(target.getClass().getSimpleName())).append(" does not implement ").append("HasOnViewAttachListener. Please use IconTextView, IconButton or IconToggleButton.").toString());
            }
        } else if (target instanceof HasOnViewAttachListener) {
            ((HasOnViewAttachListener) target).setOnViewAttachListener(null);
        }
        return spannableBuilder;
    }

    private static boolean hasAnimatedSpans(SpannableStringBuilder spannableBuilder) {
        for (CustomTypefaceSpan span : (CustomTypefaceSpan[]) spannableBuilder.getSpans(0, spannableBuilder.length(), CustomTypefaceSpan.class)) {
            if (span.isAnimated()) {
                return true;
            }
        }
        return false;
    }

    private static void recursivePrepareSpannableIndexes(Context context, String fullText, SpannableStringBuilder text, List<IconFontDescriptorWrapper> iconFontDescriptors, int start) {
        String stringText = text.toString();
        int startIndex = stringText.indexOf("{", start);
        if (startIndex != -1) {
            int i;
            int endIndex = stringText.indexOf("}", startIndex) + 1;
            String[] strokes = stringText.substring(startIndex + 1, endIndex - 1).split(" ");
            String key = strokes[0];
            IconFontDescriptorWrapper iconFontDescriptor = null;
            Icon icon = null;
            for (i = 0; i < iconFontDescriptors.size(); i++) {
                iconFontDescriptor = (IconFontDescriptorWrapper) iconFontDescriptors.get(i);
                icon = iconFontDescriptor.getIcon(key);
                if (icon != null) {
                    break;
                }
            }
            if (icon == null) {
                recursivePrepareSpannableIndexes(context, fullText, text, iconFontDescriptors, endIndex);
                return;
            }
            float iconSizePx = -1.0f;
            int iconColor = Integer.MAX_VALUE;
            float iconSizeRatio = -1.0f;
            boolean spin = false;
            for (i = 1; i < strokes.length; i++) {
                String stroke = strokes[i];
                if (stroke.equalsIgnoreCase("spin")) {
                    spin = true;
                } else {
                    Context context2;
                    if (stroke.matches("([0-9]*(\\.[0-9]*)?)dp")) {
                        context2 = context;
                        iconSizePx = dpToPx(context2, Float.valueOf(stroke.substring(0, stroke.length() - 2)).floatValue());
                    } else {
                        if (stroke.matches("([0-9]*(\\.[0-9]*)?)sp")) {
                            context2 = context;
                            iconSizePx = spToPx(context2, Float.valueOf(stroke.substring(0, stroke.length() - 2)).floatValue());
                        } else {
                            if (stroke.matches("([0-9]*)px")) {
                                iconSizePx = (float) Integer.valueOf(stroke.substring(0, stroke.length() - 2)).intValue();
                            } else {
                                if (stroke.matches("@dimen/(.*)")) {
                                    iconSizePx = getPxFromDimen(context, stroke.substring(7));
                                    if (iconSizePx < 0.0f) {
                                        throw new IllegalArgumentException("Unknown resource " + stroke + " in \"" + fullText + "\"");
                                    }
                                } else {
                                    if (stroke.matches("([0-9]*(\\.[0-9]*)?)%")) {
                                        iconSizeRatio = Float.valueOf(stroke.substring(0, stroke.length() - 1)).floatValue() / 100.0f;
                                    } else {
                                        if (stroke.matches("#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})")) {
                                            iconColor = Color.parseColor(stroke);
                                        } else {
                                            if (stroke.matches("@color/(.*)")) {
                                                iconColor = getColorFromResource(context, stroke.substring(7));
                                                if (iconColor == Integer.MAX_VALUE) {
                                                    throw new IllegalArgumentException("Unknown resource " + stroke + " in \"" + fullText + "\"");
                                                }
                                            } else {
                                                throw new IllegalArgumentException("Unknown expression " + stroke + " in \"" + fullText + "\"");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            text = text.replace(startIndex, endIndex, icon.character());
            text.setSpan(new CustomTypefaceSpan(icon, iconFontDescriptor.getTypeface(context), iconSizePx, iconSizeRatio, iconColor, spin), startIndex, startIndex + 1, 17);
            recursivePrepareSpannableIndexes(context, fullText, text, iconFontDescriptors, startIndex);
        }
    }

    public static float getPxFromDimen(Context context, String resName) {
        Resources resources = context.getResources();
        int resId = resources.getIdentifier(resName, "dimen", context.getPackageName());
        if (resId <= 0) {
            return -1.0f;
        }
        return resources.getDimension(resId);
    }

    public static int getColorFromResource(Context context, String resName) {
        Resources resources = context.getResources();
        int resId = resources.getIdentifier(resName, "color", context.getPackageName());
        if (resId <= 0) {
            return Integer.MAX_VALUE;
        }
        return resources.getColor(resId, null);
    }

    public static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(1, dp, context.getResources().getDisplayMetrics());
    }

    public static float spToPx(Context context, float sp) {
        return TypedValue.applyDimension(2, sp, context.getResources().getDisplayMetrics());
    }
}
