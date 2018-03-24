package com.netspace.library.window;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.netspace.library.components.TextComponent;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import java.util.HashMap;
import org.apache.http.HttpStatus;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class TextWindow extends StandOutWindow {
    private HashMap<Integer, TextComponent> mMapRootView = new HashMap();

    public String getAppName() {
        return "文本窗";
    }

    public int getAppIcon() {
        return 17301560;
    }

    public void createAndAttachView(int id, FrameLayout frame) {
        Utilities.logWindow(this, "create");
        TextComponent RootView = new TextComponent(this);
        RootView.setAutoHeight(false);
        frame.addView(RootView, new LayoutParams(-1, -1));
        this.mMapRootView.put(Integer.valueOf(id), RootView);
    }

    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(this, id, 250, HttpStatus.SC_MULTIPLE_CHOICES, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public int getFlags(int id) {
        return ((((super.getFlags(id) | StandOutFlags.FLAG_DECORATION_SYSTEM) | StandOutFlags.FLAG_BODY_MOVE_ENABLE) | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE) | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE) | StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;
    }

    public String getPersistentNotificationMessage(int id) {
        return "点击这里关闭" + getAppName();
    }

    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, TextWindow.class, id);
    }

    public boolean onHide(int id, Window window) {
        Log.d("DrawWindow", "onHide " + id);
        return super.onHide(id, window);
    }

    public boolean onClose(int id, Window window) {
        Log.d("DrawWindow", "onClose " + id);
        Utilities.logWindow(this, "close");
        TextComponent rootView = (TextComponent) this.mMapRootView.get(Integer.valueOf(id));
        if (rootView != null) {
            QuestionWidgetsUtilities.updateWidgetData(Integer.valueOf(id), rootView.getData());
        }
        this.mMapRootView.remove(Integer.valueOf(id));
        return super.onClose(id, window);
    }

    public boolean onCloseAll() {
        Log.d("DrawWindow", "onCloseAll");
        this.mMapRootView.clear();
        return super.onCloseAll();
    }

    public int getThemeStyle() {
        return 16973934;
    }

    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> cls, int fromId) {
        TextComponent rootView = (TextComponent) this.mMapRootView.get(Integer.valueOf(id));
        if (rootView != null) {
            String changedText = data.getString("data");
            if (changedText != null) {
                rootView.setData(changedText);
            }
            if (data.getBoolean("lock")) {
                rootView.setLocked(true);
            }
        }
    }
}
