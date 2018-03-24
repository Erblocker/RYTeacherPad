package me.maxwin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.netspace.pad.library.R;

public class XListViewHeader extends LinearLayout {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_REFRESHING = 2;
    private final int ROTATE_ANIM_DURATION = 180;
    private ImageView mArrowImageView;
    private LinearLayout mContainer;
    public String mHintTextLoading;
    public String mHintTextNormal;
    public String mHintTextReady;
    private TextView mHintTextView;
    private ProgressBar mProgressBar;
    private Animation mRotateDownAnim;
    private Animation mRotateUpAnim;
    private int mState = 0;

    public XListViewHeader(Context context) {
        super(context);
        initView(context);
    }

    public XListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutParams lp = new LayoutParams(-1, 0);
        this.mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.xlistview_header, null);
        addView(this.mContainer, lp);
        setGravity(80);
        this.mArrowImageView = (ImageView) findViewById(R.id.xlistview_header_arrow);
        this.mHintTextView = (TextView) findViewById(R.id.xlistview_header_hint_textview);
        this.mProgressBar = (ProgressBar) findViewById(R.id.xlistview_header_progressbar);
        this.mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, 1, 0.5f, 1, 0.5f);
        this.mRotateUpAnim.setDuration(180);
        this.mRotateUpAnim.setFillAfter(true);
        this.mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, 1, 0.5f, 1, 0.5f);
        this.mRotateDownAnim.setDuration(180);
        this.mRotateDownAnim.setFillAfter(true);
    }

    public void setState(int state) {
        if (state != this.mState) {
            if (state == 2) {
                this.mArrowImageView.clearAnimation();
                this.mArrowImageView.setVisibility(4);
                this.mProgressBar.setVisibility(0);
            } else {
                this.mArrowImageView.setVisibility(0);
                this.mProgressBar.setVisibility(4);
            }
            switch (state) {
                case 0:
                    if (this.mState == 1) {
                        this.mArrowImageView.startAnimation(this.mRotateDownAnim);
                    }
                    if (this.mState == 2) {
                        this.mArrowImageView.clearAnimation();
                    }
                    if (this.mHintTextNormal == null) {
                        this.mHintTextView.setText(R.string.xlistview_header_hint_normal);
                        break;
                    } else {
                        this.mHintTextView.setText(this.mHintTextNormal);
                        break;
                    }
                case 1:
                    if (this.mState != 1) {
                        this.mArrowImageView.clearAnimation();
                        this.mArrowImageView.startAnimation(this.mRotateUpAnim);
                        if (this.mHintTextReady == null) {
                            this.mHintTextView.setText(R.string.xlistview_header_hint_ready);
                            break;
                        } else {
                            this.mHintTextView.setText(this.mHintTextReady);
                            break;
                        }
                    }
                    break;
                case 2:
                    if (this.mHintTextLoading == null) {
                        this.mHintTextView.setText(R.string.xlistview_header_hint_loading);
                        break;
                    } else {
                        this.mHintTextView.setText(this.mHintTextLoading);
                        break;
                    }
            }
            this.mState = state;
        }
    }

    public void setVisiableHeight(int height) {
        if (height < 0) {
            height = 0;
        }
        LayoutParams lp = (LayoutParams) this.mContainer.getLayoutParams();
        lp.height = height;
        this.mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        return this.mContainer.getHeight();
    }
}
