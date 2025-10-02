package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventPacket;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "NoRecoil", description = "Removes recoil", category = Category.COMBAT)
public class NoRecoil extends Module {
    public static int fix = 0;
    public static float yaw, pitch;

    @Override
    public void onDisable() {
        fix = 0;
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
