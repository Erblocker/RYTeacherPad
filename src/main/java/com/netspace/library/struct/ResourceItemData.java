package com.netspace.library.struct;

import android.graphics.Bitmap;
import java.util.ArrayList;

public class ResourceItemData {
    public ArrayList<String> arrThumbnailUrls;
    public boolean bAnswered = false;
    public boolean bFav = false;
    public boolean bFolder = false;
    public boolean bLoaded = false;
    public boolean bLocked = false;
    public boolean bRead = false;
    public boolean bReadOnly;
    public boolean bThumbDown = false;
    public boolean bThumbUp = false;
    public Bitmap bmThumbnail;
    public int nCorrectResult = 0;
    public int nTipNumber = 0;
    public int nType = 0;
    public int nUsageType = 0;
    public String szAuthor = "";
    public String szDateTime = "";
    public String szFileType = "";
    public String szGUID = "";
    public String szResourceGUID = "";
    public String szResourceType = "";
    public String szScheduleGUID = "";
    public String szScheduleResourceGUID = "";
    public String szTitle = "";
}
