package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.receive;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AvailableQueryResponse extends MessageResponse {
    @SerializedName("details")
    public String details;
    @SerializedName("summary_md5")
    public String summaryMd5;
    @SerializedName("entities")
    public List<Server> entities;

    public static class Server {
        @SerializedName("entity_id")
        public String id;
        @SerializedName("item_type")
        public int item_type;
        @SerializedName("name")
        public String name;
        @SerializedName("item_version")
        public String itemVersion;
        @SerializedName("developer_name")
        public String developers;
        @SerializedName("master_type_id")
        public String masterTypeId;
        @SerializedName("secondary_type_id")
        public String secondaryTypeId;
        @SerializedName("brief_summary")
        public String briefSummary;
        @SerializedName("balance_grade")
        public int balanceGrade;
        @SerializedName("available_scope")
        public int availableScope;
        @SerializedName("review_status")
        public int reviewStatus;
        @SerializedName("publish_time")
        public long publishTime;
        @SerializedName("is_auth")
        public boolean isAuth;
        @SerializedName("goods_state")
        public int goodsState;
        @SerializedName("resource_version")
        public int resourceVersion;
        @SerializedName("game_status")
        public int gameStatus;
        @SerializedName("mod_id")
        public int modId;
        @SerializedName("vip_only")
        public boolean vipOnly;
        @SerializedName("season_begin")
        public int seasonBegin;
        @SerializedName("season_number")
        public int seasonNumber;
        @SerializedName("is_apollo")
        public int isApollo;
        @SerializedName("rarity")
        public int rarity;
        @SerializedName("online_count")
        public String onlineCount;
        @SerializedName("effect_mtypeid")
        public int effectMtypeId;
        @SerializedName("effect_stypeid")
        public int effectStypeId;
        @SerializedName("rel_iid")
        public String relIid;
        @SerializedName("lobby_min_num")
        public int lobbyMinNum;
        @SerializedName("lobby_max_num")
        public int lobbyMaxNum;
        @SerializedName("download_num")
        public int downloads;
        @SerializedName("like_num")
        public int likes;
        @SerializedName("is_current_season")
        public boolean isCurrentSeason;
        @SerializedName("is_has")
        public boolean isHas;
    }
}
