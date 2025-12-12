package moe.ichinomiya.naven.utils.font;

import net.minecraft.client.Minecraft;
import net.optifine.util.FontUtils;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class FontManager {
    public static HashMap<String, BaseFontRender> fonts = new HashMap<>();

    public GlyphPageFontRenderer opensans10 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 10), false);
    public GlyphPageFontRenderer opensans11 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 11), false);
    public GlyphPageFontRenderer opensans12 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 12), false);
    public GlyphPageFontRenderer opensans13 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 13), false);
    public GlyphPageFontRenderer opensans14 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 14), false);
    public GlyphPageFontRenderer opensans15 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 15), false);
    public GlyphPageFontRenderer opensans16 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 16), false);
    public GlyphPageFontRenderer opensans17 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 17), false);
    public GlyphPageFontRenderer opensans18 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 18), false);
    public GlyphPageFontRenderer opensans20 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 20), false);
    public GlyphPageFontRenderer opensans23 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 23), false);
    public GlyphPageFontRenderer opensans25 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 25), false);
    public GlyphPageFontRenderer opensans28 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 28), false);
    public GlyphPageFontRenderer opensans30 = GlyphPageFontRenderer.create(this.getFont("opensans.ttf", 30), false);

    public GlyphPageFontRenderer regular10 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 10), false);
    public GlyphPageFontRenderer regular11 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 11), false);
    public GlyphPageFontRenderer regular12 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 12), false);
    public GlyphPageFontRenderer regular13 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 13), false);
    public GlyphPageFontRenderer regular14 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 14), false);
    public GlyphPageFontRenderer regular15 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 15), false);
    public GlyphPageFontRenderer regular16 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 16), false);
    public GlyphPageFontRenderer regular17 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 17), false);
    public GlyphPageFontRenderer regular18 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 18), false);
    public GlyphPageFontRenderer regular20 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 20), false);
    public GlyphPageFontRenderer regular23 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 23), false);
    public GlyphPageFontRenderer regular25 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 25), false);
    public GlyphPageFontRenderer regular28 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 28), false);
    public GlyphPageFontRenderer regular30 = GlyphPageFontRenderer.create(this.getFont("regular.ttf", 30), false);

    public GlyphPageFontRenderer comfortaa16 = GlyphPageFontRenderer.create(this.getFont("comfortaa.ttf", 16), false, 1);
    public GlyphPageFontRenderer comfortaa18 = GlyphPageFontRenderer.create(this.getFont("comfortaa.ttf", 18), false, 2);
    public GlyphPageFontRenderer comfortaa20 = GlyphPageFontRenderer.create(this.getFont("comfortaa.ttf", 20), false, 2);
    public GlyphPageFontRenderer comfortaa35 = GlyphPageFontRenderer.create(this.getFont("comfortaa.ttf", 35), false, 2);

    public GlyphPageFontRenderer poppinsregular10 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 10), false);
    public GlyphPageFontRenderer poppinsregular11 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 11), false);
    public GlyphPageFontRenderer poppinsregular12 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 12), false);
    public GlyphPageFontRenderer poppinsregular13 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 13), false);
    public GlyphPageFontRenderer poppinsregular14 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 14), false);
    public GlyphPageFontRenderer poppinsregular15 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 15), false);
    public GlyphPageFontRenderer poppinsregular16 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 16), false);
    public GlyphPageFontRenderer poppinsregular17 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 17), false);
    public GlyphPageFontRenderer poppinsregular18 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 18), false);
    public GlyphPageFontRenderer poppinsregular20 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 20), false);
    public GlyphPageFontRenderer poppinsregular23 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 23), false);
    public GlyphPageFontRenderer poppinsregular25 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 25), false);
    public GlyphPageFontRenderer poppinsregular28 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 28), false);
    public GlyphPageFontRenderer poppinsregular30 = GlyphPageFontRenderer.create(this.getFont("poppinsregular.ttf", 30), false);


    public GlyphPageFontRenderer siyuan13 = GlyphPageFontRenderer.createUseCache("siyuan", 13);
    public GlyphPageFontRenderer siyuan14 = GlyphPageFontRenderer.createUseCache("siyuan", 14);
    public GlyphPageFontRenderer siyuan16 = GlyphPageFontRenderer.createUseCache("siyuan", 16);
    public GlyphPageFontRenderer siyuan18 = GlyphPageFontRenderer.createUseCache("siyuan", 18);

    public GlyphPageFontRenderer icons10 = GlyphPageFontRenderer.create(this.getFont("icons.ttf", 10), false);
    public GlyphPageFontRenderer icons12 = GlyphPageFontRenderer.create(this.getFont("icons.ttf", 12), false);
    public GlyphPageFontRenderer icons14 = GlyphPageFontRenderer.create(this.getFont("icons.ttf", 14), false);
    public GlyphPageFontRenderer icons16 = GlyphPageFontRenderer.create(this.getFont("icons.ttf", 16), false);
    public GlyphPageFontRenderer icons18 = GlyphPageFontRenderer.create(this.getFont("icons.ttf", 18), false);
    public GlyphPageFontRenderer icons20 = GlyphPageFontRenderer.create(this.getFont("icons.ttf", 20), false);

    public FontManager() {
        fonts.put("minecraft", Minecraft.getMinecraft().fontRendererObj);
    }

    public BaseFontRender getFont(String name) {
        return fonts.get(name.toLowerCase(Locale.ROOT));
    }

    public Font getFont(String fontName, float size) {
        return this.getFont(fontName, Font.PLAIN, size);
    }

    public Font getFont(String fontName, int type, float size) {
        Font font;

        try {
            InputStream is = FontUtils.class.getResourceAsStream("/assets/minecraft/client/fonts/" + fontName);
            font = Font.createFont(type, Objects.requireNonNull(is));
            font = font.deriveFont(type, size);
        } catch (Exception ex) {
            System.out.println("Error while loading font " + fontName + " - " + size + "!");
            font = new Font("Arial", type, 0).deriveFont(size);
        }

        return font;
    }
}
