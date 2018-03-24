package eu.janmuller.android.simplecropimage;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import eu.janmuller.android.simplecropimage.BitmapManager.ThreadSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class CropImage extends MonitoredActivity {
    public static final String ACTION_INLINE_DATA = "inline-data";
    public static final String ASPECT_X = "aspectX";
    public static final String ASPECT_Y = "aspectY";
    public static final int CANNOT_STAT_ERROR = -2;
    public static final String CIRCLE_CROP = "circleCrop";
    public static final String IMAGE_PATH = "image-path";
    public static final int NO_STORAGE_ERROR = -1;
    public static final String ORIENTATION_IN_DEGREES = "orientation_in_degrees";
    public static final String OUTPUT_X = "outputX";
    public static final String OUTPUT_Y = "outputY";
    public static final String RETURN_DATA = "return-data";
    public static final String RETURN_DATA_AS_BITMAP = "data";
    public static final String SCALE = "scale";
    public static final String SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    private static final String TAG = "CropImage";
    final int IMAGE_MAX_SIZE = 1024;
    private int mAspectX;
    private int mAspectY;
    private Bitmap mBitmap;
    private boolean mCircleCrop = false;
    private ContentResolver mContentResolver;
    HighlightView mCrop;
    private final ThreadSet mDecodingThreads = new ThreadSet();
    private boolean mDoFaceDetection = false;
    private final Handler mHandler = new Handler();
    private Button mImageEnhanceButton;
    private String mImagePath;
    private CropImageView mImageView;
    private CompressFormat mOutputFormat = CompressFormat.JPEG;
    private int mOutputX;
    private int mOutputY;
    Runnable mRunFaceDetection = new Runnable() {
        Face[] mFaces = new Face[3];
        Matrix mImageMatrix;
        int mNumFaces;
        float mScale = 1.0f;

        private void handleFace(Face f) {
            PointF midPoint = new PointF();
            int r = ((int) (f.eyesDistance() * this.mScale)) * 2;
            f.getMidPoint(midPoint);
            midPoint.x *= this.mScale;
            midPoint.y *= this.mScale;
            int midX = (int) midPoint.x;
            int midY = (int) midPoint.y;
            HighlightView hv = new HighlightView(CropImage.this.mImageView);
            Rect imageRect = new Rect(0, 0, CropImage.this.mBitmap.getWidth(), CropImage.this.mBitmap.getHeight());
            RectF faceRect = new RectF((float) midX, (float) midY, (float) midX, (float) midY);
            faceRect.inset((float) (-r), (float) (-r));
            if (faceRect.left < 0.0f) {
                faceRect.inset(-faceRect.left, -faceRect.left);
            }
            if (faceRect.top < 0.0f) {
                faceRect.inset(-faceRect.top, -faceRect.top);
            }
            if (faceRect.right > ((float) imageRect.right)) {
                faceRect.inset(faceRect.right - ((float) imageRect.right), faceRect.right - ((float) imageRect.right));
            }
            if (faceRect.bottom > ((float) imageRect.bottom)) {
                faceRect.inset(faceRect.bottom - ((float) imageRect.bottom), faceRect.bottom - ((float) imageRect.bottom));
            }
            Matrix matrix = this.mImageMatrix;
            boolean access$4 = CropImage.this.mCircleCrop;
            boolean z = (CropImage.this.mAspectX == 0 || CropImage.this.mAspectY == 0) ? false : true;
            hv.setup(matrix, imageRect, faceRect, access$4, z);
            CropImage.this.mImageView.add(hv);
        }

        private void makeDefault() {
            boolean z = false;
            HighlightView hv = new HighlightView(CropImage.this.mImageView);
            int width = CropImage.this.mBitmap.getWidth();
            int height = CropImage.this.mBitmap.getHeight();
            Rect imageRect = new Rect(0, 0, width, height);
            int cropWidth = (Math.min(width, height) * 4) / 5;
            int cropHeight = cropWidth;
            if (!(CropImage.this.mAspectX == 0 || CropImage.this.mAspectY == 0)) {
                if (CropImage.this.mAspectX > CropImage.this.mAspectY) {
                    cropHeight = (CropImage.this.mAspectY * cropWidth) / CropImage.this.mAspectX;
                } else {
                    cropWidth = (CropImage.this.mAspectX * cropHeight) / CropImage.this.mAspectY;
                }
            }
            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;
            RectF cropRect = new RectF((float) x, (float) y, (float) (x + cropWidth), (float) (y + cropHeight));
            Matrix matrix = this.mImageMatrix;
            boolean access$4 = CropImage.this.mCircleCrop;
            if (!(CropImage.this.mAspectX == 0 || CropImage.this.mAspectY == 0)) {
                z = true;
            }
            hv.setup(matrix, imageRect, cropRect, access$4, z);
            CropImage.this.mImageView.mHighlightViews.clear();
            CropImage.this.mImageView.add(hv);
        }

        private Bitmap prepareBitmap() {
            if (CropImage.this.mBitmap == null || CropImage.this.mBitmap.isRecycled()) {
                return null;
            }
            if (CropImage.this.mBitmap.getWidth() > 256) {
                this.mScale = 256.0f / ((float) CropImage.this.mBitmap.getWidth());
            }
            Matrix matrix = new Matrix();
            matrix.setScale(this.mScale, this.mScale);
            return Bitmap.createBitmap(CropImage.this.mBitmap, 0, 0, CropImage.this.mBitmap.getWidth(), CropImage.this.mBitmap.getHeight(), matrix, true);
        }

        public void run() {
            this.mImageMatrix = CropImage.this.mImageView.getImageMatrix();
            Bitmap faceBitmap = prepareBitmap();
            this.mScale = 1.0f / this.mScale;
            if (faceBitmap != null && CropImage.this.mDoFaceDetection) {
                this.mNumFaces = new FaceDetector(faceBitmap.getWidth(), faceBitmap.getHeight(), this.mFaces.length).findFaces(faceBitmap, this.mFaces);
            }
            if (!(faceBitmap == null || faceBitmap == CropImage.this.mBitmap)) {
                faceBitmap.recycle();
            }
            CropImage.this.mHandler.post(new Runnable() {
                public void run() {
                    CropImage.this.mWaitingToPick = AnonymousClass1.this.mNumFaces > 1;
                    if (AnonymousClass1.this.mNumFaces > 0) {
                        for (int i = 0; i < AnonymousClass1.this.mNumFaces; i++) {
                            AnonymousClass1.this.handleFace(AnonymousClass1.this.mFaces[i]);
                        }
                    } else {
                        AnonymousClass1.this.makeDefault();
                    }
                    CropImage.this.mImageView.invalidate();
                    if (CropImage.this.mImageView.mHighlightViews.size() == 1) {
                        CropImage.this.mCrop = (HighlightView) CropImage.this.mImageView.mHighlightViews.get(0);
                        CropImage.this.mCrop.setFocus(true);
                    }
                    if (AnonymousClass1.this.mNumFaces > 1) {
                        Toast.makeText(CropImage.this, "Multi face crop help", 0).show();
                    }
                }
            });
        }
    };
    private Uri mSaveUri = null;
    boolean mSaving;
    private boolean mScale;
    private boolean mScaleUp = true;
    boolean mWaitingToPick;

    public static class CropImageEvent {
        public String szImageFileName;
        public String szSourceEventType;
    }

    public static class CropImageResultEvent {
        public int nResult;
        public String szImageFileName;
        public String szSourceEventType;
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    public void onCreate(Bundle icicle) {
        requestWindowFeature(1);
        super.onCreate(icicle);
        this.mContentResolver = getContentResolver();
        setContentView(R.layout.cropimage);
        this.mImageView = (CropImageView) findViewById(R.id.image);
        showStorageToast(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString(CIRCLE_CROP) != null) {
                this.mCircleCrop = true;
                this.mAspectX = 1;
                this.mAspectY = 1;
            }
            this.mImagePath = extras.getString(IMAGE_PATH);
            this.mSaveUri = getImageUri(this.mImagePath);
            this.mBitmap = getBitmap(this.mImagePath);
            if (extras.containsKey(ASPECT_X) && (extras.get(ASPECT_X) instanceof Integer)) {
                this.mAspectX = extras.getInt(ASPECT_X);
                if (extras.containsKey(ASPECT_Y) && (extras.get(ASPECT_Y) instanceof Integer)) {
                    this.mAspectY = extras.getInt(ASPECT_Y);
                    this.mOutputX = extras.getInt(OUTPUT_X);
                    this.mOutputY = extras.getInt(OUTPUT_Y);
                    this.mScale = extras.getBoolean(SCALE, true);
                    this.mScaleUp = extras.getBoolean(SCALE_UP_IF_NEEDED, true);
                } else {
                    throw new IllegalArgumentException("aspect_y must be integer");
                }
            }
            throw new IllegalArgumentException("aspect_x must be integer");
        }
        if (this.mBitmap == null) {
            Log.d(TAG, "finish!!!");
            finish();
            return;
        }
        getWindow().addFlags(1024);
        this.mImageEnhanceButton = (Button) findViewById(R.id.imageEnhance);
        if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_IMAGE_ENHANCE)) {
            this.mImageEnhanceButton.setVisibility(0);
            this.mImageEnhanceButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("ProcessJSFunction", CropImage.this);
                    ItemObject.setSuccessListener(new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            String szFinalData = (String) ItemObject.getParam("2");
                            if (szFinalData == null || szFinalData.isEmpty()) {
                                Utilities.showAlertMessage(CropImage.this, "增强图像时出现错误", "服务器没有返回任何数据。");
                                return;
                            }
                            CropImage.this.mBitmap = Utilities.getBase64Bitmap(szFinalData.trim());
                            CropImage.this.mImageView.setImageBitmapResetBase(CropImage.this.mBitmap, true);
                        }
                    });
                    ItemObject.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                            Utilities.showAlertMessage(CropImage.this, "增强图像时出现错误", ItemObject.getErrorText());
                        }
                    });
                    ArrayList<String> arrInputParamName = new ArrayList();
                    ArrayList<String> arrInputParamValue = new ArrayList();
                    arrInputParamName.add("picturebase64");
                    arrInputParamValue.add(Utilities.saveBitmapToBase64String(CropImage.this.mBitmap));
                    ItemObject.setParam("lpszJSFileContent", "ImageEnhance.js");
                    ItemObject.setParam("arrInputParamName", arrInputParamName);
                    ItemObject.setParam("arrInputParamValue", arrInputParamValue);
                    VirtualNetworkObject.addToQueue(ItemObject);
                }
            });
        }
        findViewById(R.id.discard).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CropImage.this.setResult(0);
                CropImage.this.finish();
            }
        });
        findViewById(R.id.save).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    CropImage.this.onSaveClicked();
                } catch (Exception e) {
                    CropImage.this.finish();
                }
            }
        });
        findViewById(R.id.rotateLeft).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CropImage.this.mBitmap = Util.rotateImage(CropImage.this.mBitmap, -90.0f);
                CropImage.this.mImageView.setImageRotateBitmapResetBase(new RotateBitmap(CropImage.this.mBitmap), true);
                CropImage.this.mRunFaceDetection.run();
            }
        });
        findViewById(R.id.rotateRight).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CropImage.this.mBitmap = Util.rotateImage(CropImage.this.mBitmap, 90.0f);
                CropImage.this.mImageView.setImageRotateBitmapResetBase(new RotateBitmap(CropImage.this.mBitmap), true);
                CropImage.this.mRunFaceDetection.run();
            }
        });
        startFaceDetection();
    }

    private Uri getImageUri(String path) {
        return Uri.fromFile(new File(path));
    }

    private Bitmap getBitmap(String path) {
        Uri uri = getImageUri(path);
        try {
            InputStream in = this.mContentResolver.openInputStream(uri);
            Options o = new Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int scale = 1;
            if (o.outHeight > 1024 || o.outWidth > 1024) {
                scale = (int) Math.pow(2.0d, (double) ((int) Math.round(Math.log(1024.0d / ((double) Math.max(o.outHeight, o.outWidth))) / Math.log(0.5d))));
            }
            Options o2 = new Options();
            o2.inSampleSize = scale;
            in = this.mContentResolver.openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            in.close();
            return b;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "file " + path + " not found");
            return null;
        } catch (IOException e2) {
            Log.e(TAG, "file " + path + " not found");
            return null;
        }
    }

    private void startFaceDetection() {
        if (!isFinishing()) {
            this.mImageView.setImageBitmapResetBase(this.mBitmap, true);
            Util.startBackgroundJob(this, null, "Please wait…", new Runnable() {
                public void run() {
                    final CountDownLatch latch = new CountDownLatch(1);
                    final Bitmap b = CropImage.this.mBitmap;
                    CropImage.this.mHandler.post(new Runnable() {
                        public void run() {
                            if (!(b == CropImage.this.mBitmap || b == null)) {
                                CropImage.this.mImageView.setImageBitmapResetBase(b, true);
                                CropImage.this.mBitmap.recycle();
                                CropImage.this.mBitmap = b;
                            }
                            if (CropImage.this.mImageView.getScale() == 1.0f) {
                                CropImage.this.mImageView.center(true, true);
                            }
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                        CropImage.this.mRunFaceDetection.run();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, this.mHandler);
        }
    }

    private void onSaveClicked() throws Exception {
        if (!this.mSaving && this.mCrop != null) {
            this.mSaving = true;
            Rect r = this.mCrop.getCropRect();
            int width = r.width();
            int height = r.height();
            try {
                Bitmap croppedImage = Bitmap.createBitmap(width, height, this.mCircleCrop ? Config.ARGB_8888 : Config.RGB_565);
                if (croppedImage != null) {
                    Bitmap b;
                    new Canvas(croppedImage).drawBitmap(this.mBitmap, r, new Rect(0, 0, width, height), null);
                    if (this.mCircleCrop) {
                        Canvas c = new Canvas(croppedImage);
                        Path p = new Path();
                        p.addCircle(((float) width) / 2.0f, ((float) height) / 2.0f, ((float) width) / 2.0f, Direction.CW);
                        c.clipPath(p, Op.DIFFERENCE);
                        c.drawColor(0, Mode.CLEAR);
                    }
                    if (!(this.mOutputX == 0 || this.mOutputY == 0)) {
                        if (this.mScale) {
                            Bitmap old = croppedImage;
                            croppedImage = Util.transform(new Matrix(), croppedImage, this.mOutputX, this.mOutputY, this.mScaleUp);
                            if (old != croppedImage) {
                                old.recycle();
                            }
                        } else {
                            b = Bitmap.createBitmap(this.mOutputX, this.mOutputY, Config.RGB_565);
                            Canvas canvas = new Canvas(b);
                            Rect srcRect = this.mCrop.getCropRect();
                            Rect dstRect = new Rect(0, 0, this.mOutputX, this.mOutputY);
                            int dx = (srcRect.width() - dstRect.width()) / 2;
                            int dy = (srcRect.height() - dstRect.height()) / 2;
                            srcRect.inset(Math.max(0, dx), Math.max(0, dy));
                            dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));
                            canvas.drawBitmap(this.mBitmap, srcRect, dstRect, null);
                            croppedImage.recycle();
                            croppedImage = b;
                        }
                    }
                    Bundle myExtras = getIntent().getExtras();
                    if (myExtras == null || (myExtras.getParcelable("data") == null && !myExtras.getBoolean(RETURN_DATA))) {
                        b = croppedImage;
                        Util.startBackgroundJob(this, null, getString(R.string.saving_image), new Runnable() {
                            public void run() {
                                CropImage.this.saveOutput(b);
                            }
                        }, this.mHandler);
                        return;
                    }
                    Bundle extras = new Bundle();
                    extras.putParcelable("data", croppedImage);
                    setResult(-1, new Intent().setAction(ACTION_INLINE_DATA).putExtras(extras));
                    finish();
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveOutput(Bitmap croppedImage) {
        if (this.mSaveUri != null) {
            try {
                OutputStream outputStream = this.mContentResolver.openOutputStream(this.mSaveUri);
                if (outputStream != null) {
                    croppedImage.compress(this.mOutputFormat, 90, outputStream);
                }
                Util.closeSilently(outputStream);
                Bundle extras = new Bundle();
                Intent intent = new Intent(this.mSaveUri.toString());
                intent.putExtras(extras);
                intent.putExtra(IMAGE_PATH, this.mImagePath);
                intent.putExtra(ORIENTATION_IN_DEGREES, Util.getOrientationInDegree(this));
                setResult(-1, intent);
            } catch (IOException ex) {
                Log.e(TAG, "Cannot open file: " + this.mSaveUri, ex);
                setResult(0);
                finish();
                return;
            } catch (Throwable th) {
                Util.closeSilently(null);
            }
        } else {
            Log.e(TAG, "not defined image url");
        }
        croppedImage.recycle();
        finish();
    }

    protected void onPause() {
        super.onPause();
        BitmapManager.instance().cancelThreadDecoding(this.mDecodingThreads);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mBitmap != null) {
            this.mBitmap.recycle();
        }
    }

    public static void showStorageToast(Activity activity) {
        showStorageToast(activity, calculatePicturesRemaining(activity));
    }

    public static void showStorageToast(Activity activity, int remaining) {
        String noStorageText = null;
        if (remaining == -1) {
            if (Environment.getExternalStorageState().equals("checking")) {
                noStorageText = activity.getString(R.string.preparing_card);
            } else {
                noStorageText = activity.getString(R.string.no_storage_card);
            }
        } else if (remaining < 1) {
            noStorageText = activity.getString(R.string.not_enough_space);
        }
        if (noStorageText != null) {
            Toast.makeText(activity, noStorageText, DeviceOperationRESTServiceProvider.TIMEOUT).show();
        }
    }

    public static int calculatePicturesRemaining(Activity activity) {
        try {
            String storageDirectory = "";
            if ("mounted".equals(Environment.getExternalStorageState())) {
                storageDirectory = Environment.getExternalStorageDirectory().toString();
            } else {
                storageDirectory = activity.getFilesDir().toString();
            }
            StatFs stat = new StatFs(storageDirectory);
            return (int) ((((float) stat.getAvailableBlocks()) * ((float) stat.getBlockSize())) / 400000.0f);
        } catch (Exception e) {
            return -2;
        }
    }
}
