package com.netspace.library.database;

import com.netspace.library.interfaces.RESTBasicData;
import java.util.ArrayList;

public class RESTSynchronizePackage {
    public ArrayList<RESTBasicData> download = new ArrayList();
    public ArrayList<RESTBasicData> modified = new ArrayList();
    public ArrayList<RESTBasicData> upload = new ArrayList();
}
