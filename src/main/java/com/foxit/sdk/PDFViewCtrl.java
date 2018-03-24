package com.foxit.sdk;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;
import android.widget.Toast;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.RecoveryManager;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import io.vov.vitamio.ThumbnailUtils;
import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpStatus;

public class PDFViewCtrl extends ViewGroup implements OnDoubleTapListener, OnGestureListener, OnScaleGestureListener {
    protected static final int MOVING_DIAGONALLY = 0;
    protected static final int MOVING_DOWN = 4;
    protected static final int MOVING_LEFT = 1;
    protected static final int MOVING_RIGHT = 2;
    protected static final int MOVING_UP = 3;
    public static final int PAGELAYOUTMODE_CONTINUOUS = 2;
    public static final int PAGELAYOUTMODE_REFLOW = 3;
    public static final int PAGELAYOUTMODE_SINGLE = 1;
    public static final int ZOOMMODE_CUSTOMIZE = 0;
    public static final int ZOOMMODE_FITHEIGHT = 2;
    public static final int ZOOMMODE_FITPAGE = 3;
    public static final int ZOOMMODE_FITWIDTH = 1;
    private static Point p = new Point();
    private static PointF q = new PointF();
    private static PointF r = new PointF();
    private ArrayList<IGestureEventListener> A = new ArrayList();
    private ArrayList<IScaleGestureEventListener> B = new ArrayList();
    private ArrayList<IDoubleTapEventListener> C = new ArrayList();
    private int D = -1;
    private DisplayMetrics E;
    private int F;
    private int G;
    private boolean H;
    private int I;
    private int J;
    private int K;
    private int L;
    private PointF M;
    private d a;
    private Context b;
    private u c;
    private PDFDoc d = null;
    private int e = 0;
    private Scroller f;
    private GestureDetector g;
    private ScaleGestureDetector h;
    private PDFViewCtrl i = null;
    private SparseArray<a> j;
    private LinkedList<a> k;
    private e l = null;
    private int m = -1;
    protected ArrayList<b> mNextViewStack;
    protected ArrayList<b> mPreViewStack;
    protected int mScrollLastX;
    protected int mScrollLastY;
    private int n;
    private a o;
    private d s = null;
    public boolean shouldRecover = true;
    private UIExtensionsManager t = null;
    private ThumbListView u = null;
    private ArrayList<IDocEventListener> v = new ArrayList();
    private ArrayList<IPageEventListener> w = new ArrayList();
    private List<IRecoveryEventListener> x = new ArrayList();
    private ArrayList<IDrawEventListener> y = new ArrayList();
    private ArrayList<ITouchEventListener> z = new ArrayList();

    public interface IDocEventListener {
        void onDocClosed(PDFDoc pDFDoc, int i);

        void onDocOpened(PDFDoc pDFDoc, int i);

        void onDocSaved(PDFDoc pDFDoc, int i);

        void onDocWillClose(PDFDoc pDFDoc);

        void onDocWillOpen();

        void onDocWillSave(PDFDoc pDFDoc);
    }

    public interface IDoubleTapEventListener {
        boolean onDoubleTap(MotionEvent motionEvent);

        boolean onDoubleTapEvent(MotionEvent motionEvent);

        boolean onSingleTapConfirmed(MotionEvent motionEvent);
    }

    public interface IDrawEventListener {
        void onDraw(int i, Canvas canvas);
    }

    public interface IGestureEventListener {
        boolean onDown(MotionEvent motionEvent);

        boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2);

        void onLongPress(MotionEvent motionEvent);

        boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2);

        void onShowPress(MotionEvent motionEvent);

        boolean onSingleTapUp(MotionEvent motionEvent);
    }

    public interface IPageEventListener {
        void onPageChanged(int i, int i2);

        void onPageInvisible(int i);

        void onPageJumped();

        void onPageMoved(boolean z, int i, int i2);

        void onPageVisible(int i);

        void onPageWillMove(int i, int i2);

        void onPagesInserted(boolean z, int i, int[] iArr);

        void onPagesRemoved(boolean z, int[] iArr);

        void onPagesRotated(boolean z, int[] iArr, int i);

        void onPagesWillInsert(int i, int[] iArr);

        void onPagesWillRemove(int[] iArr);

        void onPagesWillRotate(int[] iArr, int i);
    }

    public interface IRecoveryEventListener {
        void onRecovered();

        void onWillRecover();
    }

    public interface IScaleGestureEventListener {
        boolean onScale(ScaleGestureDetector scaleGestureDetector);

        boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector);

        void onScaleEnd(ScaleGestureDetector scaleGestureDetector);
    }

    public interface ITouchEventListener {
        boolean onTouchEvent(MotionEvent motionEvent);
    }

    class b {
        protected int a;
        protected float b;
        protected float c;
        protected int d;
        protected float e;
        protected float f;
        final /* synthetic */ PDFViewCtrl g;

        protected b(PDFViewCtrl pDFViewCtrl, int i, float f, float f2) {
            this.g = pDFViewCtrl;
            this.a = i;
            this.b = f;
            this.c = f2;
        }

        protected void a(int i, float f, float f2) {
            this.d = i;
            this.e = f;
            this.f = f2;
        }
    }

    private static class c implements Runnable {
        private final WeakReference<PDFViewCtrl> a;

        public c(PDFViewCtrl pDFViewCtrl) {
            this.a = new WeakReference(pDFViewCtrl);
        }

        public void run() {
            ((PDFViewCtrl) this.a.get()).a();
        }
    }

    private class d implements Runnable {
        final /* synthetic */ PDFViewCtrl a;
        private final WeakReference<PDFViewCtrl> b;
        private int c = 0;
        private int d = 1;
        private float e = 1.0f;
        private Point f = new Point(0, 0);
        private int g = 1;
        private int h = 1;

        protected d(PDFViewCtrl pDFViewCtrl, PDFViewCtrl pDFViewCtrl2) {
            this.a = pDFViewCtrl;
            this.b = new WeakReference(pDFViewCtrl2);
        }

        protected void a(int i) {
            this.c = i;
        }

        protected void b(int i) {
            this.d = i;
        }

        protected void c(int i) {
            this.h = i;
        }

        protected void a(float f) {
            this.e = f;
        }

        protected void a(Point point) {
            this.f = point;
        }

        protected void d(int i) {
            this.g = i;
        }

        public void run() {
            PDFViewCtrl pDFViewCtrl = (PDFViewCtrl) this.b.get();
            pDFViewCtrl.b();
            int d = pDFViewCtrl.D;
            switch (pDFViewCtrl.D) {
                case 1:
                    pDFViewCtrl.c();
                    break;
                case 2:
                    pDFViewCtrl.e(this.c);
                    break;
                case 3:
                    pDFViewCtrl.g(this.d);
                    break;
                case 4:
                    pDFViewCtrl.a(this.f, this.e);
                    break;
                case 5:
                    pDFViewCtrl.i(this.g);
                    break;
                case 6:
                    pDFViewCtrl.i();
                    break;
                case 7:
                    pDFViewCtrl.h(this.h);
                    break;
            }
            pDFViewCtrl.setOperationMode(0);
            if (d == 1) {
                this.a.a(this.a.d, this.a.e);
            }
        }
    }

    class e {
        Rect b = new Rect();
        Rect c = new Rect();
        final /* synthetic */ PDFViewCtrl d;

        e(PDFViewCtrl pDFViewCtrl) {
            this.d = pDFViewCtrl;
        }

        protected void a(int i, int i2, int i3) {
            if (this.d.c.c != i || this.d.c.f != i2 || this.d.c.g != i3) {
                if (this.d.c.c != i) {
                    int i4 = this.d.c.c;
                    this.d.c.c = i;
                    this.d.i.a(i4, this.d.c.c);
                }
                this.d.c.f = i2;
                this.d.c.g = i3;
            }
        }

        protected void a(float f) {
            if (!this.d.h() && this.d.c.j == f) {
                return;
            }
            if (!this.d.h() || this.d.c.n != f) {
                if (this.d.h()) {
                    this.d.c.n = f;
                } else {
                    this.d.c.j = f;
                }
                for (int size = this.d.j.size() - 1; size >= 0; size--) {
                    a((a) this.d.j.valueAt(size));
                }
            }
        }

        protected float a(int i) {
            return this.d.c.j;
        }

        protected void a(int i, int i2, int i3, int i4) {
            int size;
            for (size = this.d.j.size() - 1; size >= 0; size--) {
                ((a) this.d.j.valueAt(size)).e();
            }
            a aVar = (a) this.d.j.get(this.d.c.c);
            if (aVar != null) {
                a(aVar.l());
                for (size = this.d.j.size() - 1; size >= 0; size--) {
                    aVar = (a) this.d.j.valueAt(size);
                    a(aVar);
                    aVar.x();
                }
            }
        }

        protected void b() {
            for (int size = this.d.j.size() - 1; size >= 0; size--) {
                ((a) this.d.j.valueAt(size)).f();
            }
        }

        protected void c() {
            if (this.d.f.isFinished()) {
                Point point = new Point(0, 0);
                a aVar = (a) this.d.j.get(this.d.c.c);
                if (aVar != null) {
                    point.x = this.d.a(this.d.c(aVar)).x;
                }
                aVar = (a) this.d.j.get(0);
                if (aVar != null && aVar.n() + this.d.L > 0) {
                    point.y = -(aVar.n() + this.d.L);
                    aVar = (a) this.d.j.get(this.d.getPageCount() - 1);
                    if (aVar != null && (aVar.p() + this.d.bottomGap(aVar)) + point.y < this.d.i.getHeight()) {
                        point.y = ((this.d.i.getHeight() - ((this.d.bottomGap(aVar) + aVar.p()) + point.y)) / 2) + point.y;
                    }
                }
                aVar = (a) this.d.j.get(this.d.getPageCount() - 1);
                if (aVar != null && (aVar.p() + this.d.bottomGap(aVar)) + this.d.L < this.d.i.getHeight()) {
                    if (point.y != 0) {
                        point.y = 0;
                    } else {
                        point.y = this.d.i.getHeight() - ((this.d.bottomGap(aVar) + aVar.p()) + this.d.L);
                        aVar = (a) this.d.j.get(0);
                        if (aVar != null && aVar.n() + point.y > 0) {
                            point.y -= (aVar.n() + point.y) / 2;
                        }
                    }
                }
                if (point.x != 0 || Math.abs(point.y) > 1) {
                    PDFViewCtrl pDFViewCtrl = this.d;
                    this.d.mScrollLastY = 0;
                    pDFViewCtrl.mScrollLastX = 0;
                    this.d.f.startScroll(0, 0, point.x, point.y, HttpStatus.SC_BAD_REQUEST);
                    this.d.c.p = 1;
                    this.d.post(new h(this.d.i));
                }
            }
        }

        protected void a(float f, float f2) {
            a aVar = (a) this.d.j.get(this.d.c.c);
            if (aVar != null) {
                if (aVar.m() + this.d.K > this.d.i.getWidth() / 2) {
                    this.d.K = this.d.K - ((aVar.m() + this.d.K) - (this.d.i.getWidth() / 2));
                }
                if ((aVar.m() + aVar.r()) + this.d.K < this.d.i.getWidth() / 2) {
                    this.d.K = ((this.d.i.getWidth() / 2) - ((aVar.r() + aVar.m()) + this.d.K)) + this.d.K;
                }
            }
            aVar = (a) this.d.j.get(0);
            if (aVar != null && aVar.n() + this.d.L > this.d.i.getHeight() / 2) {
                this.d.L = this.d.L - ((aVar.n() + this.d.L) - (this.d.i.getHeight() / 2));
            }
            aVar = (a) this.d.j.get(this.d.getPageCount() - 1);
            if (aVar != null && (aVar.n() + aVar.s()) + this.d.L < this.d.i.getHeight() / 2) {
                this.d.L = ((this.d.i.getHeight() / 2) - ((aVar.s() + aVar.n()) + this.d.L)) + this.d.L;
            }
        }

        protected boolean b(float f, float f2) {
            a aVar;
            if (f2 > 0.0f) {
                aVar = (a) this.d.j.get(0);
                if (aVar != null && aVar.n() + this.d.L >= 0) {
                    return false;
                }
            }
            aVar = (a) this.d.j.get(this.d.getPageCount() - 1);
            if (aVar != null && aVar.p() + this.d.L <= this.d.i.getHeight()) {
                return false;
            }
            aVar = (a) this.d.j.get(this.d.c.c);
            if (aVar == null) {
                return false;
            }
            Rect a = this.d.c(aVar);
            a.inset(0, -10000);
            this.d.f.fling(0, 0, this.d.c.o ? 0 : (int) f, (int) f2, a.left, a.right, a.top, a.bottom);
            this.d.c.p = 2;
            return true;
        }

        protected void a(ScaleGestureDetector scaleGestureDetector) {
            float f = this.d.c.j;
            float min = Math.min(8.0f, Math.max(1.0f, scaleGestureDetector.getScaleFactor() * f));
            float f2 = min / f;
            if (f2 != 1.0f) {
                a(min);
                a aVar = (a) this.d.j.get(this.d.c.c);
                if (aVar != null) {
                    int focusX = ((int) scaleGestureDetector.getFocusX()) - (aVar.m() + this.d.K);
                    int focusY = ((int) scaleGestureDetector.getFocusY()) - (aVar.n() + this.d.L);
                    this.d.K = (int) ((((float) focusX) - (((float) focusX) * f2)) + ((float) this.d.K));
                    this.d.L = (int) ((((float) focusY) - (((float) focusY) * f2)) + ((float) this.d.L));
                }
                this.d._layoutPages();
            }
        }

        protected boolean a(MotionEvent motionEvent) {
            if (this.d.c.j == 1.0f) {
                this.d.c.p = 4;
            } else {
                this.d.c.p = 5;
            }
            this.d.M = new PointF(motionEvent.getX(), motionEvent.getY());
            return true;
        }

        protected void a(a aVar) {
            aVar.a((int) (((float) aVar.j().x) * this.d.c.j), (int) (((float) aVar.j().y) * this.d.c.j));
        }

        protected void a() {
        }

        protected void a(int i, int i2) {
            for (int size = this.d.j.size() - 1; size >= 0; size--) {
                int keyAt = this.d.j.keyAt(size);
                a aVar;
                if (keyAt < i || keyAt > i2) {
                    aVar = (a) this.d.j.get(keyAt);
                    if (aVar.h()) {
                        this.d.i.d(aVar.t());
                    }
                    aVar.a();
                    this.d.k.add(aVar);
                    this.d.j.remove(keyAt);
                } else {
                    aVar = (a) this.d.j.get(keyAt);
                    if (aVar.t() == this.d.c.c) {
                        if (!aVar.h()) {
                            aVar.a(true);
                            this.d.i.c(aVar.t());
                        }
                    } else if (aVar.h()) {
                        aVar.a(false);
                        this.d.i.d(aVar.t());
                    }
                }
            }
        }
    }

    private static class h implements Runnable {
        private final WeakReference<PDFViewCtrl> a;

        public h(PDFViewCtrl pDFViewCtrl) {
            this.a = new WeakReference(pDFViewCtrl);
        }

        public void run() {
            ((PDFViewCtrl) this.a.get()).m();
        }
    }

    public interface UIExtensionsManager extends IDoubleTapEventListener, IDrawEventListener, IGestureEventListener, IScaleGestureEventListener {
        Annot getFocusAnnot();

        boolean onTouchEvent(int i, MotionEvent motionEvent);

        boolean shouldViewCtrlDraw(Annot annot);
    }

    class a extends e {
        final /* synthetic */ PDFViewCtrl a;

        a(PDFViewCtrl pDFViewCtrl) {
            this.a = pDFViewCtrl;
            super(pDFViewCtrl);
        }

        protected void a() {
            int height;
            int i;
            int i2;
            int i3;
            int i4;
            int i5;
            a aVar = (a) this.a.j.get(this.a.c.c);
            if (this.a.c.s) {
                this.a.c.s = false;
                if (aVar != null) {
                    height = this.a.getHeight() / 2;
                    i = 0;
                    if (this.a.c.f > height) {
                        i = height - this.a.c.f;
                    } else if (this.a.c.f + aVar.s() < height) {
                        i = (height - this.a.c.g) - aVar.s();
                    }
                    aVar.a(this.a.c.f, this.a.c.g + i, this.a.c.f + aVar.r(), (i + this.a.c.g) + aVar.s());
                }
            } else {
                while (aVar != null && (aVar.p() + (this.a.bottomGap(aVar) / 2)) + this.a.L < this.a.getHeight() / 2 && this.a.c.c < this.a.getPageCount() - 1) {
                    aVar = (a) this.a.j.get(this.a.c.c + 1);
                    if (aVar != null) {
                        a(this.a.c.c + 1, aVar.m(), aVar.n());
                    } else {
                        a(this.a.c.c + 1, 0, 0);
                    }
                }
                while (aVar != null && (aVar.n() - (this.a.a(aVar.t() - 1) / 2)) + this.a.L > this.a.getHeight() / 2 && this.a.c.c > 0) {
                    aVar = (a) this.a.j.get(this.a.c.c - 1);
                    if (aVar != null) {
                        a(this.a.c.c - 1, aVar.m(), aVar.n());
                    } else {
                        a(this.a.c.c - 1, 0, 0);
                    }
                }
            }
            Object obj = this.a.j.get(this.a.c.c) == null ? 1 : null;
            a k = this.a.k(this.a.c.c);
            if (obj != null) {
                i = this.a.c.f;
                i2 = this.a.c.g;
            } else {
                i = this.a.K + k.m();
                i2 = k.n() + this.a.L;
            }
            this.a.K = this.a.L = 0;
            height = k.r() + i;
            int s = i2 + k.s();
            Point a;
            if (this.a.c.t) {
                if (k.r() <= this.a.getWidth()) {
                    a = this.a.a(this.a.a(i, i2, height, s));
                    i3 = a.x + i;
                    i = a.x + height;
                    height = i3;
                }
                i4 = height;
                height = i;
                i = i4;
            } else {
                if (this.a.f.isFinished() || this.a.c.p == 2) {
                    a = this.a.a(this.a.a(i, i2, height, s));
                    i3 = a.x + i;
                    i = a.x + height;
                    height = i3;
                }
                i4 = height;
                height = i;
                i = i4;
            }
            k.a(height, i2, i, s);
            a(this.a.c.c, height, i2);
            this.b.set(0, 0, this.a.getWidth(), this.a.getHeight());
            while (true) {
                a k2;
                aVar = (a) this.a.j.get(this.a.c.c);
                i3 = aVar.m();
                height = aVar.n();
                i = aVar.o();
                aVar.p();
                i4 = i;
                i = this.a.c.c;
                int i6 = i3;
                i3 = height;
                height = i4;
                while (i > 0) {
                    i--;
                    a k3 = this.a.k(i);
                    this.c.set(((i6 + height) - k3.r()) / 2, (i3 - this.a.bottomGap(k3)) - k3.s(), ((height + i6) + k3.r()) / 2, i3 - this.a.bottomGap(k3));
                    k3.a(this.c.left, this.c.top, this.c.right, this.c.bottom);
                    i6 = this.c.left;
                    i3 = this.c.top;
                    height = this.c.right;
                    s = this.c.bottom;
                    if (!Rect.intersects(this.c, this.b) && this.c.bottom <= 0) {
                        i3 = i;
                        break;
                    }
                }
                i3 = i;
                i6 = aVar.m();
                aVar.n();
                height = aVar.o();
                i4 = aVar.p();
                i = this.a.c.c;
                s = i6;
                i6 = height;
                height = i4;
                while (i < this.a.getPageCount() - 1) {
                    i5 = i + 1;
                    k2 = this.a.k(i5);
                    this.c.set(((s + i6) - k2.r()) / 2, this.a.bottomGap(aVar) + height, ((i6 + s) + k2.r()) / 2, (this.a.bottomGap(aVar) + height) + k2.s());
                    k2.a(this.c.left, this.c.top, this.c.right, this.c.bottom);
                    i6 = this.c.left;
                    i2 = this.c.top;
                    height = this.c.right;
                    i2 = this.c.bottom;
                    if (!Rect.intersects(this.c, this.b) && this.c.top >= this.b.bottom) {
                        break;
                    }
                    s = i6;
                    i6 = height;
                    height = i2;
                    aVar = k2;
                    i = i5;
                }
                i5 = i;
                aVar = (a) this.a.j.get(i3);
                k2 = (a) this.a.j.get(i5);
                if (this.a.c.t || (aVar.n() <= 0 && k2.p() + this.a.bottomGap(k2) >= this.a.getHeight())) {
                    break;
                }
                if (!this.a.f.isFinished()) {
                    if (this.a.c.p != 2 && this.a.c.p != 3) {
                        break;
                    }
                    this.a.f.forceFinished(true);
                }
                s = aVar.n();
                int p = k2.p();
                if ((this.a.bottomGap(k2) + p) - s <= this.a.getHeight()) {
                    i6 = ((((this.a.getHeight() + s) - p) - this.a.bottomGap(k2)) / 2) - s;
                } else if (s > 0) {
                    i6 = -s;
                } else if (this.a.bottomGap(k2) + p < this.a.getHeight()) {
                    i6 = (this.a.getHeight() - this.a.bottomGap(k2)) - p;
                } else {
                    i6 = 0;
                }
                for (height = 0; height < this.a.j.size(); height++) {
                    aVar = (a) this.a.j.valueAt(height);
                    this.c.set(aVar.c);
                    this.c.offset(0, i6);
                    aVar.a(this.c.left, this.c.top, this.c.right, this.c.bottom);
                }
                i6 = this.a.getHeight() / 2;
                height = i3;
                while (height <= i5) {
                    aVar = (a) this.a.j.get(height);
                    if (aVar.n() <= i6 && i6 <= aVar.p()) {
                        a(height, aVar.m(), aVar.n());
                        break;
                    } else if (aVar.n() > i6) {
                        a(height, aVar.m(), aVar.n());
                        break;
                    } else {
                        height++;
                    }
                }
                if ((this.a.bottomGap(k2) + p) - s >= this.a.getHeight() || (i3 <= 0 && i5 >= this.a.getPageCount() - 1)) {
                    break;
                }
            }
            a(i3, i5);
        }
    }

    class g extends e {
        final /* synthetic */ PDFViewCtrl a;

        g(PDFViewCtrl pDFViewCtrl) {
            this.a = pDFViewCtrl;
            super(pDFViewCtrl);
        }

        protected void a(int i, int i2, int i3) {
            if (this.a.c.q) {
                super.a(i, i2, i3);
            } else if (this.a.c.c != i || this.a.c.f != i2 || this.a.c.g != i3) {
                if (this.a.c.c != i) {
                    int i4 = this.a.c.c;
                    this.a.c.c = i;
                    this.a.i.a(i4, this.a.c.c);
                    a aVar = (a) this.a.j.get(i);
                    a(aVar != null ? aVar.l() : 1.0f);
                }
                this.a.c.f = i2;
                this.a.c.g = i3;
            }
        }

        protected void a(float f) {
            if (this.a.c.q) {
                super.a(f);
                return;
            }
            this.a.c.j = f;
            a aVar = (a) this.a.j.get(this.a.c.c);
            if (aVar != null) {
                a(aVar);
            }
        }

        protected float a(int i) {
            if (this.a.c.q) {
                return super.a(i);
            }
            a aVar = (a) this.a.j.get(i);
            return aVar != null ? aVar.l() : 1.0f;
        }

        protected void c() {
            if (this.a.f.isFinished()) {
                a aVar = (a) this.a.j.get(this.a.c.c);
                if (aVar != null) {
                    this.a.slideViewOntoScreen(aVar);
                }
            }
        }

        protected void a(float f, float f2) {
            a aVar = (a) this.a.j.get(this.a.c.c);
            if (aVar != null) {
                if (this.a.c.c == 0 && aVar.m() + this.a.K > this.a.i.getWidth() / 2) {
                    this.a.K = this.a.K - ((aVar.m() + this.a.K) - (this.a.i.getWidth() / 2));
                }
                if (this.a.c.c == this.a.getPageCount() - 1 && aVar.o() + this.a.K < this.a.i.getWidth() / 2) {
                    this.a.K = this.a.K + ((this.a.i.getWidth() / 2) - (aVar.o() + this.a.K));
                }
                if (aVar.n() + this.a.L > this.a.i.getHeight() / 2) {
                    this.a.L = this.a.L - ((aVar.n() + this.a.L) - (this.a.i.getHeight() / 2));
                }
                if (aVar.p() + this.a.L < this.a.i.getHeight() / 2) {
                    this.a.L = ((this.a.i.getHeight() / 2) - (aVar.p() + this.a.L)) + this.a.L;
                }
            }
        }

        protected boolean b(float f, float f2) {
            a aVar = (a) this.a.j.get(this.a.c.c);
            if (aVar == null) {
                return false;
            }
            Rect a = this.a.a(aVar.m() + this.a.K, aVar.n() + this.a.L, aVar.o() + this.a.K, this.a.bottomGap(aVar) + (aVar.p() + this.a.L));
            switch (this.a.a(f, f2)) {
                case 1:
                    if (a.left >= 0) {
                        aVar = (a) this.a.j.get(this.a.c.c + 1);
                        if (aVar != null) {
                            this.a.slideViewOntoScreen(aVar);
                            return false;
                        }
                    }
                    break;
                case 2:
                    if (a.right <= 0) {
                        aVar = (a) this.a.j.get(this.a.c.c - 1);
                        if (aVar != null) {
                            this.a.slideViewOntoScreen(aVar);
                            return false;
                        }
                    }
                    break;
                default:
                    if (f < 0.0f && a.left >= this.a.c.k) {
                        aVar = (a) this.a.j.get(this.a.c.c + 1);
                        if (aVar != null) {
                            this.a.slideViewOntoScreen(aVar);
                            return false;
                        }
                    } else if (f > 0.0f && a.right <= (-this.a.c.k)) {
                        aVar = (a) this.a.j.get(this.a.c.c - 1);
                        if (aVar != null) {
                            this.a.slideViewOntoScreen(aVar);
                            return false;
                        }
                    }
                    break;
            }
            PDFViewCtrl pDFViewCtrl = this.a;
            this.a.mScrollLastY = 0;
            pDFViewCtrl.mScrollLastX = 0;
            Rect rect = new Rect(a);
            rect.inset(-100, -100);
            if (!this.a.a(a, f, f2) || !rect.contains(0, 0)) {
                return false;
            }
            this.a.f.fling(0, 0, this.a.c.o ? 0 : (int) f, (int) f2, a.left, a.right, a.top, a.bottom);
            this.a.c.p = 2;
            return true;
        }

        protected void a(a aVar) {
            if (this.a.c.q) {
                super.a(aVar);
                return;
            }
            float f;
            int i;
            int i2;
            float min = Math.min(((float) this.a.getWidth()) / ((float) aVar.j().x), ((float) this.a.getHeight()) / ((float) aVar.j().y));
            if (aVar == this.a.j.get(this.a.c.c)) {
                f = this.a.c.j;
            } else {
                f = aVar.l();
            }
            if (f == 1.0f || f < min) {
                i = (int) (((float) aVar.j().x) * min);
                i2 = (int) (((float) aVar.j().y) * min);
            } else {
                i = (int) (((float) aVar.j().x) * f);
                i2 = (int) (f * ((float) aVar.j().y));
            }
            aVar.a(i, i2);
        }

        protected void a() {
            Point b;
            boolean z;
            int min;
            int min2;
            int i;
            a aVar = (a) this.a.j.get(this.a.c.c);
            if (this.a.c.s) {
                this.a.c.s = false;
                if (aVar != null) {
                    aVar.a(this.a.c.f, this.a.c.g, this.a.c.f + aVar.r(), this.a.c.g + aVar.s());
                }
            } else if (aVar != null) {
                b = this.a.b(aVar);
                if (((aVar.o() + b.x) + (this.a.c.k / 2)) + this.a.K <= this.a.getWidth() / 2 && this.a.c.c < this.a.getPageCount() - 1) {
                    aVar = (a) this.a.j.get(this.a.c.c + 1);
                    if (aVar != null) {
                        a(this.a.c.c + 1, aVar.m(), aVar.n());
                    } else {
                        a(this.a.c.c + 1, 0, 0);
                    }
                }
                if (((aVar.m() - b.x) - (this.a.c.k / 2)) + this.a.K > this.a.getWidth() / 2 && this.a.c.c > 0) {
                    aVar = (a) this.a.j.get(this.a.c.c - 1);
                    if (aVar != null) {
                        a(this.a.c.c - 1, aVar.m(), aVar.n());
                    } else {
                        a(this.a.c.c - 1, 0, 0);
                    }
                }
            }
            if (this.a.j.get(this.a.c.c) == null) {
                z = true;
            } else {
                z = false;
            }
            a k = this.a.k(this.a.c.c);
            b = this.a.b(k);
            if (z) {
                Point c = this.a.a(k);
                min = Math.min(b.x, Math.max(c.x, this.a.c.f));
                min2 = Math.min(b.y, Math.max(c.y, this.a.c.g));
            } else {
                min = this.a.K + k.m();
                min2 = k.n() + this.a.L;
            }
            this.a.K = this.a.L = 0;
            int r = min + k.r();
            int s = min2 + k.s();
            Point a;
            if (!this.a.c.t && this.a.f.isFinished()) {
                if (k.s() + (this.a.bottomGap(k) * 2) <= this.a.getHeight()) {
                    a = this.a.a(this.a.a(min, min2, r, s));
                } else {
                    a = this.a.a(this.a.a(min, min2, r, this.a.bottomGap(k) + s));
                }
                int i2 = a.x + min;
                r += a.x;
                min = a.y + min2;
                s += a.y;
                i = i2;
            } else if (k.s() + (this.a.bottomGap(k) * 2) <= this.a.getHeight()) {
                a = this.a.a(this.a.a(min, min2, r, s));
                min2 += a.y;
                s += a.y;
                i = min;
                min = min2;
            } else {
                i = min;
                min = min2;
            }
            k.a(i, min, r, s);
            a(this.a.c.c, i, min);
            if (this.a.c.q) {
                b(b, i, min, r, s);
            } else {
                a(b, i, min, r, s);
            }
            a(this.a.c.c - 1, this.a.c.c + 1);
        }

        private void a(Point point, int i, int i2, int i3, int i4) {
            int i5;
            int s;
            Object obj = 1;
            if (this.a.c.c > 0) {
                Object obj2 = this.a.j.get(this.a.c.c + -1) == null ? 1 : null;
                a k = this.a.k(this.a.c.c - 1);
                Point b = this.a.b(k);
                if (obj2 != null || k.s() <= this.a.getHeight()) {
                    i5 = b.y;
                    s = k.s() + i5;
                } else {
                    i5 = k.n();
                    s = k.p();
                }
                int i6 = (b.x + this.a.c.k) + point.x;
                k.a((i - i6) - k.r(), i5, i - i6, s);
            }
            if (this.a.c.c < this.a.getPageCount() - 1) {
                int i7;
                if (this.a.j.get(this.a.c.c + 1) != null) {
                    obj = null;
                }
                a k2 = this.a.k(this.a.c.c + 1);
                Point b2 = this.a.b(k2);
                if (obj != null || k2.s() < this.a.getHeight()) {
                    i7 = b2.y;
                    s = k2.s() + i7;
                } else {
                    i7 = k2.n();
                    s = k2.p();
                }
                i5 = b2.x + (point.x + this.a.c.k);
                k2.a(i3 + i5, i7, (i5 + i3) + k2.r(), s);
            }
        }

        private void b(Point point, int i, int i2, int i3, int i4) {
            if (this.a.c.c > 0) {
                a k = this.a.k(this.a.c.c - 1);
                int i5 = (i2 + i4) / 2;
                int i6 = (this.a.b(k).x + this.a.c.k) + point.x;
                k.a((i - i6) - k.r(), i5 - (k.s() / 2), i - i6, i5 + (k.s() / 2));
            }
            if (this.a.c.c < this.a.getPageCount() - 1) {
                k = this.a.k(this.a.c.c + 1);
                i5 = (i2 + i4) / 2;
                int i7 = point.x + this.a.c.k;
                i6 = this.a.b(k).x + i7;
                k.a(i3 + i6, i5 - (k.s() / 2), (i6 + i3) + k.r(), i5 + (k.s() / 2));
            }
        }
    }

    class f extends a {
        final /* synthetic */ PDFViewCtrl e;

        f(PDFViewCtrl pDFViewCtrl) {
            this.e = pDFViewCtrl;
            super(pDFViewCtrl);
        }

        protected void a(int i, int i2, int i3, int i4) {
            this.e.k();
            this.e.a.b.clear();
            this.e.a.f();
        }

        protected void a(ScaleGestureDetector scaleGestureDetector) {
        }

        protected boolean a(MotionEvent motionEvent) {
            return true;
        }

        protected void a(a aVar) {
            aVar.a(aVar.j().x, aVar.j().y);
        }
    }

    public void registerDocEventListener(IDocEventListener iDocEventListener) {
        this.v.add(iDocEventListener);
    }

    public void unregisterDocEventListener(IDocEventListener iDocEventListener) {
        this.v.remove(iDocEventListener);
    }

    public void registerPageEventListener(IPageEventListener iPageEventListener) {
        this.w.add(iPageEventListener);
    }

    public void unregisterPageEventListener(IPageEventListener iPageEventListener) {
        this.w.remove(iPageEventListener);
    }

    public boolean rotatePages(int[] iArr, int i) {
        boolean z = false;
        a(iArr, i);
        int i2 = 0;
        while (i2 < iArr.length) {
            try {
                this.d.getPage(iArr[i2]).setRotation(i);
                i2++;
            } catch (PDFException e) {
            }
        }
        z = true;
        a(z, iArr, i);
        return z;
    }

    public boolean removePages(int[] iArr) {
        boolean z;
        a(iArr);
        for (int i = 0; i < iArr.length - 1; i++) {
            int i2;
            for (i2 = i + 1; i2 < iArr.length; i2++) {
                if (iArr[i] > iArr[i2]) {
                    int i3 = iArr[i];
                    iArr[i] = iArr[i2];
                    iArr[i2] = i3;
                }
            }
        }
        try {
            i2 = iArr.length - 1;
            z = false;
            while (i2 >= 0) {
                PDFPage page = this.d.getPage(iArr[i2]);
                if (this.d.getPageCount() == 1 && iArr[i2] == 0) {
                    z = false;
                } else {
                    z = this.d.removePage(page);
                }
                if (!z) {
                    break;
                }
                i2--;
            }
        } catch (PDFException e) {
            z = false;
        }
        a(z, iArr);
        return z;
    }

    public boolean movePage(int i, int i2) {
        boolean movePageTo;
        b(i, i2);
        try {
            movePageTo = this.d.movePageTo(this.d.getPage(i), i2);
        } catch (PDFException e) {
            movePageTo = false;
        }
        a(movePageTo, i, i2);
        return movePageTo;
    }

    public boolean insertPages(int i, long j, String str, String str2, byte[] bArr, int[] iArr) {
        boolean z;
        a(i, iArr);
        try {
            z = this.d.startImportPagesFromFilePath(i, j, str, str2, bArr, iArr, null) == 2;
        } catch (PDFException e) {
            z = false;
        }
        a(z, i, iArr);
        return z;
    }

    public boolean insertPages(int i, long j, String str, PDFDoc pDFDoc, int[] iArr) {
        boolean z;
        a(i, iArr);
        try {
            z = this.d.startImportPages(i, j, str, pDFDoc, iArr, null) == 2;
        } catch (PDFException e) {
            z = false;
        }
        a(z, i, iArr);
        return z;
    }

    public void registerRecoveryEventListener(IRecoveryEventListener iRecoveryEventListener) {
        if (!this.x.contains(iRecoveryEventListener)) {
            this.x.add(iRecoveryEventListener);
        }
    }

    public void unregisterRecoveryEventListener(IRecoveryEventListener iRecoveryEventListener) {
        if (this.x.contains(iRecoveryEventListener)) {
            this.x.remove(iRecoveryEventListener);
        }
    }

    public void registerDrawEventListener(IDrawEventListener iDrawEventListener) {
        this.y.add(iDrawEventListener);
    }

    public void unregisterDrawEventListener(IDrawEventListener iDrawEventListener) {
        this.y.remove(iDrawEventListener);
    }

    public void registerTouchEventListener(ITouchEventListener iTouchEventListener) {
        this.z.add(iTouchEventListener);
    }

    public void unregisterTouchEventListener(ITouchEventListener iTouchEventListener) {
        this.z.remove(iTouchEventListener);
    }

    public void registerGestureEventListener(IGestureEventListener iGestureEventListener) {
        this.A.add(iGestureEventListener);
    }

    public void unregisterGestureEventListener(IGestureEventListener iGestureEventListener) {
        this.A.remove(iGestureEventListener);
    }

    public void registerScaleGestureEventListener(IScaleGestureEventListener iScaleGestureEventListener) {
        this.B.add(iScaleGestureEventListener);
    }

    public void unregisterScaleGestureEventListener(IScaleGestureEventListener iScaleGestureEventListener) {
        this.B.remove(iScaleGestureEventListener);
    }

    public void registerDoubleTapEventListener(IDoubleTapEventListener iDoubleTapEventListener) {
        this.C.add(iDoubleTapEventListener);
    }

    public void unregisterDoubleTapEventListener(IDoubleTapEventListener iDoubleTapEventListener) {
        this.C.remove(iDoubleTapEventListener);
    }

    public void setUIExtensionsManager(UIExtensionsManager uIExtensionsManager) {
        if (uIExtensionsManager == null) {
            throw new NullPointerException("UIExtensionsManager can not be null");
        }
        this.t = uIExtensionsManager;
    }

    public UIExtensionsManager getUIExtensionsManager() {
        return this.t;
    }

    public PDFViewCtrl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        a(context);
    }

    public PDFViewCtrl(Context context) {
        super(context);
        a(context);
    }

    private void a(Context context) {
        boolean z = false;
        this.i = this;
        this.b = context;
        n();
        this.a = new d(this);
        setWillNotDraw(false);
        setBackgroundColor(Color.argb(255, 225, 225, 225));
        setDrawingCacheEnabled(true);
        this.c = new u();
        this.mPreViewStack = new ArrayList();
        this.mNextViewStack = new ArrayList();
        this.k = new LinkedList();
        this.j = new SparseArray(4);
        this.f = new Scroller(context);
        this.g = new GestureDetector(context, this);
        this.h = new ScaleGestureDetector(context, this);
        if (Runtime.getRuntime().availableProcessors() <= 1) {
            z = true;
        }
        c.b = z;
    }

    protected void _setLayerType(int i) {
        if (!(this.d == null || this.t == null || this.t.getFocusAnnot() == null)) {
            i = 1;
        }
        if (VERSION.SDK_INT > 11) {
            setLayerType(i, null);
        }
    }

    private void a() {
        onWillRecover();
        this.a.a(true);
        this.a.c();
        try {
            PDFDoc reloadDoc = RecoveryManager.reloadDoc(this.d);
            this.c.D = true;
            Toast.makeText(this.b, "It`s out of memory and on recovering.", 1).show();
            setDoc(reloadDoc);
            this.c.D = false;
            onRecovered();
        } catch (PDFException e) {
            if (e.getLastError() == 10) {
                Toast.makeText(this.b, "It`s out of memory and can`t recover.", 1).show();
            }
        }
    }

    public void recoverForOOM() {
        if (this.c.D) {
            Toast.makeText(this.b, "It`s out of memory and can`t recover.", 1).show();
        } else if (this.shouldRecover) {
            post(new c(this));
        }
    }

    protected void doForOOM() {
        if (this.a != null) {
            k();
            this.a.h();
            _layoutPages();
        }
    }

    private void setOperationMode(int i) {
        this.D = i;
    }

    private void b() {
        if (this.c.c >= getPageCount()) {
            this.c.c = 0;
        }
        if (this.c.c < 0) {
            this.c.c = 0;
        }
        a(this.c, true, null);
    }

    private void c() {
        _layoutPages();
        _setLayerType(1);
        this.a.f();
    }

    private void d() {
        this.mPreViewStack.clear();
        this.mNextViewStack.clear();
        this.u = null;
    }

    public void setDoc(PDFDoc pDFDoc) {
        if (pDFDoc == null) {
            throw new NullPointerException("The PDF document can not be null.");
        }
        f();
        l();
        this.a.i();
        d();
        this.d = pDFDoc;
        if (this.c == null) {
            this.c = new u();
        }
        if (getPageCount() > 0) {
            this.c.i = true;
            this.D = 1;
            if (this.s == null) {
                this.s = new d(this, this);
            }
            post(this.s);
        }
    }

    public PDFDoc getDoc() {
        return this.d;
    }

    public int getPageCount() {
        if (this.d == null) {
            throw new NullPointerException("PDFVewer: pdfdocument is null while count pages");
        }
        try {
            int pageCount = this.d.getPageCount();
            if (this.m != -1) {
                return pageCount;
            }
            this.m = pageCount;
            return pageCount;
        } catch (PDFException e) {
            this.e = e.getLastError();
            return -1;
        }
    }

    public void openDoc(String str, byte[] bArr) {
        this.a.a(str, bArr, new q<PDFDoc, Integer, Integer>(this) {
            final /* synthetic */ PDFViewCtrl a;

            {
                this.a = r1;
            }

            public void a(boolean z, PDFDoc pDFDoc, Integer num, Integer num2) {
                if (num.intValue() == 10) {
                    if (this.a.c != null && this.a.c.D) {
                        this.a.a(pDFDoc, num.intValue());
                    }
                    this.a.recoverForOOM();
                    return;
                }
                this.a.e = num.intValue();
                if (z && num.intValue() == 0) {
                    this.a.setDoc(pDFDoc);
                } else {
                    this.a.a(pDFDoc, num.intValue());
                }
            }
        });
    }

    public void openDocFromMemory(byte[] bArr, byte[] bArr2) {
        this.a.a(bArr, bArr2, new q<PDFDoc, Integer, Integer>(this) {
            final /* synthetic */ PDFViewCtrl a;

            {
                this.a = r1;
            }

            public void a(boolean z, PDFDoc pDFDoc, Integer num, Integer num2) {
                if (num.intValue() == 10) {
                    if (this.a.c != null && this.a.c.D) {
                        this.a.a(pDFDoc, num.intValue());
                    }
                    this.a.recoverForOOM();
                    return;
                }
                this.a.e = num.intValue();
                if (z && num.intValue() == 0) {
                    this.a.setDoc(pDFDoc);
                } else {
                    this.a.a(pDFDoc, num.intValue());
                }
            }
        });
    }

    private void e() {
        this.l = null;
        this.c = new u();
        this.I = 0;
        this.J = 0;
        this.K = 0;
        this.L = 0;
        this.mScrollLastX = 0;
        this.mScrollLastY = 0;
        this.M = null;
        this.a.i();
    }

    public void closeDoc() {
        if (this.a != null && isDocumentOpened()) {
            a(this.d);
            if (this.d != null) {
                this.a.a(this.d, new q<PDFDoc, Integer, Integer>(this) {
                    final /* synthetic */ PDFViewCtrl a;

                    {
                        this.a = r1;
                    }

                    public void a(boolean z, PDFDoc pDFDoc, Integer num, Integer num2) {
                        if (num.intValue() == 10) {
                            this.a.recoverForOOM();
                            return;
                        }
                        this.a.l();
                        this.a.e();
                        this.a.d();
                        c.c = null;
                        this.a.b(pDFDoc, num.intValue());
                    }
                });
            }
        }
    }

    public void saveDoc(String str, int i) {
        if ((this.a != null || isDocumentOpened()) && str != null && str.trim().length() >= 1) {
            b(this.d);
            this.a.a(this.d, str, i, new q<PDFDoc, Integer, Integer>(this) {
                final /* synthetic */ PDFViewCtrl a;

                {
                    this.a = r1;
                }

                public void a(boolean z, PDFDoc pDFDoc, Integer num, Integer num2) {
                    if (num.intValue() == 10) {
                        this.a.recoverForOOM();
                    } else {
                        this.a.c(pDFDoc, num.intValue());
                    }
                }
            });
        }
    }

    protected boolean isDocumentOpened() {
        return this.c.i;
    }

    protected u getViewStatus() {
        return this.c;
    }

    public void setHScrollPos(int i) {
        float f;
        float f2 = (float) i;
        float abs = (float) Math.abs(((a) this.j.get(this.c.c)).n());
        if (f2 < 0.0f) {
            f = 0.0f;
        } else {
            f = f2;
        }
        if (f > (((float) getScreenWidth()) * this.c.j) - ((float) getScreenWidth())) {
            f = (((float) getScreenWidth()) * this.c.j) - ((float) getScreenWidth());
        }
        a(this.c.c, -f, -abs);
        post(new h(this));
    }

    public void setVScrollPos(int i) {
        int i2 = 0;
        a aVar = (a) this.j.get(this.c.c);
        if (this.c.r == 1) {
            a(this.c.c, (float) (-Math.abs(aVar.m())), (float) (-i));
            post(new h(this));
            return;
        }
        SparseArray a = this.a.a();
        float f = 0.0f;
        float f2 = 0.0f;
        int i3 = 0;
        while (i3 < a.size()) {
            float width = (((((float) getWidth()) * (((a) a.get(i3)).c.y * this.c.j)) / ((a) a.get(i3)).c.x) + f2) + ((float) this.c.k);
            if (width > ((float) i)) {
                i2 = (int) (((float) i) - f);
                break;
            }
            f2 = width;
            i3++;
            f = width;
        }
        a(i3, (float) (-Math.abs(aVar.m())), (float) (-i2));
        post(new h(this));
    }

    public int getHScrollPos() {
        int m = 0 - ((a) this.j.get(this.c.c)).m();
        if (this.c.r != 1 || m >= 0) {
            return m;
        }
        return 0;
    }

    public int getVScrollPos() {
        int b;
        if (this.c.r == 2) {
            b = b(this.c.c);
        } else {
            b = 0;
        }
        int n = b - ((a) this.j.get(this.c.c)).n();
        if (this.c.r != 1 || n >= 0) {
            return n;
        }
        return 0;
    }

    private int b(int i) {
        SparseArray a = this.a.a();
        float f = 0.0f;
        for (int i2 = 0; i2 < i; i2++) {
            if (((a) a.get(i2)) == null) {
                Log.d("PDFViewCtrl", "getVContinueLength, the count of page size array is less than page index! please wait. ");
                break;
            }
            f += (((float) getWidth()) * (((a) a.get(i2)).c.y * this.c.j)) / ((a) a.get(i2)).c.x;
        }
        return (int) (((float) (this.c.k * i)) + f);
    }

    public int getVScrollRange() {
        if (this.c.r == 2) {
            return b(getPageCount()) - this.c.k;
        }
        return ((a) this.j.get(this.c.c)).s();
    }

    public int getHScrollRange() {
        return ((a) this.j.get(this.c.c)).r();
    }

    public int getCurrentPage() {
        if (this.a == null || !isDocumentOpened()) {
            return -1;
        }
        return this.c.c;
    }

    public int[] getVisiblePages() {
        int i = 0;
        if (this.a == null || !isDocumentOpened() || this.j.size() == 0) {
            return null;
        }
        int[] iArr;
        if (this.c.r == 1) {
            iArr = new int[]{this.c.c};
        } else if (this.c.r == 2) {
            int[] iArr2 = new int[this.j.size()];
            Rect rect = new Rect(0, 0, getWidth(), getHeight());
            int i2 = 0;
            for (int i3 = 0; i3 < this.j.size(); i3++) {
                a aVar = (a) this.j.valueAt(i3);
                if (aVar.c.intersect(rect) || aVar.c.contains(rect) || rect.contains(aVar.c)) {
                    i2++;
                    iArr2[i3] = aVar.t();
                }
            }
            if (i2 == 0) {
                return null;
            }
            iArr = new int[i2];
            while (i < i2) {
                iArr[i] = iArr2[i];
                i++;
            }
        } else {
            iArr = null;
        }
        return iArr;
    }

    public int getPageIndex(PointF pointF) {
        if (this.a == null || !isDocumentOpened()) {
            return -1;
        }
        a pageViewAtPoint = getPageViewAtPoint(new Point((int) pointF.x, (int) pointF.y));
        if (pageViewAtPoint != null) {
            return pageViewAtPoint.t();
        }
        return -1;
    }

    protected a getPageViewAtPoint(Point point) {
        for (int i = 0; i < this.j.size(); i++) {
            a aVar = (a) this.j.valueAt(i);
            if (aVar.c.contains(point.x, point.y)) {
                return aVar;
            }
        }
        return null;
    }

    public int getPageViewWidth(int i) {
        a f = f(i);
        if (f == null) {
            return 0;
        }
        return f.r();
    }

    public int getPageViewHeight(int i) {
        a f = f(i);
        if (f == null) {
            return 0;
        }
        return f.s();
    }

    public int getDisplayViewWidth() {
        return getWidth();
    }

    public int getDisplayViewHeight() {
        return getHeight();
    }

    private void f() {
        Iterator it = this.v.iterator();
        while (it.hasNext()) {
            ((IDocEventListener) it.next()).onDocWillOpen();
        }
    }

    private void a(PDFDoc pDFDoc, int i) {
        Iterator it = this.v.iterator();
        while (it.hasNext()) {
            ((IDocEventListener) it.next()).onDocOpened(pDFDoc, i);
        }
    }

    private void a(PDFDoc pDFDoc) {
        Iterator it = this.v.iterator();
        while (it.hasNext()) {
            ((IDocEventListener) it.next()).onDocWillClose(pDFDoc);
        }
    }

    private void b(PDFDoc pDFDoc, int i) {
        Iterator it = this.v.iterator();
        while (it.hasNext()) {
            ((IDocEventListener) it.next()).onDocClosed(pDFDoc, i);
        }
    }

    private void b(PDFDoc pDFDoc) {
        Iterator it = this.v.iterator();
        while (it.hasNext()) {
            ((IDocEventListener) it.next()).onDocWillSave(pDFDoc);
        }
    }

    private void c(PDFDoc pDFDoc, int i) {
        Iterator it = this.v.iterator();
        while (it.hasNext()) {
            ((IDocEventListener) it.next()).onDocSaved(pDFDoc, i);
        }
    }

    private void c(int i) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPageVisible(i);
        }
    }

    private void d(int i) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPageInvisible(i);
        }
    }

    private void a(int i, int i2) {
        if (i != i2) {
            Iterator it = this.w.iterator();
            while (it.hasNext()) {
                ((IPageEventListener) it.next()).onPageChanged(i, i2);
            }
        }
    }

    private void g() {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPageJumped();
        }
    }

    private void a(int[] iArr) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPagesWillRemove(iArr);
        }
    }

    private void b(int i, int i2) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPageWillMove(i, i2);
        }
    }

    private void a(int[] iArr, int i) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPagesWillRotate(iArr, i);
        }
    }

    private void a(int i, int[] iArr) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPagesWillInsert(i, iArr);
        }
    }

    private void a(boolean z, int[] iArr) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPagesRemoved(z, iArr);
        }
    }

    private void a(boolean z, int i, int i2) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPageMoved(z, i, i2);
        }
    }

    private void a(boolean z, int[] iArr, int i) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPagesRotated(z, iArr, i);
        }
    }

    private void a(boolean z, int i, int[] iArr) {
        Iterator it = this.w.iterator();
        while (it.hasNext()) {
            ((IPageEventListener) it.next()).onPagesInserted(z, i, iArr);
        }
    }

    protected void onWillRecover() {
        for (IRecoveryEventListener onWillRecover : this.x) {
            onWillRecover.onWillRecover();
        }
    }

    protected void onRecovered() {
        for (IRecoveryEventListener onRecovered : this.x) {
            onRecovered.onRecovered();
        }
    }

    protected void onDrawForControls(int i, Canvas canvas) {
        Iterator it = this.y.iterator();
        while (it.hasNext()) {
            ((IDrawEventListener) it.next()).onDraw(i, canvas);
        }
    }

    private boolean a(MotionEvent motionEvent) {
        Iterator it = this.z.iterator();
        while (it.hasNext()) {
            if (((ITouchEventListener) it.next()).onTouchEvent(motionEvent)) {
                return true;
            }
        }
        return false;
    }

    private boolean b(MotionEvent motionEvent) {
        Iterator it = this.C.iterator();
        while (it.hasNext()) {
            if (((IDoubleTapEventListener) it.next()).onSingleTapConfirmed(motionEvent)) {
                return true;
            }
        }
        return false;
    }

    private void e(int i) {
        if (this.D == 0) {
            a aVar = (a) this.j.get(i);
            if (aVar != null) {
                PointF a = this.a.a(i, aVar);
                if (a != null) {
                    aVar.a(i, this.a.b(i), a);
                } else {
                    if (a == null) {
                        u uVar = this.c;
                        float f = (float) u.a;
                        u uVar2 = this.c;
                        a = new PointF(f, (float) u.b);
                    }
                    aVar.a(i, a);
                }
            }
        }
        gotoPage(i, 0.0f, 0.0f);
        if (this.D == 0) {
            post(new h(this));
        }
    }

    private void a(int i, float f, float f2) {
        this.l.a(i, (int) f, (int) f2);
        this.c.s = true;
        _layoutPages();
    }

    public void gotoPage(int i) {
        if (this.a != null && isDocumentOpened()) {
            if (i < 0 || i > getPageCount() - 1) {
                throw new InvalidParameterException("The page index is invalid.");
            } else if (i != this.c.c) {
                p();
                if (this.D == 0) {
                    e(i);
                    return;
                }
                this.D = 2;
                this.s.a(i);
            }
        }
    }

    public void gotoFirstPage() {
        if (this.c.c != 0) {
            gotoPage(0);
        }
    }

    public void gotoLastPage() {
        if (this.c.c != getPageCount() - 1) {
            gotoPage(getPageCount() - 1);
        }
    }

    public void gotoPrevPage() {
        if (this.c.c != 0) {
            gotoPage(this.c.c - 1);
        }
    }

    public void gotoNextPage() {
        if (this.c.c != getPageCount() - 1) {
            gotoPage(this.c.c + 1);
        }
    }

    protected float getPageMatchScale(int i, float f, float f2) {
        a aVar = (a) this.j.get(i);
        if (aVar != null) {
            return aVar.k();
        }
        if (this.c.r == 1) {
            return Math.min(((float) getWidth()) / f, ((float) getHeight()) / f2);
        }
        if (this.c.r == 2) {
            return ((float) getWidth()) / f;
        }
        return 1.0f;
    }

    protected float getPageScale(int i) {
        return this.l.a(i);
    }

    private a f(int i) {
        a aVar = (a) this.j.get(i);
        if (aVar != null && aVar.t() == i) {
            return aVar;
        }
        for (int i2 = 0; i2 < this.j.size(); i2++) {
            aVar = (a) this.j.valueAt(i2);
            if (aVar != null && aVar.t() == i) {
                return aVar;
            }
        }
        return null;
    }

    private void g(int i) {
        j();
        PointF pointF = null;
        a f = f(this.c.c);
        if (f != null) {
            pointF = f.i();
        }
        u uVar = new u(this.c);
        uVar.r = i;
        pageLayoutWillChange(uVar);
        a(uVar, false, pointF);
        pageLayoutChanged(this.c);
        if (this.D == 0) {
            post(new h(this));
        }
    }

    private void h(int i) {
        if (this.c.u != i) {
            this.c.u = i;
            if (this.c.r == 3) {
                k();
                this.a.b.clear();
                this.a.f();
                _layoutPages();
                if (this.D == 0) {
                    post(new h(this));
                }
            }
        }
    }

    public void setPageLayoutMode(int i) {
        if (i != this.c.r) {
            if (i != 1 && i != 2 && i != 3) {
                return;
            }
            if (this.a == null || !isDocumentOpened()) {
                this.c.r = i;
            } else if (this.D == 0) {
                g(i);
            } else {
                this.D = 3;
                this.s.b(i);
            }
        }
    }

    public int getReflowMode() {
        return this.c.u;
    }

    public void setReflowMode(int i) {
        if (i != this.c.u) {
            if (this.a == null || !isDocumentOpened()) {
                this.c.u = i;
            } else if (this.D == 0) {
                h(i);
            } else {
                this.D = 7;
                this.s.c(i);
            }
        }
    }

    public int getPageLayoutMode() {
        return this.c.r;
    }

    private void i(int i) {
        float f = 1.0f;
        this.c.m = i;
        float j = j(i);
        if (j >= 1.0f) {
            if (j > 8.0f) {
                f = 8.0f;
            } else {
                f = j;
            }
        }
        a(new Point(0, 0), f);
    }

    public void setZoomMode(int i) {
        if (i == this.c.m || i < 0 || i > 3) {
            return;
        }
        if (this.a == null || !isDocumentOpened()) {
            this.c.m = 0;
            this.c.j = 1.0f;
        } else if (this.D == 0) {
            i(i);
        } else {
            this.D = 5;
            this.s.d(i);
        }
    }

    protected PointF getPageSize(int i) {
        try {
            PDFPage page = this.d.getPage(i);
            PointF pointF = new PointF(page.getWidth(), page.getHeight());
            try {
                this.d.closePage(i);
                return pointF;
            } catch (Exception e) {
                return pointF;
            }
        } catch (Exception e2) {
            return null;
        }
    }

    private float j(int i) {
        if (this.a == null || !isDocumentOpened()) {
            return 1.0f;
        }
        PointF pageSize;
        PointF b = this.a.b(this.c.c);
        if (b == null) {
            pageSize = getPageSize(this.c.c);
        } else {
            pageSize = b;
        }
        a aVar = (a) this.j.get(this.c.c);
        if (aVar == null) {
            return 1.0f;
        }
        float b2 = aVar.b();
        switch (i) {
            case 0:
                b2 = 1.0f;
                break;
            case 1:
                b2 = (((float) getWidth()) / pageSize.x) / b2;
                break;
            case 2:
                b2 = (((float) getHeight()) / pageSize.y) / b2;
                break;
            case 3:
                b2 = Math.min(((float) getWidth()) / pageSize.x, ((float) getHeight()) / pageSize.y) / b2;
                break;
            default:
                b2 = 1.0f;
                break;
        }
        return b2;
    }

    private void a(Point point, float f) {
        this.K = 0;
        this.L = 0;
        float f2 = h() ? this.c.n : this.c.j;
        this.c.y = 12;
        this.l.a(f);
        float f3 = (h() ? this.c.n : this.c.j) / f2;
        a aVar = (a) this.j.get(this.c.c);
        if (aVar != null) {
            int m = point.x - (aVar.m() + this.K);
            int n = point.y - (aVar.n() + this.L);
            this.K = (int) ((((float) m) - (((float) m) * f3)) + ((float) this.K));
            this.L = (int) ((((float) n) - (((float) n) * f3)) + ((float) this.L));
        }
        if (h()) {
            k();
            this.a.b.clear();
        }
        _layoutPages();
        this.c.y = 0;
        q();
        this.a.b();
        if (this.D == 0) {
            post(new h(this));
        }
    }

    private boolean h() {
        return this.c.r == 3;
    }

    public void setZoom(Point point, float f) {
        if (!h() && f == this.c.j) {
            return;
        }
        if (!h() || f != this.c.n) {
            if (f < 1.0f) {
                f = 1.0f;
            } else if (f > 8.0f) {
                f = 8.0f;
            }
            if (this.a == null || !isDocumentOpened()) {
                if (h()) {
                    this.c.n = f;
                } else {
                    this.c.j = f;
                }
            } else if (this.l == null) {
            } else {
                if (this.D == 0) {
                    a(point, f);
                    return;
                }
                this.D = 4;
                this.s.a(f);
                this.s.a(point);
            }
        }
    }

    public void setZoom(float f) {
        setZoom(new Point(0, 0), f);
    }

    public float getZoom() {
        if (this.c.r == 3) {
            return this.c.n;
        }
        return this.c.j;
    }

    private void i() {
        if (this.j.size() == 0) {
            c();
        } else if (this.l != null) {
            this.l.b();
        }
        if (this.D == 0) {
            post(new h(this));
        }
    }

    public void setNightMode(boolean z) {
        if (z != this.c.C) {
            p();
            if (z) {
                c.d = Color.argb(255, 0, 0, 27);
                c.e = Color.argb(255, 93, 91, 113);
            } else {
                c.d = -1;
                c.e = -16777216;
            }
            if (this.a == null || !isDocumentOpened()) {
                this.c.C = z;
                return;
            }
            this.c.C = z;
            if (this.D == 0) {
                i();
            } else {
                this.D = 6;
            }
        }
    }

    public void setBackgroundColor(int i) {
        super.setBackgroundColor(i);
    }

    public Matrix getDisplayMatrix(int i) {
        if (this.a == null || !isDocumentOpened()) {
            return null;
        }
        return this.a.a(f(i), i);
    }

    public boolean convertPageViewPtToPdfPt(PointF pointF, PointF pointF2, int i) {
        if (pointF == null || pointF2 == null) {
            throw new NullPointerException();
        }
        pointF2.set(pointF);
        if (a(i, pointF2)) {
            return true;
        }
        return false;
    }

    public boolean convertPdfPtToPageViewPt(PointF pointF, PointF pointF2, int i) {
        if (pointF2 == null || pointF == null) {
            throw new NullPointerException();
        }
        pointF2.set(pointF);
        if (b(i, pointF2)) {
            return true;
        }
        return false;
    }

    public boolean convertPdfRectToPageViewRect(RectF rectF, RectF rectF2, int i) {
        if (rectF == null || rectF2 == null) {
            throw new NullPointerException();
        }
        rectF2.set(rectF);
        if (b(i, rectF2)) {
            return true;
        }
        return false;
    }

    public boolean convertPageViewRectToPdfRect(RectF rectF, RectF rectF2, int i) {
        if (rectF2 == null || rectF == null) {
            throw new NullPointerException();
        }
        rectF2.set(rectF);
        if (a(i, rectF2)) {
            return true;
        }
        return false;
    }

    public boolean convertPageViewRectToDisplayViewRect(RectF rectF, RectF rectF2, int i) {
        if (rectF2 == null || rectF == null) {
            throw new NullPointerException();
        }
        rectF2.set(rectF);
        if (c(i, rectF2)) {
            return true;
        }
        return false;
    }

    public boolean convertDisplayViewRectToPageViewRect(RectF rectF, RectF rectF2, int i) {
        if (rectF == null || rectF2 == null) {
            throw new NullPointerException();
        }
        rectF2.set(rectF);
        if (d(i, rectF2)) {
            return true;
        }
        return false;
    }

    public boolean convertPageViewPtToDisplayViewPt(PointF pointF, PointF pointF2, int i) {
        if (pointF == null || pointF2 == null) {
            throw new NullPointerException();
        }
        pointF2.set(pointF);
        if (d(i, pointF2)) {
            return true;
        }
        return false;
    }

    public boolean convertDisplayViewPtToPageViewPt(PointF pointF, PointF pointF2, int i) {
        if (pointF2 == null || pointF == null) {
            throw new NullPointerException();
        }
        pointF2.set(pointF);
        if (c(i, pointF2)) {
            return true;
        }
        return false;
    }

    public void refresh(int i, Rect rect) {
        a f = f(i);
        if (f != null) {
            f.a(rect, new com.foxit.sdk.g.a(this) {
                final /* synthetic */ PDFViewCtrl a;

                {
                    this.a = r1;
                }

                public void a(g gVar, boolean z) {
                    this.a.post(new h(this.a.i));
                }
            });
        }
    }

    private boolean a(int i, RectF rectF) {
        return this.a.b(f(i), i, rectF);
    }

    private boolean b(int i, RectF rectF) {
        return this.a.a(f(i), i, rectF);
    }

    private boolean a(int i, PointF pointF) {
        return this.a.a(f(i), i, pointF);
    }

    private boolean b(int i, PointF pointF) {
        return this.a.b(f(i), i, pointF);
    }

    private boolean c(int i, RectF rectF) {
        a f = f(i);
        if (f == null) {
            return false;
        }
        return f.b(rectF);
    }

    private boolean d(int i, RectF rectF) {
        a f = f(i);
        if (f == null) {
            return false;
        }
        return f.a(rectF);
    }

    private boolean c(int i, PointF pointF) {
        a f = f(i);
        if (f == null) {
            return false;
        }
        return f.b(pointF);
    }

    private boolean d(int i, PointF pointF) {
        a f = f(i);
        if (f == null) {
            return false;
        }
        return f.a(pointF);
    }

    private void a(b bVar) {
        this.mPreViewStack.add(bVar);
        this.mNextViewStack.clear();
    }

    private void j() {
        this.mPreViewStack.clear();
        this.mNextViewStack.clear();
    }

    public boolean hasPrevView() {
        return this.mPreViewStack.size() > 0;
    }

    public boolean hasNextView() {
        return this.mNextViewStack.size() > 0;
    }

    public void gotoPrevView() {
        if (this.mPreViewStack.size() != 0) {
            PointF pointF = new PointF();
            b bVar = (b) this.mPreViewStack.get(this.mPreViewStack.size() - 1);
            if (this.mNextViewStack.size() == 0) {
                pointF.set((float) (-this.c.f), (float) (-this.c.g));
                if (!a(this.c.c, pointF)) {
                    pointF.y = 0.0f;
                    pointF.x = 0.0f;
                }
                bVar.a(this.c.c, pointF.x, pointF.y);
            } else {
                b bVar2 = (b) this.mNextViewStack.get(this.mNextViewStack.size() - 1);
                bVar.a(bVar2.a, bVar2.b, bVar2.c);
            }
            pointF.set(bVar.b, bVar.c);
            if (!b(bVar.a, pointF)) {
                pointF.y = 0.0f;
                pointF.x = 0.0f;
            }
            a(bVar.a, -pointF.x, -pointF.y);
            this.mPreViewStack.remove(bVar);
            this.mNextViewStack.add(bVar);
        }
    }

    public void gotoNextView() {
        if (this.mNextViewStack.size() != 0) {
            PointF pointF = new PointF();
            b bVar = (b) this.mNextViewStack.get(this.mNextViewStack.size() - 1);
            pointF.set(bVar.e, bVar.f);
            if (!b(bVar.d, pointF)) {
                pointF.y = 0.0f;
                pointF.x = 0.0f;
            }
            a(bVar.d, -pointF.x, -pointF.y);
            this.mNextViewStack.remove(bVar);
            this.mPreViewStack.add(bVar);
        }
    }

    public void gotoPage(int i, PointF pointF) {
        if (i >= 0 && i < getPageCount()) {
            p();
            u uVar = this.c;
            int i2 = uVar.f - 1;
            uVar.f = i2;
            PointF pointF2 = new PointF((float) i2, (float) (-this.c.g));
            if (!a(this.c.c, pointF2)) {
                pointF2.y = 0.0f;
                pointF2.x = 0.0f;
            }
            b bVar = new b(this, this.c.c, pointF2.x, pointF2.y);
            PointF pointF3 = new PointF(pointF.x, pointF.y);
            if (!b(i, pointF3)) {
                pointF3.x = 0.0f;
                pointF3.y = 0.0f;
            }
            a(i, -pointF3.x, -pointF3.y);
            pointF2.set((float) (-this.c.f), (float) (-this.c.g));
            a(this.c.c, pointF2);
            if (bVar.a != this.c.c || Math.abs(bVar.b - pointF2.x) >= 1.0f || Math.abs(bVar.c - pointF2.y) >= 1.0f) {
                a(bVar);
                this.i.g();
            }
            post(new h(this));
        }
    }

    public void gotoPage(int i, float f, float f2) {
        if (i >= 0 && i < getPageCount()) {
            p();
            PointF pointF = new PointF((float) (-this.c.f), (float) (-this.c.g));
            if (!a(this.c.c, pointF)) {
                pointF.y = 0.0f;
                pointF.x = 0.0f;
            }
            b bVar = new b(this, this.c.c, pointF.x, pointF.y);
            a(i, -f, -f2);
            pointF.set((float) (-this.c.f), (float) (-this.c.g));
            a(this.c.c, pointF);
            if (bVar.a != this.c.c || Math.abs(bVar.b - pointF.x) >= 1.0f || Math.abs(bVar.c - pointF.y) >= 1.0f) {
                a(bVar);
                this.i.g();
            }
            post(new h(this));
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.a != null && isDocumentOpened()) {
            Rect clipBounds = canvas.getClipBounds();
            for (int i = 0; i < this.j.size(); i++) {
                a aVar = (a) this.j.valueAt(i);
                if (Rect.intersects(aVar.c, clipBounds)) {
                    aVar.b(canvas);
                }
            }
        }
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i >= 10 && i2 >= 10 && this.a != null && isDocumentOpened() && this.l != null) {
            if (i != i3 || i2 != i4) {
                this.l.a(i, i2, i3, i4);
                this.c.s = true;
                _layoutPages();
            }
        }
    }

    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    protected Point getScreenSize() {
        DisplayMetrics displayMetrics = this.b.getApplicationContext().getResources().getDisplayMetrics();
        return new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    private void k() {
        int size = this.j.size();
        for (int i = 0; i < size; i++) {
            ((a) this.j.valueAt(i)).a();
        }
        this.j.clear();
        this.k.clear();
    }

    private void l() {
        k();
        _setLayerType(1);
        this.D = -1;
        this.s = null;
        this.m = -1;
    }

    protected void pageLayoutWillChange(u uVar) {
        if (this.c.r != uVar.r) {
            k();
            if (this.c.r == 3 || uVar.r == 3) {
                this.a.b.clear();
            }
        } else if (this.c.r != 3) {
        } else {
            if (this.c.u != uVar.u || this.c.n != uVar.n) {
                k();
                this.a.b.clear();
            }
        }
    }

    protected void pageLayoutChanged(u uVar) {
        if (this.c.r != uVar.r) {
            if (this.c.r == 3 || uVar.r == 3) {
                this.a.f();
            }
        } else if (this.c.r == 3 && !(this.c.u == uVar.u && this.c.n == uVar.n)) {
            this.a.f();
        }
        _layoutPages();
    }

    private void a(u uVar, boolean z, PointF pointF) {
        float width;
        float f = 1.0f;
        float f2 = uVar.j;
        float f3 = uVar.n;
        if (!(!z && this.c.r == uVar.r && this.c.u == uVar.u)) {
            switch (uVar.r) {
                case 2:
                    this.l = new a(this);
                    uVar.k = 10;
                    break;
                case 3:
                    this.l = new f(this);
                    uVar.k = 5;
                    break;
                default:
                    this.l = new g(this);
                    uVar.k = 20;
                    break;
            }
            if (this.c.r != uVar.r) {
                float min;
                if (pointF != null) {
                    min = Math.min(((float) getWidth()) / pointF.x, ((float) getHeight()) / pointF.y);
                    width = ((float) getWidth()) / pointF.x;
                } else {
                    width = 1.0f;
                    min = 1.0f;
                }
                if (uVar.r == 3) {
                    width = 1.0f;
                    f = Math.min(8.0f, Math.max(1.0f, this.c.j));
                } else if (this.c.r == 3) {
                    width = Math.min(8.0f, Math.max(1.0f, this.c.n));
                } else if (this.c.j != 1.0f) {
                    if (uVar.r == 1) {
                        width /= min;
                    } else if (uVar.r == 2) {
                        width = min / width;
                    } else {
                        width = 1.0f;
                    }
                    width = Math.min(8.0f, Math.max(1.0f, width * this.c.j));
                    f = f3;
                }
                this.c = new u(uVar);
                this.c.j = width;
                this.c.n = f;
            }
        }
        f = f3;
        width = f2;
        this.c = new u(uVar);
        this.c.j = width;
        this.c.n = f;
    }

    public void updatePagesLayout() {
        try {
            int pageCount = this.d.getPageCount();
            if (pageCount <= this.c.c) {
                this.c.c = pageCount - 1;
            }
            if (pageCount != this.m) {
                this.m = pageCount;
                j();
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        this.a.b.clear();
        k();
        _layoutPages();
    }

    protected boolean isAutoScrolling() {
        return this.c.p != 0 || this.c.y == 12;
    }

    protected void _layoutPages() {
        if (this.a != null && isDocumentOpened()) {
            this.l.a();
            invalidate();
        }
    }

    public boolean isPageVisible(int i) {
        if (f(i) == null) {
            return false;
        }
        return true;
    }

    public ThumbListView getThumbnailView() {
        if (this.u == null) {
            this.u = new ThumbListView(this.b);
            this.u.setAdapter(new t(this.b, this, this.a));
            this.u.setDivider(null);
            this.u.setScrollbarFadingEnabled(true);
        }
        return this.u;
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
    }

    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        if (!isDocumentOpened()) {
            return false;
        }
        if (this.c.B) {
            return true;
        }
        if (motionEvent.getPointerCount() != 1 || this.c.A) {
            return false;
        }
        p();
        if (onSingleTapForHooker(motionEvent)) {
            return true;
        }
        if (onSingleTapForDefault(motionEvent)) {
            return true;
        }
        return false;
    }

    public boolean onDoubleTap(MotionEvent motionEvent) {
        if (this.t != null && this.t.onDoubleTap(motionEvent)) {
            return true;
        }
        Iterator it = this.C.iterator();
        while (it.hasNext()) {
            if (((IDoubleTapEventListener) it.next()).onDoubleTap(motionEvent)) {
                return true;
            }
        }
        return this.l.a(motionEvent);
    }

    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        if (this.t == null || !this.t.onDoubleTapEvent(motionEvent)) {
            Iterator it = this.C.iterator();
            while (it.hasNext()) {
                if (((IDoubleTapEventListener) it.next()).onDoubleTapEvent(motionEvent)) {
                    break;
                }
            }
        }
        return true;
    }

    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        if (this.a == null || !isDocumentOpened()) {
            return true;
        }
        p();
        if (this.t != null && this.t.onScale(scaleGestureDetector)) {
            return true;
        }
        Iterator it = this.B.iterator();
        while (it.hasNext()) {
            if (((IScaleGestureEventListener) it.next()).onScale(scaleGestureDetector)) {
                return true;
            }
        }
        if (this.c.y != 12) {
            return false;
        }
        if (this.l == null) {
            return false;
        }
        this.l.a(scaleGestureDetector);
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        if (this.a != null && isDocumentOpened()) {
            p();
            if (this.t == null || !this.t.onScaleBegin(scaleGestureDetector)) {
                Iterator it = this.B.iterator();
                while (it.hasNext()) {
                    if (((IScaleGestureEventListener) it.next()).onScaleBegin(scaleGestureDetector)) {
                        break;
                    }
                }
                this.c.m = 0;
                this.c.y = 12;
                this.c.z = true;
                this.L = 0;
                this.K = 0;
                this.c.A = true;
                _setLayerType(2);
            }
        }
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        if (this.a != null && isDocumentOpened()) {
            p();
            if (this.t != null) {
                this.t.onScaleEnd(scaleGestureDetector);
            }
            Iterator it = this.B.iterator();
            while (it.hasNext()) {
                ((IScaleGestureEventListener) it.next()).onScaleEnd(scaleGestureDetector);
            }
            this.c.y = 0;
            _setLayerType(1);
            q();
            this.a.b();
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.a == null || !isDocumentOpened()) {
            return true;
        }
        p();
        if (onTouchEventForHooker(motionEvent) || onTouchEventForDefault(motionEvent)) {
            return true;
        }
        return false;
    }

    protected boolean onTouchEventForHooker(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        p.set((int) motionEvent.getX(), (int) motionEvent.getY());
        q.set(motionEvent.getX(), motionEvent.getY());
        if (actionMasked == 5 && motionEvent.getPointerCount() == 2 && this.t == null) {
            actionMasked = 3;
        }
        switch (actionMasked) {
            case 0:
                if (this.t != null) {
                    a pageViewAtPoint = getPageViewAtPoint(p);
                    if (pageViewAtPoint != null) {
                        r.set(q);
                        pageViewAtPoint.b(r);
                        if (this.t.onTouchEvent(pageViewAtPoint.t(), motionEvent)) {
                            this.n = 2;
                            this.o = pageViewAtPoint;
                            return true;
                        }
                    }
                }
                if (a(motionEvent)) {
                    this.n = 1;
                    return true;
                }
                break;
            default:
                if (this.n != 0) {
                    if (this.n == 1) {
                        a(motionEvent);
                    } else if (this.n == 2 && this.o != null) {
                        r.set(q);
                        this.o.b(r);
                        if (this.t != null) {
                            this.t.onTouchEvent(this.o.t(), motionEvent);
                        }
                    }
                    if (actionMasked != 1 && actionMasked != 3) {
                        return true;
                    }
                    this.n = 0;
                    this.o = null;
                    return true;
                }
                break;
        }
        return false;
    }

    protected boolean onTouchEventForDefault(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        switch (actionMasked) {
            case 0:
                if (this.c.p == 4 || this.c.p == 5) {
                    q();
                }
                this.c.t = true;
                this.c.p = 0;
                this.c.y = 0;
                this.c.A = motionEvent.getPointerCount() > 1;
                if (this.f.isFinished()) {
                    this.c.B = false;
                } else {
                    this.f.forceFinished(true);
                    this.c.B = true;
                }
                this.c.o = true;
                this.I = 0;
                this.J = 0;
                break;
        }
        this.h.onTouchEvent(motionEvent);
        if (this.c.y != 12) {
            this.g.onTouchEvent(motionEvent);
        }
        switch (actionMasked) {
            case 1:
            case 3:
                if (this.l != null) {
                    this.l.c();
                    if (this.c.p == 0 || this.c.p == 11) {
                        this.c.p = 0;
                        post(new h(this));
                    } else if (this.c.p == 4 || this.c.p == 5 || this.c.p == 2) {
                        _setLayerType(2);
                        post(new h(this));
                    } else if (this.c.p == 1 || this.c.p == 3) {
                        _setLayerType(2);
                    }
                    this.c.t = false;
                    this.c.y = 0;
                    this.c.z = false;
                    this.c.A = false;
                    this.c.B = false;
                    break;
                }
                break;
        }
        return true;
    }

    protected boolean onLongPressForHooker(MotionEvent motionEvent) {
        p.set((int) motionEvent.getX(), (int) motionEvent.getY());
        q.set(motionEvent.getX(), motionEvent.getY());
        if (!(this.t == null || getPageViewAtPoint(p) == null)) {
            this.t.onLongPress(motionEvent);
        }
        return false;
    }

    protected boolean onLongPressForDefault(MotionEvent motionEvent) {
        Iterator it = this.A.iterator();
        while (it.hasNext()) {
            ((IGestureEventListener) it.next()).onLongPress(motionEvent);
        }
        return true;
    }

    protected boolean onSingleTapForHooker(MotionEvent motionEvent) {
        p.set((int) motionEvent.getX(), (int) motionEvent.getY());
        q.set(motionEvent.getX(), motionEvent.getY());
        if ((this.t == null || getPageViewAtPoint(p) == null || !this.t.onSingleTapConfirmed(motionEvent)) && !b(motionEvent)) {
            return false;
        }
        return true;
    }

    protected boolean onSingleTapForDefault(MotionEvent motionEvent) {
        float width = ((float) getWidth()) * 0.8f;
        a aVar;
        if (((float) p.x) < ((float) getWidth()) * 0.2f) {
            if (this.c.c > 0 && this.f.isFinished()) {
                aVar = (a) this.j.get(this.c.c - 1);
                if (aVar != null) {
                    slideViewOntoScreen(aVar);
                }
            }
        } else if (((float) p.x) > width && this.c.c < getPageCount() - 1 && this.f.isFinished()) {
            aVar = (a) this.j.get(this.c.c + 1);
            if (aVar != null) {
                slideViewOntoScreen(aVar);
            }
        }
        return true;
    }

    public boolean onDown(MotionEvent motionEvent) {
        if (this.t == null || !this.t.onDown(motionEvent)) {
            Iterator it = this.A.iterator();
            while (it.hasNext()) {
                if (((IGestureEventListener) it.next()).onDown(motionEvent)) {
                    break;
                }
            }
        }
        return true;
    }

    public void onShowPress(MotionEvent motionEvent) {
        if (this.t != null) {
            this.t.onShowPress(motionEvent);
        }
        Iterator it = this.A.iterator();
        while (it.hasNext()) {
            ((IGestureEventListener) it.next()).onShowPress(motionEvent);
        }
    }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        if (this.t != null && this.t.onSingleTapUp(motionEvent)) {
            return true;
        }
        Iterator it = this.A.iterator();
        while (it.hasNext()) {
            if (((IGestureEventListener) it.next()).onSingleTapUp(motionEvent)) {
                return true;
            }
        }
        return false;
    }

    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        if (this.a == null || !isDocumentOpened()) {
            return true;
        }
        p();
        if (this.t != null && this.t.onScroll(motionEvent, motionEvent2, f, f2)) {
            return true;
        }
        Iterator it = this.A.iterator();
        while (it.hasNext()) {
            if (((IGestureEventListener) it.next()).onScroll(motionEvent, motionEvent2, f, f2)) {
                return true;
            }
        }
        if (this.c.z) {
            return true;
        }
        if (!(f == 0.0f && f2 == 0.0f)) {
            this.I = (int) (((float) this.I) - f);
            this.J = (int) (((float) this.J) - f2);
            this.L = (int) (((float) this.L) - f2);
            if (this.c.o) {
                switch (a((float) this.I, (float) this.J)) {
                    case 3:
                    case 4:
                        break;
                    default:
                        this.c.o = false;
                        break;
                }
            }
            this.K = (int) (((float) this.K) - f);
            if (this.l == null) {
                return false;
            }
            this.l.a(f, f2);
            if (!(this.K == 0 && this.L == 0)) {
                _layoutPages();
            }
        }
        this.c.y = 11;
        return true;
    }

    public void onLongPress(MotionEvent motionEvent) {
        if (this.a != null && isDocumentOpened() && motionEvent.getPointerCount() == 1 && !this.c.A && !this.c.B && !onLongPressForHooker(motionEvent)) {
            onLongPressForDefault(motionEvent);
        }
    }

    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        if (this.a == null || !isDocumentOpened()) {
            return true;
        }
        p();
        if (this.t != null && this.t.onFling(motionEvent, motionEvent2, f, f2)) {
            return true;
        }
        Iterator it = this.A.iterator();
        while (it.hasNext()) {
            if (((IGestureEventListener) it.next()).onFling(motionEvent, motionEvent2, f, f2)) {
                return true;
            }
        }
        if (this.c.z) {
            return true;
        }
        if (this.l == null) {
            return false;
        }
        if (this.l.b(f, f2)) {
            this.mScrollLastY = 0;
            this.mScrollLastX = 0;
        }
        return true;
    }

    private void m() {
        if (this.a != null && isDocumentOpened()) {
            int currX;
            int currY;
            if (!this.f.isFinished()) {
                this.f.computeScrollOffset();
                currX = this.f.getCurrX();
                currY = this.f.getCurrY();
                this.K += currX - this.mScrollLastX;
                this.L += currY - this.mScrollLastY;
                this.mScrollLastX = currX;
                this.mScrollLastY = currY;
                if (!(this.K == 0 && this.L == 0)) {
                    _layoutPages();
                }
                post(new h(this));
            } else if (!this.c.t) {
                a aVar;
                if (this.c.p == 4 || this.c.p == 5) {
                    aVar = (a) this.j.get(this.c.c);
                    if (aVar != null) {
                        float f = this.c.j;
                        float f2 = 1.3f;
                        if (this.c.p == 5) {
                            f2 = 0.7692308f;
                        }
                        f2 = Math.min(4.0f, Math.max(1.0f, f2 * f));
                        if (this.c.p == 4 && f > 4.0f) {
                            f2 = 4.0f;
                        }
                        this.l.a(f2);
                        f2 = this.c.j / f;
                        int m = ((int) this.M.x) - (aVar.m() + this.K);
                        currX = ((int) this.M.y) - (aVar.n() + this.L);
                        this.K = (int) ((((float) m) - (((float) m) * f2)) + ((float) this.K));
                        this.L = (int) ((((float) currX) - (((float) currX) * f2)) + ((float) this.L));
                        _layoutPages();
                        if (1.0f >= this.c.j || this.c.j >= 4.0f) {
                            this.c.p = 0;
                            _setLayerType(1);
                            this.a.b();
                            q();
                            return;
                        }
                        post(new h(this));
                        return;
                    }
                    this.c.p = 0;
                    _setLayerType(1);
                    this.a.b();
                    return;
                }
                this.c.p = 0;
                _setLayerType(1);
                this.a.b();
                _layoutPages();
                Rect rect = new Rect(0, 0, getWidth(), getHeight());
                for (currY = this.j.size() - 1; currY >= 0; currY--) {
                    aVar = (a) this.j.valueAt(currY);
                    if (Rect.intersects(aVar.c, rect)) {
                        aVar.w();
                    } else if (this.c.r == 1 && !this.c.q) {
                        aVar.a(aVar.j().x, aVar.j().y);
                        aVar.x();
                    }
                }
            }
        }
    }

    public void addTask(Task task) {
        if (this.a != null) {
            this.a.a(task);
        }
    }

    public void removeTask(Task task) {
        if (this.a != null) {
            this.a.b(task);
        }
    }

    private void n() {
        this.E = this.b.getResources().getDisplayMetrics();
        Display defaultDisplay = ((WindowManager) this.b.getSystemService("window")).getDefaultDisplay();
        if (VERSION.SDK_INT < 13) {
            this.F = this.E.widthPixels;
            this.G = this.E.heightPixels;
        } else if (VERSION.SDK_INT == 13) {
            try {
                r0 = Display.class.getMethod("getRealWidth", new Class[0]);
                r2 = Display.class.getMethod("getRealHeight", new Class[0]);
                this.F = ((Integer) r0.invoke(defaultDisplay, new Object[0])).intValue();
                this.G = ((Integer) r2.invoke(defaultDisplay, new Object[0])).intValue();
            } catch (Exception e) {
                this.F = this.E.widthPixels;
                this.G = this.E.heightPixels;
            }
        } else if (VERSION.SDK_INT > 13 && VERSION.SDK_INT < 17) {
            try {
                r0 = Display.class.getMethod("getRawWidth", new Class[0]);
                r2 = Display.class.getMethod("getRawHeight", new Class[0]);
                this.F = ((Integer) r0.invoke(defaultDisplay, new Object[0])).intValue();
                this.G = ((Integer) r2.invoke(defaultDisplay, new Object[0])).intValue();
            } catch (Exception e2) {
                this.F = this.E.widthPixels;
                this.G = this.E.heightPixels;
            }
        } else if (VERSION.SDK_INT >= 17) {
            defaultDisplay.getRealMetrics(this.E);
            this.F = this.E.widthPixels;
            this.G = this.E.heightPixels;
        }
        float sqrt = ((float) Math.sqrt(Math.pow((double) getRawScreenWidth(), 2.0d) + Math.pow((double) getRawScreenHeight(), 2.0d))) / ((float) this.E.densityDpi);
        if (sqrt < 7.0f) {
            this.H = false;
        } else if (sqrt < 7.0f || sqrt >= 8.0f || this.E.densityDpi >= ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT) {
            this.H = true;
        } else {
            this.H = false;
        }
    }

    protected int getScreenWidth() {
        return this.E.widthPixels;
    }

    protected int getScreenHeight() {
        return this.E.heightPixels;
    }

    protected boolean isLandscape() {
        if (getScreenWidth() > getScreenHeight()) {
            return true;
        }
        return false;
    }

    protected int getRawScreenWidth() {
        if (isLandscape()) {
            return Math.max(this.F, this.G);
        }
        return Math.min(this.F, this.G);
    }

    protected int getRawScreenHeight() {
        if (isLandscape()) {
            return Math.min(this.F, this.G);
        }
        return Math.max(this.F, this.G);
    }

    protected boolean isPad() {
        return this.H;
    }

    private Point a(Rect rect) {
        return new Point(Math.min(Math.max(0, rect.left), rect.right), Math.min(Math.max(0, rect.top), rect.bottom));
    }

    protected int bottomGap(a aVar) {
        return a(aVar.t());
    }

    int a(int i) {
        if (i == getPageCount() - 1 || this.c.r == 1) {
            return 0;
        }
        return 0 + this.c.k;
    }

    protected void slideViewOntoScreen(a aVar) {
        int i = 1;
        if (this.c.r != 1 || aVar.s() + (bottomGap(aVar) * 2) <= getHeight()) {
            i = 0;
        }
        Point a = a(a(aVar.m() + this.K, aVar.n() + this.L, aVar.o() + this.K, (i != 0 ? bottomGap(aVar) : 0) + (aVar.p() + this.L)));
        if (a.x != 0 || a.y != 0) {
            this.mScrollLastY = 0;
            this.mScrollLastX = 0;
            this.f.startScroll(0, 0, a.x, a.y, HttpStatus.SC_BAD_REQUEST);
            this.c.p = 3;
            post(new h(this));
        }
    }

    protected void slideToPreviousPage() {
        a aVar = (a) this.j.get(this.c.c - 1);
        if (aVar != null) {
            slideViewOntoScreen(aVar);
        }
    }

    protected void slideToNextPage() {
        a aVar = (a) this.j.get(this.c.c + 1);
        if (aVar != null) {
            slideViewOntoScreen(aVar);
        }
    }

    private int a(float f, float f2) {
        if (Math.abs(f) > Math.abs(f2) * 2.0f) {
            return f > 0.0f ? 2 : 1;
        } else {
            if (Math.abs(f2) > Math.abs(f) * 2.0f) {
                return f2 > 0.0f ? 4 : 3;
            } else {
                return 0;
            }
        }
    }

    private boolean a(Rect rect, float f, float f2) {
        switch (a(f, f2)) {
            case 0:
                return rect.contains(0, 0);
            case 1:
                if (rect.left > 0) {
                    return false;
                }
                return true;
            case 2:
                if (rect.right < 0) {
                    return false;
                }
                return true;
            case 3:
                if (rect.top > 0) {
                    return false;
                }
                return true;
            case 4:
                return rect.bottom >= 0;
            default:
                return false;
        }
    }

    private Point a(a aVar) {
        return new Point(Math.min(getWidth() - aVar.r(), (getWidth() - aVar.r()) / 2), Math.min(getHeight() - aVar.s(), (getHeight() - aVar.s()) / 2));
    }

    private Point b(a aVar) {
        return new Point(Math.max((getWidth() - aVar.r()) / 2, 0), Math.max((getHeight() - aVar.s()) / 2, 0));
    }

    private a getRecycled() {
        if (this.k.size() > 0) {
            return (a) this.k.removeFirst();
        }
        return null;
    }

    private a k(int i) {
        a aVar = (a) this.j.get(i);
        if (aVar != null) {
            return aVar;
        }
        aVar = b(i, getRecycled());
        a(i, aVar);
        return aVar;
    }

    private void a(int i, a aVar) {
        this.j.append(i, aVar);
        this.l.a(aVar);
    }

    private a b(int i, a aVar) {
        if (aVar == null) {
            aVar = o();
        }
        aVar.a(i);
        aVar.h = i;
        PointF a = this.a.a(i, aVar);
        if (a != null) {
            aVar.a(i, this.a.b(i), a);
        } else {
            if (a == null) {
                a = getPageSize(i);
            }
            if (a == null) {
                u uVar = this.c;
                float f = (float) u.a;
                u uVar2 = this.c;
                a = new PointF(f, (float) u.b);
            }
            aVar.a(i, a);
        }
        return aVar;
    }

    private a o() {
        switch (this.c.r) {
            case 1:
            case 2:
                return new m(this, this.a);
            case 3:
                return new n(this, this.a);
            default:
                return null;
        }
    }

    protected void onPageLoaded(int i) {
        if (i == this.c.c) {
            a aVar = (a) this.j.get(this.c.c);
            if (aVar != null) {
                this.c.v = aVar.i().x;
                this.c.w = aVar.i().y;
                this.c.d = (float) aVar.j().x;
                this.c.e = (float) aVar.j().y;
            }
        }
    }

    protected void requestLayout(a aVar) {
        int r = aVar.r();
        int s = aVar.s();
        this.l.a(aVar);
        if (aVar.r() != r || aVar.s() != s) {
            aVar.x();
            if (this.c.y == 0 && this.j.get(aVar.t()) != null) {
                _layoutPages();
            }
        }
    }

    private void p() {
        MemoryInfo memoryInfo = new MemoryInfo();
        ((ActivityManager) this.b.getSystemService("activity")).getMemoryInfo(memoryInfo);
        if (((double) memoryInfo.availMem) < ((double) memoryInfo.threshold) * 1.5d) {
            recoverForOOM();
        }
    }

    private Rect a(int i, int i2, int i3, int i4) {
        int width = getWidth() - i3;
        int i5 = -i;
        int height = getHeight() - i4;
        int i6 = -i2;
        if (width > i5) {
            i5 = (i5 + width) / 2;
            width = i5;
        }
        if (height > i6) {
            i6 = (i6 + height) / 2;
            height = i6;
        }
        return new Rect(width, height, i5, i6);
    }

    private void q() {
        int size = this.j.size();
        for (int i = 0; i < size; i++) {
            ((a) this.j.valueAt(i)).x();
        }
    }

    private Rect c(a aVar) {
        return a(aVar.m() + this.K, aVar.n() + this.L, aVar.o() + this.K, aVar.p() + this.L);
    }
}
