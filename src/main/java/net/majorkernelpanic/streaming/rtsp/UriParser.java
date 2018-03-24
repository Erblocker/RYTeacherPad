package net.majorkernelpanic.streaming.rtsp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.video.VideoQuality;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HTTP;

public class UriParser {
    public static final String TAG = "UriParser";

    public static Session parse(String uri) throws IllegalStateException, IOException {
        SessionBuilder builder = SessionBuilder.getInstance().clone();
        byte audioApi = (byte) 0;
        byte videoApi = (byte) 0;
        List<NameValuePair> params = URLEncodedUtils.parse(URI.create(uri), HTTP.UTF_8);
        if (params.size() > 0) {
            builder.setAudioEncoder(0).setVideoEncoder(0);
            for (NameValuePair param : params) {
                if (param.getName().equalsIgnoreCase("flash")) {
                    if (param.getValue().equalsIgnoreCase("on")) {
                        builder.setFlashEnabled(true);
                    } else {
                        builder.setFlashEnabled(false);
                    }
                } else if (param.getName().equalsIgnoreCase("camera")) {
                    if (param.getValue().equalsIgnoreCase("back")) {
                        builder.setCamera(0);
                    } else if (param.getValue().equalsIgnoreCase("front")) {
                        builder.setCamera(1);
                    }
                } else if (param.getName().equalsIgnoreCase("multicast")) {
                    if (param.getValue() == null) {
                        builder.setDestination("228.5.6.7");
                    } else if (InetAddress.getByName(param.getValue()).isMulticastAddress()) {
                        try {
                            builder.setDestination(param.getValue());
                        } catch (UnknownHostException e) {
                            throw new IllegalStateException("Invalid multicast address !");
                        }
                    } else {
                        throw new IllegalStateException("Invalid multicast address !");
                    }
                } else if (param.getName().equalsIgnoreCase("unicast")) {
                    if (param.getValue() != null) {
                        builder.setDestination(param.getValue());
                    }
                } else if (param.getName().equalsIgnoreCase("videoapi")) {
                    if (param.getValue() != null) {
                        if (param.getValue().equalsIgnoreCase("mr")) {
                            videoApi = (byte) 1;
                        } else if (param.getValue().equalsIgnoreCase("mc")) {
                            videoApi = (byte) 2;
                        }
                    }
                } else if (param.getName().equalsIgnoreCase("audioapi")) {
                    if (param.getValue() != null) {
                        if (param.getValue().equalsIgnoreCase("mr")) {
                            audioApi = (byte) 1;
                        } else if (param.getValue().equalsIgnoreCase("mc")) {
                            audioApi = (byte) 2;
                        }
                    }
                } else if (param.getName().equalsIgnoreCase("ttl")) {
                    if (param.getValue() != null) {
                        int ttl = Integer.parseInt(param.getValue());
                        if (ttl < 0) {
                            throw new IllegalStateException();
                        }
                        try {
                            builder.setTimeToLive(ttl);
                        } catch (Exception e2) {
                            throw new IllegalStateException("The TTL must be a positive integer !");
                        }
                    }
                    continue;
                } else if (param.getName().equalsIgnoreCase("h264")) {
                    builder.setVideoQuality(VideoQuality.parseQuality(param.getValue())).setVideoEncoder(1);
                } else if (param.getName().equalsIgnoreCase("h263")) {
                    builder.setVideoQuality(VideoQuality.parseQuality(param.getValue())).setVideoEncoder(2);
                } else if (param.getName().equalsIgnoreCase("amrnb") || param.getName().equalsIgnoreCase("amr")) {
                    builder.setAudioQuality(AudioQuality.parseQuality(param.getValue())).setAudioEncoder(3);
                } else if (param.getName().equalsIgnoreCase("aac")) {
                    builder.setAudioQuality(AudioQuality.parseQuality(param.getValue())).setAudioEncoder(5);
                }
            }
        }
        if (builder.getVideoEncoder() == 0 && builder.getAudioEncoder() == 0) {
            SessionBuilder b = SessionBuilder.getInstance();
            builder.setVideoEncoder(b.getVideoEncoder());
            builder.setAudioEncoder(b.getAudioEncoder());
        }
        Session session = builder.build();
        if (videoApi > (byte) 0 && session.getVideoTrack() != null) {
            session.getVideoTrack().setStreamingMethod(videoApi);
        }
        if (audioApi > (byte) 0 && session.getAudioTrack() != null) {
            session.getAudioTrack().setStreamingMethod(audioApi);
        }
        return session;
    }
}
