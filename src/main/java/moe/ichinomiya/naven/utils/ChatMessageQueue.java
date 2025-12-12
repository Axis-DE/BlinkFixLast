package moe.ichinomiya.naven.utils;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.events.impl.EventRunTicks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.util.LinkedList;

public class ChatMessageQueue {
    private final static LinkedList<String> messages = new LinkedList<>();
    private final static TimeHelper timer = new TimeHelper();
    private String lastMessage;

    @EventTarget(Priority.LOWEST)
    public void onPacket(EventPacket e) {
        if (!e.isCancelled() && e.getType() == EventType.SEND && e.getPacket() instanceof C01PacketChatMessage && ServerUtils.serverType == ServerUtils.ServerType.GERM_PLUGIN) {
            C01PacketChatMessage packet = (C01PacketChatMessage) e.getPacket();
            String message = packet.getMessage();

            if (!message.startsWith("/")) {
                messages.addFirst(message);
                e.setCancelled(true);
            }
        }
    }

    public static void addMessage(String message) {
        messages.addLast(message);
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        messages.clear();
    }

    @EventTarget
    public void onTick(EventRunTicks e) {
        if (timer.delay(4000)) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

            if (!messages.isEmpty() && player != null) {
                String message = messages.poll();
                if (message.equals(lastMessage)) {
                    messages.offer(message);
                } else {
                    lastMessage = message;
                    Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacketNoEvent(new C01PacketChatMessage(message));
                    timer.reset();
                }
            }
        }
    }
}
