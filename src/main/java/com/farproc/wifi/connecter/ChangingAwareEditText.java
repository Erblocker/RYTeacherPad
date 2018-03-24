package com.farproc.wifi.connecter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class ChangingAwareEditText extends EditText {
    private boolean mChanged = false;

    public ChangingAwareEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean getChanged() {
        return this.mChanged;
    }

    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        this.mChanged = true;
    }
}
