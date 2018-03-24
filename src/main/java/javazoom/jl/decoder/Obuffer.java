package javazoom.jl.decoder;

public abstract class Obuffer {
    public static final int MAXCHANNELS = 2;
    public static final int OBUFFERSIZE = 2304;

    public abstract void append(int i, short s);

    public abstract void clear_buffer();

    public abstract void close();

    public abstract void set_stop_flag();

    public abstract void write_buffer(int i);

    public void appendSamples(int channel, float[] f) {
        int i = 0;
        while (i < 32) {
            int i2 = i + 1;
            append(channel, clip(f[i]));
            i = i2;
        }
    }

    private final short clip(float sample) {
        if (sample > 32767.0f) {
            return Short.MAX_VALUE;
        }
        if (sample < -32768.0f) {
            return Short.MIN_VALUE;
        }
        return (short) ((int) sample);
    }
}
