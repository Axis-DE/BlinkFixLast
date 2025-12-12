package moe.ichinomiya.naven.protocols.HuaYuTing.world;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EncryptedFile extends RandomAccessFile {
    private long position = 0L;

    public EncryptedFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    public int read(byte[] data) throws IOException {
        int var2 = super.read(data);
        if (var2 != -1) {
            EncryptedUtils.xorArrayWithOffset(data, this.position);
            this.position += var2;
        }

        return var2;
    }

    public int read(byte[] data, int offset, int length) throws IOException {
        int var4 = super.read(data, offset, length);
        if (var4 != -1) {
            EncryptedUtils.xorArrayRangeWithOffset(data, this.position, offset, length);
            this.position += var4;
        }

        return var4;
    }

    public int read() throws IOException {
        int read = super.read();
        if (read != -1) {
            int value = EncryptedUtils.xorWithOffset(read, this.position);
            ++this.position;
            return value;
        } else {
            return read;
        }
    }

    public void seek(long position) throws IOException {
        super.seek(position);
        this.position = position;
    }
}
