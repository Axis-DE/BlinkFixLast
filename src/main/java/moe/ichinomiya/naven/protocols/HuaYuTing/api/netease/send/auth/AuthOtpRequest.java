package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.auth;

import com.google.gson.annotations.SerializedName;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive.LoginOtpResponse;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.SAuth;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.WPFHwid;

import static moe.ichinomiya.naven.protocols.HuaYuTing.api.NavenAPi.GSON;

public class AuthOtpRequest {
    @SerializedName("otp_token")
    public String otp_token;
    @SerializedName("otp_pwd")
    public Object otp_pwd;
    @SerializedName("aid")
    public long accountId;
    @SerializedName("sauth_json")
    public String sauth_json;
    @SerializedName("sa_data")
    public String sa_data;
    @SerializedName("version")
    public String version;

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public AuthOtpRequest(WPFHwid sa_data, SAuth sauth, Version version, LoginOtpResponse otpResponse) {
        this.sa_data = GSON.toJson(sa_data);
        this.sauth_json = GSON.toJson(sauth);
        this.version = GSON.toJson(version);
        this.accountId = otpResponse.getAid();
        this.otp_token = otpResponse.getOtpToken();
        this.otp_pwd = "";
    }

    public static class Version {
        @SerializedName("version")
        public String version;
        @SerializedName("launcher_md5")
        public String launcher_md5;
        @SerializedName("updater_md5")
        public String updater_md5;

        public Version(String version) {
            this.version = version;
            this.launcher_md5 = "";
            this.updater_md5 = "";
        }
    }
}
