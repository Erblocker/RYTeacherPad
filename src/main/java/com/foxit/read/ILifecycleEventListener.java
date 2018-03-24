package com.foxit.read;

import android.app.Activity;
import android.os.Bundle;

public interface ILifecycleEventListener {
    void onCreate(Activity activity, Bundle bundle);

    void onDestroy(Activity activity);

    void onPause(Activity activity);

    void onResume(Activity activity);

    void onSaveInstanceState(Activity activity, Bundle bundle);

    void onStart(Activity activity);

    void onStop(Activity activity);
}
