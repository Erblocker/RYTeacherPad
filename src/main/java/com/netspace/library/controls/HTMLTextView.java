package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import java.util.ArrayList;

public class HTMLTextView extends View {
    private static Typeface m_TypeFace;
    private Paint m_BackgroundPaint;
    private StaticLayout m_Layout;
    private TextPaint m_Paint;
    private ArrayList<DrawData> m_arrDrawData = new ArrayList();
    private int m_nBottomMargin = 48;
    private int m_nColumnCount = 3;
    private int m_nColumnSpacing = 46;
    private int m_nHeight = -1;
    private int m_nLeftMargin = 48;
    private int m_nRightMargin = 48;
    private int m_nScreenHeight = 720;
    private int m_nTopMargin = 48;
    private String m_szRemainText = null;
    private String m_szText;

    private class DrawData {
        StaticLayout Layout;
        int nX;
        int nY;

        private DrawData() {
        }
    }

    public HTMLTextView(Context context) {
        super(context);
        m_TypeFace = Typeface.SERIF;
        this.m_Paint = new TextPaint();
        this.m_Paint.setColor(-16316665);
        this.m_Paint.bgColor = -1;
        this.m_Paint.setTypeface(m_TypeFace);
        this.m_Paint.setTextSize(18.0f);
        this.m_Paint.setAntiAlias(true);
        this.m_szText = "";
        this.m_BackgroundPaint = new Paint();
        this.m_BackgroundPaint.setColor(-1);
    }

    public void setMargin(int nLeftMargin, int nTopMargin, int nRightMargin, int nBottomMargin) {
        this.m_nTopMargin = nTopMargin;
        this.m_nLeftMargin = nLeftMargin;
        this.m_nRightMargin = nRightMargin;
        this.m_nBottomMargin = nBottomMargin;
    }

    public void setColumn(int nColumnCount, int nSpace) {
        this.m_nColumnCount = nColumnCount;
        this.m_nColumnSpacing = nSpace;
    }

    public void setText(String szText) {
        this.m_szText = szText;
    }

    public void setScreenHeight(int nScreenHeight) {
        this.m_nScreenHeight = nScreenHeight;
    }

    public String getRemainText() {
        return this.m_szRemainText;
    }

    public int getEstHeight(int nWidth) {
        StaticLayout Layout = new StaticLayout(this.m_szText, this.m_Paint, (((nWidth - this.m_nLeftMargin) - this.m_nRightMargin) - (this.m_nColumnSpacing * (this.m_nColumnCount - 1))) / this.m_nColumnCount, Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
        int nTotalHeight = Layout.getLineBottom(Layout.getLineCount() - 1);
        int nResult = ((nTotalHeight / this.m_nColumnCount) + this.m_nTopMargin) + this.m_nBottomMargin;
        while (nTotalHeight > this.m_nScreenHeight) {
            nResult += this.m_nColumnSpacing * 2;
            nTotalHeight -= this.m_nScreenHeight;
        }
        return nResult;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.m_BackgroundPaint);
        if (this.m_arrDrawData.size() == 0) {
            String szText = this.m_szText;
            int nColumnWidth = (((getWidth() - this.m_nLeftMargin) - this.m_nRightMargin) - (this.m_nColumnSpacing * (this.m_nColumnCount - 1))) / this.m_nColumnCount;
            int nColumnHeight = (getHeight() - this.m_nTopMargin) - this.m_nBottomMargin;
            int nXPos = this.m_nLeftMargin;
            int nYPos = this.m_nTopMargin;
            String szCurrentText = "";
            if (szText.length() > 10240) {
                szText = szText.substring(0, 10240) + "...";
            }
            if (nColumnHeight > this.m_nScreenHeight) {
                nColumnHeight = this.m_nScreenHeight;
            }
            while (!szText.isEmpty()) {
                StaticLayout Layout = new StaticLayout(szText, this.m_Paint, nColumnWidth, Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
                int nlastVisibleLineNumber = Layout.getLineForVertical(nColumnHeight);
                if (Layout.getLineBaseline(nlastVisibleLineNumber) > nColumnHeight) {
                    nlastVisibleLineNumber--;
                }
                int nEnd = Layout.getLineEnd(nlastVisibleLineNumber);
                if (szText.length() > nEnd) {
                    szCurrentText = szText.substring(0, nEnd);
                } else {
                    szCurrentText = szText;
                }
                HTMLTextView hTMLTextView = this;
                DrawData drawData = new DrawData();
                drawData.Layout = new StaticLayout(szCurrentText, this.m_Paint, nColumnWidth, Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
                drawData.nX = nXPos;
                drawData.nY = nYPos;
                this.m_arrDrawData.add(drawData);
                if (szText.length() <= nEnd || nEnd <= 0) {
                    break;
                }
                szText = szText.substring(nEnd);
                nXPos += this.m_nColumnSpacing + nColumnWidth;
                if (nXPos + nColumnWidth > getWidth()) {
                    nXPos = this.m_nLeftMargin;
                    nYPos += (this.m_nColumnSpacing * 2) + nColumnHeight;
                    this.m_nHeight = nYPos + nColumnHeight;
                }
            }
            this.m_szRemainText = szText;
        }
        int i = 0;
        while (i < this.m_arrDrawData.size()) {
            DrawData DrawData = (DrawData) this.m_arrDrawData.get(i);
            canvas.save();
            canvas.translate((float) DrawData.nX, (float) DrawData.nY);
            DrawData.Layout.draw(canvas);
            canvas.restore();
            if (DrawData.nX == this.m_nLeftMargin && i > 0) {
                Paint LinePaint = new Paint();
                LinePaint.setStrokeWidth(0.0f);
                LinePaint.setColor(-4473925);
                LinePaint.setStyle(Style.STROKE);
                canvas.drawLine((float) this.m_nLeftMargin, (float) (DrawData.nY - this.m_nColumnSpacing), (float) (getWidth() - this.m_nRightMargin), (float) (DrawData.nY - this.m_nColumnSpacing), LinePaint);
            }
            i++;
        }
    }
}
