package moe.ichinomiya.naven.utils;

import lombok.Getter;
import net.minecraft.util.MathHelper;

public final class TimeHelper {

    /* fields */
    @Getter
    private long lastMS;
    private long previousTime;

    /* constructors */
    public TimeHelper() {
        this.lastMS = 0L;
        this.previousTime = -1L;
    }

    /* methods */
    public boolean sleep(long time) {
        if (time() >= time) {
            reset();
            return true;
        }

        return false;
    }

    public boolean check(float milliseconds) {
        return System.currentTimeMillis() - previousTime >= milliseconds;
    }

    public boolean delay(double milliseconds) {
        return delay(milliseconds, false);
    }

    public boolean delay(double milliseconds, boolean reset) {
        boolean result = MathHelper.clamp_float(getCurrentMS() - lastMS, 0, (float) milliseconds) >= milliseconds;
        if (result && reset) {
            reset();
        }
        return result;
    }

    public void reset() {
        this.previousTime = System.currentTimeMillis();
        this.lastMS = getCurrentMS();
    }

    public void reset(long time) {
        this.previousTime = System.currentTimeMillis();
        this.lastMS = getCurrentMS() + time;
    }

    public long time() {
        return System.nanoTime() / 1000000L - lastMS;
    }

    public long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public double getLastDelay () {
        return getCurrentMS() - getLastMS();
    }

}