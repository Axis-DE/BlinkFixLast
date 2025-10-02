package axis.shiyan.wei.bluearchive.blinkfix.ui.button;

import lombok.AllArgsConstructor;
import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SmoothAnimationTimer;

@AllArgsConstructor
@Getter
public class MainMenuButton {
    private final int id;
    private final String text;
    private final Runnable runnable;
    private final SmoothAnimationTimer timer = new SmoothAnimationTimer(60);
}
