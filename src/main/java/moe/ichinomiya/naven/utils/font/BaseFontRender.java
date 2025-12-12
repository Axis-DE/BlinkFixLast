package moe.ichinomiya.naven.utils.font;

import java.util.List;

public abstract class BaseFontRender {
    public abstract int drawStringWithShadow(String text, float x, float y, int color);
    public abstract int drawString(String text, float x, float y, int color);
    public abstract int drawString(String text, float x, float y, int color, boolean dropShadow);
    public abstract int getStringWidth(String text);
    public abstract int getFontHeight();
    public abstract int getCharWidth(char character);
    public abstract String trimStringToWidth(String text, int width);
    public abstract String trimStringToWidth(String s4, int i, boolean b);
    public abstract int drawCenteredString(String text, float x, float y, int color);
    public abstract List<String> listFormattedStringToWidth(String str, int wrapWidth);
}
