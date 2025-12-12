package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetItemAddressRequest {
    @SerializedName("item_id")
    public String item;
}
