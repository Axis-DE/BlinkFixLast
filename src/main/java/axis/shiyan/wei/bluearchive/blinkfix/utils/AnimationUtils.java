package axis.shiyan.wei.bluearchive.blinkfix.utils;

public class AnimationUtils {
    public static int delta;
    public static int rotateDirection = 0;

    public static float getAnimationState(float animation, float finalState, float speed) {
        final float add = (delta * (speed / 1000f));
        if (animation < finalState) {
            if (animation + add < finalState) {
                animation += add;
            } else {
                animation = finalState;
            }
        } else if (animation - add > finalState) {
            animation -= add;
        } else {
            animation = finalState;
        }
        return animation;
    }

    public static float smoothAnimation(float ani, float finalState, float speed, float scale) {
        return getAnimationState(ani, finalState, Math.max(10, (Math.abs(ani - finalState)) * speed) * scale);
    }

    public static float getRotateDirection() {
        rotateDirection = rotateDirection + delta;
        if (rotateDirection > 360)
            rotateDirection = 0;
        return rotateDirection;
    }
}
