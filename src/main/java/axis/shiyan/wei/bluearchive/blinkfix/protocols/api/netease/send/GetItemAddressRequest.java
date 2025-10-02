package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.send;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetItemAddressRequest {
    @SerializedName("item_id")
    public String item;
}
