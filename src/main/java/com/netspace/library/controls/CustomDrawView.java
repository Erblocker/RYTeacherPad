package com.netspace.library.controls;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.multicontent.MultiContentInterface;
import com.netspace.pad.library.R;

public class CustomDrawView extends CustomViewBase implements OnFocusChangeListener, DrawViewActionInterface {
    private Activity m_Activity;
    private Context m_Context;
    private DrawView m_DrawView;
    private ImageButton m_EditButton;
    private ImageButton m_EraseButton;
    private LayoutInflater m_Inflater;
    private CustomDrawView m_This;
    private float m_XPos = 0.0f;
    private float m_YPos = 0.0f;

    public CustomDrawView(Context context) {
        super(context);
        this.m_Context = context;
        this.m_Activity = (Activity) context;
        this.m_Inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.m_Inflater.inflate(R.layout.layout_customdrawview, this);
        this.m_DrawView = (DrawView) findViewById(R.id.drawPad);
        this.m_DrawView.setDrawFocusRect(true);
        this.m_DrawView.setCallback(this);
        setDefaultButtonIcons();
        ((ImageButton) findViewById(R.id.ButtonIncrease)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LayoutParams LayoutParam = (LayoutParams) CustomDrawView.this.m_This.getLayoutParams();
                LayoutParam.height += 100;
                CustomDrawView.this.m_This.setLayoutParams(LayoutParam);
                CustomDrawView.this.m_DrawView.cleanCache();
                CustomDrawView.this.setChanged();
            }
        });
        ((ImageButton) findViewById(R.id.ButtonDecrease)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LayoutParams LayoutParam = (LayoutParams) CustomDrawView.this.m_This.getLayoutParams();
                if (LayoutParam.height > 250) {
                    LayoutParam.height -= 100;
                    CustomDrawView.this.m_This.setLayoutParams(LayoutParam);
                    CustomDrawView.this.m_DrawView.cleanCache();
                    CustomDrawView.this.setChanged();
                }
            }
        });
        this.m_EditButton = (ImageButton) findViewById(R.id.ButtonEdit);
        this.m_EditButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                boolean z = false;
                CustomDrawView.this.m_DrawView.changeWidth(3);
                CustomDrawView.this.m_DrawView.setEraseMode(false);
                DrawView access$1 = CustomDrawView.this.m_DrawView;
                if (!CustomDrawView.this.m_DrawView.getBrushMode()) {
                    z = true;
                }
                access$1.setBrushMode(z);
                if (CustomDrawView.this.m_DrawView.getBrushMode()) {
                    CustomDrawView.this.m_EditButton.setImageResource(R.drawable.ic_edit_light);
                } else {
                    CustomDrawView.this.m_EditButton.setImageResource(R.drawable.ic_edit);
                }
                CustomDrawView.this.m_EraseButton.setImageResource(R.drawable.ic_erase);
                CustomDrawView.this.setChanged();
            }
        });
        this.m_EraseButton = (ImageButton) findViewById(R.id.ButtonErase);
        this.m_EraseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CustomDrawView.this.m_DrawView.changeWidth(3);
                CustomDrawView.this.m_DrawView.setEraseMode(!CustomDrawView.this.m_DrawView.getEraseMode());
                CustomDrawView.this.m_DrawView.setBrushMode(false);
                if (CustomDrawView.this.m_DrawView.getEraseMode()) {
                    CustomDrawView.this.m_EraseButton.setImageResource(R.drawable.ic_erase_light);
                } else {
                    CustomDrawView.this.m_EraseButton.setImageResource(R.drawable.ic_erase);
                }
                CustomDrawView.this.setChanged();
                CustomDrawView.this.m_EditButton.setImageResource(R.drawable.ic_edit);
            }
        });
        ((ImageButton) findViewById(R.id.ButtonDelete)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (CustomDrawView.this.m_Activity instanceof MultiContentInterface) {
                    ((MultiContentInterface) CustomDrawView.this.m_Activity).DeleteComponent(CustomDrawView.this.m_This);
                }
            }
        });
        ((ImageButton) findViewById(R.id.ButtonMoveUp)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (CustomDrawView.this.m_Activity instanceof MultiContentInterface) {
                    ((MultiContentInterface) CustomDrawView.this.m_Activity).MoveComponentUp(CustomDrawView.this.m_This);
                }
            }
        });
        ((ImageButton) findViewById(R.id.ButtonMoveDown)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (CustomDrawView.this.m_Activity instanceof MultiContentInterface) {
                    ((MultiContentInterface) CustomDrawView.this.m_Activity).MoveComponentDown(CustomDrawView.this.m_This);
                }
            }
        });
        onFocusChange(this.m_DrawView, false);
        this.m_DrawView.setOnFocusChangeListener(this);
        this.m_DrawView.setEnableCache(true);
        this.m_This = this;
        onInVisible();
    }

    public void onVisible() {
        this.m_DrawView.setPausePaint(false);
        this.m_DrawView.invalidate();
        super.onVisible();
    }

    public void onInVisible() {
        if (this.m_DrawView.getEnableCache()) {
            this.m_DrawView.cleanCache();
        }
        this.m_DrawView.setPausePaint(true);
        super.onInVisible();
    }

    public void clear() {
        this.m_DrawView.clear();
    }

    public void startDefaultAction() {
        this.m_EditButton.performClick();
        super.startDefaultAction();
    }

    public void onFocusChange(View v, boolean hasFocus) {
        super.onFocusChange(v, hasFocus);
        if (!this.m_bFocused) {
            this.m_DrawView.setEraseMode(false);
            this.m_DrawView.setBrushMode(false);
            this.m_EraseButton.setImageResource(R.drawable.ic_erase);
            this.m_EditButton.setImageResource(R.drawable.ic_edit);
            if (this.m_Activity instanceof MultiContentInterface) {
                this.m_Activity.SetDisableScrollView(false);
            }
        }
    }

    public void OnTouchDown() {
        if (this.m_Activity instanceof MultiContentInterface) {
            MultiContentInterface Activity = this.m_Activity;
            if (this.m_DrawView.getBrushMode() || this.m_DrawView.getEraseMode()) {
                Activity.SetDisableScrollView(true);
                setChanged();
            }
        }
        if (this.m_DrawView.getBrushMode() || this.m_DrawView.getEraseMode()) {
            setChanged();
        }
    }

    public void OnTouchUp() {
        if (this.m_Activity instanceof MultiContentInterface) {
            MultiContentInterface Activity = this.m_Activity;
            if (this.m_DrawView.getBrushMode() || this.m_DrawView.getEraseMode()) {
                Activity.SetDisableScrollView(false);
            }
        }
    }

    public String getData() {
        return this.m_DrawView.getDataAsString();
    }

    public boolean putData(String szData) {
        return this.m_DrawView.fromString(szData);
    }

    public void OnPenButtonDown() {
        if (!this.m_bLocked) {
            if (this.m_DrawView.getBrushMode()) {
                this.m_EraseButton.performClick();
            } else {
                this.m_EditButton.performClick();
            }
        }
    }

    public void OnPenButtonUp() {
    }

    public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
    }

    public void OnTouchPen() {
    }

    public void OnTouchFinger() {
    }

    public void OnTouchMove() {
    }
}
