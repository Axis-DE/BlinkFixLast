package axis.shiyan.wei.bluearchive.blinkfix.utils;

import lombok.extern.log4j.Log4j2;
import net.minecraft.potion.Potion;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Log4j2
public class PotionResolver {
    private static final Map<Integer, List<Potion>> colorMap = new HashMap<>();

    static {
        InputStream stream = PotionResolver.class.getResourceAsStream("/assets/minecraft/client/potion_effects.dat");
        if (stream != null) {
            try {
                GZIPInputStream gzipInputStream = new GZIPInputStream(stream);
                for (String s : IOUtils.readLines(gzipInputStream)) {
                    String[] split = s.split(":");

                    if (split.length == 2) {
                        int color = Integer.parseInt(split[0]);
                        String data = split[1];
                        String[] potionIds = data.split("\\+");
                        List<Potion> potions = Arrays.stream(potionIds).map(Integer::parseInt).map(id -> Potion.potionTypes[id]).collect(Collectors.toList());
                        colorMap.put(color, potions);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to load potion effects", e);
            }
        }
    }

    public static List<Potion> resolve(int color) {
        if (colorMap.containsKey(color)) {
            return colorMap.get(color);
        } else if (colorMap.containsKey(color + 1)) {
            return colorMap.get(color + 1);
        } else if (colorMap.containsKey(color - 1)) {
            return colorMap.get(color - 1);
        }

        return Collections.emptyList();
    }
}
