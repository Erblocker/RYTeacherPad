package com.netspace.library.ui;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;

public class ErrorUIDisplayer extends UIDisplayer {
    public ErrorUIDisplayer(Activity Activity) {
        super(Activity);
        this.mOverlappedLayoutID = R.layout.layout_error;
    }

    public void setText(String szText) {
        super.setText(szText);
        if (this.mOverlappedLayout != null) {
            TextView textView = (TextView) this.mOverlappedLayout.findViewById(R.id.textViewErrorDescription);
            if (textView != null) {
                textView.setText(szText);
            }
        }
    }

    public boolean showOverlappedLayout() {
        if (!super.showOverlappedLayout()) {
            return false;
        }
        if (this.mOverlappedLayout != null) {
            TextView textView = (TextView) this.mOverlappedLayout.findViewById(R.id.textViewErrorDescription);
            if (textView != null) {
                textView.setText(this.mText);
            }
            Button retryButton = (Button) this.mOverlappedLayout.findViewById(R.id.buttonRetry);
            if (retryButton != null) {
                retryButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Activity CurrentActivity = (Activity) ErrorUIDisplayer.this.mActivity.get();
                        if (CurrentActivity != null && (CurrentActivity instanceof BaseActivity)) {
                            ((BaseActivity) CurrentActivity).resetCancelFlag();
                        }
                        VirtualNetworkObject.pauseCurrentActivityExecution(false);
                        ErrorUIDisplayer.this.hideOverlappedLayout();
                    }
                });
            }
        }
        return true;
    }
}
