package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(name = "Teams", description = "Prevent attack teammates", category = Category.MISC)
public class Teams extends Module {
    public BooleanValue removeHitbox = ValueBuilder.create(this, "Remove Hitbox").setDefaultBooleanValue(false).build().getBooleanValue();

    private static String localPlayerTeam;

    public static boolean isSameTeam(Entity player) {
        if (!BlinkFix.getInstance().getModuleManager().getModule(Teams.class).isEnabled()) {
            return false;
        }

        if (player instanceof EntityPlayer && localPlayerTeam != null) {
            return player.getDisplayName().getFormattedText().startsWith(localPlayerTeam);
        }

        return false;
    }

    public static boolean isSameTeam(String displayName) {
        if (!BlinkFix.getInstance().getModuleManager().getModule(Teams.class).isEnabled()) {
            return false;
        }

        if (localPlayerTeam != null) {
            return displayName.startsWith(localPlayerTeam);
        }

        return false;
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            String playerName = mc.thePlayer.getDisplayName().getFormattedText();
            localPlayerTeam = playerName.substring(0, 2);
        }
    }
}
