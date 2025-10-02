package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventPacket;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventWorldUnload;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.Blink;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "AutoPhase", description = "", category = Category.MISC)
public class AutoPhase extends Module {
    boolean a = false;
    public static AutoPhase instance;
    public static boolean start = false;
    @EventTarget
    public void onWorld(EventWorldUnload e) {
        start = false;
    }

    @EventTarget
    public void onPacket(EventPacket event){
        Packet<?> packet = event.getPacket();
        if (packet instanceof S02PacketChat) {
            String s = ((S02PacketChat) event.getPacket()).getChatComponent().getFormattedText();
            if (s.contains("开始倒计时") && s.contains("1")) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Blink Blink = (Blink) BlinkFix.getInstance().getModuleManager().getModule(Blink.class);
                        mc.thePlayer.sendQueue.addToSendQueue(new C0FPacketConfirmTransaction(0, (short) 0,true));
                        Blink.setEnabled(false);
                        a = false;
                        start = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
            if (s.contains("开始倒计时") && s.contains("3")) {
                Blink Blink = (Blink) BlinkFix.getInstance().getModuleManager().getModule(Blink.class);
                start = true;
                if (Blink.onEnable()) Blink.setEnabled(false);
               Blink.setEnabled(true);
                new Thread(() -> {
                    try {
                        Thread.sleep(6000);
                        mc.thePlayer.sendQueue.addToSendQueue(new C0FPacketConfirmTransaction(0, (short) 0,true));
                        BlinkFix.getInstance().getModuleManager().getModule("Blink").setEnabled(false);
                        a = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();

                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), EnumFacing.UP));
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 2, mc.thePlayer.posZ), EnumFacing.UP));
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 3, mc.thePlayer.posZ), EnumFacing.UP));
                mc.theWorld.setBlockToAir(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
                mc.theWorld.setBlockToAir(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 2, mc.thePlayer.posZ));
                mc.theWorld.setBlockToAir(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 3, mc.thePlayer.posZ));

                a = true;
            }
        }
    }
}
