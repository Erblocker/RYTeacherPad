package io.vov.vitamio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

public class ThumbnailUtils {
    private static final int OPTIONS_NONE = 0;
    public static final int OPTIONS_RECYCLE_INPUT = 2;
    private static final int OPTIONS_SCALE_UP = 1;
    public static final int TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT = 160;
    public static final int TARGET_SIZE_MICRO_THUMBNAIL_WIDTH = 212;
    public static final int TARGET_SIZE_MINI_THUMBNAIL_HEIGHT = 320;
    public static final int TARGET_SIZE_MINI_THUMBNAIL_WIDTH = 426;

    public static Bitmap createVideoThumbnail(Context ctx, String filePath, int kind) {
        Throwable th;
        if (!Vitamio.isInitialized(ctx)) {
            return null;
        }
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = null;
        try {
            MediaMetadataRetriever retriever2 = new MediaMetadataRetriever(ctx);
            try {
                retriever2.setDataSource(filePath);
                bitmap = retriever2.getFrameAtTime(-1);
                try {
                    retriever2.release();
                    retriever = retriever2;
                } catch (RuntimeException e) {
                    retriever = retriever2;
                }
            } catch (Exception e2) {
                retriever = retriever2;
                try {
                    retriever.release();
                } catch (RuntimeException e3) {
                }
                if (bitmap != null) {
                    return bitmap;
                }
                if (kind != 3) {
                    return extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL_WIDTH, TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT, 2);
                }
                if (kind == 1) {
                    return extractThumbnail(bitmap, TARGET_SIZE_MINI_THUMBNAIL_WIDTH, TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, 2);
                }
                return bitmap;
            } catch (Throwable th2) {
                th = th2;
                retriever = retriever2;
                try {
                    retriever.release();
                } catch (RuntimeException e4) {
                }
                throw th;
            }
        } catch (Exception e5) {
            retriever.release();
            if (bitmap != null) {
                return bitmap;
            }
            if (kind != 3) {
                return extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL_WIDTH, TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT, 2);
            }
            if (kind == 1) {
                return bitmap;
            }
            return extractThumbnail(bitmap, TARGET_SIZE_MINI_THUMBNAIL_WIDTH, TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, 2);
        } catch (Throwable th3) {
            th = th3;
            retriever.release();
            throw th;
        }
        if (bitmap != null) {
            return bitmap;
        }
        if (kind != 3) {
            return extractThumbnail(bitmap, TARGET_SIZE_MICRO_THUMBNAIL_WIDTH, TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT, 2);
        }
        if (kind == 1) {
            return extractThumbnail(bitmap, TARGET_SIZE_MINI_THUMBNAIL_WIDTH, TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, 2);
        }
        return bitmap;
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
        return extractThumbnail(source, width, height, 0);
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height, int options) {
        if (source == null) {
            return null;
        }
        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = ((float) width) / ((float) source.getWidth());
        } else {
            scale = ((float) height) / ((float) source.getHeight());
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return transform(matrix, source, width, height, options | 1);
    }

    private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, int options) {
        Bitmap b2;
        boolean scaleUp = (options & 1) != 0;
        boolean recycle = (options & 2) != 0;
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (scaleUp || (deltaX >= 0 && deltaY >= 0)) {
            Bitmap b1;
            float bitmapWidthF = (float) source.getWidth();
            float bitmapHeightF = (float) source.getHeight();
            float scale = bitmapWidthF / bitmapHeightF > ((float) targetWidth) / ((float) targetHeight) ? ((float) targetHeight) / bitmapHeightF : ((float) targetWidth) / bitmapWidthF;
            if (scale < 0.9f || scale > 1.0f) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
            if (scaler != null) {
                b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
            } else {
                b1 = source;
            }
            if (recycle && b1 != source) {
                source.recycle();
            }
            b2 = Bitmap.createBitmap(b1, Math.max(0, b1.getWidth() - targetWidth) / 2, Math.max(0, b1.getHeight() - targetHeight) / 2, targetWidth, targetHeight);
            if (b2 != b1 && (recycle || b1 != source)) {
                b1.recycle();
            }
        } else {
            b2 = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
            Canvas c = new Canvas(b2);
            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            Rect rect = new Rect(deltaXHalf, deltaYHalf, Math.min(targetWidth, source.getWidth()) + deltaXHalf, Math.min(targetHeight, source.getHeight()) + deltaYHalf);
            int dstX = (targetWidth - rect.width()) / 2;
            int dstY = (targetHeight - rect.height()) / 2;
            c.drawBitmap(source, rect, new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY), null);
            if (recycle) {
                source.recycle();
            }
        }
        return b2;
    }
}
