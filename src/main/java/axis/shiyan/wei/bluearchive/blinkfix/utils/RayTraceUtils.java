package axis.shiyan.wei.bluearchive.blinkfix.utils;

import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;

import java.util.List;

public class RayTraceUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static MovingObjectPosition rayCast(final float partialTicks, final float[] rots) {
        MovingObjectPosition objectMouseOver = null;
        Entity entity = mc.getRenderViewEntity();

        if (entity != null && mc.theWorld != null) {
            double distance = mc.playerController.getBlockReachDistance();
            objectMouseOver = entity.customRayTrace(entity.getPositionEyes(1), distance, partialTicks, rots[0], rots[1]);
        }

        return objectMouseOver;
    }

    public static MovingObjectPosition rayCast(final float partialTicks, final float[] rots, final Vec3 eyes) {
        MovingObjectPosition objectMouseOver = null;
        Entity entity = mc.getRenderViewEntity();

        if (entity != null && mc.theWorld != null) {
            double distance = mc.playerController.getBlockReachDistance();
            objectMouseOver = entity.customRayTrace(eyes, distance, partialTicks, rots[0], rots[1]);
        }

        return objectMouseOver;
    }
}
