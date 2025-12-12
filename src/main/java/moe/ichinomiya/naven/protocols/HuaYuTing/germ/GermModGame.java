package moe.ichinomiya.naven.protocols.HuaYuTing.germ;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GermModGame {
    private final String displayName;
    private final String sid;
    private final String description;
    private final int entry;
}
