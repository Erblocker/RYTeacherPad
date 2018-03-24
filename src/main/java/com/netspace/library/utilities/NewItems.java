package com.netspace.library.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.HashSet;

public class NewItems {
    private Context mContext;
    private String mName = "newItems";
    private SharedPreferences mSettings;
    private ArrayList<String> marrNewItemDisplayedText = new ArrayList();
    private ArrayList<String> marrNewItemGUIDs = new ArrayList();

    public NewItems(Context Context, String szName) {
        this.mContext = Context;
        this.mSettings = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        this.mName = szName;
        this.marrNewItemGUIDs = new ArrayList(this.mSettings.getStringSet(this.mName, new HashSet()));
    }

    public void addNewItem(String szGUID) {
        this.marrNewItemGUIDs.add(szGUID);
        save();
    }

    public boolean hasItemNew() {
        if (this.marrNewItemGUIDs.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isItemNew(String szGUID) {
        for (int i = 0; i < this.marrNewItemGUIDs.size(); i++) {
            if (((String) this.marrNewItemGUIDs.get(i)).indexOf(szGUID) != -1) {
                return true;
            }
        }
        return false;
    }

    public boolean removeItem(String szGUID) {
        boolean bResult = false;
        int i = 0;
        while (i < this.marrNewItemGUIDs.size()) {
            if (((String) this.marrNewItemGUIDs.get(i)).indexOf(szGUID) != -1) {
                bResult = true;
                this.marrNewItemGUIDs.remove(i);
                i--;
            }
            i++;
        }
        if (bResult) {
            this.marrNewItemDisplayedText.clear();
            save();
        }
        return bResult;
    }

    private void save() {
        Editor editor = this.mSettings.edit();
        editor.putStringSet(this.mName, new HashSet(this.marrNewItemGUIDs));
        editor.commit();
    }
}
