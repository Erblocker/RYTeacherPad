package me.dm7.barcodescanner.zbar;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import me.dm7.barcodescanner.core.BarcodeScannerView;
import me.dm7.barcodescanner.core.DisplayUtils;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class ZBarScannerView extends BarcodeScannerView {
    private static final String TAG = "ZBarScannerView";
    private List<BarcodeFormat> mFormats;
    private ResultHandler mResultHandler;
    private ImageScanner mScanner;

    public interface ResultHandler {
        void handleResult(Result result);
    }

    static {
        System.loadLibrary("iconv");
    }

    public ZBarScannerView(Context context) {
        super(context);
        setupScanner();
    }

    public ZBarScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupScanner();
    }

    public void setFormats(List<BarcodeFormat> formats) {
        this.mFormats = formats;
        setupScanner();
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.mResultHandler = resultHandler;
    }

    public Collection<BarcodeFormat> getFormats() {
        if (this.mFormats == null) {
            return BarcodeFormat.ALL_FORMATS;
        }
        return this.mFormats;
    }

    public void setupScanner() {
        this.mScanner = new ImageScanner();
        this.mScanner.setConfig(0, 256, 3);
        this.mScanner.setConfig(0, 257, 3);
        this.mScanner.setConfig(0, 0, 0);
        for (BarcodeFormat format : getFormats()) {
            this.mScanner.setConfig(format.getId(), 0, 1);
        }
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        if (this.mResultHandler != null) {
            try {
                Size size = camera.getParameters().getPreviewSize();
                int width = size.width;
                int height = size.height;
                if (DisplayUtils.getScreenOrientation(getContext()) == 1) {
                    int rotationCount = getRotationCount();
                    if (rotationCount == 1 || rotationCount == 3) {
                        int tmp = width;
                        width = height;
                        height = tmp;
                    }
                    data = getRotatedData(data, camera);
                }
                Rect rect = getFramingRectInPreview(width, height);
                Image barcode = new Image(width, height, "Y800");
                barcode.setData(data);
                barcode.setCrop(rect.left, rect.top, rect.width(), rect.height());
                if (this.mScanner.scanImage(barcode) != 0) {
                    SymbolSet syms = this.mScanner.getResults();
                    final Result rawResult = new Result();
                    Iterator it = syms.iterator();
                    while (it.hasNext()) {
                        String symData;
                        Symbol sym = (Symbol) it.next();
                        if (VERSION.SDK_INT >= 19) {
                            symData = new String(sym.getDataBytes(), StandardCharsets.UTF_8);
                        } else {
                            symData = sym.getData();
                        }
                        if (!TextUtils.isEmpty(symData)) {
                            rawResult.setContents(symData);
                            rawResult.setBarcodeFormat(BarcodeFormat.getFormatById(sym.getType()));
                            break;
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            ResultHandler tmpResultHandler = ZBarScannerView.this.mResultHandler;
                            ZBarScannerView.this.mResultHandler = null;
                            ZBarScannerView.this.stopCameraPreview();
                            if (tmpResultHandler != null) {
                                tmpResultHandler.handleResult(rawResult);
                            }
                        }
                    });
                    return;
                }
                camera.setOneShotPreviewCallback(this);
            } catch (RuntimeException e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void resumeCameraPreview(ResultHandler resultHandler) {
        this.mResultHandler = resultHandler;
        super.resumeCameraPreview();
    }
}
