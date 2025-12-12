package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameCharactersResponse extends MessageResponse {
    @SerializedName("details")
    public String details;
    @SerializedName("entities")
    public List<Character> entities;
    @SerializedName("total")
    public int total;

    public Character get(int index) {
        return entities.get(index);
    }

    public static class Character {
        @SerializedName("entity_id")
        public String entityId;
        @SerializedName("game_id")
        public String gameId;
        @SerializedName("game_type")
        public int gameType;
        @SerializedName("user_id")
        public String userId;
        @SerializedName("name")
        public String name;
        @SerializedName("create_time")
        public long createTime;
        @SerializedName("expire_time")
        public long expireTime;
    }
}
