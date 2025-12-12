package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive;

import com.google.gson.annotations.SerializedName;

import static moe.ichinomiya.naven.protocols.HuaYuTing.api.NavenAPi.GSON;

public class AuthOtpResponse extends MessageResponse {
    @SerializedName("details")
    public String details;
    @SerializedName("entity")
    public Entity entity;

    public static AuthOtpResponse fromJson(String json) {
        return GSON.fromJson(json, AuthOtpResponse.class);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public static class Entity {
        @SerializedName("entity_id")
        public String entity_id;
        @SerializedName("account")
        public String account;
        @SerializedName("token")
        public String token;
        @SerializedName("sead")
        public String sead;
        @SerializedName("lock_time")
        public long lock_time;
        @SerializedName("hasMessage")
        public boolean hasMessage;
        @SerializedName("aid")
        public long aid;
        @SerializedName("sdkuid")
        public String sdkuid;
        @SerializedName("access_token")
        public String access_token;
        @SerializedName("unisdk_login_json")
        public String unisdk_login_json_base64;
        @SerializedName("verify_status")
        public String verify_status;
        @SerializedName("hasGmail")
        public boolean hasGmail;
        @SerializedName("is_register")
        public boolean is_register;
        @SerializedName("env")
        public String env;
        @SerializedName("min_engine_version")
        public String min_engine_version;
        @SerializedName("min_patch_version")
        public String min_patch_version;

        @Override
        public String toString() {
            return GSON.toJson(this);
        }
    }
}

