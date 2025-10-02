package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventKey;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMouseClick;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRenderTick;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.clickgui.ClientClickGUI;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "FreeLook", description = "Allows you to look around freely", category = Category.RENDER)
public class FreeLook extends Module {
    public boolean altIsDown = false;
    public boolean last = false;

    public float cameraYaw = 0;
    public float cameraPitch = 0;
    public float playerYaw = 0;
    public float playerPitch = 0;
    public int originPersonView = 0;
    public float originalYaw = 0;
    public float originalPitch = 0;
    boolean mouseDown = false;

    @Override
    public void toggle() {
        if (mc.currentScreen instanceof ClientClickGUI) {
            super.toggle();
        }
    }

    public boolean isKeyDown() {
        return mouseDown;
    }

    @EventTarget
    public void onKey(EventKey e) {
        if (e.getKey() == getKey()) {
            mouseDown = e.isState();
        }
    }

    @EventTarget
    public void onMouse(EventMouseClick e) {
        if (e.getKey() == -getKey()) {
            mouseDown = e.isState();
        }
    }

    @EventTarget
    public void onUpdate(EventRenderTick e) {
        if (mc.thePlayer == null) return;
        if (mc.currentScreen != null) return;

        if (isKeyDown() && last) {
            update(e.getType() == EventType.PRE);
        }

        if (!isKeyDown() && last) {
            reset();
            last = false;
        }

        if (isKeyDown()) {
            if (!altIsDown) {
                setCamera();
            }
            altIsDown = true;
            last = true;
        } else {
            altIsDown = false;
        }
    }

    public void update(boolean start) {
        Entity player = mc.getRenderViewEntity();

        if (player == null) {
            return;
        }

        updateCamera();
        if (start) {
            player.rotationYaw = player.prevRotationYaw = cameraYaw;
            player.rotationPitch = player.prevRotationPitch = -cameraPitch;
        } else {
            player.rotationYaw = mc.thePlayer.rotationYaw - cameraYaw + playerYaw;
            player.prevRotationYaw = mc.thePlayer.prevRotationYaw - cameraYaw + playerYaw;

            player.rotationPitch = -playerPitch;
            player.prevRotationPitch = -playerPitch;
        }
    }

    private void updateCamera() {
        if (!mc.inGameHasFocus) {
            return;
        }

        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;

        float dx = Mouse.getDX() * f1 * 0.15f;
        float dy = Mouse.getDY() * f1 * 0.15f;

        if (isKeyDown()) {
            cameraYaw += dx;
            cameraPitch += dy;

            cameraPitch = MathHelper.clamp_float(cameraPitch, -90.0F, 90.0F);
        }
    }

    public void reset() {
        cameraYaw = originalYaw;
        cameraPitch = originalPitch;

        playerYaw = originalYaw;
        playerPitch = originalPitch;
        mc.gameSettings.thirdPersonView = originPersonView;
    }

    public void setCamera() {
        originPersonView = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = 1;

        cameraYaw = playerYaw = originalYaw = mc.thePlayer.rotationYaw;
        cameraPitch = playerPitch = originalPitch = -mc.thePlayer.rotationPitch;
    }
}
