package net.majorkernelpanic.streaming.audio;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.os.Build.VERSION;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import net.majorkernelpanic.streaming.rtp.AACADTSPacketizer;
import net.majorkernelpanic.streaming.rtp.AACLATMPacketizer;
import net.majorkernelpanic.streaming.rtp.MediaCodecInputStream;
import org.kxml2.wap.Wbxml;

public class AACStream extends AudioStream {
    private static final String[] AUDIO_OBJECT_TYPES = new String[]{"NULL", "AAC Main", "AAC LC (Low Complexity)", "AAC SSR (Scalable Sample Rate)", "AAC LTP (Long Term Prediction)"};
    public static final int[] AUDIO_SAMPLING_RATES = new int[]{96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000, 7350, -1, -1, -1};
    public static final String TAG = "AACStream";
    private AudioRecord mAudioRecord = null;
    private int mChannel;
    private int mConfig;
    private int mProfile;
    private int mSamplingRateIndex;
    private String mSessionDescription = null;
    private SharedPreferences mSettings = null;
    private Thread mThread = null;

    public AACStream() {
        if (AACStreamingSupported()) {
            Log.d(TAG, "AAC supported on this phone");
        } else {
            Log.e(TAG, "AAC not supported on this phone");
            throw new RuntimeException("AAC not supported by this phone !");
        }
    }

    private static boolean AACStreamingSupported() {
        if (VERSION.SDK_INT < 14) {
            return false;
        }
        try {
            OutputFormat.class.getField("AAC_ADTS");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setPreferences(SharedPreferences prefs) {
        this.mSettings = prefs;
    }

    public synchronized void start() throws IllegalStateException, IOException {
        if (!this.mStreaming) {
            configure();
            super.start();
        }
    }

    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();
        this.mQuality = this.mRequestedQuality.clone();
        int i = 0;
        while (i < AUDIO_SAMPLING_RATES.length) {
            if (AUDIO_SAMPLING_RATES[i] == this.mQuality.samplingRate) {
                this.mSamplingRateIndex = i;
                break;
            }
            i++;
        }
        if (i > 12) {
            this.mQuality.samplingRate = 16000;
        }
        if (this.mMode != this.mRequestedMode || this.mPacketizer == null) {
            this.mMode = this.mRequestedMode;
            if (this.mMode == (byte) 1) {
                this.mPacketizer = new AACADTSPacketizer();
            } else {
                this.mPacketizer = new AACLATMPacketizer();
            }
            this.mPacketizer.setDestination(this.mDestination, this.mRtpPort, this.mRtcpPort);
            this.mPacketizer.getRtpSocket().setOutputStream(this.mOutputStream, this.mChannelIdentifier);
        }
        if (this.mMode == (byte) 1) {
            testADTS();
            this.mSessionDescription = "m=audio " + String.valueOf(getDestinationPorts()[0]) + " RTP/AVP 96\r\n" + "a=rtpmap:96 mpeg4-generic/" + this.mQuality.samplingRate + "\r\n" + "a=fmtp:96 streamtype=5; profile-level-id=15; mode=AAC-hbr; config=" + Integer.toHexString(this.mConfig) + "; SizeLength=13; IndexLength=3; IndexDeltaLength=3;\r\n";
        } else {
            this.mProfile = 2;
            this.mChannel = 1;
            this.mConfig = (((this.mProfile & 31) << 11) | ((this.mSamplingRateIndex & 15) << 7)) | ((this.mChannel & 15) << 3);
            this.mSessionDescription = "m=audio " + String.valueOf(getDestinationPorts()[0]) + " RTP/AVP 96\r\n" + "a=rtpmap:96 mpeg4-generic/" + this.mQuality.samplingRate + "\r\n" + "a=fmtp:96 streamtype=5; profile-level-id=15; mode=AAC-hbr; config=" + Integer.toHexString(this.mConfig) + "; SizeLength=13; IndexLength=3; IndexDeltaLength=3;\r\n";
        }
    }

    protected void encodeWithMediaRecorder() throws IOException {
        testADTS();
        ((AACADTSPacketizer) this.mPacketizer).setSamplingRate(this.mQuality.samplingRate);
        super.encodeWithMediaRecorder();
    }

    @SuppressLint({"InlinedApi", "NewApi"})
    protected void encodeWithMediaCodec() throws IOException {
        final int bufferSize = AudioRecord.getMinBufferSize(this.mQuality.samplingRate, 16, 2) * 2;
        ((AACLATMPacketizer) this.mPacketizer).setSamplingRate(this.mQuality.samplingRate);
        this.mAudioRecord = new AudioRecord(1, this.mQuality.samplingRate, 16, 2, bufferSize);
        this.mMediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        MediaFormat format = new MediaFormat();
        format.setString(io.vov.vitamio.MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger("bitrate", this.mQuality.bitRate);
        format.setInteger(io.vov.vitamio.MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(io.vov.vitamio.MediaFormat.KEY_SAMPLE_RATE, this.mQuality.samplingRate);
        format.setInteger(io.vov.vitamio.MediaFormat.KEY_AAC_PROFILE, 2);
        format.setInteger(io.vov.vitamio.MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize);
        this.mMediaCodec.configure(format, null, null, 1);
        this.mAudioRecord.startRecording();
        this.mMediaCodec.start();
        MediaCodecInputStream inputStream = new MediaCodecInputStream(this.mMediaCodec);
        final ByteBuffer[] inputBuffers = this.mMediaCodec.getInputBuffers();
        this.mThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        int bufferIndex = AACStream.this.mMediaCodec.dequeueInputBuffer(10000);
                        if (bufferIndex >= 0) {
                            inputBuffers[bufferIndex].clear();
                            int len = AACStream.this.mAudioRecord.read(inputBuffers[bufferIndex], bufferSize);
                            if (len == -3 || len == -2) {
                                Log.e(AACStream.TAG, "An error occured with the AudioRecord API !");
                            } else {
                                AACStream.this.mMediaCodec.queueInputBuffer(bufferIndex, 0, len, System.nanoTime() / 1000, 0);
                            }
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
        this.mThread.start();
        this.mPacketizer.setInputStream(inputStream);
        this.mPacketizer.start();
        this.mStreaming = true;
    }

    public synchronized void stop() {
        if (this.mStreaming) {
            if (this.mMode == (byte) 2) {
                Log.d(TAG, "Interrupting threads...");
                this.mThread.interrupt();
                this.mAudioRecord.stop();
                this.mAudioRecord.release();
                this.mAudioRecord = null;
            }
            super.stop();
        }
    }

    public String getSessionDescription() throws IllegalStateException {
        if (this.mSessionDescription != null) {
            return this.mSessionDescription;
        }
        throw new IllegalStateException("You need to call configure() first !");
    }

    @SuppressLint({"InlinedApi"})
    private void testADTS() throws IllegalStateException, IOException {
        setAudioEncoder(3);
        try {
            setOutputFormat(OutputFormat.class.getField("AAC_ADTS").getInt(null));
        } catch (Exception e) {
            setOutputFormat(6);
        }
        String key = "libstreaming-aac-" + this.mQuality.samplingRate;
        if (this.mSettings == null || !this.mSettings.contains(key)) {
            String TESTFILE = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())).append("/spydroid-test.adts").toString();
            if (Environment.getExternalStorageState().equals("mounted")) {
                byte[] buffer = new byte[9];
                this.mMediaRecorder = new MediaRecorder();
                this.mMediaRecorder.setAudioSource(this.mAudioSource);
                this.mMediaRecorder.setOutputFormat(this.mOutputFormat);
                this.mMediaRecorder.setAudioEncoder(this.mAudioEncoder);
                this.mMediaRecorder.setAudioChannels(1);
                this.mMediaRecorder.setAudioSamplingRate(this.mQuality.samplingRate);
                this.mMediaRecorder.setAudioEncodingBitRate(this.mQuality.bitRate);
                this.mMediaRecorder.setOutputFile(TESTFILE);
                this.mMediaRecorder.setMaxDuration(1000);
                this.mMediaRecorder.prepare();
                this.mMediaRecorder.start();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e2) {
                }
                this.mMediaRecorder.stop();
                this.mMediaRecorder.release();
                this.mMediaRecorder = null;
                File file = new File(TESTFILE);
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                while (true) {
                    if ((raf.readByte() & 255) == 255) {
                        buffer[0] = raf.readByte();
                        if ((buffer[0] & 240) == 240) {
                            break;
                        }
                    }
                }
                raf.read(buffer, 1, 5);
                this.mSamplingRateIndex = (buffer[1] & 60) >> 2;
                this.mProfile = ((buffer[1] & Wbxml.EXT_0) >> 6) + 1;
                this.mChannel = ((buffer[1] & 1) << 2) | ((buffer[2] & Wbxml.EXT_0) >> 6);
                this.mQuality.samplingRate = AUDIO_SAMPLING_RATES[this.mSamplingRateIndex];
                this.mConfig = (((this.mProfile & 31) << 11) | ((this.mSamplingRateIndex & 15) << 7)) | ((this.mChannel & 15) << 3);
                Log.i(TAG, "MPEG VERSION: " + ((buffer[0] & 8) >> 3));
                Log.i(TAG, "PROTECTION: " + (buffer[0] & 1));
                Log.i(TAG, "PROFILE: " + AUDIO_OBJECT_TYPES[this.mProfile]);
                Log.i(TAG, "SAMPLING FREQUENCY: " + this.mQuality.samplingRate);
                Log.i(TAG, "CHANNEL: " + this.mChannel);
                raf.close();
                if (this.mSettings != null) {
                    Editor editor = this.mSettings.edit();
                    editor.putString(key, new StringBuilder(String.valueOf(this.mQuality.samplingRate)).append(",").append(this.mConfig).append(",").append(this.mChannel).toString());
                    editor.commit();
                }
                if (!file.delete()) {
                    Log.e(TAG, "Temp file could not be erased");
                    return;
                }
                return;
            }
            throw new IllegalStateException("No external storage or external storage not ready !");
        }
        String[] s = this.mSettings.getString(key, "").split(",");
        this.mQuality.samplingRate = Integer.valueOf(s[0]).intValue();
        this.mConfig = Integer.valueOf(s[1]).intValue();
        this.mChannel = Integer.valueOf(s[2]).intValue();
    }
}
