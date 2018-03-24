package com.github.mikephil.charting.utils;

import android.content.res.Resources;
import android.graphics.Color;
import com.foxit.sdk.common.Font;
import io.vov.vitamio.ThumbnailUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.ksoap2.SoapEnvelope;
import org.kxml2.wap.Wbxml;

public class ColorTemplate {
    public static final int[] COLORFUL_COLORS = new int[]{Color.rgb(Wbxml.EXT_1, 37, 82), Color.rgb(255, 102, 0), Color.rgb(245, 199, 0), Color.rgb(106, 150, 31), Color.rgb(179, 100, 53)};
    public static final int COLOR_NONE = -1;
    public static final int COLOR_SKIP = -2;
    public static final int[] JOYFUL_COLORS = new int[]{Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(254, 247, SoapEnvelope.VER12), Color.rgb(106, 167, Font.e_fontCharsetGB2312), Color.rgb(53, Wbxml.EXT_2, 209)};
    public static final int[] LIBERTY_COLORS = new int[]{Color.rgb(HttpStatus.SC_MULTI_STATUS, 248, 246), Color.rgb(148, ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_WIDTH, ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_WIDTH), Color.rgb(Font.e_fontCharsetChineseBig5, 180, 187), Color.rgb(118, 174, 175), Color.rgb(42, 109, 130)};
    public static final int[] PASTEL_COLORS = new int[]{Color.rgb(64, 89, 128), Color.rgb(149, 165, 124), Color.rgb(217, 184, Font.e_fontCharsetTurkish), Color.rgb(191, Font.e_fontCharsetGB2312, Font.e_fontCharsetGB2312), Color.rgb(179, 48, 80)};
    public static final int[] VORDIPLOM_COLORS = new int[]{Color.rgb(Wbxml.EXT_0, 255, 140), Color.rgb(255, 247, 140), Color.rgb(255, 208, 140), Color.rgb(140, 234, 255), Color.rgb(255, 140, 157)};

    public static int getHoloBlue() {
        return Color.rgb(51, 181, 229);
    }

    public static List<Integer> createColors(Resources r, int[] colors) {
        List<Integer> result = new ArrayList();
        for (int i : colors) {
            result.add(Integer.valueOf(r.getColor(i)));
        }
        return result;
    }

    public static List<Integer> createColors(int[] colors) {
        List<Integer> result = new ArrayList();
        for (int i : colors) {
            result.add(Integer.valueOf(i));
        }
        return result;
    }
}
