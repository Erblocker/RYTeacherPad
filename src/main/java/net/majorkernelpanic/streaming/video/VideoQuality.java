package net.majorkernelpanic.streaming.video;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import java.util.Iterator;

public class VideoQuality {
    public static final VideoQuality DEFAULT_VIDEO_QUALITY = new VideoQuality(176, 144, 20, 500000);
    public static final String TAG = "VideoQuality";
    public int bitrate = 0;
    public int framerate = 0;
    public int resX = 0;
    public int resY = 0;

    public VideoQuality(int resX, int resY) {
        this.resX = resX;
        this.resY = resY;
    }

    public VideoQuality(int resX, int resY, int framerate, int bitrate) {
        this.framerate = framerate;
        this.bitrate = bitrate;
        this.resX = resX;
        this.resY = resY;
    }

    public boolean equals(VideoQuality quality) {
        int i = 1;
        if (quality == null) {
            return false;
        }
        int i2;
        int i3 = quality.resX == this.resX ? 1 : 0;
        if (quality.resY == this.resY) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        i2 &= i3;
        if (quality.framerate == this.framerate) {
            i3 = 1;
        } else {
            i3 = 0;
        }
        i3 &= i2;
        if (quality.bitrate != this.bitrate) {
            i = 0;
        }
        return i3 & i;
    }

    public VideoQuality clone() {
        return new VideoQuality(this.resX, this.resY, this.framerate, this.bitrate);
    }

    public static VideoQuality parseQuality(String str) {
        VideoQuality quality = DEFAULT_VIDEO_QUALITY.clone();
        if (str != null) {
            String[] config = str.split("-");
            try {
                quality.bitrate = Integer.parseInt(config[0]) * 1000;
                quality.framerate = Integer.parseInt(config[1]);
                quality.resX = Integer.parseInt(config[2]);
                quality.resY = Integer.parseInt(config[3]);
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return quality;
    }

    public String toString() {
        return this.resX + "x" + this.resY + " px, " + this.framerate + " fps, " + (this.bitrate / 1000) + " kbps";
    }

    public static VideoQuality determineClosestSupportedResolution(Parameters parameters, VideoQuality quality) {
        VideoQuality v = quality.clone();
        int minDist = Integer.MAX_VALUE;
        String supportedSizesStr = "Supported resolutions: ";
        Iterator<Size> it = parameters.getSupportedPreviewSizes().iterator();
        while (it.hasNext()) {
            Size size = (Size) it.next();
            supportedSizesStr = new StringBuilder(String.valueOf(supportedSizesStr)).append(size.width).append("x").append(size.height).append(it.hasNext() ? ", " : "").toString();
            int dist = Math.abs(quality.resX - size.width);
            if (dist < minDist) {
                minDist = dist;
                v.resX = size.width;
                v.resY = size.height;
            }
        }
        Log.v(TAG, supportedSizesStr);
        if (!(quality.resX == v.resX && quality.resY == v.resY)) {
            Log.v(TAG, "Resolution modified: " + quality.resX + "x" + quality.resY + "->" + v.resX + "x" + v.resY);
        }
        return v;
    }

    public static int[] determineMaximumSupportedFramerate(Parameters parameters) {
        int[] maxFps = new int[2];
        String supportedFpsRangesStr = "Supported frame rates: ";
        Iterator<int[]> it = parameters.getSupportedPreviewFpsRange().iterator();
        while (it.hasNext()) {
            int[] interval = (int[]) it.next();
            supportedFpsRangesStr = new StringBuilder(String.valueOf(supportedFpsRangesStr)).append(interval[0] / 1000).append("-").append(interval[1] / 1000).append("fps").append(it.hasNext() ? ", " : "").toString();
            if (interval[1] > maxFps[1] || (interval[0] > maxFps[0] && interval[1] == maxFps[1])) {
                maxFps = interval;
            }
        }
        Log.v(TAG, supportedFpsRangesStr);
        return maxFps;
    }
}
