package axis.shiyan.wei.bluearchive.blinkfix.protocols.germ;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventPacket;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender2D;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRespawn;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.world.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;

import java.nio.charset.StandardCharsets;

public class GermMod {
    GermMainMenuGUI germModGui = new GermMainMenuGUI();
    String action;
    boolean isGermMod = false;

    @EventTarget
    public void onRespawn(EventRespawn e) {
        action = null;

        if (e.getType() == EventType.JOIN_GAME) {
            isGermMod = false;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D e) {
        if (action != null) {
            ScaledResolution resolution = e.getResolution();
            GlStateManager.pushMatrix();
            // Scale
            GlStateManager.scale(2, 2, 2);
            Minecraft.getMinecraft().fontRendererObj.drawCenteredStringWithShadow(action, resolution.getScaledWidth() / 2f / 2f, resolution.getScaledHeight() / 2f / 2f + 20, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof C01PacketChatMessage) {
            C01PacketChatMessage packet = (C01PacketChatMessage) e.getPacket();

            if (packet.getMessage().equals("/组队")) {
                e.setCancelled(true);
                sendPacket(Packets.openTeamUI);
            }
        }

        if (e.getPacket() instanceof S3FPacketCustomPayload) {
            S3FPacketCustomPayload packet = (S3FPacketCustomPayload) e.getPacket();
            if (packet.getChannelName().contains("germplugin")) {
                if (!isGermMod) {
                    isGermMod = true;
                    sendPacket(Packets.gameLoading);
                }

                ByteBuf payload = packet.getBufferData();

                byte[] data = new byte[payload.readableBytes()];
                payload.readBytes(data);
                payload.writeBytes(data);

                if (GermPacketUtils.isDisplayMainMenu(data)) {
                    Wrapper.runOnMainThread(() -> Minecraft.getMinecraft().displayGuiScreen(germModGui));
                }

                if (GermPacketUtils.isGameDetails(data)) {
                    byte[] bytes = new byte[data.length - 43];
                    System.arraycopy(data, 43, bytes, 0, bytes.length);
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    if (json.startsWith("{")) {
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

                        GermMainMenuGUI.games.clear();
                        int entry = 0;
                        for (JsonElement subs : jsonObject.getAsJsonArray("subs")) {
                            if (subs.isJsonObject()) {
                                JsonObject sub = subs.getAsJsonObject();
                                String displayName = sub.get("name").getAsString().replace("&", "\247").replace(" ", "");
                                String sid = sub.get("sid").getAsString();
                                String desc = sub.get("desc").getAsString().replace("&", "\247");

                                GermMainMenuGUI.games.add(new GermModGame(displayName, sid, desc, entry ++));
                            }
                        }
                    }
                }

                if (GermPacketUtils.isActionBar(data)) {
                    byte[] bytes = new byte[data.length - 20];
                    System.arraycopy(data, 20, bytes, 0, bytes.length);
                    action = new String(bytes, StandardCharsets.UTF_8);
                }

                if (GermPacketUtils.isStopDisplayActionBar(data)) {
                    action = null;
                }

                if (GermPacketUtils.isOpenTeamCreateUI(data)) {
                    sendPacket(Packets.confirmOpenCreateTeamUI);
                    sendPacket(Packets.clickJoinTeamButton);
                    sendPacket(Packets.performJoinTeamAction);
                }

                if (GermPacketUtils.isOpenJoinTeamMenu(data)) {
                    sendPacket(Packets.confirmOpenTeamList);
                }

                if (GermPacketUtils.isTeamListUIData(data)) {
                    byte[] bytes = new byte[data.length - 30];
                    System.arraycopy(data, 30, bytes, 0, bytes.length);
                    String teamList = new String(bytes, StandardCharsets.UTF_8);

                    ChatComponentText components = new ChatComponentText("§a§l==============\n§a§l组队邀请:\n");
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(components);

                    for (String line : teamList.split("\n")) {
                        if (line.contains("clickScript") && line.contains("GuiScreen.post") && line.contains("bt_accept_invite") && line.contains("player_name")) {
                            String name = line.split("':'")[1].split("'")[0];
                            ChatComponentText chatComponents = new ChatComponentText("§a§l加入: \247r" + name);
                            chatComponents.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.JOIN_TEAM, name)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(name))));
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(chatComponents);
                        }
                    }
                }

                if (GermPacketUtils.isOpenTeamMainMenu(data)) {
                    sendPacket(Packets.confirmOpenTeamMainMenu);
                }

                if (GermPacketUtils.isTeamMainMenu(data)) {
                    String teamMain = new String(data, StandardCharsets.UTF_8);
                    String[] lines = teamMain.split("\n");
                    for (String line : lines) {
                        if (line.endsWith("§a的队伍")) {
                            ChatComponentText components = new ChatComponentText("§a§l==============\n§a§l队伍名: \n" + line.split("- ")[1]);
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(components);
                            ChatComponentText components2 = new ChatComponentText("\n§a§l队员: ");
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(components2);
                        } else if (line.startsWith("      entry_")) {
                            String replace = line.replace(" ", "");
                            ChatComponentText components = new ChatComponentText(" - " + replace.substring(6, replace.length() - 1));
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(components);
                        }
                    }

                    ChatComponentText components = new ChatComponentText("\n");

                    ChatComponentText warp = new ChatComponentText("§a§l[集结]");
                    ChatComponentText kick = new ChatComponentText("§a§l[踢人]");
                    ChatComponentText dismiss = new ChatComponentText("§a§l[解散]");
                    ChatComponentText leave = new ChatComponentText("§a§l[退出]");

                    warp.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.WARP_TEAMMATES)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("集结队友"))));
                    kick.setChatStyle((new ChatStyle()).setChatClickEvent(new ClickEvent(ClickEvent.Action.KICK_MEMBERS)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("踢出队友"))));
                    dismiss.setChatStyle((new ChatStyle()).setChatClickEvent(new ClickEvent(ClickEvent.Action.DISMISS_TEAM)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("解散队伍"))));
                    leave.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.LEAVE_TEAM)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("退出队伍"))));

                    components.appendSibling(warp);
                    components.appendText(" ");
                    components.appendSibling(kick);
                    components.appendText(" ");
                    components.appendSibling(dismiss);
                    components.appendText(" ");
                    components.appendSibling(leave);

                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(components);
                }

                if (GermPacketUtils.isOpenKickMemberMenu(data)) {
                    sendPacket(Packets.confirmOpenKickMemberMenu);
                }

                if (GermPacketUtils.isKickMemberMenu(data)) {
                    String kickMember = new String(data, StandardCharsets.UTF_8);
                    String[] lines = kickMember.split("\n");

                    for (String line : lines) {
                        if (line.contains("clickScript") && line.contains("GuiScreen.post") && line.contains("bt_kick") && line.contains("player_name")) {
                            String name = line.split("':'")[1].split("'")[0];
                            ChatComponentText chatcomponenttext = new ChatComponentText("踢出:" + name);
                            chatcomponenttext.setChatStyle((new ChatStyle()).setChatClickEvent(new ClickEvent(ClickEvent.Action.KICK_MEMBERS, name)));
                            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(chatcomponenttext);
                        }
                    }
                }
            }
        }
    }

    public static void sendPacket(byte[] data) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeBytes(data);
        Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("germmod-netease", buffer));
    }
}
