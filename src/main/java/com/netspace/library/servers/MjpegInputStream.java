package com.netspace.library.servers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaFormat;
import android.util.Log;
import com.netspace.library.threads.MulticastSendThread;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;
import org.apache.http.protocol.HTTP;

public class MjpegInputStream extends DataInputStream {
    private static final int FRAME_MAX_LENGTH = 10000300;
    private static final int HEADER_MAX_LENGTH = 300;
    private static final String TAG = "MjpegInputStream";
    private final String CONTENT_LENGTH = HTTP.CONTENT_LEN;
    private final byte[] CONTENT_START_MARKER = new byte[]{(byte) 13, (byte) 10, (byte) 13, (byte) 10};
    private final byte[] EOF_MARKER = new byte[]{(byte) -1, (byte) -39};
    private final String POSITION = "Position";
    private final byte[] SOI_MARKER = new byte[]{(byte) -1, (byte) -40};
    private int mContentLength = -1;
    private byte[] mFrameBuffer = new byte[512];
    private int mFrameCount = 0;
    private int mHeight = -1;
    private int mLeftPos = -1;
    private MulticastSendThread mMulticastSendThread;
    private int mRealScreenHeight = -1;
    private int mRealScreenWidth = -1;
    private int mTopPos = -1;
    private int mWidth = -1;
    private boolean mbDecodePartImage = false;
    private boolean mbIsH264 = false;
    private boolean mbIsPartImage = false;

    public MjpegInputStream(InputStream in) {
        super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
    }

    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
            if (((byte) in.readUnsignedByte()) == sequence[seqIndex]) {
                seqIndex++;
                if (seqIndex == sequence.length) {
                    return i + 1;
                }
            } else {
                seqIndex = 0;
            }
        }
        return -1;
    }

    public void setMulticastSendThread(MulticastSendThread SendThread) {
        this.mMulticastSendThread = SendThread;
    }

    public MulticastSendThread getMulticastSendThread() {
        return this.mMulticastSendThread;
    }

    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return end < 0 ? -1 : end - sequence.length;
    }

    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(HTTP.CONTENT_LEN));
    }

    private boolean parserH264(byte[] headerBytes) {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        try {
            props.load(headerIn);
            if (props.getProperty("Content-type").indexOf("h264") != -1) {
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean parseSize(byte[] headerBytes) {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        try {
            props.load(headerIn);
            String szSizeData = props.getProperty("ScreenSize");
            if (szSizeData == null || szSizeData.isEmpty()) {
                return false;
            }
            String[] arrPosData = szSizeData.split(",");
            if (arrPosData.length < 2) {
                return true;
            }
            this.mWidth = Integer.valueOf(arrPosData[0]).intValue();
            this.mHeight = Integer.valueOf(arrPosData[1]).intValue();
            if (this.mRealScreenWidth == -1) {
                this.mRealScreenWidth = this.mWidth;
            }
            if (this.mRealScreenHeight != -1) {
                return true;
            }
            this.mRealScreenHeight = this.mHeight;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean parseRealScreenSize(byte[] headerBytes) {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        try {
            props.load(headerIn);
            String szSizeData = props.getProperty("RealScreenSize");
            if (szSizeData == null || szSizeData.isEmpty()) {
                return false;
            }
            String[] arrPosData = szSizeData.split(",");
            if (arrPosData.length < 2) {
                return true;
            }
            this.mRealScreenWidth = Integer.valueOf(arrPosData[0]).intValue();
            this.mRealScreenHeight = Integer.valueOf(arrPosData[1]).intValue();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean parsePosition(byte[] headerBytes) {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        try {
            props.load(headerIn);
            String szPositionData = props.getProperty("Position");
            if (szPositionData == null || szPositionData.isEmpty()) {
                return false;
            }
            String[] arrPosData = szPositionData.split(",");
            if (arrPosData.length < 2) {
                return true;
            }
            this.mLeftPos = Integer.valueOf(arrPosData[0]).intValue();
            this.mTopPos = Integer.valueOf(arrPosData[1]).intValue();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getIsPartImage() {
        return this.mbIsPartImage;
    }

    public void setDecodePartImage(boolean bEnable) {
        this.mbDecodePartImage = bEnable;
    }

    public int getTopPos() {
        return this.mTopPos;
    }

    public int getLeftPos() {
        return this.mLeftPos;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getRealScreenWidth() {
        return this.mRealScreenWidth;
    }

    public int getRealScreenHeight() {
        return this.mRealScreenHeight;
    }

    public boolean isH264() {
        mark(FRAME_MAX_LENGTH);
        try {
            int headerLen = getStartOfSequence(this, this.CONTENT_START_MARKER);
            if (headerLen == -1) {
                return false;
            }
            reset();
            byte[] header = new byte[headerLen];
            readFully(header);
            this.mbIsH264 = parserH264(header);
            parseSize(header);
            parseRealScreenSize(header);
            header = null;
            reset();
            return this.mbIsH264;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean readH264Info(MediaFormat format, FileOutputStream dumpFile) throws IOException {
        mark(FRAME_MAX_LENGTH);
        try {
            int headerLen = getStartOfSequence(this, this.CONTENT_START_MARKER);
            if (headerLen == -1) {
                return false;
            }
            reset();
            byte[] header = new byte[headerLen];
            boolean bDecodeThisFrame = false;
            readFully(header);
            this.mContentLength = parseContentLength(header);
            header = null;
            reset();
            if (this.mContentLength > 0) {
                bDecodeThisFrame = true;
            }
            if (!bDecodeThisFrame) {
                return false;
            }
            int i;
            byte[] frameData = new byte[this.mContentLength];
            byte[] SPSData = new byte[512];
            int nSPSCount = 0;
            byte[] PPSData = new byte[512];
            int nPPSCount = 0;
            boolean bSPSData = false;
            boolean bPPSData = false;
            skipBytes(headerLen + 4);
            readFully(frameData);
            if (dumpFile != null) {
                byte[] byteMark = new byte[10];
                for (i = 0; i < 10; i++) {
                    byteMark[i] = (byte) -1;
                }
                dumpFile.write(byteMark);
                dumpFile.write(frameData);
            }
            i = 0;
            while (i < this.mContentLength) {
                if (frameData[i] == (byte) 0 && frameData[i + 1] == (byte) 0 && frameData[i + 2] == (byte) 1 && frameData[i + 3] == (byte) 103) {
                    bSPSData = true;
                    bPPSData = false;
                }
                if (frameData[i] == (byte) 0 && frameData[i + 1] == (byte) 0 && frameData[i + 2] == (byte) 1 && frameData[i + 3] == (byte) 104) {
                    bSPSData = false;
                    bPPSData = true;
                }
                if (frameData[i] == (byte) 0 && frameData[i + 1] == (byte) 0 && frameData[i + 2] == (byte) 1 && frameData[i + 3] != (byte) 103 && frameData[i + 3] != (byte) 104) {
                    break;
                }
                if (bSPSData) {
                    SPSData[nSPSCount] = frameData[i];
                    nSPSCount++;
                }
                if (bPPSData) {
                    PPSData[nPPSCount] = frameData[i];
                    nPPSCount++;
                }
                i++;
            }
            if (nSPSCount == 0) {
                Log.e(TAG, "Not found SPS Data in stream.");
            }
            if (nPPSCount == 0) {
                Log.e(TAG, "Not found PPS Data in stream.");
            }
            frameData = null;
            if (nSPSCount <= 0 || nPPSCount <= 0) {
                return false;
            }
            format.setByteBuffer("csd-0", ByteBuffer.wrap(SPSData, 0, nSPSCount));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(PPSData, 0, nPPSCount));
            return true;
        } catch (Throwable nfe) {
            nfe.getStackTrace();
            Log.d(TAG, "catch NumberFormatException hit", nfe);
            this.mContentLength = getEndOfSeqeunce(this, this.EOF_MARKER);
            throw new IOException("Can not get a vaild content-length. Connection maybe bad.");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readH264Frame(ByteBuffer buffer, FileOutputStream dumpFile) throws IOException {
        frameTest = new byte[4];
        int nSkipCount = 0;
        int nContentCount = 0;
        frameTest[0] = (byte) -1;
        frameTest[1] = (byte) -1;
        frameTest[2] = (byte) -1;
        frameTest[3] = (byte) -1;
        while (true) {
            if (frameTest[0] == (byte) 0 && frameTest[1] == (byte) 0 && frameTest[2] == (byte) 1 && frameTest[3] == (byte) 101) {
                Log.e(TAG, "KeyFrame found during scan start bit.");
            }
            if (frameTest[0] == (byte) 0 && frameTest[1] == (byte) 0 && frameTest[2] == (byte) 0 && frameTest[3] == (byte) 1) {
                break;
            }
            try {
                frameTest[0] = frameTest[1];
                frameTest[1] = frameTest[2];
                frameTest[2] = frameTest[3];
                frameTest[3] = readByte();
                nSkipCount++;
            } catch (IOException e) {
                throw e;
            }
        }
        Log.d(TAG, "Skip " + nSkipCount + " bytes before found frame head.");
        mark(FRAME_MAX_LENGTH);
        buffer.put(frameTest[0]);
        frameTest[0] = frameTest[1];
        frameTest[1] = frameTest[2];
        frameTest[2] = frameTest[3];
        frameTest[3] = readByte();
        while (true) {
            try {
                if (frameTest[0] == (byte) 0 && frameTest[1] == (byte) 0 && frameTest[2] == (byte) 1 && frameTest[3] == (byte) 101) {
                    Log.d(TAG, "KeyFrame found in content " + nContentCount);
                }
                if (frameTest[0] != (byte) 0 || frameTest[1] != (byte) 0 || frameTest[2] != (byte) 0 || frameTest[3] != (byte) 1) {
                    if (frameTest[0] == (byte) 13 && frameTest[1] == (byte) 10 && frameTest[2] == (byte) 13 && frameTest[3] == (byte) 10) {
                        break;
                    }
                    if (frameTest[0] == (byte) 13 && frameTest[1] == (byte) 10 && frameTest[2] == (byte) 45 && frameTest[3] == (byte) 45) {
                        break;
                    }
                    nContentCount++;
                    buffer.put(frameTest[0]);
                    frameTest[0] = frameTest[1];
                    frameTest[1] = frameTest[2];
                    frameTest[2] = frameTest[3];
                    frameTest[3] = readByte();
                } else {
                    if (nContentCount > 4096) {
                        break;
                    }
                    nContentCount++;
                    buffer.put(frameTest[0]);
                    frameTest[0] = frameTest[1];
                    frameTest[1] = frameTest[2];
                    frameTest[2] = frameTest[3];
                    frameTest[3] = readByte();
                }
                Log.d(TAG, "frameLength=" + buffer.position());
                return true;
            } catch (IOException e2) {
                throw e2;
            }
        }
        Log.d(TAG, "Frame end with crlf--");
        Log.d(TAG, "frameLength=" + buffer.position());
        return true;
    }

    public boolean readH264Frame2(ByteBuffer buffer, FileOutputStream dumpFile) throws IOException {
        mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, this.CONTENT_START_MARKER);
        if (headerLen == -1) {
            Log.e(TAG, "getStartOfSequence return -1");
            return false;
        }
        reset();
        byte[] header = new byte[headerLen];
        boolean bDecodeThisFrame = false;
        readFully(header);
        try {
            this.mContentLength = parseContentLength(header);
            header = null;
            reset();
            if (this.mContentLength > 0) {
                bDecodeThisFrame = true;
            } else {
                Log.e(TAG, "Decode skip because mContentLength = " + this.mContentLength);
            }
            if (!bDecodeThisFrame) {
                return false;
            }
            if (this.mFrameBuffer.length < this.mContentLength) {
                this.mFrameBuffer = new byte[this.mContentLength];
            }
            skipBytes(headerLen + 4);
            readFully(this.mFrameBuffer, 0, this.mContentLength);
            if (dumpFile != null) {
                byte[] byteMark = new byte[10];
                for (int i = 0; i < 10; i++) {
                    byteMark[i] = (byte) -1;
                }
                dumpFile.write(byteMark);
                dumpFile.write(this.mFrameBuffer, 0, this.mContentLength);
            }
            if (buffer != null) {
                buffer.put(this.mFrameBuffer, 0, this.mContentLength);
            }
            if (this.mMulticastSendThread != null) {
                this.mMulticastSendThread.sendData(this.mFrameBuffer, 0, this.mContentLength);
            }
            return true;
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            Log.e(TAG, "catch NumberFormatException hit", nfe);
            this.mContentLength = getEndOfSeqeunce(this, this.EOF_MARKER);
            throw new IOException("Can not found a vaild content length. Connection maybe bad.");
        }
    }

    public Bitmap readMjpegFrame(boolean bNoDecode) throws IOException {
        mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, this.SOI_MARKER);
        if (headerLen == -1) {
            return null;
        }
        reset();
        byte[] header = new byte[headerLen];
        boolean bHasPositionData = false;
        boolean bDecodeThisFrame = false;
        readFully(header);
        try {
            this.mContentLength = parseContentLength(header);
            bHasPositionData = parsePosition(header);
            this.mbIsPartImage = bHasPositionData;
            this.mbIsH264 = parserH264(header);
            header = (byte[]) null;
        } catch (NumberFormatException nfe) {
            nfe.getStackTrace();
            Log.d(TAG, "catch NumberFormatException hit", nfe);
            this.mContentLength = getEndOfSeqeunce(this, this.EOF_MARKER);
        }
        reset();
        if (this.mContentLength > 0) {
            if (!bHasPositionData) {
                bDecodeThisFrame = true;
            } else if (this.mbDecodePartImage) {
                bDecodeThisFrame = true;
            }
        }
        if (!bDecodeThisFrame) {
            return null;
        }
        Bitmap bmResult = null;
        byte[] frameData = new byte[this.mContentLength];
        skipBytes(headerLen);
        readFully(frameData);
        if (!bNoDecode) {
            bmResult = BitmapFactory.decodeByteArray(frameData, 0, this.mContentLength);
        }
        frameData = null;
        return bmResult;
    }
}
