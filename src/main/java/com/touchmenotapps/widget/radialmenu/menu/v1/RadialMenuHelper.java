package com.touchmenotapps.widget.radialmenu.menu.v1;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import org.apache.http.HttpStatus;

public class RadialMenuHelper {
    private long animationSpeed = 0;
    private TranslateAnimation move;
    private RotateAnimation rotate;
    private ScaleAnimation scale;

    protected PopupWindow initPopup(Context context) {
        PopupWindow window = new PopupWindow(context);
        window.setWidth(HttpStatus.SC_BAD_REQUEST);
        window.setHeight(HttpStatus.SC_BAD_REQUEST);
        window.setTouchable(true);
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.setBackgroundDrawable(new BitmapDrawable());
        return window;
    }

    protected void onOpenAnimation(View view, int xPosition, int yPosition, int xSource, int ySource) {
        this.rotate = new RotateAnimation(0.0f, 360.0f, (float) xPosition, (float) yPosition);
        this.scale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, (float) xPosition, (float) yPosition);
        this.scale.setInterpolator(new DecelerateInterpolator());
        this.move = new TranslateAnimation((float) (xSource - xPosition), 0.0f, (float) (ySource - yPosition), 0.0f);
        AnimationSet spriteAnimation = new AnimationSet(true);
        spriteAnimation.addAnimation(this.rotate);
        spriteAnimation.addAnimation(this.scale);
        spriteAnimation.addAnimation(this.move);
        spriteAnimation.setDuration(this.animationSpeed);
        view.startAnimation(spriteAnimation);
    }

    protected void onOpenAnimation(View view, int xPosition, int yPosition, int xSource, int ySource, long animTime) {
        this.rotate = new RotateAnimation(0.0f, 360.0f, (float) xPosition, (float) yPosition);
        this.scale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, (float) xPosition, (float) yPosition);
        this.scale.setInterpolator(new DecelerateInterpolator());
        this.move = new TranslateAnimation((float) (xSource - xPosition), 0.0f, (float) (ySource - yPosition), 0.0f);
        AnimationSet spriteAnimation = new AnimationSet(true);
        spriteAnimation.addAnimation(this.rotate);
        spriteAnimation.addAnimation(this.scale);
        spriteAnimation.addAnimation(this.move);
        spriteAnimation.setDuration(animTime);
        view.startAnimation(spriteAnimation);
    }

    protected void onCloseAnimation(View view, int xPosition, int yPosition, int xSource, int ySource) {
        this.rotate = new RotateAnimation(360.0f, 0.0f, (float) xPosition, (float) yPosition);
        this.scale = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, (float) xPosition, (float) yPosition);
        this.scale.setInterpolator(new AccelerateInterpolator());
        this.move = new TranslateAnimation(0.0f, (float) (xSource - xPosition), 0.0f, (float) (ySource - yPosition));
        AnimationSet spriteAnimation = new AnimationSet(true);
        spriteAnimation.addAnimation(this.rotate);
        spriteAnimation.addAnimation(this.scale);
        spriteAnimation.addAnimation(this.move);
        spriteAnimation.setDuration(this.animationSpeed);
        view.startAnimation(spriteAnimation);
    }

    protected void onCloseAnimation(View view, int xPosition, int yPosition, int xSource, int ySource, long animTime) {
        this.rotate = new RotateAnimation(360.0f, 0.0f, (float) xPosition, (float) yPosition);
        this.scale = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, (float) xPosition, (float) yPosition);
        this.scale.setInterpolator(new AccelerateInterpolator());
        this.move = new TranslateAnimation(0.0f, (float) (xSource - xPosition), 0.0f, (float) (ySource - yPosition));
        AnimationSet spriteAnimation = new AnimationSet(true);
        spriteAnimation.addAnimation(this.rotate);
        spriteAnimation.addAnimation(this.scale);
        spriteAnimation.addAnimation(this.move);
        spriteAnimation.setDuration(animTime);
        view.startAnimation(spriteAnimation);
    }

    protected boolean pntInWedge(double px, double py, float xRadiusCenter, float yRadiusCenter, int innerRadius, int outerRadius, double startAngle, double sweepAngle) {
        double diffX = px - ((double) xRadiusCenter);
        double diffY = py - ((double) yRadiusCenter);
        double angle = Math.atan2(diffY, diffX);
        if (angle < 0.0d) {
            angle += 6.283185307179586d;
        }
        if (startAngle >= 6.283185307179586d) {
            startAngle -= 6.283185307179586d;
        }
        if ((angle >= startAngle && angle <= startAngle + sweepAngle) || (6.283185307179586d + angle >= startAngle && 6.283185307179586d + angle <= startAngle + sweepAngle)) {
            double dist = (diffX * diffX) + (diffY * diffY);
            if (dist < ((double) (outerRadius * outerRadius)) && dist > ((double) (innerRadius * innerRadius))) {
                return true;
            }
        }
        return false;
    }

    protected boolean pntInCircle(double px, double py, double x1, double y1, double radius) {
        double diffX = x1 - px;
        double diffY = y1 - py;
        if ((diffX * diffX) + (diffY * diffY) < radius * radius) {
            return true;
        }
        return false;
    }
}
