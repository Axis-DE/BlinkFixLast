package axis.shiyan.wei.bluearchive.blinkfix.utils;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Log4j2
public class Base64ImageLocation {
    private static int seed = 0;
    private ResourceLocation resourceLocation;
    private final boolean isSkin;
    @Getter
    private final String base64;

    public Base64ImageLocation(String base64, boolean isSkin) {
        this.base64 = base64;
        this.isSkin = isSkin;
    }

    public ResourceLocation getResourceLocation() {
        if (resourceLocation == null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));

                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                if (isSkin && width == 64 && height == 32) {
                    long l = System.currentTimeMillis();
                    log.info("Old skin detected! Covering...");
                    bufferedImage = SkinUtils.coverImage(bufferedImage, bufferedImage);
                    log.info("Done! Using time: " + (System.currentTimeMillis() - l) + "ms");
                }

                resourceLocation = new ResourceLocation(BlinkFix.CLIENT_NAME + (seed ++));

                Minecraft.getMinecraft().getTextureManager().loadTexture(resourceLocation, new DynamicTexture(bufferedImage));
            } catch (IOException e) {
                log.error("Failed to load image", e);
            }
        }
        return resourceLocation;
    }
}