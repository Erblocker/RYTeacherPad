package eu.janmuller.android.simplecropimage;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import com.netspace.library.ui.BaseActivity;
import java.util.ArrayList;
import java.util.Iterator;

public class MonitoredActivity extends BaseActivity {
    private final ArrayList<LifeCycleListener> mListeners = new ArrayList();

    public interface LifeCycleListener {
        void onActivityCreated(MonitoredActivity monitoredActivity);

        void onActivityDestroyed(MonitoredActivity monitoredActivity);

        void onActivityPaused(MonitoredActivity monitoredActivity);

        void onActivityResumed(MonitoredActivity monitoredActivity);

        void onActivityStarted(MonitoredActivity monitoredActivity);

        void onActivityStopped(MonitoredActivity monitoredActivity);
    }

    public static class LifeCycleAdapter implements LifeCycleListener {
        public void onActivityCreated(MonitoredActivity activity) {
        }

        public void onActivityDestroyed(MonitoredActivity activity) {
        }

        public void onActivityPaused(MonitoredActivity activity) {
        }

        public void onActivityResumed(MonitoredActivity activity) {
        }

        public void onActivityStarted(MonitoredActivity activity) {
        }

        public void onActivityStopped(MonitoredActivity activity) {
        }
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    public void addLifeCycleListener(LifeCycleListener listener) {
        if (!this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
    }

    public void removeLifeCycleListener(LifeCycleListener listener) {
        this.mListeners.remove(listener);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iterator it = this.mListeners.iterator();
        while (it.hasNext()) {
            ((LifeCycleListener) it.next()).onActivityCreated(this);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        Iterator it = this.mListeners.iterator();
        while (it.hasNext()) {
            ((LifeCycleListener) it.next()).onActivityDestroyed(this);
        }
    }

    protected void onStart() {
        super.onStart();
        Iterator it = this.mListeners.iterator();
        while (it.hasNext()) {
            ((LifeCycleListener) it.next()).onActivityStarted(this);
        }
    }

    protected void onStop() {
        super.onStop();
        Iterator it = this.mListeners.iterator();
        while (it.hasNext()) {
            ((LifeCycleListener) it.next()).onActivityStopped(this);
        }
    }
}
