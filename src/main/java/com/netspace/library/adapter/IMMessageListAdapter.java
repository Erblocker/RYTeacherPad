package com.netspace.library.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.netspace.library.activity.ChatActivity;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.dialog.ShareToDialog;
import com.netspace.library.dialog.ShareToDialog.ShareDialogCallBack;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.im.IMCore;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.FaceImageGetter;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import io.vov.vitamio.utils.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import net.sqlcipher.database.SQLiteDatabase;

public class IMMessageListAdapter extends BaseAdapter {
    private String mCurrentUserName;
    private OnClickListener mImageClick = new OnClickListener() {
        public void onClick(View v) {
            if (IMMessageListAdapter.this.mUserImageClickListener == null || !IMMessageListAdapter.this.mUserImageClickListener.onUserImageClick((String) v.getTag())) {
                WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("GetTemporaryStorage", null);
                ResourceObject.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        String szKey = (String) ItemObject.getParam("lpszKey");
                        Bitmap Bitmap = Utilities.getBase64Bitmap(ItemObject.readTextData());
                        if (Bitmap != null) {
                            int nImageWidth = Bitmap.getWidth();
                            int nImageHeight = Bitmap.getHeight();
                            OutputStream stream = null;
                            try {
                                stream = new FileOutputStream(new StringBuilder(String.valueOf(IMMessageListAdapter.this.m_Context.getExternalCacheDir().getAbsolutePath())).append("/").append(szKey).append(".jpg").toString());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            if (!(stream == null || Bitmap == null)) {
                                Bitmap.compress(CompressFormat.JPEG, 100, stream);
                            }
                            Intent DrawActivity = new Intent(IMMessageListAdapter.this.m_Context, FingerDrawActivity.class);
                            DrawActivity.putExtra("imageKey", szKey);
                            DrawActivity.putExtra("imageWidth", nImageWidth);
                            DrawActivity.putExtra("imageHeight", nImageHeight);
                            DrawActivity.putExtra("allowUpload", false);
                            DrawActivity.putExtra("readonly", true);
                            DrawActivity.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                            IMMessageListAdapter.this.m_Context.startActivity(DrawActivity);
                            return;
                        }
                        Utilities.showAlertMessage(IMMessageListAdapter.this.m_Context, "无法打开图片", "图片数据无效。");
                    }
                });
                ResourceObject.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        Utilities.showAlertMessage(IMMessageListAdapter.this.m_Context, "无法打开图片", "无法打开对应的图片。");
                    }
                });
                ResourceObject.setParam("lpszKey", (String) v.getTag());
                ResourceObject.setAlwaysActiveCallbacks(true);
                VirtualNetworkObject.addToQueue(ResourceObject);
            }
        }
    };
    private ImageView mLastPlayView;
    private ListView mListView;
    private OnOpenRelatedClickListener mOpenRelatedClickListener;
    private OnClickListener mPlayClick = new OnClickListener() {
        public void onClick(View v) {
            if (IMMessageListAdapter.this.mPlayer != null) {
                IMMessageListAdapter.this.mPlayer.stop();
                IMMessageListAdapter.this.mPlayer.release();
                IMMessageListAdapter.this.mPlayer = null;
                if (IMMessageListAdapter.this.mLastPlayView != null) {
                    IMMessageListAdapter.this.mLastPlayView.setImageResource(R.drawable.ic_playback);
                    return;
                }
                return;
            }
            IMMessageListAdapter.this.mLastPlayView = (ImageView) v;
            WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("GetTemporaryStorage", null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    ItemObject DataObject = ItemObject;
                    String szFileName = new StringBuilder(String.valueOf(IMMessageListAdapter.this.m_Context.getExternalCacheDir().getAbsolutePath())).append("/").append((String) ItemObject.getParam("lpszKey")).toString();
                    if (Utilities.writeBase64ToFile(DataObject.readTextData(), szFileName)) {
                        final String szTempFile = szFileName;
                        IMMessageListAdapter.this.mPlayer = MediaPlayer.create(IMMessageListAdapter.this.m_Context, Uri.fromFile(new File(szFileName)));
                        IMMessageListAdapter.this.mPlayer.setOnCompletionListener(new OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                IMMessageListAdapter.this.mPlayer.release();
                                IMMessageListAdapter.this.mLastPlayView.setImageResource(R.drawable.ic_playback);
                                new File(szTempFile).delete();
                                IMMessageListAdapter.this.mPlayer = null;
                                IMMessageListAdapter.this.mLastPlayView = null;
                            }
                        });
                        IMMessageListAdapter.this.mPlayer.start();
                        IMMessageListAdapter.this.mLastPlayView.setImageResource(R.drawable.ic_stopplayback);
                    }
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showAlertMessage(IMMessageListAdapter.this.m_Context, "无法播放", "无法获得对应的音频数据。");
                }
            });
            ResourceObject.setParam("lpszKey", (String) v.getTag());
            ResourceObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
        }
    };
    private MediaPlayer mPlayer;
    private OnUserImageClickListener mUserImageClickListener;
    private Context m_Context;
    private LayoutInflater m_LayoutInflater;
    private ArrayList<IMMessageItem> m_arrData;
    private boolean mbEnableVote = false;

    public class IMMessageItem {
        public boolean bSearchResult;
        public String szDate;
        public String szGUID;
        public String szMessage;
        public String szName;
    }

    public interface OnOpenRelatedClickListener {
        boolean onOpenRelatedMessageClick(String str);
    }

    public interface OnUserImageClickListener {
        boolean onUserImageClick(String str);
    }

    public IMMessageListAdapter(Context context, ListView ListView, String szCurrentUserName) {
        this.m_Context = context;
        this.m_LayoutInflater = (LayoutInflater) this.m_Context.getSystemService("layout_inflater");
        this.m_arrData = new ArrayList();
        this.mListView = ListView;
        this.mCurrentUserName = szCurrentUserName;
    }

    public void add(String szName, String szMessage, String szDate) {
        IMMessageItem NewItem = new IMMessageItem();
        NewItem.szName = szName;
        NewItem.szMessage = szMessage;
        NewItem.szDate = szDate;
        if (NewItem.szMessage.startsWith("SERVERTIME=")) {
            int nPos = NewItem.szMessage.indexOf(";");
            NewItem.szDate = NewItem.szMessage.substring(11, nPos);
            NewItem.szMessage = NewItem.szMessage.substring(nPos + 1);
        }
        this.m_arrData.add(NewItem);
    }

    public void addToTop(String szName, String szMessage, String szDate) {
        IMMessageItem NewItem = new IMMessageItem();
        NewItem.szName = szName;
        NewItem.szMessage = szMessage;
        NewItem.szDate = szDate;
        if (NewItem.szMessage.startsWith("SERVERTIME=")) {
            int nPos = NewItem.szMessage.indexOf(";");
            NewItem.szDate = NewItem.szMessage.substring(11, nPos);
            NewItem.szMessage = NewItem.szMessage.substring(nPos + 1);
        }
        this.m_arrData.add(0, NewItem);
    }

    public void addSearchResult(String szGUID, String szName, String szMessage, String szDate) {
        IMMessageItem NewItem = new IMMessageItem();
        NewItem.szName = szName;
        NewItem.szMessage = szMessage;
        NewItem.szDate = szDate;
        NewItem.szGUID = szGUID;
        NewItem.bSearchResult = true;
        if (NewItem.szMessage.startsWith("SERVERTIME=")) {
            int nPos = NewItem.szMessage.indexOf(";");
            NewItem.szDate = NewItem.szMessage.substring(11, nPos);
            NewItem.szMessage = NewItem.szMessage.substring(nPos + 1);
        }
        this.m_arrData.add(NewItem);
    }

    public void removeTopMessage() {
        if (this.m_arrData.size() > 0) {
            this.m_arrData.remove(0);
        }
    }

    public void removeSearchResultMessage() {
        int i = 0;
        while (i < this.m_arrData.size()) {
            if (((IMMessageItem) this.m_arrData.get(i)).bSearchResult) {
                this.m_arrData.remove(i);
                i--;
            }
            i++;
        }
    }

    public void removeLastMessage() {
        if (this.m_arrData.size() > 0) {
            this.m_arrData.remove(this.m_arrData.size() - 1);
        }
    }

    public void clear() {
        this.m_arrData.clear();
    }

    public int getCount() {
        return this.m_arrData.size();
    }

    public Object getItem(int position) {
        return this.m_arrData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void setEnableVote(boolean bEnable) {
        this.mbEnableVote = bEnable;
    }

    public void setUserImageClickListener(OnUserImageClickListener Listener) {
        this.mUserImageClickListener = Listener;
    }

    public void setOpenRelatedClickListener(OnOpenRelatedClickListener Listener) {
        this.mOpenRelatedClickListener = Listener;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        String szVoteGUID = null;
        final IMMessageItem Data = (IMMessageItem) this.m_arrData.get(position);
        boolean bTextMessage = false;
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_immessage, null);
        }
        TextView textUserName = (TextView) convertView.findViewById(R.id.textViewMessageFrom);
        TextView textMessage = (TextView) convertView.findViewById(R.id.textViewMessageText);
        TextView textDate = (TextView) convertView.findViewById(R.id.textViewMessageTime);
        ImageView imageViewVoice = (ImageView) convertView.findViewById(R.id.imageViewVoice);
        ImageView imageViewPicture = (ImageView) convertView.findViewById(R.id.imageViewPicture);
        ImageView imageViewThumbnail = (ImageView) convertView.findViewById(R.id.imageViewThumbnail);
        LinearLayout LayoutVoice = (LinearLayout) convertView.findViewById(R.id.LinearLayoutVoice);
        TextView textVoiceTime = (TextView) convertView.findViewById(R.id.textViewTime);
        TextView textIntent = (TextView) convertView.findViewById(R.id.textViewIntentTitle);
        LinearLayout LayoutExternalIntent = (LinearLayout) convertView.findViewById(R.id.LinearLayoutExternalIntent);
        LinearLayout LayoutMessage = (LinearLayout) convertView.findViewById(R.id.LinearLayoutMessage);
        final RelativeLayout LayoutVote = (RelativeLayout) convertView.findViewById(R.id.RelativeLayoutVote);
        final TextView textVoteCount = (TextView) convertView.findViewById(R.id.textViewVoteCount);
        imageViewThumbnail.setVisibility(8);
        LayoutVote.setVisibility(8);
        int nEndPos;
        int nStartPos;
        if (Data.szMessage.startsWith("VOICE=") && Data.szMessage.indexOf(".mp3") != -1) {
            LayoutVoice.setVisibility(0);
            textMessage.setVisibility(8);
            imageViewPicture.setVisibility(8);
            LayoutExternalIntent.setVisibility(8);
            nEndPos = Data.szMessage.indexOf(".mp3") + 4;
            szVoteGUID = Data.szMessage.substring(6, nEndPos);
            imageViewVoice.setTag(Data.szMessage.substring(6, nEndPos));
            imageViewVoice.setOnClickListener(this.mPlayClick);
            if (this.mLastPlayView != imageViewVoice) {
                imageViewVoice.setImageResource(R.drawable.ic_playback);
            } else {
                imageViewVoice.setImageResource(R.drawable.ic_stopplayback);
            }
            nStartPos = Data.szMessage.indexOf("TIME=");
            if (nStartPos != -1) {
                nStartPos += 6;
                textVoiceTime.setText(Data.szMessage.substring(nStartPos, Data.szMessage.indexOf(";", nStartPos)));
                textVoiceTime.setVisibility(0);
            } else {
                textVoiceTime.setVisibility(4);
            }
        } else if (Data.szMessage.startsWith("PICTURE=") && Data.szMessage.indexOf(".jpg") != -1) {
            LayoutVoice.setVisibility(8);
            textMessage.setVisibility(8);
            imageViewPicture.setVisibility(0);
            LayoutExternalIntent.setVisibility(8);
            nEndPos = Data.szMessage.indexOf(".jpg") + 4;
            if (nEndPos > 8) {
                szVoteGUID = Data.szMessage.substring(8, nEndPos);
                imageViewPicture.setTag(Data.szMessage.substring(8, nEndPos));
                imageViewPicture.setOnClickListener(this.mImageClick);
                nStartPos = Data.szMessage.indexOf("THUMBNAIL=");
                if (nStartPos != -1) {
                    nStartPos += 10;
                    Bitmap bitmap = Utilities.getBase64Bitmap(Data.szMessage.substring(nStartPos, Data.szMessage.indexOf(";", nStartPos)).replace("<br>", "\n"));
                    if (bitmap != null) {
                        imageViewPicture.setImageBitmap(bitmap);
                    }
                } else {
                    imageViewPicture.setImageResource(R.drawable.ic_placehold_picture);
                }
            }
        } else if (Data.szMessage.startsWith("INTENT=")) {
            LayoutVoice.setVisibility(8);
            textMessage.setVisibility(8);
            imageViewPicture.setVisibility(8);
            LayoutExternalIntent.setVisibility(0);
            LayoutExternalIntent.setOnClickListener(null);
            String szIntentData = Data.szMessage.substring(7);
            String szTitle = "";
            try {
                Intent intent = Intent.parseUri(szIntentData, 0);
                if (intent == null || intent.getComponent() == null) {
                    textIntent.setText("数据解析错误");
                    Log.d("IMMessageListAdapter", "Intent.parserUri return null while process " + szIntentData);
                    Utilities.putACRAData("Intent.parseUri return null while process " + szIntentData);
                } else {
                    intent.setClassName(this.m_Context, intent.getComponent().getClassName());
                    szTitle = intent.getStringExtra("title");
                    szVoteGUID = intent.getStringExtra(CommentComponent.RESOURCEGUID);
                    if (szTitle == null) {
                        szTitle = "未命名的外部连接";
                    }
                    if (szTitle.length() > 15) {
                        szTitle = szTitle.substring(0, 15) + "...";
                    }
                    textIntent.setText(szTitle);
                    if (intent.hasExtra("resourcetype")) {
                        int nResID = MyiLibraryAdapter.getResourceIconByType(intent.getIntExtra("resourcetype", 0));
                        if (nResID != 0) {
                            imageViewThumbnail.setImageResource(nResID);
                            imageViewThumbnail.setVisibility(0);
                        }
                    }
                    final Intent intent2 = intent;
                    LayoutExternalIntent.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            intent2.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                            IMMessageListAdapter.this.m_Context.startActivity(intent2);
                        }
                    });
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
                textIntent.setText("数据解析错误");
            }
        } else {
            bTextMessage = true;
            LayoutVoice.setVisibility(8);
            imageViewPicture.setVisibility(8);
            LayoutExternalIntent.setVisibility(8);
            textMessage.setVisibility(0);
        }
        String szMessageText = Data.szMessage;
        String szFrom = IMCore.getChatUserName(szMessageText);
        final boolean bIsTextMessage = bTextMessage;
        if (szFrom.isEmpty()) {
            szFrom = Data.szName;
        }
        szMessageText = IMCore.trimMessageText(szMessageText);
        final String szSourceMessageText = szMessageText.replace("<br>", "\n").replace("<br/>", "\n");
        textUserName.setText(szFrom);
        textMessage.setText(Html.fromHtml(szMessageText, new FaceImageGetter(this.m_Context), null));
        textDate.setText(Data.szDate);
        if (Data.szName.equalsIgnoreCase(this.mCurrentUserName)) {
            textUserName.setTextColor(-13205376);
        } else {
            textUserName.setTextColor(-16776961);
        }
        if (!this.mbEnableVote || szVoteGUID == null) {
            LayoutMessage.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(new ContextThemeWrapper(IMMessageListAdapter.this.m_Context, R.style.MenuTheme), v);
                    popup.getMenu().add(0, R.id.action_copytext, 0, "复制");
                    popup.getMenu().add(0, R.id.action_share, 0, "分享给别人");
                    if (!(!Data.bSearchResult || IMMessageListAdapter.this.mOpenRelatedClickListener == null || Data.szGUID == null)) {
                        popup.getMenu().add(0, R.id.action_openRelated, 0, "查看相关信息");
                    }
                    final boolean z = bIsTextMessage;
                    final String str = szSourceMessageText;
                    final IMMessageItem iMMessageItem = Data;
                    final View view = v;
                    popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.action_copytext) {
                                ClipboardManager clipMan = (ClipboardManager) IMMessageListAdapter.this.m_Context.getSystemService("clipboard");
                                if (((TextView) view.findViewById(R.id.textViewMessageText)) == null) {
                                    return true;
                                }
                                ClipData clip;
                                if (z) {
                                    clip = ClipData.newPlainText("plaintext", str);
                                } else {
                                    clip = ClipData.newPlainText("pasteandsent", str);
                                }
                                clipMan.setPrimaryClip(clip);
                                return true;
                            }
                            if (item.getItemId() == R.id.action_openRelated) {
                                if (IMMessageListAdapter.this.mOpenRelatedClickListener != null) {
                                    IMMessageListAdapter.this.mOpenRelatedClickListener.onOpenRelatedMessageClick(iMMessageItem.szGUID);
                                }
                            } else if (item.getItemId() == R.id.action_share) {
                                if (UI.getCurrentActivity() == null || !(UI.getCurrentActivity() instanceof ChatActivity)) {
                                    Utilities.showAlertMessage(IMMessageListAdapter.this.m_Context, "分享", "请将在线答疑对话框放大后再使用此功能。");
                                } else {
                                    ChatActivity baseActivity = (ChatActivity) UI.getCurrentActivity();
                                    ShareToDialog shareDialog = new ShareToDialog();
                                    FragmentTransaction ft = baseActivity.getSupportFragmentManager().beginTransaction();
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    shareDialog.setCancelable(true);
                                    final String str = str;
                                    shareDialog.setCallBack(new ShareDialogCallBack() {
                                        public void onShare(ArrayList<String> arrSelectedUserJIDs) {
                                            for (int i = 0; i < arrSelectedUserJIDs.size(); i++) {
                                                ChatComponent.sendMessage((String) arrSelectedUserJIDs.get(i), Utilities.unescape(str));
                                            }
                                            Utilities.showAlertMessage(null, "已发出", "该内容已成功分享。");
                                        }
                                    });
                                    shareDialog.show(ft, "shareDialog");
                                }
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });
        } else {
            final String szVoteTargetGUID = szVoteGUID;
            LayoutMessage.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(new ContextThemeWrapper(IMMessageListAdapter.this.m_Context, R.style.MenuTheme), v);
                    popup.getMenu().add(0, R.id.action_vote_unvote, 0, "点赞/取消点赞");
                    popup.getMenu().add(0, R.id.action_copytext, 0, "复制");
                    popup.getMenu().add(0, R.id.action_share, 0, "分享给别人");
                    if (!(!Data.bSearchResult || IMMessageListAdapter.this.mOpenRelatedClickListener == null || Data.szGUID == null)) {
                        popup.getMenu().add(0, R.id.action_openRelated, 0, "查看相关信息");
                    }
                    final RelativeLayout relativeLayout = LayoutVote;
                    final TextView textView = textVoteCount;
                    final String str = szVoteTargetGUID;
                    final boolean z = bIsTextMessage;
                    final String str2 = szSourceMessageText;
                    final IMMessageItem iMMessageItem = Data;
                    final View view = v;
                    popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.action_vote_unvote) {
                                relativeLayout.setVisibility(0);
                                IMMessageListAdapter.this.OnVote(textView, str);
                                return true;
                            } else if (item.getItemId() == R.id.action_copytext) {
                                ClipboardManager clipMan = (ClipboardManager) IMMessageListAdapter.this.m_Context.getSystemService("clipboard");
                                if (((TextView) view.findViewById(R.id.textViewMessageText)) == null) {
                                    return true;
                                }
                                ClipData clip;
                                if (z) {
                                    clip = ClipData.newPlainText("plaintext", str2);
                                } else {
                                    clip = ClipData.newPlainText("pasteandsent", str2);
                                }
                                clipMan.setPrimaryClip(clip);
                                return true;
                            } else if (item.getItemId() != R.id.action_openRelated) {
                                if (item.getItemId() == R.id.action_share) {
                                    if (UI.getCurrentActivity() == null || !(UI.getCurrentActivity() instanceof ChatActivity)) {
                                        Utilities.showAlertMessage(IMMessageListAdapter.this.m_Context, "分享", "请将在线答疑对话框放大后再使用此功能。");
                                    } else {
                                        ChatActivity baseActivity = (ChatActivity) UI.getCurrentActivity();
                                        ShareToDialog shareDialog = new ShareToDialog();
                                        FragmentTransaction ft = baseActivity.getSupportFragmentManager().beginTransaction();
                                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                        shareDialog.setCancelable(true);
                                        final String str = str2;
                                        shareDialog.setCallBack(new ShareDialogCallBack() {
                                            public void onShare(ArrayList<String> arrSelectedUserJIDs) {
                                                for (int i = 0; i < arrSelectedUserJIDs.size(); i++) {
                                                    ChatComponent.sendMessage((String) arrSelectedUserJIDs.get(i), Utilities.unescape(str));
                                                }
                                                Utilities.showAlertMessage(null, "已发出", "该内容已成功分享。");
                                            }
                                        });
                                        shareDialog.show(ft, "shareDialog");
                                    }
                                }
                                return false;
                            } else if (IMMessageListAdapter.this.mOpenRelatedClickListener == null) {
                                return true;
                            } else {
                                IMMessageListAdapter.this.mOpenRelatedClickListener.onOpenRelatedMessageClick(iMMessageItem.szGUID);
                                return true;
                            }
                        }
                    });
                    popup.show();
                    return false;
                }
            });
            textVoteCount.setTag(szVoteGUID);
            WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussGetVoteCount", null);
            ResourceObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    if (textVoteCount.getTag() != null) {
                        int nVote = 0;
                        try {
                            nVote = Integer.valueOf(ItemObject.readTextData()).intValue();
                        } catch (NumberFormatException e) {
                        }
                        textVoteCount.setText(String.valueOf(nVote));
                        if (nVote > 0) {
                            LayoutVote.setVisibility(0);
                        } else {
                            LayoutVote.setVisibility(8);
                        }
                    }
                }
            });
            ResourceObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                }
            });
            ResourceObject.setParam("lpszObjectGUID", szVoteGUID);
            ResourceObject.setParam("lpszOwnerGUID", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
            ResourceObject.setParam("nObjectType", Integer.valueOf(6));
            ResourceObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(ResourceObject);
        }
        return convertView;
    }

    public boolean OnVote(final TextView View, final String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussVote", null);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                int nVote = 0;
                try {
                    nVote = Integer.valueOf(View.getText().toString()).intValue();
                } catch (NumberFormatException e) {
                }
                View.setText(String.valueOf(nVote + 1));
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                if (nReturnCode == ErrorCode.ERROR_ALREADY_EXISTS) {
                    IMMessageListAdapter.this.OnUnVote(View, szTopicGUID);
                }
            }
        });
        ResourceObject.setParam("lpszObjectGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        ResourceObject.setParam("nObjectType", Integer.valueOf(6));
        ResourceObject.setParam("bCancel", Integer.valueOf(0));
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
        return true;
    }

    public void OnUnVote(final TextView View, String szTopicGUID) {
        WebServiceCallItemObject ResourceObject = new WebServiceCallItemObject("DiscussVote", null);
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                int nVote = 0;
                try {
                    nVote = Integer.valueOf(View.getText().toString()).intValue();
                } catch (NumberFormatException e) {
                }
                View.setText(String.valueOf(nVote - 1));
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(IMMessageListAdapter.this.m_Context, "取消好评时出现错误", "取消好评时出现错误，请尝试重试。");
            }
        });
        ResourceObject.setParam("lpszObjectGUID", szTopicGUID);
        ResourceObject.setParam("lpszOwnerGUID", MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        ResourceObject.setParam("nObjectType", Integer.valueOf(6));
        ResourceObject.setParam("bCancel", Integer.valueOf(1));
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }
}
