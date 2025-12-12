package moe.ichinomiya.naven.protocols.HuaYuTing.api.utils.math;

public class MathHelper {
    public static int floorDouble(double value) {
        var i = (int) value;
        return value < (double) i ? i - 1 : i;
    }
    public static float sin(float value) {
        return (float) Math.sin(value);
    }

    public static float cos(float value) {
        return (float) Math.cos(value);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static float sqrt(float value) {
        return (float)Math.sqrt(value);
    }

    public static double sqrt(double value) {
        return Math.sqrt(value);
    }

    public static double lengthSquared(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    public static double square(double x) {
        return x * x;
    }

    public static int square(int x) {
        return x * x;
    }

    public static float wrapDegrees(float value) {
        var mod = value % 360.0F;
        if (mod >= 180.0F) {
            mod -= 360.0F;
        }

        if (mod < -180.0F) {
            mod += 360.0F;
        }

        return mod;
    }

    public static boolean isOutsideTolerance(double a, double b, double tolerance) {
        return Math.abs(a - b) > tolerance;
    }

    public static int floor(float value) {
        int i = (int)value;
        return value < (float)i ? i - 1 : i;
    }

    public static short shortClamp(short value, short min, short max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static double doubleClamp(double value, double min, double max) {
        if (value < min) {
            return min;
        } else {
            return Math.min(value, max);
        }
    }

    public static long getSeed(int i, int j, int k) {
        var l = (i * 3129871L) ^ (long) k * 116129781L ^ (long) j;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }

    public static boolean approximatelyEquals(float a, float b) {
        return Math.abs(b - a) < 1.0E-5F;
    }

    public static boolean approximatelyEquals(double a, double b) {
        return Math.abs(b - a) < 9.999999747378752E-6;
    }

    public static boolean inRange(double num,double start,double end) {
        double a = Math.min(start,end);
        double b = Math.max(start,end);
        return num >= a && num <= b;
    }

    public static int floor(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }

    public static double abs(double value) {
        return Math.abs(value);
    }

    public static int abs(int value) {
        return Math.abs(value);
    }

    public static float abs(float value) {
        return Math.abs(value);
    }
}
