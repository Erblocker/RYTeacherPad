package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.ui.UIDisplayer;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.utilities.disklrucache.DiskLruCache;
import com.netspace.library.utilities.disklrucache.DiskLruCache.Editor;
import com.netspace.library.utilities.disklrucache.DiskLruCache.Snapshot;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataSynchronizeEngineDataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "datasynchronizeengine";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DataSynchronizeEngineDataBase";
    private static SQLiteDatabase mDataBase;
    private static DiskLruCache mDiskCache;
    private Context mContext;

    public DataSynchronizeEngineDataBase(Context context) {
        super(context, "datasynchronizeengine.db", null, 1);
        this.mContext = context;
        if (mDataBase == null) {
            mDataBase = getWritableDatabase();
        }
        if (mDiskCache == null) {
            try {
                File cacheDir = MyiBaseApplication.getDiskCacheDir(this.mContext, "DataSynchronize");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                mDiskCache = DiskLruCache.open(cacheDir, 1, 1, 1073741824);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("" + "\t\tCREATE TABLE \"synchronize\" (")).append("\t\t\t\t  [packageid] CHAR(60),").toString())).append("\t\t\t\t  [clientid] CHAR(60),").toString())).append("\t\t\t\t  [packagedate] CHAR(60),").toString())).append("\t\t\t\t  [packagecontent] CHAR(255),").toString())).append("\t\t\t\t  [contenttype] INT DEFAULT 0,").toString())).append("\t\t\t\t  [packagedelete] INT DEFAULT 0,").toString())).append("\t\t\t\t  CONSTRAINT [] PRIMARY KEY ([packageid],[clientid]));").toString())).append("  CREATE INDEX [packagedate] ON \"synchronize\" (\"packagedate\");").toString())).append("  CREATE INDEX [packagedelete] ON \"synchronize\" (\"packagedelete\");").toString());
        db.execSQL(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("" + "\t\tCREATE TABLE \"synchronizepending\" (")).append("\t\t\t\t  [clientid] CHAR(60),").toString())).append("\t\t\t\t  [packageid] CHAR(60),").toString())).append("\t\t\t\t  CONSTRAINT [] PRIMARY KEY ([packageid],[clientid]));").toString());
        String szSQL = "";
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean addPackageToSynchronizeList(String szPackageID, String szClientID) {
        Cursor cursor = mDataBase.rawQuery("select * from synchronizepending where packageid=? and clientid=?;", new String[]{szPackageID, szClientID});
        boolean bResult = false;
        if (cursor.getCount() == 0) {
            mDataBase.execSQL("insert into synchronizepending (packageid,clientid) values (?,?);", new Object[]{szPackageID, szClientID});
            bResult = true;
        }
        cursor.close();
        return bResult;
    }

    public boolean getSynchronizePackage(ArrayList<String> arrPackageID, ArrayList<String> arrClientID) {
        Cursor cursor = mDataBase.rawQuery("select * from synchronizepending ;", null);
        boolean bResult = false;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                arrPackageID.add(cursor.getString(cursor.getColumnIndex("packageid")));
                arrClientID.add(cursor.getString(cursor.getColumnIndex(DeviceOperationRESTServiceProvider.CLIENTID)));
                cursor.moveToNext();
                bResult = true;
            }
        }
        cursor.close();
        return bResult;
    }

    public boolean hasPackage(String szPackageID, String szClientID) {
        Cursor cursor = mDataBase.rawQuery("select * from synchronize  where packageid=? and clientid=?;'", new String[]{szPackageID, szClientID});
        String szFileName = getLocalPackageFileName(szPackageID, szClientID);
        boolean bResult = false;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (cursor.getInt(cursor.getColumnIndex("contenttype")) != 0) {
                bResult = checkCache(szFileName);
            } else {
                bResult = true;
            }
        }
        cursor.close();
        return bResult;
    }

    public void deleteSynchronizePackage(String szPackageID, String szClientID) {
        mDataBase.execSQL("delete from synchronizepending where packageid=? and clientid=?;", new Object[]{szPackageID, szClientID});
    }

    public boolean addPackage(String szPackageID, String szClientID, String szValue, boolean bUseLruCache) {
        if (szValue == null) {
            return false;
        }
        int nType = 0;
        if (szValue.length() > 255) {
            nType = 1;
        }
        return addPackage(szPackageID, szClientID, szValue, nType, bUseLruCache);
    }

    public boolean addPackage(String szPackageID, String szClientID, String szData, int nContentType, boolean bUseLruCache) {
        boolean bResult;
        Cursor cursor = mDataBase.rawQuery("select * from synchronize  where packageid=? and clientid=?;", new String[]{szPackageID, szClientID});
        String szFileName = getLocalPackageFileName(szPackageID, szClientID);
        String szPackageDate = "";
        if (nContentType != 0) {
            if (!writeDataToCache(szData, szFileName, bUseLruCache)) {
                return false;
            }
            szData = szFileName;
        }
        Object[] args;
        if (cursor.getCount() == 0) {
            args = new Object[]{szPackageID, szClientID, szPackageDate, Integer.valueOf(nContentType), szData};
            mDataBase.execSQL("insert into synchronize (packageid,clientid,packagedate,contenttype,packagecontent) values (?,?,?,?,?);", args);
            bResult = true;
        } else {
            args = new Object[]{szPackageDate, Integer.valueOf(nContentType), szData, szPackageID, szClientID};
            mDataBase.execSQL("update synchronize set packagedate=?,contenttype=?,packagecontent=? where packageid=? and clientid=?;", args);
            bResult = true;
        }
        cursor.close();
        if (mDiskCache != null) {
            try {
                mDiskCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bResult;
    }

    public boolean deletePackage(String szPackageID, String szClientID) {
        Cursor cursor = mDataBase.rawQuery("select * from synchronize  where packageid=? and clientid=?;", new String[]{szPackageID, szClientID});
        boolean bResult = false;
        if (cursor.getCount() > 0) {
            Object[] args = new Object[]{Integer.valueOf(1), szPackageID, szClientID};
            mDataBase.execSQL("update synchronize set packagedelete=? where packageid=? and clientid=?;", args);
            bResult = true;
        }
        cursor.close();
        return bResult;
    }

    public String getPackageContent(String szPackageID, String szClientID) {
        Cursor cursor = mDataBase.rawQuery("select * from synchronize  where packageid=? and clientid=?;", new String[]{szPackageID, szClientID});
        String szResult = "";
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int nContentType = cursor.getInt(cursor.getColumnIndex("contenttype"));
            int nPackageDelete = cursor.getInt(cursor.getColumnIndex("packagedelete"));
            String szData = cursor.getString(cursor.getColumnIndex("packagecontent"));
            if (nPackageDelete != 0) {
                szResult = null;
            } else if (nContentType == 0) {
                szResult = szData;
            } else {
                szResult = loadDataFromCache(szData);
            }
        }
        cursor.close();
        return szResult;
    }

    public String generateSynchronizeXML(String szRequiredPackageID, String szClientID) {
        String szSQL = "select * from synchronize where clientid='" + szClientID + "' order by packagedate desc;";
        if (szRequiredPackageID != null) {
            szSQL = "select * from synchronize where packageid='" + szRequiredPackageID + "' and clientid='" + szClientID + "'";
        }
        Cursor cursor = mDataBase.rawQuery(szSQL, null);
        String szXML = "<wmStudy><DataSynchronize version=\"1.1\"/></wmStudy>";
        DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
        try {
            Document RootDocument = domfac.newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8)));
            Element RootElement = (Element) RootDocument.getDocumentElement().getFirstChild();
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Element NewElement = RootDocument.createElement("Item");
                    String szPackageID = cursor.getString(cursor.getColumnIndex("packageid"));
                    String szClientID2 = cursor.getString(cursor.getColumnIndex(DeviceOperationRESTServiceProvider.CLIENTID));
                    String szPackageDate = cursor.getString(cursor.getColumnIndex("packagedate"));
                    int nContentType = cursor.getInt(cursor.getColumnIndex("contenttype"));
                    int nPackageDelete = cursor.getInt(cursor.getColumnIndex("packagedelete"));
                    String szPackageType = String.valueOf(nContentType);
                    NewElement.setAttribute("packageId", szPackageID);
                    NewElement.setAttribute("clientId", szClientID2);
                    if (szPackageDate == null) {
                        szPackageDate = "";
                    }
                    NewElement.setAttribute("modifyDate", szPackageDate);
                    NewElement.setAttribute("type", szPackageType);
                    if (nPackageDelete != 0) {
                        NewElement.setAttribute("delete", "true");
                    }
                    if (szRequiredPackageID != null && nPackageDelete == 0) {
                        String szData = cursor.getString(cursor.getColumnIndex("packagecontent"));
                        if (nContentType == 0) {
                            NewElement.setTextContent(szData);
                        } else {
                            NewElement.setTextContent(loadDataFromCache(szData));
                        }
                    }
                    RootElement.appendChild(NewElement);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return Utilities.XMLToString(RootDocument);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return null;
        } catch (SAXException e3) {
            e3.printStackTrace();
            return null;
        } catch (IOException e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public String getLocalPackageFileName(String szPackageID, String szClientID) {
        String szFileName = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(this.mContext.getExternalCacheDir().getAbsolutePath())).append("/").toString())).append(szClientID).toString();
        File DataDir = new File(szFileName);
        if (!(DataDir.exists() || DataDir.mkdir())) {
            Log.e(TAG, "Can not create directory " + szFileName);
        }
        return new StringBuilder(String.valueOf(szFileName)).append("/").append(szPackageID).append(".bin").toString();
    }

    public int handleSynchronizeXML(String szXML, boolean bOnlyUpdateDate, UIDisplayer ProgressDisplayer) {
        String szSQL = "select packageid,clientid,packagedate from synchronize order by packagedate desc;";
        DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
        int nResult = 0;
        try {
            Element RootElement = (Element) domfac.newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement().getFirstChild();
            ProgressDisplayer.setProgressMax(Integer.valueOf(RootElement.getAttribute("totalCount")).intValue());
            if (RootElement.getChildNodes().getLength() > 0) {
                nResult = RootElement.getChildNodes().getLength();
                NodeList NodeList = RootElement.getChildNodes();
                for (int i = 0; i < NodeList.getLength(); i++) {
                    Element OneElement = (Element) NodeList.item(i);
                    String szPackageID = OneElement.getAttribute("packageId");
                    String szClientID = OneElement.getAttribute("clientId");
                    String szPackageDate = OneElement.getAttribute("modifyDate");
                    String isDelete = OneElement.getAttribute("delete");
                    String reupload = OneElement.getAttribute("reupload");
                    int nContentType = Integer.valueOf(OneElement.getAttribute("type")).intValue();
                    ProgressDisplayer.increaseProgress();
                    if (reupload != null && reupload.equalsIgnoreCase("true")) {
                        addPackageToSynchronizeList(szPackageID, szClientID);
                    } else if (isDelete == null || !isDelete.equalsIgnoreCase("true")) {
                        Cursor cursor = mDataBase.rawQuery("select * from synchronize where packageid='" + szPackageID + "' and clientid='" + szClientID + "';", null);
                        if (bOnlyUpdateDate) {
                            mDataBase.execSQL("update synchronize set packagedate=? where packageid=? and clientid=?;", new Object[]{szPackageDate, szPackageID, szClientID});
                        } else {
                            String szData = OneElement.getTextContent();
                            if (nContentType != 0) {
                                String szFileName = getLocalPackageFileName(szPackageID, szClientID);
                                writeDataToCache(szData, szFileName, true);
                                szData = szFileName;
                            }
                            if (cursor.getCount() == 0) {
                                mDataBase.execSQL("insert into synchronize (packageid,clientid,packagedate,contenttype,packagecontent) values (?,?,?,?,?);", new Object[]{szPackageID, szClientID, szPackageDate, Integer.valueOf(szContentType), szData});
                            } else {
                                mDataBase.execSQL("update synchronize set packagedate=?,contenttype=?,packagecontent=? where packageid=? and clientid=?;", new Object[]{szPackageDate, Integer.valueOf(szContentType), szData, szPackageID, szClientID});
                            }
                        }
                        cursor.close();
                    } else {
                        mDataBase.execSQL("delete from synchronize where packageid=? and clientid=?;", new String[]{szPackageID, szClientID});
                        if (nContentType != 0) {
                            deleteFromCache(getLocalPackageFileName(szPackageID, szClientID));
                        }
                    }
                }
                if (mDiskCache != null) {
                    try {
                        mDiskCache.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return nResult;
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
            return -1;
        } catch (UnsupportedEncodingException e3) {
            e3.printStackTrace();
            return -1;
        } catch (SAXException e4) {
            e4.printStackTrace();
            return -1;
        } catch (IOException e5) {
            e5.printStackTrace();
            return -1;
        }
    }

    private boolean writeDataToCache(String szData, String szFileName, boolean bUseLruCache) {
        boolean bResult = false;
        if (!bUseLruCache) {
            Log.d(TAG, "Use local file directly. Skip lru cache.");
        }
        if (mDiskCache != null && bUseLruCache) {
            synchronized (mDiskCache) {
                try {
                    Editor editor = mDiskCache.edit(Utilities.md5(szFileName));
                    if (editor != null) {
                        if (Utilities.writeTextToStream(editor.newOutputStream(0), szData)) {
                            editor.commit();
                            bResult = true;
                        } else {
                            editor.abort();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bResult) {
            return bResult;
        }
        return Utilities.writeTextToFile(szFileName, szData);
    }

    private String loadDataFromCache(String szFileName) {
        String szData = null;
        if (mDiskCache != null) {
            synchronized (mDiskCache) {
                try {
                    Snapshot snapshot = mDiskCache.get(Utilities.md5(szFileName));
                    if (snapshot != null) {
                        szData = Utilities.getTextFromStream(snapshot.getInputStream(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (szData == null) {
            return Utilities.readTextFile(szFileName);
        }
        return szData;
    }

    private boolean checkCache(String szFileName) {
        if (mDiskCache != null) {
            try {
                if (mDiskCache.get(Utilities.md5(szFileName)) != null) {
                    return true;
                }
                return false;
            } catch (IOException e) {
                return false;
            }
        } else if (new File(szFileName).exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void deleteFromCache(String szFileName) {
        boolean bResult = false;
        if (mDiskCache != null) {
            try {
                bResult = mDiskCache.remove(Utilities.md5(szFileName));
            } catch (IOException e) {
            }
        }
        if (!bResult) {
            File LocalFile = new File(szFileName);
            if (LocalFile.exists()) {
                LocalFile.delete();
            }
        }
    }
}
