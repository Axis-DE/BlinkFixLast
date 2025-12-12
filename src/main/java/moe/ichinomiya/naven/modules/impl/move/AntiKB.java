//package moe.ichinomiya.naven.modules.impl.move;
//
//import moe.ichinomiya.naven.BlinkFix;
//import moe.ichinomiya.naven.events.api.EventTarget;
//import moe.ichinomiya.naven.events.api.types.EventType;
//import moe.ichinomiya.naven.events.impl.EventPacket;
//import moe.ichinomiya.naven.events.impl.EventUpdate;
//import moe.ichinomiya.naven.modules.Category;
//import moe.ichinomiya.naven.modules.Module;
//import moe.ichinomiya.naven.modules.ModuleInfo;
//import moe.ichinomiya.naven.modules.impl.combat.Aura;
//import moe.ichinomiya.naven.utils.PacketUtil;
//import moe.ichinomiya.naven.utils.TimeHelper;
//import moe.ichinomiya.naven.values.ValueBuilder;
//import moe.ichinomiya.naven.values.impl.BooleanValue;
//import moe.ichinomiya.naven.values.impl.FloatValue;
//import de.florianmichael.viamcp.fixes.AttackOrder;
//import de.florianmichael.vialoadingbase.ViaLoadingBase;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockSoulSand;
//import net.minecraft.client.gui.GuiGameOver;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.network.play.client.C03PacketPlayer;
//import net.minecraft.network.play.client.C07PacketPlayerDigging;
//import net.minecraft.network.play.client.C0BPacketEntityAction;
//import net.minecraft.network.play.server.S08PacketPlayerPosLook;
//import net.minecraft.network.play.server.S12PacketEntityVelocity;
//import net.minecraft.network.play.server.S27PacketExplosion;
//import net.minecraft.util.AxisAlignedBB;
//import net.minecraft.util.BlockPos;
//import net.minecraft.util.EnumFacing;
//import net.minecraft.util.MathHelper;
//import net.minecraft.util.MovingObjectPosition;
//import net.minecraft.world.WorldSettings;
//
//@ModuleInfo(name = "AntiKB", description = "Grim anti-knockback", category = Category.MOVEMENT)
//public class AntiKB extends Module {
//    private final BooleanValue flagCheckValue = ValueBuilder.create(this, "Flag Check")
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//
//    private final FloatValue flagTicksValue = ValueBuilder.create(this, "Flag Ticks")
//            .setDefaultFloatValue(6.0F)
//            .setMinFloatValue(0.0F)
//            .setMaxFloatValue(30.0F)
//            .setFloatStep(1.0F)
//            .setVisibility(() -> flagCheckValue.getCurrentValue())
//            .build()
//            .getFloatValue();
//
//    private final FloatValue attackCountValue = ValueBuilder.create(this, "Attack Counts")
//            .setDefaultFloatValue(12.0F)
//            .setMinFloatValue(1.0F)
//            .setMaxFloatValue(16.0F)
//            .setFloatStep(1.0F)
//            .build()
//            .getFloatValue();
//
//    private final BooleanValue fireCheckValue = ValueBuilder.create(this, "Fire Check")
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//
//    private final BooleanValue waterCheckValue = ValueBuilder.create(this, "Water Check")
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//
//    private final BooleanValue fallCheckValue = ValueBuilder.create(this, "Fall Check")
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//
//    private final BooleanValue consumeCheck = ValueBuilder.create(this, "Consumable Check")
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//
//    private final BooleanValue raycastValue = ValueBuilder.create(this, "Ray cast")
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//
//    private boolean grim_1_17Velocity;
//    private boolean attacked;
//    private double reduceXZ;
//    private int flags;
//    private final TimeHelper flagTimer = new TimeHelper();
//
//    @Override
//    public boolean onDisable() {
//        grim_1_17Velocity = false;
//        attacked = false;
//        flags = 0;
//        return false;
//    }
//
//    @Override
//    public boolean onEnable() {
//        grim_1_17Velocity = false;
//        attacked = false;
//        flags = 0;
//        flagTimer.reset();
//        return false;
//    }
//
//    @EventTarget
//    public void onPacket(EventPacket e) {
//        if (e.getType() == EventType.RECEIVE && !e.isCancelled()) {
//            if (e.getPacket() instanceof S12PacketEntityVelocity) {
//                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) e.getPacket();
//                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
//                    handleGrimVelocity(packet, e);
//                }
//            }
//
//            if (e.getPacket() instanceof S08PacketPlayerPosLook) {
//                flagTimer.reset();
//                if (flagCheckValue.getCurrentValue()) {
//                    flags = (int) flagTicksValue.getCurrentValue();
//                }
//            }
//
//            if (e.getPacket() instanceof S27PacketExplosion &&
//                    ViaLoadingBase.getInstance().getTargetVersion().getVersion() >= 755) {
//                e.setCancelled(true);
//                grim_1_17Velocity = true;
//            }
//        }
//    }
//
//    private void handleGrimVelocity(S12PacketEntityVelocity s12, EventPacket e) {
//        if (flags != 0) {
//            return;
//        }
//
//        if (mc.thePlayer.isDead) {
//            return;
//        }
//
//        if (mc.currentScreen instanceof GuiGameOver) {
//            return;
//        }
//
//        if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR) {
//            return;
//        }
//
//        if (mc.thePlayer.isOnLadder()) {
//            return;
//        }
//
//        if (mc.thePlayer.isBurning() && fireCheckValue.getCurrentValue()) {
//            return;
//        }
//
//        if (mc.thePlayer.isInWater() && waterCheckValue.getCurrentValue()) {
//            return;
//        }
//
//        if (mc.thePlayer.fallDistance > 1.5F && fallCheckValue.getCurrentValue()) {
//            return;
//        }
//
//        if (flagCheckValue.getCurrentValue() && !flagTimer.delay(1000L)) {
//            return;
//        }
//
//        if (mc.thePlayer.isUsingItem() && consumeCheck.getCurrentValue()) {
//            return;
//        }
//
//        if (soulSandCheck()) {
//            return;
//        }
//
//        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() >= 755) {
//            e.setCancelled(true);
//            grim_1_17Velocity = true;
//        } else {
//            double horizontalStrength = Math.sqrt(s12.getMotionX() * s12.getMotionX() + s12.getMotionZ() * s12.getMotionZ());
//            if (horizontalStrength <= 1000.0F) {
//                return;
//            }
//
//            MovingObjectPosition mouse = mc.objectMouseOver;
//            Entity entity = null;
//            reduceXZ = 1.0F;
//
//            if (mouse != null && mouse.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY &&
//                    mouse.entityHit instanceof EntityLivingBase &&
//                    mc.thePlayer.getDistanceToEntity(mouse.entityHit) <= 3.0F) {
//                entity = mouse.entityHit;
//            }
//
//            if (entity == null && !raycastValue.getCurrentValue()) {
//                Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
//                if (aura != null && aura.isEnabled() && Aura.target != null) {
//                    entity = Aura.target;
//                }
//            }
//
//            boolean state = mc.thePlayer.serverSprintState;
//            if (entity != null) {
//                if (!state) {
//                    PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
//                }
//
//                // 发送攻击包
//                int count = (int) attackCountValue.getCurrentValue();
//                for (int i = 1; i <= count; ++i) {
//                    AttackOrder.sendFixedAttack(mc.thePlayer, entity);
//                }
//
//                if (!state) {
//                    PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
//                }
//
//                attacked = true;
//                reduceXZ = 0.07776;
//            }
//        }
//    }
//
//    @EventTarget
//    public void onUpdate(EventUpdate event) {
//        handleGrimUpdate();
//    }
//
//    private void handleGrimUpdate() {
//        if (grim_1_17Velocity) {
//            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(
//                    mc.thePlayer.posX,
//                    mc.thePlayer.posY,
//                    mc.thePlayer.posZ,
//                    mc.thePlayer.rotationYaw,
//                    mc.thePlayer.rotationPitch,
//                    mc.thePlayer.onGround
//            ));
//            BlockPos pos = new BlockPos(mc.thePlayer).up();
//            PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(
//                    C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
//                    pos,
//                    EnumFacing.DOWN
//            ));
//            PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(
//                    C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
//                    pos,
//                    EnumFacing.DOWN
//            ));
//            grim_1_17Velocity = false;
//        }
//
//        if (flagCheckValue.getCurrentValue() && flags > 0) {
//            flags--;
//        }
//
//        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() > 47) {
//            if (attacked) {
//                mc.thePlayer.motionX *= reduceXZ;
//                mc.thePlayer.motionZ *= reduceXZ;
//                attacked = false;
//            }
//        } else if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
//            mc.thePlayer.addVelocity(-1.3E-10, -1.3E-10, -1.3E-10);
//            mc.thePlayer.setSprinting(false);
//        }
//    }
//
//    private boolean soulSandCheck() {
//        AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().contract(0.001, 0.001, 0.001);
//        int minX = MathHelper.floor_double(bb.minX);
//        int maxX = MathHelper.floor_double(bb.maxX + 1.0F);
//        int minY = MathHelper.floor_double(bb.minY);
//        int maxY = MathHelper.floor_double(bb.maxY + 1.0F);
//        int minZ = MathHelper.floor_double(bb.minZ);
//        int maxZ = MathHelper.floor_double(bb.maxZ + 1.0F);
//
//        for (int x = minX; x < maxX; x++) {
//            for (int y = minY; y < maxY; y++) {
//                for (int z = minZ; z < maxZ; z++) {
//                    BlockPos pos = new BlockPos(x, y, z);
//                    Block block = mc.theWorld.getBlockState(pos).getBlock();
//                    if (block instanceof BlockSoulSand) {
//                        return true;
//                    }
//                }
//            }
//        }
//
//        return false;
//    }
//}