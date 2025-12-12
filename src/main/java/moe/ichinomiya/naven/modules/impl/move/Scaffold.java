package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.cooldown.CooldownBar;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.utils.font.FontManager;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;
import moe.ichinomiya.naven.values.impl.ModeValue;
import net.minecraft.block.*;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import moe.ichinomiya.naven.BlinkFix;

import java.util.*;

import static moe.ichinomiya.naven.ui.watermark.NavenWatermark.bodyColor;
import static moe.ichinomiya.naven.ui.watermark.NavenWatermark.headerColor;

@ModuleInfo(name = "Scaffold", description = "Automatically places blocks under you", category = Category.MOVEMENT)
public class Scaffold extends Module {
    public static final List<Block> blacklistedBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava, Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.chest, Blocks.trapped_chest, Blocks.torch, Blocks.anvil, Blocks.trapped_chest, Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus, Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.web, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall, Blocks.oak_fence, Blocks.redstone_torch);

    public float[] rots = new float[2];
    public float[] lastRots = new float[2];
    public BooleanValue customFov = ValueBuilder.create(this, "Custom Scaffold FoV").setDefaultBooleanValue(false).build().getBooleanValue();
    public FloatValue fov = ValueBuilder.create(this, "Custom FoV").setDefaultFloatValue(1).setMaxFloatValue(2f).setMinFloatValue(0.5f).setFloatStep(0.05f).setVisibility(() -> customFov.getCurrentValue()).build().getFloatValue();
    ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Normal", "Telly Bridge","Hypixel").build().getModeValue();
    FloatValue sprintTicks = ValueBuilder.create(this, "Sprint Ticks").setDefaultFloatValue(2).setMaxFloatValue(5).setMinFloatValue(1).setFloatStep(1).setVisibility(() -> isCurrentMode("Telly Bridge")).build().getFloatValue();
    public BooleanValue blocksWidget = ValueBuilder.create(this, "Blocks Widget").setDefaultBooleanValue(true).build().getBooleanValue();
    public ModeValue blocksWidgetMode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("DynamicIsland", "Naven").build().getModeValue();
    BooleanValue eagle = ValueBuilder.create(this, "Eagle").setDefaultBooleanValue(true).setVisibility(() -> isCurrentMode("Normal")).build().getBooleanValue();
    BooleanValue esp = ValueBuilder.create(this, "ESP").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue itemSwitchBack = ValueBuilder.create(this, "Item Switch Back").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue renderItemSpoof = ValueBuilder.create(this, "Render Item Spoof").setDefaultBooleanValue(false).setVisibility(() -> itemSwitchBack.getCurrentValue()).build().getBooleanValue();
    public FloatValue blockSwitchCount = ValueBuilder.create(this, "Early SwitchCount").setDefaultFloatValue(3f).setMaxFloatValue(5f).setMinFloatValue(1f).setFloatStep(1f).build().getFloatValue();
    public int enableSlot;
    public int slotID;
    private BlockPos pos;

    private int currentBlocks = 0;

    public static boolean isValidStack(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemBlock && stack.stackSize > 1) {
            return !blacklistedBlocks.contains(((ItemBlock) stack.getItem()).getBlock());
        }
        return false;
    }

    public static boolean isOnBlockEdge(float sensitivity) {
        return mc.theWorld.getCollisionBoxes(mc.thePlayer.getEntityBoundingBox().offset(0.0, -0.5, 0.0).expand(-sensitivity, 0.0, -sensitivity)).isEmpty();
    }

    private static Vec3 getVec3(BlockPos checkPosition, Block block) {
        double ex = MathHelper.clamp_double(mc.thePlayer.posX, checkPosition.getX(), (double) checkPosition.getX() + block.getBlockBoundsMaxX());
        double ey = MathHelper.clamp_double(mc.thePlayer.posY, checkPosition.getY(), (double) checkPosition.getY() + block.getBlockBoundsMaxY());
        double ez = MathHelper.clamp_double(mc.thePlayer.posZ, checkPosition.getZ(), (double) checkPosition.getZ() + block.getBlockBoundsMaxZ());
        return new Vec3(ex, ey, ez);
    }

    public boolean isCurrentMode(String mode) {
        return this.mode.isCurrentMode(mode);
    }

    TimeHelper disabledHelper = new TimeHelper();

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof S08PacketPlayerPosLook && !e.isCancelled()) {
            disabledHelper.reset();
            toggle();

            BlinkFix.getInstance().getCooldownBarManager().addBar(new CooldownBar(1000, "Scaffold Temporarily Disabled"));
        }
    }

    float x, y, width;

    @EventTarget
    public void onRender2D(EventRenderUI e) {
        // Draw blocks left on the screen
        if (blocksWidget.getCurrentValue()) {
            if (blocksWidgetMode.isCurrentMode("Naven")) {
                ScaledResolution resolution = e.getResolution();

                FontManager fontManager = BlinkFix.getInstance().getFontManager();
                String text = "Blocks: " + currentBlocks;

                int stringWidth = fontManager.opensans15.getStringWidth(text);

                x = resolution.getScaledWidth() / 2f - stringWidth / 2f - 5;
                y = resolution.getScaledHeight() / 2f + 30;

                width = stringWidth + 12;

                StencilUtils.write(false);
                RenderUtils.drawBoundRoundedRect(x, y, width, 18, 5f, 0xFFFFFFFF);
                StencilUtils.erase(true);

                RenderUtils.drawRectBound(x, y, width, 3, headerColor);
                RenderUtils.drawRectBound(x, y + 3, width, 15, bodyColor);

                fontManager.opensans15.drawString(text, x + 5, y + 5, 0xFFFFFFFF);
                StencilUtils.dispose();
            }
        }
    }
    public int getCurrentHeldBlocks() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) heldItem.getItem()).getBlock();
            if (!blacklistedBlocks.contains(block)) {
                return heldItem.stackSize;
            }
        }
        return 0;
    }
    @EventTarget
    public void onShader(EventShader e) {
        if (blocksWidget.getCurrentValue()) {
            if (blocksWidgetMode.isCurrentMode("Naven")) {
                RenderUtils.drawBoundRoundedRect(x, y, width, 18, 5f, 0xFFFFFFFF);
            }
        }
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        toggle();
    }

    public boolean onEnable() {
        if (!disabledHelper.delay(1000)) {
            toggle();
            return false;
        }

        if (!eagle.isVisible()) {
            eagle.setCurrentValue(true);
        }

        enableSlot = mc.thePlayer.inventory.currentItem;

        super.onEnable();
        if (mc.thePlayer != null) {
            this.rots[0] = mc.thePlayer.rotationYaw - 180;
            this.rots[1] = 90;
            this.lastRots[0] = mc.thePlayer.prevRotationYaw;
            this.lastRots[1] = 90;
            this.pos = null;
        }
        return false;
    }

    public boolean onDisable() {
        super.onDisable();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
        this.rots = this.lastRots;

        if (itemSwitchBack.getCurrentValue()) {
            mc.thePlayer.inventory.currentItem = enableSlot;
        }
        return false;
    }

    @EventTarget
    public void onRender(EventRender e) {
        if (esp.getCurrentValue() && pos != null) {
            Render3DUtils.drawSolidBlockESP(pos, 0x55ff0000, 1);
        }
    }

    int onGround;

    @EventTarget(Priority.HIGH)
    public void onEventEarlyTick(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            currentBlocks = 0;
            for (Slot slot : mc.thePlayer.inventoryContainer.inventorySlots) {
                if (slot.getHasStack() && isValidStack(slot.getStack())) {
                    currentBlocks += slot.getStack().stackSize;
                }
            }

            if (itemSwitchBack.getCurrentValue() && renderItemSpoof.getCurrentValue()) {
                mc.entityRenderer.skipRender = 1;
            }

            if (!mc.gameSettings.keyBindJump.isKeyDown() && eagle.getCurrentValue()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), mc.thePlayer.onGround && isOnBlockEdge(0.3f));
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }

            int bestSlot = -1;
            int maxCount = 0;
            for (int i = 36; i < mc.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (stack != null && stack.getItem() instanceof ItemBlock && isValidStack(stack)) {
                    if (stack.stackSize > maxCount) {
                        maxCount = stack.stackSize; bestSlot = i - 36;
                    }
                }
            }

            boolean needSwitch = false;
            if (mc.thePlayer.getHeldItem() == null) {
                needSwitch = true;
            } else if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)) {
                needSwitch = true;
            } else if (mc.thePlayer.getHeldItem().stackSize <= blockSwitchCount.getCurrentValue()) {
                needSwitch = true;
            }
            if (needSwitch && bestSlot != -1) { mc.thePlayer.inventory.currentItem = bestSlot; }

            onGround ++;
            if (mc.thePlayer.onGround) {
                onGround = 0;
            }
//
//            mc.thePlayer.inventory.currentItem = this.slotID;

            boolean isHoldingJump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());

            if (isCurrentMode("Telly Bridge") || isCurrentMode("Hypixel")) {
                mc.gameSettings.keyBindJump.pressed = PlayerUtils.movementInput() || isHoldingJump;
                if (onGround < (isCurrentMode("Hypixel") ? 1 : sprintTicks.getCurrentValue()) && PlayerUtils.movementInput()) {
                    rots[0] = mc.thePlayer.rotationYaw;
                    return;
                }
            }
            this.pos = this.getBlockPos();

            this.lastRots[0] = this.rots[0];
            this.lastRots[1] = this.rots[1];

            if (this.pos != null) {
                this.rots = this.getPlayerYawRotation();
            }
        }
    }

    public boolean isValidBlock(final BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        return !(block instanceof BlockLiquid) && !(block instanceof BlockAir) && !(block instanceof BlockChest) && !(block instanceof BlockFurnace) && !(block instanceof BlockEnderChest) && !(block instanceof BlockTallGrass) && !(block instanceof BlockSnow);
    }

    @EventTarget
    public void onClick(EventClick e) {
        placeBlock();
        e.setCancelled(true);
    }

    private void placeBlock() {
        if (this.pos != null) {
            MovingObjectPosition objectPosition = RayTraceUtils.rayCast(1, new float[]{RotationManager.rotations.x, RotationManager.rotations.y});
            ItemStack itemStack = this.getItemStack();

            if (itemStack != null && itemStack.stackSize > 0 && objectPosition != null && objectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (objectPosition.sideHit != EnumFacing.UP || mc.thePlayer.onGround || !PlayerUtils.movementInput() || isCurrentMode("Normal") || (isCurrentMode("Telly Bridge") || isCurrentMode("Hypixel")) && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
                    if (objectPosition.getBlockPos().equals(this.pos)) {
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) {
                            mc.thePlayer.swingItem();
                        }
                    } else if (this.isNearbyBlockPos(objectPosition.getBlockPos()) && objectPosition.sideHit != EnumFacing.UP) {
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) {
                            mc.thePlayer.swingItem();
                        }
                    }
                }
            }
        }
    }

    int ticks = 0;

    private float[] getPlayerYawRotation() {
        float yaw, pitch;
        float rotationYaw = mc.thePlayer.rotationYaw - 125.0F;

        if (this.isTower()) {
            MovingObjectPosition objectPosition = mc.objectMouseOver;
            if (objectPosition != null) {
                yaw = rotationYaw;
                pitch = 90;
                return new float[]{yaw, pitch};
            }
        }

        yaw = rotationYaw;
        pitch = 82F;
        float[] rotations = new float[]{yaw, pitch};

        float realYaw = mc.thePlayer.rotationYaw;

        float[] dynamicOffsets = new float[]{-0.1f, -0.07f, 0.07f, 0.1f};
        float magic = dynamicOffsets[ticks++ % dynamicOffsets.length];

        realYaw += magic;

        if (mc.gameSettings.keyBindBack.pressed) {
            realYaw += 180.0F;
            if (mc.gameSettings.keyBindLeft.pressed) {
                realYaw += 45.0F;
            } else if (mc.gameSettings.keyBindRight.pressed) {
                realYaw -= 45.0F;
            }
        } else if (mc.gameSettings.keyBindForward.pressed) {
            if (mc.gameSettings.keyBindLeft.pressed) {
                realYaw -= 45.0F;
            } else if (mc.gameSettings.keyBindRight.pressed) {
                realYaw += 45.0F;
            }
        } else if (mc.gameSettings.keyBindRight.pressed) {
            realYaw += 90.0F;
        } else if (mc.gameSettings.keyBindLeft.pressed) {
            realYaw -= 90.0F;
        }

        yaw = realYaw - 180.0F;
        rotations[0] = yaw;

        if (this.shouldBuild()) {
            float[] initialRotation = {rotations[0], rotations[1]};
            MovingObjectPosition initialHit = performRayCast(initialRotation);
            if (isHitValid(initialHit)) {
                return initialRotation;
            }
            if (isCurrentMode("Hypixel")) {
                rotations[0] = mc.thePlayer.rotationYaw - 180;
            }
            ArrayList<Float> validPitches = findValidPitches(yaw);
            if (!validPitches.isEmpty()) {
                validPitches.sort(Comparator.comparingDouble(this::distanceToLastPitch));
                rotations[1] = validPitches.get(0);
                return rotations;
            } else {
                return findOptimalRotation(yaw);
            }
        }
        if (isCurrentMode("Hypixel")) {
            rotations[0] = mc.thePlayer.rotationYaw - 180;
        }
        return rotations;
    }

    private MovingObjectPosition performRayCast(float[] rotation) {
        return RayTraceUtils.rayCast(1.0F, new float[]{rotation[0], rotation[1]});
    }

    private boolean isHitValid(MovingObjectPosition hit) {
        return hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                isValidBlock(hit.getBlockPos()) &&
                this.isNearbyBlockPos(hit.getBlockPos()) &&
                hit.sideHit != EnumFacing.DOWN &&
                hit.sideHit != EnumFacing.UP;
    }

    private ArrayList<Float> findValidPitches(float yaw) {
        ArrayList<Float> validPitches = new ArrayList<>();
        for (float i = Math.max(this.rots[1] - 30.0F, -90.0F); i < Math.min(this.rots[1] + 20.0F, 90F); i += 0.3F) {
            float[] f = RotationUtils.getFixedRotation(yaw, i, this.rots[0], this.rots[1]);
            MovingObjectPosition position = performRayCast(new float[]{yaw, f[1]});
            if (isHitValid(position)) {
                validPitches.add(f[1]);
            }
        }
        return validPitches;
    }

    private float[] findOptimalRotation(float yaw) {
        for (float yawLoops = 0; yawLoops < 180; yawLoops += 2) {
            float currentPitch = this.rots[1];

            for (float pitchLoops = 0; pitchLoops < 25; pitchLoops += 2) {
                for (int i = 0; i < 2; i++) {
                    float pitch = currentPitch - (pitchLoops * (i == 0 ? 1 : -1));

                    float[][] offsets = {
                            {yaw + yawLoops, pitch},
                            {yaw - yawLoops, pitch}
                    };

                    for (float[] rotation : offsets) {
                        float rayCastPitch = MathHelper.clamp_float(rotation[1], -90, 90);
                        float[] fixedRotation = RotationUtils.getFixedRotation(rotation[0], rayCastPitch, this.rots[0], this.rots[1]);
                        MovingObjectPosition position = performRayCast(fixedRotation);

                        if (isHitValid(position)) {
                            return fixedRotation;
                        }
                    }
                }
            }
        }
        if (isCurrentMode("Hypixel")) {
            yaw = mc.thePlayer.rotationYaw - 180;
        }
        return new float[]{yaw, this.rots[1]};
    }

    private double distanceToLastPitch(float pitch) {
        return Math.abs(pitch - this.rots[1]);
    }

    private boolean isNearbyBlockPos(BlockPos blockPos) {
        if (!mc.thePlayer.onGround) {
            return blockPos.equals(this.pos);
        } else {
            for (int x = this.pos.getX() - 1; x <= this.pos.getX() + 1; ++x) {
                for (int z = this.pos.getZ() - 1; z <= this.pos.getZ() + 1; ++z) {
                    if (blockPos.equals(new BlockPos(x, this.pos.getY(), z))) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private ItemStack getItemStack() {
        ItemStack itemStack = mc.thePlayer.getCurrentEquippedItem();
        this.slotID = mc.thePlayer.inventory.currentItem;
        return itemStack;
    }

    private BlockPos getBlockPos() {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0D, mc.thePlayer.posZ);
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, BlockPos> hashMap = new HashMap<>();

        for (int x = playerPos.getX() - 5; x <= playerPos.getX() + 5; ++x) {
            for (int y = playerPos.getY() - 1; y <= playerPos.getY(); ++y) {
                for (int z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; ++z) {
                    BlockPos checkPosition = new BlockPos(x, y, z);
                    if (isValidBlock(checkPosition)) {
                        Block block = mc.theWorld.getBlockState(checkPosition).getBlock();
                        Vec3 vec3 = getVec3(checkPosition, block);
                        positions.add(vec3);
                        hashMap.put(vec3, checkPosition);
                    }
                }
            }
        }

        if (!positions.isEmpty()) {
            positions.sort(Comparator.comparingDouble(this::getBlockDistance));
            if (this.isTower() && (double) hashMap.get(positions.get(0)).getY() != mc.thePlayer.posY - 1.5D) {
                return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.5D, mc.thePlayer.posZ);
            } else {
                return hashMap.get(positions.get(0));
            }
        } else {
            return null;
        }
    }

    private boolean shouldBuild() {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5D, mc.thePlayer.posZ);
        return mc.theWorld.isAirBlock(playerPos);
    }

    private double getBlockDistance(Vec3 vec3) {
        return mc.thePlayer.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    private boolean isTower() {
        return mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindBack.isKeyDown() && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown();
    }
}
