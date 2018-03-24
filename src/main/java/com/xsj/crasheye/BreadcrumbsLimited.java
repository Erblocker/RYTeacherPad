package com.xsj.crasheye;

import com.xsj.crasheye.log.Logger;
import com.xsj.crasheye.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;

class BreadcrumbsLimited extends ArrayList<String> {
    protected static final int MAX_BREADCRUMBS = 16;
    private static final long serialVersionUID = -7130367487360671781L;

    BreadcrumbsLimited() {
    }

    public boolean addToList(String breadcrumb) {
        if (breadcrumb == null) {
            breadcrumb = "null";
        }
        Logger.logInfo("Breadcrumb: " + breadcrumb + " was added to the breadcrumb list");
        add(new StringBuilder(String.valueOf(breadcrumb)).append(":").append(Utils.getTime()).toString());
        if (size() > 16) {
            Logger.logInfo("Breadcrumbs list is bigger than " + String.valueOf(16) + " items, removing the oldest one.");
            remove(0);
        }
        return true;
    }

    public JSONArray getList() {
        JSONArray breadcrumbs = new JSONArray();
        Iterator it = iterator();
        while (it.hasNext()) {
            breadcrumbs.put((String) it.next());
        }
        return breadcrumbs;
    }
}
