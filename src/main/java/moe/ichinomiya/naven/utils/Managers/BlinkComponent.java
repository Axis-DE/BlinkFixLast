package moe.ichinomiya.naven.utils.Managers;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventWorldUnload;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;

import java.util.concurrent.ConcurrentLinkedQueue;

import static moe.ichinomiya.naven.events.api.types.Priority.HIGHEST;
import static moe.ichinomiya.naven.utils.Utils.mc;

public final class BlinkComponent {

    public static final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    public static boolean blinking;

    @EventTarget(HIGHEST)
    public void onPacket(EventPacket event){
        if (mc.thePlayer == null) {
            packets.clear();
            return;
        }

        if(event.getType() == EventType.SEND) {
            if (mc.thePlayer.isDead || mc.isSingleplayer() || !mc.getNetHandler().doneLoadingTerrain) {
                packets.forEach(mc.getNetHandler().getNetworkManager()::sendPacketNoEvent);
                packets.clear();
                blinking = false;
                return;
            }

            final Packet<?> packet = event.getPacket();

            if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                    packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                    packet instanceof C01PacketEncryptionResponse) {
                return;
            }

            if (blinking) {
                if (!event.isCancelled()) {
                    packets.add(packet);
                    event.setCancelled(true);
                }
            }
        }
    }


    public static void dispatch() {
         blinking = false;
         packets.forEach(mc.getNetHandler()::addToSendQueueUnregistered);
         packets.clear();
    }

    @EventTarget(HIGHEST)
    public void onWorld(EventWorldUnload event) {
        packets.forEach(mc.getNetHandler()::addToSendQueueUnregistered);
        packets.clear();
        BlinkComponent.blinking = false;
    }
}
