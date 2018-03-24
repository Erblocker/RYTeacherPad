package com.netspace.library.components;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.internal.view.SupportMenu;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.activity.FingerDrawActivity.FingerDrawCallbackInterface;
import com.netspace.library.activity.FingerDrawActivity.FingerDrawTouchUpInterface;
import com.netspace.library.adapter.FriendsListAdapter;
import com.netspace.library.adapter.FriendsListAdapter.FriendItem;
import com.netspace.library.adapter.FriendsListAdapter2;
import com.netspace.library.adapter.IMMessageListAdapter;
import com.netspace.library.adapter.IMMessageListAdapter.OnOpenRelatedClickListener;
import com.netspace.library.adapter.IMMessageListAdapter.OnUserImageClickListener;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.DrawComponent.DrawComponentCallBack;
import com.netspace.library.components.VideoChatComponent.VideoChatCallBack;
import com.netspace.library.consts.Features;
import com.netspace.library.controls.CustomEditText;
import com.netspace.library.controls.CustomEditText.OnEditTextActionCallBack;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.controls.DrawView;
import com.netspace.library.database.IMDataBase;
import com.netspace.library.database.IWmExamDBOpenHelper;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.im.IMService;
import com.netspace.library.im.IMService.OnIMServiceArrivedListener;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.restful.RESTEvent;
import com.netspace.library.restful.RESTRequest;
import com.netspace.library.restful.RESTRequest.RESTRequestBeforeStart;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.restful.RESTService;
import com.netspace.library.restful.RESTServiceProvider;
import com.netspace.library.restful.provider.camera.CameraRESTServiceProvider;
import com.netspace.library.restful.provider.imagecrop.CropRESTServiceProvider;
import com.netspace.library.restful.provider.screencapture.ScreenCaptureRESTServiceProvider;
import com.netspace.library.servers.MP3PlayThread;
import com.netspace.library.servers.MP3PlayThread.MP3PlayThreadCallBackInterface;
import com.netspace.library.servers.MP3RecordThread;
import com.netspace.library.servers.MP3RecordThread.MP3RecordThreadCallBackInterface;
import com.netspace.library.struct.RecentUserItem;
import com.netspace.library.struct.UserClassInfo;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.FaceImageGetter;
import com.netspace.library.utilities.NewItems;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.GetTemporaryStorageItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.library.window.ChatWindow;
import com.netspace.library.window.TextWindow;
import com.netspace.library.window.VideoChatWindow;
import com.netspace.library.window.VideoWindow;
import com.netspace.library.wrapper.CameraCaptureActivity;
import com.netspace.library.wrapper.CameraCaptureActivity.CameraCaptureCallBack;
import com.netspace.pad.library.R;
import io.vov.vitamio.MediaMetadataRetriever;
import io.vov.vitamio.MediaPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import wei.mark.standout.StandOutWindow;

public class ChatComponent extends CustomFrameLayout implements OnClickListener, OnItemClickListener, OnChildClickListener, OnGroupClickListener {
    private static ChatComponent mActiveComponent = null;
    private static NewItems mChatNewItems;
    private static StatusBarDisplayer mChatStatusBarDisplayer;
    private static String mClassGUID;
    private static String mClassName;
    private static Context mContext;
    private static IWmExamDBOpenHelper mDBHelper;
    private static DrawView mFullScreenDrawView;
    private static IMDataBase mIMDataBase;
    private static OnIMServiceArrivedListener mIMMessageListener = new OnIMServiceArrivedListener() {
        public void OnMessageArrived(String szMessage) {
            if (szMessage.startsWith("{")) {
                try {
                    JSONObject jSONObject = new JSONObject(szMessage.replaceAll("\\n", "\n"));
                    String szGUID = jSONObject.getString("guid");
                    String szFrom = jSONObject.getString("from");
                    szMessage = jSONObject.getString("content");
                    if (szMessage.startsWith("CHAT")) {
                        String szServerTime;
                        szMessage = szMessage.substring(5);
                        if (jSONObject.has("servertime")) {
                            szServerTime = jSONObject.getString("servertime");
                            szMessage = "SERVERTIME=" + szServerTime + ";" + szMessage;
                        } else {
                            szServerTime = Utilities.getNowMillsecond();
                        }
                        if (ChatComponent.mDBHelper != null) {
                            ChatComponent.mDBHelper.addIMMessage(szGUID, szFrom, IMService.getIMUserName(), szMessage, szServerTime);
                        } else {
                            ChatComponent.mIMDataBase.addIMMessage(szGUID, szFrom, IMService.getIMUserName(), szMessage, szServerTime);
                        }
                        ChatComponent.mChatNewItems.addNewItem(szFrom);
                        if (ChatComponent.mActiveComponent != null) {
                            final String str = szMessage;
                            final String str2 = szFrom;
                            final String str3 = szServerTime;
                            ChatComponent.mActiveComponent.post(new Runnable() {
                                public void run() {
                                    if (ChatComponent.mActiveComponent != null) {
                                        ChatComponent.mActiveComponent.addMessage(str, str2, str3);
                                    }
                                }
                            });
                        } else {
                            if (ChatComponent.mChatStatusBarDisplayer != null) {
                                ChatComponent.mChatStatusBarDisplayer.shutDown();
                                ChatComponent.mChatStatusBarDisplayer = null;
                            }
                            if (ChatComponent.mChatStatusBarDisplayer == null) {
                                ChatComponent.mChatStatusBarDisplayer = new StatusBarDisplayer(ChatComponent.mContext);
                                ChatComponent.mChatStatusBarDisplayer.setIcon(R.drawable.ic_chat_white);
                                ChatComponent.mChatStatusBarDisplayer.setNotifyID(20);
                                ChatComponent.mChatStatusBarDisplayer.setSound(true);
                                ChatComponent.mChatStatusBarDisplayer.setVibrate(true);
                                ChatComponent.mChatStatusBarDisplayer.setTitle("收到在线答疑消息");
                                ChatComponent.mChatStatusBarDisplayer.setText("收到尚未阅读的在线答疑消息了。");
                                ChatComponent.mChatStatusBarDisplayer.setPendingIntent(PendingIntent.getService(ChatComponent.mContext, 0, StandOutWindow.getShowIntent(ChatComponent.mContext, ChatWindow.class, 1000), 1073741824));
                                if (UI.isScreenLocked()) {
                                    ChatComponent.mChatStatusBarDisplayer.setSound(false);
                                } else {
                                    ChatComponent.mChatStatusBarDisplayer.setSound(true);
                                }
                                ChatComponent.mChatStatusBarDisplayer.showAlertBox();
                            }
                        }
                        if (szGUID != null) {
                            WebServiceCallItemObject webServiceCallItemObject = new WebServiceCallItemObject("IMSetMessageReceived", null);
                            webServiceCallItemObject.setSuccessListener(new OnSuccessListener() {
                                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                }
                            });
                            ArrayList<String> arrParam = new ArrayList();
                            arrParam.add(szGUID);
                            webServiceCallItemObject.setParam("arrMessageGUIDs", arrParam);
                            webServiceCallItemObject.setParam("lpszIP", "");
                            webServiceCallItemObject.setParam("bDeleteFromDB", Integer.valueOf(0));
                            webServiceCallItemObject.setAlwaysActiveCallbacks(true);
                            VirtualNetworkObject.addToQueue(webServiceCallItemObject);
                            return;
                        }
                        return;
                    }
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            }
            int nSendDataPos;
            String szSender;
            if (szMessage.indexOf("StartVoice") != -1) {
                String str4 = szMessage;
                String szKey = str4.substring(szMessage.indexOf("StartVoice") + 12);
                nSendDataPos = szMessage.indexOf(":");
                if (nSendDataPos != -1) {
                    szSender = szMessage.substring(0, nSendDataPos);
                    Log.d("ChatComponent", "Receive startvoice im message. key:" + szKey);
                    if (ChatComponent.mActiveComponent != null) {
                        if (szSender.equalsIgnoreCase(ChatComponent.mActiveComponent.mTargetJID)) {
                            ChatComponent.mActiveComponent.startListenToVoiceChat(szKey);
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            if (szMessage.indexOf("StartVideo") != -1) {
                str4 = szMessage;
                String szSDPFileName = "rtsp://" + MyiBaseApplication.getCommonVariables().ServerInfo.getServerHost() + "/" + str4.substring(szMessage.indexOf("StartVideo") + 12);
                Log.d("ChatComponent", "Receive startvideo im message. url:" + szSDPFileName);
                StandOutWindow.show(MyiBaseApplication.getBaseAppContext(), VideoWindow.class, MediaPlayer.MEDIA_INFO_UNKNOW_TYPE);
                Bundle data = new Bundle();
                data.putString("data", szSDPFileName);
                StandOutWindow.sendData(ChatComponent.mContext, VideoWindow.class, MediaPlayer.MEDIA_INFO_UNKNOW_TYPE, 0, data, TextWindow.class, MediaPlayer.MEDIA_INFO_UNKNOW_TYPE);
                return;
            }
            if (szMessage.indexOf("ChatBroadData") == -1) {
                if (szMessage.indexOf("ClearBroadData") == -1) {
                    if (szMessage.indexOf("BroadBackground") == -1) {
                        return;
                    }
                }
            }
            nSendDataPos = szMessage.indexOf(":");
            if (nSendDataPos != -1) {
                szSender = szMessage.substring(0, nSendDataPos);
                int nDataPos = szMessage.indexOf("ChatBroadData: ");
                if (nDataPos != -1) {
                    String szDrawData = szMessage.substring("ChatBroadData: ".length() + nDataPos);
                    String szPartDrawData = szDrawData;
                    if (ChatComponent.mMapDrawComponentData.containsKey(szSender)) {
                        szDrawData = new StringBuilder(String.valueOf((String) ChatComponent.mMapDrawComponentData.get(szSender))).append(szDrawData).toString();
                    }
                    ChatComponent.mMapDrawComponentData.put(szSender, szDrawData);
                    if (ChatComponent.mActiveComponent != null) {
                        if (szSender.equalsIgnoreCase(ChatComponent.mActiveComponent.mTargetJID)) {
                            if (ChatComponent.mActiveComponent.mDrawComponent != null) {
                                str = szPartDrawData;
                                ChatComponent.mActiveComponent.mDrawComponent.getDrawView().post(new Runnable() {
                                    public void run() {
                                        if (ChatComponent.mActiveComponent != null && ChatComponent.mActiveComponent.mDrawComponent != null) {
                                            ChatComponent.mActiveComponent.mDrawComponent.getDrawView().fromString(str);
                                        }
                                    }
                                });
                            }
                            if (ChatComponent.mFullScreenDrawView != null) {
                                str = szPartDrawData;
                                ChatComponent.mFullScreenDrawView.post(new Runnable() {
                                    public void run() {
                                        if (ChatComponent.mFullScreenDrawView != null) {
                                            ChatComponent.mFullScreenDrawView.fromString(str);
                                        }
                                    }
                                });
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                if (szMessage.indexOf("ClearBroadData") != -1) {
                    ChatComponent.mMapDrawComponentData.remove(szSender);
                    if (ChatComponent.mActiveComponent != null) {
                        if (szSender.equalsIgnoreCase(ChatComponent.mActiveComponent.mTargetJID) && ChatComponent.mActiveComponent.mDrawComponent != null) {
                            ChatComponent.mActiveComponent.mLastData = "";
                            ChatComponent.mActiveComponent.mnLastDataIndex = 0;
                            ChatComponent.mActiveComponent.mDrawComponent.getDrawView().post(new Runnable() {
                                public void run() {
                                    if (ChatComponent.mActiveComponent != null && ChatComponent.mActiveComponent.mDrawComponent != null) {
                                        ChatComponent.mActiveComponent.mDrawComponent.getDrawView().clearPoints();
                                        ChatComponent.mActiveComponent.mDrawComponent.getDrawView().cleanCache();
                                    }
                                }
                            });
                            return;
                        }
                        return;
                    }
                    return;
                }
                nDataPos = szMessage.indexOf("BroadBackground");
                if (nDataPos != -1) {
                    if (szMessage.indexOf(":") != -1) {
                        String szSender2 = szMessage.substring(0, nSendDataPos);
                        if (szMessage.indexOf("BroadBackground: ") != -1) {
                            String szBoardBackgroundKey = szMessage.substring("BroadBackground: ".length() + nDataPos);
                            if (ChatComponent.mActiveComponent != null) {
                                if (ChatComponent.mActiveComponent.mFriendsListAdapter != null) {
                                    ChatComponent.mActiveComponent.mFriendsListAdapter.updateBackgroundKey(szSender2, szBoardBackgroundKey);
                                }
                                if (ChatComponent.mActiveComponent.mFriendsListAdapter2 != null) {
                                    ChatComponent.mActiveComponent.mFriendsListAdapter2.updateBackgroundKey(szSender2, szBoardBackgroundKey);
                                }
                                if (szSender2.equalsIgnoreCase(ChatComponent.mActiveComponent.mTargetJID)) {
                                    ChatComponent.mActiveComponent.switchDrawPadBackground(szBoardBackgroundKey);
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    private static int mLastSelectedGroupIndex;
    private static int mLastSelectedItemIndex;
    private static Parcelable mListViewState;
    private static HashMap<String, String> mMapDrawComponentData = new HashMap();
    private static String mRealName;
    private static String mTeacherUserName;
    private static boolean mbLocked = false;
    private static boolean mbTeacherMode = false;
    private AudioComponent mAudioComponent;
    private String mBackgroundKey = "";
    private XListView mChatMessageListView;
    private Context mContextThemeWrapper;
    private FingerDrawCallbackInterface mDrawCallBackInterface = new FingerDrawCallbackInterface() {
        public void OnFingerDrawCreate(Activity Activity) {
        }

        public boolean HasMJpegClients() {
            return false;
        }

        public void OnUpdateMJpegImage(Bitmap bitmap, Activity Activity) {
        }

        public void OnProject(Activity Activity) {
        }

        public void OnDestroy(Activity Activity) {
            ChatComponent.mFullScreenDrawView = null;
            FingerDrawActivity.SetTouchUpInterface(null);
            ChatComponent.this.mnLastDataIndex = 0;
        }

        public void OnBroadcast(Activity Activity) {
        }

        public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight, Activity Activity) {
        }

        public void OnOK(Bitmap bitmap, final String szUploadName, final Activity Activity) {
            Bitmap bitmapSmall = ThumbnailUtils.extractThumbnail(bitmap, 48, 48);
            final String szBitmap = Utilities.saveBitmapToBase64String(bitmapSmall);
            String szEncodedData = Utilities.saveBitmapToBase64String(bitmap, 100);
            bitmapSmall.recycle();
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", null);
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    ChatComponent.this.sendRawMessage("PICTURE=" + szUploadName + ",THUMBNAIL=" + szBitmap + ";");
                    Activity.finish();
                    if (ChatComponent.this.mDrawComponent != null) {
                        ChatComponent.this.mDrawComponent.getDrawView().cleanCache();
                        ChatComponent.this.mDrawComponent.getDrawView().clearPoints();
                        IMService.getIMService().sendMessage(IMService.getIMUserName() + ": ClearBroadData", ChatComponent.this.mTargetJID);
                        IMService.getIMService().sendMessage(IMService.getIMUserName() + ": BroadBackground: ", ChatComponent.this.mTargetJID);
                        ChatComponent.this.switchDrawPadBackground("");
                        if (ChatComponent.this.mFriendsListAdapter != null) {
                            ChatComponent.this.mFriendsListAdapter.updateBackgroundKey(ChatComponent.this.mTargetJID, ChatComponent.this.mBackgroundKey);
                        }
                        if (ChatComponent.this.mFriendsListAdapter2 != null) {
                            ChatComponent.this.mFriendsListAdapter2.updateBackgroundKey(ChatComponent.this.mTargetJID, ChatComponent.this.mBackgroundKey);
                        }
                    }
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showAlertMessage(Activity, "保存图片失败", "图片保存出现问题。");
                }
            });
            CallItem.setParam("lpszBase64Data", szEncodedData);
            CallItem.setParam("szKey", szUploadName);
            CallItem.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(CallItem);
        }
    };
    private DrawComponent mDrawComponent;
    private ImageButton mDrawPadButton;
    private String mDrawPadKey;
    private FingerDrawTouchUpInterface mDrawTouchUpInterface = new FingerDrawTouchUpInterface() {
        public void OnDrawViewReady(DrawView DrawView) {
            ChatComponent.mFullScreenDrawView = DrawView;
            ChatComponent.this.mnLastDataIndex = 0;
        }

        public void OnTouchUp(DrawView DrawView) {
            ChatComponent.mFullScreenDrawView = DrawView;
            String szNewData = DrawView.getDataAsString(ChatComponent.this.mnLastDataIndex);
            if (szNewData != null) {
                IMService.getIMService().sendMessage(IMService.getIMUserName() + ": ChatBroadData: " + szNewData, ChatComponent.this.mTargetJID);
                ChatComponent.this.mLastData = szNewData;
                ChatComponent.this.mnLastDataIndex = DrawView.getDataPointsCount();
            }
        }
    };
    private TextView mEmptyTextView;
    private ImageButton mFaceButton;
    private FaceComponent mFaceComponent;
    private ListView mFriendListView;
    private ExpandableListView mFriendListView2;
    private FriendsListAdapter mFriendsListAdapter;
    private FriendsListAdapter2 mFriendsListAdapter2;
    private String mLastData = "";
    private LinearLayout mLayoutChatComponent;
    private LinearLayout mLayoutChatContent;
    private TextView mLockedTextView;
    private IMMessageListAdapter mMessageListAdapter;
    private int mMessageStartIndex = 0;
    private OnOpenRelatedClickListener mOpenRelatedClickListener = new OnOpenRelatedClickListener() {
        public boolean onOpenRelatedMessageClick(String szMessageGUID) {
            ChatComponent.this.mMessageListAdapter.clear();
            ChatComponent.this.mChatMessageListView.setStackFromBottom(false);
            if (ChatComponent.mDBHelper != null) {
                ChatComponent.mDBHelper.getIMRelatedMessage(ChatComponent.this.mTargetJID, szMessageGUID, ChatComponent.this.mMessageListAdapter);
            } else {
                ChatComponent.mIMDataBase.getIMRelatedMessage(ChatComponent.this.mTargetJID, szMessageGUID, ChatComponent.this.mMessageListAdapter);
            }
            ChatComponent.this.mChatMessageListView.setAdapter(ChatComponent.this.mMessageListAdapter);
            ChatComponent.this.mMessageStartIndex = ChatComponent.this.mMessageListAdapter.getCount();
            return false;
        }
    };
    private MP3PlayThread mPlayServer;
    private MP3RecordThread mRecordServer;
    private View mRootView;
    private int mSelectedGroupIndex = 0;
    private int mSelectedItemIndex = 0;
    private String mTargetDisplayName;
    private String mTargetJID = "";
    private CustomEditText mTextToSend;
    private OnUserImageClickListener mUserChatImageClickListener = new OnUserImageClickListener() {
        public boolean onUserImageClick(String szImageKey) {
            if (ChatComponent.this.mDrawComponent == null || ChatComponent.this.mDrawComponent.getVisibility() != 0) {
                return false;
            }
            ChatComponent.this.mDrawComponent.getDrawView().cleanCache();
            ChatComponent.this.mDrawComponent.getDrawView().clearPoints();
            IMService.getIMService().sendMessage(IMService.getIMUserName() + ": ClearBroadData", ChatComponent.this.mTargetJID);
            IMService.getIMService().sendMessage(IMService.getIMUserName() + ": BroadBackground: " + szImageKey, ChatComponent.this.mTargetJID);
            ChatComponent.this.switchDrawPadBackground(szImageKey);
            return true;
        }
    };
    private ImageButton mVideoChatButton;
    private ImageButton mVoiceButton;
    private ImageButton mVoiceChatButton;
    private boolean mbFriendsListLoaded = false;
    private int mnLastDataIndex = 0;

    public ChatComponent(Context context) {
        super(context);
        initView();
    }

    public ChatComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ChatComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void processIMMessage(String szData) {
    }

    public static ChatComponent getCurrentChatComponent() {
        return mActiveComponent;
    }

    public boolean sendMessage(String szData) {
        if (this.mTargetJID == null || this.mTargetJID.isEmpty()) {
            return false;
        }
        sendRawMessage(Html.fromHtml(szData, new FaceImageGetter(getContext()), null).toString());
        return true;
    }

    public static void setChatUserInfo(Context context, String szRealName, String szUserClassName, String szUserClassGUID, IWmExamDBOpenHelper DBHelper, boolean bTeacherMode, String szTeacherUserName) {
        mContext = context;
        mRealName = szRealName;
        mClassName = szUserClassName;
        mClassGUID = szUserClassGUID;
        if (mDBHelper == null) {
            mDBHelper = DBHelper;
        }
        mbTeacherMode = bTeacherMode;
        mTeacherUserName = szTeacherUserName;
        if (mDBHelper == null) {
            mIMDataBase = new IMDataBase(context, szTeacherUserName);
        }
        if (mChatNewItems == null) {
            mChatNewItems = new NewItems(context, "ChatNewItems_" + IMService.getIMUserName());
            IMService.getIMService().registerCallBack(mIMMessageListener);
        }
    }

    public static void shutdown() {
        if (mIMDataBase != null) {
            mIMDataBase.close();
            mIMDataBase = null;
        }
        if (mChatStatusBarDisplayer != null) {
            mChatStatusBarDisplayer.hideMessage();
            mChatStatusBarDisplayer = null;
        }
        mDBHelper = null;
        IMService.getIMService().unregisterCallBack(mIMMessageListener);
        mChatNewItems = null;
    }

    public static void lockChat(boolean bLocked) {
        mbLocked = bLocked;
        if (mActiveComponent != null) {
            mActiveComponent.setLocked(bLocked);
        }
    }

    public void setLocked(boolean bLocked) {
        RelativeLayout RootLayout = (RelativeLayout) this.mRootView.findViewById(R.id.RelativeLayout1);
        for (int i = 0; i < RootLayout.getChildCount(); i++) {
            View oneView = RootLayout.getChildAt(i);
            if (bLocked) {
                oneView.setVisibility(4);
            } else {
                oneView.setVisibility(0);
            }
        }
        if (bLocked) {
            this.mLockedTextView.setVisibility(0);
            return;
        }
        this.mLockedTextView.setVisibility(4);
        if (mbTeacherMode) {
            this.mFriendListView.setVisibility(4);
            this.mFriendListView2.setVisibility(0);
        } else {
            this.mFriendListView2.setVisibility(4);
            this.mFriendListView.setVisibility(0);
        }
        if (this.mTargetJID.isEmpty()) {
            this.mLayoutChatContent.setVisibility(8);
            this.mEmptyTextView.setVisibility(4);
            return;
        }
        this.mLayoutChatContent.setVisibility(0);
        if (this.mMessageListAdapter.getCount() == 0) {
            this.mEmptyTextView.setVisibility(0);
        } else {
            this.mEmptyTextView.setVisibility(4);
        }
    }

    public void initView() {
        Log.d("ChatComponent", "initView");
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_chat, this, true);
        this.mLayoutChatComponent = (LinearLayout) this.mRootView.findViewById(R.id.LinearLayoutChatModules);
        this.mLayoutChatContent = (LinearLayout) this.mRootView.findViewById(R.id.LinearLayoutChatContent);
        this.mEmptyTextView = (TextView) this.mRootView.findViewById(R.id.TextViewEmptyText);
        this.mEmptyTextView.setVisibility(4);
        this.mTextToSend = (CustomEditText) this.mRootView.findViewById(R.id.editText1);
        this.mTextToSend.setCallBack(new OnEditTextActionCallBack() {
            public boolean onTextCut() {
                return false;
            }

            public boolean onTextCopy() {
                return false;
            }

            public boolean onTextPaste() {
                ClipboardManager clipboard = (ClipboardManager) ChatComponent.mContext.getSystemService("clipboard");
                if (!clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType("text/plain")) {
                    return false;
                }
                String szLabel = "";
                String szData = "";
                if (clipboard.getPrimaryClip().getDescription().getLabel() != null) {
                    szLabel = clipboard.getPrimaryClip().getDescription().getLabel().toString();
                }
                szData = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                if (szLabel.equalsIgnoreCase("plaintext")) {
                    ChatComponent.this.mTextToSend.setText(Html.fromHtml(szData, new FaceImageGetter(ChatComponent.mContext), null));
                } else if (szLabel.equalsIgnoreCase("pasteandsent")) {
                    ChatComponent.this.sendRawMessage(szData);
                } else {
                    ChatComponent.this.mTextToSend.setText(szData);
                }
                return true;
            }
        });
        this.mChatMessageListView = (XListView) this.mRootView.findViewById(R.id.listViewChatMessage);
        this.mLockedTextView = (TextView) this.mRootView.findViewById(R.id.textViewLocked);
        this.mLayoutChatContent.setVisibility(8);
        this.mFaceComponent = new FaceComponent(this.mContextThemeWrapper);
        this.mLayoutChatComponent.addView(this.mFaceComponent, -1, 200);
        this.mFaceComponent.setCallBack(new ComponentCallBack() {
            public void OnDataLoaded(String szFileName, IComponents Component) {
            }

            public void OnDataUploaded(String szData, IComponents Component) {
                ChatComponent.this.mTextToSend.append(Html.fromHtml("<img src='" + szData + "'/>", new FaceImageGetter(ChatComponent.this.mContextThemeWrapper), null));
            }

            public void OnRequestIntent(Intent intent, IComponents Component) {
            }
        });
        this.mFaceComponent.setVisibility(8);
        this.mFriendListView = (ListView) this.mRootView.findViewById(R.id.listViewFriendsList);
        this.mFriendsListAdapter = new FriendsListAdapter(this.mContextThemeWrapper, this.mFriendListView);
        this.mFriendListView.setAdapter(this.mFriendsListAdapter);
        this.mFriendListView.setChoiceMode(1);
        this.mFriendListView.setOnItemClickListener(this);
        this.mFriendListView2 = (ExpandableListView) this.mRootView.findViewById(R.id.listViewFriendsList2);
        this.mFriendsListAdapter2 = new FriendsListAdapter2(this.mContextThemeWrapper, this.mFriendListView2);
        this.mFriendListView2.setAdapter(this.mFriendsListAdapter2);
        this.mFriendListView2.setChoiceMode(1);
        this.mFriendListView2.setOnChildClickListener(this);
        this.mFriendListView2.setOnGroupClickListener(this);
        this.mFriendListView.setLayerType(1, null);
        this.mFriendListView2.setLayerType(1, null);
        Log.d("ChatComponent", "mbTeacherMode=" + mbTeacherMode);
        if (mbTeacherMode) {
            this.mFriendListView.setVisibility(4);
            this.mFriendListView2.setVisibility(0);
        } else {
            this.mFriendListView2.setVisibility(4);
            this.mFriendListView.setVisibility(0);
        }
        this.mMessageListAdapter = new IMMessageListAdapter(this.mContextThemeWrapper, this.mChatMessageListView, IMService.getIMUserName());
        this.mMessageListAdapter.setUserImageClickListener(this.mUserChatImageClickListener);
        this.mMessageListAdapter.setOpenRelatedClickListener(this.mOpenRelatedClickListener);
        this.mChatMessageListView.setAdapter(this.mMessageListAdapter);
        this.mChatMessageListView.setChoiceMode(0);
        this.mChatMessageListView.setDividerHeight(0);
        this.mChatMessageListView.setPullLoadEnable(false);
        this.mChatMessageListView.setPullRefreshEnable(true);
        this.mChatMessageListView.setRefreshText("下拉加载以往的消息", "松手开始加载", "正在加载以往的记录...");
        this.mChatMessageListView.setXListViewListener(new IXListViewListener() {
            public void onRefresh() {
                boolean bHasNewMessage;
                ChatComponent.this.mChatMessageListView.setStackFromBottom(false);
                ChatComponent.this.mMessageListAdapter.removeSearchResultMessage();
                if (ChatComponent.mDBHelper != null) {
                    bHasNewMessage = ChatComponent.mDBHelper.getIMMessages(ChatComponent.this.mTargetJID, ChatComponent.this.mMessageStartIndex, HttpStatus.SC_MULTIPLE_CHOICES, ChatComponent.this.mMessageListAdapter);
                } else {
                    bHasNewMessage = ChatComponent.mIMDataBase.getIMMessages(ChatComponent.this.mTargetJID, ChatComponent.this.mMessageStartIndex, HttpStatus.SC_MULTIPLE_CHOICES, ChatComponent.this.mMessageListAdapter);
                }
                if (bHasNewMessage) {
                    ChatComponent.this.mChatMessageListView.setAdapter(ChatComponent.this.mMessageListAdapter);
                    ChatComponent chatComponent = ChatComponent.this;
                    chatComponent.mMessageStartIndex = chatComponent.mMessageStartIndex + HttpStatus.SC_MULTIPLE_CHOICES;
                    ChatComponent.this.mChatMessageListView.stopRefresh();
                    return;
                }
                ChatComponent.this.mMessageStartIndex = ChatComponent.this.mMessageListAdapter.getCount();
                WebServiceCallItemObject CallItem = new WebServiceCallItemObject("IMResendHistoryMessage", null);
                CallItem.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        String szResult = ItemObject.readTextData();
                        boolean bHasNewMessage = false;
                        if (!(szResult == null || szResult.isEmpty())) {
                            JSONArray jsonObject = new JSONArray(szResult);
                            for (int i = 0; i < jsonObject.length(); i++) {
                                JSONObject oneObject = jsonObject.getJSONObject(i);
                                String szRealContent = oneObject.getString(TestHandler.TEXT);
                                String szFrom = oneObject.getString("from");
                                String szTo = oneObject.getString("to");
                                try {
                                    JSONObject oneChatContent = new JSONObject(szRealContent);
                                    if (oneChatContent.has("guid")) {
                                        try {
                                            String szGUID = oneChatContent.getString("guid");
                                            String szMessage = oneChatContent.getString("content");
                                            if (szMessage.startsWith("CHAT")) {
                                                String szMessageDate;
                                                szMessage = szMessage.substring(5);
                                                String szServerTime;
                                                if (oneChatContent.has("servertime")) {
                                                    szServerTime = oneChatContent.getString("servertime");
                                                    szMessageDate = szServerTime;
                                                    szMessage = "SERVERTIME=" + szServerTime + ";" + szMessage;
                                                } else {
                                                    szServerTime = Utilities.getNow();
                                                    szMessageDate = oneObject.getString(MediaMetadataRetriever.METADATA_KEY_DATE);
                                                }
                                                if (ChatComponent.mDBHelper != null) {
                                                    ChatComponent.mDBHelper.addIMMessage(szGUID, szFrom, szTo, szMessage, szMessageDate);
                                                } else {
                                                    ChatComponent.mIMDataBase.addIMMessage(szGUID, szFrom, szTo, szMessage, szMessageDate);
                                                }
                                            } else {
                                                continue;
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        continue;
                                    }
                                } catch (JSONException e2) {
                                }
                            }
                            if (ChatComponent.mDBHelper != null) {
                                bHasNewMessage = ChatComponent.mDBHelper.getIMMessages(ChatComponent.this.mTargetJID, ChatComponent.this.mMessageStartIndex, HttpStatus.SC_MULTIPLE_CHOICES, ChatComponent.this.mMessageListAdapter);
                            } else {
                                bHasNewMessage = ChatComponent.mIMDataBase.getIMMessages(ChatComponent.this.mTargetJID, ChatComponent.this.mMessageStartIndex, HttpStatus.SC_MULTIPLE_CHOICES, ChatComponent.this.mMessageListAdapter);
                            }
                        }
                        if (bHasNewMessage) {
                            ChatComponent access$0 = ChatComponent.this;
                            access$0.mMessageStartIndex = access$0.mMessageStartIndex + HttpStatus.SC_MULTIPLE_CHOICES;
                            ChatComponent.this.mChatMessageListView.setAdapter(ChatComponent.this.mMessageListAdapter);
                        }
                        ChatComponent.this.mChatMessageListView.stopRefresh();
                        if (ChatComponent.this.mMessageListAdapter.getCount() == 0) {
                            ChatComponent.this.mEmptyTextView.setVisibility(0);
                        } else {
                            ChatComponent.this.mEmptyTextView.setVisibility(4);
                        }
                    }
                });
                CallItem.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        ChatComponent.this.mChatMessageListView.stopRefresh();
                    }
                });
                CallItem.setParam("lpszFromClientID", ChatComponent.this.mTargetJID);
                CallItem.setParam("lpszToClientID", IMService.getIMUserName());
                CallItem.setParam("nFrom", Integer.valueOf(ChatComponent.this.mMessageStartIndex));
                CallItem.setParam("nCount", Integer.valueOf(HttpStatus.SC_MULTIPLE_CHOICES));
                CallItem.setAlwaysActiveCallbacks(true);
                VirtualNetworkObject.addToQueue(CallItem);
            }

            public void onLoadMore() {
            }
        });
        this.mRootView.findViewById(R.id.imageButtonSend).setOnClickListener(this);
        this.mFaceButton = (ImageButton) this.mRootView.findViewById(R.id.imageButtonFace);
        this.mFaceButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (ChatComponent.this.mFaceComponent.getVisibility() == 8) {
                    ChatComponent.this.mFaceComponent.setVisibility(0);
                    ChatComponent.this.mFaceButton.setSelected(true);
                } else {
                    ChatComponent.this.mFaceComponent.setVisibility(8);
                    ChatComponent.this.mFaceButton.setSelected(false);
                }
                if (ChatComponent.this.mAudioComponent != null) {
                    ChatComponent.this.mAudioComponent.setVisibility(8);
                }
                ChatComponent.this.mVoiceButton.setSelected(false);
                if (ChatComponent.this.mDrawComponent != null) {
                    ChatComponent.this.mDrawComponent.setVisibility(8);
                }
                ChatComponent.this.mDrawPadButton.setSelected(false);
            }
        });
        this.mVoiceButton = (ImageButton) this.mRootView.findViewById(R.id.imageButtonVoice);
        this.mVoiceButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (ChatComponent.this.mAudioComponent == null) {
                    ChatComponent.this.mAudioComponent = new AudioComponent(ChatComponent.this.mContextThemeWrapper);
                    ChatComponent.this.mLayoutChatComponent.addView(ChatComponent.this.mAudioComponent, -1, 200);
                    ChatComponent.this.mAudioComponent.setCallBack(new ComponentCallBack() {
                        public void OnDataLoaded(String szFileName, IComponents Component) {
                            if (ChatComponent.this.mAudioComponent != null) {
                                ChatComponent.this.sendRawMessage("VOICE=" + szFileName + ",TIME=" + ((AudioComponent) Component).getTotalTime() + ";");
                                ChatComponent.this.mLayoutChatComponent.removeView(ChatComponent.this.mAudioComponent);
                                ChatComponent.this.mAudioComponent = null;
                                ChatComponent.this.mVoiceButton.setSelected(false);
                            }
                        }

                        public void OnDataUploaded(String szData, IComponents Component) {
                        }

                        public void OnRequestIntent(Intent intent, IComponents Component) {
                        }
                    });
                    ChatComponent.this.mVoiceButton.setSelected(true);
                    ChatComponent.this.mAudioComponent.setVisibility(0);
                } else if (ChatComponent.this.mAudioComponent.getVisibility() == 8) {
                    ChatComponent.this.mAudioComponent.setVisibility(0);
                    ChatComponent.this.mVoiceButton.setSelected(true);
                } else {
                    ChatComponent.this.mAudioComponent.setVisibility(8);
                    ChatComponent.this.mVoiceButton.setSelected(false);
                }
                ChatComponent.this.mFaceComponent.setVisibility(8);
                ChatComponent.this.mFaceButton.setSelected(false);
                if (ChatComponent.this.mDrawComponent != null) {
                    ChatComponent.this.mDrawComponent.setVisibility(8);
                }
                ChatComponent.this.mDrawPadButton.setSelected(false);
            }
        });
        this.mVoiceChatButton = (ImageButton) this.mRootView.findViewById(R.id.imageButtonPhone);
        if (!MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_VOICECHAT)) {
            this.mVoiceChatButton.setVisibility(8);
        }
        this.mVoiceChatButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                ChatComponent.this.mVoiceChatButton.setSelected(!ChatComponent.this.mVoiceChatButton.isSelected());
                if (!ChatComponent.this.mVoiceChatButton.isSelected()) {
                    ChatComponent.this.stopVoiceChat();
                } else if (ChatComponent.this.mRecordServer == null) {
                    String szHost = MyiBaseApplication.getCommonVariables().ServerInfo.getServerHost();
                    final String szKey = new StringBuilder(String.valueOf(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)).append("_Voice.mp3").toString();
                    String szURI = "/Redirect?filename=" + szKey;
                    if (szHost.indexOf(":") != -1) {
                        szHost = szHost.substring(0, szHost.indexOf(":"));
                    }
                    ChatComponent.this.mRecordServer = new MP3RecordThread(ChatComponent.mContext, szHost, MyiBaseApplication.getCommonVariables().ServerInfo.getServerPort(), szURI, new MP3RecordThreadCallBackInterface() {
                        public void onRecordStart() {
                            IMService.getIMService().sendMessage(IMService.getIMUserName() + ": StartVoice: " + szKey, ChatComponent.this.mTargetJID);
                            Utilities.runOnUIThread(ChatComponent.mContext, new Runnable() {
                                public void run() {
                                    Toast.makeText(ChatComponent.mContext, "已开始语音通话", 0).show();
                                }
                            });
                        }

                        public void onRecordError() {
                            Utilities.runOnUIThread(ChatComponent.mContext, new Runnable() {
                                public void run() {
                                    Toast.makeText(ChatComponent.mContext, "打开语音通话时出现错误，请确保当前没有在进行屏幕录制或正在上课。", 0).show();
                                    ChatComponent.this.mVoiceChatButton.setSelected(false);
                                }
                            });
                            ChatComponent.this.mRecordServer.stopRecord();
                            ChatComponent.this.mRecordServer = null;
                        }

                        public void onNewMP3RecordThreadInstance(MP3RecordThread NewThread) {
                            ChatComponent.this.mRecordServer = NewThread;
                        }
                    });
                    ChatComponent.this.mRecordServer.setAutoReconnect(true);
                    ChatComponent.this.mRecordServer.start();
                }
            }
        });
        this.mVideoChatButton = (ImageButton) this.mRootView.findViewById(R.id.imageButtonVideo);
        if (VERSION.SDK_INT >= 18) {
            this.mVideoChatButton.setVisibility(0);
        } else {
            this.mVideoChatButton.setVisibility(8);
        }
        if (!MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_VIDEOCHAT)) {
            this.mVideoChatButton.setVisibility(8);
        }
        this.mVideoChatButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (VERSION.SDK_INT >= 18) {
                    StandOutWindow.show(ChatComponent.this.mContextThemeWrapper, VideoChatWindow.class, 1000);
                    VideoChatComponent.setSessionCallBack(new VideoChatCallBack() {
                        public void OnSessionReady(String szSDPFileName) {
                            Log.d("ChatComponent", "VideoChatComponent session ready. Send im message.");
                            IMService.getIMService().sendMessage(IMService.getIMUserName() + ": StartVideo: " + szSDPFileName, ChatComponent.this.mTargetJID);
                        }
                    });
                }
            }
        });
        ((ImageButton) this.mRootView.findViewById(R.id.imageButtonCamera)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                CameraCaptureActivity.setCallBack(new CameraCaptureCallBack() {
                    public void onCaptureComplete(String szFileName) {
                        final String szDrawPadKey = "Camera_" + Utilities.createGUID() + ".jpg";
                        Bitmap bitmap = Utilities.loadBitmapFromFile(szFileName);
                        Bitmap bitmapSmall = ThumbnailUtils.extractThumbnail(bitmap, 48, 48);
                        final String szBitmap = Utilities.saveBitmapToBase64String(bitmapSmall);
                        String szEncodedData = Utilities.saveBitmapToBase64String(bitmap, 100);
                        bitmapSmall.recycle();
                        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", null);
                        CallItem.setSuccessListener(new OnSuccessListener() {
                            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                ChatComponent.this.sendRawMessage("PICTURE=" + szDrawPadKey + ",THUMBNAIL=" + szBitmap + ";");
                            }
                        });
                        CallItem.setFailureListener(new OnFailureListener() {
                            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                Utilities.showAlertMessage(ChatComponent.this.mContextThemeWrapper, "保存图片失败", "图片保存出现问题。");
                            }
                        });
                        CallItem.setParam("lpszBase64Data", szEncodedData);
                        CallItem.setParam("szKey", szDrawPadKey);
                        CallItem.setAlwaysActiveCallbacks(true);
                        VirtualNetworkObject.addToQueue(CallItem);
                    }
                });
                Intent newIntent = new Intent(ChatComponent.mContext, CameraCaptureActivity.class);
                newIntent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                ChatComponent.mContext.startActivity(newIntent);
            }
        });
        this.mDrawPadButton = (ImageButton) this.mRootView.findViewById(R.id.imageButtonDrawPad);
        this.mDrawPadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utilities.logClick(v);
                if (ChatComponent.this.mDrawComponent == null) {
                    ChatComponent.this.mDrawComponent = new DrawComponent(ChatComponent.this.mContextThemeWrapper);
                    ChatComponent.this.mLayoutChatComponent.addView(ChatComponent.this.mDrawComponent, -1, 500);
                    ChatComponent.this.mDrawComponent.requestFocus();
                    ChatComponent.this.switchDrawPadBackground(ChatComponent.this.mBackgroundKey);
                    ChatComponent.this.mDrawComponent.setDrawComponentCallBack(new DrawComponentCallBack() {
                        private boolean mbWindowHide = false;

                        public void OnZoomout(DrawView DrawView) {
                            int nImageWidth = Utilities.getScreenWidth(ChatComponent.this.mContextThemeWrapper);
                            int nImageHeight = Utilities.getScreenHeight(ChatComponent.this.mContextThemeWrapper);
                            ChatComponent.this.mDrawPadKey = "DrawPad_" + Utilities.createGUID() + ".jpg";
                            Intent DrawActivity = new Intent(ChatComponent.this.mContextThemeWrapper, FingerDrawActivity.class);
                            FingerDrawActivity.SetCallbackInterface(ChatComponent.this.mDrawCallBackInterface);
                            FingerDrawActivity.SetTouchUpInterface(ChatComponent.this.mDrawTouchUpInterface);
                            if (!(ChatComponent.this.mBackgroundKey == null || ChatComponent.this.mBackgroundKey.isEmpty())) {
                                DrawActivity.putExtra("imageKey", ChatComponent.this.mBackgroundKey.substring(0, ChatComponent.this.mBackgroundKey.length() - 4));
                                Drawable backgroundDrawable = DrawView.getBackground();
                                if (backgroundDrawable instanceof BitmapDrawable) {
                                    Utilities.saveBitmapToJpeg(new StringBuilder(String.valueOf(ChatComponent.this.mContextThemeWrapper.getExternalCacheDir().getAbsolutePath())).append("/").append(ChatComponent.this.mBackgroundKey).toString(), ((BitmapDrawable) backgroundDrawable).getBitmap());
                                }
                            }
                            DrawActivity.putExtra("imageWidth", nImageWidth);
                            DrawActivity.putExtra("imageHeight", nImageHeight);
                            DrawActivity.putExtra("imageData", DrawView.getDataAsString());
                            DrawActivity.putExtra("allowUpload", true);
                            DrawActivity.putExtra("allowCamera", true);
                            DrawActivity.putExtra("uploadName", ChatComponent.this.mDrawPadKey);
                            DrawActivity.putExtra("enableBackButton", true);
                            DrawActivity.setFlags(335544320);
                            ChatComponent.this.mContextThemeWrapper.startActivity(DrawActivity);
                        }

                        public void OnSave(DrawView DrawView) {
                            ChatComponent.this.mDrawPadKey = "DrawPad_" + Utilities.createGUID() + ".jpg";
                            DrawView.setSize(DrawView.getWidth(), DrawView.getHeight());
                            Bitmap bitmap = DrawView.saveToBitmap();
                            Bitmap bitmapSmall = ThumbnailUtils.extractThumbnail(bitmap, 48, 48);
                            final String szBitmap = Utilities.saveBitmapToBase64String(bitmapSmall);
                            String szEncodedData = Utilities.saveBitmapToBase64String(bitmap, 100);
                            bitmapSmall.recycle();
                            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", null);
                            CallItem.setSuccessListener(new OnSuccessListener() {
                                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                    ChatComponent.this.sendRawMessage("PICTURE=" + ChatComponent.this.mDrawPadKey + ",THUMBNAIL=" + szBitmap + ";");
                                    ChatComponent.this.mLastData = "";
                                    ChatComponent.this.mnLastDataIndex = 0;
                                    ChatComponent.mMapDrawComponentData.remove(ChatComponent.this.mTargetJID);
                                    if (ChatComponent.this.mDrawComponent != null) {
                                        ChatComponent.this.mDrawComponent.getDrawView().cleanCache();
                                        ChatComponent.this.mDrawComponent.getDrawView().clearPoints();
                                        IMService.getIMService().sendMessage(IMService.getIMUserName() + ": ClearBroadData", ChatComponent.this.mTargetJID);
                                        IMService.getIMService().sendMessage(IMService.getIMUserName() + ": BroadBackground: ", ChatComponent.this.mTargetJID);
                                        ChatComponent.this.switchDrawPadBackground("");
                                        if (ChatComponent.this.mFriendsListAdapter != null) {
                                            ChatComponent.this.mFriendsListAdapter.updateBackgroundKey(ChatComponent.this.mTargetJID, ChatComponent.this.mBackgroundKey);
                                        }
                                        if (ChatComponent.this.mFriendsListAdapter2 != null) {
                                            ChatComponent.this.mFriendsListAdapter2.updateBackgroundKey(ChatComponent.this.mTargetJID, ChatComponent.this.mBackgroundKey);
                                        }
                                    }
                                    ChatComponent.this.mDrawComponent.getDrawView().setSize(-1, -1);
                                }
                            });
                            CallItem.setFailureListener(new OnFailureListener() {
                                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                    Utilities.showAlertMessage(ChatComponent.this.mContextThemeWrapper, "保存图片失败", "图片保存出现问题。");
                                }
                            });
                            CallItem.setParam("lpszBase64Data", szEncodedData);
                            CallItem.setParam("szKey", ChatComponent.this.mDrawPadKey);
                            CallItem.setAlwaysActiveCallbacks(true);
                            VirtualNetworkObject.addToQueue(CallItem);
                        }

                        public void OnDrawViewTouchUp(DrawView DrawView) {
                            String szNewData = DrawView.getDataAsString(ChatComponent.this.mnLastDataIndex);
                            if (szNewData != null) {
                                IMService.getIMService().sendMessage(IMService.getIMUserName() + ": ChatBroadData: " + szNewData, ChatComponent.this.mTargetJID);
                                ChatComponent.this.mLastData = szNewData;
                                ChatComponent.this.mnLastDataIndex = DrawView.getDataPointsCount();
                            }
                        }

                        public void OnCamera(DrawView mDrawView) {
                            RESTService.getDefault().execute(CameraRESTServiceProvider.URI).setOnBeforeStart(new RESTRequestBeforeStart() {
                                public void onBeforeStart(RESTServiceProvider service, RESTRequest request) {
                                    if (((View) ChatComponent.this.getParent()).getContext() instanceof ChatWindow) {
                                        StandOutWindow.hideAll(ChatComponent.mContext, ChatWindow.class);
                                        AnonymousClass1.this.mbWindowHide = true;
                                    }
                                }
                            }).chainRequest(CropRESTServiceProvider.URI).uniqueRequest("ChatComponentCameraCapture").start(new RESTRequestCallBack() {
                                public void onRestSuccess(RESTServiceProvider service, RESTRequest request, RESTEvent event) {
                                    if (AnonymousClass1.this.mbWindowHide) {
                                        StandOutWindow.restoreAll(ChatComponent.mContext, ChatWindow.class);
                                        AnonymousClass1.this.mbWindowHide = false;
                                    }
                                    final Bitmap b = Utilities.loadBitmapFromFile(request.getParam("targetfilename"));
                                    ChatComponent.this.mBackgroundKey = "ChatBackground_" + Utilities.createGUID() + ".jpg";
                                    if (b != null) {
                                        String szEncodedData = Utilities.saveBitmapToBase64String(b, 100);
                                        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", null);
                                        CallItem.setSuccessListener(new OnSuccessListener() {
                                            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                                if (ChatComponent.this.mDrawComponent != null) {
                                                    Utilities.setViewBackground(ChatComponent.this.mDrawComponent.getDrawView(), new BitmapDrawable(ChatComponent.this.getResources(), b));
                                                    IMService.getIMService().sendMessage(IMService.getIMUserName() + ": BroadBackground: " + ChatComponent.this.mBackgroundKey, ChatComponent.this.mTargetJID);
                                                    if (ChatComponent.this.mFriendsListAdapter != null) {
                                                        ChatComponent.this.mFriendsListAdapter.updateBackgroundKey(ChatComponent.this.mTargetJID, ChatComponent.this.mBackgroundKey);
                                                    }
                                                    if (ChatComponent.this.mFriendsListAdapter2 != null) {
                                                        ChatComponent.this.mFriendsListAdapter2.updateBackgroundKey(ChatComponent.this.mTargetJID, ChatComponent.this.mBackgroundKey);
                                                    }
                                                }
                                            }
                                        });
                                        CallItem.setFailureListener(new OnFailureListener() {
                                            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                                Utilities.showAlertMessage(ChatComponent.this.mContextThemeWrapper, "保存图片失败", "图片保存出现问题。");
                                            }
                                        });
                                        CallItem.setParam("lpszBase64Data", szEncodedData);
                                        CallItem.setParam("szKey", ChatComponent.this.mBackgroundKey);
                                        CallItem.setAlwaysActiveCallbacks(true);
                                        VirtualNetworkObject.addToQueue(CallItem);
                                    }
                                }

                                public void onRestFailure(RESTServiceProvider service, RESTRequest request, RESTEvent event) {
                                    if (AnonymousClass1.this.mbWindowHide) {
                                        StandOutWindow.restoreAll(ChatComponent.mContext, ChatWindow.class);
                                        AnonymousClass1.this.mbWindowHide = false;
                                    }
                                }
                            });
                        }

                        public void OnCapturePad(DrawView mDrawView) {
                            RESTService.getDefault().execute(ScreenCaptureRESTServiceProvider.URI).setOnBeforeStart(new RESTRequestBeforeStart() {
                                public void onBeforeStart(RESTServiceProvider service, RESTRequest request) {
                                    if (((View) ChatComponent.this.getParent()).getContext() instanceof ChatWindow) {
                                        StandOutWindow.hideAll(ChatComponent.mContext, ChatWindow.class);
                                        AnonymousClass1.this.mbWindowHide = true;
                                    }
                                    ChatComponent.this.mBackgroundKey = "ChatBackground_" + Utilities.createGUID() + ".jpg";
                                }
                            }).chainRequest(CropRESTServiceProvider.URI).uniqueRequest("ChatComponentScreenCapture").start(new RESTRequestCallBack() {
                                public void onRestSuccess(RESTServiceProvider service, RESTRequest request, RESTEvent event) {
                                    if (AnonymousClass1.this.mbWindowHide) {
                                        StandOutWindow.restoreAll(ChatComponent.mContext, ChatWindow.class);
                                        AnonymousClass1.this.mbWindowHide = false;
                                    }
                                    final Bitmap b = Utilities.loadBitmapFromFile(request.getParam("targetfilename"));
                                    String szEncodedData = Utilities.saveBitmapToBase64String(b, 100);
                                    WebServiceCallItemObject CallItem = new WebServiceCallItemObject("PutTemporaryStorage", null);
                                    CallItem.setSuccessListener(new OnSuccessListener() {
                                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                            if (ChatComponent.this.mDrawComponent != null) {
                                                Utilities.setViewBackground(ChatComponent.this.mDrawComponent.getDrawView(), new BitmapDrawable(ChatComponent.this.getResources(), b));
                                                IMService.getIMService().sendMessage(IMService.getIMUserName() + ": BroadBackground: " + ChatComponent.this.mBackgroundKey, ChatComponent.this.mTargetJID);
                                                if (ChatComponent.this.mFriendsListAdapter != null) {
                                                    ChatComponent.this.mFriendsListAdapter.updateBackgroundKey(ChatComponent.this.mTargetJID, ChatComponent.this.mBackgroundKey);
                                                }
                                                if (ChatComponent.this.mFriendsListAdapter2 != null) {
                                                    ChatComponent.this.mFriendsListAdapter2.updateBackgroundKey(ChatComponent.this.mTargetJID, ChatComponent.this.mBackgroundKey);
                                                }
                                            }
                                        }
                                    });
                                    CallItem.setFailureListener(new OnFailureListener() {
                                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                            Utilities.showAlertMessage(ChatComponent.this.mContextThemeWrapper, "保存图片失败", "图片保存出现问题。");
                                        }
                                    });
                                    CallItem.setParam("lpszBase64Data", szEncodedData);
                                    CallItem.setParam("szKey", ChatComponent.this.mBackgroundKey);
                                    CallItem.setAlwaysActiveCallbacks(true);
                                    VirtualNetworkObject.addToQueue(CallItem);
                                }

                                public void onRestFailure(RESTServiceProvider service, RESTRequest request, RESTEvent event) {
                                    if (AnonymousClass1.this.mbWindowHide) {
                                        StandOutWindow.restoreAll(ChatComponent.mContext, ChatWindow.class);
                                        AnonymousClass1.this.mbWindowHide = false;
                                    }
                                    Utilities.showAlertMessage(ChatComponent.this.mContextThemeWrapper, "捕获失败", "屏幕捕获失败。");
                                }
                            });
                        }
                    });
                    ChatComponent.this.mDrawComponent.setCallBack(new ComponentCallBack() {
                        public void OnDataLoaded(String szFileName, IComponents Component) {
                        }

                        public void OnDataUploaded(String szData, IComponents Component) {
                        }

                        public void OnRequestIntent(Intent intent, IComponents Component) {
                        }
                    });
                    ChatComponent.this.mDrawPadButton.setSelected(true);
                    ChatComponent.this.mDrawComponent.setVisibility(0);
                } else if (ChatComponent.this.mDrawComponent.getVisibility() == 8) {
                    ChatComponent.this.mDrawComponent.setVisibility(0);
                    ChatComponent.this.mDrawPadButton.setSelected(true);
                } else {
                    ChatComponent.this.mDrawComponent.setVisibility(8);
                    ChatComponent.this.mDrawPadButton.setSelected(false);
                }
                if (ChatComponent.mbTeacherMode) {
                    ChatComponent.this.mDrawComponent.getDrawView().setColor(SupportMenu.CATEGORY_MASK);
                }
                ChatComponent.this.mFaceComponent.setVisibility(8);
                ChatComponent.this.mFaceButton.setSelected(false);
                if (ChatComponent.this.mAudioComponent != null) {
                    ChatComponent.this.mAudioComponent.setVisibility(8);
                }
                ChatComponent.this.mVoiceButton.setSelected(false);
            }
        });
        setLocked(mbLocked);
        mActiveComponent = this;
    }

    public static boolean getLocked() {
        return mbLocked;
    }

    private void stopVoiceChatPlay() {
        if (this.mPlayServer != null) {
            this.mPlayServer.stopPlay();
            this.mPlayServer = null;
        }
    }

    private void stopVoiceChat() {
        if (this.mRecordServer != null) {
            this.mRecordServer.stopRecord();
            this.mRecordServer = null;
        }
        Utilities.runOnUIThread(mContext, new Runnable() {
            public void run() {
                ChatComponent.this.mVoiceChatButton.setSelected(false);
            }
        });
    }

    public void startListenToVoiceChat(String szKey) {
        stopVoiceChatPlay();
        this.mPlayServer = new MP3PlayThread(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/Redirect?filename=" + szKey, new MP3PlayThreadCallBackInterface() {
            public void onPlayStart() {
            }

            public void onPlayError() {
                Utilities.runOnUIThread(ChatComponent.mContext, new Runnable() {
                    public void run() {
                        Toast.makeText(ChatComponent.mContext, "对方语音已结束", 0).show();
                        if (ChatComponent.this.mPlayServer != null) {
                            ChatComponent.this.mPlayServer.stopPlay();
                        }
                    }
                });
            }

            public boolean onEndOfStream() {
                ChatComponent.this.mPlayServer = null;
                return false;
            }

            public void onNewMP3PlayThreadInstance(MP3PlayThread NewThread) {
                ChatComponent.this.mPlayServer = NewThread;
            }
        });
        this.mPlayServer.start();
    }

    public void switchDrawPadBackground(String szBackgroundKey) {
        this.mBackgroundKey = szBackgroundKey;
        if (this.mDrawComponent == null) {
            return;
        }
        if (this.mBackgroundKey == null || this.mBackgroundKey.isEmpty()) {
            Utilities.runOnUIThread(mContext, new Runnable() {
                public void run() {
                    ChatComponent.this.mDrawComponent.getDrawView().setBackgroundResource(R.drawable.background_drawpad);
                }
            });
            return;
        }
        GetTemporaryStorageItemObject ResourceObject = new GetTemporaryStorageItemObject(this.mBackgroundKey, null);
        final String szLocalFileName = new StringBuilder(String.valueOf(mContext.getExternalCacheDir().getAbsolutePath())).append("/").append(this.mBackgroundKey).toString();
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    Context access$18 = ChatComponent.mContext;
                    final String str = szLocalFileName;
                    Utilities.runOnUIThread(access$18, new Runnable() {
                        public void run() {
                            Utilities.setViewBackground(ChatComponent.this.mDrawComponent.getDrawView(), new BitmapDrawable(ChatComponent.mContext.getResources(), Utilities.loadBitmapFromFile(str)));
                        }
                    });
                }
            }
        });
        ResourceObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        ResourceObject.setSaveToFile(true);
        ResourceObject.setTargetFileName(szLocalFileName);
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    protected void loadTeacherFriendsList() {
        Log.d("ChatComponent", "MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.size()=" + MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.size());
        if (MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.size() > 0) {
            UserClassInfo UserClassInfo;
            String szJID;
            Iterator it;
            UserInfo UserInfo;
            String szStudentName;
            Iterator it2 = MyiBaseApplication.getRecentUser().getData().iterator();
            while (it2.hasNext()) {
                RecentUserItem RecentUserItem = (RecentUserItem) it2.next();
                this.mFriendsListAdapter2.add(RecentUserItem.szName, RecentUserItem.szUID, "最近联系人", mChatNewItems.isItemNew(RecentUserItem.szUID));
            }
            it2 = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
            while (it2.hasNext()) {
                UserClassInfo = (UserClassInfo) it2.next();
                szJID = "*_" + UserClassInfo.szClassGUID + "_*";
                it = UserClassInfo.arrStudents.iterator();
                while (it.hasNext()) {
                    UserInfo = (UserInfo) it.next();
                    szStudentName = UserInfo.szRealName;
                    szJID = "myipad_" + UserInfo.szUserName;
                    if (mChatNewItems.isItemNew(szJID)) {
                        this.mFriendsListAdapter2.add(szStudentName, szJID, "未读联系人", true);
                    }
                }
            }
            it2 = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
            while (it2.hasNext()) {
                UserClassInfo = (UserClassInfo) it2.next();
                String szUserClassName = UserClassInfo.szClassName;
                szJID = "*_" + UserClassInfo.szClassGUID + "_*";
                if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_CHAT_CLASSGROUPCHAT)) {
                    this.mFriendsListAdapter2.add("班级讨论区", szJID, szUserClassName, mChatNewItems.isItemNew(szJID));
                }
                it = UserClassInfo.arrStudents.iterator();
                while (it.hasNext()) {
                    UserInfo = (UserInfo) it.next();
                    szStudentName = UserInfo.szRealName;
                    szJID = "myipad_" + UserInfo.szUserName;
                    this.mFriendsListAdapter2.add(szStudentName, szJID, szUserClassName, mChatNewItems.isItemNew(szJID));
                }
            }
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetPrivateData2", null);
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    if (ItemObject.readTextData() != null) {
                        try {
                            JSONArray jsonObject = new JSONArray(ItemObject.readTextData());
                            for (int i = 0; i < jsonObject.length(); i++) {
                                JSONObject oneClass = jsonObject.getJSONObject(i);
                                if (!oneClass.has("szClassGUID")) {
                                    String szGroupName = oneClass.getString("szName");
                                    JSONArray arrStudents = oneClass.getJSONArray("students");
                                    for (int j = 0; j < arrStudents.length(); j++) {
                                        JSONObject oneStudent = arrStudents.getJSONObject(j);
                                        String szJID = "myipad_" + oneStudent.getString(UserHonourFragment.USERNAME);
                                        ChatComponent.this.mFriendsListAdapter2.add(oneStudent.getString("realname"), szJID, szGroupName, ChatComponent.mChatNewItems.isItemNew(szJID));
                                    }
                                }
                            }
                            Log.d("ChatComponent", "FriendsGroupLoaded. reassign adapter to listview.");
                            ChatComponent.this.mFriendListView2.setAdapter(ChatComponent.this.mFriendsListAdapter2);
                            ChatComponent.this.mFriendListView2.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ChatComponent.this.restoreState();
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                }
            });
            CallItem.setParam("lpszKey", "RecentClasses_" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            CallItem.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(CallItem);
            return;
        }
        Log.d("ChatComponent", "loadTeacherFriendsList from database. mTeacherUserName=" + mTeacherUserName);
        String szSQL = "select lessonsscheduleresult.userclassname, lessonsscheduleresult.studentid, lessonsscheduleresult.studentname, lessonsscheduleresult.userclassguid from lessonsschedule,lessonsscheduleresult,resources where resources.acl_ownername='" + mTeacherUserName + "' and resources.type = '1000' and resources.guid = lessonsschedule.resourceguid and lessonsscheduleresult.LessonsScheduleGUID = lessonsschedule.guid group by lessonsscheduleresult.studentid," + " lessonsscheduleresult.userclassguid";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operation", "read");
            jsonObject.put("tableName", "resources");
            jsonObject.put("sql", "%SQL_ChatComponent_LoadTeacherFriendsList%");
            jsonObject.put("teacherUserName", mTeacherUserName);
            CallItem = new WebServiceCallItemObject("JsonTableAccess", null);
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    Log.d("ChatComponent", "loadTeacherFriendsList OnDataSuccess");
                    String szResult = ItemObject.readTextData();
                    if (szResult != null) {
                        try {
                            JSONArray arrData = new JSONObject(szResult).getJSONArray("data");
                            Log.d("ChatComponent", "loadTeacherFriendsList return " + String.valueOf(arrData.length()) + " nodes.");
                            for (int i = 0; i < arrData.length(); i++) {
                                JSONObject oneData = arrData.getJSONObject(i);
                                String szUserClassName = oneData.getString("userclassname");
                                String szStudentID = oneData.getString("studentid");
                                String szStudentName = oneData.getString("studentname");
                                String szJID = "myipad_" + oneData.getString(UserHonourFragment.USERCLASSGUID) + "_" + szStudentID;
                                int nFindPos = szUserClassName.indexOf("校");
                                if (nFindPos != -1) {
                                    szUserClassName = szUserClassName.substring(nFindPos + 1);
                                }
                                ChatComponent.this.mFriendsListAdapter2.add(szStudentName, szJID, szUserClassName, ChatComponent.mChatNewItems.isItemNew(szJID));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ChatComponent.this.mFriendsListAdapter2.notifyDataSetChanged();
                    ChatComponent.this.invalidate();
                    ChatComponent.this.requestLayout();
                }
            });
            CallItem.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                }
            });
            CallItem.setParam("lpszJsonInputData", jsonObject.toString());
            CallItem.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(CallItem);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void loadStudentsFriendsList() {
        ArrayList<String> arrRealNames = new ArrayList();
        ArrayList<String> arrUIDs = new ArrayList();
        MyiBaseApplication.getCommonVariables().UserInfo.getAllClassesTeachers(arrRealNames, arrUIDs);
        for (int i = 0; i < arrRealNames.size(); i++) {
            String szUID = (String) arrUIDs.get(i);
            this.mFriendsListAdapter.add((String) arrRealNames.get(i), szUID);
            this.mFriendsListAdapter.setNewMessage(szUID, mChatNewItems.isItemNew(szUID));
        }
        if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_CHAT_CLASSGROUPCHAT)) {
            Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
            while (it.hasNext()) {
                UserClassInfo UserClassInfo = (UserClassInfo) it.next();
                String szUserClassName = UserClassInfo.szClassName;
                String szJID = "*_" + UserClassInfo.szClassGUID + "_*";
                this.mFriendsListAdapter.add(new StringBuilder(String.valueOf(szUserClassName)).append("讨论区").toString(), szJID);
                this.mFriendsListAdapter.setNewMessage(szJID, mChatNewItems.isItemNew(szJID));
            }
        }
    }

    public void setTargetJID(String szTargetJID) {
        this.mTargetJID = szTargetJID;
        int nMessageCount = HttpStatus.SC_MULTIPLE_CHOICES;
        if (this.mTargetJID.indexOf("*") != -1) {
            nMessageCount = 1000;
        }
        if (mDBHelper != null) {
            mDBHelper.getIMMessages(this.mTargetJID, 0, nMessageCount, this.mMessageListAdapter);
        } else {
            mIMDataBase.getIMMessages(this.mTargetJID, 0, nMessageCount, this.mMessageListAdapter);
        }
        this.mMessageStartIndex = nMessageCount;
        this.mChatMessageListView.setAdapter(this.mMessageListAdapter);
    }

    public String getTargetJID() {
        return this.mTargetJID;
    }

    private void rememberLastDrawComponentData() {
        if (this.mDrawComponent != null && this.mTargetJID != null && !this.mTargetJID.isEmpty()) {
            mMapDrawComponentData.put(this.mTargetJID, this.mDrawComponent.getDrawView().getDataAsString());
        }
    }

    private void updateDrawComponentData() {
        if (this.mDrawComponent != null && this.mTargetJID != null && !this.mTargetJID.isEmpty()) {
            String szData = (String) mMapDrawComponentData.get(this.mTargetJID);
            this.mDrawComponent.getDrawView().clearPoints();
            this.mDrawComponent.getDrawView().cleanCache();
            this.mDrawComponent.getDrawView().fromString(szData);
        }
    }

    public void refresh() {
        Iterator it;
        if (!(this.mTargetJID == null || this.mTargetJID.isEmpty() || !mChatNewItems.isItemNew(this.mTargetJID))) {
            this.mMessageListAdapter = new IMMessageListAdapter(this.mContextThemeWrapper, this.mChatMessageListView, IMService.getIMUserName());
            this.mMessageListAdapter.setUserImageClickListener(this.mUserChatImageClickListener);
            this.mMessageListAdapter.setOpenRelatedClickListener(this.mOpenRelatedClickListener);
            int nMessageCount = HttpStatus.SC_MULTIPLE_CHOICES;
            if (this.mTargetJID.indexOf("*") != -1) {
                this.mMessageListAdapter.setEnableVote(true);
                nMessageCount = 1000;
            }
            if (mDBHelper != null) {
                mDBHelper.getIMMessages(this.mTargetJID, 0, nMessageCount, this.mMessageListAdapter);
            } else {
                mIMDataBase.getIMMessages(this.mTargetJID, 0, nMessageCount, this.mMessageListAdapter);
            }
            this.mMessageStartIndex = nMessageCount;
            this.mChatMessageListView.setAdapter(this.mMessageListAdapter);
        }
        if (this.mMessageListAdapter.getCount() == 0) {
            this.mEmptyTextView.setVisibility(0);
        } else {
            this.mEmptyTextView.setVisibility(4);
        }
        if (this.mFriendsListAdapter != null) {
            it = this.mFriendsListAdapter.getData().iterator();
            while (it.hasNext()) {
                FriendItem item = (FriendItem) it.next();
                if (mChatNewItems.isItemNew(item.szUID)) {
                    item.bHasNewMessage = true;
                }
            }
            this.mFriendsListAdapter.notifyDataSetChanged();
        }
        if (this.mFriendsListAdapter2 != null) {
            it = this.mFriendsListAdapter2.getData().iterator();
            while (it.hasNext()) {
                FriendsListAdapter2.FriendItem item2 = (FriendsListAdapter2.FriendItem) it.next();
                if (mChatNewItems.isItemNew(item2.szUID)) {
                    item2.bHasNewMessage = true;
                }
            }
            this.mFriendsListAdapter2.notifyDataSetChanged();
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        FriendItem FriendItem = (FriendItem) this.mFriendsListAdapter.getItem(position);
        rememberLastDrawComponentData();
        this.mLastData = "";
        this.mnLastDataIndex = 0;
        this.mTargetJID = FriendItem.szUID;
        this.mTargetDisplayName = FriendItem.szName;
        if (!this.mTargetJID.isEmpty()) {
            this.mLayoutChatContent.setVisibility(0);
        }
        mChatNewItems.removeItem(this.mTargetJID);
        switchDrawPadBackground(FriendItem.szDrawPadBackground);
        this.mMessageListAdapter = new IMMessageListAdapter(this.mContextThemeWrapper, this.mChatMessageListView, IMService.getIMUserName());
        this.mMessageListAdapter.setUserImageClickListener(this.mUserChatImageClickListener);
        this.mMessageListAdapter.setOpenRelatedClickListener(this.mOpenRelatedClickListener);
        int nMessageCount = HttpStatus.SC_MULTIPLE_CHOICES;
        if (this.mTargetJID.indexOf("*") != -1) {
            this.mMessageListAdapter.setEnableVote(true);
            nMessageCount = 1000;
        }
        if (mDBHelper != null) {
            mDBHelper.getIMMessages(this.mTargetJID, 0, nMessageCount, this.mMessageListAdapter);
        } else {
            mIMDataBase.getIMMessages(this.mTargetJID, 0, nMessageCount, this.mMessageListAdapter);
        }
        this.mMessageStartIndex = nMessageCount;
        this.mChatMessageListView.setAdapter(this.mMessageListAdapter);
        this.mFriendsListAdapter.setNewMessage(this.mTargetJID, false);
        this.mFriendsListAdapter.notifyDataSetChanged();
        updateDrawComponentData();
        switchUser();
    }

    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        mLastSelectedGroupIndex = groupPosition;
        mLastSelectedItemIndex = childPosition;
        this.mSelectedGroupIndex = groupPosition;
        rememberLastDrawComponentData();
        FriendsListAdapter2.FriendItem FriendItem = (FriendsListAdapter2.FriendItem) this.mFriendsListAdapter2.getChild(groupPosition, childPosition);
        if (FriendItem == null) {
            return false;
        }
        this.mChatMessageListView.setStackFromBottom(true);
        this.mLastData = "";
        this.mnLastDataIndex = 0;
        this.mTargetJID = FriendItem.szUID;
        this.mTargetDisplayName = FriendItem.szName;
        if (!this.mTargetJID.isEmpty()) {
            this.mLayoutChatContent.setVisibility(0);
        }
        mChatNewItems.removeItem(this.mTargetJID);
        switchDrawPadBackground(FriendItem.szDrawPadBackground);
        this.mMessageListAdapter = new IMMessageListAdapter(this.mContextThemeWrapper, this.mChatMessageListView, IMService.getIMUserName());
        this.mMessageListAdapter.setUserImageClickListener(this.mUserChatImageClickListener);
        this.mMessageListAdapter.setOpenRelatedClickListener(this.mOpenRelatedClickListener);
        int nMessageCount = HttpStatus.SC_MULTIPLE_CHOICES;
        this.mMessageStartIndex = 0;
        if (this.mTargetJID.indexOf("*") != -1) {
            this.mMessageListAdapter.setEnableVote(true);
            nMessageCount = 1000;
        }
        if (mDBHelper != null) {
            mDBHelper.getIMMessages(this.mTargetJID, this.mMessageStartIndex, nMessageCount, this.mMessageListAdapter);
        } else {
            mIMDataBase.getIMMessages(this.mTargetJID, this.mMessageStartIndex, nMessageCount, this.mMessageListAdapter);
        }
        this.mMessageStartIndex = nMessageCount;
        this.mChatMessageListView.setAdapter(this.mMessageListAdapter);
        if (this.mMessageListAdapter.getCount() == 0) {
            this.mEmptyTextView.setVisibility(0);
        } else {
            this.mEmptyTextView.setVisibility(4);
        }
        int nIndex = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
        this.mFriendListView2.setItemChecked(nIndex, true);
        this.mSelectedItemIndex = nIndex;
        this.mFriendsListAdapter2.setNewMessage(this.mTargetJID, false);
        this.mFriendsListAdapter2.notifyDataSetChanged();
        updateDrawComponentData();
        switchUser();
        return true;
    }

    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        Utilities.runOnUIThread(mContext, new Runnable() {
            public void run() {
                if (ChatComponent.this.mFriendListView2.getCheckedItemCount() == 0) {
                    ChatComponent.this.mLayoutChatContent.setVisibility(8);
                    ChatComponent.this.mTargetJID = "";
                }
            }
        });
        return false;
    }

    private void switchUser() {
        stopVoiceChat();
        stopVoiceChatPlay();
    }

    public void sendRawMessage(String szTextToSend) {
        String szMessageToSend;
        UserInfo.UserScore("ChatSendText", szTextToSend);
        String szMessageText = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szTextToSend)).append("fields=realname=").append(mRealName).append(";").toString())).append("userclassname=").append(mClassName).append(";").toString();
        boolean bGroupChatMessage = false;
        String szGUID = Utilities.createGUID();
        if (this.mTargetJID.indexOf("*") == -1) {
            szMessageToSend = IMService.buildMessage("CHAT " + szMessageText, szGUID, 0);
        } else {
            szMessageToSend = IMService.buildMessage("CHAT " + szMessageText, szGUID, this.mTargetJID, this.mTargetJID, 0);
            bGroupChatMessage = true;
        }
        IMService.getIMService().sendMessage(szMessageToSend, this.mTargetJID);
        if (!bGroupChatMessage) {
            String szMessageDate = Utilities.getNowMillsecond();
            this.mMessageListAdapter.add(IMService.getIMUserName(), szMessageText, szMessageDate);
            this.mMessageListAdapter.notifyDataSetChanged();
            if (mDBHelper != null) {
                mDBHelper.addIMMessage(szGUID, IMService.getIMUserName(), this.mTargetJID, szMessageText, szMessageDate);
            } else {
                mIMDataBase.addIMMessage(szGUID, IMService.getIMUserName(), this.mTargetJID, szMessageText, szMessageDate);
            }
            MyiBaseApplication.getRecentUser().addUser(this.mTargetDisplayName, this.mTargetJID, 0);
        }
        this.mEmptyTextView.setVisibility(4);
        this.mTextToSend.setText("");
    }

    public void onClick(View v) {
        Utilities.logClick(v);
        if (v.getId() == R.id.imageButtonSend && !this.mTextToSend.getText().toString().isEmpty()) {
            sendRawMessage(Utilities.trimToHtml(Html.toHtml(this.mTextToSend.getText())).replaceAll("INTENT=", "").replaceAll("PICTURE=", "").replaceAll("VOICE=", "").replaceAll("THUMBNAIL=", "").replaceAll("realname=", ""));
        }
    }

    public static void sendMessage(String szUID, String szTextToSend) {
        String szMessageToSend;
        if (mActiveComponent != null) {
            ChatComponent ChatComponent = mActiveComponent;
            if (ChatComponent.mTargetJID.equalsIgnoreCase(szUID)) {
                ChatComponent.sendMessage(szTextToSend);
                return;
            }
        }
        String szMessageText = new StringBuilder(String.valueOf(Utilities.trimToHtml(szTextToSend) + "fields=realname=" + mRealName + ";")).append("userclassname=").append(mClassName).append(";").toString();
        boolean bGroupChatMessage = false;
        String szGUID = Utilities.createGUID();
        if (szUID.indexOf("*") == -1) {
            szMessageToSend = IMService.buildMessage("CHAT " + szMessageText, szGUID, 0);
        } else {
            szMessageToSend = IMService.buildMessage("CHAT " + szMessageText, szGUID, szUID, szUID, 0);
            bGroupChatMessage = true;
        }
        IMService.getIMService().sendMessage(szMessageToSend, szUID);
        if (!bGroupChatMessage) {
            if (mDBHelper != null) {
                mDBHelper.addIMMessage(szGUID, IMService.getIMUserName(), szUID, szMessageText, null);
            } else {
                mIMDataBase.addIMMessage(szGUID, IMService.getIMUserName(), szUID, szMessageText, null);
            }
        }
    }

    public void addMessage(String szMessageText, String szFrom, String szDate) {
        if (szFrom.equalsIgnoreCase(this.mTargetJID)) {
            this.mMessageListAdapter.add(szFrom, szMessageText, szDate);
            this.mMessageListAdapter.notifyDataSetChanged();
        } else if (mbTeacherMode) {
            if (this.mFriendsListAdapter2.setNewMessage(szFrom, true)) {
                String szRealName = this.mFriendsListAdapter2.getUserRealName(szFrom);
                if (szRealName != null) {
                    this.mFriendsListAdapter2.addToTop(szRealName, szFrom, "未读联系人", true);
                }
                mChatNewItems.addNewItem(szFrom);
                this.mFriendsListAdapter2.notifyDataSetChanged();
                this.mFriendListView2.expandGroup(this.mSelectedGroupIndex);
                this.mFriendListView2.setItemChecked(this.mSelectedItemIndex, true);
            }
        } else if (this.mFriendsListAdapter.setNewMessage(szFrom, true)) {
            mChatNewItems.addNewItem(szFrom);
            this.mFriendsListAdapter.notifyDataSetChanged();
        }
    }

    public void search(String szKeyword) {
        this.mMessageListAdapter.clear();
        if (mDBHelper != null) {
            mDBHelper.searchIMMessage(this.mTargetJID, szKeyword, this.mMessageListAdapter);
        } else {
            mIMDataBase.searchIMMessage(this.mTargetJID, szKeyword, this.mMessageListAdapter);
        }
        UserInfo.UserScore("ChatSearchText", szKeyword);
        this.mMessageStartIndex = 0;
        this.mChatMessageListView.setAdapter(this.mMessageListAdapter);
    }

    public void rememberState() {
        if (mbTeacherMode) {
            mListViewState = this.mFriendListView2.onSaveInstanceState();
        }
    }

    public void restoreState() {
        if (mbTeacherMode && mListViewState != null) {
            this.mFriendListView2.onRestoreInstanceState(mListViewState);
            if (this.mFriendListView2.getCheckedItemPosition() != -1) {
                onChildClick(this.mFriendListView2, this.mFriendListView2.getSelectedView(), mLastSelectedGroupIndex, mLastSelectedItemIndex, this.mFriendsListAdapter2.getChildId(mLastSelectedGroupIndex, mLastSelectedItemIndex));
                this.mFriendListView2.setSelection(this.mFriendListView2.getCheckedItemPosition());
                Log.d("ChatComponent", "restoreState called.");
            }
        }
    }

    protected void onDetachedFromWindow() {
        rememberState();
        mActiveComponent = null;
        VideoChatComponent.setSessionCallBack(null);
        super.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        Log.d("ChatComponent", "onAttachedToWindow");
        mActiveComponent = this;
        if (mbTeacherMode) {
            this.mFriendListView.setVisibility(4);
            this.mFriendListView2.setVisibility(0);
        } else {
            this.mFriendListView2.setVisibility(4);
            this.mFriendListView.setVisibility(0);
        }
        if (this.mbFriendsListLoaded) {
            refresh();
        } else {
            if (mbTeacherMode) {
                loadTeacherFriendsList();
            } else {
                loadStudentsFriendsList();
            }
            this.mbFriendsListLoaded = true;
        }
        if (mChatStatusBarDisplayer != null) {
            mChatStatusBarDisplayer.hideMessage();
            mChatStatusBarDisplayer = null;
        }
        super.onAttachedToWindow();
    }
}
