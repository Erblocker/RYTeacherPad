package com.foxit.uiextensions.annots.stamp;

import android.graphics.Bitmap;
import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class StampUndoItem extends AnnotUndoItem {
    Bitmap mBitmap;
    DynamicStampIconProvider mDsip;
    String mIconName;
    int mStampType;
}
