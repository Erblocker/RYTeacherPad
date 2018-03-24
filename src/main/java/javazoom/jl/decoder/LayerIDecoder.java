package javazoom.jl.decoder;

class LayerIDecoder implements FrameDecoder {
    protected Obuffer buffer;
    protected Crc16 crc;
    protected SynthesisFilter filter1;
    protected SynthesisFilter filter2;
    protected Header header;
    protected int mode;
    protected int num_subbands;
    protected Bitstream stream;
    protected Subband[] subbands;
    protected int which_channels;

    static abstract class Subband {
        public static final float[] scalefactors = new float[]{2.0f, 1.587401f, 1.2599211f, 1.0f, 0.7937005f, 0.62996054f, 0.5f, 0.39685026f, 0.31498027f, 0.25f, 0.19842513f, 0.15749013f, 0.125f, 0.099212565f, 0.07874507f, 0.0625f, 0.049606282f, 0.039372534f, 0.03125f, 0.024803141f, 0.019686267f, 0.015625f, 0.012401571f, 0.009843133f, 0.0078125f, 0.0062007853f, 0.0049215667f, 0.00390625f, 0.0031003926f, 0.0024607833f, 0.001953125f, 0.0015501963f, 0.0012303917f, 9.765625E-4f, 7.7509816E-4f, 6.1519584E-4f, 4.8828125E-4f, 3.8754908E-4f, 3.0759792E-4f, 2.4414062E-4f, 1.9377454E-4f, 1.5379896E-4f, 1.2207031E-4f, 9.688727E-5f, 7.689948E-5f, 6.1035156E-5f, 4.8443635E-5f, 3.844974E-5f, 3.0517578E-5f, 2.4221818E-5f, 1.922487E-5f, 1.5258789E-5f, 1.2110909E-5f, 9.612435E-6f, 7.6293945E-6f, 6.0554544E-6f, 4.8062175E-6f, 3.8146973E-6f, 3.0277272E-6f, 2.4031087E-6f, 1.9073486E-6f, 1.5138636E-6f, 1.2015544E-6f, 0.0f};

        public abstract boolean put_next_sample(int i, SynthesisFilter synthesisFilter, SynthesisFilter synthesisFilter2);

        public abstract void read_allocation(Bitstream bitstream, Header header, Crc16 crc16) throws DecoderException;

        public abstract boolean read_sampledata(Bitstream bitstream);

        public abstract void read_scalefactor(Bitstream bitstream, Header header);

        Subband() {
        }
    }

    static class SubbandLayer1 extends Subband {
        public static final float[] table_factor = new float[]{0.0f, 0.6666667f, 0.2857143f, 0.13333334f, 0.06451613f, 0.031746034f, 0.015748031f, 0.007843138f, 0.0039138943f, 0.0019550342f, 9.770396E-4f, 4.884005E-4f, 2.4417043E-4f, 1.2207776E-4f, 6.103702E-5f};
        public static final float[] table_offset = new float[]{0.0f, -0.6666667f, -0.8571429f, -0.9333334f, -0.9677419f, -0.98412704f, -0.992126f, -0.9960785f, -0.99804306f, -0.9990225f, -0.9995115f, -0.99975586f, -0.9998779f, -0.99993896f, -0.9999695f};
        protected int allocation;
        protected float factor;
        protected float offset;
        protected float sample;
        protected int samplelength;
        protected int samplenumber = 0;
        protected float scalefactor;
        protected int subbandnumber;

        public SubbandLayer1(int subbandnumber) {
            this.subbandnumber = subbandnumber;
        }

        public void read_allocation(Bitstream stream, Header header, Crc16 crc) throws DecoderException {
            int i = stream.get_bits(4);
            this.allocation = i;
            if (i == 15) {
                throw new DecoderException((int) DecoderErrors.ILLEGAL_SUBBAND_ALLOCATION, null);
            }
            if (crc != null) {
                crc.add_bits(this.allocation, 4);
            }
            if (this.allocation != 0) {
                this.samplelength = this.allocation + 1;
                this.factor = table_factor[this.allocation];
                this.offset = table_offset[this.allocation];
            }
        }

        public void read_scalefactor(Bitstream stream, Header header) {
            if (this.allocation != 0) {
                this.scalefactor = scalefactors[stream.get_bits(6)];
            }
        }

        public boolean read_sampledata(Bitstream stream) {
            if (this.allocation != 0) {
                this.sample = (float) stream.get_bits(this.samplelength);
            }
            int i = this.samplenumber + 1;
            this.samplenumber = i;
            if (i != 12) {
                return false;
            }
            this.samplenumber = 0;
            return true;
        }

        public boolean put_next_sample(int channels, SynthesisFilter filter1, SynthesisFilter filter2) {
            if (!(this.allocation == 0 || channels == 2)) {
                filter1.input_sample(((this.sample * this.factor) + this.offset) * this.scalefactor, this.subbandnumber);
            }
            return true;
        }
    }

    static class SubbandLayer1IntensityStereo extends SubbandLayer1 {
        protected float channel2_scalefactor;

        public SubbandLayer1IntensityStereo(int subbandnumber) {
            super(subbandnumber);
        }

        public void read_allocation(Bitstream stream, Header header, Crc16 crc) throws DecoderException {
            super.read_allocation(stream, header, crc);
        }

        public void read_scalefactor(Bitstream stream, Header header) {
            if (this.allocation != 0) {
                this.scalefactor = scalefactors[stream.get_bits(6)];
                this.channel2_scalefactor = scalefactors[stream.get_bits(6)];
            }
        }

        public boolean read_sampledata(Bitstream stream) {
            return super.read_sampledata(stream);
        }

        public boolean put_next_sample(int channels, SynthesisFilter filter1, SynthesisFilter filter2) {
            if (this.allocation != 0) {
                this.sample = (this.sample * this.factor) + this.offset;
                if (channels == 0) {
                    float sample2 = this.sample * this.channel2_scalefactor;
                    filter1.input_sample(this.sample * this.scalefactor, this.subbandnumber);
                    filter2.input_sample(sample2, this.subbandnumber);
                } else if (channels == 1) {
                    filter1.input_sample(this.sample * this.scalefactor, this.subbandnumber);
                } else {
                    filter1.input_sample(this.sample * this.channel2_scalefactor, this.subbandnumber);
                }
            }
            return true;
        }
    }

    static class SubbandLayer1Stereo extends SubbandLayer1 {
        protected int channel2_allocation;
        protected float channel2_factor;
        protected float channel2_offset;
        protected float channel2_sample;
        protected int channel2_samplelength;
        protected float channel2_scalefactor;

        public SubbandLayer1Stereo(int subbandnumber) {
            super(subbandnumber);
        }

        public void read_allocation(Bitstream stream, Header header, Crc16 crc) throws DecoderException {
            this.allocation = stream.get_bits(4);
            this.channel2_allocation = stream.get_bits(4);
            if (crc != null) {
                crc.add_bits(this.allocation, 4);
                crc.add_bits(this.channel2_allocation, 4);
            }
            if (this.allocation != 0) {
                this.samplelength = this.allocation + 1;
                this.factor = table_factor[this.allocation];
                this.offset = table_offset[this.allocation];
            }
            if (this.channel2_allocation != 0) {
                this.channel2_samplelength = this.channel2_allocation + 1;
                this.channel2_factor = table_factor[this.channel2_allocation];
                this.channel2_offset = table_offset[this.channel2_allocation];
            }
        }

        public void read_scalefactor(Bitstream stream, Header header) {
            if (this.allocation != 0) {
                this.scalefactor = scalefactors[stream.get_bits(6)];
            }
            if (this.channel2_allocation != 0) {
                this.channel2_scalefactor = scalefactors[stream.get_bits(6)];
            }
        }

        public boolean read_sampledata(Bitstream stream) {
            boolean returnvalue = super.read_sampledata(stream);
            if (this.channel2_allocation != 0) {
                this.channel2_sample = (float) stream.get_bits(this.channel2_samplelength);
            }
            return returnvalue;
        }

        public boolean put_next_sample(int channels, SynthesisFilter filter1, SynthesisFilter filter2) {
            super.put_next_sample(channels, filter1, filter2);
            if (!(this.channel2_allocation == 0 || channels == 1)) {
                float sample2 = ((this.channel2_sample * this.channel2_factor) + this.channel2_offset) * this.channel2_scalefactor;
                if (channels == 0) {
                    filter2.input_sample(sample2, this.subbandnumber);
                } else {
                    filter1.input_sample(sample2, this.subbandnumber);
                }
            }
            return true;
        }
    }

    public LayerIDecoder() {
        this.crc = null;
        this.crc = new Crc16();
    }

    public void create(Bitstream stream0, Header header0, SynthesisFilter filtera, SynthesisFilter filterb, Obuffer buffer0, int which_ch0) {
        this.stream = stream0;
        this.header = header0;
        this.filter1 = filtera;
        this.filter2 = filterb;
        this.buffer = buffer0;
        this.which_channels = which_ch0;
    }

    public void decodeFrame() throws DecoderException {
        this.num_subbands = this.header.number_of_subbands();
        this.subbands = new Subband[32];
        this.mode = this.header.mode();
        createSubbands();
        readAllocation();
        readScaleFactorSelection();
        if (this.crc != null || this.header.checksum_ok()) {
            readScaleFactors();
            readSampleData();
        }
    }

    protected void createSubbands() {
        int i;
        if (this.mode == 3) {
            for (i = 0; i < this.num_subbands; i++) {
                this.subbands[i] = new SubbandLayer1(i);
            }
        } else if (this.mode == 1) {
            i = 0;
            while (i < this.header.intensity_stereo_bound()) {
                this.subbands[i] = new SubbandLayer1Stereo(i);
                i++;
            }
            while (i < this.num_subbands) {
                this.subbands[i] = new SubbandLayer1IntensityStereo(i);
                i++;
            }
        } else {
            for (i = 0; i < this.num_subbands; i++) {
                this.subbands[i] = new SubbandLayer1Stereo(i);
            }
        }
    }

    protected void readAllocation() throws DecoderException {
        for (int i = 0; i < this.num_subbands; i++) {
            this.subbands[i].read_allocation(this.stream, this.header, this.crc);
        }
    }

    protected void readScaleFactorSelection() {
    }

    protected void readScaleFactors() {
        for (int i = 0; i < this.num_subbands; i++) {
            this.subbands[i].read_scalefactor(this.stream, this.header);
        }
    }

    protected void readSampleData() {
        boolean read_ready = false;
        boolean write_ready = false;
        int mode = this.header.mode();
        do {
            int i;
            for (i = 0; i < this.num_subbands; i++) {
                read_ready = this.subbands[i].read_sampledata(this.stream);
            }
            do {
                for (i = 0; i < this.num_subbands; i++) {
                    write_ready = this.subbands[i].put_next_sample(this.which_channels, this.filter1, this.filter2);
                }
                this.filter1.calculate_pcm_samples(this.buffer);
                if (this.which_channels == 0 && mode != 3) {
                    this.filter2.calculate_pcm_samples(this.buffer);
                    continue;
                }
            } while (!write_ready);
        } while (!read_ready);
    }
}
