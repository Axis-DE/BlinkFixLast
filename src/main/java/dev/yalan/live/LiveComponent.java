package dev.yalan.live;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.yalan.live.events.EventLiveChannelActive;
import dev.yalan.live.events.EventLiveChannelInactive;
import dev.yalan.live.events.EventLiveGenericMessage;
import dev.yalan.live.netty.LiveProto;
import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventTick;
import axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.NetEaseAltManager.GuiAltLogin;
import axis.shiyan.wei.bluearchive.blinkfix.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("StringBufferReplaceableByString")
public class LiveComponent {
    private final JsonParser parser = new JsonParser();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final LiveClient live;

    private int worldTicks = 0;

    LiveComponent(LiveClient live) {
        this.live = live;

        BlinkFix.getInstance().getEventManager().register(this);
    }

    @EventTarget
    public void onLiveGenericMessage(EventLiveGenericMessage e) {
        switch (e.getChannel()) {
            case "GetNeteaseCookie" -> {
                if (mc.currentScreen instanceof GuiAltLogin al) {
                    if (al.getCookieButton != null && al.username != null) {
                        al.getCookieButton.enabled = true;
                        al.status = EnumChatFormatting.RED + e.getMessage();
                    }
                }
            }
            case "SendOperation" -> {
                if (mc.theWorld != null) {
                    ChatUtils.addChatMessage(e.getMessage());
                }
            }
        }
    }

    @EventTarget
    public void onLiveChannelActive(EventLiveChannelActive e) {
        if (mc.theWorld != null) {
            ChatUtils.addChatMessage(EnumChatFormatting.GREEN + "Reconnect to LiveIRCServer successfully!");
        }
    }

    @EventTarget
    public void onLiveChannelInactive(EventLiveChannelInactive e) {
        if (mc.theWorld != null) {
            ChatUtils.addChatMessage(EnumChatFormatting.RED + "LiveIRCServer disconnected, trying to reconnect...");
        }
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (e.getType() == EventType.POST) {
            return;
        }
        
        if (mc.getNetHandler() != null && worldTicks % 200 == 199) {
            for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                live.sendPacket(LiveProto.createQueryMinecraftProfile(playerInfo.getGameProfile()));
            }
        }

        worldTicks++;
    }

    public void onWorldLoad() {
        worldTicks = 0;
    }

    public void handleQueryResultMinecraftProfile(UUID mcUUID, String clientId, UUID userId, String userPayloadString) {
        if (mc.theWorld == null) {
            return;
        }

        final JsonObject payload = parser.parse(userPayloadString).getAsJsonObject();
        LiveUser liveUser = null;

        if (mc.getNetHandler() != null) {
            final NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mcUUID);

            if (playerInfo != null) {
                playerInfo.liveUser = liveUser = new LiveUser(clientId, userId, payload);
            }
        }

        final EntityPlayer player = mc.theWorld.getPlayerEntityByUUID(mcUUID);

        if (player != null) {
            player.liveUser = liveUser == null ? new LiveUser(clientId, userId, payload) : liveUser;
        }
    }

    public void handleChat(String channel, String payloadString) {
        final JsonObject payload = parser.parse(payloadString).getAsJsonObject();

        switch (channel) {
            case "LivePublic": {
                final String clientId = payload.get("clientId").getAsString();
                final String message = payload.get("message").getAsString();

                if ("Naven".equals(clientId)) {
                    printPublicChat(clientId, message, payload.get("username").getAsString(), payload.get("rank").getAsString());
                } else {
                    printPublicChat(clientId, message, payload.get("username").getAsString(), Optional.ofNullable(payload.get("rank")).map(JsonElement::getAsString).orElse(null));
                }

                break;
            }
            case "ServerLog": {
                final String message = payload.get("message").getAsString();

                printSimpleChat(EnumChatFormatting.AQUA + "Server", message);
                break;
            }
            case "Broadcast": {
                final String message = payload.get("message").getAsString();

                printSimpleChat(EnumChatFormatting.GOLD + "Broadcast", message);
                break;
            }
        }
    }

    private void printPublicChat(String clientId, String message, String username, String rank) {
        final StringBuilder builder = new StringBuilder();

        builder.append(EnumChatFormatting.GRAY).append("[");
        builder.append(EnumChatFormatting.YELLOW).append("Live-IRC");
        builder.append(EnumChatFormatting.GRAY).append("-");
        builder.append(EnumChatFormatting.RED).append(clientId);
        builder.append(EnumChatFormatting.GRAY).append("] ");
        builder.append(EnumChatFormatting.RESET).append(username);

        if (rank != null) {
            builder.append(EnumChatFormatting.GRAY).append("(");
            builder.append(EnumChatFormatting.RESET).append(rank).append(EnumChatFormatting.RESET);
            builder.append(EnumChatFormatting.GRAY).append(")");
        }

        builder.append(EnumChatFormatting.GRAY).append(": ");
        builder.append(EnumChatFormatting.RESET).append(message);

        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(builder.toString()));
    }

    private void printSimpleChat(String sender, String message) {
        final StringBuilder builder = new StringBuilder();

        builder.append(EnumChatFormatting.GRAY).append("[");
        builder.append(EnumChatFormatting.YELLOW).append("Live-IRC");
        builder.append(EnumChatFormatting.GRAY).append("-");
        builder.append(EnumChatFormatting.RESET).append(sender);
        builder.append(EnumChatFormatting.GRAY).append("]: ");
        builder.append(EnumChatFormatting.RESET).append(message);

        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(builder.toString()));
    }

    public static String getLiveUserDisplayName(LiveUser liveUser) {
        final StringBuilder builder = new StringBuilder();

        builder.append(EnumChatFormatting.GRAY).append("[");
        builder.append(EnumChatFormatting.YELLOW).append("Live-IRC");
        builder.append(EnumChatFormatting.GRAY).append("-");
        builder.append(EnumChatFormatting.RED).append(liveUser.getClientId());
        builder.append(EnumChatFormatting.GRAY).append("-");
        builder.append(EnumChatFormatting.RESET).append(liveUser.getName());

        final String rank = liveUser.getRank();

        if (rank != null) {
            builder.append(EnumChatFormatting.GRAY).append("(");
            builder.append(EnumChatFormatting.RESET).append(rank).append(EnumChatFormatting.RESET);
            builder.append(EnumChatFormatting.GRAY).append(")");
        }

        builder.append(EnumChatFormatting.GRAY).append("]");

        return builder.toString();
    }
}
