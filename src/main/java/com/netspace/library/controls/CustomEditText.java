package com.netspace.library.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class CustomEditText extends EditText {
    private OnEditTextActionCallBack mCallBack;
    private Context mContext;

    public interface OnEditTextActionCallBack {
        boolean onTextCopy();

        boolean onTextCut();

        boolean onTextPaste();
    }

    public CustomEditText(Context context) {
        super(context);
        this.mContext = context;
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public void setCallBack(OnEditTextActionCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public boolean onTextContextMenuItem(int id) {
        boolean bConsumed = false;
        if (this.mCallBack != null) {
            switch (id) {
                case 16908320:
                    bConsumed = this.mCallBack.onTextCut();
                    break;
                case 16908321:
                    bConsumed = this.mCallBack.onTextCopy();
                    break;
                case 16908322:
                    bConsumed = this.mCallBack.onTextPaste();
                    break;
            }
        }
        if (bConsumed) {
            return bConsumed;
        }
        return super.onTextContextMenuItem(id);
    }
}
