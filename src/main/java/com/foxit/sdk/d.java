package com.foxit.sdk;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.SparseArray;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: DocManager */
class d {
    private static int f = 2;
    private static int g = 4;
    protected long a = 0;
    protected SparseArray<a> b;
    private s c;
    private PDFViewCtrl d = null;
    private Point e = null;
    private ArrayList<Bitmap> h = new ArrayList(8);
    private ArrayList<Bitmap> i = new ArrayList(8);

    /* compiled from: DocManager */
    static class a {
        int a;
        boolean b;
        PointF c;
        PointF d;
        Matrix e;
        ArrayList<String> f;

        protected a(int i, boolean z, PointF pointF, PointF pointF2, Matrix matrix, ArrayList<String> arrayList) {
            this.a = i;
            this.b = z;
            this.c = pointF;
            this.d = pointF2;
            this.e = matrix;
            this.f = arrayList;
        }
    }

    protected d(PDFViewCtrl pDFViewCtrl) {
        this.d = pDFViewCtrl;
        this.c = new s(pDFViewCtrl);
        this.b = new SparseArray();
        int rawScreenWidth = ((this.d.getRawScreenWidth() + 512) - 1) / 512;
        g = ((this.d.getRawScreenHeight() + 512) - 1) / 512;
        g = rawScreenWidth * g;
    }

    protected Point a(int i) {
        if (this.e == null) {
            this.e = this.d.getScreenSize();
        }
        PointF pageSize = this.d.getPageSize(i);
        if (pageSize == null) {
            pageSize = new PointF((float) u.a, (float) u.b);
        }
        float max = Math.max(Math.min(((float) this.e.x) / pageSize.x, ((float) this.e.y) / pageSize.y), Math.min(((float) this.e.y) / pageSize.x, ((float) this.e.x) / pageSize.y));
        c.c = new Point();
        c.c.x = (int) (pageSize.x * max);
        c.c.y = (int) (pageSize.y * max);
        return c.c;
    }

    public SparseArray<a> a() {
        return this.b;
    }

    protected Task a(Task task) {
        return this.c.a(task);
    }

    protected void b(Task task) {
        this.c.b(task);
    }

    protected void b() {
        this.c.c();
    }

    protected void a(boolean z) {
        this.c.a(z);
    }

    protected void c() {
        if (this.b != null) {
            this.b.clear();
        }
    }

    protected PDFViewCtrl d() {
        return this.d;
    }

    protected void a(String str, byte[] bArr, final q<PDFDoc, Integer, Integer> qVar) {
        Task hVar = new h(this, str, bArr, new CallBack(this) {
            final /* synthetic */ d b;

            public void result(Task task) {
                h hVar = (h) task;
                if (qVar != null) {
                    qVar.a(hVar.exeSuccess(), hVar.a(), Integer.valueOf(hVar.errorCode()), Integer.valueOf(hVar.extErrorCode()));
                }
            }
        });
        this.c.a();
        a(hVar);
    }

    protected void a(byte[] bArr, byte[] bArr2, final q<PDFDoc, Integer, Integer> qVar) {
        Task hVar = new h(this, bArr, bArr2, new CallBack(this) {
            final /* synthetic */ d b;

            public void result(Task task) {
                h hVar = (h) task;
                if (qVar != null) {
                    qVar.a(hVar.exeSuccess(), hVar.a(), Integer.valueOf(hVar.errorCode()), Integer.valueOf(hVar.extErrorCode()));
                }
            }
        });
        this.c.a();
        a(hVar);
    }

    protected void a(PDFDoc pDFDoc, final q<PDFDoc, Integer, Integer> qVar) {
        Task bVar = new b(this, pDFDoc, new CallBack(this) {
            final /* synthetic */ d b;

            public void result(Task task) {
                b bVar = (b) task;
                this.b.d.getViewStatus().i = false;
                this.b.b.clear();
                if (qVar != null) {
                    qVar.a(bVar.exeSuccess(), bVar.a(), Integer.valueOf(bVar.errorCode()), Integer.valueOf(bVar.extErrorCode()));
                }
            }
        });
        this.c.a(true);
        a(bVar);
        this.c.b();
    }

    protected void a(PDFDoc pDFDoc, String str, int i, final q<PDFDoc, Integer, Integer> qVar) {
        a(new o(this, pDFDoc, str, i, new CallBack(this) {
            final /* synthetic */ d b;

            public void result(Task task) {
                o oVar = (o) task;
                if (qVar != null) {
                    qVar.a(oVar.exeSuccess(), oVar.a(), Integer.valueOf(oVar.errorCode()), Integer.valueOf(oVar.extErrorCode()));
                }
            }
        }));
    }

    protected PDFDoc e() {
        return this.d.getDoc();
    }

    private void a(int i, final long j) {
        if (i >= 0 && i < this.d.getPageCount()) {
            int i2;
            int i3 = i;
            while (i3 < this.d.getPageCount()) {
                if (this.b.get(i3) == null) {
                    i2 = i3;
                    break;
                } else if (i3 != this.d.getPageCount() - 1) {
                    i3++;
                } else {
                    return;
                }
            }
            i2 = i;
            Task lVar = new l(this, e(), i2, this.d.getViewStatus().r, 0, 0, new CallBack(this) {
                final /* synthetic */ d b;

                public void result(Task task) {
                    l lVar = (l) task;
                    if (task.errorCode() == 10) {
                        this.b.d.recoverForOOM();
                    } else if (this.b.d.isDocumentOpened() && j == this.b.a && lVar.b == this.b.d.getViewStatus().r) {
                        if (lVar.exeSuccess()) {
                            this.b.b.append(lVar.a, new a(lVar.b, true, lVar.e, lVar.f, lVar.g, null));
                        }
                        if (lVar.a < this.b.d.getPageCount() - 1) {
                            this.b.a(lVar.a + 1, j);
                        }
                    }
                }
            });
            lVar.setPriority(1);
            a(lVar);
        }
    }

    protected void f() {
        if (this.d.isDocumentOpened()) {
            this.a++;
            a(0, this.a);
        }
    }

    protected PointF a(int i, a aVar) {
        a aVar2 = (a) this.b.get(i);
        if (aVar2 != null) {
            return aVar2.d;
        }
        switch (aVar.g()) {
            case 1:
                return b(i, aVar);
            case 2:
                return c(i, aVar);
            default:
                return null;
        }
    }

    protected PointF b(int i) {
        a aVar = (a) this.b.get(i);
        if (aVar != null) {
            return aVar.c;
        }
        return null;
    }

    private PointF b(int i, final a aVar) {
        a(new l(this, e(), i, this.d.getViewStatus().r, 0, 0, new CallBack(this) {
            final /* synthetic */ d b;

            public void result(Task task) {
                l lVar = (l) task;
                if (lVar.errorCode() == 10) {
                    this.b.d.recoverForOOM();
                } else if (lVar.exeSuccess() && !lVar.isCanceled() && this.b.d.isDocumentOpened() && lVar.b == this.b.d.getViewStatus().r) {
                    this.b.b.append(lVar.a, new a(lVar.b, true, lVar.e, lVar.f, lVar.g, null));
                    if (aVar.t() == lVar.a) {
                        aVar.a(lVar.a, lVar.e, lVar.f);
                    }
                }
            }
        }));
        return null;
    }

    private PointF c(int i, final a aVar) {
        Point d = aVar.d();
        a(new l(this, e(), i, this.d.getPageLayoutMode(), d.x, d.y, new CallBack(this) {
            final /* synthetic */ d b;

            public void result(Task task) {
                l lVar = (l) task;
                if (lVar.exeSuccess() && this.b.d.isDocumentOpened() && lVar.b == this.b.d.getViewStatus().r && this.b.d.getViewStatus().u == lVar.h) {
                    this.b.b.append(lVar.a, new a(lVar.b, true, lVar.e, lVar.f, lVar.g, null));
                    if (aVar.t() == lVar.a) {
                        aVar.a(lVar.a, lVar.e, lVar.f);
                    }
                }
            }
        }));
        return null;
    }

    protected Matrix a(a aVar, int i) {
        a aVar2 = (a) this.b.get(i);
        if (aVar2 == null) {
            return null;
        }
        float f = aVar2.d.x;
        float f2 = aVar2.d.y;
        if (f == 0.0f || f2 == 0.0f) {
            return null;
        }
        float pageScale;
        float f3;
        Matrix displayMatrix;
        int i2 = this.d.getViewStatus().l;
        if (aVar == null || aVar.r() <= 0 || aVar.s() <= 0) {
            float pageMatchScale = this.d.getPageMatchScale(i, f, f2);
            pageScale = this.d.getPageScale(i);
            f3 = (float) ((int) ((f2 * pageMatchScale) * pageScale));
            pageScale = (float) ((int) ((f * pageMatchScale) * pageScale));
        } else {
            f3 = (float) aVar.s();
            pageScale = (float) aVar.r();
        }
        try {
            PDFPage page = this.d.getDoc().getPage(i);
            if (!page.isParsed()) {
                int startParse = page.startParse(0, null, false);
                while (startParse != 2) {
                    startParse = page.continueParse();
                    if (startParse == 0) {
                        return null;
                    }
                }
            }
            displayMatrix = page.getDisplayMatrix(0, 0, (int) pageScale, (int) f3, i2);
        } catch (Exception e) {
            e.printStackTrace();
            displayMatrix = null;
        }
        return displayMatrix;
    }

    protected boolean a(a aVar, int i, RectF rectF) {
        if (this.d.getViewStatus().r != 1 && this.d.getViewStatus().r != 2) {
            return false;
        }
        Matrix a = a(aVar, i);
        if (a == null) {
            return false;
        }
        a.mapRect(rectF);
        return true;
    }

    protected boolean b(a aVar, int i, RectF rectF) {
        if (this.d.getViewStatus().r != 1 && this.d.getViewStatus().r != 2) {
            return false;
        }
        Matrix a = a(aVar, i);
        if (a == null) {
            return false;
        }
        Matrix matrix = new Matrix();
        a.invert(matrix);
        matrix.mapRect(rectF);
        float f = rectF.top;
        rectF.top = rectF.bottom;
        rectF.bottom = f;
        return true;
    }

    protected boolean a(a aVar, int i, PointF pointF) {
        if (this.d.getViewStatus().r != 1 && this.d.getViewStatus().r != 2) {
            return false;
        }
        Matrix a = a(aVar, i);
        if (a == null) {
            return false;
        }
        Matrix matrix = new Matrix();
        a.invert(matrix);
        float[] fArr = new float[]{pointF.x, pointF.y};
        matrix.mapPoints(fArr);
        pointF.x = fArr[0];
        pointF.y = fArr[1];
        return true;
    }

    protected boolean b(a aVar, int i, PointF pointF) {
        if (this.d.getViewStatus().r != 1 && this.d.getViewStatus().r != 2) {
            return false;
        }
        Matrix a = a(aVar, i);
        if (a == null) {
            return false;
        }
        float[] fArr = new float[]{pointF.x, pointF.y};
        a.mapPoints(fArr);
        pointF.x = fArr[0];
        pointF.y = fArr[1];
        return true;
    }

    protected Bitmap c(int i) {
        Bitmap bitmap = null;
        Point a = a(i);
        if (this.h.size() > 0) {
            bitmap = (Bitmap) this.h.remove(0);
            if (!(bitmap.getWidth() == a.x && bitmap.getHeight() == a.y)) {
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            return bitmap;
        }
        try {
            bitmap = Bitmap.createBitmap(a.x, a.y, Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            this.d.doForOOM();
        }
        return bitmap;
    }

    protected void a(Bitmap bitmap) {
        if (this.h.size() < f) {
            this.h.add(bitmap);
        }
    }

    protected Bitmap g() {
        Bitmap bitmap = null;
        if (this.i.size() > 0) {
            return (Bitmap) this.i.remove(0);
        }
        try {
            return Bitmap.createBitmap(512, 512, Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            this.d.doForOOM();
            return bitmap;
        }
    }

    protected void h() {
        i();
    }

    protected void i() {
        Iterator it = this.h.iterator();
        while (it.hasNext()) {
            ((Bitmap) it.next()).recycle();
        }
        it = this.i.iterator();
        while (it.hasNext()) {
            ((Bitmap) it.next()).recycle();
        }
        this.h.clear();
        this.i.clear();
    }

    protected void b(Bitmap bitmap) {
        if (this.i.size() < g) {
            this.i.add(bitmap);
        }
    }

    protected boolean a(Annot annot) {
        if (this.d.getUIExtensionsManager() != null) {
            return this.d.getUIExtensionsManager().shouldViewCtrlDraw(annot);
        }
        return true;
    }

    protected Annot j() {
        if (this.d.getUIExtensionsManager() != null) {
            return this.d.getUIExtensionsManager().getFocusAnnot();
        }
        return null;
    }
}
