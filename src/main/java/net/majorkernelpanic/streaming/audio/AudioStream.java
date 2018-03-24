package net.majorkernelpanic.streaming.audio;

import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import net.majorkernelpanic.streaming.MediaStream;

public abstract class AudioStream extends MediaStream {
    protected int mAudioEncoder;
    protected int mAudioSource;
    protected int mOutputFormat;
    protected AudioQuality mQuality = this.mRequestedQuality.clone();
    protected AudioQuality mRequestedQuality = AudioQuality.DEFAULT_AUDIO_QUALITY.clone();

    public AudioStream() {
        setAudioSource(5);
    }

    public void setAudioSource(int audioSource) {
        this.mAudioSource = audioSource;
    }

    public void setAudioQuality(AudioQuality quality) {
        this.mRequestedQuality = quality;
    }

    public AudioQuality getAudioQuality() {
        return this.mQuality;
    }

    protected void setAudioEncoder(int audioEncoder) {
        this.mAudioEncoder = audioEncoder;
    }

    protected void setOutputFormat(int outputFormat) {
        this.mOutputFormat = outputFormat;
    }

    protected void encodeWithMediaRecorder() throws IOException {
        FileDescriptor fd;
        InputStream is;
        createSockets();
        Log.v("MediaStream", "Requested audio with " + (this.mQuality.bitRate / 1000) + "kbps" + " at " + (this.mQuality.samplingRate / 1000) + "kHz");
        this.mMediaRecorder = new MediaRecorder();
        this.mMediaRecorder.setAudioSource(this.mAudioSource);
        this.mMediaRecorder.setOutputFormat(this.mOutputFormat);
        this.mMediaRecorder.setAudioEncoder(this.mAudioEncoder);
        this.mMediaRecorder.setAudioChannels(1);
        this.mMediaRecorder.setAudioSamplingRate(this.mQuality.samplingRate);
        this.mMediaRecorder.setAudioEncodingBitRate(this.mQuality.bitRate);
        if (sPipeApi == (byte) 2) {
            fd = this.mParcelWrite.getFileDescriptor();
        } else {
            fd = this.mSender.getFileDescriptor();
        }
        this.mMediaRecorder.setOutputFile(fd);
        this.mMediaRecorder.setOutputFile(fd);
        this.mMediaRecorder.prepare();
        this.mMediaRecorder.start();
        if (sPipeApi == (byte) 2) {
            is = new AutoCloseInputStream(this.mParcelRead);
        } else {
            try {
                is = this.mReceiver.getInputStream();
            } catch (IOException e) {
                stop();
                throw new IOException("Something happened with the local sockets :/ Start failed !");
            }
        }
        this.mPacketizer.setInputStream(is);
        this.mPacketizer.start();
        this.mStreaming = true;
    }
}
