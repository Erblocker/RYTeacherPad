package com.foxit.sdk;

import com.foxit.sdk.Task.CallBack;

/* compiled from: PanelPageView */
abstract class e implements CallBack {
    int a;

    e() {
    }

    int a() {
        return this.a;
    }

    void b() {
        this.a++;
    }

    void c() {
        this.a--;
    }
}
