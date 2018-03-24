package com.foxit.uiextensions.annots.note;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

class NoteUtil {
    public static final float TA_BEZIER = 0.5522848f;

    NoteUtil() {
    }

    public static Path GetPathStringByType(String iconName, RectF rect) {
        switch (getIconByIconName(iconName)) {
            case 0:
                return GetPathDataComment(rect);
            case 1:
                return GetPathDataComment(rect);
            case 2:
                return GetPathDataKey(rect);
            case 3:
                return GetPathDataNote(rect);
            case 4:
                return GetPathDataHelp(rect);
            case 5:
                return GetPathDataNewParagraph(rect);
            case 6:
                return GetPathDataParagraph(rect);
            case 7:
                return GetPathDataInsert(rect);
            default:
                return null;
        }
    }

    public static Path GetPathDataComment(RectF rect) {
        Path path = new Path();
        float l = rect.left;
        float b = rect.bottom;
        float r = rect.right;
        float t = rect.top;
        float w = r - l;
        float h = t - b;
        path.moveTo((w / 15.0f) + l, t - (h / 6.0f));
        path.cubicTo((w / 15.0f) + l, (t - (h / 6.0f)) + (0.5522848f * ((h / 6.0f) - (h / 10.0f))), (((2.0f * w) / 15.0f) + l) - ((0.5522848f * w) / 15.0f), t - (h / 10.0f), ((2.0f * w) / 15.0f) + l, t - (h / 10.0f));
        path.lineTo(r - ((2.0f * w) / 15.0f), t - (h / 10.0f));
        path.cubicTo((r - ((2.0f * w) / 15.0f)) + ((0.5522848f * w) / 15.0f), t - (h / 10.0f), r - (w / 15.0f), (t - (h / 6.0f)) + (0.5522848f * ((h / 6.0f) - (h / 10.0f))), r - (w / 15.0f), t - (h / 6.0f));
        path.lineTo(r - (w / 15.0f), (h / 3.0f) + b);
        path.cubicTo(r - (w / 15.0f), (((4.0f * h) / 15.0f) + b) + ((0.5522848f * h) / 15.0f), (r - ((2.0f * w) / 15.0f)) + ((0.5522848f * w) / 15.0f), ((4.0f * h) / 15.0f) + b, r - ((2.0f * w) / 15.0f), ((4.0f * h) / 15.0f) + b);
        path.lineTo(((5.0f * w) / 15.0f) + l, ((4.0f * h) / 15.0f) + b);
        path.cubicTo(((5.0f * w) / 15.0f) + l, (((2.0f * h) / 15.0f) + b) + (((0.5522848f * h) * 2.0f) / 15.0f), (((5.0f * w) / 15.0f) + l) - (((0.5522848f * w) * 2.0f) / 15.0f), ((2.0f * h) / 15.0f) + b, ((6.0f * w) / 30.0f) + l, ((2.0f * h) / 15.0f) + b);
        path.cubicTo((((7.0f * w) / 30.0f) + l) + ((0.5522848f * w) / 30.0f), ((2.0f * h) / 15.0f) + b, ((7.0f * w) / 30.0f) + l, (((2.0f * h) / 15.0f) + b) + (((0.5522848f * h) * 2.0f) / 15.0f), ((7.0f * w) / 30.0f) + l, ((4.0f * h) / 15.0f) + b);
        path.lineTo(((2.0f * w) / 15.0f) + l, ((4.0f * h) / 15.0f) + b);
        path.cubicTo((((2.0f * w) / 15.0f) + l) - ((0.5522848f * w) / 15.0f), ((4.0f * h) / 15.0f) + b, (w / 15.0f) + l, ((h / 3.0f) + b) - ((0.5522848f * h) / 15.0f), (w / 15.0f) + l, (h / 3.0f) + b);
        path.lineTo((w / 15.0f) + l, t - (h / 6.0f));
        path.moveTo(((2.0f * w) / 15.0f) + l, t - ((8.0f * h) / 30.0f));
        path.lineTo(r - ((2.0f * w) / 15.0f), t - ((8.0f * h) / 30.0f));
        path.moveTo(((2.0f * w) / 15.0f) + l, t - ((25.0f * h) / 60.0f));
        path.lineTo(r - ((2.0f * w) / 15.0f), t - ((25.0f * h) / 60.0f));
        path.moveTo(((2.0f * w) / 15.0f) + l, t - ((17.0f * h) / 30.0f));
        path.lineTo(r - ((4.0f * w) / 15.0f), t - ((17.0f * h) / 30.0f));
        return path;
    }

    public static Path GetPathDataKey(RectF rect) {
        Path path = new Path();
        float l = rect.left;
        float b = rect.bottom;
        float r = rect.right;
        float w = r - l;
        float h = rect.top - b;
        float k = (-h) / w;
        PointF tail = new PointF(0.0f, 0.0f);
        PointF center = new PointF(0.0f, 0.0f);
        tail.x = (0.9f * w) + l;
        tail.y = ((tail.x - r) * k) + b;
        center.x = (0.15f * w) + l;
        center.y = ((center.x - r) * k) + b;
        path.moveTo(tail.x + (w / 30.0f), (((-w) / 30.0f) / k) + tail.y);
        path.lineTo((tail.x + (w / 30.0f)) - (0.18f * w), ((((-k) * w) * 0.18f) - ((w / 30.0f) / k)) + tail.y);
        path.lineTo(((tail.x + (w / 30.0f)) - (0.18f * w)) + (0.07f * w), (((((-w) * 0.07f) / k) - ((k * w) * 0.18f)) - ((w / 30.0f) / k)) + tail.y);
        path.lineTo((((tail.x + (w / 30.0f)) - (0.18f * w)) - (w / 20.0f)) + (0.07f * w), ((((((-w) * 0.07f) / k) - ((k * w) / 20.0f)) - ((k * w) * 0.18f)) - ((w / 30.0f) / k)) + tail.y);
        path.lineTo(((tail.x + (w / 30.0f)) - (0.18f * w)) - (w / 20.0f), (((((-k) * w) / 20.0f) - ((k * w) * 0.18f)) - ((w / 30.0f) / k)) + tail.y);
        path.lineTo((((tail.x + (w / 30.0f)) - (0.18f * w)) - (w / 20.0f)) - (w / 15.0f), ((((((-k) * w) / 15.0f) - ((k * w) / 20.0f)) - ((k * w) * 0.18f)) - ((w / 30.0f) / k)) + tail.y);
        path.lineTo(((((tail.x + (w / 30.0f)) - (0.18f * w)) - (w / 20.0f)) - (w / 15.0f)) + (0.07f * w), (((((((-w) * 0.07f) / k) - ((k * w) / 15.0f)) - ((k * w) / 20.0f)) - ((k * w) * 0.18f)) - ((w / 30.0f) / k)) + tail.y);
        path.lineTo((((((tail.x + (w / 30.0f)) - (0.18f * w)) - (w / 20.0f)) - (w / 15.0f)) - (w / 20.0f)) + (0.07f * w), ((((((((-w) * 0.07f) / k) + (((-k) * w) / 20.0f)) + (((-k) * w) / 15.0f)) - ((k * w) / 20.0f)) - ((k * w) * 0.18f)) - ((w / 30.0f) / k)) + tail.y);
        path.lineTo(((((tail.x + (w / 30.0f)) - (0.18f * w)) - (w / 20.0f)) - (w / 15.0f)) - (w / 20.0f), (((((((-k) * w) / 20.0f) + (((-k) * w) / 15.0f)) - ((k * w) / 20.0f)) - ((k * w) * 0.18f)) - ((w / 30.0f) / k)) + tail.y);
        path.lineTo((tail.x + (w / 30.0f)) - (0.45f * w), ((((-k) * w) * 0.45f) - ((w / 30.0f) / k)) + tail.y);
        path.cubicTo(((tail.x + (w / 30.0f)) - (0.45f * w)) + (0.2f * w), (((((-w) * 0.4f) / k) - ((k * w) * 0.45f)) - ((w / 30.0f) / k)) + tail.y, center.x + (0.2f * w), (((-w) * 0.1f) / k) + center.y, center.x, center.y);
        path.cubicTo(center.x - (w / 60.0f), (((-k) * w) / 60.0f) + center.y, center.x - (w / 60.0f), (((-k) * w) / 60.0f) + center.y, center.x, center.y);
        path.cubicTo(center.x - (0.22f * w), (((0.35f * w) / k) + center.y) - (0.05f * h), ((tail.x - (w / 30.0f)) - (0.45f * w)) - (0.18f * w), (((((0.05f * w) / k) - ((k * w) * 0.45f)) + ((w / 30.0f) / k)) + tail.y) - (0.05f * h), (tail.x - (w / 30.0f)) - (0.45f * w), ((((-k) * w) * 0.45f) + ((w / 30.0f) / k)) + tail.y);
        path.lineTo(tail.x - (w / 30.0f), ((w / 30.0f) / k) + tail.y);
        path.lineTo(tail.x + (w / 30.0f), (((-w) / 30.0f) / k) + tail.y);
        path.moveTo(center.x + (0.08f * w), ((k * w) * 0.08f) + center.y);
        path.cubicTo((center.x + (0.08f * w)) + (0.1f * w), ((((-w) * 0.1f) / k) + ((k * w) * 0.08f)) + center.y, (center.x + (0.22f * w)) + (0.1f * w), (((k * w) * 0.22f) + center.y) - ((0.1f * w) / k), center.x + (0.22f * w), ((k * w) * 0.22f) + center.y);
        path.cubicTo((center.x + (0.22f * w)) - (0.1f * w), (((0.1f * w) / k) + ((k * w) * 0.22f)) + center.y, (center.x + (0.08f * w)) - (0.1f * w), (((0.1f * w) / k) + ((k * w) * 0.08f)) + center.y, center.x + (0.08f * w), ((k * w) * 0.08f) + center.y);
        return path;
    }

    public static Path GetPathDataNote(RectF rect) {
        Path path = new Path();
        float l = rect.left;
        float b = rect.bottom;
        float r = rect.right;
        float t = rect.top;
        float w = r - l;
        float h = t - b;
        path.moveTo(r - ((w * 3.0f) / 10.0f), (h / 15.0f) + b);
        path.lineTo(((7.0f * w) / 10.0f) + l, ((h * 4.0f) / 15.0f) + b);
        path.lineTo(r - (w / 10.0f), ((h * 4.0f) / 15.0f) + b);
        path.lineTo(r - (w / 10.0f), t - (h / 15.0f));
        path.lineTo((w / 10.0f) + l, t - (h / 15.0f));
        path.lineTo((w / 10.0f) + l, (h / 15.0f) + b);
        path.lineTo(r - ((w * 3.0f) / 10.0f), (h / 15.0f) + b);
        path.lineTo(r - (w / 10.0f), ((h * 4.0f) / 15.0f) + b);
        path.lineTo(r - ((w * 3.0f) / 10.0f), (h / 15.0f) + b);
        path.lineTo(r - ((w * 3.0f) / 10.0f), ((h * 4.0f) / 15.0f) + b);
        path.lineTo(r - (w / 10.0f), ((h * 4.0f) / 15.0f) + b);
        path.moveTo((w / 5.0f) + l, t - ((h * 4.0f) / 15.0f));
        path.lineTo(r - (w / 5.0f), t - ((h * 4.0f) / 15.0f));
        path.moveTo((w / 5.0f) + l, t - ((7.0f * h) / 15.0f));
        path.lineTo(r - (w / 5.0f), t - ((7.0f * h) / 15.0f));
        path.moveTo((w / 5.0f) + l, t - ((h * 10.0f) / 15.0f));
        path.lineTo(r - ((w * 3.0f) / 10.0f), t - ((h * 10.0f) / 15.0f));
        return path;
    }

    public static Path GetPathDataHelp(RectF rect) {
        Path path = new Path();
        float l = rect.left;
        float b = rect.bottom;
        float r = rect.right;
        float t = rect.top;
        float w = r - l;
        float h = t - b;
        path.moveTo((w / 60.0f) + l, (h / 2.0f) + b);
        path.cubicTo((w / 60.0f) + l, ((h / 2.0f) + b) + (0.5522848f * ((h / 60.0f) - (h / 2.0f))), ((w / 2.0f) + l) - (0.5522848f * ((w / 2.0f) - (w / 60.0f))), (h / 60.0f) + b, (w / 2.0f) + l, (h / 60.0f) + b);
        path.cubicTo(((w / 2.0f) + l) + (((0.5522848f * w) * 29.0f) / 60.0f), (h / 60.0f) + b, r - (w / 60.0f), ((h / 2.0f) + b) + (0.5522848f * ((h / 60.0f) - (h / 2.0f))), r - (w / 60.0f), (h / 2.0f) + b);
        path.cubicTo(r - (w / 60.0f), ((h / 2.0f) + b) + (((0.5522848f * h) * 29.0f) / 60.0f), ((w / 2.0f) + l) + (((0.5522848f * w) * 29.0f) / 60.0f), t - (h / 60.0f), (w / 2.0f) + l, t - (h / 60.0f));
        path.cubicTo(((w / 2.0f) + l) - (((0.5522848f * w) * 29.0f) / 60.0f), t - (h / 60.0f), (w / 60.0f) + l, ((h / 2.0f) + b) + (((0.5522848f * h) * 29.0f) / 60.0f), (w / 60.0f) + l, (h / 2.0f) + b);
        path.moveTo((0.27f * w) + l, t - (0.36f * h));
        path.cubicTo((0.27f * w) + l, (t - (0.36f * h)) + ((0.5522848f * h) * 0.23f), ((0.5f * w) + l) - ((0.5522848f * w) * 0.23f), (0.87f * h) + b, (0.5f * w) + l, (0.87f * h) + b);
        path.cubicTo(((0.5f * w) + l) + ((0.5522848f * w) * 0.23f), (0.87f * h) + b, r - (0.27f * w), (t - (0.36f * h)) + ((0.5522848f * h) * 0.23f), r - (0.27f * w), t - (0.36f * h));
        path.cubicTo((r - (0.27f * w)) - ((0.08f * w) * 0.2f), (t - (0.36f * h)) - ((0.15f * h) * 0.7f), (r - (0.35f * w)) + ((0.08f * w) * 0.2f), (t - (0.51f * h)) + ((0.15f * h) * 0.2f), r - (0.35f * w), t - (0.51f * h));
        path.cubicTo((r - (0.35f * w)) - ((0.1f * w) * 0.5f), (t - (0.51f * h)) - ((0.15f * h) * 0.3f), (r - (0.45f * w)) - ((0.1f * w) * 0.5f), (t - (0.68f * h)) + ((0.15f * h) * 0.5f), r - (0.45f * w), t - (0.68f * h));
        path.lineTo(r - (0.45f * w), (0.3f * h) + b);
        path.cubicTo(r - (0.45f * w), ((0.3f * h) + b) + ((0.1f * w) * 0.7f), r - (0.55f * w), ((0.3f * h) + b) + ((0.1f * w) * 0.7f), r - (0.55f * w), (0.3f * h) + b);
        path.lineTo(r - (0.55f * w), t - (0.66f * h));
        path.cubicTo((r - (0.55f * w)) - ((0.1f * w) * 0.05f), (t - (0.66f * h)) + ((0.18f * h) * 0.5f), (r - (0.45f * w)) - ((0.1f * w) * 0.05f), (t - (0.48f * h)) - ((0.18f * h) * 0.3f), r - (0.45f * w), t - (0.48f * h));
        path.cubicTo((r - (0.45f * w)) + ((0.08f * w) * 0.2f), (t - (0.48f * h)) + ((0.18f * h) * 0.2f), (r - (0.37f * w)) - ((0.08f * w) * 0.2f), (t - (0.36f * h)) - ((0.18f * h) * 0.7f), r - (0.37f * w), t - (0.36f * h));
        path.cubicTo(r - (0.37f * w), (t - (0.36f * h)) + ((0.5522848f * h) * 0.13f), ((0.5f * w) + l) + ((0.5522848f * w) * 0.13f), (0.77f * h) + b, (0.5f * w) + l, (0.77f * h) + b);
        path.cubicTo(((0.5f * w) + l) - ((0.5522848f * w) * 0.13f), (0.77f * h) + b, (0.37f * w) + l, (t - (0.36f * h)) + ((0.5522848f * h) * 0.13f), (0.37f * w) + l, t - (0.36f * h));
        path.cubicTo((0.37f * w) + l, (t - (0.36f * h)) + ((0.1f * w) * 0.6f), (0.27f * w) + l, (t - (0.36f * h)) + ((0.1f * w) * 0.6f), (0.27f * w) + l, t - (0.36f * h));
        path.moveTo(r - (0.56f * w), (0.13f * h) + b);
        path.cubicTo(r - (0.56f * w), ((0.13f * h) + b) + ((0.5522848f * h) * 0.055f), (r - (0.505f * w)) - ((0.5522848f * w) * 0.095f), (0.185f * h) + b, r - (0.505f * w), (0.185f * h) + b);
        path.cubicTo((r - (0.505f * w)) + ((0.5522848f * w) * 0.065f), (0.185f * h) + b, r - (0.44f * w), ((0.13f * h) + b) + ((0.5522848f * h) * 0.055f), r - (0.44f * w), (0.13f * h) + b);
        path.cubicTo(r - (0.44f * w), ((0.13f * h) + b) - ((0.5522848f * h) * 0.055f), (r - (0.505f * w)) + ((0.5522848f * w) * 0.065f), (0.075f * h) + b, r - (0.505f * w), (0.075f * h) + b);
        path.cubicTo((r - (0.505f * w)) - ((0.5522848f * w) * 0.065f), (0.075f * h) + b, r - (0.56f * w), ((0.13f * h) + b) - ((0.5522848f * h) * 0.055f), r - (0.56f * w), (0.13f * h) + b);
        return path;
    }

    public static Path GetPathDataNewParagraph(RectF rect) {
        Path path = new Path();
        float l = rect.left;
        float b = rect.bottom;
        float r = rect.right;
        float t = rect.top;
        float w = r - l;
        float h = t - b;
        path.moveTo((w / 2.0f) + l, t - (h / 20.0f));
        path.lineTo((w / 10.0f) + l, t - (h / 2.0f));
        path.lineTo(r - (w / 10.0f), t - (h / 2.0f));
        path.lineTo((w / 2.0f) + l, t - (h / 20.0f));
        path.moveTo((0.12f * w) + l, t - ((17.0f * h) / 30.0f));
        path.lineTo((0.12f * w) + l, (h / 10.0f) + b);
        path.lineTo((0.22f * w) + l, (h / 10.0f) + b);
        path.lineTo((0.22f * w) + l, (t - ((17.0f * h) / 30.0f)) + (0.14f * w));
        path.lineTo((0.38f * w) + l, (h / 10.0f) + b);
        path.lineTo((0.48f * w) + l, (h / 10.0f) + b);
        path.lineTo((0.48f * w) + l, t - ((17.0f * h) / 30.0f));
        path.lineTo((0.38f * w) + l, t - ((17.0f * h) / 30.0f));
        path.lineTo((0.38f * w) + l, b - (0.24f * w));
        path.lineTo((0.22f * w) + l, t - ((17.0f * h) / 30.0f));
        path.lineTo((0.12f * w) + l, t - ((17.0f * h) / 30.0f));
        path.moveTo((0.6f * w) + l, (h / 10.0f) + b);
        path.lineTo((0.7f * w) + l, (h / 10.0f) + b);
        path.lineTo((0.7f * w) + l, ((h / 10.0f) + b) + (h / 7.0f));
        path.cubicTo((0.97f * w) + l, ((h / 10.0f) + b) + (h / 7.0f), (0.97f * w) + l, t - ((17.0f * h) / 30.0f), (0.7f * w) + l, t - ((17.0f * h) / 30.0f));
        path.lineTo((0.6f * w) + l, t - ((17.0f * h) / 30.0f));
        path.lineTo((0.6f * w) + l, (h / 10.0f) + b);
        path.moveTo((0.7f * w) + l, ((h / 7.0f) + b) + (0.18f * h));
        path.cubicTo((0.85f * w) + l, ((h / 7.0f) + b) + (0.18f * h), (0.85f * w) + l, (t - ((17.0f * h) / 30.0f)) - (0.08f * h), (0.7f * w) + l, (t - ((17.0f * h) / 30.0f)) - (0.08f * h));
        path.lineTo((0.7f * w) + l, ((h / 7.0f) + b) + (0.18f * h));
        return path;
    }

    public static Path GetPathDataParagraph(RectF rect) {
        Path path = new Path();
        float l = rect.left;
        float b = rect.bottom;
        float r = rect.right;
        float t = rect.top;
        float w = r - l;
        float h = t - b;
        path.moveTo((w / 2.0f) + l, t - (h / 15.0f));
        path.lineTo((0.7f * w) + l, t - (h / 15.0f));
        path.lineTo((0.7f * w) + l, (h / 15.0f) + b);
        path.lineTo((0.634f * w) + l, (h / 15.0f) + b);
        path.lineTo((0.634f * w) + l, t - ((h * 2.0f) / 15.0f));
        path.lineTo((w * 0.566f) + l, t - ((h * 2.0f) / 15.0f));
        path.lineTo((w * 0.566f) + l, (h / 15.0f) + b);
        path.lineTo((w / 2.0f) + l, (h / 15.0f) + b);
        path.lineTo((w / 2.0f) + l, (t - (h / 15.0f)) - (h * 0.4f));
        path.cubicTo((w * 0.2f) + l, (t - (h / 15.0f)) - (h * 0.4f), (w * 0.2f) + l, t - (h / 15.0f), (w / 2.0f) + l, t - (h / 15.0f));
        return path;
    }

    public static Path GetPathDataInsert(RectF rect) {
        Path path = new Path();
        float l = rect.left;
        float b = rect.bottom;
        float r = rect.right;
        float t = rect.top;
        float w = r - l;
        float h = t - b;
        path.moveTo((w / 10.0f) + l, (h / 10.0f) + b);
        path.lineTo((w / 2.0f) + l, t - ((h * 2.0f) / 15.0f));
        path.lineTo(r - (w / 10.0f), (h / 10.0f) + b);
        path.lineTo((w / 10.0f) + l, (h / 10.0f) + b);
        return path;
    }

    public static String getIconNameByType(int type) {
        String iconName = "Comment";
        switch (type) {
            case 1:
                return "Comment";
            case 2:
                return "Key";
            case 3:
                return "Note";
            case 4:
                return "Help";
            case 5:
                return "NewParagraph";
            case 6:
                return "Paragraph";
            case 7:
                return "Insert";
            default:
                return iconName;
        }
    }

    public static int getIconByIconName(String iconName) {
        if (iconName == null || iconName.equalsIgnoreCase("Comment")) {
            return 1;
        }
        if (iconName.equalsIgnoreCase("Key")) {
            return 2;
        }
        if (iconName.equalsIgnoreCase("Note")) {
            return 3;
        }
        if (iconName.equalsIgnoreCase("Help")) {
            return 4;
        }
        if (iconName.equalsIgnoreCase("NewParagraph")) {
            return 5;
        }
        if (iconName.equalsIgnoreCase("Paragraph")) {
            return 6;
        }
        if (iconName.equalsIgnoreCase("Insert")) {
            return 7;
        }
        return 1;
    }
}
