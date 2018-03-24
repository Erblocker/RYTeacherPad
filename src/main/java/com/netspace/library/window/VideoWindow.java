package com.netspace.library.window;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.netspace.library.components.VideoComponent;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.wrapper.CameraRecordActivity;
import io.vov.vitamio.ThumbnailUtils;
import java.util.HashMap;
import net.sqlcipher.database.SQLiteDatabase;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class VideoWindow extends StandOutWindow implements ComponentCallBack {
    private HashMap<Integer, String> mMapVideoURI = new HashMap();
    private HashMap<Integer, VideoComponent> mMapView = new HashMap();

    public String getAppName() {
        return "视频窗";
    }

    public int getAppIcon() {
        return 17301560;
    }

    public void createAndAttachView(int id, FrameLayout frame) {
        Utilities.logWindow(this, "create");
        VideoComponent VideoComponent = new VideoComponent(this);
        frame.addView(VideoComponent);
        VideoComponent.setTag(Integer.valueOf(id));
        VideoComponent.setCallBack(this);
        this.mMapView.put(Integer.valueOf(id), VideoComponent);
    }

    public void onResize(int id, Window window, View view, MotionEvent event) {
        super.onResize(id, window, view, event);
    }

    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> cls, int fromId) {
        VideoComponent VideoComponent = (VideoComponent) this.mMapView.get(Integer.valueOf(id));
        String szData = data.getString("data");
        if (szData != null) {
            VideoComponent.setData(szData);
            return;
        }
        String changedText = data.getString("uri");
        if (changedText != null) {
            Intent intent = new Intent();
            intent.setData(Uri.parse(changedText));
            VideoComponent.intentComplete(intent);
        }
        if (data.getBoolean("lock")) {
            VideoComponent.setLocked(true);
        }
    }

    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(this, id, Utilities.dpToPixel((int) ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, getApplicationContext()), Utilities.dpToPixel(240, getApplicationContext()), 0, StandOutLayoutParams.AUTO_POSITION);
    }

    public int getFlags(int id) {
        return ((((super.getFlags(id) | StandOutFlags.FLAG_DECORATION_SYSTEM) | StandOutFlags.FLAG_BODY_MOVE_ENABLE) | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE) | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE) | StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;
    }

    public String getPersistentNotificationMessage(int id) {
        return "点击这里关闭" + getAppName();
    }

    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, VideoWindow.class, id);
    }

    public boolean onHide(int id, Window window) {
        Log.d("DrawWindow", "onHide " + id);
        return super.onHide(id, window);
    }

    public boolean onClose(int id, Window window) {
        Log.d("DrawWindow", "onClose " + id);
        Utilities.logWindow(this, "close");
        String szData = (String) this.mMapVideoURI.get(Integer.valueOf(id));
        if (szData != null) {
            QuestionWidgetsUtilities.updateWidgetData(Integer.valueOf(id), szData);
        }
        this.mMapView.remove(Integer.valueOf(id));
        this.mMapVideoURI.remove(Integer.valueOf(id));
        return super.onClose(id, window);
    }

    public boolean onCloseAll() {
        return super.onCloseAll();
    }

    public int getThemeStyle() {
        return 16973934;
    }

    public void OnDataLoaded(String szFileName, IComponents Component) {
    }

    public void OnDataUploaded(String szData, IComponents Component) {
        if (Component instanceof View) {
            this.mMapVideoURI.put(Integer.valueOf(((Integer) ((View) Component).getTag()).intValue()), szData);
        }
    }

    public void OnRequestIntent(Intent intent, IComponents Component) {
        if (Component instanceof View) {
            int id = ((Integer) ((View) Component).getTag()).intValue();
            Intent intent2 = new Intent(getApplicationContext(), CameraRecordActivity.class);
            intent2.putExtra("id", id);
            intent2.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
            getApplicationContext().startActivity(intent2);
        }
    }
}
