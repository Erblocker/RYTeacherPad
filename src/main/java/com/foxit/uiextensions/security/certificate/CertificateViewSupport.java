package com.foxit.uiextensions.security.certificate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UIMatchDialog;
import com.foxit.uiextensions.security.certificate.CertificateFragment.ICertDialogCallback;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import java.util.ArrayList;
import java.util.List;

public class CertificateViewSupport {
    public static final int MESSAGE_FINISH = 18;
    public static final int MESSAGE_HIDEALLPFXFILEDLG = 19;
    public static final int MESSAGE_UPDATE = 17;
    CertificateFragment mCertDialog;
    private List<CertificateFileInfo> mCertInfos = new ArrayList();
    private CertificateSupport mCertSupport;
    private Context mContext;
    private CertificateDataSupport mDataSupport;
    CertificateDetailDialog mDetailDialog;
    private boolean mDoEncrypt;
    private UIMatchDialog mPasswordDialog;

    public CertificateViewSupport(Context context, CertificateSupport support) {
        this.mDataSupport = new CertificateDataSupport(context);
        this.mCertSupport = support;
        this.mContext = context;
    }

    CertificateDataSupport getDataSupport() {
        return this.mDataSupport;
    }

    CertificateSupport getCertSupport() {
        return this.mCertSupport;
    }

    public void showAllPfxFileDialog(boolean isOnlyPFX, boolean x, ICertDialogCallback callback) {
        boolean z = true;
        this.mCertDialog = new CertificateFragment(this.mContext);
        if (!isOnlyPFX) {
            this.mCertDialog.init(this, callback, 1);
        } else if (x) {
            this.mCertDialog.init(this, callback, 3);
        } else {
            this.mCertDialog.init(this, callback, 2);
        }
        if (isOnlyPFX) {
            z = false;
        }
        this.mDoEncrypt = z;
        this.mCertDialog.showDialog();
        this.mCertDialog.setCanceledOnTouchOutside(false);
    }

    public void dismissPfxDialog() {
        FragmentManager fm = this.mContext.getSupportFragmentManager();
        if (this.mCertDialog != null && this.mCertDialog.isShowing()) {
            this.mCertDialog.dismiss();
        }
    }

    void showPasswordDialog(final CertificateFileInfo info, final ICertDialogCallback callback) {
        if (this.mPasswordDialog != null) {
            this.mPasswordDialog.dismiss();
        }
        this.mPasswordDialog = new UIMatchDialog(this.mContext, 0);
        View view = View.inflate(this.mContext, R.layout.rv_password_dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.rv_document_password);
        editText.setVisibility(0);
        ((TextView) view.findViewById(R.id.rv_tips)).setText(AppResource.getString(this.mContext, R.string.rv_security_certlist_inputpasswd));
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.rv_document_password_ly);
        LayoutParams lp = new LayoutParams(-1, -2);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            lp.setMargins(AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_horz_left_margin_pad), 0, AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_horz_right_margin_pad), 0);
        } else {
            lp.setMargins(AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_horz_left_margin_phone), 0, AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_horz_right_margin_phone), 0);
        }
        layout.setLayoutParams(lp);
        AppUtil.showSoftInput(editText);
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (CertificateViewSupport.this.mPasswordDialog != null) {
                    String passwdStr = s.toString();
                    if (passwdStr == null || passwdStr.length() <= 0) {
                        CertificateViewSupport.this.mPasswordDialog.setButtonEnable(false, 4);
                    } else {
                        CertificateViewSupport.this.mPasswordDialog.setButtonEnable(true, 4);
                    }
                }
            }
        });
        this.mPasswordDialog.setContentView(view);
        this.mPasswordDialog.setHeight(-2);
        this.mPasswordDialog.setTitle(AppResource.getString(this.mContext, R.string.rv_password_dialog_title));
        this.mPasswordDialog.setButton(5);
        this.mPasswordDialog.setButtonEnable(false, 4);
        this.mPasswordDialog.setBackButtonVisible(8);
        this.mPasswordDialog.setListener(new DialogListener() {
            public void onResult(long btType) {
                if (btType == 4) {
                    String psd = editText.getText().toString();
                    info.certificateInfo = CertificateViewSupport.this.mCertSupport.verifyPassword(info.filePath, psd);
                    if (info.certificateInfo != null) {
                        info.password = psd;
                        info.issuer = info.certificateInfo.issuer;
                        info.publisher = info.certificateInfo.publisher;
                        info.serialNumber = info.certificateInfo.serialNumber;
                        if (!CertificateViewSupport.this.mCertInfos.contains(info)) {
                            CertificateViewSupport.this.updateInfo(info);
                        }
                        CertificateViewSupport.this.mPasswordDialog.dismiss();
                        CertificateViewSupport.this.mPasswordDialog = null;
                        if (callback != null) {
                            callback.result(true, null, null);
                            return;
                        }
                        return;
                    }
                    editText.setText("");
                    editText.setFocusable(true);
                    UIToast.getInstance(CertificateViewSupport.this.mContext).show(AppResource.getString(CertificateViewSupport.this.mContext, R.string.rv_security_certlist_invalidpasswd));
                } else if (btType == 1) {
                    CertificateViewSupport.this.mPasswordDialog.dismiss();
                    CertificateViewSupport.this.mPasswordDialog = null;
                    if (callback != null) {
                        callback.result(false, null, null);
                    }
                }
            }

            public void onBackClick() {
            }
        });
        this.mPasswordDialog.showDialog();
        this.mPasswordDialog.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == 4) {
                    CertificateViewSupport.this.mPasswordDialog.dismiss();
                    CertificateViewSupport.this.mPasswordDialog = null;
                    if (callback == null) {
                        return true;
                    }
                    callback.result(false, null, null);
                    return true;
                }
                if (keyCode == 66 && event.getAction() == 0) {
                    String psd = editText.getText().toString();
                    info.certificateInfo = CertificateViewSupport.this.mCertSupport.verifyPassword(info.filePath, psd);
                    if (info.certificateInfo != null) {
                        info.password = psd;
                        info.issuer = info.certificateInfo.issuer;
                        info.publisher = info.certificateInfo.publisher;
                        info.serialNumber = info.certificateInfo.serialNumber;
                        if (!CertificateViewSupport.this.mCertInfos.contains(info)) {
                            CertificateViewSupport.this.updateInfo(info);
                        }
                        CertificateViewSupport.this.mPasswordDialog.dismiss();
                        CertificateViewSupport.this.mPasswordDialog = null;
                        if (callback != null) {
                            callback.result(true, null, null);
                        }
                    } else {
                        editText.setText("");
                        editText.setFocusable(true);
                        UIToast.getInstance(CertificateViewSupport.this.mContext).show(AppResource.getString(CertificateViewSupport.this.mContext, R.string.rv_security_certlist_invalidpasswd));
                    }
                }
                return false;
            }
        });
        this.mPasswordDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                CertificateViewSupport.this.mPasswordDialog.dismiss();
                CertificateViewSupport.this.mPasswordDialog = null;
            }
        });
        this.mPasswordDialog.setCanceledOnTouchOutside(false);
    }

    void showPermissionDialog(CertificateFileInfo info) {
        FragmentActivity act = this.mContext;
        int dlgType = 0;
        if (this.mDoEncrypt) {
            this.mDetailDialog = new CertificateDetailDialog(act, false);
        } else {
            dlgType = 1;
            this.mDetailDialog = new CertificateDetailDialog(act, true);
        }
        this.mDetailDialog.init(dlgType, info);
        this.mDetailDialog.showDialog();
    }

    private void updateInfo(CertificateFileInfo info) {
        if (info.isCertFile) {
            this.mDataSupport.insertCert(info.issuer, info.publisher, info.serialNumber, info.filePath, info.fileName);
        } else {
            this.mDataSupport.insertPfx(info.issuer, info.publisher, info.serialNumber, info.filePath, info.fileName, info.password);
        }
    }

    public void onConfigurationChanged() {
    }
}
