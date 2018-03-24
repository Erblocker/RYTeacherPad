package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendDirection;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.FSize;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LegendRenderer extends Renderer {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendForm;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendPosition;
    protected Legend mLegend;
    protected Paint mLegendFormPaint;
    protected Paint mLegendLabelPaint = new Paint(1);

    static /* synthetic */ int[] $SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendForm() {
        int[] iArr = $SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendForm;
        if (iArr == null) {
            iArr = new int[LegendForm.values().length];
            try {
                iArr[LegendForm.CIRCLE.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[LegendForm.LINE.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[LegendForm.SQUARE.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            $SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendForm = iArr;
        }
        return iArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendPosition() {
        int[] iArr = $SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendPosition;
        if (iArr == null) {
            iArr = new int[LegendPosition.values().length];
            try {
                iArr[LegendPosition.ABOVE_CHART_CENTER.ordinal()] = 12;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[LegendPosition.ABOVE_CHART_LEFT.ordinal()] = 10;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[LegendPosition.ABOVE_CHART_RIGHT.ordinal()] = 11;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[LegendPosition.BELOW_CHART_CENTER.ordinal()] = 9;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[LegendPosition.BELOW_CHART_LEFT.ordinal()] = 7;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[LegendPosition.BELOW_CHART_RIGHT.ordinal()] = 8;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[LegendPosition.LEFT_OF_CHART.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[LegendPosition.LEFT_OF_CHART_CENTER.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[LegendPosition.LEFT_OF_CHART_INSIDE.ordinal()] = 6;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[LegendPosition.PIECHART_CENTER.ordinal()] = 13;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[LegendPosition.RIGHT_OF_CHART.ordinal()] = 1;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[LegendPosition.RIGHT_OF_CHART_CENTER.ordinal()] = 2;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[LegendPosition.RIGHT_OF_CHART_INSIDE.ordinal()] = 3;
            } catch (NoSuchFieldError e13) {
            }
            $SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendPosition = iArr;
        }
        return iArr;
    }

    public LegendRenderer(ViewPortHandler viewPortHandler, Legend legend) {
        super(viewPortHandler);
        this.mLegend = legend;
        this.mLegendLabelPaint.setTextSize(Utils.convertDpToPixel(9.0f));
        this.mLegendLabelPaint.setTextAlign(Align.LEFT);
        this.mLegendFormPaint = new Paint(1);
        this.mLegendFormPaint.setStyle(Style.FILL);
        this.mLegendFormPaint.setStrokeWidth(3.0f);
    }

    public Paint getLabelPaint() {
        return this.mLegendLabelPaint;
    }

    public Paint getFormPaint() {
        return this.mLegendFormPaint;
    }

    public void computeLegend(ChartData<?> data) {
        if (!this.mLegend.isLegendCustom()) {
            List<String> labels = new ArrayList();
            List<Integer> colors = new ArrayList();
            for (int i = 0; i < data.getDataSetCount(); i++) {
                DataSet<? extends Entry> dataSet = data.getDataSetByIndex(i);
                List<Integer> clrs = dataSet.getColors();
                int entryCount = dataSet.getEntryCount();
                int j;
                if ((dataSet instanceof BarDataSet) && ((BarDataSet) dataSet).isStacked()) {
                    BarDataSet bds = (BarDataSet) dataSet;
                    String[] sLabels = bds.getStackLabels();
                    j = 0;
                    while (j < clrs.size() && j < bds.getStackSize()) {
                        labels.add(sLabels[j % sLabels.length]);
                        colors.add((Integer) clrs.get(j));
                        j++;
                    }
                    if (bds.getLabel() != null) {
                        colors.add(Integer.valueOf(-2));
                        labels.add(bds.getLabel());
                    }
                } else if (dataSet instanceof PieDataSet) {
                    List<String> xVals = data.getXVals();
                    PieDataSet pds = (PieDataSet) dataSet;
                    j = 0;
                    while (j < clrs.size() && j < entryCount && j < xVals.size()) {
                        labels.add((String) xVals.get(j));
                        colors.add((Integer) clrs.get(j));
                        j++;
                    }
                    if (pds.getLabel() != null) {
                        colors.add(Integer.valueOf(-2));
                        labels.add(pds.getLabel());
                    }
                } else {
                    j = 0;
                    while (j < clrs.size() && j < entryCount) {
                        if (j >= clrs.size() - 1 || j >= entryCount - 1) {
                            labels.add(data.getDataSetByIndex(i).getLabel());
                        } else {
                            labels.add(null);
                        }
                        colors.add((Integer) clrs.get(j));
                        j++;
                    }
                }
            }
            if (!(this.mLegend.getExtraColors() == null || this.mLegend.getExtraLabels() == null)) {
                for (int color : this.mLegend.getExtraColors()) {
                    colors.add(Integer.valueOf(color));
                }
                Collections.addAll(labels, this.mLegend.getExtraLabels());
            }
            this.mLegend.setComputedColors(colors);
            this.mLegend.setComputedLabels(labels);
        }
        Typeface tf = this.mLegend.getTypeface();
        if (tf != null) {
            this.mLegendLabelPaint.setTypeface(tf);
        }
        this.mLegendLabelPaint.setTextSize(this.mLegend.getTextSize());
        this.mLegendLabelPaint.setColor(this.mLegend.getTextColor());
        this.mLegend.calculateDimensions(this.mLegendLabelPaint, this.mViewPortHandler);
    }

    public void renderLegend(Canvas c) {
        if (this.mLegend.isEnabled()) {
            Typeface tf = this.mLegend.getTypeface();
            if (tf != null) {
                this.mLegendLabelPaint.setTypeface(tf);
            }
            this.mLegendLabelPaint.setTextSize(this.mLegend.getTextSize());
            this.mLegendLabelPaint.setColor(this.mLegend.getTextColor());
            float labelLineHeight = Utils.getLineHeight(this.mLegendLabelPaint);
            float labelLineSpacing = Utils.getLineSpacing(this.mLegendLabelPaint) + this.mLegend.getYEntrySpace();
            float formYOffset = labelLineHeight - (((float) Utils.calcTextHeight(this.mLegendLabelPaint, "ABC")) / 2.0f);
            String[] labels = this.mLegend.getLabels();
            int[] colors = this.mLegend.getColors();
            float formToTextSpace = this.mLegend.getFormToTextSpace();
            float xEntrySpace = this.mLegend.getXEntrySpace();
            LegendDirection direction = this.mLegend.getDirection();
            float formSize = this.mLegend.getFormSize();
            float stackSpace = this.mLegend.getStackSpace();
            float yoffset = this.mLegend.getYOffset();
            float xoffset = this.mLegend.getXOffset();
            LegendPosition legendPosition = this.mLegend.getPosition();
            float f;
            float posX;
            float posY;
            int i;
            switch ($SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendPosition()[legendPosition.ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 13:
                    float stack = 0.0f;
                    boolean wasStacked = false;
                    if (legendPosition == LegendPosition.PIECHART_CENTER) {
                        float chartWidth = this.mViewPortHandler.getChartWidth() / 2.0f;
                        if (direction == LegendDirection.LEFT_TO_RIGHT) {
                            f = (-this.mLegend.mTextWidthMax) / 2.0f;
                        } else {
                            f = this.mLegend.mTextWidthMax / 2.0f;
                        }
                        posX = chartWidth + f;
                        posY = ((this.mViewPortHandler.getChartHeight() / 2.0f) - (this.mLegend.mNeededHeight / 2.0f)) + this.mLegend.getYOffset();
                    } else {
                        boolean isRightAligned = legendPosition == LegendPosition.RIGHT_OF_CHART || legendPosition == LegendPosition.RIGHT_OF_CHART_CENTER || legendPosition == LegendPosition.RIGHT_OF_CHART_INSIDE;
                        if (isRightAligned) {
                            posX = this.mViewPortHandler.getChartWidth() - xoffset;
                            if (direction == LegendDirection.LEFT_TO_RIGHT) {
                                posX -= this.mLegend.mTextWidthMax;
                            }
                        } else {
                            posX = xoffset;
                            if (direction == LegendDirection.RIGHT_TO_LEFT) {
                                posX += this.mLegend.mTextWidthMax;
                            }
                        }
                        if (legendPosition == LegendPosition.RIGHT_OF_CHART || legendPosition == LegendPosition.LEFT_OF_CHART) {
                            posY = this.mViewPortHandler.contentTop() + yoffset;
                        } else if (legendPosition == LegendPosition.RIGHT_OF_CHART_CENTER || legendPosition == LegendPosition.LEFT_OF_CHART_CENTER) {
                            posY = (this.mViewPortHandler.getChartHeight() / 2.0f) - (this.mLegend.mNeededHeight / 2.0f);
                        } else {
                            posY = this.mViewPortHandler.contentTop() + yoffset;
                        }
                    }
                    for (i = 0; i < labels.length; i++) {
                        Boolean drawingForm = Boolean.valueOf(colors[i] != -2);
                        float x = posX;
                        if (drawingForm.booleanValue()) {
                            if (direction == LegendDirection.LEFT_TO_RIGHT) {
                                x += stack;
                            } else {
                                x -= formSize - stack;
                            }
                            drawForm(c, x, posY + formYOffset, i, this.mLegend);
                            if (direction == LegendDirection.LEFT_TO_RIGHT) {
                                x += formSize;
                            }
                        }
                        if (labels[i] != null) {
                            if (drawingForm.booleanValue() && !wasStacked) {
                                if (direction == LegendDirection.LEFT_TO_RIGHT) {
                                    f = formToTextSpace;
                                } else {
                                    f = -formToTextSpace;
                                }
                                x += f;
                            } else if (wasStacked) {
                                x = posX;
                            }
                            if (direction == LegendDirection.RIGHT_TO_LEFT) {
                                x -= (float) Utils.calcTextWidth(this.mLegendLabelPaint, labels[i]);
                            }
                            if (wasStacked) {
                                posY += labelLineHeight + labelLineSpacing;
                                drawLabel(c, x, posY + labelLineHeight, labels[i]);
                            } else {
                                drawLabel(c, x, posY + labelLineHeight, labels[i]);
                            }
                            posY += labelLineHeight + labelLineSpacing;
                            stack = 0.0f;
                        } else {
                            stack += formSize + stackSpace;
                            wasStacked = true;
                        }
                    }
                    return;
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                    float originPosX;
                    float contentWidth = this.mViewPortHandler.contentWidth();
                    if (legendPosition == LegendPosition.BELOW_CHART_LEFT || legendPosition == LegendPosition.ABOVE_CHART_LEFT) {
                        originPosX = this.mViewPortHandler.contentLeft() + xoffset;
                        if (direction == LegendDirection.RIGHT_TO_LEFT) {
                            originPosX += this.mLegend.mNeededWidth;
                        }
                    } else if (legendPosition == LegendPosition.BELOW_CHART_RIGHT || legendPosition == LegendPosition.ABOVE_CHART_RIGHT) {
                        originPosX = this.mViewPortHandler.contentRight() - xoffset;
                        if (direction == LegendDirection.LEFT_TO_RIGHT) {
                            originPosX -= this.mLegend.mNeededWidth;
                        }
                    } else {
                        originPosX = this.mViewPortHandler.contentLeft() + (contentWidth / 2.0f);
                    }
                    FSize[] calculatedLineSizes = this.mLegend.getCalculatedLineSizes();
                    FSize[] calculatedLabelSizes = this.mLegend.getCalculatedLabelSizes();
                    Boolean[] calculatedLabelBreakPoints = this.mLegend.getCalculatedLabelBreakPoints();
                    posX = originPosX;
                    if (legendPosition == LegendPosition.ABOVE_CHART_LEFT || legendPosition == LegendPosition.ABOVE_CHART_RIGHT || legendPosition == LegendPosition.ABOVE_CHART_CENTER) {
                        posY = 0.0f;
                    } else {
                        posY = (this.mViewPortHandler.getChartHeight() - yoffset) - this.mLegend.mNeededHeight;
                    }
                    int lineIndex = 0;
                    i = 0;
                    int count = labels.length;
                    while (i < count) {
                        if (i < calculatedLabelBreakPoints.length && calculatedLabelBreakPoints[i].booleanValue()) {
                            posX = originPosX;
                            posY += labelLineHeight + labelLineSpacing;
                        }
                        if (posX == originPosX && legendPosition == LegendPosition.BELOW_CHART_CENTER && lineIndex < calculatedLineSizes.length) {
                            posX += (direction == LegendDirection.RIGHT_TO_LEFT ? calculatedLineSizes[lineIndex].width : -calculatedLineSizes[lineIndex].width) / 2.0f;
                            lineIndex++;
                        }
                        boolean drawingForm2 = colors[i] != -2;
                        boolean isStacked = labels[i] == null;
                        if (drawingForm2) {
                            if (direction == LegendDirection.RIGHT_TO_LEFT) {
                                posX -= formSize;
                            }
                            drawForm(c, posX, posY + formYOffset, i, this.mLegend);
                            if (direction == LegendDirection.LEFT_TO_RIGHT) {
                                posX += formSize;
                            }
                        }
                        if (isStacked) {
                            posX += direction == LegendDirection.RIGHT_TO_LEFT ? -stackSpace : stackSpace;
                        } else {
                            if (drawingForm2) {
                                if (direction == LegendDirection.RIGHT_TO_LEFT) {
                                    f = -formToTextSpace;
                                } else {
                                    f = formToTextSpace;
                                }
                                posX += f;
                            }
                            if (direction == LegendDirection.RIGHT_TO_LEFT) {
                                posX -= calculatedLabelSizes[i].width;
                            }
                            drawLabel(c, posX, posY + labelLineHeight, labels[i]);
                            if (direction == LegendDirection.LEFT_TO_RIGHT) {
                                posX += calculatedLabelSizes[i].width;
                            }
                            if (direction == LegendDirection.RIGHT_TO_LEFT) {
                                f = -xEntrySpace;
                            } else {
                                f = xEntrySpace;
                            }
                            posX += f;
                        }
                        i++;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    protected void drawForm(Canvas c, float x, float y, int index, Legend legend) {
        if (legend.getColors()[index] != -2) {
            this.mLegendFormPaint.setColor(legend.getColors()[index]);
            float formsize = legend.getFormSize();
            float half = formsize / 2.0f;
            switch ($SWITCH_TABLE$com$github$mikephil$charting$components$Legend$LegendForm()[legend.getForm().ordinal()]) {
                case 1:
                    c.drawRect(x, y - half, x + formsize, y + half, this.mLegendFormPaint);
                    return;
                case 2:
                    c.drawCircle(x + half, y, half, this.mLegendFormPaint);
                    return;
                case 3:
                    c.drawLine(x, y, x + formsize, y, this.mLegendFormPaint);
                    return;
                default:
                    return;
            }
        }
    }

    protected void drawLabel(Canvas c, float x, float y, String label) {
        c.drawText(label, x, y, this.mLegendLabelPaint);
    }
}
