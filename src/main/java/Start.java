import net.minecraft.client.main.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

//public class Start {
//    public static void main(String[] args) throws IOException {
//        InputStream dllStream = Start.class.getResourceAsStream("assets/minecraft/lwjgl64.dll");
//        File tempDir = new File(System.getProperty("java.io.tmpdir"));
//        File dllFile = new File(tempDir, "lwjgl64.dll");
//        Files.copy(dllStream, dllFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//
//        String nativePath = tempDir.getAbsolutePath();
//
//        System.setProperty("java.library.path",nativePath);
//        System.setProperty("org.lwjgl.librarypath",nativePath);
//        Main.main(concat(new String[] {"--version", "mcp", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}"}, args));
//    }
//
//    public static <T> T[] concat(T[] first, T[] second) {
//        T[] result = Arrays.copyOf(first, first.length + second.length);
//        System.arraycopy(second, 0, result, first.length, second.length);
//        return result;
//    }
//}

import net.minecraft.client.main.Main;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public class Start {
    public static void main(String[] args) {
        // 设置 LWJGL 本地库路径
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

        // 生成离线模式参数
        String username = "Player"; // 可以修改为自定义用户名
        String accessToken = "0"; // 离线模式使用 0 作为访问令牌
        String uuid = UUID.randomUUID().toString().replace("-", ""); // 生成随机 UUID

        // 构建 Minecraft 启动参数
        String[] minecraftArgs = concat(new String[]{
                "--version", "1.8.9",
                "--username", username,
                "--accessToken", accessToken,
                "--uuid", uuid,
                "--userType", "legacy", // 使用 legacy 用户类型
                "--assetsDir", "assets",
                "--assetIndex", "1.8",
                "--gameDir", ".",
                "--userProperties", "{}"
        }, args);

        // 启动 Minecraft
        Main.main(minecraftArgs);
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}




//秋雨.水影
