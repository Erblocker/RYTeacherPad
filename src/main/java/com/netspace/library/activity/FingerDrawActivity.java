package com.netspace.library.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.NovaIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.bluetooth.BlueToothPen;
import com.netspace.library.bluetooth.BlueToothPen.PenActionInterface;
import com.netspace.library.components.DrawComponent;
import com.netspace.library.components.DrawComponent.DrawComponentGraphic;
import com.netspace.library.controls.CustomGraphicCanvas;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.controls.FriendlyPoint;
import com.netspace.library.controls.MoveableObject;
import com.netspace.library.controls.Point;
import com.netspace.library.dialog.ColorPickerDialog;
import com.netspace.library.dialog.ColorPickerDialog.OnColorChangedListener;
import com.netspace.library.servers.MJpegDisplayThread;
import com.netspace.library.servers.MJpegDisplayThread.MJpegCallInterface;
import com.netspace.library.servers.MJpegDisplayThread.MJpegFrameData;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.window.ChatWindow;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import eu.janmuller.android.simplecropimage.CropImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import net.sourceforge.opencamera.MainActivity;
import org.apache.http.HttpStatus;
import wei.mark.standout.StandOutWindow;

public class FingerDrawActivity extends BaseActivity implements OnClickListener, MJpegCallInterface, DrawViewActionInterface, PenActionInterface, OnHoverListener {
    private static final int CROP_BIG_PICTURE = 2;
    private static final int TAKE_BIG_PICTURE = 1;
    private static HashMap<String, Boolean> mMapVoteCount = new HashMap();
    private static FingerDrawCallbackInterface m_Interface;
    private static FingerDrawActivity m_LastFingerActivity;
    private static FingerDrawTouchUpInterface m_TouchUpInterface;
    private static FingerDrawVoteCallBack m_VoteCallBack;
    private static ArrayList<String> m_arrPageContent = new ArrayList();
    private static boolean m_bKeepCallback = false;
    private static int m_nDefaultColor = -16777216;
    private static int m_nPageCount = 1;
    private static int m_nPageIndex = 1;
    private static boolean mbClearAllContent = false;
    private static String mszSkipVoteKey = null;
    private View mActiveTooltipView;
    private ImageView mBackPage;
    private BlueToothPen mBlueToothPen;
    private ViewGroup mButtonsView;
    private ImageView mCancelButton;
    private CustomGraphicCanvas mGraphicCanvas;
    private ImageView mGraphicsButton;
    private Runnable mHidePointRunnable = new Runnable() {
        public void run() {
            FingerDrawActivity.this.mPointer.setVisibility(4);
        }
    };
    private int mLastHoverX = 0;
    private int mLastHoverY = 0;
    private Point mLastPoint;
    private Toast mLastToast;
    private View mLastTooltipView;
    private LinearLayout mLayoutPageNav;
    private HashMap<String, String> mMapPageNotes = new HashMap();
    private ImageView mNextPage;
    private ImageView mOKButton;
    private TextView mPageIndex;
    private ImageView mPointer;
    private TextView mQuestionIndex;
    private RelativeLayout mRelativeLayout;
    private Runnable mShowTipRunnable = new Runnable() {
        public void run() {
            if (FingerDrawActivity.this.mActiveTooltipView != FingerDrawActivity.this.mLastTooltipView) {
                if (FingerDrawActivity.this.mLastToast != null) {
                    FingerDrawActivity.this.mLastToast.cancel();
                }
                FingerDrawActivity.this.mLastTooltipView = null;
                FingerDrawActivity.this.mLastToast = null;
                int[] location = new int[2];
                String szText = "";
                FingerDrawActivity.this.mActiveTooltipView.getLocationOnScreen(location);
                if (FingerDrawActivity.this.mLastHoverX <= location[0] || FingerDrawActivity.this.mLastHoverX >= location[0] + FingerDrawActivity.this.mActiveTooltipView.getWidth() || FingerDrawActivity.this.mLastHoverY <= location[1] || FingerDrawActivity.this.mLastHoverY >= location[1] + FingerDrawActivity.this.mActiveTooltipView.getHeight()) {
                    FingerDrawActivity.this.mActiveTooltipView = null;
                    return;
                }
                location[1] = location[1] + 20;
                if (FingerDrawActivity.this.mActiveTooltipView.getTag() != null) {
                    szText = (String) FingerDrawActivity.this.mActiveTooltipView.getTag();
                }
                if (!szText.isEmpty()) {
                    FingerDrawActivity.this.mLastTooltipView = FingerDrawActivity.this.mActiveTooltipView;
                    Toast toast = Toast.makeText(FingerDrawActivity.this.getApplicationContext(), szText, 0);
                    toast.setGravity(51, location[0], location[1]);
                    toast.show();
                    FingerDrawActivity.this.mLastToast = toast;
                }
            }
        }
    };
    private ScrollView mToolsScrollView;
    private Activity m_Activity;
    private ImageView m_BroadcastButton;
    private ImageView m_BrushButton;
    private ImageView m_ButtonBestFit;
    private ImageView m_CameraButton;
    private ImageView m_ColorButton;
    private Context m_Context;
    private DrawView m_DrawView;
    private ImageView m_EraseButton;
    private Handler m_Handler;
    private Uri m_ImageUri;
    private MJpegDisplayThread m_MJpegDisplayThread;
    private Handler m_MJpegMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj != null && (msg.obj instanceof MJpegFrameData)) {
                MJpegFrameData FrameData = msg.obj;
                Bitmap bm = FrameData.bm;
                if (FingerDrawActivity.this.m_nWidth == 1000 && FingerDrawActivity.this.m_nHeight == 1000) {
                    FingerDrawActivity.this.m_nWidth = bm.getWidth();
                    FingerDrawActivity.this.m_nHeight = bm.getHeight();
                    FingerDrawActivity.this.m_DrawView.setSize(FingerDrawActivity.this.m_nWidth, FingerDrawActivity.this.m_nHeight);
                }
                Drawable drawable = FingerDrawActivity.this.m_DrawView.getBackground();
                if (drawable != null && (drawable instanceof BitmapDrawable)) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
                FingerDrawActivity.this.m_DrawView.setBackgroundDrawable(new BitmapDrawable(FingerDrawActivity.this.m_Context.getResources(), bm));
                FrameData.DisplayObject.setFrameHandled();
            }
        }
    };
    private ImageView m_OKButton;
    private ImageView m_PencialButton;
    private ImageView m_RotateButton;
    private ImageView m_TextButton;
    private TextView m_TextView;
    private PointF m_TouchStartPoint;
    private ImageView m_VoteButton;
    private boolean m_bAllowAutoSubmit = false;
    private boolean m_bAllowBroadcast;
    private boolean m_bAllowCamera = false;
    private boolean m_bAllowProject = false;
    private boolean m_bAllowUpload = false;
    private boolean m_bEnableBackButton = false;
    private boolean m_bIsAutoSubmit = false;
    private boolean m_bReadOnly = false;
    private boolean m_bTouchOn = false;
    private float m_fLastDistance = 0.0f;
    private float m_fLastScale = 1.0f;
    private int m_nHeight = 1000;
    private int m_nLastXPos = 0;
    private int m_nLastYPos = 0;
    private int m_nWidth = 1000;
    private String m_szAutoSubmitUser = "";
    private String m_szCameraFileName;
    private String m_szMJpegServer = "";
    private String m_szUploadName = "";
    private boolean mbEnableMultiPage = false;
    private boolean mbFirstCallLoadPage = true;
    private boolean mbHasImageKey = false;
    private boolean mbLockStudentAnswerImageService = false;
    private boolean mbSecondBackExit = false;
    private String mszCurrentImageKey;

    public interface FingerDrawCallbackInterface {
        boolean HasMJpegClients();

        void OnBroadcast(Activity activity);

        void OnDestroy(Activity activity);

        void OnFingerDrawCreate(Activity activity);

        void OnOK(Bitmap bitmap, String str, Activity activity);

        void OnPenAction(String str, float f, float f2, int i, int i2, Activity activity);

        void OnProject(Activity activity);

        void OnUpdateMJpegImage(Bitmap bitmap, Activity activity);
    }

    public interface FingerDrawTouchUpInterface {
        void OnDrawViewReady(DrawView drawView);

        void OnTouchUp(DrawView drawView);
    }

    public interface FingerDrawVoteCallBack {
        void OnUnVote(String str);

        void OnVote(String str);
    }

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    public static void SetCallbackInterface(FingerDrawCallbackInterface Interface) {
        m_Interface = Interface;
    }

    public static void SetTouchUpInterface(FingerDrawTouchUpInterface Interface) {
        m_TouchUpInterface = Interface;
    }

    public static void SetVoteInterface(FingerDrawVoteCallBack VoteCallBack) {
        m_VoteCallBack = VoteCallBack;
    }

    public static void SetSkipVoteKey(String szImageKey) {
        mszSkipVoteKey = szImageKey;
    }

    public static void setKeepCallbacks() {
        m_bKeepCallback = true;
    }

    public static void Close() {
        if (m_LastFingerActivity != null) {
            m_LastFingerActivity.finish();
            m_LastFingerActivity = null;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.setFullScreenWindow(getWindow());
        Utilities.setKeepScreenOn(getWindow());
        this.m_Context = this;
        this.m_Activity = this;
        m_LastFingerActivity = this;
        this.m_Handler = new Handler();
        mbClearAllContent = false;
        setContentView(R.layout.activity_fingerdraw);
        this.mRelativeLayout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        this.mToolsScrollView = (ScrollView) findViewById(R.id.scrollView1);
        this.mPointer = (ImageView) findViewById(R.id.imageViewPointer);
        this.m_DrawView = (DrawView) findViewById(R.id.drawPad);
        this.m_DrawView.setCallback(this);
        this.m_DrawView.setColor(m_nDefaultColor);
        this.m_TextView = (TextView) findViewById(R.id.editText);
        this.m_TextView.setVisibility(4);
        this.mNextPage = (ImageView) findViewById(R.id.imageButtonNext);
        this.mBackPage = (ImageView) findViewById(R.id.imageButtonBack);
        this.mPageIndex = (TextView) findViewById(R.id.textViewPageIndex);
        this.mNextPage.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_arrow_circle_o_right).color(-16777216).actionBarSize());
        this.mBackPage.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_arrow_circle_o_left).color(-16777216).actionBarSize());
        this.mLayoutPageNav = (LinearLayout) findViewById(R.id.layoutNewPage);
        this.mPageIndex.setText(new StringBuilder(String.valueOf(String.valueOf(m_nPageIndex))).append("/").append(String.valueOf(m_nPageCount)).toString());
        this.mNextPage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (FingerDrawActivity.this.mbEnableMultiPage) {
                    if (FingerDrawActivity.m_nPageIndex == FingerDrawActivity.m_nPageCount) {
                        FingerDrawActivity.m_nPageCount = FingerDrawActivity.m_nPageCount + 1;
                        FingerDrawActivity.this.savePageContent(FingerDrawActivity.m_nPageIndex);
                        new File(new StringBuilder(String.valueOf(FingerDrawActivity.this.getCacheDir().getAbsolutePath())).append(String.valueOf(FingerDrawActivity.m_nPageIndex + 1)).append("back.png").toString()).delete();
                        FingerDrawActivity.m_nPageIndex = FingerDrawActivity.m_nPageIndex + 1;
                        FingerDrawActivity.this.loadPageContent(FingerDrawActivity.m_nPageIndex, false);
                    } else {
                        FingerDrawActivity.this.savePageContent(FingerDrawActivity.m_nPageIndex);
                        FingerDrawActivity.m_nPageIndex = FingerDrawActivity.m_nPageIndex + 1;
                        FingerDrawActivity.this.loadPageContent(FingerDrawActivity.m_nPageIndex, false);
                    }
                    FingerDrawActivity.this.mPageIndex.setText(new StringBuilder(String.valueOf(String.valueOf(FingerDrawActivity.m_nPageIndex))).append("/").append(String.valueOf(FingerDrawActivity.m_nPageCount)).toString());
                }
            }
        });
        this.mBackPage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (FingerDrawActivity.this.mbEnableMultiPage) {
                    if (FingerDrawActivity.m_nPageIndex > 1) {
                        FingerDrawActivity.this.savePageContent(FingerDrawActivity.m_nPageIndex);
                        FingerDrawActivity.m_nPageIndex = FingerDrawActivity.m_nPageIndex - 1;
                        FingerDrawActivity.this.loadPageContent(FingerDrawActivity.m_nPageIndex, false);
                    }
                    FingerDrawActivity.this.mPageIndex.setText(new StringBuilder(String.valueOf(String.valueOf(FingerDrawActivity.m_nPageIndex))).append("/").append(String.valueOf(FingerDrawActivity.m_nPageCount)).toString());
                }
            }
        });
        this.mLayoutPageNav.setVisibility(4);
        if (getIntent().getBooleanExtra("multipage", false)) {
            this.mLayoutPageNav.setVisibility(0);
            this.mbEnableMultiPage = true;
        }
        BitmapDrawable background;
        if (getIntent().getExtras().containsKey("imageKey")) {
            String szKey = getIntent().getExtras().getString("imageKey");
            this.mszCurrentImageKey = szKey;
            background = new BitmapDrawable(getResources(), getExternalCacheDir() + "/" + szKey + ".jpg");
            this.m_DrawView.setBackgroundDrawable(background);
            this.m_nWidth = getIntent().getExtras().getInt("imageWidth");
            this.m_nHeight = getIntent().getExtras().getInt("imageHeight");
            if (this.m_nWidth == -1 || this.m_nHeight == -1) {
                this.m_nWidth = background.getBitmap().getWidth();
                this.m_nHeight = background.getBitmap().getHeight();
            }
            this.mbHasImageKey = true;
        } else {
            this.m_DrawView.setBackgroundResource(R.drawable.background_drawpad);
            if (getIntent().getExtras().containsKey("imageWidth") && getIntent().getExtras().containsKey("imageHeight")) {
                this.m_nWidth = getIntent().getExtras().getInt("imageWidth");
                this.m_nHeight = getIntent().getExtras().getInt("imageHeight");
                if (this.m_nWidth == -1 || this.m_nHeight == -1) {
                    background = (BitmapDrawable) this.m_DrawView.getBackground();
                    this.m_nWidth = background.getBitmap().getWidth();
                    this.m_nHeight = background.getBitmap().getHeight();
                }
            }
        }
        if (getIntent().getExtras().containsKey("allowUpload")) {
            this.m_bAllowUpload = getIntent().getExtras().getBoolean("allowUpload");
            this.m_szUploadName = getIntent().getExtras().getString("uploadName");
        }
        if (getIntent().getExtras().containsKey("allowProject")) {
            this.m_bAllowProject = getIntent().getExtras().getBoolean("allowProject");
        }
        this.mQuestionIndex = (TextView) findViewById(R.id.textViewQuestionIndex);
        String szQuestionIndex = getIntent().getExtras().getString("displayText");
        if (szQuestionIndex == null || szQuestionIndex.isEmpty()) {
            this.mQuestionIndex.setVisibility(8);
        } else {
            this.mQuestionIndex.setText(szQuestionIndex);
            this.mQuestionIndex.setVisibility(0);
        }
        this.m_CameraButton = (ImageView) findViewById(R.id.buttonCamera);
        this.m_CameraButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_camera_2).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        if (getIntent().getExtras().containsKey("allowCamera")) {
            this.m_bAllowCamera = getIntent().getExtras().getBoolean("allowCamera");
        }
        if (getIntent().getExtras().containsKey("enableBackButton")) {
            this.m_bEnableBackButton = getIntent().getExtras().getBoolean("enableBackButton");
        }
        if (getIntent().getExtras().containsKey("readonly")) {
            this.m_bReadOnly = getIntent().getExtras().getBoolean("readonly");
        }
        if (this.m_bAllowCamera) {
            this.m_CameraButton.setOnClickListener(this);
        } else {
            this.m_CameraButton.setVisibility(8);
        }
        this.m_BroadcastButton = (ImageView) findViewById(R.id.buttonBroadcast);
        this.m_BroadcastButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_share_signal_tower).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        if (getIntent().getExtras().containsKey("allowBroadcast")) {
            this.m_bAllowBroadcast = getIntent().getExtras().getBoolean("allowBroadcast");
        }
        if (this.m_bAllowBroadcast) {
            this.m_BroadcastButton.setOnClickListener(this);
        } else {
            this.m_BroadcastButton.setVisibility(8);
        }
        if (getIntent().getExtras().containsKey("imageData")) {
            this.m_DrawView.fromString(getIntent().getExtras().getString("imageData"));
        }
        if (getIntent().getExtras().containsKey("MJpegServer")) {
            this.m_szMJpegServer = getIntent().getExtras().getString("MJpegServer");
        }
        if (getIntent().hasExtra("allowautoupload")) {
            this.m_bAllowAutoSubmit = getIntent().getBooleanExtra("allowautoupload", false);
            this.m_szAutoSubmitUser = getIntent().getStringExtra("uploadcommandusername");
        }
        this.m_OKButton = (ImageView) findViewById(R.id.buttonOk);
        this.m_OKButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_check_bubble).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        if (this.m_bAllowUpload) {
            this.m_OKButton.setOnClickListener(this);
        } else {
            this.m_OKButton.setVisibility(8);
        }
        if (this.m_bAllowProject) {
            ImageView projectButton = (ImageView) findViewById(R.id.buttonProjector);
            projectButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_tv).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
            projectButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Utilities.logClick(v);
                    if (FingerDrawActivity.m_Interface != null) {
                        FingerDrawActivity.m_Interface.OnProject(FingerDrawActivity.this.m_Activity);
                        v.setSelected(true);
                    }
                }
            });
        } else {
            findViewById(R.id.buttonProjector).setVisibility(8);
        }
        this.m_DrawView.setSize(this.m_nWidth, this.m_nHeight);
        this.m_DrawView.setEnableCache(true);
        this.m_TextButton = (ImageView) findViewById(R.id.buttonText);
        this.m_TextButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_text_input_1).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.m_TextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (v.isSelected()) {
                    FingerDrawActivity.this.m_TextButton.setSelected(false);
                    FingerDrawActivity.this.m_TextView.setVisibility(4);
                    return;
                }
                FingerDrawActivity.this.m_TextButton.setSelected(true);
                FingerDrawActivity.this.m_TextView.setVisibility(0);
                FingerDrawActivity.this.m_TextView.requestFocus();
                FingerDrawActivity.this.enableGraphic(true);
            }
        });
        this.m_TextView.setOnTouchListener(new MoveableObject(this, null));
        this.m_PencialButton = (ImageView) findViewById(R.id.buttonPencial);
        this.m_PencialButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_pencil_3).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.m_PencialButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (FingerDrawActivity.this.m_PencialButton.isSelected()) {
                    FingerDrawActivity.this.m_PencialButton.setSelected(false);
                    FingerDrawActivity.this.m_EraseButton.setSelected(false);
                    FingerDrawActivity.this.m_BrushButton.setSelected(false);
                    FingerDrawActivity.this.m_DrawView.setBrushMode(false);
                    return;
                }
                FingerDrawActivity.this.m_PencialButton.setSelected(true);
                FingerDrawActivity.this.m_EraseButton.setSelected(false);
                FingerDrawActivity.this.m_BrushButton.setSelected(false);
                FingerDrawActivity.this.m_DrawView.setEraseMode(false);
                FingerDrawActivity.this.m_DrawView.setBrushMode(true);
                FingerDrawActivity.this.m_DrawView.changeWidth(3);
                FingerDrawActivity.this.enableGraphic(true);
            }
        });
        this.m_VoteButton = (ImageView) findViewById(R.id.buttonVote);
        this.m_VoteButton.setImageDrawable(new IconDrawable((Context) this, FontAwesomeIcons.fa_thumbs_up).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.m_VoteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (FingerDrawActivity.mszSkipVoteKey != null && FingerDrawActivity.this.mszCurrentImageKey.equalsIgnoreCase(FingerDrawActivity.mszSkipVoteKey)) {
                    Utilities.showAlertMessage(FingerDrawActivity.this, "无法点赞", "您不能给自己的作答点赞");
                } else if (v.isSelected()) {
                    v.setSelected(false);
                    if (FingerDrawActivity.this.mszCurrentImageKey != null) {
                        FingerDrawActivity.mMapVoteCount.remove(FingerDrawActivity.this.mszCurrentImageKey);
                        if (FingerDrawActivity.m_VoteCallBack != null) {
                            FingerDrawActivity.m_VoteCallBack.OnUnVote(FingerDrawActivity.this.mszCurrentImageKey);
                        }
                    }
                } else {
                    v.setSelected(true);
                    if (FingerDrawActivity.this.mszCurrentImageKey != null) {
                        FingerDrawActivity.mMapVoteCount.put(FingerDrawActivity.this.mszCurrentImageKey, Boolean.valueOf(true));
                        if (FingerDrawActivity.m_VoteCallBack != null) {
                            FingerDrawActivity.m_VoteCallBack.OnVote(FingerDrawActivity.this.mszCurrentImageKey);
                        }
                    }
                }
            }
        });
        if (getIntent().getBooleanExtra("allowvote", false)) {
            this.m_VoteButton.setVisibility(0);
        } else {
            this.m_VoteButton.setVisibility(8);
        }
        if (getIntent().getBooleanExtra("lockStudentAnswerImageService", false)) {
            this.mbLockStudentAnswerImageService = true;
            lockStudentAnswerImageService();
        }
        this.m_EraseButton = (ImageView) findViewById(R.id.buttonEraser);
        this.m_EraseButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_eraser).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.m_EraseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (v.isSelected()) {
                    FingerDrawActivity.this.m_PencialButton.setSelected(false);
                    FingerDrawActivity.this.m_EraseButton.setSelected(false);
                    FingerDrawActivity.this.m_BrushButton.setSelected(false);
                    FingerDrawActivity.this.m_DrawView.setEraseMode2(false, 0);
                    FingerDrawActivity.this.enableGraphic(true);
                    return;
                }
                FingerDrawActivity.this.m_EraseButton.setSelected(true);
                FingerDrawActivity.this.m_BrushButton.setSelected(false);
                FingerDrawActivity.this.m_PencialButton.setSelected(false);
                FingerDrawActivity.this.m_DrawView.setEraseMode2(true, 0);
                FingerDrawActivity.this.enableGraphic(false);
            }
        });
        this.m_EraseButton.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                Utilities.logClick(v, "onLongClick");
                new Builder(FingerDrawActivity.this.m_Context).setTitle("清除确认").setMessage("是否清除全部内容？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FingerDrawActivity.this.m_DrawView.clearPoints();
                    }
                }).setNegativeButton("否", null).show();
                return false;
            }
        });
        this.m_BrushButton = (ImageView) findViewById(R.id.buttonBrush);
        this.m_BrushButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_paint_brush_1).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.m_BrushButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (v.isSelected()) {
                    FingerDrawActivity.this.m_PencialButton.setSelected(false);
                    FingerDrawActivity.this.m_EraseButton.setSelected(false);
                    FingerDrawActivity.this.m_BrushButton.setSelected(false);
                    FingerDrawActivity.this.m_DrawView.setBrushMode(false);
                    FingerDrawActivity.this.m_DrawView.setEraseMode(false);
                    return;
                }
                FingerDrawActivity.this.m_BrushButton.setSelected(true);
                FingerDrawActivity.this.m_EraseButton.setSelected(false);
                FingerDrawActivity.this.m_PencialButton.setSelected(false);
                FingerDrawActivity.this.m_DrawView.setEraseMode(false);
                FingerDrawActivity.this.m_DrawView.setBrushMode(true);
                FingerDrawActivity.this.m_DrawView.changeWidth(10);
                FingerDrawActivity.this.enableGraphic(true);
            }
        });
        this.m_ColorButton = (ImageView) findViewById(R.id.buttonColorize);
        this.m_ColorButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_paint_palette).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.m_ColorButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                new ColorPickerDialog(FingerDrawActivity.this.m_Context, FingerDrawActivity.this.m_DrawView.getColor(), "选择颜色", new OnColorChangedListener() {
                    public void colorChanged(int color) {
                        FingerDrawActivity.this.m_DrawView.setColor(color);
                    }
                }).show();
            }
        });
        this.m_RotateButton = (ImageView) findViewById(R.id.buttonRotate);
        this.m_RotateButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_mobile_phone_rotate_1).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.m_RotateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (FingerDrawActivity.this.m_DrawView.getBackground() instanceof BitmapDrawable) {
                    BitmapDrawable BitmapBackground = (BitmapDrawable) FingerDrawActivity.this.m_DrawView.getBackground();
                    if (BitmapBackground != null) {
                        Bitmap SourceBitmap = BitmapBackground.getBitmap();
                        if (SourceBitmap != null) {
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90.0f);
                            FingerDrawActivity.this.m_DrawView.setBackgroundDrawable(new BitmapDrawable(FingerDrawActivity.this.getResources(), Bitmap.createBitmap(BitmapBackground.getBitmap(), 0, 0, SourceBitmap.getWidth(), SourceBitmap.getHeight(), matrix, true)));
                            int nTemp = FingerDrawActivity.this.m_nWidth;
                            FingerDrawActivity.this.m_nWidth = FingerDrawActivity.this.m_nHeight;
                            FingerDrawActivity.this.m_nHeight = nTemp;
                            FingerDrawActivity.this.m_DrawView.setSize(FingerDrawActivity.this.m_nWidth, FingerDrawActivity.this.m_nHeight);
                            SourceBitmap.recycle();
                        }
                    }
                }
            }
        });
        this.mGraphicsButton = (ImageView) findViewById(R.id.buttonGraphics);
        this.mGraphicsButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_ruler_2).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mGraphicsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                PopupMenu popup = new PopupMenu(FingerDrawActivity.this, v);
                for (int i = 0; i < DrawComponent.getGraphics().size(); i++) {
                    popup.getMenu().add(0, i, i, ((DrawComponentGraphic) DrawComponent.getGraphics().get(i)).getName());
                }
                popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        DrawComponentGraphic DrawComponentGraphic = (DrawComponentGraphic) DrawComponent.getGraphics().get(item.getItemId());
                        if (FingerDrawActivity.this.mGraphicCanvas != null) {
                            FingerDrawActivity.this.mRelativeLayout.removeView(FingerDrawActivity.this.mGraphicCanvas);
                            FingerDrawActivity.this.mGraphicCanvas = null;
                        }
                        FingerDrawActivity.this.mGraphicCanvas = new CustomGraphicCanvas(FingerDrawActivity.this);
                        FingerDrawActivity.this.mGraphicCanvas.setGraphic(DrawComponentGraphic);
                        FingerDrawActivity.this.mRelativeLayout.addView(FingerDrawActivity.this.mGraphicCanvas, 200, 100);
                        FingerDrawActivity.this.mGraphicCanvas.setBackgroundResource(R.drawable.background_grahpiccanvas);
                        FingerDrawActivity.this.mGraphicCanvas.setDrawView(FingerDrawActivity.this.m_DrawView);
                        DrawComponentGraphic.init(FingerDrawActivity.this.mGraphicCanvas);
                        LayoutParams Params = (LayoutParams) FingerDrawActivity.this.mGraphicCanvas.getLayoutParams();
                        Params.topMargin = Math.max(FingerDrawActivity.this.m_DrawView.getTop(), 0);
                        Params.leftMargin = Math.max(FingerDrawActivity.this.m_DrawView.getLeft(), 0);
                        Params.width = Math.min(FingerDrawActivity.this.m_DrawView.getRight() - Params.leftMargin, Utilities.getScreenWidth(FingerDrawActivity.this.m_Context) - FingerDrawActivity.this.mToolsScrollView.getWidth());
                        Params.height = FingerDrawActivity.this.m_DrawView.getBottom() - Params.topMargin;
                        FingerDrawActivity.this.mOKButton.setVisibility(0);
                        FingerDrawActivity.this.mCancelButton.setVisibility(0);
                        FingerDrawActivity.this.enableOtherButtons(false);
                        return false;
                    }
                });
                popup.show();
            }
        });
        this.mOKButton = (ImageView) findViewById(R.id.buttonGraphicOK);
        this.mOKButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_check_circle_2).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mOKButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (FingerDrawActivity.this.mGraphicCanvas != null) {
                    FingerDrawActivity.this.mGraphicCanvas.measureDataToDrawView();
                    FingerDrawActivity.this.mGraphicCanvas.setVisibility(8);
                    FingerDrawActivity.this.mRelativeLayout.removeView(FingerDrawActivity.this.mGraphicCanvas);
                    FingerDrawActivity.this.mGraphicCanvas = null;
                    FingerDrawActivity.this.mOKButton.setVisibility(8);
                    FingerDrawActivity.this.mCancelButton.setVisibility(8);
                }
                FingerDrawActivity.this.enableOtherButtons(true);
            }
        });
        this.mCancelButton = (ImageView) findViewById(R.id.buttonGraphicCancel);
        this.mCancelButton.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_close_circle).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (FingerDrawActivity.this.mGraphicCanvas != null) {
                    FingerDrawActivity.this.mGraphicCanvas.setVisibility(8);
                    FingerDrawActivity.this.mRelativeLayout.removeView(FingerDrawActivity.this.mGraphicCanvas);
                    FingerDrawActivity.this.mGraphicCanvas = null;
                }
                FingerDrawActivity.this.mOKButton.setVisibility(8);
                FingerDrawActivity.this.mCancelButton.setVisibility(8);
                FingerDrawActivity.this.enableOtherButtons(true);
            }
        });
        this.m_ButtonBestFit = (ImageView) findViewById(R.id.buttonBestFit);
        this.m_ButtonBestFit.setImageDrawable(new IconDrawable((Context) this, NovaIcons.nova_icon_focus_2).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        this.m_ButtonBestFit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                FingerDrawActivity.this.DoBestFit();
            }
        });
        this.m_DrawView.setKeepScreenOn(false);
        if (m_Interface != null) {
            m_Interface.OnFingerDrawCreate(this);
        }
        if (m_TouchUpInterface != null) {
            m_TouchUpInterface.OnDrawViewReady(this.m_DrawView);
        }
        if (this.m_bReadOnly) {
            this.m_PencialButton.setVisibility(8);
            this.m_BrushButton.setVisibility(8);
            this.m_EraseButton.setVisibility(8);
            this.m_TextButton.setVisibility(8);
            this.m_ColorButton.setVisibility(8);
            this.mGraphicsButton.setVisibility(8);
            this.m_CameraButton.setVisibility(8);
        } else {
            this.mBlueToothPen = new BlueToothPen();
            this.mBlueToothPen.setCallBack(this);
            this.m_PencialButton.performClick();
        }
        if (getIntent().getBooleanExtra("multipage", false)) {
            loadPageContent(m_nPageIndex, this.mbHasImageKey);
        }
        this.mButtonsView = (ViewGroup) findViewById(R.id.tools);
        this.mButtonsView.setOnHoverListener(new OnHoverListener() {
            public boolean onHover(View v, MotionEvent event) {
                FingerDrawActivity.this.mLastHoverX = (int) event.getX();
                FingerDrawActivity.this.mLastHoverY = (int) event.getY();
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                FingerDrawActivity fingerDrawActivity = FingerDrawActivity.this;
                fingerDrawActivity.mLastHoverX = fingerDrawActivity.mLastHoverX + location[0];
                fingerDrawActivity = FingerDrawActivity.this;
                fingerDrawActivity.mLastHoverY = fingerDrawActivity.mLastHoverY + location[1];
                return false;
            }
        });
        for (int i = 0; i < this.mButtonsView.getChildCount(); i++) {
            View oneView = this.mButtonsView.getChildAt(i);
            if ((oneView instanceof ImageView) && oneView.getVisibility() == 0) {
                oneView.setOnHoverListener(this);
            }
        }
        DoBestFit();
    }

    public void lockStudentAnswerImageService() {
        if (StudentAnswerImageService.getInstance() != null) {
            StudentAnswerImageService.getInstance().setLock(true);
        }
    }

    public void unLockStudentAnswerImageService() {
        if (StudentAnswerImageService.getInstance() != null) {
            StudentAnswerImageService.getInstance().setLock(false);
        }
    }

    public void switchImage(String szImageKey, String szDisplayTitle) {
        Log.d("FingerDrawActivity", "szImageKey=" + szImageKey);
        if (this.mszCurrentImageKey != null) {
            this.mMapPageNotes.put(this.mszCurrentImageKey, this.m_DrawView.getDataAsString());
        }
        BitmapDrawable background = new BitmapDrawable(getResources(), getExternalCacheDir() + "/" + szImageKey + ".jpg");
        this.m_DrawView.setBackgroundDrawable(background);
        this.m_nWidth = background.getBitmap().getWidth();
        this.m_nHeight = background.getBitmap().getHeight();
        this.mszCurrentImageKey = szImageKey;
        this.m_DrawView.clear();
        if (this.mMapPageNotes.containsKey(szImageKey)) {
            this.m_DrawView.fromString((String) this.mMapPageNotes.get(szImageKey));
        }
        if (mMapVoteCount.containsKey(this.mszCurrentImageKey)) {
            this.m_VoteButton.setSelected(true);
        } else {
            this.m_VoteButton.setSelected(false);
        }
        if (szDisplayTitle != null) {
            this.mQuestionIndex.setText(szDisplayTitle);
        }
        DoBestFit();
    }

    public boolean onHover(View v, MotionEvent event) {
        this.mLastHoverX = (int) event.getX();
        this.mLastHoverY = (int) event.getY();
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        this.mLastHoverX += location[0];
        this.mLastHoverY += location[1];
        if (this.mActiveTooltipView != v) {
            this.mActiveTooltipView = v;
            this.mButtonsView.removeCallbacks(this.mShowTipRunnable);
            this.mButtonsView.postDelayed(this.mShowTipRunnable, 1000);
        }
        return false;
    }

    public static void clearPageContents() {
        for (int nPageIndex = 1; nPageIndex <= m_nPageCount; nPageIndex++) {
            new File(new StringBuilder(String.valueOf(MyiBaseApplication.getBaseAppContext().getCacheDir().getAbsolutePath())).append(String.valueOf(nPageIndex)).append("back.png").toString()).delete();
            new File(new StringBuilder(String.valueOf(MyiBaseApplication.getBaseAppContext().getCacheDir().getAbsolutePath())).append(String.valueOf(nPageIndex)).append(".png").toString()).delete();
        }
        m_nPageCount = 1;
        m_nPageIndex = 1;
        m_arrPageContent.clear();
        mbClearAllContent = true;
    }

    private void savePageContent(int nPageIndex) {
        String szDrawData = this.m_DrawView.getDataAsString();
        String szCacheFileName = new StringBuilder(String.valueOf(getCacheDir().getAbsolutePath())).append(String.valueOf(nPageIndex)).append(".png").toString();
        while (m_arrPageContent.size() <= nPageIndex) {
            m_arrPageContent.add("");
        }
        m_arrPageContent.set(nPageIndex, szDrawData);
        this.m_DrawView.saveCacheToBitmap(szCacheFileName);
        szCacheFileName = new StringBuilder(String.valueOf(getCacheDir().getAbsolutePath())).append(String.valueOf(nPageIndex)).append("back.png").toString();
        new File(szCacheFileName).delete();
        Drawable background = this.m_DrawView.getBackground();
        if (background instanceof BitmapDrawable) {
            Utilities.saveBitmapToPng(szCacheFileName, ((BitmapDrawable) background).getBitmap());
        }
    }

    private void loadPageContent(int nPageIndex, boolean bSkipBackground) {
        String szCacheFileName = new StringBuilder(String.valueOf(getCacheDir().getAbsolutePath())).append(String.valueOf(nPageIndex)).append(".png").toString();
        if (new File(szCacheFileName).exists()) {
            this.m_DrawView.loadCacheFromDisk(szCacheFileName);
        }
        szCacheFileName = new StringBuilder(String.valueOf(getCacheDir().getAbsolutePath())).append(String.valueOf(nPageIndex)).append("back.png").toString();
        if (new File(szCacheFileName).exists()) {
            Bitmap bitmap = Utilities.loadBitmapFromFile(szCacheFileName);
            if (!(bSkipBackground || bitmap == null)) {
                Utilities.setViewBackground(this.m_DrawView, new BitmapDrawable(getResources(), bitmap));
                if (this.mbFirstCallLoadPage && this.m_nWidth == 1000 && this.m_nHeight == 1000) {
                    this.m_nWidth = bitmap.getWidth();
                    this.m_nHeight = bitmap.getHeight();
                    this.m_DrawView.setSize(this.m_nWidth, this.m_nHeight);
                }
            }
        } else if (!bSkipBackground) {
            this.m_DrawView.setBackgroundResource(R.drawable.background_drawpad);
        }
        this.mbFirstCallLoadPage = false;
        this.m_DrawView.clearPoints();
        if (m_arrPageContent.size() > nPageIndex) {
            this.m_DrawView.fromString((String) m_arrPageContent.get(nPageIndex));
        } else {
            this.m_DrawView.cleanCache();
        }
    }

    private void enableGraphic(boolean bEnable) {
        if (bEnable) {
            this.mGraphicsButton.setEnabled(true);
            this.mGraphicsButton.setAlpha(1.0f);
            return;
        }
        this.mGraphicsButton.setEnabled(false);
        this.mGraphicsButton.setAlpha(0.5f);
    }

    private void enableOtherButtons(boolean bEnable) {
        ArrayList<ImageView> arrButtons = new ArrayList();
        arrButtons.add(this.m_BrushButton);
        arrButtons.add(this.m_PencialButton);
        arrButtons.add(this.m_ColorButton);
        arrButtons.add(this.mGraphicsButton);
        arrButtons.add(this.m_EraseButton);
        arrButtons.add(this.m_OKButton);
        arrButtons.add(this.m_TextButton);
        arrButtons.add(this.m_TextButton);
        arrButtons.add(this.m_CameraButton);
        arrButtons.add(this.m_RotateButton);
        arrButtons.add(this.m_ButtonBestFit);
        for (int i = 0; i < arrButtons.size(); i++) {
            ImageView oneButton = (ImageView) arrButtons.get(i);
            if (oneButton.getVisibility() == 0) {
                if (bEnable) {
                    oneButton.setEnabled(true);
                    oneButton.setAlpha(1.0f);
                } else {
                    oneButton.setEnabled(false);
                    oneButton.setAlpha(0.5f);
                }
            }
        }
    }

    private void DoBestFit() {
        Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        int nDisplayWidth = display.getWidth();
        int nDisplayHeight = display.getHeight() - Utilities.getStatusBarHeight(this.m_Context);
        float fScale = 1.0f / Math.max(((float) this.m_nWidth) / ((float) nDisplayWidth), ((float) this.m_nHeight) / ((float) nDisplayHeight));
        this.m_DrawView.setScale(fScale);
        LayoutParams Param = (LayoutParams) this.m_DrawView.getLayoutParams();
        Param.topMargin = (int) ((((float) nDisplayHeight) - (((float) this.m_nHeight) * fScale)) / 2.0f);
        Param.leftMargin = (int) ((((float) nDisplayWidth) - (((float) this.m_nWidth) * fScale)) / 2.0f);
        this.m_DrawView.setLayoutParams(Param);
    }

    protected void onDestroy() {
        if (this.mbEnableMultiPage) {
            if (mbClearAllContent) {
                clearPageContents();
            } else {
                savePageContent(m_nPageIndex);
            }
        }
        super.onDestroy();
        Utilities.unbindDrawables(findViewById(R.id.RelativeLayout1));
        this.m_DrawView.setBackgroundDrawable(null);
        this.m_DrawView.clear();
        if (m_Interface != null) {
            m_Interface.OnDestroy(this);
        }
        if (this.m_MJpegDisplayThread != null) {
            this.m_MJpegDisplayThread.stopDisplay();
            this.m_MJpegDisplayThread = null;
        }
        if (this.mBlueToothPen != null) {
            this.mBlueToothPen.stop();
            this.mBlueToothPen = null;
        }
        if (this.mbLockStudentAnswerImageService) {
            unLockStudentAnswerImageService();
        }
        StandOutWindow.restoreAll(this.m_Context, ChatWindow.class);
        PicassoTools.clearCache(Picasso.with(this));
        clearCallbacks();
    }

    private static void clearCallbacks() {
        if (!m_bKeepCallback) {
            m_Interface = null;
            m_TouchUpInterface = null;
        }
        m_bKeepCallback = false;
    }

    public static void setDefaultColor(int nColor) {
        m_nDefaultColor = nColor;
    }

    protected void onPause() {
        if (this.m_MJpegDisplayThread != null) {
            this.m_MJpegDisplayThread.stopDisplay();
            this.m_MJpegDisplayThread = null;
        }
        if (this.mBlueToothPen != null) {
            this.mBlueToothPen.stop();
        }
        super.onPause();
    }

    protected void onResume() {
        if (!this.m_szMJpegServer.isEmpty()) {
            this.m_MJpegDisplayThread = new MJpegDisplayThread(this.m_Context, this.m_MJpegMessageHandler, 0, this.m_szMJpegServer, this);
            this.m_MJpegDisplayThread.start();
        }
        if (this.mBlueToothPen != null) {
            this.mBlueToothPen.start();
        }
        StandOutWindow.hideAll(this.m_Context, ChatWindow.class);
        Utilities.sliderFromRightToLeft(this.mToolsScrollView, HttpStatus.SC_MULTIPLE_CHOICES);
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
                    LayoutParams Param2 = (LayoutParams) this.m_DrawView.getLayoutParams();
                    Param2.topMargin = (int) (CurrentCenterPoint.y - (((float) this.m_nLastYPos) * this.m_DrawView.getScale()));
                    Param2.leftMargin = nNewLeftMargin;
                    Param2.rightMargin = (int) (((((float) this.m_nWidth) * this.m_DrawView.getScale()) - ((float) display.getWidth())) + ((float) Param2.leftMargin));
                    Param2.bottomMargin = (int) (((((float) this.m_nHeight) * this.m_DrawView.getScale()) - ((float) display.getHeight())) + ((float) Param2.topMargin));
                    this.m_DrawView.setLayoutParams(Param2);
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

    public static String CreateCameraFileName(Context Context) {
        return Context.getExternalCacheDir() + "/camera_" + Utilities.createGUID() + ".jpg";
    }

    public void StartTakePicture() {
        Intent it = new Intent(this.m_Context, MainActivity.class);
        it.setAction("android.media.action.IMAGE_CAPTURE");
        try {
            this.m_szCameraFileName = CreateCameraFileName(this.m_Context);
            this.m_ImageUri = Uri.fromFile(new File(this.m_szCameraFileName));
            it.putExtra("output", this.m_ImageUri);
            it.setFlags(67108864);
            startActivityForResult(it, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent(this.m_Context, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, this.m_szCameraFileName);
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 0);
        intent.putExtra(CropImage.ASPECT_Y, 0);
        intent.putExtra(CropImage.OUTPUT_X, 0);
        intent.putExtra(CropImage.OUTPUT_Y, 0);
        startActivityForResult(intent, requestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            new File(this.m_szCameraFileName).delete();
            this.m_szCameraFileName = "";
        } else if (requestCode == 1) {
            Log.d("TakePicture", "m_ImageUri:" + this.m_ImageUri.toString());
            cropImageUri(this.m_ImageUri, 640, 480, 2);
        } else if (requestCode == 2) {
            this.m_DrawView.setBackgroundDrawable(new BitmapDrawable(getResources(), this.m_szCameraFileName));
            DoBestFit();
        }
    }

    private Bitmap combineImage(Bitmap c, Bitmap s) {
        Bitmap result = Bitmap.createBitmap(Math.max(c.getWidth(), s.getWidth()), c.getHeight() + s.getHeight(), Config.ARGB_8888);
        Canvas comboImage = new Canvas(result);
        comboImage.drawBitmap(c, 0.0f, 0.0f, null);
        comboImage.drawBitmap(s, 0.0f, (float) c.getHeight(), null);
        return result;
    }

    public void onClick(View v) {
        Utilities.logClick(v);
        if (v.getId() == this.m_OKButton.getId()) {
            Bitmap Result = this.m_DrawView.saveToBitmap();
            if (this.m_TextView.getVisibility() == 0) {
                Bitmap textResult = Bitmap.createBitmap(this.m_TextView.getWidth(), this.m_TextView.getHeight(), Config.ARGB_8888);
                this.m_TextView.draw(new Canvas(textResult));
                int nTop = this.m_TextView.getTop();
                new Canvas(Result).drawBitmap(textResult, (float) this.m_TextView.getLeft(), (float) nTop, null);
                textResult.recycle();
            }
            if (m_Interface != null) {
                m_Interface.OnOK(Result, this.m_szUploadName, this);
            }
            Result.recycle();
        } else if (v.getId() == this.m_CameraButton.getId()) {
            StartTakePicture();
        } else if (v.getId() == this.m_BroadcastButton.getId() && m_Interface != null) {
            m_Interface.OnBroadcast(this);
        }
        enableGraphic(true);
    }

    public void onBackPressed() {
        if (this.m_bEnableBackButton) {
            if (this.mbSecondBackExit) {
                super.onBackPressed();
                finish();
                return;
            }
            this.mbSecondBackExit = true;
            Toast.makeText(this, "再按一次返回键退出", 0).show();
            this.m_Handler.postDelayed(new Runnable() {
                public void run() {
                    FingerDrawActivity.this.mbSecondBackExit = false;
                }
            }, 2000);
        } else if (!this.m_bAllowUpload) {
            super.onBackPressed();
            finish();
        }
    }

    public void OnNewMJpegInstance(MJpegDisplayThread NewThread) {
        this.m_MJpegDisplayThread = NewThread;
    }

    public void OnTouchDown() {
        this.m_bTouchOn = true;
    }

    public void OnTouchUp() {
        this.m_bTouchOn = false;
        if (m_TouchUpInterface != null) {
            m_TouchUpInterface.OnTouchUp(this.m_DrawView);
        }
    }

    public void OnPenButtonDown() {
    }

    public void OnPenButtonUp() {
    }

    public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
        if (m_Interface != null) {
            m_Interface.OnPenAction(szAction, fX, fY, nWidth, nHeight, this);
        }
    }

    public void OnTouchPen() {
    }

    public void OnTouchFinger() {
    }

    public void OnMJpegError(String szMessage) {
    }

    public void OnMJpegMessage(String szMessage) {
    }

    public void onPenAction(String szAction, int nX, int nY, float fPressure) {
        boolean bShowPointer = false;
        int nOldX = nX;
        int nOldY = nY;
        if (szAction.equalsIgnoreCase("newpage") && this.mbEnableMultiPage) {
            this.mNextPage.performClick();
        }
        if (szAction.equalsIgnoreCase("write")) {
            if (this.mLastPoint != null) {
                final FriendlyPoint friendlyPoint = new FriendlyPoint((float) nX, (float) nY, this.m_DrawView.getColor(), this.mLastPoint, (int) (3.0f * fPressure));
                getDrawView().post(new Runnable() {
                    public void run() {
                        FingerDrawActivity.this.m_DrawView.setEraseMode(false);
                        FingerDrawActivity.this.m_DrawView.setBrushMode(true);
                        FingerDrawActivity.this.m_DrawView.changeWidth(3);
                        FingerDrawActivity.this.m_DrawView.addPoint(friendlyPoint);
                        FingerDrawActivity.this.m_DrawView.invalidate();
                    }
                });
                this.mLastPoint = friendlyPoint;
            } else {
                final Point Point = new Point((float) nX, (float) nY, this.m_DrawView.getColor(), (int) (3.0f * fPressure));
                getDrawView().post(new Runnable() {
                    public void run() {
                        FingerDrawActivity.this.m_DrawView.setEraseMode(false);
                        FingerDrawActivity.this.m_DrawView.setBrushMode(true);
                        FingerDrawActivity.this.m_DrawView.changeWidth(3);
                        FingerDrawActivity.this.m_DrawView.addPoint(Point);
                        FingerDrawActivity.this.m_DrawView.invalidate();
                    }
                });
                this.mLastPoint = Point;
            }
            bShowPointer = true;
        } else if (szAction.equalsIgnoreCase("move")) {
            bShowPointer = true;
            this.mLastPoint = null;
        } else if (szAction.equalsIgnoreCase("reset")) {
            bShowPointer = false;
            this.mLastPoint = null;
        }
        if (bShowPointer) {
            final int nPointerX = nOldX;
            final int nPointerY = nOldY;
            this.mPointer.post(new Runnable() {
                public void run() {
                    FingerDrawActivity.this.mPointer.setVisibility(0);
                    int nWidth = FingerDrawActivity.this.mPointer.getWidth();
                    int nHeight = FingerDrawActivity.this.mPointer.getHeight();
                    LayoutParams param = (LayoutParams) FingerDrawActivity.this.mPointer.getLayoutParams();
                    param.leftMargin = (FingerDrawActivity.this.m_DrawView.getLeft() + ((int) (((float) nPointerX) * FingerDrawActivity.this.m_DrawView.getScale()))) - (nWidth / 2);
                    param.topMargin = (FingerDrawActivity.this.m_DrawView.getTop() + ((int) (((float) nPointerY) * FingerDrawActivity.this.m_DrawView.getScale()))) - (nHeight / 2);
                    FingerDrawActivity.this.mPointer.setLayoutParams(param);
                }
            });
            this.mPointer.removeCallbacks(this.mHidePointRunnable);
            this.mPointer.postDelayed(this.mHidePointRunnable, 5000);
            return;
        }
        this.mPointer.post(new Runnable() {
            public void run() {
                FingerDrawActivity.this.mPointer.setVisibility(4);
            }
        });
    }

    public void onPenConnected() {
    }

    public void onPenDisconnected() {
    }

    public boolean isAutoSubmit() {
        return this.m_bIsAutoSubmit;
    }

    public void onIMMessage(String szFrom, String szMessage) {
        if (this.m_bAllowAutoSubmit && this.m_szAutoSubmitUser.equalsIgnoreCase(szFrom) && szMessage.equalsIgnoreCase("allsubmit")) {
            this.m_bIsAutoSubmit = true;
            Bitmap Result = this.m_DrawView.saveToBitmap();
            if (this.m_TextView.getVisibility() == 0) {
                Bitmap textResult = Bitmap.createBitmap(this.m_TextView.getWidth(), this.m_TextView.getHeight(), Config.ARGB_8888);
                this.m_TextView.draw(new Canvas(textResult));
                Bitmap finalResult = combineImage(Result, textResult);
                Result.recycle();
                textResult.recycle();
                Result = finalResult;
            }
            Bitmap bmpMask = BitmapFactory.decodeResource(getResources(), R.drawable.autosubmitflag);
            new Canvas(Result).drawBitmap(bmpMask, (float) (Result.getWidth() - bmpMask.getWidth()), 0.0f, null);
            bmpMask.recycle();
            if (m_Interface != null) {
                m_Interface.OnOK(Result, this.m_szUploadName, this);
            }
            Result.recycle();
        }
    }

    public void OnTouchMove() {
    }
}
