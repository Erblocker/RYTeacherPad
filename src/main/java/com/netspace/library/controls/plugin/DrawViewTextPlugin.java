package com.netspace.library.controls.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewPlugin;
import com.netspace.library.utilities.Utilities;

public class DrawViewTextPlugin implements DrawViewPlugin {
    private static final String TAG = "DrawViewTextPlugin";
    private Context mContext;
    private TextPaint mPaint;
    private int mnXOffset = ((int) Utilities.dpToPixel(20));
    private int mnYOffset = ((int) Utilities.dpToPixel(20));
    private String mszText = "";

    public boolean initialize(Context context, DrawView drawView) {
        this.mContext = context;
        this.mPaint = new TextPaint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(-16777216);
        this.mPaint.setTextSize(24.0f * context.getResources().getDisplayMetrics().density);
        return true;
    }

    public DrawViewTextPlugin setText(String szText) {
        this.mszText = szText;
        return this;
    }

    public DrawViewTextPlugin setTextColor(int nColor) {
        this.mPaint.setColor(nColor);
        return this;
    }

    public DrawViewTextPlugin setTextSize(float textSize) {
        this.mPaint.setTextSize(this.mContext.getResources().getDisplayMetrics().density * textSize);
        return this;
    }

    public DrawViewTextPlugin setTextOffset(int nXOffset, int nYOffset) {
        this.mnXOffset = nXOffset;
        this.mnYOffset = nYOffset;
        return this;
    }

    public void onDraw(Canvas canvas, float fScale) {
        if (this.mszText != null && !this.mszText.isEmpty()) {
            float fTextSize = this.mPaint.getTextSize();
            float fWidth = ((float) canvas.getWidth()) - (2.0f * (((float) this.mnXOffset) * fScale));
            int nXOffset = this.mnXOffset;
            if (fWidth < 0.0f) {
                fWidth = (float) canvas.getWidth();
                nXOffset = 0;
            }
            this.mPaint.setTextSize(fTextSize * fScale);
            canvas.save();
            canvas.translate(((float) nXOffset) * fScale, ((float) this.mnYOffset) * fScale);
            new StaticLayout(this.mszText, this.mPaint, (int) fWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true).draw(canvas);
            canvas.restore();
            this.mPaint.setTextSize(fTextSize);
        }
    }
}
