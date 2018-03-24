package com.foxit.uiextensions.security.digitalsignature;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.security.certificate.CertificateFileInfo;
import com.foxit.uiextensions.security.certificate.CertificateFragment.ICertDialogCallback;
import com.foxit.uiextensions.security.certificate.CertificateSupport;
import com.foxit.uiextensions.security.certificate.CertificateViewSupport;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppSQLite;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DigitalSignatureUtil implements IDigitalSignatureUtil {
    private static final String CHANGEFILEPATH = "file_change_path";
    private static final String DB_TABLE_DSG_PFX = "_pfx_dsg_cert";
    private static final String FILENAME = "file_name";
    private static final String FILEPATH = "file_path";
    public static final int FULLPERMCODE = 3900;
    private static final String ISSUER = "issuer";
    private static final String PASSWORD = "password";
    private static final String PUBLISHER = "publisher";
    private static final String SERIALNUMBER = "serial_number";
    public DigitalSignatureAnnotHandler mAnnotHandler;
    public Bitmap mBitmap;
    public CertificateSupport mCertSupport = new CertificateSupport(this.mContext);
    public Context mContext;
    public CertificateFileInfo mFileInfo;
    public PDFViewCtrl mPdfViewCtrl;
    public DigitalSignatureSecurityHandler mSecurityHandler = new DigitalSignatureSecurityHandler(this.mContext, this.mPdfViewCtrl, this.mSupport);
    public CertificateSupport mSupport = new CertificateSupport(this.mContext);
    public CertificateViewSupport mViewSupport = new CertificateViewSupport(this.mContext, this.mCertSupport);

    public DigitalSignatureUtil(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    private void changeColor(Bitmap bitmap) {
        try {
            int[] colors = new int[(bitmap.getHeight() * bitmap.getWidth())];
            bitmap.getPixels(colors, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            for (int i = 0; i < colors.length; i++) {
                int color = colors[i];
                if (color != 0) {
                    colors[i] = Color.argb(Color.alpha(color), Color.blue(color), Color.green(color), Color.red(color));
                }
            }
            this.mBitmap = Bitmap.createBitmap(colors, bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    public void addCertSignature(String docPath, String certPath, Bitmap bitmap, RectF rectF, int pageIndex, IDigitalSignatureCreateCallBack callBack) {
        final Bitmap bitmap2 = bitmap;
        final String str = certPath;
        final String str2 = docPath;
        final int i = pageIndex;
        final RectF rectF2 = rectF;
        final IDigitalSignatureCreateCallBack iDigitalSignatureCreateCallBack = callBack;
        new Thread(new Runnable() {
            public void run() {
                DigitalSignatureUtil.this.changeColor(bitmap2);
                Cursor cursor = AppSQLite.getInstance(DigitalSignatureUtil.this.mContext).select(DigitalSignatureUtil.DB_TABLE_DSG_PFX, null, null, null, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String newPath = str + "x";
                        if (newPath.equals(cursor.getString(cursor.getColumnIndex(DigitalSignatureUtil.CHANGEFILEPATH)))) {
                            DigitalSignatureUtil.this.copyFile(newPath, cursor.getString(cursor.getColumnIndex(DigitalSignatureUtil.FILEPATH)));
                            final CertificateFileInfo info = new CertificateFileInfo();
                            info.serialNumber = cursor.getString(cursor.getColumnIndex(DigitalSignatureUtil.SERIALNUMBER));
                            info.issuer = cursor.getString(cursor.getColumnIndex(DigitalSignatureUtil.ISSUER));
                            info.publisher = cursor.getString(cursor.getColumnIndex("publisher"));
                            info.filePath = cursor.getString(cursor.getColumnIndex(DigitalSignatureUtil.FILEPATH));
                            info.fileName = cursor.getString(cursor.getColumnIndex(DigitalSignatureUtil.FILENAME));
                            info.password = cursor.getString(cursor.getColumnIndex(DigitalSignatureUtil.PASSWORD));
                            info.isCertFile = false;
                            info.permCode = 3900;
                            Handler handler = new Handler(Looper.getMainLooper());
                            final String str = str2;
                            final int i = i;
                            final RectF rectF = rectF2;
                            final IDigitalSignatureCreateCallBack iDigitalSignatureCreateCallBack = iDigitalSignatureCreateCallBack;
                            handler.post(new Runnable() {
                                public void run() {
                                    DigitalSignatureUtil.this.creatDSGSign(str, i, rectF, iDigitalSignatureCreateCallBack, info);
                                }
                            });
                            break;
                        }
                    }
                    cursor.close();
                }
            }
        }).start();
    }

    public void addCertList(final IDigitalSignatureCallBack callBack) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                CertificateViewSupport certificateViewSupport = DigitalSignatureUtil.this.mViewSupport;
                final IDigitalSignatureCallBack iDigitalSignatureCallBack = callBack;
                certificateViewSupport.showAllPfxFileDialog(true, true, new ICertDialogCallback() {
                    public void result(boolean succeed, Object result, Bitmap forSign) {
                        if (!succeed) {
                            DigitalSignatureUtil.this.mViewSupport.dismissPfxDialog();
                        } else if (result != null) {
                            DigitalSignatureUtil.this.mFileInfo = (CertificateFileInfo) result;
                            String newCertPath = DigitalSignatureUtil.this.mContext.getFilesDir() + "/DSGCert/" + DigitalSignatureUtil.this.mFileInfo.fileName;
                            String changeCertPath = new StringBuilder(String.valueOf(newCertPath)).append("x").toString();
                            DigitalSignatureUtil.this.copyFile(DigitalSignatureUtil.this.mFileInfo.filePath, changeCertPath);
                            ContentValues values = new ContentValues();
                            values.put(DigitalSignatureUtil.ISSUER, DigitalSignatureUtil.this.mFileInfo.issuer);
                            values.put("publisher", DigitalSignatureUtil.this.mFileInfo.publisher);
                            values.put(DigitalSignatureUtil.SERIALNUMBER, DigitalSignatureUtil.this.mFileInfo.serialNumber);
                            values.put(DigitalSignatureUtil.FILEPATH, newCertPath);
                            values.put(DigitalSignatureUtil.CHANGEFILEPATH, changeCertPath);
                            values.put(DigitalSignatureUtil.FILENAME, DigitalSignatureUtil.this.mFileInfo.fileName);
                            values.put(DigitalSignatureUtil.PASSWORD, DigitalSignatureUtil.this.mFileInfo.password);
                            Cursor cursor = AppSQLite.getInstance(DigitalSignatureUtil.this.mContext).select(DigitalSignatureUtil.DB_TABLE_DSG_PFX, null, null, null, null, null, null);
                            AppSQLite.getInstance(DigitalSignatureUtil.this.mContext).insert(DigitalSignatureUtil.DB_TABLE_DSG_PFX, values);
                            if (iDigitalSignatureCallBack != null) {
                                iDigitalSignatureCallBack.onCertSelect(newCertPath, DigitalSignatureUtil.this.mFileInfo.fileName);
                            }
                        }
                    }
                });
            }
        });
    }

    public void creatDSGSign(String docPath, final int pageIndex, final RectF rectF, final IDigitalSignatureCreateCallBack callBack, CertificateFileInfo info) {
        this.mSecurityHandler.addSignature(docPath, info, this.mBitmap, pageIndex, rectF, new Callback() {
            public void result(Event event, boolean success) {
                if (success) {
                    DigitalSignatureUtil.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
                    DigitalSignatureUtil.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(rectF));
                    DigitalSignatureUtil.this.mBitmap.recycle();
                    DigitalSignatureUtil.this.mBitmap = null;
                    Toast.makeText(DigitalSignatureUtil.this.mContext, AppResource.getString(DigitalSignatureUtil.this.mContext, R.string.dsg_sign_succeed), 0).show();
                    callBack.onCreateFinish(true);
                    return;
                }
                DigitalSignatureUtil.this.mBitmap.recycle();
                DigitalSignatureUtil.this.mBitmap = null;
                Toast.makeText(DigitalSignatureUtil.this.mContext, AppResource.getString(DigitalSignatureUtil.this.mContext, R.string.dsg_sign_failed), 0).show();
                callBack.onCreateFinish(false);
            }
        });
    }

    public void copyFile(String oldPath, String newPath) {
        try {
            if (!new File(newPath).exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while (true) {
                    int byteread = inStream.read(buffer);
                    if (byteread == -1) {
                        inStream.close();
                        return;
                    }
                    fs.write(buffer, 0, byteread);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
