package org.kobjects.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class Util {
    public static OutputStream streamcopy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[(Runtime.getRuntime().freeMemory() >= 1048576 ? 16384 : 128)];
        while (true) {
            int count = is.read(buf, 0, buf.length);
            if (count == -1) {
                is.close();
                return os;
            }
            os.write(buf, 0, count);
        }
    }

    public static int indexOf(Object[] arr, Object find) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(find)) {
                return i;
            }
        }
        return -1;
    }

    public static String buildUrl(String base, String local) {
        int ci = local.indexOf(58);
        if (local.startsWith("/") || ci == 1) {
            return "file:///" + local;
        }
        if (ci > 2 && ci < 6) {
            return local;
        }
        if (base == null) {
            base = "file:///";
        } else {
            if (base.indexOf(58) == -1) {
                base = "file:///" + base;
            }
            if (!base.endsWith("/")) {
                base = base + "/";
            }
        }
        return base + local;
    }

    public static void sort(Object[] arr, int start, int end) {
        if (end - start <= 2) {
            if (end - start == 2 && arr[start].toString().compareTo(arr[start + 1].toString()) > 0) {
                Object tmp = arr[start];
                arr[start] = arr[start + 1];
                arr[start + 1] = tmp;
            }
        } else if (end - start == 3) {
            sort(arr, start, start + 2);
            sort(arr, start + 1, start + 3);
            sort(arr, start, start + 2);
        } else {
            int middle = (start + end) / 2;
            sort(arr, start, middle);
            sort(arr, middle, end);
            Object[] tmp2 = new Object[(end - start)];
            int i0 = start;
            int i1 = middle;
            for (int i = 0; i < tmp2.length; i++) {
                int i12;
                if (i0 == middle) {
                    i12 = i1 + 1;
                    tmp2[i] = arr[i1];
                    i1 = i12;
                } else if (i1 == end || arr[i0].toString().compareTo(arr[i1].toString()) < 0) {
                    int i02 = i0 + 1;
                    tmp2[i] = arr[i0];
                    i0 = i02;
                } else {
                    i12 = i1 + 1;
                    tmp2[i] = arr[i1];
                    i1 = i12;
                }
            }
            System.arraycopy(tmp2, 0, arr, start, tmp2.length);
        }
    }
}
