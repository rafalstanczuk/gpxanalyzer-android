package com.itservices.gpxanalyzer.utils.common;

public class PrecisionUtil {
    public static final int NDIG_PREC_COMP = 10;

    public static boolean isValueBetweenIncluded(long testVal, long start, long end) {
        return testVal >= start && testVal <= end;
    }

    public static boolean isValueBetweenIncluded(float testVal, float start, float end) {
        return testVal >= start && testVal <= end;
    }

    public static boolean isValueBetweenIncluded(float testVal, float start, float end, int nDigits) {
        return isGreaterEqual(testVal, start, nDigits) && isLessEqual(testVal, end, nDigits);
    }

    public static boolean isEqualOneDigitPrecision(float first, float second) {
        return isEqualNDigitsPrecision(first, second, 1);
    }

    public static boolean isEqualNDigitsPrecision(float first, float second, int nDigits) {
        return Math.abs(first - second) <  Math.pow(10, -nDigits);
    }

    public static boolean isGreaterEqual(float first, float second, int nDigits) {
        return first > second
                ||
                isEqualNDigitsPrecision(first, second, nDigits);
    }

    public static boolean isLessEqual(float first, float second, int nDigits) {
        return first < second
                ||
                isEqualNDigitsPrecision(first, second, nDigits);
    }

    public static boolean greaterEqualOneDigitPrecision(float first, float second) {
        return isGreaterEqual(first, second, 1);
    }

    public static boolean lessEqualOneDigitPrecision(float first, float second) {
        return isLessEqual(first, second, 1);
    }

    public static float roundToOneDigit(float value) {
        return roundToNDigits(value, 1);
    }

    public static float roundToNDigits(float value, int nDigits) {
        int scale = (int) Math.pow(10, nDigits);
        return (float) Math.round(value * scale) / scale;
    }
}
