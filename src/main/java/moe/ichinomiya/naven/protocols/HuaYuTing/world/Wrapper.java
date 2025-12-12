package moe.ichinomiya.naven.protocols.HuaYuTing.world;

import moe.ichinomiya.naven.BlinkFix;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class Wrapper {
    public static final Logger logger = LogManager.getLogger(Wrapper.class);
    public static final WorldManager worldManager = new WorldManager();

    public Wrapper() {
        BlinkFix.getInstance().getEventManager().register(worldManager);
    }

    public static void runOnMainThread(Runnable var0) {
        getMinecraft().addScheduledTask(var0);
    }

    public static int getRenderDistance() {
        return getMinecraft().gameSettings.renderDistanceChunks;
    }    private static Minecraft mc = getMinecraft();

    public static File getFile(String... var0) {
        return new File(getMcDataDir(), Arrays.stream(var0).map((var0x) -> (new StringBuilder()).insert(0, "/").append(var0x).toString()).collect(Collectors.joining()));
    }

    public static Minecraft getMinecraft() {
        if (mc == null) {
            mc = Minecraft.getMinecraft();
        }

        return mc;
    }

    public static World getWorld() {
        return getMinecraft().theWorld;
    }

    public static Entity getPlayer() {
        return getMinecraft().getRenderViewEntity();
    }

    public static File getMcDataDir() {
        return getMinecraft().mcDataDir;
    }

    public static byte[] encode(String s) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gZIPOutputStream.write(s.getBytes(StandardCharsets.UTF_8));
        gZIPOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    @EventTarget
    public void onRespawn(EventRespawn e) throws IOException {
        if (e.getType() == EventType.JOIN_GAME) {
            if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() >= 757) {
                mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("forge")));
            } else if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() > 47) {
                mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("fml,forge")));

                byte[] modList = "\u0002\u001c\tminecraft\u00061.12.2\tdepartmod\u00031.0\rscreenshotmod\u00031.0\u0003ess\u00051.0.2\u0007vexview\u00062.6.10\u0012basemodneteasecore\u00051.9.4\nsidebarmod\u00031.0\u000bskincoremod\u00061.12.2\u000ffullscreenpopup\f1.12.2.38000\bstoremod\u00031.0\u0003mcp\u00049.42\u0007skinmod\u00031.0\rplayermanager\u00031.0\rdepartcoremod\u00061.12.2\tmcbasemod\u00031.0\u0011mercurius_updater\u00031.0\u0003FML\t8.0.99.99\u000bneteasecore\u00061.12.2\u0007antimod\u00032.0\u000bfoamfixcore\u00057.7.4\nnetworkmod\u00061.11.2\u0007foamfix\t@VERSION@\u0005forge\f14.23.5.2768\rfriendplaymod\u00031.0\u0004libs\u00051.0.2\tfiltermod\u00031.0\u0007germmod\u00053.4.2\tpromotion\u000e1.0.0-SNAPSHOT\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000".getBytes();
                byte[] channels = "FML|HS\u0000FML\u0000FML|MP\u0000FML\u0000antimod\u0000ChatVexView\u0000Base64VexView\u0000HudBase64VexView\u0000FORGE\u0000germplugin-netease\u0000VexView\u0000hyt0\u0000armourers\u0000promotion".getBytes();
                byte[] vexView = encode("{\"packet_sub_type\":\"814:469\",\"packet_data\":\"2.6.10\",\"packet_type\":\"ver\"}");

                mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("REGISTER", new PacketBuffer(Unpooled.buffer().writeBytes(channels))));
                mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("FML|HS", new PacketBuffer(Unpooled.buffer().writeBytes(modList))));
                mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("VexView", new PacketBuffer(Unpooled.buffer().writeBytes(vexView))));
            } else {
                mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("vanilla")));
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof S3FPacketCustomPayload) {
            S3FPacketCustomPayload packet = (S3FPacketCustomPayload) e.getPacket();

            String channel = packet.getChannelName();
            ByteBuf payload = packet.getBufferData();

            int oldIndex = payload.readerIndex();
            byte[] data = new byte[payload.readableBytes()];
            payload.readBytes(data);
            payload.readerIndex(oldIndex);

            if (channel.equals("hyt0")) {
                Wrapper.runOnMainThread(() -> {
                    this.processMapLoading(payload);
                });
            }
        }
    }

    public void processMapLoading(ByteBuf var1) {
        PacketBuffer buffer = new PacketBuffer(var1);
        byte var3 = buffer.readByte();
        if (var3 == 0) {
            String worldName = buffer.readStringFromBuffer(123456);
            logger.info("客户端预加载世界 {}", new Object[]{worldName});
            worldManager.setWorldName(worldName);
        } else {
            if (var3 == 1) {
                World world = Wrapper.getWorld();
                if (!(world instanceof WorldClient)) {
                    logger.info("世界未初始化", new Object[0]);
                    return;
                }

                short size = buffer.readShort();
                logger.info("开始客户端加载 {} 个区块", new Object[]{size});
                worldManager.worldInit(mc.theWorld);

                for (int i = 0; i < size; ++i) {
                    int chuckX = buffer.readInt();
                    int chuckZ = buffer.readInt();
                    logger.info("加载区块坐标 {}", new Object[]{chuckX + " " + chuckZ});
                    worldManager.loadChunk(chuckX, chuckZ);
                }
            }

        }
    }
}
