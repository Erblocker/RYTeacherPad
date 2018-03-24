package net.majorkernelpanic.streaming.mp4;

import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MP4Parser {
    private static final String TAG = "MP4Parser";
    private HashMap<String, Long> mBoxes = new HashMap();
    private final RandomAccessFile mFile;
    private long mPos = 0;

    public static MP4Parser parse(String path) throws IOException {
        return new MP4Parser(path);
    }

    private MP4Parser(String path) throws IOException, FileNotFoundException {
        this.mFile = new RandomAccessFile(new File(path), "r");
        try {
            parse("", this.mFile.length());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Parse error: malformed mp4 file");
        }
    }

    public void close() {
        try {
            this.mFile.close();
        } catch (Exception e) {
        }
    }

    public long getBoxPos(String box) throws IOException {
        if (((Long) this.mBoxes.get(box)) != null) {
            return ((Long) this.mBoxes.get(box)).longValue();
        }
        throw new IOException("Box not found: " + box);
    }

    public StsdBox getStsdBox() throws IOException {
        try {
            return new StsdBox(this.mFile, getBoxPos("/moov/trak/mdia/minf/stbl/stsd"));
        } catch (IOException e) {
            throw new IOException("stsd box could not be found");
        }
    }

    private void parse(String path, long len) throws IOException {
        long sum = 0;
        byte[] buffer = new byte[8];
        String name = "";
        if (!path.equals("")) {
            this.mBoxes.put(path, Long.valueOf(this.mPos - 8));
        }
        while (sum < len) {
            this.mFile.read(buffer, 0, 8);
            this.mPos += 8;
            sum += 8;
            if (validBoxName(buffer)) {
                long newlen;
                name = new String(buffer, 4, 4);
                if (buffer[3] == (byte) 1) {
                    this.mFile.read(buffer, 0, 8);
                    this.mPos += 8;
                    sum += 8;
                    newlen = ByteBuffer.wrap(buffer, 0, 8).getLong() - 16;
                } else {
                    newlen = (long) (ByteBuffer.wrap(buffer, 0, 4).getInt() - 8);
                }
                if (newlen < 0 || newlen == 1061109559) {
                    throw new IOException();
                }
                Log.d(TAG, "Atom -> name: " + name + " position: " + this.mPos + ", length: " + newlen);
                sum += newlen;
                parse(new StringBuilder(String.valueOf(path)).append('/').append(name).toString(), newlen);
            } else if (len < 8) {
                this.mFile.seek((this.mFile.getFilePointer() - 8) + len);
                sum += len - 8;
            } else if (this.mFile.skipBytes((int) (len - 8)) < ((int) (len - 8))) {
                throw new IOException();
            } else {
                this.mPos += len - 8;
                sum += len - 8;
            }
        }
    }

    private boolean validBoxName(byte[] buffer) {
        int i = 0;
        while (i < 4) {
            if ((buffer[i + 4] < (byte) 97 || buffer[i + 4] > (byte) 122) && (buffer[i + 4] < (byte) 48 || buffer[i + 4] > (byte) 57)) {
                return false;
            }
            i++;
        }
        return true;
    }

    static String toHexString(byte[] buffer, int start, int len) {
        StringBuilder s = new StringBuilder();
        for (int i = start; i < start + len; i++) {
            String c = Integer.toHexString(buffer[i] & 255);
            if (c.length() < 2) {
                c = "0" + c;
            }
            s.append(c);
        }
        return s.toString();
    }
}
