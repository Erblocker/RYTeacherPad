package io.vov.vitamio.utils;

import android.os.Build;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class CPU {
    public static final int FEATURE_ARM_NEON = 32;
    public static final int FEATURE_ARM_V5TE = 1;
    public static final int FEATURE_ARM_V6 = 2;
    public static final int FEATURE_ARM_V7A = 8;
    public static final int FEATURE_ARM_VFP = 4;
    public static final int FEATURE_ARM_VFPV3 = 16;
    public static final int FEATURE_MIPS = 128;
    public static final int FEATURE_X86 = 64;
    private static int cachedFeature = -1;
    private static String cachedFeatureString = null;
    private static final Map<String, String> cpuinfo = new HashMap();

    public static String getFeatureString() {
        getFeature();
        return cachedFeatureString;
    }

    public static int getFeature() {
        Throwable e;
        Throwable th;
        boolean hasARMv6;
        String val;
        int i;
        if (cachedFeature > 0) {
            return getCachedFeature();
        }
        boolean hasARMv7;
        String vendor_id;
        cachedFeature = 1;
        if (cpuinfo.isEmpty()) {
            BufferedReader bis = null;
            try {
                BufferedReader bis2 = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                while (true) {
                    try {
                        String line = bis2.readLine();
                        if (line == null) {
                            break;
                        } else if (!line.trim().equals("")) {
                            String[] pairs = line.split(":");
                            if (pairs.length > 1) {
                                cpuinfo.put(pairs[0].trim(), pairs[1].trim());
                            }
                        }
                    } catch (Exception e2) {
                        e = e2;
                        bis = bis2;
                    } catch (Throwable th2) {
                        th = th2;
                        bis = bis2;
                    }
                }
                if (bis2 != null) {
                    try {
                        bis2.close();
                    } catch (Throwable e3) {
                        Log.e("getCPUFeature", e3);
                    }
                }
            } catch (Exception e4) {
                e3 = e4;
                try {
                    Log.e("getCPUFeature", e3);
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (Throwable e32) {
                            Log.e("getCPUFeature", e32);
                        }
                    }
                    if (!cpuinfo.isEmpty()) {
                        for (String key : cpuinfo.keySet()) {
                            Log.d("%s:%s", key, cpuinfo.get(key));
                        }
                        hasARMv6 = false;
                        hasARMv7 = false;
                        val = (String) cpuinfo.get("CPU architecture");
                        if (TextUtils.isEmpty(val)) {
                            vendor_id = (String) cpuinfo.get("vendor_id");
                            String mips = (String) cpuinfo.get("cpu model");
                            if (!!TextUtils.isEmpty(vendor_id)) {
                            }
                            cachedFeature |= 128;
                        } else {
                            try {
                                i = StringUtils.convertToInt(val);
                                Log.d("CPU architecture: %s", Integer.valueOf(i));
                                if (i >= 7) {
                                    hasARMv6 = true;
                                    hasARMv7 = true;
                                } else if (i >= 6) {
                                    hasARMv6 = true;
                                    hasARMv7 = false;
                                }
                            } catch (Throwable ex) {
                                Log.e("getCPUFeature", ex);
                            }
                            val = (String) cpuinfo.get("Processor");
                            if (TextUtils.isEmpty(val)) {
                                val = (String) cpuinfo.get("model name");
                            }
                            hasARMv6 = true;
                            hasARMv7 = true;
                            hasARMv6 = true;
                            hasARMv7 = false;
                            if (hasARMv6) {
                                cachedFeature |= 2;
                            }
                            if (hasARMv7) {
                                cachedFeature |= 8;
                            }
                            val = (String) cpuinfo.get("Features");
                            if (val != null) {
                                if (val.contains("neon")) {
                                    cachedFeature |= 52;
                                } else if (val.contains("vfpv3")) {
                                    cachedFeature |= 20;
                                } else if (val.contains("vfp")) {
                                    cachedFeature |= 4;
                                }
                            }
                        }
                    }
                    return getCachedFeature();
                } catch (Throwable th3) {
                    th = th3;
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (Throwable e322) {
                            Log.e("getCPUFeature", e322);
                        }
                    }
                    throw th;
                }
            }
        }
        if (cpuinfo.isEmpty()) {
            for (String key2 : cpuinfo.keySet()) {
                Log.d("%s:%s", key2, cpuinfo.get(key2));
            }
            hasARMv6 = false;
            hasARMv7 = false;
            val = (String) cpuinfo.get("CPU architecture");
            if (TextUtils.isEmpty(val)) {
                i = StringUtils.convertToInt(val);
                Log.d("CPU architecture: %s", Integer.valueOf(i));
                if (i >= 7) {
                    hasARMv6 = true;
                    hasARMv7 = true;
                } else if (i >= 6) {
                    hasARMv6 = true;
                    hasARMv7 = false;
                }
                val = (String) cpuinfo.get("Processor");
                if (TextUtils.isEmpty(val)) {
                    val = (String) cpuinfo.get("model name");
                }
                if (val != null && (val.contains("(v7l)") || val.contains("ARMv7"))) {
                    hasARMv6 = true;
                    hasARMv7 = true;
                }
                if (val != null && (val.contains("(v6l)") || val.contains("ARMv6"))) {
                    hasARMv6 = true;
                    hasARMv7 = false;
                }
                if (hasARMv6) {
                    cachedFeature |= 2;
                }
                if (hasARMv7) {
                    cachedFeature |= 8;
                }
                val = (String) cpuinfo.get("Features");
                if (val != null) {
                    if (val.contains("neon")) {
                        cachedFeature |= 52;
                    } else if (val.contains("vfpv3")) {
                        cachedFeature |= 20;
                    } else if (val.contains("vfp")) {
                        cachedFeature |= 4;
                    }
                }
            } else {
                vendor_id = (String) cpuinfo.get("vendor_id");
                String mips2 = (String) cpuinfo.get("cpu model");
                if (!TextUtils.isEmpty(vendor_id) && vendor_id.contains("GenuineIntel")) {
                    cachedFeature |= 64;
                } else if (!TextUtils.isEmpty(mips2) && mips2.contains("MIPS")) {
                    cachedFeature |= 128;
                }
            }
        }
        return getCachedFeature();
    }

    private static int getCachedFeature() {
        if (cachedFeatureString == null) {
            StringBuffer sb = new StringBuffer();
            if ((cachedFeature & 1) > 0) {
                sb.append("V5TE ");
            }
            if ((cachedFeature & 2) > 0) {
                sb.append("V6 ");
            }
            if ((cachedFeature & 4) > 0) {
                sb.append("VFP ");
            }
            if ((cachedFeature & 8) > 0) {
                sb.append("V7A ");
            }
            if ((cachedFeature & 16) > 0) {
                sb.append("VFPV3 ");
            }
            if ((cachedFeature & 32) > 0) {
                sb.append("NEON ");
            }
            if ((cachedFeature & 64) > 0) {
                sb.append("X86 ");
            }
            if ((cachedFeature & 128) > 0) {
                sb.append("MIPS ");
            }
            cachedFeatureString = sb.toString();
        }
        Log.d("GET CPU FATURE: %s", cachedFeatureString);
        return cachedFeature;
    }

    public static boolean isDroidXDroid2() {
        return Build.MODEL.trim().equalsIgnoreCase("DROIDX") || Build.MODEL.trim().equalsIgnoreCase("DROID2") || Build.FINGERPRINT.toLowerCase().contains("shadow") || Build.FINGERPRINT.toLowerCase().contains("droid2");
    }
}
