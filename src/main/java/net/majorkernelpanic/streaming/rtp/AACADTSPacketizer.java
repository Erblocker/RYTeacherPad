package net.majorkernelpanic.streaming.rtp;

import java.io.IOException;

public class AACADTSPacketizer extends AbstractPacketizer implements Runnable {
    private static final String TAG = "AACADTSPacketizer";
    private int samplingRate = 8000;
    private Thread t;

    public void start() {
        if (this.t == null) {
            this.t = new Thread(this);
            this.t.start();
        }
    }

    public void stop() {
        if (this.t != null) {
            try {
                this.is.close();
            } catch (IOException e) {
            }
            this.t.interrupt();
            try {
                this.t.join();
            } catch (InterruptedException e2) {
            }
            this.t = null;
        }
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
        this.socket.setClockFrequency((long) samplingRate);
    }

    public void run() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:36)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:60)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r24 = this;
        r17 = "AACADTSPacketizer";
        r18 = "AAC ADTS packetizer started !";
        android.util.Log.d(r17, r18);
        r12 = android.os.SystemClock.elapsedRealtime();
        r10 = r12;
        r17 = 8;
        r0 = r17;
        r6 = new byte[r0];
    L_0x0014:
        r17 = java.lang.Thread.interrupted();	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        if (r17 == 0) goto L_0x0024;
    L_0x001a:
        r17 = "AACADTSPacketizer";
        r18 = "AAC ADTS packetizer stopped !";
        android.util.Log.d(r17, r18);
        return;
    L_0x0024:
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.is;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17.read();	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0 & 255;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 255; // 0xff float:3.57E-43 double:1.26E-321;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        if (r0 != r1) goto L_0x0024;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x003c:
        r17 = 1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.is;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r18.read();	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = (byte) r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r6[r17] = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = 1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r6[r17];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0 & 240;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 240; // 0xf0 float:3.36E-43 double:1.186E-321;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        if (r0 != r1) goto L_0x0024;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x0061:
        r17 = 2;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 5;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r2 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0.fill(r6, r1, r2);	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = 1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r6[r17];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17 & 1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        if (r17 <= 0) goto L_0x01ba;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x0076:
        r15 = 1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x0077:
        r17 = 3;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r6[r17];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17 & 3;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17 << 11;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 4;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r6[r18];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0 & 255;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r18 << 3;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17 | r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 5;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r6[r18];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0 & 255;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r18 >> 5;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r5 = r17 | r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        if (r15 == 0) goto L_0x01bd;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x009d:
        r17 = 7;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x009f:
        r5 = r5 - r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = 6;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r6[r17];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17 & 3;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r8 = r17 + 1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r5 / 1272;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r9 = r17 + 1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        if (r15 != 0) goto L_0x00c4;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x00b1:
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.is;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = 2;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r2 = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0.read(r6, r1, r2);	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x00c4:
        r17 = net.majorkernelpanic.streaming.audio.AACStream.AUDIO_SAMPLING_RATES;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 2;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r6[r18];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r18 & 60;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r18 >> 2;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17[r18];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1.samplingRate = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = 2;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r6[r17];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0 & 192;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17 >> 6;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r14 = r17 + 1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.ts;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r20 = 1024000000000; // 0xee6b280000 float:2.0309954E26 double:5.059232213414E-312;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.samplingRate;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = (long) r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r22 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r20 = r20 / r22;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r18 + r20;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r2 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r2.ts = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r16 = 0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x0106:
        r0 = r16;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        if (r0 >= r5) goto L_0x0014;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x010a:
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.socket;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r17.requestBuffer();	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1.buffer = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.socket;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.ts;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17.updateTimestamp(r18);	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r5 - r16;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 1256; // 0x4e8 float:1.76E-42 double:6.205E-321;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        if (r0 <= r1) goto L_0x01c1;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x0133:
        r7 = 1256; // 0x4e8 float:1.76E-42 double:6.205E-321;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x0135:
        r16 = r16 + r7;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.buffer;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 16;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r2 = r18;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0.fill(r1, r2, r7);	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.buffer;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 12;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = 0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17[r18] = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.buffer;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 13;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = 16;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17[r18] = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.buffer;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 14;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r5 >> 5;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = (byte) r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17[r18] = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.buffer;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 15;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r5 << 3;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = (byte) r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17[r18] = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.buffer;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 15;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r17[r18];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0 & 248;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = (byte) r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17[r18] = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.buffer;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r18 = 15;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r17[r18];	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r19 | 0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = (byte) r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r19 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17[r18] = r19;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r7 + 16;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r1 = r17;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0.send(r1);	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        goto L_0x0106;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x01b7:
        r17 = move-exception;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        goto L_0x001a;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x01ba:
        r15 = 0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        goto L_0x0077;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x01bd:
        r17 = 9;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        goto L_0x009f;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
    L_0x01c1:
        r7 = r5 - r16;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r24;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r0 = r0.socket;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17 = r0;	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        r17.markNextPacket();	 Catch:{ IOException -> 0x01b7, ArrayIndexOutOfBoundsException -> 0x01ce, InterruptedException -> 0x0204 }
        goto L_0x0135;
    L_0x01ce:
        r4 = move-exception;
        r18 = "AACADTSPacketizer";
        r19 = new java.lang.StringBuilder;
        r17 = "ArrayIndexOutOfBoundsException: ";
        r0 = r19;
        r1 = r17;
        r0.<init>(r1);
        r17 = r4.getMessage();
        if (r17 == 0) goto L_0x0200;
    L_0x01e4:
        r17 = r4.getMessage();
    L_0x01e8:
        r0 = r19;
        r1 = r17;
        r17 = r0.append(r1);
        r17 = r17.toString();
        r0 = r18;
        r1 = r17;
        android.util.Log.e(r0, r1);
        r4.printStackTrace();
        goto L_0x001a;
    L_0x0200:
        r17 = "unknown error";
        goto L_0x01e8;
    L_0x0204:
        r17 = move-exception;
        goto L_0x001a;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.majorkernelpanic.streaming.rtp.AACADTSPacketizer.run():void");
    }

    private int fill(byte[] buffer, int offset, int length) throws IOException {
        int sum = 0;
        while (sum < length) {
            int len = this.is.read(buffer, offset + sum, length - sum);
            if (len < 0) {
                throw new IOException("End of stream");
            }
            sum += len;
        }
        return sum;
    }
}
