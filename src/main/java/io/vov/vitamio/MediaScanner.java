package io.vov.vitamio;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import com.foxit.uiextensions.utils.AppSQLite;
import io.vov.vitamio.provider.MediaStore;
import io.vov.vitamio.provider.MediaStore.MediaColumns;
import io.vov.vitamio.provider.MediaStore.Video.Media;
import io.vov.vitamio.utils.ContextUtils;
import io.vov.vitamio.utils.FileUtils;
import io.vov.vitamio.utils.Log;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class MediaScanner {
    private static final int DATE_MODIFIED_VIDEO_COLUMN_INDEX = 2;
    private static final int ID_VIDEO_COLUMN_INDEX = 0;
    private static final int PATH_VIDEO_COLUMN_INDEX = 1;
    private static final String[] VIDEO_PROJECTION = new String[]{AppSQLite.KEY_ID, "_data", MediaColumns.DATE_MODIFIED};
    private boolean mCaseInsensitivePaths;
    private MyMediaScannerClient mClient = new MyMediaScannerClient();
    private Context mContext;
    private HashMap<String, FileCacheEntry> mFileCache;
    private ContentProviderClient mProvider;

    private static class FileCacheEntry {
        long mLastModified;
        boolean mLastModifiedChanged = false;
        String mPath;
        long mRowId;
        boolean mSeenInFileSystem = false;
        Uri mTableUri;

        FileCacheEntry(Uri tableUri, long rowId, String path, long lastModified) {
            this.mTableUri = tableUri;
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
        }

        public String toString() {
            return this.mPath;
        }
    }

    private class MyMediaScannerClient implements MediaScannerClient {
        private String mAlbum;
        private String mArtist;
        private long mDuration;
        private long mFileSize;
        private int mFileType;
        private int mHeight;
        private String mLanguage;
        private long mLastModified;
        private String mMimeType;
        private String mPath;
        private String mTitle;
        private int mWidth;

        private MyMediaScannerClient() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public FileCacheEntry beginFile(String path, long lastModified, long fileSize) {
            int lastSlash = path.lastIndexOf(47);
            if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
                if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                    return null;
                }
                if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                    if (!path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10)) {
                        if (!path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                            int length = (path.length() - lastSlash) - 1;
                            if (length == 17) {
                            }
                            if (length == 10) {
                            }
                        }
                    }
                    return null;
                }
            }
            MediaFileType mediaFileType = MediaFile.getFileType(path);
            if (mediaFileType != null) {
                this.mFileType = mediaFileType.fileType;
                this.mMimeType = mediaFileType.mimeType;
            }
            String key = FileUtils.getCanonical(new File(path));
            if (MediaScanner.this.mCaseInsensitivePaths) {
                key = path.toLowerCase();
            }
            FileCacheEntry entry = (FileCacheEntry) MediaScanner.this.mFileCache.get(key);
            if (entry == null) {
                entry = new FileCacheEntry(null, 0, path, 0);
                MediaScanner.this.mFileCache.put(key, entry);
            }
            entry.mSeenInFileSystem = true;
            long delta = lastModified - entry.mLastModified;
            if (delta > 1 || delta < -1) {
                entry.mLastModified = lastModified;
                entry.mLastModifiedChanged = true;
            }
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFileSize = fileSize;
            this.mTitle = null;
            this.mDuration = 0;
            return entry;
        }

        public void scanFile(String path, long lastModified, long fileSize) {
            Log.i("scanFile: %s", path);
            doScanFile(path, lastModified, fileSize, false);
        }

        public Uri doScanFile(String path, long lastModified, long fileSize, boolean scanAlways) {
            try {
                FileCacheEntry entry = beginFile(path, lastModified, fileSize);
                if (entry == null) {
                    return null;
                }
                if (!entry.mLastModifiedChanged && !scanAlways) {
                    return null;
                }
                if (MediaScanner.this.processFile(path, null)) {
                    return endFile(entry);
                }
                if (MediaScanner.this.mCaseInsensitivePaths) {
                    MediaScanner.this.mFileCache.remove(path.toLowerCase());
                    return null;
                }
                MediaScanner.this.mFileCache.remove(path);
                return null;
            } catch (Throwable e) {
                Log.e("RemoteException in MediaScanner.scanFile()", e);
                return null;
            }
        }

        private int parseSubstring(String s, int start, int defaultValue) {
            int length = s.length();
            if (start == length) {
                return defaultValue;
            }
            int start2 = start + 1;
            char ch = s.charAt(start);
            if (ch < '0' || ch > '9') {
                start = start2;
                return defaultValue;
            }
            int result = ch - 48;
            while (start2 < length) {
                start = start2 + 1;
                ch = s.charAt(start2);
                if (ch < '0' || ch > '9') {
                    return result;
                }
                result = (result * 10) + (ch - 48);
                start2 = start;
            }
            start = start2;
            return result;
        }

        public void handleStringTag(String name, byte[] valueBytes, String valueEncoding) {
            String value;
            try {
                value = new String(valueBytes, valueEncoding);
            } catch (Throwable e) {
                Log.e("handleStringTag", e);
                value = new String(valueBytes);
            }
            Log.i("%s : %s", name, value);
            if (name.equalsIgnoreCase("title")) {
                this.mTitle = value;
            } else if (name.equalsIgnoreCase("artist")) {
                this.mArtist = value.trim();
            } else if (name.equalsIgnoreCase("albumartist")) {
                if (TextUtils.isEmpty(this.mArtist)) {
                    this.mArtist = value.trim();
                }
            } else if (name.equalsIgnoreCase("album")) {
                this.mAlbum = value.trim();
            } else if (name.equalsIgnoreCase("language")) {
                this.mLanguage = value.trim();
            } else if (name.equalsIgnoreCase("duration")) {
                this.mDuration = (long) parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("width")) {
                this.mWidth = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("height")) {
                this.mHeight = parseSubstring(value, 0, 0);
            }
        }

        public void setMimeType(String mimeType) {
            Log.i("setMimeType: %s", mimeType);
            this.mMimeType = mimeType;
            this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
        }

        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put("_data", this.mPath);
            map.put(MediaColumns.DATE_MODIFIED, Long.valueOf(this.mLastModified));
            map.put(MediaColumns.SIZE, Long.valueOf(this.mFileSize));
            map.put(MediaColumns.MIME_TYPE, this.mMimeType);
            map.put("title", this.mTitle);
            if (MediaFile.isVideoFileType(this.mFileType)) {
                map.put("duration", Long.valueOf(this.mDuration));
                map.put("language", this.mLanguage);
                map.put("album", this.mAlbum);
                map.put("artist", this.mArtist);
                map.put("width", Integer.valueOf(this.mWidth));
                map.put("height", Integer.valueOf(this.mHeight));
            }
            return map;
        }

        private Uri endFile(FileCacheEntry entry) throws RemoteException {
            boolean isVideo;
            if (!MediaFile.isVideoFileType(this.mFileType) || this.mWidth <= 0 || this.mHeight <= 0) {
                isVideo = false;
            } else {
                isVideo = true;
            }
            if (!isVideo) {
                return null;
            }
            Uri tableUri = Media.CONTENT_URI;
            entry.mTableUri = tableUri;
            ContentValues values = toValues();
            if (TextUtils.isEmpty(values.getAsString("title"))) {
                String title = values.getAsString("_data");
                int lastSlash = title.lastIndexOf(47);
                if (lastSlash >= 0) {
                    lastSlash++;
                    if (lastSlash < title.length()) {
                        title = title.substring(lastSlash);
                    }
                }
                int lastDot = title.lastIndexOf(46);
                if (lastDot > 0) {
                    title = title.substring(0, lastDot);
                }
                values.put("title", title);
            }
            long rowId = entry.mRowId;
            Uri result;
            if (rowId == 0) {
                result = MediaScanner.this.mProvider.insert(tableUri, values);
                if (result == null) {
                    return result;
                }
                entry.mRowId = ContentUris.parseId(result);
                return result;
            }
            result = ContentUris.withAppendedId(tableUri, rowId);
            MediaScanner.this.mProvider.update(result, values, null, null);
            return result;
        }

        public void addNoMediaFolder(String path) {
            ContentValues values = new ContentValues();
            values.put("_data", "");
            try {
                MediaScanner.this.mProvider.update(Media.CONTENT_URI, values, "_data LIKE ?", new String[]{new StringBuilder(String.valueOf(path)).append('%').toString()});
            } catch (RemoteException e) {
                throw new RuntimeException();
            }
        }
    }

    private static native boolean loadFFmpeg_native(String str);

    private final native void native_finalize();

    private final native void native_init(MediaScannerClient mediaScannerClient);

    private native void processDirectory(String str, String str2);

    private native boolean processFile(String str, String str2);

    public native void release();

    static {
        String LIB_ROOT = Vitamio.getLibraryPath();
        Log.i("LIB ROOT: %s", LIB_ROOT);
        System.load(new StringBuilder(String.valueOf(LIB_ROOT)).append("libstlport_shared.so").toString());
        System.load(new StringBuilder(String.valueOf(LIB_ROOT)).append("libvscanner.so").toString());
        loadFFmpeg_native(new StringBuilder(String.valueOf(LIB_ROOT)).append("libffmpeg.so").toString());
    }

    public MediaScanner(Context ctx) {
        this.mContext = ctx;
        native_init(this.mClient);
    }

    private void initialize() {
        this.mCaseInsensitivePaths = true;
    }

    private void prescan(String filePath) throws RemoteException {
        this.mProvider = this.mContext.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        Cursor c = null;
        String where = null;
        String[] selectionArgs = null;
        if (this.mFileCache == null) {
            this.mFileCache = new HashMap();
        } else {
            this.mFileCache.clear();
        }
        if (filePath != null) {
            try {
                where = "_data=?";
                selectionArgs = new String[]{filePath};
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
        c = this.mProvider.query(Media.CONTENT_URI, VIDEO_PROJECTION, where, selectionArgs, null);
        if (c != null) {
            while (c.moveToNext()) {
                long rowId = c.getLong(0);
                String path = c.getString(1);
                long lastModified = c.getLong(2);
                if (path.startsWith("/")) {
                    File file = new File(path);
                    if (TextUtils.isEmpty(filePath) || file.exists()) {
                        String key;
                        path = FileUtils.getCanonical(file);
                        if (this.mCaseInsensitivePaths) {
                            key = path.toLowerCase();
                        } else {
                            key = path;
                        }
                        this.mFileCache.put(key, new FileCacheEntry(Media.CONTENT_URI, rowId, path, lastModified));
                    } else {
                        this.mProvider.delete(Media.CONTENT_URI, where, selectionArgs);
                        c.close();
                        c = null;
                        if (c != null) {
                            c.close();
                            return;
                        }
                        return;
                    }
                }
            }
            c.close();
            c = null;
        }
        if (c != null) {
            c.close();
        }
    }

    private void postscan(String[] directories) throws RemoteException {
        Iterator<FileCacheEntry> iterator = this.mFileCache.values().iterator();
        while (iterator.hasNext()) {
            FileCacheEntry entry = (FileCacheEntry) iterator.next();
            String path = entry.mPath;
            if (!(entry.mSeenInFileSystem || !inScanDirectory(path, directories) || new File(path).exists())) {
                this.mProvider.delete(ContentUris.withAppendedId(entry.mTableUri, entry.mRowId), null, null);
                iterator.remove();
            }
        }
        this.mFileCache.clear();
        this.mFileCache = null;
        this.mProvider.release();
        this.mProvider = null;
    }

    private boolean inScanDirectory(String path, String[] directories) {
        for (String startsWith : directories) {
            if (path.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    public void scanDirectories(String[] directories) {
        try {
            long start = System.currentTimeMillis();
            prescan(null);
            long prescan = System.currentTimeMillis();
            for (int i = 0; i < directories.length; i++) {
                if (!TextUtils.isEmpty(directories[i])) {
                    directories[i] = ContextUtils.fixLastSlash(directories[i]);
                    processDirectory(directories[i], MediaFile.sFileExtensions);
                }
            }
            long scan = System.currentTimeMillis();
            postscan(directories);
            long end = System.currentTimeMillis();
            Log.d(" prescan time: %dms", Long.valueOf(prescan - start));
            Log.d("    scan time: %dms", Long.valueOf(scan - prescan));
            Log.d("postscan time: %dms", Long.valueOf(end - scan));
            Log.d("   total time: %dms", Long.valueOf(end - start));
        } catch (Throwable e) {
            Log.e("SQLException in MediaScanner.scan()", e);
        } catch (Throwable e2) {
            Log.e("UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (Throwable e22) {
            Log.e("RemoteException in MediaScanner.scan()", e22);
        }
    }

    public Uri scanSingleFile(String path, String mimeType) {
        try {
            prescan(path);
            File file = new File(path);
            return this.mClient.doScanFile(path, file.lastModified() / 1000, file.length(), true);
        } catch (Throwable e) {
            Log.e("RemoteException in MediaScanner.scanFile()", e);
            return null;
        }
    }

    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }
}
