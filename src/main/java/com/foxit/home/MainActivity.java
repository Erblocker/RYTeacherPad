package com.foxit.home;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.app.App;
import com.foxit.home.local.HM_LocalModule;
import com.foxit.sdk.common.PDFError;
import com.foxit.uiextensions.utils.UIToast;

public class MainActivity extends AppCompatActivity {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$foxit$sdk$common$PDFError;
    private RelativeLayout mContentView;
    private RelativeLayout mRootView;
    private RelativeLayout mTopToolBar;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$foxit$sdk$common$PDFError() {
        int[] iArr = $SWITCH_TABLE$com$foxit$sdk$common$PDFError;
        if (iArr == null) {
            iArr = new int[PDFError.values().length];
            try {
                iArr[PDFError.CERTIFICATE_ERROR.ordinal()] = 6;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[PDFError.DATA_CONFLICT.ordinal()] = 16;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[PDFError.DATA_NOT_FOUND.ordinal()] = 14;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[PDFError.FILE_ERROR.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[PDFError.FORMAT_ERROR.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[PDFError.HANDLER_ERROR.ordinal()] = 5;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[PDFError.INVALID_OBJECT_TYPE.ordinal()] = 15;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[PDFError.LICENSE_INVALID.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[PDFError.NOT_PARSED_ERROR.ordinal()] = 13;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[PDFError.NO_ERROR.ordinal()] = 1;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[PDFError.OOM.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[PDFError.PARAM_INVALID.ordinal()] = 9;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[PDFError.PASSWORD_INVALID.ordinal()] = 4;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[PDFError.SECURITY_HANDLE_ERROR.ordinal()] = 12;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[PDFError.UNKNOWN_ERROR.ordinal()] = 7;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[PDFError.UNSUPPORTED.ordinal()] = 10;
            } catch (NoSuchFieldError e16) {
            }
            $SWITCH_TABLE$com$foxit$sdk$common$PDFError = iArr;
        }
        return iArr;
    }

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(1);
        super.onCreate(savedInstanceState);
        if (checkLicense()) {
            if (App.instance().getDisplay().isPad()) {
                this.mRootView = (RelativeLayout) View.inflate(App.instance().getApplicationContext(), R.layout.hf_home_right_pad, null);
            } else {
                this.mRootView = (RelativeLayout) View.inflate(App.instance().getApplicationContext(), R.layout.hf_home_right_phone, null);
            }
            this.mTopToolBar = (RelativeLayout) this.mRootView.findViewById(R.id.toptoolbar);
            this.mContentView = (RelativeLayout) this.mRootView.findViewById(R.id.contentview);
            HM_LocalModule module = new HM_LocalModule(this);
            module.loadModule();
            module.loadHomeModule(this);
            module.onActivated();
            this.mContentView.removeAllViews();
            this.mContentView.addView(module.getContentView(this));
            View view = module.getTopToolbar(this);
            LayoutParams params;
            if (view == null) {
                this.mTopToolBar.setVisibility(8);
                params = (LayoutParams) this.mContentView.getLayoutParams();
                params.topMargin = 0;
                this.mContentView.setLayoutParams(params);
            } else {
                this.mTopToolBar.setVisibility(0);
                this.mTopToolBar.addView(view);
                params = (LayoutParams) this.mContentView.getLayoutParams();
                if (App.instance().getDisplay().isPad()) {
                    params.topMargin = (int) App.instance().getApplicationContext().getResources().getDimension(R.dimen.ux_toolbar_height_pad);
                } else {
                    params.topMargin = (int) App.instance().getApplicationContext().getResources().getDimension(R.dimen.ux_toolbar_height_phone);
                }
                this.mContentView.setLayoutParams(params);
            }
            setContentView(this.mRootView);
        }
    }

    private boolean checkLicense() {
        switch ($SWITCH_TABLE$com$foxit$sdk$common$PDFError()[PDFError.valueOf(Integer.valueOf(App.getLicenseErrCode())).ordinal()]) {
            case 1:
                return true;
            case 8:
                UIToast.getInstance(this).show((CharSequence) "The License is invalid!");
                return false;
            default:
                UIToast.getInstance(this).show((CharSequence) "Failed to initialize the library!");
                return false;
        }
    }
}
