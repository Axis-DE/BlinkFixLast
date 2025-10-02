package axis.shiyan.wei.bluearchive.blinkfix.protocols.world;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class ChunkFile {
    public static final Logger logger = LogManager.getLogger();
    private final int[] chunkOffsets = new int[1024];
    private List chunkExists;
    private RandomAccessFile file;
    private final int[] Field1517 = new int[1024];
    private final File saveFile;

    public void close() throws IOException {
        if (this.file != null) {
            this.file.close();
        }
    }

    private boolean isChunkOutOfBounds(int var1, int var2) {
        return var1 < 0 || var1 >= 32 || var2 < 0 || var2 >= 32;
    }

    public ChunkFile(File var1) {
        this.saveFile = var1;

        try {
            this.file = new EncryptedFile(var1, "r");
            int var2 = (int)this.file.length() / 4096;
            this.chunkExists = Lists.newArrayListWithCapacity(var2);

            int var3;
            for(var3 = 0; var3 < var2; ++var3) {
                this.chunkExists.add(true);
            }

            this.chunkExists.set(0, false);
            this.chunkExists.set(1, false);
            this.file.seek(0L);

            int var4;
            for(var3 = 0; var3 < 1024; ++var3) {
                var4 = this.file.readInt();
                this.chunkOffsets[var3] = var4;
                int var5 = var4 & 255;
                if (var5 == 255 && var4 >> 8 <= this.chunkExists.size()) {
                    this.file.seek((var4 >> 8) * 4096L);
                    var5 = (this.file.readInt() + 4) / 4096 + 1;
                    this.file.seek(var3 * 4 + 4);
                }

                if (var4 != 0 && (var4 >> 8) + var5 <= this.chunkExists.size()) {
                    for(int var6 = 0; var6 < var5; ++var6) {
                        this.chunkExists.set((var4 >> 8) + var6, false);
                    }
                } else if (var5 > 0) {
                    logger.warn("Invalid chunk: ({}, {}) Offset: {} Length: {} runs off end file. {}", var3 % 32, var3 / 32, var4 >> 8, var5, var1);
                }
            }

            for(var3 = 0; var3 < 1024; ++var3) {
                var4 = this.file.readInt();
                this.Field1517[var3] = var4;
            }
        } catch (IOException var7) {
            var7.printStackTrace();
        }

    }

    public synchronized DataInputStream getChunkDataInputStream(int var1, int var2) {
        if (this.isChunkOutOfBounds(var1, var2)) {
            return null;
        } else {
            try {
                int var3 = this.getChunkOffset(var1, var2);
                if (var3 == 0) {
                    return null;
                } else {
                    int var4 = var3 >> 8;
                    int var5 = var3 & 255;
                    if (var5 == 255) {
                        this.file.seek(var4 * 4096L);
                        var5 = (this.file.readInt() + 4) / 4096 + 1;
                    }

                    if (var4 + var5 > this.chunkExists.size()) {
                        return null;
                    } else {
                        this.file.seek(var4 * 4096L);
                        int var6 = this.file.readInt();
                        if (var6 > 4096 * var5) {
                            logger.warn("Invalid chunk: ({}, {}) Offset: {} Invalid Size: {}>{} {}", var1, var2, var4, var6, var5 * 4096, this.saveFile);
                            return null;
                        } else if (var6 <= 0) {
                            logger.warn("Invalid chunk: ({}, {}) Offset: {} Invalid Size: {} {}", var1, var2, var4, var6, this.saveFile);
                            return null;
                        } else {
                            byte var7 = this.file.readByte();
                            byte[] var8;
                            if (var7 == 1) {
                                var8 = new byte[var6 - 1];
                                this.file.read(var8);
                                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(var8))));
                            } else if (var7 == 2) {
                                var8 = new byte[var6 - 1];
                                this.file.read(var8);
                                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(var8))));
                            } else {
                                return null;
                            }
                        }
                    }
                }
            } catch (IOException var9) {
                return null;
            }
        }
    }

    private int getChunkOffset(int var1, int var2) {
        return this.chunkOffsets[var1 + var2 * 32];
    }

    public boolean isChunkSaved(int var1, int var2) {
        return this.getChunkOffset(var1, var2) != 0;
    }
}
