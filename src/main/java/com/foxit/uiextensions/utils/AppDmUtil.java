package com.foxit.uiextensions.utils;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.text.format.Time;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class AppDmUtil {
    private static final int BEGIN_YEAR = 1900;
    private static final int MICROSECONDS_PER_HOUR = 3600000;
    private static final int MICROSECONDS_PER_MINUTE = 60000;
    public static final String dateOriValue = "0000-00-00 00:00:00 GMT+00'00'";

    public static String getLocalDateString(DateTime dateTime) {
        if (isZero(dateTime)) {
            return dateOriValue;
        }
        return getLocalDateString(documentDateToJavaDate(dateTime));
    }

    public static boolean isZero(DateTime dateTime) {
        try {
            return dateTime.getYear() == 0 && dateTime.getMonth() == 0 && dateTime.getDay() == 0 && dateTime.getHour() == 0 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getUTHourOffset() == (short) 0 && dateTime.getUTMinuteOffset() == 0;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getLocalDateString(Date date) {
        return date.toString();
    }

    public static DateTime javaDateToDocumentDate(long date) {
        return javaDateToDocumentDate(new Date(date));
    }

    public static DateTime javaDateToDocumentDate(Date date) {
        if (date == null) {
            return null;
        }
        try {
            int year = date.getYear() + 1900;
            int month = date.getMonth() + 1;
            int day = date.getDate();
            int hour = date.getHours();
            int minute = date.getMinutes();
            int second = date.getSeconds();
            int timezone = TimeZone.getDefault().getRawOffset();
            int localHour = timezone / MICROSECONDS_PER_HOUR;
            int localMinute = (timezone % MICROSECONDS_PER_HOUR) / 60000;
            DateTime dateTime = new DateTime();
            try {
                dateTime.set(year, month, day, hour, minute, second, 0, (short) localHour, localMinute);
                return dateTime;
            } catch (PDFException e) {
                return dateTime;
            }
        } catch (PDFException e2) {
            return null;
        }
    }

    public static DateTime currentDateToDocumentDate() {
        Time now = new Time();
        now.setToNow();
        try {
            int year = now.year;
            int month = now.month + 1;
            int date = now.monthDay;
            int hour = now.hour;
            int minute = now.minute;
            int second = now.second;
            int timezone = TimeZone.getDefault().getRawOffset();
            int localHour = timezone / MICROSECONDS_PER_HOUR;
            int localMinute = (timezone % MICROSECONDS_PER_HOUR) / 60000;
            DateTime dateTime = new DateTime();
            try {
                dateTime.set(year, month, date, hour, minute, second, 0, (short) localHour, localMinute);
                return dateTime;
            } catch (PDFException e) {
                return dateTime;
            }
        } catch (PDFException e2) {
            return null;
        }
    }

    public static DateTime parseDocumentDate(String date) {
        if (date == null) {
            return null;
        }
        return javaDateToDocumentDate(Date.parse(date));
    }

    public static Date documentDateToJavaDate(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        Date date = new Date();
        try {
            date.setYear(dateTime.getYear() - 1900);
            date.setMonth(dateTime.getMonth() - 1);
            date.setDate(dateTime.getDay());
            date.setHours(dateTime.getHour());
            date.setMinutes(dateTime.getMinute());
            date.setSeconds(dateTime.getSecond());
            return new Date(date.getTime() - ((long) (((dateTime.getUTMinuteOffset() * 60000) + (dateTime.getUTHourOffset() * MICROSECONDS_PER_HOUR)) - TimeZone.getDefault().getRawOffset())));
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RectF rectToRectF(Rect rect) {
        return new RectF(rect);
    }

    public static Rect rectFToRect(RectF rect) {
        return new Rect((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom);
    }

    public static int opacity100To255(int opacity) {
        if (opacity < 0 || opacity >= 100) {
            return 255;
        }
        return Math.min(255, (int) ((((float) opacity) / 100.0f) * 256.0f));
    }

    public static int opacity255To100(int opacity) {
        if (opacity < 0 || opacity >= 255) {
            return 100;
        }
        return (int) ((((float) opacity) / 256.0f) * 100.0f);
    }

    public static String randomUUID(String separator) {
        String uuid = UUID.randomUUID().toString();
        if (separator != null) {
            uuid.replace("-", separator);
        }
        return uuid;
    }

    public static int calColorByMultiply(int color, int alpha) {
        int rColor = color | -16777216;
        float opacity = ((float) alpha) / 255.0f;
        return (((rColor & -16777216) | (((int) ((((float) ((16711680 & rColor) >> 16)) * opacity) + ((1.0f - opacity) * 255.0f))) << 16)) | (((int) ((((float) ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & rColor) >> 8)) * opacity) + ((1.0f - opacity) * 255.0f))) << 8)) | ((int) ((((float) (rColor & 255)) * opacity) + ((1.0f - opacity) * 255.0f)));
    }

    public static float distanceOfTwoPoints(PointF p1, PointF p2) {
        return (float) Math.sqrt((double) (((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y))));
    }

    public static float distanceFromPointToLine(float x, float y, float x1, float y1, float x2, float y2) {
        if (x1 == x2) {
            return Math.abs(x - x1);
        }
        if (y1 == y2) {
            return Math.abs(y - y1);
        }
        float k = (y2 - y1) / (x2 - x1);
        return (float) (((double) Math.abs(((k * x) - y) + (y2 - (k * x2)))) / Math.sqrt((double) ((k * k) + 1.0f)));
    }

    public static float distanceFromPointToLine(PointF p, PointF p1, PointF p2) {
        return distanceFromPointToLine(p.x, p.y, p1.x, p1.y, p2.x, p2.y);
    }

    public static boolean isPointVerticalIntersectOnLine(float x, float y, float x1, float y1, float x2, float y2) {
        double r = ((double) (((x2 - x1) * (x - x1)) + ((y2 - y1) * (y - y1)))) / ((double) (((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1))));
        if (r <= 0.0d || r >= 1.0d) {
            return false;
        }
        return true;
    }

    public static boolean isPointVerticalIntersectOnLine(PointF p, PointF p1, PointF p2) {
        return isPointVerticalIntersectOnLine(p.x, p.y, p1.x, p1.y, p2.x, p2.y);
    }

    public static String getAnnotAuthor() {
        return "foxit sdk";
    }
}
