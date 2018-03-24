package eu.janmuller.android.simplecropimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import java.io.FileDescriptor;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

public class BitmapManager {
    private static final String TAG = "BitmapManager";
    private static BitmapManager sManager = null;
    private final WeakHashMap<Thread, ThreadStatus> mThreadStatus = new WeakHashMap();

    private enum State {
        CANCEL,
        ALLOW
    }

    public static class ThreadSet implements Iterable<Thread> {
        private final WeakHashMap<Thread, Object> mWeakCollection = new WeakHashMap();

        public void add(Thread t) {
            this.mWeakCollection.put(t, null);
        }

        public void remove(Thread t) {
            this.mWeakCollection.remove(t);
        }

        public Iterator<Thread> iterator() {
            return this.mWeakCollection.keySet().iterator();
        }
    }

    private static class ThreadStatus {
        public Options mOptions;
        public State mState;

        private ThreadStatus() {
            this.mState = State.ALLOW;
        }

        public String toString() {
            String s;
            if (this.mState == State.CANCEL) {
                s = "Cancel";
            } else if (this.mState == State.ALLOW) {
                s = "Allow";
            } else {
                s = "?";
            }
            return "thread state = " + s + ", options = " + this.mOptions;
        }
    }

    private BitmapManager() {
    }

    private synchronized ThreadStatus getOrCreateThreadStatus(Thread t) {
        ThreadStatus status;
        status = (ThreadStatus) this.mThreadStatus.get(t);
        if (status == null) {
            status = new ThreadStatus();
            this.mThreadStatus.put(t, status);
        }
        return status;
    }

    private synchronized void setDecodingOptions(Thread t, Options options) {
        getOrCreateThreadStatus(t).mOptions = options;
    }

    synchronized Options getDecodingOptions(Thread t) {
        ThreadStatus status;
        status = (ThreadStatus) this.mThreadStatus.get(t);
        return status != null ? status.mOptions : null;
    }

    synchronized void removeDecodingOptions(Thread t) {
        ((ThreadStatus) this.mThreadStatus.get(t)).mOptions = null;
    }

    public synchronized void allowThreadDecoding(ThreadSet threads) {
        Iterator it = threads.iterator();
        while (it.hasNext()) {
            allowThreadDecoding((Thread) it.next());
        }
    }

    public synchronized void cancelThreadDecoding(ThreadSet threads) {
        Iterator it = threads.iterator();
        while (it.hasNext()) {
            cancelThreadDecoding((Thread) it.next());
        }
    }

    public synchronized boolean canThreadDecoding(Thread t) {
        boolean z = true;
        synchronized (this) {
            ThreadStatus status = (ThreadStatus) this.mThreadStatus.get(t);
            if (status != null) {
                if (status.mState == State.CANCEL) {
                    z = false;
                }
            }
        }
        return z;
    }

    public synchronized void allowThreadDecoding(Thread t) {
        getOrCreateThreadStatus(t).mState = State.ALLOW;
    }

    public synchronized void cancelThreadDecoding(Thread t) {
        ThreadStatus status = getOrCreateThreadStatus(t);
        status.mState = State.CANCEL;
        if (status.mOptions != null) {
            status.mOptions.requestCancelDecode();
        }
        notifyAll();
    }

    public synchronized void dump() {
        for (Entry<Thread, ThreadStatus> entry : this.mThreadStatus.entrySet()) {
            Log.v(TAG, "[Dump] Thread " + entry.getKey() + " (" + ((Thread) entry.getKey()).getId() + ")'s status is " + entry.getValue());
        }
    }

    public static synchronized BitmapManager instance() {
        BitmapManager bitmapManager;
        synchronized (BitmapManager.class) {
            if (sManager == null) {
                sManager = new BitmapManager();
            }
            bitmapManager = sManager;
        }
        return bitmapManager;
    }

    public Bitmap decodeFileDescriptor(FileDescriptor fd, Options options) {
        if (options.mCancel) {
            return null;
        }
        Thread thread = Thread.currentThread();
        if (!canThreadDecoding(thread)) {
            return null;
        }
        setDecodingOptions(thread, options);
        Bitmap b = BitmapFactory.decodeFileDescriptor(fd, null, options);
        removeDecodingOptions(thread);
        return b;
    }
}
