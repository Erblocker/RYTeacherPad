package com.netspace.library.controls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.pad.library.R;

public class CustomTitleBarView extends LinearLayout {
    private Context m_Context;

    public CustomTitleBarView(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customtitlebarview, this);
        this.m_Context = context;
    }

    public void setTitle(String szTitle) {
        TextView TextView = (TextView) findViewById(R.id.TextViewTitle);
        if (TextView != null) {
            TextView.setText(szTitle);
        }
    }

    public void setSummery(String szSummery) {
        TextView TextView = (TextView) findViewById(R.id.TextViewSubTitle);
        if (TextView != null) {
            TextView.setText(szSummery);
            if (szSummery.isEmpty()) {
                TextView.setVisibility(8);
            }
        }
    }

    public void setBlockColor(int nColor) {
        View BlockView = findViewById(R.id.ViewColorBlock);
        if (BlockView != null) {
            BlockView.setBackgroundColor(nColor);
        }
    }

    public void setBlockVisible(boolean bVisible) {
    }
}
