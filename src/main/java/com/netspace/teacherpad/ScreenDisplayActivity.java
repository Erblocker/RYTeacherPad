package com.netspace.teacherpad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.NovaIcons;
import com.netspace.library.adapter.FragmentsPageAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.controls.CustomSurfaceView;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.controls.DrawView.DrawViewDrawActionInterface;
import com.netspace.library.im.IMService;
import com.netspace.library.servers.MJpegDisplayThread;
import com.netspace.library.servers.MJpegDisplayThread.MJpegCallInterface;
import com.netspace.library.servers.MJpegDisplayThread.MJpegFrameData;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.struct.Size;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.SwipeTouchListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PutTemporaryStorageItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.library.wrapper.CameraCaptureActivity;
import com.netspace.library.wrapper.CameraCaptureActivity.CameraCaptureCallBack;
import com.netspace.teacherpad.adapter.ScreenSelectAdapter;
import com.netspace.teacherpad.controls.CustomMultiScreenView;
import com.netspace.teacherpad.controls.CustomMultiScreenView.MultiScreenCallBack;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import com.netspace.teacherpad.fragments.PDFResourcesListFragment;
import com.netspace.teacherpad.fragments.ResourcesListFragment;
import com.netspace.teacherpad.fragments.ScreenResourcesHistoryListFragment;
import com.netspace.teacherpad.popup.PencialPopupWindow;
import com.netspace.teacherpad.popup.PencialPopupWindow.OnChangeCallBack;
import com.netspace.teacherpad.structure.MultiScreen;
import com.netspace.teacherpad.util.SimpleTooltip;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import net.sqlcipher.database.SQLiteDatabase;

public class ScreenDisplayActivity extends BaseActivity implements OnClickListener, MJpegCallInterface, DrawViewActionInterface, DrawViewDrawActionInterface, MultiScreenCallBack, OnItemSelectedListener, OnTabSelectedListener {
    protected static final int MENUGROUPSCREEN = 200;
    protected static final String TAG = "ScreenDisplayActivity";
    private static boolean mCursorMode = false;
    private static boolean mEraseMode = false;
    private static ScreenDisplayActivity m_LastFingerActivity;
    private static int mnPenColor = SupportMenu.CATEGORY_MASK;
    private static int mnPenWidth = 3;
    private final Runnable BuildDisplayThreadRunnable = new Runnable() {
        public void run() {
            if (ScreenDisplayActivity.this.m_SurfaceView.getHolder().getSurface().isValid()) {
                if (TeacherPadApplication.mMJpegRelayToMulticastThread == null || !TeacherPadApplication.mMJpegRelayToMulticastThread.isReady()) {
                    ScreenDisplayActivity.this.m_MJpegDisplayThread = new MJpegDisplayThread(ScreenDisplayActivity.this.m_Context, ScreenDisplayActivity.this.mMJpegMessageHandler, 0, ScreenDisplayActivity.this.m_szMJpegServer, ScreenDisplayActivity.this);
                } else {
                    ScreenDisplayActivity.this.m_MJpegDisplayThread = new MJpegDisplayThread(ScreenDisplayActivity.this.m_Context, ScreenDisplayActivity.this.mMJpegMessageHandler, 0, TeacherPadApplication.szCurrentMulticastAddress, Utilities.getWifiIP(MyiBaseApplication.getBaseAppContext()), TeacherPadApplication.mMJpegRelayToMulticastThread.getPort(), TeacherPadApplication.mMJpegRelayToMulticastThread.getWidth(), TeacherPadApplication.mMJpegRelayToMulticastThread.getHeight(), ScreenDisplayActivity.this);
                }
                ScreenDisplayActivity.this.m_MJpegDisplayThread.setSurface(ScreenDisplayActivity.this.m_SurfaceView.getHolder().getSurface());
                ScreenDisplayActivity.this.m_MJpegDisplayThread.setDecodePartImage(true);
                ScreenDisplayActivity.this.m_MJpegDisplayThread.setNoReallocBitmap(true);
                ScreenDisplayActivity.this.m_MJpegDisplayThread.start();
                return;
            }
            ScreenDisplayActivity.this.m_Handler.postDelayed(ScreenDisplayActivity.this.BuildDisplayThreadRunnable, 300);
        }
    };
    private final Runnable ClearDrawPadRunnable = new Runnable() {
        public void run() {
            while (true) {
                int nDataCount = ScreenDisplayActivity.this.m_DrawView.getDataPointsCount();
                if (nDataCount > ScreenDisplayActivity.this.mLastPointerCount) {
                    String szNewData = ScreenDisplayActivity.this.m_DrawView.getDataAsString(ScreenDisplayActivity.this.mLastPointerCount);
                    ScreenDisplayActivity.this.mLastPointerCount = nDataCount;
                    ScreenDisplayActivity.this.mnLastPointerTime = System.currentTimeMillis();
                    if (szNewData != null) {
                        TeacherPadApplication.IMThread.SendMessage("WhiteBoardData: " + szNewData + " " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                    }
                } else {
                    ScreenDisplayActivity.this.mLastData = "";
                    ScreenDisplayActivity.this.mLastPointerCount = 0;
                    ScreenDisplayActivity.this.m_DrawView.cleanCache();
                    ScreenDisplayActivity.this.m_DrawView.clearPoints();
                    ScreenDisplayActivity.this.m_DrawView.invalidate();
                    return;
                }
            }
        }
    };
    private final Runnable GetPlayPosRunable = new Runnable() {
        public void run() {
            if (TeacherPadApplication.IMThread != null) {
                TeacherPadApplication.IMThread.SendMessage("PlayStats", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                TeacherPadApplication.IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            }
            ScreenDisplayActivity.this.m_Handler.postDelayed(this, 1000);
        }
    };
    private final Runnable SendDrawPadDataRunnable = new Runnable() {
        public void run() {
            int nDataCount = ScreenDisplayActivity.this.m_DrawView.getDataPointsCount();
            if (nDataCount > ScreenDisplayActivity.this.mLastPointerCount) {
                String szNewData = ScreenDisplayActivity.this.m_DrawView.getDataAsString(ScreenDisplayActivity.this.mLastPointerCount);
                ScreenDisplayActivity.this.mLastPointerCount = nDataCount;
                ScreenDisplayActivity.this.mnLastPointerTime = System.currentTimeMillis();
                Log.d(ScreenDisplayActivity.TAG, "DataPoints " + nDataCount + "," + ScreenDisplayActivity.this.m_DrawView.getDataPointsCount());
                if (szNewData != null) {
                    TeacherPadApplication.IMThread.SendMessage("WhiteBoardData: " + szNewData + " " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                }
            }
        }
    };
    private FragmentsPageAdapter mAdapter;
    private LinearLayout mBottomLayoutControls;
    private ImageView mButtonBestFit;
    private ImageView mCloseButton;
    private DrawerLayout mDrawerLayout;
    private ImageView mEmptyWhiteBoardButton;
    private ImageView mEraseButton;
    private TextView mInfoTextView;
    private String mLastData = "";
    private int mLastPointerCount = 0;
    private LinearLayout mLayoutControls;
    private Handler mMJpegMessageHandler = new Handler() {
        private long nFPS = 0;
        private long nLastSecond = 0;

        public void handleMessage(Message msg) {
            if (msg.obj != null && (msg.obj instanceof MJpegFrameData)) {
                MJpegFrameData FrameData = msg.obj;
                Bitmap bm = FrameData.bm;
                ScreenDisplayActivity.this.mInfoTextView.setVisibility(4);
                if (ScreenDisplayActivity.this.m_nWidth == 1000 && ScreenDisplayActivity.this.m_nHeight == 1000 && bm != null) {
                    ScreenDisplayActivity.this.m_nWidth = bm.getWidth();
                    ScreenDisplayActivity.this.m_nHeight = bm.getHeight();
                    ScreenDisplayActivity.this.m_DrawView.setSize(ScreenDisplayActivity.this.m_nWidth, ScreenDisplayActivity.this.m_nHeight);
                    ScreenDisplayActivity.this.DoBestFit();
                }
                boolean bDisplay = false;
                if (!ScreenDisplayActivity.this.mbNoDisplay) {
                    bDisplay = true;
                }
                if (bDisplay) {
                    ScreenDisplayActivity.this.m_DrawView.setBackgroundBitmap(bm);
                    ScreenDisplayActivity.this.m_DrawView.invalidate();
                }
                FrameData.DisplayObject.setFrameHandled();
                this.nFPS++;
                if (System.currentTimeMillis() - this.nLastSecond > 1000) {
                    Log.i(ScreenDisplayActivity.TAG, "Display fps = " + this.nFPS);
                    this.nFPS = 0;
                    this.nLastSecond = System.currentTimeMillis();
                }
            } else if (msg.obj != null && (msg.obj instanceof Size)) {
                Size FrameSize = msg.obj;
                ScreenDisplayActivity.this.mInfoTextView.setVisibility(4);
                if (ScreenDisplayActivity.this.m_nWidth == 1000 && ScreenDisplayActivity.this.m_nHeight == 1000) {
                    ScreenDisplayActivity.this.m_nWidth = FrameSize.getWidth();
                    ScreenDisplayActivity.this.m_nHeight = FrameSize.getHeight();
                    ScreenDisplayActivity.this.m_DrawView.setSize(ScreenDisplayActivity.this.m_nWidth, ScreenDisplayActivity.this.m_nHeight);
                    ScreenDisplayActivity.this.DoBestFit();
                }
            }
        }
    };
    private ImageView mMaximizeButton;
    private StartClassControlUnit mMonitorDialog;
    private ImageView mMouseButton;
    private ImageView mMoveButton;
    private ImageView mNextButton;
    private PDFResourcesListFragment mPDFThumbnailFragment;
    private ImageView mPenButton;
    private ImageView mPenColorButton;
    private ImageView mPlayButton;
    private ImageView mPrevButton;
    private ImageView mRestoreButton;
    private ScreenResourcesHistoryListFragment mScreenResourceFragment;
    private ScreenSelectAdapter mScreenSelectAdapter;
    protected Spinner mScreenSpinner;
    private ImageView mStopButton;
    private SwipeTouchListener mSwipeTouchListener = new SwipeTouchListener() {
        public void onSwipeRight() {
            if (TeacherPadApplication.IMThread != null) {
                ScreenDisplayActivity.this.cleanLocalDrawView();
                TeacherPadApplication.IMThread.SendMessage("PlayControl Next " + String.valueOf(TeacherPadApplication.mActiveScreenID));
            }
        }

        public void onSwipeLeft() {
            if (TeacherPadApplication.IMThread != null) {
                ScreenDisplayActivity.this.cleanLocalDrawView();
                TeacherPadApplication.IMThread.SendMessage("PlayControl Prev " + String.valueOf(TeacherPadApplication.mActiveScreenID));
            }
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    };
    private TextView mTextViewIMState;
    private final Runnable mTimerIMStateRunnable = new Runnable() {
        public void run() {
            String szServerInfo = "";
            boolean bIMOK = false;
            if (!VirtualNetworkObject.getOfflineMode()) {
                if (TeacherPadApplication.IMThread != null) {
                    synchronized (TeacherPadApplication.IMThread) {
                        if (TeacherPadApplication.IMThread.IsConnected()) {
                            szServerInfo = "消息系统就绪";
                            bIMOK = true;
                            if (IMService.isDirectIMClientConnected(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                                szServerInfo = "直接连接";
                            } else {
                                szServerInfo = "服务器中转";
                            }
                        } else if (TeacherPadApplication.IMThread.IsConnecting()) {
                            szServerInfo = "消息系统连接中";
                        } else {
                            szServerInfo = "消息系统异常";
                        }
                    }
                } else {
                    szServerInfo = "消息系统异常";
                }
                if (!ScreenDisplayActivity.this.mszJpegMessage.isEmpty()) {
                    szServerInfo = new StringBuilder(String.valueOf(szServerInfo)).append(",").append(ScreenDisplayActivity.this.mszJpegMessage).toString();
                }
                if (bIMOK) {
                    ScreenDisplayActivity.this.mTextViewIMState.setBackgroundColor(-2130706433);
                    ScreenDisplayActivity.this.mTextViewIMState.setTextColor(Integer.MIN_VALUE);
                } else {
                    ScreenDisplayActivity.this.mTextViewIMState.setBackgroundColor(-8388608);
                    ScreenDisplayActivity.this.mTextViewIMState.setTextColor(-1);
                }
                ScreenDisplayActivity.this.mTextViewIMState.setText(szServerInfo);
                ScreenDisplayActivity.this.m_Handler.postDelayed(ScreenDisplayActivity.this.mTimerIMStateRunnable, 1000);
            }
        }
    };
    private SimpleTooltip mTooltip = new SimpleTooltip();
    private Runnable mUpdateWhiteBoardStateRunnable = new Runnable() {
        public void run() {
            if (ScreenDisplayActivity.mCursorMode) {
                ScreenDisplayActivity.this.m_DrawView.setBrushMode(false);
                ScreenDisplayActivity.this.m_DrawView.setPausePaint(true);
                ScreenDisplayActivity.this.m_DrawView.cleanCache();
                ScreenDisplayActivity.this.m_DrawView.clearPoints();
                TeacherPadApplication.IMThread.SendMessage("HideWhiteBoard");
                ScreenDisplayActivity.this.mLastData = "";
                return;
            }
            if (ScreenDisplayActivity.mEraseMode) {
                ScreenDisplayActivity.this.m_DrawView.setEraseMode2(true, -16776192);
                ScreenDisplayActivity.this.m_DrawView.getPaint().setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            } else {
                ScreenDisplayActivity.this.m_DrawView.setEraseMode2(false, -16776192);
                ScreenDisplayActivity.this.m_DrawView.changeWidth(ScreenDisplayActivity.mnPenWidth);
                ScreenDisplayActivity.this.m_DrawView.setEnableCache(true);
                ScreenDisplayActivity.this.m_DrawView.setBrushMode(true);
                ScreenDisplayActivity.this.m_DrawView.setPausePaint(false);
                ScreenDisplayActivity.this.m_DrawView.setColor(ScreenDisplayActivity.mnPenColor);
            }
            TeacherPadApplication.IMThread.SendMessage("ShowWhiteBoard");
        }
    };
    private CustomViewPager mViewPager;
    private ImageView mZoominButton;
    private ImageView mZoomoutButton;
    private Context m_Context;
    private DrawView m_DrawView;
    private Handler m_Handler;
    private MJpegDisplayThread m_MJpegDisplayThread;
    private CustomMultiScreenView m_MultiScreenView;
    private CustomSurfaceView m_SurfaceView;
    private PointF m_TouchStartPoint;
    private boolean m_bTouchOn = false;
    private float m_fLastDistance = 0.0f;
    private float m_fLastScale = 1.0f;
    private int m_nHeight = 1000;
    private int m_nLastXPos = 0;
    private int m_nLastYPos = 0;
    private int m_nWidth = 1000;
    private String m_szMJpegServer = "";
    private boolean mbNeedUpdate = false;
    private boolean mbNoDisplay = false;
    private boolean mbNoMJpegErrorDisplay = false;
    private boolean mbSideMenuExpanded = false;
    private int mnLastActiveControlScreenID = -1;
    private long mnLastPointerTime = 0;
    private String mszJpegMessage = "";

    /* renamed from: com.netspace.teacherpad.ScreenDisplayActivity$19 */
    class AnonymousClass19 implements OnSuccessListener {
        private final /* synthetic */ String val$szUploadName;

        AnonymousClass19(String str) {
            this.val$szUploadName = str;
        }

        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            ScreenDisplayActivity.sendCameraImageDisplayCommand(this.val$szUploadName);
        }
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    public static void Close() {
        if (m_LastFingerActivity != null) {
            m_LastFingerActivity.finish();
            m_LastFingerActivity = null;
        }
    }

    private ImageView initButton(int nButtonID, String szTooltip, Icon buttonIcon, OnClickListener onClickListener, String szShortTitle) {
        return initButton(nButtonID, szTooltip, buttonIcon, false, onClickListener, szShortTitle);
    }

    private ImageView initButton(int nButtonID, String szTooltip, Icon buttonIcon, boolean bUpdateCornerButton, OnClickListener onClickListener, String szShortTitle) {
        ImageView Button = (ImageView) findViewById(nButtonID);
        TextView textViewTooltip = (TextView) ((RelativeLayout) Button.getParent()).getChildAt(1);
        textViewTooltip.setTextSize(32.0f);
        textViewTooltip.setText(szTooltip);
        Button.setImageDrawable(new IconDrawable((Context) this, buttonIcon).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        final OnClickListener onClickListener2 = onClickListener;
        final boolean z = bUpdateCornerButton;
        final int i = nButtonID;
        final Icon icon = buttonIcon;
        Button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onClickListener2 != null) {
                    onClickListener2.onClick(v);
                } else {
                    ScreenDisplayActivity.this.mMonitorDialog.onClick(v);
                }
                if (z) {
                    ScreenDisplayActivity.this.m_MultiScreenView.setMonitorActiveButton(i, ScreenDisplayActivity.this.m_MultiScreenView.getActiveScreenID(), new IconDrawable(ScreenDisplayActivity.this, icon).color(-1).actionBarSize());
                } else {
                    ScreenDisplayActivity.this.displayMonitorTools(ScreenDisplayActivity.this.m_MultiScreenView.getActiveScreenID());
                }
            }
        });
        RelativeLayout relativeLayout = (RelativeLayout) Button.getParent();
        TextView textViewTitle = new TextView(this);
        textViewTitle.setTextSize(3, 6.0f);
        textViewTitle.setGravity(17);
        textViewTitle.setTextColor(-16750849);
        textViewTitle.setText(szShortTitle);
        textViewTitle.setPadding(0, 0, (int) Utilities.dpToPixel(1), (int) Utilities.dpToPixel(4));
        relativeLayout.addView(textViewTitle, -2, -2);
        LayoutParams LayoutParams = (LayoutParams) textViewTitle.getLayoutParams();
        LayoutParams.addRule(12);
        LayoutParams.addRule(9);
        LayoutParams.addRule(11);
        textViewTitle.setLayoutParams(LayoutParams);
        return Button;
    }

    private Bitmap drawScreenPreviewBitmap(int[] nColumns) {
        Bitmap bitmapSource = BitmapFactory.decodeResource(getResources(), R.drawable.ic_display);
        Bitmap bitmap = Utilities.cloneBitmap(bitmapSource, 0, 0, bitmapSource.getWidth(), bitmapSource.getHeight());
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        int nWidth = bitmap.getWidth();
        int nHeight = bitmap.getHeight();
        float fScale = ((float) nWidth) / 128.0f;
        canvas.setBitmap(bitmap);
        paint.setStyle(Style.FILL);
        paint.setStrokeWidth(2.0f);
        paint.setColor(-1);
        paint.setStyle(Style.STROKE);
        paint.setColor(-16777216);
        Rect rect = new Rect((int) (16.0f * fScale), (int) (12.0f * fScale), (int) (111.0f * fScale), (int) (83.0f * fScale));
        int nXStep = rect.width() / nColumns.length;
        paint.setColor(-1118482);
        canvas.drawRect(rect, paint);
        paint.setColor(-16777216);
        int i = 0;
        while (i < nColumns.length) {
            int nYStep = rect.height() / nColumns[i];
            if (nColumns[i] > 1) {
                for (int j = 0; j < nColumns[i]; j++) {
                    if (j != nColumns[i] - 1) {
                        canvas.drawLine((float) (rect.left + (nXStep * i)), (float) (rect.top + ((j + 1) * nYStep)), (float) (rect.left + ((i + 1) * nXStep)), (float) (rect.top + ((j + 1) * nYStep)), paint);
                    }
                }
            }
            if (nColumns.length > 1 && i != nColumns.length - 1) {
                canvas.drawLine((float) (rect.left + ((i + 1) * nXStep)), (float) rect.top, (float) (rect.left + ((i + 1) * nXStep)), (float) rect.bottom, paint);
            }
            i++;
        }
        return bitmap;
    }

    @SuppressLint({"NewApi"})
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (VERSION.SDK_INT >= 19 && hasFocus) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.flags |= -2080374784;
            win.setAttributes(winParams);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int i = VERSION.SDK_INT;
        this.m_Context = this;
        m_LastFingerActivity = this;
        this.mbDisableScreenCopyRunnable = true;
        this.m_Handler = new Handler();
        setContentView((int) R.layout.activity_screendisplay);
        this.mScreenSelectAdapter = new ScreenSelectAdapter(this, R.layout.layout_screen_spinner_row);
        int[] n0 = new int[]{1};
        this.mScreenSelectAdapter.addScreen("1屏", drawScreenPreviewBitmap(n0), n0);
        int[] n1_1 = new int[]{1, 1};
        this.mScreenSelectAdapter.addScreen("2屏", drawScreenPreviewBitmap(n1_1), n1_1);
        int[] n2_0 = new int[]{2};
        this.mScreenSelectAdapter.addScreen("2屏", drawScreenPreviewBitmap(n2_0), n2_0);
        int[] n1_2 = new int[]{1, 2};
        this.mScreenSelectAdapter.addScreen("3屏", drawScreenPreviewBitmap(n1_2), n1_2);
        int[] n2_1 = new int[]{2, 1};
        this.mScreenSelectAdapter.addScreen("3屏", drawScreenPreviewBitmap(n2_1), n2_1);
        int[] n2_2 = new int[]{2, 2};
        this.mScreenSelectAdapter.addScreen("4屏", drawScreenPreviewBitmap(n2_2), n2_2);
        int[] iArr = new int[3];
        iArr = new int[]{3, 3, 3};
        this.mScreenSelectAdapter.addScreen("9屏", drawScreenPreviewBitmap(iArr), iArr);
        this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (VERSION.SDK_INT < 21) {
            this.mDrawerLayout.setDrawerShadow((int) R.drawable.drawer_shadow, 3);
        }
        this.mDrawerLayout.setDrawerListener(new DrawerListener() {
            private boolean mbDataLoaded = false;

            public void onDrawerClosed(View arg0) {
                ScreenDisplayActivity.this.mbSideMenuExpanded = false;
                this.mbDataLoaded = false;
            }

            public void onDrawerOpened(View arg0) {
                ScreenDisplayActivity.this.mbSideMenuExpanded = true;
                if (!this.mbDataLoaded) {
                    ScreenDisplayActivity.this.reloadSideMenuContent();
                    this.mbDataLoaded = true;
                }
            }

            public void onDrawerSlide(View arg0, float arg1) {
                if (!ScreenDisplayActivity.this.mbSideMenuExpanded && !this.mbDataLoaded) {
                    ScreenDisplayActivity.this.reloadSideMenuContent();
                    this.mbDataLoaded = true;
                }
            }

            public void onDrawerStateChanged(int arg0) {
            }
        });
        ImageView imageViewDrawer = (ImageView) findViewById(R.id.imageButtonDrawer);
        imageViewDrawer.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        imageViewDrawer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ScreenDisplayActivity.this.mDrawerLayout.openDrawer(3);
            }
        });
        this.mBottomLayoutControls = (LinearLayout) findViewById(R.id.linearLayoutBottomControls);
        this.mAdapter = new FragmentsPageAdapter(getSupportFragmentManager());
        this.mPDFThumbnailFragment = new PDFResourcesListFragment();
        this.mScreenResourceFragment = new ScreenResourcesHistoryListFragment();
        this.mAdapter.addPage(this.mPDFThumbnailFragment, "缩略图");
        this.mAdapter.addPage(this.mScreenResourceFragment, "历史资源");
        this.mScreenSpinner = (Spinner) findViewById(R.id.spinnerScreenLayout);
        this.mScreenSpinner.setAdapter(this.mScreenSelectAdapter);
        this.mScreenSpinner.setOnItemSelectedListener(this);
        if (!(TeacherPadApplication.mszActiveMonitorLayoutTags == null || TeacherPadApplication.mszActiveMonitorLayoutTags.isEmpty())) {
            this.mScreenSpinner.setSelection(Utilities.toInt(TeacherPadApplication.mszActiveMonitorLayoutTags));
        }
        this.mViewPager = (CustomViewPager) findViewById(R.id.pager);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setOffscreenPageLimit(4);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(this.mViewPager);
        tabLayout.setOnTabSelectedListener(this);
        this.m_DrawView = (DrawView) findViewById(R.id.drawPad);
        this.m_DrawView.setCallback(this);
        this.m_DrawView.setDrawCallback(this);
        this.m_DrawView.setColor(mnPenColor);
        this.m_DrawView.changeWidth(mnPenWidth);
        this.m_DrawView.setOnlyActivePenDraw(true);
        this.m_DrawView.setAllEvent(true);
        this.mUpdateWhiteBoardStateRunnable.run();
        this.m_SurfaceView = (CustomSurfaceView) findViewById(R.id.surfaceView1);
        this.m_SurfaceView.setKeepScreenOn(true);
        this.m_MultiScreenView = (CustomMultiScreenView) findViewById(R.id.drawOverlay);
        this.m_MultiScreenView.setCallBack(this);
        updateCornerButtonState();
        this.mMonitorDialog = new StartClassControlUnit(this);
        if (getIntent().getExtras().containsKey("MJpegServer")) {
            this.m_szMJpegServer = getIntent().getExtras().getString("MJpegServer");
        } else {
            this.m_szMJpegServer = TeacherPadApplication.getMJpegURL();
        }
        TeacherPadApplication.IMThread.SendMessage("ShowWhiteBoard");
        mCursorMode = false;
        this.m_DrawView.setSize(this.m_nWidth, this.m_nHeight);
        this.m_DrawView.setEnableCache(true);
        this.m_DrawView.setSwipeListener(this.mSwipeTouchListener);
        this.mLayoutControls = (LinearLayout) findViewById(R.id.linearLayoutControls);
        this.mLayoutControls.setVisibility(8);
        this.mInfoTextView = (TextView) findViewById(R.id.textViewMJpegInfo);
        this.mInfoTextView.setVisibility(4);
        this.mTextViewIMState = (TextView) findViewById(R.id.textViewIMState);
        this.mTextViewIMState.setVisibility(0);
        this.mTextViewIMState.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                Utilities.showAlertMessage(ScreenDisplayActivity.this.m_Context, "状态提示", "这个标签显示了当前消息系统的连接状态：\n\n直接连接：\n当前睿易派已经和睿易通直接建立了连接，所有命令都在内网直接传递。这种模式效率最高。\n\n服务器中转：\n当前睿易通和睿易派需要通过服务器中转进行消息传递，如果睿易通和睿易派都在一个内网里，请尝试关闭睿易通所在机器的Windows防火墙或者在防火墙上允许睿易通访问网络。\n\n消息系统连接中：\n当前和服务器的消息系统连接正在建立中，请稍候。\n\n消息系统异常：\n当前无法和服务器建立连接，请检查网络是否正常。");
                return false;
            }
        });
        this.mButtonBestFit = initButton(R.id.buttonBestFit, "调整画面到最合适大小", NovaIcons.nova_icon_focus_2, new OnClickListener() {
            public void onClick(View v) {
                if (!ScreenDisplayActivity.this.DoBestFit()) {
                    ScreenDisplayActivity.this.mMonitorDialog.onClick(v);
                }
            }
        }, "适屏");
        this.mPenButton = initButton(R.id.buttonWhiteBoard, "进入手写模式，只能在放大后的屏幕上书写", FontAwesomeIcons.fa_pencil_square_o, true, null, "手写");
        this.mMouseButton = initButton(R.id.buttonMouse, "进入鼠标模式", FontAwesomeIcons.fa_mouse_pointer, true, null, "鼠标");
        this.mZoominButton = initButton(R.id.buttonZoomin, "放大显示内容", FontAwesomeIcons.fa_search_plus, null, "放大");
        this.mZoomoutButton = initButton(R.id.buttonZoomout, "缩小显示内容", FontAwesomeIcons.fa_search_minus, null, "缩小");
        this.mMaximizeButton = initButton(R.id.buttonMaximize, "最大化窗口", FontAwesomeIcons.fa_window_maximize, null, "最大");
        this.mRestoreButton = initButton(R.id.buttonRestore, "还原窗口", FontAwesomeIcons.fa_window_restore, null, "还原");
        this.mCloseButton = initButton(R.id.buttonClose, "关闭当前资源", FontAwesomeIcons.fa_window_close_o, null, "关闭");
        this.mPlayButton = initButton(R.id.buttonPlay, "开始播放", FontAwesomeIcons.fa_play, null, "播放");
        this.mStopButton = initButton(R.id.buttonStop, "停止播放", FontAwesomeIcons.fa_stop, null, "停止");
        this.mNextButton = initButton(R.id.buttonNext, "下一页", FontAwesomeIcons.fa_step_forward, null, "后页");
        this.mPrevButton = initButton(R.id.buttonPrev, "上一页", FontAwesomeIcons.fa_step_backward, null, "前页");
        this.mEraseButton = initButton(R.id.buttonErase, "点击进入擦除模式", NovaIcons.nova_icon_eraser, true, new OnClickListener() {
            public void onClick(View v) {
                ScreenDisplayActivity.this.m_DrawView.setEraseMode2(true, -16776192);
                ScreenDisplayActivity.mEraseMode = true;
                ScreenDisplayActivity.this.m_DrawView.getPaint().setXfermode(new PorterDuffXfermode(Mode.CLEAR));
                ScreenDisplayActivity.this.m_Handler.post(ScreenDisplayActivity.this.ClearDrawPadRunnable);
            }
        }, "橡皮");
        this.mEmptyWhiteBoardButton = initButton(R.id.buttonClearWhiteBoard, "清空白板上全部内容", FontAwesomeIcons.fa_trash_o, new OnClickListener() {
            public void onClick(View v) {
                TeacherPadApplication.IMThread.SendMessage("ClearWhiteBoard " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                ScreenDisplayActivity.this.m_Handler.post(ScreenDisplayActivity.this.ClearDrawPadRunnable);
                ScreenDisplayActivity.this.sendWhiteBoardInfo();
            }
        }, "清空");
        this.mPenColorButton = initButton(R.id.buttonColor, "选择画笔颜色和粗细", NovaIcons.nova_icon_paint_palette, new OnClickListener() {
            public void onClick(View v) {
                if (ScreenDisplayActivity.mEraseMode) {
                    ScreenDisplayActivity.this.m_DrawView.setEraseMode2(false, -16776192);
                    ScreenDisplayActivity.mEraseMode = false;
                    ScreenDisplayActivity.this.m_DrawView.setBrushMode(true);
                }
                PencialPopupWindow colorWindow = new PencialPopupWindow(ScreenDisplayActivity.this, ScreenDisplayActivity.this.m_DrawView.getPenWidth(), ScreenDisplayActivity.this.m_DrawView.getColor());
                colorWindow.setCallBack(new OnChangeCallBack() {
                    public void onDataChanged(int nWidth, int nColor) {
                        ScreenDisplayActivity.this.m_DrawView.setColor(nColor);
                        ScreenDisplayActivity.this.m_DrawView.changeWidth(nWidth);
                        ScreenDisplayActivity.mnPenWidth = nWidth;
                        ScreenDisplayActivity.mnPenColor = nColor;
                        ScreenDisplayActivity.this.sendWhiteBoardInfo();
                    }
                });
                colorWindow.show(v);
            }
        }, "颜色");
        this.m_DrawView.setKeepScreenOn(false);
        Utilities.runOnUIThread(this.m_Context, new Runnable() {
            public void run() {
                ScreenDisplayActivity.this.DoBestFit();
            }
        });
        this.m_Handler.postDelayed(this.mUpdateWhiteBoardStateRunnable, 300);
        TeacherPadApplication.IMThread.forgetData();
    }

    public void clearWhiteBoard() {
        TeacherPadApplication.IMThread.SendMessage("ClearWhiteBoard " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        this.m_Handler.post(this.ClearDrawPadRunnable);
    }

    public void sendWhiteBoardInfo() {
        if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_EXPERMENTAL)) {
            TeacherPadApplication.IMThread.SendMessage("WhiteBoardData: " + this.m_DrawView.getInfoAsString() + " " + String.valueOf(TeacherPadApplication.mActiveScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        }
    }

    public boolean isSideMenuOpened() {
        return this.mbSideMenuExpanded;
    }

    public void switchBottomControls() {
        if (this.mBottomLayoutControls.getVisibility() == 0) {
            Utilities.sliderAndHideFromTopToBottom(this.mBottomLayoutControls, 200);
            this.mBottomLayoutControls.setVisibility(8);
            return;
        }
        this.mBottomLayoutControls.setVisibility(0);
        Utilities.sliderFromBottomToTop(this.mBottomLayoutControls, 200);
    }

    private boolean DoBestFit() {
        boolean bResult = false;
        Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        RelativeLayout layoutFullScreen = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        int nDisplayWidth = layoutFullScreen.getWidth();
        int nDisplayHeight = layoutFullScreen.getHeight();
        if (nDisplayWidth == 0 || nDisplayHeight == 0) {
            return false;
        }
        float fScale = Math.min(((float) nDisplayHeight) / ((float) this.m_nHeight), ((float) nDisplayWidth) / ((float) this.m_nWidth));
        this.m_DrawView.setScale(fScale);
        int nLeftMargin = (int) ((((float) nDisplayWidth) - (((float) this.m_nWidth) * fScale)) / 2.0f);
        int nHeight = (int) (((float) this.m_nHeight) * fScale);
        int nWidth = (int) (((float) this.m_nWidth) * fScale);
        LayoutParams Param = (LayoutParams) this.m_DrawView.getLayoutParams();
        if (!(Param.topMargin == ((int) ((((float) nDisplayHeight) - (((float) this.m_nHeight) * fScale)) / 2.0f)) && Param.leftMargin == nLeftMargin && Param.height == nHeight && Param.width == nWidth)) {
            bResult = true;
        }
        Param.topMargin = (int) ((((float) nDisplayHeight) - (((float) this.m_nHeight) * fScale)) / 2.0f);
        Param.leftMargin = (int) ((((float) nDisplayWidth) - (((float) this.m_nWidth) * fScale)) / 2.0f);
        Param.height = (int) (((float) this.m_nHeight) * fScale);
        Param.width = (int) (((float) this.m_nWidth) * fScale);
        this.m_DrawView.setLayoutParams(Param);
        Param = (LayoutParams) this.m_SurfaceView.getLayoutParams();
        Param.topMargin = (int) ((((float) nDisplayHeight) - (((float) this.m_nHeight) * fScale)) / 2.0f);
        Param.leftMargin = (int) ((((float) nDisplayWidth) - (((float) this.m_nWidth) * fScale)) / 2.0f);
        Param.height = (int) (((float) this.m_nHeight) * fScale);
        Param.width = (int) (((float) this.m_nWidth) * fScale);
        this.m_SurfaceView.setLayoutParams(Param);
        this.m_SurfaceView.resizeVideo((int) (((float) this.m_nWidth) * fScale), (int) (((float) this.m_nHeight) * fScale));
        Param = (LayoutParams) this.m_MultiScreenView.getLayoutParams();
        Param.topMargin = (int) ((((float) nDisplayHeight) - (((float) this.m_nHeight) * fScale)) / 2.0f);
        Param.leftMargin = (int) ((((float) nDisplayWidth) - (((float) this.m_nWidth) * fScale)) / 2.0f);
        Param.height = (int) (((float) this.m_nHeight) * fScale);
        Param.width = (int) (((float) this.m_nWidth) * fScale);
        this.m_MultiScreenView.setLayoutParams(Param);
        this.m_MultiScreenView.setScale(fScale);
        return bResult;
    }

    protected void onDestroy() {
        super.onDestroy();
        this.m_Handler.removeCallbacks(this.ClearDrawPadRunnable);
        Utilities.unbindDrawables(findViewById(R.id.RelativeLayout1));
        this.m_DrawView.setBackgroundDrawable(null);
        this.m_DrawView.clear();
        if (this.m_MJpegDisplayThread != null) {
            this.mbNoMJpegErrorDisplay = true;
            this.m_MJpegDisplayThread.stopDisplay();
            this.m_MJpegDisplayThread = null;
        }
    }

    public void cleanLocalDrawView() {
        this.m_Handler.removeCallbacks(this.ClearDrawPadRunnable);
        if (!mCursorMode) {
            this.m_Handler.post(this.SendDrawPadDataRunnable);
        }
        this.m_Handler.post(this.ClearDrawPadRunnable);
    }

    protected void onPause() {
        this.m_Handler.removeCallbacks(this.GetPlayPosRunable);
        this.m_Handler.removeCallbacks(this.mTimerIMStateRunnable);
        this.m_Handler.removeCallbacks(this.BuildDisplayThreadRunnable);
        this.mbNoDisplay = true;
        this.mbNoMJpegErrorDisplay = true;
        this.m_DrawView.setBackgroundDrawable(null);
        if (this.m_MJpegDisplayThread != null) {
            this.m_MJpegDisplayThread.stopDisplay();
            this.m_MJpegDisplayThread = null;
        }
        if (UI.ScreenJpegServer != null) {
            UI.ScreenJpegServer.setPause(false);
        }
        Utilities.unbindDrawables(this.m_DrawView);
        super.onPause();
    }

    protected void onResume() {
        this.m_Handler.postDelayed(this.GetPlayPosRunable, 500);
        this.m_Handler.postDelayed(this.mTimerIMStateRunnable, 1000);
        this.mbNoDisplay = false;
        this.mbNoMJpegErrorDisplay = false;
        if (!this.m_szMJpegServer.isEmpty()) {
            this.m_Handler.postDelayed(this.BuildDisplayThreadRunnable, 300);
        }
        if (UI.ScreenJpegServer != null) {
            UI.ScreenJpegServer.setPause(true);
        }
        super.onResume();
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
            Param = (LayoutParams) this.m_MultiScreenView.getLayoutParams();
            Param.topMargin--;
            this.m_MultiScreenView.setLayoutParams(Param);
        }
        switch (event.getActionMasked()) {
            case 0:
                this.m_bTouchOn = true;
                break;
            case 1:
            case 6:
                this.m_bTouchOn = false;
                this.m_TouchStartPoint = null;
                break;
            case 2:
                this.m_bTouchOn = true;
                if (this.m_TouchStartPoint != null) {
                    PointF CurrentCenterPoint = new PointF();
                    getMidPoint(CurrentCenterPoint, event);
                    Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
                    float fCurrentDistance = getPointSpacing(event);
                    float fScale = (fCurrentDistance - this.m_fLastDistance) / fCurrentDistance;
                    float fTotalScale = fScale + this.m_fLastScale;
                    if (((double) fTotalScale) < 0.1d) {
                        this.m_DrawView.setScale(0.1f);
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
                    Param2 = (LayoutParams) this.m_MultiScreenView.getLayoutParams();
                    Param2.topMargin = nNewTopMargin;
                    Param2.leftMargin = nNewLeftMargin;
                    Param2.width = (int) fNewWidth;
                    Param2.height = (int) fNewHeight;
                    this.m_MultiScreenView.setLayoutParams(Param2);
                    this.m_MultiScreenView.setScale(this.m_DrawView.getScale());
                    break;
                }
                break;
            case 5:
                this.m_bTouchOn = true;
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

    public void onClick(View v) {
    }

    public void onBackPressed() {
        finish();
    }

    public void OnNewMJpegInstance(MJpegDisplayThread NewThread) {
        this.m_MJpegDisplayThread = NewThread;
    }

    public void OnTouchDown() {
        this.m_bTouchOn = true;
        this.m_Handler.removeCallbacks(this.ClearDrawPadRunnable);
    }

    public void OnTouchUp() {
        this.m_bTouchOn = false;
        if (!mCursorMode) {
            this.m_Handler.post(this.SendDrawPadDataRunnable);
        }
        this.m_Handler.postDelayed(this.ClearDrawPadRunnable, 1000);
    }

    public void OnPenButtonDown() {
        boolean z = true;
        if (TeacherPadApplication.bPenButtonSwitch) {
            if (mCursorMode) {
                z = false;
            }
            mCursorMode = z;
        } else {
            mCursorMode = true;
        }
        this.m_Handler.postDelayed(this.mUpdateWhiteBoardStateRunnable, 100);
    }

    public void OnPenButtonUp() {
        if (!TeacherPadApplication.bPenButtonSwitch) {
            mCursorMode = false;
            this.m_Handler.postDelayed(this.mUpdateWhiteBoardStateRunnable, 100);
        }
    }

    private void updateCornerButtonState() {
        int nButtonID;
        Icon buttonIcon;
        if (mCursorMode) {
            nButtonID = R.id.buttonMouse;
            buttonIcon = FontAwesomeIcons.fa_mouse_pointer;
        } else if (mEraseMode) {
            nButtonID = R.id.buttonErase;
            buttonIcon = NovaIcons.nova_icon_eraser;
        } else {
            nButtonID = R.id.buttonWhiteBoard;
            buttonIcon = FontAwesomeIcons.fa_pencil_square_o;
        }
        if (this.m_MultiScreenView != null) {
            this.m_MultiScreenView.setMonitorActiveButton(nButtonID, this.m_MultiScreenView.getActiveScreenID(), new IconDrawable((Context) this, buttonIcon).color(-1).actionBarSize());
        }
    }

    public void setCursorMode(boolean bOn) {
        if (mCursorMode != bOn) {
            mCursorMode = bOn;
            this.m_Handler.postDelayed(this.mUpdateWhiteBoardStateRunnable, 100);
        }
        updateCornerButtonState();
    }

    public static boolean getCursorMode() {
        return mCursorMode;
    }

    public void setPenWidth(int nWidth) {
        this.m_DrawView.changeWidth(nWidth);
        mnPenWidth = nWidth;
    }

    public int getPenWidth() {
        return mnPenWidth;
    }

    public void setPenColor(int nColor) {
        this.m_DrawView.setColor(nColor);
        mnPenColor = nColor;
    }

    public int getPenColor() {
        return mnPenColor;
    }

    public void setPenMode(boolean bOn) {
        mCursorMode = false;
        if (bOn) {
            mEraseMode = false;
            this.m_DrawView.setEraseMode2(false, -16776192);
            this.m_DrawView.setBrushMode(true);
            this.m_DrawView.setColor(getPenColor());
            this.m_DrawView.changeWidth(getPenWidth());
        } else {
            mEraseMode = true;
            this.m_DrawView.setEraseMode2(true, -16776192);
            this.m_DrawView.getPaint().setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            this.m_Handler.post(this.ClearDrawPadRunnable);
        }
        this.m_Handler.postDelayed(this.mUpdateWhiteBoardStateRunnable, 100);
        updateCornerButtonState();
    }

    public static boolean getPenMode() {
        if (mEraseMode) {
            return false;
        }
        return true;
    }

    public void OnTouchPen() {
    }

    public void OnTouchFinger() {
    }

    public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
        if (TeacherPadApplication.IMThread == null) {
            return;
        }
        if (mCursorMode || szAction.equalsIgnoreCase("CursorMove")) {
            String szData = String.format("%s %f %f %d %d", new Object[]{szAction, Float.valueOf(fX), Float.valueOf(fY), Integer.valueOf(nWidth), Integer.valueOf(nHeight)});
            TeacherPadApplication.IMThread.SendMessage(szData, MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            Log.d(TAG, szData);
            if (this.m_bTouchOn && !mCursorMode && szAction.equalsIgnoreCase("CursorMove") && System.currentTimeMillis() - this.mnLastPointerTime > 1000) {
                this.m_Handler.post(this.SendDrawPadDataRunnable);
            }
        }
    }

    public static void showDesktop() {
        if (TeacherPadApplication.IMThread != null) {
            TeacherPadApplication.IMThread.SendMessage("ShowDesktop", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        }
    }

    public static void startCamera() {
        CameraCaptureActivity.setCallBack(new CameraCaptureCallBack() {
            public void onCaptureComplete(final String szFileName) {
                try {
                    final String szUploadName = "TeacherCapture_" + Utilities.createGUID() + ".jpg";
                    PutTemporaryStorageItemObject CallItem = new PutTemporaryStorageItemObject(TeacherPadApplication.getMJpegURL() + "/" + szUploadName, null);
                    CallItem.setSuccessListener(new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            ScreenDisplayActivity.sendCameraImageDisplayCommand(szUploadName);
                        }
                    });
                    CallItem.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                            ScreenDisplayActivity.uploadCameraImageToServer(Utilities.loadBitmapFromFile(szFileName), szUploadName);
                        }
                    });
                    CallItem.setSourceFileName(szFileName);
                    CallItem.setAlwaysActiveCallbacks(true);
                    CallItem.setTimeout(2000);
                    VirtualNetworkObject.executeNow(CallItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Context context = MyiBaseApplication.getBaseAppContext();
        if (UI.getCurrentActivity() != null) {
            context = UI.getCurrentActivity();
        }
        Intent newIntent = new Intent(context, CameraCaptureActivity.class);
        if (UI.getCurrentActivity() == null) {
            newIntent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        }
        context.startActivity(newIntent);
    }

    private static void uploadCameraImageToServer(Bitmap bitmap, String szUploadName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 75, baos);
        String encodedImage = Base64.encodeToString(baos.toByteArray(), 0);
        byte[] byteArrayImage = null;
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", null);
        CallItem.setSuccessListener(new AnonymousClass19(szUploadName));
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(MyiBaseApplication.getBaseAppContext(), "保存图片失败", "图片保存出现问题");
            }
        });
        CallItem.setParam("lpszBase64Data", encodedImage);
        CallItem.setParam("szKey", szUploadName);
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    private static void sendCameraImageDisplayCommand(String szUploadName) {
        String szGUID = Utilities.createGUID();
        TeacherPadApplication.IMThread.sendMessageToMonitor("AddCameraImageAndDisplay: " + szGUID + " " + szUploadName, false);
        ResourceItemData itemData = new ResourceItemData();
        itemData.szGUID = szGUID;
        itemData.nType = 1;
        itemData.szTitle = "上课拍照";
        TeacherPadApplication.arrResourceData.add(itemData);
        ResourcesListFragment.refrehResourceAdapter();
    }

    public void OnBeforeDraw(DrawView drawView) {
        if (mEraseMode) {
            this.m_DrawView.getPaint().setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        }
    }

    public void OnMJpegError(final String szMessage) {
        this.mszJpegMessage = "";
        Utilities.runOnUIThread(this, new Runnable() {
            public void run() {
                if (ScreenDisplayActivity.this.mbNoMJpegErrorDisplay) {
                    ScreenDisplayActivity.this.mInfoTextView.setVisibility(4);
                    return;
                }
                ScreenDisplayActivity.this.mInfoTextView.setText("和睿易通的大屏幕链接出现中断，如果当前睿易通还在上课模式中正常运行，\n则有可能是网络问题引起，程序会自动重连。\n\n如果当前睿易通已经退出上课模式或已经登出，请手动退出睿易派。\n\n错误信息：\n" + szMessage);
                ScreenDisplayActivity.this.mInfoTextView.setVisibility(0);
            }
        });
    }

    public void OnMJpegMessage(String szMessage) {
        this.mszJpegMessage = szMessage;
    }

    public void OnTouchMove() {
    }

    public void onClickInCorner(int nScreenID, Rect rectButton) {
        if (this.mLayoutControls.getVisibility() == 0) {
            this.mLayoutControls.setVisibility(8);
            return;
        }
        this.mLayoutControls.setVisibility(0);
        displayMonitorTools(nScreenID);
        LayoutParams params = (LayoutParams) this.mLayoutControls.getLayoutParams();
        LayoutParams params2 = (LayoutParams) this.m_MultiScreenView.getLayoutParams();
        params.leftMargin = rectButton.right + params2.leftMargin;
        params.topMargin = rectButton.top + params2.topMargin;
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f);
        anim.setDuration(100);
        this.mLayoutControls.startAnimation(anim);
    }

    public void onRepositionTools(Rect rectButton) {
        LayoutParams params = (LayoutParams) this.mLayoutControls.getLayoutParams();
        LayoutParams params2 = (LayoutParams) this.m_MultiScreenView.getLayoutParams();
        params.leftMargin = rectButton.right + params2.leftMargin;
        params.topMargin = rectButton.top + params2.topMargin;
    }

    private void setButtonState(ImageView button, int nViewState) {
        ((View) button.getParent()).setVisibility(nViewState);
    }

    private void displayMonitorTools(int nScreenID) {
        if (nScreenID >= 0 && nScreenID < TeacherPadApplication.marrMonitors.size()) {
            MultiScreen screen = (MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID);
            if (screen.bMaximized) {
                setButtonState(this.mMaximizeButton, 8);
                setButtonState(this.mRestoreButton, 0);
            } else {
                setButtonState(this.mMaximizeButton, 0);
                setButtonState(this.mRestoreButton, 8);
            }
            if (TeacherPadApplication.marrMonitors.size() == 1) {
                setButtonState(this.mMaximizeButton, 8);
                setButtonState(this.mRestoreButton, 8);
            }
            if (screen.arrPlayStackFlags.size() > 0) {
                int nTopResourceFlags = ((Integer) screen.arrPlayStackFlags.get(0)).intValue();
                setButtonState(this.mCloseButton, 0);
                if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTZOOM & nTopResourceFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTZOOM) {
                    setButtonState(this.mZoominButton, 0);
                    setButtonState(this.mZoomoutButton, 0);
                } else {
                    setButtonState(this.mZoominButton, 8);
                    setButtonState(this.mZoomoutButton, 8);
                }
                if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK & nTopResourceFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTSEEK) {
                    setButtonState(this.mNextButton, 0);
                    setButtonState(this.mPrevButton, 0);
                } else {
                    setButtonState(this.mNextButton, 8);
                    setButtonState(this.mPrevButton, 8);
                }
                if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP & nTopResourceFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP) {
                    setButtonState(this.mPlayButton, 0);
                    setButtonState(this.mStopButton, 0);
                    if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_PLAYING & nTopResourceFlags) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_PLAYING) {
                        setButtonState(this.mPlayButton, 8);
                        setButtonState(this.mStopButton, 0);
                    } else {
                        setButtonState(this.mPlayButton, 0);
                        setButtonState(this.mStopButton, 8);
                    }
                } else {
                    setButtonState(this.mPlayButton, 8);
                    setButtonState(this.mStopButton, 8);
                }
                int i = StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPAN;
                i = StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPAN;
            } else {
                setButtonState(this.mCloseButton, 8);
                setButtonState(this.mZoominButton, 8);
                setButtonState(this.mZoomoutButton, 8);
                setButtonState(this.mMaximizeButton, 8);
                setButtonState(this.mRestoreButton, 8);
                setButtonState(this.mPlayButton, 8);
                setButtonState(this.mStopButton, 8);
                setButtonState(this.mNextButton, 8);
                setButtonState(this.mPrevButton, 8);
            }
            hideButtonFromSettings();
        }
    }

    public boolean hasButton(int nButtonID) {
        String szName;
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this);
        String[] arrDefaultButtons = getResources().getStringArray(R.array.startclass_pcscreen_icons_default);
        Set<String> set = new HashSet();
        for (Object add : arrDefaultButtons) {
            set.add(add);
        }
        ArrayList arrVisibleButtons = new ArrayList(Settings.getStringSet("PCVisibleButtons", set));
        try {
            szName = MyiBaseApplication.getBaseAppContext().getResources().getResourceEntryName(nButtonID);
        } catch (NotFoundException e) {
            szName = "Invalid ID";
        }
        if (Utilities.isInArray(arrVisibleButtons, szName)) {
            return true;
        }
        return false;
    }

    private void hideButtonFromSettings() {
        int i;
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this);
        String[] arrDefaultButtons = getResources().getStringArray(R.array.startclass_pcscreen_icons_default);
        Set<String> set = new HashSet();
        for (Object add : arrDefaultButtons) {
            set.add(add);
        }
        ArrayList arrVisibleButtons = new ArrayList(Settings.getStringSet("PCVisibleButtons", set));
        for (i = 0; i < this.mBottomLayoutControls.getChildCount(); i++) {
            View oneView = this.mBottomLayoutControls.getChildAt(i);
            if (oneView instanceof RelativeLayout) {
                oneView = ((RelativeLayout) oneView).getChildAt(0);
            }
            if (oneView instanceof ImageView) {
                ImageView oneButton = (ImageView) oneView;
                boolean bFound = false;
                String szName = "";
                try {
                    szName = MyiBaseApplication.getBaseAppContext().getResources().getResourceEntryName(oneButton.getId());
                } catch (NotFoundException e) {
                    szName = "Invalid ID";
                }
                if (Utilities.isInArray(arrVisibleButtons, szName)) {
                    bFound = true;
                }
                if (!bFound) {
                    setButtonState(oneButton, 8);
                }
            }
        }
    }

    public void onActiveScreen(int nScreenID) {
        TeacherPadApplication.mActiveScreenID = nScreenID;
        displayMonitorTools(TeacherPadApplication.mActiveScreenID);
        if (this.mLayoutControls.getVisibility() == 0) {
            this.mLayoutControls.setVisibility(8);
        }
    }

    public void onClickSomeWhere() {
        switchBottomControls();
    }

    public boolean updateSideMenu() {
        Log.d(TAG, "updateSideMenu");
        if (this.mDrawerLayout.isDrawerOpen(3)) {
            reloadSideMenuContent();
        }
        return true;
    }

    public void reloadSideMenuContent() {
        if (!(this.mViewPager.getCurrentItem() == 0 && this.mbSideMenuExpanded)) {
            int nScreenID = TeacherPadApplication.mActiveScreenID;
            if (nScreenID >= 0 && nScreenID < TeacherPadApplication.marrMonitors.size()) {
                MultiScreen oneScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID);
                if (oneScreen.arrPlayStack.size() > 0) {
                    String szCurrentTopResource = (String) oneScreen.arrPlayStack.get(0);
                    for (int i = 0; i < TeacherPadApplication.arrResourceData.size(); i++) {
                        ResourceItemData oneItem = (ResourceItemData) TeacherPadApplication.arrResourceData.get(i);
                        if (oneItem.szGUID.equalsIgnoreCase(szCurrentTopResource)) {
                            if (oneItem.arrThumbnailUrls == null || oneItem.arrThumbnailUrls.size() <= 0) {
                                this.mPDFThumbnailFragment.setImageListUrls(null);
                                this.mPDFThumbnailFragment.setResourceItem(null);
                            } else {
                                this.mPDFThumbnailFragment.setImageListUrls(oneItem.arrThumbnailUrls);
                                this.mPDFThumbnailFragment.setResourceItem(oneItem);
                            }
                        }
                    }
                }
            }
        }
        this.mScreenResourceFragment.refresh();
    }

    public void updateDisplay() {
        Log.d(TAG, "updateDisplay");
        this.m_MultiScreenView.invalidate();
        if (TeacherPadApplication.mActiveScreenID >= 0) {
            displayMonitorTools(TeacherPadApplication.mActiveScreenID);
        }
        updateSideMenu();
        this.mbNeedUpdate = false;
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (TeacherPadApplication.marrMonitors.size() > 0) {
            if (!String.valueOf(position).equalsIgnoreCase(TeacherPadApplication.mszActiveMonitorLayoutTags)) {
                this.m_MultiScreenView.splitScreen(this.mScreenSelectAdapter.getScreenData(position), String.valueOf(position));
            }
            this.mbNeedUpdate = true;
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void onTabReselected(Tab arg0) {
    }

    public void onTabUnselected(Tab arg0) {
    }

    public void onTabSelected(Tab arg0) {
        this.mViewPager.setCurrentItem(arg0.getPosition());
    }
}
