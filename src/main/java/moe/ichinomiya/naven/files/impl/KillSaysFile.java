package moe.ichinomiya.naven.files.impl;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.files.ClientFile;
import moe.ichinomiya.naven.modules.impl.misc.KillSay;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class KillSaysFile extends ClientFile {
    private static final String[] styles = {"%s Get Good Get BlinkFix Client BlinkFix.Today!",
            "%s 你已被Naven 客户端击毙，BlinkFix.Today",
            "%s L",
            "%s fw",
            "%s 我喜欢你",
            "%s 我喜欢你♥",
            "%s 兄弟你好香",
            "%s 可以和我交往吗？",
            "%s 你好可爱",
            "%s 别急",
            "%s 兄弟我是曹源#659001201005183237我觉得你漏防了",
            "%s 兄弟我是曹新卫#659001197308213211我觉得你漏防了",
            "%s 兄弟我是梁晶（死亡）#65400119751202072X我觉得你漏防了",
            "%s 兄弟我是曹诚钦#659001199912243213我觉得你漏防了",
            "%s 你这个客户端给我曹源帝看笑了，什么客户端也敢跟你曹源帝一战？",
            "%s 我曹源最擅长诈骗背刺拉黑意淫，你能把我怎么样？",
            "%s 我曹源最爱在互联网上碰瓷，我天下无敌不服来战！",
            "曹源曹新卫曹诚钦梁晶全都死了，%s 我们一起为一家子棉花默哀吧。"
//            "%s 你已被清朝杀手陈安健害死！快使用Hack Lunar！",
//            "%s 你已被狂笑的蛇陈安健本人害死，快使用我编写的Hack Lunar端！"
    };

    public KillSaysFile() {
        super("killsays.cfg");
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        KillSay module = (KillSay) BlinkFix.getInstance().getModuleManager().getModule(KillSay.class);
        List<BooleanValue> values = module.getValues();

        String line;
        while ((line = reader.readLine()) != null) {
            values.add(ValueBuilder.create(module, line).setDefaultBooleanValue(false).build().getBooleanValue());
        }

        if (values.isEmpty()) {
            for (String style : styles) {
                values.add(ValueBuilder.create(module, style).setDefaultBooleanValue(false).build().getBooleanValue());
            }
        }
    }

    @Override
    public void save(BufferedWriter writer) throws IOException {
        KillSay module = (KillSay) BlinkFix.getInstance().getModuleManager().getModule(KillSay.class);
        List<BooleanValue> values = module.getValues();

        for (BooleanValue value : values) {
            writer.write(value.getName() + "\n");
        }
    }
}
