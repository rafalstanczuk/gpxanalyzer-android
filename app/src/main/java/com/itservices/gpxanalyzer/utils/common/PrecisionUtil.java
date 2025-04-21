package com.itservices.gpxanalyzer.utils.common;

/**
 * Utility class for handling floating-point number comparisons and rounding with specified precision.
 * Addresses the inherent inaccuracies in floating-point arithmetic by introducing tolerance levels
 * based on the number of significant decimal digits.
 */
public class PrecisionUtil {
    /**
     * Default number of digits for precision comparison if not otherwise specified.
     * (Currently unused in the class methods, but defined for potential future use).
     */
    public static final int NDIG_PREC_COMP = 10;

    /**
     * Checks if a long value falls within a specified range (inclusive).
     *
     * @param testVal The value to test.
     * @param start   The lower bound of the range (inclusive).
     * @param end     The upper bound of the range (inclusive).
     * @return {@code true} if {@code testVal} is between {@code start} and {@code end} (inclusive), {@code false} otherwise.
     */
    public static boolean isValueBetweenIncluded(long testVal, long start, long end) {
        return testVal >= start && testVal <= end;
    }

    /**
     * Checks if a float value falls within a specified range (inclusive) using standard comparison operators (>=, <=).
     * Note: This method might be susceptible to floating-point inaccuracies when checking values very close to the boundaries.
     * For more robust boundary checks, consider using {@link #isValueBetweenIncluded(float, float, float, int)}.
     *
     * @param testVal The value to test.
     * @param start   The lower bound of the range (inclusive).
     * @param end     The upper bound of the range (inclusive).
     * @return {@code true} if {@code testVal} is numerically greater than or equal to {@code start} AND less than or equal to {@code end}, {@code false} otherwise.
     */
    public static boolean isValueBetweenIncluded(float testVal, float start, float end) {
        return testVal >= start && testVal <= end;
    }

    /**
     * Checks if a float value falls within a specified range (inclusive) considering a certain number of decimal digits for precision.
     * This method uses precision-aware comparisons ({@link #isGreaterEqual(float, float, int)} and {@link #isLessEqual(float, float, int)})
     * to mitigate floating-point inaccuracies at the boundaries.
     *
     * @param testVal The value to test.
     * @param start   The lower bound of the range (inclusive).
     * @param end     The upper bound of the range (inclusive).
     * @param nDigits The number of decimal digits to consider for precision in boundary comparisons.
     * @return {@code true} if {@code testVal} is considered greater than or equal to {@code start} AND less than or equal to {@code end} within the given precision, {@code false} otherwise.
     */
    public static boolean isValueBetweenIncluded(float testVal, float start, float end, int nDigits) {
        return isGreaterEqual(testVal, start, nDigits) && isLessEqual(testVal, end, nDigits);
    }

    /**
     * Checks if two float values are equal within a precision of one decimal digit.
     * Equivalent to calling {@code isEqualNDigitsPrecision(first, second, 1)}.
     *
     * @param first  The first float value.
     * @param second The second float value.
     * @return {@code true} if the absolute difference between the values is less than 0.1, {@code false} otherwise.
     */
    public static boolean isEqualOneDigitPrecision(float first, float second) {
        return isEqualNDigitsPrecision(first, second, 1);
    }

    /**
     * Checks if two float values are equal within a specified number of decimal digits.
     * Equality is determined by checking if the absolute difference between the two numbers
     * is less than 10<sup>-nDigits</sup>.
     *
     * @param first   The first float value.
     * @param second  The second float value.
     * @param nDigits The number of decimal digits to consider for precision (must be non-negative).
     * @return {@code true} if the absolute difference is less than the calculated tolerance, {@code false} otherwise.
     */
    public static boolean isEqualNDigitsPrecision(float first, float second, int nDigits) {
        return Math.abs(first - second) <  Math.pow(10, -nDigits);
    }

    /**
     * Checks if the first float value is greater than or equal to the second float value,
     * considering a specified number of decimal digits for precision in the equality check.
     *
     * @param first   The first float value.
     * @param second  The second float value.
     * @param nDigits The number of decimal digits to consider for precision when checking equality.
     * @return {@code true} if {@code first} is strictly greater than {@code second}, OR if they are equal within the specified precision, {@code false} otherwise.
     */
    public static boolean isGreaterEqual(float first, float second, int nDigits) {
        return first > second
                ||
                isEqualNDigitsPrecision(first, second, nDigits);
    }

    /**
     * Checks if the first float value is less than or equal to the second float value,
     * considering a specified number of decimal digits for precision in the equality check.
     *
     * @param first   The first float value.
     * @param second  The second float value.
     * @param nDigits The number of decimal digits to consider for precision when checking equality.
     * @return {@code true} if {@code first} is strictly less than {@code second}, OR if they are equal within the specified precision, {@code false} otherwise.
     */
    public static boolean isLessEqual(float first, float second, int nDigits) {
        return first < second
                ||
                isEqualNDigitsPrecision(first, second, nDigits);
    }

    /**
     * Checks if the first float value is greater than or equal to the second float value
     * within a precision of one decimal digit.
     * Equivalent to calling {@code isGreaterEqual(first, second, 1)}.
     *
     * @param first  The first float value.
     * @param second The second float value.
     * @return {@code true} if {@code first} > {@code second} or they are equal within 1 decimal digit precision, {@code false} otherwise.
     */
    public static boolean greaterEqualOneDigitPrecision(float first, float second) {
        return isGreaterEqual(first, second, 1);
    }

    /**
     * Checks if the first float value is less than or equal to the second float value
     * within a precision of one decimal digit.
     * Equivalent to calling {@code isLessEqual(first, second, 1)}.
     *
     * @param first  The first float value.
     * @param second The second float value.
     * @return {@code true} if {@code first} < {@code second} or they are equal within 1 decimal digit precision, {@code false} otherwise.
     */
    public static boolean lessEqualOneDigitPrecision(float first, float second) {
        return isLessEqual(first, second, 1);
    }

    /**
     * Rounds a float value to one decimal digit.
     * Equivalent to calling {@code roundToNDigits(value, 1)}.
     *
     * @param value The float value to round.
     * @return The value rounded to one decimal place.
     */
    public static float roundToOneDigit(float value) {
        return roundToNDigits(value, 1);
    }

    /**
     * Rounds a float value to a specified number of decimal digits.
     * Uses standard {@link Math#round(float)} after scaling the number.
     *
     * @param value   The float value to round.
     * @param nDigits The desired number of decimal digits (must be non-negative).
     * @return The value rounded to {@code nDigits} decimal places.
     */
    public static float roundToNDigits(float value, int nDigits) {
        int scale = (int) Math.pow(10, nDigits);
        return (float) Math.round(value * scale) / scale;
    }
}
