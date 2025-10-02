package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import de.florianmichael.viamcp.fixes.AttackOrder;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import net.minecraft.entity.projectile.EntityFireball;

import java.util.Optional;

@ModuleInfo(name = "AntiFireball", description = "Prevents fireballs from damaging you", category = Category.MISC)
public class AntiFireball extends Module {
    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            Optional<EntityFireball> fireball = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityFireball && mc.thePlayer.getDistanceToEntity(entity) < 6).map(entity -> (EntityFireball) entity).findFirst();

            if (!fireball.isPresent()) {
                return;
            }

            EntityFireball entity = fireball.get();
            AttackOrder.sendFixedAttack(mc.thePlayer, entity);
        }
    }
}
