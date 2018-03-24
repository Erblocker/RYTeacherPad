package okhttp3.internal.publicsuffix;

import java.net.IDN;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.internal.Util;

public final class PublicSuffixDatabase {
    private static final String[] EMPTY_RULE = new String[0];
    private static final byte EXCEPTION_MARKER = (byte) 33;
    private static final String[] PREVAILING_RULE = new String[]{"*"};
    public static final String PUBLIC_SUFFIX_RESOURCE = "publicsuffixes.gz";
    private static final byte[] WILDCARD_LABEL = new byte[]{(byte) 42};
    private static final PublicSuffixDatabase instance = new PublicSuffixDatabase();
    private final AtomicBoolean listRead = new AtomicBoolean(false);
    private byte[] publicSuffixExceptionListBytes;
    private byte[] publicSuffixListBytes;
    private final CountDownLatch readCompleteLatch = new CountDownLatch(1);

    private void readTheList() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:25:? in {10, 15, 17, 21, 22, 24, 26, 27} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:323)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:226)
*/
        /*
        r10 = this;
        r4 = 0;
        r3 = 0;
        r7 = okhttp3.internal.publicsuffix.PublicSuffixDatabase.class;
        r7 = r7.getClassLoader();
        r8 = "publicsuffixes.gz";
        r2 = r7.getResourceAsStream(r8);
        if (r2 == 0) goto L_0x0033;
    L_0x0011:
        r7 = new okio.GzipSource;
        r8 = okio.Okio.source(r2);
        r7.<init>(r8);
        r0 = okio.Okio.buffer(r7);
        r5 = r0.readInt();	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r4 = new byte[r5];	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r0.readFully(r4);	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r6 = r0.readInt();	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r3 = new byte[r6];	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r0.readFully(r3);	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        okhttp3.internal.Util.closeQuietly(r0);
    L_0x0033:
        monitor-enter(r10);
        r10.publicSuffixListBytes = r4;
        r10.publicSuffixExceptionListBytes = r3;
        monitor-exit(r10);
        r7 = r10.readCompleteLatch;
        r7.countDown();
        return;
    L_0x003f:
        r1 = move-exception;
        r7 = okhttp3.internal.platform.Platform.get();	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r8 = 5;	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r9 = "Failed to read public suffix list";	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r7.log(r8, r9, r1);	 Catch:{ IOException -> 0x003f, all -> 0x0051 }
        r4 = 0;
        r3 = 0;
        okhttp3.internal.Util.closeQuietly(r0);
        goto L_0x0033;
    L_0x0051:
        r7 = move-exception;
        okhttp3.internal.Util.closeQuietly(r0);
        throw r7;
    L_0x0056:
        r7 = move-exception;
        monitor-exit(r10);
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.publicsuffix.PublicSuffixDatabase.readTheList():void");
    }

    public static PublicSuffixDatabase get() {
        return instance;
    }

    public String getEffectiveTldPlusOne(String domain) {
        if (domain == null) {
            throw new NullPointerException("domain == null");
        }
        String[] domainLabels = IDN.toUnicode(domain).split("\\.");
        String[] rule = findMatchingRule(domainLabels);
        if (domainLabels.length == rule.length && rule[0].charAt(0) != '!') {
            return null;
        }
        int firstLabelOffset;
        if (rule[0].charAt(0) == '!') {
            firstLabelOffset = domainLabels.length - rule.length;
        } else {
            firstLabelOffset = domainLabels.length - (rule.length + 1);
        }
        StringBuilder effectiveTldPlusOne = new StringBuilder();
        String[] punycodeLabels = domain.split("\\.");
        for (int i = firstLabelOffset; i < punycodeLabels.length; i++) {
            effectiveTldPlusOne.append(punycodeLabels[i]).append('.');
        }
        effectiveTldPlusOne.deleteCharAt(effectiveTldPlusOne.length() - 1);
        return effectiveTldPlusOne.toString();
    }

    private String[] findMatchingRule(String[] domainLabels) {
        int i;
        int labelIndex;
        if (this.listRead.get() || !this.listRead.compareAndSet(false, true)) {
            try {
                this.readCompleteLatch.await();
            } catch (InterruptedException e) {
            }
        } else {
            readTheList();
        }
        synchronized (this) {
            if (this.publicSuffixListBytes == null) {
                throw new IllegalStateException("Unable to load publicsuffixes.gz resource from the classpath.");
            }
        }
        byte[][] domainLabelsUtf8Bytes = new byte[domainLabels.length][];
        for (i = 0; i < domainLabels.length; i++) {
            domainLabelsUtf8Bytes[i] = domainLabels[i].getBytes(Util.UTF_8);
        }
        String exactMatch = null;
        for (i = 0; i < domainLabelsUtf8Bytes.length; i++) {
            String rule = binarySearchBytes(this.publicSuffixListBytes, domainLabelsUtf8Bytes, i);
            if (rule != null) {
                exactMatch = rule;
                break;
            }
        }
        String wildcardMatch = null;
        if (domainLabelsUtf8Bytes.length > 1) {
            byte[][] labelsWithWildcard = (byte[][]) domainLabelsUtf8Bytes.clone();
            for (labelIndex = 0; labelIndex < labelsWithWildcard.length - 1; labelIndex++) {
                labelsWithWildcard[labelIndex] = WILDCARD_LABEL;
                rule = binarySearchBytes(this.publicSuffixListBytes, labelsWithWildcard, labelIndex);
                if (rule != null) {
                    wildcardMatch = rule;
                    break;
                }
            }
        }
        String exception = null;
        if (wildcardMatch != null) {
            for (labelIndex = 0; labelIndex < domainLabelsUtf8Bytes.length - 1; labelIndex++) {
                rule = binarySearchBytes(this.publicSuffixExceptionListBytes, domainLabelsUtf8Bytes, labelIndex);
                if (rule != null) {
                    exception = rule;
                    break;
                }
            }
        }
        if (exception != null) {
            return ("!" + exception).split("\\.");
        }
        if (exactMatch == null && wildcardMatch == null) {
            return PREVAILING_RULE;
        }
        String[] exactRuleLabels;
        String[] wildcardRuleLabels;
        if (exactMatch != null) {
            exactRuleLabels = exactMatch.split("\\.");
        } else {
            exactRuleLabels = EMPTY_RULE;
        }
        if (wildcardMatch != null) {
            wildcardRuleLabels = wildcardMatch.split("\\.");
        } else {
            wildcardRuleLabels = EMPTY_RULE;
        }
        return exactRuleLabels.length <= wildcardRuleLabels.length ? wildcardRuleLabels : exactRuleLabels;
    }

    private static String binarySearchBytes(byte[] bytesToSearch, byte[][] labels, int labelIndex) {
        int low = 0;
        int high = bytesToSearch.length;
        while (low < high) {
            int mid = (low + high) / 2;
            while (mid > -1 && bytesToSearch[mid] != (byte) 10) {
                mid--;
            }
            mid++;
            int end = 1;
            while (bytesToSearch[mid + end] != (byte) 10) {
                end++;
            }
            int publicSuffixLength = (mid + end) - mid;
            int currentLabelIndex = labelIndex;
            int currentLabelByteIndex = 0;
            int publicSuffixByteIndex = 0;
            boolean expectDot = false;
            while (true) {
                int byte0;
                if (expectDot) {
                    byte0 = 46;
                    expectDot = false;
                } else {
                    byte0 = labels[currentLabelIndex][currentLabelByteIndex] & 255;
                }
                int compareResult = byte0 - (bytesToSearch[mid + publicSuffixByteIndex] & 255);
                if (compareResult == 0) {
                    publicSuffixByteIndex++;
                    currentLabelByteIndex++;
                    if (publicSuffixByteIndex != publicSuffixLength) {
                        if (labels[currentLabelIndex].length == currentLabelByteIndex) {
                            if (currentLabelIndex == labels.length - 1) {
                                break;
                            }
                            currentLabelIndex++;
                            currentLabelByteIndex = -1;
                            expectDot = true;
                        }
                    } else {
                        break;
                    }
                }
                break;
            }
            if (compareResult < 0) {
                high = mid - 1;
            } else if (compareResult > 0) {
                low = (mid + end) + 1;
            } else {
                int publicSuffixBytesLeft = publicSuffixLength - publicSuffixByteIndex;
                int labelBytesLeft = labels[currentLabelIndex].length - currentLabelByteIndex;
                for (int i = currentLabelIndex + 1; i < labels.length; i++) {
                    labelBytesLeft += labels[i].length;
                }
                if (labelBytesLeft < publicSuffixBytesLeft) {
                    high = mid - 1;
                } else if (labelBytesLeft <= publicSuffixBytesLeft) {
                    return new String(bytesToSearch, mid, publicSuffixLength, Util.UTF_8);
                } else {
                    low = (mid + end) + 1;
                }
            }
        }
        return null;
    }

    void setListBytes(byte[] publicSuffixListBytes, byte[] publicSuffixExceptionListBytes) {
        this.publicSuffixListBytes = publicSuffixListBytes;
        this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
        this.listRead.set(true);
        this.readCompleteLatch.countDown();
    }
}
