package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.internal.view.SupportMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.controls.CustomAudioView;
import com.netspace.library.controls.CustomDocumentView;
import com.netspace.library.controls.CustomImageView;
import com.netspace.library.controls.CustomVideoPlayerWrapper;
import com.netspace.library.controls.CustomViewBase;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.DrawView.DrawViewActionInterface;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.parser.LessonClassParser;
import com.netspace.library.parser.LessonsScheduleUserAnswerParser;
import com.netspace.library.parser.QuestionParser;
import com.netspace.library.parser.ResourceBase;
import com.netspace.library.parser.ResourceBase.ResourceBaseCallBack;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.service.AnswerSheetV2MenuService;
import com.netspace.library.service.AnswerSheetV3MenuService;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.QuestionWidgetsUtilities.QuestionWidgetChangeCallBack;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.QuestionItemObject;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.cookie.ClientCookie;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentDisplayComponent extends RelativeLayout implements IComponents, OnScrollChangedListener, OnClickListener {
    public static final String ADDTOQUESTIONBOOK = "addtoquestionbook";
    public static final String ADDTORESOURCELIBRARY = "addtoresourcelibrary";
    public static final String ANSWERSWITCHBUTTON = "hasanswerswitchbutton";
    public static final String DATA = "data";
    public static final int DISPLAY_ANSWERS = 64;
    public static final int DISPLAY_ANSWERS_INLINE = 512;
    public static final int DISPLAY_OPTIONS_AUTO_PLAYVIDEO = 256;
    public static final int DISPLAY_OPTIONS_ENABLE_CORRECTION = 24;
    public static final int DISPLAY_OPTIONS_ENABLE_FULL_QUESTION_DRAW = 8;
    public static final int DISPLAY_OPTIONS_NO_ANSWER = 2;
    public static final int DISPLAY_OPTIONS_NO_CONTENT = 1;
    public static final int DISPLAY_OPTIONS_NO_MODIFY = 32;
    public static final int DISPLAY_OPTIONS_NO_TOOLS = 4;
    public static final int DISPLAY_OPTIONS_SHOW_EXTERNAL = 128;
    public static final String FLAGS = "flags";
    private static final String TAG = "ContentDisplayComponent";
    public static final int VIEW_RESULT_ANSWERED = 2;
    public static final int VIEW_RESULT_OPENED = 1;
    private static int mAnswerSheetIndex = 3000;
    private ComponentCallBack mCallBack;
    private String mClientID;
    private LinearLayout mContentLayout;
    private RelativeLayout mContentRelativeLayout;
    private LinearLayout mCorrectAnswerLayout;
    private String mData;
    private IWmExamDBOpenHelper mDataBase;
    private boolean mDataLoaded = false;
    private int mDisplayOptions = 0;
    private Runnable mDisplayQuestionAnswerRunnable = new Runnable() {
        public void run() {
            if ((ContentDisplayComponent.this.mDisplayOptions & 512) == 512 && ContentDisplayComponent.this.mQuestionXML != null) {
                ContentDisplayComponent.this.mContentRelativeLayout.setMinimumHeight(ContentDisplayComponent.this.mContentLayout.getHeight());
                ContentDisplayComponent.this.mCorrectAnswerLayout.removeAllViews();
                ContentDisplayComponent.this.mCorrectAnswerLayout.setVisibility(0);
                ContentDisplayComponent.this.mCorrectAnswerLayout.setBackgroundResource(R.drawable.background_questionansweritem);
                QuestionParser QuestionParser2 = new QuestionParser();
                if (QuestionParser2.initialize(ContentDisplayComponent.this.getContext(), ContentDisplayComponent.this.mQuestionXML)) {
                    QuestionParser2.setDisplayAnswers(true);
                    QuestionParser2.setDisplayMainContent(false);
                    QuestionParser2.setNoAnswerArea(true);
                    if (QuestionParser2.display(ContentDisplayComponent.this.mCorrectAnswerLayout)) {
                        Utilities.fadeOutView(ContentDisplayComponent.this.mCorrectAnswerLayout, 500);
                        return;
                    }
                    ContentDisplayComponent.this.mCorrectAnswerLayout.removeAllViews();
                    ContentDisplayComponent.this.mCorrectAnswerLayout.setVisibility(8);
                }
            }
        }
    };
    private DrawView mDrawView;
    private DrawView mFullQuestionDrawView;
    private String mGUID;
    private ImageView mImageViewAnswerSheet;
    private ImageView mImageViewFavorite;
    private ImageView mImageViewLock;
    private ImageView mImageViewOpenExternal;
    private ImageView mImageViewPlus;
    private LinearLayout mLoadingLayout;
    final OnFailureListener mOnResourceLoadFailureListener = new OnFailureListener() {
        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            if (ContentDisplayComponent.this.mbAttachedToWindow) {
                ContentDisplayComponent.this.displayMessage("数据加载错误，错误原因：" + ItemObject.getErrorText());
            }
        }
    };
    final OnSuccessListener mOnResourceLoadSuccessListener = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            int i = 0;
            if (ContentDisplayComponent.this.mbAttachedToWindow) {
                ContentDisplayComponent.this.mContentLayout.setVisibility(0);
                if ((ContentDisplayComponent.this.mDisplayOptions & 4) != 4) {
                    ContentDisplayComponent.this.mToolsLayout.setVisibility(0);
                    ContentDisplayComponent.this.checkAnswerSheet();
                } else if ((ContentDisplayComponent.this.mDisplayOptions & 128) == 128) {
                    ContentDisplayComponent.this.mToolsLayout.setVisibility(0);
                    ContentDisplayComponent.this.mImageViewAnswerSheet.setVisibility(8);
                    ContentDisplayComponent.this.mImageViewPlus.setVisibility(8);
                    ContentDisplayComponent.this.mImageViewFavorite.setVisibility(8);
                    ContentDisplayComponent.this.mImageViewLock.setVisibility(8);
                    ContentDisplayComponent.this.mImageViewLock.setVisibility(8);
                    ContentDisplayComponent.this.mImageViewOpenExternal.setVisibility(0);
                } else {
                    ContentDisplayComponent.this.mToolsLayout.setVisibility(4);
                }
                ContentDisplayComponent.this.mLoadingLayout.setVisibility(8);
                ContentDisplayComponent.this.mTextViewMessage.setVisibility(8);
                if (ContentDisplayComponent.this.mbIsQuestion) {
                    ContentDisplayComponent.this.handleQuestionData(ItemObject.readTextData());
                } else {
                    ContentDisplayComponent.this.handleResourceData(ItemObject.readTextData());
                }
                if ((ContentDisplayComponent.this.mDisplayOptions & 2) == 2) {
                    ContentDisplayComponent.this.mQuestionWidgets.setLocked(true);
                }
                if (ContentDisplayComponent.this.mDataBase == null) {
                    return;
                }
                if (ContentDisplayComponent.this.mGUID == null || ContentDisplayComponent.this.mGUID.isEmpty()) {
                    ContentDisplayComponent.this.mGUID = Utilities.createGUID();
                    if (ContentDisplayComponent.this.mScheduleArrangeGUID == null) {
                        throw new IllegalArgumentException("ScheduleArrangeGUID can not be null if using database. ");
                    }
                    IWmExamDBOpenHelper access$14 = ContentDisplayComponent.this.mDataBase;
                    String access$17 = ContentDisplayComponent.this.mScheduleArrangeGUID;
                    String access$18 = ContentDisplayComponent.this.mResourceGUID;
                    String access$15 = ContentDisplayComponent.this.mGUID;
                    String access$19 = ContentDisplayComponent.this.mUserClassGUID;
                    String access$20 = ContentDisplayComponent.this.mUserClassName;
                    if (!ContentDisplayComponent.this.mbIsQuestion) {
                        i = 1;
                    }
                    access$14.SetItemRead(access$17, access$18, access$15, access$19, access$20, i, 1);
                }
            }
        }
    };
    private QuestionWidgetsUtilities mQuestionWidgets;
    private String mQuestionXML;
    final ResourceBaseCallBack mResourceBaseCallBack = new ResourceBaseCallBack() {
        public boolean onAfterLayoutComplete(ResourceBase ResourceObject) {
            if (!ContentDisplayComponent.this.mDataLoaded) {
                return false;
            }
            if (ResourceObject instanceof LessonsScheduleUserAnswerParser) {
                ContentDisplayComponent.this.mbUserAnswerLayoutComplete = true;
                Log.d(ContentDisplayComponent.TAG, "LessonsScheduleUserAnswerParser onAfterLayoutComplete called.");
            }
            if (ResourceObject instanceof ResourceParser) {
                ContentDisplayComponent.this.mbQuestionOrResourceLayoutComplete = true;
                Log.d(ContentDisplayComponent.TAG, "ResourceParser onAfterLayoutComplete called.");
            }
            if (ResourceObject instanceof QuestionParser) {
                ContentDisplayComponent.this.mbQuestionOrResourceLayoutComplete = true;
                Log.d(ContentDisplayComponent.TAG, "QuestionParser onAfterLayoutComplete called.");
            }
            if (!ContentDisplayComponent.this.mbUserAnswerLayoutComplete || !ContentDisplayComponent.this.mbQuestionOrResourceLayoutComplete) {
                return true;
            }
            ContentDisplayComponent.this.displayHandWrite();
            if (ContentDisplayComponent.this.mbHideAnswer || (ContentDisplayComponent.this.mDisplayOptions & 512) != 512) {
                return true;
            }
            ContentDisplayComponent.this.mRootView.removeCallbacks(ContentDisplayComponent.this.mDisplayQuestionAnswerRunnable);
            ContentDisplayComponent.this.mRootView.postDelayed(ContentDisplayComponent.this.mDisplayQuestionAnswerRunnable, 300);
            return true;
        }
    };
    private String mResourceGUID;
    private ResourceItemData mResourceItemData;
    private int mResourceSubjectID = -1;
    private View mRootView;
    private String mScheduleArrangeGUID;
    private int mSubjectID = -1;
    private TextView mTextViewDiscussCount;
    private TextView mTextViewMessage;
    private String mTitle;
    private LinearLayout mToolsLayout;
    private LessonsScheduleUserAnswerParser mUserAnswerParser;
    private String mUserClassGUID;
    private String mUserClassName;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    private boolean mbAllowModify = true;
    private boolean mbAnswerSheetNewVersion = false;
    private boolean mbAttachedToWindow = false;
    private boolean mbChanged = false;
    private boolean mbFavorite = false;
    private boolean mbHideAnswer = false;
    private boolean mbIsQuestion = false;
    private boolean mbLayoutComplete = false;
    private boolean mbLocked = false;
    private boolean mbQuestionOrResourceLayoutComplete = false;
    private boolean mbUserAnswerLayoutComplete = false;
    private String mszUserAnswerXML;

    public ContentDisplayComponent(Context context) {
        super(context);
        initView();
    }

    public ContentDisplayComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ContentDisplayComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        this.mRootView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.component_contentdisplay, this, true);
        this.mContentLayout = (LinearLayout) this.mRootView.findViewById(R.id.contentLayout);
        this.mLoadingLayout = (LinearLayout) this.mRootView.findViewById(R.id.loadingLayout);
        this.mCorrectAnswerLayout = (LinearLayout) this.mRootView.findViewById(R.id.correctAnswerLayout);
        this.mToolsLayout = (LinearLayout) this.mRootView.findViewById(R.id.questiontoolsLayout);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mContentRelativeLayout = (RelativeLayout) this.mRootView.findViewById(R.id.contentRelativeLayout);
        this.mDrawView = (DrawView) this.mRootView.findViewById(R.id.drawPad);
        this.mFullQuestionDrawView = (DrawView) this.mRootView.findViewById(R.id.fullQuestionDrawPad);
        this.mLoadingLayout.setVisibility(0);
        this.mToolsLayout.setVisibility(4);
        this.mCorrectAnswerLayout.setVisibility(4);
        this.mContentLayout.setVisibility(4);
        this.mDrawView.setVisibility(4);
        this.mFullQuestionDrawView.setVisibility(4);
        this.mFullQuestionDrawView.setFocusable(false);
        this.mDrawView.setFocusable(false);
        this.mTextViewMessage.setVisibility(4);
        this.mImageViewPlus = (ImageView) this.mRootView.findViewById(R.id.imageViewAdd);
        this.mImageViewLock = (ImageView) this.mRootView.findViewById(R.id.imageViewLock);
        this.mImageViewFavorite = (ImageView) this.mRootView.findViewById(R.id.imageViewFavorite);
        this.mImageViewOpenExternal = (ImageView) this.mRootView.findViewById(R.id.imageViewMore);
        this.mImageViewAnswerSheet = (ImageView) this.mRootView.findViewById(R.id.imageViewAnswerSheet);
        this.mImageViewAnswerSheet.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_th).color(-16777216).actionBarSize());
        this.mTextViewDiscussCount = (TextView) this.mRootView.findViewById(R.id.textViewTip);
        this.mTextViewDiscussCount.setText("");
        this.mTextViewDiscussCount.setVisibility(4);
        this.mImageViewOpenExternal.setVisibility(8);
        this.mImageViewAnswerSheet.setVisibility(8);
        this.mImageViewAnswerSheet.setOnClickListener(this);
        this.mImageViewPlus.setOnClickListener(this);
        this.mImageViewLock.setOnClickListener(this);
        this.mImageViewFavorite.setOnClickListener(this);
        this.mImageViewOpenExternal.setOnClickListener(this);
        this.mQuestionWidgets = new QuestionWidgetsUtilities(getContext(), this.mContentLayout, this.mContentRelativeLayout, new QuestionWidgetChangeCallBack() {
            public void onChanged() {
                ContentDisplayComponent.this.setChanged(true);
            }
        });
        this.mQuestionWidgets.setClientID(this.mClientID);
        this.mbAttachedToWindow = false;
    }

    public void clear() {
        this.mDataLoaded = false;
        this.mUserAnswerParser = null;
        this.mContentRelativeLayout.removeAllViews();
        this.mContentLayout.removeAllViews();
    }

    protected String initContent(String szResourceGUID, boolean bIsQuestion, String szUserAnswerDataSychronizeItemGUID) {
        this.mDataLoaded = true;
        if (szResourceGUID == null) {
            throw new IllegalArgumentException("szResourceGUID can not be null.");
        }
        if ((this.mDisplayOptions & 4) == 4) {
            this.mToolsLayout.setVisibility(4);
        }
        this.mResourceGUID = szResourceGUID;
        this.mGUID = szUserAnswerDataSychronizeItemGUID;
        this.mbIsQuestion = bIsQuestion;
        if (this.mDataBase != null) {
            if (this.mScheduleArrangeGUID == null) {
                throw new IllegalArgumentException("ScheduleArrangeGUID can not be null if using database. Must be set before call initContent");
            }
            this.mGUID = this.mDataBase.CheckItemExsit(this.mScheduleArrangeGUID, this.mResourceGUID);
        }
        if (bIsQuestion) {
            QuestionItemObject ResourceObject = new QuestionItemObject(szResourceGUID, null);
            ResourceObject.setSuccessListener(this.mOnResourceLoadSuccessListener);
            ResourceObject.setFailureListener(this.mOnResourceLoadFailureListener);
            ResourceObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
            this.mVirtualNetworkObjectManager.add(ResourceObject);
        } else {
            ResourceItemObject ResourceObject2 = new ResourceItemObject(szResourceGUID, null);
            ResourceObject2.setSuccessListener(this.mOnResourceLoadSuccessListener);
            ResourceObject2.setFailureListener(this.mOnResourceLoadFailureListener);
            ResourceObject2.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ResourceObject2);
            this.mVirtualNetworkObjectManager.add(ResourceObject2);
        }
        this.mUserAnswerParser = new LessonsScheduleUserAnswerParser();
        this.mUserAnswerParser.setRelativeLayout(this.mContentRelativeLayout);
        this.mUserAnswerParser.setCallBack(this.mResourceBaseCallBack);
        if ((this.mDisplayOptions & 1) == 1) {
            this.mUserAnswerParser.setAlignTop(true);
        }
        this.mUserAnswerParser.setClientID(this.mClientID);
        LessonsScheduleUserAnswerParser.setUseScaledBitmap(true);
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetPrivateData2", null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    try {
                        if (new JSONObject(ItemObject.readTextData()).getBoolean("enableDiscuss") && ContentDisplayComponent.this.mImageViewOpenExternal.getVisibility() == 0) {
                            ContentDisplayComponent.this.mTextViewDiscussCount.setVisibility(0);
                            ContentDisplayComponent.this.getDiscussTopicCount(ContentDisplayComponent.this.mResourceGUID);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("lpszKey", "ResourceProperties_" + szResourceGUID);
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
        this.mVirtualNetworkObjectManager.add(CallItem);
        return this.mGUID;
    }

    private void getDiscussTopicCount(String szResourceGUID) {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("DiscussGetTopicCount", null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    ContentDisplayComponent.this.mTextViewDiscussCount.setText(ItemObject.readTextData());
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("lpszObjectGUID", szResourceGUID);
        CallItem.setParam("lpszStartDate", "");
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
        this.mVirtualNetworkObjectManager.add(CallItem);
    }

    public void setDataBase(IWmExamDBOpenHelper dataBase) {
        if (dataBase == null) {
            throw new IllegalArgumentException("dataBase can not be null.");
        }
        this.mDataBase = dataBase;
    }

    public void setScheduleArrangeGUID(String szGUID) {
        this.mScheduleArrangeGUID = szGUID;
    }

    public void setDisplayOptions(int nOptions) {
        if (this.mDataLoaded) {
            throw new IllegalArgumentException("setDisplayOptions must be called before data loaded. Or no changes will take place.");
        } else if ((nOptions & 3) == 3) {
            throw new IllegalArgumentException("Can not hide content while hide answer.");
        } else {
            this.mDisplayOptions = nOptions;
            if ((this.mDisplayOptions & 4) == 4) {
                if ((this.mDisplayOptions & 128) == 128) {
                    this.mToolsLayout.setVisibility(0);
                    this.mImageViewPlus.setVisibility(8);
                    this.mImageViewFavorite.setVisibility(8);
                    this.mImageViewLock.setVisibility(8);
                    this.mImageViewLock.setVisibility(8);
                    this.mImageViewOpenExternal.setVisibility(0);
                } else {
                    this.mToolsLayout.setVisibility(4);
                }
            }
            if (this.mClientID == null || this.mClientID.isEmpty()) {
                this.mImageViewPlus.setVisibility(8);
                this.mImageViewFavorite.setVisibility(8);
                this.mImageViewLock.setVisibility(8);
                this.mImageViewLock.setVisibility(8);
                if ((this.mDisplayOptions & 128) == 128) {
                    this.mImageViewOpenExternal.setVisibility(0);
                }
            }
            if ((this.mDisplayOptions & 32) == 32) {
                this.mbAllowModify = false;
            } else {
                this.mbAllowModify = true;
            }
        }
    }

    protected void displayMessage(String szMessage) {
        this.mLoadingLayout.setVisibility(4);
        this.mContentLayout.setVisibility(4);
        this.mToolsLayout.setVisibility(4);
        this.mDrawView.setVisibility(4);
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
    }

    public void setClientID(String szClientID) {
        this.mClientID = szClientID;
        this.mQuestionWidgets.setClientID(szClientID);
    }

    public void setUserClassInfo(String szUserClassName, String szUserClassGUID) {
        this.mUserClassName = szUserClassName;
        this.mUserClassGUID = szUserClassGUID;
    }

    public String getClientID() {
        return this.mClientID;
    }

    public String getResourceGUID() {
        return this.mResourceGUID;
    }

    public boolean isQuestion() {
        return this.mbIsQuestion;
    }

    public String getTitle() {
        return this.mTitle;
    }

    protected void autoStartViewPlay() {
        if ((this.mDisplayOptions & 256) == 256) {
            for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
                View oneView = this.mContentLayout.getChildAt(i);
                if (oneView instanceof CustomVideoPlayerWrapper) {
                    ((CustomVideoPlayerWrapper) oneView).startPlay();
                }
            }
        }
    }

    protected void handleResourceData(String szXMLText) {
        ResourceParser ResourceParser = new ResourceParser();
        LessonClassParser LessonClassParser = new LessonClassParser();
        ResourceParser.setCallBack(this.mResourceBaseCallBack);
        LessonClassParser.setCallBack(this.mResourceBaseCallBack);
        if ((this.mDisplayOptions & 64) == 64) {
            this.mImageViewOpenExternal.setVisibility(0);
        }
        if (ResourceParser.initialize(getContext(), szXMLText)) {
            this.mTitle = ResourceParser.getTitle();
            if ((this.mDisplayOptions & 1) == 0) {
                ResourceParser.display(this.mContentLayout);
            } else if ((this.mDisplayOptions & 1) == 1) {
                this.mbQuestionOrResourceLayoutComplete = true;
            }
            autoStartViewPlay();
            loadUserAnswers();
        } else if (LessonClassParser.initialize(getContext(), szXMLText)) {
            this.mTitle = LessonClassParser.getTitle();
            if ((this.mDisplayOptions & 1) == 0) {
                LessonClassParser.display(this.mContentLayout);
            }
            autoStartViewPlay();
            loadUserAnswers();
        } else {
            displayMessage("资源处理出现问题，错误原因：" + ResourceParser.getErrorText());
        }
    }

    protected void handleQuestionData(String szXMLText) {
        QuestionParser QuestionParser = new QuestionParser();
        QuestionParser.setCallBack(this.mResourceBaseCallBack);
        if (QuestionParser.initialize(getContext(), szXMLText)) {
            String szQuestionTitle = QuestionParser.getTitle();
            if (!szQuestionTitle.isEmpty()) {
                this.mTitle = szQuestionTitle;
            }
            this.mQuestionXML = szXMLText;
            if (this.mResourceSubjectID == -1) {
                this.mResourceSubjectID = QuestionParser.getSubjectID();
            }
            if ((this.mDisplayOptions & 1) == 1) {
                QuestionParser.setHideQuestionContent(true);
            }
            if ((this.mDisplayOptions & 64) == 64) {
                if ((this.mDisplayOptions & 2) == 2) {
                    QuestionParser.setDisplayAnswers(true);
                } else {
                    this.mImageViewOpenExternal.setVisibility(0);
                }
            }
            QuestionParser.display(this.mContentLayout);
            loadUserAnswers();
            return;
        }
        displayMessage("资源处理出现问题，错误原因：" + QuestionParser.getErrorText());
    }

    public int getResourceSubjectID() {
        return this.mResourceSubjectID;
    }

    public void showCorrectAnswer(boolean bShow) {
        if (bShow) {
            this.mbHideAnswer = false;
            if (this.mCorrectAnswerLayout.getVisibility() != 0) {
                this.mRootView.removeCallbacks(this.mDisplayQuestionAnswerRunnable);
                this.mRootView.postDelayed(this.mDisplayQuestionAnswerRunnable, 300);
                return;
            }
            return;
        }
        this.mbHideAnswer = true;
        this.mCorrectAnswerLayout.removeAllViews();
        this.mCorrectAnswerLayout.setVisibility(8);
    }

    private void parseUserAnswer(String szAnswerXML) {
        boolean bUserAnswered = false;
        if (!(szAnswerXML == null || szAnswerXML.isEmpty())) {
            if (this.mUserAnswerParser.initialize(getContext(), szAnswerXML)) {
                bUserAnswered = this.mUserAnswerParser.display(this.mContentLayout);
                this.mbLocked = this.mUserAnswerParser.getItemData().bLocked;
                this.mbFavorite = this.mUserAnswerParser.getItemData().bFav;
                if ((this.mDisplayOptions & 24) == 24) {
                    this.mbLocked = true;
                }
                if ((this.mDisplayOptions & 32) == 32) {
                    this.mbLocked = true;
                }
                updateIcons();
                setLocked(this.mbLocked);
                this.mQuestionWidgets.finishLoad();
            } else {
                displayMessage("学生作答数据解析错误，错误原因：" + this.mUserAnswerParser.getErrorText());
            }
        }
        if ((this.mDisplayOptions & 1) == 1 && !bUserAnswered) {
            displayMessage("此学生没有作答。");
        }
    }

    protected void loadUserAnswers() {
        if (this.mszUserAnswerXML != null) {
            parseUserAnswer(this.mszUserAnswerXML);
        }
        if ((this.mDisplayOptions & 2) != 2 && this.mGUID != null && !this.mGUID.isEmpty()) {
            DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(this.mGUID, null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    if (ContentDisplayComponent.this.mbAttachedToWindow) {
                        ContentDisplayComponent.this.parseUserAnswer(ItemObject.readTextData());
                    }
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    if (ContentDisplayComponent.this.mbAttachedToWindow) {
                        ContentDisplayComponent.this.displayMessage("学生作答数据加载错误，错误原因：" + ItemObject.getErrorText());
                    }
                }
            });
            ResourceObject.setClientID(this.mClientID);
            ResourceObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
            this.mVirtualNetworkObjectManager.add(ResourceObject);
        }
    }

    public DrawView getDrawView() {
        return this.mDrawView;
    }

    public void loadCorrectHandWriting() {
        DataSynchronizeItemObject ResourceObject = new DataSynchronizeItemObject(this.mGUID + "_CorrectResult", null);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                ContentDisplayComponent.this.mDrawView.fromString(ItemObject.readTextData());
            }
        });
        ResourceObject.setClientID(this.mClientID);
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
        this.mVirtualNetworkObjectManager.add(ResourceObject);
    }

    protected void displayHandWrite() {
        if ((this.mDisplayOptions & 2) != 2) {
            boolean bSkipHandWriteLoad = false;
            if (this.mDrawView.getVisibility() == 0) {
                bSkipHandWriteLoad = true;
            }
            this.mDrawView.setVisibility(0);
            int nTop = this.mUserAnswerParser.getFirstContentTop();
            int nHeight = Math.max(this.mContentLayout.getBottom(), this.mContentRelativeLayout.getBottom());
            LayoutParams Params = (LayoutParams) this.mDrawView.getLayoutParams();
            int i = 0;
            while (i < this.mContentLayout.getChildCount()) {
                View OneView = this.mContentLayout.getChildAt(i);
                if ((OneView instanceof WebView) || (OneView instanceof CustomImageView) || (OneView instanceof CustomVideoPlayerWrapper) || (OneView instanceof CustomAudioView) || (OneView instanceof CustomDocumentView) || (OneView instanceof ImageView)) {
                    i++;
                } else {
                    if (nTop == 0 && OneView.getTop() > 0) {
                        nTop = OneView.getTop();
                    }
                    Params.topMargin = nTop;
                    Params.height = nHeight - nTop;
                    this.mDrawView.setLayoutParams(Params);
                    this.mDrawView.setEnableCache(true);
                    if (!bSkipHandWriteLoad) {
                        loadCorrectHandWriting();
                    }
                    if ((this.mDisplayOptions & 8) == 8) {
                        this.mDrawView.setBrushMode(true);
                        if ((this.mDisplayOptions & 24) == 24) {
                            this.mDrawView.setColor(SupportMenu.CATEGORY_MASK);
                            this.mDrawView.setOnlyActivePenDraw(true);
                        }
                        this.mDrawView.setCallback(new DrawViewActionInterface() {
                            private boolean mEraseMode = false;

                            public void OnTouchDown() {
                                ContentDisplayComponent.this.lockParents(true);
                                ContentDisplayComponent.this.mbChanged = true;
                            }

                            public void OnTouchUp() {
                                ContentDisplayComponent.this.lockParents(false);
                            }

                            public void OnPenButtonDown() {
                                if (this.mEraseMode) {
                                    Utilities.showToastMessage(ContentDisplayComponent.this.getContext(), "已切换为书写模式");
                                    ContentDisplayComponent.this.mDrawView.setEraseMode2(false, 0);
                                    ContentDisplayComponent.this.mDrawView.setBrushMode(true);
                                    this.mEraseMode = false;
                                    return;
                                }
                                Utilities.showToastMessage(ContentDisplayComponent.this.getContext(), "已切换为擦除模式");
                                ContentDisplayComponent.this.mDrawView.setBrushMode(false);
                                ContentDisplayComponent.this.mDrawView.setEraseMode2(true, 0);
                                this.mEraseMode = true;
                            }

                            public void OnPenButtonUp() {
                            }

                            public void OnTouchPen() {
                            }

                            public void OnTouchFinger() {
                            }

                            public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight) {
                            }

                            public void OnTouchMove() {
                            }
                        });
                    }
                }
            }
            Params.topMargin = nTop;
            Params.height = nHeight - nTop;
            this.mDrawView.setLayoutParams(Params);
            this.mDrawView.setEnableCache(true);
            if (bSkipHandWriteLoad) {
                loadCorrectHandWriting();
            }
            if ((this.mDisplayOptions & 8) == 8) {
                this.mDrawView.setBrushMode(true);
                if ((this.mDisplayOptions & 24) == 24) {
                    this.mDrawView.setColor(SupportMenu.CATEGORY_MASK);
                    this.mDrawView.setOnlyActivePenDraw(true);
                }
                this.mDrawView.setCallback(/* anonymous class already generated */);
            }
        }
    }

    protected void lockParents(boolean bLock) {
        View parentView = (View) getParent();
        while (parentView != null) {
            boolean z;
            if (parentView instanceof LockableScrollView) {
                LockableScrollView LockableScrollView = (LockableScrollView) parentView;
                if (bLock) {
                    z = false;
                } else {
                    z = true;
                }
                LockableScrollView.setScrollingEnabled(z);
            }
            if (parentView instanceof CustomViewPager) {
                CustomViewPager CustomViewPager = (CustomViewPager) parentView;
                if (bLock) {
                    z = false;
                } else {
                    z = true;
                }
                CustomViewPager.setPagingEnabled(z);
            }
            if (parentView.getParent() instanceof View) {
                parentView = (View) parentView.getParent();
            } else {
                return;
            }
        }
    }

    protected void onAttachedToWindow() {
        this.mbAttachedToWindow = true;
        if (getLocalVisibleRect(new Rect())) {
            initContent();
        } else {
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                vto.addOnScrollChangedListener(this);
            }
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        this.mbAttachedToWindow = false;
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.removeOnScrollChangedListener(this);
        }
        this.mQuestionWidgets.close();
        this.mQuestionWidgets.clearCallBack();
        this.mVirtualNetworkObjectManager.cancelAll();
        Utilities.unbindDrawables(this);
        super.onDetachedFromWindow();
    }

    public void onClick(View v) {
        boolean z = false;
        Utilities.logClick(v, this.mData);
        if (v.getId() == R.id.imageViewAdd) {
            this.mContentLayout.performLongClick();
        } else if (v.getId() == R.id.imageViewLock) {
            if (this.mbAllowModify) {
                if (!this.mbLocked) {
                    z = true;
                }
                setLocked(z);
            }
        } else if (v.getId() == R.id.imageViewFavorite) {
            if (!this.mbFavorite) {
                z = true;
            }
            addFavorite(z);
            this.mbChanged = true;
        } else if (v.getId() == R.id.imageViewAnswerSheet) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(CommentComponent.RESOURCEGUID, this.mResourceGUID);
                jsonObject.put("lock", this.mbLocked);
                jsonObject.put(DeviceOperationRESTServiceProvider.CLIENTID, getClientID());
                jsonObject.put("scheduleguid", this.mScheduleArrangeGUID);
                jsonObject.put("guid", this.mGUID);
                jsonObject.put("userclassname", this.mUserClassName);
                jsonObject.put(UserHonourFragment.USERCLASSGUID, this.mUserClassGUID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (this.mbAnswerSheetNewVersion) {
                Intent AnswerSheetV3Service = new Intent(getContext(), AnswerSheetV3MenuService.class);
                AnswerSheetV3Service.putExtra("operation", 100);
                AnswerSheetV3Service.putExtra("data", jsonObject.toString());
                getContext().startService(AnswerSheetV3Service);
            } else {
                Intent AnswerSheetV2Service = new Intent(getContext(), AnswerSheetV2MenuService.class);
                AnswerSheetV2Service.putExtra("operation", 100);
                AnswerSheetV2Service.putExtra("data", jsonObject.toString());
                AnswerSheetV2MenuService.setDataBase(this.mDataBase);
                getContext().startService(AnswerSheetV2Service);
            }
        } else if (v.getId() == R.id.imageViewMore) {
            Intent intent = new Intent(getContext(), ResourceDetailActivity.class);
            intent.putExtra(CommentComponent.RESOURCEGUID, this.mResourceGUID);
            intent.putExtra("isquestion", this.mbIsQuestion);
            intent.putExtra("title", this.mTitle + "详细信息");
            intent.putExtra("resourcetype", this.mResourceItemData.nType);
            intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
            getContext().startActivity(intent);
        }
        updateIcons();
    }

    protected void addFavorite(boolean bAdd) {
        this.mbFavorite = bAdd;
        if (this.mResourceItemData.szGUID.isEmpty() || this.mGUID == null || this.mGUID.isEmpty()) {
            this.mResourceItemData.szGUID = this.mDataBase.CheckItemExsit(this.mScheduleArrangeGUID, this.mResourceGUID);
            this.mGUID = this.mResourceItemData.szGUID;
        }
        save();
        if (this.mDataBase != null) {
            this.mDataBase.SetItemResult(this.mScheduleArrangeGUID, this.mResourceGUID, 2, this.mbFavorite ? 1 : 0);
            if (bAdd) {
                this.mDataBase.addResourceToQuestionBook(this.mResourceItemData, this.mResourceItemData.szTitle, this.mSubjectID, "我的好题本", "收藏的", "");
            } else {
                this.mDataBase.deleteQuestionBookItemByObjectGUID(this.mResourceItemData.szGUID);
            }
        }
    }

    protected void updateIcons() {
        if (this.mbLocked) {
            this.mImageViewLock.setImageResource(R.drawable.ic_locked_light);
        } else {
            this.mImageViewLock.setImageResource(R.drawable.ic_locked);
        }
        if (this.mbFavorite) {
            this.mImageViewFavorite.setImageResource(R.drawable.ic_star_light);
        } else {
            this.mImageViewFavorite.setImageResource(R.drawable.ic_star);
        }
        if (this.mResourceItemData != null) {
            this.mResourceItemData.bFav = this.mbFavorite;
            this.mResourceItemData.bLocked = this.mbLocked;
        }
    }

    public void setData(String szData) {
        if (szData == null) {
            throw new NullPointerException("setData can not use null as data.");
        }
        this.mData = szData;
        try {
            JSONObject JSON = new JSONObject(this.mData);
            this.mResourceGUID = JSON.getString(CommentComponent.RESOURCEGUID);
            this.mbIsQuestion = JSON.getBoolean("isquestion");
            this.mGUID = JSON.getString("guid");
            if (JSON.has("subject")) {
                this.mSubjectID = JSON.getInt("subject");
            }
            if (JSON.has("title")) {
                this.mTitle = JSON.getString("title");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (getLocalVisibleRect(new Rect())) {
            initContent();
        }
    }

    public void initContent() {
        if (this.mData != null && !this.mData.isEmpty()) {
            try {
                JSONObject JSON = new JSONObject(this.mData);
                initContent(JSON.getString(CommentComponent.RESOURCEGUID), JSON.getBoolean("isquestion"), JSON.getString("guid"));
                Utilities.logClick(this, this.mData, "initcontent");
            } catch (JSONException e) {
                e.printStackTrace();
                displayMessage("显示资源时出现错误，错误信息：" + e.getMessage());
            }
        }
    }

    public String getData() {
        JSONObject JSON = new JSONObject();
        String szResult = "";
        try {
            JSON.put(CommentComponent.RESOURCEGUID, this.mResourceGUID);
            JSON.put("isquestion", this.mbIsQuestion);
            JSON.put("guid", this.mGUID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return szResult;
    }

    public String getGUID() {
        return this.mGUID;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void onScrollChanged() {
        if (getLocalVisibleRect(new Rect()) && !this.mDataLoaded) {
            initContent();
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                vto.removeOnScrollChangedListener(this);
            }
        }
    }

    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility != 0) {
            for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
                View OneView = this.mContentLayout.getChildAt(i);
                if (OneView instanceof CustomVideoPlayerWrapper) {
                    CustomVideoPlayerWrapper OnePlayer = (CustomVideoPlayerWrapper) OneView;
                    if (OnePlayer.isPlaying()) {
                        OnePlayer.pause();
                    }
                } else if (OneView instanceof CustomAudioView) {
                    ((CustomAudioView) OneView).stopPlay();
                }
            }
        }
        super.onWindowVisibilityChanged(visibility);
    }

    public void setResourceItem(ResourceItemData ResourceItemData) {
        this.mResourceItemData = ResourceItemData;
    }

    public ResourceItemData getResourceItem() {
        return this.mResourceItemData;
    }

    public String getUserAnswers() {
        if (this.mResourceItemData == null) {
            this.mResourceItemData = new ResourceItemData();
        }
        this.mResourceItemData.szGUID = this.mGUID;
        this.mResourceItemData.szResourceGUID = this.mResourceGUID;
        this.mResourceItemData.bLocked = this.mbLocked;
        this.mResourceItemData.bFav = this.mbFavorite;
        return this.mUserAnswerParser.save(this.mResourceItemData, this.mContentLayout);
    }

    public void setUserAnswer(String szUserAnswerXML) {
        this.mszUserAnswerXML = szUserAnswerXML;
    }

    public void save() {
        int i = 0;
        if ((this.mDisplayOptions & 2) != 2 && (this.mDisplayOptions & 24) != 24 && this.mDataLoaded) {
            if (this.mGUID == null || this.mGUID.isEmpty()) {
                this.mGUID = Utilities.createGUID();
            }
            if (this.mResourceItemData == null) {
                this.mResourceItemData = new ResourceItemData();
            }
            this.mResourceItemData.szGUID = this.mGUID;
            this.mResourceItemData.szResourceGUID = this.mResourceGUID;
            this.mResourceItemData.bLocked = this.mbLocked;
            this.mResourceItemData.bFav = this.mbFavorite;
            String szXML = this.mUserAnswerParser.save(this.mResourceItemData, this.mContentLayout);
            if (!szXML.isEmpty()) {
                DataSynchronizeItemObject DataObject = new DataSynchronizeItemObject(this.mGUID, null);
                DataObject.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        ContentDisplayComponent.this.setChanged(false);
                    }
                });
                DataObject.writeTextData(szXML);
                DataObject.setReadOperation(false);
                DataObject.setNoDeleteOnFinish(true);
                DataObject.setClientID(this.mClientID);
                DataObject.setAlwaysActiveCallbacks(true);
                VirtualNetworkObject.addToQueue(DataObject);
                this.mVirtualNetworkObjectManager.add(DataObject);
                if (this.mDataBase != null && this.mUserAnswerParser.getRealAnswered()) {
                    IWmExamDBOpenHelper iWmExamDBOpenHelper = this.mDataBase;
                    String str = this.mScheduleArrangeGUID;
                    String str2 = this.mResourceGUID;
                    String str3 = this.mGUID;
                    String str4 = this.mUserClassGUID;
                    String str5 = this.mUserClassName;
                    if (!this.mbIsQuestion) {
                        i = 1;
                    }
                    iWmExamDBOpenHelper.SetItemRead(str, str2, str3, str4, str5, i, 2);
                }
            }
        }
    }

    public void setChanged(boolean bChanged) {
        if (bChanged) {
            Log.d(TAG, "change found.");
            if (!this.mbChanged) {
                Utilities.logClick(this, this.mData, "changed");
            }
        }
        this.mbChanged = bChanged;
    }

    public boolean isChanged() {
        if (!this.mbChanged) {
            for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
                View OneView = this.mContentLayout.getChildAt(i);
                if ((OneView instanceof CustomViewBase) && ((CustomViewBase) OneView).isChanged()) {
                    return true;
                }
            }
        }
        return this.mbChanged;
    }

    public void setLocked(boolean bLock) {
        this.mbLocked = bLock;
        this.mQuestionWidgets.setLocked(bLock);
        for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
            View OneView = this.mContentLayout.getChildAt(i);
            if (OneView instanceof CustomViewBase) {
                ((CustomViewBase) OneView).setLocked(this.mbLocked);
            }
        }
    }

    private void checkAnswerSheet() {
        PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject("AnswerSheet_" + this.mResourceGUID, null, new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szXML = ItemObject.readTextData();
                if (szXML != null && !szXML.isEmpty()) {
                    try {
                        JSONObject json = new JSONObject(szXML);
                        if (json.has(ClientCookie.VERSION_ATTR) && json.getString(ClientCookie.VERSION_ATTR).equalsIgnoreCase("3.0")) {
                            ContentDisplayComponent.this.mbAnswerSheetNewVersion = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ContentDisplayComponent.this.mImageViewAnswerSheet.setVisibility(0);
                }
            }
        });
        PrivateDataObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                ContentDisplayComponent.this.mImageViewAnswerSheet.setVisibility(8);
            }
        });
        PrivateDataObject.setAllowCache(true);
        PrivateDataObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(PrivateDataObject);
    }
}
