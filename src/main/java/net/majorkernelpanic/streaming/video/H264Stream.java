package net.majorkernelpanic.streaming.video;

import android.annotation.SuppressLint;
import android.content.SharedPreferences.Editor;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import io.vov.vitamio.MediaPlayer;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import net.majorkernelpanic.streaming.exceptions.ConfNotSupportedException;
import net.majorkernelpanic.streaming.exceptions.StorageUnavailableException;
import net.majorkernelpanic.streaming.hw.EncoderDebugger;
import net.majorkernelpanic.streaming.mp4.MP4Config;
import net.majorkernelpanic.streaming.rtp.H264Packetizer;

public class H264Stream extends VideoStream {
    public static final String TAG = "H264Stream";
    private MP4Config mConfig;
    private Semaphore mLock;

    public H264Stream() {
        this(1);
    }

    public H264Stream(int cameraId) {
        super(cameraId);
        this.mLock = new Semaphore(0);
        this.mMimeType = "video/avc";
        this.mCameraImageFormat = 17;
        this.mVideoEncoder = 2;
        this.mPacketizer = new H264Packetizer();
    }

    public synchronized String getSessionDescription() throws IllegalStateException {
        if (this.mConfig == null) {
            throw new IllegalStateException("You need to call configure() first !");
        }
        return "m=video " + String.valueOf(getDestinationPorts()[0]) + " RTP/AVP 96\r\n" + "a=rtpmap:96 H264/90000\r\n" + "a=fmtp:96 packetization-mode=1;profile-level-id=" + this.mConfig.getProfileLevel() + ";sprop-parameter-sets=" + this.mConfig.getB64SPS() + "," + this.mConfig.getB64PPS() + ";\r\n";
    }

    public synchronized void start() throws IllegalStateException, IOException {
        if (!this.mStreaming) {
            configure();
            ((H264Packetizer) this.mPacketizer).setStreamParameters(Base64.decode(this.mConfig.getB64PPS(), 2), Base64.decode(this.mConfig.getB64SPS(), 2));
            super.start();
        }
    }

    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();
        this.mMode = this.mRequestedMode;
        this.mQuality = this.mRequestedQuality.clone();
        this.mConfig = testH264();
    }

    private MP4Config testH264() throws IllegalStateException, IOException {
        if (this.mMode != (byte) 1) {
            return testMediaCodecAPI();
        }
        return testMediaRecorderAPI();
    }

    @SuppressLint({"NewApi"})
    private MP4Config testMediaCodecAPI() throws RuntimeException, IOException {
        createCamera();
        updateCamera();
        try {
            int i = this.mQuality.resX;
            EncoderDebugger debugger = EncoderDebugger.debug(this.mSettings, this.mQuality.resX, this.mQuality.resY);
            return new MP4Config(debugger.getB64SPS(), debugger.getB64PPS());
        } catch (Exception e) {
            Log.e(TAG, "Resolution not supported with the MediaCodec API, we fallback on the old streamign method.");
            this.mMode = (byte) 1;
            return testH264();
        }
    }

    private MP4Config testMediaRecorderAPI() throws RuntimeException, IOException {
        String key = "libstreaming-h264-mr-" + this.mRequestedQuality.framerate + "," + this.mRequestedQuality.resX + "," + this.mRequestedQuality.resY;
        if (this.mSettings != null && this.mSettings.contains(key)) {
            String[] s = this.mSettings.getString(key, "").split(",");
            return new MP4Config(s[0], s[1], s[2]);
        } else if (Environment.getExternalStorageState().equals("mounted")) {
            String TESTFILE = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())).append("/spydroid-test.mp4").toString();
            Log.i(TAG, "Testing H264 support... Test file saved at: " + TESTFILE);
            try {
                new File(TESTFILE).createNewFile();
                boolean savedFlashState = this.mFlashEnabled;
                this.mFlashEnabled = false;
                boolean previewStarted = this.mPreviewStarted;
                boolean cameraOpen = this.mCamera != null;
                createCamera();
                if (this.mPreviewStarted) {
                    lockCamera();
                    try {
                        this.mCamera.stopPreview();
                    } catch (Exception e) {
                    }
                    this.mPreviewStarted = false;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                unlockCamera();
                try {
                    this.mMediaRecorder = new MediaRecorder();
                    this.mMediaRecorder.setCamera(this.mCamera);
                    this.mMediaRecorder.setVideoSource(1);
                    this.mMediaRecorder.setOutputFormat(1);
                    this.mMediaRecorder.setVideoEncoder(this.mVideoEncoder);
                    this.mMediaRecorder.setPreviewDisplay(this.mSurfaceView.getHolder().getSurface());
                    this.mMediaRecorder.setVideoSize(this.mRequestedQuality.resX, this.mRequestedQuality.resY);
                    this.mMediaRecorder.setVideoFrameRate(this.mRequestedQuality.framerate);
                    this.mMediaRecorder.setVideoEncodingBitRate((int) (((double) this.mRequestedQuality.bitrate) * 0.8d));
                    this.mMediaRecorder.setOutputFile(TESTFILE);
                    this.mMediaRecorder.setMaxDuration(3000);
                    this.mMediaRecorder.setOnInfoListener(new OnInfoListener() {
                        public void onInfo(MediaRecorder mr, int what, int extra) {
                            Log.d(H264Stream.TAG, "MediaRecorder callback called !");
                            if (what == 800) {
                                Log.d(H264Stream.TAG, "MediaRecorder: MAX_DURATION_REACHED");
                            } else if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                                Log.d(H264Stream.TAG, "MediaRecorder: MAX_FILESIZE_REACHED");
                            } else if (what == 1) {
                                Log.d(H264Stream.TAG, "MediaRecorder: INFO_UNKNOWN");
                            } else {
                                Log.d(H264Stream.TAG, "WTF ?");
                            }
                            H264Stream.this.mLock.release();
                        }
                    });
                    this.mMediaRecorder.prepare();
                    this.mMediaRecorder.start();
                    if (this.mLock.tryAcquire(6, TimeUnit.SECONDS)) {
                        Log.d(TAG, "MediaRecorder callback was called :)");
                        Thread.sleep(400);
                    } else {
                        Log.d(TAG, "MediaRecorder callback was not called after 6 seconds... :(");
                    }
                    try {
                        this.mMediaRecorder.stop();
                    } catch (Exception e2) {
                    }
                    this.mMediaRecorder.release();
                    this.mMediaRecorder = null;
                    lockCamera();
                    if (!cameraOpen) {
                        destroyCamera();
                    }
                    this.mFlashEnabled = savedFlashState;
                    if (previewStarted) {
                        try {
                            startPreview();
                        } catch (Exception e3) {
                        }
                    }
                } catch (IOException e4) {
                    throw new ConfNotSupportedException(e4.getMessage());
                } catch (RuntimeException e5) {
                    throw new ConfNotSupportedException(e5.getMessage());
                } catch (InterruptedException e6) {
                    e6.printStackTrace();
                    try {
                        this.mMediaRecorder.stop();
                    } catch (Exception e7) {
                    }
                    this.mMediaRecorder.release();
                    this.mMediaRecorder = null;
                    lockCamera();
                    if (!cameraOpen) {
                        destroyCamera();
                    }
                    this.mFlashEnabled = savedFlashState;
                    if (previewStarted) {
                        try {
                            startPreview();
                        } catch (Exception e8) {
                        }
                    }
                } catch (Throwable th) {
                    try {
                        this.mMediaRecorder.stop();
                    } catch (Exception e9) {
                    }
                    this.mMediaRecorder.release();
                    this.mMediaRecorder = null;
                    lockCamera();
                    if (!cameraOpen) {
                        destroyCamera();
                    }
                    this.mFlashEnabled = savedFlashState;
                    if (previewStarted) {
                        try {
                            startPreview();
                        } catch (Exception e10) {
                        }
                    }
                }
                MP4Config config = new MP4Config(TESTFILE);
                if (!new File(TESTFILE).delete()) {
                    Log.e(TAG, "Temp file could not be erased");
                }
                Log.i(TAG, "H264 Test succeded...");
                if (this.mSettings == null) {
                    return config;
                }
                Editor editor = this.mSettings.edit();
                editor.putString(key, config.getProfileLevel() + "," + config.getB64SPS() + "," + config.getB64PPS());
                editor.commit();
                return config;
            } catch (IOException e42) {
                throw new StorageUnavailableException(e42.getMessage());
            }
        } else {
            throw new StorageUnavailableException("No external storage or external storage not ready !");
        }
    }
}
