package com.foxit.sdk;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.g.a;
import java.util.Iterator;

/* compiled from: PanelPageView */
class m extends a {
    private Bitmap F;
    private Task G;
    private Point H;
    private j<k, i> I;

    protected m(PDFViewCtrl pDFViewCtrl, d dVar) {
        super(pDFViewCtrl, dVar);
        this.d = 1;
        this.H = new Point();
        this.I = new j(8);
    }

    protected void a() {
        super.a();
        if (this.F != null) {
            this.b.a(this.F);
            this.F = null;
        }
        if (this.G != null) {
            this.b.b(this.G);
            this.G = null;
        }
        u();
        v();
        y();
    }

    protected void y() {
        for (i iVar : this.I.b()) {
            this.b.b(iVar.a);
        }
        this.I.c();
    }

    protected boolean q() {
        return this.e || (this.F == null && this.p.a() == 0 && this.I.a() == 0);
    }

    protected void e() {
        if (!this.e) {
            float f = this.l;
            this.l = b();
            this.k = c();
            if (this.m != 1.0f) {
                this.m = Math.min(8.0f, Math.max(1.0f, (f * this.m) / this.l));
            }
        }
    }

    protected void f() {
        if (!this.e) {
            if (this.F != null) {
                this.b.a(this.F);
                this.F = null;
            }
            if (this.G != null) {
                this.b.b(this.G);
                this.G = null;
            }
            u();
            v();
            y();
            b(true);
            a(this.c.left, this.c.top, this.c.right, this.c.bottom, true, false, null);
        }
    }

    protected float b() {
        switch (this.a.getViewStatus().r) {
            case 1:
                return Math.min(((float) this.a.getWidth()) / this.j.x, ((float) this.a.getHeight()) / this.j.y);
            default:
                return ((float) this.a.getWidth()) / this.j.x;
        }
    }

    protected Point c() {
        return new Point((int) (this.j.x * this.l), (int) (this.j.y * this.l));
    }

    protected Point d() {
        return new Point(0, 0);
    }

    protected void a(int i, int i2) {
        if (!this.f || this.c.width() != i || this.c.height() != i2) {
            this.c.right = this.c.left + i;
            this.c.bottom = this.c.top + i2;
            this.m = ((float) this.c.width()) / ((float) this.k.x);
            if (!this.f) {
                this.f = true;
                this.o.set(this.c.width(), this.c.height());
            }
        }
    }

    protected void w() {
        if (!this.e) {
            a(this.c.left, this.c.top, this.c.right, this.c.bottom, false, false, null);
        }
    }

    protected void x() {
        if (!this.e) {
            if (!(this.o.x == this.c.width() && this.o.y == this.c.height())) {
                if (this.p.a() >= this.I.a()) {
                    y();
                    this.H.set(this.o.x, this.o.y);
                    this.I.a(this.p);
                } else {
                    v();
                }
                u();
                this.o.set(this.c.width(), this.c.height());
                this.p.c();
            }
            a(this.c.left, this.c.top, this.c.right, this.c.bottom, false, false, new a(this) {
                final /* synthetic */ m a;

                {
                    this.a = r1;
                }

                public void a(g gVar, boolean z) {
                    this.a.y();
                }
            });
        }
    }

    public void a(Rect rect, final a aVar) {
        b(rect);
        b(true);
        a(this.c.left, this.c.top, this.c.right, this.c.bottom, true, true, new a(this) {
            final /* synthetic */ m b;

            public void a(g gVar, boolean z) {
                if (aVar != null) {
                    aVar.a(gVar, z);
                }
            }
        });
    }

    protected void a(int i, int i2, int i3, int i4, boolean z, boolean z2, a aVar) {
        this.c.set(i, i2, i3, i4);
        if (!this.e && (!c.b || !this.a.isAutoScrolling())) {
            c(y);
            if (y.width() == 0.0f || y.height() == 0.0f) {
                u();
                v();
                if (aVar != null) {
                    aVar.a(null, false);
                    return;
                }
                return;
            }
            float width = ((float) this.o.x) / ((float) this.c.width());
            int i5 = (int) ((y.left * width) / 512.0f);
            int i6 = (int) ((y.right * width) / 512.0f);
            int i7 = (int) ((y.top * width) / 512.0f);
            int i8 = (int) ((width * y.bottom) / 512.0f);
            j jVar = this.p;
            this.p = this.q;
            this.q = jVar;
            jVar = this.r;
            this.r = this.s;
            this.s = jVar;
            b(i7, i8, i5, i6, z, z2, aVar);
            for (i iVar : this.q.b()) {
                this.b.b(iVar.a);
            }
            this.q.c();
            for (Task b : this.s.b()) {
                this.b.b(b);
            }
            this.s.c();
        } else if (aVar != null) {
            aVar.a(null, false);
        }
    }

    private void b(int i, int i2, int i3, int i4, boolean z, boolean z2, a aVar) {
        e anonymousClass3;
        e anonymousClass4;
        if (z) {
            final Rect rect = new Rect(i3 * 512, i * 512, Math.min((i4 + 1) * 512, this.o.x), Math.min((i2 + 1) * 512, this.o.y));
            anonymousClass3 = new e(this) {
                final /* synthetic */ m c;

                public void result(Task task) {
                    f fVar = (f) task;
                    if (!this.c.a.isAutoScrolling()) {
                        if (fVar == null || fVar.a == this.c.h) {
                            this.c.a(rect);
                        }
                    }
                }
            };
        } else {
            anonymousClass3 = null;
        }
        if (aVar != null) {
            final a aVar2 = aVar;
            anonymousClass4 = new e(this) {
                final /* synthetic */ m c;

                public void result(Task task) {
                    aVar2.a(null, true);
                }
            };
        } else {
            anonymousClass4 = null;
        }
        while (i2 >= i) {
            for (int i5 = i3; i5 <= i4; i5++) {
                Rect rect2 = new Rect(i5 * 512, i2 * 512, Math.min((i5 + 1) * 512, this.o.x), Math.min((i2 + 1) * 512, this.o.y));
                if (!(rect2.width() == 0 || rect2.height() == 0)) {
                    u.a(rect2);
                    i iVar = (i) this.q.a(u);
                    if (iVar != null) {
                        this.p.a(new k(rect2), iVar);
                        this.q.b(u);
                        if (!iVar.b) {
                        }
                    }
                    f fVar = (f) this.s.a(u);
                    if (fVar == null || fVar.a()) {
                        final boolean z3 = z;
                        Task fVar2 = new f(this.b, this.b.e(), this.h, this.a.getViewStatus().r, 0, 0, rect2, new Point(this.o), 4, this.a.getViewStatus().l, this.a.getViewStatus().C, new CallBack(this) {
                            final /* synthetic */ m b;

                            public void result(Task task) {
                                f fVar = (f) task;
                                if (fVar.errorCode() == 10) {
                                    this.b.a.recoverForOOM();
                                    return;
                                }
                                if (fVar.mAttachedCallbacks != null) {
                                    Iterator it = fVar.mAttachedCallbacks.iterator();
                                    while (it.hasNext()) {
                                        CallBack callBack = (CallBack) it.next();
                                        e eVar = (e) callBack;
                                        eVar.c();
                                        if (eVar.a() == 0) {
                                            callBack.result(fVar);
                                        }
                                    }
                                }
                                if (this.b.h == fVar.a) {
                                    a.u.a(fVar.e);
                                    if (fVar.f.x == this.b.o.x && this.b.r.a(a.u) == fVar) {
                                        this.b.r.b(a.u);
                                    }
                                    if (fVar.exeSuccess() && !fVar.isCanceled()) {
                                        if (fVar.f.x == this.b.o.x) {
                                            Object kVar = new k(fVar.e);
                                            i iVar = (i) this.b.p.a(kVar);
                                            if (iVar != null) {
                                                this.b.b.b(iVar.a);
                                                this.b.p.b(kVar);
                                            }
                                            this.b.p.a(kVar, new i(fVar.h));
                                            if (!z3 && !this.b.a.isAutoScrolling()) {
                                                this.b.a(fVar.e);
                                                return;
                                            }
                                            return;
                                        } else if (fVar.f.x == this.b.H.x) {
                                            this.b.I.a(new k(fVar.e), new i(fVar.h));
                                            if (!z3 && !this.b.a.isAutoScrolling()) {
                                                this.b.a(fVar.e);
                                                return;
                                            }
                                            return;
                                        }
                                    }
                                }
                                if (fVar.h != null) {
                                    this.b.b.b(fVar.h);
                                }
                            }
                        });
                        if (fVar != null) {
                            fVar.deliverCallbacks(fVar2);
                        }
                        if (anonymousClass3 != null) {
                            fVar2.attachCallback(anonymousClass3);
                            anonymousClass3.b();
                        }
                        if (anonymousClass4 != null) {
                            fVar2.attachCallback(anonymousClass4);
                            anonymousClass4.b();
                        }
                        if (z2) {
                            fVar2.setPriority(6);
                        }
                        this.b.a(fVar2);
                        this.r.a(new k(rect2), fVar2);
                    } else {
                        this.r.a(new k(rect2), fVar);
                        this.s.b(u);
                        if (anonymousClass3 != null) {
                            fVar.attachCallback(anonymousClass3);
                            anonymousClass3.b();
                        }
                        if (anonymousClass4 != null) {
                            fVar.attachCallback(anonymousClass4);
                            anonymousClass4.b();
                        }
                    }
                }
            }
            i2--;
        }
        if (anonymousClass3 != null && anonymousClass3.a() == 0) {
            anonymousClass3.result(null);
        }
        if (anonymousClass4 != null && anonymousClass4.a() == 0) {
            anonymousClass4.result(null);
        }
    }

    private void b(Rect rect) {
        float width = ((float) this.o.x) / ((float) this.c.width());
        int i = (int) ((((float) rect.left) * width) / 512.0f);
        int i2 = (int) ((((float) rect.right) * width) / 512.0f);
        int i3 = (int) ((((float) rect.top) * width) / 512.0f);
        int i4 = (int) ((width * ((float) rect.bottom)) / 512.0f);
        this.H.set(this.o.x, this.o.y);
        for (int i5 = i3; i5 <= i4; i5++) {
            for (int i6 = i; i6 <= i2; i6++) {
                Rect rect2 = new Rect(i6 * 512, i5 * 512, Math.min((i6 + 1) * 512, this.o.x), Math.min((i5 + 1) * 512, this.o.y));
                if (!(rect2.width() == 0 || rect2.height() == 0)) {
                    u.a(rect2);
                    i iVar = (i) this.p.a(u);
                    if (iVar != null) {
                        iVar.b = true;
                    }
                    f fVar = (f) this.r.a(u);
                    if (fVar != null) {
                        fVar.a(true);
                    }
                }
            }
        }
    }

    protected void a(Canvas canvas) {
        d(canvas);
    }

    protected void d(Canvas canvas) {
        y.set(0.0f, 0.0f, (float) this.a.getWidth(), (float) this.a.getHeight());
        if (y.intersect((float) this.c.left, (float) this.c.top, (float) this.c.right, (float) this.c.bottom)) {
            y.offset((float) (-this.c.left), (float) (-this.c.top));
            canvas.getClipBounds(v);
            if (y.intersect((float) v.left, (float) v.top, (float) v.right, (float) v.bottom)) {
                float width = ((float) this.o.x) / ((float) this.c.width());
                a(y, width);
                int i = (int) (y.left / 512.0f);
                int i2 = (int) (y.right / 512.0f);
                int i3 = (int) (y.bottom / 512.0f);
                for (int i4 = (int) (y.top / 512.0f); i4 <= i3; i4++) {
                    for (int i5 = i; i5 <= i2; i5++) {
                        u.a(i5 * 512, i4 * 512, Math.min((i5 + 1) * 512, this.o.x), Math.min((i4 + 1) * 512, this.o.y));
                        if (!(u.a() == 0 || u.b() == 0)) {
                            z.set(u.a);
                            A.set(u.a);
                            i iVar = (i) this.p.a(u);
                            if (iVar != null) {
                                z.intersect(y);
                                z.offset(-A.left, -A.top);
                                A.intersect(y);
                                a(A, 1.0f / width);
                                z.round(w);
                                A.round(x);
                                canvas.drawBitmap(iVar.a, w, x, this.t);
                            } else if (!a(canvas, this.o, A, width)) {
                                if (this.F != null) {
                                    A.intersect(y);
                                    float width2 = ((float) this.F.getWidth()) / ((float) this.o.x);
                                    float height = ((float) this.F.getHeight()) / ((float) this.o.y);
                                    z.set(A.left * width2, A.top * height, width2 * A.right, height * A.bottom);
                                    a(A, 1.0f / width);
                                    z.round(w);
                                    A.round(x);
                                    canvas.drawBitmap(this.F, w, x, this.t);
                                } else {
                                    this.t.setColor(c.d);
                                    A.intersect(y);
                                    a(A, 1.0f / width);
                                    A.round(x);
                                    canvas.drawRect(x, this.t);
                                }
                            }
                        }
                    }
                }
                if (this.a.getUIExtensionsManager() != null) {
                    this.a.getUIExtensionsManager().onDraw(t(), canvas);
                }
                this.a.onDrawForControls(t(), canvas);
            }
        }
    }

    private boolean a(Canvas canvas, Point point, RectF rectF, float f) {
        if (this.I.a() == 0) {
            return false;
        }
        float f2 = ((float) this.H.x) / ((float) point.x);
        B.set(rectF);
        a(B, f2);
        int i = (int) (B.left / 512.0f);
        int i2 = (int) (B.right / 512.0f);
        int i3 = (int) (B.bottom / 512.0f);
        for (int i4 = (int) (B.top / 512.0f); i4 <= i3; i4++) {
            for (int i5 = i; i5 <= i2; i5++) {
                u.a(i5 * 512, i4 * 512, Math.min((i5 + 1) * 512, this.H.x), Math.min((i4 + 1) * 512, this.H.y));
                if (!(u.a() == 0 || u.b() == 0)) {
                    C.set(u.a);
                    D.set(u.a);
                    i iVar = (i) this.I.a(u);
                    if (iVar != null) {
                        C.intersect(B);
                        C.offset(-D.left, -D.top);
                        D.intersect(B);
                        a(D, 1.0f / (f2 * f));
                        C.round(w);
                        D.round(x);
                        canvas.drawBitmap(iVar.a, w, x, this.t);
                    } else if (this.F != null) {
                        D.intersect(B);
                        float width = ((float) this.F.getWidth()) / ((float) this.H.x);
                        float height = ((float) this.F.getHeight()) / ((float) this.H.y);
                        C.set(D.left * width, D.top * height, width * D.right, height * D.bottom);
                        a(D, 1.0f / (f2 * f));
                        C.round(w);
                        D.round(x);
                        canvas.drawBitmap(this.F, w, x, this.t);
                    } else {
                        this.t.setColor(-1);
                        D.intersect(B);
                        a(D, 1.0f / (f2 * f));
                        D.round(x);
                        canvas.drawRect(x, this.t);
                    }
                }
            }
        }
        return true;
    }

    private void a(RectF rectF, float f) {
        rectF.left *= f;
        rectF.top *= f;
        rectF.right *= f;
        rectF.bottom *= f;
    }

    protected void a(int i, PointF pointF, PointF pointF2) {
        boolean z;
        this.e = false;
        this.i.set(pointF);
        this.j.set(pointF2);
        this.l = b();
        this.k = c();
        this.m = 1.0f;
        if (this.h == i) {
            z = true;
        } else {
            this.h = i;
            z = false;
        }
        b(false);
        this.a.onPageLoaded(this.h);
        if (z) {
            this.a.requestLayout(this);
        }
    }

    private void b(final boolean z) {
        if ((this.F == null && this.G == null) || z) {
            Point a = this.b.a(this.h);
            Task fVar = new f(this.b, this.b.e(), this.h, this.a.getViewStatus().r, 0, 0, new Rect(0, 0, a.x, a.y), a, 2, this.a.getViewStatus().l, this.a.getViewStatus().C, new CallBack(this) {
                final /* synthetic */ m b;

                public void result(Task task) {
                    f fVar = (f) task;
                    if (fVar.errorCode() == 10) {
                        this.b.a.recoverForOOM();
                        return;
                    }
                    if (this.b.h == fVar.a) {
                        if (this.b.G == task) {
                            this.b.G = null;
                        }
                        if (fVar.exeSuccess() && !fVar.isCanceled()) {
                            if (this.b.F != null) {
                                this.b.b.a(this.b.F);
                            }
                            this.b.F = fVar.h;
                            if (!z && !this.b.a.isAutoScrolling()) {
                                this.b.w();
                                this.b.a(null);
                                return;
                            }
                            return;
                        }
                    }
                    if (fVar.h != null) {
                        this.b.b.a(fVar.h);
                    }
                }
            });
            fVar.setPriority(4);
            this.b.a(fVar);
            this.G = fVar;
        }
    }
}
