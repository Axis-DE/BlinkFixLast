package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import com.google.common.collect.Lists;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.ArrayList;

@ModuleInfo(name = "XRay", description = "See ores through walls", category = Category.RENDER)
public class XRay extends Module {
    public static ArrayList<Integer> blockIdList = Lists.newArrayList(10, 11, 8, 9, 14, 15, 16, 21, 41, 42, 46, 48, 52, 56, 57, 61, 62, 73, 74, 84, 89, 103, 116, 117, 118, 120, 129, 133, 137, 145, 152, 153, 154);
    FloatValue alpha = ValueBuilder.create(this, "Alpha").setDefaultFloatValue(100).setFloatStep(1).setMinFloatValue(0f).setMaxFloatValue(255f).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getFloatValue();

    BooleanValue diamond = ValueBuilder.create(this, "Diamond").setDefaultBooleanValue(true).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getBooleanValue();
    BooleanValue emerald = ValueBuilder.create(this, "Emerald").setDefaultBooleanValue(true).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getBooleanValue();
    BooleanValue gold = ValueBuilder.create(this, "Gold").setDefaultBooleanValue(true).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getBooleanValue();
    BooleanValue lapis = ValueBuilder.create(this, "Lapis").setDefaultBooleanValue(true).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getBooleanValue();
    BooleanValue redStone = ValueBuilder.create(this, "Red Stone").setDefaultBooleanValue(true).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getBooleanValue();
    BooleanValue iron = ValueBuilder.create(this, "Iron").setDefaultBooleanValue(true).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getBooleanValue();
    BooleanValue coal = ValueBuilder.create(this, "Coal").setDefaultBooleanValue(true).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getBooleanValue();
    BooleanValue chest = ValueBuilder.create(this, "Chest").setDefaultBooleanValue(true).setOnUpdate(value -> mc.renderGlobal.loadRenderers()).build().getBooleanValue();

    public static int getAlpha() {
        return (int) ((XRay) BlinkFix.getInstance().getModuleManager().getModule(XRay.class)).alpha.getCurrentValue();
    }

    public static boolean isXrayEnabled() {
        return BlinkFix.getInstance().getModuleManager().getModule(XRay.class).isEnabled();
    }

    public boolean isValidBlock(Block block) {
        if (!diamond.getCurrentValue() && block == Blocks.diamond_ore) {
            return false;
        }

        if (!emerald.getCurrentValue() && block == Blocks.emerald_ore) {
            return false;
        }

        if (!gold.getCurrentValue() && block == Blocks.gold_ore) {
            return false;
        }

        if (!lapis.getCurrentValue() && block == Blocks.lapis_ore) {
            return false;
        }

        if (!redStone.getCurrentValue() && block == Blocks.redstone_ore) {
            return false;
        }

        if (!iron.getCurrentValue() && block == Blocks.iron_ore) {
            return false;
        }

        if (!coal.getCurrentValue() && block == Blocks.coal_ore) {
            return false;
        }

        if (!chest.getCurrentValue() && block == Blocks.chest) {
            return false;
        }

        return blockIdList.contains(Block.getIdFromBlock(block));
    }

    @Override
    public boolean onEnable() {
        mc.renderGlobal.loadRenderers();
        return false;
    }

    @Override
    public void onDisable() {
        mc.renderGlobal.loadRenderers();
    }
}
