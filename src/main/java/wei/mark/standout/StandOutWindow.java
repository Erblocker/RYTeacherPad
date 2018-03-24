package wei.mark.standout;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public abstract class StandOutWindow extends Service {
    public static final String ACTION_CLOSE = "CLOSE";
    public static final String ACTION_CLOSE_ALL = "CLOSE_ALL";
    public static final String ACTION_HIDE = "HIDE";
    public static final String ACTION_HIDE_ALL = "HIDE_ALL";
    public static final String ACTION_RESTORE = "RESTORE";
    public static final String ACTION_RESTORE_ALL = "RESTORE_ALL";
    public static final String ACTION_SEND_DATA = "SEND_DATA";
    public static final String ACTION_SHOW = "SHOW";
    public static final int DEFAULT_ID = 0;
    public static final int DISREGARD_ID = -2;
    public static final int ONGOING_NOTIFICATION_ID = -1;
    static final String TAG = "StandOutWindow";
    static Window sFocusedWindow = null;
    static WindowCache sWindowCache = new WindowCache();
    LayoutInflater mLayoutInflater;
    private NotificationManager mNotificationManager;
    WindowManager mWindowManager;
    private boolean startedForeground;

    protected class DropDownListItem {
        public Runnable action;
        public String description;
        public int icon;

        public DropDownListItem(int icon, String description, Runnable action) {
            this.icon = icon;
            this.description = description;
            this.action = action;
        }

        public String toString() {
            return this.description;
        }
    }

    public class StandOutLayoutParams extends LayoutParams {
        public static final int AUTO_POSITION = -2147483647;
        public static final int BOTTOM = Integer.MAX_VALUE;
        public static final int CENTER = Integer.MIN_VALUE;
        public static final int LEFT = 0;
        public static final int RIGHT = Integer.MAX_VALUE;
        public static final int TOP = 0;
        public int maxHeight;
        public int maxWidth;
        public int minHeight;
        public int minWidth;
        public int threshold;

        public StandOutLayoutParams(int id) {
            super(200, 200, 2002, 262176, -3);
            int windowFlags = StandOutWindow.this.getFlags(id);
            setFocusFlag(false);
            if (!Utils.isSet(windowFlags, StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE)) {
                this.flags |= 512;
            }
            this.x = getX(id, this.width);
            this.y = getY(id, this.height);
            this.gravity = 51;
            this.threshold = 10;
            this.minHeight = 0;
            this.minWidth = 0;
            this.maxHeight = Integer.MAX_VALUE;
            this.maxWidth = Integer.MAX_VALUE;
        }

        public StandOutLayoutParams(StandOutWindow standOutWindow, int id, int w, int h) {
            this(id);
            this.width = w;
            this.height = h;
        }

        public StandOutLayoutParams(StandOutWindow standOutWindow, int id, int w, int h, int xpos, int ypos) {
            this(standOutWindow, id, w, h);
            if (xpos != AUTO_POSITION) {
                this.x = xpos;
            }
            if (ypos != AUTO_POSITION) {
                this.y = ypos;
            }
            Display display = standOutWindow.mWindowManager.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            if (this.x == Integer.MAX_VALUE) {
                this.x = width - w;
            } else if (this.x == Integer.MIN_VALUE) {
                this.x = (width - w) / 2;
            }
            if (this.y == Integer.MAX_VALUE) {
                this.y = height - h;
            } else if (this.y == Integer.MIN_VALUE) {
                this.y = (height - h) / 2;
            }
        }

        public StandOutLayoutParams(StandOutWindow standOutWindow, int id, int w, int h, int xpos, int ypos, int minWidth, int minHeight) {
            this(standOutWindow, id, w, h, xpos, ypos);
            this.minWidth = minWidth;
            this.minHeight = minHeight;
        }

        public StandOutLayoutParams(StandOutWindow standOutWindow, int id, int w, int h, int xpos, int ypos, int minWidth, int minHeight, int threshold) {
            this(standOutWindow, id, w, h, xpos, ypos, minWidth, minHeight);
            this.threshold = threshold;
        }

        private int getX(int id, int width) {
            return ((StandOutWindow.sWindowCache.size() * 100) + (id * 100)) % (StandOutWindow.this.mWindowManager.getDefaultDisplay().getWidth() - width);
        }

        private int getY(int id, int height) {
            Display display = StandOutWindow.this.mWindowManager.getDefaultDisplay();
            return ((StandOutWindow.sWindowCache.size() * 100) + (this.x + (((id * 100) * 200) / (display.getWidth() - this.width)))) % (display.getHeight() - height);
        }

        public void setFocusFlag(boolean focused) {
            if (focused) {
                this.flags ^= 8;
            } else {
                this.flags |= 8;
            }
        }
    }

    public abstract void createAndAttachView(int i, FrameLayout frameLayout);

    public abstract int getAppIcon();

    public abstract String getAppName();

    public abstract StandOutLayoutParams getParams(int i, Window window);

    public static void show(Context context, Class<? extends StandOutWindow> cls, int id) {
        context.startService(getShowIntent(context, cls, id));
    }

    public static void hide(Context context, Class<? extends StandOutWindow> cls, int id) {
        context.startService(getHideIntent(context, cls, id));
    }

    public static void close(Context context, Class<? extends StandOutWindow> cls, int id) {
        context.startService(getCloseIntent(context, cls, id));
    }

    public static void closeAll(Context context, Class<? extends StandOutWindow> cls) {
        context.startService(getCloseAllIntent(context, cls));
    }

    public static void hideAll(Context context, Class<? extends StandOutWindow> cls) {
        context.startService(getHideAllIntent(context, cls));
    }

    public static void restoreAll(Context context, Class<? extends StandOutWindow> cls) {
        context.startService(getRestoreAllIntent(context, cls));
    }

    public static void sendData(Context context, Class<? extends StandOutWindow> toCls, int toId, int requestCode, Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
        context.startService(getSendDataIntent(context, toCls, toId, requestCode, data, fromCls, fromId));
    }

    public static Intent getShowIntent(Context context, Class<? extends StandOutWindow> cls, int id) {
        boolean cached = sWindowCache.isCached(id, cls);
        return new Intent(context, cls).putExtra("id", id).setAction(cached ? ACTION_RESTORE : ACTION_SHOW).setData(cached ? Uri.parse("standout://" + cls + '/' + id) : null);
    }

    public static Intent getHideIntent(Context context, Class<? extends StandOutWindow> cls, int id) {
        return new Intent(context, cls).putExtra("id", id).setAction(ACTION_HIDE);
    }

    public static Intent getCloseIntent(Context context, Class<? extends StandOutWindow> cls, int id) {
        return new Intent(context, cls).putExtra("id", id).setAction(ACTION_CLOSE);
    }

    public static Intent getCloseAllIntent(Context context, Class<? extends StandOutWindow> cls) {
        return new Intent(context, cls).setAction(ACTION_CLOSE_ALL);
    }

    public static Intent getHideAllIntent(Context context, Class<? extends StandOutWindow> cls) {
        return new Intent(context, cls).setAction(ACTION_HIDE_ALL);
    }

    public static Intent getRestoreAllIntent(Context context, Class<? extends StandOutWindow> cls) {
        return new Intent(context, cls).setAction(ACTION_RESTORE_ALL);
    }

    public static Intent getSendDataIntent(Context context, Class<? extends StandOutWindow> toCls, int toId, int requestCode, Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
        return new Intent(context, toCls).putExtra("id", toId).putExtra("requestCode", requestCode).putExtra("wei.mark.standout.data", data).putExtra("wei.mark.standout.fromCls", fromCls).putExtra("fromId", fromId).setAction(ACTION_SEND_DATA);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        this.mWindowManager = (WindowManager) getSystemService("window");
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        this.mLayoutInflater = (LayoutInflater) getSystemService("layout_inflater");
        this.startedForeground = false;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            String action = intent.getAction();
            int id = intent.getIntExtra("id", 0);
            if (id == -1) {
                throw new RuntimeException("ID cannot equals StandOutWindow.ONGOING_NOTIFICATION_ID");
            } else if (ACTION_SHOW.equals(action) || ACTION_RESTORE.equals(action)) {
                show(id);
            } else if (ACTION_HIDE.equals(action)) {
                hide(id);
            } else if (ACTION_CLOSE.equals(action)) {
                close(id);
            } else if (ACTION_CLOSE_ALL.equals(action)) {
                closeAll();
            } else if (ACTION_HIDE_ALL.equals(action)) {
                hideAll();
            } else if (ACTION_RESTORE_ALL.equals(action)) {
                restoreAll();
            } else if (ACTION_SEND_DATA.equals(action)) {
                if (!(isExistingId(id) || id == -2)) {
                    Log.w(TAG, "Sending data to non-existant window. If this is not intended, make sure toId is either an existing window's id or DISREGARD_ID.");
                }
                Bundle data = intent.getBundleExtra("wei.mark.standout.data");
                onReceiveData(id, intent.getIntExtra("requestCode", 0), data, (Class) intent.getSerializableExtra("wei.mark.standout.fromCls"), intent.getIntExtra("fromId", 0));
            }
        } else {
            Log.w(TAG, "Tried to onStartCommand() with a null intent.");
        }
        return 2;
    }

    public void onDestroy() {
        super.onDestroy();
        closeAll();
    }

    public int getFlags(int id) {
        return 0;
    }

    public String getTitle(int id) {
        return getAppName();
    }

    public int getIcon(int id) {
        return getAppIcon();
    }

    public String getPersistentNotificationTitle(int id) {
        return getAppName() + " 正在运行";
    }

    public String getPersistentNotificationMessage(int id) {
        return "";
    }

    public Intent getPersistentNotificationIntent(int id) {
        return null;
    }

    public int getHiddenIcon() {
        return getAppIcon();
    }

    public String getHiddenNotificationTitle(int id) {
        return getAppName() + " Hidden";
    }

    public String getHiddenNotificationMessage(int id) {
        return "";
    }

    public Intent getHiddenNotificationIntent(int id) {
        return null;
    }

    public Notification getPersistentNotification(int id) {
        int icon = getAppIcon();
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String tickerText = String.format("%s: %s", new Object[]{getPersistentNotificationTitle(id), getPersistentNotificationMessage(id)});
        Intent notificationIntent = getPersistentNotificationIntent(id);
        PendingIntent contentIntent = null;
        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, UUID.randomUUID().hashCode(), notificationIntent, 134217728);
        }
        Notification notification = new Notification(icon, tickerText, when);
        notification.setLatestEventInfo(c, contentTitle, contentText, contentIntent);
        return notification;
    }

    public Notification getHiddenNotification(int id) {
        int icon = getHiddenIcon();
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String tickerText = String.format("%s: %s", new Object[]{getHiddenNotificationTitle(id), getHiddenNotificationMessage(id)});
        Intent notificationIntent = getHiddenNotificationIntent(id);
        PendingIntent contentIntent = null;
        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, 0, notificationIntent, 134217728);
        }
        Notification notification = new Notification(icon, tickerText, when);
        notification.setLatestEventInfo(c, contentTitle, contentText, contentIntent);
        return notification;
    }

    public Animation getShowAnimation(int id) {
        return AnimationUtils.loadAnimation(this, 17432576);
    }

    public Animation getHideAnimation(int id) {
        return AnimationUtils.loadAnimation(this, 17432577);
    }

    public Animation getCloseAnimation(int id) {
        return AnimationUtils.loadAnimation(this, 17432577);
    }

    public int getThemeStyle() {
        return 0;
    }

    public PopupWindow getDropDown(int id) {
        List<DropDownListItem> items;
        List<DropDownListItem> dropDownListItems = getDropDownItems(id);
        if (dropDownListItems != null) {
            items = dropDownListItems;
        } else {
            items = new ArrayList();
        }
        items.add(new DropDownListItem(17301560, "关闭所有 " + getAppName(), new Runnable() {
            public void run() {
                StandOutWindow.this.closeAll();
            }
        }));
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(1);
        final PopupWindow dropDown = new PopupWindow(list, -2, -2, true);
        for (final DropDownListItem item : items) {
            ViewGroup listItem = (ViewGroup) this.mLayoutInflater.inflate(R.layout.drop_down_list_item, null);
            list.addView(listItem);
            ((ImageView) listItem.findViewById(R.id.icon)).setImageResource(item.icon);
            ((TextView) listItem.findViewById(R.id.description)).setText(item.description);
            listItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    item.action.run();
                    dropDown.dismiss();
                }
            });
        }
        dropDown.setBackgroundDrawable(getResources().getDrawable(17301530));
        return dropDown;
    }

    public List<DropDownListItem> getDropDownItems(int id) {
        return null;
    }

    public boolean onTouchBody(int id, Window window, View view, MotionEvent event) {
        return false;
    }

    public void onMove(int id, Window window, View view, MotionEvent event) {
    }

    public void onTouchWindowButton(int nButtonID, int id, Window window, View view) {
    }

    public void onResize(int id, Window window, View view, MotionEvent event) {
    }

    public boolean onShow(int id, Window window) {
        return false;
    }

    public boolean onHide(int id, Window window) {
        return false;
    }

    public boolean onClose(int id, Window window) {
        return false;
    }

    public boolean onCloseAll() {
        return false;
    }

    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> cls, int fromId) {
    }

    public boolean onUpdate(int id, Window window, StandOutLayoutParams params) {
        return false;
    }

    public boolean onBringToFront(int id, Window window) {
        return false;
    }

    public boolean onFocusChange(int id, Window window, boolean focus) {
        return false;
    }

    public boolean onKeyEvent(int id, Window window, KeyEvent event) {
        return false;
    }

    public final synchronized Window show(int id) {
        Window window;
        Window cachedWindow = getWindow(id);
        if (cachedWindow != null) {
            window = cachedWindow;
        } else {
            window = new Window(this, id);
        }
        if (onShow(id, window)) {
            Log.d(TAG, "Window " + id + " show cancelled by implementation.");
            window = null;
        } else if (window.visibility == 1) {
            Log.d(TAG, "Window " + id + " is already shown.");
            focus(id);
        } else {
            window.visibility = 1;
            Animation animation = getShowAnimation(id);
            try {
                this.mWindowManager.addView(window, window.getLayoutParams());
                if (animation != null) {
                    window.getChildAt(0).startAnimation(animation);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sWindowCache.putCache(id, getClass(), window);
            Notification notification = getPersistentNotification(id);
            if (notification != null) {
                notification.flags |= 32;
                this.mNotificationManager.notify(getClass().hashCode() + id, notification);
            } else if (!this.startedForeground) {
                throw new RuntimeException("Your StandOutWindow service mustprovide a persistent notification.The notification prevents Androidfrom killing your service in lowmemory situations.");
            }
            focus(id);
        }
        return window;
    }

    public final synchronized void hide(int id) {
        final Window window = getWindow(id);
        if (window == null) {
            throw new IllegalArgumentException("Tried to hide(" + id + ") a null window.");
        } else if (onHide(id, window)) {
            Log.d(TAG, "Window " + id + " hide cancelled by implementation.");
        } else {
            if (window.visibility == 0) {
                Log.d(TAG, "Window " + id + " is already hidden.");
            }
            if (Utils.isSet(window.flags, StandOutFlags.FLAG_WINDOW_HIDE_ENABLE)) {
                window.visibility = 2;
                Notification notification = getHiddenNotification(id);
                Animation animation = getHideAnimation(id);
                if (animation != null) {
                    try {
                        animation.setAnimationListener(new AnimationListener() {
                            public void onAnimationStart(Animation animation) {
                            }

                            public void onAnimationRepeat(Animation animation) {
                            }

                            public void onAnimationEnd(Animation animation) {
                                StandOutWindow.this.mWindowManager.removeView(window);
                                window.visibility = 0;
                            }
                        });
                        window.getChildAt(0).startAnimation(animation);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    this.mWindowManager.removeView(window);
                }
                notification.flags = (notification.flags | 32) | 16;
                this.mNotificationManager.notify(getClass().hashCode() + id, notification);
            } else {
                close(id);
            }
        }
    }

    public final synchronized void hideWindow(int id) {
        Window window = getWindow(id);
        if (window == null) {
            throw new IllegalArgumentException("Tried to hide(" + id + ") a null window.");
        } else if (window.getVisibility() != 4) {
            window.setVisibility(4);
        }
    }

    public final synchronized void restoreWindow(int id) {
        Window window = getWindow(id);
        if (window == null) {
            throw new IllegalArgumentException("Tried to hide(" + id + ") a null window.");
        } else if (window.getVisibility() != 0) {
            window.setVisibility(0);
        }
    }

    public final synchronized void close(final int id) {
        final Window window = getWindow(id);
        if (window != null) {
            if (window.visibility != 2) {
                if (onClose(id, window)) {
                    Log.w(TAG, "Window " + id + " close cancelled by implementation.");
                } else {
                    this.mNotificationManager.cancel(getClass().hashCode() + id);
                    unfocus(window);
                    window.visibility = 2;
                    Animation animation = getCloseAnimation(id);
                    if (animation != null) {
                        try {
                            animation.setAnimationListener(new AnimationListener() {
                                public void onAnimationStart(Animation animation) {
                                }

                                public void onAnimationRepeat(Animation animation) {
                                }

                                public void onAnimationEnd(Animation animation) {
                                    StandOutWindow.this.mWindowManager.removeView(window);
                                    window.visibility = 0;
                                    StandOutWindow.sWindowCache.removeCache(id, StandOutWindow.this.getClass());
                                    if (StandOutWindow.this.getExistingIds().size() == 0) {
                                        StandOutWindow.this.startedForeground = false;
                                        StandOutWindow.this.stopForeground(true);
                                    }
                                }
                            });
                            window.getChildAt(0).startAnimation(animation);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        this.mWindowManager.removeView(window);
                        sWindowCache.removeCache(id, getClass());
                        if (sWindowCache.getCacheSize(getClass()) == 0) {
                            this.startedForeground = false;
                            stopForeground(true);
                        }
                    }
                }
            }
        }
    }

    public final synchronized void hideAll() {
        LinkedList<Integer> ids = new LinkedList();
        for (Integer intValue : getExistingIds()) {
            ids.add(Integer.valueOf(intValue.intValue()));
        }
        Iterator it = ids.iterator();
        while (it.hasNext()) {
            hideWindow(((Integer) it.next()).intValue());
        }
    }

    public final synchronized void restoreAll() {
        LinkedList<Integer> ids = new LinkedList();
        for (Integer intValue : getExistingIds()) {
            ids.add(Integer.valueOf(intValue.intValue()));
        }
        Iterator it = ids.iterator();
        while (it.hasNext()) {
            restoreWindow(((Integer) it.next()).intValue());
        }
    }

    public final synchronized void closeAll() {
        if (onCloseAll()) {
            Log.w(TAG, "Windows close all cancelled by implementation.");
        } else {
            LinkedList<Integer> ids = new LinkedList();
            for (Integer intValue : getExistingIds()) {
                ids.add(Integer.valueOf(intValue.intValue()));
            }
            Iterator it = ids.iterator();
            while (it.hasNext()) {
                close(((Integer) it.next()).intValue());
            }
        }
    }

    public final void sendData(int fromId, Class<? extends StandOutWindow> toCls, int toId, int requestCode, Bundle data) {
        sendData(this, toCls, toId, requestCode, data, getClass(), fromId);
    }

    public final synchronized void bringToFront(int id) {
        Window window = getWindow(id);
        if (window == null) {
            throw new IllegalArgumentException("Tried to bringToFront(" + id + ") a null window.");
        } else if (window.visibility == 0) {
            throw new IllegalStateException("Tried to bringToFront(" + id + ") a window that is not shown.");
        } else if (window.visibility != 2) {
            if (onBringToFront(id, window)) {
                Log.w(TAG, "Window " + id + " bring to front cancelled by implementation.");
            } else {
                StandOutLayoutParams params = window.getLayoutParams();
                try {
                    this.mWindowManager.removeView(window);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    this.mWindowManager.addView(window, params);
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        }
    }

    public final synchronized boolean focus(int id) {
        boolean onFocus;
        Window window = getWindow(id);
        if (window == null) {
            Log.e(TAG, "Tried to focus(" + id + ") a null window.");
        } else if (!Utils.isSet(window.flags, StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE)) {
            if (sFocusedWindow != null) {
                unfocus(sFocusedWindow);
            }
            onFocus = window.onFocus(true);
        }
        onFocus = false;
        return onFocus;
    }

    public final synchronized boolean unfocus(int id) {
        return unfocus(getWindow(id));
    }

    public final int getUniqueId() {
        int unique = 0;
        for (Integer intValue : getExistingIds()) {
            unique = Math.max(unique, intValue.intValue() + 1);
        }
        return unique;
    }

    public final boolean isExistingId(int id) {
        return sWindowCache.isCached(id, getClass());
    }

    public final Set<Integer> getExistingIds() {
        return sWindowCache.getCacheIds(getClass());
    }

    public final Window getWindow(int id) {
        return sWindowCache.getCache(id, getClass());
    }

    public final Window getFocusedWindow() {
        return sFocusedWindow;
    }

    public final void setFocusedWindow(Window window) {
        sFocusedWindow = window;
    }

    public final void setTitle(int id, String text) {
        Window window = getWindow(id);
        if (window != null) {
            View title = window.findViewById(R.id.title);
            if (title instanceof TextView) {
                ((TextView) title).setText(text);
            }
        }
    }

    public final void setIcon(int id, int drawableRes) {
        Window window = getWindow(id);
        if (window != null) {
            View icon = window.findViewById(R.id.window_icon);
            if (icon instanceof ImageView) {
                ((ImageView) icon).setImageResource(drawableRes);
            }
        }
    }

    public boolean onTouchHandleMove(int id, Window window, View view, MotionEvent event) {
        boolean tap = false;
        StandOutLayoutParams params = window.getLayoutParams();
        int totalDeltaX = window.touchInfo.lastX - window.touchInfo.firstX;
        int totalDeltaY = window.touchInfo.lastY - window.touchInfo.firstY;
        switch (event.getAction()) {
            case 0:
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();
                window.touchInfo.firstX = window.touchInfo.lastX;
                window.touchInfo.firstY = window.touchInfo.lastY;
                break;
            case 1:
                window.touchInfo.moving = false;
                if (event.getPointerCount() != 1) {
                    if (Utils.isSet(window.flags, StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH)) {
                        bringToFront(id);
                        break;
                    }
                }
                if (Math.abs(totalDeltaX) < params.threshold && Math.abs(totalDeltaY) < params.threshold) {
                    tap = true;
                }
                if (tap && Utils.isSet(window.flags, StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP)) {
                    bringToFront(id);
                    break;
                }
                break;
            case 2:
                int deltaX = ((int) event.getRawX()) - window.touchInfo.lastX;
                int deltaY = ((int) event.getRawY()) - window.touchInfo.lastY;
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();
                if (window.touchInfo.moving || Math.abs(totalDeltaX) >= params.threshold || Math.abs(totalDeltaY) >= params.threshold) {
                    window.touchInfo.moving = true;
                    if (Utils.isSet(window.flags, StandOutFlags.FLAG_BODY_MOVE_ENABLE)) {
                        if (event.getPointerCount() == 1) {
                            params.x += deltaX;
                            params.y += deltaY;
                        }
                        window.edit().setPosition(params.x, params.y).commit();
                        break;
                    }
                }
                break;
        }
        onMove(id, window, view, event);
        return true;
    }

    public boolean onTouchHandleResize(int id, Window window, View view, MotionEvent event) {
        StandOutLayoutParams params = window.getLayoutParams();
        switch (event.getAction()) {
            case 0:
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();
                window.touchInfo.firstX = window.touchInfo.lastX;
                window.touchInfo.firstY = window.touchInfo.lastY;
                break;
            case 2:
                int deltaY = ((int) event.getRawY()) - window.touchInfo.lastY;
                params.width += ((int) event.getRawX()) - window.touchInfo.lastX;
                params.height += deltaY;
                if (params.width >= params.minWidth && params.width <= params.maxWidth) {
                    window.touchInfo.lastX = (int) event.getRawX();
                }
                if (params.height >= params.minHeight && params.height <= params.maxHeight) {
                    window.touchInfo.lastY = (int) event.getRawY();
                }
                window.edit().setSize(params.width, params.height).commit();
                break;
        }
        onResize(id, window, view, event);
        return true;
    }

    public synchronized boolean unfocus(Window window) {
        if (window == null) {
            throw new IllegalArgumentException("Tried to unfocus a null window.");
        }
        return window.onFocus(false);
    }

    public void updateViewLayout(int id, StandOutLayoutParams params) {
        Window window = getWindow(id);
        if (window == null) {
            Log.e(TAG, "Tried to updateViewLayout(" + id + ") a null window.");
        } else if (window.visibility != 0 && window.visibility != 2) {
            if (onUpdate(id, window, params)) {
                Log.w(TAG, "Window " + id + " update cancelled by implementation.");
                return;
            }
            try {
                window.setLayoutParams(params);
                this.mWindowManager.updateViewLayout(window, params);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
