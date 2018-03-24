package org.kobjects.util;

import java.util.Vector;

public class Csv {
    public static String encode(String value, char quote) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == quote || c == '^') {
                buf.append(c);
                buf.append(c);
            } else if (c < ' ') {
                buf.append('^');
                buf.append((char) (c + 64));
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public static String encode(Object[] values) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                buf.append(',');
            }
            Object v = values[i];
            if ((v instanceof Number) || (v instanceof Boolean)) {
                buf.append(v.toString());
            } else {
                buf.append('\"');
                buf.append(encode(v.toString(), '\"'));
                buf.append('\"');
            }
        }
        return buf.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String[] decode(String line) {
        Vector tmp = new Vector();
        int p0 = 0;
        int len = line.length();
        while (true) {
            if (p0 >= len || line.charAt(p0) > ' ') {
                if (p0 >= len) {
                    break;
                } else if (line.charAt(p0) == '\"') {
                    int p02;
                    p0++;
                    StringBuffer buf = new StringBuffer();
                    while (true) {
                        p02 = p0 + 1;
                        char c = line.charAt(p0);
                        if (c != '^' || p02 >= len) {
                            if (c != '\"') {
                                p0 = p02;
                            } else if (p02 == len || line.charAt(p02) != '\"') {
                                tmp.addElement(buf.toString());
                                p0 = p02;
                            } else {
                                p0 = p02 + 1;
                            }
                            buf.append(c);
                        } else {
                            p0 = p02 + 1;
                            char c2 = line.charAt(p02);
                            if (c2 != '^') {
                                c2 = (char) (c2 - 64);
                            }
                            buf.append(c2);
                        }
                    }
                    tmp.addElement(buf.toString());
                    p0 = p02;
                    while (p0 < len && line.charAt(p0) <= ' ') {
                        p0++;
                    }
                    if (p0 >= len) {
                        break;
                    } else if (line.charAt(p0) != ',') {
                        throw new RuntimeException("Comma expected at " + p0 + " line: " + line);
                    } else {
                        p0++;
                    }
                } else {
                    int p1 = line.indexOf(44, p0);
                    if (p1 == -1) {
                        break;
                    }
                    tmp.addElement(line.substring(p0, p1).trim());
                    p0 = p1 + 1;
                }
            } else {
                p0++;
            }
        }
        String[] result = new String[tmp.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) tmp.elementAt(i);
        }
        return result;
    }
}
