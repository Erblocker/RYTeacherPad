package net.majorkernelpanic.streaming.mp4;

import android.util.Base64;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MP4Config {
    public static final String TAG = "MP4Config";
    private String mPPS;
    private String mProfilLevel;
    private String mSPS;
    private MP4Parser mp4Parser;

    public MP4Config(String profil, String sps, String pps) {
        this.mProfilLevel = profil;
        this.mPPS = pps;
        this.mSPS = sps;
    }

    public MP4Config(String sps, String pps) {
        this.mPPS = pps;
        this.mSPS = sps;
        this.mProfilLevel = MP4Parser.toHexString(Base64.decode(sps, 2), 1, 3);
    }

    public MP4Config(byte[] sps, byte[] pps) {
        this.mPPS = Base64.encodeToString(pps, 0, pps.length, 2);
        this.mSPS = Base64.encodeToString(sps, 0, sps.length, 2);
        this.mProfilLevel = MP4Parser.toHexString(sps, 1, 3);
    }

    public MP4Config(String path) throws IOException, FileNotFoundException {
        try {
            this.mp4Parser = MP4Parser.parse(path);
        } catch (IOException e) {
        }
        StsdBox stsdBox = this.mp4Parser.getStsdBox();
        this.mPPS = stsdBox.getB64PPS();
        this.mSPS = stsdBox.getB64SPS();
        this.mProfilLevel = stsdBox.getProfileLevel();
        this.mp4Parser.close();
    }

    public String getProfileLevel() {
        return this.mProfilLevel;
    }

    public String getB64PPS() {
        Log.d(TAG, "PPS: " + this.mPPS);
        return this.mPPS;
    }

    public String getB64SPS() {
        Log.d(TAG, "SPS: " + this.mSPS);
        return this.mSPS;
    }
}
