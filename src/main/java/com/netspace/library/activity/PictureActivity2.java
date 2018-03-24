package com.netspace.library.activity;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Base64InputStream;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import org.apache.http.protocol.HTTP;

public class PictureActivity2 extends BaseActivity implements OnTouchListener {
    static final int DRAG = 1;
    static final float MAX_SCALE = 4.0f;
    static final int NONE = 0;
    static final int ZOOM = 2;
    private static String mBase64Image = null;
    float dist = 1.0f;
    DisplayMetrics dm;
    ImageView imgView;
    Bitmap m_Bitmap;
    Matrix matrix = new Matrix();
    PointF mid = new PointF();
    float minScaleR;
    int mode = 0;
    PointF prev = new PointF();
    Matrix savedMatrix = new Matrix();

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(1);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_picture2);
        this.imgView = (ImageView) findViewById(R.id.pictureView);
        if (getIntent().getExtras() != null) {
            String szURL = getIntent().getExtras().getString(StudentAnswerImageService.LISTURL);
            if (szURL == null || szURL.isEmpty()) {
                this.m_Bitmap = getBase64Bitmap(getIntent().getExtras().getString("base64"));
            } else {
                this.m_Bitmap = getLocalBitmap(szURL);
            }
        }
        if (mBase64Image != null) {
            this.m_Bitmap = getBase64Bitmap(mBase64Image);
            mBase64Image = null;
        }
        if (this.m_Bitmap == null) {
            new Builder(this).setTitle("无法打开").setMessage("无法打开对应的图片，图片数据解码错误。").setPositiveButton("确定", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PictureActivity2.this.finish();
                }
            }).setCancelable(false).show();
            return;
        }
        int nScreenWidth = Utilities.getScreenWidth((Context) this) * 2;
        if (this.m_Bitmap.getWidth() > nScreenWidth) {
            Bitmap scaled = Bitmap.createScaledBitmap(this.m_Bitmap, nScreenWidth, (int) (((float) this.m_Bitmap.getHeight()) * (((float) nScreenWidth) / ((float) this.m_Bitmap.getWidth()))), true);
            this.m_Bitmap.recycle();
            this.m_Bitmap = scaled;
        }
        PlaceBitmap();
    }

    public static void setBase64Bitmap(String szBase64Data) {
        mBase64Image = szBase64Data;
    }

    public void PlaceBitmap() {
        this.imgView.setImageBitmap(this.m_Bitmap);
        this.imgView.setOnTouchListener(this);
        this.dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(this.dm);
        minZoom();
        center();
        this.imgView.setImageMatrix(this.matrix);
    }

    public boolean setImageBase64(String szBase64) {
        Bitmap Bitmap = getBase64Bitmap(szBase64);
        if (Bitmap == null) {
            return false;
        }
        this.m_Bitmap = Bitmap;
        PlaceBitmap();
        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
        this.imgView.setImageBitmap(null);
        if (this.m_Bitmap != null) {
            this.m_Bitmap.recycle();
            this.m_Bitmap = null;
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & 255) {
            case 0:
                this.savedMatrix.set(this.matrix);
                this.prev.set(event.getX(), event.getY());
                this.mode = 1;
                break;
            case 1:
            case 6:
                this.mode = 0;
                break;
            case 2:
                if (this.mode != 1) {
                    if (this.mode == 2) {
                        float newDist = spacing(event);
                        if (newDist > 10.0f) {
                            this.matrix.set(this.savedMatrix);
                            float tScale = newDist / this.dist;
                            this.matrix.postScale(tScale, tScale, this.mid.x, this.mid.y);
                            break;
                        }
                    }
                }
                this.matrix.set(this.savedMatrix);
                this.matrix.postTranslate(event.getX() - this.prev.x, event.getY() - this.prev.y);
                break;
                break;
            case 5:
                this.dist = spacing(event);
                if (spacing(event) > 10.0f) {
                    this.savedMatrix.set(this.matrix);
                    midPoint(this.mid, event);
                    this.mode = 2;
                    break;
                }
                break;
        }
        this.imgView.setImageMatrix(this.matrix);
        CheckView();
        return true;
    }

    private void CheckView() {
        float[] p = new float[9];
        this.matrix.getValues(p);
        if (this.mode == 2) {
            if (p[0] < this.minScaleR) {
                this.matrix.setScale(this.minScaleR, this.minScaleR);
            }
            if (p[0] > MAX_SCALE) {
                this.matrix.set(this.savedMatrix);
            }
        }
        if (this.m_Bitmap != null) {
            center();
        }
    }

    private void minZoom() {
        this.minScaleR = Math.min(((float) this.dm.widthPixels) / ((float) this.m_Bitmap.getWidth()), ((float) this.dm.heightPixels) / ((float) this.m_Bitmap.getHeight()));
        if (((double) this.minScaleR) < 1.0d) {
            this.matrix.postScale(this.minScaleR, this.minScaleR);
        }
    }

    private void center() {
        center(true, true);
    }

    protected void center(boolean horizontal, boolean vertical) {
        Matrix m = new Matrix();
        m.set(this.matrix);
        RectF rect = new RectF(0.0f, 0.0f, (float) this.m_Bitmap.getWidth(), (float) this.m_Bitmap.getHeight());
        m.mapRect(rect);
        float height = rect.height();
        float width = rect.width();
        float deltaX = 0.0f;
        float deltaY = 0.0f;
        if (vertical) {
            int screenHeight = this.dm.heightPixels;
            if (height < ((float) screenHeight)) {
                deltaY = ((((float) screenHeight) - height) / 2.0f) - rect.top;
            } else if (rect.top > 0.0f) {
                deltaY = -rect.top;
            } else if (rect.bottom < ((float) screenHeight)) {
                deltaY = ((float) this.imgView.getHeight()) - rect.bottom;
            }
        }
        if (horizontal) {
            int screenWidth = this.dm.widthPixels;
            if (width < ((float) screenWidth)) {
                deltaX = ((((float) screenWidth) - width) / 2.0f) - rect.left;
            } else if (rect.left > 0.0f) {
                deltaX = -rect.left;
            } else if (rect.right < ((float) screenWidth)) {
                deltaX = ((float) screenWidth) - rect.right;
            }
        }
        this.matrix.postTranslate(deltaX, deltaY);
    }

    private float spacing(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0.0f;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    private void midPoint(PointF point, MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            point.set((event.getX(0) + event.getX(1)) / 2.0f, (event.getY(0) + event.getY(1)) / 2.0f);
        }
    }

    public static Bitmap getLocalBitmap(String url) {
        try {
            return BitmapFactory.decodeStream(new FileInputStream(url));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBase64Bitmap(String szBase64) {
        try {
            return BitmapFactory.decodeStream(new Base64InputStream(new ByteArrayInputStream(szBase64.getBytes(HTTP.UTF_8)), 0));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
