package moe.ichinomiya.naven.modules.impl.misc;

import de.florianmichael.viamcp.fixes.AttackOrder;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
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
