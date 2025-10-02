package dev.yalan.live;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jnic.JNICInclude;
import lombok.Getter;
import tech.skidonion.obfuscator.annotations.ControlFlowObfuscation;

import java.util.Optional;
import java.util.UUID;

@Getter
@ControlFlowObfuscation
@JNICInclude
public class LiveUser {
    private final String clientId;
    private final UUID userId;
    private final JsonObject payload;
    private final Level level;

    public LiveUser(String clientId, UUID userId, JsonObject payload) {
        this.clientId = clientId;
        this.userId = userId;
        this.payload = payload;
        this.level = isBlinkFixUser() ? Level.of(payload.get("level").getAsString()) : Level.FOREIGNER;
    }

    public String getName() {
        return payload.get("username").getAsString();
    }

    public String getRank() {
        return Optional.ofNullable(payload.get("rank")).map(JsonElement::getAsString).orElse(null);
    }

    public boolean isBlinkFixUser() {
        return "BlinkFix".equals(clientId);
    }

    public boolean isSupportedClientUser() {
        return isBlinkFixUser();
    }

    public enum Level {
        GENERAL("Normal"),
        ADMINISTRATOR("Administrator"),
        FOREIGNER("Foreigner"),
        UNKNOWN("Unknown");

        private final String formalName;

        Level(String formalName) {
            this.formalName = formalName;
        }

        public static Level of(String name) {
            for (Level level : values()) {
                if (level.formalName.equalsIgnoreCase(name)) {
                    return level;
                }
            }

            return UNKNOWN;
        }
    }
}
