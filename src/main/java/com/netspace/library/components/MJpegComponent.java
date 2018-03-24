package com.netspace.library.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.controls.CustomSurfaceView;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.servers.MJpegDisplayThread;
import com.netspace.library.servers.MJpegDisplayThread.MJpegCallInterface;
import com.netspace.library.servers.MJpegDisplayThread.MJpegFrameData;
import com.netspace.library.struct.Size;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class MJpegComponent extends CustomFrameLayout implements IComponents, DrawViewActionInterface, MJpegCallInterface {
    private static final String TAG = "MJpegComponent";
    private String mBase64ImageData;
    private ComponentCallBack mCallBack;
    private LinearLayout mCaptureLayout;
    private String mClientID;
    private ContextThemeWrapper mContextThemeWrapper;
    private String mData;
    private ImageView mImageView;
    private LinearLayout mLayoutButtons;
    private String mMulticastTCPAddress;
    private Runnable mReconnectRunnable = new Runnable() {
        public void run() {
            if (MJpegComponent.this.m_MJpegDisplayThread != null && !MJpegComponent.this.m_MJpegDisplayThread.isConnected()) {
                MJpegComponent.this.m_MJpegDisplayThread.stopDisplay(false);
                MJpegComponent.this.m_MJpegDisplayThread = null;
                if (!MJpegComponent.this.m_szMJpegServer.isEmpty()) {
                    if (MJpegComponent.this.mbUsingMulticast) {
                        MJpegComponent.this.m_MJpegDisplayThread = new MJpegDisplayThread(MJpegComponent.this.getContext(), MJpegComponent.this.m_MJpegMessageHandler, 0, MJpegComponent.this.m_szMJpegServer, MJpegComponent.this.mMulticastTCPAddress, MJpegComponent.this.mnMulticastPort, MJpegComponent.this.mScreenWidth, MJpegComponent.this.mScreenHeight, MJpegComponent.this);
                    } else {
                        MJpegComponent.this.m_MJpegDisplayThread = new MJpegDisplayThread(MJpegComponent.this.getContext(), MJpegComponent.this.m_MJpegMessageHandler, 0, MJpegComponent.this.m_szMJpegServer, MJpegComponent.this);
                    }
                    MJpegComponent.this.m_MJpegDisplayThread.setSurface(MJpegComponent.this.m_SurfaceView.getHolder().getSurface());
                    MJpegComponent.this.m_MJpegDisplayThread.setDecodePartImage(true);
                    MJpegComponent.this.m_MJpegDisplayThread.setNoReallocBitmap(true);
                    MJpegComponent.this.m_MJpegDisplayThread.start();
                    Utilities.showToastMessage("尝试重新建立和大屏幕的连接", 0);
                    MJpegComponent.this.m_Handler.postDelayed(MJpegComponent.this.mReconnectRunnable, 6000);
                }
            }
        }
    };
    private View mRootView;
    private int mScreenHeight;
    private ScreenStateReceiver mScreenReceiver;
    private int mScreenWidth;
    private TextView mTextMessage;
    private TextView mTextViewMessage;
    private DrawView m_DrawView;
    private Handler m_Handler = new Handler();
    private MJpegDisplayThread m_MJpegDisplayThread;
    private Handler m_MJpegMessageHandler = new Handler() {
        private long nFPS = 0;
        private long nLastSecond = 0;

        public void handleMessage(Message msg) {
            if (msg.obj != null && (msg.obj instanceof MJpegFrameData)) {
                MJpegFrameData FrameData = msg.obj;
                Bitmap bm = FrameData.bm;
                if (MJpegComponent.this.m_nWidth == 1000 && MJpegComponent.this.m_nHeight == 1000 && bm != null) {
                    MJpegComponent.this.m_nWidth = bm.getWidth();
                    MJpegComponent.this.m_nHeight = bm.getHeight();
                    MJpegComponent.this.m_DrawView.setSize(MJpegComponent.this.m_nWidth, MJpegComponent.this.m_nHeight);
                    MJpegComponent.this.DoBestFit();
                }
                boolean bDisplay = false;
                if (!MJpegComponent.this.mbNoDisplay) {
                    bDisplay = true;
                }
                if (bDisplay) {
                    MJpegComponent.this.m_DrawView.setBackgroundBitmap(bm);
                    MJpegComponent.this.m_DrawView.invalidate();
                }
                FrameData.DisplayObject.setFrameHandled();
                this.nFPS++;
                if (System.currentTimeMillis() - this.nLastSecond > 1000) {
                    Log.i(MJpegComponent.TAG, "Display fps = " + this.nFPS);
                    this.nFPS = 0;
                    this.nLastSecond = System.currentTimeMillis();
                }
            } else if (msg.obj != null && (msg.obj instanceof Size)) {
                Size FrameSize = msg.obj;
                if (MJpegComponent.this.m_nWidth == 1000 && MJpegComponent.this.m_nHeight == 1000) {
                    MJpegComponent.this.m_nWidth = FrameSize.getWidth();
                    MJpegComponent.this.m_nHeight = FrameSize.getHeight();
                    MJpegComponent.this.m_DrawView.setSize(MJpegComponent.this.m_nWidth, MJpegComponent.this.m_nHeight);
                    MJpegComponent.this.DoBestFit();
                }
            }
        }
    };
    private CustomSurfaceView m_SurfaceView;
    private PointF m_TouchStartPoint;
    private float m_fLastDistance = 0.0f;
    private float m_fLastScale = 1.0f;
    private int m_nHeight = 1000;
    private int m_nLastXPos = 0;
    private int m_nLastYPos = 0;
    private int m_nWidth = 1000;
    private String m_szMJpegServer = "";
    private boolean mbDataLoaded = false;
    private boolean mbDetachedFromWindow = false;
    private boolean mbFirstCall = false;
    private boolean mbNoDisplay = false;
    private boolean mbUsingMulticast = false;
    private boolean mbWindowMode = false;
    private int mnMulticastPort;

    private class ScreenStateReceiver extends BroadcastReceiver {
        private ScreenStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                Log.i(MJpegComponent.TAG, "Screen went OFF");
                if (MJpegComponent.this.mbWindowMode) {
                    Log.i(MJpegComponent.TAG, "Window mode. Execute onPause");
                    MJpegComponent.this.onPause();
                }
            } else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                Log.i(MJpegComponent.TAG, "Screen went On");
                if (MJpegComponent.this.mbWindowMode) {
                    Log.i(MJpegComponent.TAG, "Window mode. Execute onResume");
                    MJpegComponent.this.onResume();
                }
            }
        }
    }

    public MJpegComponent(Context context) {
        super(context);
        initView();
    }

    public MJpegComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MJpegComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_mjpeg, this, true);
        this.m_DrawView = (DrawView) this.mRootView.findViewById(R.id.drawPad);
        this.m_DrawView.setCallback(this);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setTextColor(Integer.MIN_VALUE);
        this.mTextViewMessage.setBackgroundColor(-2130706433);
        if (MyiBaseApplication.isDebugOn()) {
            this.mTextViewMessage.setLines(20);
        }
        this.m_DrawView.setSize(this.m_nWidth, this.m_nHeight);
        this.m_DrawView.setEnableCache(true);
        this.m_SurfaceView = (CustomSurfaceView) this.mRootView.findViewById(R.id.surfaceView1);
        this.mLayoutButtons = (LinearLayout) this.mRootView.findViewById(R.id.layoutButons);
        if (this.mLayoutButtons != null) {
            if (this.mbWindowMode) {
                this.mLayoutButtons.setVisibility(8);
            } else {
                this.mLayoutButtons.setVisibility(0);
            }
        }
        ImageView ButtonBestFit = (ImageView) this.mRootView.findViewById(R.id.buttonBestFit);
        ButtonBestFit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MJpegComponent.this.DoBestFit();
            }
        });
        ButtonBestFit.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_search).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        ImageView ButtonZoom = (ImageView) this.mRootView.findViewById(R.id.buttonMax);
        if (ButtonZoom != null) {
            ButtonZoom.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_window_restore).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        }
        this.m_DrawView.setKeepScreenOn(false);
    }

    public void attachButton(int nButton, OnClickListener onClickListener) {
        ImageView button = (ImageView) this.mRootView.findViewById(nButton);
        if (button == null) {
            return;
        }
        if (onClickListener != null) {
            button.setVisibility(0);
            button.setOnClickListener(onClickListener);
            return;
        }
        button.setVisibility(8);
    }

    public void setServerInfo(String szServerInfo) {
        this.m_szMJpegServer = szServerInfo;
        this.mbUsingMulticast = false;
        this.mTextViewMessage.setText("正在连接到" + this.m_szMJpegServer + "...");
    }

    public void setServerInfo(String szMulticastServerInfo, String szMulticastTCPServer, int nPort, int nWidth, int nHeight) {
        this.mbUsingMulticast = true;
        this.m_szMJpegServer = szMulticastServerInfo;
        this.mnMulticastPort = nPort;
        this.mScreenWidth = nWidth;
        this.mScreenHeight = nHeight;
        this.mMulticastTCPAddress = szMulticastTCPServer;
        this.mTextViewMessage.setText("正在连接到" + this.m_szMJpegServer + "...");
    }

    public void cloneServerInfo(MJpegComponent target) {
        target.mbUsingMulticast = this.mbUsingMulticast;
        target.m_szMJpegServer = this.m_szMJpegServer;
        target.mnMulticastPort = this.mnMulticastPort;
        target.mScreenWidth = this.mScreenWidth;
        target.mScreenHeight = this.mScreenHeight;
        target.mMulticastTCPAddress = this.mMulticastTCPAddress;
    }

    public void setWindowMode(boolean bEnable) {
        this.mbWindowMode = bEnable;
        if (this.mLayoutButtons == null) {
            return;
        }
        if (this.mbWindowMode) {
            this.mLayoutButtons.setVisibility(8);
        } else {
            this.mLayoutButtons.setVisibility(0);
        }
    }

    public void setData(String szData) {
    }

    public String getData() {
        return this.mData;
    }

    public void setClientID(String szClientID) {
        this.mClientID = szClientID;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }

    public void onResume() {
        Utilities.logInfo("MJpegComponent onResume");
        Log.d(TAG, "onResume");
        this.mbNoDisplay = false;
        if (this.m_MJpegDisplayThread == null) {
            if (!Utilities.isScreenOn()) {
                Log.e(TAG, "Screen is not on. Skip onResume MJpegDisplayThread create process.");
            } else if (!this.m_szMJpegServer.isEmpty()) {
                Utilities.logInfo("MJpegComponent onResume ready to create MJpegDisplayThread");
                if (this.mbUsingMulticast) {
                    this.m_MJpegDisplayThread = new MJpegDisplayThread(getContext(), this.m_MJpegMessageHandler, 0, this.m_szMJpegServer, this.mMulticastTCPAddress, this.mnMulticastPort, this.mScreenWidth, this.mScreenHeight, this);
                } else {
                    this.m_MJpegDisplayThread = new MJpegDisplayThread(getContext(), this.m_MJpegMessageHandler, 0, this.m_szMJpegServer, this);
                }
                this.m_MJpegDisplayThread.setSurface(this.m_SurfaceView.getHolder().getSurface());
                this.m_MJpegDisplayThread.setDecodePartImage(true);
                this.m_MJpegDisplayThread.setNoReallocBitmap(true);
                this.m_MJpegDisplayThread.start();
                this.m_Handler.removeCallbacks(this.mReconnectRunnable);
                this.m_Handler.postDelayed(this.mReconnectRunnable, 6000);
            }
        }
    }

    public void onPause() {
        Log.d(TAG, "onPause");
        this.m_Handler.removeCallbacks(this.mReconnectRunnable);
        if (this.m_MJpegDisplayThread != null) {
            this.m_MJpegDisplayThread.stopDisplay(true);
            this.m_MJpegDisplayThread = null;
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Utilities.logInfo("MJpegComponent onAttachedToWindow");
        Log.d(TAG, "onAttachedToWindow");
        this.mScreenReceiver = new ScreenStateReceiver();
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction("android.intent.action.SCREEN_ON");
        screenStateFilter.addAction("android.intent.action.SCREEN_OFF");
        getContext().registerReceiver(this.mScreenReceiver, screenStateFilter);
        if (!this.mbFirstCall) {
            Utilities.runOnUIThread(getContext(), new Runnable() {
                public void run() {
                    MJpegComponent.this.DoBestFit();
                    Log.d(MJpegComponent.TAG, "onAttachedToWindow about to execute onResume");
                    MJpegComponent.this.onResume();
                }
            });
            this.mbFirstCall = false;
        }
    }

    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow");
        Utilities.logInfo("MJpegComponent onDetachedFromWindow");
        this.mbDetachedFromWindow = true;
        getContext().unregisterReceiver(this.mScreenReceiver);
        this.mScreenReceiver = null;
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow about to execute onPause");
        onPause();
    }

    public void DoBestFit() {
        int nDisplayWidth = getWidth();
        int nDisplayHeight = getHeight();
        if (nDisplayWidth != 0 && nDisplayHeight != 0) {
            float fScale = Math.min(((float) nDisplayHeight) / ((float) this.m_nHeight), ((float) nDisplayWidth) / ((float) this.m_nWidth));
            this.m_DrawView.setScale(fScale);
            LayoutParams Param = (LayoutParams) this.m_DrawView.getLayoutParams();
            Param.topMargin = (int) ((((float) nDisplayHeight) - (((float) this.m_nHeight) * fScale)) / 2.0f);
            Param.leftMargin = (int) ((((float) nDisplayWidth) - (((float) this.m_nWidth) * fScale)) / 2.0f);
            this.m_DrawView.setLayoutParams(Param);
            Param = (LayoutParams) this.m_SurfaceView.getLayoutParams();
            Param.topMargin = (int) ((((float) nDisplayHeight) - (((float) this.m_nHeight) * fScale)) / 2.0f);
            Param.leftMargin = (int) ((((float) nDisplayWidth) - (((float) this.m_nWidth) * fScale)) / 2.0f);
            Param.height = (int) (((float) this.m_nHeight) * fScale);
            Param.width = (int) (((float) this.m_nWidth) * fScale);
            this.m_SurfaceView.setLayoutParams(Param);
            this.m_SurfaceView.resizeVideo((int) (((float) this.m_nWidth) * fScale), (int) (((float) this.m_nHeight) * fScale));
        }
    }

    private void getMidPoint(PointF point, MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            point.set((event.getX(0) + event.getX(1)) / 2.0f, (event.getY(0) + event.getY(1)) / 2.0f);
        }
    }

    private float getPointSpacing(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0.0f;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            LayoutParams Param = (LayoutParams) this.m_DrawView.getLayoutParams();
            Param.topMargin--;
            this.m_DrawView.setLayoutParams(Param);
            Param = (LayoutParams) this.m_SurfaceView.getLayoutParams();
            Param.topMargin--;
            this.m_SurfaceView.setLayoutParams(Param);
        }
        switch (event.getActionMasked()) {
            case 1:
            case 6:
                this.m_TouchStartPoint = null;
                break;
            case 2:
                if (this.m_TouchStartPoint != null) {
                    PointF CurrentCenterPoint = new PointF();
                    getMidPoint(CurrentCenterPoint, event);
                    float fCurrentDistance = getPointSpacing(event);
                    float fScale = (fCurrentDistance - this.m_fLastDistance) / fCurrentDistance;
                    float fTotalScale = fScale + this.m_fLastScale;
                    if (((double) fTotalScale) < 0.3d) {
                        this.m_DrawView.setScale(0.3f);
                    } else if (fTotalScale > 3.0f) {
                        this.m_DrawView.setScale(3.0f);
                    } else {
                        this.m_DrawView.setScale(this.m_fLastScale + fScale);
                    }
                    float fNewWidth = ((float) this.m_nWidth) * this.m_DrawView.getScale();
                    float fNewHeight = ((float) this.m_nHeight) * this.m_DrawView.getScale();
                    int nNewLeftMargin = (int) (CurrentCenterPoint.x - (((float) this.m_nLastXPos) * this.m_DrawView.getScale()));
                    int nNewTopMargin = (int) (CurrentCenterPoint.y - (((float) this.m_nLastYPos) * this.m_DrawView.getScale()));
                    LayoutParams Param2 = (LayoutParams) this.m_DrawView.getLayoutParams();
                    Param2.topMargin = nNewTopMargin;
                    Param2.leftMargin = nNewLeftMargin;
                    Param2.width = (int) fNewWidth;
                    Param2.height = (int) fNewHeight;
                    this.m_DrawView.setLayoutParams(Param2);
                    Param2 = (LayoutParams) this.m_SurfaceView.getLayoutParams();
                    Param2.topMargin = nNewTopMargin;
                    Param2.leftMargin = nNewLeftMargin;
                    Param2.width = (int) fNewWidth;
                    Param2.height = (int) fNewHeight;
                    this.m_SurfaceView.resizeVideo((int) fNewWidth, (int) fNewHeight);
                    this.m_SurfaceView.setLayoutParams(Param2);
                    break;
                }
                break;
            case 5:
                if (this.m_TouchStartPoint == null) {
                    this.m_TouchStartPoint = new PointF();
                }
                getMidPoint(this.m_TouchStartPoint, event);
                Param = (LayoutParams) this.m_DrawView.getLayoutParams();
                this.m_fLastDistance = getPointSpacing(event);
                this.m_fLastScale = this.m_DrawView.getScale();
                this.m_nLastXPos = (int) ((this.m_TouchStartPoint.x - ((float) Param.leftMargin)) / this.m_fLastScale);
                this.m_nLastYPos = (int) ((this.m_TouchStartPoint.y - ((float) Param.topMargin)) / this.m_fLastScale);
                break;
        }
        return true;
    }

    public DrawView getDrawView() {
        return this.m_DrawView;
    }

    public void OnNewMJpegInstance(MJpegDisplayThread NewThread) {
        this.m_MJpegDisplayThread = NewThread;
    }

    public void OnMJpegMessage(final String szMessage) {
        this.mTextViewMessage.post(new Runnable() {
            public void run() {
                MJpegComponent.this.mTextViewMessage.setTextColor(Integer.MIN_VALUE);
                MJpegComponent.this.mTextViewMessage.setBackgroundColor(-2130706433);
                if (MyiBaseApplication.isDebugOn()) {
                    MJpegComponent.this.setNewMessage(szMessage, false);
                } else {
                    MJpegComponent.this.mTextViewMessage.setText(szMessage);
                }
            }
        });
    }

    public void OnMJpegError(final String szMessage) {
        this.mTextViewMessage.post(new Runnable() {
            public void run() {
                MJpegComponent.this.mTextViewMessage.setBackgroundColor(-8388608);
                MJpegComponent.this.mTextViewMessage.setTextColor(-1);
                if (MyiBaseApplication.isDebugOn()) {
                    MJpegComponent.this.setNewMessage(szMessage, true);
                } else {
                    MJpegComponent.this.mTextViewMessage.setText(szMessage);
                }
            }
        });
    }

    private void setNewMessage(String szMessage, boolean bPushOldMessage) {
        int i;
        String[] arrText = this.mTextViewMessage.getText().toString().split("\n");
        ArrayList<String> arrFinalText = new ArrayList();
        String szLastMessage = "";
        if (arrText.length > 0) {
            szLastMessage = arrText[arrText.length - 1];
            if (szLastMessage.equalsIgnoreCase(szMessage)) {
                return;
            }
        }
        for (Object add : arrText) {
            arrFinalText.add(add);
        }
        if (!(bPushOldMessage || szLastMessage.isEmpty() || szMessage.startsWith(szLastMessage.substring(0, 4)))) {
            bPushOldMessage = true;
        }
        if (bPushOldMessage) {
            arrFinalText.add(szMessage);
        } else if (arrFinalText.size() == 0) {
            arrFinalText.add(szMessage);
        } else {
            arrFinalText.set(arrFinalText.size() - 1, szMessage);
        }
        String szFinalText = "";
        for (i = Math.max(0, arrFinalText.size() - this.mTextViewMessage.getMaxLines()); i < arrFinalText.size(); i++) {
            szFinalText = new StringBuilder(String.valueOf(szFinalText)).append((String) arrFinalText.get(i)).append("\n").toString();
        }
        this.mTextViewMessage.setText(szFinalText.substring(0, szFinalText.length() - 1));
    }

    public void OnTouchDown() {
    }

    public void OnTouchUp() {
    }

    public void OnPenButtonDown() {
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
