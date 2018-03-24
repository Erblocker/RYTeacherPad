package javazoom.jl.decoder;

import android.support.v4.media.TransportMediator;
import com.foxit.sdk.common.Font;
import io.vov.vitamio.ThumbnailUtils;
import java.lang.reflect.Array;
import org.apache.http.HttpStatus;
import org.ksoap2.SoapEnvelope;
import org.kxml2.wap.Wbxml;

final class LayerIIIDecoder implements FrameDecoder {
    private static final int SBLIMIT = 32;
    private static final int SSLIMIT = 18;
    public static final float[] TAN12 = new float[]{0.0f, 0.2679492f, 0.57735026f, 1.0f, 1.7320508f, 3.732051f, 9.9999998E10f, -3.732051f, -1.7320508f, -1.0f, -0.57735026f, -0.2679492f, 0.0f, 0.2679492f, 0.57735026f, 1.0f};
    private static final float[] ca = new float[]{-0.51449573f, -0.47173196f, -0.31337744f, -0.1819132f, -0.09457419f, -0.040965583f, -0.014198569f, -0.0036999746f};
    private static final float[] cs = new float[]{0.8574929f, 0.881742f, 0.94962865f, 0.9833146f, 0.9955178f, 0.9991606f, 0.9998992f, 0.99999315f};
    public static final float[][] io = new float[][]{new float[]{1.0f, 0.8408964f, 0.70710677f, 0.59460354f, 0.5f, 0.4204482f, 0.35355338f, 0.29730177f, 0.25f, 0.2102241f, 0.17677669f, 0.14865088f, 0.125f, 0.10511205f, 0.088388346f, 0.07432544f, 0.0625f, 0.052556027f, 0.044194173f, 0.03716272f, 0.03125f, 0.026278013f, 0.022097087f, 0.01858136f, 0.015625f, 0.013139007f, 0.011048543f, 0.00929068f, 0.0078125f, 0.0065695033f, 0.0055242716f, 0.00464534f}, new float[]{1.0f, 0.70710677f, 0.5f, 0.35355338f, 0.25f, 0.17677669f, 0.125f, 0.088388346f, 0.0625f, 0.044194173f, 0.03125f, 0.022097087f, 0.015625f, 0.011048543f, 0.0078125f, 0.0055242716f, 0.00390625f, 0.0027621358f, 0.001953125f, 0.0013810679f, 9.765625E-4f, 6.9053395E-4f, 4.8828125E-4f, 3.4526698E-4f, 2.4414062E-4f, 1.7263349E-4f, 1.2207031E-4f, 8.6316744E-5f, 6.1035156E-5f, 4.3158372E-5f, 3.0517578E-5f, 2.1579186E-5f}};
    public static final int[][][] nr_of_sfb_block;
    public static final int[] pretab;
    private static int[][] reorder_table;
    private static final int[][] slen;
    public static final float[] t_43 = create_t_43();
    public static final float[] two_to_negative_half_pow = new float[]{1.0f, 0.70710677f, 0.5f, 0.35355338f, 0.25f, 0.17677669f, 0.125f, 0.088388346f, 0.0625f, 0.044194173f, 0.03125f, 0.022097087f, 0.015625f, 0.011048543f, 0.0078125f, 0.0055242716f, 0.00390625f, 0.0027621358f, 0.001953125f, 0.0013810679f, 9.765625E-4f, 6.9053395E-4f, 4.8828125E-4f, 3.4526698E-4f, 2.4414062E-4f, 1.7263349E-4f, 1.2207031E-4f, 8.6316744E-5f, 6.1035156E-5f, 4.3158372E-5f, 3.0517578E-5f, 2.1579186E-5f, 1.5258789E-5f, 1.0789593E-5f, 7.6293945E-6f, 5.3947965E-6f, 3.8146973E-6f, 2.6973983E-6f, 1.9073486E-6f, 1.3486991E-6f, 9.536743E-7f, 6.7434956E-7f, 4.7683716E-7f, 3.3717478E-7f, 2.3841858E-7f, 1.6858739E-7f, 1.1920929E-7f, 8.4293696E-8f, 5.9604645E-8f, 4.2146848E-8f, 2.9802322E-8f, 2.1073424E-8f, 1.4901161E-8f, 1.0536712E-8f, 7.4505806E-9f, 5.268356E-9f, 3.7252903E-9f, 2.634178E-9f, 1.8626451E-9f, 1.317089E-9f, 9.313226E-10f, 6.585445E-10f, 4.656613E-10f, 3.2927225E-10f};
    public static final float[][] win = new float[][]{new float[]{-0.016141215f, -0.05360318f, -0.100707136f, -0.16280818f, -0.5f, -0.38388735f, -0.6206114f, -1.1659756f, -3.8720753f, -4.225629f, -1.519529f, -0.97416484f, -0.73744076f, -1.2071068f, -0.5163616f, -0.45426053f, -0.40715656f, -0.3696946f, -0.3387627f, -0.31242222f, -0.28939587f, -0.26880082f, -0.5f, -0.23251417f, -0.21596715f, -0.20004979f, -0.18449493f, -0.16905846f, -0.15350361f, -0.13758625f, -0.12103922f, -0.20710678f, -0.084752575f, -0.06415752f, -0.041131172f, -0.014790705f}, new float[]{-0.016141215f, -0.05360318f, -0.100707136f, -0.16280818f, -0.5f, -0.38388735f, -0.6206114f, -1.1659756f, -3.8720753f, -4.225629f, -1.519529f, -0.97416484f, -0.73744076f, -1.2071068f, -0.5163616f, -0.45426053f, -0.40715656f, -0.3696946f, -0.33908543f, -0.3151181f, -0.29642227f, -0.28184548f, -0.5411961f, -0.2621323f, -0.25387916f, -0.2329629f, -0.19852729f, -0.15233535f, -0.0964964f, -0.03342383f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}, new float[]{-0.0483008f, -0.15715657f, -0.28325045f, -0.42953748f, -1.2071068f, -0.8242648f, -1.1451749f, -1.769529f, -4.5470223f, -3.489053f, -0.7329629f, -0.15076515f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.15076514f, -0.7329629f, -3.489053f, -4.5470223f, -1.769529f, -1.1451749f, -0.8313774f, -1.306563f, -0.54142016f, -0.46528974f, -0.4106699f, -0.3700468f, -0.3387627f, -0.31242222f, -0.28939587f, -0.26880082f, -0.5f, -0.23251417f, -0.21596715f, -0.20004979f, -0.18449493f, -0.16905846f, -0.15350361f, -0.13758625f, -0.12103922f, -0.20710678f, -0.084752575f, -0.06415752f, -0.041131172f, -0.014790705f}};
    private int CheckSumHuff = 0;
    private temporaire2[] III_scalefac_t;
    private BitReserve br;
    private Obuffer buffer;
    private int channels;
    private int counter = 0;
    final double d43 = 1.3333333333333333d;
    private SynthesisFilter filter1;
    private SynthesisFilter filter2;
    private int first_channel;
    private int frame_start;
    private Header header;
    private int[] is_1d;
    int[] is_pos = new int[576];
    float[] is_ratio = new float[576];
    private float[][] k;
    private int last_channel;
    private float[][][] lr;
    private int max_gr;
    private final int[] new_slen = new int[4];
    private int[] nonzero;
    private float[] out_1d;
    private int part2_start;
    private float[][] prevblck;
    float[] rawout = new float[36];
    private float[][][] ro;
    private float[] samples1 = new float[32];
    private float[] samples2 = new float[32];
    private temporaire2[] scalefac;
    public int[] scalefac_buffer;
    private SBI[] sfBandIndex;
    private int sfreq;
    public Sftable sftable;
    private III_side_info_t si;
    private Bitstream stream;
    float[] tsOutCopy = new float[18];
    int[] v = new int[1];
    int[] w = new int[1];
    private int which_channels;
    int[] x = new int[1];
    int[] y = new int[1];

    static class III_side_info_t {
        public temporaire[] ch = new temporaire[2];
        public int main_data_begin = 0;
        public int private_bits = 0;

        public III_side_info_t() {
            this.ch[0] = new temporaire();
            this.ch[1] = new temporaire();
        }
    }

    static class SBI {
        public int[] l;
        public int[] s;

        public SBI() {
            this.l = new int[23];
            this.s = new int[14];
        }

        public SBI(int[] thel, int[] thes) {
            this.l = thel;
            this.s = thes;
        }
    }

    class Sftable {
        public int[] l;
        public int[] s;

        public Sftable() {
            this.l = new int[5];
            this.s = new int[3];
        }

        public Sftable(int[] thel, int[] thes) {
            this.l = thel;
            this.s = thes;
        }
    }

    static class gr_info_s {
        public int big_values = 0;
        public int block_type = 0;
        public int count1table_select = 0;
        public int global_gain = 0;
        public int mixed_block_flag = 0;
        public int part2_3_length = 0;
        public int preflag = 0;
        public int region0_count = 0;
        public int region1_count = 0;
        public int scalefac_compress = 0;
        public int scalefac_scale = 0;
        public int[] subblock_gain = new int[3];
        public int[] table_select = new int[3];
        public int window_switching_flag = 0;
    }

    static class temporaire2 {
        public int[] l = new int[23];
        public int[][] s = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{3, 13}));
    }

    static class temporaire {
        public gr_info_s[] gr = new gr_info_s[2];
        public int[] scfsi = new int[4];

        public temporaire() {
            this.gr[0] = new gr_info_s();
            this.gr[1] = new gr_info_s();
        }
    }

    public LayerIIIDecoder(Bitstream stream0, Header header0, SynthesisFilter filtera, SynthesisFilter filterb, Obuffer buffer0, int which_ch0) {
        huffcodetab.inithuff();
        this.is_1d = new int[580];
        this.ro = (float[][][]) Array.newInstance(Float.TYPE, new int[]{2, 32, 18});
        this.lr = (float[][][]) Array.newInstance(Float.TYPE, new int[]{2, 32, 18});
        this.out_1d = new float[576];
        this.prevblck = (float[][]) Array.newInstance(Float.TYPE, new int[]{2, 576});
        this.k = (float[][]) Array.newInstance(Float.TYPE, new int[]{2, 576});
        this.nonzero = new int[2];
        this.III_scalefac_t = new temporaire2[2];
        this.III_scalefac_t[0] = new temporaire2();
        this.III_scalefac_t[1] = new temporaire2();
        this.scalefac = this.III_scalefac_t;
        this.sfBandIndex = new SBI[9];
        int[] l0 = new int[23];
        l0[1] = 6;
        l0[2] = 12;
        l0[3] = 18;
        l0[4] = 24;
        l0[5] = 30;
        l0[6] = 36;
        l0[7] = 44;
        l0[8] = 54;
        l0[9] = 66;
        l0[10] = 80;
        l0[11] = 96;
        l0[12] = 116;
        l0[13] = 140;
        l0[14] = 168;
        l0[15] = 200;
        l0[16] = Font.e_fontCharsetEastEurope;
        l0[17] = 284;
        l0[18] = 336;
        l0[19] = 396;
        l0[20] = 464;
        l0[21] = 522;
        l0[22] = 576;
        int[] s0 = new int[14];
        s0[1] = 4;
        s0[2] = 8;
        s0[3] = 12;
        s0[4] = 18;
        s0[5] = 24;
        s0[6] = 32;
        s0[7] = 42;
        s0[8] = 56;
        s0[9] = 74;
        s0[10] = 100;
        s0[11] = Wbxml.LITERAL_A;
        s0[12] = 174;
        s0[13] = Wbxml.EXT_0;
        int[] l1 = new int[23];
        l1[1] = 6;
        l1[2] = 12;
        l1[3] = 18;
        l1[4] = 24;
        l1[5] = 30;
        l1[6] = 36;
        l1[7] = 44;
        l1[8] = 54;
        l1[9] = 66;
        l1[10] = 80;
        l1[11] = 96;
        l1[12] = 114;
        l1[13] = Font.e_fontCharsetChineseBig5;
        l1[14] = Font.e_fontCharsetTurkish;
        l1[15] = Wbxml.EXT_2;
        l1[16] = 232;
        l1[17] = 278;
        l1[18] = 330;
        l1[19] = 394;
        l1[20] = 464;
        l1[21] = 540;
        l1[22] = 576;
        int[] s1 = new int[14];
        s1[1] = 4;
        s1[2] = 8;
        s1[3] = 12;
        s1[4] = 18;
        s1[5] = 26;
        s1[6] = 36;
        s1[7] = 48;
        s1[8] = 62;
        s1[9] = 80;
        s1[10] = 104;
        s1[11] = Font.e_fontCharsetChineseBig5;
        s1[12] = 180;
        s1[13] = Wbxml.EXT_0;
        int[] l2 = new int[23];
        l2[1] = 6;
        l2[2] = 12;
        l2[3] = 18;
        l2[4] = 24;
        l2[5] = 30;
        l2[6] = 36;
        l2[7] = 44;
        l2[8] = 54;
        l2[9] = 66;
        l2[10] = 80;
        l2[11] = 96;
        l2[12] = 116;
        l2[13] = 140;
        l2[14] = 168;
        l2[15] = 200;
        l2[16] = Font.e_fontCharsetEastEurope;
        l2[17] = 284;
        l2[18] = 336;
        l2[19] = 396;
        l2[20] = 464;
        l2[21] = 522;
        l2[22] = 576;
        int[] s2 = new int[14];
        s2[1] = 4;
        s2[2] = 8;
        s2[3] = 12;
        s2[4] = 18;
        s2[5] = 26;
        s2[6] = 36;
        s2[7] = 48;
        s2[8] = 62;
        s2[9] = 80;
        s2[10] = 104;
        s2[11] = Font.e_fontCharsetGB2312;
        s2[12] = 174;
        s2[13] = Wbxml.EXT_0;
        int[] l3 = new int[23];
        l3[1] = 4;
        l3[2] = 8;
        l3[3] = 12;
        l3[4] = 16;
        l3[5] = 20;
        l3[6] = 24;
        l3[7] = 30;
        l3[8] = 36;
        l3[9] = 44;
        l3[10] = 52;
        l3[11] = 62;
        l3[12] = 74;
        l3[13] = 90;
        l3[14] = SoapEnvelope.VER11;
        l3[15] = Font.e_fontCharsetGB2312;
        l3[16] = Font.e_fontCharsetTurkish;
        l3[17] = Wbxml.LITERAL_AC;
        l3[18] = Font.e_fontCharsetEastEurope;
        l3[19] = 288;
        l3[20] = 342;
        l3[21] = 418;
        l3[22] = 576;
        int[] s3 = new int[14];
        s3[1] = 4;
        s3[2] = 8;
        s3[3] = 12;
        s3[4] = 16;
        s3[5] = 22;
        s3[6] = 30;
        s3[7] = 40;
        s3[8] = 52;
        s3[9] = 66;
        s3[10] = 84;
        s3[11] = 106;
        s3[12] = Font.e_fontCharsetChineseBig5;
        s3[13] = Wbxml.EXT_0;
        int[] l4 = new int[23];
        l4[1] = 4;
        l4[2] = 8;
        l4[3] = 12;
        l4[4] = 16;
        l4[5] = 20;
        l4[6] = 24;
        l4[7] = 30;
        l4[8] = 36;
        l4[9] = 42;
        l4[10] = 50;
        l4[11] = 60;
        l4[12] = 72;
        l4[13] = 88;
        l4[14] = 106;
        l4[15] = 128;
        l4[16] = 156;
        l4[17] = 190;
        l4[18] = 230;
        l4[19] = 276;
        l4[20] = 330;
        l4[21] = 384;
        l4[22] = 576;
        int[] s4 = new int[14];
        s4[1] = 4;
        s4[2] = 8;
        s4[3] = 12;
        s4[4] = 16;
        s4[5] = 22;
        s4[6] = 28;
        s4[7] = 38;
        s4[8] = 50;
        s4[9] = 64;
        s4[10] = 80;
        s4[11] = 100;
        s4[12] = TransportMediator.KEYCODE_MEDIA_PLAY;
        s4[13] = Wbxml.EXT_0;
        int[] l5 = new int[23];
        l5[1] = 4;
        l5[2] = 8;
        l5[3] = 12;
        l5[4] = 16;
        l5[5] = 20;
        l5[6] = 24;
        l5[7] = 30;
        l5[8] = 36;
        l5[9] = 44;
        l5[10] = 54;
        l5[11] = 66;
        l5[12] = 82;
        l5[13] = 102;
        l5[14] = TransportMediator.KEYCODE_MEDIA_PLAY;
        l5[15] = 156;
        l5[16] = Wbxml.EXT_2;
        l5[17] = 240;
        l5[18] = 296;
        l5[19] = 364;
        l5[20] = 448;
        l5[21] = 550;
        l5[22] = 576;
        int[] s5 = new int[14];
        s5[1] = 4;
        s5[2] = 8;
        s5[3] = 12;
        s5[4] = 16;
        s5[5] = 22;
        s5[6] = 30;
        s5[7] = 42;
        s5[8] = 58;
        s5[9] = 78;
        s5[10] = 104;
        s5[11] = 138;
        s5[12] = 180;
        s5[13] = Wbxml.EXT_0;
        int[] l6 = new int[23];
        l6[1] = 6;
        l6[2] = 12;
        l6[3] = 18;
        l6[4] = 24;
        l6[5] = 30;
        l6[6] = 36;
        l6[7] = 44;
        l6[8] = 54;
        l6[9] = 66;
        l6[10] = 80;
        l6[11] = 96;
        l6[12] = 116;
        l6[13] = 140;
        l6[14] = 168;
        l6[15] = 200;
        l6[16] = Font.e_fontCharsetEastEurope;
        l6[17] = 284;
        l6[18] = 336;
        l6[19] = 396;
        l6[20] = 464;
        l6[21] = 522;
        l6[22] = 576;
        int[] s6 = new int[14];
        s6[1] = 4;
        s6[2] = 8;
        s6[3] = 12;
        s6[4] = 18;
        s6[5] = 26;
        s6[6] = 36;
        s6[7] = 48;
        s6[8] = 62;
        s6[9] = 80;
        s6[10] = 104;
        s6[11] = Font.e_fontCharsetGB2312;
        s6[12] = 174;
        s6[13] = Wbxml.EXT_0;
        int[] l7 = new int[23];
        l7[1] = 6;
        l7[2] = 12;
        l7[3] = 18;
        l7[4] = 24;
        l7[5] = 30;
        l7[6] = 36;
        l7[7] = 44;
        l7[8] = 54;
        l7[9] = 66;
        l7[10] = 80;
        l7[11] = 96;
        l7[12] = 116;
        l7[13] = 140;
        l7[14] = 168;
        l7[15] = 200;
        l7[16] = Font.e_fontCharsetEastEurope;
        l7[17] = 284;
        l7[18] = 336;
        l7[19] = 396;
        l7[20] = 464;
        l7[21] = 522;
        l7[22] = 576;
        int[] s7 = new int[14];
        s7[1] = 4;
        s7[2] = 8;
        s7[3] = 12;
        s7[4] = 18;
        s7[5] = 26;
        s7[6] = 36;
        s7[7] = 48;
        s7[8] = 62;
        s7[9] = 80;
        s7[10] = 104;
        s7[11] = Font.e_fontCharsetGB2312;
        s7[12] = 174;
        s7[13] = Wbxml.EXT_0;
        int[] l8 = new int[23];
        l8[1] = 12;
        l8[2] = 24;
        l8[3] = 36;
        l8[4] = 48;
        l8[5] = 60;
        l8[6] = 72;
        l8[7] = 88;
        l8[8] = 108;
        l8[9] = Wbxml.LITERAL_A;
        l8[10] = ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT;
        l8[11] = Wbxml.EXT_0;
        l8[12] = 232;
        l8[13] = 280;
        l8[14] = 336;
        l8[15] = HttpStatus.SC_BAD_REQUEST;
        l8[16] = 476;
        l8[17] = 566;
        l8[18] = 568;
        l8[19] = 570;
        l8[20] = 572;
        l8[21] = 574;
        l8[22] = 576;
        int[] s8 = new int[]{8, 16, 24, 36, 52, 72, 96, 124, ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT, Font.e_fontCharsetTurkish, 164, 166, Wbxml.EXT_0, new SBI(l0, s0)};
        this.sfBandIndex[1] = new SBI(l1, s1);
        this.sfBandIndex[2] = new SBI(l2, s2);
        this.sfBandIndex[3] = new SBI(l3, s3);
        this.sfBandIndex[4] = new SBI(l4, s4);
        this.sfBandIndex[5] = new SBI(l5, s5);
        this.sfBandIndex[6] = new SBI(l6, s6);
        this.sfBandIndex[7] = new SBI(l7, s7);
        this.sfBandIndex[8] = new SBI(l8, s8);
        if (reorder_table == null) {
            reorder_table = new int[9][];
            for (int i = 0; i < 9; i++) {
                reorder_table[i] = reorder(this.sfBandIndex[i].s);
            }
        }
        int[] ll0 = new int[5];
        ll0[1] = 6;
        ll0[2] = 11;
        ll0[3] = 16;
        ll0[4] = 21;
        int[] ss0 = new int[3];
        ss0[1] = 6;
        ss0[2] = 12;
        this.sftable = new Sftable(ll0, ss0);
        this.scalefac_buffer = new int[54];
        this.stream = stream0;
        this.header = header0;
        this.filter1 = filtera;
        this.filter2 = filterb;
        this.buffer = buffer0;
        this.which_channels = which_ch0;
        this.frame_start = 0;
        this.channels = this.header.mode() == 3 ? 1 : 2;
        this.max_gr = this.header.version() == 1 ? 2 : 1;
        int sample_frequency = this.header.sample_frequency();
        int i2 = this.header.version() == 1 ? 3 : this.header.version() == 2 ? 6 : 0;
        this.sfreq = i2 + sample_frequency;
        if (this.channels == 2) {
            switch (this.which_channels) {
                case 1:
                case 3:
                    this.last_channel = 0;
                    this.first_channel = 0;
                    break;
                case 2:
                    this.last_channel = 1;
                    this.first_channel = 1;
                    break;
                default:
                    this.first_channel = 0;
                    this.last_channel = 1;
                    break;
            }
        }
        this.last_channel = 0;
        this.first_channel = 0;
        for (int ch = 0; ch < 2; ch++) {
            for (int j = 0; j < 576; j++) {
                this.prevblck[ch][j] = 0.0f;
            }
        }
        int[] iArr = this.nonzero;
        this.nonzero[1] = 576;
        iArr[0] = 576;
        this.br = new BitReserve();
        this.si = new III_side_info_t();
    }

    public void seek_notify() {
        this.frame_start = 0;
        for (int ch = 0; ch < 2; ch++) {
            for (int j = 0; j < 576; j++) {
                this.prevblck[ch][j] = 0.0f;
            }
        }
        this.br = new BitReserve();
    }

    public void decodeFrame() {
        decode();
    }

    public void decode() {
        int nSlots = this.header.slots();
        get_side_info();
        for (int i = 0; i < nSlots; i++) {
            this.br.hputbuf(this.stream.get_bits(8));
        }
        int main_data_end = this.br.hsstell() >>> 3;
        int flush_main = this.br.hsstell() & 7;
        if (flush_main != 0) {
            this.br.hgetbits(8 - flush_main);
            main_data_end++;
        }
        int bytes_to_discard = (this.frame_start - main_data_end) - this.si.main_data_begin;
        this.frame_start += nSlots;
        if (bytes_to_discard >= 0) {
            if (main_data_end > 4096) {
                this.frame_start -= 4096;
                this.br.rewindNbytes(4096);
            }
            while (bytes_to_discard > 0) {
                this.br.hgetbits(8);
                bytes_to_discard--;
            }
            for (int gr = 0; gr < this.max_gr; gr++) {
                int ch;
                for (ch = 0; ch < this.channels; ch++) {
                    this.part2_start = this.br.hsstell();
                    if (this.header.version() == 1) {
                        get_scale_factors(ch, gr);
                    } else {
                        get_LSF_scale_factors(ch, gr);
                    }
                    huffman_decode(ch, gr);
                    dequantize_sample(this.ro[ch], ch, gr);
                }
                stereo(gr);
                if (this.which_channels == 3 && this.channels > 1) {
                    do_downmix();
                }
                for (ch = this.first_channel; ch <= this.last_channel; ch++) {
                    int sb18;
                    int ss;
                    reorder(this.lr[ch], ch, gr);
                    antialias(ch, gr);
                    hybrid(ch, gr);
                    for (sb18 = 18; sb18 < 576; sb18 += 36) {
                        for (ss = 1; ss < 18; ss += 2) {
                            this.out_1d[sb18 + ss] = -this.out_1d[sb18 + ss];
                        }
                    }
                    int sb;
                    if (ch == 0 || this.which_channels == 2) {
                        for (ss = 0; ss < 18; ss++) {
                            sb = 0;
                            for (sb18 = 0; sb18 < 576; sb18 += 18) {
                                this.samples1[sb] = this.out_1d[sb18 + ss];
                                sb++;
                            }
                            this.filter1.input_samples(this.samples1);
                            this.filter1.calculate_pcm_samples(this.buffer);
                        }
                    } else {
                        for (ss = 0; ss < 18; ss++) {
                            sb = 0;
                            for (sb18 = 0; sb18 < 576; sb18 += 18) {
                                this.samples2[sb] = this.out_1d[sb18 + ss];
                                sb++;
                            }
                            this.filter2.input_samples(this.samples2);
                            this.filter2.calculate_pcm_samples(this.buffer);
                        }
                    }
                }
            }
            this.counter++;
            this.buffer.write_buffer(1);
        }
    }

    private boolean get_side_info() {
        int ch;
        if (this.header.version() == 1) {
            this.si.main_data_begin = this.stream.get_bits(9);
            if (this.channels == 1) {
                this.si.private_bits = this.stream.get_bits(5);
            } else {
                this.si.private_bits = this.stream.get_bits(3);
            }
            for (ch = 0; ch < this.channels; ch++) {
                this.si.ch[ch].scfsi[0] = this.stream.get_bits(1);
                this.si.ch[ch].scfsi[1] = this.stream.get_bits(1);
                this.si.ch[ch].scfsi[2] = this.stream.get_bits(1);
                this.si.ch[ch].scfsi[3] = this.stream.get_bits(1);
            }
            int gr = 0;
            while (gr < 2) {
                ch = 0;
                while (ch < this.channels) {
                    this.si.ch[ch].gr[gr].part2_3_length = this.stream.get_bits(12);
                    this.si.ch[ch].gr[gr].big_values = this.stream.get_bits(9);
                    this.si.ch[ch].gr[gr].global_gain = this.stream.get_bits(8);
                    this.si.ch[ch].gr[gr].scalefac_compress = this.stream.get_bits(4);
                    this.si.ch[ch].gr[gr].window_switching_flag = this.stream.get_bits(1);
                    if (this.si.ch[ch].gr[gr].window_switching_flag != 0) {
                        this.si.ch[ch].gr[gr].block_type = this.stream.get_bits(2);
                        this.si.ch[ch].gr[gr].mixed_block_flag = this.stream.get_bits(1);
                        this.si.ch[ch].gr[gr].table_select[0] = this.stream.get_bits(5);
                        this.si.ch[ch].gr[gr].table_select[1] = this.stream.get_bits(5);
                        this.si.ch[ch].gr[gr].subblock_gain[0] = this.stream.get_bits(3);
                        this.si.ch[ch].gr[gr].subblock_gain[1] = this.stream.get_bits(3);
                        this.si.ch[ch].gr[gr].subblock_gain[2] = this.stream.get_bits(3);
                        if (this.si.ch[ch].gr[gr].block_type == 0) {
                            return false;
                        }
                        if (this.si.ch[ch].gr[gr].block_type == 2 && this.si.ch[ch].gr[gr].mixed_block_flag == 0) {
                            this.si.ch[ch].gr[gr].region0_count = 8;
                        } else {
                            this.si.ch[ch].gr[gr].region0_count = 7;
                        }
                        this.si.ch[ch].gr[gr].region1_count = 20 - this.si.ch[ch].gr[gr].region0_count;
                    } else {
                        this.si.ch[ch].gr[gr].table_select[0] = this.stream.get_bits(5);
                        this.si.ch[ch].gr[gr].table_select[1] = this.stream.get_bits(5);
                        this.si.ch[ch].gr[gr].table_select[2] = this.stream.get_bits(5);
                        this.si.ch[ch].gr[gr].region0_count = this.stream.get_bits(4);
                        this.si.ch[ch].gr[gr].region1_count = this.stream.get_bits(3);
                        this.si.ch[ch].gr[gr].block_type = 0;
                    }
                    this.si.ch[ch].gr[gr].preflag = this.stream.get_bits(1);
                    this.si.ch[ch].gr[gr].scalefac_scale = this.stream.get_bits(1);
                    this.si.ch[ch].gr[gr].count1table_select = this.stream.get_bits(1);
                    ch++;
                }
                gr++;
            }
        } else {
            this.si.main_data_begin = this.stream.get_bits(8);
            if (this.channels == 1) {
                this.si.private_bits = this.stream.get_bits(1);
            } else {
                this.si.private_bits = this.stream.get_bits(2);
            }
            ch = 0;
            while (ch < this.channels) {
                this.si.ch[ch].gr[0].part2_3_length = this.stream.get_bits(12);
                this.si.ch[ch].gr[0].big_values = this.stream.get_bits(9);
                this.si.ch[ch].gr[0].global_gain = this.stream.get_bits(8);
                this.si.ch[ch].gr[0].scalefac_compress = this.stream.get_bits(9);
                this.si.ch[ch].gr[0].window_switching_flag = this.stream.get_bits(1);
                if (this.si.ch[ch].gr[0].window_switching_flag != 0) {
                    this.si.ch[ch].gr[0].block_type = this.stream.get_bits(2);
                    this.si.ch[ch].gr[0].mixed_block_flag = this.stream.get_bits(1);
                    this.si.ch[ch].gr[0].table_select[0] = this.stream.get_bits(5);
                    this.si.ch[ch].gr[0].table_select[1] = this.stream.get_bits(5);
                    this.si.ch[ch].gr[0].subblock_gain[0] = this.stream.get_bits(3);
                    this.si.ch[ch].gr[0].subblock_gain[1] = this.stream.get_bits(3);
                    this.si.ch[ch].gr[0].subblock_gain[2] = this.stream.get_bits(3);
                    if (this.si.ch[ch].gr[0].block_type == 0) {
                        return false;
                    }
                    if (this.si.ch[ch].gr[0].block_type == 2 && this.si.ch[ch].gr[0].mixed_block_flag == 0) {
                        this.si.ch[ch].gr[0].region0_count = 8;
                    } else {
                        this.si.ch[ch].gr[0].region0_count = 7;
                        this.si.ch[ch].gr[0].region1_count = 20 - this.si.ch[ch].gr[0].region0_count;
                    }
                } else {
                    this.si.ch[ch].gr[0].table_select[0] = this.stream.get_bits(5);
                    this.si.ch[ch].gr[0].table_select[1] = this.stream.get_bits(5);
                    this.si.ch[ch].gr[0].table_select[2] = this.stream.get_bits(5);
                    this.si.ch[ch].gr[0].region0_count = this.stream.get_bits(4);
                    this.si.ch[ch].gr[0].region1_count = this.stream.get_bits(3);
                    this.si.ch[ch].gr[0].block_type = 0;
                }
                this.si.ch[ch].gr[0].scalefac_scale = this.stream.get_bits(1);
                this.si.ch[ch].gr[0].count1table_select = this.stream.get_bits(1);
                ch++;
            }
        }
        return true;
    }

    private void get_scale_factors(int ch, int gr) {
        gr_info_s gr_info = this.si.ch[ch].gr[gr];
        int scale_comp = gr_info.scalefac_compress;
        int length0 = slen[0][scale_comp];
        int length1 = slen[1][scale_comp];
        if (gr_info.window_switching_flag == 0 || gr_info.block_type != 2) {
            if (this.si.ch[ch].scfsi[0] == 0 || gr == 0) {
                this.scalefac[ch].l[0] = this.br.hgetbits(length0);
                this.scalefac[ch].l[1] = this.br.hgetbits(length0);
                this.scalefac[ch].l[2] = this.br.hgetbits(length0);
                this.scalefac[ch].l[3] = this.br.hgetbits(length0);
                this.scalefac[ch].l[4] = this.br.hgetbits(length0);
                this.scalefac[ch].l[5] = this.br.hgetbits(length0);
            }
            if (this.si.ch[ch].scfsi[1] == 0 || gr == 0) {
                this.scalefac[ch].l[6] = this.br.hgetbits(length0);
                this.scalefac[ch].l[7] = this.br.hgetbits(length0);
                this.scalefac[ch].l[8] = this.br.hgetbits(length0);
                this.scalefac[ch].l[9] = this.br.hgetbits(length0);
                this.scalefac[ch].l[10] = this.br.hgetbits(length0);
            }
            if (this.si.ch[ch].scfsi[2] == 0 || gr == 0) {
                this.scalefac[ch].l[11] = this.br.hgetbits(length1);
                this.scalefac[ch].l[12] = this.br.hgetbits(length1);
                this.scalefac[ch].l[13] = this.br.hgetbits(length1);
                this.scalefac[ch].l[14] = this.br.hgetbits(length1);
                this.scalefac[ch].l[15] = this.br.hgetbits(length1);
            }
            if (this.si.ch[ch].scfsi[3] == 0 || gr == 0) {
                this.scalefac[ch].l[16] = this.br.hgetbits(length1);
                this.scalefac[ch].l[17] = this.br.hgetbits(length1);
                this.scalefac[ch].l[18] = this.br.hgetbits(length1);
                this.scalefac[ch].l[19] = this.br.hgetbits(length1);
                this.scalefac[ch].l[20] = this.br.hgetbits(length1);
            }
            this.scalefac[ch].l[21] = 0;
            this.scalefac[ch].l[22] = 0;
        } else if (gr_info.mixed_block_flag != 0) {
            int sfb;
            int window;
            for (sfb = 0; sfb < 8; sfb++) {
                this.scalefac[ch].l[sfb] = this.br.hgetbits(slen[0][gr_info.scalefac_compress]);
            }
            for (sfb = 3; sfb < 6; sfb++) {
                for (window = 0; window < 3; window++) {
                    this.scalefac[ch].s[window][sfb] = this.br.hgetbits(slen[0][gr_info.scalefac_compress]);
                }
            }
            for (sfb = 6; sfb < 12; sfb++) {
                for (window = 0; window < 3; window++) {
                    this.scalefac[ch].s[window][sfb] = this.br.hgetbits(slen[1][gr_info.scalefac_compress]);
                }
            }
            for (window = 0; window < 3; window++) {
                this.scalefac[ch].s[window][12] = 0;
            }
        } else {
            this.scalefac[ch].s[0][0] = this.br.hgetbits(length0);
            this.scalefac[ch].s[1][0] = this.br.hgetbits(length0);
            this.scalefac[ch].s[2][0] = this.br.hgetbits(length0);
            this.scalefac[ch].s[0][1] = this.br.hgetbits(length0);
            this.scalefac[ch].s[1][1] = this.br.hgetbits(length0);
            this.scalefac[ch].s[2][1] = this.br.hgetbits(length0);
            this.scalefac[ch].s[0][2] = this.br.hgetbits(length0);
            this.scalefac[ch].s[1][2] = this.br.hgetbits(length0);
            this.scalefac[ch].s[2][2] = this.br.hgetbits(length0);
            this.scalefac[ch].s[0][3] = this.br.hgetbits(length0);
            this.scalefac[ch].s[1][3] = this.br.hgetbits(length0);
            this.scalefac[ch].s[2][3] = this.br.hgetbits(length0);
            this.scalefac[ch].s[0][4] = this.br.hgetbits(length0);
            this.scalefac[ch].s[1][4] = this.br.hgetbits(length0);
            this.scalefac[ch].s[2][4] = this.br.hgetbits(length0);
            this.scalefac[ch].s[0][5] = this.br.hgetbits(length0);
            this.scalefac[ch].s[1][5] = this.br.hgetbits(length0);
            this.scalefac[ch].s[2][5] = this.br.hgetbits(length0);
            this.scalefac[ch].s[0][6] = this.br.hgetbits(length1);
            this.scalefac[ch].s[1][6] = this.br.hgetbits(length1);
            this.scalefac[ch].s[2][6] = this.br.hgetbits(length1);
            this.scalefac[ch].s[0][7] = this.br.hgetbits(length1);
            this.scalefac[ch].s[1][7] = this.br.hgetbits(length1);
            this.scalefac[ch].s[2][7] = this.br.hgetbits(length1);
            this.scalefac[ch].s[0][8] = this.br.hgetbits(length1);
            this.scalefac[ch].s[1][8] = this.br.hgetbits(length1);
            this.scalefac[ch].s[2][8] = this.br.hgetbits(length1);
            this.scalefac[ch].s[0][9] = this.br.hgetbits(length1);
            this.scalefac[ch].s[1][9] = this.br.hgetbits(length1);
            this.scalefac[ch].s[2][9] = this.br.hgetbits(length1);
            this.scalefac[ch].s[0][10] = this.br.hgetbits(length1);
            this.scalefac[ch].s[1][10] = this.br.hgetbits(length1);
            this.scalefac[ch].s[2][10] = this.br.hgetbits(length1);
            this.scalefac[ch].s[0][11] = this.br.hgetbits(length1);
            this.scalefac[ch].s[1][11] = this.br.hgetbits(length1);
            this.scalefac[ch].s[2][11] = this.br.hgetbits(length1);
            this.scalefac[ch].s[0][12] = 0;
            this.scalefac[ch].s[1][12] = 0;
            this.scalefac[ch].s[2][12] = 0;
        }
    }

    private void get_LSF_scale_data(int ch, int gr) {
        int blocktypenumber;
        int mode_ext = this.header.mode_extension();
        int blocknumber = 0;
        gr_info_s gr_info = this.si.ch[ch].gr[gr];
        int scalefac_comp = gr_info.scalefac_compress;
        if (gr_info.block_type != 2) {
            blocktypenumber = 0;
        } else if (gr_info.mixed_block_flag == 0) {
            blocktypenumber = 1;
        } else if (gr_info.mixed_block_flag == 1) {
            blocktypenumber = 2;
        } else {
            blocktypenumber = 0;
        }
        if (!((mode_ext == 1 || mode_ext == 3) && ch == 1)) {
            if (scalefac_comp < HttpStatus.SC_BAD_REQUEST) {
                this.new_slen[0] = (scalefac_comp >>> 4) / 5;
                this.new_slen[1] = (scalefac_comp >>> 4) % 5;
                this.new_slen[2] = (scalefac_comp & 15) >>> 2;
                this.new_slen[3] = scalefac_comp & 3;
                this.si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 0;
            } else if (scalefac_comp < 500) {
                this.new_slen[0] = ((scalefac_comp - 400) >>> 2) / 5;
                this.new_slen[1] = ((scalefac_comp - 400) >>> 2) % 5;
                this.new_slen[2] = (scalefac_comp - 400) & 3;
                this.new_slen[3] = 0;
                this.si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 1;
            } else if (scalefac_comp < 512) {
                this.new_slen[0] = (scalefac_comp - 500) / 3;
                this.new_slen[1] = (scalefac_comp - 500) % 3;
                this.new_slen[2] = 0;
                this.new_slen[3] = 0;
                this.si.ch[ch].gr[gr].preflag = 1;
                blocknumber = 2;
            }
        }
        if ((mode_ext == 1 || mode_ext == 3) && ch == 1) {
            int int_scalefac_comp = scalefac_comp >>> 1;
            if (int_scalefac_comp < 180) {
                this.new_slen[0] = int_scalefac_comp / 36;
                this.new_slen[1] = (int_scalefac_comp % 36) / 6;
                this.new_slen[2] = (int_scalefac_comp % 36) % 6;
                this.new_slen[3] = 0;
                this.si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 3;
            } else if (int_scalefac_comp < 244) {
                this.new_slen[0] = ((int_scalefac_comp - 180) & 63) >>> 4;
                this.new_slen[1] = ((int_scalefac_comp - 180) & 15) >>> 2;
                this.new_slen[2] = (int_scalefac_comp - 180) & 3;
                this.new_slen[3] = 0;
                this.si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 4;
            } else if (int_scalefac_comp < 255) {
                this.new_slen[0] = (int_scalefac_comp - 244) / 3;
                this.new_slen[1] = (int_scalefac_comp - 244) % 3;
                this.new_slen[2] = 0;
                this.new_slen[3] = 0;
                this.si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 5;
            }
        }
        for (int x = 0; x < 45; x++) {
            this.scalefac_buffer[x] = 0;
        }
        int m = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < nr_of_sfb_block[blocknumber][blocktypenumber][i]; j++) {
                int i2;
                int[] iArr = this.scalefac_buffer;
                if (this.new_slen[i] == 0) {
                    i2 = 0;
                } else {
                    i2 = this.br.hgetbits(this.new_slen[i]);
                }
                iArr[m] = i2;
                m++;
            }
        }
    }

    private void get_LSF_scale_factors(int ch, int gr) {
        int m = 0;
        gr_info_s gr_info = this.si.ch[ch].gr[gr];
        get_LSF_scale_data(ch, gr);
        int sfb;
        if (gr_info.window_switching_flag == 0 || gr_info.block_type != 2) {
            for (sfb = 0; sfb < 21; sfb++) {
                this.scalefac[ch].l[sfb] = this.scalefac_buffer[m];
                m++;
            }
            this.scalefac[ch].l[21] = 0;
            this.scalefac[ch].l[22] = 0;
        } else if (gr_info.mixed_block_flag != 0) {
            for (sfb = 0; sfb < 8; sfb++) {
                this.scalefac[ch].l[sfb] = this.scalefac_buffer[m];
                m++;
            }
            for (sfb = 3; sfb < 12; sfb++) {
                for (window = 0; window < 3; window++) {
                    this.scalefac[ch].s[window][sfb] = this.scalefac_buffer[m];
                    m++;
                }
            }
            for (window = 0; window < 3; window++) {
                this.scalefac[ch].s[window][12] = 0;
            }
        } else {
            for (sfb = 0; sfb < 12; sfb++) {
                for (window = 0; window < 3; window++) {
                    this.scalefac[ch].s[window][sfb] = this.scalefac_buffer[m];
                    m++;
                }
            }
            for (window = 0; window < 3; window++) {
                this.scalefac[ch].s[window][12] = 0;
            }
        }
    }

    private void huffman_decode(int ch, int gr) {
        int region1Start;
        int region2Start;
        huffcodetab h;
        int i;
        this.x[0] = 0;
        this.y[0] = 0;
        this.v[0] = 0;
        this.w[0] = 0;
        int part2_3_end = this.part2_start + this.si.ch[ch].gr[gr].part2_3_length;
        if (this.si.ch[ch].gr[gr].window_switching_flag == 0 || this.si.ch[ch].gr[gr].block_type != 2) {
            int buf = this.si.ch[ch].gr[gr].region0_count + 1;
            int buf1 = (this.si.ch[ch].gr[gr].region1_count + buf) + 1;
            if (buf1 > this.sfBandIndex[this.sfreq].l.length - 1) {
                buf1 = this.sfBandIndex[this.sfreq].l.length - 1;
            }
            region1Start = this.sfBandIndex[this.sfreq].l[buf];
            region2Start = this.sfBandIndex[this.sfreq].l[buf1];
        } else {
            region1Start = this.sfreq == 8 ? 72 : 36;
            region2Start = 576;
        }
        int index = 0;
        for (int i2 = 0; i2 < (this.si.ch[ch].gr[gr].big_values << 1); i2 += 2) {
            if (i2 < region1Start) {
                h = huffcodetab.ht[this.si.ch[ch].gr[gr].table_select[0]];
            } else if (i2 < region2Start) {
                h = huffcodetab.ht[this.si.ch[ch].gr[gr].table_select[1]];
            } else {
                h = huffcodetab.ht[this.si.ch[ch].gr[gr].table_select[2]];
            }
            huffcodetab.huffman_decoder(h, this.x, this.y, this.v, this.w, this.br);
            i = index + 1;
            this.is_1d[index] = this.x[0];
            index = i + 1;
            this.is_1d[i] = this.y[0];
            this.CheckSumHuff = (this.CheckSumHuff + this.x[0]) + this.y[0];
        }
        h = huffcodetab.ht[this.si.ch[ch].gr[gr].count1table_select + 32];
        int num_bits = this.br.hsstell();
        i = index;
        while (num_bits < part2_3_end && i < 576) {
            huffcodetab.huffman_decoder(h, this.x, this.y, this.v, this.w, this.br);
            index = i + 1;
            this.is_1d[i] = this.v[0];
            i = index + 1;
            this.is_1d[index] = this.w[0];
            index = i + 1;
            this.is_1d[i] = this.x[0];
            i = index + 1;
            this.is_1d[index] = this.y[0];
            this.CheckSumHuff = (((this.CheckSumHuff + this.v[0]) + this.w[0]) + this.x[0]) + this.y[0];
            num_bits = this.br.hsstell();
        }
        if (num_bits > part2_3_end) {
            this.br.rewindNbits(num_bits - part2_3_end);
            index = i - 4;
        } else {
            index = i;
        }
        num_bits = this.br.hsstell();
        if (num_bits < part2_3_end) {
            this.br.hgetbits(part2_3_end - num_bits);
        }
        if (index < 576) {
            this.nonzero[ch] = index;
        } else {
            this.nonzero[ch] = 576;
        }
        if (index < 0) {
            index = 0;
        }
        while (index < 576) {
            this.is_1d[index] = 0;
            index++;
        }
    }

    private void i_stereo_k_values(int is_pos, int io_type, int i) {
        if (is_pos == 0) {
            this.k[0][i] = 1.0f;
            this.k[1][i] = 1.0f;
        } else if ((is_pos & 1) != 0) {
            this.k[0][i] = io[io_type][(is_pos + 1) >>> 1];
            this.k[1][i] = 1.0f;
        } else {
            this.k[0][i] = 1.0f;
            this.k[1][i] = io[io_type][is_pos >>> 1];
        }
    }

    private void dequantize_sample(float[][] xr, int ch, int gr) {
        int next_cb_boundary;
        int j;
        gr_info_s gr_info = this.si.ch[ch].gr[gr];
        int cb = 0;
        int cb_begin = 0;
        int cb_width = 0;
        int index = 0;
        float[][] xr_1d = xr;
        if (gr_info.window_switching_flag == 0 || gr_info.block_type != 2) {
            next_cb_boundary = this.sfBandIndex[this.sfreq].l[1];
        } else if (gr_info.mixed_block_flag != 0) {
            next_cb_boundary = this.sfBandIndex[this.sfreq].l[1];
        } else {
            cb_width = this.sfBandIndex[this.sfreq].s[1];
            next_cb_boundary = (cb_width << 2) - cb_width;
            cb_begin = 0;
        }
        float g_gain = (float) Math.pow(2.0d, 0.25d * (((double) gr_info.global_gain) - 210.0d));
        for (j = 0; j < this.nonzero[ch]; j++) {
            int reste = j % 18;
            int quotien = (j - reste) / 18;
            if (this.is_1d[j] == 0) {
                xr_1d[quotien][reste] = 0.0f;
            } else {
                int abv = this.is_1d[j];
                if (abv < t_43.length) {
                    if (this.is_1d[j] > 0) {
                        xr_1d[quotien][reste] = t_43[abv] * g_gain;
                    } else {
                        if ((-abv) < t_43.length) {
                            xr_1d[quotien][reste] = (-g_gain) * t_43[-abv];
                        } else {
                            xr_1d[quotien][reste] = (-g_gain) * ((float) Math.pow((double) (-abv), 1.3333333333333333d));
                        }
                    }
                } else if (this.is_1d[j] > 0) {
                    xr_1d[quotien][reste] = ((float) Math.pow((double) abv, 1.3333333333333333d)) * g_gain;
                } else {
                    xr_1d[quotien][reste] = (-g_gain) * ((float) Math.pow((double) (-abv), 1.3333333333333333d));
                }
            }
        }
        j = 0;
        while (j < this.nonzero[ch]) {
            reste = j % 18;
            quotien = (j - reste) / 18;
            if (index == next_cb_boundary) {
                if (gr_info.window_switching_flag == 0 || gr_info.block_type != 2) {
                    cb++;
                    next_cb_boundary = this.sfBandIndex[this.sfreq].l[cb + 1];
                } else if (gr_info.mixed_block_flag == 0) {
                    cb++;
                    next_cb_boundary = this.sfBandIndex[this.sfreq].s[cb + 1];
                    next_cb_boundary = (next_cb_boundary << 2) - next_cb_boundary;
                    cb_begin = this.sfBandIndex[this.sfreq].s[cb];
                    cb_width = this.sfBandIndex[this.sfreq].s[cb + 1] - cb_begin;
                    cb_begin = (cb_begin << 2) - cb_begin;
                } else if (index == this.sfBandIndex[this.sfreq].l[8]) {
                    next_cb_boundary = this.sfBandIndex[this.sfreq].s[4];
                    next_cb_boundary = (next_cb_boundary << 2) - next_cb_boundary;
                    cb = 3;
                    cb_width = this.sfBandIndex[this.sfreq].s[4] - this.sfBandIndex[this.sfreq].s[3];
                    cb_begin = this.sfBandIndex[this.sfreq].s[3];
                    cb_begin = (cb_begin << 2) - cb_begin;
                } else if (index < this.sfBandIndex[this.sfreq].l[8]) {
                    cb++;
                    next_cb_boundary = this.sfBandIndex[this.sfreq].l[cb + 1];
                } else {
                    cb++;
                    next_cb_boundary = this.sfBandIndex[this.sfreq].s[cb + 1];
                    next_cb_boundary = (next_cb_boundary << 2) - next_cb_boundary;
                    cb_begin = this.sfBandIndex[this.sfreq].s[cb];
                    cb_width = this.sfBandIndex[this.sfreq].s[cb + 1] - cb_begin;
                    cb_begin = (cb_begin << 2) - cb_begin;
                }
            }
            int idx;
            float[] fArr;
            if (gr_info.window_switching_flag == 0 || (!(gr_info.block_type == 2 && gr_info.mixed_block_flag == 0) && (gr_info.block_type != 2 || gr_info.mixed_block_flag == 0 || j < 36))) {
                idx = this.scalefac[ch].l[cb];
                if (gr_info.preflag != 0) {
                    idx += pretab[cb];
                }
                idx <<= gr_info.scalefac_scale;
                fArr = xr_1d[quotien];
                fArr[reste] = fArr[reste] * two_to_negative_half_pow[idx];
            } else {
                int t_index = (index - cb_begin) / cb_width;
                idx = (this.scalefac[ch].s[t_index][cb] << gr_info.scalefac_scale) + (gr_info.subblock_gain[t_index] << 2);
                fArr = xr_1d[quotien];
                fArr[reste] = fArr[reste] * two_to_negative_half_pow[idx];
            }
            index++;
            j++;
        }
        for (j = this.nonzero[ch]; j < 576; j++) {
            reste = j % 18;
            quotien = (j - reste) / 18;
            if (reste < 0) {
                reste = 0;
            }
            if (quotien < 0) {
                quotien = 0;
            }
            xr_1d[quotien][reste] = 0.0f;
        }
    }

    private void reorder(float[][] xr, int ch, int gr) {
        gr_info_s gr_info = this.si.ch[ch].gr[gr];
        float[][] xr_1d = xr;
        int index;
        if (gr_info.window_switching_flag == 0 || gr_info.block_type != 2) {
            for (index = 0; index < 576; index++) {
                int reste = index % 18;
                this.out_1d[index] = xr_1d[(index - reste) / 18][reste];
            }
            return;
        }
        for (index = 0; index < 576; index++) {
            this.out_1d[index] = 0.0f;
        }
        if (gr_info.mixed_block_flag != 0) {
            for (index = 0; index < 36; index++) {
                reste = index % 18;
                this.out_1d[index] = xr_1d[(index - reste) / 18][reste];
            }
            for (int sfb = 3; sfb < 13; sfb++) {
                int sfb_start = this.sfBandIndex[this.sfreq].s[sfb];
                int sfb_lines = this.sfBandIndex[this.sfreq].s[sfb + 1] - sfb_start;
                int sfb_start3 = (sfb_start << 2) - sfb_start;
                int freq = 0;
                int freq3 = 0;
                while (freq < sfb_lines) {
                    int src_line = sfb_start3 + freq;
                    int des_line = sfb_start3 + freq3;
                    reste = src_line % 18;
                    this.out_1d[des_line] = xr_1d[(src_line - reste) / 18][reste];
                    src_line += sfb_lines;
                    des_line++;
                    reste = src_line % 18;
                    this.out_1d[des_line] = xr_1d[(src_line - reste) / 18][reste];
                    src_line += sfb_lines;
                    reste = src_line % 18;
                    this.out_1d[des_line + 1] = xr_1d[(src_line - reste) / 18][reste];
                    freq++;
                    freq3 += 3;
                }
            }
            return;
        }
        for (index = 0; index < 576; index++) {
            int j = reorder_table[this.sfreq][index];
            reste = j % 18;
            this.out_1d[index] = xr_1d[(j - reste) / 18][reste];
        }
    }

    private void stereo(int gr) {
        int sb;
        int ss;
        if (this.channels == 1) {
            for (sb = 0; sb < 32; sb++) {
                for (ss = 0; ss < 18; ss += 3) {
                    this.lr[0][sb][ss] = this.ro[0][sb][ss];
                    this.lr[0][sb][ss + 1] = this.ro[0][sb][ss + 1];
                    this.lr[0][sb][ss + 2] = this.ro[0][sb][ss + 2];
                }
            }
            return;
        }
        int i;
        gr_info_s gr_info = this.si.ch[0].gr[gr];
        int mode_ext = this.header.mode_extension();
        boolean ms_stereo = this.header.mode() == 1 && (mode_ext & 2) != 0;
        boolean i_stereo = this.header.mode() == 1 && (mode_ext & 1) != 0;
        boolean lsf = this.header.version() == 0 || this.header.version() == 2;
        int io_type = gr_info.scalefac_compress & 1;
        for (i = 0; i < 576; i++) {
            this.is_pos[i] = 7;
            this.is_ratio[i] = 0.0f;
        }
        if (i_stereo) {
            int sfb;
            if (gr_info.window_switching_flag == 0 || gr_info.block_type != 2) {
                i = 31;
                ss = 17;
                sb = 0;
                while (i >= 0) {
                    if (this.ro[1][i][ss] != 0.0f) {
                        sb = ((i << 4) + (i << 1)) + ss;
                        i = -1;
                    } else {
                        ss--;
                        if (ss < 0) {
                            i--;
                            ss = 17;
                        }
                    }
                }
                i = 0;
                while (this.sfBandIndex[this.sfreq].l[i] <= sb) {
                    i++;
                }
                i = this.sfBandIndex[this.sfreq].l[i];
                for (sfb = i; sfb < 21; sfb++) {
                    for (sb = this.sfBandIndex[this.sfreq].l[sfb + 1] - this.sfBandIndex[this.sfreq].l[sfb]; sb > 0; sb--) {
                        this.is_pos[i] = this.scalefac[1].l[sfb];
                        if (this.is_pos[i] != 7) {
                            if (lsf) {
                                i_stereo_k_values(this.is_pos[i], io_type, i);
                            } else {
                                this.is_ratio[i] = TAN12[this.is_pos[i]];
                            }
                        }
                        i++;
                    }
                }
                sfb = this.sfBandIndex[this.sfreq].l[20];
                for (sb = 576 - this.sfBandIndex[this.sfreq].l[21]; sb > 0 && i < 576; sb--) {
                    this.is_pos[i] = this.is_pos[sfb];
                    if (lsf) {
                        this.k[0][i] = this.k[0][sfb];
                        this.k[1][i] = this.k[1][sfb];
                    } else {
                        this.is_ratio[i] = this.is_ratio[sfb];
                    }
                    i++;
                }
            } else if (gr_info.mixed_block_flag != 0) {
                int max_sfb = 0;
                for (j = 0; j < 3; j++) {
                    sfbcnt = 2;
                    sfb = 12;
                    while (sfb >= 3) {
                        i = this.sfBandIndex[this.sfreq].s[sfb];
                        lines = this.sfBandIndex[this.sfreq].s[sfb + 1] - i;
                        i = (((i << 2) - i) + ((j + 1) * lines)) - 1;
                        while (lines > 0) {
                            if (this.ro[1][i / 18][i % 18] != 0.0f) {
                                sfbcnt = sfb;
                                sfb = -10;
                                lines = -10;
                            }
                            lines--;
                            i--;
                        }
                        sfb--;
                    }
                    sfb = sfbcnt + 1;
                    if (sfb > max_sfb) {
                        max_sfb = sfb;
                    }
                    while (sfb < 12) {
                        temp = this.sfBandIndex[this.sfreq].s[sfb];
                        sb = this.sfBandIndex[this.sfreq].s[sfb + 1] - temp;
                        i = ((temp << 2) - temp) + (j * sb);
                        while (sb > 0) {
                            this.is_pos[i] = this.scalefac[1].s[j][sfb];
                            if (this.is_pos[i] != 7) {
                                if (lsf) {
                                    i_stereo_k_values(this.is_pos[i], io_type, i);
                                } else {
                                    this.is_ratio[i] = TAN12[this.is_pos[i]];
                                }
                            }
                            i++;
                            sb--;
                        }
                        sfb++;
                    }
                    sfb = this.sfBandIndex[this.sfreq].s[10];
                    sfb = ((sfb << 2) - sfb) + (j * (this.sfBandIndex[this.sfreq].s[11] - sfb));
                    temp = this.sfBandIndex[this.sfreq].s[11];
                    sb = this.sfBandIndex[this.sfreq].s[12] - temp;
                    i = ((temp << 2) - temp) + (j * sb);
                    while (sb > 0) {
                        this.is_pos[i] = this.is_pos[sfb];
                        if (lsf) {
                            this.k[0][i] = this.k[0][sfb];
                            this.k[1][i] = this.k[1][sfb];
                        } else {
                            this.is_ratio[i] = this.is_ratio[sfb];
                        }
                        i++;
                        sb--;
                    }
                }
                if (max_sfb <= 3) {
                    i = 2;
                    ss = 17;
                    sb = -1;
                    while (i >= 0) {
                        if (this.ro[1][i][ss] != 0.0f) {
                            sb = ((i << 4) + (i << 1)) + ss;
                            i = -1;
                        } else {
                            ss--;
                            if (ss < 0) {
                                i--;
                                ss = 17;
                            }
                        }
                    }
                    i = 0;
                    while (this.sfBandIndex[this.sfreq].l[i] <= sb) {
                        i++;
                    }
                    i = this.sfBandIndex[this.sfreq].l[i];
                    for (sfb = i; sfb < 8; sfb++) {
                        for (sb = this.sfBandIndex[this.sfreq].l[sfb + 1] - this.sfBandIndex[this.sfreq].l[sfb]; sb > 0; sb--) {
                            this.is_pos[i] = this.scalefac[1].l[sfb];
                            if (this.is_pos[i] != 7) {
                                if (lsf) {
                                    i_stereo_k_values(this.is_pos[i], io_type, i);
                                } else {
                                    this.is_ratio[i] = TAN12[this.is_pos[i]];
                                }
                            }
                            i++;
                        }
                    }
                }
            } else {
                for (j = 0; j < 3; j++) {
                    sfbcnt = -1;
                    sfb = 12;
                    while (sfb >= 0) {
                        temp = this.sfBandIndex[this.sfreq].s[sfb];
                        lines = this.sfBandIndex[this.sfreq].s[sfb + 1] - temp;
                        i = (((temp << 2) - temp) + ((j + 1) * lines)) - 1;
                        while (lines > 0) {
                            if (this.ro[1][i / 18][i % 18] != 0.0f) {
                                sfbcnt = sfb;
                                sfb = -10;
                                lines = -10;
                            }
                            lines--;
                            i--;
                        }
                        sfb--;
                    }
                    for (sfb = sfbcnt + 1; sfb < 12; sfb++) {
                        temp = this.sfBandIndex[this.sfreq].s[sfb];
                        sb = this.sfBandIndex[this.sfreq].s[sfb + 1] - temp;
                        i = ((temp << 2) - temp) + (j * sb);
                        while (sb > 0) {
                            this.is_pos[i] = this.scalefac[1].s[j][sfb];
                            if (this.is_pos[i] != 7) {
                                if (lsf) {
                                    i_stereo_k_values(this.is_pos[i], io_type, i);
                                } else {
                                    this.is_ratio[i] = TAN12[this.is_pos[i]];
                                }
                            }
                            i++;
                            sb--;
                        }
                    }
                    temp = this.sfBandIndex[this.sfreq].s[10];
                    int temp2 = this.sfBandIndex[this.sfreq].s[11];
                    sfb = ((temp << 2) - temp) + (j * (temp2 - temp));
                    sb = this.sfBandIndex[this.sfreq].s[12] - temp2;
                    i = ((temp2 << 2) - temp2) + (j * sb);
                    while (sb > 0) {
                        this.is_pos[i] = this.is_pos[sfb];
                        if (lsf) {
                            this.k[0][i] = this.k[0][sfb];
                            this.k[1][i] = this.k[1][sfb];
                        } else {
                            this.is_ratio[i] = this.is_ratio[sfb];
                        }
                        i++;
                        sb--;
                    }
                }
            }
        }
        i = 0;
        for (sb = 0; sb < 32; sb++) {
            for (ss = 0; ss < 18; ss++) {
                if (this.is_pos[i] == 7) {
                    if (ms_stereo) {
                        this.lr[0][sb][ss] = (this.ro[0][sb][ss] + this.ro[1][sb][ss]) * 0.70710677f;
                        this.lr[1][sb][ss] = (this.ro[0][sb][ss] - this.ro[1][sb][ss]) * 0.70710677f;
                    } else {
                        this.lr[0][sb][ss] = this.ro[0][sb][ss];
                        this.lr[1][sb][ss] = this.ro[1][sb][ss];
                    }
                } else if (i_stereo) {
                    if (lsf) {
                        this.lr[0][sb][ss] = this.ro[0][sb][ss] * this.k[0][i];
                        this.lr[1][sb][ss] = this.ro[0][sb][ss] * this.k[1][i];
                    } else {
                        this.lr[1][sb][ss] = this.ro[0][sb][ss] / (1.0f + this.is_ratio[i]);
                        this.lr[0][sb][ss] = this.lr[1][sb][ss] * this.is_ratio[i];
                    }
                }
                i++;
            }
        }
    }

    private void antialias(int ch, int gr) {
        gr_info_s gr_info = this.si.ch[ch].gr[gr];
        if (gr_info.window_switching_flag == 0 || gr_info.block_type != 2 || gr_info.mixed_block_flag != 0) {
            int sb18lim;
            if (gr_info.window_switching_flag == 0 || gr_info.mixed_block_flag == 0 || gr_info.block_type != 2) {
                sb18lim = 558;
            } else {
                sb18lim = 18;
            }
            for (int sb18 = 0; sb18 < sb18lim; sb18 += 18) {
                for (int ss = 0; ss < 8; ss++) {
                    int src_idx1 = (sb18 + 17) - ss;
                    int src_idx2 = (sb18 + 18) + ss;
                    float bu = this.out_1d[src_idx1];
                    float bd = this.out_1d[src_idx2];
                    this.out_1d[src_idx1] = (cs[ss] * bu) - (ca[ss] * bd);
                    this.out_1d[src_idx2] = (cs[ss] * bd) + (ca[ss] * bu);
                }
            }
        }
    }

    private void hybrid(int ch, int gr) {
        gr_info_s gr_info = this.si.ch[ch].gr[gr];
        int sb18 = 0;
        while (sb18 < 576) {
            int cc;
            int bt = (gr_info.window_switching_flag == 0 || gr_info.mixed_block_flag == 0 || sb18 >= 36) ? gr_info.block_type : 0;
            float[] tsOut = this.out_1d;
            for (cc = 0; cc < 18; cc++) {
                this.tsOutCopy[cc] = tsOut[cc + sb18];
            }
            inv_mdct(this.tsOutCopy, this.rawout, bt);
            for (cc = 0; cc < 18; cc++) {
                tsOut[cc + sb18] = this.tsOutCopy[cc];
            }
            float[][] prvblk = this.prevblck;
            tsOut[sb18 + 0] = this.rawout[0] + prvblk[ch][sb18 + 0];
            prvblk[ch][sb18 + 0] = this.rawout[18];
            tsOut[sb18 + 1] = this.rawout[1] + prvblk[ch][sb18 + 1];
            prvblk[ch][sb18 + 1] = this.rawout[19];
            tsOut[sb18 + 2] = this.rawout[2] + prvblk[ch][sb18 + 2];
            prvblk[ch][sb18 + 2] = this.rawout[20];
            tsOut[sb18 + 3] = this.rawout[3] + prvblk[ch][sb18 + 3];
            prvblk[ch][sb18 + 3] = this.rawout[21];
            tsOut[sb18 + 4] = this.rawout[4] + prvblk[ch][sb18 + 4];
            prvblk[ch][sb18 + 4] = this.rawout[22];
            tsOut[sb18 + 5] = this.rawout[5] + prvblk[ch][sb18 + 5];
            prvblk[ch][sb18 + 5] = this.rawout[23];
            tsOut[sb18 + 6] = this.rawout[6] + prvblk[ch][sb18 + 6];
            prvblk[ch][sb18 + 6] = this.rawout[24];
            tsOut[sb18 + 7] = this.rawout[7] + prvblk[ch][sb18 + 7];
            prvblk[ch][sb18 + 7] = this.rawout[25];
            tsOut[sb18 + 8] = this.rawout[8] + prvblk[ch][sb18 + 8];
            prvblk[ch][sb18 + 8] = this.rawout[26];
            tsOut[sb18 + 9] = this.rawout[9] + prvblk[ch][sb18 + 9];
            prvblk[ch][sb18 + 9] = this.rawout[27];
            tsOut[sb18 + 10] = this.rawout[10] + prvblk[ch][sb18 + 10];
            prvblk[ch][sb18 + 10] = this.rawout[28];
            tsOut[sb18 + 11] = this.rawout[11] + prvblk[ch][sb18 + 11];
            prvblk[ch][sb18 + 11] = this.rawout[29];
            tsOut[sb18 + 12] = this.rawout[12] + prvblk[ch][sb18 + 12];
            prvblk[ch][sb18 + 12] = this.rawout[30];
            tsOut[sb18 + 13] = this.rawout[13] + prvblk[ch][sb18 + 13];
            prvblk[ch][sb18 + 13] = this.rawout[31];
            tsOut[sb18 + 14] = this.rawout[14] + prvblk[ch][sb18 + 14];
            prvblk[ch][sb18 + 14] = this.rawout[32];
            tsOut[sb18 + 15] = this.rawout[15] + prvblk[ch][sb18 + 15];
            prvblk[ch][sb18 + 15] = this.rawout[33];
            tsOut[sb18 + 16] = this.rawout[16] + prvblk[ch][sb18 + 16];
            prvblk[ch][sb18 + 16] = this.rawout[34];
            tsOut[sb18 + 17] = this.rawout[17] + prvblk[ch][sb18 + 17];
            prvblk[ch][sb18 + 17] = this.rawout[35];
            sb18 += 18;
        }
    }

    private void do_downmix() {
        for (int sb = 0; sb < 18; sb++) {
            for (int ss = 0; ss < 18; ss += 3) {
                this.lr[0][sb][ss] = (this.lr[0][sb][ss] + this.lr[1][sb][ss]) * 0.5f;
                this.lr[0][sb][ss + 1] = (this.lr[0][sb][ss + 1] + this.lr[1][sb][ss + 1]) * 0.5f;
                this.lr[0][sb][ss + 2] = (this.lr[0][sb][ss + 2] + this.lr[1][sb][ss + 2]) * 0.5f;
            }
        }
    }

    public void inv_mdct(float[] in, float[] out, int block_type) {
        float tmpf_16 = 0.0f;
        float tmpf_15 = 0.0f;
        float tmpf_14 = 0.0f;
        float tmpf_13 = 0.0f;
        float tmpf_12 = 0.0f;
        float tmpf_11 = 0.0f;
        float tmpf_10 = 0.0f;
        float tmpf_9 = 0.0f;
        float tmpf_8 = 0.0f;
        float tmpf_7 = 0.0f;
        float tmpf_6 = 0.0f;
        float tmpf_5 = 0.0f;
        float tmpf_4 = 0.0f;
        float tmpf_3 = 0.0f;
        float tmpf_2 = 0.0f;
        float tmpf_1 = 0.0f;
        float tmpf_0 = 0.0f;
        if (block_type == 2) {
            out[0] = 0.0f;
            out[1] = 0.0f;
            out[2] = 0.0f;
            out[3] = 0.0f;
            out[4] = 0.0f;
            out[5] = 0.0f;
            out[6] = 0.0f;
            out[7] = 0.0f;
            out[8] = 0.0f;
            out[9] = 0.0f;
            out[10] = 0.0f;
            out[11] = 0.0f;
            out[12] = 0.0f;
            out[13] = 0.0f;
            out[14] = 0.0f;
            out[15] = 0.0f;
            out[16] = 0.0f;
            out[17] = 0.0f;
            out[18] = 0.0f;
            out[19] = 0.0f;
            out[20] = 0.0f;
            out[21] = 0.0f;
            out[22] = 0.0f;
            out[23] = 0.0f;
            out[24] = 0.0f;
            out[25] = 0.0f;
            out[26] = 0.0f;
            out[27] = 0.0f;
            out[28] = 0.0f;
            out[29] = 0.0f;
            out[30] = 0.0f;
            out[31] = 0.0f;
            out[32] = 0.0f;
            out[33] = 0.0f;
            out[34] = 0.0f;
            out[35] = 0.0f;
            int six_i = 0;
            for (int i = 0; i < 3; i++) {
                int i2 = i + 15;
                in[i2] = in[i2] + in[i + 12];
                i2 = i + 12;
                in[i2] = in[i2] + in[i + 9];
                i2 = i + 9;
                in[i2] = in[i2] + in[i + 6];
                i2 = i + 6;
                in[i2] = in[i2] + in[i + 3];
                i2 = i + 3;
                in[i2] = in[i2] + in[i + 0];
                i2 = i + 15;
                in[i2] = in[i2] + in[i + 9];
                i2 = i + 9;
                in[i2] = in[i2] + in[i + 3];
                float pp1 = in[i + 6] * 0.8660254f;
                float sum = in[i + 0] + (in[i + 12] * 0.5f);
                tmpf_1 = in[i + 0] - in[i + 12];
                tmpf_0 = sum + pp1;
                tmpf_2 = sum - pp1;
                pp1 = in[i + 9] * 0.8660254f;
                sum = in[i + 3] + (in[i + 15] * 0.5f);
                tmpf_3 = (sum - pp1) * 1.9318516f;
                tmpf_4 = (in[i + 3] - in[i + 15]) * 0.70710677f;
                tmpf_5 = (sum + pp1) * 0.5176381f;
                float save = tmpf_0;
                tmpf_0 += tmpf_5;
                tmpf_5 = save - tmpf_5;
                save = tmpf_1;
                tmpf_1 += tmpf_4;
                tmpf_4 = save - tmpf_4;
                save = tmpf_2;
                tmpf_0 *= 0.5043145f;
                tmpf_1 *= 0.5411961f;
                tmpf_2 = (tmpf_2 + tmpf_3) * 0.6302362f;
                tmpf_4 *= 1.306563f;
                tmpf_5 *= 3.830649f;
                tmpf_8 = (-tmpf_0) * 0.7933533f;
                tmpf_9 = (-tmpf_0) * 0.6087614f;
                tmpf_7 = (-tmpf_1) * 0.9238795f;
                tmpf_10 = (-tmpf_1) * 0.38268343f;
                tmpf_6 = (-tmpf_2) * 0.9914449f;
                tmpf_11 = (-tmpf_2) * 0.13052619f;
                tmpf_0 = (save - tmpf_3) * 0.8213398f;
                tmpf_1 = tmpf_4 * 0.38268343f;
                tmpf_2 = tmpf_5 * 0.6087614f;
                tmpf_3 = (-tmpf_5) * 0.7933533f;
                tmpf_4 = (-tmpf_4) * 0.9238795f;
                tmpf_5 = (-tmpf_0) * 0.9914449f;
                i2 = six_i + 6;
                out[i2] = out[i2] + (tmpf_0 * 0.13052619f);
                i2 = six_i + 7;
                out[i2] = out[i2] + tmpf_1;
                i2 = six_i + 8;
                out[i2] = out[i2] + tmpf_2;
                i2 = six_i + 9;
                out[i2] = out[i2] + tmpf_3;
                i2 = six_i + 10;
                out[i2] = out[i2] + tmpf_4;
                i2 = six_i + 11;
                out[i2] = out[i2] + tmpf_5;
                i2 = six_i + 12;
                out[i2] = out[i2] + tmpf_6;
                i2 = six_i + 13;
                out[i2] = out[i2] + tmpf_7;
                i2 = six_i + 14;
                out[i2] = out[i2] + tmpf_8;
                i2 = six_i + 15;
                out[i2] = out[i2] + tmpf_9;
                i2 = six_i + 16;
                out[i2] = out[i2] + tmpf_10;
                i2 = six_i + 17;
                out[i2] = out[i2] + tmpf_11;
                six_i += 6;
            }
            return;
        }
        in[17] = in[17] + in[16];
        in[16] = in[16] + in[15];
        in[15] = in[15] + in[14];
        in[14] = in[14] + in[13];
        in[13] = in[13] + in[12];
        in[12] = in[12] + in[11];
        in[11] = in[11] + in[10];
        in[10] = in[10] + in[9];
        in[9] = in[9] + in[8];
        in[8] = in[8] + in[7];
        in[7] = in[7] + in[6];
        in[6] = in[6] + in[5];
        in[5] = in[5] + in[4];
        in[4] = in[4] + in[3];
        in[3] = in[3] + in[2];
        in[2] = in[2] + in[1];
        in[1] = in[1] + in[0];
        in[17] = in[17] + in[15];
        in[15] = in[15] + in[13];
        in[13] = in[13] + in[11];
        in[11] = in[11] + in[9];
        in[9] = in[9] + in[7];
        in[7] = in[7] + in[5];
        in[5] = in[5] + in[3];
        in[3] = in[3] + in[1];
        float i00 = in[0] + in[0];
        float iip12 = i00 + in[12];
        float tmp0 = (((in[4] * 1.8793852f) + iip12) + (in[8] * 1.5320889f)) + (in[16] * 0.34729636f);
        float tmp1 = ((((in[4] + i00) - in[8]) - in[12]) - in[12]) - in[16];
        float tmp2 = ((iip12 - (in[4] * 0.34729636f)) - (in[8] * 1.8793852f)) + (in[16] * 1.5320889f);
        float tmp3 = ((iip12 - (in[4] * 1.5320889f)) + (in[8] * 0.34729636f)) - (in[16] * 1.8793852f);
        float tmp4 = (((in[0] - in[4]) + in[8]) - in[12]) + in[16];
        float i66_ = in[6] * 1.7320508f;
        float tmp0_ = (((in[2] * 1.9696155f) + i66_) + (in[10] * 1.2855753f)) + (in[14] * 0.6840403f);
        float tmp1_ = ((in[2] - in[10]) - in[14]) * 1.7320508f;
        float tmp2_ = (((in[2] * 1.2855753f) - i66_) - (in[10] * 0.6840403f)) + (in[14] * 1.9696155f);
        float tmp3_ = (((in[2] * 0.6840403f) - i66_) + (in[10] * 1.9696155f)) - (in[14] * 1.2855753f);
        float i0 = in[1] + in[1];
        float i0p12 = i0 + in[13];
        float tmp0o = (((in[5] * 1.8793852f) + i0p12) + (in[9] * 1.5320889f)) + (in[17] * 0.34729636f);
        float tmp1o = ((((in[5] + i0) - in[9]) - in[13]) - in[13]) - in[17];
        float tmp2o = ((i0p12 - (in[5] * 0.34729636f)) - (in[9] * 1.8793852f)) + (in[17] * 1.5320889f);
        float tmp3o = ((i0p12 - (in[5] * 1.5320889f)) + (in[9] * 0.34729636f)) - (in[17] * 1.8793852f);
        float tmp4o = ((((in[1] - in[5]) + in[9]) - in[13]) + in[17]) * 0.70710677f;
        float i6_ = in[7] * 1.7320508f;
        float tmp0_o = (((in[3] * 1.9696155f) + i6_) + (in[11] * 1.2855753f)) + (in[15] * 0.6840403f);
        float tmp1_o = ((in[3] - in[11]) - in[15]) * 1.7320508f;
        float tmp2_o = (((in[3] * 1.2855753f) - i6_) - (in[11] * 0.6840403f)) + (in[15] * 1.9696155f);
        float tmp3_o = (((in[3] * 0.6840403f) - i6_) + (in[11] * 1.9696155f)) - (in[15] * 1.2855753f);
        float e = tmp0 + tmp0_;
        float o = (tmp0o + tmp0_o) * 0.5019099f;
        tmpf_0 = e + o;
        float tmpf_17 = e - o;
        e = tmp1 + tmp1_;
        o = (tmp1o + tmp1_o) * 0.5176381f;
        tmpf_1 = e + o;
        tmpf_16 = e - o;
        e = tmp2 + tmp2_;
        o = (tmp2o + tmp2_o) * 0.55168897f;
        tmpf_2 = e + o;
        tmpf_15 = e - o;
        e = tmp3 + tmp3_;
        o = (tmp3o + tmp3_o) * 0.61038727f;
        tmpf_3 = e + o;
        tmpf_14 = e - o;
        tmpf_4 = tmp4 + tmp4o;
        tmpf_13 = tmp4 - tmp4o;
        e = tmp3 - tmp3_;
        o = (tmp3o - tmp3_o) * 0.8717234f;
        tmpf_5 = e + o;
        tmpf_12 = e - o;
        e = tmp2 - tmp2_;
        o = (tmp2o - tmp2_o) * 1.1831008f;
        tmpf_6 = e + o;
        tmpf_11 = e - o;
        e = tmp1 - tmp1_;
        o = (tmp1o - tmp1_o) * 1.9318516f;
        tmpf_7 = e + o;
        tmpf_10 = e - o;
        e = tmp0 - tmp0_;
        o = (tmp0o - tmp0_o) * 5.7368565f;
        tmpf_8 = e + o;
        tmpf_9 = e - o;
        float[] win_bt = win[block_type];
        out[0] = (-tmpf_9) * win_bt[0];
        out[1] = (-tmpf_10) * win_bt[1];
        out[2] = (-tmpf_11) * win_bt[2];
        out[3] = (-tmpf_12) * win_bt[3];
        out[4] = (-tmpf_13) * win_bt[4];
        out[5] = (-tmpf_14) * win_bt[5];
        out[6] = (-tmpf_15) * win_bt[6];
        out[7] = (-tmpf_16) * win_bt[7];
        out[8] = (-tmpf_17) * win_bt[8];
        out[9] = win_bt[9] * tmpf_17;
        out[10] = win_bt[10] * tmpf_16;
        out[11] = win_bt[11] * tmpf_15;
        out[12] = win_bt[12] * tmpf_14;
        out[13] = win_bt[13] * tmpf_13;
        out[14] = win_bt[14] * tmpf_12;
        out[15] = win_bt[15] * tmpf_11;
        out[16] = win_bt[16] * tmpf_10;
        out[17] = win_bt[17] * tmpf_9;
        out[18] = win_bt[18] * tmpf_8;
        out[19] = win_bt[19] * tmpf_7;
        out[20] = win_bt[20] * tmpf_6;
        out[21] = win_bt[21] * tmpf_5;
        out[22] = win_bt[22] * tmpf_4;
        out[23] = win_bt[23] * tmpf_3;
        out[24] = win_bt[24] * tmpf_2;
        out[25] = win_bt[25] * tmpf_1;
        out[26] = win_bt[26] * tmpf_0;
        out[27] = win_bt[27] * tmpf_0;
        out[28] = win_bt[28] * tmpf_1;
        out[29] = win_bt[29] * tmpf_2;
        out[30] = win_bt[30] * tmpf_3;
        out[31] = win_bt[31] * tmpf_4;
        out[32] = win_bt[32] * tmpf_5;
        out[33] = win_bt[33] * tmpf_6;
        out[34] = win_bt[34] * tmpf_7;
        out[35] = win_bt[35] * tmpf_8;
    }

    static {
        r0 = new int[2][];
        r1 = new int[16];
        r1 = new int[16];
        r1[1] = 1;
        r1[2] = 2;
        r1[3] = 3;
        r1[5] = 1;
        r1[6] = 2;
        r1[7] = 3;
        r1[8] = 1;
        r1[9] = 2;
        r1[10] = 3;
        r1[11] = 1;
        r1[12] = 2;
        r1[13] = 3;
        r1[14] = 2;
        r1[15] = 3;
        r0[1] = r1;
        slen = r0;
        r0 = new int[22];
        pretab = r0;
        int[][][] iArr = new int[6][][];
        iArr[0] = new int[][]{new int[]{6, 5, 5, 5}, new int[]{9, 9, 9, 9}, new int[]{6, 9, 9, 9}};
        iArr[1] = new int[][]{new int[]{6, 5, 7, 3}, new int[]{9, 9, 12, 6}, new int[]{6, 9, 12, 6}};
        r1 = new int[3][];
        int[] iArr2 = new int[4];
        iArr2[0] = 11;
        iArr2[1] = 10;
        r1[0] = iArr2;
        iArr2 = new int[4];
        iArr2[0] = 18;
        iArr2[1] = 18;
        r1[1] = iArr2;
        iArr2 = new int[]{15, 18, iArr2, r1};
        r1 = new int[3][];
        iArr2 = new int[]{7, 7, 7, iArr2};
        iArr2 = new int[]{12, 12, 12, iArr2};
        iArr2 = new int[]{6, 15, 12, iArr2};
        iArr[3] = r1;
        iArr[4] = new int[][]{new int[]{6, 6, 6, 3}, new int[]{12, 9, 9, 6}, new int[]{6, 12, 9, 6}};
        r2 = new int[3][];
        int[] iArr3 = new int[]{8, 8, 5, iArr3};
        iArr3 = new int[]{15, 12, 9, iArr3};
        iArr3 = new int[]{6, 18, 9, iArr3};
        iArr[5] = r2;
        nr_of_sfb_block = iArr;
    }

    private static float[] create_t_43() {
        float[] t43 = new float[8192];
        for (int i = 0; i < 8192; i++) {
            t43[i] = (float) Math.pow((double) i, 1.3333333333333333d);
        }
        return t43;
    }

    static int[] reorder(int[] scalefac_band) {
        int j = 0;
        int[] ix = new int[576];
        for (int sfb = 0; sfb < 13; sfb++) {
            int start = scalefac_band[sfb];
            int end = scalefac_band[sfb + 1];
            int window = 0;
            while (window < 3) {
                int i = start;
                int j2 = j;
                while (i < end) {
                    j = j2 + 1;
                    ix[(i * 3) + window] = j2;
                    i++;
                    j2 = j;
                }
                window++;
                j = j2;
            }
        }
        return ix;
    }
}
