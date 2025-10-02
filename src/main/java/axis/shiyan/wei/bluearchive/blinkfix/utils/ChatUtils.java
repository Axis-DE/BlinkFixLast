package axis.shiyan.wei.bluearchive.blinkfix.utils;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import net.minecraft.util.ChatComponentText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils implements Utils {
    private final static String PREFIX = "\2477[\2474" + "BlinkFix" + "\2477] ";
//    BlinkFix.CLIENT_DISPLAY_NAME.charAt(0)

    public static void addChatMessage(boolean prefix, String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText((prefix ? PREFIX : "") + message));
        }
    }

    public static void addChatMessage(String message) {
        addChatMessage(true, message);
    }

    public static boolean isSpammer(String message) {
        if (getFullCharCounts(message) > 10) {
            return true;
        }

        message = convertFullWidthToHalfWidth(message)
                .replace(" ", "")
                .replace("?", "")
                .replace(",", "")
                .replace("-", "")
                .replace(".", "")
                .replace("'", "")
                .replace("!", "")
                .toLowerCase();

        if (message.contains("heshuyouxyz")) {
            return true;
        }

        if (message.contains("silencefix4")) {
            return true;
        }

        if (message.contains("silencefix3")) {
            return true;
        }

        if (message.contains("205521532")) {
            return true;
        }

        if (message.contains("bestconfigfree")) {
            return true;
        }

        if (message.contains("免费哦点击")) {
            return true;
        }

        if (message.contains("一破") && message.contains("卧龙出山")) {
            return true;
        }

        if (message.contains("双连") && message.contains("一战成名")) {
            return true;
        }

        if (message.contains("三连") && message.contains("举世皆惊")) {
            return true;
        }

        if (message.contains("你已被southside客户端击毙")) {
            return true;
        }

        if (message.contains("mftzshop")) {
            return true;
        }

        return false;
    }

    public static String[] processMessage(String message) {
        String regex = "\\((\\d{1,4})\\) <([^>]+)> ([\\s\\S]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        if (matcher.matches()) {
            return new String[] {matcher.group(2), matcher.group(3)};
        } else {
            return null;
        }
    }

    private static int getFullCharCounts(String input) {
        int count = 0;
        for (char c : input.toCharArray()) {
            if (c >= 65296 && c <= 65305) {
                count++;
            } else if (c >= 65313 && c <= 65338) {
                count++;
            } else if (c >= 65345 && c <= 65370) {
                count++;
            }
        }
        return count;
    }

    public static String convertFullWidthToHalfWidth(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 65281 && c <= 65374) { // Check if the character is in the full-width ASCII range
                sb.append((char) (c - 65248)); // Convert full-width character to half-width by subtracting 65248
            } else if (c == 'ˌ') { // Check for the special character
                sb.append('.');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
