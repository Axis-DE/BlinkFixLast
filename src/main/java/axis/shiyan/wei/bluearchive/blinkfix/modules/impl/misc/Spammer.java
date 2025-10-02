package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRunTicks;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.ServerUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import axis.shiyan.wei.bluearchive.blinkfix.values.Value;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;

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
