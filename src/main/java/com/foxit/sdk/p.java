package com.foxit.sdk;

import android.os.Process;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: TaskServer */
class p implements Runnable {
    private s a;
    private int b = 1;
    private ExecutorService c = Executors.newSingleThreadExecutor();
    private Task d;

    protected p(s sVar) {
        this.a = sVar;
    }

    protected int a() {
        return this.b;
    }

    protected void a(Task task) {
        this.d = task;
        this.d.prepare();
        this.c.execute(this);
    }

    public void run() {
        Task task = this.d;
        Process.setThreadPriority(task != null ? task.getThreadPriority() : 10);
        if (task != null) {
            if (!task.isCanceled() && task.getStatus() == 1) {
                task.execute();
            }
            this.a.a(task, false);
        }
    }
}
