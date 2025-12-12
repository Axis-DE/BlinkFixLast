package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventRender;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.ModuleManager;
import moe.ichinomiya.naven.modules.impl.combat.Aura;
import moe.ichinomiya.naven.modules.impl.move.Blink;
import moe.ichinomiya.naven.modules.impl.move.Scaffold;
import moe.ichinomiya.naven.modules.impl.move.Stuck;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ModuleInfo(name = "ChestAura", description = "Automatically opens chests", category = Category.MISC)
public class ChestAura extends Module {
    public Vector2f rotation;
    private final BooleanValue renderChest = ValueBuilder.create(this, "Render Targets").setDefaultBooleanValue(true).build().getBooleanValue();

    private final BooleanValue chest = ValueBuilder.create(this, "Chest").setDefaultBooleanValue(true).build().getBooleanValue();
    private final BooleanValue furnace = ValueBuilder.create(this, "Furnace").setDefaultBooleanValue(true).build().getBooleanValue();
    private final BooleanValue brewingStand = ValueBuilder.create(this, "Brewing Stand").setDefaultBooleanValue(true).build().getBooleanValue();

    private final int chestColor = new Color(0, 255, 0, 50).getRGB();
    private final int openedChestColor = new Color(255, 0, 0, 50).getRGB();
    private final TimeHelper openTimer = new TimeHelper();

    @Override
    public boolean onDisable() {
        rotation = null;
        return false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();

            rotation = null;
            if (mc.currentScreen != null || moduleManager.getModule(Scaffold.class).isEnabled() || moduleManager.getModule(Blink.class).isEnabled() || moduleManager.getModule(Stuck.class).isEnabled() || Aura.target != null || ContainerStealer.isStealing() || mc.thePlayer.isUsingItem()) {
                return;
            }

            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getDisplayName().equals("§b职业选择菜单")) {
                return;
            }

            Set<BlockPos> allBlockPos = new HashSet<>();

            if (chest.getCurrentValue()) {
                allBlockPos.addAll(WorldMonitor.getChests());
            }

            if (furnace.getCurrentValue()) {
                allBlockPos.addAll(WorldMonitor.getFurnaces());
            }

            if (brewingStand.getCurrentValue()) {
                allBlockPos.addAll(WorldMonitor.getBrewingStand());
            }

            allBlockPos.removeIf(pos -> WorldMonitor.getOpenedChests().contains(pos));
            allBlockPos.removeIf(pos -> mc.thePlayer.getDistance(pos.getX(), pos.getY(), pos.getZ()) > 10);

            double minDistance = Double.MAX_VALUE;
            Vector2f bestRotation = null;

            for (BlockPos pos : allBlockPos) {
                List<Vec3> possibleHit = getAllPossibleHit(pos);

                for (Vec3 vec3 : possibleHit) {
                    for (double possibleEyeHeight : RotationUtils.getPossibleEyeHeights()) {
                        Vec3 eyeHeight = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + possibleEyeHeight, mc.thePlayer.posZ);
                        Vector2f rotations = RotationUtils.getRotations(eyeHeight, vec3);

                       MovingObjectPosition position = RayTraceUtils.rayCast(1, new float[]{rotations.x, rotations.y}, eyeHeight);

                        if (position != null && position.getBlockPos().equals(pos)) {
                            double distance = eyeHeight.distanceTo(position.hitVec);

                            if (distance < minDistance) {
                                minDistance = distance;
                                bestRotation = rotations;
                            }
                        }
                    }
                }
            }

            if (minDistance < 4) {
                rotation = bestRotation;
                rotation.x += RandomUtils.nextFloat(0, 0.5f) - 0.25f;
            }

            if (RotationManager.lastRotations != null) {
                for (double possibleEyeHeight : RotationUtils.getPossibleEyeHeights()) {
                    Vec3 eyeHeight = new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + possibleEyeHeight, mc.thePlayer.lastTickPosZ);
                    MovingObjectPosition position = RayTraceUtils.rayCast(1, new float[]{RotationManager.lastRotations.x, RotationManager.lastRotations.y}, eyeHeight);

                    if (position != null && position.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && allBlockPos.contains(position.getBlockPos()) && openTimer.delay(300)) {
                        Block block = mc.theWorld.getBlockState(position.getBlockPos()).getBlock();
                        if (block == Blocks.furnace || block == Blocks.brewing_stand) {
                            WorldMonitor.getOpenedChests().add(position.getBlockPos());
                        }

                        mc.getNetHandler().getNetworkManager().sendPacket(new C08PacketPlayerBlockPlacement(position.getBlockPos(), position.sideHit.getIndex(), mc.thePlayer.getHeldItem(), 0, 0, 0));
                        openTimer.reset();
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRenderChest(EventRender e) {
        if (renderChest.getCurrentValue()) {
            if (brewingStand.getCurrentValue()) {
                for (BlockPos blockPos : WorldMonitor.getBrewingStand()) {
                    Render3DUtils.drawSolidBlockESP(blockPos, WorldMonitor.getOpenedChests().contains(blockPos) ? openedChestColor : chestColor, 1);
                }
            }

            if (furnace.getCurrentValue()) {
                for (BlockPos blockPos : WorldMonitor.getFurnaces()) {
                    Render3DUtils.drawSolidBlockESP(blockPos, WorldMonitor.getOpenedChests().contains(blockPos) ? openedChestColor : chestColor, 1);
                }
            }

            if (chest.getCurrentValue()) {
                for (BlockPos blockPos : WorldMonitor.getChests()) {
                    Render3DUtils.drawSolidBlockESP(blockPos, WorldMonitor.getOpenedChests().contains(blockPos) ? openedChestColor : chestColor, 1);
                }
            }
        }
    }

    public List<Vec3> getAllPossibleHit(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        List<Vec3> list = new ArrayList<>();

        for (double x = block.getBlockBoundsMinX(); x <= block.getBlockBoundsMaxX(); x += 0.2f) {
            for (double y = block.getBlockBoundsMinY(); y <= block.getBlockBoundsMaxY(); y += 0.2f) {
                for (double z = block.getBlockBoundsMinZ(); z <= block.getBlockBoundsMaxZ(); z += 0.2f) {
                    list.add(new Vec3(pos.getX() + x, pos.getY() + y, pos.getZ() + z));
                }
            }
        }

        return list;
    }
}
