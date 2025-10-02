package net.minecraft.network.play.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.ViaPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

@Data
@AllArgsConstructor
public class CPacketPlayerTryUseItem extends ViaPacket {
    int hand;

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {

    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {

    }

    @Override
    public void processPacket(INetHandlerPlayServer handler) {

    }
}
