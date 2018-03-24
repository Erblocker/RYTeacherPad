package com.github.mikephil.charting.animation;

public class Easing {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$github$mikephil$charting$animation$Easing$EasingOption;

    private static class EasingFunctions {
        public static final EasingFunction EaseInBack = new EasingFunction() {
            public float getInterpolation(float input) {
                float position = input;
                return (position * position) * ((2.70158f * position) - 1.70158f);
            }
        };
        public static final EasingFunction EaseInBounce = new EasingFunction() {
            public float getInterpolation(float input) {
                return 1.0f - EasingFunctions.EaseOutBounce.getInterpolation(1.0f - input);
            }
        };
        public static final EasingFunction EaseInCirc = new EasingFunction() {
            public float getInterpolation(float input) {
                return -(((float) Math.sqrt((double) (1.0f - (input * input)))) - 1.0f);
            }
        };
        public static final EasingFunction EaseInCubic = new EasingFunction() {
            public float getInterpolation(float input) {
                return (input * input) * input;
            }
        };
        public static final EasingFunction EaseInElastic = new EasingFunction() {
            public float getInterpolation(float input) {
                if (input == 0.0f) {
                    return 0.0f;
                }
                float position = input;
                if (position == 1.0f) {
                    return 1.0f;
                }
                position -= 1.0f;
                return -(((float) Math.pow(2.0d, (double) (10.0f * position))) * ((float) Math.sin((((double) (position - ((0.3f / 6.2831855f) * ((float) Math.asin(1.0d))))) * 6.283185307179586d) / ((double) 1050253722))));
            }
        };
        public static final EasingFunction EaseInExpo = new EasingFunction() {
            public float getInterpolation(float input) {
                return input == 0.0f ? 0.0f : (float) Math.pow(2.0d, (double) (10.0f * (input - 1.0f)));
            }
        };
        public static final EasingFunction EaseInOutBack = new EasingFunction() {
            public float getInterpolation(float input) {
                float position = input / 0.5f;
                if (position < 1.0f) {
                    float s = 1.70158f * 1.525f;
                    return ((position * position) * (((1.0f + s) * position) - s)) * 0.5f;
                }
                position -= 2.0f;
                s = 1.70158f * 1.525f;
                return (((position * position) * (((1.0f + s) * position) + s)) + 2.0f) * 0.5f;
            }
        };
        public static final EasingFunction EaseInOutBounce = new EasingFunction() {
            public float getInterpolation(float input) {
                if (input < 0.5f) {
                    return EasingFunctions.EaseInBounce.getInterpolation(2.0f * input) * 0.5f;
                }
                return (EasingFunctions.EaseOutBounce.getInterpolation((2.0f * input) - 1.0f) * 0.5f) + 0.5f;
            }
        };
        public static final EasingFunction EaseInOutCirc = new EasingFunction() {
            public float getInterpolation(float input) {
                float position = input / 0.5f;
                if (position < 1.0f) {
                    return -0.5f * (((float) Math.sqrt((double) (1.0f - (position * position)))) - 1.0f);
                }
                position -= 2.0f;
                return (((float) Math.sqrt((double) (1.0f - (position * position)))) + 1.0f) * 0.5f;
            }
        };
        public static final EasingFunction EaseInOutCubic = new EasingFunction() {
            public float getInterpolation(float input) {
                float position = input / 0.5f;
                if (position < 1.0f) {
                    return ((0.5f * position) * position) * position;
                }
                position -= 2.0f;
                return (((position * position) * position) + 2.0f) * 0.5f;
            }
        };
        public static final EasingFunction EaseInOutElastic = new EasingFunction() {
            public float getInterpolation(float input) {
                if (input == 0.0f) {
                    return 0.0f;
                }
                float position = input / 0.5f;
                if (position == 2.0f) {
                    return 1.0f;
                }
                float s = (0.45000002f / 6.2831855f) * ((float) Math.asin(1.0d));
                if (position < 1.0f) {
                    position -= 1.0f;
                    return -0.5f * (((float) Math.sin((((double) ((1.0f * position) - s)) * 6.283185307179586d) / ((double) 1055286887))) * ((float) Math.pow(2.0d, (double) (10.0f * position))));
                }
                position -= 1.0f;
                return ((((float) Math.pow(2.0d, (double) (-10.0f * position))) * ((float) Math.sin((((double) ((position * 1.0f) - s)) * 6.283185307179586d) / ((double) 1055286887)))) * 0.5f) + 1.0f;
            }
        };
        public static final EasingFunction EaseInOutExpo = new EasingFunction() {
            public float getInterpolation(float input) {
                if (input == 0.0f) {
                    return 0.0f;
                }
                if (input == 1.0f) {
                    return 1.0f;
                }
                float position = input / 0.5f;
                if (position < 1.0f) {
                    return ((float) Math.pow(2.0d, (double) (10.0f * (position - 1.0f)))) * 0.5f;
                }
                return ((-((float) Math.pow(2.0d, (double) (-10.0f * (position - 1.0f))))) + 2.0f) * 0.5f;
            }
        };
        public static final EasingFunction EaseInOutQuad = new EasingFunction() {
            public float getInterpolation(float input) {
                float position = input / 0.5f;
                if (position < 1.0f) {
                    return (0.5f * position) * position;
                }
                position -= 1.0f;
                return -0.5f * (((position - 2.0f) * position) - 1.0f);
            }
        };
        public static final EasingFunction EaseInOutQuart = new EasingFunction() {
            public float getInterpolation(float input) {
                float position = input / 0.5f;
                if (position < 1.0f) {
                    return (((0.5f * position) * position) * position) * position;
                }
                position -= 2.0f;
                return -0.5f * ((((position * position) * position) * position) - 2.0f);
            }
        };
        public static final EasingFunction EaseInOutSine = new EasingFunction() {
            public float getInterpolation(float input) {
                return -0.5f * (((float) Math.cos(3.141592653589793d * ((double) input))) - 1.0f);
            }
        };
        public static final EasingFunction EaseInQuad = new EasingFunction() {
            public float getInterpolation(float input) {
                return input * input;
            }
        };
        public static final EasingFunction EaseInQuart = new EasingFunction() {
            public float getInterpolation(float input) {
                return ((input * input) * input) * input;
            }
        };
        public static final EasingFunction EaseInSine = new EasingFunction() {
            public float getInterpolation(float input) {
                return (-((float) Math.cos(((double) input) * 1.5707963267948966d))) + 1.0f;
            }
        };
        public static final EasingFunction EaseOutBack = new EasingFunction() {
            public float getInterpolation(float input) {
                float position = input - 1.0f;
                return ((position * position) * ((2.70158f * position) + 1.70158f)) + 1.0f;
            }
        };
        public static final EasingFunction EaseOutBounce = new EasingFunction() {
            public float getInterpolation(float input) {
                float position = input;
                if (position < 0.36363637f) {
                    return (7.5625f * position) * position;
                }
                if (position < 0.72727275f) {
                    position -= 0.54545456f;
                    return ((7.5625f * position) * position) + 0.75f;
                } else if (position < 0.90909094f) {
                    position -= 0.8181818f;
                    return ((7.5625f * position) * position) + 0.9375f;
                } else {
                    position -= 0.95454544f;
                    return ((7.5625f * position) * position) + 0.984375f;
                }
            }
        };
        public static final EasingFunction EaseOutCirc = new EasingFunction() {
            public float getInterpolation(float input) {
                input -= 1.0f;
                return (float) Math.sqrt((double) (1.0f - (input * input)));
            }
        };
        public static final EasingFunction EaseOutCubic = new EasingFunction() {
            public float getInterpolation(float input) {
                input -= 1.0f;
                return ((input * input) * input) + 1.0f;
            }
        };
        public static final EasingFunction EaseOutElastic = new EasingFunction() {
            public float getInterpolation(float input) {
                if (input == 0.0f) {
                    return 0.0f;
                }
                float position = input;
                if (position == 1.0f) {
                    return 1.0f;
                }
                return (((float) Math.pow(2.0d, (double) (-10.0f * position))) * ((float) Math.sin((((double) (position - ((0.3f / 6.2831855f) * ((float) Math.asin(1.0d))))) * 6.283185307179586d) / ((double) 1050253722)))) + 1.0f;
            }
        };
        public static final EasingFunction EaseOutExpo = new EasingFunction() {
            public float getInterpolation(float input) {
                return input == 1.0f ? 1.0f : -((float) Math.pow(2.0d, (double) ((1.0f + input) * -10.0f)));
            }
        };
        public static final EasingFunction EaseOutQuad = new EasingFunction() {
            public float getInterpolation(float input) {
                return (-input) * (input - 2.0f);
            }
        };
        public static final EasingFunction EaseOutQuart = new EasingFunction() {
            public float getInterpolation(float input) {
                input -= 1.0f;
                return -((((input * input) * input) * input) - 1.0f);
            }
        };
        public static final EasingFunction EaseOutSine = new EasingFunction() {
            public float getInterpolation(float input) {
                return (float) Math.sin(((double) input) * 1.5707963267948966d);
            }
        };
        public static final EasingFunction Linear = new EasingFunction() {
            public float getInterpolation(float input) {
                return input;
            }
        };

        private EasingFunctions() {
        }
    }

    public enum EasingOption {
        Linear,
        EaseInQuad,
        EaseOutQuad,
        EaseInOutQuad,
        EaseInCubic,
        EaseOutCubic,
        EaseInOutCubic,
        EaseInQuart,
        EaseOutQuart,
        EaseInOutQuart,
        EaseInSine,
        EaseOutSine,
        EaseInOutSine,
        EaseInExpo,
        EaseOutExpo,
        EaseInOutExpo,
        EaseInCirc,
        EaseOutCirc,
        EaseInOutCirc,
        EaseInElastic,
        EaseOutElastic,
        EaseInOutElastic,
        EaseInBack,
        EaseOutBack,
        EaseInOutBack,
        EaseInBounce,
        EaseOutBounce,
        EaseInOutBounce
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$github$mikephil$charting$animation$Easing$EasingOption() {
        int[] iArr = $SWITCH_TABLE$com$github$mikephil$charting$animation$Easing$EasingOption;
        if (iArr == null) {
            iArr = new int[EasingOption.values().length];
            try {
                iArr[EasingOption.EaseInBack.ordinal()] = 23;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[EasingOption.EaseInBounce.ordinal()] = 26;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[EasingOption.EaseInCirc.ordinal()] = 17;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[EasingOption.EaseInCubic.ordinal()] = 5;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[EasingOption.EaseInElastic.ordinal()] = 20;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[EasingOption.EaseInExpo.ordinal()] = 14;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[EasingOption.EaseInOutBack.ordinal()] = 25;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[EasingOption.EaseInOutBounce.ordinal()] = 28;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[EasingOption.EaseInOutCirc.ordinal()] = 19;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[EasingOption.EaseInOutCubic.ordinal()] = 7;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[EasingOption.EaseInOutElastic.ordinal()] = 22;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[EasingOption.EaseInOutExpo.ordinal()] = 16;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[EasingOption.EaseInOutQuad.ordinal()] = 4;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[EasingOption.EaseInOutQuart.ordinal()] = 10;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[EasingOption.EaseInOutSine.ordinal()] = 13;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[EasingOption.EaseInQuad.ordinal()] = 2;
            } catch (NoSuchFieldError e16) {
            }
            try {
                iArr[EasingOption.EaseInQuart.ordinal()] = 8;
            } catch (NoSuchFieldError e17) {
            }
            try {
                iArr[EasingOption.EaseInSine.ordinal()] = 11;
            } catch (NoSuchFieldError e18) {
            }
            try {
                iArr[EasingOption.EaseOutBack.ordinal()] = 24;
            } catch (NoSuchFieldError e19) {
            }
            try {
                iArr[EasingOption.EaseOutBounce.ordinal()] = 27;
            } catch (NoSuchFieldError e20) {
            }
            try {
                iArr[EasingOption.EaseOutCirc.ordinal()] = 18;
            } catch (NoSuchFieldError e21) {
            }
            try {
                iArr[EasingOption.EaseOutCubic.ordinal()] = 6;
            } catch (NoSuchFieldError e22) {
            }
            try {
                iArr[EasingOption.EaseOutElastic.ordinal()] = 21;
            } catch (NoSuchFieldError e23) {
            }
            try {
                iArr[EasingOption.EaseOutExpo.ordinal()] = 15;
            } catch (NoSuchFieldError e24) {
            }
            try {
                iArr[EasingOption.EaseOutQuad.ordinal()] = 3;
            } catch (NoSuchFieldError e25) {
            }
            try {
                iArr[EasingOption.EaseOutQuart.ordinal()] = 9;
            } catch (NoSuchFieldError e26) {
            }
            try {
                iArr[EasingOption.EaseOutSine.ordinal()] = 12;
            } catch (NoSuchFieldError e27) {
            }
            try {
                iArr[EasingOption.Linear.ordinal()] = 1;
            } catch (NoSuchFieldError e28) {
            }
            $SWITCH_TABLE$com$github$mikephil$charting$animation$Easing$EasingOption = iArr;
        }
        return iArr;
    }

    public static EasingFunction getEasingFunctionFromOption(EasingOption easing) {
        switch ($SWITCH_TABLE$com$github$mikephil$charting$animation$Easing$EasingOption()[easing.ordinal()]) {
            case 2:
                return EasingFunctions.EaseInQuad;
            case 3:
                return EasingFunctions.EaseOutQuad;
            case 4:
                return EasingFunctions.EaseInOutQuad;
            case 5:
                return EasingFunctions.EaseInCubic;
            case 6:
                return EasingFunctions.EaseOutCubic;
            case 7:
                return EasingFunctions.EaseInOutCubic;
            case 8:
                return EasingFunctions.EaseInQuart;
            case 9:
                return EasingFunctions.EaseOutQuart;
            case 10:
                return EasingFunctions.EaseInOutQuart;
            case 11:
                return EasingFunctions.EaseInSine;
            case 12:
                return EasingFunctions.EaseOutSine;
            case 13:
                return EasingFunctions.EaseInOutSine;
            case 14:
                return EasingFunctions.EaseInExpo;
            case 15:
                return EasingFunctions.EaseOutExpo;
            case 16:
                return EasingFunctions.EaseInOutExpo;
            case 17:
                return EasingFunctions.EaseInCirc;
            case 18:
                return EasingFunctions.EaseOutCirc;
            case 19:
                return EasingFunctions.EaseInOutCirc;
            case 20:
                return EasingFunctions.EaseInElastic;
            case 21:
                return EasingFunctions.EaseOutElastic;
            case 22:
                return EasingFunctions.EaseInOutElastic;
            case 23:
                return EasingFunctions.EaseInBack;
            case 24:
                return EasingFunctions.EaseOutBack;
            case 25:
                return EasingFunctions.EaseInOutBack;
            case 26:
                return EasingFunctions.EaseInBounce;
            case 27:
                return EasingFunctions.EaseOutBounce;
            case 28:
                return EasingFunctions.EaseInOutBounce;
            default:
                return EasingFunctions.Linear;
        }
    }
}
