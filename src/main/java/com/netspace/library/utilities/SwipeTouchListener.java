package com.netspace.library.utilities;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SwipeTouchListener implements OnTouchListener {
    private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());
    private boolean mbEnable = true;

    private final class GestureListener extends SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        private GestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > 100.0f && Math.abs(velocityX) > 100.0f) {
                        if (diffX > 0.0f) {
                            SwipeTouchListener.this.onSwipeRight();
                        } else {
                            SwipeTouchListener.this.onSwipeLeft();
                        }
                    }
                } else if (Math.abs(diffY) > 100.0f && Math.abs(velocityY) > 100.0f) {
                    if (diffY > 0.0f) {
                        SwipeTouchListener.this.onSwipeBottom();
                    } else {
                        SwipeTouchListener.this.onSwipeTop();
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    public void setEnable(boolean bEnable) {
        this.mbEnable = bEnable;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        onBeforeTouch();
        if (this.mbEnable) {
            return this.gestureDetector.onTouchEvent(motionEvent);
        }
        return false;
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

    public void onBeforeTouch() {
    }
}
