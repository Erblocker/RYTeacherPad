package com.netspace.library.window;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.netspace.library.activity.ChatActivity;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import net.sqlcipher.database.SQLiteDatabase;
import wei.mark.standout.R;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class ChatWindow extends StandOutWindow {
    private ChatComponent mChatComponent = null;

    public String getAppName() {
        return "在线答疑窗";
    }

    public int getAppIcon() {
        return 17301560;
    }

    public void createAndAttachView(int id, FrameLayout frame) {
        Log.d("ChatWindow", "createAndAttachView");
        if (UI.getCurrentActivity() != null) {
            Activity activity = UI.getCurrentActivity();
            if (activity instanceof ChatActivity) {
                this.mChatComponent = null;
                activity.finish();
            }
        }
        createChatView(frame);
    }

    private void createChatView(FrameLayout frame) {
        Log.d("ChatWindow", "createChatView");
        Utilities.logWindow(this, "create");
        if (this.mChatComponent == null) {
            ChatComponent RootView = new ChatComponent(this);
            frame.addView(RootView, new LayoutParams(-1, -1));
            this.mChatComponent = RootView;
        } else {
            frame.addView(this.mChatComponent, new LayoutParams(-1, -1));
        }
        this.mChatComponent.setLocked(ChatComponent.getLocked());
    }

    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(this, id, 600, 350, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public int getFlags(int id) {
        return ((((super.getFlags(id) | StandOutFlags.FLAG_DECORATION_SYSTEM) | StandOutFlags.FLAG_BODY_MOVE_ENABLE) | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE) | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE) | StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;
    }

    public String getPersistentNotificationMessage(int id) {
        return "点击这里关闭在线答疑窗";
    }

    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, ChatWindow.class, id);
    }

    public void onResize(int id, Window window, View view, MotionEvent event) {
        super.onResize(id, window, view, event);
    }

    public boolean onHide(int id, Window window) {
        Log.d("ChatWindow", "onHide " + id);
        return super.onHide(id, window);
    }

    public boolean onClose(int id, Window window) {
        Utilities.logWindow(this, "close");
        if (this.mChatComponent != null) {
            this.mChatComponent = null;
        }
        Log.d("DrawWindow", "onClose " + id);
        return super.onClose(id, window);
    }

    public void onTouchWindowButton(int nButtonID, int id, Window window, View view) {
        if (nButtonID == R.id.maximize) {
            Intent intent = new Intent(this, ChatActivity.class);
            if (this.mChatComponent != null) {
                ((ViewGroup) this.mChatComponent.getParent()).removeView(this.mChatComponent);
                ChatActivity.setChatComponent(this.mChatComponent);
                this.mChatComponent = null;
            }
            intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
            startActivity(intent);
        }
    }

    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> cls, int fromId) {
    }

    public int getThemeStyle() {
        return 16973934;
    }

    public boolean onCloseAll() {
        Log.d("ChatWindow", "onCloseAll");
        this.mChatComponent = null;
        return super.onCloseAll();
    }
}
