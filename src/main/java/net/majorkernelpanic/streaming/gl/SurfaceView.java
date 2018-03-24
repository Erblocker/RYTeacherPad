package net.majorkernelpanic.streaming.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View.MeasureSpec;
import java.util.concurrent.Semaphore;

public class SurfaceView extends android.view.SurfaceView implements Runnable, OnFrameAvailableListener, Callback {
    public static final int ASPECT_RATIO_PREVIEW = 1;
    public static final int ASPECT_RATIO_STRETCH = 0;
    public static final String TAG = "SurfaceView";
    private int mAspectRatioMode;
    private SurfaceManager mCodecSurfaceManager;
    private boolean mFrameAvailable;
    private Handler mHandler;
    private final Semaphore mLock;
    private boolean mRunning;
    private final Object mSyncObject;
    private TextureManager mTextureManager;
    private Thread mThread;
    private ViewAspectRatioMeasurer mVARM;
    private SurfaceManager mViewSurfaceManager;

    public class ViewAspectRatioMeasurer {
        private double aspectRatio;
        private Integer measuredHeight = null;
        private Integer measuredWidth = null;

        public void setAspectRatio(double aspectRatio) {
            this.aspectRatio = aspectRatio;
        }

        public double getAspectRatio() {
            return this.aspectRatio;
        }

        public void measure(int widthMeasureSpec, int heightMeasureSpec) {
            measure(widthMeasureSpec, heightMeasureSpec, this.aspectRatio);
        }

        public void measure(int widthMeasureSpec, int heightMeasureSpec, double aspectRatio) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = widthMode == 0 ? Integer.MAX_VALUE : MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = heightMode == 0 ? Integer.MAX_VALUE : MeasureSpec.getSize(heightMeasureSpec);
            if (heightMode == 1073741824 && widthMode == 1073741824) {
                this.measuredWidth = Integer.valueOf(widthSize);
                this.measuredHeight = Integer.valueOf(heightSize);
            } else if (heightMode == 1073741824) {
                this.measuredWidth = Integer.valueOf((int) Math.min((double) widthSize, ((double) heightSize) * aspectRatio));
                this.measuredHeight = Integer.valueOf((int) (((double) this.measuredWidth.intValue()) / aspectRatio));
            } else if (widthMode == 1073741824) {
                this.measuredHeight = Integer.valueOf((int) Math.min((double) heightSize, ((double) widthSize) / aspectRatio));
                this.measuredWidth = Integer.valueOf((int) (((double) this.measuredHeight.intValue()) * aspectRatio));
            } else if (((double) widthSize) > ((double) heightSize) * aspectRatio) {
                this.measuredHeight = Integer.valueOf(heightSize);
                this.measuredWidth = Integer.valueOf((int) (((double) this.measuredHeight.intValue()) * aspectRatio));
            } else {
                this.measuredWidth = Integer.valueOf(widthSize);
                this.measuredHeight = Integer.valueOf((int) (((double) this.measuredWidth.intValue()) / aspectRatio));
            }
        }

        public int getMeasuredWidth() {
            if (this.measuredWidth != null) {
                return this.measuredWidth.intValue();
            }
            throw new IllegalStateException("You need to run measure() before trying to get measured dimensions");
        }

        public int getMeasuredHeight() {
            if (this.measuredHeight != null) {
                return this.measuredHeight.intValue();
            }
            throw new IllegalStateException("You need to run measure() before trying to get measured dimensions");
        }
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mThread = null;
        this.mHandler = null;
        this.mFrameAvailable = false;
        this.mRunning = true;
        this.mAspectRatioMode = 0;
        this.mViewSurfaceManager = null;
        this.mCodecSurfaceManager = null;
        this.mTextureManager = null;
        this.mLock = new Semaphore(0);
        this.mSyncObject = new Object();
        this.mVARM = new ViewAspectRatioMeasurer();
        this.mHandler = new Handler();
        getHolder().addCallback(this);
    }

    public void setAspectRatioMode(int mode) {
        this.mAspectRatioMode = mode;
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mTextureManager.getSurfaceTexture();
    }

    public void addMediaCodecSurface(Surface surface) {
        synchronized (this.mSyncObject) {
            this.mCodecSurfaceManager = new SurfaceManager(surface, this.mViewSurfaceManager);
        }
    }

    public void removeMediaCodecSurface() {
        synchronized (this.mSyncObject) {
            if (this.mCodecSurfaceManager != null) {
                this.mCodecSurfaceManager.release();
                this.mCodecSurfaceManager = null;
            }
        }
    }

    public void startGLThread() {
        Log.d(TAG, "Thread started.");
        if (this.mTextureManager == null) {
            this.mTextureManager = new TextureManager();
        }
        if (this.mTextureManager.getSurfaceTexture() == null) {
            this.mThread = new Thread(this);
            this.mRunning = true;
            this.mThread.start();
            this.mLock.acquireUninterruptibly();
        }
    }

    public void run() {
        this.mViewSurfaceManager = new SurfaceManager(getHolder().getSurface());
        this.mViewSurfaceManager.makeCurrent();
        this.mTextureManager.createTexture().setOnFrameAvailableListener(this);
        this.mLock.release();
        long ts = 0;
        while (this.mRunning) {
            try {
                synchronized (this.mSyncObject) {
                    this.mSyncObject.wait(2500);
                    if (this.mFrameAvailable) {
                        this.mFrameAvailable = false;
                        this.mViewSurfaceManager.makeCurrent();
                        this.mTextureManager.updateFrame();
                        this.mTextureManager.drawFrame();
                        this.mViewSurfaceManager.swapBuffer();
                        if (this.mCodecSurfaceManager != null) {
                            this.mCodecSurfaceManager.makeCurrent();
                            this.mTextureManager.drawFrame();
                            long oldts = ts;
                            ts = this.mTextureManager.getSurfaceTexture().getTimestamp();
                            this.mCodecSurfaceManager.setPresentationTime(ts);
                            this.mCodecSurfaceManager.swapBuffer();
                        }
                    } else {
                        Log.e(TAG, "No frame received !");
                    }
                }
            } catch (InterruptedException e) {
            } finally {
                this.mViewSurfaceManager.release();
                this.mTextureManager.release();
            }
        }
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this.mSyncObject) {
            this.mFrameAvailable = true;
            this.mSyncObject.notifyAll();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (this.mThread != null) {
            this.mThread.interrupt();
        }
        this.mRunning = false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mVARM.getAspectRatio() <= 0.0d || this.mAspectRatioMode != 1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        this.mVARM.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(this.mVARM.getMeasuredWidth(), this.mVARM.getMeasuredHeight());
    }

    public void requestAspectRatio(double aspectRatio) {
        if (this.mVARM.getAspectRatio() != aspectRatio) {
            this.mVARM.setAspectRatio(aspectRatio);
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (SurfaceView.this.mAspectRatioMode == 1) {
                        SurfaceView.this.requestLayout();
                    }
                }
            });
        }
    }
}
