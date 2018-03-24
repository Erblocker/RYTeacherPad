package io.vov.vitamio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import io.vov.vitamio.utils.FileUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class MediaMetadataRetriever {
    public static final String METADATA_KEY_ALBUM = "album";
    public static final String METADATA_KEY_ALBUM_ARTIST = "album_artist";
    public static final String METADATA_KEY_ARTIST = "artist";
    public static final String METADATA_KEY_AUDIO_CODEC = "audio_codec";
    public static final String METADATA_KEY_AUTHOR = "author";
    public static final String METADATA_KEY_COMMENT = "comment";
    public static final String METADATA_KEY_COMPOSER = "composer";
    public static final String METADATA_KEY_COPYRIGHT = "copyright";
    public static final String METADATA_KEY_CREATION_TIME = "creation_time";
    public static final String METADATA_KEY_DATE = "date";
    public static final String METADATA_KEY_DISC = "disc";
    public static final String METADATA_KEY_DURATION = "duration";
    public static final String METADATA_KEY_ENCODED_BY = "encoded_by";
    public static final String METADATA_KEY_ENCODER = "encoder";
    public static final String METADATA_KEY_FILENAME = "filename";
    public static final String METADATA_KEY_GENRE = "genre";
    public static final String METADATA_KEY_HAS_AUDIO = "has_audio";
    public static final String METADATA_KEY_HAS_VIDEO = "has_video";
    public static final String METADATA_KEY_LANGUAGE = "language";
    public static final String METADATA_KEY_NUM_TRACKS = "num_tracks";
    public static final String METADATA_KEY_PERFORMER = "performer";
    public static final String METADATA_KEY_PUBLISHER = "publisher";
    public static final String METADATA_KEY_SERVICE_NAME = "service_name";
    public static final String METADATA_KEY_SERVICE_PROVIDER = "service_provider";
    public static final String METADATA_KEY_TITLE = "title";
    public static final String METADATA_KEY_TRACK = "track";
    public static final String METADATA_KEY_VARIANT_BITRATE = "bitrate";
    public static final String METADATA_KEY_VIDEO_CODEC = "video_codec";
    public static final String METADATA_KEY_VIDEO_HEIGHT = "height";
    public static final String METADATA_KEY_VIDEO_ROTATION = "rotate";
    public static final String METADATA_KEY_VIDEO_WIDTH = "width";
    private AssetFileDescriptor mFD = null;
    private int mNativeContext;

    private native void _release();

    private static native boolean loadFFmpeg_native(String str);

    private final native void native_finalize();

    private static final native void native_init();

    private native void native_setup();

    public native String extractMetadata(String str) throws IllegalStateException;

    public native byte[] getEmbeddedPicture() throws IllegalStateException;

    public native Bitmap getFrameAtTime(long j) throws IllegalStateException;

    public native void setDataSource(FileDescriptor fileDescriptor) throws IOException, IllegalArgumentException, IllegalStateException;

    public native void setDataSource(String str) throws IOException, IllegalArgumentException, IllegalStateException;

    static {
        String LIB_ROOT = "";
        try {
            if (new File(Vitamio.getLibraryPath() + "libstlport_shared.so").exists()) {
                LIB_ROOT = Vitamio.getLibraryPath();
            } else if (new File(Vitamio.getDataPath() + "libstlport_shared.so").exists()) {
                LIB_ROOT = Vitamio.getDataPath();
            }
        } catch (Exception e) {
            Log.e("scanner err", " load library err ");
        }
        if (LIB_ROOT == null) {
            System.loadLibrary("stlport_shared");
            System.loadLibrary("vscanner");
            loadFFmpeg_native("libffmpeg.so");
        } else {
            System.load(new StringBuilder(String.valueOf(LIB_ROOT)).append("libstlport_shared.so").toString());
            System.load(new StringBuilder(String.valueOf(LIB_ROOT)).append("libvscanner.so").toString());
            loadFFmpeg_native(new StringBuilder(String.valueOf(LIB_ROOT)).append("libffmpeg.so").toString());
        }
        native_init();
    }

    public MediaMetadataRetriever(Context ctx) {
        native_setup();
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (context == null || uri == null) {
            throw new IllegalArgumentException();
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            setDataSource(FileUtils.getPath(uri.toString()));
            return;
        }
        try {
            this.mFD = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (this.mFD != null) {
                setDataSource(this.mFD.getParcelFileDescriptor().getFileDescriptor());
            }
        } catch (Exception e) {
            closeFD();
            Log.e("Couldn't open file on client side, trying server side %s", uri.toString());
            setDataSource(uri.toString());
        }
    }

    public void release() {
        _release();
        closeFD();
    }

    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }

    private void closeFD() {
        if (this.mFD != null) {
            try {
                this.mFD.close();
            } catch (IOException e) {
            }
            this.mFD = null;
        }
    }
}
