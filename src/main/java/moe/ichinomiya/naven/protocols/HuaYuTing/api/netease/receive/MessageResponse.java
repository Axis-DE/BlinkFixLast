package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("code")
    public int code;
    @SerializedName("message")
    public String message;
}
