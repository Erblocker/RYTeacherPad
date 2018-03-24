package net.majorkernelpanic.streaming.hw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;

@SuppressLint({"NewApi"})
public class EncoderDebugger {
    private static final int BITRATE = 1000000;
    private static final boolean DEBUG = false;
    private static final int FRAMERATE = 15;
    private static final String MIME_TYPE = "video/avc";
    private static final int NB_DECODED = 34;
    private static final int NB_ENCODED = 50;
    private static final String PREF_PREFIX = "libstreaming-";
    public static final String TAG = "EncoderDebugger";
    private static final boolean VERBOSE = true;
    private static final int VERSION = 3;
    private String mB64PPS;
    private String mB64SPS;
    private byte[] mData;
    private MediaFormat mDecOutputFormat;
    private byte[][] mDecodedVideo;
    private MediaCodec mDecoder;
    private int mDecoderColorFormat;
    private String mDecoderName;
    private MediaCodec mEncoder;
    private int mEncoderColorFormat;
    private String mEncoderName;
    private String mErrorLog;
    private int mHeight;
    private byte[] mInitialImage;
    private NV21Convertor mNV21;
    private byte[] mPPS;
    private SharedPreferences mPreferences;
    private byte[] mSPS;
    private int mSize;
    private byte[][] mVideo;
    private int mWidth;

    /* renamed from: net.majorkernelpanic.streaming.hw.EncoderDebugger$1 */
    class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Context val$context;
        private final /* synthetic */ int val$height;
        private final /* synthetic */ int val$width;

        AnonymousClass1(Context context, int i, int i2) {
            this.val$context = context;
            this.val$width = i;
            this.val$height = i2;
        }

        public void run() {
            try {
                EncoderDebugger.debug(PreferenceManager.getDefaultSharedPreferences(this.val$context), this.val$width, this.val$height);
            } catch (Exception e) {
            }
        }
    }

    private void debug() {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r19 = this;
        r15 = r19.checkTestNeeded();
        if (r15 != 0) goto L_0x0219;
    L_0x0006:
        r15 = new java.lang.StringBuilder;
        r0 = r19;
        r0 = r0.mWidth;
        r16 = r0;
        r16 = java.lang.String.valueOf(r16);
        r15.<init>(r16);
        r16 = "x";
        r15 = r15.append(r16);
        r0 = r19;
        r0 = r0.mHeight;
        r16 = r0;
        r15 = r15.append(r16);
        r16 = "-";
        r15 = r15.append(r16);
        r10 = r15.toString();
        r0 = r19;
        r15 = r0.mPreferences;
        r16 = new java.lang.StringBuilder;
        r17 = "libstreaming-";
        r16.<init>(r17);
        r0 = r16;
        r16 = r0.append(r10);
        r17 = "success";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = 0;
        r13 = r15.getBoolean(r16, r17);
        if (r13 != 0) goto L_0x008a;
    L_0x0056:
        r15 = new java.lang.RuntimeException;
        r16 = new java.lang.StringBuilder;
        r17 = "Phone not supported with this resolution (";
        r16.<init>(r17);
        r0 = r19;
        r0 = r0.mWidth;
        r17 = r0;
        r16 = r16.append(r17);
        r17 = "x";
        r16 = r16.append(r17);
        r0 = r19;
        r0 = r0.mHeight;
        r17 = r0;
        r16 = r16.append(r17);
        r17 = ")";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r15.<init>(r16);
        throw r15;
    L_0x008a:
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mWidth;
        r16 = r0;
        r0 = r19;
        r0 = r0.mHeight;
        r17 = r0;
        r15.setSize(r16, r17);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mPreferences;
        r16 = r0;
        r17 = new java.lang.StringBuilder;
        r18 = "libstreaming-";
        r17.<init>(r18);
        r0 = r17;
        r17 = r0.append(r10);
        r18 = "sliceHeight";
        r17 = r17.append(r18);
        r17 = r17.toString();
        r18 = 0;
        r16 = r16.getInt(r17, r18);
        r15.setSliceHeigth(r16);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mPreferences;
        r16 = r0;
        r17 = new java.lang.StringBuilder;
        r18 = "libstreaming-";
        r17.<init>(r18);
        r0 = r17;
        r17 = r0.append(r10);
        r18 = "stride";
        r17 = r17.append(r18);
        r17 = r17.toString();
        r18 = 0;
        r16 = r16.getInt(r17, r18);
        r15.setStride(r16);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mPreferences;
        r16 = r0;
        r17 = new java.lang.StringBuilder;
        r18 = "libstreaming-";
        r17.<init>(r18);
        r0 = r17;
        r17 = r0.append(r10);
        r18 = "padding";
        r17 = r17.append(r18);
        r17 = r17.toString();
        r18 = 0;
        r16 = r16.getInt(r17, r18);
        r15.setYPadding(r16);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mPreferences;
        r16 = r0;
        r17 = new java.lang.StringBuilder;
        r18 = "libstreaming-";
        r17.<init>(r18);
        r0 = r17;
        r17 = r0.append(r10);
        r18 = "planar";
        r17 = r17.append(r18);
        r17 = r17.toString();
        r18 = 0;
        r16 = r16.getBoolean(r17, r18);
        r15.setPlanar(r16);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mPreferences;
        r16 = r0;
        r17 = new java.lang.StringBuilder;
        r18 = "libstreaming-";
        r17.<init>(r18);
        r0 = r17;
        r17 = r0.append(r10);
        r18 = "reversed";
        r17 = r17.append(r18);
        r17 = r17.toString();
        r18 = 0;
        r16 = r16.getBoolean(r17, r18);
        r15.setColorPanesReversed(r16);
        r0 = r19;
        r15 = r0.mPreferences;
        r16 = new java.lang.StringBuilder;
        r17 = "libstreaming-";
        r16.<init>(r17);
        r0 = r16;
        r16 = r0.append(r10);
        r17 = "encoderName";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = "";
        r15 = r15.getString(r16, r17);
        r0 = r19;
        r0.mEncoderName = r15;
        r0 = r19;
        r15 = r0.mPreferences;
        r16 = new java.lang.StringBuilder;
        r17 = "libstreaming-";
        r16.<init>(r17);
        r0 = r16;
        r16 = r0.append(r10);
        r17 = "colorFormat";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = 0;
        r15 = r15.getInt(r16, r17);
        r0 = r19;
        r0.mEncoderColorFormat = r15;
        r0 = r19;
        r15 = r0.mPreferences;
        r16 = new java.lang.StringBuilder;
        r17 = "libstreaming-";
        r16.<init>(r17);
        r0 = r16;
        r16 = r0.append(r10);
        r17 = "pps";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = "";
        r15 = r15.getString(r16, r17);
        r0 = r19;
        r0.mB64PPS = r15;
        r0 = r19;
        r15 = r0.mPreferences;
        r16 = new java.lang.StringBuilder;
        r17 = "libstreaming-";
        r16.<init>(r17);
        r0 = r16;
        r16 = r0.append(r10);
        r17 = "sps";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = "";
        r15 = r15.getString(r16, r17);
        r0 = r19;
        r0.mB64SPS = r15;
    L_0x0218:
        return;
    L_0x0219:
        r15 = "EncoderDebugger";
        r16 = new java.lang.StringBuilder;
        r17 = ">>>> Testing the phone for resolution ";
        r16.<init>(r17);
        r0 = r19;
        r0 = r0.mWidth;
        r17 = r0;
        r16 = r16.append(r17);
        r17 = "x";
        r16 = r16.append(r17);
        r0 = r19;
        r0 = r0.mHeight;
        r17 = r0;
        r16 = r16.append(r17);
        r16 = r16.toString();
        android.util.Log.d(r15, r16);
        r15 = "video/avc";
        r4 = net.majorkernelpanic.streaming.hw.CodecManager.findEncodersForMimeType(r15);
        r15 = "video/avc";
        r2 = net.majorkernelpanic.streaming.hw.CodecManager.findDecodersForMimeType(r15);
        r1 = 0;
        r7 = 1;
        r5 = 0;
    L_0x0257:
        r15 = r4.length;
        if (r5 < r15) goto L_0x02be;
    L_0x025a:
        r5 = 0;
    L_0x025b:
        r15 = r4.length;
        if (r5 < r15) goto L_0x02c7;
    L_0x025e:
        r15 = 0;
        r0 = r19;
        r0.saveTestResult(r15);
        r15 = "EncoderDebugger";
        r16 = new java.lang.StringBuilder;
        r17 = "No usable encoder were found on the phone for resolution ";
        r16.<init>(r17);
        r0 = r19;
        r0 = r0.mWidth;
        r17 = r0;
        r16 = r16.append(r17);
        r17 = "x";
        r16 = r16.append(r17);
        r0 = r19;
        r0 = r0.mHeight;
        r17 = r0;
        r16 = r16.append(r17);
        r16 = r16.toString();
        android.util.Log.e(r15, r16);
        r15 = new java.lang.RuntimeException;
        r16 = new java.lang.StringBuilder;
        r17 = "No usable encoder were found on the phone for resolution ";
        r16.<init>(r17);
        r0 = r19;
        r0 = r0.mWidth;
        r17 = r0;
        r16 = r16.append(r17);
        r17 = "x";
        r16 = r16.append(r17);
        r0 = r19;
        r0 = r0.mHeight;
        r17 = r0;
        r16 = r16.append(r17);
        r16 = r16.toString();
        r15.<init>(r16);
        throw r15;
    L_0x02be:
        r15 = r4[r5];
        r15 = r15.formats;
        r15 = r15.length;
        r1 = r1 + r15;
        r5 = r5 + 1;
        goto L_0x0257;
    L_0x02c7:
        r6 = 0;
    L_0x02c8:
        r15 = r4[r5];
        r15 = r15.formats;
        r15 = r15.length;
        if (r6 < r15) goto L_0x02d2;
    L_0x02cf:
        r5 = r5 + 1;
        goto L_0x025b;
    L_0x02d2:
        r19.reset();
        r15 = r4[r5];
        r15 = r15.name;
        r0 = r19;
        r0.mEncoderName = r15;
        r15 = r4[r5];
        r15 = r15.formats;
        r15 = r15[r6];
        r15 = r15.intValue();
        r0 = r19;
        r0.mEncoderColorFormat = r15;
        r15 = "EncoderDebugger";
        r16 = new java.lang.StringBuilder;
        r17 = ">> Test ";
        r16.<init>(r17);
        r8 = r7 + 1;
        r0 = r16;
        r16 = r0.append(r7);
        r17 = "/";
        r16 = r16.append(r17);
        r0 = r16;
        r16 = r0.append(r1);
        r17 = ": ";
        r16 = r16.append(r17);
        r0 = r19;
        r0 = r0.mEncoderName;
        r17 = r0;
        r16 = r16.append(r17);
        r17 = " with color format ";
        r16 = r16.append(r17);
        r0 = r19;
        r0 = r0.mEncoderColorFormat;
        r17 = r0;
        r16 = r16.append(r17);
        r17 = " at ";
        r16 = r16.append(r17);
        r0 = r19;
        r0 = r0.mWidth;
        r17 = r0;
        r16 = r16.append(r17);
        r17 = "x";
        r16 = r16.append(r17);
        r0 = r19;
        r0 = r0.mHeight;
        r17 = r0;
        r16 = r16.append(r17);
        r16 = r16.toString();
        android.util.Log.v(r15, r16);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mWidth;
        r16 = r0;
        r0 = r19;
        r0 = r0.mHeight;
        r17 = r0;
        r15.setSize(r16, r17);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mHeight;
        r16 = r0;
        r15.setSliceHeigth(r16);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mWidth;
        r16 = r0;
        r15.setStride(r16);
        r0 = r19;
        r15 = r0.mNV21;
        r16 = 0;
        r15.setYPadding(r16);
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mEncoderColorFormat;
        r16 = r0;
        r15.setEncoderColorFormat(r16);
        r19.createTestImage();
        r0 = r19;
        r15 = r0.mNV21;
        r0 = r19;
        r0 = r0.mInitialImage;
        r16 = r0;
        r15 = r15.convert(r16);
        r0 = r19;
        r0.mData = r15;
        r19.configureEncoder();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r19.searchSPSandPPS();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = "EncoderDebugger";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = "SPS and PPS in b64: SPS=";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16.<init>(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r0.mB64SPS;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = r0;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = ", PPS=";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r0.mB64PPS;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = r0;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.toString();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        android.util.Log.v(r15, r16);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r19.encode();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = 1;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0.saveTestResult(r15);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = "EncoderDebugger";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = "The encoder ";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16.<init>(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r0.mEncoderName;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = r0;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = " is usable with resolution ";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r0.mWidth;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = r0;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = "x";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r0.mHeight;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r17 = r0;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.append(r17);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r16.toString();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        android.util.Log.v(r15, r16);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r19.releaseEncoder();
        goto L_0x0218;
    L_0x042d:
        r3 = move-exception;
        r14 = new java.io.StringWriter;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r14.<init>();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r9 = new java.io.PrintWriter;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r9.<init>(r14);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r3.printStackTrace(r9);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r11 = r14.toString();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = "Encoder ";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15.<init>(r16);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r0.mEncoderName;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r0;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = r15.append(r16);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = " cannot be used with color format ";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = r15.append(r16);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r0.mEncoderColorFormat;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = r0;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = r15.append(r16);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r12 = r15.toString();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = "EncoderDebugger";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        android.util.Log.e(r15, r12, r3);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = r0.mErrorLog;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = java.lang.String.valueOf(r15);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r16;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0.<init>(r15);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r16;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = r0.append(r12);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r16 = "\n";	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = r15.append(r16);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = r15.append(r11);	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r15 = r15.toString();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0 = r19;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r0.mErrorLog = r15;	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r3.printStackTrace();	 Catch:{ Exception -> 0x042d, all -> 0x049f }
        r19.releaseEncoder();
        r6 = r6 + 1;
        r7 = r8;
        goto L_0x02c8;
    L_0x049f:
        r15 = move-exception;
        r19.releaseEncoder();
        throw r15;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.majorkernelpanic.streaming.hw.EncoderDebugger.debug():void");
    }

    public static synchronized void asyncDebug(Context context, int width, int height) {
        synchronized (EncoderDebugger.class) {
            new Thread(new AnonymousClass1(context, width, height)).start();
        }
    }

    public static synchronized EncoderDebugger debug(Context context, int width, int height) {
        EncoderDebugger debug;
        synchronized (EncoderDebugger.class) {
            debug = debug(PreferenceManager.getDefaultSharedPreferences(context), width, height);
        }
        return debug;
    }

    public static synchronized EncoderDebugger debug(SharedPreferences prefs, int width, int height) {
        EncoderDebugger debugger;
        synchronized (EncoderDebugger.class) {
            debugger = new EncoderDebugger(prefs, width, height);
            debugger.debug();
        }
        return debugger;
    }

    public String getB64PPS() {
        return this.mB64PPS;
    }

    public String getB64SPS() {
        return this.mB64SPS;
    }

    public String getEncoderName() {
        return this.mEncoderName;
    }

    public int getEncoderColorFormat() {
        return this.mEncoderColorFormat;
    }

    public NV21Convertor getNV21Convertor() {
        return this.mNV21;
    }

    public String getErrorLog() {
        return this.mErrorLog;
    }

    private EncoderDebugger(SharedPreferences prefs, int width, int height) {
        this.mPreferences = prefs;
        this.mWidth = width;
        this.mHeight = height;
        this.mSize = width * height;
        reset();
    }

    private void reset() {
        this.mNV21 = new NV21Convertor();
        this.mVideo = new byte[50][];
        this.mDecodedVideo = new byte[34][];
        this.mErrorLog = "";
        this.mPPS = null;
        this.mSPS = null;
    }

    private boolean checkTestNeeded() {
        String resolution = this.mWidth + "x" + this.mHeight + "-";
        if (this.mPreferences == null || !this.mPreferences.contains(new StringBuilder(PREF_PREFIX).append(resolution).append("lastSdk").toString())) {
            return VERBOSE;
        }
        int lastSdk = this.mPreferences.getInt(new StringBuilder(PREF_PREFIX).append(resolution).append("lastSdk").toString(), 0);
        int lastVersion = this.mPreferences.getInt(new StringBuilder(PREF_PREFIX).append(resolution).append("lastVersion").toString(), 0);
        if (VERSION.SDK_INT > lastSdk || 3 > lastVersion) {
            return VERBOSE;
        }
        return false;
    }

    private void saveTestResult(boolean success) {
        String resolution = this.mWidth + "x" + this.mHeight + "-";
        Editor editor = this.mPreferences.edit();
        editor.putBoolean(new StringBuilder(PREF_PREFIX).append(resolution).append("success").toString(), success);
        if (success) {
            editor.putInt(new StringBuilder(PREF_PREFIX).append(resolution).append("lastSdk").toString(), VERSION.SDK_INT);
            editor.putInt(new StringBuilder(PREF_PREFIX).append(resolution).append("lastVersion").toString(), 3);
            editor.putInt(new StringBuilder(PREF_PREFIX).append(resolution).append("sliceHeight").toString(), this.mNV21.getSliceHeigth());
            editor.putInt(new StringBuilder(PREF_PREFIX).append(resolution).append(io.vov.vitamio.MediaFormat.KEY_STRIDE).toString(), this.mNV21.getStride());
            editor.putInt(new StringBuilder(PREF_PREFIX).append(resolution).append("padding").toString(), this.mNV21.getYPadding());
            editor.putBoolean(new StringBuilder(PREF_PREFIX).append(resolution).append("planar").toString(), this.mNV21.getPlanar());
            editor.putBoolean(new StringBuilder(PREF_PREFIX).append(resolution).append("reversed").toString(), this.mNV21.getUVPanesReversed());
            editor.putString(new StringBuilder(PREF_PREFIX).append(resolution).append("encoderName").toString(), this.mEncoderName);
            editor.putInt(new StringBuilder(PREF_PREFIX).append(resolution).append("colorFormat").toString(), this.mEncoderColorFormat);
            editor.putString(new StringBuilder(PREF_PREFIX).append(resolution).append("encoderName").toString(), this.mEncoderName);
            editor.putString(new StringBuilder(PREF_PREFIX).append(resolution).append("pps").toString(), this.mB64PPS);
            editor.putString(new StringBuilder(PREF_PREFIX).append(resolution).append("sps").toString(), this.mB64SPS);
        }
        editor.commit();
    }

    private void createTestImage() {
        int i;
        this.mInitialImage = new byte[((this.mSize * 3) / 2)];
        for (i = 0; i < this.mSize; i++) {
            this.mInitialImage[i] = (byte) ((i % 199) + 40);
        }
        for (i = this.mSize; i < (this.mSize * 3) / 2; i += 2) {
            this.mInitialImage[i] = (byte) ((i % 200) + 40);
            this.mInitialImage[i + 1] = (byte) (((i + 99) % 200) + 40);
        }
    }

    private boolean compareLumaPanes() {
        int f = 0;
        for (int j = 0; j < 34; j++) {
            for (int i = 0; i < this.mSize; i += 10) {
                int d = (this.mInitialImage[i] & 255) - (this.mDecodedVideo[j][i] & 255);
                int e = (this.mInitialImage[i + 1] & 255) - (this.mDecodedVideo[j][i + 1] & 255);
                if (d < 0) {
                    d = -d;
                }
                if (e < 0) {
                    e = -e;
                }
                if (d > 50 && e > 50) {
                    this.mDecodedVideo[j] = null;
                    f++;
                    break;
                }
            }
        }
        if (f <= 17) {
            return VERBOSE;
        }
        return false;
    }

    private int checkPaddingNeeded() {
        int j = ((this.mSize * 3) / 2) - 1;
        int max = 0;
        int[] r = new int[34];
        int k = 0;
        while (k < 34) {
            if (this.mDecodedVideo[k] != null) {
                int i = 0;
                while (i < j && (this.mDecodedVideo[k][j - i] & 255) < 50) {
                    i += 2;
                }
                if (i > 0) {
                    r[k] = (i >> 6) << 6;
                    if (r[k] > max) {
                        max = r[k];
                    }
                    Log.e(TAG, "Padding needed: " + r[k]);
                } else {
                    Log.v(TAG, "No padding needed.");
                }
            }
            k++;
        }
        return (max >> 6) << 6;
    }

    private boolean compareChromaPanes(boolean crossed) {
        int f = 0;
        for (int j = 0; j < 34; j++) {
            if (this.mDecodedVideo[j] != null) {
                int i;
                int d;
                if (crossed) {
                    for (i = this.mSize; i < (this.mSize * 3) / 2; i += 2) {
                        d = (this.mInitialImage[i] & 255) - (this.mDecodedVideo[j][i + 1] & 255);
                        if (d < 0) {
                            d = -d;
                        }
                        if (d > 50) {
                            f++;
                        }
                    }
                } else {
                    for (i = this.mSize; i < (this.mSize * 3) / 2; i++) {
                        d = (this.mInitialImage[i] & 255) - (this.mDecodedVideo[j][i] & 255);
                        if (d < 0) {
                            d = -d;
                        }
                        if (d > 50) {
                            f++;
                            break;
                        }
                    }
                }
            }
        }
        if (f <= 17) {
            return VERBOSE;
        }
        return false;
    }

    private void convertToNV21(int k) {
        byte[] buffer = new byte[((this.mSize * 3) / 2)];
        int stride = this.mWidth;
        int sliceHeight = this.mHeight;
        int colorFormat = this.mDecoderColorFormat;
        boolean planar = false;
        if (this.mDecOutputFormat != null) {
            MediaFormat format = this.mDecOutputFormat;
            if (format != null) {
                if (format.containsKey(io.vov.vitamio.MediaFormat.KEY_SLICE_HEIGHT)) {
                    sliceHeight = format.getInteger(io.vov.vitamio.MediaFormat.KEY_SLICE_HEIGHT);
                    if (sliceHeight < this.mHeight) {
                        sliceHeight = this.mHeight;
                    }
                }
                if (format.containsKey(io.vov.vitamio.MediaFormat.KEY_STRIDE)) {
                    stride = format.getInteger(io.vov.vitamio.MediaFormat.KEY_STRIDE);
                    if (stride < this.mWidth) {
                        stride = this.mWidth;
                    }
                }
                if (format.containsKey(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT) && format.getInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT) > 0) {
                    colorFormat = format.getInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT);
                }
            }
        }
        switch (colorFormat) {
            case 19:
            case 20:
                planar = VERBOSE;
                break;
            case 21:
            case 39:
            case 2130706688:
                planar = false;
                break;
        }
        int i = 0;
        while (i < this.mSize) {
            if (i % this.mWidth == 0) {
                i += stride - this.mWidth;
            }
            buffer[i] = this.mDecodedVideo[k][i];
            i++;
        }
        int j;
        if (planar) {
            i = 0;
            for (j = 0; j < this.mSize / 4; j++) {
                if ((i % this.mWidth) / 2 == 0) {
                    i += (stride - this.mWidth) / 2;
                }
                buffer[(this.mSize + (j * 2)) + 1] = this.mDecodedVideo[k][(stride * sliceHeight) + i];
                buffer[this.mSize + (j * 2)] = this.mDecodedVideo[k][(((stride * sliceHeight) * 5) / 4) + i];
                i++;
            }
        } else {
            i = 0;
            for (j = 0; j < this.mSize / 4; j++) {
                if ((i % this.mWidth) / 2 == 0) {
                    i += (stride - this.mWidth) / 2;
                }
                buffer[(this.mSize + (j * 2)) + 1] = this.mDecodedVideo[k][(stride * sliceHeight) + (i * 2)];
                buffer[this.mSize + (j * 2)] = this.mDecodedVideo[k][((stride * sliceHeight) + (i * 2)) + 1];
                i++;
            }
        }
        this.mDecodedVideo[k] = buffer;
    }

    private void configureEncoder() throws IOException {
        this.mEncoder = MediaCodec.createByCodecName(this.mEncoderName);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, this.mWidth, this.mHeight);
        mediaFormat.setInteger("bitrate", BITRATE);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_FRAME_RATE, 15);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, this.mEncoderColorFormat);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        this.mEncoder.configure(mediaFormat, null, null, 1);
        this.mEncoder.start();
    }

    private void releaseEncoder() {
        if (this.mEncoder != null) {
            try {
                this.mEncoder.stop();
            } catch (Exception e) {
            }
            try {
                this.mEncoder.release();
            } catch (Exception e2) {
            }
        }
    }

    private void configureDecoder() throws IOException {
        prefix = new byte[4];
        ByteBuffer csd0 = ByteBuffer.allocate(((this.mSPS.length + 4) + 4) + this.mPPS.length);
        byte[] bArr = new byte[4];
        bArr[3] = (byte) 1;
        csd0.put(bArr);
        csd0.put(this.mSPS);
        bArr = new byte[4];
        bArr[3] = (byte) 1;
        csd0.put(bArr);
        csd0.put(this.mPPS);
        this.mDecoder = MediaCodec.createByCodecName(this.mDecoderName);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, this.mWidth, this.mHeight);
        mediaFormat.setByteBuffer("csd-0", csd0);
        mediaFormat.setInteger(io.vov.vitamio.MediaFormat.KEY_COLOR_FORMAT, this.mDecoderColorFormat);
        this.mDecoder.configure(mediaFormat, null, null, 0);
        this.mDecoder.start();
        ByteBuffer[] decInputBuffers = this.mDecoder.getInputBuffers();
        int decInputIndex = this.mDecoder.dequeueInputBuffer(66666);
        if (decInputIndex >= 0) {
            decInputBuffers[decInputIndex].clear();
            decInputBuffers[decInputIndex].put(prefix);
            decInputBuffers[decInputIndex].put(this.mSPS);
            this.mDecoder.queueInputBuffer(decInputIndex, 0, decInputBuffers[decInputIndex].position(), timestamp(), 0);
        } else {
            Log.e(TAG, "No buffer available !");
        }
        decInputIndex = this.mDecoder.dequeueInputBuffer(66666);
        if (decInputIndex >= 0) {
            decInputBuffers[decInputIndex].clear();
            decInputBuffers[decInputIndex].put(prefix);
            decInputBuffers[decInputIndex].put(this.mPPS);
            this.mDecoder.queueInputBuffer(decInputIndex, 0, decInputBuffers[decInputIndex].position(), timestamp(), 0);
            return;
        }
        Log.e(TAG, "No buffer available !");
    }

    private void releaseDecoder() {
        if (this.mDecoder != null) {
            try {
                this.mDecoder.stop();
            } catch (Exception e) {
            }
            try {
                this.mDecoder.release();
            } catch (Exception e2) {
            }
        }
    }

    private long searchSPSandPPS() {
        ByteBuffer[] inputBuffers = this.mEncoder.getInputBuffers();
        ByteBuffer[] outputBuffers = this.mEncoder.getOutputBuffers();
        BufferInfo info = new BufferInfo();
        byte[] csd = new byte[128];
        int p = 4;
        int q = 4;
        long elapsed = 0;
        long now = timestamp();
        while (elapsed < 3000000 && (this.mSPS == null || this.mPPS == null)) {
            int bufferIndex = this.mEncoder.dequeueInputBuffer(66666);
            if (bufferIndex >= 0) {
                check(inputBuffers[bufferIndex].capacity() >= this.mData.length ? VERBOSE : false, "The input buffer is not big enough.");
                inputBuffers[bufferIndex].clear();
                inputBuffers[bufferIndex].put(this.mData, 0, this.mData.length);
                this.mEncoder.queueInputBuffer(bufferIndex, 0, this.mData.length, timestamp(), 0);
            } else {
                Log.e(TAG, "No buffer available !");
            }
            int index = this.mEncoder.dequeueOutputBuffer(info, 66666);
            if (index == -2) {
                MediaFormat format = this.mEncoder.getOutputFormat();
                ByteBuffer spsb = format.getByteBuffer("csd-0");
                ByteBuffer ppsb = format.getByteBuffer("csd-1");
                this.mSPS = new byte[(spsb.capacity() - 4)];
                spsb.position(4);
                spsb.get(this.mSPS, 0, this.mSPS.length);
                this.mPPS = new byte[(ppsb.capacity() - 4)];
                ppsb.position(4);
                ppsb.get(this.mPPS, 0, this.mPPS.length);
                break;
            }
            if (index == -3) {
                outputBuffers = this.mEncoder.getOutputBuffers();
            } else if (index >= 0) {
                int len = info.size;
                if (len < 128) {
                    outputBuffers[index].get(csd, 0, len);
                    if (len > 0 && csd[0] == (byte) 0 && csd[1] == (byte) 0 && csd[2] == (byte) 0 && csd[3] == (byte) 1) {
                        while (p < len) {
                            while (true) {
                                if (!(csd[p + 0] == (byte) 0 && csd[p + 1] == (byte) 0 && csd[p + 2] == (byte) 0 && csd[p + 3] == (byte) 1) && p + 3 < len) {
                                    p++;
                                }
                            }
                            if (p + 3 >= len) {
                                p = len;
                            }
                            if ((csd[q] & 31) == 7) {
                                this.mSPS = new byte[(p - q)];
                                System.arraycopy(csd, q, this.mSPS, 0, p - q);
                            } else {
                                this.mPPS = new byte[(p - q)];
                                System.arraycopy(csd, q, this.mPPS, 0, p - q);
                            }
                            p += 4;
                            q = p;
                        }
                    }
                }
                this.mEncoder.releaseOutputBuffer(index, false);
            }
            elapsed = timestamp() - now;
        }
        check((this.mPPS != null ? 1 : 0) & (this.mSPS != null ? 1 : 0), "Could not determine the SPS & PPS.");
        this.mB64PPS = Base64.encodeToString(this.mPPS, 0, this.mPPS.length, 2);
        this.mB64SPS = Base64.encodeToString(this.mSPS, 0, this.mSPS.length, 2);
        return elapsed;
    }

    private long encode() {
        long elapsed = 0;
        long now = timestamp();
        BufferInfo info = new BufferInfo();
        ByteBuffer[] encInputBuffers = this.mEncoder.getInputBuffers();
        ByteBuffer[] encOutputBuffers = this.mEncoder.getOutputBuffers();
        int n = 0;
        while (elapsed < 5000000) {
            int n2;
            int encInputIndex = this.mEncoder.dequeueInputBuffer(66666);
            if (encInputIndex >= 0) {
                check(encInputBuffers[encInputIndex].capacity() >= this.mData.length ? VERBOSE : false, "The input buffer is not big enough.");
                encInputBuffers[encInputIndex].clear();
                encInputBuffers[encInputIndex].put(this.mData, 0, this.mData.length);
                this.mEncoder.queueInputBuffer(encInputIndex, 0, this.mData.length, timestamp(), 0);
            } else {
                Log.d(TAG, "No buffer available !");
            }
            int encOutputIndex = this.mEncoder.dequeueOutputBuffer(info, 66666);
            if (encOutputIndex == -3) {
                encOutputBuffers = this.mEncoder.getOutputBuffers();
                n2 = n;
            } else if (encOutputIndex >= 0) {
                this.mVideo[n] = new byte[info.size];
                encOutputBuffers[encOutputIndex].clear();
                n2 = n + 1;
                encOutputBuffers[encOutputIndex].get(this.mVideo[n], 0, info.size);
                this.mEncoder.releaseOutputBuffer(encOutputIndex, false);
                if (n2 >= 50) {
                    flushMediaCodec(this.mEncoder);
                    return elapsed;
                }
            } else {
                n2 = n;
            }
            elapsed = timestamp() - now;
            n = n2;
        }
        throw new RuntimeException("The encoder is too slow.");
    }

    private long decode(boolean withPrefix) {
        int n = 0;
        int i = 0;
        int j = 0;
        long elapsed = 0;
        long now = timestamp();
        ByteBuffer[] decInputBuffers = this.mDecoder.getInputBuffers();
        ByteBuffer[] decOutputBuffers = this.mDecoder.getOutputBuffers();
        BufferInfo info = new BufferInfo();
        while (elapsed < 3000000) {
            if (i < 50) {
                int decInputIndex = this.mDecoder.dequeueInputBuffer(66666);
                if (decInputIndex >= 0) {
                    int l1 = decInputBuffers[decInputIndex].capacity();
                    int l2 = this.mVideo[i].length;
                    decInputBuffers[decInputIndex].clear();
                    if ((withPrefix && hasPrefix(this.mVideo[i])) || (!withPrefix && !hasPrefix(this.mVideo[i]))) {
                        check(l1 >= l2 ? VERBOSE : false, "The decoder input buffer is not big enough (nal=" + l2 + ", capacity=" + l1 + ").");
                        decInputBuffers[decInputIndex].put(this.mVideo[i], 0, this.mVideo[i].length);
                    } else if (withPrefix && !hasPrefix(this.mVideo[i])) {
                        check(l1 >= l2 + 4 ? VERBOSE : false, "The decoder input buffer is not big enough (nal=" + (l2 + 4) + ", capacity=" + l1 + ").");
                        ByteBuffer byteBuffer = decInputBuffers[decInputIndex];
                        byte[] bArr = new byte[4];
                        bArr[3] = (byte) 1;
                        byteBuffer.put(bArr);
                        decInputBuffers[decInputIndex].put(this.mVideo[i], 0, this.mVideo[i].length);
                    } else if (!withPrefix && hasPrefix(this.mVideo[i])) {
                        check(l1 >= l2 + -4 ? VERBOSE : false, "The decoder input buffer is not big enough (nal=" + (l2 - 4) + ", capacity=" + l1 + ").");
                        decInputBuffers[decInputIndex].put(this.mVideo[i], 4, this.mVideo[i].length - 4);
                    }
                    this.mDecoder.queueInputBuffer(decInputIndex, 0, l2, timestamp(), 0);
                    i++;
                } else {
                    Log.d(TAG, "No buffer available !");
                }
            }
            int decOutputIndex = this.mDecoder.dequeueOutputBuffer(info, 66666);
            if (decOutputIndex == -3) {
                decOutputBuffers = this.mDecoder.getOutputBuffers();
            } else if (decOutputIndex == -2) {
                this.mDecOutputFormat = this.mDecoder.getOutputFormat();
            } else if (decOutputIndex < 0) {
                continue;
            } else {
                if (n > 2) {
                    int length = info.size;
                    this.mDecodedVideo[j] = new byte[length];
                    decOutputBuffers[decOutputIndex].clear();
                    decOutputBuffers[decOutputIndex].get(this.mDecodedVideo[j], 0, length);
                    convertToNV21(j);
                    if (j >= 33) {
                        flushMediaCodec(this.mDecoder);
                        Log.v(TAG, "Decoding " + n + " frames took " + (elapsed / 1000) + " ms");
                        return elapsed;
                    }
                    j++;
                }
                this.mDecoder.releaseOutputBuffer(decOutputIndex, false);
                n++;
            }
            elapsed = timestamp() - now;
        }
        throw new RuntimeException("The decoder did not decode anything.");
    }

    private boolean hasPrefix(byte[] nal) {
        if (nal[0] == (byte) 0 && nal[1] == (byte) 0 && nal[2] == (byte) 0 && nal[3] == (byte) 1) {
            return VERBOSE;
        }
        return false;
    }

    private void encodeDecode() throws IOException {
        encode();
        try {
            configureDecoder();
            decode(VERBOSE);
        } finally {
            releaseDecoder();
        }
    }

    private void flushMediaCodec(MediaCodec mc) {
        int index = 0;
        BufferInfo info = new BufferInfo();
        while (index != -1) {
            index = mc.dequeueOutputBuffer(info, 66666);
            if (index >= 0) {
                mc.releaseOutputBuffer(index, false);
            }
        }
    }

    private void check(boolean cond, String message) {
        if (!cond) {
            Log.e(TAG, message);
            throw new IllegalStateException(message);
        }
    }

    private long timestamp() {
        return System.nanoTime() / 1000;
    }
}
