package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ichinomiya.naven.events.api.events.Event;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

@Getter
@AllArgsConstructor
public class EventRenderBlock implements Event {
    private final Block block;
    private final BlockPos blockPos;
}