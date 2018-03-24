package com.foxit.sdk;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.g.a;

/* compiled from: ReflowPageView */
class n extends a {
    protected n(PDFViewCtrl pDFViewCtrl, d dVar) {
        super(pDFViewCtrl, dVar);
        this.d = 2;
    }

    protected boolean q() {
        return false;
    }

    protected void f() {
        u();
        v();
        a(this.c.left, this.c.top, this.c.right, this.c.bottom);
    }

    protected void a() {
        super.a();
        u();
        v();
    }

    protected void e() {
        if (!this.e) {
            this.l = b();
            this.k = c();
            this.m = 1.0f;
        }
    }

    protected float b() {
        return ((float) (this.a.getWidth() - 20)) / this.j.x;
    }

    protected Point c() {
        return new Point(this.a.getWidth(), (int) ((this.j.y * this.l) + 40.0f));
    }

    protected Point d() {
        Point point = new Point(0, 0);
        point.x = (this.a.getWidth() - 20) / 1;
        point.y = 0;
        return point;
    }

    protected void a(int i, int i2) {
        if (!this.f || this.c.width() != i || this.c.height() != i2) {
            this.c.right = this.c.left + i;
            this.c.bottom = this.c.top + i2;
            this.m = ((float) this.c.width()) / ((float) this.k.x);
            this.f = true;
        }
    }

    protected void a(int i, PointF pointF, PointF pointF2) {
        this.e = false;
        this.i.set(pointF);
        this.j.set(pointF2);
        this.l = b();
        this.k = c();
        this.o.set(this.k.x - 20, this.k.y - 40);
        if (this.h == i) {
            this.a.requestLayout(this);
        } else {
            this.h = i;
        }
        this.a.onPageLoaded(this.h);
    }

    protected void w() {
        if (!this.e) {
            a(this.c.left, this.c.top, this.c.right, this.c.bottom, false, false, null);
        }
    }

    protected void a(int i, int i2, int i3, int i4, boolean z, boolean z2, a aVar) {
        this.c.set(i, i2, i3, i4);
        if (!this.e) {
            if (!c.b || !this.a.isAutoScrolling()) {
                c(y);
                if (y.width() == 0.0f || y.height() == 0.0f) {
                    u();
                    v();
                    return;
                }
                a(this.o, y, E);
                int i5 = (int) (E.left / 512.0f);
                int i6 = (int) (E.right / 512.0f);
                int i7 = (int) (E.top / 512.0f);
                int i8 = (int) (E.bottom / 512.0f);
                j jVar = this.p;
                this.p = this.q;
                this.q = jVar;
                jVar = this.r;
                this.r = this.s;
                this.s = jVar;
                a(i7, i8, i5, i6, z);
                for (i iVar : this.q.b()) {
                    this.b.b(iVar.a);
                }
                this.q.c();
                for (Task b : this.s.b()) {
                    this.b.b(b);
                }
                this.s.c();
            }
        }
    }

    private void a(int i, int i2, int i3, int i4, boolean z) {
        while (i <= i2) {
            for (int i5 = i3; i5 <= i4; i5++) {
                Rect rect = new Rect(i5 * 512, i * 512, Math.min((i5 + 1) * 512, this.o.x), Math.min((i + 1) * 512, this.o.y));
                if (!(rect.width() == 0 || rect.height() == 0)) {
                    u.a(rect);
                    i iVar = (i) this.q.a(u);
                    if (iVar != null) {
                        this.p.a(new k(rect), iVar);
                        this.q.b(u);
                    } else {
                        f fVar = (f) this.s.a(u);
                        if (fVar != null) {
                            this.r.a(new k(rect), fVar);
                            this.s.b(u);
                        } else {
                            Task fVar2 = new f(this.b, this.b.e(), this.h, this.a.getPageLayoutMode(), (int) this.j.x, (int) this.j.y, rect, new Point(this.o), 4, this.a.getViewStatus().l, this.a.getViewStatus().C, new CallBack(this) {
                                final /* synthetic */ n a;

                                {
                                    this.a = r1;
                                }

                                public void result(Task task) {
                                    f fVar = (f) task;
                                    if (this.a.h == fVar.a) {
                                        a.u.a(fVar.e);
                                        if (fVar.f.x == this.a.o.x && this.a.r.a(a.u) == fVar) {
                                            this.a.r.b(a.u);
                                        }
                                        if (fVar.exeSuccess() && !fVar.isCanceled()) {
                                            this.a.p.a(new k(fVar.e), new i(fVar.h));
                                            if (!this.a.a.isAutoScrolling()) {
                                                a.u.a.offset(10, 20);
                                                this.a.a(a.u.a);
                                                return;
                                            }
                                            return;
                                        }
                                    }
                                    if (fVar.h != null) {
                                        this.a.b.b(fVar.h);
                                    }
                                }
                            });
                            this.b.a(fVar2);
                            this.r.a(new k(rect), fVar2);
                        }
                    }
                }
            }
            i++;
        }
    }

    private void a(Point point, RectF rectF, RectF rectF2) {
        rectF2.set(rectF);
        rectF2.offset(-10.0f, -20.0f);
        rectF2.left = Math.max(0.0f, rectF2.left);
        rectF2.right = Math.min((float) point.x, rectF2.right);
        rectF2.top = Math.max(0.0f, rectF2.top);
        rectF2.bottom = Math.min((float) point.y, rectF2.bottom);
    }

    protected void a(Canvas canvas) {
        y.set(0.0f, 0.0f, (float) this.a.getWidth(), (float) this.a.getHeight());
        if (y.intersect((float) this.c.left, (float) this.c.top, (float) this.c.right, (float) this.c.bottom)) {
            y.offset((float) (-this.c.left), (float) (-this.c.top));
            canvas.getClipBounds(v);
            if (y.intersect((float) v.left, (float) v.top, (float) v.right, (float) v.bottom)) {
                this.t.setColor(c.d);
                if (this.e) {
                    canvas.drawRect(y, this.t);
                    return;
                }
                for (int i = 0; i < 4; i++) {
                    if (i == 0) {
                        A.set(0.0f, 0.0f, (float) this.k.x, 20.0f);
                    } else if (i == 1) {
                        A.set(0.0f, (float) (this.k.y - 20), (float) this.k.x, (float) this.k.y);
                    } else if (i == 2) {
                        A.set(0.0f, 0.0f, 10.0f, (float) this.k.y);
                    } else {
                        A.set((float) (this.k.x - 10), 0.0f, (float) this.k.x, (float) this.k.y);
                    }
                    if (A.intersect(y)) {
                        A.round(x);
                        canvas.drawRect(x, this.t);
                    }
                }
                a(this.o, y, E);
                int i2 = (int) (E.left / 512.0f);
                int i3 = (int) (E.right / 512.0f);
                int i4 = (int) (E.bottom / 512.0f);
                for (int i5 = (int) (E.top / 512.0f); i5 <= i4; i5++) {
                    for (int i6 = i2; i6 <= i3; i6++) {
                        u.a(i6 * 512, i5 * 512, Math.min((i6 + 1) * 512, this.o.x), Math.min((i5 + 1) * 512, this.o.y));
                        if (!(u.a() == 0 || u.b() == 0)) {
                            z.set(u.a);
                            A.set(u.a);
                            i iVar = (i) this.p.a(u);
                            if (iVar != null) {
                                z.intersect(E);
                                z.offset(-A.left, -A.top);
                                A.intersect(E);
                                A.offset(10.0f, 20.0f);
                                z.round(w);
                                A.round(x);
                                canvas.drawBitmap(iVar.a, w, x, this.t);
                            } else {
                                A.intersect(E);
                                A.offset(10.0f, 20.0f);
                                A.round(x);
                                canvas.drawRect(x, this.t);
                            }
                        }
                    }
                }
            }
        }
    }
}
