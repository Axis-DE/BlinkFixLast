package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.cooldown.CooldownBar;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.Notification;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.NotificationLevel;
import axis.shiyan.wei.bluearchive.blinkfix.utils.ChatUtils;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import tech.skidonion.obfuscator.annotations.ControlFlowObfuscation;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import java.util.concurrent.LinkedBlockingQueue;

import static axis.shiyan.wei.bluearchive.blinkfix.events.api.types.Priority.LOWEST;

@ControlFlowObfuscation
@NativeObfuscation(virtualize = NativeObfuscation.VirtualMachine.TIGER_BLACK)
@ModuleInfo(name = "AutoGapple", description = "Auto Eat Gapple When Attacking", category = Category.COMBAT)
public class AutoGapple extends Module {

    public int c03s = 0;
    public static boolean velocityed = true;
    private final LinkedBlockingQueue<Packet<?>> packets;
    boolean eating = false;
    private boolean hasEatenOnce = false;

    FloatValue tick = ValueBuilder.create(this, "Eat Ticks").setDefaultFloatValue(33).setFloatStep(1).setMinFloatValue(32).setMaxFloatValue(35).build().getFloatValue();
    BooleanValue logging = ValueBuilder.create(this, "Loggin").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue devlog = ValueBuilder.create(this, "DevLog").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue render = ValueBuilder.create(this, "Render").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue once = ValueBuilder.create(this, "Once").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue stuck = ValueBuilder.create(this, "Cancel Move").setDefaultBooleanValue(false).build().getBooleanValue();
    ModeValue rendermode = ValueBuilder.create(this, "Render Mode")
            .setModes("Naven", "NoTick")
            .setVisibility(() -> render.getCurrentValue())
            .build()
            .getModeValue();

    private CooldownBar cooldownBar;

    public AutoGapple() {
        this.packets = new LinkedBlockingQueue<>();
    }

    @Override
    public boolean onEnable() {
        if (mc.thePlayer == null) return false;
        if (mc.thePlayer.isDead) return false;
        if (mc.isSingleplayer()) {
            BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.INFO, "You can't use gapple in Singleplayer !", 3000));
            this.setEnabled(false);
            return false;
        }
        this.packets.clear();
        this.c03s = 0;
        eating = false;
        hasEatenOnce = false;
        resetCooldownBar();
        return false;
    }


    @Override
    public void onDisable() {
        eating = false;
        blink();
        cleanupCooldownBar();
    }

    @EventTarget
    public void OnWorld(EventWorldUnload e) {
        this.setEnabled(false);
    }

    @EventTarget
    public void onMoveMath(EventMotionCalculate event) {
        if (Minecraft.getMinecraft().thePlayer.positionUpdateTicks < 19 && !velocityed) {
            return;
        } else if (velocityed) {
            velocityed = false;
        }
    }

    public int finditem() {
        if (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null)
            return -100;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack();
            if (stack != null && stack.getItem() instanceof ItemAppleGold) {
                return i;
            }
        }
        return -100;
    }

    public static boolean cancelmove = true;

    @EventTarget
    public void onTick(EventTick e) {
        if (mc.thePlayer == null) return;
        mc.thePlayer.setSprinting(false);
        if (eating && stuck.getCurrentValue()) {
            mc.thePlayer.movementInput.moveStrafe *= 0.2f;
            mc.thePlayer.movementInput.moveForward *= 0.2f;
        }
        cancelmove = stuck.getCurrentValue() && eating;
    }

    @EventTarget
    public void onSlow(EventSlowdown e) {
        e.setSlowdown(true);
    }

    @EventTarget(LOWEST)
    public void onMotion(EventMotion e) {
            if (e.getType() == EventType.POST) {
                this.packets.add(new C01PacketChatMessage("sb"));
                if (eating && c03s > 0 && render.getCurrentValue()) {
                    ensureCooldownBar();
                    float progress = c03s / (float) tick.getCurrentValue();
                    int progressPercent = (int) (progress * 100);
                    if (progressPercent >= 100) {
                        progressPercent = 99;
                    }
                    cooldownBar.setProgress(progressPercent);
                    if (rendermode.isCurrentMode("Naven")) {
                        cooldownBar.setTitle("Eating: " + c03s + "/" + (int) tick.getCurrentValue() + " ticks");
                    } else if (rendermode.isCurrentMode("NoTick")) {
                        cooldownBar.setTitle("Eating ticks");
                    } else if (!eating && render.getCurrentValue()) {
                        ensureCooldownBar();
                        cooldownBar.setProgress(0);
                    }
                }
            }

        if (e.getType() == EventType.PRE) {
            if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) {
                setEnabled(false);
                return;
            }

            if (finditem() == -100) {
                BlinkFix.getInstance().getModuleManager().getModule("AutoGapple").setEnabled(false);
                BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.WARNING, "You don't have any gapple !", 3000));
                return;
            }

            eating = true;
            if (this.c03s >= this.tick.getCurrentValue()) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(finditem()));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                blink();
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                this.c03s = 0;
                eating = false;
                if (!once.getCurrentValue()) {
                    BlinkFix.getInstance().getNotificationManager().addNotification(
                            new Notification(NotificationLevel.SUCCESS, "Eaten an golden apple", 1000L)
                    );
                }
                if (once.getCurrentValue() && !hasEatenOnce) {
                    hasEatenOnce = true;
                    this.setEnabled(false);
                }
            } else if (mc.thePlayer.ticksExisted % 5 == 0) {
                while (!packets.isEmpty()) {
                    final Packet<?> packet = packets.poll();

                    if (packet instanceof C01PacketChatMessage) {
                        break;
                    }

                    if (packet instanceof C03PacketPlayer) {
                        c03s--;
                    }
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
                }
            }

            if (eating) {
                mc.thePlayer.moveForward *= 0.2f;
                mc.thePlayer.moveStrafing *= 0.2f;
            }
        }
    }
    private void ensureCooldownBar() {
        if (cooldownBar == null) {
            cooldownBar = new CooldownBar();
            BlinkFix.getInstance().getCooldownBarManager().addBar(cooldownBar);
        }
    }

    private void resetCooldownBarProgress() {
        if (cooldownBar != null) {
            cooldownBar.setProgress(0);
        }
    }

    @EventTarget
    private void onMoveInput(EventMoveInput e) {
        if (eating && stuck.getCurrentValue()) {
            if (devlog.getCurrentValue()) {
                ChatUtils.addChatMessage("MoveInput - Forward: " + e.getForward() + ", Strafe: " + e.getStrafe());
            }
                e.setForward(0.0F);
                e.setStrafe(0.0F);
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

        while (!packets.isEmpty()) {
            final Packet<?> packet = packets.poll();

            if (packet instanceof C01PacketChatMessage || packet instanceof C08PacketPlayerBlockPlacement || packet instanceof C07PacketPlayerDigging)
                continue;

            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
        }
        c03s = 0;
        ReleaseBlocking();
    }

    private void ReleaseBlocking() {
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
    }
    private void cleanupCooldownBar() {
        if (cooldownBar != null) {
            cooldownBar.markExpired();
            cooldownBar = null;
        }
    }
    private void resetCooldownBar() {
        resetCooldownBarProgress();
    }
}