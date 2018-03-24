package com.foxit.uiextensions.controls.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.utils.AppUtil;

public class UIEncryptionDialogFragment extends AppCompatDialogFragment {
    private static final String BUNDLE_KEY_ENCRYPT = "BUNDLE_KEY_ENCRYPT";
    private boolean mEncrypt;
    private UIEncryptionDialogEventListener mEncryptionDialogEventListener;

    public interface UIEncryptionDialogEventListener {
        void onCancel();

        void onConfirmed(boolean z);
    }

    public void setEncryptionDialogEventListener(UIEncryptionDialogEventListener listener) {
        this.mEncryptionDialogEventListener = listener;
    }

    public static UIEncryptionDialogFragment newInstance(boolean encrypt) {
        UIEncryptionDialogFragment fragment = new UIEncryptionDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_KEY_ENCRYPT, encrypt);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (this.mEncryptionDialogEventListener != null) {
            this.mEncryptionDialogEventListener.onCancel();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreInstance(getArguments());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        if (savedInstanceState != null) {
            restoreInstance(savedInstanceState);
        }
        UITextEditDialog dialog = new UITextEditDialog(getActivity());
        dialog.getInputEditText().setVisibility(8);
        if (this.mEncrypt) {
            dialog.getPromptTextView().setText(R.string.rv_encrypt_dialog_description);
            dialog.setTitle(R.string.rv_encrypt_dialog_title);
        } else {
            dialog.getPromptTextView().setText(R.string.rv_decrypt_dialog_description);
            dialog.setTitle(R.string.rv_decrypt_dialog_title);
        }
        dialog.getOKButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AppUtil.isFastDoubleClick()) {
                    UIEncryptionDialogFragment.this.dismissAllowingStateLoss();
                    if (UIEncryptionDialogFragment.this.mEncryptionDialogEventListener != null) {
                        UIEncryptionDialogFragment.this.mEncryptionDialogEventListener.onConfirmed(UIEncryptionDialogFragment.this.mEncrypt);
                    }
                }
            }
        });
        dialog.getCancelButton().setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UIEncryptionDialogFragment.this.dismissAllowingStateLoss();
                if (UIEncryptionDialogFragment.this.mEncryptionDialogEventListener != null) {
                    UIEncryptionDialogFragment.this.mEncryptionDialogEventListener.onCancel();
                }
            }
        });
        return dialog.getDialog();
    }

    public void onSaveInstanceState(Bundle arg0) {
        super.onSaveInstanceState(arg0);
        arg0.putBoolean(BUNDLE_KEY_ENCRYPT, this.mEncrypt);
    }

    private void restoreInstance(Bundle savedInstance) {
        this.mEncrypt = savedInstance.getBoolean(BUNDLE_KEY_ENCRYPT);
    }
}
