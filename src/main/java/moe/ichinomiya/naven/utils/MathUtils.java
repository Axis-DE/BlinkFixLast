package moe.ichinomiya.naven.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;

/**
 * The utility does some calculations.
 */
public class MathUtils {

    public static float map(float x, float prev_min, float prev_max, float new_min, float new_max) {
        return (x - prev_min) / (prev_max - prev_min) * (new_max - new_min) + new_min;
    }

    public static double getRandomInRange(double min, double max) {
        SecureRandom random = new SecureRandom();
        return min == max ? min : random.nextDouble() * (max - min) + min;
    }


    /**
     * Limit the maximum and minimum values of the specified number.
     */
    public static <T extends Number> T clamp(T value, T minimum, T maximum) {
        if (value instanceof Integer) {
            if (value.intValue() > maximum.intValue()) {
                value = maximum;
            } else if (value.intValue() < minimum.intValue()) {
                value = minimum;
            }
        } else if (value instanceof Float) {
            if (value.floatValue() > maximum.floatValue()) {
                value = maximum;
            } else if (value.floatValue() < minimum.floatValue()) {
                value = minimum;
            }
        } else if (value instanceof Double) {
            if (value.doubleValue() > maximum.doubleValue()) {
                value = maximum;
            } else if (value.doubleValue() < minimum.doubleValue()) {
                value = minimum;
            }
        } else if (value instanceof Long) {
            if (value.longValue() > maximum.longValue()) {
                value = maximum;
            } else if (value.longValue() < minimum.longValue()) {
                value = minimum;
            }
        } else if (value instanceof Short) {
            if (value.shortValue() > maximum.shortValue()) {
                value = maximum;
            } else if (value.shortValue() < minimum.shortValue()) {
                value = minimum;
            }
        } else if (value instanceof Byte) {
            if (value.byteValue() > maximum.byteValue()) {
                value = maximum;
            } else if (value.byteValue() < minimum.byteValue()) {
                value = minimum;
            }
        }

        return value;
    }

    public static final Random random = new Random();

    public static double getRandomDoubleInRange(double minDouble, double maxDouble) {
        return minDouble >= maxDouble ? minDouble : random.nextDouble() * (maxDouble - minDouble) + minDouble;
    }

    public static int getRandomIntInRange(int startInclusive, int endExclusive) {
        if (endExclusive - startInclusive <= 0) {
            return startInclusive;
        }
        return startInclusive + new Random().nextInt(endExclusive - startInclusive);
    }

    /**
     * Set the specified angle to normal。
     */
    public static float normalizeAngle(float angle) {
        float newAngle = angle % 360F;

        return newAngle < -180.0F ? newAngle + 360.0F
                : newAngle > 180.0F ? newAngle - 360.0F
                : newAngle;
    }

    public static float interpolate(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    /**
     * Returns an interpolated angle in degrees between a set of start and end。
     */
    public static float interpolateAngle(float delta, float start, float end) {
        return start + delta * normalizeAngle(end - start);
    }

    /**
     * Round the specified number.
     */
    public static double roundToPlace(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        return new BigDecimal(value)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Get the number of decimal places for a set of numbers.
     */
    public static <T extends Number> int getNumberDecimalDigits(T value) {
        if (value instanceof Integer || value instanceof Long)
            return 0;

        String[] number = value.toString().split("\\.");

        if (number.length == 2) {
            if (number[1].endsWith("0"))
                number[1] = number[1].substring(0, number[1].length() - 1);
            return number[1].length();
        }

        return 0;
    }

    public static float clampValue(final float value, final float floor, final float cap) {
        if (value < floor) {
            return floor;
        }
        return Math.min(value, cap);
    }

    public static int clampValue(final int value, final int floor, final int cap) {
        if (value < floor) {
            return floor;
        }
        return Math.min(value, cap);
    }

    /**
     * Returns a power of two size for the given target capacity.
     */
    public static int powerOfTwo(int cap) {
        int n = cap - 1;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n + 1;
    }

    public static float log(double base, double number) {
        return (float) (Math.log(number) / Math.log(base));
    }


    public static double round(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean contains(float x, float y, float minX, float minY, float maxX, float maxY) {
        return x > minX && x < maxX && y > minY && y < maxY;
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double PI = 3.141592653;
        double output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }
}
