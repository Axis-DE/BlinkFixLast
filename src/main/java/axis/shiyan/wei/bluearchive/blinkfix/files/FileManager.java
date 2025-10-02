package axis.shiyan.wei.bluearchive.blinkfix.files;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.files.impl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    public static final Logger logger = LogManager.getLogger(FileManager.class);
    public static final File clientFolder = new File(BlinkFix.CLIENT_NAME);

    private final List<ClientFile> files = new ArrayList<>();

    public FileManager() {
        if (!clientFolder.exists()) {
            if (clientFolder.mkdir()) {
                logger.info("Created client folder!");
            }
        }

        this.files.add(new SpammerFile());
        this.files.add(new KillSaysFile());
        this.files.add(new ModuleFile());
        this.files.add(new ValueFile());
        this.files.add(new FriendFile());
        this.files.add(new CGuiFile());
        this.files.add(new WidgetFile());
    }

    public void load() {
        for (ClientFile clientFile : files) {
            File file = clientFile.getFile();

            try {
                if (!file.exists()) {
                    if (file.createNewFile()) {
                        logger.info("Created file " + file.getName() + "!");
                        saveFile(clientFile);
                    }
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
                clientFile.read(reader);
                reader.close();
            } catch (IOException e) {
                logger.error("Failed to load file " + file.getName() + "!", e);
                saveFile(clientFile);
            }
        }
    }

    public void save() {
        for (ClientFile clientFile : files) {
            saveFile(clientFile);
        }
        logger.info("Saved all files!");
    }

    private void saveFile(ClientFile clientFile) {
        File file = clientFile.getFile();

        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    logger.info("Created file " + file.getName() + "!");
                }
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8));
            clientFile.save(writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
