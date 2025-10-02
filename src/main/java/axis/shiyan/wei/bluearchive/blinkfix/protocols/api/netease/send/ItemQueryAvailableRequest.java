package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.send;

import com.google.gson.annotations.SerializedName;

import static axis.shiyan.wei.bluearchive.blinkfix.protocols.api.NavenAPi.GSON;

public class ItemQueryAvailableRequest {
    @SerializedName("item_type")
    public int item_type;
    @SerializedName("master_type_id")
    public int master_type_id;
    @SerializedName("price_type")
    public int price_type;
    @SerializedName("is_has")
    public boolean is_has;
    @SerializedName("offset")
    public int offset;
    @SerializedName("length")
    public int length;

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public ItemQueryAvailableRequest(int offset, int length) {
        this.offset = offset;
        this.length = length;

        this.item_type = 1;
        this.master_type_id = 2;
        this.price_type = 0;
        this.is_has = false;
    }
}
