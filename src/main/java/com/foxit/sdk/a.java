package com.foxit.sdk;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.foxit.sdk.common.Font;

/* compiled from: AbstractPageView */
abstract class a {
    static RectF A = new RectF();
    static RectF B = new RectF();
    static RectF C = new RectF();
    static RectF D = new RectF();
    static RectF E = new RectF();
    static k u = new k(new Rect());
    static Rect v = new Rect();
    static Rect w = new Rect();
    static Rect x = new Rect();
    static RectF y = new RectF();
    static RectF z = new RectF();
    protected PDFViewCtrl a;
    protected d b;
    protected Rect c;
    protected int d;
    protected boolean e;
    protected boolean f;
    protected boolean g;
    protected int h;
    protected PointF i;
    protected PointF j;
    protected Point k;
    protected float l;
    protected float m;
    protected int n = -1;
    protected Point o;
    protected j<k, i> p;
    protected j<k, i> q;
    protected j<k, f> r;
    protected j<k, f> s;
    protected Paint t;

    protected abstract void a(int i, int i2);

    protected abstract void a(int i, int i2, int i3, int i4, boolean z, boolean z2, com.foxit.sdk.g.a aVar);

    protected abstract void a(int i, PointF pointF, PointF pointF2);

    protected abstract void a(Canvas canvas);

    protected abstract float b();

    protected abstract Point c();

    protected abstract Point d();

    protected abstract void e();

    protected abstract void f();

    protected a(PDFViewCtrl pDFViewCtrl, d dVar) {
        this.a = pDFViewCtrl;
        this.b = dVar;
        this.c = new Rect();
        this.e = true;
        this.f = false;
        this.g = false;
        this.h = -1;
        this.i = new PointF();
        this.j = new PointF();
        this.k = new Point();
        this.l = 1.0f;
        this.m = 1.0f;
        this.o = new Point();
        this.p = new j(8);
        this.q = new j(8);
        this.r = new j(8);
        this.s = new j(8);
        this.t = new Paint();
        this.t.setStyle(Style.FILL);
        this.t.setColor(-1);
        this.t.setAntiAlias(false);
        this.t.setFilterBitmap(false);
    }

    protected void a(int i, PointF pointF) {
        this.e = true;
        this.f = false;
        this.g = false;
        this.h = i;
        this.i.set(pointF);
        this.j.set(pointF);
        this.l = b();
        this.k = c();
        this.m = 1.0f;
    }

    protected void a() {
        this.e = true;
        this.f = false;
        this.g = false;
        this.h = -1;
    }

    protected void a(int i) {
        this.n = i;
    }

    protected int g() {
        return this.d;
    }

    protected boolean h() {
        return this.g;
    }

    protected void a(boolean z) {
        this.g = z;
    }

    protected PointF i() {
        return this.i;
    }

    protected Point j() {
        return this.k;
    }

    protected float k() {
        return this.l;
    }

    protected float l() {
        return this.m;
    }

    protected int m() {
        return this.c.left;
    }

    protected int n() {
        return this.c.top;
    }

    protected int o() {
        return this.c.right;
    }

    protected int p() {
        return this.c.bottom;
    }

    protected boolean q() {
        return this.e;
    }

    public int r() {
        return this.c.width();
    }

    public int s() {
        return this.c.height();
    }

    public int t() {
        return this.h;
    }

    public boolean a(PointF pointF) {
        pointF.x += (float) m();
        pointF.y += (float) n();
        return true;
    }

    public boolean b(PointF pointF) {
        pointF.x -= (float) m();
        pointF.y -= (float) n();
        return true;
    }

    public boolean a(RectF rectF) {
        rectF.offset((float) (-m()), (float) (-n()));
        return true;
    }

    public boolean b(RectF rectF) {
        rectF.offset((float) m(), (float) n());
        return true;
    }

    public void a(Rect rect) {
        if (rect == null) {
            this.a.invalidate();
            return;
        }
        Rect rect2 = new Rect(rect);
        rect2.offset(this.c.left, this.c.top);
        this.a.invalidate(rect2);
    }

    public void a(Rect rect, com.foxit.sdk.g.a aVar) {
        a(rect);
    }

    protected void c(RectF rectF) {
        rectF.set(0.0f, 0.0f, (float) this.a.getWidth(), (float) this.a.getHeight());
        if (rectF.intersect((float) this.c.left, (float) this.c.top, (float) this.c.right, (float) this.c.bottom)) {
            rectF.offset((float) (-this.c.left), (float) (-this.c.top));
        } else {
            rectF.set(0.0f, 0.0f, 0.0f, 0.0f);
        }
    }

    protected void u() {
        for (Task b : this.r.b()) {
            this.b.b(b);
        }
        this.r.c();
    }

    protected void v() {
        for (i iVar : this.p.b()) {
            this.b.b(iVar.a);
        }
        this.p.c();
    }

    protected void w() {
    }

    protected void x() {
    }

    protected void a(int i, int i2, int i3, int i4) {
        a(i, i2, i3, i4, false, false, null);
    }

    protected void b(Canvas canvas) {
        canvas.save();
        canvas.clipRect(this.c);
        canvas.translate((float) this.c.left, (float) this.c.top);
        if (q()) {
            c(canvas);
        } else {
            a(canvas);
        }
        canvas.restore();
    }

    void c(Canvas canvas) {
        y.set(0.0f, 0.0f, (float) this.a.getWidth(), (float) this.a.getHeight());
        if (y.intersect((float) this.c.left, (float) this.c.top, (float) this.c.right, (float) this.c.bottom)) {
            y.offset((float) (-this.c.left), (float) (-this.c.top));
            canvas.getClipBounds(v);
            if (y.intersect((float) v.left, (float) v.top, (float) v.right, (float) v.bottom)) {
                if (this.a.getViewStatus().C) {
                    this.t.setColor(c.d);
                } else {
                    this.t.setColor(Color.argb(255, 233, 240, Font.e_fontCharsetEastEurope));
                }
                this.t.setStyle(Style.FILL);
                canvas.drawRect(y, this.t);
            }
        }
    }
}
