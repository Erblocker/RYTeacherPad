package com.foxit.uiextensions.security.standard;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.uiextensions.R;

public class PasswordDialog extends Dialog {
    public PasswordDialog(Context context, int theme) {
        super(context, theme);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.rv_rms_waitting_dialog, null);
        LayoutParams params = new LayoutParams(-1, -1);
        params.addRule(13, -1);
        view.setLayoutParams(params);
        setContentView(view);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}
