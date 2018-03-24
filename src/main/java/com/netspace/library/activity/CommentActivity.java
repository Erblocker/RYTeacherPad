package com.netspace.library.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomChatInputView;
import com.netspace.library.controls.CustomChatInputView.CustomChatCallBack;
import com.netspace.library.controls.CustomCommentView;
import com.netspace.library.controls.CustomCommentView.CommentViewCallBack;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import net.sqlcipher.database.SQLiteDatabase;

public class CommentActivity extends Activity implements CustomChatCallBack, CommentViewCallBack {
    private static String mOwnerGUID = "";
    private static boolean mbCanDeleteAll = false;
    private LinearLayout mContentLayout;
    private Context mContext;
    private CustomChatInputView mInputView;
    private String mObjectGUID = "";
    private LinearLayout mRootLayout;
    private TextView mTextViewEmpty;

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

    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_comment);
        super.onCreate(savedInstanceState);
        this.mContentLayout = (LinearLayout) findViewById(R.id.LinearLayoutContent);
        this.mRootLayout = (LinearLayout) findViewById(R.id.LinearLayoutRoot);
        this.mInputView = new CustomChatInputView(this);
        this.mInputView.setCallBack(this);
        this.mRootLayout.addView(this.mInputView, 0);
        this.mTextViewEmpty = (TextView) findViewById(R.id.textViewEmpty);
        this.mContext = this;
        String szTitle = "讨论";
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("objectGUID")) {
                this.mObjectGUID = getIntent().getExtras().getString("objectGUID");
            }
            if (getIntent().getExtras().containsKey("title")) {
                szTitle = getIntent().getExtras().getString("title");
            }
        }
        this.mInputView = new CustomChatInputView(this);
        this.mInputView.setCallBack(this);
        setTitle(szTitle);
        this.mInputView.setHintText("在这里输入新的主题...");
        loadContent();
    }

    private void loadContent() {
        this.mContentLayout.removeAllViews();
        this.mTextViewEmpty.setText("稍候...");
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussGetTopic", this);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                ArrayList<String> arrGUIDs = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrDate = (ArrayList) ItemObject.getParam("2");
                ArrayList<String> arrMessage = (ArrayList) ItemObject.getParam("3");
                ArrayList<String> arrOwnerGUID = (ArrayList) ItemObject.getParam("1");
                ArrayList<Integer> arrCommentCount = Utilities.StringArrayToIntArray((ArrayList) ItemObject.getParam("4"));
                ArrayList<Integer> arrVoteCount = Utilities.StringArrayToIntArray((ArrayList) ItemObject.getParam("5"));
                if (arrGUIDs.size() > 0) {
                    CommentActivity.this.mTextViewEmpty.setVisibility(8);
                } else {
                    CommentActivity.this.mTextViewEmpty.setText("当前没有任何主题，请先发起一个主题。");
                }
                int i = 0;
                while (i < arrGUIDs.size()) {
                    CustomCommentView CommentView = new CustomCommentView(CommentActivity.this.mContext, CommentActivity.this);
                    CommentView.setTopicGUID((String) arrGUIDs.get(i));
                    CommentView.setOwnerGUID((String) arrOwnerGUID.get(i));
                    CommentView.setData((String) arrDate.get(i), (String) arrMessage.get(i));
                    CommentView.setCommentCount(((Integer) arrCommentCount.get(i)).intValue());
                    CommentView.setVoteCount(((Integer) arrVoteCount.get(i)).intValue());
                    if (CommentActivity.mbCanDeleteAll || ((String) arrOwnerGUID.get(i)).equalsIgnoreCase(CommentActivity.mOwnerGUID)) {
                        CommentView.setCanDelete(true);
                    }
                    CommentActivity.this.mContentLayout.addView(CommentView);
                    LayoutParams Param = (LayoutParams) CommentView.getLayoutParams();
                    Param.leftMargin = 10;
                    Param.rightMargin = 10;
                    Param.topMargin = 10;
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
                Utilities.showAlertMessage(CommentActivity.this.mContext, "无法打开讨论", "无法获得对应的讨论数据");
            }
        });
        ResourceObject.setParam("lpszObjectGUID", this.mObjectGUID);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    public void OnSendMessage(String szMessage, final String szHTMLMessage) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussPostTopic", this);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                CommentActivity.this.mInputView.clearText();
                String szGUID = (String) ItemObject.getParam("0");
                CustomCommentView CommentView = new CustomCommentView(CommentActivity.this.mContext, CommentActivity.this);
                CommentView.setTopicGUID(szGUID);
                CommentView.setData(Utilities.getNow(), szHTMLMessage);
                CommentView.setOwnerGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                CommentView.setCommentCount(0);
                CommentView.setVoteCount(0);
                CommentView.setCanDelete(true);
                CommentActivity.this.mContentLayout.addView(CommentView, 0);
                CommentActivity.this.mTextViewEmpty.setVisibility(8);
                LayoutParams Param = (LayoutParams) CommentView.getLayoutParams();
                Param.leftMargin = 10;
                Param.rightMargin = 10;
                Param.topMargin = 10;
                if (CommentActivity.this.mContentLayout.getChildCount() == 1) {
                    Param.bottomMargin = 10;
                } else {
                    Param.bottomMargin = 0;
                }
                CommentView.setLayoutParams(Param);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentActivity.this.mContext, "新建主题出现错误", "新建主题时出现错误，请尝试重试");
            }
        });
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("lpszObjectGUID", this.mObjectGUID);
        ResourceObject.setParam("lpszContent", szHTMLMessage);
        ResourceObject.setParam("nFlags", Integer.valueOf(0));
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    public boolean OnRequestTopicCommentList(final CustomCommentView View, String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussGetComment", this);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                ArrayList<String> arrGUIDs = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrDate = (ArrayList) ItemObject.getParam("2");
                ArrayList<String> arrMessage = (ArrayList) ItemObject.getParam("3");
                ArrayList<String> arrOwnerGUID = (ArrayList) ItemObject.getParam("1");
                int i = 0;
                while (i < arrGUIDs.size()) {
                    boolean bCanDelete = false;
                    if (CommentActivity.mbCanDeleteAll || ((String) arrOwnerGUID.get(i)).equalsIgnoreCase(CommentActivity.mOwnerGUID)) {
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
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussPostComment", this);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                View.addComment((String) ItemObject.getParam("0"), Utilities.getNow(), szHTMLMessage, true, true, CommentActivity.mOwnerGUID);
                View.setCommentCount(View.getCommentCount() + 1);
                View.clearCommentInput();
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentActivity.this.mContext, "提交评论出现错误", "提交评论时出现错误，请尝试重试");
            }
        });
        ResourceObject.setParam("lpszTopicGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("lpszContent", szHTMLMessage);
        VirtualNetworkObject.addToQueue(ResourceObject);
        return true;
    }

    public boolean OnVote(final CustomCommentView View, final String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussVote", this);
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
                    new Builder(CommentActivity.this.mContext).setTitle("您已经赞过了").setMessage("您已经赞过这个主题了，不能重复点赞。").setPositiveButton("确定", null).setNeutralButton("取消赞", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CommentActivity.this.OnUnVote(customCommentView, str);
                            dialog.dismiss();
                        }
                    }).show();
                    return;
                }
                Utilities.showAlertMessage(CommentActivity.this.mContext, "提交好评时出现错误", "提交好评时出现错误，请尝试重试。");
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
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussVote", this);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                View.setVoteCount(View.getVoteCount() - 1);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentActivity.this.mContext, "取消好评时出现错误", "取消好评时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszObjectGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("nObjectType", Integer.valueOf(5));
        ResourceObject.setParam("bCancel", Integer.valueOf(1));
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    public boolean OnDeleteComment(final CustomCommentView View, String szTopicGUID, final String szCommentGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussDeleteComment", this);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                View.deleteComment(szCommentGUID);
                View.setCommentCount(View.getCommentCount() - 1);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentActivity.this.mContext, "删除评论出现错误", "删除评论时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszTopicGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("lpszCommentGUID", szCommentGUID);
        VirtualNetworkObject.addToQueue(ResourceObject);
        return false;
    }

    public boolean OnDeleteTopic(final CustomCommentView View, String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussDeleteTopic", this);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                CommentActivity.this.mContentLayout.removeView(View);
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(CommentActivity.this.mContext, "删除主题出现错误", "删除主题时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszTopicGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", mOwnerGUID);
        ResourceObject.setParam("lpszObjectGUID", this.mObjectGUID);
        VirtualNetworkObject.addToQueue(ResourceObject);
        return false;
    }
}
