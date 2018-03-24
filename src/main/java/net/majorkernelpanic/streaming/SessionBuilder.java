package net.majorkernelpanic.streaming;

import android.content.Context;
import android.preference.PreferenceManager;
import net.majorkernelpanic.streaming.Session.Callback;
import net.majorkernelpanic.streaming.audio.AACStream;
import net.majorkernelpanic.streaming.audio.AMRNBStream;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.audio.AudioStream;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.H263Stream;
import net.majorkernelpanic.streaming.video.H264Stream;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;

public class SessionBuilder {
    public static final int AUDIO_AAC = 5;
    public static final int AUDIO_AMRNB = 3;
    public static final int AUDIO_NONE = 0;
    public static final String TAG = "SessionBuilder";
    public static final int VIDEO_H263 = 2;
    public static final int VIDEO_H264 = 1;
    public static final int VIDEO_NONE = 0;
    private static volatile SessionBuilder sInstance = null;
    private int mAudioEncoder = 3;
    private AudioQuality mAudioQuality = AudioQuality.DEFAULT_AUDIO_QUALITY;
    private Callback mCallback = null;
    private int mCamera = 0;
    private Context mContext;
    private String mDestination = null;
    private boolean mFlash = false;
    private int mOrientation = 0;
    private String mOrigin = null;
    private SurfaceView mSurfaceView = null;
    private int mTimeToLive = 64;
    private int mVideoEncoder = 2;
    private VideoQuality mVideoQuality = VideoQuality.DEFAULT_VIDEO_QUALITY;

    private SessionBuilder() {
    }

    public static final SessionBuilder getInstance() {
        if (sInstance == null) {
            synchronized (SessionBuilder.class) {
                if (sInstance == null) {
                    sInstance = new SessionBuilder();
                }
            }
        }
        return sInstance;
    }

    public Session build() {
        Session session = new Session();
        session.setOrigin(this.mOrigin);
        session.setDestination(this.mDestination);
        session.setTimeToLive(this.mTimeToLive);
        session.setCallback(this.mCallback);
        switch (this.mAudioEncoder) {
            case 3:
                session.addAudioTrack(new AMRNBStream());
                break;
            case 5:
                AACStream stream = new AACStream();
                session.addAudioTrack(stream);
                if (this.mContext != null) {
                    stream.setPreferences(PreferenceManager.getDefaultSharedPreferences(this.mContext));
                    break;
                }
                break;
        }
        switch (this.mVideoEncoder) {
            case 1:
                H264Stream stream2 = new H264Stream(this.mCamera);
                if (this.mContext != null) {
                    stream2.setPreferences(PreferenceManager.getDefaultSharedPreferences(this.mContext));
                }
                session.addVideoTrack(stream2);
                break;
            case 2:
                session.addVideoTrack(new H263Stream(this.mCamera));
                break;
        }
        if (session.getVideoTrack() != null) {
            VideoStream video = session.getVideoTrack();
            video.setFlashState(this.mFlash);
            video.setVideoQuality(this.mVideoQuality);
            video.setSurfaceView(this.mSurfaceView);
            video.setPreviewOrientation(this.mOrientation);
            video.setDestinationPorts(11006);
        }
        if (session.getAudioTrack() != null) {
            AudioStream audio = session.getAudioTrack();
            audio.setAudioQuality(this.mAudioQuality);
            audio.setDestinationPorts(11004);
        }
        return session;
    }

    public SessionBuilder setContext(Context context) {
        this.mContext = context;
        return this;
    }

    public SessionBuilder setDestination(String destination) {
        this.mDestination = destination;
        return this;
    }

    public SessionBuilder setOrigin(String origin) {
        this.mOrigin = origin;
        return this;
    }

    public SessionBuilder setVideoQuality(VideoQuality quality) {
        this.mVideoQuality = quality.clone();
        return this;
    }

    public SessionBuilder setAudioEncoder(int encoder) {
        this.mAudioEncoder = encoder;
        return this;
    }

    public SessionBuilder setAudioQuality(AudioQuality quality) {
        this.mAudioQuality = quality.clone();
        return this;
    }

    public SessionBuilder setVideoEncoder(int encoder) {
        this.mVideoEncoder = encoder;
        return this;
    }

    public SessionBuilder setFlashEnabled(boolean enabled) {
        this.mFlash = enabled;
        return this;
    }

    public SessionBuilder setCamera(int camera) {
        this.mCamera = camera;
        return this;
    }

    public SessionBuilder setTimeToLive(int ttl) {
        this.mTimeToLive = ttl;
        return this;
    }

    public SessionBuilder setSurfaceView(SurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
        return this;
    }

    public SessionBuilder setPreviewOrientation(int orientation) {
        this.mOrientation = orientation;
        return this;
    }

    public SessionBuilder setCallback(Callback callback) {
        this.mCallback = callback;
        return this;
    }

    public Context getContext() {
        return this.mContext;
    }

    public String getDestination() {
        return this.mDestination;
    }

    public String getOrigin() {
        return this.mOrigin;
    }

    public int getAudioEncoder() {
        return this.mAudioEncoder;
    }

    public int getCamera() {
        return this.mCamera;
    }

    public int getVideoEncoder() {
        return this.mVideoEncoder;
    }

    public VideoQuality getVideoQuality() {
        return this.mVideoQuality;
    }

    public AudioQuality getAudioQuality() {
        return this.mAudioQuality;
    }

    public boolean getFlashState() {
        return this.mFlash;
    }

    public SurfaceView getSurfaceView() {
        return this.mSurfaceView;
    }

    public int getTimeToLive() {
        return this.mTimeToLive;
    }

    public SessionBuilder clone() {
        return new SessionBuilder().setDestination(this.mDestination).setOrigin(this.mOrigin).setSurfaceView(this.mSurfaceView).setPreviewOrientation(this.mOrientation).setVideoQuality(this.mVideoQuality).setVideoEncoder(this.mVideoEncoder).setFlashEnabled(this.mFlash).setCamera(this.mCamera).setTimeToLive(this.mTimeToLive).setAudioEncoder(this.mAudioEncoder).setAudioQuality(this.mAudioQuality).setContext(this.mContext).setCallback(this.mCallback);
    }
}
