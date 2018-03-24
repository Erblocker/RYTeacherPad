package com.foxit.uiextensions.annots.ink;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;
import com.foxit.sdk.pdf.annots.Ink;
import java.util.ArrayList;

class InkAnnotUtil {
    InkAnnotUtil() {
    }

    public long getSupportedProperties() {
        return 7;
    }

    protected ArrayList<ArrayList<PointF>> docLinesFromPageView(PDFViewCtrl pdfViewCtrl, int pageIndex, ArrayList<ArrayList<PointF>> lines, RectF bbox) {
        RectF bboxF = null;
        ArrayList<ArrayList<PointF>> docLines = new ArrayList();
        for (int i = 0; i < lines.size(); i++) {
            ArrayList<PointF> newLine = new ArrayList();
            for (int j = 0; j < ((ArrayList) lines.get(i)).size(); j++) {
                PointF curPoint = new PointF();
                curPoint.set((PointF) ((ArrayList) lines.get(i)).get(j));
                if (bboxF == null) {
                    bboxF = new RectF(curPoint.x, curPoint.y, curPoint.x, curPoint.y);
                } else {
                    bboxF.union(curPoint.x, curPoint.y);
                }
                pdfViewCtrl.convertPageViewPtToPdfPt(curPoint, curPoint, pageIndex);
                newLine.add(curPoint);
            }
            docLines.add(newLine);
        }
        if (bboxF != null) {
            pdfViewCtrl.convertPageViewRectToPdfRect(bboxF, bboxF, pageIndex);
            bbox.set(bboxF.left, bboxF.top, bboxF.right, bboxF.bottom);
        }
        return docLines;
    }

    protected static ArrayList<Path> generatePathData(PDFViewCtrl pdfViewCtrl, int pageIndex, Ink annot) {
        return generateInkPaths(pdfViewCtrl, pageIndex, annot);
    }

    protected static ArrayList<Path> generateInkPaths(PDFViewCtrl pdfViewCtrl, int pageIndex, Ink annot) {
        try {
            PDFPath pdfPath = annot.getInkList();
            if (pdfPath == null) {
                return null;
            }
            ArrayList<Path> paths = new ArrayList();
            PointF pointF = new PointF();
            int ptCount = pdfPath.getPointCount();
            Path path;
            if (ptCount == 1) {
                path = new Path();
                pointF.set(pdfPath.getPoint(0).x, pdfPath.getPoint(0).y);
                pdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                path.moveTo(pointF.x, pointF.y);
                path.lineTo(pointF.x + 0.1f, pointF.y + 0.1f);
                paths.add(path);
                return paths;
            }
            float cx = 0.0f;
            float cy = 0.0f;
            path = null;
            int i = 0;
            while (i < ptCount) {
                pointF.set(pdfPath.getPoint(i).x, pdfPath.getPoint(i).y);
                pdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                if (pdfPath.getPointType(i) == 1) {
                    path = new Path();
                    path.moveTo(pointF.x, pointF.y);
                    cx = pointF.x;
                    cy = pointF.y;
                } else {
                    path.quadTo(cx, cy, (pointF.x + cx) / 2.0f, (pointF.y + cy) / 2.0f);
                    cx = pointF.x;
                    cy = pointF.y;
                    if (i == ptCount - 1 || (i + 1 < ptCount && pdfPath.getPointType(i + 1) == 1)) {
                        path.lineTo(pointF.x, pointF.y);
                        paths.add(path);
                    }
                }
                i++;
            }
            return paths;
        } catch (PDFException e) {
            return null;
        }
    }

    public static void correctPvPoint(PDFViewCtrl pdfViewCtrl, int pageIndex, PointF pt) {
        pt.x = Math.max(0.0f, pt.x);
        pt.y = Math.max(0.0f, pt.y);
        pt.x = Math.min((float) pdfViewCtrl.getPageViewWidth(pageIndex), pt.x);
        pt.y = Math.min((float) pdfViewCtrl.getPageViewHeight(pageIndex), pt.y);
    }

    public static ArrayList<ArrayList<PointF>> cloneInkList(ArrayList<ArrayList<PointF>> lines) {
        if (lines == null) {
            return null;
        }
        ArrayList<ArrayList<PointF>> newLines = new ArrayList();
        for (int i = 0; i < lines.size(); i++) {
            ArrayList<PointF> line = (ArrayList) lines.get(i);
            ArrayList<PointF> newLine = new ArrayList();
            for (int j = 0; j < line.size(); j++) {
                newLine.add(new PointF(((PointF) line.get(j)).x, ((PointF) line.get(j)).y));
            }
            newLines.add(newLine);
        }
        return newLines;
    }

    public static ArrayList<ArrayList<PointF>> generateInkList(PDFPath pdfPath) {
        PDFException e;
        if (pdfPath == null) {
            return null;
        }
        ArrayList<ArrayList<PointF>> newLines = new ArrayList();
        try {
            ArrayList<PointF> newLine;
            int ptCount = pdfPath.getPointCount();
            int i = 0;
            ArrayList<PointF> newLine2 = null;
            while (i < ptCount) {
                try {
                    if (pdfPath.getPointType(i) == 1) {
                        newLine = new ArrayList();
                    } else {
                        newLine = newLine2;
                    }
                    newLine.add(pdfPath.getPoint(i));
                    if (i == ptCount - 1 || (i + 1 < ptCount && pdfPath.getPointType(i + 1) == 1)) {
                        newLines.add(newLine);
                    }
                    i++;
                    newLine2 = newLine;
                } catch (PDFException e2) {
                    e = e2;
                    newLine = newLine2;
                }
            }
            newLine = newLine2;
            return newLines;
        } catch (PDFException e3) {
            e = e3;
        }
        e.printStackTrace();
        return newLines;
    }
}
