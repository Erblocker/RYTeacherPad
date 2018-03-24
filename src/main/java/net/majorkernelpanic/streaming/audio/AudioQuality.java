package net.majorkernelpanic.streaming.audio;

public class AudioQuality {
    public static final AudioQuality DEFAULT_AUDIO_QUALITY = new AudioQuality(8000, 32000);
    public int bitRate = 0;
    public int samplingRate = 0;

    public AudioQuality(int samplingRate, int bitRate) {
        this.samplingRate = samplingRate;
        this.bitRate = bitRate;
    }

    public boolean equals(AudioQuality quality) {
        int i = 1;
        if (quality == null) {
            return false;
        }
        int i2 = quality.samplingRate == this.samplingRate ? 1 : 0;
        if (quality.bitRate != this.bitRate) {
            i = 0;
        }
        return i2 & i;
    }

    public AudioQuality clone() {
        return new AudioQuality(this.samplingRate, this.bitRate);
    }

    public static AudioQuality parseQuality(String str) {
        AudioQuality quality = DEFAULT_AUDIO_QUALITY.clone();
        if (str != null) {
            String[] config = str.split("-");
            try {
                quality.bitRate = Integer.parseInt(config[0]) * 1000;
                quality.samplingRate = Integer.parseInt(config[1]);
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return quality;
    }
}
