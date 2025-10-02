package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRenderEntity;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "NoRender", description = "Disable rendering of certain things.", category = Category.RENDER)
public class NoRender extends Module {
    public BooleanValue arrow = ValueBuilder.create(this, "Arrow").setDefaultBooleanValue(true).build().getBooleanValue();

    @EventTarget
    public void onRender(EventRenderEntity e) {
        if (arrow.getCurrentValue() && e.getEntity() instanceof EntityItem) {
            ItemStack entityItem = ((EntityItem) e.getEntity()).getEntityItem();

            if (entityItem != null && entityItem.getItem() == Items.arrow) {
                e.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            mc.theWorld.loadedEntityList.forEach(entity -> {
                if (entity instanceof EntityItem) {
                    ItemStack entityItem = ((EntityItem) entity).getEntityItem();

                    if (entityItem != null && entityItem.getItem() == Items.arrow) {
                        mc.theWorld.removeEntity(entity);
                    }
                }
            });
        }
    }
}
