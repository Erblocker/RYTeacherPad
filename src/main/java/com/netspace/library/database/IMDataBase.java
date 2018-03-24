package com.netspace.library.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import com.netspace.library.adapter.IMMessageListAdapter;
import com.netspace.library.utilities.Utilities;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.http.protocol.HTTP;

public class IMDataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "im_1_";
    private static final int DATABASE_VERSION = 1;
    public static final int GETITEMRESULT_TYPE_ANSWERRESULT = 3;
    public static final int GETITEMRESULT_TYPE_FAVORITE = 2;
    public static final int GETITEMRESULT_TYPE_VOTE = 1;
    private static final String TAG = null;
    private static DataChangeInterface m_DataChangeInterface;
    private boolean bValid;
    private SQLiteDatabase m_DataBase = getWritableDatabase();
    private Handler m_Handler = new Handler();

    public IMDataBase(Context context, String szUserName) {
        super(context, new StringBuilder(DATABASE_NAME).append(szUserName).append(".db").toString(), null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("" + "\t\tCREATE TABLE `immessage` (")).append("\t\t\t\t  `GUID` CHAR(60) NOT NULL ,").toString())).append("\t\t\t\t  `SourceJID` CHAR(60) NOT NULL ,").toString())).append("\t\t\t\t  `TargetJID` CHAR(60) NOT NULL ,").toString())).append("  \t\t\t\t  `MessageText` CHAR(1048576) ,").toString())).append("\t\t\t\t  `MessageDate` CHAR(60) ,").toString())).append("\t\t\t\t  CONSTRAINT [] PRIMARY KEY (`GUID`));").toString())).append("  CREATE INDEX [SourceJID] ON  `immessage` (`SourceJID`);").toString())).append("  CREATE INDEX [TargetJID] ON `immessage` (`TargetJID`);").toString())).append("  CREATE INDEX [MessageDate] ON `immessage` (`MessageDate`);").toString());
        String szSQL = "";
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean addIMMessage(String szGUID, String szFromJID, String szToJID, String szMessage, String szMessageDate) {
        Cursor cursor = this.m_DataBase.rawQuery("select * from immessage where guid=?", new String[]{szGUID});
        if (szMessageDate == null || szMessageDate.isEmpty()) {
            szMessageDate = Utilities.getNowMillsecond();
        }
        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        Object[] args = new Object[]{szGUID, szFromJID, szToJID, szMessage, szMessageDate};
        this.m_DataBase.execSQL("insert into immessage (guid,SourceJID,TargetJID,MessageText,MessageDate) values (?,?,?,?,?);", args);
        return true;
    }

    public boolean getIMMessages(String szJID, int nStart, int nLimit, IMMessageListAdapter Adapter) {
        boolean bResult = false;
        Cursor cursor = this.m_DataBase.rawQuery("select * from immessage where SourceJID=? or TargetJID=? order by MessageDate desc", new String[]{szJID, szJID});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (nStart > 0) {
                    nStart--;
                } else {
                    String szName = cursor.getString(cursor.getColumnIndex("SourceJID"));
                    String szDate = cursor.getString(cursor.getColumnIndex("MessageDate"));
                    String szMessage = cursor.getString(cursor.getColumnIndex("MessageText"));
                    if (szDate.indexOf(".") != -1) {
                        szDate = szDate.substring(0, szDate.indexOf("."));
                    }
                    Adapter.addToTop(szName, szMessage, szDate);
                    nLimit--;
                    bResult = true;
                    if (nLimit == 0) {
                        break;
                    }
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return bResult;
    }

    public boolean searchIMMessage(String szJID, String szKeyword, IMMessageListAdapter Adapter) {
        String szFixedKeywords = "";
        String szUrlEncoded = "";
        try {
            szUrlEncoded = URLEncoder.encode(szKeyword, "utf-8").replace("%", "%%");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        for (int i = 0; i < szKeyword.length(); i++) {
            char szChar = szKeyword.charAt(i);
            int nData = szKeyword.codePointAt(i);
            if (nData < 255) {
                szFixedKeywords = new StringBuilder(String.valueOf(szFixedKeywords)).append(szChar).toString();
            } else {
                szFixedKeywords = new StringBuilder(String.valueOf(szFixedKeywords)).append("&#").append(String.valueOf(nData)).append(";").toString();
            }
        }
        String szSQL = "select * from immessage where (SourceJID=? or TargetJID=?) and (MessageText like '%" + szFixedKeywords + "%'  or MessageText like '%" + szUrlEncoded + "%' or MessageText like '%" + szKeyword + "%')  order by MessageDate desc";
        Cursor cursor = this.m_DataBase.rawQuery(szSQL, new String[]{szJID, szJID});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String szGUID = cursor.getString(cursor.getColumnIndex("GUID"));
                String szName = cursor.getString(cursor.getColumnIndex("SourceJID"));
                String szDate = cursor.getString(cursor.getColumnIndex("MessageDate"));
                String szMessage = cursor.getString(cursor.getColumnIndex("MessageText"));
                int nPos = 0;
                boolean bValid = false;
                String szContent = szMessage;
                if (szMessage.startsWith("SERVERTIME=")) {
                    nPos = szMessage.indexOf(";") + 1;
                }
                if (szMessage.contains("PICTURE=") || szMessage.contains("VOICE=")) {
                    cursor.moveToNext();
                } else {
                    if (szMessage.indexOf("INTENT=") != -1) {
                        int nTitlePos = szMessage.indexOf("title=");
                        int nTitleEndPos = szMessage.indexOf(";", nTitlePos);
                        if (!(nTitlePos == -1 || nTitleEndPos == -1)) {
                            try {
                                if (URLDecoder.decode(szMessage.substring(nTitlePos + 6, nTitleEndPos), HTTP.UTF_8).indexOf(szKeyword) != -1) {
                                    bValid = true;
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        int nPos2 = szMessage.lastIndexOf("fields=");
                        if (nPos2 != -1) {
                            szContent = szMessage.substring(nPos, nPos2);
                        }
                        if (szContent.indexOf(szFixedKeywords) != -1) {
                            bValid = true;
                        }
                    }
                    if (!bValid) {
                        int nNamePos = szMessage.lastIndexOf("realname=");
                        if (nNamePos != -1) {
                            if (szMessage.substring(nNamePos + 9, szMessage.indexOf(";", nNamePos)).indexOf(szKeyword) != -1) {
                                bValid = true;
                            }
                        }
                    }
                    if (bValid) {
                        if (szDate.indexOf(".") != -1) {
                            szDate = szDate.substring(0, szDate.indexOf("."));
                        }
                        Adapter.addSearchResult(szGUID, szName, szMessage, szDate);
                    }
                    cursor.moveToNext();
                }
            }
        }
        cursor.close();
        return false;
    }

    public boolean getIMRelatedMessage(String szJID, String szMessageGUID, IMMessageListAdapter Adapter) {
        Cursor cursor = this.m_DataBase.rawQuery("select * from immessage where (SourceJID=? or TargetJID=?) order by MessageDate desc", new String[]{szJID, szJID});
        boolean bFoundMatch = false;
        int nStopCount = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (cursor.getString(cursor.getColumnIndex("GUID")).equalsIgnoreCase(szMessageGUID)) {
                    bFoundMatch = true;
                    nStopCount = Adapter.getCount() + 3;
                }
                String szName = cursor.getString(cursor.getColumnIndex("SourceJID"));
                String szDate = cursor.getString(cursor.getColumnIndex("MessageDate"));
                String szMessage = cursor.getString(cursor.getColumnIndex("MessageText"));
                if (szDate.indexOf(".") != -1) {
                    szDate = szDate.substring(0, szDate.indexOf("."));
                }
                if (bFoundMatch) {
                    Adapter.addToTop(szName, szMessage, szDate);
                    if (Adapter.getCount() >= nStopCount) {
                        break;
                    }
                } else {
                    Adapter.addToTop(szName, szMessage, szDate);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return false;
    }
}
