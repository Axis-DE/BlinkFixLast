package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender2D;
import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.Colors;
import axis.shiyan.wei.bluearchive.blinkfix.utils.EntityWatcher;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RenderUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SharedESPData;
import axis.shiyan.wei.bluearchive.blinkfix.utils.font.BaseFontRender;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "PlayerTags", description = "List tags of player.", category = Category.RENDER)
public class PlayerTags extends Module {
    BooleanValue shared = ValueBuilder.create(this, "Shared").setDefaultBooleanValue(true).build().getBooleanValue();

    List<TargetInfo> entityPositions = new CopyOnWriteArrayList<>();

    private void updatePositions() {
        entityPositions.clear();
        float pTicks = mc.timer.renderPartialTicks;
        for (EntityPlayer entity : mc.theWorld.playerEntities) {
            if (entity == mc.thePlayer) {
                continue;
            }

            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * pTicks - mc.getRenderManager().renderPosX;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * pTicks - mc.getRenderManager().renderPosY;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * pTicks - mc.getRenderManager().renderPosZ;
            y += entity.height + 0.25d;
            double[] convertTo2D = RenderUtils.convertTo2D(x, y, z);

            if (convertTo2D != null) {
                if ((convertTo2D[2] >= 0.0D) && (convertTo2D[2] < 1.0D)) {
                    double[] position = {convertTo2D[0], convertTo2D[1], Math.abs(RenderUtils.convertTo2D(x, y + 1.0D, z, entity)[1] - RenderUtils.convertTo2D(x, y, z, entity)[1]), convertTo2D[2]};

                    if (entity.getTags() != null && !entity.getTags().isEmpty()) {
                        entityPositions.add(new TargetInfo(position, entity.getTags()));
                    }
                }
            }
        }

        if (shared.getCurrentValue()) {
            Map<String, SharedESPData> dataMap = EntityWatcher.getSharedESPData();
            for (SharedESPData value : dataMap.values()) {
                double x = value.getPosX() - mc.getRenderManager().renderPosX;
                double y = value.getPosY() - mc.getRenderManager().renderPosY;
                double z = value.getPosZ() - mc.getRenderManager().renderPosZ;
                y += mc.thePlayer.height + 0.25d;

                double[] convertTo2D = RenderUtils.convertTo2D(x, y, z);
                if (convertTo2D != null) {
                    if ((convertTo2D[2] >= 0.0D) && (convertTo2D[2] < 1.0D) && value.getTags() != null && value.getTags().length > 0) {
                        double[] position = {convertTo2D[0], convertTo2D[1], Math.abs(RenderUtils.convertTo2D(x, y + 1.0D, z, mc.thePlayer)[1] - RenderUtils.convertTo2D(x, y, z, mc.thePlayer)[1]), convertTo2D[2]};
                        entityPositions.add(new TargetInfo(position, Arrays.asList(value.getTags())));
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
    public void onRender(EventRender2D e) {
        ScaledResolution scaledRes = e.getResolution();
        for (TargetInfo info : entityPositions) {
            double[] renderPositions = info.getPosition();
            List<String> description = info.getDescription();

            GlStateManager.pushMatrix();

            BaseFontRender font = BlinkFix.getInstance().getFontManager().siyuan16;
            GlStateManager.translate(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), 0.0D);
            GlStateManager.translate(10D, 0, 0.0D);

            int height = 0;
            for (String str : description) {
                font.drawStringWithShadow(I18n.format(str), 0, height, Colors.RED.c);
                height += 10;
            }

            GlStateManager.popMatrix();
        }
    }
}

@Data
@AllArgsConstructor
class TargetInfo {
    double[] position;
    List<String> description;
}