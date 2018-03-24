package com.netspace.library.controls;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.multicontent.MultiContentInterface;
import com.netspace.pad.library.R;

public class CustomTextView extends CustomViewBase implements OnTouchListener, OnFocusChangeListener {
    private String mTextContent;
    private Activity m_Activity;
    private Context m_Context;
    private EditText m_EditText;
    private LayoutInflater m_Inflater;
    private CustomTextView m_This;
    private float m_XPos = 0.0f;
    private float m_YPos = 0.0f;

    public CustomTextView(Context context) {
        super(context);
        this.m_Context = context;
        this.m_Activity = (Activity) context;
        this.m_Inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.m_Inflater.inflate(R.layout.layout_customtextview, this);
        setDefaultButtonIcons();
        this.m_EditText = (EditText) findViewById(R.id.EditText);
        this.m_EditText.setInputType(0);
        this.m_EditText.setSingleLine(false);
        this.m_Activity.getWindow().setSoftInputMode(3);
        ((ImageButton) findViewById(R.id.ButtonIncrease)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LayoutParams LayoutParam = (LayoutParams) CustomTextView.this.m_This.getLayoutParams();
                LayoutParam.height += 200;
                CustomTextView.this.m_This.setLayoutParams(LayoutParam);
                CustomTextView.this.setChanged();
            }
        });
        ((ImageButton) findViewById(R.id.ButtonDecrease)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LayoutParams LayoutParam = (LayoutParams) CustomTextView.this.m_This.getLayoutParams();
                if (LayoutParam.height > 250) {
                    LayoutParam.height -= 200;
                    CustomTextView.this.m_This.setLayoutParams(LayoutParam);
                    CustomTextView.this.setChanged();
                }
            }
        });
        ((ImageButton) findViewById(R.id.ButtonEdit)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CustomTextView.this.m_EditText.setInputType(131073);
                CustomTextView.this.m_EditText.setSingleLine(false);
                CustomTextView.this.m_EditText.requestFocus();
                ((InputMethodManager) CustomTextView.this.m_Context.getSystemService("input_method")).showSoftInput(CustomTextView.this.m_EditText, 2);
            }
        });
        ((ImageButton) findViewById(R.id.ButtonDelete)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (CustomTextView.this.m_Activity instanceof MultiContentInterface) {
                    ((MultiContentInterface) CustomTextView.this.m_Activity).DeleteComponent(CustomTextView.this.m_This);
                }
            }
        });
        ((ImageButton) findViewById(R.id.ButtonMoveUp)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (CustomTextView.this.m_Activity instanceof MultiContentInterface) {
                    ((MultiContentInterface) CustomTextView.this.m_Activity).MoveComponentUp(CustomTextView.this.m_This);
                }
            }
        });
        ((ImageButton) findViewById(R.id.ButtonMoveDown)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (CustomTextView.this.m_Activity instanceof MultiContentInterface) {
                    ((MultiContentInterface) CustomTextView.this.m_Activity).MoveComponentDown(CustomTextView.this.m_This);
                }
            }
        });
        this.m_EditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (CustomTextView.this.mTextContent == null) {
                    CustomTextView.this.setChanged();
                } else if (!CustomTextView.this.mTextContent.contentEquals(s)) {
                    CustomTextView.this.setChanged();
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.m_EditText.setOnFocusChangeListener(this);
        this.m_This = this;
    }

    public boolean onTouch(View arg0, MotionEvent event) {
        this.m_XPos = event.getX();
        this.m_YPos = event.getY();
        return false;
    }

    public void onFocusChange(View v, boolean hasFocus) {
        super.onFocusChange(v, hasFocus);
        this.m_EditText.setInputType(0);
        this.m_EditText.setSingleLine(false);
        this.m_Activity.getWindow().setSoftInputMode(3);
    }

    public void startDefaultAction() {
        ((ImageButton) findViewById(R.id.ButtonEdit)).performClick();
        super.startDefaultAction();
    }

    public String getData() {
        return this.m_EditText.getText().toString();
    }

    public boolean putData(String szData) {
        this.mTextContent = szData;
        this.m_EditText.setText(szData);
        return true;
    }
}
