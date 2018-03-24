package com.netspace.library.virtualnetworkobject;

import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VirtualNetworkObjectManager {
    private static final String TAG = "VirtualNetworkObjectManager";
    private ArrayList<WeakReference<ItemObject>> marrObjects = new ArrayList();

    public void add(ItemObject itemObject) {
        this.marrObjects.add(new WeakReference(itemObject));
    }

    public void cancelAll() {
        for (int i = 0; i < this.marrObjects.size(); i++) {
            ItemObject ItemObject = (ItemObject) ((WeakReference) this.marrObjects.get(i)).get();
            if (ItemObject != null) {
                ItemObject.setCancelled();
                Log.d(TAG, "Cancelled ItemObject " + ItemObject.toString());
            }
        }
        this.marrObjects.clear();
    }
}
