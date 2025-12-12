package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import moe.ichinomiya.naven.protocols.HuaYuTing.world.ChunkPos;
import moe.ichinomiya.naven.protocols.HuaYuTing.world.Wrapper;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ChunkProviderClient implements IChunkProvider {
    private static final Logger logger = LogManager.getLogger();
    private final Chunk blankChunk;

    @Getter
    private final LongHashMap<Chunk> chunkMapping = new LongHashMap();
    private final List<Chunk> chunkListing = Lists.newArrayList();
    private final World worldObj;
    private final AnvilChunkLoader loader = new AnvilChunkLoader(null);


    // Hua Yu Ting Start
    @Getter
    private Set chunksBeingSaved = Sets.newHashSet();
    private final HashMap<Long, Object> chunksToSave = new HashMap<>();

    public boolean Method2867(int var1, int var2) {
        return this.chunksBeingSaved.contains(ChunkPos.asLong(var1, var2));
    }

    public Chunk getChuck(long var1) {
        return (Chunk) this.chunksToSave.get(var1);
    }
    // End


    public ChunkProviderClient(World worldIn) {
        this.blankChunk = new EmptyChunk(worldIn, 0, 0);
        this.worldObj = worldIn;
    }

    public boolean chunkExists(int x, int z) {
        return true;
    }

    public void unloadChunk(int x, int z) {
        Chunk chunk = this.provideChunk(x, z);

        if (!chunk.isEmpty()) {
            chunk.onChunkUnload();
        }

        this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(x, z));
        this.chunkListing.remove(chunk);
    }

    public Chunk loadChunk(int chunkX, int chunkZ) {
        if (Wrapper.worldManager.getWorldName() == null) {
            Chunk chunk = new Chunk(this.worldObj, chunkX, chunkZ);
            this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);
            this.chunkListing.add(chunk);
            chunk.setChunkLoaded(true);
            return chunk;
        }

        try {
            Chunk chunk = loader.loadChunk(worldObj, chunkX, chunkZ);
            if (chunk != null) {
                this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);
                this.chunkListing.add(chunk);
                chunk.setChunkLoaded(true);

                long var4 = ChunkPos.asLong(chunkX, chunkZ);
                this.chunksBeingSaved.add(var4);
                this.chunksToSave.put(var4, chunk);
            }
            return chunk;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Chunk provideChunk(int x, int z) {
        Chunk chunk = this.chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
        return chunk == null ? this.blankChunk : chunk;
    }

    public boolean saveChunks(boolean saveAllChunks, IProgressUpdate progressCallback) {
        return true;
    }

    public void saveExtraData() {
    }

    public boolean unloadQueuedChunks() {
        long i = System.currentTimeMillis();

        for (Chunk chunk : this.chunkListing) {
            chunk.func_150804_b(System.currentTimeMillis() - i > 5L);
        }

        if (System.currentTimeMillis() - i > 100L) {
            logger.info("Warning: Clientside chunk ticking took {} ms", System.currentTimeMillis() - i);
        }

        return false;
    }

    public boolean canSave() {
        return false;
    }

    public void populate(IChunkProvider chunkProvider, int x, int z) {
    }

    public boolean populateChunk(IChunkProvider chunkProvider, Chunk chunkIn, int x, int z) {
        return false;
    }

    public String makeString() {
        return "MultiplayerChunkCache: " + this.chunkMapping.getNumHashElements() + ", " + this.chunkListing.size();
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return null;
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return null;
    }

    public int getLoadedChunkCount() {
        return this.chunkListing.size();
    }

    public void recreateStructures(Chunk chunkIn, int x, int z) {
    }

    public Chunk provideChunk(BlockPos blockPosIn) {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
