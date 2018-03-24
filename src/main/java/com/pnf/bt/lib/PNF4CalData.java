package com.pnf.bt.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PointF;
import com.pnf.bt.lib.transform.PerspectiveTransform;

public class PNF4CalData {
    final String PNF4CalSharedPre = "PNFCalibData4Cal";
    boolean isExistCaliData = false;
    boolean isLetterPaper = false;
    Context mContext;
    PerspectiveTransform mPerspectiveTransform;
    PointF[] m_posPhysicalPen = null;
    PointF[] m_posScreen = null;
    int margin = 0;

    public PNF4CalData(Context con) {
        this.mContext = con;
        this.isExistCaliData = existCalibrationInfo();
        this.m_posPhysicalPen = new PointF[4];
        this.m_posScreen = new PointF[4];
        for (int i = 0; i < 4; i++) {
            this.m_posPhysicalPen[i] = new PointF();
            this.m_posScreen[i] = new PointF();
        }
    }

    PointF getPNFPerspective(float x, float y) {
        return getPNFPerspectivePoint(x, y);
    }

    PointF getPNFPerspective(float x, float y, int StationPosition, boolean bRight) {
        return getPNFPerspectivePoint(x, y, StationPosition, bRight);
    }

    PointF getPNFPerspective(float x, float y, boolean bDeviceReverse) {
        return getPNFPerspectivePoint(x, y, bDeviceReverse);
    }

    PointF getPNFPerspectivePoint(float x, float y) {
        PointF srcPoint = new PointF(x, y);
        PointF dstPoint = new PointF(0.0f, 0.0f);
        this.mPerspectiveTransform.transform(srcPoint, dstPoint);
        return dstPoint;
    }

    PointF getPNFPerspectivePoint(float x, float y, int StationPosition, boolean bRight) {
        PointF srcPoint = new PointF(x, y);
        PointF dstPoint = new PointF(0.0f, 0.0f);
        this.mPerspectiveTransform.transform(srcPoint, dstPoint);
        if (!bRight) {
            switch (StationPosition) {
                case 1:
                case 4:
                    dstPoint.y = (this.m_posScreen[2].y - this.m_posScreen[1].y) - dstPoint.y;
                    break;
                case 2:
                case 3:
                    dstPoint.x = (this.m_posScreen[1].x - this.m_posScreen[0].x) - dstPoint.x;
                    break;
                default:
                    dstPoint.x = (this.m_posScreen[1].x - this.m_posScreen[0].x) - dstPoint.x;
                    break;
            }
        }
        return dstPoint;
    }

    PointF getPNFPerspectivePoint(float x, float y, boolean bDeviceReverse) {
        PointF srcPoint = new PointF(x, y);
        PointF dstPoint = new PointF(0.0f, 0.0f);
        this.mPerspectiveTransform.transform(srcPoint, dstPoint);
        if (bDeviceReverse) {
            dstPoint.x = (this.m_posScreen[1].x - this.m_posScreen[0].x) - dstPoint.x;
            dstPoint.y = (this.m_posScreen[2].y - this.m_posScreen[1].y) - dstPoint.y;
        }
        return dstPoint;
    }

    void setCalibrationTurnLeft90Angle() {
        PointF TransPoint = new PointF(0.0f, 0.0f);
        TransPoint.x = this.m_posScreen[0].x;
        TransPoint.y = this.m_posScreen[0].y;
        this.m_posScreen[0].x = this.m_posScreen[1].x;
        this.m_posScreen[0].y = this.m_posScreen[1].y;
        this.m_posScreen[1].x = this.m_posScreen[2].x;
        this.m_posScreen[1].y = this.m_posScreen[2].y;
        this.m_posScreen[2].x = this.m_posScreen[3].x;
        this.m_posScreen[2].y = this.m_posScreen[3].y;
        this.m_posScreen[3].x = TransPoint.x;
        this.m_posScreen[3].y = TransPoint.y;
        setCalibrationData(this.m_posScreen, this.margin, this.m_posPhysicalPen);
    }

    void setCalibrationTurnRight90Angle() {
        PointF TransPoint = new PointF(0.0f, 0.0f);
        TransPoint.x = this.m_posScreen[0].x;
        TransPoint.y = this.m_posScreen[0].y;
        this.m_posScreen[0].x = this.m_posScreen[3].x;
        this.m_posScreen[0].y = this.m_posScreen[3].y;
        this.m_posScreen[3].x = this.m_posScreen[2].x;
        this.m_posScreen[3].y = this.m_posScreen[2].y;
        this.m_posScreen[2].x = this.m_posScreen[1].x;
        this.m_posScreen[2].y = this.m_posScreen[1].y;
        this.m_posScreen[1].x = TransPoint.x;
        this.m_posScreen[1].y = TransPoint.y;
        setCalibrationData(this.m_posScreen, this.margin, this.m_posPhysicalPen);
    }

    void setPTFQuadToQuad() {
        setPTFQuadToQuadPoint();
    }

    void setPTFQuadToQuadPoint() {
        this.mPerspectiveTransform = PerspectiveTransform.getQuadToQuad((double) this.m_posPhysicalPen[0].x, (double) this.m_posPhysicalPen[0].y, (double) this.m_posPhysicalPen[1].x, (double) this.m_posPhysicalPen[1].y, (double) this.m_posPhysicalPen[3].x, (double) this.m_posPhysicalPen[3].y, (double) this.m_posPhysicalPen[2].x, (double) this.m_posPhysicalPen[2].y, (double) this.m_posScreen[0].x, (double) this.m_posScreen[0].y, (double) this.m_posScreen[1].x, (double) this.m_posScreen[1].y, (double) this.m_posScreen[3].x, (double) this.m_posScreen[3].y, (double) this.m_posScreen[2].x, (double) this.m_posScreen[2].y);
    }

    double GetDistPosition(PointF startpt, PointF endpt) {
        return Math.sqrt(Math.pow((double) (endpt.x - startpt.x), 2.0d) + Math.pow((double) (endpt.y - startpt.y), 2.0d));
    }

    boolean existCalibrationInfo() {
        return this.mContext.getSharedPreferences("PNFCalibData4Cal", 0).getBoolean("isFile", false);
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
                if (i == 0 || i == 3) {
                    marginX = this.margin;
                } else if (i == 1 || i == 2) {
                    marginX = -this.margin;
                }
                if (i == 0 || i == 1) {
                    marginY = this.margin;
                } else if (i == 2 || i == 3) {
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
            SharedPreferences pref = this.mContext.getSharedPreferences("PNFCalibData4Cal", 0);
            this.margin = pref.getInt("ScreenMargin", 0);
            for (int i = 0; i < 4; i++) {
                this.m_posPhysicalPen[i].x = pref.getFloat("m_posPhysicalPen" + i + "x", 0.0f);
                this.m_posPhysicalPen[i].y = pref.getFloat("m_posPhysicalPen" + i + "y", 0.0f);
                this.m_posScreen[i].x = pref.getFloat("m_posScreen" + i + "x", 0.0f);
                this.m_posScreen[i].y = pref.getFloat("m_posScreen" + i + "y", 0.0f);
            }
            double Transform_m00 = (double) pref.getFloat("PerspectiveTransform_m00", 0.0f);
            double Transform_m01 = (double) pref.getFloat("PerspectiveTransform_m01", 0.0f);
            double Transform_m02 = (double) pref.getFloat("PerspectiveTransform_m02", 0.0f);
            double Transform_m10 = (double) pref.getFloat("PerspectiveTransform_m10", 0.0f);
            double Transform_m11 = (double) pref.getFloat("PerspectiveTransform_m11", 0.0f);
            double Transform_m12 = (double) pref.getFloat("PerspectiveTransform_m12", 0.0f);
            double Transform_m20 = (double) pref.getFloat("PerspectiveTransform_m20", 0.0f);
            double Transform_m21 = (double) pref.getFloat("PerspectiveTransform_m21", 0.0f);
            double Transform_m22 = (double) pref.getFloat("PerspectiveTransform_m22", 0.0f);
            setCalibrationData(this.m_posScreen, 0, this.m_posPhysicalPen);
            matrix = new double[3][];
            matrix[0] = new double[]{Transform_m00, Transform_m01, Transform_m02};
            matrix[1] = new double[]{Transform_m10, Transform_m11, Transform_m12};
            matrix[2] = new double[]{Transform_m20, Transform_m21, Transform_m22};
            this.mPerspectiveTransform.setTransform(matrix);
            return;
        }
        if (lcdWid < 300 || lcdHei < 300) {
            lcdWid = 800;
            lcdHei = 1280;
        }
        calPoint = new PointF[4];
        PointF[] calResultPoint = new PointF[]{new PointF(0.0f, 0.0f), new PointF((float) lcdWid, 0.0f), new PointF((float) lcdWid, (float) lcdHei), new PointF(0.0f, (float) lcdHei)};
        switch (_modeCode) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (!this.isLetterPaper) {
                    calResultPoint[0] = new PointF(1768.0f, 563.0f);
                    calResultPoint[1] = new PointF(5392.0f, 563.0f);
                    calResultPoint[2] = new PointF(5392.0f, 5160.0f);
                    calResultPoint[3] = new PointF(1768.0f, 5160.0f);
                    break;
                }
                calResultPoint[0] = new PointF(1737.0f, 541.0f);
                calResultPoint[1] = new PointF(5445.0f, 541.0f);
                calResultPoint[2] = new PointF(5445.0f, 4818.0f);
                calResultPoint[3] = new PointF(1737.0f, 4818.0f);
                break;
            default:
                calResultPoint[0] = new PointF(1728.0f, 45372.0f);
                calResultPoint[1] = new PointF(15524.0f, 45372.0f);
                calResultPoint[2] = new PointF(15524.0f, 54824.0f);
                calResultPoint[3] = new PointF(1728.0f, 54824.0f);
                break;
        }
        setCalibrationData(calPoint, 0, calResultPoint);
        saveCaliData(calPoint, 0, calResultPoint);
    }

    void saveCaliData() {
        saveCaliData(this.m_posScreen, this.margin, this.m_posPhysicalPen);
    }

    void saveCaliData(PointF[] calScreen, int margin, PointF[] calPen) {
        this.isExistCaliData = true;
        Editor ePref = this.mContext.getSharedPreferences("PNFCalibData4Cal", 0).edit();
        ePref.putBoolean("isFile", this.isExistCaliData);
        ePref.putInt("ScreenMargin", margin);
        for (int i = 0; i < 4; i++) {
            ePref.putFloat("m_posPhysicalPen" + i + "x", this.m_posPhysicalPen[i].x);
            ePref.putFloat("m_posPhysicalPen" + i + "y", this.m_posPhysicalPen[i].y);
            ePref.putFloat("m_posScreen" + i + "x", this.m_posScreen[i].x);
            ePref.putFloat("m_posScreen" + i + "y", this.m_posScreen[i].y);
        }
        ePref.putFloat("PerspectiveTransform_m00", (float) this.mPerspectiveTransform.m00);
        ePref.putFloat("PerspectiveTransform_m01", (float) this.mPerspectiveTransform.m01);
        ePref.putFloat("PerspectiveTransform_m02", (float) this.mPerspectiveTransform.m02);
        ePref.putFloat("PerspectiveTransform_m10", (float) this.mPerspectiveTransform.m10);
        ePref.putFloat("PerspectiveTransform_m11", (float) this.mPerspectiveTransform.m11);
        ePref.putFloat("PerspectiveTransform_m12", (float) this.mPerspectiveTransform.m12);
        ePref.putFloat("PerspectiveTransform_m20", (float) this.mPerspectiveTransform.m20);
        ePref.putFloat("PerspectiveTransform_m21", (float) this.mPerspectiveTransform.m21);
        ePref.putFloat("PerspectiveTransform_m22", (float) this.mPerspectiveTransform.m22);
        ePref.commit();
    }
}
