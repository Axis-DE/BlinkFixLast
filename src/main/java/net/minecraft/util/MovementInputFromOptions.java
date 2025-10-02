package net.minecraft.util;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMoveInput;
import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput {
    private final GameSettings gameSettings;

    public MovementInputFromOptions(final GameSettings gameSettingsIn) {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown()) {
            ++this.moveForward;
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            --this.moveForward;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            ++this.moveStrafe;
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            --this.moveStrafe;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        final EventMoveInput eventMoveInput = new EventMoveInput(moveForward, moveStrafe, jump, sneak, 0.3D);
        BlinkFix.getInstance().getEventManager().call(eventMoveInput);

        final double sneakMultiplier = eventMoveInput.getSneakSlowDownMultiplier();
        this.moveForward = eventMoveInput.getForward();
        this.moveStrafe = eventMoveInput.getStrafe();
        this.jump = eventMoveInput.isJump();
        this.sneak = eventMoveInput.isSneak();

        if (this.sneak) {
            this.moveStrafe = (float) ((double) this.moveStrafe * sneakMultiplier);
            this.moveForward = (float) ((double) this.moveForward * sneakMultiplier);
        }
    }
}
