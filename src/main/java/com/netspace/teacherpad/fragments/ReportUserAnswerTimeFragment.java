package com.netspace.teacherpad.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.controls.ReportMarkerView;
import com.netspace.teacherpad.fragments.ReportUserAnswerStatisticFragment.ReportModuleInterface;
import com.netspace.teacherpad.modules.ReportModule_AnswerTime;
import java.util.ArrayList;
import java.util.Iterator;

public class ReportUserAnswerTimeFragment extends Fragment {
    private LineChart mChart;
    private Handler mHandler = new Handler();
    private View mRootView;
    private TextView mTextViewMessage;
    private ArrayList<ReportModuleInterface> marrReportModules;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.fragment_useranswertime, null);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.marrReportModules = new ArrayList();
        ReportModuleInterface oneModule = new ReportModule_AnswerTime();
        View chart = this.mRootView.findViewById(R.id.chartAnswerTime);
        TableLayout table = (TableLayout) this.mRootView.findViewById(R.id.chartAnswerTimeTable);
        this.mChart = (LineChart) chart;
        ReportMarkerView mv = new ReportMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChart(this.mChart);
        this.mChart.setMarkerView(mv);
        oneModule.setContext(getActivity());
        if (oneModule.initModule(null, null, chart, table)) {
            this.marrReportModules.add(oneModule);
        }
        if (TeacherPadApplication.marrStudentAnswers.size() == 0) {
            reportMessage("当前没有学生提交作答");
        }
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
    }

    public void onDetach() {
        super.onDetach();
    }

    public void refresh() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                Iterator it = ReportUserAnswerTimeFragment.this.marrReportModules.iterator();
                while (it.hasNext()) {
                    if (((ReportModuleInterface) it.next()).refresh()) {
                        ReportUserAnswerTimeFragment.this.mTextViewMessage.setVisibility(8);
                    }
                }
            }
        }, 100);
    }
}
