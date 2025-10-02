package axis.shiyan.wei.bluearchive.blinkfix.utils;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventPacket;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventWorldUnload;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class BlinkUtils implements Utils {

    public static final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    public static boolean blinking, dispatch;
    public static ArrayList<Class<?>> exemptedPackets = new ArrayList<>();
    public static TimeHelper exemptionWatch = new TimeHelper();

    public static void setExempt(Class<?>... packets) {
        exemptedPackets = new ArrayList<>(Arrays.asList(packets));
        exemptionWatch.reset();
    }

    @EventTarget
    public void onPacket(EventPacket event){
        if (mc.thePlayer == null) {
            packets.clear();
            exemptedPackets.clear();
            return;
        }

        if(event.getType() == EventType.SEND) {
            if (mc.thePlayer.isDead || mc.isSingleplayer() || !mc.getNetHandler().doneLoadingTerrain) {
                //packets.forEach(PacketUtils::sendPacketNoEvent);
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent((Packet<?>) packets);
                packets.clear();
                blinking = false;
                exemptedPackets.clear();
                return;
            }

            final Packet<?> packet = event.getPacket();

            if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                    packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                    packet instanceof C01PacketEncryptionResponse) {
                return;
            }

            if (blinking && !dispatch) {
                if (exemptionWatch.delay(100)) {
                    exemptionWatch.reset();
                    exemptedPackets.clear();
                }

                if (!event.isCancelled() && exemptedPackets.stream().noneMatch(packetClass ->
                        packetClass == packet.getClass())) {
                    packets.add(packet);
                    event.setCancelled(true);
                }
            } else if (packet instanceof C03PacketPlayer) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
                packets.clear();
                dispatch = false;
            }
        }
    }

    public static void dispatch() {
        dispatch = true;
    }

    @EventTarget
    public void onWorld(EventWorldUnload event) {
        packets.clear();
        BlinkUtils.blinking = false;
    }
}
