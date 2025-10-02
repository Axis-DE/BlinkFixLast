package axis.shiyan.wei.bluearchive.blinkfix.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharedESPData {
    public String displayName;
    public double posX, posY, posZ;
    public double health, maxHealth, absorption;
    public double[] renderPosition;
    public String[] tags;
    public long updateTime;
}
