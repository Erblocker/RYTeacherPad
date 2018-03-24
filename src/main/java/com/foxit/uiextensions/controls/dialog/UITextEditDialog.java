package com.foxit.uiextensions.controls.dialog;

import android.content.Context;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppUtil;
import java.util.regex.Pattern;

public class UITextEditDialog extends UIDialog {
    AppDisplay mAppDisplay;
    TextWatcher mBaseTextChangeWatcher;
    Button mCancelButton;
    boolean mCheckEditEmail;
    View mCuttingLine;
    String mEditPattern;
    EditText mInputEditText = ((EditText) this.mContentView.findViewById(R.id.fx_dialog_edittext));
    Button mOkButton;
    TextView mPromptTextView = ((TextView) this.mContentView.findViewById(R.id.fx_dialog_textview));

    public UITextEditDialog(Context context) {
        super(context, R.layout.fx_dialog_tv_edittext, getDialogTheme(), AppDisplay.getInstance(context).getUITextEditDialogWidth());
        this.mInputEditText.setTextColor(-16777216);
        this.mAppDisplay = AppDisplay.getInstance(context);
        if (this.mAppDisplay.isPad()) {
            usePadDimes();
        }
        this.mOkButton = (Button) this.mContentView.findViewById(R.id.fx_dialog_ok);
        this.mCuttingLine = this.mContentView.findViewById(R.id.fx_dialog_button_cutting_line);
        this.mCancelButton = (Button) this.mContentView.findViewById(R.id.fx_dialog_cancel);
        this.mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UITextEditDialog.this.mDialog.dismiss();
            }
        });
        this.mInputEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (66 != keyCode || event.getAction() != 0) {
                    return false;
                }
                ((InputMethodManager) v.getContext().getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });
        this.mBaseTextChangeWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean z = false;
                String text1 = UITextEditDialog.this.mInputEditText.getText().toString();
                String text2 = stringFilter(text1);
                if (!text1.equals(text2)) {
                    UITextEditDialog.this.mInputEditText.setText(text2);
                    UITextEditDialog.this.mInputEditText.setSelection(UITextEditDialog.this.mInputEditText.length());
                }
                if (!UITextEditDialog.this.mCheckEditEmail) {
                    Button button = UITextEditDialog.this.mOkButton;
                    if (text2.toString().length() != 0) {
                        z = true;
                    }
                    button.setEnabled(z);
                } else if (AppUtil.isEmailFormatForRMS(text2)) {
                    UITextEditDialog.this.mOkButton.setEnabled(true);
                } else {
                    UITextEditDialog.this.mOkButton.setEnabled(false);
                }
            }

            public void afterTextChanged(Editable s) {
            }

            private String stringFilter(String input) {
                return (UITextEditDialog.this.mEditPattern == null || UITextEditDialog.this.mEditPattern.length() == 0) ? input : Pattern.compile(UITextEditDialog.this.mEditPattern).matcher(input).replaceAll("");
            }
        };
        this.mInputEditText.addTextChangedListener(this.mBaseTextChangeWatcher);
        this.mDialog.getWindow().setBackgroundDrawableResource(17170445);
    }

    private void usePadDimes() {
        try {
            ((LayoutParams) this.mTitleView.getLayoutParams()).leftMargin = this.mAppDisplay.dp2px(24.0f);
            ((LayoutParams) this.mTitleView.getLayoutParams()).rightMargin = this.mAppDisplay.dp2px(24.0f);
            ((LayoutParams) this.mPromptTextView.getLayoutParams()).leftMargin = this.mAppDisplay.dp2px(24.0f);
            ((LayoutParams) this.mPromptTextView.getLayoutParams()).rightMargin = this.mAppDisplay.dp2px(24.0f);
            ((LayoutParams) this.mInputEditText.getLayoutParams()).leftMargin = this.mAppDisplay.dp2px(24.0f);
            ((LayoutParams) this.mInputEditText.getLayoutParams()).rightMargin = this.mAppDisplay.dp2px(24.0f);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e("UITextEditDialog", e.getMessage());
            } else {
                Log.e("UITextEditDialog", "usePadDimes_has_an_error");
            }
        }
    }

    public TextView getPromptTextView() {
        return this.mPromptTextView;
    }

    public EditText getInputEditText() {
        return this.mInputEditText;
    }

    public Button getOKButton() {
        return this.mOkButton;
    }

    public Button getCancelButton() {
        return this.mCancelButton;
    }

    public void setPattern(String pattern) {
        this.mEditPattern = pattern;
    }

    public void show() {
        if (this.mOkButton.getVisibility() == 0 && this.mCancelButton.getVisibility() == 0) {
            this.mCuttingLine.setVisibility(0);
            this.mOkButton.setBackgroundResource(R.drawable.dialog_right_button_background_selector);
            this.mCancelButton.setBackgroundResource(R.drawable.dialog_left_button_background_selector);
            if (VERSION.SDK_INT < 11) {
                this.mOkButton.setBackgroundResource(R.drawable.dialog_left_button_background_selector);
                this.mCancelButton.setBackgroundResource(R.drawable.dialog_right_button_background_selector);
            }
        } else {
            this.mCuttingLine.setVisibility(8);
            this.mOkButton.setBackgroundResource(R.drawable.dialog_button_background_selector);
            this.mCancelButton.setBackgroundResource(R.drawable.dialog_button_background_selector);
        }
        super.show();
    }

    public boolean isShowing() {
        return this.mDialog.isShowing();
    }

    public static int getDialogTheme() {
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
}
