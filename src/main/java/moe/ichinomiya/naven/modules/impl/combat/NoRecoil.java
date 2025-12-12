package moe.ichinomiya.naven.modules.impl.combat;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "NoRecoil", description = "Removes recoil", category = Category.COMBAT)
public class NoRecoil extends Module {
    public static int fix = 0;
    public static float yaw, pitch;

    @Override
    public boolean onDisable() {
        fix = 0;
        return false;
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof S08PacketPlayerPosLook) {
            fix = 2;
            yaw = mc.thePlayer.rotationYaw;
            pitch = mc.thePlayer.rotationPitch;
        }
    }

    @EventTarget
    public void onPre(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (fix == 2) {
                mc.thePlayer.rotationYaw = yaw;
                mc.thePlayer.rotationPitch = pitch;
            }
            fix --;
        }
    }
}
