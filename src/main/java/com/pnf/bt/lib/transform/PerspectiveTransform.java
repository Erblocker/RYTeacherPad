package com.pnf.bt.lib.transform;

import android.graphics.PointF;
import java.lang.reflect.Array;

public final class PerspectiveTransform {
    private final double PERSPECTIVE_DIVIDE_EPSILON;
    public PointF dstPoint;
    public double m00;
    public double m01;
    public double m02;
    public double m10;
    public double m11;
    public double m12;
    public double m20;
    public double m21;
    public double m22;
    public PointF srcPoint;

    public PerspectiveTransform() {
        this.PERSPECTIVE_DIVIDE_EPSILON = 1.0E-10d;
        this.srcPoint = null;
        this.dstPoint = null;
        this.m22 = 1.0d;
        this.m11 = 1.0d;
        this.m00 = 1.0d;
        this.m21 = 0.0d;
        this.m20 = 0.0d;
        this.m12 = 0.0d;
        this.m10 = 0.0d;
        this.m02 = 0.0d;
        this.m01 = 0.0d;
    }

    public PerspectiveTransform(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        this.PERSPECTIVE_DIVIDE_EPSILON = 1.0E-10d;
        this.srcPoint = null;
        this.dstPoint = null;
        this.m00 = (double) m00;
        this.m01 = (double) m01;
        this.m02 = (double) m02;
        this.m10 = (double) m10;
        this.m11 = (double) m11;
        this.m12 = (double) m12;
        this.m20 = (double) m20;
        this.m21 = (double) m21;
        this.m22 = (double) m22;
    }

    public PerspectiveTransform(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
        this.PERSPECTIVE_DIVIDE_EPSILON = 1.0E-10d;
        this.srcPoint = null;
        this.dstPoint = null;
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    public PerspectiveTransform(float[] flatmatrix) {
        this.PERSPECTIVE_DIVIDE_EPSILON = 1.0E-10d;
        this.srcPoint = null;
        this.dstPoint = null;
        this.m00 = (double) flatmatrix[0];
        this.m01 = (double) flatmatrix[1];
        this.m02 = (double) flatmatrix[2];
        this.m10 = (double) flatmatrix[3];
        this.m11 = (double) flatmatrix[4];
        this.m12 = (double) flatmatrix[5];
        this.m20 = (double) flatmatrix[6];
        this.m21 = (double) flatmatrix[7];
        this.m22 = (double) flatmatrix[8];
    }

    public PerspectiveTransform(float[][] matrix) {
        this.PERSPECTIVE_DIVIDE_EPSILON = 1.0E-10d;
        this.srcPoint = null;
        this.dstPoint = null;
        this.m00 = (double) matrix[0][0];
        this.m01 = (double) matrix[0][1];
        this.m02 = (double) matrix[0][2];
        this.m10 = (double) matrix[1][0];
        this.m11 = (double) matrix[1][1];
        this.m12 = (double) matrix[1][2];
        this.m20 = (double) matrix[2][0];
        this.m21 = (double) matrix[2][1];
        this.m22 = (double) matrix[2][2];
    }

    public PerspectiveTransform(double[] flatmatrix) {
        this.PERSPECTIVE_DIVIDE_EPSILON = 1.0E-10d;
        this.srcPoint = null;
        this.dstPoint = null;
        this.m00 = flatmatrix[0];
        this.m01 = flatmatrix[1];
        this.m02 = flatmatrix[2];
        this.m10 = flatmatrix[3];
        this.m11 = flatmatrix[4];
        this.m12 = flatmatrix[5];
        this.m20 = flatmatrix[6];
        this.m21 = flatmatrix[7];
        this.m22 = flatmatrix[8];
    }

    public PerspectiveTransform(double[][] matrix) {
        this.PERSPECTIVE_DIVIDE_EPSILON = 1.0E-10d;
        this.srcPoint = null;
        this.dstPoint = null;
        this.m00 = matrix[0][0];
        this.m01 = matrix[0][1];
        this.m02 = matrix[0][2];
        this.m10 = matrix[1][0];
        this.m11 = matrix[1][1];
        this.m12 = matrix[1][2];
        this.m20 = matrix[2][0];
        this.m21 = matrix[2][1];
        this.m22 = matrix[2][2];
    }

    private final void makeAdjoint() {
        double m01p = (this.m12 * this.m20) - (this.m10 * this.m22);
        double m02p = (this.m10 * this.m21) - (this.m11 * this.m20);
        double m10p = (this.m02 * this.m21) - (this.m01 * this.m22);
        double m11p = (this.m00 * this.m22) - (this.m02 * this.m20);
        double m12p = (this.m01 * this.m20) - (this.m00 * this.m21);
        double m20p = (this.m01 * this.m12) - (this.m02 * this.m11);
        double m21p = (this.m02 * this.m10) - (this.m00 * this.m12);
        double m22p = (this.m00 * this.m11) - (this.m01 * this.m10);
        this.m00 = (this.m11 * this.m22) - (this.m12 * this.m21);
        this.m01 = m10p;
        this.m02 = m20p;
        this.m10 = m01p;
        this.m11 = m11p;
        this.m12 = m21p;
        this.m20 = m02p;
        this.m21 = m12p;
        this.m22 = m22p;
    }

    private final void normalize() {
        double invscale = 1.0d / this.m22;
        this.m00 *= invscale;
        this.m01 *= invscale;
        this.m02 *= invscale;
        this.m10 *= invscale;
        this.m11 *= invscale;
        this.m12 *= invscale;
        this.m20 *= invscale;
        this.m21 *= invscale;
        this.m22 = 1.0d;
    }

    private static final void getSquareToQuad(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, PerspectiveTransform tx) {
        double dx3 = ((x0 - x1) + x2) - x3;
        double dy3 = ((y0 - y1) + y2) - y3;
        tx.m22 = 1.0d;
        if (dx3 == 0.0d && dy3 == 0.0d) {
            tx.m00 = x1 - x0;
            tx.m01 = x2 - x1;
            tx.m02 = x0;
            tx.m10 = y1 - y0;
            tx.m11 = y2 - y1;
            tx.m12 = y0;
            tx.m20 = 0.0d;
            tx.m21 = 0.0d;
            return;
        }
        double dx1 = x1 - x2;
        double dy1 = y1 - y2;
        double dx2 = x3 - x2;
        double dy2 = y3 - y2;
        double invdet = 1.0d / ((dx1 * dy2) - (dx2 * dy1));
        tx.m20 = ((dx3 * dy2) - (dx2 * dy3)) * invdet;
        tx.m21 = ((dx1 * dy3) - (dx3 * dy1)) * invdet;
        tx.m00 = (x1 - x0) + (tx.m20 * x1);
        tx.m01 = (x3 - x0) + (tx.m21 * x3);
        tx.m02 = x0;
        tx.m10 = (y1 - y0) + (tx.m20 * y1);
        tx.m11 = (y3 - y0) + (tx.m21 * y3);
        tx.m12 = y0;
    }

    public static PerspectiveTransform getSquareToQuad(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        PerspectiveTransform tx = new PerspectiveTransform();
        getSquareToQuad(x0, y0, x1, y1, x2, y2, x3, y3, tx);
        return tx;
    }

    public PerspectiveTransform getSquareToQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        return getSquareToQuad((double) x0, (double) y0, (double) x1, (double) y1, (double) x2, (double) y2, (double) x3, (double) y3);
    }

    public static PerspectiveTransform getQuadToSquare(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        PerspectiveTransform tx = new PerspectiveTransform();
        getSquareToQuad(x0, y0, x1, y1, x2, y2, x3, y3, tx);
        tx.makeAdjoint();
        return tx;
    }

    public PerspectiveTransform getQuadToSquare(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        return getQuadToSquare((double) x0, (double) y0, (double) x1, (double) y1, (double) x2, (double) y2, (double) x3, (double) y3);
    }

    public static PerspectiveTransform getQuadToQuad(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double x0p, double y0p, double x1p, double y1p, double x2p, double y2p, double x3p, double y3p) {
        PerspectiveTransform tx1 = getQuadToSquare(x0, y0, x1, y1, x2, y2, x3, y3);
        tx1.concatenate(getSquareToQuad(x0p, y0p, x1p, y1p, x2p, y2p, x3p, y3p));
        return tx1;
    }

    public PerspectiveTransform getQuadToQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, float x0p, float y0p, float x1p, float y1p, float x2p, float y2p, float x3p, float y3p) {
        return getQuadToQuad((double) x0, (double) y0, (double) x1, (double) y1, (double) x2, (double) y2, (double) x3, (double) y3, (double) x0p, (double) y0p, (double) x1p, (double) y1p, (double) x2p, (double) y2p, (double) x3p, (double) y3p);
    }

    public double getDeterminant() {
        return ((this.m00 * ((this.m11 * this.m22) - (this.m12 * this.m21))) - (this.m01 * ((this.m10 * this.m22) - (this.m12 * this.m20)))) + (this.m02 * ((this.m10 * this.m21) - (this.m11 * this.m20)));
    }

    public double[] getMatrix(double[] flatmatrix) {
        if (flatmatrix == null) {
            flatmatrix = new double[9];
        }
        flatmatrix[0] = this.m00;
        flatmatrix[1] = this.m01;
        flatmatrix[2] = this.m02;
        flatmatrix[3] = this.m10;
        flatmatrix[4] = this.m11;
        flatmatrix[5] = this.m12;
        flatmatrix[6] = this.m20;
        flatmatrix[7] = this.m21;
        flatmatrix[8] = this.m22;
        return flatmatrix;
    }

    public double[][] getMatrix(double[][] matrix) {
        if (matrix == null) {
            matrix = (double[][]) Array.newInstance(Double.TYPE, new int[]{3, 3});
        }
        matrix[0][0] = this.m00;
        matrix[0][1] = this.m01;
        matrix[0][2] = this.m02;
        matrix[1][0] = this.m10;
        matrix[1][1] = this.m11;
        matrix[1][2] = this.m12;
        matrix[2][0] = this.m20;
        matrix[2][1] = this.m21;
        matrix[2][2] = this.m22;
        return matrix;
    }

    public void translate(double tx, double ty) {
        PerspectiveTransform Tx = new PerspectiveTransform();
        Tx.setToTranslation(tx, ty);
        concatenate(Tx);
    }

    public void rotate(double theta) {
        PerspectiveTransform Tx = new PerspectiveTransform();
        Tx.setToRotation(theta);
        concatenate(Tx);
    }

    public void rotate(double theta, double x, double y) {
        PerspectiveTransform Tx = new PerspectiveTransform();
        Tx.setToRotation(theta, x, y);
        concatenate(Tx);
    }

    public void scale(double sx, double sy) {
        PerspectiveTransform Tx = new PerspectiveTransform();
        Tx.setToScale(sx, sy);
        concatenate(Tx);
    }

    public void shear(double shx, double shy) {
        PerspectiveTransform Tx = new PerspectiveTransform();
        Tx.setToShear(shx, shy);
        concatenate(Tx);
    }

    public void setToIdentity() {
        this.m22 = 1.0d;
        this.m11 = 1.0d;
        this.m00 = 1.0d;
        this.m21 = 0.0d;
        this.m12 = 0.0d;
        this.m20 = 0.0d;
        this.m02 = 0.0d;
        this.m10 = 0.0d;
        this.m01 = 0.0d;
    }

    public void setToTranslation(double tx, double ty) {
        this.m00 = 1.0d;
        this.m01 = 0.0d;
        this.m02 = tx;
        this.m10 = 0.0d;
        this.m11 = 1.0d;
        this.m12 = ty;
        this.m20 = 0.0d;
        this.m21 = 0.0d;
        this.m22 = 1.0d;
    }

    public void setToRotation(double theta) {
        this.m00 = Math.cos(theta);
        this.m01 = -Math.sin(theta);
        this.m02 = 0.0d;
        this.m10 = -this.m01;
        this.m11 = this.m00;
        this.m12 = 0.0d;
        this.m20 = 0.0d;
        this.m21 = 0.0d;
        this.m22 = 1.0d;
    }

    public void setToRotation(double theta, double x, double y) {
        setToRotation(theta);
        double sin = this.m10;
        double oneMinusCos = 1.0d - this.m00;
        this.m02 = (x * oneMinusCos) + (y * sin);
        this.m12 = (y * oneMinusCos) - (x * sin);
    }

    public void setToScale(double sx, double sy) {
        this.m00 = sx;
        this.m01 = 0.0d;
        this.m02 = 0.0d;
        this.m10 = 0.0d;
        this.m11 = sy;
        this.m12 = 0.0d;
        this.m20 = 0.0d;
        this.m21 = 0.0d;
        this.m22 = 1.0d;
    }

    public void setToShear(double shx, double shy) {
        this.m00 = 1.0d;
        this.m01 = shx;
        this.m02 = 0.0d;
        this.m10 = shy;
        this.m11 = 1.0d;
        this.m12 = 0.0d;
        this.m20 = 0.0d;
        this.m21 = 0.0d;
        this.m22 = 1.0d;
    }

    public void setTransform(PerspectiveTransform Tx) {
        this.m00 = Tx.m00;
        this.m01 = Tx.m01;
        this.m02 = Tx.m02;
        this.m10 = Tx.m10;
        this.m11 = Tx.m11;
        this.m12 = Tx.m12;
        this.m20 = Tx.m20;
        this.m21 = Tx.m21;
        this.m22 = Tx.m22;
    }

    public void setTransform(float m00, float m10, float m20, float m01, float m11, float m21, float m02, float m12, float m22) {
        this.m00 = (double) m00;
        this.m01 = (double) m01;
        this.m02 = (double) m02;
        this.m10 = (double) m10;
        this.m11 = (double) m11;
        this.m12 = (double) m12;
        this.m20 = (double) m20;
        this.m21 = (double) m21;
        this.m22 = (double) m22;
    }

    public void setTransform(double[][] matrix) {
        this.m00 = matrix[0][0];
        this.m01 = matrix[0][1];
        this.m02 = matrix[0][2];
        this.m10 = matrix[1][0];
        this.m11 = matrix[1][1];
        this.m12 = matrix[1][2];
        this.m20 = matrix[2][0];
        this.m21 = matrix[2][1];
        this.m22 = matrix[2][2];
    }

    public void concatenate(PerspectiveTransform Tx) {
        double m10p = ((this.m00 * Tx.m10) + (this.m10 * Tx.m11)) + (this.m20 * Tx.m12);
        double m20p = ((this.m00 * Tx.m20) + (this.m10 * Tx.m21)) + (this.m20 * Tx.m22);
        double m01p = ((this.m01 * Tx.m00) + (this.m11 * Tx.m01)) + (this.m21 * Tx.m02);
        double m11p = ((this.m01 * Tx.m10) + (this.m11 * Tx.m11)) + (this.m21 * Tx.m12);
        double m21p = ((this.m01 * Tx.m20) + (this.m11 * Tx.m21)) + (this.m21 * Tx.m22);
        double m02p = ((this.m02 * Tx.m00) + (this.m12 * Tx.m01)) + (this.m22 * Tx.m02);
        double m12p = ((this.m02 * Tx.m10) + (this.m12 * Tx.m11)) + (this.m22 * Tx.m12);
        double m22p = ((this.m02 * Tx.m20) + (this.m12 * Tx.m21)) + (this.m22 * Tx.m22);
        this.m00 = ((this.m00 * Tx.m00) + (this.m10 * Tx.m01)) + (this.m20 * Tx.m02);
        this.m10 = m10p;
        this.m20 = m20p;
        this.m01 = m01p;
        this.m11 = m11p;
        this.m21 = m21p;
        this.m02 = m02p;
        this.m12 = m12p;
        this.m22 = m22p;
    }

    public void preConcatenate(PerspectiveTransform Tx) {
        double m10p = ((Tx.m00 * this.m10) + (Tx.m10 * this.m11)) + (Tx.m20 * this.m12);
        double m20p = ((Tx.m00 * this.m20) + (Tx.m10 * this.m21)) + (Tx.m20 * this.m22);
        double m01p = ((Tx.m01 * this.m00) + (Tx.m11 * this.m01)) + (Tx.m21 * this.m02);
        double m11p = ((Tx.m01 * this.m10) + (Tx.m11 * this.m11)) + (Tx.m21 * this.m12);
        double m21p = ((Tx.m01 * this.m20) + (Tx.m11 * this.m21)) + (Tx.m21 * this.m22);
        double m02p = ((Tx.m02 * this.m00) + (Tx.m12 * this.m01)) + (Tx.m22 * this.m02);
        double m12p = ((Tx.m02 * this.m10) + (Tx.m12 * this.m11)) + (Tx.m22 * this.m12);
        double m22p = ((Tx.m02 * this.m20) + (Tx.m12 * this.m21)) + (Tx.m22 * this.m22);
        this.m00 = ((Tx.m00 * this.m00) + (Tx.m10 * this.m01)) + (Tx.m20 * this.m02);
        this.m10 = m10p;
        this.m20 = m20p;
        this.m01 = m01p;
        this.m11 = m11p;
        this.m21 = m21p;
        this.m02 = m02p;
        this.m12 = m12p;
        this.m22 = m22p;
    }

    public PerspectiveTransform createAdjoint() throws CloneNotSupportedException {
        PerspectiveTransform tx = (PerspectiveTransform) clone();
        tx.makeAdjoint();
        return tx;
    }

    public PointF transform(PointF ptSrc, PointF ptDst) {
        if (ptDst == null) {
            ptDst = new PointF();
        }
        float x = ptSrc.x;
        float y = ptSrc.y;
        float w = (float) (((this.m20 * ((double) x)) + (this.m21 * ((double) y))) + this.m22);
        ptDst.set(((float) (((this.m00 * ((double) x)) + (this.m01 * ((double) y))) + this.m02)) / w, ((float) (((this.m10 * ((double) x)) + (this.m11 * ((double) y))) + this.m12)) / w);
        return ptDst;
    }

    public void transform(android.graphics.PointF[] r19, int r20, android.graphics.PointF[] r21, int r22, int r23) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:36)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:60)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r18 = this;
        if (r19 == 0) goto L_0x0074;
    L_0x0002:
        r4 = r23;
        r3 = r22;
        r6 = r20;
    L_0x0008:
        r23 = r4 + -1;
        if (r4 > 0) goto L_0x000d;
    L_0x000c:
        return;
    L_0x000d:
        r20 = r6 + 1;
        r5 = r19[r6];
        r22 = r3 + 1;
        r2 = r21[r3];
        if (r2 != 0) goto L_0x0020;
    L_0x0017:
        r2 = new android.graphics.PointF;
        r2.<init>();
        r10 = r22 + -1;
        r21[r10] = r2;
    L_0x0020:
        r8 = r5.x;
        r9 = r5.y;
        r0 = r18;
        r10 = r0.m20;
        r12 = (double) r8;
        r10 = r10 * r12;
        r0 = r18;
        r12 = r0.m21;
        r14 = (double) r9;
        r12 = r12 * r14;
        r10 = r10 + r12;
        r0 = r18;
        r12 = r0.m22;
        r10 = r10 + r12;
        r7 = (float) r10;
        r10 = 0;
        r10 = (r7 > r10 ? 1 : (r7 == r10 ? 0 : -1));
        if (r10 != 0) goto L_0x0046;
    L_0x003c:
        r2.set(r8, r9);
        r4 = r23;
        r3 = r22;
        r6 = r20;
        goto L_0x0008;
    L_0x0046:
        r0 = r18;
        r10 = r0.m00;
        r12 = (double) r8;
        r10 = r10 * r12;
        r0 = r18;
        r12 = r0.m01;
        r14 = (double) r9;
        r12 = r12 * r14;
        r10 = r10 + r12;
        r0 = r18;
        r12 = r0.m02;
        r10 = r10 + r12;
        r10 = (float) r10;
        r10 = r10 / r7;
        r0 = r18;
        r12 = r0.m10;
        r14 = (double) r8;
        r12 = r12 * r14;
        r0 = r18;
        r14 = r0.m11;
        r0 = (double) r9;
        r16 = r0;
        r14 = r14 * r16;
        r12 = r12 + r14;
        r0 = r18;
        r14 = r0.m12;
        r12 = r12 + r14;
        r11 = (float) r12;
        r11 = r11 / r7;
        r2.set(r10, r11);
    L_0x0074:
        r4 = r23;
        r3 = r22;
        r6 = r20;
        goto L_0x0008;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.pnf.bt.lib.transform.PerspectiveTransform.transform(android.graphics.PointF[], int, android.graphics.PointF[], int, int):void");
    }

    public void transform(float[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
        int numPts2;
        int dstOff2;
        int srcOff2;
        if (dstPts == null) {
            dstPts = new float[((numPts * 2) + dstOff)];
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        } else {
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        }
        while (true) {
            numPts = numPts2 - 1;
            if (numPts2 > 0) {
                srcOff = srcOff2 + 1;
                float x = srcPts[srcOff2];
                srcOff2 = srcOff + 1;
                float y = srcPts[srcOff];
                double w = ((this.m20 * ((double) x)) + (this.m21 * ((double) y))) + this.m22;
                if (w == 0.0d) {
                    dstOff = dstOff2 + 1;
                    dstPts[dstOff2] = x;
                    dstOff2 = dstOff + 1;
                    dstPts[dstOff] = y;
                    numPts2 = numPts;
                } else {
                    dstOff = dstOff2 + 1;
                    dstPts[dstOff2] = (float) ((((this.m00 * ((double) x)) + (this.m01 * ((double) y))) + this.m02) / w);
                    dstOff2 = dstOff + 1;
                    dstPts[dstOff] = (float) ((((this.m10 * ((double) x)) + (this.m11 * ((double) y))) + this.m12) / w);
                    numPts2 = numPts;
                }
            } else {
                return;
            }
        }
    }

    public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        int numPts2;
        int dstOff2;
        int srcOff2;
        if (dstPts == null) {
            dstPts = new double[((numPts * 2) + dstOff)];
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        } else {
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        }
        while (true) {
            numPts = numPts2 - 1;
            if (numPts2 > 0) {
                srcOff = srcOff2 + 1;
                double x = srcPts[srcOff2];
                srcOff2 = srcOff + 1;
                double y = srcPts[srcOff];
                double w = ((this.m20 * x) + (this.m21 * y)) + this.m22;
                if (w == 0.0d) {
                    dstOff = dstOff2 + 1;
                    dstPts[dstOff2] = x;
                    dstOff2 = dstOff + 1;
                    dstPts[dstOff] = y;
                    numPts2 = numPts;
                } else {
                    dstOff = dstOff2 + 1;
                    dstPts[dstOff2] = (((this.m00 * x) + (this.m01 * y)) + this.m02) / w;
                    dstOff2 = dstOff + 1;
                    dstPts[dstOff] = (((this.m10 * x) + (this.m11 * y)) + this.m12) / w;
                    numPts2 = numPts;
                }
            } else {
                return;
            }
        }
    }

    public void transform(float[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        int numPts2;
        int dstOff2;
        int srcOff2;
        if (dstPts == null) {
            dstPts = new double[((numPts * 2) + dstOff)];
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        } else {
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        }
        while (true) {
            numPts = numPts2 - 1;
            if (numPts2 > 0) {
                srcOff = srcOff2 + 1;
                float x = srcPts[srcOff2];
                srcOff2 = srcOff + 1;
                float y = srcPts[srcOff];
                double w = ((this.m20 * ((double) x)) + (this.m21 * ((double) y))) + this.m22;
                if (w == 0.0d) {
                    dstOff = dstOff2 + 1;
                    dstPts[dstOff2] = (double) x;
                    dstOff2 = dstOff + 1;
                    dstPts[dstOff] = (double) y;
                    numPts2 = numPts;
                } else {
                    dstOff = dstOff2 + 1;
                    dstPts[dstOff2] = (((this.m00 * ((double) x)) + (this.m01 * ((double) y))) + this.m02) / w;
                    dstOff2 = dstOff + 1;
                    dstPts[dstOff] = (((this.m10 * ((double) x)) + (this.m11 * ((double) y))) + this.m12) / w;
                    numPts2 = numPts;
                }
            } else {
                return;
            }
        }
    }

    public void transform(double[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
        int numPts2;
        int dstOff2;
        int srcOff2;
        if (dstPts == null) {
            dstPts = new float[((numPts * 2) + dstOff)];
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        } else {
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        }
        while (true) {
            numPts = numPts2 - 1;
            if (numPts2 > 0) {
                srcOff = srcOff2 + 1;
                double x = srcPts[srcOff2];
                srcOff2 = srcOff + 1;
                double y = srcPts[srcOff];
                double w = ((this.m20 * x) + (this.m21 * y)) + this.m22;
                if (w == 0.0d) {
                    dstOff = dstOff2 + 1;
                    dstPts[dstOff2] = (float) x;
                    dstOff2 = dstOff + 1;
                    dstPts[dstOff] = (float) y;
                    numPts2 = numPts;
                } else {
                    dstOff = dstOff2 + 1;
                    dstPts[dstOff2] = (float) ((((this.m00 * x) + (this.m01 * y)) + this.m02) / w);
                    dstOff2 = dstOff + 1;
                    dstPts[dstOff] = (float) ((((this.m10 * x) + (this.m11 * y)) + this.m12) / w);
                    numPts2 = numPts;
                }
            } else {
                return;
            }
        }
    }

    public PointF inverseTransform(PointF ptSrc, PointF ptDst) {
        if (ptDst == null) {
            ptDst = new PointF();
        }
        double x = (double) ptSrc.x;
        double y = (double) ptSrc.y;
        double tmp_x = ((((this.m11 * this.m22) - (this.m12 * this.m21)) * x) + (((this.m02 * this.m21) - (this.m01 * this.m22)) * y)) + ((this.m01 * this.m12) - (this.m02 * this.m11));
        double tmp_y = ((((this.m12 * this.m20) - (this.m10 * this.m22)) * x) + (((this.m00 * this.m22) - (this.m02 * this.m20)) * y)) + ((this.m02 * this.m10) - (this.m00 * this.m12));
        double w = ((((this.m10 * this.m21) - (this.m11 * this.m20)) * x) + (((this.m01 * this.m20) - (this.m00 * this.m21)) * y)) + ((this.m00 * this.m11) - (this.m01 * this.m10));
        double wabs = w;
        if (w < 0.0d) {
            wabs = -w;
        }
        PointF pointF = ptDst;
        pointF.set((float) (tmp_x / w), (float) (tmp_y / w));
        return ptDst;
    }

    public void inverseTransform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        int numPts2;
        int dstOff2;
        int srcOff2;
        if (dstPts == null) {
            dstPts = new double[((numPts * 2) + dstOff)];
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        } else {
            numPts2 = numPts;
            dstOff2 = dstOff;
            srcOff2 = srcOff;
        }
        while (true) {
            numPts = numPts2 - 1;
            if (numPts2 > 0) {
                srcOff = srcOff2 + 1;
                double x = srcPts[srcOff2];
                srcOff2 = srcOff + 1;
                double y = srcPts[srcOff];
                double tmp_x = ((((this.m11 * this.m22) - (this.m12 * this.m21)) * x) + (((this.m02 * this.m21) - (this.m01 * this.m22)) * y)) + ((this.m01 * this.m12) - (this.m02 * this.m11));
                double tmp_y = ((((this.m12 * this.m20) - (this.m10 * this.m22)) * x) + (((this.m00 * this.m22) - (this.m02 * this.m20)) * y)) + ((this.m02 * this.m10) - (this.m00 * this.m12));
                double w = ((((this.m10 * this.m21) - (this.m11 * this.m20)) * x) + (((this.m01 * this.m20) - (this.m00 * this.m21)) * y)) + ((this.m00 * this.m11) - (this.m01 * this.m10));
                double wabs = w;
                if (w < 0.0d) {
                    wabs = -w;
                }
                dstOff = dstOff2 + 1;
                dstPts[dstOff2] = tmp_x / w;
                dstOff2 = dstOff + 1;
                dstPts[dstOff] = tmp_y / w;
                numPts2 = numPts;
            } else {
                return;
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Perspective transform matrix\n");
        sb.append(this.m00);
        sb.append("\t");
        sb.append(this.m01);
        sb.append("\t");
        sb.append(this.m02);
        sb.append("\n");
        sb.append(this.m10);
        sb.append("\t");
        sb.append(this.m11);
        sb.append("\t");
        sb.append(this.m12);
        sb.append("\n");
        sb.append(this.m20);
        sb.append("\t");
        sb.append(this.m21);
        sb.append("\t");
        sb.append(this.m22);
        sb.append("\n");
        return new String(sb);
    }

    public boolean isIdentity() {
        return this.m01 == 0.0d && this.m02 == 0.0d && this.m10 == 0.0d && this.m12 == 0.0d && this.m20 == 0.0d && this.m21 == 0.0d && this.m22 != 0.0d && this.m00 / this.m22 == 1.0d && this.m11 / this.m22 == 1.0d;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PerspectiveTransform)) {
            return false;
        }
        PerspectiveTransform a = (PerspectiveTransform) obj;
        if (this.m00 == a.m00 && this.m10 == a.m10 && this.m20 == a.m20 && this.m01 == a.m01 && this.m11 == a.m11 && this.m21 == a.m21 && this.m02 == a.m02 && this.m12 == a.m12 && this.m22 == a.m22) {
            return true;
        }
        return false;
    }

    public PointF GetCoordinatePostionXY4Cal(float x, float y) {
        if (this.srcPoint == null) {
            this.srcPoint = new PointF(x, y);
        }
        if (this.dstPoint == null) {
            this.dstPoint = new PointF(0.0f, 0.0f);
        }
        this.srcPoint.set(x, y);
        transform(this.srcPoint, this.dstPoint);
        return this.dstPoint;
    }

    public PointF GetCoordinateStationXY4Cal(float x, float y) {
        if (this.srcPoint == null) {
            this.srcPoint = new PointF(x, y);
        }
        if (this.dstPoint == null) {
            this.dstPoint = new PointF(0.0f, 0.0f);
        }
        this.srcPoint.set(x, y);
        inverseTransform(this.srcPoint, this.dstPoint);
        return this.dstPoint;
    }
}
