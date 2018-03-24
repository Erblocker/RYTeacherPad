package com.netspace.library.controls;

import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.pad.library.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomCalendarView extends FrameLayout {
    private static final int ADJUSTMENT_SCROLL_DURATION = 500;
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final int DAYS_PER_WEEK = 7;
    private static final int DEFAULT_DATE_TEXT_SIZE = 14;
    private static final String DEFAULT_MAX_DATE = "01/01/2100";
    private static final String DEFAULT_MIN_DATE = "01/01/1900";
    private static final int DEFAULT_SHOWN_WEEK_COUNT = 6;
    private static final boolean DEFAULT_SHOW_WEEK_NUMBER = true;
    private static final int DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID = -1;
    private static final int GOTO_SCROLL_DURATION = 1000;
    private static final String LOG_TAG = CustomCalendarView.class.getSimpleName();
    private static final long MILLIS_IN_DAY = 86400000;
    private static final long MILLIS_IN_WEEK = 604800000;
    private static final int SCROLL_CHANGE_DELAY = 40;
    private static final int SCROLL_HYST_WEEKS = 2;
    private static final int UNSCALED_BOTTOM_BUFFER = 20;
    private static final int UNSCALED_LIST_SCROLL_TOP_OFFSET = 2;
    private static final int UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH = 6;
    private static final int UNSCALED_WEEK_MIN_VISIBLE_HEIGHT = 12;
    private static final int UNSCALED_WEEK_SEPARATOR_LINE_WIDTH = 1;
    private WeeksAdapter mAdapter;
    private int mBottomBuffer;
    private Context mContext;
    private Locale mCurrentLocale;
    private int mCurrentMonthDisplayed;
    private int mCurrentScrollState;
    private final DateFormat mDateFormat;
    private int mDateTextAppearanceResId;
    private int mDateTextSize;
    private String[] mDayLabels;
    private ViewGroup mDayNamesHeader;
    private int mDaysPerWeek;
    private Calendar mFirstDayOfMonth;
    private int mFirstDayOfWeek;
    private int mFocusedMonthDateColor;
    private float mFriction;
    private boolean mIsScrollingUp;
    private int mListScrollTopOffset;
    private ListView mListView;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private TextView mMonthName;
    private OnDateChangeListener mOnDateChangeListener;
    private long mPreviousScrollPosition;
    private int mPreviousScrollState;
    private ScrollStateRunnable mScrollStateChangedRunnable;
    private Drawable mSelectedDateVerticalBar;
    private final int mSelectedDateVerticalBarWidth;
    private int mSelectedWeekBackgroundColor;
    private boolean mShowWeekNumber;
    private int mShownWeekCount;
    private Calendar mTempDate;
    private int mUnfocusedMonthDateColor;
    private float mVelocityScale;
    private int mWeekDayTextAppearanceResId;
    private int mWeekMinVisibleHeight;
    private int mWeekNumberColor;
    private int mWeekSeparatorLineColor;
    private final int mWeekSeperatorLineWidth;

    public interface OnDateChangeListener {
        void onDrawDay(int i, int i2, Canvas canvas, int i3, int i4, Date date);

        void onSelectedDayChange(CustomCalendarView customCalendarView, int i, int i2, int i3);
    }

    private class ScrollStateRunnable implements Runnable {
        private int mNewState;
        private AbsListView mView;

        private ScrollStateRunnable() {
        }

        public void doScrollStateChange(AbsListView view, int scrollState) {
            this.mView = view;
            this.mNewState = scrollState;
            CustomCalendarView.this.removeCallbacks(this);
            CustomCalendarView.this.postDelayed(this, 40);
        }

        public void run() {
            CustomCalendarView.this.mCurrentScrollState = this.mNewState;
            if (this.mNewState == 0 && CustomCalendarView.this.mPreviousScrollState != 0) {
                View child = this.mView.getChildAt(0);
                if (child != null) {
                    int dist = child.getBottom() - CustomCalendarView.this.mListScrollTopOffset;
                    if (dist > CustomCalendarView.this.mListScrollTopOffset) {
                        if (CustomCalendarView.this.mIsScrollingUp) {
                            this.mView.smoothScrollBy(dist - child.getHeight(), 500);
                        } else {
                            this.mView.smoothScrollBy(dist, 500);
                        }
                    }
                } else {
                    return;
                }
            }
            CustomCalendarView.this.mPreviousScrollState = this.mNewState;
        }
    }

    private class WeekView extends View {
        private String[] mDayNumbers;
        private Date[] mDayValues;
        private final Paint mDrawPaint = new Paint();
        private Calendar mFirstDay;
        private boolean[] mFocusDay;
        private boolean mHasFocusedDay;
        private boolean mHasSelectedDay = false;
        private boolean mHasUnfocusedDay;
        private int mHeight;
        private int mLastWeekDayMonth = -1;
        private final Paint mMonthNumDrawPaint = new Paint();
        private int mMonthOfFirstWeekDay = -1;
        private int mNumCells;
        private int mSelectedDay = -1;
        private int mSelectedLeft = -1;
        private int mSelectedRight = -1;
        private final Rect mTempRect = new Rect();
        private int mWeek = -1;
        private int mWidth;

        public WeekView(Context context) {
            super(context);
            initilaizePaints();
        }

        public void init(int weekNumber, int selectedWeekDay, int focusedMonth) {
            int access$14;
            this.mSelectedDay = selectedWeekDay;
            this.mHasSelectedDay = this.mSelectedDay != -1 ? CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER : false;
            if (CustomCalendarView.this.mShowWeekNumber) {
                access$14 = CustomCalendarView.this.mDaysPerWeek + 1;
            } else {
                access$14 = CustomCalendarView.this.mDaysPerWeek;
            }
            this.mNumCells = access$14;
            this.mWeek = weekNumber;
            CustomCalendarView.this.mTempDate.setTimeInMillis(CustomCalendarView.this.mMinDate.getTimeInMillis());
            CustomCalendarView.this.mTempDate.add(3, this.mWeek);
            CustomCalendarView.this.mTempDate.setFirstDayOfWeek(CustomCalendarView.this.mFirstDayOfWeek);
            this.mDayNumbers = new String[this.mNumCells];
            this.mDayValues = new Date[this.mNumCells];
            this.mFocusDay = new boolean[this.mNumCells];
            int i = 0;
            if (CustomCalendarView.this.mShowWeekNumber) {
                this.mDayNumbers[0] = String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(CustomCalendarView.this.mTempDate.get(3))});
                i = 0 + 1;
            }
            CustomCalendarView.this.mTempDate.add(5, CustomCalendarView.this.mFirstDayOfWeek - CustomCalendarView.this.mTempDate.get(7));
            this.mFirstDay = (Calendar) CustomCalendarView.this.mTempDate.clone();
            this.mMonthOfFirstWeekDay = CustomCalendarView.this.mTempDate.get(2);
            this.mHasUnfocusedDay = CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER;
            for (i = 
/*
Method generation error in method: com.netspace.library.controls.CustomCalendarView.WeekView.init(int, int, int):void, dex: classes.dex
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r1_2 'i' int) = (r1_0 'i' int), (r1_1 'i' int) binds: {(r1_1 'i' int)=B:8:0x0067, (r1_0 'i' int)=B:7:0x0065} in method: com.netspace.library.controls.CustomCalendarView.WeekView.init(int, int, int):void, dex: classes.dex
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:187)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:320)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:257)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:220)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:110)
	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:233)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:219)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:110)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:75)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:40)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:537)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:509)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 18 more

*/

            private void initilaizePaints() {
                this.mDrawPaint.setFakeBoldText(false);
                this.mDrawPaint.setAntiAlias(CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER);
                this.mDrawPaint.setStyle(Style.FILL);
                this.mMonthNumDrawPaint.setFakeBoldText(CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER);
                this.mMonthNumDrawPaint.setAntiAlias(CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER);
                this.mMonthNumDrawPaint.setStyle(Style.FILL);
                this.mMonthNumDrawPaint.setTextAlign(Align.CENTER);
                this.mMonthNumDrawPaint.setTextSize((float) CustomCalendarView.this.mDateTextSize);
            }

            public int getMonthOfFirstWeekDay() {
                return this.mMonthOfFirstWeekDay;
            }

            public int getMonthOfLastWeekDay() {
                return this.mLastWeekDayMonth;
            }

            public Calendar getFirstDay() {
                return this.mFirstDay;
            }

            public boolean getDayFromLocation(float x, Calendar outCalendar) {
                int start;
                int end;
                boolean isLayoutRtl = isLayoutRtl();
                if (isLayoutRtl) {
                    start = 0;
                    end = CustomCalendarView.this.mShowWeekNumber ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth;
                } else {
                    if (CustomCalendarView.this.mShowWeekNumber) {
                        start = this.mWidth / this.mNumCells;
                    } else {
                        start = 0;
                    }
                    end = this.mWidth;
                }
                if (x < ((float) start) || x > ((float) end)) {
                    outCalendar.clear();
                    return false;
                }
                int dayPosition = (int) (((x - ((float) start)) * ((float) CustomCalendarView.this.mDaysPerWeek)) / ((float) (end - start)));
                if (isLayoutRtl) {
                    dayPosition = (CustomCalendarView.this.mDaysPerWeek - 1) - dayPosition;
                }
                outCalendar.setTimeInMillis(this.mFirstDay.getTimeInMillis());
                outCalendar.add(5, dayPosition);
                return CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER;
            }

            protected void onDraw(Canvas canvas) {
                drawBackground(canvas);
                drawWeekNumbersAndDates(canvas);
                drawWeekSeparators(canvas);
                drawSelectedDateVerticalBars(canvas);
            }

            private void drawBackground(Canvas canvas) {
                int i = 0;
                if (this.mHasSelectedDay) {
                    this.mDrawPaint.setColor(CustomCalendarView.this.mSelectedWeekBackgroundColor);
                    this.mTempRect.top = CustomCalendarView.this.mWeekSeperatorLineWidth;
                    this.mTempRect.bottom = this.mHeight;
                    boolean isLayoutRtl = isLayoutRtl();
                    if (isLayoutRtl) {
                        this.mTempRect.left = 0;
                        this.mTempRect.right = this.mSelectedLeft - 2;
                    } else {
                        Rect rect = this.mTempRect;
                        if (CustomCalendarView.this.mShowWeekNumber) {
                            i = this.mWidth / this.mNumCells;
                        }
                        rect.left = i;
                        this.mTempRect.right = this.mSelectedLeft - 2;
                    }
                    canvas.drawRect(this.mTempRect, this.mDrawPaint);
                    if (isLayoutRtl) {
                        this.mTempRect.left = this.mSelectedRight + 3;
                        this.mTempRect.right = CustomCalendarView.this.mShowWeekNumber ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth;
                    } else {
                        this.mTempRect.left = this.mSelectedRight + 3;
                        this.mTempRect.right = this.mWidth;
                    }
                    canvas.drawRect(this.mTempRect, this.mDrawPaint);
                }
            }

            private boolean isLayoutRtl() {
                return false;
            }

            private void drawWeekNumbersAndDates(Canvas canvas) {
                int y = ((int) ((((float) this.mHeight) + this.mDrawPaint.getTextSize()) / 2.0f)) - CustomCalendarView.this.mWeekSeperatorLineWidth;
                int nDays = this.mNumCells;
                int divisor = nDays * 2;
                this.mDrawPaint.setTextAlign(Align.CENTER);
                this.mDrawPaint.setTextSize((float) CustomCalendarView.this.mDateTextSize);
                int i = 0;
                int access$18;
                int x;
                if (isLayoutRtl()) {
                    while (i < nDays - 1) {
                        Paint paint = this.mMonthNumDrawPaint;
                        if (this.mFocusDay[i]) {
                            access$18 = CustomCalendarView.this.mFocusedMonthDateColor;
                        } else {
                            access$18 = CustomCalendarView.this.mUnfocusedMonthDateColor;
                        }
                        paint.setColor(access$18);
                        x = (((i * 2) + 1) * this.mWidth) / divisor;
                        if (CustomCalendarView.this.mOnDateChangeListener != null) {
                            CustomCalendarView.this.mOnDateChangeListener.onDrawDay(x - ((this.mWidth / divisor) / 2), 0, canvas, this.mWidth / divisor, this.mHeight, this.mDayValues[i]);
                        }
                        canvas.drawText(this.mDayNumbers[(nDays - 1) - i], (float) x, (float) y, this.mMonthNumDrawPaint);
                        i++;
                    }
                    if (CustomCalendarView.this.mShowWeekNumber) {
                        this.mDrawPaint.setColor(CustomCalendarView.this.mWeekNumberColor);
                        canvas.drawText(this.mDayNumbers[0], (float) (this.mWidth - (this.mWidth / divisor)), (float) y, this.mDrawPaint);
                        return;
                    }
                    return;
                }
                if (CustomCalendarView.this.mShowWeekNumber) {
                    this.mDrawPaint.setColor(CustomCalendarView.this.mWeekNumberColor);
                    canvas.drawText(this.mDayNumbers[0], (float) (this.mWidth / divisor), (float) y, this.mDrawPaint);
                    i = 0 + 1;
                }
                while (i < nDays) {
                    paint = this.mMonthNumDrawPaint;
                    if (this.mFocusDay[i]) {
                        access$18 = CustomCalendarView.this.mFocusedMonthDateColor;
                    } else {
                        access$18 = CustomCalendarView.this.mUnfocusedMonthDateColor;
                    }
                    paint.setColor(access$18);
                    x = (((i * 2) + 1) * this.mWidth) / divisor;
                    if (CustomCalendarView.this.mOnDateChangeListener != null) {
                        CustomCalendarView.this.mOnDateChangeListener.onDrawDay(x - ((this.mWidth / divisor) / 2), 0, canvas, this.mWidth / divisor, this.mHeight, this.mDayValues[i]);
                    }
                    canvas.drawText(this.mDayNumbers[i], (float) x, (float) y, this.mMonthNumDrawPaint);
                    i++;
                }
            }

            private void drawWeekSeparators(Canvas canvas) {
                int i = 0;
                int firstFullyVisiblePosition = CustomCalendarView.this.mListView.getFirstVisiblePosition();
                if (CustomCalendarView.this.mListView.getChildAt(0).getTop() < 0) {
                    firstFullyVisiblePosition++;
                }
                if (firstFullyVisiblePosition != this.mWeek) {
                    float startX;
                    float stopX;
                    this.mDrawPaint.setColor(CustomCalendarView.this.mWeekSeparatorLineColor);
                    this.mDrawPaint.setStrokeWidth((float) CustomCalendarView.this.mWeekSeperatorLineWidth);
                    if (isLayoutRtl()) {
                        startX = 0.0f;
                        stopX = (float) (CustomCalendarView.this.mShowWeekNumber ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth);
                    } else {
                        if (CustomCalendarView.this.mShowWeekNumber) {
                            i = this.mWidth / this.mNumCells;
                        }
                        startX = (float) i;
                        stopX = (float) this.mWidth;
                    }
                    canvas.drawLine(startX, 0.0f, stopX, 0.0f, this.mDrawPaint);
                }
            }

            private void drawSelectedDateVerticalBars(Canvas canvas) {
                if (this.mHasSelectedDay) {
                    Paint BlackPaint = new Paint();
                    BlackPaint.setColor(-16777216);
                    BlackPaint.setStrokeWidth((float) (CustomCalendarView.this.mWeekSeperatorLineWidth * 2));
                    BlackPaint.setStyle(Style.STROKE);
                    canvas.drawRect((float) (this.mSelectedLeft - (CustomCalendarView.this.mSelectedDateVerticalBarWidth / 2)), (float) CustomCalendarView.this.mWeekSeperatorLineWidth, (float) (this.mSelectedRight + (CustomCalendarView.this.mSelectedDateVerticalBarWidth / 2)), (float) (this.mHeight - CustomCalendarView.this.mWeekSeperatorLineWidth), BlackPaint);
                }
            }

            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                this.mWidth = w;
                updateSelectionPositions();
            }

            private void updateSelectionPositions() {
                if (this.mHasSelectedDay) {
                    boolean isLayoutRtl = isLayoutRtl();
                    int selectedPosition = this.mSelectedDay - CustomCalendarView.this.mFirstDayOfWeek;
                    if (selectedPosition < 0) {
                        selectedPosition += 7;
                    }
                    if (CustomCalendarView.this.mShowWeekNumber && !isLayoutRtl) {
                        selectedPosition++;
                    }
                    if (isLayoutRtl) {
                        this.mSelectedLeft = (((CustomCalendarView.this.mDaysPerWeek - 1) - selectedPosition) * this.mWidth) / this.mNumCells;
                    } else {
                        this.mSelectedLeft = (this.mWidth * selectedPosition) / this.mNumCells;
                    }
                    this.mSelectedRight = this.mSelectedLeft + (this.mWidth / this.mNumCells);
                }
            }

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                this.mHeight = ((CustomCalendarView.this.mListView.getHeight() - CustomCalendarView.this.mListView.getPaddingTop()) - CustomCalendarView.this.mListView.getPaddingBottom()) / CustomCalendarView.this.mShownWeekCount;
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), this.mHeight);
            }
        }

        private class WeeksAdapter extends BaseAdapter implements OnTouchListener {
            private int mFocusedMonth;
            private GestureDetector mGestureDetector;
            private final Calendar mSelectedDate = Calendar.getInstance();
            private int mSelectedWeek;
            private int mTotalWeekCount;

            class CalendarGestureListener extends SimpleOnGestureListener {
                CalendarGestureListener() {
                }

                public boolean onSingleTapUp(MotionEvent e) {
                    return CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER;
                }
            }

            public WeeksAdapter(Context context) {
                this.mGestureDetector = new GestureDetector(CustomCalendarView.this.mContext, new CalendarGestureListener());
                init();
            }

            private void init() {
                this.mSelectedWeek = CustomCalendarView.this.getWeeksSinceMinDate(this.mSelectedDate);
                this.mTotalWeekCount = CustomCalendarView.this.getWeeksSinceMinDate(CustomCalendarView.this.mMaxDate);
                if (CustomCalendarView.this.mMinDate.get(7) != CustomCalendarView.this.mFirstDayOfWeek || CustomCalendarView.this.mMaxDate.get(7) != CustomCalendarView.this.mFirstDayOfWeek) {
                    this.mTotalWeekCount++;
                }
            }

            public void setSelectedDay(Calendar selectedDay) {
                if (selectedDay.get(6) != this.mSelectedDate.get(6) || selectedDay.get(1) != this.mSelectedDate.get(1)) {
                    this.mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
                    this.mSelectedWeek = CustomCalendarView.this.getWeeksSinceMinDate(this.mSelectedDate);
                    this.mFocusedMonth = this.mSelectedDate.get(2);
                    notifyDataSetChanged();
                }
            }

            public Calendar getSelectedDay() {
                return this.mSelectedDate;
            }

            public int getCount() {
                return this.mTotalWeekCount;
            }

            public Object getItem(int position) {
                return null;
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                WeekView weekView;
                int selectedWeekDay;
                if (convertView != null) {
                    weekView = (WeekView) convertView;
                } else {
                    weekView = new WeekView(CustomCalendarView.this.mContext);
                    weekView.setLayoutParams(new LayoutParams(-2, -2));
                    weekView.setClickable(CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER);
                    weekView.setOnTouchListener(this);
                }
                if (this.mSelectedWeek == position) {
                    selectedWeekDay = this.mSelectedDate.get(7);
                } else {
                    selectedWeekDay = -1;
                }
                weekView.init(position, selectedWeekDay, this.mFocusedMonth);
                return weekView;
            }

            public void setFocusMonth(int month) {
                if (this.mFocusedMonth != month) {
                    this.mFocusedMonth = month;
                    notifyDataSetChanged();
                }
            }

            public boolean onTouch(View v, MotionEvent event) {
                if (!CustomCalendarView.this.mListView.isEnabled() || !this.mGestureDetector.onTouchEvent(event)) {
                    return false;
                }
                if (!((WeekView) v).getDayFromLocation(event.getX(), CustomCalendarView.this.mTempDate) || CustomCalendarView.this.mTempDate.before(CustomCalendarView.this.mMinDate) || CustomCalendarView.this.mTempDate.after(CustomCalendarView.this.mMaxDate)) {
                    return CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER;
                }
                onDateTapped(CustomCalendarView.this.mTempDate);
                return CustomCalendarView.DEFAULT_SHOW_WEEK_NUMBER;
            }

            private void onDateTapped(Calendar day) {
                setSelectedDay(day);
                CustomCalendarView.this.setMonthDisplayed(day);
            }
        }

        public CustomCalendarView(Context context) {
            this(context, null);
            this.mContext = context;
        }

        public CustomCalendarView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
            this.mContext = context;
        }

        public CustomCalendarView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, 0);
            this.mListScrollTopOffset = 2;
            this.mWeekMinVisibleHeight = 12;
            this.mBottomBuffer = 20;
            this.mDaysPerWeek = 7;
            this.mFriction = 0.05f;
            this.mVelocityScale = 0.333f;
            this.mIsScrollingUp = false;
            this.mPreviousScrollState = 0;
            this.mCurrentScrollState = 0;
            this.mScrollStateChangedRunnable = new ScrollStateRunnable();
            this.mDateFormat = new SimpleDateFormat(DATE_FORMAT);
            this.mContext = context;
            setCurrentLocale(Locale.getDefault());
            this.mShowWeekNumber = false;
            this.mFirstDayOfWeek = 2;
            this.mShownWeekCount = 6;
            this.mSelectedWeekBackgroundColor = -2236963;
            this.mFocusedMonthDateColor = -16777216;
            this.mUnfocusedMonthDateColor = 1711276032;
            this.mWeekSeparatorLineColor = 436207615;
            this.mSelectedDateVerticalBar = new BitmapDrawable(this.mContext.getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.day_picker_week_view_dayline_holo));
            this.mDateTextAppearanceResId = 16842818;
            updateDateTextSize();
            this.mWeekDayTextAppearanceResId = -1;
            parseDate(DEFAULT_MIN_DATE, this.mMinDate);
            parseDate(DEFAULT_MAX_DATE, this.mMaxDate);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            this.mWeekMinVisibleHeight = (int) TypedValue.applyDimension(1, 12.0f, displayMetrics);
            this.mListScrollTopOffset = (int) TypedValue.applyDimension(1, 2.0f, displayMetrics);
            this.mBottomBuffer = (int) TypedValue.applyDimension(1, 20.0f, displayMetrics);
            this.mSelectedDateVerticalBarWidth = (int) TypedValue.applyDimension(1, 6.0f, displayMetrics);
            this.mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(1, 1.0f, displayMetrics);
            View content = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.calendar_view, null, false);
            addView(content);
            this.mListView = (ListView) findViewById(R.id.Calendarlist);
            this.mDayNamesHeader = (ViewGroup) content.findViewById(R.id.day_names);
            this.mMonthName = (TextView) content.findViewById(R.id.month_name);
            setUpHeader();
            setUpListView();
            setUpAdapter();
            this.mTempDate.setTimeInMillis(System.currentTimeMillis());
            if (this.mTempDate.before(this.mMinDate)) {
                goTo(this.mMinDate, false, DEFAULT_SHOW_WEEK_NUMBER, DEFAULT_SHOW_WEEK_NUMBER);
            } else if (this.mMaxDate.before(this.mTempDate)) {
                goTo(this.mMaxDate, false, DEFAULT_SHOW_WEEK_NUMBER, DEFAULT_SHOW_WEEK_NUMBER);
            } else {
                goTo(this.mTempDate, false, DEFAULT_SHOW_WEEK_NUMBER, DEFAULT_SHOW_WEEK_NUMBER);
            }
            invalidate();
        }

        public void setShownWeekCount(int count) {
            if (this.mShownWeekCount != count) {
                this.mShownWeekCount = count;
                invalidate();
            }
        }

        public int getShownWeekCount() {
            return this.mShownWeekCount;
        }

        public void setSelectedWeekBackgroundColor(int color) {
            if (this.mSelectedWeekBackgroundColor != color) {
                this.mSelectedWeekBackgroundColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasSelectedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        public int getSelectedWeekBackgroundColor() {
            return this.mSelectedWeekBackgroundColor;
        }

        public void setFocusedMonthDateColor(int color) {
            if (this.mFocusedMonthDateColor != color) {
                this.mFocusedMonthDateColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasFocusedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        public int getFocusedMonthDateColor() {
            return this.mFocusedMonthDateColor;
        }

        public void setUnfocusedMonthDateColor(int color) {
            if (this.mUnfocusedMonthDateColor != color) {
                this.mUnfocusedMonthDateColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasUnfocusedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        public int getUnfocusedMonthDateColor() {
            return this.mFocusedMonthDateColor;
        }

        public void setWeekNumberColor(int color) {
            if (this.mWeekNumberColor != color) {
                this.mWeekNumberColor = color;
                if (this.mShowWeekNumber) {
                    invalidateAllWeekViews();
                }
            }
        }

        public int getWeekNumberColor() {
            return this.mWeekNumberColor;
        }

        public void setWeekSeparatorLineColor(int color) {
            if (this.mWeekSeparatorLineColor != color) {
                this.mWeekSeparatorLineColor = color;
                invalidateAllWeekViews();
            }
        }

        public int getWeekSeparatorLineColor() {
            return this.mWeekSeparatorLineColor;
        }

        public void setSelectedDateVerticalBar(int resourceId) {
            setSelectedDateVerticalBar(getResources().getDrawable(resourceId));
        }

        public void setSelectedDateVerticalBar(Drawable drawable) {
            if (this.mSelectedDateVerticalBar != drawable) {
                this.mSelectedDateVerticalBar = drawable;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasSelectedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        public Drawable getSelectedDateVerticalBar() {
            return this.mSelectedDateVerticalBar;
        }

        public void setWeekDayTextAppearance(int resourceId) {
            if (this.mWeekDayTextAppearanceResId != resourceId) {
                this.mWeekDayTextAppearanceResId = resourceId;
                setUpHeader();
            }
        }

        public int getWeekDayTextAppearance() {
            return this.mWeekDayTextAppearanceResId;
        }

        public void setDateTextAppearance(int resourceId) {
            if (this.mDateTextAppearanceResId != resourceId) {
                this.mDateTextAppearanceResId = resourceId;
                updateDateTextSize();
                invalidateAllWeekViews();
            }
        }

        public int getDateTextAppearance() {
            return this.mDateTextAppearanceResId;
        }

        public void setEnabled(boolean enabled) {
            this.mListView.setEnabled(enabled);
        }

        public boolean isEnabled() {
            return this.mListView.isEnabled();
        }

        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            setCurrentLocale(newConfig.locale);
        }

        public long getMinDate() {
            return this.mMinDate.getTimeInMillis();
        }

        public void setMinDate(long minDate) {
            this.mTempDate.setTimeInMillis(minDate);
            if (!isSameDate(this.mTempDate, this.mMinDate)) {
                this.mMinDate.setTimeInMillis(minDate);
                Calendar date = this.mAdapter.mSelectedDate;
                if (date.before(this.mMinDate)) {
                    this.mAdapter.setSelectedDay(this.mMinDate);
                }
                this.mAdapter.init();
                if (date.before(this.mMinDate)) {
                    setDate(this.mTempDate.getTimeInMillis());
                } else {
                    goTo(date, false, DEFAULT_SHOW_WEEK_NUMBER, false);
                }
            }
        }

        public long getMaxDate() {
            return this.mMaxDate.getTimeInMillis();
        }

        public void setMaxDate(long maxDate) {
            this.mTempDate.setTimeInMillis(maxDate);
            if (!isSameDate(this.mTempDate, this.mMaxDate)) {
                this.mMaxDate.setTimeInMillis(maxDate);
                this.mAdapter.init();
                Calendar date = this.mAdapter.mSelectedDate;
                if (date.after(this.mMaxDate)) {
                    setDate(this.mMaxDate.getTimeInMillis());
                } else {
                    goTo(date, false, DEFAULT_SHOW_WEEK_NUMBER, false);
                }
            }
        }

        public void setShowWeekNumber(boolean showWeekNumber) {
            if (this.mShowWeekNumber != showWeekNumber) {
                this.mShowWeekNumber = showWeekNumber;
                this.mAdapter.notifyDataSetChanged();
                setUpHeader();
            }
        }

        public boolean getShowWeekNumber() {
            return this.mShowWeekNumber;
        }

        public int getFirstDayOfWeek() {
            return this.mFirstDayOfWeek;
        }

        public void setFirstDayOfWeek(int firstDayOfWeek) {
            if (this.mFirstDayOfWeek != firstDayOfWeek) {
                this.mFirstDayOfWeek = firstDayOfWeek;
                this.mAdapter.init();
                this.mAdapter.notifyDataSetChanged();
                setUpHeader();
            }
        }

        public void setOnDateChangeListener(OnDateChangeListener listener) {
            this.mOnDateChangeListener = listener;
        }

        public long getDate() {
            return this.mAdapter.mSelectedDate.getTimeInMillis();
        }

        public void setDate(long date) {
            setDate(date, false, false);
        }

        public void setDate(long date, boolean animate, boolean center) {
            this.mTempDate.setTimeInMillis(date);
            if (!isSameDate(this.mTempDate, this.mAdapter.mSelectedDate)) {
                goTo(this.mTempDate, animate, DEFAULT_SHOW_WEEK_NUMBER, center);
            }
        }

        private void updateDateTextSize() {
            this.mDateTextSize = 14;
        }

        private void invalidateAllWeekViews() {
            int childCount = this.mListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mListView.getChildAt(i).invalidate();
            }
        }

        private void setCurrentLocale(Locale locale) {
            if (!locale.equals(this.mCurrentLocale)) {
                this.mCurrentLocale = locale;
                this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
                this.mFirstDayOfMonth = getCalendarForLocale(this.mFirstDayOfMonth, locale);
                this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
                this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
            }
        }

        private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
            if (oldCalendar == null) {
                return Calendar.getInstance(locale);
            }
            long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }

        private boolean isSameDate(Calendar firstDate, Calendar secondDate) {
            if (firstDate.get(6) == secondDate.get(6) && firstDate.get(1) == secondDate.get(1)) {
                return DEFAULT_SHOW_WEEK_NUMBER;
            }
            return false;
        }

        private void setUpAdapter() {
            if (this.mAdapter == null) {
                this.mAdapter = new WeeksAdapter(this.mContext);
                this.mAdapter.registerDataSetObserver(new DataSetObserver() {
                    public void onChanged() {
                        if (CustomCalendarView.this.mOnDateChangeListener != null) {
                            Calendar selectedDay = CustomCalendarView.this.mAdapter.getSelectedDay();
                            CustomCalendarView.this.mOnDateChangeListener.onSelectedDayChange(CustomCalendarView.this, selectedDay.get(1), selectedDay.get(2), selectedDay.get(5));
                        }
                    }
                });
                this.mListView.setAdapter(this.mAdapter);
            }
            this.mAdapter.notifyDataSetChanged();
        }

        private void setUpHeader() {
            int i;
            this.mDayLabels = new String[this.mDaysPerWeek];
            int count = this.mFirstDayOfWeek + this.mDaysPerWeek;
            for (i = this.mFirstDayOfWeek; i < count; i++) {
                int calendarDay;
                if (i > 7) {
                    calendarDay = i - 7;
                } else {
                    calendarDay = i;
                }
                this.mDayLabels[i - this.mFirstDayOfWeek] = DateUtils.getDayOfWeekString(calendarDay, 50);
            }
            TextView label = (TextView) this.mDayNamesHeader.getChildAt(0);
            if (this.mShowWeekNumber) {
                label.setVisibility(0);
            } else {
                label.setVisibility(8);
            }
            count = this.mDayNamesHeader.getChildCount();
            for (i = 1; i < count; i++) {
                label = (TextView) this.mDayNamesHeader.getChildAt(i);
                if (this.mWeekDayTextAppearanceResId > -1) {
                    label.setTextAppearance(this.mContext, this.mWeekDayTextAppearanceResId);
                }
                if (i < this.mDaysPerWeek + 1) {
                    label.setText(this.mDayLabels[i - 1]);
                    label.setVisibility(0);
                } else {
                    label.setVisibility(8);
                }
            }
            this.mDayNamesHeader.invalidate();
        }

        private void setUpListView() {
            this.mListView.setDivider(null);
            this.mListView.setItemsCanFocus(DEFAULT_SHOW_WEEK_NUMBER);
            this.mListView.setVerticalScrollBarEnabled(false);
            this.mListView.setOnScrollListener(new OnScrollListener() {
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    CustomCalendarView.this.onScrollStateChanged(view, scrollState);
                }

                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    CustomCalendarView.this.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
            });
            this.mListView.setFriction(this.mFriction);
            this.mListView.setVelocityScale(this.mVelocityScale);
        }

        private void goTo(Calendar date, boolean animate, boolean setSelected, boolean forceScroll) {
            if (date.before(this.mMinDate) || date.after(this.mMaxDate)) {
                throw new IllegalArgumentException("Time not between " + this.mMinDate.getTime() + " and " + this.mMaxDate.getTime());
            }
            int firstFullyVisiblePosition = this.mListView.getFirstVisiblePosition();
            View firstChild = this.mListView.getChildAt(0);
            if (firstChild != null && firstChild.getTop() < 0) {
                firstFullyVisiblePosition++;
            }
            int lastFullyVisiblePosition = (this.mShownWeekCount + firstFullyVisiblePosition) - 1;
            if (firstChild != null && firstChild.getTop() > this.mBottomBuffer) {
                lastFullyVisiblePosition--;
            }
            if (setSelected) {
                this.mAdapter.setSelectedDay(date);
            }
            int position = getWeeksSinceMinDate(date);
            if (position < firstFullyVisiblePosition || position > lastFullyVisiblePosition || forceScroll) {
                this.mFirstDayOfMonth.setTimeInMillis(date.getTimeInMillis());
                this.mFirstDayOfMonth.set(5, 1);
                setMonthDisplayed(this.mFirstDayOfMonth);
                if (this.mFirstDayOfMonth.before(this.mMinDate)) {
                    position = 0;
                } else {
                    position = getWeeksSinceMinDate(this.mFirstDayOfMonth);
                }
                this.mPreviousScrollState = 2;
                if (animate) {
                    this.mListView.smoothScrollToPositionFromTop(position, this.mListScrollTopOffset, GOTO_SCROLL_DURATION);
                    return;
                }
                this.mListView.setSelectionFromTop(position, this.mListScrollTopOffset);
                onScrollStateChanged(this.mListView, 0);
            } else if (setSelected) {
                setMonthDisplayed(date);
            }
        }

        private boolean parseDate(String date, Calendar outDate) {
            try {
                outDate.setTime(this.mDateFormat.parse(date));
                return DEFAULT_SHOW_WEEK_NUMBER;
            } catch (ParseException e) {
                Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
                return false;
            }
        }

        private void onScrollStateChanged(AbsListView view, int scrollState) {
            this.mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
        }

        private void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            WeekView child = (WeekView) view.getChildAt(0);
            if (child != null) {
                int month;
                long currScroll = (long) ((view.getFirstVisiblePosition() * child.getHeight()) - child.getBottom());
                if (currScroll < this.mPreviousScrollPosition) {
                    this.mIsScrollingUp = DEFAULT_SHOW_WEEK_NUMBER;
                } else if (currScroll > this.mPreviousScrollPosition) {
                    this.mIsScrollingUp = false;
                } else {
                    return;
                }
                int offset = child.getBottom() < this.mWeekMinVisibleHeight ? 1 : 0;
                if (this.mIsScrollingUp) {
                    child = (WeekView) view.getChildAt(offset + 2);
                } else if (offset != 0) {
                    child = (WeekView) view.getChildAt(offset);
                }
                if (this.mIsScrollingUp) {
                    month = child.getMonthOfFirstWeekDay();
                } else {
                    month = child.getMonthOfLastWeekDay();
                }
                int monthDiff;
                if (this.mCurrentMonthDisplayed == 11 && month == 0) {
                    monthDiff = 1;
                } else if (this.mCurrentMonthDisplayed == 0 && month == 11) {
                    monthDiff = -1;
                } else {
                    monthDiff = month - this.mCurrentMonthDisplayed;
                }
                if ((!this.mIsScrollingUp && monthDiff > 0) || (this.mIsScrollingUp && monthDiff < 0)) {
                    Calendar firstDay = child.getFirstDay();
                    if (this.mIsScrollingUp) {
                        firstDay.add(5, -7);
                    } else {
                        firstDay.add(5, 7);
                    }
                    setMonthDisplayed(firstDay);
                }
                this.mPreviousScrollPosition = currScroll;
                this.mPreviousScrollState = this.mCurrentScrollState;
            }
        }

        private void setMonthDisplayed(Calendar calendar) {
            this.mCurrentMonthDisplayed = calendar.get(2);
            this.mAdapter.setFocusMonth(this.mCurrentMonthDisplayed);
            long millis = calendar.getTimeInMillis();
            this.mMonthName.setText(DateUtils.formatDateRange(this.mContext, millis, millis, 52));
            this.mMonthName.invalidate();
        }

        private int getWeeksSinceMinDate(Calendar date) {
            if (date.before(this.mMinDate)) {
                throw new IllegalArgumentException("fromDate: " + this.mMinDate.getTime() + " does not precede toDate: " + date.getTime());
            }
            return (int) ((((date.getTimeInMillis() + ((long) date.getTimeZone().getOffset(date.getTimeInMillis()))) - (this.mMinDate.getTimeInMillis() + ((long) this.mMinDate.getTimeZone().getOffset(this.mMinDate.getTimeInMillis())))) + (((long) (this.mMinDate.get(7) - this.mFirstDayOfWeek)) * 86400000)) / MILLIS_IN_WEEK);
        }
    }
