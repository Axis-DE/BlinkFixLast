package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.send;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class ItemDownloadListRequest {
    @SerializedName("item_id_list")
    public List<String> items;

    public ItemDownloadListRequest(List<String> items) {
        this.items = items;
    }

    public ItemDownloadListRequest(String... item) {
        this(Arrays.stream(item).toList());
    }
}
