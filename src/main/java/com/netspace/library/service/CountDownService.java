package com.netspace.library.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;

public class CountDownService extends Service {
    private static final int HIDE_WINDOW = 300;
    public static final String OPERATION = "operation";
    public static final int OPERATION_HIDE = 101;
    public static final int OPERATION_SHOW = 100;
    private static final int SHOW_WINDOW = 200;
    public static final String TAG = "CountDownService";
    private static LayoutParams mParams;
    private static WindowManager mWindowManager;
    private boolean mAdded = false;
    private boolean mButtonsShow = false;
    private View mButtonsView;
    private Context mContext;
    private Runnable mCountDownRunnable = new Runnable() {
        public void run() {
            if (CountDownService.this.mnTimeToEnd == 0 || System.currentTimeMillis() <= CountDownService.this.mnTimeToEnd) {
                CountDownService countDownService = CountDownService.this;
                countDownService.mnCountDownSeconds = countDownService.mnCountDownSeconds - 1;
                if (CountDownService.this.mnCountDownSeconds <= 0) {
                    CountDownService.this.stopSelf();
                    return;
                }
                CountDownService.this.mTextView.setText(String.valueOf(CountDownService.this.mnCountDownSeconds));
                CountDownService.this.mHandler.postDelayed(this, 1000);
                return;
            }
            CountDownService.this.stopSelf();
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (!CountDownService.this.mAdded) {
                        CountDownService.mWindowManager.addView(CountDownService.this.mButtonsView, CountDownService.mParams);
                        CountDownService.this.mAdded = true;
                        return;
                    }
                    return;
                case 300:
                    if (CountDownService.this.mAdded) {
                        CountDownService.mWindowManager.removeView(CountDownService.this.mButtonsView);
                        CountDownService.this.mAdded = false;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private LayoutInflater mInflater;
    private boolean mItemMove = false;
    private TextView mTextView;
    private boolean mbNoBackground = false;
    private int mnCountDownSeconds = 5;
    private long mnTimeToEnd = 0;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        this.mHandler.sendEmptyMessage(300);
        super.onDestroy();
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("operation")) {
            this.mnCountDownSeconds = intent.getIntExtra("seconds", 5);
            this.mbNoBackground = intent.getBooleanExtra("nobackground", false);
            this.mnTimeToEnd = intent.getLongExtra("timetoend", 0);
            createFloatView();
            switch (intent.getIntExtra("operation", 100)) {
                case 100:
                    this.mHandler.sendEmptyMessage(200);
                    return;
                case 101:
                    this.mHandler.sendEmptyMessage(300);
                    return;
                default:
                    return;
            }
        }
    }

    private void createFloatView() {
        if (!this.mAdded) {
            this.mContext = getApplicationContext();
            this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            this.mButtonsView = this.mInflater.inflate(R.layout.layout_countdown, null);
            if (this.mbNoBackground) {
                Utilities.setViewBackground(this.mButtonsView.findViewById(R.id.layoutCountDown), null);
            }
            this.mTextView = (TextView) this.mButtonsView.findViewById(R.id.textViewCountDown);
            this.mTextView.setText(String.valueOf(this.mnCountDownSeconds));
            mWindowManager = (WindowManager) getApplicationContext().getSystemService("window");
            mParams = new LayoutParams();
            mParams.type = 2003;
            mParams.format = 1;
            mParams.flags = 40;
            if (this.mbNoBackground) {
                LayoutParams layoutParams = mParams;
                layoutParams.flags |= 16;
            }
            mParams.width = -1;
            mParams.height = -1;
            mWindowManager.addView(this.mButtonsView, mParams);
            this.mAdded = true;
            mWindowManager.getDefaultDisplay().getMetrics(new DisplayMetrics());
            mWindowManager.updateViewLayout(this.mButtonsView, mParams);
            this.mHandler.postDelayed(this.mCountDownRunnable, 1000);
        }
    }
}
