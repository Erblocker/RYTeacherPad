package com.netspace.library.struct;

import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.fragment.RESTLibraryFragment;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecentUser {
    private ArrayList<RecentUserItem> marrRecentUser = new ArrayList();

    public void init() {
        String szRecentUser = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext()).getString("recentuser", "");
        if (!szRecentUser.isEmpty()) {
            try {
                JSONArray jsonObject = new JSONArray(szRecentUser);
                for (int i = 0; i < jsonObject.length(); i++) {
                    JSONObject oneUser = (JSONObject) jsonObject.get(i);
                    RecentUserItem oneUserItem = new RecentUserItem();
                    oneUserItem.szUID = oneUser.getString("uid");
                    oneUserItem.szName = oneUser.getString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX);
                    oneUserItem.nType = oneUser.getInt("type");
                    this.marrRecentUser.add(oneUserItem);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        this.marrRecentUser.clear();
    }

    public ArrayList<RecentUserItem> getData() {
        return this.marrRecentUser;
    }

    public void addUser(String szUserName, String szUID, int nType) {
        RecentUserItem user = new RecentUserItem();
        user.nType = nType;
        user.szName = szUserName;
        user.szUID = szUID;
        addUser(user);
    }

    public void addUser(RecentUserItem user) {
        int i;
        for (i = 0; i < this.marrRecentUser.size(); i++) {
            if (((RecentUserItem) this.marrRecentUser.get(i)).szUID.equalsIgnoreCase(user.szUID)) {
                this.marrRecentUser.remove(i);
                break;
            }
        }
        this.marrRecentUser.add(0, user);
        while (this.marrRecentUser.size() > 20) {
            this.marrRecentUser.remove(this.marrRecentUser.size() - 1);
        }
        try {
            JSONArray jsonObject = new JSONArray();
            for (i = 0; i < this.marrRecentUser.size(); i++) {
                RecentUserItem oneUser = (RecentUserItem) this.marrRecentUser.get(i);
                JSONObject oneJson = new JSONObject();
                oneJson.put(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX, oneUser.szName);
                oneJson.put("uid", oneUser.szUID);
                oneJson.put("type", oneUser.nType);
                jsonObject.put(oneJson);
            }
            Editor editor = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext()).edit();
            editor.putString("recentuser", jsonObject.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
