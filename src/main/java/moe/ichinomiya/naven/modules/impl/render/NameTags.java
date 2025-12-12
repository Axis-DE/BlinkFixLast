package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.combat.AntiBot;
import moe.ichinomiya.naven.modules.impl.misc.HackerDetector;
import moe.ichinomiya.naven.modules.impl.misc.Teams;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.utils.font.BaseFontRender;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
//import dev.yalan.live.LiveComponent;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@jnic.JNICInclude
@ModuleInfo(name = "NameTags", description = "Renders name tags", category = Category.RENDER)
public class NameTags extends Module {
    BooleanValue mcf = ValueBuilder.create(this, "Middle Click Friend").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue teammate = ValueBuilder.create(this, "Show Team").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue bot = ValueBuilder.create(this, "Show Bots").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue sharedESP = ValueBuilder.create(this, "Shared ESP").setDefaultBooleanValue(true).build().getBooleanValue();

    ConcurrentHashMap<EntityLivingBase, double[]> entityPositions = new ConcurrentHashMap<>();

    ScaledResolution scaledRes;

    private String getDisplayName(EntityLivingBase ent) {
        String str = "";

//        if (ent instanceof EntityPlayer && ((EntityPlayer) ent).liveUser != null) {
//            str += LiveComponent.getLiveUserDisplayName(((EntityPlayer) ent).liveUser) + " ";
//        }

        if (HackerDetector.isCheating((EntityPlayer) ent)) {
            str += "\247cHacker \247f| ";
        }

        if (teammate.getCurrentValue() && Teams.isSameTeam(ent)) {
            str += "\247aTeam \247f| ";
        }

        if (bot.getCurrentValue() && AntiBot.isBot(ent)) {
            str += "\247cBot \247f| ";
        }

        str += ent.getDisplayName().getFormattedText();

        if (ent.getAbsorptionAmount() > 0) {
            str += "\247f | " + Math.round(ent.getHealth()) + "+" + Math.round(ent.getAbsorptionAmount()) + "HP";
        } else {
            str += "\247f | " + Math.round(ent.getHealth()) + "HP";
        }
        return str;
    }

    private void updatePositions() {
        entityPositions.clear();

        float pTicks = mc.timer.renderPartialTicks;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
                double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * pTicks - mc.getRenderManager().renderPosX;
                double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * pTicks - mc.getRenderManager().renderPosY;
                double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * pTicks - mc.getRenderManager().renderPosZ;
                y += entity.height + 0.25d;
                double[] convertTo2D = RenderUtils.convertTo2D(x, y, z);

                if (convertTo2D != null) {
                    if ((convertTo2D[2] >= 0.0D) && (convertTo2D[2] < 1.0D)) {
                        entityPositions.put((EntityPlayer) entity, convertTo2D);
                    }
                }
            }
        }

        if (sharedESP.getCurrentValue()) {
            for (SharedESPData data : EntityWatcher.getSharedESPData().values()) {
                data.renderPosition = null;

                double x = data.getPosX() - mc.getRenderManager().renderPosX;
                double y = data.getPosY() - mc.getRenderManager().renderPosY;
                double z = data.getPosZ() - mc.getRenderManager().renderPosZ;
                y += mc.thePlayer.height + 0.25d;

                double[] convertTo2D = RenderUtils.convertTo2D(x, y, z);

                if (convertTo2D != null) {
                    if ((convertTo2D[2] >= 0.0D) && (convertTo2D[2] < 1.0D)) {
                        data.renderPosition = convertTo2D;
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

    public static EntityPlayer aimingPlayer;

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            EntityWatcher.getSharedESPData().forEach((ign, data) -> {
                if (System.currentTimeMillis() - data.getUpdateTime() > 1000) {
                    EntityWatcher.getSharedESPData().remove(ign);
                }
            });

            if (mcf.getCurrentValue()) {
                for (EntityPlayer player : mc.theWorld.playerEntities) {
                    if (player.getEntityId() < 0) {
                        continue;
                    }
                    if (player != mc.thePlayer) {
                        if (isAiming(player, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) {
                            if (player.aimTicks >= 10) {
                                aimingPlayer = player;
                                break;
                            } else {
                                player.aimTicks++;
                            }
                        } else {
                            if (player.aimTicks > 0) {
                                player.aimTicks--;
                            }
                        }
                    }
                }

                if (aimingPlayer != null && aimingPlayer.aimTicks <= 0) {
                    aimingPlayer = null;
                }
            } else {
                aimingPlayer = null;
            }
        }
    }

    public static boolean isAiming(Entity targetEntity, float yaw, float pitch) {
        Vec3 playerEye = new Vec3(mc.thePlayer.posX,
                mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ);

        Vec3 playerLook = mc.thePlayer.getLook(yaw, pitch);
        Vec3 playerEyeBox = playerEye.addVector(playerLook.xCoord * 150,
                playerLook.yCoord * 150,
                playerLook.zCoord * 150);

        float expands = 0.25f;
        AxisAlignedBB targetBoundingBox = targetEntity.getEntityBoundingBox().expand(expands, expands, expands);
        MovingObjectPosition position = targetBoundingBox.calculateIntercept(playerEye, playerEyeBox);

        if (position == null) {
            return false;
        }

        return position.hitVec.distanceTo(playerEye) <= 150;
    }

    @EventTarget
    public void onMouseKey(EventMouseClick e) {
        if (!e.isState()) {
            if (e.getKey() == 2) {
                if (aimingPlayer != null) {
                    if (FriendManager.isFriend(aimingPlayer)) {
                        FriendManager.removeFriend(aimingPlayer);
                    } else {
                        FriendManager.addFriend(aimingPlayer);
                    }
                }
            }
        }
    }

    public void render(boolean shader) {
        try {
            for (EntityLivingBase ent : entityPositions.keySet()) {
                if (ent instanceof EntityPlayer) {
                    if (ent == mc.thePlayer) {
                        continue;
                    }

                    if (ent.getEntityId() < 0) {
                        continue;
                    }

                    double[] renderPositions = entityPositions.get(ent);

                    GlStateManager.pushMatrix();
                    BaseFontRender font = BlinkFix.getInstance().getFontManager().siyuan16;
                    GlStateManager.translate(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), 0.0D);
                    GlStateManager.translate(0.0D, -2.5D, 0.0D);

                    GlStateManager.scale(0.75, 0.75, 1);

                    String str = getDisplayName(ent);
                    float allWidth = font.getStringWidth(str.replaceAll("\247.", "")) + 8;

                    SmoothAnimationTimer animation = ((EntityPlayer) ent).aimingAnimation;
                    float addon = animation.value;

                    if (shader) {
                        RenderUtils.drawRect(-allWidth / 2, -14.0f - addon, allWidth / 2, 0, 0xFFFFFFFF);
                    } else {
                        if (aimingPlayer == ent) {
                            animation.target = 13;
                        } else {
                            animation.target = 0;
                        }
                        animation.update(true);

                        StencilUtils.write(false);
                        RenderUtils.drawRect(-allWidth / 2, -14f - addon, allWidth / 2, 0, 0xFFFFFFFF);
                        StencilUtils.erase(true);
                        RenderUtils.drawRect(-allWidth / 2, -29f, allWidth / 2, 0, Colors.getColor(0, 0, 0, 40));
                        float nowHealth = (float) Math.ceil(ent.getHealth() + ent.getAbsorptionAmount());
                        float maxHealth = ent.getMaxHealth() + ent.getAbsorptionAmount();
                        float healthP = MathUtils.clamp(nowHealth / maxHealth, 0f, 1f);
                        RenderUtils.drawRect(-allWidth / 2, -29f, allWidth / 2 - ((allWidth / 2) * (1 - healthP)) * 2, 0, Colors.getColor(0, 0, 0, 50));
                        font.drawStringWithShadow(str, -allWidth / 2 + 2.5f, -14F, Colors.WHITE.c);

                        String text = FriendManager.isFriend(ent) ? "\247aFriend" : "\247cEnemy";
                        font.drawStringWithShadow(text, -font.getStringWidth(text) / 2f, -25, Colors.WHITE.c);
                        StencilUtils.dispose();

                        List<ItemStack> itemsToRender = new ArrayList<>();
                        for (int i = 0; i < 5; i++) {
                            ItemStack stack = ent.getEquipmentInSlot(i);
                            if (stack != null) {
                                itemsToRender.add(stack);
                            }
                        }

                        int x = -(itemsToRender.size() * 9) - 3;

                        for (ItemStack stack : itemsToRender) {
                            GlStateManager.pushMatrix();
                            RenderHelper.enableGUIStandardItemLighting();
                            GlStateManager.disableAlpha();
                            GlStateManager.clear(256);
                            mc.getRenderItem().zLevel = -150.0F;

                            GlStateManager.disableLighting();
                            GlStateManager.disableDepth();
                            GlStateManager.disableBlend();
                            GlStateManager.enableLighting();
                            GlStateManager.enableDepth();
                            GlStateManager.disableLighting();
                            GlStateManager.disableDepth();
                            GlStateManager.disableTexture2D();
                            GlStateManager.disableAlpha();
                            GlStateManager.disableBlend();
                            GlStateManager.enableBlend();
                            GlStateManager.enableAlpha();
                            GlStateManager.enableTexture2D();
                            GlStateManager.enableLighting();
                            GlStateManager.enableDepth();

                            mc.getRenderItem().renderItemIntoGUI(stack, x + 6, -32 - addon);
                            BaseFontRender miniFont = BlinkFix.getInstance().getFontManager().siyuan16;
                            mc.getRenderItem().renderItemOverlays(miniFont, stack, x + 6, -32 - addon);
                            mc.getRenderItem().zLevel = 0.0F;
                            x += 20;
                            GlStateManager.enableAlpha();
                            RenderHelper.disableStandardItemLighting();
                            GlStateManager.popMatrix();

                        }
                    }
                    GlStateManager.popMatrix();
                }
            }

            if (sharedESP.getCurrentValue()) {
                for (Map.Entry<String, SharedESPData> data : EntityWatcher.getSharedESPData().entrySet()) {
                    SharedESPData espData = data.getValue();
                    String displayName = "\247bShared\247f | ";

                    if (HackerDetector.isCheating(data.getKey())) {
                        displayName += "\247cHacker \247f| ";
                    }

                    if (teammate.getCurrentValue() && Teams.isSameTeam(espData.getDisplayName())) {
                        displayName += "\247aTeam \247f| ";
                    }

                    displayName += espData.getDisplayName();

                    if (espData.getAbsorption() > 0) {
                        displayName += "\247f | " + Math.round(espData.getHealth()) + "+" + Math.round(espData.getAbsorption()) + "HP";
                    } else {
                        displayName += "\247f | " + Math.round(espData.getHealth()) + "HP";
                    }

                    double[] renderPositions = espData.getRenderPosition();

                    if (renderPositions == null) {
                        continue;
                    }

                    GlStateManager.pushMatrix();

                    BaseFontRender font = BlinkFix.getInstance().getFontManager().siyuan16;
                    GlStateManager.translate(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), 0.0D);
                    GlStateManager.translate(0.0D, -2.5D, 0.0D);

                    GlStateManager.scale(0.75, 0.75, 1);

                    float allWidth = font.getStringWidth(displayName.replaceAll("\247.", "")) + 8;

                    if (shader) {
                        RenderUtils.drawRect(-allWidth / 2, -14.0f, allWidth / 2, 0, 0xFFFFFFFF);
                    } else {
                        RenderUtils.drawRect(-allWidth / 2, -14.0f, allWidth / 2, 0, Colors.getColor(0, 0, 0, 40));
                        float nowHealth = (float) Math.ceil(espData.getHealth() + espData.getAbsorption());
                        float maxHealth = (float) (espData.getMaxHealth() + espData.getAbsorption());
                        float healthP = MathUtils.clamp(nowHealth / maxHealth, 0f, 1f);
                        RenderUtils.drawRect(-allWidth / 2, -14.0f, allWidth / 2 - ((allWidth / 2) * (1 - healthP)) * 2, 0, Colors.getColor(0, 0, 0, 50));
                        font.drawStringWithShadow(displayName, -allWidth / 2 + 2.5f, -14F, Colors.WHITE.c);
                    }
                    GlStateManager.popMatrix();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}
