package moe.ichinomiya.naven.protocols.HuaYuTing.world;

import com.google.common.collect.Maps;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ChunkFileManager {
    private static final Map<File, ChunkFile> fileManagerMap = Maps.newHashMap();

    public static synchronized void closeAllChunkFiles() {

        for (ChunkFile var1 : fileManagerMap.values()) {
            try {
                if (var1 != null) {
                    var1.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileManagerMap.clear();
    }

    public static synchronized ChunkFile getChunkFile(File file, int x, int z) {
        File var3 = new File(file, "region");
        File var4 = new File(var3, "r." + (x >> 5) + "." + (z >> 5) + ".mca");
        ChunkFile var5 = fileManagerMap.get(var4);
        if (var5 != null) {
            return var5;
        } else if (var3.exists() && var4.exists()) {
            if (fileManagerMap.size() >= 256) {
                closeAllChunkFiles();
            }

            ChunkFile var6 = new ChunkFile(var4);
            fileManagerMap.put(var4, var6);
            return var6;
        } else {
            return null;
        }
    }

    public static DataInputStream getChunkDataInputStream(File file, int x, int z) {
        ChunkFile var3 = getChunkFile(file, x, z);
        return var3 == null ? null : var3.getChunkDataInputStream(x & 31, z & 31);
    }
}
