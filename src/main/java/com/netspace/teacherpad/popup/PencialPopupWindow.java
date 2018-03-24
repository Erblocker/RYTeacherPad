package com.netspace.teacherpad.popup;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.netspace.library.adapter.ViewPageAdapter;
import com.netspace.library.controls.ColorPickerView;
import com.netspace.library.controls.ColorPickerView.OnColorChangedListener;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import org.apache.http.HttpStatus;

public class PencialPopupWindow extends BasicPopupWindow implements OnClickListener {
    private ViewPageAdapter mAdapter;
    private OnChangeCallBack mCallBack;
    private ColorPickerView mColorPickerView;
    private View mContentView;
    private View mPreviewView;
    private SeekBar mSeekBarWidth;
    private TextView mTextViewWidth;
    private CustomViewPager mViewPager;
    private int mnColor = 0;
    private int mnWidth = 0;

    public interface OnChangeCallBack {
        void onDataChanged(int i, int i2);
    }

    public PencialPopupWindow(Context context, int nLineWidth, int nColor) {
        super(context);
        this.mnWidth = nLineWidth;
        this.mnColor = nColor;
        updateDisplay();
        this.mSeekBarWidth.setProgress(this.mnWidth);
        this.mColorPickerView.setColor(-16777216 | this.mnColor);
    }

    public void initView() {
        super.initView();
        setWidth(Utilities.dpToPixel((int) HttpStatus.SC_BAD_REQUEST, this.mContext));
        setHeight(Utilities.dpToPixel(340, this.mContext));
        this.mContentView = this.mLayoutInflater.inflate(R.layout.popup_pencial, null);
        this.mColorPickerView = new ColorPickerView(this.mContext);
        this.mColorPickerView.setOnColorChangedListener(new OnColorChangedListener() {
            public void onColorChanged(int newColor) {
                PencialPopupWindow.this.mnColor = newColor;
                PencialPopupWindow.this.updateDisplay();
            }
        });
        this.mColorPickerView.setColor(-16777216 | this.mnColor);
        this.mViewPager = (CustomViewPager) this.mContentView.findViewById(R.id.customViewPager1);
        this.mViewPager.setOffscreenPageLimit(1);
        this.mPreviewView = this.mContentView.findViewById(R.id.viewPreview);
        this.mTextViewWidth = (TextView) this.mContentView.findViewById(R.id.textViewWidth);
        this.mSeekBarWidth = (SeekBar) this.mContentView.findViewById(R.id.seekBarWidth);
        this.mSeekBarWidth.getProgressDrawable().setColorFilter(Utilities.getThemeCustomColor(R.attr.float_button_border_color), Mode.SRC_IN);
        this.mSeekBarWidth.getThumb().setColorFilter(Utilities.getThemeCustomColor(R.attr.float_button_border_color), Mode.SRC_IN);
        this.mSeekBarWidth.setMax(20);
        this.mSeekBarWidth.setProgress(this.mnWidth);
        this.mSeekBarWidth.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                }
                PencialPopupWindow.this.mnWidth = progress;
                PencialPopupWindow.this.updateDisplay();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.mAdapter = new ViewPageAdapter(this.mContext);
        this.mAdapter.addPage(this.mColorPickerView, "颜色");
        this.mViewPager.setAdapter(this.mAdapter);
        this.mContentLayout.addView(this.mContentView, -1, -1);
    }

    public void setCallBack(OnChangeCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void updateDisplay() {
        LayoutParams params = this.mPreviewView.getLayoutParams();
        params.height = this.mnWidth;
        this.mPreviewView.setLayoutParams(params);
        this.mTextViewWidth.setText(new StringBuilder(String.valueOf(String.valueOf(this.mnWidth))).append("px").toString());
        this.mPreviewView.setBackgroundColor(this.mnColor | -16777216);
        if (this.mCallBack != null) {
            this.mCallBack.onDataChanged(this.mnWidth, this.mnColor | -16777216);
        }
    }

    public void setColor(int nColor) {
        this.mnColor = nColor;
        updateDisplay();
    }

    public void setLineWidth(int nWidth) {
        this.mnWidth = nWidth;
        updateDisplay();
    }

    public void onClick(View v) {
    }
}
