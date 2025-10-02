package axis.shiyan.wei.bluearchive.blinkfix.protocols.world;

import com.google.common.collect.Sets;
import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventTick;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventWorldUnload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;

public class WorldManager {
    @Getter
    private String worldName = null;
    @Getter
    private boolean init = false;
    private ChunkProviderClient providerClient;

    public void worldInit(WorldClient world) {
        if (!this.init) {
            providerClient = world.clientChunkProvider;
            this.init = true;
        }
    }

    @EventTarget
    public void onWorldUnload(EventWorldUnload e) {
        providerClient = null;
        init = false;
        worldName = null;
        ChunkFileManager.closeAllChunkFiles();
    }

    @EventTarget
    public void Method1179(EventTick event) {
        if (event.getType() == EventType.POST) {
            if (this.isInit()) {
                Entity player = Wrapper.getPlayer();
                if (player != null) {
                    if (player.worldObj instanceof WorldClient) {
                        if (player.ticksExisted % 10 == 0) {
                            HashSet<Long> loaded = Sets.newHashSet();
                            int distance = Wrapper.getRenderDistance();
                            double playerPosX = player.posX;
                            double playerPosZ = player.posZ;
                            int chunkX = MathHelper.floor_double(playerPosX / 16.0D);
                            int chunkZ = MathHelper.floor_double(playerPosZ / 16.0D);

                            for (int x = -distance; x <= distance; ++x) {
                                for (int z = -distance; z <= distance; ++z) {
                                    int disX = x + chunkX;
                                    int disZ = z + chunkZ;
                                    loaded.add(ChunkPos.asLong(disX, disZ));
                                    if (!this.isChunkLoaded(disX, disZ)) {
                                        this.loadChunk(disX, disZ);
                                    }
                                }
                            }

                            HashSet<Long> savedChunks = new HashSet<>(this.providerClient.getChunksBeingSaved());
                            savedChunks.removeAll(loaded);

                            for (Long var17 : savedChunks) {
                                Chunk var18 = this.providerClient.getChuck(var17);
                                this.unloadChunk(var18.xPosition, var18.zPosition);
                            }

                        }
                    }
                }
            }
        }
    }

    public void unloadChunk(int x, int z) {
        Minecraft.getMinecraft().theWorld.doPreChunk(x, z, false);
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public boolean loadChunk(int x, int z) {
        WorldClient var3 = Minecraft.getMinecraft().theWorld;
        var3.doPreChunk(x, z, true);
        if (this.isChunkLoaded(x, z)) {
            var3.markBlockRangeForRenderUpdate(x << 4, 0, z << 4, (x << 4) + 15, 256, (z << 4) + 15);
            return true;
        } else {
            return false;
        }
    }

    public boolean isChunkLoaded(int x, int z) {
        return this.providerClient.Method2867(x, z);
    }
}
