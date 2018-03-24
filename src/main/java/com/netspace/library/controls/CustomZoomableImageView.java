package com.netspace.library.controls;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.netspace.pad.library.R;

public class CustomZoomableImageView extends FrameLayout implements OnTouchListener {
    private GestureDetector mGestureDetector;
    private ImageView mImageView;
    private RelativeLayout mRelativeLayout;
    private float mfCurrentScale = 1.0f;
    private float mfLastDistance = 0.0f;
    private float mfLastScale = 1.0f;
    private int mnFullScaleHeight = 0;
    private int mnFullScaleWidth = 0;
    private int mnLastXPos = 0;
    private int mnLastYPos = 0;
    private PointF mptTouchStart;

    public CustomZoomableImageView(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customzoomableimageview, this);
        this.mImageView = (ImageView) findViewById(R.id.ImageView);
        this.mRelativeLayout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        this.mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                CustomZoomableImageView.this.doBestFit();
                return super.onDoubleTap(e);
            }
        });
        this.mRelativeLayout.setOnTouchListener(this);
    }

    public ImageView getImageView() {
        return this.mImageView;
    }

    private void getMidPoint(PointF point, MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            point.set((event.getX(0) + event.getX(1)) / 2.0f, (event.getY(0) + event.getY(1)) / 2.0f);
        }
    }

    private float getPointSpacing(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0.0f;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    public void doBestFit() {
        Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
        int nDisplayWidth = getWidth();
        int nDisplayHeight = getHeight();
        float fScale = 1.0f / Math.max(((float) this.mnFullScaleWidth) / ((float) nDisplayWidth), ((float) this.mnFullScaleHeight) / ((float) nDisplayHeight));
        LayoutParams Param = (LayoutParams) this.mImageView.getLayoutParams();
        int i = (int) ((((float) nDisplayHeight) - (((float) this.mnFullScaleHeight) * fScale)) / 2.0f);
        Param.bottomMargin = i;
        Param.topMargin = i;
        i = (int) ((((float) nDisplayWidth) - (((float) this.mnFullScaleWidth) * fScale)) / 2.0f);
        Param.rightMargin = i;
        Param.leftMargin = i;
        this.mImageView.setLayoutParams(Param);
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (this.mnFullScaleWidth == 0 || this.mnFullScaleHeight == 0) {
            Drawable drawable = this.mImageView.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) {
                    this.mnFullScaleWidth = bitmapDrawable.getBitmap().getWidth();
                    this.mnFullScaleHeight = bitmapDrawable.getBitmap().getHeight();
                }
            }
        }
        if (this.mnFullScaleWidth == 0 || this.mnFullScaleHeight == 0) {
            return false;
        }
        if (this.mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        if (event.getPointerCount() >= 2) {
            LayoutParams Param = (LayoutParams) this.mImageView.getLayoutParams();
            Param.topMargin--;
            this.mImageView.setLayoutParams(Param);
        }
        switch (event.getActionMasked()) {
            case 1:
            case 6:
                this.mptTouchStart = null;
                break;
            case 2:
                if (this.mptTouchStart != null) {
                    PointF CurrentCenterPoint = new PointF();
                    getMidPoint(CurrentCenterPoint, event);
                    Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
                    float fCurrentDistance = getPointSpacing(event);
                    float fScale = (fCurrentDistance - this.mfLastDistance) / fCurrentDistance;
                    float fTotalScale = fScale + this.mfLastScale;
                    if (((double) fTotalScale) < 0.3d) {
                        this.mfCurrentScale = 0.3f;
                    } else if (fTotalScale > 3.0f) {
                        this.mfCurrentScale = 3.0f;
                    } else {
                        this.mfCurrentScale = this.mfLastScale + fScale;
                    }
                    int nNewLeftMargin = (int) (CurrentCenterPoint.x - (((float) this.mnLastXPos) * this.mfCurrentScale));
                    LayoutParams Param2 = (LayoutParams) this.mImageView.getLayoutParams();
                    Param2.topMargin = (int) (CurrentCenterPoint.y - (((float) this.mnLastYPos) * this.mfCurrentScale));
                    Param2.leftMargin = nNewLeftMargin;
                    Param2.rightMargin = (int) (((((float) this.mnFullScaleWidth) * this.mfCurrentScale) - ((float) getWidth())) + ((float) Param2.leftMargin));
                    Param2.bottomMargin = (int) (((((float) this.mnFullScaleHeight) * this.mfCurrentScale) - ((float) getHeight())) + ((float) Param2.topMargin));
                    Param2.width = (int) (((float) this.mnFullScaleWidth) * this.mfCurrentScale);
                    Param2.height = (int) (((float) this.mnFullScaleHeight) * this.mfCurrentScale);
                    this.mImageView.setLayoutParams(Param2);
                    break;
                }
                break;
            case 5:
                if (this.mptTouchStart == null) {
                    this.mptTouchStart = new PointF();
                }
                getMidPoint(this.mptTouchStart, event);
                Param = (LayoutParams) this.mImageView.getLayoutParams();
                this.mfLastDistance = getPointSpacing(event);
                this.mfLastScale = this.mfCurrentScale;
                this.mnLastXPos = (int) ((this.mptTouchStart.x - ((float) Param.leftMargin)) / this.mfLastScale);
                this.mnLastYPos = (int) ((this.mptTouchStart.y - ((float) Param.topMargin)) / this.mfLastScale);
                break;
        }
        return true;
    }
}
