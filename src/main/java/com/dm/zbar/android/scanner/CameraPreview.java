package com.dm.zbar.android.scanner;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import java.io.IOException;
import java.util.List;
import org.apache.http.HttpStatus;

public class CameraPreview extends ViewGroup implements Callback {
    private final String TAG = "CameraPreview";
    AutoFocusCallback mAutoFocusCallback;
    Camera mCamera;
    SurfaceHolder mHolder;
    PreviewCallback mPreviewCallback;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    SurfaceView mSurfaceView;

    public CameraPreview(Context context, PreviewCallback previewCallback, AutoFocusCallback autoFocusCb) {
        super(context);
        this.mPreviewCallback = previewCallback;
        this.mAutoFocusCallback = autoFocusCb;
        this.mSurfaceView = new SurfaceView(context);
        addView(this.mSurfaceView);
        this.mHolder = this.mSurfaceView.getHolder();
        this.mHolder.addCallback(this);
        this.mHolder.setType(3);
    }

    public void setCamera(Camera camera) {
        this.mCamera = camera;
        if (this.mCamera != null) {
            this.mSupportedPreviewSizes = this.mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (this.mSupportedPreviewSizes != null) {
            this.mPreviewSize = getOptimalPreviewSize(this.mSupportedPreviewSizes, width, height);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            View child = getChildAt(0);
            int width = r - l;
            int height = b - t;
            int previewWidth = width;
            int previewHeight = height;
            if (this.mPreviewSize != null) {
                previewWidth = this.mPreviewSize.width;
                previewHeight = this.mPreviewSize.height;
            }
            if (width * previewHeight > height * previewWidth) {
                int scaledChildWidth = (previewWidth * height) / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
                return;
            }
            int scaledChildHeight = (previewHeight * width) / previewWidth;
            child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
        }
    }

    public void hideSurfaceView() {
        this.mSurfaceView.setVisibility(4);
    }

    public void showSurfaceView() {
        this.mSurfaceView.setVisibility(0);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (this.mCamera != null) {
                this.mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e("CameraPreview", "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (this.mCamera != null) {
            this.mCamera.cancelAutoFocus();
            this.mCamera.stopPreview();
        }
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        double targetRatio = ((double) w) / ((double) h);
        if (sizes == null) {
            return null;
        }
        if (sizes.size() > 0) {
            return (Size) sizes.get(0);
        }
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Size size : sizes) {
            if (Math.abs((((double) size.width) / ((double) size.height)) - targetRatio) <= 0.1d && size.height >= HttpStatus.SC_MULTIPLE_CHOICES && ((double) Math.abs(size.height - targetHeight)) < minDiff) {
                optimalSize = size;
                minDiff = (double) Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            if (sizes.size() > 0) {
                optimalSize = (Size) sizes.get(0);
            }
            for (Size size2 : sizes) {
                if (size2.height >= HttpStatus.SC_MULTIPLE_CHOICES && ((double) Math.abs(size2.height - targetHeight)) < minDiff) {
                    optimalSize = size2;
                    minDiff = (double) Math.abs(size2.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (holder.getSurface() != null && this.mCamera != null) {
            Parameters parameters = this.mCamera.getParameters();
            try {
                parameters.setPreviewSize(this.mPreviewSize.width, this.mPreviewSize.height);
                requestLayout();
                this.mCamera.setParameters(parameters);
                this.mCamera.setPreviewCallback(this.mPreviewCallback);
                this.mCamera.startPreview();
                this.mCamera.autoFocus(this.mAutoFocusCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
