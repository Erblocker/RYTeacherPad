package javazoom.jl.decoder;

public class Decoder implements DecoderErrors {
    private static final Params DEFAULT_PARAMS = new Params();
    private Equalizer equalizer;
    private SynthesisFilter filter1;
    private SynthesisFilter filter2;
    private boolean initialized;
    private LayerIDecoder l1decoder;
    private LayerIIDecoder l2decoder;
    private LayerIIIDecoder l3decoder;
    private Obuffer output;
    private int outputChannels;
    private int outputFrequency;
    private Params params;

    public static class Params implements Cloneable {
        private Equalizer equalizer = new Equalizer();
        private OutputChannels outputChannels = OutputChannels.BOTH;

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException ex) {
                throw new InternalError(this + ": " + ex);
            }
        }

        public void setOutputChannels(OutputChannels out) {
            if (out == null) {
                throw new NullPointerException("out");
            }
            this.outputChannels = out;
        }

        public OutputChannels getOutputChannels() {
            return this.outputChannels;
        }

        public Equalizer getInitialEqualizerSettings() {
            return this.equalizer;
        }
    }

    public Decoder() {
        this(null);
    }

    public Decoder(Params params0) {
        this.equalizer = new Equalizer();
        if (params0 == null) {
            params0 = DEFAULT_PARAMS;
        }
        this.params = params0;
        Equalizer eq = this.params.getInitialEqualizerSettings();
        if (eq != null) {
            this.equalizer.setFrom(eq);
        }
    }

    public static Params getDefaultParams() {
        return (Params) DEFAULT_PARAMS.clone();
    }

    public void setEqualizer(Equalizer eq) {
        if (eq == null) {
            eq = Equalizer.PASS_THRU_EQ;
        }
        this.equalizer.setFrom(eq);
        float[] factors = this.equalizer.getBandFactors();
        if (this.filter1 != null) {
            this.filter1.setEQ(factors);
        }
        if (this.filter2 != null) {
            this.filter2.setEQ(factors);
        }
    }

    public Obuffer decodeFrame(Header header, Bitstream stream) throws DecoderException {
        if (!this.initialized) {
            initialize(header);
        }
        int layer = header.layer();
        this.output.clear_buffer();
        retrieveDecoder(header, stream, layer).decodeFrame();
        this.output.write_buffer(1);
        return this.output;
    }

    public void setOutputBuffer(Obuffer out) {
        this.output = out;
    }

    public int getOutputFrequency() {
        return this.outputFrequency;
    }

    public int getOutputChannels() {
        return this.outputChannels;
    }

    public int getOutputBlockSize() {
        return Obuffer.OBUFFERSIZE;
    }

    protected DecoderException newDecoderException(int errorcode) {
        return new DecoderException(errorcode, null);
    }

    protected DecoderException newDecoderException(int errorcode, Throwable throwable) {
        return new DecoderException(errorcode, throwable);
    }

    protected FrameDecoder retrieveDecoder(Header header, Bitstream stream, int layer) throws DecoderException {
        FrameDecoder decoder = null;
        switch (layer) {
            case 1:
                if (this.l1decoder == null) {
                    this.l1decoder = new LayerIDecoder();
                    this.l1decoder.create(stream, header, this.filter1, this.filter2, this.output, 0);
                }
                decoder = this.l1decoder;
                break;
            case 2:
                if (this.l2decoder == null) {
                    this.l2decoder = new LayerIIDecoder();
                    this.l2decoder.create(stream, header, this.filter1, this.filter2, this.output, 0);
                }
                decoder = this.l2decoder;
                break;
            case 3:
                if (this.l3decoder == null) {
                    this.l3decoder = new LayerIIIDecoder(stream, header, this.filter1, this.filter2, this.output, 0);
                }
                decoder = this.l3decoder;
                break;
        }
        if (decoder != null) {
            return decoder;
        }
        throw newDecoderException(513, null);
    }

    private void initialize(Header header) throws DecoderException {
        int channels;
        int mode = header.mode();
        int layer = header.layer();
        if (mode == 3) {
            channels = 1;
        } else {
            channels = 2;
        }
        if (this.output == null) {
            this.output = new SampleBuffer(header.frequency(), channels);
        }
        float[] factors = this.equalizer.getBandFactors();
        this.filter1 = new SynthesisFilter(0, 32700.0f, factors);
        if (channels == 2) {
            this.filter2 = new SynthesisFilter(1, 32700.0f, factors);
        }
        this.outputChannels = channels;
        this.outputFrequency = header.frequency();
        this.initialized = true;
    }
}
