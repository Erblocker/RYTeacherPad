package org.greenrobot.eventbus;

import android.os.Looper;

public interface MainThreadSupport {

    public static class AndroidHandlerMainThreadSupport implements MainThreadSupport {
        private final Looper looper;

        public AndroidHandlerMainThreadSupport(Looper looper) {
            this.looper = looper;
        }

        public boolean isMainThread() {
            return this.looper == Looper.myLooper();
        }

        public Poster createPoster(EventBus eventBus) {
            return new HandlerPoster(eventBus, this.looper, 10);
        }
    }

    Poster createPoster(EventBus eventBus);

    boolean isMainThread();
}
