package moe.ichinomiya.naven.modules.impl.combat;


import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.status.client.C00PacketServerQuery;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.cooldown.CooldownBar;
import moe.ichinomiya.naven.utils.ChatUtils;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;


import java.util.concurrent.LinkedBlockingQueue;

import static moe.ichinomiya.naven.events.api.types.Priority.*;
//@NativeObfuscation(virtualize = NativeObfuscation.VirtualMachine.TIGER_BLACK)
@ModuleInfo(name = "AutoGapple", description = "Auto Eat Gapple When Attacking", category = Category.COMBAT)
public class AutoGapple extends Module {

    public int c03s = 0;
    public static boolean velocityed = true;
    private final LinkedBlockingQueue<Packet<?>> packets;
    public boolean eating = false;


    public FloatValue tick = ValueBuilder.create(this, "Eat Ticks").setDefaultFloatValue(33).setFloatStep(1).setMinFloatValue(32).setMaxFloatValue(35).build().getFloatValue();
    BooleanValue logging = ValueBuilder.create(this, "Loggin").setDefaultBooleanValue(false).build().getBooleanValue();
    public BooleanValue render = ValueBuilder.create(this, "Render").setDefaultBooleanValue(false).build().getBooleanValue();
    public BooleanValue stuck = ValueBuilder.create(this, "Cancel Move").setDefaultBooleanValue(false).build().getBooleanValue();
    private final CooldownBar cooldownBar = new CooldownBar(Integer.MAX_VALUE, "Eating ticks") {
        @Override
        public float getState() {
            return 1F - (c03s / (float) tick.getCurrentValue());
        }
    };
    public AutoGapple() {
        this.packets = new LinkedBlockingQueue<>();
    }
    @Override
    public boolean onEnable() {
        if (mc.thePlayer == null) return false;
        if (mc.thePlayer.isDead) return false;
        if (mc.isSingleplayer()){
            BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.INFO, "You can't use this module in single player!", 5000));
            this.setEnabled(false);
            return false;
        }
        this.packets.clear();
        this.c03s = 0;
        eating = false;
        return false;
    }

    @Override
    public boolean onDisable() {
        eating = false;
        blink();
        return false;
    }
    @EventTarget
    public void OnWorld(EventWorldUnload e){
        this.setEnabled(false);
    }



    @EventTarget
    public void onMoveMath(MoveMathEvent event) {
        if (Minecraft.getMinecraft().thePlayer.positionUpdateTicks < 20 && !velocityed) {
            return;
        } else if (velocityed) {
            velocityed = false;
        }
    }
    public int finditem() {
        if (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null)
            return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack();
            if (stack != null && stack.getItem() instanceof ItemAppleGold) {
                return i;
            }
        }
        return -1;
    }
    public static boolean cancelmove = true;

    @EventTarget
    public void onTick(EventTick e) {
        mc.thePlayer.setSprinting(false);
        mc.thePlayer.movementInput.moveStrafe  *= 0.2f;
        mc.thePlayer.movementInput.moveForward *= 0.2f;
        cancelmove = stuck.getCurrentValue();
    }

    @EventTarget
    public void onSlow(EventSlowdown e){
        e.setSlowdown(true);
    }

    @EventTarget(LOWEST)
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.POST) {
            this.packets.add(new C01PacketChatMessage());
        }
        if (e.getType() == EventType.PRE) {

            if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) {
                setEnabled(false);
                return;
            }

            if (finditem() == -1) {
                BlinkFix.getInstance().getModuleManager().getModule("AutoGapple").setEnabled(false);
                BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.ERROR, "You don't have any gapple!", 5000));
                return;
            }
            if (eating && c03s > 0 && render.getCurrentValue()) {
                BlinkFix.getInstance().getCooldownBarManager().addBar(cooldownBar);
            }
            eating = true;
            if (this.c03s >= this.tick.getCurrentValue()) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(finditem()));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                blink();
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                this.c03s = 0;
            } else if (mc.thePlayer.ticksExisted % 5 == 0) {
                while (!packets.isEmpty()) {
                    Packet<?> packet = packets.poll();
                    if (packet instanceof C01PacketChatMessage/* || packet instanceof C07PacketPlayerDigging*/) {
                        break;
                    }
                    if (packet instanceof C03PacketPlayer) {
                        c03s--;
                    }
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
                }
            }
            //if (eating) {
            //  mc.thePlayer.moveForward *= 0.2f;
            //mc.thePlayer.moveStrafing *= 0.2f;
            //    }
        }
    }
    long time = System.currentTimeMillis();

    @EventTarget
    private void onMoveInput(EventMoveInput e){
        if (eating){
          //  e.setForward(0.2F);
          //  e.setStrafe(0.2F);
        }
    }


    @EventTarget
    public void onPacket(EventPacket e) {
        Packet<?> packet = e.getPacket();
        if (packet == null) return;
        if (e.getEventType() == EventPacket.EventState.SEND) {
            if (packet instanceof C00Handshake
                    || packet instanceof C00PacketLoginStart
                    || packet instanceof C00PacketServerQuery
                    || packet instanceof C01PacketPing
                    || packet instanceof C01PacketEncryptionResponse
                    || packet instanceof C01PacketChatMessage) {
                return;
            }

            if (packet instanceof C03PacketPlayer) {
                this.c03s++;
                if (logging.getCurrentValue()) {
                    ChatUtils.addChatMessage(String.valueOf(c03s));
                }
            }
            if (packet instanceof C07PacketPlayerDigging || packet instanceof C09PacketHeldItemChange || packet instanceof C0EPacketClickWindow || packet instanceof C0DPacketCloseWindow) {
                e.setCancelled(true);
                return;
            }
            if (!(packet instanceof C08PacketPlayerBlockPlacement) && eating) {
                this.packets.add(packet);
                e.setCancelled(true);
            }
        }
    }

    private void blink() {
        if (mc.getNetHandler() == null || mc.thePlayer == null || mc.thePlayer.isDead)
            return;
        while (!this.packets.isEmpty()) {
            Packet<?> packet = this.packets.poll();
            if (packet instanceof C01PacketChatMessage
                    || packet instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
                    || packet instanceof C0EPacketClickWindow
                    || packet instanceof C0DPacketCloseWindow
                    || packet instanceof C09PacketHeldItemChange)
                continue;
            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
        }
        this.c03s = 0;
        ReleaseBlocking();
    }
    private void ReleaseBlocking(){
       // if (packets.isEmpty())
          //  return;
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new  C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        //mc.
    }
}
