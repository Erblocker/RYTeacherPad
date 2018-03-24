package com.foxit.sdk;

import android.graphics.Bitmap;

/* compiled from: AbstractPageView */
class i {
    public Bitmap a;
    public boolean b;

    public i(Bitmap bitmap) {
        this.a = bitmap;
    }

    public int hashCode() {
        return this.a.hashCode();
    }

    public boolean equals(Object obj) {
        return this.a.equals(obj);
    }
}
