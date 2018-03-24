package io.vov.vitamio;

import android.util.SparseArray;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import org.apache.http.protocol.HTTP;

public class Metadata {
    public static final int ALBUM = 4;
    public static final int ALBUM_ART = 14;
    public static final int ANY = 0;
    public static final int ARTIST = 5;
    public static final int AUDIO_BIT_RATE = 18;
    public static final int AUDIO_CODEC = 23;
    public static final int AUDIO_SAMPLE_RATE = 20;
    public static final int AUTHOR = 6;
    public static final int BIT_RATE = 17;
    public static final int CD_TRACK_MAX = 12;
    public static final int CD_TRACK_NUM = 11;
    public static final int COMMENT = 2;
    public static final int COMPOSER = 7;
    public static final int COPYRIGHT = 3;
    public static final int DATE = 9;
    public static final int DRM_CRIPPLED = 28;
    public static final int DURATION = 10;
    private static final int FIRST_CUSTOM = 8192;
    public static final int GENRE = 8;
    private static final int LAST_SYSTEM = 32;
    public static final int LENGTH = 16;
    public static final int MIME_TYPE = 22;
    public static final int NUM_TRACKS = 27;
    public static final int PAUSE_AVAILABLE = 29;
    public static final int RATING = 13;
    public static final int SEEK_AVAILABLE = 32;
    public static final int SEEK_BACKWARD_AVAILABLE = 30;
    public static final int SEEK_FORWARD_AVAILABLE = 31;
    public static final int TITLE = 1;
    public static final int VIDEO_BIT_RATE = 19;
    public static final int VIDEO_CODEC = 24;
    public static final int VIDEO_FRAME = 15;
    public static final int VIDEO_FRAME_RATE = 21;
    public static final int VIDEO_HEIGHT = 25;
    public static final int VIDEO_WIDTH = 26;
    private String mEncoding = HTTP.UTF_8;
    private SparseArray<byte[]> mMeta = new SparseArray();

    public boolean parse(Map<byte[], byte[]> meta, String encoding) {
        byte[] value = null;
        this.mEncoding = encoding;
        for (byte[] keyBytes : meta.keySet()) {
            String key;
            try {
                key = new String(keyBytes, this.mEncoding).trim().toLowerCase(Locale.US);
            } catch (UnsupportedEncodingException e) {
                key = new String(keyBytes).trim().toLowerCase(Locale.US);
            }
            value = (byte[]) meta.get(keyBytes);
            if (key.equals("title")) {
                this.mMeta.put(1, value);
            } else if (key.equals("comment")) {
                this.mMeta.put(2, value);
            } else if (key.equals(MediaMetadataRetriever.METADATA_KEY_COPYRIGHT)) {
                this.mMeta.put(3, value);
            } else if (key.equals("album")) {
                this.mMeta.put(4, value);
            } else if (key.equals("artist")) {
                this.mMeta.put(5, value);
            } else if (key.equals(MediaMetadataRetriever.METADATA_KEY_AUTHOR)) {
                this.mMeta.put(6, value);
            } else if (key.equals("composer")) {
                this.mMeta.put(7, value);
            } else if (key.equals(MediaMetadataRetriever.METADATA_KEY_GENRE)) {
                this.mMeta.put(8, value);
            } else if (key.equals(MediaMetadataRetriever.METADATA_KEY_CREATION_TIME) || key.equals(MediaMetadataRetriever.METADATA_KEY_DATE)) {
                this.mMeta.put(9, value);
            } else if (key.equals("duration")) {
                this.mMeta.put(10, value);
            } else if (key.equals("length")) {
                this.mMeta.put(16, value);
            } else if (key.equals("bit_rate")) {
                this.mMeta.put(17, value);
            } else if (key.equals("audio_bit_rate")) {
                this.mMeta.put(18, value);
            } else if (key.equals("video_bit_rate")) {
                this.mMeta.put(19, value);
            } else if (key.equals("audio_sample_rate")) {
                this.mMeta.put(20, value);
            } else if (key.equals("video_frame_rate")) {
                this.mMeta.put(21, value);
            } else if (key.equals("format")) {
                this.mMeta.put(22, value);
            } else if (key.equals(MediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC)) {
                this.mMeta.put(23, value);
            } else if (key.equals(MediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC)) {
                this.mMeta.put(24, value);
            } else if (key.equals("video_height")) {
                this.mMeta.put(25, value);
            } else if (key.equals("video_width")) {
                this.mMeta.put(26, value);
            } else if (key.equals(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)) {
                this.mMeta.put(27, value);
            } else if (key.equals("cap_pause")) {
                this.mMeta.put(29, value);
            } else if (key.equals("cap_seek")) {
                this.mMeta.put(32, value);
            }
        }
        return true;
    }

    public boolean has(int metadataId) {
        if (checkMetadataId(metadataId)) {
            return this.mMeta.indexOfKey(metadataId) >= 0;
        } else {
            throw new IllegalArgumentException("Invalid key: " + metadataId);
        }
    }

    public String getString(int key) {
        byte[] value = (byte[]) this.mMeta.get(key);
        if (value == null) {
            return null;
        }
        try {
            return new String(value, this.mEncoding);
        } catch (UnsupportedEncodingException e) {
            return new String(value);
        }
    }

    public int getInt(int key) {
        try {
            return Integer.parseInt(getString(key));
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean getBoolean(int key) {
        try {
            return Boolean.parseBoolean(getString(key));
        } catch (Exception e) {
            return false;
        }
    }

    public long getLong(int key) {
        try {
            return Long.parseLong(getString(key));
        } catch (Exception e) {
            return -1;
        }
    }

    public double getDouble(int key) {
        try {
            return Double.parseDouble(getString(key));
        } catch (Exception e) {
            return -1.0d;
        }
    }

    public byte[] getByteArray(int key) {
        return (byte[]) this.mMeta.get(key);
    }

    private boolean checkMetadataId(int val) {
        if (val <= 0 || (32 < val && val < 8192)) {
            return false;
        }
        return true;
    }
}
