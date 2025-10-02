package axis.shiyan.wei.bluearchive.blinkfix.utils.font;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

public class GlyphPage {
    private int imgSize;
    private int maxFontHeight = -1;
    private final Font font;
    private final boolean antiAliasing;
    private final boolean fractionalMetrics;
    private final HashMap<Character, Glyph> glyphCharacterMap = new HashMap<>();
    private final boolean allChars;
    private final boolean useCache;
    public int texID;

    public String fontName = "";
    public int fontSize = 0;

    public GlyphPage(Font font, boolean antiAliasing, boolean fractionalMetrics, boolean allChars) {
        this(font, antiAliasing, fractionalMetrics, allChars, false);
    }

    public GlyphPage(Font font, boolean antiAliasing, boolean fractionalMetrics, boolean allChars, boolean useCache) {
        this.font = font;
        this.antiAliasing = antiAliasing;
        this.fractionalMetrics = fractionalMetrics;
        this.allChars = allChars;
        this.useCache = useCache;
    }

    public void generateGlyphPage(char[] chars) {
        if (useCache) {
            DataInputStream in = null;
            try {
                BufferedImage img = ImageIO.read(Objects.requireNonNull(FontManager.class.getResourceAsStream("/assets/minecraft/client/fonts/glyphs/" + fontName + fontSize + ".png")));
                this.imgSize = img.getWidth();
                in = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(FontManager.class.getResourceAsStream("/assets/minecraft/client/fonts/glyphs/" + fontName + fontSize + "_glyph.bin"))));

                // Store the first character data
                int x = in.readShort();
                int y = in.readShort();
                int w = in.readShort();
                int h = in.readShort();
                Glyph g = new Glyph(x, y, w, h);
                glyphCharacterMap.put((char) 0, g);

                // Store the rest
                for(int i = 1; i < 65535; ++i) {
                    x = in.readShort();
                    y = in.readShort();
                    w = in.readShort();
                    g = new Glyph(x, y, w, h);

                    if (g.height > maxFontHeight) maxFontHeight = g.height;

                    glyphCharacterMap.put((char) i, g);
                }

                try {
                    this.texID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), img, true, !allChars);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch(Exception ignored) {}
                }
            }

            return;
        }

        // Calculate glyphPageSize
        double maxWidth = -1;
        double maxHeight = -1;

        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, antiAliasing, fractionalMetrics);

        if (allChars) {
            imgSize = 8192;
        } else {
            for (char ch : chars) {
                Rectangle2D bounds = font.getStringBounds(Character.toString(ch), fontRenderContext);
                if (maxWidth < bounds.getWidth()) maxWidth = bounds.getWidth();
                if (maxHeight < bounds.getHeight()) maxHeight = bounds.getHeight();
            }

            // Leave some additional space
            maxWidth += 2;
            maxHeight += 2;

            imgSize = (int) Math.ceil(Math.max(
                    Math.ceil(Math.sqrt(maxWidth * maxWidth * chars.length) / maxWidth),
                    Math.ceil(Math.sqrt(maxHeight * maxHeight * chars.length) / maxHeight))
                    * Math.max(maxWidth, maxHeight)) + 1;
        }

        BufferedImage bufferedImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();

        g.setFont(font);
        // Set Color to Transparent
        g.setColor(new Color(255, 255, 255, 0));
        // Set the image background to transparent
        g.fillRect(0, 0, imgSize, imgSize);

        g.setColor(Color.white);

        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        FontMetrics fontMetrics = g.getFontMetrics();

        int currentCharHeight = 0;
        int posX = 0;
        int posY = 1;

        for (char ch : chars) {
            Glyph glyph = new Glyph();

            Rectangle2D bounds = fontMetrics.getStringBounds(Character.toString(ch), g);

            glyph.width = bounds.getBounds().width + 8; // Leave some additional space
            glyph.height = bounds.getBounds().height;

            if (posX + glyph.width >= imgSize) {
                posX = 0;
                posY += currentCharHeight;
                currentCharHeight = 0;
            }

            glyph.x = posX;
            glyph.y = posY;

            if (glyph.height > maxFontHeight) maxFontHeight = glyph.height;
            if (glyph.height > currentCharHeight) currentCharHeight = glyph.height;

            g.drawString(Character.toString(ch), posX + 2, posY + fontMetrics.getAscent());

            posX += glyph.width;

            glyphCharacterMap.put(ch, glyph);
        }

        try {
            this.texID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), bufferedImage, true, !allChars);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void bindTexture() {
        GlStateManager.bindTexture(this.texID);
    }

    public void unbindTexture() {
        GlStateManager.bindTexture(0);
    }

    public float drawChar(char ch, float x, float y) {
        Glyph glyph = glyphCharacterMap.get(ch);
        if (glyph == null) glyph = glyphCharacterMap.get('?');
        if (glyph == null) throw new IllegalArgumentException("'" + ch + "' wasn't found");

        float pageX = glyph.x / (float) imgSize;
        float pageY = glyph.y / (float) imgSize;

        float pageWidth = glyph.width / (float) imgSize;
        float pageHeight = glyph.height / (float) imgSize;

        float width = glyph.width;
        float height = glyph.height;

        glBegin(GL_TRIANGLES);

        glTexCoord2f(pageX + pageWidth, pageY);
        glVertex2f(x + width, y);

        glTexCoord2f(pageX, pageY);
        glVertex2f(x, y);

        glTexCoord2f(pageX, pageY + pageHeight);
        glVertex2f(x, y + height);

        glTexCoord2f(pageX, pageY + pageHeight);
        glVertex2f(x, y + height);

        glTexCoord2f(pageX + pageWidth, pageY + pageHeight);
        glVertex2f(x + width, y + height);

        glTexCoord2f(pageX + pageWidth, pageY);
        glVertex2f(x + width, y);


        glEnd();

        return width - 8;
    }

    public float getWidth(char ch) {
        return glyphCharacterMap.get(ch) == null ? glyphCharacterMap.get('A').width : glyphCharacterMap.get(ch).width;
    }

    public int getMaxFontHeight() {
        return maxFontHeight;
    }

    public boolean isAntiAliasingEnabled() {
        return antiAliasing;
    }

    public boolean isFractionalMetricsEnabled() {
        return fractionalMetrics;
    }

    static class Glyph {
        private int x;
        private int y;
        private int width;
        private int height;

        Glyph(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        Glyph() {
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
