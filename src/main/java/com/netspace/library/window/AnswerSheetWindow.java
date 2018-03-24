package com.netspace.library.window;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.netspace.library.components.AnswerSheetComponent;
import com.netspace.library.components.AnswerSheetComponent.AnswerSheetCallBack;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import io.vov.vitamio.MediaPlayer;
import java.util.HashMap;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class AnswerSheetWindow extends StandOutWindow {
    private HashMap<Integer, AnswerSheetComponent> mMapDrawView = new HashMap();

    public String getAppName() {
        return "答题卡";
    }

    public int getAppIcon() {
        return 17301560;
    }

    public void createAndAttachView(final int id, FrameLayout frame) {
        Utilities.logWindow(this, "create");
        AnswerSheetComponent RootView = new AnswerSheetComponent(this);
        RootView.setAnswerSheetCallBack(new AnswerSheetCallBack() {
            public void onSubmit() {
                StandOutWindow.close(AnswerSheetWindow.this, AnswerSheetWindow.class, id);
            }

            public void onCancel() {
                StandOutWindow.close(AnswerSheetWindow.this, AnswerSheetWindow.class, id);
            }
        });
        frame.addView(RootView, new LayoutParams(-1, -1));
        this.mMapDrawView.put(Integer.valueOf(id), RootView);
    }

    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(this, id, 450, MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING, Integer.MAX_VALUE, 0);
    }

    public int getFlags(int id) {
        return (((((super.getFlags(id) | StandOutFlags.FLAG_DECORATION_SYSTEM) | StandOutFlags.FLAG_BODY_MOVE_ENABLE) | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE) | StandOutFlags.FLAG_DECORATION_CLOSE_DISABLE) | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE) | StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;
    }

    public String getPersistentNotificationMessage(int id) {
        return "点击这里关闭答题卡窗" + id;
    }

    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, AnswerSheetWindow.class, id);
    }

    public void onResize(int id, Window window, View view, MotionEvent event) {
        super.onResize(id, window, view, event);
    }

    public boolean onHide(int id, Window window) {
        return super.onHide(id, window);
    }

    public boolean onClose(int id, Window window) {
        Utilities.logWindow(this, "close");
        AnswerSheetComponent AnswerSheetView = (AnswerSheetComponent) this.mMapDrawView.get(Integer.valueOf(id));
        if (!(AnswerSheetView == null || AnswerSheetView.isCancelled())) {
            QuestionWidgetsUtilities.updateWidgetData(Integer.valueOf(id), AnswerSheetView.getData());
        }
        this.mMapDrawView.remove(Integer.valueOf(id));
        return super.onClose(id, window);
    }

    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> cls, int fromId) {
        AnswerSheetComponent rootView = (AnswerSheetComponent) this.mMapDrawView.get(Integer.valueOf(id));
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
        return super.onCloseAll();
    }
}
