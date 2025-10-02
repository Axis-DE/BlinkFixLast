package axis.shiyan.wei.bluearchive.blinkfix.utils.mousetweaks;

import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.MouseTweaker;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class MouseTweakerMain extends DeobfuscationLayer {
    public static boolean disableRMBTweak = false;

    private static GuiScreen oldGuiScreen = null;
    private static Object container = null;
    private static Slot oldSelectedSlot = null;
    private static Slot firstSlot = null;
    private static ItemStack oldStackOnMouse = null;
    private static boolean firstSlotClicked = false;
    private static boolean shouldClick = true;
    private static boolean disableForThisContainer = false;
    private static boolean disableWheelForThisContainer = false;

    private static GuiContainerID guiContainerID;

    private static boolean readConfig = false;

    public static void onUpdateInGame() {
        GuiScreen currentScreen = getCurrentScreen();
        if (currentScreen == null) {
            // Reset stuff
            oldGuiScreen = null;
            container = null;
            oldSelectedSlot = null;
            firstSlot = null;
            oldStackOnMouse = null;
            firstSlotClicked = false;
            shouldClick = true;
            disableForThisContainer = false;
            disableWheelForThisContainer = false;
            readConfig = true;

            guiContainerID = GuiContainerID.NOTASSIGNED;
        } else {
            if (readConfig) {
                readConfig = false;
            }

            if (guiContainerID == GuiContainerID.NOTASSIGNED) {
                guiContainerID = getGuiContainerID(currentScreen);
            }

            onUpdateInGui(currentScreen);
        }
    }

    public static void onUpdateInGui(GuiScreen currentScreen) {
        if (oldGuiScreen != currentScreen) {
            oldGuiScreen = currentScreen;

            // If we opened an inventory from another inventory (for example, NEI's options menu).
            guiContainerID = getGuiContainerID(currentScreen);
            if (guiContainerID == GuiContainerID.NOTGUICONTAINER) return;

            disableForThisContainer = isDisabledForThisContainer(currentScreen);
            disableWheelForThisContainer = isWheelDisabledForThisContainer(currentScreen);

            if (!disableForThisContainer) {
                container = getContainerWithID(currentScreen);
            }
        }

        if (guiContainerID == GuiContainerID.NOTGUICONTAINER) return;

        if ((MouseTweakerMain.disableRMBTweak || !MouseTweaker.getModule().rmbTweak.getCurrentValue()) && !MouseTweaker.getModule().lmbTweakWithItem.getCurrentValue() && !MouseTweaker.getModule().lmbTweakWithoutItem.getCurrentValue() && !MouseTweaker.getModule().wheelTweak.getCurrentValue())
            return;

        if (disableForThisContainer) return;

        // It's better to have this here, because there are some inventories
        // that change slot count at runtime (for example, NEI's crafting recipe GUI).

        if (mc.thePlayer.inventoryContainer == null) {
            return;
        }

        int slotCount = getSlotCountWithID(currentScreen);
        if (slotCount == 0) // If there are no slots, then there is nothing to do.
            return;

        int wheel = (MouseTweaker.getModule().wheelTweak.getCurrentValue() && !disableWheelForThisContainer) ? Mouse.getDWheel() : 0;

        if (wheel != 0) {
            System.out.println(wheel);
        }

        if (!Mouse.isButtonDown(1)) {
            firstSlotClicked = false;
            firstSlot = null;
            shouldClick = true;
        }

        Slot selectedSlot = getSelectedSlotWithID(currentScreen, slotCount);

        // Copy the stacks, so that they don't change while we do our stuff.
        ItemStack stackOnMouse = copyItemStack(getStackOnMouse());
        ItemStack targetStack = copyItemStack(getSlotStack(selectedSlot));

        // To correctly determine when and how the default RMB drag needs to be disabled, we need a bunch of conditions...
        if (Mouse.isButtonDown(1) && (oldStackOnMouse != stackOnMouse) && (oldStackOnMouse == null)) {
            shouldClick = false;
        }

        if (oldSelectedSlot != selectedSlot) {
            // ...and some more conditions.
            if (Mouse.isButtonDown(1) && !firstSlotClicked && (firstSlot == null) && (oldSelectedSlot != null)) {
                if (!areStacksCompatible(stackOnMouse, getSlotStack(oldSelectedSlot))) {
                    shouldClick = false;
                }

                firstSlot = oldSelectedSlot;
            }

            if (Mouse.isButtonDown(1) && (oldSelectedSlot == null) && !firstSlotClicked && (firstSlot == null)) {
                shouldClick = false;
            }

            if (selectedSlot == null) {
                oldSelectedSlot = selectedSlot;

                if ((firstSlot != null) && !firstSlotClicked) {
                    firstSlotClicked = true;
                    disableRMBDragWithID(currentScreen);
                    firstSlot = null;
                }

                return;
            }

            boolean shiftIsDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

            if (Mouse.isButtonDown(1)) { // Right mouse button
                if (MouseTweaker.getModule().rmbTweak.getCurrentValue() && !MouseTweakerMain.disableRMBTweak) {

                    if ((stackOnMouse != null) && areStacksCompatible(stackOnMouse, targetStack) && !isCraftingOutputSlot(currentScreen, selectedSlot)) {
                        if ((firstSlot != null) && !firstSlotClicked) {
                            firstSlotClicked = true;
                            disableRMBDragWithID(currentScreen);
                            firstSlot = null;
                        } else {
                            shouldClick = false;
                            disableRMBDragWithID(currentScreen);
                        }

                        clickSlot(currentScreen, selectedSlot, 1, false);
                    }

                }
            } else if (Mouse.isButtonDown(0)) { // Left mouse button
                if (stackOnMouse != null) {
                    if (MouseTweaker.getModule().lmbTweakWithItem.getCurrentValue()) {
                        if ((targetStack != null) && areStacksCompatible(stackOnMouse, targetStack)) {

                            if (shiftIsDown) { // If shift is down, we just shift-click the slot and the item gets moved into another inventory.
                                clickSlot(currentScreen, selectedSlot, 0, true);
                            } else { // If shift is not down, we need to merge the item stack on the mouse with the one in the slot.
                                if ((getItemStackSize(stackOnMouse) + getItemStackSize(targetStack)) <= getMaxItemStackSize(stackOnMouse)) {
                                    // We need to click on the slot so that our item stack gets merged with it,
                                    // and then click again to return the stack to the mouse.
                                    // However, if the slot is crafting output, then the item is added to the mouse stack
                                    // on the first click and we don't need to click the second time.
                                    clickSlot(currentScreen, selectedSlot, 0, false);
                                    if (!isCraftingOutputSlot(currentScreen, selectedSlot))
                                        clickSlot(currentScreen, selectedSlot, 0, false);
                                }
                            }
                        }

                    }
                } else if (MouseTweaker.getModule().lmbTweakWithoutItem.getCurrentValue()) {
                    if (targetStack != null) {
                        if (shiftIsDown) {
                            clickSlot(currentScreen, selectedSlot, 0, true);
                        }
                    }
                }
            }

            oldSelectedSlot = selectedSlot;
        }

        if ((wheel != 0) && (selectedSlot != null)) {
            int numItemsToMove = Math.abs(wheel);

            if (slotCount > 36) {
                ItemStack originalStack = getSlotStack(selectedSlot);
                boolean isCraftingOutput = isCraftingOutputSlot(currentScreen, selectedSlot);

                if ((originalStack != null) && ((stackOnMouse == null) || (isCraftingOutput == areStacksCompatible(originalStack, stackOnMouse)))) {
                    do {
                        Slot applicableSlot = null;

                        int slotCounter = 0;
                        int countUntil = slotCount - 36;
                        if (getSlotNumber(selectedSlot) < countUntil) {
                            slotCounter = countUntil;
                            countUntil = slotCount;
                        }

                        if ((wheel < 0) || MouseTweaker.getModule().wheelSearchOrder.isCurrentMode("First to last")) {
                            for (int i = slotCounter; i < countUntil; i++) {
                                Slot sl = getSlotWithID(currentScreen, i);
                                ItemStack stackSl = getSlotStack(sl);

                                if (stackSl == null) {
                                    if ((applicableSlot == null) && (wheel < 0) && sl.isItemValid(originalStack) && !isCraftingOutputSlot(currentScreen, sl)) {
                                        applicableSlot = sl;
                                    }
                                } else if (areStacksCompatible(originalStack, stackSl)) {
                                    if ((wheel < 0) && (stackSl.stackSize < stackSl.getMaxStackSize())) {
                                        applicableSlot = sl;
                                        break;
                                    } else if (wheel > 0) {
                                        applicableSlot = sl;
                                        break;
                                    }
                                }
                            }
                        } else {
                            for (int i = countUntil - 1; i >= slotCounter; i--) {
                                Slot sl = getSlotWithID(currentScreen, i);
                                ItemStack stackSl = getSlotStack(sl);

                                if (stackSl == null) {
                                    if ((applicableSlot == null) && (wheel < 0) && sl.isItemValid(originalStack)) {
                                        applicableSlot = sl;
                                    }
                                } else if (areStacksCompatible(originalStack, stackSl)) {
                                    if ((wheel < 0) && (stackSl.stackSize < stackSl.getMaxStackSize())) {
                                        applicableSlot = sl;
                                        break;
                                    } else if (wheel > 0) {
                                        applicableSlot = sl;
                                        break;
                                    }
                                }
                            }
                        }

                        if (isCraftingOutput) {
                            if (wheel < 0) {
                                boolean mouseWasEmpty = stackOnMouse == null;

                                for (int i = 0; i < numItemsToMove; i++) {
                                    clickSlot(currentScreen, selectedSlot, 0, false);
                                }

                                if ((applicableSlot != null) && mouseWasEmpty) {
                                    clickSlot(currentScreen, applicableSlot, 0, false);
                                }
                            }

                            break;
                        }

                        if (applicableSlot != null) {
                            Slot slotTo = (wheel < 0) ? applicableSlot : selectedSlot;
                            Slot slotFrom = (wheel < 0) ? selectedSlot : applicableSlot;
                            ItemStack stackTo = (getSlotStack(slotTo) != null) ? copyItemStack(getSlotStack(slotTo)) : null;
                            ItemStack stackFrom = copyItemStack(getSlotStack(slotFrom));

                            if (wheel < 0) {
                                numItemsToMove = Math.min(numItemsToMove, getItemStackSize(stackFrom));

                                if ((stackTo != null) && ((getMaxItemStackSize(stackTo) - getItemStackSize(stackTo)) <= numItemsToMove)) {
                                    clickSlot(currentScreen, slotFrom, 0, false);
                                    clickSlot(currentScreen, slotTo, 0, false);
                                    clickSlot(currentScreen, slotFrom, 0, false);

                                    numItemsToMove -= getMaxItemStackSize(stackTo) - getItemStackSize(stackTo);
                                } else {
                                    clickSlot(currentScreen, slotFrom, 0, false);

                                    if (getItemStackSize(stackFrom) <= numItemsToMove) {
                                        clickSlot(currentScreen, slotTo, 0, false);
                                    } else {
                                        for (int i = 0; i < numItemsToMove; i++) {
                                            clickSlot(currentScreen, slotTo, 1, false);
                                        }
                                    }

                                    clickSlot(currentScreen, slotFrom, 0, false);

                                    numItemsToMove = 0;
                                }
                            } else {
                                if ((getMaxItemStackSize(stackTo) - getItemStackSize(stackTo)) <= numItemsToMove) {
                                    clickSlot(currentScreen, slotFrom, 0, false);
                                    clickSlot(currentScreen, slotTo, 0, false);
                                    clickSlot(currentScreen, slotFrom, 0, false);
                                } else {
                                    clickSlot(currentScreen, slotFrom, 0, false);

                                    if (getItemStackSize(stackFrom) <= numItemsToMove) {
                                        clickSlot(currentScreen, slotTo, 0, false);
                                        numItemsToMove -= getMaxItemStackSize(stackFrom);
                                    } else {
                                        for (int i = 0; i < numItemsToMove; i++) {
                                            clickSlot(currentScreen, slotTo, 1, false);
                                        }

                                        numItemsToMove = 0;
                                    }

                                    clickSlot(currentScreen, slotFrom, 0, false);
                                }

                                if (getMaxItemStackSize(stackTo) == getMaxItemStackSize(stackTo)) {
                                    numItemsToMove = 0;
                                }
                            }
                        } else {
                            break;
                        }
                    } while (numItemsToMove != 0);
                }
            }
        }

        oldStackOnMouse = stackOnMouse;
    }

    public static GuiContainerID getGuiContainerID(GuiScreen currentScreen) {
        return GuiContainerID.MINECRAFT;
    }

    public static Object getContainerWithID(GuiScreen currentScreen) {
        return getContainer(asGuiContainer(currentScreen));
    }

    public static int getSlotCountWithID(GuiScreen currentScreen) {
        if (currentScreen instanceof GuiContainer) {
            return getSlots(asContainer(container)).size();
        }

        return 0;
    }

    public static boolean isDisabledForThisContainer(GuiScreen currentScreen) {
        if (!(currentScreen instanceof GuiContainer)) {
            return true;
        }

        return isGuiContainerCreative(currentScreen);
    }

    public static boolean isWheelDisabledForThisContainer(GuiScreen currentScreen) {
        return false;
    }

    public static Slot getSelectedSlotWithID(GuiScreen currentScreen, int slotCount) {
        return getSelectedSlot(asGuiContainer(currentScreen), asContainer(container), slotCount);
    }

    public static void clickSlot(GuiScreen currentScreen, Slot targetSlot, int mouseButton, boolean shiftPressed) {
        windowClick(getWindowId(asContainer(container)), getSlotNumber(targetSlot), mouseButton, shiftPressed ? 1 : 0);
    }

    public static boolean isCraftingOutputSlot(GuiScreen currentScreen, Slot targetSlot) {
        return isVanillaCraftingOutputSlot(asContainer(container), targetSlot);
    }

    public static Slot getSlotWithID(GuiScreen currentScreen, int slotNumber) {
        return getSlot(asContainer(container), slotNumber);
    }

    public static void disableRMBDragWithID(GuiScreen currentScreen) {
        disableVanillaRMBDrag(asGuiContainer(currentScreen));

        if (shouldClick) {
            clickSlot(currentScreen, firstSlot, 1, false);
        }
    }
}
