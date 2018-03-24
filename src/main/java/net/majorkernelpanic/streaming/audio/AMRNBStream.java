package net.majorkernelpanic.streaming.audio;

import android.media.MediaRecorder.OutputFormat;
import java.io.IOException;
import net.majorkernelpanic.streaming.rtp.AMRNBPacketizer;

public class AMRNBStream extends AudioStream {
    public AMRNBStream() {
        this.mPacketizer = new AMRNBPacketizer();
        setAudioSource(5);
        try {
            setOutputFormat(OutputFormat.class.getField("RAW_AMR").getInt(null));
        } catch (Exception e) {
            setOutputFormat(3);
        }
        setAudioEncoder(1);
    }

    public synchronized void start() throws IllegalStateException, IOException {
        if (!this.mStreaming) {
            configure();
            super.start();
        }
    }

    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();
        this.mMode = (byte) 1;
        this.mQuality = this.mRequestedQuality.clone();
    }

    public String getSessionDescription() {
        return "m=audio " + String.valueOf(getDestinationPorts()[0]) + " RTP/AVP 96\r\n" + "a=rtpmap:96 AMR/8000\r\n" + "a=fmtp:96 octet-align=1;\r\n";
    }

    protected void encodeWithMediaCodec() throws IOException {
        super.encodeWithMediaRecorder();
    }
}
