package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.Teams;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.Notification;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.NotificationLevel;
import axis.shiyan.wei.bluearchive.blinkfix.utils.*;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemTool;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;

@ModuleInfo(name = "RageBot", description = "Automatically attacks entities", category = Category.COMBAT)
public class RageBot extends Module {
    public static EntityPlayer target;
    public static boolean aiming = false;
    public static float yaw, pitch;
    FloatValue predictScale = ValueBuilder.create(this, "Predict Scale").setDefaultFloatValue(5).setFloatStep(0.1f).setMinFloatValue(0f).setMaxFloatValue(20).build().getFloatValue();
    FloatValue height = ValueBuilder.create(this, "Height Scale").setDefaultFloatValue(1).setFloatStep(0.01f).setMinFloatValue(0f).setMaxFloatValue(2f).build().getFloatValue();
    FloatValue shotTime = ValueBuilder.create(this, "Shot Delay").setDefaultFloatValue(500).setFloatStep(10).setMinFloatValue(0f).setMaxFloatValue(2000f).build().getFloatValue();
    TimeHelper timer = new TimeHelper();

    double lastHealth = 0;
    double lastShield = 0;

    public static float[] getRotationsToHead(EntityPlayer entity) {
        RageBot module = (RageBot) BlinkFix.getInstance().getModuleManager().getModule(RageBot.class);

        return RotationUtils.getRotations(new Vec3(
                entity.posX + (entity.posX - entity.lastTickPosX) * module.predictScale.getCurrentValue(),
                entity.posY + (entity.getEyeHeight() * module.height.getCurrentValue()) + (entity.posY - entity.lastTickPosY) * module.predictScale.getCurrentValue(),
                entity.posZ + (entity.posZ - entity.lastTickPosZ) * module.predictScale.getCurrentValue()));
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @EventTarget
    public void onRender(EventRender e) {
        if (target != null) {
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            mc.entityRenderer.orientCamera(mc.timer.renderPartialTicks);

            final double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * predictScale.getCurrentValue() * e.getRenderPartialTicks();
            final double y = (target.lastTickPosY + (target.posY - target.lastTickPosY) * predictScale.getCurrentValue() * e.getRenderPartialTicks()) + target.getEyeHeight();
            final double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * predictScale.getCurrentValue() * e.getRenderPartialTicks();

            RenderUtils.drawLine(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY + (mc.thePlayer.getEyeHeight() * height.getCurrentValue()), mc.getRenderManager().renderPosZ, x, y, z, Color.RED, 1.5F);

            GlStateManager.resetColor();
            GlStateManager.popMatrix();
        }
    }

    public boolean checkDistance(Entity targetEntity, double range, float yaw, float pitch) {
        Vec3 playerEye = new Vec3(mc.thePlayer.lastTickPosX,
                mc.thePlayer.lastTickPosY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.lastTickPosZ);

        Vec3 playerLook = mc.thePlayer.getLook(yaw, pitch);
        Vec3 playerEyeBox = playerEye.addVector(playerLook.xCoord * range,
                playerLook.yCoord * range,
                playerLook.zCoord * range);

        float expands = targetEntity.getCollisionBorderSize();
        AxisAlignedBB targetBoundingBox = targetEntity.getEntityBoundingBox().offset(
                (targetEntity.posX - targetEntity.lastTickPosX) * predictScale.getCurrentValue(),
                (targetEntity.posY - targetEntity.lastTickPosY) * predictScale.getCurrentValue(),
                (targetEntity.posZ - targetEntity.lastTickPosZ) * predictScale.getCurrentValue()
        ).expand(expands, expands, expands);

        MovingObjectPosition position = targetBoundingBox.calculateIntercept(playerEye, playerEyeBox);

        if (position == null) {
            return false;
        }

        return position.hitVec.distanceTo(playerEye) <= range;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (mc.currentScreen != null) {
                return;
            }

            EntityPlayer currentTarget = getTarget();

            if (currentTarget != null && currentTarget != target) {
                lastHealth = currentTarget.getHealth();

                if (currentTarget.getHeldItem() != null && currentTarget.getHeldItem().getDisplayName().equals("§8防爆盾§8")) {
                    lastShield = currentTarget.getHeldItem().getItemDamage();
                }
            }

            target = currentTarget;

            if (target != null) {
                if (target.getHealth() < lastHealth) {
                    double deltaHealth = Math.round((lastHealth - target.getHealth()) * 10) / 10d;
                    BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.SUCCESS, "Damaged " + target.getName() + " for " + deltaHealth + " health!", 1000));
                }
                lastHealth = target.getHealth();

                if (target.getHeldItem() != null && target.getHeldItem().getDisplayName().equals("§8防爆盾§8")) {
                    if (target.getHeldItem().getItemDamage() > lastShield) {
                        double deltaShield = target.getHeldItem().getItemDamage() - lastShield;
                        BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.SUCCESS, "Damaged " + target.getName() + " for " + deltaShield + " on shield!", 1000));
                    }
                    lastShield = target.getHeldItem().getItemDamage();
                }
            }

            aiming = false;
            Item item = mc.thePlayer.getHeldItem().getItem();
            if (target != null && mc.thePlayer.canEntityBeSeen(target) && (item instanceof ItemTool || item instanceof ItemHoe || item instanceof ItemLead)) {
                float[] targetRotation = RageBot.getRotationsToHead(RageBot.target);
                // Aiming Check
                if (checkDistance(target, 1000, targetRotation[0], targetRotation[1]) && timer.delay(shotTime.getCurrentValue())) {
                    aiming = true;
                    yaw = targetRotation[0];
                    pitch = targetRotation[1];
                    timer.reset();
                }

                // Shoot Check
                if (checkDistance(target, 1000, RotationManager.lastRotations.x, RotationManager.lastRotations.y)) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                }
            }
        }
    }

    public EntityPlayer getTarget() {
        ArrayList<EntityPlayer> players = new ArrayList<>();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (Teams.isSameTeam(player)) {
                continue;
            }

            if (AntiBot.isBot(player)) {
                continue;
            }

            if (!mc.thePlayer.canEntityBeSeen(player)) {
                continue;
            }

            if (player.isDead || player.getHealth() <= 0) {
                continue;
            }

            if (AntiBot.isBot(player)) {
                continue;
            }

            if (FriendManager.isFriend(player)) {
                continue;
            }

            // Blink Fake Entity
            if (player.getEntityId() < 0) {
                continue;
            }

            players.add(player);
        }

        // Sort by range
        players.sort((o1, o2) -> (int) (o1.getDistanceToEntity(mc.thePlayer) * 1000 - o2.getDistanceToEntity(mc.thePlayer) * 1000));

        return players.isEmpty() ? null : players.get(0);
    }
}
