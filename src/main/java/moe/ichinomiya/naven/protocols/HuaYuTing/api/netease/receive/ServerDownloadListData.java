package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServerDownloadListData extends MessageResponse {
    @SerializedName("details")
    public String details;
    @SerializedName("summary_md5")
    public String summaryMd5;
    @SerializedName("total")
    public int total;

    @SerializedName("entities")
    public List<Entity> entities;

    public static class Entity {
        @SerializedName("entity_id")
        public String entityId;
        @SerializedName("item_id")
        public String itemId;
        @SerializedName("itype")
        public String itype;
        @SerializedName("mtypeid")
        public String mtypeId;
        @SerializedName("stypeid")
        public String stypeId;
        @SerializedName("user_id")
        public String userId;
        @SerializedName("sub_entities")
        public List<SubEntity> subEntities;
    }

    public static class SubEntity {
        @SerializedName("entity_id")
        public String entityId;
        @SerializedName("jar_md5")
        public String jar_md5;
        @SerializedName("java_version")
        public String java_version;
        @SerializedName("mc_version_name")
        public String mc_version_name;
        @SerializedName("res_md5")
        public String res_md5;
        @SerializedName("res_name")
        public String res_name;
        @SerializedName("res_size")
        public int res_size;
        @SerializedName("res_url")
        public String res_url;
        @SerializedName("res_version")
        public int res_version;
    }
}
