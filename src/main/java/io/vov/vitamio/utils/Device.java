package io.vov.vitamio.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import java.util.Locale;

public class Device {
    public static String getLocale() {
        Locale locale = Locale.getDefault();
        if (locale != null) {
            String lo = locale.getLanguage();
            Log.i("getLocale " + lo, new Object[0]);
            if (lo != null) {
                return lo.toLowerCase();
            }
        }
        return "en";
    }

    public static String getDeviceFeatures(Context ctx) {
        return getIdentifiers(ctx) + getSystemFeatures() + getScreenFeatures(ctx);
    }

    @SuppressLint({"NewApi"})
    public static String getIdentifiers(Context ctx) {
        StringBuilder sb = new StringBuilder();
        if (VERSION.SDK_INT > 8) {
            sb.append(getPair("serial", Build.SERIAL));
        } else {
            sb.append(getPair("serial", "No Serial"));
        }
        sb.append(getPair("android_id", Secure.getString(ctx.getContentResolver(), "android_id")));
        TelephonyManager tel = (TelephonyManager) ctx.getSystemService("phone");
        sb.append(getPair("sim_country_iso", tel.getSimCountryIso()));
        sb.append(getPair("network_operator_name", tel.getNetworkOperatorName()));
        sb.append(getPair("unique_id", Crypto.md5(sb.toString())));
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService("connectivity");
        sb.append(getPair("network_type", cm.getActiveNetworkInfo() == null ? "-1" : String.valueOf(cm.getActiveNetworkInfo().getType())));
        return sb.toString();
    }

    public static String getSystemFeatures() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPair("android_release", VERSION.RELEASE));
        sb.append(getPair("android_sdk_int", VERSION.SDK_INT));
        sb.append(getPair("device_cpu_abi", Build.CPU_ABI));
        sb.append(getPair("device_model", Build.MODEL));
        sb.append(getPair("device_manufacturer", Build.MANUFACTURER));
        sb.append(getPair("device_board", Build.BOARD));
        sb.append(getPair("device_fingerprint", Build.FINGERPRINT));
        sb.append(getPair("device_cpu_feature", CPU.getFeatureString()));
        return sb.toString();
    }

    public static String getScreenFeatures(Context ctx) {
        StringBuilder sb = new StringBuilder();
        DisplayMetrics disp = ctx.getResources().getDisplayMetrics();
        sb.append(getPair("screen_density", disp.density));
        sb.append(getPair("screen_density_dpi", disp.densityDpi));
        sb.append(getPair("screen_height_pixels", disp.heightPixels));
        sb.append(getPair("screen_width_pixels", disp.widthPixels));
        sb.append(getPair("screen_scaled_density", disp.scaledDensity));
        sb.append(getPair("screen_xdpi", disp.xdpi));
        sb.append(getPair("screen_ydpi", disp.ydpi));
        return sb.toString();
    }

    private static String getPair(String key, String value) {
        return "&" + (key == null ? "" : key.trim()) + "=" + (value == null ? "" : value.trim());
    }
}
