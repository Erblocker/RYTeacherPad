package io.vov.vitamio.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import com.foxit.uiextensions.utils.AppSQLite;
import io.vov.vitamio.utils.Log;

public final class MediaStore {
    public static final String AUTHORITY = "me.abitno.vplayer.mediaprovider";
    private static final String BASE_SQL_FIELDS = "_id INTEGER PRIMARY KEY,_data TEXT NOT NULL,_directory TEXT NOT NULL,_directory_name TEXT NOT NULL,_size INTEGER,_display_name TEXT,title TEXT,title_key TEXT,date_added INTEGER,date_modified INTEGER,mime_type TEXT,available_size INTEGER default 0,play_status INTEGER ,";
    public static final String CONTENT_AUTHORITY_SLASH = "content://me.abitno.vplayer.mediaprovider/";
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH);
    public static final String MEDIA_SCANNER_VOLUME = "volume";

    public static final class Audio {

        public interface AudioColumns extends MediaColumns {
            public static final String ALBUM = "album";
            public static final String ARTIST = "artist";
            public static final String BOOKMARK = "bookmark";
            public static final String COMPOSER = "composer";
            public static final String DURATION = "duration";
            public static final String TRACK = "track";
            public static final String YEAR = "year";
        }

        public static final class Media implements AudioColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/audio";
            public static final Uri CONTENT_URI = Uri.parse("content://me.abitno.vplayer.mediaprovider/audios/media");
        }
    }

    private static class InternalThumbnails implements BaseColumns {
        static final int DEFAULT_GROUP_ID = 0;
        private static final int MICRO_KIND = 3;
        private static final int MINI_KIND = 1;
        private static final String[] PROJECTION = new String[]{AppSQLite.KEY_ID, "_data"};
        private static byte[] sThumbBuf;
        private static final Object sThumbBufLock = new Object();

        private InternalThumbnails() {
        }

        private static Bitmap getMiniThumbFromFile(Cursor c, Uri baseUri, ContentResolver cr, Options options) {
            Bitmap bitmap = null;
            try {
                ParcelFileDescriptor pfdInput = cr.openFileDescriptor(ContentUris.withAppendedId(baseUri, c.getLong(0)), "r");
                bitmap = BitmapFactory.decodeFileDescriptor(pfdInput.getFileDescriptor(), null, options);
                pfdInput.close();
                return bitmap;
            } catch (Throwable ex) {
                Log.e("getMiniThumbFromFile", ex);
                return bitmap;
            } catch (Throwable ex2) {
                Log.e("getMiniThumbFromFile", ex2);
                return bitmap;
            } catch (Throwable ex22) {
                Log.e("getMiniThumbFromFile", ex22);
                return bitmap;
            }
        }

        static void cancelThumbnailRequest(ContentResolver cr, long origId, Uri baseUri, long groupId) {
            Cursor c = null;
            try {
                c = cr.query(baseUri.buildUpon().appendQueryParameter("cancel", "1").appendQueryParameter("orig_id", String.valueOf(origId)).appendQueryParameter("group_id", String.valueOf(groupId)).build(), PROJECTION, null, null, null);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        static String getThumbnailPath(Context ctx, ContentResolver cr, long origId, Uri baseUri) {
            String column = "video_id=";
            String path = "";
            Cursor c = null;
            try {
                c = cr.query(baseUri, PROJECTION, new StringBuilder(String.valueOf(column)).append(origId).toString(), null, null);
                if (c != null && c.moveToFirst()) {
                    path = c.getString(c.getColumnIndex("_data"));
                }
                if (c != null) {
                    c.close();
                }
                return path;
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }

        static Bitmap getThumbnail(Context ctx, ContentResolver cr, long origId, long groupId, int kind, Options options, Uri baseUri) {
            Cursor c;
            Bitmap bitmap = null;
            MiniThumbFile thumbFile = MiniThumbFile.instance(baseUri);
            if (thumbFile.getMagic(origId) != 0) {
                if (kind == 3) {
                    synchronized (sThumbBufLock) {
                        if (sThumbBuf == null) {
                            sThumbBuf = new byte[10000];
                        }
                        if (thumbFile.getMiniThumbFromFile(origId, sThumbBuf) != null) {
                            bitmap = BitmapFactory.decodeByteArray(sThumbBuf, 0, sThumbBuf.length);
                            if (bitmap == null) {
                                Log.d("couldn't decode byte array.", new Object[0]);
                            }
                        }
                    }
                    return bitmap;
                } else if (kind == 1) {
                    String column = "video_id=";
                    c = null;
                    try {
                        c = cr.query(baseUri, PROJECTION, new StringBuilder(String.valueOf(column)).append(origId).toString(), null, null);
                        if (c != null && c.moveToFirst()) {
                            bitmap = getMiniThumbFromFile(c, baseUri, cr, options);
                            if (bitmap != null) {
                                return bitmap;
                            }
                        }
                        if (c != null) {
                            c.close();
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                }
            }
            c = null;
            try {
                c = cr.query(baseUri.buildUpon().appendQueryParameter("blocking", "1").appendQueryParameter("orig_id", String.valueOf(origId)).appendQueryParameter("group_id", String.valueOf(groupId)).build(), PROJECTION, null, null, null);
                if (c == null) {
                    if (c != null) {
                        c.close();
                    }
                    return null;
                }
                if (kind == 3) {
                    synchronized (sThumbBufLock) {
                        if (sThumbBuf == null) {
                            sThumbBuf = new byte[10000];
                        }
                        if (thumbFile.getMiniThumbFromFile(origId, sThumbBuf) != null) {
                            bitmap = BitmapFactory.decodeByteArray(sThumbBuf, 0, sThumbBuf.length);
                            if (bitmap == null) {
                                Log.d("couldn't decode byte array.", new Object[0]);
                            }
                        }
                    }
                } else if (kind != 1) {
                    throw new IllegalArgumentException("Unsupported kind: " + kind);
                } else if (c.moveToFirst()) {
                    bitmap = getMiniThumbFromFile(c, baseUri, cr, options);
                }
                if (c != null) {
                    c.close();
                }
                return bitmap;
            } catch (Throwable ex) {
                try {
                    Log.e("getThumbnail", ex);
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    public interface MediaColumns extends BaseColumns {
        public static final String AVAILABLE_SIZE = "available_size";
        public static final String DATA = "_data";
        public static final String DATE_ADDED = "date_added";
        public static final String DATE_MODIFIED = "date_modified";
        public static final String DIRECTORY = "_directory";
        public static final String DIRECTORY_NAME = "_directory_name";
        public static final String DISPLAY_NAME = "_display_name";
        public static final String MIME_TYPE = "mime_type";
        public static final String PLAY_STATUS = "play_status";
        public static final String SIZE = "_size";
        public static final String TITLE = "title";
        public static final String TITLE_KEY = "title_key";
    }

    public static final class Video {

        public static class Thumbnails implements BaseColumns {
            public static final Uri CONTENT_URI = Uri.parse("content://me.abitno.vplayer.mediaprovider/videos/thumbnails");
            public static final String DATA = "_data";
            public static final String HEIGHT = "height";
            public static final String KIND = "kind";
            public static final int MICRO_KIND = 3;
            public static final int MINI_KIND = 1;
            protected static final String SQL_FIELDS = "_id INTEGER PRIMARY KEY,_data TEXT,video_id INTEGER,kind INTEGER,width INTEGER,height INTEGER";
            protected static final String SQL_INDEX_VIDEO_ID = "CREATE INDEX IF NOT EXISTS video_id_index on videothumbnails(video_id);";
            protected static final String SQL_TRIGGER_VIDEO_THUMBNAILS_CLEANUP = "CREATE TRIGGER IF NOT EXISTS videothumbnails_cleanup DELETE ON videothumbnails BEGIN SELECT _DELETE_FILE(old._data);END";
            protected static final String TABLE_NAME = "videothumbnails";
            public static final String THUMBNAILS_DIRECTORY = "Android/data/me.abitno.vplayer.t/thumbnails";
            public static final String VIDEO_ID = "video_id";
            public static final String WIDTH = "width";

            public static void cancelThumbnailRequest(ContentResolver cr, long origId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, CONTENT_URI, 0);
            }

            public static Bitmap getThumbnail(Context ctx, ContentResolver cr, long origId, int kind, Options options) {
                return InternalThumbnails.getThumbnail(ctx, cr, origId, 0, kind, options, CONTENT_URI);
            }

            public static Bitmap getThumbnail(Context ctx, ContentResolver cr, long origId, long groupId, int kind, Options options) {
                return InternalThumbnails.getThumbnail(ctx, cr, origId, groupId, kind, options, CONTENT_URI);
            }

            public static String getThumbnailPath(Context ctx, ContentResolver cr, long origId) {
                return InternalThumbnails.getThumbnailPath(ctx, cr, origId, CONTENT_URI);
            }

            public static void cancelThumbnailRequest(ContentResolver cr, long origId, long groupId) {
                InternalThumbnails.cancelThumbnailRequest(cr, origId, CONTENT_URI, groupId);
            }
        }

        public interface VideoColumns extends MediaColumns {
            public static final String ALBUM = "album";
            public static final String ARTIST = "artist";
            public static final String AUDIO_TRACK = "audio_track";
            public static final String BOOKMARK = "bookmark";
            public static final String DATE_TAKEN = "datetaken";
            public static final String DESCRIPTION = "description";
            public static final String DURATION = "duration";
            public static final String HEIGHT = "height";
            public static final String HIDDEN = "hidden";
            public static final String LANGUAGE = "language";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String MINI_THUMB_MAGIC = "mini_thumb_magic";
            public static final String SUBTRACK = "sub_track";
            public static final String WIDTH = "width";
        }

        public static final class Media implements VideoColumns {
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/video";
            public static final Uri CONTENT_URI = Uri.parse("content://me.abitno.vplayer.mediaprovider/videos/media");
            protected static final String SQL_FIELDS = "_id INTEGER PRIMARY KEY,_data TEXT NOT NULL,_directory TEXT NOT NULL,_directory_name TEXT NOT NULL,_size INTEGER,_display_name TEXT,title TEXT,title_key TEXT,date_added INTEGER,date_modified INTEGER,mime_type TEXT,available_size INTEGER default 0,play_status INTEGER ,duration INTEGER,artist TEXT,album TEXT,width INTEGER,height INTEGER,description TEXT,language TEXT,latitude DOUBLE,longitude DOUBLE,datetaken INTEGER,bookmark INTEGER,mini_thumb_magic INTEGER,hidden INTEGER default 0,sub_track TEXT,audio_track INTEGER";
            protected static final String SQL_TRIGGER_VIDEO_CLEANUP = "CREATE TRIGGER IF NOT EXISTS video_cleanup AFTER DELETE ON videos BEGIN SELECT _DELETE_FILE(old._data);SELECT _DELETE_FILE(old._data || '.ssi');END";
            protected static final String SQL_TRIGGER_VIDEO_UPDATE = "CREATE TRIGGER IF NOT EXISTS video_update AFTER UPDATE ON videos WHEN new._data <> old._data BEGIN SELECT _DELETE_FILE(old._data || '.ssi');END";
            protected static final String TABLE_NAME = "videos";
        }
    }

    public static Uri getMediaScannerUri() {
        return Uri.parse("content://me.abitno.vplayer.mediaprovider/media_scanner");
    }

    public static Uri getVolumeUri() {
        return Uri.parse("content://me.abitno.vplayer.mediaprovider/volume");
    }
}
