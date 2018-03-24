package com.foxit.uiextensions.security.digitalsignature;

import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.PDFViewCtrl.IRecoveryEventListener;
import com.foxit.sdk.common.Library;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.security.certificate.CertificateSupport;
import com.foxit.uiextensions.security.certificate.CertificateViewSupport;
import com.foxit.uiextensions.utils.AppSQLite;
import com.foxit.uiextensions.utils.AppSQLite.FieldInfo;
import java.io.File;
import java.util.ArrayList;

public class DigitalSignatureModule implements Module {
    private static final String CHANGEFILEPATH = "file_change_path";
    private static final String DB_TABLE_DSG_PFX = "_pfx_dsg_cert";
    private static final String FILENAME = "file_name";
    private static final String FILEPATH = "file_path";
    private static final String ISSUER = "issuer";
    private static final String PASSWORD = "password";
    private static final String PUBLISHER = "publisher";
    private static final String SERIALNUMBER = "serial_number";
    private DigitalSignatureAnnotHandler mAnnotHandler;
    private CertificateSupport mCertSupport;
    private Context mContext;
    private DigitalSignatureUtil mDigitalSignature_Util;
    private DocPathChangeListener mDocPathChangeListener = null;
    private IDrawEventListener mDrawEventListener = new IDrawEventListener() {
        public void onDraw(int pageIndex, Canvas canvas) {
            if (DigitalSignatureModule.this.mAnnotHandler != null) {
                DigitalSignatureModule.this.mAnnotHandler.onDrawForControls(canvas);
            }
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private DigitalSignatureSecurityHandler mSecurityHandler;
    private CertificateSupport mSupport;
    private CertificateViewSupport mViewSupport;
    IRecoveryEventListener recoveryEventListener = new IRecoveryEventListener() {
        public void onWillRecover() {
        }

        public void onRecovered() {
            try {
                Library.registerDefaultSignatureHandler();
            } catch (PDFException e) {
            }
        }
    };

    public interface DocPathChangeListener {
        void onDocPathChange(String str);
    }

    public DigitalSignatureUtil getDSG_Util() {
        return this.mDigitalSignature_Util;
    }

    public DigitalSignatureModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getName() {
        return Module.MODULE_NAME_DIGITALSIGNATURE;
    }

    public AnnotHandler getAnnotHandler() {
        return this.mAnnotHandler;
    }

    public boolean loadModule() {
        if (!AppSQLite.getInstance(this.mContext).isDBOpened()) {
            AppSQLite.getInstance(this.mContext).openDB();
        }
        this.mCertSupport = new CertificateSupport(this.mContext);
        this.mViewSupport = new CertificateViewSupport(this.mContext, this.mCertSupport);
        this.mSupport = new CertificateSupport(this.mContext);
        this.mSecurityHandler = new DigitalSignatureSecurityHandler(this.mContext, this.mPdfViewCtrl, this.mSupport);
        this.mAnnotHandler = new DigitalSignatureAnnotHandler(this.mContext, this.mParent, this.mPdfViewCtrl, this.mSecurityHandler);
        this.mDigitalSignature_Util = new DigitalSignatureUtil(this.mContext, this.mPdfViewCtrl);
        initDBTableForDSG();
        try {
            Library.registerDefaultSignatureHandler();
            this.mPdfViewCtrl.registerDrawEventListener(this.mDrawEventListener);
            this.mPdfViewCtrl.registerRecoveryEventListener(this.recoveryEventListener);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unloadModule() {
        this.mPdfViewCtrl.unregisterDrawEventListener(this.mDrawEventListener);
        return true;
    }

    public void setDocPathChangeListener(DocPathChangeListener listener) {
        this.mDocPathChangeListener = listener;
    }

    public DocPathChangeListener getDocPathChangeListener() {
        return this.mDocPathChangeListener;
    }

    private void initDBTableForDSG() {
        if (!AppSQLite.getInstance(this.mContext).isTableExist(DB_TABLE_DSG_PFX)) {
            ArrayList<FieldInfo> fieldInfos = new ArrayList();
            fieldInfos.add(new FieldInfo(SERIALNUMBER, AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo(ISSUER, AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo("publisher", AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo(FILEPATH, AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo(CHANGEFILEPATH, AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo(FILENAME, AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo(PASSWORD, AppSQLite.KEY_TYPE_VARCHAR));
            AppSQLite.getInstance(this.mContext).createTable(DB_TABLE_DSG_PFX, fieldInfos);
        }
        File file = new File(this.mContext.getFilesDir() + "/DSGCert");
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
