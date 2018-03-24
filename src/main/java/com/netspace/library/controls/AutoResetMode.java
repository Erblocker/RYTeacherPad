package com.netspace.library.controls;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface AutoResetMode {
    public static final int ALWAYS = 2;
    public static final int NEVER = 3;
    public static final int OVER = 1;
    public static final int UNDER = 0;

    public static class Parser {
        public static int fromInt(int value) {
            switch (value) {
                case 1:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 3;
                default:
                    return 0;
            }
        }
    }
}
