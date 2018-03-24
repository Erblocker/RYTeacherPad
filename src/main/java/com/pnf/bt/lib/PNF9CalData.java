package com.pnf.bt.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.graphics.PointF;
import com.pnf.bt.lib.transform.PerspectiveTransform;

public class PNF9CalData {
    public double[] Intercept = new double[5];
    final String PNF9CalSharedPre = "PNFCalibData9Cal";
    PointF dstPoint = null;
    boolean isExistCaliData = false;
    boolean isLetterPaper = false;
    Context mContext;
    PerspectiveTransform mPTF1;
    PerspectiveTransform mPTF2;
    PerspectiveTransform mPTF3;
    PerspectiveTransform mPTF4;
    PointF[] m_posPhysicalPen = null;
    PointF[] m_posScreen = null;
    Point[] m_vm_posScreen = null;
    int margin = 0;
    public double[] scope = new double[5];
    PointF srcPoint = null;

    public PNF9CalData(Context con) {
        int i;
        this.mContext = con;
        this.isExistCaliData = existCalibrationInfo();
        this.m_posPhysicalPen = new PointF[9];
        this.m_posScreen = new PointF[9];
        for (i = 0; i < 9; i++) {
            this.m_posPhysicalPen[i] = new PointF();
            this.m_posScreen[i] = new PointF();
        }
        this.m_vm_posScreen = new Point[9];
        for (i = 0; i < 9; i++) {
            this.m_vm_posScreen[i] = new Point();
        }
    }

    public PointF getPNFPerspective(float x, float y) {
        if (this.srcPoint == null) {
            this.srcPoint = new PointF(x, y);
        }
        if (this.dstPoint == null) {
            this.dstPoint = new PointF(0.0f, 0.0f);
        }
        if (this.mPTF1 == null || this.mPTF2 == null || this.mPTF3 == null || this.mPTF4 == null) {
            return this.dstPoint;
        }
        this.srcPoint.set(x, y);
        switch (GetRectPostion(x, y)) {
            case 1:
                this.mPTF1.transform(this.srcPoint, this.dstPoint);
                break;
            case 2:
                this.mPTF2.transform(this.srcPoint, this.dstPoint);
                break;
            case 3:
                this.mPTF3.transform(this.srcPoint, this.dstPoint);
                break;
            case 4:
                this.mPTF4.transform(this.srcPoint, this.dstPoint);
                break;
        }
        return this.dstPoint;
    }

    int GetRectPostion(float x, float y) {
        double referencePointY1 = (this.scope[0] * ((double) x)) + this.Intercept[0];
        double referencePointY2 = (this.scope[1] * ((double) x)) + this.Intercept[1];
        double referencePointX1 = (((double) y) - this.Intercept[2]) / this.scope[2];
        double referencePointX2 = (((double) y) - this.Intercept[3]) / this.scope[3];
        if (((double) y) >= referencePointY1 || ((double) y) >= referencePointY2) {
            if (((double) y) < referencePointY1 || ((double) y) < referencePointY2) {
                return 0;
            }
            if (((double) x) < referencePointX2) {
                return 3;
            }
            return 4;
        } else if (((double) x) < referencePointX1) {
            return 1;
        } else {
            return 2;
        }
    }

    public void setPTFQuadToQuad() {
        setPTFQuadToQuadPoint();
    }

    public void setPTFQuadToQuadPoint() {
        makeVmposScreen();
        this.mPTF1 = PerspectiveTransform.getQuadToQuad((double) this.m_posPhysicalPen[0].x, (double) this.m_posPhysicalPen[0].y, (double) this.m_posPhysicalPen[5].x, (double) this.m_posPhysicalPen[5].y, (double) this.m_posPhysicalPen[4].x, (double) this.m_posPhysicalPen[4].y, (double) this.m_posPhysicalPen[1].x, (double) this.m_posPhysicalPen[1].y, (double) this.m_vm_posScreen[0].x, (double) this.m_vm_posScreen[0].y, (double) this.m_vm_posScreen[5].x, (double) this.m_vm_posScreen[5].y, (double) this.m_vm_posScreen[4].x, (double) this.m_vm_posScreen[4].y, (double) this.m_vm_posScreen[1].x, (double) this.m_vm_posScreen[1].y);
        this.mPTF2 = PerspectiveTransform.getQuadToQuad((double) this.m_posPhysicalPen[5].x, (double) this.m_posPhysicalPen[5].y, (double) this.m_posPhysicalPen[6].x, (double) this.m_posPhysicalPen[6].y, (double) this.m_posPhysicalPen[7].x, (double) this.m_posPhysicalPen[7].y, (double) this.m_posPhysicalPen[4].x, (double) this.m_posPhysicalPen[4].y, (double) this.m_vm_posScreen[5].x, (double) this.m_vm_posScreen[5].y, (double) this.m_vm_posScreen[6].x, (double) this.m_vm_posScreen[6].y, (double) this.m_vm_posScreen[7].x, (double) this.m_vm_posScreen[7].y, (double) this.m_vm_posScreen[4].x, (double) this.m_vm_posScreen[4].y);
        this.mPTF3 = PerspectiveTransform.getQuadToQuad((double) this.m_posPhysicalPen[1].x, (double) this.m_posPhysicalPen[1].y, (double) this.m_posPhysicalPen[4].x, (double) this.m_posPhysicalPen[4].y, (double) this.m_posPhysicalPen[3].x, (double) this.m_posPhysicalPen[3].y, (double) this.m_posPhysicalPen[2].x, (double) this.m_posPhysicalPen[2].y, (double) this.m_vm_posScreen[1].x, (double) this.m_vm_posScreen[1].y, (double) this.m_vm_posScreen[4].x, (double) this.m_vm_posScreen[4].y, (double) this.m_vm_posScreen[3].x, (double) this.m_vm_posScreen[3].y, (double) this.m_vm_posScreen[2].x, (double) this.m_vm_posScreen[2].y);
        this.mPTF4 = PerspectiveTransform.getQuadToQuad((double) this.m_posPhysicalPen[4].x, (double) this.m_posPhysicalPen[4].y, (double) this.m_posPhysicalPen[7].x, (double) this.m_posPhysicalPen[7].y, (double) this.m_posPhysicalPen[8].x, (double) this.m_posPhysicalPen[8].y, (double) this.m_posPhysicalPen[3].x, (double) this.m_posPhysicalPen[3].y, (double) this.m_vm_posScreen[4].x, (double) this.m_vm_posScreen[4].y, (double) this.m_vm_posScreen[7].x, (double) this.m_vm_posScreen[7].y, (double) this.m_vm_posScreen[8].x, (double) this.m_vm_posScreen[8].y, (double) this.m_vm_posScreen[3].x, (double) this.m_vm_posScreen[3].y);
        MakeReferenceLine();
    }

    public void makeVmposScreen() {
        for (int i = 0; i < 9; i++) {
            this.m_vm_posScreen[i] = new Point((int) this.m_posScreen[i].x, (int) this.m_posScreen[i].y);
        }
        int y1 = (int) this.m_posScreen[0].y;
        int y2 = (int) (this.m_posScreen[2].y - this.m_posScreen[0].y);
        int x1 = (int) this.m_posScreen[0].x;
        int x2 = (int) (this.m_posScreen[6].x - this.m_posScreen[0].x);
        this.m_vm_posScreen[1].y = (int) (((double) y1) + ((((double) y2) * GetDistPosition(this.m_posPhysicalPen[0], this.m_posPhysicalPen[1])) / GetDistPosition(this.m_posPhysicalPen[0], this.m_posPhysicalPen[2])));
        this.m_vm_posScreen[4].y = (int) (((double) y1) + ((((double) y2) * GetDistPosition(this.m_posPhysicalPen[5], this.m_posPhysicalPen[4])) / GetDistPosition(this.m_posPhysicalPen[5], this.m_posPhysicalPen[3])));
        this.m_vm_posScreen[7].y = (int) (((double) y1) + ((((double) y2) * GetDistPosition(this.m_posPhysicalPen[6], this.m_posPhysicalPen[7])) / GetDistPosition(this.m_posPhysicalPen[6], this.m_posPhysicalPen[8])));
        this.m_vm_posScreen[5].x = (int) (((double) x1) + ((((double) x2) * GetDistPosition(this.m_posPhysicalPen[0], this.m_posPhysicalPen[5])) / GetDistPosition(this.m_posPhysicalPen[0], this.m_posPhysicalPen[6])));
        this.m_vm_posScreen[4].x = (int) (((double) x1) + ((((double) x2) * GetDistPosition(this.m_posPhysicalPen[1], this.m_posPhysicalPen[4])) / GetDistPosition(this.m_posPhysicalPen[1], this.m_posPhysicalPen[7])));
        this.m_vm_posScreen[3].x = (int) (((double) x1) + ((((double) x2) * GetDistPosition(this.m_posPhysicalPen[2], this.m_posPhysicalPen[3])) / GetDistPosition(this.m_posPhysicalPen[2], this.m_posPhysicalPen[8])));
    }

    private double GetDistPosition(PointF startpt, PointF endpt) {
        return Math.sqrt(Math.pow((double) (endpt.x - startpt.x), 2.0d) + Math.pow((double) (endpt.y - startpt.y), 2.0d));
    }

    public void MakeReferenceLine() {
        this.scope[0] = (((double) this.m_posPhysicalPen[4].y) - ((double) this.m_posPhysicalPen[1].y)) / (((double) this.m_posPhysicalPen[4].x) - ((double) this.m_posPhysicalPen[1].x));
        this.Intercept[0] = ((double) this.m_posPhysicalPen[1].y) - (((double) this.m_posPhysicalPen[1].x) * this.scope[0]);
        this.scope[1] = (((double) this.m_posPhysicalPen[7].y) - ((double) this.m_posPhysicalPen[4].y)) / (((double) this.m_posPhysicalPen[7].x) - ((double) this.m_posPhysicalPen[4].x));
        this.Intercept[1] = ((double) this.m_posPhysicalPen[4].y) - (((double) this.m_posPhysicalPen[4].x) * this.scope[1]);
        this.scope[2] = (((double) this.m_posPhysicalPen[4].y) - ((double) this.m_posPhysicalPen[5].y)) / (((double) this.m_posPhysicalPen[4].x) - ((double) this.m_posPhysicalPen[5].x));
        this.Intercept[2] = ((double) this.m_posPhysicalPen[5].y) - (((double) this.m_posPhysicalPen[5].x) * this.scope[2]);
        this.scope[3] = (((double) this.m_posPhysicalPen[3].y) - ((double) this.m_posPhysicalPen[4].y)) / (((double) this.m_posPhysicalPen[3].x) - ((double) this.m_posPhysicalPen[4].x));
        this.Intercept[3] = ((double) this.m_posPhysicalPen[4].y) - (((double) this.m_posPhysicalPen[4].x) * this.scope[3]);
    }

    boolean existCalibrationInfo() {
        return this.mContext.getSharedPreferences("PNFCalibData9Cal", 0).getBoolean("isFile", false);
    }

    public void setCalibrationData(PointF[] _calScreen, int _margin, PointF[] _calPen) {
        int marginX = 0;
        int marginY = 0;
        this.margin = _margin;
        int i = 0;
        while (i < this.m_posPhysicalPen.length) {
            if (_calPen != null) {
                this.m_posPhysicalPen[i].set(_calPen[i].x, _calPen[i].y);
            }
            if (_calScreen != null) {
                if (i == 0 || i == 1 || i == 2) {
                    marginX = this.margin;
                } else if (i == 6 || i == 7 || i == 8) {
                    marginX = -this.margin;
                }
                if (i == 0 || i == 4 || i == 5) {
                    marginY = this.margin;
                } else if (i == 2 || i == 3 || i == 8) {
                    marginY = -this.margin;
                }
                this.m_posScreen[i].set(_calScreen[i].x + ((float) marginX), _calScreen[i].y + ((float) marginY));
            }
            i++;
        }
        setPTFQuadToQuad();
        saveCaliData(_calScreen, this.margin, _calPen);
    }

    void loadCaliData(int lcdWid, int lcdHei, int _modeCode) {
        if (this.isExistCaliData) {
            SharedPreferences pref = this.mContext.getSharedPreferences("PNFCalibData9Cal", 0);
            this.margin = pref.getInt("ScreenMargin", 0);
            for (int i = 0; i < 9; i++) {
                this.m_posPhysicalPen[i].x = pref.getFloat("m_posPhysicalPen" + i + "x", 0.0f);
                this.m_posPhysicalPen[i].y = pref.getFloat("m_posPhysicalPen" + i + "y", 0.0f);
                this.m_posScreen[i].x = pref.getFloat("m_posScreen" + i + "x", 0.0f);
                this.m_posScreen[i].y = pref.getFloat("m_posScreen" + i + "y", 0.0f);
            }
            setCalibrationData(this.m_posScreen, 0, this.m_posPhysicalPen);
            return;
        }
        calPoint = new PointF[9];
        PointF[] calResultPoint = new PointF[]{new PointF(0.0f, 0.0f), new PointF(0.0f, (float) (lcdHei / 2)), new PointF(0.0f, (float) lcdHei), new PointF((float) (lcdWid / 2), (float) lcdHei), new PointF((float) (lcdWid / 2), (float) (lcdHei / 2)), new PointF((float) (lcdWid / 2), 0.0f), new PointF((float) lcdWid, 0.0f), new PointF((float) lcdWid, (float) (lcdHei / 2)), new PointF((float) lcdWid, (float) lcdHei)};
        calResultPoint[0] = new PointF(670.0f, 325.0f);
        calResultPoint[1] = new PointF(629.0f, 1588.0f);
        calResultPoint[2] = new PointF(630.0f, 2871.0f);
        calResultPoint[3] = new PointF(2947.0f, 2898.0f);
        calResultPoint[4] = new PointF(2931.0f, 1620.0f);
        calResultPoint[5] = new PointF(2947.0f, 332.0f);
        calResultPoint[6] = new PointF(5230.0f, 400.0f);
        calResultPoint[7] = new PointF(5216.0f, 1660.0f);
        calResultPoint[8] = new PointF(5241.0f, 2928.0f);
        setCalibrationData(calPoint, 0, calResultPoint);
    }

    void saveCaliData() {
        saveCaliData(this.m_posScreen, this.margin, this.m_posPhysicalPen);
    }

    void saveCaliData(PointF[] calScreen, int margin, PointF[] calPen) {
        this.isExistCaliData = true;
        Editor ePref = this.mContext.getSharedPreferences("PNFCalibData9Cal", 0).edit();
        ePref.putBoolean("isFile", this.isExistCaliData);
        ePref.putInt("ScreenMargin", margin);
        for (int i = 0; i < 9; i++) {
            ePref.putFloat("m_posPhysicalPen" + i + "x", this.m_posPhysicalPen[i].x);
            ePref.putFloat("m_posPhysicalPen" + i + "y", this.m_posPhysicalPen[i].y);
            ePref.putFloat("m_posScreen" + i + "x", this.m_posScreen[i].x);
            ePref.putFloat("m_posScreen" + i + "y", this.m_posScreen[i].y);
        }
        ePref.commit();
    }
}
