package net.majorkernelpanic.streaming.gl;

import android.annotation.SuppressLint;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.view.Surface;

@SuppressLint({"NewApi"})
public class SurfaceManager {
    private static final int EGL_RECORDABLE_ANDROID = 12610;
    public static final String TAG = "TextureManager";
    private EGLContext mEGLContext = null;
    private EGLDisplay mEGLDisplay = null;
    private EGLContext mEGLSharedContext = null;
    private EGLSurface mEGLSurface = null;
    private Surface mSurface;

    public SurfaceManager(Surface surface, SurfaceManager manager) {
        this.mSurface = surface;
        this.mEGLSharedContext = manager.mEGLContext;
        eglSetup();
    }

    public SurfaceManager(Surface surface) {
        this.mSurface = surface;
        eglSetup();
    }

    public void makeCurrent() {
        if (!EGL14.eglMakeCurrent(this.mEGLDisplay, this.mEGLSurface, this.mEGLSurface, this.mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public void swapBuffer() {
        EGL14.eglSwapBuffers(this.mEGLDisplay, this.mEGLSurface);
    }

    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(this.mEGLDisplay, this.mEGLSurface, nsecs);
        checkEglError("eglPresentationTimeANDROID");
    }

    private void eglSetup() {
        this.mEGLDisplay = EGL14.eglGetDisplay(0);
        if (this.mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (EGL14.eglInitialize(this.mEGLDisplay, version, 0, version, 1)) {
            int[] attribList;
            if (this.mEGLSharedContext == null) {
                attribList = new int[]{12324, 8, 12323, 8, 12322, 8, 12352, 4, 12344};
            } else {
                attribList = new int[]{12324, 8, 12323, 8, 12322, 8, 12352, 4, EGL_RECORDABLE_ANDROID, 1, 12344};
            }
            EGLConfig[] configs = new EGLConfig[1];
            int i = 0;
            EGL14.eglChooseConfig(this.mEGLDisplay, attribList, 0, configs, i, configs.length, new int[1], 0);
            checkEglError("eglCreateContext RGB888+recordable ES2");
            int[] attrib_list = new int[]{12440, 2, 12344};
            if (this.mEGLSharedContext == null) {
                this.mEGLContext = EGL14.eglCreateContext(this.mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT, attrib_list, 0);
            } else {
                this.mEGLContext = EGL14.eglCreateContext(this.mEGLDisplay, configs[0], this.mEGLSharedContext, attrib_list, 0);
            }
            checkEglError("eglCreateContext");
            this.mEGLSurface = EGL14.eglCreateWindowSurface(this.mEGLDisplay, configs[0], this.mSurface, new int[]{12344}, 0);
            checkEglError("eglCreateWindowSurface");
            GLES20.glDisable(2929);
            GLES20.glDisable(2884);
            return;
        }
        throw new RuntimeException("unable to initialize EGL14");
    }

    public void release() {
        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(this.mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
            EGL14.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.mEGLDisplay);
        }
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLContext = EGL14.EGL_NO_CONTEXT;
        this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        this.mSurface.release();
    }

    private void checkEglError(String msg) {
        int error = EGL14.eglGetError();
        if (error != 12288) {
            throw new RuntimeException(new StringBuilder(String.valueOf(msg)).append(": EGL error: 0x").append(Integer.toHexString(error)).toString());
        }
    }
}
