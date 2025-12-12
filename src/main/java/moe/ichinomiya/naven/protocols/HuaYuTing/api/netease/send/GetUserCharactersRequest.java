package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send;

import com.google.gson.annotations.SerializedName;

public class GetUserCharactersRequest {
    @SerializedName("game_id")
    public String gameId;
    @SerializedName("game_type")
    public int game_type;
    @SerializedName("length")
    public int length;
    @SerializedName("offset")
    public int offset;
    @SerializedName("user_id")
    public String user_id;

    public GetUserCharactersRequest(String gameId, String user_id,int game_type) {
        this.gameId = gameId;
        this.user_id = user_id;

        this.game_type = game_type;
        this.offset = 0;
        this.length = 10;
    }

    public static final int NETEASE_SERVER_TYPE = 2;
}
