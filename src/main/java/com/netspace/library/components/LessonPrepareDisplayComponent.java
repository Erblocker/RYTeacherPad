package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.parser.LessonsPrepareResourceParser;
import com.netspace.library.parser.LessonsPrepareResourceParser.LessonPrepareResourceItem;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObjectManager;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class LessonPrepareDisplayComponent extends FrameLayout implements IComponents {
    public static final int CORRECTANSWER_LEVEL_ALL = 2;
    public static final int CORRECTANSWER_LEVEL_NONE = 0;
    public static final int CORRECTANSWER_LEVEL_WRONGQUESTION = 1;
    private static String mClientID;
    private static String mTitle;
    private ComponentCallBack mCallBack;
    private LinearLayout mContentLayout;
    private IWmExamDBOpenHelper mDataBase;
    private int mDisplayOptions = 0;
    private String mJumpToObjectGUID;
    private String mLessonPrepareResourceGUID;
    private LinearLayout mLoadingLayout;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (LessonPrepareDisplayComponent.this.mUserOnClickListener != null) {
                LessonPrepareDisplayComponent.this.mUserOnClickListener.onClick(v);
            }
        }
    };
    private View mRootView;
    private String mScheduleGUID;
    private LockableScrollView mScrollView;
    private Object mSubjectID;
    private TextView mTextViewMessage;
    private boolean mUsePagerContainer = false;
    private String mUserClassGUID;
    private String mUserClassName;
    private OnClickListener mUserOnClickListener;
    private VirtualNetworkObjectManager mVirtualNetworkObjectManager = new VirtualNetworkObjectManager();
    private ArrayList<ResourceItemData> marrData = new ArrayList();
    private ArrayList<String> marrLimitResourceGUIDs;

    public LessonPrepareDisplayComponent(Context context) {
        super(context);
        initView();
    }

    public LessonPrepareDisplayComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LessonPrepareDisplayComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        this.mRootView = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.component_lessonpreparedisplay, this, true);
        this.mContentLayout = (LinearLayout) findViewById(R.id.layoutContent);
        this.mLoadingLayout = (LinearLayout) findViewById(R.id.loadingLayout);
        this.mTextViewMessage = (TextView) findViewById(R.id.textViewMessage);
        this.mScrollView = (LockableScrollView) findViewById(R.id.scrollView1);
        this.mScrollView.setDescendantFocusability(131072);
        this.mScrollView.setFocusable(true);
        this.mScrollView.setFocusableInTouchMode(true);
        this.mScrollView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });
        QuestionWidgetsUtilities.setScrollView(this.mScrollView);
    }

    public void setDataBase(IWmExamDBOpenHelper dataBase) {
        if (dataBase == null) {
            throw new IllegalArgumentException("dataBase can not be null.");
        }
        this.mDataBase = dataBase;
    }

    public void setLimitResourceGUIDs(ArrayList<String> arrGUIDs) {
        this.marrLimitResourceGUIDs = arrGUIDs;
    }

    protected void loadLessonPrepareData(String szLessonPrepareResourceGUID) {
        this.mLessonPrepareResourceGUID = szLessonPrepareResourceGUID;
        if (this.marrData.size() == 0) {
            displayLoading();
            ResourceItemObject ResourceObject = new ResourceItemObject(this.mLessonPrepareResourceGUID, null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    LessonPrepareDisplayComponent.this.hideLoading();
                    LessonPrepareDisplayComponent.this.parserResourceList(ItemObject.readTextData());
                    LessonPrepareDisplayComponent.this.displayContent();
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    LessonPrepareDisplayComponent.this.displayMessage("数据加载错误，错误原因：" + ItemObject.getErrorText());
                }
            });
            ResourceObject.setAllowCache(false);
            ResourceObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
            this.mVirtualNetworkObjectManager.add(ResourceObject);
            return;
        }
        hideLoading();
        setTitle(mTitle);
        displayContent();
    }

    protected void displayContent() {
        if (this.mUserClassName == null || this.mUserClassGUID == null) {
            throw new NullPointerException("Must set userclassname and userclassguid before display content.");
        }
        for (int i = 0; i < this.marrData.size(); i++) {
            ResourceItemData OneItem = (ResourceItemData) this.marrData.get(i);
            JSONObject JSON = new JSONObject();
            try {
                JSON.put(CommentComponent.RESOURCEGUID, OneItem.szResourceGUID);
                JSON.put("isquestion", OneItem.nType == 0);
                JSON.put("guid", OneItem.szGUID);
                JSON.put("subject", this.mSubjectID);
            } catch (JSONException e) {
            }
            int dwFlags = 0;
            if (mClientID == null || mClientID.isEmpty()) {
                dwFlags = ((0 | 2) | 32) | 128;
            }
            if ((this.mDisplayOptions & 64) == 64) {
                dwFlags |= 512;
            }
            if (!this.mUsePagerContainer) {
                ContentDisplayComponent Pager = new ContentDisplayComponent(getContext());
                this.mContentLayout.addView(Pager, new LayoutParams(-1, -2));
                Pager.setOnClickListener(this.mOnClickListener);
                Pager.setScheduleArrangeGUID(this.mScheduleGUID);
                Pager.setUserClassInfo(this.mUserClassName, this.mUserClassGUID);
                Pager.setClientID(mClientID);
                Pager.setResourceItem(OneItem);
                if (OneItem.nCorrectResult != 0) {
                    Pager.setDisplayOptions((this.mDisplayOptions | 32) | dwFlags);
                } else {
                    Pager.setDisplayOptions(this.mDisplayOptions | dwFlags);
                }
                if (this.mDataBase != null) {
                    Pager.setDataBase(this.mDataBase);
                }
                Pager.setData(JSON.toString());
                setContentBlockMargin(Pager);
                if (this.mJumpToObjectGUID != null && this.mJumpToObjectGUID.equalsIgnoreCase(OneItem.szResourceGUID)) {
                    Pager.setBackgroundResource(R.drawable.background_highlightresourceitem);
                }
            }
            if (i != this.marrData.size() - 1) {
                insertLine();
            }
        }
        if (this.mCallBack != null) {
            this.mCallBack.OnDataLoaded(this.mLessonPrepareResourceGUID, this);
        }
        if (this.mJumpToObjectGUID != null && !this.mJumpToObjectGUID.isEmpty()) {
            postDelayed(new Runnable() {
                public void run() {
                    LessonPrepareDisplayComponent.this.jumpToResource(LessonPrepareDisplayComponent.this.mJumpToObjectGUID);
                }
            }, 1000);
        }
    }

    public void save() {
        for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
            View OneView = this.mContentLayout.getChildAt(i);
            if (OneView instanceof ContentDisplayComponent) {
                ContentDisplayComponent ContentDisplayComponent = (ContentDisplayComponent) OneView;
                if (ContentDisplayComponent.isChanged()) {
                    ContentDisplayComponent.save();
                }
            }
        }
        UserInfo.UserScore("SaveHomework", this.mScheduleGUID);
    }

    protected void setContentBlockMargin(View Components) {
        LinearLayout.LayoutParams Params = (LinearLayout.LayoutParams) Components.getLayoutParams();
        int dpToPixel = Utilities.dpToPixel(16, getContext());
        Params.leftMargin = dpToPixel;
        Params.rightMargin = dpToPixel;
        Params.topMargin = dpToPixel;
        Components.setLayoutParams(Params);
    }

    public void setTargetResourceGUID(String szGUID) {
        this.mJumpToObjectGUID = szGUID;
    }

    public void setUserClassInfo(String szUserClassName, String szUserClassGUID) {
        this.mUserClassName = szUserClassName;
        this.mUserClassGUID = szUserClassGUID;
    }

    public void showCorrectAnswer(int nLevel) {
        int i;
        View oneView;
        if (nLevel == 0) {
            for (i = 0; i < this.mContentLayout.getChildCount(); i++) {
                oneView = this.mContentLayout.getChildAt(i);
                if (oneView instanceof ContentDisplayComponent) {
                    ((ContentDisplayComponent) oneView).showCorrectAnswer(false);
                }
            }
        } else if (nLevel == 1) {
            for (i = 0; i < this.mContentLayout.getChildCount(); i++) {
                oneView = this.mContentLayout.getChildAt(i);
                if (oneView instanceof ContentDisplayComponent) {
                    ContentDisplayComponent = (ContentDisplayComponent) oneView;
                    nCorrectResult = ContentDisplayComponent.getResourceItem().nCorrectResult;
                    if (nCorrectResult == 0) {
                        ContentDisplayComponent.showCorrectAnswer(false);
                    } else if (nCorrectResult == -1) {
                        ContentDisplayComponent.showCorrectAnswer(true);
                    } else if (nCorrectResult == 1) {
                        ContentDisplayComponent.showCorrectAnswer(true);
                    } else if (nCorrectResult == 2) {
                        ContentDisplayComponent.showCorrectAnswer(false);
                    }
                }
            }
        } else if (nLevel == 2) {
            for (i = 0; i < this.mContentLayout.getChildCount(); i++) {
                oneView = this.mContentLayout.getChildAt(i);
                if (oneView instanceof ContentDisplayComponent) {
                    ContentDisplayComponent = (ContentDisplayComponent) oneView;
                    nCorrectResult = ContentDisplayComponent.getResourceItem().nCorrectResult;
                    ContentDisplayComponent.showCorrectAnswer(true);
                }
            }
        }
    }

    protected void insertLine() {
        View sepline = new View(getContext());
        this.mContentLayout.addView(sepline, new LayoutParams(-1, Utilities.dpToPixel(1, getContext())));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) sepline.getLayoutParams();
        params.topMargin = Utilities.dpToPixel(10, getContext());
        int dpToPixel = Utilities.dpToPixel(16, getContext());
        params.rightMargin = dpToPixel;
        params.leftMargin = dpToPixel;
        params.bottomMargin = Utilities.dpToPixel(10, getContext());
        sepline.setBackgroundColor(Utilities.getThemeCustomColor(R.attr.content_display_seperatelinecolor));
        sepline.setVisibility(0);
    }

    protected void setTitle(String szTitle) {
        mTitle = szTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public ArrayList<ResourceItemData> getResourceItemData() {
        return this.marrData;
    }

    public LockableScrollView getScrollView() {
        return this.mScrollView;
    }

    public LinearLayout getContentLayout() {
        return this.mContentLayout;
    }

    public void setDisplayOptions(int nOptions) {
        this.mDisplayOptions = nOptions;
    }

    public boolean jumpToResource(String szResourceGUID) {
        for (int i = 0; i < this.mContentLayout.getChildCount(); i++) {
            if (this.mContentLayout.getChildAt(i) instanceof ContentDisplayComponent) {
                ContentDisplayComponent OneView = (ContentDisplayComponent) this.mContentLayout.getChildAt(i);
                if (OneView.getResourceGUID() != null && OneView.getResourceGUID().equalsIgnoreCase(szResourceGUID)) {
                    this.mScrollView.scrollTo(0, OneView.getTop());
                    return true;
                }
            }
        }
        return false;
    }

    protected void displayMessage(String szMessage) {
        this.mLoadingLayout.setVisibility(4);
        this.mContentLayout.setVisibility(4);
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
    }

    protected void displayLoading() {
        this.mLoadingLayout.setVisibility(0);
        this.mContentLayout.setVisibility(4);
        this.mTextViewMessage.setVisibility(4);
    }

    protected void hideLoading() {
        this.mLoadingLayout.setVisibility(4);
        this.mContentLayout.setVisibility(0);
        this.mTextViewMessage.setVisibility(4);
    }

    public void reloadCorrectResult() {
        if (this.mDataBase != null) {
            for (int i = 0; i < this.marrData.size(); i++) {
                ResourceItemData OneItem = (ResourceItemData) this.marrData.get(i);
                OneItem.nCorrectResult = this.mDataBase.GetItemResult(this.mScheduleGUID, OneItem.szResourceGUID, 3);
            }
        }
    }

    protected boolean parserResourceList(String szData) {
        LessonsPrepareResourceParser ResourceParser = new LessonsPrepareResourceParser();
        if (ResourceParser.initialize(getContext(), szData)) {
            this.mSubjectID = Integer.valueOf(ResourceParser.getSubjectID());
            setTitle(ResourceParser.getTitle());
            for (int i = 0; i < ResourceParser.getCount(); i++) {
                LessonPrepareResourceItem OneItem = ResourceParser.getItem(i);
                ResourceItemData NewItem = new ResourceItemData();
                if (this.marrLimitResourceGUIDs == null || Utilities.isInArray(this.marrLimitResourceGUIDs, OneItem.szGUID)) {
                    NewItem.szTitle = OneItem.szTitle;
                    NewItem.szResourceGUID = OneItem.szGUID;
                    NewItem.szFileType = OneItem.szResourceType;
                    NewItem.nType = OneItem.nType;
                    NewItem.nUsageType = OneItem.nUsageType;
                    NewItem.szScheduleGUID = this.mScheduleGUID;
                    NewItem.szScheduleResourceGUID = this.mLessonPrepareResourceGUID;
                    if (this.mDataBase != null) {
                        NewItem.nCorrectResult = this.mDataBase.GetItemResult(this.mScheduleGUID, OneItem.szGUID, 3);
                        NewItem.szGUID = this.mDataBase.CheckItemExsit(this.mScheduleGUID, OneItem.szGUID);
                    }
                    this.marrData.add(NewItem);
                }
            }
            if (this.marrData.size() != 0) {
                return true;
            }
            displayMessage("当前备课资源文件当中没有包含任何内容。");
            return true;
        }
        displayMessage("解析备课资源文件时出现错误。");
        return false;
    }

    public void setOnClickListener(OnClickListener l) {
        this.mUserOnClickListener = l;
    }

    public void setData(String szData) {
        throw new IllegalArgumentException("Please use setData(String szLessonPrepareResourceGUID,String szLessonScheduleGUID, String szClientID) instead of setData(szData");
    }

    public void setData(String szLessonPrepareResourceGUID, String szLessonScheduleGUID, String szClientID) {
        this.mScheduleGUID = szLessonScheduleGUID;
        mClientID = szClientID;
        loadLessonPrepareData(szLessonPrepareResourceGUID);
    }

    public String getData() {
        return null;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }

    protected void onDetachedFromWindow() {
        this.mVirtualNetworkObjectManager.cancelAll();
        Utilities.unbindDrawables(this.mContentLayout);
        this.mContentLayout.removeAllViews();
        QuestionWidgetsUtilities.setScrollView(null);
        super.onDetachedFromWindow();
    }
}
