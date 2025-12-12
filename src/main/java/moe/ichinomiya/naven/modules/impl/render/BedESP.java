package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.Colors;
import moe.ichinomiya.naven.utils.Render3DUtils;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "BedESP", description = "Highlights beds", category = Category.RENDER)
public class BedESP extends Module {
    private final static int bedColor = new Color(255, 0, 0, 100).getRGB();
    private final List<BlockPos> bedPositions = new CopyOnWriteArrayList<>();
    BooleanValue blocks = ValueBuilder.create(this, "Display Blocks").setDefaultBooleanValue(true).build().getBooleanValue();
    ConcurrentHashMap<BlockPos, double[]> blockPositions = new ConcurrentHashMap<>();
    ScaledResolution scaledRes;

    @Override
    public boolean onEnable() {
        mc.renderGlobal.loadRenderers();
        bedPositions.clear();
        return false;
    }

    @Override
    public boolean onDisable() {
        mc.renderGlobal.loadRenderers();
        return false;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        bedPositions.clear();
    }

    @EventTarget
    public void onBlockRender(EventRenderBlock e) {
        if (e.getBlock() == Blocks.bed) {
            BlockPos blockPos = new BlockPos(e.getBlockPos().getX(), e.getBlockPos().getY(), e.getBlockPos().getZ());
            if (!bedPositions.contains(blockPos)) {
                bedPositions.add(blockPos);
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.POST) {
            bedPositions.removeIf(blockPos -> mc.theWorld.getBlockState(blockPos).getBlock() != Blocks.bed);
        }
    }

    @EventTarget
    public void onRender(EventRender e) {
        for (BlockPos position : bedPositions) {
            Render3DUtils.drawSolidBlockESP(position, bedColor, 0.56f);
        }
    }

    private void updatePositions() {
        blockPositions.clear();

        for (BlockPos bed : bedPositions) {
            processBlockPos(bed, 1, 0, 0);
            processBlockPos(bed, -1, 0, 0);
            processBlockPos(bed, 0, 0, 1);
            processBlockPos(bed, 0, 0, -1);
            processBlockPos(bed, 0, 1, 0);
        }
    }

    private void processBlockPos(BlockPos bed, int x, int y, int z) {
        BlockPos pos = new BlockPos(bed.getX() + x, bed.getY() + y, bed.getZ() + z);

        Block block = mc.theWorld.getBlockState(pos).getBlock();

        if (block == Blocks.air || block == Blocks.bed) {
            return;
        }

        double[] convertTo2D = RenderUtils.convertTo2D(pos.getX() - mc.getRenderManager().renderPosX + 0.5, pos.getY() - mc.getRenderManager().renderPosY + 0.5, pos.getZ() - mc.getRenderManager().renderPosZ + 0.5);

        if (convertTo2D != null) {
            if ((convertTo2D[2] >= 0.0D) && (convertTo2D[2] < 1.0D)) {
                blockPositions.put(pos, convertTo2D);
            }
        }
    }

    @EventTarget
    public void update(EventRender event) {
        if (!blocks.getCurrentValue()) {
            return;
        }

        try {
            updatePositions();
        } catch (Exception ignored) {
        }
    }

    @EventTarget
    public void onShader(EventShader e) {
        if (!blocks.getCurrentValue()) {
            return;
        }

        if (scaledRes != null) {
            render(true);
        }
    }

    @EventTarget
    public void on2DRender(EventRender2D e) {
        if (!blocks.getCurrentValue()) {
            return;
        }

        scaledRes = e.getResolution();
        render(false);
    }

    public void render(boolean shader) {
        for (BlockPos position : blockPositions.keySet()) {
            if (position != null) {
                double[] renderPositions = blockPositions.get(position);
                if ((renderPositions[2] < 0.0D) || (renderPositions[2] >= 1.0D)) {
                    continue;
                }
                GlStateManager.pushMatrix();
                try {
                    GlStateManager.translate(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), 0.0D);

                    GlStateManager.scale(0.75, 0.75, 1);
                    float allWidth = 20;

                    if (shader) {
                        RenderUtils.drawRect(-allWidth / 2, -20.0f, allWidth / 2, 0, 0xFFFFFFFF);
                    } else {
                        RenderUtils.drawRect(-allWidth / 2, -20.0f, allWidth / 2, 0, Colors.getColor(0, 0, 0, 40));
                        ItemStack stack = new ItemStack(mc.theWorld.getBlockState(position).getBlock());

                        if (stack.getItem() != null) {
                            // Draw Item Icon
                            GlStateManager.pushMatrix();
                            RenderHelper.enableGUIStandardItemLighting();
                            GlStateManager.disableAlpha();
                            GlStateManager.clear(256);
                            mc.getRenderItem().zLevel = -150.0F;

                            GlStateManager.disableLighting();
                            GlStateManager.disableDepth();
                            GlStateManager.disableBlend();
                            GlStateManager.enableLighting();
                            GlStateManager.enableDepth();
                            GlStateManager.disableLighting();
                            GlStateManager.disableDepth();
                            GlStateManager.disableTexture2D();
                            GlStateManager.disableAlpha();
                            GlStateManager.disableBlend();
                            GlStateManager.enableBlend();
                            GlStateManager.enableAlpha();
                            GlStateManager.enableTexture2D();
                            GlStateManager.enableLighting();
                            GlStateManager.enableDepth();

                            mc.getRenderItem().renderItemIntoGUI(stack, -8, -18);
                            mc.getRenderItem().zLevel = 0.0F;
                            GlStateManager.enableAlpha();
                            RenderHelper.disableStandardItemLighting();
                            GlStateManager.popMatrix();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                GlStateManager.popMatrix();
            }
        }
    }
}
