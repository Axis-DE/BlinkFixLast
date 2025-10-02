package dev.yalan.live.netty.handler;

import com.google.gson.JsonObject;
import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;
import dev.yalan.live.events.*;
import dev.yalan.live.netty.LiveByteBuf;
import dev.yalan.live.netty.LiveProto;
import dev.yalan.live.netty.codec.crypto.AESDecoder;
import dev.yalan.live.netty.codec.crypto.AESEncoder;
import axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.NetEaseAltManager.GuiAltLogin;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jnic.JNICInclude;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

@JNICInclude
public class LiveHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LogManager.getLogger("LiveHandler");
    private final HashMap<Integer, BiConsumer<ChannelHandlerContext, LiveByteBuf>> functionMap = new HashMap<>();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final LiveClient live;

    private boolean notCheckedProtocolVersion = true;

    public LiveHandler(LiveClient live) {
        this.live = live;
        this.functionMap.put(0, this::handleHandshake);
        this.functionMap.put(1, this::handleKeepAlive);
        this.functionMap.put(2, this::handleGenericMessage);
        this.functionMap.put(3, this::handleAuthenticationResult);
        this.functionMap.put(4, this::handleChat);
        this.functionMap.put(5, this::handleQueryResultMinecraftProfile);
        this.functionMap.put(6, this::handleCustomOperation);
        this.functionMap.put(7, this::handleCommandOut);
        this.functionMap.put(8, this::handleNeteaseAPIResult);
        this.functionMap.put(9, this::handleCookie);
    }

    private void handleHandshake(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final SecretKeySpec aesKey = new SecretKeySpec(buf.readByteArray(16), "AES");
        final byte[] aesAAD = "UXoa2diI5LuS3roA".getBytes(StandardCharsets.UTF_8);

        ctx.pipeline().replace("rsa_decoder", "aes_decoder", new AESDecoder(aesKey, aesAAD));
        ctx.pipeline().replace("rsa_encoder", "aes_encoder", new AESEncoder(aesKey, aesAAD));
        LiveProto.sendPacket(ctx.channel(), LiveProto.createVerify("0OgYtVDZjv7mMbip", ZoneId.systemDefault().getId(), Instant.now().toEpochMilli())).syncUninterruptibly();

        final String autoUsername = live.autoUsername.get();
        final String autoPassword = live.autoPassword.get();

        if (autoUsername != null && autoPassword != null) {
            LiveProto.sendPacket(ctx.channel(), LiveProto.createAuthentication(autoUsername, autoPassword, live.getHardwareId()));
        }
    }

    private void handleKeepAlive(ChannelHandlerContext ctx, LiveByteBuf buf) {
        LiveProto.sendPacket(ctx.channel(), LiveProto.createKeepAlive());
    }

    private void handleGenericMessage(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final String channel = buf.readUTF();
        final String message = buf.readUTF();

        logger.info("[GenericMessage] Channel({}): {}", channel, message);

        mc.addScheduledTask(() -> {
            live.eventManager.call(new EventLiveGenericMessage(channel, message));
        });
    }

    private void handleAuthenticationResult(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final boolean isSuccess = buf.readBoolean();
        final String message = buf.readUTF();
        final UUID userId;
        final String username;
        final String userRank;
        final String userLevel;

        if (isSuccess) {
            userId = buf.readUUID();
            username = buf.readUTF();
            userRank = buf.readUTF();
            userLevel = buf.readUTF();
        } else {
            userId = new UUID(0L, 0L);
            username = "";
            userRank = "";
            userLevel = "";
        }

        logger.info("[Authentication] isSuccess({}) Message({})", isSuccess, message);

        mc.addScheduledTask(() -> {
            if (isSuccess) {
                final JsonObject payload = new JsonObject();
                payload.addProperty("username", username);
                payload.addProperty("rank", userRank);
                payload.addProperty("level", userLevel);

                live.liveUser = new LiveUser("BlinkFix", userId, payload);
            }

            live.eventManager.call(new EventLiveAuthenticationResult(isSuccess, message));
        });
    }

    private void handleChat(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final String channel = buf.readUTF();
        final String payloadString = buf.readUTF();

        logger.info("[LiveChat] Channel({}) Payload({})", channel, payloadString);

        mc.addScheduledTask(() -> live.liveComponent.handleChat(channel, payloadString));
    }

    private void handleQueryResultMinecraftProfile(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final UUID mcUUID = buf.readUUID();
        final String clientId = buf.readUTF();
        final UUID userId = buf.readUUID();
        final String userPayload = buf.readUTF();

        mc.addScheduledTask(() -> live.liveComponent.handleQueryResultMinecraftProfile(mcUUID, clientId, userId, userPayload));
    }

    private void handleCustomOperation(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final String operationName = buf.readUTF();
        final String payload = buf.readUTF();

        mc.addScheduledTask(() -> {
            switch (operationName) {
                case "Kick" -> {
                    if (mc.theWorld != null && mc.getNetHandler() != null && !mc.isSingleplayer()) {
                        final JsonObject payloadObject = LiveClient.JSON_PARSER.parse(payload).getAsJsonObject();

                        mc.getNetHandler().getNetworkManager().closeChannel(new ChatComponentText(
                                "你被" + payloadObject.get("operator").getAsString() + "踢出服务器: " + payloadObject.get("reason").getAsString()
                        ));
                    }
                }
                case "Crash" -> {
                    final JsonObject payloadObject = LiveClient.JSON_PARSER.parse(payload).getAsJsonObject();

                    live.crashMessage.set("你被" + payloadObject.get("operator").getAsString() + "崩溃游戏: " + payloadObject.get("reason").getAsString());
                    mc.shutdown();
                }
            }
        });
    }

    private void handleCommandOut(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final UUID executionId = buf.readUUID();
        final String type = buf.readUTF();
        final String out = buf.readUTF();
    }

    private void handleNeteaseAPIResult(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final String resultJson = buf.readUTF();

        NetHandlerLoginClient.lock.lock();
        try {
            NetHandlerLoginClient.condition.signal();
        } finally {
            NetHandlerLoginClient.lock.unlock();
        }
    }

    private void handleCookie(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final String cookie = buf.readUTF();

        mc.addScheduledTask(() -> {
            if (mc.currentScreen instanceof GuiAltLogin al) {
                if (al.getCookieButton != null && al.username != null) {
                    al.getCookieButton.enabled = true;
                    al.username.setText(cookie);
                    al.status = EnumChatFormatting.GREEN + "获取成功! 请点击 '添加' 添加到账号列表里";
                }
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        if (buf.readableBytes() < 4) {
            logger.warn("Channel{} was sent an malformed(length is less than 4) packet", ctx.channel());
            return;
        }

        if (notCheckedProtocolVersion) {
            notCheckedProtocolVersion = false;

            final int serversideProtocolVersion = buf.readInt();

            if (serversideProtocolVersion != LiveProto.PROTOCOL_VERSION) {
                logger.warn("ProtocolVersion doesn't match ({}, {})", LiveProto.PROTOCOL_VERSION, serversideProtocolVersion);
                ctx.channel().close();
            }

            return;
        }

        final int packetId = buf.readInt();
        final BiConsumer<ChannelHandlerContext, LiveByteBuf> handleFunction = functionMap.get(packetId);

        if (handleFunction == null) {
            logger.warn("Channel{} was sent an unrecognized PacketId({})", ctx.channel(), packetId);
            return;
        }

        handleFunction.accept(ctx, new LiveByteBuf(buf));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Channel active");

        mc.addScheduledTask(() -> {
            live.eventManager.call(new EventLiveChannelActive());
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("Channel inactive");

        mc.addScheduledTask(() -> {
            live.eventManager.call(new EventLiveChannelInactive());
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("LiveService Error occurred", cause);

        ctx.channel().close();

        mc.addScheduledTask(() -> {
            live.eventManager.call(new EventLiveChannelException(cause));
        });
    }
}
