package io.vov.vitamio;

import java.util.HashMap;

public class MediaFile {
    public static final int FILE_TYPE_3GPP = 703;
    public static final int FILE_TYPE_3GPP2 = 704;
    public static final int FILE_TYPE_AAC = 8;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_APE = 13;
    public static final int FILE_TYPE_ASF = 706;
    public static final int FILE_TYPE_AVS = 717;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_DIVX = 713;
    public static final int FILE_TYPE_DVD = 712;
    public static final int FILE_TYPE_FLAC = 14;
    public static final int FILE_TYPE_FLV = 709;
    public static final int FILE_TYPE_IMY = 12;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_M4V = 702;
    public static final int FILE_TYPE_MID = 10;
    public static final int FILE_TYPE_MKA = 9;
    public static final int FILE_TYPE_MKV = 707;
    public static final int FILE_TYPE_MOV = 710;
    public static final int FILE_TYPE_MP2TS = 708;
    public static final int FILE_TYPE_MP3 = 1;
    public static final int FILE_TYPE_MP4 = 701;
    public static final int FILE_TYPE_OGG = 7;
    public static final int FILE_TYPE_OGV = 714;
    public static final int FILE_TYPE_RAW = 719;
    public static final int FILE_TYPE_RM = 711;
    public static final int FILE_TYPE_SMF = 11;
    public static final int FILE_TYPE_SWF = 718;
    public static final int FILE_TYPE_VIVO = 715;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_WMV = 705;
    public static final int FILE_TYPE_WTV = 716;
    private static final int FIRST_AUDIO_FILE_TYPE = 1;
    private static final int FIRST_VIDEO_FILE_TYPE = 701;
    private static final int LAST_AUDIO_FILE_TYPE = 14;
    private static final int LAST_VIDEO_FILE_TYPE = 719;
    protected static final String sFileExtensions;
    private static HashMap<String, MediaFileType> sFileTypeMap = new HashMap();
    private static HashMap<String, Integer> sMimeTypeMap = new HashMap();

    protected static class MediaFileType {
        int fileType;
        String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    static {
        addFileType("M1V", 701, "video/mpeg");
        addFileType("MP2", 701, "video/mpeg");
        addFileType("MPE", 701, "video/mpeg");
        addFileType("MPG", 701, "video/mpeg");
        addFileType("MPEG", 701, "video/mpeg");
        addFileType("MP4", 701, "video/mp4");
        addFileType("M4V", 702, "video/mp4");
        addFileType("3GP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("3G2", 704, "video/3gpp2");
        addFileType("3GPP2", 704, "video/3gpp2");
        addFileType("MKV", FILE_TYPE_MKV, "video/x-matroska");
        addFileType("WEBM", FILE_TYPE_MKV, "video/x-matroska");
        addFileType("MTS", FILE_TYPE_MP2TS, "video/mp2ts");
        addFileType("TS", FILE_TYPE_MP2TS, "video/mp2ts");
        addFileType("TP", FILE_TYPE_MP2TS, "video/mp2ts");
        addFileType("WMV", FILE_TYPE_WMV, "video/x-ms-wmv");
        addFileType("ASF", FILE_TYPE_ASF, "video/x-ms-asf");
        addFileType("ASX", FILE_TYPE_ASF, "video/x-ms-asf");
        addFileType("FLV", FILE_TYPE_FLV, "video/x-flv");
        addFileType("F4V", FILE_TYPE_FLV, "video/x-flv");
        addFileType("HLV", FILE_TYPE_FLV, "video/x-flv");
        addFileType("MOV", FILE_TYPE_MOV, "video/quicktime");
        addFileType("QT", FILE_TYPE_MOV, "video/quicktime");
        addFileType("RM", FILE_TYPE_RM, "video/x-pn-realvideo");
        addFileType("RMVB", FILE_TYPE_RM, "video/x-pn-realvideo");
        addFileType("VOB", FILE_TYPE_DVD, "video/dvd");
        addFileType("DAT", FILE_TYPE_DVD, "video/dvd");
        addFileType("AVI", FILE_TYPE_DIVX, "video/x-divx");
        addFileType("OGV", FILE_TYPE_OGV, "video/ogg");
        addFileType("OGG", FILE_TYPE_OGV, "video/ogg");
        addFileType("VIV", FILE_TYPE_VIVO, "video/vnd.vivo");
        addFileType("VIVO", FILE_TYPE_VIVO, "video/vnd.vivo");
        addFileType("WTV", FILE_TYPE_WTV, "video/wtv");
        addFileType("AVS", FILE_TYPE_AVS, "video/avs-video");
        addFileType("SWF", FILE_TYPE_SWF, "video/x-shockwave-flash");
        addFileType("YUV", 719, "video/x-raw-yuv");
        StringBuilder builder = new StringBuilder();
        for (String append : sFileTypeMap.keySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(append);
        }
        sFileExtensions = builder.toString();
    }

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
    }

    public static boolean isAudioFileType(int fileType) {
        return fileType >= 1 && fileType <= 14;
    }

    public static boolean isVideoFileType(int fileType) {
        return fileType >= 701 && fileType <= 719;
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0) {
            return null;
        }
        return (MediaFileType) sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase());
    }

    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = (Integer) sMimeTypeMap.get(mimeType);
        return value == null ? 0 : value.intValue();
    }
}
