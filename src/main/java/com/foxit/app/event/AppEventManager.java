package com.foxit.app.event;

import android.app.Activity;
import android.os.Bundle;
import com.foxit.app.App;
import com.foxit.read.ILifecycleEventListener;
import java.util.ArrayList;
import java.util.Iterator;

public class AppEventManager implements ILifecycleEventListener, IUxEventListener {
    private long mIdleStartTime;
    private ArrayList<ILifecycleEventListener> mLifecycleEventList = new ArrayList();
    private ArrayList<IUxEventListener> mUxEventList = new ArrayList();
    Runnable timeRunnable = new Runnable() {
        public void run() {
            App.instance().getThreadManager().getMainThreadHandler().postDelayed(AppEventManager.this.timeRunnable, 1000);
        }
    };

    public AppEventManager() {
        App.instance().getThreadManager().getMainThreadHandler().postDelayed(this.timeRunnable, 1000);
    }

    public void triggerInteractTimer() {
        this.mIdleStartTime = System.currentTimeMillis();
    }

    public void onCreate(Activity act, Bundle savedInstanceState) {
        Iterator it = this.mLifecycleEventList.iterator();
        while (it.hasNext()) {
            ((ILifecycleEventListener) it.next()).onCreate(act, savedInstanceState);
        }
    }

    public void onStart(Activity act) {
        Iterator it = this.mLifecycleEventList.iterator();
        while (it.hasNext()) {
            ((ILifecycleEventListener) it.next()).onStart(act);
        }
    }

    public void onPause(Activity act) {
        Iterator it = this.mLifecycleEventList.iterator();
        while (it.hasNext()) {
            ((ILifecycleEventListener) it.next()).onPause(act);
        }
    }

    public void onResume(Activity act) {
        Iterator it = this.mLifecycleEventList.iterator();
        while (it.hasNext()) {
            ((ILifecycleEventListener) it.next()).onResume(act);
        }
    }

    public void onStop(Activity act) {
        Iterator it = this.mLifecycleEventList.iterator();
        while (it.hasNext()) {
            ((ILifecycleEventListener) it.next()).onStop(act);
        }
    }

    public void onDestroy(Activity act) {
        Iterator it = this.mLifecycleEventList.iterator();
        while (it.hasNext()) {
            ((ILifecycleEventListener) it.next()).onDestroy(act);
        }
    }

    public void onSaveInstanceState(Activity act, Bundle bundle) {
        Iterator it = this.mLifecycleEventList.iterator();
        while (it.hasNext()) {
            ((ILifecycleEventListener) it.next()).onSaveInstanceState(act, bundle);
        }
    }

    public void onTriggerDismissMenu() {
        Iterator it = this.mUxEventList.iterator();
        while (it.hasNext()) {
            ((IUxEventListener) it.next()).onTriggerDismissMenu();
        }
    }
}
