package axis.shiyan.wei.bluearchive.blinkfix.utils;

import lombok.Data;

@Data
public class PositionAndRotation {
    private final double x, y, z;
    private final float yaw, pitch;
}
