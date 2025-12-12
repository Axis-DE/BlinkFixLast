package moe.ichinomiya.naven.protocols.HuaYuTing;

import moe.ichinomiya.naven.ui.notification.Notification;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.Version;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class MythProtocol {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Map<UUID, SkinData> skins = new ConcurrentHashMap<>();
    private static final Set<UUID> requested = new HashSet<>();

    public static Optional<SkinData> getSkin(UUID uuid) {
        return Optional.ofNullable(skins.get(uuid));
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        if (e.getType() == EventType.JOIN_GAME) {
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeString("Version");
            buffer.writeString(BlinkFix.CLIENT_DISPLAY_NAME + " " + Version.getVersion());
            mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("MythGameAPI", buffer));
            requested.clear();
            skins.clear();
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getType() == EventType.RECEIVE) {
            if (event.getPacket() instanceof S38PacketPlayerListItem) {
                S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.getPacket();

                if (packet.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                    for (S38PacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                        if (data.getProfile() != null) {
                            UUID id = data.getProfile().getId();

                            if (!requested.contains(id)) {
                                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                                buffer.writeString("Skin");
                                buffer.writeString(id.toString());
                                mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("MythGameAPI", buffer));
                                requested.add(id);
                                log.info("Requesting skin for {}", id.toString());
                            }
                        }
                    }
                }
            } else if (event.getPacket() instanceof S3FPacketCustomPayload) {
                S3FPacketCustomPayload packet = (S3FPacketCustomPayload) event.getPacket();

                if (packet.getChannelName().equals("MythGameAPI")) {
                    ByteBuf payload = packet.getBufferData();
                    PacketBuffer buffer = new PacketBuffer(payload);

                    String packetType = buffer.readStringFromBuffer(100);

                    if (packetType.equals("Skin")) {
                        long time = System.currentTimeMillis();
                        UUID uuid = UUID.fromString(buffer.readStringFromBuffer(64));
                        String skinUrl = buffer.readStringFromBuffer(1000);
                        int skinType = buffer.readInt();

                        try {
                            BufferedImage image = ImageIO.read(new URL(skinUrl));
                            ResourceLocation resource = new ResourceLocation("MythSkin-" + uuid);
                            Minecraft.getMinecraft().addScheduledTask(() -> {
                                Minecraft.getMinecraft().getTextureManager().loadTexture(resource, new DynamicTexture(image));
                                skins.put(uuid, new SkinData(resource, skinType));
                                log.info("Received skin for {}, using time: {}ms", uuid, System.currentTimeMillis() - time);
                            });
                        } catch (IOException e) {
                            log.error("Failed to load skin!", e);
                        }
                    } else if (packetType.equals("Version")) {
                        String version = buffer.readStringFromBuffer(32);
                        BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.INFO, "Myth (" + version + ") Proxied Server", 3000));
                    } else {
                        log.warn("Unknown Myth packet type: {}", packetType);
                    }
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class SkinData {
        private final ResourceLocation location;
        private final int skinType;
    }
}
