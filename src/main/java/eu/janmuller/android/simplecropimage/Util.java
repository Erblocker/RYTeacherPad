package eu.janmuller.android.simplecropimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import eu.janmuller.android.simplecropimage.MonitoredActivity.LifeCycleAdapter;
import java.io.Closeable;

public class Util {
    private static final String TAG = "db.Util";

    private static class BackgroundJob extends LifeCycleAdapter implements Runnable {
        private final MonitoredActivity mActivity;
        private final Runnable mCleanupRunner = new Runnable() {
            public void run() {
                BackgroundJob.this.mActivity.removeLifeCycleListener(BackgroundJob.this);
                if (BackgroundJob.this.mDialog.getWindow() != null) {
                    BackgroundJob.this.mDialog.dismiss();
                }
            }
        };
        private final ProgressDialog mDialog;
        private final Handler mHandler;
        private final Runnable mJob;

        public BackgroundJob(MonitoredActivity activity, Runnable job, ProgressDialog dialog, Handler handler) {
            this.mActivity = activity;
            this.mDialog = dialog;
            this.mJob = job;
            this.mActivity.addLifeCycleListener(this);
            this.mHandler = handler;
        }

        public void run() {
            try {
                this.mJob.run();
            } finally {
                this.mHandler.post(this.mCleanupRunner);
            }
        }

        public void onActivityDestroyed(MonitoredActivity activity) {
            this.mCleanupRunner.run();
            this.mHandler.removeCallbacks(this.mCleanupRunner);
        }

        public void onActivityStopped(MonitoredActivity activity) {
            this.mDialog.hide();
        }

        public void onActivityStarted(MonitoredActivity activity) {
            this.mDialog.show();
        }
    }

    private Util() {
    }

    public static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, boolean scaleUp) {
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        Bitmap b2;
        if (scaleUp || (deltaX >= 0 && deltaY >= 0)) {
            Bitmap b1;
            float bitmapWidthF = (float) source.getWidth();
            float bitmapHeightF = (float) source.getHeight();
            float scale;
            if (bitmapWidthF / bitmapHeightF > ((float) targetWidth) / ((float) targetHeight)) {
                scale = ((float) targetHeight) / bitmapHeightF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler.setScale(scale, scale);
                } else {
                    scaler = null;
                }
            } else {
                scale = ((float) targetWidth) / bitmapWidthF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler.setScale(scale, scale);
                } else {
                    scaler = null;
                }
            }
            if (scaler != null) {
                b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
            } else {
                b1 = source;
            }
            b2 = Bitmap.createBitmap(b1, Math.max(0, b1.getWidth() - targetWidth) / 2, Math.max(0, b1.getHeight() - targetHeight) / 2, targetWidth, targetHeight);
            if (b1 == source) {
                return b2;
            }
            b1.recycle();
            return b2;
        }
        b2 = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
        Canvas c = new Canvas(b2);
        int deltaXHalf = Math.max(0, deltaX / 2);
        int deltaYHalf = Math.max(0, deltaY / 2);
        int i = deltaXHalf;
        int i2 = deltaYHalf;
        Rect rect = new Rect(i, i2, Math.min(targetWidth, source.getWidth()) + deltaXHalf, Math.min(targetHeight, source.getHeight()) + deltaYHalf);
        int dstX = (targetWidth - rect.width()) / 2;
        int dstY = (targetHeight - rect.height()) / 2;
        c.drawBitmap(source, rect, new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY), null);
        return b2;
    }

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable th) {
            }
        }
    }

    public static void startBackgroundJob(MonitoredActivity activity, String title, String message, Runnable job, Handler handler) {
        new Thread(new BackgroundJob(activity, job, ProgressDialog.show(activity, title, message, true, false), handler)).start();
    }

    public static Options createNativeAllocOptions() {
        return new Options();
    }

    public static Bitmap rotateImage(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static int getOrientationInDegree(Activity activity) {
        switch (activity.getWindowManager().getDefaultDisplay().getOrientation()) {
            case 0:
                return 0;
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
            default:
                return 0;
        }
    }
}
