package net.sqlcipher.database;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

public class SQLiteContentHelper {
    public static AssetFileDescriptor getBlobColumnAsAssetFile(SQLiteDatabase db, String sql, String[] selectionArgs) throws FileNotFoundException {
        ParcelFileDescriptor fd = null;
        try {
            MemoryFile file = simpleQueryForBlobMemoryFile(db, sql, selectionArgs);
            if (file == null) {
                throw new FileNotFoundException("No results.");
            }
            try {
                Method m = file.getClass().getDeclaredMethod("getParcelFileDescriptor", new Class[0]);
                m.setAccessible(true);
                fd = (ParcelFileDescriptor) m.invoke(file, new Object[0]);
            } catch (Exception e) {
                Log.i("SQLiteContentHelper", "SQLiteCursor.java: " + e);
            }
            return new AssetFileDescriptor(fd, 0, (long) file.length());
        } catch (IOException ex) {
            throw new FileNotFoundException(ex.toString());
        }
    }

    private static MemoryFile simpleQueryForBlobMemoryFile(SQLiteDatabase db, String sql, String[] selectionArgs) throws IOException {
        MemoryFile memoryFile = null;
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    byte[] bytes = cursor.getBlob(0);
                    if (bytes == null) {
                        cursor.close();
                    } else {
                        memoryFile = new MemoryFile(null, bytes.length);
                        memoryFile.writeBytes(bytes, 0, 0, bytes.length);
                        cursor.close();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return memoryFile;
    }
}
