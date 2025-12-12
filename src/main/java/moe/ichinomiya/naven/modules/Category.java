package moe.ichinomiya.naven.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Category {
    COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), MISC("Misc");

    private final String displayName;
}
