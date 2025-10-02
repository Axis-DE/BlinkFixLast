package axis.shiyan.wei.bluearchive.blinkfix.files.impl;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.exceptions.NoSuchModuleException;
import axis.shiyan.wei.bluearchive.blinkfix.files.ClientFile;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class ModuleFile extends ClientFile {
    private final static Logger logger = LogManager.getLogger(ModuleFile.class);

    public ModuleFile() {
        super("modules.cfg");
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(":", 3);

            if (split.length != 3) {
                logger.error("Failed to read line {}!", line);
                continue;
            }

            String name = split[0];
            int key = Integer.parseInt(split[1]);
            boolean enabled = Boolean.parseBoolean(split[2]);

            try {
                Module module = moduleManager.getModule(name);
                module.setKey(key);
                module.setEnabled(enabled);
            } catch (NoSuchModuleException e) {
                logger.error("Failed to find module {}!", name);
            }
        }
    }

    @Override
    public void save(BufferedWriter writer) throws IOException {
        ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();
        ArrayList<Module> modules = new ArrayList<>(moduleManager.getModules());

        for (Module module : modules) {
            writer.write(String.format("%s:%d:%s\n", module.getName(), module.getKey(), module.isEnabled()));
        }
    }
}
