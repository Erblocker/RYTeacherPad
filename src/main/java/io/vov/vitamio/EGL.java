package io.vov.vitamio;

import android.util.Log;
import android.view.Surface;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

public class EGL {
    private EGLConfigChooser mEGLConfigChooser = new SimpleEGLConfigChooser();
    private EGLContextFactory mEGLContextFactory = new EGLContextFactory();
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory = new EGLWindowSurfaceFactory();
    private EGL10 mEgl;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;

    private abstract class EGLConfigChooser {
        protected int[] mConfigSpec;

        abstract EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr);

        public EGLConfigChooser(int[] configSpec) {
            this.mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (egl.eglChooseConfig(display, this.mConfigSpec, null, 0, num_config)) {
                int numConfigs = num_config[0];
                if (numConfigs <= 0) {
                    throw new IllegalArgumentException("No configs match configSpec");
                }
                EGLConfig[] configs = new EGLConfig[numConfigs];
                if (egl.eglChooseConfig(display, this.mConfigSpec, configs, numConfigs, num_config)) {
                    EGLConfig config = chooseConfig(egl, display, configs);
                    if (config != null) {
                        return config;
                    }
                    throw new IllegalArgumentException("No config chosen");
                }
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        private int[] filterConfigSpec(int[] configSpec) {
            int len = configSpec.length;
            int[] newConfigSpec = new int[(len + 2)];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = 12352;
            newConfigSpec[len] = 4;
            newConfigSpec[len + 1] = 12344;
            return newConfigSpec;
        }
    }

    private class EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION;

        private EGLContextFactory() {
            this.EGL_CONTEXT_CLIENT_VERSION = 12440;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, new int[]{this.EGL_CONTEXT_CLIENT_VERSION, 2, 12344});
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                throw new RuntimeException("eglDestroyContext failed: ");
            }
        }
    }

    private static class EGLWindowSurfaceFactory {
        private EGLWindowSurfaceFactory() {
        }

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
        }

        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    private class ComponentSizeChooser extends EGLConfigChooser {
        protected int mAlphaSize;
        protected int mBlueSize;
        protected int mDepthSize;
        protected int mGreenSize;
        protected int mRedSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];

        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            super(new int[]{12324, redSize, 12323, greenSize, 12322, blueSize, 12321, alphaSize, 12325, depthSize, 12326, stencilSize, 12344});
            this.mRedSize = redSize;
            this.mGreenSize = greenSize;
            this.mBlueSize = blueSize;
            this.mAlphaSize = alphaSize;
            this.mDepthSize = depthSize;
            this.mStencilSize = stencilSize;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config, 12325, 0);
                int s = findConfigAttrib(egl, display, config, 12326, 0);
                if (d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = findConfigAttrib(egl, display, config, 12324, 0);
                    int g = findConfigAttrib(egl, display, config, 12323, 0);
                    int b = findConfigAttrib(egl, display, config, 12322, 0);
                    int a = findConfigAttrib(egl, display, config, 12321, 0);
                    if (r == this.mRedSize && g == this.mGreenSize && b == this.mBlueSize && a == this.mAlphaSize) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
                return this.mValue[0];
            }
            return defaultValue;
        }
    }

    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser() {
            super(5, 6, 5, 0, 0, 0);
        }
    }

    public boolean initialize(Surface surface) {
        start();
        return createSurface(surface) != null;
    }

    public void release() {
        destroySurface();
        finish();
    }

    public void start() {
        this.mEgl = (EGL10) EGLContext.getEGL();
        this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }
        if (this.mEgl.eglInitialize(this.mEglDisplay, new int[2])) {
            this.mEglConfig = this.mEGLConfigChooser.chooseConfig(this.mEgl, this.mEglDisplay);
            this.mEglContext = this.mEGLContextFactory.createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
            if (this.mEglContext == null || this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                this.mEglContext = null;
                throwEglException("createContext");
            }
            this.mEglSurface = null;
            return;
        }
        throw new RuntimeException("eglInitialize failed");
    }

    public GL createSurface(Surface surface) {
        if (this.mEgl == null) {
            throw new RuntimeException("egl not initialized");
        } else if (this.mEglDisplay == null) {
            throw new RuntimeException("eglDisplay not initialized");
        } else if (this.mEglConfig == null) {
            throw new RuntimeException("mEglConfig not initialized");
        } else {
            if (!(this.mEglSurface == null || this.mEglSurface == EGL10.EGL_NO_SURFACE)) {
                this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                this.mEGLWindowSurfaceFactory.destroySurface(this.mEgl, this.mEglDisplay, this.mEglSurface);
            }
            this.mEglSurface = this.mEGLWindowSurfaceFactory.createWindowSurface(this.mEgl, this.mEglDisplay, this.mEglConfig, surface);
            if (this.mEglSurface == null || this.mEglSurface == EGL10.EGL_NO_SURFACE) {
                int error = this.mEgl.eglGetError();
                if (error == 12299) {
                    Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                    return null;
                }
                throwEglException("createWindowSurface", error);
            }
            if (!this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
                throwEglException("eglMakeCurrent");
            }
            return this.mEglContext.getGL();
        }
    }

    public boolean swap() {
        if (!this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface)) {
            int error = this.mEgl.eglGetError();
            switch (error) {
                case 12299:
                    Log.e("EglHelper", "eglSwapBuffers returned EGL_BAD_NATIVE_WINDOW. tid=" + Thread.currentThread().getId());
                    break;
                case 12301:
                    Log.e("EglHelper", "eglSwapBuffers returned EGL_BAD_SURFACE. tid=" + Thread.currentThread().getId());
                    return false;
                case 12302:
                    return false;
                default:
                    throwEglException("eglSwapBuffers", error);
                    break;
            }
        }
        return true;
    }

    public void destroySurface() {
        if (this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE) {
            this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            this.mEGLWindowSurfaceFactory.destroySurface(this.mEgl, this.mEglDisplay, this.mEglSurface);
            this.mEglSurface = null;
        }
    }

    public void finish() {
        if (this.mEglContext != null) {
            this.mEGLContextFactory.destroyContext(this.mEgl, this.mEglDisplay, this.mEglContext);
            this.mEglContext = null;
        }
        if (this.mEglDisplay != null) {
            this.mEgl.eglTerminate(this.mEglDisplay);
            this.mEglDisplay = null;
        }
    }

    private void throwEglException(String function) {
        throwEglException(function, this.mEgl.eglGetError());
    }

    private void throwEglException(String function, int error) {
        String message = String.format("%s failed: %x", new Object[]{function, Integer.valueOf(error)});
        Log.e("EglHelper", "throwEglException tid=" + Thread.currentThread().getId() + " " + message);
        throw new RuntimeException(message);
    }
}
