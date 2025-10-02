package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.send;

import com.google.gson.annotations.SerializedName;

import static axis.shiyan.wei.bluearchive.blinkfix.protocols.api.NavenAPi.GSON;

public class SAuth {
    @SerializedName("gameid")
    public String gameid;
    @SerializedName("login_channel")
    public String login_channel;
    @SerializedName("app_channel")
    public String app_channel;
    @SerializedName("platform")
    public String platform;
    @SerializedName("sdkuid")
    public String sdkuid;
    @SerializedName("sessionid")
    public String sessionid;
    @SerializedName("sdk_version")
    public String sdk_version;
    @SerializedName("udid")
    public String udid;
    @SerializedName("deviceid")
    public String deviceid;
    @SerializedName("aim_info")
    public String aim_info;

    public SAuth(String sdkuid, String sessionid, String sdk_version, String udid, String deviceid) {
        this.sdkuid = sdkuid;
        this.sessionid = sessionid;
        this.sdk_version = sdk_version;
        this.udid = udid;
        this.deviceid = deviceid;

        this.gameid = "x19";
        this.login_channel = "api";
        this.app_channel = "api";
        this.platform = "pc";
        this.aim_info = "{\"aim\":\"100.100.100.100\",\"country\":\"CN\",\"tz\":\"+0800\",\"tzid\":\"\"}";
    }

    public SAuth(String sauth_p) {
        SAuth temp = GSON.fromJson(sauth_p, SAuth.class);
        this.gameid = temp.gameid;
        this.login_channel = temp.login_channel;
        this.app_channel = temp.app_channel;
        this.platform = temp.platform;
        this.sdkuid = temp.sdkuid;
        this.sessionid = temp.sessionid;
        this.sdk_version = temp.sdk_version;
        this.udid = temp.udid;
        this.deviceid = temp.deviceid;
        this.aim_info = temp.aim_info;
    }

    public AimInfo getAimInfo() {
        return AimInfo.fromJson(this.aim_info);
    }

    public static class AimInfo {
        @SerializedName("aim")
        public String aim;
        @SerializedName("country")
        public String country;
        @SerializedName("tz")
        public String tz;
        @SerializedName("tzid")
        public String tzid;

        public static AimInfo fromJson(String text) {
            return GSON.fromJson(text, AimInfo.class);
        }

        @Override
        public String toString() {
            return GSON.toJson(this);
        }
    }

    public static SAuth fromJson(String text) {
        return GSON.fromJson(text, SAuth.class);
    }
    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
