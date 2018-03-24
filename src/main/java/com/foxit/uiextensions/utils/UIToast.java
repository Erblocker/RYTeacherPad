package com.foxit.uiextensions.utils;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.Toast;
import com.foxit.uiextensions.R;

public class UIToast {
    private static final int HIDE = 3843;
    private static final int INIT = 3841;
    private static final int LAST = 2;
    private static final int LONG = 1;
    private static final String MODE = "mode";
    private static final String MSG = "message";
    private static final int SHOW = 3842;
    private static UIToast mInstance = null;
    private Toast mAnnotToast;
    private AppDisplay mAppDisplay;
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == UIToast.INIT) {
                UIToast.this.mToast = Toast.makeText(UIToast.this.mContext, "", 1);
            } else if (msg.what == UIToast.SHOW) {
                if (UIToast.this.mToast != null) {
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        Object obj = bundle.get(UIToast.MSG);
                        if (obj != null) {
                            try {
                                if (obj instanceof Integer) {
                                    UIToast.this.mToast.setText(((Integer) Integer.class.cast(obj)).intValue());
                                } else if (obj instanceof CharSequence) {
                                    UIToast.this.mToast.setText((CharSequence) CharSequence.class.cast(obj));
                                }
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            }
                            int mode = bundle.getInt(UIToast.MODE, 1);
                            if (mode == 2) {
                                UIToast.this.mToast.setDuration(1);
                                UIToast.this.mToast.show();
                                Message m = Message.obtain();
                                m.copyFrom(msg);
                                UIToast.this.mHandler.sendMessageDelayed(m, 3000);
                                return;
                            }
                            UIToast.this.mHandler.removeMessages(UIToast.SHOW);
                            UIToast.this.mToast.setDuration(mode);
                            UIToast.this.mToast.show();
                        }
                    }
                }
            } else if (msg.what == UIToast.HIDE) {
                UIToast.this.mHandler.removeMessages(UIToast.SHOW);
                if (UIToast.this.mToast != null) {
                    UIToast.this.mToast.cancel();
                }
            }
        }
    };
    private Toast mToast;

    public static UIToast getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UIToast(context);
        }
        return mInstance;
    }

    private UIToast(Context context) {
        this.mContext = context;
        this.mAppDisplay = AppDisplay.getInstance(context);
        this.mHandler.obtainMessage(INIT).sendToTarget();
        initAnnotToast();
    }

    public void show(int resId) {
        Message msg = this.mHandler.obtainMessage(SHOW);
        Bundle data = new Bundle();
        data.putInt(MSG, resId);
        data.putInt(MODE, 1);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void show(CharSequence s) {
        Message msg = this.mHandler.obtainMessage(SHOW);
        Bundle data = new Bundle();
        data.putCharSequence(MSG, s);
        data.putInt(MODE, 1);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void show(int resId, int duration) {
        Message msg = this.mHandler.obtainMessage(SHOW);
        Bundle data = new Bundle();
        data.putInt(MSG, resId);
        data.putInt(MODE, duration);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void show(CharSequence s, int duration) {
        Message msg = this.mHandler.obtainMessage(SHOW);
        Bundle data = new Bundle();
        data.putCharSequence(MSG, s);
        data.putInt(MODE, duration);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void show(int resId, long timeout) {
        this.mHandler.removeMessages(SHOW);
        Message msg = Message.obtain();
        msg.what = SHOW;
        Bundle data = new Bundle();
        data.putInt(MSG, resId);
        data.putInt(MODE, 2);
        msg.setData(data);
        msg.sendToTarget();
        this.mHandler.sendEmptyMessageDelayed(HIDE, timeout);
    }

    public void show(CharSequence s, long timeout) {
        this.mHandler.removeMessages(SHOW);
        Message msg = this.mHandler.obtainMessage(SHOW);
        Bundle data = new Bundle();
        data.putCharSequence(MSG, s);
        data.putInt(MODE, 2);
        msg.setData(data);
        msg.sendToTarget();
        this.mHandler.sendEmptyMessageDelayed(HIDE, timeout);
    }

    private void initAnnotToast() {
        try {
            int yOffset;
            this.mAnnotToast = new Toast(this.mContext);
            this.mAnnotToast.setView(((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.annot_continue_create_tips, null));
            this.mAnnotToast.setDuration(0);
            if (this.mAppDisplay.isPad()) {
                yOffset = AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_toolbar_height_pad) + (this.mAppDisplay.dp2px(16.0f) * 3);
            } else {
                yOffset = AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_toolbar_height_phone) + (this.mAppDisplay.dp2px(16.0f) * 3);
            }
            this.mAnnotToast.setGravity(80, 0, yOffset);
        } catch (Exception e) {
            this.mAnnotToast = null;
        }
    }
}
