package com.netspace.library.controls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class CustomSelectorView extends CustomViewBase implements OnClickListener {
    private CustomSelectorViewCallBack mCallBack;
    private Context m_Context;
    private LinearLayout m_RootLayout;
    private boolean m_bMultiableSelect = false;
    private float m_fTextSize = 20.0f;
    private int m_nHeight = -2;
    private int m_nWidth = -2;

    public interface CustomSelectorViewCallBack {
        void onChange(CustomSelectorView customSelectorView);
    }

    public CustomSelectorView(Context context) {
        super(context);
        View RootView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customselectorview, this);
        this.m_Context = context;
        this.m_RootLayout = (LinearLayout) RootView.findViewById(R.id.LayoutButtons);
    }

    public void setCallBack(CustomSelectorViewCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void setMultiableSelect(boolean bValue) {
        this.m_bMultiableSelect = bValue;
    }

    public void setSize(int nWidth, int nHeight) {
        this.m_nWidth = nWidth;
        this.m_nHeight = nHeight;
    }

    public void setTextSize(float fSize) {
        this.m_fTextSize = fSize;
    }

    public void addOptions(String szOptionText) {
        Button NewButton = new Button(this.m_Context);
        NewButton.setText(szOptionText);
        Utilities.setViewBackground(NewButton, Utilities.getThemeCustomDrawable(R.attr.selector_button_background, this.m_Context));
        NewButton.setTag(szOptionText);
        NewButton.setOnClickListener(this);
        NewButton.setTextSize(this.m_fTextSize);
        this.m_RootLayout.addView(NewButton);
        LayoutParams LayoutParam = (LayoutParams) NewButton.getLayoutParams();
        if (this.m_RootLayout.getChildCount() == 1) {
            LayoutParam.leftMargin = 0;
        } else {
            LayoutParam.leftMargin = 10;
        }
        LayoutParam.width = this.m_nWidth;
        LayoutParam.height = this.m_nHeight;
        NewButton.setLayoutParams(LayoutParam);
    }

    public String getValue() {
        String szResult = "";
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            View OneChild = this.m_RootLayout.getChildAt(i);
            if ((OneChild instanceof Button) && OneChild.isSelected()) {
                szResult = new StringBuilder(String.valueOf(szResult)).append(((Button) OneChild).getText()).toString();
            }
        }
        return szResult;
    }

    public void putValue(String szValue) {
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            View OneChild = this.m_RootLayout.getChildAt(i);
            if ((OneChild instanceof Button) && szValue.indexOf(((Button) OneChild).getText().toString()) != -1) {
                ((Button) OneChild).setSelected(true);
            }
        }
    }

    public void putCorrectValue(String szValue) {
        for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
            View OneChild = this.m_RootLayout.getChildAt(i);
            if ((OneChild instanceof Button) && szValue.indexOf(((Button) OneChild).getText().toString()) != -1) {
                ((Button) OneChild).setTextColor(-16716288);
            }
        }
    }

    public void onClick(View v) {
        boolean z = false;
        if (!this.m_bLocked) {
            Button Button = (Button) v;
            Utilities.logClick(this, Button.getText().toString());
            if (this.m_bMultiableSelect) {
                if (!Button.isSelected()) {
                    z = true;
                }
                Button.setSelected(z);
                setChanged();
            } else {
                boolean bPreviousState = Button.isSelected();
                for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
                    View OneChild = this.m_RootLayout.getChildAt(i);
                    if (OneChild instanceof Button) {
                        OneChild.setSelected(false);
                    }
                }
                Button.setSelected(true);
                if (Button.isSelected() != bPreviousState) {
                    setChanged();
                }
            }
            if (this.mCallBack != null) {
                this.mCallBack.onChange(this);
            }
        }
    }
}
