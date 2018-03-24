package com.foxit.uiextensions.annots.freetext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.DefaultAppearance;
import com.foxit.sdk.common.Font;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import java.util.ArrayList;
import java.util.HashMap;

public class FtTextUtil {
    private final float NEWLINE_BORDER_REDUNDANT_THRESHOLDS = 10.0f;
    private Blink mBlink;
    private Context mContext;
    private int mCurrentLine = 0;
    private float mCurrentLineWidth;
    private int mCurrentPageIndex;
    private int mCurrentSelectIndex;
    private Paint mCursorPaint = new Paint();
    private float mEditPointX = 0.0f;
    private float mEditPointY = 0.0f;
    private boolean mEditState;
    private float mFontHeight = 0.0f;
    private FontMetrics mFontMetrics;
    private String mFontName;
    private HashMap<String, String> mFontNameMap;
    private float mFontSize;
    private boolean mInvalidate = true;
    private float mMaxTextHeight = 0.0f;
    private float mMaxTextWidth = 0.0f;
    private int mPageLineNum = 0;
    private float mPageOffset;
    private PDFViewCtrl mPdfViewCtrl;
    private int mRealLine = 0;
    private boolean mResetEdit;
    private int mSpace;
    private float mStartPointX = 0.0f;
    private float mStartPointY = 0.0f;
    private float mStartToBorderX;
    private float mStartToBorderY;
    private ArrayList<String> mStringList = new ArrayList();
    private String mTempTextContent = "";
    private int mTextColor;
    private String mTextContent = "";
    private int mTextOpacity;
    private Paint mTextPaint = new Paint();
    private OnTextValuesChangedListener mTextValuesChangedListener;

    private class Blink extends Handler implements Runnable {
        private Blink() {
        }

        public void run() {
            FtTextUtil.this.mInvalidate = !FtTextUtil.this.mInvalidate;
            if (FtTextUtil.this.mPdfViewCtrl != null && FtTextUtil.this.mPdfViewCtrl.isPageVisible(FtTextUtil.this.mCurrentPageIndex)) {
                RectF rect = new RectF();
                if (FtTextUtil.this.mTextContent == null || FtTextUtil.this.mTextContent.equals("")) {
                    rect.set((FtTextUtil.this.mStartPointX - ((float) FtTextUtil.this.mSpace)) + FtTextUtil.this.mCurrentLineWidth, (FtTextUtil.this.mStartPointY + (FtTextUtil.this.mFontHeight * ((float) FtTextUtil.this.mRealLine))) - ((float) FtTextUtil.this.mSpace), (FtTextUtil.this.mStartPointX + FtTextUtil.this.mCurrentLineWidth) + ((float) FtTextUtil.this.mSpace), (FtTextUtil.this.mStartPointY + (FtTextUtil.this.mFontHeight * ((float) (FtTextUtil.this.mRealLine + 1)))) + ((float) FtTextUtil.this.mSpace));
                } else if (FtTextUtil.this.mEditPointX == 0.0f && FtTextUtil.this.mEditPointY == 0.0f) {
                    rect.set((FtTextUtil.this.mStartPointX - ((float) FtTextUtil.this.mSpace)) + FtTextUtil.this.mCurrentLineWidth, (FtTextUtil.this.mStartPointY + (FtTextUtil.this.mFontHeight * ((float) (FtTextUtil.this.mRealLine - 1)))) - ((float) FtTextUtil.this.mSpace), (FtTextUtil.this.mStartPointX + FtTextUtil.this.mCurrentLineWidth) + ((float) FtTextUtil.this.mSpace), (FtTextUtil.this.mStartPointY + (FtTextUtil.this.mFontHeight * ((float) FtTextUtil.this.mRealLine))) + ((float) FtTextUtil.this.mSpace));
                } else {
                    rect.set(FtTextUtil.this.mEditPointX - ((float) FtTextUtil.this.mSpace), FtTextUtil.this.mEditPointY - ((float) FtTextUtil.this.mSpace), FtTextUtil.this.mEditPointX + ((float) FtTextUtil.this.mSpace), (FtTextUtil.this.mEditPointY + FtTextUtil.this.mFontHeight) + ((float) FtTextUtil.this.mSpace));
                }
                FtTextUtil.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rect, rect, FtTextUtil.this.mCurrentPageIndex);
                FtTextUtil.this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rect));
            }
            postDelayed(this, 500);
        }
    }

    public interface OnTextValuesChangedListener {
        void onCurrentSelectIndex(int i);

        void onEditPointChanged(float f, float f2);

        void onMaxHeightChanged(float f);

        void onMaxWidthChanged(float f);
    }

    public void setOnWidthChanged(OnTextValuesChangedListener listener) {
        if (this.mTextValuesChangedListener == null) {
            this.mTextValuesChangedListener = listener;
        }
    }

    public FtTextUtil(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mCursorPaint.setColor(-16777216);
        this.mCursorPaint.setStyle(Style.STROKE);
        this.mCursorPaint.setAntiAlias(true);
        this.mCursorPaint.setDither(true);
        this.mCursorPaint.setStrokeWidth((float) AppDisplay.getInstance(context).dp2px(2.0f));
        this.mTextPaint.setTextAlign(Align.LEFT);
        this.mTextPaint.setAntiAlias(true);
        initFontNameMap();
    }

    private void initFontNameMap() {
        try {
            this.mFontNameMap = new HashMap();
            Font Courier = Font.createStandard(0);
            this.mFontNameMap.put(Courier.getName(), "Courier");
            Courier.release();
            Font Helvetica = Font.createStandard(4);
            this.mFontNameMap.put(Helvetica.getName(), "Helvetica");
            Helvetica.release();
            Font Times = Font.createStandard(8);
            this.mFontNameMap.put(Times.getName(), "Times");
            Times.release();
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public String getSupportFontName(String key) {
        return this.mFontNameMap.get(key) != null ? (String) this.mFontNameMap.get(key) : "Courier";
    }

    public Font getSupportFont(String name) {
        if (name == null) {
            try {
                return Font.createStandard(0);
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        } else if (name.equals("Courier")) {
            return Font.createStandard(0);
        } else {
            if (name.equals("Helvetica")) {
                return Font.createStandard(4);
            }
            if (name.equals("Times")) {
                return Font.createStandard(8);
            }
            if (name.equalsIgnoreCase("Courier") || name.equalsIgnoreCase("Helvetica") || name.equalsIgnoreCase("Times")) {
                return null;
            }
            return Font.createStandard(0);
        }
    }

    public String getSupportFontName(DefaultAppearance da) {
        Font font;
        String fontname = null;
        if (da != null) {
            try {
                font = da.getFont();
            } catch (PDFException e) {
            }
        } else {
            font = null;
        }
        if (font != null) {
            fontname = font.getName();
        }
        return getSupportFontName(fontname);
    }

    public void setTextString(int pageIndex, String text, boolean editState) {
        this.mCurrentPageIndex = pageIndex;
        this.mTextContent = text;
        this.mEditState = editState;
    }

    public void setStartPoint(PointF startPoint) {
        this.mStartPointX = startPoint.x;
        this.mStartPointY = startPoint.y;
    }

    public void setEditPoint(PointF editPoint) {
        this.mEditPointX = editPoint.x;
        this.mEditPointY = editPoint.y;
    }

    public void setMaxRect(float width, float height) {
        this.mMaxTextWidth = width;
        this.mMaxTextHeight = height;
    }

    public void setTextColor(int textColor, int opacity) {
        this.mTextColor = textColor;
        this.mTextOpacity = opacity;
    }

    public void setFont(String fontName, float fontSize) {
        this.mFontName = fontName;
        this.mFontSize = fontSize;
    }

    public void setEndSelection(int endSelection) {
        this.mCurrentSelectIndex = endSelection;
    }

    public void loadText() {
        this.mStringList.clear();
        this.mRealLine = 0;
        this.mCurrentLineWidth = 0.0f;
        setPaintFont(this.mTextPaint, this.mFontName);
        this.mTextPaint.setTextSize(PdfSize2PageViewSize(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mFontSize));
        this.mTextPaint.setColor(this.mTextColor);
        this.mTextPaint.setAlpha(this.mTextOpacity);
        this.mFontHeight = getFontHeight(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mFontName, this.mFontSize);
        this.mPageLineNum = (int) (this.mMaxTextHeight / this.mFontHeight);
        this.mSpace = ((int) this.mFontHeight) * 5;
        if (!(this.mTextContent == null || this.mTextContent.equals(""))) {
            this.mStringList = getComposedText(this.mPdfViewCtrl, this.mCurrentPageIndex, new RectF(0.0f, 0.0f, this.mMaxTextWidth, this.mMaxTextHeight), this.mTextContent, this.mFontName, this.mFontSize);
        }
        jumpPageWithKeyBoard();
        if (this.mTextContent != null && !this.mTextContent.equals("")) {
            this.mRealLine = this.mStringList.size();
            adjustEditPoint(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStringList, this.mFontName, this.mFontSize);
            float maxWidth = getTextMaxWidth(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStringList, this.mFontName, this.mFontSize);
            this.mTextValuesChangedListener.onMaxHeightChanged(this.mFontHeight * ((float) this.mRealLine));
            this.mTextValuesChangedListener.onMaxWidthChanged(maxWidth);
            PointF point = new PointF(this.mStartPointX, this.mStartPointY);
            this.mPdfViewCtrl.convertPageViewPtToDisplayViewPt(point, point, this.mCurrentPageIndex);
            if (this.mCurrentPageIndex == this.mPdfViewCtrl.getPageCount() - 1 || this.mPdfViewCtrl.getPageLayoutMode() == 1) {
                this.mStartToBorderX = ((float) AppDisplay.getInstance(this.mContext).getRawScreenWidth()) - point.x;
                this.mStartToBorderY = (((float) AppDisplay.getInstance(this.mContext).getRawScreenHeight()) - point.y) + this.mPageOffset;
            } else {
                this.mStartToBorderX = ((float) AppDisplay.getInstance(this.mContext).getRawScreenWidth()) - point.x;
                this.mStartToBorderY = ((float) AppDisplay.getInstance(this.mContext).getRawScreenHeight()) - point.y;
            }
            if (!this.mTextContent.equals(this.mTempTextContent)) {
                jumpPageWithHorizontal(maxWidth);
                jumpPageWithVertical(maxWidth);
            }
        }
    }

    public ArrayList<String> getComposedText(PDFViewCtrl pdfViewCtrl, int pageIndex, RectF annotRect, String textContent, String fontName, float fontSize) {
        int rotate;
        ArrayList<String> composedText = new ArrayList();
        float tCurrentLineWidth = 0.0f;
        Paint paint = new Paint();
        setPaintFont(paint, fontName);
        paint.setTextSize(100.0f * fontSize);
        RectF rect = new RectF(annotRect);
        try {
            rotate = pdfViewCtrl.getDoc().getPage(pageIndex).getRotation();
        } catch (PDFException e) {
            e.printStackTrace();
            rotate = 0;
        }
        pdfViewCtrl.convertPageViewRectToPdfRect(rect, rect, pageIndex);
        int iStart = 0;
        if (textContent == null) {
            textContent = "";
        }
        int count = textContent.length();
        int i = 0;
        while (i < count) {
            char ch = textContent.charAt(i);
            float chWidth = paint.measureText(String.valueOf(ch));
            if (ch == '\n' || ch == '\r') {
                composedText.add(textContent.substring(iStart, i + 1));
                iStart = i + 1;
                if (i == count - 1) {
                    composedText.add("");
                }
                tCurrentLineWidth = 0.0f;
            } else {
                tCurrentLineWidth += chWidth;
                if (this.mEditState) {
                    float abs = (rotate == 90 || rotate == 270) ? (Math.abs(rect.height()) * 100.0f) + 10.0f : (Math.abs(rect.width()) * 100.0f) + 10.0f;
                    if (tCurrentLineWidth > abs) {
                        abs = (rotate == 90 || rotate == 270) ? (Math.abs(rect.height()) * 100.0f) + 10.0f : (Math.abs(rect.width()) * 100.0f) + 10.0f;
                        if (abs > chWidth) {
                            composedText.add(textContent.substring(iStart, i));
                            iStart = i;
                            i--;
                            tCurrentLineWidth = 0.0f;
                        }
                    }
                    if (i == count - 1) {
                        composedText.add(textContent.substring(iStart, count));
                    }
                } else if (i == count - 1) {
                    composedText.add(textContent.substring(iStart, count));
                }
            }
            i++;
        }
        this.mCurrentLineWidth = PdfSize2PageViewSize(pdfViewCtrl, pageIndex, tCurrentLineWidth) / 100.0f;
        return composedText;
    }

    public void adjustTextRect(PDFViewCtrl pdfViewCtrl, int pageIndex, String fontName, float fontSize, RectF rect, ArrayList<String> composedText) {
        float rectHeight = getFontHeight(pdfViewCtrl, pageIndex, fontName, fontSize) * ((float) composedText.size());
        if (rectHeight > rect.height()) {
            rect.bottom = rect.top + rectHeight;
        }
    }

    private float getTextMaxWidth(PDFViewCtrl pdfViewCtrl, int pageIndex, ArrayList<String> textStrings, String fontName, float fontSize) {
        Paint paint = new Paint();
        setPaintFont(paint, fontName);
        paint.setTextSize(fontSize * 100.0f);
        float maxWidth = 0.0f;
        for (int i = 0; i < textStrings.size(); i++) {
            maxWidth = Math.max(maxWidth, paint.measureText((String) textStrings.get(i)));
        }
        return Math.max(PdfSize2PageViewSize(pdfViewCtrl, pageIndex, maxWidth) / 100.0f, this.mCurrentLineWidth);
    }

    private void adjustEditPoint(PDFViewCtrl pdfViewCtrl, int pageIndex, ArrayList<String> textStrings, String fontName, float fontSize) {
        int editSelectIndex = 0;
        Paint paint = new Paint();
        setPaintFont(paint, fontName);
        paint.setTextSize(100.0f * fontSize);
        int i;
        String currentLineStr;
        float currentWidth;
        int j;
        float currentWidthPage;
        if (this.mResetEdit) {
            if (this.mTempTextContent.length() != this.mTextContent.length()) {
                for (i = 0; i < textStrings.size(); i++) {
                    currentLineStr = (String) textStrings.get(i);
                    if (i > 0 && currentLineStr.contains("\n")) {
                        editSelectIndex++;
                    }
                    if (i == textStrings.size() - 1) {
                        editSelectIndex++;
                    }
                    currentLineStr = currentLineStr.replace("\n", "");
                    if (this.mCurrentSelectIndex >= editSelectIndex && this.mCurrentSelectIndex <= currentLineStr.length() + editSelectIndex) {
                        currentWidth = 0.0f;
                        if (currentLineStr.length() > 0) {
                            j = 0;
                            while (j < currentLineStr.length()) {
                                currentWidth += paint.measureText(String.valueOf(currentLineStr.charAt(j)));
                                currentWidthPage = PdfSize2PageViewSize(pdfViewCtrl, pageIndex, currentWidth) / 100.0f;
                                if (this.mCurrentSelectIndex == editSelectIndex) {
                                    this.mEditPointX = this.mStartPointX;
                                    this.mEditPointY = this.mStartPointY + (this.mFontHeight * ((float) i));
                                    this.mTextValuesChangedListener.onEditPointChanged(this.mEditPointX, this.mEditPointY);
                                    break;
                                } else if (this.mCurrentSelectIndex == (editSelectIndex + j) + 1) {
                                    this.mEditPointX = this.mStartPointX + currentWidthPage;
                                    this.mEditPointY = this.mStartPointY + (this.mFontHeight * ((float) i));
                                    this.mTextValuesChangedListener.onEditPointChanged(this.mEditPointX, this.mEditPointY);
                                    break;
                                } else {
                                    j++;
                                }
                            }
                        } else {
                            this.mEditPointX = this.mStartPointX;
                            this.mEditPointY = this.mStartPointY + (this.mFontHeight * ((float) i));
                            this.mTextValuesChangedListener.onEditPointChanged(this.mEditPointX, this.mEditPointY);
                        }
                    }
                    editSelectIndex += currentLineStr.length();
                }
                return;
            }
            return;
        }
        i = 0;
        while (i < textStrings.size()) {
            currentLineStr = (String) textStrings.get(i);
            if (i > 0 && currentLineStr.contains("\n")) {
                editSelectIndex++;
            }
            if (i == textStrings.size() - 1) {
                editSelectIndex++;
            }
            currentLineStr = currentLineStr.replace("\n", "");
            float currentlinewidth = PdfSize2PageViewSize(pdfViewCtrl, pageIndex, paint.measureText(currentLineStr)) / 100.0f;
            if (new RectF(this.mStartPointX, this.mStartPointY + (getFontHeight(pdfViewCtrl, pageIndex, fontName, fontSize) * ((float) i)), this.mStartPointX + currentlinewidth, this.mStartPointY + (getFontHeight(pdfViewCtrl, pageIndex, fontName, fontSize) * ((float) (i + 1)))).contains(this.mEditPointX, this.mEditPointY)) {
                currentWidth = 0.0f;
                j = 0;
                while (j < currentLineStr.length()) {
                    float charWidth = paint.measureText(String.valueOf(currentLineStr.charAt(j)));
                    currentWidth += charWidth;
                    currentWidthPage = PdfSize2PageViewSize(pdfViewCtrl, pageIndex, currentWidth) / 100.0f;
                    float charWidthPage = PdfSize2PageViewSize(pdfViewCtrl, pageIndex, charWidth) / 100.0f;
                    if (this.mEditPointX >= (this.mStartPointX + currentWidthPage) - charWidthPage && this.mEditPointX < (this.mStartPointX + currentWidthPage) - (charWidthPage / 2.0f)) {
                        this.mEditPointX = (this.mStartPointX + currentWidthPage) - charWidthPage;
                        this.mEditPointY = this.mStartPointY + (this.mFontHeight * ((float) i));
                        this.mCurrentSelectIndex = editSelectIndex + j;
                        this.mTextValuesChangedListener.onCurrentSelectIndex(this.mCurrentSelectIndex);
                        this.mResetEdit = true;
                        this.mTextValuesChangedListener.onEditPointChanged(this.mEditPointX, this.mEditPointY);
                        return;
                    } else if (this.mEditPointX < (this.mStartPointX + currentWidthPage) - (charWidthPage / 2.0f) || this.mEditPointX >= this.mStartPointX + currentWidthPage) {
                        j++;
                    } else {
                        this.mEditPointX = this.mStartPointX + currentWidthPage;
                        this.mEditPointY = this.mStartPointY + (this.mFontHeight * ((float) i));
                        this.mCurrentSelectIndex = (editSelectIndex + j) + 1;
                        this.mTextValuesChangedListener.onCurrentSelectIndex(this.mCurrentSelectIndex);
                        this.mTextValuesChangedListener.onEditPointChanged(this.mEditPointX, this.mEditPointY);
                        this.mResetEdit = true;
                        return;
                    }
                }
                return;
            } else if (this.mEditPointY < this.mStartPointY + (getFontHeight(pdfViewCtrl, pageIndex, fontName, fontSize) * ((float) i)) || this.mEditPointY >= this.mStartPointY + (getFontHeight(pdfViewCtrl, pageIndex, fontName, fontSize) * ((float) (i + 1)))) {
                editSelectIndex += currentLineStr.length();
                i++;
            } else {
                this.mEditPointX = this.mStartPointX + currentlinewidth;
                this.mEditPointY = this.mStartPointY + (this.mFontHeight * ((float) i));
                this.mCurrentSelectIndex = currentLineStr.length() + editSelectIndex;
                if (this.mCurrentSelectIndex > this.mTextContent.length()) {
                    this.mCurrentSelectIndex = this.mTextContent.length();
                }
                this.mTextValuesChangedListener.onCurrentSelectIndex(this.mCurrentSelectIndex);
                this.mTextValuesChangedListener.onEditPointChanged(this.mEditPointX, this.mEditPointY);
                this.mResetEdit = true;
                return;
            }
        }
    }

    private void jumpPageWithHorizontal(float tempmaxWidth) {
        RectF rectF = new RectF(0.0f, 0.0f, tempmaxWidth, this.mFontHeight * ((float) this.mRealLine));
        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, this.mCurrentPageIndex);
        if (rectF.width() > this.mStartToBorderX && this.mEditState && getKeyboardHeight() > 0) {
            this.mPdfViewCtrl.gotoPage(this.mCurrentPageIndex, getPageViewOrigin(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStartPointX, this.mStartPointY).x + ((rectF.width() - this.mStartToBorderX) + 2.0f), getPageViewOrigin(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStartPointX, this.mStartPointY).y);
        }
    }

    private void jumpPageWithVertical(float tempmaxWidth) {
        RectF rectF = new RectF(0.0f, 0.0f, tempmaxWidth, this.mFontHeight * ((float) this.mRealLine));
        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, this.mCurrentPageIndex);
        if (this.mEditState && rectF.height() > this.mStartToBorderY - ((float) getKeyboardHeight()) && getKeyboardHeight() > AppDisplay.getInstance(this.mContext).getRawScreenHeight() / 5) {
            float offset = (rectF.height() - (this.mStartToBorderY - ((float) getKeyboardHeight()))) + 2.0f;
            this.mPageOffset += offset;
            if ((this.mCurrentPageIndex == this.mPdfViewCtrl.getPageCount() - 1 || this.mPdfViewCtrl.getPageLayoutMode() == 1) && this.mPageOffset < ((float) getKeyboardHeight())) {
                this.mPdfViewCtrl.layout(0, 0 - ((int) this.mPageOffset), this.mPdfViewCtrl.getWidth(), this.mPdfViewCtrl.getHeight() - ((int) this.mPageOffset));
            }
            this.mPdfViewCtrl.gotoPage(this.mCurrentPageIndex, getPageViewOrigin(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStartPointX, this.mStartPointY).x, getPageViewOrigin(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStartPointX, this.mStartPointY).y + offset);
        }
    }

    private void jumpPageWithKeyBoard() {
        PointF point = new PointF(this.mStartPointX, this.mStartPointY);
        this.mPdfViewCtrl.convertPageViewPtToDisplayViewPt(point, point, this.mCurrentPageIndex);
        if (this.mEditState && getKeyboardHeight() > AppDisplay.getInstance(this.mContext).getRawScreenHeight() / 5) {
            if (((float) AppDisplay.getInstance(this.mContext).getRawScreenHeight()) - ((((float) (this.mStringList.size() == 0 ? 1 : this.mStringList.size())) * this.mFontHeight) + (point.y - this.mPageOffset)) < ((float) getKeyboardHeight())) {
                if (this.mTextContent == null || this.mTextContent.equals("")) {
                    this.mPageOffset = ((((float) getKeyboardHeight()) + this.mFontHeight) - (((float) AppDisplay.getInstance(this.mContext).getRawScreenHeight()) - point.y)) + 2.0f;
                } else {
                    this.mPageOffset = ((((float) getKeyboardHeight()) + (this.mFontHeight * ((float) getComposedText(this.mPdfViewCtrl, this.mCurrentPageIndex, new RectF(0.0f, 0.0f, this.mMaxTextWidth, this.mMaxTextHeight), this.mTextContent, this.mFontName, this.mFontSize).size()))) - (((float) AppDisplay.getInstance(this.mContext).getRawScreenHeight()) - point.y)) + 2.0f;
                }
                if (this.mCurrentPageIndex == this.mPdfViewCtrl.getPageCount() - 1 || this.mPdfViewCtrl.getPageLayoutMode() == 1) {
                    this.mPdfViewCtrl.layout(0, 0 - ((int) this.mPageOffset), this.mPdfViewCtrl.getWidth(), this.mPdfViewCtrl.getHeight() - ((int) this.mPageOffset));
                }
                this.mPdfViewCtrl.gotoPage(this.mCurrentPageIndex, getPageViewOrigin(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStartPointX, this.mStartPointY).x, getPageViewOrigin(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStartPointX, this.mStartPointY).y + this.mPageOffset);
            }
        }
        if (this.mEditState && getKeyboardHeight() < AppDisplay.getInstance(this.mContext).getRawScreenHeight() / 5 && this.mCurrentPageIndex == this.mPdfViewCtrl.getPageCount() - 1 && this.mPageOffset > 0.0f) {
            this.mPdfViewCtrl.layout(0, 0, this.mPdfViewCtrl.getWidth(), this.mPdfViewCtrl.getHeight());
            this.mPageOffset = 0.0f;
            this.mPdfViewCtrl.gotoPage(this.mCurrentPageIndex, getPageViewOrigin(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStartPointX, this.mStartPointY).x, getPageViewOrigin(this.mPdfViewCtrl, this.mCurrentPageIndex, this.mStartPointX, this.mStartPointY).y);
        }
    }

    public void resetEditState() {
        this.mResetEdit = false;
    }

    public void DrawText(Canvas canvas) {
        Canvas canvas2;
        if (this.mTextContent != null && !this.mTextContent.equals("")) {
            this.mTempTextContent = this.mTextContent;
            if (this.mEditState && this.mInvalidate) {
                if (this.mEditPointX == 0.0f && this.mEditPointY == 0.0f) {
                    canvas2 = canvas;
                    canvas2.drawLine(this.mCurrentLineWidth + this.mStartPointX, (this.mFontHeight * ((float) (this.mRealLine - 1))) + this.mStartPointY, this.mCurrentLineWidth + this.mStartPointX, (this.mFontHeight * ((float) this.mRealLine)) + this.mStartPointY, this.mCursorPaint);
                } else {
                    canvas2 = canvas;
                    canvas2.drawLine(this.mEditPointX, this.mEditPointY, this.mEditPointX, this.mFontHeight + this.mEditPointY, this.mCursorPaint);
                }
            }
            int i = this.mCurrentLine;
            int j = 0;
            while (i < this.mRealLine && j <= this.mPageLineNum) {
                float textBaseY = ((this.mStartPointY + (this.mFontHeight * ((float) i))) + (this.mFontHeight / 2.0f)) - ((this.mFontMetrics.ascent + this.mFontMetrics.descent) / 2.0f);
                String drawStr = (String) this.mStringList.get(i);
                int count = drawStr.length();
                if (count > 0 && drawStr.charAt(count - 1) == '\n') {
                    StringBuffer buffer = new StringBuffer(drawStr);
                    buffer.deleteCharAt(buffer.length() - 1);
                    drawStr = buffer.toString();
                }
                canvas.drawText(drawStr, this.mStartPointX, textBaseY, this.mTextPaint);
                i++;
                j++;
            }
        } else if (this.mEditState && this.mInvalidate) {
            canvas2 = canvas;
            canvas2.drawLine(this.mCurrentLineWidth + this.mStartPointX, (this.mFontHeight * ((float) this.mRealLine)) + this.mStartPointY, this.mCurrentLineWidth + this.mStartPointX, (this.mFontHeight * ((float) (this.mRealLine + 1))) + this.mStartPointY, this.mCursorPaint);
        }
    }

    public PointF getPageViewOrigin(PDFViewCtrl pdfViewCtrl, int pageIndex, float x, float y) {
        PointF pagePt = new PointF(x, y);
        pdfViewCtrl.convertPageViewPtToDisplayViewPt(pagePt, pagePt, pageIndex);
        RectF rect = new RectF(0.0f, 0.0f, pagePt.x, pagePt.y);
        pdfViewCtrl.convertDisplayViewRectToPageViewRect(rect, rect, pageIndex);
        return new PointF(x - rect.width(), y - rect.height());
    }

    @SuppressLint({"NewApi"})
    private int getKeyboardHeight() {
        Rect r = new Rect();
        ((Activity) this.mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        return AppDisplay.getInstance(this.mContext).getRawScreenHeight() - r.bottom;
    }

    public Handler getBlink() {
        if (this.mBlink == null) {
            this.mBlink = new Blink();
        }
        return this.mBlink;
    }

    private void setPaintFont(Paint paint, String fontname) {
        if (fontname.equals("Courier")) {
            paint.setTypeface(Typeface.create(Typeface.MONOSPACE, 0));
        } else if (fontname.equals("Helvetica")) {
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, 0));
        } else if (fontname.equals("Times")) {
            paint.setTypeface(Typeface.create(Typeface.SERIF, 0));
        }
    }

    public float getFontHeight(PDFViewCtrl pdfViewCtrl, int pageIndex, String fontName, float fontSize) {
        setPaintFont(this.mTextPaint, fontName);
        this.mTextPaint.setTextSize(PdfSize2PageViewSize(pdfViewCtrl, pageIndex, fontSize));
        this.mFontMetrics = this.mTextPaint.getFontMetrics();
        return (float) ((int) Math.ceil((double) (this.mFontMetrics.descent - this.mFontMetrics.ascent)));
    }

    public float getFontWidth(PDFViewCtrl pdfViewCtrl, int pageIndex, String fontname, float fontsize) {
        return getFontHeight(pdfViewCtrl, pageIndex, fontname, fontsize);
    }

    private float PdfSize2PageViewSize(PDFViewCtrl pdfViewCtrl, int pageIndex, float PdfFontSize) {
        RectF rectF = new RectF(0.0f, 0.0f, PdfFontSize, PdfFontSize);
        pdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
        return rectF.width();
    }

    public void setKeyboardOffset(int keyboardOffset) {
        this.mPageOffset = (float) keyboardOffset;
    }

    public float getKeyboardOffset() {
        return this.mPageOffset;
    }

    public static String filterEmoji(String source) {
        if (!containsEmoji(source)) {
            return source;
        }
        StringBuilder buf = null;
        int len = source.length();
        for (int i = 0; i < len; i++) {
            if (!isEmojiCharacter(source.codePointAt(i))) {
                if (buf == null) {
                    buf = new StringBuilder(source.length());
                }
                buf.append(source.charAt(i));
            }
        }
        if (buf == null) {
            return "";
        }
        return buf.length() == len ? source : buf.toString();
    }

    private static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            if (isEmojiCharacter(source.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmojiCharacter(int codePoint) {
        return codePoint == 0 || codePoint == 9 || codePoint == 169 || codePoint == 174 || codePoint == 12349 || codePoint == 12336 || codePoint == 11093 || codePoint == 11036 || codePoint == 11035 || codePoint == 11088 || ((codePoint >= 127183 && codePoint <= 128696) || codePoint == 13 || codePoint == 56845 || ((codePoint >= 8448 && codePoint <= 10239) || ((codePoint >= 11013 && codePoint <= 11015) || ((codePoint >= 10548 && codePoint <= 10549) || ((codePoint >= 8252 && codePoint <= 8265) || ((codePoint >= 12951 && codePoint <= 12953) || ((codePoint >= 128512 && codePoint <= 128591) || (codePoint >= 56320 && codePoint <= 59000))))))));
    }
}
