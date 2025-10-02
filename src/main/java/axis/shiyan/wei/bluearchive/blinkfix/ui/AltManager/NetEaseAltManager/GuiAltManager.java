package axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.NetEaseAltManager;

import axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.NeteaseAccount;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.receive.GameCharactersResponse;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.util.EnumChatFormatting;
import org.bytedeco.javacv.FrameGrabber;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static axis.shiyan.wei.bluearchive.blinkfix.BlinkFix.videoPlayer;

public final class GuiAltManager
extends GuiScreen {

    private static final String[] NAME_ARRAY_1 = {
            "云逸尘", "青璇月", "墨离殇", "寒江雪", "夜溟风", "凌霜华", "萧暮雨", "慕容雪", "冷千羽", "南宫月",
            "风无痕", "白若璃", "慕清歌", "苏浅夏", "叶倾歌", "洛清幽", "花镜辞", "离鸢夜", "萧疏影", "月清秋",
            "玄冥夜", "墨倾池", "楚流云", "冷青锋", "夜阑珊", "凤栖梧", "玉惊鸿", "雪轻尘", "夜归人", "萧寒舟",
            "云栖鹤", "月扶摇", "凌千朔", "江暮烟", "萧听雨", "玉临风", "寒惊秋", "慕凝霜", "叶孤城", "冷画屏",
            "夜沉璧", "墨流觞", "洛长空", "萧别离", "月胧明", "楚云深", "凌九霄", "白夜行", "凤君离", "苏枕河",
            "玉微凉", "萧断琴", "花满楼", "夜无寐", "云遮月", "冷千绝", "慕天光", "叶寒衣", "江浸月", "离恨天",
            "墨飞星", "凌寒彻", "萧陌然", "月独酌", "楚南风", "玉生烟", "凤栖霞", "夜未央", "云破晓", "冷星魂",
            "慕沧溟", "叶知秋", "洛红衣", "萧残阳", "月清绝", "寒江夜", "墨千机", "凌飞羽", "白无瑕", "夜焚香",
            "花解语", "云隐舟", "玉碎空", "萧暮寒", "凤求凰", "冷千回", "楚暮雪", "月倾城", "洛惊鸿", "夜阑星",
            "墨无锋", "凌霜降", "萧夜白", "玉沉渊", "寒烟翠", "慕云舒", "叶凌霄", "江逐月", "离人泪", "夜听风",
            "白清欢", "凤栖云", "云断崖", "墨染衣", "冷孤烟", "萧易水", "月华浓", "楚江寒", "洛天依", "夜流火",
            "玉惊弦", "凌寒影", "花非雾", "萧寒衣", "墨千夜", "冷千秋", "月沉沙", "叶寒川", "慕沧海", "夜无痕"
    };

    private static final String[] NAME_ARRAY_2 = {
            "镜琉璃", "夜忘川", "玉绯烟", "萧瑟秋", "凤栖夜", "冷残月", "云断鸿", "墨青瓷", "凌寒江", "楚空山",
            "月焚香", "洛惊羽", "夜凝霜", "白疏狂", "花醉墨", "萧沉舟", "玉碎心", "寒千夜", "慕流云", "叶焚天",
            "江寒彻", "离人醉", "云织梦", "墨吟风", "夜观澜", "凌孤鸿", "凤栖梧", "冷秋河", "萧暮云", "月笼纱",
            "玉碎寒", "楚霜降", "夜无眠", "墨惊鹊", "凌九幽", "花弄影", "萧千夜", "寒江吟", "慕晚风", "叶沉舟",
            "月落尘", "洛飞星", "云破月", "冷画魂", "玉断弦", "夜归舟", "墨离歌", "凌寒衣", "凤栖霞", "萧千绝",
            "花葬魂", "楚夜阑", "月沉渊", "冷青衫", "玉生寒", "夜听雪", "墨流萤", "凌断云", "白惊鸿", "萧暮烟",
            "寒千影", "凤栖宫", "云遮天", "玉碎冰", "夜焚霜", "楚江离", "月无缺", "冷千山", "墨飞烟", "凌寒刃",
            "花残血", "萧断水", "夜孤城", "玉寒秋", "慕云归", "叶惊棠", "江逐星", "离恨水", "月沉江", "冷千尘",
            "墨吟秋", "凌霜白", "萧夜阑", "玉碎霜", "寒惊鸿", "凤栖木", "云断岳", "夜无欢", "楚空城", "月临江",
            "冷画秋", "墨流冰", "凌寒星", "花醉月", "萧沉渊", "玉碎风", "夜观星", "寒千雪", "慕归尘", "叶焚霜",
            "江寒月", "离人怨", "云织锦", "墨惊云", "夜凝碧", "凌孤雁", "凤栖梧", "冷秋心", "萧暮雪", "月笼烟",
            "玉碎影", "楚霜白", "夜无垠", "墨惊羽", "凌九霄", "花弄玉", "萧千殇", "寒江雪", "慕晚秋", "叶沉沙",
            "月落霜", "洛飞烟", "云破空", "冷画秋", "玉断魂", "夜归人", "墨离殇", "凌寒月", "凤栖梧", "萧千夜"
    };

    private static final String[] NAME_ARRAY_3 = {
            "墨羽", "青璃", "寒星", "千夜", "流云", "飞霜", "惊鸿", "孤月", "沧溟", "离殇",
            "忘川", "绯烟", "沉渊", "听雨", "无痕", "落尘", "凌烟", "九霄", "断弦", "焚天",
            "暮雪", "玄夜", "疏影", "绝尘", "紫陌", "清歌", "凌霄", "幽夜", "浮生", "无欢",
            "残月", "断风", "冰魄", "冥夜", "空城", "长歌", "流火", "凝碧", "霜华", "千绝",
            "夜阑", "苍羽", "碎星", "离恨", "天光", "孤鸿", "烬灭", "云崖", "冷月", "惊蝉",
            "葬花", "空山", "幽篁", "寂雪", "鬼泣", "龙吟", "凤血", "青锋", "黄泉", "赤霄",
            "白夜", "紫煞", "墨渊", "冰河", "雷怒", "风寂", "雪魄", "霜刃", "血薇", "星陨",
            "绝情", "断念", "焚寂", "魅影", "幻音", "破军", "修罗", "罗刹", "天煞", "贪狼",
            "七杀", "无相", "忘忧", "彼岸", "轮回", "往生", "红莲", "碧落", "黄泉", "青冥",
            "玄冰", "赤炎", "紫电", "惊雷", "流风", "飞雪", "暗香", "孤舟", "残阳", "破晓",
            "苍炎", "冰封", "鬼刃", "魔瞳", "邪影", "妖月", "血衣", "骨泣", "魂灭", "魄散",
            "幽泉", "冥火", "尸语", "蛊心", "毒煞", "阴风", "蚀骨", "腐萤", "魇梦", "咒怨"
    };

    public static final List<ProxyServer> proxyServers = new ArrayList<>();
    public static ProxyServer currentProxyServer = null;

    private static final String HytServerId = "77114517833647104";

    private final GuiScreen parentScreen;
    private GuiButton buttonLogin;
    private GuiButton buttonRemove;
    private GuiButton buttonAddCharacter;
    private GuiButton buttonAddRandomCharacter;
    private GuiButton buttonStartProxyServer;

    private String status = EnumChatFormatting.YELLOW + "请先选择账号";
    private String addStatus = "";

    private NeteaseAccount selectAlt;
    private double offset = 0.0;

    private List<GameCharactersResponse.Character> characters = new ArrayList<>();
    private GameCharactersResponse.Character selectedCharacter;
    private double characterOffset = 0.0;

    public GuiAltManager(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    private void updateCharacters() {
        if (selectAlt != null && selectAlt.isLogged()) {
            var response = selectAlt.queryServerCharacters(HytServerId, 2);
            if (response != null && response.entities != null) {
                characters = response.entities;
            } else {
                characters = new ArrayList<>();
            }
        } else {
            characters = new ArrayList<>();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final double halfWidth = width / 2.0;
        try {
            videoPlayer.render(0, 0, width, height);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

        //Back
//        RoundedUtils.drawGradientRoundLR(0.0f, 0.0f, width, 46.0f, 0, new Color(0, 0, 0, 90), new Color(40, 40, 40, 90));
//        RoundedUtils.drawGradientRoundLR(0.0f, height - 46.0f, width, 46.0f, 0, new Color(0, 0, 0, 90), new Color(40, 40, 40, 90));

        mc.fontRendererObj.drawCenteredString(status, (float) halfWidth, (float) (23.0 - mc.fontRendererObj.FONT_HEIGHT), -1);
        mc.fontRendererObj.drawCenteredString(addStatus, (float) halfWidth, 23.0F, -1);
        mc.fontRendererObj.drawString("Name: " + mc.session.getUsername(), 10, 23 - mc.fontRendererObj.FONT_HEIGHT, -1);

        super.drawScreen(mouseX, mouseY, partialTicks);

        final boolean mouseInScreen = mouseY >= 46.0 && mouseY <= height - 46.0;

//        RenderUtil.startGlScissor(2, 52, width - 4, height - 98);

        int accountIndex = 0;
        for (NeteaseAccount alt : AltManager.Instance.getAltList()) {
            final double accountY = 52.0 + accountIndex * 27.0 - offset;
            if (accountY < 0.0 || accountY > height) {
                accountIndex++;
                continue;
            }

            final double textX = halfWidth / 2.0 - 4.0;

//            RenderUtil.drawRect(2.0, accountY, halfWidth, accountY + 25.0, ColorUtils.getRGB(0, 0, 0, 100));
            mc.fontRendererObj.drawCenteredString("UserId: " + alt.getEntityId(), (float) textX, (float) (accountY + 4.0), -1);
            mc.fontRendererObj.drawCenteredString(EnumChatFormatting.YELLOW + "Netease", (float) textX, (float) (accountY + 15.0), -1);

            if (selectAlt == alt) {
//                RenderUtil.drawRect(halfWidth - 2, accountY, halfWidth,accountY + 25.0, -1);
            }

            if (mouseInScreen && isHovered(2.0, accountY, halfWidth, accountY + 25.0, mouseX, mouseY)) {
//                RenderUtil.drawRect(2.0, accountY, halfWidth,accountY + 25.0, ColorUtils.getRGB(0,0,0,50));

                if (selectAlt != alt && Mouse.isButtonDown(0)) {
                    selectAlt = alt;
                    selectedCharacter = null;
                    characterOffset = 0.0;
                    characters = new ArrayList<>();
                    updateCharacters();
                }
            }

            accountIndex++;
        }

//        RenderUtil.stopGlScissor();

        final double characterX = halfWidth + 4;

        if (selectAlt != null ) {
            final NeteaseAccount Alt = selectAlt;

            if (characters != null && !characters.isEmpty()) {
//                RenderUtil.startGlScissor(width / 2, 52, width, height - 98);

                int characterIndex = 0;
                for (GameCharactersResponse.Character character : characters) {
                    final double characterY = 52.0 + characterIndex * 27.0 - characterOffset;
                    if (characterY < 0.0 || characterY > height) {
                        characterIndex++;
                        continue;
                    }

                    final double textX = characterX + characterX / 2.0 - 4.0;

//                    RenderUtil.drawRect(characterX, characterY, width, characterY + 25.0, ColorUtils.getRGB(0, 0, 0, 100));
                    mc.fontRendererObj.drawCenteredString("角色名: " + EnumChatFormatting.YELLOW + character.name, (float) textX, (float) (characterY + 4.0), -1);

                    if (Objects.equals(selectedCharacter, character)) {
//                        RenderUtil.drawRect(width - 2.0, characterY, width,characterY + 25.0, -1);
                    }

                    if (mouseInScreen && isHovered(characterX, characterY, width, characterY + 25.0, mouseX, mouseY)) {
//                        RenderUtil.drawRect(characterX, characterY, width,characterY + 25.0, ColorUtils.getRGB(0,0,0,50));

                        if (!Objects.equals(selectedCharacter, character) && Mouse.isButtonDown(0)) {
                            selectedCharacter = character;
                        }
                    }

                    characterIndex++;
                }

//                RenderUtil.stopGlScissor();
            }
        }

        final boolean isMouseInAccountArea = isHovered(2.0, 46.0, halfWidth, height - 46.0, mouseX, mouseY);
        final boolean isMouseInCharacterArea = selectedCharacter != null && isHovered(characterX, 46.0, width, height - 46.0, mouseX, mouseY);

        if (Mouse.hasWheel()) {
            final int wheel = Mouse.getDWheel();

            if (wheel != 0) {
                if (isMouseInAccountArea) {
                    offset += wheel < 0 ? 30.0 : -30.0;
                } else if (isMouseInCharacterArea) {
                    characterOffset += wheel < 0 ? 30.0 : -30.0;
                }
            }
        }

        final double offsetMax = (AltManager.Instance.getAltList().size() * 27.0) - (height - 92.0);

        if (offset < 0.0) {
            offset = 0.0;
        } else if (offset > offsetMax) {
            offset = Math.max(0.0, offsetMax);
        }

        if (selectAlt != null) {
            final double characterOffsetMax = (characters.size() * 27.0) - (height - 92.0);

            if (characterOffset < 0.0) {
                characterOffset = 0.0;
            } else if (characterOffset > characterOffsetMax) {
                characterOffset = Math.max(0.0, characterOffsetMax);
            }
        }

        if (selectAlt == null){
            buttonRemove.enabled = false;
            buttonLogin.enabled = false;
            buttonAddCharacter.enabled = false;
            buttonAddRandomCharacter.enabled = false;
            buttonStartProxyServer.enabled = false;
            status = EnumChatFormatting.YELLOW + "请先选择账号";
        }else{
            buttonRemove.enabled = true;
            var isLogged = selectAlt.isLogged();
            if(isLogged){
                status = EnumChatFormatting.GREEN + "UserId: " + selectAlt.getEntityId();
            }else{
                status = EnumChatFormatting.YELLOW + "请先登录";
            }
            buttonLogin.enabled = !isLogged;
            buttonAddCharacter.enabled = isLogged;
            buttonAddRandomCharacter.enabled = isLogged;
            buttonStartProxyServer.enabled = isLogged && selectedCharacter != null;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) { // 返回
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 1) { // 登录
            if (selectAlt != null) {
                try {
                    selectAlt.login();
                }catch (Exception e){
                }
                if (selectAlt.isLogged()){
                    updateCharacters();
                    status = EnumChatFormatting.GREEN + "登录成功";
                }else{
                    status = EnumChatFormatting.RED + "登录失败，可能是Cookies结构不正确，登录过于频繁，账号无效或已被封禁导致的";
                }
            }
        }
        else if (button.id == 2) { // 删除
            if (selectAlt != null) {
                AltManager.Instance.getAltList().remove(selectAlt);
                selectAlt = null;
            }
        }
        else if (button.id == 3) { // 添加
            mc.displayGuiScreen(new GuiAltLogin(this) {
                @Override
                public void onLogin(String cookies) {
                    NeteaseAccount account = null;
                    try {
                        account = new NeteaseAccount(cookies);
                    }catch (Exception e){
                        status = EnumChatFormatting.RED + "添加失败，可能是Cookies结构不正确导致的";
                        return;
                    }
                    try {
                        account.login();
                    }catch (Exception e){
                    }
                    if (account.isLogged()){

                        for (NeteaseAccount alt : AltManager.Instance.getAltList()) {
                            if (alt.getEntityId().equals(account.getEntityId())) {
                                status = EnumChatFormatting.RED + "添加失败，该账号已存在";
                                return;
                            }
                        }
                        AltManager.Instance.getAltList().add(account);
                        status = EnumChatFormatting.GREEN + "添加成功";
                    }else{
                        status = EnumChatFormatting.RED + "添加失败，可能是Cookies结构不正确，登录过于频繁，账号无效或已被封禁导致的";
                    }
                }
            });
        }
        else if (button.id == 4) { // 添加随机角色
            var characterName = NAME_ARRAY_1[ThreadLocalRandom.current().nextInt(0, NAME_ARRAY_1.length)]
                    + NAME_ARRAY_2[ThreadLocalRandom.current().nextInt(0, NAME_ARRAY_2.length)]
                    + NAME_ARRAY_3[ThreadLocalRandom.current().nextInt(0, NAME_ARRAY_3.length)];

            selectAlt.createGameCharacter(HytServerId, characterName, 2);
            updateCharacters();
        }
        else if (button.id == 5) { // 添加角色
            mc.displayGuiScreen(new GuiAltLogin(this) {
                @Override
                public void onLogin(String username) {
                    status = selectAlt.createGameCharacter(HytServerId,username,2).message;
                    if (Objects.equals(status, "正常返回"))
                        status = EnumChatFormatting.GREEN + "添加成功";
                    else
                        status = EnumChatFormatting.RED + status;
                    updateCharacters();
                }
            });
        }
        else if (button.id == 6) { // 启动代理服务
            var roleName = selectedCharacter.name;
            var serverAddressObject = selectAlt.queryServerAddress(HytServerId).data;
            var serverAddress = serverAddressObject.ip+":"+serverAddressObject.port;
            proxyServers.clear();
            proxyServers.add(new ProxyServer("§a§lNaven§r §7§k §r §e§o" + roleName + " §7§k §r §c§o" + serverAddress,
                    serverAddress,
                    roleName,
                    selectAlt,
                    HytServerId,
                    "1.12.2",
                    "{\"mods\":[{\"modPath\":\"4633394671597683929@3@15\",\"id\":\"4633394671597683929@3@15\",\"iid\":\"4633394671597683929\",\"name\":\"\",\"md5\":\"517F47A95E6F5AB2497C03A689BC4BAE\"},{\"modPath\":\"4620273834451558259@3@15\",\"id\":\"4620273834451558259@3@15\",\"iid\":\"4620273834451558259\",\"name\":\"\",\"md5\":\"16F1B443D0731C9A28C39DEF3EECCB87\"},{\"modPath\":\"4621632218832071536@3@15\",\"id\":\"4621632218832071536@3@15\",\"iid\":\"4621632218832071536\",\"name\":\"\",\"md5\":\"19324A1CB2BBE94CF422E225B14B6BFF\"},{\"modPath\":\"4620273834395780067@3@15\",\"id\":\"4620273834395780067@3@15\",\"iid\":\"4620273834395780067\",\"name\":\"\",\"md5\":\"8F689D988D3E12BDF483122217AB07FF\"},{\"modPath\":\"4680625170468362466@3@15\",\"id\":\"4680625170468362466@3@15\",\"iid\":\"4680625170468362466\",\"name\":\"\",\"md5\":\"7F35FC99901639BCB5D3DCCD032483AC\"},{\"modPath\":\"4620273834210283309@3@15\",\"id\":\"4620273834210283309@3@15\",\"iid\":\"4620273834210283309\",\"name\":\"\",\"md5\":\"31E33BDA2DA5782E213C261C4F7F67C6\"},{\"modPath\":\"4620702976810361335@3@15\",\"id\":\"4620702976810361335@3@15\",\"iid\":\"4620702976810361335\",\"name\":\"\",\"md5\":\"6A06C67C0C99EC30995BA040A454D5D4\"},{\"modPath\":\"4624104029891423116@3@15\",\"id\":\"4624104029891423116@3@15\",\"iid\":\"4624104029891423116\",\"name\":\"\",\"md5\":\"B7FE765E2EF1601A6B62D057C3539388\"},{\"modPath\":\"4663899962476874551@3@15\",\"id\":\"4663899962476874551@3@15\",\"iid\":\"4663899962476874551\",\"name\":\"\",\"md5\":\"73C57DD7F8647B9B0C477C51D4824445\"},{\"modPath\":\"4640208094372507127@3@15\",\"id\":\"4640208094372507127@3@15\",\"iid\":\"4640208094372507127\",\"name\":\"\",\"md5\":\"72167D680F4ED888028691046F052510\"}]}"
            ));

            addStatus = "成功添加代理服务";
        }
//        else if (button.id == 7) {
//            try {
//                Desktop.getDesktop().browse(URI.create("https://heshuyou.xyz/"));
//            } catch (Throwable e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(0, 2, height - 22, 50, 20, "返回"));
        this.buttonList.add(this.buttonLogin = new GuiButton(1, 2, height - 44, 50, 20, "登录"));
        this.buttonList.add(this.buttonRemove = new GuiButton(2, 54, height - 44, 50, 20, "删除"));
        this.buttonList.add(new GuiButton(3, 54, height - 22, 50, 20, "添加"));
        this.buttonList.add(this.buttonAddRandomCharacter = new GuiButton(4, width - 72, height - 44, 70, 20, "随机角色"));
        this.buttonAddCharacter = new GuiButton(5, width - 124, height - 44, 50, 20, "自定义角色");
        this.buttonList.add(this.buttonStartProxyServer = new GuiButton(6, width - 72, height - 22, 70, 20, "§e启动花雨庭"));
//        this.buttonList.add(new GuiButton(7, 106, height - 22, 50, 20, "§e跳转官网"));

//        if (IRCClient.Instance.neteaseAuthFree) {
//            if (IRCClient.Instance.ircUser.level.isHigherOrSame(IRCUserLevel.PAID)) {
                this.buttonList.add(this.buttonAddCharacter);
//            }
//        } else if (IRCClient.Instance.ircUser.level != IRCUserLevel.FREE) {
//            this.buttonList.add(this.buttonAddCharacter);
//        }

        super.initGui();
    }

    private static boolean isHovered(double x, double y, double x2, double y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    public static class ProxyServer extends LanServerDetector.LanServer {
        public final NeteaseAccount neteaseAccount;
        public final String serverId;
        public final String serverVersion;
        public final String modHashList;
        public final String roleName;
        public ProxyServer(String motd, String address, String roleName,NeteaseAccount neteaseAccount, String serverId, String serverVersion, String modHashList) {
            super(motd, address);
            this.neteaseAccount = neteaseAccount;
            this.serverId = serverId;
            this.serverVersion = serverVersion;
            this.modHashList = modHashList;
            this.roleName = roleName;
        }
    }
}

