package com.netspace.library.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.RemoteViews;
import com.netspace.library.utilities.Utilities;
import java.util.ArrayList;

public class StatusBarDisplayer extends UIDisplayer {
    private static final int MSG_HIDEMESSAGE = 6;
    private static final int MSG_HIDEPROGRESS = 4;
    private static final int MSG_PROGRESSCURRENT = 3;
    private static final int MSG_SHOWMESSAGE = 2;
    private static final int MSG_SHOWPROGRESS = 1;
    private static final int MSG_UPDATETEXT = 5;
    private static volatile Integer mInstanceCount = Integer.valueOf(0);
    private static ArrayList<StatusBarDisplayer> marrStatusBars = new ArrayList();
    private Builder mBuilder;
    private Context mContext;
    private int mCurrentInstanceIndex = 0;
    private StatusMessageHandler mMessageHandler;
    private NotificationPrepareInterface mNotifPrepareCallBack;
    private int mNotifyID = -1;
    private NotificationManager mNotifyManager;
    private PendingIntent mPendingIntent = null;
    private boolean mShutdown = false;
    private final Runnable mUpdateProgressRunnable = new Runnable() {
        private int mnLastProgress = -1;

        public void run() {
            if (StatusBarDisplayer.this.mMessageHandler != null && StatusBarDisplayer.this.mProgressMax > 0 && StatusBarDisplayer.this.mProgressCurrent >= 0 && this.mnLastProgress != StatusBarDisplayer.this.mProgressCurrent) {
                StatusBarDisplayer.this.mMessageHandler.obtainMessage(3, StatusBarDisplayer.this.mProgressMax, StatusBarDisplayer.this.mProgressCurrent).sendToTarget();
                this.mnLastProgress = StatusBarDisplayer.this.mProgressCurrent;
            }
            StatusBarDisplayer.this.mMessageHandler.postDelayed(this, 1000);
        }
    };
    private ArrayList<Integer> marrMessagesID = new ArrayList();
    private boolean mbAlwaysHere = false;
    private boolean mbSound = false;
    private boolean mbVibrate = false;

    public interface NotificationPrepareInterface {
        void prepareNotification(Notification notification);
    }

    private class StatusMessageHandler extends Handler {
        private StatusMessageHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (StatusBarDisplayer.this.mShutdown) {
                        Log.e("StatusBarDisplayer", "shutdown is received before progress is shown.");
                    }
                    if (StatusBarDisplayer.this.mBuilder == null) {
                        StatusBarDisplayer.this.mBuilder = new Builder(StatusBarDisplayer.this.mContext);
                        StatusBarDisplayer.this.mBuilder.setContentTitle(StatusBarDisplayer.this.mTitle).setContentText(StatusBarDisplayer.this.mText).setSmallIcon(StatusBarDisplayer.this.mIconResID).setAutoCancel(false).setOngoing(true);
                        StatusBarDisplayer.this.mNotifyManager.notify(StatusBarDisplayer.this.mNotifyID, StatusBarDisplayer.this.mBuilder.build());
                        return;
                    }
                    return;
                case 2:
                    Builder Builder = new Builder(StatusBarDisplayer.this.mContext);
                    Builder.setContentTitle(StatusBarDisplayer.this.mTitle).setContentText(StatusBarDisplayer.this.mText).setSmallIcon(StatusBarDisplayer.this.mIconResID).setAutoCancel(false).setProgress(0, 0, false).setOngoing(StatusBarDisplayer.this.mbAlwaysHere);
                    if (StatusBarDisplayer.this.mPendingIntent != null) {
                        Builder.setContentIntent(StatusBarDisplayer.this.mPendingIntent);
                    }
                    Notification notif = Builder.build();
                    if (StatusBarDisplayer.this.mbSound) {
                        notif.defaults |= 1;
                        notif.defaults |= 4;
                    }
                    if (StatusBarDisplayer.this.mbVibrate) {
                        notif.defaults |= 2;
                    }
                    if (StatusBarDisplayer.this.mOverlappedLayoutID != 0) {
                        notif.contentView = new RemoteViews(StatusBarDisplayer.this.mContext.getPackageName(), StatusBarDisplayer.this.mOverlappedLayoutID);
                    }
                    if (StatusBarDisplayer.this.mNotifPrepareCallBack != null) {
                        StatusBarDisplayer.this.mNotifPrepareCallBack.prepareNotification(notif);
                    }
                    StatusBarDisplayer.this.mNotifyManager.notify(StatusBarDisplayer.this.mNotifyID, notif);
                    StatusBarDisplayer.this.marrMessagesID.add(Integer.valueOf(StatusBarDisplayer.this.mNotifyID));
                    StatusBarDisplayer statusBarDisplayer = StatusBarDisplayer.this;
                    statusBarDisplayer.mNotifyID = statusBarDisplayer.mNotifyID + 1;
                    return;
                case 3:
                    if (StatusBarDisplayer.this.mBuilder != null) {
                        StatusBarDisplayer.this.mBuilder.setProgress(msg.arg1, msg.arg2, false);
                        StatusBarDisplayer.this.mBuilder.setOngoing(true);
                        StatusBarDisplayer.this.mNotifyManager.notify(StatusBarDisplayer.this.mNotifyID, StatusBarDisplayer.this.mBuilder.build());
                        return;
                    }
                    return;
                case 4:
                    if (StatusBarDisplayer.this.mBuilder != null) {
                        StatusBarDisplayer.this.mNotifyManager.cancel(StatusBarDisplayer.this.mNotifyID);
                        StatusBarDisplayer.this.mBuilder = null;
                        return;
                    }
                    Log.d("StatusBarDisplayer", "progress builder is not vaild.");
                    return;
                case 5:
                    if (StatusBarDisplayer.this.mBuilder != null) {
                        StatusBarDisplayer.this.mBuilder.setContentText(StatusBarDisplayer.this.mText).setContentTitle(StatusBarDisplayer.this.mTitle);
                        return;
                    }
                    return;
                case 6:
                    for (int i = 0; i < StatusBarDisplayer.this.marrMessagesID.size(); i++) {
                        StatusBarDisplayer.this.mNotifyManager.cancel(((Integer) StatusBarDisplayer.this.marrMessagesID.get(i)).intValue());
                    }
                    StatusBarDisplayer.this.marrMessagesID.clear();
                    return;
                default:
                    return;
            }
        }
    }

    public StatusBarDisplayer(Context Context) {
        super(null);
        synchronized (mInstanceCount) {
            mInstanceCount = Integer.valueOf(mInstanceCount.intValue() + 1);
            this.mCurrentInstanceIndex = mInstanceCount.intValue();
        }
        this.mNotifyManager = (NotificationManager) Context.getSystemService("notification");
        this.mContext = Context;
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            this.mMessageHandler = new StatusMessageHandler();
        } else {
            Utilities.runOnUIThread(this.mContext, new Runnable() {
                public void run() {
                    StatusBarDisplayer.this.mMessageHandler = new StatusMessageHandler();
                }
            });
            while (this.mMessageHandler == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        this.mIconResID = 17301581;
        synchronized (marrStatusBars) {
            marrStatusBars.add(this);
        }
    }

    public void setNotifyID(int nID) {
        this.mNotifyID = nID;
    }

    public void setAlwaysHere(boolean bAlways) {
        this.mbAlwaysHere = bAlways;
    }

    public void setSound(boolean bOn) {
        this.mbSound = bOn;
    }

    public void setVibrate(boolean bOn) {
        this.mbVibrate = bOn;
    }

    public void setPendingIntent(PendingIntent PendingIntent) {
        this.mPendingIntent = PendingIntent;
    }

    public void setNotifPrepareCallBack(NotificationPrepareInterface CallBack) {
        this.mNotifPrepareCallBack = CallBack;
    }

    public void startIntent() {
        if (this.mPendingIntent != null) {
            try {
                this.mPendingIntent.send(this.mContext, 0, new Intent());
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    public void setProgress(int nPos) {
        super.setProgress(nPos);
    }

    public void increaseProgress() {
        super.increaseProgress();
    }

    public void hideMessage() {
        if (this.mMessageHandler != null) {
            this.mMessageHandler.removeMessages(2);
            this.mMessageHandler.obtainMessage(6).sendToTarget();
        }
    }

    public void setProgressMax(int nMax) {
        super.setProgressMax(nMax);
    }

    public void setText(String szText) {
        super.setText(szText);
        if (this.mMessageHandler != null) {
            this.mMessageHandler.obtainMessage(5).sendToTarget();
        }
    }

    public void setTitle(String szTitle) {
        super.setTitle(szTitle);
        if (this.mMessageHandler != null) {
            this.mMessageHandler.obtainMessage(5).sendToTarget();
        }
    }

    public boolean showAlertBox() {
        if (this.mNotifyID == -1) {
            this.mNotifyID = this.mCurrentInstanceIndex * 10000;
        }
        if (this.mMessageHandler == null) {
            return false;
        }
        this.mMessageHandler.obtainMessage(2).sendToTarget();
        return true;
    }

    public boolean showProgressBox(OnClickListener OnCancelClick) {
        if (this.mNotifyID == -1) {
            this.mNotifyID = this.mCurrentInstanceIndex * 1000;
        }
        if (this.mMessageHandler == null) {
            return false;
        }
        this.mMessageHandler.postDelayed(this.mUpdateProgressRunnable, 1000);
        this.mMessageHandler.obtainMessage(1).sendToTarget();
        return true;
    }

    public void hideProgressBox() {
        super.hideProgressBox();
        if (this.mMessageHandler != null) {
            this.mMessageHandler.removeMessages(1);
            if (this.mBuilder != null) {
                this.mMessageHandler.obtainMessage(4).sendToTarget();
            }
        }
    }

    public void shutDown() {
        super.shutDown();
        this.mShutdown = true;
        this.mMessageHandler.removeCallbacks(this.mUpdateProgressRunnable);
        hideMessage();
        hideProgressBox();
        synchronized (marrStatusBars) {
            for (int i = 0; i < marrStatusBars.size(); i++) {
                if (((StatusBarDisplayer) marrStatusBars.get(i)).equals(this)) {
                    marrStatusBars.remove(i);
                    break;
                }
            }
        }
    }

    public static void shutdownAll() {
        ArrayList<StatusBarDisplayer> arrStatusBars = (ArrayList) marrStatusBars.clone();
        for (int i = 0; i < arrStatusBars.size(); i++) {
            ((StatusBarDisplayer) arrStatusBars.get(i)).shutDown();
        }
        marrStatusBars.clear();
    }
}
