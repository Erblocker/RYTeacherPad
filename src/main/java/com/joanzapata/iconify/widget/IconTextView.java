package com.joanzapata.iconify.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.internal.HasOnViewAttachListener;
import com.joanzapata.iconify.internal.HasOnViewAttachListener.HasOnViewAttachListenerDelegate;
import com.joanzapata.iconify.internal.HasOnViewAttachListener.OnViewAttachListener;

public class IconTextView extends TextView implements HasOnViewAttachListener {
    private HasOnViewAttachListenerDelegate delegate;

    public IconTextView(Context context) {
        super(context);
        init();
    }

    public IconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IconTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setTransformationMethod(null);
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(Iconify.compute(getContext(), text, this), type);
    }

    public void setOnViewAttachListener(OnViewAttachListener listener) {
        if (this.delegate == null) {
            this.delegate = new HasOnViewAttachListenerDelegate(this);
        }
        this.delegate.setOnViewAttachListener(listener);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.delegate.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.delegate.onDetachedFromWindow();
    }
}
