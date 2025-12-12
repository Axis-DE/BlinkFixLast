package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.modules.impl.misc.AutoArmor;
import moe.ichinomiya.naven.modules.impl.misc.ContainerStealer;
import moe.ichinomiya.naven.ui.clickgui.ClientClickGUI;
import moe.ichinomiya.naven.utils.Managers.BlinkComponent;
import moe.ichinomiya.naven.utils.MoveUtils;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.ModeValue;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventRunTicks;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;

import java.util.Arrays;
import java.util.List;

import static moe.ichinomiya.naven.modules.impl.misc.ContainerStealer.isStealing;

@ModuleInfo(name = "InventoryMove", description = "Allows you to move while your inventory is open", category = Category.MOVEMENT)
public class InventoryMove extends Module {
    ModeValue mode = ValueBuilder.create(this, "Mode(Hypixel Mode is No Work!!)").setDefaultModeIndex(0).setModes("Normal", "Legit","Hypixel").build().getModeValue();
    private boolean blinking;

    private boolean isCurrentMode(String mode) {
        return this.mode.isCurrentMode(mode);
    }

    public boolean isLegit(){
        return isCurrentMode("Legit") && MoveUtils.isMoving();
    }


    public boolean isHypixel(){
        return isCurrentMode("Hypixel");
    }

    @EventTarget
    public void onMotion(EventRunTicks e) {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward);
            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack);
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight);
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft);
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump);
            mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint)
                    || BlinkFix.getInstance().getModuleManager().getModule(Sprint.class).isEnabled()
                    && !blinking;
        }
        List<String> Chestblacklist = Arrays.asList(
                "My Profile",
                "Game Menu",
                "Play SkyWars",
                "SkyWars Menu",
                "Collectibles",
                "SkyWars Lobby Selector",
                "BedWars Lobby Selector",
                "The Delivery Man",
                "SkyWars Solo Normal",
                "SkyWars Challenges",
                "Kit Selector",
                "Play Murder Mystery",
                "Play Bed Wars",
                "Play Duels",
                "Bed Wars Solo",
                "Bed Wars Doubles",
                "Bed Wars 3v3v3v3",
                "Bed Wars 4v4v4v4",
                "Bed Wars Duels",
                "The Bridge",
                "SkyWars Duels",
                "Other Modes",
                "Team Duels",
                "Solo Duels",
                "Spleef Duels",
                "Quakecraft Duels",
                "Parkour Duels",
                "Mega Walls Duels",
                "Bow Duels",
                "Duelist's Odyssey",
                "Classic Duels",
                "Bed Wars Practice",

                "Select Language",
                "个人档案",
                "SkyWars Doubles Normal"
        );
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof ClientClickGUI)){
            if (mc.thePlayer != null && mc.thePlayer.openContainer instanceof ContainerChest) {
                ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                IInventory lowerChest = chest.getLowerChestInventory();
                String name = lowerChest.getName();
//                System.out.println(name);
                if (Chestblacklist.contains(name)) {
                    return;
                }
            }
            blinking = true;
            BlinkComponent.blinking = true;
        }
        if (mc.currentScreen == null){
            BlinkComponent.dispatch();
            blinking = false;
        }
    }
}
