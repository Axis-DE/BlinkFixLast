package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.auth;

import com.google.gson.annotations.SerializedName;

import static moe.ichinomiya.naven.protocols.HuaYuTing.api.NavenAPi.GSON;

public class AuthUpdateRequest {
    @SerializedName("entity_id")
    public long aid;

    public AuthUpdateRequest(long aid) {
        this.aid = aid;
    }

    public AuthUpdateRequest(String en) {
        this.aid = Long.parseLong(en);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
