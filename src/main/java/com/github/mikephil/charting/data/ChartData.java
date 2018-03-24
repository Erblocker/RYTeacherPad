package com.github.mikephil.charting.data;

import android.graphics.Typeface;
import android.util.Log;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ChartData<T extends DataSet<? extends Entry>> {
    protected List<T> mDataSets;
    protected int mLastEnd;
    protected int mLastStart;
    protected float mLeftAxisMax;
    protected float mLeftAxisMin;
    protected float mRightAxisMax;
    protected float mRightAxisMin;
    private float mXValAverageLength;
    protected List<String> mXVals;
    protected float mYMax;
    protected float mYMin;
    private int mYValCount;
    private float mYValueSum;

    public ChartData() {
        this.mYMax = 0.0f;
        this.mYMin = 0.0f;
        this.mLeftAxisMax = 0.0f;
        this.mLeftAxisMin = 0.0f;
        this.mRightAxisMax = 0.0f;
        this.mRightAxisMin = 0.0f;
        this.mYValueSum = 0.0f;
        this.mYValCount = 0;
        this.mLastStart = 0;
        this.mLastEnd = 0;
        this.mXValAverageLength = 0.0f;
        this.mXVals = new ArrayList();
        this.mDataSets = new ArrayList();
    }

    public ChartData(List<String> xVals) {
        this.mYMax = 0.0f;
        this.mYMin = 0.0f;
        this.mLeftAxisMax = 0.0f;
        this.mLeftAxisMin = 0.0f;
        this.mRightAxisMax = 0.0f;
        this.mRightAxisMin = 0.0f;
        this.mYValueSum = 0.0f;
        this.mYValCount = 0;
        this.mLastStart = 0;
        this.mLastEnd = 0;
        this.mXValAverageLength = 0.0f;
        this.mXVals = xVals;
        this.mDataSets = new ArrayList();
        init();
    }

    public ChartData(String[] xVals) {
        this.mYMax = 0.0f;
        this.mYMin = 0.0f;
        this.mLeftAxisMax = 0.0f;
        this.mLeftAxisMin = 0.0f;
        this.mRightAxisMax = 0.0f;
        this.mRightAxisMin = 0.0f;
        this.mYValueSum = 0.0f;
        this.mYValCount = 0;
        this.mLastStart = 0;
        this.mLastEnd = 0;
        this.mXValAverageLength = 0.0f;
        this.mXVals = arrayToList(xVals);
        this.mDataSets = new ArrayList();
        init();
    }

    public ChartData(List<String> xVals, List<T> sets) {
        this.mYMax = 0.0f;
        this.mYMin = 0.0f;
        this.mLeftAxisMax = 0.0f;
        this.mLeftAxisMin = 0.0f;
        this.mRightAxisMax = 0.0f;
        this.mRightAxisMin = 0.0f;
        this.mYValueSum = 0.0f;
        this.mYValCount = 0;
        this.mLastStart = 0;
        this.mLastEnd = 0;
        this.mXValAverageLength = 0.0f;
        this.mXVals = xVals;
        this.mDataSets = sets;
        init();
    }

    public ChartData(String[] xVals, List<T> sets) {
        this.mYMax = 0.0f;
        this.mYMin = 0.0f;
        this.mLeftAxisMax = 0.0f;
        this.mLeftAxisMin = 0.0f;
        this.mRightAxisMax = 0.0f;
        this.mRightAxisMin = 0.0f;
        this.mYValueSum = 0.0f;
        this.mYValCount = 0;
        this.mLastStart = 0;
        this.mLastEnd = 0;
        this.mXValAverageLength = 0.0f;
        this.mXVals = arrayToList(xVals);
        this.mDataSets = sets;
        init();
    }

    private List<String> arrayToList(String[] array) {
        return Arrays.asList(array);
    }

    protected void init() {
        checkLegal();
        calcMinMax(this.mLastStart, this.mLastEnd);
        calcYValueSum();
        calcYValueCount();
        calcXValAverageLength();
    }

    private void calcXValAverageLength() {
        if (this.mXVals.size() <= 0) {
            this.mXValAverageLength = 1.0f;
            return;
        }
        float sum = 1.0f;
        for (int i = 0; i < this.mXVals.size(); i++) {
            sum += (float) ((String) this.mXVals.get(i)).length();
        }
        this.mXValAverageLength = sum / ((float) this.mXVals.size());
    }

    private void checkLegal() {
        if (this.mDataSets != null && !(this instanceof ScatterData)) {
            for (int i = 0; i < this.mDataSets.size(); i++) {
                if (((DataSet) this.mDataSets.get(i)).getYVals().size() > this.mXVals.size()) {
                    throw new IllegalArgumentException("One or more of the DataSet Entry arrays are longer than the x-values array of this ChartData object.");
                }
            }
        }
    }

    public void notifyDataChanged() {
        init();
    }

    public void calcMinMax(int start, int end) {
        if (this.mDataSets == null || this.mDataSets.size() < 1) {
            this.mYMax = 0.0f;
            this.mYMin = 0.0f;
            return;
        }
        this.mLastStart = start;
        this.mLastEnd = end;
        this.mYMin = Float.MAX_VALUE;
        this.mYMax = -3.4028235E38f;
        for (int i = 0; i < this.mDataSets.size(); i++) {
            ((DataSet) this.mDataSets.get(i)).calcMinMax(start, end);
            if (((DataSet) this.mDataSets.get(i)).getYMin() < this.mYMin) {
                this.mYMin = ((DataSet) this.mDataSets.get(i)).getYMin();
            }
            if (((DataSet) this.mDataSets.get(i)).getYMax() > this.mYMax) {
                this.mYMax = ((DataSet) this.mDataSets.get(i)).getYMax();
            }
        }
        if (this.mYMin == Float.MAX_VALUE) {
            this.mYMin = 0.0f;
            this.mYMax = 0.0f;
        }
        T firstLeft = getFirstLeft();
        if (firstLeft != null) {
            this.mLeftAxisMax = firstLeft.getYMax();
            this.mLeftAxisMin = firstLeft.getYMin();
            for (DataSet<?> dataSet : this.mDataSets) {
                if (dataSet.getAxisDependency() == AxisDependency.LEFT) {
                    if (dataSet.getYMin() < this.mLeftAxisMin) {
                        this.mLeftAxisMin = dataSet.getYMin();
                    }
                    if (dataSet.getYMax() > this.mLeftAxisMax) {
                        this.mLeftAxisMax = dataSet.getYMax();
                    }
                }
            }
        }
        T firstRight = getFirstRight();
        if (firstRight != null) {
            this.mRightAxisMax = firstRight.getYMax();
            this.mRightAxisMin = firstRight.getYMin();
            for (DataSet<?> dataSet2 : this.mDataSets) {
                if (dataSet2.getAxisDependency() == AxisDependency.RIGHT) {
                    if (dataSet2.getYMin() < this.mRightAxisMin) {
                        this.mRightAxisMin = dataSet2.getYMin();
                    }
                    if (dataSet2.getYMax() > this.mRightAxisMax) {
                        this.mRightAxisMax = dataSet2.getYMax();
                    }
                }
            }
        }
        handleEmptyAxis(firstLeft, firstRight);
    }

    protected void calcYValueSum() {
        this.mYValueSum = 0.0f;
        if (this.mDataSets != null) {
            for (int i = 0; i < this.mDataSets.size(); i++) {
                this.mYValueSum = Math.abs(((DataSet) this.mDataSets.get(i)).getYValueSum()) + this.mYValueSum;
            }
        }
    }

    protected void calcYValueCount() {
        this.mYValCount = 0;
        if (this.mDataSets != null) {
            int count = 0;
            for (int i = 0; i < this.mDataSets.size(); i++) {
                count += ((DataSet) this.mDataSets.get(i)).getEntryCount();
            }
            this.mYValCount = count;
        }
    }

    public int getDataSetCount() {
        if (this.mDataSets == null) {
            return 0;
        }
        return this.mDataSets.size();
    }

    public float getAverage() {
        return getYValueSum() / ((float) getYValCount());
    }

    public float getYMin() {
        return this.mYMin;
    }

    public float getYMin(AxisDependency axis) {
        if (axis == AxisDependency.LEFT) {
            return this.mLeftAxisMin;
        }
        return this.mRightAxisMin;
    }

    public float getYMax() {
        return this.mYMax;
    }

    public float getYMax(AxisDependency axis) {
        if (axis == AxisDependency.LEFT) {
            return this.mLeftAxisMax;
        }
        return this.mRightAxisMax;
    }

    public float getXValAverageLength() {
        return this.mXValAverageLength;
    }

    public float getYValueSum() {
        return this.mYValueSum;
    }

    public int getYValCount() {
        return this.mYValCount;
    }

    public List<String> getXVals() {
        return this.mXVals;
    }

    public void addXValue(String xVal) {
        this.mXValAverageLength = (this.mXValAverageLength + ((float) xVal.length())) / 2.0f;
        this.mXVals.add(xVal);
    }

    public void removeXValue(int index) {
        this.mXVals.remove(index);
    }

    public List<T> getDataSets() {
        return this.mDataSets;
    }

    protected int getDataSetIndexByLabel(List<T> dataSets, String label, boolean ignorecase) {
        int i;
        if (ignorecase) {
            for (i = 0; i < dataSets.size(); i++) {
                if (label.equalsIgnoreCase(((DataSet) dataSets.get(i)).getLabel())) {
                    return i;
                }
            }
        } else {
            for (i = 0; i < dataSets.size(); i++) {
                if (label.equals(((DataSet) dataSets.get(i)).getLabel())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getXValCount() {
        return this.mXVals.size();
    }

    protected String[] getDataSetLabels() {
        String[] types = new String[this.mDataSets.size()];
        for (int i = 0; i < this.mDataSets.size(); i++) {
            types[i] = ((DataSet) this.mDataSets.get(i)).getLabel();
        }
        return types;
    }

    public Entry getEntryForHighlight(Highlight highlight) {
        if (highlight.getDataSetIndex() >= this.mDataSets.size()) {
            return null;
        }
        return ((DataSet) this.mDataSets.get(highlight.getDataSetIndex())).getEntryForXIndex(highlight.getXIndex());
    }

    public T getDataSetByLabel(String label, boolean ignorecase) {
        int index = getDataSetIndexByLabel(this.mDataSets, label, ignorecase);
        if (index < 0 || index >= this.mDataSets.size()) {
            return null;
        }
        return (DataSet) this.mDataSets.get(index);
    }

    public T getDataSetByIndex(int index) {
        if (this.mDataSets == null || index < 0 || index >= this.mDataSets.size()) {
            return null;
        }
        return (DataSet) this.mDataSets.get(index);
    }

    public void addDataSet(T d) {
        if (d != null) {
            this.mYValCount += d.getEntryCount();
            this.mYValueSum += d.getYValueSum();
            if (this.mDataSets.size() <= 0) {
                this.mYMax = d.getYMax();
                this.mYMin = d.getYMin();
                if (d.getAxisDependency() == AxisDependency.LEFT) {
                    this.mLeftAxisMax = d.getYMax();
                    this.mLeftAxisMin = d.getYMin();
                } else {
                    this.mRightAxisMax = d.getYMax();
                    this.mRightAxisMin = d.getYMin();
                }
            } else {
                if (this.mYMax < d.getYMax()) {
                    this.mYMax = d.getYMax();
                }
                if (this.mYMin > d.getYMin()) {
                    this.mYMin = d.getYMin();
                }
                if (d.getAxisDependency() == AxisDependency.LEFT) {
                    if (this.mLeftAxisMax < d.getYMax()) {
                        this.mLeftAxisMax = d.getYMax();
                    }
                    if (this.mLeftAxisMin > d.getYMin()) {
                        this.mLeftAxisMin = d.getYMin();
                    }
                } else {
                    if (this.mRightAxisMax < d.getYMax()) {
                        this.mRightAxisMax = d.getYMax();
                    }
                    if (this.mRightAxisMin > d.getYMin()) {
                        this.mRightAxisMin = d.getYMin();
                    }
                }
            }
            this.mDataSets.add(d);
            handleEmptyAxis(getFirstLeft(), getFirstRight());
        }
    }

    private void handleEmptyAxis(T firstLeft, T firstRight) {
        if (firstLeft == null) {
            this.mLeftAxisMax = this.mRightAxisMax;
            this.mLeftAxisMin = this.mRightAxisMin;
        } else if (firstRight == null) {
            this.mRightAxisMax = this.mLeftAxisMax;
            this.mRightAxisMin = this.mLeftAxisMin;
        }
    }

    public boolean removeDataSet(T d) {
        if (d == null) {
            return false;
        }
        boolean removed = this.mDataSets.remove(d);
        if (!removed) {
            return removed;
        }
        this.mYValCount -= d.getEntryCount();
        this.mYValueSum -= d.getYValueSum();
        calcMinMax(this.mLastStart, this.mLastEnd);
        return removed;
    }

    public boolean removeDataSet(int index) {
        if (index >= this.mDataSets.size() || index < 0) {
            return false;
        }
        return removeDataSet((DataSet) this.mDataSets.get(index));
    }

    public void addEntry(Entry e, int dataSetIndex) {
        if (this.mDataSets.size() <= dataSetIndex || dataSetIndex < 0) {
            Log.e("addEntry", "Cannot add Entry because dataSetIndex too high or too low.");
            return;
        }
        float val = e.getVal();
        DataSet set = (DataSet) this.mDataSets.get(dataSetIndex);
        if (this.mYValCount == 0) {
            this.mYMin = val;
            this.mYMax = val;
            if (set.getAxisDependency() == AxisDependency.LEFT) {
                this.mLeftAxisMax = e.getVal();
                this.mLeftAxisMin = e.getVal();
            } else {
                this.mRightAxisMax = e.getVal();
                this.mRightAxisMin = e.getVal();
            }
        } else {
            if (this.mYMax < val) {
                this.mYMax = val;
            }
            if (this.mYMin > val) {
                this.mYMin = val;
            }
            if (set.getAxisDependency() == AxisDependency.LEFT) {
                if (this.mLeftAxisMax < e.getVal()) {
                    this.mLeftAxisMax = e.getVal();
                }
                if (this.mLeftAxisMin > e.getVal()) {
                    this.mLeftAxisMin = e.getVal();
                }
            } else {
                if (this.mRightAxisMax < e.getVal()) {
                    this.mRightAxisMax = e.getVal();
                }
                if (this.mRightAxisMin > e.getVal()) {
                    this.mRightAxisMin = e.getVal();
                }
            }
        }
        this.mYValCount++;
        this.mYValueSum += val;
        handleEmptyAxis(getFirstLeft(), getFirstRight());
        set.addEntry(e);
    }

    public boolean removeEntry(Entry e, int dataSetIndex) {
        if (e == null || dataSetIndex >= this.mDataSets.size()) {
            return false;
        }
        boolean removed = ((DataSet) this.mDataSets.get(dataSetIndex)).removeEntry(e.getXIndex());
        if (!removed) {
            return removed;
        }
        float val = e.getVal();
        this.mYValCount--;
        this.mYValueSum -= val;
        calcMinMax(this.mLastStart, this.mLastEnd);
        return removed;
    }

    public boolean removeEntry(int xIndex, int dataSetIndex) {
        if (dataSetIndex >= this.mDataSets.size()) {
            return false;
        }
        Entry e = ((DataSet) this.mDataSets.get(dataSetIndex)).getEntryForXIndex(xIndex);
        if (e == null || e.getXIndex() != xIndex) {
            return false;
        }
        return removeEntry(e, dataSetIndex);
    }

    public T getDataSetForEntry(Entry e) {
        if (e == null) {
            return null;
        }
        for (int i = 0; i < this.mDataSets.size(); i++) {
            DataSet set = (DataSet) this.mDataSets.get(i);
            for (int j = 0; j < set.getEntryCount(); j++) {
                if (e.equalTo(set.getEntryForXIndex(e.getXIndex()))) {
                    return set;
                }
            }
        }
        return null;
    }

    public int[] getColors() {
        if (this.mDataSets == null) {
            return null;
        }
        int i;
        int clrcnt = 0;
        for (i = 0; i < this.mDataSets.size(); i++) {
            clrcnt += ((DataSet) this.mDataSets.get(i)).getColors().size();
        }
        int[] colors = new int[clrcnt];
        int cnt = 0;
        for (i = 0; i < this.mDataSets.size(); i++) {
            for (Integer clr : ((DataSet) this.mDataSets.get(i)).getColors()) {
                colors[cnt] = clr.intValue();
                cnt++;
            }
        }
        return colors;
    }

    public int getIndexOfDataSet(T dataSet) {
        for (int i = 0; i < this.mDataSets.size(); i++) {
            if (this.mDataSets.get(i) == dataSet) {
                return i;
            }
        }
        return -1;
    }

    public T getFirstLeft() {
        for (DataSet dataSet : this.mDataSets) {
            if (dataSet.getAxisDependency() == AxisDependency.LEFT) {
                return dataSet;
            }
        }
        return null;
    }

    public T getFirstRight() {
        for (DataSet dataSet : this.mDataSets) {
            if (dataSet.getAxisDependency() == AxisDependency.RIGHT) {
                return dataSet;
            }
        }
        return null;
    }

    public static List<String> generateXVals(int from, int to) {
        List<String> xvals = new ArrayList();
        for (int i = from; i < to; i++) {
            xvals.add(i);
        }
        return xvals;
    }

    public void setValueFormatter(ValueFormatter f) {
        if (f != null) {
            for (DataSet<?> set : this.mDataSets) {
                set.setValueFormatter(f);
            }
        }
    }

    public void setValueTextColor(int color) {
        for (DataSet<?> set : this.mDataSets) {
            set.setValueTextColor(color);
        }
    }

    public void setValueTypeface(Typeface tf) {
        for (DataSet<?> set : this.mDataSets) {
            set.setValueTypeface(tf);
        }
    }

    public void setValueTextSize(float size) {
        for (DataSet<?> set : this.mDataSets) {
            set.setValueTextSize(size);
        }
    }

    public void setDrawValues(boolean enabled) {
        for (DataSet<?> set : this.mDataSets) {
            set.setDrawValues(enabled);
        }
    }

    public void setHighlightEnabled(boolean enabled) {
        for (DataSet<?> set : this.mDataSets) {
            set.setHighlightEnabled(enabled);
        }
    }

    public boolean isHighlightEnabled() {
        for (DataSet<?> set : this.mDataSets) {
            if (!set.isHighlightEnabled()) {
                return false;
            }
        }
        return true;
    }

    public void clearValues() {
        this.mDataSets.clear();
        notifyDataChanged();
    }

    public boolean contains(Entry e) {
        for (DataSet set : this.mDataSets) {
            if (set.contains(e)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(T dataSet) {
        for (DataSet set : this.mDataSets) {
            if (set.equals(dataSet)) {
                return true;
            }
        }
        return false;
    }
}
