package com.foxit.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: TaskServer */
class s {
    private p a = new p(this);
    private Handler b;
    private Handler c;
    private ArrayList<ArrayList<Task>> d = new ArrayList();
    private ArrayList<Task> e;
    private boolean f = true;
    private Task g = null;
    private Task h = null;
    private PDFViewCtrl i;

    protected s(PDFViewCtrl pDFViewCtrl) {
        this.i = pDFViewCtrl;
        for (int i = 0; i < 7; i++) {
            this.d.add(new ArrayList(8));
        }
        this.e = new ArrayList(8);
        this.b = new Handler(this, Looper.getMainLooper()) {
            final /* synthetic */ s a;

            public void handleMessage(Message message) {
                super.handleMessage(message);
                switch (message.what) {
                    case 1:
                    case 3:
                        ((Task) message.obj).finish();
                        this.a.g = null;
                        this.a.c();
                        return;
                    default:
                        return;
                }
            }
        };
        this.c = new Handler(this, Looper.getMainLooper()) {
            final /* synthetic */ s a;

            public void handleMessage(Message message) {
                super.handleMessage(message);
                switch (message.what) {
                    case 1:
                    case 3:
                        ((Task) message.obj).finish();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    protected void a() {
        this.f = true;
    }

    protected void b() {
        this.f = false;
    }

    protected void c() {
        if (this.g == null) {
            this.g = d();
            if (this.g != null) {
                c(this.g);
                this.a.a(this.g);
            }
        }
    }

    protected Task a(Task task) {
        if (!this.f || this.a.a() == 3 || this.a.a() == 4) {
            return null;
        }
        ((ArrayList) this.d.get(task.getPriority())).add(task);
        c();
        return task;
    }

    private Task d() {
        ArrayList arrayList;
        if (c.b && this.i.isAutoScrolling()) {
            arrayList = (ArrayList) this.d.get(4);
            if (arrayList.size() > 0) {
                Task task = (Task) arrayList.get(0);
                task.setThreadPriority(10);
                return task;
            }
        }
        for (int i = 6; i >= 0; i--) {
            arrayList = (ArrayList) this.d.get(i);
            if (arrayList.size() > 0) {
                return (Task) arrayList.get(0);
            }
        }
        return null;
    }

    private boolean c(Task task) {
        return ((ArrayList) this.d.get(task.getPriority())).remove(task);
    }

    protected boolean b(Task task) {
        if (task.canCancel() && c(task)) {
            task.cancel();
            a(task, true);
        }
        return true;
    }

    protected void a(boolean z) {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            ArrayList arrayList = (ArrayList) it.next();
            Iterator it2 = arrayList.iterator();
            while (it2.hasNext()) {
                Task task = (Task) it2.next();
                if (task.canCancel()) {
                    task.cancel();
                    a(task, true);
                    this.e.add(task);
                }
            }
            arrayList.removeAll(this.e);
            this.e.clear();
        }
    }

    protected void a(Task task, boolean z) {
        Message message = new Message();
        if (task.isModify()) {
            message.what = 3;
        } else {
            message.what = 1;
        }
        message.obj = task;
        if (z) {
            this.c.sendMessage(message);
        } else {
            this.b.sendMessage(message);
        }
    }
}
