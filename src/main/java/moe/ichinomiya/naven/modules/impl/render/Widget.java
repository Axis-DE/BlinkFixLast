package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.ui.widgets.*;
import com.google.common.collect.Lists;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.events.impl.EventRender2D;
import moe.ichinomiya.naven.events.impl.EventShader;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.widgets.*;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

@ModuleInfo(name = "Widget", description = "Widgets", category = Category.RENDER)
public class Widget extends Module {
    BooleanValue items = ValueBuilder.create(this, "Items").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue players = ValueBuilder.create(this, "Players").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue blockRate = ValueBuilder.create(this, "Block Rate").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue targetHud = ValueBuilder.create(this, "Target HUD").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue rearView = ValueBuilder.create(this, "Rear View").setDefaultBooleanValue(false).build().getBooleanValue();
//    BooleanValue DSJmedia = ValueBuilder.create(this, "Media").setDefaultBooleanValue(false).build().getBooleanValue();


    public List<DraggableWidget> widgets = Lists.newArrayList(new ItemWidget(items), new PlayerWidget(players), new BlockRateWidget(blockRate), new TargetHUDWidget(targetHud), new RearviewWidget(rearView));

    public Widget() {
        super();

        for (DraggableWidget widget : widgets) {
            BlinkFix.getInstance().getEventManager().register(widget);
        }
    }

    @EventTarget(Priority.LOW)
    public void onRender(EventRender2D e) {
        for (DraggableWidget widget : widgets) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(widget.getX().value, widget.getY().value, 0);
            widget.render();
            GlStateManager.popMatrix();
        }
    }

    @EventTarget
    public void onShader(EventShader e) {
        for (DraggableWidget widget : widgets) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(widget.getX().value, widget.getY().value, 0);
            widget.renderShader();
            GlStateManager.popMatrix();
        }
    }

    public void processDrag(int mouseX, int mouseY, int mouseButton) {
        widgets.forEach(widget -> widget.processDrag(mouseX, mouseY, mouseButton));
    }
}
