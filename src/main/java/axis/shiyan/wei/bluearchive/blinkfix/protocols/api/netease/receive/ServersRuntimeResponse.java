package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.receive;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServersRuntimeResponse extends MessageResponse {
    @SerializedName("details")
    public String details;
    @SerializedName("entities")
    public List<Data> entities;
    @SerializedName("count")
    public int count;

    public Data find(String id) {
        return entities.stream().filter(c -> c.itemId.equals(id)).findFirst().orElse(null);
    }

    public static class Data {
        @SerializedName("entity_id")
        public String entityId;
        @SerializedName("item_id")
        public String itemId;
        @SerializedName("java_version")
        public int javaVersion;
        @SerializedName("mc_version_id")
        public int mcVersionId;
    }
}
