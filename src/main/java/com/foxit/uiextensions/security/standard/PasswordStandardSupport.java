package com.foxit.uiextensions.security.standard;

import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.security.StandardSecurityHandler;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.utils.AppUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PasswordStandardSupport {
    private boolean bSuccess = false;
    public UITextEditDialog mCheckOwnerPWD;
    private Context mContext;
    private PasswordDialog mDialog;
    public EditText mEditText;
    private String mFilePath = null;
    private boolean mIsDocOpenAuthEvent = true;
    private boolean mIsOwner = false;
    private PDFViewCtrl mPdfViewCtrl;
    private PasswordSettingFragment mSettingDialog;

    public PasswordStandardSupport(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    public String getFilePath() {
        return this.mFilePath;
    }

    public boolean checkOwnerPassword(String password) {
        if (password == null) {
            return false;
        }
        try {
            if (this.mPdfViewCtrl.getDoc().checkPassword(password.getBytes()) == 3) {
                return true;
            }
            return false;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isOwner() {
        boolean isOwner = DocumentManager.getInstance(this.mPdfViewCtrl).isOwner();
        this.mIsOwner = isOwner;
        return isOwner;
    }

    public void showCheckOwnerPasswordDialog(final int operatorType) {
        this.mCheckOwnerPWD = new UITextEditDialog(this.mContext);
        this.mEditText = this.mCheckOwnerPWD.getInputEditText();
        TextView tv = this.mCheckOwnerPWD.getPromptTextView();
        this.mCheckOwnerPWD.setTitle(this.mContext.getString(R.string.rv_doc_encrpty_standard_ownerpassword_title));
        tv.setText(this.mContext.getString(R.string.rv_doc_encrypt_standard_ownerpassword_content));
        final Button button_ok = this.mCheckOwnerPWD.getOKButton();
        Button button_cancel = this.mCheckOwnerPWD.getCancelButton();
        this.mEditText.setInputType(129);
        this.mEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (PasswordStandardSupport.this.mEditText.getText().length() == 0 || PasswordStandardSupport.this.mEditText.getText().length() > 32) {
                    button_ok.setEnabled(false);
                } else {
                    button_ok.setEnabled(true);
                }
            }
        });
        this.mEditText.setKeyListener(new NumberKeyListener() {
            public int getInputType() {
                return 129;
            }

            protected char[] getAcceptedChars() {
                return PasswordConstants.mAcceptChars;
            }
        });
        button_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PasswordStandardSupport.this.mIsDocOpenAuthEvent) {
                    PasswordStandardSupport.this.mIsOwner = PasswordStandardSupport.this.checkOwnerPassword(PasswordStandardSupport.this.mEditText.getText().toString());
                    if (PasswordStandardSupport.this.mIsOwner) {
                        PasswordStandardSupport.this.mCheckOwnerPWD.dismiss();
                        if (operatorType == 13) {
                            PasswordStandardSupport.this.removePassword();
                            return;
                        }
                        return;
                    }
                    PasswordStandardSupport.this.mEditText.setText("");
                    Toast.makeText(PasswordStandardSupport.this.mContext, R.string.rv_doc_encrpty_standard_ownerpassword_failed, 0).show();
                }
            }
        });
        button_cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PasswordStandardSupport.this.mCheckOwnerPWD.dismiss();
            }
        });
        this.mCheckOwnerPWD.show();
        AppUtil.showSoftInput(this.mEditText);
    }

    public void passwordManager(int operatorType) {
        int type = 0;
        try {
            type = this.mPdfViewCtrl.getDoc().getEncryptionType();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        if ((type != 1 || this.mIsOwner) && this.mIsDocOpenAuthEvent) {
            switch (operatorType) {
                case 11:
                    showSettingDialog();
                    return;
                case 13:
                    removePassword();
                    return;
                default:
                    return;
            }
        }
        showCheckOwnerPasswordDialog(operatorType);
    }

    public void showSettingDialog() {
        this.mSettingDialog = new PasswordSettingFragment(this.mContext);
        this.mSettingDialog.init(this, this.mPdfViewCtrl);
        this.mSettingDialog.showDialog();
    }

    public void addPassword(String userPassword, String ownerPassword, boolean isAddAnnot, boolean isCopy, boolean isManagePage, boolean isPrint, boolean isFillForm, boolean isModifyDoc, boolean isTextAccess, String newFilePath) {
        long userPermission;
        showDialog();
        if (isAddAnnot) {
            userPermission = -4 | 32;
        } else {
            userPermission = -4 & -33;
        }
        if (isCopy) {
            userPermission |= 16;
        } else {
            userPermission &= -17;
        }
        if (isManagePage) {
            userPermission |= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        } else {
            userPermission &= -1025;
        }
        if (isPrint) {
            userPermission = (4 | userPermission) | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        } else {
            userPermission &= -2053;
        }
        if (isFillForm) {
            userPermission |= 256;
        } else {
            userPermission &= -257;
        }
        if (isModifyDoc) {
            userPermission |= 8;
        } else {
            userPermission &= -9;
        }
        if (isTextAccess) {
            userPermission |= 512;
        } else {
            userPermission &= -513;
        }
        try {
            StandardSecurityHandler securityHandler = StandardSecurityHandler.create();
            securityHandler.initialize(userPermission, userPassword == null ? null : userPassword.getBytes(), ownerPassword == null ? null : ownerPassword.getBytes(), 2, 16, true);
            if (this.mPdfViewCtrl.getDoc().isEncrypted()) {
                this.mPdfViewCtrl.getDoc().removeSecurity();
            }
            this.mPdfViewCtrl.getDoc().setSecurityHandler(securityHandler);
            reopenDoc(this.mFilePath + "fsencrypt.pdf", userPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removePassword() {
        final UITextEditDialog removePassworDialog = new UITextEditDialog(this.mContext);
        removePassworDialog.setTitle(this.mContext.getString(R.string.rv_doc_encrpty_standard_remove));
        removePassworDialog.getPromptTextView().setText(this.mContext.getString(R.string.rv_doc_encrpty_standard_removepassword_confirm));
        removePassworDialog.getInputEditText().setVisibility(8);
        removePassworDialog.getOKButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PasswordStandardSupport.this.showDialog();
                removePassworDialog.dismiss();
                try {
                    PasswordStandardSupport.this.mPdfViewCtrl.getDoc().removeSecurity();
                    PasswordStandardSupport.this.mIsOwner = true;
                } catch (PDFException e) {
                    e.printStackTrace();
                }
                PasswordStandardSupport.this.reopenDoc(new StringBuilder(String.valueOf(PasswordStandardSupport.this.mFilePath)).append("fsencrypt.pdf").toString(), null);
            }
        });
        removePassworDialog.getCancelButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                removePassworDialog.dismiss();
            }
        });
        removePassworDialog.show();
    }

    private static int getDialogTheme() {
        if (VERSION.SDK_INT >= 21) {
            return 16973941;
        }
        if (VERSION.SDK_INT >= 14) {
            return 16974132;
        }
        if (VERSION.SDK_INT >= 11) {
            return 16973941;
        }
        return R.style.rv_dialog_style;
    }

    public void showDialog() {
        ((Activity) this.mContext).runOnUiThread(new Runnable() {
            public void run() {
                if (PasswordStandardSupport.this.mDialog == null) {
                    PasswordStandardSupport.this.mDialog = new PasswordDialog(PasswordStandardSupport.this.mContext, PasswordStandardSupport.getDialogTheme());
                    PasswordStandardSupport.this.mDialog.getWindow().setBackgroundDrawableResource(R.color.ux_color_translucent);
                }
                PasswordStandardSupport.this.mDialog.show();
            }
        });
    }

    public void hideDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
        if (this.mSettingDialog != null && this.mSettingDialog.isShowing()) {
            this.mSettingDialog.dismiss();
            this.mSettingDialog = null;
        }
    }

    public boolean getIsOwner() {
        return this.mIsOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.mIsOwner = isOwner;
    }

    public boolean isDocOpenAuthEvent() {
        return this.mIsDocOpenAuthEvent;
    }

    public void setDocOpenAuthEvent(boolean mIsDocOpenAuthEvent) {
        this.mIsDocOpenAuthEvent = mIsDocOpenAuthEvent;
    }

    private static boolean copyFile(String oriPath, String desPath) {
        Throwable th;
        if (oriPath == null || desPath == null) {
            return false;
        }
        OutputStream os = null;
        try {
            OutputStream os2 = new FileOutputStream(desPath);
            try {
                byte[] buffer = new byte[8192];
                InputStream is = new FileInputStream(oriPath);
                for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                    os2.write(buffer, 0, len);
                }
                is.close();
                if (os2 != null) {
                    try {
                        os2.flush();
                        os2.close();
                    } catch (IOException e) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e2) {
                os = os2;
            } catch (Throwable th2) {
                th = th2;
                os = os2;
            }
        } catch (Exception e3) {
            if (os == null) {
                return false;
            }
            try {
                os.flush();
                os.close();
                return false;
            } catch (IOException e4) {
                return false;
            }
        } catch (Throwable th3) {
            th = th3;
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e5) {
                    return false;
                }
            }
            throw th;
        }
    }

    private void reopenDoc(final String path, final String password) {
        this.mPdfViewCtrl.addTask(new Task(new CallBack() {
            public void result(Task task) {
                if (PasswordStandardSupport.this.bSuccess) {
                    PasswordStandardSupport.this.mPdfViewCtrl.openDoc(PasswordStandardSupport.this.mFilePath, password == null ? null : password.getBytes());
                    if (password == null) {
                        PasswordStandardSupport.this.mIsDocOpenAuthEvent = true;
                    } else {
                        DocumentManager.getInstance(PasswordStandardSupport.this.mPdfViewCtrl).clearUndoRedo();
                    }
                    PasswordStandardSupport.this.mIsOwner = true;
                    PasswordStandardSupport.this.hideDialog();
                }
            }
        }) {
            protected void execute() {
                try {
                    PasswordStandardSupport.this.bSuccess = PasswordStandardSupport.this.mPdfViewCtrl.getDoc().saveAs(path, 0);
                    if (PasswordStandardSupport.this.bSuccess) {
                        File oriFile = new File(PasswordStandardSupport.this.mFilePath);
                        if (oriFile.exists()) {
                            oriFile.delete();
                        }
                        File newFile = new File(path);
                        if (newFile.exists()) {
                            PasswordStandardSupport.this.bSuccess = PasswordStandardSupport.copyFile(path, PasswordStandardSupport.this.mFilePath);
                            if (PasswordStandardSupport.this.bSuccess) {
                                newFile.delete();
                                PasswordStandardSupport.this.bSuccess = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    PasswordStandardSupport.this.bSuccess = false;
                }
            }
        });
    }
}
