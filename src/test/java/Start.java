import me.djtheredstoner.devauth.common.DevAuth;
import net.minecraft.client.main.Main;

import java.io.File;
import java.util.Arrays;

public class Start {
    public static void main(String[] args) {
        File versionFolder = new File("versions/1.8.9");
        if (versionFolder.exists() && versionFolder.isDirectory()) {
            File[] files = versionFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && file.getName().startsWith("native")) {
                        String nativePath = file.getAbsolutePath();
                        System.out.println("Setting LWJGL Library Path to: " + nativePath);
                        System.setProperty("org.lwjgl.librarypath", nativePath);
                        break;
                    }
                }
            }
        }

        // Dev Auth should be default enabled!
        System.setProperty("devauth.enabled", "true");

        DevAuth devAuth = new DevAuth();
        Main.main(devAuth.processArguments(concat(new String[]{"--version", "MavenMCP",
                "--assetsDir", "assets",
                "--assetIndex", "1.8",
                "--userProperties", "{}"}, args)));
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
