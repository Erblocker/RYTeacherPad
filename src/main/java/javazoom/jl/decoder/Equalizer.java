package javazoom.jl.decoder;

public final class Equalizer {
    private static final int BANDS = 32;
    public static final float BAND_NOT_PRESENT = Float.NEGATIVE_INFINITY;
    public static final Equalizer PASS_THRU_EQ = new Equalizer();
    private final float[] settings;

    public static abstract class EQFunction {
        public float getBand(int band) {
            return 0.0f;
        }
    }

    public Equalizer() {
        this.settings = new float[32];
    }

    public Equalizer(float[] settings) {
        this.settings = new float[32];
        setFrom(settings);
    }

    public Equalizer(EQFunction eq) {
        this.settings = new float[32];
        setFrom(eq);
    }

    public void setFrom(float[] eq) {
        int max = 32;
        reset();
        if (eq.length <= 32) {
            max = eq.length;
        }
        for (int i = 0; i < max; i++) {
            this.settings[i] = limit(eq[i]);
        }
    }

    public void setFrom(EQFunction eq) {
        reset();
        for (int i = 0; i < 32; i++) {
            this.settings[i] = limit(eq.getBand(i));
        }
    }

    public void setFrom(Equalizer eq) {
        if (eq != this) {
            setFrom(eq.settings);
        }
    }

    public void reset() {
        for (int i = 0; i < 32; i++) {
            this.settings[i] = 0.0f;
        }
    }

    public int getBandCount() {
        return this.settings.length;
    }

    public float setBand(int band, float neweq) {
        if (band < 0 || band >= 32) {
            return 0.0f;
        }
        float eq = this.settings[band];
        this.settings[band] = limit(neweq);
        return eq;
    }

    public float getBand(int band) {
        if (band < 0 || band >= 32) {
            return 0.0f;
        }
        return this.settings[band];
    }

    private float limit(float eq) {
        if (eq == Float.NEGATIVE_INFINITY) {
            return eq;
        }
        if (eq > 1.0f) {
            return 1.0f;
        }
        if (eq < -1.0f) {
            return -1.0f;
        }
        return eq;
    }

    float[] getBandFactors() {
        float[] factors = new float[32];
        for (int i = 0; i < 32; i++) {
            factors[i] = getBandFactor(this.settings[i]);
        }
        return factors;
    }

    float getBandFactor(float eq) {
        if (eq == Float.NEGATIVE_INFINITY) {
            return 0.0f;
        }
        return (float) Math.pow(2.0d, (double) eq);
    }
}
