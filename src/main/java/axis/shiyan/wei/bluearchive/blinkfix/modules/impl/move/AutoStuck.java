package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventPacket;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.AutoClip;
import axis.shiyan.wei.bluearchive.blinkfix.utils.MoveUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "AutoStuck", description = "Automatically enable stuck when you over void!", category = Category.MOVEMENT)
public class AutoStuck extends Module {
    FloatValue fallDistance = ValueBuilder.create(this, "Fall Distance").setDefaultFloatValue(10).setFloatStep(0.1f).setMinFloatValue(3f).setMaxFloatValue(15f).build().getFloatValue();
    TimeHelper timer = new TimeHelper();

    @EventTarget
    public void onMotion(EventMotion e) {
        if (!AutoClip.work && mc.thePlayer.ticksExisted > 20) {
            Stuck stuck = (Stuck) BlinkFix.getInstance().getModuleManager().getModule(Stuck.class);

            if (stuck.isEnabled()) {
                timer.reset();
            }

            int pearlSlot = -1;

            // Get the pearl slot
            for (int i = 0; i < 9; i++) {
                if (mc.thePlayer.inventory.getStackInSlot(i) != null && mc.thePlayer.inventory.getStackInSlot(i).getItem() == Items.ender_pearl) {
                    pearlSlot = i;
                    break;
                }
            }

            if (((pearlSlot != -1 && mc.thePlayer.fallDistance > fallDistance.getCurrentValue()) || (mc.thePlayer.posY + mc.thePlayer.motionY < -50))
                    && MoveUtils.isBoundingBoxOverVoid(mc.thePlayer.getEntityBoundingBox()) && !mc.thePlayer.onGround && timer.delay(1000)) {
                if (!stuck.isEnabled()) {
                    stuck.toggle();
                }
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof S08PacketPlayerPosLook) {
            timer.reset();
        }
    }
}
