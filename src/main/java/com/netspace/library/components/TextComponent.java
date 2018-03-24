package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.controls.LinedEditText;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class TextComponent extends CustomFrameLayout implements IComponents {
    private ComponentCallBack mCallBack;
    private ContextThemeWrapper mContextThemeWrapper;
    private LinedEditText mEditText;
    private KeyListener mKeyListener;
    private boolean mbAutoHeight = true;
    private String mszData;

    public TextComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public TextComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TextComponent(Context context) {
        super(context);
        initView();
    }

    public void setAutoHeight(boolean bEnable) {
        this.mbAutoHeight = bEnable;
    }

    public void initView() {
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mEditText = new LinedEditText(this.mContextThemeWrapper);
        this.mEditText.setBackgroundResource(R.drawable.background_textcomponent);
        this.mEditText.setGravity(48);
        this.mEditText.setLineColor(-10395295);
        this.mEditText.setLongClickable(false);
        this.mEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (TextComponent.this.mCallBack != null) {
                    TextComponent.this.mCallBack.OnDataUploaded(TextComponent.this.getData(), TextComponent.this);
                }
                if (TextComponent.this.mbAutoHeight) {
                    TextComponent.this.mEditText.measure(0, 0);
                    TextComponent.this.mEditText.scrollTo(0, 0);
                    int nTextHeight = TextComponent.this.mEditText.getMeasuredHeight();
                    LayoutParams Params = TextComponent.this.getLayoutParams();
                    if (Params != null) {
                        Params.height = Utilities.dpToPixel(30, TextComponent.this.getContext()) + nTextHeight;
                        Params.width = -1;
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        this.mKeyListener = this.mEditText.getKeyListener();
        addView(this.mEditText, new FrameLayout.LayoutParams(-1, -1));
    }

    public void setData(String szData) {
        this.mEditText.setText(szData);
    }

    public String getData() {
        return this.mEditText.getText().toString();
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
        if (bLock) {
            this.mEditText.setFocusable(false);
            return;
        }
        this.mEditText.setFocusableInTouchMode(true);
        this.mEditText.setFocusable(true);
    }
}
