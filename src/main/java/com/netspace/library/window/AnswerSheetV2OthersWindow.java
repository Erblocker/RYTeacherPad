package com.netspace.library.window;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.netspace.library.components.AnswerSheetV2OthersComponent;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.library.utilities.QuestionWidgetsUtilities;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.wrapper.CameraCaptureActivity;
import java.util.HashMap;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.HttpStatus;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class AnswerSheetV2OthersWindow extends StandOutWindow implements ComponentCallBack {
    private HashMap<Integer, String> mMapCameraURI = new HashMap();
    private HashMap<Integer, AnswerSheetV2OthersComponent> mMapRootView = new HashMap();

    public String getAppName() {
        return "答题窗口";
    }

    public int getAppIcon() {
        return 17301560;
    }

    public void createAndAttachView(int id, FrameLayout frame) {
        Utilities.logWindow(this, "create");
        AnswerSheetV2OthersComponent CameraComponent = new AnswerSheetV2OthersComponent(this);
        CameraComponent.setIndex(id);
        CameraComponent.setTag(Integer.valueOf(id));
        CameraComponent.setCallBack(this);
        frame.addView(CameraComponent);
        CameraComponent.updateDisplay();
        this.mMapRootView.put(Integer.valueOf(id), CameraComponent);
    }

    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(this, id, 500, HttpStatus.SC_BAD_REQUEST, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public int getFlags(int id) {
        return ((((super.getFlags(id) | StandOutFlags.FLAG_DECORATION_SYSTEM) | StandOutFlags.FLAG_BODY_MOVE_ENABLE) | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE) | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE) | StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;
    }

    public String getPersistentNotificationMessage(int id) {
        return "点击这里关闭答题窗口";
    }

    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, AnswerSheetV2OthersWindow.class, id);
    }

    public boolean onHide(int id, Window window) {
        Log.d("DrawWindow", "onHide " + id);
        return super.onHide(id, window);
    }

    public boolean onClose(int id, Window window) {
        Log.d("DrawWindow", "onClose " + id);
        Utilities.logWindow(this, "close");
        String szData = (String) this.mMapCameraURI.get(Integer.valueOf(id));
        if (szData != null) {
            QuestionWidgetsUtilities.updateWidgetData(Integer.valueOf(id), szData);
        }
        this.mMapRootView.remove(Integer.valueOf(id));
        this.mMapCameraURI.remove(Integer.valueOf(id));
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
        AnswerSheetV2OthersComponent AnswerSheetV2OthersComponent = (AnswerSheetV2OthersComponent) this.mMapRootView.get(Integer.valueOf(id));
        if (AnswerSheetV2OthersComponent != null) {
            String szData = data.getString("data");
            if (szData != null) {
                AnswerSheetV2OthersComponent.setData(szData);
                return;
            }
            String changedText = data.getString("uri");
            if (changedText != null) {
                Intent intent = new Intent();
                intent.setData(Uri.parse(changedText));
                AnswerSheetV2OthersComponent.intentComplete(intent);
            }
            if (data.getBoolean("lock")) {
                AnswerSheetV2OthersComponent.setLocked(true);
            }
        }
    }

    public void OnDataLoaded(String szFileName, IComponents Component) {
    }

    public void OnDataUploaded(String szData, IComponents Component) {
        if (Component instanceof View) {
            this.mMapCameraURI.put(Integer.valueOf(((Integer) ((View) Component).getTag()).intValue()), szData);
        }
    }

    public void OnRequestIntent(Intent intent, IComponents Component) {
        if (Component instanceof View) {
            int id = ((Integer) ((View) Component).getTag()).intValue();
            Intent intent2 = new Intent(getApplicationContext(), CameraCaptureActivity.class);
            intent2.putExtra("id", id);
            intent2.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
            getApplicationContext().startActivity(intent2);
        }
    }
}
