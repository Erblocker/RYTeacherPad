package io.vov.vitamio.provider;

import android.net.Uri;
import android.os.Environment;
import io.vov.vitamio.provider.MediaStore.Video.Thumbnails;
import io.vov.vitamio.utils.Log;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Hashtable;

public class MiniThumbFile {
    protected static final int BYTES_PER_MINTHUMB = 10000;
    private static final int HEADER_SIZE = 13;
    private static final int MINI_THUMB_DATA_FILE_VERSION = 7;
    private static Hashtable<String, MiniThumbFile> sThumbFiles = new Hashtable();
    private ByteBuffer mBuffer = ByteBuffer.allocateDirect(BYTES_PER_MINTHUMB);
    private FileChannel mChannel;
    private RandomAccessFile mMiniThumbFile;
    private Uri mUri;

    public MiniThumbFile(Uri uri) {
        this.mUri = uri;
    }

    protected static synchronized void reset() {
        synchronized (MiniThumbFile.class) {
            for (MiniThumbFile file : sThumbFiles.values()) {
                file.deactivate();
            }
            sThumbFiles.clear();
        }
    }

    protected static synchronized MiniThumbFile instance(Uri uri) {
        MiniThumbFile file;
        synchronized (MiniThumbFile.class) {
            String type = (String) uri.getPathSegments().get(0);
            file = (MiniThumbFile) sThumbFiles.get(type);
            if (file == null) {
                file = new MiniThumbFile(Uri.parse(new StringBuilder(MediaStore.CONTENT_AUTHORITY_SLASH).append(type).append("/media").toString()));
                sThumbFiles.put(type, file);
            }
        }
        return file;
    }

    private String randomAccessFilePath(int version) {
        return new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().toString())).append("/").append(Thumbnails.THUMBNAILS_DIRECTORY).toString())).append("/.thumbdata").append(version).append("-").append(this.mUri.hashCode()).toString();
    }

    private void removeOldFile() {
        File oldFile = new File(randomAccessFilePath(6));
        if (oldFile.exists()) {
            try {
                oldFile.delete();
            } catch (SecurityException e) {
            }
        }
    }

    private RandomAccessFile miniThumbDataFile() {
        if (this.mMiniThumbFile == null) {
            removeOldFile();
            String path = randomAccessFilePath(7);
            File directory = new File(path).getParentFile();
            if (!(directory.isDirectory() || directory.mkdirs())) {
                Log.e("Unable to create .thumbnails directory %s", directory.toString());
            }
            File f = new File(path);
            try {
                this.mMiniThumbFile = new RandomAccessFile(f, "rw");
            } catch (IOException e) {
                try {
                    this.mMiniThumbFile = new RandomAccessFile(f, "r");
                } catch (IOException e2) {
                }
            }
            if (this.mMiniThumbFile != null) {
                this.mChannel = this.mMiniThumbFile.getChannel();
            }
        }
        return this.mMiniThumbFile;
    }

    protected synchronized void deactivate() {
        if (this.mMiniThumbFile != null) {
            try {
                this.mMiniThumbFile.close();
                this.mMiniThumbFile = null;
            } catch (IOException e) {
            }
        }
    }

    protected synchronized long getMagic(long id) {
        long j;
        if (miniThumbDataFile() != null) {
            long pos = id * 10000;
            FileLock lock = null;
            try {
                this.mBuffer.clear();
                this.mBuffer.limit(9);
                lock = this.mChannel.lock(pos, 9, true);
                if (this.mChannel.read(this.mBuffer, pos) == 9) {
                    this.mBuffer.position(0);
                    if (this.mBuffer.get() == (byte) 1) {
                        j = this.mBuffer.getLong();
                        if (lock != null) {
                            try {
                                lock.release();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e2) {
                    }
                }
            } catch (Throwable ex) {
                Log.e("Got exception checking file magic: ", ex);
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException ex2) {
                Log.e("Got exception when reading magic, id = %d, disk full or mount read-only? %s", Long.valueOf(id), ex2.getClass().toString());
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th) {
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e5) {
                    }
                }
            }
        }
        j = 0;
        return j;
    }

    protected synchronized void saveMiniThumbToFile(byte[] data, long id, long magic) throws IOException {
        if (miniThumbDataFile() != null) {
            long pos = id * 10000;
            FileLock lock = null;
            if (data != null) {
                try {
                    if (data.length <= 9987) {
                        this.mBuffer.clear();
                        this.mBuffer.put((byte) 1);
                        this.mBuffer.putLong(magic);
                        this.mBuffer.putInt(data.length);
                        this.mBuffer.put(data);
                        this.mBuffer.flip();
                        lock = this.mChannel.lock(pos, 10000, false);
                        this.mChannel.write(this.mBuffer, pos);
                    } else if (lock != null) {
                        try {
                            lock.release();
                        } catch (IOException e) {
                        }
                    }
                } catch (IOException ex) {
                    Log.e("couldn't save mini thumbnail data for %d; %s", Long.valueOf(id), ex.getMessage());
                    throw ex;
                } catch (RuntimeException ex2) {
                    Log.e("couldn't save mini thumbnail data for %d, disk full or mount read-only? %s", Long.valueOf(id), ex2.getClass().toString());
                    if (lock != null) {
                        try {
                            lock.release();
                        } catch (IOException e2) {
                        }
                    }
                } catch (Throwable th) {
                    if (lock != null) {
                        try {
                            lock.release();
                        } catch (IOException e3) {
                        }
                    }
                }
            }
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e4) {
                }
            }
        }
        return;
    }

    protected synchronized byte[] getMiniThumbFromFile(long id, byte[] data) {
        if (miniThumbDataFile() == null) {
            data = null;
        } else {
            long pos = id * 10000;
            FileLock lock = null;
            try {
                this.mBuffer.clear();
                lock = this.mChannel.lock(pos, 10000, true);
                int size = this.mChannel.read(this.mBuffer, pos);
                if (size > 13) {
                    this.mBuffer.position(9);
                    int length = this.mBuffer.getInt();
                    if (size >= length + 13 && data.length >= length) {
                        this.mBuffer.get(data, 0, length);
                        if (lock != null) {
                            try {
                                lock.release();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException ex) {
                Log.e("got exception when reading thumbnail id = %d, exception: %s", Long.valueOf(id), ex.getMessage());
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException ex2) {
                Log.e("Got exception when reading thumbnail, id = %d, disk full or mount read-only? %s", Long.valueOf(id), ex2.getClass().toString());
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th) {
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e5) {
                    }
                }
            }
            data = null;
        }
        return data;
    }
}
