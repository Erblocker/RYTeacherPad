package net.majorkernelpanic.streaming;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build.VERSION;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Random;
import net.majorkernelpanic.streaming.rtp.AbstractPacketizer;

public abstract class MediaStream implements Stream {
    public static final byte MODE_MEDIACODEC_API = (byte) 2;
    public static final byte MODE_MEDIACODEC_API_2 = (byte) 5;
    public static final byte MODE_MEDIARECORDER_API = (byte) 1;
    public static final byte PIPE_API_LS = (byte) 1;
    public static final byte PIPE_API_PFD = (byte) 2;
    protected static final String PREF_PREFIX = "libstreaming-";
    protected static final String TAG = "MediaStream";
    protected static final byte sPipeApi;
    protected static byte sSuggestedMode;
    protected byte mChannelIdentifier = (byte) 0;
    protected boolean mConfigured = false;
    protected InetAddress mDestination;
    private LocalServerSocket mLss = null;
    protected MediaCodec mMediaCodec;
    protected MediaRecorder mMediaRecorder;
    protected byte mMode = sSuggestedMode;
    protected OutputStream mOutputStream = null;
    protected AbstractPacketizer mPacketizer = null;
    protected ParcelFileDescriptor[] mParcelFileDescriptors;
    protected ParcelFileDescriptor mParcelRead;
    protected ParcelFileDescriptor mParcelWrite;
    protected LocalSocket mReceiver;
    protected byte mRequestedMode = sSuggestedMode;
    protected int mRtcpPort = 0;
    protected int mRtpPort = 0;
    protected LocalSocket mSender = null;
    private int mSocketId;
    protected boolean mStreaming = false;
    private int mTTL = 64;

    protected abstract void encodeWithMediaCodec() throws IOException;

    protected abstract void encodeWithMediaRecorder() throws IOException;

    public abstract String getSessionDescription();

    static {
        sSuggestedMode = (byte) 1;
        try {
            Class.forName("android.media.MediaCodec");
            sSuggestedMode = (byte) 2;
            Log.i(TAG, "Phone supports the MediaCoded API");
        } catch (ClassNotFoundException e) {
            sSuggestedMode = (byte) 1;
            Log.i(TAG, "Phone does not support the MediaCodec API");
        }
        sSuggestedMode = (byte) 2;
        if (VERSION.SDK_INT > 20) {
            sPipeApi = (byte) 2;
        } else {
            sPipeApi = (byte) 1;
        }
    }

    public void setDestinationAddress(InetAddress dest) {
        this.mDestination = dest;
    }

    public void setDestinationPorts(int dport) {
        if (dport % 2 == 1) {
            this.mRtpPort = dport - 1;
            this.mRtcpPort = dport;
            return;
        }
        this.mRtpPort = dport;
        this.mRtcpPort = dport + 1;
    }

    public void setDestinationPorts(int rtpPort, int rtcpPort) {
        this.mRtpPort = rtpPort;
        this.mRtcpPort = rtcpPort;
        this.mOutputStream = null;
    }

    public void setOutputStream(OutputStream stream, byte channelIdentifier) {
        this.mOutputStream = stream;
        this.mChannelIdentifier = channelIdentifier;
    }

    public void setTimeToLive(int ttl) throws IOException {
        this.mTTL = ttl;
    }

    public int[] getDestinationPorts() {
        return new int[]{this.mRtpPort, this.mRtcpPort};
    }

    public int[] getLocalPorts() {
        return this.mPacketizer.getRtpSocket().getLocalPorts();
    }

    public void setStreamingMethod(byte mode) {
        this.mRequestedMode = mode;
    }

    public byte getStreamingMethod() {
        return this.mMode;
    }

    public AbstractPacketizer getPacketizer() {
        return this.mPacketizer;
    }

    public long getBitrate() {
        return !this.mStreaming ? 0 : this.mPacketizer.getRtpSocket().getBitrate();
    }

    public boolean isStreaming() {
        return this.mStreaming;
    }

    public synchronized void configure() throws IllegalStateException, IOException {
        if (this.mStreaming) {
            throw new IllegalStateException("Can't be called while streaming.");
        }
        if (this.mPacketizer != null) {
            this.mPacketizer.setDestination(this.mDestination, this.mRtpPort, this.mRtcpPort);
            this.mPacketizer.getRtpSocket().setOutputStream(this.mOutputStream, this.mChannelIdentifier);
        }
        this.mMode = this.mRequestedMode;
        this.mConfigured = true;
    }

    public synchronized void start() throws IllegalStateException, IOException {
        if (this.mDestination == null) {
            throw new IllegalStateException("No destination ip address set for the stream !");
        } else if (this.mRtpPort <= 0 || this.mRtcpPort <= 0) {
            throw new IllegalStateException("No destination ports set for the stream !");
        } else {
            this.mPacketizer.setTimeToLive(this.mTTL);
            if (this.mMode != (byte) 1) {
                encodeWithMediaCodec();
            } else {
                encodeWithMediaRecorder();
            }
        }
    }

    @SuppressLint({"NewApi"})
    public synchronized void stop() {
        if (this.mStreaming) {
            try {
                if (this.mMode == (byte) 1) {
                    this.mMediaRecorder.stop();
                    this.mMediaRecorder.release();
                    this.mMediaRecorder = null;
                    closeSockets();
                    this.mPacketizer.stop();
                } else {
                    this.mPacketizer.stop();
                    this.mMediaCodec.stop();
                    this.mMediaCodec.release();
                    this.mMediaCodec = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mStreaming = false;
        }
    }

    public int getSSRC() {
        return getPacketizer().getSSRC();
    }

    protected void createSockets() throws IOException {
        if (sPipeApi == (byte) 1) {
            String LOCAL_ADDR = "net.majorkernelpanic.streaming-";
            int i = 0;
            while (i < 10) {
                try {
                    this.mSocketId = new Random().nextInt();
                    this.mLss = new LocalServerSocket("net.majorkernelpanic.streaming-" + this.mSocketId);
                    break;
                } catch (IOException e) {
                    i++;
                }
            }
            this.mReceiver = new LocalSocket();
            this.mReceiver.connect(new LocalSocketAddress("net.majorkernelpanic.streaming-" + this.mSocketId));
            this.mReceiver.setReceiveBufferSize(500000);
            this.mReceiver.setSoTimeout(3000);
            this.mSender = this.mLss.accept();
            this.mSender.setSendBufferSize(500000);
            return;
        }
        Log.e(TAG, "parcelFileDescriptors createPipe version = Lollipop");
        this.mParcelFileDescriptors = ParcelFileDescriptor.createPipe();
        this.mParcelRead = new ParcelFileDescriptor(this.mParcelFileDescriptors[0]);
        this.mParcelWrite = new ParcelFileDescriptor(this.mParcelFileDescriptors[1]);
    }

    protected void closeSockets() {
        if (sPipeApi == (byte) 1) {
            try {
                this.mReceiver.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.mSender.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                this.mLss.close();
            } catch (Exception e22) {
                e22.printStackTrace();
            }
            this.mLss = null;
            this.mSender = null;
            this.mReceiver = null;
            return;
        }
        try {
            if (this.mParcelRead != null) {
                this.mParcelRead.close();
            }
        } catch (Exception e222) {
            e222.printStackTrace();
        }
        try {
            if (this.mParcelWrite != null) {
                this.mParcelWrite.close();
            }
        } catch (Exception e2222) {
            e2222.printStackTrace();
        }
    }
}
