package javazoom.jl.decoder;

public class SampleBuffer extends Obuffer {
    private short[] buffer = new short[Obuffer.OBUFFERSIZE];
    private int[] bufferp = new int[2];
    private int channels;
    private int frequency;

    public SampleBuffer(int sample_frequency, int number_of_channels) {
        this.channels = number_of_channels;
        this.frequency = sample_frequency;
        for (int i = 0; i < number_of_channels; i++) {
            this.bufferp[i] = (short) i;
        }
    }

    public int getChannelCount() {
        return this.channels;
    }

    public int getSampleFrequency() {
        return this.frequency;
    }

    public short[] getBuffer() {
        return this.buffer;
    }

    public int getBufferLength() {
        return this.bufferp[0];
    }

    public void append(int channel, short value) {
        this.buffer[this.bufferp[channel]] = value;
        int[] iArr = this.bufferp;
        iArr[channel] = iArr[channel] + this.channels;
    }

    public void appendSamples(int channel, float[] f) {
        int pos = this.bufferp[channel];
        int i = 0;
        while (i < 32) {
            int i2 = i + 1;
            float fs = f[i];
            if (fs > 32767.0f) {
                fs = 32767.0f;
            } else if (fs < -32767.0f) {
                fs = -32767.0f;
            }
            this.buffer[pos] = (short) ((int) fs);
            pos += this.channels;
            i = i2;
        }
        this.bufferp[channel] = pos;
    }

    public void write_buffer(int val) {
    }

    public void close() {
    }

    public void clear_buffer() {
        for (int i = 0; i < this.channels; i++) {
            this.bufferp[i] = (short) i;
        }
    }

    public void set_stop_flag() {
    }
}
