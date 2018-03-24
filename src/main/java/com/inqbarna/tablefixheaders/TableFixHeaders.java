package com.inqbarna.tablefixheaders;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;
import com.inqbarna.tablefixheaders.adapters.TableAdapter;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.List;

public class TableFixHeaders extends ViewGroup {
    private TableAdapter adapter;
    private List<List<View>> bodyViewTable;
    private int columnCount;
    private List<View> columnViewList;
    private int currentX;
    private int currentY;
    private int firstColumn;
    private int firstRow;
    private final Flinger flinger;
    private View headView;
    private int height;
    private int[] heights;
    private OnClickListener mOnClickListener;
    private final int maximumVelocity;
    private final int minimumVelocity;
    private boolean needRelayout;
    private Recycler recycler;
    private int rowCount;
    private List<View> rowViewList;
    private int scrollX;
    private int scrollY;
    private final int shadowSize;
    private final ImageView[] shadows;
    private TableAdapterDataSetObserver tableAdapterDataSetObserver;
    private int touchSlop;
    private VelocityTracker velocityTracker;
    private int width;
    private int[] widths;

    private class Flinger implements Runnable {
        private int lastX = 0;
        private int lastY = 0;
        private final Scroller scroller;

        Flinger(Context context) {
            this.scroller = new Scroller(context);
        }

        void start(int initX, int initY, int initialVelocityX, int initialVelocityY, int maxX, int maxY) {
            this.scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY);
            this.lastX = initX;
            this.lastY = initY;
            TableFixHeaders.this.post(this);
        }

        public void run() {
            if (!this.scroller.isFinished()) {
                boolean more = this.scroller.computeScrollOffset();
                int x = this.scroller.getCurrX();
                int y = this.scroller.getCurrY();
                int diffX = this.lastX - x;
                int diffY = this.lastY - y;
                if (!(diffX == 0 && diffY == 0)) {
                    TableFixHeaders.this.scrollBy(diffX, diffY);
                    this.lastX = x;
                    this.lastY = y;
                }
                if (more) {
                    TableFixHeaders.this.post(this);
                }
            }
        }

        boolean isFinished() {
            return this.scroller.isFinished();
        }

        void forceFinished() {
            if (!this.scroller.isFinished()) {
                this.scroller.forceFinished(true);
            }
        }
    }

    private class TableAdapterDataSetObserver extends DataSetObserver {
        private TableAdapterDataSetObserver() {
        }

        public void onChanged() {
            TableFixHeaders.this.needRelayout = true;
            TableFixHeaders.this.requestLayout();
        }

        public void onInvalidated() {
        }
    }

    public TableFixHeaders(Context context) {
        this(context, null);
    }

    public TableFixHeaders(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                TableFixHeaders.this.adapter.onCellClick(((Integer) v.getTag(R.id.tag_row)).intValue(), ((Integer) v.getTag(R.id.tag_column)).intValue());
            }
        };
        this.headView = null;
        this.rowViewList = new ArrayList();
        this.columnViewList = new ArrayList();
        this.bodyViewTable = new ArrayList();
        this.needRelayout = true;
        this.shadows = new ImageView[4];
        this.shadows[0] = new ImageView(context);
        this.shadows[0].setImageResource(R.drawable.shadow_left);
        this.shadows[1] = new ImageView(context);
        this.shadows[1].setImageResource(R.drawable.shadow_top);
        this.shadows[2] = new ImageView(context);
        this.shadows[2].setImageResource(R.drawable.shadow_right);
        this.shadows[3] = new ImageView(context);
        this.shadows[3].setImageResource(R.drawable.shadow_bottom);
        this.shadowSize = getResources().getDimensionPixelSize(R.dimen.shadow_size);
        this.flinger = new Flinger(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.touchSlop = configuration.getScaledTouchSlop();
        this.minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        setWillNotDraw(false);
    }

    public TableAdapter getAdapter() {
        return this.adapter;
    }

    public void setAdapter(TableAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(this.tableAdapterDataSetObserver);
        }
        this.adapter = adapter;
        this.tableAdapterDataSetObserver = new TableAdapterDataSetObserver();
        this.adapter.registerDataSetObserver(this.tableAdapterDataSetObserver);
        this.recycler = new Recycler(adapter.getViewTypeCount());
        this.scrollX = 0;
        this.scrollY = 0;
        this.firstColumn = 0;
        this.firstRow = 0;
        this.needRelayout = true;
        requestLayout();
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                this.currentX = (int) event.getRawX();
                this.currentY = (int) event.getRawY();
                return false;
            case 2:
                int x2 = Math.abs(this.currentX - ((int) event.getRawX()));
                int y2 = Math.abs(this.currentY - ((int) event.getRawY()));
                if (x2 > this.touchSlop || y2 > this.touchSlop) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case 0:
                if (!this.flinger.isFinished()) {
                    this.flinger.forceFinished();
                }
                this.currentX = (int) event.getRawX();
                this.currentY = (int) event.getRawY();
                break;
            case 1:
                VelocityTracker velocityTracker = this.velocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.maximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                int velocityY = (int) velocityTracker.getYVelocity();
                if (Math.abs(velocityX) <= this.minimumVelocity && Math.abs(velocityY) <= this.minimumVelocity) {
                    if (this.velocityTracker != null) {
                        this.velocityTracker.recycle();
                        this.velocityTracker = null;
                        break;
                    }
                }
                this.flinger.start(getActualScrollX(), getActualScrollY(), velocityX, velocityY, getMaxScrollX(), getMaxScrollY());
                break;
                break;
            case 2:
                int x2 = (int) event.getRawX();
                int y2 = (int) event.getRawY();
                int diffX = this.currentX - x2;
                int diffY = this.currentY - y2;
                this.currentX = x2;
                this.currentY = y2;
                scrollBy(diffX, diffY);
                break;
        }
        return true;
    }

    public void scrollTo(int x, int y) {
        if (this.needRelayout) {
            this.scrollX = x;
            this.firstColumn = 0;
            this.scrollY = y;
            this.firstRow = 0;
            return;
        }
        scrollBy((x - sumArray(this.widths, 1, this.firstColumn)) - this.scrollX, (y - sumArray(this.heights, 1, this.firstRow)) - this.scrollY);
    }

    public void scrollBy(int x, int y) {
        this.scrollX += x;
        this.scrollY += y;
        if (!this.needRelayout) {
            scrollBounds();
            if (this.scrollX != 0) {
                if (this.scrollX > 0) {
                    while (this.widths[this.firstColumn + 1] < this.scrollX) {
                        if (!this.rowViewList.isEmpty()) {
                            removeLeft();
                        }
                        this.scrollX -= this.widths[this.firstColumn + 1];
                        this.firstColumn++;
                    }
                    while (getFilledWidth() < this.width) {
                        addRight();
                    }
                } else {
                    while (!this.rowViewList.isEmpty() && getFilledWidth() - this.widths[this.firstColumn + this.rowViewList.size()] >= this.width) {
                        removeRight();
                    }
                    if (this.rowViewList.isEmpty()) {
                        while (this.scrollX < 0) {
                            this.firstColumn--;
                            this.scrollX += this.widths[this.firstColumn + 1];
                        }
                        while (getFilledWidth() < this.width) {
                            addRight();
                        }
                    } else {
                        while (this.scrollX < 0) {
                            addLeft();
                            this.firstColumn--;
                            this.scrollX += this.widths[this.firstColumn + 1];
                        }
                    }
                }
            }
            if (this.scrollY != 0) {
                if (this.scrollY > 0) {
                    while (this.heights[this.firstRow + 1] < this.scrollY) {
                        if (!this.columnViewList.isEmpty()) {
                            removeTop();
                        }
                        this.scrollY -= this.heights[this.firstRow + 1];
                        this.firstRow++;
                    }
                    while (getFilledHeight() < this.height) {
                        addBottom();
                    }
                } else {
                    while (!this.columnViewList.isEmpty() && getFilledHeight() - this.heights[this.firstRow + this.columnViewList.size()] >= this.height) {
                        removeBottom();
                    }
                    if (this.columnViewList.isEmpty()) {
                        while (this.scrollY < 0) {
                            this.firstRow--;
                            this.scrollY += this.heights[this.firstRow + 1];
                        }
                        while (getFilledHeight() < this.height) {
                            addBottom();
                        }
                    } else {
                        while (this.scrollY < 0) {
                            addTop();
                            this.firstRow--;
                            this.scrollY += this.heights[this.firstRow + 1];
                        }
                    }
                }
            }
            repositionViews();
            shadowsVisibility();
            awakenScrollBars();
        }
    }

    protected int computeHorizontalScrollExtent() {
        float tableSize = (float) (this.width - this.widths[0]);
        return Math.round((tableSize / ((float) (sumArray(this.widths) - this.widths[0]))) * tableSize);
    }

    protected int computeHorizontalScrollOffset() {
        return this.widths[0] + Math.round(((float) ((this.width - this.widths[0]) - computeHorizontalScrollExtent())) * (((float) getActualScrollX()) / ((float) (sumArray(this.widths) - this.width))));
    }

    protected int computeHorizontalScrollRange() {
        return this.width;
    }

    protected int computeVerticalScrollExtent() {
        float tableSize = (float) (this.height - this.heights[0]);
        return Math.round((tableSize / ((float) (sumArray(this.heights) - this.heights[0]))) * tableSize);
    }

    protected int computeVerticalScrollOffset() {
        return this.heights[0] + Math.round(((float) ((this.height - this.heights[0]) - computeVerticalScrollExtent())) * (((float) getActualScrollY()) / ((float) (sumArray(this.heights) - this.height))));
    }

    protected int computeVerticalScrollRange() {
        return this.height;
    }

    public int getActualScrollX() {
        return this.scrollX + sumArray(this.widths, 1, this.firstColumn);
    }

    public int getActualScrollY() {
        return this.scrollY + sumArray(this.heights, 1, this.firstRow);
    }

    private int getMaxScrollX() {
        return Math.max(0, sumArray(this.widths) - this.width);
    }

    private int getMaxScrollY() {
        return Math.max(0, sumArray(this.heights) - this.height);
    }

    private int getFilledWidth() {
        return (this.widths[0] + sumArray(this.widths, this.firstColumn + 1, this.rowViewList.size())) - this.scrollX;
    }

    private int getFilledHeight() {
        return (this.heights[0] + sumArray(this.heights, this.firstRow + 1, this.columnViewList.size())) - this.scrollY;
    }

    private void addLeft() {
        addLeftOrRight(this.firstColumn - 1, 0);
    }

    private void addTop() {
        addTopAndBottom(this.firstRow - 1, 0);
    }

    private void addRight() {
        int size = this.rowViewList.size();
        addLeftOrRight(this.firstColumn + size, size);
    }

    private void addBottom() {
        int size = this.columnViewList.size();
        addTopAndBottom(this.firstRow + size, size);
    }

    private void addLeftOrRight(int column, int index) {
        this.rowViewList.add(index, makeView(-1, column, this.widths[column + 1], this.heights[0]));
        int i = this.firstRow;
        for (List<View> list : this.bodyViewTable) {
            list.add(index, makeView(i, column, this.widths[column + 1], this.heights[i + 1]));
            i++;
        }
    }

    private void addTopAndBottom(int row, int index) {
        this.columnViewList.add(index, makeView(row, -1, this.widths[0], this.heights[row + 1]));
        List<View> list = new ArrayList();
        int size = this.rowViewList.size() + this.firstColumn;
        for (int i = this.firstColumn; i < size; i++) {
            list.add(makeView(row, i, this.widths[i + 1], this.heights[row + 1]));
        }
        this.bodyViewTable.add(index, list);
    }

    private void removeLeft() {
        removeLeftOrRight(0);
    }

    private void removeTop() {
        removeTopOrBottom(0);
    }

    private void removeRight() {
        removeLeftOrRight(this.rowViewList.size() - 1);
    }

    private void removeBottom() {
        removeTopOrBottom(this.columnViewList.size() - 1);
    }

    private void removeLeftOrRight(int position) {
        removeView((View) this.rowViewList.remove(position));
        for (List<View> list : this.bodyViewTable) {
            removeView((View) list.remove(position));
        }
    }

    private void removeTopOrBottom(int position) {
        removeView((View) this.columnViewList.remove(position));
        for (View view : (List) this.bodyViewTable.remove(position)) {
            removeView(view);
        }
    }

    public void removeView(View view) {
        super.removeView(view);
        int typeView = ((Integer) view.getTag(R.id.tag_type_view)).intValue();
        if (typeView != -1) {
            this.recycler.addRecycledView(view, typeView);
        }
    }

    private void repositionViews() {
        int left = this.widths[0] - this.scrollX;
        int i = this.firstColumn;
        for (View view : this.rowViewList) {
            i++;
            int right = left + this.widths[i];
            view.layout(left, 0, right, this.heights[0]);
            left = right;
        }
        int top = this.heights[0] - this.scrollY;
        i = this.firstRow;
        for (View view2 : this.columnViewList) {
            i++;
            int bottom = top + this.heights[i];
            view2.layout(0, top, this.widths[0], bottom);
            top = bottom;
        }
        top = this.heights[0] - this.scrollY;
        i = this.firstRow;
        for (List<View> list : this.bodyViewTable) {
            i++;
            bottom = top + this.heights[i];
            left = this.widths[0] - this.scrollX;
            int j = this.firstColumn;
            for (View view22 : list) {
                j++;
                right = left + this.widths[j];
                view22.layout(left, top, right, bottom);
                left = right;
            }
            top = bottom;
        }
        invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w;
        int h;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (this.adapter != null) {
            int i;
            int[] iArr;
            int i2;
            this.rowCount = this.adapter.getRowCount();
            this.columnCount = this.adapter.getColumnCount();
            this.widths = new int[(this.columnCount + 1)];
            for (i = -1; i < this.columnCount; i++) {
                iArr = this.widths;
                i2 = i + 1;
                iArr[i2] = iArr[i2] + this.adapter.getWidth(i);
            }
            this.heights = new int[(this.rowCount + 1)];
            for (i = -1; i < this.rowCount; i++) {
                iArr = this.heights;
                i2 = i + 1;
                iArr[i2] = iArr[i2] + this.adapter.getHeight(i);
            }
            if (widthMode == Integer.MIN_VALUE) {
                w = Math.min(widthSize, sumArray(this.widths));
            } else if (widthMode == 0) {
                w = sumArray(this.widths);
            } else {
                w = widthSize;
                int sumArray = sumArray(this.widths);
                if (sumArray < widthSize) {
                    float factor = ((float) widthSize) / ((float) sumArray);
                    for (i = 1; i < this.widths.length; i++) {
                        this.widths[i] = Math.round(((float) this.widths[i]) * factor);
                    }
                    this.widths[0] = widthSize - sumArray(this.widths, 1, this.widths.length - 1);
                }
            }
            if (heightMode == Integer.MIN_VALUE) {
                h = Math.min(heightSize, sumArray(this.heights));
            } else if (heightMode == 0) {
                h = sumArray(this.heights);
            } else {
                h = heightSize;
            }
        } else if (heightMode == Integer.MIN_VALUE || widthMode == 0) {
            w = 0;
            h = 0;
        } else {
            w = widthSize;
            h = heightSize;
        }
        if (this.firstRow >= this.rowCount || getMaxScrollY() - getActualScrollY() < 0) {
            this.firstRow = 0;
            this.scrollY = Integer.MAX_VALUE;
        }
        if (this.firstColumn >= this.columnCount || getMaxScrollX() - getActualScrollX() < 0) {
            this.firstColumn = 0;
            this.scrollX = Integer.MAX_VALUE;
        }
        setMeasuredDimension(w, h);
    }

    private int sumArray(int[] array) {
        return sumArray(array, 0, array.length);
    }

    private int sumArray(int[] array, int firstIndex, int count) {
        int sum = 0;
        for (int i = firstIndex; i < count + firstIndex; i++) {
            sum += array[i];
        }
        return sum;
    }

    @SuppressLint({"DrawAllocation"})
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.needRelayout || changed) {
            this.needRelayout = false;
            resetTable();
            if (this.adapter != null) {
                int i;
                this.width = r - l;
                this.height = b - t;
                int right = Math.min(this.width, sumArray(this.widths));
                int bottom = Math.min(this.height, sumArray(this.heights));
                addShadow(this.shadows[0], this.widths[0], 0, this.shadowSize + this.widths[0], bottom);
                addShadow(this.shadows[1], 0, this.heights[0], right, this.heights[0] + this.shadowSize);
                addShadow(this.shadows[2], right - this.shadowSize, 0, right, bottom);
                addShadow(this.shadows[3], 0, bottom - this.shadowSize, right, bottom);
                this.headView = makeAndSetup(-1, -1, 0, 0, this.widths[0], this.heights[0]);
                scrollBounds();
                adjustFirstCellsAndScroll();
                int left = this.widths[0] - this.scrollX;
                for (i = this.firstColumn; i < this.columnCount && left < this.width; i++) {
                    right = left + this.widths[i + 1];
                    this.rowViewList.add(makeAndSetup(-1, i, left, 0, right, this.heights[0]));
                    left = right;
                }
                int top = this.heights[0] - this.scrollY;
                for (i = this.firstRow; i < this.rowCount && top < this.height; i++) {
                    bottom = top + this.heights[i + 1];
                    this.columnViewList.add(makeAndSetup(i, -1, 0, top, this.widths[0], bottom));
                    top = bottom;
                }
                top = this.heights[0] - this.scrollY;
                for (i = this.firstRow; i < this.rowCount && top < this.height; i++) {
                    bottom = top + this.heights[i + 1];
                    left = this.widths[0] - this.scrollX;
                    List<View> list = new ArrayList();
                    for (int j = this.firstColumn; j < this.columnCount && left < this.width; j++) {
                        right = left + this.widths[j + 1];
                        list.add(makeAndSetup(i, j, left, top, right, bottom));
                        left = right;
                    }
                    this.bodyViewTable.add(list);
                    top = bottom;
                }
                shadowsVisibility();
            }
        }
    }

    private void scrollBounds() {
        this.scrollX = scrollBounds(this.scrollX, this.firstColumn, this.widths, this.width);
        this.scrollY = scrollBounds(this.scrollY, this.firstRow, this.heights, this.height);
    }

    private int scrollBounds(int desiredScroll, int firstCell, int[] sizes, int viewSize) {
        if (desiredScroll == 0) {
            return desiredScroll;
        }
        if (desiredScroll < 0) {
            return Math.max(desiredScroll, -sumArray(sizes, 1, firstCell));
        }
        return Math.min(desiredScroll, Math.max(0, (sumArray(sizes, firstCell + 1, (sizes.length - 1) - firstCell) + sizes[0]) - viewSize));
    }

    private void adjustFirstCellsAndScroll() {
        int[] values = adjustFirstCellsAndScroll(this.scrollX, this.firstColumn, this.widths);
        this.scrollX = values[0];
        this.firstColumn = values[1];
        values = adjustFirstCellsAndScroll(this.scrollY, this.firstRow, this.heights);
        this.scrollY = values[0];
        this.firstRow = values[1];
    }

    private int[] adjustFirstCellsAndScroll(int scroll, int firstCell, int[] sizes) {
        if (scroll != 0) {
            if (scroll > 0) {
                while (sizes[firstCell + 1] < scroll) {
                    firstCell++;
                    scroll -= sizes[firstCell];
                }
            } else {
                while (scroll < 0) {
                    scroll += sizes[firstCell];
                    firstCell--;
                }
            }
        }
        return new int[]{scroll, firstCell};
    }

    private void shadowsVisibility() {
        int actualScrollX = getActualScrollX();
        int actualScrollY = getActualScrollY();
        int[] remainPixels = new int[]{actualScrollX, actualScrollY, getMaxScrollX() - actualScrollX, getMaxScrollY() - actualScrollY};
        for (int i = 0; i < this.shadows.length; i++) {
            setAlpha(this.shadows[i], Math.min(((float) remainPixels[i]) / ((float) this.shadowSize), 1.0f));
        }
    }

    @TargetApi(11)
    private void setAlpha(ImageView imageView, float alpha) {
        if (VERSION.SDK_INT >= 11) {
            imageView.setAlpha(alpha);
        } else {
            imageView.setAlpha(Math.round(255.0f * alpha));
        }
    }

    private void addShadow(ImageView imageView, int l, int t, int r, int b) {
        imageView.layout(l, t, r, b);
        addView(imageView);
    }

    private void resetTable() {
        this.headView = null;
        this.rowViewList.clear();
        this.columnViewList.clear();
        this.bodyViewTable.clear();
        removeAllViews();
    }

    private View makeAndSetup(int row, int column, int left, int top, int right, int bottom) {
        View view = makeView(row, column, right - left, bottom - top);
        view.layout(left, top, right, bottom);
        return view;
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        Integer row = (Integer) child.getTag(R.id.tag_row);
        Integer column = (Integer) child.getTag(R.id.tag_column);
        if (row == null || (row.intValue() == -1 && column.intValue() == -1)) {
            return super.drawChild(canvas, child, drawingTime);
        }
        canvas.save();
        if (row.intValue() == -1) {
            canvas.clipRect(this.widths[0], 0, canvas.getWidth(), canvas.getHeight());
        } else if (column.intValue() == -1) {
            canvas.clipRect(0, this.heights[0], canvas.getWidth(), canvas.getHeight());
        } else {
            canvas.clipRect(this.widths[0], this.heights[0], canvas.getWidth(), canvas.getHeight());
        }
        boolean ret = super.drawChild(canvas, child, drawingTime);
        canvas.restore();
        return ret;
    }

    private View makeView(int row, int column, int w, int h) {
        View recycledView;
        int itemViewType = this.adapter.getItemViewType(row, column);
        if (itemViewType == -1) {
            recycledView = null;
        } else {
            recycledView = this.recycler.getRecycledView(itemViewType);
        }
        View view = this.adapter.getView(row, column, recycledView, this);
        view.setTag(R.id.tag_type_view, Integer.valueOf(itemViewType));
        view.setTag(R.id.tag_row, Integer.valueOf(row));
        view.setTag(R.id.tag_column, Integer.valueOf(column));
        view.measure(MeasureSpec.makeMeasureSpec(w, 1073741824), MeasureSpec.makeMeasureSpec(h, 1073741824));
        addTableView(view, row, column);
        view.setOnClickListener(this.mOnClickListener);
        view.setClickable(true);
        return view;
    }

    private void addTableView(View view, int row, int column) {
        if (row == -1 && column == -1) {
            addView(view, getChildCount() - 4);
        } else if (row == -1 || column == -1) {
            addView(view, getChildCount() - 5);
        } else {
            addView(view, 0);
        }
    }
}
