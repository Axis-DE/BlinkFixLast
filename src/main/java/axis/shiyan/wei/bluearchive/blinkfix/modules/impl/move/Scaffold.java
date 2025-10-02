package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.utils.*;
import lombok.AllArgsConstructor;
import axis.shiyan.wei.bluearchive.blinkfix.events.EventLivingUpdate;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.Priority;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.cooldown.CooldownBar;
import axis.shiyan.wei.bluearchive.blinkfix.utils.font.FontManager;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
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

import java.util.*;

import static axis.shiyan.wei.bluearchive.blinkfix.ui.watermark.NavenWatermark.bodyColor;
import static axis.shiyan.wei.bluearchive.blinkfix.ui.watermark.NavenWatermark.headerColor;
@ModuleInfo(name = "Scaffold", description = "Automatically places blocks under you", category = Category.MOVEMENT)
public class Scaffold extends Module {
    public static final List<Block> blacklistedBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava, Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.chest, Blocks.trapped_chest, Blocks.torch, Blocks.anvil, Blocks.trapped_chest, Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus, Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.web, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall, Blocks.oak_fence, Blocks.redstone_torch);

    public float[] rots = new float[2];
    public float[] lastRots = new float[2];
    ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Normal", "Keep Y", "Telly Bridge").build().getModeValue();
    FloatValue sprintTicks = ValueBuilder.create(this, "Sprint Ticks").setDefaultFloatValue(2).setMaxFloatValue(5).setMinFloatValue(1).setFloatStep(1).setVisibility(() -> isCurrentMode("Telly Bridge")).build().getFloatValue();
    BooleanValue blocksWidget = ValueBuilder.create(this, "Blocks Widget").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue eagle = ValueBuilder.create(this, "Eagle").setDefaultBooleanValue(true).setVisibility(() -> !isCurrentMode("Telly Bridge")).build().getBooleanValue();
    BooleanValue esp = ValueBuilder.create(this, "ESP").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue customFov = ValueBuilder.create(this, "Custom Scaffold FoV").setDefaultBooleanValue(false).build().getBooleanValue();
    public FloatValue fov = ValueBuilder.create(this, "Custom FoV").setDefaultFloatValue(1).setMaxFloatValue(2f).setMinFloatValue(0.5f).setFloatStep(0.05f).setVisibility(() -> customFov.getCurrentValue()).build().getFloatValue();
    BooleanValue itemSwitchBack = ValueBuilder.create(this, "Item Switch Back").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue renderItemSpoof = ValueBuilder.create(this, "Render Item Spoof").setDefaultBooleanValue(false).setVisibility(() -> itemSwitchBack.getCurrentValue()).build().getBooleanValue();
    BooleanValue swing = ValueBuilder.create(this, "Swing").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue skiptick = ValueBuilder.create(this, "Auto SkipTick").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue sidewayRot = ValueBuilder.create(this, "SideWay Rotation").setDefaultBooleanValue(false).build().getBooleanValue();
    public PlaceData data;
    public int enableSlot;
    private int slotID;
    private BlockPos pos;
    private int jumpCount = 0;
    private boolean stopSprint = false;
    private int currentBlocks = 0;

    public static boolean isValidStack(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemBlock && stack.stackSize > 0) {
            return !blacklistedBlocks.contains(((ItemBlock) stack.getItem()).getBlock());
        }
        return false;
    }

    @AllArgsConstructor
    public static class PlaceData {
        public BlockPos blockPos;
        public EnumFacing facing;
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

    private boolean isCurrentMode(String mode) {
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
    public void onRender2D(EventRender2D e) {
        // Draw blocks left on the screen
        if (blocksWidget.getCurrentValue()) {
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

    @EventTarget
    public void onShader(EventShader e) {
        if (blocksWidget.getCurrentValue()) {
            RenderUtils.drawBoundRoundedRect(x, y, width, 18, 5f, 0xFFFFFFFF);
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

    public void onDisable() {
        placeCount = 0;
        super.onDisable();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
        this.rots = this.lastRots;
        mc.theWorld.skiptick = 0;

        if (itemSwitchBack.getCurrentValue()) {
            mc.thePlayer.inventory.currentItem = enableSlot;
        }
    }
    private int onGroundTick;


    @EventTarget
    public void onRender(EventRender e) {
        if (esp.getCurrentValue() && pos != null) {
            Render3DUtils.drawSolidBlockESP(pos, 0x55ff0000, 1);
        }
    }
    boolean reachable = true;
    int onGround;
    private int rotateCount = 0;
    @EventTarget(Priority.HIGH)
    public void onEventEarlyTick(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            currentBlocks = 0;
            for (Slot slot : mc.thePlayer.inventoryContainer.inventorySlots) {
                if (slot.getHasStack() && isValidStack(slot.getStack())) {
                    currentBlocks += slot.getStack().stackSize;
                }
            }
            if (currentBlocks <=0) return;
            if (itemSwitchBack.getCurrentValue() && renderItemSpoof.getCurrentValue()) {
                mc.entityRenderer.skipRender = 1;
            }

            if (!mc.gameSettings.keyBindJump.isKeyDown() && eagle.getCurrentValue()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), mc.thePlayer.onGround && isOnBlockEdge(0.3f));
            }

            for (int i = 36; i < mc.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (stack != null && stack.getItem() instanceof ItemBlock && stack.stackSize > 0 && isValidStack(stack)) {
                    this.slotID = i - 36;
                    break;
                }
            }

            onGround ++;
            if (mc.thePlayer.onGround) {
                onGround = 0;
            }
            if (mc.thePlayer.onGround){
                onGroundTick++;
            }
            if (onGroundTick > 10){
                onGroundTick = 0;
            }

            mc.thePlayer.inventory.currentItem = this.slotID;

            boolean isHoldingJump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());

            if (isCurrentMode("Telly Bridge")) {
                if (sidewayRot.getCurrentValue()) {
                    if (mc.thePlayer.onGround) {
                        if (jumpCount >= 2) {
                            stopSprint = true;
                            jumpCount = 0;
                        } else {
                            stopSprint = false;
                        }
                    }

                    if (stopSprint) {
                        mc.gameSettings.keyBindSprint.pressed = false;
                        mc.thePlayer.setSprinting(false);
                    }
                } else {
                    jumpCount = 0;
                    stopSprint = false;
                }

                mc.gameSettings.keyBindJump.pressed = PlayerUtils.movementInput() || isHoldingJump;

                if (onGround < sprintTicks.getCurrentValue() && PlayerUtils.movementInput()) {
                    rots[0] = mc.thePlayer.rotationYaw;
                    return;
                }
            }

            this.pos = this.getBlockPos();

            this.lastRots[0] = this.rots[0];
            this.lastRots[1] = this.rots[1];

            if (this.pos != null) {
                if (reachable) {
                    this.rots = getPlayerYawRotation();
                }
            }
        }
    }



    @EventTarget
    public void onUpdate(EventLivingUpdate e) {
            if (pos != null && skiptick.getCurrentValue()) {
                if (mc.thePlayer.motionY < -0.1) {
                    FallingPlayer fallingPlayer = new FallingPlayer(mc.thePlayer);
                    fallingPlayer.calculate(2);
                    if (pos.getY() > fallingPlayer.getY() + 0.5) {
                        reachable = false;
                    }
                }
                boolean needSkip = (!reachable);

                if (needSkip && rotateCount <= 8 && !BlinkFix.getInstance().getModuleManager().getModule(Blink.class).isEnabled()) {
                    RotationUtils.Rotation rotation = RotationUtils.getRotationBlock(pos, 0F);
                    mc.theWorld.skiptick++;
                    rotateCount++;
                    ChatUtils.addChatMessage(String.valueOf(rotateCount));
                    // if (rotateCount % 2 == 0) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(
                            new C03PacketPlayer.C05PacketPlayerLook(
                                    rotation.getYaw(), rotation.getPitch(), mc.thePlayer.onGround
                            )
                    );
                    // }
                    //RotationUtils.

//                    RotationManager.rotations.x = rotation.getYaw();
//                    RotationManager.rotations.y = rotation.getPitch();

                    placeBlock();
                    if (rotateCount < 8 && rotateCount % 2 != 0) {
                        mc.theWorld.skiptick++;
                    }
                    // if (mc.thePlayer.onGround)
                } else {
                    placeBlock();
                    if (mc.thePlayer.onGround) {
                        rotateCount = 0;
                        reachable = true;
                    }
                }
            } else {
                placeBlock();
            }
    }

    private Vec3 getVec3(PlaceData data) {
        BlockPos pos = data.blockPos;
        EnumFacing face = data.facing;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        x += face.getFrontOffsetX() / 2.0D;
        z += face.getFrontOffsetZ() / 2.0D;
        y += face.getFrontOffsetY() / 2.0D;

        return new Vec3(x, y, z);
    }
    public boolean isValidBlock(final BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        return !(block instanceof BlockLiquid) && !(block instanceof BlockAir) && !(block instanceof BlockChest) && !(block instanceof BlockFurnace) && !(block instanceof BlockEnderChest) && !(block instanceof BlockTallGrass) && !(block instanceof BlockSnow);
    }
    private boolean shouldDisable() {
        return mc.thePlayer != null && mc.thePlayer.ticksExisted > 20 && ServerUtils.getGrimTransactionCount() > 50 && !mc.thePlayer.isDead && !mc.thePlayer.isSpectator() && !mc.isSingleplayer() && (!isEnabled() || ticks < 30);
    }
    @EventTarget
    public void onClick(EventClick e) {
        e.setCancelled(true);
    }

    private int placeCount = 0;
    private void placeBlock() {
        if (this.pos != null) {
            MovingObjectPosition objectPosition = RayTraceUtils.rayCast(1, new float[]{RotationManager.rotations.x, RotationManager.rotations.y});
            ItemStack itemStack = this.getItemStack();

            if (itemStack != null && itemStack.stackSize > 0 && objectPosition != null && objectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (objectPosition.sideHit != EnumFacing.UP || mc.thePlayer.onGround || !PlayerUtils.movementInput() || isCurrentMode("Normal") || ((isCurrentMode("Expands") || isCurrentMode("Telly Bridge")) && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()))) {



                    if (objectPosition.getBlockPos().equals(this.pos) || (this.isNearbyBlockPos(objectPosition.getBlockPos()))) {
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) {
                            placeCount++;


                            if (swing.getCurrentValue()) {
                                mc.thePlayer.swingItem();
                                mc.getItemRenderer().resetEquippedProgress();
                            } else {
                                mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                            }
                        }
                    } else if (this.isNearbyBlockPos(objectPosition.getBlockPos()) && objectPosition.sideHit != EnumFacing.UP) {
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, objectPosition.getBlockPos(), objectPosition.sideHit, objectPosition.hitVec)) {
                            if (swing.getCurrentValue()) {
                                mc.thePlayer.swingItem();
                                mc.getItemRenderer().resetEquippedProgress();
                            } else {
                                mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                            }
                        }
                    }
                }
            }
        }
    }

    int ticks = 0;
    private float[] getPlayerYawRotation() {
        float yaw, pitch;
        float rotationYaw = mc.thePlayer.rotationYaw - 180.0F;

        if (this.isTower()) {
            MovingObjectPosition objectPosition = mc.objectMouseOver;
            if (objectPosition != null) {
                yaw = rotationYaw;
                pitch = 90;
                return new float[]{yaw, pitch};
            }
        }

        yaw = rotationYaw;
        if (sidewayRot.getCurrentValue()) {
                pitch = 87F;
        } else  {
            pitch = 82F;
        }
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
            if (sidewayRot.getCurrentValue()) {
                realYaw += 56.0F;
            }
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

            ArrayList<Float> validPitches = findValidPitches(yaw);
            if (!validPitches.isEmpty()) {
                validPitches.sort(Comparator.comparingDouble(this::distanceToLastPitch));
                rotations[1] = validPitches.get(0);
                return rotations;
            } else {
                return findOptimalRotation(yaw);
            }
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
        // Default return if no valid rotation found
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

        double dx = vec3.xCoord - mc.thePlayer.posX;
        double dy = vec3.yCoord - mc.thePlayer.posY;
        double dz = vec3.zCoord - mc.thePlayer.posZ;


        double distanceSq = dx * dx + dy * dy + dz * dz;
        return distanceSq <= 25.0 ? distanceSq : Double.MAX_VALUE;
    }

    private boolean isTower() {
        return mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindBack.isKeyDown() && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown();
    }
}
