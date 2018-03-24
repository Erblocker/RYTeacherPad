package org.achartengine.model;

import java.io.Serializable;

public final class Point implements Serializable {
    private float mX;
    private float mY;

    public Point(float x, float y) {
        this.mX = x;
        this.mY = y;
    }

    public float getX() {
        return this.mX;
    }

    public float getY() {
        return this.mY;
    }

    public void setX(float x) {
        this.mX = x;
    }

    public void setY(float y) {
        this.mY = y;
    }
}
