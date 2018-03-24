package net.sourceforge.opencamera;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

@TargetApi(21)
public class CameraControllerManager2 extends CameraControllerManager {
    private Context context = null;

    public CameraControllerManager2(Context context) {
        this.context = context;
    }

    public int getNumberOfCameras() {
        try {
            return ((CameraManager) this.context.getSystemService("camera")).getCameraIdList().length;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    boolean isFrontFacing(int cameraId) {
        CameraManager manager = (CameraManager) this.context.getSystemService("camera");
        try {
            if (((Integer) manager.getCameraCharacteristics(manager.getCameraIdList()[cameraId]).get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                return true;
            }
            return false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
}
