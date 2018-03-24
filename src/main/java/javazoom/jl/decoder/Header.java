package javazoom.jl.decoder;

import android.support.v4.view.MotionEventCompat;

public final class Header {
    public static final int DUAL_CHANNEL = 2;
    public static final int FOURTYEIGHT = 1;
    public static final int FOURTYFOUR_POINT_ONE = 0;
    public static final int JOINT_STEREO = 1;
    public static final int MPEG1 = 1;
    public static final int MPEG25_LSF = 2;
    public static final int MPEG2_LSF = 0;
    public static final int SINGLE_CHANNEL = 3;
    public static final int STEREO = 0;
    public static final int THIRTYTWO = 2;
    public static final String[][][] bitrate_str;
    public static final int[][][] bitrates;
    public static final int[][] frequencies = new int[][]{new int[]{22050, 24000, 16000, 1}, new int[]{44100, 48000, 32000, 1}, new int[]{11025, 12000, 8000, 1}};
    private int _headerstring = -1;
    public short checksum;
    private Crc16 crc;
    public int framesize;
    private int h_bitrate_index;
    private boolean h_copyright;
    private int h_intensity_stereo_bound;
    private int h_layer;
    private int h_mode;
    private int h_mode_extension;
    private int h_number_of_subbands;
    private boolean h_original;
    private int h_padding_bit;
    private int h_protection_bit;
    private int h_sample_frequency;
    private boolean h_vbr;
    private int h_vbr_bytes;
    private int h_vbr_frames;
    private int h_vbr_scale;
    private double[] h_vbr_time_per_frame = new double[]{-1.0d, 384.0d, 1152.0d, 1152.0d};
    private byte[] h_vbr_toc;
    private int h_version;
    public int nSlots;
    private byte syncmode = Bitstream.INITIAL_SYNC;

    static {
        r0 = new int[3][][];
        r1 = new int[3][];
        int[] iArr = new int[16];
        iArr[1] = 32000;
        iArr[2] = 48000;
        iArr[3] = 56000;
        iArr[4] = 64000;
        iArr[5] = 80000;
        iArr[6] = 96000;
        iArr[7] = 112000;
        iArr[8] = 128000;
        iArr[9] = 144000;
        iArr[10] = 160000;
        iArr[11] = 176000;
        iArr[12] = 192000;
        iArr[13] = 224000;
        iArr[14] = 256000;
        r1[0] = iArr;
        iArr = new int[16];
        iArr[1] = 8000;
        iArr[2] = 16000;
        iArr[3] = 24000;
        iArr[4] = 32000;
        iArr[5] = 40000;
        iArr[6] = 48000;
        iArr[7] = 56000;
        iArr[8] = 64000;
        iArr[9] = 80000;
        iArr[10] = 96000;
        iArr[11] = 112000;
        iArr[12] = 128000;
        iArr[13] = 144000;
        iArr[14] = 160000;
        r1[1] = iArr;
        iArr = new int[]{8000, 16000, 24000, 32000, 40000, 48000, 56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, iArr, r1};
        r1 = new int[3][];
        iArr = new int[16];
        iArr[1] = 32000;
        iArr[2] = 64000;
        iArr[3] = 96000;
        iArr[4] = 128000;
        iArr[5] = 160000;
        iArr[6] = 192000;
        iArr[7] = 224000;
        iArr[8] = 256000;
        iArr[9] = 288000;
        iArr[10] = 320000;
        iArr[11] = 352000;
        iArr[12] = 384000;
        iArr[13] = 416000;
        iArr[14] = 448000;
        r1[0] = iArr;
        iArr = new int[16];
        iArr[1] = 32000;
        iArr[2] = 48000;
        iArr[3] = 56000;
        iArr[4] = 64000;
        iArr[5] = 80000;
        iArr[6] = 96000;
        iArr[7] = 112000;
        iArr[8] = 128000;
        iArr[9] = 160000;
        iArr[10] = 192000;
        iArr[11] = 224000;
        iArr[12] = 256000;
        iArr[13] = 320000;
        iArr[14] = 384000;
        r1[1] = iArr;
        iArr = new int[]{32000, 40000, 48000, 56000, 64000, 80000, 96000, 112000, 128000, 160000, 192000, 224000, 256000, 320000, iArr, r1};
        r1 = new int[3][];
        iArr = new int[16];
        iArr[1] = 32000;
        iArr[2] = 48000;
        iArr[3] = 56000;
        iArr[4] = 64000;
        iArr[5] = 80000;
        iArr[6] = 96000;
        iArr[7] = 112000;
        iArr[8] = 128000;
        iArr[9] = 144000;
        iArr[10] = 160000;
        iArr[11] = 176000;
        iArr[12] = 192000;
        iArr[13] = 224000;
        iArr[14] = 256000;
        r1[0] = iArr;
        iArr = new int[16];
        iArr[1] = 8000;
        iArr[2] = 16000;
        iArr[3] = 24000;
        iArr[4] = 32000;
        iArr[5] = 40000;
        iArr[6] = 48000;
        iArr[7] = 56000;
        iArr[8] = 64000;
        iArr[9] = 80000;
        iArr[10] = 96000;
        iArr[11] = 112000;
        iArr[12] = 128000;
        iArr[13] = 144000;
        iArr[14] = 160000;
        r1[1] = iArr;
        iArr = new int[]{8000, 16000, 24000, 32000, 40000, 48000, 56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, iArr, r1};
        bitrates = r0;
        r0 = new String[3][][];
        r1 = new String[3][];
        r1[0] = new String[]{"free format", "32 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s", "176 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "forbidden"};
        r1[1] = new String[]{"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s", "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s", "forbidden"};
        r1[2] = new String[]{"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s", "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s", "forbidden"};
        r0[0] = r1;
        r1 = new String[3][];
        r1[0] = new String[]{"free format", "32 kbit/s", "64 kbit/s", "96 kbit/s", "128 kbit/s", "160 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "288 kbit/s", "320 kbit/s", "352 kbit/s", "384 kbit/s", "416 kbit/s", "448 kbit/s", "forbidden"};
        r1[1] = new String[]{"free format", "32 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "160 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "320 kbit/s", "384 kbit/s", "forbidden"};
        r1[2] = new String[]{"free format", "32 kbit/s", "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "160 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "320 kbit/s", "forbidden"};
        r0[1] = r1;
        r1 = new String[3][];
        r1[0] = new String[]{"free format", "32 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s", "176 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "forbidden"};
        r1[1] = new String[]{"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s", "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s", "forbidden"};
        r1[2] = new String[]{"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s", "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s", "forbidden"};
        r0[2] = r1;
        bitrate_str = r0;
    }

    Header() {
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(200);
        buffer.append("Layer ");
        buffer.append(layer_string());
        buffer.append(" frame ");
        buffer.append(mode_string());
        buffer.append(' ');
        buffer.append(version_string());
        if (!checksums()) {
            buffer.append(" no");
        }
        buffer.append(" checksums");
        buffer.append(' ');
        buffer.append(sample_frequency_string());
        buffer.append(',');
        buffer.append(' ');
        buffer.append(bitrate_string());
        return buffer.toString();
    }

    void read_header(Bitstream stream, Crc16[] crcp) throws BitstreamException {
        boolean sync = false;
        do {
            int headerstring = stream.syncHeader(this.syncmode);
            this._headerstring = headerstring;
            if (this.syncmode == Bitstream.INITIAL_SYNC) {
                this.h_version = (headerstring >>> 19) & 1;
                if (((headerstring >>> 20) & 1) == 0) {
                    if (this.h_version == 0) {
                        this.h_version = 2;
                    } else {
                        throw stream.newBitstreamException(256);
                    }
                }
                int i = (headerstring >>> 10) & 3;
                this.h_sample_frequency = i;
                if (i == 3) {
                    throw stream.newBitstreamException(256);
                }
            }
            this.h_layer = (4 - (headerstring >>> 17)) & 3;
            this.h_protection_bit = (headerstring >>> 16) & 1;
            this.h_bitrate_index = (headerstring >>> 12) & 15;
            this.h_padding_bit = (headerstring >>> 9) & 1;
            this.h_mode = (headerstring >>> 6) & 3;
            this.h_mode_extension = (headerstring >>> 4) & 3;
            if (this.h_mode == 1) {
                this.h_intensity_stereo_bound = (this.h_mode_extension << 2) + 4;
            } else {
                this.h_intensity_stereo_bound = 0;
            }
            if (((headerstring >>> 3) & 1) == 1) {
                this.h_copyright = true;
            }
            if (((headerstring >>> 2) & 1) == 1) {
                this.h_original = true;
            }
            if (this.h_layer == 1) {
                this.h_number_of_subbands = 32;
            } else {
                int channel_bitrate = this.h_bitrate_index;
                if (this.h_mode != 3) {
                    if (channel_bitrate == 4) {
                        channel_bitrate = 1;
                    } else {
                        channel_bitrate -= 4;
                    }
                }
                if (channel_bitrate == 1 || channel_bitrate == 2) {
                    if (this.h_sample_frequency == 2) {
                        this.h_number_of_subbands = 12;
                    } else {
                        this.h_number_of_subbands = 8;
                    }
                } else if (this.h_sample_frequency == 1 || (channel_bitrate >= 3 && channel_bitrate <= 5)) {
                    this.h_number_of_subbands = 27;
                } else {
                    this.h_number_of_subbands = 30;
                }
            }
            if (this.h_intensity_stereo_bound > this.h_number_of_subbands) {
                this.h_intensity_stereo_bound = this.h_number_of_subbands;
            }
            calculate_framesize();
            int framesizeloaded = stream.read_frame_data(this.framesize);
            if (this.framesize >= 0 && framesizeloaded != this.framesize) {
                throw stream.newBitstreamException(261);
            } else if (stream.isSyncCurrentPosition(this.syncmode)) {
                if (this.syncmode == Bitstream.INITIAL_SYNC) {
                    this.syncmode = Bitstream.STRICT_SYNC;
                    stream.set_syncword(-521024 & headerstring);
                }
                sync = true;
                continue;
            } else {
                stream.unreadFrame();
                continue;
            }
        } while (!sync);
        stream.parse_frame();
        if (this.h_protection_bit == 0) {
            this.checksum = (short) stream.get_bits(16);
            if (this.crc == null) {
                this.crc = new Crc16();
            }
            this.crc.add_bits(headerstring, 16);
            crcp[0] = this.crc;
            return;
        }
        crcp[0] = null;
    }

    void parseVBR(byte[] firstframe) throws BitstreamException {
        int offset;
        String xing = "Xing";
        byte[] tmp = new byte[4];
        if (this.h_version == 1) {
            if (this.h_mode == 3) {
                offset = 17;
            } else {
                offset = 32;
            }
        } else if (this.h_mode == 3) {
            offset = 9;
        } else {
            offset = 17;
        }
        try {
            int length;
            System.arraycopy(firstframe, offset, tmp, 0, 4);
            if (xing.equals(new String(tmp))) {
                this.h_vbr = true;
                this.h_vbr_frames = -1;
                this.h_vbr_bytes = -1;
                this.h_vbr_scale = -1;
                this.h_vbr_toc = new byte[100];
                byte[] flags = new byte[4];
                System.arraycopy(firstframe, offset + 4, flags, 0, flags.length);
                length = 4 + flags.length;
                if ((flags[3] & 1) != 0) {
                    System.arraycopy(firstframe, offset + length, tmp, 0, tmp.length);
                    this.h_vbr_frames = ((((tmp[0] << 24) & -16777216) | ((tmp[1] << 16) & 16711680)) | ((tmp[2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | (tmp[3] & 255);
                    length += 4;
                }
                if ((flags[3] & 2) != 0) {
                    System.arraycopy(firstframe, offset + length, tmp, 0, tmp.length);
                    this.h_vbr_bytes = ((((tmp[0] << 24) & -16777216) | ((tmp[1] << 16) & 16711680)) | ((tmp[2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | (tmp[3] & 255);
                    length += 4;
                }
                if ((flags[3] & 4) != 0) {
                    System.arraycopy(firstframe, offset + length, this.h_vbr_toc, 0, this.h_vbr_toc.length);
                    length += this.h_vbr_toc.length;
                }
                if ((flags[3] & 8) != 0) {
                    System.arraycopy(firstframe, offset + length, tmp, 0, tmp.length);
                    this.h_vbr_scale = ((((tmp[0] << 24) & -16777216) | ((tmp[1] << 16) & 16711680)) | ((tmp[2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | (tmp[3] & 255);
                    length += 4;
                }
            }
            String vbri = "VBRI";
            try {
                System.arraycopy(firstframe, 32, tmp, 0, 4);
                if (vbri.equals(new String(tmp))) {
                    this.h_vbr = true;
                    this.h_vbr_frames = -1;
                    this.h_vbr_bytes = -1;
                    this.h_vbr_scale = -1;
                    this.h_vbr_toc = new byte[100];
                    System.arraycopy(firstframe, 42, tmp, 0, tmp.length);
                    this.h_vbr_bytes = ((((tmp[0] << 24) & -16777216) | ((tmp[1] << 16) & 16711680)) | ((tmp[2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | (tmp[3] & 255);
                    length = 10 + 4;
                    System.arraycopy(firstframe, 46, tmp, 0, tmp.length);
                    this.h_vbr_frames = ((((tmp[0] << 24) & -16777216) | ((tmp[1] << 16) & 16711680)) | ((tmp[2] << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK)) | (tmp[3] & 255);
                    length += 4;
                }
            } catch (Throwable e) {
                throw new BitstreamException("VBRIVBRHeader Corrupted", e);
            }
        } catch (Throwable e2) {
            throw new BitstreamException("XingVBRHeader Corrupted", e2);
        }
    }

    public int version() {
        return this.h_version;
    }

    public int layer() {
        return this.h_layer;
    }

    public int bitrate_index() {
        return this.h_bitrate_index;
    }

    public int sample_frequency() {
        return this.h_sample_frequency;
    }

    public int frequency() {
        return frequencies[this.h_version][this.h_sample_frequency];
    }

    public int mode() {
        return this.h_mode;
    }

    public boolean checksums() {
        if (this.h_protection_bit == 0) {
            return true;
        }
        return false;
    }

    public boolean copyright() {
        return this.h_copyright;
    }

    public boolean original() {
        return this.h_original;
    }

    public boolean vbr() {
        return this.h_vbr;
    }

    public int vbr_scale() {
        return this.h_vbr_scale;
    }

    public byte[] vbr_toc() {
        return this.h_vbr_toc;
    }

    public boolean checksum_ok() {
        return this.checksum == this.crc.checksum();
    }

    public boolean padding() {
        if (this.h_padding_bit == 0) {
            return false;
        }
        return true;
    }

    public int slots() {
        return this.nSlots;
    }

    public int mode_extension() {
        return this.h_mode_extension;
    }

    public int calculate_framesize() {
        int i = 17;
        int i2 = 0;
        if (this.h_layer == 1) {
            this.framesize = (bitrates[this.h_version][0][this.h_bitrate_index] * 12) / frequencies[this.h_version][this.h_sample_frequency];
            if (this.h_padding_bit != 0) {
                this.framesize++;
            }
            this.framesize <<= 2;
            this.nSlots = 0;
        } else {
            this.framesize = (bitrates[this.h_version][this.h_layer - 1][this.h_bitrate_index] * 144) / frequencies[this.h_version][this.h_sample_frequency];
            if (this.h_version == 0 || this.h_version == 2) {
                this.framesize >>= 1;
            }
            if (this.h_padding_bit != 0) {
                this.framesize++;
            }
            if (this.h_layer != 3) {
                this.nSlots = 0;
            } else if (this.h_version == 1) {
                r3 = this.framesize;
                if (this.h_mode != 3) {
                    i = 32;
                }
                r3 -= i;
                if (this.h_protection_bit != 0) {
                    i = 0;
                } else {
                    i = 2;
                }
                this.nSlots = (r3 - i) - 4;
            } else {
                r3 = this.framesize;
                if (this.h_mode == 3) {
                    i = 9;
                }
                i = r3 - i;
                if (this.h_protection_bit == 0) {
                    i2 = 2;
                }
                this.nSlots = (i - i2) - 4;
            }
        }
        this.framesize -= 4;
        return this.framesize;
    }

    public int max_number_of_frames(int streamsize) {
        if (this.h_vbr) {
            return this.h_vbr_frames;
        }
        if ((this.framesize + 4) - this.h_padding_bit == 0) {
            return 0;
        }
        return streamsize / ((this.framesize + 4) - this.h_padding_bit);
    }

    public int min_number_of_frames(int streamsize) {
        if (this.h_vbr) {
            return this.h_vbr_frames;
        }
        if ((this.framesize + 5) - this.h_padding_bit == 0) {
            return 0;
        }
        return streamsize / ((this.framesize + 5) - this.h_padding_bit);
    }

    public float ms_per_frame() {
        if (this.h_vbr) {
            double tpf = this.h_vbr_time_per_frame[layer()] / ((double) frequency());
            if (this.h_version == 0 || this.h_version == 2) {
                tpf /= 2.0d;
            }
            return (float) (1000.0d * tpf);
        }
        return new float[][]{new float[]{8.707483f, 8.0f, 12.0f}, new float[]{26.12245f, 24.0f, 36.0f}, new float[]{26.12245f, 24.0f, 36.0f}}[this.h_layer - 1][this.h_sample_frequency];
    }

    public float total_ms(int streamsize) {
        return ((float) max_number_of_frames(streamsize)) * ms_per_frame();
    }

    public int getSyncHeader() {
        return this._headerstring;
    }

    public String layer_string() {
        switch (this.h_layer) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            default:
                return null;
        }
    }

    public String bitrate_string() {
        if (this.h_vbr) {
            return new StringBuilder(String.valueOf(Integer.toString(bitrate() / 1000))).append(" kb/s").toString();
        }
        return bitrate_str[this.h_version][this.h_layer - 1][this.h_bitrate_index];
    }

    public int bitrate() {
        if (this.h_vbr) {
            return ((int) (((float) (this.h_vbr_bytes * 8)) / (ms_per_frame() * ((float) this.h_vbr_frames)))) * 1000;
        }
        return bitrates[this.h_version][this.h_layer - 1][this.h_bitrate_index];
    }

    public int bitrate_instant() {
        return bitrates[this.h_version][this.h_layer - 1][this.h_bitrate_index];
    }

    public String sample_frequency_string() {
        switch (this.h_sample_frequency) {
            case 0:
                if (this.h_version == 1) {
                    return "44.1 kHz";
                }
                if (this.h_version == 0) {
                    return "22.05 kHz";
                }
                return "11.025 kHz";
            case 1:
                if (this.h_version == 1) {
                    return "48 kHz";
                }
                if (this.h_version == 0) {
                    return "24 kHz";
                }
                return "12 kHz";
            case 2:
                if (this.h_version == 1) {
                    return "32 kHz";
                }
                if (this.h_version == 0) {
                    return "16 kHz";
                }
                return "8 kHz";
            default:
                return null;
        }
    }

    public String mode_string() {
        switch (this.h_mode) {
            case 0:
                return "Stereo";
            case 1:
                return "Joint stereo";
            case 2:
                return "Dual channel";
            case 3:
                return "Single channel";
            default:
                return null;
        }
    }

    public String version_string() {
        switch (this.h_version) {
            case 0:
                return "MPEG-2 LSF";
            case 1:
                return "MPEG-1";
            case 2:
                return "MPEG-2.5 LSF";
            default:
                return null;
        }
    }

    public int number_of_subbands() {
        return this.h_number_of_subbands;
    }

    public int intensity_stereo_bound() {
        return this.h_intensity_stereo_bound;
    }
}
