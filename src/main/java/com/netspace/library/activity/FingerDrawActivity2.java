package com.netspace.library.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.netspace.library.activity.plugins.ActivityPluginBase;
import com.netspace.library.activity.plugins.ActivityPlugin_AnswerSheetOtherQuestions;
import com.netspace.library.activity.plugins.ActivityPlugin_BasicDraw;
import com.netspace.library.activity.plugins.ActivityPlugin_PanZoom;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.controls.LockableScrollView.InterceptHoverEvent;
import com.netspace.library.controls.LockableScrollView.InterceptTouchEvent;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.http.HttpStatus;

public class FingerDrawActivity2 extends BaseActivity {
    protected static final String TAG = "FingerDrawActivity2";
    private static int mnDefaultColor = -16777216;
    private ImageView mBlueToothPointer;
    private DrawView mDrawView;
    private Runnable mFadeToolbarRunnable = new Runnable() {
        public void run() {
            FingerDrawActivity2.this.mToolsScrollView.setAlpha(0.5f);
        }
    };
    private ActivityPlugin_BasicDraw mPluginBasicDraw;
    private ActivityPlugin_PanZoom mPluginPanZoom;
    private TextView mQuestionIndex;
    private RelativeLayout mRelativeLayout;
    private TextView mTextInputView;
    private LinearLayout mToolsLayout;
    private LockableScrollView mToolsScrollView;
    private ArrayList<ActivityPluginBase> marrPlugins = new ArrayList();
    private int mnFullImageHeight = 0;
    private int mnFullImageWidth = 0;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.setFullScreenWindow(getWindow());
        Utilities.setKeepScreenOn(getWindow());
        setContentView(R.layout.activity_fingerdraw2);
        this.mRelativeLayout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        this.mToolsScrollView = (LockableScrollView) findViewById(R.id.scrollView1);
        this.mToolsLayout = (LinearLayout) findViewById(R.id.tools);
        this.mToolsScrollView.setOnInterceptHoverEventListener(new InterceptHoverEvent() {
            public boolean onInterceptHoverEvent(MotionEvent event) {
                FingerDrawActivity2.this.makeToolsbarVisible();
                return false;
            }
        });
        this.mToolsScrollView.setOnInterceptTouchEventListener(new InterceptTouchEvent() {
            public boolean onInterceptTouchEvent(MotionEvent event) {
                FingerDrawActivity2.this.makeToolsbarVisible();
                return false;
            }
        });
        this.mToolsScrollView.setOnHoverListener(new OnHoverListener() {
            public boolean onHover(View v, MotionEvent event) {
                FingerDrawActivity2.this.makeToolsbarVisible();
                return false;
            }
        });
        this.mToolsScrollView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                FingerDrawActivity2.this.makeToolsbarVisible();
                return false;
            }
        });
        this.mBlueToothPointer = (ImageView) findViewById(R.id.imageViewPointer);
        this.mDrawView = (DrawView) findViewById(R.id.drawPad);
        this.mDrawView.setColor(mnDefaultColor);
        this.mTextInputView = (TextView) findViewById(R.id.editText);
        this.mTextInputView.setVisibility(4);
        this.mQuestionIndex = (TextView) findViewById(R.id.textViewQuestionIndex);
        this.mPluginPanZoom = new ActivityPlugin_PanZoom(this, this.mDrawView);
        this.mPluginBasicDraw = new ActivityPlugin_BasicDraw(this, this.mRelativeLayout, this.mTextInputView, this.mDrawView, this.mToolsLayout);
        this.mPluginBasicDraw.setPenPointer(this.mBlueToothPointer);
        this.marrPlugins.add(this.mPluginPanZoom);
        this.marrPlugins.add(this.mPluginBasicDraw);
        this.mnFullImageWidth = (int) (((float) Utilities.getScreenWidth()) - Utilities.dpToPixel(42));
        this.mnFullImageHeight = Utilities.getScreenHeight();
        this.mDrawView.setBackgroundResource(R.drawable.background_drawpad);
        if (!(getIntent() == null || getIntent().getExtras() == null)) {
            if (getIntent().getExtras().containsKey("answersheet")) {
                this.marrPlugins.add(new ActivityPlugin_AnswerSheetOtherQuestions(this, this.mRelativeLayout, this.mTextInputView, this.mDrawView, this.mToolsLayout));
            }
            if (getIntent().getExtras().containsKey("base64image")) {
                Bitmap bitmap = Utilities.getBase64Bitmap(getIntent().getExtras().getString("base64image"));
                if (bitmap != null) {
                    this.mnFullImageWidth = bitmap.getWidth();
                    this.mnFullImageHeight = bitmap.getHeight();
                    this.mDrawView.setBackgroundBitmap(bitmap);
                }
            }
            if (getIntent().getExtras().containsKey("imageWidth") && getIntent().getExtras().containsKey("imageHeight")) {
                this.mnFullImageWidth = getIntent().getExtras().getInt("imageWidth");
                this.mnFullImageHeight = getIntent().getExtras().getInt("imageHeight");
            }
            BitmapDrawable background;
            if (getIntent().getExtras().containsKey("imageKey")) {
                background = new BitmapDrawable(getResources(), getExternalCacheDir() + "/" + getIntent().getExtras().getString("imageKey") + ".jpg");
                this.mDrawView.setBackgroundDrawable(background);
                this.mnFullImageWidth = background.getBitmap().getWidth();
                this.mnFullImageHeight = background.getBitmap().getHeight();
            } else {
                this.mDrawView.setBackgroundResource(R.drawable.background_drawpad);
                if (this.mnFullImageWidth == -1 || this.mnFullImageHeight == -1) {
                    background = (BitmapDrawable) this.mDrawView.getBackground();
                    this.mnFullImageWidth = background.getBitmap().getWidth();
                    this.mnFullImageHeight = background.getBitmap().getHeight();
                }
            }
            String szQuestionIndex = getIntent().getExtras().getString("displayText");
            if (szQuestionIndex == null || szQuestionIndex.isEmpty()) {
                this.mQuestionIndex.setVisibility(8);
            } else {
                this.mQuestionIndex.setText(szQuestionIndex);
                this.mQuestionIndex.setVisibility(0);
            }
            if (getIntent().getExtras().containsKey("imageData")) {
                this.mDrawView.fromString(getIntent().getExtras().getString("imageData"));
            }
        }
        Iterator it = this.marrPlugins.iterator();
        while (it.hasNext()) {
            ((ActivityPluginBase) it.next()).setFullScaleSize(this.mnFullImageWidth, this.mnFullImageHeight);
        }
        this.mDrawView.setColor(mnDefaultColor);
        this.mDrawView.setSize(this.mnFullImageWidth, this.mnFullImageHeight);
        this.mDrawView.setEnableCache(true);
        this.mDrawView.setKeepScreenOn(false);
        this.mPluginBasicDraw.doBestFit();
    }

    public void setImageFullSize(int nWidth, int nHeight) {
        this.mnFullImageWidth = nWidth;
        this.mnFullImageHeight = nHeight;
        Iterator it = this.marrPlugins.iterator();
        while (it.hasNext()) {
            ((ActivityPluginBase) it.next()).setFullScaleSize(this.mnFullImageWidth, this.mnFullImageHeight);
        }
        this.mDrawView.setSize(this.mnFullImageWidth, this.mnFullImageHeight);
        this.mPluginBasicDraw.doBestFit();
    }

    private void makeToolsbarVisible() {
        this.mToolsScrollView.setAlpha(1.0f);
        this.mToolsScrollView.removeCallbacks(this.mFadeToolbarRunnable);
        this.mToolsScrollView.postDelayed(this.mFadeToolbarRunnable, 5000);
    }

    protected void onDestroy() {
        super.onDestroy();
        Utilities.unbindDrawables(findViewById(R.id.RelativeLayout1));
        this.mDrawView.setBackgroundDrawable(null);
        this.mDrawView.clear();
        Iterator it = this.marrPlugins.iterator();
        while (it.hasNext()) {
            ((ActivityPluginBase) it.next()).onDestroy();
        }
        PicassoTools.clearCache(Picasso.with(this));
    }

    public static void setDefaultColor(int nColor) {
        mnDefaultColor = nColor;
    }

    protected void onPause() {
        Iterator it = this.marrPlugins.iterator();
        while (it.hasNext()) {
            ((ActivityPluginBase) it.next()).onPause();
        }
        super.onPause();
    }

    protected void onResume() {
        makeToolsbarVisible();
        Iterator it = this.marrPlugins.iterator();
        while (it.hasNext()) {
            ((ActivityPluginBase) it.next()).onResume();
        }
        Utilities.sliderFromRightToLeft(this.mToolsScrollView, HttpStatus.SC_MULTIPLE_CHOICES);
        super.onResume();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.mPluginPanZoom.onTouchEvent(event);
    }
}
