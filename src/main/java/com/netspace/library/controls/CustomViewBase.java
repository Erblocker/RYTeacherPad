package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.netspace.library.multicontent.MultiContentInterface;
import com.netspace.pad.library.R;

public class CustomViewBase extends LinearLayout implements OnFocusChangeListener {
    private Runnable mCheckVisibleRunnable = new Runnable() {
        public void run() {
            if (CustomViewBase.this.checkVisibleArea()) {
                if (!CustomViewBase.this.mVisible) {
                    CustomViewBase.this.mVisible = true;
                    CustomViewBase.this.onVisible();
                }
            } else if (CustomViewBase.this.mVisible) {
                CustomViewBase.this.mVisible = false;
                CustomViewBase.this.onInVisible();
            }
        }
    };
    private Handler mHandler = new Handler();
    protected boolean mVisible = false;
    private boolean m_bChanged;
    protected boolean m_bFocused;
    protected boolean m_bLocked;

    public CustomViewBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomViewBase(Context context) {
        super(context);
    }

    public void startVisibleCheck() {
        this.mHandler.postDelayed(this.mCheckVisibleRunnable, 100);
    }

    public boolean testVisibleArea() {
        Rect scrollBounds = new Rect();
        getHitRect(scrollBounds);
        if (getLocalVisibleRect(scrollBounds)) {
            onVisible();
            return true;
        }
        onInVisible();
        return false;
    }

    public boolean checkVisibleArea() {
        Rect scrollBounds = new Rect();
        getHitRect(scrollBounds);
        if (getLocalVisibleRect(scrollBounds)) {
            return true;
        }
        return false;
    }

    public void onVisible() {
    }

    public void onInVisible() {
    }

    public void onFocusChange(View v, boolean hasFocus) {
        LinearLayout MenuLayout = (LinearLayout) findViewById(R.id.SubMenuLayout);
        if (MenuLayout != null) {
            this.m_bFocused = hasFocus;
            TranslateAnimation SlideIn;
            if (!hasFocus) {
                SlideIn = new TranslateAnimation(0.0f, -100.0f, 0.0f, 0.0f);
                SlideIn.setInterpolator(new DecelerateInterpolator());
                SlideIn.setDuration(700);
                MenuLayout.setVisibility(0);
                MenuLayout.startAnimation(SlideIn);
                MenuLayout.setVisibility(4);
                MenuLayout.setVisibility(8);
            } else if (!this.m_bLocked) {
                SlideIn = new TranslateAnimation(-100.0f, 0.0f, 0.0f, 0.0f);
                SlideIn.setInterpolator(new DecelerateInterpolator());
                SlideIn.setDuration(700);
                MenuLayout.setVisibility(4);
                MenuLayout.startAnimation(SlideIn);
                MenuLayout.setVisibility(0);
            }
        }
    }

    public boolean isLocked() {
        return this.m_bLocked;
    }

    public boolean isChanged() {
        return this.m_bChanged;
    }

    public void startDefaultAction() {
    }

    public void setChanged() {
        this.m_bChanged = true;
        Context ParentContext = getContext();
        if (ParentContext instanceof MultiContentInterface) {
            ((MultiContentInterface) ParentContext).setChanged(true);
        }
    }

    public void resetChangeFlag() {
        this.m_bChanged = false;
    }

    public void setLocked(boolean bLocked) {
        LinearLayout MenuLayout = (LinearLayout) findViewById(R.id.SubMenuLayout);
        if (this.m_bLocked != bLocked) {
            this.m_bChanged = true;
        }
        if (MenuLayout != null) {
            if (MenuLayout.getVisibility() == 0 && bLocked) {
                onFocusChange(null, false);
            } else if (!(bLocked || getFocusedChild() == null || MenuLayout.getVisibility() != 8)) {
                this.m_bLocked = false;
                onFocusChange(null, true);
            }
        }
        this.m_bLocked = bLocked;
    }

    public void setDefaultButtonIcons() {
        setButtonIconState(R.drawable.ic_plus_light, R.drawable.ic_plus, R.id.ButtonIncrease);
        setButtonIconState(R.drawable.ic_decrease_light, R.drawable.ic_decrease, R.id.ButtonDecrease);
        setButtonIconState(R.drawable.ic_edit_light, R.drawable.ic_edit, R.id.ButtonEdit);
        setButtonIconState(R.drawable.ic_erase_light, R.drawable.ic_erase, R.id.ButtonErase);
        setButtonIconState(R.drawable.ic_recycle_light, R.drawable.ic_recycle, R.id.ButtonDelete);
        setButtonIconState(R.drawable.ic_moveup_light, R.drawable.ic_moveup, R.id.ButtonMoveUp);
        setButtonIconState(R.drawable.ic_movedown_light, R.drawable.ic_movedown, R.id.ButtonMoveDown);
        setButtonIconState(R.drawable.ic_takepicture_light, R.drawable.ic_takepicture, R.id.ButtonTakePicture);
    }

    public void setButtonIconState(int nLightIconID, int nNormalIconID, int nViewID) {
        if (findViewById(nViewID) != null) {
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{16842919}, getContext().getResources().getDrawable(nLightIconID));
            states.addState(new int[]{16842908}, getContext().getResources().getDrawable(nLightIconID));
            states.addState(new int[0], getContext().getResources().getDrawable(nNormalIconID));
            ((ImageButton) findViewById(nViewID)).setImageDrawable(states);
        }
    }
}
