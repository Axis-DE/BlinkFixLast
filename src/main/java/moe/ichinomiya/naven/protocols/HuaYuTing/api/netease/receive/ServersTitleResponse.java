package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServersTitleResponse extends MessageResponse {
    @SerializedName("details")
    public String details;
    @SerializedName("summary_md5")
    public String summaryMd5;
    @SerializedName("entities")
    public List<Title> entities;

    @SerializedName("total")
    public int total;   

    public Title find(String id) {
        return entities.stream().filter(c -> c.itemId.equals(id)).findFirst().orElse(null);
    }

    public static class Title {
        @SerializedName("entity_id")
        public String entityId;
        @SerializedName("item_id")
        public String itemId;
        @SerializedName("channel_id")
        public int channelId;
        @SerializedName("title_image_url")
        public String icon;
    }
}
