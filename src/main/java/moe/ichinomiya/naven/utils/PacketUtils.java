//package moe.ichinomiya.naven.utils;
//
//import com.viaversion.viaversion.api.Via;
//import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
//import com.viaversion.viaversion.api.type.Types;
//import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
//import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.Packet;
//
//public class PacketUtils {
//    private static final Minecraft mc = Minecraft.getMinecraft();
//
//    public static void sendPacket (Packet<?> packet){
//        mc.getNetHandler().getNetworkManager().sendPacket(packet);
//    }
//    public static void sendPacketNoEvent(Packet<?> packet) {
//        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
//    }
//
//    public static void sendPlayerTryUseItem(int hand){
//        PacketWrapper useItem = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
//        useItem.write(Types.VAR_INT, hand);
//        useItem.sendToServer(Protocol1_8To1_9.class,true);
//    }
//
//
//}
