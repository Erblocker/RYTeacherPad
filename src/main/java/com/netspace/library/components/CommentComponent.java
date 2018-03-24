package com.netspace.library.components;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.activity.CommentActivity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.controls.CustomChatInputView;
import com.netspace.library.controls.CustomChatInputView.CustomChatCallBack;
import com.netspace.library.controls.CustomCommentView;
import com.netspace.library.controls.CustomCommentView.CommentViewCallBack;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentComponent extends CustomFrameLayout implements IComponents, CustomChatCallBack, CommentViewCallBack, OnScrollChangedListener {
    public static final String RESOURCEGUID = "resourceguid";
    private static String mOwnerGUID = "";
    private static boolean mbCanDeleteAll = false;
    private LinearLayout mContentLayout;
    private Context mContext;
    private Context mContextThemeWrapper;
    private boolean mDataLoaded = false;
    private CustomChatInputView mInputView;
    private String mObjectGUID = "";
    private LinearLayout mRootLayout;
    private View mRootView;
    private TextView mTextViewEmpty;
    private boolean mbAttachedToWindow;
    private boolean mbDiscussLock = false;
    private boolean mbDiscussOpen = false;
    private boolean mbEnableWrite = true;

    public CommentComponent(Context context) {
        super(context);
        initView();
    }

    public CommentComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CommentComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public static void setOwnerGUID(String szOwnerGUID) {
        mOwnerGUID = szOwnerGUID;
    }

    public static void setCanDeleteAll(boolean bCanDeleteAll) {
        mbCanDeleteAll = bCanDeleteAll;
    }

    public static void startComment(Context context, String szObjectGUID, String szTitle) {
        Intent intent = new Intent(context, CommentActivity.class);
        intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        intent.putExtra("objectGUID", szObjectGUID);
        intent.putExtra("title", szTitle);
        context.startActivity(intent);
    }

    protected void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.dialog_comment, this, true);
        this.mContentLayout = (LinearLayout) this.mRootView.findViewById(R.id.LinearLayoutContent);
        this.mRootLayout = (LinearLayout) this.mRootView.findViewById(R.id.LinearLayoutRoot);
        this.mInputView = new CustomChatInputView(this.mContextThemeWrapper);
        this.mInputView.setCallBack(this);
        this.mInputView.setHintText("在这里输入新的主题...");
        this.mRootLayout.addView(this.mInputView, 0);
        if (!MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_DISCUSS_WRITE)) {
            this.mInputView.setHintText("当前不能发布新内容");
            this.mInputView.setEnabled(false);
            this.mbEnableWrite = false;
        }
        this.mTextViewEmpty = (TextView) this.mRootView.findViewById(R.id.textViewEmpty);
        this.mContext = this.mContextThemeWrapper;
    }

    private void checkDiscussOpen() {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetPrivateData2", UI.getCurrentActivity());
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ItemObject.readTextData());
                        if (jsonObject.has("enableDiscuss") && jsonObject.getBoolean("enableDiscuss")) {
                            if (jsonObject.has("lockDiscuss") && jsonObject.getBoolean("lockDiscuss")) {
                                CommentComponent.this.mbDiscussOpen = true;
                                CommentComponent.this.mbDiscussLock = true;
                                CommentComponent.this.loadDiscussData();
                                CommentComponent.this.mInputView.setHintText("此资源的讨论组功能被锁定，不能发新内容");
                                CommentComponent.this.mInputView.setEnabled(false);
                                CommentComponent.this.mbEnableWrite = false;
                            } else {
                                CommentComponent.this.mbDiscussOpen = true;
                                CommentComponent.this.mbDiscussLock = false;
                                if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_DISCUSS_WRITE)) {
                                    CommentComponent.this.mInputView.setHintText("在这里输入新的主题...");
                                    CommentComponent.this.mInputView.setEnabled(true);
                                    CommentComponent.this.mbEnableWrite = true;
                                } else {
                                    CommentComponent.this.mInputView.setHintText("您当前没有权限发布新的主题");
                                    CommentComponent.this.mInputView.setEnabled(false);
                                    CommentComponent.this.mbEnableWrite = false;
                                }
                                CommentComponent.this.loadDiscussData();
                            }
                            if (CommentComponent.this.getContext() instanceof FragmentActivity) {
                                ((FragmentActivity) CommentComponent.this.getContext()).invalidateOptionsMenu();
                                return;
                            }
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                CommentComponent.this.mInputView.setHintText("此资源的讨论组功能没有开放");
                CommentComponent.this.mInputView.setEnabled(false);
                CommentComponent.this.mbEnableWrite = false;
                if (CommentComponent.this.getContext() instanceof FragmentActivity) {
                    ((FragmentActivity) CommentComponent.this.getContext()).invalidateOptionsMenu();
                }
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                CommentComponent.this.mInputView.setHintText("此资源的讨论组功能没有开放");
                CommentComponent.this.mInputView.setEnabled(false);
                CommentComponent.this.mbEnableWrite = false;
                CommentComponent.this.mbDiscussOpen = false;
                CommentComponent.this.mbDiscussLock = false;
                if (CommentComponent.this.getContext() instanceof FragmentActivity) {
                    ((FragmentActivity) CommentComponent.this.getContext()).invalidateOptionsMenu();
                }
            }
        });
        CallItem.setParam("lpszKey", "ResourceProperties_" + this.mObjectGUID);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public boolean getDiscussLock() {
        return this.mbDiscussLock;
    }

    public void setDiscussLock(boolean bLock) {
        this.mbDiscussLock = bLock;
        setDiscussState();
    }

    public void setDiscussOpen(boolean bOpen) {
        this.mbDiscussOpen = bOpen;
        setDiscussState();
    }

    private void setDiscussState() {
        PrivateDataItemObject ItemObject = new PrivateDataItemObject("ResourceProperties_" + this.mObjectGUID, UI.getCurrentActivity(), new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(UI.getCurrentActivity(), "配置成功", "讨论组的设置已成功更新");
                CommentComponent.this.checkDiscussOpen();
            }
        });
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("enableDiscuss", this.mbDiscussOpen);
            jsonObject.put("lockDiscuss", this.mbDiscussLock);
            ItemObject.setReadOperation(false);
            ItemObject.writeTextData(jsonObject.toString());
            VirtualNetworkObject.addToQueue(ItemObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean getDiscussOpen() {
        return this.mbDiscussOpen;
    }

    private void loadDiscussData() {
        this.mTextViewEmpty.setText("稍候...");
        this.mTextViewEmpty.setVisibility(0);
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussGetTopic", UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                ArrayList<String> arrGUIDs = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrDate = (ArrayList) ItemObject.getParam("2");
                ArrayList<String> arrMessage = (ArrayList) ItemObject.getParam("3");
                ArrayList<String> arrOwnerGUID = (ArrayList) ItemObject.getParam("1");
                ArrayList<Integer> arrCommentCount = Utilities.StringArrayToIntArray((ArrayList) ItemObject.getParam("4"));
                ArrayList<Integer> arrVoteCount = Utilities.StringArrayToIntArray((ArrayList) ItemObject.getParam("5"));
                if (arrGUIDs.size() > 0) {
                    CommentComponent.this.mTextViewEmpty.setVisibility(8);
                } else {
                    CommentComponent.this.mTextViewEmpty.setText("当前没有任何主题，请先发起一个主题。");
                }
                CommentComponent.this.mContentLayout.removeAllViews();
                int i = 0;
                while (i < arrGUIDs.size()) {
                    CustomCommentView CommentView = new CustomCommentView(CommentComponent.this.mContext, CommentComponent.this);
                    CommentView.setTopicGUID((String) arrGUIDs.get(i));
                    CommentView.setOwnerGUID((String) arrOwnerGUID.get(i));
                    CommentView.setData((String) arrDate.get(i), (String) arrMessage.get(i));
                    CommentView.setCommentCount(((Integer) arrCommentCount.get(i)).intValue());
                    CommentView.setVoteCount(((Integer) arrVoteCount.get(i)).intValue());
                    CommentView.setCanWrite(CommentComponent.this.mbEnableWrite);
                    if (CommentComponent.mbCanDeleteAll || ((String) arrOwnerGUID.get(i)).equalsIgnoreCase(CommentComponent.mOwnerGUID)) {
                        CommentView.setCanDelete(true);
                    }
                    CommentComponent.this.mContentLayout.addView(CommentView);
                    LayoutParams Param = (LayoutParams) CommentView.getLayoutParams();
                    Param.leftMargin = 0;
                    Param.rightMargin = 0;
                    Param.topMargin = 0;
                    if (i != arrGUIDs.size() - 1) {
                        Param.bottomMargin = 0;
                    } else {
                        Param.bottomMargin = 10;
                    }
                    CommentView.setLayoutParams(Param);
                    i++;
                }
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentComponent.this.mContext, "无法打开讨论", "无法获得对应的讨论数据。");
            }
        });
        ResourceObject.setParam("lpszObjectGUID", this.mObjectGUID);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    private void loadContent() {
        if (!this.mDataLoaded && this.mObjectGUID != null && !this.mObjectGUID.isEmpty()) {
            this.mContentLayout.removeAllViews();
            this.mTextViewEmpty.setVisibility(8);
            checkDiscussOpen();
            this.mDataLoaded = true;
        }
    }

    public void OnSendMessage(String szMessage, final String szHTMLMessage) {
        if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
            Utilities.showAlertMessageWithNotDisplayAgain(this.mContextThemeWrapper, "添加评论内容注意", "注意，您所添加的内容可以被您的任课老师看到且可以被删除，请勿添加老师不允许的内容，否则您的任课老师有权将您的账号设置为禁言");
        }
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussPostTopic", null);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                CommentComponent.this.mInputView.clearText();
                String szGUID = (String) ItemObject.getParam("0");
                CustomCommentView CommentView = new CustomCommentView(CommentComponent.this.mContext, CommentComponent.this);
                CommentView.setTopicGUID(szGUID);
                CommentView.setData(Utilities.getNow(), szHTMLMessage);
                CommentView.setOwnerGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                CommentView.setCommentCount(0);
                CommentView.setVoteCount(0);
                CommentView.setCanDelete(true);
                CommentComponent.this.mContentLayout.addView(CommentView, 0);
                CommentComponent.this.mTextViewEmpty.setVisibility(8);
                LayoutParams Param = (LayoutParams) CommentView.getLayoutParams();
                Param.leftMargin = 0;
                Param.rightMargin = 0;
                Param.topMargin = 0;
                if (CommentComponent.this.mContentLayout.getChildCount() == 1) {
                    Param.bottomMargin = 10;
                } else {
                    Param.bottomMargin = 0;
                }
                CommentView.setLayoutParams(Param);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentComponent.this.mContext, "新建主题出现错误", "新建主题时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("lpszObjectGUID", this.mObjectGUID);
        ResourceObject.setParam("lpszContent", szHTMLMessage);
        ResourceObject.setParam("nFlags", Integer.valueOf(0));
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    public boolean OnRequestTopicCommentList(final CustomCommentView View, String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussGetComment", UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                ArrayList<String> arrGUIDs = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrDate = (ArrayList) ItemObject.getParam("2");
                ArrayList<String> arrMessage = (ArrayList) ItemObject.getParam("3");
                ArrayList<String> arrOwnerGUID = (ArrayList) ItemObject.getParam("1");
                int i = 0;
                while (i < arrGUIDs.size()) {
                    boolean bCanDelete = false;
                    if (CommentComponent.mbCanDeleteAll || ((String) arrOwnerGUID.get(i)).equalsIgnoreCase(CommentComponent.mOwnerGUID)) {
                        bCanDelete = true;
                    }
                    View.addComment((String) arrGUIDs.get(i), (String) arrDate.get(i), (String) arrMessage.get(i), bCanDelete, false, (String) arrOwnerGUID.get(i));
                    i++;
                }
            }
        });
        ResourceObject.setParam("lpszTopicGUID", szTopicGUID);
        VirtualNetworkObject.addToQueue(ResourceObject);
        return true;
    }

    public boolean OnPostNewComment(final CustomCommentView View, String szTopicGUID, String szMessage, final String szHTMLMessage) {
        if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
            Utilities.showAlertMessageWithNotDisplayAgain(this.mContextThemeWrapper, "添加评论内容注意", "注意，您所添加的内容可以被您的任课老师看到且可以被删除，请勿添加老师不允许的内容，否则您的任课老师有权将您的账号设置为禁言");
        }
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussPostComment", UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                View.addComment((String) ItemObject.getParam("0"), Utilities.getNow(), szHTMLMessage, true, true, CommentComponent.mOwnerGUID);
                View.setCommentCount(View.getCommentCount() + 1);
                View.clearCommentInput();
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentComponent.this.mContext, "提交评论出现错误", "提交评论时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszTopicGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("lpszContent", szHTMLMessage);
        VirtualNetworkObject.addToQueue(ResourceObject);
        return true;
    }

    public boolean OnVote(final CustomCommentView View, final String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussVote", UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                View.setVoteCount(View.getVoteCount() + 1);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                if (nReturnCode == ErrorCode.ERROR_ALREADY_EXISTS) {
                    final CustomCommentView customCommentView = View;
                    final String str = szTopicGUID;
                    new Builder(CommentComponent.this.mContext).setTitle("您已经赞过了").setMessage("您已经赞过这个主题了，不能重复点赞。").setPositiveButton("确定", null).setNeutralButton("取消赞", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CommentComponent.this.OnUnVote(customCommentView, str);
                            dialog.dismiss();
                        }
                    }).show();
                    return;
                }
                Utilities.showAlertMessage(CommentComponent.this.mContext, "提交好评时出现错误", "提交好评时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszObjectGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("nObjectType", Integer.valueOf(5));
        ResourceObject.setParam("bCancel", Integer.valueOf(0));
        VirtualNetworkObject.addToQueue(ResourceObject);
        return true;
    }

    public void OnUnVote(final CustomCommentView View, String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussVote", UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                View.setVoteCount(View.getVoteCount() - 1);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentComponent.this.mContext, "取消好评时出现错误", "取消好评时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszObjectGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("nObjectType", Integer.valueOf(5));
        ResourceObject.setParam("bCancel", Integer.valueOf(1));
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    public boolean OnDeleteComment(final CustomCommentView View, String szTopicGUID, final String szCommentGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussDeleteComment", UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                View.deleteComment(szCommentGUID);
                View.setCommentCount(View.getCommentCount() - 1);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentComponent.this.mContext, "删除评论出现错误", "删除评论时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszTopicGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("lpszCommentGUID", szCommentGUID);
        VirtualNetworkObject.addToQueue(ResourceObject);
        return false;
    }

    public boolean OnDeleteTopic(final CustomCommentView View, String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussDeleteTopic", UI.getCurrentActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                CommentComponent.this.mContentLayout.removeView(View);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentComponent.this.mContext, "删除主题出现错误", "删除主题时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszTopicGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("lpszObjectGUID", this.mObjectGUID);
        VirtualNetworkObject.addToQueue(ResourceObject);
        return false;
    }

    public void setData(String szData) {
        this.mObjectGUID = szData;
        if (this.mbAttachedToWindow && getLocalVisibleRect(new Rect())) {
            loadContent();
        }
    }

    protected void onAttachedToWindow() {
        this.mbAttachedToWindow = true;
        if (getLocalVisibleRect(new Rect())) {
            loadContent();
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
        super.onDetachedFromWindow();
    }

    public void onScrollChanged() {
        if (getLocalVisibleRect(new Rect()) && !this.mDataLoaded) {
            loadContent();
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                vto.removeOnScrollChangedListener(this);
            }
        }
    }

    public String getData() {
        return null;
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }
}
