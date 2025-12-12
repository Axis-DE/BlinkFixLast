package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class ItemSearchChannelRequest {
    @SerializedName("channel_id")
    public int channel_id;
    @SerializedName("item_id_list")
    public List<String> items;

    public ItemSearchChannelRequest(int channel_id, List<String> items) {
        this.channel_id = channel_id;
        this.items = items;
    }

    public ItemSearchChannelRequest(int channel_id,String... items) {
        this(channel_id, Arrays.stream(items).toList());
    }

    public static final int NETEASE_SERVER = 24;

}
