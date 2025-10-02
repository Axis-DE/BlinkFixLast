package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RotationManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.Optional;

@ModuleInfo(name = "WallShop", description = "", category = Category.MISC)
public class WallShop extends Module {
    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && RotationManager.lastRotations != null) {
            Optional<Entity> aimingVillager = mc.theWorld.loadedEntityList.stream()
                    .filter(entity -> entity instanceof EntityVillager)
                    .filter(entity -> isAiming(entity, RotationManager.lastRotations.x, RotationManager.lastRotations.y))
                    .findAny();

            if (aimingVillager.isPresent() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                mc.getNetHandler().getNetworkManager().sendPacket(new C02PacketUseEntity(aimingVillager.get(), C02PacketUseEntity.Action.INTERACT));
            }
        }
    }

    private boolean isAiming(Entity targetEntity, float yaw, float pitch) {
        Vec3 playerEye = new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + mc.thePlayer.getEyeHeight(), mc.thePlayer.lastTickPosZ);

        Vec3 playerLook = mc.thePlayer.getLook(yaw, pitch);
        Vec3 playerEyeBox = playerEye.addVector(playerLook.xCoord * 3,
                playerLook.yCoord * 3,
                playerLook.zCoord * 3);

        AxisAlignedBB targetBoundingBox = targetEntity.getEntityBoundingBox();
        MovingObjectPosition position = targetBoundingBox.calculateIntercept(playerEye, playerEyeBox);

        if (position == null) {
            return false;
        }

        return position.hitVec.distanceTo(playerEye) <= 3;
    }
}
