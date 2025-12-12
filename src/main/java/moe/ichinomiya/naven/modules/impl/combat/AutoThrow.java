package moe.ichinomiya.naven.modules.impl.combat;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.misc.ChestAura;
import moe.ichinomiya.naven.modules.impl.misc.Disabler;
import moe.ichinomiya.naven.modules.impl.misc.Teams;
import moe.ichinomiya.naven.modules.impl.move.Blink;
import moe.ichinomiya.naven.modules.impl.move.Scaffold;
import moe.ichinomiya.naven.modules.impl.move.Stuck;
import moe.ichinomiya.naven.utils.FriendManager;
import moe.ichinomiya.naven.utils.RotationUtils;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import org.lwjgl.util.vector.Vector2f;

import java.util.Comparator;
import java.util.Optional;

@ModuleInfo(name = "AutoThrow", description = "Automatically throw snowballs and eggs.", category = Category.COMBAT)
public class AutoThrow extends Module {
    private final FloatValue minDistance = ValueBuilder.create(this, "Min Distance").setDefaultFloatValue(5).setFloatStep(1).setMinFloatValue(3).setMaxFloatValue(30).build().getFloatValue();
    private final FloatValue maxDistance = ValueBuilder.create(this, "Max Distance").setDefaultFloatValue(10).setFloatStep(1).setMinFloatValue(3).setMaxFloatValue(30).build().getFloatValue();
    private final FloatValue delay = ValueBuilder.create(this, "Delay").setDefaultFloatValue(500).setFloatStep(50).setMinFloatValue(50).setMaxFloatValue(2000).build().getFloatValue();
    private final TimeHelper timer = new TimeHelper();
    public Vector2f rotation;
    public int rotationSet;
    private int swapBack = -1;

    @EventTarget
    public void onTick(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() || BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()) {
                rotationSet = 0;
                return;
            }

            rotation = null;

            int throwableHotbar = -1;

            for (int hotbar = 0; hotbar < 9; hotbar++) {
                Slot slot = mc.thePlayer.getSlotFromPlayerContainer(hotbar + 36);

                if (slot.getHasStack() && (slot.getStack().getItem() == Items.egg || slot.getStack().getItem() == Items.snowball)) {
                    throwableHotbar = hotbar;
                    break;
                }
            }

            if (throwableHotbar != -1) {
                if (--rotationSet == 0) {
                    int originalHotbar = mc.thePlayer.inventory.currentItem;
                    boolean shouldSwap = originalHotbar != throwableHotbar;

                    if (shouldSwap) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(throwableHotbar));
                        swapBack = originalHotbar;
                    }

                    mc.getNetHandler().getNetworkManager().sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                } else if (getTarget().isPresent() && timer.delay(delay.getCurrentValue())) {
                    ChestAura chestAura = (ChestAura) BlinkFix.getInstance().getModuleManager().getModule(ChestAura.class);
                    Blink blink = (Blink) BlinkFix.getInstance().getModuleManager().getModule(Blink.class);
                    Disabler disabler = (Disabler) BlinkFix.getInstance().getModuleManager().getModule(Disabler.class);
                    Stuck stuck = (Stuck) BlinkFix.getInstance().getModuleManager().getModule(Stuck.class);

                    if ((mc.thePlayer.getHeldItem() == null || (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemEnderPearl)
                            &&!(mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) &&
                            !(mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) &&
                            !(mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion))) && (!chestAura.isEnabled() || chestAura.rotation == null) && !blink.isEnabled() && !stuck.isEnabled() && disabler.delayedServerPackets.size() < 2) {
                        rotation = getRotationToEntity(getTarget().get());
                        timer.reset();
                    }
                }
            }
        } else {
            if (swapBack != -1) {
                mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(swapBack));
                swapBack = -1;
            }
        }
    }

    private Vector2f getRotationToEntity(Entity target) {
        double distanceToEnt = mc.thePlayer.getDistanceToEntity(target);
        double predictX = target.posX + (target.posX - target.lastTickPosX) * (distanceToEnt * 0.8f);
        double predictY = target.posY + (target.posY - target.lastTickPosY) * (distanceToEnt * 0.8f);
        double predictZ = target.posZ + (target.posZ - target.lastTickPosZ) * (distanceToEnt * 0.8f);

        double x = predictX - mc.thePlayer.posX;
        double h = predictY + 1.2 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double z = predictZ - mc.thePlayer.posZ;

        double h1 = Math.sqrt(x * x + z * z);

        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = -RotationUtils.getTrajAngleSolutionLow((float) h1, (float) h, 0.6f, 0.006f);

        return new Vector2f(yaw, pitch);
    }

    private Optional<EntityPlayer> getTarget() {
        return mc.theWorld.playerEntities.stream()
                .filter(e -> !Teams.isSameTeam(e))
                .filter(e -> !FriendManager.isFriend(e))
                .filter(e -> !AntiBot.isBot(e))
                .filter(e -> !e.isFakePlayer())
                .filter(e -> mc.thePlayer.getHorizonDistanceToEntity(e) <= maxDistance.getCurrentValue() && mc.thePlayer.getHorizonDistanceToEntity(e) >= minDistance.getCurrentValue())
                .filter(mc.thePlayer::canEntityBeSeen)
                .filter(e -> !e.isInvisibleToPlayer(mc.thePlayer))
                .filter(e -> e != mc.thePlayer)
                .min(Comparator.comparingDouble(e1 -> mc.thePlayer.getDistanceToEntity(e1)));
    }
}
