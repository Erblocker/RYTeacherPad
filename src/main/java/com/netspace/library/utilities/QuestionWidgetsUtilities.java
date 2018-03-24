package com.netspace.library.utilities;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.AudioComponent;
import com.netspace.library.components.CameraComponent;
import com.netspace.library.components.DrawComponent;
import com.netspace.library.components.TextComponent;
import com.netspace.library.components.VideoComponent;
import com.netspace.library.consts.Features;
import com.netspace.library.controls.CustomVideoView4;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.utilities.MoveableObject.MoveEndCallBack;
import com.netspace.library.utilities.QuestionBlockContentUtilities.QuestionUtilitiesChangeCallBack;
import com.netspace.library.window.AnswerSheetV2OthersWindow;
import com.netspace.library.window.AnswerSheetV3OthersWindow;
import com.netspace.library.window.AnswerSheetWindow;
import com.netspace.library.window.CameraWindow;
import com.netspace.library.window.DrawWindow;
import com.netspace.library.window.TextWindow;
import com.netspace.library.window.VideoWindow;
import com.netspace.library.window.VoiceWindow;
import com.netspace.library.wrapper.CameraCaptureActivity;
import com.netspace.library.wrapper.CameraRecordActivity;
import com.netspace.pad.library.R;
import java.util.HashMap;
import net.sqlcipher.database.SQLiteDatabase;
import wei.mark.standout.StandOutWindow;

public class QuestionWidgetsUtilities implements OnLongClickListener, ComponentCallBack {
    protected static final int ITEM_TYPE_DRAW = 1;
    protected static final int ITEM_TYPE_PICTURE = 0;
    protected static final int ITEM_TYPE_TEXT = 2;
    protected static final int ITEM_TYPE_VIDEO = 4;
    protected static final int ITEM_TYPE_VOICE = 3;
    protected static final int TAGID_CLASSNAME = R.id.textView1;
    protected static final int TAGID_DATA = R.id.textView3;
    protected static final int TAGID_ID = R.id.textView2;
    protected static final int TAGID_LOCK = R.id.textViewAll;
    protected static IComponents mLastCallComponent;
    protected static NestedScrollView mScrollView;
    protected static HashMap<Integer, QuestionWidgetsUtilities> mWidgetHosts = new HashMap();
    protected static HashMap<Integer, View> mWidgets = new HashMap();
    protected static Integer mWindowID = Integer.valueOf(0);
    private OnClickListener contentBlockImageViewClick = new OnClickListener() {
        public void onClick(View v) {
            String szClassName = (String) v.getTag(QuestionWidgetsUtilities.TAGID_CLASSNAME);
            int nID = Integer.valueOf((String) v.getTag(QuestionWidgetsUtilities.TAGID_ID)).intValue();
            String szData = (String) v.getTag(QuestionWidgetsUtilities.TAGID_DATA);
            boolean bLock = false;
            if (v.getTag(QuestionWidgetsUtilities.TAGID_LOCK) != null && ((String) v.getTag(QuestionWidgetsUtilities.TAGID_LOCK)).equalsIgnoreCase("true")) {
                bLock = true;
            }
            Bundle data;
            if (szClassName.contentEquals("TextWindow")) {
                StandOutWindow.show(QuestionWidgetsUtilities.this.mContext, TextWindow.class, nID);
                if (szData != null) {
                    data = new Bundle();
                    data.putString("data", szData);
                    if (bLock) {
                        data.putBoolean("lock", true);
                    }
                    StandOutWindow.sendData(QuestionWidgetsUtilities.this.mContext, TextWindow.class, nID, 0, data, TextWindow.class, nID);
                }
            } else if (szClassName.contentEquals("VoiceWindow")) {
                StandOutWindow.show(QuestionWidgetsUtilities.this.mContext, VoiceWindow.class, nID);
                if (szData != null) {
                    data = new Bundle();
                    data.putString("data", szData);
                    if (bLock) {
                        data.putBoolean("lock", true);
                    }
                    StandOutWindow.sendData(QuestionWidgetsUtilities.this.mContext, VoiceWindow.class, nID, 0, data, VoiceWindow.class, nID);
                }
            } else if (szClassName.contentEquals("DrawWindow")) {
                StandOutWindow.show(QuestionWidgetsUtilities.this.mContext, DrawWindow.class, nID);
                if (szData != null) {
                    data = new Bundle();
                    data.putString("data", szData);
                    if (bLock) {
                        data.putBoolean("lock", true);
                    }
                    StandOutWindow.sendData(QuestionWidgetsUtilities.this.mContext, DrawWindow.class, nID, 0, data, DrawWindow.class, nID);
                }
            } else if (szClassName.contentEquals("VideoWindow")) {
                StandOutWindow.show(QuestionWidgetsUtilities.this.mContext, VideoWindow.class, nID);
                if (szData != null) {
                    data = new Bundle();
                    data.putString("data", szData);
                    if (bLock) {
                        data.putBoolean("lock", true);
                    }
                    StandOutWindow.sendData(QuestionWidgetsUtilities.this.mContext, VideoWindow.class, nID, 0, data, VideoWindow.class, nID);
                }
            } else if (szClassName.contentEquals("CameraWindow")) {
                StandOutWindow.show(QuestionWidgetsUtilities.this.mContext, CameraWindow.class, nID);
                if (szData != null) {
                    data = new Bundle();
                    data.putString("data", szData);
                    if (bLock) {
                        data.putBoolean("lock", true);
                    }
                    StandOutWindow.sendData(QuestionWidgetsUtilities.this.mContext, CameraWindow.class, nID, 0, data, CameraWindow.class, nID);
                }
            }
        }
    };
    protected int mAnswerSheetWindowID = -1;
    protected QuestionBlockContentUtilities mBlockContentUtilities;
    protected QuestionWidgetChangeCallBack mCallBack;
    protected String mClientID;
    protected Context mContext;
    protected int mLastTouchX = 0;
    protected int mLastTouchY = 0;
    protected LinearLayout mLinearLayout;
    protected MoveableObject mMoveableObject;
    protected RelativeLayout mRelativeLayout;
    protected boolean mbLocked = false;
    private OnLongClickListener onLongClickListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            if (QuestionWidgetsUtilities.this.mbLocked) {
                return false;
            }
            String[] arrNames = new String[]{"移动/改变大小", "删除", "显示为图标/内容"};
            String[] arrNames1 = new String[]{"移动/改变大小", "删除", "显示为图标/内容", "还原默认大小"};
            final View TargetView = v;
            Builder dialogBuilder = new Builder(new ContextThemeWrapper(QuestionWidgetsUtilities.this.mContext, 16974130));
            if (TargetView instanceof IComponents) {
                arrNames[2] = "显示为图标";
                arrNames1[2] = "显示为图标";
            } else {
                arrNames[0] = "移动";
                arrNames[2] = "显示为内容";
                arrNames1[0] = "移动";
                arrNames1[2] = "显示为内容";
            }
            String[] arrOptions = null;
            if (v instanceof ImageView) {
                arrOptions = arrNames;
            } else {
                arrOptions = arrNames1;
            }
            dialogBuilder.setItems(arrOptions, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        UserInfo.UserScore("WidgetMove");
                        if (TargetView instanceof FrameLayout) {
                            QuestionWidgetsUtilities.this.mMoveableObject.prepareMoveObject((FrameLayout) TargetView);
                        }
                        TargetView.setOnTouchListener(QuestionWidgetsUtilities.this.mMoveableObject);
                        TargetView.setOnLongClickListener(null);
                    } else if (which == 1) {
                        final View view = TargetView;
                        new Builder(QuestionWidgetsUtilities.this.mContext, 3).setTitle("删除").setMessage("确实删除此内容吗？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                UserInfo.UserScore("WidgetDelete");
                                QuestionWidgetsUtilities.this.mRelativeLayout.removeView(view);
                                QuestionWidgetsUtilities.this.setChanged(true);
                            }
                        }).setNegativeButton("否", null).show();
                    } else if (which == 2) {
                        int nType;
                        if (TargetView instanceof IComponents) {
                            IComponents Components = TargetView;
                            nType = -1;
                            if (TargetView instanceof TextComponent) {
                                nType = 2;
                            } else if (TargetView instanceof DrawComponent) {
                                nType = 1;
                            } else if (TargetView instanceof VideoComponent) {
                                nType = 4;
                            } else if (TargetView instanceof AudioComponent) {
                                nType = 3;
                            } else if (TargetView instanceof CameraComponent) {
                                nType = 0;
                            }
                            if (nType != -1) {
                                UserInfo.UserScore("WidgetDisplayAsIcon");
                                Params = (LayoutParams) TargetView.getLayoutParams();
                                QuestionWidgetsUtilities.this.mLastTouchX = Params.leftMargin;
                                QuestionWidgetsUtilities.this.mLastTouchY = Params.topMargin;
                                QuestionWidgetsUtilities.this.addContentIcon(nType, Components.getData(), false);
                                QuestionWidgetsUtilities.this.mRelativeLayout.removeView(TargetView);
                                QuestionWidgetsUtilities.this.setChanged(true);
                            }
                        } else if (TargetView instanceof ImageView) {
                            ImageView ImageView = TargetView;
                            String szKey = (String) ImageView.getTag(QuestionWidgetsUtilities.TAGID_ID);
                            String szType = (String) ImageView.getTag(QuestionWidgetsUtilities.TAGID_CLASSNAME);
                            nType = -1;
                            int nID = Integer.valueOf(szKey).intValue();
                            if (szType.contentEquals("CameraWindow")) {
                                nType = 0;
                                StandOutWindow.close(QuestionWidgetsUtilities.this.mContext, CameraWindow.class, nID);
                            } else if (szType.contentEquals("TextWindow")) {
                                nType = 2;
                                StandOutWindow.close(QuestionWidgetsUtilities.this.mContext, TextWindow.class, nID);
                            } else if (szType.contentEquals("VoiceWindow")) {
                                nType = 3;
                                StandOutWindow.close(QuestionWidgetsUtilities.this.mContext, VoiceWindow.class, nID);
                            } else if (szType.contentEquals("VideoWindow")) {
                                nType = 4;
                                StandOutWindow.close(QuestionWidgetsUtilities.this.mContext, VideoWindow.class, nID);
                            } else if (szType.contentEquals("DrawWindow")) {
                                nType = 1;
                                StandOutWindow.close(QuestionWidgetsUtilities.this.mContext, DrawWindow.class, nID);
                            }
                            if (nType != -1) {
                                UserInfo.UserScore("WidgetDisplayAsContent");
                                QuestionWidgetsUtilities.this.addContentBlock(nType, (String) ImageView.getTag(QuestionWidgetsUtilities.TAGID_DATA), false);
                            }
                            synchronized (QuestionWidgetsUtilities.mWidgets) {
                                QuestionWidgetsUtilities.mWidgets.remove(Integer.valueOf(szKey));
                                QuestionWidgetsUtilities.mWidgetHosts.remove(Integer.valueOf(szKey));
                            }
                            QuestionWidgetsUtilities.this.mRelativeLayout.removeView(TargetView);
                            QuestionWidgetsUtilities.this.setChanged(true);
                        }
                    } else if (which == 3) {
                        UserInfo.UserScore("WidgetResetSize");
                        Params = (LayoutParams) TargetView.getLayoutParams();
                        Params.width = -1;
                        Params.height = 500;
                        Params.leftMargin = 0;
                        TargetView.setLayoutParams(Params);
                    }
                }
            }).setTitle("选择动作").create().show();
            return true;
        }
    };

    public interface QuestionWidgetChangeCallBack {
        void onChanged();
    }

    public QuestionWidgetsUtilities(Context context, LinearLayout linearLayout, RelativeLayout relativeLayout, QuestionWidgetChangeCallBack callBack) {
        this.mContext = context;
        this.mRelativeLayout = relativeLayout;
        this.mLinearLayout = linearLayout;
        this.mLinearLayout.setOnLongClickListener(this);
        this.mLinearLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & 255) {
                    case 0:
                        QuestionWidgetsUtilities.this.mLastTouchX = (int) event.getX();
                        QuestionWidgetsUtilities.this.mLastTouchY = (int) event.getY();
                        break;
                }
                return false;
            }
        });
        this.mCallBack = callBack;
        this.mMoveableObject = new MoveableObject(context);
        this.mMoveableObject.setMoveEndCallBack(new MoveEndCallBack() {
            public void onMoveEnd(View v) {
                if (v instanceof FrameLayout) {
                    QuestionWidgetsUtilities.this.mMoveableObject.unprepareMoveObject((FrameLayout) v);
                }
                QuestionWidgetsUtilities.this.setChanged(true);
                v.setOnTouchListener(null);
                v.setOnLongClickListener(QuestionWidgetsUtilities.this.onLongClickListener);
            }

            public void onMoving(View v) {
            }
        });
        this.mBlockContentUtilities = new QuestionBlockContentUtilities(context, this.mLinearLayout, new QuestionUtilitiesChangeCallBack() {
            public void onChanged() {
                QuestionWidgetsUtilities.this.setChanged(true);
            }
        });
    }

    public void setClientID(String szClientID) {
        this.mClientID = szClientID;
    }

    public void setLocked(boolean bLocked) {
        this.mbLocked = bLocked;
        for (int i = 0; i < this.mRelativeLayout.getChildCount(); i++) {
            View OneView = this.mRelativeLayout.getChildAt(i);
            if (OneView instanceof IComponents) {
                ((IComponents) OneView).setLocked(this.mbLocked);
            }
            if (OneView instanceof ImageView) {
                ImageView ImageView = (ImageView) OneView;
                if (this.mbLocked) {
                    ImageView.setTag(TAGID_LOCK, "true");
                } else {
                    ImageView.setTag(TAGID_LOCK, "false");
                }
            }
        }
    }

    public static boolean updateWidgetData(Integer nID, String szData) {
        synchronized (mWidgets) {
            View view = (View) mWidgets.get(nID);
            QuestionWidgetsUtilities host = (QuestionWidgetsUtilities) mWidgetHosts.get(nID);
            if (view != null) {
                view.setTag(TAGID_DATA, szData);
                host.setChanged(true);
                return true;
            }
            return false;
        }
    }

    public void close() {
        synchronized (mWidgets) {
            for (int i = 0; i < this.mRelativeLayout.getChildCount(); i++) {
                View OneView = this.mRelativeLayout.getChildAt(i);
                if ((OneView instanceof ImageView) && OneView.getTag(TAGID_ID) != null) {
                    String szID = (String) OneView.getTag(TAGID_ID);
                    mWidgets.remove(Integer.valueOf(szID));
                    mWidgetHosts.remove(Integer.valueOf(szID));
                }
            }
        }
    }

    public void clearCallBack() {
        this.mCallBack = null;
    }

    protected void addContentBlock(int nType, String szData, boolean bToBottom) {
        LayoutParams Params = null;
        if (nType == 2) {
            UserInfo.UserScore("WidgetAddText");
            TextComponent TextComponent = new TextComponent(this.mContext);
            this.mRelativeLayout.addView(TextComponent);
            TextComponent.setBackgroundResource(Utilities.getThemeCustomResID(R.attr.question_widget_background, this.mContext));
            Params = (LayoutParams) TextComponent.getLayoutParams();
            if (bToBottom) {
                Params.leftMargin = 0;
                Params.topMargin = Math.max(this.mRelativeLayout.getHeight(), this.mLinearLayout.getHeight()) + Utilities.dpToPixel(10, this.mContext);
                Params.width = -1;
                Params.height = 200;
            } else {
                Params.leftMargin = this.mLastTouchX;
                Params.topMargin = this.mLastTouchY;
                Params.width = 200;
                Params.height = 200;
            }
            TextComponent.setLayoutParams(Params);
            TextComponent.setData(szData);
            TextComponent.setOnLongClickListener(this.onLongClickListener);
            TextComponent.setCallBack(this);
        } else if (nType == 1) {
            UserInfo.UserScore("WidgetAddDraw");
            DrawComponent DrawComponent = new DrawComponent(this.mContext);
            this.mRelativeLayout.addView(DrawComponent);
            DrawComponent.setBackgroundResource(Utilities.getThemeCustomResID(R.attr.question_widget_background, this.mContext));
            Params = (LayoutParams) DrawComponent.getLayoutParams();
            if (bToBottom) {
                Params.leftMargin = 0;
                Params.topMargin = Math.max(this.mRelativeLayout.getHeight(), this.mLinearLayout.getHeight()) + Utilities.dpToPixel(10, this.mContext);
                Params.width = -1;
                Params.height = 500;
            } else {
                Params.leftMargin = this.mLastTouchX;
                Params.topMargin = this.mLastTouchY;
                Params.width = 200;
                Params.height = 500;
            }
            DrawComponent.setLayoutParams(Params);
            DrawComponent.setData(szData);
            DrawComponent.setOnLongClickListener(this.onLongClickListener);
            DrawComponent.setCallBack(this);
        } else if (nType == 3) {
            UserInfo.UserScore("WidgetAddVoice");
            AudioComponent AudioComponent = new AudioComponent(this.mContext);
            this.mRelativeLayout.addView(AudioComponent);
            AudioComponent.setBackgroundResource(Utilities.getThemeCustomResID(R.attr.question_widget_background, this.mContext));
            Params = (LayoutParams) AudioComponent.getLayoutParams();
            if (bToBottom) {
                Params.leftMargin = 0;
                Params.topMargin = Math.max(this.mRelativeLayout.getHeight(), this.mLinearLayout.getHeight()) + Utilities.dpToPixel(10, this.mContext);
                Params.width = -1;
                Params.height = 200;
            } else {
                Params.leftMargin = this.mLastTouchX;
                Params.topMargin = this.mLastTouchY;
                Params.width = 200;
                Params.height = 200;
            }
            AudioComponent.setLayoutParams(Params);
            AudioComponent.setData(szData);
            AudioComponent.setOnLongClickListener(this.onLongClickListener);
            AudioComponent.setCallBack(this);
        } else if (nType == 4) {
            UserInfo.UserScore("WidgetAddVideo");
            VideoComponent VideoComponent = new VideoComponent(this.mContext);
            this.mRelativeLayout.addView(VideoComponent);
            VideoComponent.setBackgroundResource(Utilities.getThemeCustomResID(R.attr.question_widget_background, this.mContext));
            Params = (LayoutParams) VideoComponent.getLayoutParams();
            if (bToBottom) {
                Params.leftMargin = 0;
                Params.topMargin = Math.max(this.mRelativeLayout.getHeight(), this.mLinearLayout.getHeight()) + Utilities.dpToPixel(10, this.mContext);
                Params.width = -1;
                Params.height = 500;
            } else {
                Params.leftMargin = this.mLastTouchX;
                Params.topMargin = this.mLastTouchY;
                Params.width = 200;
                Params.height = 500;
            }
            VideoComponent.setLayoutParams(Params);
            VideoComponent.setData(szData);
            VideoComponent.setOnLongClickListener(this.onLongClickListener);
            VideoComponent.setCallBack(this);
        } else if (nType == 0) {
            UserInfo.UserScore("WidgetAddPicture");
            CameraComponent CameraComponent = new CameraComponent(this.mContext);
            this.mRelativeLayout.addView(CameraComponent);
            CameraComponent.setBackgroundResource(Utilities.getThemeCustomResID(R.attr.question_widget_background, this.mContext));
            Params = (LayoutParams) CameraComponent.getLayoutParams();
            if (bToBottom) {
                Params.leftMargin = 0;
                Params.topMargin = Math.max(this.mRelativeLayout.getHeight(), this.mLinearLayout.getHeight()) + Utilities.dpToPixel(10, this.mContext);
                Params.width = -1;
                Params.height = 500;
            } else {
                Params.leftMargin = this.mLastTouchX;
                Params.topMargin = this.mLastTouchY;
                Params.width = 200;
                Params.height = 500;
            }
            CameraComponent.setClientID(this.mClientID);
            CameraComponent.setLayoutParams(Params);
            CameraComponent.setData(szData);
            CameraComponent.setOnLongClickListener(this.onLongClickListener);
            CameraComponent.setCallBack(this);
        }
        if (mScrollView != null && Params != null && bToBottom) {
            int nScrollY = Params.topMargin;
            for (View ParentView = (View) this.mRelativeLayout.getParent(); !ParentView.equals(mScrollView); ViewGroup ParentView2 = (ViewGroup) ParentView.getParent()) {
                nScrollY += ParentView.getTop();
                if (!(ParentView.getParent() instanceof ViewGroup)) {
                    break;
                }
            }
            if (nScrollY > mScrollView.getChildAt(0).getHeight()) {
                nScrollY += Params.height;
            }
            final int nFinalScroll = nScrollY;
            mScrollView.postDelayed(new Runnable() {
                public void run() {
                    QuestionWidgetsUtilities.mScrollView.smoothScrollTo(0, nFinalScroll);
                }
            }, 100);
        }
    }

    public static void setScrollView(NestedScrollView ScrollView) {
        mScrollView = ScrollView;
    }

    protected void addContentIcon(int nType, String szData, boolean bShow) {
        ImageView ImageView = new ImageView(this.mContext);
        ImageView.setImageResource(R.drawable.ic_alert);
        this.mRelativeLayout.addView(ImageView);
        LayoutParams Params = (LayoutParams) ImageView.getLayoutParams();
        Params.leftMargin = this.mLastTouchX;
        Params.topMargin = this.mLastTouchY;
        ImageView.setLayoutParams(Params);
        if (nType == 0) {
            ImageView.setImageResource(R.drawable.ic_camera);
            ImageView.setTag(TAGID_CLASSNAME, "CameraWindow");
            if (bShow) {
                StandOutWindow.show(this.mContext, CameraWindow.class, mWindowID.intValue() + 0);
            }
        } else if (nType == 2) {
            ImageView.setImageResource(R.drawable.ic_text);
            ImageView.setTag(TAGID_CLASSNAME, "TextWindow");
            if (bShow) {
                StandOutWindow.show(this.mContext, TextWindow.class, mWindowID.intValue() + 0);
            }
        } else if (nType == 1) {
            ImageView.setImageResource(R.drawable.ic_drawpad);
            ImageView.setTag(TAGID_CLASSNAME, "DrawWindow");
            if (bShow) {
                StandOutWindow.show(this.mContext, DrawWindow.class, mWindowID.intValue() + 0);
            }
        } else if (nType == 3) {
            ImageView.setImageResource(R.drawable.ic_voice);
            ImageView.setTag(TAGID_CLASSNAME, "VoiceWindow");
            if (bShow) {
                StandOutWindow.show(this.mContext, VoiceWindow.class, mWindowID.intValue() + 0);
            }
        } else if (nType == 4) {
            ImageView.setImageResource(R.drawable.ic_multimedia);
            ImageView.setTag(TAGID_CLASSNAME, "VideoWindow");
            if (bShow) {
                StandOutWindow.show(this.mContext, VideoWindow.class, mWindowID.intValue() + 0);
            }
        }
        setChanged(true);
        ImageView.setTag(TAGID_ID, String.valueOf(mWindowID.intValue() + 0));
        ImageView.setTag(TAGID_DATA, szData);
        synchronized (mWidgets) {
            mWidgets.put(Integer.valueOf(mWindowID.intValue() + 0), ImageView);
            mWidgetHosts.put(Integer.valueOf(mWindowID.intValue() + 0), this);
        }
        checkWindowObjects(true);
        synchronized (mWindowID) {
            mWindowID = Integer.valueOf(mWindowID.intValue() + 1);
        }
    }

    public void showAnswerSheet(String szSheetData) {
        if (this.mAnswerSheetWindowID == -1) {
            this.mAnswerSheetWindowID = mWindowID.intValue();
            synchronized (mWindowID) {
                mWindowID = Integer.valueOf(mWindowID.intValue() + 1);
            }
        }
        StandOutWindow.show(this.mContext, AnswerSheetWindow.class, this.mAnswerSheetWindowID);
        Bundle data = new Bundle();
        data.putString("data", szSheetData);
        if (this.mbLocked) {
            data.putBoolean("lock", true);
        }
        StandOutWindow.sendData(this.mContext, AnswerSheetWindow.class, this.mAnswerSheetWindowID, 0, data, AnswerSheetWindow.class, this.mAnswerSheetWindowID);
    }

    public boolean onLongClick(View v) {
        if (!this.mbLocked) {
            new Builder(new ContextThemeWrapper(this.mContext, 16974130)).setItems(new String[]{"拍照", "绘画板", "文本", "语音", "视频(测试阶段)"}, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 4) {
                        if (!MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_MYILIBRARY_ADDVIDEO)) {
                            Utilities.showAlertMessage(QuestionWidgetsUtilities.this.mContext, "暂时无法添加视频", "您当前没有添加视频的权限。");
                            return;
                        } else if (CustomVideoView4.getOpenedPlayers() >= 2) {
                            Utilities.showAlertMessage(QuestionWidgetsUtilities.this.mContext, "暂时无法添加视频", "由于目前已经播放过至少2个视频作答，目前无法继续添加新的视频。\n\n请先退出并重新打开当前作业，并在播放任意一个视频前添加所需要的视频作答。");
                            return;
                        }
                    }
                    QuestionWidgetsUtilities.this.addContentBlock(which, "", true);
                    QuestionWidgetsUtilities.this.setChanged(true);
                }
            }).setTitle("选择要增加的内容").create().show();
        }
        return false;
    }

    protected void setChanged(boolean bChanged) {
        if (this.mCallBack != null && bChanged) {
            this.mCallBack.onChanged();
        }
    }

    public void finishLoad() {
        checkWindowObjects(false);
    }

    public synchronized void checkWindowObjects(boolean bNoChangeID) {
        RelativeLayout RelativeLayout = this.mRelativeLayout;
        for (int i = 0; i < RelativeLayout.getChildCount(); i++) {
            View OneView = RelativeLayout.getChildAt(i);
            if (OneView instanceof ImageView) {
                ImageView ImageView = (ImageView) OneView;
                LayoutParams Params = (LayoutParams) ImageView.getLayoutParams();
                Params.width = Utilities.dpToPixel(64, this.mContext);
                Params.height = Utilities.dpToPixel(64, this.mContext);
                OneView.setLayoutParams(Params);
                OneView.setBackgroundResource(Utilities.getThemeCustomResID(R.attr.question_widget_background, this.mContext));
                OneView.setOnClickListener(this.contentBlockImageViewClick);
                OneView.setOnLongClickListener(this.onLongClickListener);
                String szClassName = (String) OneView.getTag(TAGID_CLASSNAME);
                if (szClassName.equalsIgnoreCase("TextWindow")) {
                    ImageView.setImageResource(R.drawable.ic_text);
                } else if (szClassName.equalsIgnoreCase("DrawWindow")) {
                    ImageView.setImageResource(R.drawable.ic_drawpad);
                } else if (szClassName.equalsIgnoreCase("VoiceWindow")) {
                    ImageView.setImageResource(R.drawable.ic_voice);
                } else if (szClassName.equalsIgnoreCase("VideoWindow")) {
                    ImageView.setImageResource(R.drawable.ic_multimedia);
                } else if (szClassName.equalsIgnoreCase("CameraWindow")) {
                    ImageView.setImageResource(R.drawable.ic_camera);
                }
                if (!bNoChangeID) {
                    OneView.setTag(TAGID_ID, String.valueOf(mWindowID));
                    mWindowID = Integer.valueOf(mWindowID.intValue() + 1);
                }
            } else if (OneView instanceof IComponents) {
                OneView.setBackgroundResource(Utilities.getThemeCustomResID(R.attr.question_widget_background, this.mContext));
                OneView.setOnLongClickListener(this.onLongClickListener);
                ((IComponents) OneView).setCallBack(this);
            }
        }
    }

    public static void closeAllWindow(Context context) {
        StandOutWindow.closeAll(context, DrawWindow.class);
        StandOutWindow.closeAll(context, TextWindow.class);
        StandOutWindow.closeAll(context, VoiceWindow.class);
        StandOutWindow.closeAll(context, VideoWindow.class);
        StandOutWindow.closeAll(context, CameraWindow.class);
        StandOutWindow.closeAll(context, AnswerSheetWindow.class);
        StandOutWindow.closeAll(context, AnswerSheetV2OthersWindow.class);
        StandOutWindow.closeAll(context, AnswerSheetV3OthersWindow.class);
    }

    public static void hideAllWindow(Context context) {
        StandOutWindow.hideAll(context, DrawWindow.class);
        StandOutWindow.hideAll(context, TextWindow.class);
        StandOutWindow.hideAll(context, VoiceWindow.class);
        StandOutWindow.hideAll(context, VideoWindow.class);
        StandOutWindow.hideAll(context, CameraWindow.class);
    }

    public static void restoreAllWindow(Context context) {
        StandOutWindow.restoreAll(context, DrawWindow.class);
        StandOutWindow.restoreAll(context, TextWindow.class);
        StandOutWindow.restoreAll(context, VoiceWindow.class);
        StandOutWindow.restoreAll(context, VideoWindow.class);
        StandOutWindow.restoreAll(context, CameraWindow.class);
        StandOutWindow.restoreAll(context, AnswerSheetWindow.class);
        StandOutWindow.restoreAll(context, AnswerSheetV2OthersWindow.class);
        StandOutWindow.restoreAll(context, AnswerSheetV3OthersWindow.class);
    }

    public void OnDataLoaded(String szFileName, IComponents Component) {
    }

    public void OnDataUploaded(String szData, IComponents Component) {
        setChanged(true);
    }

    public void OnRequestIntent(Intent intent, IComponents Component) {
        mLastCallComponent = Component;
        Intent newIntent;
        if (Component instanceof VideoComponent) {
            newIntent = new Intent(this.mContext, CameraRecordActivity.class);
            newIntent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
            this.mContext.startActivity(newIntent);
        } else if (Component instanceof CameraComponent) {
            newIntent = new Intent(this.mContext, CameraCaptureActivity.class);
            newIntent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
            this.mContext.startActivity(newIntent);
        }
    }

    public static boolean setIntentCallBack(Intent intent) {
        if (mLastCallComponent == null) {
            return false;
        }
        mLastCallComponent.intentComplete(intent);
        return true;
    }
}
