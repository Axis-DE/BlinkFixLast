package moe.ichinomiya.naven.utils;

import lombok.Data;

@Data
public class PositionAndRotation {
    private final double x, y, z;
    private final float yaw, pitch;
}
