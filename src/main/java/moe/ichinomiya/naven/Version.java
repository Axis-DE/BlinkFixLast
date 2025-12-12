package moe.ichinomiya.naven;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;

public final class Version {
    private static final String CLIENT_TYPE;
    private static final String BUILD_DATE;

    static {
        boolean isJar = isRunningFromJar();
        String buildDate;

        if (isJar) {
            buildDate = getJarBuildDate();
            CLIENT_TYPE = "Release";
        } else {
            buildDate = getCurrentDateTime();
            CLIENT_TYPE = "Development";
        }

        BUILD_DATE = buildDate;
    }

    public static String getVersion() {
        return CLIENT_TYPE;
    }

    public static String getClientVersion() {
        if ("Release".equals(CLIENT_TYPE)) {
            return BUILD_DATE;
        } else {
            return CLIENT_TYPE + " Build ";
        }
    }

    static boolean isRunningFromJar() {
        try {
            String className = Version.class.getName().replace('.', '/') + ".class";
            java.net.URL resource = Version.class.getClassLoader().getResource(className);
            if (resource != null) {
                String protocol = resource.getProtocol();
                return "jar".equals(protocol) || "war".equals(protocol) || "wsjar".equals(protocol);
            }
        } catch (Exception e) {}
        return false;
    }

    static String getJarBuildDate() {
        try {
            java.net.URL jarUrl = Version.class.getProtectionDomain().getCodeSource().getLocation();
            Path jarPath = Paths.get(jarUrl.toURI());
            FileTime lastModifiedTime = Files.getLastModifiedTime(jarPath);
            Instant instant = lastModifiedTime.toInstant();
            LocalDateTime buildDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
            return buildDateTime.format(DateTimeFormatter.ofPattern("yyMMdd-HHmm"));
        } catch (Exception e) {
            return getCurrentDateTime();
        }
    }

    private static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd-HHmm"));
    }
}
