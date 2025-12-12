package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive;

import com.google.gson.annotations.SerializedName;

public class ServerAddressResponse extends MessageResponse {
    @SerializedName("details")
    public String details;
    @SerializedName("entity")
    public Data data;

    public static class Data {
        @SerializedName("ip")
        public String ip;
        @SerializedName("port")
        public int port;
        @SerializedName("announcement")
        public String announcement;
        @SerializedName("entity_id")
        public String serverId;
        @SerializedName("game_status")
        public int gameStatus;
        @SerializedName("in_whitelist")
        public boolean inWhitelist;
        @SerializedName("isp_enable")
        public boolean ispEnable;
    }
}
