package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.send;

import com.google.gson.annotations.SerializedName;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.X19Crypt;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.objs.WPFVersionInfo;

public class WPFHwid {
    @SerializedName("os_name")
    public String os_name;
    @SerializedName("os_ver")
    public String os_ver;
    @SerializedName("mac_addr")
    public String mac_addr;
    @SerializedName("udid")
    public String udid;
    @SerializedName("app_ver")
    public String app_ver;
    @SerializedName("sdk_ver")
    public String sdk_ver;
    @SerializedName("network")
    public String network;
    @SerializedName("disk")
    public String disk;
    @SerializedName("is64bit")
    public String is64bit;
    @SerializedName("video_card1")
    public String video_card1;
    @SerializedName("video_card2")
    public String video_card2;
    @SerializedName("video_card3")
    public String video_card3;
    @SerializedName("video_card4")
    public String video_card4;
    @SerializedName("launcher_type")
    public String launcher_type;
    @SerializedName("pay_channel")
    public String pay_channel;

    public WPFHwid(boolean is4399, String os_name, String os_ver, String mac_addr, String udid_sa, String app_ver, String disk) {
        this.os_name = os_name;
        this.os_ver = os_ver;
        this.mac_addr = mac_addr;
        this.udid = udid_sa;
        this.app_ver = app_ver;
        this.disk = disk;

        this.sdk_ver = "";
        this.network = "";
        this.launcher_type = "PC_java";
        this.pay_channel = is4399 ? "4399" : "api";
        this.is64bit = "1";
        this.video_card1 = "NVIDIA GeForce RTX 4090";
        this.video_card2 = "NVIDIA GeForce RTX 4060";
        this.video_card3 = "";
        this.video_card4 = "";
    }

    public static WPFHwid random(WPFVersionInfo ver) {
        return new WPFHwid(
                false,
                "windows",
                "Microsoft Windows 10",
                X19Crypt.generateMACAddress(),
                X19Crypt.generateUDID(),
                ver.version,
                X19Crypt.generateDiskID()
        );
    }
}
