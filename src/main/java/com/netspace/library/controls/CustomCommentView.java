package com.netspace.library.controls;

import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.controls.CustomChatInputView.CustomChatCallBack;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;

public class CustomCommentView extends LinearLayout implements OnClickListener, CustomChatCallBack, OnLongClickListener {
    private CommentViewCallBack mCallBack;
    private CustomChatDisplayView mChatDisplayView;
    private CustomChatInputView mChatInputView;
    private int mCommentCount = 0;
    private LinearLayout mCommentLayout;
    private Context mContext;
    private LinearLayout mExsitCommentLayout;
    private ImageView mImageViewPictureVote;
    private String mOwnerGUID = "";
    private TextView mTextViewComment;
    private TextView mTextViewUser;
    private TextView mTextViewVoteCount;
    private String mTopicGUID;
    private int mVoteCount = 0;
    private boolean mbCanDelete = false;
    private boolean mbCanWrite = true;

    public interface CommentViewCallBack {
        boolean OnDeleteComment(CustomCommentView customCommentView, String str, String str2);

        boolean OnDeleteTopic(CustomCommentView customCommentView, String str);

        boolean OnPostNewComment(CustomCommentView customCommentView, String str, String str2, String str3);

        boolean OnRequestTopicCommentList(CustomCommentView customCommentView, String str);

        boolean OnVote(CustomCommentView customCommentView, String str);
    }

    public CustomCommentView(Context context, CommentViewCallBack CallBack) {
        super(context);
        this.mContext = context;
        this.mCallBack = CallBack;
        this.mChatDisplayView = new CustomChatDisplayView(context);
        this.mChatInputView = new CustomChatInputView(context);
        this.mChatInputView.setCallBack(this);
        this.mChatInputView.setHintText("在这里输入新的评论...");
        this.mChatDisplayView.setAllowImageReedit(true, this.mChatInputView.getFingerCallBack());
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customcommentview, this);
        this.mCommentLayout = (LinearLayout) findViewById(R.id.LinearLayoutComments);
        this.mCommentLayout.setVisibility(8);
        this.mTextViewUser = (TextView) findViewById(R.id.textViewMessageFrom);
        this.mExsitCommentLayout = (LinearLayout) findViewById(R.id.LinearLayoutExsitComment);
        this.mTextViewComment = (TextView) findViewById(R.id.textViewComment);
        this.mTextViewComment.setOnClickListener(this);
        this.mTextViewVoteCount = (TextView) findViewById(R.id.textViewVoteCount);
        this.mTextViewVoteCount.setOnClickListener(this);
        this.mImageViewPictureVote = (ImageView) findViewById(R.id.imageViewPictureVote);
        this.mImageViewPictureVote.setOnClickListener(this);
    }

    public void setTopicGUID(String szGUID) {
        this.mTopicGUID = szGUID;
    }

    public void setOwnerGUID(String szOwnerGUID) {
        this.mOwnerGUID = szOwnerGUID;
    }

    public void setCanWrite(boolean bCanWrite) {
        this.mbCanWrite = bCanWrite;
        this.mChatDisplayView.setAllowImageReedit(this.mbCanWrite, this.mChatInputView.getFingerCallBack());
    }

    public void setCanDelete(boolean bCanDelete) {
        this.mbCanDelete = bCanDelete;
        if (this.mbCanDelete) {
            setOnLongClickListener(this);
        } else {
            setOnLongClickListener(null);
        }
    }

    public void setData(String szDate, String szMessage) {
        this.mChatDisplayView.setData(szDate, szMessage, this);
        this.mTextViewUser.setTextColor(-16777216);
    }

    public void setCommentCount(int nCount) {
        if (nCount > 0) {
            this.mTextViewComment.setText("评论 " + String.valueOf(nCount));
        } else {
            this.mTextViewComment.setText("评论");
        }
        this.mCommentCount = nCount;
    }

    public int getCommentCount() {
        return this.mCommentCount;
    }

    public void setVoteCount(int nCount) {
        if (nCount > 0) {
            this.mTextViewVoteCount.setText(String.valueOf(nCount));
        } else {
            this.mTextViewVoteCount.setText("");
        }
        this.mVoteCount = nCount;
    }

    public int getVoteCount() {
        return this.mVoteCount;
    }

    public void clearCommentInput() {
        this.mChatInputView.clearText();
    }

    public boolean addComment(String szGUID, String szDate, String szText, boolean bCanDelete, boolean bToTop, String szOwnerGUID) {
        if (this.mCommentLayout.getVisibility() != 0) {
            return false;
        }
        CustomChatDisplayView ChatDisplayView = new CustomChatDisplayView(this.mContext);
        ChatDisplayView.setData(szDate, szText, null);
        ChatDisplayView.setGUID(szGUID);
        ChatDisplayView.setOwnerGUID(szOwnerGUID);
        ChatDisplayView.setAllowImageReedit(this.mbCanWrite, this.mChatInputView.getFingerCallBack());
        if (bCanDelete) {
            ChatDisplayView.setOnLongClickListener(this);
        }
        if (bToTop) {
            this.mExsitCommentLayout.addView(ChatDisplayView, 0);
        } else {
            this.mExsitCommentLayout.addView(ChatDisplayView);
        }
        return true;
    }

    public boolean deleteComment(String szGUID) {
        for (int i = 0; i < this.mExsitCommentLayout.getChildCount(); i++) {
            View OneView = this.mExsitCommentLayout.getChildAt(i);
            if ((OneView instanceof CustomChatDisplayView) && ((CustomChatDisplayView) OneView).getGUID().equalsIgnoreCase(szGUID)) {
                this.mExsitCommentLayout.removeViewAt(i);
                return true;
            }
        }
        return false;
    }

    public void onClick(View v) {
        Utilities.logClick(v, "CustomCommentView");
        if (v.getId() == R.id.textViewComment) {
            if (this.mCommentLayout.getVisibility() == 8) {
                this.mCommentLayout.setVisibility(0);
                if (this.mbCanWrite) {
                    this.mCommentLayout.addView(this.mChatInputView, 1);
                    ((LayoutParams) this.mChatInputView.getLayoutParams()).leftMargin = Utilities.dpToPixel(32, this.mContext);
                }
                if (this.mCallBack != null) {
                    this.mCallBack.OnRequestTopicCommentList(this, this.mTopicGUID);
                    return;
                }
                return;
            }
            this.mExsitCommentLayout.removeAllViews();
            this.mCommentLayout.removeView(this.mChatInputView);
            this.mCommentLayout.setVisibility(8);
        } else if ((v.getId() == R.id.imageViewPictureVote || v.getId() == R.id.textViewVoteCount) && this.mbCanWrite && this.mCallBack != null) {
            this.mCallBack.OnVote(this, this.mTopicGUID);
        }
    }

    public void OnSendMessage(String szMessage, String szHTMLMessage) {
        if (this.mCallBack != null) {
            this.mCallBack.OnPostNewComment(this, this.mTopicGUID, szMessage, szHTMLMessage);
        }
    }

    public boolean onLongClick(View v) {
        if (!this.mbCanWrite) {
            return false;
        }
        PopupMenu popup;
        if (v instanceof CustomChatDisplayView) {
            final CustomChatDisplayView Target = (CustomChatDisplayView) v;
            final String szGUID = Target.getGUID();
            final String szOwnerGUID = Target.getOwnerGUID();
            popup = new PopupMenu(this.mContext, v);
            popup.getMenuInflater().inflate(R.menu.menu_discusscomment, popup.getMenu());
            if (szOwnerGUID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID) || MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
                popup.getMenu().removeItem(R.id.menuBlock);
            }
            if (!Target.getIsText()) {
                popup.getMenu().removeItem(R.id.menuCopy);
            }
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    final String str;
                    if (item.getItemId() == R.id.menuDelete) {
                        if (CustomCommentView.this.mCallBack != null) {
                            str = szGUID;
                            new Builder(CustomCommentView.this.mContext).setTitle("删除评论").setMessage("确实删除此评论吗？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    CustomCommentView.this.mCallBack.OnDeleteComment(CustomCommentView.this, CustomCommentView.this.mTopicGUID, str);
                                }
                            }).setNegativeButton("否", null).show();
                        }
                    } else if (item.getItemId() == R.id.menuCopy) {
                        ((ClipboardManager) CustomCommentView.this.mContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("text label", Target.getTextContent()));
                    } else if (item.getItemId() == R.id.menuBlock) {
                        str = szOwnerGUID;
                        new Builder(CustomCommentView.this.mContext).setTitle("禁言").setMessage("确实要将作者禁言吗？后期您可以在学生信息里将被禁言的学生恢复发言权。").setPositiveButton("是", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                WebServiceCallItemObject CallItem = new WebServiceCallItemObject("UsersSetACL", null);
                                CallItem.setSuccessListener(new OnSuccessListener() {
                                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                        Utilities.showAlertMessage(CustomCommentView.this.mContext, "成功设置禁言", "已成功设置禁言。");
                                    }
                                });
                                CallItem.setFailureListener(new OnFailureListener() {
                                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                        Utilities.showAlertMessage(CustomCommentView.this.mContext, "设置禁言出现错误", "设置禁言时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                                    }
                                });
                                CallItem.setParam("lpszUserNameOrGUID", str);
                                CallItem.setParam("lpszFieldName", Features.PERMISSION_DISCUSS_WRITE);
                                CallItem.setParam("lpszFieldValue", "off");
                                CallItem.setAlwaysActiveCallbacks(true);
                                VirtualNetworkObject.addToQueue(CallItem);
                            }
                        }).setNegativeButton("否", null).show();
                    }
                    return false;
                }
            });
            popup.show();
        } else if (v.equals(this)) {
            popup = new PopupMenu(this.mContext, v);
            popup.getMenuInflater().inflate(R.menu.menu_discusstopic, popup.getMenu());
            if (this.mOwnerGUID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID) || MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
                popup.getMenu().removeItem(R.id.menuBlock);
            }
            if (!this.mChatDisplayView.getIsText()) {
                popup.getMenu().removeItem(R.id.menuCopy);
            }
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.menuDelete) {
                        if (CustomCommentView.this.mCallBack != null) {
                            new Builder(CustomCommentView.this.mContext).setTitle("删除主题").setMessage("确实删除此主题吗？这主题下的所有评论也将被一起删除").setPositiveButton("是", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    CustomCommentView.this.mCallBack.OnDeleteTopic(CustomCommentView.this, CustomCommentView.this.mTopicGUID);
                                }
                            }).setNegativeButton("否", null).show();
                        }
                    } else if (item.getItemId() == R.id.menuCopy) {
                        ((ClipboardManager) CustomCommentView.this.mContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("text label", CustomCommentView.this.mChatDisplayView.getTextContent()));
                    } else if (item.getItemId() == R.id.menuBlock) {
                        new Builder(CustomCommentView.this.mContext).setTitle("禁言").setMessage("确实要将作者禁言吗？后期您可以在学生信息里将被禁言的学生恢复发言权。").setPositiveButton("是", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                WebServiceCallItemObject CallItem = new WebServiceCallItemObject("UsersSetACL", null);
                                CallItem.setSuccessListener(new OnSuccessListener() {
                                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                        Utilities.showAlertMessage(CustomCommentView.this.mContext, "成功设置禁言", "已成功设置禁言。");
                                    }
                                });
                                CallItem.setFailureListener(new OnFailureListener() {
                                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                        Utilities.showAlertMessage(CustomCommentView.this.mContext, "设置禁言出现错误", "设置禁言时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                                    }
                                });
                                CallItem.setParam("lpszUserNameOrGUID", CustomCommentView.this.mOwnerGUID);
                                CallItem.setParam("lpszFieldName", Features.PERMISSION_DISCUSS_WRITE);
                                CallItem.setParam("lpszFieldValue", "off");
                                CallItem.setAlwaysActiveCallbacks(true);
                                VirtualNetworkObject.addToQueue(CallItem);
                            }
                        }).setNegativeButton("否", null).show();
                    }
                    return false;
                }
            });
            popup.show();
        }
        return true;
    }
}
