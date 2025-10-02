package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender2D;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventShader;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.AutoArmor;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.InventoryManager;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.HYTUtils;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.Colors;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RenderUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.font.BaseFontRender;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.*;

import java.util.concurrent.ConcurrentHashMap;

@ModuleInfo(name = "ItemTags", description = "Show item tags.", category = Category.RENDER)
public class ItemTags extends Module {
    BooleanValue allItems = ValueBuilder.create(this, "All Items").setDefaultBooleanValue(false).build().getBooleanValue();

    BooleanValue godItems = ValueBuilder.create(this, "God Items").setDefaultBooleanValue(true).setVisibility(() -> !allItems.getCurrentValue()).build().getBooleanValue();
    BooleanValue diamond = ValueBuilder.create(this, "Diamond").setDefaultBooleanValue(true).setVisibility(() -> !allItems.getCurrentValue()).build().getBooleanValue();
    BooleanValue gold = ValueBuilder.create(this, "Gold").setDefaultBooleanValue(true).setVisibility(() -> !allItems.getCurrentValue()).build().getBooleanValue();
    BooleanValue iron = ValueBuilder.create(this, "Iron").setDefaultBooleanValue(true).setVisibility(() -> !allItems.getCurrentValue()).build().getBooleanValue();
    BooleanValue enderPearl = ValueBuilder.create(this, "Ender Pearl").setDefaultBooleanValue(true).setVisibility(() -> !allItems.getCurrentValue()).build().getBooleanValue();
    BooleanValue goldenApple = ValueBuilder.create(this, "Golden Apple").setDefaultBooleanValue(true).setVisibility(() -> !allItems.getCurrentValue()).build().getBooleanValue();
    BooleanValue usefulItem = ValueBuilder.create(this, "Useful Item").setDefaultBooleanValue(true).setVisibility(() -> !allItems.getCurrentValue()).build().getBooleanValue();

    ConcurrentHashMap<EntityItem, double[]> entityPositions = new ConcurrentHashMap<>();
    ScaledResolution scaledRes;

    private static String getDisplayName(EntityItem ent) {
        ItemStack item = ent.getEntityItem();
        return item.getDisplayName() + " * " + item.stackSize;
    }

    private boolean isValidItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        if (allItems.getCurrentValue()) {
            return true;
        }

        if (godItems.getCurrentValue()) {
            if (HYTUtils.isKBBall(stack)) {
                return true;
            }

            if (HYTUtils.isEnchantedGoldenApple(stack)) {
                return true;
            }

            if (HYTUtils.isGodAxe(stack)) {
                return true;
            }
        }

        if (diamond.getCurrentValue() && stack.getItem() == Items.diamond) {
            return true;
        }

        if (gold.getCurrentValue() && stack.getItem() == Items.gold_ingot) {
            return true;
        }

        if (iron.getCurrentValue() && stack.getItem() == Items.iron_ingot) {
            return true;
        }

        if (enderPearl.getCurrentValue() && stack.getItem() == Items.ender_pearl) {
            return true;
        }

        if (goldenApple.getCurrentValue() && stack.getItem() == Items.golden_apple) {
            return true;
        }

        if (usefulItem.getCurrentValue()) {
            InventoryManager manager = (InventoryManager) BlinkFix.getInstance().getModuleManager().getModule(InventoryManager.class);

            if (AutoArmor.isBestArmor(stack)) {
                return true;
            }

            if (stack.getItem() instanceof ItemBlock && stack.stackSize < 16) {
                return false;
            }

            if (stack.getItem() instanceof ItemTool) {
                return false;
            }

            if ((stack.getItem() instanceof ItemSnowball || stack.getItem() instanceof ItemEgg) && stack.stackSize < 3) {
                return false;
            }

            if (!manager.shouldDrop(stack)) {
                return true;
            }
        }

        return false;
    }

    private void updatePositions() {
        entityPositions.clear();
        float pTicks = mc.timer.renderPartialTicks;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityItem && isValidItem(((EntityItem) entity).getEntityItem())) {
                double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * pTicks - mc.getRenderManager().renderPosX;
                double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * pTicks - mc.getRenderManager().renderPosY + (BlinkFix.getInstance().getModuleManager().getModule(ItemPhysics.class).isEnabled() ? 0.25f : 0.5f);
                double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * pTicks - mc.getRenderManager().renderPosZ;

                double[] convertTo2D = RenderUtils.convertTo2D(x, y, z);

                if (convertTo2D != null) {
                    if ((convertTo2D[2] >= 0.0D) && (convertTo2D[2] < 1.0D)) {
                        entityPositions.put((EntityItem) entity, new double[]{convertTo2D[0], convertTo2D[1], Math.abs(RenderUtils.convertTo2D(x, y + 1.0D, z, entity)[1] - RenderUtils.convertTo2D(x, y, z, entity)[1]), convertTo2D[2]});
                    }
                }
            }
        }
    }

    @EventTarget
    public void update(EventRender event) {
        try {
            updatePositions();
        } catch (Exception ignored) {
        }
    }

    @EventTarget
    public void onShader(EventShader e) {
        if (scaledRes != null) {
            render(true);
        }
    }

    @EventTarget
    public void on2DRender(EventRender2D e) {
        scaledRes = e.getResolution();
        render(false);
    }

    public void render(boolean shader) {
        try {
            for (EntityItem ent : entityPositions.keySet()) {
                if (ent != null) {
                    double[] renderPositions = entityPositions.get(ent);
                    if ((renderPositions[3] < 0.0D) || (renderPositions[3] >= 1.0D)) {
                        continue;
                    }

                    GlStateManager.pushMatrix();

                    BaseFontRender font = BlinkFix.getInstance().getFontManager().siyuan16;
                    GlStateManager.translate(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), 0.0D);
                    GlStateManager.translate(0.0D, 0.0D, 0.0D);

                    GlStateManager.scale(0.75, 0.75, 1);
                    String str = getDisplayName(ent);

                    float allWidth = font.getStringWidth(str) + 8;

                    if (shader) {
                        RenderUtils.drawRect(-allWidth / 2, -14.0f, allWidth / 2, 0, 0xFFFFFFFF);
                    } else {
                        RenderUtils.drawRect(-allWidth / 2, -14.0f, allWidth / 2, 0, Colors.getColor(0, 0, 0, 40));
                        font.drawStringWithShadow(str, -allWidth / 2 + 2.5f, -14F, Colors.WHITE.c);
                    }
                    GlStateManager.popMatrix();
                }
            }
        } catch (Exception ignored) {

        }
    }
}
