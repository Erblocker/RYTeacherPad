package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.BarLineScatterCandleBubbleDataProvider;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.util.ArrayList;
import java.util.List;

public class CombinedChartRenderer extends DataRenderer {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$github$mikephil$charting$charts$CombinedChart$DrawOrder;
    protected List<DataRenderer> mRenderers;

    static /* synthetic */ int[] $SWITCH_TABLE$com$github$mikephil$charting$charts$CombinedChart$DrawOrder() {
        int[] iArr = $SWITCH_TABLE$com$github$mikephil$charting$charts$CombinedChart$DrawOrder;
        if (iArr == null) {
            iArr = new int[DrawOrder.values().length];
            try {
                iArr[DrawOrder.BAR.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DrawOrder.BUBBLE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DrawOrder.CANDLE.ordinal()] = 4;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DrawOrder.LINE.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[DrawOrder.SCATTER.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            $SWITCH_TABLE$com$github$mikephil$charting$charts$CombinedChart$DrawOrder = iArr;
        }
        return iArr;
    }

    public CombinedChartRenderer(CombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        createRenderers(chart, animator, viewPortHandler);
    }

    protected void createRenderers(CombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        this.mRenderers = new ArrayList();
        for (DrawOrder order : chart.getDrawOrder()) {
            switch ($SWITCH_TABLE$com$github$mikephil$charting$charts$CombinedChart$DrawOrder()[order.ordinal()]) {
                case 1:
                    if (chart.getBarData() == null) {
                        break;
                    }
                    this.mRenderers.add(new BarChartRenderer(chart, animator, viewPortHandler));
                    break;
                case 2:
                    if (chart.getBubbleData() == null) {
                        break;
                    }
                    this.mRenderers.add(new BubbleChartRenderer(chart, animator, viewPortHandler));
                    break;
                case 3:
                    if (chart.getLineData() == null) {
                        break;
                    }
                    this.mRenderers.add(new LineChartRenderer(chart, animator, viewPortHandler));
                    break;
                case 4:
                    if (chart.getCandleData() == null) {
                        break;
                    }
                    this.mRenderers.add(new CandleStickChartRenderer(chart, animator, viewPortHandler));
                    break;
                case 5:
                    if (chart.getScatterData() == null) {
                        break;
                    }
                    this.mRenderers.add(new ScatterChartRenderer(chart, animator, viewPortHandler));
                    break;
                default:
                    break;
            }
        }
    }

    public void initBuffers() {
        for (DataRenderer renderer : this.mRenderers) {
            renderer.initBuffers();
        }
    }

    public void drawData(Canvas c) {
        for (DataRenderer renderer : this.mRenderers) {
            renderer.drawData(c);
        }
    }

    public void drawValues(Canvas c) {
        for (DataRenderer renderer : this.mRenderers) {
            renderer.drawValues(c);
        }
    }

    public void drawExtras(Canvas c) {
        for (DataRenderer renderer : this.mRenderers) {
            renderer.drawExtras(c);
        }
    }

    public void drawHighlighted(Canvas c, Highlight[] indices) {
        for (DataRenderer renderer : this.mRenderers) {
            renderer.drawHighlighted(c, indices);
        }
    }

    public void calcXBounds(BarLineScatterCandleBubbleDataProvider chart, int xAxisModulus) {
        for (DataRenderer renderer : this.mRenderers) {
            renderer.calcXBounds(chart, xAxisModulus);
        }
    }

    public DataRenderer getSubRenderer(int index) {
        if (index >= this.mRenderers.size() || index < 0) {
            return null;
        }
        return (DataRenderer) this.mRenderers.get(index);
    }

    public List<DataRenderer> getSubRenderers() {
        return this.mRenderers;
    }

    public void setSubRenderers(List<DataRenderer> renderers) {
        this.mRenderers = renderers;
    }
}
