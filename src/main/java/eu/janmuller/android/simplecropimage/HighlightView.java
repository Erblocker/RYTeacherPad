package eu.janmuller.android.simplecropimage;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.netspace.pad.library.R;

class HighlightView {
    public static final int GROW_BOTTOM_EDGE = 16;
    public static final int GROW_LEFT_EDGE = 2;
    public static final int GROW_NONE = 1;
    public static final int GROW_RIGHT_EDGE = 4;
    public static final int GROW_TOP_EDGE = 8;
    public static final int MOVE = 32;
    private static final String TAG = "HighlightView";
    private boolean mCircle = false;
    View mContext;
    RectF mCropRect;
    Rect mDrawRect;
    private final Paint mFocusPaint = new Paint();
    boolean mHidden;
    private RectF mImageRect;
    private float mInitialAspectRatio;
    boolean mIsFocused;
    private boolean mMaintainAspectRatio = false;
    Matrix mMatrix;
    private ModifyMode mMode = ModifyMode.None;
    private final Paint mNoFocusPaint = new Paint();
    private final Paint mOutlinePaint = new Paint();
    private Drawable mResizeDrawableDiagonal;
    private Drawable mResizeDrawableHeight;
    private Drawable mResizeDrawableWidth;

    enum ModifyMode {
        None,
        Move,
        Grow
    }

    public HighlightView(View ctx) {
        this.mContext = ctx;
    }

    private void init() {
        Resources resources = this.mContext.getResources();
        this.mResizeDrawableWidth = resources.getDrawable(R.drawable.camera_crop_width);
        this.mResizeDrawableHeight = resources.getDrawable(R.drawable.camera_crop_height);
        this.mResizeDrawableDiagonal = resources.getDrawable(R.drawable.indicator_autocrop);
    }

    public boolean hasFocus() {
        return this.mIsFocused;
    }

    public void setFocus(boolean f) {
        this.mIsFocused = f;
    }

    public void setHidden(boolean hidden) {
        this.mHidden = hidden;
    }

    protected void draw(Canvas canvas) {
        if (!this.mHidden) {
            Path path = new Path();
            if (hasFocus()) {
                Rect viewDrawingRect = new Rect();
                this.mContext.getDrawingRect(viewDrawingRect);
                if (this.mCircle) {
                    canvas.save();
                    float width = (float) this.mDrawRect.width();
                    path.addCircle(((float) this.mDrawRect.left) + (width / 2.0f), ((float) this.mDrawRect.top) + (((float) this.mDrawRect.height()) / 2.0f), width / 2.0f, Direction.CW);
                    this.mOutlinePaint.setColor(-1112874);
                    canvas.clipPath(path, Op.DIFFERENCE);
                    canvas.drawRect(viewDrawingRect, hasFocus() ? this.mFocusPaint : this.mNoFocusPaint);
                    canvas.restore();
                } else {
                    Rect rect = new Rect(viewDrawingRect.left, viewDrawingRect.top, viewDrawingRect.right, this.mDrawRect.top);
                    if (rect.width() > 0 && rect.height() > 0) {
                        canvas.drawRect(rect, hasFocus() ? this.mFocusPaint : this.mNoFocusPaint);
                    }
                    Rect bottomRect = new Rect(viewDrawingRect.left, this.mDrawRect.bottom, viewDrawingRect.right, viewDrawingRect.bottom);
                    if (bottomRect.width() > 0 && bottomRect.height() > 0) {
                        canvas.drawRect(bottomRect, hasFocus() ? this.mFocusPaint : this.mNoFocusPaint);
                    }
                    Rect leftRect = new Rect(viewDrawingRect.left, rect.bottom, this.mDrawRect.left, bottomRect.top);
                    if (leftRect.width() > 0 && leftRect.height() > 0) {
                        canvas.drawRect(leftRect, hasFocus() ? this.mFocusPaint : this.mNoFocusPaint);
                    }
                    rect = new Rect(this.mDrawRect.right, rect.bottom, viewDrawingRect.right, bottomRect.top);
                    if (rect.width() > 0 && rect.height() > 0) {
                        Paint paint;
                        if (hasFocus()) {
                            paint = this.mFocusPaint;
                        } else {
                            paint = this.mNoFocusPaint;
                        }
                        canvas.drawRect(rect, paint);
                    }
                    path.addRect(new RectF(this.mDrawRect), Direction.CW);
                    this.mOutlinePaint.setColor(-30208);
                }
                canvas.drawPath(path, this.mOutlinePaint);
                if (this.mMode != ModifyMode.Grow) {
                    return;
                }
                if (this.mCircle) {
                    int d = (int) Math.round(Math.cos(0.7853981633974483d) * (((double) this.mDrawRect.width()) / 2.0d));
                    int x = ((this.mDrawRect.left + (this.mDrawRect.width() / 2)) + d) - (this.mResizeDrawableDiagonal.getIntrinsicWidth() / 2);
                    int y = ((this.mDrawRect.top + (this.mDrawRect.height() / 2)) - d) - (this.mResizeDrawableDiagonal.getIntrinsicHeight() / 2);
                    this.mResizeDrawableDiagonal.setBounds(x, y, this.mResizeDrawableDiagonal.getIntrinsicWidth() + x, this.mResizeDrawableDiagonal.getIntrinsicHeight() + y);
                    this.mResizeDrawableDiagonal.draw(canvas);
                    return;
                }
                int left = this.mDrawRect.left + 1;
                int right = this.mDrawRect.right + 1;
                int top = this.mDrawRect.top + 4;
                int bottom = this.mDrawRect.bottom + 3;
                int widthWidth = this.mResizeDrawableWidth.getIntrinsicWidth() / 2;
                int widthHeight = this.mResizeDrawableWidth.getIntrinsicHeight() / 2;
                int heightHeight = this.mResizeDrawableHeight.getIntrinsicHeight() / 2;
                int heightWidth = this.mResizeDrawableHeight.getIntrinsicWidth() / 2;
                int xMiddle = this.mDrawRect.left + ((this.mDrawRect.right - this.mDrawRect.left) / 2);
                int yMiddle = this.mDrawRect.top + ((this.mDrawRect.bottom - this.mDrawRect.top) / 2);
                this.mResizeDrawableWidth.setBounds(left - widthWidth, yMiddle - widthHeight, left + widthWidth, yMiddle + widthHeight);
                this.mResizeDrawableWidth.draw(canvas);
                this.mResizeDrawableWidth.setBounds(right - widthWidth, yMiddle - widthHeight, right + widthWidth, yMiddle + widthHeight);
                this.mResizeDrawableWidth.draw(canvas);
                this.mResizeDrawableHeight.setBounds(xMiddle - heightWidth, top - heightHeight, xMiddle + heightWidth, top + heightHeight);
                this.mResizeDrawableHeight.draw(canvas);
                this.mResizeDrawableHeight.setBounds(xMiddle - heightWidth, bottom - heightHeight, xMiddle + heightWidth, bottom + heightHeight);
                this.mResizeDrawableHeight.draw(canvas);
                return;
            }
            this.mOutlinePaint.setColor(-16777216);
            canvas.drawRect(this.mDrawRect, this.mOutlinePaint);
        }
    }

    public ModifyMode getMode() {
        return this.mMode;
    }

    public void setMode(ModifyMode mode) {
        if (mode != this.mMode) {
            this.mMode = mode;
            this.mContext.invalidate();
        }
    }

    public int getHit(float x, float y) {
        Rect r = computeLayout();
        int retval = 1;
        if (this.mCircle) {
            float distX = x - ((float) r.centerX());
            float distY = y - ((float) r.centerY());
            int distanceFromCenter = (int) Math.sqrt((double) ((distX * distX) + (distY * distY)));
            int radius = this.mDrawRect.width() / 2;
            if (((float) Math.abs(distanceFromCenter - radius)) <= 20.0f) {
                if (Math.abs(distY) > Math.abs(distX)) {
                    if (distY < 0.0f) {
                        return 8;
                    }
                    return 16;
                } else if (distX < 0.0f) {
                    return 2;
                } else {
                    return 4;
                }
            } else if (distanceFromCenter < radius) {
                return 32;
            } else {
                return 1;
            }
        }
        boolean verticalCheck = y >= ((float) r.top) - 20.0f && y < ((float) r.bottom) + 20.0f;
        boolean horizCheck = x >= ((float) r.left) - 20.0f && x < ((float) r.right) + 20.0f;
        if (Math.abs(((float) r.left) - x) < 20.0f && verticalCheck) {
            retval = 1 | 2;
        }
        if (Math.abs(((float) r.right) - x) < 20.0f && verticalCheck) {
            retval |= 4;
        }
        if (Math.abs(((float) r.top) - y) < 20.0f && horizCheck) {
            retval |= 8;
        }
        if (Math.abs(((float) r.bottom) - y) < 20.0f && horizCheck) {
            retval |= 16;
        }
        if (retval == 1 && r.contains((int) x, (int) y)) {
            return 32;
        }
        return retval;
    }

    void handleMotion(int edge, float dx, float dy) {
        int i = -1;
        Rect r = computeLayout();
        if (edge != 1) {
            if (edge == 32) {
                moveBy((this.mCropRect.width() / ((float) r.width())) * dx, (this.mCropRect.height() / ((float) r.height())) * dy);
                return;
            }
            if ((edge & 6) == 0) {
                dx = 0.0f;
            }
            if ((edge & 24) == 0) {
                dy = 0.0f;
            }
            float yDelta = dy * (this.mCropRect.height() / ((float) r.height()));
            float width = ((float) ((edge & 2) != 0 ? -1 : 1)) * (dx * (this.mCropRect.width() / ((float) r.width())));
            if ((edge & 8) == 0) {
                i = 1;
            }
            growBy(width, ((float) i) * yDelta);
        }
    }

    void moveBy(float dx, float dy) {
        Rect invalRect = new Rect(this.mDrawRect);
        this.mCropRect.offset(dx, dy);
        this.mCropRect.offset(Math.max(0.0f, this.mImageRect.left - this.mCropRect.left), Math.max(0.0f, this.mImageRect.top - this.mCropRect.top));
        this.mCropRect.offset(Math.min(0.0f, this.mImageRect.right - this.mCropRect.right), Math.min(0.0f, this.mImageRect.bottom - this.mCropRect.bottom));
        this.mDrawRect = computeLayout();
        invalRect.union(this.mDrawRect);
        invalRect.inset(-10, -10);
        this.mContext.invalidate(invalRect);
    }

    void growBy(float dx, float dy) {
        float heightCap = 25.0f;
        if (this.mMaintainAspectRatio) {
            if (dx != 0.0f) {
                dy = dx / this.mInitialAspectRatio;
            } else if (dy != 0.0f) {
                dx = dy * this.mInitialAspectRatio;
            }
        }
        RectF r = new RectF(this.mCropRect);
        if (dx > 0.0f && r.width() + (2.0f * dx) > this.mImageRect.width()) {
            dx = (this.mImageRect.width() - r.width()) / 2.0f;
            if (this.mMaintainAspectRatio) {
                dy = dx / this.mInitialAspectRatio;
            }
        }
        if (dy > 0.0f && r.height() + (2.0f * dy) > this.mImageRect.height()) {
            dy = (this.mImageRect.height() - r.height()) / 2.0f;
            if (this.mMaintainAspectRatio) {
                dx = dy * this.mInitialAspectRatio;
            }
        }
        r.inset(-dx, -dy);
        if (r.width() < 25.0f) {
            r.inset((-(25.0f - r.width())) / 2.0f, 0.0f);
        }
        if (this.mMaintainAspectRatio) {
            heightCap = 25.0f / this.mInitialAspectRatio;
        }
        if (r.height() < heightCap) {
            r.inset(0.0f, (-(heightCap - r.height())) / 2.0f);
        }
        if (r.left < this.mImageRect.left) {
            r.offset(this.mImageRect.left - r.left, 0.0f);
        } else if (r.right > this.mImageRect.right) {
            r.offset(-(r.right - this.mImageRect.right), 0.0f);
        }
        if (r.top < this.mImageRect.top) {
            r.offset(0.0f, this.mImageRect.top - r.top);
        } else if (r.bottom > this.mImageRect.bottom) {
            r.offset(0.0f, -(r.bottom - this.mImageRect.bottom));
        }
        this.mCropRect.set(r);
        this.mDrawRect = computeLayout();
        this.mContext.invalidate();
    }

    public Rect getCropRect() {
        return new Rect((int) this.mCropRect.left, (int) this.mCropRect.top, (int) this.mCropRect.right, (int) this.mCropRect.bottom);
    }

    private Rect computeLayout() {
        RectF r = new RectF(this.mCropRect.left, this.mCropRect.top, this.mCropRect.right, this.mCropRect.bottom);
        this.mMatrix.mapRect(r);
        return new Rect(Math.round(r.left), Math.round(r.top), Math.round(r.right), Math.round(r.bottom));
    }

    public void invalidate() {
        this.mDrawRect = computeLayout();
    }

    public void setup(Matrix m, Rect imageRect, RectF cropRect, boolean circle, boolean maintainAspectRatio) {
        if (circle) {
            maintainAspectRatio = true;
        }
        this.mMatrix = new Matrix(m);
        this.mCropRect = cropRect;
        this.mImageRect = new RectF(imageRect);
        this.mMaintainAspectRatio = maintainAspectRatio;
        this.mCircle = circle;
        this.mInitialAspectRatio = this.mCropRect.width() / this.mCropRect.height();
        this.mDrawRect = computeLayout();
        this.mFocusPaint.setARGB(125, 50, 50, 50);
        this.mNoFocusPaint.setARGB(125, 50, 50, 50);
        this.mOutlinePaint.setStrokeWidth(3.0f);
        this.mOutlinePaint.setStyle(Style.STROKE);
        this.mOutlinePaint.setAntiAlias(true);
        this.mMode = ModifyMode.None;
        init();
    }
}
