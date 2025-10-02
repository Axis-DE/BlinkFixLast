package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.utils.Colors;
import axis.shiyan.wei.bluearchive.blinkfix.utils.MathUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RenderUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SmoothAnimationTimer;
import lombok.Getter;
import lombok.Setter;
import axis.shiyan.wei.bluearchive.blinkfix.events.EventLivingUpdate;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender2D;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRespawn;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRunTicks;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.font.BaseFontRender;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "DamageParticle", description = "Shows damage particles", category = Category.RENDER)
public class DamageParticle extends Module {
    private final List<Particle> particles = new CopyOnWriteArrayList<>();

    @EventTarget
    public void onRespawn(EventRespawn event) {
        this.particles.clear();
    }

    @EventTarget
    public void onTick(EventRunTicks event) {
        this.particles.removeIf(particle -> particle.getAnimation().isAnimationDone(true));
    }

    @EventTarget
    public void onLivingUpdate(EventLivingUpdate event) {
        EntityLivingBase entity = event.getEntity();

        if (entity == mc.thePlayer || entity.ticksExisted < 3) {
            return;
        }

        final float before = entity.lastHealth + entity.lastAbsorptionAmount;
        final float after = entity.getHealth() + entity.getAbsorptionAmount();

        if (before != after) {
            String text;

            float damage = before - after;

            if (Float.isNaN(damage) || Float.isInfinite(damage)) {
                return;
            }

            if (damage < 0) {
                text = EnumChatFormatting.GREEN.toString() + -MathUtils.roundToPlace(damage, 1);
            } else {
                text = EnumChatFormatting.YELLOW.toString() + MathUtils.roundToPlace(damage, 1);
            }

            AxisAlignedBB box = entity.getEntityBoundingBox();
            double[] location = new double[]{MathUtils.getRandomDoubleInRange(box.minX, box.maxX), MathUtils.getRandomDoubleInRange(box.minY, box.maxY), MathUtils.getRandomDoubleInRange(box.minZ, box.maxZ)};
            Particle particle = new Particle(location, text);
            particle.getAnimation().value = 0;
            particle.getAnimation().target = 60;
            particle.getAnimation().speed = 0.08f;
            particles.add(particle);
        }
    }

    @EventTarget
    public void onRenderWorld(EventRender event) {
        float pTicks = mc.timer.renderPartialTicks;

        for (Particle particle : particles) {
            double[] currentPosition = new double[]{particle.getOriginPosition()[0], particle.getOriginPosition()[1] + particle.getAnimation().value / 100f, particle.getOriginPosition()[2]};
            double[] lastPosition = particle.getLastPosition() == null ? currentPosition : particle.getLastPosition();

            double x = lastPosition[0] + (currentPosition[0] - lastPosition[0]) * pTicks - mc.getRenderManager().renderPosX;
            double y = lastPosition[1] + (currentPosition[1] - lastPosition[1]) * pTicks - mc.getRenderManager().renderPosY;
            double z = lastPosition[2] + (currentPosition[2] - lastPosition[2]) * pTicks - mc.getRenderManager().renderPosZ;

            double[] convertTo2D = RenderUtils.convertTo2D(x, y, z);

            if (convertTo2D != null) {
                if ((convertTo2D[2] >= 0.0D) && (convertTo2D[2] < 1.0D)) {
                    particle.setRenderPosition(convertTo2D);
                }
            }

            particle.setLastPosition(currentPosition);
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D e) {
        ScaledResolution scaledRes = new ScaledResolution(mc);

        for (Particle particle : this.particles) {
            particle.getAnimation().update(true);

            if (particle.getRenderPosition() != null) {
                double x = particle.getRenderPosition()[0];
                double y = particle.getRenderPosition()[1];

                GlStateManager.pushMatrix();
                GlStateManager.translate(x / scaledRes.getScaleFactor(), y / scaledRes.getScaleFactor(), 0.0D);

                float scale = (float) (1 - Math.min(1, Math.max(0, Math.sqrt(mc.thePlayer.getDistance(particle.getPosition()[0], particle.getPosition()[1], particle.getPosition()[2]) / 10))));
                GlStateManager.scale(scale * 2.5f + 0.5, scale * 2.5f + 0.5, 0);

                BaseFontRender font = BlinkFix.getInstance().getFontManager().regular30;
                String text = particle.getText();
                font.drawStringWithShadow(text, 0, font.getStringWidth(text) / 2f, Colors.getColor(255, 255, 255, Math.min(1005 - (int) (particle.getAnimation().value / 60 * 1000), 255)));

                GlStateManager.popMatrix();
            }
        }
    }
}

@Getter
class Particle {
    private final SmoothAnimationTimer animation = new SmoothAnimationTimer(100);
    private final String text;

    @Setter
    private double[] originPosition, position, lastPosition;
    @Setter
    private double[] renderPosition;

    public Particle(double[] position, String text) {
        this.originPosition = new double[]{position[0], position[1], position[2]};
        this.position = position;
        this.text = text;
    }
}