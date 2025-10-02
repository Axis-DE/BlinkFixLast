package axis.shiyan.wei.bluearchive.blinkfix.files.impl;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.files.ClientFile;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.Spammer;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class SpammerFile extends ClientFile {
    private static final String[] styles = {"大家好我是新疆爱摘棉花的曹源，659001201005183237是我的身份证！",
            "大家好我是曹源亲哥曹诚钦，我的弟弟曹源喜欢在网上耀武扬威！659001199912243213是我的身份证！",
            "大家好我是曹源亲爹曹新卫，659001197308213211是我的身份证，我宣布人人都是我曹新卫！",
            "大家好我是曹源亲妈梁晶（死亡版），65400119751202072X是我的身份证！",
            "BlinkFix.Today Get BlinkFix The Best!",
            "曹源曹新卫曹诚钦梁晶全都死了，让我们大家为一家子棉花默哀。",
            "我曹源最爱在互联网上碰瓷，我天下无敌不服来战！",
            "你这个客户端给我曹源帝看笑了，什么客户端也敢跟你曹源帝战？",
            "我曹源最爱摘棉花了，新疆人都是神，大家一起来摘棉花！",
            "我曹源觉得你漏防了！",
            "我曹新卫觉得你漏防了！",
            "我梁晶觉得你漏防了！",
            "我曹诚钦觉得你漏防了！",
            "我曹源最擅长诈骗背刺拉黑意淫，你能把我怎么样？",
            "欢迎大家光临我曹家的佳美超市！"

//            "我是狂笑的蛇陈安健，我是花雨庭反作弊兼Hack Lunar端制作者！",
//            "我是狂笑的蛇陈安健，我使用Hack Lunar不会Ban！因为我制作了花雨庭反作弊！",
//            "你知道吗，我是狂笑的蛇陈安健！我制作了花雨庭反作弊，误封你们！",
//            "不如我狂笑的蛇将写散文制作的Hack Lunar，我很稳定因为花雨庭反作弊就是出自本人之手！",
//            "兄弟，你听我说！我不会被封，因为我是狂笑的蛇，我制作了花雨庭反作弊！",
//            "我是狂笑的蛇陈安健，我疯狂巴结花雨庭的技术！",
//            "陈安健制作的Hack Lunar依然稳定绕过花雨庭，因为我是狂笑的蛇！",
//            "我是大名鼎鼎的狂笑的蛇将写散文本人陈安健，我能无视反作弊飘主播因为我制作了花雨庭反作弊！",
//            "羡慕我不会被封吗？因为我狂笑的蛇本人陈安健制作了花雨庭的反作弊！",
//            "B站搜索狂笑的蛇将写散文，看我陈安健精彩集锦！",
//            "我狂笑的蛇陈安健能无视反作弊，因为花雨庭反作弊就是我做的！",
//            "哈哈你知道你为什么会被误封吗？因为我狂笑的蛇陈安健制作了花雨庭反作弊！"
    };

    public SpammerFile() {
        super("spammers.cfg");
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        Spammer module = (Spammer) BlinkFix.getInstance().getModuleManager().getModule(Spammer.class);
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
        Spammer module = (Spammer) BlinkFix.getInstance().getModuleManager().getModule(Spammer.class);
        List<BooleanValue> values = module.getValues();

        for (BooleanValue value : values) {
            writer.write(value.getName() + "\n");
        }
    }
}
