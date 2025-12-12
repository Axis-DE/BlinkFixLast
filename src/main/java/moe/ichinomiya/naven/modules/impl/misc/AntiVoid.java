//package moe.ichinomiya.naven.modules.impl.misc;
//
//import moe.ichinomiya.naven.BlinkFix;
//import moe.ichinomiya.naven.events.api.EventTarget;
//import moe.ichinomiya.naven.events.impl.EventMotion;
//import moe.ichinomiya.naven.events.impl.EventPacket;
//import moe.ichinomiya.naven.modules.Category;
//import moe.ichinomiya.naven.modules.Module;
//import moe.ichinomiya.naven.modules.ModuleInfo;
//import moe.ichinomiya.naven.modules.impl.move.Scaffold;
//import moe.ichinomiya.naven.modules.impl.move.Stuck;
//import moe.ichinomiya.naven.utils.ChatUtils;
//import moe.ichinomiya.naven.utils.FallingPlayer;
//import moe.ichinomiya.naven.utils.MoveUtils;
//import moe.ichinomiya.naven.utils.TimeHelper;
//import moe.ichinomiya.naven.values.ValueBuilder;
//import moe.ichinomiya.naven.values.impl.BooleanValue;
//import moe.ichinomiya.naven.values.impl.FloatValue;
//import net.minecraft.util.BlockPos;
//import net.minecraft.util.EnumFacing;
//import net.minecraft.init.Items;
//import net.minecraft.network.play.server.S08PacketPlayerPosLook;
//
//@ModuleInfo(name = "AntiVoid", description = "Automatically stuck when you over void!", category = Category.MISC)
//public class AntiVoid extends Module {
//    FloatValue fallDistance = ValueBuilder.create(this, "Fall Distance").setDefaultFloatValue(10).setFloatStep(0.1f).setMinFloatValue(3f).setMaxFloatValue(15f).build().getFloatValue();
//    FloatValue fallDistance2 = ValueBuilder.create(this, "Scaffold Fall Distance").setDefaultFloatValue(3).setFloatStep(1f).setMinFloatValue(3f).setMaxFloatValue(8f).build().getFloatValue();
//    BooleanValue stuck = ValueBuilder.create(this,"Stuck").setDefaultBooleanValue(false).build().getBooleanValue();
//    BooleanValue scaffold = ValueBuilder.create(this,"Toggle Scaffold").setDefaultBooleanValue(true).build().getBooleanValue();
//    TimeHelper timer = new TimeHelper();
//    private boolean scaffoldEnabled;
//    @EventTarget
//    public void onMotion(EventMotion e) {
//        if (mc.thePlayer == null) return;
//        if (mc.thePlayer.isSpectator()) return;
//        if (!AutoClip.work && mc.thePlayer.ticksExisted > 20 && stuck.getCurrentValue()) {
//            Stuck stuck = (Stuck) BlinkFix.getInstance().getModuleManager().getModule(Stuck.class);
//            if (stuck.isEnabled()) {
//                timer.reset();
//            }
//
//            int pearlSlot = -1;
//
//            // Get the pearl slot
//            for (int i = 0; i < 9; i++) {
//                if (mc.thePlayer.inventory.getStackInSlot(i) != null && mc.thePlayer.inventory.getStackInSlot(i).getItem() == Items.ender_pearl) {
//                    pearlSlot = i;
//                    break;
//                }
//            }
//
//            if (((pearlSlot != -1 && mc.thePlayer.fallDistance > fallDistance.getCurrentValue()) || (mc.thePlayer.posY + mc.thePlayer.motionY < -50))
//                    && MoveUtils.isBoundingBoxOverVoid(mc.thePlayer.getEntityBoundingBox()) && !mc.thePlayer.onGround && timer.delay(1000)) {
//                if (!stuck.isEnabled()) {
//                    stuck.toggle();
//                }
//            }
//        }
//        if (scaffold.getCurrentValue()){
//            Scaffold scaffold = (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
//            if (mc.thePlayer.onGround) {
//                if (scaffoldEnabled) {
//                    scaffold.setEnabled(false);
//                    scaffoldEnabled = false;
//                }
//                return;
//            }
//            if (mc.thePlayer.motionY < 0.1 &&
//                    new FallingPlayer(mc.thePlayer).findCollision(60) == null &&
//                    !FallingPlayer.isBlockUnder(mc.thePlayer.posY + mc.thePlayer.getEyeHeight()) &&
//                    mc.thePlayer.fallDistance > fallDistance2.getCurrentValue()) {
//
//
//                if (mc.thePlayer.motionY >= -1 && !scaffold.isEnabled() && !BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()
//                        && hasBlockNearPlayer(7) && !mc.thePlayer.capabilities.isFlying) {
//                    scaffoldEnabled = true;
//                    scaffold.setEnabled(true);
//                    ChatUtils.addChatMessage("gg!");
//                    scaffold.bigVelocityTick = 10;
//                }
//                if (scaffold.isEnabled() && !hasBlockNearPlayer(7)){
//                    scaffold.setEnabled(false);
//                    scaffoldEnabled = false;
//                }
//            }
//        }
//    }
//    private boolean hasBlockNearPlayer(int radius) {
//        BlockPos playerPos = new BlockPos(mc.thePlayer);
//
//        for (int x = -radius; x <= radius; x++) {
//            for (int y = -radius; y <= radius; y++) {
//                for (int z = -radius; z <= radius; z++) {
//                    BlockPos checkPos = playerPos.add(x, y, z);
//                    if (isValidPlacePos(checkPos)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    private boolean isValidPlacePos(BlockPos pos) {
//        if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof net.minecraft.block.BlockAir)) {
//            return false;
//        }
//        for (EnumFacing facing : EnumFacing.values()) {
//            BlockPos neighborPos = pos.offset(facing);
//            if (mc.theWorld.getBlockState(neighborPos).getBlock().isBlockNormalCube()) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//    @Override
//    public boolean onDisable() {
//        if (scaffoldEnabled) {
//            Scaffold scaffold = (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
//            scaffold.setEnabled(false);
//            scaffoldEnabled = false;
//        }
//        return super.onDisable();
//    }
//    @EventTarget
//    public void onPacket(EventPacket e) {
//        if (e.getPacket() instanceof S08PacketPlayerPosLook) {
//            timer.reset();
//        }
//    }
//}
