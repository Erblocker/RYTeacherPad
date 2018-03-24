package com.netspace.library.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;
import com.netspace.pad.library.R;

public class CheckableFrameLayout extends FrameLayout implements Checkable {
    private boolean mChecked = false;

    public CheckableFrameLayout(Context context) {
        super(context);
    }

    public CheckableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed) {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.background_checkableframelayout));
        } else {
            setBackgroundDrawable(null);
        }
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
        if (checked) {
            setBackgroundDrawable(getResources().getDrawable(R.drawable.background_checkableframelayout));
        } else {
            setBackgroundDrawable(null);
        }
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public void toggle() {
        setChecked(!this.mChecked);
    }
}
