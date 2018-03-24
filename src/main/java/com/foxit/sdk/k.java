package com.foxit.sdk;

import android.graphics.Rect;

/* compiled from: AbstractPageView */
class k {
    protected Rect a;

    public k(Rect rect) {
        this.a = rect;
    }

    public void a(Rect rect) {
        this.a.set(rect);
    }

    public void a(int i, int i2, int i3, int i4) {
        this.a.set(i, i2, i3, i4);
    }

    public int a() {
        return this.a.width();
    }

    public int b() {
        return this.a.height();
    }

    public int hashCode() {
        return (((((this.a.left * 31) + this.a.top) * 31) + this.a.right) * 31) + this.a.bottom;
    }

    public boolean equals(Object obj) {
        return this.a.equals(((k) obj).a);
    }
}
