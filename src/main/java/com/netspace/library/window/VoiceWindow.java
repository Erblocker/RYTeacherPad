package com.netspace.library.window;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.netspace.library.components.AudioComponent;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import java.util.HashMap;
import org.apache.http.HttpStatus;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class VoiceWindow extends StandOutWindow implements ComponentCallBack {
    private HashMap<Integer, String> mMapURI = new HashMap();
    private HashMap<Integer, AudioComponent> mMapView = new HashMap();

    public String getAppName() {
        return "语音窗";
    }

    public int getAppIcon() {
        return 17301560;
    }

    public void createAndAttachView(int id, FrameLayout frame) {
        Utilities.logWindow(this, "create");
        AudioComponent AudioComponent = new AudioComponent(this);
        frame.addView(AudioComponent);
        AudioComponent.setTag(Integer.valueOf(id));
        AudioComponent.setCallBack(this);
        this.mMapView.put(Integer.valueOf(id), AudioComponent);
    }

    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(this, id, 500, HttpStatus.SC_BAD_REQUEST, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public int getFlags(int id) {
        return ((super.getFlags(id) | StandOutFlags.FLAG_DECORATION_SYSTEM) | StandOutFlags.FLAG_BODY_MOVE_ENABLE) | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE;
    }

    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> cls, int fromId) {
        AudioComponent AudioComponent = (AudioComponent) this.mMapView.get(Integer.valueOf(id));
        String szData = data.getString("data");
        this.mMapURI.put(Integer.valueOf(id), szData);
        if (!(szData == null || szData.isEmpty())) {
            AudioComponent.setData(szData);
        }
        if (data.getBoolean("lock")) {
            AudioComponent.setLocked(true);
        }
    }

    public String getPersistentNotificationMessage(int id) {
        return "点击这里关闭语音窗";
    }

    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, VoiceWindow.class, id);
    }

    public boolean onHide(int id, Window window) {
        Log.d("DrawWindow", "onHide " + id);
        return super.onHide(id, window);
    }

    public boolean onClose(int id, Window window) {
        Utilities.logWindow(this, "close");
        String szData = (String) this.mMapURI.get(Integer.valueOf(id));
        if (szData != null) {
            QuestionWidgetsUtilities.updateWidgetData(Integer.valueOf(id), szData);
        }
        this.mMapURI.remove(Integer.valueOf(id));
        this.mMapView.remove(Integer.valueOf(id));
        return super.onClose(id, window);
    }

    public boolean onCloseAll() {
        Log.d("DrawWindow", "onCloseAll");
        return super.onCloseAll();
    }

    public int getThemeStyle() {
        return 16973934;
    }

    public void OnDataLoaded(String szFileName, IComponents Component) {
    }

    public void OnDataUploaded(String szData, IComponents Component) {
        if (Component instanceof View) {
            this.mMapURI.put(Integer.valueOf(((Integer) ((View) Component).getTag()).intValue()), szData);
        }
    }

    public void OnRequestIntent(Intent intent, IComponents Component) {
    }
}
