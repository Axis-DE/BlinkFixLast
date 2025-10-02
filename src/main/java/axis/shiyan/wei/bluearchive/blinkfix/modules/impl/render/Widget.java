package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.ui.widgets.*;
import com.google.common.collect.Lists;
import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.Priority;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender2D;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventShader;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
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
