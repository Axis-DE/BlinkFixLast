package moe.ichinomiya.naven.modules.impl.misc;

import lombok.Getter;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventRunTicks;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.ServerUtils;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.values.Value;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.ModeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@ModuleInfo(name = "Spammer", description = "Spam chat!", category = Category.MISC)
public class Spammer extends Module {
    Random random = new Random();
    ModeValue prefix = ValueBuilder.create(this, "Prefix").setDefaultModeIndex(0).setModes("None", "@").build().getModeValue();

    @Getter
    private final List<BooleanValue> values = new ArrayList<>();

    private final TimeHelper timer = new TimeHelper();

    @EventTarget
    public void onMotion(EventRunTicks e) {
        if (mc.thePlayer == null) return;
        if (timer.delay(5000) && ServerUtils.serverType != ServerUtils.ServerType.LOYISA_TEST_SERVER) {
            String prefix = this.prefix.isCurrentMode("None") ? "" : this.prefix.getCurrentMode();

            List<String> styles = values.stream().filter(BooleanValue::getCurrentValue).map(Value::getName).collect(Collectors.toList());

            if (styles.isEmpty()) {
                return;
            }

            String style = styles.get(random.nextInt(styles.size()));
            String message = prefix + style;
            mc.thePlayer.sendChatMessage(message);
            timer.reset();
        }
    }
}
