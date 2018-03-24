package com.foxit.uiextensions.modules.signature;

import android.graphics.Bitmap;
import android.graphics.Rect;

class SignatureInkItem {
    Bitmap bitmap;
    int color;
    float diameter;
    String dsgPath;
    boolean isOpened;
    String key;
    Rect rect;
    boolean selected;

    SignatureInkItem() {
    }
}
