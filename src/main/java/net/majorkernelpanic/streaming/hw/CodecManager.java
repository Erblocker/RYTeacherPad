package net.majorkernelpanic.streaming.hw;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@SuppressLint({"InlinedApi"})
public class CodecManager {
    public static final int[] SUPPORTED_COLOR_FORMATS = new int[]{21, 39, 19, 20, 2130706688};
    public static final String TAG = "CodecManager";
    private static Codec[] sDecoders = null;
    private static Codec[] sEncoders = null;

    static class Codec {
        public Integer[] formats;
        public String name;

        public Codec(String name, Integer[] formats) {
            this.name = name;
            this.formats = formats;
        }
    }

    @SuppressLint({"NewApi"})
    public static synchronized Codec[] findEncodersForMimeType(String mimeType) {
        Codec[] codecArr;
        synchronized (CodecManager.class) {
            if (sEncoders != null) {
                codecArr = sEncoders;
            } else {
                ArrayList<Codec> encoders = new ArrayList();
                for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                    MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                    if (codecInfo.isEncoder()) {
                        String[] types = codecInfo.getSupportedTypes();
                        for (String equalsIgnoreCase : types) {
                            if (equalsIgnoreCase.equalsIgnoreCase(mimeType)) {
                                try {
                                    CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
                                    Set<Integer> formats = new HashSet();
                                    for (int format : capabilities.colorFormats) {
                                        for (int i : SUPPORTED_COLOR_FORMATS) {
                                            if (format == i) {
                                                formats.add(Integer.valueOf(format));
                                            }
                                        }
                                    }
                                    encoders.add(new Codec(codecInfo.getName(), (Integer[]) formats.toArray(new Integer[formats.size()])));
                                } catch (Exception e) {
                                    Log.wtf("CodecManager", e);
                                }
                            }
                        }
                        continue;
                    }
                }
                sEncoders = (Codec[]) encoders.toArray(new Codec[encoders.size()]);
                codecArr = sEncoders;
            }
        }
        return codecArr;
    }

    @SuppressLint({"NewApi"})
    public static synchronized Codec[] findDecodersForMimeType(String mimeType) {
        Codec[] codecArr;
        synchronized (CodecManager.class) {
            if (sDecoders != null) {
                codecArr = sDecoders;
            } else {
                int i;
                ArrayList<Codec> decoders = new ArrayList();
                for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                    MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                    if (!codecInfo.isEncoder()) {
                        String[] types = codecInfo.getSupportedTypes();
                        for (String equalsIgnoreCase : types) {
                            if (equalsIgnoreCase.equalsIgnoreCase(mimeType)) {
                                try {
                                    CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
                                    Set<Integer> formats = new HashSet();
                                    for (int format : capabilities.colorFormats) {
                                        for (int i2 : SUPPORTED_COLOR_FORMATS) {
                                            if (format == i2) {
                                                formats.add(Integer.valueOf(format));
                                            }
                                        }
                                    }
                                    decoders.add(new Codec(codecInfo.getName(), (Integer[]) formats.toArray(new Integer[formats.size()])));
                                } catch (Exception e) {
                                    Log.wtf("CodecManager", e);
                                }
                            }
                        }
                        continue;
                    }
                }
                sDecoders = (Codec[]) decoders.toArray(new Codec[decoders.size()]);
                for (i = 0; i < sDecoders.length; i++) {
                    if (sDecoders[i].name.equalsIgnoreCase("omx.google.h264.decoder")) {
                        Codec codec = sDecoders[0];
                        sDecoders[0] = sDecoders[i];
                        sDecoders[i] = codec;
                    }
                }
                codecArr = sDecoders;
            }
        }
        return codecArr;
    }
}
