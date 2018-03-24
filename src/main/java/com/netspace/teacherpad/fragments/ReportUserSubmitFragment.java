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
import com.netspace.teacherpad.controls.ReportMarkerView;
import com.netspace.teacherpad.fragments.ReportUserAnswerStatisticFragment.ReportModuleInterface;
import com.netspace.teacherpad.modules.ReportModule_AnswerTime;
import com.netspace.teacherpad.modules.ReportModule_UserSubmit;
import java.util.ArrayList;
import java.util.Iterator;

public class ReportUserSubmitFragment extends Fragment {
    private Handler mHandler = new Handler();
    private View mRootView;
    private TextView mTextViewMessage;
    private ArrayList<ReportModuleInterface> marrReportModules;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.fragment_usersubmit, null);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.marrReportModules = new ArrayList();
        ReportModuleInterface oneModuleAnswerTime = new ReportModule_AnswerTime();
        View chartAnswerTime = this.mRootView.findViewById(R.id.chartAnswerTime);
        TableLayout tableAnswerTime = (TableLayout) this.mRootView.findViewById(R.id.chartAnswerTimeTable);
        LineChart LineChart = (LineChart) chartAnswerTime;
        ReportMarkerView mv = new ReportMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChart(LineChart);
        LineChart.setMarkerView(mv);
        oneModuleAnswerTime.setContext(getActivity());
        if (oneModuleAnswerTime.initModule(null, null, chartAnswerTime, tableAnswerTime)) {
            this.marrReportModules.add(oneModuleAnswerTime);
        }
        ReportModuleInterface oneModule = new ReportModule_UserSubmit();
        View chart = this.mRootView.findViewById(R.id.chartUserSubmit);
        TableLayout table = (TableLayout) this.mRootView.findViewById(R.id.chartUserSubmitTable);
        oneModule.setContext(getActivity());
        if (oneModule.initModule(null, null, chart, table)) {
            this.marrReportModules.add(oneModule);
        }
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
    }

    public void refresh() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (ReportUserSubmitFragment.this.marrReportModules != null) {
                    Iterator it = ReportUserSubmitFragment.this.marrReportModules.iterator();
                    while (it.hasNext()) {
                        ((ReportModuleInterface) it.next()).refresh();
                    }
                }
            }
        }, 100);
    }
}
