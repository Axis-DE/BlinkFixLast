package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.auth;

import com.google.gson.annotations.SerializedName;

public class CreateGameCharacterRequest {
    @SerializedName("create_time")
    public long createTime;
    @SerializedName("entity_id")
    public String entity_id;
    @SerializedName("game_id")
    public String gameId;
    @SerializedName("game_type")
    public int game_type;
    @SerializedName("name")
    public String name;
    @SerializedName("user_id")
    public String user_id;

    public CreateGameCharacterRequest(String gameId, int game_type, String name, String user_id) {
        this.createTime = System.currentTimeMillis();
        this.entity_id = "0";
        this.gameId = gameId;
        this.game_type = game_type;
        this.name = name;
        this.user_id = user_id;
    }
}
