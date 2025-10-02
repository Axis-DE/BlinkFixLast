package axis.shiyan.wei.bluearchive.blinkfix.protocols.germ;

import java.nio.charset.StandardCharsets;

public class GermPacketUtils {
    private static final byte[] displayMainMenu = new byte[]{0, 0, 0, 76, 8, 109, 97, 105, 110, 109, 101, 110, 117};
    private static final byte[] gameDetails = new byte[]{0, 0, 0, 79, 3, 71, 85, 73, 0, 0, 0, 0};
    private static final byte[] joinGame1 = new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47};
    private static final byte[] actionBar = new byte[]{0, 0, 0, 75, 14, 37, 97, 99, 116, 105, 111, 110, 95, 98, 97, 114, 95, 49, 37};
    private static final byte[] stopDisplayActionBar = new byte[]{0, 0, 0, 75, 14, 37, 97, 99, 116, 105, 111, 110, 95, 98, 97, 114, 95, 49, 37, 0};
    private static final byte[] openTeamCreateUI = new byte[]{0, 0, 0, 76, 11, 116, 101, 97, 109, 95, 99, 114, 101, 97, 116, 101};
    private static final byte[] joinTeamMenu = new byte[]{0, 0, 0, 67, 20, 71, 85, 73, 36, 116, 101, 97, 109, 95, 99, 114, 101, 97, 116, 101, 64, 106, 111, 105, 110, 22, 123, 34, 64, 101, 114, 114, 111, 114, 34, 58, 34, 99, 97, 110, 99, 101, 108, 108, 101, 100, 34, 125};
    private static final byte[] teamListUIData = new byte[]{0, 0, 0, 73, 3, 103, 117, 105, 9, 116, 101, 97, 109, 95, 108, 105, 115, 116};
    private static final byte[] teamMainMenu = new byte[]{0, 0, 0, 73, 3, 103, 117, 105, 9, 116, 101, 97, 109, 95, 109, 97, 105, 110};
    private static final byte[] openTeamMainMenu = new byte[]{0, 0, 0, 76, 9, 116, 101, 97, 109, 95, 109, 97, 105, 110};
    private static final byte[] kickMemberMenu = new byte[]{0, 0, 0, 73, 3, 103, 117, 105, 14, 116, 101, 97, 109, 95, 107, 105, 99, 107, 95, 108, 105, 115, 116};
    private static final byte[] openKickMemberMenu = new byte[]{0, 0, 0, 67, 18, 71, 85, 73, 36, 116, 101, 97, 109, 95, 109, 97, 105, 110, 64, 107, 105, 99, 107, 22, 123, 34, 64, 101, 114, 114, 111, 114, 34, 58, 34, 99, 97, 110, 99, 101, 108, 108, 101, 100, 34, 125};

    public static boolean isOpenKickMemberMenu(byte[] bytes) {
        for (int i = 0; i < openKickMemberMenu.length; i++) {
            if (bytes[i] != openKickMemberMenu[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isKickMemberMenu(byte[] bytes) {
        for (int i = 0; i < kickMemberMenu.length; i++) {
            if (bytes[i] != kickMemberMenu[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOpenTeamMainMenu(byte[] bytes) {
        for (int i = 0; i < openTeamMainMenu.length; i++) {
            if (bytes[i] != openTeamMainMenu[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTeamMainMenu(byte[] bytes) {
        for (int i = 0; i < teamMainMenu.length; i++) {
            if (bytes[i] != teamMainMenu[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTeamListUIData(byte[] bytes) {
        for (int i = 0; i < teamListUIData.length; i++) {
            if (bytes[i] != teamListUIData[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOpenTeamCreateUI(byte[] bytes) {
        for (int i = 0; i < openTeamCreateUI.length; i++) {
            if (bytes[i] != openTeamCreateUI[i]) {
                return false;
            }
        }

        return true;
    }
    public static boolean isOpenJoinTeamMenu(byte[] bytes) {
        for (int i = 0; i < joinTeamMenu.length; i++) {
            if (bytes[i] != joinTeamMenu[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean isDisplayMainMenu(byte[] bytes) {
        for (int i = 0; i < displayMainMenu.length; i++) {
            if (bytes[i] != displayMainMenu[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean isGameDetails(byte[] bytes) {
        if (bytes.length < 100) {
            return false;
        }

        for (int i = 0; i < gameDetails.length; i++) {
            if (bytes[i] != gameDetails[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean isActionBar(byte[] bytes) {
        for (int i = 0; i < actionBar.length; i++) {
            if (bytes[i] != actionBar[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean isStopDisplayActionBar(byte[] bytes) {
        for (int i = 0; i < stopDisplayActionBar.length; i++) {
            if (bytes[i] != stopDisplayActionBar[i]) {
                return false;
            }
        }

        return true;
    }

    public static byte[] buildKickMemberPacket(String playerName) {
        byte[] payload = ("{\"player_name\":\"" + playerName + "\"}").getBytes(StandardCharsets.UTF_8);
        byte[] data = {0, 0, 0, 26, 26, 71, 85, 73, 36, 116, 101, 97, 109, 95, 107, 105, 99, 107, 95, 108, 105, 115, 116, 64, 98, 116, 95, 107, 105, 99, 107};

        byte[] bytes = new byte[data.length + payload.length + 1];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[data.length] = (byte) payload.length;
        System.arraycopy(payload, 0, bytes, data.length + 1, payload.length);

        return bytes;
    }

    public static byte[] buildJoinTeamPacket(String playerName) {
        byte[] payload = ("{\"player_name\":\"" + playerName + "\"}").getBytes(StandardCharsets.UTF_8);
        byte[] data = {0, 0, 0, 26, 30, 71, 85, 73, 36, 116, 101, 97, 109, 95, 108, 105, 115, 116, 64, 98, 116, 95, 97, 99, 99, 101, 112, 116, 95, 105, 110, 118, 105, 116, 101};

        byte[] bytes = new byte[data.length + payload.length + 1];
        System.arraycopy(data, 0, bytes, 0, data.length);
        bytes[data.length] = (byte) payload.length;
        System.arraycopy(payload, 0, bytes, data.length + 1, payload.length);

        return bytes;
    }

    public static byte[] buildJoinGamePacket(int entry, String sid) {
        sid = "{\"entry\":" + entry + ",\"sid\":\"" + sid + "\"}";

        byte[] bytes = new byte[joinGame1.length + sid.getBytes().length + 2];
        System.arraycopy(joinGame1, 0, bytes, 0, joinGame1.length);

        bytes[joinGame1.length] = (byte) (48 + entry);
        bytes[joinGame1.length + 1] = (byte) sid.length();

        System.arraycopy(sid.getBytes(), 0, bytes, joinGame1.length + 2, sid.getBytes().length);

        return bytes;
    }
}
