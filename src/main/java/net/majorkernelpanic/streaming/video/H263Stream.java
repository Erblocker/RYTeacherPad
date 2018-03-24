package net.majorkernelpanic.streaming.video;

import java.io.IOException;
import net.majorkernelpanic.streaming.rtp.H263Packetizer;

public class H263Stream extends VideoStream {
    public H263Stream() throws IOException {
        this(0);
    }

    public H263Stream(int cameraId) {
        super(cameraId);
        this.mCameraImageFormat = 17;
        this.mVideoEncoder = 1;
        this.mPacketizer = new H263Packetizer();
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
        return "m=video " + String.valueOf(getDestinationPorts()[0]) + " RTP/AVP 96\r\n" + "a=rtpmap:96 H263-1998/90000\r\n";
    }
}
