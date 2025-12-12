//package moe.ichinomiya.naven.utils;
//
//import moe.ichinomiya.naven.events.api.EventTarget;
//import moe.ichinomiya.naven.events.impl.EventPacket;
//import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
//import com.viaversion.viaversion.api.Via;
//import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
//import com.viaversion.viaversion.api.type.Type;
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.Packet;
//import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
//
//public class PacketUtil {
//    private static final Minecraft mc = Minecraft.getMinecraft();
//
//    public static void sendPacket (Packet<?> packet){
//        mc.getNetHandler().getNetworkManager().sendPacket(packet);
//    }
//    public static void sendPacketNoEvent(Packet<?> packet) {mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);}
//
//
//    public static void sendPlayerTryUseItem(int hand ,boolean Click){
//        PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
//        useItem.write(Type.VAR_INT, hand);
//        com.viaversion.viarewind.utils.PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
//        if (Click) {
//            mc.gameSettings.keyBindUseItem.pressed = true;
//        }
//    }
//
//
//
//
//    @EventTarget
//    public void onPacket(EventPacket eventPacket){
//        Packet<?> packet = eventPacket.getPacket();
//        if (packet instanceof CPacketPlayerTryUseItem){
//            CPacketPlayerTryUseItem cPacketPlayerTryUseItem = (CPacketPlayerTryUseItem) packet;
//        }
//    }
//
//
//    enum handType {
//        MainHand,
//        OffHand;
//    }
//
//
//
//}
