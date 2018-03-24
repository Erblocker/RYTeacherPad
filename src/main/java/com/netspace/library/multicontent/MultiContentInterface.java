package com.netspace.library.multicontent;

import android.view.View;
import com.netspace.library.controls.CustomCameraView;

public interface MultiContentInterface {
    void DeleteComponent(View view);

    void MoveComponentDown(View view);

    void MoveComponentUp(View view);

    void SetDisableScrollView(boolean z);

    void StartTakePicture(CustomCameraView customCameraView);

    void setChanged(boolean z);
}
