package moe.ichinomiya.naven.utils;

import lombok.Getter;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventRenderBlock;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockFurnace;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WorldMonitor {
    private static final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private static final Set<BlockPos> furnaces = new CopyOnWriteArraySet<>();
    @Getter
    private static final Set<BlockPos> brewingStand = new CopyOnWriteArraySet<>();
    @Getter
    private static final Set<BlockPos> chests = new CopyOnWriteArraySet<>();
    @Getter
    private static final Set<BlockPos> openedChests = new CopyOnWriteArraySet<>();
    @Getter
    private static final Set<BlockPos> fires = new CopyOnWriteArraySet<>();

    @EventTarget
    public void onUpdate(EventMotion e) {
        if (e.getType() == EventType.POST) {
            furnaces.removeIf(pos -> mc.theWorld.getBlockState(pos).getBlock() != Blocks.furnace);
            brewingStand.removeIf(pos -> mc.theWorld.getBlockState(pos).getBlock() != Blocks.brewing_stand);
            fires.removeIf(pos -> mc.theWorld.getBlockState(pos).getBlock() != Blocks.fire);

            for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                if (tileEntity instanceof TileEntityChest) {
                    BlockPos pos = tileEntity.getPos();
                    chests.add(pos);
                }
            }

            chests.removeIf(pos -> {
                Block block = mc.theWorld.getBlockState(pos).getBlock();
                return block != Blocks.chest && block != Blocks.trapped_chest;
            });
        }
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        if (e.getType() == EventType.JOIN_GAME) {
            furnaces.clear();
            brewingStand.clear();
            chests.clear();
            openedChests.clear();
            fires.clear();
        }
    }

    @EventTarget
    public void onRenderBlock(EventRenderBlock e) {
        BlockPos blockPos = new BlockPos(e.getBlockPos().getX(), e.getBlockPos().getY(), e.getBlockPos().getZ());

        if (e.getBlock() instanceof BlockFurnace) {
            furnaces.add(blockPos);
        }

        if (e.getBlock() instanceof BlockBrewingStand) {
            brewingStand.add(blockPos);
        }

        if (e.getBlock() instanceof BlockFire) {
            fires.add(blockPos);
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getType() == EventType.RECEIVE) {
            if (e.getPacket() instanceof S24PacketBlockAction) {
                S24PacketBlockAction packet = (S24PacketBlockAction) e.getPacket();
                if (packet.getData1() == 1 && packet.getData2() == 1 && (packet.getBlockType() == Blocks.chest || packet.getBlockType() == Blocks.trapped_chest)) {
                    openedChests.add(packet.getBlockPosition());
                }
            }

            if (e.getPacket() instanceof S29PacketSoundEffect) {
                S29PacketSoundEffect packet = (S29PacketSoundEffect) e.getPacket();
                if (packet.getSoundName().equals("random.chestopen")) {
                    BlockPos blockPos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                    openedChests.add(blockPos);
                }
            }
        }
    }
}
