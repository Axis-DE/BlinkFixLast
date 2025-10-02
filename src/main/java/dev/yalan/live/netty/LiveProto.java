package dev.yalan.live.netty;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import jnic.JNICInclude;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("CodeBlock2Expr")
@JNICInclude
public class LiveProto {
    public static final int PROTOCOL_VERSION = 0;

    public static ChannelFuture sendPacket(Channel channel, LivePacket packet) {
        final ByteBuf buf = channel.alloc().buffer();
        packet.write(new LiveByteBuf(buf));

        return channel.writeAndFlush(buf).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static LivePacket createHandshake() {
        return new LivePacket(0);
    }

    public static LivePacket createVerify(String verifyString, String clientZoneRegion, long clientTime) {
        return new LivePacket(1, (buf) -> {
            buf.writeUTF(verifyString);
            buf.writeUTF(clientZoneRegion);
            buf.writeLong(clientTime);
        });
    }

    public static LivePacket createKeepAlive() {
        return new LivePacket(2);
    }

    public static LivePacket createAuthentication(String username, String password, String hardwareId) {
        return new LivePacket(3, (buf) -> {
            buf.writeUTF(username);
            buf.writeUTF(password);
            buf.writeUTF(hardwareId);
        });
    }

    public static LivePacket createChat(String message) {
        return new LivePacket(4, (buf) -> {
            buf.writeUTF(message);
        });
    }

    public static LivePacket createUpdateMinecraftProfile(UUID mcUUID, String mcName) {
        return new LivePacket(5, (buf) -> {
            buf.writeUUID(mcUUID);
            buf.writeUTF(mcName);
        });
    }

    public static LivePacket createRemoveMinecraftProfile() {
        return new LivePacket(6);
    }

    public static LivePacket createQueryMinecraftProfile(GameProfile profile) {
        return createQueryMinecraftProfile(EntityPlayer.getUUID(profile));
    }

    public static LivePacket createQueryMinecraftProfile(UUID mcUUID) {
        return new LivePacket(7, (buf) -> {
            buf.writeUUID(mcUUID);
        });
    }

    public static LivePacket createSendOperation(String operationName, String username, String payload) {
        return new LivePacket(8, (buf) -> {
            buf.writeUTF(operationName);
            buf.writeUTF(username);
            buf.writeUTF(payload);
        });
    }

    public static LivePacket createExecuteCommand(UUID executionId, String commandString) {
        return new LivePacket(9, (buf) -> {
            buf.writeUUID(executionId);
            buf.writeUTF(commandString);
        });
    }

    public static LivePacket createJoinNeteaseServer(String userId, String userToken, String serverIdHash, String serverId, String serverVersion, String modHashList) {
        return new LivePacket(10, (buf) -> {
            buf.writeUTF(userId);
            buf.writeUTF(userToken);
            buf.writeUTF(serverIdHash);
            buf.writeUTF(serverId);
            buf.writeUTF(serverVersion);
            buf.writeUTF(modHashList);
        });
    }

    public static LivePacket createGetNeteaseCookie() {
        return new LivePacket(11);
    }

    public static class LivePacket {
        private final int id;
        private final Consumer<LiveByteBuf> writeFunc;

        public LivePacket(int id) {
            this(id, null);
        }

        public LivePacket(int id, Consumer<LiveByteBuf> writeFunc) {
            this.id = id;
            this.writeFunc = writeFunc;
        }

        public void write(LiveByteBuf buf) {
            buf.writeInt(id);

            if (writeFunc != null) {
                writeFunc.accept(buf);
            }
        }
    }
}
