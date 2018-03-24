package com.foxit.sdk.common;

public class Font {
    public static final int e_fontCharsetANSI = 0;
    public static final int e_fontCharsetArabic = 178;
    public static final int e_fontCharsetBaltic = 186;
    public static final int e_fontCharsetChineseBig5 = 136;
    public static final int e_fontCharsetDefault = 1;
    public static final int e_fontCharsetEastEurope = 238;
    public static final int e_fontCharsetGB2312 = 134;
    public static final int e_fontCharsetGreek = 161;
    public static final int e_fontCharsetHangeul = 129;
    public static final int e_fontCharsetHebrew = 177;
    public static final int e_fontCharsetRussian = 204;
    public static final int e_fontCharsetShift_JIS = 128;
    public static final int e_fontCharsetSymbol = 2;
    public static final int e_fontCharsetThai = 222;
    public static final int e_fontCharsetTurkish = 162;
    public static final int e_fontStandardIDCourier = 0;
    public static final int e_fontStandardIDCourierB = 1;
    public static final int e_fontStandardIDCourierBI = 2;
    public static final int e_fontStandardIDCourierI = 3;
    public static final int e_fontStandardIDHelvetica = 4;
    public static final int e_fontStandardIDHelveticaB = 5;
    public static final int e_fontStandardIDHelveticaBI = 6;
    public static final int e_fontStandardIDHelveticaI = 7;
    public static final int e_fontStandardIDSymbol = 12;
    public static final int e_fontStandardIDTimes = 8;
    public static final int e_fontStandardIDTimesB = 9;
    public static final int e_fontStandardIDTimesBI = 10;
    public static final int e_fontStandardIDTimesI = 11;
    public static final int e_fontStandardIDZapfDingbats = 13;
    public static final int e_fontStyleAllCap = 65536;
    public static final int e_fontStyleFixedPitch = 1;
    public static final int e_fontStyleItalic = 64;
    public static final int e_fontStyleNonSymbolic = 32;
    public static final int e_fontStyleScript = 8;
    public static final int e_fontStyleSerif = 2;
    public static final int e_fontStyleSymbolic = 4;
    public static final int e_fontStylesBold = 262144;
    public static final int e_fontStylesSmallCap = 131072;
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    protected Font(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(Font font) {
        return font == null ? 0 : font.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                CommonJNI.delete_Font(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    private static boolean a(int i) {
        return i < 0 || i > 13;
    }

    private static boolean a(long j) {
        return j < 0;
    }

    private static boolean b(int i) {
        return (i == 0 || i == 1 || i == 2 || i == 128 || i == 129 || i == e_fontCharsetGB2312 || i == e_fontCharsetChineseBig5 || i == e_fontCharsetThai || i == e_fontCharsetEastEurope || i == 204 || i == e_fontCharsetGreek || i == e_fontCharsetTurkish || i == e_fontCharsetHebrew || i == e_fontCharsetArabic || i == e_fontCharsetBaltic) ? false : true;
    }

    public static Font create(String str, long j, int i, int i2) throws PDFException {
        if (str == null || str.trim().length() < 1 || a(j) || i < 0 || b(i2)) {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
        long Font_create = CommonJNI.Font_create(str, j, i, i2);
        return Font_create == 0 ? null : new Font(Font_create, false);
    }

    public static Font createStandard(int i) throws PDFException {
        if (a(i)) {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
        long Font_createStandard = CommonJNI.Font_createStandard(i);
        return Font_createStandard == 0 ? null : new Font(Font_createStandard, false);
    }

    public void release() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        CommonJNI.Font_release(this.swigCPtr, this);
        this.swigCPtr = 0;
    }

    public String getName() throws PDFException {
        if (this.swigCPtr != 0) {
            return CommonJNI.Font_getName(this.swigCPtr, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }
}
