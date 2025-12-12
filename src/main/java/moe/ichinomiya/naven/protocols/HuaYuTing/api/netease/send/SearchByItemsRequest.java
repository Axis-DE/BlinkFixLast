package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class SearchByItemsRequest {
    @SerializedName("item_ids")
    public List<String> items;

    public SearchByItemsRequest(List<String> items) {
        this.items = items;
    }

    public SearchByItemsRequest(String... items) {
        this(Arrays.stream(items).toList());
    }
}
