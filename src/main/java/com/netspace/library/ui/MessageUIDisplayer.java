package com.netspace.library.ui;

import android.app.Activity;
import android.widget.TextView;
import com.netspace.pad.library.R;

public class MessageUIDisplayer extends UIDisplayer {
    public MessageUIDisplayer(Activity Activity) {
        super(Activity);
        this.mOverlappedLayoutID = R.layout.layout_message;
    }

    public void setText(String szText) {
        super.setText(szText);
        if (this.mOverlappedLayout != null) {
            TextView textView = (TextView) this.mOverlappedLayout.findViewById(R.id.textViewMessageDescription);
            if (textView != null) {
                textView.setText(szText);
            }
        }
    }

    public void setTitle(String szText) {
        super.setTitle(szText);
        if (this.mOverlappedLayout != null) {
            TextView textView = (TextView) this.mOverlappedLayout.findViewById(R.id.textViewMessageTitle);
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
            TextView textView = (TextView) this.mOverlappedLayout.findViewById(R.id.textViewMessageDescription);
            if (textView != null) {
                textView.setText(this.mText);
            }
            textView = (TextView) this.mOverlappedLayout.findViewById(R.id.textViewMessageTitle);
            if (textView != null) {
                textView.setText(this.mTitle);
            }
        }
        return true;
    }
}
