package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

@Getter
@Setter
@AllArgsConstructor
public class EventBondingBoxSet extends EventCancellable {
    private Block block;
    private BlockPos pos;
    private AxisAlignedBB boundingBox;
}
