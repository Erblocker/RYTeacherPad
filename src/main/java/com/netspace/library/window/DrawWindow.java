package com.netspace.library.window;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.netspace.library.components.DrawComponent;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import java.util.HashMap;
import org.apache.http.HttpStatus;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class DrawWindow extends StandOutWindow {
    private HashMap<Integer, DrawComponent> mMapDrawView = new HashMap();

    public String getAppName() {
        return "绘画窗";
    }

    public int getAppIcon() {
        return 17301560;
    }

    public void createAndAttachView(int id, FrameLayout frame) {
        Utilities.logWindow(this, "create");
        DrawComponent RootView = new DrawComponent(this);
        RootView.setWindowMode(true);
        frame.addView(RootView, new LayoutParams(-1, -1));
        this.mMapDrawView.put(Integer.valueOf(id), RootView);
    }

    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(this, id, 250, HttpStatus.SC_MULTIPLE_CHOICES, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public int getFlags(int id) {
        return ((((super.getFlags(id) | StandOutFlags.FLAG_DECORATION_SYSTEM) | StandOutFlags.FLAG_BODY_MOVE_ENABLE) | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE) | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE) | StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;
    }

    public String getPersistentNotificationMessage(int id) {
        return "点击这里关闭绘画窗" + id;
    }

    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, DrawWindow.class, id);
    }

    public void onResize(int id, Window window, View view, MotionEvent event) {
        super.onResize(id, window, view, event);
    }

    public boolean onHide(int id, Window window) {
        Log.d("DrawWindow", "onHide " + id);
        return super.onHide(id, window);
    }

    public boolean onClose(int id, Window window) {
        Utilities.logWindow(this, "close");
        DrawComponent DrawView = (DrawComponent) this.mMapDrawView.get(Integer.valueOf(id));
        if (DrawView != null) {
            QuestionWidgetsUtilities.updateWidgetData(Integer.valueOf(id), DrawView.getData());
        }
        this.mMapDrawView.remove(Integer.valueOf(id));
        Log.d("DrawWindow", "onClose " + id);
        return super.onClose(id, window);
    }

    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> cls, int fromId) {
        DrawComponent rootView = (DrawComponent) this.mMapDrawView.get(Integer.valueOf(id));
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

    public boolean onCloseAll() {
        Log.d("DrawWindow", "onCloseAll");
        return super.onCloseAll();
    }
}
