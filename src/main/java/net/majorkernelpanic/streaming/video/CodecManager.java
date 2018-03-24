package net.majorkernelpanic.streaming.video;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.os.Build.VERSION;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressLint({"InlinedApi"})
public class CodecManager {
    public static final String[] SOFTWARE_ENCODERS = new String[]{"OMX.google.h264.encoder"};
    public static final int[] SUPPORTED_COLOR_FORMATS = new int[]{19, 21};
    public static final String TAG = "CodecManager";

    static class Codecs {
        public String hardwareCodec;
        public int hardwareColorFormat;
        public String softwareCodec;
        public int softwareColorFormat;

        Codecs() {
        }
    }

    static class Selector {
        private static HashMap<String, SparseArray<ArrayList<String>>> sHardwareCodecs = new HashMap();
        private static HashMap<String, SparseArray<ArrayList<String>>> sSoftwareCodecs = new HashMap();

        Selector() {
        }

        public static Codecs findCodecsFormMimeType(String mimeType, boolean tryColorFormatSurface) {
            findSupportedColorFormats(mimeType);
            SparseArray<ArrayList<String>> hardwareCodecs = (SparseArray) sHardwareCodecs.get(mimeType);
            SparseArray<ArrayList<String>> softwareCodecs = (SparseArray) sSoftwareCodecs.get(mimeType);
            Codecs list = new Codecs();
            if (VERSION.SDK_INT < 18 || !tryColorFormatSurface) {
                int i = 0;
                while (i < CodecManager.SUPPORTED_COLOR_FORMATS.length) {
                    try {
                        list.hardwareCodec = (String) ((ArrayList) hardwareCodecs.get(CodecManager.SUPPORTED_COLOR_FORMATS[i])).get(0);
                        list.hardwareColorFormat = CodecManager.SUPPORTED_COLOR_FORMATS[i];
                        break;
                    } catch (Exception e) {
                        i++;
                    }
                }
                i = 0;
                while (i < CodecManager.SUPPORTED_COLOR_FORMATS.length) {
                    try {
                        list.softwareCodec = (String) ((ArrayList) softwareCodecs.get(CodecManager.SUPPORTED_COLOR_FORMATS[i])).get(0);
                        list.softwareColorFormat = CodecManager.SUPPORTED_COLOR_FORMATS[i];
                        break;
                    } catch (Exception e2) {
                        i++;
                    }
                }
                if (list.hardwareCodec != null) {
                    Log.v("CodecManager", "Choosen primary codec: " + list.hardwareCodec + " with color format: " + list.hardwareColorFormat);
                } else {
                    Log.e("CodecManager", "No supported hardware codec found !");
                }
                if (list.softwareCodec != null) {
                    Log.v("CodecManager", "Choosen secondary codec: " + list.hardwareCodec + " with color format: " + list.softwareColorFormat);
                } else {
                    Log.e("CodecManager", "No supported software codec found !");
                }
            } else {
                try {
                    list.hardwareCodec = (String) ((ArrayList) hardwareCodecs.get(2130708361)).get(0);
                    list.hardwareColorFormat = 2130708361;
                } catch (Exception e3) {
                }
                try {
                    list.softwareCodec = (String) ((ArrayList) softwareCodecs.get(2130708361)).get(0);
                    list.softwareColorFormat = 2130708361;
                } catch (Exception e4) {
                }
                if (list.hardwareCodec != null) {
                    Log.v("CodecManager", "Choosen primary codec: " + list.hardwareCodec + " with color format: " + list.hardwareColorFormat);
                } else {
                    Log.e("CodecManager", "No supported hardware codec found !");
                }
                if (list.softwareCodec != null) {
                    Log.v("CodecManager", "Choosen secondary codec: " + list.hardwareCodec + " with color format: " + list.hardwareColorFormat);
                } else {
                    Log.e("CodecManager", "No supported software codec found !");
                }
            }
            return list;
        }

        @SuppressLint({"NewApi"})
        private static void findSupportedColorFormats(String mimeType) {
            SparseArray<ArrayList<String>> softwareCodecs = new SparseArray();
            SparseArray<ArrayList<String>> hardwareCodecs = new SparseArray();
            if (!sSoftwareCodecs.containsKey(mimeType)) {
                int i;
                Log.v("CodecManager", "Searching supported color formats for mime type \"" + mimeType + "\"...");
                for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                    MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                    if (codecInfo.isEncoder()) {
                        String[] types = codecInfo.getSupportedTypes();
                        for (i = 0; i < types.length; i++) {
                            if (types[i].equalsIgnoreCase(mimeType)) {
                                int k;
                                CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
                                boolean software = false;
                                for (k = 0; k < CodecManager.SOFTWARE_ENCODERS.length; k++) {
                                    if (codecInfo.getName().equalsIgnoreCase(CodecManager.SOFTWARE_ENCODERS[i])) {
                                        software = true;
                                    }
                                }
                                for (int format : capabilities.colorFormats) {
                                    if (software) {
                                        if (softwareCodecs.get(format) == null) {
                                            softwareCodecs.put(format, new ArrayList());
                                        }
                                        ((ArrayList) softwareCodecs.get(format)).add(codecInfo.getName());
                                    } else {
                                        if (hardwareCodecs.get(format) == null) {
                                            hardwareCodecs.put(format, new ArrayList());
                                        }
                                        ((ArrayList) hardwareCodecs.get(format)).add(codecInfo.getName());
                                    }
                                }
                            }
                        }
                    }
                }
                StringBuilder e = new StringBuilder();
                e.append("Supported color formats on this phone: ");
                for (i = 0; i < softwareCodecs.size(); i++) {
                    e.append(softwareCodecs.keyAt(i) + ", ");
                }
                i = 0;
                while (i < hardwareCodecs.size()) {
                    e.append(hardwareCodecs.keyAt(i) + (i == hardwareCodecs.size() + -1 ? "." : ", "));
                    i++;
                }
                Log.v("CodecManager", e.toString());
                sSoftwareCodecs.put(mimeType, softwareCodecs);
                sHardwareCodecs.put(mimeType, hardwareCodecs);
            }
        }
    }

    static class Translator {
        private int bufferSize = (this.mYSize + (this.mUVSize * 2));
        private int i;
        private int mHeight;
        private int mOutputColorFormat;
        private int mUVSize = ((this.mUVStride * this.mHeight) / 2);
        private int mUVStride = (((int) Math.ceil(((double) (this.mYStride / 2)) / 16.0d)) * 16);
        private int mWidth;
        private int mYSize = (this.mYStride * this.mHeight);
        private int mYStride = (((int) Math.ceil(((double) this.mWidth) / 16.0d)) * 16);
        private byte[] tmp = new byte[(this.mUVSize * 2)];

        public Translator(int outputColorFormat, int width, int height) {
            this.mOutputColorFormat = outputColorFormat;
            this.mWidth = width;
            this.mHeight = height;
        }

        public int getBufferSize() {
            return this.bufferSize;
        }

        public int getUVStride() {
            return this.mUVStride;
        }

        public int getYStride() {
            return this.mYStride;
        }

        public byte[] translate(byte[] buffer) {
            if (this.mOutputColorFormat == 19) {
                int wh4 = this.bufferSize / 6;
                this.i = wh4 * 4;
                while (this.i < wh4 * 5) {
                    byte tmp = buffer[this.i];
                    buffer[this.i] = buffer[this.i + wh4];
                    buffer[this.i + wh4] = tmp;
                    this.i++;
                }
            } else if (this.mOutputColorFormat == 21) {
                System.arraycopy(buffer, this.mYSize, this.tmp, 0, this.mUVSize * 2);
                this.i = 0;
                while (this.i < this.mUVSize) {
                    buffer[this.mYSize + (this.i * 2)] = this.tmp[this.i + this.mUVSize];
                    buffer[(this.mYSize + (this.i * 2)) + 1] = this.tmp[this.i];
                    this.i++;
                }
            }
            return buffer;
        }
    }
}
