package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.utils.ChatUtils;
import com.google.common.collect.Lists;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.cooldown.CooldownBar;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

import java.util.List;

@ModuleInfo(name = "AntiStaff", description = "Prevents staff from detecting you", category = Category.MISC)
public class AntiStaff extends Module {
    private final BooleanValue autoLeaveGame = ValueBuilder.create(this, "Auto Leave Game").setDefaultBooleanValue(true).build().getBooleanValue();
    private final BooleanValue keepArmor = ValueBuilder.create(this, "Keep Armor").setVisibility(autoLeaveGame::getCurrentValue).setDefaultBooleanValue(true).build().getBooleanValue();
    private final BooleanValue outputAllPlayers = ValueBuilder.create(this, "Output All Players").setDefaultBooleanValue(false).build().getBooleanValue();

    private final TimeHelper timer = new TimeHelper();
    private static final List<String> staff = Lists.newArrayList("weiler_", "小布丁qwq", "艾森啊", "gotnumb", "swemOG", "桃子OTM", "Wighterr", "China丶旭梦", "血樱丶星梦", "Toxic_AslGy", "仙阁灬特色", "TNT丶UFO", "小符xfu360", "xPir4te_", "落花榭",
            "Bir__yezi138", "欲生北茶丿年糕", "抖音搜MC小饭", "kklkll12","LaoZiKaiG_QS","mc加载", "CN_HYP_印花", "刀客塔", "CK_87", "Toxic_Yuuki", "MxyxPlays", "zoay", "抖音搜兴龙睡不着", "Cloudy_C", "Lucky陌北","gua_tai");

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getType() == EventType.RECEIVE && e.getPacket() instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem packet = (S38PacketPlayerListItem) e.getPacket();

            if (packet.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                for (S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                    if (data.getProfile() != null) {
                        String name = data.getProfile().getName();
                        if (outputAllPlayers.getCurrentValue()) {
                            ChatUtils.addChatMessage("§ePlayer: §a" + name + "join");
                        }
                        if (staff.contains(name) && timer.delay(1000)) {
                            BlinkFix.getInstance().getCooldownBarManager().addBar(new CooldownBar(5000, name + " is watching you!"));

                            if (autoLeaveGame.getCurrentValue()) {
                                if (keepArmor.getCurrentValue()) {
                                    mc.thePlayer.shiftClick(5);
                                    mc.thePlayer.shiftClick(6);
                                    mc.thePlayer.shiftClick(7);
                                    mc.thePlayer.shiftClick(8);
                                }

                                mc.thePlayer.sendChatMessage("/hub");
                            }
                            timer.reset();
                            break;
                        }
                    }
                }
            }
        }
    }
}
