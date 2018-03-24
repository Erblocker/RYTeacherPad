package com.eclipsesource.v8.utils;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import java.util.LinkedList;

public class V8Executor extends Thread {
    private Exception exception;
    private volatile boolean forceTerminating;
    private boolean longRunning;
    private String messageHandler;
    private LinkedList<String[]> messageQueue;
    private String result;
    private V8 runtime;
    private final String script;
    private volatile boolean shuttingDown;
    private volatile boolean terminated;

    class ExecutorTermination implements JavaVoidCallback {
        ExecutorTermination() {
        }

        public void invoke(V8Object receiver, V8Array parameters) {
            if (V8Executor.this.forceTerminating) {
                throw new RuntimeException("V8Thread Termination");
            }
        }
    }

    public V8Executor(String script, boolean longRunning, String messageHandler) {
        this.terminated = false;
        this.shuttingDown = false;
        this.forceTerminating = false;
        this.exception = null;
        this.messageQueue = new LinkedList();
        this.script = script;
        this.longRunning = longRunning;
        this.messageHandler = messageHandler;
    }

    public V8Executor(String script) {
        this(script, false, null);
    }

    protected void setup(V8 runtime) {
    }

    public String getResult() {
        return this.result;
    }

    public void postMessage(String... message) {
        synchronized (this) {
            this.messageQueue.add(message);
            notify();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        synchronized (this) {
            this.runtime = V8.createV8Runtime();
            this.runtime.registerJavaMethod(new ExecutorTermination(), "__j2v8__checkThreadTerminate");
            setup(this.runtime);
        }
        if (!this.forceTerminating) {
            Object scriptResult = this.runtime.executeScript("__j2v8__checkThreadTerminate();\n" + this.script, getName(), -1);
            if (scriptResult != null) {
                this.result = scriptResult.toString();
            }
            if (scriptResult instanceof Releasable) {
                ((Releasable) scriptResult).release();
            }
            if (scriptResult instanceof Releasable) {
                ((Releasable) scriptResult).release();
            }
        }
        while (!this.forceTerminating && this.longRunning) {
            synchronized (this) {
                if (this.messageQueue.isEmpty() && !this.shuttingDown) {
                    wait();
                }
                if (!((this.messageQueue.isEmpty() && this.shuttingDown) || this.forceTerminating)) {
                }
            }
        }
        synchronized (this) {
            if (this.runtime.getLocker().hasLock()) {
                this.runtime.release();
                this.runtime = null;
            }
            this.terminated = true;
        }
    }

    public boolean hasException() {
        return this.exception != null;
    }

    public Exception getException() {
        return this.exception;
    }

    public boolean hasTerminated() {
        return this.terminated;
    }

    public void forceTermination() {
        synchronized (this) {
            this.forceTerminating = true;
            this.shuttingDown = true;
            if (this.runtime != null) {
                this.runtime.terminateExecution();
            }
            notify();
        }
    }

    public void shutdown() {
        synchronized (this) {
            this.shuttingDown = true;
            notify();
        }
    }

    public boolean isShuttingDown() {
        return this.shuttingDown;
    }

    public boolean isTerminating() {
        return this.forceTerminating;
    }
}
