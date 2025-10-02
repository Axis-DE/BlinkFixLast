package axis.shiyan.wei.bluearchive.blinkfix.files;

import lombok.Getter;

import java.io.*;

@Getter

public abstract class ClientFile {
    private final String fileName;
    private final File file;

    public ClientFile(String fileName) {
        this.fileName = fileName;
        this.file = new File(FileManager.clientFolder, fileName);
    }

    public abstract void read(BufferedReader reader) throws IOException;

    public abstract void save(BufferedWriter writer) throws IOException;
}
