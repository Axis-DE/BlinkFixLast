package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.receive;

import com.google.gson.annotations.SerializedName;

import static axis.shiyan.wei.bluearchive.blinkfix.protocols.api.NavenAPi.GSON;

public class LoginOtpResponse extends MessageResponse {
    @SerializedName("details")
    public String details;
    @SerializedName("entity")
    public Entity entity;

    public String getOtpToken() {
        return this.entity == null ? "null" : this.entity.otp_token;
    }

    public long getAid() {
        return this.entity == null ? 0 : this.entity.aid;
    }

    public static LoginOtpResponse fromJson(String text) {
        return GSON.fromJson(text, LoginOtpResponse.class);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public static class Entity {
        @SerializedName("otp")
        public int otp;
        @SerializedName("otp_token")
        public String otp_token;
        @SerializedName("aid")
        public long aid;
        @SerializedName("lock_time")
        public long lock_time;
        @SerializedName("open_otp")
        public int open_otp;
    }
}
