package com.foxit.read.common;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.foxit.uiextensions.Module;
import com.foxit.view.propertybar.IML_MultiLineBar.IML_ValueChangeListener;

public class RD_ScreenLock implements Module {
    public static String MODULE_NAME_SCREENLOCK = "ScreenLock Module";
    private Context mContext;
    private IML_ValueChangeListener mScreenLockListener = new IML_ValueChangeListener() {
        public void onValueChanged(int type, Object value) {
            if (6 == type) {
                final ScreenLockDialog dialog = new ScreenLockDialog(RD_ScreenLock.this.mContext);
                dialog.setCurOption(RD_ScreenLock.this.getScreenLockPosition());
                dialog.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        if (position != RD_ScreenLock.this.getScreenLockPosition()) {
                            RD_ScreenLock.this.setOrientation(position);
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        }

        public void onDismiss() {
        }

        public int getType() {
            return 6;
        }
    };

    public RD_ScreenLock(Context context) {
        this.mContext = context;
    }

    public String getName() {
        return MODULE_NAME_SCREENLOCK;
    }

    public boolean loadModule() {
        initApplyValue();
        return true;
    }

    public boolean unloadModule() {
        return true;
    }

    private void initApplyValue() {
        setOrientation(2);
    }

    private void setOrientation(int orientation) {
        switch (orientation) {
            case 0:
                ((Activity) this.mContext).setRequestedOrientation(0);
                return;
            case 1:
                ((Activity) this.mContext).setRequestedOrientation(1);
                return;
            case 2:
                ((Activity) this.mContext).setRequestedOrientation(2);
                return;
            default:
                return;
        }
    }

    public IML_ValueChangeListener getScreenLockListener() {
        return this.mScreenLockListener;
    }

    private int getScreenLockPosition() {
        if (((Activity) this.mContext).getRequestedOrientation() == 0) {
            return 0;
        }
        if (((Activity) this.mContext).getRequestedOrientation() == 1) {
            return 1;
        }
        return 2;
    }
}
