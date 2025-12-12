package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventRenderEntity;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
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
