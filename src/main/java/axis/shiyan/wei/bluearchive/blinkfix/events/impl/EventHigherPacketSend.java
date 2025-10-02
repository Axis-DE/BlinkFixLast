package axis.shiyan.wei.bluearchive.blinkfix.events.impl;


import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;
import net.minecraft.network.Packet;;

public class EventHigherPacketSend
        extends EventCancellable {
    public Packet packet;

    public EventHigherPacketSend(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return this.packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}

