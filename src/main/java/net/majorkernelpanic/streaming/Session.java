package net.majorkernelpanic.streaming;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.audio.AudioStream;
import net.majorkernelpanic.streaming.exceptions.CameraInUseException;
import net.majorkernelpanic.streaming.exceptions.ConfNotSupportedException;
import net.majorkernelpanic.streaming.exceptions.InvalidSurfaceException;
import net.majorkernelpanic.streaming.exceptions.StorageUnavailableException;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;

public class Session {
    public static final int ERROR_CAMERA_ALREADY_IN_USE = 0;
    public static final int ERROR_CAMERA_HAS_NO_FLASH = 3;
    public static final int ERROR_CONFIGURATION_NOT_SUPPORTED = 1;
    public static final int ERROR_INVALID_SURFACE = 4;
    public static final int ERROR_OTHER = 6;
    public static final int ERROR_STORAGE_NOT_READY = 2;
    public static final int ERROR_UNKNOWN_HOST = 5;
    public static final int STREAM_AUDIO = 0;
    public static final int STREAM_VIDEO = 1;
    public static final String TAG = "Session";
    private AudioStream mAudioStream = null;
    private Callback mCallback;
    private String mDestination;
    private Handler mHandler;
    private Handler mMainHandler;
    private String mOrigin;
    private int mTimeToLive = 64;
    private long mTimestamp;
    private Runnable mUpdateBitrate = new Runnable() {
        public void run() {
            if (Session.this.isStreaming()) {
                Session.this.postBitRate(Session.this.getBitrate());
                Session.this.mHandler.postDelayed(Session.this.mUpdateBitrate, 500);
                return;
            }
            Session.this.postBitRate(0);
        }
    };
    private VideoStream mVideoStream = null;

    public interface Callback {
        void onBitrateUpdate(long j);

        void onPreviewStarted();

        void onSessionConfigured();

        void onSessionError(int i, int i2, Exception exception);

        void onSessionStarted();

        void onSessionStopped();
    }

    public Session() {
        long uptime = System.currentTimeMillis();
        HandlerThread thread = new HandlerThread("net.majorkernelpanic.streaming.Session");
        thread.start();
        this.mHandler = new Handler(thread.getLooper());
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mTimestamp = ((uptime / 1000) << 32) & (((uptime - ((uptime / 1000) * 1000)) >> 32) / 1000);
        this.mOrigin = "127.0.0.1";
    }

    void addAudioTrack(AudioStream track) {
        removeAudioTrack();
        this.mAudioStream = track;
    }

    void addVideoTrack(VideoStream track) {
        removeVideoTrack();
        this.mVideoStream = track;
    }

    void removeAudioTrack() {
        if (this.mAudioStream != null) {
            this.mAudioStream.stop();
            this.mAudioStream = null;
        }
    }

    void removeVideoTrack() {
        if (this.mVideoStream != null) {
            this.mVideoStream.stopPreview();
            this.mVideoStream = null;
        }
    }

    public AudioStream getAudioTrack() {
        return this.mAudioStream;
    }

    public VideoStream getVideoTrack() {
        return this.mVideoStream;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setOrigin(String origin) {
        this.mOrigin = origin;
    }

    public void setDestination(String destination) {
        this.mDestination = destination;
    }

    public void setTimeToLive(int ttl) {
        this.mTimeToLive = ttl;
    }

    public void setVideoQuality(VideoQuality quality) {
        if (this.mVideoStream != null) {
            this.mVideoStream.setVideoQuality(quality);
        }
    }

    public void setSurfaceView(final SurfaceView view) {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mVideoStream != null) {
                    Session.this.mVideoStream.setSurfaceView(view);
                }
            }
        });
    }

    public void setPreviewOrientation(int orientation) {
        if (this.mVideoStream != null) {
            this.mVideoStream.setPreviewOrientation(orientation);
        }
    }

    public void setAudioQuality(AudioQuality quality) {
        if (this.mAudioStream != null) {
            this.mAudioStream.setAudioQuality(quality);
        }
    }

    public Callback getCallback() {
        return this.mCallback;
    }

    public String getSessionDescription() {
        StringBuilder sessionDescription = new StringBuilder();
        if (this.mDestination == null) {
            throw new IllegalStateException("setDestination() has not been called !");
        }
        sessionDescription.append("v=0\r\n");
        sessionDescription.append("o=- " + this.mTimestamp + " " + this.mTimestamp + " IN IP4 " + this.mOrigin + "\r\n");
        sessionDescription.append("s=Unnamed\r\n");
        sessionDescription.append("i=N/A\r\n");
        sessionDescription.append("c=IN IP4 " + this.mDestination + "\r\n");
        sessionDescription.append("t=0 0\r\n");
        sessionDescription.append("a=recvonly\r\n");
        if (this.mAudioStream != null) {
            sessionDescription.append(this.mAudioStream.getSessionDescription());
            sessionDescription.append("a=control:trackID=0\r\n");
        }
        if (this.mVideoStream != null) {
            sessionDescription.append(this.mVideoStream.getSessionDescription());
            sessionDescription.append("a=control:trackID=1\r\n");
        }
        return sessionDescription.toString();
    }

    public String getDestination() {
        return this.mDestination;
    }

    public long getBitrate() {
        long sum = 0;
        if (this.mAudioStream != null) {
            sum = 0 + this.mAudioStream.getBitrate();
        }
        if (this.mVideoStream != null) {
            return sum + this.mVideoStream.getBitrate();
        }
        return sum;
    }

    public boolean isStreaming() {
        if ((this.mAudioStream == null || !this.mAudioStream.isStreaming()) && (this.mVideoStream == null || !this.mVideoStream.isStreaming())) {
            return false;
        }
        return true;
    }

    public void configure() {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    Session.this.syncConfigure();
                } catch (Exception e) {
                }
            }
        });
    }

    public void syncConfigure() throws CameraInUseException, StorageUnavailableException, ConfNotSupportedException, InvalidSurfaceException, RuntimeException, IOException {
        int id = 0;
        while (id < 2) {
            Stream stream = id == 0 ? this.mAudioStream : this.mVideoStream;
            if (!(stream == null || stream.isStreaming())) {
                try {
                    stream.configure();
                } catch (CameraInUseException e) {
                    postError(0, id, e);
                    throw e;
                } catch (StorageUnavailableException e2) {
                    postError(2, id, e2);
                    throw e2;
                } catch (ConfNotSupportedException e3) {
                    postError(1, id, e3);
                    throw e3;
                } catch (InvalidSurfaceException e4) {
                    postError(4, id, e4);
                    throw e4;
                } catch (IOException e5) {
                    postError(6, id, e5);
                    throw e5;
                } catch (RuntimeException e6) {
                    postError(6, id, e6);
                    throw e6;
                }
            }
            id++;
        }
        postSessionConfigured();
    }

    public void start() {
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    Session.this.syncStart();
                } catch (Exception e) {
                }
            }
        });
    }

    public void syncStart(int id) throws CameraInUseException, StorageUnavailableException, ConfNotSupportedException, InvalidSurfaceException, UnknownHostException, IOException {
        Stream stream = id == 0 ? this.mAudioStream : this.mVideoStream;
        if (stream != null && !stream.isStreaming()) {
            try {
                InetAddress destination = InetAddress.getByName(this.mDestination);
                stream.setTimeToLive(this.mTimeToLive);
                stream.setDestinationAddress(destination);
                stream.start();
                if (getTrack(1 - id) == null || getTrack(1 - id).isStreaming()) {
                    postSessionStarted();
                }
                if (getTrack(1 - id) == null || !getTrack(1 - id).isStreaming()) {
                    this.mHandler.post(this.mUpdateBitrate);
                }
            } catch (UnknownHostException e) {
                postError(5, id, e);
                throw e;
            } catch (CameraInUseException e2) {
                postError(0, id, e2);
                throw e2;
            } catch (StorageUnavailableException e3) {
                postError(2, id, e3);
                throw e3;
            } catch (ConfNotSupportedException e4) {
                postError(1, id, e4);
                throw e4;
            } catch (InvalidSurfaceException e5) {
                postError(4, id, e5);
                throw e5;
            } catch (IOException e6) {
                postError(6, id, e6);
                throw e6;
            } catch (RuntimeException e7) {
                postError(6, id, e7);
                throw e7;
            }
        }
    }

    public void syncStart() throws CameraInUseException, StorageUnavailableException, ConfNotSupportedException, InvalidSurfaceException, UnknownHostException, IOException {
        syncStart(1);
        try {
            syncStart(0);
        } catch (RuntimeException e) {
            syncStop(1);
            throw e;
        } catch (IOException e2) {
            syncStop(1);
            throw e2;
        }
    }

    public void stop() {
        this.mHandler.post(new Runnable() {
            public void run() {
                Session.this.syncStop();
            }
        });
    }

    private void syncStop(int id) {
        Stream stream = id == 0 ? this.mAudioStream : this.mVideoStream;
        if (stream != null) {
            stream.stop();
        }
    }

    public void syncStop() {
        syncStop(0);
        syncStop(1);
        postSessionStopped();
    }

    public void startPreview() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mVideoStream != null) {
                    try {
                        Session.this.mVideoStream.startPreview();
                        Session.this.postPreviewStarted();
                        Session.this.mVideoStream.configure();
                    } catch (CameraInUseException e) {
                        Session.this.postError(0, 1, e);
                    } catch (ConfNotSupportedException e2) {
                        Session.this.postError(1, 1, e2);
                    } catch (InvalidSurfaceException e3) {
                        Session.this.postError(4, 1, e3);
                    } catch (RuntimeException e4) {
                        Session.this.postError(6, 1, e4);
                    } catch (StorageUnavailableException e5) {
                        Session.this.postError(2, 1, e5);
                    } catch (IOException e6) {
                        Session.this.postError(6, 1, e6);
                    }
                }
            }
        });
    }

    public void stopPreview() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mVideoStream != null) {
                    Session.this.mVideoStream.stopPreview();
                }
            }
        });
    }

    public void switchCamera() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mVideoStream != null) {
                    try {
                        Session.this.mVideoStream.switchCamera();
                        Session.this.postPreviewStarted();
                    } catch (CameraInUseException e) {
                        Session.this.postError(0, 1, e);
                    } catch (ConfNotSupportedException e2) {
                        Session.this.postError(1, 1, e2);
                    } catch (InvalidSurfaceException e3) {
                        Session.this.postError(4, 1, e3);
                    } catch (IOException e4) {
                        Session.this.postError(6, 1, e4);
                    } catch (RuntimeException e5) {
                        Session.this.postError(6, 1, e5);
                    }
                }
            }
        });
    }

    public int getCamera() {
        return this.mVideoStream != null ? this.mVideoStream.getCamera() : 0;
    }

    public void toggleFlash() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mVideoStream != null) {
                    try {
                        Session.this.mVideoStream.toggleFlash();
                    } catch (RuntimeException e) {
                        Session.this.postError(3, 1, e);
                    }
                }
            }
        });
    }

    public void release() {
        removeAudioTrack();
        removeVideoTrack();
        this.mHandler.getLooper().quit();
    }

    private void postPreviewStarted() {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mCallback != null) {
                    Session.this.mCallback.onPreviewStarted();
                }
            }
        });
    }

    private void postSessionConfigured() {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mCallback != null) {
                    Session.this.mCallback.onSessionConfigured();
                }
            }
        });
    }

    private void postSessionStarted() {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mCallback != null) {
                    Session.this.mCallback.onSessionStarted();
                }
            }
        });
    }

    private void postSessionStopped() {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mCallback != null) {
                    Session.this.mCallback.onSessionStopped();
                }
            }
        });
    }

    private void postError(final int reason, final int streamType, final Exception e) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mCallback != null) {
                    Session.this.mCallback.onSessionError(reason, streamType, e);
                }
            }
        });
    }

    private void postBitRate(final long bitrate) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (Session.this.mCallback != null) {
                    Session.this.mCallback.onBitrateUpdate(bitrate);
                }
            }
        });
    }

    public boolean trackExists(int id) {
        if (id == 0) {
            if (this.mAudioStream != null) {
                return true;
            }
            return false;
        } else if (this.mVideoStream == null) {
            return false;
        } else {
            return true;
        }
    }

    public Stream getTrack(int id) {
        if (id == 0) {
            return this.mAudioStream;
        }
        return this.mVideoStream;
    }
}
