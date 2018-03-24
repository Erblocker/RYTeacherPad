package net.majorkernelpanic.streaming.video;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Looper;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.exceptions.CameraInUseException;
import net.majorkernelpanic.streaming.exceptions.ConfNotSupportedException;
import net.majorkernelpanic.streaming.exceptions.InvalidSurfaceException;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.hw.EncoderDebugger;
import net.majorkernelpanic.streaming.hw.NV21Convertor;
import net.majorkernelpanic.streaming.rtp.MediaCodecInputStream;

public abstract class VideoStream extends MediaStream {
    protected static final String TAG = "VideoStream";
    protected Camera mCamera;
    protected int mCameraId;
    protected int mCameraImageFormat;
    protected Looper mCameraLooper;
    protected boolean mCameraOpenedManually;
    protected Thread mCameraThread;
    protected int mEncoderColorFormat;
    protected String mEncoderName;
    protected boolean mFlashEnabled;
    protected int mMaxFps;
    protected String mMimeType;
    protected int mOrientation;
    protected boolean mPreviewStarted;
    protected VideoQuality mQuality;
    protected int mRequestedOrientation;
    protected VideoQuality mRequestedQuality;
    protected SharedPreferences mSettings;
    protected Callback mSurfaceHolderCallback;
    protected boolean mSurfaceReady;
    protected SurfaceView mSurfaceView;
    protected boolean mUnlocked;
    protected boolean mUpdated;
    protected int mVideoEncoder;

    public abstract String getSessionDescription() throws IllegalStateException;

    public VideoStream() {
        this(0);
    }

    @SuppressLint({"InlinedApi"})
    public VideoStream(int camera) {
        this.mRequestedQuality = VideoQuality.DEFAULT_VIDEO_QUALITY.clone();
        this.mQuality = this.mRequestedQuality.clone();
        this.mSurfaceHolderCallback = null;
        this.mSurfaceView = null;
        this.mSettings = null;
        this.mCameraId = 0;
        this.mRequestedOrientation = 0;
        this.mOrientation = 0;
        this.mCameraOpenedManually = true;
        this.mFlashEnabled = false;
        this.mSurfaceReady = false;
        this.mUnlocked = false;
        this.mPreviewStarted = false;
        this.mUpdated = false;
        this.mMaxFps = 0;
        setCamera(camera);
    }

    public void setCamera(int camera) {
        CameraInfo cameraInfo = new CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == camera) {
                this.mCameraId = i;
                return;
            }
        }
    }

    public void switchCamera() throws RuntimeException, IOException {
        int i = 1;
        if (Camera.getNumberOfCameras() == 1) {
            throw new IllegalStateException("Phone only has one camera !");
        }
        boolean previewing;
        boolean streaming = this.mStreaming;
        if (this.mCamera == null || !this.mCameraOpenedManually) {
            previewing = false;
        } else {
            previewing = true;
        }
        if (this.mCameraId != 0) {
            i = 0;
        }
        this.mCameraId = i;
        setCamera(this.mCameraId);
        stopPreview();
        this.mFlashEnabled = false;
        if (previewing) {
            startPreview();
        }
        if (streaming) {
            start();
        }
    }

    public int getCamera() {
        return this.mCameraId;
    }

    public synchronized void setSurfaceView(SurfaceView view) {
        this.mSurfaceView = view;
        if (!(this.mSurfaceHolderCallback == null || this.mSurfaceView == null || this.mSurfaceView.getHolder() == null)) {
            this.mSurfaceView.getHolder().removeCallback(this.mSurfaceHolderCallback);
        }
        if (this.mSurfaceView.getHolder() != null) {
            this.mSurfaceHolderCallback = new Callback() {
                public void surfaceDestroyed(SurfaceHolder holder) {
                    VideoStream.this.mSurfaceReady = false;
                    VideoStream.this.stopPreview();
                    Log.d(VideoStream.TAG, "Surface destroyed !");
                }

                public void surfaceCreated(SurfaceHolder holder) {
                    VideoStream.this.mSurfaceReady = true;
                }

                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Log.d(VideoStream.TAG, "Surface Changed !");
                }
            };
            this.mSurfaceView.getHolder().addCallback(this.mSurfaceHolderCallback);
            this.mSurfaceReady = true;
        }
    }

    public synchronized void setFlashState(boolean state) {
        if (this.mCamera != null) {
            if (this.mStreaming && this.mMode == (byte) 1) {
                lockCamera();
            }
            Parameters parameters = this.mCamera.getParameters();
            if (parameters.getFlashMode() == null) {
                throw new RuntimeException("Can't turn the flash on !");
            }
            parameters.setFlashMode(state ? "torch" : "off");
            try {
                this.mCamera.setParameters(parameters);
                this.mFlashEnabled = state;
                if (this.mStreaming && this.mMode == (byte) 1) {
                    unlockCamera();
                }
            } catch (RuntimeException e) {
                this.mFlashEnabled = false;
                throw new RuntimeException("Can't turn the flash on !");
            } catch (Throwable th) {
                if (this.mStreaming && this.mMode == (byte) 1) {
                    unlockCamera();
                }
            }
        } else {
            this.mFlashEnabled = state;
        }
    }

    public synchronized void toggleFlash() {
        setFlashState(!this.mFlashEnabled);
    }

    public boolean getFlashState() {
        return this.mFlashEnabled;
    }

    public void setPreviewOrientation(int orientation) {
        this.mRequestedOrientation = orientation;
        this.mUpdated = false;
    }

    public void setVideoQuality(VideoQuality videoQuality) {
        if (!this.mRequestedQuality.equals(videoQuality)) {
            this.mRequestedQuality = videoQuality.clone();
            this.mUpdated = false;
        }
    }

    public VideoQuality getVideoQuality() {
        return this.mRequestedQuality;
    }

    public void setPreferences(SharedPreferences prefs) {
        this.mSettings = prefs;
    }

    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();
        this.mOrientation = this.mRequestedOrientation;
    }

    public synchronized void start() throws IllegalStateException, IOException {
        if (!this.mPreviewStarted) {
            this.mCameraOpenedManually = false;
        }
        super.start();
        Log.d(TAG, "Stream configuration: FPS: " + this.mQuality.framerate + " Width: " + this.mQuality.resX + " Height: " + this.mQuality.resY);
    }

    public synchronized void stop() {
        if (this.mCamera != null) {
            if (this.mMode == (byte) 2) {
                this.mCamera.setPreviewCallbackWithBuffer(null);
            }
            if (this.mMode == (byte) 5) {
                this.mSurfaceView.removeMediaCodecSurface();
            }
            super.stop();
            if (this.mCameraOpenedManually) {
                try {
                    startPreview();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            } else {
                destroyCamera();
            }
        }
    }

    public synchronized void startPreview() throws CameraInUseException, InvalidSurfaceException, RuntimeException {
        this.mCameraOpenedManually = true;
        if (!this.mPreviewStarted) {
            createCamera();
            updateCamera();
        }
    }

    public synchronized void stopPreview() {
        this.mCameraOpenedManually = false;
        stop();
    }

    protected void encodeWithMediaRecorder() throws IOException, ConfNotSupportedException {
        Log.d(TAG, "Video encoded using the MediaRecorder API");
        createSockets();
        destroyCamera();
        createCamera();
        unlockCamera();
        try {
            FileDescriptor fd;
            InputStream is;
            this.mMediaRecorder = new MediaRecorder();
            this.mMediaRecorder.setCamera(this.mCamera);
            this.mMediaRecorder.setVideoSource(1);
            this.mMediaRecorder.setOutputFormat(1);
            this.mMediaRecorder.setVideoEncoder(this.mVideoEncoder);
            this.mMediaRecorder.setPreviewDisplay(this.mSurfaceView.getHolder().getSurface());
            this.mMediaRecorder.setVideoSize(this.mRequestedQuality.resX, this.mRequestedQuality.resY);
            this.mMediaRecorder.setVideoFrameRate(this.mRequestedQuality.framerate);
            this.mMediaRecorder.setVideoEncodingBitRate((int) (((double) this.mRequestedQuality.bitrate) * 0.8d));
            if (sPipeApi == (byte) 2) {
                fd = this.mParcelWrite.getFileDescriptor();
            } else {
                fd = this.mSender.getFileDescriptor();
            }
            this.mMediaRecorder.setOutputFile(fd);
            this.mMediaRecorder.prepare();
            this.mMediaRecorder.start();
            if (sPipeApi == (byte) 2) {
                is = new AutoCloseInputStream(this.mParcelRead);
            } else {
                is = this.mReceiver.getInputStream();
            }
            try {
                byte[] buffer = new byte[4];
                while (!Thread.interrupted()) {
                    do {
                    } while (is.read() != 109);
                    is.read(buffer, 0, 3);
                    if (buffer[0] == (byte) 100 && buffer[1] == (byte) 97 && buffer[2] == (byte) 116) {
                        break;
                    }
                }
                this.mPacketizer.setInputStream(is);
                this.mPacketizer.start();
                this.mStreaming = true;
            } catch (IOException e) {
                Log.e(TAG, "Couldn't skip mp4 header :/");
                stop();
                throw e;
            }
        } catch (Exception e2) {
            throw new ConfNotSupportedException(e2.getMessage());
        }
    }

    protected void encodeWithMediaCodec() throws RuntimeException, IOException {
        if (this.mMode == (byte) 5) {
            encodeWithMediaCodecMethod2();
        } else {
            encodeWithMediaCodecMethod1();
        }
    }

    @SuppressLint({"NewApi"})
    protected void encodeWithMediaCodecMethod1() throws RuntimeException, IOException {
        Log.d(TAG, "Video encoded using the MediaCodec API with a buffer");
        createCamera();
        updateCamera();
        measureFramerate();
        if (!this.mPreviewStarted) {
            try {
                this.mCamera.startPreview();
                this.mPreviewStarted = true;
            } catch (RuntimeException e) {
                destroyCamera();
                throw e;
            }
        }
        EncoderDebugger debugger = EncoderDebugger.debug(this.mSettings, this.mQuality.resX, this.mQuality.resY);
        final NV21Convertor convertor = debugger.getNV21Convertor();
        this.mMediaCodec = MediaCodec.createByCodecName(debugger.getEncoderName());
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", this.mQuality.resX, this.mQuality.resY);
        mediaFormat.setInteger("bitrate", this.mQuality.bitrate);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_FRAME_RATE, this.mQuality.framerate);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, debugger.getEncoderColorFormat());
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        this.mMediaCodec.configure(mediaFormat, null, null, 1);
        this.mMediaCodec.start();
        PreviewCallback callback = new PreviewCallback() {
            long i = 0;
            ByteBuffer[] inputBuffers;
            long now = (System.nanoTime() / 1000);
            long oldnow = this.now;

            public void onPreviewFrame(byte[] data, Camera camera) {
                this.oldnow = this.now;
                this.now = System.nanoTime() / 1000;
                long j = this.i;
                this.i = 1 + j;
                if (j > 3) {
                    this.i = 0;
                }
                try {
                    int bufferIndex = VideoStream.this.mMediaCodec.dequeueInputBuffer(500000);
                    if (bufferIndex >= 0) {
                        this.inputBuffers[bufferIndex].clear();
                        if (data == null) {
                            Log.e(VideoStream.TAG, "Symptom of the \"Callback buffer was to small\" problem...");
                        } else {
                            convertor.convert(data, this.inputBuffers[bufferIndex]);
                        }
                        VideoStream.this.mMediaCodec.queueInputBuffer(bufferIndex, 0, this.inputBuffers[bufferIndex].position(), this.now, 0);
                    } else {
                        Log.e(VideoStream.TAG, "No buffer available !");
                    }
                    VideoStream.this.mCamera.addCallbackBuffer(data);
                } catch (Throwable th) {
                    VideoStream.this.mCamera.addCallbackBuffer(data);
                }
            }
        };
        for (int i = 0; i < 10; i++) {
            this.mCamera.addCallbackBuffer(new byte[convertor.getBufferSize()]);
        }
        this.mCamera.setPreviewCallbackWithBuffer(callback);
        this.mPacketizer.setInputStream(new MediaCodecInputStream(this.mMediaCodec));
        this.mPacketizer.start();
        this.mStreaming = true;
    }

    @SuppressLint({"InlinedApi", "NewApi"})
    protected void encodeWithMediaCodecMethod2() throws RuntimeException, IOException {
        Log.d(TAG, "Video encoded using the MediaCodec API with a surface");
        createCamera();
        updateCamera();
        measureFramerate();
        this.mMediaCodec = MediaCodec.createByCodecName(EncoderDebugger.debug(this.mSettings, this.mQuality.resX, this.mQuality.resY).getEncoderName());
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", this.mQuality.resX, this.mQuality.resY);
        mediaFormat.setInteger("bitrate", this.mQuality.bitrate);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_FRAME_RATE, this.mQuality.framerate);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, 2130708361);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        this.mMediaCodec.configure(mediaFormat, null, null, 1);
        this.mSurfaceView.addMediaCodecSurface(this.mMediaCodec.createInputSurface());
        this.mMediaCodec.start();
        this.mPacketizer.setInputStream(new MediaCodecInputStream(this.mMediaCodec));
        this.mPacketizer.start();
        this.mStreaming = true;
    }

    private void openCamera() throws RuntimeException {
        final Semaphore lock = new Semaphore(0);
        final RuntimeException[] exception = new RuntimeException[1];
        this.mCameraThread = new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                VideoStream.this.mCameraLooper = Looper.myLooper();
                try {
                    VideoStream.this.mCamera = Camera.open(VideoStream.this.mCameraId);
                } catch (RuntimeException e) {
                    exception[0] = e;
                } finally {
                    lock.release();
                    Looper.loop();
                }
            }
        });
        this.mCameraThread.start();
        lock.acquireUninterruptibly();
        if (exception[0] != null) {
            throw new CameraInUseException(exception[0].getMessage());
        }
    }

    protected synchronized void createCamera() throws RuntimeException {
        if (this.mSurfaceView == null) {
            throw new InvalidSurfaceException("Invalid surface !");
        } else if (this.mSurfaceView.getHolder() == null || !this.mSurfaceReady) {
            throw new InvalidSurfaceException("Invalid surface !");
        } else if (this.mCamera == null) {
            openCamera();
            this.mUpdated = false;
            this.mUnlocked = false;
            this.mCamera.setErrorCallback(new ErrorCallback() {
                public void onError(int error, Camera camera) {
                    if (error == 100) {
                        Log.e(VideoStream.TAG, "Media server died !");
                        VideoStream.this.mCameraOpenedManually = false;
                        VideoStream.this.stop();
                        return;
                    }
                    Log.e(VideoStream.TAG, "Error unknown with the camera: " + error);
                }
            });
            try {
                Parameters parameters = this.mCamera.getParameters();
                if (parameters.getFlashMode() != null) {
                    parameters.setFlashMode(this.mFlashEnabled ? "torch" : "off");
                }
                parameters.setRecordingHint(true);
                this.mCamera.setParameters(parameters);
                this.mCamera.setDisplayOrientation(this.mOrientation);
                if (this.mMode == (byte) 5) {
                    this.mSurfaceView.startGLThread();
                    this.mCamera.setPreviewTexture(this.mSurfaceView.getSurfaceTexture());
                } else {
                    this.mCamera.setPreviewDisplay(this.mSurfaceView.getHolder());
                }
            } catch (IOException e) {
                throw new InvalidSurfaceException("Invalid surface !");
            } catch (RuntimeException e2) {
                destroyCamera();
                throw e2;
            }
        }
    }

    protected synchronized void destroyCamera() {
        if (this.mCamera != null) {
            if (this.mStreaming) {
                super.stop();
            }
            lockCamera();
            this.mCamera.stopPreview();
            try {
                this.mCamera.release();
            } catch (Exception e) {
                String message;
                String str = TAG;
                if (e.getMessage() != null) {
                    message = e.getMessage();
                } else {
                    message = "unknown error";
                }
                Log.e(str, message);
            }
            this.mCamera = null;
            this.mCameraLooper.quit();
            this.mUnlocked = false;
            this.mPreviewStarted = false;
        }
    }

    protected synchronized void updateCamera() throws RuntimeException {
        if (!this.mUpdated) {
            if (this.mPreviewStarted) {
                this.mPreviewStarted = false;
                this.mCamera.stopPreview();
            }
            Parameters parameters = this.mCamera.getParameters();
            this.mQuality = VideoQuality.determineClosestSupportedResolution(parameters, this.mQuality);
            int[] max = VideoQuality.determineMaximumSupportedFramerate(parameters);
            this.mSurfaceView.requestAspectRatio(((double) this.mQuality.resX) / ((double) this.mQuality.resY));
            parameters.setPreviewFormat(this.mCameraImageFormat);
            parameters.setPreviewSize(this.mQuality.resX, this.mQuality.resY);
            parameters.setPreviewFpsRange(max[0], max[1]);
            try {
                this.mCamera.setParameters(parameters);
                this.mCamera.setDisplayOrientation(this.mOrientation);
                this.mCamera.startPreview();
                this.mPreviewStarted = true;
                this.mUpdated = true;
            } catch (RuntimeException e) {
                destroyCamera();
                throw e;
            }
        }
    }

    protected void lockCamera() {
        if (this.mUnlocked) {
            Log.d(TAG, "Locking camera");
            try {
                this.mCamera.reconnect();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            this.mUnlocked = false;
        }
    }

    protected void unlockCamera() {
        if (!this.mUnlocked) {
            Log.d(TAG, "Unlocking camera");
            try {
                this.mCamera.unlock();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            this.mUnlocked = true;
        }
    }

    private void measureFramerate() {
        final Semaphore lock = new Semaphore(0);
        this.mCamera.setPreviewCallback(new PreviewCallback() {
            long count = 0;
            int i = 0;
            long now;
            long oldnow;
            int t = 0;

            public void onPreviewFrame(byte[] data, Camera camera) {
                this.i++;
                this.now = System.nanoTime() / 1000;
                if (this.i > 3) {
                    this.t = (int) (((long) this.t) + (this.now - this.oldnow));
                    this.count++;
                }
                if (this.i > 20) {
                    VideoStream.this.mQuality.framerate = (int) ((1000000 / (((long) this.t) / this.count)) + 1);
                    lock.release();
                }
                this.oldnow = this.now;
            }
        });
        try {
            lock.tryAcquire(2, TimeUnit.SECONDS);
            Log.d(TAG, "Actual framerate: " + this.mQuality.framerate);
            if (this.mSettings != null) {
                Editor editor = this.mSettings.edit();
                editor.putInt("libstreaming-fps" + this.mRequestedQuality.framerate + "," + this.mCameraImageFormat + "," + this.mRequestedQuality.resX + this.mRequestedQuality.resY, this.mQuality.framerate);
                editor.commit();
            }
        } catch (InterruptedException e) {
        }
        this.mCamera.setPreviewCallback(null);
    }
}
