package io.vov.vitamio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import io.vov.vitamio.utils.FileUtils;
import io.vov.vitamio.utils.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressLint({"NewApi"})
public class MediaPlayer {
    public static final int CACHE_INFO_NO_SPACE = 1;
    public static final int CACHE_INFO_STREAM_NOT_SUPPORT = 2;
    public static final int CACHE_TYPE_COMPLETE = 5;
    public static final int CACHE_TYPE_NOT_AVAILABLE = 1;
    public static final int CACHE_TYPE_SPEED = 4;
    public static final int CACHE_TYPE_START = 2;
    public static final int CACHE_TYPE_UPDATE = 3;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_CACHE = 300;
    private static final String MEDIA_CACHING_INFO = "caching_info";
    private static final String MEDIA_CACHING_SEGMENTS = "caching_segment";
    private static final String MEDIA_CACHING_TYPE = "caching_type";
    private static final int MEDIA_CACHING_UPDATE = 2000;
    private static final int MEDIA_ERROR = 100;
    public static final int MEDIA_ERROR_IO = -5;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    private static final int MEDIA_HW_ERROR = 400;
    private static final int MEDIA_INFO = 200;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_DOWNLOAD_RATE_CHANGED = 901;
    public static final int MEDIA_INFO_FILE_OPEN_OK = 704;
    public static final int MEDIA_INFO_GET_CODEC_INFO_ERROR = 1002;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_UNKNOW_TYPE = 1001;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final String MEDIA_SUBTITLE_BYTES = "sub_bytes";
    private static final String MEDIA_SUBTITLE_STRING = "sub_string";
    private static final String MEDIA_SUBTITLE_TYPE = "sub_type";
    private static final int MEDIA_TIMED_TEXT = 1000;
    private static AtomicBoolean NATIVE_OMX_LOADED = new AtomicBoolean(false);
    private static final int SUBTITLE_BITMAP = 1;
    public static final int SUBTITLE_EXTERNAL = 1;
    public static final int SUBTITLE_INTERNAL = 0;
    private static final int SUBTITLE_TEXT = 0;
    public static final String[] SUB_TYPES = new String[]{".srt", ".ssa", ".smi", ".txt", ".sub", ".ass", ".webvtt"};
    public static final int VIDEOCHROMA_RGB565 = 0;
    public static final int VIDEOCHROMA_RGBA = 1;
    public static final int VIDEOQUALITY_HIGH = 16;
    public static final int VIDEOQUALITY_LOW = -16;
    public static final int VIDEOQUALITY_MEDIUM = 0;
    private static String path;
    int channels;
    private AudioTrack mAudioTrack;
    private int mAudioTrackBufferSize;
    private Bitmap mBitmap;
    private ByteBuffer mByteBuffer;
    private Context mContext;
    private EventHandler mEventHandler;
    private AssetFileDescriptor mFD;
    private boolean mInBuffering;
    private TrackInfo[] mInbandTracks;
    private Surface mLocalSurface;
    private Metadata mMeta;
    private boolean mNeedResume;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnCachingUpdateListener mOnCachingUpdateListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnHWRenderFailedListener mOnHWRenderFailedListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnTimedTextListener mOnTimedTextListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private TrackInfo mOutOfBandTracks;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;
    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;
    private WakeLock mWakeLock;
    int sampleRateInHz;

    @SuppressLint({"HandlerLeak"})
    private class EventHandler extends Handler {
        private Bundle mData;
        private MediaPlayer mMediaPlayer;

        public EventHandler(MediaPlayer mp, Looper looper) {
            super(looper);
            this.mMediaPlayer = mp;
        }

        public void release() {
            this.mMediaPlayer = null;
        }

        private void onInfo(Message msg) {
            switch (msg.arg1) {
                case 701:
                    MediaPlayer.this.mInBuffering = true;
                    if (MediaPlayer.this.isPlaying()) {
                        MediaPlayer.this._pause();
                        MediaPlayer.this.mNeedResume = true;
                        break;
                    }
                    break;
                case 702:
                    MediaPlayer.this.mInBuffering = false;
                    if (MediaPlayer.this.mNeedResume) {
                        MediaPlayer.this._start();
                        MediaPlayer.this.mNeedResume = false;
                        break;
                    }
                    break;
            }
            if (MediaPlayer.this.mOnInfoListener != null) {
                MediaPlayer.this.mOnInfoListener.onInfo(this.mMediaPlayer, msg.arg1, msg.arg2);
            }
        }

        private void onBufferingUpdate(Message msg) {
            int percent = msg.arg1;
            if (MediaPlayer.this.mOnBufferingUpdateListener != null) {
                MediaPlayer.this.mOnBufferingUpdateListener.onBufferingUpdate(this.mMediaPlayer, msg.arg1);
            }
            if (percent >= 100 && MediaPlayer.this.mInBuffering) {
                MediaPlayer.this.mInBuffering = false;
                if (MediaPlayer.this.mNeedResume) {
                    MediaPlayer.this._start();
                    MediaPlayer.this.mNeedResume = false;
                }
                if (MediaPlayer.this.mOnInfoListener != null) {
                    MediaPlayer.this.mOnInfoListener.onInfo(this.mMediaPlayer, 702, percent);
                }
            }
        }

        public void handleMessage(Message msg) {
            if (this.mMediaPlayer != null) {
                switch (msg.what) {
                    case 0:
                    case 300:
                        return;
                    case 1:
                        if (MediaPlayer.this.mOnPreparedListener != null) {
                            MediaPlayer.this.mOnPreparedListener.onPrepared(this.mMediaPlayer);
                            return;
                        }
                        return;
                    case 2:
                        if (MediaPlayer.this.mOnCompletionListener != null) {
                            MediaPlayer.this.mOnCompletionListener.onCompletion(this.mMediaPlayer);
                        }
                        MediaPlayer.this.stayAwake(false);
                        return;
                    case 3:
                        onBufferingUpdate(msg);
                        return;
                    case 4:
                        if (MediaPlayer.this.isPlaying()) {
                            MediaPlayer.this.stayAwake(true);
                        }
                        if (MediaPlayer.this.mOnSeekCompleteListener != null) {
                            MediaPlayer.this.mOnSeekCompleteListener.onSeekComplete(this.mMediaPlayer);
                            return;
                        }
                        return;
                    case 5:
                        if (MediaPlayer.this.mOnVideoSizeChangedListener != null) {
                            MediaPlayer.this.mOnVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, msg.arg1, msg.arg2);
                            return;
                        }
                        return;
                    case 100:
                        Log.e("Error (%d, %d)", Integer.valueOf(msg.arg1), Integer.valueOf(msg.arg2));
                        boolean error_was_handled = false;
                        if (MediaPlayer.this.mOnErrorListener != null) {
                            error_was_handled = MediaPlayer.this.mOnErrorListener.onError(this.mMediaPlayer, msg.arg1, msg.arg2);
                        }
                        if (!(MediaPlayer.this.mOnCompletionListener == null || error_was_handled)) {
                            MediaPlayer.this.mOnCompletionListener.onCompletion(this.mMediaPlayer);
                        }
                        MediaPlayer.this.stayAwake(false);
                        return;
                    case 200:
                        Log.i("Info (%d, %d)", Integer.valueOf(msg.arg1), Integer.valueOf(msg.arg2));
                        if (MediaPlayer.this.mOnInfoListener != null) {
                            MediaPlayer.this.mOnInfoListener.onInfo(this.mMediaPlayer, msg.arg1, msg.arg2);
                            return;
                        }
                        return;
                    case 400:
                        if (MediaPlayer.this.mOnHWRenderFailedListener != null) {
                            MediaPlayer.this.mOnHWRenderFailedListener.onFailed();
                            return;
                        }
                        return;
                    case MediaPlayer.MEDIA_TIMED_TEXT /*1000*/:
                        this.mData = msg.getData();
                        if (this.mData.getInt(MediaPlayer.MEDIA_SUBTITLE_TYPE) == 0) {
                            Log.i("Subtitle : %s", this.mData.getString(MediaPlayer.MEDIA_SUBTITLE_STRING));
                            if (MediaPlayer.this.mOnTimedTextListener != null) {
                                MediaPlayer.this.mOnTimedTextListener.onTimedText(this.mData.getString(MediaPlayer.MEDIA_SUBTITLE_STRING));
                                return;
                            }
                            return;
                        } else if (this.mData.getInt(MediaPlayer.MEDIA_SUBTITLE_TYPE) == 1) {
                            Log.i("Subtitle : bitmap", new Object[0]);
                            if (MediaPlayer.this.mOnTimedTextListener != null) {
                                MediaPlayer.this.mOnTimedTextListener.onTimedTextUpdate(this.mData.getByteArray(MediaPlayer.MEDIA_SUBTITLE_BYTES), msg.arg1, msg.arg2);
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    case MediaPlayer.MEDIA_CACHING_UPDATE /*2000*/:
                        if (MediaPlayer.this.mOnCachingUpdateListener != null) {
                            int cacheType = msg.getData().getInt(MediaPlayer.MEDIA_CACHING_TYPE);
                            if (cacheType == 1) {
                                MediaPlayer.this.mOnCachingUpdateListener.onCachingNotAvailable(this.mMediaPlayer, msg.getData().getInt(MediaPlayer.MEDIA_CACHING_INFO));
                                return;
                            } else if (cacheType == 3) {
                                MediaPlayer.this.mOnCachingUpdateListener.onCachingUpdate(this.mMediaPlayer, msg.getData().getLongArray(MediaPlayer.MEDIA_CACHING_SEGMENTS));
                                return;
                            } else if (cacheType == 4) {
                                MediaPlayer.this.mOnCachingUpdateListener.onCachingSpeed(this.mMediaPlayer, msg.getData().getInt(MediaPlayer.MEDIA_CACHING_INFO));
                                return;
                            } else if (cacheType == 2) {
                                MediaPlayer.this.mOnCachingUpdateListener.onCachingStart(this.mMediaPlayer);
                                return;
                            } else if (cacheType == 5) {
                                MediaPlayer.this.mOnCachingUpdateListener.onCachingComplete(this.mMediaPlayer);
                                return;
                            } else {
                                return;
                            }
                        }
                        return;
                    default:
                        Log.e("Unknown message type " + msg.what, new Object[0]);
                        return;
                }
            }
        }
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(MediaPlayer mediaPlayer, int i);
    }

    public interface OnCachingUpdateListener {
        void onCachingComplete(MediaPlayer mediaPlayer);

        void onCachingNotAvailable(MediaPlayer mediaPlayer, int i);

        void onCachingSpeed(MediaPlayer mediaPlayer, int i);

        void onCachingStart(MediaPlayer mediaPlayer);

        void onCachingUpdate(MediaPlayer mediaPlayer, long[] jArr);
    }

    public interface OnCompletionListener {
        void onCompletion(MediaPlayer mediaPlayer);
    }

    public interface OnErrorListener {
        boolean onError(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnHWRenderFailedListener {
        void onFailed();
    }

    public interface OnInfoListener {
        boolean onInfo(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnPreparedListener {
        void onPrepared(MediaPlayer mediaPlayer);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(MediaPlayer mediaPlayer);
    }

    public interface OnTimedTextListener {
        void onTimedText(String str);

        void onTimedTextUpdate(byte[] bArr, int i, int i2);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2);
    }

    public static class TrackInfo {
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        final SparseArray<MediaFormat> mTrackInfoArray;
        final int mTrackType;

        TrackInfo(int trackType, SparseArray<MediaFormat> trackInfoArray) {
            this.mTrackType = trackType;
            this.mTrackInfoArray = trackInfoArray;
        }

        public int getTrackType() {
            return this.mTrackType;
        }

        public SparseArray<MediaFormat> getTrackInfoArray() {
            return this.mTrackInfoArray;
        }
    }

    private native void _pause() throws IllegalStateException;

    private native void _release();

    private native void _reset();

    private native void _setDataSegmentsSource(String[] strArr, String str);

    private native void _setDataSource(String str, String[] strArr, String[] strArr2) throws IOException, IllegalArgumentException, IllegalStateException;

    private native void _setVideoSurface(Surface surface);

    private native void _start() throws IllegalStateException;

    private native void _stop() throws IllegalStateException;

    private native int getVideoHeight_a();

    private native int getVideoWidth_a();

    private static native boolean loadFFmpeg_native(String str);

    private static native boolean loadOMX_native(String str);

    private static native boolean loadVAO_native(String str);

    private static native boolean loadVVO_native(String str);

    private final native void native_finalize();

    private final native boolean native_getMetadata(Map<byte[], byte[]> map);

    private final native boolean native_getTrackInfo(SparseArray<byte[]> sparseArray);

    private final native void native_init();

    private native void selectOrDeselectTrack(int i, boolean z);

    private static native void unloadOMX_native();

    protected native void _releaseVideoSurface();

    public native void addTimedTextSource(String str);

    public native void audioInitedOk(long j);

    public native int getAudioTrack();

    public native int getBufferProgress();

    public native Bitmap getCurrentFrame();

    public native long getCurrentPosition();

    public native long getDuration();

    public native String getMetaEncoding();

    public native int getTimedTextLocation();

    public native String getTimedTextPath();

    public native int getTimedTextTrack();

    public native float getVideoAspectRatio();

    public native int getVideoHeight();

    public native int getVideoTrack();

    public native int getVideoWidth();

    public native boolean isBuffering();

    public native boolean isLooping();

    public native boolean isPlaying();

    public native void prepare() throws IOException, IllegalStateException;

    public native void prepareAsync() throws IllegalStateException;

    public native void seekTo(long j) throws IllegalStateException;

    public native void setAdaptiveStream(boolean z);

    public native void setAudioAmplify(float f);

    public native void setBufferSize(long j);

    public native void setCacheDirectory(String str);

    public native void setDataSource(FileDescriptor fileDescriptor) throws IOException, IllegalArgumentException, IllegalStateException;

    public native void setDeinterlace(boolean z);

    public native void setLooping(boolean z);

    public native void setMetaEncoding(String str);

    public native void setPlaybackSpeed(float f);

    public native void setTimedTextEncoding(String str);

    public native void setTimedTextShown(boolean z);

    public native void setUseCache(boolean z);

    public native void setVideoChroma(int i);

    public native void setVideoQuality(int i);

    public native void setVolume(float f, float f2);

    static {
        String LIB_ROOT = "";
        try {
            if (new File(Vitamio.getLibraryPath() + "libstlport_shared.so").exists()) {
                LIB_ROOT = Vitamio.getLibraryPath();
            } else if (new File(Vitamio.getDataPath() + "libstlport_shared.so").exists()) {
                LIB_ROOT = Vitamio.getDataPath();
            }
            if (LIB_ROOT == null) {
                System.loadLibrary("stlport_shared");
                System.loadLibrary("vplayer");
                loadFFmpeg_native("libffmpeg.so");
                loadVVO_native("libvvo.9.so");
                loadVVO_native("libvvo.9.so");
                loadVAO_native("libvao.0.so");
                return;
            }
            System.load(new StringBuilder(String.valueOf(LIB_ROOT)).append("libstlport_shared.so").toString());
            System.load(new StringBuilder(String.valueOf(LIB_ROOT)).append("libvplayer.so").toString());
            loadFFmpeg_native(new StringBuilder(String.valueOf(LIB_ROOT)).append("libffmpeg.so").toString());
            loadVVO_native(new StringBuilder(String.valueOf(LIB_ROOT)).append("libvvo.9.so").toString());
            loadVAO_native(new StringBuilder(String.valueOf(LIB_ROOT)).append("libvao.0.so").toString());
        } catch (Exception e) {
            Log.e("load library err ", new Object[0]);
        }
    }

    public MediaPlayer(Context ctx) {
        this(ctx, false);
    }

    private static boolean load_omxnative_lib(String path, String name) {
        if (new File(Vitamio.getBrowserLibraryPath() + "/" + name).exists()) {
            return loadOMX_native(Vitamio.getBrowserLibraryPath() + "/" + name);
        }
        boolean load;
        if (path == "") {
            load = loadOMX_native(name);
        } else {
            load = loadOMX_native(new StringBuilder(String.valueOf(path)).append(name).toString());
        }
        return load;
    }

    private static boolean loadVVO_native_lib(String path, String name) {
        if (new File(Vitamio.getBrowserLibraryPath() + "/" + name).exists()) {
            return loadVVO_native(Vitamio.getBrowserLibraryPath() + "/" + name);
        }
        boolean load;
        if (path == "") {
            load = loadVVO_native(name);
        } else {
            load = loadVVO_native(new StringBuilder(String.valueOf(path)).append(name).toString());
        }
        return load;
    }

    private static boolean loadVAO_native_lib(String path, String name) {
        if (new File(Vitamio.getBrowserLibraryPath() + "/" + name).exists()) {
            return loadVAO_native(Vitamio.getBrowserLibraryPath() + "/" + name);
        }
        boolean load;
        if (path == "") {
            load = loadVAO_native(name);
        } else {
            load = loadVAO_native(new StringBuilder(String.valueOf(path)).append(name).toString());
        }
        return load;
    }

    private static boolean loadFFmpeg_native_lib(String path, String name) {
        if (new File(Vitamio.getBrowserLibraryPath() + "/" + name).exists()) {
            return loadFFmpeg_native(Vitamio.getBrowserLibraryPath() + "/" + name);
        }
        boolean load;
        if (path == "") {
            load = loadFFmpeg_native(name);
        } else {
            load = loadFFmpeg_native(new StringBuilder(String.valueOf(path)).append(name).toString());
        }
        return load;
    }

    private static boolean load_lib(String path, String name) {
        if (new File(Vitamio.getBrowserLibraryPath() + "/" + name).exists()) {
            System.load(Vitamio.getBrowserLibraryPath() + "/" + name);
        } else if (path == "") {
            System.load(name);
        } else {
            System.load(new StringBuilder(String.valueOf(path)).append(name).toString());
        }
        return true;
    }

    public MediaPlayer(Context ctx, boolean preferHWDecoder) {
        String LIB_ROOT;
        this.mWakeLock = null;
        this.mFD = null;
        this.mNeedResume = false;
        this.mInBuffering = false;
        this.mContext = ctx;
        if (VERSION.SDK_INT > 23) {
            LIB_ROOT = Vitamio.getLibraryPath();
        } else if (VERSION.SDK_INT > 20) {
            LIB_ROOT = "";
        } else {
            LIB_ROOT = Vitamio.getLibraryPath();
        }
        if (!preferHWDecoder) {
            try {
                unloadOMX_native();
            } catch (UnsatisfiedLinkError e) {
                Log.e("unloadOMX failed %s", e.toString());
            }
            NATIVE_OMX_LOADED.set(false);
        } else if (!NATIVE_OMX_LOADED.get()) {
            if (VERSION.SDK_INT > 17) {
                load_omxnative_lib(LIB_ROOT, "libOMX.18.so");
            } else if (VERSION.SDK_INT > 13) {
                load_omxnative_lib(LIB_ROOT, "libOMX.14.so");
            } else if (VERSION.SDK_INT > 10) {
                load_omxnative_lib(LIB_ROOT, "libOMX.11.so");
            } else {
                load_omxnative_lib(LIB_ROOT, "libOMX.9.so");
            }
            NATIVE_OMX_LOADED.set(true);
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        native_init();
    }

    private static void postEventFromNative(Object mediaplayer_ref, int what, int arg1, int arg2, Object obj) {
        MediaPlayer mp = (MediaPlayer) mediaplayer_ref;
        if (mp != null) {
            try {
                if (mp.mEventHandler != null) {
                    mp.mEventHandler.sendMessage(mp.mEventHandler.obtainMessage(what, arg1, arg2, obj));
                }
            } catch (Exception e) {
                Log.e("exception: " + e, new Object[0]);
            }
        }
    }

    public void setDisplay(SurfaceHolder sh) {
        if (sh == null) {
            releaseDisplay();
            return;
        }
        this.mSurfaceHolder = sh;
        this.mSurface = sh.getSurface();
        _setVideoSurface(this.mSurface);
        updateSurfaceScreenOn();
    }

    public void setSurface(Surface surface) {
        if (surface == null) {
            releaseDisplay();
            return;
        }
        this.mSurfaceHolder = null;
        this.mSurface = surface;
        _setVideoSurface(this.mSurface);
        updateSurfaceScreenOn();
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        _setDataSource(path, null, null);
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, null);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
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
            setDataSource(uri.toString(), (Map) headers);
        }
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String[] keys = null;
        String[] values = null;
        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];
            int i = 0;
            for (Entry<String, String> entry : headers.entrySet()) {
                keys[i] = (String) entry.getKey();
                values[i] = (String) entry.getValue();
                i++;
            }
        }
        setDataSource(path, keys, values);
    }

    public void setDataSource(String path, String[] keys, String[] values) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Uri uri = Uri.parse(path);
        if ("file".equals(uri.getScheme())) {
            path = uri.getPath();
        }
        File file = new File(path);
        if (file.exists()) {
            FileInputStream is = new FileInputStream(file);
            setDataSource(is.getFD());
            is.close();
            return;
        }
        _setDataSource(path, keys, values);
    }

    public void setDataSegments(String[] uris, String cacheDir) {
        _setDataSegmentsSource(uris, cacheDir);
    }

    public void setOnHWRenderFailedListener(OnHWRenderFailedListener l) {
        this.mOnHWRenderFailedListener = l;
    }

    public void start() throws IllegalStateException {
        stayAwake(true);
        if (this.mInBuffering) {
            this.mNeedResume = true;
        } else {
            _start();
        }
    }

    public void stop() throws IllegalStateException {
        stayAwake(false);
        _stop();
        this.mInBuffering = false;
        this.mNeedResume = false;
    }

    public void pause() throws IllegalStateException {
        stayAwake(false);
        this.mNeedResume = false;
        _pause();
    }

    @SuppressLint({"Wakelock"})
    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (this.mWakeLock != null) {
            if (this.mWakeLock.isHeld()) {
                washeld = true;
                this.mWakeLock.release();
            }
            this.mWakeLock = null;
        }
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(536870912 | mode, MediaPlayer.class.getName());
        this.mWakeLock.setReferenceCounted(false);
        if (washeld) {
            this.mWakeLock.acquire();
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (this.mScreenOnWhilePlaying != screenOn) {
            this.mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    @SuppressLint({"Wakelock"})
    private void stayAwake(boolean awake) {
        if (this.mWakeLock != null) {
            if (awake && !this.mWakeLock.isHeld()) {
                this.mWakeLock.acquire();
            } else if (!awake && this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
        this.mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        if (this.mSurfaceHolder != null) {
            SurfaceHolder surfaceHolder = this.mSurfaceHolder;
            boolean z = this.mScreenOnWhilePlaying && this.mStayAwake;
            surfaceHolder.setKeepScreenOn(z);
        }
    }

    public Metadata getMetadata() {
        if (this.mMeta == null) {
            this.mMeta = new Metadata();
            Map<byte[], byte[]> meta = new HashMap();
            if (!(native_getMetadata(meta) && this.mMeta.parse(meta, getMetaEncoding()))) {
                return null;
            }
        }
        return this.mMeta;
    }

    public void release() {
        stayAwake(false);
        updateSurfaceScreenOn();
        this.mOnPreparedListener = null;
        this.mOnBufferingUpdateListener = null;
        this.mOnCompletionListener = null;
        this.mOnSeekCompleteListener = null;
        this.mOnErrorListener = null;
        this.mOnInfoListener = null;
        this.mOnVideoSizeChangedListener = null;
        this.mOnCachingUpdateListener = null;
        this.mOnHWRenderFailedListener = null;
        if (this.mEventHandler != null) {
            this.mEventHandler.release();
        }
        _release();
        closeFD();
        this.mInBuffering = false;
        this.mNeedResume = false;
    }

    public void reset() {
        stayAwake(false);
        _reset();
        if (this.mEventHandler != null) {
            this.mEventHandler.removeCallbacksAndMessages(null);
        }
        closeFD();
        this.mInBuffering = false;
        this.mNeedResume = false;
    }

    private void closeFD() {
        if (this.mFD != null) {
            try {
                this.mFD.close();
            } catch (Throwable e) {
                Log.e("closeFD", e);
            }
            this.mFD = null;
        }
    }

    public TrackInfo[] getTrackInfo(String encoding) {
        TrackInfo[] trackInfo = getInbandTrackInfo(encoding);
        String timedTextPath = getTimedTextPath();
        if (TextUtils.isEmpty(timedTextPath)) {
            return trackInfo;
        }
        TrackInfo[] allTrackInfo = new TrackInfo[(trackInfo.length + 1)];
        System.arraycopy(trackInfo, 0, allTrackInfo, 0, trackInfo.length);
        int i = trackInfo.length;
        SparseArray<MediaFormat> trackInfoArray = new SparseArray();
        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setString("title", timedTextPath.substring(timedTextPath.lastIndexOf("/")));
        mediaFormat.setString("path", timedTextPath);
        SparseArray<MediaFormat> timedTextSparse = findTrackFromTrackInfo(3, trackInfo);
        if (timedTextSparse == null || timedTextSparse.size() == 0) {
            trackInfoArray.put(timedTextSparse.keyAt(0), mediaFormat);
        } else {
            trackInfoArray.put(timedTextSparse.keyAt(timedTextSparse.size() - 1), mediaFormat);
        }
        this.mOutOfBandTracks = new TrackInfo(4, trackInfoArray);
        allTrackInfo[i] = this.mOutOfBandTracks;
        return allTrackInfo;
    }

    private TrackInfo[] getInbandTrackInfo(String encoding) {
        if (this.mInbandTracks == null) {
            SparseArray<byte[]> trackSparse = new SparseArray();
            if (!native_getTrackInfo(trackSparse)) {
                return null;
            }
            int size = trackSparse.size();
            this.mInbandTracks = new TrackInfo[size];
            for (int i = 0; i < size; i++) {
                this.mInbandTracks[i] = new TrackInfo(trackSparse.keyAt(i), parseTrackInfo((byte[]) trackSparse.valueAt(i), encoding));
            }
        }
        return this.mInbandTracks;
    }

    public TrackInfo[] getTrackInfo() {
        return getTrackInfo(Charset.defaultCharset().name());
    }

    private SparseArray<MediaFormat> parseTrackInfo(byte[] tracks, String encoding) {
        String trackString;
        int i = 0;
        SparseArray<MediaFormat> trackSparse = new SparseArray();
        try {
            trackString = new String(tracks, encoding);
        } catch (Exception e) {
            Log.e("getTrackMap exception", new Object[0]);
            trackString = new String(tracks);
        }
        String[] split = trackString.split("!#!");
        int length = split.length;
        while (i < length) {
            MediaFormat mediaFormat = null;
            try {
                String[] formats = split[i].split("\\.");
                if (formats != null) {
                    int trackNum = Integer.parseInt(formats[0]);
                    if (formats.length == 3) {
                        mediaFormat = MediaFormat.createSubtitleFormat(formats[2], formats[1]);
                    } else if (formats.length == 2) {
                        mediaFormat = MediaFormat.createSubtitleFormat("", formats[1]);
                    }
                    trackSparse.put(trackNum, mediaFormat);
                }
            } catch (NumberFormatException e2) {
            }
            i++;
        }
        return trackSparse;
    }

    public SparseArray<MediaFormat> findTrackFromTrackInfo(int mediaTrackType, TrackInfo[] trackInfo) {
        for (int i = 0; i < trackInfo.length; i++) {
            if (trackInfo[i].getTrackType() == mediaTrackType) {
                return trackInfo[i].getTrackInfoArray();
            }
        }
        return null;
    }

    public void selectTrack(int index) {
        selectOrDeselectBandTrack(index, true);
    }

    public void deselectTrack(int index) {
        selectOrDeselectBandTrack(index, false);
    }

    private void selectOrDeselectBandTrack(int index, boolean select) {
        if (this.mOutOfBandTracks != null) {
            SparseArray<MediaFormat> mediaSparse = this.mOutOfBandTracks.getTrackInfoArray();
            MediaFormat mediaFormat = (MediaFormat) mediaSparse.valueAt(0);
            if (index == mediaSparse.keyAt(0) && select) {
                addTimedTextSource(mediaFormat.getString("path"));
                return;
            }
        }
        selectOrDeselectTrack(index, select);
    }

    protected void finalize() {
        native_finalize();
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mOnCompletionListener = listener;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.mOnBufferingUpdateListener = listener;
    }

    public void setOnCachingUpdateListener(OnCachingUpdateListener listener) {
        this.mOnCachingUpdateListener = listener;
    }

    private void updateCacheStatus(int type, int info, long[] segments) {
        if (this.mEventHandler != null) {
            Message m = this.mEventHandler.obtainMessage(MEDIA_CACHING_UPDATE);
            Bundle b = m.getData();
            b.putInt(MEDIA_CACHING_TYPE, type);
            b.putInt(MEDIA_CACHING_INFO, info);
            b.putLongArray(MEDIA_CACHING_SEGMENTS, segments);
            this.mEventHandler.sendMessage(m);
        }
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener = listener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.mOnErrorListener = listener;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    public void setOnTimedTextListener(OnTimedTextListener listener) {
        this.mOnTimedTextListener = listener;
    }

    private void updateSub(int subType, byte[] bytes, String encoding, int width, int height) {
        if (this.mEventHandler != null) {
            Message m = this.mEventHandler.obtainMessage(MEDIA_TIMED_TEXT, width, height);
            Bundle b = m.getData();
            if (subType == 0) {
                b.putInt(MEDIA_SUBTITLE_TYPE, 0);
                if (encoding == null) {
                    b.putString(MEDIA_SUBTITLE_STRING, new String(bytes));
                } else {
                    try {
                        b.putString(MEDIA_SUBTITLE_STRING, new String(bytes, encoding.trim()));
                    } catch (Throwable e) {
                        Log.e("updateSub", e);
                        b.putString(MEDIA_SUBTITLE_STRING, new String(bytes));
                    }
                }
            } else if (subType == 1) {
                b.putInt(MEDIA_SUBTITLE_TYPE, 1);
                b.putByteArray(MEDIA_SUBTITLE_BYTES, bytes);
            }
            this.mEventHandler.sendMessage(m);
        }
    }

    public void releaseDisplay() {
        _releaseVideoSurface();
        this.mSurfaceHolder = null;
        this.mSurface = null;
    }

    @SuppressLint({"NewApi"})
    private int audioTrackInit(int sampleRateInHz, int channels) {
        audioTrackRelease();
        int channelConfig = channels >= 2 ? 12 : 4;
        try {
            this.mAudioTrackBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, 2);
            this.mAudioTrack = new AudioTrack(3, sampleRateInHz, channelConfig, 2, this.mAudioTrackBufferSize, 1);
        } catch (Throwable e) {
            this.mAudioTrackBufferSize = 0;
            Log.e("audioTrackInit", e);
        }
        return this.mAudioTrackBufferSize;
    }

    public int audioTrackInit() {
        audioTrackRelease();
        int channelConfig = this.channels >= 2 ? 12 : 4;
        try {
            this.mAudioTrackBufferSize = AudioTrack.getMinBufferSize(this.sampleRateInHz, channelConfig, 2);
            this.mAudioTrack = new AudioTrack(3, this.sampleRateInHz, channelConfig, 2, this.mAudioTrackBufferSize, 1);
        } catch (Throwable e) {
            this.mAudioTrackBufferSize = 0;
            Log.e("audioTrackInit", e);
        }
        return this.mAudioTrackBufferSize;
    }

    private void audioTrackSetVolume(float leftVolume, float rightVolume) {
        if (this.mAudioTrack != null) {
            this.mAudioTrack.setStereoVolume(leftVolume, rightVolume);
        }
    }

    private void audioTrackWrite(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (this.mAudioTrack != null && this.mAudioTrack.getPlayState() == 3) {
            while (sizeInBytes > 0) {
                int written;
                if (sizeInBytes > this.mAudioTrackBufferSize) {
                    written = this.mAudioTrackBufferSize;
                } else {
                    written = sizeInBytes;
                }
                this.mAudioTrack.write(audioData, offsetInBytes, written);
                sizeInBytes -= written;
                offsetInBytes += written;
            }
        }
    }

    private void audioTrackStart() {
        if (this.mAudioTrack != null && this.mAudioTrack.getState() == 1 && this.mAudioTrack.getPlayState() != 3) {
            this.mAudioTrack.play();
        }
    }

    private void audioTrackPause() {
        if (this.mAudioTrack != null && this.mAudioTrack.getState() == 1) {
            this.mAudioTrack.pause();
        }
    }

    private void audioTrackRelease() {
        if (this.mAudioTrack != null) {
            if (this.mAudioTrack.getState() == 1) {
                this.mAudioTrack.stop();
            }
            this.mAudioTrack.release();
        }
        this.mAudioTrack = null;
    }

    public int getAudioSessionId() {
        return this.mAudioTrack.getAudioSessionId();
    }

    private ByteBuffer surfaceInit() {
        ByteBuffer byteBuffer;
        synchronized (this) {
            this.mLocalSurface = this.mSurface;
            int w = getVideoWidth_a();
            int h = getVideoHeight_a();
            if (this.mLocalSurface == null || w == 0 || h == 0) {
                this.mBitmap = null;
                this.mByteBuffer = null;
            } else {
                this.mBitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
                this.mByteBuffer = ByteBuffer.allocateDirect((w * h) * 2);
            }
            byteBuffer = this.mByteBuffer;
        }
        return byteBuffer;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void surfaceRender() {
        synchronized (this) {
            if (this.mLocalSurface == null || !this.mLocalSurface.isValid() || this.mBitmap == null || this.mByteBuffer == null) {
            } else {
                try {
                    Canvas c = this.mLocalSurface.lockCanvas(null);
                    this.mBitmap.copyPixelsFromBuffer(this.mByteBuffer);
                    c.drawBitmap(this.mBitmap, 0.0f, 0.0f, null);
                    this.mLocalSurface.unlockCanvasAndPost(c);
                } catch (Throwable e) {
                    Log.e("surfaceRender", e);
                }
            }
        }
    }

    private void surfaceRelease() {
        synchronized (this) {
            this.mLocalSurface = null;
            this.mBitmap = null;
            this.mByteBuffer = null;
        }
    }
}
