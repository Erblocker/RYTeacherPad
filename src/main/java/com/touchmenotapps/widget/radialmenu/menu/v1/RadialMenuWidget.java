package com.touchmenotapps.widget.radialmenu.menu.v1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.List;
import org.ksoap2.SoapEnvelope;

public class RadialMenuWidget extends View {
    private static final int ANIMATE_IN = 1;
    private static final int ANIMATE_OUT = 2;
    private static final int PADDING = 6;
    private boolean HeaderBoxBounded = false;
    private int MaxIconSize = scalePX(35);
    private int MaxSize = scalePX(90);
    private int MinIconSize = scalePX(15);
    private int MinSize = scalePX(35);
    private boolean Wedge2Shown = false;
    private RadialMenuWedge[] Wedges = new RadialMenuWedge[this.wedgeQty];
    private RadialMenuWedge[] Wedges2 = new RadialMenuWedge[this.wedgeQty2];
    private boolean animateOuterIn = false;
    private boolean animateOuterOut = false;
    private int animateSections = 4;
    private int animateTextSize = this.textSize;
    private int cRadius = (this.MinSize - scalePX(7));
    private RadialMenuItem centerCircle = null;
    private int defaultAlpha = 180;
    private int defaultColor = Color.rgb(34, 96, SoapEnvelope.VER12);
    private int defaultIconOffsetWithLabel = scalePX(20);
    private int disabledAlpha = 255;
    private int disabledColor = Color.rgb(34, 96, SoapEnvelope.VER12);
    private RadialMenuWedge enabled = null;
    private int headerBackgroundAlpha = 180;
    private int headerBackgroundColor = Color.rgb(0, 0, 0);
    private int headerBuffer = scalePX(8);
    private String headerString = null;
    private int headerTextAlpha = 255;
    private int headerTextBottom;
    private int headerTextColor = Color.rgb(255, 255, 255);
    private int headerTextLeft;
    private int headerTextSize = this.textSize;
    private RadialMenuHelper helper = new RadialMenuHelper();
    private Rect[] iconRect = new Rect[this.wedgeQty];
    private Rect[] iconRect2 = new Rect[this.wedgeQty2];
    private boolean inCircle = false;
    private boolean inWedge = false;
    private boolean inWedge2 = false;
    private PopupWindow mWindow;
    private List<RadialMenuItem> menuEntries = new ArrayList();
    private int outlineAlpha = 255;
    private int outlineColor = Color.rgb(150, 150, 150);
    private int pictureAlpha = 255;
    private int r2MaxSize = (this.r2MinSize + scalePX(55));
    private int r2MinSize = (this.MaxSize + scalePX(5));
    private int r2VariableSize;
    private float screen_density = getContext().getResources().getDisplayMetrics().density;
    private RadialMenuWedge selected = null;
    private RadialMenuWedge selected2 = null;
    private int selectedAlpha = 210;
    private int selectedColor = Color.rgb(70, 130, 180);
    private boolean showSource = false;
    private int textAlpha = 255;
    private RectF textBoxRect = new RectF();
    private int textColor = Color.rgb(255, 255, 255);
    private Rect textRect = new Rect();
    private int textSize = scalePX(15);
    private int wedge2Alpha = 210;
    private int wedge2Color = Color.rgb(50, 50, 50);
    private RadialMenuInterface wedge2Data = null;
    private int wedgeQty = 1;
    private int wedgeQty2 = 1;
    private int xPosition = scalePX(SoapEnvelope.VER12);
    private int xSource = 0;
    private int yPosition = scalePX(SoapEnvelope.VER12);
    private int ySource = 0;

    public RadialMenuWidget(Context context) {
        super(context);
        this.mWindow = this.helper.initPopup(context);
        this.mWindow.setWidth((this.r2MaxSize + 6) * 2);
        this.mWindow.setHeight((this.r2MaxSize + 6) * 2);
        this.xPosition = this.r2MaxSize + 6;
        this.yPosition = this.r2MaxSize + 6;
        determineWedges();
        this.helper.onOpenAnimation(this, this.xPosition, this.yPosition, this.xSource, this.ySource);
    }

    public void setOnDismissListener(OnDismissListener Listener) {
        this.mWindow.setOnDismissListener(Listener);
    }

    public RadialMenuItem findItem(String szName) {
        for (int i = 0; i < this.menuEntries.size(); i++) {
            RadialMenuItem Item = (RadialMenuItem) this.menuEntries.get(i);
            if (Item.getName().equalsIgnoreCase(szName)) {
                return Item;
            }
            if (Item.getChildren() != null) {
                for (int j = 0; j < Item.getChildren().size(); j++) {
                    RadialMenuItem ChildItem = (RadialMenuItem) Item.getChildren().get(j);
                    if (ChildItem.getName().equalsIgnoreCase(szName)) {
                        return ChildItem;
                    }
                }
                continue;
            }
        }
        return null;
    }

    public boolean onTouchEvent(MotionEvent e) {
        int state = e.getAction();
        int eventX = (int) e.getX();
        int eventY = (int) e.getY();
        int i;
        RadialMenuWedge f;
        if (state == 0) {
            double slice;
            this.inWedge = false;
            this.inWedge2 = false;
            this.inCircle = false;
            for (i = 0; i < this.Wedges.length; i++) {
                f = this.Wedges[i];
                slice = 6.283185307179586d / ((double) this.wedgeQty);
                this.inWedge = this.helper.pntInWedge((double) eventX, (double) eventY, (float) this.xPosition, (float) this.yPosition, this.MinSize, this.MaxSize, (((double) i) * slice) + (4.71238898038469d - (slice / 2.0d)), slice);
                if (this.inWedge) {
                    this.selected = f;
                    break;
                }
            }
            if (this.Wedge2Shown) {
                for (i = 0; i < this.Wedges2.length; i++) {
                    f = this.Wedges2[i];
                    slice = 6.283185307179586d / ((double) this.wedgeQty2);
                    this.inWedge2 = this.helper.pntInWedge((double) eventX, (double) eventY, (float) this.xPosition, (float) this.yPosition, this.r2MinSize, this.r2MaxSize, (((double) i) * slice) + (4.71238898038469d - (slice / 2.0d)), slice);
                    if (this.inWedge2) {
                        this.selected2 = f;
                        break;
                    }
                }
            }
            if (this.centerCircle != null) {
                this.inCircle = this.helper.pntInCircle((double) eventX, (double) eventY, (double) this.xPosition, (double) this.yPosition, (double) this.cRadius);
            }
        } else if (state == 1) {
            if (this.inCircle) {
                if (this.Wedge2Shown) {
                    this.enabled = null;
                    this.animateOuterIn = true;
                }
                this.selected = null;
                this.centerCircle.menuActiviated();
            } else if (this.selected != null) {
                for (i = 0; i < this.Wedges.length; i++) {
                    f = this.Wedges[i];
                    if (f == this.selected) {
                        if (this.enabled != null) {
                            this.enabled = null;
                            this.animateOuterIn = true;
                        } else {
                            ((RadialMenuItem) this.menuEntries.get(i)).menuActiviated();
                            if (((RadialMenuItem) this.menuEntries.get(i)).getChildren() != null) {
                                determineOuterWedges((RadialMenuItem) this.menuEntries.get(i));
                                this.enabled = f;
                                this.animateOuterOut = true;
                            } else {
                                this.Wedge2Shown = false;
                            }
                        }
                        this.selected = null;
                    }
                }
            } else if (this.selected2 != null) {
                for (i = 0; i < this.Wedges2.length; i++) {
                    if (this.Wedges2[i] == this.selected2) {
                        ((RadialMenuItem) this.wedge2Data.getChildren().get(i)).menuActiviated();
                    }
                }
            } else {
                dismiss();
            }
            this.selected2 = null;
            this.inCircle = false;
        }
        invalidate();
        return true;
    }

    private void PaintText(Paint paint, String szText, Canvas c, RadialMenuWedge f, int nTopOffset) {
        Rect textRect = new Rect();
        paint.getTextBounds(szText, 0, szText.length(), textRect);
        paint.setTextAlign(Align.LEFT);
        Canvas canvas = c;
        String str = szText;
        Path path = f;
        canvas.drawTextOnPath(str, path, (((float) ((6.283185307179586d * ((double) ((float) f.getOuterSize()))) * ((double) (f.getArcWidth() / 360.0f)))) - ((float) textRect.width())) / 2.0f, (float) nTopOffset, paint);
    }

    protected void onDraw(Canvas c) {
        float textHeight;
        Paint paint = new Paint();
        Paint shadowPaint = new Paint();
        setLayerType(1, paint);
        setLayerType(1, shadowPaint);
        shadowPaint.setAntiAlias(true);
        shadowPaint.setShadowLayer(6.0f, 1.0f, 1.0f, -12303292);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3.0f);
        if (this.showSource) {
            paint.setColor(this.outlineColor);
            paint.setAlpha(this.outlineAlpha);
            paint.setStyle(Style.STROKE);
            c.drawCircle((float) this.xSource, (float) this.ySource, (float) (this.cRadius / 10), paint);
            paint.setColor(this.selectedColor);
            paint.setAlpha(this.selectedAlpha);
            paint.setStyle(Style.FILL);
            c.drawCircle((float) this.xSource, (float) this.ySource, (float) (this.cRadius / 10), paint);
        }
        shadowPaint.setStyle(Style.FILL);
        shadowPaint.setColor(-1);
        c.drawCircle((float) this.xPosition, (float) this.yPosition, (float) this.MaxSize, shadowPaint);
        int i = 0;
        while (i < this.Wedges.length) {
            String[] stringArray;
            Rect rect;
            int j;
            float textBottom;
            float textLeft;
            Drawable drawable;
            RadialMenuWedge f = this.Wedges[i];
            paint.setColor(this.outlineColor);
            paint.setAlpha(this.outlineAlpha);
            paint.setStyle(Style.STROKE);
            c.drawPath(f, paint);
            if (f == this.enabled && this.Wedge2Shown) {
                paint.setColor(this.wedge2Color);
                paint.setAlpha(this.wedge2Alpha);
                paint.setStyle(Style.FILL);
                c.drawPath(f, paint);
            } else if (f != this.enabled && this.Wedge2Shown) {
                paint.setColor(this.disabledColor);
                paint.setAlpha(this.disabledAlpha);
                paint.setStyle(Style.FILL);
                c.drawPath(f, paint);
            } else if (f == this.enabled && !this.Wedge2Shown) {
                paint.setColor(this.wedge2Color);
                paint.setAlpha(this.wedge2Alpha);
                paint.setStyle(Style.FILL);
                c.drawPath(f, paint);
            } else if (f == this.selected) {
                paint.setColor(this.wedge2Color);
                paint.setAlpha(this.wedge2Alpha);
                paint.setStyle(Style.FILL);
                c.drawPath(f, paint);
            } else {
                paint.setColor(this.defaultColor);
                paint.setAlpha(this.defaultAlpha);
                paint.setStyle(Style.FILL);
                c.drawPath(f, paint);
            }
            Rect rf = this.iconRect[i];
            if (((RadialMenuItem) this.menuEntries.get(i)).getIcon() != null && ((RadialMenuItem) this.menuEntries.get(i)).getLabel() != null) {
                stringArray = ((RadialMenuItem) this.menuEntries.get(i)).getLabel().split("\n");
                paint.setColor(this.textColor);
                if (f == this.enabled || !this.Wedge2Shown) {
                    paint.setAlpha(this.textAlpha);
                } else {
                    paint.setAlpha(this.disabledAlpha);
                }
                paint.setStyle(Style.FILL);
                paint.setTextSize((float) this.textSize);
                rect = new Rect();
                textHeight = 0.0f;
                for (j = 0; j < stringArray.length; j++) {
                    paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                    textHeight += (float) (rect.height() + 3);
                }
                Rect rf2 = new Rect();
                rf2.set(rf.left, rf.top - (((int) textHeight) / 2), rf.right, rf.bottom - (((int) textHeight) / 2));
                textBottom = (float) rf2.bottom;
                for (j = 0; j < stringArray.length; j++) {
                    paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                    textLeft = (float) (rf.centerX() - (rect.width() / 2));
                    textBottom += (float) (rect.height() + 3);
                    PaintText(paint, stringArray[j], c, f, scalePX(16));
                }
                drawable = ((RadialMenuItem) this.menuEntries.get(i)).getIcon();
                drawable.setBounds(rf2);
                if (f == this.enabled || !this.Wedge2Shown) {
                    drawable.setAlpha(this.pictureAlpha);
                } else {
                    drawable.setAlpha(this.disabledAlpha);
                }
                drawable.draw(c);
            } else if (((RadialMenuItem) this.menuEntries.get(i)).getIcon() != null) {
                drawable = ((RadialMenuItem) this.menuEntries.get(i)).getIcon();
                drawable.setBounds(rf);
                if (f == this.enabled || !this.Wedge2Shown) {
                    drawable.setAlpha(this.pictureAlpha);
                } else {
                    drawable.setAlpha(this.disabledAlpha);
                }
                drawable.draw(c);
            } else {
                paint.setColor(this.textColor);
                if (f == this.enabled || !this.Wedge2Shown) {
                    paint.setAlpha(this.textAlpha);
                } else {
                    paint.setAlpha(this.disabledAlpha);
                }
                paint.setStyle(Style.FILL);
                paint.setTextSize((float) this.textSize);
                stringArray = ((RadialMenuItem) this.menuEntries.get(i)).getLabel().split("\n");
                rect = new Rect();
                textHeight = 0.0f;
                for (j = 0; j < stringArray.length; j++) {
                    paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                    textHeight += (float) (rect.height() + 3);
                }
                textBottom = ((float) rf.centerY()) - (textHeight / 2.0f);
                for (j = 0; j < stringArray.length; j++) {
                    paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                    textLeft = (float) (rf.centerX() - (rect.width() / 2));
                    textBottom += (float) (rect.height() + 3);
                    PaintText(paint, stringArray[j], c, f, scalePX(30));
                }
            }
            i++;
        }
        if (this.animateOuterIn) {
            animateOuterWedges(1);
        } else if (this.animateOuterOut) {
            animateOuterWedges(2);
        }
        if (this.Wedge2Shown) {
            shadowPaint.setStyle(Style.STROKE);
            shadowPaint.setStrokeWidth(0.0f);
            shadowPaint.setColor(-16777216);
            c.drawCircle((float) this.xPosition, (float) this.yPosition, (float) (this.r2MaxSize - this.r2VariableSize), shadowPaint);
        }
        if (this.Wedge2Shown) {
            i = 0;
            while (i < this.Wedges2.length) {
                f = this.Wedges2[i];
                paint.setColor(this.outlineColor);
                paint.setAlpha(this.outlineAlpha);
                paint.setStyle(Style.STROKE);
                c.drawPath(f, paint);
                if (f == this.selected2) {
                    paint.setColor(this.selectedColor);
                    paint.setAlpha(this.selectedAlpha);
                    paint.setStyle(Style.FILL);
                    c.drawPath(f, paint);
                } else {
                    paint.setColor(this.wedge2Color);
                    paint.setAlpha(this.wedge2Alpha);
                    paint.setStyle(Style.FILL);
                    c.drawPath(f, paint);
                }
                rf = this.iconRect2[i];
                if (((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getIcon() != null && ((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getLabel() != null) {
                    stringArray = ((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getLabel().split("\n");
                    paint.setColor(this.textColor);
                    paint.setAlpha(this.textAlpha);
                    paint.setStyle(Style.FILL);
                    paint.setTextSize((float) this.animateTextSize);
                    rect = new Rect();
                    textHeight = 0.0f;
                    for (j = 0; j < stringArray.length; j++) {
                        paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                        textHeight += (float) (rect.height() + 3);
                    }
                    rf2 = new Rect();
                    rf2.set(rf.left, rf.top - (((int) 0.0f) / 2), rf.right, rf.bottom - (((int) 0.0f) / 2));
                    textBottom = (float) rf2.bottom;
                    for (j = 0; j < stringArray.length; j++) {
                        paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                        textLeft = (float) (rf.centerX() - (rect.width() / 2));
                        textBottom += (float) (rect.height() + 3);
                        PaintText(paint, stringArray[j], c, f, scalePX(20));
                    }
                    drawable = ((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getIcon();
                    drawable.setBounds(rf2);
                    drawable.setAlpha(this.pictureAlpha);
                    drawable.draw(c);
                } else if (((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getIcon() != null) {
                    drawable = ((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getIcon();
                    drawable.setBounds(rf);
                    drawable.setAlpha(this.pictureAlpha);
                    drawable.draw(c);
                } else {
                    paint.setColor(this.textColor);
                    paint.setAlpha(this.textAlpha);
                    paint.setStyle(Style.FILL);
                    paint.setTextSize((float) this.animateTextSize);
                    stringArray = ((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getLabel().split("\n");
                    rect = new Rect();
                    textHeight = 0.0f;
                    for (j = 0; j < stringArray.length; j++) {
                        paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                        textHeight += (float) (rect.height() + 3);
                    }
                    textBottom = ((float) rf.centerY()) - (textHeight / 2.0f);
                    for (j = 0; j < stringArray.length; j++) {
                        paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                        textLeft = (float) (rf.centerX() - (rect.width() / 2));
                        textBottom += (float) (rect.height() + 3);
                        PaintText(paint, stringArray[j], c, f, scalePX(40));
                    }
                }
                i++;
            }
        }
        if (this.centerCircle != null) {
            paint.setColor(this.outlineColor);
            paint.setAlpha(this.outlineAlpha);
            paint.setStyle(Style.STROKE);
            c.drawCircle((float) this.xPosition, (float) this.yPosition, (float) this.cRadius, paint);
            if (this.inCircle) {
                paint.setColor(this.selectedColor);
                paint.setAlpha(this.selectedAlpha);
                paint.setStyle(Style.FILL);
                c.drawCircle((float) this.xPosition, (float) this.yPosition, (float) this.cRadius, paint);
                this.helper.onCloseAnimation(this, this.xPosition, this.yPosition, this.xSource, this.ySource);
            } else {
                paint.setColor(this.defaultColor);
                paint.setAlpha(this.defaultAlpha);
                paint.setStyle(Style.FILL);
                c.drawCircle((float) this.xPosition, (float) this.yPosition, (float) this.cRadius, paint);
            }
            int h;
            int w;
            Canvas canvas;
            if (this.centerCircle.getIcon() != null && this.centerCircle.getLabel() != null) {
                stringArray = this.centerCircle.getLabel().split("\n");
                paint.setColor(this.textColor);
                paint.setAlpha(this.textAlpha);
                paint.setStyle(Style.FILL);
                paint.setTextSize((float) this.textSize);
                Rect rectText = new Rect();
                Rect rectIcon = new Rect();
                drawable = this.centerCircle.getIcon();
                h = getIconSize(drawable.getIntrinsicHeight(), this.MinIconSize, this.MaxIconSize);
                w = getIconSize(drawable.getIntrinsicWidth(), this.MinIconSize, this.MaxIconSize);
                rectIcon.set(this.xPosition - (w / 2), this.yPosition - (h / 2), this.xPosition + (w / 2), this.yPosition + (h / 2));
                textHeight = 0.0f;
                for (j = 0; j < stringArray.length; j++) {
                    paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rectText);
                    textHeight += (float) (rectText.height() + 3);
                }
                rectIcon.set(rectIcon.left, rectIcon.top - (((int) textHeight) / 2), rectIcon.right, rectIcon.bottom - (((int) textHeight) / 2));
                textBottom = (float) rectIcon.bottom;
                for (j = 0; j < stringArray.length; j++) {
                    paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rectText);
                    textBottom += (float) (rectText.height() + 3);
                    canvas = c;
                    canvas.drawText(stringArray[j], ((float) (this.xPosition - (rectText.width() / 2))) - ((float) rectText.left), textBottom - ((float) rectText.bottom), paint);
                }
                drawable.setBounds(rectIcon);
                drawable.setAlpha(this.pictureAlpha);
                drawable.draw(c);
            } else if (this.centerCircle.getIcon() != null) {
                rect = new Rect();
                drawable = this.centerCircle.getIcon();
                h = getIconSize(drawable.getIntrinsicHeight(), this.MinIconSize, this.MaxIconSize);
                w = getIconSize(drawable.getIntrinsicWidth(), this.MinIconSize, this.MaxIconSize);
                rect.set(this.xPosition - (w / 2), this.yPosition - (h / 2), this.xPosition + (w / 2), this.yPosition + (h / 2));
                drawable.setBounds(rect);
                drawable.setAlpha(this.pictureAlpha);
                drawable.draw(c);
            } else {
                paint.setColor(this.textColor);
                paint.setAlpha(this.textAlpha);
                paint.setStyle(Style.FILL);
                paint.setTextSize((float) this.textSize);
                stringArray = this.centerCircle.getLabel().split("\n");
                rect = new Rect();
                textHeight = 0.0f;
                for (j = 0; j < stringArray.length; j++) {
                    paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                    textHeight += (float) (rect.height() + 3);
                }
                textBottom = ((float) this.yPosition) - (textHeight / 2.0f);
                for (j = 0; j < stringArray.length; j++) {
                    paint.getTextBounds(stringArray[j], 0, stringArray[j].length(), rect);
                    textBottom += (float) (rect.height() + 3);
                    canvas = c;
                    canvas.drawText(stringArray[j], ((float) (this.xPosition - (rect.width() / 2))) - ((float) rect.left), textBottom - ((float) rect.bottom), paint);
                }
            }
        }
        if (this.headerString != null) {
            paint.setTextSize((float) this.headerTextSize);
            paint.getTextBounds(this.headerString, 0, this.headerString.length(), this.textRect);
            if (!this.HeaderBoxBounded) {
                determineHeaderBox();
                this.HeaderBoxBounded = true;
            }
            paint.setColor(this.outlineColor);
            paint.setAlpha(this.outlineAlpha);
            paint.setStyle(Style.STROKE);
            c.drawRoundRect(this.textBoxRect, (float) scalePX(5), (float) scalePX(5), paint);
            paint.setColor(this.headerBackgroundColor);
            paint.setAlpha(this.headerBackgroundAlpha);
            paint.setStyle(Style.FILL);
            c.drawRoundRect(this.textBoxRect, (float) scalePX(5), (float) scalePX(5), paint);
            paint.setColor(this.headerTextColor);
            paint.setAlpha(this.headerTextAlpha);
            paint.setStyle(Style.FILL);
            paint.setTextSize((float) this.headerTextSize);
            c.drawText(this.headerString, (float) this.headerTextLeft, (float) this.headerTextBottom, paint);
        }
    }

    private int scalePX(int dp_size) {
        return (int) ((((float) dp_size) * this.screen_density) + 0.5f);
    }

    private int getIconSize(int iconSize, int minSize, int maxSize) {
        if (iconSize > minSize) {
            return iconSize > maxSize ? maxSize : iconSize;
        } else {
            return minSize;
        }
    }

    private void animateOuterWedges(int animation_direction) {
        boolean animationComplete = false;
        float slice2 = 360.0f / ((float) this.wedgeQty2);
        float start_slice2 = 270.0f - (slice2 / 2.0f);
        double rSlice2 = 6.283185307179586d / ((double) this.wedgeQty2);
        double rStart2 = 4.71238898038469d - (rSlice2 / 2.0d);
        this.Wedges2 = new RadialMenuWedge[this.wedgeQty2];
        this.iconRect2 = new Rect[this.wedgeQty2];
        this.Wedge2Shown = true;
        int wedgeSizeChange = (this.r2MaxSize - this.r2MinSize) / this.animateSections;
        int i;
        int nLabelOffset;
        float xCenter;
        float yCenter;
        int h;
        int w;
        Drawable drawable;
        int widthOffset;
        if (animation_direction == 2) {
            if ((this.r2MinSize + this.r2VariableSize) + wedgeSizeChange < this.r2MaxSize) {
                this.r2VariableSize += wedgeSizeChange;
            } else {
                this.animateOuterOut = false;
                this.r2VariableSize = this.r2MaxSize - this.r2MinSize;
                animationComplete = true;
            }
            this.animateTextSize = (this.textSize / this.animateSections) * (this.r2VariableSize / wedgeSizeChange);
            float fStartDegree = start_slice2;
            for (i = 0; i < this.Wedges2.length; i++) {
                if (i != this.Wedges2.length - 1) {
                    this.Wedges2[i] = new RadialMenuWedge(this.xPosition, this.yPosition, this.r2MinSize, this.r2MinSize + this.r2VariableSize, fStartDegree, slice2);
                    fStartDegree += slice2;
                } else {
                    float fTemp = fStartDegree;
                    while (fTemp >= 360.0f) {
                        fTemp -= 360.0f;
                    }
                    this.Wedges2[i] = new RadialMenuWedge(this.xPosition, this.yPosition, this.r2MinSize, this.r2MinSize + this.r2VariableSize, fStartDegree, start_slice2 - fTemp);
                    fStartDegree += slice2;
                }
                nLabelOffset = 0;
                if (((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getLabel() != null) {
                    nLabelOffset = -this.defaultIconOffsetWithLabel;
                }
                xCenter = ((float) ((Math.cos(((((double) i) * rSlice2) + (0.5d * rSlice2)) + rStart2) * ((double) (((this.r2MinSize + this.r2VariableSize) + this.r2MinSize) + nLabelOffset))) / 2.0d)) + ((float) this.xPosition);
                yCenter = ((float) ((Math.sin(((((double) i) * rSlice2) + (0.5d * rSlice2)) + rStart2) * ((double) (((this.r2MinSize + this.r2VariableSize) + this.r2MinSize) + nLabelOffset))) / 2.0d)) + ((float) this.yPosition);
                h = this.MaxIconSize;
                w = this.MaxIconSize;
                if (((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getIcon() != null) {
                    drawable = ((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getIcon();
                    h = getIconSize(drawable.getIntrinsicHeight(), this.MinIconSize, this.MaxIconSize);
                    w = getIconSize(drawable.getIntrinsicWidth(), this.MinIconSize, this.MaxIconSize);
                }
                if (this.r2VariableSize < h) {
                    h = this.r2VariableSize;
                }
                if (this.r2VariableSize < w) {
                    w = this.r2VariableSize;
                }
                this.iconRect2[i] = new Rect(((int) xCenter) - (w / 2), ((int) yCenter) - (h / 2), ((int) xCenter) + (w / 2), ((int) yCenter) + (h / 2));
                widthOffset = this.MaxSize;
                if (widthOffset < this.textRect.width() / 2) {
                    widthOffset = (this.textRect.width() / 2) + scalePX(3);
                }
                this.textBoxRect.set((float) (this.xPosition - widthOffset), (float) ((((this.yPosition - (this.r2MinSize + this.r2VariableSize)) - this.headerBuffer) - this.textRect.height()) - scalePX(3)), (float) (this.xPosition + widthOffset), (float) (((this.yPosition - (this.r2MinSize + this.r2VariableSize)) - this.headerBuffer) + scalePX(3)));
                this.headerTextBottom = ((this.yPosition - (this.r2MinSize + this.r2VariableSize)) - this.headerBuffer) - this.textRect.bottom;
            }
        } else if (animation_direction == 1) {
            if (this.r2MinSize < (this.r2MaxSize - this.r2VariableSize) - wedgeSizeChange) {
                this.r2VariableSize += wedgeSizeChange;
            } else {
                this.animateOuterIn = false;
                this.r2VariableSize = this.r2MaxSize;
                animationComplete = true;
            }
            this.animateTextSize = this.textSize - ((this.textSize / this.animateSections) * (this.r2VariableSize / wedgeSizeChange));
            for (i = 0; i < this.Wedges2.length; i++) {
                this.Wedges2[i] = new RadialMenuWedge(this.xPosition, this.yPosition, this.r2MinSize, this.r2MaxSize - this.r2VariableSize, (((float) i) * slice2) + start_slice2, slice2);
                nLabelOffset = 0;
                if (((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getLabel() != null) {
                    nLabelOffset = -this.defaultIconOffsetWithLabel;
                }
                xCenter = ((float) ((Math.cos(((((double) i) * rSlice2) + (0.5d * rSlice2)) + rStart2) * ((double) (((this.r2MaxSize - this.r2VariableSize) + this.r2MinSize) + nLabelOffset))) / 2.0d)) + ((float) this.xPosition);
                yCenter = ((float) ((Math.sin(((((double) i) * rSlice2) + (0.5d * rSlice2)) + rStart2) * ((double) (((this.r2MaxSize - this.r2VariableSize) + this.r2MinSize) + nLabelOffset))) / 2.0d)) + ((float) this.yPosition);
                h = this.MaxIconSize;
                w = this.MaxIconSize;
                if (((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getIcon() != null) {
                    drawable = ((RadialMenuItem) this.wedge2Data.getChildren().get(i)).getIcon();
                    h = getIconSize(drawable.getIntrinsicHeight(), this.MinIconSize, this.MaxIconSize);
                    w = getIconSize(drawable.getIntrinsicWidth(), this.MinIconSize, this.MaxIconSize);
                }
                if ((this.r2MaxSize - this.r2MinSize) - this.r2VariableSize < h) {
                    h = (this.r2MaxSize - this.r2MinSize) - this.r2VariableSize;
                }
                if ((this.r2MaxSize - this.r2MinSize) - this.r2VariableSize < w) {
                    w = (this.r2MaxSize - this.r2MinSize) - this.r2VariableSize;
                }
                this.iconRect2[i] = new Rect(((int) xCenter) - (w / 2), ((int) yCenter) - (h / 2), ((int) xCenter) + (w / 2), ((int) yCenter) + (h / 2));
                int heightOffset = this.r2MaxSize - this.r2VariableSize;
                widthOffset = this.MaxSize;
                if (this.MaxSize > this.r2MaxSize - this.r2VariableSize) {
                    heightOffset = this.MaxSize;
                }
                if (widthOffset < this.textRect.width() / 2) {
                    widthOffset = (this.textRect.width() / 2) + scalePX(3);
                }
                this.textBoxRect.set((float) (this.xPosition - widthOffset), (float) ((((this.yPosition - heightOffset) - this.headerBuffer) - this.textRect.height()) - scalePX(3)), (float) (this.xPosition + widthOffset), (float) (((this.yPosition - heightOffset) - this.headerBuffer) + scalePX(3)));
                this.headerTextBottom = ((this.yPosition - heightOffset) - this.headerBuffer) - this.textRect.bottom;
            }
        }
        if (animationComplete) {
            this.r2VariableSize = 0;
            this.animateTextSize = this.textSize;
            if (animation_direction == 1) {
                this.Wedge2Shown = false;
            }
        }
        invalidate();
    }

    private void determineWedges() {
        int entriesQty = this.menuEntries.size();
        if (entriesQty > 0) {
            this.wedgeQty = entriesQty;
            float degSlice = (float) (360 / this.wedgeQty);
            float start_degSlice = 270.0f - (degSlice / 2.0f);
            double rSlice = 6.283185307179586d / ((double) this.wedgeQty);
            double rStart = 4.71238898038469d - (rSlice / 2.0d);
            this.Wedges = new RadialMenuWedge[this.wedgeQty];
            this.iconRect = new Rect[this.wedgeQty];
            float fStartDegree = start_degSlice;
            for (int i = 0; i < this.Wedges.length; i++) {
                this.Wedges[i] = new RadialMenuWedge(this.xPosition, this.yPosition, this.MinSize, this.MaxSize, fStartDegree, degSlice);
                fStartDegree += degSlice;
                float xCenter = ((float) ((Math.cos(((((double) i) * rSlice) + (0.5d * rSlice)) + rStart) * ((double) (this.MaxSize + this.MinSize))) / 2.0d)) + ((float) this.xPosition);
                float yCenter = ((float) ((Math.sin(((((double) i) * rSlice) + (0.5d * rSlice)) + rStart) * ((double) (this.MaxSize + this.MinSize))) / 2.0d)) + ((float) this.yPosition);
                int h = this.MaxIconSize;
                int w = this.MaxIconSize;
                if (((RadialMenuItem) this.menuEntries.get(i)).getIcon() != null) {
                    Drawable drawable = ((RadialMenuItem) this.menuEntries.get(i)).getIcon();
                    h = getIconSize(drawable.getIntrinsicHeight(), this.MinIconSize, this.MaxIconSize);
                    w = getIconSize(drawable.getIntrinsicWidth(), this.MinIconSize, this.MaxIconSize);
                }
                this.iconRect[i] = new Rect(((int) xCenter) - (w / 2), ((int) yCenter) - (h / 2), ((int) xCenter) + (w / 2), ((int) yCenter) + (h / 2));
            }
            invalidate();
        }
    }

    private void determineOuterWedges(RadialMenuItem entry) {
        this.wedgeQty2 = entry.getChildren().size();
        float degSlice2 = 360.0f / ((float) this.wedgeQty2);
        float start_degSlice2 = 270.0f - (degSlice2 / 2.0f);
        double rSlice2 = 6.283185307179586d / ((double) this.wedgeQty2);
        double rStart2 = 4.71238898038469d - (rSlice2 / 2.0d);
        this.Wedges2 = new RadialMenuWedge[this.wedgeQty2];
        this.iconRect2 = new Rect[this.wedgeQty2];
        float fStartDegree = start_degSlice2;
        for (int i = 0; i < this.Wedges2.length; i++) {
            if (i != this.Wedges2.length - 1) {
                this.Wedges2[i] = new RadialMenuWedge(this.xPosition, this.yPosition, this.r2MinSize, this.r2MaxSize, fStartDegree, degSlice2);
                fStartDegree += degSlice2;
            } else {
                float fTemp = fStartDegree;
                while (fTemp > 360.0f) {
                    fTemp -= 360.0f;
                }
                this.Wedges2[i] = new RadialMenuWedge(this.xPosition, this.yPosition, this.r2MinSize, this.r2MaxSize, fStartDegree, start_degSlice2 - fTemp);
                fStartDegree += degSlice2;
            }
            entry.getLabel();
            float xCenter = ((float) (((Math.cos(((((double) i) * rSlice2) + (0.5d * rSlice2)) + rStart2) * ((double) (this.r2MaxSize + this.r2MinSize))) / 2.0d) + ((double) 0))) + ((float) this.xPosition);
            float yCenter = ((float) (((Math.sin(((((double) i) * rSlice2) + (0.5d * rSlice2)) + rStart2) * ((double) (this.r2MaxSize + this.r2MinSize))) / 2.0d) + ((double) 0))) + ((float) this.yPosition);
            int h = this.MaxIconSize;
            int w = this.MaxIconSize;
            if (((RadialMenuItem) entry.getChildren().get(i)).getIcon() != null) {
                Drawable drawable = ((RadialMenuItem) entry.getChildren().get(i)).getIcon();
                h = getIconSize(drawable.getIntrinsicHeight(), this.MinIconSize, this.MaxIconSize);
                w = getIconSize(drawable.getIntrinsicWidth(), this.MinIconSize, this.MaxIconSize);
            }
            this.iconRect2[i] = new Rect(((int) xCenter) - (w / 2), ((int) yCenter) - (h / 2), ((int) xCenter) + (w / 2), ((int) yCenter) + (h / 2));
        }
        this.wedge2Data = entry;
        invalidate();
    }

    private void determineHeaderBox() {
        this.headerTextLeft = this.xPosition - (this.textRect.width() / 2);
        this.headerTextBottom = ((this.yPosition - this.MaxSize) - this.headerBuffer) - this.textRect.bottom;
        int offset = this.MaxSize;
        if (offset < this.textRect.width() / 2) {
            offset = (this.textRect.width() / 2) + scalePX(3);
        }
        this.textBoxRect.set((float) (this.xPosition - offset), (float) ((((this.yPosition - this.MaxSize) - this.headerBuffer) - this.textRect.height()) - scalePX(3)), (float) (this.xPosition + offset), (float) (((this.yPosition - this.MaxSize) - this.headerBuffer) + scalePX(3)));
    }

    public void addMenuEntry(List<RadialMenuItem> menuItems) {
        this.menuEntries.addAll(menuItems);
        determineWedges();
    }

    public void addMenuEntry(RadialMenuItem menuItem) {
        this.menuEntries.add(menuItem);
        determineWedges();
    }

    public void setCenterCircle(RadialMenuItem menuItem) {
        this.centerCircle = menuItem;
    }

    public void setInnerRingRadius(int InnerRadius, int OuterRadius) {
        this.MinSize = scalePX(InnerRadius);
        this.MaxSize = scalePX(OuterRadius);
        determineWedges();
    }

    public void setOuterRingRadius(int InnerRadius, int OuterRadius) {
        this.r2MinSize = scalePX(InnerRadius);
        this.r2MaxSize = scalePX(OuterRadius);
        determineWedges();
    }

    public void setCenterCircleRadius(int centerRadius) {
        this.cRadius = scalePX(centerRadius);
        determineWedges();
    }

    public void setTextSize(int TextSize) {
        this.textSize = scalePX(TextSize);
        this.animateTextSize = this.textSize;
    }

    public void setIconSize(int minIconSize, int maxIconSize) {
        this.MinIconSize = scalePX(minIconSize);
        this.MaxIconSize = scalePX(maxIconSize);
        determineWedges();
    }

    public void setCenterLocation(int x, int y) {
        this.xPosition = x;
        this.yPosition = y;
        determineWedges();
        this.helper.onOpenAnimation(this, this.xPosition, this.yPosition, this.xSource, this.ySource);
    }

    public void setSourceLocation(int x, int y) {
        this.xSource = x;
        this.ySource = y;
        this.helper.onOpenAnimation(this, this.xPosition, this.yPosition, this.xSource, this.ySource);
    }

    public void setShowSourceLocation(boolean showSourceLocation) {
        this.showSource = showSourceLocation;
        this.helper.onOpenAnimation(this, this.xPosition, this.yPosition, this.xSource, this.ySource);
    }

    public void setAnimationSpeed(long millis) {
        this.helper.onOpenAnimation(this, this.xPosition, this.yPosition, this.xSource, this.ySource, millis);
    }

    public void setInnerRingColor(int color, int alpha) {
        this.defaultColor = color;
        this.defaultAlpha = alpha;
    }

    public void setOuterRingColor(int color, int alpha) {
        this.wedge2Color = color;
        this.wedge2Alpha = alpha;
    }

    public void setOutlineColor(int color, int alpha) {
        this.outlineColor = color;
        this.outlineAlpha = alpha;
    }

    public void setSelectedColor(int color, int alpha) {
        this.selectedColor = color;
        this.selectedAlpha = alpha;
    }

    public void setDisabledColor(int color, int alpha) {
        this.disabledColor = color;
        this.disabledAlpha = alpha;
    }

    public void setTextColor(int color, int alpha) {
        this.textColor = color;
        this.textAlpha = alpha;
    }

    public void setHeader(String header, int TextSize) {
        this.headerString = header;
        this.headerTextSize = scalePX(TextSize);
        this.HeaderBoxBounded = false;
    }

    public void setHeaderColors(int TextColor, int TextAlpha, int BgColor, int BgAlpha) {
        this.headerTextColor = TextColor;
        this.headerTextAlpha = TextAlpha;
        this.headerBackgroundColor = BgColor;
        this.headerBackgroundAlpha = BgAlpha;
    }

    public void show(View anchor, int posX, int posY) {
        int i = this.r2MaxSize + 6;
        this.yPosition = i;
        this.xPosition = i;
        this.ySource = 0;
        this.xSource = 0;
        this.mWindow.setContentView(this);
        this.mWindow.showAtLocation(anchor, 17, 0, 0);
    }

    public void show(View anchor) {
        this.mWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_blueborder));
        this.mWindow.setContentView(this);
        this.mWindow.showAtLocation(anchor, 0, this.xSource, this.ySource);
    }

    public void dismiss() {
        this.enabled = null;
        this.selected = null;
        this.Wedge2Shown = false;
        this.animateOuterIn = false;
        this.animateOuterOut = false;
        if (this.mWindow != null) {
            this.mWindow.dismiss();
        }
    }
}
