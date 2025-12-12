package moe.ichinomiya.naven.utils.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static net.minecraft.client.gui.FontRenderer.getFormatFromString;
import static org.lwjgl.opengl.GL11.*;

public class GlyphPageFontRenderer extends BaseFontRender {
    /**
     * Current X coordinate at which to draw the next character.
     */
    private float posX;
    /**
     * Current Y coordinate at which to draw the next character.
     */
    private float posY;
    /**
     * Array of RGB triplets defining the 16 standard chat colors followed by 16 darker version of the same colors for
     * drop shadows.
     */
    private final int[] colorCode = new int[32];
    /**
     * Used to specify new red value for the current color.
     */
    private float red;
    /**
     * Used to specify new blue value for the current color.
     */
    private float blue;
    /**
     * Used to specify new green value for the current color.
     */
    private float green;
    /**
     * Used to speify new alpha value for the current color.
     */
    private float alpha;
    /**
     * Text color of the currently rendering string.
     */
    private int textColor;

    /**
     * Set if the "k" style (random) is active in currently rendering string
     */
    private boolean randomStyle;
    /**
     * Set if the "l" style (bold) is active in currently rendering string
     */
    private boolean boldStyle;
    /**
     * Set if the "o" style (italic) is active in currently rendering string
     */
    private boolean italicStyle;
    /**
     * Set if the "n" style (underlined) is active in currently rendering string
     */
    private boolean underlineStyle;
    /**
     * Set if the "m" style (strikethrough) is active in currently rendering string
     */
    private boolean strikethroughStyle;

    private final GlyphPage regularGlyphPage;
    private final GlyphPage boldGlyphPage;
    private final GlyphPage italicGlyphPage;
    private final GlyphPage boldItalicGlyphPage;

    int offset;

    public GlyphPageFontRenderer(GlyphPage regularGlyphPage, GlyphPage boldGlyphPage, GlyphPage italicGlyphPage, GlyphPage boldItalicGlyphPage, int offset) {
        this.regularGlyphPage = regularGlyphPage;
        this.boldGlyphPage = boldGlyphPage;
        this.italicGlyphPage = italicGlyphPage;
        this.boldItalicGlyphPage = boldItalicGlyphPage;

        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }


            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }

        this.offset = offset;
    }

    public static GlyphPageFontRenderer create(Font font) {
        return create(font, false);
    }

    public static GlyphPageFontRenderer createUseCache(String fileName, int fileSize) {
        GlyphPage regularPage;

        regularPage = new GlyphPage(null, true, true, true, true);

        regularPage.fontName = fileName;
        regularPage.fontSize = fileSize;

        regularPage.generateGlyphPage(null);

        GlyphPage boldPage = regularPage;
        GlyphPage italicPage = regularPage;
        GlyphPage boldItalicPage = regularPage;
        GlyphPageFontRenderer glyphPageFontRenderer = new GlyphPageFontRenderer(regularPage, boldPage, italicPage, boldItalicPage, 0);
        FontManager.fonts.put((fileName + fileSize).toLowerCase(), glyphPageFontRenderer);
        return glyphPageFontRenderer;
    }

    public static GlyphPageFontRenderer create(Font font, boolean allChars) {
        return create(font, allChars, 0);
    }

    public static char[] charsA = new char[65535]; // All chars
    public static char[] ascii_chars = new char[256]; // ASCII chars

    static {
        for (int i = 0; i < charsA.length; i++) {
            charsA[i] = (char) i;
        }

        for (int i = 0; i < ascii_chars.length; i++) {
            ascii_chars[i] = (char) i;
        }
    }

    public static GlyphPageFontRenderer create(Font font, boolean allChars, int offset) {
        char[] chars = allChars ? charsA : ascii_chars;
        GlyphPage regularPage;

        regularPage = new GlyphPage(font, true, true, allChars);
        regularPage.generateGlyphPage(chars);

        GlyphPage boldPage = regularPage;
        GlyphPage italicPage = regularPage;
        GlyphPage boldItalicPage = regularPage;
        GlyphPageFontRenderer glyphPageFontRenderer = new GlyphPageFontRenderer(regularPage, boldPage, italicPage, boldItalicPage, offset);
        FontManager.fonts.put((font.getFamily().replace(" ", "") + font.getSize()).toLowerCase(), glyphPageFontRenderer);
        return glyphPageFontRenderer;
    }

    static HashMap<String, String> controlChar = new HashMap<>();

    public static String removeControlChar(String text) {
        String result;

        try {
            if ((result = controlChar.get(text)) == null) {
                controlChar.put(text, result = (text.replaceAll("\247.", "")));
            }
        } catch (Throwable e) {
            return "";
        }

        return result;
    }

    public int drawCenteredString(String text, float x, float y, int color, boolean dropShadow) {
        return drawString(text, x - (this.getStringWidth(removeControlChar(text)) / 2f), y, color, dropShadow);
    }

    public int drawCenteredString(String text, float x, float y, int color) {
        return drawString(text, x - (this.getStringWidth(removeControlChar(text)) / 2f), y, color, false);
    }

    @Override
    public List<String> listFormattedStringToWidth(String str, int wrapWidth)
    {
        return Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        return wrapFormattedStringToWidth(str, wrapWidth, 0);
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth, int counter)
    {
        if (str.length() <= 1 || counter > 20)
        {
            return str;
        }
        else
        {
            int i = this.sizeStringToWidth(str, wrapWidth);

            if (str.length() <= i)
            {
                return str;
            }
            else
            {
                String s = str.substring(0, i);
                char c0 = str.charAt(i);
                boolean flag = c0 == 32 || c0 == 10;
                String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
                return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth, ++ counter);
            }
        }
    }

    private int sizeStringToWidth(String str, int wrapWidth)
    {
        int i = str.length();
        float f = 0.0F;
        int j = 0;
        int k = -1;

        for (boolean flag = false; j < i; ++j)
        {
            char c0 = str.charAt(j);

            switch (c0)
            {
                case '\n':
                    --j;
                    break;

                case ' ':
                    k = j;

                default:
                    f += (float)this.getCharWidth(c0);

                    if (flag)
                    {
                        ++f;
                    }

                    break;

                case '\u00a7':
                    if (j < i - 1)
                    {
                        ++j;
                        char c1 = str.charAt(j);

                        if (c1 != 108 && c1 != 76)
                        {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (c0 == 10)
            {
                ++j;
                k = j;
                break;
            }

            if (Math.round(f) > wrapWidth)
            {
                break;
            }
        }

        return j != i && k != -1 && k < j ? k : j;
    }

    public int drawCenteredStringWithShadow(String text, float x, float y, int color) {
        return drawString(text, x - (this.getStringWidth(removeControlChar(text)) / 2f), y, color, true);
    }

    public int drawStringWithShadow(String text, float x, float y, int color) {
        return drawString(text, x, y, color, true);
    }

    public int drawString(String text, float x, float y, int color) {
        return drawString(text, x, y, color, false);
    }

    /**
     * Draws the specified string.
     */

    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        GlStateManager.enableAlpha();
        this.resetStyles();
        int i;

        if (dropShadow) {
            i = this.renderString(text, x + 0.5F, y + 0.5F, color, true);
            i = Math.max(i, this.renderString(text, x, y, color, false));
        } else {
            i = this.renderString(text, x, y, color, false);
        }

        return i;
    }

    /**
     * Render single line string by setting GL color, current (posX,posY), and calling renderStringAtPos()
     */
    private int renderString(String text, float x, float y, int color, boolean dropShadow) {
        text = processString(text);

        y += offset;
        if (text == null) {
            return 0;
        } else {

            if ((color & -67108864) == 0) {
                color |= -16777216;
            }

            if (dropShadow) {
                color = (color & 16579836) >> 2 | color & -16777216;
            }

            this.red = (float) (color >> 16 & 255) / 255.0F;
            this.blue = (float) (color >> 8 & 255) / 255.0F;
            this.green = (float) (color & 255) / 255.0F;
            this.alpha = (float) (color >> 24 & 255) / 255.0F;
            GlStateManager.color(this.red, this.blue, this.green, this.alpha);
            this.posX = x * 2.0f;
            this.posY = y * 2.0f;
            this.renderStringAtPos(text, dropShadow);
            return (int) (this.posX / 4.0f);
        }
    }

    /**
     * Render a single line string at the current (posX,posY) and update posX
     */
    private void renderStringAtPos(String text, boolean shadow) {
        GlyphPage glyphPage = getCurrentGlyphPage();

        glPushMatrix();

        glScaled(0.5, 0.5, 0.5);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();

        glyphPage.bindTexture();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        for (int i = 0; i < text.length(); ++i) {
            char c0 = text.charAt(i);

            if (c0 == 167 && i + 1 < text.length()) {
                int i1 = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));

                if (i1 < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    if (i1 < 0) {
                        i1 = 15;
                    }

                    if (shadow) {
                        i1 += 16;
                    }

                    int j1 = this.colorCode[i1];
                    this.textColor = j1;

                    GlStateManager.color((float) (j1 >> 16) / 255.0F, (float) (j1 >> 8 & 255) / 255.0F, (float) (j1 & 255) / 255.0F, this.alpha);
                } else if (i1 == 16) {
                    this.randomStyle = true;
                } else if (i1 == 17) {
                    this.boldStyle = true;
                } else if (i1 == 18) {
                    this.strikethroughStyle = true;
                } else if (i1 == 19) {
                    this.underlineStyle = true;
                } else if (i1 == 20) {
                    this.italicStyle = true;
                } else {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    GlStateManager.color(this.red, this.blue, this.green, this.alpha);
                }

                ++i;
            } else {
                glyphPage = getCurrentGlyphPage();

                glyphPage.bindTexture();

                float f = glyphPage.drawChar(c0, posX, posY);

                doDraw(f, glyphPage);
            }
        }

        glPopMatrix();
    }

    private void doDraw(float f, GlyphPage glyphPage) {
        if (this.strikethroughStyle) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION);
            worldrenderer.pos(this.posX, this.posY + (float) (glyphPage.getMaxFontHeight() / 2), 0.0D).endVertex();
            worldrenderer.pos(this.posX + f, this.posY + (float) (glyphPage.getMaxFontHeight() / 2), 0.0D).endVertex();
            worldrenderer.pos(this.posX + f, this.posY + (float) (glyphPage.getMaxFontHeight() / 2) - 1.0F, 0.0D).endVertex();
            worldrenderer.pos(this.posX, this.posY + (float) (glyphPage.getMaxFontHeight() / 2) - 1.0F, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        if (this.underlineStyle) {
            Tessellator tessellator1 = Tessellator.getInstance();
            WorldRenderer worldrenderer1 = tessellator1.getWorldRenderer();
            GlStateManager.disableTexture2D();
            worldrenderer1.begin(7, DefaultVertexFormats.POSITION);
            int l = this.underlineStyle ? -1 : 0;
            worldrenderer1.pos(this.posX + (float) l, this.posY + (float) glyphPage.getMaxFontHeight(), 0.0D).endVertex();
            worldrenderer1.pos(this.posX + f, this.posY + (float) glyphPage.getMaxFontHeight(), 0.0D).endVertex();
            worldrenderer1.pos(this.posX + f, this.posY + (float) glyphPage.getMaxFontHeight() - 1.0F, 0.0D).endVertex();
            worldrenderer1.pos(this.posX + (float) l, this.posY + (float) glyphPage.getMaxFontHeight() - 1.0F, 0.0D).endVertex();
            tessellator1.draw();
            GlStateManager.enableTexture2D();
        }

        this.posX += f;
    }


    private GlyphPage getCurrentGlyphPage() {
        if (boldStyle && italicStyle)
            return boldItalicGlyphPage;
        else if (boldStyle)
            return boldGlyphPage;
        else if (italicStyle)
            return italicGlyphPage;
        else
            return regularGlyphPage;
    }

    /**
     * Reset all style flag fields in the class to false; called at the start of string rendering
     */
    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    public int getFontHeight() {
        return regularGlyphPage.getMaxFontHeight() / 2 + offset;
    }

    @Override
    public int getCharWidth(char character) {
        return getStringWidth(String.valueOf(character));
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, int width)
    {
        return this.trimStringToWidth(text, width, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String text, int width, boolean reverse)
    {
        text = processString(text);

        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float)width; k += j)
        {
            char c0 = text.charAt(k);
            float f1 = this.getCharWidth(c0);

            if (flag)
            {
                flag = false;

                if (c0 != 108 && c0 != 76)
                {
                    if (c0 == 114 || c0 == 82)
                    {
                        flag1 = false;
                    }
                }
                else
                {
                    flag1 = true;
                }
            }
            else if (f1 < 0.0F)
            {
                flag = true;
            }
            else
            {
                f += f1;

                if (flag1)
                {
                    ++f;
                }
            }

            if (f > (float)width)
            {
                break;
            }

            if (reverse)
            {
                stringbuilder.insert(0, c0);
            }
            else
            {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    private static boolean isFormatColor(char colorChar)
    {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }

    private final HashMap<String, Integer> stringWidthCache = new HashMap<>();

    private static final Minecraft mc = Minecraft.getMinecraft();

    public int getStringWidth(String text) {
        Integer result;

        if ((result = stringWidthCache.get(text)) == null) {
            stringWidthCache.put(text, result = getStringWidthNoCache(text));
        }

        return result;
    }

    public int getStringWidthNoCache(String text) {
        if (text == null) {
            return 0;
        }

        text = removeControlChar(text);

        int width = 0;

        GlyphPage currentPage;

        int size = text.length();

        for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            currentPage = getCurrentGlyphPage();
            width += currentPage.getWidth(character) - 8;
        }

        return width / 2;
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, float width) {
        return this.trimStringToWidth(text, width, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String text, float maxWidth, boolean reverse) {
        text = processString(text);
        StringBuilder stringbuilder = new StringBuilder();

        boolean on = false;

        int j = reverse ? text.length() - 1 : 0;
        int k = reverse ? -1 : 1;
        float width = 0;

        GlyphPage currentPage;

        for (int i = j; i >= 0 && i < text.length() && width < maxWidth; i += k) {
            char character = text.charAt(i);

            if (character == '\247')
                on = true;
            else if (on && character >= '0' && character <= 'r') {
                int colorIndex = "0123456789abcdefklmnor".indexOf(character);
                if (colorIndex < 16) {
                    boldStyle = false;
                    italicStyle = false;
                } else if (colorIndex == 17) {
                    boldStyle = true;
                } else if (colorIndex == 20) {
                    italicStyle = true;
                } else if (colorIndex == 21) {
                    boldStyle = false;
                    italicStyle = false;
                }
                i++;
                on = false;
            } else {
                if (on) i--;

                character = text.charAt(i);
                currentPage = getCurrentGlyphPage();
                width += (currentPage.getWidth(character) - 8) / 2;
            }

            if (width > maxWidth) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, character);
            } else {
                stringbuilder.append(character);
            }
        }

        return stringbuilder.toString();
    }

    private final HashMap<String, String> processedStrings = new HashMap<>();

    private String processString(String text) {
        String result;

        if ((result = processedStrings.get(text)) == null) {
            processedStrings.put(text, result = processStringNoCache(text));
        }

        return result;
    }

    private String processStringNoCache(String text) {
        if (text == null) return "";

        StringBuilder str = new StringBuilder();
        for (char c : text.toCharArray()) {
            if ((c < 50000 || c > 60000) && c != 9917) str.append(c);
        }

        text = str.toString()
                .replace("\247r", "")
                .replace('▬', '=')
                .replace('❤', '♥')
                .replace('⋆', '☆')
                .replace('☠', '☆')
                .replace('✰', '☆')
                .replace("✫", "☆")
                .replace("✙", "+")
                .replace('⬅', '←')
                .replace('⬆', '↑')
                .replace('⬇', '↓')
                .replace('➡', '→')
                .replace('⬈', '↗')
                .replace('⬋', '↙')
                .replace('⬉', '↖')
                .replace('⬊', '↘')
                .replace('✦', '·')
                .replace("========", "=======");
        return text;
    }
}
