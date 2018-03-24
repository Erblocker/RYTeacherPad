package com.netspace.library.struct;

import java.util.ArrayList;

public class TableChangeMessage {
    public ArrayList<String> arrGUIDs = new ArrayList();
    public String szTableName;

    public TableChangeMessage(String szTableName, String[] arrGUIDs) {
        this.szTableName = szTableName;
        for (Object add : arrGUIDs) {
            this.arrGUIDs.add(add);
        }
    }

    public TableChangeMessage(String szTableName, String szGUID) {
        this.szTableName = szTableName;
        this.arrGUIDs.add(szGUID);
    }
}
