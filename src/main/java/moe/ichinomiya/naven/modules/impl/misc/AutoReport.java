package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import moe.ichinomiya.naven.utils.ServerUtils;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S30PacketWindowItems;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@ModuleInfo(name = "AutoReport", description = "Automatically reports players", category = Category.MISC)
public class AutoReport extends Module {
    private final BooleanValue sendChat = ValueBuilder.create(this, "Send Chat").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue reportLegit = ValueBuilder.create(this, "Report Legit Players").setDefaultBooleanValue(false).build().getBooleanValue();
    private final List<String> reported = new LinkedList<>();
    private final TimeHelper reportDelay = new TimeHelper();
    private int windowId = -1;
    private String playerName;

    @EventTarget
    public void onRespawn(EventRespawn e) {
        if (e.getType() == EventType.JOIN_GAME) {
            reported.clear();
            windowId = -1;
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && ServerUtils.serverType == ServerUtils.ServerType.GERM_PLUGIN) {
            Optional<String> cheater = mc.getNetHandler().getPlayerInfoMap().stream()
                    .filter(info -> HackerDetector.isCheating(info.getGameProfile().getName()) || reportLegit.getCurrentValue())
                    .filter(info -> !reported.contains(info.getGameProfile().getName()))
                    .filter(info -> !info.getGameProfile().getName().equals(mc.thePlayer.getName()))
                    .findAny()
                    .map(info -> info.getGameProfile().getName());

            if (reportDelay.delay(15000) && cheater.isPresent()) {
                mc.thePlayer.sendChatMessage("/report " + cheater.get());
                reportDelay.reset();
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getType() == EventType.RECEIVE) {
            if (e.getPacket() instanceof S2DPacketOpenWindow) {
                S2DPacketOpenWindow packet = (S2DPacketOpenWindow) e.getPacket();
                if (packet.getWindowTitle().getUnformattedText().contains("请选择举报理由")) {
                    windowId = packet.getWindowId();
                    e.setCancelled(true);
                }
            } else if (e.getPacket() instanceof S30PacketWindowItems) {
                S30PacketWindowItems packet = (S30PacketWindowItems) e.getPacket();

                if (packet.getWindowId() == windowId) {
                    for (int slot = 0; slot < packet.getItemStacks().length; slot ++) {
                        ItemStack stack = packet.getItemStacks()[slot];
                        if (stack != null) {
                            if (stack.getDisplayName().contains("杀戮光环")) {
                                mc.getNetHandler().getNetworkManager().sendPacket(new C0EPacketClickWindow(windowId, slot, 0, 0, stack, (short) 0));
                            }
                        }
                    }
                    e.setCancelled(true);
                }
            } else if (e.getPacket() instanceof S2EPacketCloseWindow) {
                S2EPacketCloseWindow packet = (S2EPacketCloseWindow) e.getPacket();

                if (packet.getWindowId() == windowId) {
                    windowId = -1;
                    e.setCancelled(true);
                }
            } else if (e.getPacket() instanceof S02PacketChat) {
                S02PacketChat packet = (S02PacketChat) e.getPacket();

                String chat = packet.getChatComponent().getFormattedText();
                if (chat.startsWith("§r§2我们已经收到你对玩家") && chat.endsWith("的举报，谢谢！§r")) {
                    e.setCancelled(true);
                    playerName = chat.substring(14, chat.length() - 9);
                } else if (chat.startsWith("§r§4举报成功！举报编号") && chat.endsWith("§r") && playerName != null) {
                    e.setCancelled(true);
                    String code = chat.substring(13, chat.length() - 2);
                    BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.SUCCESS, "Reported " + playerName + " successfully, Code: " + code + "!", 5000));

                    reported.add(playerName);
                    if (sendChat.getCurrentValue()) {
                        mc.thePlayer.sendChatMessage("已举报" + playerName + "，举报编号: " + code + "!");
                    }

                    playerName = null;
                }
            }
        }
    }
}
