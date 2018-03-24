package com.netspace.teacherpad.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenu;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.adapter.SimplePagesAdapter;
import com.netspace.teacherpad.adapter.SimplePagesAdapter.ViewPagerReadyInterface;
import com.netspace.teacherpad.controls.CustomViewPager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class ReportDialog extends Dialog implements ViewPagerReadyInterface {
    private SimplePagesAdapter m_Adapter;
    private GraphicalView m_ChartView1;
    private GraphicalView m_ChartView2;
    private GraphicalView m_ChartView3;
    private Context m_Context;
    private LinkedHashMap m_arrAnswerTime;
    private XYMultipleSeriesRenderer m_rendererAnswerTime;
    private XYMultipleSeriesRenderer m_rendererAnswers;
    private XYMultipleSeriesRenderer m_rendererWrongCorrect;

    public ReportDialog(Context context) {
        super(context);
        this.m_Context = context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(128, 128);
        setContentView(R.layout.popup_report);
        ArrayList<Integer> arrPages = new ArrayList();
        arrPages.add(Integer.valueOf(R.layout.page_empty1));
        arrPages.add(Integer.valueOf(R.layout.page_empty2));
        arrPages.add(Integer.valueOf(R.layout.page_empty3));
        ArrayList<TextView> arrPageLabels = new ArrayList();
        arrPageLabels.add((TextView) findViewById(R.id.textViewAnswerTime));
        arrPageLabels.add((TextView) findViewById(R.id.textViewAnswers));
        arrPageLabels.add((TextView) findViewById(R.id.textViewAnswerCorrectRate));
        CustomViewPager m_myPager = (CustomViewPager) findViewById(R.id.pageSelector);
        m_myPager.setPagingEnabled(false);
        this.m_Adapter = new SimplePagesAdapter(arrPages, arrPageLabels, m_myPager, this);
        m_myPager.setAdapter(this.m_Adapter);
        m_myPager.setOnPageChangeListener(this.m_Adapter);
        m_myPager.setCurrentItem(0);
        this.m_Adapter.onPageSelected(m_myPager.getCurrentItem());
        HashMap<String, Integer> mapStudentsAnswerTime = new HashMap();
        this.m_arrAnswerTime = sortHashMapByValuesD((HashMap) TeacherPadApplication.mapStudentsAnswerTime.clone());
        this.m_rendererAnswerTime = getRendererAnswerTime();
        this.m_rendererWrongCorrect = getRendererWrongCorrect();
        this.m_rendererAnswers = getRendererAnswers();
    }

    public LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        LinkedHashMap sortedMap = new LinkedHashMap();
        for (Object val : mapValues) {
            for (Object key : mapKeys) {
                if (passedMap.get(key).toString().equals(val.toString())) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String) key, (Integer) val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    private XYMultipleSeriesRenderer getRendererWrongCorrect() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(16.0f);
        renderer.setChartTitleTextSize(20.0f);
        renderer.setLabelsTextSize(15.0f);
        renderer.setLegendTextSize(15.0f);
        renderer.setPointSize(5.0f);
        renderer.setMargins(new int[]{20, 50, 15, 10});
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(-16711936);
        r.setPointStyle(PointStyle.SQUARE);
        r.setFillBelowLine(true);
        r.setFillBelowLineColor(-1);
        r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        r = new XYSeriesRenderer();
        r.setPointStyle(PointStyle.CIRCLE);
        r.setColor(SupportMenu.CATEGORY_MASK);
        r.setFillPoints(true);
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(-1);
        renderer.setMarginsColor(-1);
        renderer.addSeriesRenderer(r);
        renderer.setAxesColor(-16777216);
        renderer.setLabelsColor(-16777216);
        renderer.setYLabelsPadding(30.0f);
        renderer.setDisplayValues(true);
        renderer.setZoomInLimitY(0.0d);
        renderer.setYTitle("数量");
        renderer.setYLabelsAlign(Align.LEFT);
        renderer.setXLabels(0);
        renderer.setYLabelsColor(0, -16777216);
        renderer.setPanEnabled(false, true);
        renderer.setShowGrid(true);
        renderer.setPanLimits(new double[]{0.0d, 2.0d, 0.0d, (double) TeacherPadApplication.nStudentsCount});
        renderer.setZoomLimits(new double[]{0.0d, 1.0d, 0.0d, (double) TeacherPadApplication.nStudentsCount});
        renderer.setZoomEnabled(false, true);
        renderer.setYLabelsPadding(20.0f);
        renderer.setBarWidth(100.0f);
        renderer.setXAxisMin(0.5d);
        renderer.setXAxisMax(1.5d);
        renderer.setYAxisMin(0.0d);
        renderer.setYAxisMax((double) TeacherPadApplication.nStudentsCount);
        return renderer;
    }

    private XYMultipleSeriesRenderer getRendererAnswerTime() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(16.0f);
        renderer.setChartTitleTextSize(20.0f);
        renderer.setLabelsTextSize(15.0f);
        renderer.setLegendTextSize(15.0f);
        renderer.setPointSize(5.0f);
        renderer.setMargins(new int[]{20, 50, 15, 10});
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(-16776961);
        r.setPointStyle(PointStyle.SQUARE);
        r.setFillBelowLine(true);
        r.setFillBelowLineColor(-1);
        r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(-1);
        renderer.setMarginsColor(-1);
        renderer.setAxesColor(-16777216);
        renderer.setLabelsColor(-16777216);
        renderer.setYLabelsPadding(30.0f);
        renderer.setDisplayValues(true);
        renderer.setZoomInLimitY(0.0d);
        renderer.setYLabelsColor(0, -16777216);
        renderer.setYTitle("回答时间（秒）");
        renderer.setDisplayValues(true);
        renderer.setShowLegend(false);
        renderer.setShowGrid(true);
        renderer.setYLabelsAlign(Align.LEFT);
        renderer.setPanLimits(new double[]{1.0d, (double) TeacherPadApplication.nStudentsCount, 0.0d, 200.0d});
        renderer.setInitialRange(new double[]{1.0d, (double) TeacherPadApplication.nStudentsCount, 0.0d, 10.0d});
        renderer.setYLabelsPadding(20.0f);
        renderer.setBarWidth(100.0f);
        renderer.setXTitle("学生姓名");
        renderer.setXLabels(0);
        renderer.setXLabelsColor(-16777216);
        int nIndex = 1;
        for (String key : this.m_arrAnswerTime.keySet()) {
            String szName = (String) TeacherPadApplication.mapStudentName.get(key);
            if (szName != null) {
                renderer.addXTextLabel((double) nIndex, szName);
            }
            nIndex++;
        }
        return renderer;
    }

    private XYMultipleSeriesRenderer getRendererAnswers() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(16.0f);
        renderer.setChartTitleTextSize(20.0f);
        renderer.setLabelsTextSize(15.0f);
        renderer.setLegendTextSize(15.0f);
        renderer.setPointSize(5.0f);
        renderer.setMargins(new int[]{20, 50, 15, 10});
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(SupportMenu.CATEGORY_MASK);
        r.setPointStyle(PointStyle.SQUARE);
        r.setFillBelowLine(true);
        r.setFillBelowLineColor(-1);
        r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(-1);
        renderer.setMarginsColor(-1);
        renderer.setAxesColor(-16777216);
        renderer.setLabelsColor(-16777216);
        renderer.setYLabelsPadding(30.0f);
        renderer.setDisplayValues(true);
        renderer.setZoomInLimitY(0.0d);
        renderer.setYLabelsColor(0, -16777216);
        renderer.setYTitle("数量");
        renderer.setDisplayValues(true);
        renderer.setShowLegend(false);
        renderer.setShowGrid(true);
        renderer.setShowGridX(true);
        renderer.setYLabelsAlign(Align.LEFT);
        renderer.setInitialRange(new double[]{-1.0d, 5.0d, -1.0d, 10.0d});
        renderer.setYLabelsPadding(20.0f);
        renderer.setBarWidth(100.0f);
        renderer.setXTitle("学生答案");
        renderer.setXLabels(0);
        renderer.setXLabelsColor(-16777216);
        int nIndex = 1;
        HashMap<String, String> mapAnswerExsitTest = new HashMap();
        for (String key : this.m_arrAnswerTime.keySet()) {
            String szAnswer = (String) TeacherPadApplication.mapStudentsQuestionAnswers.get(key);
            if (!mapAnswerExsitTest.containsKey(szAnswer)) {
                if (szAnswer.equalsIgnoreCase(TeacherPadApplication.szCorrectAnswer)) {
                    renderer.addXTextLabel((double) nIndex, new StringBuilder(String.valueOf(szAnswer)).append("(正确答案)").toString());
                } else {
                    renderer.addXTextLabel((double) nIndex, szAnswer);
                }
                nIndex++;
                mapAnswerExsitTest.put(szAnswer, "");
            }
        }
        return renderer;
    }

    private XYMultipleSeriesDataset getCorrectWrongDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int nCorrectCount = 0;
        int nWrongCount = 0;
        for (String key : TeacherPadApplication.mapStudentsQuestionAnswers.keySet()) {
            String szOneAnswer = (String) TeacherPadApplication.mapStudentsQuestionAnswers.get(key);
            if (szOneAnswer.length() < 10) {
                if (szOneAnswer.equalsIgnoreCase(TeacherPadApplication.szCorrectAnswer)) {
                    nCorrectCount++;
                } else {
                    nWrongCount++;
                }
            }
        }
        if (nCorrectCount <= 0 && nWrongCount <= 0) {
            return null;
        }
        CategorySeries CorrectSeries = new CategorySeries("回答正确(" + nCorrectCount + "个)");
        CorrectSeries.add((double) nCorrectCount);
        CategorySeries WrongSeries = new CategorySeries("回答错误(" + nWrongCount + "个)");
        WrongSeries.add((double) nWrongCount);
        dataset.addSeries(CorrectSeries.toXYSeries());
        dataset.addSeries(WrongSeries.toXYSeries());
        return dataset;
    }

    private XYMultipleSeriesDataset getAnswerTimeDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int nIndex = 1;
        XYSeries Series = new XYSeries("数量");
        for (String key : this.m_arrAnswerTime.keySet()) {
            Series.add((double) nIndex, ((double) ((Integer) this.m_arrAnswerTime.get(key)).intValue()) / 1000.0d);
            nIndex++;
        }
        dataset.addSeries(Series);
        return dataset;
    }

    private XYMultipleSeriesDataset getAnswersDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeries Series = new XYSeries("答案情况");
        HashMap<String, Integer> mapAnswerExsitTest = new HashMap();
        ArrayList<String> arrAnswers = new ArrayList();
        ArrayList<Integer> arrCount = new ArrayList();
        for (String key : this.m_arrAnswerTime.keySet()) {
            String szAnswer = (String) TeacherPadApplication.mapStudentsQuestionAnswers.get(key);
            if (szAnswer.length() <= 10) {
                if (mapAnswerExsitTest.containsKey(szAnswer)) {
                    int nIndex = ((Integer) mapAnswerExsitTest.get(szAnswer)).intValue();
                    arrCount.set(nIndex, Integer.valueOf(((Integer) arrCount.get(nIndex)).intValue() + 1));
                } else {
                    arrAnswers.add(szAnswer);
                    arrCount.add(Integer.valueOf(1));
                    mapAnswerExsitTest.put(szAnswer, Integer.valueOf(arrAnswers.size() - 1));
                }
            }
        }
        for (int i = 0; i < arrAnswers.size(); i++) {
            Series.add((double) (i + 1), (double) ((Integer) arrCount.get(i)).intValue());
        }
        dataset.addSeries(Series);
        return arrAnswers.size() > 0 ? dataset : null;
    }

    public void OnPageInstantiated(int nPosition) {
        XYMultipleSeriesDataset DataSet;
        LinearLayout layout;
        TextView NoDataLabel;
        if (nPosition == 0) {
            if (this.m_ChartView1 == null) {
                DataSet = getAnswerTimeDataset();
                layout = (LinearLayout) findViewById(R.id.Layout1);
                if (DataSet != null) {
                    this.m_ChartView1 = ChartFactory.getLineChartView(this.m_Context, DataSet, this.m_rendererAnswerTime);
                    layout.addView(this.m_ChartView1);
                    return;
                }
                NoDataLabel = new TextView(this.m_Context);
                NoDataLabel.setText("没有数据，可能是主观题没有正确答案或者没有学生作答。");
                layout.addView(NoDataLabel);
                return;
            }
            this.m_ChartView1.repaint();
        } else if (nPosition == 1) {
            if (this.m_ChartView2 == null) {
                DataSet = getAnswersDataset();
                layout = (LinearLayout) findViewById(R.id.Layout2);
                if (DataSet != null) {
                    this.m_ChartView2 = ChartFactory.getLineChartView(this.m_Context, DataSet, this.m_rendererAnswers);
                    layout.addView(this.m_ChartView2);
                    return;
                }
                NoDataLabel = new TextView(this.m_Context);
                NoDataLabel.setText("没有数据，可能是主观题没有正确答案或者没有学生作答。");
                layout.addView(NoDataLabel);
                return;
            }
            this.m_ChartView2.repaint();
        } else if (nPosition != 2) {
        } else {
            if (this.m_ChartView3 == null) {
                DataSet = getCorrectWrongDataset();
                layout = (LinearLayout) findViewById(R.id.Layout3);
                if (DataSet != null) {
                    this.m_ChartView3 = ChartFactory.getBarChartView(this.m_Context, DataSet, this.m_rendererWrongCorrect, Type.DEFAULT);
                    layout.addView(this.m_ChartView3);
                    return;
                }
                NoDataLabel = new TextView(this.m_Context);
                NoDataLabel.setText("没有数据，可能是主观题没有正确答案或者没有学生作答。");
                layout.addView(NoDataLabel);
                return;
            }
            this.m_ChartView3.repaint();
        }
    }
}
