package com.foxit.sdk;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.foxit.sdk.Task.CallBack;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: ThumbListView */
class t extends BaseAdapter {
    private Context a;
    private PDFViewCtrl b;
    private d c;
    private int d;
    private int e;
    private int f;
    private int g;
    private int h;
    private int i;
    private int j;
    private int k;
    private String l = "#FFE1E1E1";

    /* compiled from: ThumbListView */
    class a extends ViewGroup {
        final /* synthetic */ t a;
        private int b;
        private ArrayList<ImageView> c = new ArrayList();
        private ArrayList<Task> d = new ArrayList();

        protected a(t tVar, Context context) {
            this.a = tVar;
            super(context);
        }

        protected void a(int i) {
            removeAllViews();
            this.b = i;
            this.c.clear();
            Iterator it = this.d.iterator();
            while (it.hasNext()) {
                this.a.c.b((Task) it.next());
            }
            this.d.clear();
        }

        protected void b(int i) {
            this.b = i;
            int b = this.a.g * this.b;
            int i2 = 0;
            while (i2 < this.a.g && b + i2 < this.a.b.getPageCount()) {
                View imageView = new ImageView(this.a.a);
                imageView.setBackgroundColor(-1);
                this.c.add(imageView);
                addView(imageView);
                imageView.setOnClickListener(new OnClickListener(this) {
                    final /* synthetic */ a b;

                    public void onClick(View view) {
                        int b = (this.b.a.g * this.b.b) + i2;
                        this.b.a.b.gotoPage(b, 0.0f, 0.0f);
                        if (this.b.a.b.getThumbnailView().mClickListener != null) {
                            this.b.a.b.getThumbnailView().mClickListener.onClick(b);
                        }
                    }
                });
                Task fVar = new f(this.a.c, this.a.c.e(), b + i2, 1, 0, 0, new Rect(0, 0, this.a.e, this.a.f), new Point(this.a.e, this.a.f), 8, 0, this.a.b.getViewStatus().C, new CallBack(this) {
                    final /* synthetic */ a a;

                    {
                        this.a = r1;
                    }

                    public void result(Task task) {
                        if (task.errorCode() == 10) {
                            this.a.a.b.recoverForOOM();
                        }
                        this.a.d.remove(task);
                        if (this.a.b >= 0) {
                            f fVar = (f) task;
                            if (fVar.exeSuccess() && !fVar.isCanceled()) {
                                int b = fVar.a - (this.a.a.g * this.a.b);
                                if (b >= 0 && b < this.a.c.size()) {
                                    ((ImageView) this.a.c.get(b)).setImageBitmap(fVar.h);
                                    this.a.invalidate();
                                }
                            }
                        }
                    }
                });
                this.a.c.a(fVar);
                this.d.add(fVar);
                i2++;
            }
        }

        protected void onMeasure(int i, int i2) {
            int width;
            int f;
            switch (MeasureSpec.getMode(i)) {
                case 0:
                    width = this.a.b.getWidth();
                    break;
                default:
                    width = MeasureSpec.getSize(i);
                    break;
            }
            switch (MeasureSpec.getMode(i2)) {
                case 0:
                    f = this.a.f + (this.a.j * 2);
                    break;
                default:
                    f = MeasureSpec.getSize(i2);
                    break;
            }
            setMeasuredDimension(width, f);
        }

        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            for (int size = this.c.size() - 1; size >= 0; size--) {
                ((ImageView) this.c.get(size)).layout(this.a.k + ((this.a.k + this.a.e) * size), this.a.j, (this.a.k + ((this.a.k + this.a.e) * size)) + this.a.e, this.a.j + this.a.f);
            }
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            return super.onTouchEvent(motionEvent);
        }
    }

    protected t(Context context, PDFViewCtrl pDFViewCtrl, d dVar) {
        this.a = context;
        this.b = pDFViewCtrl;
        this.c = dVar;
        this.d = this.b.getWidth();
        this.g = 1;
        this.h = 0;
        this.i = 10;
        this.j = 10;
        this.k = 10;
        float f = (float) this.a.getResources().getDisplayMetrics().densityDpi;
        if (f == 0.0f) {
            f = 240.0f;
        }
        this.e = (int) ((f / 5.0f) * 3.5f);
        this.f = (int) ((f / 5.0f) * 5.0f);
    }

    protected void a(int i, int i2) {
        this.d = i;
    }

    public int getCount() {
        int pageCount = this.b.getPageCount();
        this.g = this.d / (this.e + this.i);
        this.h = ((pageCount + this.g) - 1) / this.g;
        this.k = (this.d - (this.g * this.e)) / (this.g + 1);
        return this.h;
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return 0;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        view = (a) view;
        if (view == null) {
            view = new a(this, this.a);
            view.setMinimumWidth(viewGroup.getWidth());
            view.setMinimumHeight(this.f + (this.j * 2));
        }
        view.a(i);
        view.b(i);
        view.setBackgroundColor(Color.parseColor(this.l));
        return view;
    }
}
